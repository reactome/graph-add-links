package org.reactome.resource;

import org.reactome.DownloadInfo;
import org.reactome.utils.ConfigParser;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 4/5/2022
 */
public abstract class AuthenticationBasicFileRetriever extends BasicFileRetriever {
    private DownloadInfo downloadInfo;

    public AuthenticationBasicFileRetriever(String resourceName) {
        super(resourceName);
    }

    @Override
    public void downloadFile(DownloadInfo.Downloadable downloadable) throws IOException {
        Files.createDirectories(ConfigParser.getDownloadDirectoryPath());

        HttpURLConnection httpURLConnection = (HttpURLConnection) getResourceFileRemoteURL(downloadable).openConnection();
        httpURLConnection.addRequestProperty("User-Agent", "Mozilla/4.0");

        String authString = getUserName() + ":" + getPassword();
        String encodedAuthString = java.util.Base64.getEncoder().encodeToString(authString.getBytes());
        String authHeaderValue = "Basic " + encodedAuthString;
        httpURLConnection.setRequestProperty("Authorization", authHeaderValue);

        httpURLConnection.setConnectTimeout(twoMinutes());
        httpURLConnection.setReadTimeout(twoMinutes());

        ReadableByteChannel remoteFileByteChannel = Channels.newChannel(httpURLConnection.getInputStream());
        FileOutputStream localFileOutputStream = new FileOutputStream(getLocalFilePath(downloadable).toFile());

        localFileOutputStream.getChannel().transferFrom(remoteFileByteChannel, 0, Long.MAX_VALUE);

        httpURLConnection.disconnect();
    }

    protected abstract String getUserName();

    protected abstract String getPassword();

    private int twoMinutes() {
        final int twoMinutesInMilliSeconds = 1000 * 60 * 2;
        return twoMinutesInMilliSeconds;
    }

}