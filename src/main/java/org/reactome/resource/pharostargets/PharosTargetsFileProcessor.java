package org.reactome.resource.pharostargets;

import org.reactome.fileprocessors.FileProcessor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 11/20/2023
 */
public class PharosTargetsFileProcessor implements FileProcessor {

    private Path filePath;
    private Map<String, Set<String>> uniProtToResourceIdentifiers;

    public PharosTargetsFileProcessor(Path filePath) throws IOException {
        this.filePath = filePath;
    }

    @Override
    public Map<String, Set<String>> getSourceToResourceIdentifiers() throws IOException {
        if (uniProtToResourceIdentifiers == null || uniProtToResourceIdentifiers.isEmpty()) {
            this.uniProtToResourceIdentifiers = new HashMap<>();

            Files.lines(getFilePath(), StandardCharsets.ISO_8859_1).forEach(uniProtIdentifier -> {
                this.uniProtToResourceIdentifiers.computeIfAbsent(uniProtIdentifier, k -> new HashSet<>())
                    .add(uniProtIdentifier);
            });
        }

        return this.uniProtToResourceIdentifiers;
    }

    private Path getFilePath() {
        return this.filePath;
    }
}
