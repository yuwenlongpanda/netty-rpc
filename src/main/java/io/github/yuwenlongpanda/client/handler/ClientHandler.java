package io.github.yuwenlongpanda.client.handler;

import io.github.yuwenlongpanda.pojo.RpcResponse;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@ChannelHandler.Sharable
public class ClientHandler extends SimpleChannelInboundHandler<RpcResponse> {

    // 用来接收结果的 future 对象
    public static final Map<Integer, CompletableFuture<RpcResponse>> FUTURE = new ConcurrentHashMap<>();

    @Override

    protected void channelRead0(ChannelHandlerContext ctx, RpcResponse msg) throws Exception {
        log.debug("{}", msg);
        // 拿到空的 future
        CompletableFuture<RpcResponse> future = FUTURE.remove(msg.getSequenceId());
        if (future != null) {
            future.complete(msg);
        }
    }
}
