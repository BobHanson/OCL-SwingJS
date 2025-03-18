package org.iupac;

public interface InChIStructureProvider {
  
  abstract void initializeInchiModel(String inchi) throws Exception;

  //InChIStructureProvider Setters
  abstract void setAtom(int i);
  abstract void setBond(int i);
  abstract void setStereo0D(int i);
  
  // general counts
  abstract int getNumAtoms();
  abstract int getNumBonds();
  abstract int getNumStereo0D();
  
  //Atom Methods
  abstract String getElementType();
  abstract double getX();
  abstract double getY();
  abstract double getZ();
  abstract int getCharge();
  abstract int getImplicitH();
  
  /**
   * from inchi_api.h
   * 
   * #define ISOTOPIC_SHIFT_FLAG 10000
   * 
   * add to isotopic mass if isotopic_mass = (isotopic mass - average atomic
   * mass)
   * 
   * AT_NUM isotopic_mass;
   * 
   * 0 => non-isotopic; isotopic mass or ISOTOPIC_SHIFT_FLAG + mass - (average
   * atomic mass)
   * 
   * 
   * @return inchi's value of of the average mass
   */
  abstract int getIsotopicMass();
 
  //Bond Methods
  abstract int getIndexOriginAtom();
  abstract int getIndexTargetAtom();
  abstract String getInchiBondType();
  
  //Stereo Methods
  abstract String getParity();
  abstract String getStereoType();
  abstract int getCenterAtom();
  abstract int[] getNeighbors();

}
