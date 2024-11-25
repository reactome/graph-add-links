package org.reactome.verifier;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.reactome.release.verifier.FileUtils.downloadFileFromS3;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 * Created 11/24/2024
 */
public class Utils {

    public static void downloadAddLinksFilesAndSizesListFromS3(int versionNumber) {
        if (Files.notExists(Paths.get(getAddLinksFilesAndSizesListName()))) {
            downloadFileFromS3("reactome", getAddLinksFilesAndSizesListPathInS3(versionNumber));
        }
    }

    private static String getAddLinksFilesAndSizesListPathInS3(int versionNumber) {
        return String.format("private/releases/%d/add_links/downloads/data/%s",
            versionNumber ,getAddLinksFilesAndSizesListName()
        );
    }

    static String getAddLinksFilesAndSizesListName() {
        return "files_and_sizes.txt";
    }
}
