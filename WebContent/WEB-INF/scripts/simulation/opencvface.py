import struct
import os
import json
import cv2
import sys
#print(sys.argv)
#print(sys.argv[1])
cascPath = sys.argv[1]
faceCascade = cv2.CascadeClassifier(cascPath)
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
    file = command["file"]
    #print("face detection " + file)
    face = cv2.imread(file, cv2.COLOR_BGR2GRAY)
    
    multi = faceCascade.detectMultiScale(
        face,
        scaleFactor=1.1,
        minNeighbors=5,
        minSize=(30, 30),
        flags = cv2.CASCADE_SCALE_IMAGE
    )
    arr = []
    #print(multi)
    for (x, y, w, h) in multi:
    	p = {"x":int(x), "y":int(y),"w":int(w) ,"h":int(h)}
    	arr.append(p)
    writeoutput({"faces":arr})
