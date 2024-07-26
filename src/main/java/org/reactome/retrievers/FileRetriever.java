package org.reactome.retrievers;

import org.reactome.DownloadInfo;

import java.io.IOException;
import java.net.URL;

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