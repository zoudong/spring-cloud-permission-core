package com.zoudong.permission.listener;

import com.zoudong.permission.rocketmq.ATestTransactionMQProducer;
import com.zoudong.permission.rocketmq.RocketMQConsumerListener;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * ApplicationReadyEvent
 * 初始化实例监听
 */
@Slf4j
@Component
public class TestRocketMQStartListener implements ApplicationListener<ApplicationReadyEvent> {

    @Resource
    private ATestTransactionMQProducer aTestTransactionMQProducer;
    @Resource
    private RocketMQConsumerListener rocketMQConsumerListener;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent contextRefreshedEvent) {
        //root application context 没有parent，才执行他.避免执行2次初始化
        if (contextRefreshedEvent.getApplicationContext().getParent().getId().equals("bootstrap")) {

            try {
                //启动Rocket事务消息消费监听,不要每次发送都启动一次
                aTestTransactionMQProducer.init();

                //这儿的测试报文使用时要用高效序列化框架来序列话
                Message m = new Message("TopicTest1", "TagA", "这是一个Rocket事务消息发送和消费监听启动测试消息".getBytes(RemotingHelper.DEFAULT_CHARSET));
                aTestTransactionMQProducer.getTransactionMQProducer().sendMessageInTransaction(m,null);


                //启动监听收消息
                rocketMQConsumerListener.allStart();

            } catch (Exception e) {
                log.info("哥,出大问题了,分布式事物实现的方式没工作或有异常！");
                e.printStackTrace();
            }
            log.info("Rocket事务消息发送和消费监听已启动");
        }
    }

}