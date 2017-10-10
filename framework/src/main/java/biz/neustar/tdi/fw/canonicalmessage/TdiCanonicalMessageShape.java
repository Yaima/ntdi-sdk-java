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

package biz.neustar.tdi.fw.canonicalmessage;

import biz.neustar.tdi.fw.keystructure.TdiKeyStructureShape;

import java.util.List;
import java.util.Map;

/**
 * Base format of the message that would be transferred internally in the
 * framework, library, and plugins. This independent of format that is sent over
 * the wire.
 */
public interface TdiCanonicalMessageShape {
  /**
   * Returns the ID associated with this message.
   * 
   * @return Integer id of the message.
   */
  public Integer getId();

  /**
   * Claims holding the source data and additional details required for
   * validating the message.
   * 
   * @return {@link TdiClaims}
   */
  public TdiClaims getClaims();

  /**
   * Returns the list of {@link TdiKeyStructureShape} objects that would be
   * required to sign an out-bound message.
   * 
   * @return List&lt;{@link TdiKeyStructureShape}&gt;
   */
  public List<TdiKeyStructureShape> getSigners();

  /**
   * Adds a {@link TdiKeyStructureShape} signer to the message object that would
   * be required to sign.
   * 
   * @param signer
   *          : {@link TdiKeyStructureShape}
   */
  void addSigner(TdiKeyStructureShape signer);

  /**
   * Returns the list of {@link TdiKeyStructureShape} objects that would be
   * required to verify an in-bound message.
   * 
   * @return List&lt;{@link TdiKeyStructureShape}&gt;
   */
  public List<TdiKeyStructureShape> getVerifiers();

  /**
   * Adds the {@link TdiKeyStructureShape} verifier to the message object that
   * would be required to verify.
   * 
   * @param verifier
   *          : {@link TdiKeyStructureShape}
   */
  void addVerifier(TdiKeyStructureShape verifier);

  /**
   * Return the fleet ID to which this message belongs.
   * 
   * @return String ID of the fleet/project.
   */
  public String getCurrentProject();

  /**
   * Sets the current project
   * 
   * @param currentProject
   *          : ID of the project this message is associated with.
   */
  public void setCurrentProject(String currentProject);
  
  /**
   * Method to add the claim to the claims list.
   * 
   * @param key
   *          : Claim key
   * @param value
   *          : Claim value
   */
  public void addClaim(String key, Object value);

  /**
   * Method to add all the claims in the input to the claims list of this
   * message object.
   * 
   * @param claims
   *          : Claims
   */
  public void addClaims(Map<String, Object> claims);

  /**
   * Method to override/set {@link TdiClaims}.
   * 
   * @param claims
   *          : {@link TdiClaims}
   */
  public void setClaims(TdiClaims claims);

  /**
   * Returns the type of signature.
   * 
   * @return {@link String}
   */
  public String getSignatureType();

  /**
   * Sets the type of the signature.
   * 
   * @param signatureType
   *          {@link String}
   */
  void setSignatureType(String signatureType);

  /**
   * The full signed message or token to be returned to the application.
   * 
   * @return {@link String}
   */
  public String getBuiltMessage();

  /**
   * The full signed message or token to be returned to the application.
   * 
   * @param builtMessage
   *          {@link String}
   */
  void setBuiltMessage(String builtMessage);

  /**
   * The raw received message or token from the application.
   * 
   * @return {@link String}
   */
  public String getReceivedMessage();

  /**
   * Stores the raw received message or token from the application.
   * 
   * @param message
   *          : {@link String} message
   */
  public void setReceivedMessage(String message);

  /**
   * Signatures present on a message that are to be retained after signing.
   * 
   * @return {@link List} of Signature Object
   */
  public List<Object> getHeldSignatures();

  /**
   * Adds siganture to the internal list for access through
   * {@link #getHeldSignatures()}.
   * 
   * @param signature
   *          : Signature Object.
   */
  public void addHeldSignature(Object signature);

  /**
   * Adds signatures to the internal list for access through
   * {@link #getHeldSignatures()}.
   * 
   * @param heldSignatures
   *          : {@link List} of Signature object
   */
  public void addHeldSignatures(List<Object> heldSignatures);

  /**
   * Signatures present on a message that are to be verified.
   * 
   * @return{@link List} of Object containing details of the signatures to
   *               verify. Application using the
   *               {@link TdiCanonicalMessageShape} has the freedom to store
   *               whichever type of the object to store those details.
   */
  public List<Object> getSignaturesToVerify();

  /**
   * Adds signature to the internal list for access through
   * {@link #getSignaturesToVerify()}.
   * 
   * @param signature
   *          : Object containing details of the signatures to verify.
   *          Application using the {@link TdiCanonicalMessageShape} has the
   *          freedom to store whichever type of the object to store those
   *          details.
   */
  public void addSignatureToVerify(Object signature);

  /**
   * Adds signatures to the internal list for access through
   * {@link #getSignaturesToVerify()}.
   * 
   * @param signatures
   *          : {@link List} of Object
   */
  public void addSignaturesToVerify(List<Object> signatures);

  /**
   * The raw string the application requests to be signed (payload).
   * 
   * @return {@link String}
   */
  public String getRawPayload();

  /**
   * The raw string the application requests to be signed (payload).
   * 
   * @param rawPayload
   *          : {@link String}
   */
  void setRawPayload(String rawPayload);
}
