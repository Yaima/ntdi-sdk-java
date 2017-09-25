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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import biz.neustar.tdi.fw.keystructure.TdiKeyStructure;
import biz.neustar.tdi.fw.keystructure.TdiKeyStructureShape;
import biz.neustar.tdi.fw.platform.TdiPlatformShape;
import biz.neustar.tdi.platform.facet.CryptFacet;
import biz.neustar.tdi.platform.facet.keystore.KeyRef;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Unit test class for CrypFact.
 * 
 *
 */
public class CryptFacetTest {

  public static CryptFacet objCryptFacet;
  public static TdiPlatformShape pf;

  // Test data used for sign and verify 
  public static KeyRef keyRef  = new KeyRef();
  public static String kid     = "9724baf5-0ec5-44ec-9bd7-2795d0814bdf"; 
  public static String fleet   = "9724baf5-0ec5-44ec-9bd7-2795d0814bdf";
  public static int flags      = 2;
  public static String payload = "Test Data For Sign";
  
  /**
   * CrypFacetTest Junit Setup method.
   * 
   * @throws Exception
   * 
   */
  @BeforeClass
  public static void setup() throws Exception {
    
    // Set KeyRef test data
    keyRef.use    = "sig";
    keyRef.alg    = "ES256";
    keyRef.kty    = "EC";
    keyRef.kid    = "9724baf5-0ec5-44ec-9bd7-2795d0814bdf";
    keyRef.crv    = "P-256";
    keyRef.cordX      = "ykbxmdBGk_G2LrHJ2R2YQTUX5eBOm4zSsJF8blpE_Bw";
    keyRef.cordY      = "IwwJLBglVpBEOELjnQ0XXWrt5WPs5RO4BP-2NXN3w2M";
    keyRef.privateD      = "TxEyAp7JrLA3xyw17Qw-O-L9zM_nurjgqJG5BvLTxjY";

    pf = TestData.getDummyLibraryPlatform();
    objCryptFacet = new CryptFacet(pf);
  }
  
  @Test
  public void testgetPlatform() {
    TdiPlatformShape platformObj = objCryptFacet.getPlatform();
    assertNotNull(platformObj);
    assertTrue(platformObj instanceof TdiPlatformShape);
  }
  
  @Test
  public void testInit() {
    CompletableFuture<Void> future = objCryptFacet.init();
    assertNotNull(future);
  }
  
  @Test
  public void testSign() throws InterruptedException, ExecutionException { 
    
    TdiKeyStructureShape key = new TdiKeyStructure(kid, fleet,
        keyRef, flags);
    
    // Sign payload with key
    CompletableFuture<String> future = objCryptFacet.sign(key, payload);
    assertNotNull(future);
    
    String signPayload = future.get();
    assertNotNull(signPayload);
    assertFalse(signPayload.isEmpty());
  }

  @Test(expected = ExecutionException.class)
  public void testSignWithNullKey() throws InterruptedException, ExecutionException { 
  
    TdiKeyStructureShape key = null;
    String payload = "";
    CompletableFuture<String> future = objCryptFacet.sign(key, payload);
    assertNotNull(future);
    future.get(); 
  }
  
  @Test(expected = ExecutionException.class)
  public void testSignWithInvalidKeyRef() throws InterruptedException, ExecutionException { 
  
    TdiKeyStructureShape key = new TdiKeyStructure(kid, fleet,
        null, flags);
    CompletableFuture<String> future = objCryptFacet.sign(key, payload);
    assertNotNull(future);
    future.get(); 
  }  
  
  @Test(expected = ExecutionException.class)
  public void testSignWithNullPayload() throws InterruptedException, ExecutionException { 
  
    TdiKeyStructureShape key = new TdiKeyStructure(kid, fleet,
        keyRef, flags);
    String payload = null;
    
    CompletableFuture<String> future = objCryptFacet.sign(key, payload);
    assertNotNull(future);
    future.get();
  }
  
  @Test(expected = ExecutionException.class)
  public void testSignWithEmptyPayload() throws InterruptedException, ExecutionException { 
  
    TdiKeyStructureShape key = new TdiKeyStructure(kid, fleet,
        keyRef, flags);
    
    String payload = "";
    CompletableFuture<String> future = objCryptFacet.sign(key, payload);
    assertNotNull(future);
    future.get(); 
  }  
  
  @Test
  public void testVerify() throws InterruptedException, ExecutionException {
  
    TdiKeyStructureShape key = new TdiKeyStructure(kid, fleet,
        keyRef, flags);
    
    // This test signature is generated using sign method with default payload text
    String signature = "38fis8Rf_fCTw3Jk74WF9UdQcu1Tqwf"
                      + "UahppEMdKgTS4MeyySphKu1DS"
                      + "--oRN4yOBKrzqHToPeBpyQd1dyuecg";

    CompletableFuture<Boolean> future = objCryptFacet.verify(key, payload, signature);
    assertNotNull(future);
    boolean result = future.get();
    assertTrue(result);
  }
  
  @Test
  public void testVerifyDiffPayload() throws InterruptedException, ExecutionException {
  
    TdiKeyStructureShape key = new TdiKeyStructure(kid, fleet,
        keyRef, flags);
    
    // Create new payload as different text
    String payload = "Some test Data";
    
    // This test signature is generated using sign method with default payload text
    String signature = "38fis8Rf_fCTw3Jk74WF9UdQcu1TqwfUahppEMdKgTS4MeyySphKu1DS"
                     + "--oRN4yOBKrzqHToPeBpyQd1dyuecg";

    CompletableFuture<Boolean> future = objCryptFacet.verify(key, payload, signature);
    assertNotNull(future);
    boolean result = future.get();
    assertFalse(result);
  }

