package org.reactome.resource.mgi;

import org.reactome.fileprocessors.FileProcessor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 11/20/2023
 */
public class MGIFileProcessor implements FileProcessor {
    private static final int MGI_IDENTIFIER_INDEX = 0;
    private static final int UNIPROT_IDENTIFIER_INDEX = 6;

    private Path filePath;
    private Map<String, Set<String>> uniProtToResourceIdentifiers;

    public MGIFileProcessor(Path filePath) throws IOException {
        this.filePath = filePath;
    }

    @Override
    public Map<String, Set<String>> getSourceToResourceIdentifiers() throws IOException {
        if (uniProtToResourceIdentifiers == null || uniProtToResourceIdentifiers.isEmpty()) {
            this.uniProtToResourceIdentifiers = new HashMap<>();

            Files.lines(getUnzippedFilePath(), StandardCharsets.ISO_8859_1).forEach(line -> {
                String[] lineColumns = line.split("\t");
                String mgiId = lineColumns[MGI_IDENTIFIER_INDEX].replace("MGI:","");
                String uniProtIdsString = lineColumns.length > UNIPROT_IDENTIFIER_INDEX ?
                    lineColumns[UNIPROT_IDENTIFIER_INDEX] : "";

                if (!uniProtIdsString.isEmpty()) {
                    String[] uniProtIds = uniProtIdsString.split(" ");
                    for (String uniProtId : uniProtIds) {
                        this.uniProtToResourceIdentifiers.computeIfAbsent(uniProtId, k -> new HashSet<>())
                            .add(mgiId);
                    }
                }
            });
        }

        return this.uniProtToResourceIdentifiers;
    }

    private Path getUnzippedFilePath() {
        String unzippedFilePathAsString = getFilePath().toString().replace(".gz","");
        return Paths.get(unzippedFilePathAsString);
    }

    private Path getFilePath() {
        return this.filePath;
    }
}
