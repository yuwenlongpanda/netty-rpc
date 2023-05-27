package io.github.yuwenlongpanda.client;

import io.github.yuwenlongpanda.client.handler.ClientHandler;
import io.github.yuwenlongpanda.pojo.RpcRequest;
import io.github.yuwenlongpanda.pojo.RpcResponse;
import io.github.yuwenlongpanda.protocol.RpcCodec;
import io.github.yuwenlongpanda.protocol.ProtocolDecoder;
import io.github.yuwenlongpanda.common.IdGenerator;
import io.github.yuwenlongpanda.server.service.HelloService;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Proxy;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class RpcClient {


    public static void main(String[] args) {
        HelloService service = getProxyService(HelloService.class);
        System.out.println(service.sayHello("zhangsan"));
//        System.out.println(service.sayHello("lisi"));
//        System.out.println(service.sayHello("wangwu"));
    }

    // 创建代理类
    public static <T> T getProxyService(Class<T> serviceClass) {
        ClassLoader loader = serviceClass.getClassLoader();
        Class<?>[] interfaces = new Class[]{serviceClass};
        //                                                            sayHello  "张三"
        Object o = Proxy.newProxyInstance(loader, interfaces, (proxy, method, args) -> {
            // 1. 将方法调用转换为 消息对象
            int sequenceId = IdGenerator.nextId();
            RpcRequest msg = new RpcRequest(
                    sequenceId,
                    serviceClass.getName(),
                    method.getName(),
                    method.getReturnType(),
                    method.getParameterTypes(),
                    args
            );
            // 2. 将消息对象发送出去
            getChannel().writeAndFlush(msg);

            // 3. 准备一个空 future 对象，来接收结果             指定 future 对象异步接收结果线程
            CompletableFuture<RpcResponse> future = new CompletableFuture<>();
            ClientHandler.FUTURE.put(sequenceId, future);

//            future.addListener(future -> {
//                // 线程
//            });

            // 4. 等待 future 结果
            RpcResponse response = future.get();
            if (response.getReturnValue() != null) {
                // 调用正常
                return response.getReturnValue();
            } else {
                // 调用失败
                throw new RuntimeException(response.getExceptionValue());
            }
        });
        return (T) o;
    }

    private static Channel channel = null;
    private static final Object LOCK = new Object();

    // 获取唯一的 channel 对象
    public static Channel getChannel() {
        if (channel != null) {
            return channel;
        }
        synchronized (LOCK) { //  t2
            if (channel != null) { // t1
                return channel;
            }
            initChannel();
            return channel;
        }
    }

    // 初始化 channel 方法
    private static void initChannel() {
        NioEventLoopGroup group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.group(group);
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(new ProtocolDecoder());
                ch.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG));
                ch.pipeline().addLast(new RpcCodec());
                ch.pipeline().addLast(new ClientHandler());
            }
        });
        try {
            channel = bootstrap.connect("localhost", 8080).sync().channel();
            channel.closeFuture().addListener(future -> {
                group.shutdownGracefully();
            });
        } catch (Exception e) {
            log.error("client error", e);
        }
    }
}
