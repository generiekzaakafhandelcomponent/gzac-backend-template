fun configureEnvironment(task: ProcessForkOptions) {
    val f = File("${rootProject.projectDir}/.env.properties")
    if (f.isFile) {
        f.readLines().forEach {
            if (it.isNotEmpty() && !it.startsWith("#")) {
                val pair = it.split("=", limit = 2)
                task.environment[pair[0]] = pair[1]
            }
        }
    }
}
extra["configureEnvironment"] = { task: ProcessForkOptions -> configureEnvironment(task) }