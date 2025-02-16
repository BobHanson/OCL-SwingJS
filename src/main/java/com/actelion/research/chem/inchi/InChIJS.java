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

import java.util.List;
import java.util.Map;

import org.iupac.InChIStructureProvider;
import org.iupac.InchiUtils;

import com.actelion.research.chem.MolfileCreator;
import com.actelion.research.chem.StereoMolecule;

/**
 * This class implements inchi-web.wasm from InChI-SwingJS, which is adapted
 * from https://github.com/IUPAC-InChI/InChI-Web-Demo by Bob Hanson and Josh
 * Charlton 2025.01.23-2025.01.24.
 * 
 * In the case of a Jmol model for mdel to InChI, we first generate MOL file
 * data.
 * 
 * For InChI to SMILES, we use the inchi-web.c method model_from_inchi() that we
 * developed with the assistance of Frank Lange.
 * 
 * The class originally adapted Richard Apodaca's 2020 molfile-to-inchi
 * LLVM-derived Web Assembly implementation of IUPAC InChI v. 1.05. see
 * https://depth-first.com/articles/2020/03/02/compiling-inchi-to-webassembly-part-2-from-molfile-to-inchi/
 * 
 * Note that this initialiation is asynchronous. One has to either use
 * 
 * sync inchi
 * 
 * or invoke a call to generate an InChI, such as:
 * 
 * x = {none}.find("inchi")
 * 
 */
public class InChIJS extends InChIOCL implements InChIStructureProvider {

	static {
		try {
			/**
			 * Import inchi-web-SwingJS.js
			 * 
			 * @j2sNative 
			 * 
			 * var j2sPath = J2S._applets.master._j2sFullPath; 
			 * J2S.inchiPath = J2S._applets.master._j2sFullPath + "/_ES6"; 
			 * $.getScript(J2S.inchiPath +   "/inchi-web-SwingJS.js");
			 */
			{
			}
		} catch (Throwable t) {
			//
		}

	}

	public InChIJS() {
		// for dynamic loading
	}

	@Override
	protected void initAndRun(Runnable r) {
		
		
		/**
		 * @j2sNative
		 *    
		 *    if (!J2S) {
		 *      alert("J2S has not been installed");
		 *      System.exit(0);
		 *    }
		 *   var t = [];
		 *   t[0] = setInterval(
		 *      function(){
		 *       if (J2S.inchiWasmLoaded && J2S.inchiWasmLoaded()) {
		 *        clearInterval(t[0]);
		 *        r.run$();
		 *       }
		 *      }, 50);
		 */
	}

	@Override
	protected boolean implementsMolDataOnlyToInChI() {
		return true;
	}

	@Override
	protected String getInchiImpl(StereoMolecule mol, String molFileDataOrInChI, String options) {
      if (options.equals("version")) {
          /**
           * @j2sNative return (J2S.modelFromInchi ?
           *            J2S.modelFromInchi('').ver : ""); 
           */
          {
          }
        }
		options = options.replace('-', ' ').replaceAll("\\s+", " ").trim().replaceAll(" ", " -").toLowerCase();
		if (options.length() > 0)
			options = "-" + options;
		if (mol != null)
			molFileDataOrInChI = new MolfileCreator(mol).getMolfile();
		boolean isLoaded = (execute("inchiFromInchi", null, null, null) != null);
		if (!isLoaded)
			return "";
		String inchi = (isInputInChI ? molFileDataOrInChI
				: execute("inchiFromMolfile", molFileDataOrInChI, options, "inchi"));
		return (getInchiModel ? execute("modelFromInchi", inchi, options, "model")  //
				: getKey ? execute("inchikeyFromInchi", inchi, options, "inchikey") //
				: isInputInChI ? execute("inchiFromInchi", inchi, options, "inchi") //
				: inchi);
	}

