package com.zoudong.permission.rocketmq;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.springframework.stereotype.Component;

/**
 * @author zd
 * @description class
 * @date 2018/8/23 11:32
 */
@Slf4j
@Component
public class RocketMQConsumerListener {

    public void allStart()throws Exception{
        //启动aTest监听
        aTestConsumerStart();
    }

    /**
     * aTest监听
     * @throws Exception
     */
    public void aTestConsumerStart() throws Exception{
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("consumer_demo1");
        //指定NameServer地址，多个地址以 ; 隔开
        consumer.setNamesrvAddr("192.168.1.30:9876"); //修改为自己的(以配置方式加载)
        /**
         * 设置Consumer第一次启动是从队列头部开始消费还是队列尾部开始消费
         * 如果非第一次启动，那么按照上次消费的位置继续消费
         */
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
        consumer.subscribe("TopicTest1", "TagA");
        //设置aTest监听的监听器
        consumer.registerMessageListener(new ATestRocketListener());
        consumer.start();
        log.info("RocketMQConsumerListener的test1消费监听已经启动");
    }

}