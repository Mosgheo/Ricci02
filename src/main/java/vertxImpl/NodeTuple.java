package vertxImpl;

public class NodeTuple {
	private final String father;
	private final String value;
	private final int depth;
	
	public NodeTuple(final String father, final String value, final int depth) {
		this.father = father;
		this.value = value;
		this.depth = depth;
	}
	public String getFather() {
		return this.father;
	}
	public String getValue() {
		return this.value;
	}
	public int getDepth() {
		return this.depth;
	}
}
