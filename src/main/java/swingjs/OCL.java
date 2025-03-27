package swingjs;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Window;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Base64;
import java.util.Locale;
import java.util.function.Consumer;

import javax.imageio.ImageIO;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JRootPane;

import com.actelion.research.chem.AbstractDepictor;
import com.actelion.research.chem.IsomericSmilesCreator;
import com.actelion.research.chem.MolfileCreator;
import com.actelion.research.chem.MolfileParser;
import com.actelion.research.chem.SmilesParser;
import com.actelion.research.chem.StereoMolecule;
import com.actelion.research.chem.inchi.InChIOCL;
import com.actelion.research.gui.JStructureView;
import com.actelion.research.gui.editor.EditorEvent;
import com.actelion.research.gui.editor.GenericEditorArea;
import com.actelion.research.gui.editor.SwingEditorArea;
import com.actelion.research.gui.editor.SwingEditorPanel;
import com.actelion.research.gui.generic.GenericEventListener;
import com.actelion.research.gui.swing.SwingDialog;
import com.sun.jna.Pointer;

import io.github.dan2097.jnainchi.InchiAPI;
import io.github.dan2097.jnainchi.InchiInput;
import io.github.dan2097.jnainchi.inchi.InchiLibrary;

/**
 * @j2sExport
 * 
 * @author hanso
 *
 */
public class OCL {

	/**
	 * 
	 * 
	 * @param inchi
	 * @return
	 */
	public static String get2DMolFromInChI(String inchi) {
		try {
			return get2DMolFromOCLMolecule(getOCLMoleculeFromInChI(inchi, "fixamides"));
		} catch (Throwable e) {
			return null;
		}
	}

	/**
	 * 
	 * @param mol
	 * @return
	 */
	public static String get2DMolFromOCLMolecule(StereoMolecule mol) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			MolfileCreator creator = new MolfileCreator(mol);
			OutputStreamWriter writer = new OutputStreamWriter(bos);
			creator.writeMolfile(writer);
			writer.close();
			return new String(bos.toByteArray());
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 
	 */
	public static String get2DMolFromSmiles(String Smiles) {
		return get2DMolFromOCLMolecule(new SmilesParser().parseMolecule(Smiles));
	}

	/**
	 * 
	 * 
	 * @param inchi
	 * @return
	 */
	public static String getDataURIFromInChI(String inchi) {
		return getDataURIFromOCLMolecule(getOCLMoleculeFromInChI(inchi, "fixamides"));
	}

