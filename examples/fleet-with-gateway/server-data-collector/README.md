# Device Action Service

This is an NTDI service that collects data from various connected devices through gateways.

## Configuration

The configuration files in `src/main/resources` are in two parts. In `tdi/config.json` are basic NTDI configuration values for the server. These include where to find the keystore and data files, as well as basic parameters such as nonce expiration times.

In `app/config.json` are runtime values required for the service, such as MQTT parameters and Fleet information. It also contains the ID of the gateway devices and the sensors connected to them. The Gateway and Sensor public keys should also be included in the service's keystore.

## Running

Once configured and built, the service can be run from the commmand line:
```
mvn exec:java
```

Or, with more detailed logging (change "debug" to "trace" for even more verbose logging):
```
mvn exec:java -Dorg.slf4j.simpleLogger.defaultLogLevel=debug
```
