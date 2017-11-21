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

import biz.neustar.tdi.fw.keystructure.TdiKeyFlagsEnum;
import biz.neustar.tdi.fw.keystructure.TdiKeyStructure;
import biz.neustar.tdi.fw.keystructure.TdiKeyStructureShape;
import biz.neustar.tdi.fw.platform.TdiPlatformShape;
import biz.neustar.tdi.fw.platform.facet.keys.TdiPlatformKeysShape;
import biz.neustar.tdi.platform.Constants;
import biz.neustar.tdi.platform.Constants.JwkParams;
import biz.neustar.tdi.platform.UtilKeyGeneration;
import biz.neustar.tdi.platform.Utils;
import biz.neustar.tdi.platform.exception.PlatformRuntimeException;
import biz.neustar.tdi.platform.facet.keystore.Key;
import biz.neustar.tdi.platform.facet.keystore.KeyRef;
import biz.neustar.tdi.platform.facet.keystore.Keys;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.util.BigIntegers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Implementation of KeystoreFacet class.
 *
 */
public class KeystoreFacet implements TdiPlatformKeysShape {

  private static final Logger LOG = LoggerFactory.getLogger(KeystoreFacet.class);

  private static final String SELF = "SELF";

  private TdiPlatformShape pf;
  private Map<String, TdiKeyStructureShape> kstore;
  private Map<String, TdiKeyStructureShape> rstore;

  /**
   * Full File path for keystore json file.
   */
  String keystoreFilePath = "";

  /**
   * Constructor for class KeystoreFacet.
   *
   * @param pf
   *          : Object of {@link TdiPlatformShape}
   */
  public KeystoreFacet(TdiPlatformShape pf) {
    this.pf = pf;
    this.kstore = new HashMap<>();
    this.rstore = new HashMap<>();

    // Get the base path for json file
    Map<String, Object> platformConfigMap = pf.getConfig();

    @SuppressWarnings("unchecked")
    Map<String, Object> basePathConfig = (Map<String, Object>) platformConfigMap
        .get(Constants.PLATFORM_CONFIG_KEY_KEYSTORE);
    String dataBasePath = basePathConfig != null
        ? (String) basePathConfig.get(Constants.PLATFORM_CONFIG_KEY_BASEPATH) : "";
    if (StringUtils.isEmpty(dataBasePath)) {
      dataBasePath = Constants.PLATFORM_DEFAULT_BASE_PATH;
    }
    keystoreFilePath = dataBasePath + Constants.KEY_CONFIG_FILE;
  }

  /**
   * Method to get {@link TdiPlatformShape} class object.
   *
   * @return {@link TdiPlatformShape} class object
   */
  @Override
  public TdiPlatformShape getPlatform() {
    return this.pf;
  }

  /**
   * Init method for KeystoreFacet class.
   *
   * @return {@link CompletableFuture} with either of the following states: <br>
   *         <b>Completed Successfully</b>: Void after successful initialization
   *         of {@link KeystoreFacet}. <br>
   *         <b>Completed Exceptionally</b>: {@link PlatformRuntimeException} if
   *         error in reading keystore.
   */
  @Override
  public CompletableFuture<Void> init() {
    return storeFileCreate(null).thenCompose((keysObj) -> {
      List<CompletableFuture<?>> queue = new ArrayList<>();
      if (keysObj != null) {
        for (Key keyObj : keysObj.keys) {
          // Store key and add future in the queue
          queue.add(addKey(keyObj));
        }
      }
      // Wait till all tasks are done
      return CompletableFuture.allOf(queue.toArray(new CompletableFuture<?>[0]));
    });
  }

