package swingjs;

import java.awt.Dimension;

import javax.swing.JFrame;

import com.actelion.research.chem.AbstractDepictor;
import com.actelion.research.chem.IDCodeParser;
import com.actelion.research.chem.SVGDepictor;
import com.actelion.research.chem.SmilesParser;
import com.actelion.research.chem.StereoMolecule;
import com.actelion.research.gui.FileHelper;
import com.actelion.research.gui.editor.SwingEditorArea;
import com.actelion.research.gui.editor.SwingEditorPanel;
import com.actelion.research.gui.generic.GenericRectangle;
import com.actelion.research.gui.swing.SwingDialog;


class OCLSwingTest {

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
		System.out.println(new OclCodeCheck().checkOCL("dcMD@ItARfUV^V``h`@@"));
		System.out.println(new OclCodeCheck().checkOCL("daT@`@\\DjfjZn[jjjkJcKhGP`phxdtl|wY@xD`uyo]{|lKUfp"));
		System.out.println(new OclCodeCheck().checkOCL("fc" + (char) 0x7F + "A@@@YEDeLhedihdXleJ\\eiwhyjijjjjjjjj`@@"));
		System.out.println(new OclCodeCheck().checkOCL("g3\0A;ADLA;MD@ItARfUV^V``h`@@"));
		String[] aminoAcidsLabeled = ("gGX`BDdwMULPGzILwXM[jD\n" + 
		"dctd@BE]ADf{UYjjihp`GzBfMvCS]Plw^OMtbK]hrwUj}tRfwnbXp\n" + 
		"diEL@BDDyInvZjZL`OtiL[lFfzaYn|^{iFLO]Hi`\n" + 
		"diFB@BANEInvZjZLHA~eIc]`twTKMwcw]Hqa{iEL\n" + 
		"gNxhMV@aI[jihj@SHF{qc]PinpP\n" + 
		"defB@BAAeInufjihr@QdqnpZ[jEf{qyndQ{mFMO]hi`\n" + 
		"deeL@BdDEInufjihp`GzLfMvCS]Plw^OMtbO]hqi{mEL\n" + 
		"gJX`BDdvu@OtbYnpP\n" + 
		"dmwD@ByPQInvVUZjejL`OtyL[lFfzaYn|^{iFLO]Hii{mFLo]hi`\n" + 
		"diFD@BADf{ejjdrU@_iRXwXMMuBw]xqn{oELO]Hq`\n" + 
		"diFD@BADf{Yjjhr@RdqnpZ[jEf{q{ndTp}tcF@\n" + 
		"deeD@BdDR[mUjjjL`OtYL[lFfzaYn|^[iDV{QenkP\n" + 
		"diFD`JxPBDivzjihI@RdAndX[oEF{QqnhR[lD\n" + 
		"dcND@BADf{YU]Zj@@cHC}ASF{AinhV[oGnzQSCwRLZ^{QSKwZL[Vzm@\n" + 
		"daFD@BADfyVyjjhr@PdqnpZ[jEfzQyn|P\n" + 
		"gNy`BDtf{ZjfHC}Lf[lFmuBv{q@\n" + 
		"dazL@BAFR[nZjdrT`_hRXwXMMuBw]xqn{oEL\n" + 
		"foAP`@BZ@aInvYWejsfjiB@bFB@OttfF{AhwTKF{qywRJXW]Hqi]vbfUwZN[W]hqc]uZfmwUnYw]Di`\n" + 
		"dknL@BACR[me]]Zj@BHr@RTqnpZ[jEf{q{ndTp}tcFgntTr}vcFunkS[hd\n" + 
		"dazD@BADf{fjjL`OtIL[lFfza[n|Tw]wcF@s").split("\n");
		for (int i = 0; i < aminoAcidsLabeled.length; i++)
		System.out.println(new OclCodeCheck().checkOCL(aminoAcidsLabeled[i]));		
	}
	

	public static void main(String[] args) {
		new OCLSwingTest().showDialogTest(args);
	}

	private void showDialogTest(String[] args) {

		// Molecule.setDefaultAverageBondLength(HiDPIHelper.scale(12));

		new FileHelper(null).readStructuresFromFileAsync(false, (list) -> {
			StereoMolecule mol = (list == null || list.isEmpty()
					? new SmilesParser().parseMolecule("CC(CCCC(C1CCC2C1(C)CCC1C2CC=C2C1(C)CCC(C2)O)C)C") // Nc1cc(OCCO)cc(N)c1
					: (StereoMolecule) list.get(0));

 			System.out.println("nonisomeric cholesterol is " + mol.getIDCode());
			SwingEditorPanel p = new SwingEditorPanel(mol);
			p.setSize(new Dimension(500, 500));

			int mode = AbstractDepictor.cDModeSuppressCIPParity | AbstractDepictor.cDModeSuppressESR
					| AbstractDepictor.cDModeSuppressChiralText;

			SwingEditorArea area = (SwingEditorArea) p.getComponent(0);
			area.getGenericDrawArea().setDisplayMode(mode);

			SwingDialog d = new SwingDialog((JFrame) null, "testing2");

			// test for SVG output

			d.setSize(new Dimension(500, 500));
			d.add(p);
			d.setVisible(true);

			SVGDepictor svgd = new SVGDepictor(mol, mode, "");
			svgd.validateView(null, new GenericRectangle(0, 0, 300, 200), AbstractDepictor.cModeInflateToHighResAVBL);
			svgd.paint(null);
			svgd.setLegacyMode(false);
			System.out.println(svgd.toString());

		});

	}
	
}