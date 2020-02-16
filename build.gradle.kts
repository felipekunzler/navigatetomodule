plugins {
    id("org.jetbrains.intellij") version "0.4.16"
    kotlin("jvm") version "1.3.61"
}

group = "com.navigatetomodule"
version = "0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
}

// See https://github.com/JetBrains/gradle-intellij-plugin/
intellij {
    version = "2019.3.3"
}
tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    publishPlugin {
        if (project.hasProperty("intellijPublishToken")) {
            token(project.property("intellijPublishToken"))
        }
    }
}
tasks.getByName<org.jetbrains.intellij.tasks.PatchPluginXmlTask>("patchPluginXml") {
}
