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

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.iupac.InChIStructureProvider;
import org.iupac.InchiUtils;

import com.actelion.research.chem.Molecule;
import com.actelion.research.chem.MolfileParser;
import com.actelion.research.chem.StereoMolecule;

import net.sf.jniinchi.INCHI_BOND_STEREO;
import net.sf.jniinchi.INCHI_BOND_TYPE;
import net.sf.jniinchi.JniInchiAtom;
import net.sf.jniinchi.JniInchiBond;
import net.sf.jniinchi.JniInchiInput;
import net.sf.jniinchi.JniInchiInputInchi;
import net.sf.jniinchi.JniInchiOutput;
import net.sf.jniinchi.JniInchiOutputStructure;
import net.sf.jniinchi.JniInchiStereo0D;
import net.sf.jniinchi.JniInchiStructure;
import net.sf.jniinchi.JniInchiWrapper;

/**
 * Interface with inchi.c via JNI-InChIwen( .
 * 
 * For MOL file data to InChi, we first create a Jmol model from the mol file
 * data using Jmol's adapter, and then use that to create a JNI-InChI model.
 * 
 * For InChI to SMILES, we use JNI-InChI to read InChI's input structure, via
 * JNI-InChI.
 * 
 * 
 */
public class InChIJNI1 extends InChIOCL {

	private JniInchiOutputStructure jniInchiStructure;

	/**
	 * JNI-InChI 1.03 cannot pass a MOL file to inchi.c
	 */
	@Override
	protected boolean implementsMolDataToInChI() {
		return false;
	}

