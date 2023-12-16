plugins {
    id("java")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation ("org.xerial:sqlite-jdbc:3.34.0")
    implementation ("org.zeromq:jeromq:0.5.4")
    implementation ("com.google.code.gson:gson:2.8.9")
}

tasks.test {
    useJUnitPlatform()
}