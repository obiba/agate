  /*
   * Copyright (c) 2019 OBiBa. All rights reserved.
   *
   * This program and the accompanying materials
   * are made available under the terms of the GNU Public License v3.0.
   *
   * You should have received a copy of the GNU General Public License
   * along with this program.  If not, see <http://www.gnu.org/licenses/>.
   */

  package org.obiba.agate.service;

  import com.fasterxml.jackson.databind.ObjectMapper;
  import com.google.common.base.Strings;
  import com.google.common.collect.Maps;
  import com.google.common.collect.Sets;
  import com.google.common.eventbus.EventBus;
  import com.google.common.eventbus.Subscribe;
  import freemarker.template.Configuration;
  import freemarker.template.Template;
  import jakarta.annotation.Nonnull;
  import jakarta.ws.rs.BadRequestException;
  import org.apache.commons.lang.LocaleUtils;
  import org.apache.shiro.SecurityUtils;
  import org.apache.shiro.authc.AuthenticationInfo;
  import org.apache.shiro.authc.UsernamePasswordToken;
  import org.apache.shiro.authz.UnauthenticatedException;
  import org.apache.shiro.crypto.hash.Sha512Hash;
  import org.apache.shiro.mgt.SessionsSecurityManager;
  import org.apache.shiro.realm.Realm;
  import org.joda.time.DateTime;
  import org.json.JSONException;
  import org.json.JSONObject;
  import org.obiba.agate.domain.*;
  import org.obiba.agate.event.UserApprovedEvent;
  import org.obiba.agate.event.UserDeletedEvent;
  import org.obiba.agate.event.UserJoinedEvent;
  import org.obiba.agate.repository.RealmConfigRepository;
  import org.obiba.agate.repository.UserCredentialsRepository;
  import org.obiba.agate.repository.UserRepository;
  import org.obiba.agate.service.support.MessageResolverMethod;
  import org.obiba.agate.validator.EmailValidator;
  import org.obiba.agate.validator.NameValidator;
  import org.slf4j.Logger;
  import org.slf4j.LoggerFactory;
  import org.springframework.beans.BeanUtils;
  import org.springframework.beans.factory.annotation.Value;
  import org.springframework.context.MessageSource;
  import org.springframework.core.env.Environment;
  import org.springframework.scheduling.annotation.Scheduled;
  import org.springframework.stereotype.Service;
  import org.springframework.transaction.annotation.Transactional;
  import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

  import jakarta.annotation.Nullable;
  import jakarta.inject.Inject;
  import java.io.IOException;
  import java.util.*;
  import java.util.function.Function;
  import java.util.regex.Pattern;
  import java.util.stream.Collectors;
  import java.util.stream.StreamSupport;

  /**
   * Service class for managing users.
   */
  @Service
  @Transactional
  public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private static final int PWD_MINIMUM_LENGTH = 8;

    private static final int PWD_MAXIMUM_LENGTH = 64;

    static final Pattern PWD_PATTERN = Pattern.compile(
        "^(?=.*[0-9])"       // a digit must occur at least once
            + "(?=.*[a-z])"      // a lower case alphabet must occur at least once
            + "(?=.*[A-Z])"      // a upper case alphabet must occur at least once
            + "(?=.*[@#$%^&+=!])" // a special character that must occur at least once
            + "(?=\\S+$).{" + PWD_MINIMUM_LENGTH + "," + PWD_MAXIMUM_LENGTH + "}$");


    @Value("${login.otpTimeout:300}")
    private int otpTimeout;

    private final UserRepository userRepository;

    private final GroupService groupService;

    private final UserCredentialsRepository userCredentialsRepository;

    private final Environment env;

    private final EventBus eventBus;

    private final Configuration freemarkerConfiguration;

    private final MessageSource messageSource;

    private final MailService mailService;

    private final ConfigurationService configurationService;

    private final RealmConfigRepository realmConfigRepository;

    private final TotpService totpService;

    @Inject
    public UserService(
        UserRepository userRepository,
        GroupService groupService,
        UserCredentialsRepository userCredentialsRepository,
        Environment env,
        EventBus eventBus,
        Configuration freemarkerConfiguration,
        MessageSource messageSource, MailService mailService,
        ConfigurationService configurationService,
        RealmConfigRepository realmConfigRepository,
        TotpService totpService) {
      this.userRepository = userRepository;
      this.groupService = groupService;
      this.userCredentialsRepository = userCredentialsRepository;
      this.env = env;
      this.eventBus = eventBus;
      this.freemarkerConfiguration = freemarkerConfiguration;
      this.messageSource = messageSource;
      this.mailService = mailService;
      this.configurationService = configurationService;
      this.realmConfigRepository = realmConfigRepository;
      this.totpService = totpService;
    }

    //
    // Finders
    //

    /**
     * Find all {@link org.obiba.agate.domain.User}.
     *
     * @return
     */
    public List<User> findUsers() {
      return userRepository.findAll();
    }

    public List<User> findUsers(UserStatus status) {
      return userRepository.findByStatus(status);
    }

    /**
     * Find active users having access to the provided application and optionally belonging to the specified group.
     *
     * @param application
     * @param group       any group if null or empty
     * @return
     */
    public List<User> findActiveUsersByApplicationAndGroup(@Nonnull String application, @Nullable String group) {
      List<String> groupNames = groupService.findByApplication(application).stream() //
          .map(Group::getName) //
          .collect(Collectors.toList());

      return (Strings.isNullOrEmpty(group)
          ? userRepository.findByStatus(UserStatus.ACTIVE)
          : userRepository.findByStatusAndGroups(UserStatus.ACTIVE, group)).stream() //
          .filter(user -> (user.hasApplication(application) || user.hasOneOfGroup(groupNames)) && user.hasGroup(group)) //
          .collect(Collectors.toList());
    }

    public List<User> findActiveUserByApplication(@Nonnull String username, @Nonnull String application) {
      return findActiveUserByApplicationAndGroup(username, application, null);
    }

    public List<User> findActiveUserByApplicationAndGroup(@Nonnull String username, @Nonnull String application,
                                                          @Nullable String group) {
      List<String> groupNames = groupService.findByApplication(application).stream() //
          .map(Group::getName) //
          .collect(Collectors.toList());

      return (Strings.isNullOrEmpty(group)
          ? userRepository.findByNameAndStatus(username, UserStatus.ACTIVE)
          : userRepository.findByNameAndStatusAndGroups(username, UserStatus.ACTIVE, group)).stream() //
          .filter(user -> (user.hasApplication(application) || user.hasOneOfGroup(groupNames)) && user.hasGroup(group)) //
          .collect(Collectors.toList());
    }

    /**
     * Find active users having access to the provided application.
     *
     * @param application
     * @return
     */
    public List<User> findActiveUsersByApplication(@Nonnull String application) {
      return findActiveUsersByApplicationAndGroup(application, null);
    }

    /**
     * Find a {@link org.obiba.agate.domain.User} by its name.
     *
     * @param username
     * @return null if not found
     */
    @Nullable
    public User findUser(@Nonnull String username) {
      List<User> users = userRepository.findByName(username);
      return users == null || users.isEmpty() ? null : users.get(0);
    }

    @Nullable
    public User findActiveUser(@Nonnull String username) {
      List<User> users = userRepository.findByNameAndStatus(username, UserStatus.ACTIVE);
      return users == null || users.isEmpty() ? null : users.get(0);
    }

    @Nullable
    public User findUserByEmail(@Nonnull String email) {
      List<User> users = userRepository.findByEmail(email);
      return users == null || users.isEmpty() ? null : users.get(0);
    }

    @Nullable
    public User findActiveUserByEmail(@Nonnull String email) {
      List<User> users = userRepository.findByEmailAndStatus(email, UserStatus.ACTIVE);
      return users == null || users.isEmpty() ? null : users.get(0);
    }

    public
    @Nullable
    UserCredentials findUserCredentials(@Nonnull String username) {
      List<UserCredentials> users = userCredentialsRepository.findByName(username);
      return users == null || users.isEmpty() ? null : users.get(0);
    }

    public List<User> searchUsers(@Nonnull String prefix) {
      return userRepository.findByNameOrEmailContainingIgnoreCase(prefix, prefix);
    }

    //
    // User methods
    //

    public void updateCurrentUser(String firstName, String lastName, String email) {
      User currentUser = getCurrentUser();
      currentUser.setFirstName(firstName);
      currentUser.setLastName(lastName);
      currentUser.setEmail(email);
      currentUser.setLastModifiedDate(new DateTime());
      userRepository.save(currentUser);
      log.debug("Changed information for User: {}", currentUser);
    }

    public void updateUserPassword(@Nonnull User user, @Nonnull String password) {
      if (user == null) throw new BadRequestException("Invalid User");
      if (Strings.isNullOrEmpty(password)) throw new BadRequestException("User password cannot be empty");
      if (!user.getRealm().equals(AgateRealm.AGATE_USER_REALM.getName()))
        throw new BadRequestException("User password cannot be changed");
      if (password.length() < PWD_MINIMUM_LENGTH) throw new PasswordTooShortException(PWD_MINIMUM_LENGTH);
      if (password.length() > PWD_MAXIMUM_LENGTH) throw new PasswordTooLongException(PWD_MAXIMUM_LENGTH);

      if (!PWD_PATTERN.matcher(password).matches()) throw new PasswordTooWeakException();

      UserCredentials userCredentials = findUserCredentials(user.getName());
      String hashedPassword = hashPassword(password);

      if (userCredentials == null) {
        userCredentials = UserCredentials.newBuilder().name(user.getName()).password(hashPassword(password)).build();
      } else if (userCredentials.getPassword().equals(hashedPassword)) {
        throw new PasswordNotChangedException();
      } else {
        userCredentials.setPassword(hashPassword(password));
      }

      save(userCredentials);
    }

    public void updateUserPassword(@Nonnull User user, @Nonnull String password0, @Nonnull String password) {
      if (user == null) throw new BadRequestException("Invalid User");
      UserCredentials userCredentials = findUserCredentials(user.getName());
      if (userCredentials == null) throw new BadRequestException("Invalid User");
      String hashedPassword0 = hashPassword(password0);
      if (!userCredentials.getPassword().equals(hashedPassword0)) {
        throw new CurrentPasswordInvalidException();
      }
      updateUserPassword(user, password);
    }

    public User createUser(@Nonnull User user, @Nullable String password) {
      checkNameOrEmail(user.getName());
      checkName(user.getFirstName());
      checkName(user.getLastName());

      if (Strings.isNullOrEmpty(password)) {
        if (user.getRealm() == null) user.setRealm(AgateRealm.AGATE_USER_REALM.getName());
        else {
          List<RealmConfig> foundConfigs = getRealmConfigs(user);
          if (foundConfigs.size() == 1) {
            RealmConfig realmConfig = foundConfigs.get(0);
            user.setStatus(UserStatus.ACTIVE);
            user.addGroups(Sets.newHashSet(realmConfig.getGroups()));
          } else {
            user.setRealm(AgateRealm.AGATE_USER_REALM.getName());
          }
        }
      }

      if (AgateRealm.AGATE_USER_REALM.getName().equals(user.getRealm())) {
        if (!Strings.isNullOrEmpty(password)) {
          updateUserPassword(user, password);
        } else if (user.getStatus() == UserStatus.PENDING) {
          eventBus.post(new UserJoinedEvent(user));
        } else if (user.getStatus() == UserStatus.APPROVED) {
          eventBus.post(new UserApprovedEvent(user));
        }
      }

      return save(user);
    }

    /**
     * Insert or update a {@link org.obiba.agate.domain.User}.
     *
     * @param user
     * @return
     */
    public User save(@Nonnull User user) {
      User saved = user;
      UserStatus prevStatus = null;

      boolean uNew = false;
      if (user.isNew()) {
        user.setNameAsId();
        uNew = true;
      } else {
        saved = userRepository.findById(user.getId()).orElse(null);
        if (saved == null) {
          saved = user;
        } else {
          prevStatus = saved.getStatus();
          updateUserCredentials(saved, user);
          BeanUtils.copyProperties(user, saved, "id", "name", "version", "createdBy", "createdDate", "lastModifiedBy",
              "lastModifiedDate");
        }
      }

      // verify user email is unique
      User userWithEmail = findActiveUserByEmail(user.getEmail());
      if (userWithEmail != null && !userWithEmail.getId().equals(user.getId())) {
        throw new EmailAlreadyAssignedException(user.getEmail());
      }

      if (uNew)
        userRepository.insert(user);
      else {
        saved.setLastModifiedDate(new DateTime());
        userRepository.save(saved);
      }
      if (saved.getGroups() != null) {
        for (String groupName : saved.getGroups()) {
          Group group = groupService.findGroup(groupName);
          if (group == null) groupService.save(new Group(groupName));
        }
      }

      if (UserStatus.APPROVED.equals(user.getStatus()) && (prevStatus == null || !prevStatus.equals(user.getStatus())))
        eventBus.post(new UserApprovedEvent(user));

      return saved;
    }

    /**
     * Remove the user credentials if new realm is other than `agate-user-realm`. Notify user for new password if new realm
     * is `agate-user-realm` which in turn will create valid user credentials.
     *
     * @param saved
     * @param user
     */
    private void updateUserCredentials(User saved, User user) {
      String savedRealm = saved.getRealm();
      String newRealm = user.getRealm();

      if (!savedRealm.equals(newRealm)) {
        String agateUserRealm = AgateRealm.AGATE_USER_REALM.getName();

        if (agateUserRealm.equals(savedRealm)) {
          // cleanup credentials
          UserCredentials userCredential = userCredentialsRepository.findOneByName(saved.getName());
          if (userCredential != null) userCredentialsRepository.delete(userCredential);
        } else if (agateUserRealm.equals(newRealm)) {
          // Re-approve the user and send email so user to set a password
          user.setStatus(UserStatus.APPROVED);
          eventBus.post(new UserApprovedEvent(user));
        }
      }

    }

    public AuthenticationInfo test(String provider, UsernamePasswordToken token) {
      SessionsSecurityManager securityManager = (SessionsSecurityManager) SecurityUtils.getSecurityManager();
      Optional<Realm> optionalRealm = securityManager.getRealms().stream().filter(realm -> realm.getName().equals(provider)).findFirst();

      if (optionalRealm.isPresent()) {
        Realm realm = optionalRealm.get();
        RealmConfig realmConfig = realmConfigRepository.findOneByName(realm.getName());
        return realm.getAuthenticationInfo(token);
      }

      return null;
    }

    public UserCredentials save(@Nonnull UserCredentials userCredentials) {
      userCredentials.setLastModifiedDate(new DateTime());
      userCredentialsRepository.save(userCredentials);
      return userCredentials;
    }

    public void updateUserStatus(User user, UserStatus status) {
      user.setStatus(status);
      save(user);
    }

    public void confirmUser(@Nonnull User user, String password) {
      UserCredentials userCredentials = findUserCredentials(user.getName());

      if (userCredentials == null) {
        userCredentials = UserCredentials.newBuilder().name(user.getName()).build();
      }

      if (!PWD_PATTERN.matcher(password).matches()) throw new PasswordTooWeakException();

      userCredentials.setPassword(hashPassword(password));
      userCredentials.setLastModifiedDate(new DateTime());
      userCredentialsRepository.save(userCredentials);

      user.setStatus(UserStatus.ACTIVE);
      save(user);
    }

    public void updateUserLastLogin(@Nonnull String username) {
      User user = findUser(username);

      if (user != null) {
        user.setLastLogin(DateTime.now());
        user.setOtp(null);
        save(user);
      }
    }

    @Scheduled(cron = "0 0 0 * * ?") //every day at midnight
    public void removeInactiveUsers() {
      List<User> inactiveUsers = userRepository.findByRoleAndLastLoginLessThan("agate-user",
          DateTime.now().minusHours(configurationService.getConfiguration().getInactiveTimeout()));

      inactiveUsers.forEach(u -> {
        u.setStatus(UserStatus.INACTIVE);
        userRepository.save(u);
      });
    }

    public void resetPassword(User user) throws IOException {
      ObjectMapper mapper = new ObjectMapper();
      String keyData = mapper.writeValueAsString(new HashMap<String, String>() {{
        put("username", user.getName());
        put("expire", DateTime.now().plusHours(1).toString());
      }});

      String key = configurationService.encrypt(keyData);
      Map<String, Object> ctx = Maps.newHashMap();
      String organization = configurationService.getConfiguration().getName();
      ctx.put("key", key);

      sendEmail(user, "[" + organization + "] " + env.getProperty("registration.resetPasswordSubject"),
          "resetPasswordEmail", ctx);
    }

    @Subscribe
    public void sendPendingEmail(UserJoinedEvent userJoinedEvent) {
      log.info("Sending pending review email: {}", userJoinedEvent.getPersistable());
      List<User> administrators = userRepository.findByRole("agate-administrator");
      User user = userJoinedEvent.getPersistable();
      String organization = configurationService.getConfiguration().getName();

      Map<String, Object> context = Maps.newHashMap();
      context.put("user", new UserProfile(user));

      administrators.forEach(u -> sendEmail(u, "[" + organization + "] " + env.getProperty("registration.pendingForReviewSubject"),
          "pendingForReviewEmail", context));

      sendEmail(user, "[" + organization + "] " + env.getProperty("registration.pendingForApprovalSubject"),
          "pendingForApprovalEmail", context);
    }

    @Subscribe
    public void sendConfirmationEmail(UserApprovedEvent userApprovedEvent) {
      log.info("Sending confirmation email: {}", userApprovedEvent.getPersistable());
      User user = userApprovedEvent.getPersistable();
      String organization = configurationService.getConfiguration().getName();
      Map<String, Object> ctx = Maps.newHashMap();
      ctx.put("key", configurationService.encrypt(user.getName()));
      sendEmail(user, "[" + organization + "] " + env.getProperty("registration.confirmationSubject"),
          "confirmationEmail", ctx);
    }

    private void sendEmail(User user, String subject, String templateName, Map<String, Object> context) {
      Locale locale = LocaleUtils.toLocale(user.getPreferredLanguage());
      Map<String, Object> ctx = context == null ? Maps.newHashMap() : Maps.newHashMap(context);
      if (!ctx.containsKey("user"))
        ctx.put("user", new UserProfile(user));
      ctx.put("organization", configurationService.getConfiguration().getName());
      // get user's realm and find if there is a specific agate base url for this realm
      Optional<RealmConfig> realmConfig = getRealmConfigs(user).stream().findFirst();
      ctx.put("publicUrl", realmConfig.isPresent() && realmConfig.get().hasPublicUrl() ? realmConfig.get().getPublicUrl() : configurationService.getPublicUrl());
      ctx.put("msg", new MessageResolverMethod(messageSource, locale));
      String templateLocation = "notifications/" + templateName + ".ftl";
      try {
        Template template = freemarkerConfiguration.getTemplate(templateLocation, locale);
        mailService
            .sendEmail(user.getEmail(), subject,
                FreeMarkerTemplateUtils.processTemplateIntoString(template, ctx));
      } catch (Exception e) {
        log.error("Error while handling template {}", templateLocation, e);
      }
    }

    /**
     * Delete a {@link org.obiba.agate.domain.User}.
     *
     * @param id
     */
    public void delete(@Nonnull String id) {
      Optional<User> toDelete = userRepository.findById(id);
      toDelete.ifPresent(this::delete);
    }

    /**
     * Delete a {@link org.obiba.agate.domain.User}.
     *
     * @param user
     */
    public void delete(@Nonnull User user) {
      UserCredentials userCredentials = findUserCredentials(user.getName());
      if (userCredentials != null) userCredentialsRepository.delete(userCredentials);
      userRepository.delete(user);

      eventBus.post(new UserDeletedEvent(user));
    }

    /**
     * Get user with id and throws {@link org.obiba.agate.service.NoSuchUserException} if not found.
     *
     * @param id
     * @return
     */
    public User getUser(String id) {
      Optional<User> user = userRepository.findById(id);
      if (!user.isPresent()) throw NoSuchUserException.withId(id);
      return user.get();
    }

    /**
     * Get JSON representation of user profile.
     *
     * @param user
     * @return
     * @throws JSONException
     */
    public JSONObject getUserProfile(User user) throws JSONException {
      List<AttributeConfiguration> attrConfigs = configurationService.getConfiguration().getUserAttributes();
      Map<String, AttributeConfiguration> attrConfigMap = attrConfigs.stream().collect(Collectors.toMap(AttributeConfiguration::getName, Function.identity()));
      JSONObject profile = new JSONObject();
      profile.put("email", user.getEmail());
      profile.put("firstname", user.getFirstName());
      profile.put("lastname", user.getLastName());
      profile.put("locale", user.getPreferredLanguage());
      profile.put("realm", user.getRealm());

      if (user.hasAttributes()) {
        user.getAttributes().forEach((k, v) -> {
          try {
            if (attrConfigMap.containsKey(k)) {
              AttributeConfiguration attrConfig = attrConfigMap.get(k);
              switch (attrConfig.getType()) {
                case BOOLEAN:
                  profile.put(k, Boolean.parseBoolean(v));
                  break;
                case INTEGER:
                  profile.put(k, Strings.isNullOrEmpty(v) ? null : Long.parseLong(v));
                  break;
                case NUMBER:
                  profile.put(k, Strings.isNullOrEmpty(v) ? null : Double.parseDouble(v));
                  break;
                default:
                  profile.put(k, v);
              }
            } else {
              profile.put(k, v);
            }
          } catch (JSONException e) {
            //ignore
          }
        });
      }

      return profile;
    }

    /**
     * Update user profile from a JSON representation.
     *
     * @param user
     * @param profile
     * @throws JSONException
     */
    public void updateUserProfile(User user, JSONObject profile) throws JSONException {
      Iterable<String> iterable = profile::keys;
      StreamSupport.stream(iterable.spliterator(), false).forEach(k -> {
        String value;
        try {
          value = profile.get(k) == null ? null : profile.get(k).toString();
          if ("firstname".equals(k)) {
            user.setFirstName(value);
          } else if ("lastname".equals(k)) {
            user.setLastName(value);
          } else if ("email".equals(k)) {
            user.setEmail(value);
          } else if ("locale".equals(k)) {
            user.setPreferredLanguage(value);
          } else {
            user.getAttributes().put(k, value);
          }
        } catch (JSONException e) {
          log.warn("Unable to read profile value '{}'", k, e);
        }
      });
      profile.keys();

      save(user);
    }

    /**
     * Get currently logged user and throws {@link org.obiba.agate.service.NoSuchUserException} if not found.
     *
     * @return
     * @throws org.obiba.agate.service.NoSuchUserException
     */
    public User getCurrentUser() {
      if (SecurityUtils.getSubject().getPrincipal() == null) {
        throw new UnauthenticatedException();
      }
      String username = SecurityUtils.getSubject().getPrincipal().toString();
      User currentUser = findUser(username);
      if (currentUser == null) throw NoSuchUserException.withName(username);
      return currentUser;
    }

    /**
     * Get currently logged user from {@link org.obiba.agate.security.AgateUserRealm} and throws
     * {@link org.obiba.agate.service.NoSuchUserException} if not found or if user is not bound to this realm.
     *
     * @return
     * @throws org.obiba.agate.service.NoSuchUserException
     */
    public UserCredentials getCurrentUserCredentials() {
      User user = getCurrentUser();
      if (!user.getRealm().equals(AgateRealm.AGATE_USER_REALM)) throw NoSuchUserException.withName(user.getName());
      UserCredentials currentUser = findUserCredentials(user.getName());
      if (currentUser == null) throw NoSuchUserException.withName(user.getName());
      return currentUser;
    }

    /**
     * Hash user password.
     *
     * @param password
     * @return
     */
    public String hashPassword(String password) {
      return new Sha512Hash(password, env.getProperty("shiro.password.salt"),
          env.getProperty("shiro.password.nbHashIterations", Integer.class, 10000)).toString();
    }

    /**
     * Get all the applications the user has access to: explicitly defined and inherited from the groups.
     *
     * @param user
     * @return
     */
    public Set<String> getUserApplications(User user) {
      Set<String> applications = Sets.newTreeSet();
      if (user.hasApplications()) applications.addAll(user.getApplications());
      if (user.hasGroups())
        user.getGroups().forEach(g -> Optional.ofNullable(groupService.findGroup(g)).flatMap((Group r) -> {
          r.getApplications().forEach(applications::add);
          return Optional.of(r);
        }));
      return applications;
    }

    public boolean validateCode(User user, String code) {
      return totpService.validateCode(code, user.getSecret());
    }

    public boolean validateOtp(User user, String code) {
      JSONObject otp = new JSONObject(configurationService.decrypt(user.getOtp()));
      long now = DateTime.now().getMillis();
      user.setOtp(null);
      save(user);
      return now < otp.getLong("expires") && code.equals(otp.getString("code"));
    }

    public void applyAndSendOtp(User user) {
      String code = totpService.generateRandomCode();
      long now = DateTime.now().getMillis();
      JSONObject otp = new JSONObject();
      otp.put("code", code);
      otp.put("expires", now + otpTimeout*1000); // millis
      user.setOtp(configurationService.encrypt(otp.toString()));
      save(user);

      Map<String, Object> ctx = Maps.newHashMap();
      String organization = configurationService.getConfiguration().getName();
      ctx.put("code", code);
      ctx.put("timeout", otpTimeout/60); // minutes

      sendEmail(user, "[" + organization + "] Code", "otpEmail", ctx);
    }

    public JSONObject applyTempSecret(User user) {
      String newSecret = totpService.generateSecret();
      String newQrImage = totpService.getQrImageDataUri(user.getEmail(), newSecret);
      user.resetSecret(newSecret);
      user.setSecret(null);
      save(user);
      JSONObject otp = new JSONObject();
      otp.put("image", newQrImage);
      return otp;
    }

    private List<RealmConfig> getRealmConfigs(User user) {
      return realmConfigRepository.findAll().stream().filter(realmConfig -> user.getRealm().equals(realmConfig.getName())).collect(Collectors.toList());
    }

    private void checkName(String name) {
      if (!NameValidator.isValid(name)) {
        throw new BadRequestException("Name contains invalid characters");
      }
    }

    private void checkNameOrEmail(String name) {
      if (!NameValidator.isValid(name) && !EmailValidator.isValid(name)) {
        throw new BadRequestException("Name contains invalid characters");
      }
    }
  }
