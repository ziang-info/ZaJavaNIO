package info.ziang.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Refer to https://www.jianshu.com/p/a4e03835921a
 */
public class NettyServer {

    public static void main(String[] args) {

        /**
         * interface EventLoopGroup
         *      extends EventExecutorGroup
         *      extends ScheduledExecutorService
         *      extends ExecutorService
         * 配置服务端的 NIO 线程池,用于网络事件处理，实质上他们就是 Reactor 线程组
         *
         * bossGroup 用于服务端接受客户端连接，
         * workerGroup 用于进行 SocketChannel 网络读写
         */
        NioEventLoopGroup boos = new NioEventLoopGroup();
        NioEventLoopGroup worker = new NioEventLoopGroup();

        /**
         ServerBootstrap 是 Netty 用于启动 NIO 服务端的辅助启动类，用于降低开发难度
         */
        ServerBootstrap serverBootstrap = new ServerBootstrap();

        serverBootstrap
                .group(boos, worker)
                .channel(NioServerSocketChannel.class)
                /**
                 BACKLOG用于构造服务端套接字ServerSocket对象，标识当服务器请求处理线程全满时，
                 用于临时存放已完成三次握手的请求的队列的最大长度。如果未设置或所设置的值小于1，
                 Java将使用默认值50。
                 */
                .option(ChannelOption.SO_BACKLOG, 1024)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    protected void initChannel(NioSocketChannel ch) {

                        /**
                         * FixedLengthFrameDecoder(int frameLength)： frameLength：指定单条消息的长度
                         * 为了测试结果明显，这里设置消息长度为 64 字节，之后在发送消息时，会让部分消息长度小于等于 64，然后部分消息长度大于 64，以观察区别
                         * 实际应用中应该根据时间情况进行设置，比如 1024 字节
                         */
                        //ch.pipeline().addLast(new FixedLengthFrameDecoder(64));

                        /**
                         * 添加 LineBasedFrameDecoder 与 StringDecoder解码器
                         */
                        //ch.pipeline().addLast(new LineBasedFrameDecoder(1024));

                        /**
                         * 创建分隔符缓冲对象 ByteBuf，使用自定义的 "$_" 作为消息结束符，自己也可以定义为其它的字符作为结束符
                         *
                         * DelimiterBasedFrameDecoder(int maxFrameLength, ByteBuf delimiter)
                         * DelimiterBasedFrameDecoder(int maxFrameLength, ByteBuf... delimiters)
                         * 分隔符解码器重载了好几个构造器方法，其中常用的就是上面这两个
                         *      maxFrameLength：单条消息的最大长度,当达到该长度后仍然没有查找到分隔符时，则抛出 TooLongFrameException 异常
                         *      防止由于异常码流缺失分隔符导致内存溢出（亲测 Netty 4.1 版本，服务器并未抛出异常，而是客户端被强制断开连接了）
                         *      delimiter：分隔符缓冲对象,第二个构造器可见可以指定多个结束符
                         */
                        ByteBuf delimiter = Unpooled.copiedBuffer("$_".getBytes());
                        ch.pipeline().addLast(new DelimiterBasedFrameDecoder(1024, delimiter));


                        ch.pipeline().addLast(new StringDecoder());

                        ch.pipeline().addLast(new SimpleChannelInboundHandler<String>() {
                            @Override
                            protected void channelRead0(ChannelHandlerContext ctx, String msg) {
                                System.out.println(msg);
                            }
                        });
                        ch.pipeline().addLast(new ZaServerHandler());
                    }
                });

        /**
         服务器启动辅助类配置完成后，调用 bind 方法绑定监听端口，调用 sync 方法同步等待绑定操作完成
         */

        try {
            ChannelFuture f = serverBootstrap.bind(8000).sync();

            System.out.println(Thread.currentThread().getName() + ",服务器开始监听端口，等待客户端连接.........");
            /**
             下面会进行阻塞，等待服务器连接关闭之后 main 方法退出，程序结束
             */
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}