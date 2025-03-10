package com.actelion.research.gui;

import com.actelion.research.gui.hidpi.HiDPIHelper;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.InvocationTargetException;

public class ScrollPaneAutoScrollerWhenDragging {
	private static final int SCROLL_AREA_HEIGHT = HiDPIHelper.scale(32);
	private static final int SCROLL_SLEEP_MILLIS = 20;
	private static final int SCROLL_MAX_PIXELS = HiDPIHelper.scale(16);

	private JScrollPane	mScrollPane;
	private boolean		mIsVertical;
	private int			mContentSize,mViewportSize;
	private Thread		mScrollThread;
	private float		mScrollSpeed;
	private Timer timer;

	public ScrollPaneAutoScrollerWhenDragging(JScrollPane scrollPane, boolean isVertical) {
		mScrollPane = scrollPane;
		mIsVertical = isVertical;
		}

	public void autoScroll() {
		Component content = mScrollPane.getViewport().getView();
		mContentSize = mIsVertical ? content.getHeight() : content.getWidth();
		mViewportSize = mIsVertical ? mScrollPane.getViewport().getHeight() : mScrollPane.getViewport().getWidth();

		Point mp = mScrollPane.getMousePosition();
		if (mp != null) {
			int mousePosition = mIsVertical ? mp.y : mp.x;
			int viewportPosition = mIsVertical ? mScrollPane.getViewport().getY() : mScrollPane.getViewport().getX();
			int mouseToTop = mousePosition - viewportPosition;
			int mouseToBottom = viewportPosition + mViewportSize - mousePosition;
			mScrollSpeed = (mouseToTop < SCROLL_AREA_HEIGHT)
					? -Math.min(1.0f, (float) (SCROLL_AREA_HEIGHT - mouseToTop) / SCROLL_AREA_HEIGHT)
					: (mouseToBottom < SCROLL_AREA_HEIGHT)
							? Math.min(1.0f, (float) (SCROLL_AREA_HEIGHT - mouseToBottom) / SCROLL_AREA_HEIGHT)
							: 0;
		}

		if (mScrollSpeed == 0) {
			mScrollThread = null;
		} else if (mScrollThread == null) {
			mScrollThread = new Thread(() -> {
				this.timer = new Timer((int) SCROLL_SLEEP_MILLIS, (a) -> {
					if (mScrollThread == null) {
						timer.stop();
						timer = null;
						return;
					}
					Point vp = mScrollPane.getViewport().getViewPosition();
					int minStep = mIsVertical ? -vp.y : -vp.x;
					int maxStep = Math.max(0, mContentSize - mViewportSize + minStep);
					int step = Math.max(minStep, Math.min(maxStep, Math.round(mScrollSpeed * SCROLL_MAX_PIXELS)));
					if (step == 0) {
						mScrollThread = null;
					} else if (mIsVertical) {
						vp.y += step;
					} else {
						vp.x += step;
						try {
							SwingUtilities.invokeAndWait(() -> mScrollPane.getViewport().setViewPosition(vp));
						} catch (InvocationTargetException | InterruptedException e) {
							mScrollThread = null;
						}
					}
				});
				timer.setRepeats(true);
				timer.start();
			});
			mScrollThread.start();
		}
	}

	public void stopScrolling() {
		mScrollThread = null;
		}
	}
