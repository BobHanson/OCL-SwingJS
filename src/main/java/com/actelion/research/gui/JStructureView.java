/*
 * Copyright (c) 1997 - 2016
 * Actelion Pharmaceuticals Ltd.
 * Gewerbestrasse 16
 * CH-4123 Allschwil, Switzerland
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of the the copyright holder nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * @author Thomas Sander
 */

package com.actelion.research.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.UIManager;

import com.actelion.research.chem.AbstractDepictor;
import com.actelion.research.chem.IDCodeParser;
import com.actelion.research.chem.IsomericSmilesCreator;
import com.actelion.research.chem.SmilesParser;
import com.actelion.research.chem.StereoMolecule;
import com.actelion.research.chem.name.StructureNameResolver;
import com.actelion.research.gui.clipboard.IClipboardHandler;
import com.actelion.research.gui.dnd.MoleculeDragAdapter;
import com.actelion.research.gui.dnd.MoleculeDropAdapter;
import com.actelion.research.gui.dnd.MoleculeTransferable;
import com.actelion.research.gui.generic.GenericDepictor;
import com.actelion.research.gui.generic.GenericPolygon;
import com.actelion.research.gui.generic.GenericRectangle;
import com.actelion.research.gui.generic.GenericShape;
import com.actelion.research.gui.hidpi.HiDPIHelper;
import com.actelion.research.gui.swing.SwingCursorHelper;
import com.actelion.research.gui.swing.SwingDrawContext;
import com.actelion.research.util.ColorHelper;

public class JStructureView extends SwingCanvas implements ActionListener,MouseListener,MouseMotionListener,StructureListener {
    static final long serialVersionUID = 0x20061113;

    private static final Color DEFAULT_SELECTION_BACKGROUND = new Color(128,164,192);

    private static final String ITEM_COPY = "Copy Structure";
	private static final String ITEM_COPY_SMILES = "Copy Structure As SMILES-String";
	private static final String ITEM_PASTE= "Paste Structure";
	private static final String ITEM_PASTE_WITH_NAME = ITEM_PASTE+" or Name";
	private static final String ITEM_CLEAR = "Clear Structure";


	private static final int DRAG_MARGIN = 12;

	private ArrayList<StructureListener> mListener;
	private String mIDCode;
	private StereoMolecule mMol,mDisplayMol;
    private GenericDepictor mDepictor;
	private boolean mShowBorder,mAllowFragmentStatusChangeOnPasteOrDrop,mIsDraggingThis,mIsEditable,mDisableBorder,
			mIsSelectable,mIsSelecting,mIsLassoSelect;
	private int mChiralTextPosition,mDisplayMode;
	private String[] mAtomText;
	private IClipboardHandler mClipboardHandler;
	protected MoleculeDropAdapter mDropAdapter = null;
	protected int mAllowedDragAction;
	protected int mAllowedDropAction;
	private int[] mAtomHiliteColor;
	private float[] mAtomHiliteRadius;
	private double mTextSizeFactor,mX1,mY1,mX2,mY2;
	private GenericPolygon mLassoRegion;


	public JStructureView() {
        this(null);
		}

	/**
	 * This creates a standard structure view where the displayed molecule is
	 * used for D&D and clipboard transfer after removing atom colors and bond highlights.
	 * The default will support copy/paste and drag&drop from this view only,
	 * but dropping anything onto this view doesn't have an effect.
	 * Call setEditable(true) to allow changes through drag&drop and pasting.
	 * @param mol used for display, clipboard copy and d&d
	 */
	public JStructureView(StereoMolecule mol) {
        this(mol, DnDConstants.ACTION_COPY_OR_MOVE, DnDConstants.ACTION_COPY_OR_MOVE);
	    }

