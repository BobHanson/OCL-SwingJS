/*
 * Copyright 2006-2011 Sam Adams <sea36 at users.sourceforge.net>
 *
 * This file is part of JNI-InChI.
 *
 * JNI-InChI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JNI-InChI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with JNI-InChI.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.actelion.research.chem.inchi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.actelion.research.chem.Canonizer;
import com.actelion.research.chem.IsomericSmilesCreator;
import com.actelion.research.chem.Molecule;
import com.actelion.research.chem.StereoMolecule;
import com.actelion.research.chem.coords.CoordinateInventor;

import net.sf.jniinchi.INCHI_BOND_STEREO;
import net.sf.jniinchi.INCHI_BOND_TYPE;
import net.sf.jniinchi.INCHI_PARITY;
import net.sf.jniinchi.JniInchiAtom;
import net.sf.jniinchi.JniInchiBond;
import net.sf.jniinchi.JniInchiInput;
import net.sf.jniinchi.JniInchiInputInchi;
import net.sf.jniinchi.JniInchiOutput;
import net.sf.jniinchi.JniInchiOutputStructure;
import net.sf.jniinchi.JniInchiStereo0D;
import net.sf.jniinchi.JniInchiStructure;
import net.sf.jniinchi.JniInchiWrapper;

public class InChIJNI {

	// all methods are static, but we still need a public constructor
	// for dynamic class loading in JavaScript. (Or do we? Maybe not in SwingJS
	
	public InChIJNI() {
		// for dynamic loading in JavaScript
	}

	public static boolean inchiToMolecule(String inchi, StereoMolecule mol) {
		try {
			JniInchiOutputStructure struc = JniInchiWrapper.getStructureFromInchi(new JniInchiInputInchi(inchi));
			getOCLMolecule(struc, mol);
			return true;
		} catch (Throwable e) {
			e.printStackTrace();
			return false;
		}
	}

	public static String inchiToSMILES(String inchi) {
		try {
			StereoMolecule mol = new StereoMolecule();
			inchiToMolecule(inchi, mol);
			return IsomericSmilesCreator.createSmiles(mol);
		} catch (Throwable e) {
			e.printStackTrace();
			return null;
		}
	}

	static String toJSON(JniInchiStructure mol) {
		int na = mol.getNumAtoms();
		int nb = mol.getNumBonds();
		int ns = mol.getNumStereo0D();
		Map<JniInchiAtom, Integer> mapAtoms = new HashMap<>();
		String s = "{\"atoms\":[\n";
		for (int i = 0; i < na; i++) {
			JniInchiAtom a = mol.getAtom(i);
			mapAtoms.put(a, Integer.valueOf(i));
			if (i > 0)
				s += ",\n";
			s += "{\n";
			s += toJSON("index", Integer.valueOf(i), ",");
			s += toJSON("elementType", a.getElementType(), ",");
			s += toJSON("charge", a.getCharge(), ",");
			s += toJSON("isotopeMass", a.getIsotopicMass(), ",");
			s += toJSON("implicitH", a.getImplicitH(), ",");
			s += toJSON("radical", a.getRadical(), ",");
			s += toJSON("x", a.getX(), ",");
			s += toJSON("y", a.getY(), ",");
			s += toJSON("z", a.getZ(), ",");
			s += toJSON("implicitDeuterium", a.getImplicitDeuterium(), ",");
			s += toJSON("implicitProtium", a.getImplicitProtium(), ",");
			s += toJSON("implicitTritium", a.getImplicitTritium(), "");
			s += "}";
		}
		s += "\n],\n\"bonds\":[\n";

		for (int i = 0; i < nb; i++) {
			if (i > 0)
				s += ",\n";
			s += "{\n";
			JniInchiBond b = mol.getBond(i);
			s += toJSON("originAtom", mapAtoms.get(b.getOriginAtom()), ",");
			s += toJSON("targetAtom", mapAtoms.get(b.getTargetAtom()), ",");
			s += toJSON("bondType", b.getBondType(), ",");
			s += toJSON("bondStereo", b.getBondStereo(), "");
			s += "}";
		}
		s += "\n],\n\"stereo\":[\n";
		for (int i = 0; i < ns; i++) {
			if (i > 0)
				s += ",\n";
			s += "{\n";
			JniInchiStereo0D d = mol.getStereo0D(i);
			s += toJSON("centralAtomID", mapAtoms.get(d.getCentralAtom()), ",");
			s += toJSON("debugString", d.getDebugString(), ",");
			s += toJSON("disconnectedParity", d.getDisconnectedParity(), ",");
			s += toJSON("parity", d.getParity(), ",");
			s += toJSON("stereoType", d.getStereoType(), ",");
			JniInchiAtom[] an = d.getNeighbors();
			int[] nbs = new int[an.length];
			for (int j = 0; j < an.length; j++) {
				nbs[j] = mapAtoms.get(d.getNeighbor(j)).intValue();
			}
			s += toJSON("neighbors", nbs, "");
			s += "}";
		}
		s += "\n]}\n";
		return s;
	}

	private static String toJSON(String key, Object val, String term) {
		key = "\"" + key + "\"";
		String sval = null;
		if (val instanceof int[]) {
			sval = "";
			int[] a = (int[]) val;
			for (int i = 0; i < a.length; i++) {
				sval += "," + a[i];
			}
			sval = "[" + (sval.length() > 1 ? sval.substring(1) : "") + "]";
		} else if (val instanceof String) {
			sval = "\"" + val + "\"";
		} else {
			sval = "" + sval;
		}
		return key + ":" + sval + term + "\n";
	}

	private static void getOCLMolecule(JniInchiOutputStructure struc, StereoMolecule mol) {
		int nAtoms = struc.getNumAtoms();
		int nBonds = struc.getNumBonds();
		int nStereo = struc.getNumStereo0D();
		List<JniInchiAtom> atoms = new ArrayList<>();
		Map<JniInchiAtom, Integer> map = new HashMap<>();
		for (int i = 0; i < nAtoms; i++) {
			JniInchiAtom a = struc.getAtom(i);
			atoms.add(a);
			String sym = a.getElementType();
			int nH = a.getImplicitH();
			//System.out.println("inchi " + i + "=" + sym + (nH == 0 ? "" : "H" + nH));
			int atom = mol.addAtom(Molecule.getAtomicNoFromLabel(sym));
			mol.setAtomCharge(atom, a.getCharge());
			map.put(a, Integer.valueOf(i));
		}
		for (int i = 0; i < nStereo; i++) {
			JniInchiStereo0D d = struc.getStereo0D(i);
			JniInchiAtom atom = d.getCentralAtom();
			int ia = Integer.valueOf(map.get(atom));
			int[] neighbors = new int[d.getNeighbors().length];
			for (int j = neighbors.length; --j >= 0;) {
				neighbors[j] = Integer.valueOf(map.get(d.getNeighbor(j)));
			}
			int p = getOCLParity(ia, d.getParity(), isOrdered(neighbors));
			//System.out.println("inchi " + ia + " " + Arrays.toString(neighbors) + " " + p);
			mol.setAtomParity(ia, p, false);
		}
		for (int i = 0; i < nBonds; i++) {
			JniInchiBond b = struc.getBond(i);
			JniInchiAtom a1 = b.getOriginAtom();
			JniInchiAtom a2 = b.getTargetAtom();
			int bt = getOCLSimpleBondType(b);
			int i1 = map.get(a1);
			int i2 = map.get(a2);
			mol.addBond(i1, i2, bt);
		}
		// temporarily preserve parities
//		mol.copyMolecule(molOut);
//		enabled[0] = true;
		mol.setParitiesValid(0);
		// coordinates are not 
		mol.setPrioritiesPreset(true);
		new CoordinateInventor(Canonizer.COORDS_ARE_3D | CoordinateInventor.MODE_SKIP_DEFAULT_TEMPLATES).invent(mol);
		mol.ensureHelperArrays(31);
		//mol.setPrioritiesPreset(false);
//		enabled[0] = false;
		//mol.copyMolecule(molOut);
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

	private static int getOCLParity(int ia, INCHI_PARITY parity, boolean isOrdered) {
		switch (parity) {
		case ODD:
		case EVEN:
			return ((parity == INCHI_PARITY.ODD) ^ isOrdered ? Molecule.cAtomParity1 : Molecule.cAtomParity2);
		case UNKNOWN:
			return Molecule.cAtomParityUnknown;
		case NONE:
		default:
			return Molecule.cAtomParityNone;
		}
	}

	private static int getOCLSimpleBondType(JniInchiBond b) {
		INCHI_BOND_TYPE type = b.getBondType();
		switch (type) {
		case NONE:
			return 0;
		case ALTERN:
			return Molecule.cBondTypeDelocalized;
		case DOUBLE:
			return Molecule.cBondTypeDouble;
		case TRIPLE:
			return Molecule.cBondTypeTriple;
		case SINGLE:
		default:
			return Molecule.cBondTypeSingle;
		}
 		
	}

	// TODO enumerate all options
	
	/**
	 * Get an InChIKey based on options
	 * @param mol
	 * @param options standard InChI options and "fixedh?", meaning fixedh only if it is different from standard
	 * @return InChIKey
	 */
	public static String getInChIKey(StereoMolecule mol, String options) {
		return getInChI(mol, options, true);
	}

	/**
	 * Get an InChI based on options
	 * @param mol
	 * @param options standard InChI options and "fixedh?", meaning fixedh only if it is different from standard
	 * @return InChIKey
	 */
	public static String getInChI(StereoMolecule mol, String options) {
		return getInChI(mol, options, false);
	}
	
	private static String getInChI(StereoMolecule mol, String options, boolean getKey) {
		try {
			if (options == null)
				options = "";
			String inchi = null;
			String lc = options.toLowerCase();
			options = lc;
			if (options.indexOf("fixedh?") >= 0) {
				String fxd = getInChI(mol, options.replace('?', ' '), false);
				options = options.replaceAll("fixedh\\?", "");
				String std = getInChI(mol, options, false);
				inchi = (fxd != null && fxd.length() <= std.length() ? std : fxd);
			} else {
				JniInchiInput in = new JniInchiInput(options);
				JniInchiStructure struc = newJniInchiStructure(mol);
				in.setStructure(struc);
				JniInchiOutput out = JniInchiWrapper.getInchi(in);
				String msg = out.getMessage();
				if (msg != null)
					System.err.println(msg);
				inchi = out.getInchi();
			}
			return (getKey ? JniInchiWrapper.getInchiKey(inchi).getKey() : inchi);
		} catch (Throwable e) {
			System.out.println(e);

			if (e.getMessage().indexOf("ption") >= 0)
				System.out.println(e.getMessage() + ": " + options.toLowerCase()
						+ "\n See https://www.inchi-trust.org/download/104/inchi-faq.pdf for valid options");
			else
				e.printStackTrace();
			return "";
		}

	}
	private static JniInchiStructure newJniInchiStructure(StereoMolecule mol) {
		JniInchiStructure struc = new JniInchiStructure();
		int nAtoms = mol.getAllAtoms();
		JniInchiAtom[] atoms = new JniInchiAtom[nAtoms];
		for (int i = 0; i < nAtoms; i++) {
			int elem = mol.getAtomicNo(i);
			String sym = Molecule.cAtomLabel[elem];
			int iso = mol.getAtomMass(i);
			if (elem == 1) {
				sym = "H"; // in case this is D
			}
			JniInchiAtom a = atoms[i] = new JniInchiAtom(mol.getAtomX(i), -mol.getAtomY(i), mol.getAtomZ(i), sym);
			struc.addAtom(a);
			a.setCharge(mol.getAtomCharge(i));
			if (iso > 0)
				a.setIsotopicMass(iso);
			a.setImplicitH(mol.getImplicitHydrogens(i));
		}
		int nBonds = mol.getAllBonds();
		for (int i = 0; i < nBonds; i++) {
			int oclOrder = mol.getBondTypeSimple(i);
			INCHI_BOND_TYPE order = getInChIOrder(oclOrder);
			if (order != null) {
				int atom1 = mol.getBondAtom(0, i);
				int atom2 = mol.getBondAtom(1, i);
				int oclType = mol.getBondType(i);
				int oclParity = mol.getBondParity(i);
				INCHI_BOND_STEREO stereo = getInChIStereo(oclOrder, oclType, oclParity);
				struc.addBond(new JniInchiBond(atoms[atom1], atoms[atom2], order, stereo));
			}
		}
		return struc;
	}

	private static INCHI_BOND_TYPE getInChIOrder(int oclOrder) {
		switch (oclOrder) {
		case Molecule.cBondTypeSingle:
			return INCHI_BOND_TYPE.SINGLE;
		case Molecule.cBondTypeDouble:
			return INCHI_BOND_TYPE.DOUBLE;
		case Molecule.cBondTypeTriple:
			return INCHI_BOND_TYPE.TRIPLE;
		case Molecule.cBondTypeDelocalized:
			return INCHI_BOND_TYPE.ALTERN;
		case Molecule.cBondTypeMetalLigand:
		default:
			return null;
		}
	}

	private static INCHI_BOND_STEREO getInChIStereo(int oclOrder, int oclType, int oclParity) {
		if (oclOrder == 1) {
			switch (oclType) {
			case Molecule.cBondTypeDown:
				return INCHI_BOND_STEREO.SINGLE_1DOWN;
			case Molecule.cBondTypeUp:
				return INCHI_BOND_STEREO.SINGLE_1UP;
			default:
				if (oclParity == Molecule.cBondParityUnknown) {
					return INCHI_BOND_STEREO.SINGLE_1EITHER;
				}
			}
		}
		return INCHI_BOND_STEREO.NONE;
	}

	
	
}
