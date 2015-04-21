EXAMPLES="AbcExample
MarkovChain
MonteCarlo
StochasticSIR
DeterministicSir
DelaySir"

for f in $EXAMPLES
do
	cd $f
	echo "Building $f" | tee build.log
	mvn3 clean package | tee -a build.log
	echo "Analysing $f" | tee -a build.log
	mvn3 sonar:sonar | tee -a build.log
	echo "Running $f" | tee -a build.log
	./Broadwick.sh -c Broadwick.xml | tee -a build.log
	cd -;
done
 