	/**
	 * If key is null, just return the method itself. (Used to confirm that the
	 * module has loaded.)
	 * 
	 * Otherwise, execute the InChI-web-SwingJS method with the given data and
	 * options and return the STRING value JSON structure delivered by that key.
	 * 
	 * @param method
	 * @param options
	 * @param key
	 * @return the JSON value requested or the method requested
	 */
	private String execute(String method, String data, String options, String key) {
		/**
		 * @j2sNative return(key == null ? J2S[method] : J2S[method](data, options)[key]
		 *            || null);
		 */
		{
			return null;
		}
	}

//all javascript maps and arrays, only accessible through j2sNative.
	List<Map<String, Object>> atoms, bonds, stereo;
	private Map<String, Object> thisAtom;
	private Map<String, Object> thisBond;
	private Map<String, Object> thisStereo;

	@Override
	public void initializeInchiModel(String inchi) {
		/**
		 * @j2sNative var json = JSON.parse(J2S.modelFromInchi(inchi).model); this.atoms
		 *            = json.atoms; this.bonds = json.bonds; this.stereo = (json.stereo
		 *            || []);
		 */
	}

	/// Atoms ///

	@Override
	public int getNumAtoms() {
		/**
		 * @j2sNative return this.atoms.length;
		 */
		{
			return 0;
		}
	}

	@Override
	public InChIStructureProvider setAtom(int i) {
		/**
		 * @j2sNative this.thisAtom = this.atoms[i];
		 */
		{
		}
		return this;
	}

	@Override
	public String getElementType() {
		return getString(thisAtom, "elname", "");
	}

	@Override
	public double getX() {
		return getDouble(thisAtom, "x", 0);
	}

	@Override
	public double getY() {
		return getDouble(thisAtom, "y", 0);
	}

	@Override
	public double getZ() {
		return getDouble(thisAtom, "z", 0);
	}

	@Override
	public int getCharge() {
		return getInt(thisAtom, "charge", 0);
	}

	@Override
	public int getImplicitH() {
		return getInt(thisAtom, "implicitH", 0);
	}

	@Override
	public int getIsotopicMass() {
		String sym = getElementType();
		int mass = 0;
		/**
		 * @j2sNative mass = this.thisAtom["isotopicMass"] || 0;
		 */
		{
		}
		return InchiUtils.getActualMass(sym, mass);
	}

	/// Bonds ///

	@Override
	public int getNumBonds() {
		/**
		 * @j2sNative return this.bonds.length;
		 */
		{
			return 0;
		}
	}

	@Override
	public InChIStructureProvider setBond(int i) {
		/**
		 * @j2sNative this.thisBond = this.bonds[i];
		 */
		{
		}
		return this;
	}

	@Override
	public int getIndexOriginAtom() {
		return getInt(thisBond, "originAtom", 0);
	}

	@Override
	public int getIndexTargetAtom() {
		return getInt(thisBond, "targetAtom", 0);
	}

	@Override
	public String getInchiBondType() {
		return getString(thisBond, "type", "SINGLE");
	}

	/// Stereo ///

	@Override
	public int getNumStereo0D() {
		/**
		 * @j2sNative return this.stereo.length;
		 */
		{
			return 0;
		}
	}

	@Override
	public InChIStructureProvider setStereo0D(int i) {
		/**
		 * @j2sNative this.thisStereo = this.stereo[i];
		 */
		{
		}
		return this;
	}

	@Override
	public String getParity() {
		return getString(thisStereo, "parity", "");
	}

	@Override
	public String getStereoType() {
		return getString(thisStereo, "type", "");
	}

	@Override
	public int getCenterAtom() {
		return getInt(thisStereo, "centralAtom", -1);
	}

	@Override
	public int[] getNeighbors() {
		/**
		 * @j2sNative return this.thisStereo.neighbors;
		 */
		{
			return null;
		}
	}

	private int getInt(Map<String, Object> map, String name, int defaultValue) {
		/**
		 * @j2sNative var val = map[name]; if (val || val == 0) return val;
		 */
		{
		}
		return defaultValue;
	}

	private double getDouble(Map<String, Object> map, String name, double defaultValue) {
		/**
		 * @j2sNative var val = map[name]; if (val || val == 0) return val;
		 */
		{
		}
		return defaultValue;
	}

	private String getString(Map<String, Object> map, String name, String defaultValue) {
		/**
		 * @j2sNative var val = map[name]; if (val || val == "") return val;
		 */
		{
		}
		return defaultValue;
	}

}
