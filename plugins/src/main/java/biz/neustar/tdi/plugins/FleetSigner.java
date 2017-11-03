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

package biz.neustar.tdi.plugins;

import biz.neustar.tdi.fw.implementation.TdiFlowArguments;
import biz.neustar.tdi.fw.implementation.TdiImplementationShape;
import biz.neustar.tdi.fw.keystructure.TdiKeyFlagsEnum;
import biz.neustar.tdi.fw.keystructure.TdiKeyStructure;
import biz.neustar.tdi.fw.keystructure.TdiKeyStructureShape;
import biz.neustar.tdi.fw.canonicalmessage.TdiCanonicalMessageShape;
import biz.neustar.tdi.fw.platform.TdiPlatformShape;
import biz.neustar.tdi.fw.platform.facet.crypto.TdiPlatformCryptoShape;
import biz.neustar.tdi.fw.platform.facet.data.TdiPlatformDataShape;
import biz.neustar.tdi.fw.platform.facet.keys.TdiPlatformKeysShape;
import biz.neustar.tdi.fw.platform.facet.time.TdiPlatformTimeShape;
import biz.neustar.tdi.fw.platform.facet.utils.TdiPlatformUtilsShape;
import biz.neustar.tdi.fw.plugin.TdiPluginBase;
import biz.neustar.tdi.fw.plugin.TdiPluginBaseFactory;
import biz.neustar.tdi.fw.wrapper.TdiSdkWrapperShape;
import biz.neustar.tdi.fw.exception.FrameworkRuntimeException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 */
public class FleetSigner extends TdiPluginBase {
  private static final Logger LOG = LoggerFactory.getLogger(FleetSigner.class);

  /**
   * This is populated by config and is the network location for our preferred cosigner.
   */
  private String baseURI = "";

  /**
   * Generates a new signature around a payload with the fleet key
   * @param payload Payload to be encased in claims
   */
  public Function<Object, CompletableFuture<Object>> fleetSign = null;
  /**
   * Appends a fleet signature to the provided JWS
   * @param jws The JWS to add a signature to
   */
  public Function<Object, CompletableFuture<Object>> fleetCosign = null;
  /**
   * Verify a server or device signature
   * @param jws The JWS to be verified
   */
  public Function<Object, CompletableFuture<Object>> fleetVerify = null;
  /**
   * Execute a full round-trip signing ceremony.  Returns a JWS ready for device consumption
   * @param payload
   */
  public Function<Object, CompletableFuture<Object>> fleetToDevice = null;
  /**
   * Execute a full round-trip verify ceremony.  Returns a payload if all verifications pass
   * @param JWS
   */
  public Function<Object, CompletableFuture<Object>> fleetFromDevice = null;
  /**
   * Signs a payload as a token
   * @param payload Payload to be sent
   */
  public Function<Object, CompletableFuture<Object>> signToken = null;


  public FleetSigner(TdiImplementationShape impl, TdiSdkWrapperShape sdkWrapper) {
    super("FleetSigner", impl, sdkWrapper);
    LOG.trace("FleetSigner:constructor()");
    // Now that we have refs to implementation and SDK, build up our API methods...
    // NOTE: Since these functions mutually reference, this setup order might be
    //   important. TODO: Determine this.
    this.fleetSign       = this.impl.buildApiFlow(this.buildFlowFleetSign(),    null);
    this.signToken       = this.impl.buildApiFlow(this.buildFlowSignToken(),    null);  // <--- No deferral to orginal flow.
    this.fleetCosign     = this.impl.buildApiFlow(this.buildFlowFleetCosign(),  null);  // _or: ['setSigners'],
    this.fleetVerify     = this.impl.buildApiFlow(this.buildFlowFleetVerify(),  null);  // _or: ['prepSignatures', 'handleReturn'],
    this.fleetToDevice   = this.impl.buildApiFlow(this.buildFlowFleetToDev(),   null);
    this.fleetFromDevice = this.impl.buildApiFlow(this.buildFlowFleetFromDev(), null);

//    this.fleetSign = this.impl.buildApiFlow(
//      this.sdkWrapper.defApi["sign"].flow, // Flow
//      this.sdkWrapper.defApi["sign"].flowFn(this.imp), // Default
//      // Override function below (same as a mod)
//      {
//      });
  }


