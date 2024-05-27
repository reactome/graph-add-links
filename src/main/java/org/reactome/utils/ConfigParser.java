package org.reactome.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 4/11/2022
 */
public class ConfigParser {
    private static Path configFilePath;
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

    public static void setConfigFilePath(String configFilePathAsString) {
        configFilePath = Paths.get(configFilePathAsString);
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

    private static InputStream getConfigFileInputStream() throws IOException {
        if (configFilePath == null) {
            return ConfigParser.class.getClassLoader().getResourceAsStream("config.properties");
        }
        return Files.newInputStream(configFilePath);
    }
}
