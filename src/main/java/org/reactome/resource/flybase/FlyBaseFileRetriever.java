package org.reactome.resource.flybase;

import org.reactome.DownloadInfo;
import org.reactome.resource.BasicFileRetriever;

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
public class FlyBaseFileRetriever extends BasicFileRetriever {

    public FlyBaseFileRetriever() {
        super("FlyBase");
    }

    @Override
    public URL getResourceFileRemoteURL(DownloadInfo.Downloadable downloadable) throws IOException {
        String baseRemoteHTML = getBaseRemoteHTML(downloadable);

        Pattern fileURLPattern = Pattern.compile("(" + downloadable.getRemoteFileName() + ")");
        Matcher fileURLMatcher = fileURLPattern.matcher(baseRemoteHTML);
        if (!fileURLMatcher.find()) {
            throw new IllegalStateException("Can not find pattern " + fileURLPattern + " in " +
                downloadable.getBaseRemoteURL() + " HTML");
        }

        String remoteFileName = fileURLMatcher.group(1);
        return new URL(downloadable.getBaseRemoteURL().toString().concat(remoteFileName));
    }

    private String getBaseRemoteHTML(DownloadInfo.Downloadable downloadable) throws IOException {
        try (InputStream baseRemoteURLInputStream = downloadable.getBaseRemoteURL().openStream()) {
            BufferedReader baseRemoteURLBufferedReader =
                new BufferedReader(new InputStreamReader(baseRemoteURLInputStream));
            return baseRemoteURLBufferedReader.lines().collect(Collectors.joining(System.lineSeparator()));
        }
    }
}
