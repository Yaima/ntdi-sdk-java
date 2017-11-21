# Gateway

This is an NTDI gateway device that forwards messages between local devices and the cloud.

## Configuration

The configuration files in `src/main/resources` are in two parts. In `tdi/config.json` are basic NTDI configuration values for the device. These include where to find the keystore and data files, as well as basic parameters such as nonce expiration times.

In `app/config.json` are runtime values required for the Gateway controller, such as MQTT parameters and Fleet information. It also includes the list of sensor devices that it knows about. Note that the public keys of these sensors need to be included in the Gateway's keystore.

## Running

Once configured and built, the Gateway can be run from the commmand line:
```
mvn exec:java
```

Or, with more detailed logging (change "debug" to "trace" for even more verbose logging):
```
mvn exec:java -Dorg.slf4j.simpleLogger.defaultLogLevel=debug
```
