package org.reactome.resource.gtptargets;

import org.apache.commons.csv.CSVParser;
import org.reactome.resource.FileProcessor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.reactome.utils.FileUtils.getCSVParser;
import static org.reactome.utils.UniProtUtils.isValidUniProtId;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 11/20/2023
 */
public class GTPTargetsFileProcessor implements FileProcessor {
    //private static final int GTP_TARGET_IDENTIFIER_INDEX = 3;
    //private static final int UNIPROT_IDENTIFIER_INDEX = 16;

    private Path filePath;
    private Map<String, Set<String>> uniProtToResourceIdentifiers;

    public GTPTargetsFileProcessor(Path filePath) throws IOException {
        this.filePath = filePath;
    }

    @Override
    public Map<String, Set<String>> getSourceToResourceIdentifiers() throws IOException {
        if (uniProtToResourceIdentifiers == null || uniProtToResourceIdentifiers.isEmpty()) {
            this.uniProtToResourceIdentifiers = new HashMap<>();

            CSVParser parser = getCSVParser(getFilePath());
            parser.forEach(line -> {
                String targetID = line.get("Target id");
                String uniprotID = line.get("Human SwissProt");
                if (isValidUniProtId(uniprotID)) {
                    this.uniProtToResourceIdentifiers.computeIfAbsent(uniprotID, k -> new HashSet<>()).add(targetID);
                }
            });
        }

        System.out.println(this.uniProtToResourceIdentifiers);
        return this.uniProtToResourceIdentifiers;
    }

    private Path getFilePath() {
        return this.filePath;
    }
}
