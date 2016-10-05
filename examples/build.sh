EXAMPLES="AbcExample
DeterministicSir
MarkovChain
MaxLikelihoodSeirModel
MonteCarlo
GillespieSIR
NetworkedSir
"

for f in $EXAMPLES
do
	cd $f
	echo "Building $f" | tee build.log
	mvn clean package | tee -a build.log
	echo "Analysing $f" | tee -a build.log
	mvn sonar:sonar | tee -a build.log
	echo "Running $f" | tee -a build.log
	./Broadwick.sh -c Broadwick.xml | tee -a build.log
	cd -;
done
 
