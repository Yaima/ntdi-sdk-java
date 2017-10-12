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

package biz.neustar.tdi.fw.keystructure;

/**
 * Internal structure of a key along with its associated meta data.
 */
public interface TdiKeyStructureShape {
  /**
   * Returns the identity (<b>KID</b>) associated with this key.
   * 
   * @return String KID
   */
  public String getKeyId();

  /**
   * Returns the identity of fleet with which this key is associated.
   * 
   * @return String Fleet ID
   */
  public String getFleetId();

  /**
   * Returns bit-masked integer flags.
   * 
   * @return Integer
   */
  public Integer getFlags();

  /**
   * Returns the key data which can be used to serialize or de-serialize a key.
   * 
   * @return Object which can be type-casted to the corresponding
   *         implementation.
   */
  public Object getKeyData();

  /**
   * Returns the public key data as Object, which can be type-casted to the
   * corresponding implementation.
   * 
   * @return Object which can by type-casted.
   */
  public Object getPublicKey();

  /**
   * Method that returns the exported TdiKeyStructureShape object (Stripped down version
   * of key which are not generally required while being transported over the
   * wire.
   * 
   * @return {@link TdiKeyStructureShape}
   */
  public TdiKeyStructureShape export();

  /**
   * Returns true if this key is allowed to be used for signing.
   * 
   * @return boolean
   */
  public boolean allowedToSign();

  /**
   * Returns true if this key can sign. (i.e. if it is a private key or a
   * symmetric key).
   * 
   * @return boolean
   */
  public boolean canSign();

  /**
   * Returns true if this key is invalid.
   * 
   * @return boolean
   */
  public boolean isInvalid();

  /**
   * Returns true if this key is expirable.
   * 
   * @return boolean
   */
  public boolean isExpirable();

  /**
   * Returns true if this key is revokable.
   * 
   * @return boolean
   */
  public boolean isRevokable();

  /**
   * Returns true if this key is our own.
   * 
   * @return boolean
   */
  public boolean isOurOwn();

  /**
   * Returns true if this key originated from a persistent storage.
   * 
   * @return boolean
   */
  public boolean isOriginPersistent();

  /**
   * Returns true if this key is embedded onto the executable or device.
   * 
   * @return boolean
   */
  public boolean isOriginFlash();

  /**
   * Returns true if this key is generated.
   * 
   * @return boolean
   */
  public boolean isGenerated();

  /**
   * Returns true if this key is from an external source such as TLS.
   * 
   * @return boolean
   */
  public boolean isOriginExternal();

  /**
   * Returns true if this key originated from an HSM or secure element.
   * 
   * @return boolean
   */
  public boolean isOriginHsm();

  /**
   * Returns true if this key is imported from PKI.
   * 
   * @return boolean
   */
  public boolean isOriginPki();

  /**
   * Returns the role flag associated with this key.
   * 
   * @return Integer
   */
  public Integer getRoleFlag();
}
