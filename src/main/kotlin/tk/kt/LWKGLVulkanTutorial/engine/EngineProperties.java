package tk.kt.LWKGLVulkanTutorial.engine;

import org.tinylog.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class EngineProperties {
    private static final int DEFAULT_UPS = 30;
    private static final String FILENAME = "eng.properties";
    private static EngineProperties instance;
    private String physDeviceName;
    private int ups;
    private boolean validate;

    private EngineProperties() {
        // Singleton
        Properties props = new Properties();

        try (InputStream stream = EngineProperties.class.getResourceAsStream("/" + FILENAME)) {
            props.load(stream);
            ups = Integer.parseInt(props.getOrDefault("ups", DEFAULT_UPS).toString());
            validate = Boolean.parseBoolean(props.getOrDefault("vkValidate", false).toString());
            physDeviceName = props.getProperty("physDeviceName");
            if (physDeviceName == null) {
                throw new NullPointerException("Cannot read the physical device name, check your eng.properties file");
            }
        } catch (IOException excp) {
            Logger.error("Could not read [{}] properties file", FILENAME, excp);
        } catch (Exception e) {
            Logger.error("An error occurred while trying to access properties from [{}]: \n\t\t\t{}", FILENAME, e.getMessage(), e);
            throw e;
        }
    }

    public static synchronized EngineProperties getInstance() {
        if (instance == null) {
            instance = new EngineProperties();
        }
        return instance;
    }

    public String getPhysDeviceName() {
        return physDeviceName;
    }

    public int getUps() {
        return ups;
    }

    public boolean isValidate() {
        return validate;
    }
}
