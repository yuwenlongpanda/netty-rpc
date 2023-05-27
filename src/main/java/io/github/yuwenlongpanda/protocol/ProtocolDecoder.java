package io.github.yuwenlongpanda.protocol;

import io.github.yuwenlongpanda.common.constants.RpcConstants;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public class ProtocolDecoder extends LengthFieldBasedFrameDecoder {

    public ProtocolDecoder() {
        this(RpcConstants.MAX_FRAME_LENGTH, 12, 4, 0, 0);
    }

    public ProtocolDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment, int initialBytesToStrip) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip);
    }
}
