package org.reactome.resource.hmdbmetabolite;

import org.reactome.resource.BasicFileRetriever;
import org.reactome.resource.FileRetriever;

import java.io.IOException;
import java.net.URL;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 11/20/2023
 */
public class HMDBMetaboliteFileRetriever extends BasicFileRetriever {

    public HMDBMetaboliteFileRetriever() {
        super("HMDBMetabolite");
    }
}
