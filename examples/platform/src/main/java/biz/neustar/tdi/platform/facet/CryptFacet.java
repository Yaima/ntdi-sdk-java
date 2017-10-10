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

package biz.neustar.tdi.platform.facet;

import biz.neustar.tdi.fw.keystructure.TdiKeyStructureShape;
import biz.neustar.tdi.fw.platform.TdiPlatformShape;
import biz.neustar.tdi.fw.platform.facet.crypto.TdiPlatformCryptoShape;
import biz.neustar.tdi.platform.Constants;
import biz.neustar.tdi.platform.UtilKeyGeneration;
import biz.neustar.tdi.platform.Utils;
import biz.neustar.tdi.platform.exception.PlatformRuntimeException;
import biz.neustar.tdi.platform.facet.keystore.KeyRef;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1OutputStream;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.BigIntegers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.Signature;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.util.concurrent.CompletableFuture;

/**
 * Implementation of CryptFacet class.
 * 
 */
public class CryptFacet implements TdiPlatformCryptoShape {

  private TdiPlatformShape pf;

  /**
   * Public constructor for CryptFacet class.
   * 
   * @param pf
   *          : {@link TdiPlatformShape} class object
   * 
   */
  public CryptFacet(TdiPlatformShape pf) {
    this.pf = pf;
  }

  /**
   * Method to get {@link TdiPlatformShape} object.
   * 
   * @return pf : {@link TdiPlatformShape} object from this class
   * 
   */
  @Override
  public TdiPlatformShape getPlatform() {
    return pf;
  }

  /**
   * Init method.
   * 
   * @see biz.neustar.tdi.fw.platform.facet.TdiPlatformFacetShape#init()
   * 
   * @return Completed {@link CompletableFuture}&lt;Void&gt; future.
   */
  @Override
  public CompletableFuture<Void> init() {
    return CompletableFuture.completedFuture(null);
  }

  /**
   * Method to sign payload using KeyStruct
   * 
   * @param key
   *          : {@link TdiKeyStructureShape} class object
   * @param payload
   *          : String payload to sign
   * 
   * @return {@link CompletableFuture} with either of the following states: <br>
   *         <b>Completed Successfully</b>: Computed signature of the sign
   *         operation. <br>
   *         <b>Completed Exceptionally</b>: {@link PlatformRuntimeException} in
   *         case of errors.
   * 
   * @see biz.neustar.tdi.fw.platform.facet.crypto.TdiPlatformCryptoShape#sign
   *      (biz.neustar.tdi.fw.keystructure.TdiKeyStructureShape,
   *      java.lang.String)
   */
  @Override
  public CompletableFuture<String> sign(TdiKeyStructureShape key, String payload) {

    return CompletableFuture.supplyAsync(() -> {
      if (key == null || (StringUtils.isEmpty(payload))) {
        throw new PlatformRuntimeException("Error: key or payload Object is NULL");
      } else {
        Object keyRefObject = key.getKeyData();

        if (keyRefObject instanceof KeyRef) {
          KeyRef keyRef = (KeyRef) keyRefObject;
          try {

            ECPrivateKey privKey;
            privKey = UtilKeyGeneration.generateEcPrivateKey(keyRef);

            Signature ecdsa = Signature.getInstance(Constants.SIGNATURE_ALG_SCHEME_SHA256withECDSA,
                BouncyCastleProvider.PROVIDER_NAME);
            ecdsa.initSign(privKey);
            ecdsa.update(Utils.toBytes(payload));
            byte[] signedBytes = ecdsa.sign();
            String encodedVal = encodeRs(signedBytes);

            return encodedVal;
          } catch (Exception err) {
            throw new PlatformRuntimeException(err.getMessage());
          }
        } else {
          throw new PlatformRuntimeException("Error: instance is not of KeyRef Type");
        }
      }
    });
  }

  /**
   * Encodes the signature to produce a nearly fixed size bytes.
   * 
   * @param signedBytes
   *          : signature array of bytes
   * 
   * @return encoded signature String
   * 
   * @throws IOException
   * 
   */
  private static String encodeRs(byte[] signedBytes) throws IOException {
    ASN1InputStream sigIs = new ASN1InputStream(signedBytes);
    ASN1Sequence sigSeq = (ASN1Sequence) sigIs.readObject();
    sigIs.close();

    if (sigSeq.size() != 2) {
      throw new IllegalArgumentException("Expected ASN1 Sequence size is 2 !");
    }

    ASN1Integer ansIntR = (ASN1Integer) sigSeq.getObjectAt(0);
    ASN1Integer ansIntS = (ASN1Integer) sigSeq.getObjectAt(1);

    byte[] rbytes = BigIntegers.asUnsignedByteArray(Constants.KEYSIZE_BYTES, ansIntR.getValue());
    byte[] sbytes = BigIntegers.asUnsignedByteArray(Constants.KEYSIZE_BYTES, ansIntS.getValue());
    byte[] rs = new byte[rbytes.length + sbytes.length];
    System.arraycopy(rbytes, 0, rs, 0, rbytes.length);
    System.arraycopy(sbytes, 0, rs, rbytes.length, sbytes.length);
    String b64Sig = Utils.toStringData(Utils.base64UrlEncode(rs));
    return b64Sig;
  }

