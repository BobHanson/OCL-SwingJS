package com.actelion.research.gui.editor;

import com.actelion.research.gui.generic.GenericActionEvent;
import com.actelion.research.gui.generic.GenericDialog;
import com.actelion.research.gui.generic.GenericEventListener;

public abstract class AsynchronousQueryBuilder implements GenericEventListener<GenericActionEvent> {

	protected abstract void setQueryFeatures();

	protected GenericDialog mDialog;
	protected boolean mOKSelected;
	protected Runnable onOK;
	protected Runnable onCancel;

	public boolean handleOkCancel(GenericActionEvent e) {
		if (e.getWhat() == GenericActionEvent.WHAT_OK) {
			setQueryFeatures();
			mOKSelected = true;
			mDialog.disposeDialog();
			if (onOK != null)
				onOK.run();
			return true;
		} 
		if (e.getWhat() == GenericActionEvent.WHAT_CANCEL) {
			mDialog.disposeDialog();
			if (onCancel != null)
				onCancel.run();
			return true;
		}
		return false;
	}
	
	/**
	 * Synchronous version
	 * 
	 * @return true if OK was pressed and potential change was applied to molecule
	 */
	public boolean showDialog() {
		mOKSelected = false;
		mDialog.showDialog();
		return mOKSelected;
		}

	/**
	 * Asynchronous version
	 * 
	 * @returns immediately but allows for asynchronous OK or CANCEL return
	 */
	public void showDialog(Runnable onOK, Runnable onCancel) {
		this.onOK = onOK;
		this.onCancel = onCancel;
		mDialog.showDialog(onOK, onCancel);
	}
	
	public GenericDialog getDialog() {
		return mDialog;
	}
	
}
