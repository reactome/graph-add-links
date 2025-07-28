package org.reactome.resource.mondo;

import org.reactome.graphdb.ReactomeGraphDatabase;
import org.reactome.graphnodes.Event;
import org.reactome.graphnodes.IdentifierNode;
import org.reactome.graphnodes.PhysicalEntity;
import org.reactome.referencecreators.DatabaseIdentifierReferenceCreator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 * Created 7/24/2025
 */
public class MondoReferenceCreator extends DatabaseIdentifierReferenceCreator {
	private List<IdentifierNode> eventsAndPhysicalEntities;

	public MondoReferenceCreator(Map<String, Set<String>> diseaseOntologyIdentifierToResourceIdentifiers) {
		super("Mondo", diseaseOntologyIdentifierToResourceIdentifiers);
	}

	@Override
	protected List<? extends IdentifierNode> getIdentifierNodes() {
		if (this.eventsAndPhysicalEntities == null) {
			this.eventsAndPhysicalEntities = queryEventsAndPhysicalEntities();
		}
		return this.eventsAndPhysicalEntities;
	}

	private List<IdentifierNode> queryEventsAndPhysicalEntities() {
		List<IdentifierNode> eventsAndPhysicalEntities = new ArrayList<>();
		eventsAndPhysicalEntities.addAll(queryEvents());
		eventsAndPhysicalEntities.addAll(queryPhysicalEntities());
		return eventsAndPhysicalEntities;

	}

	private List<IdentifierNode> queryEvents() {
		return ReactomeGraphDatabase.getSession()
			.run("MATCH (d:Disease)-[:disease]-(i:Event) RETURN i.dbId as dbId, d.identifier as doID")
			.stream()
			.map(record -> {
				long dbId = record.get("dbId").asLong();
				String diseaseOntologyID = "DOID:" + record.get("doID").asString();

				return new Event(dbId, diseaseOntologyID);
			})
			.collect(Collectors.toList());
	}

	private List<IdentifierNode> queryPhysicalEntities() {
		return ReactomeGraphDatabase.getSession()
			.run("MATCH (d:Disease)-[:disease]-(i:PhysicalEntity) RETURN i.dbId as dbId, d.identifier as doID")
			.stream()
			.map(record -> {
				long dbId = record.get("dbId").asLong();
				String diseaseOntologyID = "DOID:" + record.get("doID").asString();

				return new PhysicalEntity(dbId, diseaseOntologyID);
			})
			.collect(Collectors.toList());
	}
}
