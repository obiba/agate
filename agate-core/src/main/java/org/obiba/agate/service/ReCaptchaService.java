/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.agate.service;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertyResolver;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;

@Component
public class ReCaptchaService {

  private static final Logger log = LoggerFactory.getLogger(ReCaptchaService.class);

  @Inject
  private RestTemplate restTemplate;

  @Inject
  private Environment env;

  public boolean verify(String reCaptchaResponse) {
    PropertyResolver propertyResolver = new RelaxedPropertyResolver(env, "recaptcha.");

    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("secret", propertyResolver.getProperty("secret"));
    map.add("response", reCaptchaResponse);

    ReCaptchaVerifyResponse recaptchaVerifyResponse = restTemplate
      .postForObject(propertyResolver.getProperty("verifyUrl"), map, ReCaptchaVerifyResponse.class);

    if(!recaptchaVerifyResponse.isSuccess() && (recaptchaVerifyResponse.getErrorCodes().contains("invalid-input-secret") ||
      recaptchaVerifyResponse.getErrorCodes().contains("missing-input-secret"))) {
      log.error("Error verifying recaptcha: " + reCaptchaResponse);
      throw new RuntimeException("Error verifying recaptcha.");
    }

    return recaptchaVerifyResponse.isSuccess();
  }

  private static class ReCaptchaVerifyResponse {
    private boolean success;
    private List<String> errorCodes = Lists.newArrayList();

    public ReCaptchaVerifyResponse() {
    }

    public boolean isSuccess() {
      return success;
    }

    public void setSuccess(boolean success) {
      this.success = success;
    }

    public List<String> getErrorCodes() {
      return errorCodes;
    }

    @JsonSetter("error-codes")
    public void setErrorCodes(List<String> errorCodes) {
      if (errorCodes != null) this.errorCodes = errorCodes;
    }

    @Override
    public String toString() {
      return Objects.toStringHelper(this).add("success", success).add("errorCodes", errorCodes).toString();
    }
  }
}
