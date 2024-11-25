package org.reactome;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import org.json.JSONArray;
import org.json.JSONObject;
import org.reactome.utils.ResourceJSONParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.reactome.release.verifier.FileUtils.downloadFileFromS3;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 * Created 8/9/2024
 */
public class Verifier {
    @Parameter(names = {"--download_directory", "-dd"})
    private String downloadDirectory;

    @Parameter(names = {"--release", "-r"})
    private int releaseNumber;

    public static void main(String[] args) throws IOException {
        Verifier verifier = new Verifier();
        JCommander.newBuilder()
            .addObject(verifier)
            .build()
            .parse(args);
        verifier.run();
    }

    private void run() throws IOException {
        List<String> missingFiles = getMissingFiles();
        List<String> tooSmallFiles = getTooSmallFiles();

        if (missingFiles.isEmpty() && tooSmallFiles.isEmpty()) {
            System.out.println("All graph-add-links files downloaded successfully!");
        } else {
            if (!missingFiles.isEmpty()) {
                System.err.println("The following files are MISSING from the graph-add-links downloads:");

                missingFiles.forEach(System.err::println);

                System.err.println(); // Extra new line for formatting
            }

            if (!tooSmallFiles.isEmpty()) {
                System.err.println("The following files are TOO SMALL from the graph-add-links downloads:");

                tooSmallFiles.forEach(System.err::println);
            }

            System.exit(1);
        }
    }

    private List<String> getTooSmallFiles() throws IOException {
        if (Files.notExists(Paths.get(getAddLinksFilesAndSizesListName()))) {
            downloadFileFromS3("reactome", getAddLinksFilesAndSizesListPathInS3());
        }
        List<String> tooSmallFiles = new ArrayList<>();
        for (String expectedFileName : getExpectedFileNames()) {
            Path currentFileNamePath = Paths.get(this.downloadDirectory, expectedFileName);

            if (currentFileIsSmaller(currentFileNamePath)) {
                tooSmallFiles.add(currentFileNamePath.toString());
            }
        }
        return tooSmallFiles;
    }

    private String getAddLinksFilesAndSizesListPathInS3() {
        return String.format("private/releases/%d/add_links/downloads/data/%s",
            getPreviousReleaseNumber(), getAddLinksFilesAndSizesListName()
        );
    }

    private String getAddLinksFilesAndSizesListName() {
        return "files_and_sizes.txt";
    }

    private int getPreviousReleaseNumber() {
        return this.releaseNumber - 1;
    }

    private List<String> getMissingFiles() {
        List<String> missingFiles = new ArrayList<>();
        for (String expectedFileName : getExpectedFileNames()) {
            Path fileNamePath = Paths.get(this.downloadDirectory, expectedFileName);
            if (!Files.exists(fileNamePath)) {
                missingFiles.add(fileNamePath.toString());
            }
        }
        return missingFiles;
    }

    private boolean currentFileIsSmaller(Path currentFileNamePath) throws IOException {
        long actualFileSizeInBytes = getCurrentFileSize(currentFileNamePath);
        long expectedFileSizeInBytes = getExpectedFileSize(currentFileNamePath);

        return actualFileSizeInBytes < expectedFileSizeInBytes;
    }

    private long getCurrentFileSize(Path currentFileNamePath) {
        try {
            return Files.size(currentFileNamePath);
        } catch (IOException e) {
            // TODO: Add logger statement for unsizable file
            return 0L;
        }
    }

    private long getExpectedFileSize(Path currentFileNamePath) throws IOException {
        long expectedFileSize = getExpectedFileNameToSizeMap().computeIfAbsent(
            currentFileNamePath.getFileName().toString(), k -> 0L);
        //System.out.println(currentFileNamePath + "\t" + expectedFileSize);
        return expectedFileSize;
    }

    private Map<String, Long> getExpectedFileNameToSizeMap() throws IOException {
        return Files.lines(Paths.get(getAddLinksFilesAndSizesListName()))
            .map(line -> line.split(" "))
            .collect(Collectors.toMap(
                this::getFileName,
                this::getFileSizeInBytes
            ));
    }

    private String getFileName(String[] columns) {
        return columns[1].replace("./","");
    }

    private long getFileSizeInBytes(String[] columns) {
        return Long.parseLong(columns[0]);
    }

    private List<String> getExpectedFileNames() {
        List<String> expectedFileNames = new ArrayList<>();

        Map<String, JSONObject> resourceNameToJSON = ResourceJSONParser.getResourceJSONObjects();
        for (String resourceName : resourceNameToJSON.keySet()) {
            JSONObject resourceJSON = resourceNameToJSON.get(resourceName);
            for (JSONObject downloadObject : getDownloadJSONObjects(resourceJSON)) {
                String localFileName = downloadObject.getString("localName");
                expectedFileNames.add(localFileName);
            }
        }

        return expectedFileNames;
    }

    private List<JSONObject> getDownloadJSONObjects(JSONObject resourceJSON) {
        List<JSONObject> downloadJSONObjects = new ArrayList<>();

        JSONArray downloadsJSONArray = resourceJSON.getJSONArray("downloads");
        for (int index = 0; index < downloadsJSONArray.length(); index++) {
            JSONObject downloadJSONObject = downloadsJSONArray.getJSONObject(index);
            downloadJSONObjects.add(downloadJSONObject);
        }

        return downloadJSONObjects;
    }
}
