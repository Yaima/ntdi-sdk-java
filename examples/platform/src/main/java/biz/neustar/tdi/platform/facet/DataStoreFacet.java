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
import biz.neustar.tdi.fw.platform.facet.data.TdiPlatformDataShape;
import biz.neustar.tdi.fw.utils.Utils;
import biz.neustar.tdi.platform.Constants;
import biz.neustar.tdi.platform.exception.PlatformRuntimeException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Implementation of DataStoreFacet class.
 * 
 */
public class DataStoreFacet implements TdiPlatformDataShape {

  private static final Logger LOG = LoggerFactory.getLogger(KeystoreFacet.class);

  /**
   * Extension of the store file.
   */
  private static final String STORE_FILE_EXT = ".dat";

  /**
   * Map of key values, for holding multiple stores.
   */
  private Map<String, Object> stores;

  private TdiPlatformShape pf;

  /**
   * Base file path for storing json files.
   */
  String storeBasePath = "";

  /**
   * Constructor for {@link DataStoreFacet} class.
   * 
   * @param pf
   *          : Object of {@link TdiPlatformShape} class.
   */
  public DataStoreFacet(TdiPlatformShape pf) {
    this.pf = pf;

    // Create new map
    stores = new HashMap<>();

    // Get datastore file base path
    Map<String, Object> platformConfigMap = pf.getConfig();
    @SuppressWarnings("unchecked")
    Map<String, Object> basePathConfig = (Map<String, Object>) platformConfigMap
        .get(Constants.PLATFORM_CONFIG_KEY_DATASTORE);
    storeBasePath = basePathConfig != null
        ? (String) basePathConfig.get(Constants.PLATFORM_CONFIG_KEY_BASEPATH) : "";
    if (StringUtils.isEmpty(storeBasePath)) {
      storeBasePath = Constants.PLATFORM_DEFAULT_BASE_PATH;
    }
    LOG.debug("DataStoreFacet: storeBasePath:" + storeBasePath);
  }

  /**
   * Method to get platform.
   * 
   * @see biz.neustar.tdi.fw.platform.facet.TdiPlatformFacetShape#getPlatform()
   * 
   * @return {@link TdiPlatformShape} : calls object
   * 
   */
  @Override
  public TdiPlatformShape getPlatform() {
    return pf;
  }

  /**
   * Method to initialize {@link DataStoreFacet} class.
   * 
   * @see biz.neustar.tdi.fw.platform.facet.TdiPlatformFacetShape#init()
   * 
   * @return CompletableFuture &lt; Void &gt; future.
   */
  @Override
  public CompletableFuture<Void> init() {
    return CompletableFuture.completedFuture(null);
  }

  /**
   * Get a key's value from the given store.
   * 
   * @param storeName
   *          : String store name.
   * @param key
   *          : String key to get from the store.
   * 
   * @return {@link CompletableFuture} with either of the following states: <br>
   *         <b>Completed Successfully</b>: Value of the key with corresponding
   *         datatype. (Typecast required) <br>
   *         <b>Completed Exceptionally</b>: {@link PlatformRuntimeException} if
   *         store doesn't exists or if key is not in the store.
   * 
   * @see biz.neustar.tdi.fw.platform.facet.data
   *      .TdiPlatformDataShape#get(java.lang.String, java.lang.String)
   */
  @Override
  public CompletableFuture<?> get(String storeName, String key) {

    return CompletableFuture.supplyAsync(() -> {
      if (!this.stores.containsKey(storeName)) {
        throw new PlatformRuntimeException("get: Store not found " + storeName + ".");
      } else {
        if (key.equalsIgnoreCase("*")) {
          return this.stores.get(storeName);
        } else {
          @SuppressWarnings("unchecked")
          Map<String, Object> mapValue = (Map<String, Object>) stores.get(storeName);
          if (mapValue.containsKey(key)) {
            return mapValue.get(key);
          } else {
            throw new PlatformRuntimeException(
                "Key " + key + " not found in store " + storeName + ".");
          }
        }
      }
    });
  }

