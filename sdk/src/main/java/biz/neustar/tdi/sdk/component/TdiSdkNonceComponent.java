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
import biz.neustar.tdi.sdk.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

/**
 * This class is for version2 message nonces. This component is required to
 * prevent replay attacks.
 *
 * <p>The usage of the datastore in this class is somewhat non-standard. It is
 * structured like this: { 'burnt': { '&lt;ISO String&gt;': epochtimestamp },
 * 'nbfMinimum': 0, 'expDuration': 0 }
 */
public class TdiSdkNonceComponent extends TdiComponent {
  private Long localExpDuration;
  private Long localNbfMinimum;
  private Map<String, Long> burnList;
  private static final String validCharacters = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ"
      + "abcdefghijklmnopqrstuvwxyz";
  private static final String NONCE_REGEX = "^00[12][2-9][0-9]{3}-(0[1-9]|1[0-2])-"
      + "(0[1-9]|[12][0-9]|3[01])T([01][0-9]|2[0-3])(:[0-5][0-9]){2}Z[A-Za-z0-9]{6}$";
  private int randomLength = 6;

  private static final Logger LOG = LoggerFactory.getLogger(TdiSdkNonceComponent.class);

  /**
   * Constructor.
   *
   * @param componentName
   *          : Component Name.
   * @param impl
   *          : {@link TdiImplementationShape} instance.
   */
  public TdiSdkNonceComponent(String componentName, TdiImplementationShape impl) {
    super(componentName, impl);
    burnList = new HashMap<>();
  }

  /*
   * (non-Javadoc)
   *
   * @see biz.neustar.tdi.fw.interfaces.TdiComponentShape#init()
   */
  @SuppressWarnings("unchecked")
  @Override
  public CompletableFuture<Void> init() {
    return this.getPlatform().getDataStore().createStore(getName(), null)
        .thenCompose((storeData) -> {
          List<CompletableFuture<?>> queue = new ArrayList<>();
          Map<String, Object> storeMap = (Map<String, Object>) storeData;

          // Exp Duration check.
          if (storeMap.containsKey(NonceConfig.EXP_DURATION)) {
            this.localExpDuration = Utils.getLong(storeMap.get(NonceConfig.EXP_DURATION));
          } else {
            this.localExpDuration = Utils.getLong(
                (int) ((Map<String, Object>) this.getConfig().get(NonceConfig.STORE))
                    .get(NonceConfig.EXP_DURATION));
            queue.add(this.getDataStore().set(NonceConfig.EXP_DURATION, this.localExpDuration));
          }

          // NBF Minimum
          if (storeMap.containsKey(NonceConfig.NBF_MINIMUM)) {
            this.localNbfMinimum = Utils.getLong(storeMap.get(NonceConfig.NBF_MINIMUM));
          } else {
            this.localNbfMinimum = this.getPlatform().getTime().timestamp(null);
            queue.add(this.getDataStore().set(NonceConfig.NBF_MINIMUM, this.localNbfMinimum));
          }

          return CompletableFuture.allOf(queue.toArray(new CompletableFuture<?>[0]))
              .thenApply((arg) -> {
                if (storeMap.containsKey(NonceConfig.BURNT)) {
                  burnList.putAll(
                      (Map<? extends String, ? extends Long>) storeMap.get(NonceConfig.BURNT));
                }
                return null;
              });
        });
  }

  /**
   * Creates a new nonce string for inclusion in an outbound claim.
   *
   * @return String
   */
  public String create() {
    StringBuilder randomString = new StringBuilder();
    randomString.append(NonceConfig.VERSION);
    randomString.append(this.getPlatform().getTime().isoDate(localExpDuration));
    for (int loopIndex = 0; loopIndex < randomLength; loopIndex++) {
      randomString.append(
        validCharacters.charAt(new Random().nextInt(validCharacters.length()-1))
      );
    }
    return randomString.toString();
  }

  /**
   * Returns true if all of the following are true of the given nonce string...
   * 0) ...represents a time greater than nfbMinimum... 1) ...is version 002...
   * 2) ...represents a time that does not lie in the future... 3) ...has not
   * been seen before.
   *
   * @param nonceStr
   *          : The JTI claim from an incoming message.
   *
   * @return {@link CompletableFuture} with either of the following states: <br>
   *         <b>Completed Successfully</b>: {@link Boolean} with true if valid.
   *         false otherwise. <br>
   *         <b>Completed Exceptionally</b>: {@link Exception} in case of
   *         failure.
   */
  public CompletableFuture<Boolean> check(String nonceStr) {

    return this.getDataStore().get(NonceConfig.NBF_MINIMUM).thenApply((minimum) -> {
      if (Pattern.matches(NONCE_REGEX, nonceStr) == true) {
        String version = nonceStr.substring(0, 3);
        String timeStr = nonceStr.substring(3, nonceStr.length() - randomLength);
        Long expiration = this.getPlatform().getTime().timestamp(timeStr);

        if (!NonceConfig.VERSION.equals(version)) {
          LOG.debug("Nonce: Bad nonce version: " + nonceStr);
          return false;
        }

        if (expiration < Utils.getLong(minimum)) {
          LOG.debug("Nonce: Exp (" + expiration + ") > Minimum (" + minimum + ")");
          return false;
        }

        if (expiration <= this.getPlatform().getTime().timestamp(null)) {
          LOG.debug("Nonce: Bad nonce: " + nonceStr);
          return false;
        }

        return !burnList.containsKey(nonceStr);
      } else {
        LOG.debug("Nonce: Invalid Nonce:" + nonceStr);
      }

      return false;
    });
  }

  /**
   * Will add the given nonce string to a list of burned nonces. Updates
   * nbfMinimum as a side-effect.
   *
   * @param nonceStr
   *          : nonce that we are marking as received.
   *
   * @return {@link CompletableFuture} with either of the following states: <br>
   *         <b>Completed Successfully</b>: Void. <br>
   *         <b>Completed Exceptionally</b>: {@link Exception} in case of failure.
   */
  public CompletableFuture<Void> burn(String nonceStr) {
    cleanCache();

    this.burnList.put(nonceStr, this.getPlatform().getTime()
        .timestamp(nonceStr.substring(3, nonceStr.length() - randomLength)));

    return this.getDataStore().set(NonceConfig.BURNT, this.burnList).thenCompose((arg) -> {
      return this.getDataStore().set(NonceConfig.NBF_MINIMUM, this.localNbfMinimum);
    });
  }

  /**
   * Called to update the burn list.
   */
  private void cleanCache() {
    Map<String, Long> newList = new HashMap<>();
    Long currentTimestamp = this.getPlatform().getTime().timestamp(null);
    Long newNbfMinimum = this.localNbfMinimum;
    Long nonce = null;

    for (String key : burnList.keySet()) {
      nonce = Utils.getLong(burnList.get(key));
      if (nonce > currentTimestamp) {
        newList.put(key, nonce);
      } else if (nonce > newNbfMinimum) {
        newNbfMinimum = nonce;
      }
    }

    this.localNbfMinimum = newNbfMinimum;
    burnList.clear();
    burnList.putAll(newList);
  }
}
