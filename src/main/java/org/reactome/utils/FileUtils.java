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

        byte[] buffer = new byte[1024];
        ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(filePath.toString()));
        ZipEntry zipEntry = zipInputStream.getNextEntry();
        while (zipEntry != null) {
            File newFile = new File(filePath.getParent().toFile(), zipEntry.getName());
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

    public static Path getPathWithoutZipExtension(Path filePath) {
        if (filePath.toString().endsWith(".gz")) {
            return Paths.get(filePath.toString().replace(".gz", ""));
        }

        if (filePath.toString().endsWith(".zip")) {
            return Paths.get(filePath.toString().replace(".zip", ""));
        }
        return filePath;
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
