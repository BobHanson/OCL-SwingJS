package com.actelion.research.gui.fx;

import com.actelion.research.gui.generic.GenericCursorHelper;
import javafx.geometry.Dimension2D;
import javafx.scene.Cursor;
import javafx.scene.ImageCursor;

public class FXCursorHelper extends GenericCursorHelper {
	private static Cursor[]	sCursor;

	public static Cursor getCursor(int cursor) {
		if (sCursor == null)
			sCursor = new Cursor[cCursorCount];

		if (sCursor[cursor] == null)
			sCursor[cursor] = createCursor(cursor);

		return sCursor[cursor];
	}

	public static Cursor createCursor(int cursor) {
		Dimension2D size = ImageCursor.getBestSize(32, 32);
		if (size.getWidth() >= 32 && size.getHeight() >= 32 && IMAGE_NAME_32[cursor] != null) {
			return new ImageCursor(new FXImage("cursor/" + IMAGE_NAME_32[cursor]).get());
		}
		else if (size.getWidth() >= 24 && size.getHeight() >= 24 && IMAGE_NAME_32[cursor] != null) {
			FXImage image = new FXImage("cursor/" + IMAGE_NAME_32[cursor]);
			image.scale(24, 24);
			return new ImageCursor(image.get());
		}

		if (size.getWidth() >= 16 && size.getHeight() >= 16 && cursor<IMAGE_DATA_16.length) {
			FXImage image = new FXImage((int)size.getWidth(), (int)size.getHeight());
			build16x16CursorImage(image, cursor);
			return new ImageCursor(image.get());
		}

		if (cursor == cPointerCursor)
			return Cursor.DEFAULT;
		if (cursor == cTextCursor)
			return Cursor.TEXT;
		if (cursor == cPointedHandCursor)
			return Cursor.HAND;

		return Cursor.DEFAULT;
	}
}