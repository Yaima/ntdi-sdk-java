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
	static EasySdk serverSdk;
	static EasySdk deviceSdk;
	static final Logger log = LoggerFactory.getLogger(FleetSignerTest.class);

	@BeforeClass
	public static void setup() throws ExecutionException, InterruptedException {
		serverSdk = new EasySdk(Arrays.asList(FleetSigner::new), "server/config.json");
		deviceSdk = new EasySdk(null, "device/config.json");
	}

	@Test
	public void tesFleetSign() throws ExecutionException, InterruptedException {
		String signedMsg = serverSdk.sign("testMessage");
		final String logMsg = String.format("Sign message: [%s]: ", signedMsg);
		log.info(logMsg);
		assertNotNull(signedMsg);
	}

	@Test
	public void testFleetCosign() throws ExecutionException, InterruptedException {
		String cosignedMsg = serverSdk.cosign(serverSdk.sign("testMessage"));
		final String logMsg = String.format("Cosign message: [%s]: ", cosignedMsg);
		log.info(logMsg);
		assertNotNull(cosignedMsg);
	}

	@Test
	public void testFleetVerify() throws ExecutionException, InterruptedException {
		String fleetToDeviceMsg = serverSdk.fleetToDevice(serverSdk.sign("testMessage"));
		String verifiedMsg = deviceSdk.verify(fleetToDeviceMsg);
		final String logMsg = String.format("Verify message: [%s]: ", verifiedMsg);
		log.info(logMsg);
		assertNotNull(verifiedMsg);
	}

	@Test
	public void testFleetToDevice() throws ExecutionException, InterruptedException {
		String fleetToDeviceMsg = serverSdk.fleetToDevice("message to device");
		final String logMsg = String.format("Fleet to device message: [%s]: ", fleetToDeviceMsg);
		log.info(logMsg);
		assertNotNull(fleetToDeviceMsg);
	}

	@Test
	public void testFleetFromDevice() throws ExecutionException, InterruptedException {
		String deviceMsg = deviceSdk.sign("fromDeviceMsg");
		String fleetFromDeviceMsg = serverSdk.fleetFromDevice(deviceMsg);
		final String logMsg = String.format("Device to fleet msg: [%s]: ", fleetFromDeviceMsg);
		log.info(logMsg);
		assertNotNull(fleetFromDeviceMsg);
	}

}
