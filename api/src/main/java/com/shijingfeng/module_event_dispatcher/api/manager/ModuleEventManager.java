package com.shijingfeng.module_event_dispatcher.api.manager;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.AnyThread;
import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.shijingfeng.module_event_dispatcher.api.util.ClassUtilKt;
import com.shijingfeng.module_event_dispatcher.data.constant.Constant;
import com.shijingfeng.module_event_dispatcher.data.entity.ModuleEventReceiverData;
import com.shijingfeng.module_event_dispatcher.data.interfaces.IModuleEventDataLoader;
import com.shijingfeng.module_event_dispatcher.data.interfaces.ModuleEventListener;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Function: 模块事件 管理器 (暴露的接口使用Java写有许多好处:  1. Java工程中有类继承该暴露接口能复制文档   2. Java工程可以看到该暴露接口源码)
 * Date: 2021/3/11 21:10
 * Description:
 * Author: ShiJingFeng
 */
public class ModuleEventManager {

    /** 是否初始化过了  true:已经初始化过了 */
    private static boolean mHasInit = false;

    /**
     * 分组后的 模块事件接收器数据[ModuleEventReceiverData] 列表
     * Key:   组名 [ModuleEventReceiverData.group]
     * Value: 模块事件接收器数据[ModuleEventReceiverData] 列表
     */
    private static final Map<String, List<ModuleEventReceiverData>> MODULE_EVENT_RECEIVER_DATA_LIST_MAP = new HashMap<>();

    /**
     * Key:   组名 [ModuleEventReceiverData.group]
     * Value: 模块事件监听器[ModuleEventListener] 列表
     */
    private static final Map<String, List<ModuleEventListener>> MODULE_EVENT_LISTENER_LIST_MAP = new HashMap<>();

    /**
     * 使用 Gradle Plugin 通过 ASM 在 class文件的当前方法中插入要执行的逻辑
     */
    private static void loadByGradlePlugin() {
        /**
         * 通过反编译, 此处会插入Java代码举例(针对于ASM没有换行, 是因为织入代码时没有指定行号)
         * load("{@link AUTO_GENERATE_FILE_PACKAGE_NAME}.{@link MODULE_DATA_LOADER_PREFIX}$$app");load("{@link AUTO_GENERATE_FILE_PACKAGE_NAME}.{@link MODULE_DATA_LOADER_PREFIX}$$base")
         */
    }

