import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("java")
    id("com.github.johnrengelman.shadow").version("8.1.0")
    id("maven-publish")
}

group = "org.useless"
version = "beta.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains:annotations:24.0.0")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    implementation(files("lib/bta-7.2_01-client.jar"))
}

tasks.test {
    useJUnitPlatform()
}

task("jarViewer", ShadowJar::class) {
    group = "useless"

    from(sourceSets["main"].output.classesDirs)
    from(sourceSets["main"].resources)
    archiveFileName.set("SeedViewer-$version.jar")
    manifest {
        attributes["Main-Class"] = "org.useless.Main"
        attributes["Class-Path"] = "libraries/bta-7.2_01-client.jar"
        attributes["Implementation-Version"] = version
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}