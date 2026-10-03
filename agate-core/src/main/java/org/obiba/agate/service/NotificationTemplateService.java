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

import freemarker.core.Environment;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Discovers the application notification template folders ({@code notifications/<name>/}) available to FreeMarker,
 * and manages the notification templates of the Agate home ({@code AGATE_HOME/conf/templates/notifications/}), which
 * take precedence over the bundled ones.
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

  private static final String BUNDLED_LOCATION = "classpath:/_templates/";

  private static final Pattern NAME_PATTERN = Pattern.compile("[A-Za-z0-9_-]+");

  // application names may contain spaces
  private static final Pattern FOLDER_PATTERN = Pattern.compile("[A-Za-z0-9 _.-]+");

  private static final String EXT = ".ftl";

  /**
   * A notification template: {@code bundled} when shipped with Agate, {@code custom} when present in the Agate home
   * (both: the custom one overrides the bundled one), or {@code inheritedFrom} the fallback folder when only there.
   */
  public record NotificationTemplate(String name, boolean bundled, boolean custom, String inheritedFrom) {

    NotificationTemplate(String name, boolean bundled, boolean custom) {
      this(name, bundled, custom, null);
    }
  }

  private final ResourcePatternResolver resolver;

  private final List<String> locations;

  private final String bundledLocation;

  private final Path homeNotifications;

  private final Configuration freemarkerConfiguration;

  @Inject
  public NotificationTemplateService(Configuration freemarkerConfiguration) {
    this(new PathMatchingResourcePatternResolver(), LOCATIONS, BUNDLED_LOCATION,
      Path.of(System.getProperty("AGATE_HOME", "."), "conf", "templates"), freemarkerConfiguration);
  }

  NotificationTemplateService(ResourcePatternResolver resolver, List<String> locations, String bundledLocation,
                              Path homeTemplates, Configuration freemarkerConfiguration) {
    this.resolver = resolver;
    this.locations = locations;
    this.bundledLocation = bundledLocation;
    this.homeNotifications = homeTemplates.resolve(NOTIFICATIONS_DIR).toAbsolutePath().normalize();
    this.freemarkerConfiguration = freemarkerConfiguration;
  }

  /**
   * Names of the {@code notifications/<name>/} folders holding at least one template, in any of the template
   * locations (bundled templates and the ones of the Agate home), sorted and without duplicates.
   */
  public List<String> getFolders() {
    Set<String> folders = new TreeSet<>();
    for (String location : locations) {
      String pattern = location + NOTIFICATIONS_DIR + "*/*" + EXT;
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

  /**
   * Templates of a folder, bundled and custom ones merged, sorted by name.
   *
   * @param folder application folder, null or empty for Agate's own templates
   */
  public List<NotificationTemplate> list(String folder) {
    Set<String> bundled = new TreeSet<>();
    String pattern = bundledLocation + NOTIFICATIONS_DIR + folderPrefix(folder) + "*" + EXT;
    try {
      for (Resource resource : resolver.getResources(pattern)) {
        if (resource.exists()) bundled.add(stripExt(resource.getFilename()));
      }
    } catch (IOException e) {
      log.debug("Cannot list notification templates with pattern {}", pattern, e);
    }

    Set<String> custom = new TreeSet<>();
    Path dir = resolveDir(folder);
    if (Files.isDirectory(dir)) {
      try (Stream<Path> files = Files.list(dir)) {
        files.filter(Files::isRegularFile)
          .map(p -> p.getFileName().toString())
          .filter(n -> n.endsWith(EXT))
          .forEach(n -> custom.add(stripExt(n)));
      } catch (IOException e) {
        log.warn("Cannot list notification templates in {}", dir, e);
      }
    }

    TreeMap<String, NotificationTemplate> templates = new TreeMap<>();
    bundled.forEach(n -> templates.put(n, new NotificationTemplate(n, true, custom.contains(n))));
    custom.forEach(n -> templates.putIfAbsent(n, new NotificationTemplate(n, false, true)));
    return new ArrayList<>(templates.values());
  }

  /**
   * Templates of a folder, plus the ones of its fallback folder that it does not have, see how notification emails
   * are sent on behalf of an application.
   *
   * @param fallback fallback folder, ignored when null or empty
   */
  public List<NotificationTemplate> list(String folder, String fallback) {
    List<NotificationTemplate> templates = list(folder);
    if (fallback == null || fallback.isEmpty() || fallback.equals(folder)) return templates;
    Set<String> names = new TreeSet<>();
    templates.forEach(t -> names.add(t.name()));
    list(fallback).stream()
      .filter(t -> !names.contains(t.name()))
      .forEach(t -> templates.add(new NotificationTemplate(t.name(), false, false, fallback)));
    templates.sort(Comparator.comparing(NotificationTemplate::name));
    return templates;
  }

  /**
   * Content of a template: the custom one if any, else the bundled one.
   *
   * @throws NoSuchElementException when there is no such template
   */
  public String read(String folder, String name) throws IOException {
    return read(folder, name, null);
  }

  /**
   * Content of a template of a folder, else of its fallback folder.
   *
   * @throws NoSuchElementException when there is no such template
   */
  public String read(String folder, String name, String fallback) throws IOException {
    String content = readOrNull(folder, name);
    if (content == null && fallback != null && !fallback.isEmpty()) content = readOrNull(fallback, name);
    if (content == null) throw new NoSuchElementException("Notification template not found: " + name);
    return content;
  }

  private String readOrNull(String folder, String name) throws IOException {
    Path file = resolveFile(folder, name);
    if (Files.isRegularFile(file)) return Files.readString(file, StandardCharsets.UTF_8);
    Resource resource = resolver.getResource(bundledLocation + NOTIFICATIONS_DIR + folderPrefix(folder) + name + EXT);
    return resource.exists() ? resource.getContentAsString(StandardCharsets.UTF_8) : null;
  }

  /**
   * Save a custom template in the Agate home, after having verified it is a valid FreeMarker template.
   *
   * @throws IllegalArgumentException when the name is not valid or the template does not parse
   */
  public void write(String folder, String name, String content) throws IOException {
    Path file = resolveFile(folder, name);
    try {
      new Template(name + EXT, new StringReader(content == null ? "" : content), freemarkerConfiguration);
    } catch (IOException e) {
      throw new IllegalArgumentException(e.getMessage(), e);
    }
    Files.createDirectories(file.getParent());
    Files.writeString(file, content == null ? "" : content, StandardCharsets.UTF_8);
    freemarkerConfiguration.clearTemplateCache();
  }

  /**
   * Render a template content for preview: the statements that fail (typically on variables only the sender
   * provides) produce no output instead of failing the whole rendering.
   *
   * @throws IllegalArgumentException when the template does not parse
   */
  public String preview(String name, String content, Map<String, Object> model) throws IOException {
    Template template;
    try {
      template = new Template(name + EXT, new StringReader(content == null ? "" : content), freemarkerConfiguration);
    } catch (IOException e) {
      throw new IllegalArgumentException(e.getMessage(), e);
    }
    StringWriter out = new StringWriter();
    try {
      Environment env = template.createProcessingEnvironment(model, out);
      env.setTemplateExceptionHandler(TemplateExceptionHandler.IGNORE_HANDLER);
      env.setLogTemplateExceptions(false);
      env.process();
    } catch (TemplateException e) {
      throw new IllegalArgumentException(e.getMessage(), e);
    }
    return out.toString();
  }

  /**
   * Delete a custom template from the Agate home, the bundled one (if any) applies again.
   *
   * @throws NoSuchElementException when there is no such custom template
   */
  public void delete(String folder, String name) throws IOException {
    Path file = resolveFile(folder, name);
    if (!Files.deleteIfExists(file)) throw new NoSuchElementException("Custom notification template not found: " + name);
    freemarkerConfiguration.clearTemplateCache();
  }

  private Path resolveDir(String folder) {
    if (folder == null || folder.isEmpty()) return homeNotifications;
    if (!FOLDER_PATTERN.matcher(folder).matches() || folder.startsWith("."))
      throw new IllegalArgumentException("Invalid notification templates folder: " + folder);
    Path dir = homeNotifications.resolve(folder).normalize();
    if (!dir.getParent().equals(homeNotifications))
      throw new IllegalArgumentException("Invalid notification templates folder: " + folder);
    return dir;
  }

  private Path resolveFile(String folder, String name) {
    if (name == null || !NAME_PATTERN.matcher(name).matches())
      throw new IllegalArgumentException("Invalid notification template name: " + name);
    return resolveDir(folder).resolve(name + EXT);
  }

  private String folderPrefix(String folder) {
    resolveDir(folder); // validation
    return folder == null || folder.isEmpty() ? "" : folder + "/";
  }

  private static String stripExt(String filename) {
    return filename.substring(0, filename.length() - EXT.length());
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
