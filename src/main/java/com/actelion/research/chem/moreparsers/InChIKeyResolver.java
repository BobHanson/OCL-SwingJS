package com.actelion.research.chem.moreparsers;

/**
 * Not so much a parser as a finder. This parser just
 * looks up the InChiKey at PubChem, retrieving SDF file data,
 * then parses that. 
 * 
 * Seems like about all that one could do. 
 * 
 * @author hanson@stolaf.edu
 *
 */
public class InChIKeyResolver extends ChemicalIdentifierResolver {

	public InChIKeyResolver() {
		this(false);
	}

	public InChIKeyResolver(boolean keepHydrogenMap) {
		super(TYPE_INCHIKEY, keepHydrogenMap);
	}	

}
