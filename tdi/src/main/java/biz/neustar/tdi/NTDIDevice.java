package biz.neustar.tdi;

import java.util.List;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import biz.neustar.tdi.fw.platform.TdiPlatformShapeFactory;
import biz.neustar.tdi.fw.plugin.TdiPluginBaseFactory;


public class NTDIDevice extends BaseNTDI {
    private static Logger log = LoggerFactory.getLogger(NTDIDevice.class);

    public NTDIDevice() throws ExecutionException, InterruptedException, IOException {
        super();
    }

    public NTDIDevice(Config config) throws ExecutionException, InterruptedException, IOException {
        super(config);
    }

    public NTDIDevice(String configPath) throws ExecutionException, InterruptedException, IOException {
        super(configPath);
    }

    public NTDIDevice(List<TdiPluginBaseFactory> plugins, Config config) throws ExecutionException, InterruptedException, IOException {
        super(plugins, config);
    }

    public NTDIDevice(TdiPlatformShapeFactory platform, List<TdiPluginBaseFactory> plugins, Config config) throws ExecutionException, InterruptedException, IOException {
        super(platform, plugins, config);
    }

    public String verify(String msg) throws ExecutionException, InterruptedException {
        log.debug("verifying {}", msg);
        return ((String) this.sdk.api("VerifyFlow").apply(msg).get());
    }
}
