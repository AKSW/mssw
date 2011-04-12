package org.aksw.mssw.triplestore;

import java.util.ArrayList;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

import android.database.AbstractCursor;
import android.util.Log;

/**
 * A Cursor, which holds Triples with one common Subject.
 * 
 * @author natanael
 * 
 */
public class TripleCursor extends AbstractCursor {

	private static final String TAG = "MsswTripleCursor";

	private ArrayList<Statement> properties;

	public TripleCursor() {
		// If you create a cursor with nothing, nothing will work
	}

	public TripleCursor(Resource subject) {
		this(subject, null, false);
	}

	public TripleCursor(Resource subject, String[] predicates) {
		this(subject, predicates, false);
	}

	public TripleCursor(Resource subject, String[] predicates, boolean complement) {
		// the cursor iterates the properties of a resource at the moment.
		// maybe it should hold a list of resources in the future.
		if (predicates == null) {
			this.properties = new ArrayList<Statement>(subject.listProperties().toList());
		} else {
			Model model = subject.getModel();
			if (model != null) {
				ArrayList<Statement> properties = new ArrayList<Statement>();
				Property property;
				
				for (String predicate : predicates) {
					property = model.getProperty(predicate);
					properties.addAll(subject.listProperties(property).toList());
				}

				if (complement) {
					this.properties = new ArrayList<Statement>(subject.listProperties().toList());
					this.properties.removeAll(properties);
				} else {
					this.properties = new ArrayList<Statement>(properties);
				}
			} else {
				// Error
			}
		}
		Log.v(TAG, "TripleCursor ready.");
	}

	@Override
	public String[] getColumnNames() {
		String[] names = { "_id", "subject", "predicate", "object",
				"subjectType", "objectType", "predicateReadable",
				"objectReadable", "oIsResource", "oIsBlankNode" };
		return names;
	}

	@Override
	public int getCount() {
		if (this.properties != null) {
			return this.properties.size();
		} else {
			return 0;
		}
	}

	@Override
	public double getDouble(int column) {
		Log.v(TAG, "getDouble.");
		return 0;
	}

	@Override
	public float getFloat(int column) {
		Log.v(TAG, "getFloat.");
		return 0;
	}

	@Override
	public int getInt(int column) {
		Log.v(TAG, "getInt from column: " + column + " at possition: '" + mPos + "'.");
		Statement stmt = properties.get(mPos);
		switch (column) {
			case 0:
				return mPos;
			case 4:
				if (stmt.getSubject().isURIResource()) {
					return 0;
				} else if (stmt.getSubject().isAnon()) {
					return 1;
				} else if (stmt.getSubject().isLiteral()) {
					return 2;
				}
			case 5:
				if (stmt.getObject().isURIResource()) {
					return 0;
				} else if (stmt.getObject().isAnon()) {
					return 1;
				} else if (stmt.getObject().isLiteral()) {
					return 2;
				}
			case 8:
				Log.v(TAG, "asks if the object is a Resource, I would say: '" + stmt.getObject().isResource() + "'.");
				if (stmt.getObject().isResource()) {
					return 1;
				} else {
					return 0;
				}
			case 9:
				Log.v(TAG, "asks if the object is a BlankNode, I would say: '" + stmt.getObject().isAnon() + "'.");
				if (stmt.getObject().isAnon()) {
					return 1;
				} else {
					return 0;
				}
			default:
				return 0;
		}
	}

	@Override
	public long getLong(int column) {
		Log.v(TAG, "getLong.");
		return 0;
	}

	@Override
	public short getShort(int column) {
		Log.v(TAG, "getShort.");
		return 0;
	}

	/**
	 * subject_type 0 - NamedResource, 1 - AnonymousResource, 2 - Literal
	 * object_type 0 - NamedResource, 1 - AnonymousResource, 2 - Literal
	 */
	@Override
	public String getString(int column) {
		// Log.v(TAG, "getString(" + column + "," + mPos + ").");
		Statement stmt = properties.get(mPos);
		switch (column) {
			case 1:
				return stmt.getSubject().getURI();
			case 2:
				return stmt.getPredicate().getURI();
			case 3:
				if (stmt.getObject().isURIResource()) {
					Resource res = (Resource) stmt.getObject();
					return res.getURI();
				} else if (stmt.getObject().isAnon()) {
					Resource res = (Resource) stmt.getObject();
					return res.getId().toString();
				} else if (stmt.getObject().isLiteral()) {
					Literal lit = (Literal) stmt.getObject();
					return lit.getLexicalForm();
				}
			case 4:
				if (stmt.getSubject().isURIResource()) {
					return "0";
				} else if (stmt.getSubject().isAnon()) {
					return "1";
				} else if (stmt.getSubject().isLiteral()) {
					return "2";
				}
			case 5:
				if (stmt.getObject().isURIResource()) {
					return "0";
				} else if (stmt.getObject().isAnon()) {
					return "1";
				} else if (stmt.getObject().isLiteral()) {
					return "2";
				}
			case 6:
				return stmt.getPredicate().getLocalName();
			case 7:
				RDFNode node = stmt.getObject();
				if (node.isLiteral()) {
					Literal lit = (Literal) node;
					return lit.getLexicalForm();
				} else if (node.isResource()) {
					// get label for this resource
					Resource res = (Resource) node;
					// TODO implement a method to search for rdfs:label, foaf:name
					// or something like that
					// return TripleProvider.getLable(res);
					return res.getURI();
				} else {
					// what could a object be, if it is neither literal, nor a
					// resource?
					// I don't know
					return null;
				}
			case 8:
				// Log.v(TAG, "asks if the object is a Resource, I would say: '"
				// + stmt.getObject().isResource() + "'.");
				return stmt.getObject().isResource() ? "true" : "false";
			case 9:
				// Log.v(TAG, "asks if the object is a BlankNode, I would say: '"
				// + stmt.getObject().isAnon() + "'.");
				return stmt.getObject().isAnon() ? "true" : "false";
			default:
				return null;
		}
	}

	@Override
	public boolean isNull(int column) {
		Log.v(TAG, "isNull.");
		return false;
	}

}
