package org.reactome.resource;

import org.reactome.utils.ConfigParser;

import java.io.IOException;
import java.nio.file.Path;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 11/22/2023
 */
public interface Retriever {
    void downloadFile() throws IOException;

    default Path getLocalFilePath() {
        return ConfigParser.getDownloadDirectoryPath().resolve(getLocalFileName());
    }

    String getLocalFileName();
}
