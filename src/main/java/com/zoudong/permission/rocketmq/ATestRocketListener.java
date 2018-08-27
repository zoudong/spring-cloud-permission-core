package com.zoudong.permission.rocketmq;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author zd
 * @description class
 * @date 2018/8/23 11:21
 */
@Slf4j
@Component
public class ATestRocketListener implements MessageListenerConcurrently {
    @Override
    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> list, ConsumeConcurrentlyContext consumeConcurrentlyContext) {
        try{
            for (MessageExt message : list) {
                log.info("收到事物消息:" + message);
                String msg = new String(message.getBody());
                //处理消息......

            }
            /* if(1==1){
            //故意失败查看是否重试接收消息并处理（已确定新版重新开放了消息重试消费功能）
            return ConsumeConcurrentlyStatus.RECONSUME_LATER;
            }*/
        }catch (Exception e){
            log.error("处理消息有异常，稍后重新接收处理。");
            return ConsumeConcurrentlyStatus.RECONSUME_LATER;
        }
        log.info("成功处理这次收到的所有事物消息");
        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
    }
}