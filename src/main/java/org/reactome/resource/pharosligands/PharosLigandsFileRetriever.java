package org.reactome.resource.pharosligands;

import org.json.JSONArray;
import org.json.JSONObject;
import org.reactome.DownloadInfo;
import org.reactome.retrievers.BasicFileRetriever;
import org.reactome.retrievers.Retriever;

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
public class PharosLigandsFileRetriever implements Retriever {
    //private static final String SKIP_AMOUNT_TOKEN = "##SKIPAMOUNT##";
    //private static final String TOP_AMOUNT_TOKEN = "##TOPAMOUNT##";

    private DownloadInfo downloadInfo;

    private BasicFileRetriever pharosLigandsBasicFileRetriever;

    public PharosLigandsFileRetriever() {
        this.downloadInfo = new DownloadInfo("PharosLigands");
        this.pharosLigandsBasicFileRetriever = new PharosLigandsBasicFileRetriever(
            getDownloadInfo().getDownloadables().get(0)
        );
    }

    @Override
    public void downloadFiles() throws IOException {
        this.pharosLigandsBasicFileRetriever.downloadFileIfNeeded();
    }

    @Override
    public DownloadInfo getDownloadInfo() {
        return this.downloadInfo;
    }

    public static class PharosLigandsBasicFileRetriever extends BasicFileRetriever {
        public PharosLigandsBasicFileRetriever(DownloadInfo.Downloadable downloadable) {
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

                List<String> ligandsData = getLigandsData(httpURLConnection.getInputStream());
                if (ligandsData.isEmpty()) {
                    break;
                }

                for (String ligandDatum : ligandsData) {
                    localFileOutputStreamWriter.write(ligandDatum);
                }

                requestNumber += 1;
            }

            //ReadableByteChannel remoteFileByteChannel = Channels.newChannel(httpURLConnection.getInputStream());
            //localFileOutputStream.getChannel().transferFrom(remoteFileByteChannel, 0, Long.MAX_VALUE);
        }

        private String getGraphQLQuery(int requestNumber) {
            final int queryBatchSize = 1000;
            final int skipAmount = queryBatchSize * requestNumber;

            return "{ \"query\": \"query {"
                + "  ligands(filter: {facets: [{facet:\\\"Data Source\\\" values:[\\\"Guide to Pharmacology\\\"]}]}){"
                + "    count"
                + "    ligands(top:" + queryBatchSize + " skip:" + skipAmount + "){"
                + "      ligid"
                + "      name"
                + "      isdrug"
                + "      synonyms {"
                + "        name"
                + "        value"
                + "      }"
                + "    }"
                + "  }"
                + "}\"}";
        }

        private List<String> getLigandsData(InputStream httpURLConnectionInputStream) throws IOException {
            JSONObject jsonObject = new JSONObject(getJSONResponse(httpURLConnectionInputStream));
            JSONArray ligands = ((JSONObject) ((JSONObject) jsonObject.get("data")).get("ligands")).getJSONArray("ligands");

            List<String> ligandsData = new ArrayList<>();
            for (int i = 0; i < ligands.length(); i++) {
                JSONObject ligand = ((JSONObject) ligands.get(i));
                boolean isDrug = ligand.getBoolean("isdrug");
                if (isDrug) {
                    String ligandId = ligand.getString("ligid");
                    String gtpIdentifier = getGTPIdentifierFromLigandSynonyms(ligand);

                    if (!gtpIdentifier.isEmpty() && !ligandId.isEmpty()) {
                        ligandsData.add(gtpIdentifier + "\t" + ligandId + "\n");
                    }
                }
            }
            return ligandsData;
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

        private String getGTPIdentifierFromLigandSynonyms(JSONObject ligand) {
            JSONArray synonyms = ligand.getJSONArray("synonyms");

            return toJSONObjectList(synonyms)
                .stream()
                .filter(synonym -> isGTPSynonym(synonym))
                .map(synonym -> getIdentifierValue(synonym))
                .findFirst()
                .orElse("");
        }

        private List<JSONObject> toJSONObjectList(JSONArray synonymsArray) {
            List<JSONObject> jsonObjectSynonyms = new ArrayList<>();
            for (int i = 0; i < synonymsArray.length(); i++) {
                jsonObjectSynonyms.add(synonymsArray.getJSONObject(i));
            }
            return jsonObjectSynonyms;
        }

        private boolean isGTPSynonym(JSONObject ligandSynonym) {
            return ligandSynonym.getString("name").equals("Guide to Pharmacology");
        }

        private String getIdentifierValue(JSONObject ligandSynonym) {
            return ligandSynonym.getString("value");
        }
    }
}
