package tk.kt.LWKGLVulkanTutorial.engine.graph

import org.tinylog.kotlin.Logger
import tk.kt.LWKGLVulkanTutorial.engine.EngineProperties
import tk.kt.LWKGLVulkanTutorial.engine.Window
import tk.kt.LWKGLVulkanTutorial.engine.graph.vk.Instance
import tk.kt.LWKGLVulkanTutorial.engine.scene.Scene


class Render(window: Window, scene: Scene) {

    lateinit var instance : Instance

    init {
        init(window, scene)
    }

    fun init(window : Window, scene : Scene) {
        Logger.debug{"Render has been initialised"}
        val engProps = EngineProperties.getInstance()
        instance = Instance(engProps.isValidate())
    }

    fun cleanup() {
        Logger.debug("Render cleanup has been called")

        Logger.debug("Cleaning Vulkan Instance")
        instance.cleanup()
    }

    fun render(window : Window, scene : Scene) {

    }
}