package org.reactome;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.reactome.referencecreators.ReferenceCreator;
import org.reactome.resource.FileProcessor;
import org.reactome.resource.Retriever;
import org.reactome.resource.ctdgene.CTDGeneFileProcessor;
import org.reactome.resource.omim.OMIMFileProcessor;
import org.reactome.resource.pharmacodb.PharmacoDBFileProcessor;
import org.reactome.utils.ConfigParser;
import org.reactome.utils.ResourceJSONParser;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 4/8/2022
 */
public class Main {
    private static final Logger logger = LogManager.getLogger();

    @Parameter(names = {"--help", "-h"}, description = "Help mode")
    private boolean help;
    @Parameter(names = {"--config", "-c"}, description = "Config file path")
    private String configFilePath;
    @Parameter(names = {"--downloads", "-d"}, description = "Download files")
    private boolean downloads;
    @Parameter(names = {"--insertions", "-i"}, description = "Insert resources into graph database")
    private boolean insertions;
    @Parameter(names = {"--resources", "-r"}, description = "Names of resources to download and/or insert")
    private List<String> resourceNames;
    @Parameter(names = {"--list_resources", "-l"}, description = "List supported resource names")
    private boolean listResources;

    public static void main(String[] args) throws Exception {
        Main main = new Main();
        JCommander.newBuilder()
            .addObject(main)
            .build()
            .parse(args);
        main.run();
    }

    private void run() throws Exception {

        if (this.help) {
            printUsageInstructions();
            System.exit(0);
        }

        if (this.listResources) {
            listSupportedResourceNames();
            System.exit(0);
        }

        if (this.resourceNames == null || this.resourceNames.isEmpty()) {
            this.resourceNames = getAllSupportedResourceNames();
        }

        if (this.configFilePath != null) {
            ConfigParser.setConfigFilePath(this.configFilePath);
        }

        if (this.downloads) {
            downloadFiles();
        }

        if (this.insertions) {
            insertResources();
        }
        System.exit(0);
    }

    private void printUsageInstructions() throws URISyntaxException {
        StringBuilder stringBuilder = new StringBuilder();
        String jarPath = Main.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
        stringBuilder.append(String.format("Usage: java -jar %s [-d|--downloads] [-i|--insertions] [-r|--resources <name-of-resource1, name-of-resource2, ...>] [-l|--list_resources] [--help]%n", jarPath));
        stringBuilder.append("Options:\n");
        stringBuilder.append("\t[-c|--config]: Used to specify the configuration file's path (default of config.properties in the resource directory) \n");
        stringBuilder.append("\t[-d|--downloads]: Used to download files for resources (specified with -r or all if -r omitted)\n");
        stringBuilder.append("\t[-i|--insertions]: Used to process downloaded files and insert resources (specified with -r or all if -r omitted) into the graph database\n");
        stringBuilder.append("\t[-r|--resources <name-of-resource1, name-of-resource2, ...>]: Comma-delimited list of names (NOTE: no spaces between name of resources)\n");
        stringBuilder.append("\t[-l|--list_resources]: Lists names of supported resources to use with -r|--resources\n");
        stringBuilder.append("\t[-h|--help]: Displays this help message");

        System.out.println(stringBuilder.toString());
    }

    private void downloadFiles() throws Exception {
        final int attemptNumber = 1;
        retrieveResources(this.resourceNames, attemptNumber);
    }

    private void insertResources() throws Exception {
        for (String resourceName : this.resourceNames) {
            String resourcePackage = "org.reactome.resource." + resourceName.toLowerCase();
            String resourceFileRetriever = resourcePackage + "." + resourceName + "FileRetriever";

            Retriever retriever = (Retriever) Class.forName(resourceFileRetriever)
                .getDeclaredConstructor().newInstance();

            String resourceFileProcessor = resourcePackage + "." + resourceName + "FileProcessor";
            logger.info("Running " + resourceFileProcessor + "...");
            logger.info("Parsing local file(s): " + retriever.getLocalFilePaths());
            FileProcessor fileProcessor = getFileProcessor(resourceFileProcessor, retriever);
            logger.info("Map size: " + fileProcessor.getSourceToResourceIdentifiers().size());
            logger.debug("Mapping: " + fileProcessor.getSourceToResourceIdentifiers());

            String resourceReferenceCreator = resourcePackage + "." + resourceName + "ReferenceCreator";
            logger.info("Running " + resourceReferenceCreator + "...");
            ReferenceCreator referenceCreator = (ReferenceCreator) Class.forName(resourceReferenceCreator)
                .getDeclaredConstructor(Map.class).newInstance(fileProcessor.getSourceToResourceIdentifiers());

            referenceCreator.insertIdentifiers();
            logger.info("Finished inserting identifiers for " + resourceName);
        }
    }

