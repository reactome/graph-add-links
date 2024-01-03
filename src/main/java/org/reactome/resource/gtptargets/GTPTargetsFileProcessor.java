package org.reactome.resource.gtptargets;

import org.reactome.resource.FileProcessor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 11/20/2023
 */
public class GTPTargetsFileProcessor implements FileProcessor {
    private static final int RGD_IDENTIFIER_INDEX = 1;
    private static final int UNIPROT_IDENTIFIER_INDEX = 21;

    private Path filePath;
    private Map<String, Set<String>> uniProtToResourceIdentifiers;

    public GTPTargetsFileProcessor(Path filePath) throws IOException {
        this.filePath = filePath;
    }

    @Override
    public Map<String, Set<String>> getSourceToResourceIdentifiers() throws IOException {
        if (uniProtToResourceIdentifiers == null || uniProtToResourceIdentifiers.isEmpty()) {
            this.uniProtToResourceIdentifiers = new HashMap<>();

            Files.lines(getFilePath(), StandardCharsets.ISO_8859_1).skip(1).forEach(line -> {
                if (isFileBodyLine(line)) {
                    String[] lineColumns = line.split("\t");
                    String rgdId = lineColumns[RGD_IDENTIFIER_INDEX];
                    String uniProtIdsString = lineColumns.length > UNIPROT_IDENTIFIER_INDEX ?
                        lineColumns[UNIPROT_IDENTIFIER_INDEX] : "";

                    if (!uniProtIdsString.isEmpty()) {
                        String[] uniProtIds = uniProtIdsString.replaceAll("\"", "").split("\\|");
                        for (String uniProtId : uniProtIds) {
                            this.uniProtToResourceIdentifiers.computeIfAbsent(uniProtId, k -> new HashSet<>())
                                .add(rgdId);
                        }
                    }
                }
            });
        }

        return this.uniProtToResourceIdentifiers;
    }

    private Path getFilePath() {
        return this.filePath;
    }

    private boolean isFileBodyLine(String line) {
        return Character.isDigit(line.charAt(0));
    }
}