  @Override
  public CompletableFuture<Boolean> init() {
    LOG.trace("FleetSigner:init()");
    // Need to have configuration describing the cosigner's network location.
    return this.validatePluginDataStore(Arrays.asList("cosigner"))
      .thenCompose((Boolean confValid) -> {
        if (confValid) {
          return this.getDataStore().get("cosigner")
            .thenApply((cosignerConf) -> {
              LOG.trace("Cosigner conf loaded.");
              Map<String, Object> cosignerConfMap = (Map<String, Object>) cosignerConf;
              if (cosignerConfMap.containsKey("baseURI")) {
                this.baseURI = cosignerConfMap.get("baseURI").toString();
                LOG.info("The selected cosigner is at " + this.baseURI);
                return true;
              }
              else {
                LOG.error("FleetSigner requires a cosigner.baseURI string.");
              }
              return false;
            });
        }
        else {
          LOG.error("FleetSigner requires a cosigner configuration.");
          return CompletableFuture.supplyAsync(() -> false);
        }
      });
  }


  //public void serverSign() {
  //  LOG.trace("FleetSigner:serverSign()");
  //}

  //public void serverVerify() {
  //  LOG.trace("FleetSigner:serverVerify()");
  //}



  private TdiFlowArguments buildFlowFleetSign() {
    TdiFlowArguments flow = new TdiFlowArguments();
    // TODO: Why is this replicated?
    flow.addOverrideSteps(Arrays.asList("setSigners"));
    flow.addMethod("setSigners", (data) -> {
      TdiCanonicalMessageShape msgObj = (TdiCanonicalMessageShape) data;
      msgObj.setSignatureType("compact");
      return this.impl.getPlatform().getKeystore().getKeyByRole(TdiKeyFlagsEnum.ROLE_F_S.getNumber(), msgObj.getCurrentProject())
        .thenApply((TdiKeyStructureShape key) -> {
          CompletableFuture<TdiCanonicalMessageShape> future = new CompletableFuture<>();
          if ((null != key) && key.canSign()) {
            msgObj.addSigner(key);
            future.complete(msgObj);
          }
          else {
            future.completeExceptionally(
              new FrameworkRuntimeException("setSigners() failed to find a signing key for either F_C or F_S.")
            );
          }
          return future;
        })
        .thenCompose(arg -> {
          return arg;
        })
        .exceptionally(throwable -> {
          String errMsg = "setSigners() failed.";
          LOG.error(errMsg);
          throw new FrameworkRuntimeException(errMsg);
        });
    });
    return flow;
  }

  private TdiFlowArguments buildFlowFleetCosign() {
    // TODO: Why is this replicated?
    //   I think it may be because of a composability mechanic that I don't yet
    //   know how to translate...
    //          this.currentApi.defApi['sign'].flow,
    //          this.currentApi.defApi['cosign'].flow,
    TdiFlowArguments flow = new TdiFlowArguments();
    flow.addOverrideSteps(Arrays.asList("setSigners"));
    flow.addMethod("setSigners", (data) -> {
      TdiCanonicalMessageShape msgObj = (TdiCanonicalMessageShape) data;
      msgObj.setSignatureType("compact");
      return this.impl.getPlatform().getKeystore().getKeyByRole(TdiKeyFlagsEnum.ROLE_F_S.getNumber(), msgObj.getCurrentProject())
        .thenApply((TdiKeyStructureShape key) -> {
          CompletableFuture<TdiCanonicalMessageShape> future = new CompletableFuture<>();
          if ((null != key) && key.canSign()) {
            msgObj.addSigner(key);
            future.complete(msgObj);
          }
          else {
            future.completeExceptionally(
              new FrameworkRuntimeException("setSigners() failed to find a signing key for either F_C or F_S.")
            );
          }
          return future;
        })
        .thenCompose(arg -> {
          return arg;
        })
        .exceptionally(throwable -> {
          String errMsg = "setSigners() failed.";
          LOG.error(errMsg);
          throw new FrameworkRuntimeException(errMsg);
        });
    });
    return flow;
  }

