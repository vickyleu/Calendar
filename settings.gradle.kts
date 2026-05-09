rootProject.name = "Calendar"
//include(":sample:composeApp")
include(":ComposeCalendar")

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.aliyun.com/repository/google")
        maven("https://maven.aliyun.com/repository/public")
        maven("https://maven.aliyun.com/repository/gradle-plugin")
        maven {
            setUrl("https://plugins.gradle.org/m2/")
        }
    }
}

dependencyResolutionManagement {
    repositories {
        maven("https://maven.aliyun.com/repository/google") {
            content {
                excludeModule("androidx.savedstate", "savedstate-js")
                excludeModule("androidx.savedstate", "savedstate-compose-js")
            }
        }
        google {
            content {
                includeModule("androidx.savedstate", "savedstate-js")
                includeModule("androidx.savedstate", "savedstate-compose-js")
            }
        }
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.aliyun.com/repository/public")
        maven("https://maven.aliyun.com/repository/gradle-plugin")
    }
}
