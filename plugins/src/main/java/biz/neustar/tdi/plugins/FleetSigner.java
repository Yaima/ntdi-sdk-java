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
	 * Generates a new signature around a payload with the fleet key
	 *
	 * @param payload
	 *            Payload to be encased in claims
	 */
	public Function<String, CompletableFuture<TdiCanonicalMessageShape>> fleetSign = null;
	/**
	 * Appends a fleet signature to the provided JWS
	 *
	 * @param jws
	 *            The JWS to add a signature to
	 */
	public Function<String, CompletableFuture<TdiCanonicalMessageShape>> fleetCosign = null;
	/**
	 * Verify a server or device signature
	 *
	 * @param jws
	 *            The JWS to be verified
	 */
	public Function<String, CompletableFuture<TdiCanonicalMessageShape>> fleetVerify = null;
	/**
	 * Execute a full round-trip signing ceremony. Returns a JWS ready for device
	 * consumption
	 *
	 * @param payload
	 */
	public Function<String, CompletableFuture<TdiCanonicalMessageShape>> fleetToDevice = null;
	/**
	 * Execute a full round-trip verify ceremony. Returns a payload if all
	 * verifications pass
	 *
	 * @param JWS
	 */
	public Function<String, CompletableFuture<TdiCanonicalMessageShape>> fleetFromDevice = null;
	/**
	 * Signs a payload as a token
	 *
	 * @param payload
	 *            Payload to be sent
	 */
	public Function<String, CompletableFuture<TdiCanonicalMessageShape>> signToken = null;

	public FleetSigner(TdiImplementationShape impl, TdiSdkWrapperShape sdkWrapper) {
		super("FleetSigner", impl, sdkWrapper);
		LOG.trace("FleetSigner:constructor()");
		this.fleetSign = this.impl.buildApiFlow(this.sdkWrapper.getDefaultFlows().get("SignFlow"), this.buildFlowFleetSign());
		this.signToken = this.impl.buildApiFlow(this.sdkWrapper.getDefaultFlows().get("SignFlow"), this.buildFlowSignToken()); // <--- No deferral to orginal flow.
		this.fleetCosign = this.impl.buildApiFlow(this.sdkWrapper.getDefaultFlows().get("CosignFlow"), this.buildFlowFleetCosign()); // _or: ['setSigners'],
		this.fleetVerify = this.impl.buildApiFlow(this.sdkWrapper.getDefaultFlows().get("VerifyFlow"), this.buildFlowFleetVerify()); // _or: ['prepSignatures',
																						// 'handleReturn'],
		this.fleetToDevice = this.impl.buildApiFlow(this.buildFlowFleetToDev(), null);
		this.fleetFromDevice = this.impl.buildApiFlow(this.buildFlowFleetFromDev(), null);

	}

	@Override
	public CompletableFuture<Boolean> init() {
		LOG.trace("FleetSigner:init()");
		return this.validatePluginDataStore(Arrays.asList("cosigner")).thenCompose((Boolean confValid) -> {
			if (confValid) {
				return this.getDataStore().get("cosigner").thenApply((cosignerConf) -> {
					LOG.trace("Cosigner conf loaded.");
					Map<String, Object> cosignerConfMap = (Map<String, Object>) cosignerConf;
					if (cosignerConfMap.containsKey("baseURI")) {
						this.baseURI = cosignerConfMap.get("baseURI").toString();
						LOG.info("The selected cosigner is at " + this.baseURI);
						return true;
					} else {
						LOG.error("FleetSigner requires a cosigner.baseURI string.");
					}
					return false;
				});
			} else {
				LOG.error("FleetSigner requires a cosigner configuration.");
				return CompletableFuture.supplyAsync(() -> false);
			}
		});
	}

	private TdiFlowArguments buildFlowFleetSign() {
		TdiFlowArguments flow = new TdiFlowArguments();
		flow.addOverrideSteps(Arrays.asList("setSigners"));
		flow.addMethod("setSigners", (data) -> {
			TdiCanonicalMessageShape msgObj = (TdiCanonicalMessageShape) data;
			msgObj.setSignatureType("compact");
			return this.impl.getPlatform().getKeystore()
					.getKeyByRole(TdiKeyFlagsEnum.ROLE_F_S.getNumber(), msgObj.getCurrentProject())
					.thenApply((TdiKeyStructureShape key) -> {
						CompletableFuture<TdiCanonicalMessageShape> future = new CompletableFuture<>();
						if ((null != key) && key.canSign()) {
							msgObj.addSigner(key);
							future.complete(msgObj);
						} else {
							future.completeExceptionally(new FrameworkRuntimeException(
									"setSigners() failed to find a signing key for either F_C or F_S."));
						}
						return future;
					}).thenCompose(arg -> {
						return arg;
					}).exceptionally(throwable -> {
						String errMsg = "setSigners() failed.";
						LOG.error(errMsg + ": " + throwable.getMessage());
						throw new FrameworkRuntimeException(errMsg);
					});
		});
		return flow;
	}

	private TdiFlowArguments buildFlowFleetCosign() {
		TdiFlowArguments flow = new TdiFlowArguments();
		flow.addOverrideSteps(Arrays.asList("setSigners"));
		flow.addMethod("setSigners", (data) -> {
			TdiCanonicalMessageShape msgObj = (TdiCanonicalMessageShape) data;
			msgObj.setSignatureType("compact");
			return this.impl.getPlatform().getKeystore()
					.getKeyByRole(TdiKeyFlagsEnum.ROLE_F_S.getNumber(), msgObj.getCurrentProject())
					.thenApply((TdiKeyStructureShape key) -> {
						CompletableFuture<TdiCanonicalMessageShape> future = new CompletableFuture<>();
						if ((null != key) && key.canSign()) {
							msgObj.addSigner(key);
							future.complete(msgObj);
						} else {
							future.completeExceptionally(new FrameworkRuntimeException(
									"setSigners() failed to find a signing key for either F_C or F_S."));
						}
						return future;
					}).thenCompose(arg -> {
						return arg;
					}).exceptionally(throwable -> {
						String errMsg = "setSigners() failed.";
						LOG.error(errMsg + ": " + throwable.getMessage());
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
										LOG.info("prepSignatures(): Key " + t_key.getKeyId() + " was marked invalid.");
										return null;
									}
									msgObj.addVerifier(t_key);
									return t_key;
								}));
					}
				}
			}
			return CompletableFuture.allOf(promise_queue.toArray(new CompletableFuture<?>[0]))
					.thenApply((arg) -> {
						CompletableFuture<TdiCanonicalMessageShape> future = new CompletableFuture<>();
						if (0 == msgObj.getCurrentProject().length()) {
							LOG.warn("fleetVerify(): WARNING: No currentProject set");
						}
						int verify_count = 0;

						for (CompletableFuture<TdiKeyStructureShape> cffk : (CompletableFuture<TdiKeyStructureShape>[]) promise_queue.toArray(new CompletableFuture<?>[0])) {
							if (null != cffk) {
								verify_count++;
							}
						}
						if (0 != verify_count) {
							// NOTE: We are not checking for roles here.
							return future.complete(msgObj);
						} else {
							future.completeExceptionally(new FrameworkRuntimeException(
									"prepSignatures(): No known keys to verify against."));
						}
						return future;
					}).exceptionally(throwable -> {
						String errMsg = "sendToCosigner() failed.";
						LOG.error(errMsg + ": " + throwable.getMessage());
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
//		flow.addMethod("sign", (data) -> {
//			return this.sdkWrapper.api("sign").apply(data);
//		});
		flow.addMethod("sendToCosigner", (data) -> {
			String jwsPayload = (String) data;
			return this.impl.getPlatform().getKeystore().getSelfKey().thenApply((TdiKeyStructureShape self) -> {
				CompletableFuture<String> future = new CompletableFuture<>();
				String fleet = self.getFleetId();
				String kid = self.getKeyId();
				try {
					future.complete(this.httpPostToCosigner("cosign_for_server", fleet, kid, jwsPayload));
				} catch (Exception e) {
					future.completeExceptionally(new FrameworkRuntimeException(e.toString()));
				}
				return future;
			}).thenCompose(arg -> {
				return arg;
			}).exceptionally(throwable -> {
				String errMsg = "sendToCosigner() failed.";
				LOG.error(errMsg + ": " + throwable.getMessage());
				throw new FrameworkRuntimeException(errMsg);
			});
		});
		flow.addMethod("validateCosigner", (data) -> {
			String requestBody = (String) data;
			return this.fleetVerify.apply(requestBody).thenApply((msg_str) -> {
				TdiCanonicalMessageShape cosignedJWS = (TdiCanonicalMessageShape) msg_str;
				// NOTE: Not _strictly_ what the TS package does, but should be equivalent.
				return CompletableFuture.completedFuture(cosignedJWS.getRawPayload());
			}).exceptionally(throwable -> {
				LOG.error("validateCosigner() failed.");
				throw new FrameworkRuntimeException(throwable.getMessage());
			});
		});
		flow.addMethod("fleetSign", (data) -> {
			String verifiedJWS = (String) data;
			return this.fleetCosign.apply(verifiedJWS);
		});
		return flow;
	}

	private TdiFlowArguments buildFlowFleetFromDev() {
		TdiFlowArguments flow = new TdiFlowArguments();
		flow.addOverrideSteps(Arrays.asList("sendToCosigner", "validateCosigner", "fleetSign"));
		flow.addMethod("sendToCosigner", (data) -> {
			String outboundJWS = (String) data;
			return this.impl.getPlatform().getKeystore().getSelfKey().thenApply((TdiKeyStructureShape self) -> {
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
			}).exceptionally(throwable -> {
				String errMsg = "sendToCosigner() failed.";
				LOG.error(errMsg + ": " + throwable.getMessage());
				throw new FrameworkRuntimeException(errMsg);
			});
		});
		flow.addMethod("validateCosigner", (data) -> {
			String requestBody = (String) data;
			return this.fleetVerify.apply(requestBody).thenApply((msg_str) -> {
				TdiCanonicalMessageShape cosignedJWS = (TdiCanonicalMessageShape) msg_str;
				// NOTE: Not _strictly_ what the TS package does, but should be equivalent.
				return CompletableFuture.completedFuture(cosignedJWS.getRawPayload());
			}).exceptionally(throwable -> {
				String errMsg = "validateCosigner() failed.";
				LOG.error(errMsg + ": " + throwable.getMessage());
				throw new FrameworkRuntimeException(errMsg);
			});
		});
		flow.addMethod("fleetSign", (data) -> {
			TdiCanonicalMessageShape msgObj = (TdiCanonicalMessageShape) data;
			return this.fleetCosign.apply(msgObj.getBuiltMessage());
		});
		return flow;
	}

	/**
	 * Signs a token if a ROLE_EXTERN key is found in the keystore relevent to the
	 * project
	 */
	private TdiFlowArguments buildFlowSignToken() {
		TdiFlowArguments flow = new TdiFlowArguments();
		flow.addOverrideSteps(Arrays.asList("setSigners"));
		flow.addMethod("setSigners", (data) -> {
			TdiCanonicalMessageShape msgObj = (TdiCanonicalMessageShape) data;
			msgObj.setSignatureType("compact");
			return this.impl.getPlatform().getKeystore()
					.getKeyByRole(TdiKeyFlagsEnum.ROLE_EXTERN.getNumber(), msgObj.getCurrentProject())
					.thenApply((TdiKeyStructureShape key) -> {
						CompletableFuture<TdiCanonicalMessageShape> future = new CompletableFuture<>();
						if ((null != key) && key.canSign()) {
							msgObj.addSigner(key);
							future.complete(msgObj);
						} else {
							future.completeExceptionally(new FrameworkRuntimeException(
									"setSigners() failed to find a signing key for either F_C or F_S."));
						}
						return future;
					}).thenCompose(arg -> {
						return arg;
					}).exceptionally(throwable -> {
						String errMsg = "setSigners() failed.";
						LOG.error(errMsg + ": " + throwable.getMessage());
						throw new FrameworkRuntimeException(errMsg);
					});
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
		LOG.info("Sending a " + payload_len + " byte payload for cosigning.");
		try {
			URL url = new URL(this.baseURI + "/projects/" + fleet + "/" + method + "/" + kid);
			LOG.info("Calling out to cosigner: " + url.toString());
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
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (con != null) {
			con.disconnect();
		}
		return resp.toString();
	}

	private String fetchIssFromJwsString(String jwsString)
			throws IOException, JsonParseException, JsonMappingException {
		String ret = null;
		Map<String, Object> jws = new ObjectMapper().readValue(jwsString, new TypeReference<Map<String, Object>>() {
		});
		String serialized_inner_payload = (String) jws.get("payload");
		String decoded = this.impl.getPlatform().getUtils().b64UrlDecode(serialized_inner_payload);
		Map<String, Object> inner_jws = new ObjectMapper().readValue(decoded, new TypeReference<Map<String, Object>>() {
		});
		ret = (String) inner_jws.get("iss");
		return ret;
	}
}
