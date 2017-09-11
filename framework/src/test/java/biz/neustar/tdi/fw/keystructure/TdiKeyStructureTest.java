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

package biz.neustar.tdi.fw.keystructure;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import biz.neustar.tdi.fw.keystructure.TdiKeyFlagsEnum;
import biz.neustar.tdi.fw.keystructure.TdiKeyStructure;
import biz.neustar.tdi.fw.keystructure.TdiKeyStructureShape;
import org.junit.BeforeClass;
import org.junit.Test;

public class TdiKeyStructureTest {
  private static TdiKeyStructure goodSigner;
  private static Integer flags;

  @BeforeClass
  public static void setup() {
    flags = TdiKeyFlagsEnum.CAN_SIGN.getNumber();
    goodSigner = new TdiKeyStructure("1", "1", "TempData", flags);
  }

  @Test
  public void testKeyStructGetters() {
    assertEquals("1", goodSigner.getKeyId());
    assertEquals("1", goodSigner.getFleetId());
    assertEquals(flags, goodSigner.getFlags());
    assertEquals("TempData", goodSigner.getKeyData());

    assertTrue(goodSigner.hasFlag(TdiKeyFlagsEnum.CAN_SIGN));
    assertFalse(goodSigner.hasFlag(TdiKeyFlagsEnum.INVALID));
  }

  @Test
  public void testKeyStructSetFlags() {
    assertFalse(goodSigner.hasFlag(TdiKeyFlagsEnum.EXPIRABLE));
    goodSigner.setFlag(TdiKeyFlagsEnum.EXPIRABLE, true);
    assertTrue(goodSigner.hasFlag(TdiKeyFlagsEnum.EXPIRABLE));
    goodSigner.setFlag(TdiKeyFlagsEnum.EXPIRABLE, false);
    assertFalse(goodSigner.hasFlag(TdiKeyFlagsEnum.EXPIRABLE));
  }

  @Test
  public void testSetters() {
    // Test Can Sign
    assertTrue(goodSigner.allowedToSign());
    assertTrue(goodSigner.canSign());
    goodSigner.setCanSign(false);
    assertFalse(goodSigner.canSign());
    goodSigner.setCanSign(true);

    // Invalid test
    assertFalse(goodSigner.isInvalid());
    goodSigner.setIsInvalid(true);
    assertTrue(goodSigner.isInvalid());
    goodSigner.setIsInvalid(false);

    // Expirable
    assertFalse(goodSigner.isExpirable());
    goodSigner.setIsExpirable(true);
    assertTrue(goodSigner.isExpirable());
    goodSigner.setIsExpirable(false);

    // Revokable
    assertFalse(goodSigner.isRevokable());
    goodSigner.setIsRevokable(true);
    assertTrue(goodSigner.isRevokable());
    goodSigner.setIsRevokable(false);

    // Is Our Own
    assertFalse(goodSigner.isOurOwn());
    goodSigner.setIsOurOwn(true);
    assertTrue(goodSigner.isOurOwn());
    goodSigner.setIsOurOwn(false);

    // Origin Persistent
    assertFalse(goodSigner.isOriginPersistent());
    goodSigner.setIsOriginPersistent(true);
    assertTrue(goodSigner.isOriginPersistent());
    goodSigner.setIsOriginPersistent(false);

    // Origin Flash
    assertFalse(goodSigner.isOriginFlash());
    goodSigner.setIsOriginFlash(true);
    assertTrue(goodSigner.isOriginFlash());
    goodSigner.setIsOriginFlash(false);

    // Origin Generated
    assertFalse(goodSigner.isGenerated());
    goodSigner.setIsGenerated(true);
    assertTrue(goodSigner.isGenerated());
    goodSigner.setIsGenerated(false);

    // Origin External
    assertFalse(goodSigner.isOriginExternal());
    goodSigner.setIsOriginExternal(true);
    assertTrue(goodSigner.isOriginExternal());
    goodSigner.setIsOriginExternal(false);

    // Origin HSM
    assertFalse(goodSigner.isOriginHsm());
    goodSigner.setIsOriginHsm(true);
    assertTrue(goodSigner.isOriginHsm());
    goodSigner.setIsOriginHsm(false);

    // Origin PKI
    assertFalse(goodSigner.isOriginPki());
    goodSigner.setIsOriginPki(true);
    assertTrue(goodSigner.isOriginPki());
    goodSigner.setIsOriginPki(false);

    // Public key test
    String publicKey = "TempPublicKey";
    goodSigner.setPublicKey(publicKey);
    assertEquals(publicKey, goodSigner.getPublicKey());

    // Role flag test
    assertFalse(goodSigner.getRoleFlag() == TdiKeyFlagsEnum.ROLE_F_S.getNumber());
    goodSigner.setRoleFlag(TdiKeyFlagsEnum.ROLE_F_S.getNumber());
    assertTrue(goodSigner.getRoleFlag() == TdiKeyFlagsEnum.ROLE_F_S.getNumber());
  }

  @Test
  public void testExport() {
    TdiKeyStructureShape exported = goodSigner.export();
    assertFalse(exported.canSign());
  }
}
