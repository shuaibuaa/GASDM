import os
import sys
import time
import datetime
import math
from datetime import datetime, timedelta

def win(startdate, ft):
    cur = datetime.strptime(startdate, '%Y%m%d')
    windate = []
    if ft == 0:
        start = cur - timedelta(days=7)
        end = cur + timedelta(days=7)
    elif ft == -1:
        start = cur - timedelta(days=7)
        end = cur
    elif ft == 1:
        start = cur
        end = cur + timedelta(days=7)

    #while start <= cur + timedelta(days=7):
    while start <= end:
        temp = datetime.strftime(start, '%Y%m%d')
        windate.append(temp)
        start += timedelta(days=1)
    return windate


if len(sys.argv) != 3:
    print 'python tprfpr.py [graph] [haze_real.txt]'
    sys.exit()
testfn = sys.argv[1]
truefn = sys.argv[2]

P = 0
N = 0
TP = 0
FP = 0

data = open(testfn).read().split('\n')
#testdata = [[item.split(' ')[0], item.split(' ')[1]] for item in data if len(item) > 0 and item.split(' ')[0] > '20141201']
testdata = [[item.split(' ')[0], item.split(' ')[1]] for item in data if len(item) > 0]
data = open(truefn).read().split('\r\n')
truedata = {}
for item in data:
    if len(item) == 0:
        continue
    P += 1
    items = item.split(' ')
    if items[1] not in truedata:
        truedata[items[1]] = {}
    truedata[items[1]][items[0]] = 0

#N = len(truedata)*35 - P

daynum = {u[0]:1 for u in testdata}
print daynum.keys()
print len(daynum.keys())

N = len(daynum.keys())*25 - P
K = 263

print testdata[:3]
print 'P:', P, 'N:', N
f = open('tprfpr_'+testfn, 'w')
print testfn

delkw = ['\xe9\xa6\x99\xe6\xb8\xaf', '\xe5\x8f\xb0\xe6\xb9\xbe', '\xe6\xbe\xb3\xe9\x97\xa8', '\xe8\xa5\xbf\xe8\x97\x8f', '\xe6\xb5\xb7\xe5\xa4\x96', \
    '\xe7\xa6\x8f\xe5\xbb\xba', '\xe6\xb5\xb7\xe5\x8d\x97', '\xe9\x9d\x92\xe6\xb5\xb7', '\xe8\xb4\xb5\xe5\xb7\x9e', '\xe4\xba\x91\xe5\x8d\x97']

label = []
lead = 0
leadcnt = 0
lag = 0
lagcnt = 0
metric = []
for item in testdata:
    windate = win(item[0], 0)
    province = item[1]
    if province in delkw:
        continue
    
    print item
    print province
    #print item[0], province
    isright = 0
    current = datetime.strptime(item[0], '%Y%m%d')
    lead = 0
    lag = 0
    for day in windate:
        if day in truedata:
            if province in truedata[day]:
                isright = 1
                print 'test', province, day
                if truedata[day][province] == 0:
                    TP += 1
                    truedata[day][province] = 1
                    #break
                span = (current - datetime.strptime(day, '%Y%m%d')).days
                if span < 0:
                    lag = 0
                elif span > 0:
                    lead = span
    if isright != 1:
        lag = 7
    FP += 1 - isright
    label.append(isright)
    print 'FPR:', FP*1.0/N, 'TPR:', TP*1.0/P
    #f.write('FPR:'+str(FP*1.0/N) + ' TPR:' + str(TP*1.0/P) + '\n')
    metric.append([lead, lag, FP, TP])
    #print 'FPR:', FP*1.0/K, 'TPR:', TP*1.0/P
    #f.write('FPR:'+str(FP*1.0/K) + ' TPR:' + str(TP*1.0/P) + '\n')
    #if FP >= K:
    #    break

lagcnt = 0
leadcnt = 0
itr = 0
lltime = []
isfirst = [False, False, False, False, False]
for item in metric:
    lagcnt += item[1]
    leadcnt += item[0]
    itr += 1
    f.write('FPR ' + str(item[2]*1.0/N) + ' TPR ' + str(item[3]*1.0/P) + \
        ' lead ' + str(leadcnt*1.0/itr) + ' lag ' + str(lagcnt*1.0/itr) + '\n')
    print str(item[2]*1.0/N), str(item[3]*1.0/P)
    if item[2]*1.0/N - 0.15 > 0 and not isfirst[0]:
        lltime.append([item[2]*1.0/N, str(item[3]*1.0/P)])
        lltime.append([str(leadcnt*1.0/itr), str(lagcnt*1.0/itr)])
        isfirst[0] = True
    if item[2]*1.0/N - 0.2 > 0 and not isfirst[1]:
        lltime.append([item[2]*1.0/N, str(item[3]*1.0/P)])
        lltime.append([str(leadcnt*1.0/itr), str(lagcnt*1.0/itr)])
        isfirst[1] = True
    if item[2]*1.0/N - 0.05 > 0 and not isfirst[2]:
        #lltime.append([str(leadcnt*1.0/itr), str(lagcnt*1.0/itr)])
        isfirst[2] = True
    if item[2]*1.0/N - 0.1 > 0 and not isfirst[3]:
        #lltime.append([str(leadcnt*1.0/itr), str(lagcnt*1.0/itr)])
        isfirst[3] = True
    

#print label
f.flush()

print lltime

sys.exit()


TRUEP = P
P = 0
for i in label:
    P += i
N = len(label) - P

print P, N

TP = 0
for i in range(len(label)):
    TP += label[i]
    FP = i - TP + 1
    print 'FPR:', FP*1.0/N, 'TPR:', TP*1.0/TRUEP
    f.write('FPR:'+str(FP*1.0/N) + ' TPR:' + str(TP*1.0/TRUEP) + '\n')

print TP, FP
print len(truedata)
print TRUEP

f.close()