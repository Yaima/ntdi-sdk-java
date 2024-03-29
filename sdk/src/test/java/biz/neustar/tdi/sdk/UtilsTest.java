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

package biz.neustar.tdi.sdk;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Test class for {@link Utils}.
 */
public class UtilsTest {
  @Test
  public void testGetLong() {
    int integer = 10; 
    Long longValue = 100L;
    assertEquals(Integer.toUnsignedLong(10), Utils.getLong(integer).longValue());
    assertEquals(longValue.longValue(), Utils.getLong(longValue).longValue());
    assertEquals(0L, Utils.getLong(null).longValue());
  }
}
