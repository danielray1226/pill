import json


s = '{"faces":[{"x":195,"y":166,"w":230,"h":230}]}'
j = json.loads(s)
if j.get("faces") == None:
    print('Key not found')
else:
    print('Key found') 
    print(j)
    faces= j["faces"]
    for face in faces:
        print(face)
        x = face["x"]
        y = face["y"]
        w = face["w"]
        h = face["h"]
        print(x, y, w, h)
		#cv2.rectangle(frame, (x, y), (x + w, y + h), (0, 255, 0), 2)
		#cv2.imshow('Video', frame)
		#cv2.imwrite('video.png', frame)