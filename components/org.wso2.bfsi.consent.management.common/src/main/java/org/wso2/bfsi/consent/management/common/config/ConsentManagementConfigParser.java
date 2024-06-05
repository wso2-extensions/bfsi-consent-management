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

package org.wso2.bfsi.consent.management.common.config;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.bfsi.consent.management.common.exceptions.ConsentManagementRuntimeException;
import org.wso2.bfsi.consent.management.common.util.CarbonUtils;
import org.wso2.bfsi.consent.management.common.util.ConsentManagementConstants;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Stack;
import java.util.stream.Collectors;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import static java.util.Map.Entry.comparingByKey;

/**
 * Config parser for bfsi-consent-management.xml.
 */
public class ConsentManagementConfigParser {

    private static final Log log = LogFactory.getLog(ConsentManagementConfigParser.class);
    // To enable attempted thread-safety using double-check locking
    private static final Object lock = new Object();
    private static volatile ConsentManagementConfigParser parser;
    private static final Map<String, Object> configuration = new HashMap<>();
    private static final Map<String, Map<Integer, String>> authorizeSteps = new HashMap<>();

    /**
     * Private Constructor of config parser.
     */
    private ConsentManagementConfigParser() {

        buildConfiguration();
    }

    /**
     * Singleton getInstance method to create only one object.
     *
     * @return ConsentManagementConfigParser object
     */
    public static ConsentManagementConfigParser getInstance() {

        if (parser == null) {
            synchronized (lock) {
                if (parser == null) {
                    parser = new ConsentManagementConfigParser();
                }
            }
        }
        return parser;
    }

    /**
     * Method to read the configuration (in a recursive manner) as a model and put them in the configuration map.
     */
    @SuppressFBWarnings("PATH_TRAVERSAL_IN")
    // Suppressed content - new File(CarbonUtils.getCarbonConfigDirPath(), ConsentManagementConstants.CONFIG_FILE);
    // Suppression reason - False Positive : File path is constructed using CarbonUtils.getCarbonConfigDirPath()
    // Suppressed warning count - 1
    private void buildConfiguration() {

        StAXOMBuilder builder;
        File configXml = new File(CarbonUtils.getCarbonConfigDirPath(), ConsentManagementConstants.CONFIG_FILE);
        if (configXml.exists()) {
            try (FileInputStream fileInputStream = new FileInputStream(configXml)) {
                builder = new StAXOMBuilder(fileInputStream);
                OMElement rootElement = builder.getDocumentElement();
                Stack<String> nameStack = new Stack<>();
                readChildElements(rootElement, nameStack);
                buildConsentAuthSteps(rootElement);
            } catch (IOException | XMLStreamException | OMException e) {
                throw new ConsentManagementRuntimeException("Error occurred while building configuration from" +
                        " bfsi-consent-management.xml", e);
            }
        } else {
            throw new ConsentManagementRuntimeException("bfsi-consent-management.xml file not found in " +
                    CarbonUtils.getCarbonConfigDirPath());
        }
    }

    /**
     * Method to read text configs from xml when root element is given.
     *
     * @param serverConfig XML root element object
     * @param nameStack    stack of config names
     */
    private void readChildElements(OMElement serverConfig, Stack<String> nameStack) {

        for (Iterator childElements = serverConfig.getChildElements(); childElements.hasNext(); ) {
            OMElement element = (OMElement) childElements.next();
            nameStack.push(element.getLocalName());
            if (elementHasText(element)) {
                String key = getKey(nameStack);
                Object currentObject = configuration.get(key);
                String value = replaceSystemProperty(element.getText());
                if (currentObject == null) {
                    configuration.put(key, value);
                } else if (currentObject instanceof ArrayList) {
                    ArrayList<String> list = (ArrayList) currentObject;
                    if (!list.contains(value)) {
                        list.add(value);
                        configuration.put(key, list);
                    }
                } else {
                    if (!value.equals(currentObject)) {
                        ArrayList<Object> arrayList = new ArrayList<>(2);
                        arrayList.add(currentObject);
                        arrayList.add(value);
                        configuration.put(key, arrayList);
                    }
                }
            }
            readChildElements(element, nameStack);
            nameStack.pop();
        }
    }

    /**
     * Method to check whether config element has text value.
     *
     * @param element root element as a object
     * @return availability of text in the config
     */
    private boolean elementHasText(OMElement element) {

        return StringUtils.isNotBlank(element.getText());
    }

    /**
     * Method to obtain config key from stack.
     *
     * @param nameStack Stack of strings with names.
     * @return key as a String
     */
    private String getKey(Stack<String> nameStack) {

        return nameStack.stream().collect(Collectors.joining("."));
    }

    /**
     * Method to replace system properties in configs.
     *
     * @param text String that may require modification
     * @return modified string
     */
    private String replaceSystemProperty(String text) {

        int indexOfStartingChars = -1;
        int indexOfClosingBrace;

        // The following condition deals with properties.
        // Properties are specified as ${system.property},
        // and are assumed to be System properties
        StringBuilder textBuilder = new StringBuilder(text);
        while (indexOfStartingChars < textBuilder.indexOf("${")
                && (indexOfStartingChars = textBuilder.indexOf("${")) != -1
                && (indexOfClosingBrace = textBuilder.indexOf("}")) != -1) { // Is a property used?
            String sysProp = textBuilder.substring(indexOfStartingChars + 2, indexOfClosingBrace);
            String propValue = System.getProperty(sysProp);
            if (propValue != null) {
                textBuilder = new StringBuilder(textBuilder.substring(0, indexOfStartingChars) + propValue
                        + textBuilder.substring(indexOfClosingBrace + 1));
            }
            if (sysProp.equals(ConsentManagementConstants.CARBON_HOME) &&
                    System.getProperty(ConsentManagementConstants.CARBON_HOME).equals(".")) {
                textBuilder.insert(0, new File(".").getAbsolutePath() + File.separator);
            }
        }
        return textBuilder.toString();
    }

