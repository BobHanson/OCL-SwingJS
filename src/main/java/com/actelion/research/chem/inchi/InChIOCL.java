package com.actelion.research.chem.inchi;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

import com.actelion.research.chem.Canonizer;
import com.actelion.research.chem.IsomericSmilesCreator;
import com.actelion.research.chem.Molecule;
import com.actelion.research.chem.SmilesParser;
import com.actelion.research.chem.StereoMolecule;
import com.actelion.research.chem.coords.CoordinateInventor;
import com.sun.jna.Native;

import io.github.dan2097.jnainchi.InchiAPI;
import io.github.dan2097.jnainchi.InchiAtom;
import io.github.dan2097.jnainchi.InchiBond;
import io.github.dan2097.jnainchi.InchiBondStereo;
import io.github.dan2097.jnainchi.InchiBondType;
import io.github.dan2097.jnainchi.InchiFlag;
import io.github.dan2097.jnainchi.InchiInput;
import io.github.dan2097.jnainchi.InchiKeyOutput;
import io.github.dan2097.jnainchi.InchiOptions;
import io.github.dan2097.jnainchi.InchiOptions.InchiOptionsBuilder;
import io.github.dan2097.jnainchi.InchiOutput;
import io.github.dan2097.jnainchi.InchiStereo;
import io.github.dan2097.jnainchi.JnaInchi;
import io.github.dan2097.jnainchi.inchi.InchiLibrary;

public class InChIOCL {

	/**
	 * allow testing performance of JnaInchi vs InchiAPI
	 * 
	 * Bob's initial results for frameless test:
	 * 
	 * JNAInchi: DONE 74 1205 ms (5 run average, in Eclipse)
	 * 
	 * 
	 * InchiAPI: DONE 74 1077 ms (5 run average, in Eclipse)
	 * 
	 * about 10% faster.
	 * 
	 * Leave this value TRUE -- false (use JnaInchi) is Java-only
	 * 
	 * 
	 */
	private static boolean isJS = /** @j2sNative true || */
			false;
	protected static boolean useIXA = true;// false;

	/**
	 * Just loads this class, initiating JavaScript loading of the WASM. ini Note
	 * that a clock tick is required to continue after this asynchronous load.
	 * 
	 * @param r
	 */
	public static void init(Runnable r) {
		if (isJS)
			useIXA = true;
		getInstance().initAndRun(r);
	}

