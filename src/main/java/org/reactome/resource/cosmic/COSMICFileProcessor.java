package org.reactome.resource.cosmic;

import org.reactome.graphnodes.ReferenceGeneProduct;
import org.reactome.resource.FileProcessor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static org.reactome.utils.FileUtils.getPathWithoutZipExtension;
import static org.reactome.utils.FileUtils.gunzipFile;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 1/1/2024
 */
public class COSMICFileProcessor implements FileProcessor {
    private static final int GENE_NAME_INDEX = 0;
    private static final int HGNC_IDENTIFIER_INDEX = 3;

    private Path filePath;
    private Map<String, Set<String>> uniProtIdentifierToCosmicGeneName;

    public COSMICFileProcessor(Path filePath) {
        this.filePath = filePath;
    }

    @Override
    public Map<String, Set<String>> getSourceToResourceIdentifiers() throws IOException {
        if (this.uniProtIdentifierToCosmicGeneName == null || this.uniProtIdentifierToCosmicGeneName.isEmpty()) {
            Map<String, List<ReferenceGeneProduct>> uniProtIdentifierToReferenceGeneProducts =
                ReferenceGeneProduct.fetchHumanReferenceGeneProducts();
            Set<String> cosmicGeneNames = getCosmicGeneNames();

            this.uniProtIdentifierToCosmicGeneName = uniProtIdentifierToReferenceGeneProducts.entrySet().stream()
                .filter(entry -> entry.getValue().stream().flatMap(rgp -> rgp.getGeneNames().stream()).anyMatch(cosmicGeneNames::contains))
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    entry -> getMatchingGeneNames(
                        cosmicGeneNames,
                        entry.getValue().stream().flatMap(rgp -> rgp.getGeneNames().stream()).collect(Collectors.toList())
                    )
                ));
        }

        return this.uniProtIdentifierToCosmicGeneName;
    }

    private Set<String> getCosmicGeneNames() throws IOException {
        if (!Files.exists(getPathWithoutZipExtension(getFilePath()))) {
            gunzipFile(getFilePath());
        }
        return Files.lines(getPathWithoutZipExtension(getFilePath()), StandardCharsets.ISO_8859_1)
            .skip(1)
            .map(line -> {
                String[] lineColumns = line.split("\t");
                return lineColumns[GENE_NAME_INDEX];
            })
            .collect(Collectors.toSet());
    }

    private Set<String> getMatchingGeneNames(Set<String> cosmicGeneNames, List<String> rgpGeneNames) {
        Set<String> matchingGeneNames = new LinkedHashSet<>();
        for (String rgpGeneName : rgpGeneNames) {
            if (cosmicGeneNames.contains(rgpGeneName)) {
                matchingGeneNames.add(rgpGeneName);
            }
        }
        return matchingGeneNames;
    }

    private Path getFilePath() {
        return this.filePath;
    }
}
