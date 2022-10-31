package org.uiuc;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.uiuc.AppConstants.*;
import static org.uiuc.UtilHelper.copy;

public class Main {

  static Map<String, Map<String, Map<String, Map<String, Object>>>> map = new LinkedHashMap<>();

  public static void main(String[] args) throws IOException, InterruptedException {

    int initialIndex = 0;
    processMvnTest(initialIndex);
  }

  private static void processMvnTest(int index) throws IOException, InterruptedException {

    if (index == testCases.size()) {
      Gson gson = new Gson();
      String json = gson.toJson(map);
      System.out.println(json);
      return;
    }

    Process p = null;
    String testCase = testCases.get(index);
    try {
      p = Runtime.getRuntime().exec("mvn test -Dtest=" + testCase + " -DfailIfNoTests=false");
    } catch (IOException e) {
      System.err.println(ERROR_MSG);
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
    StringBuilder storeModule = new StringBuilder();
    StringBuilder storeProvider = new StringBuilder();
    StringBuilder storeProperty = new StringBuilder();
    Map<String, Object> configMap = new LinkedHashMap<>();

    while (next != null) {
      if (prev.contains(CTEST_MODULE) && next.contains(CTEST_PROVIDER)) {
        storeModule = new StringBuilder(prev);
        storeProvider = new StringBuilder(next);
      }
      if (next.contains(CTEST_PROPERTY_WRAPPER)) {
        storeProperty = new StringBuilder(next);
        processMapping(testCase, configMap, storeModule.toString(), storeProvider.toString(), next, null, null,
            null);
      }
      if (next.contains(CTEST_SUB_PROPERTY_WRAPPER)) {
        processMapping(testCase, configMap, storeModule.toString(), storeProvider.toString(),
            storeProperty.toString(), next, null, null);
      }
      if (next.contains(CTEST_PROPERTY_RESET_WRAPPER)) {
        processMapping(testCase, configMap, storeModule.toString(), storeProvider.toString(), null, null, next,
            null);
      }
      if (next.contains(CTEST_SUB_PROPERTY_RESET_WRAPPER)) {
        processMapping(testCase, configMap, storeModule.toString(), storeProvider.toString(),
            storeProperty.toString(), next, null, next);
      }
      prev = next;
      next = bufReader.readLine();
    }
    index = index + 1;
    processMvnTest(index);
    p.waitFor();
  }

  private static void processMapping(String test, Map<String, Object> configMap, String module, String provider,
      String propKey, String subPropKey, String resetProp, String resetSubProp) {

    String moduleExtracted = (module == null || module.isEmpty()) ? null
        : module.substring(module.indexOf(SEPARATOR) + 3, module.lastIndexOf(SEPARATOR));

    String providerExtracted = (provider == null || provider.isEmpty()) ? null
        : provider.substring(provider.indexOf(SEPARATOR) + 3, provider.lastIndexOf(SEPARATOR));

    String propKeyExtracted = (propKey == null || propKey.isEmpty()) ? null
        : propKey.substring(propKey.indexOf(SEPARATOR) + 3, propKey.lastIndexOf(SEPARATOR));

    String subPropKeyExtracted = (subPropKey == null || subPropKey.isEmpty()) ? null
        : subPropKey.substring(subPropKey.indexOf(SEPARATOR) + 3, subPropKey.lastIndexOf(SEPARATOR));

    String resetPropKey = (resetProp == null || resetProp.isEmpty()) ? null
        : resetProp.substring(resetProp.indexOf(SEPARATOR) + 3, resetProp.lastIndexOf(SEPARATOR));

    String resetPropValue = (resetProp == null || resetProp.isEmpty()) ? null
        : resetProp.substring(resetProp.indexOf(SEPARATOR_ASTERISK) + 3,
            resetProp.lastIndexOf(SEPARATOR_ASTERISK));

    System.out.println(test + " " + moduleExtracted + " " + providerExtracted + " " + propKeyExtracted + " " + resetPropKey);

    if (resetProp != null && map.containsKey(test) && module != null && map.get(test).containsKey(module)
        && provider != null && map.get(test).get(module).containsKey(provider) && map.get(test).get(module)
        .get(provider).containsKey(resetPropKey)) {
      map.get(test).get(module).get(provider).put(resetPropKey, resetPropValue);
      return;
    }

    String resetSubPropKey = (resetSubProp == null || resetSubProp.isEmpty()) ? null
        : resetSubProp.substring(resetProp.indexOf(SEPARATOR) + 3, resetSubProp.lastIndexOf(SEPARATOR));
    String resetSubPropValue = (resetSubProp == null || resetSubProp.isEmpty()) ? null
        : resetSubProp.substring(resetProp.indexOf(SEPARATOR_ASTERISK) + 3,
            resetSubProp.lastIndexOf(SEPARATOR_ASTERISK));

    if (resetSubPropKey != null && map.containsKey(test) && module != null && map.get(test).containsKey(module)
        && provider != null && map.get(test).get(module).containsKey(provider) && map.get(test).get(module)
        .get(provider).containsKey(resetSubPropKey)) {
      ((Map) map.get(test).get(module).get(provider).get("properties")).put(resetSubPropKey, resetSubPropValue);
      return;
    }

    String configStr = provider.substring(provider.indexOf("{") + 1, provider.lastIndexOf("}"));

    if (configStr.contains("properties={") && propKeyExtracted.equals("properties")
        && subPropKeyExtracted != null) {
      String propertiesStr = configStr.substring(configStr.indexOf("{") + 1, configStr.indexOf("}"));
      configStr = configStr.replace(propertiesStr, "").replace("properties={}", "");
      String[] innerProp = propertiesStr.split(", ");
      Map<String, String> propMap = new LinkedHashMap<>();
      for (String s : innerProp) {
        String[] eachProp = s.split("=");
        if (subPropKeyExtracted.equals(eachProp[0])) {
          propMap.put(eachProp[0], eachProp[1]);
        }
      }
      if (propMap.size() > 0) {
        configMap.put("properties", propMap);
      }
    }

    if (configStr.contains("downsampling=") && propKeyExtracted.equals("downsampling")) {
      String arrayStr = configStr.substring(configStr.indexOf("["), configStr.indexOf("]") + 1);
      configStr = configStr.replace(arrayStr, "").replace("downsampling=,", "");
      configMap.put("downsampling", arrayStr);
    }

    String[] parts = configStr.split(", ");

    for (String part : parts) {
      String[] eachConfig = part.split("=");
      if (eachConfig.length == 1 && propKeyExtracted.equals(eachConfig[0])) {
        configMap.put(eachConfig[0], "");
      } else {
        if (propKeyExtracted.equals(eachConfig[0])) {
          configMap.put(eachConfig[0], eachConfig[1]);
        }
      }
    }

    Map<String, Map<String, Object>> providerMap = new LinkedHashMap<>();
    providerMap.put(providerExtracted, configMap);
    Map<String, Map<String, Map<String, Object>>> moduleMap = new LinkedHashMap<>();
    moduleMap.put(moduleExtracted, providerMap);
    map.put(test, moduleMap);
  }


}