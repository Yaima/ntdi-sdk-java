package biz.neustar.tdi;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import biz.neustar.tdi.fw.platform.TdiPlatformShapeFactory;
import biz.neustar.tdi.platform.Platform;
import biz.neustar.tdi.fw.plugin.TdiPluginBaseFactory;
import biz.neustar.tdi.fw.wrapper.TdiSdkWrapperShape;
import biz.neustar.tdi.sdk.TdiSdk;
import biz.neustar.tdi.sdk.TdiSdkOptions;


class NTDIFactory {
    private static Logger log = LoggerFactory.getLogger(NTDIFactory.class);

    // ASYNC INIT
    public CompletableFuture<TdiSdkWrapperShape> setup(Config config) {
        return setup(null, null, config);
    }

    public CompletableFuture<TdiSdkWrapperShape> setup(List<TdiPluginBaseFactory> plugins, Config config) {
        return setup(null, plugins, config);
    }

    public CompletableFuture<TdiSdkWrapperShape> setup(TdiPlatformShapeFactory platform, List<TdiPluginBaseFactory> plugins, Config config) {
        if (platform == null) platform = Platform::new;
        TdiSdkOptions sdkOptions = new TdiSdkOptions();
        sdkOptions.platform = platform;
        sdkOptions.plugins = plugins;
        if (config != null) sdkOptions.config = config.map;

        log.debug("config={}", sdkOptions.config);

        return (new TdiSdk(sdkOptions)).init();
    }
}
