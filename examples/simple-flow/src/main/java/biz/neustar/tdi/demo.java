package biz.neustar.tdi;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class demo {

  public static void main(String[] args) throws InterruptedException, ExecutionException, IOException {

    NTDIDevice device = new NTDIDevice("data/device/config.json");
    NTDIFleet fleet = new NTDIFleet("data/server/config.json");
    NTDIGateway gateway = new NTDIGateway("data/gateway/config.json");

    System.out.println("\n**** Device to Fleet Flow ****");

    System.out.println("\n---- Device ----");
    String deviceData = "Hello Fleet, I'm a device.";
    String msg = device.sign(deviceData);
    System.out.println("Device to Gateway Msg: " + msg);

    System.out.println("\n---- Gateway ----");
    System.out.println("Gateway Verified:" + gateway.verifyFromDevice(msg));
    String gwMsg = gateway.cosign(msg);
    System.out.println("Gateway to Server Msg: " + gwMsg);

    System.out.println("\n---- FleetServer ----");
    System.out.println("Server Verified:" + fleet.verifyFromDevice(gwMsg));


    System.out.println("\n**** Fleet to Device Flow ****");

    System.out.println("\n---- Server ----");
    String fleetData = "Hello Device, I'm the fleet.";
    String fleetMsg = fleet.signForFleet(fleetData);
    System.out.println("\n\nFleet Data: " + fleetData);
    System.out.println("\n\nFleet to Gateway Msg: " + fleetMsg);

    System.out.println("\n---- Gateway ----");
    System.out.println("Gateway Verified:" + gateway.verify(fleetMsg));
    String fleetGwMsg = gateway.cosign(fleetMsg);
    System.out.println("Gateway to Device Msg: " + fleetGwMsg);

    System.out.println("\n---- Device ----");
    System.out.println("Device Verified:" + device.verify(fleetGwMsg));

  }

}

