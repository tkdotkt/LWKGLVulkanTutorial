package tk.kt.LWKGLVulkanTutorial.engine.input

import org.joml.Vector2f
import org.lwjgl.glfw.GLFW.*
import org.tinylog.Logger


class MouseInput(windowHandle : Long) {
    private lateinit var currentPos: Vector2f
    private lateinit var displVec: Vector2f
    private lateinit var previousPos: Vector2f
    private var inWindow = false
    private var leftButtonPressed = false
    private var rightButtonPressed = false

    init {
        init(windowHandle)
    }

    private fun init(windowHandle: Long) {
        Logger.debug{"Initialising MouseInput"}
        previousPos = Vector2f(-1f, -1f)
        currentPos = Vector2f()
        displVec = Vector2f()
        leftButtonPressed = false
        rightButtonPressed = false
        inWindow = false

        Logger.debug{"Setting cursor position callback"}
        glfwSetCursorPosCallback(windowHandle) { handle: Long, xpos: Double, ypos: Double ->
            currentPos.x = xpos.toFloat()
            currentPos.y = ypos.toFloat()
        }
        Logger.debug{"Setting cursor enter callback"}
        glfwSetCursorEnterCallback(
            windowHandle
        ) { handle: Long, entered: Boolean -> inWindow = entered }
        Logger.debug{"Setting mouse button callback"}
        glfwSetMouseButtonCallback(
            windowHandle
        ) { handle: Long, button: Int, action: Int, mode: Int ->
            leftButtonPressed = button == GLFW_MOUSE_BUTTON_1 && action == GLFW_PRESS
            rightButtonPressed = button == GLFW_MOUSE_BUTTON_2 && action == GLFW_PRESS
        }
    }

    fun getCurrentPos(): Vector2f {
        return currentPos
    }

    fun getDisplVec(): Vector2f {
        return displVec
    }

    fun input() {
        displVec.x = 0f
        displVec.y = 0f
        if (previousPos.x >= 0 && previousPos.y >= 0 && inWindow) {
            displVec.x = currentPos.y - previousPos.y
            displVec.y = currentPos.x - previousPos.x
        }
        previousPos.x = currentPos.x
        previousPos.y = currentPos.y
    }

    fun isLeftButtonPressed(): Boolean {
        return leftButtonPressed
    }

    fun isRightButtonPressed(): Boolean {
        return rightButtonPressed
    }

}