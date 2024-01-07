/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.agate.web.rest.ticket;

import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import io.jsonwebtoken.Claims;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.apache.shiro.subject.Subject;
import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;
import org.obiba.agate.domain.Application;
import org.obiba.agate.domain.Authorization;
import org.obiba.agate.domain.Ticket;
import org.obiba.agate.domain.User;
import org.obiba.agate.service.*;
import org.apache.oltu.oauth2.as.issuer.MD5Generator;
import org.apache.oltu.oauth2.as.issuer.OAuthIssuer;
import org.apache.oltu.oauth2.as.issuer.OAuthIssuerImpl;
import org.apache.oltu.oauth2.as.request.OAuthAuthzRequest;
import org.apache.oltu.oauth2.as.request.OAuthTokenRequest;
import org.apache.oltu.oauth2.as.response.OAuthASResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.OAuthResponse;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.obiba.agate.web.rest.security.AuthorizationValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;

@Component
@Path("/oauth2")
@Scope("request")
public class OAuthResource {

  private static final Logger log = LoggerFactory.getLogger(OAuthResource.class);

  private final List<String> AGATE_SCOPES = Lists.newArrayList("openid", "email", "profile");

  private final String APPLICATION_ATTRIBUTE = "application";

  @Inject
  private ConfigurationService configurationService;

  @Inject
  private AuthorizationService authorizationService;

  @Inject
  private ApplicationService applicationService;

  @Inject
  private UserService userService;

  @Inject
  private TicketService ticketService;

  @Inject
  private AuthorizationValidator authorizationValidator;

  @Inject
  private TokenUtils tokenUtils;

  @GET
  @Path("/authorize")
  public Response validateAuthorize(@Context HttpServletRequest servletRequest) throws URISyntaxException, OAuthSystemException {
    Subject subject = SecurityUtils.getSubject();
    if (subject.isAuthenticated()) {
      OAuthAuthzRequest oAuthRequest = null;
      String redirectURI = null;
      try {
        oAuthRequest = new OAuthAuthzRequest(servletRequest);
        String clientId = oAuthRequest.getParam(OAuth.OAUTH_CLIENT_ID);
        User user = userService.getCurrentUser();
        redirectURI = validateClientApplication(clientId, oAuthRequest.getParam(OAuth.OAUTH_REDIRECT_URI));
        // check user has access to the application
        authorizationValidator.validateApplication(servletRequest, user, clientId);
        SecurityUtils.getSubject().getSession().setAttribute(APPLICATION_ATTRIBUTE, clientId);
        validateScope(oAuthRequest.getScopes());
        OAuthRequestData data = new OAuthRequestData(clientId, redirectURI, oAuthRequest);
        Authorization authorization = authorizationService.find(user.getName(), data.getClientId());
        // check client previous authorizations
        boolean allScopesCovered = true;
        for (String scope : oAuthRequest.getScopes()) {
          // agate scopes do not need to be approved by user as all applications are trusted
          if (!AGATE_SCOPES.contains(scope) && authorization != null && !authorization.hasScope(scope)) {
            allScopesCovered = false;
            break;
          }
        }
        if (allScopesCovered) {
          return replyAuthorized(servletRequest, data, authorization);
        }
      } catch (ForbiddenException e) {
        return buildErrorResponse(e, oAuthRequest, redirectURI);
      } catch (Exception e) {
        // if any problem, continue with authorization page
        log.warn("Error when validating authorization", e);
      }
    }

    return Response.status(Response.Status.FOUND).location(URI.create(
        String.format("%s/authorize?%s", configurationService.getBaseURL(servletRequest), servletRequest.getQueryString()))).build();
  }

  @GET
  @Path("/userinfo")
  @RequiresRoles("openid")
  @Produces("application/json")
  public Claims userInfo(@Context HttpServletRequest servletRequest) {
    Subject subject = SecurityUtils.getSubject();
    String username = subject.getPrincipal().toString();

    return tokenUtils.buildClaims(username, getSupportedOpenIdScopes(subject::hasRole));
  }

