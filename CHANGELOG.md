<a name="1.1.0"></a>
#1.1.0 (2017-11-30)


### Features

* **tdi:** Add code for any external config path ([9609e283](https://github.com/Neustar-TDI/ntdi-sdk-java/commit/9609e283d880845800b8cd03870a478f11543e41))
* Asynchronous cleanup ([238933e3...3dd40995](https://github.com/Neustar-TDI/ntdi-sdk-java/compare/238933e32e9d5eed6c4b0564e4da6cd676b8e661...3dd409953d9a67c9664d3cfb2e9e35bfd16e02c4))
* **examples:** Modified gateway controller flow to allow verification toggle ([40154b08](https://github.com/Neustar-TDI/ntdi-sdk-java/commit/c1d6eaed82d91aa3480b6df40154b08d2249fae7))

### BREAKING CHANGES

* **config:** The Config class constructor now uses `FileInputStream` instead of `getResourceAsStream` when 
loading the given `configPath`.


<a name="1.0.1"></a>
#1.0.1 (2017-11-22)


### Bug Fixes

* **FleetSigner:** improvements ([b5c6055](https://github.com/Neustar-TDI/java-ntdi/commit/b5c6055))
* **Utils:** generalize JSON processing ([baab29e](https://github.com/Neustar-TDI/java-ntdi/commit/baab29e))


### Features

* **tdi:** Add top-level classes ([c42add3](https://github.com/Neustar-TDI/java-ntdi/commit/c42add3))
* **tdi:** build out comprehensive ctor set (#25) ([09d3c98](https://github.com/Neustar-TDI/java-ntdi/commit/09d3c98))



