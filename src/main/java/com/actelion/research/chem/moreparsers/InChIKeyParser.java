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
public class InChIKeyParser extends InChIParser {

	public InChIKeyParser() {
		this(0);
	}

	public InChIKeyParser(int mode) {
		super(mode, "inchikey");
	}	

}
