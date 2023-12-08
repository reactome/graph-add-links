package org.reactome.resource.rhea;

import org.reactome.graphdb.ReactomeGraphDatabase;
import org.reactome.graphnodes.Complex;
import org.reactome.graphnodes.IdentifierNode;
import org.reactome.graphnodes.Reaction;
import org.reactome.referencecreators.DatabaseIdentifierReferenceCreator;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 11/19/2023
 */
public class RheaReferenceCreator extends DatabaseIdentifierReferenceCreator {
    private List<IdentifierNode> reactions;

    public RheaReferenceCreator(Map<String, Set<String>> sourceIdentifierToResourceIdentifiers) {
        super("Rhea", sourceIdentifierToResourceIdentifiers);
    }

    @Override
    protected List<IdentifierNode> getIdentifierNodes() {
        if (this.reactions == null) {
            this.reactions = queryReactions();
        }
        return this.reactions;
    }

    private List<IdentifierNode> queryReactions() {
        return ReactomeGraphDatabase.getSession()
            .run("MATCH (r:ReactionLikeEvent) RETURN r.dbId as dbId, r.stId as stableId")
            .stream()
            .map(record -> {
                long dbId = record.get("dbId").asLong();
                String stableId = record.get("stableId").asString();

                return new Reaction(dbId, stableId);
            })
            //.peek(System.out::println)
            .collect(Collectors.toList());
    }

}