  @GET
  @Path("/certs")
  @Produces("application/json")
  public Response certs(@Context HttpServletRequest servletRequest) throws JSONException {
    JSONObject jwks = new JSONObject();
    jwks.put("keys", new String[]{});
    return Response.ok(jwks.toString(2)).build();
  }


  @POST
  @Path("/authz")
  @RequiresRoles("agate-user")
  public Response authorize(@Context HttpServletRequest servletRequest, @FormParam("grant") Boolean grant)
      throws URISyntaxException, OAuthSystemException {
    return tryBuildResponse(servletRequest, (data) -> {
      try {
        if (grant != null && !grant) {
          return buildErrorResponse(OAuthProblemException.error("access_denied", "Owner denied authorization."), data.getRequest(), data.getRedirectUri());
        }
        Authorization authorization = doAuthorization(data);
        return replyAuthorized(servletRequest, data, authorization);
      } catch (URISyntaxException | OAuthSystemException | OAuthProblemException e) {
        throw Throwables.propagate(e);
      }
    });
  }

  @POST
  @Consumes("application/x-www-form-urlencoded")
  @Produces("application/json")
  @Path("/token")
  public Response access(@Context HttpServletRequest servletRequest, MultivaluedMap<String, String> formParams) throws OAuthSystemException {
    try {
      HttpServletRequest requestWrapper = new OAuthServletRequest(servletRequest, formParams);
      OAuthTokenRequest oAuthRequest = new OAuthTokenRequest(requestWrapper);
      GrantType type = GrantType.valueOf(oAuthRequest.getParam(OAuth.OAUTH_GRANT_TYPE).toUpperCase());

      switch (type) {
        case AUTHORIZATION_CODE:
          return accessAuthorizationCodeGrant(requestWrapper, oAuthRequest);
        case PASSWORD:
          return accessPasswordGrant(requestWrapper, oAuthRequest);
        default:
          OAuthResponse res = OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST).buildJSONMessage();
          return Response.status(res.getResponseStatus()).entity(res.getBody()).build();
      }
    } catch (OAuthProblemException e) {
      OAuthResponse res = OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST).error(e).buildJSONMessage();
      return Response.status(res.getResponseStatus()).entity(res.getBody()).build();
    } catch (Exception e) {
      OAuthResponse res = OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST) //
          .setError(e.getClass().getSimpleName()) //
          .setErrorDescription(e.getMessage()) //
          .buildJSONMessage();
      return Response.status(res.getResponseStatus()).entity(res.getBody()).build();
    }
  }

  @GET
  @Path("/logout")
  public Response logout(@QueryParam("post_logout_redirect_uri") String postLogoutRedirectUri) {
    // Redirect to the sign-out page
    try {
      UriBuilder builder = UriBuilder.fromUri(configurationService.getPublicUrl() + "/signout");
      if (!Strings.isNullOrEmpty(postLogoutRedirectUri)) {
        builder.queryParam("post_logout_redirect_uri", postLogoutRedirectUri);
      }
      return Response.temporaryRedirect(builder.build()).build();
    } catch (Exception e) {
      return Response.status(Response.Status.BAD_REQUEST).build();
    }
  }

  //
  // Private methods
  //

  private Authorization doAuthorization(OAuthRequestData data) throws OAuthSystemException {
    OAuthIssuer oAuthIssuer = new OAuthIssuerImpl(new MD5Generator());
    User user = userService.getCurrentUser();
    Authorization authorization = authorizationService.find(user.getName(), data.getClientId());

    if (authorization == null) {
      authorization = new Authorization(user.getName(), data.getClientId());
    }

    authorization.setCode(oAuthIssuer.authorizationCode());
    authorization.addScopes(data.getRequest().getScopes());
    authorization.addRedirectURI(data.getRedirectUri());
    authorizationService.save(authorization);
    return authorization;
  }

  private Response replyAuthorized(HttpServletRequest servletRequest, OAuthRequestData data, Authorization authorization) throws OAuthSystemException, URISyntaxException, OAuthProblemException {
    Authorization authz = authorization;
    OAuthAuthzRequest oAuthRequest = new OAuthAuthzRequest(servletRequest);
    String clientId = oAuthRequest.getParam(OAuth.OAUTH_CLIENT_ID);
    String redirectURI = validateClientApplication(clientId, oAuthRequest.getParam(OAuth.OAUTH_REDIRECT_URI));
    if (authorization == null) {
      authz = doAuthorization(new OAuthRequestData(clientId, redirectURI, oAuthRequest));
    } else {
      // case multiple urls are allowed per application
      authorization.addRedirectURI(redirectURI);
      authorizationService.save(authorization);
    }

    long expiresIn = authorizationService.getExpirationDate(authz).getMillis() - DateTime.now().getMillis();
    OAuthASResponse.OAuthAuthorizationResponseBuilder builder = OAuthASResponse
        .authorizationResponse(servletRequest, HttpServletResponse.SC_FOUND) //
        .setCode(authz.getCode()) //
        .setExpiresIn(expiresIn / 1000) //
        .location(data.getRedirectUri());

    setState(builder, data.getRequest());
    OAuthResponse response = builder.buildQueryMessage();
    return Response.status(response.getResponseStatus()).location(new URI(response.getLocationUri())).build();
  }

  private Response tryBuildResponse(HttpServletRequest servletRequest, Function<OAuthRequestData, Response> responseBuilder)
      throws URISyntaxException, OAuthSystemException {
    OAuthAuthzRequest oAuthRequest = null;
    String redirectURI = null;

    try {
      oAuthRequest = new OAuthAuthzRequest(servletRequest);
      String clientId = oAuthRequest.getParam(OAuth.OAUTH_CLIENT_ID);
      redirectURI = validateClientApplication(clientId, oAuthRequest.getParam(OAuth.OAUTH_REDIRECT_URI));
      // check user has access to the application
      authorizationValidator.validateApplication(servletRequest, userService.getCurrentUser(), clientId);
      validateScope(oAuthRequest.getScopes());
      return responseBuilder.apply(new OAuthRequestData(clientId, redirectURI, oAuthRequest));
    } catch (ForbiddenException | OAuthProblemException e) {
      return buildErrorResponse(e, oAuthRequest, redirectURI);
    } catch (RuntimeException e) {
      if (e.getCause() instanceof OAuthProblemException) {
        return buildErrorResponse((OAuthProblemException) e.getCause(), oAuthRequest, redirectURI);
      } else if (e.getCause() instanceof URISyntaxException) throw (URISyntaxException) e.getCause();

      throw e;
    } catch (Exception e) {
      return buildErrorResponse(OAuthProblemException.error("server_error", e.getMessage()), oAuthRequest, redirectURI);
    }
  }

  private Response buildErrorResponse(Exception e, OAuthAuthzRequest oAuthRequest, String redirectURI)
      throws OAuthSystemException, URISyntaxException {
    boolean canRedirect = !Strings.isNullOrEmpty(redirectURI);
    OAuthASResponse.OAuthErrorResponseBuilder builder = OAuthASResponse
        .errorResponse(canRedirect ? HttpServletResponse.SC_FOUND : HttpServletResponse.SC_BAD_REQUEST);
    if (e instanceof OAuthProblemException)
      builder.error((OAuthProblemException) e);
    else {
      builder.setError(e.getClass().getSimpleName());
      builder.setErrorDescription(e.getMessage());
    }
    setState(builder, oAuthRequest);
    OAuthResponse response;

    if (canRedirect) {
      builder.location(redirectURI);
      response = builder.buildQueryMessage();
      return Response.status(response.getResponseStatus()).location(new URI(response.getLocationUri())).build();
    } else {
      response = builder.buildJSONMessage();
      return Response.status(response.getResponseStatus()).entity(response.getBody()).build();
    }
  }

  private void validateScope(Set<String> scopes) throws OAuthProblemException {
    for (String s : scopes) {
      if (!TokenUtils.OPENID_SCOPES.contains(s)) {
        String[] scopeParts = s.split(":");
        Application application = applicationService.find(scopeParts[0]);

        if (application == null || (scopeParts.length > 1 && !application.hasScope(scopeParts[1])))
          throw OAuthProblemException.error("invalid_scope", String.format("Invalid scope %s", s));
      }
    }
  }

  private Response accessAuthorizationCodeGrant(HttpServletRequest servletRequest, OAuthTokenRequest oAuthRequest)
      throws OAuthSystemException, OAuthProblemException {
    validateClient(oAuthRequest);

    String clientId = oAuthRequest.getClientId();
    String redirectURI = oAuthRequest.getParam(OAuth.OAUTH_REDIRECT_URI);
    Authorization authorization = authorizationService.getByCode(oAuthRequest.getParam(OAuth.OAUTH_CODE));
    // verify authorization
    if (!authorization.getApplication().equals(clientId)) {
      throw OAuthProblemException
          .error("invalid_client_id", "The client ID does not match the one of the authorization");
    }
    if (!authorization.getRedirectURIs().contains(redirectURI)) {
      throw OAuthProblemException
          .error("invalid_redirect_uri", "The redirect URI does not match the one of the authorization");
    }
    User user = userService.findActiveUser(authorization.getUsername());
    if (user == null) {
      throw OAuthProblemException.error("inactive_user", "The user of the authorization is not active");
    }

    Ticket ticket = ticketService.create(authorization);
    return getAccessResponse(ticket, authorization, clientId);
  }

  private Response accessPasswordGrant(HttpServletRequest servletRequest, OAuthTokenRequest oAuthRequest)
      throws OAuthSystemException {
    validateClient(oAuthRequest);

    String clientId = oAuthRequest.getClientId();
    String username = oAuthRequest.getUsername();
    String password = oAuthRequest.getPassword();
    User user = userService.findActiveUser(username);
    if (user == null) user = userService.findActiveUserByEmail(username);

    authorizationValidator.validateUser(servletRequest, username, user);
    authorizationValidator.validateApplication(servletRequest, user, clientId);

    // check authentication
    Subject subject = SecurityUtils.getSubject();
    assert user != null;
    subject.login(new UsernamePasswordToken(user.getName(), password));
    authorizationValidator.validateRealm(servletRequest, user, subject);
    subject.logout();

    Ticket ticket = ticketService.create(user.getName(), false, false, clientId);
    return getAccessResponse(ticket, null, clientId);
  }

  private Response getAccessResponse(@Nonnull Ticket ticket, @Nullable Authorization authorization, String clientId) throws OAuthSystemException {
    String token = tokenUtils.makeAccessToken(ticket, clientId);
    long expiresIn = ticketService.getExpirationDate(ticket).getMillis() - DateTime.now().getMillis();

    OAuthASResponse.OAuthTokenResponseBuilder responseBuilder = OAuthASResponse.tokenResponse(HttpServletResponse.SC_OK) //
        .setAccessToken(token) //
        .setTokenType(OAuth.OAUTH_HEADER_NAME.toLowerCase()) // bug: OAUTH_BEARER_TOKEN has a wrong value
        .setExpiresIn(expiresIn / 1000 + "");

    if (authorization != null && authorization.hasScope(TokenUtils.OPENID_SCOPE)) {
      responseBuilder.setParam(TokenUtils.OPENID_TOKEN, tokenUtils.makeIDToken(authorization,
          getSupportedOpenIdScopes(authorization::hasScope)));
    }

    OAuthResponse response = responseBuilder.buildJSONMessage();
    return Response.status(response.getResponseStatus()).entity(response.getBody()).build();
  }

  private List<String> getSupportedOpenIdScopes(Function<String, Boolean> filter) {
    String[] supported = {TokenUtils.OPENID_EMAIL_SCOPE, TokenUtils.OPENID_PROFILE_SCOPE};
    return Arrays.stream(supported).filter(filter::apply).collect(toList());
  }

  /**
   * Set the state parameter if any was defined in the request into the response.
   *
   * @param builder
   * @param oAuthRequest
   */
  private void setState(OAuthResponse.OAuthResponseBuilder builder, OAuthAuthzRequest oAuthRequest) {
    if (oAuthRequest == null) return;
    String state = oAuthRequest.getState();
    if (!Strings.isNullOrEmpty(state)) {
      builder.setParam(OAuth.OAUTH_STATE, state);
    }
  }

  /**
   * Check that the application exists and is available for OAuth process (default redirect URI); check also that the
   * provided redirect URI includes the default URI same host: (and port, if any), same path or is sub-path.
   *
   * @param clientId    Client ID is the {@link Application} name
   * @param redirectURI Optional: if null or empty default Application's redirect URI is used else it must be a valid redirect URI.
   * @return the redirectURI
   */
  private String validateClientApplication(String clientId, String redirectURI) throws OAuthProblemException {
    Application application = applicationService.getApplication(clientId);
    if (!application.hasRedirectURI()) {
      throw OAuthProblemException
          .error("missing_application_redirect_uri", "Application does not have a default redirect URI");
    }
    // TODO? check user has access to this application
    // Verify the validity of the given URI
    String normalizedURI = redirectURI;
    List<String> defaultURIs = application.getRedirectURIs();
    if (Strings.isNullOrEmpty(redirectURI)) {
      normalizedURI = defaultURIs.get(0);
    } else if (defaultURIs.stream().noneMatch(normalizedURI::startsWith)) {
      throw OAuthProblemException
          .error("invalid_redirect_uri", "The redirect URI does not match the application's redirect URI");
    }
    return normalizedURI;
  }

  /**
   * Verify {@link Application}'s secret key.
   *
   * @param oAuthRequest
   */
  private void validateClient(OAuthTokenRequest oAuthRequest) {
    authorizationValidator.validateApplicationParameters(oAuthRequest.getClientId(), oAuthRequest.getClientSecret());
  }

  private static class OAuthRequestData {
    private String redirectUri;
    private String clientId;
    private OAuthAuthzRequest request;

    public OAuthRequestData(String clientId, String redirectUri, OAuthAuthzRequest request) {
      this.redirectUri = redirectUri;
      this.clientId = clientId;
      this.request = request;
    }

    public String getRedirectUri() {
      return redirectUri;
    }

    public void setRedirectUri(String redirectUri) {
      this.redirectUri = redirectUri;
    }

    public String getClientId() {
      return clientId;
    }

    public void setClientId(String clientId) {
      this.clientId = clientId;
    }

    public OAuthAuthzRequest getRequest() {
      return request;
    }

    public void setRequest(OAuthAuthzRequest request) {
      this.request = request;
    }
  }

  /**
   * Servlet request wrapper to workaround an incompatibility between Jersey and Jetty:
   * Jersey consumes the request's content input stream, and then the Jetty's Request
   * does not want to extract parameters.
   */
  private static class OAuthServletRequest extends HttpServletRequestWrapper {

    private final MultivaluedMap<String, String> formParams;

    /**
     * Constructs a request object wrapping the given request.
     *
     * @param request the {@link HttpServletRequest} to be wrapped.
     * @throws IllegalArgumentException if the request is null
     */
    public OAuthServletRequest(HttpServletRequest request, MultivaluedMap<String, String> formParams) {
      super(request);
      this.formParams = formParams;
    }

    @Override
    public Enumeration<String> getParameterNames() {
      return Collections.enumeration(formParams.keySet());
    }

    @Override
    public String getParameter(String name) {
      return formParams.getFirst(name);
    }

    @Override
    public String[] getParameterValues(String name) {
      return formParams.get(name).toArray(new String[0]);
    }
  }

}
