package tk.kt.LWKGLVulkanTutorial.engine

import org.lwjgl.glfw.Callbacks.glfwFreeCallbacks
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWKeyCallbackI
import org.lwjgl.glfw.GLFWVulkan.glfwVulkanSupported
import org.lwjgl.system.MemoryUtil
import org.tinylog.Logger
import tk.kt.LWKGLVulkanTutorial.engine.input.MouseInput


class Window {
    private lateinit var mouseInput: MouseInput
    private var windowHandle: Long = 0
    private var width = 800
    private var height = 600
    private var resized = false


    constructor(title : String) {
        init(title, null)
    }

    constructor(title : String, keyCallback: GLFWKeyCallbackI?) {
        init(title, keyCallback)
    }

    private fun init(title : String, keyCallback: GLFWKeyCallbackI?) {
        Logger.debug{"Initialising the window"}

        Logger.debug{"Initialising GLFW"}
        if (!glfwInit()) {
            throw IllegalStateException("Unable to initialise GLFW.")
        }
        Logger.debug{"Successfully initialised GLFW"}

        Logger.debug{"Checking if vulkan is supported"}
        if (!glfwVulkanSupported()) {
            throw IllegalStateException("Cannot find a compatible Vulkan installable client driver (ICD)");
        }
        Logger.debug{"Success, Vulkan is supported"}

        Logger.debug{"Setting vidMode"}
        val vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor())!!
        if (width == 0 || height == 0) {
            width = vidMode.width()
            height = vidMode.height()
        } else {
            width
            height
        }

        Logger.debug{"Setting glfw window hints"}
        glfwDefaultWindowHints();
        Logger.debug{"GLFW window has default hints"}
        glfwWindowHint(GLFW_CLIENT_API, GLFW_NO_API);
        Logger.debug{"GLFW_CLIENT_API is set to GLFW_NO_API"}
        glfwWindowHint(GLFW_MAXIMIZED, GLFW_FALSE);
        Logger.debug{"GLFW_MAXIMISED is set to GLFW_FALSE"}

        Logger.debug{"Creating window"}
        windowHandle = glfwCreateWindow(width, height, title, MemoryUtil.NULL, MemoryUtil.NULL)
        if (windowHandle == MemoryUtil.NULL) {
            throw RuntimeException("Failed to create the GLFW window")
        }
        Logger.debug{"Successfully created window"}

        Logger.debug{"Setting up framebuffer size callback"}
        glfwSetFramebufferSizeCallback(windowHandle) {window, w, h -> resize(w, h)}

        Logger.debug{"Setting up key callback"}
        glfwSetKeyCallback(
            windowHandle
        ) { window: Long, key: Int, scancode: Int, action: Int, mods: Int ->
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                Logger.debug{"Escape has been pressed, window now closing"}
                glfwSetWindowShouldClose(window, true)
            }
            keyCallback?.invoke(window, key, scancode, action, mods)
        }

        mouseInput = MouseInput(windowHandle)

    }

    fun cleanup() {
        Logger.debug{"Cleaning up window"}
        glfwFreeCallbacks(windowHandle)
        glfwDestroyWindow(windowHandle)
        glfwTerminate()
    }

    fun getHeight(): Int {
        return height
    }

    fun getMouseInput(): MouseInput {
        return mouseInput
    }

    fun getWidth(): Int {
        return width
    }

    fun getWindowHandle(): Long {
        return windowHandle
    }

    fun isKeyPressed(keyCode: Int): Boolean {
        return glfwGetKey(windowHandle, keyCode) == GLFW_PRESS
    }

    fun isResized(): Boolean {
        return resized
    }

    fun pollEvents() {
        glfwPollEvents()
        mouseInput.input()
    }

    fun resetResized() {
        resized = false
    }

    fun resize(width: Int, height: Int) {
        resized = true
        this.width = width
        this.height = height
    }

    fun setResized(resized: Boolean) {
        this.resized = resized
    }

    fun setShouldClose() {
        glfwSetWindowShouldClose(windowHandle, true)
    }

    fun shouldClose(): Boolean {
        return glfwWindowShouldClose(windowHandle)
    }
}