/*
 * Copyright (c) 2026 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.agate.web;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import freemarker.cache.ClassTemplateLoader;
import freemarker.core.HTMLOutputFormat;
import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.utility.StringUtil;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Translations can hold quotes and markup (custom translations are edited by administrators): written in a script or
 * an attribute, they must be printed with messageJs or messageAttr, not with the message macro which prints them as is.
 */
public class TemplatesTranslationsTest {

  private static final String PAYLOAD = "<b>'\"&";

  // a JavaScript string literal written by the templates
  private static final String JS_STRING = "\"((?:[^\"\\\\]|\\\\.)*)\"";

  private static final Pattern PLACEHOLDER = Pattern.compile("placeholder=\"([^\"]*)\"");

  private static final Pattern REQUIRED_FIELD_TITLE = Pattern.compile("title: " + JS_STRING);

  private static final Pattern ERROR_MESSAGE = Pattern.compile("'[\\w.-]+': " + JS_STRING);

  private static final Pattern SCRIPT_BLOCK = Pattern.compile("<script\\b[^>]*>(.*?)</script\\s*>",
    Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

  // an attribute value of the tag being written that holds a message macro call
  private static final Pattern MESSAGE_IN_ATTRIBUTE = Pattern.compile("\\s[\\w:.-]+\\s*=\\s*([\"'])[^\"'<>]*<@message\\b");

  /**
   * Translation of every code: the code followed by a quote and markup.
   */
  public static class HostileRequestContext {
    public String getMessage(String code, String defaultMessage) {
      return translation(code);
    }
  }

  @Test
  public void test_signup_translations_in_attributes_and_scripts_are_escaped() throws Exception {
    String output = render("signup.ftl");

    // placeholders: HTML-escaped, the attribute is not closed by the quote of the translation
    assertThat(groups(PLACEHOLDER, output)).containsExactly(
      attr("username"), attr("email"), attr("firstname"), attr("lastname"), attr("hostile"));

    // required fields of the form validation: a JavaScript string each, the quote of the translation does not end it
    assertThat(groups(REQUIRED_FIELD_TITLE, output)).containsExactly(
      js("email"), js("firstname"), js("lastname"), js("username"), js("hostile"), js("captcha"));

    // error messages of signup-scripts.ftl
    assertThat(groups(ERROR_MESSAGE, output)).containsExactly(
      js("server.error.bad-request"), js("server.error.bad-captcha"), js("server.error.email-already-assigned"),
      js("server.error.unhandledException"));
    assertThat(output).contains("msg = \"" + js("sign-up-fields-required") + ": \"");
  }

  @Test
  public void test_no_message_macro_in_scripts_or_attributes() throws Exception {
    List<String> found = new ArrayList<>();
    Path root = templatesRoot();
    try (Stream<Path> files = Files.walk(root)) {
      for (Path file : files.filter(f -> f.toString().endsWith(".ftl")).sorted().toList()) {
        String name = root.relativize(file).toString();
        String text = Files.readString(file, StandardCharsets.UTF_8);
        Matcher script = SCRIPT_BLOCK.matcher(text);
        while (script.find()) {
          if (script.group(1).contains("<@message")) found.add(name + ": in a script");
        }
        Matcher attribute = MESSAGE_IN_ATTRIBUTE.matcher(text);
        while (attribute.find()) {
          found.add(name + ": in an attribute, " + attribute.group().trim());
        }
      }
    }
    assertThat(found).as("translations printed with the message macro, use messageJs or messageAttr").isEmpty();
  }

  //
  // Private methods
  //

  private static String translation(String code) {
    return code + PAYLOAD;
  }

  private static String attr(String code) {
    return StringUtil.XHTMLEnc(translation(code));
  }

  // in a script, the JavaScript string is HTML-escaped too, as the pages display it as HTML
  private static String js(String code) {
    return StringUtil.XHTMLEnc(StringUtil.javaScriptStringEnc(translation(code)));
  }

  private static List<String> groups(Pattern pattern, String text) {
    List<String> values = new ArrayList<>();
    Matcher matcher = pattern.matcher(text);
    while (matcher.find()) values.add(matcher.group(1));
    return values;
  }

  private static String render(String page) throws Exception {
    Configuration configuration = new Configuration(Configuration.VERSION_2_3_32);
    configuration.setTemplateLoader(new ClassTemplateLoader(TemplatesTranslationsTest.class, "/_templates/"));
    configuration.setDefaultEncoding("UTF-8");
    configuration.setOutputFormat(HTMLOutputFormat.INSTANCE);
    configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    configuration.setLocale(Locale.ENGLISH);

    Map<String, Object> model = Map.of(
      "springMacroRequestContext", new HostileRequestContext(),
      "config", Map.of("contextPath", "", "name", "Agate"),
      "authConfig", Map.of(
        "reCaptchaEnabled", true,
        "reCaptchaKey", "key",
        "joinWithUsername", true,
        "languages", List.of("en"),
        "userAttributes", List.of(Map.of("name", "hostile", "inputType", "text", "values", List.of(), "required", true))));

    StringWriter writer = new StringWriter();
    configuration.getTemplate(page).process(model, writer);
    return writer.toString();
  }

  private static Path templatesRoot() throws URISyntaxException, IOException {
    return Path.of(TemplatesTranslationsTest.class.getResource("/_templates/libs/head.ftl").toURI()).getParent().getParent();
  }

}
