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

package biz.neustar.tdi.sdk.api;

import biz.neustar.tdi.fw.canonicalmessage.TdiCanonicalMessage;
import biz.neustar.tdi.sdk.TestData;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.ExecutionException;

/**
 * Test class to cover negative scenarios of Sign flow.
 *
 */
public class SignTest {

  private static Sign sign;

  /**
   * Sets up the test data.
   */
  @BeforeClass
  public static void setup() {

    sign = new Sign(TestData.getDummyImplementation());
  }

  @Test(expected = ExecutionException.class)
  public void testHandleReturn_BlankBuiltMessage() throws Exception {
    TdiCanonicalMessage msg = new TdiCanonicalMessage(1);
    sign.handleReturn(msg).get();
  }

  @Test(expected = ExecutionException.class)
  public void testSetSigners() throws Exception {
    TdiCanonicalMessage msg = new TdiCanonicalMessage(1);
    Sign sign1 = new Sign(TestData.getDummyImplementationWithKeyError());
    sign1.setSigners(msg).get();
  }
}
