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

/**
 * Sdk Constants.
 */
public class Constants {

  /**
   * API flows that we support out-of-box.
   */
  public class Api {
    public static final String SignFlow          = "SignFlow";
    public static final String VerifyFlow        = "VerifyFlow";
    public static final String CosignFlow        = "CosignFlow";
    public static final String VerifyGeneralFlow = "VerifyGeneralFlow";
  }

  /**
   * Class for all pre-defined api flows.
   * No order is implied by these definitions. Composition of these named flow
   *   methods is handled in {@link TdiSdkFactory}.
   */
  public class FlowMethods {
    public static final String handleInit   = "handleInit";
    public static final String handleReturn = "handleReturn";

    /** Methods specific to signing. */
    public class Signing {
      public static final String parseRaw   = "parseRaw";
      public static final String setClaims  = "setClaims";
      public static final String packClaims = "packClaims";
      public static final String sign       = "sign";
      public static final String setSigners = "setSigners";
    }

    /** Methods specific to verifying. */
    public class Verifying {
      public static final String unpackEnvelope   = "unpackEnvelope";
      public static final String unpackClaims     = "unpackClaims";
      public static final String validateClaims   = "validateClaims";
      public static final String prepSignatures   = "prepSignatures";
      public static final String verifySignatures = "verifySignatures";
      public static final String afterVerify      = "afterVerify";
    }
  }

  public static class Components {
    public static final String JWS = "jws";
    public static final String JWT = "jwt";
    public static final String EXP = "exp";
    public static final String NBF = "nbf";
    public static final String NONCE = "nonce";
  }

  public static class DefaultJws {
    public static final String type = "JOSE+JSON";
    public static final String alg = "ES256";
  }

  public static class JwsKeys {
    public static final String TYPE = "typ";
    public static final String ALG = "alg";
    public static final String KID = "kid";
    public static final String PROTECTED = "protected";
    public static final String SIGNATURE = "signature";
    public static final String PAYLOAD = "payload";
    public static final String SIGNATURES = "signatures";

    public static final String PARSED_HEADER = "parsedHeader";
  }

  public static class NonceConfig {
    public static final String STORE = "nonce002";
    public static final String EXP_DURATION = "expDuration";
    public static final String NBF_MINIMUM = "nbfMinimum";
    public static final String BURNT = "burnt";
    public static final String VERSION = "002";
  }
}
