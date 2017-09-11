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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import biz.neustar.tdi.fw.exception.InvalidFormatException;
import biz.neustar.tdi.fw.utils.Utils;
import org.junit.Test;

/**
 * Unit test case for {@link TdiClaims} class.
 */
public class TdiClaimsTest {
  @Test
  public void testJsonToClaims() throws InvalidFormatException {
    String jsonString = "{\"iss\":\"Issuer\",\"nbf\":1504258092,\"exp\":1504258110}";
    TdiClaims claims = Utils.jsonToObject(jsonString, TdiClaims.class);

    assertEquals("Issuer", claims.iss);
    assertTrue(claims.nbf.longValue() == 1504258092);
    assertTrue(claims.exp.longValue() == 1504258110);
    assertEquals(0, claims.getClaimsMap().size());
  }

  @Test
  public void testJsonToClaimsOtherKeys() throws InvalidFormatException {
    String jsonString = "{\"claimKey\":\"claimValue\",\"claimKey2\":10000}";
    TdiClaims claims = Utils.jsonToObject(jsonString, TdiClaims.class);

    assertEquals(2, claims.getClaimsMap().size());
    assertEquals("claimValue", claims.getClaimsMap().get("claimKey"));
    assertEquals(10000, claims.getClaimsMap().get("claimKey2"));
  }

  @Test(expected = InvalidFormatException.class)
  public void testJsonToClaimsEmptyJson() throws InvalidFormatException {
    Utils.jsonToObject("", TdiClaims.class);
  }
  
  @Test(expected = InvalidFormatException.class)
  public void testJsonToClaimsInvalidClass() throws InvalidFormatException {
    String jsonString = "{\"iss\":\"Issuer\",\"nbf\":1504258092,\"exp\":1504258110}";
    Utils.jsonToObject(jsonString, TdiCanonicalMessage.class);
  }
  
  @Test
  public void testClaimsToJson() throws InvalidFormatException {
    TdiClaims claims = new TdiClaims();
    claims.iss = "Issuer";
    claims.exp = (long) 1504258110;
    claims.nbf = (long) 1504258092;
    
    String expJsonString = "{\"iss\":\"Issuer\",\"exp\":1504258110,\"nbf\":1504258092}";
    String generatedJsonString = Utils.objectToJson(claims);
    
    assertEquals(expJsonString, generatedJsonString);
  }
  
  @Test
  public void testClaimsToJsonOtherKeys() throws InvalidFormatException {
    TdiClaims claims = new TdiClaims();
    claims.addToClaimsMap("claimKey", "claimValue");
    claims.addToClaimsMap("claimKey2", 10000);
    
    String expJsonString = "{\"claimKey\":\"claimValue\",\"claimKey2\":10000}";
    String generatedJsonString = Utils.objectToJson(claims);
    
    assertEquals(expJsonString, generatedJsonString);
  }
  
  @Test
  public void testClaimsEquals() throws InvalidFormatException {
    /*String jsonString = "{\"iss\":\"Issuer\",\"nbf\":1504258092,\"exp\":1504258110}";
    TdiClaims claims = Utils.jsonToObject(jsonString, TdiClaims.class);
    TdiClaims otherClaims = Utils.jsonToObject(jsonString, TdiClaims.class);*/
    TdiClaims claims = new TdiClaims();
    TdiClaims otherClaims = new TdiClaims();

    //Test same object equals
    assertEquals(claims, claims);
    
    //Test difference in iss
    claims.iss = "Issuer";
    assertFalse(claims.equals(otherClaims));
    assertFalse(otherClaims.equals(claims));
    otherClaims.iss = "DiffIssuer";
    assertFalse(claims.equals(otherClaims));
    otherClaims.iss = "Issuer";
    assertTrue(claims.equals(otherClaims));

    //Test difference in exp
    claims.exp = System.currentTimeMillis();
    assertFalse(claims.equals(otherClaims));
    assertFalse(otherClaims.equals(claims));
    otherClaims.exp = System.currentTimeMillis() + 100;
    assertFalse(claims.equals(otherClaims));
    otherClaims.exp = claims.exp;
    assertTrue(claims.equals(otherClaims));

    //Test difference in nbf
    claims.nbf = System.currentTimeMillis();
    assertFalse(claims.equals(otherClaims));
    assertFalse(otherClaims.equals(claims));
    otherClaims.nbf = System.currentTimeMillis() + 100;
    assertFalse(claims.equals(otherClaims));
    otherClaims.nbf = claims.nbf;
    assertTrue(claims.equals(otherClaims));

    //Test difference in payload
    claims.payload = "Payload";
    assertFalse(claims.equals(otherClaims));
    assertFalse(otherClaims.equals(claims));
    otherClaims.payload = "DiffPayload";
    assertFalse(claims.equals(otherClaims));
    otherClaims.payload = "Payload";
    assertTrue(claims.equals(otherClaims));

    //Test difference in payload
    claims.jti = "Nonce";
    assertFalse(claims.equals(otherClaims));
    assertFalse(otherClaims.equals(claims));
    otherClaims.jti = "DiffNonce";
    assertFalse(claims.equals(otherClaims));
    otherClaims.jti = "Nonce";
    assertTrue(claims.equals(otherClaims));

    //Test difference in claimsMap
    claims.addToClaimsMap("key1", "value1");
    assertFalse(claims.equals(otherClaims));
    assertFalse(otherClaims.equals(claims));
    otherClaims.addToClaimsMap("key2", "value2");
    assertFalse(claims.equals(otherClaims));
    otherClaims.getClaimsMap().remove("key2");
    otherClaims.addToClaimsMap("key1", "value1");
    assertTrue(claims.equals(otherClaims));

    /*//Test difference in exp
    claims.exp = System.currentTimeMillis();
    assertFalse(claims.equals(otherClaims));
    otherClaims.exp = claims.exp;
    assertTrue(claims.equals(otherClaims));
    
    //Test difference in nbf
    claims.nbf = System.currentTimeMillis();
    assertFalse(claims.equals(otherClaims));
    otherClaims.nbf = claims.nbf;
    assertTrue(claims.equals(otherClaims));
    
    //Test difference in payload
    claims.payload = "Payload";
    assertFalse(claims.equals(otherClaims));
    otherClaims.payload = claims.payload;
    assertTrue(claims.equals(otherClaims));

    //Test difference in claimsMap
    claims.addToClaimsMap("key1", "value1");
    assertFalse(claims.equals(otherClaims));
    otherClaims.addToClaimsMap("key1", "value1");
    assertTrue(claims.equals(otherClaims));*/
  }
}
