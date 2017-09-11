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

package biz.neustar.tdi.sdk.component;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import biz.neustar.tdi.fw.canonicalmessage.TdiCanonicalMessage;
import biz.neustar.tdi.fw.canonicalmessage.TdiCanonicalMessageShape;
import biz.neustar.tdi.fw.canonicalmessage.TdiClaims;
import biz.neustar.tdi.fw.exception.FrameworkRuntimeException;
import biz.neustar.tdi.fw.keystructure.TdiKeyStructure;
import biz.neustar.tdi.fw.utils.Utils;
import biz.neustar.tdi.sdk.Constants.DefaultJws;
import biz.neustar.tdi.sdk.TestData;
import biz.neustar.tdi.sdk.component.jws.TdiJws;
import biz.neustar.tdi.sdk.component.jws.TdiJwsHeader;
import biz.neustar.tdi.sdk.component.jws.TdiJwsSignature;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;
import java.util.Map;

/**
 * Unit test cases for {@link TdiSdkJsonWebSignature} class.
 */
public class TdiSdkJsonWebSignatureTest {
  private static TdiSdkJsonWebSignature component = null;

  @BeforeClass
  public static void setup() {
    component = new TdiSdkJsonWebSignature("jws", TestData.getDummyImplementation());
  }

  @Test
  public void testInit() throws Exception {
    assertNull(component.init().get());
  }

