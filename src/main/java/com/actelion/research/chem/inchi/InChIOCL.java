package com.actelion.research.chem.inchi;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.iupac.InChIStructureProvider;

import com.actelion.research.chem.Canonizer;
import com.actelion.research.chem.IsomericSmilesCreator;
import com.actelion.research.chem.Molecule;
import com.actelion.research.chem.MolfileParser;
import com.actelion.research.chem.SmilesCreator;
import com.actelion.research.chem.SmilesParser;
import com.actelion.research.chem.StereoMolecule;
import com.actelion.research.chem.coords.CoordinateInventor;

public abstract class InChIOCL implements InChIStructureProvider {

	/**
	 * Just loads this class, initiating JavaScript loading of the WASM.
	 * 
	 * Note that a clock tick is required to continue after this asynchronous load.
	 */
	public static void init() {
		getPlatformSubclass();
	}

	/**
	 * Do not set this final, as the java2script transpiler needs to evaluate the javadoc.
	 */
	private /*nonfinal*/ static boolean interfaceHasMolFileToInChI = /** @j2sNative true || */Boolean.FALSE.booleanValue();
	private /*nonfinal*/ static boolean isJS = /** @j2sNative true || */Boolean.FALSE.booleanValue();
	

	protected abstract String getInchiImpl(StereoMolecule mol, String molFileDataOrInChI, String options, boolean getKey);


	///// molecule to InChI //

	/**
	 * Get an InChI from a StereoMolecule
	 * 
	 * @param mol
	 * @param options standard InChI options and "fixedh?", meaning fixedh only if
	 *                it is different from standard
	 * @return InChI 
	 */
	public static String getInChI(StereoMolecule mol, String options) {
		return getPlatformSubclass().getInchiPvt(mol, null, options, false);
	}

	/**
	 * Get an InChI from V2 or V3 MOL file data or from an InChI (a string starting with "InChI=")
	 * 
	 * @param molFileDataOrInChI V2 or V3 MOL file data or an InChI string
	 * @param options            standard InChI options and "fixedh?", meaning
	 *                           fixedh only if it is different from standard
	 * @return InChIKey
	 */
	public static String getInChI(String molFileDataOrInChI, String options) {
		return getPlatformSubclass().getInchiPvt(null, molFileDataOrInChI, options, false);
	}

