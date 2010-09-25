package org.aksw.msw;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.reasoner.rulesys.GenericRuleReasoner;
import com.hp.hpl.jena.reasoner.rulesys.Rule;

public class FoafMapper {

	public static String TAG = "FoafMapper";
	private static String NOTVERSIONED_HEAD = "# A Mapping of the FOAF-Covabulary to the Datastructure of the Android addressbook";
	private static String UPDATE_HEAD = "update=yes";
	private static String VERSION_KEY = "version=";

	List<Rule> rules;
	Reasoner reasoner;

	public FoafMapper(File storage, String fileName, Context context) {

		/*
		 * TODO add comments like here:
		 * try {
				fm = new FoafMapper(storage, Constants.RULE_FILE);
			} catch (RulesetNotFoundException e) {
				if (Environment.MEDIA_MOUNTED.equals(state)) {
					Log.v(TAG, "The ruleset file does not exists at '"
							+ Constants.RULE_FILE
							+ "' will create new one with default rules.");
					fm = new FoafMapper(storage, Constants.RULE_FILE, context);
				} else {
					Log.v(TAG, "The ruleset file does not exists at '"
							+ Constants.RULE_FILE
							+ "' and can't create new one with default rules.",
							e);
				}
			}
		 */
		
		File ruleFile = new File(storage, fileName);
		
		boolean update = false;
		int latestVersion = 0;
		int installedVersion = 0;
		
		try {
			String packageName = context.getPackageName();
			PackageInfo pkgInfo = context.getPackageManager().getPackageInfo(
					packageName, 0);
			latestVersion = pkgInfo.versionCode;
		} catch (NameNotFoundException e) {
			Log.v(TAG,
					"Couldn't get information of this package assuming versionCode == 0");
		}

		Log.v(TAG,"File: " + ruleFile.getAbsolutePath() + " has following properties: exists=" + ruleFile.exists() +  " isFile=" + ruleFile.isFile() +  " isDirectory=" + ruleFile.isDirectory() +  " canRead=" + ruleFile.canRead() +  " canWrite=" + ruleFile.canWrite() +  " string=" + ruleFile.toString() +  ".");
		
		if (ruleFile.isFile()) {
			try {
				// FileReader fr = ;
				BufferedReader br = new BufferedReader(
						new FileReader(ruleFile), 8);
				String bufVal;
				bufVal = br.readLine();
				if (bufVal.equals(NOTVERSIONED_HEAD)) {
					Log.v(TAG, "Update because very old version");
					update = true;
				} else {
					if (bufVal.contains(VERSION_KEY)) {
						int offset = bufVal.indexOf(VERSION_KEY) + VERSION_KEY.length();
						installedVersion = Integer.parseInt(bufVal.substring(offset)
							.trim());
						bufVal = br.readLine();
					}
					if (bufVal.contains(UPDATE_HEAD)) {
						if (installedVersion < latestVersion) {
							Log.v(TAG, "Update because installed version outdated");
							update = true;
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
				Log.v(TAG, "Update because '" + ruleFile.getAbsolutePath() + "' file not found", e);
				update = true;
			} catch (IOException e) {
				//Log.v(TAG, "Update because '" + ruleFile.getAbsolutePath() + "' IO exception, message: '" + e.getMessage() + "'.");
				Log.v(TAG, "Update because '" + ruleFile.getAbsolutePath() + "' IO exception.", e);
				update = true;
			}
		} else {
			Log.v(TAG, "Update because '" + ruleFile.getAbsolutePath() + "' is not a file");
			update = true;
		}

		Log.v(TAG, "installedVersion=" + installedVersion + " latestVersion=" + latestVersion);
		
		if (update) {
			InputStream in = context.getResources().openRawResource(
					R.raw.defaultmapping);

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
		} else {
			Log.v(TAG, "Not updating the rules file.");
		}

		rules = Rule.rulesFromURL(ruleFile.getAbsolutePath());
		reasoner = new GenericRuleReasoner(rules);
	}

	public InfModel map(Model model) {
		InfModel infmodel = ModelFactory.createInfModel(reasoner, model);
		infmodel.prepare();

		return infmodel;
	}

}
