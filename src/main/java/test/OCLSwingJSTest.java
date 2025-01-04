package test;

import java.awt.Color;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.actelion.research.chem.AbstractDepictor;
import com.actelion.research.chem.MolfileCreator;
import com.actelion.research.chem.MolfileParser;
import com.actelion.research.chem.SmilesParser;
import com.actelion.research.chem.StereoMolecule;
import com.actelion.research.chem.inchi.InChIJNI;
import com.actelion.research.chem.moreparsers.CDXParser;
import com.actelion.research.chem.moreparsers.InChIKeyParser;
import com.actelion.research.chem.moreparsers.InChIParser;
import com.actelion.research.chem.moreparsers.ParserUtils;
import com.actelion.research.gui.FileHelper;
import com.actelion.research.gui.JStructureView;
import com.actelion.research.gui.editor.SwingEditorArea;
import com.actelion.research.gui.editor.SwingEditorPanel;
import com.actelion.research.gui.swing.SwingDialog;

public class OCLSwingJSTest {

	public static int nFrame;

	public static void main(String[] args) {
		String outdir = null;//"C:/temp/";
		testSmilesParser(outdir);
		testCDXParsers(outdir);
		testInChIParsers(outdir);
		testAllene(outdir);
		testEne(outdir);
		
		
		//testDialog(args);
	}

	private static void testEne(String outdir) {
		String inchi;
		inchi = "InChI=1S/C4H8/c1-3-4-2/h3-4H,1-2H3/b4-3-";
		testInChI(inchi, outdir);
		inchi = "InChI=1S/C4H8/c1-3-4-2/h3-4H,1-2H3/b4-3+";
		testInChI(inchi, outdir);		
		inchi = "InChI=1S/C20H32O2/c1-2-3-4-5-6-7-8-9-10-11-12-13-14-15-16-17-18-19-20(21)22/h6-7,9-10,12-13,15-16H,2-5,8,11,14,17-19H2,1H3,(H,21,22)/b7-6-,10-9-,13-12-,16-15-";
		testInChI(inchi, outdir);
		inchi = "InChI=1S/C13H18BBrCl2O/c1-4-10(15)7-8(2)5-6-11(14)13(18)12(17)9(3)16/h4-6,9,18H,7,14H2,1-3H3/b8-5+,10-4-,11-6+,13-12+/t9-/m0/s1";
		testInChI(inchi, outdir);
		inchi = "InChI=1S/C13H19BBrClO/c1-4-10(15)8-9(3)6-7-11(14)13(17)12(16)5-2/h4,6-7,17H,5,8,14H2,1-3H3/b9-6+,10-4-,11-7-,13-12+";
		testInChI(inchi, outdir);
		inchi = "InChI=1S/C13H19BBrClO/c1-4-10(15)8-9(3)6-7-11(14)13(17)12(16)5-2/h4,6-7,17H,5,8,14H2,1-3H3/b9-6+,10-4-,11-7+,13-12+";
		testInChI(inchi, outdir);
		inchi = "InChI=1S/C4H9BO/c1-2-4(5)3-6/h2,6H,3,5H2,1H3/b4-2+";
		testInChI(inchi, outdir);
		inchi = "InChI=1S/C5H7BBrClO/c6-4(3-9)1-5(7)2-8/h1-2,9H,3,6H2/b4-1+,5-2+";
		testInChI(inchi, outdir);
		inchi = "InChI=1S/C6H5BBr2ClFO/c7-3(2-12)5(6(9)11)4(8)1-10/h1-2,12H,7H2/b3-2-,4-1+,6-5-";
		testInChI(inchi, outdir);

	}

	private static void testDialog(String[] args) {
		SwingUtilities.invokeLater(()->{showDialogTest(args);});
	}

	private static void testAllene(String outdir) {
		
		// note that PubChem will return allene structures with no stereochemistry
		String inchi, smiles;
		// InChI to mol and back
		inchi = "InChI=1S/C9H5BBr2ClFO/c10-7(2-4-15)6(5-9(12)14)8(11)1-3-13/h3-4,15H,10H2/t1-,2+,5-/m1/s1";
		testInChI(inchi, outdir);
	
		inchi = "InChI=1S/C3HBrClF/c4-2-1-3(5)6/h2H/t1-/m0/s1";
		testInChI(inchi, outdir);
		
		// mol to InChI
		String filein = "tallene.mol";
		String fileout = "tallene";
		String moldata = getString(filein, outdir);
		StereoMolecule mol = new StereoMolecule();
		new MolfileParser().parse(mol, moldata);
		testShowViewAndWriteMol(mol, "allene", fileout, outdir);
		testInChIOut(mol, inchi, true);
		
	}

