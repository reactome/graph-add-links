package org.reactome.resource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 11/22/2023
 */
public class UniProtFileProcessor implements FileProcessor {

    private Path filePath;
    private Map<String, Set<String>> uniProtToResourceIdentifiers;

    public UniProtFileProcessor(Path filePath) throws IOException {
        this.filePath = filePath;
    }

    @Override
    public Map<String, Set<String>> getSourceToResourceIdentifiers() throws IOException {
        if (this.uniProtToResourceIdentifiers == null || this.uniProtToResourceIdentifiers.isEmpty()) {
            this.uniProtToResourceIdentifiers = new HashMap<>();

            Files.lines(getFilePath()).skip(1).forEach(line -> {
                String[] lineColumns = line.split("\t");
                String uniProtId = lineColumns[0];
                String resourceId = lineColumns[1];

                this.uniProtToResourceIdentifiers.computeIfAbsent(uniProtId, k -> new HashSet<>())
                    .add(resourceId);
            });
        }

        return this.uniProtToResourceIdentifiers;
    }

    private Path getFilePath() {
        return this.filePath;
    }
}
