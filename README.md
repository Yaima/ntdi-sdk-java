[![CircleCI](https://circleci.com/gh/Neustar-TDI/java-ntdi.svg?style=svg&circle-token=8df38531e4dfff635375fd651a9bda1a8948362c)](https://circleci.com/gh/Neustar-TDI/java-ntdi)

# java-ntdi

This is Neustar's Trusted Device Identity (TDI) implemention in Java.


### Build:
```bash
mvn clean package
```

### Logging:
This project uses slf4j api for logging. So please drop a implementation of logger compatible with slf4j api in classpath of your application (where this project is being used) for the logs to get generated.
