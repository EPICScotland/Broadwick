if (!"optparse" %in% installed.packages())
    install.packages("optparse")
suppressPackageStartupMessages(library("optparse"))
if (!"fields" %in% installed.packages())
    install.packages("fields")
suppressPackageStartupMessages(library("fields"))





option_list <- list(
                    make_option(c("-i", "--input"), default="particle_*.dat", help = "Input file containing the parameters to plot [default \"%default\"]"),
                    make_option(c("-o", "--output"), default="scoreDist.pdf", help = "Output file containing [default \"%default\"]"),
                    make_option(c("-s", "--separator"), dest="separator", default=",", help = "the data field separator [default \"%default\"]"),
                    make_option(c("-k", "--skip"), type="integer", default=0, dest="skip", help="Number of rows in the data file to skip [default \"%default\"]", metavar="number"),
                    make_option(c("-v", "--var"), dest="var", type="integer", default=1, help = "The column that holds the variable to plot [default \"%default\"]"),
                    make_option(c("-f", "--filter"), type="double", default="-2e+308", dest="filterVal", help = "The minimum value of the score in the distribution. [default \"%default\"]"),
                    make_option(c("-c", "--columns"), dest="columns", default="2,3,4,5,6", help = "List of columns to plot [default \"%default\"]"),
                    make_option(c("-l", "--labels"), dest="labels", default="alpha,beta,gamma,delta,epsilon", help = "Labels of the columns to plot [default \"%default\"]"),
                    make_option(c("-p", "--plot"), dest="plot", type="integer", default=-1, help = "The column that holds the flag on whether to include this line in the plot (1=include, 0=ignore) [default \"%default\"]"),
                    make_option(c("-t", "--limits"), dest="limits", default="", help = "Set limits for the sampled parameter plots (NULL = use quartiles) [default \"%default\"]"),
                    make_option(c("--colours"), action="store_true", default=FALSE, dest="useColours", help = "Use colours for the different particles [default \"%default\"]")
                    )


opt <- parse_args(OptionParser(option_list=option_list))

opt_columns <- as.numeric(strsplit(opt$columns, split = ",")[[1]])
opt_labels <- strsplit(opt$labels, split = ",")[[1]]
opt_limits <- as.list(strsplit(opt$limits, split = ","))
opt_filter <- as.numeric(opt$filterVal)


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
chains$matrix <- matrix(nrow=0, ncol=length(opt_columns))
chains$num <- 0
chains$maxLength <- 0
chains$maxVar <- opt_filter*1000

