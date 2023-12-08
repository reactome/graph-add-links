package org.reactome.resource.hmdbmetabolite;

import org.reactome.resource.FileProcessor;

import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 11/20/2023
 */
public class HMDBMetaboliteFileProcessor implements FileProcessor {
    private Path filePath;
    private Map<String, Set<String>> uniProtToResourceIdentifiers;

    public HMDBMetaboliteFileProcessor(Path filePath) throws IOException {
        this.filePath = filePath;
    }

    @Override
    public Map<String, Set<String>> getSourceToResourceIdentifiers() throws IOException {
        unzipHMDBMetaboliteXMLFile();
        transformHMDBMetaboliteXML();

        if (uniProtToResourceIdentifiers == null || uniProtToResourceIdentifiers.isEmpty()) {
            this.uniProtToResourceIdentifiers = new HashMap<>();

            Files.readAllLines(getTransformedFilePath()).forEach(line -> {
                String[] lineColumns = line.split("\t");

                if (lineColumns.length >= 2) {
                    String hmdbMetaboliteId = lineColumns[0];
                    String uniProtId = lineColumns[1];

                    if (!uniProtId.isEmpty() && !hmdbMetaboliteId.isEmpty()) {
                        this.uniProtToResourceIdentifiers.computeIfAbsent(
                            uniProtId, k -> new HashSet<>()).add(hmdbMetaboliteId);
                    }
                }
            });
        }

        return this.uniProtToResourceIdentifiers;
    }

    private void unzipHMDBMetaboliteXMLFile() throws IOException {
        byte[] buffer = new byte[1024];
        ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(getFilePath().toString()));
        ZipEntry zipEntry = zipInputStream.getNextEntry();
        while (zipEntry != null) {
            File newFile = new File(getFilePath().getParent().toFile(), zipEntry.getName());
            FileOutputStream fileOutputStream = new FileOutputStream(newFile);
            int len;
            while ((len = zipInputStream.read(buffer)) > 0) {
                fileOutputStream.write(buffer, 0, len);
            }
            fileOutputStream.close();
            zipEntry = zipInputStream.getNextEntry();
        }
        zipInputStream.closeEntry();
        zipInputStream.close();
    }

    private void transformHMDBMetaboliteXML() {
        TransformerFactory factory = TransformerFactory.newInstance();

        Source source = new StreamSource(this.getClass().getClassLoader().getResourceAsStream(
            "xml_transformers/hmdb_metabolites_transform.xsl"));
        Transformer transformer;
        try {
            transformer = factory.newTransformer(source);

            Source xmlSource = new StreamSource(getUnzippedFilePath().toString());
            Result outputTarget =  new StreamResult(new File(getTransformedFilePath().toString()));
            transformer.transform(xmlSource, outputTarget);
        } catch (TransformerException e) {
            throw new RuntimeException("Unable to transform HMDB metabolites XML" ,e);
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
