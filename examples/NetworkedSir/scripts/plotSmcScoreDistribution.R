if (!"optparse" %in% installed.packages())
    install.packages("optparse")
suppressPackageStartupMessages(library("optparse"))


option_list <- list(
		    make_option(c("-i", "--input"), default="particle_*.dat", help = "Input file containing the parameters to plot [default \"%default\"]"),
		    make_option(c("-o", "--output"), default="scoreDist.pdf", help = "Output file containing [default \"%default\"]"),
		    make_option(c("-s", "--separator"), dest="separator", default=",", help = "the data field separator [default \"%default\"]"),
		    make_option(c("-k", "--skip"), type="integer", default=0, dest="skip", help="Number of rows in the data file to skip [default \"%default\"]", metavar="number"),
		    make_option(c("-v", "--var"), dest="var", type="integer", default=1, help = "The column that holds the variable to plot [default \"%default\"]"),
		    make_option(c("-x", "--xlab"), default="Score", help = "The label for the horizontal axis. [default \"%default\"]"),
		    make_option(c("-y", "--ylab"), default="", help = "The label for the vertical axis. [default \"%default\"]"),
		    make_option(c("-m", "--min"), type="integer", default=0,  dest="minVal", help = "The minimum value of the score in the distribution. [default \"%default\"]"),
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


# This is the data store we will use in the script. It consists of 4 data types
# 1) chains - a list containing all the chains we've read (converted into matrices) chains$chains[[i]] = ith chain

chains <- new.env()
chains$chains <- list()
chains$chains2 <- list()
chains$num <- 0
chains$maxLength <- 0

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

	# filter out the score values that are too small
	chain <- subset(chain, chain[,opt$var] > opt$minVal)

	# From all the columns in the data we only require the opt$var column so we remove all others now
	chain <- chain[opt$var]

	if (nrow(chain) > 1 ) {
	    cat (sprintf("                                                      Reading %s (%d rows)\n", datafile, nrow(chain)))

	    # We now have all the data from this file to save to our list of chains from all the files.
	    chains$chains <- unlist(list(chains$chains, list(data.matrix(chain))), recursive=FALSE) # will append a matrix put we can iterate over each chain
	    chains$chains2 <- unlist(list(chains$chains2, list(data.matrix(tail(chain, n=1)))), recursive=FALSE) # will append a matrix put we can iterate over each chain
	    chains$num <- 1 + chains$num 
	    chains$maxLength <- max(chains$maxLength, nrow(chain))
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


cols <- rep("black", chains$num)
if (opt$useColours) {
  cols <- rainbow(chains$num)
}


############################################################################################################################################
#
#  Plots of the evolution of each score (likelihood) 
#
############################################################################################################################################

cat (sprintf("Printing Score distribution for %s\n", opt$input))
result = tryCatch({

    pdf(file=opt$output, onefile=FALSE, width=7.5, height=7.5)

    # I want only one chain here - unlist all the chains into one data

    min_x <- 1E100
    max_x <- -1E100
    max_y <- 0
    chains2 <- unlist (chains$chains)
    box <-  boxplot.stats(chains2)
    dens <- try(density(chains2, kernel="gaussian", bw="nrd", n=8192), silent=TRUE)
    min_x <- min(min_x, box$stats[1]-0.01*abs(box$stats[1]))
    max_x <- max(max_x, box$stats[5]+0.01*abs(box$stats[5])) 
    max_y <- max(max_y, max(dens$y)*1.05)
    plot(1, type='n', ylab=opt$ylab, xlab=opt$xlab, xlim=c(min_x,max_x), ylim=c(0,max_y))
    dens <- try(density(chains2, kernel="gaussian", bw="nrd", n=8192), silent=TRUE)
    lines(dens)






    pdffile <- paste(opt$output, "_individual_scores.pdf", sep="", collapse = NULL)
    pdf(file=pdffile, onefile=FALSE, width=7.5, height=7.5)

    for (j in 1:chains$num) {
	dens <- try(density(chains$chains[[j]], kernel="gaussian", bw="nrd", n=8192), silent=TRUE)
	max_y <- max(max_y, max(dens$y)*1.05)
    }

    plot(1, type='n', ylab=opt$ylab, xlab=opt$xlab, xlim=c(min_x,max_x), ylim=c(0,max_y))
    for (j in 1:chains$num) {
	dens <- try(density(chains$chains[[j]], kernel="gaussian", bw="nrd", n=8192), silent=TRUE)
	lines(dens, col=cols[j])
    }


}, error = function(err) {
  cat(paste("****Could not plot score distribution:  ",err))
}, warning = function(war) {
  cat(paste("****Could not plot score distribution:  ",war))
})




