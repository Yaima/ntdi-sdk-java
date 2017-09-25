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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import biz.neustar.tdi.fw.exception.InvalidFormatException;
import biz.neustar.tdi.fw.keystructure.TdiKeyStructureShape;
import biz.neustar.tdi.fw.platform.TdiPlatformShape;
import biz.neustar.tdi.platform.Constants;
import biz.neustar.tdi.platform.Utils;
import biz.neustar.tdi.platform.facet.keystore.Key;
import biz.neustar.tdi.platform.facet.keystore.KeyRef;
import biz.neustar.tdi.platform.facet.keystore.Keys;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.security.interfaces.ECPublicKey;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * JUnit test class for KeystoreFacet.
 * 
 */
public class KeystoreFacetTest {

  public static KeystoreFacet objKeystoreFacet;
  public static TdiPlatformShape pf;
  public static Keys arrayKeys;

  /**
   * TestKeystoreFacet JUnit Setup method.
   * 
   * @throws Exception
   * 
   */
  @BeforeClass
  public static void setup() throws Exception {

    pf = TestData.getDummyLibraryPlatform();
    objKeystoreFacet = new KeystoreFacet(pf);

    // Call init, which will read the key file and store in map
    CompletableFuture<Void> future = objKeystoreFacet.init();
    future.get(); // Block till everything is initialized.

    Map<String, Object> configMap = pf.getConfig();

    // Get the base path for keystruct json file
    @SuppressWarnings("unchecked")
    Map<String, Object> basePathConfig = (Map<String, Object>) configMap
        .get(Constants.PLATFORM_CONFIG_KEY_KEYSTORE);
    String dataBasePath = (String) basePathConfig.get(Constants.PLATFORM_CONFIG_KEY_BASEPATH);
    if (dataBasePath.isEmpty()) {
      dataBasePath = Constants.PLATFORM_DEFAULT_BASE_PATH;
    }

    // Read JSON from file to Object, used for testing
    arrayKeys = biz.neustar.tdi.fw.utils.Utils
        .jsonFileToObject(new File((dataBasePath + Constants.KEY_CONFIG_FILE)), Keys.class);
    assertNotNull(arrayKeys);
  }

  @Test
  public void testGetPlatform() {
    TdiPlatformShape pf = objKeystoreFacet.getPlatform();
    assertNotNull(pf);
  }

  @Test
  public void testGetKey() throws Exception {
    Integer flags = 0;
    String kid = "";
    String fleetId = "";
    CompletableFuture<TdiKeyStructureShape> generateFuture = objKeystoreFacet.generateKey(flags,
        kid, fleetId);
    assertNotNull(generateFuture);
    TdiKeyStructureShape generatedKeyStruct = generateFuture.get();
    assertNotNull(generatedKeyStruct);
    String newKid = generatedKeyStruct.getKeyId();

    CompletableFuture<TdiKeyStructureShape> getFuture = objKeystoreFacet.getKey(newKid);
    assertNotNull(getFuture);
    TdiKeyStructureShape keyStruct = getFuture.get();
    assertNotNull(keyStruct);
    assertTrue(keyStruct.getKeyId().equals(generatedKeyStruct.getKeyId()));
    assertTrue(keyStruct.getFlags() == generatedKeyStruct.getFlags());
    assertTrue(keyStruct.getFleetId().equals(generatedKeyStruct.getFleetId()));
  }

  @Test(expected = ExecutionException.class)
  public void testGetKeyEmptyKid() throws Exception {
    String kid = "";
    CompletableFuture<TdiKeyStructureShape> future = objKeystoreFacet.getKey(kid);
    TdiKeyStructureShape keyStructure = future.get();
    assertNull(keyStructure);
  }

  @Test(expected = ExecutionException.class)
  public void testGetKeyInvalidKid() throws Exception {
    String kid = "invalidKIdString";
    CompletableFuture<TdiKeyStructureShape> future = objKeystoreFacet.getKey(kid);
    TdiKeyStructureShape keyStructure = future.get();
    assertNull(keyStructure);
  }

