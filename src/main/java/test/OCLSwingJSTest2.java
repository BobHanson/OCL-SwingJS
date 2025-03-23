package test;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Locale;

import javax.imageio.ImageIO;

import com.actelion.research.chem.StereoMolecule;

import swingjs.OCL;

public class OCLSwingJSTest2 {

	private static boolean isJS = /** @j2sNative true || */
			false;

	public static void main(String[] args) {
		Locale.setDefault(Locale.ROOT);
		// load JavaScript:
		try {	
			OCL.init(()->{	
				System.out.println(OCL.getInChIVersion(false));
				runTests();
			});
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	protected static void runTests() {
		long t = System.currentTimeMillis();
		
		String outdir = null;//"C:/temp/";

		test0(outdir);
		
		testInChI1(outdir);
		testSmilesParser(outdir);
		testInChIParsers(outdir);
		testAllene(outdir);
		testEne(outdir);
		t = (System.currentTimeMillis() - t);
		System.out.println("OCL " + (isJS ? "JS" : "Java") + " called:" + OCL.getCallCount() + " checked:" + nChecked + " " + t + " ms");
	}

	private static void test0(String outdir) {
		
		String inchi = "InChI=1S/C41H44O22/c42-13-27-31(50)33(52)36(55)40(61-27)59-25-11-19(44)10-24-20(25)12-26(37(58-24)17-4-7-21(45)22(46)9-17)60-41-38(63-39-35(54)30(49)23(47)14-57-39)34(53)32(51)28(62-41)15-56-29(48)8-3-16-1-5-18(43)6-2-16/h1-12,23,27-28,30-36,38-42,47,49-55H,13-15H2,(H3-,43,44,45,46,48)/p+1/t23-,27-,28-,30+,31-,32-,33+,34+,35-,36-,38-,39+,40-,41-/m1/s1";
		String filein = "LMPK12010169.mol";
		StereoMolecule mol;
		mol = OCL.getOCLMoleculeFromMOL(getString(filein));
		testInChIOut(mol, inchi, true, 3);
		
		filein = "LMPK12010169b.mol";
		mol = OCL.getOCLMoleculeFromMOL(getString(filein));
		testInChIOut(mol, inchi, true, 4);

		filein = "LMPK12010169.mol";
		mol = OCL.getOCLMoleculeFromMOL(getString(filein));
		testInChIOut(mol, inchi, true, 3);

		filein = "LMPK12010169b.mol";
		testInChIOut(getString(filein), inchi, true, 3);
		
		String smiles = OCL.getSmilesFromInChI(inchi);
		System.out.println(smiles);


		filein = "LMPK12010169N.mol";
		inchi = "InChI=1S/C41H45NO21/c43-13-27-32(51)34(53)37(56)40(61-27)59-25-11-19(45)10-21-20(25)12-26(30(42-21)17-4-7-22(46)23(47)9-17)60-41-38(63-39-36(55)31(50)24(48)14-58-39)35(54)33(52)28(62-41)15-57-29(49)8-3-16-1-5-18(44)6-2-16/h1-12,24,27-28,31-41,43-48,50-56H,13-15H2"
				+ "/b8-3+/t24-,27-,28-,31+,32-,33-,34+,35+,36-,37-,38-,39+,40-,41-/m1/s1";
		testInChIOut(getString(filein), inchi, true, 5);
// this one should fail, because InChI algoritm removes stereochemistry for the cationic species.
//		filein = "LMPK12010169O.mol";
//		inchi = "InChI=1S/C41H45NO21/c43-13-27-32(51)34(53)37(56)40(61-27)59-25-11-19(45)10-21-20(25)12-26(30(42-21)17-4-7-22(46)23(47)9-17)60-41-38(63-39-36(55)31(50)24(48)14-58-39)35(54)33(52)28(62-41)15-57-29(49)8-3-16-1-5-18(44)6-2-16/h1-12,24,27-28,31-41,43-48,50-56H,13-15H2"
//				+ "/b8-3+/t24-,27-,28-,31+,32-,33-,34+,35+,36-,37-,38-,39+,40-,41-/m1/s1";
//		moldata = getString(filein);
//		testInChIOut(moldata, inchi, true, 3);

	}

	private static void testInChI1(String outdir) { 
		StereoMolecule mol;
		String inchi = "InChI=1S/C3H3NO/c5-3-1-2-4-3/h1-2H,(H,4,5)";
		
		// SMILES from inchi
		String smiles = OCL.getSmilesFromInChI(inchi);
		// inchi from SMILES
		System.out.println(smiles);
		// OC1=NC=C1

		String inchiOut = OCL.getInChIFromSmiles(smiles, "fixedh");
		System.out.println(inchiOut);
		
		
		// FixedH-InChI from InChI
		
		String inchiOut1 = OCL.getInChIFromInChI(inchi, "standard");
		String inchiOut1f = OCL.getInChIFromInChI(inchi, "fixedH");
		System.out.println(smiles);
		System.out.println(inchiOut1);
		System.out.println(inchiOut1f);
		
		// FixedH-InChI from FixedH-InChI

		// smiles to inchi
		smiles = "O=C1-NC=C1";

		System.out.println(smiles);
		
		inchiOut1f = OCL.getInChIFromSmiles(smiles, "fixedh");
		System.out.println(inchiOut1f);		
		String inchiOut2f = OCL.getInChIFromInChI(inchiOut1f, "fixedH");
		System.out.println(inchiOut2f);
		smiles = OCL.getSmilesFromInChI(inchiOut2f);
		System.out.println(smiles);

// not in CDK, so not in this test, either
//		// and
//		
//		inchi = OCL.getInChIFromInChI("InChI=1/C3H3NO/c5-3-1-2-4-3/h1-2H,(H,4,5)", "reference");
//		System.out.println(inchi);		
//		inchi = OCL.getInChIFromInChI("InChI=1/C3H3NO/c5-3-1-2-4-3/h1-2H,(H,4,5)/f/h4H", "reference");
//		System.out.println(inchi);		
//		inchi = OCL.getInChIFromInChI("InChI=1/C3H3NO/c5-3-1-2-4-3/h1-2H,(H,4,5)/f/h5H", "reference");
//		System.out.println(inchi);		
//
//		// all give the same thing. 
		
		// inchi model from inchi
		// not in JnaInchI System.out.println(OCL.getInChIModelJSON(inchi));
		
		inchi = "InChI=1S/C3H3NO/c5-3-1-2-4-3/h1-2H,(H,4,5)";
		// molecule from inchi
		mol = OCL.getOCLMoleculeFromInChI(inchi, null);
		testShowMol(mol, "from inchi");
	
		// optional fixed-H inchi from standard inchi
		// not in CDK
		String i2 = OCL.getInChIFromInChI(inchi, "fixedH");// '?");
		String inchiFixedH = "InChI=1/C3H3NO/c5-3-1-2-4-3/h1-2H,(H,4,5)/f/h5H";
		//checkEquals(inchiFixedH, i2, true, 4);
		
		// standard inchi from fixed-H inchi
		i2 = OCL.getInChIFromInChI(inchiFixedH, null);
		checkEquals(inchi, i2, true, 5);
		
		// molecule from inchi
		mol = OCL.getOCLMoleculeFromInChI(inchi, "fixamides");
		testShowMol(mol, "from inchi");

		// not in JnaInchI 
		/*
		String json;
		
		// inchi model from inchi
		inchi = "InChI=1S/C13H18BBrCl2O/c1-4-10(15)7-8(2)5-6-11(14)13(18)12(17)9(3)16/h4-6,9,18H,7,14H2,1-3H3/b8-5+,10-4-,11-6+,13-12+/t9-/m0/s1";
		json = OCL.getInChIModelJSON(inchi);
		System.out.println(json);
		checkEquals(true,json.length() > 1800, false, 0);
	
		// inchi model from MOL data
		String filein = "tallene.mol";
		String moldata = getString(filein);
		json = OCL.getInChIModelJSON(moldata);
		System.out.println(json);
		checkEquals(true,json.length() > 500, false, 0);
		*/
		inchi = OCL.getInChIFromSmiles("c1cnc1O", "standard");
		checkEquals("InChI=1S/C3H3NO/c5-3-1-2-4-3/h1-2H,(H,4,5)", inchi, true, 0);
		inchi = OCL.getInChIFromSmiles("c1cnc1O", "fixedh");
		checkEquals("InChI=1/C3H3NO/c5-3-1-2-4-3/h1-2H,(H,4,5)/f/h5H", inchi, true, 0);
		inchi = OCL.getInChIFromInChI("InChI=1/C3H3NO/c5-3-1-2-4-3/h1-2H,(H,4,5)/f/h5H", "fixedh");
		checkEquals("InChI=1/C3H3NO/c5-3-1-2-4-3/h1-2H,(H,4,5)/f/h5H", inchi, true, 0);
		// not in JnaInchI 
		//json = OCL.getInChIModelJSON(inchi);
		//System.out.println(json);
		//inchi C does not add fixed hydrogens to its model
		inchi = OCL.getInChIFromInChI("InChI=1S/C3H3NO/c5-3-1-2-4-3/h1-2H,(H,4,5)", "fixedH");
		checkEquals("InChI=1/C3H3NO/c5-3-1-2-4-3/h1-2H,(H,4,5)/f/h5H", inchi, true, 0);
		// and here as well, which is surprising to me:
		inchi = OCL.getInChIFromInChI("InChI=1/C3H3NO/c5-3-1-2-4-3/h1-2H,(H,4,5)/f/h5H", "standard");
		checkEquals("InChI=1S/C3H3NO/c5-3-1-2-4-3/h1-2H,(H,4,5)", inchi, true, 0);
		return;
	}

	private static void testEne(String outdir) {
		String inchi;
		inchi = "InChI=1S/C4H8/c1-3-4-2/h3-4H,1-2H3/b4-3-";
		testInChI(inchi, outdir, 0);
		inchi = "InChI=1S/C4H8/c1-3-4-2/h3-4H,1-2H3/b4-3+";
		testInChI(inchi, outdir, 0);		
		inchi = "InChI=1S/C20H32O2/c1-2-3-4-5-6-7-8-9-10-11-12-13-14-15-16-17-18-19-20(21)22/h6-7,9-10,12-13,15-16H,2-5,8,11,14,17-19H2,1H3,(H,21,22)/b7-6-,10-9-,13-12-,16-15-";
		testInChI(inchi, outdir, 0);
		inchi = "InChI=1S/C13H18BBrCl2O/c1-4-10(15)7-8(2)5-6-11(14)13(18)12(17)9(3)16/h4-6,9,18H,7,14H2,1-3H3/b8-5+,10-4-,11-6+,13-12+/t9-/m0/s1";
		testInChI(inchi, outdir, 0);
		inchi = "InChI=1S/C13H19BBrClO/c1-4-10(15)8-9(3)6-7-11(14)13(17)12(16)5-2/h4,6-7,17H,5,8,14H2,1-3H3/b9-6+,10-4-,11-7-,13-12+";
		testInChI(inchi, outdir, 0);
		inchi = "InChI=1S/C13H19BBrClO/c1-4-10(15)8-9(3)6-7-11(14)13(17)12(16)5-2/h4,6-7,17H,5,8,14H2,1-3H3/b9-6+,10-4-,11-7+,13-12+";
		testInChI(inchi, outdir, 0);
		inchi = "InChI=1S/C4H9BO/c1-2-4(5)3-6/h2,6H,3,5H2,1H3/b4-2+";
		testInChI(inchi, outdir, 0);
		inchi = "InChI=1S/C5H7BBrClO/c6-4(3-9)1-5(7)2-8/h1-2,9H,3,6H2/b4-1+,5-2+";
		testInChI(inchi, outdir, 0);
		inchi = "InChI=1S/C6H5BBr2ClFO/c7-3(2-12)5(6(9)11)4(8)1-10/h1-2,12H,7H2/b3-2-,4-1+,6-5-";
		testInChI(inchi, outdir, 0);

	}

	private static void testAllene(String outdir) {
		
		// note that PubChem will return allene structures with no stereochemistry
		String inchi;
		// InChI to mol and back
		// this is a triply conjugated tri-allene.
		inchi = "InChI=1S/C9H5BBr2ClFO/c10-7(2-4-15)6(5-9(12)14)8(11)1-3-13/h3-4,15H,10H2/t1-,2+,5-/m1/s1";
		testInChI(inchi, outdir, 1);
		inchi = "InChI=1S/C3HBrClF/c4-2-1-3(5)6/h2H/t1-/m0/s1";
		testInChI(inchi, outdir, 2);
		
		// mol to InChI
		String filein = "tallene.mol";
		String moldata = getString(filein);
		testInChIOut(moldata, inchi, true, 3);
		
	}

	private static void testSmilesParser(String outdir) {
		String smiles, inchi;

		// from https://cactus.nci.nih.gov/chemical/structure/[S@](=O)(C)CC/file?format=stdinchi
		String inchi0f = "InChI=1S/C3H3FO/c4-2-1-3-5/h2-3,5H/t1-/m0/s1"; 
//		String inchi1f = "InChI=1S/C3H3FO/c4-2-1-3-5/h2-3,5H/t1-/m1/s1";
		String inchi2f = "InChI=1S/C4H5FO/c1-4(5)2-3-6/h3,6H,1H3/t2-/m0/s1";

		// test 9 failing for OCL main, not here
		inchi ="InChI=1S/C3H3FO2/c4-3(6)1-2-5/h2,5-6H/t1-/m1/s1";
		smiles = "OC(F)=[C@]=C(O)[H]";
		testSmilesInChI(smiles, inchi, true, 0);
		smiles = "OC(F)=[C@@]=CO";		
		testSmilesInChI(smiles, inchi, true, 0);

		// note: these two both give the same result (/m1/s1)
		// https://cactus.nci.nih.gov/chemical/structure/[H]C([2H])=[C@]=CF/file?format=stdinchi
		// https://cactus.nci.nih.gov/chemical/structure/[2H]C([H])=[C@]=CF/file?format=stdinchi
		smiles = "[H]C([2H])=[C@]=CF"; 
		testSmilesInChI(smiles, "InChI=1S/C3H3F/c1-2-3-4/h3H,1H2/i1D/t2-/m1/s1", true, 201); 
		smiles = "[2H]C([H])=[C@]=CF"; 
		testSmilesInChI(smiles, "InChI=1S/C3H3F/c1-2-3-4/h3H,1H2/i1D/t2-/m0/s1", true, 202); 

		
		smiles = "[H]C(O)=[C@@]=CF"; 
		testSmilesInChI(smiles, inchi0f, true, 203); 
		smiles = "OC([H])=[C@]=CF"; 
		testSmilesInChI(smiles, inchi0f, true, 204); 
		smiles = "OC=[C@]=C([H])F";
		testSmilesInChI(smiles, inchi0f, true, 205); 		
		smiles = "OC=[C@@]=C1[H].F1";
		testSmilesInChI(smiles, inchi0f, true, 0); 
		smiles = "OC=[C@]=C1F.[H]1";
		testSmilesInChI(smiles, inchi0f, true, 0);

		smiles = "CC(F)=[C@]=C(O)[H]";// main() reports error because is OC(F) not CC(F)
		testSmilesInChI(smiles, inchi2f, true, 0);
		smiles = "CC(F)=[C@@]=CO";		
		testSmilesInChI(smiles, inchi2f, true, 0);

		// from https://cactus.nci.nih.gov/chemical/structure/[S@](=O)(C)CC/file?format=stdinchi
		String inchi0s = "InChI=1S/C3H8OS/c1-3-5(2)4/h3H2,1-2H3/t5-/m0/s1";
		// from https://cactus.nci.nih.gov/chemical/structure/[S@](=O)(N)CC/file?format=stdinchi
		String inchi1n = "InChI=1S/C2H7NOS/c1-2-5(3)4/h2-3H2,1H3/t5-/m1/s1";
		smiles = "N[S@@](CC)=O";	
		testSmilesInChI(smiles, inchi1n, true, 0);
		smiles = "[S@](N)(CC)=O";	
		testSmilesInChI(smiles, inchi1n, true, 0);
		smiles = "[S@](=O)(N)CC";
		testSmilesInChI(smiles, inchi1n, true, 0);
		smiles = "CC[S@](N)=O";
		testSmilesInChI(smiles, inchi1n, true, 0);
		
		smiles = "C[S@@](CC)=O";	
		testSmilesInChI(smiles, inchi0s, true, 0);
		smiles = "[S@](=O)(C)CC";
		testSmilesInChI(smiles, inchi0s, true, 0);
		smiles = "CC[S@](C)=O";
		testSmilesInChI(smiles, inchi0s, true, 0);

	
		smiles = "[C@H](N)(C)C(=O)O";
		inchi = "InChI=1S/C3H7NO2/c1-2(4)3(5)6/h2H,4H2,1H3,(H,5,6)/t2-/m0/s1";
		testSmilesInChI(smiles, inchi, true, 0);
		smiles = "N[C@@H](C)C(=O)O";
		testSmilesInChI(smiles, inchi, true, 0);

		testSmilesInChI("[C@H](F)(B)O", "InChI=1S/CH4BFO/c2-1(3)4/h1,4H,2H2/t1-/m1/s1", true, 0);

		String inchi0 = "InChI=1S/C3H2BrF/c4-2-1-3-5/h2-3H/t1-/m0/s1";
		String inchi1 = "InChI=1S/C3H2BrF/c4-2-1-3-5/h2-3H/t1-/m1/s1";
		String inchi0cl = "InChI=1S/C3HBrClF/c4-2-1-3(5)6/h2H/t1-/m0/s1";
		String inchi1cl = "InChI=1S/C3HBrClF/c4-2-1-3(5)6/h2H/t1-/m1/s1";

// conjugated allene fails DO I CARE??
		// the problem here is that InChI cannot take un-wedged allenes in
		// and these conjugated ones utilize those, but there are problems
		// around the conjugation.
//		smiles = "C(O)=[C@@]=C(B)C1=[C@@]=C(F)Br.C1(Br)=[C@]=CCl";
//		inchi = "InChI=1S/C9H5BBr2ClFO/c10-7(2-4-15)6(5-9(12)14)8(11)1-3-13/h3-4,15H,10H2/t1-,2+,5-/m1/s1";
//		testSmilesInChI(smiles, inchi, true);		

//		// Jmol SMILES and InChI
		smiles = "C(O)=[C@]=C(B)CCC1=[C@@]=C(F)Br.C1CCC(Br)=[C@]=CCl";
		smiles = "B[C](CC[C](CCC[C](Br)=[C@]=[CH]Cl)=[C@@]=[C](F)Br)=[C@]=[CH]O";
		inchi = "InChI=1S/C14H15BBr2ClFO/c15-12(7-9-20)5-4-11(10-14(17)19)2-1-3-13(16)6-8-18/h8-9,20H,1-5,15H2/t6-,7+,10+/m1/s1";
		testSmilesInChI(smiles, inchi, true, 0);

		smiles = "B[C](CCC)=[C@]=[CH]O";
		inchi = "InChI=1S/C6H11BO/c1-2-3-6(7)4-5-8/h5,8H,2-3,7H2,1H3/t4-/m0/s1";
		testSmilesInChI(smiles, inchi, true, 0);

		smiles = "CCCC(B)=[C@@]=CO";
		inchi = "InChI=1S/C6H11BO/c1-2-3-6(7)4-5-8/h5,8H,2-3,7H2,1H3/t4-/m0/s1";
		testSmilesInChI(smiles, inchi, true, 0);

// from https://cactus.nci.nih.gov/chemical/structure/InChI=1S/C14H15BBr2ClFO/c15-12(7-9-20)5-4-11(10-14(17)19)2-1-3-13(16)6-8-18/h8-9,20H,1-5,15H2/t6-,7+,10+/m1/s1/file?format=smiles
		smiles = "B[C](CC[C](CCC[C](Br)=[C@]=[CH]Cl)=[C@@]=[C](F)Br)=[C@]=[CH]O";
		inchi = "InChI=1S/C14H15BBr2ClFO/c15-12(7-9-20)5-4-11(10-14(17)19)2-1-3-13(16)6-8-18/h8-9,20H,1-5,15H2/t6-,7+,10+/m1/s1";
		testSmilesInChI(smiles, inchi, true, 0);

		smiles = "FC=[C@]=CBr";
		testSmilesInChI(smiles, inchi0, true, 0);
		smiles = "F[CH]=[C@]=CBr";
		testSmilesInChI(smiles, inchi0, true, 0);

		smiles = "C(F)=[C@]=CBr";
		testSmilesInChI(smiles, inchi1, true, 0);
		smiles = "[CH](F)=[C@]=CBr";
		testSmilesInChI(smiles, inchi1, true, 0);
		smiles = "C1=[C@]=CBr.F1";
		testSmilesInChI(smiles, inchi1, true, 0);
		smiles = "F1.C1=[C@]=CBr";
		testSmilesInChI(smiles, inchi1, true, 0);

		smiles = "FC=[C@@]=CBr";
		testSmilesInChI(smiles, inchi1, true, 0);

		smiles = "FC(Cl)=[C@]=CBr";
		testSmilesInChI(smiles, inchi0cl, true, 0);
		smiles = "C1(Cl)=[C@]=CBr.F1";
		testSmilesInChI(smiles, inchi0cl, true, 0);
		smiles = "F1.C1(Cl)=[C@]=CBr";
		testSmilesInChI(smiles, inchi0cl, true, 0);
		smiles = "C12=[C@]=CBr.F1.Cl2";
		testSmilesInChI(smiles, inchi0cl, true, 0);
		smiles = "C21=[C@]=CBr.F1.Cl2";
		testSmilesInChI(smiles, inchi1cl, true, 0);
		smiles = "Cl1.F2.C21=[C@]=CBr";
		testSmilesInChI(smiles, inchi0cl, true, 0);
		smiles = "C21=[C@]=CBr.Cl1.F2";
		testSmilesInChI(smiles, inchi0cl, true, 0);

		smiles = "N12C(=O)OC(C)(C)C.C1CC[C@H]2C(=O)[N](CCC)C1=CC=CC2=CC=CC=C12";
		inchi = "InChI=1S/C23H30N2O3/c1-5-15-24(19-13-8-11-17-10-6-7-12-18(17)19)21(26)20-14-9-16-25(20)22(27)28-23(2,3)4/h6-8,10-13,20H,5,9,14-16H2,1-4H3/t20-/m0/s1";
		testSmilesInChI(smiles, inchi, true, 0);		

		smiles = "C1=CC(O)=C2C3=C1C[C@@H]4[C@H]5[C@]36[C@@H]7[C@@H](O)C=C5.O72.C6CN4C";
		inchi = "InChI=1S/C17H19NO3/c1-18-7-6-17-10-3-5-13(20)16(17)21-15-12(19)4-2-9(14(15)17)8-11(10)18/h2-5,10-11,13,16,19-20H,6-8H2,1H3/t10-,11+,13-,16-,17-/m0/s1";
		testSmilesInChI(smiles, inchi, true, 0);		

		smiles = "CN1CC[C@@]23[C@H]4OC5=C(O)C=CC(=C25)C[C@@H]1[C@@H]3C=C[C@@H]4O";
		inchi = "InChI=1S/C17H19NO3/c1-18-7-6-17-10-3-5-13(20)16(17)21-15-12(19)4-2-9(14(15)17)8-11(10)18/h2-5,10-11,13,16,19-20H,6-8H2,1H3/t10-,11+,13-,16-,17-/m0/s1";
		testSmilesInChI(smiles, inchi, true, 0);		
		
	}

	private static void testSmilesInChI(String smiles, String inchi, boolean throwError, int testpt) {
		StereoMolecule mol = OCL.getOCLMoleculeFromSmiles(smiles);
		if (!testInChIOut(mol, inchi, false, testpt)) {
			throw new RuntimeException();
		}
		return;
	}

	private static void testShowMol(StereoMolecule mol, String title) {
		//testShowViewAndWriteMol(mol, title, null, null);
	}


	private static void testInChIParsers(String outdir) {

		String[] tests = new String[] {
				// inchi = "InChI=1S/C4H11N/c1-3-5-4-2/h5H,3-4H2,1-2H3";
				// note that this next one is nonstandard, as it indicates the "higher" 5+6- not
				// the "lower" 5-6+ option
				// the inchi will be accepted, but it will be corrected if output
				// inchi = "InChI=1S/C6H10BrCl/c7-5-3-1-2-4-6(5)8/h5-6H,1-4H2/t5+,6-/m0/s1";
				"InChI=1S/C4H10O/c1-3-4(2)5/h4-5H,3H2,1-2H3/t4-/m0/s1",
				"InChI=1S/C4H8BrCl/c1-3-4(2,5)6/h3H2,1-2H3/t4-/m1/s1",
				"InChI=1S/C6H10BrCl/c7-5-3-1-2-4-6(5)8/h5-6H,1-4H2/t5-,6+/m1/s1",
				"InChI=1S/C12H22Br4/c1-3-5-10(14)7-12(16)8-11(15)6-9(13)4-2/h9-12H,3-8H2,1-2H3/t9-,10+,11-,12+/m1/s1",
				"InChI=1S/C17H19NO3/c1-18-7-6-17-10-3-5-13(20)16(17)21-15-12(19)4-2-9(14(15)17)8-11(10)18/h2-5,10-11,13,16,19-20H,6-8H2,1H3/t10-,11+,13-,16-,17-/m0/s1",
				"InChI=1S/C41H45NO21/c43-13-27-32(51)34(53)37(56)40(61-27)59-25-11-19(45)10-21-20(25)12-26(30(42-21)17-4-7-22(46)23(47)9-17)60-41-38(63-39-36(55)31(50)24(48)14-58-39)35(54)33(52)28(62-41)15-57-29(49)8-3-16-1-5-18(44)6-2-16/h1-12,24,27-28,31-41,43-48,50-56H,13-15H2"
				+ "/b8-3+/t24-,27-,28-,31+,32-,33-,34+,35+,36-,37-,38-,39+,40-,41-/m1/s1"
		};

		for (int i = 0; i < tests.length; i++)
			testInChI(tests[i], outdir, 0);

	}

	private static void testInChI(String inchi, String outdir, int testpt) {
		StereoMolecule mol = OCL.getOCLMoleculeFromInChI(inchi, null);
		// not in JnaInchi System.out.println(OCL.getInChIModelJSON(inchi));
		testInChIOut(mol, inchi, true, 0);
	}

	private static boolean testInChIOut(StereoMolecule mol, String inchi, boolean throwError, int testpt) {
		String options = "";
		String s = OCL.getInChIFromOCLMolecule(mol, options);
		if (s == null || s.length() == 0)
			s = "<inchi was null>";
		return checkEquals(inchi, s,throwError, testpt);
	}
	
	private static boolean testInChIOut(String molData, String inchi, boolean throwError, int testpt) {
		String options = "";
		String s = OCL.getInChIFromMOL(molData, options);
		if (s == null || s.length() == 0)
			s = "<inchi was null>";
		return checkEquals(inchi, s,throwError, testpt);
	}
	
	static int nChecked = 0;

	private static boolean checkEquals(Object expected, Object got, boolean throwError, int testpt) {
		nChecked++;
			boolean ok = expected.equals(got);
		System.out.println(nChecked + "." + testpt + " exp:" + expected);
		System.out.println(nChecked + "." + testpt + " got:" + got); 
		System.out.println(ok + " " + nChecked + " " +  OCL.getCallCount());
		if (!ok) 
		if (!ok && throwError)
			throw new RuntimeException("checkEquals fails at " + testpt);
		return ok;		
	}

	private static void writeImage(BufferedImage bi, String fname, String dir) {
		if (dir == null || dir.length() == 0)
			return;
		if (!dir.endsWith("/"))
			dir += "/";
		fname = dir + fname;
		try {
			FileOutputStream fos = new FileOutputStream(fname);
			ImageIO.write(bi, "png", fos);
			fos.close();
			System.out.println("Created " + fname + " " + new File(fname).length());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static String getString(String filein) {
		return new String(getBytes(filein));
	}
	
	private static byte[] getBytes(String filein) {
		try {
			return getResourceBytes(OCLSwingJSTest2.class, filein);
		} catch (IOException e) {
			return null;
		}
	}
	
	
	public static byte[] getResourceBytes(Class<?> c, String fileName) throws FileNotFoundException, IOException {
		return getLimitedStreamBytes(c.getResourceAsStream(fileName), -1, null, true, true);
	}

	public static byte[] getLimitedStreamBytes(InputStream is, int n, OutputStream out, boolean andCloseInput,
			boolean andCloseOutput) throws IOException {

		// Note: You cannot use InputStream.available() to reliably read
		// zip data from the web.

		boolean toOut = (out != null);
		int buflen = (n > 0 && n < 1024 ? (int) n : 1024);
		byte[] buf = new byte[buflen];
		byte[] bytes = (out == null ? new byte[n < 0 ? 4096 : (int) n] : null);
		int len = 0;
		int totalLen = 0;
		if (n < 0)
			n = Integer.MAX_VALUE;
		while (totalLen < n && (len = is.read(buf, 0, buflen)) > 0) {
			totalLen += len;
			if (toOut) {
				out.write(buf, 0, len);
			} else {
				if (totalLen > bytes.length)
					bytes = Arrays.copyOf(bytes, totalLen * 2);
				System.arraycopy(buf, 0, bytes, totalLen - len, len);
				if (n != Integer.MAX_VALUE && totalLen + buflen > bytes.length)
					buflen = bytes.length - totalLen;
			}
		}
		if (andCloseInput) {
			try {
				is.close();
			} catch (IOException e) {
				// ignore
			}
		}
		if (toOut) {
			if (andCloseOutput)
				try {
					out.close();
				} catch (IOException e) {
					// ignore
				}
			return null;
		}
		if (totalLen == bytes.length)
			return bytes;
		buf = new byte[totalLen];
		System.arraycopy(bytes, 0, buf, 0, totalLen);
		return buf;
	}





}