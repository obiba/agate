/*
 * Copyright (c) 2026 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.agate.service;

import com.google.common.base.Splitter;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Discovers the application notification template folders ({@code notifications/<name>/}) available to FreeMarker.
 */
@Component
public class NotificationTemplateService {

  private static final Logger log = LoggerFactory.getLogger(NotificationTemplateService.class);

  private static final String NOTIFICATIONS_DIR = "notifications/";

  private final ResourcePatternResolver resolver;

  private final List<String> loaderPaths;

  @Inject
  public NotificationTemplateService(
    @Value("${spring.freemarker.template-loader-path:classpath:/templates/}") String loaderPaths) {
    this(new PathMatchingResourcePatternResolver(), loaderPaths);
  }

  NotificationTemplateService(ResourcePatternResolver resolver, String loaderPaths) {
    this.resolver = resolver;
    this.loaderPaths = Splitter.on(",").trimResults().omitEmptyStrings().splitToList(loaderPaths);
  }

  /**
   * Names of the {@code notifications/<name>/} folders holding at least one template, in any of the FreeMarker
   * template locations (bundled templates and the ones of the Agate home), sorted and without duplicates.
   *
   * @return
   */
  public List<String> getFolders() {
    Set<String> folders = new TreeSet<>();
    for (String path : loaderPaths) {
      String pattern = asAllClasspathPattern(path) + NOTIFICATIONS_DIR + "*/*.ftl";
      try {
        for (Resource resource : resolver.getResources(pattern)) {
          String folder = folderOf(resource);
          if (folder != null) folders.add(folder);
        }
      } catch (IOException e) {
        log.debug("Cannot list notification templates with pattern {}", pattern, e);
      }
    }
    return new ArrayList<>(folders);
  }

  private static String asAllClasspathPattern(String path) {
    String base = path.endsWith("/") ? path : path + "/";
    return base.startsWith("classpath:") ? "classpath*:" + base.substring("classpath:".length()) : base;
  }

  private static String folderOf(Resource resource) throws IOException {
    String url = resource.getURL().toString();
    int start = url.lastIndexOf("/" + NOTIFICATIONS_DIR);
    if (start < 0) return null;
    String rest = url.substring(start + NOTIFICATIONS_DIR.length() + 1);
    int end = rest.indexOf('/');
    return end > 0 ? URLDecoder.decode(rest.substring(0, end), StandardCharsets.UTF_8) : null;
  }
}
