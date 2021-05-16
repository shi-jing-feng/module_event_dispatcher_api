package com.shijingfeng.module_event_dispatcher.data.constant;

/**
 * Function: 静态常量  (暴露的接口使用Java写有许多好处:  1. Java工程中有类继承该暴露接口能复制文档   2. Java工程可以看到该暴露接口源码)
 * Date: 2021/3/11 22:26
 * Description:
 * Author: ShiJingFeng
 */
public class Constant {

    /** 优先级: 高  */
    public static final int PRIORITY_HIGH = 0;
    /** 优先级: 中  */
    public static final int PRIORITY_MEDIUM = 1;
    /** 优先级: 低  */
    public static final int PRIORITY_LOW = 2;

    /** 指定组的所有的标志的接收器都会接收到分发事件 */
    public static final int ALL_FLAG = 0x0;

    /** 自动生成的模块数据加载器 名字前缀 */
    public static final String MODULE_DATA_LOADER_PREFIX = "ModuleDataLoader$$";

    /**
     * 自动生成的文件 包名
     * 以后本工程通过注解执行器生成的所有类文件都在此包中(包含不同业务), 这样可以一次性加载(加载是耗时操作)
     * 然后再转到各个业务处理类中
     */
    public static final String AUTO_GENERATE_FILE_PACKAGE_NAME = "com.shijingfeng.module_event_dispatcher.auto_generate";

}
