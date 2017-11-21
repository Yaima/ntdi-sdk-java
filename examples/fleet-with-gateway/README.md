# NTDI Fleet with Gateway Example (Java)

This example includes a Fleet that has sensor devices that may not have Internet access, and gateway devices that manage them, and connect with cloud-based services.

For more information about the Neustar TDI SDK for Java, see the main [README](../../README.md).

There are four components in this example. Each is designed to run independently, although the server components could easily be combined.

## Components

### [Sensor Devices](device-sensor/README.md)

At the very edge of the network, sensor devices measure temperature and other raw data at a particular location. They report this data upstream through a gateway device, which does basic validation of the device and data, and forwards it up to a data collection service.

There are two types of devices represented in this example. In one case, the device is capable of including its own NTDI SDK and has its own identity, and sends signed messages to the gateway. It can also verify messages from the Fleet.

The other device, however, is a legacy device that cannot be updated. It is proxied for in the gateway with an identity that is assigned to it, but the signing and verification happens in the gateway.

### [Gateway Device](device-gateway/README.md)

The gateway device acts as a mediator between the sensors and the cloud services. A gateway can manage a number of devices, but each device has only one gateway that it accepts messages from and sends messages to.

In cases where the devices are able to sign messages, the gateway will perform basic validation of the message and the device. Specifically, it will ensure that the device is one it knows about, and that the message is of the format expected from that device.

For legacy devices that cannot sign, the gateway will proxy the devices as best it can, providing identities and creating signed messages in their behalf. While these messages are technically from the gateway, they will appear to upstream consumers as coming from those devices, which can be revoked independently from the gateway, and later upgraded to signing devices on their own, without changes to the cloud services.

The gateway will also forward messages from the Fleet after is verifies them as valid, and appropriate in context.

### Cloud services

#### [Data Collection Service](server-data-collector/README.md)

On the cloud side, we have services that consume data from devices, verifying that it is from an authentic and authorized device before storing it.

#### [Device Activation Service](server-actuator/README.md)

The Fleet also includes a service that controls the actions of certain remote devices, such as a power outlets, lights, etc. It provides Fleet-signed and NTDI Core co-signed messages for devices that they can authenticate before taking action.

## Communications

All communications among devices and cloud services is done through MQTT.

Two brokers are assumed, one local to each gateway/device cluster, and one global for the Fleet. For this example, they can be one and the same.

But one could image that local MQTT could be replaced by non-IP communication, such as [Z-Wave](https://en.wikipedia.org/wiki/Z-Wave) or [BLE](https://en.wikipedia.org/wiki/Bluetooth_Low_Energy).

Fleet-level topics include:
* `ntdi/examples/<fleetID>/<gatewayID>/report/<deviceID>/<feature>`
Data messages from gateways to the Fleet reporting `<feature>` data from sensor `<deviceID>`.
* `ntdi/examples/<fleetID>/<gatewayID>/action/<deviceID>/<action>`
Action messages from the Fleet to a gateway device to perform `<action>` on device `<deviceID>`.

Gateway-local topics (more compact for device memory savings):
* `<gatewayID>/report/<deviceID>`
Data messages from a sensor to gateway.
* `<gatewayID>/action/<deviceID>/<action>`
Action messages from gateway to sensor.

## Running the demo

### Check out and build the code

### Install the NTDI Core CLI

### Create API Credentials
```
$ oneid-cli configure --yes --name "Joe Admin <admin@joe.example.com>"
```

### TODO: quick-and-easy way
```
$ bin/configure-fleet
```

### Manual Way

#### Create the Fleet
```
$ oneid-cli create-project --yes --name "My Project" --keystore ./fleet-keystore.json --use-jwk-keystore
```
Record the Fleet ID, as it will be needed in subsequent steps.

#### Create and Configure the Gateway
```
$ oneid-cli provision --yes --project_id $FLEET_ID --name "My Gateway" --type edge_device --keystore ./device-gateway/src/main/resources/device/keystore.json --use-jwk-keystore
```
Record the Gateway ID, as it will be needed for the sensor(s).

Copy `./device-gateway/src/main/resources/device/sample-config.json` to `./device-gateway/src/main/resources/device/config.json` and open in an editor.

Replace `<fleetID>` with the value saved during Fleet creation, and `<gatewayID>` with the value saved above.

#### Create and Configure the Sensor(s)
```
$ oneid-cli provision --yes --project_id $FLEET_ID --name "My Motion/Temperature Sensor" --type edge_device --keystore ./device-sensor/src/main/resources/device/keystore.json --use-jwk-keystore
```
Copy `./device-sensor/src/main/resources/device/sample-config.json` to `./device-sensor/src/main/resources/device/config.json` and open in an editor.

Replace `<fleetID>` with the value saved during Fleet creation, and `<gatewayID>` and `<deviceID>` with the values saved above.

#### Connect Gateway and Sensors
(add sensor ID(s) to gateway config)

#### Create and Configure the Servers

### Run
(one script to run all? one per service/device?)

(script to turn switch on/off)

### Revoking Devices

### Revoking Services
