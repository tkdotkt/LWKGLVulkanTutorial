package tk.kt.LWKGLVulkanTutorial

import org.tinylog.Logger
import tk.kt.LWKGLVulkanTutorial.engine.*
import tk.kt.LWKGLVulkanTutorial.engine.graph.Render
import tk.kt.LWKGLVulkanTutorial.engine.scene.Scene


class Main : IAppLogic {
    override fun cleanup() {
        Logger.debug{"Cleaning up Main class"}
        // To be implemented
    }

    override fun init(window: Window?, scene: Scene?, render: Render?) {
        Logger.debug{"Initialising window class"}
        // To be implemented
    }

    override fun input(window: Window?, scene: Scene?, diffTimeMillis: Long) {
        // To be implemented
    }

    override fun update(window: Window?, scene: Scene?, diffTimeMillis: Long) {
        // To be implemented
    }



    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            Logger.info{"Starting application"}
            val engine: Engine = Engine("Vulkan Book", Main())
            engine.start()
        }
    }
}