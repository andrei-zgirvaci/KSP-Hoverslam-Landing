import krpc.client.*;
import krpc.client.Stream;
import krpc.client.services.SpaceCenter;
import krpc.client.services.SpaceCenter.*;
import krpc.client.services.KRPC;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;


public class VesselAutomationSystem {

    private Connection connection;
    private Flight flight;
    private Vessel vessel;
    private double vesselHeight;
    private Stream<Double> vesselAltitude;
    private Stream<Double> vesselVerticalVelocity;
    private KRPC krpc;

    VesselAutomationSystem(Vessel vessel, double vesselHeight) throws RPCException, StreamException {
        this.vessel = vessel;
        ReferenceFrame referenceFrame = vessel.getOrbit().getBody().getReferenceFrame();
        flight = vessel.flight(referenceFrame);
        connection = KrpcClient.connection;
        vesselAltitude = connection.addStream(flight, "getSurfaceAltitude");
        vesselVerticalVelocity = connection.addStream(flight, "getVerticalSpeed");
        this.vesselHeight = vesselHeight;
        krpc = KRPC.newInstance(connection);

        vesselVerticalVelocity.addCallback(
                (Double value) -> {
                    try {
                        KrpcClient.writeText(KrpcClient.vesselVerticalVelocity, value);
                    } catch (RPCException e) {
                        e.printStackTrace();
                    }
                });
        vesselVerticalVelocity.start();
        vesselAltitude.addCallback(
                (Double value) -> {
                    try {
                        KrpcClient.writeText(KrpcClient.vesselAltitude, value-(vesselHeight/2));
                    } catch (RPCException e) {
                        e.printStackTrace();
                    }
                });
        vesselAltitude.start();
    }

    public boolean launch() throws RPCException, InterruptedException {
        for (int stage=5; stage>=0; stage--) {
            switch (stage) {
                case 5:
                    helpers.playAudio("src/10-0_countdown.wav");
                    System.out.println("---------------------------");
                    System.out.println("[!] Preparing rocket for lunch");
                    TimeUnit.SECONDS.sleep(1);
                    if (vessel.getSituation() != SpaceCenter.VesselSituation.PRE_LAUNCH) {
                        System.out.println("[-] Rocket in not in pre launch situation");
                        abortLaunch();
                        return false;
                    }
                    System.out.println("---------------------------");
                    break;
                case 4 :
                    System.out.println("[!] Setting throttle -> 0");
                    vessel.getControl().setThrottle(0.0f);
                    System.out.println("[+] Throttle set -> 0");
                    System.out.println("---------------------------");
                    break;
                case 3 :
                    System.out.println("[!] Activating RCS and SAS");
                    TimeUnit.SECONDS.sleep(1);
                    vessel.getControl().setRCS(true);
                    vessel.getControl().setSAS(true);
                    vessel.getControl().setSASMode(SASMode.STABILITY_ASSIST);
                    System.out.println("[+] RCS activated");
                    System.out.println("[+] SAS activated");
                    System.out.println("---------------------------");
                    break;
                case 2 :
                    System.out.println("[!] Starting main engine");
                    TimeUnit.SECONDS.sleep(1);
                    vessel.getControl().activateNextStage();
                    System.out.println("[+] Main engine started");
                    System.out.println("---------------------------");
                    break;
                case 1:
                    System.out.println("[!] Setting throttle to maximum");
                    if (!setThrottle(vessel, 0, 1, 4)) {
                       return false;
                    }
                    System.out.println("---------------------------");
                    break;
                case 0 :
                    System.out.println("[!] Solid rocket booster ignition and liftoff");
                    TimeUnit.SECONDS.sleep(1);
                    vessel.getControl().activateNextStage();
                    if (vessel.getSituation() == SpaceCenter.VesselSituation.FLYING) {
                        System.out.println("[+] Rocket successfully launched");
                        System.out.println("---------------------------");
                        return true;
                    }
                    else {
                        System.out.println("[-] Couldn't launch the rocket");
                        abortLaunch();
                        return false;
                    }
            }
        }
        return false;
    }

    private void abortLaunch() throws InterruptedException, RPCException {
        System.out.println("---------------------------");
        System.out.println("[!] Aborting launch...");
        if (vessel.getControl().getThrottle() > 0) {
            System.out.println("[!] Killing main engine");
            TimeUnit.SECONDS.sleep(1);
            vessel.getControl().setThrottle(0.0f);
            System.out.println("[+] Throttle set -> 0");
        }
        System.out.println("---------------------------");
    }

