import krpc.client.RPCException;
import krpc.client.StreamException;
import java.io.IOException;

public class PerformSuicideBurn {

    public static void main(String[] args) throws IOException, RPCException, InterruptedException, StreamException {
        KrpcClient.connectToKrpcServer("Default Server", "127.0.0.1", 50000, 50001);
        KrpcClient.loadLaunchSave("launch");
        KrpcClient.drawUI();
        VesselAutomationSystem vesselAutomationSystem = new VesselAutomationSystem(KrpcClient.getActiveVessel(), 45 );
        KrpcClient.setCameraToVessel();
        if (vesselAutomationSystem.launch()) {
            if (vesselAutomationSystem.reachAltitudeByPitchAndHeading(1000, 85, 360)) {
                vesselAutomationSystem.land();
            }
        }
        KrpcClient.closeConnectionToServer();
    }

}