package org.obiba.agate.config.locale;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import org.obiba.agate.service.ConfigurationService;
import org.obiba.core.translator.JsonTranslator;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;

public class ExtendedResourceBundleMessageSource extends ReloadableResourceBundleMessageSource {

  private final ConfigurationService configurationService;

  private Map<String, JsonTranslator> translations = Maps.newConcurrentMap();

  public ExtendedResourceBundleMessageSource(ConfigurationService configurationService, int cacheSeconds) {
    this.configurationService = configurationService;
  }

  @Override
  protected String resolveCodeWithoutArguments(String code, Locale locale) {
    String result = super.resolveCodeWithoutArguments(code, locale);
    if (result != null) return result;

    final String key = locale.getLanguage();
    JsonTranslator translator = getTranslator(key);
    if (translator != null) {
      try {
        result = translator.translate(code);
      } catch (Exception e) {
        // ignore
      }
    }

    return result;
  }

  public void evict() {
    translations.clear();
  }

  private JsonTranslator getTranslator(String locale) {
    try {
      if (!translations.containsKey(locale)) {
        String tr = configurationService.getTranslations(locale, false).toString();
        if (!Strings.isNullOrEmpty(tr)) translations.put(locale, new JsonTranslator(tr));
      }
      return translations.get(locale);
    } catch (IOException e) {
      // ignore
      return null;
    }
  }

}