  /**
   * Method to store key-value pair in a store.
   * 
   * @param storeName
   *          : String name of the store
   * @param key
   *          : String key name
   * @param value
   *          : Object to store against the key
   * 
   * @see biz.neustar.tdi.fw.platform.facet.data
   *      .TdiPlatformDataShape#set(java.lang.String, java.lang.String,
   *      java.lang.Object)
   * 
   * @return {@link CompletableFuture} with either of the following states: <br>
   *         <b>Completed Successfully</b>: Void <br>
   *         <b>Completed Exceptionally</b>: {@link PlatformRuntimeException} if
   *         store doesn't exists or if other errors are encountered while
   *         updating store.
   */
  @Override
  public CompletableFuture<Void> set(String storeName, String key, Object value) {
    CompletableFuture<Void> failedFuture = new CompletableFuture<>();

    if (StringUtils.isEmpty(storeName)) {
      failedFuture.completeExceptionally(
          new PlatformRuntimeException("Invalid store name '" + storeName + "'"));
    } else {
      if (key.equalsIgnoreCase("*")) {
        failedFuture.completeExceptionally(
            new PlatformRuntimeException("Can not set key '*' in datastore"));
      } else {
        if (!stores.containsKey(storeName)) {
          Map<String, Object> mapValue = new HashMap<>();
          mapValue.put(key, value);
          return createStore(storeName, mapValue).thenApply((arg) -> {
            return null;
          });
        } else {
          @SuppressWarnings("unchecked")
          Map<String, Object> mapValue = (Map<String, Object>) stores.get(storeName);
          mapValue.put(key, value);

          return storeFileSave(storeName, mapValue).thenApply((arg) -> {
            return null;
          });
        }
      }
    }
    return failedFuture;
  }

