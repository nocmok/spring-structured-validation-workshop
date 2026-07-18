import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
	kotlin("jvm")
	id("org.springframework.boot")
	id("io.spring.dependency-management")
	id("org.openapi.generator")
}

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	api("org.springframework:spring-webmvc")
	api("jakarta.servlet:jakarta.servlet-api")
	api("jakarta.validation:jakarta.validation-api")
	api("com.fasterxml.jackson.core:jackson-annotations")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
	}
}

// This module only produces a plain library jar, not a runnable Spring Boot application.
tasks.named<BootJar>("bootJar") {
	enabled = false
}

tasks.named<Jar>("jar") {
	enabled = true
}

val openApiGeneratedDir = layout.buildDirectory.dir("generated/openapi")

openApiGenerate {
	generatorName.set("kotlin-spring")
	inputSpec.set(project.file("openapi.yaml").path)
	outputDir.set(openApiGeneratedDir.get().asFile.path)
	apiPackage.set("com.github.nocmok.api")
	modelPackage.set("com.github.nocmok.api.model")
	configOptions.set(
		mapOf(
			"interfaceOnly" to "true",
			"useTags" to "true",
			"useBeanValidation" to "true",
			"useSpringBoot3" to "true",
			"exceptionHandler" to "false",
			"gradleBuildFile" to "false",
			"documentationProvider" to "none",
		)
	)
}

sourceSets {
	main {
		kotlin.srcDir(openApiGeneratedDir.map { it.dir("src/main/kotlin") })
	}
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
	dependsOn(tasks.openApiGenerate)
}
