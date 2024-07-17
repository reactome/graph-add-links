package org.reactome;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 * Created 7/16/2024
 */
public interface IdentifierCreator {

    void writeCSV() throws Exception;
    void readCSV() throws Exception;

    default void insertIdentifiers() throws Exception {
        writeCSV();
        readCSV();
    }
}
