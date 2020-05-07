package model;

public class Voice {
	final private int depth;
	final private String title;
	final private String father;
	final private String color;
	
	public Voice(int depth, String title, String father, String color) {
		this.depth = depth;
		this.title = title;
		this.father = father;
		this.color = color;
	}
	
	public int getDepth() {
		return depth;
	}
	
	public String getTitle() {
		return title;
	}
	
	public String getFather() {
		return father;
	}
	
	public String getColor() {
		return color;
	}
}
