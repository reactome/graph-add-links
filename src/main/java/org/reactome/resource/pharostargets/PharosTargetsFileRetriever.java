package org.reactome.resource.pharostargets;

import org.json.JSONArray;
import org.json.JSONObject;
import org.reactome.DownloadInfo;
import org.reactome.resource.BasicFileRetriever;
import org.reactome.resource.Retriever;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 12/19/2023
 */
public class PharosTargetsFileRetriever implements Retriever {
    //private static final String SKIP_AMOUNT_TOKEN = "##SKIPAMOUNT##";
    //private static final String TOP_AMOUNT_TOKEN = "##TOPAMOUNT##";

    private DownloadInfo downloadInfo;

    private BasicFileRetriever pharosTargetsBasicFileRetriever;

    public PharosTargetsFileRetriever() {
        this.downloadInfo = new DownloadInfo("PharosTargets");
        this.pharosTargetsBasicFileRetriever = new PharosTargetsBasicFileRetriever(
            getDownloadInfo().getDownloadables().get(0)
        );
    }

    @Override
    public void downloadFiles() throws IOException {
        this.pharosTargetsBasicFileRetriever.downloadFile();
    }

    @Override
    public DownloadInfo getDownloadInfo() {
        return this.downloadInfo;
    }

    public static class PharosTargetsBasicFileRetriever extends BasicFileRetriever {

        public PharosTargetsBasicFileRetriever(DownloadInfo.Downloadable downloadable) {
            super(downloadable);
        }

        @Override
        public void downloadFile() throws IOException {
            OutputStreamWriter localFileOutputStreamWriter =
                new OutputStreamWriter(Files.newOutputStream(getDownloadable().getLocalFilePath().toFile().toPath()));

            int requestNumber = 0;
            while (true) {
                URL url = getDownloadable().getBaseRemoteURL();
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setRequestProperty("Content-Type", "application/json");
                httpURLConnection.setDoOutput(true);

                OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(getGraphQLQuery(requestNumber).getBytes());
                outputStream.flush();

                if (httpURLConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    throw new RuntimeException("GraphQL query failed: " + httpURLConnection.getResponseCode());
                }

                List<String> targetsData = getTargetsData(httpURLConnection.getInputStream());
                if (targetsData.isEmpty()) {
                    break;
                }

                for (String targetDatum : targetsData) {
                    localFileOutputStreamWriter.write(targetDatum);
                }

                requestNumber += 1;
            }
        }

        private String getGraphQLQuery(int requestNumber) {
            final int queryBatchSize = 1000;
            final int skipAmount = queryBatchSize * requestNumber;

            return "{ \"query\": \"query {"
                + "    targets {"
                + "        count"
                + "        targets(top:" + queryBatchSize + " skip:" + skipAmount + ") {"
                + "            uniprot"
                + "        }"
                + "    }"
                + "}\" }";
        }

        private List<String> getTargetsData(InputStream httpURLConnectionInputStream) throws IOException {
            JSONObject jsonObject = new JSONObject(getJSONResponse(httpURLConnectionInputStream));
            JSONArray targets = ((JSONObject) ((JSONObject) jsonObject.get("data")).get("targets")).getJSONArray("targets");

            List<String> targetsData = new ArrayList<>();
            for (int i = 0; i < targets.length(); i++) {
                JSONObject target = ((JSONObject) targets.get(i));
                if (target.has("uniprot")) {
                    targetsData.add(target.get("uniprot") + "\n");
                }
            }
            return targetsData;
        }

        private String getJSONResponse(InputStream httpURLConnectionInputStream) throws IOException {
            BufferedReader reader = new BufferedReader(new InputStreamReader(httpURLConnectionInputStream));
            StringBuilder response = new StringBuilder();

            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        }
    }
}
