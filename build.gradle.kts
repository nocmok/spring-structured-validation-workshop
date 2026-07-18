plugins {
    kotlin("jvm") version "2.3.21" apply false
    kotlin("plugin.spring") version "2.3.21" apply false
    id("org.springframework.boot") version "4.1.0" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
    id("org.openapi.generator") version "7.14.0" apply false
}

allprojects {
    group = "com.github.nocmok"
    version = "0.0.1-SNAPSHOT"
}
