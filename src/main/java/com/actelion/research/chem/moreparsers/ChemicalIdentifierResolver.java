package com.actelion.research.chem.moreparsers;

import com.actelion.research.chem.MolfileParser;
import com.actelion.research.chem.SmilesParser;
import com.actelion.research.chem.StereoMolecule;
import com.actelion.research.chem.inchi.InChIOCL;

public abstract class ChemicalIdentifierResolver {

	public final static String TYPE_INCHI = "inchi";
	public final static String TYPE_INCHIKEY = "inchikey";
	public final static String TYPE_NAME = "name";

	public final static String SOURCE_PUBCHEM = "pubchem";
	public final static String SOURCE_CIR = "cir";

	private final boolean keepHydrogenMap;
	
	private final String type;
	private String source = SOURCE_PUBCHEM;

	public ChemicalIdentifierResolver(String type, boolean keepHydrogenMap) {
		this.type = type;
		this.keepHydrogenMap = keepHydrogenMap;
	}

	public ChemicalIdentifierResolver setSource(String source) {
		this.source = source;
		return this;
	}
	
    private final static String pubchemInChIKey = "https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/inchikey/XX/SDF?record_type=2d";
    private final static String pubchemInChI = "https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/inchi/SDF?record_type=2d&inchi=";
    private final static String pubchemName = "https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/name/XX/SDF?record_type=2d";

    private final static String cir ="https://cactus.nci.nih.gov/chemical/structure/XX/smiles";

	public boolean resolve(StereoMolecule mol, String code) {
		String url;
		switch (source) {
		default:
		case SOURCE_CIR:
			url = getChemicalIdentifierResolverURL(code);
			break;
		case SOURCE_PUBCHEM:
			url = getPubChemURL(code);
			break;
		}
		if (url == null) {
			return false;
		}
		String data = ParserUtils.getURLContentsAsString(url);
		System.out.println(getClass().getName() + "\n -for " + code + "\n -url " + url);
		switch (source) {
		case SOURCE_CIR:
			try {
				new SmilesParser().parse(mol, data);
				return true;
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		case SOURCE_PUBCHEM:
			return data != null && new MolfileParser(keepHydrogenMap ? 1 : 0).parse(mol, data);
		}
		return false;
	}

	private String getChemicalIdentifierResolverURL(String code) {
		code = ParserUtils.escapeCIRQuery(code);
		switch (type) {
		case TYPE_INCHI:
		case TYPE_INCHIKEY:
		case TYPE_NAME:
			return cir.replace("XX", code);
		}
		System.out.println("ChemicalIdentifierResolver: CIR type " + type + " not implemented");
		return null;
	}

	private String getPubChemURL(String code) {
		code = ParserUtils.escapePubChemQuery(code);
		switch (type) {
		case TYPE_INCHI:
			code = ParserUtils.escapePubChemQuery(code.substring(8));
			return pubchemInChI + code;
		case TYPE_INCHIKEY:
			return pubchemInChIKey.replace("XX", code);
		case TYPE_NAME:
			return pubchemName.replace("XX", code);
		}
		System.out.println(getClass().getName() + " pubchem type " + type + " not implemented");
		return null;
	}


}
