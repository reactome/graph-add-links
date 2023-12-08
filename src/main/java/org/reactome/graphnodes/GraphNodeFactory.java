package org.reactome.graphnodes;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 5/26/2022
 */
public class GraphNodeFactory {

    public <E extends GraphNode> E createIdentifierNode(GraphNodeType graphNodeType, String identifier, ReferenceDatabase referenceDatabase, boolean isInDatabase) {
        if (graphNodeType.equals(GraphNodeType.DATABASE_IDENTIFIER)) {
            //return new DatabaseIdentifier(identifier, referenceDatabase);
        }
        return null;
    }

    public enum GraphNodeType {
        DATABASE_IDENTIFIER,
        REFERENCE_DNA_SEQUENCE,
        REFERENCE_RNA_SEQUENCE,
        REFERENCE_GENE_PRODUCT,
        REFERENCE_DATABASE
    }
}
