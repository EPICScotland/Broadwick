#!/bin/sh

echo "plotting deterministic output"
Rscript plotTimeSeries.R  > /dev/null

echo "plotting particle tracks"
Rscript plotSmcParticles.R \
    --input="../results/particle_*.dat" \
    --output="../particles_tracks.pdf" \
    --separator="," \
    --skip=0 \
    --var=6 \
    --xlab="step" \
    --plot=2  > /dev/null


echo "plotting posterior distribution"
Rscript plotSmcPosteriorDistribution.R  \
    --input="../results/particle_*.dat" \
    --output="../particles.pdf" \
    --separator="," \
    --skip=0 \
    --var=6 \
    --columns="3,4,5" \
    --labels="beta,sigma,gamma" \
    --diagonal \
    --nlogscale \
    --plot=2 \
    --limits="0.0001,10.0,0.0001,5.0,0.0001,10.0" \
    --filter="-4.8" \
    --colours

echo "plotting posterior distribution"
Rscript plotSmcPosteriorDistribution.R  \
    --input="../results/particle_*.dat" \
    --output="../particles_all.pdf" \
    --separator="," \
    --skip=0 \
    --var=6 \
    --columns="3,4,5" \
    --labels="beta,sigma,gamma" \
    --diagonal \
    --nlogscale \
    --plot=2 \
    --limits="0.0001,10.0,0.0001,5.0,0.0001,10.0" \
    --colours  > /dev/null

echo "plotting likelihood distribution"
Rscript plotSmcScoreDistribution.R  \
    --input="../results/particle_*.dat" \
    --output="../particles_scoreDist.pdf" \
    --separator="," \
    --skip=0 \
    --var=6 \
    --xlab="Log-Likelihood" \
    --ylab=" " \
    --min=-4.8 \
    --plot=2 \
    --colours > /dev/null

echo "plotting populations"
Rscript plotPosteriorPopulations.R  \
    --input="../results/particle_*.dat" \
    --output="../particles_populations.pdf" \
    --separator="," \
    --skip=0 \
    --var=6 \
    --columns="7,8,9,10" \
    --labels="S,E[1],E[2],I" \
    --plot=2 \
    --filter="-4.8" \
    --colours 


