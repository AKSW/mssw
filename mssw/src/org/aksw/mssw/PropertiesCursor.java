package org.aksw.mssw;

import java.util.ArrayList;

import com.hp.hpl.jena.rdf.model.Statement;

import android.database.AbstractCursor;
import android.database.Cursor;

public class PropertiesCursor extends AbstractCursor {
	
	private ArrayList<Statement> properties;
	
	public PropertiesCursor(Cursor cursor, String[] mask, boolean positiv) {
		properties = new ArrayList<Statement>();

	}

	@Override
	public String[] getColumnNames() {
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub
		String[] names = {"subject", "predicat", "objectUri", "objectLiteral"};
		return names;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return 0;
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
		return null;
	}

	@Override
	public boolean isNull(int column) {
		// TODO Auto-generated method stub
		return false;
	}

}
