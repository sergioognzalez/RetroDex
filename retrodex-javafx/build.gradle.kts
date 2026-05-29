plugins {
    java
    application
    jacoco
    id("org.openjfx.javafxplugin")
}

group = "com.sergio"
version = "1.0.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

application {
    mainClass.set("com.sergio.retrodexjavafx.RetroDexFxApp")
}

javafx {
    version = "21.0.5"
    modules = listOf("javafx.controls", "javafx.graphics")
}

val e2eTest = sourceSets.create("e2eTest") {
    java.srcDir("src/e2eTest/java")
    resources.srcDir("src/e2eTest/resources")
    compileClasspath += sourceSets.main.get().output + configurations.testRuntimeClasspath.get()
    runtimeClasspath += output + compileClasspath
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
    "e2eTestImplementation"("org.junit.jupiter:junit-jupiter:5.11.4")
    "e2eTestImplementation"("org.testfx:testfx-core:4.0.18")
    "e2eTestImplementation"("org.testfx:testfx-junit5:4.0.18")
    "e2eTestImplementation"("org.hamcrest:hamcrest:3.0")
    "e2eTestImplementation"("org.openjfx:javafx-base:21.0.5:win")
    "e2eTestImplementation"("org.openjfx:javafx-controls:21.0.5:win")
    "e2eTestImplementation"("org.openjfx:javafx-graphics:21.0.5:win")
    "e2eTestImplementation"("org.openjfx:javafx-base:21.0.5:linux")
    "e2eTestImplementation"("org.openjfx:javafx-controls:21.0.5:linux")
    "e2eTestImplementation"("org.openjfx:javafx-graphics:21.0.5:linux")
}

configurations.named("e2eTestImplementation") {
    extendsFrom(configurations.testImplementation.get())
}

configurations.named("e2eTestRuntimeOnly") {
    extendsFrom(configurations.testRuntimeOnly.get())
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
    systemProperty("testfx.robot", "glass")
    systemProperty("testfx.headless", "true")
    systemProperty("prism.order", "sw")
    systemProperty("prism.text", "t2k")
    systemProperty("java.awt.headless", "true")
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)

    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }

    classDirectories.setFrom(
        files(classDirectories.files.map {
            fileTree(it) {
                include("com/sergio/retrodexjavafx/core/**")
            }
        })
    )
}

tasks.jacocoTestCoverageVerification {
    dependsOn(tasks.jacocoTestReport)

    classDirectories.setFrom(
        files(classDirectories.files.map {
            fileTree(it) {
                include("com/sergio/retrodexjavafx/core/**")
            }
        })
    )

    violationRules {
        rule {
            limit {
                minimum = "1.00".toBigDecimal()
            }
        }
    }
}

tasks.register<Test>("e2eTest") {
    description = "Runs JavaFX E2E tests with TestFX."
    group = "verification"

    testClassesDirs = e2eTest.output.classesDirs
    classpath = e2eTest.runtimeClasspath
    useJUnitPlatform()

    systemProperty("testfx.robot", "glass")
    systemProperty("prism.order", "sw")
    systemProperty("prism.text", "t2k")
    systemProperty("java.awt.headless", "false")
}
