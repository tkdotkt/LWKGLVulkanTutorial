package tk.kt.LWKGLVulkanTutorial.engine

import org.tinylog.Logger
import tk.kt.LWKGLVulkanTutorial.engine.graph.Render
import tk.kt.LWKGLVulkanTutorial.engine.scene.Scene

class Engine(
    private val windowTitle : String,
    private val appLogic : IAppLogic
){
    private val window : Window = Window(windowTitle)
    private val scene : Scene = Scene(window)
    private val render : Render = Render(window, scene)
    private var running = false

    init {
        appLogic.init(window, scene, render)
    }

    private fun cleanup() {
        Logger.debug{"Cleaning up Engine"}

        Logger.debug{"Cleaning appLogic"}
        appLogic.cleanup();
        Logger.debug{"Cleaning renderer"}
        render.cleanup();
        Logger.debug{"Cleaning window"}
        window.cleanup();
    }

    fun run() {
        val engineProperties: EngineProperties = EngineProperties.getInstance()
        var initialTime = System.currentTimeMillis()
        val timeU: Float = 1000.0f / engineProperties.getUps()
        var deltaUpdate = 0.0

        var updateTime = initialTime
        while (running && !window.shouldClose()) {
            window.pollEvents()

            val now = System.currentTimeMillis()
            deltaUpdate += ((now - initialTime) / timeU).toDouble()

            appLogic.input(window, scene, now - initialTime)

            if (deltaUpdate >= 1) {
                val diffTimeMilis = now - updateTime
                appLogic.update(window, scene, diffTimeMilis)
                updateTime = now
                deltaUpdate--
            }

            render.render(window, scene)

            initialTime = now
        }

        cleanup()
    }

    fun start() {
        Logger.debug{"Engine is starting"}
        Logger.debug{"Set running = true"}
        running = true
        run()
    }

    fun stop() {
        Logger.debug{"Stop command, stopping engine"}
        Logger.debug{"Set running = false"}
        running = false
    }

}