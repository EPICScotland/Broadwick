#!/bin/sh

echo "plotting particle tracks"
Rscript plotSmcParticles.R \
    --input="../results/particle_*.dat" \
    --output="../particles_tracks.pdf" \
    --separator="," \
    --skip=0 \
    --var=5 \
    --xlab="step" \
    --plot=2  > /dev/null


echo "plotting posterior distribution"
Rscript plotSmcPosteriorDistribution.R  \
    --input="../results/particle_*.dat" \
    --output="../particles.pdf" \
    --separator="," \
    --skip=0 \
    --var=5 \
    --columns="3,4" \
    --labels="beta,sigma" \
    --diagonal \
    --nlogscale \
    --plot=2 \
    --limits="0.001,0.1,0.0001,0.01" \
    --filter="-3.0" \
    --colours

echo "plotting likelihood distribution"
Rscript plotSmcScoreDistribution.R  \
    --input="../results/particle_*.dat" \
    --output="../particles_scoreDist.pdf" \
    --separator="," \
    --skip=0 \
    --var=5 \
    --xlab="Log-Likelihood" \
    --ylab=" " \
    --min=-3.0 \
    --plot=2 \
    --colours > /dev/null

echo "plotting populations"
Rscript plotPosteriorPopulations.R  \
    --input="../results/particle_*.dat" \
    --output="../particles_populations.pdf" \
    --separator="," \
    --skip=0 \
    --var=5 \
    --columns="6,8" \
    --labels="I,R" \
    --plot=2 \
    --filter="-3.0" \
    --colours 


echo "plotting time series data"
Rscript plotTimeSeries.R \
    --input="../results/TimeSeriesInfectedLocations*.csv" \
    --output="../TimeSeriesInfectedLocations.pdf" \
    --ylabel="#Infected Locations" | grep -v "Read "  > /dev/null


Rscript plotTimeSeries.R \
    --input="../results/TimeSeriesInfectedIndividuals*.csv" \
    --output="../TimeSeriesInfectedIndividuals.pdf" \
    --ylabel="#Infected Individuals" | grep -v "Read "  > /dev/null


Rscript plotTimeSeries.R \
    --input="../results/TimeSeriesRemovedIndividuals*.csv" \
    --output="../TimeSeriesRemovedIndividuals.pdf" \
    --ylabel="#Removed Individuals" | grep -v "Read "  > /dev/null


