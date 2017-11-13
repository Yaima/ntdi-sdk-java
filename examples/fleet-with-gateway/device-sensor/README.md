# NTDI Sensor Example  (Java)

This project demonstrates the usage of Neustar's TDI components namely 
framework, sdk and platform example. This example Signs and Co-signs
the payload ```(motion=active, name=Room 1 Sensor, battery=85)``` string. 
Note: The explaination about the key flags in keystore.json files is provided 
in [KeyFlags.txt](KeyFlags.txt) file.


## Pre-requisite
Checkout Neustar TDI [ntdi-sdk-java](https://github.com/Neustar-TDI/ntdi-sdk-java) and execute the command below
```
$ git clone https://github.com/Neustar-TDI/ntdi-sdk-java
$ cd ntdi-sdk-java
$ mvn clean install
```

## Building this example
Checkout this project and build it as shown below:
```
$ git clone https://github.com/Neustar-TDI/demo-device-java.git
$ cd demo-device-java
$ mvn clean package
```
## How does it work

The Sensor signs the data ```(motion=active, name=Room 1 Sensor, battery=85)```, ```signDevice(String payload)``` and sends the jws
to the Gateway to co-sign ```cosignDevice(String signedMsg)```. The Gateway will send the co-signed jws to ntdi-demo-docker-java 
to verify the message ```verifySignature(String cosignedMsg)```.

## Executing the example:
### 1. Via command line
```
mvn exec:java
```

### 2. Execute from any editior
Just execute the ```Sensor.java```.

## Additional Documentation
### TDI Node Services
The TDI node services also provide API endpoints for `/signMessage` `/verifyMessage` and `/device`. Documentation for the API can be found in [documentation/node_api_doc.md](documentation/node_api_doc.md).
