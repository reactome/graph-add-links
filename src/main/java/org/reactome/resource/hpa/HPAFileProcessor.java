package org.reactome.resource.hpa;

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
public class HPAFileProcessor implements FileProcessor {
    private Path filePath;
    private Map<String, Set<String>> uniProtToResourceIdentifiers;

    public HPAFileProcessor(Path filePath) throws IOException {
        this.filePath = filePath;
    }

    @Override
    public Map<String, Set<String>> getSourceToResourceIdentifiers() throws IOException {
        unzipFile(getFilePath());

        if (uniProtToResourceIdentifiers == null || uniProtToResourceIdentifiers.isEmpty()) {
            this.uniProtToResourceIdentifiers = new HashMap<>();

            Files.lines(getUnzippedFilePath(), StandardCharsets.ISO_8859_1).skip(1).forEach(line -> {
                String[] lineColumns = line.split("\t");
                String geneName = lineColumns[0];
                String ensemblGeneId = lineColumns[2];
                String uniProtId = lineColumns[4];
                String hpaId = ensemblGeneId + "-" + geneName;

                if (noneEmpty(geneName, ensemblGeneId, uniProtId)) {
                    this.uniProtToResourceIdentifiers.computeIfAbsent(uniProtId, k -> new HashSet<>()).add(hpaId);
                }
            });
        }

        return this.uniProtToResourceIdentifiers;
    }

    private Path getFilePath() {
        return this.filePath;
    }

    private Path getUnzippedFilePath() {
        String unzippedFilePathAsString = getFilePath().toString().replace(".zip","");
        return Paths.get(unzippedFilePathAsString);
    }

    private boolean noneEmpty(String ...identifiers) {
        return Arrays.stream(identifiers).noneMatch(String::isEmpty);
    }
}
