package org.reactome;

import org.json.JSONObject;
import org.reactome.referencecreators.ReferenceCreator;
import org.reactome.resource.FileProcessor;
import org.reactome.resource.Retriever;
import org.reactome.resource.pharmacodb.PharmacoDBFileProcessor;
import org.reactome.utils.ResourceJSONParser;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Map;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 4/8/2022
 */
public class Main {
    public static void main(String[] args) throws
        IOException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException,
        InvocationTargetException, InstantiationException, URISyntaxException {

        Map<String, JSONObject> resourceNameToResourceObject = ResourceJSONParser.getResourceJSONObjects();


        for (String resourceName : resourceNameToResourceObject.keySet()) {
//            if (!resourceName.equals("COSMIC")) {
//                continue;
//            }

            String resourcePackage = "org.reactome.resource." + resourceName.toLowerCase();
            String resourceFileRetriever = resourcePackage + "." + resourceName + "FileRetriever";
            System.out.println(resourceFileRetriever);

            Retriever retriever = (Retriever) Class.forName(resourceFileRetriever).getDeclaredConstructor().newInstance();
            retriever.downloadFiles();

            String resourceFileProcessor = resourcePackage + "." + resourceName + "FileProcessor";
            System.out.println(resourceFileProcessor);
            System.out.println(retriever.getLocalFilePaths().get(0));
            FileProcessor fileProcessor = getFileProcessor(resourceFileProcessor, retriever);
            System.out.println(fileProcessor.getSourceToResourceIdentifiers());

            String resourceReferenceCreator = resourcePackage + "." + resourceName + "ReferenceCreator";
            ReferenceCreator referenceCreator = (ReferenceCreator) Class.forName(resourceReferenceCreator).getDeclaredConstructor(Map.class).newInstance(fileProcessor.getSourceToResourceIdentifiers());

            referenceCreator.insertIdentifiers();
        }
    }

    private static FileProcessor getFileProcessor(String resourceFileProcessor, Retriever retriever)
        throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        if (resourceFileProcessor.contains("PharmacoDB")) {
            Path[] filePaths = retriever.getLocalFilePaths().toArray(new Path[0]);

            return new PharmacoDBFileProcessor(filePaths);
        }

        return (FileProcessor) Class.forName(resourceFileProcessor).getDeclaredConstructor(Path.class)
            .newInstance(retriever.getLocalFilePaths().get(0));
    }
}