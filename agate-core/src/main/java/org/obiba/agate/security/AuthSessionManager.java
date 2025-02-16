/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.agate.security;

import org.obiba.oidc.OIDCSession;
import org.obiba.oidc.utils.DefaultOIDCSessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AuthSessionManager extends DefaultOIDCSessionManager {
  private final Logger log = LoggerFactory.getLogger(AuthSessionManager.class);
  @Override
  public void saveSession(OIDCSession session) {
    log.debug("Saving session {}", session.getId());
    super.saveSession(session);
  }

  @Override
  public boolean hasSession(String client) {
    log.debug("Checking if session {} exists", client);
    return super.hasSession(client);
  }

  @Override
  public OIDCSession getSession(String client) {
    log.debug("Getting session {}", client);
    return super.getSession(client);
  }
}
