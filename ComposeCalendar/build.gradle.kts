@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import com.android.build.gradle.tasks.SourceJarTask
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import java.util.Properties


plugins {
    id(libs.plugins.kotlin.multiplatform.get().pluginId)
    alias(libs.plugins.jetbrains.compose)
	id(libs.plugins.android.library.get().pluginId)
    alias(libs.plugins.ktlint)
	alias(libs.plugins.dokka)
	alias(libs.plugins.compose.compiler)
	id("maven-publish")
}


kotlin {
	targets.withType<KotlinNativeTarget> {
		binaries.all {
			freeCompilerArgs += "-Xdisable-phases=VerifyBitcode"
			freeCompilerArgs += "-Xbinary=bundleId=com.wojciechosak.calendar"
		}
	}
    androidTarget {
		publishLibraryVariants("release")
		withSourcesJar(publish = true)
		compilerOptions{
			jvmTarget.set(JvmTarget.fromTarget(libs.versions.jvmTarget.get()))
		}
    }
    jvm()
    js {
        browser()
        binaries.executable()
    }
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach {
        it.binaries.framework {
            baseName = "ComposeApp"
            isStatic = false
        }
    }

    sourceSets {
        all {
            languageSettings {
                optIn("org.jetbrains.compose.resources.ExperimentalResourceApi")
            }
        }
        commonMain.dependencies {
			implementation(project.dependencies.platform(libs.compose.bom))
			implementation(project.dependencies.platform(libs.coroutines.bom))
            implementation(compose.runtime)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
            implementation(compose.components.resources)
            implementation(libs.coroutines.core)
            implementation(libs.composeIcons.featherIcons)
            api(libs.kotlinx.datetime)
        }

        androidMain.dependencies {
            implementation(libs.androidx.appcompat)
            implementation(libs.androidx.activity.compose)
            implementation(libs.compose.ui.tooling)
            implementation(libs.coroutines.android)
        }

        jvmMain.dependencies {
            implementation(compose.desktop.common)
            implementation(compose.desktop.currentOs)
            implementation(libs.coroutines.swing)
        }

        jsMain.dependencies {
            implementation(compose.html.core)
        }

        iosMain.dependencies {
        }
    }
}


android {
    namespace = "com.wojciechosak.calendar"
    compileSdk = 34

    defaultConfig {
        minSdk = 24
    }

    sourceSets["main"].apply {
        manifest.srcFile("src/androidMain/AndroidManifest.xml")
        res.srcDirs("src/androidMain/resources")
        resources.srcDirs("src/commonMain/resources")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(libs.versions.jvmTarget.get())
        targetCompatibility = JavaVersion.toVersion(libs.versions.jvmTarget.get())
    }
    buildFeatures {
        compose = true
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.wojciechosak.calendar.desktopApp"
            packageVersion = "1.0.0"
        }
    }
}




buildscript {
	dependencies {
		val dokkaVersion = libs.versions.dokka.get()
		classpath("org.jetbrains.dokka:dokka-base:$dokkaVersion")
	}
}

//group = "io.github.ltttttttttttt"
////上传到mavenCentral命令: ./gradlew publishAllPublicationsToSonatypeRepository
////mavenCentral后台: https://s01.oss.sonatype.org/#stagingRepositories
//version = "${libs.versions.compose.plugin.get()}.beta1"

group = "com.vickyleu.calendar"
version = "1.0.2"


tasks.withType<PublishToMavenRepository> {
	val isMac = DefaultNativePlatform.getCurrentOperatingSystem().isMacOsX
	onlyIf {
		isMac.also {
			if (!isMac) logger.error(
				"""
                    Publishing the library requires macOS to be able to generate iOS artifacts.
                    Run the task on a mac or use the project GitHub workflows for publication and release.
                """
			)
		}
	}
}

val javadocJar by tasks.registering(Jar::class) {
	dependsOn(tasks.dokkaHtml)
	from(tasks.dokkaHtml.flatMap(DokkaTask::outputDirectory))
	archiveClassifier = "javadoc"
}


