import com.gitlab.ninjaphenix.gradle.api.task.MinifyJsonTask
import org.gradle.jvm.tasks.Jar

plugins {
    alias(libs.plugins.gradle.utils)
    alias(libs.plugins.gradle.forge)
    alias(libs.plugins.gradle.mixin)
}

mixin {
    disableAnnotationProcessorCheck()
}

minecraft {
    mappings("official", properties["minecraft_version"] as String)

    accessTransformer(file("src/common/resources/META-INF/accesstransformer.cfg")) // Currently, this location cannot be changed from the default.

    runs {
        create("client") {
            workingDirectory(rootProject.file("run"))
            mods {
                create("expandedstorage") {
                    source(sourceSets.main.get())
                }
            }
            // "SCAN": For mods scan.
            // "REGISTRIES": For firing of registry events.
            // "REGISTRYDUMP": For getting the contents of all registries.
            //property("forge.logging.markers", "REGISTRIES")

            // Recommended logging level for the console
            // You can set various levels here.
            // Please read: https://stackoverflow.com/questions/2031163/when-to-use-the-different-log-levels
            //property("forge.logging.console.level", "debug")
        }

        create("server") {
            workingDirectory(rootProject.file("run"))
            mods {
                create("expandedstorage") {
                    source(sourceSets.main.get())
                }
            }
            // "SCAN": For mods scan.
            // "REGISTRIES": For firing of registry events.
            // "REGISTRYDUMP": For getting the contents of all registries.
            //property("forge.logging.markers", "REGISTRIES")

            // Recommended logging level for the console
            // You can set various levels here.
            // Please read: https://stackoverflow.com/questions/2031163/when-to-use-the-different-log-levels
            //property("forge.logging.console.level", "debug")
        }
    }
}

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    minecraft("net.minecraftforge:forge:${properties["minecraft_version"]}-${properties["forge_version"]}")
    implementation(group = "org.spongepowered", name = "mixin", version = properties["mixin_version"] as String)
    annotationProcessor(group = "org.spongepowered", name = "mixin", version = properties["mixin_version"] as String, classifier = "processor")
    implementation(fg.deobf("ninjaphenix:container_library:${properties["container_library_version"]}+${properties["container_library_minecraft_version"]}:forge"))
    implementation(group = "org.jetbrains", name = "annotations", version = properties["jetbrains_annotations_version"] as String)
}

tasks.withType<ProcessResources> {
    val props = mutableMapOf("version" to properties["mod_version"]) // Needs to be mutable
    inputs.properties(props)
    filesMatching("META-INF/mods.toml") {
        expand(props)
    }
}

val jarTask = tasks.getByName<Jar>("jar") {
    archiveClassifier.set("fat")

    this.finalizedBy("reobfJar")
}

val minifyJarTask = tasks.register<MinifyJsonTask>("minJar") {
    input.set(jarTask.outputs.files.singleFile)
    archiveClassifier.set("")

    manifest.attributes(mapOf(
            "Automatic-Module-Name" to "ninjaphenix.expandedstorage",
    ))

    from(rootDir.resolve("LICENSE"))
    dependsOn(jarTask)
}

tasks.getByName("build") {
    dependsOn(minifyJarTask)
}
