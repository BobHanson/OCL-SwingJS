package swingjs;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

import com.actelion.research.chem.AbstractDepictor;
import com.actelion.research.chem.MolfileCreator;
import com.actelion.research.chem.MolfileV3Creator;
import com.actelion.research.chem.SmilesParser;
import com.actelion.research.chem.StereoMolecule;
import com.actelion.research.chem.moreparsers.CDXParser;
import com.actelion.research.chem.moreparsers.InChIKeyParser;
import com.actelion.research.chem.moreparsers.InChIParser;
import com.actelion.research.gui.FileHelper;
import com.actelion.research.gui.JStructureView;
import com.actelion.research.gui.editor.SwingEditorArea;
import com.actelion.research.gui.editor.SwingEditorPanel;
import com.actelion.research.gui.swing.SwingDialog;

public class OCLSwingTest {

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

	public static int nFrame;

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

	public static void main(String[] args) {
		testCDXParsers();
//		testInChIParsers();
//		testSmilesParser();

//		showDialogTest(args);
	}

	private static void testSmilesParser() {
		String smiles = "N12C(=O)OC(C)(C)C.C1CC[C@H]2C(=O)[N](CCC)C1=CC=CC2=CC=CC=C12";
		String fileout = "C:/temp/tsmiles.png";
		System.out.println(smiles + " => " + fileout);
		StereoMolecule mol = new SmilesParser().parseMolecule(smiles);
		JStructureView view = JStructureView.getStandardView(mol);
		BufferedImage bi = view.getSizedImage();
		writeImage(bi, fileout);
		view.showInFrame(smiles, nextLoc());
	}

	private static void testInChIParsers() {

		StereoMolecule mol;
		JStructureView view;
		String fileout;

		fileout = "C:/temp/tinchikey.png";
		String inchiKey = "BQJCRHHNABKAKU-KBQPJGBKSA-N";
		System.out.println(inchiKey + " => " + fileout);
		mol = new StereoMolecule();
		new InChIKeyParser().parse(mol, inchiKey);
		view = JStructureView.getStandardView(mol);
		BufferedImage bi = view.getSizedImage();
		writeImage(bi, inchiKey);
		view.showInFrame(inchiKey, nextLoc());

		fileout = "C:/temp/tinchi.png";
		String inchi = "InChI=1S/C4H11N/c1-3-5-4-2/h5H,3-4H2,1-2H3";
		System.out.println(inchi + " => " + fileout);
		mol = new StereoMolecule();
		new InChIParser().parse(mol, inchi);
		view = JStructureView.getStandardView(mol);
		bi = view.getSizedImage();
		writeImage(bi, fileout);
		view.showInFrame(inchi, nextLoc());
	}

	private static void testCDXParsers() {

		StereoMolecule mol;
		JStructureView view;
		String filein, fileout;
		
		filein = "C:/temp/3aa.cdxml";
		fileout = "C:/temp/t3aa.png";
		System.out.println(filein + " => " + fileout);
		mol = CDXParser.parseFile(filein);
		if (mol != null) {
			view = JStructureView.getStandardView(mol);
			view.showInFrame(filein, nextLoc());
			BufferedImage bi = view.getSizedImage();
			writeImage(bi, fileout);
			showEditFrame(mol);
			writeMolFile(mol, filein + ".mol");
		}
		

		filein = "C:/temp/t.cdxml";
		fileout = "C:/temp/tcdxml.png";
		System.out.println(filein + " => " + fileout);
		mol = CDXParser.parseFile(filein);
		if (mol != null) {
			view = JStructureView.getStandardView(mol);
			view.showInFrame(filein, nextLoc());
			BufferedImage bi = view.getSizedImage();
			writeImage(bi, fileout);
		}

		filein = "C:/temp/t.cdx";
		fileout = "C:/temp/tcdx.png";
		System.out.println(filein + " => " + fileout);
		mol = CDXParser.parseFile(filein);
		if (mol != null) {
			view = JStructureView.getStandardView(mol);
			view.showInFrame(filein, nextLoc());
			BufferedImage bi = view.getSizedImage();
			writeImage(bi, fileout);
		}

		filein = "C:/temp/tout.cdxml";
		fileout = "C:/temp/tout.png";
		System.out.println(filein + " => " + fileout);
		mol = CDXParser.parseFile(filein);
		if (mol != null) {
			view = JStructureView.getStandardView(mol);
			view.showInFrame(filein, nextLoc());
			BufferedImage bi = view.getSizedImage();
			writeImage(bi, fileout);
		}
		filein = "C:/temp/t-acs.cdxml";
		fileout = "C:/temp/t-acs-cdxml.png";
		System.out.println(filein + " => " + fileout);
		mol = CDXParser.parseFile(filein);
		if (mol != null) {
			view = JStructureView.getStandardView(mol);
			view.showInFrame(filein, nextLoc());
			BufferedImage bi = view.getSizedImage();
			writeImage(bi, fileout);
		}

		filein = "C:/temp/t-acs.cdx";
		fileout = "C:/temp/t-acs-cdx.png";
		System.out.println(filein + " => " + fileout);
		mol = CDXParser.parseFile(filein);
		if (mol != null) {
			view = JStructureView.getStandardView(mol);
			view.showInFrame(filein, nextLoc());
			BufferedImage bi = view.getSizedImage();
			writeImage(bi, fileout);
		}

	}

	private static void writeMolFile(StereoMolecule mol, String filename) {
		try {
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename), "UTF-8"));
			new MolfileCreator(mol).writeMolfile(bw);
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static Point nextLoc() {
		return new Point(100 * nFrame++, 0);
	}

	private static void writeImage(BufferedImage bi, String fname) {
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

}