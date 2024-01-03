package org.reactome.resource.pharmacodb;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.reactome.resource.FileProcessor;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 12/6/2023
 */
public class PharmacoDBFileProcessor implements FileProcessor {
    private Path guideToPharmacologyMappingFilePath;
    private Path pharmacoDBMappingFilePath;

    public PharmacoDBFileProcessor(Path[] filePaths) {
        if (filePaths.length < 2) {
            throw new IllegalStateException("Two files are required for PharmacoDB - a Guide To Pharmacology " +
                " mapping file and a PharmacoDB mapping file ");
        }

        this.guideToPharmacologyMappingFilePath = filePaths[0];
        this.pharmacoDBMappingFilePath = filePaths[1];
    }

    @Override
    public Map<String, Set<String>> getSourceToResourceIdentifiers() throws IOException {
        Map<String, String> guideToPharmacology2PubChem = processGuideToPharmacologyFile();
        Map<String, String> pubChem2PharmacoDB = processPharmacoDBFile();

        Map<String, Set<String>> guideToPharmacology2PharmacoDB = new HashMap<>();
        for (Map.Entry<String, String> entry : guideToPharmacology2PubChem.entrySet()) {
            String guideToPharmacologyId = entry.getKey();
            String pubChemId = entry.getValue();
            String pharmacoDBId = pubChem2PharmacoDB.get(pubChemId);

            if (isValidPharmacoDBId(pharmacoDBId)) {
                guideToPharmacology2PharmacoDB.computeIfAbsent(guideToPharmacologyId, k -> new HashSet<>())
                    .add(pharmacoDBId);
            }
        }

        return guideToPharmacology2PharmacoDB;
    }

    private Map<String, String> processGuideToPharmacologyFile() {
        Map<String, String> guideToPharmacology2PubChem = new HashMap<>();

        try(CSVParser parser = getCSVParser(getGuideToPharmacologyFilePath())) {
            parser.forEach(record -> guideToPharmacology2PubChem.put(
                record.get("Ligand id"),
                record.get("PubChem CID")
            ));
        } catch (IOException e) {
            throw new RuntimeException("Unable to parse Guide To Pharmacology file", e);
        }

        return guideToPharmacology2PubChem;
    }

    private Map<String, String> processPharmacoDBFile() {
        Map<String, String> pubChem2PharmacoDB = new HashMap<>();

        try (CSVParser parser = new CSVParser(new FileReader(getPharmacoDBMappingFilePath().toString()),
            CSVFormat.DEFAULT.withFirstRecordAsHeader())) {

            parser.forEach(record -> {
                String status = record.get("PharmacoDB.status");
                if (status.equalsIgnoreCase("present")) {
                    pubChem2PharmacoDB.put(
                        record.get("cid"),
                        record.get("PharmacoDB.uid")
                    );
                }
            });
        } catch (IOException e) {
            throw new RuntimeException("Unable to parse PharmacoDB file",e);
        }

        return pubChem2PharmacoDB;
    }

    private boolean isValidPharmacoDBId(String pharmacoDBId) {
        return pharmacoDBId != null &&
            !pharmacoDBId.isEmpty() &&
            !pharmacoDBId.trim().isEmpty() &&
            pharmacoDBId.matches("PDBC\\d+");
    }

    private CSVParser getCSVParser(Path csvFilePath) throws IOException {
        return new CSVParser(
            getReaderAfterVersionHeader(Files.newBufferedReader(csvFilePath)),
            CSVFormat.DEFAULT.withFirstRecordAsHeader()
        );
    }

    private Reader getReaderAfterVersionHeader(BufferedReader reader) throws IOException {
        String header = reader.readLine();
        if (!header.contains("GtoPdb Version")) {
            reader.reset(); // Reset to beginning to include first line if not version header
        }
        return reader; // Return reader at proper position after version header, if exists
    }

    private Path getGuideToPharmacologyFilePath() {
        return this.guideToPharmacologyMappingFilePath;
    }

    private Path getPharmacoDBMappingFilePath() {
        return this.pharmacoDBMappingFilePath;
    }
}
