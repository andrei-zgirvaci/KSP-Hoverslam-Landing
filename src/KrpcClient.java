import krpc.client.Connection;
import krpc.client.services.SpaceCenter;
import krpc.client.services.SpaceCenter.Vessel;
import krpc.client.services.SpaceCenter.Camera;
import krpc.client.services.UI;
import krpc.client.RPCException;
import org.javatuples.Pair;
import java.io.IOException;


public class KrpcClient {

    private static SpaceCenter spaceCenter;
    public static Connection connection;

    public static UI.Text vesselVerticalVelocity;
    public static UI.Text vesselAltitude;

    public static void connectToKrpcServer(String serverName, String serverAddress, int rpcPort, int streamPort) throws IOException {
        System.out.println("[!] Connecting to KRPC server...");
        connection = Connection.newInstance(serverName, serverAddress, rpcPort, streamPort);
        System.out.printf("[+] Connected to KRPC server: %s\n", serverName);
        spaceCenter = SpaceCenter.newInstance(connection);
    }

    public static void closeConnectionToServer() throws IOException {
        connection.close();
    }

    public static void drawUI() throws RPCException {
        UI ui = UI.newInstance(connection);
        UI.Canvas canvas = ui.getStockCanvas();

        Pair<Double, Double> screenSize = canvas.getRectTransform().getSize();
        UI.Panel panel = canvas.addPanel(true);
        panel.getRectTransform().setPosition(Pair.with(screenSize.getValue0() / 2 - 200, 0.0));
        panel.getRectTransform().setSize(Pair.with(400.0, 200.0));

        Pair<Double, Double> panelScreenSize = panel.getRectTransform().getSize();
        vesselAltitude = panel.addText("Altitude: 0", true);
        vesselAltitude.getRectTransform().setPosition(Pair.with(-panelScreenSize.getValue0()/4, +25.0));
        vesselAltitude.setSize(20);

        vesselVerticalVelocity = panel.addText("Velocity: 0", true);
        vesselVerticalVelocity.setSize(20);
        vesselVerticalVelocity.getRectTransform().setPosition(Pair.with(-panelScreenSize.getValue0()/4, -25.0));
    }

    public static void writeText(UI.Text textPanel, double value) throws RPCException {
        if (textPanel == vesselAltitude) {
            vesselAltitude.setContent(String.format("Altitude: %.2f", value));
        }
        else if (textPanel == vesselVerticalVelocity) {
            vesselVerticalVelocity.setContent(String.format("Velocity: %.2f", value));
        }
    }

    public static Vessel getActiveVessel() throws RPCException {
        return spaceCenter.getActiveVessel();
    }

    public static void setCameraToVessel() throws RPCException {
        Camera camera = spaceCenter.getCamera();
        camera.setHeading(90);
        camera.setPitch(0);
        camera.setDistance(60);
    }

    public static void loadLaunchSave(String saveName) throws RPCException {
        System.out.printf("[!] Loading save: '%s'\n", saveName);
        spaceCenter.load(saveName);
        System.out.printf("[+] Save '%s' loaded\n", saveName);
    }

}