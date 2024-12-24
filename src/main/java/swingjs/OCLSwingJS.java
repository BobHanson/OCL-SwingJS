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
import javax.swing.WindowConstants;

import com.actelion.research.chem.AbstractDepictor;
import com.actelion.research.chem.MolfileCreator;
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

public class OCLSwingJS {


	public static void main(String[] args) {
		showDialogTest(null);
		/**
		 * @j2sNative
		 */
		{
		System.exit(0);
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
		d.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	}

	private static void testSmilesParser(String outdir) {
		String smiles = "N12C(=O)OC(C)(C)C.C1CC[C@H]2C(=O)[N](CCC)C1=CC=CC2=CC=CC=C12";
		String fileout = outdir + "tsmiles.png";
		System.out.println(smiles + " => " + fileout);
		StereoMolecule mol = new SmilesParser().parseMolecule(smiles);
		JStructureView view = JStructureView.getStandardView(mol);
		BufferedImage bi = view.getSizedImage();
		writeImage(bi, fileout);
		view.showInFrame(smiles, nextLoc());
	}

	private static void testInChIParsers(String outdir) {

		StereoMolecule mol;
		JStructureView view;
		String fileout;
		BufferedImage bi;

		fileout = outdir + "tinchi.png";
		String inchi = "InChI=1S/C4H11N/c1-3-5-4-2/h5H,3-4H2,1-2H3";

		inchi = "InChI=1S/C17H19NO3/c1-18-7-6-17-10-3-5-13(20)16(17)21-15-12(19)4-2-9(14(15)17)8-11(10)18/h2-5,10-11,13,16,19-20H,6-8H2,1H3/t10-,11+,13-,16-,17-/m0/s1";

		// inchi = "InChI=1S/C4H10O/c1-3-4(2)5/h4-5H,3H2,1-2H3/t4-/m0/s1";
		// inchi =
		// "PubChem:InChI=1S/C6H12O/c1-3-5-6(7)4-2/h3,5-7H,4H2,1-2H3/b5-3+/t6-/m1/s1";
		System.out.println(inchi + " => " + fileout);
		mol = new StereoMolecule();
		// atom parity 0x4005002 is stereocenter R EVEN
		// bond parity 0x11 is stereobond E ODD
		// bond type 0x101 is single up
//		new InChIParser().parse(mol, inchi);
//		view = JStructureView.getStandardView(mol);
//		bi = view.getSizedImage();
//		writeImage(bi, fileout);
//		view.showInFrame(inchi, nextLoc());
//		writeMolFile(mol, fileout + ".mol");

		inchi = "PubChem:" + inchi;
		System.out.println(inchi + " => " + fileout);
		mol = new StereoMolecule();
		// atom parity 0x4005002 is stereocenter R EVEN
		// bond parity 0x11 is stereobond E ODD
		// bond type 0x101 is single up
		new InChIParser().parse(mol, inchi);
		view = JStructureView.getStandardView(mol);
		bi = view.getSizedImage();
		writeImage(bi, fileout);
		view.showInFrame(inchi, nextLoc());

		fileout = outdir + "tinchikey.png";
		String inchiKey = "BQJCRHHNABKAKU-KBQPJGBKSA-N";
		System.out.println(inchiKey + " => " + fileout);
		mol = new StereoMolecule();
		new InChIKeyParser().parse(mol, inchiKey);
		view = JStructureView.getStandardView(mol);
		bi = view.getSizedImage();
		writeImage(bi, inchiKey);
		view.showInFrame(inchiKey, nextLoc());

	}

	private static void testCDXParsers(String outdir) {
		StereoMolecule mol;
		JStructureView view;
		String filein, fileout;
		
		filein = outdir + "3aa.cdxml";
		fileout = outdir + "t3aa.png";
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
		

		filein = outdir + "t.cdxml";
		fileout = outdir + "tcdxml.png";
		System.out.println(filein + " => " + fileout);
		mol = CDXParser.parseFile(filein);
		if (mol != null) {
			view = JStructureView.getStandardView(mol);
			view.showInFrame(filein, nextLoc());
			BufferedImage bi = view.getSizedImage();
			writeImage(bi, fileout);
		}

		filein = outdir + "t.cdx";
		fileout = outdir + "tcdx.png";
		System.out.println(filein + " => " + fileout);
		mol = CDXParser.parseFile(filein);
		if (mol != null) {
			view = JStructureView.getStandardView(mol);
			view.showInFrame(filein, nextLoc());
			BufferedImage bi = view.getSizedImage();
			writeImage(bi, fileout);
		}

		filein = outdir + "tout.cdxml";
		fileout = outdir + "tout.png";
		System.out.println(filein + " => " + fileout);
		mol = CDXParser.parseFile(filein);
		if (mol != null) {
			view = JStructureView.getStandardView(mol);
			view.showInFrame(filein, nextLoc());
			BufferedImage bi = view.getSizedImage();
			writeImage(bi, fileout);
		}
		filein = outdir + "t-acs.cdxml";
		fileout = outdir + "t-acs-cdxml.png";
		System.out.println(filein + " => " + fileout);
		mol = CDXParser.parseFile(filein);
		if (mol != null) {
			view = JStructureView.getStandardView(mol);
			view.showInFrame(filein, nextLoc());
			BufferedImage bi = view.getSizedImage();
			writeImage(bi, fileout);
		}

		filein = outdir + "t-acs.cdx";
		fileout = outdir + "t-acs-cdx.png";
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

	private static int nFrame = 0;
	
	private static Point nextLoc() {
		return new Point(150 * nFrame++, 0);
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

}