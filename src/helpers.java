import krpc.client.RPCException;
import krpc.client.services.SpaceCenter;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.File;
import java.util.concurrent.CompletableFuture;


public class helpers {

    public static void playAudio(String filename) {
        CompletableFuture.runAsync(() -> {
            try {
                Clip clip = AudioSystem.getClip();
                clip.open(AudioSystem.getAudioInputStream(new File(filename)));
                clip.start();
                Thread.sleep(clip.getMicrosecondLength()/1000);
            }
            catch (Exception exc) {
                exc.printStackTrace(System.out);
            }
        });
    }

    public static double calculateSuicideBurnDistance(SpaceCenter.Vessel vessel, SpaceCenter.Flight flight, double vesselVerticalVelocity, double vesselAltitude) throws RPCException {

        double availableThrust = vessel.getAvailableThrust()/1000;
        double mass = vessel.getMass()/1000;
        double gravity = vessel.getOrbit().getBody().getSurfaceGravity();
        double TWR = availableThrust/(mass*gravity);
//        double drag0 = flight.getDrag().getValue0();
//        double drag1 = flight.getDrag().getValue1();
//        double drag2 = flight.getDrag().getValue2();
//        double ISP = vessel.getSpecificImpulse();

//        double eISP = availableThrust/(availableThrust * ISP);
//        double Ve = eISP * gravity;
//        double finalMass = Math.pow(Math.E, (-1*vesselVerticalVelocity/Ve));
//        double maximumAcceleration = (availableThrust / mass) / 2;
//        double maximumAcceleration1 = availableThrust / finalMass;
//        double maximumAcceleration = maximumAcceleration0 + maximumAcceleration1;
//        double suicideBurnDistance = Math.pow(vesselVerticalVelocity, 2)/(2*maximumAcceleration);
        double maximumAcceleration = TWR * gravity;
        double timeToBurn = Math.abs(vesselVerticalVelocity)/maximumAcceleration;
        double suicideBurnDistance = (Math.abs(vesselVerticalVelocity)*timeToBurn)+(0.5*(maximumAcceleration*Math.pow(timeToBurn, 2)));

//        System.out.printf("[DEBUG] Altitude: %f\n", vesselAltitude);
//        System.out.printf("[DEBUG] Vertical velocity: %f\n", vesselVerticalVelocity);
//        System.out.printf("[DEBUG] Available thrust: %f\n", availableThrust);
//        System.out.printf("[DEBUG] Mass: %f\n", mass);
//        System.out.printf("[DEBUG] Gravity: %f\n", gravity);
//        System.out.printf("[DEBUG] Drag0: %f\n", drag0);
//        System.out.printf("[DEBUG] Drag1: %f\n", drag1);
//        System.out.printf("[DEBUG] Drag2: %f\n", drag2);
//        System.out.printf("[DEBUG] ISP: %f\n", ISP);
//        System.out.printf("[DEBUG] TWR: %f\n", TWR);
//        System.out.printf("[DEBUG] Maximum acceleration: %f\n", maximumAcceleration);
//        System.out.printf("[DEBUG] Time to burn: %f\n", timeToBurn);
//        System.out.printf("[DEBUG] Suicide burn distance: %f\n", suicideBurnDistance);
//        System.out.println("---------------------------");

        return suicideBurnDistance * 1.1;
    }

}