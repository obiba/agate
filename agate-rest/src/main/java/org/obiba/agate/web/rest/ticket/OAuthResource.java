/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.agate.web.rest.ticket;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;
import java.util.function.Function;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

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
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.apache.shiro.subject.Subject;
import org.joda.time.DateTime;
import org.obiba.agate.domain.Application;
import org.obiba.agate.domain.Authorization;
import org.obiba.agate.domain.Ticket;
import org.obiba.agate.domain.User;
import org.obiba.agate.service.ApplicationService;
import org.obiba.agate.service.AuthorizationService;
import org.obiba.agate.service.TicketService;
import org.obiba.agate.service.TokenUtils;
import org.obiba.agate.service.UserService;
import org.obiba.agate.web.rest.security.AuthorizationValidator;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;
import com.google.common.base.Throwables;

import io.jsonwebtoken.Claims;

@Component
@Path("/oauth2")
@Scope("request")
public class OAuthResource {

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
  public Response validateAuthorize(@Context HttpServletRequest servletRequest) {
    return Response.status(Response.Status.FOUND).location(URI.create(
        String.format("/#/authorize?%s", servletRequest.getQueryString()))).build();
  }

  @GET
  @Path("/userinfo")
  @RequiresRoles("openid")
  @Produces("application/json")
  public Claims userInfo(@Context HttpServletRequest servletRequest) {
    String username = SecurityUtils.getSubject().getPrincipal().toString();
    return tokenUtils.buildClaims(username);
  }

  @POST
  @Path("/authz")
  @RequiresRoles("agate-user")
  public Response authorize(@Context HttpServletRequest servletRequest, @FormParam("grant") boolean grant)
    throws URISyntaxException, OAuthSystemException {
    return tryBuildResponse(servletRequest, (data) -> {
      try {
        if(!grant) {
          return buildErrorResponse(OAuthProblemException.error("access_denied", "Owner denied authorization."), data.getRequest(), data.getRedirectUri());
        }

        OAuthIssuer oAuthIssuer = new OAuthIssuerImpl(new MD5Generator());
        User user = userService.getCurrentUser();
        Authorization authorization = authorizationService.find(user.getName(), data.getClientId());

        if(authorization == null) {
          authorization = new Authorization(user.getName(), data.getClientId());
        }

        authorization.setCode(oAuthIssuer.authorizationCode());
        authorization.setScopes(data.getRequest().getScopes());
        authorization.setRedirectURI(data.getRedirectUri());
        authorizationService.save(authorization);

        long expiresIn = authorizationService.getExpirationDate(authorization).getMillis() - DateTime.now().getMillis();
        OAuthASResponse.OAuthAuthorizationResponseBuilder builder = OAuthASResponse
            .authorizationResponse(servletRequest, HttpServletResponse.SC_FOUND) //
            .setCode(authorization.getCode()) //
            .setExpiresIn(expiresIn / 1000) //
            .location(data.getRedirectUri());

        setState(builder, data.getRequest());
        OAuthResponse response = builder.buildQueryMessage();
        return Response.status(response.getResponseStatus()).location(new URI(response.getLocationUri())).build();
      } catch(URISyntaxException | OAuthSystemException e) {
        throw Throwables.propagate(e);
      }
    });
  }

