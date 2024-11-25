package org.reactome.verifier;

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

import static org.reactome.verifier.TooSmallFile.currentFileIsSmaller;

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
        List<TooSmallFile> tooSmallFiles = getTooSmallFiles();

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

    private List<TooSmallFile> getTooSmallFiles() throws IOException {
        Utils.downloadAddLinksFilesAndSizesListFromS3(getPreviousReleaseNumber());

        List<TooSmallFile> tooSmallFiles = new ArrayList<>();
        for (String expectedFileName : getExpectedFileNames()) {
            Path currentFileNamePath = Paths.get(this.downloadDirectory, expectedFileName);

            if (Files.exists(currentFileNamePath) && currentFileIsSmaller(currentFileNamePath)) {
                tooSmallFiles.add(new TooSmallFile(currentFileNamePath));
            }
        }
        return tooSmallFiles;
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
