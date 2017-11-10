[![CircleCI](https://circleci.com/gh/Neustar-TDI/ntdi-sdk-java.svg?style=svg&circle-token=8df38531e4dfff635375fd651a9bda1a8948362c)](https://circleci.com/gh/Neustar-TDI/ntdi-sdk-java)

# NTDI Platform Template for Java

The framework packages relies on a Platform being present. Platform is composed of facets:

| Facet | API Route | Interface | Purpose |
| --- |:---- | :-- | :-- |
Key Storage   | pf.getKeystore()   | TdiPlatformKeysShape   | Stores cryptographic keys alongside their metadata. |
Data Storage  | pf.getDataStore()  | TdiPlatformDataShape   | Stores data in a manner that the platform can recall it later. |
Cryptographic | pf.getCrypto() | TdiPlatformCryptoShape | Gives access to the system's cryptographic methods and primitives. |
Utility       | pf.getUtils()   | TdiPlatformUtilsShape   | Exposes scattered utility functions to the framework. |
Time and Date | pf.getTime()   | TdiPlatformTimeShape   | Exposes the systems notions of time and date. |

The configuration options given below are only applicable to these example facets.

#### Key Storage
**WARNING:** The example implementation of this facet stores key material unencrypted on the filesystem. It is provided for the sake of example, and is not intended to be run in a production context.

Configuration options for this facet:

| Key | Type | Required | Doc |
| --- |:----:| :------: | :-- |
`basePath`   | `string` | `true` | The base path where the framework should store and load keys. The filename will be `keystore.json`.


#### Data Storage
| Key | Type | Required | Doc |
| --- |:----:| :------: | :-- |
`basePath`   | `string` | `true` | The base path where the framework should store and load general data. Each independent store will have a filename that matches the name of the component, plus `.dat`.

#### Cryptographic
This facet has no configuration.

#### Utility
This facet has no configuration.

#### Time and Date
This facet has no configuration.

