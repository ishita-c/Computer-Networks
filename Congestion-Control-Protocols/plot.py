
import math
import numpy as np
import matplotlib.pyplot as plt 
filename=input("File Name: ")
proName=input("Protocol Name: ")
x=[]
y=[]


f = open(filename,'r')
for row in f:
    row = row.split('\t')
    x.append(row[0])
    y.append(int(row[2]))

fig,axes=plt.subplots(1,1)

axes.plot(x, y, marker="+")
for i, tick in enumerate(axes.xaxis.get_ticklabels()):
    if i % 250 != 0:
        tick.set_visible(False)
        
axes.set_title(proName, fontsize="12")
axes.set_xlabel("Time (in secs)", fontsize="12")
axes.set_ylabel("Congestion Window Size", fontsize="12")
plt.show()
