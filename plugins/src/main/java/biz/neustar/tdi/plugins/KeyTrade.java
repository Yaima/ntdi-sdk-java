/**
 * @module node-tdi-plugins/key/trade
 */ /** */
/**
* Copyright 2017 Neustar, Inc
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package biz.neustar.tdi.plugins;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import biz.neustar.tdi.fw.canonicalmessage.TdiCanonicalMessageShape;
import biz.neustar.tdi.fw.exception.FrameworkRuntimeException;
import biz.neustar.tdi.fw.implementation.TdiFlowArguments;
import biz.neustar.tdi.fw.implementation.TdiImplementationShape;
import biz.neustar.tdi.fw.keystructure.TdiKeyFlagsEnum;
import biz.neustar.tdi.fw.keystructure.TdiKeyStructureShape;
import biz.neustar.tdi.fw.plugin.TdiPluginBase;
import biz.neustar.tdi.fw.wrapper.TdiSdkWrapperShape;
import biz.neustar.tdi.sdk.Constants;
import biz.neustar.tdi.sdk.component.jws.TdiJwsSignature;


/**
 * This is a plugin to allow formation and observation of reset messages.
 *
 * Provisioning Message Notes:
 * =============================================================================
 * These messages are used for initial onboarding in the case where no existing
 *   key material is present. If there are existing active keys, this message
 *   must be signed by the existing fleet corrosponding to those keys.
 * Provisioning messages may be used to place key material for a fleet alongside
 *   the keys for the home fleet, rather than supplanting them. In this case,
 *   the home fleet's cosigner must be a signatory on the message.
 * There is no preferential client-server ordering of this message, but a reply
 *   is required. The receiving side will need to reply to the message to
 *   complete the provisioning process. When it does, it will use the kid field
 *   from the provisioning request as the aud for the reply. The reply shall
 *   contain an array of JWKs (or COSE equivalents) for application storage.
 *
 * Claims added and processed by this message type:
 * "prov": {
 *   "sdk": {
 *     "ver": "0",
 *     "nom": "SDKName"
 *   },
 *   "homefleet": {},          // Optional field for info about the home fleet.
 *   "ntdif": 0x00000000,      // Flags reflecting support and role.
 *   "slots": -1,              // The number of open key slots on this platform.
 *   "keys": [                 // An array of keys.
 *     {
 *       "fleet": UUID,
 *       "kid":  UUID,         // Key identifier as a UUID string or 16-byte array.
 *       "parentFleet": UUID,
 *       "exp":                // epoch timestamp of expiration date
 *       "pub":  DER/PEM,
 *       "flags": 0x00000000   // Flags that relate to this key. Includes role.
 *     },
 *     ....
 *   ],
 *   "returnuri": "udp://ip.of.device:port"   // By what route to send a reply?
 * }
 *
 */
public class KeyTrade extends TdiPluginBase {
  private static final Logger LOG = LoggerFactory.getLogger(FleetSigner.class);

  /**
   * SDK reference for provisioning messages
   * The plugin's name.
   */
  private static final String plugName = "KeyTrade";
  /**
   * The SDK version.
   * @private
   */
  private static final String SDK_VER = "0.1.0"; // TODO: Might-should be FW version?
  /**
   * The SDK name.
   * @private
   */
  private static final String SDK_NOM = "java-sdk";

  public Function<String, CompletableFuture<TdiCanonicalMessageShape>> announce = null;
  public Function<String, CompletableFuture<TdiCanonicalMessageShape>> provision = null;
  public Function<String, CompletableFuture<TdiCanonicalMessageShape>> acceptKeys = null;


  public KeyTrade(TdiImplementationShape impl, TdiSdkWrapperShape sdkWrapper) {
    super(plugName, impl, sdkWrapper);
    LOG.trace("KeyTrade:constructor()");

    // public flows
    this.announce = this.impl.buildApiFlow(
      this.sdkWrapper.getDefaultFlows().get(Constants.Api.SignFlow),
      this.buildFlowAnnounce()
    );
    this.provision = this.impl.buildApiFlow(
      this.sdkWrapper.getDefaultFlows().get(Constants.Api.SignFlow),
      this.buildFlowProvision()
    );
    this.acceptKeys = this.impl.buildApiFlow(
      this.buildFlowAcceptKeys(),
      null
    );
  }

