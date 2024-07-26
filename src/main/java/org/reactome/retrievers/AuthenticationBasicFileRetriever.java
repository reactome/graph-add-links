package org.reactome.retrievers;

import org.reactome.DownloadInfo;
import org.reactome.utils.ConfigParser;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 4/5/2022
 */
public abstract class AuthenticationBasicFileRetriever extends BasicFileRetriever {

    public AuthenticationBasicFileRetriever(DownloadInfo.Downloadable downloadable) {
        super(downloadable);
    }

    @Override
    public void downloadFile() throws IOException {
        Files.createDirectories(ConfigParser.getDownloadDirectoryPath());

        HttpURLConnection httpURLConnection = (HttpURLConnection) getResourceFileRemoteURL().openConnection();
        httpURLConnection.addRequestProperty("User-Agent", "Mozilla/4.0");

        httpURLConnection.setRequestProperty("Authorization", getAuthorizationHeaderValue());

        httpURLConnection.setConnectTimeout(twoMinutes());
        httpURLConnection.setReadTimeout(twoMinutes());

        ReadableByteChannel remoteFileByteChannel = Channels.newChannel(httpURLConnection.getInputStream());
        FileOutputStream localFileOutputStream = new FileOutputStream(getDownloadable().getLocalFilePath().toFile());

        localFileOutputStream.getChannel().transferFrom(remoteFileByteChannel, 0, Long.MAX_VALUE);

        httpURLConnection.disconnect();
    }

    protected abstract String getUserName();

    protected abstract String getPassword();

    protected String getAuthorizationHeaderValue() {
        String authString = getUserName() + ":" + getPassword();
        String encodedAuthString = java.util.Base64.getEncoder().encodeToString(authString.getBytes());

        return "Basic " + encodedAuthString;
    }
}