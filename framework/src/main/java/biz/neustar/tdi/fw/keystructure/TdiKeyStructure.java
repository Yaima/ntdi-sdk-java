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
 * Base implementation of {@link TdiKeyStructureShape}.
 */
public class TdiKeyStructure implements TdiKeyStructureShape {
  /**
   * The messaging layer will reference keys using this key id.
   */
  String keyId;

  /**
   * Identifying string for the fleet.
   */
  String fleetId;

  /**
   * Bitmask field holding metadata of the key.
   */
  Integer flags;

  /**
   * Storage and cryptographic layers will reference key data using this
   * variable.
   */
  Object keyData;

  /**
   * Public key would be referenced through this variable.
   */
  Object publicKey;

  /**
   * Default constructor.
   * 
   * @param keyId
   *          : Key ID
   * @param fleetId
   *          : Fleet ID
   * @param keyData
   *          : Key data for serializing key when required.
   * @param flags
   *          : Flags holding metadata of the key.
   */
  public TdiKeyStructure(String keyId, String fleetId, Object keyData, Integer flags) {
    this.keyId = keyId;
    this.fleetId = fleetId;
    this.keyData = keyData;
    this.flags = flags;
  }

  @Override
  public String getKeyId() {
    return keyId;
  }

  @Override
  public String getFleetId() {
    return fleetId;
  }

  @Override
  public Integer getFlags() {
    return flags;
  }

  public boolean hasFlag(TdiKeyFlagsEnum flag) {
    return flag.getNumber() == (this.flags & flag.getNumber());
  }

  /**
   * Sets the flags.
   * 
   * @param flag
   *          : {@link TdiKeyFlagsEnum} Flag to set.
   * @param set
   *          : true for setting, false for unsetting.
   */
  public void setFlag(TdiKeyFlagsEnum flag, boolean set) {
    this.flags = this.flags & ~flag.getNumber();
    if (set) {
      this.flags = this.flags | flag.getNumber();
    }
  }

  @Override
  public Object getKeyData() {
    return keyData;
  }

  @Override
  public boolean allowedToSign() {
    return (TdiKeyFlagsEnum.CAN_SIGN
        .equals((this.flags
            & (TdiKeyFlagsEnum.CAN_SIGN.getNumber() | TdiKeyFlagsEnum.INVALID.getNumber()))));
  }

  @Override
  public boolean canSign() {
    return this.hasFlag(TdiKeyFlagsEnum.CAN_SIGN);
  }

  public void setCanSign(boolean set) {
    this.setFlag(TdiKeyFlagsEnum.CAN_SIGN, set);
  }

  @Override
  public boolean isInvalid() {
    return this.hasFlag(TdiKeyFlagsEnum.INVALID);
  }

  public void setIsInvalid(boolean set) {
    this.setFlag(TdiKeyFlagsEnum.INVALID, set);
  }

  @Override
  public boolean isExpirable() {
    return this.hasFlag(TdiKeyFlagsEnum.EXPIRABLE);
  }

  public void setIsExpirable(boolean set) {
    this.setFlag(TdiKeyFlagsEnum.EXPIRABLE, set);
  }

  @Override
  public boolean isRevokable() {
    return this.hasFlag(TdiKeyFlagsEnum.REVOKABLE);
  }

  public void setIsRevokable(boolean set) {
    this.setFlag(TdiKeyFlagsEnum.REVOKABLE, set);
  }

  @Override
  public boolean isOurOwn() {
    return this.hasFlag(TdiKeyFlagsEnum.OUR_OWN);
  }

  public void setIsOurOwn(boolean set) {
    this.setFlag(TdiKeyFlagsEnum.OUR_OWN, set);
  }

  @Override
  public boolean isOriginPersistent() {
    return this.hasFlag(TdiKeyFlagsEnum.ORIGIN_PERSIST);
  }

  public void setIsOriginPersistent(boolean set) {
    this.setFlag(TdiKeyFlagsEnum.ORIGIN_PERSIST, set);
  }

  @Override
  public boolean isOriginFlash() {
    return this.hasFlag(TdiKeyFlagsEnum.ORIGIN_FLASH);
  }

  public void setIsOriginFlash(boolean set) {
    this.setFlag(TdiKeyFlagsEnum.ORIGIN_FLASH, set);
  }

  @Override
  public boolean isGenerated() {
    return this.hasFlag(TdiKeyFlagsEnum.ORIGIN_GEN);
  }

  public void setIsGenerated(boolean set) {
    this.setFlag(TdiKeyFlagsEnum.ORIGIN_GEN, set);
  }

  @Override
  public boolean isOriginExternal() {
    return this.hasFlag(TdiKeyFlagsEnum.ORIGIN_EXTERN);
  }

  public void setIsOriginExternal(boolean set) {
    this.setFlag(TdiKeyFlagsEnum.ORIGIN_EXTERN, set);
  }

  @Override
  public boolean isOriginHsm() {
    return this.hasFlag(TdiKeyFlagsEnum.ORIGIN_HSM);
  }

  public void setIsOriginHsm(boolean set) {
    this.setFlag(TdiKeyFlagsEnum.ORIGIN_HSM, set);
  }

  @Override
  public boolean isOriginPki() {
    return this.hasFlag(TdiKeyFlagsEnum.ORIGIN_PKI);
  }

  public void setIsOriginPki(boolean set) {
    this.setFlag(TdiKeyFlagsEnum.ORIGIN_PKI, set);
  }

  @Override
  public Object getPublicKey() {
    return publicKey;
  }

  public void setPublicKey(Object publicKey) {
    this.publicKey = publicKey;
  }

  @Override
  public TdiKeyStructureShape export() {
    TdiKeyStructure exported = new TdiKeyStructure(this.keyId, this.fleetId, null,
        this.flags & TdiKeyFlagsEnum.EXPORT_MASK.getNumber());
    exported.setPublicKey(this.getPublicKey());
    return exported;
  }

  @Override
  public Integer getRoleFlag() {
    return this.flags & TdiKeyFlagsEnum.ROLE_MASK.getNumber();
  }

  public void setRoleFlag(Integer roleNumber) {
    this.flags = (this.flags & ~TdiKeyFlagsEnum.ROLE_MASK.getNumber()) | roleNumber;
  }
}
