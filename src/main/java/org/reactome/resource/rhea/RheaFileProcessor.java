package org.reactome.resource.rhea;

import org.reactome.resource.FileProcessor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 11/20/2023
 */
public class RheaFileProcessor implements FileProcessor {
    private static final int RHEA_IDENTIFIER_INDEX = 0;
    private static final int REACTOME_REACTION_IDENTIFIER_INDEX = 3;

    private Path filePath;
    private Map<String, Set<String>> reactomeReactionToResourceIdentifiers;

    public RheaFileProcessor(Path filePath) throws IOException {
        this.filePath = filePath;
    }

    @Override
    public Map<String, Set<String>> getSourceToResourceIdentifiers() throws IOException {
        if (reactomeReactionToResourceIdentifiers == null || reactomeReactionToResourceIdentifiers.isEmpty()) {
            this.reactomeReactionToResourceIdentifiers = new HashMap<>();

            Files.lines(getFilePath(), StandardCharsets.ISO_8859_1).skip(1).forEach(line -> {
                String[] lineColumns = line.split("\t");
                String rheaId = lineColumns[RHEA_IDENTIFIER_INDEX];
                String reactomeReactionId = lineColumns.length > REACTOME_REACTION_IDENTIFIER_INDEX ?
                    lineColumns[REACTOME_REACTION_IDENTIFIER_INDEX] : "";
                reactomeReactionId = removeMinorVersion(reactomeReactionId);

                if (!reactomeReactionId.isEmpty()) {
                    this.reactomeReactionToResourceIdentifiers
                        .computeIfAbsent(reactomeReactionId, k -> new HashSet<>())
                        .add(rheaId);
                }
            });
        }

        return this.reactomeReactionToResourceIdentifiers;
    }

    private Path getFilePath() {
        return this.filePath;
    }

    private String removeMinorVersion(String stableId) {
        return stableId.replaceAll("\\.\\d+", "");
    }
}
