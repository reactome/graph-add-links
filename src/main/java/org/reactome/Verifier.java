package org.reactome;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.io.FilenameUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.reactome.utils.ResourceJSONParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.reactome.release.verifier.FileUtils.downloadFileFromS3;
import static org.reactome.release.verifier.FileUtils.gunzipFile;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 * Created 8/9/2024
 */
public class Verifier {
    @Parameter(names = {"--download_directory", "-dd"})
    private String downloadDirectory;

    @Parameter(names = {"--release", "-r"})
    private int releaseNumber;

    private String previousDownloadDirectory;

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
        preparePreviousReleaseDownloadDirectory();

        List<String> tooSmallFiles = new ArrayList<>();
        for (String expectedFileName : getExpectedFileNames()) {
            Path currentFileNamePath = Paths.get(this.downloadDirectory, expectedFileName);
            Path previousFileNamePath = Paths.get(this.previousDownloadDirectory, expectedFileName);

            if (currentFileIsSmaller(currentFileNamePath, previousFileNamePath)) {
                tooSmallFiles.add(currentFileNamePath.toString());
            }
        }
        return tooSmallFiles;
    }

    private void preparePreviousReleaseDownloadDirectory() throws IOException {
        downloadFileFromS3("reactome", getAddLinksDownloadTarFilePathInS3());
        gunzipFile(Paths.get(getAddLinksDownloadTarFileName() + ".gz"));
        unTarFile(getAddLinksDownloadTarFileName());
        this.previousDownloadDirectory = getAddLinksDownloadTarBaseName();
    }

    private String getAddLinksDownloadTarFilePathInS3() {
        return String.format("private/releases/%d/add_links/downloads/data/%s",
            getPreviousReleaseNumber(), getAddLinksDownloadTarFileName()
        );
    }

    private String getAddLinksDownloadTarFileName() {
        return String.format("addlinks-downloads-v%d.tar", getPreviousReleaseNumber());
    }

    private String getAddLinksDownloadTarBaseName() {
        return FilenameUtils.getBaseName(getAddLinksDownloadTarFileName());
    }

    private void unTarFile(String fileName) throws IOException {
        try (FileInputStream fis = new FileInputStream(fileName);
             TarArchiveInputStream tis = new TarArchiveInputStream(fis)) {

            TarArchiveEntry entry;
            while ((entry = tis.getNextTarEntry()) != null) {
                File outputFile = new File(FilenameUtils.getBaseName(fileName), entry.getName());

                if (entry.isDirectory()) {
                    outputFile.mkdirs();
                } else {
                    // Ensure parent directories are created
                    outputFile.getParentFile().mkdirs();
                    try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = tis.read(buffer)) > 0) {
                            fos.write(buffer, 0, length);
                        }
                    }
                }
            }
        }
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

    private boolean currentFileIsSmaller(Path currentFileNamePath, Path previousFileNamePath) throws IOException {
        long actualFileSizeInBytes = Files.size(currentFileNamePath);
        long expectedFileSizeInBytes = Files.size(previousFileNamePath);

        return actualFileSizeInBytes < expectedFileSizeInBytes;
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