	/**
	 * This creates a structure view that distinguishes between displayed molecule
	 * and the one being used for D&D and clipboard transfer. Use this if the displayed
	 * molecule is structurally different, e.g. uses custom atom labels or additional
	 * illustrative atoms or bonds, which shall not be copied.
	 * Custom atom colors or highlighted bonds don't require a displayMol.
	 * The default will support copy/paste and drag&drop from this view only,
	 * but dropping anything onto this view doesn't have an effect.
	 * Call setEditable(true) to allow changes through drag&drop and pasting.
	 * @param mol used for clipboard copy and d&d; used for display if displayMol is null
	 * @param displayMol null if mol shall be displayed
	 */
	public JStructureView(StereoMolecule mol, StereoMolecule displayMol) {
        this(mol, displayMol, DnDConstants.ACTION_COPY_OR_MOVE, DnDConstants.ACTION_COPY_OR_MOVE);
	    }

	public JStructureView(int dragAction, int dropAction) {
        this(null, dragAction, dropAction);
	    }

	/**
	 * This creates a standard structure view where the displayed molecule is
	 * used for D&D and clipboard transfer after removing atom colors and bond highlights.
	 * The default will support copy/paste and drag&drop from this view only,
	 * but dropping anything onto this view doesn't have an effect.
	 * Call setEditable(true) to allow changes through drag&drop and pasting.
	 * @param mol used for display, clipboard copy and d&d
	 * @param dragAction
	 * @param dropAction
	 */
	public JStructureView(StereoMolecule mol, int dragAction, int dropAction) {
        this(mol, null, dragAction, dropAction);
		}

	/**
	 * This creates a structure view that distinguishes between displayed molecule
	 * and the one being used for D&D and clipboard transfer. Use this if the displayed
	 * molecule is structurally different, e.g. uses custom atom labels or additional
	 * illustrative atoms or bonds, which shall not be copied.
	 * Custom atom colors or highlighted bonds don't require a displayMol.
	 * The default will support copy/paste and drag&drop from this view only,
	 * but dropping anything onto this view doesn't have an effect.
	 * Call setEditable(true) to allow changes through drag&drop and pasting.
	 * @param mol used for clipboard copy and d&d; used for display if displayMol is null
	 * @param displayMol null if mol shall be displayed
	 * @param dragAction
	 * @param dropAction
	 */
	public JStructureView(StereoMolecule mol, StereoMolecule displayMol, int dragAction, int dropAction) {
		mMol = (mol == null) ? new StereoMolecule() : new StereoMolecule(mol);
		mDisplayMol = (displayMol == null) ? mMol : displayMol;
		mDisplayMode = AbstractDepictor.cDModeHiliteAllQueryFeatures;
		mTextSizeFactor = 1.0;
		mIsEditable = false;
		updateBackground();
		addMouseListener(this);
		addMouseMotionListener(this);
		try {
		initializeDragAndDrop(dragAction, dropAction);
		} catch (Exception e) {
			// headless linux operation will fail gracefully here
		}
	    }

	public void setSelectable(boolean s) {
		mIsSelectable = s;
		}

	@Override
	public void updateUI() {
		super.updateUI();
		updateBackground();
		}

    /**
     * Call this in order to get clipboard support:
     * setClipboardHandler(new ClipboardHandler());
     */
	public void setClipboardHandler(IClipboardHandler h) {
		mClipboardHandler = h;
	    }

	public IClipboardHandler getClipboardHandler() {
		return mClipboardHandler;
	    }

	public int getDisplayMode() {
		return mDisplayMode;
		}

	/**
	 * Sets the display mode for the Depictor. The default is
	 * AbstractDepictor.cDModeHiliteAllQueryFeatures.
	 * @param mode
	 */
	public void setDisplayMode(int mode) {
		if (mDisplayMode != mode) {
		    mDisplayMode = mode;
		    repaint();
			}
	    }

	/**
	 * Sets a multiplication factor to the text size of all labels. The default is 1.0.
	 * @param factor text size factor
	 */
	public void setFactorTextSize(double factor) {
		mTextSizeFactor = factor;
		}