	/**
	 * OCL molecule to InChI
	 * 
	 * @param mol
	 * @param options
	 * @return
	 */
	protected String getInchiImpl(StereoMolecule mol, String molFileDataOrInChI, String options) {
		try {
			String inchi = null;
			JniInchiInputInchi jniInchiInput = null;
			if (isInputInChI) {
				if (getKey) {
					// inchi => inchikey
					inchi = molFileDataOrInChI;
				} else {
					// get the inchi from JNI
					jniInchiInput = new JniInchiInputInchi(molFileDataOrInChI, options);
					if (getInchiModel) {
						// inchi ==> inchi model
						// we just need jniInchiInput
					} else {
						// inchi ==> inchi
						inchi = JniInchiWrapper.getInchiFromInchi(jniInchiInput).getInchi();
					}
				}
			} else {
				// molecule to inchi
				// or moldata to inchi
				if (mol == null) {
					mol = new StereoMolecule();
					new MolfileParser().parse(mol, molFileDataOrInChI);
				}
				JniInchiOutput out = JniInchiWrapper.getInchi(new JniInchiInput(newJniInchiStructure(mol), options));
				String msg = out.getMessage();
				if (msg != null)
					System.err.println(msg);
				inchi = out.getInchi();
				if (getInchiModel) {
					jniInchiInput = new JniInchiInputInchi(inchi, options);
				}
			}
			if (getInchiModel) {
				// only not null now if we need a model.
				// get JNI-InChI structure and generate JSON model
				return toJSON(JniInchiWrapper.getStructureFromInchi(jniInchiInput));
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
		mol.ensureHelperArrays(Molecule.cHelperNeighbours);
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
				// System.out.println("Inchi-out bond " + i + " " + order + " " + oclType + " "
				// + oclParity + " " + stereo);
				JniInchiBond bond = new JniInchiBond(atoms[atom1], atoms[atom2], order, stereo);
				struc.addBond(bond);
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
					return (oclOrder == Molecule.cBondTypeDouble ? INCHI_BOND_STEREO.DOUBLE_EITHER
							: INCHI_BOND_STEREO.SINGLE_1EITHER);
				}
			}
		}
		return INCHI_BOND_STEREO.NONE;
	}

	private Map<JniInchiAtom, Integer> mapAtomToIndex = new Hashtable<JniInchiAtom, Integer>();
	private JniInchiAtom thisAtom;
	private JniInchiBond thisBond;
	private JniInchiStereo0D thisStereo;

	@Override
	public void initializeInchiModel(String inchi) throws Exception {
		jniInchiStructure = JniInchiWrapper.getStructureFromInchi(new JniInchiInputInchi(inchi));
		for (int i = getNumAtoms(); --i >= 0;)
			mapAtomToIndex.put(jniInchiStructure.getAtom(i), Integer.valueOf(i));
	}

	/// atoms ///

	@Override
	public int getNumAtoms() {
		return jniInchiStructure.getNumAtoms();
	}

	@Override
	public InChIStructureProvider setAtom(int i) {
		thisAtom = jniInchiStructure.getAtom(i);
		return this;
	}

	@Override
	public String getElementType() {
		return thisAtom.getElementType();
	}

	@Override
	public double getX() {
		return thisAtom.getX();
	}

	@Override
	public double getY() {
		return thisAtom.getY();
	}

	@Override
	public double getZ() {
		return thisAtom.getZ();
	}

	@Override
	public int getCharge() {
		return thisAtom.getCharge();
	}

	@Override
	public int getIsotopicMass() {
		return InchiUtils.getActualMass(getElementType(), thisAtom.getIsotopicMass());
	}

	@Override
	public int getImplicitH() {
		return thisAtom.getImplicitH();
	}

	/// bonds ///

	@Override
	public int getNumBonds() {
		return jniInchiStructure.getNumBonds();
	}

	@Override
	public InChIStructureProvider setBond(int i) {
		thisBond = jniInchiStructure.getBond(i);
		return this;
	}

	@Override
	public int getIndexOriginAtom() {
		return mapAtomToIndex.get(thisBond.getOriginAtom()).intValue();
	}

	@Override
	public int getIndexTargetAtom() {
		return mapAtomToIndex.get(thisBond.getTargetAtom()).intValue();
	}

	@Override
	public String getInchiBondType() {
		return thisBond.getBondType().name();
	}

	/// Stereo ///

	@Override
	public int getNumStereo0D() {
		return jniInchiStructure.getNumStereo0D();
	}

	@Override
	public InChIStructureProvider setStereo0D(int i) {
		thisStereo = jniInchiStructure.getStereo0D(i);
		return this;
	}

	@Override
	public int[] getNeighbors() {
		JniInchiAtom[] an = thisStereo.getNeighbors();

		int n = an.length;
		int[] a = new int[n];

		// add for loop
		for (int i = 0; i < n; i++) {
			a[i] = mapAtomToIndex.get(an[i]).intValue();
		}
		return a;
	}

	@Override
	public int getCenterAtom() {
		JniInchiAtom ca = thisStereo.getCentralAtom();
		return (ca == null ? -1 : mapAtomToIndex.get(ca).intValue());
	}

	@Override
	public String getStereoType() {
		return thisStereo.getStereoType().toString();
	}

	@Override
	public String getParity() {
		return thisStereo.getParity().toString();
	}

	//// molecule from Structure

	private static String toJSON(JniInchiOutputStructure inchiModel) {
		int na = inchiModel.getNumAtoms();
		int nb = inchiModel.getNumBonds();
		int ns = inchiModel.getNumStereo0D();
		Map<JniInchiAtom, Integer> mapAtoms = new HashMap<>();
		boolean haveXYZ = false;
		for (int i = 0; i < na; i++) {
			JniInchiAtom a = inchiModel.getAtom(i);
			if (a.getX() != 0 || a.getY() != 0 || a.getZ() != 0) {
				haveXYZ = true;
				break;
			}
		}
		String s = "{";
		s += "\n\"atomCount\":" + na + ",\n";
		s += "\"atoms\":[\n";
		for (int i = 0; i < na; i++) {
			JniInchiAtom a = inchiModel.getAtom(i);
			mapAtoms.put(a, Integer.valueOf(i));
			if (i > 0)
				s += ",\n";
			s += "{";
			s += toJSONInt("index", Integer.valueOf(i), "");
			s += toJSONNotNone("elname", a.getElementType(), ",");
			if (haveXYZ) {
				s += toJSONDouble("x", a.getX(), ",");
				s += toJSONDouble("y", a.getY(), ",");
				s += toJSONDouble("z", a.getZ(), ",");
			}
			s += toJSONNotNone("radical", a.getRadical(), ",");
			s += toJSONNonZero("charge", a.getCharge(), ",");
			s += toJSONNonZero("isotopeMass", a.getIsotopicMass(), ",");
			if (a.getImplicitH() > 0)
				s += toJSONNonZero("implicitH", a.getImplicitH(), ",");
			s += toJSONNonZero("implicitDeuterium", a.getImplicitDeuterium(), ",");
			s += toJSONNonZero("implicitProtium", a.getImplicitProtium(), ",");
			s += toJSONNonZero("implicitTritium", a.getImplicitTritium(), ",");
			s += "}";
		}
		s += "\n],";
		s += "\n\"bondCount\":" + nb + ",";
		s += "\n\"bonds\":[\n";

		for (int i = 0; i < nb; i++) {
			if (i > 0)
				s += ",\n";
			s += "{";
			JniInchiBond b = inchiModel.getBond(i);
			s += toJSONInt("originAtom", mapAtoms.get(b.getOriginAtom()).intValue(), "");
			s += toJSONInt("targetAtom", mapAtoms.get(b.getTargetAtom()).intValue(), ",");
			String bt = b.getBondType().toString();
			if (!bt.equals("SINGLE"))
				s += toJSONString("type", bt, ",");
			s += toJSONNotNone("stereo", b.getBondStereo(), ",");
			s += "}";
		}
		s += "\n]";
		if (ns > 0) {
			s += ",\n\"stereoCount\":" + ns + ",\n";
			s += "\"stereo\":[\n";
			for (int i = 0; i < ns; i++) {
				if (i > 0)
					s += ",\n";
				s += "{";
				JniInchiStereo0D d = inchiModel.getStereo0D(i);
				JniInchiAtom a = d.getCentralAtom();
				s += toJSONNotNone("parity", d.getParity(), "");
				s += toJSONNotNone("type", d.getStereoType(), ",");
				if (a != null)
					s += toJSONInt("centralAtom", mapAtoms.get(a).intValue(), ",");
				// s += toJSON("debugString",d.getDebugString(), ",");
				// never implemented? s +=
				// toJSON("disconnectedParity",d.getDisconnectedParity(), ",");
				JniInchiAtom[] an = d.getNeighbors();
				int[] nbs = new int[an.length];
				for (int j = 0; j < an.length; j++) {
					nbs[j] = mapAtoms.get(d.getNeighbor(j)).intValue();
				}
				s += toJSONArray("neighbors", nbs, ",");
				s += "}";
			}
			s += "\n]";
		}
		s += "}";
		return s;
	}

	private static String toJSONArray(String key, int[] val, String term) {
		String s = term + "\"" + key + "\":[" + val[0];
		for (int i = 1; i < val.length; i++) {
			s += "," + val[i];
		}
		return s + "]";
	}

	private static String toJSONNonZero(String key, int val, String term) {
		return (val == 0 ? "" : toJSONInt(key, val, term));
	}

	private static String toJSONInt(String key, int val, String term) {
		return term + "\"" + key + "\":" + val;
	}

	private static String toJSONDouble(String key, double val, String term) {
		String s;
		if (val == 0) {
			s = "0";
		} else {
			s = "" + (val + (val > 0 ? 0.00000001 : -0.00000001));
			s = s.substring(0, s.indexOf(".") + 5);
			int n = s.length();
			while (s.charAt(--n) == '0') {
			}
			s = s.substring(0, n + 1);
		}
		return term + "\"" + key + "\":" + s;
	}

	private static String toJSONString(String key, String val, String term) {
		return term + "\"" + key + "\":\"" + val + "\"";
	}

	private static String toJSONNotNone(String key, Object val, String term) {
		String s = val.toString();
		return ("NONE".equals(s) ? "" : term + "\"" + key + "\":\"" + s + "\"");
	}

}