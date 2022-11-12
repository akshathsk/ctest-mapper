package org.uiuc;

import java.util.Arrays;
import java.util.List;

public interface AppConstants {

  List<String> testCases = Arrays.asList(
//          "org.apache.skywalking.oap.server.starter.config.ApplicationConfigLoaderTestCase#testLoadConfig",
//          "org.apache.skywalking.oap.server.starter.config.ApplicationConfigLoaderTestCase#testLoadStringTypeConfig",
//          "org.apache.skywalking.oap.server.starter.config.ApplicationConfigLoaderTestCase#testLoadIntegerTypeConfig",
//          "org.apache.skywalking.oap.server.starter.config.ApplicationConfigLoaderTestCase#testLoadBooleanTypeConfig",
//          "org.apache.skywalking.oap.server.starter.config.ApplicationConfigLoaderTestCase#testLoadSpecialStringTypeConfig",
      "org.apache.skywalking.oap.server.configuration.apollo.ITApolloConfigurationTest#shouldReadUpdated"
  );

  String ERROR_MSG = "Error on exec() method";

  String CTEST_MODULE = "[CTEST][getModuleConfiguration]";
  String CTEST_PROVIDER = "[CTEST][getProviderConfiguration]";

  String CTEST_PROPERTY_WRAPPER = "[CTEST][PropertiesWrapper]";

  String CTEST_PROPERTY_RESET_WRAPPER = "[CTEST][PropertiesWrapper-reset]";

  String CTEST_SUB_PROPERTY_WRAPPER = "[CTEST][SubPropertiesWrapper]";

  String CTEST_SUB_PROPERTY_RESET_WRAPPER = "[CTEST][SubPropertiesWrapper-reset]";
  String SEPARATOR = "###";
  String SEPARATOR_ASTERISK = "***";
}
