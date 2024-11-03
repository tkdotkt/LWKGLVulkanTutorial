package tk.kt.LWKGLVulkanTutorial.engine.graph.vk

import org.lwjgl.PointerBuffer
import org.lwjgl.system.MemoryStack
import org.lwjgl.vulkan.*
import org.lwjgl.vulkan.VK10.*
import org.tinylog.kotlin.Logger
import tk.kt.LWKGLVulkanTutorial.engine.graph.vk.VulkanUtils.vkCheck


open class PhysicalDevice(vkPhysicalDevice: VkPhysicalDevice) {

    private var vkDeviceExtensions: VkExtensionProperties.Buffer? = null
    private var vkMemoryProperties: VkPhysicalDeviceMemoryProperties? = null
    private var vkPhysicalDevice: VkPhysicalDevice? = null
    private var vkPhysicalDeviceFeatures: VkPhysicalDeviceFeatures? = null
    private var vkPhysicalDeviceProperties: VkPhysicalDeviceProperties? = null
    private var vkQueueFamilyProps: VkQueueFamilyProperties.Buffer? = null

    init {
        init(vkPhysicalDevice!!)
    }

    private fun init(vkPhysicalDevice: VkPhysicalDevice) {
        try {
            MemoryStack.stackPush().use { stack ->
                this.vkPhysicalDevice = vkPhysicalDevice
                val intBuffer = stack.mallocInt(1)


                // Get device properties
                vkPhysicalDeviceProperties = VkPhysicalDeviceProperties.calloc()
                vkGetPhysicalDeviceProperties(vkPhysicalDevice, vkPhysicalDeviceProperties)


                // Get device extensions
                vkCheck(
                    vkEnumerateDeviceExtensionProperties(vkPhysicalDevice, null as String?, intBuffer, null),
                    "Failed to get number of device extension properties"
                )
                vkDeviceExtensions = VkExtensionProperties.calloc(intBuffer[0])
                vkCheck(
                    vkEnumerateDeviceExtensionProperties(vkPhysicalDevice, null as String?, intBuffer, vkDeviceExtensions),
                    "Failed to get extension properties"
                )


                // Get Queue family properties
                vkGetPhysicalDeviceQueueFamilyProperties(vkPhysicalDevice, intBuffer, null)
                vkQueueFamilyProps = VkQueueFamilyProperties.calloc(intBuffer[0])
                vkGetPhysicalDeviceQueueFamilyProperties(vkPhysicalDevice, intBuffer, vkQueueFamilyProps)

                vkPhysicalDeviceFeatures = VkPhysicalDeviceFeatures.calloc()
                vkGetPhysicalDeviceFeatures(vkPhysicalDevice, vkPhysicalDeviceFeatures)


                // Get Memory information and properties
                vkMemoryProperties = VkPhysicalDeviceMemoryProperties.calloc()
                vkGetPhysicalDeviceMemoryProperties(vkPhysicalDevice, vkMemoryProperties)
            }
        } catch (e: Exception) {
            Logger.error("An error has occurred while initialising the physical device: ${e.message}", e)
            throw e
        }
    }

    fun cleanup() {
        if (Logger.isDebugEnabled()) {
            Logger.debug("Destroying physical device [{}]", vkPhysicalDeviceProperties!!.deviceNameString())
        }
        vkMemoryProperties!!.free()
        vkPhysicalDeviceFeatures!!.free()
        vkQueueFamilyProps!!.free()
        vkDeviceExtensions!!.free()
        vkPhysicalDeviceProperties!!.free()
    }

    fun getDeviceName(): String {
        return vkPhysicalDeviceProperties!!.deviceNameString()
    }

    fun getVkMemoryProperties(): VkPhysicalDeviceMemoryProperties? {
        return vkMemoryProperties
    }

    fun getVkPhysicalDevice(): VkPhysicalDevice {
        return vkPhysicalDevice!!
    }

    fun getVkPhysicalDeviceFeatures(): VkPhysicalDeviceFeatures? {
        return vkPhysicalDeviceFeatures
    }

    fun getVkPhysicalDeviceProperties(): VkPhysicalDeviceProperties? {
        return vkPhysicalDeviceProperties
    }

