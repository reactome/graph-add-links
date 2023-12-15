package org.reactome.resource;

import org.reactome.DownloadInfo;
import org.reactome.utils.ConfigParser;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;

/**
 * @author Joel Weiser
 *         Created 3/3/2023
 */
public interface FileRetriever extends Retriever {
    default URL getResourceFileRemoteURL(DownloadInfo.Downloadable downloadable) throws IOException {
        return new URL(downloadable.getBaseRemoteURL().toString().concat(downloadable.getRemoteFileName()));
    }
}