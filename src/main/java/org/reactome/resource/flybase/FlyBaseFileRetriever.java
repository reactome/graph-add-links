package org.reactome.resource.flybase;

import org.reactome.DownloadInfo;
import org.reactome.resource.BasicFileRetriever;
import org.reactome.resource.Retriever;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 11/17/2023
 */
public class FlyBaseFileRetriever implements Retriever {
    private DownloadInfo downloadInfo;
    private BasicFileRetriever basicFileRetriever;

    public FlyBaseFileRetriever() {
        this.downloadInfo = new DownloadInfo("FlyBase");
        this.basicFileRetriever = new RegexNameBasicFileRetriever(getDownloadInfo().getDownloadables().get(0));
    }

    @Override
    public void downloadFiles() throws IOException {
        this.basicFileRetriever.downloadFile();
    }

    @Override
    public DownloadInfo getDownloadInfo() {
        return this.downloadInfo;
    }

    private static class RegexNameBasicFileRetriever extends BasicFileRetriever {

        public RegexNameBasicFileRetriever(DownloadInfo.Downloadable downloadable) {
            super(downloadable);
        }

        @Override
        public URL getResourceFileRemoteURL() throws IOException {
            String baseRemoteHTML = getBaseRemoteHTML();

            Pattern fileURLPattern = Pattern.compile("(" + getDownloadable().getRemoteFileName() + ")");
            Matcher fileURLMatcher = fileURLPattern.matcher(baseRemoteHTML);
            if (!fileURLMatcher.find()) {
                throw new IllegalStateException("Can not find pattern " + fileURLPattern + " in " +
                    getDownloadable().getBaseRemoteURL() + " HTML");
            }

            String remoteFileName = fileURLMatcher.group(1);
            return new URL(getDownloadable().getBaseRemoteURL().toString().concat(remoteFileName));
        }

        private String getBaseRemoteHTML() throws IOException {
            try (InputStream baseRemoteURLInputStream = getDownloadable().getBaseRemoteURL().openStream()) {
                BufferedReader baseRemoteURLBufferedReader =
                    new BufferedReader(new InputStreamReader(baseRemoteURLInputStream));
                return baseRemoteURLBufferedReader.lines().collect(Collectors.joining(System.lineSeparator()));
            }
        }
    }
}
