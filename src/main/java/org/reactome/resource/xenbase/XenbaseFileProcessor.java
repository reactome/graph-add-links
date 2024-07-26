package org.reactome.resource.xenbase;

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
public class XenbaseFileProcessor implements FileProcessor {
    private static final int UNIPROT_IDENTIFIER_INDEX = 0;
    private static final int XENBASE_IDENTIFIER_INDEX = 3;

    private Path filePath;
    private Map<String, Set<String>> uniProtToResourceIdentifiers;

    public XenbaseFileProcessor(Path filePath) throws IOException {
        this.filePath = filePath;
    }

    @Override
    public Map<String, Set<String>> getSourceToResourceIdentifiers() throws IOException {
        if (uniProtToResourceIdentifiers == null || uniProtToResourceIdentifiers.isEmpty()) {
            this.uniProtToResourceIdentifiers = new HashMap<>();

            Files.lines(getFilePath(), StandardCharsets.ISO_8859_1).skip(1).forEach(line -> {
                String[] lineColumns = line.split("\t");
                String uniProtId = lineColumns[UNIPROT_IDENTIFIER_INDEX];
                String xenbaseId = lineColumns.length > XENBASE_IDENTIFIER_INDEX ?
                    lineColumns[XENBASE_IDENTIFIER_INDEX] : "";

                if (!xenbaseId.isEmpty()) {
                    this.uniProtToResourceIdentifiers
                        .computeIfAbsent(uniProtId, k -> new HashSet<>())
                        .add(xenbaseId);
                }
            });
        }

        return this.uniProtToResourceIdentifiers;
    }

    private Path getFilePath() {
        return this.filePath;
    }
}
