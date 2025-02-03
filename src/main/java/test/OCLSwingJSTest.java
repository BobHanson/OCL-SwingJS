package test;

import java.awt.Color;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import com.actelion.research.chem.AbstractDepictor;
import com.actelion.research.chem.MolfileCreator;
import com.actelion.research.chem.MolfileParser;
import com.actelion.research.chem.SmilesParser;
import com.actelion.research.chem.StereoMolecule;
import com.actelion.research.chem.inchi.InChIOCL;
import com.actelion.research.chem.moreparsers.CDXParser;
import com.actelion.research.chem.moreparsers.ChemicalNameResolver;
import com.actelion.research.chem.moreparsers.InChIKeyResolver;
import com.actelion.research.chem.moreparsers.InChIParser;
import com.actelion.research.chem.moreparsers.InChIResolver;
import com.actelion.research.chem.moreparsers.ParserUtils;
import com.actelion.research.gui.FileHelper;
import com.actelion.research.gui.JStructureView;
import com.actelion.research.gui.editor.SwingEditorArea;
import com.actelion.research.gui.editor.SwingEditorPanel;
import com.actelion.research.gui.swing.SwingDialog;

public class OCLSwingJSTest {

	public static int nFrame;
	private final static boolean showFrames = false;

	@SuppressWarnings("unused")
	public static void main(String[] args) {
		// load JavaScript:
		InChIOCL.init();
		if (/** @j2sNative true ||*/ false) {
			runAsync();
		} else {
			runTests();
		}
	}

