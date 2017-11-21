# NTDI Service Example (Java)

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
$ git clone https://github.com/Neustar-TDI/demo-service-java.git
$ cd demo-service-java
$ mvn clean package
```
## Executing the example:
### 1. Via command line
```
mvn exec:java
```

Or, with increased log verbosity...
```
mvn exec:java -Dorg.slf4j.simpleLogger.defaultLogLevel=trace
```
