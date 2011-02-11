package org.aksw.mssw.triplestore;

import java.io.File;
import java.util.List;

import android.util.Log;

import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.reasoner.rulesys.GenericRuleReasoner;
import com.hp.hpl.jena.reasoner.rulesys.Rule;

public class FoafMapper {

	private static String TAG = "FoafMapper";

	private List<Rule> rules;
	private Reasoner reasoner;

	public FoafMapper(File storage, String fileName) {
		File ruleFile = new File(storage, fileName);

		if (ruleFile.exists()) {
			rules = Rule.rulesFromURL(ruleFile.getAbsolutePath());
			reasoner = new GenericRuleReasoner(rules);
		} else {
			Log.e(TAG, "There is no rules-file (" + ruleFile.getAbsolutePath()
					+ "), maybe the update checker failed.");
		}
	}

	public Model map(Model model) {
		if (reasoner != null) {
			InfModel infmodel = ModelFactory.createInfModel(reasoner, model);
			infmodel.prepare();

			return infmodel;
		} else {
			Log.e(TAG, "No reasoner specified");
			return model;
		}
	}

}
