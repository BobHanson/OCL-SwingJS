package com.actelion.research.gui;

import javax.swing.JPanel;
import javax.swing.Timer;

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
