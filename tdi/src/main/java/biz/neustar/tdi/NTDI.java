package biz.neustar.tdi;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import biz.neustar.tdi.fw.canonicalmessage.TdiCanonicalMessageShape;
import biz.neustar.tdi.fw.wrapper.TdiSdkWrapperShape;
import biz.neustar.tdi.fw.plugin.TdiPluginBaseFactory;
import biz.neustar.tdi.fw.utils.Utils;
import biz.neustar.tdi.plugins.FleetSigner;
import biz.neustar.tdi.platform.Platform;
import biz.neustar.tdi.sdk.TdiSdk;
import biz.neustar.tdi.sdk.TdiSdkOptions;
import biz.neustar.tdi.fw.exception.InvalidFormatException;

import biz.neustar.tdi.Config;


public class NTDI {
    protected static Logger log = LoggerFactory.getLogger(NTDI.class);

    public static final String DEFAULT_CONFIG_PATH = "tdi/config.json";

    protected TdiSdkWrapperShape sdk;

    public NTDI() throws ExecutionException, InterruptedException, IOException {
        this(null, null);
    }

    public NTDI(Config config) throws ExecutionException, InterruptedException, IOException {
        this(null, config);
    }

    public NTDI(String configPath) throws ExecutionException, InterruptedException, IOException {
        this(null, new Config(configPath));
    }

    public NTDI(List<TdiPluginBaseFactory> plugins, Config config) throws ExecutionException, InterruptedException, IOException {
        if (config == null) config = new Config(DEFAULT_CONFIG_PATH);
        this.sdk = (new NTDIFactory()).setup(plugins, config).get();
    }

    public String sign(Map<String, Object> data) throws ExecutionException, InterruptedException, InvalidFormatException {
        // TODO: better format for payload
        return sign(Utils.mapToJson(data));
    }

    public String sign(String data) throws ExecutionException, InterruptedException {
        log.debug("signing {}", data);
        return ((TdiCanonicalMessageShape) this.sdk.api("SignFlow").apply(data).get()).getBuiltMessage();
    }

    public String cosign(String msg) throws ExecutionException, InterruptedException {
        log.debug("cosigning {}", msg);
        return ((TdiCanonicalMessageShape) this.sdk.api("CosignFlow").apply(msg).get()).getBuiltMessage();
    }

    public String verify(String msg) throws ExecutionException, InterruptedException {
        log.debug("verifying {}", msg);
        return ((String) this.sdk.api("VerifyFlow").apply(msg).get());
    }
}
