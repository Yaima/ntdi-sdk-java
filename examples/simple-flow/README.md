# NTDI Service Example (Java)

## Building this example
From `simple-flow` root:
```
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
