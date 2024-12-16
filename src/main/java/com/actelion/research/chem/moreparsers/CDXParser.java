package com.actelion.research.chem.moreparsers;

import java.io.BufferedReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import com.actelion.research.chem.PeriodicTable;
import com.actelion.research.chem.StereoMolecule;
import com.actelion.research.chem.coords.CoordinateInventor;

/**
 * A parser for binary CDX or XML CDXML files.
 * 
 * For the specification of CDX/CDXML, see
 * https://iupac.github.io/IUPAC-FAIRSpec
 * 
 * All the binary parser does is convert the CDX to CDXML and then feed that to
 * XML parser. (Seemed like a useful thing to have anyway, and it was simpler
 * this way.)
 * 
 * revvity site:
 * 
 * 
 * https://support.revvitysignals.com/hc/en-us/articles/4408233129748-Where-is-the-ChemDraw-SDK-located
 * 
 * Their link:
 * 
 * https://web.archive.org/web/20221209095323/https://www.cambridgesoft.com/services/documentation/sdk/chemdraw/cdx/
 * 
 * WayBack machine Overview:
 * 
 * https://web.archive.org/web/20240000000000*
 * /https://www.cambridgesoft.com/services/documentation/sdk/chemdraw/cdx
 * 
 * Partial archives:
 * 
 * https://web.archive.org/web/20160911235313/http://www.cambridgesoft.com/services/documentation/sdk/chemdraw/cdx/index.htm
 * 
 * https://web.archive.org/web/20160310081515/http://www.cambridgesoft.com/services/documentation/sdk/chemdraw/cdx/
 * 
 * https://web.archive.org/web/20100503174209/http://www.cambridgesoft.com/services/documentation/sdk/chemdraw/cdx/
 * 
 * Unfortunately, there appears to be no single archive that has all the images,
 * and so some of those are missing.
 * 
 * But see https://iupac.github.io/IUPAC-FAIRSpec for the full, detailed
 * specification.
 * 
 * Here we are just looking for simple aspects that could be converted to valid
 * 2D MOL files, SMILES, and InChI.
 * 
 * Fragments (such as CH2CH2OH) and "Nickname"-type fragments such as Ac and Ph,
 * are processed correctly. But their 2D representations are pretty nuts.
 * ChemDraw does not make any attempt to place these in reasonable locations.
 * That said, Jmol's 3D minimization does a pretty fair job, and the default is
 * to do that minimization.
 * 
 * If minimization and addition of H is not desired, use FILTER "NOH" or FILTER
 * "NO3D"
 * 
 * @author hansonr@stolaf.edu
 * 
 *
 */
public class CDXParser extends XmlReader {

	private static final int BOND_ORDER_NULL = 0;
	private static final int ORDER_STEREO_EITHER = -1;

	public static StereoMolecule parseFile(String path) {
		byte[] cdx = ParserUtils.getURLContentsAsBytes(path);
		StereoMolecule mol = new StereoMolecule();
		boolean isOK;
		if (cdx == null || cdx.length == 0)
			return null;
		if (cdx[0] == 0x56) {
			isOK = new CDXParser().parse(mol, cdx);
		} else {
			try {
				isOK = new CDXParser().parse(mol, new String(cdx, "utf-8"));
			} catch (UnsupportedEncodingException e) {
				isOK = false;
			}
		}
		return (isOK && mol.getAllAtoms() > 0 ? mol : null);
	}

	public boolean parse(StereoMolecule mol, String cdxml) {
		reader = new BufferedReader(new StringReader(cdxml));
		setMyError(parseXML());
		finalizeReader(mol);
		new CoordinateInventor(0).invent(mol);
		return (err == null);
	}

