package org.reactome;

import org.json.JSONObject;
import org.reactome.referencecreators.ReferenceCreator;
import org.reactome.resource.FileProcessor;
import org.reactome.resource.FileRetriever;
import org.reactome.resource.Retriever;
import org.reactome.resource.biogpsgene.BioGPSGeneFileProcessor;
import org.reactome.resource.biogpsgene.BioGPSGeneFileRetriever;
import org.reactome.resource.biogpsgene.BioGPSGeneReferenceCreator;
import org.reactome.resource.complexportal.ComplexPortalFileProcessor;
import org.reactome.resource.complexportal.human.ComplexPortalHumanFileRetriever;
import org.reactome.resource.complexportal.human.ComplexPortalHumanReferenceCreator;
import org.reactome.resource.complexportal.sars.ComplexPortalSARSFileRetriever;
import org.reactome.resource.ctdgene.CTDGeneFileProcessor;
import org.reactome.resource.ctdgene.CTDGeneFileRetriever;
import org.reactome.resource.ctdgene.CTDGeneReferenceCreator;
import org.reactome.resource.flybase.FlyBaseFileProcessor;
import org.reactome.resource.flybase.FlyBaseFileRetriever;
import org.reactome.resource.flybase.FlyBaseReferenceCreator;
import org.reactome.resource.glygen.GlyGenFileProcessor;
import org.reactome.resource.glygen.GlyGenFileRetriever;
import org.reactome.resource.omim.OMIMFileRetriever;
import org.reactome.resource.opentargets.OpenTargetsFileProcessor;
import org.reactome.resource.opentargets.OpenTargetsFileRetriever;
import org.reactome.resource.orphanet.OrphanetFileProcessor;
import org.reactome.resource.orphanet.OrphanetFileRetriever;
import org.reactome.resource.pdb.PDBFileRetriever;
import org.reactome.resource.pro.PROFileProcessor;
import org.reactome.resource.pro.PROFileRetriever;
import org.reactome.resource.pro.PROReferenceCreator;
import org.reactome.resource.vgnc.VGNCFileProcessor;
import org.reactome.resource.vgnc.VGNCFileRetriever;
import org.reactome.resource.vgnc.VGNCReferenceCreator;
import org.reactome.resource.zfin.ZFINFileRetriever;
import org.reactome.utils.ResourceJSONParser;

import java.io.IOException;
import java.util.Map;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 4/8/2022
 */
public class Main {
    public static void main(String[] args) throws IOException {
        Map<String, JSONObject> resourceNameToResourceObject = ResourceJSONParser.getResourceJSONObjects();



        Retriever retriever = new CTDGeneFileRetriever();
        //retriever.downloadFile();
        FileProcessor fileProcessor = new CTDGeneFileProcessor(retriever.getLocalFilePath());
        System.out.println(fileProcessor.getSourceToResourceIdentifiers());
        ReferenceCreator referenceCreator = new CTDGeneReferenceCreator(fileProcessor.getSourceToResourceIdentifiers());
        referenceCreator.insertIdentifiers();
//        Retriever retriever = new ComplexPortalHumanFileRetriever();
//        retriever.downloadFile();
//        FileProcessor fp = new ComplexPortalFileProcessor(retriever.getLocalFilePath());
//        System.out.println(fp.getSourceToResourceIdentifiers());
//        ReferenceCreator rc = new ComplexPortalHumanReferenceCreator(fp.getSourceToResourceIdentifiers());
//        rc.insertIdentifiers();
//        Retriever retriever = new GlyGenFileRetriever();
//        retriever.downloadFile();
//        FileProcessor fp = new GlyGenFileProcessor(retriever.getLocalFilePath());
//        System.out.println(fp.getSourceToResourceIdentifiers());
//
//        System.exit(0);
        for (String resourceName : resourceNameToResourceObject.keySet()) {


        }
//            FileRetriever fileRetriever = new VGNCFileRetriever();
//            //fileRetriever.downloadFile();
//            FileProcessor fileProcessor = new VGNCFileProcessor(fileRetriever.getLocalFilePath());
//            System.out.println(fileProcessor.getSourceToResourceIdentifiers());
//            ReferenceCreator referenceCreator = new VGNCReferenceCreator(fileProcessor.getSourceToResourceIdentifiers());
//            referenceCreator.insertIdentifiers();


//            String resourceName = "PRO";
//            JSONObject resourceJSONObject = resourceNameToResourceObject.get(resourceName);

//            FileRetriever flyBaseFileRetriever = new FlyBaseFileRetriever();
//            flyBaseFileRetriever.downloadFile();
            //System.out.println("Creating file retriever...");
            //FileRetriever proFileRetriever = new PROFileRetriever();
            //System.out.println("Downloading file...");
            //proFileRetriever.downloadFile();

//            FileProcessor flyBaseFileProcessor = new FlyBaseFileProcessor(flyBaseFileRetriever.getLocalFilePath());
//            System.out.println("Creating file processor...");
            //FileProcessor proFileProcessor = new PROFileProcessor(proFileRetriever.getLocalFilePath());


//            System.out.println("Creating database identifier reference creator...");
//            ReferenceCreator flyBaseReferenceCreator = new FlyBaseReferenceCreator(flyBaseFileProcessor.getSourceToResourceIdentifiers());

            //ReferenceCreator proReferenceCreator =
            //    new PROReferenceCreator(proFileProcessor.getSourceToResourceIdentifiers());
            //System.out.println(databaseIdentifierReferenceCreator.getReferenceDatabase().toString());

//            System.out.println("Inserting database identifiers...");
            //proReferenceCreator.insertIdentifiers();
//        flyBaseReferenceCreator.insertIdentifiers();
        //}
        System.exit(0);
//
//        Collection<ReferenceGeneProduct> referenceGeneProducts =
//            ReferenceGeneProduct.fetchAllReferenceGeneProducts().values();

//        FileRetriever fileRetriever = new FileRetriever();
        //System.out.println(fileRetriever.getFlyBaseFileRemoteURL().toString());
//        fileRetriever.downloadFile();

//        //System.out.println(fileRetriever.getLocalFilePath());
//        FlyBaseFileProcessor fileProcessor = new FlyBaseFileProcessor(fileRetriever.getLocalFilePath());
//        DatabaseIdentifierReferenceCreator databaseIdentifierReferenceCreator =
//            new DatabaseIdentifierReferenceCreator("FlyBase", fileProcessor.getUniProtToFlyBase());

//        for (ReferenceGeneProduct referenceGeneProduct : referenceGeneProducts) {
//            List<DatabaseIdentifier> databaseIdentifiers =
//                databaseIdentifierReferenceCreator.createDatabaseIdentifiersForReferenceGeneProduct(referenceGeneProduct);
//
//            for (DatabaseIdentifier databaseIdentifier : databaseIdentifiers) {
//                referenceGeneProduct.connectTo(databaseIdentifier, new GraphNode.Relationship("crossReference"));
//            }
//        }

        //fileProcessor.getUniProtToFlyBase()
        //System.out.println(fileProcessor.getUniProtToFlyBase());
    }
}