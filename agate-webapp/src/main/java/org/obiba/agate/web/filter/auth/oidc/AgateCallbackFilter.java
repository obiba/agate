/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.agate.web.filter.auth.oidc;

import com.google.common.base.Strings;
import com.google.common.eventbus.Subscribe;
import jakarta.ws.rs.ext.RuntimeDelegate;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.obiba.agate.domain.*;
import org.obiba.agate.event.AgateConfigUpdatedEvent;
import org.obiba.agate.security.OidcAuthConfigurationProvider;
import org.obiba.agate.service.*;
import org.obiba.agate.web.rest.ticket.TicketsResource;
import org.obiba.agate.web.support.URLUtils;
import org.obiba.oidc.*;
import org.obiba.oidc.shiro.authc.OIDCAuthenticationToken;
import org.obiba.oidc.shiro.realm.DefaultOIDCGroupsExtractor;
import org.obiba.oidc.web.J2EContext;
import org.obiba.oidc.web.filter.OIDCCallbackFilter;
import org.obiba.shiro.web.filter.AuthenticationExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.web.filter.DelegatingFilterProxy;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.NewCookie;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * This filter is used upon a successful OIDC authentication. Clients are signed-in to Agate via a <code>obibasid</code> or
 * a <code>obibaid</code> cookie.
 */
public class AgateCallbackFilter extends OIDCCallbackFilter {

  private static final Logger log = LoggerFactory.getLogger(AgateCallbackFilter.class);

  private final ConfigurationService configurationService;

  private final OIDCConfigurationProvider oidcConfigurationProvider;

  private final OidcAuthConfigurationProvider oidcAuthConfigurationProvider;

  private final OIDCSessionManager oidcSessionManager;

  private final AuthenticationExecutor authenticationExecutor;

  private final ApplicationService applicationService;

  private final RealmConfigService realmConfigService;

  private final UserService userService;

  private final TicketService ticketService;

  private final TokenUtils tokenUtils;

  private String publicUrl;

  public AgateCallbackFilter(OIDCConfigurationProvider oidcConfigurationProvider, OidcAuthConfigurationProvider oidcAuthConfigurationProvider, OIDCSessionManager oidcSessionManager, AuthenticationExecutor authenticationExecutor, ConfigurationService configurationService, ApplicationService applicationService, RealmConfigService realmConfigService, UserService userService, TicketService ticketService, TokenUtils tokenUtils) {
    this.oidcConfigurationProvider = oidcConfigurationProvider;
    this.oidcAuthConfigurationProvider = oidcAuthConfigurationProvider;
    this.oidcSessionManager = oidcSessionManager;
    this.authenticationExecutor = authenticationExecutor;
    this.configurationService = configurationService;
    this.applicationService = applicationService;
    this.realmConfigService = realmConfigService;
    this.userService = userService;
    this.ticketService = ticketService;
    this.tokenUtils = tokenUtils;
    init();
  }

  public void init() {
    setOIDCConfigurationProvider(oidcConfigurationProvider);
    setOIDCSessionManager(oidcSessionManager);
    initFilterUrls();
  }

  protected void initFilterUrls() {
    publicUrl = configurationService.getPublicUrl();
    String callbackUrl = publicUrl + (publicUrl.endsWith("/") ? "" : "/") + "auth/callback/";
    setCallbackURL(callbackUrl);
    setDefaultRedirectURL(publicUrl);
  }

  @Subscribe
  public void agateConfigUpdated(AgateConfigUpdatedEvent event) {
    initFilterUrls();
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
    try {
      super.doFilterInternal(request, response, filterChain);
    } catch (OIDCException e) {
      sendRedirectOrSendError(makeErrorUrl(null), e.getMessage(), response);
    }
  }

