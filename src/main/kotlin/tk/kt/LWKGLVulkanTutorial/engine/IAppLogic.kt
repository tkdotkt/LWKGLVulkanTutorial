package tk.kt.LWKGLVulkanTutorial.engine

import tk.kt.LWKGLVulkanTutorial.engine.graph.Render
import tk.kt.LWKGLVulkanTutorial.engine.scene.Scene

interface IAppLogic {
    fun init(window: Window?, scene: Scene?, render: Render?)
    fun input(window: Window?, scene: Scene?, diffTimeMillis: Long)
    fun update(window: Window?, scene: Scene?, diffTimeMillis: Long)
    fun cleanup()
}