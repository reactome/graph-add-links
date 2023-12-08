package org.reactome.resource.orphanet;

import org.reactome.resource.AuthenticationBasicFileRetriever;
import org.reactome.utils.ConfigParser;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 11/19/2023
 */
public class OrphanetFileRetriever extends AuthenticationBasicFileRetriever {

    public OrphanetFileRetriever() {
        super("Orphanet");
    }

    @Override
    protected String getUserName() {
        return ConfigParser.getConfigProperty("orphanetUser");
    }

    @Override
    protected String getPassword() {
        return ConfigParser.getConfigProperty("orphanetPassword");
    }
}
