import struct
import os
import json
import time    #https://docs.python.org/fr/3/library/time.html
from adafruit_servokit import ServoKit
infd=0
outfd= os.dup(1)
print("new fd = ", outfd)
os.dup2(2,1)
def readinput ():
    lengthtuple = struct.unpack('>L', os.read(infd, 4))
    length = lengthtuple[0]
    b= os.read(infd, length)
    a= b.decode("utf-8")
    #return json.loads(a)
    return json.loads(a)
def writeoutput(j):
    s = json.dumps(j)
    b= s.encode('utf-8')
    lb= len(b)
    finding= struct.pack('>I', lb)
    os.write(outfd, finding)
    os.write(outfd, b)
MIN_IMPULSE = 450
MAX_IMPULSE = 2650
MIN_ANGLE=0
MAX_ANGLE=270
board = ServoKit(channels=16,frequency=60)  
for i in range(16):
    board.servo[i].actuation_range=270
    board.servo[i].set_pulse_width_range(MIN_IMPULSE, MAX_IMPULSE)
while True: 
    command = readinput()
    angle = command["angle"]
    servo = command["servonum"]
    board.servo[servo].angle=angle
    writeoutput(command)
