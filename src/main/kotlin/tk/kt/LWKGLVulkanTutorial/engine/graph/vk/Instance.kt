package tk.kt.LWKGLVulkanTutorial.engine.graph.vk

import org.lwjgl.PointerBuffer
import org.lwjgl.glfw.GLFWVulkan
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import org.lwjgl.vulkan.*
import org.lwjgl.vulkan.EXTDebugUtils.*
import org.lwjgl.vulkan.KHRPortabilityEnumeration.VK_INSTANCE_CREATE_ENUMERATE_PORTABILITY_BIT_KHR
import org.lwjgl.vulkan.VK10.VK_FALSE
import org.lwjgl.vulkan.VK11.*
import org.lwjgl.vulkan.VK13.VK_API_VERSION_1_3
import org.tinylog.kotlin.Logger
import tk.kt.LWKGLVulkanTutorial.engine.graph.vk.VulkanUtils.vkCheck
import java.nio.ByteBuffer
import java.nio.LongBuffer


class Instance(validate: Boolean) {

    val MESSAGE_SEVERITY_BITMASK: Int = VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT or
            VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT
    val MESSAGE_TYPE_BITMASK: Int = VK_DEBUG_UTILS_MESSAGE_TYPE_GENERAL_BIT_EXT or
            VK_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT or
            VK_DEBUG_UTILS_MESSAGE_TYPE_PERFORMANCE_BIT_EXT

    private val PORTABILITY_EXTENSION: String = "VK_KHR_portability_enumeration"

    private var vkInstance: VkInstance? = null

    private var debugUtils: VkDebugUtilsMessengerCreateInfoEXT? = null
    private var vkDebugHandle: Long = 0

    init {
        init(validate)
    }

