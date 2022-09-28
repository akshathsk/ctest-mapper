package org.uiuc;

import java.io.IOException;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.StringReader;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {

    static final List<String> testCases = Arrays.asList(
            "org.apache.skywalking.oap.server.starter.config.ApplicationConfigLoaderTestCase#testLoadConfig",
            "org.apache.skywalking.oap.server.starter.config.ApplicationConfigLoaderTestCase#testLoadStringTypeConfig",
            "org.apache.skywalking.oap.server.starter.config.ApplicationConfigLoaderTestCase#testLoadIntegerTypeConfig",
            "org.apache.skywalking.oap.server.starter.config.ApplicationConfigLoaderTestCase#testLoadBooleanTypeConfig",
            "org.apache.skywalking.oap.server.starter.config.ApplicationConfigLoaderTestCase#testLoadSpecialStringTypeConfig"
            );
    static Map<String, Map<String, Map<String, Map<String, Object>>>> map = new HashMap<>();

    public static void main(String[] args) throws IOException, InterruptedException {

        int initialIndex = 0;
        processMvnTest(initialIndex);
    }

    private static void processMvnTest(int index) throws IOException, InterruptedException {

        if(index == testCases.size()) {
            System.out.println(map);
            return;
        }

        Process p = null;
        String testCase = testCases.get(index);
        try {
            p = Runtime.getRuntime().exec("mvn test -Dtest=" + testCase + " -DfailIfNoTests=false");
        } catch (IOException e) {
            System.err.println("Error on exec() method");
            e.printStackTrace();
        }
        OutputStream output = new OutputStream() {
            private final StringBuilder string = new StringBuilder();

            @Override
            public void write(int b) {
                this.string.append((char) b);
            }

            public String toString() {
                return this.string.toString();
            }
        };

        copy(p.getInputStream(), output);
        BufferedReader bufReader = new BufferedReader(new StringReader(output.toString()));
        String prev = bufReader.readLine();
        String next = bufReader.readLine();
        while (next != null) {
            if (prev.contains("[CTEST][getModuleConfiguration]") && next.contains("[CTEST][getProviderConfiguration]")) {
                processMapping(testCase, prev, next);
                index = index + 1;
                processMvnTest(index);
            }
            prev = next;
            next = bufReader.readLine();
        }
        p.waitFor();
    }

    static void copy(InputStream in, OutputStream out) throws IOException {
        while (true) {
            int c = in.read();
            if (c == -1)
                break;
            out.write((char) c);
        }
    }

    private static void processMapping(String test, String module, String provider) {

        String moduleExtracted = module.substring(module.indexOf("###") + 3, module.lastIndexOf("###"));
        String providerExtracted = provider.substring(provider.indexOf("###") + 3, provider.lastIndexOf("###"));
        String configStr = provider.substring(provider.indexOf("{") + 1, provider.lastIndexOf("}"));
        String propertiesStr = "";

        if (configStr.contains("properties={")) {
            propertiesStr = configStr.substring(configStr.indexOf("{") + 1, configStr.indexOf("}"));
            configStr = configStr.replace(propertiesStr, "").replace("properties={}", "");
        }

        String[] innerProp = propertiesStr.split(", ");
        Map<String, String> propMap = new HashMap<>();
        for (String s : innerProp) {
            String[] eachProp = s.split("=");
            propMap.put(eachProp[0], eachProp[1]);
        }
        Map<String, Object> configMap = new HashMap<>();
        if (propMap.size() > 0) {
            configMap.put("properties", propMap);
        }
        String[] parts = configStr.split(", ");

        for (String part : parts) {

            String[] eachConfig = part.split("=");
            configMap.put(eachConfig[0], eachConfig[1]);
        }

        Map<String, Map<String, Object>> providerMap = new HashMap<>();
        providerMap.put(providerExtracted, configMap);
        Map<String, Map<String, Map<String, Object>>> moduleMap = new HashMap<>();
        moduleMap.put(moduleExtracted, providerMap);
        map.put(test, moduleMap);
    }
}