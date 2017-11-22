[![CircleCI](https://circleci.com/gh/Neustar-TDI/ntdi-sdk-java.svg?style=svg&circle-token=8df38531e4dfff635375fd651a9bda1a8948362c)](https://circleci.com/gh/Neustar-TDI/ntdi-sdk-java)

# Neustar TDI Java SDK

This is Neustar's Trusted Device Identity (TDI) SDK for Java.

## Installation:

To get started, download following, build and include the library in your project:

`https://github.com/Neustar-TDI/ntdi-sdk-java.git`

## Classes

The top-level `biz.neustar.tdi` package includes three primary classes. Each can be used in different contexts.

### NTDIDevice

This class is primarily designed for simple devices and other entities that produce and consume messages to and from a Fleet.

It consists of two methods:

| Method | Argument Type | Return Type |
| --- | --- | --- |
| sign | (String \| Map) | String *`JWS`*  |
| verify | (String *`JWS`*) | String |

**sign** will generate a JWS payload with a signature on behalf of the devices *self* key.

**verify** will return a parsed payload string if both the Fleet and Cosigner signatures are present and valid on the received JWS.

### NTDIGateway

A gateway may be a device that intermediates with other devices. This class, a subclass of NTDIDevice, adds methods for dealing with gateway use cases:

| Method | Argument Type | Return Type |
| --- | --- | --- |
| cosign | (String *`JWS`*) | String *`JWS`*  |
| verifyFromDevice | (String *`JWS`*) | String |
| sign | (String \| Map) | String *`JWS`*  |
| verify | (String *`JWS`*) | String |

In simple pass through cases, you'll only need **cosign** and **verifyFromDevice**

**cosign** will append a signature on behalf of the gateway's *self* key to the provided JWS.

**verifyFromDevice** will validate all known keys present on the message, and return the payload if there is at least one match and all known signatures are verified.

**sign** will generate a JWS payload with a signature on behalf of the gateways *self* key.

**verify** will return a parsed payload string if both the Fleet and Cosigner signatures are present and valid on the received JWS.

### NTDIFleet

Fleet servers provide services that represent the Fleet, and validate messages with the NTDI Core. This class provides methods useful for a Fleet server:

| Method | Argument Type | Return Type |
| --- | --- | --- |
| signForFleet | (String \| Map) | String *`JWS`*  |
| verifyFromDevice | (String *`JWS`*) | String |

**signForFleet** will sign the message with the *server* key, request a cosignature from the assigned cosigner HTTP endpoint, sign the response message with the *fleet* key, and return the JWS string.

**verifyFromDevice** will send the message to the assigned cosigner HTTP endpoint for verification, verify the response, verify the device signature itself, and return the payload string.


These classes prove insufficient, they can be extended with [plugins](../plugins/README.md), and provided alternate [platforms](../platform/README.md). Or, the [lower-level API](../sdk/README.md) is also available.

# Usage Example

NOTE: In order to run these, you'll require valid `keystore.json` files as specified in your `config.json` (see below).

```java
import biz.neustar.tdi;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class demo2 {
  public static void main(String[] args)
      throws InterruptedException, ExecutionException, IOException {

    NTDIDevice device = new NTDIDevice("device/config.json");
    NTDIFleet fleet = new NTDIFleet(new Config("server/config.json"));
    NTDIGateway gateway = new NTDIGateway("gateway/config.json");

    // ********
    // Device to Fleet flow, via Gateway
    // ********

    // Generating device data and signed payload
    String deviceData = "Device Data";
    String deviceMsg = device.sign(deviceData);

    // Insert desired network transport...

    // Checking device signature at the gateway
    String gwCheck = gateway.verifyFromDevice(deviceMsg);
    // Appending a signature
    String gwMsg = gateway.cosign(deviceMsg);

    // Insert desired network transport...

    // Verifying Device and Gateway signatures locally and at configurable Cosigner URL endpoint
    String fleetVerifiedData = fleet.verifyFromDevice(gwMsg);

    // ********
    // Fleet to Device Flow, via Gateway
    // ********

    // Generating fleet data, and assembling required signaturs from Cosigner URL endpoint
    String fleetData = "Fleet Data";
    String fleetMsg = fleet.signForFleet(fleetData);

    // Insert desired network transport...

    // Checking fleet signature on the gateway
    String gwCheckFleet = gateway.verify(fleetMsg);
    // Appending signature
    String fleetGwMsg = gateway.cosign(fleetMsg);

    // Insert desired network transport...

    // Verifying Fleet signatures on the device
    String deviceVerifiedData = device.verify(fleetGwMsg);
  }
}
```
### Config
##### server/config.json
```json
{
  "platform": {
    "data": {
      "basepath": "...relative path to datastores..."
    },
    "keys": {
      "basepath": "...relative path to keystore..."
    }
  },
  "nonce002": {
    "expDuration": 3600
  },
  "FleetSigner": {
    "acceptDevices": false,
    "cosigner": {
      "baseURI":  "https://api-demo.oneid.com"
    }
  }
}
```
##### device/config.json
```json
{
  "platform": {
    "data": {
      "basepath": "...relative path to datastores..."
    },
    "keys": {
      "basepath": "...relative path to keystore..."
    }
  },
  "nonce002": {
    "expDuration": 3600
  }
}
```
##### gateway/config.json
```json
{
  "platform": {
    "data": {
      "basepath": "...relative path to datastores..."
    },
    "keys": {
      "basepath": "...relative path to keystore..."
    }
  },
  "nonce002": {
    "expDuration": 3600
  }
}


```