	private static void testSmilesParser(String outdir) {
		String smiles, inchi;

		inchi = "InChI=1S/C3H7NO2/c1-2(4)3(5)6/h2H,4H2,1H3,(H,5,6)/t2-/m0/s1";
		smiles = "[C@H](N)(C)C(=O)O";
		testSmilesInChI(smiles, inchi, true);
		smiles = "N[C@@H](C)C(=O)O";
		testSmilesInChI(smiles, inchi, true);

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
		testSmilesInChI(smiles, inchi, true);

		smiles = "B[C](CCC)=[C@]=[CH]O";
		inchi = "InChI=1S/C6H11BO/c1-2-3-6(7)4-5-8/h5,8H,2-3,7H2,1H3/t4-/m0/s1";
		testSmilesInChI(smiles, inchi, true);

		smiles = "CCCC(B)=[C@@]=CO";
		inchi = "InChI=1S/C6H11BO/c1-2-3-6(7)4-5-8/h5,8H,2-3,7H2,1H3/t4-/m0/s1";
		testSmilesInChI(smiles, inchi, true);

// from https://cactus.nci.nih.gov/chemical/structure/InChI=1S/C14H15BBr2ClFO/c15-12(7-9-20)5-4-11(10-14(17)19)2-1-3-13(16)6-8-18/h8-9,20H,1-5,15H2/t6-,7+,10+/m1/s1/file?format=smiles
		smiles = "B[C](CC[C](CCC[C](Br)=[C@]=[CH]Cl)=[C@@]=[C](F)Br)=[C@]=[CH]O";
		inchi = "InChI=1S/C14H15BBr2ClFO/c15-12(7-9-20)5-4-11(10-14(17)19)2-1-3-13(16)6-8-18/h8-9,20H,1-5,15H2/t6-,7+,10+/m1/s1";
		testSmilesInChI(smiles, inchi, true);

		smiles = "FC=[C@]=CBr";
		testSmilesInChI(smiles, inchi0, true);
		smiles = "F[CH]=[C@]=CBr";
		testSmilesInChI(smiles, inchi0, true);

		smiles = "C(F)=[C@]=CBr";
		testSmilesInChI(smiles, inchi1, true);
		smiles = "[CH](F)=[C@]=CBr";
		testSmilesInChI(smiles, inchi1, true);
		smiles = "C1=[C@]=CBr.F1";
		testSmilesInChI(smiles, inchi1, true);
		smiles = "F1.C1=[C@]=CBr";
		testSmilesInChI(smiles, inchi1, true);

		smiles = "FC=[C@@]=CBr";
		testSmilesInChI(smiles, inchi1, true);

		smiles = "FC(Cl)=[C@]=CBr";
		testSmilesInChI(smiles, inchi0cl, true);
		smiles = "C1(Cl)=[C@]=CBr.F1";
		testSmilesInChI(smiles, inchi0cl, true);
		smiles = "F1.C1(Cl)=[C@]=CBr";
		testSmilesInChI(smiles, inchi0cl, true);
		smiles = "C12=[C@]=CBr.F1.Cl2";
		testSmilesInChI(smiles, inchi0cl, true);
		smiles = "C21=[C@]=CBr.F1.Cl2";
		testSmilesInChI(smiles, inchi1cl, true);
		smiles = "Cl1.F2.C21=[C@]=CBr";
		testSmilesInChI(smiles, inchi0cl, true);
		smiles = "C21=[C@]=CBr.Cl1.F2";
		testSmilesInChI(smiles, inchi0cl, true);

		smiles = "N12C(=O)OC(C)(C)C.C1CC[C@H]2C(=O)[N](CCC)C1=CC=CC2=CC=CC=C12";
		inchi = "InChI=1S/C23H30N2O3/c1-5-15-24(19-13-8-11-17-10-6-7-12-18(17)19)21(26)20-14-9-16-25(20)22(27)28-23(2,3)4/h6-8,10-13,20H,5,9,14-16H2,1-4H3/t20-/m0/s1";
		testSmilesInChI(smiles, inchi, true);		

		smiles = "C1=CC(O)=C2C3=C1C[C@@H]4[C@H]5[C@]36[C@@H]7[C@@H](O)C=C5.O72.C6CN4C";
		inchi = "InChI=1S/C17H19NO3/c1-18-7-6-17-10-3-5-13(20)16(17)21-15-12(19)4-2-9(14(15)17)8-11(10)18/h2-5,10-11,13,16,19-20H,6-8H2,1H3/t10-,11+,13-,16-,17-/m0/s1";
		testSmilesInChI(smiles, inchi, true);		

		smiles = "CN1CC[C@@]23[C@H]4OC5=C(O)C=CC(=C25)C[C@@H]1[C@@H]3C=C[C@@H]4O";
		inchi = "InChI=1S/C17H19NO3/c1-18-7-6-17-10-3-5-13(20)16(17)21-15-12(19)4-2-9(14(15)17)8-11(10)18/h2-5,10-11,13,16,19-20H,6-8H2,1H3/t10-,11+,13-,16-,17-/m0/s1";
		testSmilesInChI(smiles, inchi, true);		
		
	}

