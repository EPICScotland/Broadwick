#!/usr/bin/env python
# This sample network processing code accompanies the
# manuscript:
# Reconstructing Disease Transmission Dynamics from Animal Movements and Test Data
# by O'Hare and Enright
# and is used in the section: Network Analysis of Spatio-temporal Data 


import sys
import networkx as nx
import numpy as np
import matplotlib.pyplot as plt
from collections import Counter
INFINITY = 999999999


#  This code assumes that each line 
# is comma-separated, and has the form:
# sourceFarm,destinationFarm,dayNumber
# where sourceFarm is the identifier of the source farm
# destinationFarm is the identifier of the destination farm
# dayNumber is a non-negative integer indicating the day
# of the movement 
def readMovements(filename):
    triple = []
    for line in open(filename):
        # print "reading line " + line.strip()
        split = line.strip().split(",")
        if split[0] != split[1]:
           triple.append((split[0], split[1], int(split[2])))
    return triple


#  Takes as parameters
# digraph: a networkx digraph
# filename: the base filename for figure files
# This function finds an saves figures of in- and out- degree
# distributions for digraph 
def  staticDegreeDistributions(digraph, filename):
    distribIn = (digraph.in_degree()).values()
    inCounts = Counter(distribIn)

    distribOut = (digraph.out_degree()).values()
    outCounts = Counter(distribOut)
    
    plt.scatter(inCounts.keys(), inCounts.values(), alpha=0.4)
    plt.title("In-degree distribution")
    plt.xlabel("In-degree")
    plt.ylabel("Frequency (log)")
    plt.yscale('log')
    plt.savefig("InDegree." + filename)
    plt.clf()
    plt.scatter(outCounts.keys(), outCounts.values(), alpha=0.4)
    plt.title("Out-degree distribution")
    plt.xlabel("Out-degree")
    plt.ylabel("Frequency (log)")
    plt.yscale('log')
    plt.savefig("OutDegree." + filename)
    plt.clf()

# Takes as parameters
# digraph: a neteworkx digraph
# locations: the x,y locations of nodes in digraph as a dictionary
#            here assumed to be between 0 and 1 in each dimension 
# filename: the filename for map figure that this function will produce
# This function produces and saves a geographically
# embedded version of digraph 
def plotFlorinDigraph(digraph, locations, filename):
    nx.draw_networkx(digraph, pos=locations, arrows=True, with_labels=False, node_size = 5, linewidths = 0.001, alpha=0.2, node_color="darkblue")
    plt.xlim(-0.5, 1.5)
    plt.savefig(filename)
    plt.clf() 

    
    
#  This code assumes that each line is
# comma-separated, and has the form:
# farmID,x,y
# where farmID is the identifier of the farm
# x is the real-numbered x-coordinate location (in the range from 0 to 1)
# y is the real-numbered y-coordinate location (in the range from 0 to 1)
def readLocations(filename):
    locationDict = {}
    for line in open(filename):
        # print "reading line " + line.strip()
        split = line.strip().split(",")
        locationDict[split[0]] = (float(split[1]), float(split[2]))
    return locationDict

# given the triples for movements (source, destination, time)
# this generates a time-expanded graph 
def generateTEG(triples):
    teg = nx.DiGraph()
    nodesSeen = []
    maxT = 0
    minT = INFINITY
    for (u, v, t) in triples:
        nodesSeen.append(u)
        nodesSeen.append(v)
        teg.add_edge((u, t), (v, t+1))
        if t > maxT:
            maxT = t
        if t < minT:
            minT = t
        
    for i in range(minT-1, maxT+1):
        for guy in nodesSeen:
            teg.add_edge((guy, i), (guy, i+1))
    return teg

# Takes as parameters:
# triples: a list if size 3 tuples (that is, triples)
#          which are each of the form (source, destination, time)
# This function produces a static aggregation of the edges in triples
# as a networkx digraph 
def generateVanilla(triples):
    
    diGraph = nx.DiGraph()
    for (u, v, t) in triples:
        diGraph.add_edge(u, v)
    
    return diGraph
    


