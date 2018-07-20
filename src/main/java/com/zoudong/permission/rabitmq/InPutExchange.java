package com.zoudong.permission.rabitmq;

import com.zoudong.permission.constant.MQConstant;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.stereotype.Component;


@Component
public interface InPutExchange {
    @Input(MQConstant.input_permission_exchange)
    SubscribableChannel inputPermissionExchange();
}
