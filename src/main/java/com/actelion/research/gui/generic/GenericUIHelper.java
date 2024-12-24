package com.actelion.research.gui.generic;

import java.io.File;
import java.util.function.Consumer;

public interface GenericUIHelper {
	
	public static boolean isJS = /** @j2sNative true || */
			false;

	public static boolean isAsynchronous() {
		return isJS;
	}	

	void showMessage(String message);
	void showHelpDialog(String url, String title);
	GenericDialog createDialog(String title, GenericEventListener<GenericActionEvent> consumer);
	GenericPopupMenu createPopupMenu(GenericEventListener<GenericActionEvent> consumer);
	GenericImage createImage(String name);
	GenericImage createImage(int width, int height);
	void grabFocus();
	void setCursor(int cursor);
	void runLater(Runnable r);
}