    private fun init(validate: Boolean) {
        Logger.debug("Creating Vulkan Instance")
        MemoryStack.stackPush().use { stack ->
            Logger.debug("Allocating application info")
            val appShortName: ByteBuffer = stack.UTF8("VulkanBook")
            val appInfo = VkApplicationInfo.calloc(stack)
                .sType(VK_STRUCTURE_TYPE_APPLICATION_INFO)
                .pApplicationName(appShortName)
                .applicationVersion(1)
                .pEngineName(appShortName)
                .engineVersion(0)
                .apiVersion(VK_API_VERSION_1_3)
            Logger.debug("Vulkan instance created with appShortName = \"VulkanBook\"")

            Logger.debug("Getting supported validation layers")
            val validationLayers: List<String> = getSupportedValidationLayers()
            val numValidationLayers = validationLayers.size
            var supportsValidation = false
            if (validate && numValidationLayers == 0) {
                supportsValidation = true
                Logger.warn("Request validation but no supported validation layers found. Falling back to no validation")
            }
            Logger.debug("Validation: $supportsValidation")

            Logger.debug("Setting required layers")
            var requiredLayers: PointerBuffer? = null
            if (supportsValidation) {
                requiredLayers = stack.mallocPointer(numValidationLayers)
                for (i in 0 until numValidationLayers) {
                    Logger.debug("Using validation layer [{}]", validationLayers[1])
                    requiredLayers.put(i, stack.ASCII(validationLayers[i]))
                }
            }

            Logger.debug("Getting instance extensions")
            val instanceExtensions: Set<String> = getInstanceExtensions()

            Logger.debug("Getting GLFW extensions")
            val glfwExtensions: PointerBuffer? = GLFWVulkan.glfwGetRequiredInstanceExtensions()
            if (glfwExtensions == null) {
                Logger.error("Failed to find the GLFW platform surface extensions", RuntimeException())
                throw RuntimeException("Failed to find the GLFW platform surface extensions")
            }

            Logger.debug("Setting required extensions")
            var requiredExtensions: PointerBuffer
            val usePortability = instanceExtensions.contains(PORTABILITY_EXTENSION) &&
                    VulkanUtils.getOS() == VulkanUtils.OSType.MACOS
            if (supportsValidation) {
                val vkDebugUtilsExtension = stack.UTF8(VK_EXT_DEBUG_UTILS_EXTENSION_NAME)
                val numExtensions =
                    if (usePortability) glfwExtensions.remaining() + 2 else glfwExtensions.remaining() + 1
                requiredExtensions = stack.mallocPointer(numExtensions)
                requiredExtensions.put(glfwExtensions).put(vkDebugUtilsExtension)
                if (usePortability) {
                    requiredExtensions.put(stack.UTF8(PORTABILITY_EXTENSION))
                }
            } else {
                val numExtensions = if (usePortability) glfwExtensions.remaining() + 1 else glfwExtensions.remaining()
                requiredExtensions = stack.mallocPointer(numExtensions)
                requiredExtensions.put(glfwExtensions)
                if (usePortability) {
                    requiredExtensions.put(stack.UTF8(KHRPortabilitySubset.VK_KHR_PORTABILITY_SUBSET_EXTENSION_NAME))
                }
            }
            requiredExtensions.flip()

            Logger.debug("Creating debug callback if validation is supported")
            var extension = MemoryUtil.NULL
            if (supportsValidation) {
                debugUtils = createDebugCallBack()
                extension = debugUtils!!.address()
            }

            Logger.debug("Allocating instance create info")
            val instanceInfo = VkInstanceCreateInfo.calloc(stack)
                .sType(VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO)
                .pNext(extension)
                .pApplicationInfo(appInfo)
                .ppEnabledLayerNames(requiredLayers)
                .ppEnabledExtensionNames(requiredExtensions)
            if (usePortability) {
                instanceInfo.flags(VK_INSTANCE_CREATE_ENUMERATE_PORTABILITY_BIT_KHR)
            }

            Logger.debug("Creating Vulkan instance")
            val pInstance: PointerBuffer = stack.mallocPointer(1)
            vkCheck(vkCreateInstance(instanceInfo, null, pInstance), "Error creating instance")
            vkInstance = VkInstance(pInstance[0], instanceInfo)

            Logger.debug("Creating debug utils messenger if validation is supported")
            vkDebugHandle = VK_NULL_HANDLE
            if (supportsValidation) {
                val longBuff: LongBuffer = stack.mallocLong(1)
                vkCheck(vkCreateDebugUtilsMessengerEXT(vkInstance, debugUtils, null, longBuff), "Error creating debug utilities")
            }
        }
    }

    fun cleanup() {
        Logger.debug("Destroying Vulkan instance")
        if (vkDebugHandle != VK_NULL_HANDLE) {
            vkDestroyDebugUtilsMessengerEXT(vkInstance, vkDebugHandle, null)
        }

        if (debugUtils != null) {
            debugUtils!!.pfnUserCallback().free()
            debugUtils!!.free()
        }
        vkDestroyInstance(vkInstance, null)
    }

    fun getVkInstance(): VkInstance? {
        return vkInstance
    }

    private fun createDebugCallBack(): VkDebugUtilsMessengerCreateInfoEXT {
        val result = VkDebugUtilsMessengerCreateInfoEXT
            .calloc()
            .sType(VK_STRUCTURE_TYPE_DEBUG_UTILS_MESSENGER_CREATE_INFO_EXT)
            .messageSeverity(MESSAGE_SEVERITY_BITMASK)
            .messageType(MESSAGE_TYPE_BITMASK)
            .pfnUserCallback { messageSeverity: Int, messageTypes: Int, pCallbackData: Long, pUserData: Long ->
                val callbackData =
                    VkDebugUtilsMessengerCallbackDataEXT.create(pCallbackData)
                if ((messageSeverity and VK_DEBUG_UTILS_MESSAGE_SEVERITY_INFO_BIT_EXT) != 0) {
                    Logger.info("VkDebugUtilsCallback, {}", callbackData.pMessageString())
                } else if ((messageSeverity and VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT) != 0) {
                    Logger.warn("VkDebugUtilsCallback, {}", callbackData.pMessageString())
                } else if ((messageSeverity and VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT) != 0) {
                    Logger.error("VkDebugUtilsCallback, {}", callbackData.pMessageString())
                } else {
                    Logger.debug("VkDebugUtilsCallback, {}", callbackData.pMessageString())
                }
                VK_FALSE
            }
        return result
    }