  /**
   * Method to verify.
   * 
   * @param key
   *          : {@link TdiKeyStructureShape}
   * @param payload
   *          : String payload to verify
   * @param signature
   *          : String signature to verify
   * 
   * @return {@link CompletableFuture} with either of the following states: <br>
   *         <b>Completed Successfully</b>: boolean state with true if
   *         successfully verified. false otherwise. <br>
   *         <b>Completed Exceptionally</b>: {@link PlatformRuntimeException} if
   *         error in verification such as verification with invalid keys.
   * 
   * @see biz.neustar.tdi.fw.platform.facet.crypto.TdiPlatformCryptoShape#verify
   *      (biz.neustar.tdi.fw.keystructure.TdiKeyStructureShape,
   *      java.lang.String, java.lang.String)
   */
  @Override
  public CompletableFuture<Boolean> verify(TdiKeyStructureShape key, String payload,
      String signature) {

    return CompletableFuture.supplyAsync(() -> {
      if (key == null || (StringUtils.isEmpty(payload)) || (StringUtils.isEmpty(signature))) {
        throw new PlatformRuntimeException("Error: Invalid Input");
      } else {
        try {
          Object keyRefObject = key.getKeyData();

          if (keyRefObject instanceof KeyRef) {
            KeyRef keyRef = (KeyRef) keyRefObject;

            ECPublicKey pubKey = UtilKeyGeneration.generateEcpublicKey(keyRef);

            byte[] signedBytes = decodeRs(signature);
            Signature ecdsa = Signature.getInstance(Constants.SIGNATURE_ALG_SCHEME_SHA256withECDSA,
                BouncyCastleProvider.PROVIDER_NAME);
            ecdsa.initVerify(pubKey);
            ecdsa.update(Utils.toBytes(payload));
            return ecdsa.verify(signedBytes);
          } else {
            throw new PlatformRuntimeException("Error: instance is not of KeyRef Type");
          }
        } catch (Exception err) {
          throw new PlatformRuntimeException(err.getMessage());
        }
      }
    });
  }

  /**
   * Decodes the signature.
   * 
   * @param encodedData
   *          : String encoded signature
   * 
   * @return Decoded signature byte array
   * 
   * @throws IOException
   * 
   */
  private static byte[] decodeRs(String encodedData) throws IOException {

    byte[] b64SigDecoded = Utils.base64UrlDecode(encodedData);
    byte[] rbytes = new byte[b64SigDecoded.length / 2];
    byte[] sbytes = new byte[b64SigDecoded.length / 2];
    System.arraycopy(b64SigDecoded, 0, rbytes, 0, rbytes.length);
    System.arraycopy(b64SigDecoded, rbytes.length, sbytes, 0, sbytes.length);

    BigInteger rbi = BigIntegers.fromUnsignedByteArray(rbytes);
    BigInteger sbi = BigIntegers.fromUnsignedByteArray(sbytes);

    ASN1Integer ansIntR = new ASN1Integer(rbi);
    ASN1Integer ansIntS = new ASN1Integer(sbi);

    ByteArrayOutputStream sigBaos = new ByteArrayOutputStream();
    ASN1OutputStream sigAsn1Os = new ASN1OutputStream(sigBaos);
    DERSequence derSeq = new DERSequence(new ASN1Integer[] { ansIntR, ansIntS });
    sigAsn1Os.writeObject(derSeq);
    sigAsn1Os.close();

    return sigBaos.toByteArray();
  }

  /**
   * Encrypt method.
   * 
   * @return Exceptionally completed {@link CompletableFuture} as encryption is
   *         not yet supported.
   * 
   * @see biz.neustar.tdi.fw.platform.facet.crypto.TdiPlatformCryptoShape#encrypt()
   */
  @Override
  public CompletableFuture<Void> encrypt() {
    CompletableFuture<Void> future = new CompletableFuture<>();
    future.completeExceptionally(new PlatformRuntimeException("Unimplemented"));
    return future;
  }

  /**
   * Decrypt method.
   * 
   * @return Exceptionally completed {@link CompletableFuture} as decryption is
   *         not yet supported.
   * 
   * @see biz.neustar.tdi.fw.platform.facet.crypto.TdiPlatformCryptoShape#decrypt()
   */
  @Override
  public CompletableFuture<Void> decrypt() {
    CompletableFuture<Void> future = new CompletableFuture<>();
    future.completeExceptionally(new PlatformRuntimeException("Unimplemented"));
    return future;
  }
}
