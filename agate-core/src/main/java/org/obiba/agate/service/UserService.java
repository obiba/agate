package org.obiba.agate.service;

import java.io.IOException;
import java.security.SignatureException;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.BadRequestException;

import org.apache.commons.lang.LocaleUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.crypto.hash.Sha512Hash;
import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;
import org.obiba.agate.domain.Group;
import org.obiba.agate.domain.User;
import org.obiba.agate.domain.UserCredentials;
import org.obiba.agate.domain.UserStatus;
import org.obiba.agate.event.UserApprovedEvent;
import org.obiba.agate.event.UserJoinedEvent;
import org.obiba.agate.repository.GroupRepository;
import org.obiba.agate.repository.UserCredentialsRepository;
import org.obiba.agate.repository.UserRepository;
import org.obiba.agate.security.AgateUserRealm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertyResolver;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring4.SpringTemplateEngine;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

/**
 * Service class for managing users.
 */
@Service
@Transactional
public class UserService {

  private static final Logger log = LoggerFactory.getLogger(UserService.class);

  private static final int MINIMUM_LEMGTH = 8;

  @Inject
  private UserRepository userRepository;

  @Inject
  private UserCredentialsRepository userCredentialsRepository;

  @Inject
  private GroupRepository groupRepository;

  @Inject
  private Environment env;

  @Inject
  private EventBus eventBus;

  @Inject
  private SpringTemplateEngine templateEngine;

  @Inject
  private MailService mailService;

  @Inject
  private ConfigurationService configurationService;

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
   * @param group any group if null or empty
   * @return
   */
  public List<User> findActiveUsersByApplicationAndGroup(@NotNull String application, @Nullable String group) {
    List<String> groupNames = groupRepository.findByApplications(application).stream() //
      .map(Group::getName) //
      .collect(Collectors.toList());

    return (Strings.isNullOrEmpty(group)
      ? userRepository.findByStatus(UserStatus.ACTIVE)
      : userRepository.findByStatusAndGroups(UserStatus.ACTIVE, group)).stream() //
      .filter(user -> (user.hasApplication(application) || user.hasOneOfGroup(groupNames)) && user.hasGroup(group)) //
      .collect(Collectors.toList());
  }

  public List<User> findActiveUserByApplication(@NotNull String username, @NotNull String application) {
    return findActiveUserByApplicationAndGroup(username, application, null);
  }

