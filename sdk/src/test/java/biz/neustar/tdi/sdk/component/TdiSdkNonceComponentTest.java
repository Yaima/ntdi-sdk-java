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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import biz.neustar.tdi.sdk.TestData;
import biz.neustar.tdi.sdk.component.TdiSdkNonceComponent;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

/**
 * Unit test cases for {@link TdiSdkNonceComponent} class.
 */
public class TdiSdkNonceComponentTest {
  static TdiSdkNonceComponent component = null;
  static final String NONCE_REGEX = "^00[12][2-9][0-9]{3}-(0[1-9]|1[0-2])-"
      + "(0[1-9]|[12][0-9]|3[01])T([01][0-9]|2[0-3])(:[0-5][0-9]){2}Z[A-Za-z0-9]{6}$";

  /**
   * Setup method.
   * @throws Exception if there is any execution exception.
   */
  @BeforeClass
  public static void setup() throws Exception {
    component = new TdiSdkNonceComponent("nonce", TestData.getDummyImplementation());
    CompletableFuture<Void> future = component.init();
    future.get();
  }

  @Test
  public void testInit() throws Exception {
    CompletableFuture<Void> future = component.init();
    future.get();
    assertTrue(true);
  }

  @Test
  public void testGetters() {
    assertEquals("nonce", component.getName());
  }

  @Test
  public void testCreate() {
    String nonce = component.create();
    assertTrue(Pattern.matches(NONCE_REGEX, nonce));
  }

  @Test
  public void testCheck() throws Exception {
    String nonce = component.create();
    assertTrue(component.check(nonce).get());

    // Invalid nonce string check
    nonce = "InvalidNonce";
    assertFalse(component.check(nonce).get());

    // Invalid version check
    nonce = "0012017-02-11T10:12:00ZfBBfgf";
    assertFalse(component.check(nonce).get());

    // NBFMinimum check
    nonce = "0022017-02-11T10:12:00ZfBBfgf";
    assertFalse(component.check(nonce).get());

    // Expiration check
    StringBuilder randomString = new StringBuilder();
    randomString.append("002");
    randomString.append(component.getPlatform().getTime().isoDate((long) 0));
    randomString.append("ffasDD");
    Thread.sleep(200);
    assertFalse(component.check(randomString.toString()).get());
  }

  @Test
  public void testBurn() throws Exception {
    String nonce = component.create();
    assertTrue(component.check(nonce).get());

    component.burn(nonce);
    assertFalse(component.check(nonce).get());

    // Burn multiple entries
    nonce = component.create();
    component.burn(nonce);

    nonce = component.create();
    component.burn(nonce);

    nonce = component.create();
    component.burn(nonce);

    nonce = component.create();
    component.burn(nonce);

    Thread.sleep(2000);

    nonce = component.create();
    component.burn(nonce);

    nonce = component.create();
    component.burn(nonce);

    nonce = component.create();
    component.burn(nonce);
  }
}
