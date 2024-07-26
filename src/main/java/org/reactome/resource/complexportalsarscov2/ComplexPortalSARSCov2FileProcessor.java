package org.reactome.resource.complexportalsarscov2;

import org.reactome.fileprocessors.FileProcessor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 11/20/2023
 */
public class ComplexPortalSARSCov2FileProcessor implements FileProcessor {
    private static final int COMPLEX_PORTAL_IDENTIFIER_INDEX = 0;
    private static final int XREFS_IDENTIFIER_INDEX = 8;

    private Path filePath;
    private Map<String, Set<String>> complexToResourceIdentifiers;

    public ComplexPortalSARSCov2FileProcessor(Path filePath) {
        this.filePath = filePath;
    }

    @Override
    public Map<String, Set<String>> getSourceToResourceIdentifiers() throws IOException {
        if (complexToResourceIdentifiers == null || complexToResourceIdentifiers.isEmpty()) {
            this.complexToResourceIdentifiers = new HashMap<>();

            Files.lines(getFilePath(), StandardCharsets.ISO_8859_1).skip(1).forEach(line -> {
                String[] lineColumns = line.split("\t");
                String complexPortalId = lineColumns[COMPLEX_PORTAL_IDENTIFIER_INDEX];
                String xrefIdsString = lineColumns.length > XREFS_IDENTIFIER_INDEX ?
                    lineColumns[XREFS_IDENTIFIER_INDEX] : "";

                if (!xrefIdsString.isEmpty()) {
                    List<String> reactomeComplexStableIds = Arrays.stream(xrefIdsString.split("\\|"))
                        .filter(xref -> xref.startsWith("reactome"))
                        .map(xref -> xref.replace("reactome:","").replaceAll("\\(.*\\)",""))
                        .collect(Collectors.toList());

                    for (String reactomeComplexStableId : reactomeComplexStableIds) {
                        this.complexToResourceIdentifiers.computeIfAbsent(reactomeComplexStableId, k -> new HashSet<>())
                            .add(complexPortalId);
                    }
                }
            });
        }

        return this.complexToResourceIdentifiers;
    }

    private Path getFilePath() {
        return this.filePath;
    }
}
