rootProject.name = "Calendar"
//include(":sample:composeApp")
include(":ComposeCalendar")

pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
        maven {
            setUrl("https://plugins.gradle.org/m2/")
        }
    }
}

dependencyResolutionManagement {
    repositories {
        google{
			content {
				excludeGroupByRegex("com.vickyleu.*")
				excludeGroupByRegex("com.github.*")
				excludeGroupByRegex("io.github.*")
			}
		}
        mavenCentral{
			content {
				excludeGroupByRegex("com.vickyleu.*")
				excludeGroupByRegex("com.github.*")
				excludeGroupByRegex("io.github.*")
			}
		}
        maven {
            setUrl("https://plugins.gradle.org/m2/")
			content {
				excludeGroupByRegex("com.vickyleu.*")
				excludeGroupByRegex("com.github.*")
				excludeGroupByRegex("io.github.*")
			}
        }

		val properties = java.util.Properties().apply {
			runCatching { rootProject.projectDir.resolve("local.properties") }
				.getOrNull()
				.takeIf { it?.exists() ?: false }
				?.reader()
				?.use(::load)
		}
		val environment: Map<String, String?> = System.getenv()
		extra["githubToken"] = properties["github.token"] as? String
			?: environment["GITHUB_TOKEN"] ?: ""
		maven {
			url = uri("https://maven.pkg.github.com/vickyleu/${rootProject.name.lowercase()}")
			credentials {
				username = "vickyleu"
				password = extra["githubToken"]?.toString()
			}
			content {
				excludeGroupByRegex("(?!com|cn).github.(?!vickyleu).*")
			}
		}

    }

}
