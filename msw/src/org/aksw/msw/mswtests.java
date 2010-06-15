package org.aksw.msw;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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

	private static final String TAG = "mswtest";

	private final ArrayList<String> items = new ArrayList<String>();

	private ArrayAdapter<String> aa;

	private ListView properties;
	private TextView status;
	private EditText uriInput;
	private Button importButton;
	private Button loadTmpButton;

	// private TripleProvider provider;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mswtest);

		aa = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, items);

		properties = (ListView) findViewById(R.id.Properties);
		status = (TextView) findViewById(R.id.Status);
		uriInput = (EditText) findViewById(R.id.UriInput);

		properties.setAdapter(aa);
		status.setText("");

		this.importButton = (Button) this.findViewById(R.id.Import);
		this.loadTmpButton = (Button) this.findViewById(R.id.Load);

		this.loadTmpButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				loadRes(true);
			}

		});

		this.importButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				loadRes();
			}

		});

		loadRes(true);
	}

	public void loadRes() {
		loadRes(false);
	}

	public void loadRes(Boolean tmp) {

		aa.clear();

		// String uri = "http://comiles.eu/~natanael/foaf.rdf#me";
		String uri = uriInput.getText().toString();

		if (uri.length() > 0) {

			// String uri = "hm";
			String enc = "UTF-8";

			String persistence;
			if (!tmp) {
				persistence = "save";
			} else {
				persistence = "tmp";
			}

			status.setText("Loading (" + persistence + ") URI: <" + uri + ">.");

			try {
				Uri contentUri;
				contentUri = Uri.parse(TripleProvider.CONTENT_URI
						+ "/resource/" + persistence + "/"
						+ URLEncoder.encode(uri, enc));

				// ResourceCursor rc = (ResourceCursor) managedQuery(contentUri,
				// null,
				// null, null, null);

				Log.v(TAG, "Starting Query with uri: <" + contentUri.toString()
						+ ">.");

				Cursor rc = managedQuery(contentUri, null, null, null, null);

				if (rc != null) {
					String[] cNames = rc.getColumnNames();

					for (String name : cNames) {
						items.add(name);
					}
				} else {
					status.setText("Error with Query.");
				}

			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				items.add("error retriving Data from Contentprovider");
			}
		} else {
			status.append("No URI inserted.");
		}

		aa.notifyDataSetChanged();

	}
}