  @Override
  protected void onAuthenticationError(OIDCSession session, String error, HttpServletResponse response) {
    try {
      if (session != null) {
        String errorUrl = makeErrorUrl(session);
        session.setCallbackError(error);
        if (!Strings.isNullOrEmpty(errorUrl)) response.sendRedirect(errorUrl);
      } else {
        sendRedirectOrSendError(publicUrl + (publicUrl.endsWith("/") ? "" : "/") + "error", error, response);
      }
    } catch (IOException ignore) {
      // ignore
    }
  }

  @Override
  protected void onRedirect(OIDCSession session, J2EContext context, String provider) throws IOException {
    if (session == null) return;
    if (context.getResponse().isCommitted()) return;

    Map<String, String[]> requestParameters = session.getRequestParameters();
    String action = retrieveRequestParameter(FilterParameter.ACTION.value(), requestParameters);
    String redirect = retrieveRequestParameter(FilterParameter.REDIRECT.value(), requestParameters);
    log.debug("onRedirect: Action: {}", action);
    log.debug("onRedirect: Redirect URL (params): {}", redirect);

    if (Strings.isNullOrEmpty(redirect)) {
      if (FilterAction.SIGNIN.equals(FilterAction.valueOf(action))) {
        redirect = retrieveRedirectUrl(requestParameters);
      } else {
        redirect = retrieveSignupRedirectUrl(requestParameters);
      }
    }
    log.debug("onRedirect: Redirect URL (location): {}", redirect);
    context.getResponse().addHeader(HttpHeaders.LOCATION, redirect);
    context.getResponse().sendRedirect(redirect);
  }

  /**
   * Depending on the specified action as part of the request parameters, sign in/up client.
   */
  @Override
  protected void onAuthenticationSuccess(OIDCSession session, OIDCCredentials credentials, HttpServletResponse response) {
    Map<String, String[]> requestParameters = session.getRequestParameters();
    String[] action = requestParameters.get(FilterParameter.ACTION.value());
    String redirect = retrieveRedirectUrl(requestParameters);
    String provider = retrieveRequestParameter(FilterParameter.OIDC_PROVIDER_ID.value(), requestParameters);
    String errorUrl = makeErrorUrl(session);
    String signInErrorUrl = makeSignInErrorUrl(session);

    Optional<Application> application = Optional.empty();

    try {
      switch (FilterAction.valueOf(action[0])) {
        case SIGNIN:
          if (Strings.isNullOrEmpty(redirect)) {
            signIn(credentials, response, provider, errorUrl, signInErrorUrl);
          } else {
            application = findApplication(redirect);
            if (application.isPresent()) {
              signInWithTicket(credentials, response, provider, application.get(), errorUrl, signInErrorUrl);
            } else {
              String redirectUri = extractRedirectUri(redirect);
              log.warn("Could not find an Application matching the redirect URI: {}", redirectUri);
              signIn(credentials, response, provider, errorUrl, signInErrorUrl);
            }
          }
          break;
        case SIGNUP:
          signUp(credentials, response, provider, errorUrl);
        default:
      }
    } catch (JSONException | IOException e) {
      // ignore
    }
  }

  /**
   * Returns the usernameClaim for OIDC Realm
   *
   * @param config
   * @return
   */
  private String getUsernameClaim(RealmConfig config) {
    if (AgateRealm.AGATE_OIDC_REALM.equals(config.getType()) && config.getUserInfoMapping() != null) {
      return config.getUserInfoMapping().get("username");
    }

    return null;
  }

