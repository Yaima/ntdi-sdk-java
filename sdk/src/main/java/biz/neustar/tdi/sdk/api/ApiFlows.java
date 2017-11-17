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

  public enum SignFlow {

    /** Create Canonical message and populate payload claim. */
    handleInit,
    /**
     * Unused at the moment. Meant to do more parsing of the passed in data.
     */
    parseRaw,
    /** Loads values in to canonical "claims" key value pairs. */
    setClaims,
    /** Populates canonical "rawPayload" field with serialized claims. */
    packClaims,
    /** Assign "kid" strings to canonical "signers" array . */
    setSigners,
    /** Signs the "rawPayload" with the keys from "signers". */
    sign,
    /** Handles the canonical "builtMessage" field for final return. */
    handleReturn
  }

  public static enum VerifyFlow {

    /**
     * Create Canonical message and populate canonical "receivedMessage" field.
     */
    handleInit,
    /**
     * Break the "receivedMessage" in to "rawPayload" and populate
     * "signaturesToVerify" array.
     */
    unpackEnvelope,
    /** Parse "rawClaims" in to "claims" key/values. */
    unpackClaims,
    /** Validate all "claims" properties. */
    validateClaims,
    /** Manipulate and/or validate "signaturesToVerify". */
    prepSignatures,
    /** Validate signatures to verify relative to "rawPayload". */
    verifySignatures,
    /** Run any necessary cleanup or post-validation steps. */
    afterVerify,
    /** Returns the authenticated message. */
    handleReturn
  }

  public static enum CosignFlow {

    /** Create Canonical message &amp; populate payload claim and signatures. */
    handleInit,
    /** assign "kid" strings to canonical "signers" array. */
    setSigners,
    /** Co-signs the message. */
    sign,
    /** Update the built message with heldSignatures. */
    handleReturn
  }

  public static enum VerifyGeneralFlow {
    /**
     * Create Canonical message and populate canonical "receivedMessage" field.
     */
    handleInit,
    /**
     * Break the "receivedMessage" in to "rawPayload" and populate
     * "signaturesToVerify" array.
     */
    unpackEnvelope,
    /** Parse "rawClaims" in to "claims" key/values. */
    unpackClaims,
    /** Validate all "claims" properties. */
    validateClaims,
    /** Manipulate and/or validate "signaturesToVerify". */
    prepSignatures,
    /** Validate signatures to verify relative to "rawPayload". */
    verifySignatures,
    /** Run any necessary cleanup or post-validation steps. */
    afterVerify,
    /** Returns the authenticated message. */
    handleReturn

  }

}