	private static void runAsync() {
		// just need a single clock tick
		// SwingUtilities.invokeLater() does not work.
		Timer t = new Timer(1, new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				runTests();
				//System.exit(0);
			}
			
		});
		t.setRepeats(false);
		t.start();

}

	protected static void runTests() {
		long t = System.currentTimeMillis();
		Object x = InChIOCL.getInChI("adfadsf", "FixedH");
		String outdir = null;//"C:/temp/";
		testInChI1(outdir);
		testSmilesParser(outdir);
		testCDXParsers(outdir);	
		testInChIParsers(outdir);
		testAllene(outdir);
		testEne(outdir);
		//testResolvers(outdir);
		System.out.println("DONE " + nChecked + " " + (System.currentTimeMillis() - t) + " ms");
	}

	private static void testResolvers(String outdir) {
		StereoMolecule mol;
		String fileout;

		// known inchi key to PubChem
		
		fileout = "tinchikeypc";
		String inchiKey = "BQJCRHHNABKAKU-KBQPJGBKSA-N";
		System.out.println(inchiKey + " => " + fileout);
		mol = new StereoMolecule();
		if (new InChIKeyResolver().setSource(InChIKeyResolver.SOURCE_CIR).resolve(mol, inchiKey)) {
			testShowViewAndWriteMol(mol, "inchikey", fileout, outdir);
			String keyReturned = InChIOCL.getInChIKey(mol, null);
			checkEquals(inchiKey, keyReturned, true, 101);
			//String smiles = "CN(CC[C@]12c3c(C4)ccc(O)c3O[C@H]11)[C@H]4[C@@H]2C=C[C@@H]1O";
			//checkEquals(new IsomericSmilesCreator(mol).getSmiles(), smiles, true, 101);
		} else {
			checkEquals(inchiKey, "not found at PubChem", true, 103);
		}

		// known inchi to PubChem
		
		fileout = "tinchipc";
 		mol = new StereoMolecule();
 		String inchi;
 		inchi = "InChI=1S/C3H3NO/c5-3-1-2-4-3/h1-2H,(H,4,5)";
		if (new InChIResolver().setSource(InChIResolver.SOURCE_CIR).resolve(mol, inchi)) {
			testShowViewAndWriteMol(mol, "inchi", fileout, outdir);
			testInChIOut(mol, inchi, true, 102);
		} else {
			checkEquals(inchi, "not found at PubChem", true, 103);
		}

		// unknown inchi to PubChem
		// will not be found

		fileout = "tinchipc";
 		mol = new StereoMolecule();
 		inchi = "InChI=1S/C13H18BBrCl2O/c1-4-10(15)7-8(2)5-6-11(14)13(18)12(17)9(3)16/h4-6,9,18H,7,14H2,1-3H3/b8-5+,10-4-,11-6+,13-12+/t9-/m0/s1";
		if (new InChIResolver().setSource(InChIResolver.SOURCE_PUBCHEM).resolve(mol, inchi)) {
			testShowViewAndWriteMol(mol, "inchi", fileout, outdir);
			testInChIOut(mol, inchi, true, 104);
		} else {
			checkEquals(inchi, "unknown InChI not found at PubChem, as expected", false, 105);
		}

		// unknown inchi to CIR
		
		fileout = "tinchipc";
 		mol = new StereoMolecule();
 		inchi = "InChI=1S/C13H18BBrCl2O/c1-4-10(15)7-8(2)5-6-11(14)13(18)12(17)9(3)16/h4-6,9,18H,7,14H2,1-3H3/b8-5+,10-4-,11-6+,13-12+/t9-/m0/s1";;
		if (new InChIResolver().setSource(InChIResolver.SOURCE_CIR).resolve(mol, inchi)) {
			testShowViewAndWriteMol(mol, "inchi", fileout, outdir);
			testInChIOut(mol, inchi, true, 106);
		} else {
			checkEquals(inchi, "not found at CIR", true, 107);
		}

		
		
		// oddball chemical name to NCI/CADD CIR
		
		String name = "(R)-cis-4-hydroxyhex-2-ene";
		// is actually (Z,3R)-hex-4-en-3-ol
		mol = new StereoMolecule();
		if (new ChemicalNameResolver().resolve(mol, name)) {
		inchi = InChIOCL.getInChI(mol, null);
		testShowMol(mol, name);
		checkEquals("InChI=1S/C6H12O/c1-3-5-6(7)4-2/h3,5-7H,4H2,1-2H3/b5-3-/t6-/m1/s1", inchi, true, 108);
		} else {
			checkEquals(name, "not found", true, 109);
		}
		

	}

	private static void testInChI1(String outdir) { 
		StereoMolecule mol;
		String inchi = "InChI=1S/C3H3NO/c5-3-1-2-4-3/h1-2H,(H,4,5)";
		
		// SMILES from inchi
		System.out.println(InChIOCL.getSmilesFromInChI(inchi, null));
		
		// inchi model from inchi
		System.out.println(InChIOCL.getInChIModelJSON(inchi));
		
		// molecule from inchi
		mol = new StereoMolecule();
		InChIOCL.getMoleculeFromInChI(inchi, mol);
		testShowMol(mol, "from inchi");
	
		// optional fixed-H inchi from standard inchi
		String i2 = InChIOCL.getInChI(inchi, "fixedH?");
		String inchiFixedH = "InChI=1/C3H3NO/c5-3-1-2-4-3/h1-2H,(H,4,5)/f/h5H";
		checkEquals(inchiFixedH, i2, true, 4);
		
		// standard inchi from fixed-H inchi
		i2 = InChIOCL.getInChI(inchiFixedH, null);
		checkEquals(inchi, i2, true, 5);
		
		// molecule from inchi
		mol = new StereoMolecule();
		InChIOCL.getMoleculeFromInChI(inchi, mol);
		testShowMol(mol, "from inchi");

		String json;
		
		// inchi model from inchi
		inchi = "InChI=1S/C13H18BBrCl2O/c1-4-10(15)7-8(2)5-6-11(14)13(18)12(17)9(3)16/h4-6,9,18H,7,14H2,1-3H3/b8-5+,10-4-,11-6+,13-12+/t9-/m0/s1";
		json = InChIOCL.getInChIModelJSON(inchi);
		System.out.println(json);
		checkEquals(true,json.length() > 1800, false, 0);
	
		// inchi model from MOL data
		String filein = "tallene.mol";
		String moldata = getString(filein, outdir);
		json = InChIOCL.getInChIModelJSON(moldata);
		System.out.println(json);
		checkEquals(true,json.length() > 1800, false, 0);
		
		inchi = InChIOCL.getInChIFromSmiles("c1cnc1O", "standard");
		checkEquals("InChI=1S/C3H3NO/c5-3-1-2-4-3/h1-2H,(H,4,5)", inchi, true, 0);
		inchi = InChIOCL.getInChIFromSmiles("c1cnc1O", "fixedh");
		checkEquals("InChI=1/C3H3NO/c5-3-1-2-4-3/h1-2H,(H,4,5)/f/h5H", inchi, true, 0);
		inchi = InChIOCL.getInChI("InChI=1/C3H3NO/c5-3-1-2-4-3/h1-2H,(H,4,5)/f/h5H", "fixedh");
		checkEquals("InChI=1/C3H3NO/c5-3-1-2-4-3/h1-2H,(H,4,5)/f/h5H", inchi, true, 0);
		json = InChIOCL.getInChIModelJSON(inchi);
		System.out.println(json);
		//inchi C does not add fixed hydrogens to its model
		inchi = InChIOCL.getInChI("InChI=1S/C3H3NO/c5-3-1-2-4-3/h1-2H,(H,4,5)", "fixedH");
		checkEquals("InChI=1/C3H3NO/c5-3-1-2-4-3/h1-2H,(H,4,5)/f/h5H", inchi, true, 0);
		// and here as well, which is surprising to me:
		inchi = InChIOCL.getInChI("InChI=1/C3H3NO/c5-3-1-2-4-3/h1-2H,(H,4,5)/f/h5H", "standard");
		checkEquals("InChI=1S/C3H3NO/c5-3-1-2-4-3/h1-2H,(H,4,5)", inchi, true, 0);
		return;
	}

	private static String smilesToMolfile(String smiles) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			StereoMolecule mol = new SmilesParser().parseMolecule(smiles);
			MolfileCreator creator = new MolfileCreator(mol);
			OutputStreamWriter writer = new OutputStreamWriter(bos);
			creator.writeMolfile(writer);
			writer.close();
			return new String(bos.toByteArray());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
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

	private static void testDialog(String[] args) {
		SwingUtilities.invokeLater(()->{showDialogTest(args);});
	}

	private static void testAllene(String outdir) {
		
		// note that PubChem will return allene structures with no stereochemistry
		String inchi;
		// InChI to mol and back
		inchi = "InChI=1S/C9H5BBr2ClFO/c10-7(2-4-15)6(5-9(12)14)8(11)1-3-13/h3-4,15H,10H2/t1-,2+,5-/m1/s1";
		testInChI(inchi, outdir, 1);
		inchi = "InChI=1S/C3HBrClF/c4-2-1-3(5)6/h2H/t1-/m0/s1";
		testInChI(inchi, outdir, 2);
		
		// mol to InChI
		String filein = "tallene.mol";
		String fileout = "tallene";
		String moldata = getString(filein, outdir);
		StereoMolecule mol = new StereoMolecule();
		if (!new MolfileParser().parse(mol, moldata)) {
			System.err.println("OCLSwingJSTest parser failed for " + moldata);
		}
		testShowViewAndWriteMol(mol, "allene", fileout, outdir);
		testInChIOut(mol, inchi, true, 3);
		
	}

	private static void testSmilesParser(String outdir) {
		String smiles, inchi;

		String s = smilesToMolfile("CCC");
		System.out.println(s);

		// from https://cactus.nci.nih.gov/chemical/structure/[S@](=O)(C)CC/file?format=stdinchi
		String inchi0f = "InChI=1S/C3H3FO/c4-2-1-3-5/h2-3,5H/t1-/m0/s1"; 
//		String inchi1f = "InChI=1S/C3H3FO/c4-2-1-3-5/h2-3,5H/t1-/m1/s1";
		String inchi2f = "InChI=1S/C4H5FO/c1-4(5)2-3-6/h3,6H,1H3/t2-/m0/s1";
		
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
		System.out.println(smiles);
		StereoMolecule mol = new SmilesParser().parseMolecule(smiles);
		JStructureView view = testShowViewAndWriteMol(mol, "smiles", null, null);
		if (!testInChIOut(mol, inchi, false, testpt)) {
			view.setBackground(Color.yellow);
			System.out.println(smiles + " failed");
			if (throwError)
				throw new RuntimeException();
		}
		return;
	}

	private static void testShowMol(StereoMolecule mol, String title) {
		testShowViewAndWriteMol(mol, title, null, null);
	}

	private static JStructureView testShowViewAndWriteMol(StereoMolecule mol, String title, String fileout, String outdir) {
		JStructureView view = JStructureView.getStandardView(
				JStructureView.classicView
				, mol);
		if (showFrames) 
		view.showInFrame(title, nextLoc());
		if (fileout != null) {
			writeViewImage(view, fileout + ".png", outdir);
			writeMolFile(mol, fileout + ".mol", outdir);
		}
		return view;
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
		};

		for (int i = 0; i < tests.length; i++)
			testInChI(tests[i], outdir, 0);

	}

	private static void testInChI(String inchi, String outdir, int testpt) {
		String fileout = "tinchi";
 		StereoMolecule mol = new StereoMolecule();
 		fileout= "tinchi-jni";
		System.out.println(inchi + " => " + fileout);
		System.out.println(InChIOCL.getInChIModelJSON(inchi));
		if (new InChIParser().parse(mol, inchi)) {
			testShowViewAndWriteMol(mol, "fromInchi", fileout, outdir);
			testInChIOut(mol, inchi, true, 0);
		}
	}

	private static boolean testInChIOut(StereoMolecule mol, String inchi, boolean throwError, int testpt) {
		String options = "";
		String s = InChIOCL.getInChI(mol, options);
		if (s.length() == 0)
			s = "<inchi was null>";
		return checkEquals(inchi, s,throwError, testpt);
	}
	
	static int nChecked = 0;

	private static boolean checkEquals(Object expected, Object got, boolean throwError, int testpt) {
		nChecked++;
			boolean ok = expected.equals(got);
		System.out.println(nChecked + "." + testpt + " exp:" + expected);
		System.out.println(nChecked + "." + testpt + " got:" + got); 
		System.out.println(ok);
		if (!ok) 
		if (!ok && throwError)
			throw new RuntimeException("checkEquals fails at " + testpt);
		return ok;		
	}

	private static void testCDXParsers(String outdir) {
		String filein, fileout;

		filein = "3aa.cdxml";
		fileout = "t3aa";
		testCDXML(filein, fileout, outdir);

		filein = "t.cdxml";
		fileout = "tcdxml";
		testCDXML(filein, fileout, outdir);
		
		filein = "t.cdx";
		fileout = "tcdx";
		testCDX(filein, fileout, outdir);

		// the CDX byte[] reader can also be used for cdxml data
		filein = "tout.cdxml";
		fileout = "tout";
		testCDX(filein, fileout, outdir);

		filein = "t-acs.cdxml";
		fileout = "t-acs-cdxml";
		testCDXML(filein, fileout, outdir);

		filein = "t-acs.cdx";
		fileout = "t-acs-cdx";
		testCDX(filein, fileout, outdir);
	}

	private static void testCDXML(String filein, String fileout, String outdir) {
		String cdxml;
		System.out.println(filein + " => " + fileout);
		// one can also use tye parseFile method with CDX or CDXML
		//		mol = CDXParser.parseFile(filein);
		StereoMolecule mol = new StereoMolecule();
		cdxml = getString(filein, outdir);
		if (new CDXParser().parse(mol, cdxml)) {
			testShowViewAndWriteMol(mol, "cdxml", fileout, outdir);
		}

	}

	private static void testCDX(String filein, String fileout, String outdir) {
		System.out.println(filein + " => " + fileout);
		StereoMolecule mol = new StereoMolecule();
		byte[] cdx = getBytes(filein, outdir);
		if (new CDXParser().parse(mol, cdx)) {
			testShowViewAndWriteMol(mol, "cdx", fileout, outdir);
		}
	}

	private static void writeViewImage(JStructureView view, String fileout, String outdir) {
		writeImage(view.getSizedImage(), fileout, outdir);
	}

	private static byte[] getBytes(String filein, String dir) {
		try {
			if (dir == null) {
				return ParserUtils.getResourceBytes(OCLSwingJSTest.class, filein);
			} else {
				if (!dir.endsWith("/"))
					dir += "/";
				return ParserUtils.getURLContentsAsBytes(dir + filein);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private static String getString(String filein, String dir) {
		try {
			return new String(getBytes(filein, dir));
		} catch (Exception e) {
			return null;
		}
	}

	private static void writeMolFile(StereoMolecule mol, String filename, String dir) {
		if (dir == null || dir.length() == 0)
			return;
		if (!dir.endsWith("/"))
			dir += "/";
		try {
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dir + filename), "UTF-8"));
			new MolfileCreator(mol).writeMolfile(bw);
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	static int frameY = -1;
	
	private static Point nextLoc() {
		if (frameY == -1) {
			frameY = 0;
		} else if (nFrame % 10 == 0) {
			frameY += 110;
			nFrame = 0;
		}
		return new Point(130 * nFrame++, frameY);
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

	@SuppressWarnings("unused")
	private static void showDialogTest(String[] args) {

		// Molecule.setDefaultAverageBondLength(HiDPIHelper.scale(12));

		new FileHelper(null).readStructuresFromFileAsync(false, (list) -> {
			StereoMolecule mol;
			String title;
			if (list == null || list.isEmpty()) {
				String smiles = JOptionPane.showInputDialog("enter a SMILES",
						"N12C(=O)OC(C)(C)C.C1CC[C@H]2C(=O)[NH]C1=CC=CC2=CC=CC=C12"
//			+ "CC(CCCC(C1CCC2C1(C)CCC1C2CC=C2C1(C)CCC(C2)O)C)C"
//						+ "CCN.[b]12-c3ccccc3.o1[b]4-c5ccccc5.o4[b]6-c7ccccc7.o62"


				
				);
				title = smiles;
				mol = new SmilesParser().parseMolecule(smiles); // Nc1cc(OCCO)cc(N)c1
			} else {
				title = "mol";
				mol = (StereoMolecule) list.get(0);
			}

			showEditFrame(mol);
			// test for SVG output
//
//			SVGDepictor svgd = new SVGDepictor(mol, mode, "");
//			svgd.validateView(null, new GenericRectangle(0, 0, 300, 200), AbstractDepictor.cModeInflateToHighResAVBL);
//			svgd.paint(null);
//			svgd.setLegacyMode(false);
//			System.out.println(svgd.toString());
//
		});

	}

	private static void showEditFrame(StereoMolecule mol) {
		SwingEditorPanel p = new SwingEditorPanel(mol);
		p.setSize(new Dimension(500, 500));

		int mode = AbstractDepictor.cDModeSuppressCIPParity | AbstractDepictor.cDModeSuppressESR
				| AbstractDepictor.cDModeSuppressChiralText;

		SwingEditorArea area = (SwingEditorArea) p.getComponent(0);
		area.getGenericDrawArea().setDisplayMode(mode);

		SwingDialog d = new SwingDialog(null, "testing2", ModalityType.MODELESS);

		p.setPreferredSize(new Dimension(500, 500));
		d.add(p);
		d.showDialog(()->{
			System.out.println("OK");
			System.exit(0);
		},()->{
			System.out.println("cancel");
			System.exit(1);
		});
	}


}