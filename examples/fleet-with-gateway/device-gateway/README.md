# Gateway

This is an NTDI gateway device that forwards messages between local devices and the cloud.

# Configuration

The configuration files in `src/main/resources` are in two parts. In `gateway/config.json` are basic NTDI configuration values for the device. These include where to find the keystore and data files, as well as basic parameters such as nonce expiration times.

In `gateway/config.json` are runtime values required for the Gateway, such as MQTT values and Fleet information.

# Running

Once configured and built, the device can be run from the commmand line:
```
mvn exec:java
```

Or, with logging (change "info" to "debug" for more detailed logging):
```
mvn exec:java -Dorg.slf4j.simpleLogger.defaultLogLevel=info
```
