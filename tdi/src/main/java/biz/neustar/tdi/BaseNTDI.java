package biz.neustar.tdi;

import java.io.IOException;
import java.util.Map;
import java.util.List;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import biz.neustar.tdi.fw.canonicalmessage.TdiCanonicalMessageShape;
import biz.neustar.tdi.fw.wrapper.TdiSdkWrapperShape;
import biz.neustar.tdi.fw.plugin.TdiPluginBaseFactory;
import biz.neustar.tdi.fw.platform.TdiPlatformShapeFactory;
import biz.neustar.tdi.fw.utils.Utils;
import biz.neustar.tdi.fw.exception.InvalidFormatException;

import biz.neustar.tdi.Config;


class BaseNTDI {
    private static Logger log = LoggerFactory.getLogger(BaseNTDI.class);

    public static final String DEFAULT_CONFIG_PATH = "tdi/config.json";

    protected TdiSdkWrapperShape sdk;

    public BaseNTDI() throws ExecutionException, InterruptedException, IOException {
        this(null, null, null);
    }

    public BaseNTDI(Config config) throws ExecutionException, InterruptedException, IOException {
        this(null, null, config);
    }

    public BaseNTDI(String configPath) throws ExecutionException, InterruptedException, IOException {
        this(null, null, new Config(configPath));
    }

    public BaseNTDI(List<TdiPluginBaseFactory> plugins, Config config) throws ExecutionException, InterruptedException, IOException {
        this(null, plugins, config);
    }

    public BaseNTDI(TdiPlatformShapeFactory platform, List<TdiPluginBaseFactory> plugins, Config config) throws ExecutionException, InterruptedException, IOException {
        if (config == null) config = new Config(DEFAULT_CONFIG_PATH);
        this.sdk = (new NTDIFactory()).setup(platform, plugins, config).get();
    }

    public String sign(Map<String, Object> data) throws ExecutionException, InterruptedException, InvalidFormatException {
        // TODO: better format for payload
        return sign(Utils.mapToJson(data));
    }

    public String sign(String data) throws ExecutionException, InterruptedException {
        log.debug("signing {}", data);
        return ((TdiCanonicalMessageShape) this.sdk.api("SignFlow").apply(data).get()).getBuiltMessage();
    }
}
