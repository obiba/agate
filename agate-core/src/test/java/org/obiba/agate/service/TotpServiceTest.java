package org.obiba.agate.service;

import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TotpServiceTest {

  @Test
  void codeIsSingleUse() throws Exception {
    String secret = new DefaultSecretGenerator(64).generate();
    ConfigurationService configurationService = mock(ConfigurationService.class);
    when(configurationService.decrypt("encrypted")).thenReturn(secret);
    TotpService totpService = new TotpService(configurationService);

    String code = new DefaultCodeGenerator().generate(secret, System.currentTimeMillis() / 1000 / 30);

    assertTrue(totpService.validateCode(code, "encrypted"));
    assertFalse(totpService.validateCode(code, "encrypted"));
  }
}