	/**
	 * 
	 * 
	 * @param mol
	 * @param os
	 * @return
	 */
	public static String getDataURIFromOCLMolecule(StereoMolecule mol) {
		JStructureView view = JStructureView.getStandardView(
				// really want this to be no chiral H unless bicyclic
				JStructureView.defaultChemistsMode, mol);
		try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
			ImageIO.write(view.getSizedImage(), "png", os);
			byte[] bytes = Base64.getEncoder().encode(os.toByteArray());
			return "data:image/png;base64," + new String(bytes);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 
	 */
	public static String getInChIFromInChI(String inchi, String options) {
		return InChIOCL.getInChIFromInChI(inchi, options);
	}

	public static String getInChIFromInchiInput(InchiInput inchiInput, String options) {
		return InchiAPI.getInChIFromInchiInput(inchiInput, options);
	}
	
	/**
	 * 
	 */
	public static String getInChIFromMOL(String molData, String options) {
		return InChIOCL.getInChI(molData, options);
	}

	/**
	 * 
	 */
	public static String getInChIFromOCLMolecule(StereoMolecule mol, String options) {
		return InChIOCL.getInChI(mol, options);
	}

	/**
	 * 
	 */
	public static String getInChIFromSmiles(String smiles, String options) {
		return InChIOCL.getInChIFromSmiles(smiles, options);
	}

	/**
	 * 
	 */
	public static InchiInput getInchiInputFromInChI(String inchi, String moreOptions) {
		return InChIOCL.getInchiInputFromInChI(inchi, moreOptions);
	}

	/**
	 * 
	 * 
	 * @return InchiInput
	 */
	public static InchiInput getInchiInputFromMoleculeHandle(Pointer hStatus, Pointer hMolecule, String moreOptions) {
		return InchiAPI.getInchiInputFromMoleculeHandle(hStatus, hMolecule, moreOptions);
	}

	/**
	 * 
	 */
	public static InchiInput getInchiInputFromOCLMolecule(StereoMolecule mol) {
		return InChIOCL.getInchiInputFromOCLMolecule(mol);
	}

	/**
	 * 
	 */
	public static String getInChIKey(StereoMolecule mol, String options) {
		String inchi = getInChIFromOCLMolecule(mol, options);
		return InchiAPI.getInChIKeyFromInChI(inchi);
	}

	/**
	 * 
	 */
	public static String getInChIModelJSON(String inchi) {
		return InChIOCL.getInChIModelJSON(inchi);
	}

	/**
	 * 
	 */
	public static String getInChIVersion(boolean fullDescription) {
		return InChIOCL.getInChIVersion(fullDescription);
	}

	/**
	 * 
	 */
	public static String getJSONFromInchiInput(InchiInput input) {
		return InchiAPI.getJSONFromInchiInput(input);
	}

	/**
	 * 
	 * 
	 * @param inchi
	 * @param string 
	 * @return
	 */
	public static StereoMolecule getOCLMoleculeFromInChI(String inchi, String moreOptions) {
		try {
			StereoMolecule mol = new StereoMolecule();
			InchiInput input = getInchiInputFromInChI(inchi, moreOptions);
			InChIOCL.getOCLMoleculeFromInchiInput(input, mol);
			return mol;
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Particularly for JavaScript, this method allows passing to
	 * 
	 *  
	 * @param input an InchiInput object
	 * @return a OCL molecule as in StereoMolecule
	 */
	public static StereoMolecule getOCLMoleculeFromInchiInput(InchiInput input) {
		try {
			StereoMolecule mol = new StereoMolecule();
			InChIOCL.getOCLMoleculeFromInchiInput(input, mol);
			return mol;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 
	 */
	public static StereoMolecule getOCLMoleculeFromMOL(String moldata) {
		StereoMolecule mol = new StereoMolecule();
		if (!new MolfileParser().parse(mol, moldata)) {
			return null;
		}
		return mol;
	}

	/**
	 * 
	 */
	public static StereoMolecule getOCLMoleculeFromSmiles(String Smiles) {
		return new SmilesParser().parseMolecule(Smiles);
	}

	/**
	 * 
	 */
	public static String getSmilesFromInChI(String inchi) {
		try {
			return InChIOCL.getSmilesFromInChI(inchi);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * 
	 */
	public static String getSmilesFromOCLMolecule(StereoMolecule mol) {
		return IsomericSmilesCreator.createSmiles(mol);
	}

	/**
	 * 
	 */
	public static void initInchi(Runnable r) {
		InChIOCL.init(r);
	}

	public final static void main(String[] args) {
		System.out.println(InchiLibrary.class.getName());
		Locale.setDefault(Locale.ROOT);
	}

	/**
	 * 
	 * 
	 * @param mol
	 * @return mol
	 */
	public static StereoMolecule suppressHydrogens(StereoMolecule mol) {
		return mol;// not necessary?
	}

	/**
	 * Get a modal dialog of the given width and height with the given title.
	 * In Java, the thread will wait for the dialog to close; in JavaScript, the 
	 * thread will continue, but the reset of the page will be grayed out.
	 * 
	 * @param title   null here will produce a frame instead of a dialog
	 * @param width
	 * @param height
	 * @param mode                   See {@link AbstractDepictor}; -1 for default
	 *                               with AbstractDepictor.cDModeSuppressCIPParity |
	 *                               AbstractDepictor.cDModeSuppressESR |
	 *                               AbstractDepictor.cDModeSuppressChiralText;
	 * 
	 * @param drawingChangedListener anything that has a method accept(Object), where this Object will be a GenericEditorArea
	 * @return
	 */
	public static Window getModalDialog(String title, int width, int height, int mode, Consumer<Object> drawingChangedListener) {
		return getWindow(null, title, width, height, mode, drawingChangedListener);
	}
	
	public static Window getFrame(String name, int width, int height, int mode, Consumer<Object> drawingChangedListener) {
		return getWindow(name, null, width, height, mode, drawingChangedListener);		
	}
	
	private static Window getWindow(String name, String title, int width, int height, int mode, Consumer<Object> drawingChangedListener) {
		if (width == 0) {
			width = 500;
			height = 500;
		}
		if (mode <= 0)
			mode = AbstractDepictor.cDModeSuppressCIPParity 
					| AbstractDepictor.cDModeSuppressESR
					| AbstractDepictor.cDModeSuppressChiralText;

		SwingEditorPanel p = new SwingEditorPanel(null, mode);
		p.setSize(new Dimension(width, height));
		p.setPreferredSize(new Dimension(width, height));
		SwingEditorArea area = (SwingEditorArea) p.getComponent(0);
		GenericEditorArea drawing = area.getGenericDrawArea();
		drawing.setDisplayMode(mode);
		drawing.addDrawAreaListener(new GenericEventListener<EditorEvent>() {

			@Override
			public void eventHappened(EditorEvent e) {
				switch (e.getWhat()) {
				case EditorEvent.WHAT_MOLECULE_CHANGED:
					if (drawingChangedListener != null)
						drawingChangedListener.accept(drawing);
					break;
				case EditorEvent.WHAT_SELECTION_CHANGED:
				case EditorEvent.WHAT_HILITE_ATOM_CHANGED:
				case EditorEvent.WHAT_HILITE_BOND_CHANGED:
					break;
				}
			}
		});

		if (title == null) {
			// embed
			JFrame frame = new JFrame(name);
			frame.setName(name);
			frame.add(p);
			frame.pack();
			frame.setVisible(true);
			return frame;
		} else {
			SwingDialog d = new SwingDialog((Frame) null, title);
			d.setSize(new Dimension(width, height));
			d.add(p);
			d.setVisible(true);
			return d;
		}
	}

	public static GenericEditorArea getGenericDrawArea(Window w) {
		Container cpane = (w instanceof JFrame ? ((JFrame) w).getContentPane() : ((JDialog) w).getContentPane());
		for (int i = cpane.getComponentCount(); --i >= 0;) {
			Component c = cpane.getComponent(i);
			if (c instanceof SwingEditorPanel) {
				return ((SwingEditorArea) ((SwingEditorPanel) c).getComponent(0)).getGenericDrawArea();
			}
		}
		return null;
	}
	
	public static void setMolecule(GenericEditorArea drawing, StereoMolecule mol) {
		drawing.setMolecule(mol);
	}
	
	public static String getCallCount() {
			return "" + InchiAPI.getCallCount();
	}

}