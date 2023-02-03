package com.actelion.research.gui;

import javax.swing.JPanel;
import javax.swing.Timer;

<<<<<<< HEAD
/**
 * common class between Views and JPanel when timed paintable messaging is desired. 
 * 
 * @author hansonr
 *
 */
=======
>>>>>>> branch 'master' of https://github.com/BobHanson/OCL-SwingJS.git
@SuppressWarnings("serial")
public abstract class SwingCanvas extends JPanel {

	private static final int WARNING_MILLIS = 1200;
	protected String mWarningMessage;

	protected void showWarningMessage(String msg) {
		mWarningMessage = msg;
		repaint();
		Timer t = new Timer(WARNING_MILLIS, (a) -> {
			mWarningMessage = null;
			repaint();
		});
		t.setRepeats(false);
		t.start();
	}
}
