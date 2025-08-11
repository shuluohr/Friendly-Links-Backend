package com.yupi.yupaoBackend.service;

import com.yupi.yupaoBackend.model.dto.ChatDTO;
import com.yupi.yupaoBackend.model.request.ChatRequest;
import com.yupi.yupaoBackend.model.vo.ChatVO;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Map;

/**
 * @author 陈君哲
 */
public interface ChatService {

    /**
     * netty 服务端开启
     */
    void serverRun() throws InterruptedException;


    /**
     * 存储发送的消息
     * @param chatRequest
     * @return
     */
    void messageStore(ChatRequest chatRequest);

    /**
     * 获取存入的消息对象列表
     */
    List<Map<String, String>> getMessageList(Long id);

    /**
     * 获取最新消息
     * @param id
     * @return
     */
    Map<String,String> getNewMessage(Long id);
}
