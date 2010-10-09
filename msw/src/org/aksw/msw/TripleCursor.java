package org.aksw.msw;

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

	public TripleCursor(Resource subject) {
		this(subject, null, false);
	}

	public TripleCursor(Resource subject, String[] predicates) {
		this(subject, predicates, false);
	}

	public TripleCursor(Resource subject, String[] predicates,
			boolean complement) {
		// the cursor iterates the properties of a resource at the moment.
		// maybe it should hold a list of resources in the future.
		if (predicates == null) {
			this.properties = new ArrayList<Statement>(subject.listProperties()
					.toList());
		} else {
			Model model = subject.getModel();
			if (model != null) {
				ArrayList<Statement> properties = new ArrayList<Statement>();
				Property property;

				for (String predicate : predicates) {
					property = model.getProperty(predicate);

					properties
							.addAll(subject.listProperties(property).toList());
				}

				if (complement) {
					this.properties = new ArrayList<Statement>(subject
							.listProperties().toList());
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
				"predicateReadable", "objectReadable", "oIsResource",
				"oIsBlankNode"};
		return names;
	}

	@Override
	public int getCount() {
		return this.properties.size();
	}

	@Override
	public double getDouble(int column) {
		Log.v(TAG, "getDouble.");
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float getFloat(int column) {
		Log.v(TAG, "getFloat.");
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getInt(int column) {
		Log.v(TAG, "getInt from column: " + column + " at possition: '" + mPos
				+ "'.");
		Statement stmt = properties.get(mPos);
		switch (column) {
		case 0:
			return mPos;
		case 6:
			Log.v(TAG, "asks if the object is a Resource, I would say: '"
					+ stmt.getObject().isResource() + "'.");
			if (stmt.getObject().isResource()) {
				return 1;
			} else {
				return 0;
			}
		case 7:
			Log.v(TAG, "asks if the object is a BlankNode, I would say: '"
					+ stmt.getObject().isAnon() + "'.");
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
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public short getShort(int column) {
		Log.v(TAG, "getShort.");
		// TODO Auto-generated method stub
		return 0;
	}

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
			return stmt.getPredicate().getLocalName();
		case 5:
			RDFNode node = stmt.getObject();
			if (node.isLiteral()) {
				Literal lit = (Literal) node;
				return lit.getLexicalForm();
			} else if (node.isResource()) {
				// get label for this resource
				Resource res = (Resource) node;
				// TODO implement a method to search for rdfs:label, foaf:name
				// or something like that
				return TripleProvider.getLable(res);
			} else {
				// what could a object be, if it is neither literal, nor a
				// resource?
				// I don't know
				return null;
			}
		case 6:
			// Log.v(TAG, "asks if the object is a Resource, I would say: '"
			// + stmt.getObject().isResource() + "'.");
			return stmt.getObject().isResource() ? "true" : "false";
		case 7:
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
