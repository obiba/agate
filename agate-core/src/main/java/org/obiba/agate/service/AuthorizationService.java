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

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import jakarta.annotation.Nonnull;
import org.joda.time.DateTime;
import org.obiba.agate.domain.Authorization;
import org.obiba.agate.event.ApplicationDeletedEvent;
import org.obiba.agate.event.AuthorizationDeletedEvent;
import org.obiba.agate.event.UserDeletedEvent;
import org.obiba.agate.repository.AuthorizationRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Optional;

/**
 * Manage the {@link org.obiba.agate.domain.Authorization} entities.
 */
@Service
public class AuthorizationService {

  @Inject
  private AuthorizationRepository authorizationRepository;

  @Inject
  private ConfigurationService configurationService;

  @Inject
  private UserService userService;

  @Inject
  private EventBus eventBus;

  /**
   * Persist the {@link Authorization}.
   *
   * @param authorization
   * @return
   */
  public Authorization save(Authorization authorization) {
    return authorizationRepository.save(authorization);
  }

  /**
   * Get the {@link Authorization} by its ID.
   *
   * @param id
   * @return
   * @throws NoSuchAuthorizationException
   */
  public Authorization get(@Nonnull String id) {
    Optional<Authorization> authorization = authorizationRepository.findById(id);
    if(!authorization.isPresent()) throw NoSuchAuthorizationException.withId(id);
    return authorization.get();
  }

  /**
   * Get the {@link Authorization} by its code (the authorization_code in the OAuth context).
   *
   * @param code
   * @return
   * @throws NoSuchAuthorizationException
   */
  public Authorization getByCode(@Nonnull String code) {
    Optional<Authorization> authorization = authorizationRepository.findByCode(code).stream().findFirst();
    if(!authorization.isPresent()) throw NoSuchAuthorizationException.withCode(code);
    return authorization.get();
  }

  /**
   * Get the unique {@link Authorization} for the user name and application.
   *
   * @param username
   * @param application
   * @return
   * @throws NoSuchAuthorizationException
   */
  public Authorization get(@Nonnull String username, @Nonnull String application) {
    Authorization authorization = find(username, application);
    if(authorization == null) throw NoSuchAuthorizationException.withUsernameAndApplication(username, application);
    return authorization;
  }

  /**
   * Get all the authorizations associated to the user name.
   *
   * @param username
   * @return
   */
  public List<Authorization> list(@Nonnull String username) {
    return authorizationRepository.findByUsername(username);
  }

  /**
   * Find {@link Authorization} by its ID.
   *
   * @param id null if not found
   * @return
   */
  @Nullable
  public Authorization find(@Nonnull String id) {
    return authorizationRepository.findById(id).get();
  }

  /**
   * Find {@link Authorization} by its user name and application.
   *
   * @param username
   * @param application
   * @return null if not found
   */
  @Nullable
  public Authorization find(@Nonnull String username, @Nonnull String application) {
    Optional<Authorization> authorization = authorizationRepository.findByUsernameAndApplication(username, application)
      .stream().findFirst();
    return authorization.orElse(null);
  }

  /**
   * Delete all the listed {@link Authorization}s.
   *
   * @param authorizations
   */
  public void delete(List<Authorization> authorizations) {
    if(authorizations == null) return;
    authorizations.forEach(this::delete);
  }

  /**
   * Delete the {@link Authorization} with the given ID.
   *
   * @param id
   */
  public void delete(@Nonnull String id) {
    delete(authorizationRepository.findById(id).get());
  }

  /**
   * Delete the {@link Authorization}.
   *
   * @param authorization
   */
  public void delete(Authorization authorization) {
    if(authorization == null) return;
    authorizationRepository.deleteById(authorization.getId());
    eventBus.post(new AuthorizationDeletedEvent(authorization));
  }

  /**
   * Compute expiration date using configured timeouts.
   *
   * @param authorization
   * @return
   */
  public DateTime getExpirationDate(Authorization authorization) {
    return getExpirationDate(authorization.getCreatedDate());
  }

  //
  // Event handling
  //

  @Subscribe
  public void onUserDeleted(UserDeletedEvent event) {
    authorizationRepository.findByUsername(event.getPersistable().getName())
      .forEach(a -> authorizationRepository.delete(a));
  }

  @Subscribe
  public void onApplicationDeleted(ApplicationDeletedEvent event) {
    delete(authorizationRepository.findByApplication(event.getPersistable().getName()));
  }

  /**
   * Remove the expired {@link Authorization}s.
   */
  @Scheduled(cron = "0 0/15 * * * *")
  public void removeExpired() {
    delete(authorizationRepository
      .findByCreatedDateBefore(DateTime.now().minusHours(configurationService.getConfiguration().getLongTimeout())));
  }

  //
  // Private methods
  //

  private DateTime getExpirationDate(DateTime created) {
    return created.plusHours(configurationService.getConfiguration().getLongTimeout());
  }
}