	public boolean parse(StereoMolecule mol, byte[] cdx) {
		try {
			String cdxml = CDX2CDXML.fromCDX(cdx);
			//dump(cdxml);
			return parse(mol, cdxml);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

	}

	private StringBuffer warnings = new StringBuffer();

	private double minX = Double.MAX_VALUE;
	private double minY = Double.MAX_VALUE;
	private double maxY = -Double.MAX_VALUE;
	private double maxX = -Double.MAX_VALUE;

	/**
	 * main list
	 */
	List<CDNode> atoms = new ArrayList<CDNode>();

	/**
	 * main list
	 */
	List<CDBond> bonds = new ArrayList<CDBond>();

	private BitSet bsAtoms = new BitSet(), bsBonds = new BitSet();

	private Map<String, CDNode> atomSymbolicMap = new HashMap<String, CDNode>();

	private Stack<BracketedGroup> bracketedGroups;

	/**
	 * CDNode extends Atom in order to maintain information about fragments,
	 * connectivity, and validity
	 * 
	 */
	class CDNode {

		double x;
		double y;

		String warning;
		String id;
		int intID;
		boolean isValid = true;
		boolean isConnected;
		boolean isExternalPt;
		String nodeType;

		boolean isFragment; // could also be a Nickname
		/**
		 * fragment ID for the fragment containing this node
		 */
		String outerFragmentID;

		/**
		 * fragment ID of this fragment node
		 */
		String innerFragmentID;

		public String text;
		CDNode parentNode;
		/**
		 * list of connection bonds, ordered by ID
		 */
		List<Object[]> orderedConnectionBonds;
		/**
		 * for an external point, the actual atom associated with it in the fragment
		 */
		CDNode internalAtom;
		/**
		 * for a fragment, the list of external points for a fragment, ordered by
		 * sequence in the label
		 */
		List<CDNode> orderedExternalPoints;

		/**
		 * 0x0432 For multicenter attachment nodes or variable attachment nodes a list
		 * of IDs of the nodes which are multiply or variably attached to this node
		 * array of attachment id values;
		 * 
		 * for example, in ferrocene, we are attaching to all of the carbon atoms if
		 * this node is the special point that indicates that attachment
		 */
		private String[] attachments;
		/**
		 * 0x0431 BondOrdering An ordering of the bonds to this node used for
		 * stereocenters fragments and named alternative groups with more than one
		 * attachment.
		 * 
		 */
		private String[] bondOrdering;
		/**
		 * 0x0505 ConnectionOrder An ordered list of attachment points within a
		 * fragment.
		 * 
		 */
		private String[] connectionOrder;

		public boolean hasMultipleAttachments;
		CDNode attachedAtom;

		private int atomSerial = Integer.MIN_VALUE;

		private int index;

		private String elementSymbol;

		private int elementNumber;
		public String symbol;
		public int formalCharge;
		public int isotope;

		CDNode(int index, String id, String nodeType, String fragmentID, CDNode parent) {
			this.id = id;
			this.index = index;
			this.outerFragmentID = fragmentID;
			this.atomSerial = intID = Integer.parseInt(id);
			this.nodeType = nodeType;
			this.parentNode = parent;
			isFragment = "Fragment".equals(nodeType) || "Nickname".equals(nodeType);
			isExternalPt = "ExternalConnectionPoint".equals(nodeType);
			// isGeneric = "GenericNickname".equals(nodeType);
		}

		public void set(double x, double y) {
			this.x = x;
			this.y = y;
			// System.out.println("atom " + index + " " + x + " " + y);
		}

		public void setInnerFragmentID(String id) {
			innerFragmentID = id;
		}

		void setBondOrdering(String[] bondOrdering) {
			this.bondOrdering = bondOrdering;
		}

		void setConnectionOrder(String[] connectionOrder) {
			this.connectionOrder = connectionOrder;
		}

		void setMultipleAttachments(String[] attachments) {
			this.attachments = attachments;
			hasMultipleAttachments = true;
		}

		/**
		 * keep these in order
		 * 
		 * @param externalPoint
		 */
		void addExternalPoint(CDNode externalPoint) {
			if (orderedExternalPoints == null)
				orderedExternalPoints = new ArrayList<CDNode>();
			int i = orderedExternalPoints.size();
			while (--i >= 0 && orderedExternalPoints.get(i).intID >= externalPoint.internalAtom.intID) {
				// continue;
			}
			orderedExternalPoints.add(++i, externalPoint);
		}

		public void setInternalAtom(CDNode a) {
			internalAtom = a;
			if (parentNode != null) {
				parentNode.addExternalPoint(this);
			}
		}

		void addAttachedAtom(CDBond bond, int pt) {
			if (orderedConnectionBonds == null)
				orderedConnectionBonds = new ArrayList<Object[]>();
			int i = orderedConnectionBonds.size();
			while (--i >= 0 && ((Integer) orderedConnectionBonds.get(i)[0]).intValue() > pt) {
				// continue;
			}
			orderedConnectionBonds.add(++i, new Object[] { Integer.valueOf(pt), bond });
		}

		void fixAttachments() {
			if (hasMultipleAttachments && attachedAtom != null) {
				// something like Ferrocene
				int order = StereoMolecule.cBondTypeMetalLigand;
				CDNode a1 = attachedAtom;
				for (int i = attachments.length; --i >= 0;) {
					CDNode a = (CDNode) objectsByID.get(attachments[i]);
					if (a != null) {
						bsBonds.set(bonds.size());
						addBond(new CDBond(null, a1.id, a.id, order));						
					}
				}
			}

			if (orderedExternalPoints == null || text == null)
				return;
			// fragments and Nicknames
			int n = orderedExternalPoints.size();
			if (n != orderedConnectionBonds.size()) {
				System.err.println("CDXMLParser cannot fix attachments for fragment " + text);
				return;
			}
			if (bondOrdering == null) {
				bondOrdering = new String[n];
				for (int i = 0; i < n; i++) {
					bondOrdering[i] = ((CDBond) orderedConnectionBonds.get(i)[1]).id;
				}
			}
			if (connectionOrder == null) {
				connectionOrder = new String[n];
				for (int i = 0; i < n; i++) {
					connectionOrder[i] = orderedExternalPoints.get(i).id;
				}
			}

			for (int i = 0; i < n; i++) {
				CDBond b = (CDBond) objectsByID.get(bondOrdering[i]);
				CDNode a = ((CDNode) objectsByID.get(connectionOrder[i])).internalAtom;
				updateExternalBond(b, a);
			}
		}

		/**
		 * Replace the fragment connection (to this fragment node) in bond b with the
		 * internal atom a.
		 * 
		 * @param bond2f
		 * @param intAtom
		 */
		private void updateExternalBond(CDBond bond2f, CDNode intAtom) {
			bsBonds.set(bond2f.index);
			if (bond2f.atomIndex2 == index) {
				bond2f.atomIndex2 = intAtom.index;
				bond2f.atom2 = intAtom;
			} else if (bond2f.atomIndex1 == index) {
				bond2f.atomIndex1 = intAtom.index;
				bond2f.atom1 = intAtom;
			} else {
				System.err.println("CDXMLParser attachment failed! " + intAtom + " " + bond2f);
			}

		}

		@Override
		public String toString() {
			return "[CDNode " + id + " " + elementSymbol + " " + elementNumber + " index=" + index + " ext="
					+ isExternalPt + " frag=" + isFragment + " " + elementSymbol + " " + x + " " + y + "]";
		}

		public double distance(CDNode a2) {
			return Math.sqrt(x * a2.x + y * a2.y);
		}

	}

	class CDBond {
		int index;
		int atomIndex1;
		int atomIndex2;
		String id, id1, id2;
		private int type;
		private CDNode atom1;
		private CDNode atom2;

		CDBond(String id, String id1, String id2, int type) {

			atomIndex1 = (atom1 = (CDNode) objectsByID.get(id1)).index;
			atomIndex2 = (atom2 = (CDNode) objectsByID.get(id2)).index;

			this.type = type;
			this.id = id;
			this.id1 = id1;
			this.id2 = id2;
		}

		CDNode getOtherNode(CDNode a) {
			return atoms.get(atomIndex1 == a.index ? atomIndex2 : atomIndex1);
		}

		@Override
		public String toString() {
			return "[CDBond " + id + " id1=" + id1 + " id2=" + id2 + super.toString() + "]";
		}

	}

	private Stack<String> fragments = new Stack<String>();

	private String thisFragmentID;
	private CDNode thisAtom;
	private CDNode thisNode;

	private Stack<CDNode> nodes = new Stack<CDNode>();
	private List<CDNode> nostereo = new ArrayList<CDNode>();
	Map<String, Object> objectsByID = new HashMap<String, Object>();

	/**
	 * temporary holder of style chunks within text objects
	 */
	private String textBuffer;

	private int atomIndex;

//	private void dump(String cdxml) throws IOException {
//		System.out.println(cdxml);
//		FileOutputStream fos = new FileOutputStream("c:/temp/tout.cdxml");
//		fos.write(cdxml.getBytes());
//		fos.close();
//	}

	@Override
	public void processStartElement(String localName, String nodeName) {
		String id = atts.get("id");
		if ("fragment".equals(localName)) {
			objectsByID.put(id, setFragment(id));
			return;
		}

		if ("n".equals(localName)) {
			objectsByID.put(id, setNode(id));
			return;
		}

		if ("b".equals(localName)) {
			objectsByID.put(id, setBond(id));
			return;
		}

		if ("t".equals(localName)) {
			textBuffer = "";
			return;
		}

		if ("s".equals(localName)) {
			setKeepChars(true);
			return;
		}

		if ("crossingbond".equals(localName)) {
			BracketedGroup bg = (bracketedGroups == null || bracketedGroups.isEmpty() ? null
					: bracketedGroups.get(bracketedGroups.size() - 1));
			if (bg != null && bg.repeatCount > 0) {
				bg.innerAtomID = parseIntStr(atts.get("inneratomid"));
				bg.bondID = parseIntStr(atts.get("bondid"));
			}
			return;
		}
		if ("bracketedgroup".equals(localName)) {
			String usage = atts.get("bracketusage");
			if (bracketedGroups == null)
				bracketedGroups = new Stack<>();
			int[] ids = null;
			int repeatCount = 0;
			if ("MultipleGroup".equals(usage)) {
				String[] sids = getTokens(atts.get("bracketedobjectids"));
				ids = new int[sids.length];
				for (int i = ids.length; --i >= 0;)
					ids[i] = parseIntStr(sids[i]);
				repeatCount = parseIntStr(atts.get("repeatcount"));
			}
			bracketedGroups.add(new BracketedGroup(ids, repeatCount));
		}

	}

	private String[] getTokens(String s) {
		return s.split("\\s");
	}

	private int parseIntStr(String s) {
		try {
			return Integer.parseInt(s);
		} catch (Exception e) {
			return Integer.MIN_VALUE;
		}
	}

	private double parseDoubleStr(String s) {
		try {
			return Double.parseDouble(s);
		} catch (Exception e) {
			return Double.NaN;
		}
	}

	static class BracketedGroup {
		int[] ids;
		int bondID;
		int innerAtomID;
		int repeatCount;

		BracketedGroup(int[] ids, int repeatCount) {
			this.ids = ids;
			this.repeatCount = repeatCount;
		}

		public void process() {
			// TODO
			// here we would process all of this;
			System.out.println("bracketed groups not implmented");

		}
	}

	private CDNode setFragment(String id) {
		fragments.push(thisFragmentID = id);
		CDNode fragmentNode = (thisNode == null || !thisNode.isFragment ? null : thisNode);
		if (fragmentNode != null) {
			fragmentNode.setInnerFragmentID(id);
		}
		String s = atts.get("connectionorder");
		if (s != null) {
			System.out.println(id + " ConnectionOrder is " + s);
			thisNode.setConnectionOrder(s.trim().split(" "));
		}
		return fragmentNode;
	}

	@Override
	void processEndElement(String localName) {
		if ("fragment".equals(localName)) {
			thisFragmentID = fragments.pop();
			return;
		}
		if ("n".equals(localName)) {
			thisNode = (nodes.size() == 0 ? null : nodes.pop());
			return;
		}
		if ("BracketedGroup".equals(localName)) {
			bracketedGroups.pop().process();
		}
		if ("s".equals(localName)) {
			textBuffer += chars.toString();
		}

		if ("t".equals(localName)) {
			if (thisNode == null) {
				System.out.println("CDXMLParser found unassigned 't' text: " + textBuffer);
			} else {
				thisNode.text = textBuffer;
				if (thisAtom.elementNumber == 0) {
					System.err.println("XmlChemDrawReader: Problem with \"" + textBuffer + "\"");
				}
				if (thisNode.warning != null)
					warnings.append("Warning: " + textBuffer + " " + thisNode.warning + "\n");
			}
			textBuffer = "";
		}

		setKeepChars(false);
	}

	/**
	 * Set the atom information. Reading:
	 * 
	 * NodeType, Warning. Element, Isotope, Charge, xyz, p
	 * 
	 * 3D coordinates xyz is only used if there are no 2D p coordinates. This may
	 * not be possible. I don't know. These aren't real 3D coordinates, just
	 * enhanced z values.
	 * 
	 * @param id
	 * @return thisNode
	 */
	private CDNode setNode(String id) {
		String nodeType = atts.get("nodetype");
		if (thisNode != null)
			nodes.push(thisNode);
		if ("_".equals(nodeType)) {
			// internal Jmol code for ignored node
			thisAtom = thisNode = null;
			return null;
		}

		thisAtom = thisNode = new CDNode(atomIndex++, id, nodeType, thisFragmentID, thisNode);
		addAtomWithMappedSerialNumber(thisAtom);
		bsAtoms.set(thisAtom.index);

		String w = atts.get("warning");
		if (w != null) {
			thisNode.warning = w.replace("&apos;", "'");
			thisNode.isValid = (w.indexOf("ChemDraw can't interpret") < 0);
		}

		String element = atts.get("element");
		String s = atts.get("genericnickname");
		if (s != null) {
			element = s;
		}

		thisAtom.elementNumber = (!checkWarningOK(w) ? 0 : element == null ? 6 : parseIntStr(element));
		thisAtom.symbol = PeriodicTable.symbol(thisAtom.elementNumber);
		s = atts.get("isotope");
		if (s != null)
			thisAtom.isotope = parseIntStr(s);
		s = atts.get("charge");
		if (s != null) {
			thisAtom.formalCharge = parseIntStr(s);
		}

		// boolean hasXYZ = (atts.containsKey("xyz"));
		// boolean hasXY = (atts.containsKey("p"));
		setAtom();
		s = atts.get("attachments");
		if (s != null) {
			// System.out.println(id + " Attachments is " + s);
			thisNode.setMultipleAttachments(split(s.trim(), " "));
		}

		s = atts.get("bondordering");
		if (s != null) {
			// System.out.println(id + " BondOrdering is " + s);
			thisNode.setBondOrdering(split(s.trim(), " "));
		}

//		if (Logger.debugging)
//			Logger.info("XmlChemDraw id=" + id + " " + element + " " + atom);

		return thisNode;
	}

	private void addAtomWithMappedSerialNumber(CDNode atom) {
		atoms.add(atom);
		int atomSerial = atom.atomSerial;
		if (atomSerial != Integer.MIN_VALUE)
			atomSymbolicMap.put("" + atomSerial, atom);
	}

	private String[] split(String s, String p) {
		return s.split(p);
	}

	private boolean checkWarningOK(String warning) {
		return (warning == null || warning.indexOf("valence") >= 0 || warning.indexOf("very close") >= 0
				|| warning.indexOf("two identical colinear bonds") >= 0);
	}

	/**
	 * Process the bond tags. We only look at the following attributes:
	 * 
	 * B beginning atom (atom1)
	 * 
	 * E ending atom (atom2)
	 * 
	 * BeginAttach associates atom1 with a fragment
	 * 
	 * EndAttach associates atom2 with a fragment
	 * 
	 * Order -- the bond order
	 * 
	 * Display -- wedges and such
	 * 
	 * Display2 -- only important here for partial bonds
	 * 
	 * bonds to multiple attachments are not actually made.
	 * 
	 * @param id
	 * @return the bond
	 * 
	 */

//	public static final int cBondTypeSingle			= 0x00000001;
//	public static final int cBondTypeDouble			= 0x00000002;
//	public static final int cBondTypeTriple			= 0x00000004;
//	public static final int cBondTypeQuadruple		= 0x00000008;
//	public static final int cBondTypeQuintuple		= 0x00000010;
//	public static final int cBondTypeMetalLigand	= 0x00000020;
//	public static final int cBondTypeDelocalized	= 0x00000040;
//	public static final int cBondTypeDown			= 0x00000081;
//	public static final int cBondTypeUp				= 0x00000101;
//	public static final int cBondTypeCross			= 0x00000182;
//	public static final int cBondTypeDeleted		= 0x00000200;
//	public static final int cBondTypeIncreaseOrder  = 0x000001FF;

	private CDBond setBond(String id) {
		String atom1 = atts.get("b");
		String atom2 = atts.get("e");
		String a = atts.get("beginattach");
		int beginAttach = (a == null ? 0 : parseIntStr(a));
		a = atts.get("endattach");
		int endAttach = (a == null ? 0 : parseIntStr(a));
		String s = atts.get("order");
		String disp = atts.get("display");
		String disp2 = atts.get("display2");
		int type = BOND_ORDER_NULL;
		boolean invertEnds = false;
		if (disp == null) {
			if (s == null) {
				type = StereoMolecule.cBondTypeSingle;
			} else if (s.equals("1.5")) {
				type = StereoMolecule.cBondTypeDelocalized;
			} else {
				if (s.indexOf(".") > 0 && !"Dash".equals(disp2)) {
					// partial only works with "dash" setting for second line
					s = s.substring(0, s.indexOf("."));
				}
				type = getBondTypeFromString(s);
			}
		} else if (disp.equals("WedgeBegin")) {
			type = StereoMolecule.cBondTypeUp; // near
		} else if (disp.equals("Hash") || disp.equals("WedgedHashBegin")) {
			type = StereoMolecule.cBondTypeDown;
		} else if (disp.equals("WedgeEnd")) {
			invertEnds = true;
			type = StereoMolecule.cBondTypeUp;
		} else if (disp.equals("WedgedHashEnd")) {
			invertEnds = true;
			type = StereoMolecule.cBondTypeDown;
		} else if (disp.equals("Bold")) {
			type = StereoMolecule.cBondTypeSingle;
		} else if (disp.equals("Wavy")) {
			type = ORDER_STEREO_EITHER;
		}
		if (type == BOND_ORDER_NULL) {
			// dative, ionic, hydrogen, threecenter
			System.err.println("XmlChemDrawReader ignoring bond type " + s);
			return null;
		}
		CDBond b = (invertEnds ? new CDBond(id, atom2, atom1, type) : new CDBond(id, atom1, atom2, type));

		CDNode node1 = atoms.get(b.atomIndex1);
		CDNode node2 = atoms.get(b.atomIndex2);

		if (type == ORDER_STEREO_EITHER) {
			if (!nostereo.contains(node1))
				nostereo.add(node1);
			if (!nostereo.contains(node2))
				nostereo.add(node2);
		}

		if (node1.hasMultipleAttachments) {
			node1.attachedAtom = node2;
			return b;
		} else if (node2.hasMultipleAttachments) {
			node2.attachedAtom = node1;
			return b;
		}

		if (node1.isFragment && beginAttach == 0)
			beginAttach = 1;
		if (node2.isFragment && endAttach == 0)
			endAttach = 1;
		if (beginAttach > 0) {
			(invertEnds ? node2 : node1).addAttachedAtom(b, beginAttach);
		}
		if (endAttach > 0) {
			(invertEnds ? node1 : node2).addAttachedAtom(b, endAttach);
		}
		if (node1.isExternalPt) {
			node1.setInternalAtom(node2);
		}
		if (node2.isExternalPt) {
			node2.setInternalAtom(node1);
		}

		return addBond(b);
	}

	private CDBond addBond(CDBond b) {
		b.index = bonds.size();
		bonds.add(b);
		return b;
	}

	private int getBondTypeFromString(String s) {
		switch (s) {
		case "1":
			return StereoMolecule.cBondTypeSingle;
		case "2":
			return StereoMolecule.cBondTypeDouble;
		case "3":
			return StereoMolecule.cBondTypeTriple;
		default:
			return StereoMolecule.cBondTypeMetalLigand;
		}
	}

	/**
	 * Set the 2D or pseudo-3D coordinates of the atoms. ChemDraw pseudo-3D is just
	 * a z-layering of chunks of the molecule. Nothing really useful. These
	 * coordinates are ignored if there are any atoms also with 2D coordinates or
	 * for FILTER "NO3D". So, pretty much, the z coordinates are never used.
	 * 
	 * @param key
	 */
	private void setAtom() {
		String xy = atts.get("p");
		String[] tokens = getTokens(xy);
		double x = parseDoubleStr(tokens[0]);
		double y = parseDoubleStr(tokens[1]);
		if (x < minX)
			minX = x;
		if (x > maxX)
			maxX = x;
		if (y < minY)
			minY = y;
		if (y > maxY)
			maxY = y;
		thisAtom.set(x, y);
	}

	protected void finalizeReader(StereoMolecule mol) {
		fixConnections();
		fixInvalidAtoms();
		centerAndScale();
		createMol(mol);
	}

	private void createMol(StereoMolecule mol) {
		for (int i = bsAtoms.nextSetBit(0); i >= 0; i = bsAtoms.nextSetBit(i + 1)) {
			CDNode a = atoms.get(i);
			int ia = mol.addAtom(a.x, a.y);
			a.index = ia;
			mol.setAtomCharge(ia, a.formalCharge);
			mol.setAtomicNo(ia, a.elementNumber);
			if (a.isotope > 0)
				mol.setAtomMass(ia, a.isotope);
		}
		for (int i = bsBonds.nextSetBit(0); i >= 0; i = bsBonds.nextSetBit(i + 1)) {
			CDBond bond = bonds.get(i);	
			System.out.println("creating bond " + bond.type + " " + bond);
			int ib = mol.addBond(bond.atom1.index, bond.atom2.index);
			mol.setBondType(ib, bond.type);
		}
	}

	/**
	 * First fix all the attachments, tying together the atoms identified as
	 * ExternalConnectionPoints with atoms of bonds indicating "BeginAttach" or
	 * "EndAttach".
	 * 
	 * Then flag all unconnected atoms and also remove any wedges or hashes that are
	 * associated with bonds to atoms that also have wavy bonds.
	 */
	private void fixConnections() {

		// fix attachments for fragments

		for (int i = atoms.size(); --i >= 0;) {
			CDNode a = atoms.get(i);
			if (a.isFragment || a.hasMultipleAttachments)
				a.fixAttachments();
		}

		// indicate all atoms that are connected

		for (int i = 0, n = bonds.size(); i < n; i++) {
			CDBond b = bonds.get(i);
			if (b == null) {
				continue; // bond to nickname
			}
			CDNode a1 = atoms.get(b.atomIndex1);
			CDNode a2 = atoms.get(b.atomIndex2);
			a1.isConnected = true;
			a2.isConnected = true;
			if (b.type == ORDER_STEREO_EITHER || nostereo.contains(a1) != nostereo.contains(a2)) {
				// wavy line, so no stereo bonds here
				b.type = StereoMolecule.cBondTypeSingle;
			}
		}
	}

	/**
	 * Adjust the scale to have an average bond length of 1.45 Angstroms. This is
	 * just to get the structure in the range of other structures rather than being
	 * huge.
	 * 
	 */
	private void centerAndScale() {
		if (minX > maxX)
			return;
		double sum = 0;
		int n = 0;
		double lenH = 1;
		for (int i = bonds.size(); --i >= 0;) {
			CDNode a1 = atoms.get(bonds.get(i).atomIndex1);
			CDNode a2 = atoms.get(bonds.get(i).atomIndex2);
			double d = a1.distance(a2);
			if (a1.elementNumber > 1 && a2.elementNumber > 1) {
				sum += d;
				n++;
			} else {
				lenH = d;
			}
		}
		double f = (sum > 0 ? 1.45d * n / sum : lenH > 0 ? 1 / lenH : 1);
		// in case somehow ChemDraw uses Cartesians.
		if (f > 0.5)
			f = 1;
		f *= 10;

		double cx = (maxX + minX) / 2;
		double cy = (maxY + minY) / 2;
		for (int i = atoms.size(); --i >= 0;) {
			CDNode a = atoms.get(i);
			a.x = (a.x - cx) * f;
			a.y = (a.y - cy) * f;
		}
	}

	/**
	 * Remove fragment, external point, or invalid unconnected nodes (including
	 * unconnected carbon nodes, which can arise from deletions (in my experience)
	 * and are then not noticed because they have no associated text.
	 */
	private void fixInvalidAtoms() {
		for (int i = atoms.size(); --i >= 0;) {
			CDNode a = atoms.get(i);
			a.atomSerial = Integer.MIN_VALUE;
			if (a.isFragment || a.isExternalPt
					|| !a.isConnected && (!a.isValid || a.elementNumber == 6 || a.elementNumber == 0)) {
				// System.out.println("removing atom " + a.id + " " + a.nodeType);
				bsAtoms.clear(a.index);
			}
		}

		for (int p = 0, i = bsAtoms.nextSetBit(0); i >= 0; i = bsAtoms.nextSetBit(i + 1)) {
			CDNode a = atoms.get(i);
			a.atomSerial = ++p;
		}

		for (int i = bonds.size(); --i >= 0;) {
			CDBond b = bonds.get(i);
			if (b.atom1.atomSerial >= 0 && b.atom2.atomSerial >= 0) {
				bsBonds.set(i);
			} else {
				// fragment
			}
		}

	}

}
