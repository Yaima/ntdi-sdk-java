<!--
 Copyright 2017 Neustar, Inc.

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
-->
# NTDI Platform Template for Java

*[Generated API Documentation](https://github.com/Neustar-TDI/node-ntdi-doc/doc/platform)*

The NTDI library relies on a Platform being present. Platform is composed of facets:

| Facet | Platform Member | Interface | Purpose |
| --- |:----:| :-- | :-- |
Key Storage   | keystore   | biz.neustar.tdi.fw.platform.facet.keys.TdiPlatformKeysShape | Stores cryptographic keys alongside their metadata. |
Data Storage  | datastore  | biz.neustar.tdi.fw.platform.facet.data.TdiPlatformDataShape | Stores data in a manner that the platform can recall it later. |
Cryptographic | crypto     | biz.neustar.tdi.fw.platform.facet.crypto.TdiPlatformCryptoShape | Gives access to the system's cryptographic methods and primitives. |
Utility       | util       | biz.neustar.tdi.fw.platform.facet.utils.TdiPlatformUtilsShape | Exposes scattered utility functions to the framework. |
Time and Date | time       | biz.neustar.tdi.fw.platform.facet.time.TdiPlatformTimeShape | Exposes the systems notions of time and date. |

Any given facet may be over-ridden by application-provided replacement. The configuration options given below are only applicable to these example facets.

Application-provided facets must conform to the corresponding facet interface, as found in the [framework namespace](https://github.com/Neustar-TDI/ntdi-sdk-java/framework).

#### Key Storage
**WARNING:** The example implementation of this facet stores key material unencrypted on the filesystem. It is provided for the sake of example, and is not intended to be run in a production context.

Configuration options for this facet:

| Key | Type | Required | Doc |
| --- |:----:| :------: | :-- |
`basePath`   | `string` | `true` | The base path where the framework should store and load keys. The filename will be `keystore.dat`.


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


## Replacing one specific facet of a platform instance

In the (likely) event that user-supplied code wants to take on the responsibilities of one or more facets, the desired facet an be imported and attached to the platform object ahead of TDI initialization. Like so....

```javascript
// TODO: Provide example of this when demanded.
```
