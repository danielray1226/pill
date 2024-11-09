import struct
import os
import json
import time    #https://docs.python.org/fr/3/library/time.html
import RPi.GPIO as GPIO
import time
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
    
GPIO.setmode(GPIO.BCM)
setupPins={}
sleepSec=0.0005


try:
    while True:
        command = readinput() 
        pinsArray=command["pins"]
        untriggeredPins={}
        for pin in pinsArray:
            untriggeredPins[pin]='still waiting'
            if pin not in setupPins:
                print("setting up pin ",pin)
                GPIO.setup(pin, GPIO.IN, pull_up_down=GPIO.PUD_UP)
                setupPins[pin]="yes"
                print("done setting up pin ",pin)
        
        waitSec=command["waitMs"] / 1000.0
        print("waitSec: ", waitSec)
        print("untriggeredPins: ", untriggeredPins)
        
        while waitSec > 0:
            #print("wait ",waitSec)
            for pin in list(untriggeredPins.keys()):
                state=GPIO.input(pin)
                if state==0:
                    del untriggeredPins[pin]
                    
            if len(untriggeredPins)==0:
                print("all triggered: ")
                break
                                
            time.sleep(sleepSec)
            waitSec-=sleepSec

        response={}
        print("reset response: ", response)
        for pin in pinsArray:
            if pin in untriggeredPins:
                response[pin]="inactive"
            else:
                response[pin]="active"
        print("response: ", response)
        writeoutput(response)
        
    
except Exception:
    print(traceback.format_exc())
    GPIO.cleanup() 