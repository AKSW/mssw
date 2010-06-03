package org.aksw.natanael.mssw;

import java.util.ArrayList;

import org.aksw.natanael.msw.TripleProvider;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class browser extends Activity {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.browser);
		ListView myListView = (ListView) findViewById(R.id.ListView01);

		final ArrayList<String> items = new ArrayList<String>();

		final ArrayAdapter<String> aa = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, items);
		myListView.setAdapter(aa);

		// Make the query.

		/*
		Cursor cursor = managedQuery(TripleProvider.CONTENT_URI,
				null, // Which columns to return
				null, // Which rows to return (all rows)
				null, // Selection arguments (none)
				null // Put the results in ascending order by name
		);

		startManagingCursor(cursor);
		
		int nameIdx = cursor.getColumnIndexOrThrow(TripleProvider.DISPLAY_NAME);
		//int accoIdx = cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID);

		if (cursor.moveToFirst()) {
			do {
				items.add(cursor.getString(nameIdx));
			} while (cursor.moveToNext());
		}

		stopManagingCursor(cursor);
		*/
		
		TripleProvider tp = new TripleProvider();
		Resource res = tp.getResource("http://comiles.eu/~natanael/foaf.rdf#me");
		StmtIterator stmtIt = res.listProperties();
		Statement stmt;
		while (stmtIt.hasNext()) {
			stmt = stmtIt.next();
			items.add(stmt.toString());
		}
		
		aa.notifyDataSetChanged();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.browser, menu);
		return true;
	}

}