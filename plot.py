# import matplotlib.pyplot as plt
import csv
import numpy as np
# import matplotlib.mlab as mlab
import math


f = open("theta.csv")

x = csv.reader(f, delimiter=",")
reads = []
errors = []
for row in x:
	if (row[0] == "read"): continue
	reads.append(float(row[0]))
	errors.append(float(row[0]) - float(row[1]))


# grafico da normal do erro
acc = 0
desv = 0
for error in errors:
	acc += error

acc = acc / len(errors)

for error in errors:
	desv += (acc - error)**2

desv = math.sqrt(desv / len(errors))

print(acc)
print(desv)

# mu = acc
# sigma = desv
# x = np.linspace(mu - 3*sigma, mu + 3*sigma, 100)
# plt.plot(x,mlab.normpdf(x, mu, sigma))
# plt.show()


# grafico do medido vs erro
# n = len(reads)
# plt.ylabel("Erro")
# plt.xlabel("Medido")
# plt.plot(reads, errors, '.')
# plt.show()