[![CircleCI](https://circleci.com/gh/Neustar-TDI/ntdi-sdk-java.svg?style=svg&circle-token=8df38531e4dfff635375fd651a9bda1a8948362c)](https://circleci.com/gh/Neustar-TDI/ntdi-sdk-java)

# Java TDI Framework

This is Neustar's Trusted Device Identity (TDI) framework implemented in Java. It forms the definition and glue layer between....

| Repository | Purpose |
| --- | :-- |
[TDI](https://github.com/Neustar-TDI/ntdi-sdk-java/sdk) | The application-facing SDK
[Plugins](https://github.com/Neustar-TDI/ntdi-sdk-java/plugins) | Plugins for TDI
[Platform](https://github.com/Neustar-TDI/ntdi-sdk-java/examples/platform) | The boundary between the framework and the hardware
[app-examples](https://github.com/Neustar-TDI/ntdi-sdk-java/examples/app) | The application itself. The project gives some possible example applications.


This repo is pulled in by these other components in the course of their installation. Application code _may_ use definitions from this repo, but should never otherwise know of it. All application usage of TDI should be done via the [sdk package](https://github.com/Neustar-TDI/ntdi-sdk-java/sdk). See that documentation for the TDI API.

<br>
Javadoc would be during build process.
<br>
Alternatively Javadoc can also be generated using following commands:
<br> 

*Maven*
```bash 
mvn javadoc:javadoc
```

### Build:
```bash
mvn clean package
```

### Add dependency to other projects
```XML
<dependency>
    <groupId>biz.neustar.tdi</groupId>
    <artifactId>framework</artifactId>
    <version>1.0</version>
</dependency>

```

