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
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
}

-flattenpackagehierarchy
-keepnames @dagger.hilt.android.lifecycle.HiltViewModel class * extends androidx.lifecycle.ViewModel
-keep,allowobfuscation,allowshrinking @dagger.hilt.android.EarlyEntryPoint class *

-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.examples.android.model.** { <fields>; }
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}
-keep,allowobfuscation,allowshrinking class com.google.gson.reflect.TypeToken
-keep,allowobfuscation,allowshrinking class * extends com.google.gson.reflect.TypeToken


-keep class messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.* { *; }
-keep class messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.BackupDb { *; }
-keep class messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.BackupType { *; }
-keep class messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.BlockedNumber { *; }
-keep class messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.Conversation { *; }
-keep class messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.Converter { *; }
-keep class messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.DataContainer { *; }
-keep class messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.Message { *; }
-keep class messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.MessageType { *; }
-keep class messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.PhoneNumber { *; }
-keep class messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.ScheduleMessage { *; }
-keep class messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.SearchResult { *; }
-keep class messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.SimpleContact { *; }
-keep class messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.SmsBackup { *; }
-keep class messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.SyncDb { *; }
-keep class messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.ThreadItem { *; }
-keep class messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.UpdateSetting { *; }
-keep class messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.UpdateConversations { *; }
-keep class messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.UpdateSwipeAction { *; }
-keep class messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.RefreshMessages { *; }
-keep class messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.Callback { *; }
-keep class messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.CallbackS { *; }
-keep class messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.CallbackSchedule { *; }
-keep class messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.CallbackString { *; }
-keep class messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.UpdateProgress { *; }
-keep class messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.UpdateProgressStatus { *; }
-keep class messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.UpdateContacts { *; }
-keep class messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.Backup { *; }
-keep class messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.MessageNew { *; }
-keep class messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.ConversationNew { *; }
-keep class messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.BackupMessage { *; }
-keep class messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.PhoneNumber { *; }
-keep class messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.Recipient { *; }

