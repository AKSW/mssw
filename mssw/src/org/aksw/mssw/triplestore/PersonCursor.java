package org.aksw.mssw.triplestore;

import java.util.LinkedList;

import org.aksw.mssw.Constants;

import com.hp.hpl.jena.rdf.model.Resource;

import android.content.Context;
import android.database.AbstractCursor;
import android.database.Cursor;
import android.util.Log;

public class PersonCursor extends AbstractCursor {
	private static final String TAG = "PersonCursor";

	private LinkedList<String[]> persons;

	public PersonCursor() {
		persons = new LinkedList<String[]>();
		
	}
	
	public void addPerson(String uri, String relation, String name, String relationReadable, String picture) {
		String[] person = {uri, relation, name, relationReadable, picture};
		persons.add(person);
	}
	
	public void requestNames(Context context, String defaultResource){
		mm = new ModelManager(context, defaultResource);
		
		nameGetters = new NameGetter[persons.size()];
		for(int i = 0; i < persons.size(); i++){
			nameGetters[i] = new NameGetter();
			nameGetters[i].setNum(i);
			nameGetters[i].setUri(persons.get(i)[0]);
			nameGetters[i].start();
		}
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
	
	
	
	// name request
	private NameGetter[] nameGetters;
	private static ModelManager mm;
	
	private class NameGetter extends Thread {
		private int _num;
		private String _uri;
		
		public void setNum(int num){
			_num = num;
		}
		
		public void setUri(String uri){
			_uri = uri;
		}
		
		public void run() {
			try {
                Resource res = mm.getModel(_uri, false, true).getResource(_uri);
                Cursor rc = new TripleCursor(res, Constants.projection.toArray(new String[] {}), false);
                
                if (rc != null) {
                        String predicate;
                        String name = "";
                        
                        /**
                         * quality is a measure of the quality of the resulting string for a name
                         * the less the better. The worst is no name in this case we will use the uri. 
                         */
                        int quality = Constants.projection.size();
                        while (rc.moveToNext()) {
                                predicate = rc.getString(rc.getColumnIndex("predicate"));
                                Log.v(TAG,"Got name '" + rc.getString(
                                		rc.getColumnIndex("object"))+ "' with güfak: "+ 
                                		Constants.projection.indexOf(predicate)
                                );
                                if (Constants.projection.indexOf(predicate) < quality) {
                                        quality = Constants.projection.indexOf(predicate);
                                        name = rc.getString(rc.getColumnIndex("object"));
                                }
                        }
                        if (quality < Constants.projection.size()) {
                        	persons.get(_num)[2] = name;
                        }else{
                        	persons.get(_num)[2] = _uri;
                        }
                }
			} catch (Exception e) {
				persons.get(_num)[2] = _uri;
                Log.e(TAG, "Could not encode uri for query. Skipping <" + _uri + ">", e);
			}
			Log.v(TAG, "Ready with getting Name: " + persons.get(_num)[2] + ".");
		}
	}
	// Create runnable for posting
    final Runnable mUpdateNames = new Runnable() {
        public void run() {
        	updateNames();
        }
    };
	private void updateNames(){
		Log.v(TAG, "updating names list");
		
		this.notifyAll();
	}

}
