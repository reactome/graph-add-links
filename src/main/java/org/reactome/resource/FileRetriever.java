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
public abstract class FileRetriever extends SingleRetriever {

    public FileRetriever(DownloadInfo.Downloadable downloadable) {
        super(downloadable);
    }

    protected URL getResourceFileRemoteURL() throws IOException {
        return new URL(getDownloadable().getBaseRemoteURL().toString().concat(getDownloadable().getRemoteFileName()));
    }
}