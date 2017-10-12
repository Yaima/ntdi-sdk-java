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
import biz.neustar.tdi.fw.canonicalmessage.TdiCanonicalMessageShape;
import biz.neustar.tdi.fw.wrapper.TdiSdkWrapperShape;
import biz.neustar.tdi.sdk.Constants.Api;
import biz.neustar.tdi.sdk.TdiSdk;
import biz.neustar.tdi.sdk.TestData;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

/**
 * Test class to cover negative scenarios of Verify flow.
 *
 */
public class VerifyTest {

  private static CompletableFuture<TdiSdkWrapperShape> defaultSdkWrapper;
  private static Verify verify;

  /**
   * Sets up the test data.
   * 
   * @throws Exception
   *           if there is any execution exception.
   */
  @BeforeClass
  public static void setup() throws Exception {

    TdiSdk defaultSdk = new TdiSdk(TestData.getDummyPlatformOptions());
    defaultSdkWrapper = defaultSdk.init();

    verify = new Verify(defaultSdkWrapper.get().getImpl());
  }

  @Test(expected = ExecutionException.class)
  public void testValidateClaims_iss() throws Exception {
    TdiCanonicalMessage tdiMsg = new TdiCanonicalMessage(1);
    verify.validateClaims(tdiMsg).get();
  }

  @Test(expected = ExecutionException.class)
  public void testValidateClaims_exp() throws Exception {
    TdiCanonicalMessage tdiMsg = new TdiCanonicalMessage(1);
    tdiMsg.getClaims().iss = "iss";
    tdiMsg.getClaims().exp = System.currentTimeMillis() / 1000;
    verify.validateClaims(tdiMsg).get();
  }

  @Test(expected = ExecutionException.class)
  public void testValidateClaims_nbf() throws Exception {
    TdiCanonicalMessage tdiMsg = new TdiCanonicalMessage(1);
    tdiMsg.getClaims().iss = "iss";
    tdiMsg.getClaims().exp = System.currentTimeMillis();
    tdiMsg.getClaims().nbf = System.currentTimeMillis();
    verify.validateClaims(tdiMsg).get();
  }

  @Test(expected = ExecutionException.class)
  public void testValidateClaims_jti() throws Exception {

    TdiCanonicalMessage tdiMsg = new TdiCanonicalMessage(1);
    tdiMsg.getClaims().iss = "iss";
    tdiMsg.getClaims().exp = System.currentTimeMillis();
    tdiMsg.getClaims().nbf = (System.currentTimeMillis() / 1000) - 10;
    tdiMsg.getClaims().jti = "bad nonce";
    verify.validateClaims(tdiMsg).get();
  }

  @Test
  public void testPrepSignatures1() throws Exception {
    TdiCanonicalMessage msg = TestData.getSignedMsg();
    CompletableFuture<TdiCanonicalMessageShape> initMsg = verify.handleInit(msg.getBuiltMessage());
    CompletableFuture<TdiCanonicalMessageShape> unpackedMsg = verify.unpackEnvelope(initMsg.get());
    verify.prepSignatures(unpackedMsg.get());
  }

  @Test(expected = ExecutionException.class)
  public void testPrepSignatures_KidNotInKeystore() throws Exception {
    // get a signed message
    TdiCanonicalMessage msg = TestData.getSignedMsg();

    // get a Verify instance
    TdiSdk defaultSdk1 = new TdiSdk(TestData.getKidErrorPlatformSdkOptions());
    CompletableFuture<TdiSdkWrapperShape> defaultSdkWrapper1 = defaultSdk1.init();
    Verify verify1 = new Verify(defaultSdkWrapper1.get().getImpl());

    // run it through the steps so that the 'signaturesToVerify' are filled in
    // the message
    CompletableFuture<TdiCanonicalMessageShape> initMsg = verify1.handleInit(msg.getBuiltMessage());
    CompletableFuture<TdiCanonicalMessageShape> unpackedMsg = verify1.unpackEnvelope(initMsg.get());

    // method to test
    verify1.prepSignatures(unpackedMsg.get()).get();
  }

  @Test(expected = ExecutionException.class)
  public void testPrepSignatures_MissingRoleFc() throws Exception {
    // get a signed message
    TdiCanonicalMessage msg = TestData.getSignedMsg();

    // run it through the steps so that the 'signaturesToVerify' are filled in
    // the message
    CompletableFuture<TdiCanonicalMessageShape> initMsg = verify.handleInit(msg.getBuiltMessage());
    CompletableFuture<TdiCanonicalMessageShape> unpackedMsg = verify.unpackEnvelope(initMsg.get());

    // method to test
    verify.prepSignatures(unpackedMsg.get()).get();
  }

  @Test(expected = ExecutionException.class)
  public void testPrepSignatures_MissingRoleFs() throws Exception {
    // get a signed message
    CompletableFuture<TdiCanonicalMessage> signResult = defaultSdkWrapper
        .thenCompose((sdkWrapper) -> {
          Function<String, CompletableFuture<TdiCanonicalMessage>> signFunction = sdkWrapper
              .api(Api.SignFlow.name());

          return signFunction.apply(TestData.PAYLOAD_VALUE);
        });
    TdiCanonicalMessage signedMsg = (TdiCanonicalMessage) signResult.get();

    TdiCanonicalMessage cosignedMsg = TestData.getCosignedMsg(signedMsg.getBuiltMessage());

    // run it through the steps so that the 'signaturesToVerify' are filled in
    // the message
    CompletableFuture<TdiCanonicalMessageShape> initMsg = verify
        .handleInit(cosignedMsg.getBuiltMessage());
    CompletableFuture<TdiCanonicalMessageShape> unpackedMsg = verify.unpackEnvelope(initMsg.get());

    // method to test
    verify.prepSignatures(unpackedMsg.get()).get();
  }
}
