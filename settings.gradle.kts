pluginManagement {
	repositories {
		maven {
			url = uri("https://maven.aliyun.com/repository/gradle-plugin")
		}
		google()
		mavenCentral()
		gradlePluginPortal()
	}
}
dependencyResolutionManagement {
	repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
	repositories {
		maven {
			url = uri("https://maven.aliyun.com/repository/central")
		}
		maven {
			url = uri("https://maven.aliyun.com/repository/public")
		}
		google()
		mavenCentral()
	}
}

rootProject.name = "Dex2oat"
include(":app")
 