    private void buildConsentAuthSteps(OMElement rootElement) {

        OMElement consentElement = rootElement.getFirstChildWithName(
                new QName(ConsentManagementConstants.OB_CONFIG_QNAME,
                        ConsentManagementConstants.CONSENT_CONFIG_TAG));

        if (consentElement != null) {
            OMElement consentAuthorizeSteps = consentElement.getFirstChildWithName(
                    new QName(ConsentManagementConstants.OB_CONFIG_QNAME,
                            ConsentManagementConstants.AUTHORIZE_STEPS_CONFIG_TAG));

            if (consentAuthorizeSteps != null) {
                //obtaining each step type element under AuthorizeSteps tag
                Iterator stepTypeElement = consentAuthorizeSteps.getChildElements();
                while (stepTypeElement.hasNext()) {
                    OMElement stepType = (OMElement) stepTypeElement.next();
                    String consentTypeName = stepType.getLocalName();
                    Map<Integer, String> executors = new HashMap<>();
                    //obtaining each step under each consent type
                    Iterator<OMElement> obExecutor = stepType.getChildrenWithName(
                            new QName(ConsentManagementConstants.OB_CONFIG_QNAME,
                                    ConsentManagementConstants.STEP_CONFIG_TAG));
                    if (obExecutor != null) {
                        while (obExecutor.hasNext()) {
                            OMElement executorElement = obExecutor.next();
                            //Retrieve class name and priority from executor config
                            String obExecutorClass = executorElement.getAttributeValue(new QName("class"));
                            String obExecutorPriority = executorElement.getAttributeValue(new QName("priority"));

                            if (StringUtils.isEmpty(obExecutorClass)) {
                                //Throwing exceptions since we cannot proceed without invalid executor names
                                throw new ConsentManagementRuntimeException("Executor class is not defined " +
                                        "correctly in open-banking.xml");
                            }
                            int priority = Integer.MAX_VALUE;
                            if (!StringUtils.isEmpty(obExecutorPriority)) {
                                priority = Integer.parseInt(obExecutorPriority);
                            }
                            executors.put(priority, obExecutorClass);
                        }
                    }
                    //Ordering the executors based on the priority number
                    LinkedHashMap<Integer, String> priorityMap = executors.entrySet()
                            .stream()
                            .sorted(comparingByKey())
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
                                    LinkedHashMap::new));
                    authorizeSteps.put(consentTypeName, priorityMap);
                }
            }
        }
    }

    /**
     * Method to obtain map of configs.
     *
     * @return Config map
     */
    public Map<String, Object> getConfiguration() {

        return this.configuration;
    }

    /**
     * Returns the element with the provided key as a String.
     *
     * @param key local part name
     * @return Corresponding value for key
     */
    private  Optional<String> getConfigurationFromKeyAsString(final String key) {
        return Optional.ofNullable((String) this.configuration.get(key));
    }

    /**
     * Returns the element with the provided key as a list.
     *
     * @param key local part name
     * @return Corresponding value for key
     */
    private Optional getConfigurationFromKeyAsList(final String key) {
        return Optional.of((ArrayList<String>) this.configuration.get(key));
    }

    public String getDataSourceName() {
        Optional<String> source = getConfigurationFromKeyAsString(ConsentManagementConstants.JDBC_PERSISTENCE_CONFIG);
        return source.map(String::trim).orElse("");
    }

    /**
     * Returns the database connection verification timeout in seconds configured.
     *
     * @return 1 if nothing is configured
     */
    public int getConnectionVerificationTimeout() {

        Optional<String> timeout = getConfigurationFromKeyAsString(ConsentManagementConstants.DB_VERIFICATION_TIMEOUT);
        return timeout.map(Integer::parseInt).orElse(1);
    }

    public Map<String, Map<Integer, String>> getConsentAuthorizeSteps() {

        return authorizeSteps;
    }

    public String getConsentValidationConfig() {

        Optional<String> source =
                getConfigurationFromKeyAsString(ConsentManagementConstants.CONSENT_JWT_PAYLOAD_VALIDATION);
        return source.map(String::trim).orElse("");
    }

    public int getConsentCacheAccessExpiry() {

        Optional<String> expiryTime = getConfigurationFromKeyAsString(ConsentManagementConstants.CACHE_ACCESS_EXPIRY);
        return expiryTime.map(Integer::parseInt).orElse(60);
    }

    public int getConsentCacheModifiedExpiry() {

        Optional<String> expiryTime = getConfigurationFromKeyAsString(ConsentManagementConstants.CACHE_MODIFY_EXPIRY);
        return expiryTime.map(Integer::parseInt).orElse(60);
    }

    public String getPreserveConsent() {

        Optional<String> source = getConfigurationFromKeyAsString(ConsentManagementConstants.PRESERVE_CONSENT);
        return source.map(String::trim).orElse("false");
    }

    public String getAuthServletExtension() {

        Optional<String> source = getConfigurationFromKeyAsString(ConsentManagementConstants.AUTH_SERVLET_EXTENSION);
        return source.map(String::trim).orElse("");
    }

    public String getConsentAPIUsername() {

        Optional<String> source = getConfigurationFromKeyAsString(ConsentManagementConstants.CONSENT_API_USERNAME);
        return source.map(String::trim).orElse("admin");
    }

    public String getConsentAPIPassword() {

        Optional<String> source = getConfigurationFromKeyAsString(ConsentManagementConstants.CONSENT_API_PASSWORD);
        return source.map(String::trim).orElse("admin");
    }
}
