package org.reactome.resource.mondo.helpers;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
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
					if (line.startsWith("id:")) {
						String id = line.substring(3).replaceFirst("MONDO:","").trim();
						currentTerm.setId(id);
					} else if (line.startsWith("name:")) {
						String name = line.substring(5).trim();
						currentTerm.setName(name);
					} else if (line.startsWith("def:")) {
						String def = line.substring(4).trim();
						currentTerm.setDef(def);
					} else if (line.startsWith("xref:")) {
						String xref = line.substring(5).replaceFirst("\\{.*}","").trim();
						currentTerm.addXRef(xref);
					}
				}
			}

			// Add last term
			if (currentTerm != null && currentTerm.getId() != null && currentTerm.getName() != null) {
				terms.add(currentTerm);
			}
		}
		return terms;
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
