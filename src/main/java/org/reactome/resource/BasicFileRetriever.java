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
public class BasicFileRetriever implements FileRetriever {
    private DownloadInfo downloadInfo;

    public BasicFileRetriever(String resourceName) {
        this.downloadInfo = new DownloadInfo(resourceName);
    }

    @Override
    public void downloadFile() throws IOException {
        Files.createDirectories(ConfigParser.getDownloadDirectoryPath());

        HttpURLConnection httpURLConnection = (HttpURLConnection) getResourceFileRemoteURL().openConnection();
        httpURLConnection.addRequestProperty("User-Agent", "Mozilla/4.0");
        httpURLConnection.setConnectTimeout(twoMinutes());
        httpURLConnection.setReadTimeout(twoMinutes());
        ReadableByteChannel remoteFileByteChannel = Channels.newChannel(httpURLConnection.getInputStream());
        FileOutputStream localFileOutputStream = new FileOutputStream(getLocalFilePath().toFile());

        localFileOutputStream.getChannel().transferFrom(remoteFileByteChannel, 0, Long.MAX_VALUE);
    }

    public URL getBaseRemoteURL() {
        return getDownloadInfo().getBaseRemoteURL();
    }

    public URL getFileRemoteURL() throws MalformedURLException {
        return new URL(getBaseRemoteURL().toString().concat(getRemoteFileName()));
    }

    public String getLocalFileName() {
        return getDownloadInfo().getLocalFileName();
    }

    public String getRemoteFileName() {
        return getDownloadInfo().getRemoteFileName();
    }

    private int twoMinutes() {
        final int twoMinutesInMilliSeconds = 1000 * 60 * 2;
        return twoMinutesInMilliSeconds;
    }

    private DownloadInfo getDownloadInfo() {
        return this.downloadInfo;
    }
}