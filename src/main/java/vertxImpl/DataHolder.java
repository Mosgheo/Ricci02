package vertxImpl;

import java.util.LinkedList;
import java.util.List;

final class DataHolder {
	private final NodeTuple data;
	private final List<NodeTuple> updateData;

	@Override
	public String toString() {
		return "Holder{" + "data=" + data + "updateData=" + updateData + '}';
	}

	public DataHolder(final NodeTuple data) {
		this.data = data;
		this.updateData = new LinkedList<NodeTuple>();
	}

	public DataHolder(final List<NodeTuple> updateData) {
		this.data = null;
		this.updateData = updateData;
	}

	public NodeTuple getData() {
		return data;
	}

	public List<NodeTuple> getUpdateList() {
		return this.updateData;
	}
}
