package org.reactome.resource.ctdgene;

import org.apache.commons.csv.CSVParser;
import org.reactome.fileprocessors.UniProtFileProcessor;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.reactome.utils.FileUtils.getCSVParser;
import static org.reactome.utils.FileUtils.getPathWithoutZipExtension;
import static org.reactome.utils.FileUtils.gunzipFile;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 11/22/2023
 */
public class CTDGeneFileProcessor extends UniProtFileProcessor {
    private Set<String> ctdIdentifiers;

    public CTDGeneFileProcessor(Path ...filePaths) throws IOException {
        super(filePaths[0]);
        this.ctdIdentifiers = getCTDIdentifiers(filePaths[1]);
    }

    @Override
    public Map<String, Set<String>> getSourceToResourceIdentifiers() throws IOException {
        Map<String, Set<String>> uniProtToCTDIdentifiers = super.getSourceToResourceIdentifiers();

        Map<String, Set<String>> filteredUniProtToCTDIdentifiers = new HashMap<>();
        for (String uniProtIdentifier : uniProtToCTDIdentifiers.keySet()) {
            Set<String> ctdIdentifiers = uniProtToCTDIdentifiers.get(uniProtIdentifier);
            for (String ctdIdentifier : ctdIdentifiers) {
                if (this.ctdIdentifiers.contains(ctdIdentifier)) {
                    filteredUniProtToCTDIdentifiers.computeIfAbsent(uniProtIdentifier, k -> new HashSet<>())
                        .add(ctdIdentifier);
                }
            }
        }

        return filteredUniProtToCTDIdentifiers;
    }

    private Set<String> getCTDIdentifiers(Path ctdFilePath) throws IOException {
        gunzipFile(ctdFilePath);

        String[] headers = {
            "ChemicalName",
            "ChemicalID",
            "CasRN",
            "GeneSymbol",
            "GeneID",
            "GeneForms",
            "Organism",
            "OrganismID",
            "Interaction",
            "InteractionActions",
            "PubMedIDs"
        };

        Set<String> ctdIdentifiers = new HashSet<>();

        try (CSVParser parser = getCSVParser(getPathWithoutZipExtension(ctdFilePath),'#', headers)) {
            parser.forEach(line -> {
                String geneId = line.get("GeneID");
                if (isValidGeneId(geneId)) {
                    ctdIdentifiers.add(geneId);
                }
            });
        }
        return ctdIdentifiers;
    }

    private boolean isValidGeneId(String geneId) {
        Pattern validGeneIdPattern = Pattern.compile("^\\d+$");
        Matcher validGeneIdMatcher = validGeneIdPattern.matcher(geneId);
        return validGeneIdMatcher.find();
    }
}
