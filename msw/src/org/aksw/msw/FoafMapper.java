package org.aksw.msw;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import android.content.Context;

import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.reasoner.rulesys.GenericRuleReasoner;
import com.hp.hpl.jena.reasoner.rulesys.Rule;

public class FoafMapper {

	public static String TAG = "FoafMapper";

	List<Rule> rules;
	Reasoner reasoner;

	public FoafMapper(Context context) {
		InputStream in = context.getResources().openRawResource(
				R.raw.defaultmapping);

		BufferedReader br = new BufferedReader(new InputStreamReader(in), 8);
		rules = Rule.parseRules(Rule.rulesParserFromReader(br));
		reasoner = new GenericRuleReasoner(rules);
	}

	public FoafMapper(String fileName) {
		rules = Rule.rulesFromURL(fileName);
		reasoner = new GenericRuleReasoner(rules);
	}

	public InfModel map(Model model) {
		InfModel infmodel = ModelFactory.createInfModel(reasoner, model);
		infmodel.prepare();

		return infmodel;
	}

}
