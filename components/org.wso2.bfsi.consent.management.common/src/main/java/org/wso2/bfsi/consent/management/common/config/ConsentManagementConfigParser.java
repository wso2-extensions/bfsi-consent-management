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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.bfsi.consent.management.common.exceptions.ConsentManagementRuntimeException;
import org.wso2.bfsi.consent.management.common.util.CarbonUtils;
import org.wso2.bfsi.consent.management.common.util.ConsentManagementConstants;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

import javax.xml.stream.XMLStreamException;

/**
 * Config parser for bfsi-consent-management.xml.
 */
public class ConsentManagementConfigParser {

    private static final Log log = LogFactory.getLog(ConsentManagementConfigParser.class);
    // To enable attempted thread-safety using double-check locking
    private static final Object lock = new Object();
    private static volatile ConsentManagementConfigParser parser;
    private static final Map<String, Object> configuration = new HashMap<>();

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
    private void buildConfiguration() {

        InputStream inStream = null;
        StAXOMBuilder builder;
        String warningMessage = "";
        try {
            File configXml = new File(CarbonUtils.getCarbonConfigDirPath(), ConsentManagementConstants.CONFIG_FILE);
            if (configXml.exists()) {
                inStream = new FileInputStream(configXml);
            }
            if (inStream == null) {
                String message =
                        "BFSI Consent Management configuration not found at: " + CarbonUtils.getCarbonConfigDirPath() +
                                ConsentManagementConstants.CONFIG_FILE + " . Cause - " + warningMessage;
                if (log.isDebugEnabled()) {
                    log.debug(message.replaceAll("[\r\n]", ""));
                }
                throw new FileNotFoundException(message);
            }
            builder = new StAXOMBuilder(inStream);
            OMElement rootElement = builder.getDocumentElement();
            Stack<String> nameStack = new Stack<>();
            readChildElements(rootElement, nameStack);
        } catch (IOException | XMLStreamException | OMException e) {
            throw new ConsentManagementRuntimeException("Error occurred while building configuration from" +
                    " bfsi-consent-management.xml", e);
        } finally {
            try {
                if (inStream != null) {
                    inStream.close();
                }
            } catch (IOException e) {
                log.error("Error closing the input stream for bfsi-consent-management.xml", e);
            }
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

        String text = element.getText();
        return text != null && text.trim().length() != 0;
    }

    /**
     * Method to obtain config key from stack.
     *
     * @param nameStack Stack of strings with names.
     * @return key as a String
     */
    private String getKey(Stack<String> nameStack) {

        StringBuilder key = new StringBuilder();
        for (int index = 0; index < nameStack.size(); index++) {
            String name = nameStack.elementAt(index);
            key.append(name).append(".");
        }
        key.deleteCharAt(key.lastIndexOf("."));
        return key.toString();
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

    /**
     * Returns the element with the provided key.
     *
     * @param key local part name
     * @return Corresponding value for key
     */
    public Object getConfigElementFromKey(String key) {

        return configuration.get(key);
    }

    public String getDataSourceName() {

        return getConfigElementFromKey(ConsentManagementConstants.JDBC_PERSISTENCE_CONFIG) == null ? "" :
                ((String) getConfigElementFromKey(ConsentManagementConstants.JDBC_PERSISTENCE_CONFIG)).trim();
    }

}
