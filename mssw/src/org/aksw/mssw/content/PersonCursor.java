package org.aksw.mssw.content;

import java.util.LinkedList;
import java.util.Map.Entry;

import android.database.AbstractCursor;

public class PersonCursor extends AbstractCursor {

	LinkedList<String[]> persons;

	public PersonCursor() {
		persons = new LinkedList<String[]>();
		
	}
	
	public void addPerson(String uri, String relation, String name, String relationReadable, String picture) {
		String[] person = {uri, relation, name, relationReadable, picture};
		persons.add(person);
	}

	@Override
	public String[] getColumnNames() {
		String[] columns = { "_id", "webid", "relation", "name",
				"relationReadable", "picture" };
		return columns;
	}

	@Override
	public int getCount() {
		return persons.size();
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
		// { "_id", "uri", "relation", "name",
		// "relationReadable", "picture" };
		if (column == 0) {
			return "" + mPos;
		} else if (0 < column && column < persons.get(mPos).length) {
			return persons.get(mPos)[column-1];
		} else {
			return null;
		}
	}

	@Override
	public boolean isNull(int column) {
		// TODO Auto-generated method stub
		return false;
	}

}
