# Java TDI Framework

This is Neustar's Trusted Device Identity (TDI) framework implemented in Java. It forms the definition and glue layer between the following...

| Namespace | Purpose |
| --- | :-- |
User application | The software that is importing and leveraging the library.
[SDK](https://github.com/Neustar-TDI/ntdi-sdk-java/sdk) | The application-facing SDK that represents the API to the library.
[Plugins](https://github.com/Neustar-TDI/ntdi-sdk-java/plugins) | Augments functionality of TDI. Generally to add message handlers and work-flows such that they need not be re-written for every discrete usage in a given network application.
[Platform](https://github.com/Neustar-TDI/ntdi-sdk-java/examples/platform) | The boundary between TDI and the hardware.

Types and interfaces are contained in the framework, as well as a handful of library constants.

## Important application-facing classes

Despite SDK being the application API, application code will probably need one or more of the classes in the framework namespace. The following are classes that serve common needs...

All given class names are under the common namespace `biz.neustar.tdi.fw`. This is omitted for brevity.

##### canonicalmessage.TdiCanonicalMessageShape
*Canonical: (adjective) In linguistics, of a form or pattern. Characteristic. General or basic.*

This is TDI's normalized representation of a network message. It is invariant with respect to wire format and transport, and represents the authenticated envelope for user payloads.

##### keystructure.TdiKeyStructureShape
TDI's platform normalizes various implementations of cryptographic keys into this interface. TDI metadata about keys, and the keys themselves are made available by this interface.

##### plugin.TdiPluginBase
The default framework is rather bare. Typically, a user application will want to write one or more plugins to handle its workflows. To the extent that commonly-needed features of a network application have been anticipated, Neustar has bundled a set of default plugins for applications to use (none of which are required to use the library).

##### platform.TdiPlatformShape
All support flows from hardware. This interface allows the hardware implementations to change independently of the software that relies on it. The library relies on this abstraction, but user application code need not.

##### platform.facet.TdiPlatformFacetShape
If you are needing to implement specific cryptographic hardware, clocks, or data persistence strategies, this is the interface that will allow for it. More details can be found in [Platform](https://github.com/Neustar-TDI/ntdi-sdk-java/examples/platform).
