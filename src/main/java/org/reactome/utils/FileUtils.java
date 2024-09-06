package org.reactome.utils;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 11/20/2023
 */
public class FileUtils {

    public static void unzipFile(Path filePath) throws IOException {
        if (filePath.toString().endsWith(".gz")) {
            gunzipFile(filePath);
            return;
        }

        extractZip(filePath.toFile());
    }



    public static void gunzipFile(Path filePath) throws IOException {
        if (filePath.toString().endsWith(".gz")) {
            byte[] buffer = new byte[1024];

            GZIPInputStream gzipInputStream = new GZIPInputStream(Files.newInputStream(filePath));
            OutputStream outputStream = Files.newOutputStream(getPathWithoutZipExtension(filePath));
            int len;
            while ((len = gzipInputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, len);
            }
            outputStream.close();
            gzipInputStream.close();
        }

    }

    public static CSVParser getCSVParser(Path csvFilePath) throws IOException {
        return new CSVParser(
            getReaderAfterVersionHeader(Files.newBufferedReader(csvFilePath)),
            CSVFormat.DEFAULT.withFirstRecordAsHeader()
        );
    }

    public static CSVParser getCSVParser(Path csvFilePath, char commentMarker, String ...headers) throws IOException {
        return new CSVParser(
            getReaderAfterVersionHeader(Files.newBufferedReader(csvFilePath)),
            CSVFormat.DEFAULT.withCommentMarker(commentMarker).withHeader(headers)
        );
    }

    public static Path getPathWithoutZipExtension(Path filePath) {
        if (filePath.toString().endsWith(".gz")) {
            return Paths.get(filePath.toString().replace(".gz", ""));
        }

        if (filePath.toString().endsWith(".zip")) {
            return Paths.get(filePath.toString().replace(".zip", ""));
        }
        return filePath;
    }

    private static void extractZip(File zipFile) throws IOException {
        byte[] buffer = new byte[1024];

        // Use try-with-resources to ensure streams are properly closed
        try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry zipEntry = zipInputStream.getNextEntry();

            while (zipEntry != null) {
                // Handle directories
                if (zipEntry.isDirectory()) {
                    File dir = new File(zipFile.getParent(), zipEntry.getName());
                    if (!dir.exists()) {
                        dir.mkdirs();  // Create directory if it doesn't exist
                    }
                } else {
                    // Create new file and parent directories
                    File newFile = new File(zipFile.getParent(), zipEntry.getName());
                    new File(newFile.getParent()).mkdirs();  // Ensure parent directories exist

                    // Write file content
                    try (FileOutputStream fileOutputStream = new FileOutputStream(newFile)) {
                        int len;
                        while ((len = zipInputStream.read(buffer)) > 0) {
                            fileOutputStream.write(buffer, 0, len);
                        }
                    }
                }

                // Move to the next entry
                zipInputStream.closeEntry();
                zipEntry = zipInputStream.getNextEntry();
            }
        }
    }

    private static Reader getReaderAfterVersionHeader(BufferedReader reader) throws IOException {
        final int bufferSize = 1024; // Set a reasonable buffer size
        if (reader.markSupported()) {
            reader.mark(bufferSize);
        } else {
            System.err.println("Mark not supported for buffered reader");
        }

        String header = reader.readLine();
        if (!header.contains("GtoPdb Version")) {
            reader.reset(); // Reset to beginning to include first line if not version header
        }
        return reader; // Return reader at proper position after version header, if exists
    }
}
