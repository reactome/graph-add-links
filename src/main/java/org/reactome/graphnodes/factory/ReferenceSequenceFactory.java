package org.reactome.graphnodes.factory;

import org.reactome.graphnodes.ReferenceDNASequence;
import org.reactome.graphnodes.ReferenceDatabase;
import org.reactome.graphnodes.ReferenceRNASequence;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 * Created 7/29/2025
 */
public class ReferenceSequenceFactory {
	private static Map<ReferenceDatabase, Map<String, Map<List<String>, ReferenceDNASequence>>>
		referenceDNASequenceCache;

	private static Map<ReferenceDatabase, Map<String, Map<List<String>, ReferenceRNASequence>>>
		referenceRNASequenceCache;

	public static ReferenceDNASequence createOrFetchReferenceDNASequence(
		String identifier, ReferenceDatabase referenceDatabase, List<String> geneNames
	) {
		if (referenceDNASequenceCache == null) {
			referenceDNASequenceCache = new HashMap<>();
		}

		return referenceDNASequenceCache
			.computeIfAbsent(referenceDatabase, k -> new HashMap<>())
			.computeIfAbsent(identifier, k -> new HashMap<>())
			.computeIfAbsent(geneNames, k -> new ReferenceDNASequence(identifier, referenceDatabase, geneNames));
	}

	public static ReferenceRNASequence createOrFetchReferenceRNASequence(
		String identifier, ReferenceDatabase referenceDatabase, List<String> geneNames
	) {
		if (referenceRNASequenceCache == null) {
			referenceRNASequenceCache = new HashMap<>();
		}

		return referenceRNASequenceCache
			.computeIfAbsent(referenceDatabase, k -> new HashMap<>())
			.computeIfAbsent(identifier, k -> new HashMap<>())
			.computeIfAbsent(geneNames, k -> new ReferenceRNASequence(identifier, referenceDatabase, geneNames));
	}
}
