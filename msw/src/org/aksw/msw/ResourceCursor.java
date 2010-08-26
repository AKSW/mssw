/**
 * 
 */
package org.aksw.msw;

import java.util.ArrayList;

import android.database.AbstractCursor;
import android.util.Log;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.rdf.model.impl.StmtIteratorImpl;

/**
 * @author natanael
 * 
 */
public class ResourceCursor extends AbstractCursor {

	private static final String TAG = "ResourceCursor";

	private ArrayList<Statement> properties;
	private Resource subject;
	private Resource[] subjects;

	public ResourceCursor(Resource subject) {
		this.subjects[0] = subject;
		
		// the cursor iterates the properties of a resource at the moment.
		// maybe it should hold a list of resources in the future.
		this.properties = new ArrayList<Statement>(subject.listProperties()
				.toList());
	}
	
	public ResourceCursor(Resource[] subject) {
		this.subjects = subject;
		//this.subject = new ArrayList<Statement>(subject.listProperties()
		//		.toList());
	}
	
	@Override
	public String[] getColumnNames() {

		try {
			ArrayList<String> cols = new ArrayList<String>();
			StmtIterator it = new StmtIteratorImpl(this.properties.iterator());
			while (it.hasNext()) {
				String prop = it.next().getPredicate().getURI();
				cols.add(prop);
				Log.v(TAG, "Adding <" + prop + "> to column List.");
			}

			return cols.toArray(new String[0]);
		} catch (Exception e) {
			Log.v(TAG, "An Exception occured", e);
			return null;
		}
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return 1;
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
		return 0;
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
		// TODO Auto-generated method stub
		Statement stmt = properties.get(column);
		try {
			int type = getType(column);
			switch(type) {
			case LITERAL:
				return stmt.getString();
			case NAMED_RESOURCE:
				Resource res = (Resource) stmt.getObject();
				return res.getURI();
			default:
				return "BlankNode or Nothing";
			}
		} catch (Exception e) {
			Log.v(TAG, "The statement in column " + column + " threw an exception.", e);
			return null;
		}
	}
	
	//---- my methods ----

	public Resource getObject(int column) {
		// TODO Auto-generated method stub
		Statement stmt = properties.get(column);
		try {
			return (Resource) stmt.getObject();
		} catch (Exception e) {
			return null;
		}
	}

	public String getUri(int column) {
		Statement stmt = properties.get(column);
		try {
			if (stmt.getObject().isURIResource()) {
				Resource obj = (Resource) stmt.getObject();
				return obj.getURI();
			} else {
				return null;
			}
		} catch (Exception e) {
			return null;
		}
	}

	public static final int UNDEFINED = 0;
	public static final int LITERAL = 10;
	public static final int RESOURCE = 0; // 20, unused
	public static final int BLANK_NODE = 21;
	public static final int NAMED_RESOURCE = 22;

	public int getType(int column) {
		Statement stmt = properties.get(column);
		if (stmt.getObject().isLiteral()) {
			return LITERAL;
		} else if (stmt.getObject().isAnon()) {
			return BLANK_NODE;
		} else if (stmt.getObject().isURIResource()) {
			return NAMED_RESOURCE;
		} else if (stmt.getObject().isResource()) {
			// is impossible, because a resource either has a name or is
			// anonymous
			return RESOURCE;
		} else {
			return 0;
		}

	}

	@Override
	public boolean isNull(int column) {
		// TODO Auto-generated method stub
		return false;
	}

}
