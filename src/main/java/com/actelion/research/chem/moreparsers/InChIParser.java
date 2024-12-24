package com.actelion.research.chem.moreparsers;

import com.actelion.research.chem.MolfileParser;
import com.actelion.research.chem.StereoMolecule;
import com.actelion.research.chem.inchi.InChIJNI;

/**
 * A "parser" for InChI and InChIKey that just attempts to get the SDF file from
 * PubChem corresponding to this string.
 * 
 * Note that we could implement InChI/Java here and use InChI's exposed
 * structure model along with org.jmol.smiles.SmilesGenerator to create a SMILES
 * string from an InChI and then parse that.
 * 
 * This is what org.jmol.inchi.InChIJNI.java does.
 * 
 * But that would be a lot of code transfer from Jmol. So I decided to punt on
 * that for now.
 * 
 * But using Jmol as a library to do that would also work quite simply.
 * 
 * 
 * @author hanson@stolaf.edu
 *
 */
public class InChIParser {

	private int mMode;
	
	private final String type;

	public InChIParser() {
		this(0);
	}

	protected InChIParser(String type) {
		mMode = 0;
		this.type = type;
	}

	public InChIParser(int mode) {
		mMode = mode;
		type = "inchi";
	}
	
	protected InChIParser(int mode, String type) {
		mMode = mode;
		this.type = type;
	}
	
    private final static String pubchemInChIKey = "https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/inchikey/XX/SDF?record_type=2d";
    private final static String pubchemInChI = "https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/inchi/SDF?record_type=2d&inchi=";

    
	public boolean parse(StereoMolecule mol, String code) {
		String url;
		switch (type) {
		default:
		case "inchi":
			if (!code.startsWith("PubChem:")) {
				return InChIJNI.inchiToMolecule(code, mol);
			} else {
				code = code.substring(8);
				code = code.replaceAll("=", "%3D")
						.replaceAll("/", "%2F")
						.replaceAll("\\+", "%2B")
						.replaceAll(",", "%2C")
						.replaceAll("\\(", "%28")
						.replaceAll("\\)", "%29");
				url = pubchemInChI + code;
// InChI=  1S/C9H8O4/c1-6    (  10)  13-8-5-3-2-4-7(8)9(11)12/h2-5H,1H3,(H,11,12)
// InChI%3D1S%2FC9H8O4%2Fc1-6%2810%2913-8-5-3-2-4-7%288%299%2811%2912%2Fh2-5H%2C1H3%2C%28H%2C11%2C12%29
				break;
			}
		case "inchikey":
			url = pubchemInChIKey.replace("XX", code);
			break;
		}
		String molfile = ParserUtils.getURLContentsAsString(url);
		return molfile != null && new MolfileParser(mMode).parse(mol, molfile);
	}

}
