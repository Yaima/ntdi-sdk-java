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

package biz.neustar.tdi.fw;

import biz.neustar.tdi.fw.component.TdiComponent;
import biz.neustar.tdi.fw.implementation.TdiImplementationShape;
import biz.neustar.tdi.fw.keystructure.TdiKeyFlagsEnum;
import biz.neustar.tdi.fw.keystructure.TdiKeyStructure;
import biz.neustar.tdi.fw.keystructure.TdiKeyStructureShape;
import biz.neustar.tdi.fw.platform.TdiPlatformBase;
import biz.neustar.tdi.fw.platform.TdiPlatformShape;
import biz.neustar.tdi.fw.platform.TdiPlatformShapeFactory;
import biz.neustar.tdi.fw.platform.facet.crypto.TdiPlatformCryptoShape;
import biz.neustar.tdi.fw.platform.facet.data.TdiPlatformDataShape;
import biz.neustar.tdi.fw.platform.facet.keys.TdiPlatformKeysShape;
import biz.neustar.tdi.fw.platform.facet.time.TdiPlatformTimeShape;
import biz.neustar.tdi.fw.platform.facet.utils.TdiPlatformUtilsShape;
import biz.neustar.tdi.fw.plugin.TdiPluginBase;
import biz.neustar.tdi.fw.wrapper.TdiSdkWrapperShape;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class TestData {

  public static TdiKeyStructureShape goodKey = new TdiKeyStructure("Key1", "Fleet1", "TempData",
      TdiKeyFlagsEnum.CAN_SIGN.getNumber());

  public static class DummyPlatformBase extends TdiPlatformBase {

    /**
     * Constructor.
     * 
     * @param conf
     *          : config map
     */
    public DummyPlatformBase(Map<String, Object> conf) {
      super(conf);
      config.put("key1", "value1");
      config.put("key2", "value2");
      config.put("key3", "value3");

    }

    @Override
    public CompletableFuture<Void> init() {
      return null;
    }

    @Override
    public TdiPlatformUtilsShape getUtils() {
      return null;
    }

    @Override
    public TdiPlatformTimeShape getTime() {
      return null;
    }

    @Override
    public TdiPlatformKeysShape getKeystore() {
      return null;
    }

    @Override
    public TdiPlatformDataShape getDataStore() {
      return null;
    }

    @Override
    public TdiPlatformCryptoShape getCrypto() {
      return null;
    }

    @Override
    public TdiPlatformShapeFactory newInstance(Map<String, Object> conf) {
      return null;
    }

  }

  /**
   * Creates a dummy datastore.
   * 
   * @return TdiPlatformDataShape instance.
   */
  public static TdiPlatformDataShape getDummyDataStore() {
    return new TdiPlatformDataShape() {
      @Override
      public CompletableFuture<Void> init() {
        return null;
      }

      @Override
      public TdiPlatformShape getPlatform() {
        return null;
      }

      @Override
      public CompletableFuture<Void> set(String storeName, String key, Object value) {
        return CompletableFuture.completedFuture(null);
      }

      @Override
      public CompletableFuture<List<String>> keys(String storeName) {
        return CompletableFuture.completedFuture(Arrays.asList("key1", "key2"));
      }

      @Override
      public CompletableFuture<?> get(String storeName, String key) {
        if (key.equals("key1")) {

          return CompletableFuture.supplyAsync(() -> {
            return "fromDataStore";
          });
        } else {
          return CompletableFuture.completedFuture(null);
        }
      }

      @Override
      public CompletableFuture<Void> drop(String storeName, String key) {
        return CompletableFuture.completedFuture(null);
      }

      @Override
      public CompletableFuture<Void> deleteStore(String storeName) {
        return null;
      }

      @Override
      public CompletableFuture<?> createStore(String storeName, Map<String, Object> value) {
        return CompletableFuture.completedFuture("created");
      }
    };
  }
  
  public static class DummyKeyStore implements TdiPlatformKeysShape {
    
    @Override
    public CompletableFuture<Void> init() {
      return null;
    }

    @Override
    public TdiPlatformShape getPlatform() {
      return null;
    }

    @Override
    public CompletableFuture<TdiKeyStructureShape> setKeyFromProvision(Object key) {
      return null;
    }

    @Override
    public CompletableFuture<TdiKeyStructureShape> setKey(Object key, Integer flags,
        String fleetId) {
      return null;
    }

    @Override
    public CompletableFuture<Void> saveStore() {
      return null;
    }

    @Override
    public CompletableFuture<TdiKeyStructureShape> getSelfKey() {
      return CompletableFuture.completedFuture(goodKey);
    }

    @Override
    public Object getPublicPem(Object key) {
      return null;
    }

    @Override
    public CompletableFuture<List<TdiKeyStructureShape>> getKeys() {
      return null;
    }

    @Override
    public CompletableFuture<TdiKeyStructureShape> getKeyByRole(Integer role, String fleetId) {
      return null;
    }

    @Override
    public CompletableFuture<TdiKeyStructureShape> getKey(String kid) {
      return null;
    }

    @Override
    public CompletableFuture<TdiKeyStructureShape> generateKey(Integer flags, String kid,
        String fleetId) {
      return null;
    }

    @Override
    public CompletableFuture<Void> forgetKey(String kid) {
      return null;
    }
  }

  /**
   * Creates a dummy keystore.
   * 
   * @return TdiPlatformKeysShape instance
   */
  public static TdiPlatformKeysShape getDummyKeyStore() {
    return new DummyKeyStore();
  }
  
  
  public static class DummyKeyStore1 extends DummyKeyStore {
    
    @Override
    public CompletableFuture<TdiKeyStructureShape> getSelfKey() {
      return CompletableFuture.completedFuture(null);
    }
  } 
  
  public static TdiPlatformKeysShape getDummyKeyStore1() {
    return new DummyKeyStore1();
  }

  public static class DummyPlatform implements TdiPlatformShape {

    /**
     * Configuration for datastore defaults.
     */
    Map<String, Object> config;

    public DummyPlatform(Map<String, Object> conf) {
      config = conf;
    }

    @Override
    public CompletableFuture<Void> init() {
      return CompletableFuture.completedFuture(null);
    }

    @Override
    public TdiPlatformUtilsShape getUtils() {
      return null;
    }

    @Override
    public TdiPlatformTimeShape getTime() {
      return null;
    }

    @Override
    public TdiPlatformKeysShape getKeystore() {
      return TestData.getDummyKeyStore();
    }

    @Override
    public TdiPlatformDataShape getDataStore() {
      return TestData.getDummyDataStore();
    }

    @Override
    public TdiPlatformCryptoShape getCrypto() {
      return null;
    }

    @Override
    public Map<String, Object> getConfig() {
      return null;
    }

    @Override
    public boolean checkConfig(Set<String> keys) {
      return false;
    }

    @Override
    public TdiPlatformShapeFactory newInstance(Map<String, Object> conf) {
      return null;
    }

  }
  
  public static class DummyPlatform1 extends DummyPlatform {
    
    public DummyPlatform1(Map<String, Object> conf) {
      super(conf);
    }
    
    @Override
    public TdiPlatformKeysShape getKeystore() {
      return TestData.getDummyKeyStore1();
    }
  }

  public static class ComponentImpl extends TdiComponent {
    public ComponentImpl(String componentName, TdiImplementationShape impl) {
      super(componentName, impl);
    }

    @Override
    public CompletableFuture<Void> init() {
      return CompletableFuture.supplyAsync(() -> {
        return null;
      });
    }
  }

  public static class PluginImpl extends TdiPluginBase {

    public PluginImpl(TdiImplementationShape impl, TdiSdkWrapperShape sdkWrapper) {
      super(impl, sdkWrapper);
    }

    @Override
    public CompletableFuture<Boolean> init() {
      return CompletableFuture.supplyAsync(() -> {
        return true;
      });
    }

    @Override
    public String getName() {
      return "PluginImpl";
    }
  }
}
