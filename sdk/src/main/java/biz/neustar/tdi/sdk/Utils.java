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

package biz.neustar.tdi.sdk;

/**
 * Utility class for SDK.
 */
public class Utils {
  /**
   * Returns a {@link Long} object.
   * 
   * @param expObj
   *          : an {@link Integer} or {@link Long} object
   * 
   * @return {@link Long} value
   */
  public static Long getLong(Object expObj) {

    Long expVal = 0L;

    if (expObj != null) {
      if (expObj instanceof Integer) {
        expVal = ((Integer) expObj).longValue();
      } else {
        expVal = (Long) expObj;
      }
    }

    return expVal;
  }
}
