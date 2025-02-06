package org.reactome.graphdb;

import org.neo4j.driver.*;
import org.reactome.utils.ConfigParser;

import java.io.IOException;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 12/28/2021
 */
public class ReactomeGraphDatabase {
    private static Driver driver;
    private static Session session;

    // Make class non-instantiable
    private ReactomeGraphDatabase() {}

    public static Session getSession() {
        if (session == null) {
            session = getDriver().session();
        }

        return session;
    }

    private static Driver getDriver() {
        if (driver == null) {
            AuthToken connectionCredentials;
            try {
                connectionCredentials = getGraphDatabaseCredentials();
            } catch (IOException e) {
                throw new RuntimeException(
                    "Unable to read 'auth.properties' resource file to get graph database credentials", e
                );
            }

            driver = GraphDatabase.driver("bolt://localhost:7687", connectionCredentials);
        }
        return driver;
    }

    private static AuthToken getGraphDatabaseCredentials() throws IOException {
        return AuthTokens.basic(getUserName(), getPassword());
    }

    private static String getUserName() throws IOException {
        return ConfigParser.getConfigProperty("neo4jUserName");
    }

    private static String getPassword() throws IOException {
        return ConfigParser.getConfigProperty("neo4jPassword");
    }
}

