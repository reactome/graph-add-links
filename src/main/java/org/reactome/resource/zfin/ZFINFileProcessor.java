package org.reactome.resource.zfin;

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
public class ZFINFileProcessor implements FileProcessor {
    private static final int ZFIN_IDENTIFIER_INDEX = 0;
    private static final int UNIPROT_IDENTIFIER_INDEX = 3;

    private Path filePath;
    private Map<String, Set<String>> uniProtToResourceIdentifiers;

    public ZFINFileProcessor(Path filePath) throws IOException {
        this.filePath = filePath;
    }

    @Override
    public Map<String, Set<String>> getSourceToResourceIdentifiers() throws IOException {
        if (uniProtToResourceIdentifiers == null || uniProtToResourceIdentifiers.isEmpty()) {
            this.uniProtToResourceIdentifiers = new HashMap<>();

            Files.lines(getFilePath(), StandardCharsets.ISO_8859_1).forEach(line -> {
                String[] lineColumns = line.split("\t");
                String zfinId = lineColumns[ZFIN_IDENTIFIER_INDEX];
                String uniProtIdsString = lineColumns.length > UNIPROT_IDENTIFIER_INDEX ?
                    lineColumns[UNIPROT_IDENTIFIER_INDEX] : "";

                if (!uniProtIdsString.isEmpty()) {
                    String[] uniProtIds = uniProtIdsString.replaceAll("\"", "").split("\\|");
                    for (String uniProtId : uniProtIds) {
                        this.uniProtToResourceIdentifiers.computeIfAbsent(uniProtId, k -> new HashSet<>())
                            .add(zfinId);
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
