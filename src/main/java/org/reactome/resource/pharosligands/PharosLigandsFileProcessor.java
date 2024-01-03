package org.reactome.resource.pharosligands;

import org.reactome.resource.FileProcessor;

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
public class PharosLigandsFileProcessor implements FileProcessor {

    private Path filePath;
    private Map<String, Set<String>> guideToPharmacologyToResourceIdentifiers;

    public PharosLigandsFileProcessor(Path filePath) throws IOException {
        this.filePath = filePath;
    }

    @Override
    public Map<String, Set<String>> getSourceToResourceIdentifiers() throws IOException {
        if (guideToPharmacologyToResourceIdentifiers == null || guideToPharmacologyToResourceIdentifiers.isEmpty()) {
            this.guideToPharmacologyToResourceIdentifiers = new HashMap<>();

            Files.lines(getFilePath(), StandardCharsets.ISO_8859_1).forEach(line -> {
                String[] lineColumns = line.split("\t");
                String guideToPharmacologyIdentifier = lineColumns[0];
                String resourceId = lineColumns[1];

                this.guideToPharmacologyToResourceIdentifiers.computeIfAbsent(guideToPharmacologyIdentifier, k -> new HashSet<>())
                    .add(resourceId);
            });
        }

        return this.guideToPharmacologyToResourceIdentifiers;
    }

    private Path getFilePath() {
        return this.filePath;
    }
}
