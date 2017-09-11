[![CircleCI](https://circleci.com/gh/Neustar-TDI/java-ntdi.svg?style=svg&circle-token=8df38531e4dfff635375fd651a9bda1a8948362c)](https://circleci.com/gh/Neustar-TDI/java-ntdi)

# Neustar TDI Java Implementation

This is Neustar's Trusted Device Identity (TDI) SDK implemented in java.

This combined with a Platform will allow you to
create a device SDK.  You are able to expand this package with Plugins to do many other things as well.

## Installation:

This java library includes full definitions and sourceMaps for debugging if you choose to use them.

To get started, download following, build and include the library in your project:

`https://github.com/Neustar-TDI/java-ntdi.git`

This will download the framework and sdk source codes. 
It would also download example implementations for Platform and Plugin. You can however write your own implementations for them by referring to the examples.  

## A tour of important structures

#### 1. TDI Sdk (this repo)

This library forms the application's secure and authenticated channel to the rest of the network-application. Its sole purpose is to take user configuration and extension code (if any), and return an organized API object to the user that represents the choices. From this API object, authenticated communication can be abstracted away from both the network and the application layers.

To use the OSI metaphor, this package represents a layer-6 authentication mechanism that allows the injection of policy and arbitrary rules surrounding signatures.

#### 2. [Platform](https://github.com/Neustar-TDI/java-ntdi/examples/platform)

The platform is the interface between the library, and the system below it. Our example platform and application demos assume java, and provide a uniform interface to (often novel) hardware features (cryptography, time and date, persistent storage, etc).

For more detail, see the platform repo. For now, we will use the example platform.

`https://github.com/Neustar-TDI/java-ntdi/examples/platform`

#### 3. Configuration

Configuration for the returned API is provided as a single nested object. Each property scopes to the name of the element being configured.

This configuration object will be passed throughout the library, and represents the initial configuration of the system. The library may alter and persist these values. If conflicting configuration is loaded from storage by the platform, these values will be ignored.

##### Example config.json:

```
{
    "platform" : { // See platform-example README for specifics.
        // Both FS based. Store data files in local directory.
        "data": {
            "basePath": "./"
        },
        "keys": {
            "basePath": "./"
        },
        "nonce002": { // Config for the replay-prevention claim handler.
            "expDuration": 300 // Default lifetime of 5 minutes
        }
    }
}
```

#### 4. Claim handlers

These are modular handlers for specific claims inside of any given message. Wire-encodings are also handled this way.

Not all supported claims need be present in a message for the message to be valid,  although that is typically the case. User code may extend the message metadata or alter the encoding strategy by writing new claim handlers.

TDI ships with these default claim handlers:

| Handler | Purpose |
| --- |:---|
| `nonce` | Replay prevention |
| `exp` and `nbf` | Enforcement of temporal validity window |
| `jws` | Organizes and parses claims according to [JWS specification](https://tools.ietf.org/html/rfc7515) |
| `jwt` | Encodes/decodes messages according to [JWT specification](https://tools.ietf.org/html/rfc7519)  |

At present, only `nonce` has configurable behavior. Its configuration options are...

| Key | Type | Required | Doc |
| --- |:----:| :------: | :-- |
| `expDuration` | _number_ | `true`  | For how many seconds will messages be valid? |
| `nbfMinimum` | _number_ | `false`  | If provided, represents the earliest date this system will begin accepting messages. Expressed as a 64-bit epoch timestamp. <br />If not provided and absent from the datastore, defaults to the system time. This value will be regularly updated in the datastore. |

#### 5. [Plugins](https://github.com/Neustar-TDI/java-ntdi/plugins)

These are modular API extensions that impart a given capability to the network-level application. More detail can be found in the plugin and  [app-examples](https://github.com/Neustar-TDI/java-ntdi/examples/app) repos.


##### Plugin loading example

Key exchange is a common requirement for networked applications. The plugin repo contains a plugin for this purpose. It would be loaded in this way. Plugin method references are added to the `pluginList`

```java
List<TdiPluginBaseFactory> pluginsList = new ArrayList<>();

TdiSdkOptions sdkOptions = new TdiSdkOptions();
sdkOptions.plugins = pluginsList;
```

## Tying it together

`TdiSdk.init()` returns a CompletableFuture<TdiSdkWrapperShape> that will return a TdiSdkWrapper object when it finishes loading and passes all of its internal checks. See the [framework repo](https://github.com/Neustar-TDI/java-ntdi/framework) for details on this process.

`TdiSdk` constructor takes a `TdiSdkOptions` composed of the following arguments:
 - (required) A Platform class
 - (required) A configuration defaults object
 - (optional) An array of Plugin classes - null can be passed for plugins
 - (required) expose Implementation flag


```java
/*
 * build the arguments
 */
TdiSdkOptions sdkOptions = new TdiSdkOptions();
sdkOptions.platform = Platform::new;
sdkOptions.config = getConfig(); //load it from config file
sdkOptions.exposeImpl = true;
sdkOptions.plugins = new ArrayList<>();

/*
 * create and initialize the sdk instance
 * Note: The sdk will have to be initialized once each for  platforms of sign, cosign and verify functions.
 */
TdiSdk defaultSdk = new TdiSdk(sdkOptions);
CompletableFuture<TdiSdkWrapperShape> defaultHandle = defaultSdk.init();

/*
 * Usage
 * Get the required api from sdk and use it.
 * Note that the Api.VerifyFlow.name() can be replaced either by Api.SignFlow.name() 
 * or Api.CosignFlow.name() to get the required Api as a Function reference.
 */
 defaultHandle.thenCompose((sdkHandle) -> {
    Function<String, CompletableFuture<String>> verifyApi = sdkHandle.api(Api.VerifyFlow.name());
    CompletableFuture<String> result = api.apply(<payload_to_verify>);
    return result;
});

/*
 * Example creation of each Api function and its usage.
 * Note: The 'signSdkWrapper', 'cosignSdkWrapper' and 'verifySdkWrapper' are instances of CompletableFuture<TdiSdkWrapperShape> and are created by 
 * initializing the sdk once each for the platforms of sign, cosign and verify functions.
 * 
 */
//sign the payload  
CompletableFuture<TdiCanonicalMessage> signedMessage = signSdkWrapper.thenCompose((signWrapper) -> {
  Function<String, CompletableFuture<TdiCanonicalMessage>> signApi = signWrapper.api(Api.SignFlow.name());
  CompletableFuture<TdiCanonicalMessage> signResult = signApi.apply(<payload_to_sign>);
  return signResult;
});
TdiCanonicalMessage signedMsg = signedMessage.get();

// cosign the payload
CompletableFuture<TdiCanonicalMessage> cosignedMessage = cosignSdkWrapper.thenCompose((cosignWrapper) -> {
  Function<String, CompletableFuture<TdiCanonicalMessage>> cosignApi = cosignWrapper.api(Api.CosignFlow.name());
  CompletableFuture<TdiCanonicalMessage> cosignResult = cosignApi.apply(signedMsg.getBuiltMessage());
  return cosignResult;
});

TdiCanonicalMessage cosignedMsg = cosignedMessage.get();

// verify the payload
CompletableFuture<String> result = verifySdkWrapper.thenCompose((verifyWrapper) -> {
  Function<String, CompletableFuture<String>> verifyApi = verifyWrapper.api(Api.VerifyFlow.name());
  CompletableFuture<String> verifyResult = verifyApi.apply(cosignedMsg.getBuiltMessage());
  return verifyResult;
});

String verifiedPayload = result.get();
```

## Basic TDI API
The methods to use for each api-function is apply() (as shown in the example above)

### public CompletableFuture<TdiCanonicalMessage> **signApi.apply**(`String PAYLOAD_VALUE`)
  Sign will take a string payload (file and json can be sent by converting it to string) and generate a message that is signed with your 'SELF' key.
  This is distinct, because you will require a plugin to sign with non-self keys.

### public CompletableFuture<TdiCanonicalMessage> **cosignApi.apply**(`String signedJwsPayload`)
  Cosign will take a signed TDI packet and append an additional signature with your 'SELF' key.  This will NOT validate said packet, so make sure to call `verify` on the packet first if you intend on only cosigning valid packets.
  
### public CompletableFuture<String> **verifyApi.apply**(`String aSignedJwsString`) 
  Verify will return you the payload of a signed TDI packet if the validation checks pass, as well as the signature relationships (Fleet Signer and Fleet Cosigner relative to the 'fleet' field associated with your 'SELF' key).  You will receive a CompletableFuture with exception if anything goes wrong with an error code.  
