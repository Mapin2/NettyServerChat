package es.ubisoft.netty.chat;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class ChatServer {

	private final int port;

	public ChatServer(int port) {
		this.port = port;
	}

	public static void main(String[] args) throws Exception {
		new ChatServer(8080).run();
	}

	public void run() throws Exception {
		// NioEventLoopGroup is a multithreaded event loop that handles I/O operation.

		// BossGroup accepts an incoming connection
		EventLoopGroup bossGroup = new NioEventLoopGroup();

		// WorkrtGroup handles the traffic of the accepted connection once the boss
		// accepts the connection and registers the accepted connection to the worker
		EventLoopGroup workerGroup = new NioEventLoopGroup();

		try {
			// Configure the server, ServerBootstrap is a helper class that sets up a
			// server.
			ServerBootstrap bootstrap = new ServerBootstrap();
			bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
					.childHandler(new ChatServerInitializer())
					.option(ChannelOption.SO_BACKLOG, 1000) // Number of connections queued
					.childOption(ChannelOption.SO_KEEPALIVE, true)
					.childOption(ChannelOption.TCP_NODELAY, true);

			// Bind and start to accept incoming connections.
			ChannelFuture channelFuture = bootstrap.bind(port).sync();
			// Wait until the server socket is closed.
			channelFuture.channel().closeFuture().sync();
		} finally {
			// Shut down all event loops to terminate all threads.
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}
}
