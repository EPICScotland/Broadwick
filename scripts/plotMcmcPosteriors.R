suppressPackageStartupMessages(library("optparse"))
suppressPackageStartupMessages(library("akima"))
suppressPackageStartupMessages(library("fields"))
suppressPackageStartupMessages(library(coda))
suppressPackageStartupMessages(library(foreach))
suppressPackageStartupMessages(library(doMC))
suppressPackageStartupMessages(library(MASS))
suppressPackageStartupMessages(library(PerformanceAnalytics))

registerDoMC()

option_list <- list(
		    #make_option(c("-h", "--help"), action="store_true", default=FALSE, help="Show this help message and exit")
		    make_option(c("-i", "--input"), default="herd.dat", help = "Input file containing the parameters to plot [default \"%default\"]"),
		    make_option(c("-o", "--output"), default="herd.png", help = "Output file containing [default \"%default\"]"),
		    make_option(c("-s", "--separator"), dest="separator", default="\t", help = "the data field separator [default \"%default\"]"),
		    make_option(c("-k", "--skip"), type="integer", default=0, dest="skip", help="Number of rows in the data file to skip [default \"%default\"]", metavar="number"),
		    make_option(c("-z", "--score"), dest="score", type="integer", default=1, help = "The column that holds the height data for the 3d plot [default \"%default\"]"),
		    make_option(c("-p", "--plot"), dest="plot", type="integer", default=-1, help = "The column that holds the flag on whether to include this line in the plot (1=include, 0=ignore) [default \"%default\"]"),
		    make_option(c("-b", "--burnin"), dest="burnin", default="0", help = "The number of selected samples to ignore (can be a csv list specific for each chain). If negative then then the we will remove all but the last n values from the chain. [default \"%default\"]"),
		    make_option(c("-c", "--columns"), dest="columns", default="2,3,4,5,6", help = "List of columns to plot [default \"%default\"]"),
		    make_option(c("-l", "--labels"), dest="labels", default="alpha,beta,gamma,delta,epsilon", help = "Labels of the columns to plot [default \"%default\"]"),
		    make_option(c("-x", "--xmin"), dest="xmin", type="double", default="-2e+308", help = "minimum values of the likelihood to consider [default \"%default\"]"),
		    make_option(c("-d", "--diagonal"), action="store_true", default=FALSE, dest="diagonal", help = "Plot the parameter density distributions along the diagonal [default \"%default\"]"),
		    make_option(c("-n", "--nlogscale"), action="store_true", default=FALSE, dest="logscale", help = "use log sale for the vertical axes of converging plots.[default \"%default\"]"),
		    make_option(c("-m", "--gelmanrubin"), action="store_true", default=FALSE, dest="gelman", help = "Calculate Gelman Rubin convergence statistics [default \"%default\"]")
		    )

# if you prefer to have each chain depicted in a different colour set useColours to true (T), the default (false)
# will set all chains to black.
useColours <- F

opt <- parse_args(OptionParser(option_list=option_list))

opt_columns <- as.numeric(strsplit(opt$columns, split = ",")[[1]])
opt_labels <- strsplit(opt$labels, split = ",")[[1]]

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

processedData <- new.env()
processedData$num_chains = 0
processedData$chain <- c()
processedData$chain_list <- list()
# a vector of the MCMC objects (contains the MCMC scores)
processedData$mcmc_chain <- c()

burn_in <- as.integer(unlist(strsplit(opt$burnin, ',')))
#if (length(burn_in) < length(files)) {
#    burn_in <- c(opt$burnin)
#}

# look at each chain in parallel, extracting accepted steps and removing burn-in steps as required.
process_chains <- function(datafile, burnin) {

    chain <- try(read.csv(datafile, sep=opt$separator,  comment.char = "#", header=FALSE), silent=TRUE)
    if (class(chain) != "try-error") {
	if (opt$plot != -1) {
	    # filter out the rejected samples from the data frame.
	    dataSubSet <- subset(chain, chain[,opt$plot] == 1)

	    # now remove the burn-in samples (if the burn-in is larger than the samples ignore data set)
	    if (burnin < 0) {
		chain <- tail(dataSubSet, -1*burnin)
	    } else if (burnin < nrow(dataSubSet)) {
		chain <- dataSubSet[(burnin+1):nrow(dataSubSet),]
	    } else {
		chain <- dataSubSet[0,]
	    }
	}
	chain <- subset(chain, chain[,opt$score] > opt$xmin)

	if (nrow(chain) > 1 ) {
	    cat (sprintf("                             merging %s (%d rows)\n", datafile, nrow(chain)))
	    processedData$num_chains <- processedData$num_chains + 1
	    processedData$chain <- rbind(processedData$chain, chain)

	    # to process chains we need to filter out values that are ridicuously low, e.g. ignored trials often are
	    # set to the minimum value of a double.
	    chain_subset <- subset(chain, chain[,opt$score] > opt$xmin)

	    processedData$chain_list <- append(processedData$chain_list, chain_subset)
	    processedData$mcmc_chain <- append(processedData$mcmc_chain, mcmc.list(mcmc(chain_subset[opt$score])) )
	}
    }
}

