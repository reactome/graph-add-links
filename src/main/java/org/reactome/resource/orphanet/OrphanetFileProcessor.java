package org.reactome.resource.orphanet;

import org.reactome.resource.FileProcessor;

import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 11/20/2023
 */
public class OrphanetFileProcessor implements FileProcessor {
    private Path filePath;
    private Map<String, Set<String>> uniProtToResourceIdentifiers;

    public OrphanetFileProcessor(Path filePath) throws IOException {
        this.filePath = filePath;
    }

    @Override
    public Map<String, Set<String>> getSourceToResourceIdentifiers() throws IOException {
        transformOrphanetXML();

        if (this.uniProtToResourceIdentifiers == null || this.uniProtToResourceIdentifiers.isEmpty()) {
            this.uniProtToResourceIdentifiers = new HashMap<>();

            Files.readAllLines(getTransformedFilePath()).forEach(line -> {
                String[] lineColumns = line.split(",");

                if (lineColumns.length >= 3) {
                    String orphanetId = lineColumns[0];
                    String uniProtId = lineColumns[2];

                    if (!uniProtId.isEmpty() && !orphanetId.isEmpty()) {
                        this.uniProtToResourceIdentifiers.computeIfAbsent(
                            uniProtId, k -> new HashSet<>()).add(orphanetId);
                    }
                }
            });
        }

        return this.uniProtToResourceIdentifiers;
    }

    private void transformOrphanetXML() {
        TransformerFactory factory = TransformerFactory.newInstance();

        Source source = new StreamSource(this.getClass().getClassLoader().getResourceAsStream(
            "xml_transformers/orphanet_transform.xsl"));
        try {
            Transformer transformer = factory.newTransformer(source);

            Source xmlSource = new StreamSource(getFilePath().toString());
            Result outputTarget =  new StreamResult(new File(getTransformedFilePath().toString()));
            transformer.transform(xmlSource, outputTarget);
        } catch (TransformerException e) {
            throw new RuntimeException("Unable to transform Orphanet XML" ,e);
        }
    }

    private Path getTransformedFilePath() {
        return Paths.get(getFilePath().toString() + ".transformed.csv");
    }

    private Path getFilePath() {
        return this.filePath;
    }
}
