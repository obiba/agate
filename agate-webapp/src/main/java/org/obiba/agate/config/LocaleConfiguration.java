/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.agate.config;

import com.google.common.eventbus.Subscribe;
import org.obiba.agate.config.locale.AngularCookieLocaleResolver;
import org.obiba.agate.config.locale.ExtendedResourceBundleMessageSource;
import org.obiba.agate.event.AgateConfigUpdatedEvent;
import org.obiba.agate.service.ConfigurationService;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import javax.inject.Inject;
import java.util.Locale;

@Configuration
public class LocaleConfiguration implements WebMvcConfigurer, EnvironmentAware {

  private Environment environment;

  private final ConfigurationService configurationService;

  private ExtendedResourceBundleMessageSource messageSource;

  @Inject
  public LocaleConfiguration(ConfigurationService configurationService) {
    this.configurationService = configurationService;
  }

  @Override
  public void setEnvironment(Environment environment) {
    this.environment = environment;
  }

  @Bean(name = "localeResolver")
  public LocaleResolver localeResolver() {
    AngularCookieLocaleResolver cookieLocaleResolver = new AngularCookieLocaleResolver(configurationService);
    //cookieLocaleResolver.setDefaultLocale(Locale.ENGLISH);
    return cookieLocaleResolver;
  }

  @Bean
  public MessageSource messageSource() {
    int cacheSeconds = environment.getProperty("spring.messageSource.cacheSeconds", Integer.class, 60);
    messageSource = new ExtendedResourceBundleMessageSource(configurationService, cacheSeconds);
    messageSource.setBasenames("classpath:/translations/messages", "classpath:/translations/notifications/messages", "classpath:/i18n/messages", "classpath:/i18n/notifications/messages");
    messageSource.setDefaultEncoding("UTF-8");
    messageSource.setCacheSeconds(cacheSeconds);
    return messageSource;
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    LocaleChangeInterceptor localeChangeInterceptor = new LocaleChangeInterceptor();
    localeChangeInterceptor.setParamName("language");

    registry.addInterceptor(localeChangeInterceptor);
  }

  @Async
  @Subscribe
  public void configUpdated(AgateConfigUpdatedEvent event) {
    if (messageSource != null)
      messageSource.evict();
  }
}

