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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import jakarta.inject.Inject;
import java.security.SecureRandom;
import java.time.Duration;

@Component
public class TotpService {

  private static final int DEFAULT_PERIOD = 30;

  private static final int DEFAULT_DIGITS = 6;

  private static final int DEFAULT_SECRET_LENGTH = 64;

  private static final String DEFAULT_HASH_ALGORITHM = "SHA1";

  private final ConfigurationService configurationService;

  // codes already accepted, kept longer than the verifier's window (previous, current and next period) so each is single-use
  // ponytail: in-memory, per node; a shared store is needed if several Agate instances serve logins
  private final Cache<String, Boolean> usedCodes = CacheBuilder.newBuilder()
      .expireAfterWrite(Duration.ofSeconds(DEFAULT_PERIOD * 4L))
      .build();

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
    return verifier.isValidCode(configurationService.decrypt(secret), code)
        && usedCodes.asMap().putIfAbsent(secret + ":" + code, Boolean.TRUE) == null;
  }

  public String generateRandomCode() {
    SecureRandom random = new SecureRandom();
    // Generates a random integer between 100000 and 999999
    int code = random.nextInt(900000) + 100000;
    return String.valueOf(code);
  }
}
