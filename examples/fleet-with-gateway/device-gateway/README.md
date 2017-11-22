# Gateway

This is an NTDI gateway device that forwards messages between local devices and the cloud.

## Configuration

The configuration files in `src/main/resources` are in two parts: 
### `tdi/config.json` 
Basic NTDI configuration values for the device. These include where to find the keystore and data files, as well as basic parameters such as nonce expiration times.

### `app/config.json` 
Runtime values required for the Gateway controller, such as MQTT parameters and Fleet information. It also includes the list of sensor devices that it knows about. Note that the public keys of these sensors need to be included in the Gateway's keystore.

## Setup (pre-provisioned)
1. copy provided tdi/config.json & keystore.json to `src/main/resources/tdi`
2. copy provided app/config.json to `src/main/resources/app`


## Running

Once configured and built, the Gateway can be run from the commmand line:
```
mvn exec:java
```

Or, with more detailed logging (change "debug" to "trace" for even more verbose logging):
```
mvn exec:java -Dorg.slf4j.simpleLogger.defaultLogLevel=debug
```
