package com.shijingfeng.module_event_dispatcher.api.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.MODE_MULTI_PROCESS
import android.content.Context.MODE_PRIVATE
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.WorkerThread
import dalvik.system.DexFile
import kotlinx.coroutines.*
import java.io.File
import java.io.IOException
import java.util.*
import java.util.regex.Pattern.compile

/**
 * Function: 类相关工具类 (参考 ARouter 的 ClassUtils 工具类)
 * Date: 2020/12/6 16:42
 * Description:
 * Author: ShiJingFeng
 */

private const val EXTRACTED_NAME_EXT = ".classes"
private const val EXTRACTED_SUFFIX = ".zip"

private val SECONDARY_FOLDER_NAME = "code_cache" + File.separator + "secondary-dexes"

private const val PREFS_FILE = "multidex.version"
private const val KEY_DEX_NUMBER = "dex.number"

private const val VM_WITH_MULTIDEX_VERSION_MAJOR = 2
private const val VM_WITH_MULTIDEX_VERSION_MINOR = 1

/**
 * 通过包名获取App内文件名
 * 注意: 该操作是耗时操作
 * 如果获取的文件是Java类文件，则可以直接通过发射加载类
 * 如果是Kotlin文件, 则需要特殊处理
 */
@WorkerThread
@Suppress("DEPRECATION")
internal fun getClassNameSetByPackageName(
    context: Context,
    packageNameSet: Set<String>
): Map<String, Set<String>> {
    val classNameSetMap = mutableMapOf<String, MutableSet<String>>()
    val sourcePathList = getSourcePathList(context)

    if (sourcePathList.isEmpty()) {
        return classNameSetMap
    }
    sourcePathList.forEach { path ->
        var dexFile: DexFile? = null

        try {
            dexFile = if (path.endsWith(EXTRACTED_SUFFIX)) {
                DexFile.loadDex(path, "$path.tmp", 0)
            } else {
                DexFile(path)
            }

            dexFile?.entries()?.run {
                while (hasMoreElements()) {
                    val className = nextElement()

                    packageNameSet.forEach { packageName ->
                        if (className.startsWith(packageName)) {
                            val classNameSet = classNameSetMap[packageName]

                            if (classNameSet === null) {
                                classNameSetMap[packageName] = mutableSetOf(className)
                            } else {
                                classNameSet.add(className)
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                dexFile?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    return classNameSetMap
}

/**
 * 使用协程
 * 通过包名获取App内文件名
 * 注意: 该操作是耗时操作
 * 如果获取的文件是Java类文件，则可以直接通过发射加载类
 * 如果是Kotlin文件, 则需要特殊处理
 */
@WorkerThread
@Suppress("DEPRECATION")
internal suspend fun getClassNameSetByPackageNameUseCoroutine(
    context: Context,
    packageNameSet: Set<String>,
): Map<String, Set<String>> = withContext(context = Dispatchers.IO) {
    val classNameSetMap = mutableMapOf<String, MutableSet<String>>()
    val sourcePathList = getSourcePathList(context)

    if (sourcePathList.isEmpty()) {
        return@withContext classNameSetMap
    }

    val deferredList = mutableListOf<Deferred<Any>>()

    sourcePathList.forEach { path ->
        // 注意: context 使用 getCustomThreadExecutorInstance().asCoroutineDispatcher() 会导致弹出警告 Inappropriate blocking method call
        // 故使用 Dispatchers.IO
        // 注意: async{}.await() 和 val result = async{} result.await() 不一样
        // 前者会阻塞, 导致后面的代码暂停执行, 而后者不会
        val deferred = async(context = Dispatchers.IO) {
            var dexFile: DexFile? = null

            try {
                dexFile = if (path.endsWith(EXTRACTED_SUFFIX)) {
                    DexFile.loadDex(path, "$path.tmp", 0)
                } else {
                    DexFile(path)
                }

                dexFile?.entries()?.run {
                    while (hasMoreElements()) {
                        val className = nextElement()

                        packageNameSet.forEach { packageName ->
                            if (className.startsWith(packageName)) {
                                val classNameSet = classNameSetMap[packageName]

                                if (classNameSet === null) {
                                    classNameSetMap[packageName] = mutableSetOf(className)
                                } else {
                                    classNameSet.add(className)
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                try {
                    dexFile?.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return@async
            }
        }

        deferredList.add(deferred)
    }

    deferredList.forEach { deferred ->
        deferred.await()
    }
    return@withContext classNameSetMap
}

/**
 * 通过包名获取App内文件名
 * 注意: 该操作是耗时操作
 * 如果获取的文件是Java类文件，则可以直接通过发射加载类
 * 如果是Kotlin文件, 则需要特殊处理
 */
@WorkerThread
@Suppress("DEPRECATION")
internal fun getClassNameSetByPackageName(
    context: Context,
    packageName: String,
): Set<String> {
    val classNameSet = mutableSetOf<String>()
    val sourcePathList = getSourcePathList(context)

    if (sourcePathList.isEmpty()) {
        return classNameSet
    }

    sourcePathList.forEach { path ->
        var dexFile: DexFile? = null

        try {
            dexFile = if (path.endsWith(EXTRACTED_SUFFIX)) {
                DexFile.loadDex(path, "$path.tmp", 0)
            } else {
                DexFile(path)
            }

            dexFile?.entries()?.run {
                while (hasMoreElements()) {
                    val className = nextElement()

                    if (className.startsWith(packageName)) {
                        classNameSet.add(className)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                dexFile?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    return classNameSet
}

/**
 * 使用协程
 * 通过包名获取App内文件名
 * 注意: 该操作是耗时操作
 * 如果获取的文件是Java类文件，则可以直接通过发射加载类
 * 如果是Kotlin文件, 则需要特殊处理
 */
@WorkerThread
@Suppress("DEPRECATION")
internal suspend fun getClassNameSetByPackageNameUseCoroutine(
    context: Context,
    packageName: String,
): Set<String> = withContext(context = Dispatchers.IO) {
    val classNameSet = mutableSetOf<String>()
    val sourcePathList = getSourcePathList(context)

    if (sourcePathList.isEmpty()) {
        return@withContext classNameSet
    }

    val deferredList = mutableListOf<Deferred<Any>>()

    sourcePathList.forEach { path ->
        // 注意: context 使用 getCustomThreadExecutorInstance().asCoroutineDispatcher() 会导致弹出警告 Inappropriate blocking method call
        // 故使用 Dispatchers.IO
        // 注意: async{}.await() 和 val result = async{} result.await() 不一样
        // 前者会阻塞, 导致后面的代码暂停执行, 而后者不会
        val deferred = async(context = Dispatchers.IO) {
            var dexFile: DexFile? = null

            try {
                dexFile = if (path.endsWith(EXTRACTED_SUFFIX)) {
                    DexFile.loadDex(path, "$path.tmp", 0)
                } else {
                    DexFile(path)
                }

                dexFile?.entries()?.run {
                    while (hasMoreElements()) {
                        val className = nextElement()

                        if (className.startsWith(packageName)) {
                            classNameSet.add(className)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                try {
                    dexFile?.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return@async
            }
        }

        deferredList.add(deferred)
    }

    deferredList.forEach { deferred ->
        deferred.await()
    }
    return@withContext classNameSet
}

/**
 * 获取apk路径列表(分包后会有多个文件)
 */
internal fun getSourcePathList(context: Context): List<String> {
    val sourcePathList = mutableListOf<String>()
    val appInfo: ApplicationInfo

    try {
        appInfo = context.packageManager.getApplicationInfo(context.packageName, 0)
    } catch (e: PackageManager.NameNotFoundException) {
        return sourcePathList
    }
    val sourceApk = File(appInfo.sourceDir)
    // 其他的文件前缀名称(dex分包后其他额外的包) 例如: base.apk.classes
    val extraFilePrefix = sourceApk.name + EXTRACTED_NAME_EXT

    // 添加默认apk路径
    sourcePathList.add(appInfo.sourceDir)
    // 注意: 通过是否存在sp中的multidex.version是不准确的，因为从低版本升级上来的用户，是包含这个sp配置的
    if (!isVMMultidexCapable) {
        // VM不支持dex分包(Api21以下), 使用Multidex框架不影响
        val totalDexNumber = getMultiDexPreferences(context).getInt(KEY_DEX_NUMBER, 1)
        val dexDir = File(appInfo.dataDir, SECONDARY_FOLDER_NAME)

        for (secondaryNumber in 2..totalDexNumber) {
            val extraFileName = extraFilePrefix + secondaryNumber + EXTRACTED_SUFFIX
            val extraFile = File(dexDir, extraFileName)

            if (extraFile.isFile) {
                sourcePathList.add(extraFile.absolutePath)
            } else {
                throw IOException("Missing extracted secondary dex file '" + extraFile.absolutePath + "'")
            }
        }
    }
    return sourcePathList
}

/**
 * VM是否可以有dex分包(multidex)能力, 注: Android5.0(API 21)以上 VM 有此能力，以下需要使用 Multidex 框架
 *
 * @return true: VM有dex分包能力
 */
private val isVMMultidexCapable: Boolean
        get() = try {
            if (isAliYunOS()) {
                val sdkVersion = System.getProperty("ro.build.version.sdk")?.toInt() ?: Build.VERSION_CODES.BASE

                sdkVersion >= Build.VERSION_CODES.LOLLIPOP
            } else {
                val vmVersion = System.getProperty("java.vm.version")

                if (!vmVersion.isNullOrEmpty()) {
                    val matcher = compile("(\\\\d+)\\\\.(\\\\d+)(\\\\.\\\\d+)?").matcher(vmVersion)

                    if (matcher.matches()) {
                        try {
                            val major = matcher.group(1)?.toInt() ?: 0
                            val minor = matcher.group(2)?.toInt() ?: 0

                            (major > VM_WITH_MULTIDEX_VERSION_MAJOR) || ((major == VM_WITH_MULTIDEX_VERSION_MAJOR) && (minor >= VM_WITH_MULTIDEX_VERSION_MINOR))
                        } catch (e: Exception) {
                            false
                        }
                    } else {
                        false
                    }
                } else {
                    false
                }
            }
        } catch (e: Exception) { false }

/**
 * 判断是不是阿里云操作系统 (基于Android, 车辆网和物联网上可能有用到的)
 *
 * @return true: 是 阿里云OS
 */
private fun isAliYunOS() = try {
    val version = System.getProperty("ro.yunos.version")
    val vmName = System.getProperty("java.vm.name")

    (vmName != null && vmName.toLowerCase(Locale.getDefault()).contains("lemur")) || (version != null && version.trim().isNotEmpty())
} catch (e: Exception) {
    false
}

/**
 * 获取 MultiDex 存储在本地的 SharedPreferences
 */
@SuppressLint("ObsoleteSdkInt")
private fun getMultiDexPreferences(
    context: Context
) = context.getSharedPreferences(PREFS_FILE, if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) MODE_PRIVATE else (MODE_PRIVATE or MODE_MULTI_PROCESS))