  /**
   * Takes a buffer and clobbers data at keystore.dat This is a helper fxn that
   * is particular to a file-based keystore. If data is provided, any
   * already-extant store by that name will be clobbered. Otherwise, tries to
   * load an existing keystore and the given path.
   *
   * @param value
   *          : Object of class Keys or null
   *
   * @return {@link CompletableFuture} with either of the following states: <br>
   *         <b>Completed Successfully</b>: {@link Keys} object with all the
   *         keys loaded from keystore.json. <br>
   *         <b>Completed Exceptionally</b>: {@link PlatformRuntimeException} if
   *         error in reading keystore.json.
   */
  CompletableFuture<Keys> storeFileCreate(Keys value) {
    if (value != null) {
      return storeFileSave(value).thenApply((arg) -> {
        return value;
      });
    } else {
      return CompletableFuture.supplyAsync(() -> {
        try {
          Keys arrayKeys = biz.neustar.tdi.fw.utils.Utils
              .jsonFileToObject(new File(keystoreFilePath), Keys.class);
          return arrayKeys;
        } catch (Exception err) {
          throw new PlatformRuntimeException(err.getMessage());
        }
      });
    }
  }

  /**
   * Takes a buffer and clobbers data at keystore.dat This is a helper fxn that
   * is particular to a file-based keystore.
   *
   * @param data
   *          : Object of Keys class
   *
   * @return {@link CompletableFuture} with either of the following states: <br>
   *         <b>Completed Successfully</b>: Void. <br>
   *         <b>Completed Exceptionally</b>: {@link PlatformRuntimeException} if
   *         error in writing to keystore.json.
   */
  private CompletableFuture<Void> storeFileSave(Keys data) {
    return CompletableFuture.supplyAsync(() -> {
      try {
        biz.neustar.tdi.fw.utils.Utils.objectToJsonFile(data, new File(keystoreFilePath));
        return null;
      } catch (Exception err) {
        throw new PlatformRuntimeException(err.getMessage());
      }
    });
  }

  /**
   * Method to add key to local key-value map.
   *
   * @param kstruct
   *          : Object of Key class to save.
   *
   * @return Completed
   *         {@link CompletableFuture}&lt;{@link TdiKeyStructureShape}&gt;
   *         future with {@link TdiKeyStructureShape} instance initialized with
   *         kstruct data.
   */
  private CompletableFuture<TdiKeyStructureShape> addKey(Key kstruct) {
    TdiKeyStructureShape newKeyStruct = new TdiKeyStructure(kstruct.kid, kstruct.fleet, kstruct.ref,
        kstruct.flags);
    kstore.put(kstruct.kid, newKeyStruct);

    // Check if it is our own key, if it is then add as SELF
    if (newKeyStruct.isOurOwn()) {
      rstore.put(SELF, newKeyStruct);
    }

    // Add this obj as fleet + role flag key
    // It will be used in case of getKeyByRole
    String key = kstruct.fleet + newKeyStruct.getRoleFlag();
    rstore.put(key, newKeyStruct);

    return CompletableFuture.completedFuture(newKeyStruct);
  }

  /**
   * Method to add key to local key-value map and dump map to to file.
   *
   * @param kstruct
   *          : Object of Key class to save.
   *
   * @return {@link CompletableFuture} with either of the following states: <br>
   *         <b>Completed Successfully</b>: {@link TdiKeyStructureShape}
   *         instance. <br>
   *         <b>Completed Exceptionally</b>: {@link PlatformRuntimeException} if
   *         error encountered while saving to keystore.json.
   */
  private CompletableFuture<TdiKeyStructureShape> addKeyAndSaveToFile(Key kstruct) {
    CompletableFuture<TdiKeyStructureShape> future = new CompletableFuture<>();
    CompletableFuture.supplyAsync(() -> {
      addKey(kstruct).thenApply((argTdiKeyStructure) -> {
        saveStore().thenApply((fileArg) -> {
          future.complete(argTdiKeyStructure);
          return null;
        }).exceptionally(err -> {
          future.completeExceptionally(new PlatformRuntimeException(err.getMessage()));
          return null;
        });
        return true;
      });
      return null;
    });
    return future;
  }

  /**
   * Method to get {@link TdiKeyStructureShape} based on KeyId.
   *
   * @param kid
   *          : String KeyId of the key to get.
   *
   * @return {@link CompletableFuture} with either of the following states: <br>
   *         <b>Completed Successfully</b>: {@link TdiKeyStructureShape}
   *         instance with key details. <br>
   *         <b>Completed Exceptionally</b>: {@link PlatformRuntimeException} if
   *         invalid kid is passed or if kid not present in keystore.json.
   */
  @Override
  public CompletableFuture<TdiKeyStructureShape> getKey(String kid) {
    return CompletableFuture.supplyAsync(() -> {
      if (StringUtils.isEmpty(kid)) {
        throw new PlatformRuntimeException("Invalid input, kid is null");
      } else {
        if (kstore.containsKey(kid)) {
          return kstore.get(kid);
        } else {
          throw new PlatformRuntimeException("kid: " + kid + " - not found in keystore");
        }
      }
    });
  }

