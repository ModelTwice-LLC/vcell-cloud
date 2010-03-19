package org.vcell.sybil.actions.graph.layout;

/*   GraphCircularLayoutAction  --- by Oliver Ruebenacker, UCHC --- July 2007 to February 2009
 *   Perform circular layout
 */

import java.awt.event.ActionEvent;

import org.vcell.sybil.actions.ActionSpecs;
import org.vcell.sybil.actions.BaseAction;
import org.vcell.sybil.actions.CoreManager;
import org.vcell.sybil.util.graphlayout.LayoutType;

public class GraphCircularLayoutAction extends BaseAction {

	private static final long serialVersionUID = 3185114990608665398L;

	public GraphCircularLayoutAction(ActionSpecs newSpecs, CoreManager coreManager) {
		super(newSpecs, coreManager);
	}
	public void actionPerformed(ActionEvent event) {
		coreManager().graphSpace().layoutGraph(LayoutType.Circularizer);
	}


}
