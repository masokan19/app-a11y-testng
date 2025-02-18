package com.browserstack;
import com.browserstack.local.Local;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import java.net.URL;
import java.util.Map;
import java.util.HashMap;

import java.util.Iterator;
import java.io.FileReader;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.json.JSONObject;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.json.JSONArray;
import java.io.FileWriter;

import org.openqa.selenium.remote.DesiredCapabilities;
import java.io.FileNotFoundException;
import java.io.IOException;


import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import org.openqa.selenium.WebElement;
import java.io.FileInputStream;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.AfterMethod;


public class BrowserStackTestNGTest {

    public AndroidDriver driver;
    private Local local;

    @BeforeMethod(alwaysRun=true)
    @org.testng.annotations.Parameters(value = { "config", "deviceIndex" })
    public void setUp(String config_file, String deviceIndex) throws Exception {


        // Read and parse the config file
        String configFilePath = "/Users/manyaasokan/Downloads/Databackup/databackup4/app_a11y_test_project 2/android/testng-examples/src/test/resources/com/browserstack/run_first_test/first.conf.json";
        String content = new String(Files.readAllBytes(Paths.get(configFilePath)));

        // Parse the JSON configuration file
        JSONObject config = new JSONObject(content);

        // Extract "environments" JSON array
        JSONArray envs = config.getJSONArray("environments");

        // Example: Creating UiAutomator2Options (if needed)
        DesiredCapabilities options = new DesiredCapabilities();
        System.out.println("Environments: " + envs.toString());

        // Get environment capabilities
        JSONObject envCapabilities = envs.getJSONObject(Integer.parseInt(deviceIndex));
        Iterator<String> it = envCapabilities.keys();
        while (it.hasNext()) {
            String key = it.next();
            options.setCapability(key, envCapabilities.get(key));
        }


        Map<String, Object> commonCapabilities = new HashMap<>();
        Iterator<String> keys = config.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            commonCapabilities.put(key, config.get(key));
        }

        // Iterate over common capabilities and set them as desired capabilities
        it = commonCapabilities.keySet().iterator();
        while (it.hasNext()) {
            String key = it.next();
            Object value = commonCapabilities.get(key);
            if (options.getCapability(key) == null) {
                options.setCapability(key, value);
            } else if (key.equalsIgnoreCase("bstack:options")) {
                // Handle bstack:options, ensuring the value is a Map
                if (value instanceof Map) {
                    HashMap<String, Object> bstackOptionsMap = (HashMap<String, Object>) value;
                    HashMap<String, Object> currentOptions = (HashMap<String, Object>) options.getCapability("bstack:options");
                    bstackOptionsMap.putAll(currentOptions); // Merge the two maps
                    options.setCapability("bstack:options", bstackOptionsMap);
                } else {
                    System.out.println("Expected 'bstack:options' to be a Map, but found: " + value.getClass().getName());
                }
            }
        }

        // Get the bstack:options capability and cast it to JSONObject
        JSONObject browserstackOptions = (JSONObject) options.getCapability("bstack:options");

        // Initialize AndroidDriver with the updated options
        driver = new AndroidDriver(new URL("http://" + config.get("server") + "/wd/hub"), options);
    }

    private static Map<String, Object> loadYamlFile(File file) throws FileNotFoundException {
        Yaml yaml = new Yaml();
        return yaml.load(new FileInputStream(file));
    }

    // Save the modified content back into the YAML file
    private static void saveYamlFile(Map<String, Object> yamlContent, File file) throws IOException {
        Yaml yaml = new Yaml();
        FileWriter writer = new FileWriter(file);
        yaml.dump(yamlContent, writer);  // Write the updated YAML to the file
    }
    @AfterMethod(alwaysRun=true)
    public void tearDown() throws Exception {
        // Invoke driver.quit() to indicate that the test is completed. 
        // Otherwise, it will appear as timed out on BrowserStack.
        driver.quit();
        if(local != null) local.stop();
    }
}
