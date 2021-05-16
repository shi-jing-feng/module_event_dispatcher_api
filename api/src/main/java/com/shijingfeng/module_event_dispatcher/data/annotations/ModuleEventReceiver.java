package com.shijingfeng.module_event_dispatcher.data.annotations;

import com.shijingfeng.module_event_dispatcher.data.constant.Constant;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Function: 模块事件接收器 注解 (暴露的接口使用Java写有许多好处:  1. Java工程中有类继承该暴露接口能复制文档   2. Java工程可以看到该暴露接口源码)
 * Date: 2021/3/11 22:20
 * Description:
 * Author: ShiJingFeng
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface ModuleEventReceiver {

    /** 组名 */
    String group();

    /** 优先级 越小优先级越大  */
    int priority() default Constant.PRIORITY_MEDIUM;

    /** 标志(注意: {@link Constant#ALL_FLAG} (0 或 0x0) 代表所有) 最好使用位标志(int类型占4个字节，可以用32个位进行组合) 例如: 00000000 00000000 00000000 00000000 */
    int flag() default Constant.ALL_FLAG;

}