  @Test
  public void testGetSelfKey() throws Exception {
    Integer flags = 512; // Must be decimal 512, 0x0200 flag for self
    String fleetId = "9724baf5-0ec5-44ec-9bd7-2795d0814bdf";
    String kid = "9724baf5-0ec5-44ec-9bd7-3895d0815bdf";
    String key = "{\"use\":\"sig\"," + "\"alg\":\"ES256\"," + "\"kty\":\"EC\"," + "\"kid\":\"" + kid
        + "\"," + "\"crv\":\"P-256\"," + "\"x\":\"JLzzbuz2tRnLFlOL-6bTX6giVavAsc6NDFFT0IMCd2g\","
        + "\"y\":\"m00zVAw5BRrIKtHB-SWD4P-sVJTARSq1mHt8kOIWrPc\","
        + "\"d\":\"TxEyAp7JrLA3xyw17Qw-O-L9zM_nurjgqJG5BvLTxPP\"}";

    // Self key should throw exception here.
    try {
      objKeystoreFacet.getSelfKey().get();
      assertFalse(true); // Should not reach here.
    } catch (Exception e) {
      // Do Nothing. this is expected.
    }

    CompletableFuture<TdiKeyStructureShape> setFuture = objKeystoreFacet.setKey(key, flags,
        fleetId);
    assertNotNull(setFuture);
    setFuture.get();

    CompletableFuture<TdiKeyStructureShape> selfFuture = objKeystoreFacet.getSelfKey();
    assertNotNull(selfFuture.get());

    TdiKeyStructureShape keyStruct = selfFuture.get();
    assertNotNull(keyStruct);
    assertTrue(keyStruct.isOurOwn());
    objKeystoreFacet.forgetKey(keyStruct.getKeyId());
  }

  @Test
  public void testGetKeyByRole() throws Exception {
    Integer flags = 2;
    String kid = "";
    String fleetId = "9724baf5-0ec5-44ec-9bd7-2795d0814bdf";

    CompletableFuture<TdiKeyStructureShape> generateFuture = objKeystoreFacet.generateKey(flags,
        kid, fleetId);
    assertNotNull(generateFuture);
    TdiKeyStructureShape keyStruct = generateFuture.get();
    assertNotNull(keyStruct);

    CompletableFuture<TdiKeyStructureShape> getFuture = objKeystoreFacet
        .getKeyByRole(keyStruct.getRoleFlag(), keyStruct.getFleetId());
    assertNotNull(getFuture);

    TdiKeyStructureShape getKeyStruct = getFuture.get();
    assertNotNull(getKeyStruct);
    assertTrue(getKeyStruct.getFleetId().equals(getKeyStruct.getFleetId()));
    assertTrue(keyStruct.getFlags() == getKeyStruct.getFlags());
    assertNotNull(keyStruct.getKeyData());
  }

  @Test(expected = ExecutionException.class)
  public void testGetKeyByRoleWithExcption() throws Exception {
    String fleetId = "9724baf5-0ec5-44ec-9bd7-Bad-Test";
    Integer role = 12343;

    CompletableFuture<TdiKeyStructureShape> future = objKeystoreFacet.getKeyByRole(role, fleetId);
    assertNotNull(future);
    TdiKeyStructureShape keyStruct = future.get();
    assertNotNull(keyStruct);
  }

  @Test
  public void testGetPublicPem() throws Exception {
    String key = "{\"use\":\"sig\",\"alg\":\"ES256\",\"kty\":\"EC\","
        + "\"kid\":\"9724baf5-0ec5-44ec-9bd7-2795d0814bdf\","
        + "\"crv\":\"P-256\",\"x\":\"ykbxmdBGk_G2LrHJ2R2YQTUX5eBOm4zSsJF8blpE_Bw\","
        + "\"y\":\"IwwJLBglVpBEOELjnQ0XXWrt5WPs5RO4BP-2NXN3w2M\","
        + "\"d\":\"TxEyAp7JrLA3xyw17Qw-O-L9zM_nurjgqJG5BvLTxjY\"}";

    Object ecPublicKey = objKeystoreFacet.getPublicPem(key);
    assertNotNull(ecPublicKey);
    assertFalse((ecPublicKey instanceof ECPublicKey));
  }

