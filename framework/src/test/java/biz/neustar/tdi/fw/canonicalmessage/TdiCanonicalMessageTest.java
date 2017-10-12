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

package biz.neustar.tdi.fw.canonicalmessage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import biz.neustar.tdi.fw.Constants.TdiClaimKeys;
import biz.neustar.tdi.fw.keystructure.TdiKeyStructure;
import biz.neustar.tdi.fw.keystructure.TdiKeyStructureShape;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class TdiCanonicalMessageTest {
  private static TdiCanonicalMessage msgObj;
  private static Integer msgId;

  @BeforeClass
  public static void setup() {
    msgId = new Random().nextInt();
    msgObj = new TdiCanonicalMessage(msgId);
  }

  @Test
  public void testId() {
    assertEquals(msgId, msgObj.getId());
  }

  @Test
  public void testRawPayload() {
    String payload = "Test Payload";

    msgObj.setRawPayload(payload);
    assertTrue(payload.equals(msgObj.getRawPayload()));
  }

  @Test
  public void testClaims() {
    Map<String, Object> claimsMap = new HashMap<>();
    claimsMap.put("Key1", "Value1");
    claimsMap.put("Key2", "Value2");

    msgObj.addClaims(claimsMap);

    assertTrue(claimsMap.keySet().containsAll(msgObj.getClaims().getClaimsMap().keySet()));

    msgObj.addClaim("Key3", "Value3");

    Set<String> claimsKeySet = new HashSet<>();
    claimsKeySet.addAll(claimsMap.keySet());
    claimsKeySet.add("Key3");
    assertTrue(claimsKeySet.containsAll(msgObj.getClaims().getClaimsMap().keySet()));
    
    msgObj.addClaim(TdiClaimKeys.ISS, "Issuer");
    assertEquals("Issuer", msgObj.getClaims().iss);

    msgObj.addClaim(TdiClaimKeys.EXP, 10000);
    assertTrue(msgObj.getClaims().exp.longValue() == 10000);

    msgObj.addClaim(TdiClaimKeys.NBF, 10000);
    assertTrue(msgObj.getClaims().nbf.longValue() == 10000);

    msgObj.addClaim(TdiClaimKeys.JTI, "NonceStr");
    assertEquals("NonceStr", msgObj.getClaims().jti);

    msgObj.addClaim(TdiClaimKeys.PAYLOAD, "Payload");
    assertEquals("Payload", msgObj.getClaims().payload);
    
    msgObj.addClaims(null);
  }

  @Test
  public void testSetClaims() {
    TdiClaims claims = new TdiClaims();
    claims.payload = "Payload";
    
    msgObj.setClaims(null);
    assertNull(msgObj.getClaims());
    
    msgObj.setClaims(claims);
    assertNotNull(msgObj.getClaims());
    assertEquals("Payload", msgObj.getClaims().payload);
  }
  
  @Test
  public void testSignatureType() {
    String msgType = "JOSE+JSON";

    msgObj.setSignatureType(msgType);

    assertEquals(msgType, msgObj.getSignatureType());
  }

  @Test
  public void testCurrentProject() {
    String currentProject = "project1";

    msgObj.setCurrentProject(currentProject);

    assertEquals(currentProject, msgObj.getCurrentProject());
  }

  @Test
  public void testSigners() {
    TdiKeyStructure signer1 = new TdiKeyStructure("1", "1", null, 2);
    TdiKeyStructure signer2 = new TdiKeyStructure("2", "2", null, 2);

    msgObj.addSigner(signer1);
    msgObj.addSigner(signer2);

    assertTrue(msgObj.getSigners().size() == 2);

    int count = 0;
    for (TdiKeyStructureShape signer : msgObj.getSigners()) {
      if (signer.getKeyId().equals(signer1.getKeyId())
          || signer.getKeyId().equals(signer2.getKeyId())) {
        count++;
      }
    }

    assertTrue(count == 2);
  }

  @Test
  public void testVerifiers() {
    TdiKeyStructure verifier1 = new TdiKeyStructure("1", "1", null, 2);
    TdiKeyStructure verifier2 = new TdiKeyStructure("2", "2", null, 2);

    msgObj.addVerifier(verifier1);
    msgObj.addVerifier(verifier2);

    assertTrue(msgObj.getVerifiers().size() == 2);

    int count = 0;
    for (TdiKeyStructureShape signer : msgObj.getVerifiers()) {
      if (signer.getKeyId().equals(verifier1.getKeyId())
          || signer.getKeyId().equals(verifier2.getKeyId())) {
        count++;
      }
    }

    assertTrue(count == 2);
  }
  
  @Test
  public void testBuiltMessage() {
    String builtMessage = "TestString";
    
    msgObj.setBuiltMessage(builtMessage);
    assertEquals(builtMessage, msgObj.getBuiltMessage());
  }
  
  @Test
  public void testReceivedMessage() {
    String receivedMessage = "TestString";
    
    msgObj.setReceivedMessage(receivedMessage);
    assertEquals(receivedMessage, msgObj.getReceivedMessage());
  }
  
  @Test
  public void testHeldSignatures() {
    List<Object> signatures = new ArrayList<>();
    Map<String, Object> map1 = new HashMap<>();
    map1.put("TestKey1", "TestValue1");
    signatures.add(map1);
    
    Map<String, Object> map2 = new HashMap<>();
    map2.put("TestKey2", "TestValue2");
    signatures.add(map2);
    
    msgObj.addHeldSignatures(signatures);
    assertEquals(signatures, msgObj.getHeldSignatures());
    
    Map<String, Object> map3 = new HashMap<>();
    map3.put("TestKey3", "TestValue3");
    msgObj.addHeldSignature(map3);
    signatures.add(map3);
    
    assertEquals(signatures, msgObj.getHeldSignatures());
  }
  
  @Test
  public void testSignaturesToVerify() {
    List<Object> signatures = new ArrayList<>();
    Map<String, Object> map1 = new HashMap<>();
    map1.put("TestKey1", "TestValue1");
    signatures.add(map1);
    
    Map<String, Object> map2 = new HashMap<>();
    map2.put("TestKey2", "TestValue2");
    signatures.add(map2);
    
    msgObj.addSignaturesToVerify(signatures);
    assertEquals(signatures, msgObj.getSignaturesToVerify());
    
    Map<String, Object> map3 = new HashMap<>();
    map3.put("TestKey3", "TestValue3");
    msgObj.addSignatureToVerify(map3);
    
    signatures.add(map3);
    assertEquals(signatures, msgObj.getSignaturesToVerify());
  }
}
