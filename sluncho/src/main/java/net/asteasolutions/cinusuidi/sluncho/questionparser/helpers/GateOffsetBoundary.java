package net.asteasolutions.cinusuidi.sluncho.questionparser.helpers;

public class GateOffsetBoundary {
	public Long start;
	public Long end;
	public Long length;
	
	public GateOffsetBoundary(Long start, Long end) {
		this.start = start;
		this.end = end;
		this.length = end - start;
	}

	public GateOffsetBoundary(GateOffsetBoundary boundary) {
		this.start = new Long(boundary.start);
		this.end = new Long(boundary.end);
		this.length = new Long(boundary.length);
	}

}