  /**
   * Sign in a client with a redirect URI matching that of a registered application.
   *
   * @param credentials
   * @param response
   * @param provider
   * @param application
   * @param errorUrl
   * @see AgateSignInFilter to see how the redirect URI is added to the initial OIDC authentication request.
   */
  private void signInWithTicket(OIDCCredentials credentials, HttpServletResponse response, String provider, Application application, String errorUrl, String signInErrorUrl) throws IOException {
    RealmConfig realmConfig = realmConfigService.findConfig(provider);
    OIDCAuthenticationToken oidcAuthenticationToken = new OIDCAuthenticationToken(credentials);
    User user = userService.findUser(credentials.getUsername(getUsernameClaim(realmConfig)));

    if (user != null) {
      if (!user.getRealm().equals(provider)) {
        sendRedirectOrSendError(errorUrl, "User already registered with another realm.", response);
      } else {
        Subject subject = authenticationExecutor.login(oidcAuthenticationToken);
        if (subject != null) {
          Session subjectSession = prepareSubjectSession(subject);
          int timeout = (int) (subjectSession.getTimeout() / 1000);

          updateUserGroups(user, subject);

          Configuration configuration = configurationService.getConfiguration();
          Ticket ticket = ticketService.create(subject.getPrincipal().toString(), false, false, application.getId());
          String token = tokenUtils.makeAccessToken(ticket);

          // get domain from realm config
          String domain = realmConfig.hasDomain() ? realmConfig.getDomain() : configuration.getDomain();

          response.addHeader(HttpHeaders.SET_COOKIE, toCookieString(new NewCookie.Builder(TicketsResource.TICKET_COOKIE_NAME)
              .value(token)
              .path("/")
              .domain(domain)
              .comment("Obiba session deleted")
              .maxAge(timeout)
              .secure(true)
              .httpOnly(true)
              .build()));
          log.debug("Successfully authenticated subject {}", SecurityUtils.getSubject().getPrincipal());
        }
      }
    } else {
      log.info("Agate Authentication failure for '{}', user does not exist in Agate", credentials.getUsername(getUsernameClaim(realmConfig)));
      try {
        setUserAuthCookieForSignUp(credentials, response, provider, errorUrl);
      } catch (JSONException e) {
        // ignore
      }
      response.sendRedirect(signInErrorUrl);
    }
  }

  private void updateUserGroups(User user, Subject subject) {
    // do it only when the realm is OIDC
    OIDCConfiguration oidcConfig = oidcAuthConfigurationProvider.getConfiguration(user.getRealm());
    if (oidcConfig != null) {
      // groups to be retrieved from user info claims
      for (Object principal : subject.getPrincipals()) {
        if (principal instanceof Map) {
          Set<String> groups = user.getGroups();
          int count = groups.size();
          new DefaultOIDCGroupsExtractor().extractGroups(oidcConfig, (Map<String, Object>) principal).stream().map(String::trim).filter(g -> !g.isEmpty()).forEach(groups::add);
          if (count < groups.size()) {
            user.setGroups(groups);
            userService.save(user);
          }
        }
      }
    }
  }

  /**
   * Sign in a client to Agate
   *
   * @param credentials
   * @param response
   * @param provider
   */
  private void signIn(OIDCCredentials credentials, HttpServletResponse response, String provider, String errorUrl, String signInErrorUrl) throws IOException {
    RealmConfig config = realmConfigService.findConfig(provider);
    OIDCAuthenticationToken oidcAuthenticationToken = new OIDCAuthenticationToken(credentials);
    User user = userService.findUser(credentials.getUsername(getUsernameClaim(config)));

    if (user != null) {
      if (!user.getRealm().equals(provider)) {
        sendRedirectOrSendError(errorUrl, "User already registered with another realm.", response);
      } else {
        Subject subject = authenticationExecutor.login(oidcAuthenticationToken);
        if (subject != null) {
          Session subjectSession = prepareSubjectSession(subject);
          int timeout = (int) (subjectSession.getTimeout() / 1000);

          updateUserGroups(user, subject);

          response.addHeader(HttpHeaders.SET_COOKIE, toCookieString(
              new NewCookie.Builder("agatesid")
                  .value(subjectSession.getId().toString())
                  .path(configurationService.getContextPath() + "/")
                  .maxAge(timeout)
                  .secure(true)
                  .httpOnly(true)
                  .build()));
          log.debug("Successfully authenticated subject {}", SecurityUtils.getSubject().getPrincipal());
        }
      }
    } else {
      log.info("Agate Authentication failure for '{}', user does not exist in Agate", credentials.getUsername(getUsernameClaim(config)));
      try {
        setUserAuthCookieForSignUp(credentials, response, provider, errorUrl);
      } catch (JSONException e) {
        // ignore
      }
      response.sendRedirect(signInErrorUrl);
    }
  }

