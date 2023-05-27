package io.github.yuwenlongpanda.protocol;

import io.github.yuwenlongpanda.common.Serializer;
import io.github.yuwenlongpanda.common.constants.RpcConstants;
import io.github.yuwenlongpanda.common.constants.config.Config;
import io.github.yuwenlongpanda.pojo.RpcMeta;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@ChannelHandler.Sharable
/**
 * 必须和 LengthFieldBasedFrameDecoder 一起使用，确保接到的 ByteBuf 消息是完整的
 */
public class RpcCodec extends MessageToMessageCodec<ByteBuf, RpcMeta> {
    @Override
    public void encode(ChannelHandlerContext ctx, RpcMeta msg, List<Object> outList) {
        ByteBuf out = ctx.alloc().buffer();
        // 魔法数（4 字节）
        out.writeBytes(RpcConstants.MAGIC_NUMBER);
        // 协议版本号（1 字节）
        out.writeByte(RpcConstants.VERSION);
        // 序列化方式 jdk 0 , json 1, protobuf 2（1 字节）
        out.writeByte(Config.getSerializerAlgorithm().ordinal());
        // 请求类型（1 字节）
        out.writeByte(msg.getMessageType());
        // 请求序号（4 个字节）
        out.writeInt(msg.getSequenceId());
        // 无意义，对齐填充
        out.writeByte(0);
        // 获取内容的字节数组
        byte[] bytes = Config.getSerializerAlgorithm().serialize(msg);
        // 消息长度（4 个字节）
        out.writeInt(bytes.length);
        // 消息内容（内容）
        out.writeBytes(bytes);
        outList.add(out);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        int magicNum = in.readInt();
        byte version = in.readByte();
        byte codeType = in.readByte();
        byte messageType = in.readByte();
        int sequenceId = in.readInt();
        in.readByte();
        int length = in.readInt();
        byte[] bytes = new byte[length];
        in.readBytes(bytes, 0, length);

        // 找到反序列化算法
        Serializer.Algorithm algorithm = Serializer.Algorithm.values()[codeType];
        // 确定具体消息类型
        Class<? extends RpcMeta> messageClass = RpcMeta.getMessageClass(messageType);
        RpcMeta message = algorithm.deserialize(messageClass, bytes);
        out.add(message);
    }

}
