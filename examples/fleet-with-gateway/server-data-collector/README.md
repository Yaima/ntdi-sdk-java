# Device Action Service

This is an NTDI service that collects data from various connected devices through gateways.

## Configuration

The configuration files in `src/main/resources` are in two parts:
### `tdi/config.json`
Basic NTDI configuration values for the server. These include where to find the keystore and data files, as well as basic parameters such as nonce expiration times.

### `app/config.json`
Runtime values required for the service, such as MQTT parameters and Fleet information. It also contains the ID of the gateway devices and the sensors connected to them. The Gateway and Sensor public keys should also be included in the service's keystore.

## Setup (pre-provisioned)
1. copy provided tdi/config.json & keystore.json to `src/main/resources/tdi`
2. copy provided app/config.json to `src/main/resources/app`

## Running

Once configured and built, the service can be run from the commmand line:
```
mvn exec:java
```

Or, with more detailed logging (change "debug" to "trace" for even more verbose logging):
```
mvn exec:java -Dorg.slf4j.simpleLogger.defaultLogLevel=debug
```
