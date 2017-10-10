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

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.nio.charset.StandardCharsets;
import java.security.Security;
import java.util.Base64;

/**
 * Utils class for common utility methods.
 *
 */
public class Utils {

  static {
    init();
  }
  
  /**
   * Converts to UTF-8 byte[] from the input string. If byte[] is provided as
   * input, it is returned without changes.
   * 
   * @param data : 
   *          expects either a String or a byte[]
   * 
   * @return byte[] resulting byte[]
   */
  public static byte[] toBytes(Object data) {

    return (data instanceof String) ? ((String) data).getBytes(StandardCharsets.UTF_8)
        : (byte[]) data;
  }

  /**
   * Converts to UTF-8 string from the input byte[]. If string is provided as
   * input, it is returned without changes.
   * 
   * @param data : 
   *          expects either a String or a byte[]
   * 
   * @return {@link String} object
   */
  public static String toStringData(Object data) {

    return (data instanceof String) ? ((String) data)
        : new String((byte[]) data, StandardCharsets.UTF_8);
  }

  /**
   * Default b64_encode adds padding, jwt spec removes padding.
   * 
   * @param msg : 
   *          data to encode which is either a String or a byte[]
   * 
   * @return base64 encoded data
   */
  public static byte[] base64UrlEncode(Object msg) {

    String encodedString = Base64.getUrlEncoder().encodeToString(toBytes(msg));
    String strippedString = encodedString.replaceAll("=", "");
    return toBytes(strippedString);
  }

  /**
   * JWT spec doesn't allow padding characters. base64url_encode removes them,
   * base64url_decode, adds them back in before trying to base64 decode the
   * message.
   * 
   * @param amsg : 
   *          URL safe base64 message which is either a String or a byte[]
   * 
   * @return decoded data
   */
  public static byte[] base64UrlDecode(Object amsg) {

    byte[] bmsg = toBytes(amsg);
    byte[] bpaddedMsg;
    int pad = (bmsg.length % 4);

    if (pad > 0) {

      bpaddedMsg = new byte[bmsg.length + (4 - pad)];
      System.arraycopy(bmsg, 0, bpaddedMsg, 0, bmsg.length);

      for (int i = bmsg.length; i < bpaddedMsg.length; i++) {
        bpaddedMsg[i] = (byte) '=';
      }
      bmsg = bpaddedMsg;
    }
    return Base64.getUrlDecoder().decode(bmsg);
  }
  
  /**
   * Adds the Bouncy Castle provider, if not added already.
   */
  public static void addBouncyCastleProvider() {
    if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
      Security.addProvider(new BouncyCastleProvider());
    }
  }
  
  /**
   * Initializes and installs Bouncy Castle Provider for better performance.  
   */
  public static void init() {
    addBouncyCastleProvider();
  }
}
