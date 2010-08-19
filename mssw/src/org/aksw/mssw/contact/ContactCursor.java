package org.aksw.mssw.contact;

import java.util.ArrayList;

import android.database.AbstractCursor;

public class ContactCursor extends AbstractCursor {

	ArrayList<Triple> triples;

	public void addTriple(Triple tripple) {
		triples.add(tripple);
	}

	public void addTriple(String subject, String predicat, String object,
			boolean oIsResource, boolean oIsBlankNode) {
		triples.add(new Triple(subject, predicat, object, oIsResource,
				oIsBlankNode));
	}

	public void addDataset() {

	}

	@Override
	public String[] getColumnNames() {
		return new String[] { "_id", "subject", "predicat", "object",
				"oIsResource", "oIsBlankNode" };
	}

	@Override
	public int getCount() {
		return triples.size();
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
		case 4:
			return triples.get(mPos).oIsResource ? 1 : 0;
		case 5:
			return triples.get(mPos).oIsBlankNode ? 1 : 0;
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
		switch (column) {
		case 1:
			return triples.get(mPos).subject;
		case 2:
			return triples.get(mPos).predicat;
		case 3:
			return triples.get(mPos).object;
		case 4:
			return triples.get(mPos).oIsResource ? "true" : "false";
		case 5:
			return triples.get(mPos).oIsBlankNode ? "true" : "false";
		default:
			return null;
		}
	}

	@Override
	public boolean isNull(int column) {
		switch (column) {
		case 4:
			return !triples.get(mPos).oIsResource;
		case 5:
			return !triples.get(mPos).oIsBlankNode;
		default:
			return false;
		}
	}

	public class Triple {
		public String subject, predicat, object;
		public boolean oIsResource, oIsBlankNode;

		public Triple(String subject, String predicat, String object,
				boolean oIsResource, boolean oIsBlankNode) {

		}
	}

}
