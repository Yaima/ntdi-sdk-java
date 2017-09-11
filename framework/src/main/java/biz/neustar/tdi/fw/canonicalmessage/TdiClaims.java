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

package biz.neustar.tdi.fw.canonicalmessage;

import biz.neustar.tdi.fw.Constants.TdiClaimKeys;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Holds the claims for message.
 */
public class TdiClaims {
  /**
   * Claims Issuer.
   */
  @JsonInclude(Include.NON_NULL)
  @JsonProperty(TdiClaimKeys.ISS)
  public String iss;

  /**
   * Claims expiration time.
   */
  @JsonInclude(Include.NON_NULL)
  @JsonProperty(TdiClaimKeys.EXP)
  public Long exp;

  /**
   * Claims Not Before time.
   */
  @JsonInclude(Include.NON_NULL)
  @JsonProperty(TdiClaimKeys.NBF)
  public Long nbf;

  /**
   * Claims Nonce.
   */
  @JsonInclude(Include.NON_NULL)
  @JsonProperty(TdiClaimKeys.JTI)
  public String jti;

  /**
   * Claims payload map.
   */
  @JsonInclude(Include.NON_NULL)
  @JsonProperty(TdiClaimKeys.PAYLOAD)
  public Object payload;

  private Map<String, Object> claimsMap = new LinkedHashMap<>();

  @JsonAnySetter
  void addToClaimsMap(String key, Object value) {
    claimsMap.put(key, value);
  }

  @JsonAnyGetter
  public Map<String, Object> getClaimsMap() {
    return claimsMap;
  }

  @JsonIgnore
  @Override
  public boolean equals(Object obj) {
    TdiClaims other = (TdiClaims) obj;

    if (this == other) {
      return true;
    }

    if ((this.iss == null) ? (other.iss != null) : !this.iss.equals(other.iss)) {
      return false;
    }

    if ((this.exp == null) ? (other.exp != null) : !this.exp.equals(other.exp)) {
      return false;
    }

    if ((this.nbf == null) ? (other.nbf != null) : !this.nbf.equals(other.nbf)) {
      return false;
    }

    if ((this.jti == null) ? (other.jti != null) : !this.jti.equals(other.jti)) {
      return false;
    }

    if ((this.payload == null) ? (other.payload != null) : !this.payload.equals(other.payload)) {
      return false;
    }

    if (!this.claimsMap.equals(other.claimsMap)) {
      return false;
    }

    return true;
  }
}
