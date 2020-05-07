package vertxImpl;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;

class DataCodec implements MessageCodec<DataHolder, DataHolder> {


    @Override
    public DataHolder transform(DataHolder holder) {
        return holder;
    }

    @Override
    public String name() {
        return "HolderCodec";
    }

    @Override
    public byte systemCodecID() {
        return -1;
    }

	@Override
	public DataHolder decodeFromWire(int arg0, Buffer arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void encodeToWire(Buffer arg0, DataHolder arg1) {
		// TODO Auto-generated method stub
		
	}
}