  @Test
  public void testGetPublicPemEmptyKey() throws Exception {
    String key = "";
    Object ecPublicKey = objKeystoreFacet.getPublicPem(key);
    assertNull(ecPublicKey);
  }

  @Test
  public void testGetPublicPemInvalidKey() throws Exception {
    String key = "InvalidJSON";
    Object ecPublicKey = objKeystoreFacet.getPublicPem(key);
    assertNull(ecPublicKey);
  }

  @Test
  public void testGenerateKey() throws InterruptedException, ExecutionException {
    Integer flags = 0;
    String kid = "";
    String fleetId = "";

    CompletableFuture<TdiKeyStructureShape> future = objKeystoreFacet.generateKey(flags, kid,
        fleetId);
    assertNotNull(future);
    TdiKeyStructureShape keyStruct = future.get();
    assertNotNull(keyStruct);
    assertTrue(keyStruct.getFleetId().equals(fleetId));
    assertFalse(keyStruct.getKeyId().equals(""));
    assertNotNull(keyStruct.getKeyData());
    assertTrue(keyStruct.canSign());
    assertTrue(keyStruct.isGenerated());
  }

  @Test
  public void testGenerateKeyWithValidKey() throws InterruptedException, ExecutionException {
    Integer flags = 0;
    String kid = "9724baf5-0ec5-44ec-9bd7-2795d0814bdf";
    String fleetId = "9724baf5-0ec5-44ec-9bd7-2795d0814bdf";

    CompletableFuture<TdiKeyStructureShape> future = objKeystoreFacet.generateKey(flags, kid,
        fleetId);
    assertNotNull(future);
    TdiKeyStructureShape keyStruct = future.get();
    assertNotNull(keyStruct);
    assertTrue(keyStruct.getFleetId().equals(fleetId));
    assertTrue(keyStruct.canSign());
    assertTrue(keyStruct.isGenerated());
    assertTrue(keyStruct.getKeyId().equals(kid));
    assertNotNull(keyStruct.getKeyData());
  }

  @Test
  public void testSetKey() throws InterruptedException, ExecutionException {

    Integer flags = 333;
    String kid = "9724baf5-0ec5-44ec-9bd7-2795dPatil";
    String fleetId = "9724baf5-0ec6-55ec-9bd7-2795dPatil";
    String key = "{\"use\":\"sig\",\"alg\":\"ES256\",\"kty\":\"EC\"," + "\"kid\":\"" + kid
        + "\",\"crv\":\"P-256\"," + "\"x\":\"ykbxmdBGk_G2LrHJ2R2YQTUX5eBOm4zSsJF8blpE_PP\","
        + "\"y\":\"IwwJLBglVpBEOELjnQ0XXWrt5WPs5RO4BP-2NXN3wPP\","
        + "\"d\":\"TxEyAp7JrLA3xyw17Qw-O-L9zM_nurjgqJG5BvLTxPP\"}";

    CompletableFuture<TdiKeyStructureShape> future = objKeystoreFacet.setKey(key, flags, fleetId);
    assertNotNull(future);

    TdiKeyStructureShape argTdiKeyStructure = future.get();
    assertNotNull(argTdiKeyStructure);
    assertTrue(argTdiKeyStructure.getFleetId().equals(fleetId));
    assertTrue(argTdiKeyStructure.getFlags() == flags);
    assertTrue(argTdiKeyStructure.getKeyId().equals(kid));

    String keyId = argTdiKeyStructure.getKeyId();

    CompletableFuture<TdiKeyStructureShape> verifyFuture = objKeystoreFacet.getKey(keyId);
    assertNotNull(verifyFuture);

    TdiKeyStructureShape getKeyObj = verifyFuture.get();
    assertNotNull(getKeyObj);
    assertTrue((getKeyObj.getFleetId().equals(fleetId)));
    assertTrue((getKeyObj.getFlags() == flags));
    assertTrue((getKeyObj.getKeyId().equals(keyId)));
  }

