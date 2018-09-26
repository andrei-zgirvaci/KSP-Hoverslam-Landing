import krpc.client.RPCException;
import krpc.client.StreamException;
import java.io.IOException;

public class TestSuicideBurn {

    public static void main(String[] args) throws IOException, RPCException, InterruptedException, StreamException {
        KrpcClient.connectToKrpcServer("Default Server", "127.0.0.1", 50000, 50001);
        KrpcClient.loadLaunchSave("launch");
        KrpcClient.drawUI();
        VesselAutomationSystem vesselAutomationSystem = new VesselAutomationSystem(KrpcClient.getActiveVessel(), 24);
        KrpcClient.setCameraToVessel();
        if (vesselAutomationSystem.launch()) {
            if (vesselAutomationSystem.reachAltitude(500)) {
                vesselAutomationSystem.land();
            }
        }
        KrpcClient.closeConnectionToServer();
    }

}