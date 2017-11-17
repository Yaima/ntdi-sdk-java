package biz.neustar.tdi.plugins;

import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.concurrent.ExecutionException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import biz.neustar.tdi.fw.wrapper.TdiSdkWrapperShape;

public class FleetSignerTest {
	static TdiSdkWrapperShape sdkWrapper;
	static NTDI serverSdk;
	static NTDI deviceSdk;
	static NTDI serverSdkException;
	static final Logger log = LoggerFactory.getLogger(FleetSignerTest.class);
	String logMsg;

	@BeforeClass
	public static void setup() throws ExecutionException, InterruptedException {
		serverSdk = new NTDI(Arrays.asList(FleetSigner::new), "server/config.json");
		deviceSdk = new NTDI(null, "device/config.json");
		serverSdkException = new NTDI(Arrays.asList(FleetSigner::new), "exception/config.json");
	}

	@Test
	public void tesFleetSign() throws ExecutionException, InterruptedException {
		String signedMsg = serverSdk.sign("test message");
		logMsg = String.format("Sign msg: [%s]: ", signedMsg);
		log.info(logMsg);
		assertNotNull(signedMsg);
	}

	@Test
	public void testFleetCosign() throws ExecutionException, InterruptedException {
		String cosignedMsg = serverSdk.cosign(serverSdk.sign("test message"));
		logMsg = String.format("Cosign message: [%s]: ", cosignedMsg);
		log.info(logMsg);
		assertNotNull(cosignedMsg);
	}

	@Test
	public void testFleetVerify() throws ExecutionException, InterruptedException {
		String fleetToDeviceMsg = serverSdk.fleetToDevice(serverSdk.sign("test message"));
		String verifiedMsg = deviceSdk.verify(fleetToDeviceMsg);
		logMsg = String.format("Verify message: [%s]: ", verifiedMsg);
		log.info(logMsg);
		assertNotNull(verifiedMsg);
	}

	@Test
	public void testFleetToDevice() throws ExecutionException, InterruptedException {
		String fleetToDeviceMsg = serverSdk.fleetToDevice("message to device");
		logMsg = String.format("Fleet to device message: [%s]: ", fleetToDeviceMsg);
		log.info(logMsg);
		assertNotNull(fleetToDeviceMsg);
	}

	@Test
	public void testFleetFromDevice() throws ExecutionException, InterruptedException {
		String deviceMsg = deviceSdk.sign("message from device");
		String fleetFromDeviceMsg = serverSdk.fleetFromDevice(deviceMsg);
		logMsg = String.format("Device to fleet message: [%s]: ", fleetFromDeviceMsg);
		log.info(logMsg);
		assertNotNull(fleetFromDeviceMsg);
	}

	@Test(expected = ExecutionException.class)
	public void testFleetToDeviceException() throws ExecutionException, InterruptedException {
		serverSdkException.fleetToDevice("message to device");
	}

	@Test(expected = ExecutionException.class)
	public void testFleetFromDeviceException() throws ExecutionException, InterruptedException {
		serverSdkException.fleetFromDevice("message from device");
	}

}
