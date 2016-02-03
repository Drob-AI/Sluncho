package net.asteasolutions.cinusuidi.sluncho.bot.postPipelineProcessors;

import java.util.ArrayList;

import net.asteasolutions.cinusuidi.sluncho.bot.Query;

public interface IPostPipelineProcessor {
	ArrayList<Query> expand(ArrayList<Query> queries);
}
