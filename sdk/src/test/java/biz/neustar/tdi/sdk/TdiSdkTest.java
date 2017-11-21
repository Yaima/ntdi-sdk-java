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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import biz.neustar.tdi.fw.canonicalmessage.TdiCanonicalMessage;
import biz.neustar.tdi.fw.exception.FrameworkRuntimeException;
import biz.neustar.tdi.fw.wrapper.TdiSdkWrapperShape;
import biz.neustar.tdi.sdk.Constants.Api;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * Test class for {@link TdiSdk}.
 *
 */
public class TdiSdkTest {

  private static CompletableFuture<TdiSdkWrapperShape> verifySdkWrapper;
  private static CompletableFuture<TdiSdkWrapperShape> signSdkWrapper;
  private static CompletableFuture<TdiSdkWrapperShape> cosignSdkWrapper;

  /**
   * Sets up the test data.
   */
  @BeforeClass
  public static void setup() {

    /*
     * Ideally the below three instances would be either on 3 different systems
     * or as 3 different processes.
     */

    /*
     * Verification sdk instance
     */
    TdiSdkOptions sdkOptions = TestData.getDummyPlatformOptions();
    sdkOptions.exposeImpl = false;
    TdiSdk defaultSdk = new TdiSdk(sdkOptions);
    verifySdkWrapper = defaultSdk.init();

    /*
     * Signing sdk instance
     */
    TdiSdkOptions signSdkOptions = TestData.getSignPlatformSdkOptions();
    TdiSdk signSdk = new TdiSdk(signSdkOptions);
    signSdkWrapper = signSdk.init();

    /*
     * Co-signing instance
     */
    TdiSdkOptions cosignSdkOptions = TestData.getCosignPlatformSdkOptions();
    TdiSdk cosignSdk = new TdiSdk(cosignSdkOptions);
    cosignSdkWrapper = cosignSdk.init();
  }

  @Test
  public void testApis() throws Exception {

    // sign the payload
    CompletableFuture<TdiCanonicalMessage> signedMessage = signSdkWrapper
        .thenCompose((signWrapper) -> {
          Function<String, CompletableFuture<TdiCanonicalMessage>> signApi = signWrapper
              .api(Api.SignFlow);
          CompletableFuture<TdiCanonicalMessage> signResult = signApi
              .apply(TestData.PAYLOAD_VALUE);
          return signResult;
        });
    TdiCanonicalMessage signedMsg = signedMessage.get();

    assertEquals(TestData.PAYLOAD_VALUE, signedMsg.getClaims().payload);

    // cosign the payload
    CompletableFuture<TdiCanonicalMessage> cosignedMessage = cosignSdkWrapper
        .thenCompose((cosignWrapper) -> {
          Function<String, CompletableFuture<TdiCanonicalMessage>> cosignApi = cosignWrapper
              .api(Api.CosignFlow);
          CompletableFuture<TdiCanonicalMessage> cosignResult = cosignApi
              .apply(signedMsg.getBuiltMessage());
          return cosignResult;
        });

    TdiCanonicalMessage cosignedMsg = cosignedMessage.get();

    // verify the payload
    CompletableFuture<String> result = verifySdkWrapper.thenCompose((verifyWrapper) -> {
      Function<String, CompletableFuture<String>> verifyApi = verifyWrapper
          .api(Api.VerifyFlow);
      CompletableFuture<String> verifyResult = verifyApi.apply(cosignedMsg.getBuiltMessage());
      return verifyResult;
    });

    assertEquals(TestData.PAYLOAD_VALUE, result.get());
  }

  @Test(expected = FrameworkRuntimeException.class)
  public void testValidateConfig() throws Exception {
    TdiSdkOptions sdkOptions = new TdiSdkOptions();
    sdkOptions.platform = TestData.DummyPlatform::new;

    new TdiSdk(sdkOptions);
  }

  @Test(expected = FrameworkRuntimeException.class)
  public void testValidatePlatform() throws Exception {
    TdiSdkOptions sdkOptions = new TdiSdkOptions();
    sdkOptions.config = TestData.getTestConfig();
    new TdiSdk(sdkOptions);
  }

  @Test(expected = FrameworkRuntimeException.class)
  public void testValidateOptions() throws Exception {
    new TdiSdk(null);
  }

  @Test
  public void testExposeImpl() throws Exception {

    // the default sdkWrapper (in setup) has exposeImpl flag as false
    assertNull(verifySdkWrapper.get().getImpl());
    TdiSdkOptions sdkOptions = TestData.getDummyPlatformOptions();
    sdkOptions.exposeImpl = true;

    TdiSdk defaultSdk1 = new TdiSdk(sdkOptions);
    CompletableFuture<TdiSdkWrapperShape> defaultWrapper = defaultSdk1.init();

    assertNotNull(defaultWrapper.get().getImpl());

  }

}
