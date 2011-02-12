package org.aksw.mssw.update;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;

import org.aksw.mssw.Constants;
import org.aksw.mssw.R;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Environment;
import android.util.Log;

public class RulesCheck extends UpdateCheck {

	private static String TAG = "RulesCheck";
	
	/**
	 * The typo "FOAF-Covabulary" is correct, because it is also in the first (not-versioned) rule-files
	 */
	private static String NOTVERSIONED_HEAD = "# A Mapping of the FOAF-Covabulary to the Datastructure of the Android addressbook";
	private static String UPDATE_HEAD = "consistency=yes";
	private static String VERSION_KEY = "version=";

	private static boolean fileChecked = false;
	private static boolean consistency = false;


	private File storage;
	private File ruleFile;
	private int latestVersion = 0;
	private int installedVersion = 0;

	
	@Override
	public void setContext(Context context) {
		this.context = context;
	}


	/*
	 * TODO add comments like here: try { fm = new FoafMapper(storage,
	 * Constants.RULE_FILE); } catch (RulesetNotFoundException e) { if
	 * (Environment.MEDIA_MOUNTED.equals(state)) { Log.v(TAG,
	 * "The ruleset file does not exists at '" + Constants.RULE_FILE +
	 * "' will create new one with default rules."); fm = new
	 * FoafMapper(storage, Constants.RULE_FILE, context); } else {
	 * Log.v(TAG, "The ruleset file does not exists at '" +
	 * Constants.RULE_FILE +
	 * "' and can't create new one with default rules.", e); } }
	 */

	
	@Override
	public boolean isConsistent() {
		if (!RulesCheck.fileChecked) {
			// check file
			this.checkUpdateŃeccessarity();
		}
		return RulesCheck.consistency;
	}

	@Override
	public void configure() throws MswUpdateException {
		Log.v(TAG, "configure RulesCheck");

		if (!consistency) {
			InputStream in = context.getResources().openRawResource(R.raw.defaultmapping);

			FileOutputStream out = null;

			try {
				ruleFile.getParentFile().mkdirs();
				if (!ruleFile.exists()) {
					ruleFile.createNewFile();
				} else if (ruleFile.isDirectory()) {
					throw new IOException(
							"There is a directory with the rulefiles name. Please rename or remove this directory.");
				}

				out = new FileOutputStream(ruleFile);

				byte[] b = new byte[0xfff];

				String nl = System.getProperty("line.separator");

				String head = new String();
				head += "# " + VERSION_KEY + latestVersion + nl;
				head += "# " + UPDATE_HEAD + nl;
				head += "# " + nl;
				out.write(head.getBytes());

				for (int len; (len = in.read(b)) != -1;) {
					out.write(b, 0, len);
				}

			} catch (IOException e) {
				Log.e(TAG, "Could not create Rulefile on external storrage.", e);
			} finally {
				if (out != null) {
					try {
						out.close();
					} catch (IOException e) {
						// could not close file
					}
				}
				if (in != null) {
					try {
						in.close();
					} catch (IOException e) {
						// could not close file
					}
				}
			}
			RulesCheck.consistency = true;
		} else {
			Log.v(TAG, "Not updating the rules file.");
		}

	}
	
	private boolean checkUpdateŃeccessarity() {

		storage = Environment.getExternalStorageDirectory();
		ruleFile = new File(storage, Constants.RULE_FILE);

		try {
			String packageName = context.getPackageName();
			PackageInfo pkgInfo = context.getPackageManager().getPackageInfo(
					packageName, 0);
			
			// get current installed version of SemanticWebCore (msw)
			latestVersion = pkgInfo.versionCode;
		} catch (NameNotFoundException e) {
			Log.v(TAG,
					"Couldn't get information of this package assuming versionCode == 0");
		}
		
		Log.v(TAG,
				"File: " + ruleFile.getAbsolutePath()
						+ " has following properties: exists="
						+ ruleFile.exists() + " isFile=" + ruleFile.isFile()
						+ " isDirectory=" + ruleFile.isDirectory()
						+ " canRead=" + ruleFile.canRead() + " canWrite="
						+ ruleFile.canWrite() + " string="
						+ ruleFile.toString() + ".");
		
		consistency = true;
		
		if (ruleFile.isFile()) {
			try {
				BufferedReader br = new BufferedReader(
						new FileReader(ruleFile), 8);
				String bufVal;
				bufVal = br.readLine();
				if (bufVal.equals(NOTVERSIONED_HEAD)) {
					Log.v(TAG, "Update because very old version");
					consistency = false;
				} else {
					if (bufVal.contains(VERSION_KEY)) {
						int offset = bufVal.indexOf(VERSION_KEY)
								+ VERSION_KEY.length();
						installedVersion = Integer.parseInt(bufVal.substring(
								offset).trim());
						bufVal = br.readLine();
						// true
					}
					if (bufVal.contains(UPDATE_HEAD)) {
						if (installedVersion < latestVersion) {
							Log.v(TAG,
									"Update because installed version outdated");
							consistency = false;
						}
					}
				}

				if (br != null) {
					try {
						br.close();
					} catch (IOException e) {
						// could not close file
					}
				}
			} catch (FileNotFoundException e) {
				Log.v(TAG, "Update because '" + ruleFile.getAbsolutePath()
						+ "' file not found", e);
				consistency = false;
			} catch (IOException e) {
				// Log.v(TAG, "Update because '" + ruleFile.getAbsolutePath() +
				// "' IO exception, message: '" + e.getMessage() + "'.");
				Log.v(TAG, "Update because '" + ruleFile.getAbsolutePath()
						+ "' IO exception.", e);
				consistency = false;
			}
		} else {
			Log.v(TAG, "Update because '" + ruleFile.getAbsolutePath()
					+ "' is not a file");
			consistency = false;
		}

		Log.v(TAG, "installedVersion=" + installedVersion + " latestVersion="
				+ latestVersion);
		
		RulesCheck.fileChecked = true;
		return consistency;
	}

}
