package com.actelion.research.chem.inchi;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.iupac.InChIStructureProvider;

import com.actelion.research.chem.Canonizer;
import com.actelion.research.chem.IsomericSmilesCreator;
import com.actelion.research.chem.Molecule;
import com.actelion.research.chem.StereoMolecule;
import com.actelion.research.chem.coords.CoordinateInventor;

public abstract class InChIOCL implements InChIStructureProvider {

	private final static boolean jniHasMolFileToInChI = false;

	protected abstract String getInChIimpl(StereoMolecule mol, String molData, String options, boolean isKey);

	protected abstract boolean getMoleculeFromInchiStringImpl(String inchi, StereoMolecule mol);

	/**
	 * This private method selects among:
	 * 
	 * InChIJS -- JavaScript-WASM interface
	 * 
	 * InChIJNI0 -- First-generation JNI interface that does not have molFileToInChI
	 * (will be removed)
	 * 
	 * InChIJNI -- Second-generation JNI interface that includes molFileToInChI
	 * 
	 * @return
	 */
	@SuppressWarnings("unused")
	private static InChIOCL getInChIOCL() {
		return (/** j2sNative true || */
		false) ? new InChIJS() : jniHasMolFileToInChI ? new InChIJNI() : new InChIJNI0();
	}

	///// to InChI //

	/**
	 * Get an InChIKey based on options
	 * 
	 * @param mol
	 * @param options standard InChI options and "fixedh?", meaning fixedh only if
	 *                it is different from standard
	 * @return InChIKey
	 */
	public static String getInChIKey(StereoMolecule mol, String options) {
		return getInChIOCL().getInChIimpl(mol, null, options, true);
	}

	/**
	 * Get an InChIKey based on options
	 * 
	 * @param molFileDataOrInChI
	 * @param options            standard InChI options and "fixedh?", meaning
	 *                           fixedh only if it is different from standard
	 * @return InChIKey
	 */
	public static String getInChIKey(String molFileDataOrInChI, String options) {
		return getInChIOCL().getInChIimpl(null, molFileDataOrInChI, options, true);
	}

	/**
	 * Get an InChIKey based on options
	 * 
	 * @param mol
	 * @param options standard InChI options and "fixedh?", meaning fixedh only if
	 *                it is different from standard
	 * @return InChIKey
	 */
	public static String getInChI(StereoMolecule mol, String options) {
		return getInChIOCL().getInChIimpl(mol, null, options, true);
	}

	/**
	 * Get an InChIKey based on options
	 * 
	 * @param molFileDataOrInChI
	 * @param options            standard InChI options and "fixedh?", meaning
	 *                           fixedh only if it is different from standard
	 * @return InChIKey
	 */
	public static String getInChI(String molFileDataOrInChI, String options) {
		return getInChIOCL().getInChIimpl(null, molFileDataOrInChI, options, true);
	}

	///// to StereoMolecule //

	/**
	 * Currently implemented
	 * 
	 * @param inchi
	 * @param mol
	 * @return
	 */
	public static boolean getMoleculeFromInChI(String inchi, StereoMolecule mol) {
		try {
			getInChIOCL().getOCLMolecule(inchi, mol);
			return true;
		} catch (Throwable e) {
			e.printStackTrace();
			return false;
		}
	}

	public static String getSmilesFromInChI(String inchi) {
		StereoMolecule mol = new StereoMolecule();
		return (getMoleculeFromInChI(inchi, mol) ? IsomericSmilesCreator.createSmiles(mol) : null);
	}


	
	/**
	 * Get an InChI based on options
	 * 
	 * @param molfileDataOrInChI
	 * @param options            standard InChI options and "fixedh?", meaning
	 *                           fixedh only if it is different from standard
	 * @return InChIKey
	 */
	protected static String getInChI(StereoMolecule mol, String molfileDataOrInChI, String options) {
		return getInChIOCL().getInChIimpl(null, molfileDataOrInChI, options, false);
	}

	/**
	 * the real thing
	 * 
	 * Create an OCL molecule from an InChI output structure.
	 * 
	 * @param struc
	 * @param mol
	 */
	final protected void getOCLMolecule(String inchi, StereoMolecule mol) {
		
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

	protected boolean inputInChI;
	protected String inchi;
	protected boolean getInchiModel;
	protected boolean getKey;
	protected boolean fixedH;

	protected String setParameters(String options, String molData, StereoMolecule mol) {
		if (mol == null ? molData == null : mol.getAtoms() == 0)
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
			lc = lc.substring(5);
		}
		boolean optionalFixedH = (options.indexOf("fixedh?") >= 0);
		boolean inputInChI = (molData instanceof String && ((String) molData).startsWith("InChI="));
		if (inputInChI) {
			inchi = (String) molData;
		} else {
			options = lc;
			if (getKey) {
				options = options.replace("inchikey", "");
				options = options.replace("key", "");
			}

			if (optionalFixedH) {
				String fxd = getInChIimpl(mol, molData, options.replace('?', ' '), false);
				options = options.replace("fixedh?", "");
				String std = getInChIimpl(mol, molData, options, false);
				inchi = (fxd != null && fxd.length() <= std.length() ? std : fxd);
			}
		}
		this.fixedH = optionalFixedH;
		this.inputInChI = inputInChI;
		this.inchi = inchi;
		this.getKey = getKey;
		this.getInchiModel = getInchiModel;
		return options;
	}

}
