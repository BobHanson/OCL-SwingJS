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
import net.sf.jniinchi.JniInchiInputInchi;
import net.sf.jniinchi.JniInchiOutputStructure;
import net.sf.jniinchi.JniInchiStereo0D;
import net.sf.jniinchi.JniInchiStructure;
import net.sf.jniinchi.JniInchiWrapper;

public class InChIJNI {

	public InChIJNI() {
		// for dynamic loading
	}

	public static boolean inchiToMolecule(String inchi, StereoMolecule mol) {
		try {
			JniInchiOutputStructure struc = JniInchiWrapper.getStructureFromInchi(new JniInchiInputInchi(inchi));
			getMolecule(struc, mol);
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


	@SuppressWarnings("boxing")
	private static String toJSON(JniInchiStructure mol) {
		int na = mol.getNumAtoms();
		int nb = mol.getNumBonds();
		int ns = mol.getNumStereo0D();
		Map<JniInchiAtom, Integer> mapAtoms = new HashMap<>();
		String s = "{\"atoms\":[\n";
		String sep = "";
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

	private static void getMolecule(JniInchiOutputStructure struc, StereoMolecule molOut) {
		boolean[] enabled = new boolean[] {false};
		StereoMolecule mol = new StereoMolecule() {
			public int getAtomParity(int atom) {
				if (enabled[0]) {
					
					switch (atom) {
					case 10:
					case 15:
					case 16:
						return Molecule.cAtomParity2;
					case 9:
					case 12:
						return Molecule.cAtomParity1;
					}
					
					return molOut.getAtomParity(atom);
				}
				return super.getAtomParity(atom);
			}
		};
		int nAtoms = struc.getNumAtoms();
		int nBonds = struc.getNumBonds();
		int nStereo = struc.getNumStereo0D();
		int nh = 0;
		for (int i = 0; i < nAtoms; i++) {
			JniInchiAtom a = struc.getAtom(i);
			nh += a.getImplicitH();
		}
		List<JniInchiAtom> atoms = new ArrayList<>();
		Map<JniInchiAtom, Integer> map = new HashMap<>();
		for (int i = 0; i < nAtoms; i++) {
			JniInchiAtom a = struc.getAtom(i);
			atoms.add(a);
			String sym = a.getElementType();
			int atom = mol.addAtom(Molecule.getAtomicNoFromLabel(sym));
			mol.setAtomCharge(atom, a.getCharge());
			mol.setAtomX(atom, -1);
			map.put(a, Integer.valueOf(i));
		}
		for (int i = 0; i < nStereo; i++) {
			JniInchiStereo0D d = struc.getStereo0D(i);
			int ia = Integer.valueOf(map.get(d.getCentralAtom()));
			int p = decodeParity(d.getParity());
			mol.setAtomParity(ia, p, false);
			JniInchiAtom[] an = d.getNeighbors();
			int[] nbs = new int[an.length];
			for (int j = 0; j < an.length; j++) {
				nbs[j] = map.get(d.getNeighbor(j)).intValue();
			}
		}
		for (int i = 0; i < nBonds; i++) {
			JniInchiBond b = struc.getBond(i);
			JniInchiAtom a1 = b.getOriginAtom();
			JniInchiAtom a2 = b.getTargetAtom();
			int bt = getBondType(b);
			int i1 = map.get(a1);
			int i2 = map.get(a2);
			mol.addBond(i1, i2, bt);
		}
		// temporarily preserve parities
		mol.copyMolecule(molOut);
		enabled[0] = true;
		mol.setParitiesValid(0);
		new CoordinateInventor(Canonizer.COORDS_ARE_3D | CoordinateInventor.MODE_SKIP_DEFAULT_TEMPLATES).invent(mol);
		enabled[0] = false;
		mol.copyMolecule(molOut);
		//new CoordinateInventor(0).invent(molOut);
	}


	private static int decodeParity(INCHI_PARITY parity) {

		switch (parity) {
		case EVEN:
			return Molecule.cAtomParity2;
		case ODD:
			return Molecule.cAtomParity1;
		case UNKNOWN:
			return Molecule.cAtomParityUnknown;
		case NONE:
		default:
			return Molecule.cAtomParityNone;
		}
	}

	private static int getBondType(JniInchiBond b) {
		INCHI_BOND_TYPE type = b.getBondType();
		INCHI_BOND_STEREO stereo = b.getBondStereo();

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
			if (true)
				break;
			switch (stereo) {
			case NONE:
				break;
			case SINGLE_1UP:
				break;
			case SINGLE_1EITHER:
				break;
			case SINGLE_1DOWN:
				break;
			case SINGLE_2UP:
				break;
			case SINGLE_2EITHER:
				break;
			case SINGLE_2DOWN:
				break;
			case DOUBLE_EITHER:
				break;
			}
		default:
			break;
 		}
		return Molecule.cBondTypeSingle;
	}

}
