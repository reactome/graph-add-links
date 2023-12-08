package org.reactome.resource.vgnc;

import org.reactome.resource.FileProcessor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.reactome.utils.FileUtils.unzipFile;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 11/20/2023
 */
public class VGNCFileProcessor implements FileProcessor {
    private static final int VGNC_IDENTIFIER_INDEX = 1;
    private static final int UNIPROT_IDENTIFIER_INDEX = 21;

    private Path filePath;
    private Map<String, Set<String>> uniProtToResourceIdentifiers;

    public VGNCFileProcessor(Path filePath) throws IOException {
        this.filePath = filePath;
    }

    @Override
    public Map<String, Set<String>> getSourceToResourceIdentifiers() throws IOException {
        unzipFile(getFilePath());

        if (uniProtToResourceIdentifiers == null || uniProtToResourceIdentifiers.isEmpty()) {
            this.uniProtToResourceIdentifiers = new HashMap<>();

            Files.lines(getUnzippedFilePath(), StandardCharsets.ISO_8859_1).skip(1).forEach(line -> {
                String[] lineColumns = line.split("\t");
                String vgncId = lineColumns[VGNC_IDENTIFIER_INDEX].replace("VGNC:","");
                String uniProtIdsString = lineColumns.length > UNIPROT_IDENTIFIER_INDEX ?
                    lineColumns[UNIPROT_IDENTIFIER_INDEX] : "";

                if (!uniProtIdsString.isEmpty()) {
                    String[] uniProtIds = uniProtIdsString.replaceAll("\"", "").split("\\|");
                    for (String uniProtId : uniProtIds) {
                        this.uniProtToResourceIdentifiers.computeIfAbsent(uniProtId, k -> new HashSet<>())
                            .add(vgncId);
                    }
                }
            });
        }

        return this.uniProtToResourceIdentifiers;
    }

    private Path getUnzippedFilePath() {
        String unzippedFilePathAsString = getFilePath().toString().replace(".gz","");
        return Paths.get(unzippedFilePathAsString);
    }

    private Path getFilePath() {
        return this.filePath;
    }
}
