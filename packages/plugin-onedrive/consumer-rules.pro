# Keep the names of JSON-serializable objects.
-keepnames public class * implements com.microsoft.graph.serializer.IJsonBackedObject

## GSON 2.2.4 specific rules ##
# Source: https://github.com/google/gson/blob/main/examples/android-proguard-example/proguard.cfg
# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature
# For using GSON @Expose annotation
-keepattributes *Annotation*
-keepattributes EnclosingMethod
# Gson specific classes
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.stream.** { *; }
# Gson specific classes used by Broker.
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

-dontnote sun.misc.Unsafe
-dontnote com.google.gson.**

# JUnit
-dontwarn org.junit.**
-dontwarn junit.**

# okhttp
-keepattributes Signature
-keepattributes *Annotation*
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**

# okio
-keep class sun.misc.Unsafe { *; }
-dontwarn java.nio.file.*
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn okio.**

# blockhound
-dontwarn reactor.blockhound.integration.BlockHoundIntegration