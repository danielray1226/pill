import struct
import os
import json
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
    print(command)
    mykey = command["key1"]
    command["key6"] = "hello from python"
    print(mykey)
    writeoutput(command)
