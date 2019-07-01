package info.ziang.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.util.Date;

public class NettyClient {

    /**
     * 使用 3 个线程模拟三个客户端
     *
     * @param args
     */
    public static void main(String[] args) throws InterruptedException {

        for (int i = 0; i < 3; i++) {
            new Thread(() -> runClient()).start();
        }
    }

    private static void runClient() {

        /**Bootstrap 与 ServerBootstrap 都继承(extends)于 AbstractBootstrap
         * 创建客户端辅助启动类,并对其配置,与服务器稍微不同，这里的 Channel 设置为 NioSocketChannel
         * 然后为其添加 Handler，这里直接使用匿名内部类，实现 initChannel 方法
         * 作用是当创建 NioSocketChannel 成功后，在进行初始化时,将它的ChannelHandler设置到ChannelPipeline中，用于处理网络I/O事件*/
        Bootstrap bootstrap = new Bootstrap();

        /** 配置客户端 NIO 线程组/池 */
        NioEventLoopGroup group = new NioEventLoopGroup();

        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) {

                        /**
                         * 添加 LineBasedFrameDecoder、StringDecoder 解码器
                         */
                        ch.pipeline().addLast(new LineBasedFrameDecoder(1024));
                        ch.pipeline().addLast(new StringEncoder());

                        ch.pipeline().addLast(new SimpleChannelInboundHandler<String>() {
                            @Override
                            protected void channelRead0(ChannelHandlerContext ctx, String msg) {
                                System.out.println(msg);
                            }
                        });

                        ch.pipeline().addLast(new ZaClientHandler());
                    }
                });


        try {
            /**connect：发起异步连接操作，调用同步方法 sync 等待连接成功*/
            ChannelFuture channelFuture = bootstrap.connect("127.0.0.1", 8000);
            Channel channel=channelFuture.channel();
            //ChannelFuture channelFuture = bootstrap.connect("127.0.0.1", 8000).sync();

            System.out.println(Thread.currentThread().getName() + ",客户端发起异步连接..........");

            /**等待客户端链路关闭*/
            //channelFuture.channel().closeFuture().sync();
            channelFuture.channel().closeFuture().sync();

            String clientId = Thread.currentThread().getName();
            while (true) {
                String msg = new Date() + ": hello from " + clientId;
                channel.writeAndFlush(msg);
                System.out.println(msg);
                Thread.sleep(2000);
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            /**优雅退出，释放NIO线程组*/
            group.shutdownGracefully();
        }
    }
}