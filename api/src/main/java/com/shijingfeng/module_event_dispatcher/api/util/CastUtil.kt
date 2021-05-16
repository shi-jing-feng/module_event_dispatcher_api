/** 生成的 Java 类名 */
@file:JvmName("CastUtil")
package com.shijingfeng.module_event_dispatcher.api.util

/**
 * Function: 转换工具类
 * Date: 2020/1/17 20:27
 * Description:
 * Author: ShiJingFeng
 */

/**
 * 用于消除类型装换警告
 * @param obj 原类型数据
 * @param <T> 泛型
 * @return 装换类型后的数据
 */
@Suppress("UNCHECKED_CAST")
internal fun <T> cast(obj : Any?) : T = obj as T