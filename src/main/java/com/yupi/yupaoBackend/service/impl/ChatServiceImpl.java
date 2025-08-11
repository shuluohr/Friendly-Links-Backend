package com.yupi.yupaoBackend.service.impl;

import com.yupi.yupaoBackend.model.dto.ChatDTO;
import com.yupi.yupaoBackend.model.request.ChatRequest;
import com.yupi.yupaoBackend.model.vo.ChatVO;
import com.yupi.yupaoBackend.service.ChatService;
import com.yupi.yupaoBackend.utils.NettyServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 陈君哲
 */
@Service
public class ChatServiceImpl implements ChatService {


    @Resource
    private RedisTemplate redisTemplate;



    /**
     * netty 服务端开启
     */
    @Override
    public void serverRun() throws InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();

            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new HttpServerCodec());
                            pipeline.addLast(new ChunkedWriteHandler());
                            pipeline.addLast(new HttpObjectAggregator(8192));
                            pipeline.addLast(new WebSocketServerProtocolHandler("/hello"));
                            pipeline.addLast(new NettyServerHandler());
                        }
                    });

            System.out.println(".....服务器 is ready.....");

            ChannelFuture cf = bootstrap.bind(7000).sync();

            cf.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    /**
     * 存储发送的消息
     * @param chatRequest
     * @return
     */
    @Override
    public void messageStore(ChatRequest chatRequest) {
        String userAccount = chatRequest.getUserAccount();
        String avatarUrl = chatRequest.getAvatarUrl();
        String message = chatRequest.getMessage();
        redisTemplate.opsForList().rightPush("com.yupi:yupaoBackend:chat:" + chatRequest.getId(), Pair.of(userAccount+','+ avatarUrl,message));
    }

    /**
     * 获取存入的消息对象列表
     */
    @Override
    public List<Map<String, String>> getMessageList(Long id) {

        //从redis取出全部消息
        List<Pair<String,String>> range = redisTemplate.opsForList().range("com.yupi:yupaoBackend:chat:" + id, 0, -1);
        int size = range.size();
        List<Map<String, String>> messageEntriesList = new ArrayList<>();
        //把每一条消息的发起人和内容当作一个entry，放入一个list中
        /*
             [{"userAccount":"admin","message":content},{"userAccount":"yupi","message":content}]
         */
        for (int i = 0 ; i < size; i++){
            Pair<String,String> entry = range.get(i);
            HashMap<String, String> messageEntry = new HashMap<>();
            messageEntry.put("sender",entry.getKey().split(",")[0]);
            messageEntry.put("content",entry.getValue());
            messageEntry.put("avatarUrl",entry.getKey().split(",")[1]);
            messageEntriesList.add(messageEntry);
        }

        return messageEntriesList;

    }

    /**
     * 获取最新消息
     *
     * @param id
     * @return
     */
    @Override
    public Map<String,String> getNewMessage(Long id) {
        //从redis取出最新消息
        Pair<String,String> entry = (Pair<String,String>)redisTemplate.opsForList().index("com.yupi:yupaoBackend:chat:" + id, -1);
        if (entry == null){
            return null;
        }
        HashMap<String, String> newMessageEntry = new HashMap<>();
        newMessageEntry.put("sender",entry.getKey().split(",")[0]);
        newMessageEntry.put("content",entry.getValue());
        newMessageEntry.put("avatarUrl",entry.getKey().split(",")[1]);
        return newMessageEntry;
    }


}
