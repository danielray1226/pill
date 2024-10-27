import struct
import os
import json
import cv2
import sys
dir = sys.argv[1]
cap = cv2.VideoCapture(0)
cap.set(3, 640)
cap.set(4, 480)
infd = 0
outfd = os.dup(1)
count = 0
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
    # print("taking picture " + file)
    success, img = cap.read()
    if (success):
        file = dir + "/" + str(count) + ".jpg";
        command["file"] = file
        if command.get("faces") != None and len(command.get("faces"))>0:
            aiImage = img.copy()
            aiFile = dir + "/" + str(count) + "-ai.jpg"
            command["aiFile"] = aiFile
            faces = command["faces"]
            for face in faces:
                # print(face)
                x = face["x"]
                y = face["y"]
                w = face["w"]
                h = face["h"]
                # print(x, y, w, h)
                cv2.rectangle(aiImage, (x, y), (x + w, y + h), (0, 255, 0), 2)
                # cv2.imshow('Video', frame)
                cv2.imwrite(aiFile, aiImage)
    
        cv2.imwrite(file, img)
    count += 1
    
    writeoutput(command)
