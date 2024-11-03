package tk.kt.LWKGLVulkanTutorial.engine.graph.vk

import org.lwjgl.system.MemoryStack
import org.lwjgl.vulkan.VK10.*
import org.lwjgl.vulkan.VkQueue
import org.tinylog.kotlin.Logger


open class Queue(device : Device, queueFamilyIndex : Int, queueIndex : Int) {
    init{init(device, queueFamilyIndex, queueIndex)}
    private lateinit var vkQueue : VkQueue

    private fun init(device : Device, queueFamilyIndex : Int, queueIndex : Int) {
        Logger.debug("Creating queue")

        MemoryStack.stackPush().use { stack ->
            val pQueue = stack.mallocPointer(1)
            vkGetDeviceQueue(device.getVkDevice(), queueFamilyIndex, queueIndex, pQueue)
            val queue = pQueue[0]
            vkQueue = VkQueue(queue, device.getVkDevice())
        }
    }

    fun getVkQueue(): VkQueue {
        return vkQueue
    }

    fun waitIdle() {
        vkQueueWaitIdle(vkQueue)
    }

    companion object {
        class GraphicsQueue(device: Device, queueIndex: Int) :
            Queue(device, getGraphicsQueueFamilyIndex(device), queueIndex) {
            companion object {
                private fun getGraphicsQueueFamilyIndex(device: Device): Int {
                    var index = -1
                    val physicalDevice = device.getPhysicalDevice()
                    val queuePropsBuff = physicalDevice.getVkQueueFamilyProps()
                    val numQueuesFamilies = queuePropsBuff!!.capacity()
                    for (i in 0 until numQueuesFamilies) {
                        val props = queuePropsBuff[i]
                        val graphicsQueue = (props.queueFlags() and VK_QUEUE_GRAPHICS_BIT) != 0
                        if (graphicsQueue) {
                            index = i
                            break
                        }
                    }

                    if (index < 0) {
                        throw RuntimeException("Failed to get graphics Queue family index")
                    }
                    return index
                }
            }
        }
    }
}