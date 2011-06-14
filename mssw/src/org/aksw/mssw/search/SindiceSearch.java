package org.aksw.mssw.search;

import org.aksw.mssw.triplestore.PersonCursor;

import android.util.Log;

import com.sindice.Sindice;
import com.sindice.result.SearchResult;
import com.sindice.result.SearchResults;

public class SindiceSearch {
	
	private static final String TAG = "SindiceSearch";
	
	private Sindice sindice;
	
	public SindiceSearch(){
		sindice = new Sindice();
	}
	
	public PersonCursor findTerm(String term){
		PersonCursor pc;
		try{
			SearchResults res = sindice.termSearch(term);
			pc = new PersonCursor();
			
			for(SearchResult searchResult : res) {
				pc.addPerson(searchResult.getLink(), null, searchResult.getTitle(), null, null);
			}
		}catch(Exception e){
			pc = null;
			Log.v(TAG, "Sindice search error");
		}
		return pc;
	}
}
