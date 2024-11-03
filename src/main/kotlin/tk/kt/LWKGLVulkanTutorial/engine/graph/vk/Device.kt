package tk.kt.LWKGLVulkanTutorial.engine.graph.vk

import org.lwjgl.PointerBuffer
import org.lwjgl.system.MemoryStack
import org.lwjgl.vulkan.*
import org.lwjgl.vulkan.KHRPortabilitySubset.VK_KHR_PORTABILITY_SUBSET_EXTENSION_NAME
import org.lwjgl.vulkan.VK10.*
import org.lwjgl.vulkan.VK11.VK_STRUCTURE_TYPE_DEVICE_CREATE_INFO
import org.lwjgl.vulkan.VK11.vkEnumerateDeviceExtensionProperties
import org.tinylog.kotlin.Logger
import tk.kt.LWKGLVulkanTutorial.engine.graph.vk.VulkanUtils.vkCheck


class Device(private val physicalDevice: PhysicalDevice) {
    private lateinit var vkDevice : VkDevice

    init {
        init(physicalDevice)
    }

    private fun init(physicalDevice: PhysicalDevice) {
        Logger.debug("Creating device")

        try {
            MemoryStack.stackPush().use { stack ->
                val deviceExtensions : Set<String> = getDeviceExtensions()
                val usePortability : Boolean = (deviceExtensions.contains(VK_KHR_PORTABILITY_SUBSET_EXTENSION_NAME) && VulkanUtils.getOS() == VulkanUtils.OSType.MACOS)
                val numExtensions = if (usePortability) 2 else 1
                val requiredExtensions : PointerBuffer = stack.mallocPointer(numExtensions)
                requiredExtensions.put(stack.ASCII(KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME));
                if (usePortability) {
                    requiredExtensions.put(stack.ASCII(VK_KHR_PORTABILITY_SUBSET_EXTENSION_NAME));
                }
                requiredExtensions.flip();

                // Setup required features
                val features : VkPhysicalDeviceFeatures = VkPhysicalDeviceFeatures.calloc(stack)

                // Enable all the queue families
                val queuePropsBuff : VkQueueFamilyProperties.Buffer? = physicalDevice.getVkQueueFamilyProps()
                val numQueuesFamilies: Int = queuePropsBuff!!.capacity()
                val queueCreationInfoBuf = VkDeviceQueueCreateInfo.calloc(numQueuesFamilies, stack)
                for (i in 0 until numQueuesFamilies) {
                    val priorities = stack.callocFloat(queuePropsBuff.get(i).queueCount())
                    queueCreationInfoBuf[i]
                        .sType(VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO)
                        .queueFamilyIndex(i)
                        .pQueuePriorities(priorities)
                }

                val deviceCreateInfo : VkDeviceCreateInfo = VkDeviceCreateInfo.calloc(stack)
                    .sType(VK_STRUCTURE_TYPE_DEVICE_CREATE_INFO)
                    .ppEnabledExtensionNames(requiredExtensions)
                    .pEnabledFeatures(features)
                    .pQueueCreateInfos(queueCreationInfoBuf)

                val pp : PointerBuffer = stack.mallocPointer(1)
                vkCheck(
                    vkCreateDevice(physicalDevice.getVkPhysicalDevice(), deviceCreateInfo, null, pp),
                    "Failed to create device"
                )
                vkDevice = VkDevice(pp[0], physicalDevice.getVkPhysicalDevice(), deviceCreateInfo)
            }
        } catch (e: Exception) {
            Logger.debug("An error has occurred while creating the device: ${e.printStackTrace()}", e)
            throw e
        }
    }

    private fun getDeviceExtensions() : Set<String> {
        val deviceExtensions: MutableSet<String> = HashSet()
        MemoryStack.stackPush().use { stack ->
            val numExtensionsBuf = stack.callocInt(1)
            vkEnumerateDeviceExtensionProperties(
                physicalDevice.getVkPhysicalDevice(),
                null as String?,
                numExtensionsBuf,
                null
            )
            val numExtensions = numExtensionsBuf[0]
            Logger.debug("Device supports [{}] extensions", numExtensions)

            val propsBuff = VkExtensionProperties.calloc(numExtensions, stack)
            vkEnumerateDeviceExtensionProperties(
                physicalDevice.getVkPhysicalDevice(),
                null as String?,
                numExtensionsBuf,
                propsBuff
            )
            for (i in 0 until numExtensions) {
                val props = propsBuff[i]
                val extensionName = props.extensionNameString()
                deviceExtensions.add(extensionName)
                Logger.debug("Supported device extension [{}]", extensionName)
            }
        }
        return deviceExtensions
    }

    fun cleanup() {
        Logger.debug("Destroying Vulkan device")
        vkDestroyDevice(vkDevice, null)
    }

    fun getPhysicalDevice(): PhysicalDevice {
        return physicalDevice
    }

    fun getVkDevice(): VkDevice {
        return vkDevice
    }

    fun waitIdle() {
        vkDeviceWaitIdle(vkDevice)
    }
}