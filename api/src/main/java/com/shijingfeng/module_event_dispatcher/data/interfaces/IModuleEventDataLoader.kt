package com.shijingfeng.module_event_dispatcher.data.interfaces

import com.shijingfeng.module_event_dispatcher.data.entity.ModuleEventReceiverData

/**
 * Function: 模块数据 加载接口 (参考 ARouter)
 * Date: 2020/12/8 16:40
 * Description:
 * Author: ShiJingFeng
 */
interface IModuleEventDataLoader {

    /**
     * 执行加载
     *
     * @param dataList 已加载的数据
     */
    fun load(dataList: List<ModuleEventReceiverData>)

}