package swingjs;

import java.awt.Dimension;

import javax.swing.JFrame;

import com.actelion.research.chem.AbstractDepictor;
import com.actelion.research.chem.SVGDepictor;
import com.actelion.research.chem.SmilesParser;
import com.actelion.research.chem.StereoMolecule;
import com.actelion.research.gui.FileHelper;
import com.actelion.research.gui.editor.SwingEditorArea;
import com.actelion.research.gui.editor.SwingEditorPanel;
import com.actelion.research.gui.generic.GenericRectangle;
import com.actelion.research.gui.swing.SwingDialog;


class OCLSwingTest {
	
	public static void main(String[] args) {
		new OCLSwingTest().showDialogTest(args);
	}

	private void showDialogTest(String[] args) {

		// Molecule.setDefaultAverageBondLength(HiDPIHelper.scale(12));

		new FileHelper(null).readStructuresFromFileAsync(false, (list) -> {
			StereoMolecule mol = (list == null || list.isEmpty() 
					? new SmilesParser().parseMolecule("Nc1cc(OCCO)cc(N)c1")
			: (StereoMolecule) list.get(0));
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