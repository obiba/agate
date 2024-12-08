package org.obiba.agate.service;

import dev.samstevens.totp.code.*;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import dev.samstevens.totp.util.Utils;
import org.springframework.stereotype.Component;

import jakarta.inject.Inject;
import java.util.Random;

@Component
public class TotpService {

  private static final int DEFAULT_PERIOD = 30;

  private static final int DEFAULT_DIGITS = 6;

  private static final int DEFAULT_SECRET_LENGTH = 64;

  private static final String DEFAULT_HASH_ALGORITHM = "SHA1";

  private final ConfigurationService configurationService;

  @Inject
  public TotpService(ConfigurationService configurationService) {
    this.configurationService = configurationService;
  }

  public String generateSecret() {
    // 32 chars long secret
    SecretGenerator generator = new DefaultSecretGenerator(DEFAULT_SECRET_LENGTH);
    return configurationService.encrypt(generator.generate());
  }

  public String getQrImageDataUri(String label, String secret) {
    String serverName = configurationService.getConfiguration().getName();
    QrData data = new QrData.Builder()
        .label(label)
        .secret(configurationService.decrypt(secret))
        .issuer(serverName)
        .algorithm(HashingAlgorithm.valueOf(DEFAULT_HASH_ALGORITHM))
        .digits(DEFAULT_DIGITS)
        .period(DEFAULT_PERIOD)
        .build();

    String dataUri;
    try {
      QrGenerator generator = new ZxingPngQrGenerator();
      byte[] imageData = generator.generate(data);
      String mimeType = generator.getImageMimeType();
      dataUri = Utils.getDataUriForImage(imageData, mimeType);
    } catch (QrGenerationException e) {
      throw new RuntimeException(e);
    }

    return dataUri;
  }

  public boolean validateCode(String code, String secret) {
    TimeProvider timeProvider = new SystemTimeProvider();
    CodeGenerator codeGenerator = new DefaultCodeGenerator(HashingAlgorithm.valueOf(DEFAULT_HASH_ALGORITHM), DEFAULT_DIGITS);
    CodeVerifier verifier = new DefaultCodeVerifier(codeGenerator, timeProvider);
    return verifier.isValidCode(configurationService.decrypt(secret), code);
  }

  public String generateRandomCode() {
    Random random = new Random();
    // Generates a random integer between 100000 and 999999
    int code = random.nextInt(900000) + 100000;
    return String.valueOf(code);
  }
}
