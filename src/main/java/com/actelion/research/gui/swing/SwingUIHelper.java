package com.actelion.research.gui.swing;

import java.awt.Dialog;
import java.awt.Window;
import java.io.File;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.text.html.HTMLEditorKit;

import com.actelion.research.chem.io.CompoundFileHelper;
import com.actelion.research.gui.FileHelper;
import com.actelion.research.gui.generic.GenericActionEvent;
import com.actelion.research.gui.generic.GenericDialog;
import com.actelion.research.gui.generic.GenericEventListener;
import com.actelion.research.gui.generic.GenericImage;
import com.actelion.research.gui.generic.GenericPopupMenu;
import com.actelion.research.gui.generic.GenericUIHelper;
import com.actelion.research.gui.hidpi.HiDPIHelper;
import com.actelion.research.gui.hidpi.ScaledEditorKit;

public class SwingUIHelper implements GenericUIHelper {
	private JComponent mParentComponent;
	private JDialog mHelpDialog;

	public SwingUIHelper(JComponent parent) {
		mParentComponent = parent;
		}

	@Override
	public void showMessage(String message) {
		JOptionPane.showMessageDialog(getWindow(), message);
		}

	@Override
	public GenericDialog createDialog(String title, GenericEventListener<GenericActionEvent> consumer) {
		GenericDialog dialog = new SwingDialog(getWindow(), title);
		dialog.setEventConsumer(consumer);
		return dialog;
		}

	@Override
	public GenericPopupMenu createPopupMenu(GenericEventListener<GenericActionEvent> consumer) {
		return new SwingPopupMenu(mParentComponent, consumer);
		}

	@Override
	public GenericImage createImage(String name) {
		return new SwingImage(name);
		}

	@Override
	public GenericImage createImage(int width, int height) {
		return new SwingImage(width, height);
		}

	@Override
	public void runLater(Runnable r) {
		SwingUtilities.invokeLater(r);
		}

	@Override
	public void grabFocus() {
		mParentComponent.requestFocus();
	}

	@Override
	public void setCursor(int cursor) {
		mParentComponent.setCursor(SwingCursorHelper.getCursor(cursor));
	}

	@Override
	public File openChemistryFile(boolean isReaction) {
		return isReaction ?
				FileHelper.getFile(mParentComponent, "Please select a reaction file",
						FileHelper.cFileTypeRXN | CompoundFileHelper.cFileTypeRD)
				: FileHelper.getFile(mParentComponent, "Please select a molecule file",
				FileHelper.cFileTypeMOL | CompoundFileHelper.cFileTypeMOL2);
		}

	@Override
	public void showHelpDialog(String url, String title) {
		if (mHelpDialog == null || !mHelpDialog.isVisible()) {
			JEditorPane helpPane = new JEditorPane();
			helpPane.setEditorKit(HiDPIHelper.getUIScaleFactor() == 1f ? new HTMLEditorKit() : new ScaledEditorKit());
			helpPane.setEditable(false);
			try {
				helpPane.setPage(getClass().getResource(url));
				}
			catch (Exception ex) {
				helpPane.setText(ex.toString());
				}

			Window c = getWindow();
			mHelpDialog = new JDialog(c, title, Dialog.DEFAULT_MODALITY_TYPE);

			mHelpDialog.setSize(HiDPIHelper.scale(520), HiDPIHelper.scale(440));
			mHelpDialog.getContentPane().add(new JScrollPane(helpPane,
					JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
					JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
			int x = (c.getX()>=8 + mHelpDialog.getWidth()) ? c.getX() - 8 - mHelpDialog.getWidth() : c.getX() + 8 + c.getWidth();
			mHelpDialog.setLocation(x, c.getY());
			mHelpDialog.setVisible(true);
			}
		else {
			Window c = getWindow();
			int x = (mHelpDialog.getX() + mHelpDialog.getWidth() / 2>=c.getX() + c.getWidth() / 2) ?
					c.getX() - 8 - mHelpDialog.getWidth() : c.getX() + 8 + c.getWidth();
			mHelpDialog.setLocation(x, c.getY());
			}
		}

	private Window getWindow() {
		return getWindow(mParentComponent);
	}

	public static Window getWindow(JComponent c) {
		return (Window) c.getTopLevelAncestor();
	}
	
}
