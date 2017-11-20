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

/**
 * Class for all pre-defined api flows.
 *
 */
public class ApiFlows {
  public static class SignFlow {
    /** Create Canonical message and populate payload claim. */
    public static final String handleInit   = "handleInit";
    /** Optional secondary parsing step for inbound messages. */
    public static final String parseRaw     = "parseRaw";
    /** Loads values in to canonical "claims" key value pairs. */
    public static final String setClaims    = "setClaims";
    /** Populates canonical "rawPayload" field with serialized claims. */
    public static final String packClaims   = "packClaims";
    /** Assign "kid" strings to canonical "signers" array. */
    public static final String setSigners   = "setSigners";
    /** Signs the "rawPayload" with the keys from "signers". */
    public static final String sign         = "sign";
    /** Handles the canonical "builtMessage" field for final return. */
    public static final String handleReturn = "handleReturn";
  }

  public class VerifyFlow {
    /** Create Canonical message and populate the "receivedMessage" field. */
    public static final String handleInit       = "handleInit";
    /** Break "receivedMessage" into its components (header/payload/sigs). */
    public static final String unpackEnvelope   = "unpackEnvelope";
    /** Parse "rawClaims" in to "claims" key/values. */
    public static final String unpackClaims     = "unpackClaims";
    /** Validate all "claims" properties. */
    public static final String validateClaims   = "validateClaims";
    /** Manipulate and/or validate "signaturesToVerify". */
    public static final String prepSignatures   = "prepSignatures";
    /** Validate signatures to verify relative to "rawPayload". */
    public static final String verifySignatures = "verifySignatures";
    /** Run any necessary cleanup or post-validation steps. */
    public static final String afterVerify      = "afterVerify";
    /** Returns the authenticated message. */
    public static final String handleReturn     = "handleReturn";
  }

  public class CosignFlow {
    /** Create Canonical message &amp; populate payload claim and signatures. */
    public static final String handleInit   = "handleInit";
    /** assign "kid" strings to canonical "signers" array. */
    public static final String setSigners   = "setSigners";
    /** Co-signs the message. */
    public static final String sign         = "sign";
    /** Update the built message with heldSignatures. */
    public static final String handleReturn = "handleReturn";
  }

  public class VerifyGeneralFlow {
    /** Create Canonical message and populate the "receivedMessage" field. */
    public static final String handleInit       = "handleInit";
    /** Break "receivedMessage" into its components (header/payload/sigs). */
    public static final String unpackEnvelope   = "unpackEnvelope";
    /** Parse "rawClaims" in to "claims" key/values. */
    public static final String unpackClaims     = "unpackClaims";
    /** Validate all "claims" properties. */
    public static final String validateClaims   = "validateClaims";
    /** Manipulate and/or validate "signaturesToVerify". */
    public static final String prepSignatures   = "prepSignatures";
    /** Validate signatures to verify relative to "rawPayload". */
    public static final String verifySignatures = "verifySignatures";
    /** Run any necessary cleanup or post-validation steps. */
    public static final String afterVerify      = "afterVerify";
    /** Returns the authenticated message. */
    public static final String handleReturn     = "handleReturn";
  }
}
