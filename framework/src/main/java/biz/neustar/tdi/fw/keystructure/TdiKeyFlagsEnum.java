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

public enum TdiKeyFlagsEnum {
  // TODO: Audit these for correctness. The commentary doesn't line up, and I
  //   forsee unexplained enum convergence. Possibly in the Typescript as well.
  //   Possible inappropriate use of ROLE_EXTERN. Conflated with network exchange flags?
  ROLE_UNDEF(0x0000), ROLE_EXTERN(0x0001),
  /** General use for non-standard keys. */
  ROLE_F_S(0x0002),
  /** Fleet key. */
  ROLE_F_C(0x0003),
  /** Fleet co-signer key. */
  ROLE_DEVICE(0x0004),
  /** A device's SELF key. As represented to non-self actors. */
  ROLE_SERVER(0x0005),
  /** A server's SELF key. As represented to non-self actors. */
  ROLE_COSERV(0x0006),
  /** A cosigner's SELF key. As represented to non-self actors. */
  ROLE_MASK(0x0007),
  /** This region of flag space overlaps with KeyRoles. */
  EXPIRABLE(0x0008),
  /** This key may expire at some point. */
  REVOKABLE(0x0010),
  /** This key can be revoked. */
  INVALID(0x0020),
  /** This key is no longer valid. */
  NET_ACCEPT(0x0040),
  /** Is acceptable for the network layer. */
  APP_ACCEPT(0x0080),
  /** Is acceptable for the application layer. */
  CAN_SIGN(0x0100),
  /** Is acceptable for the network layer. */
  OUR_OWN(0x0200),
  /** This is our own identity. */
  ORIGIN_PERSIST(0x0400),
  /** Key came from persistant storage (May be RW). */
  ORIGIN_FLASH(0x0800),
  /** Key is baked into the executable. */
  ORIGIN_GEN(0x1000),
  /** Was generated on this system. */
  ORIGIN_EXTERN(0x2000),
  /** Came from outside. Usually from TLS. */
  ORIGIN_HSM(0x4000),
  /** Came from an HSM or secure element. */
  ORIGIN_PKI(0x8000), /** Imparted by a PKI. */

  /** These are the key origin flags. */
  ORIGIN_MASK(ORIGIN_PERSIST.flagNumber
      | ORIGIN_FLASH.flagNumber
      | ORIGIN_GEN.flagNumber
      | ORIGIN_EXTERN.flagNumber
      | ORIGIN_HSM.flagNumber
      | ORIGIN_PKI.flagNumber),

  /** These flags are meaningless to another system. */
  EXPORT_MASK(~(CAN_SIGN.flagNumber
      | ORIGIN_GEN.flagNumber
      | ORIGIN_PERSIST.flagNumber
      | OUR_OWN.flagNumber));

  private Integer flagNumber;

  private TdiKeyFlagsEnum(int flagNumber) {
    this.flagNumber = flagNumber;
  }

  /**
   * Returns the {@link TdiKeyFlagsEnum} enum agains the flagNumber.
   *
   * @param flagNumber : Flag number
   *
   * @return {@link TdiKeyFlagsEnum}
   */
  public static TdiKeyFlagsEnum getKeyFlag(int flagNumber) {
    for (TdiKeyFlagsEnum flag : values()) {
      if (flag.flagNumber == flagNumber) {
        return flag;
      }
    }

    return ROLE_UNDEF;
  }

  /**
   * Returns the {@link TdiKeyFlagsEnum} based on the flag name.
   *
   * @param flagName : Flag Name
   *
   * @return {@link TdiKeyFlagsEnum}
   */
  public static TdiKeyFlagsEnum getKeyFlag(String flagName) {
    for (TdiKeyFlagsEnum flag : values()) {
      if (flag.name().equals(flagName)) {
        return flag;
      }
    }

    return ROLE_UNDEF;
  }

  public Integer getNumber() {
    return this.flagNumber;
  }

  public boolean equals(Integer flagNumber) {
    return this.flagNumber.equals(flagNumber);
  }
}
