import struct
import os
import json
import time    #https://docs.python.org/fr/3/library/time.html
import traceback


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
    


try:
    while True:
        command = readinput() 
        pinsArray=command["pins"]
        untriggeredPins={}
        for pin in pinsArray:
            untriggeredPins[pin]='still waiting'
            if pin not in setupPins:
                print("setting up pin ",pin)
                setupPins[pin]="yes"
                print("done setting up pin ",pin)
        

        response={}
        print("reset response: ", response)
        for pin in pinsArray:
                response[pin]="active"
        print("response: ", response)
        writeoutput(response)
        
    
except Exception:
    print(traceback.format_exc())
    GPIO.cleanup() 