  /**
   * Called by the framework during initialization to accomodate asynchronous
   *   setup. This is the appropriate place to check and persist conf.
   */
  public CompletableFuture<Boolean> init() {
    LOG.trace("KeyTrade:init()");
    return this.validatePluginDataStore(Arrays.asList(
        // Any fleets that we are aware of will be itemized here.
        // TODO: Is this a good idea? Right now, we are deriving this info from
        //         inspection of the keystore.
        // ['residentFleets', (val: any) -> { return val.isArray(); }],
        "netRole", // Used to derive flags for keys and role-based behavior.
        "keySlots",
        "returnUri"
      ))
      .thenCompose((Boolean confValid) -> {
        if (confValid) {
          // NOTE: Corrective measures for missing conf would ordinarilly go here.
          // If none are desired, this block can be struck.
        }
        return confValid;
      });
  }


  /**
   * Helper fxn to build a base provisioning payload.
   * {
   *   sdk: {
   *     ver:  "0",
   *     nom:  "0",
   *   },
   *   slots: -1,
   *   keys: <any>[],
   *   returnUri: "127.0.0.1:14000"
   * }
   * @private
   */
  private CompletableFuture<TdiCanonicalMessageShape> provMsg()  {
    // TODO: Need error checking on config everywhere it is used.
    // TODO: Flesh this out a lot
    List<CompletableFuture<TdiKeyStructureShape>> promise_queue =
      new ArrayList<>(
        this.getDataStore().get("keySlots"),
        this.getDataStore().get("returnUri")
      );

    return CompletableFuture.allOf(promise_queue.toArray(new CompletableFuture<?>[0]))
      .thenCompose((arg) -> {
        return {
          sdk: {
            ver: SDK_VER,
            nom: SDK_NOM
          },
          slots: queue[0],
          keys: <any>[],
          returnUri: queue[1]
        };
      });
  }

  /**
   * Helper fxn to accept and validate a base provisioning payload.
   * @private
   * @param data The provisioning block of the provisioning payload.
   */
  private boolean verifyProvMsg(data: any) {
    // TODO: Do heavier validation to make sure we're not accepting bad flags or things we
    // shouldn't over the network (or perhaps fleets outside of our own context after provisioning)
    // ALSO: Check for F_C and F_S with matching fleets...
    // ALSO: make the parameter (provisioning packet) an interface.
    if (
      "sdk" in data &&
      "ver" in data.sdk &&
      "keys" in data &&
      data.keys.length > 0
    ) {
      LOG.info(
        `Accepted provisioning payload from ${data.sdk.nom} v${data.sdk.ver}.`
      );
      return true;
    }
    return false;
  }

  /**
   * Helper fxn to sanitize key flags for export over the network.
   * @private
   * TODO: Straighten-out return value.
   * @param kid The kid of the key to export.
   */
  private CompletableFuture<?> exportKey(String kid) {
    LOG.trace("exportKey(" + kid + ")");
    return this.impl.getPlatform().getKeystore().getKey(kid)
      .thenCompose((t_key: TdiKeyStructure) -> {
        const pem: string = this.impl.getPlatform().getKeystore().getPublicPem(t_key.ref);
        const newKey: any = t_key.export();
        newKey.pub = pem;
        return Promise.resolve(newKey);
      })
      .catch((err: string) -> {
        return Promise.reject("exportKey() failure (somehow). " + err);
      });
  }

  /**
   * Helper fxn to sanitize SELF key for export over the network.
   * @private
   * TODO: Straighten-out return value.
   */
  private CompletableFuture<?> exportSelf() {
    LOG.trace("exportSelf()");
    return this.impl.getPlatform().getKeystore().getSelf()
      .thenCompose((t_key: TdiKeyStructure) -> {
        const pem: string = this.impl.getPlatform().getKeystore().getPublicPem(t_key.ref);
        const newKey: any = t_key.export();
        newKey.pub = pem;
        return Promise.resolve(newKey);
      })
      .catch((err: string) -> {
        return Promise.reject("exportSelf() failure (somehow). " + err);
      });
  }

