package org.reactome.resource.mondo;

import org.reactome.fileprocessors.FileProcessor;
import org.reactome.resource.mondo.helpers.MondoOBOFileParser;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 * Created 7/24/2025
 */
public class MondoFileProcessor implements FileProcessor {
	private Path filePath;
	private Map<String, Set<String>> diseaseOntologyToResourceIdentifiers;

	public MondoFileProcessor(Path filePath) {
		this.filePath = filePath;
	}

	@Override
	public Map<String, Set<String>> getSourceToResourceIdentifiers() throws IOException {
		if (this.diseaseOntologyToResourceIdentifiers == null ||
			this.diseaseOntologyToResourceIdentifiers.isEmpty()) {
			this.diseaseOntologyToResourceIdentifiers = new HashMap<>();

			MondoOBOFileParser mondoOBOFileParser = new MondoOBOFileParser(getFilePath());
			List<MondoOBOFileParser.MondoTerm> mondoTerms = mondoOBOFileParser.parseOBOFile();
			for (MondoOBOFileParser.MondoTerm mondoTerm : mondoTerms) {
				for (String doID : mondoTerm.getDiseaseOntologyXRefs()) {
					this.diseaseOntologyToResourceIdentifiers.computeIfAbsent(doID, k -> new TreeSet<>())
						.add(mondoTerm.getId());
				}
			}
		}

		return this.diseaseOntologyToResourceIdentifiers;
	}

	private Path getFilePath() {
		return this.filePath;
	}
}