  private TdiFlowArguments buildFlowFleetVerify() {
    TdiFlowArguments flow = new TdiFlowArguments();
    // Prevent the default behavior from taking place for these phases.
    flow.addOverrideSteps(Arrays.asList("prepSignatures", "handleReturn"));
    flow.addMethod("prepSignatures", (data) -> {
      TdiCanonicalMessageShape msgObj = (TdiCanonicalMessageShape) data;
      // get kid values for signatures (prune signatures and check project keystore scope?)
      int verify_count = 0;
      //    const promise_queue = [];
      //    for (const i of msg.signaturesToVerify) {
      //      const t_kid: string = i['parsedHeader']['kid'];
      //      promise_queue.push(
      //        this.imp.pf.keys
      //          .getKey(t_kid)
      //          .then((t_key: TdiKeyStructure) => {
      //            // We have knowledge of the key.
      //            if (t_key.fleet !== '') {
      //              msg.currentProject = t_key.fleet;
      //            }
      //            if (t_key.invalid) {
      //              msg.verifiers = []; // No point to verifying.
      //              return Promise.reject(
      //                `prepSignatures(): Key ${t_key.kid} was marked invalid.`
      //              );
      //            }
      //            msg.verifiers.push(t_key);
      //            verify_count++;
      //          })
      //          .catch((err: string) => {
      //            return false;
      //          })
      //      );
      //    }
      //    return Promise.all(promise_queue).then(() => {
      //      if (msg.currentProject === '') {
      //        this.log('fleetVerify(): WARNING: No currentProject set');
      //      }
      //      if (verify_count !== 0) {
      //        // NOTE: We are not checking for roles here.
      //        return Promise.resolve(msg);
      //      } else {
      //        return Promise.reject(
      //          'prepSignatures(): No known keys to verify against.'
      //        );
      //      }
      //    });
      //  },
      return CompletableFuture.allOf()
        .thenApply((arg) -> {
          CompletableFuture<TdiCanonicalMessageShape> future = new CompletableFuture<>();
          if (0 == msgObj.getCurrentProject().length()) {
            LOG.warn("fleetVerify(): WARNING: No currentProject set");
          }
          if (0 != verify_count) {
            return future.complete(msgObj);
          }
          else {
            future.completeExceptionally(
              new FrameworkRuntimeException("prepSignatures(): No known keys to verify against.")
            );
          }
          return future;
        })
        .exceptionally(throwable -> {
          String errMsg = "sendToCosigner() failed.";
          LOG.error(errMsg);
          throw new FrameworkRuntimeException(errMsg);
        });
    });
    flow.addMethod("handleReturn", (data) -> {
      TdiCanonicalMessageShape msgObj = (TdiCanonicalMessageShape) data;
      return CompletableFuture.completedFuture(msgObj.getRawPayload());
    });
    return flow;
  }

  private TdiFlowArguments buildFlowFleetToDev() {
    TdiFlowArguments flow = new TdiFlowArguments();
    // TODO: These were NOT in an array named _or[]. Just an array.
    //flow.addOverrideSteps(Arrays.asList("sign", "sendToCosigner", "validateCosigner", "fleetSign"));
    flow.addMethod("sign", (data) -> {
      return this.sdkWrapper.api("sign").apply(data);
    });
    flow.addMethod("sendToCosigner", (data) -> {
      String jwsPayload = (String) data;
      return this.impl.getPlatform().getKeystore().getSelfKey()
        .thenApply((TdiKeyStructureShape self) -> {
          CompletableFuture<TdiCanonicalMessageShape> future = new CompletableFuture<>();
          self.getFleetId();
          self.getKeyId();
          //      return rp({
          //        method: 'POST',
          //        uri: `${this.baseURI}/projects/${fleet}/cosign_for_server/${kid}`,
          //        body: jwsPayload,
          //        headers: {
          //          'content-type': 'application/JOSE+JSON',
          //          Accept: 'application/JOSE+JSON',
          //          encoding: 'utf8'
          //          // Bearer: 'jwt '
          //        }
          //      });
          return future;
        })
        .thenCompose(arg -> {
          return arg;
        })
        .exceptionally(throwable -> {
          String errMsg = "sendToCosigner() failed.";
          LOG.error(errMsg);
          throw new FrameworkRuntimeException(errMsg);
        });
    });
    flow.addMethod("validateCosigner", (data) -> {
      CompletableFuture<TdiCanonicalMessageShape> future = new CompletableFuture<>();
      String requestBody = (String) data;
      if ((null != requestBody) && (0 < requestBody.length())) {
        // TODO: Might-should have better check?
        //      return this.fleetVerify(
        //        requestBody
        //      ).then((vCanon: TdiCanonicalMessageShape) => {
        //        return requestBody;
        //      });
        return this.fleetVerify.apply(requestBody);
      }
      else {
        future.completeExceptionally(
          new FrameworkRuntimeException("No response body in cosigner reply.")
        );
      }
      return future;
    });
    flow.addMethod("fleetSign", (data) -> {
      TdiCanonicalMessageShape verifiedJWS = (TdiCanonicalMessageShape) data;
      return this.fleetCosign.apply(verifiedJWS);
    });
    return flow;
  }


