package org.aksw.msw;

import java.util.ArrayList;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

import android.database.AbstractCursor;

/**
 * A Cursor, which holds Triples with one common Subject.
 * 
 * @author natanael
 * 
 */
public class TripleCursor extends AbstractCursor {

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
	}

	@Override
	public String[] getColumnNames() {
		// TODO Auto-generated method stub
		String[] names = { "_id", "subject", "predicat", "object", "predicatReadable", "objectReadable", "oIsResource", "oIsBlankNode" };
		return names;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return this.properties.size();
	}

	@Override
	public double getDouble(int column) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float getFloat(int column) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getInt(int column) {
		// TODO Auto-generated method stub
		switch (column) {
		case 0:
			return mPos;
		default:
			return 0;
		}
	}

	@Override
	public long getLong(int column) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public short getShort(int column) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getString(int column) {
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
		default:
			return null;
		}
	}

	@Override
	public boolean isNull(int column) {
		Statement stmt = properties.get(mPos);
		switch (column) {
		case 6:
			return !stmt.getObject().isResource();
		case 7:
			return !stmt.getObject().isAnon();
		default:
			return false;
		}
	}

}