  /**
   * Build an announcement packet
   * @param desired_fleet optional fleet to assign
   */
  private CompletableFuture<TdiKeyStructureShape[]> bootstrapServer(String desired_fleet = "") {
    return this.datastore.get("netRole").then((n_r: string) -> {
      LOG.info("Building announcement message as appropriate for " + n_r);
      const n: Promise<TdiKeyStructure>[] = [];
      switch (n_r) {
        case 'F_S':
          n.push(
            this.impl.getPlatform().getKeystore()
              .getKeyByRole(TdiKeyStructureFlags.ROLE_F_S, desired_fleet)
              .thenCompose((key: TdiKeyStructure) -> {
                return this.exportKey(key.kid);
              })
          );
        /* falls through */
        case 'DEVICE':
          n.push(this.exportSelf());
          break;
        default:
          LOG.warn(
            "Key announcement is an unsupported operation for this role.",
            3
          );
          return Promise.reject(
            new FrameworkRuntimeException(
              "Key announcement is an unsupported operation for this role."
            )
          );
      }
      return Promise.all(n);
    });
  }

  /**
   * Helper fxn to build a provisioning return message.
   * @private
   * @param desired_fleet Optional fleetID we wish to join.
   */
  private CompletableFuture<TdiKeyStructureShape[]> provisionDevice(String desired_fleet = "") {
    LOG.trace("provisionDevice(): " + desired_fleet);
    const keysArray: Promise<TdiKeyStructureShape>[] = [];
    keysArray.push(
      this.impl.getPlatform().getKeystore().getKeyByRole(
        TdiKeyStructureFlags.ROLE_F_S,
        desired_fleet
      )
    );
    keysArray.push(
      this.impl.getPlatform().getKeystore().getKeyByRole(
        TdiKeyStructureFlags.ROLE_F_C,
        desired_fleet
      )
    );
    return Promise.all(keysArray).then((keys: TdiKeyStructure[]) -> {
      const n: Promise<TdiKeyStructure>[] = [];
      keys.forEach((key: TdiKeyStructure) -> {
        if (key) {
          n.push(this.exportKey(key.kid));
        }
      });
      return Promise.all(n);
    });
  }

  /**
   * Helper fxn to build a provisioning return message.
   * @private
   * @param desired_fleet Optional fleetID we wish to join.
   */
  private CompletableFuture<TdiKeyStructureShape[]> provisionFleet(String desired_fleet = "") {
    LOG.trace("provisionFleet(): " + desired_fleet);
    if (desired_fleet) {
      // A message that came in without this data is to be interpreted as a
      // request for a fleet ID to match the given key.
      return this.impl.getPlatform().getKeystore().getKeys()
        .thenCompose((keys: { [k: string]: TdiKeyStructure }) -> {
          // Search the keys for the first F_C key so we can grab its fleet.
          const n: Promise<TdiKeyStructure>[] = [];
          for (const idx in keys) {
            if (keys.hasOwnProperty(idx)) {
              LOG.debug(`Checking role on kid ${idx}  (${keys[idx].roleFlag})`);
              if (TdiKeyStructureFlags.ROLE_F_C === keys[idx].roleFlag) {
                n.push(this.exportKey(keys[idx].kid));
              }
            }
          }
          return Promise.all(n);
        });
    } else {
      return this.impl.getPlatform().getKeystore()
        .getKeyByRole(TdiKeyStructureFlags.ROLE_F_C, desired_fleet)
        .thenCompose((key: TdiKeyStructure) -> {
          const n: Promise<TdiKeyStructure>[] = [];
          if (key) {
            n.push(this.exportKey(key.kid));
          }
          return Promise.all(n);
        });
    }
  }

