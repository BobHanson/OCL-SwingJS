/*
 * Copyright 2006-2011 Sam Adams <sea36 at users.sourceforge.net>
 *
 * This file is part of -InChI.
 *
 * -InChI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * -InChI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with -InChI.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.actelion.research.chem.inchi;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.StringTokenizer;

import org.iupac.InChIStructureProvider;
import org.iupac.InchiUtils;

import com.actelion.research.chem.Molecule;
import com.actelion.research.chem.MolfileParser;
import com.actelion.research.chem.StereoMolecule;
import com.sun.jna.Native;

import io.github.dan2097.jnainchi.InchiBondType;
import io.github.dan2097.jnainchi.InchiFlag;
import io.github.dan2097.jnainchi.InchiBondStereo;
import io.github.dan2097.jnainchi.InchiAtom;
import io.github.dan2097.jnainchi.InchiBond;
import io.github.dan2097.jnainchi.InchiInput;
import io.github.dan2097.jnainchi.InchiOptions;
import io.github.dan2097.jnainchi.InchiOptions.InchiOptionsBuilder;
import io.github.dan2097.jnainchi.inchi.InchiLibrary;
import io.github.dan2097.jnainchi.InchiOutput;
import io.github.dan2097.jnainchi.InchiStereo;
import io.github.dan2097.jnainchi.JnaInchi;

/**
 * Interface with inchi.c via JNA (David Lowe)
 * 
 * For InChI to SMILES, we use JNA-InChI to read InChI's input structure, via
 * -InChI.
 * 
 * 
 */
public class InChIJNA extends InChIOCL {

	@Override
	protected void initAndRun(Runnable r) {
		r.run();
	}

	private InchiInput inchiModel;

	/**
	 * OCL molecule to InChI
	 * 
	 * @param mol
	 * @param options
	 * @return a string 
	 */
	protected String getInchiImpl(StereoMolecule mol, String molFileDataOrInChI, String options) {
	    if ("version".equals(options))
	    	  return getInternalInchiVersion(); 
		try {
			String inchi = null;
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
					out = JnaInchi.inchiToInchi(molFileDataOrInChI, ops);
				}
			} else if (mol == null){
				   // molfile data => inchi
		          out = JnaInchi.molToInchi((String) molFileDataOrInChI, ops);			
			} else {
				// molecule to inchi
		        out = JnaInchi.toInchi(newInchiStructure(mol), getOptions(options));
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
				return toJSON(JnaInchi.getInchiInputFromInchi(inchi).getInchiInput());
			}
			return (getKey ? JnaInchi.inchiToInchiKey(inchi).getInchiKey() : inchi);
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

