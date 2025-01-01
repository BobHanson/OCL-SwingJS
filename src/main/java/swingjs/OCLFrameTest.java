package swingjs;

import java.awt.Dimension;
import java.awt.Frame;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import com.actelion.research.chem.AbstractDepictor;
import com.actelion.research.chem.IsomericSmilesCreator;
import com.actelion.research.chem.Molecule;
import com.actelion.research.chem.SmilesParser;
import com.actelion.research.chem.StereoMolecule;
import com.actelion.research.chem.coords.CoordinateInventor;
import com.actelion.research.gui.FileHelper;
import com.actelion.research.gui.editor.SwingEditorArea;
import com.actelion.research.gui.editor.SwingEditorPanel;
import com.actelion.research.gui.swing.SwingDialog;

public class OCLFrameTest {

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

	public static void main(String[] args) {
		showDialogTest(args);
	}


	@SuppressWarnings("unused")
	private static void showDialogTest(String[] args) {

		ArrayList<StereoMolecule> list = new FileHelper(null).readStructuresFromFile(false);
		StereoMolecule mol;
		String title;
		title = "mol";
		if (list == null || list.isEmpty()) {
			String smiles = JOptionPane.showInputDialog("enter a SMILES",
					"CN1CC[C@@]23[C@H]4OC5=C(O)C=CC(=C25)C[C@@H]1[C@@H]3C=C[C@@H]4O"

					
					
//					"" + "N12C(=O)OC(C)(C)C.C1CC[C@H]2C(=O)[NH]C1=CC=CC2=CC=CC=C12"
//		+ "CC(CCCC(C1CCC2C1(C)CCC1C2CC=C2C1(C)CCC(C2)O)C)C"
//					+ "CCN.[b]12-c3ccccc3.o1[b]4-c5ccccc5.o4[b]6-c7ccccc7.o62"


			
			);
			title = smiles;
			mol = new SmilesParser().parseMolecule(smiles); // Nc1cc(OCCO)cc(N)c1
		} else {
			mol = (StereoMolecule) list.get(0);
		}
		mol.ensureHelperArrays(Molecule.cHelperParities);
		new CoordinateInventor(0).invent(mol);
		showEditFrame(mol);
	}

	private static void showEditFrame(StereoMolecule mol) {
		
		SwingEditorPanel p = new SwingEditorPanel(mol);
		p.setSize(new Dimension(500, 500));

		int mode = 0;
		//AbstractDepictor.cDModeSuppressCIPParity | AbstractDepictor.cDModeSuppressESR
			//	| AbstractDepictor.cDModeSuppressChiralText;

		SwingEditorArea area = (SwingEditorArea) p.getComponent(0);
		area.getGenericDrawArea().setDisplayMode(mode);

		SwingDialog d = new SwingDialog((Frame) null, "OCLFrameTest");

		d.setSize(new Dimension(500, 500));
		d.add(p);
		d.setVisible(true);
	}

}