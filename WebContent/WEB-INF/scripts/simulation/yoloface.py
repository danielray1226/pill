import struct
import os
import json
from ultralytics import YOLO
import math
import cv2
import sys
# print(sys.argv)
# print(sys.argv[1])
yoloweights = sys.argv[1]
model = YOLO(yoloweights)
infd = 0
outfd = os.dup(1)
print("new fd = ", outfd)
os.dup2(2, 1)


def readinput ():
    lengthtuple = struct.unpack('>L', os.read(infd, 4))
    length = lengthtuple[0]
    b = os.read(infd, length)
    a = b.decode("utf-8")
    # return json.loads(a)
    return json.loads(a)


def writeoutput(j):
    s = json.dumps(j)
    b = s.encode('utf-8')
    lb = len(b)
    finding = struct.pack('>I', lb)
    os.write(outfd, finding)
    os.write(outfd, b)


while True: 
    command = readinput()
    file = command["file"]
    # print("face detection " + file)
    face = cv2.imread(file)
    results = model(face, stream=True)
    arr = []
    for r in results:
        boxes = r.boxes

        for box in boxes:
            # bounding box
            x1, y1, x2, y2 = box.xyxy[0]
            x1, y1, x2, y2 = int(x1), int(y1), int(x2), int(y2)  # convert to int values
            print(x1, y1, x2, y2)

            # put box in cam
            p = {"x":int(x1), "y":int(y1),"w":int(x2-x1) ,"h":int(y2-y1)}
            arr.append(p)

            # confidence
            confidence = math.ceil((box.conf[0] * 100)) / 100
            #print("Confidence --->", confidence)

            # class name
            cls = int(box.cls[0])
            #print("Class name -->", model.names[cls])
  
    # print(multi)
    writeoutput({"faces":arr})
