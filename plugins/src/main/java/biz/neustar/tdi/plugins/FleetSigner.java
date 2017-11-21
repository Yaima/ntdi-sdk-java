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
 *
 */
public class FleetSigner extends TdiPluginBase {
  private static final Logger LOG = LoggerFactory.getLogger(FleetSigner.class);

  /**
   * This is populated by config and is the network location for our preferred
   * cosigner.
   */
  private String baseURI = "";

  /**
   * Execute a full round-trip signing ceremony. Returns a JWS ready for device
   * consumption
   *
   */
  public Function<String, CompletableFuture<TdiCanonicalMessageShape>> fleetToDevice = null;

  /**
   * Execute a full round-trip verify ceremony. Returns a payload if all
   * verifications pass
   * TODO: This will return a CompletableFuture<TdiCanonicalMessageShape> very soon.
   *    Don't code against it this way
   *
   */
  public Function<String, CompletableFuture<String>> fleetFromDevice = null;

  /**
   * Appends the server's signature to the provided JWS
   *
   *            The JWS to add a signature to
   */
  private Function<String, CompletableFuture<TdiCanonicalMessageShape>> cosign = null;

  /**
   * Appends a fleet signature to the provided JWS
   *
   *            The JWS to add a signature to
   */
  private Function<String, CompletableFuture<TdiCanonicalMessageShape>> fleetCosign = null;

  /**
   * Verify a server or device signature
   *
   *            The JWS to be verified
   */
  private Function<String, CompletableFuture<TdiCanonicalMessageShape>> fleetVerify = null;

