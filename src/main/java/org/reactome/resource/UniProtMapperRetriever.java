package org.reactome.resource;

import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.StatementResult;
import org.reactome.DownloadInfo;
import org.reactome.graphdb.ReactomeGraphDatabase;
import org.reactome.utils.ConfigParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.reactome.utils.CollectionUtils.split;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 11/18/2023
 */
public class UniProtMapperRetriever implements Retriever {
    private final static String UNIPROT_REST_URL = "https://rest.uniprot.org";

    private DownloadInfo downloadInfo;

    public UniProtMapperRetriever(String resourceName) {
        this.downloadInfo = new DownloadInfo(resourceName);
    }

    @Override
    public void downloadFile() throws IOException {
        List<Collection<String>> uniProtIdentifiersCollections = split(getUniProtIdsFromGraphDatabase(),100);

        Files.write(getLocalFilePath(), "From\tTo\n".getBytes(), StandardOpenOption.CREATE);
        for (Collection<String> uniProtIdentifiers : uniProtIdentifiersCollections) {
            Map<String, List<String>> uniProtIdentifierToResourceIdentifiers =
                getMapping(uniProtIdentifiers, getTargetDatabase());

            for (String uniProtIdentifier : uniProtIdentifierToResourceIdentifiers.keySet()) {
                for (String resourceIdentifier : uniProtIdentifierToResourceIdentifiers.get(uniProtIdentifier)) {
                    Files.write(
                        getLocalFilePath(),
                        (uniProtIdentifier + "\t" + resourceIdentifier + "\n").getBytes(),
                        StandardOpenOption.APPEND
                    );
                }
            }
        }
    }

    Collection<String> getUniProtIdsFromGraphDatabase() {
        StatementResult result = ReactomeGraphDatabase.getSession().run(
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
                throw new RuntimeException("Waited " + maximumWaitTimeInMinutes + "minutes but job not finished for " +
                    uniprotIds.size() + " identifiers mapping to " + targetDatabase);
            }
        }

        return getJobResults(jobId);
    }

    @Override
    public String getLocalFileName() {
        return this.downloadInfo.getLocalFileName();
    }

    private String getTargetDatabase() {
        return this.downloadInfo.getTargetDatabaseName();
    }

    private static boolean jobFinished(String jobID) throws IOException {
        StringBuilder curlQueryBuilder = new StringBuilder();
        curlQueryBuilder.append("curl -s ");
        curlQueryBuilder.append(getUniProtMappingRestUrl() + "status/" + jobID);

        Process process = Runtime.getRuntime().exec(curlQueryBuilder.toString());
        BufferedReader jobStatusWebSource = new BufferedReader(new InputStreamReader(process.getInputStream()));


        return jobStatusWebSource
            .lines()
            .anyMatch(
                line -> line.contains("{\"jobStatus\":\"FINISHED\"}")
            );
    }

    private Map<String, List<String>> getJobResults(String jobID) throws IOException {
        String jobResultsURL = getUniProtMappingRestUrl() + "stream/" + jobID;

        BufferedReader jobResultsWebSource = getCurlReader("curl -s", jobResultsURL);

        Map<String, List<String>> uniProtIdToTargetIds = new HashMap<>();

        final Pattern mappingPairPattern = Pattern.compile("\"from\":\"(\\w+)\",\"to\":\"(.*?)\"");

        String resultsLine;
        while ((resultsLine = jobResultsWebSource.readLine()) != null) {
            Matcher mappingPairMatcher = mappingPairPattern.matcher(resultsLine);
            while (mappingPairMatcher.find()) {
                String uniprotId = mappingPairMatcher.group(1);
                String targetId = mappingPairMatcher.group(2);

                uniProtIdToTargetIds.computeIfAbsent(uniprotId, k -> new ArrayList<>()).add(targetId);
            }

        }

        return uniProtIdToTargetIds;
    }

    private String submitQuery(Collection<String> ids, String targetDatabase) throws IOException  {
        StringBuilder curlQueryBuilder = new StringBuilder();
        curlQueryBuilder.append("curl --request POST ");
        curlQueryBuilder.append(getUniProtMappingRestUrl() + "run ");
        curlQueryBuilder.append(getIdsInfoAsString(ids));
        curlQueryBuilder.append(getUniProtDatabaseInfoAsString());
        curlQueryBuilder.append(getTargetDatabaseInfoAsString(targetDatabase));

        Process process = Runtime.getRuntime().exec(curlQueryBuilder.toString());
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        return bufferedReader
            .lines()
            .filter(line -> line.contains("jobId"))
            .map(line -> {
                Matcher jobIdMatcher = Pattern.compile("\"jobId\":\"(.*)\"").matcher(line);
                if (jobIdMatcher.find()) {
                    return jobIdMatcher.group(1);
                } else {
                    throw new RuntimeException("Could not get job id from " + line);
                }
            })
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Curl could not get job id from query: " +
                curlQueryBuilder.toString()));
    }

    private static String getUniProtMappingRestUrl() {
        return getUniProtRestUrl() + "/idmapping/";
    }

    private String getIdsInfoAsString(Collection<String> ids) {
        return getInfoStringTaggerArguval() + "ids=" + "\"" + String.join( ",",ids) + "\"" + " ";
    }

    private String getUniProtDatabaseInfoAsString() {
        return getInfoStringTaggerArguval() + "from=" + "\"" + "UniProtKB_AC-ID" + "\"" + " ";
    }

    private String getTargetDatabaseInfoAsString(String targetDatabase) {
        return getInfoStringTaggerArguval() + "to=" + "\"" + targetDatabase + "\"" + " ";
    }

    private String getInfoStringTaggerArguval() {
        return "--form ";
    }

    private static String getUniProtRestUrl() {
        return UNIPROT_REST_URL;
    }

    private BufferedReader getCurlReader(String curlCommand, String url) throws IOException {
        Process process = Runtime.getRuntime().exec(curlCommand + " " + url);
        return new BufferedReader(new InputStreamReader(process.getInputStream()));
    }

    private long getMinutesToMilliseconds(long minutes) {
        final long minutesToSecondsConversionFactor = 60;

        long seconds = minutes * minutesToSecondsConversionFactor;

        return getSecondsToMilliseconds(seconds);
    }

    private long getSecondsToMilliseconds(long seconds) {
        final long secondsToMillisecondsConversionFactor = 1000;

        return seconds * secondsToMillisecondsConversionFactor;
    }
}
