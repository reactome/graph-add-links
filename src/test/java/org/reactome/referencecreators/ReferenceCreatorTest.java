package org.reactome.referencecreators;

import org.junit.jupiter.api.Test;
import org.reactome.graphnodes.Event;
import org.reactome.graphnodes.IdentifierNode;

import java.util.*;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;

public class ReferenceCreatorTest {

    /** Disease Ontology id to MONDO ids, as MondoFileProcessor builds it. */
    private static final Map<String, List<String>> DO_TO_MONDO = new HashMap<>();
    static {
        DO_TO_MONDO.put("DOID:162", Collections.singletonList("0004992"));
        DO_TO_MONDO.put("DOID:3068", Collections.singletonList("0018177"));
        DO_TO_MONDO.put("DOID:3187", Collections.singletonList("0002544"));
        DO_TO_MONDO.put("DOID:9999", Collections.singletonList("0004992"));
    }

    private static List<? extends IdentifierNode> mondoFor(IdentifierNode source) {
        return DO_TO_MONDO.getOrDefault(source.getIdentifier(), Collections.emptyList()).stream()
            .map(mondo -> new Event(0L, mondo))
            .collect(Collectors.toList());
    }

    private static List<String> identifiers(List<? extends IdentifierNode> nodes) {
        return nodes.stream().map(IdentifierNode::getIdentifier).collect(Collectors.toList());
    }

    @Test
    public void anObjectWithSeveralDiseaseTermsGetsEachTermsMondo() {
        // One row per (object, DO term), as MondoReferenceCreator queries them.
        List<IdentifierNode> rows = Arrays.asList(
            new Event(1996341L, "DOID:162"),
            new Event(1996341L, "DOID:3068"),
            new Event(1996341L, "DOID:3187")
        );

        Map<Long, List<? extends IdentifierNode>> bySource =
            ReferenceCreator.externalIdentifiersBySource(rows, ReferenceCreatorTest::mondoFor);

        assertThat(identifiers(bySource.get(1996341L)), containsInAnyOrder("0004992", "0018177", "0002544"));
    }

    @Test
    public void aMondoReachedThroughTwoTermsIsListedOnce() {
        List<IdentifierNode> rows = Arrays.asList(
            new Event(42L, "DOID:162"),
            new Event(42L, "DOID:9999")
        );

        Map<Long, List<? extends IdentifierNode>> bySource =
            ReferenceCreator.externalIdentifiersBySource(rows, ReferenceCreatorTest::mondoFor);

        assertThat(identifiers(bySource.get(42L)), contains("0004992"));
    }

    @Test
    public void objectsAreKeptApart() {
        List<IdentifierNode> rows = Arrays.asList(
            new Event(1L, "DOID:162"),
            new Event(2L, "DOID:3187")
        );

        Map<Long, List<? extends IdentifierNode>> bySource =
            ReferenceCreator.externalIdentifiersBySource(rows, ReferenceCreatorTest::mondoFor);

        assertThat(identifiers(bySource.get(1L)), contains("0004992"));
        assertThat(identifiers(bySource.get(2L)), contains("0002544"));
    }
}
