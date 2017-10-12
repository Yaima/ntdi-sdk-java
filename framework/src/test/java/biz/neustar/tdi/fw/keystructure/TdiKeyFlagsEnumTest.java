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

import biz.neustar.tdi.fw.keystructure.TdiKeyFlagsEnum;
import org.junit.Test;

public class TdiKeyFlagsEnumTest {
  @Test
  public void testGetters() {
    assertEquals(TdiKeyFlagsEnum.CAN_SIGN,
        TdiKeyFlagsEnum.getKeyFlag(TdiKeyFlagsEnum.CAN_SIGN.getNumber()));
    assertEquals(TdiKeyFlagsEnum.CAN_SIGN, TdiKeyFlagsEnum.getKeyFlag("CAN_SIGN"));

    assertEquals(TdiKeyFlagsEnum.ROLE_UNDEF, TdiKeyFlagsEnum.getKeyFlag(15));
    assertEquals(TdiKeyFlagsEnum.ROLE_UNDEF, TdiKeyFlagsEnum.getKeyFlag("CAN_SIG"));

  }
}
