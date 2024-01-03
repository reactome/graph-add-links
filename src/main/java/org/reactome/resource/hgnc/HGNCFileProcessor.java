package org.reactome.resource.hgnc;

import org.reactome.resource.FileProcessor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 12/27/2023
 */
public class HGNCFileProcessor implements FileProcessor {
    private static final int HGNC_IDENTIFIER_INDEX = 0;
    private static final int UNIPROT_IDENTIFIER_INDEX = 25;

    private Path filePath;
    private Map<String, Set<String>> uniProtToResourceIdentifiers;

    public HGNCFileProcessor(Path filePath) throws IOException {
        this.filePath = filePath;
    }

    @Override
    public Map<String, Set<String>> getSourceToResourceIdentifiers() throws IOException {
        if (this.uniProtToResourceIdentifiers == null || this.uniProtToResourceIdentifiers.isEmpty()) {
            this.uniProtToResourceIdentifiers = new HashMap<>();

            Files.lines(getFilePath(), StandardCharsets.ISO_8859_1).skip(1).forEach(line -> {
                String[] lineColumns = line.split("\t");
                String hgncId = lineColumns[HGNC_IDENTIFIER_INDEX].replace("HGNC:","");
                String uniprotString = lineColumns.length > UNIPROT_IDENTIFIER_INDEX ?
                    lineColumns[UNIPROT_IDENTIFIER_INDEX] : "";

                if (!uniprotString.isEmpty()) {
                    List<String> uniProtIds = Arrays.stream(uniprotString.split("\\|"))
                        .map(id -> id.replaceAll("\"", ""))
                        .collect(Collectors.toList());

                    for (String uniProtId : uniProtIds) {
                        this.uniProtToResourceIdentifiers.computeIfAbsent(uniProtId, k -> new HashSet<>())
                            .add(hgncId);
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