  /**
   * List keys of a store.
   * 
   * @param storeName
   *          : String store name to get list of keys for
   * 
   * @see biz.neustar.tdi.fw.platform.facet.data
   *      .TdiPlatformDataShape#keys(java.lang.String)
   * 
   * @return {@link CompletableFuture} with either of the following states: <br>
   *         <b>Completed Successfully</b>: {@link List}&lt;{@link String}&gt;
   *         keys in the store<br>
   *         <b>Completed Exceptionally</b>: {@link PlatformRuntimeException} if
   *         store doesn't exists.
   */
  @Override
  public CompletableFuture<List<String>> keys(String storeName) {
    return CompletableFuture.supplyAsync(() -> {
      if (this.stores.containsKey(storeName)) {
        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) this.stores.get(storeName);
        List<String> list = new ArrayList<String>();
        list.addAll(map.keySet());
        return list;
      } else {
        throw new PlatformRuntimeException("Store with name '" + storeName + "' not found");
      }
    });
  }

  /**
   * Create a store referenced by name, with optional data to be stored there.
   * If only a store name is provided, the driver will attempt to load and
   * populate an existing store. If data is provided, any already-extant store
   * by that name will be clobbered.
   * 
   * @param storeName
   *          : String
   * @param value
   *          : Map &lt; String, Object &gt;
   * 
   * 
   * @see biz.neustar.tdi.fw.platform.facet.data
   *      .TdiPlatformDataShape#createStore(java.lang.String)
   * 
   * @return {@link CompletableFuture} with either of the following states: <br>
   *         <b>Completed Successfully</b>: {@link Map} of the store if store
   *         existed prior to creation. <br>
   *         <b>Completed Exceptionally</b>: {@link PlatformRuntimeException}.
   */
  @Override
  public CompletableFuture<?> createStore(String storeName, Map<String, Object> value) {
    if (StringUtils.isEmpty(storeName)) {
      CompletableFuture<?> future = new CompletableFuture<>();
      future.completeExceptionally(
          new PlatformRuntimeException("Invalid Store name '" + storeName + "'"));
      return future;
    } else {
      if (value == null) {
        stores.put(storeName, new HashMap<String, Object>());
      } else {
        stores.put(storeName, value);
      }

      return storeFileCreate(storeName, value).exceptionally(err -> {
        // As we have failed to store file,
        // we should remove store from local map as well.
        stores.remove(storeName);
        throw new PlatformRuntimeException(err.getMessage());
      });
    }
  }

  /**
   * Method to store value is expected to be String object.
   * 
   * @param storeName
   *          : String name of the store
   * @param value
   *          : Map Object to store
   * 
   * @return {@link CompletableFuture} with either of the following states: <br>
   *         <b>Completed Successfully</b>: {@link Map} of the store if store
   *         existed prior to creation. <br>
   *         <b>Completed Exceptionally</b>: {@link PlatformRuntimeException}.
   */
  @SuppressWarnings("unchecked")
  private CompletableFuture<?> storeFileCreate(String storeName, Object value) {
    return CompletableFuture.supplyAsync(() -> {

      boolean shouldSave = false;
      Map<String, Object> content = new HashMap<String, Object>();

      if (value != null) {
        content = (HashMap<String, Object>) value;
        shouldSave = true;
      } else {
        try {
          String fileNamePath = storeBasePath + storeName + STORE_FILE_EXT;
          LOG.debug("storeFileCreate: fileNamePath:" + fileNamePath);

          content = Utils.jsonFileToMap(new File(fileNamePath));
        } catch (Exception err) {
          shouldSave = true;
        }
      }

      stores.put(storeName, content);

      if (shouldSave) {
        return storeFileSave(storeName, content).thenApply((updatedJson) -> {
          try {
            return Utils.jsonToMap(updatedJson);
          } catch (Exception er) {
            throw new PlatformRuntimeException(er.getMessage());
          }
        });
      }

      return CompletableFuture.completedFuture(content);
    }).thenCompose((arg) -> {
      return arg;
    });
  }

  /**
   * Method to save file with filename and data to store in the file.
   * 
   * @param storeName
   *          : String file name to save.
   * @param data
   *          : Object data to save in the file.
   * 
   * @return {@link CompletableFuture} with either of the following states: <br>
   *         <b>Completed Successfully</b>: {@link String} JSON representation
   *         of the store. <br>
   *         <b>Completed Exceptionally</b>: {@link PlatformRuntimeException} if
   *         store save encounters an error.
   */
  private CompletableFuture<String> storeFileSave(String storeName, Object data) {
    return CompletableFuture.supplyAsync(() -> {
      String storeFilePath = storeBasePath + storeName + STORE_FILE_EXT;
      try {
        Utils.objectToJsonFile(data, new File(storeFilePath));
        return Utils.objectToJson(data);

      } catch (InvalidFormatException err) {
        throw new PlatformRuntimeException(err.getMessage());
      }
    });
  }

  /**
   * Deletes a store referenced by name.
   * 
   * @param storeName
   *          : String store name to drop.
   * 
   * @see biz.neustar.tdi.fw.platform.facet.data
   *      .TdiPlatformDataShape#deleteStore(java.lang.String)
   * 
   * @return {@link CompletableFuture} with either of the following states: <br>
   *         <b>Completed Successfully</b>: Void <br>
   *         <b>Completed Exceptionally</b>: {@link PlatformRuntimeException} if
   *         store doesn't exists.
   */
  @Override
  public CompletableFuture<Void> deleteStore(String storeName) {
    if (this.stores.containsKey(storeName)) {
      this.stores.remove(storeName);
      return storeFileDrop(storeName);
    } else {
      CompletableFuture<Void> future = new CompletableFuture<>();
      future.completeExceptionally(
          new PlatformRuntimeException("deleteStore: Store not found '" + storeName + "'."));
      return future;
    }
  }

  /**
   * Delete file representing data store. This is a helper function that is
   * particular to a file system datastore. {@link PlatformRuntimeException}
   * 
   * @param storeName
   *          : String name to delete.
   * 
   * @return {@link CompletableFuture} with either of the following states: <br>
   *         <b>Completed Successfully</b>: Void <br>
   *         <b>Completed Exceptionally</b>: {@link PlatformRuntimeException} if
   *         store doesn't exists or if store deletion encounters some error.
   */
  private CompletableFuture<Void> storeFileDrop(String storeName) {
    return CompletableFuture.supplyAsync(() -> {
      String path = storeBasePath + storeName + STORE_FILE_EXT;
      LOG.debug("storeFileDrop: path:" + path);
      File file = new File(path);
      if (file.exists() && !file.isDirectory()) {
        if (!file.delete()) {
          throw new PlatformRuntimeException("Failed to delete store file with name '"
              + storeName + "' ");
        }
      } else {
        throw new PlatformRuntimeException("Store file with name '" + storeName + "' not found ");
      }
      return null;
    });
  }

  /**
   * Drop a key from the given store.
   * 
   * @param storeName
   *          : String store name
   * @param key
   *          : String key to drop from the store
   * 
   * @see biz.neustar.tdi.fw.platform.facet.data
   *      .TdiPlatformDataShape#drop(java.lang.String, java.lang.String)
   * 
   * @return {@link CompletableFuture} with either of the following states: <br>
   *         <b>Completed Successfully</b>: Void <br>
   *         <b>Completed Exceptionally</b>: {@link PlatformRuntimeException} if
   *         store doesn't exists or if key doesn't exist in the store.
   */
  @Override
  public CompletableFuture<Void> drop(String storeName, String key) {
    CompletableFuture<Void> failedFuture = new CompletableFuture<>();
    if (stores.containsKey(storeName)) {
      @SuppressWarnings("unchecked")
      Map<String, Object> map = (Map<String, Object>) stores.get(storeName);
      if (map.containsKey(key)) {
        map.remove(key);
        return storeFileSave(storeName, map).thenApply((updatdJson) -> {
          return null;
        });
      } else {
        failedFuture.completeExceptionally(
            new PlatformRuntimeException("Key with name '"
                + key + "' not found in store with name '"
                + storeName + "' "));
      }
    } else {
      failedFuture.completeExceptionally(
          new PlatformRuntimeException("Store with name '" + storeName + "' not found"));
    }
    return failedFuture;
  }
}