  @Test
  public void testSetKeyWithKeysObject() throws InterruptedException, ExecutionException,
      JsonParseException, JsonMappingException, IOException, InvalidFormatException {
    Integer flags = 333;
    String fleetId = "9724baf5-0ec6-55ec-9bd7-2795dPatil";
    String key = "{\"use\":\"sig\",\"alg\":\"ES256\",\"kty\":\"EC\","
        + "\"kid\":\"9724baf5-0ec5-44ec-9bd7-2795dPatil\",\"crv\":\"P-256\","
        + "\"x\":\"ykbxmdBGk_G2LrHJ2R2YQTUX5eBOm4zSsJF8blpE_PP\","
        + "\"y\":\"IwwJLBglVpBEOELjnQ0XXWrt5WPs5RO4BP-2NXN3wPP\","
        + "\"d\":\"TxEyAp7JrLA3xyw17Qw-O-L9zM_nurjgqJG5BvLTxPP\"}";

    KeyRef keyRefObj = biz.neustar.tdi.fw.utils.Utils.jsonToObject((String) key, KeyRef.class);

    Key keysObj = new Key();
    keysObj.flags = flags;
    keysObj.ref = keyRefObj;
    keysObj.fleet = fleetId;
    keysObj.kid = "9724baf5-0ec5-44ec-9bd7-2795dPatil";

    CompletableFuture<TdiKeyStructureShape> future = objKeystoreFacet.setKey(keysObj, flags,
        fleetId);
    assertNotNull(future);

    TdiKeyStructureShape argTdiKeyStructure = future.get();
    assertNotNull(argTdiKeyStructure);
    assertTrue(argTdiKeyStructure.getFleetId().equals(fleetId));
    assertTrue(argTdiKeyStructure.getFlags() == flags);

    String keyId = argTdiKeyStructure.getKeyId();

    CompletableFuture<TdiKeyStructureShape> verifyFuture = objKeystoreFacet.getKey(keyId);
    assertNotNull(verifyFuture);
    TdiKeyStructureShape getKeyObj = verifyFuture.get();
    assertNotNull(getKeyObj);
    assertTrue((getKeyObj.getFleetId() == fleetId));
    assertTrue((getKeyObj.getFlags() == flags));
  }

  @Test
  public void testGetKeys() throws Exception {
    CompletableFuture<List<TdiKeyStructureShape>> future = objKeystoreFacet.getKeys();
    assertNotNull(future);
    List<TdiKeyStructureShape> keyStructList = future.get();
    assertNotNull(keyStructList);
    assertTrue((keyStructList.size() > 0));
  }

  @Test
  public void testForgetKey() {
    String kid = "9724baf5-0ec5-44ec-9bd7-2795d0814bdf";
    objKeystoreFacet.forgetKey(kid).thenApply((arg) -> {
      objKeystoreFacet.getKey(kid).thenApply((key) -> {
        assertNull(key);
        return true;
      });
      return true;
    });
  }

  @Test
  public void testForgetKeyNullKey() throws InterruptedException, ExecutionException {
    String kid = null;
    CompletableFuture<Void> future = objKeystoreFacet.forgetKey(kid);
    future.get();
  }

  @Test
  public void testForgetKeyInvalidKey() throws InterruptedException, ExecutionException {
    String kid = "InvalidKey";
    CompletableFuture<Void> future = objKeystoreFacet.forgetKey(kid);
    future.get();
  }

