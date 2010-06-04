package org.aksw.natanael.msw;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
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
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mswtest);
		ListView myListView = (ListView) findViewById(R.id.ListView01);
		TextView myTextView = (TextView) findViewById(R.id.TextView01);

		final ArrayList<String> items = new ArrayList<String>();

		final ArrayAdapter<String> aa = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, items);
		myListView.setAdapter(aa);

		
		// Make the query.

		String personURI    = "http://somewhere/JohnSmith";
		String fullName     = "John Smith";

		myTextView.setText("");
		
		//ModelMaker caches = ModelFactory.createMemModelMaker();
		
		//Model cache = caches.createDefaultModel();
		

		// create an empty Model
		Model model = ModelFactory.createDefaultModel();

		String uri = "http://comiles.eu/~natanael/foaf.rdf#me";
		
		model.read(uri,"RDF/XML");
		
		// create the resource
		Resource johnSmith = model.createResource(personURI);
		Property foaf_name = new PropertyImpl("http://xmlns.com/foaf/0.1/name");

		// add the property
		johnSmith.addProperty(foaf_name, fullName);
				

		Resource res = model.getResource(uri);
		//Resource res = model.getResource(personURI);
		
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
