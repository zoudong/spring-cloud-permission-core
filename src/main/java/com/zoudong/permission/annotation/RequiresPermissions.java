package com.zoudong.permission.annotation;

import com.zoudong.permission.constant.Logical;
import org.springframework.web.bind.annotation.Mapping;

import java.lang.annotation.*;


@Target({ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Mapping
public @interface RequiresPermissions {
        String[] value();
        Logical logical() default Logical.AND;
}
