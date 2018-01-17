package biz.neustar.tdi.plugins;

import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.concurrent.ExecutionException;

import java.net.HttpURLConnection;
import java.net.URL;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.runner.RunWith;
import org.junit.BeforeClass;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(PowerMockRunner.class)
@PrepareForTest({FleetSigner.class})
public class FleetSignerTest {
  static final Logger log = LoggerFactory.getLogger(FleetSignerTest.class);
  String logMsg;

  private NTDIHelper serverSdk;
  private NTDIHelper deviceSdk;
  private NTDIHelper serverSdkException;

  private void mockCosigner() throws Exception {
    mockCosigner(HttpURLConnection.HTTP_OK, null);
  }

  private void mockCosigner(int responseCode) throws Exception {
    mockCosigner(responseCode, null);
  }

  private void mockCosigner(Exception throwException) throws Exception {
    mockCosigner(HttpURLConnection.HTTP_NO_CONTENT, throwException);
  }

  private void mockCosigner(int responseCode, Exception throwException) throws Exception {
    HttpURLConnection mockConn = mock(HttpURLConnection.class);
    PowerMockito.when(mockConn.getResponseCode()).thenReturn(responseCode);

    ByteArrayOutputStream outStream = new ByteArrayOutputStream();
    PowerMockito.when(mockConn.getOutputStream()).thenReturn(outStream);

    if (null != throwException) {
      PowerMockito.when(mockConn.getInputStream()).thenThrow(throwException);
    }
    else if (responseCode != HttpURLConnection.HTTP_OK) {
      ByteArrayInputStream inStream = new ByteArrayInputStream("".getBytes());
      PowerMockito.when(mockConn.getInputStream()).thenReturn(inStream);
    }
    else {
      NTDIHelper cosignerSDK = new NTDIHelper(null, "cosigner/config.json");

      String msg = deviceSdk.sign("cosigned device message");
      String cosignedMsg = cosignerSDK.cosign(msg);

      ByteArrayInputStream inStream = new ByteArrayInputStream(cosignedMsg.getBytes());
      PowerMockito.when(mockConn.getInputStream()).thenReturn(inStream);
    }
    URL mockURL = mock(URL.class);
    PowerMockito.whenNew(URL.class).withArguments(matches("https?://mocked.example.com/(fleet|project)s/.*?/cosign/?")).thenReturn(mockURL);
    PowerMockito.when(mockURL.openConnection()).thenReturn(mockConn);
    PowerMockito.when(mockURL.toString()).thenReturn("mocked-url-" + responseCode);
  }

  @Before
  public void setup() throws Exception {
    deviceSdk = new NTDIHelper(null, "device/config.json");
    serverSdk = new NTDIHelper(Arrays.asList(FleetSigner::new), "server/config.json");
    serverSdkException = new NTDIHelper(Arrays.asList(FleetSigner::new), "exception/config.json");
  }

  @Test
  public void testFleetSign() throws Exception {
    String signedMsg = serverSdk.sign("test message");
    logMsg = String.format("Sign msg: [%s]: ", signedMsg);
    log.info(logMsg);
    assertNotNull(signedMsg);
  }

  @Test
  public void testFleetCosign() throws Exception {
    String cosignedMsg = serverSdk.cosign(serverSdk.sign("test message"));
    logMsg = String.format("Cosign message: [%s]: ", cosignedMsg);
    log.info(logMsg);
    assertNotNull(cosignedMsg);
  }

  @Test
  public void testFleetVerify() throws Exception {
    mockCosigner();

    String fleetToDeviceMsg = serverSdk.fleetToDevice(serverSdk.sign("test message"));
    String verifiedMsg = deviceSdk.verify(fleetToDeviceMsg);
    logMsg = String.format("Verify message: [%s]: ", verifiedMsg);
    log.info(logMsg);
    assertNotNull(verifiedMsg);
  }

  @Test
  public void testFleetToDevice() throws Exception {
    mockCosigner();

    String fleetToDeviceMsg = serverSdk.fleetToDevice("message to device");
    logMsg = String.format("Fleet to device message: [%s]: ", fleetToDeviceMsg);
    log.info(logMsg);
    assertNotNull(fleetToDeviceMsg);
  }

  @Test
  public void testFleetFromDevice() throws Exception {
    mockCosigner();

    String deviceMsg = deviceSdk.sign("message from device");
    String fleetFromDeviceMsg = serverSdk.fleetFromDevice(deviceMsg);
    logMsg = String.format("Device to fleet message: [%s]: ", fleetFromDeviceMsg);
    log.info(logMsg);
    assertNotNull(fleetFromDeviceMsg);
  }

  @Test(expected = ExecutionException.class)
  public void testFleetToDeviceException() throws Exception {
    mockCosigner();
    serverSdkException.fleetToDevice("message to device");
  }

  @Test(expected = ExecutionException.class)
  public void testFleetFromDeviceException() throws Exception {
    mockCosigner();
    serverSdkException.fleetFromDevice("message from device");
  }

  @Test(expected = ExecutionException.class)
  public void testFleetToDeviceSendToCosignerException() throws Exception {
    mockCosigner(new IOException("something happened"));
    serverSdk.fleetToDevice("message to device");
  }

  @Test(expected = ExecutionException.class)
  public void testFleetToDeviceSendToCosigner204Response() throws Exception {
    mockCosigner(HttpURLConnection.HTTP_NO_CONTENT);
    serverSdk.fleetToDevice("message to device");
  }

  @Test(expected = ExecutionException.class)
  public void testFleetToDeviceSendToCosigner500Response() throws Exception {
    mockCosigner(HttpURLConnection.HTTP_INTERNAL_ERROR);
    serverSdk.fleetToDevice("message to device");
  }

  @Test(expected = ExecutionException.class)
  public void testFleetToDeviceSendToCosigner403Response() throws Exception {
    mockCosigner(HttpURLConnection.HTTP_FORBIDDEN);
    serverSdk.fleetToDevice("message to device");
  }
}
