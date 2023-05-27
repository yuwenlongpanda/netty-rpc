package io.github.yuwenlongpanda.message;

import io.github.yuwenlongpanda.common.constants.RpcConstants;
import lombok.Data;
import lombok.ToString;

/**
 * @author yihang
 */
@Data
@ToString(callSuper = true)
public class RpcResponseMessage extends Message {
    /**
     * 返回值
     */
    private Object returnValue;
    /**
     * 异常值
     */
    private Exception exceptionValue;

    @Override
    public int getMessageType() {
        return RpcConstants.RESPONSE_TYPE;
    }
}
