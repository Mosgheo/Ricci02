package vertxImpl;

import java.util.List;


final class DataHolder {
    private final NodeTuple data;
    @Override
    public String toString() {
        return "Holder{" +
                "data=" + data +
                '}';
    }

    public DataHolder(final NodeTuple data) {
        this.data = data;
    }

    public NodeTuple getData() {
        return data;
    }
}
