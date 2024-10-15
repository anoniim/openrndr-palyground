# OPENRNDR playground project 

A sketch book full of [OPENRNDR](https://openrndr.org/) programs producing visually interesting and often interactive animations.

The programs are written in Kotlin and built with Gradle.

## Gradle tasks
 - `run` runs the program
 - `jar` creates an executable platform specific jar file with all dependencies
 - `zipDistribution` creates a zip file containing the application jar and the data folder
 - `jpackageZip` creates a zip with a stand-alone executable for the current platform (works with Java 14 only)

## Cross builds
To create runnable jars for a platform different from the platform you use to build use `./gradlew jar --PtargetPlatform=<platform>`. The supported platforms are `windows`, `macos`, `linux-x64` and `linux-arm64`. 

# Examples

* https://www.youtube.com/shorts/ju0FQcIc7ao
* https://www.youtube.com/shorts/Cfl20sGy0wA
* https://www.youtube.com/shorts/3lltNY_-jW4
* https://www.youtube.com/shorts/MZUwJcL1ls4
