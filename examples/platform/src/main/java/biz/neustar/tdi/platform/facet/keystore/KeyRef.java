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

package biz.neustar.tdi.platform.facet.keystore;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Class for mapping JSON ref object.
 *
 */
public class KeyRef {

  public String use;
  public String alg;
  public String kty;
  public String kid;
  public String crv;

  @JsonProperty("x") // Map Json X
  public String cordX;
  @JsonProperty("y") // Map Json Y
  public String cordY;
  @JsonProperty("d") // Map Json D
  public String privateD;
}
