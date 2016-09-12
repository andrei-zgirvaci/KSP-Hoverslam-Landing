

	def land(conn, vessel):
	vessel.auto_pilot.engage()
	s_burn = False
	
	refframe = vessel.orbit.body.reference_frame
	vSpeed = conn.add_stream(getattr, vessel.flight(refframe), 'vertical_speed')
	#hSpeed = conn.add_stream(getattr, vessel.flight(refframe), 'horizontal_speed')
	
	while (True):
		if (suicide_dist(vessel, vSpeed) > 0 and not s_burn):
			#vessel.control.activate_next_stage()
			print ("\n---------------------------\n")
			print ("<!!!> Performing Suicide Burn <!!!>")
			print ("\n---------------------------\n")
			vessel.control.throttle = 1
			s_burn = True
		if (str(vessel.situation)[16:] == "landed" and vSpeed() < 4 and hSpeed() < 4):
			print ("\n---------------------------\n")
			print ("[!] Landed succesfuly, killing thrusters [!]")
			print ("\n---------------------------\n")
			vessel.control.throttle = 0
			break
	return True

def suicide_dist(vessel, vSpeed):
	vAltitude = conn.add_stream(getattr, vessel.flight(), 'surface_altitude')
	aEngine = vessel.parts.engines[0].max_thrust
	bRadius = vessel.orbit.body.equatorial_radius
	bGravity = vessel.orbit.body.surface_gravity
	vMass = vessel.mass

	#SQRT(B6^2+((2*PlanetValues!A2)*((1/(B10+PlanetValues!A4))-(1/(B8+PlanetValues!A4)))))

	#print ("Dist = " + str(dist) + "\n")

	#return dist