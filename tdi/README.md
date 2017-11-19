[![CircleCI](https://circleci.com/gh/Neustar-TDI/ntdi-sdk-java.svg?style=svg&circle-token=8df38531e4dfff635375fd651a9bda1a8948362c)](https://circleci.com/gh/Neustar-TDI/ntdi-sdk-java)

# Neustar TDI Java SDK

This is Neustar's Trusted Device Identity (TDI) SDK for Java.

## Installation:

To get started, download following, build and include the library in your project:

`https://github.com/Neustar-TDI/ntdi-sdk-java.git`

## Classes

The top-level `biz.neustar.tdi` package includes two primary classes. Each can be used in different contexts.

### NTDI

This class is primarily designed for simple devices and other entities that produce and consume messages to and from a Fleet.

It consists of three methods:
* sign
* cosign
* verify

### NTDIFleet

This class extends `NTDI` to provide additional methods useful for a Fleet server:

* fleetToDevice
* fleetFromDevice

These classes prove insufficient, they can be extended with [plugins](../plugins/README.md), and provided alternate [platforms](../platform/README.md). Or, the [lower-level API](../sdk/README.md) is also available.