# Takes as a parameter:
# teg: a time-expanded graph as a networkx graph
# This function calculates and returns a distribution
# of reachability sets (also called 'reachability cones',
# 'possible infection cones', etc)
#  found when starting an outbreak uniformly at each node at time zero
# These are somewhat analagous to component sizes in an undirected graph. 
def reachabilityCones(teg):
    distributionOfSizes = []
    minTime = 0
    
    strippedGuys = []
    for (guy, time) in teg.nodes():
        if time == minTime:
            strippedGuys.append(guy)
            
    for guy in strippedGuys:
        tree = nx.bfs_tree(teg, (guy, 0))
        infectedInThis = []
        for (fella, time) in tree.nodes():
            if fella not in infectedInThis:
                infectedInThis.append(fella)
                
        distributionOfSizes.append(len(infectedInThis))
    
    return distributionOfSizes


# Takes as a parameter:
# vanillaDiGraph:  a static aggregated directed graph
# This function calculates and returns a distribution
# of reachability sets (also called 'reachability cones',
# 'possible infection cones', etc)
#  found when starting an outbreak uniformly at each node at time zero
# These are somewhat analagous to component sizes in an undirected graph.    
def noTimingComponentSizes(vanillaDiGraph):
    distributionOfSizes = []

    for guy in vanillaDiGraph.nodes():
        tree = nx.bfs_tree(vanillaDiGraph, guy)
        distributionOfSizes.append(len(tree.nodes()))
        
        if len(tree.nodes())>10:
            print guy
    
    return distributionOfSizes

# Takes as a parameter:
# triples: a list if size 3 tuples (that is, triples)
#          which are each of the form (source, destination, time)
# This function sorts edges by time, and returns a dictionary
# in which the keys are times, and the values are lists of edges
# at each time 
def dayByDayEdges(triples):
    edgesDict = {}
    for (u, v, t) in triples:
        if t not in edgesDict:
            edgesDict[t] = []
        edgesDict[t].append((u, v))
    return edgesDict
    
    
# Takes as parameters:
# graph: a networkx graph
# locations: the x/y locations of nodes in graph
# filename: the filename where we will store the figure we produce 
def networkPlotting(graph, locations, filename): 
    nx.draw_networkx(graph, pos=locations, with_labels=False, node_size = 15, linewidths = 0.001, alpha=0.9, node_color="darkblue")
    plt.xlim(-0.5, 1.5)
    plt.savefig(filename)
    plt.clf()
    
    
    
# Takes as parameters:
# edgesByTime: a dictionary with times as keys,
#       and lists of edges at each time as values
# filename: the base filename for the mean and max figures we will produce
# This function calculates mean and max out-degrees over time
# and plots figures of these 
def outOverTime(edgesByTime, filename):
    minTime = min(edgesByTime.keys())
    maxTime =  max(edgesByTime.keys())
    spanToMax = {}
    spanToMean = {}
    
    for i in range(1, maxTime-minTime):
        thisSpan = i
        means = []
        maxs = []
        for h in range(minTime, maxTime-thisSpan):
            edges = []
            for j in range(h, h + thisSpan):
                if j in edgesByTime:
                    edges.extend(edgesByTime[j])
            thisGraph = nx.DiGraph()
            thisGraph.add_edges_from(edges)
            
            means.append(np.mean(thisGraph.out_degree().values()))
            maxs.append(max(thisGraph.out_degree().values()))
        spanToMax[thisSpan] = max(maxs)
        spanToMean[thisSpan] = np.mean(means)
    plt.plot(spanToMax.keys(), spanToMax.values())
    plt.xlabel('Time window size in days')
    plt.ylabel('Maximum out-degree')
    plt.savefig("max" + filename)
    plt.clf()
    plt.plot(spanToMean.keys(), spanToMean.values())
    plt.xlabel('Time window size in days')
    plt.ylabel('Mean out-degree')
    plt.savefig("mean" + filename)
    plt.clf()


