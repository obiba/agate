package org.obiba.agate.service;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.Issuer;
import com.nimbusds.openid.connect.sdk.Nonce;
import com.nimbusds.openid.connect.sdk.claims.IDTokenClaimsSet;
import com.nimbusds.openid.connect.sdk.validators.IDTokenValidator;
import com.google.common.collect.Sets;
import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.obiba.agate.domain.Authorization;
import org.obiba.agate.domain.User;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * The ID token is verified by the OIDC clients: check that what is signed can be validated with the public key that is
 * published by the JWKS endpoint, using the same validator as the clients (nimbus).
 */
class TokenUtilsTest {

  private static final String ISSUER = "https://agate.example.org";

  private static final String APPLICATION = "opal";

  @Mock
  private UserService userService;

  @Mock
  private AuthorizationService authorizationService;

  @Mock
  private ConfigurationService configurationService;

  @Mock
  private KeyStoreService keyStoreService;

  @InjectMocks
  private TokenUtils tokenUtils;

  @BeforeEach
  void setUp() throws Exception {
    MockitoAnnotations.openMocks(this);
    KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
    generator.initialize(2048);
    KeyPair keyPair = generator.generateKeyPair();
    when(keyStoreService.getOrCreateOIDCKeyPair()).thenReturn(keyPair);
    when(configurationService.getPublicUrl()).thenReturn(ISSUER);
  }

  @Test
  void testIDTokenIsValidatedWithThePublishedJwk() throws Exception {
    Authorization authorization = makeAuthorization("a-nonce");
    String idToken = tokenUtils.makeIDToken(authorization, List.of());

    IDTokenClaimsSet claims = validate(idToken, new Nonce("a-nonce"));

    assertEquals(ISSUER, claims.getIssuer().getValue());
    assertEquals("joe", claims.getSubject().getValue());
    assertEquals(List.of(new com.nimbusds.oauth2.sdk.id.Audience(APPLICATION)), claims.getAudience());
  }

  @Test
  void testIDTokenIsSignedWithRS256AndAdvertisedKeyId() throws Exception {
    String idToken = tokenUtils.makeIDToken(makeAuthorization(null), List.of());

    assertEquals(JWSAlgorithm.RS256, JWTParser.parse(idToken).getHeader().getAlgorithm());
    assertEquals("RS256", tokenUtils.getIDTokenSignatureAlgorithm());
    assertEquals(publishedJwkSet().getKeys().get(0).getKeyID(),
        JWTParser.parse(idToken).getHeader().toJSONObject().get("kid"));
  }

  @Test
  void testPublishedJwkHasNoPrivateKeyMaterial() throws Exception {
    assertFalse(publishedJwkSet().getKeys().get(0).isPrivate());
    assertEquals("sig", publishedJwkSet().getKeys().get(0).getKeyUse().identifier());
  }

  @Test
  void testIDTokenIsRejectedWhenNonceDoesNotMatch() {
    String idToken = tokenUtils.makeIDToken(makeAuthorization("a-nonce"), List.of());

    assertThrows(Exception.class, () -> validate(idToken, new Nonce("another-nonce")));
  }

  @Test
  void testNoIDTokenWithoutOpenIdScope() {
    Authorization authorization = new Authorization("joe", APPLICATION);
    authorization.setScopes(Sets.newHashSet("opal:read"));

    assertEquals("", tokenUtils.makeIDToken(authorization, List.of()));
  }

  private IDTokenClaimsSet validate(String idToken, Nonce nonce) throws Exception {
    IDTokenValidator validator = new IDTokenValidator(new Issuer(ISSUER), new ClientID(APPLICATION),
        JWSAlgorithm.RS256, publishedJwkSet());
    return validator.validate(JWTParser.parse(idToken), nonce);
  }

  private JWKSet publishedJwkSet() throws Exception {
    return JWKSet.parse("{\"keys\":[" + tokenUtils.getIDTokenPublicJwk() + "]}");
  }

  private Authorization makeAuthorization(String nonce) {
    Authorization authorization = new Authorization("joe", APPLICATION);
    authorization.setScopes(Sets.newHashSet(TokenUtils.OPENID_SCOPE));
    authorization.setCreatedDate(DateTime.now());
    authorization.setNonce(nonce);
    when(userService.findUser("joe")).thenReturn(new User("joe", "agate-user-realm"));
    when(authorizationService.getExpirationDate(authorization)).thenReturn(DateTime.now().plusHours(1));
    return authorization;
  }
}
