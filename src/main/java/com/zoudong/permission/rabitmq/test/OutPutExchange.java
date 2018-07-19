package com.zoudong.permission.rabitmq.test;

import com.zoudong.permission.constant.MQConstant;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Component;


@Component
public interface OutPutExchange {
    @Output(MQConstant.output_permission_exchange)
    MessageChannel outputPermissionExchange();
}