    /**
     * 加载生成的类数据
     */
    private static void load(String moduleDataLoaderClassName) {
        if (TextUtils.isEmpty(moduleDataLoaderClassName)) {
            return;
        }
        try {
            final Class<?> clz = Class.forName(moduleDataLoaderClassName);
            final Object instance = clz.newInstance();

            if (instance instanceof IModuleEventDataLoader) {
                final IModuleEventDataLoader moduleEventDataLoader = (IModuleEventDataLoader) instance;
                final List<ModuleEventReceiverData> dataList = new ArrayList<>();

                // 加载某个模块中的所有ModuleEventReceiverData数据
                moduleEventDataLoader.load(dataList);
                for (ModuleEventReceiverData data : dataList) {
                    final String group = data.getGroup();
                    final List<ModuleEventReceiverData> moduleEventReceiverDataList = MODULE_EVENT_RECEIVER_DATA_LIST_MAP.get(group);

                    if (moduleEventReceiverDataList == null) {
                        final List<ModuleEventReceiverData> newModuleEventReceiverDataList = new ArrayList<>();

                        newModuleEventReceiverDataList.add(data);
                        MODULE_EVENT_RECEIVER_DATA_LIST_MAP.put(group, newModuleEventReceiverDataList);
                        MODULE_EVENT_LISTENER_LIST_MAP.put(group, new ArrayList<>());
                    } else {
                        moduleEventReceiverDataList.add(data);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化
     */
    private static void init() {
        for (Map.Entry<String, List<ModuleEventReceiverData>> entry : MODULE_EVENT_RECEIVER_DATA_LIST_MAP.entrySet()) {
            final String group = entry.getKey();
            final List<ModuleEventReceiverData> moduleEventReceiverDataList = entry.getValue();
            final List<ModuleEventListener> moduleEventListenerList = Objects.requireNonNull(MODULE_EVENT_LISTENER_LIST_MAP.get(group));

            // 分组后的列表 按照优先级从大到小排序
            Collections.sort(moduleEventReceiverDataList, (o1, o2) -> {
                final int priority1 = o1.getPriority();
                final int priority2 = o2.getPriority();

                return priority1 - priority2;
            });
            // 添加分组后的模块事件数据列表
            final List<ModuleEventListener> newModuleEventListenerList = new ArrayList<>();

            for (ModuleEventReceiverData data : moduleEventReceiverDataList) {
                try {
                    final Class<?> clz = Class.forName(data.getClassQualifiedName());
                    final Constructor<?> constructor = clz.getDeclaredConstructor();
                    final Object instance = constructor.newInstance();

                    // 设置私有构造方法可执行和修改
                    constructor.setAccessible(true);
                    if (instance instanceof ModuleEventListener) {
                        newModuleEventListenerList.add((ModuleEventListener) instance);
                    } else {
                        newModuleEventListenerList.add(null);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new IllegalArgumentException("${data.classQualifiedName}必须要有一个无参构造方法！");
                }
            }
            moduleEventListenerList.addAll(newModuleEventListenerList);
        }
    }

    /**
     * 动态初始化 (耗时操作, 需要通过解压dex包来查找到所有需要的类)
     */
    @WorkerThread
    public static void dynamicInit(Context context) {
        if (mHasInit) {
            return;
        }

        final Set<String> classNameSet = ClassUtilKt.getClassNameSetByPackageName(context, Constant.AUTO_GENERATE_FILE_PACKAGE_NAME);

        for (String moduleDataLoaderClassName : classNameSet) {
            load(moduleDataLoaderClassName);
        }
        init();
        mHasInit = true;
    }

    /**
     * 静态初始化 (打包时使用Gradle插件查找到所有需要的类)
     */
    @AnyThread
    public static void staticInit() {
        if (mHasInit) {
            return;
        }
        loadByGradlePlugin();
        init();
        mHasInit = true;
    }

    /**
     * 分发事件
     *
     * @param group 组名
     */
    public static void dispatch(@NonNull String group) {
        dispatch(group, 0x0, new HashMap<>());
    }

    /**
     * 分发事件
     *
     * @param group 组名
     * @param flag  标志(建议使用位标志, int类型可用32个位标志)
     */
    public static void dispatch(@NonNull String group, int flag) {
        dispatch(group, flag, new HashMap<>());
    }

    /**
     * 分发事件
     *
     * @param group 组名
     * @param data  携带的数据
     */
    public static void dispatch(@NonNull String group, @NonNull Map<String, Object> data) {
        dispatch(group, 0x0, data);
    }

    /**
     * 分发事件
     *
     * @param group 组名
     * @param flag  标志(建议使用位标志, int类型可用32个位标志)
     * @param data  携带的数据
     */
    public static void dispatch(@NonNull String group, int flag, @NonNull Map<String, Object> data) {
        final List<ModuleEventReceiverData> moduleEventReceiverDataList = MODULE_EVENT_RECEIVER_DATA_LIST_MAP.get(group);
        final List<ModuleEventListener> moduleEventListenerList = MODULE_EVENT_LISTENER_LIST_MAP.get(group);

        if (moduleEventReceiverDataList == null || moduleEventListenerList == null) {
            return;
        }
        if (flag == Constant.ALL_FLAG) {
            for (ModuleEventListener moduleEventListener : moduleEventListenerList) {
                final boolean stopDispatch = moduleEventListener != null && moduleEventListener.onReceive(data);

                if (stopDispatch) {
                    // 阻断接下来的事件执行(借鉴Broadcast)
                    break;
                }
            }
        } else {
            for (int index = 0; index < moduleEventListenerList.size(); ++index) {
                final ModuleEventListener moduleEventListener = moduleEventListenerList.get(index);
                final ModuleEventReceiverData moduleEventReceiverData = moduleEventReceiverDataList.get(index);
                final int curFlag = moduleEventReceiverData.getFlag();

                if (curFlag == flag) {
                    final boolean stopDispatch = moduleEventListener != null && moduleEventListener.onReceive(data);

                    if (stopDispatch) {
                        // 阻断接下来的事件执行(借鉴Broadcast)
                        break;
                    }
                }
            }
        }
    }

}
