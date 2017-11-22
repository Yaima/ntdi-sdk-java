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

**sign** will sign on behalf of the devices *self* key.

**verify** will return a parsed payload string if both the Fleet and Cosigner signatures are present and valid on the received JWS.

### NTDIGateway

A gateway may be a device that intermediates with other devices. This class, a subclass of NTDIDevice, adds methods for dealing with gateway use cases:

| Method | Argument Type | Return Type |
| --- | --- | --- |
| cosign | (String *`JWS`*) | String *`JWS`*  |
| verifyFromDevice | (String *`JWS`*) | String |
| sign | (String \| Map) | String *`JWS`*  |
| verifyFromFleet | (String *`JWS`*) | String |

In simple pass through cases, you'll only need **cosign** and **verifyFromDevice**

**cosign** will append a *self* key to the provided JWS.

**verifyFromDevice** will validate all keys present on the message, and return the payload if there is at least one match and all known signatures are verified.

**sign** is the same as the NTDIDevice sign.

**verifyFromDevice** is the same as the NTDIDevice verify.

### NTDIFleet

Fleet servers provide services that represent the Fleet, and validate messages with the NTDI Core. This class provides methods useful for a Fleet server:

| Method | Argument Type | Return Type |
| --- | --- | --- |
| signForFleet | (String \| Map) | String *`JWS`*  |
| verifyFromDevice | (String *`JWS`*) | String |
| verifyFromDeviceAndCosign | (String *`JWS`*) | String *`JWS`*  |

**signForFleet** will sign the message with the *server* key, request a cosignature from the assigned cosigner HTTP endpoint, sign the response message with the *fleet* key, and return the JWS string.

**verifyFromDevice** will send the message to the assigned cosigner HTTP endpoint for verification, verify the response, verify the device signature itself, and return the payload string.

**verifyFromDeviceAndCosign** will do everything *verifyFromDevice* does, and then sign the message with the *fleet* key, returning the JWS.

These classes prove insufficient, they can be extended with [plugins](../plugins/README.md), and provided alternate [platforms](../platform/README.md). Or, the [lower-level API](../sdk/README.md) is also available.

# Usage Example

```java

import biz.neustar.tdi;

public class demo {
  public static void main(String[] args) {

    NTDIDevice device = new NTDIDevice("device/config.json");
    NTDIFleet  fleet  = new NTDIFleet("servier/config.json");

    // Device to Fleet
    String deviceMsg = device.sign("Hello Fleet, I'm a device.");

    String verifiedDeviceMsg = fleet.verifyFromDevice(deviceMsg);

    // Fleet to Device
    String fleetMsg = fleet.signForFleet("Hello Device, I'm the fleet.");

    String verifiedFleetMessage = device.verify(fleetMsg);
  }

}

```
### Config
```
// server/config.json
{
  "platform": {
    "data": {
      "basepath": "...relative path..."
    },
    "keys": {
      "basepath": "...relative path..."
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


// device/config.json
{
  "platform": {
    "data": {
      "basepath": "...relative path..."
    },
    "keys": {
      "basepath": "...relative path..."
    }
  },
  "nonce002": {
    "expDuration": 3600
  }
}

```
