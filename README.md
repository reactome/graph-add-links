# Graph-Add-Links

This repository is the code responsible for adding links for resources external to Reactome to the Reactome Graph
Database.

# Resources

For each external resource, data is downloaded that will allow mapping of existing Reactome database nodes to
a cross-reference in the resource.  Most often, the Reactome nodes are of the type ReferenceGeneProduct (which
represent UniProt protein entries and are used as references for Reactome proteins) but Pathways, Reactions, Complexes,
ReferenceMolecules (representing ChEBI entries of small molecules), and ReferenceTherapeutics (representing
GuideToPharmacology entries of drugs) are also cross-referenced.

The following are the resources linked to by Reactome (as of July 2025):

- BioGPSGene
- COSMIC
- CTDGene
- ComplexPortalHuman
- ComplexPortalSARS
- ComplexPortalSARSCov2
- DbSNPGene
- EnsEMBLGene
- EnsEMBLProtein
- EnsEMBLTranscript
- FlyBase
- GTPLigands
- GTPTargets
- GeneCards
- GlyGen
- HGNC
- HMDBMetabolite
- HMDBProtein
- HPA
- IntEnz
- KEGG
- MGI
- Monarch
- Mondo
- NCBIGene
- OMIM
- OpenTargets
- Orphanet
- OtherIdentifiers
- PDB
- PRO
- PharmacoDB
- PharosLigands
- PharosTargets
- RGD
- RefSeqPeptide
- RefSeqRNA
- Rhea
- UCSC
- VGNC
- Wormbase
- Xenbase
- ZFIN
- Zinc
- ZincBiogenic
- ZincFDAApproved
- ZincInvestigational
- ZincMetabolites
- ZincPredictionsPurchasable
- ZincSubstances
- ZincTarget
- ZincWorldDrugs

# Code-organization

All code is organized in the main package "org.reactome" under different sub-packages.  The bulk of the code is stored
under the "org.reactome.resource" package where each sub-package is a different resource to be cross-referenced.  Under
each of these, there are typically three classes:

1) FileRetrievers

	FileRetrievers are responsible for retrieving data for the resource (either directly or indirectly through a
	third-party provider such as UniProt's mapping service).  Once the data is retrieved, it is stored as a file
	in a common download directory.

2) FileProcessors and 3) ReferenceCreators

	FileProcessors and ReferenceCreators work together to do the insertion of the cross-reference into the Reactome
	graph database.

	FileProcessors are responsible for parsing the downloaded data file (whose location provided by the retriever) and
	returning a mapping of source identifier (e.g. UniProt) to target identifier(s) - note the mapping may be 1-1
	or 1-many.

	ReferenceCreators are primarily responsible for receiving the mapping from a FileProcessor then inserting new nodes
	for each target identifier to the external resource as well as relationships to them.  The ReferenceCreator also
	creates a ReferenceDatabase node and relationships from the cross-references to it to represent the external
	resource's information (i.e. name, URL, etc.)  Finally, an "InstanceEdit" node is created and the relationships to
	it from the cross-references to represent the edits made to the database.  For these nodes and relationships, two
	csv files are created (stored in src/main/resources/reference_creator_csv/ - see below) and then read using
	the Cypher function "LOAD CSV" (for efficiency of bulk inserting nodes and relationships).
	The first CSV file contains the information for the node to be created with the DbId, DisplayName, SchemaClass,
	Identifier, ReferenceDbName, and URL.  The second CSV file contains the information to create 3 relationships:
	the source identifier (e.g. UniProt) and the target identifier, the target identifier and the reference
	database, and the target identifier and the instance edit.


Additionally, the "org.reactome.graphdb" package is for connection and direct interaction with the graph database.
The "org.reactome.graphnodes" package is for modeling of nodes in the graph database for the different types of objects
represented.
The "org.reactome.referencecreators" package is for the different, common types of reference creators
extended by the ReferenceCreator class mentioned above.
The "org.reactome.utils" packages is for any utilities needed
to do operations repeated in various parts of the codebase.

## The "src/main/resources" directory contains the following required files:

### XML_transformers

For three resources (HMDB Metabolites, HMDB Protein, and Orphanet), the data retrieved is in XML format and the
directory "xml_transformers" contain XSL (Extensible Stylesheet Language) files for transforming those XML files
into simple TSV/CSV files for easier parsing by their file processors.

### config.properties

This file contains the location of where resource files will be downloaded.  It also contains any properties
needed to access resource files such as usernames and passwords.  Currently, two resources (COSMIC and Orphanet)
require credentials to be able to access their data.  The username and password credentials to connect to a
running graph database are also required (Currently, a BOLT port of 7687 and host of "localhost" is assumed).
The config.properties file expects the following:

	downloadDirectory=<location_for_retrieved_mapping_data>
	orphanetUser=<username>
	orphanetPassword=<password>
	cosmicUser=<username>
	cosmicPassword=<password>
	neo4jUserName=<username>
	neo4jPassword=<password>
	personId=<personDbId>

### identifier-resources.json

This file describes the resources to be cross-referenced to the Reactome graph database.  The JSON file has an
entry for each resource keyed by its name along with the type of identifier node to insert (DatabaseIdentifier or
ReferenceDNASequence), the information for data download(s) (including the URL, the remote file name, whether the
remote file name has a pattern (see FlyBase for an example), and the local file name), as well as the reference
database representing the resource with the name(s), URL, accessURL (the pattern for looking up a particular
resource id) and the identifiers.org MIR identifier.

## Prerequisites for running

### Graph database indices

To allow efficient creation of new nodes and relationships by the Graph-Add-Links program, the following indices should
be created BEFORE running the program:

- CREATE INDEX ON :DatabaseObject(dbId)
- CREATE INDEX ON :ReferenceSequence(dbId)
- CREATE INDEX ON :DatabaseIdentifier(dbId)
- CREATE INDEX ON :ReferenceDatabase(dbId)
- CREATE INDEX ON :InstanceEdit(dbId)
- CREATE INDEX ON :ReferenceGeneProduct(dbId)

The first five "create index" statements are responsible for indexing the nodes for associating relationships between
cross-references and source (i.e. UniProt, ChEBI, etc.) nodes.  The last "create index" statement is responsible for
indexing ReferenceGeneProduct nodes for fast lookup to add "other identifiers" obtained from EnsEMBL BioMart.

## Compilation and running

The Graph-Add-Links project is a typical maven structure project which can be built simply by the command
"mvn clean package" at the root directory.  The "jar-with-dependencies" file output in the target directory can then
be run with various options.

**Usage: java -jar graph-add-links-1.0-SNAPSHOT-jar-with-dependencies.jar
	[-d|--downloads] [-i|--insertions] [-r|--resources <name-of-resource1,name-of-resource2,...>] [-l|--list_resources] [--help]**

Options:
- [-c|--config]: Used to provide the path of a configuration file (default of src/main/resources/config.properties)
- [-d|--downloads]: Used to download files for resources (specified with -r or all if -r omitted)
- [-i|--insertions]: Used to process downloaded files and insert resources (specified with -r or all if -r omitted) into the graph database
- [-r|--resources <name-of-resource1,name-of-resource2,...>]: Comma-delimited list of names (NOTE: no spaces between name of resources)
- [-l|--list_resources]: Lists names of supported resources to use with -r|--resources
- [-h|--help]: Displays this help message
