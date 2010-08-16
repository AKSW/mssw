package org.aksw.mssw.content;

import java.util.ArrayList;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

import android.database.AbstractCursor;
import android.database.Cursor;

public class PropertyCursor extends AbstractCursor {
	
	private String[] property;
	
	public PropertyCursor(String subject, String predicat, String object) {
		property = new String[]{subject, predicat, object};
	}

	@Override
	public String[] getColumnNames() {
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub
		String[] names = {"_id", "subject", "predicat", "object", "predicatReadable", "objectReadable"};
		return names;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return 1;
	}

	@Override
	public double getDouble(int arg0) {
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
		switch (column) {
		case 1:
			return property[0];
		case 2:
			return property[1];
		case 3:
			return property[2];
		case 4:
			return property[1];
		case 5:
			return property[2];
		default:
			return null;
		}
	}

	@Override
	public boolean isNull(int column) {
		// TODO Auto-generated method stub
		return false;
	}

}
