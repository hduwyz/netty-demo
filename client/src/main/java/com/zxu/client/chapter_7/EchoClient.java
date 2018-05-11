package com.zxu.client.chapter_7;

import com.zxu.client.msgpack.MsgpackDecoder;
import com.zxu.client.msgpack.MsgpackEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class EchoClient {
    private final String host;

    private final int port;

    private final int sendNumber;

    public EchoClient(String host,int port, int sendNumber){
        this.host = host;
        this.port = port;
        this.sendNumber = sendNumber;
    }

    public void run() throws Exception{
        //Configure the client.
        EventLoopGroup group = new NioEventLoopGroup();
        try{
            Bootstrap b = new Bootstrap();
            b.group(group).channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY,true)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS,3000)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) {
                            socketChannel.pipeline().addLast("msgpack decoder",new MsgpackDecoder());
                            socketChannel.pipeline().addLast("msgpack encoder", new MsgpackEncoder());
                            socketChannel.pipeline().addLast(new EchoClientHandler(sendNumber));
                        }
                    });
            //发起异步连接操作
            ChannelFuture f = b.connect(host,port).sync();
            //等待客户端链路关闭
            f.channel().closeFuture().sync();
        }finally {
            group.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception{
        int port = 7070;
        if (args != null && args.length>0){
            try {
                port = Integer.valueOf(args[0]);
            }catch (NumberFormatException e){

            }
        }
        new EchoClient("127.0.0.1",port,2).run();
    }
}
