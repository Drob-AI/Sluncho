package net.asteasolutions.cinusuidi.sluncho.bot.postPipelineProcessors;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.asteasolutions.cinusuidi.sluncho.bot.Query;
import net.asteasolutions.cinusuidi.sluncho.bot.errorCorrection.NamedEntity;
import net.asteasolutions.cinusuidi.sluncho.bot.errorCorrection.NamedEntityCorrector;
import net.asteasolutions.cinusuidi.sluncho.questionparser.AnnotationType;
import net.asteasolutions.cinusuidi.sluncho.questionparser.QueryToken;

public class LuceneNamedEntityCorrector implements IPostPipelineProcessor {
	private Query correctNamedEntities(Query q, NamedEntityCorrector corrector) {
		Query result = q; //clone
		return result;
	}
	@Override
	public ArrayList<Query> expand(ArrayList<Query> queries) {
		ArrayList<Query> result = new ArrayList<Query>();
		NamedEntityCorrector corrector = new NamedEntityCorrector();
		Iterator<Query> iter = queries.iterator();
		while(iter.hasNext()) {
			Query q = iter.next();
			result.add(correctNamedEntities(q, corrector));
			//result.add(q);
		}
		return result;
	}
}