tasks.dokkaHtml {
	// outputDirectory = layout.buildDirectory.get().resolve("dokka")
	offlineMode = false
	moduleName = "calendar"

	// See the buildscript block above and also
	// https://github.com/Kotlin/dokka/issues/2406
//    pluginConfiguration<DokkaBase, DokkaBaseConfiguration> {
////        customAssets = listOf(file("../asset/logo-icon.svg"))
////        customStyleSheets = listOf(file("../asset/logo-styles.css"))
//        separateInheritedMembers = true
//    }

	dokkaSourceSets {
		configureEach {
			reportUndocumented = true
			noAndroidSdkLink = false
			noStdlibLink = false
			noJdkLink = false
			jdkVersion = libs.versions.jvmTarget.get().toInt()
			// sourceLink {
			//     // Unix based directory relative path to the root of the project (where you execute gradle respectively).
			//     // localDirectory.set(file("src/main/kotlin"))
			//     // URL showing where the source code can be accessed through the web browser
			//     // remoteUrl = uri("https://github.com/mahozad/${project.name}/blob/main/${project.name}/src/main/kotlin").toURL()
			//     // Suffix which is used to append the line number to the URL. Use #L for GitHub
			//     remoteLineSuffix = "#L"
			// }
		}
	}
}

val properties = Properties().apply {
	runCatching { rootProject.file("local.properties") }
		.getOrNull()
		.takeIf { it?.exists() ?: false }
		?.reader()
		?.use(::load)
}
// For information about signing.* properties,
// see comments on signing { ... } block below
val environment: Map<String, String?> = System.getenv()
extra["githubToken"] = properties["github.token"] as? String
	?: environment["GITHUB_TOKEN"] ?: ""

publishing {
	val projectName = rootProject.name
	repositories {
		/*maven {
			name = "CustomLocal"
			url = uri("file://${layout.buildDirectory.get()}/local-repository")
		}
		maven {
			name = "MavenCentral"
			setUrl("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
			credentials {
				username = extra["ossrhUsername"]?.toString()
				password = extra["ossrhPassword"]?.toString()
			}
		}*/
		maven {
			name = "GitHubPackages"
			url = uri("https://maven.pkg.github.com/vickyleu/${projectName}")
			credentials {
				username = "vickyleu"
				password = extra["githubToken"]?.toString()
			}
		}
	}

	afterEvaluate {
		publications.withType<MavenPublication> {

			artifactId = artifactId.replace(project.name, projectName.lowercase())
//			if(artifactId.endsWith("android")){
//				afterEvaluate{
//					from(components["release"])
//				}
//			}else{
//				artifact(javadocJar) // Required a workaround. See below
//			}
			artifact(javadocJar) // Required a workaround. See below
			pom {
				url = "https://github.com/vickyleu/${projectName}"
				name = projectName
				description = """
                Visit the project on GitHub to learn more.
            """.trimIndent()
				inceptionYear = "2024"
				licenses {
					license {
						name = "Apache-2.0 License"
						url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
					}
				}
				developers {
					developer {
						id = "wojciech.osak"
						name = "Wojciech.osak"
						email = ""
						roles = listOf("Mobile Developer")
						timezone = "GMT+8"
					}
				}
				contributors {
					// contributor {}
				}
				scm {
					tag = "HEAD"
					url = "https://github.com/vickyleu/${projectName}"
					connection = "scm:git:github.com/vickyleu/${projectName}.git"
					developerConnection = "scm:git:ssh://github.com/vickyleu/${projectName}.git"
				}
				issueManagement {
					system = "GitHub"
					url = "https://github.com/vickyleu/${projectName}/issues"
				}
				ciManagement {
					system = "GitHub Actions"
					url = "https://github.com/vickyleu/${projectName}/actions"
				}
			}
		}
	}
}

// TODO: Remove after https://youtrack.jetbrains.com/issue/KT-46466 is fixed
//  Thanks to KSoup repository for this code snippet
tasks.withType(AbstractPublishToMaven::class).configureEach {
	dependsOn(tasks.withType(Sign::class))
}

// * Uses signing.* properties defined in gradle.properties in ~/.gradle/ or project root
// * Can also pass from command line like below
// * ./gradlew task -Psigning.secretKeyRingFile=... -Psigning.password=... -Psigning.keyId=...
// * See https://docs.gradle.org/current/userguide/signing_plugin.html
// * and https://stackoverflow.com/a/67115705
/*signing {
    sign(publishing.publications)
}*/

