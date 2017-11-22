# Simple Sensor

This is a basic NTDI device that reports motion activity and battery level, and includes a switch that can be turned on and off.

It posts its values to an MQTT topic every second (configurable), and listens on another topic for commands to turn the switch on or off.

## Configuration

The configuration files in `src/main/resources` are in two parts:
### `tdi/config.json` 
Basic NTDI configuration values for the device. These include where to find the keystore and data files, as well as basic parameters such as nonce expiration times.

### `app/config.json` 
Runtime values required for the Sensor controller, such as MQTT parameters and Fleet information. It also contains the ID of the gateway device that the sensor reports to. The Gateway's public key should also be included in each Sensor's keystore.

## Setup (pre-provisioned)
1. copy provided tdi/config.json & keystore.json to `src/main/resources/tdi`
2. copy provided app/config.json to `src/main/resources/app`

## Running

Once configured and built, the Sensor can be run from the commmand line:
```
mvn exec:java
```

Or, with more detailed logging (change "debug" to "trace" for even more verbose logging):
```
mvn exec:java -Dorg.slf4j.simpleLogger.defaultLogLevel=debug
```
