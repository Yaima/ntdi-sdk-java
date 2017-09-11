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
import biz.neustar.tdi.sdk.Constants.NonceConfig;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * This class handles the exp claim, which puts a temporal upper-bound on
 * messages.
 */
public class TdiSdkExpiresComponent extends TdiComponent {
  /**
   * Constructor.
   * 
   * @param componentName
   *          : Component Name.
   * @param impl
   *          : {@link TdiImplementationShape} instance.
   */
  public TdiSdkExpiresComponent(String componentName, TdiImplementationShape impl) {
    super(componentName, impl);
  }

  /*
   * (non-Javadoc)
   * 
   * @see biz.neustar.tdi.fw.interfaces.TdiComponentShape#init()
   */
  @Override
  public CompletableFuture<Void> init() {
    return CompletableFuture.completedFuture(null);
  }

  /**
   * Creates a new EXP claim equal to the current timestamp + the default number
   * of seconds.
   * 
   * @return Long number
   */
  public Long create() {
    @SuppressWarnings("unchecked")
    Map<String, Object> nonceConfig = (Map<String, Object>) this.getPlatform().getConfig()
        .get(NonceConfig.STORE);
    Long expirationDuration = Integer
        .toUnsignedLong((int) nonceConfig.get(NonceConfig.EXP_DURATION));

    return this.getPlatform().getTime().timestamp(null) + expirationDuration;
  }

  /**
   * Given the timestamp from an EXP claim, returns true if the date is in the
   * future.
   * 
   * @param date
   *          : Timestamp in seconds.
   * 
   * @return true if date is in future, false otherwise.
   */
  public boolean check(Long date) {
    return this.getPlatform().getTime().timestamp(null) < date;
  }
}
