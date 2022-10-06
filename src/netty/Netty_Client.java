package netty;

import spider.*;

import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import kfaka.Producer;

public class Netty_Client {
	public static void main(String[] args) {
		UrlGet Ur = new UrlGet();
		HttpClient httpC = new HttpClient();
        //worker负责读写数据
        EventLoopGroup worker = new NioEventLoopGroup();
        ExecutorService fixedThreadPool = Executors.newFixedThreadPool(10);
        try {
            //辅助启动类
            Bootstrap bootstrap = new Bootstrap();

            //设置线程池
            bootstrap.group(worker);

            //设置socket工厂
            bootstrap.channel((Class<? extends Channel>) NioSocketChannel.class);

            //设置管道
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    //获取管道
                    ChannelPipeline pipeline = socketChannel.pipeline();
                    //字符串解码器
                    pipeline.addLast(new StringDecoder());
                    //字符串编码器
                    pipeline.addLast(new StringEncoder());
                    //处理类
                    //pipeline.addLast(new ClientHandler4());
                    pipeline.addLast(new ClientHandler4());
                }
            });

            //发起异步连接操作
            ChannelFuture futrue = bootstrap.connect(new InetSocketAddress("127.0.0.1",8866)).sync();
            System.out.println(Instant.now());
            for(int i=0;i<5;i++) {//开启线程爬取网站数据并存储到kafka中
            	fixedThreadPool.execute(new Runnable() {           	
            		@Override
            		public void run() {
            		 	ArrayList<String> url = new ArrayList<String>();
            		 	url = Ur.go();
            		 	// TODO Auto-generated method stub
            		 	for(int j=0;j<2;j++) {
            		 		for(String u : url) {
            		 			Producer pro = new Producer();
            		 			String html1 = httpC.go(u);
            		 			pro.produce(html1, u);
            		 			System.out.println(Thread.currentThread().getName());  
            		 			System.out.println("transport data to kafka...");
            		 		}
            		 	}
            		 	System.out.println(Instant.now());
            		}
            	});
            }
            
            //等待客户端链路关闭
            futrue.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            //优雅的退出，释放NIO线程组
            worker.shutdownGracefully();
        }
    }

}

class ClientHandler4 extends SimpleChannelInboundHandler<String> {
	 UrlGet Ur = new UrlGet();
	HttpClient httpC = new HttpClient();

    //接受服务端发来的消息
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        System.out.println("server response ： "+msg);
    }

    //与服务器建立连接
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //给服务器发消息
    	ctx.channel().writeAndFlush(" web spider start successful !");
    	/*
    	System.out.println(Instant.now());
		for(int i=0 ;i<20;i++) {
			new Thread(new Runnable() {
				 
				  @Override
				 
				  public void run() {
					  ArrayList<String> url = new ArrayList<String>();
					  url = Ur.go();
				    // TODO Auto-generated method stub
					  for(int j=0;j<2;j++) {
					  for(String u : url) {
							Producer pro = new Producer();
							String html1 = httpC.go(u);
							pro.produce(html1, u);
							System.out.println(Thread.currentThread().getName());  
							System.out.println("transport data to kafka...");
							ctx.channel().writeAndFlush(u+" content to kafka successful !");
							System.out.println("send url to server......");
							}
					  }
					  System.out.println(Instant.now());
				    }
				 
				  }
				 
				).start();
		}
		*/
		
    }
    

    //与服务器断开连接
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("断开连接");
    }

    //异常
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        //关闭管道
        ctx.channel().close();
        //打印异常信息
        cause.printStackTrace();
    }
}

