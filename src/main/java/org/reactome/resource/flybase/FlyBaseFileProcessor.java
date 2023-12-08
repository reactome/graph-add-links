package org.reactome.resource.flybase;

import org.reactome.resource.FileProcessor;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 4/3/2022
 */
public class FlyBaseFileProcessor implements FileProcessor {
    private Path filePath;
    private Map<String, Set<String>> uniProtToResourceIdentifiers;

    public FlyBaseFileProcessor(Path filePath) {
        this.filePath = filePath;
    }

//    public Set<String> getUniProtIdentifiers() throws IOException {
//        return ge().keySet();
//    }

    @Override
    public Map<String, Set<String>> getSourceToResourceIdentifiers() throws IOException {
        if (uniProtToResourceIdentifiers == null || uniProtToResourceIdentifiers.isEmpty()) {
            this.uniProtToResourceIdentifiers = new HashMap<>();

            getFileLines().stream().filter(line -> !line.startsWith("##")).forEach(line -> {
                //System.out.println("Line: " + line);
                String[] lineColumns = line.split("\t");

                if (lineColumns.length >= 6) {
                    String flyBaseId = lineColumns[2];
                    String uniProtId = lineColumns[5];

                    if (!uniProtId.isEmpty() && !flyBaseId.isEmpty()) {
                        this.uniProtToResourceIdentifiers.computeIfAbsent(uniProtId, k -> new HashSet<>())
                            .add(flyBaseId);
                    }
                }
            });
        }

        return this.uniProtToResourceIdentifiers;
    }

    private List<String> getFileLines() throws IOException {
        if (getFilePath().toString().endsWith(".gz")) {
            GZIPInputStream gzipInputStream = new GZIPInputStream(Files.newInputStream(getFilePath()));
            InputStreamReader inputStreamReader = new InputStreamReader(gzipInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            return bufferedReader.lines().collect(Collectors.toList());
        }

        return Files.readAllLines(getFilePath());
    }

    private Path getFilePath() {
        return this.filePath;
    }
}