    private fun getInstanceExtensions() : Set<String> {
        val instanceExtensions: MutableSet<String> = HashSet()
        MemoryStack.stackPush().use { stack ->
            val numExtensionsBuf = stack.callocInt(1)
            vkEnumerateInstanceExtensionProperties(null as String?, numExtensionsBuf, null)
            val numExtensions = numExtensionsBuf[0]
            Logger.debug("Instance supports [{}] extensions", numExtensions)

            val instanceExtensionsProps =
                VkExtensionProperties.calloc(numExtensions, stack)
            vkEnumerateInstanceExtensionProperties(null as String?, numExtensionsBuf, instanceExtensionsProps)
            for (i in 0 until numExtensions) {
                val props = instanceExtensionsProps[i]
                val extensionName = props.extensionNameString()
                instanceExtensions.add(extensionName)
                Logger.debug("Supported instance extension [{}]", extensionName)
            }
        }
        return instanceExtensions
    }

    private fun getSupportedValidationLayers() : List<String> {
        MemoryStack.stackPush().use { stack ->
            val numLayersArr = stack.callocInt(1)
            vkEnumerateInstanceLayerProperties(numLayersArr, null)
            val numLayers = numLayersArr.get(0)
            Logger.debug("Instance supports [$numLayers] layers")

            val propsBuf : VkLayerProperties.Buffer = VkLayerProperties.calloc(numLayers, stack)
            vkEnumerateInstanceLayerProperties(numLayersArr, propsBuf)
            val supportedLayers : MutableList<String> = ArrayList()
            for (i in 0 until numLayers) {
                val props = propsBuf[i]
                val layerName = props.layerNameString()
                supportedLayers.add(layerName)
                Logger.debug("Supported layer [{}]", layerName)
            }

            val layersToUse: MutableList<String> = ArrayList()
            // Main validation layer
            Logger.debug("Checking if VK_LAYER_KHRONOS_validation exists in supported layers")
            if (supportedLayers.contains("VK_LAYER_KHRONOS_validation")) {
                Logger.debug("VK_LAYER_KHRONOS_validation does exist")
                layersToUse.add("VK_LAYER_KHRONOS_validation")
                return layersToUse
            }

            // Fallback 1
            Logger.debug("Unable to locate validation layer [VK_LAYER_KHRONOS_validation], falling back")
            Logger.debug("Checking if VK_LAYER_LUNARG_standard_validation exists")
            if (supportedLayers.contains("VK_LAYER_LUNARG_standard_validation")) {
                Logger.debug("VK_LAYER_LUNARG_standard_validation does exist")
                layersToUse.add("VK_LAYER_LUNARG_standard_validation")
                return layersToUse
            }
            Logger.debug("Unable to locate validation layer [VK_LAYER_LUNARG_standard_validation], falling back")

            // Fallback 2 (set)
            val requestedLayers : MutableList<String> = ArrayList()
            Logger.debug("Adding requested layers")
            requestedLayers.add("VK_LAYER_GOOGLE_threading");
            requestedLayers.add("VK_LAYER_LUNARG_parameter_validation");
            requestedLayers.add("VK_LAYER_LUNARG_object_tracker");
            requestedLayers.add("VK_LAYER_LUNARG_core_validation");
            requestedLayers.add("VK_LAYER_GOOGLE_unique_objects");

            val overlap : MutableList<String> = requestedLayers.stream().filter(supportedLayers::contains).toList()
            return overlap
        }
    }
}