  /**
   * Method to retrieve all key metadata.
   *
   * @return Completed
   *         {@link CompletableFuture}&lt;{@link List}&lt;{@link TdiKeyStructureShape}&gt;&gt;
   *         future containing all the metadata currently in the local map.
   */
  @Override
  public CompletableFuture<List<TdiKeyStructureShape>> getKeys() {
    return CompletableFuture.supplyAsync(() -> {
      List<TdiKeyStructureShape> keyStructList = new ArrayList<TdiKeyStructureShape>();
      keyStructList.addAll(kstore.values());
      return keyStructList;
    });
  }

  /**
   * Returns our SELF key.
   *
   * @return {@link CompletableFuture} with either of the following states: <br>
   *         <b>Completed Successfully</b>: {@link TdiKeyStructureShape} with
   *         SELF key details. <br>
   *         <b>Completed Exceptionally</b>: {@link PlatformRuntimeException} if
   *         no SELF key present.
   */
  @Override
  public CompletableFuture<TdiKeyStructureShape> getSelfKey() {
    return CompletableFuture.supplyAsync(() -> {
      TdiKeyStructureShape temp = rstore.get(SELF);
      if (temp == null) {
        throw new PlatformRuntimeException("No SELF defined.");
      } else {
        return temp;
      }
    });
  }

  /**
   * Find a key that has metadata matching the given role and (optionally) the
   * given fleet.
   *
   * @param role
   *          : Integer
   * @param fleetId
   *          : String
   *
   * @return {@link CompletableFuture} with either of the following states: <br>
   *         <b>Completed Successfully</b>: {@link TdiKeyStructureShape}
   *         instance with key details. <br>
   *         <b>Completed Exceptionally</b>: {@link PlatformRuntimeException} if
   *         fleetId, role pair is not found.
   */
  @Override
  public CompletableFuture<TdiKeyStructureShape> getKeyByRole(Integer role, String fleetId) {
    return CompletableFuture.supplyAsync(() -> {
      TdiKeyStructureShape temp = rstore.get((fleetId + role));
      if (temp == null) {
        String error = "Failed to find key in fleet " + fleetId + " with role " + role;
        throw new PlatformRuntimeException(error);
      } else {
        return temp;
      }
    });
  }

  /**
   * Given a kid value, lookup the key and export the public half as a PEM
   * string.
   *
   * @param inputKey
   *          : String JSON object. Mapped to KeyRef class.
   *
   * @return Object : Public pem string
   */
  @Override
  public Object getPublicPem(Object inputKey) {
    String key = (String) inputKey;
    if (!StringUtils.isEmpty(key)) {
      try {
        KeyRef ref = biz.neustar.tdi.fw.utils.Utils.jsonToObject(key, KeyRef.class);
        return Utils.toStringData(getPemBytes(UtilKeyGeneration.generateEcpublicKey(ref)));
      } catch (Exception err) {
        LOG.error("getPublicPem: Exception: " + err.getMessage());
      }
    }
    return null;
  }