	private static InchiInput newInchiStructure(StereoMolecule mol) {
		mol.ensureHelperArrays(Molecule.cHelperNeighbours);
		InchiInput struc = new InchiInput();
		int nAtoms = mol.getAllAtoms();
		InchiAtom[] atoms = new InchiAtom[nAtoms];
		for (int i = 0; i < nAtoms; i++) {
			int elem = mol.getAtomicNo(i);
			String sym = Molecule.cAtomLabel[elem];
			int iso = mol.getAtomMass(i);
			if (elem == 1) {
				sym = "H"; // in case this is D
			}
			InchiAtom a = atoms[i] = new InchiAtom(sym, mol.getAtomX(i), -mol.getAtomY(i), mol.getAtomZ(i));
			struc.addAtom(a);
			a.setCharge(mol.getAtomCharge(i));
			if (iso > 0)
				a.setIsotopicMass(iso);
			a.setImplicitHydrogen(mol.getImplicitHydrogens(i));
		}
		int nBonds = mol.getAllBonds();
		for (int i = 0; i < nBonds; i++) {
			int oclOrder = mol.getBondTypeSimple(i);
			InchiBondType order = getInChIOrder(oclOrder);
			if (order != null) {
				int atom1 = mol.getBondAtom(0, i);
				int atom2 = mol.getBondAtom(1, i);
				int oclType = mol.getBondType(i);
				int oclParity = mol.getBondParity(i);
				InchiBondStereo stereo = getInChIStereo(oclOrder, oclType, oclParity);
				// System.out.println("Inchi-out bond " + i + " " + order + " " + oclType + " "
				// + oclParity + " " + stereo);
				InchiBond bond = new InchiBond(atoms[atom1], atoms[atom2], order, stereo);
				struc.addBond(bond);
			}
		}
		return struc;
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

	@Override
	public void initializeInchiModel(String inchi) throws Exception {
		inchiModel = JnaInchi.getInchiInputFromInchi(inchi).getInchiInput();
		for (int i = getNumAtoms(); --i >= 0;)
			map.put(inchiModel.getAtom(i), Integer.valueOf(i));
	}

	private Map<InchiAtom, Integer> map = new Hashtable<InchiAtom, Integer>();

	private InchiAtom thisAtom;
	private InchiBond thisBond;
	private InchiStereo thisStereo;

	/// atoms ///

	@Override
	public int getNumAtoms() {
		return inchiModel.getAtoms().size();
	}

	@Override
	public InChIStructureProvider setAtom(int i) {
		thisAtom = inchiModel.getAtom(i);
		return this;
	}

	@Override
	public String getElementType() {
		return thisAtom.getElName();
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
		return thisAtom.getImplicitHydrogen();
	}

	/// bonds ///

	@Override
	public int getNumBonds() {
		return inchiModel.getBonds().size();
	}

	@Override
	public InChIStructureProvider setBond(int i) {
		thisBond = inchiModel.getBond(i);
		return this;
	}

	@Override
	public int getIndexOriginAtom() {
		return map.get(thisBond.getStart()).intValue();
	}

	@Override
	public int getIndexTargetAtom() {
		return map.get(thisBond.getEnd()).intValue();
	}

	@Override
	public String getInchiBondType() {
		InchiBondType type = thisBond.getType();
		return type.name();
	}

	/// Stereo ///

	@Override
	public int getNumStereo0D() {
		return inchiModel.getStereos().size();
	}

	@Override
	public InChIStructureProvider setStereo0D(int i) {
		thisStereo = inchiModel.getStereos().get(i);
		return this;
	}

	@Override
	public int[] getNeighbors() {
		InchiAtom[] an = thisStereo.getAtoms();

		int n = an.length;
		int[] a = new int[n];

		// add for loop
		for (int i = 0; i < n; i++) {
			a[i] = map.get(an[i]).intValue();
		}
		return a;
	}

	@Override
	public int getCenterAtom() {
		InchiAtom ca = thisStereo.getCentralAtom();
		return (ca == null ? -1 : map.get(ca).intValue());
	}

	@Override
	public String getStereoType() {
		return uc(thisStereo.getType());
	}

	@Override
	public String getParity() {
		return uc(thisStereo.getParity());
	}

	private static String uc(Object o) {
		return o.toString().toUpperCase();
	}

	private static String toJSON(InchiInput inchiModel) {
		int na = inchiModel.getAtoms().size();
		int nb = inchiModel.getBonds().size();
		int ns = inchiModel.getStereos().size();
		Map<InchiAtom, Integer> mapAtoms = new HashMap<>();
		boolean haveXYZ = false;
		for (int i = 0; i < na; i++) {
			InchiAtom a = inchiModel.getAtom(i);
			if (a.getX() != 0 || a.getY() != 0 || a.getZ() != 0) {
				haveXYZ = true;
				break;
			}
		}
		String s = "{";
		s += "\n\"atomCount\":" + na + ",\n";
		s += "\"atoms\":[\n";
		for (int i = 0; i < na; i++) {
			InchiAtom a = inchiModel.getAtom(i);
			mapAtoms.put(a, Integer.valueOf(i));
			if (i > 0)
				s += ",\n";
			s += "{";
			s += toJSONInt("index", Integer.valueOf(i), "");
			s += toJSONNotNone("elname", a.getElName(), ",");
			if (haveXYZ) {
				s += toJSONDouble("x", a.getX(), ",");
				s += toJSONDouble("y", a.getY(), ",");
				s += toJSONDouble("z", a.getZ(), ",");
			}
			s += toJSONNotNone("radical", a.getRadical(), ",");
			s += toJSONNonZero("charge", a.getCharge(), ",");
			s += toJSONNonZero("isotopeMass", a.getIsotopicMass(), ",");
			if (a.getImplicitHydrogen() > 0)
				s += toJSONNonZero("implicitH", a.getImplicitHydrogen(), ",");
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
			InchiBond b = inchiModel.getBond(i);
			s += toJSONInt("originAtom", mapAtoms.get(b.getStart()).intValue(), "");
			s += toJSONInt("targetAtom", mapAtoms.get(b.getEnd()).intValue(), ",");
			String bt = uc(b.getType());
			if (!bt.equals("SINGLE"))
				s += toJSONString("type", bt, ",");
			s += toJSONNotNone("stereo", uc(b.getStereo()), ",");
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
				InchiStereo d = inchiModel.getStereos().get(i);
				InchiAtom a = d.getCentralAtom();
				s += toJSONNotNone("parity", d.getParity(), "");
				s += toJSONNotNone("type", d.getType(), ",");
				if (a != null)
					s += toJSONInt("centralAtom", mapAtoms.get(a).intValue(), ",");
				// s += toJSON("debugString",d.getDebugString(), ",");
				// never implemented? s +=
				// toJSON("disconnectedParity",d.getDisconnectedParity(), ",");
				InchiAtom[] an = d.getAtoms();
				int[] nbs = new int[an.length];
				for (int j = 0; j < an.length; j++) {
					nbs[j] = mapAtoms.get(an[j]).intValue();
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

	@Override
	protected boolean implementsMolDataOnlyToInChI() {
		return false;
	}

	  private static String inchiVersionInternal;

	  /**
	   * Get the InChI version directly from the inchi code without an API.
	   * To be replaced in the future with a simple inchi IXA call?
	   * 
	   * Future format may change.
	   * 
	   * @param f
	   * @return something like "InChI version 1, Software 1.07.2 (API Library)"
	   */
	  public static String getInternalInchiVersion() {
	    if (inchiVersionInternal == null) {
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
	      //System.out.println(f);
	      //e.printStackTrace();
	      // it's gone already in Linux
	    }
	    return s;
	  }


}