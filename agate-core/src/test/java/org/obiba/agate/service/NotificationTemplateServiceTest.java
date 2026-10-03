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

import freemarker.template.Configuration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.obiba.agate.service.NotificationTemplateService.NotificationTemplate;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NotificationTemplateServiceTest {

  @TempDir
  Path home;

  private NotificationTemplateService service(List<String> locations) {
    return new NotificationTemplateService(new PathMatchingResourcePatternResolver(), locations,
      "classpath:/notification-templates/second/", home, new Configuration(Configuration.VERSION_2_3_34));
  }

  @Test
  public void testFoldersOfAllLocationsSortedWithoutDuplicates() {
    NotificationTemplateService service = service(List.of("classpath:/notification-templates/first/",
      "classpath:/notification-templates/second/", "classpath:/notification-templates/missing/"));

    // own.ftl, directly in notifications/, is an Agate email and not an application folder
    assertEquals(List.of("mica", "mica-a"), service.getFolders());
  }

  @Test
  public void testNoFolders() {
    assertTrue(service(List.of("classpath:/notification-templates/missing/")).getFolders().isEmpty());
  }

  @Test
  public void testWriteListReadRevert() throws Exception {
    NotificationTemplateService service = service(List.of());
    String bundled = service.read("mica", "two");

    service.write("mica", "two", "custom ${user}");
    service.write("mica", "other_fr", "autre");
    assertEquals(List.of(new NotificationTemplate("other_fr", false, true), new NotificationTemplate("two", true, true)),
      service.list("mica"));
    assertEquals("custom ${user}", service.read("mica", "two"));
    assertTrue(Files.exists(home.resolve("notifications/mica/two.ftl")));

    service.delete("mica", "two");
    assertEquals(bundled, service.read("mica", "two"));
    assertEquals(List.of(new NotificationTemplate("other_fr", false, true), new NotificationTemplate("two", true, false)),
      service.list("mica"));
    assertThrows(NoSuchElementException.class, () -> service.delete("mica", "two"));
    assertThrows(NoSuchElementException.class, () -> service.read("mica", "missing"));
  }

  @Test
  public void testInheritedFromFallback() throws Exception {
    NotificationTemplateService service = service(List.of());
    assertEquals(List.of(new NotificationTemplate("three", true, false), new NotificationTemplate("two", false, false, "mica")),
      service.list("mica-a", "mica"));
    assertEquals(service.read("mica", "two"), service.read("mica-a", "two", "mica"));

    // saving an inherited template makes it the application's own
    service.write("mica-a", "two", "own");
    assertEquals(List.of(new NotificationTemplate("three", true, false), new NotificationTemplate("two", false, true)),
      service.list("mica-a", "mica"));
    assertEquals("own", service.read("mica-a", "two", "mica"));
  }

  @Test
  public void testAgateOwnTemplates() throws Exception {
    NotificationTemplateService service = service(List.of());
    service.write(null, "confirmationEmail", "hello");
    assertEquals(List.of(new NotificationTemplate("confirmationEmail", false, true)), service.list(""));
    assertTrue(Files.exists(home.resolve("notifications/confirmationEmail.ftl")));
  }

  @Test
  public void testInvalidTemplateRejected() {
    NotificationTemplateService service = service(List.of());
    assertThrows(IllegalArgumentException.class, () -> service.write("mica", "two", "<#if>"));
    assertFalse(Files.exists(home.resolve("notifications/mica/two.ftl")));
  }

  @Test
  public void testPreviewSkipsFailingStatements() throws Exception {
    NotificationTemplateService service = service(List.of());
    // unknown is provided by the sender only: skipped, the rest is rendered
    assertEquals("Hello Bob, !", service.preview("x", "Hello ${user}, ${unknown.name}!", java.util.Map.of("user", "Bob")));
    assertThrows(IllegalArgumentException.class, () -> service.preview("x", "<#if>", java.util.Map.of()));
  }

  @Test
  public void testPathTraversalRejected() {
    NotificationTemplateService service = service(List.of());
    for (String folder : List.of("..", "../..", ".", "a/b", "/etc", "..\\x", "mica/../.."))
      assertThrows(IllegalArgumentException.class, () -> service.write(folder, "x", "x"), folder);
    for (String name : List.of("../x", "x.ftl", "a/b", "", ".."))
      assertThrows(IllegalArgumentException.class, () -> service.write("mica", name, "x"), name);
    assertThrows(IllegalArgumentException.class, () -> service.list(".."));
  }
}
