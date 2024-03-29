package biz.neustar.tdi;

import java.io.IOException;
import java.util.Map;
import java.util.List;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import biz.neustar.tdi.fw.canonicalmessage.TdiCanonicalMessageShape;
import biz.neustar.tdi.fw.wrapper.TdiSdkWrapperShape;
import biz.neustar.tdi.fw.platform.TdiPlatformShapeFactory;
import biz.neustar.tdi.fw.plugin.TdiPluginBaseFactory;
import biz.neustar.tdi.fw.utils.Utils;
import biz.neustar.tdi.fw.exception.InvalidFormatException;

import biz.neustar.tdi.Config;


public class NTDIGateway extends NTDIDevice {
    private static Logger log = LoggerFactory.getLogger(NTDIGateway.class);

    public NTDIGateway() throws ExecutionException, InterruptedException, IOException {
        super();
    }

    public NTDIGateway(Config config) throws ExecutionException, InterruptedException, IOException {
        super(config);
    }

    public NTDIGateway(String configPath) throws ExecutionException, InterruptedException, IOException {
        super(configPath);
    }

    public NTDIGateway(List<TdiPluginBaseFactory> plugins, Config config) throws ExecutionException, InterruptedException, IOException {
        super(plugins, config);
    }

    public NTDIGateway(TdiPlatformShapeFactory platform, List<TdiPluginBaseFactory> plugins, Config config) throws ExecutionException, InterruptedException, IOException {
        super(platform, plugins, config);
    }

    public String cosign(String msg) throws ExecutionException, InterruptedException {
        log.debug("cosigning {}", msg);
        return ((TdiCanonicalMessageShape) this.sdk.api("CosignFlow").apply(msg).get()).getBuiltMessage();
    }

    public String verifyFromDevice(String msg) throws ExecutionException, InterruptedException {
        log.debug("verifying from device(s) {}", msg);
        return ((String) this.sdk.api("VerifyGeneralFlow").apply(msg).get());
    }
}
