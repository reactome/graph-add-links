package org.reactome.resource;

import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.reactome.DownloadInfo;
import org.reactome.graphdb.ReactomeGraphDatabase;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.*;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 07/24/2024
 */
public class SelfReferringUniProtRetriever extends SingleRetriever {

    public SelfReferringUniProtRetriever(DownloadInfo.Downloadable downloadable) {
        super(downloadable);
    }

    @Override
    public void downloadFile() throws IOException {
        for (String uniProtIdentifier : getUniProtIdsFromGraphDatabase()) {
             Files.write(
                getDownloadable().getLocalFilePath(),
                (uniProtIdentifier + "\t" + uniProtIdentifier + "\n").getBytes(),
                StandardOpenOption.CREATE, StandardOpenOption.APPEND
            );
        }
    }

    Collection<String> getUniProtIdsFromGraphDatabase() {
        Result result = ReactomeGraphDatabase.getSession().run(
            "MATCH (rgp:ReferenceGeneProduct) RETURN DISTINCT rgp.identifier"
        );

        List<String> uniProtIds = new ArrayList<>();
        while (result.hasNext()) {
            Record uniProtIdentifierRecord = result.next();
            uniProtIds.add(uniProtIdentifierRecord.get(0).asString());
        }
        return uniProtIds;
    }
}
