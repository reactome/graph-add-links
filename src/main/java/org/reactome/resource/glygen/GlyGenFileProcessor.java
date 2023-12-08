package org.reactome.resource.glygen;

import org.apache.commons.csv.CSVParser;
import org.reactome.resource.FileProcessor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.reactome.utils.FileUtils.getCSVParser;
import static org.reactome.utils.FileUtils.unzipFile;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 11/20/2023
 */
public class GlyGenFileProcessor implements FileProcessor {
    private static final int ENSEMBL_GENE_IDENTIFIER_INDEX = 0;
    private static final int UNIPROT_IDENTIFIER_INDEX = 2;

    private Path filePath;
    private Map<String, Set<String>> uniProtToResourceIdentifiers;

    public GlyGenFileProcessor(Path filePath) throws IOException {
        this.filePath = filePath;
    }

    @Override
    public Map<String, Set<String>> getSourceToResourceIdentifiers() throws IOException {
        if (uniProtToResourceIdentifiers == null || uniProtToResourceIdentifiers.isEmpty()) {
            this.uniProtToResourceIdentifiers = new HashMap<>();

            try(CSVParser parser = getCSVParser(getFilePath())) {
                parser.forEach(line -> {
                    String uniProtIdentifier = line.get("uniprotkb_ac");
                    this.uniProtToResourceIdentifiers.computeIfAbsent(uniProtIdentifier, k -> new HashSet<>())
                        .add(uniProtIdentifier);
                });
            }
        }

        return this.uniProtToResourceIdentifiers;
    }

    private Path getFilePath() {
        return this.filePath;
    }
}