  /**
   * Build message to send our local notion of self to another actor on the network.
   */
  private TdiFlowArguments buildFlowAnnounce() {
    TdiFlowArguments flow = new TdiFlowArguments();
    flow.addOverrideSteps(Arrays.asList(
      // We over-ride this flow method from 'sign'.
      Constants.FlowMethods.handleInit
    ));
    flow.addMethod("handleInit", (data) -> {
//      handleInit: (): Promise<TdiCanonicalMessageShape> -> {
//        return this.imp.genMsg()
//          .then((msg: TdiCanonicalMessageShape): Promise<
//            TdiCanonicalMessageShape
//          > -> {
//            msg.claims['payload'] = {};
//            return Promise.resolve(msg);
//          })
//          .catch((err: Error) -> {
//            // TODO: Will this _ever_ fail?
//            return Promise.reject(`announce ${err} :: ${err.stack}`);
//          });
    });
    flow.addMethod("setClaims", (data) -> {
      TdiCanonicalMessageShape msgObj = (TdiCanonicalMessageShape) data;
//      setClaims: (msg: TdiCanonicalMessageShape): Promise<TdiCanonicalMessageShape> -> {
//        // TODO: This needs validation and is pretty ugly (side effects, waiting for promises to resolve in canonical)
//        // Clean up ASAP
//        msg.claims.payload['ntdiMsgType'] = 'kyxg';
//        // TODO: This is rather confusing. Should have helpers return promise.all instead of here
//        return this.provMsg()
//          .then((sdk_data: any) -> {
//            msg.claims.payload['ntdiData'] = sdk_data;
//            return this.imp.config.netRole !== 'DEVICE'
//              ? this.bootstrapServer(msg.currentProject)
//              : this.exportSelf();
//          })
//          .then((value: TdiKeyStructure[]) -> {
//            msg.claims.payload['ntdiData'].keys = value;
//            return Promise.resolve(msg);
//          });
    });
    return flow;
  }



  /**
   * Method to generate a signed provisioning payload
   * @param fleet Fleet to be provisioned against
   */
  private TdiFlowArguments buildFlowProvision() {
    TdiFlowArguments flow = new TdiFlowArguments();
    flow.addOverrideSteps(Arrays.asList(
      // We over-ride these two flow methods from 'sign'.
      // TODO: Audit... this may no longer be necessary.
      Constants.FlowMethods.handleInit,
      Constants.FlowMethods.Signing.setSigners
    ));

    flow.addMethod(Constants.FlowMethods.handleInit, (data) -> {
      TdiCanonicalMessageShape msgObj = (TdiCanonicalMessageShape) data;
    });
    flow.addMethod(Constants.FlowMethods.Signing.setSigners, (data) -> {
      TdiCanonicalMessageShape msgObj = (TdiCanonicalMessageShape) data;
    });
    flow.addMethod("setClaims", (data) -> {
      TdiCanonicalMessageShape msgObj = (TdiCanonicalMessageShape) data;
    });
//    {
//      handleInit: (fleet?: string): Promise<TdiCanonicalMessageShape> -> {
//        this.log('Building prov msg on fleet ' + fleet);
//        return this.imp
//          .genMsg(fleet)
//          .then((msg: TdiCanonicalMessageShape): Promise<
//            TdiCanonicalMessageShape
//          > -> {
//            // this.log('Built message:\n' + JSON.stringify(msg));
//            msg.claims['payload'] = {};
//            return Promise.resolve(msg);
//          })
//          .catch((err: Error) -> {
//            // TODO: Will this _ever_ fail?
//            return Promise.reject(`provision:${err} :: ${err.stack}`);
//          });
//      },
//      setSigners: (
//        msg: TdiCanonicalMessageShape
//      ): Promise<TdiCanonicalMessageShape> -> {
//        const keysArray: Promise<TdiKeyStructureShape>[] = [];
//        keysArray.push(
//          this.impl.getPlatform().getKeystore().getKeyByRole(
//            TdiKeyStructureFlags.ROLE_F_S,
//            msg.currentProject
//          )
//        );
//        keysArray.push(
//          this.impl.getPlatform().getKeystore().getKeyByRole(
//            TdiKeyStructureFlags.ROLE_F_C,
//            msg.currentProject
//          )
//        );
//        return Promise.all(keysArray).then(keys -> {
//          // TODO: Is this needed??  F_C Should not sign with self-key.
//          keys.forEach(key -> {
//            if (key.canSign) {
//              msg.signers.push(key);
//            }
//          });
//          return msg;
//        });
//      },
//      setClaims: (
//        msg: TdiCanonicalMessageShape
//      ): Promise<TdiCanonicalMessageShape> -> {
//        // TODO: validation and cleanup (this is scaffolding)
//        msg.claims.payload['ntdiMsgType'] = 'kyxg';
//
//        return this.provMsg().then((sdk_data: any) -> {
//          msg.claims.payload['ntdiData'] = sdk_data;
//          return this.datastore.get('netRole').then((n_r: string) -> {
//            this.log(
//              'Setting claims for provisioning message as appropriate for ' +
//                n_r
//            );
//            switch (n_r) {
//              case 'F_S':
//                return this.provisionDevice(
//                  msg.currentProject
//                ).then((value: TdiKeyStructure[]) -> {
//                  msg.claims.payload['ntdiData'].keys = value;
//                  return Promise.resolve(msg);
//                });
//              case 'F_C':
//                return this.provisionFleet(
//                  msg.currentProject
//                ).then((value: TdiKeyStructure[]) -> {
//                  msg.claims.payload['ntdiData'].keys = value;
//                  return Promise.resolve(msg);
//                });
//              default:
//                return Promise.reject(
//                  new FrameworkRuntimeException('This net role is not capable of provisioning')
//                );
//            }
//          });
//        });
//      }
    return flow;
    }


