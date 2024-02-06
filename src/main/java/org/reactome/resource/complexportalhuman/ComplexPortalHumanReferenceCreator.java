package org.reactome.resource.complexportalhuman;

import org.reactome.graphdb.ReactomeGraphDatabase;
import org.reactome.graphnodes.Complex;
import org.reactome.graphnodes.IdentifierNode;
import org.reactome.referencecreators.DatabaseIdentifierReferenceCreator;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 11/19/2023
 */
public class ComplexPortalHumanReferenceCreator extends DatabaseIdentifierReferenceCreator {
    private List<IdentifierNode> complexes;

    public ComplexPortalHumanReferenceCreator(Map<String, Set<String>> sourceIdentifierToResourceIdentifiers) {
        super("ComplexPortalHuman", sourceIdentifierToResourceIdentifiers);
    }

    @Override
    protected List<IdentifierNode> getIdentifierNodes() {
        if (this.complexes == null) {
            this.complexes = queryComplexes();
        }
        return this.complexes;
    }

    private List<IdentifierNode> queryComplexes() {
        return ReactomeGraphDatabase.getSession()
            .run("MATCH (c:Complex) RETURN c.dbId as dbId, c.stId as stableId")
            .stream()
            .map(record -> {
                long dbId = record.get("dbId").asLong();
                String stableId = record.get("stableId").asString();

                return new Complex(dbId, stableId);
            })
            //.peek(System.out::println)
            .collect(Collectors.toList());
    }
}
