
# Using the NTDI Core CLI to Configure the Example

## Install the CLI

The CLI used to create keys and register them with the TDI Core can be found [here](https://pypi.python.org/pypi/oneID-cli).
```
$ pip install oneid-cli
```

## Create API Credentials
```
$ export NTDI_CORE_SERVER_BASE_URL=https://api-demo.oneid.com
$ oneid-cli configure --yes --name "Joe Admin <admin@joe.example.com>"
```

## Creating the Fleet
```
$ oneid-cli create-project --yes --name "My Project" --keystore ./fleet-keystore.json --use-jwk-keystore
```
Record the Fleet ID, as it will be needed in subsequent steps.
```
$ export FLEET_ID=xxxx
```

### Create and Configure the Gateway
```
$ oneid-cli provision --yes --project_id $FLEET_ID --name "My Gateway" --type edge_device --keystore ./device-gateway/src/main/resources/tdi/keystore.json --use-jwk-keystore
```
Record the Gateway ID, as it will be needed for the sensor(s).

Copy `./device-gateway/src/main/resources/app/sample-config.json` to `./device-gateway/src/main/resources/app/config.json` and open in an editor.

Replace `<fleetID>` with the value saved during Fleet creation, and `<gatewayID>` with the value saved above.

### Create and Configure the Sensor(s)
```
$ oneid-cli provision --yes --project_id $FLEET_ID --name "My Motion/Temperature Sensor" --type edge_device --keystore ./device-sensor/src/main/resources/tdi/keystore.json --use-jwk-keystore
```
Copy `./device-sensor/src/main/resources/app/sample-config.json` to `./device-sensor/src/main/resources/app/config.json` and open in an editor.

Replace `<fleetID>` with the value saved during Fleet creation, and `<gatewayID>` and `<deviceID>` with the values saved above.

### Connect Gateway and Sensors
(add sensor ID(s) to gateway config)

### Create and Configure the Servers
```
$ oneid-cli provision --yes --project_id $FLEET_ID --name "My Data Collection Service" --type server --keystore ./server-data-collector/src/main/resources/tdi/keystore.json --use-jwk-keystore
$ oneid-cli provision --yes --project_id $FLEET_ID --name "My Action Service" --type server --keystore ./server-actuator/src/main/resources/tdi/keystore.json --use-jwk-keystore
```
Copy `./server-actuator/src/main/resources/app/sample-config.json` to `./server-actuator/src/main/resources/app/config.json` and open in an editor.

Replace `<fleetID>` with the value saved during Fleet creation, and `<gatewayID>` and `<deviceID>` with the values saved above. There is room for a large number of gateways and sensors, and that the sensors are listed under each gateway that controls them.

Note that the configuration and keystores of the two services is nearly identical, other than the IDs of the services themselves.

### Keystores

For the most part, the keystores will be created only with their own private key data. When this data is shared, the `"d"` value, being the key's secret, should not be copied.

All entities will need to include the Fleet key information we saved above in `./fleet-keystore.json`.

Note that the servers will need to be able to sign on behalf of the Fleet, so they will need the Fleet private key information. However, the sensors and gateways will not.

In a real deployment, a more secure Platform would be used than the example one used here. Key management for these platforms should provide a more secure way to store keys.

## Revoking Devices

Individual sensors, gateways, or even services can be revoked if their keys are compromised.
```
$ oneid-cli revoke --project_id $FLEET_ID --type server -i <serverID>
$ oneid-cli revoke --project_id $FLEET_ID --type edge_device -i <deviceID>
```

If necessary, the entire Fleet can be as well:
```
$ oneid-cli revoke --project_id $FLEET_ID
```

A corresponding `unrevoke` command is also available.

For more information, try
```
$ oneid-cli --help
```
