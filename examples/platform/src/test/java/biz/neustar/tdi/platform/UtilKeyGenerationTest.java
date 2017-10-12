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

package biz.neustar.tdi.platform;

import static org.junit.Assert.assertNotNull;

import biz.neustar.tdi.platform.exception.PlatformRuntimeException;
import biz.neustar.tdi.platform.facet.keystore.KeyRef;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;

/**
 * JUnit test for UtilKeyGeneration class.
 * 
 */
public class UtilKeyGenerationTest {

  /**
   * UtilKeyGenerationTest JUnit Setup method.
   * 
   * @throws Exception
   * 
   */
  @BeforeClass
  public static void setup() throws Exception {

  }

  @Test
  public void testGetEcpublicKey()
      throws NoSuchAlgorithmException, NoSuchProviderException, InvalidParameterSpecException,
      InvalidKeySpecException, JsonParseException, JsonMappingException, IOException {

    // Test data
    String key = "{\"use\":\"sig\",\"alg\":\"ES256\",\"kty\":\"EC\","
        + "\"kid\":\"9724baf5-0ec5-44ec-9bd7-2795d0814bdf\","
        + "\"crv\":\"P-256\",\"x\":\"ykbxmdBGk_G2LrHJ2R2YQTUX5eBOm4zSsJF8blpE_Bw\","
        + "\"y\":\"IwwJLBglVpBEOELjnQ0XXWrt5WPs5RO4BP-2NXN3w2M\","
        + "\"d\":\"TxEyAp7JrLA3xyw17Qw-O-L9zM_nurjgqJG5BvLTxjY\"}";

    // Create jackson mapper object
    ObjectMapper objectMapper = new ObjectMapper();

    // Read JSON from file to Object
    KeyRef ref = objectMapper.readValue(key, KeyRef.class);

    // Call get public key
    ECPublicKey publicKey = UtilKeyGeneration.generateEcpublicKey(ref);
    assertNotNull(publicKey);
  }

  @Test(expected = PlatformRuntimeException.class)
  public void testGetEcpublicKey_InvalidKeySpec()
      throws NoSuchAlgorithmException, NoSuchProviderException, InvalidParameterSpecException,
      InvalidKeySpecException, JsonParseException, JsonMappingException, IOException {

    // Test data
    String key = "{\"use\":\"sig\",\"alg\":\"ES256\",\"kty\":\"EC\","
        + "\"kid\":\"9724baf5-0ec5-44ec-9bd7-2795d0814bdf\","
        + "\"crv\":\"P-256\",\"x\":\"ykbxmdBGk_G2LrHJ2R2YQTUX5eBOm4zSsJF8blpE_Bw\","
        + "\"y\":\"IwwJLBglVpBEOELjnQ0XXWrt5WPs5RO4BP-2NXN3\","
        + "\"d\":\"TxEyAp7JrLA3xyw17Qw-O-L9zM_nurjgqJG5BvLTxjY\"}";

    // Create jackson mapper object
    ObjectMapper objectMapper = new ObjectMapper();

    // Read JSON from file to Object
    KeyRef ref = objectMapper.readValue(key, KeyRef.class);

    // Call get public key
    UtilKeyGeneration.generateEcpublicKey(ref);
  }

  @Test(expected = PlatformRuntimeException.class)
  public void testGetEcpublicKeyWithNullKeyRef() throws NoSuchAlgorithmException,
      NoSuchProviderException, InvalidParameterSpecException, InvalidKeySpecException {

    KeyRef keyRef = null;
    UtilKeyGeneration.generateEcpublicKey(keyRef);
  }

  @Test
  public void testGenerateEcPrivateKey()
      throws NoSuchAlgorithmException, NoSuchProviderException, InvalidParameterSpecException,
      InvalidKeySpecException, JsonParseException, JsonMappingException, IOException {

    // Test data
    String key = "{\"use\":\"sig\",\"alg\":\"ES256\",\"kty\":\"EC\","
        + "\"kid\":\"9724baf5-0ec5-44ec-9bd7-2795d0814bdf\","
        + "\"crv\":\"P-256\",\"x\":\"ykbxmdBGk_G2LrHJ2R2YQTUX5eBOm4zSsJF8blpE_Bw\","
        + "\"y\":\"IwwJLBglVpBEOELjnQ0XXWrt5WPs5RO4BP-2NXN3w2M\","
        + "\"d\":\"TxEyAp7JrLA3xyw17Qw-O-L9zM_nurjgqJG5BvLTxjY\"}";

    // Create jackson mapper object
    ObjectMapper objectMapper = new ObjectMapper();

    // Read JSON from file to Object
    KeyRef ref = objectMapper.readValue(key, KeyRef.class);

    // Create priavte key
    ECPrivateKey privateKey = UtilKeyGeneration.generateEcPrivateKey(ref);
    assertNotNull(privateKey);
  }

  @Test(expected = PlatformRuntimeException.class)
  public void testGenerateEcPrivateKey_InvalidKeySpec()
      throws NoSuchAlgorithmException, NoSuchProviderException, InvalidParameterSpecException,
      InvalidKeySpecException, JsonParseException, JsonMappingException, IOException {

    // Test data
    String key = "{\"use\":\"sig\",\"alg\":\"ES256\",\"kty\":\"EC\","
        + "\"kid\":\"9724baf5-0ec5-44ec-9bd7-2795d0814bdf\","
        + "\"crv\":\"P-256\",\"x\":\"ykbxmdBGk_G2LrHJ2R2YQTUX5eBOm4zSsJF8blpE_Bw\","
        + "\"y\":\"IwwJLBglVpBEOELjnQ0XXWrt5WPs5RO4BP-2NXN3w2M\"," + "\"d\":\"\"}";

    // Create jackson mapper object
    ObjectMapper objectMapper = new ObjectMapper();

    // Read JSON from file to Object
    KeyRef ref = objectMapper.readValue(key, KeyRef.class);

    // Create priavte key
    UtilKeyGeneration.generateEcPrivateKey(ref);
  }

  @Test(expected = PlatformRuntimeException.class)
  public void testGenerateEcPrivateKeyWithNullD()
      throws NoSuchAlgorithmException, NoSuchProviderException, InvalidParameterSpecException,
      InvalidKeySpecException, JsonParseException, JsonMappingException, IOException {

    // Test data
    String key = "{\"use\":\"sig\",\"alg\":\"ES256\",\"kty\":\"EC\","
        + "\"kid\":\"9724baf5-0ec5-44ec-9bd7-2795d0814bdf\","
        + "\"crv\":\"P-256\",\"x\":\"ykbxmdBGk_G2LrHJ2R2YQTUX5eBOm4zSsJF8blpE_Bw\","
        + "\"y\":\"IwwJLBglVpBEOELjnQ0XXWrt5WPs5RO4BP-2NXN3w2M\"," + "\"d\":null}";

    // Create jackson mapper object
    ObjectMapper objectMapper = new ObjectMapper();

    // Read JSON from file to Object
    KeyRef ref = objectMapper.readValue(key, KeyRef.class);

    // Create priavte key
    ECPrivateKey privateKey = UtilKeyGeneration.generateEcPrivateKey(ref);
    assertNotNull(privateKey);
  }

  @Test(expected = PlatformRuntimeException.class)
  public void testGenerateEcPrivateKeyWithNullKeyRef() throws NoSuchAlgorithmException,
      NoSuchProviderException, InvalidParameterSpecException, InvalidKeySpecException {
    KeyRef keyRef = null;
    UtilKeyGeneration.generateEcPrivateKey(keyRef);
  }
}
