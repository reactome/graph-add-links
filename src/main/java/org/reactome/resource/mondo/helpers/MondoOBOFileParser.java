package org.reactome.resource.mondo.helpers;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 * Created 7/25/2025
 */
public class MondoOBOFileParser {
	private Path filePath;

	public MondoOBOFileParser(Path filePath) {
		this.filePath = filePath;
	}

	public List<MondoTerm> parseOBOFile() throws IOException {
		List<MondoTerm> terms = new ArrayList<>();
		try (BufferedReader reader = Files.newBufferedReader(getFilePath())) {
			String line;
			MondoTerm currentTerm = null;
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if (line.isEmpty()) {
					continue;
				}

				if (line.equals("[Term]")) {
					if (currentTerm != null && currentTerm.getId() != null && currentTerm.getName() != null) {
						terms.add(currentTerm);
					}
					currentTerm = new MondoTerm();
				} else if (currentTerm != null) {
					handleMondoTermAttributeParsing(currentTerm, line);
				}
			}

			// Add last term
			if (currentTerm != null && currentTerm.getId() != null && currentTerm.getName() != null) {
				terms.add(currentTerm);
			}
		}
		return terms;
	}

	private void handleMondoTermAttributeParsing(MondoTerm currentTerm, String line) {
		Map<String, BiConsumer<String, MondoTerm>> handlers = new HashMap<>();
		handlers.put("id:",    (value, term) -> term.setId(value.replaceFirst("MONDO:", "").trim()));
		handlers.put("name:",  (value, term) -> term.setName(value.trim()));
		handlers.put("def:",   (value, term) -> term.setDef(value.trim()));
		handlers.put("xref:",  (value, term) -> term.addXRef(value.replaceFirst("\\{.*}", "").trim()));

		for (Map.Entry<String, BiConsumer<String, MondoTerm>> entry : handlers.entrySet()) {
			String prefix = entry.getKey();
			if (line.startsWith(prefix)) {
				String value = line.substring(prefix.length());
				entry.getValue().accept(value, currentTerm);
				break; // stop after first match
			}
		}
	}

	private Path getFilePath() {
		return this.filePath;
	}

	public static class MondoTerm {
		private String id;
		private String name;
		private String def;
		private List<String> xrefs;

		@Override
		public String toString() {
			return getId() + " - " + name;
		}

		public String getId() {
			return this.id;
		}

		public String getName() {
			return this.name;
		}

		public String getDef() {
			return this.def;
		}

		public List<String> getXRefs() {
			if (this.xrefs == null) {
				this.xrefs = new ArrayList<>();
			}

			return this.xrefs;
		}

		public List<String> getDiseaseOntologyXRefs() {
			return getXRefs().stream().filter(xref -> xref.contains("DOID")).collect(Collectors.toList());
		}

		private void setId(String id) {
			this.id = id;
		}

		private void setName(String name) {
			this.name = name;
		}

		private void setDef(String def) {
			this.def = def;
		}

		private void addXRef(String xref) {
			if (this.xrefs == null) {
				this.xrefs = new ArrayList<>();
			}

			this.xrefs.add(xref);
		}
	}
}
