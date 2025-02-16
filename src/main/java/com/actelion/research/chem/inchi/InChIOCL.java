package com.actelion.research.chem.inchi;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.iupac.InChIStructureProvider;

import com.actelion.research.chem.Canonizer;
import com.actelion.research.chem.IsomericSmilesCreator;
import com.actelion.research.chem.Molecule;
import com.actelion.research.chem.MolfileCreator;
import com.actelion.research.chem.SmilesParser;
import com.actelion.research.chem.StereoMolecule;
import com.actelion.research.chem.coords.CoordinateInventor;

public abstract class InChIOCL implements InChIStructureProvider {

	/**
	 * Just loads this class, initiating JavaScript loading of the WASM.
	 * ini
	 * Note that a clock tick is required to continue after this asynchronous load.
	 * @param r 
	 */
	public static void init(Runnable r) {
		getPlatformSubclass().initAndRun(r);
	}

	protected abstract void initAndRun(Runnable r);

	/**
	 * Do not set this final, as the java2script transpiler needs to evaluate the javadoc.
	 */
	private /*nonfinal*/ static boolean isJS = /** @j2sNative true || */Boolean.FALSE.booleanValue();
	

	protected abstract boolean implementsMolDataOnlyToInChI();

	protected abstract String getInchiImpl(StereoMolecule mol, String molFileDataOrInChI, String options);




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

	/**
	 * 
	 * @param inchi
	 * @param options unused currently
	 * @return
	 */
	public static String getSmilesFromInChI(String inchi, String options) {
		StereoMolecule mol = new StereoMolecule();
		return (getMoleculeFromInChI(inchi, mol) ? IsomericSmilesCreator.createSmiles(mol) : null);
	}

	///// InChI to StereoMolecule //

	/**
	 * Retrieve a standard InChI or the fixed-H InChI from the given InChI.
	 * 
	 * If the option is "reference", then the reference fixed-H InChI is returned,
	 * provided there is a fixed hydrogen layer. Otherwise, the standard InChI is
	 * returned.
	 * 
	 * 
	 * @param inchi
	 * @param options e.g. FIXEDH
	 * @return inchi or null
	 */
	public static String getInChIFromInChI(String inchi, String options) {
		boolean isReference = ("reference".equals(options));
		try {
			StereoMolecule mol = new StereoMolecule();
			getMoleculeFromInChI(inchi, mol);
			String inchiS = getInChI(mol, "standard");
			if (inchiS == null || inchiS.length() == 0 || options == null || options.trim().length() == 0) {
				return inchiS;
			}
			mol = new StereoMolecule();
			getMoleculeFromInChI(inchiS, mol);				
			inchi = getInChI(mol, isReference ? "fixedh" : options);
			return (isReference && inchi.length() < inchiS.length() ? inchiS : inchi);
		} catch (Throwable e) {
			e.printStackTrace();
			return null;
		}
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
		return (isJS ? new InChIJS() : new InChIJNA());
	}

	protected boolean getInchiModel;
	protected boolean isInputInChI;
	protected boolean getKey;
	protected boolean isFixedH;
	
	protected String inchi;

	private StereoMolecule mol;

	static int ntests = 0;

