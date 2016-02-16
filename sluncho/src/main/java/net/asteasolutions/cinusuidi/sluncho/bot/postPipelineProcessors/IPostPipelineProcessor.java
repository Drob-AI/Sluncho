package net.asteasolutions.cinusuidi.sluncho.bot.postPipelineProcessors;

import java.util.ArrayList;
import net.asteasolutions.cinusuidi.sluncho.bot.CompositeQuery;

import net.asteasolutions.cinusuidi.sluncho.bot.Query;

public interface IPostPipelineProcessor {
	ArrayList<CompositeQuery> expand(ArrayList<CompositeQuery> queries);
}
