package org.aksw.mssw.contact;

import java.util.ArrayList;

import android.database.AbstractCursor;
import android.util.Log;

public class ContactCursor extends AbstractCursor {

	private static final String TAG = "MsswContactCursor";
	
	ArrayList<Triple> triples;

	public ContactCursor () {
		triples = new ArrayList<Triple>();
		Log.v(TAG, "ContactCursor created.");
	}
	
	public void addTriple(Triple tripple) {
		triples.add(tripple);
		//Log.v(TAG, "added Triple. s = " + tripple.subject + " p = " + tripple.predicate + " o = " + tripple.object + ".");
	}

	public void addTriple(String subject, String predicate, String object,
			boolean oIsResource, boolean oIsBlankNode) {
		triples.add(new Triple(subject, predicate, object, oIsResource,
				oIsBlankNode));
		//Log.v(TAG, "added Triple. s = " + subject + " p = " + predicate + " o = " + object + ".");
	}

	public void addDataset() {

	}

	@Override
	public String[] getColumnNames() {
		return new String[] { "_id", "subject", "predicate", "object",
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
			return triples.get(mPos).predicate;
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
		return false;
	}

	public class Triple {
		public String subject, predicate, object;
		public boolean oIsResource, oIsBlankNode;

		public Triple(String subject, String predicate, String object,
				boolean oIsResource, boolean oIsBlankNode) {
				this.subject = subject;
				this.predicate = predicate;
				this.object = object;
				this.oIsResource = oIsResource;
				this.oIsBlankNode = oIsBlankNode;
		}
	}
	
	public void checkData() {
		Log.v(TAG, "ContactCursor Data Check!");
		for (int i = 0; i < triples.size(); i++) {
			Triple tr = triples.get(i);
			Log.v(TAG, "s = " + tr.subject + " p = " + tr.predicate + " o = " + tr.object + ".");
		}
		Log.v(TAG, "Done");
 	}

}
