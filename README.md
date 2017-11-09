[![CircleCI](https://circleci.com/gh/Neustar-TDI/ntdi-sdk-java.svg?style=svg&circle-token=8df38531e4dfff635375fd651a9bda1a8948362c)](https://circleci.com/gh/Neustar-TDI/ntdi-sdk-java)

# ntdi-sdk-java

This is Neustar's Trusted Device Identity (TDI) implementation in Java 8.


### Build:
```bash
mvn clean package
```

### Documentation:
```bash
mvn javadoc:javadoc
```

### Logging:
This project uses slf4j api for logging. So please drop a implementation of logger compatible with slf4j api in classpath of your application (where this project is being used) for the logs to get generated.
