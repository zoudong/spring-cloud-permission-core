package com.zoudong.permission.rocketmq;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.TransactionListener;
import org.apache.rocketmq.client.producer.TransactionMQProducer;
import org.springframework.stereotype.Component;

import java.util.concurrent.*;
@Slf4j
@Component
public class ATestTransactionMQProducer {

    private TransactionMQProducer producer;
    private String producerGroup="consumer_demo1";//consumer_demo1
    private String namesrvAddr="192.168.1.30:9876";//192.168.1.30:9876
    private String instanceName="TopicTest1";//TopicTest1
    private int retryTimes=3000;

    public void init() throws MQClientException {

        producer = new TransactionMQProducer((this.producerGroup));
        producer.setNamesrvAddr(this.namesrvAddr);
        producer.setInstanceName(this.instanceName);
        producer.setRetryTimesWhenSendFailed(this.retryTimes);

        ExecutorService executorService = new ThreadPoolExecutor(2, 5, 100, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(2000), new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setName("client-transaction-msg-check-thread");
                return thread;
            }
        });
        producer.setExecutorService(executorService);

        TransactionListener transactionListener =new ATestTransactionListener();
        producer.setTransactionListener(transactionListener);

        producer.start();
        log.info("rocketMQ初始化生产者完成[producerGroup：" + producerGroup + "，instanceName："+ instanceName +"]");
    }


    public TransactionMQProducer getTransactionMQProducer() {
        return producer;
    }

    public void setProducerGroup(String producerGroup) {
        this.producerGroup = producerGroup;
    }

    public void setNamesrvAddr(String namesrvAddr) {
        this.namesrvAddr = namesrvAddr;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    public void setRetryTimes(int retryTimes) {
        this.retryTimes = retryTimes;
    }

    public void setTransactionListener(TransactionListener transactionListener) {
        producer.setTransactionListener(transactionListener);
    }

}