  /**
   * If a message has claims that reflect a provisioning message, and the key
   *   policy is satisfied, we add the contents to our keystore and deliver the
   *   application payload.
   */
  private TdiFlowArguments buildFlowAcceptKeys() {
    TdiFlowArguments flow = new TdiFlowArguments();
    // NOTE: This flow stands alone.
    flow.addMethod("open", (data) -> {
      TdiCanonicalMessageShape msgObj = (TdiCanonicalMessageShape) data;
      // TODO: Audit. Why is this here?
      //return msgObj;
    });
    flow.addMethod("check", (data) -> {
      TdiCanonicalMessageShape msgObj = (TdiCanonicalMessageShape) data;
//     check: (provPayload: any) -> {
//       if (this.verifyProvMsg(provPayload)) {
//         return Promise.resolve(provPayload);
//       } else {
//         return Promise.reject("Invalid Provisioning Format");
//       }
//     },
    });
    flow.addMethod("addKeys", (data) -> {
      TdiCanonicalMessageShape msgObj = (TdiCanonicalMessageShape) data;
//     addKeys: (provPayload: any) -> {
//       const proms = provPayload.keys.map((c: any) -> {
//         return this.impl.getPlatform().getKeystore().setKeyFromProvision(c).then(() -> {
//           return Promise.resolve(`Added Key ${c}`);
//         });
//       });
//       return Promise.all(proms).then(() -> {
//         return provPayload;
//       });
//     }
    });
    return flow;
  }


  /**
   * Helper to check for the presence of a valid fleet.
   */
  public CompletableFuture<Boolean> hasFleet(String kid = "") {
    return this.impl.getPlatform().getKeystore().getKeys()
      .thenCompose((List<TdiKeyStructureShape> keys) -> {
        // Search the keys for the first F_C key so we can grab its fleet.
        // const n: Promise<TdiKeyStructure>[] = [];
        for (const idx in keys) {
          if (keys.hasOwnProperty(idx)) {
            if (TdiKeyStructureFlags.ROLE_F_C === keys[idx].roleFlag) {
              if (keys[idx].fleet && keys[idx].fleet.length > 0) {
                return true;
              }
            }
          }
        }
        return false;
      });
  };

  /**
   * Helper to check for the presence of a valid fleet.
   */
  public CompletableFuture<TdiKeyStructureShape> genFleetKey() {
    return this.datastore.get("netRole")
      .thenCompose((String n_r) -> {
        switch (n_r) {
          case 'F_S':
            LOG.info("Generating new F_S key...");
            return this.impl.getPlatform().getKeystore().genKey(TdiKeyStructureFlags.ROLE_F_S);
          case 'F_C':
            const fleet_id = this.impl.getPlatform().util.makeUuid(); // FC is responsible for providing this.
            LOG.info(`Generating new F_C key for fleet ${fleet_id}`);
            return this.impl.getPlatform().getKeystore().genKey(
              TdiKeyStructureFlags.ROLE_F_C,
              fleet_id
            );
          default:
            throw new FrameworkRuntimeException("Unknown role for fleet key generation.");
        }
      });
  };

