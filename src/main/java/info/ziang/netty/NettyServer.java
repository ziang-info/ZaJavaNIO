package info.ziang.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;

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
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    protected void initChannel(NioSocketChannel ch) {
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