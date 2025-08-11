package com.yupi.yupaoBackend.controller;

import com.yupi.yupaoBackend.common.BaseResponse;
import com.yupi.yupaoBackend.common.ErrorCode;
import com.yupi.yupaoBackend.common.ResultUtils;
import com.yupi.yupaoBackend.model.request.ChatRequest;
import com.yupi.yupaoBackend.service.ChatService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @author 陈君哲
 */
@Slf4j
@RestController
@RequestMapping("chat")
@CrossOrigin(allowCredentials = "true",originPatterns={"http://localhost:5173","http://ip:5173","http://ip"},methods={RequestMethod.POST,RequestMethod.GET})
//@CrossOrigin(allowCredentials = "true",origins = {"http://localhost:5173"},methods={RequestMethod.POST,RequestMethod.GET})
public class ChatController {



    @Resource
    private ChatService chatService;


    /**
     * 启动群聊服务
     * @return
     * @throws InterruptedException
     */
    @GetMapping("/server")
    public BaseResponse groupChatServer() throws InterruptedException {
        chatService.serverRun();
        return ResultUtils.success(ErrorCode.OK, null);
    }

    /**
     * 存储发送的信息
     * @param chatMessageStoreRequest
     * @return
     * @throws InterruptedException
     */
    @PostMapping("/messageStore")
    public BaseResponse messageStore(@RequestBody ChatRequest chatMessageStoreRequest)  {
        chatService.messageStore(chatMessageStoreRequest);
        return ResultUtils.success(ErrorCode.OK, null);
    }

    /**
     * 获取存入的消息对象列表
     */
    @GetMapping("/getMessageList")
    public BaseResponse<List<Map<String, String>>> getMessageList(Long id)  {
        List<Map<String, String>> message = chatService.getMessageList(id);
        return ResultUtils.success(ErrorCode.OK, message);
    }

    /**
     * 获取最新消息
     * @param id
     * @return
     */
    @GetMapping("/getNewMessage")
    public BaseResponse<Map<String,String>> getNewMessage(Long id)  {
        Map<String,String> message = chatService.getNewMessage(id);
        return ResultUtils.success(ErrorCode.OK, message);
    }
}
