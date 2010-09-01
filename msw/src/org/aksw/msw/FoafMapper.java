package org.aksw.msw;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import android.content.Context;
import android.util.Log;

import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.reasoner.rulesys.GenericRuleReasoner;
import com.hp.hpl.jena.reasoner.rulesys.Rule;
import com.hp.hpl.jena.shared.RulesetNotFoundException;

public class FoafMapper {

	public static String TAG = "FoafMapper";

	List<Rule> rules;
	Reasoner reasoner;

	public FoafMapper(File storage, String fileName, Context context) {
		InputStream in = (BufferedInputStream) context.getResources()
				.openRawResource(R.raw.defaultmapping);

		File ruleFile = new File(storage, fileName);
		FileOutputStream out = null;
		try {
			ruleFile.getParentFile().mkdirs();
			if (!ruleFile.exists()) {
				ruleFile.createNewFile();
			}

			out = new FileOutputStream(ruleFile);

			byte[] b = new byte[0xfff];

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

		rules = Rule.rulesFromURL(ruleFile.getAbsolutePath());
		reasoner = new GenericRuleReasoner(rules);
	}

	public FoafMapper(File storage, String fileName)
			throws RulesetNotFoundException {
		fileName = new File(storage, fileName).getAbsolutePath();
		rules = Rule.rulesFromURL(fileName);
		reasoner = new GenericRuleReasoner(rules);
	}

	public InfModel map(Model model) {
		InfModel infmodel = ModelFactory.createInfModel(reasoner, model);
		infmodel.prepare();

		return infmodel;
	}

}