    fun getVkQueueFamilyProps(): VkQueueFamilyProperties.Buffer? {
        return vkQueueFamilyProps
    }

    private fun hasKHRSwapChainExtension(): Boolean {
        var result = false
        val numExtensions = if (vkDeviceExtensions != null) vkDeviceExtensions!!.capacity() else 0
        for (i in 0 until numExtensions) {
            val extensionName = vkDeviceExtensions!![i].extensionNameString()
            if (KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME == extensionName) {
                result = true
                break
            }
        }
        return result
    }

    private fun hasGraphicsQueueFamily(): Boolean {
        var result = false
        val numQueueFamilies = if (vkQueueFamilyProps != null) vkQueueFamilyProps!!.capacity() else 0
        for (i in 0 until numQueueFamilies) {
            val familyProps = vkQueueFamilyProps!![i]
            if ((familyProps.queueFlags() and VK_QUEUE_GRAPHICS_BIT) != 0) {
                result = true
                break
            }
        }
        return result
    }

    companion object {
        fun createPhysicalDevice(instance: Instance, preferredDeviceName : String): PhysicalDevice? {
            try {
                Logger.debug("Selecting physical devices")
                var selectedDevice : PhysicalDevice? = null

                MemoryStack.stackPush().use { stack ->
                    // Get avaliable devices
                    val pPhysicalDevices : PointerBuffer = getPhysicalDevices(instance, stack)
                    val numDevices = pPhysicalDevices.capacity() ?: 0
                    var selectedPhysicalDevice : PhysicalDevice? = null
                    if (numDevices <= 0) {
                        throw RuntimeException("No physical devices have been found")
                    }

                    val devices : MutableList<PhysicalDevice> = ArrayList()
                    for (i in 0 until numDevices) {
                        val vkPhysicalDevice = VkPhysicalDevice(pPhysicalDevices[i], instance.getVkInstance())
                        val physicalDevice = PhysicalDevice(vkPhysicalDevice)

                        val deviceName: String = physicalDevice.getDeviceName()
                        if (physicalDevice.hasGraphicsQueueFamily() && physicalDevice.hasKHRSwapChainExtension()) {
                            Logger.debug("Device [{}] supports required extensions", deviceName)
                            if (preferredDeviceName != null && preferredDeviceName == deviceName) {
                                selectedPhysicalDevice = physicalDevice
                                break
                            }
                            devices.add(physicalDevice)
                        } else {
                            Logger.debug("Device [{}] does not support required extensions", deviceName)
                            physicalDevice.cleanup()
                        }

                        // No preferred device or it does not meet requirements, just pick the first one
                        selectedPhysicalDevice =
                            if (selectedPhysicalDevice == null && !devices.isEmpty())
                                devices.removeAt(0)
                            else
                                selectedPhysicalDevice

                        // Cleanup non-selected device
                        for (physicalDevice in devices) {
                            physicalDevice.cleanup()
                        }

                        if (selectedPhysicalDevice == null) {
                            throw RuntimeException("No suitable physical devices found")
                        }
                        Logger.debug("Selected device: [{}]", selectedPhysicalDevice.getDeviceName())
                    }
                    return selectedPhysicalDevice

                }
            } catch (e: Exception) {
                Logger.error("An error has occurred: ${e.message}, ${e.stackTrace}", e)
                throw e
            }
        }

        @JvmStatic
        protected fun getPhysicalDevices(instance: Instance, stack: MemoryStack): PointerBuffer {
            val pPhysicalDevices: PointerBuffer
            // Get number of physical devices
            val intBuffer = stack.mallocInt(1)
            vkCheck(
                vkEnumeratePhysicalDevices(instance.getVkInstance(), intBuffer, null),
                "Failed to get number of physical devices"
            )
            val numDevices = intBuffer[0]
            Logger.debug("Detected {} physical device(s)", numDevices)

            // Populate physical devices list pointer
            pPhysicalDevices = stack.mallocPointer(numDevices)
            vkCheck(
                vkEnumeratePhysicalDevices(instance.getVkInstance(), intBuffer, pPhysicalDevices),
                "Failed to get physical devices"
            )
            return pPhysicalDevices
        }
    }
}