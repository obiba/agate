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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

  /**
   * The FreeMarker template locations holding notification templates, in order of precedence: the ones of the Agate
   * home ({@code AGATE_HOME/conf} comes first in the classpath) and the bundled ones ({@code WEB-INF/classes}). Plain
   * {@code classpath:} (not {@code classpath*:}): only the first classpath root is scanned, so that no jar can add a
   * folder.
   */
  private static final List<String> LOCATIONS = List.of("classpath:/templates/", "classpath:/_templates/");

  private final ResourcePatternResolver resolver;

  private final List<String> locations;

  public NotificationTemplateService() {
    this(new PathMatchingResourcePatternResolver(), LOCATIONS);
  }

  NotificationTemplateService(ResourcePatternResolver resolver, List<String> locations) {
    this.resolver = resolver;
    this.locations = locations;
  }

  /**
   * Names of the {@code notifications/<name>/} folders holding at least one template, in any of the template
   * locations (bundled templates and the ones of the Agate home), sorted and without duplicates.
   */
  public List<String> getFolders() {
    Set<String> folders = new TreeSet<>();
    for (String location : locations) {
      String pattern = location + NOTIFICATIONS_DIR + "*/*.ftl";
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

  private static String folderOf(Resource resource) throws IOException {
    String url = resource.getURL().toString();
    int start = url.lastIndexOf("/" + NOTIFICATIONS_DIR);
    if (start < 0) return null;
    String rest = url.substring(start + NOTIFICATIONS_DIR.length() + 1);
    int end = rest.indexOf('/');
    return end > 0 ? URLDecoder.decode(rest.substring(0, end), StandardCharsets.UTF_8) : null;
  }
}
