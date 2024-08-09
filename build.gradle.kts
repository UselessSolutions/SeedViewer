import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("java")
    id("com.github.johnrengelman.shadow").version("8.1.0")
    id("maven-publish")
}

group = "org.useless"
version = "beta.2"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains:annotations:24.0.0")

    implementation("com.formdev:flatlaf:3.1.1")

    implementation(files("lib/bta-7.2_01-client.jar"))

    val log4jVersion = "2.20.0"
    implementation("org.apache.logging.log4j:log4j-core:${log4jVersion}")
    implementation("org.apache.logging.log4j:log4j-api:${log4jVersion}")
    implementation("org.apache.logging.log4j:log4j-1.2-api:${log4jVersion}")
    implementation("org.apache.logging.log4j:log4j-slf4j2-impl:${log4jVersion}")
    implementation("log4j:apache-log4j-extras:1.2.17")

    annotationProcessor("org.apache.logging.log4j:log4j-core:${log4jVersion}")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

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
        attributes["Main-Class"] = "org.useless.seedviewer.Main"
        attributes["Class-Path"] = "libraries/bta-7.2_01-client.jar"
        attributes["Implementation-Version"] = version
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}