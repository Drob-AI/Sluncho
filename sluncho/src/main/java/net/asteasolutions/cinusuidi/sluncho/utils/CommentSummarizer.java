package net.asteasolutions.cinusuidi.sluncho.utils;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import net.asteasolutions.cinusuidi.sluncho.data.QuestionRepository;
import net.asteasolutions.cinusuidi.sluncho.model.Comment;
import net.sf.classifier4J.summariser.SimpleSummariser;

public class CommentSummarizer {
	public static double summaryLengthRatio = 0.3;
	public static String summarizeGroup (String groupId) {
		SimpleSummariser ss = new SimpleSummariser();
		List<Comment> comments = QuestionRepository.Instance().extractAllCommentsPerGroup(groupId);
		if(comments.size() == 0) {
			return "";
		}
		String allComments = "";
		int sentences = 0;
		for (Comment comment : comments) {
			allComments += ". " + comment.body;
			sentences += StringUtils.countMatches(comment.body, ".");
		}
		return ss.summarise(allComments, (int) (summaryLengthRatio * sentences) + 1);
	}
}
