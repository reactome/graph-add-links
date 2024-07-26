package org.reactome.resource.hmdbmetabolite;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.reactome.fileprocessors.FileProcessor;

import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.reactome.utils.FileUtils.unzipFile;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 11/20/2023
 */
public class HMDBMetaboliteFileProcessor implements FileProcessor {
    private static final Logger logger = LogManager.getLogger();

    private Path filePath;
    private Map<String, Set<String>> chEBIToResourceIdentifiers;

    public HMDBMetaboliteFileProcessor(Path filePath) throws IOException {
        this.filePath = filePath;
    }

    @Override
    public Map<String, Set<String>> getSourceToResourceIdentifiers() throws IOException {
        if (chEBIToResourceIdentifiers == null || chEBIToResourceIdentifiers.isEmpty()) {
            unzipFile(getFilePath());
            transformHMDBMetaboliteXML();

            this.chEBIToResourceIdentifiers = new HashMap<>();

            Files.readAllLines(getTransformedFilePath()).forEach(line -> {
                String[] lineColumns = line.split("\t");

                if (lineColumns.length >= 2) {
                    String hmdbMetaboliteId = lineColumns[0];
                    String chEBIId = lineColumns[1];

                    if (!chEBIId.isEmpty() && !hmdbMetaboliteId.isEmpty()) {
                        this.chEBIToResourceIdentifiers.computeIfAbsent(
                            chEBIId, k -> new HashSet<>()).add(hmdbMetaboliteId);
                    }
                }
            });
        }

        return this.chEBIToResourceIdentifiers;
    }

//    private void unzipHMDBMetaboliteXMLFile() throws IOException {
//        byte[] buffer = new byte[1024];
//        ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(getFilePath().toString()));
//        ZipEntry zipEntry = zipInputStream.getNextEntry();
//        while (zipEntry != null) {
//            File newFile = new File(getFilePath().getParent().toFile(), zipEntry.getName());
//            FileOutputStream fileOutputStream = new FileOutputStream(newFile);
//            int len;
//            while ((len = zipInputStream.read(buffer)) > 0) {
//                fileOutputStream.write(buffer, 0, len);
//            }
//            fileOutputStream.close();
//            zipEntry = zipInputStream.getNextEntry();
//        }
//        zipInputStream.closeEntry();
//        zipInputStream.close();
//    }

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
