package com.shijingfeng.module_event_dispatcher.data.interfaces;

import java.util.Map;

/**
 * Function: 模块事件 监听器  (暴露的接口使用Java写有许多好处:  1. Java工程中有类继承该暴露接口能复制文档   2. Java工程可以看到该暴露接口源码)
 * Date: 2021/3/11 22:36
 * Description:
 * Author: ShiJingFeng
 */
public interface ModuleEventListener {

    /**
     * 事件接收回调函数
     *
     * @param data 携带的数据
     * @return 是否中断接下来的其他模块该方法的执行  true: 中断
     */
    default boolean onReceive(Map<String, Object> data) {
        return false;
    }

}
