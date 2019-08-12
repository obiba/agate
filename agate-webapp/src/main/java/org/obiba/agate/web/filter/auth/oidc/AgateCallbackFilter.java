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
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import com.google.common.eventbus.Subscribe;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.obiba.agate.domain.Application;
import org.obiba.agate.domain.Configuration;
import org.obiba.agate.domain.RealmConfig;
import org.obiba.agate.domain.Ticket;
import org.obiba.agate.domain.User;
import org.obiba.agate.event.AgateConfigUpdatedEvent;
import org.obiba.agate.service.ApplicationService;
import org.obiba.agate.service.ConfigurationService;
import org.obiba.agate.service.RealmConfigService;
import org.obiba.agate.service.TicketService;
import org.obiba.agate.service.TokenUtils;
import org.obiba.agate.service.UserService;
import org.obiba.agate.web.rest.ticket.TicketsResource;
import org.obiba.oidc.OIDCConfigurationProvider;
import org.obiba.oidc.OIDCCredentials;
import org.obiba.oidc.OIDCException;
import org.obiba.oidc.OIDCSession;
import org.obiba.oidc.OIDCSessionManager;
import org.obiba.oidc.shiro.authc.OIDCAuthenticationToken;
import org.obiba.oidc.web.filter.OIDCCallbackFilter;
import org.obiba.shiro.web.filter.AuthenticationExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.filter.DelegatingFilterProxy;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.NewCookie;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * This filter is used upon a successful OIDC authentication. Clients are signed-in to Agate via a <code>obibasid</code> or
 * a <code>obibaid</code> cookie.
 */
@Component("agateCallbackFilter")
public class AgateCallbackFilter extends OIDCCallbackFilter {

  private static final Logger log = LoggerFactory.getLogger(AgateCallbackFilter.class);

  private final ConfigurationService configurationService;

  private final OIDCConfigurationProvider oidcConfigurationProvider;

  private final OIDCSessionManager oidcSessionManager;

  private final AuthenticationExecutor authenticationExecutor;

  private final ApplicationService applicationService;

  private final RealmConfigService realmConfigService;

  private final UserService userService;

  private final TicketService ticketService;

  private final TokenUtils tokenUtils;

  private String publicUrl;

  @Inject
  public AgateCallbackFilter(
    OIDCConfigurationProvider oidcConfigurationProvider,
    OIDCSessionManager oidcSessionManager,
    AuthenticationExecutor authenticationExecutor,
    ConfigurationService configurationService,
    ApplicationService applicationService,
    RealmConfigService realmConfigService, UserService userService,
    TicketService ticketService,
    TokenUtils tokenUtils) {
    this.oidcConfigurationProvider = oidcConfigurationProvider;
    this.oidcSessionManager = oidcSessionManager;
    this.authenticationExecutor = authenticationExecutor;
    this.configurationService = configurationService;
    this.applicationService = applicationService;
    this.realmConfigService = realmConfigService;
    this.userService = userService;
    this.ticketService = ticketService;
    this.tokenUtils = tokenUtils;
  }

  @PostConstruct
  public void init() {
    publicUrl = configurationService.getPublicUrl();
    setOIDCConfigurationProvider(oidcConfigurationProvider);
    setOIDCSessionManager(oidcSessionManager);
    setDefaultRedirectURL(publicUrl);
    String callbackUrl = publicUrl + (publicUrl.endsWith("/") ? "" : "/") + "auth/callback/";
    setCallbackURL(callbackUrl);
  }

