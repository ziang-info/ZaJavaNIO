package info.ziang.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Administrator on 2017/5/16.
 * ChannelInboundHandlerAdapter extends ChannelHandlerAdapter 用于对网络事件进行读写操作
 */
public class ZaServerHandler extends ChannelInboundHandlerAdapter {

    /**
     * 因为多线程，所以使用原子操作类来进行计数
     */
    private static AtomicInteger atomicInteger = new AtomicInteger();

    /**
     * 当客户端和服务端 TCP 链路建立成功之后，Netty 的 NIO 线程会调用 channelActive 方法
     */
    //@Override
    /*
    如何添加channelActive方法，服务端收不到Client正常消息，
    但是ClientHandler的channelAcitive方法中的"我是客户端.."是可以收到的！
    */
    public void channelActive2(ChannelHandlerContext ctx) throws Exception {
        String reqMsg = "我服务端 " + Thread.currentThread().getName();
        byte[] reqMsgByte = reqMsg.getBytes("UTF-8");
        ByteBuf reqByteBuf = Unpooled.buffer(reqMsgByte.length);
        /**
         * writeBytes：将指定的源数组的数据传输到缓冲区
         * 调用 ChannelHandlerContext 的 writeAndFlush 方法将消息发送给服务器
         */
        reqByteBuf.writeBytes(reqMsgByte);
        ctx.writeAndFlush(reqByteBuf);

        /**
         需要在channelActive中添加这行语句才会调用channelRead方法
         */
        //ctx.channel().read();
    }

    /**
     * 收到客户端消息，自动触发
     *
     * @param ctx
     * @param msg
     * @throws Exception
     */
    //@Override
    public void channelRead2(ChannelHandlerContext ctx, Object msg) throws Exception {
        /**
         * 将 msg 转为 Netty 的 ByteBuf 对象，类似 JDK 中的 java.nio.ByteBuffer，不过 ButeBuf 功能更强，更灵活
         */
        ByteBuf buf = (ByteBuf) msg;
        /**readableBytes：获取缓冲区可读字节数,然后创建字节数组
         * 从而避免了像 java.nio.ByteBuffer 时，只能盲目的创建特定大小的字节数组，比如 1024
         * */
        byte[] reg = new byte[buf.readableBytes()];
        /**readBytes：将缓冲区字节数组复制到新建的 byte 数组中
         * 然后将字节数组转为字符串
         * */
        buf.readBytes(reg);
        String body = new String(reg, "UTF-8");
        System.out.println(Thread.currentThread().getName() + ",The server receive  order : " + body);

        /**回复消息
         * copiedBuffer：创建一个新的缓冲区，内容为里面的参数
         * 通过 ChannelHandlerContext 的 write 方法将消息异步发送给客户端
         * */
        String respMsg = "I am Server，消息接收 success!";
        ByteBuf respByteBuf = Unpooled.copiedBuffer(respMsg.getBytes());
        ctx.write(respByteBuf);
    }

    /**
     * 收到客户端消息，自动触发
     *
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        /**
         * 这个 msg 已经是解码成功的消息，所以不再需要像以前一样使用 ByteBuf 进行编码
         * 直接转为 string 字符串即可*/
        String body = (String) msg;
        System.out.println((atomicInteger.addAndGet(1)) + "--->"
                + Thread.currentThread().getName() + ",The server receive  order : " + body);

        /**回复消息
         * copiedBuffer：创建一个新的缓冲区，内容为里面的参数
         * 通过 ChannelHandlerContext 的 write 方法将消息异步发送给客户端
         * 注意解决 TCP 粘包的策略之一就是：在包尾增加回车换行符进行分割
         * System.getProperty("line.separator");屏蔽了 Windows和Linux的区别
         * windows 系统上回车换行符 "\n",Linux 系统上是 "/n"
         * */
        String respMsg = "I am Server，消息接收 success!"
                + Thread.currentThread().getName()
                //+ System.getProperty("line.separator");
                /**回复消息
                 * 由于创建的 DelimiterBasedFrameDecoder 解码器默认会自动去掉分隔符，所以返回给客户端时需要自己拼接分隔符
                 * DelimiterBasedFrameDecoder(int maxFrameLength, boolean stripDelimiter, ByteBuf delimiter)
                 *  这个构造器可以设置是否去除分隔符
                 * 最后创建 ByteBuf 将原始的消息重新返回给客户端
                 * */
                + "$_";

        ByteBuf respByteBuf = Unpooled.copiedBuffer(respMsg.getBytes());
        /**
         * 每次写的时候，同时刷新，防止 TCP 粘包
         */
        ctx.writeAndFlush(respByteBuf);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        /**flush：将消息发送队列中的消息写入到 SocketChannel 中发送给对方，为了频繁的唤醒 Selector 进行消息发送
         * Netty 的 write 方法并不直接将消息写如 SocketChannel 中，调用 write 只是把待发送的消息放到发送缓存数组中，再通过调用 flush
         * 方法，将发送缓冲区的消息全部写入到 SocketChannel 中
         * */
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        /**当发生异常时，关闭 ChannelHandlerContext，释放和它相关联的句柄等资源 */
        ctx.close();
    }
}