  private void signUp(OIDCCredentials credentials, HttpServletResponse response, String provider, String errorUrl) throws IOException, JSONException {
    // User profile response should be either in a cookie or the response body
    RealmConfig config = realmConfigService.findConfig(provider);
    OIDCAuthenticationToken oidcAuthenticationToken = new OIDCAuthenticationToken(credentials);
    User user = userService.findUser(credentials.getUsername(getUsernameClaim(config)));

    if (user == null) {
      setUserAuthCookieForSignUp(credentials, response, provider, errorUrl);
    } else {
      log.info("SignUp failure for '{}' with provider '{}', user already exists in Agate", credentials.getUsername(getUsernameClaim(config)), provider);
      sendRedirectOrSendError(errorUrl, "Can't sign up with these credentials.", response);
    }
  }

  private void setUserAuthCookieForSignUp(OIDCCredentials credentials, HttpServletResponse response, String provider, String errorUrl) throws IOException, JSONException {
    RealmConfig realmConfig = realmConfigService.findConfig(provider);

    if (realmConfig != null && realmConfig.isForSignup()) {
      JSONArray names = configurationService.getJoinConfiguration("en", null).getJSONObject("schema").getJSONObject("properties").names();
      Map<String, String> userInfoMapping = realmConfig.getUserInfoMapping();

      JSONObject userMappedInfo = new JSONObject();

      log.debug("User info received: {}", new JSONObject(credentials.getUserInfo()));
      log.debug("User info fields mapping: {}", new JSONObject(userInfoMapping));

      // map other fields, use config and join's schema?
      for (int i = 0; i < names.length(); i++) {
        String name = names.getString(i);
        if (userInfoMapping.containsKey(name))
          userMappedInfo.put(name, credentials.getUserInfo(userInfoMapping.get(name)));
      }

      if (!userMappedInfo.has("username"))
        userMappedInfo.put("username", credentials.getUsername(getUsernameClaim(realmConfig)));
      userMappedInfo.put("realm", realmConfig.getName());

      log.debug("User info mapped: {}", userMappedInfo);


      Configuration configuration = configurationService.getConfiguration();
      // get domain from realm config
      String domain = realmConfig.hasDomain() ? realmConfig.getDomain() : configuration.getDomain();
      response.addHeader(HttpHeaders.SET_COOKIE, toCookieString(
          new NewCookie.Builder("u_auth")
              .value(URLUtils.encode(userMappedInfo.toString()).replaceAll("\\+", "%20"))
              .path("/")
              .domain(domain)
              .maxAge(600)
              .secure(true)
              .httpOnly(true)
              .build()));
    } else {
      sendRedirectOrSendError(errorUrl, "Realm '" + provider + "' not set up for sign up.", response);
    }
  }

  private Session prepareSubjectSession(Subject subject) {
    Assert.notNull(subject, "Subject cannot be null.");
    Session subjectSession = subject.getSession();
    log.trace("Binding subject {} session {} to executing thread {}", subject.getPrincipal(), subjectSession.getId(), Thread.currentThread().getId());
    ThreadContext.bind(subject);
    subjectSession.touch();

    return subjectSession;
  }

  private String extractRedirectUri(String redirect) {
    try {
      URL redirectUrl = new URL(redirect);
      String query = redirectUrl.getQuery();
      Map<String, String> queryMap = URLUtils.queryStringToMap(query);
      return queryMap.containsKey("redirect_uri") ? queryMap.get("redirect_uri") : redirect;
    } catch (MalformedURLException e) {
      // ignore
    }
    return null;
  }

