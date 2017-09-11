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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Structure holding the JsonWebSignature.
 */
public class TdiJws {
  /**
   * Payload of the JWS Message. 
   */
  @JsonInclude(Include.NON_NULL)
  @JsonProperty(JwsKeys.PAYLOAD)
  public String payload;
  
  /**
   * List of {@link TdiJwsSignature} holding multiple signatures. 
   */
  @JsonInclude(Include.NON_NULL)
  @JsonProperty(JwsKeys.SIGNATURES)
  public List<TdiJwsSignature> signatures;

  /**
   * Constructor.
   */
  public TdiJws() {
    this.signatures = new ArrayList<>();
  }
}
