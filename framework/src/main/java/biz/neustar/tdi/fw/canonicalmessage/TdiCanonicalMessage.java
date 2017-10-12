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

import biz.neustar.tdi.fw.Constants.TdiClaimKeys;
import biz.neustar.tdi.fw.keystructure.TdiKeyStructureShape;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Default implementation of {@link TdiCanonicalMessageShape} format of the
 * message that would be transferred internally in the framework, library, and
 * plugins. This independent of format that is sent over the wire.
 */
public class TdiCanonicalMessage implements TdiCanonicalMessageShape {
  Integer id;
  String currentProject;
  TdiClaims claims;
  List<TdiKeyStructureShape> signers;
  List<TdiKeyStructureShape> verifiers;
  String signatureType;
  String rawPayload;
  String builtMessage;
  String receivedMessage;
  List<Object> heldSignatures;
  List<Object> signaturesToVerify;

  /**
   * Constructor.
   * 
   * @param id
   *          : Message ID
   */
  public TdiCanonicalMessage(Integer id) {
    this.id = id;
    this.claims = new TdiClaims();
    this.signers = new ArrayList<>();
    this.verifiers = new ArrayList<>();
    this.heldSignatures = new ArrayList<>();
    this.signaturesToVerify = new ArrayList<>();
  }

  @Override
  public Integer getId() {
    return this.id;
  }

  @Override
  public TdiClaims getClaims() {
    return this.claims;
  }

  @Override
  public void addClaim(String key, Object value) {
    switch (key) {
      case TdiClaimKeys.ISS: {
        this.claims.iss = (String) value;
      }
        break;

      case TdiClaimKeys.EXP: {
        this.claims.exp = ((Integer) value).longValue();
      }
        break;

      case TdiClaimKeys.NBF: {
        this.claims.nbf = ((Integer) value).longValue();
      }
        break;

      case TdiClaimKeys.JTI: {
        this.claims.jti = (String) value;
      }
        break;

      case TdiClaimKeys.PAYLOAD: {
        this.claims.payload = value;
      }
        break;

      default: {
        this.claims.addToClaimsMap(key, value);
      }
        break;
    }
  }

  @Override
  public void addClaims(Map<String, Object> claims) {
    if (claims != null) {
      for (Entry<String, Object> entry : claims.entrySet()) {
        this.addClaim(entry.getKey(), entry.getValue());
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * biz.neustar.tdi.fw.canonicalmessage.TdiCanonicalMessageShape#setClaims(biz.
   * neustar.tdi.fw.canonicalmessage.TdiClaims)
   */
  @Override
  public void setClaims(TdiClaims claims) {
    this.claims = claims;
  }

  @Override
  public List<TdiKeyStructureShape> getSigners() {
    return this.signers;
  }

  /**
   * Add the signer in the signers list.
   * 
   * @param signer
   *          : {@link TdiKeyStructureShape} instance with signing key details
   */
  @Override
  public void addSigner(TdiKeyStructureShape signer) {
    this.signers.add(signer);
  }

  @Override
  public List<TdiKeyStructureShape> getVerifiers() {
    return this.verifiers;
  }

  /**
   * Add the verifier in the verifiers list.
   * 
   * @param verifier
   *          {@link TdiKeyStructureShape} instance with verifiying key details.
   */
  @Override
  public void addVerifier(TdiKeyStructureShape verifier) {
    this.verifiers.add(verifier);
  }

  @Override
  public String getSignatureType() {
    return this.signatureType;
  }

  /**
   * Setter method to set the signature type.
   * 
   * @param signatureType
   *          : Type of Signature. <b>JOSE+JSON, JSON</b>
   */
  @Override
  public void setSignatureType(String signatureType) {
    this.signatureType = signatureType;
  }

  @Override
  public String getCurrentProject() {
    return this.currentProject;
  }

  @Override
  public void setCurrentProject(String currentProject) {
    this.currentProject = currentProject;
  }

  @Override
  public String getRawPayload() {
    return this.rawPayload;
  }

  @Override
  public void setRawPayload(String rawPayload) {
    this.rawPayload = rawPayload;
  }

  @Override
  public String getBuiltMessage() {
    return this.builtMessage;
  }

  @Override
  public void setBuiltMessage(String builtMessage) {
    this.builtMessage = builtMessage;
  }

  @Override
  public String getReceivedMessage() {
    return this.receivedMessage;
  }

  @Override
  public void setReceivedMessage(String receivedMessage) {
    this.receivedMessage = receivedMessage;
  }

  @Override
  public List<Object> getHeldSignatures() {
    return this.heldSignatures;
  }

  @Override
  public void addHeldSignature(Object signature) {
    this.heldSignatures.add(signature);
  }

  @Override
  public void addHeldSignatures(List<Object> heldSignatures) {
    this.heldSignatures.addAll(heldSignatures);
  }

  /*
   * (non-Javadoc)
   * 
   * @see biz.neustar.tdi.fw.canonicalmessage.TdiCanonicalMessageShape#
   * getSignaturesToVerify()
   */
  @Override
  public List<Object> getSignaturesToVerify() {
    return this.signaturesToVerify;
  }

  /*
   * (non-Javadoc)
   * 
   * @see biz.neustar.tdi.fw.canonicalmessage.TdiCanonicalMessageShape#
   * addSignatureToVerify(java.lang.String)
   */
  @Override
  public void addSignatureToVerify(Object signature) {
    this.signaturesToVerify.add(signature);
  }

  /*
   * (non-Javadoc)
   * 
   * @see biz.neustar.tdi.fw.canonicalmessage.TdiCanonicalMessageShape#
   * addSignaturesToVerify(java.util.List)
   */
  @Override
  public void addSignaturesToVerify(List<Object> signatures) {
    this.signaturesToVerify.addAll(signatures);
  }
}
