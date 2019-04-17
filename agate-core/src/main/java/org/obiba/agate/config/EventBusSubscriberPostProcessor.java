package org.obiba.agate.config;

import java.lang.reflect.Method;
import javax.inject.Inject;
import org.apache.shiro.event.EventBus;
import org.apache.shiro.event.Subscribe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@Lazy
public class EventBusSubscriberPostProcessor implements BeanPostProcessor {

  private final Logger log = LoggerFactory.getLogger(getClass());

  @Inject
  private EventBus eventBus;

  @Override
  public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
    return bean;
  }

  @Override
  public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
    // need the target class, not the Spring proxied one
    Class<?> targetClass = AopProxyUtils.ultimateTargetClass(bean);
    // for each method in the bean
    for(Method method : bean.getClass().getMethods()) {
      if(method.isAnnotationPresent(Subscribe.class)) {
        log.info("Register bean {} ({}) containing method {} to EventBus", beanName, bean.getClass().getName(),
          method.getName());
        // register it with the event bus
        eventBus.register(bean);
        return bean; // we only need to register once
      }
    }
    return bean;
  }
}
