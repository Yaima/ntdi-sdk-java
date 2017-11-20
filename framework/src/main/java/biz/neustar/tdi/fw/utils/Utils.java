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

package biz.neustar.tdi.fw.utils;

import biz.neustar.tdi.fw.exception.InvalidFormatException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Utility class.
 *
 */
public class Utils {

  static {
    init();
  }

  private static final Logger LOG = LoggerFactory.getLogger(Utils.class);

  /**
   * Method that initializes {@link ObjectMapper} for better performance.
   */
  public static void init() {
    ObjectMapper mapper = new ObjectMapper();
    mapper.getTypeFactory().constructType(new TypeReference<Map<String, Object>>() {
    });
  }

  /**
   * Method to convert Map to JSON String.
   *
   * @param map
   *          : Map object to be converted to JSON
   *
   * @return {@link String} Converted JSON String.
   *
   * @throws InvalidFormatException
   *           Thrown if there is any issues in conversion.
   */
  public static String mapToJson(Map<String, ?> map) throws InvalidFormatException {
    try {
      return new ObjectMapper().writeValueAsString(map);
    } catch (JsonProcessingException e) {
      throw new InvalidFormatException(e);
    }
  }

  /**
   * Method to convert JSON String to Map&lt;String, Object&gt;
   *
   * @param jsonString
   *          JSON string that needs to be converted.
   *
   * @return Map&lt;String, Object&gt; converted Map.
   *
   * @throws InvalidFormatException
   *           when invalid JSON format is encountered
   */
  public static Map<String, Object> jsonToMap(String jsonString) throws InvalidFormatException {
    Map<String, Object> returnMap = null;
    try {
      returnMap = new ObjectMapper().readValue(jsonString,
          new TypeReference<Map<String, Object>>() {
          });
    } catch (IOException e) {
      LOG.debug("Unable to decode JSON: " + jsonString);
      throw new InvalidFormatException(e);
    }
    return returnMap;
  }

  /**
   * Method to convert Object to JSON String.
   *
   * @param object
   *          : Object to be converted
   *
   * @return JSON String.
   *
   * @throws InvalidFormatException
   *           if there is any issues in conversion.
   */
  public static String objectToJson(Object object) throws InvalidFormatException {
    String json = null;

    try {
      json = new ObjectMapper().writeValueAsString(object);
    } catch (JsonProcessingException e) {
      LOG.debug("Unable to encode object to JSON");
      throw new InvalidFormatException(e);
    }

    return json;
  }

  /**
   * Method to convert JSON string to Object provided by Class attribute.
   *
   * @param jsonString
   *          : JSON String to be converted.
   * @param clazz
   *          : Class reference of the object to be converted into.
   * @param <T> : Class type template
   *
   * @return Object initialized with JSON properties.
   *
   * @throws InvalidFormatException
   *           if any issues in conversion to object.
   */
  public static <T> T jsonToObject(String jsonString, Class<T> clazz)
      throws InvalidFormatException {
    if (StringUtils.isEmpty(jsonString) == false) {
      ObjectMapper mapper = new ObjectMapper();
      mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
      try {
        return mapper.readValue(jsonString, clazz);
      } catch (IOException e) {
        LOG.debug("Unable to decode JSON to object: " + jsonString);
        LOG.debug(e.toString());
        throw new InvalidFormatException(e);
      }
    } else {
      LOG.debug("JSON String cannot be empty");
      throw new InvalidFormatException("JSON String cannot be empty");
    }
  }

  /**
   * Method to convert Object to JSON and write to the file specified.
   *
   * @param object
   *          : Object to be converted
   * @param jsonFilePath
   *          : JSON file to be written to.
   *
   * @throws InvalidFormatException
   *           if there is any issues in conversion.
   */
  public static void objectToJsonFile(Object object, File jsonFilePath)
      throws InvalidFormatException {
    if (jsonFilePath != null) {
      try {
        new ObjectMapper().writeValue(jsonFilePath, object);
      } catch (IOException e) {
        LOG.debug("Unable to encode object to JSON");
        throw new InvalidFormatException(e);
      }

    } else {
      LOG.debug("JSON File cannot be null");
      throw new InvalidFormatException("JSON File cannot be null");
    }
  }

  /**
   * Method to convert JSON File to Object provided by Class attribute.
   *
   * @param jsonFilePath
   *          : JSON File to be converted.
   * @param clazz
   *          : Class reference of the object to be converted into.
   * @param <T> : Class type template
   *
   * @return Object initialized with JSON properties.
   *
   * @throws InvalidFormatException
   *           if any issues in conversion to object.
   */
  public static <T> T jsonFileToObject(File jsonFilePath, Class<T> clazz)
      throws InvalidFormatException {
    if (jsonFilePath != null && jsonFilePath.exists()) {
      try {
        return new ObjectMapper().readValue(jsonFilePath, clazz);
      } catch (IOException e) {
        LOG.debug("Unable to decode JSON to object");
        throw new InvalidFormatException(e);
      }
    } else {
      LOG.debug("JSON File cannot be null or empty");
      throw new InvalidFormatException("JSON File cannot be null or empty");
    }
  }

  /**
   * Method to convert Map to JSON and write to the file specified.
   *
   * @param map
   *          : Map object to be converted to JSON
   * @param jsonFilePath
   *          : JSON file to be written to.
   *
   * @throws InvalidFormatException
   *           Thrown if there is any issues in conversion.
   */
  public static void mapToJsonFile(Map<String, ?> map, File jsonFilePath)
      throws InvalidFormatException {
    if (jsonFilePath != null) {
      try {
        new ObjectMapper().writeValue(jsonFilePath, map);
      } catch (IOException e) {
        throw new InvalidFormatException(e);
      }
    } else {
      LOG.debug("JSON File cannot be null");
      throw new InvalidFormatException("JSON File cannot be null");
    }
  }

  /**
   * Method to convert JSON String to Map&lt;String, Object&gt;
   *
   * @param jsonFilePath
   *          JSON file that needs to be converted.
   *
   * @return Map&lt;String, Object&gt; converted Map.
   *
   * @throws InvalidFormatException
   *           when invalid JSON format is encountered
   */
  public static Map<String, Object> jsonFileToMap(File jsonFilePath) throws InvalidFormatException {
    if (jsonFilePath != null && jsonFilePath.exists()) {
      Map<String, Object> returnMap = null;
      try {
        returnMap = new ObjectMapper().readValue(jsonFilePath,
            new TypeReference<Map<String, Object>>() {
            });
      } catch (IOException e) {
        LOG.debug("Unable to decode JSON from file.");
        throw new InvalidFormatException(e);
      }
      return returnMap;
    } else {
      LOG.debug("JSON File cannot be null or empty");
      throw new InvalidFormatException("JSON File cannot be null or empty");
    }
  }
}
