package cn.mst.server.net;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.springframework.stereotype.Component;

/**
 * 分布式事务服务端网络通信
 *
 * @ClassName NetServer
 * @Author buchengyin
 * @Date 2018/12/19 18:44
 **/
@Component
public class NetServer {
    private volatile boolean start = false;

    public void start() {
        if (start)
            return;
        start = true;
        NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
        NioEventLoopGroup workGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workGroup);
            bootstrap.channel(NioServerSocketChannel.class);
            bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
            bootstrap.childHandler(new ChannelInitializer<NioSocketChannel>() {
                @Override
                protected void initChannel(NioSocketChannel ch) throws Exception {
                   ch.pipeline().addLast("decoder1",new LengthFieldBasedFrameDecoder(1024,0,4));
                   ch.pipeline().addLast("decoder2",new StringDecoder());
                   ch.pipeline().addFirst("encoder1",new LengthFieldPrepender(1024,4));
                   ch.pipeline().addFirst("encoder2",new StringEncoder());
                   ch.pipeline().addLast("decoder3",new MstServerHandler());
                }
            });
            ChannelFuture future = bootstrap.bind(9090).sync();
            future.channel().closeFuture().addListener(new GenericFutureListener<Future<? super Void>>() {
                @Override
                public void operationComplete(Future<? super Void> future) throws Exception {
                    if (!future.isSuccess()) {
                        start = false;
                    }
                }
            }).sync();
        } catch (Exception e) {
            start = false;
            throw new RuntimeException(e);
        } finally {
            if (!bossGroup.isShutdown())
                bossGroup.shutdownGracefully();
            if (!workGroup.isShutdown())
                workGroup.shutdownGracefully();
        }

    }
}