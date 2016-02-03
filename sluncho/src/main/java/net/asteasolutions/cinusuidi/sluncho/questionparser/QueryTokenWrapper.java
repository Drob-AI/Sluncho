package net.asteasolutions.cinusuidi.sluncho.questionparser;

import net.asteasolutions.cinusuidi.sluncho.questionparser.helpers.GateOffsetBoundary;

public class QueryTokenWrapper implements Comparable<QueryTokenWrapper>{
	private QueryToken token;
	private GateOffsetBoundary boundary;
	
	public QueryTokenWrapper(String text, AnnotationType type, GateOffsetBoundary boundary) {
		token = new QueryToken(text, type, boundary);
		this.boundary = boundary;
	}
	
	public QueryToken getToken() {
		return token;
	}
	
	public GateOffsetBoundary getBoundary() {
		return boundary;
	}

	@Override
	public int compareTo(QueryTokenWrapper o) {
		Long result = this.boundary.start - o.boundary.start;
		if (result < 0) {
			return -1;
		}
		if (result > 0) {
			return 1;
		}
		// long tokens first
		Long lenDiff = o.boundary.length - this.boundary.length;
		if (lenDiff < 0) {
			return -1;
		}
		if (lenDiff > 0) {
			return 1;
		}
		return 0;
	}
	
	@Override
	public String toString() {
		return token.toString() + "[" + boundary.start + " : " + boundary.end + "]";
	}

	public boolean overlaps(QueryTokenWrapper token) {
		GateOffsetBoundary other = token.getBoundary();
		return (this.boundary.start <= other.start &&
				this.boundary.end >= other.start) ||
				(this.boundary.start >= other.start &&
				this.boundary.start <= other.end);
	}
}