  @Subscribe
  public void agateConfigUpdated(AgateConfigUpdatedEvent event) {
    publicUrl = configurationService.getPublicUrl();
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
        sendRedirectOrSendError(publicUrl + (publicUrl.endsWith("/") ? "" : "/") + "#/error", error, response);
      }
    } catch (IOException ignore) {
      // ignore
    }
  }

  @Override
  protected void onRedirect(OIDCSession session, HttpServletResponse response) throws IOException {
    Map<String, String[]> requestParameters = session.getRequestParameters();
    String action = retrieveRequestParameter(FilterParameter.ACTION.value(), requestParameters);

    String redirect = retrieveRequestParameter(FilterParameter.REDIRECT.value(), requestParameters);

    if (Strings.isNullOrEmpty(redirect)) {
      if (FilterAction.SIGNIN.equals(FilterAction.valueOf(action))) {
        response.sendRedirect(retrieveRedirectUrl(requestParameters));
      } else {
        response.sendRedirect(retrieveSignupRedirectUrl(requestParameters));
      }
    } else {
      String redirectHash = retrieveRequestParameter(FilterParameter.REDIRECT_HASH.value(), requestParameters);

      String redirectUrlWithHash = !Strings.isNullOrEmpty(redirectHash) ? (redirect + (redirect.endsWith("/") ? "" : "/") + "#" + (redirectHash.startsWith("/") ? "" : "/") + redirectHash) : redirect;
      response.sendRedirect(redirectUrlWithHash);
    }
  }

  /**
   * Depending on the specified action as part of the request parameters, sign in/up client.
   *
   * @param session
   * @param credentials
   * @param response
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
          application = findApplication(redirect);
          if (application.isPresent()) {
            signInWithTicket(credentials, response, provider, application.get(), errorUrl, signInErrorUrl);
          } else {
            signIn(credentials, response, provider, errorUrl, signInErrorUrl);
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
   * Sign in a client with a redirect URI matching that of a registered application.
   *
   * @see AgateSignInFilter to see how the redirect URI is added to the initial OIDC authentication request.
   *@param credentials
   * @param response
   * @param provider
   * @param application
   * @param errorUrl
   */
  private void signInWithTicket(OIDCCredentials credentials, HttpServletResponse response, String provider, Application application, String errorUrl, String signInErrorUrl)
    throws IOException {
    OIDCAuthenticationToken oidcAuthenticationToken = new OIDCAuthenticationToken(credentials);
    User user = userService.findUser(oidcAuthenticationToken.getUsername());

    if (user != null) {
      if (!user.getRealm().equals(provider)) {
        sendRedirectOrSendError(errorUrl, "User already registered with another realm.", response);
      } else {
        Subject subject = authenticationExecutor.login(oidcAuthenticationToken);
        if (subject != null) {
          Session subjectSession = prepareSubjectSession(subject);
          int timeout = (int) (subjectSession.getTimeout() / 1000);

          Configuration configuration = configurationService.getConfiguration();
          Ticket ticket = ticketService.create(subject.getPrincipal().toString(), false, false, application.getId());
          String token = tokenUtils.makeAccessToken(ticket);

          response.addHeader(HttpHeaders.SET_COOKIE,
            new NewCookie(TicketsResource.TICKET_COOKIE_NAME, token, "/", configuration.getDomain(),
              "Obiba session deleted", timeout, configuration.hasDomain()).toString());
          log.debug("Successfully authenticated subject {}", SecurityUtils.getSubject().getPrincipal());
        }
      }
    } else {
      log.info("Agate Authentication failure for '{}', user does not exist in Agate", oidcAuthenticationToken.getUsername());
      try {
        setUserAuthCookieForSignUp(credentials, oidcAuthenticationToken, response, provider, errorUrl);
      } catch (JSONException e) {
        // ignore
      }
      response.sendRedirect(signInErrorUrl);
    }
  }

  /**
   * Sign in a client to Agate
   *  @param credentials
   * @param response
   * @param provider
   */
  private void signIn(OIDCCredentials credentials, HttpServletResponse response, String provider, String errorUrl, String signInErrorUrl)
    throws IOException {
    OIDCAuthenticationToken oidcAuthenticationToken = new OIDCAuthenticationToken(credentials);
    User user = userService.findUser(oidcAuthenticationToken.getUsername());

    if (user != null) {
      if (!user.getRealm().equals(provider)) {
        sendRedirectOrSendError(errorUrl, "User already registered with another realm.", response);
      } else {
        Subject subject = authenticationExecutor.login(oidcAuthenticationToken);
        if (subject != null) {
          Session subjectSession = prepareSubjectSession(subject);
          int timeout = (int) (subjectSession.getTimeout() / 1000);

          response.addHeader(HttpHeaders.SET_COOKIE,
            new NewCookie("agatesid", subjectSession.getId().toString(), "/", null, null, timeout, false).toString());
          log.debug("Successfully authenticated subject {}", SecurityUtils.getSubject().getPrincipal());
        }
      }
    } else {
      log.info("Agate Authentication failure for '{}', user does not exist in Agate", oidcAuthenticationToken.getUsername());
      try {
        setUserAuthCookieForSignUp(credentials, oidcAuthenticationToken, response, provider, errorUrl);
      } catch (JSONException e) {
        // ignore
      }
      response.sendRedirect(signInErrorUrl);
    }
  }

  private void signUp(OIDCCredentials credentials, HttpServletResponse response, String provider, String errorUrl) throws IOException, JSONException {
    // User profile response should be either in a cookie or the response body
    OIDCAuthenticationToken oidcAuthenticationToken = new OIDCAuthenticationToken(credentials);
    User user = userService.findUser(oidcAuthenticationToken.getUsername());

    if (user == null) {
      setUserAuthCookieForSignUp(credentials, oidcAuthenticationToken, response, provider, errorUrl);
    } else {
      log.info("SignUp failure for '{}' with provider '{}', user already exists in Agate", oidcAuthenticationToken.getUsername(), provider);
      sendRedirectOrSendError(errorUrl, "Can't sign up with these credentials.", response);
    }
  }

  private void setUserAuthCookieForSignUp(OIDCCredentials credentials, OIDCAuthenticationToken oidcAuthenticationToken, HttpServletResponse response, String provider, String errorUrl) throws IOException, JSONException {
    RealmConfig config = realmConfigService.findConfig(provider);

    if (config != null && config.isForSignup()) {
      JSONArray names = configurationService.getJoinConfiguration("en", null).getJSONObject("schema").getJSONObject("properties").names();
      Map<String, String> userInfoMapping = config.getUserInfoMapping();

      JSONObject userMappedInfo = new JSONObject();

      // map other fields, use config and join's schema?
      for (int i = 0; i < names.length(); i++) {
        String name = names.getString(i);
        userMappedInfo.put(name, credentials.getUserInfo(userInfoMapping.get(name)));
      }

      if (!userMappedInfo.has("username")) userMappedInfo.put("username", oidcAuthenticationToken.getUsername());
      userMappedInfo.put("realm", config.getName());
      Configuration configuration =  configurationService.getConfiguration();
      response.addHeader(HttpHeaders.SET_COOKIE,
        new NewCookie(
          "u_auth",
          userMappedInfo.toString(),
          "/",
          configuration.getDomain(),
          null,
          600,
          configuration.hasDomain()
        ).toString());
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

  private Optional<Application> findApplication(String redirectUri) {
    return applicationService.findAll()
      .stream()
      .filter(application -> application.hasRedirectURI() && matchRedirectUrl(application.getRedirectURI(), redirectUri))
      .findFirst();
  }

  private boolean matchRedirectUrl(String source, String target) {
    String patternString =
      source
        .replaceAll("\\.", "\\\\.")
        .replaceAll("[\\/\\*]*$", ".*")
        .replaceAll("/", "\\\\/");

    Pattern compile = Pattern.compile(patternString);
    return compile.matcher(target).matches();
  }

  private String retrieveRequestParameter(String key, Map<String, String[]> requestParameters) {
    return Optional.ofNullable(requestParameters.get(key))
      .filter(p -> p != null && p.length > 0)
      .map(p -> p[0])
      .orElse("");
  }

  private String retrieveRedirectUrl(Map<String, String[]> requestParameters) {
    String redirectUrl = retrieveRequestParameter(FilterParameter.REDIRECT.value(), requestParameters);
    return Strings.isNullOrEmpty(redirectUrl) ? getDefaultRedirectURL() : redirectUrl;
  }

  private String retrieveSignupRedirectUrl(Map<String, String[]> requestParameters) {
    String signupRedirectUrl = retrieveRequestParameter(FilterParameter.REDIRECT.value(), requestParameters);
    return Strings.isNullOrEmpty(signupRedirectUrl) ? (publicUrl + (publicUrl.endsWith("/") ? "" : "/") + "#/join") : signupRedirectUrl;
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
      errorUrl = publicUrl + (publicUrl.endsWith("/") ? "" : "/") + "#/error";
    }
    return errorUrl;
  }

  private String makeSignInErrorUrl(OIDCSession session) {
    String url = publicUrl + (publicUrl.endsWith("/") ? "#/join" : "/#/join");

    Map<String, String[]> requestParameters = session.getRequestParameters();
    String redirect = retrieveRedirectUrl(requestParameters);

    if (!Strings.isNullOrEmpty(redirect) && redirect.contains("redirect_uri")) {
      url = url + "?redirect=" + redirect;
    }

    return url;
  }

}
