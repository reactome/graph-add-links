package org.reactome.resource.zincsubstances;

import org.reactome.resource.FileProcessor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.reactome.utils.UniProtUtils.isValidUniProtId;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 1/30/2024
 */
public class ZincSubstancesFileProcessor implements FileProcessor {
    private static final int ZINC_IDENTIFIER_INDEX = 0;
    private static final int UNIPROT_IDENTIFIER_INDEX = 2;

    private Path filePath;
    private Map<String, Set<String>> uniProtToResourceIdentifiers;

    public ZincSubstancesFileProcessor(Path filePath) throws IOException {
        this.filePath = filePath;
    }

    @Override
    public Map<String, Set<String>> getSourceToResourceIdentifiers() throws IOException {
        if (uniProtToResourceIdentifiers == null || uniProtToResourceIdentifiers.isEmpty()) {
            this.uniProtToResourceIdentifiers = new HashMap<>();

            Files.lines(getFilePath(), StandardCharsets.ISO_8859_1).skip(1).forEach(line -> {
                String[] lineColumns = line.split(",");
                String zincId = lineColumns[ZINC_IDENTIFIER_INDEX];
                String uniprotId = lineColumns.length > UNIPROT_IDENTIFIER_INDEX ?
                    lineColumns[UNIPROT_IDENTIFIER_INDEX] : "";

                if (!uniprotId.isEmpty() && isValidUniProtId(uniprotId)) {
                    this.uniProtToResourceIdentifiers
                        .computeIfAbsent(uniprotId, k -> new HashSet<>())
                        .add(zincId);
                }
            });
        }

        return this.uniProtToResourceIdentifiers;
    }

    private Path getFilePath() {
        return this.filePath;
    }
}
