import krpc
from time import sleep
import simpleaudio as sa
import math

#yPos = 300


def play_sound(wav_path):
    wave_obj = sa.WaveObject.from_wave_file(wav_path)
    wave_obj.play()

'''
def add_text(str, panel):
    global yPos
    yPos -= 30
    text = panel.add_text(str)
    text.rect_transform.position = (-100, yPos)
    text.size = 14
'''

def launch_vessel(vessel):
    # play countdown .wav
    play_sound("E:/Google Drive/SpaceX-Project/10-0_countdown.wav")

    # countdown from 10 to 0
    for i in range(10, -1, -1):
        if (i == 10):
            print("[*] Checking systems\n\n")
        elif (i == 9):
            # check if rocket is in pre_launch state
            if (str(vessel.situation)[16:] == "pre_launch"):
                print("[ok] Vesel situation: pre_launch\n")
            else:
                print("[bad] Vesel is not in pre_launch situation, exiting!!!\n")
                return False
        elif (i == 8):
            # set throttle to 0
            print("[ok] Throttle set to 0\n")
            vessel.control.throttle = 0.0
        elif (i == 7):
            # set pitch and heading to 90 grades
            print("[ok] Set autopilot pitch and heading to 90 grades")
            vessel.auto_pilot.target_pitch_and_heading(90, 90)
            print("\n---------------------------\n")
        elif (i == 6):
            # start engine
            print("[!] Main engine start")
            vessel.control.activate_next_stage()
            print("\n---------------------------\n")
        elif (i == 0):
            # release clamps
            vessel.control.activate_next_stage()
            print("---------------------------\n")
            print("[!] Solid rocket booster ignition and liftoff!")
            print("\n---------------------------\n")
        if (i >= 7 or i <= 2):
            sleep(1)
        elif (i <= 6 and i >= 3):
            # smoothly increase throttle
            for j in range(1, 11):
                vessel.control.throttle += 0.025
                sleep(0.04)
            print("[+] Throttle = " + str(round(vessel.control.throttle, 1)) + "\n")
    # succes, returning True
    return True


def get_high(conn, vessel):
    vessel.auto_pilot.engage()

    refframe = vessel.orbit.body.reference_frame
    cAltitude = conn.add_stream(getattr, vessel.flight(), 'surface_altitude')

    while (int(cAltitude()) < 500):
        if (int(cAltitude()) == 300):
            print("[!] Changing pitch and heading!!!\n")
            vessel.control.rcs = True
            vessel.auto_pilot.target_pitch_and_heading(80, 360)

        elif (int(cAltitude()) == 400):
            print("[!] Returning pitch and heading to 90 grades!!!\n")
            vessel.auto_pilot.target_pitch_and_heading(90, 90)
            # vessel.auto_pilot.
            for j in range(1, 11):
                vessel.control.throttle = 0.1
                sleep(0.04)

    return True


def land(conn, vessel):
    vessel.auto_pilot.engage()
    s_burn = False

    print(vessel.control.nodes)

    refframe = vessel.orbit.body.reference_frame
    vAltitude = conn.add_stream(getattr, vessel.flight(), "surface_altitude")
    vSpeed = conn.add_stream(getattr, vessel.flight(refframe), "vertical_speed")
    hSpeed = conn.add_stream(getattr, vessel.flight(refframe), "horizontal_speed")

    vessel.auto_pilot.target_pitch = 90.0;
    vessel.auto_pilot.target_heading = 0.0;

    while (True):
        print (vessel.auto_pilot.deceleration_time);
        print (vessel.auto_pilot.stopping_time);
        '''
        if (vAltitude() < suicide_dist(vessel, vSpeed, vAltitude) and not s_burn):
            print("\n---------------------------\n")
            print("<!!!> Performing Suicide Burn <!!!>")
            print("\n---------------------------\n")
            vessel.control.throttle = 10-0_countdown.wav
            s_burn = True
        if (str(vessel.situation)[16:] == "landed" and vSpeed() < 4 and hSpeed() < 4):
            print("\n---------------------------\n")
            print("[!] Landed succesfuly, killing thrusters [!]")
            print("\n---------------------------\n")
            vessel.control.throttle = 0
            break
        '''
    return True


def suicide_dist(vessel, vSpeed, vAltitude):
    mThrust = vessel.max_thrust
    bGravity = vessel.orbit.body.surface_gravity
    vMass = vessel.mass

    eIsp = 0
    for engine in vessel.parts.engines:
        eIsp += engine.max_thrust / mThrust * engine.specific_impulse
    Ve = eIsp * bGravity

    deltaV = vessel.control.nodes[0].delta_v

    final_mass = pow(vMass * math.e, -1 * deltaV / Ve)

    mAccel = mThrust / vMass

    sAlt = 0

    dist = vAltitude() - sAlt

    print("suicide burn alt:" + str(sAlt))

    return sAlt


def main():
    # connect to KSP rocket
    print("\n[*] Connecting to KSP rocket...\n")
    #try:
    conn = krpc.connect(name='Rocket-core', address='192.168.0.106', rpc_port=1000)
    vessel = conn.space_center.active_vessel
    canvas = conn.ui.stock_canvas
    #except Exception:
        #print("Error connecting to vessel, exiting!!!")
        #return False
    print("[!] Connected to : " + vessel.name)
    print("\n---------------------------\n")

    # set camera
    camera = conn.space_center.camera
    camera.heading = 270
    camera.pitch = 0
    camera.distance = 120

    '''
	screen_size = canvas.rect_transform.size
	panel = canvas.add_panel()
	rect = panel.rect_transform
	rect.size = (400, 600)
	rect.position = (200 - (screen_size[0] / 2), (screen_size[1] / 2) - 300 - 100)
	'''

    # launch SpaceX-Mission
    if (launch_vessel(vessel)):
        if (get_high(conn, vessel)):
            if (land(conn, vessel)):
                pass


if __name__ == '__main__':
    main()
