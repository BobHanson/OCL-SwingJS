package com.actelion.research.gui.swing;

import com.actelion.research.gui.generic.*;
import com.actelion.research.gui.hidpi.HiDPIHelper;
import info.clearthought.layout.TableLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

@SuppressWarnings("serial")
public class SwingDialog extends JDialog implements ActionListener, GenericDialog {
	private Component mParent;
	private JPanel mContent;
	private GenericEventListener<GenericActionEvent> mConsumer;
	private Runnable onOK;
	private Runnable onCancel;

	public SwingDialog(Window parent, String title) {
		this(parent, title, DEFAULT_MODALITY_TYPE);
	}

	/**
	 * BH added 2024.12.19
	 * @param parent
	 * @param title
	 * @param modality
	 */
	public SwingDialog(Window parent, String title, ModalityType modality) {
		super(parent, title, modality);
		mParent = parent;
	}

	@Override
	public void setEventConsumer(GenericEventListener<GenericActionEvent> consumer) {
		mConsumer = (mConsumer == null ? consumer : setEventListener(mConsumer, consumer));
	}

	private GenericEventListener<GenericActionEvent> setEventListener(GenericEventListener<GenericActionEvent> mc, GenericEventListener<GenericActionEvent> consumer) {
		return new GenericEventListener<GenericActionEvent>() {
			@Override
			public void eventHappened(GenericActionEvent e) {
				if (mc != null)
					mc.eventHappened(e);
				if (consumer != null)
					consumer.eventHappened(e);
				if (e.getWhat() == GenericActionEvent.WHAT_OK) {
					if (onOK != null)
						onOK.run();
				} else if (e.getWhat() == GenericActionEvent.WHAT_CANCEL) {
					if (onCancel != null)
						onCancel.run();
				}
			}

		};
	}

	@Override
	public void setLayout(int[] hLayout, int[] vLayout) {
		double[][] size = new double[2][];
		size[0] = new double[hLayout.length];
		size[1] = new double[vLayout.length];
		for (int i = 0; i < hLayout.length; i++)
			size[0][i] = (hLayout[i] > 0) ? HiDPIHelper.scale(hLayout[i]) : hLayout[i];
		for (int i = 0; i < vLayout.length; i++)
			size[1][i] = (vLayout[i] > 0) ? HiDPIHelper.scale(vLayout[i]) : vLayout[i];

		mContent = new JPanel();
		mContent.setLayout(new TableLayout(size));
	}

	public void add(JPanel c) {
		mContent = c;
		super.add(c);
	}
	
	@Override
	public void add(GenericComponent c, int x, int y) {
		getContent().add(((SwingComponent) c).getComponent(), x + "," + y);
	}

	public JPanel getContent() {
		if (mContent == null)
			mContent = (JPanel) getContentPane();
		return mContent;
	}

	@Override
	public void add(GenericComponent c, int x1, int y1, int x2, int y2) {
		getContent().add(((SwingComponent) c).getComponent(), x1 + "," + y1 + "," + x2 + "," + y2);
	}

	@Override
	public void showDialog() {
		JPanel buttonpanel = new JPanel();
		int gap = HiDPIHelper.scale(8);
		buttonpanel.setBorder(BorderFactory.createEmptyBorder(gap * 3 / 2, gap, gap, gap));
		buttonpanel.setLayout(new BorderLayout());
		JPanel ibp = new JPanel();
		ibp.setLayout(new GridLayout(1, 2, gap, 0));
		JButton bcancel = new JButton("Cancel");
		bcancel.addActionListener(this);
		ibp.add(bcancel);
		JButton bok = new JButton("OK");
		bok.addActionListener(this);
		ibp.add(bok);
		buttonpanel.add(ibp, BorderLayout.EAST);
		getContentPane().add(getContent(), BorderLayout.CENTER);
		getContentPane().add(buttonpanel, BorderLayout.SOUTH);
		getRootPane().setDefaultButton(bok);

		pack();
		setLocationRelativeTo(mParent);
		setVisible(true);
	}

	@Override
	public void showDialog(Runnable onOK, Runnable onCancel) {
		this.onOK = onOK;
		this.onCancel = onCancel;
		showDialog();
	}

	@Override
	public void setVisible(boolean b) {
		super.setVisible(b);
		if (!b && onCancel != null && getModalityType() != ModalityType.MODELESS)
			onCancel.run();
	}

	@Override
	public void disposeDialog() {
		dispose();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		int type;
		switch (e.getActionCommand()) {
		case "OK":
			type = GenericActionEvent.WHAT_OK;
			break;
		case "Cancel":
			type = GenericActionEvent.WHAT_CANCEL;
			break;
		default:
			return;
		}
		getEventListener().eventHappened(new GenericActionEvent(this, type, 0));
	}
	
	private GenericEventListener<GenericActionEvent> getEventListener() {
		if (mConsumer == null) {
			mConsumer = setEventListener(null, null);
		}
		return mConsumer;
	}

	@Override
	public void showMessage(String message) {
		JOptionPane.showMessageDialog(mParent, message);
	}

	@Override
	public GenericLabel createLabel(String text) {
		return new SwingLabel(text);
	}

	@Override
	public GenericTextField createTextField(int width, int height) {
		return new SwingTextField(width, height);
	}

	@Override
	public GenericCheckBox createCheckBox(String text) {
		return new SwingCheckBox(text);
	}

	@Override
	public GenericComboBox createComboBox() {
		return new SwingComboBox();
	}

}