  /**
   * Helper to check for the presence of a valid fleet.
   */
  public CompletableFuture<TdiKeyStructureShape> getSelfKey() {
    return this.impl.getPlatform().getKeystore().getSelf().catch(err -> {
      return this.datastore.get("netRole").then((String n_r) -> {
        switch (n_r) {
          case 'DEVICE':
            LOG.info("Generating new DEVICE key...");
            return this.impl.getPlatform().getKeystore().genKey(
              TdiKeyStructureFlags.ROLE_DEVICE | TdiKeyStructureFlags.OUR_OWN
            );
          case 'F_S':
            LOG.info("Generating new F_S key...");
            return this.impl.getPlatform().getKeystore()
              .genKey(TdiKeyStructureFlags.ROLE_F_S)
              .thenCompose(() -> {
                LOG.info("Generating new SELF key...");
                return this.impl.getPlatform().getKeystore().genKey(
                  TdiKeyStructureFlags.ROLE_SERVER |
                    TdiKeyStructureFlags.OUR_OWN
                );
              });
          case 'F_C':
            LOG.info("Generating new SELF key...");
            return this.impl.getPlatform().getKeystore().genKey(
              TdiKeyStructureFlags.ROLE_COSERV | TdiKeyStructureFlags.OUR_OWN
            );
          default:
            break;
        }
        return Promise.reject(
          new FrameworkRuntimeException(
            "Could not find SELF key and failed to generate one, due to an invalid role specification."
          )
        );
      });
    });
  };

  /**
   * Helper to handle 'untrusted' provisioning requests
   * TODO: Rename to 'parseMsgOnly'
   * @param rawMsg A raw octet string from the network.
   */
  public CompletableFuture<?> tofuHandle(String rawMsg) {
    return this.impl.generateMsg(rawMsg)
      .thenApply((TdiCanonicalMessageShape msg) -> {
        msg.setReceivedMessage(rawMsg);
        return this.impl.mod("jws").c_unpack(msg);
      })
      .thenCompose((TdiCanonicalMessageShape msg) -> {
        return this.imp.mod("jwt").c_unpackClaims(msg);
      })
      .thenCompose((TdiCanonicalMessageShape msg) -> {
        return Promise.resolve(msg.claims.payload);
      })
      .exceptionally(throwable -> {
        String errMsg = "tofuHandle(): Unspecified error.";
        LOG.error(errMsg + ": " + throwable.getMessage());
        throw new FrameworkRuntimeException(errMsg);
      });
  }

  /**
   * Assign a new fleetID to existing unbound key material.
   * TODO: Explicitly prevent F_C from running this
   * @param new_fleet The ID of the new fleet.
   */
  public CompletableFuture<?> setFleet(String new_fleet = "") {
    LOG.info("setFleet will be setting the fleet to " + new_fleet);
    return this.impl.getPlatform().getKeystore().getKeyByRole(TdiKeyStructureFlags.ROLE_F_S)
      .then((fleet_key: TdiKeyStructure) -> {
        fleet_key.fleet = new_fleet;
        LOG.warn("Dropping prior notion of F_S key.");
        return this.impl.getPlatform().getKeystore().forgetKey(fleet_key.kid).then(() -> {
          // Forget the fleet key and re-add it with the given fleet_id.
          LOG.info("Re-adding F_S key with assigned fleet.");
          return this.impl.getPlatform().getKeystore().setKey(fleet_key);
        });
      })
      .catch(() -> {
        LOG.warn("setFleet(): No local F_S Signer found, continuing to Self");
        return Promise.resolve();
      })
      .then(() -> {
        return this.impl.getPlatform().getKeystore().getSelf().then(self_key -> {
          self_key.fleet = new_fleet;
          LOG.warn("Dropping prior notion of SELF key.");
          return this.impl.getPlatform().getKeystore().forgetKey(self_key.kid).then(() -> {
            // Forget the fleet key and re-add it with the given fleet_id.
            LOG.info("Re-adding SELF key with assigned fleet.");
            return this.impl.getPlatform().getKeystore().setKey(self_key).then(() -> {
              return;
            });
          });
        });
      });
  };
}