	protected InchiInput inchiInput;

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
		return getInstance().getInChIPvt(mol, null, options, false);
	}

	/**
	 * Get an InChI from V2 or V3 MOL file data or from an InChI (a string starting
	 * with "InChI=")
	 * 
	 * @param molFileDataOrInChI V2 or V3 MOL file data or an InChI string
	 * @param options            standard InChI options and "fixedh?", meaning
	 *                           fixedh only if it is different from standard
	 * @return InChIKey
	 */
	public static String getInChI(String molFileDataOrInChI, String options) {
		return getInstance().getInChIPvt(null, molFileDataOrInChI, options, false);
	}

	/**
	 * Get an InChI from a SMILES string
	 * 
	 * @param smiles
	 * @param options standard InChI options and "fixedh?", meaning fixedh only if
	 *                it is different from standard
	 * @return
	 */
	public static String getInChIFromSmiles(String smiles, String options) {
		StereoMolecule mol = new StereoMolecule();
		try {
			new SmilesParser().parse(mol, smiles);
			return getInChI(mol, options);
		} catch (Exception e) {
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
		return getInstance().getInChIPvt(mol, null, options, true);
	}

	/**
	 * Get an InChIKey from V2 or V3 MOL file data or from an InChI (a string
	 * starting with "InChI=")
	 * 
	 * @param molFileDataOrInChI
	 * @param options            standard InChI options and "fixedh?", meaning
	 *                           fixedh only if it is different from standard
	 * @return InChIKey
	 */
	public static String getInChIKey(String molFileDataOrInChI, String options) {
		return getInstance().getInChIPvt(null, molFileDataOrInChI, options, true);
	}

	/**
	 * Get an InchiInput instance from an OCL molecule. Called by an
	 * 
	 * @param inchi
	 * @return InchiInput
	 */
	public static InchiInput getInchiInputFromInChI(String inchi, String moreOptions) {
		return getInstance().getInchiInputFromInChIPvt(inchi, moreOptions);
	}

	public static InchiInput getInchiInputFromOCLMolecule(StereoMolecule mol) {
		return getInstance().getInchiInputFromMolPvt(mol);
	}

	public static void getOCLMoleculeFromInchiInput(InchiInput input, StereoMolecule mol) throws Exception {
		getInstance().getMoleculeFromInchiInput(input, mol);
	}

	///// InChI to StereoMolecule //

	/**
	 * 
	 * @param inchi
	 * @param mol
	 * @return
	 */
	public static boolean getMoleculeFromInChIRaw(String inchi, StereoMolecule mol) {
		try {
			getInstance().getMoleculeFromInChIPvt(inchi, mol, null);
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
	 * @return SMILES; automatically fixes amides
	 * @throws Exception
	 */
	public static String getSmilesFromInChI(String inchi) throws Exception {
		StereoMolecule mol = new StereoMolecule();
		getMoleculeFromInChI(inchi, mol);
		return IsomericSmilesCreator.createSmiles(mol);
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
			getMoleculeFromInChIOpt(inchi, mol, null);
			String inchiS = getInChI(mol, "standard");
			if (inchiS == null || inchiS.length() == 0 || options == null || options.trim().length() == 0) {
				return inchiS;
			}
			mol = new StereoMolecule();
			getMoleculeFromInChIOpt(inchiS, mol, null);
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
		return getInstance().getInChIPvt(null, inchi, "model", false);
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
	private static InChIOCL getInstance() {
		return new InChIOCL();
	}

	protected boolean getInchiModel;
	protected boolean isInputInChI;
	protected boolean getKey;
	protected boolean isFixedH;

	protected String inchi;

	private StereoMolecule mol;
	protected boolean getAuxInfo;

	static int ntests = 0;

	/**
	 * 
	 * @param inputMol
	 * @param molDataOrInChI
	 * @param options
	 * @param getKey
	 * @return
	 */
	private String getInChIPvt(StereoMolecule inputMol, String molDataOrInChI, String options, boolean retKey) {
		if ("version".equals(options) || "description".equals(options)) {
			return getInChIVersion("description".equals(options));
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
			if (ret != null && options.length() == 0 && ret.startsWith("InChI=") && !ret.startsWith("InChI=1S/")) {
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
	 * @throws Exception
	 */
	private String setFieldsPvt(StereoMolecule mol, String molDataOrInChI, String options, boolean getKey)
			throws Exception {
		if (options == null)
			options = "";
		String lc = options.toLowerCase().trim();
		boolean getInchiModel = (lc.indexOf("model") == 0);
		boolean optionKey = (lc.indexOf("key") >= 0);
		boolean getAuxInfo = (lc.indexOf("auxinfo") >= 0);
		String inchi = this.inchi = null;
		boolean isFixedH = (lc.indexOf("fixedh") >= 0);
		boolean optionalFixedH = (lc.indexOf("fixedh?") >= 0);
		if (lc.indexOf("fixedh") < 0) {
			options = lc = lc.replace("standard", "");
		}
		boolean inputIsInChI = (molDataOrInChI != null && molDataOrInChI.startsWith("InChI="));
		if (!inputIsInChI) {
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
			inchi = getInChIOptionallyFixedH(mol, molDataOrInChI, inputIsInChI, lc);
			mol = null;
			molDataOrInChI = null;
			if (inchi == null)
				options = null;
		} else if (inputIsInChI && isFixedH) {
			// getInChIFromInChI(inchi,"fixedH")
			// but inchi.c cannot actually do this
			// so we first create a model, and get the inchi from that.
			mol = new StereoMolecule();
			getMoleculeFromInChI(molDataOrInChI, mol);
			inchi = null;
			inputIsInChI = false;
		}
		this.getInchiModel = getInchiModel;
		this.isInputInChI = (inputIsInChI || inchi != null);
		this.inchi = inchi;
		this.isFixedH = isFixedH;
		this.getKey = optionKey || getKey;
		this.mol = mol;
		this.getAuxInfo = getAuxInfo;
		return (options == null ? null : options.trim());
	}

	/**
	 * A general method for getting an InChI from a molecule, anothor InChI, or MOL
	 * data.
	 * 
	 * For option "fixedh?", optionally return standard or fixedH InChI. In this case, both
	 * are calculated, and the fixedH InChI is only returned if it has a /f layer
	 * 
	 * @param mol
	 * @param molDataOrInChI
	 * @param inputInChI     true if molDataOrInChI starts with "inchi=", otherwise assumed to be MOL file data
	 * @param options        coming in with "fixedh?" along with possibly other
	 *                       options
	 * @return
	 * @throws Exception
	 */
	private String getInChIOptionallyFixedH(StereoMolecule mol, String molDataOrInChI, 
			boolean inputInChI, String options) throws Exception {
		if (mol == null) {
			// create a mol if necessary only
			if (inputInChI) {
				// inchi from inchi
				// create a molecule from the first inchi
				// then create the inchi from that
				String inchi = molDataOrInChI;
				molDataOrInChI = null;
				mol = new StereoMolecule();
				getMoleculeFromInChI(inchi, mol);
			}
		}
		String fxd = getInChIPvt(mol, molDataOrInChI, options.replace('?', ' '), false);
		if (fxd == null) {
			// error reading mol data or inchi
			return null;
		}
		options = options.replace("fixedh?", "");
		// not that this next still might not be actually standard
		String std = getInChIPvt(mol, molDataOrInChI, options, false);
		return (fxd.indexOf("/f") < 0 ? std : fxd);
	}

	/**
	 * Create an OCL molecule from an InChI output structure.
	 * Iminols will be converted to amides.
	 * 
	 * @param struc
	 * @param mol
	 * @throws Exception
	 */
	public static boolean getMoleculeFromInChI(String inchi, StereoMolecule mol) {
		try {
			getMoleculeFromInChIOpt(inchi, mol, "fixamides");
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Allows the option of fixing amides
	 * @param inchi
	 * @param mol
	 * @param moreOptions  may be "fixamide"
	 * @return
	 */
	public static boolean getMoleculeFromInChIOpt(String inchi, StereoMolecule mol, String moreOptions) {
		try {
			getInstance().getMoleculeFromInChIPvt(inchi, mol, moreOptions);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Allow the option of fixing amides
	 * @param inchi
	 * @param mol
	 * @param moreOptions may be "fixamide"
	 * @throws Exception
	 */
	private void getMoleculeFromInChIPvt(String inchi, StereoMolecule mol, String moreOptions) throws Exception {
		getMoleculeFromInchiInput(getInchiInputFromInChI(inchi, moreOptions), mol);
	}

	public void getMoleculeFromInchiInput(InchiInput input, StereoMolecule mol) throws Exception {
		inchiInput = input;
		initializeInchiModel();
		int nAtoms = getNumAtoms();
		int nBonds = getNumBonds();
		int nStereo = getNumStereo0D();
		for (int i = 0; i < nAtoms; i++) {
			setAtom(i);
			String sym = getElementType();
			// System.out.println("inchi " + i + "=" + sym);
			int atom = mol.addAtom(Molecule.getAtomicNoFromLabel(sym));
			mol.setAtomCharge(atom, getCharge());
			int mass = getIsotopicMass();
			mol.setAtomMass(atom, mass);
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
		Integer ib = doubleBonds.get(getBondKey(neighbors[1], neighbors[2]));
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

	protected void initAndRun(Runnable r) {
		InchiAPI.initAndRun(r);
	}

	/**
	 * OCL molecule to InChI
	 * 
	 * @param mol
	 * @param options
	 * @return a string
	 */
	protected String getInchiImpl(StereoMolecule mol, String molFileDataOrInChI, String options) {
		try {
			String inchi = null;
			String moreOptions = (options.toLowerCase(Locale.ROOT).indexOf("fixamides") >= 0 ? "fixamides" : null);
			InchiOptions ops = getOptions(options);
			InchiOutput out = null;
			// set either inchi or InchiOutput
			if (isInputInChI) {
				if (getKey) {
					// inchi => inchikey
					inchi = molFileDataOrInChI;
					// get key below
				} else {
					// inchi => inchi
					out = inchiToInchi(molFileDataOrInChI, ops);
				}
			} else if (mol == null) {
				// molfile data => inchi
				out = molToInchi((String) molFileDataOrInChI, ops);
			} else {
				// molecule to inchi
				out = toInchi(getInchiInputFromOCLMolecule(mol), getOptions(options));
			}
			if (out != null) {
				if (getAuxInfo)
					return out.getAuxInfo();
				String msg = out.getMessage();
				if (msg != null)
					System.err.println(msg);
				inchi = out.getInchi();
			}
			if (getInchiModel) {
				// only not null now if we need a model.
				// get InchiInput from InchiOutput and generate JSON model
				return InchiAPI.getJSONFromInchiInput(getInchiInputFromInChI(inchi, moreOptions));
			}
			return (getKey ? inchiToInchiKey(inchi).getInchiKey() : inchi);
		} catch (Throwable e) {
			e.printStackTrace();
			return "";
		}

	}

	private static InchiOptions getOptions(String options) {
		InchiOptionsBuilder builder = new InchiOptionsBuilder();
		StringTokenizer t = new StringTokenizer(options);
		while (t.hasMoreElements()) {
			String o = t.nextToken();
			InchiFlag f = InchiFlag.getFlagFromName(o);
			if (f == null) {
				switch (o) {
				case "auxinfo":
					break;
				default:
					System.err.println("InChIJNA InChI option " + o + " not recognized -- ignored");
					break;
				}
			} else {
				builder.withFlag(f);
			}
		}
		return builder.build();
	}

	protected InchiInput getInchiInputFromMolPvt(StereoMolecule mol) {
		mol.ensureHelperArrays(Molecule.cHelperNeighbours);
		InchiInput struc = new InchiInput();
		int nAtoms = mol.getAllAtoms();
		InchiAtom[] atoms = new InchiAtom[nAtoms];
		BitSet bsCarbonAtoms = new BitSet();
		for (int i = 0; i < nAtoms; i++) {
			int elem = mol.getAtomicNo(i);
			String sym = Molecule.cAtomLabel[elem];
			int iso = mol.getAtomMass(i);
			if (elem == 1) {
				sym = "H"; // in case this is D
			} else if (elem == 6)
				bsCarbonAtoms.set(i);
			InchiAtom a = atoms[i] = new InchiAtom(sym, mol.getAtomX(i), -mol.getAtomY(i), mol.getAtomZ(i));
			a.setCharge(mol.getAtomCharge(i));
			if (iso > 0)
				a.setIsotopicMass(iso);
			a.setImplicitHydrogen(mol.getImplicitHydrogens(i));
		}
		int nBonds = mol.getAllBonds();
		BitSet bsDoubleBondAtoms = new BitSet();
		BitSet bsStereoAtoms = new BitSet();
		BitSet bsStereoBonds = new BitSet();
		InchiBond[] bonds = new InchiBond[nBonds];
		for (int i = 0; i < nBonds; i++) {
			int oclOrder = mol.getBondTypeSimple(i);
			InchiBondType order = getInChIOrder(oclOrder);
			if (order != null) {
				int atom1 = mol.getBondAtom(0, i);
				int atom2 = mol.getBondAtom(1, i);
				int oclType = mol.getBondType(i);
				int oclParity = mol.getBondParity(i);
				InchiBondStereo stereo = getInChIStereo(oclOrder, oclType, oclParity);
				switch (stereo) {
				case NONE:
					if (order == InchiBondType.DOUBLE) {
						bsDoubleBondAtoms.set(atom1);
						bsDoubleBondAtoms.set(atom2);
					}
					break;
				case SINGLE_1DOWN:
				case SINGLE_1UP:
					if (mol.getAllConnAtoms(atom1) == 3) {
						bsStereoBonds.set(i);
						bsStereoAtoms.set(atom1, !bsStereoAtoms.get(atom1));
					}
					break;
				default:
					break;

				}
				bonds[i] = new InchiBond(atoms[atom1], atoms[atom2], order, stereo);
			}
		}
		bsStereoAtoms.and(bsDoubleBondAtoms);
		bsStereoAtoms.and(bsCarbonAtoms);
		if (!bsStereoAtoms.isEmpty()) {
			checkAllenes(mol, atoms, bonds, bsStereoAtoms, bsStereoBonds);
		}

		for (int i = 0; i < nAtoms; i++) {
			struc.addAtom(atoms[i]);
		}
		for (int i = 0; i < nBonds; i++) {
			if (bonds[i] != null)
				struc.addBond(bonds[i]);
		}
		return struc;
	}

	/**
	 * We have found wedges or hashes starting on a double bonded carbon. This is
	 * almost certainly an allene. But there is only one stereochemical marking. So
	 * we need to add the other, or InChI will consider this an ambiguous marking.
	 * 
	 * InChI Technical Manual:
	 * 
	 * Allenes belong to the tetrahedral layer. However, to indicate stereochemistry
	 * of allenes in the input MOL-file a special effort may be required. Namely,
	 * the two bonds at the same end of allene system should be indicated by wedge
	 * as stereogenic (and having opposite Up/Down marks). This is a limitation of
	 * current InChI software (as per versions up to 1.04)
	 * 
	 * 
	 * @param mol
	 * @param atoms
	 * @param bonds
	 * @param bsStereoAtoms
	 * @param bsStereoBonds
	 */
	private static void checkAllenes(StereoMolecule mol, InchiAtom[] atoms, InchiBond[] bonds, BitSet bsStereoAtoms,
			BitSet bsStereoBonds) {
		// we have stereochemistry marked at double-bonded atoms.
		// make sure there are TWO such markings, opposite each other.

		for (int iatom = bsStereoAtoms.nextSetBit(0); iatom >= 0; iatom = bsStereoAtoms.nextSetBit(iatom + 1)) {
			// there should be VERY few of these.
			int ibondStereo = -1, ibondNone = -1;
			int n = mol.getAllConnAtoms(iatom);
			for (int i = 0; i < n; i++) {
				int ibond = mol.getConnBond(iatom, i);
				if (mol.getConnBondOrder(iatom, i) == 1) {
					if (bsStereoBonds.get(ibond))
						ibondStereo = ibond;
					else
						ibondNone = ibond;
				}
			}
			if (ibondNone >= 0 && ibondStereo >= 0) {
				InchiBondStereo stereo = (bonds[ibondStereo].getStereo() == InchiBondStereo.SINGLE_1UP
						? InchiBondStereo.SINGLE_1DOWN
						: InchiBondStereo.SINGLE_1UP);
				InchiAtom iatom1 = bonds[ibondNone].getStart();
				InchiAtom iatom2 = bonds[ibondNone].getEnd();

				boolean isAtom1 = (iatom1 == atoms[iatom]);
				bonds[ibondNone] = new InchiBond(isAtom1 ? iatom1 : iatom2, isAtom1 ? iatom2 : iatom1,
						InchiBondType.SINGLE, stereo);
			}
		}
	}

	private static InchiBondType getInChIOrder(int oclOrder) {
		switch (oclOrder) {
		case Molecule.cBondTypeSingle:
			return InchiBondType.SINGLE;
		case Molecule.cBondTypeDouble:
			return InchiBondType.DOUBLE;
		case Molecule.cBondTypeTriple:
			return InchiBondType.TRIPLE;
		case Molecule.cBondTypeDelocalized:
			return InchiBondType.ALTERN;
		case Molecule.cBondTypeMetalLigand:
		default:
			return null;
		}
	}

	private static InchiBondStereo getInChIStereo(int oclOrder, int oclType, int oclParity) {
		if (oclOrder == 1) {
			switch (oclType) {
			case Molecule.cBondTypeDown:
				return InchiBondStereo.SINGLE_1DOWN;
			case Molecule.cBondTypeUp:
				return InchiBondStereo.SINGLE_1UP;
			default:
				if (oclParity == Molecule.cBondParityUnknown) {
					return (oclOrder == Molecule.cBondTypeDouble ? InchiBondStereo.DOUBLE_EITHER
							: InchiBondStereo.SINGLE_1EITHER);
				}
			}
		}
		return InchiBondStereo.NONE;
	}

	protected void initializeInchiModel() throws Exception {
		for (int i = getNumAtoms(); --i >= 0;)
			map.put(inchiInput.getAtom(i), Integer.valueOf(i));
	}

	private Map<InchiAtom, Integer> map = new Hashtable<InchiAtom, Integer>();

	private InchiAtom thisAtom;
	private InchiBond thisBond;
	private InchiStereo thisStereo;

	/// atoms ///

	protected int getNumAtoms() {
		return inchiInput.getAtoms().size();
	}

	protected void setAtom(int i) {
		thisAtom = inchiInput.getAtom(i);
	}

	protected String getElementType() {
		return thisAtom.getElName();
	}

	protected double getX() {
		return thisAtom.getX();
	}

	protected double getY() {
		return thisAtom.getY();
	}

	protected double getZ() {
		return thisAtom.getZ();
	}

	protected int getCharge() {
		return thisAtom.getCharge();
	}

	protected int getIsotopicMass() {
		return InchiUtils.getActualMass(getElementType(), thisAtom.getIsotopicMass());
	}

	protected int getImplicitH() {
		return thisAtom.getImplicitHydrogen();
	}

	/// bonds ///

	protected int getNumBonds() {
		return inchiInput.getBonds().size();
	}

	protected void setBond(int i) {
		thisBond = inchiInput.getBond(i);
	}

	protected int getIndexOriginAtom() {
		return map.get(thisBond.getStart()).intValue();
	}

	protected int getIndexTargetAtom() {
		return map.get(thisBond.getEnd()).intValue();
	}

	protected String getInchiBondType() {
		InchiBondType type = thisBond.getType();
		return type.name();
	}

	/// Stereo ///

	protected int getNumStereo0D() {
		return inchiInput.getStereos().size();
	}

	protected void setStereo0D(int i) {
		thisStereo = inchiInput.getStereos().get(i);
	}

	protected int[] getNeighbors() {
		InchiAtom[] an = thisStereo.getAtoms();

		int n = an.length;
		int[] a = new int[n];

		// add for loop
		for (int i = 0; i < n; i++) {
			a[i] = map.get(an[i]).intValue();
		}
		return a;
	}

	protected int getCenterAtom() {
		InchiAtom ca = thisStereo.getCentralAtom();
		return (ca == null ? -1 : map.get(ca).intValue());
	}

	protected String getStereoType() {
		return uc(thisStereo.getType());
	}

	protected String getParity() {
		return uc(thisStereo.getParity());
	}

	private static String uc(Object o) {
		return o.toString().toUpperCase(Locale.ROOT);
	}

	private static String inchiVersionInternal;

	/**
	 * Get the InChI version directly from the inchi code without an API. To be
	 * replaced in the future with a simple inchi IXA call?
	 * 
	 * Future format may change.
	 * 
	 * @param f
	 * @return something like "InChI version 1, Software 1.07.2 (API Library)"
	 */
	public static String getInChIVersion(boolean fullDescription) {
		if (useIXA)
			return InchiAPI.getInChIVersion(fullDescription);
		if (inchiVersionInternal == null) {
			// this will not work in JavaScript yet
			File f = InchiLibrary.JNA_NATIVE_LIB.getFile();
			inchiVersionInternal = extractInchiVersionInternal(f);
			if (inchiVersionInternal == null) {
				// linux will be here after Native libary deletes the file
				try {
					// that's OK; we can get it ourselves
					f = Native.extractFromResourcePath(InchiLibrary.JNA_NATIVE_LIB.getName());
					inchiVersionInternal = extractInchiVersionInternal(f);
				} catch (Exception e) {
				}
			}
			if (inchiVersionInternal == null)
				inchiVersionInternal = "unknown";
		}
		return inchiVersionInternal;
	}

	/**
	 * Java only
	 * 
	 * @param f
	 * @return APP_DESCRIPTION
	 */
	private static String extractInchiVersionInternal(File f) {
		System.out.println(f);
		String s = null;
		try (FileInputStream fis = new FileInputStream(f)) {
			byte[] b = new byte[(int) f.length()];
			fis.read(b);
			s = new String(b);
			int pt = s.indexOf("InChI version");
			if (pt < 0) {
				s = null;
			} else {
				s = s.substring(pt, s.indexOf('\0', pt));
			}
			fis.close();
			f.delete();
		} catch (Exception e) {
			// System.out.println(f);
			// e.printStackTrace();
			// it's gone already in Linux
		}
		return s;
	}

	protected InchiInput getInchiInputFromInChIPvt(String inchi, String moreOptions) {
		if (useIXA) {
			// InChIAPI makes this more explicit
			// original is now InchiAPI.getInchiInputFromInChIOutputFromInchi(inchi, moreOptions);
			return InchiAPI.getInchiInputFromInChI(inchi, moreOptions);
		} else {
			// JnaInchi does not support "fixamide"
			return JnaInchi.getInchiInputFromInchi(inchi).getInchiInput();
		}
	}

	private InchiKeyOutput inchiToInchiKey(String inchi) {
		if (useIXA) {
			return InchiAPI.inchiToInchiKey(inchi);
		} else {
			return JnaInchi.inchiToInchiKey(inchi);
		}
	}

	private InchiOutput toInchi(InchiInput input, InchiOptions options) {
		if (useIXA) {
			return InchiAPI.toInchi(input, options);
		} else {
			return JnaInchi.toInchi(input, options);
		}
	}

	private InchiOutput molToInchi(String molFileData, InchiOptions options) {
		if (useIXA) {
			return InchiAPI.molFileToInchi(molFileData, options);
		} else {
			return JnaInchi.molToInchi(molFileData, options);
		}
	}

	private InchiOutput inchiToInchi(String inchi, InchiOptions options) {
		if (useIXA) {
			return InchiAPI.inchiToInchi(inchi, options);
		} else {
			return JnaInchi.inchiToInchi(inchi, options);
		}
	}

}
