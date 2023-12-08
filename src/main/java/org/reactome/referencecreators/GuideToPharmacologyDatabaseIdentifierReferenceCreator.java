package org.reactome.referencecreators;

import org.reactome.graphnodes.IdentifierNode;
import org.reactome.graphnodes.ReferenceTherapeutic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 11/30/2023
 */
public class GuideToPharmacologyDatabaseIdentifierReferenceCreator extends DatabaseIdentifierReferenceCreator {
    public GuideToPharmacologyDatabaseIdentifierReferenceCreator(
        String referenceName, Map<String, Set<String>> guideToPharmacologyIdentifierToReferenceIdentifiers
    ) throws IllegalArgumentException {
        super(referenceName, guideToPharmacologyIdentifierToReferenceIdentifiers);
    }

    @Override
    protected List<IdentifierNode> getIdentifierNodes() {
        return new ArrayList<>(
            ReferenceTherapeutic.fetchReferenceTherapeuticForGuideToPharmacologyIdentifiers(
                getGuideToPharmacologyIdentifiers()).values()
        );
    }

    private Set<String> getGuideToPharmacologyIdentifiers() {
        return getSourceIdentifierToReferenceIdentifiers().keySet();
    }
}
