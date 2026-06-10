package org.reactome.retrievers;

import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.reactome.DownloadInfo;
import org.reactome.graphdb.ReactomeGraphDatabase;
import org.reactome.utils.ConfigParser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.reactome.utils.CollectionUtils.split;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 11/18/2023
 */
public class UniProtMapperRetriever extends SingleRetriever {
    private final static String UNIPROT_REST_URL = "https://rest.uniprot.org";
    private final static String FILE_HEADER = "From\tTo\n";

    private final static HttpClient HTTP_CLIENT = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(30)).build();

    public UniProtMapperRetriever(DownloadInfo.Downloadable downloadable) {
        super(downloadable);
    }

    @Override
    public void downloadFile() throws IOException {
        List<Collection<String>> uniProtIdentifiersCollections = split(getUniProtIdsFromGraphDatabase(),100);

        try (BufferedWriter writer =
            Files.newBufferedWriter(
                getDownloadable().getLocalFilePath(),
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING
            )) {
            writer.write(FILE_HEADER);

            for (Collection<String> uniProtIdentifiers : uniProtIdentifiersCollections) {
                Map<String, List<String>> uniProtIdentifierToResourceIdentifiers =
                    getMapping(uniProtIdentifiers, getTargetDatabase(getDownloadable()));

                for (String uniProtIdentifier : uniProtIdentifierToResourceIdentifiers.keySet()) {
                    for (String resourceIdentifier : uniProtIdentifierToResourceIdentifiers.get(uniProtIdentifier)) {
                        writer.write(uniProtIdentifier);
                        writer.write("\t");
                        writer.write(resourceIdentifier);
                        writer.newLine();
                    }
                }
            }
        }
    }

    // Takes into account the header written at the beginning of the file before attempting to connect to UniProt to
    // get the mapping
    @Override
    public boolean isFileZeroSize() {
        final int HEADER_SIZE_IN_BYTES = FILE_HEADER.length();

        File file = new File(ConfigParser.getDownloadDirectoryPath() + "/", getDownloadable().getLocalFileName());

        return file.length() <= HEADER_SIZE_IN_BYTES;
    }

    Collection<String> getUniProtIdsFromGraphDatabase() {
        Result result = ReactomeGraphDatabase.getSession().run(
            "MATCH (rgp:ReferenceGeneProduct) RETURN DISTINCT rgp.identifier"
        );

        List<String> uniProtIds = new ArrayList<>();
        while (result.hasNext()) {
            Record uniProtIdentifierRecord = result.next();
            uniProtIds.add(uniProtIdentifierRecord.get(0).asString());
        }
        return uniProtIds;
    }


    /**
     * Submit a list of UniProt accession ids and a target database to obtain a mapping of UniProt to
     * target database ids.
     *
     * @param uniprotIds List of UniProt accession ids to map
     * @param targetDatabase Name of target database to which to map
     *
     * @return Map of UniProt accession ids to list of target database ids
     *
     * @throws IOException Thrown if unable to submit, check status, or receive results for query
     * @throws InterruptedException Thrown if unable to wait while query completes
     */
    Map<String, List<String>> getMapping(Collection<String> uniprotIds, String targetDatabase)
        throws IOException {

        final long maximumWaitTimeInMinutes = 5;
        final long sleepDelayTimeInSeconds = 10;

        String jobId = submitQuery(uniprotIds, targetDatabase);
        long timeWaitedInMilliseconds = 0;
        while (true) {
            boolean jobFinished = jobFinished(jobId);
            if (jobFinished) {
                break;
            } else if (timeWaitedInMilliseconds < getMinutesToMilliseconds(maximumWaitTimeInMinutes)) {
                long sleepTimeInMilliseconds = getSecondsToMilliseconds(sleepDelayTimeInSeconds);
                try {
                    Thread.sleep(sleepTimeInMilliseconds);
                } catch (InterruptedException e) {
                    throw new RuntimeException("Unable to complete sleep for " + sleepDelayTimeInSeconds, e);
                }
                timeWaitedInMilliseconds += sleepTimeInMilliseconds;
            } else {
                throw new RuntimeException("Waited " + maximumWaitTimeInMinutes + " minutes but job not finished for " +
                    uniprotIds.size() + " identifiers mapping to " + targetDatabase);
            }
        }

        return getJobResults(jobId);
    }

    private String getTargetDatabase(DownloadInfo.Downloadable downloadable) {
        return downloadable.getTargetDatabaseName();
    }

    private boolean jobFinished(String jobID) throws IOException {
        String response = sendGetRequest(getUniProtMappingRestUrl() + "status/" + jobID);

        return response.contains("\"jobStatus\":\"FINISHED\"");
    }

    private Map<String, List<String>> getJobResults(String jobID) throws IOException {
        String jobResultsURL = getUniProtMappingRestUrl() + "stream/" + jobID;
        String response = sendGetRequest(jobResultsURL);

        final Pattern mappingPairPattern = Pattern.compile("\"from\":\"(\\w+)\",\"to\":\"(.*?)\"");
        Matcher mappingPairMatcher = mappingPairPattern.matcher(response);

        Map<String, List<String>> uniProtIdToTargetIds = new HashMap<>();
        while (mappingPairMatcher.find()) {
            String uniprotId = mappingPairMatcher.group(1);
            String targetId = mappingPairMatcher.group(2);

            uniProtIdToTargetIds.computeIfAbsent(uniprotId, k -> new ArrayList<>()).add(targetId);
        }
        return uniProtIdToTargetIds;
    }

    private String submitQuery(Collection<String> ids, String targetDatabase) throws IOException {
        String formData =
            "ids=" +
                URLEncoder.encode(
                    String.join(",", ids),
                    StandardCharsets.UTF_8
                ) +
                "&from=UniProtKB_AC-ID" +
                "&to=" +
                URLEncoder.encode(
                    targetDatabase,
                    StandardCharsets.UTF_8
                );

        String response = sendPostRequest(getUniProtMappingRestUrl() + "run", formData);

        Matcher matcher = Pattern.compile("\"jobId\":\"(.*?)\"").matcher(response);
        if (matcher.find()) {
            return matcher.group(1);
        }

        throw new RuntimeException("Could not retrieve job ID from: " + response);
    }

    private String sendGetRequest(String url) throws IOException {
        try {
            HttpRequest request =
                HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .version(HttpClient.Version.HTTP_1_1)
                    .GET()
                    .build();

            HttpResponse<String> response =
                HTTP_CLIENT.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
                );

            return response.body();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            throw new IOException(
                "Interrupted while calling " + url,
                e
            );
        }
    }

    private String sendPostRequest(
        String url,
        String formData
    ) throws IOException {

        try {
            HttpRequest request =
                HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .version(HttpClient.Version.HTTP_1_1)
                    .header(
                        "Content-Type",
                        "application/x-www-form-urlencoded"
                    )
                    .POST(
                        HttpRequest.BodyPublishers.ofString(
                            formData
                        )
                    )
                    .build();

            HttpResponse<String> response =
                HTTP_CLIENT.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
                );

            return response.body();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            throw new IOException("Interrupted while submitting request", e);
        }
    }

    private static String getUniProtMappingRestUrl() {
        return getUniProtRestUrl() + "/idmapping/";
    }

    private static String getUniProtRestUrl() {
        return UNIPROT_REST_URL;
    }

    private long getMinutesToMilliseconds(long minutes) {
        final long secondsPerMinute = 60;

        long seconds = minutes * secondsPerMinute;

        return getSecondsToMilliseconds(seconds);
    }

    private long getSecondsToMilliseconds(long seconds) {
        final long milliSecondsPerSecond = 1000;

        return seconds * milliSecondsPerSecond;
    }
}