    public boolean reachAltitude(double altitudeNeededToReach) throws RPCException, StreamException, InterruptedException {
        krpc.schema.KRPC.ProcedureCall surfaceAltitude = connection.getCall(flight, "getSurfaceAltitude");
        krpc.client.services.KRPC.Expression reachAltitudeExpression;
        Event reachAltitudeEvent;

        reachAltitudeExpression = KRPC.Expression.greaterThanOrEqual(connection,
            KRPC.Expression.call(connection, surfaceAltitude),
            KRPC.Expression.constantDouble(connection, altitudeNeededToReach / 9)
        );
        reachAltitudeEvent = krpc.addEvent(reachAltitudeExpression);
        synchronized (reachAltitudeEvent.getCondition()) {
            reachAltitudeEvent.waitFor();
            System.out.println("[!] Changing pitch and heading -> 45, 360 grades");
            TimeUnit.SECONDS.sleep(1);
            vessel.getControl().setRCS(false);
            vessel.getControl().setSAS(false);
            System.out.println("[+] RCS deactivated");
            System.out.println("[+] SAS deactivated");
            vessel.getAutoPilot().targetPitchAndHeading(80, 360);
            vessel.getAutoPilot().engage();

            System.out.println("[+] Pitch and heading set -> 45, 360 grades");
            System.out.println("---------------------------");
        }

        reachAltitudeExpression = KRPC.Expression.greaterThanOrEqual(connection,
                KRPC.Expression.call(connection, surfaceAltitude),
                KRPC.Expression.constantDouble(connection, altitudeNeededToReach / 4)
        );
        reachAltitudeEvent = krpc.addEvent(reachAltitudeExpression);
        synchronized (reachAltitudeEvent.getCondition()) {
            reachAltitudeEvent.waitFor();

            System.out.println("[!] Activating RCS and SAS");
            TimeUnit.SECONDS.sleep(1);
            vessel.getAutoPilot().disengage();
            vessel.getControl().setRCS(true);
            vessel.getControl().setSAS(true);
            vessel.getControl().setSASMode(SASMode.RADIAL);
            vessel.getControl().setSAS(true);
            vessel.getControl().setSASMode(SASMode.RADIAL);
            System.out.println("[+] RCS activated");
            System.out.println("[+] SAS activated");
            System.out.println("---------------------------");
        }

        reachAltitudeExpression = KRPC.Expression.greaterThanOrEqual(connection,
                KRPC.Expression.call(connection, surfaceAltitude),
                KRPC.Expression.constantDouble(connection, altitudeNeededToReach)
        );
        reachAltitudeEvent = krpc.addEvent(reachAltitudeExpression);
        synchronized (reachAltitudeEvent.getCondition()) {
            reachAltitudeEvent.waitFor();
            System.out.printf("[+] Vessel reached altitude: %f\n", altitudeNeededToReach);
            System.out.println("[!] Killing main engine");
            vessel.getControl().setThrottle(0);
            System.out.println("[+] Throttle set -> 0");
            System.out.println("---------------------------");
        }
        return true;
    }

    public boolean land() throws RPCException, StreamException {

        boolean initSuicideBurnStageStarted = false;

        System.out.println("---------------------------");
        while (true) {
            double suicideBurnDistance = helpers.calculateSuicideBurnDistance(vessel, flight, vesselVerticalVelocity.get(), vesselAltitude.get());

            if (vesselAltitude.get() - (vesselHeight / 2) <= 300) {
                vessel.getControl().setGear(true);
            }

            if (vesselAltitude.get() - (vesselHeight / 2) <= suicideBurnDistance && vesselVerticalVelocity.get() < 0) {
                if (!initSuicideBurnStageStarted) {
                    System.out.println("[!] Performing Suicide Burn");
                    System.out.println("[!] Setting throttle to maximum");
                    vessel.getControl().setThrottle(1);
                    initSuicideBurnStageStarted = true;
                    System.out.println("[+] Throttle set -> 1");
                    System.out.println("---------------------------");
                }
                else {
                    vessel.getControl().setThrottle(1);
                }
            }
            else {
                vessel.getControl().setThrottle(0);
            }

            if (vessel.getSituation() == VesselSituation.LANDED && vesselVerticalVelocity.get() > 1 && vesselVerticalVelocity.get() < 1) {
                System.out.println("[+] Landed successfully");
                System.out.println("[!] Killing main engine");
                vessel.getControl().setThrottle(0);
                System.out.println("[+] Throttle set -> 0");
                System.out.println("---------------------------");
                return true;
            }
        }
    }

    private boolean setThrottle(Vessel vessel, float initThrottle, float endThrottle, float timeInSeconds) throws InterruptedException, RPCException {
        final CountDownLatch latch = new CountDownLatch(1);
        final int period = 50;
        new Timer().scheduleAtFixedRate(new TimerTask() {
            float throttle = initThrottle;
            float throttleRate = timeInSeconds * 1000 / period;
            @Override
            public void run() {
                try {
                    if (initThrottle < endThrottle) {
                        if (throttle < endThrottle) {
                            throttle += Math.abs(endThrottle - initThrottle) / throttleRate;
                            vessel.getControl().setThrottle(throttle);
                        }
                        else {
                            latch.countDown();
                        }
                    }
                    else {
                        if (throttle > endThrottle) {
                            throttle -= Math.abs(endThrottle - initThrottle) / throttleRate;
                            vessel.getControl().setThrottle(throttle);
                        }
                        else {
                            latch.countDown();
                        }
                    }
                }
                catch (RPCException e) {
                    e.printStackTrace();
                }
            }
        }, 1000, period);
        latch.await();
        if (vessel.getControl().getThrottle() == endThrottle) {
            System.out.printf("[+] Throttle set -> %f\n", endThrottle);
            return true;
        }
        else {
            System.out.printf("[-] Couldn't set throttle -> %f\n", endThrottle);
            abortLaunch();
            return false;
        }
    }

}