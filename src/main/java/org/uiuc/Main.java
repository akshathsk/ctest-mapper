package org.uiuc;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {

        processMvnTest();
    }

    private static void processMvnTest() throws IOException, InterruptedException {
        Process p = null;

        try {
            p = Runtime.getRuntime().exec("mvn test -Dtest=org.apache.skywalking.oap.server.starter.config.ApplicationConfigLoaderTestCase#testLoadConfig -DfailIfNoTests=false");
        } catch (IOException e) {
            System.err.println("Error on exec() method");
            e.printStackTrace();
        }
        OutputStream output = new OutputStream() {
            private StringBuilder string = new StringBuilder();

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
                System.out.println(prev);
                System.out.println(next);
                processMapping(prev, next);
                break;
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

    private static void processMapping(String module, String provider) {
        Map<String, Map<String, Map<String, Map<String, Object>>>> map = new HashMap<>();
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
        for (int i = 0; i < innerProp.length; i++) {
            String[] eachProp = innerProp[i].split("=");
            propMap.put(eachProp[0], eachProp[1]);
        }
        Map<String, Object> configMap = new HashMap<>();
        if (propMap.size() > 0) {
            configMap.put("properties", propMap);
        }
        String[] parts = configStr.split(", ");

        for (int i = 0; i < parts.length; i++) {

            String[] eachConfig = parts[i].split("=");
            configMap.put(eachConfig[0], eachConfig[1]);
        }

        Map<String, Map<String, Object>> providerMap = new HashMap<>();
        providerMap.put(providerExtracted, configMap);
        Map<String, Map<String, Map<String, Object>>> moduleMap = new HashMap<>();
        moduleMap.put(moduleExtracted, providerMap);
        map.put("org.apache.skywalking.oap.server.starter.config.ApplicationConfigLoaderTestCase#testLoadConfig", moduleMap);
        System.out.println(map);
    }
}