import struct
fp = open("./test.bin", mode='rb')
Data1 = struct.unpack('>L', fp.read(4)) # unsigned long, little-endian
print(Data1)
l = Data1[0]
print(l)
b= fp.read(l)
print(b)
a= b.decode("utf-8")
print(a)
