/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.agate.service;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.DefaultRedirectStrategy;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.inject.Inject;
import java.util.List;

@Component
public class ReCaptchaService {

  private static final Logger log = LoggerFactory.getLogger(ReCaptchaService.class);

  @Inject
  private Environment env;

  public boolean verify(String reCaptchaResponse) {

    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("secret", env.getProperty("recaptcha.secret"));
    map.add("response", reCaptchaResponse);

    // #495 http client that supports redirect on POST
    HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
    HttpClient httpClient = HttpClientBuilder.create()
        .setRedirectStrategy(new DefaultRedirectStrategy())
        .build();
    factory.setHttpClient(httpClient);

    RestTemplate restTemplate = new RestTemplate();
    restTemplate.setRequestFactory(factory);

    ReCaptchaVerifyResponse recaptchaVerifyResponse = restTemplate
        .postForObject(env.getProperty("recaptcha.verifyUrl"), map, ReCaptchaVerifyResponse.class);

    if (recaptchaVerifyResponse == null || !recaptchaVerifyResponse.isSuccess() && (recaptchaVerifyResponse.getErrorCodes().contains("invalid-input-secret") ||
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
      return MoreObjects.toStringHelper(this).add("success", success).add("errorCodes", errorCodes).toString();
    }
  }
}