# process the chains by reading the files, removing any burn-in and 
# then creating a single combined chain, list of chains etc for further plotting.
invisible(mapply(process_chains, files, burn_in))

# do some further filtering on the processed data e.g. extract the parameters for
# the maximum value of the score command line argument and
data <- processedData$chain
#print(data)

mergedParameterChains <- data.matrix(data[opt_columns])
maxScore <- (max( data[,opt$score] ))
dataset <- subset(data, data[,opt$score] == maxScore) 
#mattMaxL <- data.matrix(dataset[opt_columns])
cat (sprintf("                                     mean score = %g\n", mean(data[,opt$score])))
cat (sprintf("                                     Rows read = %d\n", nrow(data)))
#cat (sprintf("                                     parameters = %g\n", dataset))

if (useColours) {
    cols <- rainbow(processedData$num_chains)
} else {
    cols <- rep("black", processedData$num_chains)
}


############################################################################################################################################
############################################################################################################################################
############################################################################################################################################
############################################################################################################################################
############################################################################################################################################


cat (sprintf("Printing parameter distributions for %s\n", opt$input))

pdffile <- paste(outputFileNameBase, "_param_sampled_values.pdf", sep="", collapse = NULL)
pdf(file=pdffile, onefile=FALSE, width=7.5, height=7.5)

# scatterplot on the upper panel
panel.points <- function(x, y) { 
    try(points(x, y,pch=20, cex=0.03), silent = TRUE)
}

# plot an empty panel (useful for debugging)
panel.empty <- function(x, y) { 
}

# plot an empty panel (useful for debugging)
panel.quilt <- function(x, y) { 
    try(quilt.plot(x, y, data[,opt$score], add=TRUE, add.legend=FALSE, col=heat.colors(64), gap=0))
}

# contour plot for the upper panel
panel.contour <- function(x, y, digits=2, prefix="", cex.cor) {
    data.interp <- matrix()
    tryd <- try(data.interp <- interp(x, y, data[,opt$score], linear=T, extrap=F, duplicate="strip"), silent=TRUE)
    if(class(tryd) != "try-error") {
	try(image(data.interp, col = heat.colors(64),add=TRUE)  , silent = TRUE)
    }
}

# Diagonal panel showing the density distribution
panel.hist <- function(x, varname, ...) { 

    # get some graphical parameter settings, and reset them on exit
    usr <- par("usr"); on.exit(par(usr))
    par(usr = c(usr[1:2], 0, 1.5) )

    tryd <- try( d <- density(x, na.rm=TRUE, bw="nrd", adjust=1.2), silent=TRUE)
    if(class(tryd) != "try-error") {
	d$y <- d$y/max(d$y)
	lines(d)  
	try(boxplot(x, at=0.2, boxwex=0.3, horizontal=TRUE, add=TRUE, xaxt="n"), silent = TRUE)
    }
}

# Labels on the diagonal
panel.text <- function(x, y, opt_lbls, cex, font) {
    txt <- parse(text = opt_labels[parent.frame(1)$i])
    if (opt$gelman) {
	text(0.2, 0.85, txt, cex=1.5)
    } else {
	text(0.4, 0.75, txt, cex=1.5)
    }
}

# Now plot the data.
if (opt$gelman) {
    pairs(mergedParameterChains, lower.panel=panel.points, upper.panel=panel.quilt, diag.panel=panel.hist, text.panel=panel.text, gap=0) 
} else {
    pairs(mergedParameterChains, lower.panel=panel.points, upper.panel=panel.quilt, text.panel=panel.text, gap=0) 
}

#dev.off()

############################################################################################################################################

cat (sprintf("Printing parameter distribution densities for %s\n", opt$input))