# now we start the main set of calls 

# read our triples, and generate our two graphs 
triples = readMovements(sys.argv[1])

# vanilla will be the time-aggregated graph, and teg a time-expanded graph 
vanilla = generateVanilla(triples)
teg = generateTEG(triples)


# calculate and report reachability sizes for aggregated and time-expanded graphs 
vanillaDistrib = noTimingComponentSizes(vanilla)
tegDistrib = reachabilityCones(teg)

print "Static mean " + str(np.mean(vanillaDistrib))
print "Dynamic mean " + str(np.mean(tegDistrib))
print "Static max " + str(max(vanillaDistrib))
print "Dynamic max " + str(max(tegDistrib))


# now we plot the farms reachable from same pfar 5 
exampleGuy = "5"

emptyGraphLocations = nx.Graph()
treeVanilla = nx.dfs_postorder_nodes(vanilla,exampleGuy)
vanillaDistances = []
timedDistances = []



dayEdges = dayByDayEdges(triples)
vanillaOneDay = nx.DiGraph()
vanillaOneDay.add_edges_from(dayEdges[1])

    
treeTEG = nx.dfs_postorder_nodes(teg,(exampleGuy, 0))
distancesTimed = {}
for (fella, time) in treeTEG:
    if fella not in distancesTimed:
        path = nx.shortest_path(teg, source=(exampleGuy, 0), target=(fella, time))
        switches = 0
        for i in range(1, len(path)):
            (fella1, time1) = path[i-1]
            (fella2, time2) = path[i]
            if fella1 != fella2:
                switches = switches+1
        distancesTimed[fella] = switches
        
for fella in treeVanilla:
    emptyGraphLocations.add_node(fella)
    vanillaDistances.append(nx.shortest_path_length(vanilla, source=exampleGuy, target=fella))


locations = readLocations(sys.argv[2])

nx.draw_networkx(emptyGraphLocations, pos=locations, with_labels=False, node_size = 15, linewidths = 0.001, linecolor="white", alpha=0.9,  cmap=plt.get_cmap('Blues_r'), node_color=vanillaDistances)
(x, y) = locations[exampleGuy]
plt.scatter([x], [y], marker="s", color="red", s=[200])


plt.xlim(-0.5, 1.5)
plt.savefig("untimed.jpg")
emptyGraphLocations = nx.Graph()
plt.clf()

distancesForTimedVersion = []
for guy in distancesTimed:
    emptyGraphLocations.add_node(guy)
    distancesForTimedVersion.append(distancesTimed[guy])

nx.draw_networkx(emptyGraphLocations, pos=locations, with_labels=False, node_size = 15, linewidths = 0.001, alpha=0.9,  cmap=plt.get_cmap('Blues_r'), node_color=distancesForTimedVersion)
plt.xlim(-0.5, 1.5)
plt.scatter([x], [y], marker="s", color="red", s=[200])


plt.savefig("withTime.jpg")



# now we produce a node-only version of our graph and save it to place on a map 
emptyGraphLocations = nx.Graph()
plt.clf()

for guy in locations:
    emptyGraphLocations.add_node(guy)


networkPlotting(emptyGraphLocations, locations, "allFarmsFlorin.pdf")

# plotting directed versions of the whole dataset, and one day's
# worth of edges 
plotFlorinDigraph(vanilla, locations, "florinDigraph.pdf")
plotFlorinDigraph(vanillaOneDay, locations, "florinDigraphOneDay.pdf")


# calculate and plot frequencies of  out-degrees
staticDegreeDistributions(vanilla, "Florin.pdf")
outOverTime(dayEdges, "florinDegreeByDay.pdf")