	public void setDisableBorder(boolean b) {
		mDisableBorder = b;
		}

	/**
	 * Defines additional atom text to be displayed in top right
	 * position of some/all atom labels. If the atom is charged, then
	 * the atom text is drawn right of the atom charge.
	 * If using atom text make sure to update it accordingly, if atom
	 * indexes change due to molecule changes.
	 * Atom text is not supported for MODE_REACTION, MODE_MULTIPLE_FRAGMENTS or MODE_MARKUSH_STRUCTURE.
	 * @param atomText null or String array matching atom indexes (may contain null entries)
	 */
	public void setAtomText(String[] atomText) {
		mAtomText = atomText;
		}

	public void setEnabled(boolean enable) {
		if (enable != isEnabled()) {
			updateBackground();
			repaint();
			if (mDropAdapter != null)
				mDropAdapter.setActive(enable);
			}
		super.setEnabled(enable);
		}


	public boolean isEditable() {
		return mIsEditable;
	}

	public void setEditable(boolean b) {
		if (mIsEditable != b)
			mIsEditable = b;
		}

	/**
	 * When fragment status change on drop is allowed then dropping a fragment (molecule)
	 * on a molecule (fragment) inverts the status of the view's chemical object.
	 * As default status changes are prohibited.
	 * @param allow
	 */
	public void setAllowFragmentStatusChangeOnPasteOrDrop(boolean allow) {
		mAllowFragmentStatusChangeOnPasteOrDrop = allow;
		}

	/**
	 * If you want this structure view to also draw an atom background with specific colors for every atom,
	 * then you need to call this method before or just after one of the structureChanged() calls.
	 * @param argb if alpha < 1 then the background is mixed in accordingly
	 * @param radius <= 1.0; if null, then a default of 0.5 of the average bond length is used
	 */
	public void setAtomHighlightColors(int[] argb, float[] radius) {
		mAtomHiliteColor = argb;
		mAtomHiliteRadius = radius;
		repaint();
		}

	public boolean canDrop() {
		return mIsEditable && isEnabled() && !mIsDraggingThis;
	    }

	@Override
	public synchronized void paintComponent(Graphics g) {
        super.paintComponent(g);

        Dimension theSize = getSize();
		Insets insets = getInsets();
		theSize.width -= insets.left + insets.right;
		theSize.height -= insets.top + insets.bottom;

        if (theSize.width <= 0 || theSize.height <= 0)
            return;

        Graphics2D g2 = (Graphics2D)g;
        setGraphicsRenderingHints(g2);

		Color fg = g2.getColor();
		Color bg = getBackground();
		if (bg != null) {
			g2.setColor(bg);
			g2.fill(new Rectangle(insets.left, insets.top, theSize.width, theSize.height));
		}
		if (mShowBorder && !mDisableBorder) {
			GenericRectangle rect = mDepictor.getBoundingRect();
			if (rect != null) {
				g.setColor(ColorHelper.perceivedBrightness(bg) < 0.5f ? ColorHelper.brighter(bg, 0.85f) : ColorHelper.darker(bg, 0.85f));
				int arc = (int)Math.min(rect.height/4, Math.min(rect.width/4, HiDPIHelper.scale(10)));
				g.fillRoundRect((int)rect.x, (int)rect.y, (int)rect.width, (int)rect.height, arc, arc);
				}
			}

		g2.setColor(fg);

		mDepictor = depict(g2, getSize(), getInsets());

		if (mWarningMessage != null) {
			int fontSize = HiDPIHelper.scale(12);
			g.setFont(getFont().deriveFont(Font.BOLD, (float)fontSize));
			Color original = g.getColor();
			g.setColor(Color.RED);
			FontMetrics metrics = g.getFontMetrics();
			Rectangle2D bounds = metrics.getStringBounds(mWarningMessage, g);
			g.drawString(mWarningMessage, insets.left+(int)(theSize.width-bounds.getWidth())/2,
					insets.top+metrics.getHeight());
			g.setColor(original);
		}

		if (mIsSelecting) {
			if (mIsLassoSelect) {
				g.setColor(lassoColor());
				java.awt.Polygon p = new java.awt.Polygon();
				for (int i = 0; i<mLassoRegion.getSize(); i++)
					p.addPoint(Math.round((float)mLassoRegion.getX(i)), Math.round((float)mLassoRegion.getY(i)));
				g.drawPolygon(p);
				g.setColor(getForeground());
			} else {
				int x = (mX1 < mX2) ? (int) mX1 : (int) mX2;
				int y = (mY1 < mY2) ? (int) mY1 : (int) mY2;
				int w = (int) Math.abs(mX2 - mX1);
				int h = (int) Math.abs(mY2 - mY1);
				g.setColor(lassoColor());
				g.drawRect(x, y, w, h);
				g.setColor(getForeground());
			}
		}
	}

