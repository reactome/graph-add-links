package org.reactome.resource.pro;

import org.reactome.resource.FileProcessor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.reactome.utils.UniProtUtils.isValidUniProtId;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 11/19/2023
 */
public class PROFileProcessor implements FileProcessor {
    private Path filePath;
    private Map<String, Set<String>> uniProtToResourceIdentifiers;

    public PROFileProcessor(Path filePath) {
        this.filePath = filePath;
    }

    @Override
    public Map<String, Set<String>> getSourceToResourceIdentifiers() throws IOException {
        if (uniProtToResourceIdentifiers == null || uniProtToResourceIdentifiers.isEmpty()) {
            this.uniProtToResourceIdentifiers = new HashMap<>();

            Files.readAllLines(getFilePath()).forEach(line -> {
                String[] lineColumns = line.split("\t");

                if (lineColumns.length >= 2) {
                    String proId = lineColumns[0].replace("PR:", "");
                    String uniProtId = lineColumns[1].replace("UniProtKB:","");

                    if (!uniProtId.isEmpty() && isValidUniProtId(uniProtId) && !proId.isEmpty() && isValidUniProtId(proId)) {
                        this.uniProtToResourceIdentifiers.computeIfAbsent(uniProtId, k -> new HashSet<>()).add(proId);
                    }
                }
            });
        }

        return this.uniProtToResourceIdentifiers;
    }

    private Path getFilePath() {
        return this.filePath;
    }
}
