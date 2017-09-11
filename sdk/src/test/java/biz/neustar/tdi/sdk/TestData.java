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

package biz.neustar.tdi.sdk;

import biz.neustar.tdi.fw.canonicalmessage.TdiCanonicalMessage;
import biz.neustar.tdi.fw.exception.FrameworkRuntimeException;
import biz.neustar.tdi.fw.implementation.TdiImplementation;
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
import biz.neustar.tdi.fw.wrapper.TdiSdkWrapperShape;
import biz.neustar.tdi.sdk.Constants.Api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * Class containing test data used through out the different unit test cases.
 */
public class TestData {
  public static final String PAYLOAD_VALUE = "Hello World!";
  public static TestData instance = null;

  /**
   * Static method to return the instance.
   * 
   * @return TestData instance.
   */
  public static TestData getInstance() {
    if (instance == null) {
      instance = new TestData();
    }

    return instance;
  }

  public static TdiKeyStructureShape goodKey = new TdiKeyStructure("Key1", "Fleet1", "TempData",
      TdiKeyFlagsEnum.CAN_SIGN.getNumber());

  public static TdiKeyStructureShape signKey = new TdiKeyStructure("signKey", "Fleet1", "TempData",
      TdiKeyFlagsEnum.ROLE_F_S.getNumber());

  public static TdiKeyStructureShape cosignKey = new TdiKeyStructure("cosignKey", "Fleet1",
      "TempData", TdiKeyFlagsEnum.ROLE_F_C.getNumber());

  /**
   * Returns the configuration map.
   * 
   * @return Map of String, Object.
   */
  public static Map<String, Object> getTestConfig() {
    Map<String, Object> map = null;
    InputStream inStream = getInstance().getClass().getResourceAsStream("/config.json");
    try {
      map = new ObjectMapper().readValue(inStream, new TypeReference<Map<String, Object>>() {
      });
    } catch (IOException e) {
      // Do Nothing
    }
    return map;
  }

  /**
   * Returns a dummy implementation.
   * 
   * @return {@link TdiImplementationShape} instance
   */
  public static TdiImplementationShape getDummyImplementation() {
    TdiImplementationShape impl = new TdiImplementation(getTestConfig(), DummyPlatform::new);
    return impl;
  }

  /**
   * Returns a dummy implementation such that the getSeffKey() raises an
   * exception.
   * 
   * @return {@link TdiImplementationShape} instance
   */
  public static TdiImplementationShape getDummyImplementationWithKeyError() {
    TdiImplementationShape impl = new TdiImplementation(getTestConfig(), PlatformWithKeyError::new);
    return impl;
  }

  /**
   * Method to return a dummy utils instance.
   * 
   * @return TdiPlatformUtilsShape instance.
   */
  public static TdiPlatformUtilsShape getDummyUtils() {
    return new TdiPlatformUtilsShape() {

      @Override
      public CompletableFuture<Void> init() {
        return null;
      }

      @Override
      public TdiPlatformShape getPlatform() {
        return null;
      }

      @Override
      public void randomFill(byte[] buffer, Integer len) {
      }

      @Override
      public String makeUuid() {
        return null;
      }

      @Override
      public String b64UrlEncode(String toEncode) {
        return Base64.getEncoder().encodeToString(toEncode.getBytes());
      }

      @Override
      public String b64UrlDecode(String b64String) {
        return new String(Base64.getDecoder().decode(b64String));
      }
    };
  }

