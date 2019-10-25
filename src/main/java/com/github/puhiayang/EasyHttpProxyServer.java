package com.github.puhiayang;


import com.github.puhiayang.handler.HttpProxyHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;


/**
 * EasyHttpProxyServer
 * created on 2019/10/25 14:44
 *
 * @author puhaiyang
 */
public class EasyHttpProxyServer {
    private static EasyHttpProxyServer instace = new EasyHttpProxyServer();

    public static EasyHttpProxyServer getInstace() {
        if (instace == null) {
            instace = new EasyHttpProxyServer();
        }
        return instace;
    }

    /**
     * 启动
     *
     * @param listenPort 监控的端口
     */
    public void start(int listenPort) {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup(2);
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 100)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<Channel>() {

                        @Override
                        protected void initChannel(Channel ch) throws Exception {
                            //ch.pipeline().addLast("httpCodec", new HttpServerCodec());
                            //接收客户端请求，将客户端的请求内容解码
                            ch.pipeline().addLast("httpRequestDecoder", new HttpRequestDecoder());
                            //发送响应给客户端，并将发送内容编码
                            ch.pipeline().addLast("httpResponseEncoder", new HttpResponseEncoder());
                            ch.pipeline().addLast("http-aggregator", new HttpObjectAggregator(65536));
                            ch.pipeline().addLast("httpProxyHandler", new HttpProxyHandler());
                            ch.pipeline().addLast("httpsProxyHandler", new HttpProxyHandler());
                        }
                    });
            ChannelFuture f = b
                    .bind(listenPort)
                    .sync();
            f.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
