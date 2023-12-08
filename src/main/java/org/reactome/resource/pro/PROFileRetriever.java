package org.reactome.resource.pro;

import org.reactome.resource.BasicFileRetriever;
import org.reactome.resource.FileRetriever;

import java.io.IOException;
import java.net.URL;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 11/17/2023
 */
public class PROFileRetriever implements FileRetriever {
    private BasicFileRetriever basicFileRetriever;

    public PROFileRetriever() {
        this.basicFileRetriever = new BasicFileRetriever("PRO");
    }

    @Override
    public void downloadFile() throws IOException {
        this.basicFileRetriever.downloadFile();
    }

    @Override
    public String getLocalFileName() {
        return this.basicFileRetriever.getLocalFileName();
    }

    @Override
    public URL getBaseRemoteURL() {
        return this.basicFileRetriever.getBaseRemoteURL();
    }

    @Override
    public String getRemoteFileName() {
        return this.basicFileRetriever.getRemoteFileName();
    }
}
