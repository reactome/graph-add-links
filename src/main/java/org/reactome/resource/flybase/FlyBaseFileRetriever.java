package org.reactome.resource.flybase;

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
    //private BasicFileRetriever basicFileRetriever;

    public FlyBaseFileRetriever() {
        super("FlyBase");
    }

    @Override
    public URL getResourceFileRemoteURL() throws IOException {
        String baseRemoteHTML = getBaseRemoteHTML();

        Pattern fileURLPattern = Pattern.compile("(" + getRemoteFileName() + ")");
        Matcher fileURLMatcher = fileURLPattern.matcher(baseRemoteHTML);
        if (!fileURLMatcher.find()) {
            throw new IllegalStateException("Can not find pattern " + fileURLPattern + " in " +
                getBaseRemoteURL() + " HTML");
        }

        String remoteFileName = fileURLMatcher.group(1);
        return new URL(getBaseRemoteURL().toString().concat(remoteFileName));
    }

//    @Override
//    public URL getBaseRemoteURL() {
//        return getBasicFileRetriever().getBaseRemoteURL();
//    }
//
//    @Override
//    public String getRemoteFileName() {
//        return getBasicFileRetriever().getRemoteFileName();
//    }

    //private BasicFileRetriever getBasicFileRetriever() {
    //    return this.basicFileRetriever;
    //}

    private String getBaseRemoteHTML() throws IOException {
        try (InputStream baseRemoteURLInputStream = getBaseRemoteURL().openStream()) {
            BufferedReader baseRemoteURLBufferedReader =
                new BufferedReader(new InputStreamReader(baseRemoteURLInputStream));
            return baseRemoteURLBufferedReader.lines().collect(Collectors.joining(System.lineSeparator()));
        }
    }

//    @Override
//    public void downloadFile() throws IOException {
//        getBasicFileRetriever().downloadFile();
//    }

//    @Override
//    public String getLocalFileName() {
//        return getBasicFileRetriever().getLocalFileName();
//    }
}
