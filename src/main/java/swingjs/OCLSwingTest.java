package swingjs;

import java.awt.Dimension;

import javax.swing.JFrame;

import com.actelion.research.chem.AbstractDepictor;
import com.actelion.research.chem.Molecule;
import com.actelion.research.chem.SVGDepictor;
import com.actelion.research.chem.SmilesParser;
import com.actelion.research.chem.StereoMolecule;
import com.actelion.research.gui.editor.SwingEditorArea;
import com.actelion.research.gui.editor.SwingEditorPanel;
import com.actelion.research.gui.generic.GenericActionEvent;
import com.actelion.research.gui.generic.GenericEventListener;
import com.actelion.research.gui.generic.GenericRectangle;
import com.actelion.research.gui.hidpi.HiDPIHelper;
import com.actelion.research.gui.swing.SwingDialog;


class OCLSwingTest {
	
	public static void main(String[] args) {
		new OCLSwingTest().showDialog(args);
	}

	private void showDialog(String[] args) {
				
		//Molecule.setDefaultAverageBondLength(HiDPIHelper.scale(12));
		StereoMolecule mol = new SmilesParser().parseMolecule("Nc1cc(OCCO)cc(N)c1");
//		mol.setFragment(true); // otherwise NH2 will be just "N"
		SwingEditorPanel p = new SwingEditorPanel(mol);
		p.setSize(new Dimension(500,500));
		
		int mode = AbstractDepictor.cDModeSuppressCIPParity|AbstractDepictor.cDModeSuppressESR|AbstractDepictor.cDModeSuppressChiralText;
		
		SwingEditorArea area = (SwingEditorArea) p.getComponent(0);
		area.getGenericDrawArea().setDisplayMode(mode);

		SwingDialog d = new SwingDialog((JFrame) null, "testing2");
		
		d.setEventConsumer(new GenericEventListener<GenericActionEvent>() {

			@Override
			public void eventHappened(GenericActionEvent e) {
				// this will never be called for this type of dialog, though.
				System.out.println("Dialog reports " + e.getWhat());
			}
			
		});
		
// test for SVG output		

		d.setSize(new Dimension(500,500));
		d.add(p);
		d.setVisible(true);
		SVGDepictor svgd = new SVGDepictor(mol, mode, "");
		svgd.validateView(null, new GenericRectangle(0, 0, 300, 200),
				AbstractDepictor.cModeInflateToHighResAVBL);
		svgd.paint(null);
		svgd.setLegacyMode(false);
		System.out.println(svgd.toString());

		
	}
	
}