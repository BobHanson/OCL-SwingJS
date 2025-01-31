package com.actelion.research.chem.moreparsers;

public class ChemicalNameResolver extends ChemicalIdentifierResolver {

	public ChemicalNameResolver() {
		this(false);
	}

	public ChemicalNameResolver(boolean keepHydrogenMap) {
		super(TYPE_NAME, keepHydrogenMap);
		setSource(SOURCE_CIR);
	}	

}
