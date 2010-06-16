package org.aksw.mssw;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

//import org.aksw.msw.TripleProvider;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class browser extends Activity {
	/** Called when the activity is first created. */

	private static final String TAG = "msswBrowser";
	
	public static final String AUTHORITY = "org.aksw.msw.tripleprovider";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);

	private final ArrayList<String> items = new ArrayList<String>();

	private ArrayAdapter<String> aa;

	private ListView properties;
	private TextView status;
	private EditText uriInput;
	private Button loadTmpButton;
	private Button importButton;
	private Button readLocalButton;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.browser);

		aa = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, items);

		properties = (ListView) findViewById(R.id.Properties);
		status = (TextView) findViewById(R.id.Status);
		uriInput = (EditText) findViewById(R.id.UriInput);

		properties.setAdapter(aa);
		status.setText("");

		this.importButton = (Button) this.findViewById(R.id.Import);
		this.loadTmpButton = (Button) this.findViewById(R.id.Load);
		this.readLocalButton = (Button) this.findViewById(R.id.Offline);

		this.loadTmpButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				loadRes("tmp");
			}
		});

		this.importButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				loadRes("save");
			}
		});
		

		this.readLocalButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				loadRes("offline");
			}
		});

		loadRes("tmp");
	}

	public void loadRes() {
		loadRes("tmp");
	}

	public void loadRes(String mode) {

		aa.clear();

		// String uri = "http://comiles.eu/~natanael/foaf.rdf#me";
		String uri = uriInput.getText().toString();

		if (uri.length() > 0) {

			status.setText("Loading (" + mode + ") URI: <" + uri + ">.");

			try {
				String enc = "UTF-8";
				Uri contentUri;
				contentUri = Uri.parse(CONTENT_URI
						+ "/resource/" + mode + "/"
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.browser, menu);
		return true;
	}

}