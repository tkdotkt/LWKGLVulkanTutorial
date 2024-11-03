package tk.kt.LWKGLVulkanTutorial.engine.graph.vk

import org.lwjgl.glfw.GLFWVulkan
import org.lwjgl.system.MemoryStack
import org.lwjgl.vulkan.KHRSurface
import org.tinylog.kotlin.Logger


class Surface(private val physicalDevice:PhysicalDevice, windowHandle : Long) {
    private var vkSurface : Long = 0

    init {
        init(physicalDevice, windowHandle)
    }

    private fun init(physicalDevice: PhysicalDevice, windowHandle: Long) {
        Logger.debug("Creating Vulkan Surface")
        MemoryStack.stackPush().use { stack ->
            val pSurface = stack.mallocLong(1)
            GLFWVulkan.glfwCreateWindowSurface(
                physicalDevice.getVkPhysicalDevice().instance,
                windowHandle,
                null,
                pSurface
            )
            vkSurface = pSurface[0]
        }
    }

    fun cleanup() {
        Logger.debug("Destroying Vulkan surface")
        KHRSurface.vkDestroySurfaceKHR(physicalDevice.getVkPhysicalDevice().instance, vkSurface, null)
    }

    fun getVkSurface(): Long {
        return vkSurface
    }

}