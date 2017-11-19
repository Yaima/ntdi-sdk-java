package biz.neustar.tdi;

import java.io.IOException;
import java.util.Map;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import biz.neustar.tdi.plugins.FleetSigner;
import biz.neustar.tdi.fw.utils.Utils;
import biz.neustar.tdi.fw.exception.InvalidFormatException;


public class NTDIFleet extends BaseNTDI {
    private static Logger log = LoggerFactory.getLogger(NTDIFleet.class);

    public NTDIFleet() throws ExecutionException, InterruptedException, IOException {
        super(Arrays.asList(FleetSigner::new), null);
    }

    public NTDIFleet(String configPath) throws ExecutionException, InterruptedException, IOException {
        super(Arrays.asList(FleetSigner::new), new Config(configPath));
    }

    public NTDIFleet(Config config) throws ExecutionException, InterruptedException, IOException {
        super(Arrays.asList(FleetSigner::new), config);
    }

    public String signForFleet(Map<String, Object> data) throws ExecutionException, InterruptedException, InvalidFormatException {
        // TODO: better format for payload
        return signForFleet(Utils.mapToJson(data));
    }

    public String signForFleet(String data) throws ExecutionException, InterruptedException {
        log.debug("signForFleet {}", data);
        return (((FleetSigner) sdk.plugin("FleetSigner")).fleetToDevice.apply(this.sign(data)).get()).getBuiltMessage();
    }

    public String verifyFromDevice(String msg) throws ExecutionException, InterruptedException {
        log.debug("verifyFromDevice {}", msg);
        return ((FleetSigner) sdk.plugin("FleetSigner")).fleetFromDevice.apply(msg).get().getBuiltMessage();
    }
}