    private void listSupportedResourceNames() {
        System.out.println("Supported Resource Names: ");
        for (String resourceName : getAllSupportedResourceNames()) {
            System.out.println(resourceName);
        }
    }

    private List<String> getAllSupportedResourceNames() {
        Map<String, JSONObject> resourceNameToResourceObject = ResourceJSONParser.getResourceJSONObjects();
        List<String> resourceNames = new ArrayList<>(resourceNameToResourceObject.keySet());
        Collections.sort(resourceNames);
        return resourceNames;
    }

    private void retrieveResources(Collection<String> resourceNames, int attemptNumber) {
        final int maximumAttemptNumber = 3;

        logger.info("Downloading resource information...");
        Set<String> resourcesToRetry = new LinkedHashSet<>();
        resourceNames.parallelStream().forEach(resourceName -> {
            try {
                logger.info("Downloading " + resourceName);
                retrieveResource(resourceName);
            } catch (Exception e) {
                logger.error("Unable to download files for " + resourceName + ": " + e);
                resourcesToRetry.add(resourceName);
            }
        });

        if (!resourcesToRetry.isEmpty() && (attemptNumber <= maximumAttemptNumber)) {
            logger.info("Retrying resources " + resourcesToRetry + " for time " + attemptNumber + " of " +
                maximumAttemptNumber);
            attemptNumber++;

            retrieveResources(resourcesToRetry, attemptNumber);
        }
    }

    private void retrieveResource(String resourceName) throws Exception {
        String resourcePackage = "org.reactome.resource." + resourceName.toLowerCase();
        String resourceFileRetriever = resourcePackage + "." + resourceName + "FileRetriever";
        logger.info("Running " + resourceFileRetriever + "...");

        Retriever retriever = (Retriever) Class.forName(resourceFileRetriever).getDeclaredConstructor().newInstance();
        createDirectoriesIfNotExists(retriever.getLocalFilePaths().toArray(new Path[0]));
        retriever.downloadFile();

        logger.info("Completed " + resourceFileRetriever);
    }

    private void createDirectoriesIfNotExists(Path... filePaths) throws IOException {
        for (Path filePath : filePaths) {
            if (filePath.toFile().isDirectory()) {
                Files.createDirectories(filePath);
            } else {
                Files.createDirectories(filePath.getParent());
            }
        }
    }

    private static FileProcessor getFileProcessor(String resourceFileProcessor, Retriever retriever)
        throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException,
            InstantiationException, IOException {

        // PharmacoDB is initialized without reflection because it has two files to take into the constructor
        // (other file processors use one file)
        if (resourceFileProcessor.contains("PharmacoDB")) {
            Path[] filePaths = retriever.getLocalFilePaths().toArray(new Path[0]);

            return new PharmacoDBFileProcessor(filePaths);
        }

        if (resourceFileProcessor.contains("OMIM")) {
            Path[] filePaths = retriever.getLocalFilePaths().toArray(new Path[0]);

            return new OMIMFileProcessor(filePaths);
        }

        if (resourceFileProcessor.contains("CTDGene")) {
            Path[] filePaths = retriever.getLocalFilePaths().toArray(new Path[0]);

            return new CTDGeneFileProcessor(filePaths);
        }

        return (FileProcessor) Class.forName(resourceFileProcessor).getDeclaredConstructor(Path.class)
            .newInstance(retriever.getLocalFilePaths().get(0));
    }
}