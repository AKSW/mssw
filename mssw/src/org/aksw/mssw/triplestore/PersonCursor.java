package org.aksw.mssw.triplestore;

import java.util.LinkedList;

import org.aksw.mssw.Constants;
import org.aksw.mssw.browser.BrowserContacts.refreshCallback;

import com.hp.hpl.jena.rdf.model.Resource;

import android.content.Context;
import android.database.AbstractCursor;
import android.database.Cursor;
import android.util.Log;

public class PersonCursor extends AbstractCursor {
	private static final String TAG = "PersonCursor";

	private LinkedList<String[]> persons;
	
	private PersonCursor self;
	
	private static int MAX_THREADS = 3;
	private int counter;
	private int offset;
	private boolean done;
	
	private refreshCallback refresher;	
	
	public PersonCursor() {
		self = this;
		persons = new LinkedList<String[]>();
	}
	
	public PersonCursor(refreshCallback ref) {
		self = this;
		persons = new LinkedList<String[]>();
		refresher = ref;
	}
	
	public void addPerson(String uri, String relation, String name, String relationReadable, String picture) {
		String[] person = {uri, relation, name, relationReadable, picture};
		persons.add(person);
	}
	
	public void killThreads(){
		for(int i = 0; i < MAX_THREADS; i++){
			nameGetters[i].stop();
			nameGetters[i] = null;
		}
	}
	
	public void requestNames(Context context, String defaultResource){
		mm = new ModelManager(context, defaultResource);
		
		done = false;
		offset = 0;
		getNextNames();
	}
	
	private void getNextNames(){
		if( done ) return;
		Log.v(TAG, "getting names");
		nameGetters = new NameGetter[MAX_THREADS];
		counter = 0;
		for(int i = 0; i < MAX_THREADS; i++){
			if(offset+i < persons.size()){
				nameGetters[i] = new NameGetter();
				nameGetters[i].setNum(offset+i);
				nameGetters[i].setUri(persons.get(offset+i)[0]);
				nameGetters[i].start();
			}else{
				done = true;
				return;
			}
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
		return 0;
	}

	@Override
	public float getFloat(int column) {
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
		return 0;
	}

	@Override
	public short getShort(int column) {
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
                        String uri = "";
                        
                        /**
                         * quality is a measure of the quality of the resulting string for a name
                         * the less the better. The worst is no name in this case we will use the uri. 
                         */
                        int quality = Constants.projection.size();
                        // get name 
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
                        
                        // get image                        
                        quality = Constants.projectionImages.size(); 
                        while (rc.moveToNext()) {
                                predicate = rc.getString(rc.getColumnIndex("predicate"));
                                if (Constants.projectionImages.indexOf(predicate) < quality) {
                                        quality = Constants.projectionImages.indexOf(predicate);
                                        uri = rc.getString(rc.getColumnIndex("object"));
                                }
                        }
                        if (quality < Constants.projectionImages.size()) {
                        	persons.get(_num)[4] = uri;
                        }
                }
			} catch (Exception e) {
				persons.get(_num)[2] = _uri;
                Log.e(TAG, "Could not encode uri for query. Skipping <" + _uri + ">", e);
			}			
			Log.v(TAG, "Ready with getting Name: " + persons.get(_num)[2] + ".");
			
			Log.v(TAG, "Counter before inc: "+counter);
			counter++;
			Log.v(TAG, "Counter after inc: "+counter);
			if(counter == MAX_THREADS){
				Log.v(TAG, "counter == max. do job again");
				offset += counter;
	        	self.getNextNames();
			};
			if(refresher != null && persons.get(_num)[2] != _uri) refresher.refreshInterface();
		}
	}

}