  /**
   * Method to convert ECPublicKey to byte array.
   *
   * @param pk
   *          : public key to be converted
   *
   * @return returns byte array of Public Key
   */
  public byte[] getPemBytes(PublicKey pk) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try {
      JcaPEMWriter pemWrt = new JcaPEMWriter(new OutputStreamWriter(baos));
      pemWrt.writeObject(pk, null);
      pemWrt.close();
    } catch (Exception err) {
      LOG.error("getPemBytes: Error:" + err.getMessage());
      return null;
    }
    return baos.toByteArray();
  }

  /**
   * Method to generate key.
   *
   * @param flags
   *          : Flags Integer
   * @param kid
   *          : Key ID String
   * @param fleetId
   *          : Fleet ID String
   *
   * @return {@link CompletableFuture} with either of the following states: <br>
   *         <b>Completed Successfully</b>: {@link TdiKeyStructureShape} with
   *         generated key details. <br>
   *         <b>Completed Exceptionally</b>: {@link PlatformRuntimeException} if
   *         error encountered while generation.
   */
  @Override
  public CompletableFuture<TdiKeyStructureShape> generateKey(Integer flags, String kid,
      String fleetId) {
    try {
      KeyPair kp = UtilKeyGeneration.generateEcKeyPair();
      ECPrivateKey privateKey = (ECPrivateKey) kp.getPrivate();
      ECPublicKey publicKey = (ECPublicKey) kp.getPublic();

      String keyId = kid;
      if (StringUtils.isEmpty(kid)) {
        keyId = pf.getUtils().makeUuid();
      }
      KeyRef keyRef = getJwk(publicKey, privateKey, keyId);

      Key kstruct = new Key();
      kstruct.flags = flags | TdiKeyFlagsEnum.CAN_SIGN.getNumber()
          | TdiKeyFlagsEnum.ORIGIN_GEN.getNumber();
      kstruct.ref = keyRef;
      kstruct.kid = keyId;
      kstruct.fleet = fleetId;
      return addKeyAndSaveToFile(kstruct);
    } catch (Exception err) {
      throw new PlatformRuntimeException(err.getMessage());
    }
  }

  /**
   * Method to get JWK map from public private key.
   *
   * @param publicKey
   *          : ECPublicKey Public key
   * @param privateKey
   *          : ECPrivateKey Private key
   * @param kid
   *          : Key Id
   * @return {@link KeyRef} Object with JWK structure.
   *
   */
  public KeyRef getJwk(ECPublicKey publicKey, ECPrivateKey privateKey, String kid) {

    KeyRef ref = new KeyRef();
    ref.use = JwkParams.SIG;
    ref.alg = JwkParams.ES256;
    ref.kty = JwkParams.EC;
    ref.crv = JwkParams.P_256;
    ref.kid = kid;

    BigInteger xbi = publicKey.getW().getAffineX();
    BigInteger ybi = publicKey.getW().getAffineY();

    byte[] bytesX = BigIntegers.asUnsignedByteArray(xbi);
    byte[] bytesY = BigIntegers.asUnsignedByteArray(ybi);

    ref.cordX = Utils.toStringData(Utils.base64UrlEncode(bytesX));
    ref.cordY = Utils.toStringData(Utils.base64UrlEncode(bytesY));

    if (privateKey != null) {
      byte[] bytes = BigIntegers.asUnsignedByteArray(privateKey.getS());
      ref.privateD = Utils.toStringData(Utils.base64UrlEncode(bytes));
    }
    return ref;
  }

  /**
   * Method to store key.
   *
   * @param key
   *          : Object of class Key or JSON mappable using Key class.
   * @param flags
   *          : Integer flags
   * @param fleetId
   *          : String fleet ID
   *
   * @return {@link CompletableFuture} with either of the following states: <br>
   *         <b>Completed Successfully</b>: {@link TdiKeyStructureShape}. <br>
   *         <b>Completed Exceptionally</b>: {@link PlatformRuntimeException} if
   *         exception encountered while saving key.
   */
  @Override
  public CompletableFuture<TdiKeyStructureShape> setKey(Object key, Integer flags, String fleetId) {
    try {
      Key kstruct = null;
      if (key instanceof Key) {
        kstruct = (Key) key;
      } else {
        KeyRef keyRefObj = biz.neustar.tdi.fw.utils.Utils.jsonToObject((String) key,
            KeyRef.class);
        kstruct = new Key();
        kstruct.flags = flags;
        kstruct.ref = keyRefObj;
        kstruct.fleet = fleetId;
        kstruct.kid = keyRefObj.kid;
      }
      return addKeyAndSaveToFile(kstruct);
    } catch (Exception err) {
      throw new PlatformRuntimeException(err.getMessage());
    }
  }

  /**
   * Method to store key from provision.
   *
   * @param key
   *          : of type Object. JSON string or Object of Key class.
   *
   * @return {@link CompletableFuture} with either of the following states: <br>
   *         <b>Completed Successfully</b>: {@link TdiKeyStructureShape}. <br>
   *         <b>Completed Exceptionally</b>: {@link PlatformRuntimeException} if
   *         error encountered while parsing key.
   */
  @Override
  public CompletableFuture<TdiKeyStructureShape> setKeyFromProvision(Object key) {
    if (key == null) {
      throw new PlatformRuntimeException("setKeyFromProvision: Key is NULL");
    } else {
      if (!(key instanceof String)) {
        throw new PlatformRuntimeException("setKeyFromProvision: Key is not type of String");
      } else {
        try {
          Key keyObj = biz.neustar.tdi.fw.utils.Utils.jsonToObject((String) key, Key.class);
          if (!StringUtils.isEmpty(keyObj.pem)) {
            // To ECPublicKey from pem string
            byte[] pemByte = Utils.toBytes(keyObj.pem);
            ECPublicKey pubKey = fromPublicPem(pemByte);
            KeyRef keyRefObj = getJwk(pubKey, null, keyObj.kid);
            keyObj.ref = keyRefObj;
            return addKeyAndSaveToFile(keyObj);
          } else {
            throw new PlatformRuntimeException("Failed: Invalid or Empty Pem String");
          }
        } catch (Exception err) {
          throw new PlatformRuntimeException("Failed to parse key");
        }
      }
    }
  }

  /**
   * Creates an object of KeyPair using a PEM-formatted public ECDSA key. Note
   * that this keypair will not be capable of signing, only verifying.
   *
   * @param keyPemBytes
   *          : public key bytes
   *
   * @return {@link ECPublicKey} object initialized only with public key
   *
   * @throws IOException
   *           from ECPublicKey.
   */
  public ECPublicKey fromPublicPem(byte[] keyPemBytes) throws IOException {
    if (ArrayUtils.isNotEmpty(keyPemBytes)) {
      PublicKey readPubKey = null;
      ByteArrayInputStream pemBytesBais = new ByteArrayInputStream(keyPemBytes);
      PEMParser pemParse = new PEMParser(new InputStreamReader(pemBytesBais));
      SubjectPublicKeyInfo pubInfo = (SubjectPublicKeyInfo) pemParse.readObject();
      pemParse.close();
      readPubKey = new JcaPEMKeyConverter().setProvider(BouncyCastleProvider.PROVIDER_NAME)
          .getPublicKey(pubInfo);
      return ((ECPublicKey) readPubKey);
    }
    return null;
  }

  /**
   * Method to forget key based on key ID.
   *
   * @param kid
   *          : Key ID String
   *
   * @return Completed {@link CompletableFuture}&lt;Void&gt; object.
   */
  @Override
  public CompletableFuture<Void> forgetKey(String kid) {
    return CompletableFuture.supplyAsync(() -> {
      if (StringUtils.isEmpty(kid)) {
        LOG.error("forgetKey: kid is NULL");
      } else {
        if (kstore.containsKey(kid)) {
          kstore.remove(kid);
          LOG.debug("forgetKey: Done");
        } else {
          LOG.error("forgetKey: No key present to forget");
        }
      }
      return null;
    });
  }

  /**
   * Method to save all the metadata of kstore map to file.
   *
   * @return {@link CompletableFuture} with either of the following states: <br>
   *         <b>Completed Successfully</b>: Void. <br>
   *         <b>Completed Exceptionally</b>: {@link PlatformRuntimeException} if
   *         exception occurred in saving store.
   */
  @Override
  public CompletableFuture<Void> saveStore() {
    Keys keysObj = new Keys();
    keysObj.keys = new ArrayList<>();
    for (Map.Entry<String, TdiKeyStructureShape> entry : kstore.entrySet()) {
      TdiKeyStructureShape keyStruct = entry.getValue();
      Key key = new Key();
      key.kid = keyStruct.getKeyId();
      key.fleet = keyStruct.getFleetId();
      key.flags = keyStruct.getFlags();
      KeyRef keyRef = (KeyRef) keyStruct.getKeyData();
      key.ref = keyRef;
      keysObj.keys.add(key);
    }
    return storeFileSave(keysObj);
  }
}
