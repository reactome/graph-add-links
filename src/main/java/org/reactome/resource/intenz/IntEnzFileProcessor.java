package org.reactome.resource.intenz;

import org.reactome.resource.FileProcessor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 11/20/2023
 */
public class IntEnzFileProcessor implements FileProcessor {
    private Path filePath;
    private Map<String, Set<String>> uniProtToResourceIdentifiers;

    public IntEnzFileProcessor(Path filePath) throws IOException {
        this.filePath = filePath;
    }

    @Override
    public Map<String, Set<String>> getSourceToResourceIdentifiers() throws IOException {
        if (uniProtToResourceIdentifiers == null || uniProtToResourceIdentifiers.isEmpty()) {
            this.uniProtToResourceIdentifiers = new HashMap<>();

            String ecNumber = "";
            for (String line : Files.readAllLines(getFilePath())) {
                // New mapping entry
                if (line.startsWith("//")) {
                    ecNumber = "";
                }

                // EC Number for mapping entry
                if (line.startsWith("ID")) {
                    line = line.replaceAll("^ID\\W*", "");
                    ecNumber = line.trim();
                }

                // UniProt accessions mapping to EC number
                if (line.startsWith("DR")) {
                    line = line.replaceFirst("^DR\\W*", "");
                    String[] uniProtEntries = line.split(";");
                    for (String uniProtEntry : uniProtEntries) {
                        String[] uniProtAccessionAndGeneName = uniProtEntry.split(",");
                        String uniProtAccession = uniProtAccessionAndGeneName[0];

                        if (ecNumber.isEmpty()) {
                            throw new RuntimeException("UniProt accessions found for entry without ecNumber");
                        }
                        this.uniProtToResourceIdentifiers.computeIfAbsent(uniProtAccession, k -> new HashSet<>())
                            .add(ecNumber);
                    }
                }
            }
        }

        return this.uniProtToResourceIdentifiers;
    }

    private Path getFilePath() {
        return this.filePath;
    }
}
