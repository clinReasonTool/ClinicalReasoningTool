package actions.beanActions;

import java.util.*;
import beans.relation.Relation;

//simport beans.relation.Relation;

public interface DelAction {

	void save(Object rel, List rels);
	/**
	 * A log entry for the delete action is created and saved in a Log object
	 */
	void notifyLog(Object o);
	
	/**
	 * Delete the bean from the database.
	 * @param id
	 */
	void delete(String id);
	
	/**
	 * we remove any edge weight (implicit and explicit) from the MultiEdges for this Relation. 
	 * @param rel
	 */
	void updateGraph(Relation rel, List rels);
}
