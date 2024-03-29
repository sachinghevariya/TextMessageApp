// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.1.2" apply false
    id("org.jetbrains.kotlin.android") version "1.8.0" apply false
    id("com.google.dagger.hilt.android") version "2.48" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.10" apply false
    id("org.jetbrains.kotlin.plugin.parcelize") version "1.9.10" apply false
    id("com.android.library") version "8.1.2" apply false
    id("com.google.devtools.ksp") version "1.9.10-1.0.13" apply false
//    id("io.realm.kotlin") version "1.11.0" apply false

}
buildscript {
    dependencies {
//        classpath("com.android.tools.build:gradle:7.1.3")
        classpath("com.google.gms:google-services:4.4.0")
        classpath("com.google.firebase:firebase-crashlytics-gradle:2.9.9")
//        classpath("io.michaelrocks:paranoid-gradle-plugin:0.3.7")
        classpath("com.google.firebase:perf-plugin:1.4.2")
        classpath("io.realm:realm-gradle-plugin:10.16.1")
    }
}
