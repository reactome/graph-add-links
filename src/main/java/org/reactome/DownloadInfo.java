package org.reactome;

import org.json.JSONArray;
import org.json.JSONObject;
import org.reactome.utils.ResourceJSONParser;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 11/22/2023
 */
public class DownloadInfo {
    private String resourceName;
    private List<Downloadable> downloadables;

    public DownloadInfo(String resourceName) {
        this.resourceName = resourceName;
        this.downloadables = retrieveDownloadablesInfo();
    }

    public List<Downloadable> getDownloadables() {
        return this.downloadables;
    }

    private String getResourceName() {
        return this.resourceName;
    }

    private List<Downloadable> retrieveDownloadablesInfo() {
        List<Downloadable> downloadables = new ArrayList<>();

        JSONArray downloadJSON = retrieveDownloadJSON();
        for (int i = 0; i < downloadJSON.length(); i++) {
            JSONObject downloadableAsJSONObject = downloadJSON.getJSONObject(i);
            downloadables.add(new Downloadable(downloadableAsJSONObject));
        }
        return downloadables;
    }

    private JSONArray retrieveDownloadJSON() {
        JSONObject resourceJSON = ResourceJSONParser.getResourceJSONObject(getResourceName());
        return resourceJSON.getJSONArray("downloads");
    }

    public class Downloadable {
        private JSONObject downloadInfoAsJSON;

        private URL baseRemoteURL;
        private String remoteFileName;
        private Boolean isRegexFileName;
        private String localFileName;
        private String targetDatabaseName;

        public Downloadable(JSONObject downloadInfoAsJSON) {
            this.downloadInfoAsJSON = downloadInfoAsJSON;

            this.baseRemoteURL = retrieveBaseRemoteURL();
            this.remoteFileName = retrieveRemoteFileName();
            this.isRegexFileName = retrieveIsRegexFileName();
            this.localFileName = retrieveLocalFileName();
            this.targetDatabaseName = retrieveTargetDatabaseName();

        }

        public URL getBaseRemoteURL() {
            return this.baseRemoteURL;
        }

        public String getRemoteFileName() {
            return this.remoteFileName;
        }

        public boolean isRegexFileName() {
            return this.isRegexFileName;
        }

        public String getLocalFileName() {
            return this.localFileName;
        }

        public String getTargetDatabaseName() {
            return this.targetDatabaseName;
        }


        private URL retrieveBaseRemoteURL() {
            if (!getDownloadInfoAsJSON().has("baseURL")) {
                return null;
            }

            String baseURL = getDownloadInfoAsJSON().getString("baseURL");
            try {
                return new URL(baseURL);
            } catch (MalformedURLException e) {
                throw new RuntimeException("Could not create base remote URL", e);
            }
        }

        private String retrieveRemoteFileName() {
            if (!getDownloadInfoAsJSON().has("fileName")) {
                return "";
            }

            return getDownloadInfoAsJSON().getString("fileName");
        }

        private Boolean retrieveIsRegexFileName() {
            if (!getDownloadInfoAsJSON().has("fileNameIsRegex")) {
                return null;
            }

            return getDownloadInfoAsJSON().getBoolean("fileNameIsRegex");
        }

        private String retrieveLocalFileName() {
            return getDownloadInfoAsJSON().getString("localName");
        }

        private String retrieveTargetDatabaseName() {
            if (!getDownloadInfoAsJSON().has("targetDatabaseName")) {
                return "";
            }

            return getDownloadInfoAsJSON().getString("targetDatabaseName");
        }

        private JSONObject getDownloadInfoAsJSON() {
            return this.downloadInfoAsJSON;
        }
    }
}