  @Test
  public void testSetKeyFromProvision() throws InterruptedException, ExecutionException {
    String str = "{\"fleet\": \"9724baf5-0ec5-44ec-9bd7-2795d0814bdf\","
        + "\"kid\": \"9724baf5-0ec5-44ec-9bd7-2795d0814bdf\","
        + "\"pem\": \"-----BEGIN PUBLIC KEY-----\\n"
        + "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEJLzzbuz2tRnLFlOL+6bTX6giVavA\\n"
        + "sc6NDFFT0IMCd2ibTTNUDDkFGsgq0cH5JYPg/6xUlMBFKrWYe3yQ4has9w==\\n"
        + "-----END PUBLIC KEY-----\\n\"," + "\"flags\": 3}";

    CompletableFuture<TdiKeyStructureShape> future = objKeystoreFacet.setKeyFromProvision(str);
    TdiKeyStructureShape keyStruct = future.get();
    assertNotNull(keyStruct);
    assertTrue(keyStruct.getKeyId().equals("9724baf5-0ec5-44ec-9bd7-2795d0814bdf"));
    assertTrue(keyStruct.getFleetId().equals("9724baf5-0ec5-44ec-9bd7-2795d0814bdf"));
    assertTrue((keyStruct.getFlags() == 3));
  }

  @Test(expected = ExecutionException.class)
  public void testSetKeyFromProvisionNullPem() throws InterruptedException, ExecutionException {
    String str = "{\"fleet\": \"9724baf5-0ec5-44ec-9bd7-2795d0814bdf\","
        + "\"kid\": \"9724baf5-0ec5-44ec-9bd7-2795d0814bdf\"," + "\"pem\": null," + "\"flags\": 3}";
    CompletableFuture<TdiKeyStructureShape> future = objKeystoreFacet.setKeyFromProvision(str);
    TdiKeyStructureShape keyStruct = future.get();
    assertNotNull(keyStruct);
  }

  @Test
  public void testFromPublicPemNullPem()
      throws InterruptedException, ExecutionException, IOException {
    byte[] pemByte = Utils.toBytes(null);
    ECPublicKey pubKey = objKeystoreFacet.fromPublicPem(pemByte);
    assertNull(pubKey);
  }

  @Test
  public void testGetPemBytes() {
    byte[] bytes = objKeystoreFacet.getPemBytes(null);
    assertNull(bytes);
  }

  @Test(expected = ExecutionException.class)
  public void testSetKeyFromProvisionNullKey() throws InterruptedException, ExecutionException {
    String str = null;
    CompletableFuture<TdiKeyStructureShape> future = objKeystoreFacet.setKeyFromProvision(str);
    future.get();
  }

  @Test(expected = ExecutionException.class)
  public void testSetKeyFromProvisionInvalidKeyObj()
      throws InterruptedException, ExecutionException {
    Object obj = new Object();
    CompletableFuture<TdiKeyStructureShape> future = objKeystoreFacet.setKeyFromProvision(obj);
    future.get();
  }

  @Test
  public void testEmptyConfig() throws Exception {
    TdiPlatformShape testPlatform = TestData.getDummyLibraryPlatform();
    @SuppressWarnings("unchecked")
    Map<String, Object> keystoreConfig = (Map<String, Object>) testPlatform.getConfig()
        .get(Constants.PLATFORM_CONFIG_KEY_KEYSTORE);

    keystoreConfig.remove(Constants.PLATFORM_CONFIG_KEY_BASEPATH);
    KeystoreFacet testStoreFacet = new KeystoreFacet(testPlatform);

    assertEquals(Constants.PLATFORM_DEFAULT_BASE_PATH + Constants.KEY_CONFIG_FILE,
        testStoreFacet.keystoreFilePath);

    testPlatform.getConfig().remove(Constants.PLATFORM_CONFIG_KEY_KEYSTORE);
    testStoreFacet = new KeystoreFacet(testPlatform);
    assertEquals(Constants.PLATFORM_DEFAULT_BASE_PATH + Constants.KEY_CONFIG_FILE,
        testStoreFacet.keystoreFilePath);
  }
}
