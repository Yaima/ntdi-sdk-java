# Simple Sensor

This is a basic NTDI device that reports motion activity and battery level, and includes a switch that can be turned on and off.

It posts its values to an MQTT topic every second (configurable), and listens on another topic for commands to turn the switch on or off.

# Configuration

The configuration files in `src/main/resources` are in two parts. In `device/config.json` are basic NTDI configuration values for the device. These include where to find the keystore and data files, as well as basic parameters such as nonce expiration times.

In `controller/config.json` are runtime values required for the SensorController, such as MQTT values and Fleet information. It also contains the ID of the gateway device that the sensor reports to.

# Running

Once configured and built, the device can be run from the commmand line:
```
mvn exec:java
```

Or, with logging (change "info" to "debug" for more detailed logging):
```
mvn exec:java -Dorg.slf4j.simpleLogger.defaultLogLevel=info
```
