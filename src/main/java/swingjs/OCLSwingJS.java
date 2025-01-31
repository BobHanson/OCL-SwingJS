package swingjs;

import java.awt.Dimension;

import javax.swing.JOptionPane;
import javax.swing.WindowConstants;

import com.actelion.research.chem.IsomericSmilesCreator;
import com.actelion.research.chem.SmilesParser;
import com.actelion.research.chem.StereoMolecule;
import com.actelion.research.chem.inchi.InChIOCL;
import com.actelion.research.gui.FileHelper;
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

	private static SwingDialog dialog;
	@SuppressWarnings("unused")
	private static void showDialogTest(String[] args) {

		FileHelper helper = new FileHelper(null);
		helper.readStructuresFromFileAsync(false, (list) -> {
			StereoMolecule mol;
			String title = null;
			if (list == null || list.isEmpty()) {
				String smiles = JOptionPane.showInputDialog("enter a SMILES",
						//"F[CH@](Cl)(Br)"
						"C(F)(Cl)=[C@]=CBr"						
						//"N12C(=O)OC(C)(C)C.C1CC[C@H]2C(=O)[NH]C1=CC=CC2=CC=CC=C12"
				);
				title = smiles;
				mol = new SmilesParser().parseMolecule(smiles); // Nc1cc(OCCO)cc(N)c1
			} else {
				title = helper.getFileOpened().getName();
				mol = (StereoMolecule) list.get(0);
			}
			if (mol == null)
				System.exit(1);
			String smiles = new IsomericSmilesCreator(mol).getSmiles();
			System.out.println(smiles);
			String options = null;
			String inchi = InChIOCL.getInChI(mol, options);
			System.out.println(inchi);
			dialog = showEditFrame(mol, title);
		});

	}

	private static SwingDialog showEditFrame(StereoMolecule mol, String title) {
		SwingEditorPanel p = new SwingEditorPanel(mol);
		p.setSize(new Dimension(500, 500));

		int mode = 0;
//				AbstractDepictor.cDModeSuppressCIPParity 
//				//| AbstractDepictor.cDModeSuppressESR
//				| AbstractDepictor.cDModeSuppressChiralText;

		SwingEditorArea area = (SwingEditorArea) p.getComponent(0);
		area.getGenericDrawArea().setDisplayMode(mode);

		SwingDialog d = new SwingDialog(null, title);
		
		d.setSize(new Dimension(500, 500));
		d.add(p);
		d.setVisible(true);
		d.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		return d;
	}


}