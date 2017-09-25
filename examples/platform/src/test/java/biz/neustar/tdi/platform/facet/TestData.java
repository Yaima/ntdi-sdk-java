/*
 * Copyright 2017 Neustar, Inc
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package biz.neustar.tdi.platform.facet;

import biz.neustar.tdi.fw.exception.InvalidFormatException;
import biz.neustar.tdi.fw.platform.TdiPlatformShape;
import biz.neustar.tdi.fw.utils.Utils;
import biz.neustar.tdi.platform.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Map;

public class TestData {

  public static final String PLATFORM_CONFIG_FILE_NAME = "config.json";
  public static final String PLATFORM_CONFIG_BASE_PATH = "src/test/resources/";
  private static final Logger LOG = LoggerFactory.getLogger(TestData.class);

  /**
   * Static Method to get the dummy IPlatfrom. Used for Unit testing.
   * 
   * @return TdiPlatformShape call object
   * @throws InvalidFormatException
   *           - Exception for Mapper
   */
  public static TdiPlatformShape getDummyLibraryPlatform() {

    Map<String, Object> config = null;
    try {
      // Get platform config file
      String configFilePath = PLATFORM_CONFIG_BASE_PATH
          + PLATFORM_CONFIG_FILE_NAME;
      config = Utils.jsonFileToMap(new File(configFilePath));
    } catch (Exception err) {
      LOG.error("getDummyLibraryPlatform: Exception: " + err.getMessage());
    }
    return new Platform(config);
  }

}
