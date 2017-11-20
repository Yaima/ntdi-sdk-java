[![CircleCI](https://circleci.com/gh/Neustar-TDI/ntdi-sdk-java.svg?style=svg&circle-token=8df38531e4dfff635375fd651a9bda1a8948362c)](https://circleci.com/gh/Neustar-TDI/ntdi-sdk-java)

# ntdi-sdk-java

This is Neustar's Trusted Device Identity (TDI) implementation in Java 8.

[Framework](https://github.com/Neustar-TDI/ntdi-sdk-java/framework) is pulled in by these other components in the course of their installation. Application code _may_ use definitions from this namespace, but should not otherwise know of it. All application usage of TDI should be done via the [sdk package](https://github.com/Neustar-TDI/ntdi-sdk-java/sdk). See that documentation for the TDI API.


### Build:
```bash
mvn clean package
```

### Documentation:
```bash
mvn javadoc:javadoc
```

### Adding NTDI SDK as a dependency to other projects
```XML
<dependency>
    <groupId>biz.neustar.tdi</groupId>
    <artifactId>framework</artifactId>
    <version>1.0</version>
</dependency>
```

### Logging:
This project uses slf4j API for logging. So please drop an implementation of logger compatible with slf4j API in `CLASSPATH` of your application (where this project is being used) if you want logging.
