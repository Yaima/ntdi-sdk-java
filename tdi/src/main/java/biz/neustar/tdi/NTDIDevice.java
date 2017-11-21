package biz.neustar.tdi;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class NTDIDevice extends BaseNTDI {
    private static Logger log = LoggerFactory.getLogger(NTDIDevice.class);

    public NTDIDevice() throws ExecutionException, InterruptedException, IOException {
        super();
    }

    public String verify(String msg) throws ExecutionException, InterruptedException {
        log.debug("verifying {}", msg);
        return ((String) this.sdk.api("VerifyFlow").apply(msg).get());
    }
}
