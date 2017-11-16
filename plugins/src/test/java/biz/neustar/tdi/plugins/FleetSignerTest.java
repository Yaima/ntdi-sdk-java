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
		deviceSdk = new EasySdk(Arrays.asList(FleetSigner::new), "device/config.json");
	}

	@Test
	public void testFleetCosign() throws ExecutionException, InterruptedException {
		String cosignedMsg = serverSdk.cosign(serverSdk.sign("testMessage"));
		final String logMsg = String.format("Cosign message: [%s]: ", cosignedMsg);
		log.info(logMsg);
		assertNotNull(cosignedMsg);
	}

	@Test
	public void testSignToken() throws ExecutionException, InterruptedException {
		String signedMsg = serverSdk.sign("testMessage");
		final String logMsg = String.format("Sign message: [%s]: ", signedMsg);
		log.info(logMsg);
		assertNotNull(signedMsg);
	}

}
