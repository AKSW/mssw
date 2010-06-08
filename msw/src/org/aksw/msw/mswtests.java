package org.aksw.msw;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ModelMaker;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.SimpleSelector;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.rdf.model.impl.PropertyImpl;
import com.hp.hpl.jena.rdf.model.impl.ResourceImpl;


public class mswtests extends Activity {
	
	private final ArrayList<String> items = new ArrayList<String>();

	private ArrayAdapter<String> aa;
	
	private ListView myListView;
	private TextView myTextView;
	private Button button;
	
	private TripleProvider provider;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mswtest);
		
		 aa = new ArrayAdapter<String>(this,
					android.R.layout.simple_list_item_1, items);
		
		myListView = (ListView) findViewById(R.id.ListView01);
		myTextView = (TextView) findViewById(R.id.TextView01);

		myListView.setAdapter(aa);
		myTextView.setText("");

		provider = new TripleProvider();
		
		this.button = (Button) this.findViewById(R.id.Button01);
		this.button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				loadRes();
			}

		});
		
		loadRes();
	}
	
	public void loadRes () {
		
		aa.clear();

		String uri = "http://comiles.eu/~natanael/foaf.rdf#me";

		// add the property

		Resource res = provider.getResource(uri);
		
		String resString = res.toString();		
		
		myTextView.append(resString);

		StmtIterator stmtIt = res.listProperties();
		Statement stmt;
		while (stmtIt.hasNext()) {
			stmt = stmtIt.next();
			items.add(stmt.toString());
		}
				
		aa.notifyDataSetChanged();
		
	}
}
