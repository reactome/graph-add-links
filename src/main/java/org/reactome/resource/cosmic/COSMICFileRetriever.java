package org.reactome.resource.cosmic;

import org.reactome.DownloadInfo;
import org.reactome.resource.AuthenticationBasicFileRetriever;
import org.reactome.utils.ConfigParser;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.util.stream.Collectors;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 12/29/2023
 */
public class COSMICFileRetriever extends AuthenticationBasicFileRetriever {

    public COSMICFileRetriever() {
        super("COSMIC");
    }

    @Override
    public void downloadFile(DownloadInfo.Downloadable downloadable) throws IOException {
        Files.createDirectories(ConfigParser.getDownloadDirectoryPath());

        HttpURLConnection httpURLConnection = getHttpURLConnection(getURLForQuery(downloadable));

        ReadableByteChannel remoteFileByteChannel = Channels.newChannel(httpURLConnection.getInputStream());
        FileOutputStream localFileOutputStream = new FileOutputStream(getLocalFilePath(downloadable).toFile());

        localFileOutputStream.getChannel().transferFrom(remoteFileByteChannel, 0, Long.MAX_VALUE);

        httpURLConnection.disconnect();
    }

    private URL getURLForQuery(DownloadInfo.Downloadable downloadable) throws IOException {
        HttpURLConnection httpURLConnection = getHttpURLConnection(getResourceFileRemoteURL(downloadable));
        httpURLConnection.setRequestProperty("Authorization", getAuthorizationHeaderValue());

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
        String urlToQuery = bufferedReader.lines().collect(Collectors.joining())
            .replace("{","")
            .replace("}","")
            .replace("\"url\":","")
            .replaceAll("\"","");

        httpURLConnection.disconnect();

        return new URL(urlToQuery);
    }

    @Override
    protected String getUserName() {
        return ConfigParser.getConfigProperty("cosmicUser");
    }

    @Override
    protected String getPassword() {
        return ConfigParser.getConfigProperty("cosmicPassword");
    }

    private HttpURLConnection getHttpURLConnection(URL url) throws IOException {
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.addRequestProperty("User-Agent", "Mozilla/4.0");

        httpURLConnection.setConnectTimeout(twoMinutes());
        httpURLConnection.setReadTimeout(twoMinutes());

        return httpURLConnection;
    }
}