	private static void testSmilesInChI(String smiles, String inchi, boolean throwError) {
		System.out.println(smiles);
		StereoMolecule mol = new SmilesParser().parseMolecule(smiles);
		JStructureView view = testShowViewAndWriteMol(mol, "smiles", null, null);
		if (!testInChIOut(mol, inchi, false)) {
			view.setBackground(Color.yellow);
			if (throwError)
				throw new RuntimeException();
		}
		return;
	}

	private static JStructureView testShowViewAndWriteMol(StereoMolecule mol, String title, String fileout, String outdir) {
		JStructureView view = JStructureView.getStandardView(
				JStructureView.classicView
				, mol);
		view.showInFrame(title, nextLoc());
		if (fileout != null) {
			writeViewImage(view, fileout + ".png", outdir);
			writeMolFile(mol, fileout + ".mol", outdir);
		}
		return view;
	}

	private static void testInChIParsers(String outdir) {
		StereoMolecule mol;
		String fileout;

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
			testInChI(tests[i], outdir);

		fileout = "tinchikey";
		String inchiKey = "BQJCRHHNABKAKU-KBQPJGBKSA-N";
		System.out.println(inchiKey + " => " + fileout);
		mol = new StereoMolecule();
		if (new InChIKeyParser().parse(mol, inchiKey)) {
			testShowViewAndWriteMol(mol, "inchikey", fileout, outdir);
		}
	}

	private static void testInChI(String inchi, String outdir) {
		String fileout = "tinchi";
		System.out.println(inchi + " => " + fileout);
 		StereoMolecule mol = new StereoMolecule();
		if (new InChIParser().parse(mol, inchi)) {
			testShowViewAndWriteMol(mol, "inchi", fileout, outdir);
			testInChIOut(mol, inchi, true);
		}

		fileout = "tinchipc";
		System.out.println("PubChem:" + inchi + " => " + fileout);
		mol = new StereoMolecule();
		if (new InChIParser().parse(mol, "PubChem:" + inchi)) {
			JStructureView view = testShowViewAndWriteMol(mol, "PubChem-inchi", fileout, outdir);
			if (!testInChIOut(mol, inchi, false)) {
			  view.setBackground(Color.LIGHT_GRAY);
			}
		}

	}

	private static boolean testInChIOut(StereoMolecule mol, String inchi, boolean throwError) {
		String s = InChIJNI.getInChI(mol, null);
		boolean ok = inchi.equals(s);
		System.out.println(inchi);
		System.out.println(s);
		System.out.println(ok);
		if (!ok && throwError)
			throw new RuntimeException("inchi roundtrip failure for " + inchi);
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
		} else if (nFrame % 9 == 0) {
			frameY += 110;
			nFrame = 0;
		}
		return new Point(150 * nFrame++, frameY);
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

	static class OclCodeCheck {

		private int nAvail = 6;
		private int pt;
		private byte[] idcode;
		private int mData;
		private int abits;
		private int bbits;
		private int nBytes;

		public boolean checkOCL(String s) {
			// remove coordinates
			nBytes = s.indexOf('!');
			if (nBytes < 0)
				nBytes = s.indexOf('#');
			if (nBytes < 0)
				nBytes = s.length();
			if (nBytes < 10 || nBytes > 1000) // reasonable?
				return false;
			idcode = s.substring(0, nBytes).getBytes();
			mData = (idcode[0] & 0x3F) << 11;
			try {
				if (idcode == null || idcode.length == 0)
					return false;
				abits = decodeBits(4);
				bbits = decodeBits(4);
				int version = 8;
				if (abits > 8) {
					// abits is the version number
					version = abits;
					abits = bbits;
				}
				if (version != 8 && version != 9)
					return false;
				int allAtoms = decodeBits(abits);
				int allBonds = decodeBits(bbits);
				int closureBonds = 1 + allBonds - allAtoms;
				if (allAtoms == 0 || closureBonds < 0 || closureBonds > allAtoms - 2)
					return false;
				int nitrogens = decodeBits(abits);
				int oxygens = decodeBits(abits);
				int otherAtoms = decodeBits(abits);
				int chargedAtoms = decodeBits(abits);
				checkBits(nitrogens);
				checkBits(oxygens);
				checkBits(otherAtoms);
				checkBits(chargedAtoms);
				return true;
			} catch (Throwable e) {
				return false;
			}
		}

