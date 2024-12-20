package swingjs;

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
import com.actelion.research.chem.SmilesParser;
import com.actelion.research.chem.StereoMolecule;
import com.actelion.research.chem.moreparsers.CDXParser;
import com.actelion.research.chem.moreparsers.InChIKeyParser;
import com.actelion.research.chem.moreparsers.InChIParser;
import com.actelion.research.chem.moreparsers.ParserUtils;
import com.actelion.research.gui.FileHelper;
import com.actelion.research.gui.JStructureView;
import com.actelion.research.gui.editor.SwingEditorArea;
import com.actelion.research.gui.editor.SwingEditorPanel;
import com.actelion.research.gui.swing.SwingDialog;

public class OCLSwingTest {

	public static int nFrame;

	public static void main(String[] args) {
		String outdir = "C:/temp/";
		testCDXParsers(outdir);
		testInChIParsers(outdir);
		testSmilesParser(outdir);

		SwingUtilities.invokeLater(()->{showDialogTest(args);});
	}

	private static void testSmilesParser(String outdir) {
		String smiles = "N12C(=O)OC(C)(C)C.C1CC[C@H]2C(=O)[N](CCC)C1=CC=CC2=CC=CC=C12";
		String fileout = "tsmiles.png";
		System.out.println(smiles + " => " + fileout);
		StereoMolecule mol = new SmilesParser().parseMolecule(smiles);
		JStructureView view = JStructureView.getStandardView(mol);
		writeViewImage(view, fileout, outdir);
		view.showInFrame(smiles, nextLoc());
	}

	private static void testInChIParsers(String outdir) {

		StereoMolecule mol;
		JStructureView view;
		String fileout;

		fileout = "tinchi.png";
		String inchi = "InChI=1S/C4H11N/c1-3-5-4-2/h5H,3-4H2,1-2H3";
		inchi = "InChI=1S/C17H19NO3/c1-18-7-6-17-10-3-5-13(20)16(17)21-15-12(19)4-2-9(14(15)17)8-11(10)18/h2-5,10-11,13,16,19-20H,6-8H2,1H3/t10-,11+,13-,16-,17-/m0/s1";
		// inchi = "InChI=1S/C4H10O/c1-3-4(2)5/h4-5H,3H2,1-2H3/t4-/m0/s1";
		System.out.println(inchi + " => " + fileout);
		mol = new StereoMolecule();
		if (new InChIParser().parse(mol, inchi)) {
			view = JStructureView.getStandardView(mol);
			writeViewImage(view, fileout, outdir);
			view.showInFrame(inchi, nextLoc());
			writeMolFile(mol, fileout + ".mol", outdir);
		}

		fileout = "tinchipc.png";
		inchi = "PubChem:" + inchi;
		System.out.println(inchi + " => " + fileout);
		mol = new StereoMolecule();
		if (new InChIParser().parse(mol, inchi)) {
			view = JStructureView.getStandardView(mol);
			writeViewImage(view, fileout, outdir);
			view.showInFrame(inchi, nextLoc());
		}

		fileout = "tinchikey.png";
		String inchiKey = "BQJCRHHNABKAKU-KBQPJGBKSA-N";
		System.out.println(inchiKey + " => " + fileout);
		mol = new StereoMolecule();
		if (new InChIKeyParser().parse(mol, inchiKey)) {
			view = JStructureView.getStandardView(mol);
			writeViewImage(view, fileout, outdir);
			view.showInFrame(inchiKey, nextLoc());
		}
	}

	private static void testCDXParsers(String outdir) {
		StereoMolecule mol;
		JStructureView view;
		String filein, fileout;

		filein = "3aa.cdxml";
		fileout = "t3aa.png";
		testCDXML(filein, fileout, outdir);

		filein = "t.cdxml";
		fileout = "tcdxml.png";
		testCDXML(filein, fileout, outdir);
		
		filein = "t.cdx";
		fileout = "tcdx.png";
		testCDX(filein, fileout, outdir);

		// the CDX byte[] reader can also be used for cdxml data
		filein = "tout.cdxml";
		fileout = "tout.png";
		testCDX(filein, fileout, outdir);

		filein = "t-acs.cdxml";
		fileout = "t-acs-cdxml.png";
		testCDXML(filein, fileout, outdir);

		filein = "t-acs.cdx";
		fileout = "t-acs-cdx.png";
		testCDX(filein, fileout, outdir);
	}

	private static void testCDXML(String filein, String fileout, String dir) {
		String cdxml;
		System.out.println(filein + " => " + fileout);
		// one can also use tye parseFile method with CDX or CDXML
		//		mol = CDXParser.parseFile(filein);
		StereoMolecule mol = new StereoMolecule();
		cdxml = getString(filein, dir);
		if (new CDXParser().parse(mol, cdxml)) {
			JStructureView view = JStructureView.getStandardView(mol);
			view.showInFrame(filein, nextLoc());
			writeViewImage(view, fileout, dir);
		}

	}

	private static void testCDX(String filein, String fileout, String dir) {
		System.out.println(filein + " => " + fileout);
		StereoMolecule mol = new StereoMolecule();
		byte[] cdx = getBytes(filein, dir);
		if (new CDXParser().parse(mol, cdx)) {
			JStructureView view = JStructureView.getStandardView(mol);
			view.showInFrame(filein, nextLoc());
			writeViewImage(view, fileout, dir);
			// showEditFrame(mol);
			writeMolFile(mol, filein + ".mol", dir);
		}
	}

	private static void writeViewImage(JStructureView view, String fileout, String outdir) {
		writeImage(view.getSizedImage(), fileout, outdir);
	}

	private static byte[] getBytes(String filein, String dir) {
		try {
			if (dir == null) {
				return ParserUtils.getResourceBytes(OCLSwingTest.class, filein);
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

	static int frameY = 0;
	private static Point nextLoc() {
		if (nFrame % 5 == 0) {
			frameY += 200;
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
						"" + "N12C(=O)OC(C)(C)C.C1CC[C@H]2C(=O)[NH]C1=CC=CC2=CC=CC=C12"
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

		SwingDialog d = new SwingDialog(null, "testing2");

		d.setSize(new Dimension(500, 500));
		d.add(p);
		d.setVisible(true);
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