  @Test
  public void testVerifyDiffSign() throws InterruptedException, ExecutionException {
  
    TdiKeyStructureShape key = new TdiKeyStructure(kid, fleet,
        keyRef, flags);
    
    // Garbage signature string for testing
    String signature = "38fis8Rf_fCTw3Jk74WF9UdQcu1TqwfUahppEMdKgTS4MeyySphKu1DS"
                     + "--oRN4yOBKrzqHToPeBpyQd1dyuecg_BAD";

    CompletableFuture<Boolean> future = objCryptFacet.verify(key, payload, signature);
    assertNotNull(future);
    boolean result = future.get();
    assertFalse(result);
  }

  @Test(expected = ExecutionException.class)
  public void testVerifyNullKey() throws InterruptedException, ExecutionException {
  
    TdiKeyStructureShape key = null;
    String signature = "38fis8Rf_fCTw3Jk74WF9UdQcu1TqwfUahppEMdKgTS4MeyySphKu1DS"
                     + "--oRN4yOBKrzqHToPeBpyQd1dyuecg";

    CompletableFuture<Boolean> future = objCryptFacet.verify(key, payload, signature);
    assertNotNull(future);
    boolean result = future.get();
    assertFalse(result);
  }
  
  @Test(expected = ExecutionException.class)
  public void testVerifyNullKeyRef() throws InterruptedException, ExecutionException {
  
    TdiKeyStructureShape key = new TdiKeyStructure(kid, fleet,
        null, flags);
    
    // signature
    String signature = "38fis8Rf_fCTw3Jk74WF9UdQcu1TqwfUahppEMdKgTS4MeyySphKu1DS"
                     + "--oRN4yOBKrzqHToPeBpyQd1dyuecg";

    CompletableFuture<Boolean> future = objCryptFacet.verify(key, payload, signature);
    assertNotNull(future);
    boolean result = future.get();
    assertFalse(result);
  }

  @Test(expected = ExecutionException.class)
  public void testVerifyEmptyPayload() throws InterruptedException, ExecutionException {
  
    TdiKeyStructureShape key = new TdiKeyStructure(kid, fleet,
        keyRef, flags);
    
    // Garbage signature string for testing
    String signature = "38fis8Rf_fCTw3Jk74WF9UdQcu1TqwfUahppEMdKgTS4MeyySphKu1DS"
                     + "--oRN4yOBKrzqHToPeBpyQd1dyuecg";
    
    // Test empty payload 
    String payload = "";
    CompletableFuture<Boolean> future = objCryptFacet.verify(key, payload, signature);
    assertNotNull(future);
    boolean result = future.get();
    assertFalse(result);
  }

  @Test(expected = ExecutionException.class)
  public void testVerifyEmptySignature() throws InterruptedException, ExecutionException {
  
    TdiKeyStructureShape key = new TdiKeyStructure(kid, fleet,
        keyRef, flags);
    
    String signature = "";
    CompletableFuture<Boolean> future = objCryptFacet.verify(key, payload, signature);
    assertNotNull(future);
    boolean result = future.get();
    assertFalse(result);
  }

  @Test
  public void testSignAndVerify() 
      throws InterruptedException, 
      ExecutionException {
    
    TdiKeyStructureShape key = new TdiKeyStructure(kid, fleet,
        keyRef, flags);
    
    CompletableFuture<String> signFuture = objCryptFacet.sign(key, payload);
    assertNotNull(signFuture);
    String signedStr = signFuture.get();
    assertNotNull(signedStr);
    
    CompletableFuture<Boolean> verifyFuture = objCryptFacet.verify(key, payload, signedStr);
    assertNotNull(verifyFuture);
    Boolean result = verifyFuture.get();
    assertTrue(result);
  }
  
  @Test
  public void testSignAndVerifyWithDiffPayload() 
      throws InterruptedException, 
      ExecutionException {
    
    TdiKeyStructureShape key = new TdiKeyStructure(kid, fleet,
        keyRef, flags);
    
    CompletableFuture<String> signFuture = objCryptFacet.sign(key, payload);
    assertNotNull(signFuture);
    String signedStr = signFuture.get();
    assertNotNull(signedStr);
    
    String payload = "Test New Payload Random 12345 Test Test"; // Different Payload
    CompletableFuture<Boolean> verifyFuture = objCryptFacet.verify(key, payload, signedStr);
    assertNotNull(verifyFuture);
    Boolean result = verifyFuture.get();
    assertFalse(result);
  }
  
  @Test(expected = ExecutionException.class)
  public void testSignAndVerifyWithEmptySignature() 
      throws InterruptedException, 
      ExecutionException {
    
    TdiKeyStructureShape key = new TdiKeyStructure(kid, fleet,
        keyRef, flags);
    
    CompletableFuture<Boolean> verifyFuture = objCryptFacet.verify(key, payload, "");
    assertNotNull(verifyFuture);
    Boolean result = verifyFuture.get();
    assertFalse(result);
  }
  
  @Test
  public void testDecrypt() {
    CompletableFuture<Void> future = objCryptFacet.decrypt();
    assertNotNull(future);
  }
  
  @Test
  public void testEncrypt() {
    CompletableFuture<Void> future = objCryptFacet.encrypt();
    assertNotNull(future);
  }
    
}
