# NTDI Fleet with Gateway Example (Java)

This example includes a Fleet that has sensor devices that may not have Internet access, and gateway devices that manage them, and connect with cloud-based services.

For more information about the Neustar TDI SDK for Java, see the main [README](../../README.md).

There are four components in this example. Each is designed to run independently, although the server components could easily be combined.

## Components

### [Sensor Devices](device-sensor/README.md)

At the very edge of the network, sensor devices measure temperature and other raw data at a particular location. They report this data upstream through a gateway device, which does basic validation of the device and data, and forwards it up to a data collection service.

These devices also have a programmable switch, which can be turned on and off over the network.

### [Gateway Device](device-gateway/README.md)

The gateway device acts as a mediator between the sensors and the cloud services. A gateway can manage a number of devices, but each device has only one gateway that it accepts messages from and sends messages to.

In cases where the devices are able to sign messages, the gateway will perform basic validation of the message and the device. Specifically, it will ensure that the device is one it knows about, and that the message is of the format expected from that device.

The gateway will also forward messages from the Fleet after is verifies them as valid, and appropriate in context.

### Cloud services

#### [Data Collection Service](server-data-collector/README.md)

On the cloud side, we have services that consume data from devices, verifying that it is from an authentic and authorized device before processing it.

#### [Device Activation Service](server-actuator/README.md)

The Fleet also includes a service that controls the actions of certain remote devices, such as a power outlets, lights, etc. It provides Fleet-signed and NTDI Core co-signed messages for devices that they can authenticate before taking action.

## Communications

All communications among devices and cloud services is done through MQTT.

Two brokers are assumed, one local to each gateway/device cluster, and one global for the Fleet. For this example, they can be one and the same.

But one could image that local MQTT could be replaced by non-IP communication, such as [Z-Wave](https://en.wikipedia.org/wiki/Z-Wave) or [BLE](https://en.wikipedia.org/wiki/Bluetooth_Low_Energy).

For convenience, the topic names are the same in each zone, but they don't need to be.

Topics include:
* `<gatewayID>/report/<deviceID>`
Data messages from a sensor to gateway.
* `<gatewayID>/action/<deviceID>/<action>`
Action messages from gateway to sensor.

## Running the demo

Each entity runs on its own. It will be helpful to keep each in its own terminal window to see the output from each.

### Check out and build the code

```
$ git clone https://github.com/Neustar-TDI/ntdi-sdk-java.git
$ cd ntdi-sdk-java
$ mvn clean install
```

### Configuring and Running Servers and Devices

The configurations and keystores for each entity can be set up manually, or semi-manually using the [TDI CLI](./README-cli.md).

See the README files in each sub-directory for specific requirements for each, as well as instructions for running them.
