package biz.neustar.tdi.plugins;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import biz.neustar.tdi.fw.implementation.TdiFlowArguments;
import biz.neustar.tdi.fw.implementation.TdiImplementation;
import biz.neustar.tdi.fw.implementation.TdiImplementationShape;
import biz.neustar.tdi.fw.wrapper.TdiSdkWrapper;
import biz.neustar.tdi.fw.wrapper.TdiSdkWrapperShape;

public class FleetSignerTest {
	static TdiSdkWrapperShape sdkWrapper;
	static TdiImplementationShape impl;
	static Map<String, Object> config;
	static FleetSigner signer;
	static TdiFlowArguments flow;

	@BeforeClass
	public static void setup() throws Exception {
		sdkWrapper = new TdiSdkWrapper();
		flow = new TdiFlowArguments();
		config = new HashMap<>();
		Map<String, Object> testConfig = new HashMap<>();
		testConfig.put("k1", "v1");
		testConfig.put("k2", "v2");
		testConfig.put("k3", "v3");
		config.put("testData", testConfig);
		impl = new TdiImplementation(config, TestData.DummyPlatform::new);
		// signer = new FleetSigner(impl, sdkWrapper);
	}

	@Test
	public void testGetters()  {
		assertNotNull(impl.getPlatform());
		assertEquals(config, impl.getConfig());
	}
//
//	@Test
//	public void testFleetSign() {
//		assertNotNull(signer.fleetSign);
//	}
//
//	@Test
//	public void testSignToken() {
//		assertNotNull(signer.signToken);
//	}
//
//	@Test
//	public void testFleetCosign() {
//		assertNotNull(signer.fleetCosign);
//	}
//
//	@Test
//	public void testFleetVerify() {
//		assertNotNull(signer.fleetVerify);
//	}
//
//	@Test
//	public void testFleetToDevice() {
//		assertNotNull(signer.fleetToDevice);
//	}
//
//	@Test
//	public void testFleetFromDevice() {
//		assertNotNull(signer.fleetFromDevice);
//	}

}