result = tryCatch({
    pdffile <- paste(outputFileNameBase, "_param_posterior_distribution.pdf", sep="", collapse = NULL)
    pdf(file=pdffile, onefile=FALSE, width=7.5, height=7.5)

    opt_columns <- as.numeric(strsplit(opt$columns, split = ",")[[1]])
    numCols = 3
    numRows = ceiling(length(opt_columns)/numCols)

    par(mfrow = c(numRows, numCols))
    par(mar=c(4,4,1,1), cex.lab=2)

    for (i in 1:length(opt_columns)) {
	column = as.numeric(opt_columns[i])

	if (bw.nrd(mergedParameterChains[,i]) > 0) {
	    dens <- try(density(mergedParameterChains[,i], kernel="gaussian", bw="nrd", n=8192), silent=TRUE)
	    box <-  boxplot.stats(mergedParameterChains[,i])

	    # determine some sensible limits for the plot from the boxplot statistics. 
	    # We try a weighted mean of the limits of the boxplot.stats (i.e. the lower and upper whiskers) and the min and max values of the data
	    max_x <- weighted.mean(c(box$stats[5],max(mergedParameterChains[,i])), c(1000,1.0) )
	    min_x <-  weighted.mean(c(box$stats[1],min(mergedParameterChains[,i])), c(1000,1.0) )

	    # create the label for each plot adding the median values for each.
	    txt <- parse(text=sprintf("%s", opt_labels[i]))

	    plot(dens, axes=T, xlab=txt, ylab="", main="", xlim=c(min_x,max_x), cex.lab=1.2)
	    box_pos <- abs(0.1*max(dens$y))
	    try(boxplot(mergedParameterChains[,i], at=box_pos, boxwex=box_pos, outline=FALSE, horizontal=TRUE, add=TRUE, xaxt="n"), silent = FALSE)
	}
    }

}, error = function(err) {
    cat(paste("    Could not create parameter distribution densities:  ",err))
})

############################################################################################################################################

cat (sprintf("Printing parameter chains for %s\n", opt$input))


result = tryCatch({
    pdffile <- paste(outputFileNameBase, "_param_evolution.pdf", sep="", collapse = NULL)
    pdf(file=pdffile, onefile=FALSE, width=7.5, height=7.5)

    opt_columns <- as.numeric(strsplit(opt$columns, split = ",")[[1]])
    numCols = 3
    numRows = ceiling(length(opt_columns)/numCols)

    par(mfrow = c(numRows, numCols))
    par(mar=c(4,4,1,1), cex.lab=1.0)
    min_y <- 1E100
    max_y <- -1E100

    for (i in 1:length(opt_columns)) {
	column = as.numeric(opt_columns[i])

	lb <- opt_labels[i]
	txt <- parse(text=lb)

	# calculate the x,y limits for the axes by finding the max value of the elements in the data
	# and adding/subtracting 5%
	max_x <- 1.05*length(processedData$chain_list[[column]])
	min_y <- (1-sign(min_y)*0.05)*min(processedData$chain_list[[column]])
	max_y <- (1+sign(max_y)*0.05)*max(processedData$chain_list[[column]])
	df_size <- length(processedData$chain_list)/processedData$num_chains
	if (processedData$num_chains > 1) {
	    for (j in 2:processedData$num_chains) {
		n <- ((j-1)*df_size) + column
		max_x <- max(max_x, 1.05*length(processedData$chain_list[[n]]))
		min_y <- min(min_y, 0.95*min(processedData$chain_list[[n]]))
		max_y <- max(max_y, 1.05*max(processedData$chain_list[[n]]))
	    }
	}

	logscale = ""
	if (opt$logscale == TRUE) {
	    logscale = "y"
	}
	plot(1, type='n', log=logscale, ylab=txt, xlab="step", xlim=c(1,max_x), ylim=c(min_y,max_y))
	for (j in 1:processedData$num_chains) {
	    n <- ((j-1)*df_size) + column
	    lines( processedData$chain_list[[n]], col=cols[j])
	}
    }

}, error = function(err) {
    cat(paste("    Could not create parameter chains:  ",err))
})

############################################################################################################################################