  public List<User> findActiveUserByApplicationAndGroup(@NotNull String username, @NotNull String application,
    @Nullable String group) {
    List<String> groupNames = groupRepository.findByApplications(application).stream() //
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
  public List<User> findActiveUsersByApplication(@NotNull String application) {
    return findActiveUsersByApplicationAndGroup(application, null);
  }

  /**
   * Find a {@link org.obiba.agate.domain.User} by its name.
   *
   * @param username
   * @return null if not found
   */
  @Nullable
  public User findUser(@NotNull String username) {
    List<User> users = userRepository.findByName(username);
    return users == null || users.isEmpty() ? null : users.get(0);
  }

  @Nullable
  public User findActiveUser(@NotNull String username) {
    List<User> users = userRepository.findByNameAndStatus(username, UserStatus.ACTIVE);
    return users == null || users.isEmpty() ? null : users.get(0);
  }

  @Nullable
  public User findUserByEmail(@NotNull String email) {
    List<User> users = userRepository.findByEmail(email);
    return users == null || users.isEmpty() ? null : users.get(0);
  }

  @Nullable
  public User findActiveUserByEmail(@NotNull String email) {
    List<User> users = userRepository.findByEmailAndStatus(email, UserStatus.ACTIVE);
    return users == null || users.isEmpty() ? null : users.get(0);
  }

  public
  @Nullable
  UserCredentials findUserCredentials(@NotNull String username) {
    List<UserCredentials> users = userCredentialsRepository.findByName(username);
    return users == null || users.isEmpty() ? null : users.get(0);
  }

  //
  // User methods
  //

  public void updateCurrentUser(String firstName, String lastName, String email) {
    User currentUser = getCurrentUser();
    currentUser.setFirstName(firstName);
    currentUser.setLastName(lastName);
    currentUser.setEmail(email);
    userRepository.save(currentUser);
    log.debug("Changed information for User: {}", currentUser);
  }

  public void updateUserPassword(@NotNull User user, @NotNull String password) {
    if(user == null) throw new BadRequestException("Invalid User");
    if(Strings.isNullOrEmpty(password)) throw new BadRequestException("User password cannot be empty");
    if(!user.getRealm().equals(AgateUserRealm.AGATE_REALM))
      throw new BadRequestException("User password cannot be changed");
    if(password.length() < MINIMUM_LEMGTH) throw new PasswordTooShortException(MINIMUM_LEMGTH);

    UserCredentials userCredentials = findUserCredentials(user.getName());
    String hashedPassword = hashPassword(password);

    if(userCredentials == null) {
      userCredentials = UserCredentials.newBuilder().name(user.getName()).password(hashPassword(password)).build();
    } else if(userCredentials.getPassword().equals(hashedPassword)) {
      throw new PasswordNotChangedException();
    } else {
      userCredentials.setPassword(hashPassword(password));
    }

    save(userCredentials);
  }

  public User createUser(@NotNull User user, @Nullable String password) {
    User saved = save(user);

    if(!Strings.isNullOrEmpty(password)) {
      updateUserPassword(user, password);
    } else if(user.getStatus() == UserStatus.PENDING) {
      eventBus.post(new UserJoinedEvent(user));
    } else if(user.getStatus() == UserStatus.APPROVED) {
      eventBus.post(new UserApprovedEvent(user));
    }

    return saved;
  }

  /**
   * Insert or update a {@link org.obiba.agate.domain.User}.
   *
   * @param user
   * @return
   */
  public User save(@NotNull User user) {
    User saved = user;

    if(user.isNew()) {
      user.setNameAsId();
    } else {
      saved = userRepository.findOne(user.getId());
      if(saved == null) {
        saved = user;
      } else {
        BeanUtils.copyProperties(user, saved, "id", "name", "version", "createdBy", "createdDate", "lastModifiedBy",
          "lastModifiedDate");
      }
    }

    // verify user email is unique
    User userWithEmail = findActiveUserByEmail(user.getEmail());
    if(userWithEmail != null && !userWithEmail.getId().equals(user.getId())) {
      throw new EmailAlreadyAssignedException(user.getEmail());
    }

    userRepository.save(saved);

    if(saved.getGroups() != null) {
      for(String groupName : saved.getGroups()) {
        Group group = findGroup(groupName);
        if(group == null) save(new Group(groupName));
      }
    }

    return saved;
  }

  public UserCredentials save(@NotNull UserCredentials userCredentials) {
    userCredentialsRepository.save(userCredentials);
    return userCredentials;
  }

  public User createUser(User user) {
    return createUser(user, null);
  }

  public void updateUserStatus(User user, UserStatus status) {
    UserStatus prevStatus = user.getStatus();

    user.setStatus(status);
    save(user);

    if(prevStatus == UserStatus.PENDING && user.getStatus() == UserStatus.APPROVED)
      eventBus.post(new UserApprovedEvent(user));
  }

  public void confirmUser(@NotNull User user, String password) {
    UserCredentials userCredentials = findUserCredentials(user.getName());

    if(userCredentials == null) {
      userCredentials = UserCredentials.newBuilder().name(user.getName()).build();
    }

    userCredentials.setPassword(hashPassword(password));

    userCredentialsRepository.save(userCredentials);

    user.setStatus(UserStatus.ACTIVE);
    save(user);
  }

  public void updateUserLastLogin(@NotNull String username) {
    User user = findUser(username);

    if(user != null) {
      user.setLastLogin(DateTime.now());
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

    RelaxedPropertyResolver propertyResolver = new RelaxedPropertyResolver(env, "registration.");
    Context ctx = new Context();
    String organization = configurationService.getConfiguration().getName();
    ctx.setLocale(LocaleUtils.toLocale(user.getPreferredLanguage()));
    ctx.setVariable("user", user);
    ctx.setVariable("organization", organization);
    ctx.setVariable("publicUrl", configurationService.getPublicUrl());
    ctx.setVariable("key", key);

    mailService
      .sendEmail(user.getEmail(), "[" + organization + "] " + propertyResolver.getProperty("resetPasswordSubject"),
        templateEngine.process("resetPasswordEmail", ctx));
  }

  @Subscribe
  public void sendPendingEmail(UserJoinedEvent userJoinedEvent) throws SignatureException {
    log.info("Sending pending review email: {}", userJoinedEvent.getPersistable());
    PropertyResolver propertyResolver = new RelaxedPropertyResolver(env, "registration.");
    List<User> administrators = userRepository.findByRole("agate-administrator");
    Context ctx = new Context();
    User user = userJoinedEvent.getPersistable();
    String organization = configurationService.getConfiguration().getName();
    ctx.setLocale(LocaleUtils.toLocale(user.getPreferredLanguage()));
    ctx.setVariable("user", user);
    ctx.setVariable("organization", organization);
    ctx.setVariable("publicUrl", configurationService.getPublicUrl());

    administrators.stream().forEach(u -> mailService
      .sendEmail(u.getEmail(), "[" + organization + "] " + propertyResolver.getProperty("pendingForReviewSubject"),
        templateEngine.process("pendingForReviewEmail", ctx)));

    mailService
      .sendEmail(user.getEmail(), "[" + organization + "] " + propertyResolver.getProperty("pendingForApprovalSubject"),
        templateEngine.process("pendingForApprovalEmail", ctx));
  }

  @Subscribe
  public void sendConfirmationEmail(UserApprovedEvent userApprovedEvent) throws SignatureException {
    log.info("Sending confirmation email: {}", userApprovedEvent.getPersistable());
    PropertyResolver propertyResolver = new RelaxedPropertyResolver(env, "registration.");
    Context ctx = new Context();
    User user = userApprovedEvent.getPersistable();
    String organization = configurationService.getConfiguration().getName();
    ctx.setLocale(LocaleUtils.toLocale(user.getPreferredLanguage()));
    ctx.setVariable("user", user);
    ctx.setVariable("organization", organization);
    ctx.setVariable("publicUrl", configurationService.getPublicUrl());
    ctx.setVariable("key", configurationService.encrypt(user.getName()));

    mailService
      .sendEmail(user.getEmail(), "[" + organization + "] " + propertyResolver.getProperty("confirmationSubject"),
        templateEngine.process("confirmationEmail", ctx));
  }

  /**
   * Delete a {@link org.obiba.agate.domain.User}.
   *
   * @param id
   */
  public void delete(@NotNull String id) {
    delete(userRepository.findOne(id));
  }

  /**
   * Delete a {@link org.obiba.agate.domain.User}.
   *
   * @param user
   */
  public void delete(@NotNull User user) {
    UserCredentials userCredentials = findUserCredentials(user.getName());
    if(userCredentials != null) userCredentialsRepository.delete(userCredentials);
    userRepository.delete(user);
  }

  /**
   * Get user with id and throws {@link org.obiba.agate.service.NoSuchUserException} if not found.
   *
   * @param id
   * @return
   */
  public User getUser(String id) {
    User user = userRepository.findOne(id);
    if(user == null) throw NoSuchUserException.withId(id);
    return user;
  }

  /**
   * Get JSON representation of user profile.
   *
   * @param user
   * @return
   * @throws JSONException
   */
  public JSONObject getUserProfile(User user) throws JSONException {
    JSONObject profile = new JSONObject();
    profile.put("email", user.getEmail());
    profile.put("firstname", user.getFirstName());
    profile.put("lastname", user.getLastName());
    profile.put("locale", user.getPreferredLanguage());

    if(user.hasAttributes()) {
      user.getAttributes().forEach((k, v) -> {
        try {
          profile.put(k, v);
        } catch(JSONException e) {
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
    Iterable<String> iterable = () -> profile.keys();
    StreamSupport.stream(iterable.spliterator(), false).forEach(k -> {
      String value = null;
      try {
        value = profile.getString(k);
        if("firstname".equals(k)) {
          user.setFirstName(value);
        } else if("lastname".equals(k)) {
          user.setLastName(value);
        } else if("email".equals(k)) {
          user.setEmail(value);
        } else if("locale".equals(k)) {
          user.setPreferredLanguage(value);
        } else {
          user.getAttributes().put(k, value);
        }
      } catch(JSONException e) {
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
    String username = SecurityUtils.getSubject().getPrincipal().toString();
    User currentUser = findUser(username);
    if(currentUser == null) throw NoSuchUserException.withName(username);
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
    if(!user.getRealm().equals(AgateUserRealm.AGATE_REALM)) throw NoSuchUserException.withName(user.getName());
    UserCredentials currentUser = findUserCredentials(user.getName());
    if(currentUser == null) throw NoSuchUserException.withName(user.getName());
    return currentUser;
  }

  /**
   * Hash user password.
   *
   * @param password
   * @return
   */
  public String hashPassword(String password) {
    RelaxedPropertyResolver propertyResolver = new RelaxedPropertyResolver(env, "shiro.password.");
    return new Sha512Hash(password, propertyResolver.getProperty("salt"),
      propertyResolver.getProperty("nbHashIterations", Integer.class)).toString();
  }

  /**
   * Get all the applications the user has access to: explicitly defined and inherited from the groups.
   *
   * @param user
   * @return
   */
  public Set<String> getUserApplications(User user) {
    Set<String> applications = Sets.newTreeSet();
    if(user.hasApplications()) applications.addAll(user.getApplications());
    if(user.hasGroups()) user.getGroups().forEach(g -> Optional.ofNullable(findGroup(g)).flatMap(r -> {
      r.getApplications().forEach(applications::add);
      return Optional.of(r);
    }));
    return applications;
  }

  //
  // Group methods
  //

  /**
   * Find all {@link org.obiba.agate.domain.Group}.
   *
   * @return
   */
  public List<Group> findGroups() {
    return groupRepository.findAll();
  }

  /**
   * Find a {@link org.obiba.agate.domain.Group} by its name.
   *
   * @param name
   * @return null if not found
   */
  public
  @Nullable
  Group findGroup(@NotNull String name) {
    List<Group> groups = groupRepository.findByName(name);
    return groups == null || groups.isEmpty() ? null : groups.get(0);
  }

  /**
   * Get group with id and throws {@link org.obiba.agate.service.NoSuchGroupException} if not found.
   *
   * @param id
   * @return
   */
  public Group getGroup(String id) {
    Group group = groupRepository.findOne(id);
    if(group == null) throw NoSuchGroupException.withId(id);
    return group;
  }

  /**
   * Insert of update a {@link org.obiba.agate.domain.Group}.
   *
   * @param group
   * @return
   */
  public Group save(@NotNull Group group) {
    if(group.isNew()) {
      group.setNameAsId();
    }
    groupRepository.save(group);
    return group;
  }

  /**
   * Delete a {@link org.obiba.agate.domain.Group}.
   *
   * @param user
   */
  public void delete(@NotNull Group group) {
    for(User user : userRepository.findAll()) {
      if(user.getGroups().contains(group.getName())) {
        throw NotOrphanGroupException.withName(group.getName());
      }
    }

    groupRepository.delete(group);
  }

}
