rootProject.name = "CloudStreamPlugins"

File(rootDir, ".").listFiles()?.filter { it.isDirectory }?.forEach { module ->
    if (File(module, "build.gradle.kts").exists()) include(module.name)
}
