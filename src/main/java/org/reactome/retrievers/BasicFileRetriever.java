package org.reactome.retrievers;

import org.reactome.DownloadInfo;
import org.reactome.utils.ConfigParser;

import java.io.*;
import java.net.HttpURLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 4/5/2022
 */
public class BasicFileRetriever extends FileRetriever {
//    private DownloadInfo downloadInfo;
    private DownloadInfo.Downloadable downloadable;

    public BasicFileRetriever(DownloadInfo.Downloadable downloadable) {
        super(downloadable);
    }

    @Override
    public void downloadFile() throws IOException {
        Files.createDirectories(ConfigParser.getDownloadDirectoryPath());

        HttpURLConnection httpURLConnection = (HttpURLConnection) getResourceFileRemoteURL().openConnection();
        httpURLConnection.addRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8");
        httpURLConnection.addRequestProperty("User-Agent", "Mozilla/5.0");

        httpURLConnection.setConnectTimeout(twoMinutes());
        httpURLConnection.setReadTimeout(twoMinutes());

        ReadableByteChannel remoteFileByteChannel = Channels.newChannel(httpURLConnection.getInputStream());
        FileOutputStream localFileOutputStream = new FileOutputStream(getDownloadable().getLocalFilePath().toFile());



        localFileOutputStream.getChannel().transferFrom(remoteFileByteChannel, 0, Long.MAX_VALUE);
    }
//
//    @Override
//    public DownloadInfo getDownloadInfo() {
//        return this.downloadInfo;
//    }

    protected int twoMinutes() {
        final int twoMinutesInMilliSeconds = 1000 * 60 * 2;
        return twoMinutesInMilliSeconds;
    }
}