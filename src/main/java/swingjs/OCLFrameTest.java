package swingjs;

import java.awt.Dimension;
import java.awt.Frame;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.actelion.research.chem.Molecule;
import com.actelion.research.chem.SmilesParser;
import com.actelion.research.chem.StereoMolecule;
import com.actelion.research.chem.coords.CoordinateInventor;
import com.actelion.research.gui.FileHelper;
import com.actelion.research.gui.editor.EditorEvent;
import com.actelion.research.gui.editor.GenericEditorArea;
import com.actelion.research.gui.editor.SwingEditorArea;
import com.actelion.research.gui.editor.SwingEditorPanel;
import com.actelion.research.gui.generic.GenericEventListener;
import com.actelion.research.gui.swing.SwingDialog;

public class OCLFrameTest {


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
		if (mol == null)
			System.exit(0);
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
		GenericEditorArea drawing = area.getGenericDrawArea();
		drawing.setDisplayMode(mode);

		OCL.initInchi(null);
		
		drawing.addDrawAreaListener(new GenericEventListener<EditorEvent>() {

			@Override
			public void eventHappened(EditorEvent e) {
				switch (e.getWhat()) {
				case EditorEvent.WHAT_MOLECULE_CHANGED:
					StereoMolecule mol = drawing.getMolecule();
					String inchi = OCL.getInChIFromOCLMolecule(mol, "FixedH");
					System.out.println(inchi);
					break;
				case EditorEvent.WHAT_SELECTION_CHANGED:
				case EditorEvent.WHAT_HILITE_ATOM_CHANGED:
				case EditorEvent.WHAT_HILITE_BOND_CHANGED:
					break;
				}
			}
			
		});
	//	SwingDialog d = new SwingDialog((Frame) null, "OCLFrameTest");
		JFrame d = new JFrame("OCLFrame");
		d.setSize(new Dimension(500, 500));
		d.add(p);
		d.setVisible(true);
	}

}