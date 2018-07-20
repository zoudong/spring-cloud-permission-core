package com.zoudong.permission.rabitmq;

import com.zoudong.permission.constant.MQConstant;
import com.zoudong.permission.model.SysUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;


@Slf4j
@EnableBinding(value = {InPutExchange.class, OutPutExchange.class})
public class ReceiveProcess {

    @StreamListener(MQConstant.input_permission_exchange)
    public void receiveBotExchange(SysUser sysUser) throws Exception{
        log.info("RABBITMQ接收到消息:{}",sysUser);
        log.info(sysUser.toString());
        log.info("RABBITMQ接收完消息:{}",sysUser);
    }
}