	private void setGraphicsRenderingHints(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
	}

	private Color lassoColor() {
		Color selectionColor = selectionColor();
		return ColorHelper.createColor(selectionColor, LookAndFeelHelper.isDarkLookAndFeel() ? 0.65f : 0.35f);
	}

	private Color selectionColor() {
		Color selectionColor = UIManager.getColor("TextArea.selectionBackground");
		return (selectionColor != null) ? selectionColor : DEFAULT_SELECTION_BACKGROUND;
	}

	public void setIDCode(String idcode) {
		int index = (idcode == null) ? -1 : idcode.indexOf(' ');
		if (index == -1)
			setIDCode(idcode, null);
		else
			setIDCode(idcode.substring(0, index), idcode.substring(index+1));
	    }

	public synchronized void setIDCode(String idcode, String coordinates) {
		if (idcode != null && idcode.isEmpty())
			idcode = null;

		if (mIDCode == null && idcode == null)
			return;

		if (mIDCode != null && mIDCode.equals(idcode))
			return;

		new IDCodeParser(true).parse(mMol, idcode, coordinates);
		mDisplayMol = mMol;

        mIDCode = idcode;
        repaint();
        informListeners();
		}

	/**
	 * Updates the molecule used for display, drag & drop and clipboard transfer.
	 * Also triggers a repaint().
	 * @param mol new molecule used for display, clipboard copy and d&d; may be null
	 */
	public synchronized void structureChanged(StereoMolecule mol) {
		if (mol == null) {
			mMol.clear();
			}
		else {
			mol.copyMolecule(mMol);
			}

		mDisplayMol = mMol;
        structureChanged();
		}

	/**
	 * Updates both molecules used for display and for drag & drop/clipboard transfer.
	 * Also triggers a repaint().
	 * @param mol new molecule used for display; may be null
	 * @param displayMol new molecule used for clipboard copy and d&d, may be null
	 */
	public synchronized void structureChanged(StereoMolecule mol, StereoMolecule displayMol) {
		if (mol == null) {
			mMol.clear();
			}
		else {
			mol.copyMolecule(mMol);
			}

		mDisplayMol = displayMol;
        structureChanged();
		}

	/**
	 * Should only be called if JStructureView's internal Molecule is changed
	 * from outside as: theStructureView.getMolecule().setFragment(false);
	 * The caller is responsible to update displayMol also, if it is different from
	 * the molecule.
	 */
	public synchronized void structureChanged() {
		mIDCode = null;
		repaint();
		informListeners();
		}

	public StereoMolecule getMolecule() {
		return mMol;
		}

	public StereoMolecule getDisplayMolecule() {
		return mDisplayMol;
		}

    public GenericDepictor getDepictor() {
        return mDepictor;
        }

    public void addStructureListener(StructureListener l) {
		if(mListener == null)
			mListener = new ArrayList<>();

		mListener.add(l);
		}

