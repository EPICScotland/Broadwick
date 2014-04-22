EXAMPLES="AbcExample
MarkovChain
MonteCarlo
StochasticSIR"

for f in $EXAMPLES
do
	cd $f
	echo "Building $f" | tee build.log
	mvn3 clean package | tee -a build.log
	echo "Analysing $f" | tee -a build.log
	mvn3 sonar:sonar | tee -a build.log
	cd -;
done
 