  @Test
  public void testSign() throws Exception {
    TdiClaims claims = new TdiClaims();
    claims.exp = System.currentTimeMillis();
    claims.nbf = System.currentTimeMillis();
    claims.payload = "Payload";
    claims.iss = "Issuer";

    TdiCanonicalMessageShape message = new TdiCanonicalMessage(1);
    TdiSdkJsonWebTokenComponent jwtComponent = new TdiSdkJsonWebTokenComponent("jwt",
        TestData.getDummyImplementation());

    // Add claims to the message
    message.setClaims(claims);

    // Pack the claims.
    message = jwtComponent.packClaims(message).get();

    // Add the signers.
    message.addSigner(new TdiKeyStructure("id1", "id1", null, 0));
    message.addSigner(new TdiKeyStructure("id2", "id2", null, 0));

    // Sign the message.
    message = component.sign(message).get();
    TdiJws jwsMessage = Utils.jsonToObject(message.getBuiltMessage(), TdiJws.class);

    // Check the basic structure.
    assertNotNull(jwsMessage.payload);
    assertNotNull(jwsMessage.signatures);
    assertEquals(2, jwsMessage.signatures.size());

    // Check the payload
    String payloadJson = TestData.getDummyUtils().b64UrlDecode(jwsMessage.payload);
    TdiClaims payloadClaims = Utils.jsonToObject(payloadJson, TdiClaims.class);
    assertEquals(claims, payloadClaims);

    // Check the signatures
    for (TdiJwsSignature signature : jwsMessage.signatures) {
      String protectedHeader = TestData.getDummyUtils().b64UrlDecode(signature.protectedHeader);
      TdiJwsHeader header = Utils.jsonToObject(protectedHeader, TdiJwsHeader.class);
      assertEquals(DefaultJws.type, header.typ);
      assertEquals(DefaultJws.alg, header.alg);
    }
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testSignWithCustomSignatureType() throws Exception {
    TdiClaims claims = new TdiClaims();
    claims.exp = System.currentTimeMillis();
    claims.nbf = System.currentTimeMillis();
    claims.payload = "Payload";
    claims.iss = "Issuer";

    String customSignatureType = "customType";
    TdiCanonicalMessageShape message = new TdiCanonicalMessage(1);
    TdiSdkJsonWebTokenComponent jwtComponent = new TdiSdkJsonWebTokenComponent("jwt",
        TestData.getDummyImplementation());

    // Add custom signature type
    message.setSignatureType(customSignatureType);

    // Add claims to the message
    message.setClaims(claims);

    // Pack the claims.
    message = jwtComponent.packClaims(message).get();

    // Add the signers.
    message.addSigner(new TdiKeyStructure("id1", "id1", null, 0));
    message.addSigner(new TdiKeyStructure("id2", "id2", null, 0));

    // Sign the message.
    message = component.sign(message).get();
    Map<String, Object> builtMsgMap = Utils.jsonToMap(message.getBuiltMessage());

    // Check the signature type.
    for (Map<String, Object> signatureMap : ((List<Map<String, Object>>) builtMsgMap
        .get("signatures"))) {
      String protectedHeader = TestData.getDummyUtils()
          .b64UrlDecode((String) signatureMap.get("protected"));
      Map<String, Object> headerMap = (Map<String, Object>) Utils.jsonToMap(protectedHeader);
      assertEquals(customSignatureType, headerMap.get("typ"));
    }
  }

  @Test
  public void testUnpack() throws Exception {
    TdiClaims claims = new TdiClaims();
    claims.exp = System.currentTimeMillis();
    claims.nbf = System.currentTimeMillis();
    claims.payload = "Payload";
    claims.iss = "Issuer";

    TdiCanonicalMessageShape message = new TdiCanonicalMessage(1);
    TdiSdkJsonWebTokenComponent jwtComponent = new TdiSdkJsonWebTokenComponent("jwt",
        TestData.getDummyImplementation());

    // Add claims to the message
    message.setClaims(claims);

    // Pack the claims.
    message = jwtComponent.packClaims(message).get();

    // Add the signers.
    message.addSigner(new TdiKeyStructure("id1", "id1", null, 0));
    message.addSigner(new TdiKeyStructure("id2", "id2", null, 0));

    // Sign the message.
    message = component.sign(message).get();

    // Create a new object to unpack claims
    TdiCanonicalMessageShape unpackedMsg = new TdiCanonicalMessage(1);
    unpackedMsg.setReceivedMessage(message.getBuiltMessage());

    // Unpack the received message.
    unpackedMsg = component.unpack(unpackedMsg).get();

    // Check the payload:
    assertNotNull(unpackedMsg.getRawPayload());
    assertEquals(2, unpackedMsg.getSignaturesToVerify().size());

    // Check the payload
    String payloadJson = TestData.getDummyUtils().b64UrlDecode(unpackedMsg.getRawPayload());
    TdiClaims payloadClaims = Utils.jsonToObject(payloadJson, TdiClaims.class);
    assertEquals(claims, payloadClaims);

    // Check the signatures
    List<Object> signaturesToVerify = unpackedMsg.getSignaturesToVerify();
    for (int loopIndex = 0; loopIndex < signaturesToVerify.size(); loopIndex++) {
      TdiJwsSignature signature = (TdiJwsSignature) signaturesToVerify.get(loopIndex);
      assertEquals(DefaultJws.type, signature.parsedHeader.typ);
      assertEquals(DefaultJws.alg, signature.parsedHeader.alg);
    }
  }

  @Test
  public void testUnpackWithCompactJws() throws Exception {
    String message = "eyJraWQiOiJpZDIiLCJ0eXAiOiJKT1NFK0pTT04iLCJhbGciOiJFUzI1NiJ9."
        + "eyJrZXkxIjoidmFsdWUxIiwia2V5MiI6InZhbHVlMiJ9.MjkzMDE2ODYy";
    TdiCanonicalMessageShape unpackedMsg = new TdiCanonicalMessage(1);
    unpackedMsg.setReceivedMessage(message);

    unpackedMsg = component.unpack(unpackedMsg).get();
    // Check the payload:
    assertNotNull(unpackedMsg.getRawPayload());
    assertEquals(1, unpackedMsg.getSignaturesToVerify().size());

    // Check the signatures
    List<Object> signaturesToVerify = unpackedMsg.getSignaturesToVerify();
    for (int loopIndex = 0; loopIndex < signaturesToVerify.size(); loopIndex++) {
      TdiJwsSignature signature = (TdiJwsSignature) signaturesToVerify.get(loopIndex);
      assertEquals(DefaultJws.type, signature.parsedHeader.typ);
      assertEquals(DefaultJws.alg, signature.parsedHeader.alg);
    }
  }

  @Test
  public void testUnpackWithInvalidMessages() throws Exception {
    TdiCanonicalMessageShape unpackedMsg = new TdiCanonicalMessage(1);

    // Test condition for empty or null received message.
    try {
      component.unpack(unpackedMsg);
    } catch (Exception e) {
      assertTrue(e instanceof FrameworkRuntimeException);
    }

    // Test for empty json.
    unpackedMsg.setReceivedMessage("{}");
    try {
      component.unpack(unpackedMsg);
    } catch (Exception e) {
      assertTrue(e instanceof FrameworkRuntimeException);
    }

    // Test for json with only payload
    unpackedMsg.setReceivedMessage("{\"payload\":\"TempPayload\"}");
    try {
      component.unpack(unpackedMsg);
    } catch (Exception e) {
      assertTrue(e instanceof FrameworkRuntimeException);
    }

    // Test for json with empty signatures
    unpackedMsg.setReceivedMessage("{\"signatures\":[]}");
    try {
      component.unpack(unpackedMsg);
    } catch (Exception e) {
      assertTrue(e instanceof FrameworkRuntimeException);
    }

    // Test for json with payload and empty signature
    unpackedMsg.setReceivedMessage("{\"payload\":\"TempPayload\", \"signatures\":[]}");
    try {
      component.unpack(unpackedMsg);
    } catch (Exception e) {
      assertTrue(e instanceof FrameworkRuntimeException);
    }
  }

  @Test
  public void testVerify() throws Exception {
    TdiClaims claims = new TdiClaims();
    claims.exp = System.currentTimeMillis();
    claims.nbf = System.currentTimeMillis();
    claims.payload = "Payload";
    claims.iss = "Issuer";

    TdiCanonicalMessageShape message = new TdiCanonicalMessage(1);
    TdiSdkJsonWebTokenComponent jwtComponent = new TdiSdkJsonWebTokenComponent("jwt",
        TestData.getDummyImplementation());

    // Add claims to the message
    message.setClaims(claims);

    // Pack the claims.
    message = jwtComponent.packClaims(message).get();

    // Add the signers.
    message.addSigner(new TdiKeyStructure("id1", "id1", null, 0));
    message.addSigner(new TdiKeyStructure("id2", "id2", null, 0));

    // Sign the message.
    message = component.sign(message).get();

    // Create new CanonicalMessage for verifying
    TdiCanonicalMessageShape verifyMessage = new TdiCanonicalMessage(1);
    verifyMessage.setReceivedMessage(message.getBuiltMessage());

    // Unpack envelope
    verifyMessage = component.unpack(verifyMessage).get();

    // Verify message:
    verifyMessage = component.verify(verifyMessage).get();

    // Unpack claims:
    verifyMessage = jwtComponent.unpackClaims(verifyMessage).get();

    // Check the claims:
    assertEquals(claims, verifyMessage.getClaims());
  }
}
