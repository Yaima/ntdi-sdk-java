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

# Neustar TDI Plugins
*[Generated API Documentation](https://github.com/Neustar-TDI/node-ntdi-doc/doc/plugins)*

This is a collection of plugins for NTDI.

| Plugin | Description |
|---------:|---|
| `FleetSigner` | Fleet Signer API provides the API and support for a node/server. |


--------

## FleetSigner

#### Configuration options:
| Key | Type | Required | Doc |
| --- |:----:| :------: | :-- |
| `acceptDevices` | _boolean_ | `true`  | Allows this signer to accept new devices. |
| `cosigner` | _Object [Note below]()_ | `true` | This is a subconfiguration for the relationship with a fleet cosigner. |

##### Cosigner configuration options:

| Key | Type | Required | Doc |
| --- |:----:| :------: | :-- |
| `baseURI` | _string_ | `true`  | Provide the network route to the cosigner service. |

### API:

##### *FleetSigner*.**fleetSign**(`String payload`) : *CompletableFuture*\<`TdiCanonicalMessageShape`>
Constructs a `tdiMessage` from the provided string and signs it with the Fleet key relative to the provided `fleet_id`.

##### *FleetSigner*.**fleetCosign**(TdiCanonicalMessageShape `tdiMessage`) : *CompletableFuture*\<`TdiCanonicalMessageShape`>
Appends a Fleet signature to the `tdiMessage`.

##### *FleetSigner*.**fleetVerify**(`TdiCanonicalMessageShape tdiMessage`) : *CompletableFuture*\<`TdiCanonicalMessageShape`>
Verifies that the signatures present are not revoked and members of the same fleet.

##### *FleetSigner*.**fleetToDevice**(`String payload`) : *CompletableFuture*\<`String`>
Performs several steps to take in a payload, `sign` it, send it to the cosigner, `fleetVerify` the cosigner signature, `fleetSign` the message, and returns a valid `tdiMessage` for device consumption.

##### *FleetSigner*.**fleetFromDevice**(`String tdiMessage`) : *CompletableFuture*\<`String`>
Performs several steps to take in a tdiMessage, `fleetVerify` it, send it to the cosigner, `fleetVerify` the cosigner signature, and return the payload as successfully verified.


### Typical usage in practice

Plugins came be combined and interchanged with user-supplied versions. The specific suite of plugins in this repo represent elements of commonly-desired functionality. For examples of these plugins in working demo code, please see the [examples](/examples/).