	/**
	 * Get an InChI from a SMILES string
	 * 
	 * @param smiles
	 * @param options            standard InChI options and "fixedh?", meaning
	 *                           fixedh only if it is different from standard
	 * @return
	 */
	public static String getInChIFromSmiles(String smiles, String options) {
		StereoMolecule mol = new StereoMolecule();
		try {
			new SmilesParser().parse(mol, smiles);
			return getInChI(mol, options);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	///// molecule to InChIKey //

	/**
	 * Get an InChIKey from a StereoMolecule
	 * 
	 * @param mol
	 * @param options standard InChI options and "fixedh?", meaning fixedh only if
	 *                it is different from standard
	 * @return InChIKey
	 */
	public static String getInChIKey(StereoMolecule mol, String options) {
		return getPlatformSubclass().getInchiPvt(mol, null, options, true);
	}

	/**
	 * Get an InChIKey from V2 or V3 MOL file data or from an InChI (a string starting with "InChI=")
	 * 
	 * @param molFileDataOrInChI
	 * @param options            standard InChI options and "fixedh?", meaning
	 *                           fixedh only if it is different from standard
	 * @return InChIKey
	 */
	public static String getInChIKey(String molFileDataOrInChI, String options) {
		return getPlatformSubclass().getInchiPvt(null, molFileDataOrInChI, options, true);
	}

	///// InChI to StereoMolecule //

	/**
	 * Currently implemented
	 * 
	 * @param inchi
	 * @param mol
	 * @return
	 */
	public static boolean getMoleculeFromInChI(String inchi, StereoMolecule mol) {
		try {
			getPlatformSubclass().getOCLMoleculeFromInChI(inchi, mol);
			return true;
		} catch (Throwable e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/// to SMILES

	public static String getSmilesFromInChI(String inchi, String options) {
		StereoMolecule mol = new StereoMolecule();
		return (getMoleculeFromInChI(inchi, mol) ? IsomericSmilesCreator.createSmiles(mol) : null);
	}

	
	/// InChI to InChI model as JSON
	
	/**
	 * Starting with a standard or nonstandard (FixedH, for example), return a JSON
	 * string containing details of the internal inchi C model, including
	 * "atomCount", "atoms", "bondCount", "bonds", "stereoCount", and "stereo". The
	 * model includes explicit hydrogen atoms as well as implicit hydrogen counts. 
	 * Of significance, the "stereo" entries include center atom, an ordered list of 
	 * neighbors, a "and a parity flag "ODD" or "EVEN" that indicates the relation 
	 * 
	 * @param inchi
	 * @return
	 */
	public static String getInChIModelJSON(String inchi) {
		return getPlatformSubclass().getInchiPvt(null, inchi, "model", false);		
	}
	
	/**
	 * This private method selects between:
	 * 
	 * InChIJS -- JavaScript-WASM interface 
	 * 
	 * and 
	 * 
	 * InChIJNI1 -- JNI-InChI interface
	 * 
	 * @return the appropriate interface
	 */
	private static InChIOCL getPlatformSubclass() {
		return (isJS ? new InChIJS() : new InChIJNI1());
	}

	private String getInchiPvt(StereoMolecule mol, String molDataOrInChI, String options, boolean getKey) {
		try {
			if (mol == null && molDataOrInChI == null)
				return null;
			inchi = null;
			options = setParameters(options, molDataOrInChI, mol);
			getKey |= this.getKey;
			if (options == null)
				return "";
			if (inchi != null) {
				if (!getKey)
					return inchi;
				molDataOrInChI = inchi;
			} 
			if (molDataOrInChI != null && !inputInChI && !interfaceHasMolFileToInChI) {
				mol = new StereoMolecule();
				if (!new MolfileParser().parse(mol, molDataOrInChI))
					return null;
			}

			return getInchiImpl(mol, molDataOrInChI, options, getKey);
		} catch (Throwable e) {
			// oddly, e may be a string, not an error
			/**
			 * @j2sNative
			 * 
			 * 			e = (e.getMessage$ ? e.getMessage$() : e);
			 */
			{
			}
			System.err.println("InChIOCL exception: " + e);
			return null;
		}
	}
	
	protected boolean getInchiModel;
	protected boolean getKey;
	protected boolean inputInChI;
	protected String inchi;

	private String setParameters(String options, String molDataOrInChI, StereoMolecule mol) {
		if (mol == null ? molDataOrInChI == null : mol.getAtoms() == 0)
			return null;
		if (options == null)
			options = "";
		String inchi = null;
		String lc = options.toLowerCase().trim();
		boolean getInchiModel = (lc.indexOf("model") == 0);
		boolean getKey = (lc.indexOf("key") >= 0);
		if (lc.startsWith("model/")) {
			inchi = options.substring(10);
			options = lc = "";
		} else if (getInchiModel) {
			options = lc = lc.substring(5);
		}
		boolean optionalFixedH = (options.indexOf("fixedh?") >= 0);
		if (lc.indexOf("fixedh") < 0) {
			options = lc = lc.replace("standard",  "").trim();
		}

		boolean inputInChI = (molDataOrInChI != null && molDataOrInChI.startsWith("InChI="));
		if (!inputInChI) {
			options = lc;
			if (getKey) {
				options = options.replace("inchikey", "");
				options = options.replace("key", "");
			}
			if (optionalFixedH) {
				String fxd = getInchiPvt(mol, molDataOrInChI, options.replace('?', ' '), false);
				options = options.replace("fixedh?", "");
				String std = getInchiPvt(mol, molDataOrInChI, options, false);
				inchi = (fxd != null && fxd.length() <= std.length() ? std : fxd);
				options = null;
			}
		}
		this.inputInChI = inputInChI;
		this.inchi = inchi;
		this.getKey = getKey;
		this.getInchiModel = getInchiModel;
		return options;
	}

	/**
	 * the real thing
	 * 
	 * Create an OCL molecule from an InChI output structure.
	 * 
	 * @param struc
	 * @param mol
	 * @throws Exception 
	 */
	final protected void getOCLMoleculeFromInChI(String inchi, StereoMolecule mol) throws Exception {
		
		// uses subclass implementations
		initializeInchiModel(inchi);
		int nAtoms = getNumAtoms();
		int nBonds = getNumBonds();
		int nStereo = getNumStereo0D();
		for (int i = 0; i < nAtoms; i++) {
			setAtom(i);
			String sym = getElementType();
			// System.out.println("inchi " + i + "=" + sym);
			int atom = mol.addAtom(Molecule.getAtomicNoFromLabel(sym));
			mol.setAtomCharge(atom, getCharge());
		}
		Map<Integer, Integer> doubleBonds = new HashMap<>();
		for (int i = 0; i < nBonds; i++) {
			setBond(i);
			int i1 = getIndexOriginAtom();
			int i2 = getIndexTargetAtom();
			int bt = getOCLSimpleBondType(getInchiBondType());
			int bond = mol.addBond(i1, i2, bt);
			// System.out.println("inchi bond " + i + " " + i1 + " " + i2 + " " + bt);
			switch (bt) {
			case Molecule.cBondTypeSingle:
				break;
			case Molecule.cBondTypeDouble:
				doubleBonds.put(getBondKey(i1, i2), bond);
				break;
			}
		}
		for (int i = 0; i < nStereo; i++) {
			setStereo0D(i);
			int centerAtom = getCenterAtom();
			int[] neighbors = getNeighbors();
			if (neighbors.length != 4)
				continue;
			int p = -1;
			switch (getStereoType()) {
			case "TETRAHEDRAL":
				p = getOCLAtomParity(getParity(), isOrdered(neighbors));
				mol.setAtomParity(centerAtom, p, false);
				break;
			case "DOUBLEBOND":
				int ib = findDoubleBond(doubleBonds, neighbors);
				if (ib < 0) {
					System.err.println("InChIJNI cannot find double bond for atoms " + Arrays.toString(neighbors));
					continue;
				}
				p = getOCLBondParity(getParity(), (neighbors[0] < neighbors[3]));
				mol.setBondParity(ib, p, false);
				// System.out.println("ene " + ib + " " + p + " " + Arrays.toString(neighbors) +
				// " " + d.getParity());
				break;
			case "ALLENE":
				// reports low1-a1----a2-low2
				p = getOCLBondParity(getParity(), (neighbors[0] > neighbors[3]));
				mol.setAtomParity(centerAtom, p, false);
				// System.out.println("allene " + ic + " " + p + " " +
				// Arrays.toString(neighbors) + " " + d.getParity());
				break;
			case "NONE":
				continue;
			}
		}
		// temporarily preserve parities
		mol.setParitiesValid(0);
		// coordinates are not
		mol.setPrioritiesPreset(true);
		new CoordinateInventor(Canonizer.COORDS_ARE_3D);
		// | CoordinateInventor.MODE_SKIP_DEFAULT_TEMPLATES).invent(mol);
		mol.ensureHelperArrays(31);
		// mol.setPrioritiesPreset(false);
	}

	private static int getBondKey(int i1, int i2) {
		return (Math.min(i1, i2) << 16) + Math.max(i1, i2);
	}

	private static int findDoubleBond(Map<Integer, Integer> doubleBonds, int[] neighbors) {
		Integer ib = doubleBonds.get(getBondKey(neighbors[1],  neighbors[2]));
		return (ib == null ? -1 : ib.intValue());
	}

	private static int getOCLBondParity(String parity, boolean isReversed) {
		switch (parity) {
		case "ODD":
			// ODD means the high---low does NOT match the desired outcome
			return (isReversed ? Molecule.cBondParityZor2 : Molecule.cBondParityEor1);
		case "EVEN":
			// EVEN means the high--low DOES match the desired outcome
			return (isReversed ? Molecule.cBondParityEor1 : Molecule.cBondParityZor2);
		case "UNKNOWN":
			return Molecule.cBondParityUnknown;
		case "NONE":
		default:
			return Molecule.cBondParityNone;
		}
	}

	/**
	 * Determine whether this list is a permutation of an ordered list.
	 * 
	 * @param list
	 * @return true if even-order permuted
	 */
	private static boolean isOrdered(int[] list) {
		boolean ok = true;
		for (int i = 0; i < list.length - 1; i++) {
			int l1 = list[i];
			for (int j = i + 1; j < list.length; j++) {
				int l2 = list[j];
				if (l1 > l2) {
					list[j] = l1;
					l1 = list[i] = l2;
					ok = !ok;
				}
			}
		}
		return ok;
	}

	private static int getOCLAtomParity(String parity, boolean isOrdered) {
		boolean isOdd = false;
		switch (parity) {
		case "ODD":
			isOdd = true;
			//$FALL-THROUGH$
		case "EVEN":
			return (isOdd ^ isOrdered ? Molecule.cAtomParity1 : Molecule.cAtomParity2);
		case "UNKNOWN":
			return Molecule.cAtomParityUnknown;
		case "NONE":
		default:
			return Molecule.cAtomParityNone;
		}
	}

	private static int getOCLSimpleBondType(String type) {
		switch (type) {
		case "NONE":
			return 0;
		case "ALTERN":
			return Molecule.cBondTypeDelocalized;
		case "DOUBLE":
			return Molecule.cBondTypeDouble;
		case "TRIPLE":
			return Molecule.cBondTypeTriple;
		case "SINGLE":
		default:
			return Molecule.cBondTypeSingle;
		}
	}


}
