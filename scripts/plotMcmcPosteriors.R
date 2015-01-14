suppressPackageStartupMessages(library("optparse"))
suppressPackageStartupMessages(library("akima"))
suppressPackageStartupMessages(library("fields"))
suppressPackageStartupMessages(library("corrplot"))
suppressPackageStartupMessages(library("ggplot2"))
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
		    make_option(c("-m", "--gelmanrubin"), action="store_true", default=FALSE, dest="gelman", help = "Calculate Gelman Rubin convergence statistics [default \"%default\"]"),
		    make_option(c("-a", "--analyse"), action="store_true", default=FALSE, dest="analyse", help = "Perform analysis, e.g. principlal comp. analysis on the data [default \"%default\"]")
		    )

# if you prefer to have each chain depicted in a different colour set useColours to true (T), the default (false)
# will set all chains to black.
useColours <- T

opt <- parse_args(OptionParser(option_list=option_list))

opt_score <- as.numeric(opt$score)
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

burn_in <- as.integer(unlist(strsplit(opt$burnin, ',')))

# This is the dtat store we will use in the script. It consists of 4 data types
# 1) chains - a list containing all the chains we've read (converted into matrices) chains$chains[[i]] = ith chain
# 2) matrix - a matrix containing all the chains merged together. chains$matrix[,i] = all the values for the ith parameter we've read.
# 3) mcmc - a vector of the MCMC objects (contains the MCMC scores)
# 4) num - the number of chains storedin the environment

chains <- new.env()
chains$chains <- list()
chains$matrix <- matrix(nrow=0, ncol=length(opt_columns)+1)
chains$mcmc <- c()
chains$num <- 0
chains$maxLength <- 0

# look at each chain in parallel, exoptracting accepted steps and removing burn-in steps as required.
process_chains <- function(datafile, burnin, numChains) {


    chain <- try(read.csv(datafile, sep=opt$separator,  comment.char = "#", header=FALSE, stringsAsFactors = FALSE), silent=TRUE)
    if (class(chain) != "try-error") {
	if (opt$plot != -1) {
	    # filter out the rejected samples from the data frame.
	    chain <- subset(chain, chain[,opt$plot] == 1)

	    # now remove the burn-in samples (if the burn-in is larger than the samples ignore data set)
	    if (burnin < 0) {
		chain <- tail(chain, -1*burnin)
	    } else if (burnin < nrow(chain)) {
		chain <- chain[(burnin+1):nrow(chain),]
	    } else {
		chain <- chain[0,]
	    }
	}

	# Now remove any samples (rows in the file) from the data file where the score is too low
	chain <- subset(chain, chain[,opt$score] > opt$xmin)

	# we only ever need a subset of the columns in the sample so we filter them now and rename them with the labels we've been supplied with
	chain <- chain[c(opt_columns,opt_score)]
	sapply(chain, format, trim = TRUE)
	colnames(chain) <- c(opt_labels, "score")

	if (nrow(chain) > 1 ) {
	    cat (sprintf("                                                      merging %s (%d rows)\n", datafile, nrow(chain)))

	    # We now have all the data from this file to save to our list of chains from all the files.
	    chains$chains <- unlist(list(chains$chains, list(data.matrix(chain))), recursive=FALSE) # will append a matrix put we can iterate over each chain
	    chains$matrix <- rbind(chains$matrix, data.matrix(chain))
	    chains$mcmc <- append(chains$mcmc, mcmc.list(mcmc(chain[ncol(chain)])))  # the score that we require for the MCMC object was the last column added to the chain!
	    chains$num <- 1 + chains$num 
	    chains$maxLength <- 1 + max(chains$maxLength, nrow(chain))
	} else {
	    cat (sprintf("                                                     ignoring %s (%d rows)\n", datafile, nrow(chain)))
	}
    }

}


# process the chains by reading the files, removing any burn-in and 
# then creating a single combined chain, list of chains etc for further plotting.
invisible(mapply(process_chains, files, burn_in))

#print ("######################################################################")
#print (chains)
#print (chains$chains)
#print (chains$matrix)
#print (chains$mcmc)
#print (chains$num)
#print ("######################################################################")




# Save some useful parameters about the chains
maxScore <- (max(chains$matrix[,ncol(chains$matrix)] ))
chain_cols <- 1:(ncol(chains$matrix)-1)   # the columns that were specied on the command line are now stored in the first ncol(chains$chains)-1 columns of chains$chains
chain_colScore <- ncol(chains$matrix)     # the score column on the command line os now store in the last column of chains$chains
cat (sprintf("                                     mean score = %g\n", mean(chains$matrix[,chain_colScore])))
cat (sprintf("                                     number of rows read = %d\n", nrow(chains$matrix)))
cat (sprintf("                                     number of chains read = %d\n", chains$num))