    public void removeStructureListener(StructureListener l) {
        if(mListener != null)
            mListener.remove(l);
        }

	public void setChiralDrawPosition(int p) {
		mChiralTextPosition = p;
		}

	@Override public void mouseClicked(MouseEvent e) {}
	@Override public void mouseEntered(MouseEvent e) {}
	@Override public void mouseExited(MouseEvent e) {}

	@Override
	public void mousePressed(MouseEvent e) {
		if (handlePopupTrigger(e))
			return;

		if (mIsSelectable
		 && (e.getModifiers() & InputEvent.BUTTON1_MASK) != 0) {
			mX1 = e.getX();
			mY1 = e.getY();
			mIsSelecting = true;
			mIsLassoSelect = !e.isAltDown();
			if (mIsLassoSelect) {
				mLassoRegion = new GenericPolygon();
				mLassoRegion.addPoint(mX1, mY1);
				mLassoRegion.addPoint(mX1, mY1);
				}
			}
		}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (mIsSelecting) {
			mIsSelecting = false;
			repaint();
			}

		handlePopupTrigger(e);
		}

	@Override
	public void mouseMoved(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		boolean isInRect = mDepictor != null
				&& (mAllowedDragAction & DnDConstants.ACTION_COPY) != 0
				&& shrink(mDepictor.getBoundingRect()).contains(x, y);

		updateBorder(isInRect);
		setCursor(SwingCursorHelper.getCursor(isInRect ? SwingCursorHelper.cHandCursor : SwingCursorHelper.cPointerCursor));
		}

	private GenericRectangle shrink(GenericRectangle rect) {
		int margin = HiDPIHelper.scale(DRAG_MARGIN);
		int marginX = Math.min(margin, (int)rect.width / 6);
		int marginY = Math.min(margin, (int)rect.height / 6);
		return new GenericRectangle((int)rect.x+marginX, (int)rect.y+marginY, (int)rect.width-2*marginX, (int)rect.height-2*marginY);
	}

	@Override public void mouseDragged(MouseEvent e) {
		if (mIsSelecting) {
			mX2 = e.getX();
			mY2 = e.getY();

			GenericShape selectedShape = null;

			if (mIsLassoSelect) {
				if ((Math.abs(mX2 - mLassoRegion.getX(mLassoRegion.getSize() - 1)) >= 3)
				 || (Math.abs(mY2 - mLassoRegion.getY(mLassoRegion.getSize() - 1)) >= 3)) {
					mLassoRegion.removeLastPoint();
					mLassoRegion.addPoint(mX2, mY2);
					mLassoRegion.addPoint(mX1, mY1);

					selectedShape = mLassoRegion;
					}
				}
			else {
				selectedShape = new GenericRectangle(Math.min(mX1, mX2), Math.min(mY1, mY2), Math.abs(mX2 - mX1), Math.abs(mY2 - mY1));
				}

			if (selectedShape != null && mDepictor != null) {
				for (int atom=0; atom<mDisplayMol.getAllAtoms(); atom++) {
					boolean isSelected = selectedShape.contains((int)mDepictor.getAtomX(atom), (int)mDepictor.getAtomY(atom));
					if (isSelected != mDisplayMol.isSelectedAtom(atom)) {
						mDisplayMol.setAtomSelection(atom, isSelected);
						}
					}
				repaint();
				}
			}
		}

	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals(ITEM_COPY)) {
			mClipboardHandler.copyMolecule(mMol);
			}
		if (e.getActionCommand().equals(ITEM_COPY_SMILES)) {
			final String smiles = new IsomericSmilesCreator(mMol).getSmiles();
			final StringSelection data = new StringSelection(smiles);
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(data, data);
			}
		if (e.getActionCommand().startsWith(ITEM_PASTE) && mIsEditable) {
			int smartsMode = mAllowFragmentStatusChangeOnPasteOrDrop ? SmilesParser.SMARTS_MODE_GUESS
						   : mMol.isFragment() ? SmilesParser.SMARTS_MODE_IS_SMARTS : SmilesParser.SMARTS_MODE_IS_SMILES;
			StereoMolecule mol = mClipboardHandler.pasteMolecule(true, smartsMode);
			if (mol != null) {
				if (!mAllowFragmentStatusChangeOnPasteOrDrop)
					mol.setFragment(mMol.isFragment());
				mMol = mol;
				mDisplayMol = mol;
				structureChanged();
				}
			else {
				showWarningMessage("No molecule on clipboard!");
				}
			}
		if (e.getActionCommand().equals(ITEM_CLEAR) && mIsEditable) {
			mMol.clear();
			mDisplayMol = mMol;
			structureChanged();
			}
		}

	private void updateBackground() {
		Color bg = UIManager.getColor(isEditable() && isEnabled() ? "TextField.background" : "TextField.inactiveBackground");
		if (bg != null)
			setBackground(bg);
		}

	private boolean handlePopupTrigger(MouseEvent e) {
		if (!e.isPopupTrigger())
			return false;

		if (mMol != null && mClipboardHandler != null) {
			JPopupMenu popup = new JPopupMenu();

			JMenuItem item1 = new JMenuItem(ITEM_COPY);
			item1.addActionListener(this);
			item1.setEnabled(mMol.getAllAtoms() != 0);
			popup.add(item1);

			JMenuItem itemCopySmiles = new JMenuItem(ITEM_COPY_SMILES);
			itemCopySmiles.addActionListener(this);
			itemCopySmiles.setEnabled(mMol.getAllAtoms() != 0);
			popup.add(itemCopySmiles);

			if (mIsEditable) {
				String itemText = StructureNameResolver.getInstance() == null ? ITEM_PASTE : ITEM_PASTE_WITH_NAME;
				JMenuItem item2 = new JMenuItem(itemText);
				item2.addActionListener(this);
				popup.add(item2);

				popup.addSeparator();

				JMenuItem item3 = new JMenuItem(ITEM_CLEAR);
				item3.addActionListener(this);
				item3.setEnabled(mMol.getAllAtoms() != 0);
				popup.add(item3);
				}

			popup.show(this, e.getX(), e.getY());
		}
		return true;
	}

	private void informListeners() {
		if (mListener != null)
			for (int i = 0; i<mListener.size(); i++)
				mListener.get(i).structureChanged(mMol);
		}

	private void initializeDragAndDrop(int dragAction, int dropAction) {
		final JStructureView outer = this;
		mAllowedDragAction = dragAction;
		mAllowedDropAction = dropAction;
		mAllowFragmentStatusChangeOnPasteOrDrop = false;

		if(dragAction != DnDConstants.ACTION_NONE){
			new MoleculeDragAdapter(this) {
				public Transferable getTransferable(Point origin) {
					return getMoleculeTransferable(origin);
				}

				public void onDragEnter() {
					outer.onDragEnter();
				}

				public void dragIsValidAndStarts() {
					mIsDraggingThis = true;
					}

				/*	public void onDragOver() {
					 outer.onDragOver();
					 }
				 */
				public void onDragExit() {
					outer.onDragExit();
				}

				public void dragDropEnd(DragSourceDropEvent e) {
					mIsDraggingThis = false;
				}
			};
		}

		if(dropAction != DnDConstants.ACTION_NONE) {
			mDropAdapter = new MoleculeDropAdapter() {
				public void onDropMolecule(StereoMolecule m,Point pt) {
					if (m != null && canDrop()){
						boolean isFragment = mMol.isFragment();
						mMol = new StereoMolecule(m);
				        mMol.removeAtomColors();
				        mMol.removeBondHiliting();
				        if (!mAllowFragmentStatusChangeOnPasteOrDrop)
				        	mMol.setFragment(isFragment);
				        mDisplayMol = mMol;
						repaint();
						informListeners();
						onDrop();
					}
					updateBorder(false);
				}

				public void dragEnter(DropTargetDragEvent e) {
					boolean drop = canDrop() && isDropOK(e) ;
					if (!drop)
						e.rejectDrag();
//					updateBorder(drop);
				}

				public void dragExit(DropTargetEvent e) {
//					updateBorder(false);
				}
			};

			new DropTarget(this, mAllowedDropAction, mDropAdapter, true);
//			new DropTarget(this,mAllowedDropAction,mDropAdapter,true, getSystemFlavorMap());
		}
	}


	protected Transferable getMoleculeTransferable(Point pt) {
		return new MoleculeTransferable(mMol);
	}

	// Drag notifications if needed by subclasses
	protected void onDragEnter() {}
	protected void onDragExit() {}
	protected void onDragOver() {}
	protected void onDrop() {}

	private void updateBorder(boolean showBorder) {
		if (mShowBorder != showBorder) {
			mShowBorder = showBorder;
			repaint();
		}
	}
	
	/**
	 * Carry out the depiction for this view with given dimensions and insets.
	 * 
	 * Allows for scaling the image prior to setting the dimensions.
	 * 
	 * Code culled from paintComponent, as it is used also for image creation.
	 * 
	 * @param g2
	 * @param theSize
	 * @param insets
	 * @return GenericDepictor for further processing
	 * @author Bob Hanson
	 */
	public GenericDepictor depict(Graphics2D g2, Dimension theSize, Insets insets) {
		if (mDisplayMol == null && mDisplayMol.getAllAtoms() == 0)
			return null;
		GenericDepictor depictor = new GenericDepictor(mDisplayMol);
        depictor.setDisplayMode(mDisplayMode);
        depictor.setFactorTextSize(mTextSizeFactor);
        depictor.setAtomText(mAtomText);
        depictor.setAtomHighlightColors(mAtomHiliteColor, mAtomHiliteRadius);
        Color bg = getBackground();
        int bgRGB = (bg == null ? 0 : bg.getRGB());
		if (!isEnabled())
            depictor.setOverruleColor(ColorHelper.getContrastColor(0x808080, bgRGB), bgRGB);
		else
			depictor.setForegroundColor(getForeground().getRGB(), bgRGB);
		int avbl = HiDPIHelper.scale(AbstractDepictor.cOptAvBondLen * (float) scaling);
		Graphics2D g = (g2 == null ? getTempGraphics() : g2);
		if (theSize == null)
			theSize = new Dimension(1000,1000);
		if (insets == null)
			insets = new Insets(5, 5, 5, 5);
		int width = theSize.width - (insets.left + insets.right);
		int height = theSize.height - (insets.top + insets.bottom);		
		SwingDrawContext context = new SwingDrawContext(g);
		depictor.validateView(context, new GenericRectangle(insets.left, insets.top, width, height),
							   AbstractDepictor.cModeInflateToMaxAVBL | mChiralTextPosition | avbl
							   );
		if (g2 != null) {
			// not just setting preferred size
	        setGraphicsRenderingHints(g2);
			depictor.paint(context);
		}
		return depictor;
	}

	/**
	 * a temporary image for creating PNG images
	 * @author Bob Hanson
	 */
	private BufferedImage tempImage;

	/**
	 * Get a singleton temporary 1000x1000 pixel image.
	 * This is used for preliminary depiction; probably a better way of doing this.
	 * @return Ggraphics2D of the buffered image. User should dispose of this.
	 * @author Bob Hanson
	 */
	private Graphics2D getTempGraphics() {
		if (tempImage == null)
			tempImage = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_ARGB);
		return tempImage.createGraphics();
	}

	/**
	 * BH 2024.12.15 to fit the display size to the molecule when generating an image
	 */
	private boolean setSizeToStructure = false;

	public Dimension setSizeToStructure(boolean TF) {
		setSizeToStructure = TF;
			return getPreferredSize();
	}
	
	/**
	 * Allow the structure to define the preferred size of the panel (and thus also the frame size)
	 * 
	 * @author Bob Hanson
	 */
	@Override
	public Dimension getPreferredSize() {
		if (!isPreferredSizeSet()) {
			// allows for getting the preferred size prior to addition to a site
			AbstractDepictor<?> d = (setSizeToStructure ? depict(null, null, null) : null);
			// BH 2024.12.15
			if (d != null) {
				GenericRectangle b = d.getBoundingRect();
				if (b.width > 0 && b.height > 0)
					return new Dimension((int) b.width + 10, (int) b.height + 10);
			}
		}
		return super.getPreferredSize();
	}

	/**
	 * allow for image scaling
	 * 
	 * @author Bob Hanson 
	 */
	private double scaling = 1;

	/**
	 * Set the structure scaling factor for depict().
	 * 
	 * @param scaling
	 * @author Bob Hanson
	 */
	public void setScaling(double scaling) {
		this.scaling  = scaling;
	}

	/**
	 * Creates a standard "chemist's" view of the molecule, with H atoms on heteroatoms, but otherwise absent.
	 * @param mode tweaks for the view. Default is 
	 * 
	 * @param mol
	 * 
	 * @return a JStructureView
	 * @author Bob Hanson
	 */
	public static JStructureView getStandardView(int mode, StereoMolecule mol) {
		return createView(mol, mode, 1, Color.white);
	}

	/**
	 * @author Bob Hanson
	 */
	
	public final static int defaultChemistsMode = 
			  AbstractDepictor.cDModeSuppressCIPParity 
		    | AbstractDepictor.cDModeSuppressESR
			| AbstractDepictor.cDModeSuppressChiralText
			| AbstractDepictor.cDModeBHNoSimpleHydrogens
			;

	public final static int chemistsModeWithCIP = 
			  AbstractDepictor.cDModeSuppressCIPParity 
		    | AbstractDepictor.cDModeSuppressESR
			| AbstractDepictor.cDModeSuppressChiralText
			| AbstractDepictor.cDModeBHNoSimpleHydrogens
			;

	public final static int classicView = 1234567890;

	/**
	 * Creates a JStructureView for this molecule, allowing for adjustments for 
	 * depictor mode, scaling, and background.
	 * 
	 * @param mol the molecule to render
	 * @param mode flags indicating how to show the image. See StereoMolecule.java
	 * @param scaling factor; 0 will be treats as 1
	 * @param bg background color (or null for transparent)
	 * @return a JStructureView
	 * @author Bob Hanson
	 */
	public static JStructureView createView(StereoMolecule mol, int mode, double scaling, Color bg) {
		JStructureView mArea = new JStructureView(mol);
		switch (mode) {
		case 0:
			mode = defaultChemistsMode;
			break;
		case classicView:
			mode = 0;
			break;
		}
		mArea.setDisplayMode(mode);
		mArea.setBackground(bg);
		mArea.setScaling(scaling);
		mArea.setSizeToStructure(true);
		return mArea;
	}

	/**
	 * Shows the view in a frame scaled to the molecule. 
	 * @param mArea
	 * @param title
	 * @author Bob Hanson
	 */
	public void showInFrame(String title, Point loc) {
		JFrame frame = new JFrame(title);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JFrame d = frame;
		d.add(this);
		d.pack();
		d.setLocation(loc.x, loc.y);
		d.setVisible(true);
	}

	/**
	 * Creates a buffered image, setting the size to to match the structure.
	 * @param mArea
	 * @return buffered image
	 */
	public BufferedImage getSizedImage() {
		Dimension d = getPreferredSize();
		BufferedImage bi = new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = bi.createGraphics();
		Color c = getBackground();
		if (c != null) {
			g.setColor(c);
			g.fillRect(0, 0, d.width, d.height);
		}
		depict(g, d, null);
		return bi;
	}
}
