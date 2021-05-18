# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
#############################################start
#
# 模块事件分发器
#
#############################################

# 模块事件分发器 生成的类文件 反混淆
-keep class com.shijingfeng.module_event_dispatcher.auto_generate.**{*;}
# 模块事件分发器 实现 ModuleEventListener 的类反混淆
-keep class * implements com.shijingfeng.module_event_dispatcher.data.interfaces.ModuleEventListener