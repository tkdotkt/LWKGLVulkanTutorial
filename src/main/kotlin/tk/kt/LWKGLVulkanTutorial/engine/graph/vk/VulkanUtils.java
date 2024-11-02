package tk.kt.LWKGLVulkanTutorial.engine.graph.vk;
import org.tinylog.Logger;

import java.util.Locale;

import static org.lwjgl.vulkan.VK11.VK_SUCCESS;

public class VulkanUtils {

    private VulkanUtils() {
        // Utility class
    }

    public static OSType getOS() {
        OSType result;
        String os = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
        if ((os.indexOf("mac") >= 0) || (os.indexOf("darwin") >= 0)) {
            result = OSType.MACOS;
        } else if (os.indexOf("win") >= 0) {
            result = OSType.WINDOWS;
        } else if (os.indexOf("nux") >= 0) {
            result = OSType.LINUX;
        } else {
            result = OSType.OTHER;
        }

        return result;
    }

    public static void vkCheck(int err, String errMsg) {
        if (err != VK_SUCCESS) {
            Logger.error(errMsg + ": " + err, new RuntimeException());
            throw new RuntimeException(errMsg + ": " + err);
        } else {
            Logger.debug("Returned VK_SUCCESS");
        }
    }

    public enum OSType {WINDOWS, MACOS, LINUX, OTHER}
}