	/**
	 * 
	 * @param inputMol
	 * @param molDataOrInChI
	 * @param options
	 * @param getKey
	 * @return
	 */
	private String getInchiPvt(StereoMolecule inputMol, String molDataOrInChI, String options, boolean retKey) {
		if ("version".equals(options)) {
			return getInchiImpl(null, null, "version");
		}
		System.out.println("test " + ++ntests);
		try {
			if (inputMol == null && (molDataOrInChI == null || molDataOrInChI.length() == 0))
				return null; // this is an error return
			if (inputMol != null && inputMol.getAllAtoms() == 0) {
				// not an error, just no atoms in the molecule
				return "";
			}
			options = setFieldsPvt(inputMol, molDataOrInChI, options, retKey);
			if (options == null) { 
				// probably an error processing mol or inchi data
				return null; 
			}
			if (mol != null)
				molDataOrInChI = null;
			if (inchi != null) {
				// preprocessing has already calculating to the inchi
				// either the return InChI or the InChI to use for InChIKey
				if (!getKey || inchi.length() == 0)
					return inchi;
				mol = null;
				molDataOrInChI = inchi;
			}
			String ret = getInchiImpl(mol, molDataOrInChI, options);
			if (ret != null && options.length() == 0 
					&& ret.startsWith("InChI=") 
					&& !ret.startsWith("InChI=1S/")) {
				reportInchicError("inchi C inchifrom standard InChI without '1S/'! Fixing...");
				ret = "InChI=1S/" + ret.substring(8);
			}				
			return ret;
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
	
	protected void reportInchicError(String msg) {
	    System.out.flush();
		System.err.println(msg);
		System.err.flush();
	}


	/**
	 * 
	 * @param mol
	 * @param molDataOrInChI
	 * @param options
	 * @return options to pass on, or null if there is an error
	 */
	private String setFieldsPvt(StereoMolecule mol, String molDataOrInChI, String options, boolean getKey) {
		if (options == null)
			options = "";
		String lc = options.toLowerCase().trim();
		boolean getInchiModel = (lc.indexOf("model") == 0);
		boolean optionKey = (lc.indexOf("key") >= 0);
		String inchi = this.inchi = null;
		boolean isFixedH = (lc.indexOf("fixedh") >= 0);
		boolean optionalFixedH = (lc.indexOf("fixedh?") >= 0);
		if (lc.indexOf("fixedh") < 0) {
			options = lc = lc.replace("standard", "");
		}
		boolean inputInChI = (molDataOrInChI != null && molDataOrInChI.startsWith("InChI="));
		if (!inputInChI) {
			options = lc;
			if (optionKey) {
				// remove any key-based options
				options = options.replace("inchikey", "");
				options = options.replace("key", "");
			}
		}
		if (getInchiModel) {
			// note that if we are getting the inchi model, there is no
			// point in saying "fixedh" or "fixedh?", as the model is the same
			optionKey = isFixedH = optionalFixedH = false;
			options = "";
		}
		if (optionalFixedH) {
			// we do this here because we will re-enter
			// this.inchi will be set to a non-null value if no error
			inchi = getInChIOptionallyFixedH(mol, molDataOrInChI, inputInChI, lc);
			mol = null;
			molDataOrInChI = null;
			if (inchi == null)
				options = null;
		} else if (inputInChI && isFixedH) {
			// getInChIFromInChI(inchi,"fixedH")
			// but inchi.c cannot actually do this
			// so we first create a model, and get the inchi from that.
			mol = new StereoMolecule();
			getMoleculeFromInChI(molDataOrInChI, mol);
			inchi = null;
			inputInChI = false;
		}
		this.getInchiModel = getInchiModel;
		this.isInputInChI = (inputInChI || inchi != null);
		this.inchi = inchi;
		this.isFixedH = isFixedH;
		this.getKey = optionKey || getKey;
		this.mol = mol;
		return (options == null ? null : options.trim());
	}

	/**
	 * Optionally return standard or fixedH InChI.
	 * 
	 * The fixedH InChI is only returned if it has a /f layer (i.e. its length is
	 * longe than the standard InChI.
	 * 
	 * @param mol
	 * @param molDataOrInChI
	 * @param inputInChI     true if molDataOrInChI starts with "inchi="
	 * @param options        coming in with "fixedh?" along with possibly other
	 *                       options
	 * @return
	 */
	private String getInChIOptionallyFixedH(StereoMolecule mol, String molDataOrInChI, boolean inputInChI,
			String options) {
		if (mol == null) {
			// create a mol if necessary only
			boolean mustUseMolData= implementsMolDataOnlyToInChI();
			if (inputInChI) {
				// inchi from inchi
				// create a molecule from the first inchi
				// then create the inchi from that
				mol = new StereoMolecule();
				getMoleculeFromInChI(molDataOrInChI, mol);
				if (mustUseMolData) {
					// WASM only
					molDataOrInChI = new MolfileCreator(mol).getMolfile();
					mol = null;
				} else {
					molDataOrInChI = null;
				}
			}
		}
		String fxd = getInchiPvt(mol, molDataOrInChI, options.replace('?', ' '), false);
		if (fxd == null) {
			// error reading mol data or inchi
			return null;
		}
		options = options.replace("fixedh?", "");
		// not that this next still might not be actually standard
		String std = getInchiPvt(mol, molDataOrInChI, options, false);
		return (fxd.indexOf("/f") < 0 ? std : fxd);
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
	final private void getOCLMoleculeFromInChI(String inchi, StereoMolecule mol) throws Exception {
		
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
		mol.setParitiesPreset(true);
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
