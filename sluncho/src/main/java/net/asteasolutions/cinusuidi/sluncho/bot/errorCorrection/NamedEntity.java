package net.asteasolutions.cinusuidi.sluncho.bot.errorCorrection;

public class NamedEntity {
	public String type;
	public String name;
	public float score;
	
	public NamedEntity() { }
	
	public NamedEntity(String type, String name, float score) {
		this.type = type;
		this.name = name;
		this.score = score;
	}
	
	public void print() {
		System.out.println(this.name);
		System.out.println(this.type);
		System.out.println(this.score);
	}
	
}