# look at each chain in parallel, exoptracting accepted steps and removing burn-in steps as required.
process_chains <- function(datafile) {

    chain <- try(read.csv(datafile, sep=opt$separator,  comment.char = "#", header=FALSE, stringsAsFactors = FALSE), silent=TRUE)
    if (class(chain) != "try-error") {
        # We only want the last element in the chain, as long as the score is finite and greater than opt$filterVal, i.e. not rejected.

        if (opt$plot != -1) {
            # filter out the data that should not be plotted.
            chain <- subset(chain, chain[,opt$plot] == 1)
        }

        # filter out the score values that are too small
        chain <- subset(chain, chain[,opt$var] > opt_filter)
        if (nrow(chain) > 0 ) {
            chains$maxVar <- max(chains$maxVar, max(chain[,opt$var]))

            # From all the columns in the data we only require the opt_columns column so we remove all others now
            chain <- chain[c(opt_columns)]
            sapply(chain, format, trim = TRUE)
            colnames(chain) <- c(opt_labels)

            cat (sprintf("                                                      Reading %s (%d rows)\n", datafile, nrow(chain)))

            # We now have all the data from this file to save to our list of chains from all the files.
            chains$matrix <- rbind(chains$matrix, data.matrix(tail(chain, n=1)))
            chains$num <- 1 + chains$num 
            if (nrow(chain) > 0) {
                chains$maxLength <- max(chains$maxLength, nrow(chain))
            }
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

if(chains$num == 0) { 
    stop("Not enough observations : n < 1")
}


cat(sprintf("max value = %f\n", chains$maxVar) )

cols <- rep("black", chains$num)
if (opt$useColours) {
    cols <- rainbow(chains$num)
}



############################################################################################################################################
#
#  Plots of the evolution of each score (likelihood) 
#
############################################################################################################################################

cat (sprintf("Printing population distribution densities for %s\n", opt$input))
numCols = 2
numRows = ceiling(ncol(chains$matrix[])/numCols)


result = tryCatch({
    pdffile <- paste(outputFileNameBase, "_population_distribution.pdf", sep="", collapse = NULL)
    pdf(file=pdffile, onefile=FALSE, width=7.5, height=7.5)

    par(mfrow = c(numRows, numCols))
    par(mar=c(4,2,1,1), cex.lab=2)

    for (i in seq(1,ncol(chains$matrix[]))) {

        #if (bw.nrd(chains$matrix[,i]) > 0) {
            dens <- try(density(chains$matrix[,i], kernel="gaussian", bw="nrd0", n=256), silent=TRUE)
            box <-  boxplot.stats(chains$matrix[,i])

            # determine some sensible limits for the plot from the boxplot statistics. 
            # We try a weighted mean of the limits of the boxplot.stats (i.e. the lower and upper whiskers) and the min and max values of the data
            max_x <- weighted.mean(c(box$stats[5],max(chains$matrix[,i])), c(1000,1.0) )
            min_x <- weighted.mean(c(box$stats[1],min(chains$matrix[,i])), c(1000,1.0) )

            # if opt_limits is not null then get xmin xmax from here....
            if (length(opt_limits[[1]]) > 1) {
                min_x <- as.numeric(opt_limits[[1]][2*i-1])
                max_x <- as.numeric(opt_limits[[1]][2*i])
            }

            # create the label for each plot adding the median values for each.
            txt <- parse(text=sprintf("%s", opt_labels[i]))

            plot(dens, axes=T, xlab=txt, ylab="", main="", yaxt="n", xlim=c(min_x,max_x), cex.lab=1.2)
            box_pos <- abs(0.1*max(dens$y))
            try(boxplot(chains$matrix[,i], at=box_pos, boxwex=box_pos, outline=FALSE, horizontal=TRUE, add=TRUE, xaxt="n", na.rm=T), silent = FALSE)


            # calculate the limits defining where 95% of the density lies using the computed density kernel
            dn <- cumsum(dens$y)/sum(dens$y)
            li <- which(dn>=0.05)[1]
            ui <- which(dn>=0.95)[1]
            limits <- dens$x[c(li,ui)]

            cat(sprintf("      %s = %f [95%% CI = %f,%f]\n", opt_labels[i], box$stats[3], limits[1], limits[2]) )

        #}
    }

}, error = function(err) {
    cat(paste("****Could not plot parameter distribution:  ",err))
}, warning = function(war) {
    cat(paste("****Could not plot parameter distribution:  ",war))
})



result = tryCatch({
    pdffile <- paste(outputFileNameBase, "_population_histogram.pdf", sep="", collapse = NULL)
    pdf(file=pdffile, onefile=FALSE, width=7.5, height=7.5)

    par(mfrow = c(numRows, numCols))
    par(mar=c(4,2,1,1), cex.lab=2)

    for (i in seq(1,ncol(chains$matrix[]))) {
        dens <- try(density(chains$matrix[,i], kernel="gaussian", bw="nrd0", n=256), silent=TRUE)

        # create the label for each plot adding the median values for each.
        lb <- opt_labels[i]
        txt <- parse(text=lb)

        # if opt_limits is not null then get xmin xmax from here....
        #if (length(opt_limits[[1]]) > 1) {
        #    min_x <- as.numeric(opt_limits[[1]][2*i-1])
        #    max_x <- as.numeric(opt_limits[[1]][2*i])
            box <-  boxplot.stats(chains$matrix[,i])
            max_x <- weighted.mean(c(box$stats[5],max(chains$matrix[,i])), c(1000,1.0) )
            min_x <- weighted.mean(c(box$stats[1],min(chains$matrix[,i])), c(1000,1.0) )

            hist(chains$matrix[,i], freq = FALSE, xlab=txt, ylab="", main="", yaxt="n", xlim=c(min_x,max_x), cex.lab=1.2)
       # } else {
        #                hist(chains$matrix[,i], freq = FALSE, xlab=txt, ylab="", main = "")
        #}
        lines(dens)
    }

}, error = function(err) {
    cat(paste("****Could not plot parameter distribution:  ",err))
}, warning = function(war) {
    cat(paste("****Could not plot parameter distribution:  ",war))
})


