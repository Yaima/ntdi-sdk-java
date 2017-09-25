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

import biz.neustar.tdi.fw.platform.TdiPlatformShape;
import biz.neustar.tdi.fw.platform.facet.utils.TdiPlatformUtilsShape;
import biz.neustar.tdi.platform.Utils;

import java.security.SecureRandom;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Implementation of UtilFacet class. 
 *
 */
public class UtilFacet implements TdiPlatformUtilsShape {
  private TdiPlatformShape pf;

  /**
   * Constructor for UtilFacet class.
   * 
   * @param pf {@link TdiPlatformShape} class object
   */
  public UtilFacet(TdiPlatformShape pf) {
    this.pf = pf;
  }

  /**
   * Method to get platform object.
   * 
   * @return Object of class {@link TdiPlatformShape}
   */
  @Override
  public TdiPlatformShape getPlatform() {
    return pf;
  }

  /**
   * Method to initialize UtilFacet class.
   * 
   * @return Completed {@link CompletableFuture}&lt;Void&gt; object.
   */
  @Override
  public CompletableFuture<Void> init() {
    return CompletableFuture.completedFuture(null);
  }

  /**
   * Method to encode string in base64.
   * 
   * @param toEncode : String to convert. 
   *      
   * @return Base64 encoded string.  
   */
  @Override
  public String b64UrlEncode(String toEncode) {
    
    byte[] encodeBytes = Utils.base64UrlEncode(toEncode);
    return new String(encodeBytes);
  }

  /**
   * Method to decode base64 string.
   * 
   * @param b64String : base64 encoded string.
   *       
   * @return Decoded string.
   */
  @Override
  public String b64UrlDecode(String b64String) {
    byte[] decodStr = Utils.base64UrlDecode(b64String);
    return new String(decodStr);
  }

  /**
   * Method to random fill bytes in the provided buffer.
   * 
   * @param buffer : Buffer to fill random bytes 
   * @param len : Length of the buffer
   */
  @Override
  public void randomFill(byte[] buffer, Integer len) {
    SecureRandom random = new SecureRandom();
    byte[] randomBytes = new byte[len];
    random.nextBytes(randomBytes);
    System.arraycopy(randomBytes, 0, buffer, 0, len);
  }

  /**
   * Method to generate UUID.
   * 
   * @return {@link String} new UUID.
   */
  @Override
  public String makeUuid() {
    return UUID.randomUUID().toString();
  }

  /**
   * Converts to UTF-8 byte[] from the input string. If byte[] is provided as
   * input, it is returned without changes.
   * 
   * @param data : expects either a String or a byte[]
   * 
   * @return byte[] resulting byte[]
   */
  public byte[] toBytes(Object data) {
    return Utils.toBytes(data);
  }
  
  /**
   * Converts to UTF-8 string from the input byte[]. If string is provided as
   * input, it is returned without changes.
   * 
   * @param data : expects either a String or a byte[]
   * 
   * @return {@link String} object
   */
  public String toStringData(Object data) {
    return Utils.toStringData(data);
  }

}