  @POST
  @Consumes("application/x-www-form-urlencoded")
  @Produces("application/json")
  @Path("/token")
  public Response access(@Context HttpServletRequest servletRequest) throws OAuthSystemException {
    try {
      OAuthTokenRequest oAuthRequest = new OAuthTokenRequest(servletRequest);
      GrantType type = GrantType.valueOf(oAuthRequest.getParam(OAuth.OAUTH_GRANT_TYPE).toUpperCase());

      switch(type) {
        case AUTHORIZATION_CODE:
          return accessAuthorizationCodeGrant(servletRequest, oAuthRequest);
        case PASSWORD:
          return accessPasswordGrant(servletRequest, oAuthRequest);
        default:
          OAuthResponse res = OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST).buildJSONMessage();
          return Response.status(res.getResponseStatus()).entity(res.getBody()).build();
      }
    } catch(OAuthProblemException e) {
      OAuthResponse res = OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST).error(e).buildJSONMessage();
      return Response.status(res.getResponseStatus()).entity(res.getBody()).build();
    } catch(Exception e) {
      OAuthResponse res = OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST) //
        .setError(e.getClass().getSimpleName()) //
        .setErrorDescription(e.getMessage()) //
        .buildJSONMessage();
      return Response.status(res.getResponseStatus()).entity(res.getBody()).build();
    }
  }

  //
  // Private methods
  //

  private Response tryBuildResponse(HttpServletRequest servletRequest, Function<OAuthRequestData, Response> responseBuilder)
      throws URISyntaxException, OAuthSystemException  {
    OAuthAuthzRequest oAuthRequest = null;
    String redirectURI = null;

    try {
      oAuthRequest = new OAuthAuthzRequest(servletRequest);
      String clientId = oAuthRequest.getParam(OAuth.OAUTH_CLIENT_ID);
      redirectURI = validateClientApplication(clientId, oAuthRequest.getParam(OAuth.OAUTH_REDIRECT_URI));
      validateScope(oAuthRequest.getScopes());
      return responseBuilder.apply(new OAuthRequestData(clientId, redirectURI, oAuthRequest));
    } catch(OAuthProblemException e) {
      return buildErrorResponse(e, oAuthRequest, redirectURI);
    } catch(RuntimeException e) {
      if(e.getCause() instanceof OAuthProblemException) {
        return buildErrorResponse((OAuthProblemException)e.getCause(), oAuthRequest, redirectURI);
      } else if(e.getCause() instanceof URISyntaxException) throw (URISyntaxException) e.getCause();

      throw e;
    } catch(Exception e) {
      return buildErrorResponse(OAuthProblemException.error("server_error", e.getMessage()), oAuthRequest, redirectURI);
    }
  }

  private Response buildErrorResponse(OAuthProblemException e, OAuthAuthzRequest oAuthRequest, String redirectURI)
      throws OAuthSystemException, URISyntaxException {
    boolean canRedirect = !Strings.isNullOrEmpty(redirectURI);
    OAuthASResponse.OAuthErrorResponseBuilder builder = OAuthASResponse
        .errorResponse(canRedirect ? HttpServletResponse.SC_FOUND : HttpServletResponse.SC_BAD_REQUEST).error(e);
    setState(builder, oAuthRequest);
    OAuthResponse response;

    if(canRedirect) {
      builder.location(redirectURI);
      response = builder.buildQueryMessage();
      return Response.status(response.getResponseStatus()).location(new URI(response.getLocationUri())).build();
    } else {
      response = builder.buildJSONMessage();
      return Response.status(response.getResponseStatus()).entity(response.getBody()).build();
    }
  }

  private void validateScope(Set<String> scopes) throws OAuthProblemException{
    for(String s: scopes) {
      if (!TokenUtils.OPENID_SCOPES.contains(s)) {
        String[] scopeParts = s.split(":");
        Application application = applicationService.find(scopeParts[0]);

        if(application == null || (scopeParts.length > 1 && !application.hasScope(scopeParts[1])))
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
    if(!authorization.getApplication().equals(clientId)) {
      throw OAuthProblemException
        .error("invalid_client_id", "The client ID does not match the one of the authorization");
    }
    if(!authorization.getRedirectURI().equals(redirectURI)) {
      throw OAuthProblemException
        .error("invalid_redirect_uri", "The redirect URI does not match the one of the authorization");
    }
    User user = userService.findActiveUser(authorization.getUsername());
    if(user == null) {
      throw OAuthProblemException.error("inactive_user", "The user of the authorization is not active");
    }

    Ticket ticket = ticketService.create(authorization);
    return getAccessResponse(ticket, authorization);
  }

  private Response accessPasswordGrant(HttpServletRequest servletRequest, OAuthTokenRequest oAuthRequest)
    throws OAuthSystemException, OAuthProblemException {
    validateClient(oAuthRequest);

    String clientId = oAuthRequest.getClientId();
    String username = oAuthRequest.getUsername();
    String password = oAuthRequest.getPassword();
    User user = userService.findActiveUser(username);
    if(user == null) user = userService.findActiveUserByEmail(username);

    authorizationValidator.validateUser(servletRequest, username, user);
    authorizationValidator.validateApplication(servletRequest, user, clientId);

    // check authentication
    Subject subject = SecurityUtils.getSubject();
    assert user != null;
    subject.login(new UsernamePasswordToken(user.getName(), password));
    authorizationValidator.validateRealm(servletRequest, user, subject);
    subject.logout();

    Ticket ticket = ticketService.create(user.getName(), false, false, clientId);
    return getAccessResponse(ticket, null);
  }

  private Response getAccessResponse(@NotNull Ticket ticket, @Nullable Authorization authorization) throws OAuthSystemException {
    String token = tokenUtils.makeAccessToken(ticket);
    long expiresIn = ticketService.getExpirationDate(ticket).getMillis() - DateTime.now().getMillis();

    OAuthASResponse.OAuthTokenResponseBuilder responseBuilder = OAuthASResponse.tokenResponse(HttpServletResponse.SC_OK) //
      .setAccessToken(token) //
      .setTokenType(OAuth.OAUTH_HEADER_NAME.toLowerCase()) // bug: OAUTH_BEARER_TOKEN has a wrong value
      .setExpiresIn(expiresIn / 1000 + "");

    if (authorization != null && authorization.hasScope(TokenUtils.OPENID_SCOPE)) {
      responseBuilder.setParam(TokenUtils.OPENID_TOKEN, tokenUtils.makeIDToken(authorization));
    }

    OAuthResponse response = responseBuilder.buildJSONMessage();
    return Response.status(response.getResponseStatus()).entity(response.getBody()).build();
  }

  /**
   * Set the state parameter if any was defined in the request into the response.
   *
   * @param builder
   * @param oAuthRequest
   */
  private void setState(OAuthResponse.OAuthResponseBuilder builder, OAuthAuthzRequest oAuthRequest) {
    if(oAuthRequest == null) return;
    String state = oAuthRequest.getState();
    if(!Strings.isNullOrEmpty(state)) {
      builder.setParam(OAuth.OAUTH_STATE, state);
    }
  }

  /**
   * Check that the application exists and is available for OAuth process (default redirect URI); check also that the
   * provided redirect URI includes the default URI same host: (and port, if any), same path or is sub-path.
   *
   * @param clientId Client ID is the {@link Application} name
   * @param redirectURI Optional: if null or empty default Application's redirect URI is used else it must be a valid redirect URI.
   * @return the redirectURI
   */
  private String validateClientApplication(String clientId, String redirectURI) throws OAuthProblemException {
    Application application = applicationService.getApplication(clientId);
    if(!application.hasRedirectURI()) {
      throw OAuthProblemException
          .error("missing_application_redirect_uri", "Application does not have a default redirect URI");
    }
    String defaultURI = application.getRedirectURI();
    // TODO? check user has access to this application
    // Verify the validity of the given URI
    String normalizedURI = redirectURI;
    if(Strings.isNullOrEmpty(redirectURI)) {
      normalizedURI = defaultURI;
    }
    if(!normalizedURI.startsWith(defaultURI)) {
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
}
