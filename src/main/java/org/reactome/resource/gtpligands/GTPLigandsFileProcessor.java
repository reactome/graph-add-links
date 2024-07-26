package org.reactome.resource.gtpligands;

import org.apache.commons.csv.CSVParser;
import org.reactome.fileprocessors.FileProcessor;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

import static org.reactome.utils.FileUtils.getCSVParser;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 11/20/2023
 */
public class GTPLigandsFileProcessor implements FileProcessor {
    private Path filePath;
    private Map<String, Set<String>> chEBIToResourceIdentifiers;

    public GTPLigandsFileProcessor(Path filePath) throws IOException {
        this.filePath = filePath;
    }

    @Override
    public Map<String, Set<String>> getSourceToResourceIdentifiers() throws IOException {
        if (this.chEBIToResourceIdentifiers == null || this.chEBIToResourceIdentifiers.isEmpty()) {
            this.chEBIToResourceIdentifiers = new HashMap<>();

            try(CSVParser parser = getCSVParser(getFilePath())) {
                parser.forEach(line -> {
                    String chebiId = line.get("Chebi ID").replace("CHEBI:","");
                    String ligandId = line.get("Ligand id");
                    if (chebiId != null && !chebiId.isEmpty()) {
                        this.chEBIToResourceIdentifiers.computeIfAbsent(chebiId, k -> new HashSet<>())
                            .add(ligandId);
                    }
                });
            }
        }

        return this.chEBIToResourceIdentifiers;
    }

    private Path getFilePath() {
        return this.filePath;
    }

}
