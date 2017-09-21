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

package biz.neustar.tdi.sdk.component;

import biz.neustar.tdi.fw.component.TdiComponent;
import biz.neustar.tdi.fw.implementation.TdiImplementationShape;

import java.util.concurrent.CompletableFuture;

/**
 * This class handles the nbf claim, which puts a temporal lower-bound on messages.
 */
public class TdiSdkNotBeforeComponent extends TdiComponent {
  public static final String componentName = "nbf";
  
  /**
   * Constructor. 
   * 
   * @param componentName : Component Name.
   * @param impl : {@link TdiImplementationShape} instance. 
   */
  public TdiSdkNotBeforeComponent(String componentName, TdiImplementationShape impl) {
    super(componentName, impl);
  }

  /* (non-Javadoc)
   * @see biz.neustar.tdi.fw.interfaces.TdiComponentShape#getName()
   */
  @Override
  public String getName() {
    return componentName;
  }

  /* (non-Javadoc)
   * @see biz.neustar.tdi.fw.interfaces.TdiComponentShape#init()
   */
  @Override
  public CompletableFuture<Void> init() {
    return CompletableFuture.completedFuture(null);
  }
  
  /**
   * Creates a new NBF claim equal to the current timestamp.
   * 
   * @return Long number in seconds. 
   */
  public Long create() {
    return this.getPlatform().getTime().timestamp(null);
  }
  
  /**
   * Given the timestamp from an NBF claim, returns true if the date is not in the message. 
   * 
   * @param date : Long timestamp to check. 
   * 
   * @return true if date is in past. false otherwise. 
   */
  public boolean check(Long date) {
    return this.getPlatform().getTime().timestamp(null) > date;
  }
}