  /**
   * Creates a dummy datastore.
   * 
   * @return TdiPlatformDataShape instance.
   */
  public static TdiPlatformDataShape getDummyDataStore() {
    return new TdiPlatformDataShape() {
      Map<String, Map<String, Object>> dataStore = new HashMap<>();

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
        Map<String, Object> storeMap = dataStore.get(storeName);
        storeMap.put(key, value);
        return CompletableFuture.completedFuture(null);
      }

      @Override
      public CompletableFuture<List<String>> keys(String storeName) {
        return CompletableFuture.completedFuture(new ArrayList<String>(dataStore.keySet()));
      }

      @Override
      public CompletableFuture<?> get(String storeName, String key) {
        return CompletableFuture.completedFuture(dataStore.get(storeName).get(key));
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
        Map<String, Object> store = dataStore.get(storeName);

        if (store == null) {
          store = new HashMap<>();
          dataStore.put(storeName, store);
        }

        return CompletableFuture.completedFuture(store);
      }
    };
  }

  /**
   * Creates a dummy keystore.
   * 
   * @return TdiPlatformKeysShape instance
   */
  public static TdiPlatformKeysShape getDummyKeyStore(String keyType) {
    switch (keyType) {
      case "sign":
        return new SignKeyStore();
      case "cosign":
        return new CosignKeyStore();
      case "selfKeyError":
        return new ErrorKeyStore();
      case "kidError":
        return new ErrorKidKeyStore();

      default:
        return new DummyKeyStore();
    }
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
      switch (kid) {
        case "signKey":
          return CompletableFuture.completedFuture(signKey);
        case "cosignKey":
          return CompletableFuture.completedFuture(cosignKey);

        default:
          return CompletableFuture.completedFuture(new TdiKeyStructure(kid, "", null, 1));
      }
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

  public static class SignKeyStore extends DummyKeyStore {

    @Override
    public CompletableFuture<TdiKeyStructureShape> getSelfKey() {
      return CompletableFuture.completedFuture(signKey);
    }

    @Override
    public CompletableFuture<TdiKeyStructureShape> getKey(String kid) {
      return CompletableFuture.completedFuture(signKey);
    }
  }

  public static class CosignKeyStore extends DummyKeyStore {

    @Override
    public CompletableFuture<TdiKeyStructureShape> getSelfKey() {
      return CompletableFuture.completedFuture(cosignKey);
    }

    @Override
    public CompletableFuture<TdiKeyStructureShape> getKey(String kid) {
      return CompletableFuture.completedFuture(cosignKey);
    }
  }

  public static class ErrorKeyStore extends DummyKeyStore {

    @Override
    public CompletableFuture<TdiKeyStructureShape> getSelfKey() {
      CompletableFuture<TdiKeyStructureShape> future = new CompletableFuture<>();
      future.completeExceptionally(new FrameworkRuntimeException("error key store"));
      return future;
    }
  }

  public static class ErrorKidKeyStore extends DummyKeyStore {

    @Override
    public CompletableFuture<TdiKeyStructureShape> getKey(String kid) {
      CompletableFuture<TdiKeyStructureShape> keyStruct = new CompletableFuture<>();
      keyStruct.completeExceptionally(new FrameworkRuntimeException("kid not in keystore"));
      return keyStruct;
    }
  }

  /**
   * Method to return a dummy Time instance.
   * 
   * @return TdiPlatformTimeshape instance.
   */
  public static TdiPlatformTimeShape getDummyTime() {
    return new TdiPlatformTimeShape() {

      @Override
      public CompletableFuture<Void> init() {
        return null;
      }

      @Override
      public TdiPlatformShape getPlatform() {
        return null;
      }

      @Override
      public Long timestamp(String timeDateStr) {

        long nonceDate = System.currentTimeMillis() / 1000;
        if (timeDateStr != null && timeDateStr.isEmpty() == false) {
          SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
          formatter.setTimeZone(TimeZone.getTimeZone("UTC"));

          try {
            nonceDate = formatter.parse(timeDateStr).getTime() / 1000;
          } catch (ParseException e) {
            // Do Nothing
          }
        }

        return nonceDate;
      }

      @Override
      public String isoDate(Long timestamp) {
        timestamp = (System.currentTimeMillis() / 1000) + timestamp;

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        String formattedTime = formatter.format(new Date(timestamp * 1000));

        return formattedTime;
      }
    };
  }

  /**
   * Method to return a dummy crypto instance.
   * 
   * @return TdiPlatformCryptoShape instance.
   */
  public static TdiPlatformCryptoShape getDummyCrypto() {
    return new TdiPlatformCryptoShape() {

      @Override
      public CompletableFuture<Void> init() {
        return null;
      }

      @Override
      public TdiPlatformShape getPlatform() {
        return null;
      }

      @Override
      public CompletableFuture<Boolean> verify(TdiKeyStructureShape key, String payload,
          String signature) {
        return CompletableFuture.completedFuture(String.valueOf(payload.hashCode())
            .equals(TestData.getDummyUtils().b64UrlDecode(signature)));
      }

      @Override
      public CompletableFuture<String> sign(TdiKeyStructureShape key, String payload) {
        return CompletableFuture.completedFuture(
            TestData.getDummyUtils().b64UrlEncode(String.valueOf(payload.hashCode())));
      }

      @Override
      public CompletableFuture<Void> encrypt() {
        return null;
      }

      @Override
      public CompletableFuture<Void> decrypt() {
        return null;
      }
    };
  }

  public static class DummyPlatform extends TdiPlatformBase {
    protected TdiPlatformDataShape dataStore;
    protected TdiPlatformKeysShape keyStore;
    protected TdiPlatformTimeShape time;
    protected TdiPlatformUtilsShape util;
    protected TdiPlatformCryptoShape crypto;

    /**
     * Constructor.
     * 
     * @param conf
     *          : Configuration object.
     */
    public DummyPlatform(Map<String, Object> conf) {
      super(conf);
      dataStore = TestData.getDummyDataStore();
      keyStore = TestData.getDummyKeyStore("");
      time = TestData.getDummyTime();
      util = TestData.getDummyUtils();
      crypto = TestData.getDummyCrypto();
    }

    @Override
    public CompletableFuture<Void> init() {
      return CompletableFuture.completedFuture(null);
    }

    @Override
    public TdiPlatformUtilsShape getUtils() {
      return util;
    }

    @Override
    public TdiPlatformTimeShape getTime() {
      return time;
    }

    @Override
    public TdiPlatformKeysShape getKeystore() {
      return keyStore;
    }

    @Override
    public TdiPlatformDataShape getDataStore() {
      return dataStore;
    }

    @Override
    public TdiPlatformCryptoShape getCrypto() {
      return crypto;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> getConfig() {
      return (Map<String, Object>) config.get("platform");
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

  public static class SignPlatform extends DummyPlatform {

    public SignPlatform(Map<String, Object> conf) {
      super(conf);
      keyStore = TestData.getDummyKeyStore("sign");
    }
  }

  public static class CosignPlatform extends DummyPlatform {

    public CosignPlatform(Map<String, Object> conf) {
      super(conf);
      keyStore = TestData.getDummyKeyStore("cosign");
    }
  }

  public static class PlatformWithKeyError extends DummyPlatform {

    public PlatformWithKeyError(Map<String, Object> conf) {
      super(conf);
      keyStore = TestData.getDummyKeyStore("selfKeyError");
    }
  }

  public static class PlatformWithKidError extends DummyPlatform {

    public PlatformWithKidError(Map<String, Object> conf) {
      super(conf);
      keyStore = TestData.getDummyKeyStore("kidError");
    }
  }

  /**
   * Method to return {@link TdiSdkOptions}.
   * 
   * @return {@link TdiSdkOptions}
   */
  public static TdiSdkOptions getDummyPlatformOptions() {
    TdiSdkOptions sdkOptions = new TdiSdkOptions();
    sdkOptions.platform = TestData.DummyPlatform::new;
    sdkOptions.config = TestData.getTestConfig();
    sdkOptions.exposeImpl = true;
    return sdkOptions;
  }

  /**
   * Method to return {@link TdiSdkOptions} for Sign Api.
   * 
   * @return {@link TdiSdkOptions}
   */
  public static TdiSdkOptions getSignPlatformSdkOptions() {
    TdiSdkOptions sdkOptions = new TdiSdkOptions();
    sdkOptions.platform = TestData.SignPlatform::new;
    sdkOptions.config = TestData.getTestConfig();
    sdkOptions.exposeImpl = false;
    return sdkOptions;
  }

  /**
   * Method to return {@link TdiSdkOptions} for Cosign Api.
   * 
   * @return {@link TdiSdkOptions}
   */
  public static TdiSdkOptions getCosignPlatformSdkOptions() {
    TdiSdkOptions sdkOptions = new TdiSdkOptions();
    sdkOptions.platform = TestData.CosignPlatform::new;
    sdkOptions.config = TestData.getTestConfig();
    sdkOptions.exposeImpl = false;
    sdkOptions.plugins = new ArrayList<>();
    return sdkOptions;
  }

  /**
   * Method to return {@link TdiSdkOptions} with error retrieving key with kid.
   * 
   * @return {@link TdiSdkOptions}
   */
  public static TdiSdkOptions getKidErrorPlatformSdkOptions() {
    TdiSdkOptions sdkOptions = new TdiSdkOptions();
    sdkOptions.platform = TestData.PlatformWithKidError::new;
    sdkOptions.config = TestData.getTestConfig();
    sdkOptions.exposeImpl = true;
    return sdkOptions;
  }

  /**
   * Signs the payload.
   * 
   * @return signed {@link TdiCanonicalMessage}
   * 
   * @throws Exception
   *           if an error occurs
   */
  public static TdiCanonicalMessage getSignedMsg() throws Exception {

    CompletableFuture<TdiSdkWrapperShape> signSdkWrapper;
    TdiSdk signSdk = new TdiSdk(getSignPlatformSdkOptions());
    signSdkWrapper = signSdk.init();

    CompletableFuture<TdiCanonicalMessage> signedMessage = signSdkWrapper
        .thenCompose((signWrapper) -> {
          Function<String, CompletableFuture<TdiCanonicalMessage>> signApi = signWrapper
              .api(Api.SignFlow.name());
          CompletableFuture<TdiCanonicalMessage> signResult = signApi
              .apply(TestData.PAYLOAD_VALUE);
          return signResult;
        });
    TdiCanonicalMessage signedMsg = signedMessage.get();

    return signedMsg;

  }

  /**
   * Cosigns the received/input message.
   * 
   * @param signedMsg
   *          :jws string
   * 
   * @return cosigned {@link TdiCanonicalMessage}
   * 
   * @throws Exception
   *           if an error occurs
   */
  public static TdiCanonicalMessage getCosignedMsg(String signedMsg) throws Exception {

    CompletableFuture<TdiSdkWrapperShape> cosignSdkWrapper;
    TdiSdk cosignSdk = new TdiSdk(getCosignPlatformSdkOptions());
    cosignSdkWrapper = cosignSdk.init();

    CompletableFuture<TdiCanonicalMessage> cosignedMessage = cosignSdkWrapper
        .thenCompose((cosignWrapper) -> {
          Function<String, CompletableFuture<TdiCanonicalMessage>> cosignApi = cosignWrapper
              .api(Api.CosignFlow.name());
          CompletableFuture<TdiCanonicalMessage> cosignResult = cosignApi
              .apply(signedMsg);
          return cosignResult;
        });

    TdiCanonicalMessage cosignedMsg = cosignedMessage.get();

    return cosignedMsg;
  }

}
