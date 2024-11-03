package tk.kt.LWKGLVulkanTutorial.engine.graph

import org.tinylog.kotlin.Logger
import tk.kt.LWKGLVulkanTutorial.engine.EngineProperties
import tk.kt.LWKGLVulkanTutorial.engine.Window
import tk.kt.LWKGLVulkanTutorial.engine.graph.vk.*
import tk.kt.LWKGLVulkanTutorial.engine.scene.Scene


class Render(window: Window, scene: Scene) {

    lateinit var instance : Instance
    private lateinit var device : Device
    private lateinit var graphQueue : Queue.Companion.GraphicsQueue
    private lateinit var physicalDevice: PhysicalDevice
    private lateinit var surface: Surface

    init {
        init(window, scene)
    }

    fun init(window : Window, scene : Scene) {
        Logger.debug("Initialising renderer")
        val engProps = EngineProperties.getInstance()
        instance = Instance(engProps.isValidate())
        physicalDevice = PhysicalDevice.createPhysicalDevice(instance, engProps.getPhysDeviceName())!!
        device = Device(physicalDevice)
        surface = Surface(physicalDevice, window.getWindowHandle())
        graphQueue = Queue.Companion.GraphicsQueue(device, 0)
        Logger.debug{"Render has been initialised"}
    }

    fun cleanup() {
        Logger.debug("Render cleanup has been called")
        surface.cleanup();
        device.cleanup();
        physicalDevice.cleanup();
        instance.cleanup();
    }

    fun render(window : Window, scene : Scene) {

    }
}