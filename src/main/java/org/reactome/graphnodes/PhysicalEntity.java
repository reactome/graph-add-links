package org.reactome.graphnodes;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 * Created 7/25/2025
 */
public class PhysicalEntity extends IdentifierNode {
	public PhysicalEntity(long dbId, String identifier) {
		super(dbId, identifier);
	}

	@Override
	public String getSchemaClass() {
		return "Complex";
	}

	@Override
	public Set<String> getLabels() {
		Set<String> labels = new LinkedHashSet<>();
		labels.addAll(super.getLabels());
		labels.add(getSchemaClass());
		return labels;
	}
}