cat (sprintf("Printing convergence for %s\n", opt$input))
result = tryCatch({

    pdffile <- paste(outputFileNameBase, "_score_evolution.pdf", sep="", collapse = NULL)
    pdf(file=pdffile, onefile=FALSE, width=7.5, height=7.5)

    min_y <- 1E100
    max_y <- -1E100

    # calculate the x,y limits for the axes by finding the max value of the elements in the data
    # and adding/subtracting 5%
    max_x <- 1.05*length(processedData$chain_list[[opt$score]])
    df_size <- length(processedData$chain_list)/processedData$num_chains
    if (processedData$num_chains > 1) {
	for (j in 2:processedData$num_chains) {
	    n <- ((j-1)*df_size) + opt$score
	    max_x <- max(max_x, 1.05*length(processedData$chain_list[[n]]))
	}
    }

    max_y <- max(processedData$chain[[opt$score]])
    min_y <- min(processedData$chain[[opt$score]])
    min_y <- (1-sign(min_y)*0.05)*min_y
    max_y <- (1+sign(max_y)*0.05)*max_y

    logscale = ""
    if (opt$logscale == TRUE) {
	logscale = "y"
    }
    plot(1, type='n', log=logscale, ylab="", xlab="step", xlim=c(1,max_x), ylim=c(min_y,max_y))
    for (j in 1:processedData$num_chains) {
	n <- ((j-1)*df_size) + opt$score
	lines( processedData$chain_list[[n]], col=cols[j])
    }

}, error = function(err) {
    cat(paste("    Could not create cnvergence plot:  ",err))
})


############################################################################################################################################

cat (sprintf("Printing chain distribution %s\n", opt$input))
result = tryCatch({

    pdffile <- paste(outputFileNameBase, "_score_posterior.pdf", sep="", collapse = NULL)
    pdf(file=pdffile, onefile=FALSE, width=7.5, height=7.5)

    opt_columns <- as.numeric(strsplit(opt$columns, split = ",")[[1]])

    min_x <- 1E100
    max_x <- -1E100
    max_y <- 0
    df_size <- length(processedData$chain_list)/processedData$num_chains
    for (j in 1:processedData$num_chains) {
	n <- ((j-1)*df_size) + opt$score
	box <-  boxplot.stats(processedData$chain_list[[n]])
	dens <- try(density(processedData$chain_list[[n]], kernel="gaussian", bw="nrd", n=8192), silent=TRUE)
	min_x <- min(min_x, box$stats[1]) 
	max_x <- max(max_x, box$stats[5]) 
	max_y <- max(max_y, max(dens$y))
    }

    plot(1, type='n', ylab="", xlab="", xlim=c(min_x,max_x), ylim=c(0,max_y))
    for (j in 1:processedData$num_chains) {
	n <- ((j-1)*df_size) + opt$score
	dens <- try(density(processedData$chain_list[[n]], kernel="gaussian", bw="nrd", n=8192), silent=TRUE)
	lines(dens, col=cols[j])
    }

}, error = function(err) {
    cat(paste("    Could not create chain distributions:  ",err))
})


############################################################################################################################################

if (opt$gelman) {
    print (sprintf("Gelman-Rubin convergence statistics for %s\n", opt$input))

    tryCatch ({
	pdffile <- paste(outputFileNameBase, "_gelman_plot.pdf", sep="", collapse = NULL)
	pdf(file=pdffile, onefile=FALSE, width=7.5, height=7.5)

	mchains <- mcmc.list( processedData$mcmc_chain)
	print(gelman.diag(mchains))
	gelman.plot(mchains, xlab="Step", ylab="Potential Scale Reduction Factor", main="")
    }, error = function(err) {
	cat(paste("    Could not produce Gelman-Rubin plot:  ",err))
    })
}

############################################################################################################################################

cat (sprintf("Printing parameter correlations %s\n", opt$input))

tryCatch ({
    pdffile <- paste(outputFileNameBase, "_parameter_correlations.pdf", sep="", collapse = NULL)
    pdf(file=pdffile, onefile=FALSE, width=7.5, height=7.5)

    corrMatrix <- cor(mergedParameterChains)

    rotateImage <- function(m) t(m)[,nrow(m):1]

    # Labels on the diagonal
    panel.text <- function(x, y, opt_lbls, cex, font) {
	txt <- parse(text = opt_labels[parent.frame(1)$i])
	text(0.4, 0.75, txt, cex=2.5)
    }

    #image( rotateImage(corrMatrix))
    chart.Correlation(corrMatrix, text.panel=panel.text, histogram=T)
}, error = function(err) {
    cat(paste("    Could not create parameter correlations:  ",err))
})
