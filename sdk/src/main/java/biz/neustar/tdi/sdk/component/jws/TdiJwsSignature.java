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

package biz.neustar.tdi.sdk.component.jws;

import biz.neustar.tdi.sdk.Constants.JwsKeys;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * Structure holding the JsonWebSignatures' signatures.
 */
public class TdiJwsSignature {
  /**
   * ProtectedHeader of the signature. 
   */
  @JsonInclude(Include.NON_NULL)
  @JsonProperty(JwsKeys.PROTECTED)
  public String protectedHeader;
  
  /**
   * Actual Signature.
   */
  @JsonInclude(Include.NON_NULL)
  @JsonProperty(JwsKeys.SIGNATURE)
  public String signature;
  
  /**
   * Parsed header. Expansions of the Protected Header. 
   */
  @JsonIgnore
  public TdiJwsHeader parsedHeader; 
}
