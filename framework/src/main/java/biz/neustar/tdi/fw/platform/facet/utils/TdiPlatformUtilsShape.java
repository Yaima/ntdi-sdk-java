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

package biz.neustar.tdi.fw.platform.facet.utils;

import biz.neustar.tdi.fw.platform.facet.TdiPlatformFacetShape;

/**
 * Platform facet: Abstracted Utils.
 */
public interface TdiPlatformUtilsShape extends TdiPlatformFacetShape {
  /**
   * Method to encode toEncode string into a Base64 URL Safe string, based on
   * the platform implementation.
   * 
   * @param toEncode
   *          : String to encode.
   * 
   * @return Base64URL Safe encoded string.
   */
  public String b64UrlEncode(String toEncode);

  /**
   * Method to decode a Base64 URL Safe string into a normal string, based on
   * the platform implementation.
   * 
   * @param b64String
   *          : Base64URL Safe encoded string.
   * 
   * @return Decoded string.
   */
  public String b64UrlDecode(String b64String);

  /**
   * Method to fill the buffer with random data of length len, based on the
   * platform implementation.
   * 
   * @param buffer
   *          : byte[] to be filled
   * @param len
   *          : Length of the byte[] to be filled.
   */
  public void randomFill(byte[] buffer, Integer len);

  /**
   * Method to return a Unique Identifier, based on the platform implementation.
   * 
   * @return Generated Unique Identifier string.
   */
  public String makeUuid();
}
