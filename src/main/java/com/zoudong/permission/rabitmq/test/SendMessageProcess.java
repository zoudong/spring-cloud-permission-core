package com.zoudong.permission.rabitmq.test;

import com.zoudong.permission.exception.BusinessException;
import com.zoudong.permission.model.SysUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * @author zd
 * @description class
 * @date 2018/7/19 15:15
 */
@Slf4j
//@Async
@Component
public class SendMessageProcess {
    @Autowired
    private OutPutExchange outPutExchange;
    public void send(){
            try {
                log.info("RABBITMQ发送消息开始");
                boolean result=outPutExchange.outputPermissionExchange().send(MessageBuilder.withPayload(new SysUser()).build());
                if(!result){
                    throw new BusinessException("mq_send_error","消息发送失败");
                }
                log.info("RABBITMQ发送消息结束");
            }catch (Exception e){
                log.info("RABBITMQ发送取消息出错,非重要消息忽略错误。");
                e.printStackTrace();
            }
        }
}
