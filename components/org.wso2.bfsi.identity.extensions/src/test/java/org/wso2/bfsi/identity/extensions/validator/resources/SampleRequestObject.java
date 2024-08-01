/**
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.bfsi.identity.extensions.validator.resources;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.wso2.bfsi.identity.extensions.validator.annotation.ValidScopeFormat;
import org.wso2.carbon.identity.oauth2.RequestObjectException;

/**
 * Sample request object resource.
 */
@ValidScopeFormat(scope = "claimsSet.claims.scope", message = "Non Confirming Scope")
public class SampleRequestObject {

    private JWTClaimsSet claimsSet;
    private SignedJWT signedJWT;

    public SampleRequestObject() {
    }

    public JWTClaimsSet getClaimsSet() {
        return claimsSet;
    }

    public void setClaimSet(JWTClaimsSet claimsSet) {
        this.claimsSet = claimsSet;
    }

    public void setClaimsSet(JWTClaimsSet claimsSet) {
        this.claimsSet = claimsSet;
    }

    public void setSignedJWT(SignedJWT signedJWT) throws RequestObjectException {
        this.signedJWT = signedJWT;
    }

    public SignedJWT getSignedJWT() {
        return signedJWT;
    }
}