  public FleetSigner(TdiImplementationShape impl, TdiSdkWrapperShape sdkWrapper) {
    super("FleetSigner", impl, sdkWrapper);
    LOG.trace("FleetSigner:constructor()");

    // private flows
    this.cosign = this.impl.buildApiFlow(
      this.sdkWrapper.getDefaultFlows().get(Constants.Api.CosignFlow),
      null
    );
    this.fleetCosign = this.impl.buildApiFlow(
      // Piggy-backs on 'setSigners' from default sign flow.
      this.sdkWrapper.getDefaultFlows().get(Constants.Api.CosignFlow),
      this.buildFlowFleetCosign()
    );
    this.fleetVerify = this.impl.buildApiFlow(
      // Piggy-backs on 'prepSignatures', 'handleReturn' from default verify flow.
      this.sdkWrapper.getDefaultFlows().get(Constants.Api.VerifyFlow),
      this.buildFlowFleetVerify()
    );

    // public flows
    this.fleetToDevice = this.impl.buildApiFlow(
      this.buildFlowFleetToDev(),
      null
    );
    this.fleetFromDevice = this.impl.buildApiFlow(
      this.sdkWrapper.getDefaultFlows().get(Constants.Api.VerifyGeneralFlow),
      this.buildFlowFleetFromDev()
    );
  }

<<<<<<< HEAD
  @Override
  @SuppressWarnings("unchecked")
  public CompletableFuture<Boolean> init() {
    LOG.trace("FleetSigner:init()");
    return this.validatePluginDataStore(Arrays.asList("cosigner"))
      .thenCompose((Boolean confValid) -> {
        if (confValid) {
          return this.getDataStore().get("cosigner")
            .thenApply((cosignerConf) -> {
              LOG.info("Cosigner conf loaded.");
              // NOTE: Map cast is internal and error-checked upstream.
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
=======
	private TdiFlowArguments buildFlowFleetFromDev() {
		TdiFlowArguments flow = new TdiFlowArguments();
		flow.addOverrideSteps(Arrays.asList("sendToCosigner", "validateCosigner", "fleetSign"));
		flow.addMethod("sendToCosigner", (data) -> {
			String outboundJWS = (String) data;
			return this.impl.getPlatform().getKeystore().getSelfKey()
			  .thenCompose((TdiKeyStructureShape self) -> {
			  	CompletableFuture<String> future = new CompletableFuture<>();
			  	String fleet = self.getFleetId();
			  	try {
			  		String kid = this.fetchIssFromJwsString(outboundJWS);
			  		future.complete(this.httpPostToCosigner("cosign_for_edge_device", fleet, kid, outboundJWS));
			  	} catch (IOException e) {
			  		future.completeExceptionally(new FrameworkRuntimeException(e.toString()));
			  	} catch (Exception e) {
			  		future.completeExceptionally(new FrameworkRuntimeException(e.toString()));
			  	}
			  	return future;
			  })
			  .exceptionally(throwable -> {
			  	String errMsg = "sendToCosigner() failed: " + throwable.getMessage();
			  	LOG.error(errMsg);
			  	throw new FrameworkRuntimeException(errMsg);
			  });
		});
		flow.addMethod("validateCosigner", (data) -> {
			String requestBody = (String) data;
			return this.fleetVerify.apply(requestBody).thenCompose((msg_str) -> {
				TdiCanonicalMessageShape cosignedJWS = (TdiCanonicalMessageShape) msg_str;
				// NOTE: Not _strictly_ what the TS package does, but should be equivalent.
				return CompletableFuture.completedFuture(cosignedJWS);
			})
			.exceptionally(throwable -> {
				String errMsg = "validateCosigner() failed: " + throwable.getMessage();
				LOG.error(errMsg);
				throw new FrameworkRuntimeException(errMsg);
			});
		});
		flow.addMethod("fleetSign", (data) -> {
			TdiCanonicalMessageShape msgObj = (TdiCanonicalMessageShape) data;
			String verifiedJWS = msgObj.getReceivedMessage();
			return this.fleetCosign.apply(verifiedJWS);
		});
		return flow;
	}
>>>>>>> 849c3ef... More async cleanup.

  // override Constants.Api.CosignFlow
  private TdiFlowArguments buildFlowFleetCosign() {
    TdiFlowArguments flow = new TdiFlowArguments();
    flow.addOverrideSteps(Arrays.asList("setSigners"));
    flow.addMethod("setSigners", (data) -> {
      TdiCanonicalMessageShape msgObj = (TdiCanonicalMessageShape) data;
      return this.impl.getPlatform().getKeystore()
        .getKeyByRole(TdiKeyFlagsEnum.ROLE_F_S.getNumber(), msgObj.getCurrentProject())
        .thenCompose((TdiKeyStructureShape key) -> {
          CompletableFuture<TdiCanonicalMessageShape> future = new CompletableFuture<>();
          if ((null != key) && key.canSign()) {
            msgObj.addSigner(key);
            future.complete(msgObj);
          } else {
            future.completeExceptionally(new FrameworkRuntimeException(
                "setSigners() failed to find a signing key for either F_C or F_S."));
          }
          return future;
        })
        .exceptionally(throwable -> {
          String errMsg = "setSigners() failed.";
          LOG.error(errMsg + ": " + throwable.getMessage());
          throw new FrameworkRuntimeException(errMsg);
        });
    });
    return flow;
  }

  @SuppressWarnings("unchecked")
  private TdiFlowArguments buildFlowFleetVerify() {
    TdiFlowArguments flow = new TdiFlowArguments();
    // Prevent the default behavior from taking place for these phases.
    flow.addOverrideSteps(Arrays.asList("prepSignatures", "handleReturn"));
    flow.addMethod("prepSignatures", (data) -> {
      TdiCanonicalMessageShape msgObj = (TdiCanonicalMessageShape) data;
      List<CompletableFuture<TdiKeyStructureShape>> promise_queue = new ArrayList<>();
      for (Object s : msgObj.getSignaturesToVerify().toArray()) {
        TdiJwsSignature sig = (TdiJwsSignature) s;
        if (null != sig.parsedHeader) {
          if (0 < sig.parsedHeader.kid.length()) {
            promise_queue.add(this.impl.getPlatform().getKeystore().getKey(sig.parsedHeader.kid)
              .thenApply((TdiKeyStructureShape t_key) -> {
                // We have knowledge of the key.
                if (0 < t_key.getFleetId().length()) {
                  msgObj.setCurrentProject(t_key.getFleetId());
                }
                if (t_key.isInvalid()) {
                  msgObj.clearVerifiers(); // No point to verifying.
                  LOG.warn("prepSignatures(): Key " + t_key.getKeyId() + " was marked invalid.");
                  return null;
                }
                msgObj.addVerifier(t_key);
                return t_key;
              }));
          }
        }
      }
      return CompletableFuture.allOf(promise_queue.toArray(new CompletableFuture<?>[0]))
        .thenCompose((arg) -> {
          CompletableFuture<TdiCanonicalMessageShape> future = new CompletableFuture<>();
          if (0 == msgObj.getCurrentProject().length()) {
            LOG.warn("fleetVerify(): WARNING: No currentProject set");
          }
          int verify_count = 0;

          // NOTE: This cast is safe, since it is sourced from local scope, and
          //   its parameterized type is know with certainty.
          // TODO: Using Iterators from the List would probably clean this up a good deal.
          for (
            CompletableFuture<TdiKeyStructureShape> cffk :
            (CompletableFuture<TdiKeyStructureShape>[]) promise_queue.toArray(new CompletableFuture<?>[0])
          ) {
            if (null != cffk) {
              verify_count++;
            }
          }
          if (0 != verify_count) {
            // NOTE: We are not checking for roles here.
            future.complete(msgObj);
          } else {
            future.completeExceptionally(new FrameworkRuntimeException(
                "prepSignatures(): No known keys to verify against."));
          }
          return future;
        })
        .exceptionally(throwable -> {
          String errMsg = "sendToCosigner() failed.";
          LOG.error(errMsg + ": " + throwable.getMessage());
          throw new FrameworkRuntimeException(errMsg);
        });
    });
    flow.addMethod(Constants.FlowMethods.handleReturn, (data) -> {
      TdiCanonicalMessageShape msgObj = (TdiCanonicalMessageShape) data;
      return CompletableFuture.completedFuture(msgObj);
      // TODO: API rework made this needless.
      //return CompletableFuture.completedFuture(msgObj).thenApply(a -> a);
    });
    return flow;
  }

  private TdiFlowArguments buildFlowFleetToDev() {
    TdiFlowArguments flow = new TdiFlowArguments();
    // TODO: Fix later.
    // flow.addMethod("sign", (data) -> {
    //   return this.sdkWrapper.api("sign").apply(data);
    // });
    flow.addMethod("sendToCosigner", (data) -> {
      String jwsPayload = (String) data;
      return this.impl.getPlatform().getKeystore().getSelfKey()
        .thenCompose((TdiKeyStructureShape self) -> {
          CompletableFuture<String> future = new CompletableFuture<>();
          String fleet = self.getFleetId();
          try {
            future.complete(this.httpPostToCosigner(fleet, jwsPayload));
          } catch (Exception e) {
            future.completeExceptionally(new FrameworkRuntimeException(e.toString()));
          }
          return future;
        })
        .exceptionally(throwable -> {
          String errMsg = "sendToCosigner() failed.";
          LOG.error(errMsg + ": " + throwable.getMessage());
          throw new FrameworkRuntimeException(errMsg);
        });
    });
    flow.addMethod("validateCosigner", (data) -> {
      String requestBody = (String) data;
      return this.fleetVerify.apply(requestBody).thenCompose((msg_str) -> {
        TdiCanonicalMessageShape cosignedJWS = (TdiCanonicalMessageShape) msg_str;
        // NOTE: Not _strictly_ what the TS package does, but should be equivalent.
        return CompletableFuture.completedFuture(cosignedJWS);
      })
      .exceptionally(throwable -> {
        LOG.error("validateCosigner() failed.");
        throw new FrameworkRuntimeException(throwable.getMessage());
      });
    });
    flow.addMethod("fleetSign", (data) -> {
      String verifiedJWS = ((TdiCanonicalMessageShape) data).getReceivedMessage();
      return this.fleetCosign.apply(verifiedJWS);
    });
    return flow;
  }

  // override from Constants.Api.VerifyGeneralFlow
  private TdiFlowArguments buildFlowFleetFromDev() {
    TdiFlowArguments flow = new TdiFlowArguments();
    flow.addOverrideSteps(Arrays.asList(Constants.FlowMethods.Verifying.afterVerify, Constants.FlowMethods.handleReturn));
    flow.addMethod(Constants.FlowMethods.Verifying.afterVerify, m -> {
      TdiCanonicalMessageShape validatedMsg = (TdiCanonicalMessageShape)m;

      return this.cosign.apply(validatedMsg.getReceivedMessage())
      .thenApply(sm -> {
        TdiCanonicalMessageShape signedMsg = (TdiCanonicalMessageShape)sm;
        return this.impl.getPlatform().getKeystore().getSelfKey().thenCompose((TdiKeyStructureShape self) -> {
          CompletableFuture<String> future = new CompletableFuture<>();
          String fleet = self.getFleetId();
          try {
            future.complete(this.httpPostToCosigner(fleet, signedMsg.getBuiltMessage()));
          } catch (Exception e) {
            future.completeExceptionally(new FrameworkRuntimeException(e.toString()));
          }
          return future;
        });
      })
      // .thenCompose(a -> a)
      .thenApply((response) -> {
        CompletableFuture<String> future = new CompletableFuture<>();
        try {
          String cosignedMsg = (String) response.get();
          return this.fleetVerify.apply(cosignedMsg).thenCompose((msg_str) -> {
            TdiCanonicalMessageShape verifiedMsg = (TdiCanonicalMessageShape) msg_str;
            // NOTE: Not _strictly_ what the TS package does, but should be equivalent.
            return CompletableFuture.completedFuture(verifiedMsg);
          })
          // .thenCompose(a -> a)
          .exceptionally(throwable -> {
            String errMsg = "validateCosigner() failed.";
            LOG.error(errMsg + ": " + throwable.getMessage());
            throw new FrameworkRuntimeException(errMsg);
          });
        } catch (Exception e) {
          future.completeExceptionally(new FrameworkRuntimeException(e.toString()));
        }
        return future;
      })
      .thenCompose(a -> a)
      .exceptionally(throwable -> {
        String errMsg = "FleetFromDev() failed in afterVerify: " + throwable.getMessage();
        LOG.error(errMsg);
        throw new FrameworkRuntimeException(errMsg);
      });
    });
    flow.addMethod(Constants.FlowMethods.handleReturn, (msg) -> {
      LOG.trace("handleReturn, msg={}", msg);
      TdiCanonicalMessageShape tdiMsg = (TdiCanonicalMessageShape) msg;
      String payload = (String) tdiMsg.getClaims().payload;  // TODO: This API will change very soon.
      return CompletableFuture.completedFuture(payload);
    });
    return flow;
  }

  /*
   * Blocks until a response is received.
   */
  private String httpPostToCosigner(String method, String fleet, String kid, String payload) {
    HttpURLConnection con = null;
    StringBuffer resp = new StringBuffer();
    int payload_len = payload.getBytes().length;
    LOG.trace("Sending a " + payload_len + " byte payload for cosigning.");
    try {
      URL url = new URL(this.baseURI + "/projects/" + fleet + "/cosign");
      LOG.debug("Calling out to cosigner: " + url.toString());
      con = (HttpURLConnection) url.openConnection();
      con.setRequestMethod("POST");
      con.setRequestProperty("Content-Type", "application/JOSE+JSON");
      con.setRequestProperty("Accept", "application/JOSE+JSON");
      con.setRequestProperty("encoding", "utf8");
      con.setRequestProperty("Content-Length", Integer.toString(payload_len));
      con.setRequestProperty("Content-Language", "en-US");
      con.setUseCaches(false);
      con.setDoInput(true);
      con.setDoOutput(true);
      DataOutputStream tx = new DataOutputStream(con.getOutputStream());
      tx.writeBytes(payload);
      tx.flush();
      tx.close();
      BufferedReader rx = new BufferedReader(new InputStreamReader(con.getInputStream()));
      String line;
      while ((line = rx.readLine()) != null) {
        resp.append(line);
        resp.append('\r');
      }
      rx.close();
      if (con.getResponseCode() != 200) {  // TODO: This will be dead code soon.
        LOG.warn("co-signer error, code={}", con.getResponseCode());
        throw new FrameworkRuntimeException("Core refused to co-sign");
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    if (null != con) {
      con.disconnect();
    }
    return resp.toString();
  }
}
