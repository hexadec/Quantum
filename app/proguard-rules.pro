# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# This is of crucial importance to keep predefined operators
# and other methods / fields related to them
-keepclassmembers class hu.hexadecimal.quantum.* {
    public *;
}

-keepclassmembers class hu.hexadecimal.quantum.graphics* {
    public *;
}

-keep class hu.hexadecimal.quantum.*
-keep class hu.hexadecimal.quantum.graphics.*

# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable
-dontobfuscate

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
