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
import static org.junit.Assert.assertNull;

import biz.neustar.tdi.fw.canonicalmessage.TdiCanonicalMessage;
import biz.neustar.tdi.fw.canonicalmessage.TdiCanonicalMessageShape;
import biz.neustar.tdi.fw.canonicalmessage.TdiClaims;
import biz.neustar.tdi.fw.utils.Utils;
import biz.neustar.tdi.sdk.TestData;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.ExecutionException;

/**
 * Unit test cases for {@link TdiSdkJsonWebTokenComponent} class.
 */
public class TdiSdkJsonWebTokenComponentTest {
  private static TdiSdkJsonWebTokenComponent component = null;
  private static TdiClaims claims = null;

  /**
   * Setup method.
   */
  @BeforeClass
  public static void setup() {
    component = new TdiSdkJsonWebTokenComponent("jwt", TestData.getDummyImplementation());
    claims = new TdiClaims();
    claims.exp = System.currentTimeMillis();
    claims.nbf = System.currentTimeMillis();
    claims.payload = "Payload";
    claims.iss = "Issuer";
  }

  @Test
  public void testInit() throws Exception {
    assertNull(component.init().get());
  }

  @Test
  public void testPackClaims() throws Exception {
    TdiCanonicalMessageShape message = new TdiCanonicalMessage(1);
    message.setClaims(claims);

    String expectedJson = TestData.getDummyUtils()
        .b64UrlEncode(Utils.objectToJson(claims));
    assertEquals(expectedJson, component.packClaims(message).get().getRawPayload());
  }

  @Test
  public void testUnpackClaims() throws Exception {
    TdiCanonicalMessageShape message = new TdiCanonicalMessage(1);
    message.setClaims(claims);

    TdiCanonicalMessageShape unpackedMessage = component
        .unpackClaims(component.packClaims(message).get()).get();
    assertEquals(claims, unpackedMessage.getClaims());
  }

  @Test(expected = ExecutionException.class)
  public void testUnpackClaimsWithException() throws Exception {
    String badJsonBase64Encoded = TestData.getDummyUtils()
        .b64UrlEncode("{\"bad/\bKey\":\"bad\"Value\"}");
    TdiCanonicalMessageShape message = new TdiCanonicalMessage(1);
    ((TdiCanonicalMessage) message).setRawPayload(badJsonBase64Encoded);

    component.unpackClaims(message).get();
  }
}
