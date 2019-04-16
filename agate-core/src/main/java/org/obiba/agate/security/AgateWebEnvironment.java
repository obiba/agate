package org.obiba.agate.security;

import javax.inject.Inject;
import org.apache.shiro.mgt.SessionsSecurityManager;
import org.apache.shiro.web.env.DefaultWebEnvironment;
import org.springframework.stereotype.Component;

@Component
public class AgateWebEnvironment extends DefaultWebEnvironment {

  @Inject
  public AgateWebEnvironment(
    SessionsSecurityManager securityManager) {
    super();
    this.setSecurityManager(securityManager);
  }
}