  private TdiFlowArguments buildFlowFleetFromDev() {
    TdiFlowArguments flow = new TdiFlowArguments();
    // TODO: These were NOT in an array named _or[]. Just an array.
    //flow.addOverrideSteps(Arrays.asList("sendToCosigner", "validateCosigner", "fleetSign"));
    flow.addMethod("sendToCosigner", (data) -> {
      //  sendToCosigner: (jwsPayload: string) => {
      //    return this.imp.pf.keys.getSelf().then((self: TdiKeyStructure) => {
      //      const fleet = self.fleet;
      //      const kid = JSON.parse(
      //        this.imp.pf.util.b64URLDecode(JSON.parse(jwsPayload).payload)
      //      ).iss;
      //      return rp({
      //        method: 'POST',
      //        uri: `${this
      //          .baseURI}/projects/${fleet}/cosign_for_edge_device/${kid}`,
      //        body: jwsPayload,
      //        // json: true,
      //        headers: {
      //          'content-type': 'application/JOSE+JSON',
      //          Accept: 'application/JOSE+JSON',
      //          encoding: 'utf8'
      //          // Bearer: 'jwt '
      //        }
      //      });
      //    });
      //  },
      TdiCanonicalMessageShape verifiedJWS = (TdiCanonicalMessageShape) data;
      return this.fleetCosign.apply(verifiedJWS);
    });
    flow.addMethod("validateCosigner", (data) -> {
      //  validateCosigner: (requestBody: string) => {
      //    if (requestBody) {
      //      return this.fleetVerify(
      //        requestBody
      //      ).then((vCanon: TdiCanonicalMessageShape) => {
      //        return requestBody;
      //      });
      //    } else {
      //      return Promise.reject(
      //        new Error('No response body in cosigner reply')
      //      );
      //    }
      //  },
      TdiCanonicalMessageShape verifiedJWS = (TdiCanonicalMessageShape) data;
      return this.fleetCosign.apply(verifiedJWS);
    });
    flow.addMethod("fleetSign", (data) -> {
      TdiCanonicalMessageShape msgObj = (TdiCanonicalMessageShape) data;
      return this.fleetCosign.apply(msgObj);
    });
    return flow;
  }

/**
 * Signs a token if a ROLE_EXTERN key is found in the keystore relevent to the project
 */
private TdiFlowArguments buildFlowSignToken() {
    TdiFlowArguments flow = new TdiFlowArguments();
    // NOTE: If we want to cause the fleet server's SELF key to sign
    //   messages, we would remove 'setSigners' from the array below.
    // TODO: Why is this replicated?
    flow.addOverrideSteps(Arrays.asList("setSigners"));
    flow.addMethod("setSigners", (data) -> {
      TdiCanonicalMessageShape msgObj = (TdiCanonicalMessageShape) data;
      msgObj.setSignatureType("compact");
      return this.impl.getPlatform().getKeystore().getKeyByRole(TdiKeyFlagsEnum.ROLE_EXTERN.getNumber(), msgObj.getCurrentProject())
        .thenApply((TdiKeyStructureShape key) -> {
          CompletableFuture<TdiCanonicalMessageShape> future = new CompletableFuture<>();
          if ((null != key) && key.canSign()) {
            msgObj.addSigner(key);
            future.complete(msgObj);
          }
          else {
            future.completeExceptionally(
              new FrameworkRuntimeException("setSigners() failed to find a signing key for either F_C or F_S.")
            );
          }
          return future;
        })
        .thenCompose(arg -> {
          return arg;
        })
        .exceptionally(throwable -> {
          String errMsg = "setSigners() failed.";
          LOG.error(errMsg);
          throw new FrameworkRuntimeException(errMsg);
        });
    });
    return flow;
  }
}
