/**
 *       Copyright 2010 Newcastle University
 *
 *          http://research.ncl.ac.uk/smart/
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.obiba.agate.web.oauth2.as.validator;

import jakarta.servlet.http.HttpServletRequest;

import org.obiba.agate.web.oauth2.common.OAuth;
import org.obiba.agate.web.oauth2.common.validators.AbstractValidator;

/**
 * Validator that checks for the required fields in an OAuth Token request with the Authorization Code grant type.
 * This validator enforces client authentication either through basic authentication or body parameters.
 */
public class AuthorizationCodeValidator extends AbstractValidator<HttpServletRequest> {

    public AuthorizationCodeValidator() {
        requiredParams.add(OAuth.OAUTH_GRANT_TYPE);
        requiredParams.add(OAuth.OAUTH_CODE);
        requiredParams.add(OAuth.OAUTH_REDIRECT_URI);

        enforceClientAuthentication = true;
    }

}