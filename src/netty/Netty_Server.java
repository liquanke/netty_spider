package netty;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import kfaka.Consumer;
public class Netty_Server {
	public static void main(String[] args) {
		
        //boss线程监听端口，worker线程负责数据读写
        EventLoopGroup boss = new NioEventLoopGroup();
        EventLoopGroup worker = new NioEventLoopGroup();
        ExecutorService fixedThreadPool = Executors.newFixedThreadPool(10);
        try{
            //辅助启动类
            ServerBootstrap bootstrap = new ServerBootstrap();
            //设置线程池
            bootstrap.group(boss,worker);

            //设置socket工厂
            bootstrap.channel(NioServerSocketChannel.class);

            //设置管道工厂
            bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    //获取管道
                    ChannelPipeline pipeline = socketChannel.pipeline();
                    //字符串解码器
                    pipeline.addLast(new StringDecoder());
                    //字符串编码器
                    pipeline.addLast(new StringEncoder());
                    //处理类
                    //pipeline.addLast(new ServerHandler4());
                    pipeline.addLast(new ServerHandler4());
                }
            });

            //设置TCP参数
            //1.链接缓冲池的大小（ServerSocketChannel的设置）
            bootstrap.option(ChannelOption.SO_BACKLOG,1024);
            //维持链接的活跃，清除死链接(SocketChannel的设置)
            bootstrap.childOption(ChannelOption.SO_KEEPALIVE,true);
            //关闭延迟发送
            bootstrap.childOption(ChannelOption.TCP_NODELAY,true);

            //绑定端口
            ChannelFuture future = bootstrap.bind(8866).sync();
            System.out.println("server start ...... ");
            
            for(int i=0 ;i<5;i++) {//开启线程从kafka取数据到redis
            	fixedThreadPool.execute(new Runnable() {           	
            		@Override
            		public void run() {
            			// TODO Auto-generated method stub
            			Consumer con = new Consumer();
            			con.consume();
            			System.out.println(Thread.currentThread().getName());
            			System.out.println("tranport data to redis from kafka...");
            		}
            	});
            }
            
            //等待服务端监听端口关闭
            future.channel().closeFuture().sync();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            //优雅退出，释放线程池资源
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }

}


class ServerHandler4 extends SimpleChannelInboundHandler<String> {
	ExecutorService fixedThreadPool = Executors.newFixedThreadPool(5);
	@Override
    //读取客户端发送的数据
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        System.out.println("client response :"+msg);
        ctx.channel().writeAndFlush("get");
        // System.out.println(Thread.currentThread().getName()); 
        System.out.println("transport data from kafka...");

    }
    @Override
    //新客户端接入
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("客户端接入");

        /*
        for(int i=0;i<20;i++) {
        new Thread(new Runnable() {
        	 
        	  @Override
        	 
        	  public void run() {
        	 
        	    // TODO Auto-generated method stub
        		  System.out.println(Thread.currentThread().getName()); 
        		  Consumer con = new Consumer();
                  con.consume();
        	    }
        	 
        	  }
        	 
        	).start();
        }
        */
    }
    @Override
    //客户端断开
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("客户端断开");
    }
    @Override
    //异常
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        //关闭通道
        ctx.channel().close();
        //打印异常
        cause.printStackTrace();
    }
}

