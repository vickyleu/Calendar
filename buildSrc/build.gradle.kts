import org.jetbrains.kotlin.fir.declarations.builder.buildScript


buildscript {
    dependencies {
        classpath("com.android.library:com.android.library.gradle.plugin:${libs.versions.agp.get()}")
    }
}

plugins {
    `kotlin-dsl`
//    alias(libs.plugins.kotlin.jvm)
}


dependencies {
    implementation(project.dependencies.platform(libs.compose.bom))
    implementation(project.dependencies.platform(libs.coroutines.bom))
//    implementation(project.dependencies.platform(libs.kotlin.bom))
    implementation(libs.coroutines.jvm)

    implementation("com.android.tools.build:gradle:${libs.versions.agp.get()}")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:${libs.versions.kotlin.get()}")
//    implementation(libs.javapoet) // https://github.com/google/dagger/issues/3068
}

