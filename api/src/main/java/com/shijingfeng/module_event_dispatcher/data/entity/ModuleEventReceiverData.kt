package com.shijingfeng.module_event_dispatcher.data.entity

/**
 * Function: Activity 数据
 * Date: 2020/12/8 16:55
 * Description:
 * Author: ShiJingFeng
 */
class ModuleEventReceiverData(

    /** 模块名称  */
    var moduleName: String,

    /** 生成的加载类全限定名称 */
    var classQualifiedName: String,

    /** 组名 */
    var group: String,

    /** 优先级  */
    var priority: Int,

    /** 标志 */
    var flag: Int

)