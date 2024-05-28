/**
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 * <p>
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 *     http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.bfsi.consent.management.extensions.admin.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.bfsi.consent.management.extensions.admin.ConsentAdminHandler;
import org.wso2.bfsi.consent.management.extensions.admin.model.ConsentAdminData;
import org.wso2.bfsi.consent.management.extensions.common.ConsentException;

/**
 * Consent admin handler default implementation.
 */
public class DefaultConsentAdminHandler implements ConsentAdminHandler {
    private static final Log log = LogFactory.getLog(DefaultConsentAdminHandler.class);

    @Override
    public void handleSearch(ConsentAdminData consentAdminData) throws ConsentException {

        return;
    }

    @Override
    public void handleRevoke(ConsentAdminData consentAdminData) throws ConsentException {

        return;
    }

    public void handleConsentAmendmentHistoryRetrieval(ConsentAdminData consentAdminData) throws ConsentException {

        return;
    }

    @Override
    public void handleConsentStatusAuditSearch(ConsentAdminData consentAdminData) throws ConsentException {

        return;
    }

    @Override
    public void handleConsentFileSearch(ConsentAdminData consentAdminData) throws ConsentException {

        return;
    }
}