  private Optional<Application> findApplication(String redirect) {
    String redirectUri = extractRedirectUri(redirect);
    if (!Strings.isNullOrEmpty(redirectUri)) {
      return applicationService.findAll().stream().filter(application -> application.hasRedirectURI() && matchRedirectUrl(application.getRedirectURIs(), redirectUri)).findFirst();
    }
    return Optional.empty();
  }

  private boolean matchRedirectUrl(List<String> sources, String target) {
    return sources.stream().anyMatch(source -> matchRedirectUrl(source, target));
  }

  private boolean matchRedirectUrl(String source, String target) {
    String patternString = source.replaceAll("\\.", "\\\\.").replaceAll("[\\/\\*]*$", ".*").replaceAll("/", "\\\\/");

    Pattern compile = Pattern.compile(patternString);
    return compile.matcher(target).matches();
  }

  private String retrieveRequestParameter(String key, Map<String, String[]> requestParameters) {
    return Optional.ofNullable(requestParameters.get(key)).filter(p -> p != null && p.length > 0).map(p -> p[0]).orElse("");
  }

  private String retrieveRedirectUrl(Map<String, String[]> requestParameters) {
    String redirectUrl = retrieveRequestParameter(FilterParameter.REDIRECT.value(), requestParameters);
    return Strings.isNullOrEmpty(redirectUrl) ? getDefaultRedirectURL() : redirectUrl;
  }

  private String retrieveSignupRedirectUrl(Map<String, String[]> requestParameters) {
    String signupRedirectUrl = retrieveRequestParameter(FilterParameter.REDIRECT.value(), requestParameters);
    return Strings.isNullOrEmpty(signupRedirectUrl) ? (publicUrl + (publicUrl.endsWith("/") ? "" : "/") + "signup") : signupRedirectUrl;
  }

  public static class Wrapper extends DelegatingFilterProxy {
    public Wrapper() {
      super("agateCallbackFilter");
    }
  }

  private void sendRedirectOrSendError(String redirect, String message, HttpServletResponse response) throws IOException {
    if (Strings.isNullOrEmpty(redirect)) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, message);
    } else {
      response.sendRedirect(redirect + (redirect.contains("?") ? "&" : "?") + "error=" + HttpServletResponse.SC_BAD_REQUEST + "&message=" + message);
    }
  }

  /**
   * If not specified in the request parameter, the error redirect goes to agate's error page.
   *
   * @param session
   * @return
   */
  private String makeErrorUrl(OIDCSession session) {
    String errorUrl = session != null ? Strings.emptyToNull(retrieveRequestParameter(FilterParameter.ERROR.value(), session.getRequestParameters())) : null;
    if (Strings.isNullOrEmpty(errorUrl)) {
      errorUrl = publicUrl + (publicUrl.endsWith("/") ? "" : "/") + "error";
    }
    return errorUrl;
  }

  private String makeSignInErrorUrl(OIDCSession session) {
    Map<String, String[]> requestParameters = session.getRequestParameters();

    String errorUrl = retrieveRequestParameter(FilterParameter.SIGNIN_ERROR_URI.value(), requestParameters);
    String url = Strings.isNullOrEmpty(errorUrl) ? (publicUrl + (publicUrl.endsWith("/") ? "signup-with" : "/signup-with")) : errorUrl;

    String redirect = retrieveRedirectUrl(requestParameters);

    if (!Strings.isNullOrEmpty(redirect) && redirect.contains("redirect_uri")) {
      url = url + "?redirect=" + URLUtils.encode(redirect);
    }

    return url;
  }

  private String toCookieString(NewCookie cookie) {
    return RuntimeDelegate.getInstance().createHeaderDelegate(NewCookie.class).toString(cookie);
  }

  protected J2EContext makeJ2EContext(HttpServletRequest request, HttpServletResponse response) {
    String sid = request.getRequestedSessionId();
    log.debug("callback filter requested session id: {}", sid);
    return super.makeJ2EContext(request, response);
  }
}
