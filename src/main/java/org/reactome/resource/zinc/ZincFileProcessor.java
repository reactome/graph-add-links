package org.reactome.resource.zinc;

import org.reactome.resource.FileProcessor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.reactome.utils.FileUtils.unzipFile;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 11/20/2023
 */
public class ZincFileProcessor implements FileProcessor {
    private static final int CHEBI_IDENTIFIER_INDEX = 0;
    private static final int ZINC_IDENTIFIER_INDEX = 1;

    private Path filePath;
    private Map<String, Set<String>> chebiToResourceIdentifiers;

    public ZincFileProcessor(Path filePath) throws IOException {
        this.filePath = filePath;
    }

    @Override
    public Map<String, Set<String>> getSourceToResourceIdentifiers() throws IOException {
        unzipFile(getFilePath());

        if (chebiToResourceIdentifiers == null || chebiToResourceIdentifiers.isEmpty()) {
            this.chebiToResourceIdentifiers = new HashMap<>();

            Files.lines(getUnzippedFilePath(), StandardCharsets.ISO_8859_1).forEach(line -> {
                String[] lineColumns = line.split("\t");
                String chebiId = lineColumns[CHEBI_IDENTIFIER_INDEX].replace("CHEBI:","");
                String zincId = lineColumns.length > ZINC_IDENTIFIER_INDEX ? lineColumns[ZINC_IDENTIFIER_INDEX] : "";

                if (!zincId.isEmpty()) {
                    this.chebiToResourceIdentifiers.computeIfAbsent(chebiId, k -> new HashSet<>())
                        .add(zincId);
                }
            });
        }

        return this.chebiToResourceIdentifiers;
    }

    private Path getUnzippedFilePath() {
        String unzippedFilePathAsString = getFilePath().toString().replace(".gz","");
        return Paths.get(unzippedFilePathAsString);
    }

    private Path getFilePath() {
        return this.filePath;
    }
}
