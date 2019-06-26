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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
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
import org.obiba.agate.service.ApplicationService;
import org.obiba.agate.service.ConfigurationService;
import org.obiba.agate.service.RealmConfigService;
import org.obiba.agate.service.TicketService;
import org.obiba.agate.service.TokenUtils;
import org.obiba.agate.service.UserService;
import org.obiba.agate.web.rest.ticket.TicketsResource;
import org.obiba.oidc.OIDCConfigurationProvider;
import org.obiba.oidc.OIDCCredentials;
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

  private final ObjectMapper objectMapper;

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

    objectMapper = new ObjectMapper();

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

    Optional<Application> application = Optional.empty();

    if (!Strings.isNullOrEmpty(redirect)) setDefaultRedirectURL(redirect);

    switch (FilterAction.valueOf(action[0])) {
      case SIGNIN:
        application = findApplication(redirect);
        if (application.isPresent()) {
          signInWithTicket(credentials, response, application.get());
        } else {
          signIn(credentials, response);
        }
        break;
      case SIGNUP:
        try {
          signUp(provider, credentials, response);
        } catch (JSONException | IOException e) {
          // error
        }
      default:
    }
  }

  /**
   * Sign in a client with a redirect URI matching that of a registered application.
   *
   * @see AgateSignInFilter to see how the redirect URI is added to the initial OIDC authentication request.
   *
   * @param credentials
   * @param response
   * @param application
   */
  private void signInWithTicket(OIDCCredentials credentials, HttpServletResponse response, Application application) {
    OIDCAuthenticationToken oidcAuthenticationToken = new OIDCAuthenticationToken(credentials);
    User user = userService.findUser(oidcAuthenticationToken.getUsername());

    if (user != null) {
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
  }

  /**
   * Sign in a client to Agate
   *
   * @param credentials
   * @param response
   */
  private void signIn(OIDCCredentials credentials, HttpServletResponse response) {
    OIDCAuthenticationToken oidcAuthenticationToken = new OIDCAuthenticationToken(credentials);
    User user = userService.findUser(oidcAuthenticationToken.getUsername());

    if (user != null) {
      Subject subject = authenticationExecutor.login(oidcAuthenticationToken);
      if (subject != null) {
        Session subjectSession = prepareSubjectSession(subject);
        int timeout = (int) (subjectSession.getTimeout() / 1000);

        response.addHeader(HttpHeaders.SET_COOKIE,
          new NewCookie("agatesid", subjectSession.getId().toString(), "/", null, null, timeout, false).toString());
        log.debug("Successfully authenticated subject {}", SecurityUtils.getSubject().getPrincipal());
      }
    } else {
      // throw error?
    }
  }

  private void signUp(String provider, OIDCCredentials credentials, HttpServletResponse response) throws IOException, JSONException {
    // User profile response should be either in a cookie or the response body
    OIDCAuthenticationToken oidcAuthenticationToken = new OIDCAuthenticationToken(credentials);
    User user = userService.findUser(oidcAuthenticationToken.getUsername());

    if (user == null) {
      RealmConfig config = realmConfigService.findConfig(provider);

      if (config != null) {
        JSONArray names = configurationService.getJoinConfiguration("en").getJSONObject("schema").getJSONObject("properties").names();

        JSONObject userVals = new JSONObject();
        config.getUserInfoMapping().forEach((key, value) -> {
          try {
            userVals.put(key, credentials.getUserInfo(value));
          } catch (JSONException e) {
            //
          }
        });

        userVals.put("username", credentials.getUserInfo("preferred_username"));
        userVals.put("realm", config.getName());

        // map other fields, use config and join's schema?

        response.addHeader(HttpHeaders.SET_COOKIE,
          new NewCookie("u_auth", userVals.toString(), "/", null, null, 20000, false).toString());
      }
    } else {
      // user already exists error
    }

    response.sendRedirect(publicUrl + (publicUrl.endsWith("/") ? "" : "/") + "#/join");
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
    return retrieveRequestParameter(FilterParameter.REDIRECT.value(), requestParameters);
  }

  public static class Wrapper extends DelegatingFilterProxy {
    public Wrapper() {
      super("agateCallbackFilter");
    }
  }

}
