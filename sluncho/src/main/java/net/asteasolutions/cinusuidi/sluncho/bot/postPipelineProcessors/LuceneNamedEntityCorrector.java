package net.asteasolutions.cinusuidi.sluncho.bot.postPipelineProcessors;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.asteasolutions.cinusuidi.sluncho.bot.CompositeQuery;

import net.asteasolutions.cinusuidi.sluncho.bot.Query;
import net.asteasolutions.cinusuidi.sluncho.bot.errorCorrection.NamedEntity;
import net.asteasolutions.cinusuidi.sluncho.bot.errorCorrection.NamedEntityCorrector;
import net.asteasolutions.cinusuidi.sluncho.questionparser.AnnotationType;
import net.asteasolutions.cinusuidi.sluncho.questionparser.QueryToken;

public class LuceneNamedEntityCorrector implements IPostPipelineProcessor {
	private CompositeQuery correctNamedEntities(CompositeQuery q, NamedEntityCorrector corrector) {
		CompositeQuery result = q; //clone
		return result;
	}
	@Override
	public ArrayList<CompositeQuery> expand(ArrayList<CompositeQuery> queries) {
		ArrayList<CompositeQuery> result = new ArrayList<CompositeQuery>();
		NamedEntityCorrector corrector = new NamedEntityCorrector();
		Iterator<CompositeQuery> iter = queries.iterator();
		while(iter.hasNext()) {
			CompositeQuery q = iter.next();
			result.add(correctNamedEntities(q, corrector));
			//result.add(q);
		}
		return result;
	}
}
