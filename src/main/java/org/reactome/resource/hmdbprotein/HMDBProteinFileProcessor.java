package org.reactome.resource.hmdbprotein;

import org.reactome.resource.FileProcessor;

import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.reactome.utils.FileUtils.unzipFile;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 11/20/2023
 */
public class HMDBProteinFileProcessor implements FileProcessor {
    private Path filePath;
    private Map<String, Set<String>> uniProtToResourceIdentifiers;

    public HMDBProteinFileProcessor(Path filePath) throws IOException {
        this.filePath = filePath;
    }

    @Override
    public Map<String, Set<String>> getSourceToResourceIdentifiers() throws IOException {
        unzipFile(getFilePath());
        transformHMDBProteinXML();

        if (uniProtToResourceIdentifiers == null || uniProtToResourceIdentifiers.isEmpty()) {
            this.uniProtToResourceIdentifiers = new HashMap<>();

            Files.readAllLines(getTransformedFilePath()).forEach(line -> {
                String[] lineColumns = line.split("\t");

                if (lineColumns.length >= 2) {
                    String hmdbProteinId = lineColumns[0];
                    String uniProtId = lineColumns[1];

                    if (!uniProtId.isEmpty() && !hmdbProteinId.isEmpty()) {
                        this.uniProtToResourceIdentifiers.computeIfAbsent(
                            uniProtId, k -> new HashSet<>()).add(hmdbProteinId);
                    }
                }
            });
        }

        return this.uniProtToResourceIdentifiers;
    }

    private void transformHMDBProteinXML() {
        TransformerFactory factory = TransformerFactory.newInstance();

        Source source = new StreamSource(this.getClass().getClassLoader().getResourceAsStream(
            "xml_transformers/hmdb_protein_transform.xsl"));
        Transformer transformer;
        try {
            transformer = factory.newTransformer(source);

            Source xmlSource = new StreamSource(getUnzippedFilePath().toString());
            Result outputTarget =  new StreamResult(new File(getTransformedFilePath().toString()));
            transformer.transform(xmlSource, outputTarget);
        } catch (TransformerException e) {
            throw new RuntimeException("Unable to transform HMDB protein XML" ,e);
        }
    }

    private Path getTransformedFilePath() {
        return Paths.get(getUnzippedFilePath().toString() + ".transformed.tsv");
    }

    private Path getUnzippedFilePath() {
        String unzippedFilePathAsString = getFilePath().toString().replace(".zip",".xml");
        return Paths.get(unzippedFilePathAsString);
    }

    private Path getFilePath() {
        return this.filePath;
    }
}
