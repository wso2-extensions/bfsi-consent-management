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

package org.wso2.bfsi.consent.management.service.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.bfsi.consent.management.common.exceptions.ConsentManagementRuntimeException;
import org.wso2.bfsi.consent.management.common.persistence.JDBCPersistenceManager;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Utility class for database operations.
 */
public class DatabaseUtil {

    private static final Log log = LogFactory.getLog(DatabaseUtil.class);

    /**
     * Returns an database connection for Consent Management data source.
     *
     * @return Database connection.
     * @throws ConsentManagementRuntimeException Exception occurred when getting the data source.
     */
    public static Connection getDBConnection() throws ConsentManagementRuntimeException {

        return JDBCPersistenceManager.getInstance().getDBConnection();
    }

    /**
     * Revoke the transaction when catch then sql transaction errors.
     *
     * @param dbConnection database connection.
     */
    public static void rollbackTransaction(Connection dbConnection) {

        try {
            if (dbConnection != null) {
                dbConnection.rollback();
            }
        } catch (SQLException e) {
            log.error("An error occurred while rolling back transactions. ", e);
        }
    }

    /**
     * Commit the transaction.
     *
     * @param dbConnection database connection.
     */
    public static void commitTransaction(Connection dbConnection) {

        try {
            if (dbConnection != null) {
                dbConnection.commit();
            }
        } catch (SQLException e) {
            log.error("An error occurred while commit transactions. ", e);
        }
    }

    public static void closeConnection(Connection dbConnection) {

        if (dbConnection != null) {
            try {
                dbConnection.close();
            } catch (SQLException e) {
                log.error("Database error. Could not close statement. Continuing with others. - "
                        + e.getMessage().replaceAll("[\r\n]", ""), e);
            }
        }
    }
}
