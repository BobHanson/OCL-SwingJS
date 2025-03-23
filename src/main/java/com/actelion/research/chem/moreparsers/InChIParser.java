package com.actelion.research.chem.moreparsers;

import com.actelion.research.chem.StereoMolecule;
import com.actelion.research.chem.inchi.InChIOCL;

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

	public InChIParser() {
	}
    
	public boolean parse(StereoMolecule mol, String inchi) {
		return InChIOCL.getMoleculeFromInChI(inchi, mol); 
	}

	
}
