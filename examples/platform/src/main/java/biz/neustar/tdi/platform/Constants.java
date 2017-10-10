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

/**
 * Constants class. 
 *
 */
public class Constants {

  // Config key for platform -> key store json file path
  public static final String PLATFORM_DEFAULT_BASE_PATH = "./";

  // Platform configuration related keys
  public static final String PLATFORM_CONFIG_KEY_PLATFORM  = "platform";
  public static final String PLATFORM_CONFIG_KEY_DATASTORE = "data";
  public static final String PLATFORM_CONFIG_KEY_KEYSTORE  = "keys";
  public static final String PLATFORM_CONFIG_KEY_BASEPATH  = "basepath";
  

  // Name of the key store json file
  public static final String KEY_CONFIG_FILE = "keystore.json";
  
  // Used by TimeFacet for receiving timestamp in blow format
  public static final String ISO_TIMESTAMP_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
  
  public static final String EC_CURVE_SECP256R1 = "secp256r1";
  public static final String PKI_KEY_ALG_ECDSA = "ECDSA";

  // Crypto Key constants
  public static final String PKI_ALG_EC = "EC";
  public static final String SIGNATURE_ALG_SCHEME_SHA256withECDSA = "SHA256withECDSA";

  public static final int KEYSIZE = 256;
  public static final int KEYSIZE_BYTES = KEYSIZE / 8;

  /**
   * JWK parameters class.
   */
  public static class JwkParams {
    public static final String KID   = "kid";
    public static final String D     = "d";
    public static final String Y     = "y";
    public static final String X     = "x";
    public static final String CRV   = "crv";
    public static final String P_256 = "P-256";
    public static final String KTY   = "kty";
    public static final String EC    = "EC";
    public static final String USE   = "use";
    public static final String SIG   = "sig";
    public static final String ALG   = "alg"; 
    public static final String ES256 = "ES256";
  }  
}
