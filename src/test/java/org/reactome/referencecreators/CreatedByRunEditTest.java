package org.reactome.referencecreators;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * The created relationship points from the run's InstanceEdit to what it created, as in the rest of
 * the graph (reactome/graph-add-links#7).
 */
public class CreatedByRunEditTest {

    @Test
    public void createdGoesFromTheEditToTheNode() {
        String cypher = ReferenceCreator.createdByRunEdit("di");

        assertThat(cypher, containsString("MERGE (ie)-[cr:created]->(di)"));
        assertThat(cypher, not(containsString("(di)-[cr:created]->(ie)")));
        assertThat(cypher, containsString("ON CREATE SET cr.order = 0, cr.stoichiometry = 1"));
    }
}
