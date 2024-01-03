package org.reactome.resource.gtpligands;

import org.apache.commons.csv.CSVParser;
import org.reactome.resource.FileProcessor;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

import static org.reactome.utils.FileUtils.getCSVParser;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 11/20/2023
 */
public class GuideToPharmacologyLigandsFileProcessor implements FileProcessor {
    private static final int RGD_IDENTIFIER_INDEX = 1;
    private static final int UNIPROT_IDENTIFIER_INDEX = 21;

    private Path filePath;
    private Map<String, Set<String>> uniProtToResourceIdentifiers;

    public GuideToPharmacologyLigandsFileProcessor(Path filePath) throws IOException {
        this.filePath = filePath;
    }

    @Override
    public Map<String, Set<String>> getSourceToResourceIdentifiers() throws IOException {
        if (uniProtToResourceIdentifiers == null || uniProtToResourceIdentifiers.isEmpty()) {
            this.uniProtToResourceIdentifiers = new HashMap<>();

            try(CSVParser parser = getCSVParser(getFilePath())) {
                parser.forEach(line -> {
                    String chebiId = line.get("Chebi ID").replace("ChEBI:","");
                    String ligandId = line.get("Ligand id");
                    this.uniProtToResourceIdentifiers.computeIfAbsent(chebiId, k -> new HashSet<>())
                        .add(ligandId);
                });
            }
        }

        return this.uniProtToResourceIdentifiers;
    }

    private Path getFilePath() {
        return this.filePath;
    }

}
