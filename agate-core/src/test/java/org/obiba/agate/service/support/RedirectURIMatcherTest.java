package org.obiba.agate.service.support;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.obiba.agate.service.support.RedirectURIMatcher.matches;
import static org.obiba.agate.service.support.RedirectURIMatcher.matchesAny;

class RedirectURIMatcherTest {

  @Test
  void exactAndSubPathMatch() {
    assertTrue(matches("https://app.example.org/callback", "https://app.example.org/callback"));
    assertTrue(matches("https://app.example.org/callback", "https://app.example.org/callback/"));
    assertTrue(matches("https://app.example.org/callback", "https://app.example.org/callback/sub"));
    assertTrue(matches("https://app.example.org/callback/", "https://app.example.org/callback"));
    assertTrue(matches("https://app.example.org", "https://app.example.org/any/path"));
    assertTrue(matches("https://app.example.org", "https://app.example.org"));
  }

  @Test
  void queryStringIsAllowed() {
    assertTrue(matches("https://app.example.org/callback", "https://app.example.org/callback?state=abc"));
  }

  @Test
  void schemeHostAndPortAreCaseInsensitiveAndDefaulted() {
    assertTrue(matches("https://App.Example.org/cb", "HTTPS://app.example.ORG/cb"));
    assertTrue(matches("https://app.example.org:443/cb", "https://app.example.org/cb"));
    assertTrue(matches("http://app.example.org/cb", "http://app.example.org:80/cb"));
  }

  @Test
  void hostBoundaryIsEnforced() {
    assertFalse(matches("https://app.example.org", "https://app.example.org.attacker.test/"));
    assertFalse(matches("https://app.example.org", "https://app.example.org.attacker.test"));
    assertFalse(matches("https://app.example.org/callback", "https://app.example.orgx/callback"));
  }

  @Test
  void pathBoundaryIsEnforced() {
    assertFalse(matches("https://app.example.org/callback", "https://app.example.org/callback.attacker"));
    assertFalse(matches("https://app.example.org/callback", "https://app.example.org/callbackx"));
    assertFalse(matches("https://app.example.org/callback", "https://app.example.org/other"));
    assertFalse(matches("https://app.example.org/callback", "https://app.example.org/callback/../other"));
  }

  @Test
  void schemeAndPortMustMatch() {
    assertFalse(matches("https://app.example.org/cb", "http://app.example.org/cb"));
    assertFalse(matches("https://app.example.org/cb", "https://app.example.org:8443/cb"));
  }

  @Test
  void fragmentUserInfoAndMalformedAreRejected() {
    assertFalse(matches("https://app.example.org/cb", "https://app.example.org/cb#frag"));
    assertFalse(matches("https://app.example.org/cb", "https://app.example.org@attacker.test/cb"));
    assertFalse(matches("https://app.example.org/cb", "//app.example.org/cb"));
    assertFalse(matches("https://app.example.org/cb", "/cb"));
    assertFalse(matches("https://app.example.org/cb", "not a uri"));
    assertFalse(matches("https://app.example.org/cb", ""));
    assertFalse(matches("", "https://app.example.org/cb"));
    assertFalse(matches("https://app.example.org/cb", null));
  }

  @Test
  void matchesAnyOverRegisteredList() {
    List<String> registered = List.of("https://a.example.org/cb", "https://b.example.org");
    assertTrue(matchesAny(registered, "https://b.example.org/x"));
    assertFalse(matchesAny(registered, "https://c.example.org/x"));
    assertFalse(matchesAny(null, "https://a.example.org/cb"));
  }
}
