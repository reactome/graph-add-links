package org.reactome.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 4/11/2022
 */
public class ConfigParser {
    private static Properties configProperties;

    public static String getConfigProperty(String propertyName) {
        if (configProperties == null) {
            configProperties = getConfigProperties();
        }
        return configProperties.getProperty(propertyName);
    }

    public static Path getDownloadDirectoryPath() {
        if (configProperties == null) {
            configProperties = getConfigProperties();
        }
        return Paths.get(
            configProperties.getProperty("downloadDirectory")
        );
    }

    private static Properties getConfigProperties() {
        Properties configProperties = new Properties();
        try {
            configProperties.load(getConfigFileInputStream());
        } catch (IOException e) {
            throw new RuntimeException("Unable to get values from configuration file", e);
        }
        return configProperties;
    }

    private static InputStream getConfigFileInputStream() {
        return ConfigParser.class.getClassLoader().getResourceAsStream("config.properties");
    }
}
