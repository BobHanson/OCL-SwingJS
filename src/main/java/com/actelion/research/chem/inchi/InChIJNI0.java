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

import com.actelion.research.chem.MolfileParser;
import com.actelion.research.chem.StereoMolecule;

public class InChIJNI0 extends InChIJNI {

	// all methods are static, but we still need a public constructor
	// for dynamic class loading in JavaScript. (Or do we? Maybe not in SwingJS
	
	InChIJNI0() {
		// package-only instantiation
	}

	@Override
	/**
	 * OCL molecule to InChI, the original way, without using the MOL file data
	 * 
	 * @param mol
	 * @param options
	 * @param getKey
	 * @return
	 */
	protected String getInChIimpl(StereoMolecule mol, String molFileData, String options, boolean getKey) {
		if (molFileData != null) {
					mol = new StereoMolecule();
					new MolfileParser().parse(mol, molFileData);
		}
		return super.getInChIimpl(mol,null, options, getKey);
	}
	
}
