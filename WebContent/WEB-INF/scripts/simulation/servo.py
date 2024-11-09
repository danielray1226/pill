import struct
import os
import json
import time    #https://docs.python.org/fr/3/library/time.html
import subprocess
import sys
print(sys.argv)
print(sys.argv[1])
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

while True: 
    command = readinput()
    angle = command["angle"]
    servo = command["servonum"]
    speech = "Rotating servo " + str(servo) + " to the angle of " + str(angle)
    subprocess.run(["espeak", speech]) 
    writeoutput(command)
