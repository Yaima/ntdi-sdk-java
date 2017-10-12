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
import static org.junit.Assert.assertTrue;

import biz.neustar.tdi.platform.Utils;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * JUnit test class for Utils class.
 *
 */
public class UtilsTest {

  /**
   * UtilsTest Junit Setup method.
   * 
   * @throws Exception
   * 
   */
  @BeforeClass
  public static void setup() throws Exception {
    
  }
  
  @Test
  public void testToStringData() {
    String data = "Test String";
    String strData = Utils.toStringData(data);
    assertNotNull(strData);
    assertTrue(strData.equals(data));
  }
  
  @Test(expected = NullPointerException.class)
  public void testToStringDataWithNullData() {
    
    Object data = null;
    String strData = Utils.toStringData(data);
    assertNotNull(strData);
  }
  
}
