/** 
 * TreeOntology.java - A class for abstracting names based on a tree structure
 * given in an XML file.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 * 
 * Company: HWU
 * Project: LIREC
 * Created: 12/12/11
 * @author: Matthias Keysermann
 * Email to: muk7@hw.ac.uk
 * 
 * History: 
 * Matthias Keysermann: 12/12/11 - File created
 * **/

package FAtiMA.AdvancedMemory.ontology;

import java.io.File;
import java.io.Serializable;
import java.util.LinkedList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class TreeOntology implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final boolean ITERATIVE_DEPTH_LIMIT = false;

	private String filename;

	private Document doc;

	private int depthMax;

	public TreeOntology(String filename) {
		this.filename = filename;
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			if (filename == null) {
				System.err.println("No filename for ontology provided!");
				doc = db.newDocument();
			} else {
				File file = new File(filename);
				if (!file.exists()) {
					System.err.println("File does not exist: " + filename);
					doc = db.newDocument();
				} else {
					doc = db.parse(file);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public int getDepthMax() {
		return depthMax;
	}

	public void setDepthMax(int depthMax) {
		this.depthMax = depthMax;
	}

	/**
	 * Returns the closest common ancestors for the two given name, iteratively
	 * increasing the depth limit. Stops if common ancestors are found for the
	 * current depth limit or the given maximum depth is reached.
	 * 
	 * @param nameA
	 *            the first name
	 * @param nameB
	 *            the second name
	 * @return list of common ancestors
	 */
	public LinkedList<String> getClosestCommonAncestors(String nameA, String nameB) {

		// set starting depth
		int depthLimitStart = depthMax;
		if (ITERATIVE_DEPTH_LIMIT) {
			depthLimitStart = 0;
		}

		// iteratively increase depth limit
		for (int depthLimit = depthLimitStart; depthLimit <= depthMax; depthLimit++) {

			// initialise
			LinkedList<String> ancestors = new LinkedList<String>();

			// find all nodes with corresponding location names
			NodeList nodeListA = doc.getDocumentElement().getElementsByTagName(nameA);
			NodeList nodeListB = doc.getDocumentElement().getElementsByTagName(nameB);

			// check if both such nodes exist
			if (nodeListA.getLength() > 0 && nodeListB.getLength() > 0) {

				// loop over all combinations
				for (int i = 0; i < nodeListA.getLength(); i++) {
					for (int j = 0; j < nodeListB.getLength(); j++) {

						Node nodeA = nodeListA.item(i);
						Node nodeB = nodeListB.item(j);
						Node ancestorNode = getClosestCommonAncestor(nodeA, nodeB, depthLimit);

						if (ancestorNode == null) {
							// check if nodes are the same
							if (nodeA.equals(nodeB)) {
								ancestors.add(nodeA.getNodeName());
							}
						} else {
							// add closest common ancestor to list
							ancestors.add(ancestorNode.getNodeName());
						}
					}
				}

			} else {

				// At least one of the names is not contained in the tree. 
				// Compare the two names in order to return an ancestor. 

				// check if names are them same
				if (nameA.equals(nameB)) {
					ancestors.add(nameA);
				}

			}

			// stop if common ancestors are found or maximum depth is reached
			if (ancestors.size() > 0 || depthLimit == depthMax) {
				return ancestors;
			}

		}

		return null;
	}

	/*
	 * return the closest common ancestor node of the two given nodes within the
	 * given depth limit
	 */
	private Node getClosestCommonAncestor(Node nodeA, Node nodeB, int depthLimit) {

		// create list of parent nodes of node A
		Node nodeTempA = nodeA.getParentNode();
		LinkedList<Node> parentNodesA = new LinkedList<Node>();
		int depthA = 1;
		// loop until no parent exists or maximum depth is reached
		while (nodeTempA != null && depthA <= depthLimit) {
			parentNodesA.add(nodeTempA);
			nodeTempA = nodeTempA.getParentNode();
			depthA++;
		}

		// create list of parent nodes of node B
		Node nodeTempB = nodeB.getParentNode();
		LinkedList<Node> parentNodesB = new LinkedList<Node>();
		int depthB = 1;
		// loop until no parent exists or maximum depth is reached
		while (nodeTempB != null && depthB <= depthLimit) {
			parentNodesB.add(nodeTempB);
			nodeTempB = nodeTempB.getParentNode();
			depthB++;
		}

		// compare lists
		for (int i = 0; i < parentNodesA.size(); i++) {
			for (int j = 0; j < parentNodesB.size(); j++) {
				Node nodeI = parentNodesA.get(i);
				Node nodeJ = parentNodesB.get(j);
				if (nodeI.equals(nodeJ)) {
					return nodeI;
				}
			}
		}

		return null;
	}

}
