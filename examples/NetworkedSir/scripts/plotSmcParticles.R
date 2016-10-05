if (!"optparse" %in% installed.packages())
    install.packages("optparse")
suppressPackageStartupMessages(library("optparse"))


option_list <- list(
		    make_option(c("-i", "--input"), default="particle_*.dat", help = "Input file containing the parameters to plot [default \"%default\"]"),
		    make_option(c("-o", "--output"), default="particles.pdf", help = "Output file containing [default \"%default\"]"),
		    make_option(c("-s", "--separator"), dest="separator", default=",", help = "the data field separator [default \"%default\"]"),
		    make_option(c("-k", "--skip"), type="integer", default=0, dest="skip", help="Number of rows in the data file to skip [default \"%default\"]", metavar="number"),
		    make_option(c("-v", "--var"), dest="var", type="integer", default=1, help = "The column that holds the variable to plot [default \"%default\"]"),
		    make_option(c("-x", "--xlab"), default="Step", help = "The label for the horizontal axis. [default \"%default\"]"),
		    make_option(c("-y", "--ylab"), default="", help = "The label for the vertical axis. [default \"%default\"]"),
		    make_option(c("-l", "--logscale"), action="store_true", default=FALSE, dest="logscale", help = "use log sale for the vertical axes.[default \"%default\"]"),
		    make_option(c("-p", "--plot"), dest="plot", type="integer", default=-1, help = "The column that holds the flag on whether to include this line in the plot (1=include, 0=ignore) [default \"%default\"]"),
		    make_option(c("-c", "--colours"), action="store_true", default=FALSE, dest="useColours", help = "Use colours for the different particles [default \"%default\"]")
		    )


opt <- parse_args(OptionParser(option_list=option_list))

outputFileNameBase <- sub('(\\.)[^\\.]*$', '', opt$output)
outputExtension <- ".pdf"


# create a list of files to read from the value set in the command line. We use glob2rx to convert any globbing patterns to regular expressions
# but if the input already contains a regular expression we will not glob it - the globbing function just does a ^... [] ...$ so we check for a
# ^^ at the beginning of the generated expression.
path <- dirname(opt$input)
filename <- basename(opt$input)
patt <- glob2rx(filename)
if (substr(patt,1,2) == "^^") {
    patt = opt$input
}
files <- list.files(path = path, pattern = patt, full.names=TRUE)
if (length(files) == 0) {
    stop(paste("Cannot find any files matching pattern ", opt$input))
}


# This is the dtat store we will use in the script. It consists of 4 data types
# 1) chains - a list containing all the chains we've read (converted into matrices) chains$chains[[i]] = ith chain
# 2) matrix - a matrix containing all the chains merged together. chains$matrix[,i] = all the values for the ith parameter we've read.
# 3) mcmc - a vector of the MCMC objects (contains the MCMC scores)
# 4) num - the number of chains storedin the environment

chains <- new.env()
chains$chains <- list()
chains$num <- 0
chains$maxLength <- 0
chains$maxY <- -1e-7
chains$minY <- -1e-7

# look at each chain in parallel, exoptracting accepted steps and removing burn-in steps as required.
process_chains <- function(datafile) {

    chain <- try(read.csv(datafile, sep=opt$separator,  comment.char = "#", header=FALSE, stringsAsFactors = FALSE), silent=TRUE)
    if (class(chain) != "try-error") {
	if (opt$plot != -1) {
	    # filter out the rejected samples from the data frame.
	    chain <- subset(chain, chain[,opt$plot] == 1)
	}

	# filter out the values of -Inf or -Infinity
	chain <- subset(chain, chain[,opt$var] != -Inf)

	# From all the columns in the data we only require the opt$var column so we remove all others now
	chain <- chain[opt$var]

	if (nrow(chain) > 1 ) {
	    cat (sprintf("                                                      Reading %s (%d rows)\n", datafile, nrow(chain)))

	    # We now have all the data from this file to save to our list of chains from all the files.
	    chains$chains <- unlist(list(chains$chains, list(data.matrix(chain))), recursive=FALSE) # will append a matrix put we can iterate over each chain
	    chains$num <- 1 + chains$num 
	    chains$maxLength <- max(chains$maxLength, nrow(chain))
	    chains$maxY <- max(chains$maxY, max(unlist(chain)))
	    chains$minY <- min(chains$minY, min(unlist(chain)))
	} else {
	    cat (sprintf("                                                     ignoring %s (%d rows)\n", datafile, nrow(chain)))
	}
    }

}
chains$maxLength <- 1 + chains$maxLength




# process the chains by reading the files, removing any burn-in and 
# then creating a single combined chain, list of chains etc for further plotting.
invisible(mapply(process_chains, files))
#print(chains$chains)
#print(chains$num)
#print(chains$maxLength)
#print(chains$maxY)
#print(chains$minY)

cols <- rep("black", chains$num)
if (opt$useColours) {
  cols <- rainbow(chains$num)
}


############################################################################################################################################
#
#  Plots of the evolution of each score (likelihood) 
#
############################################################################################################################################

cat (sprintf("Printing MCMC chain evolution for %s\n", opt$input))
result = tryCatch({
  
  pdf(file=opt$output, onefile=FALSE, width=7.5, height=7.5)
  
  
  if (opt$logscale) {
      plot(1, type='n', log="y", ylab=opt$ylab, xlab=opt$xlab, xlim=c(0,chains$maxLength), ylim=c(chains$minY,chains$maxY))
  } else {
      plot(1, type='n', ylab=opt$ylab, xlab=opt$xlab, xlim=c(0,chains$maxLength), ylim=c(chains$minY,chains$maxY))
  }
  
  for (j in 1:chains$num) {
    lines(chains$chains[[j]], col=cols[j])
  }

}, error = function(err) {
  cat(paste("****Could not plot score evolution:  ",err))
}, warning = function(war) {
  cat(paste("****Could not plot score evolution:  ",war))
})



