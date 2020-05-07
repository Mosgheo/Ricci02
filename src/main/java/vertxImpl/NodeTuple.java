package vertxImpl;

public class NodeTuple {
	private final String father;
	private final String value;
	
	public NodeTuple(final String father, final String value) {
		this.father = father;
		this.value = value;
	}
	public String getFather() {
		return this.father;
	}
	public String getValue() {
		return this.value;
	}
}
