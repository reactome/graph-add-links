package org.reactome.resource.orphanet;

import org.reactome.DownloadInfo;
import org.reactome.resource.AuthenticationBasicFileRetriever;
import org.reactome.resource.Retriever;
import org.reactome.utils.ConfigParser;

import java.io.IOException;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 11/19/2023
 */
public class OrphanetFileRetriever implements Retriever {

    private DownloadInfo downloadInfo;

    private AuthenticationBasicFileRetriever orphanetAuthenticationFileRetriever;

    public OrphanetFileRetriever() {
        this.downloadInfo = new DownloadInfo("Orphanet");
        this.orphanetAuthenticationFileRetriever =
            new OrphanetFileRetriever.OrphanetAuthenticationFileRetriever(getDownloadInfo().getDownloadables().get(0));
    }

    @Override
    public void downloadFiles() throws IOException {
        this.orphanetAuthenticationFileRetriever.downloadFileIfNeeded();
    }

    @Override
    public DownloadInfo getDownloadInfo() {
        return this.downloadInfo;
    }

    private static class OrphanetAuthenticationFileRetriever extends AuthenticationBasicFileRetriever {
        public OrphanetAuthenticationFileRetriever(DownloadInfo.Downloadable downloadable) {
            super(downloadable);
        }

        @Override
        protected String getUserName() {
            return ConfigParser.getConfigProperty("orphanetUser");
        }

        @Override
        protected String getPassword() {
            return ConfigParser.getConfigProperty("orphanetPassword");
        }
    }
}
