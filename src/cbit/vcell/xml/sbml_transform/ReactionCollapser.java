/**
 * 
 */
package cbit.vcell.xml.sbml_transform;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/** Identifies and merges pairs of unidirectional reactions.
 * @author mlevin
 *
 */
class ReactionCollapser extends ASbmlTransformer {

	public static final String Name = "mergeReversibleReactions";

	public void addTransformation(String[] str) {
		super.addTransformation(str);
	}

	public String getName() {return Name;}
	
	public void transform(Document doc) {
		NodeList listOfRs = getListOfReactions(doc);
		int nReacts = listOfRs.getLength();
		if( nReacts < 2 ) return;
		
		List<ReactStep> rsList = new ArrayList<ReactStep>();
		for( int i = 0; i < nReacts; ++i ) {
			Element r = (Element)listOfRs.item(i);
			ReactStep rs = ReactStep.makePotentiallyReversible(r);
			if( null == rs) continue;

			//look for reverse reaction
			scope:{
				for( int j = 0, max = rsList.size(); j < max; ++j ) {
					ReactStep rss = rsList.get(j);
					if( rss.merge(rs, doc) ) {
						break scope;
					}
				}
				//reverse not found
				rsList.add(rs);
			}//scope
		}
		
		for( int i = 0, max = rsList.size(); i < max; ++i ) {
			rsList.get(i).commitToDom();
		}
		doc.normalizeDocument();
	}

	private NodeList getListOfReactions(Document doc) {
		return doc.getElementsByTagName(SbmlElements.React_tag);
	}

	public int countTransformations() {
		return 0;
	}

	public String[] getTransformation(int i) {
		return new String[0];
	}

	public void removeTransformation(int i) {
		throw new IndexOutOfBoundsException("no transformations stored");
	}
	

}
