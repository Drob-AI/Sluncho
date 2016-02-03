package net.asteasolutions.cinusuidi.sluncho.data;

import java.util.ArrayList;

public class Paragraph {
	public Paragraph(int level) {
		this.level = level; 
		paragraphs = new ArrayList<Paragraph>();
	}
	public Paragraph(int level, Paragraph parent) {
		this(level);
		this.parent = parent;
	}
	public int level;
	public String title;
	public String content;
	public Paragraph parent;
	public ArrayList<Paragraph> paragraphs;
}
