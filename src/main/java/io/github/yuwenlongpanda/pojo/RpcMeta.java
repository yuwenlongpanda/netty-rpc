package io.github.yuwenlongpanda.pojo;

import io.github.yuwenlongpanda.common.constants.RpcConstants;
import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Data
public abstract class RpcMeta implements Serializable {

    /**
     * 根据消息类型字节，获得对应的消息 class
     *
     * @param messageType 消息类型字节
     * @return 消息 class
     */
    public static Class<? extends RpcMeta> getMessageClass(int messageType) {
        return messageClasses.get(messageType);
    }

    private int sequenceId;

    private int messageType;

    public abstract int getMessageType();

    private static final Map<Integer, Class<? extends RpcMeta>> messageClasses = new HashMap<>();

    static {
        messageClasses.put(RpcConstants.REQUEST_TYPE, RpcRequest.class);
        messageClasses.put(RpcConstants.RESPONSE_TYPE, RpcResponse.class);
    }

}
