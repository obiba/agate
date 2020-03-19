package org.obiba.agate.config;

import org.obiba.agate.web.interceptor.ConfigurationInterceptor;
import org.obiba.agate.web.interceptor.SessionInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.inject.Inject;

@Configuration
public class WebMvcConfiguration extends WebMvcConfigurerAdapter {

  private final SessionInterceptor sessionInterceptor;

  private final ConfigurationInterceptor configurationInterceptor;

  @Inject
  public WebMvcConfiguration(SessionInterceptor sessionInterceptor, ConfigurationInterceptor configurationInterceptor) {
    this.sessionInterceptor = sessionInterceptor;
    this.configurationInterceptor = configurationInterceptor;
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(sessionInterceptor);
    registry.addInterceptor(configurationInterceptor);
  }

}
