/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.agate.web.rest.security;

import org.apache.shiro.subject.Subject;
import org.obiba.shiro.web.filter.AbstractAuthenticationExecutor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationExecutorImpl extends AbstractAuthenticationExecutor implements InitializingBean {

  @Value("${login.maxTry:3}")
  private int maxTry;

  @Value("${login.trialTime:300}")
  private int trialTime;

  @Value("${login.banTime:300}")
  private int banTime;

  @Override
  public void afterPropertiesSet() {
    configureBan(maxTry, trialTime, banTime);
  }

  @Override
  protected void ensureProfile(Subject subject) {
    // do nothing
  }
}