		private void checkBits(int n) {
			if (n != 0) {
				// check for n monotonically increasing numbers
				for (int a = -1, i = 0; i < n; i++) {
					int b = decodeBits(abits);
					if (b <= a)
						throw new NullPointerException();
					a = b;
				}
			}
		}

		private int decodeBits(int bits) {
			int allBits = bits;

			int data = 0;
			while (bits != 0) {
				if (nAvail == 0) {
					if (++pt >= idcode.length)
						throw new NullPointerException();
					mData = (idcode[pt] & 0x3F) << 11;
					nAvail = 6;
				}
				data |= ((0x00010000 & mData) >> (16 - allBits + bits));
				mData <<= 1;
				bits--;
				nAvail--;
			}
			return data;
		}
	}

	static {
//		System.out.println(new OclCodeCheck().checkOCL("dcMD@ItARfUV^V``h`@@"));
//		System.out.println(new OclCodeCheck().checkOCL("daT@`@\\DjfjZn[jjjkJcKhGP`phxdtl|wY@xD`uyo]{|lKUfp"));
//		System.out.println(new OclCodeCheck().checkOCL("fc" + (char) 0x7F + "A@@@YEDeLhedihdXleJ\\eiwhyjijjjjjjjj`@@"));
//		System.out.println(new OclCodeCheck().checkOCL("g3\0A;ADLA;MD@ItARfUV^V``h`@@"));
//		String[] aminoAcidsLabeled = ("gGX`BDdwMULPGzILwXM[jD\n" + 
//		"dctd@BE]ADf{UYjjihp`GzBfMvCS]Plw^OMtbK]hrwUj}tRfwnbXp\n" + 
//		"diEL@BDDyInvZjZL`OtiL[lFfzaYn|^{iFLO]Hi`\n" + 
//		"diFB@BANEInvZjZLHA~eIc]`twTKMwcw]Hqa{iEL\n" + 
//		"gNxhMV@aI[jihj@SHF{qc]PinpP\n" + 
//		"defB@BAAeInufjihr@QdqnpZ[jEf{qyndQ{mFMO]hi`\n" + 
//		"deeL@BdDEInufjihp`GzLfMvCS]Plw^OMtbO]hqi{mEL\n" + 
//		"gJX`BDdvu@OtbYnpP\n" + 
//		"dmwD@ByPQInvVUZjejL`OtyL[lFfzaYn|^{iFLO]Hii{mFLo]hi`\n" + 
//		"diFD@BADf{ejjdrU@_iRXwXMMuBw]xqn{oELO]Hq`\n" + 
//		"diFD@BADf{Yjjhr@RdqnpZ[jEf{q{ndTp}tcF@\n" + 
//		"deeD@BdDR[mUjjjL`OtYL[lFfzaYn|^[iDV{QenkP\n" + 
//		"diFD`JxPBDivzjihI@RdAndX[oEF{QqnhR[lD\n" + 
//		"dcND@BADf{YU]Zj@@cHC}ASF{AinhV[oGnzQSCwRLZ^{QSKwZL[Vzm@\n" + 
//		"daFD@BADfyVyjjhr@PdqnpZ[jEfzQyn|P\n" + 
//		"gNy`BDtf{ZjfHC}Lf[lFmuBv{q@\n" + 
//		"dazL@BAFR[nZjdrT`_hRXwXMMuBw]xqn{oEL\n" + 
//		"foAP`@BZ@aInvYWejsfjiB@bFB@OttfF{AhwTKF{qywRJXW]Hqi]vbfUwZN[W]hqc]uZfmwUnYw]Di`\n" + 
//		"dknL@BACR[me]]Zj@BHr@RTqnpZ[jEf{q{ndTp}tcFgntTr}vcFunkS[hd\n" + 
//		"dazD@BADf{fjjL`OtIL[lFfza[n|Tw]wcF@s").split("\n");
//		for (int i = 0; i < aminoAcidsLabeled.length; i++)
//		System.out.println(new OclCodeCheck().checkOCL(aminoAcidsLabeled[i]));		
	}


}