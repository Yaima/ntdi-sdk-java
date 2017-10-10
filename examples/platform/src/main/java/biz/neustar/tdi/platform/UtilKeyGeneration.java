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

package biz.neustar.tdi.platform;

import biz.neustar.tdi.platform.exception.PlatformRuntimeException;
import biz.neustar.tdi.platform.facet.keystore.KeyRef;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.BigIntegers;

import java.math.BigInteger;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPrivateKeySpec;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;

/**
 * Utility class for generating ECPublic and ECPrivate key.
 *
 */
public class UtilKeyGeneration {

  static {
    Utils.addBouncyCastleProvider();
    generateEcKeyPair();
  }

  /**
   * Method to generate ECPublic key based on KeyRef object.
   * 
   * @param keyRef
   *          Object of KeyRef class
   * @return Object of ECPublicKey in case of successful else null
   */
  public static ECPublicKey generateEcpublicKey(KeyRef keyRef) {

    if (keyRef == null) {
      throw new PlatformRuntimeException("getEcpublicKey: KeyRef object is NULL");
    }

    byte[] xbytes = Utils.base64UrlDecode(keyRef.cordX);
    byte[] ybytes = Utils.base64UrlDecode(keyRef.cordY);

    BigInteger xbi = BigIntegers.fromUnsignedByteArray(xbytes);
    BigInteger ybi = BigIntegers.fromUnsignedByteArray(ybytes);

    ECPoint pubPoint = new ECPoint(xbi, ybi);

    try {
      ECParameterSpec ecParameters = getEcParameterSpec();
      ECPublicKeySpec pubSpec = new ECPublicKeySpec(pubPoint, ecParameters);

      return (ECPublicKey) getKeyFactoryInstance().generatePublic(pubSpec);

    } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidParameterSpecException
        | InvalidKeySpecException | RuntimeException err) {
      throw new PlatformRuntimeException(err.getMessage());
    }
  }

  /**
   * Method to generate ECPrivate Key from KeyRef.
   * 
   * @param keyRef
   *          KeyRef class object
   * 
   * @return Return ECPrivateKey object if successful else null
   * 
   */
  public static ECPrivateKey generateEcPrivateKey(KeyRef keyRef) {

    if (keyRef == null) {
      throw new PlatformRuntimeException("generateEcPrivateKey: KeyRef is NULL");
    }

    try {
      ECParameterSpec ecParameters = getEcParameterSpec();
      if (!StringUtils.isEmpty(keyRef.privateD)) {
        BigInteger base64D = BigIntegers
            .fromUnsignedByteArray(Utils.base64UrlDecode(keyRef.privateD));
        ECPrivateKeySpec privSpec = new ECPrivateKeySpec(base64D, ecParameters);
        return (ECPrivateKey) getKeyFactoryInstance().generatePrivate(privSpec);
      } else {
        throw new PlatformRuntimeException("Generate Priavte Key Failed, 'keyRef.d' is NULL");
      }
    } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidParameterSpecException
        | InvalidKeySpecException err) {
      throw new PlatformRuntimeException(err.getMessage());
    }
  }

  /**
   * Returns a {@link KeyPair} for EC keys.
   * 
   * @return {@link KeyPair} instance
   */
  public static KeyPair generateEcKeyPair() {
    try {

      ECGenParameterSpec ecsp = getEcParamSpec();
      KeyPairGenerator kpg = KeyPairGenerator.getInstance(Constants.PKI_KEY_ALG_ECDSA,
          BouncyCastleProvider.PROVIDER_NAME);
      kpg.initialize(ecsp);
      return kpg.generateKeyPair();

    } catch (NoSuchAlgorithmException | NoSuchProviderException
        | InvalidAlgorithmParameterException err) {
      throw new PlatformRuntimeException(err.getMessage());
    }
  }

  /**
   * Returns {@link ECParameterSpec} instance initialized with required
   * parameters.
   * 
   * @return {@link ECParameterSpec} instance
   * 
   * @throws NoSuchAlgorithmException
   *           if algorithm does not exists
   * 
   * @throws NoSuchProviderException
   *           if provider does not exists
   * 
   * @throws InvalidParameterSpecException
   *           {@link InvalidParameterSpecException}
   */
  public static ECParameterSpec getEcParameterSpec()
      throws NoSuchAlgorithmException, NoSuchProviderException, InvalidParameterSpecException {

    AlgorithmParameters parameters = AlgorithmParameters.getInstance(Constants.PKI_ALG_EC,
        BouncyCastleProvider.PROVIDER_NAME);
    parameters.init(getEcParamSpec());
    ECParameterSpec ecParameters = parameters.getParameterSpec(ECParameterSpec.class);
    return ecParameters;
  }

  /**
   * Returns {@link KeyFactory} instance initialized with required parameters.
   * 
   * @return {@link KeyFactory} instance.
   * 
   * @throws NoSuchAlgorithmException
   *           if algorithm does not exists
   * 
   * @throws NoSuchProviderException
   *           if provider does not exists
   */
  public static KeyFactory getKeyFactoryInstance()
      throws NoSuchAlgorithmException, NoSuchProviderException {
    return KeyFactory.getInstance(Constants.PKI_KEY_ALG_ECDSA, BouncyCastleProvider.PROVIDER_NAME);
  }

  /**
   * Returns the {@link ECGenParameterSpec} instance initialized with required
   * parameters.
   * 
   * @return {@link ECGenParameterSpec} instance
   */
  public static ECGenParameterSpec getEcParamSpec() {
    return new ECGenParameterSpec(Constants.EC_CURVE_SECP256R1);
  }

}