if (useColours) {
  cols <- rainbow(chains$num)
} else {
  cols <- rep("black", chains$num)
}

# TODO ################
#print (chains$chains[][])
#print (nrow(chains$chains[,1]))
#maxChainLength <- length (max(nrow(chains$chains)))

############################################################################################################################################
############################################################################################################################################
#
#  Now do the plotting.............
#
############################################################################################################################################
############################################################################################################################################
############################################################################################################################################













############################################################################################################################################
#
#  Scatterplot of the sampled parameters
#
############################################################################################################################################

cat (sprintf("Printing parameter sampling distributions for %s\n", opt$input))
result = tryCatch({
      pdffile <- paste(outputFileNameBase, "_sampled_parameter_values.pdf", sep="", collapse = NULL)
  pdf(file=pdffile, onefile=FALSE, width=7.5, height=7.5)


  # scatterplot on the lower panel
  panel.points <- function(x, y) { 
    try(points(x, y, pch=20), silent = TRUE)
  }
  
  # plot an empty panel (useful for debugging)
  panel.empty <- function(x, y) { 
  }
  
  # plot an empty panel (useful for debugging)
  panel.quilt <- function(x, y) { 
    try(quilt.plot(x, y, chains$matrix[,chain_colScore], add=TRUE, add.legend=FALSE, col=heat.colors(64), gap=0))
  }
  
  # contour plot for the upper panel
  panel.contour <- function(x, y, digits=2, prefix="", cex.cor) {


      #fld <- with(df, interp(x = Lon, y = Lat, z = Rain))
      #df <- melt(fld$z, na.rm = TRUE)
      #ggplot(data = df)


    data.interp <- matrix()
    tryd <- try(data.interp <- interp(x, y, chains$matrix[,chain_colScore], linear=T, extrap=F, duplicate="strip"), silent=TRUE)
    if(class(tryd) != "try-error") {

       #filled.contour(data.interp)
       try(image(data.interp, col = heat.colors(64),add=TRUE)  , silent = TRUE)
    } else {

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
    pairs(chains$matrix[,chain_cols], lower.panel=panel.points, upper.panel=panel.quilt, diag.panel=panel.hist, text.panel=panel.text, gap=0) 
  } else {
    pairs(chains$matrix[,chain_cols], lower.panel=panel.points, upper.panel=panel.quilt, text.panel=panel.text, gap=0) 
  }
 
}, error = function(err) {
  cat(paste("****Could not plot parameter distribution:  ",err))
}, warning = function(war) {
  cat(paste("****Could not plot parameter distribution:  ",war))
})



############################################################################################################################################
#
#  Scatterplot of parameter correlations
#
############################################################################################################################################

cat (sprintf("Printing parameter correlations for %s\n", opt$input))

tryCatch ({
  pdffile <- paste(outputFileNameBase, "_parameter_correlations.pdf", sep="", collapse = NULL)
  pdf(file=pdffile, onefile=FALSE, width=7.5, height=7.5)
  
  corrMatrix <- cor(chains$matrix[,chain_cols], method="spearman")
  corrplot.mixed(corrMatrix, lower = "number", upper = "pie", order = "FPC")

}, error = function(err) {
  cat(paste("****Could not create parameter correlations:  ",err))
}, warning = function(war) {
  cat(paste("****Could not create parameter correlations:  ",war))
})

tryCatch ({
    pdffile <- paste(outputFileNameBase, "_parameter_correlations_scatterplot.pdf", sep="", collapse = NULL)
    pdf(file=pdffile, onefile=FALSE, width=7.5, height=7.5)


    panel.cor <- function(x, y, digits=2, prefix="", use="pairwise.complete.obs", cex.cor)
    {
	usr <- par("usr"); on.exit(par(usr))
	par(usr = c(0, 1, 0, 1))
	r <- abs(cor(x, y, use = use,method="spearman"))
	txt <- format(c(r, 0.123456789), digits=digits)[1]
	txt <- paste(prefix, txt, sep="")
	if(missing(cex.cor)) cex <- 0.8/strwidth(txt)

	test <- cor.test(x,y)
	# borrowed from printCoefmat
	Signif <- symnum(test$p.value, corr = FALSE, na = FALSE,
			 cutpoints = c(0, 0.001, 0.01, 0.05, 0.1, 1),
			 symbols = c("***", "**", "*", ".", " "))

	text(0.5, 0.5, txt, cex = cex * r)
	text(.8, .8, Signif, cex=cex, col=2)
    }

    hist.panel = function (x) {
	par(new = TRUE)
	hist(x,
	     col = "light gray",
	     probability = TRUE,
	     axes = FALSE,
	     main = "",
	     breaks = "FD")
	lines(density(x, na.rm=TRUE),
	      col = "red",
	      lwd = 1)
	#lines(f, col="blue", lwd=1, lty=1) how to add gaussian normal overlay?
	rug(x)
    }

    # Labels on the diagonal
    panel.text <- function(x, y, opt_lbls, cex, font) {
	txt <- parse(text = opt_labels[parent.frame(1)$i])
	text(0.4, 0.75, txt, cex=1.5)
    }
  
    pairs(chains$matrix[,chain_cols], gap=0, lower.panel=panel.smooth, upper.panel=panel.cor, diag.panel=hist.panel, text.panel=panel.text)

}, error = function(err) {
    cat(paste("****Could not create parameter correlations:  ",err))
}, warning = function(war) {
    cat(paste("****Could not create parameter correlations:  ",war))
})





############################################################################################################################################
#
#  Distribution densities for each parameter
#
############################################################################################################################################

cat (sprintf("Printing parameter distribution densities for %s\n", opt$input))

result = tryCatch({
  pdffile <- paste(outputFileNameBase, "_sampled_paramameter_distribution.pdf", sep="", collapse = NULL)
  pdf(file=pdffile, onefile=FALSE, width=7.5, height=7.5)
  
  numCols = 3
  numRows = ceiling(length(chain_cols)/numCols)
  
  par(mfrow = c(numRows, numCols))
  par(mar=c(4,4,1,1), cex.lab=2)
  
  for (i in chain_cols) {

    if (bw.nrd(chains$matrix[,i]) > 0) {
      dens <- try(density(chains$matrix[,i], kernel="gaussian", bw="nrd", n=8192), silent=TRUE)
      box <-  boxplot.stats(chains$matrix[,i])
      
      # determine some sensible limits for the plot from the boxplot statistics. 
      # We try a weighted mean of the limits of the boxplot.stats (i.e. the lower and upper whiskers) and the min and max values of the data
      max_x <- weighted.mean(c(box$stats[5],max(chains$matrix[,i])), c(1000,1.0) )
      min_x <- weighted.mean(c(box$stats[1],min(chains$matrix[,i])), c(1000,1.0) )
      
      # create the label for each plot adding the median values for each.
      txt <- parse(text=sprintf("%s", opt_labels[i]))
      
      plot(dens, axes=T, xlab=txt, ylab="", main="", xlim=c(min_x,max_x), cex.lab=1.2)
      box_pos <- abs(0.1*max(dens$y))
      try(boxplot(chains$matrix[,i], at=box_pos, boxwex=box_pos, outline=FALSE, horizontal=TRUE, add=TRUE, xaxt="n", na.rm=T), silent = FALSE)
    }
  }
  
}, error = function(err) {
  cat(paste("****Could not plot parameter distribution:  ",err))
}, warning = function(war) {
  cat(paste("****Could not plot parameter distribution:  ",war))
})


############################################################################################################################################
#
#  Plots of the evolution of each paramter 
#
############################################################################################################################################

cat (sprintf("Printing parameter evolution for %s\n", opt$input))
result = tryCatch({
  pdffile <- paste(outputFileNameBase, "_parameter_evolution.pdf", sep="", collapse = NULL)
  pdf(file=pdffile, onefile=FALSE, width=7.5, height=7.5)
  
  numCols = 3
  numRows = ceiling(length(chain_cols)/numCols)
  
  par(mfrow = c(numRows, numCols))
  par(mar=c(4,4,1,1), cex.lab=1.5)
  min_y <- 1E100
  max_y <- -1E100
  
  for (i in chain_cols) {
    
    lb <- opt_labels[i]
    txt <- parse(text=lb)
    
    # calculate the x,y limits for the axes by finding the max value of the elements in the data
    # and adding/subtracting 5%
    min_y <- (1-sign(min_y)*0.05)*min(chains$matrix[,i])
    max_y <- (1+sign(max_y)*0.05)*max(chains$matrix[,i])
    df_size <- length(chains$matrix[,i])/chains$num
    
    logscale = ""
    if (opt$logscale == TRUE) {
      logscale = "y"
    }
    plot(1, type='n', log=logscale, ylab=txt, xlab="step", xlim=c(1,chains$maxLength), ylim=c(min_y,max_y))
    for (j in 1:chains$num) {
      lines( chains$chains[[j]][,i], col=cols[j]) # the ith column of the jth chain
    }
  }
  
}, error = function(err) {
  cat(paste("****Could not plot parameter evolution:  ",err))
}, warning = function(war) {
  cat(paste("****Could not plot parameter evolution:  ",war))
})


############################################################################################################################################
#
#  Plots of the evolution of each score (likelihood) 
#
############################################################################################################################################

cat (sprintf("Printing MCMC chain evolution for %s\n", opt$input))
result = tryCatch({
  
  pdffile <- paste(outputFileNameBase, "_score_evolution.pdf", sep="", collapse = NULL)
  pdf(file=pdffile, onefile=FALSE, width=7.5, height=7.5)
  
  min_y <- min(chains$matrix[,chain_colScore])
  max_y <- max(chains$matrix[,chain_colScore])
  
  logscale = ""
  if (opt$logscale == TRUE) {
    logscale = "y"
  }

  plot(1, type='n', log=logscale, ylab="", xlab="step", xlim=c(1,chains$maxLength), ylim=c(min_y,max_y))
  for (j in 1:chains$num) {
    lines(chains$chains[[j]][,chain_colScore], col=cols[j])
  }

}, error = function(err) {
  cat(paste("****Could not plot score evolution:  ",err))
}, warning = function(war) {
  cat(paste("****Could not plot score evolution:  ",war))
})



############################################################################################################################################
#
#  Plots of the evolution of each score (likelihood) 
#
############################################################################################################################################

cat (sprintf("Printing MCMC chain distribution for %s\n", opt$input))
result = tryCatch({
  
  pdffile <- paste(outputFileNameBase, "_score_distribution.pdf", sep="", collapse = NULL)
  pdf(file=pdffile, onefile=FALSE, width=7.5, height=7.5)
  
  min_x <- 1E100
  max_x <- -1E100
  max_y <- 0
  for (j in 1:chains$num) {
      box <-  boxplot.stats(chains$chains[[j]][,chain_colScore])
      dens <- try(density(chains$chains[[j]][,chain_colScore], kernel="gaussian", bw="nrd", n=8192), silent=TRUE)
<<<<<<< mine
      min_x <- min(min_x, box$stats[1]-0.01*abs(box$stats[1]))
      max_x <- max(max_x, box$stats[5]+0.01*abs(box$stats[5])) 
      max_y <- max(max_y, max(dens$y)*1.05)
=======
      min_x <- min(min_x, box$stats[1] - abs(box$stats[1])*0.01)
      max_x <- max(max_x, box$stats[5] + abs(box$stats[5])*0.01) 
      max_y <- max(max_y, (1+sign(max_y)*0.05)*max(dens$y))
>>>>>>> theirs
  }

  plot(1, type='n', ylab="", xlab="", xlim=c(min_x,max_x), ylim=c(0,max_y))
  for (j in 1:chains$num) {
    dens <- try(density(chains$chains[[j]][,chain_colScore], kernel="gaussian", bw="nrd", n=8192), silent=TRUE)
    lines(dens, col=cols[j])
  }
  
}, error = function(err) {
  cat(paste("****Could not plot score distribution:  ",err))
}, warning = function(war) {
  cat(paste("****Could not plot score distribution:  ",war))
})



############################################################################################################################################
#
#  Calculate the Gelman-Rubin statistic for the MCMC Chain.
#
############################################################################################################################################
if (opt$gelman && opt$plot != -1) {
  cat (sprintf("Gelman-Rubin convergence statistics for %s\n", opt$input))

  tryCatch ({
    pdffile <- paste(outputFileNameBase, "_Gelman-Rubin_plot.pdf", sep="", collapse = NULL)
    pdf(file=pdffile, onefile=FALSE, width=7.5, height=7.5)
    
    mchains <- mcmc.list( chains$mcmc )
    print(gelman.diag(mchains))
    gelman.plot(mchains, xlab="Step", ylab="Potential Scale Reduction Factor", main="")
  }, error = function(err) {
    cat(paste("****Could not produce Gelman-Rubin plot:  ",err))
  })
}


############################################################################################################################################
#
#  Perform statistical analysis (e.g PCA) on the data
#
############################################################################################################################################

if (opt$analyse) {
  cat (sprintf("Performing analysis on data %s\n", opt$input))

  
  tryCatch ({
    standardisedValues <- as.data.frame(scale(chains$matrix[,chain_cols]))
    
    # The means should now all be zero and the variances should be 1, can check it now.
    #print(sapply(standardisedValues,mean))
    #print(sapply(standardisedValues,sd))
    
    # Do a principal component analysis
    pca <- prcomp(standardisedValues)
    print(summary(pca))
    
    cat("Variable loadings (the columns contain the eigenvectors)\n")
    print(pca$rotation)
    cat("Principal Components (scores)\n")
    print(pca$x)
    
    pdffile <- paste(outputFileNameBase, "_scree_plot.pdf", sep="", collapse = NULL)
    pdf(file=pdffile, onefile=FALSE, width=7.5, height=7.5)
    screeplot(pca, type="lines", main="Principal Component Analysis: scree plot")
    
    pdffile <- paste(outputFileNameBase, "_component_scores.pdf", sep="", collapse = NULL)
    pdf(file=pdffile, onefile=FALSE, width=7.5, height=7.5)
    pairs(pca$x) 
    
  }, error = function(err) {
    cat(paste("****Could not perform data analysis:  ",err))
  })

}
