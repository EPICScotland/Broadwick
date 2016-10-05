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
                    make_option(c("-c", "--columns"), dest="columns", default="2,3,4,5,6", help = "List of columns to plot [default \"%default\"]"),
                    make_option(c("-l", "--labels"), dest="labels", default="alpha,beta,gamma,delta,epsilon", help = "Labels of the columns to plot [default \"%default\"]"),
                    make_option(c("-f", "--filter"), type="double", default="-2e+308", dest="filterVal", help = "The minimum value of the score in the distribution. [default \"%default\"]"),
                    make_option(c("-d", "--diagonal"), action="store_true", default=FALSE, dest="diagonal", help = "Plot the parameter density distributions along the diagonal [default \"%default\"]"),
                    make_option(c("-n", "--nlogscale"), action="store_true", default=FALSE, dest="logscale", help = "use log sale for the vertical axes of converging plots.[default \"%default\"]"),
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
####### Modify the pairs function so that I can specify the limits for each subplot. #######################################################
####### see http://stackoverflow.com/questions/22810309/pairs-specifying-axes-limits-of-the-subpanels ######################################
############################################################################################################################################

my.pairs <- function (x, labels, panel = points, ..., lower.panel = panel, 
          upper.panel = panel, diag.panel = NULL, text.panel = textPanel, 
          label.pos = 0.5 + has.diag/3, line.main = 3, cex.labels = NULL, 
          font.labels = 1, row1attop = TRUE, gap = 1, log = "", xlim=NULL, ylim=NULL) 
{
  if (doText <- missing(text.panel) || is.function(text.panel)) 
    textPanel <- function(x = 0.5, y = 0.5, txt, cex, font) text(x, 
                                                                 y, txt, cex = cex, font = font)
  localAxis <- function(side, x, y, xpd, bg, col = NULL, main, 
                        oma, ...) {
    xpd <- NA
    if (side%%2L == 1L && xl[j]) 
      xpd <- FALSE
    if (side%%2L == 0L && yl[i]) 
      xpd <- FALSE
    if (side%%2L == 1L) 
      Axis(x, side = side, xpd = xpd, ...)
    else Axis(y, side = side, xpd = xpd, ...)
  }
  localPlot <- function(..., main, oma, font.main, cex.main) plot(...)
  localLowerPanel <- function(..., main, oma, font.main, cex.main) lower.panel(...)
  localUpperPanel <- function(..., main, oma, font.main, cex.main) upper.panel(...)
  localDiagPanel <- function(..., main, oma, font.main, cex.main) diag.panel(...)
  dots <- list(...)
  nmdots <- names(dots)
  if (!is.matrix(x)) {
    x <- as.data.frame(x)
    for (i in seq_along(names(x))) {
      if (is.factor(x[[i]]) || is.logical(x[[i]])) 
        x[[i]] <- as.numeric(x[[i]])
      if (!is.numeric(unclass(x[[i]]))) 
        stop("non-numeric argument to 'pairs'")
    }
  }
  else if (!is.numeric(x)) 
    stop("non-numeric argument to 'pairs'")
  panel <- match.fun(panel)
  if ((has.lower <- !is.null(lower.panel)) && !missing(lower.panel)) 
    lower.panel <- match.fun(lower.panel)
  if ((has.upper <- !is.null(upper.panel)) && !missing(upper.panel)) 
    upper.panel <- match.fun(upper.panel)
  if ((has.diag <- !is.null(diag.panel)) && !missing(diag.panel)) 
    diag.panel <- match.fun(diag.panel)
  if (row1attop) {
    tmp <- lower.panel
    lower.panel <- upper.panel
    upper.panel <- tmp
    tmp <- has.lower
    has.lower <- has.upper
    has.upper <- tmp
  }
  nc <- ncol(x)
  if (nc < 2) 
    stop("only one column in the argument to 'pairs'")
  if (doText) {
    if (missing(labels)) {
      labels <- colnames(x)
      if (is.null(labels)) 
        labels <- paste("var", 1L:nc)
    }
    else if (is.null(labels)) 
      doText <- FALSE
  }
  oma <- if ("oma" %in% nmdots) 
    dots$oma
  main <- if ("main" %in% nmdots) 
    dots$main
  if (is.null(oma)) 
    oma <- c(4, 4, if (!is.null(main)) 6 else 4, 4)
  opar <- par(mfrow = c(nc, nc), mar = rep.int(gap/2, 4), oma = oma)
  on.exit(par(opar))
  dev.hold()
  on.exit(dev.flush(), add = TRUE)
  xl <- yl <- logical(nc)
  if (is.numeric(log)) 
    xl[log] <- yl[log] <- TRUE
  else {
    xl[] <- grepl("x", log)
    yl[] <- grepl("y", log)
  }
  for (i in if (row1attop) 
    1L:nc
       else nc:1L) for (j in 1L:nc) {
         l <- paste0(ifelse(xl[j], "x", ""), ifelse(yl[i], "y", 
                                                   ""))
         if (is.null(xlim) & is.null(ylim))
         localPlot(x[, j], x[, i], xlab = "", ylab = "", axes = FALSE, 
                   type = "n", ..., log = l)
         if (is.null(xlim) & !is.null(ylim))
         localPlot(x[, j], x[, i], xlab = "", ylab = "", axes = FALSE, 
                   type = "n", ..., log = l, ylim=ylim[j,i,])
         if (!is.null(xlim) & is.null(ylim))
         localPlot(x[, j], x[, i], xlab = "", ylab = "", axes = FALSE, 
                   type = "n", ..., log = l, xlim = xlim[j,i,])
         if (!is.null(xlim) & !is.null(ylim))
         localPlot(x[, j], x[, i], xlab = "", ylab = "", axes = FALSE, 
                   type = "n", ..., log = l, xlim = xlim[j,i,], ylim=ylim[j,i,])

         if (i == j || (i < j && has.lower) || (i > j && has.upper)) {
           box()
           if (i == 1 && (!(j%%2L) || !has.upper || !has.lower)) 
             localAxis(1L + 2L * row1attop, x[, j], x[, i], 
                       ...)
           if (i == nc && (j%%2L || !has.upper || !has.lower)) 
             localAxis(3L - 2L * row1attop, x[, j], x[, i], 
                       ...)
           if (j == 1 && (!(i%%2L) || !has.upper || !has.lower)) 
             localAxis(2L, x[, j], x[, i], ...)
           if (j == nc && (i%%2L || !has.upper || !has.lower)) 
             localAxis(4L, x[, j], x[, i], ...)
           mfg <- par("mfg")
           if (i == j) {
             if (has.diag) 
               localDiagPanel(as.vector(x[, i]), ...)
             if (doText) {
               par(usr = c(0, 1, 0, 1))
               if (is.null(cex.labels)) {
                 l.wid <- strwidth(labels, "user")
                 cex.labels <- max(0.8, min(2, 0.9/max(l.wid)))
               }
               xlp <- if (xl[i]) 
                 10^0.5
               else 0.5
               ylp <- if (yl[j]) 
                 10^label.pos
               else label.pos
               text.panel(xlp, ylp, labels[i], cex = cex.labels, 
                          font = font.labels)
             }
           }
           else if (i < j) 
             localLowerPanel(as.vector(x[, j]), as.vector(x[, 
                                                            i]), ...)
           else localUpperPanel(as.vector(x[, j]), as.vector(x[, 
                                                               i]), ...)
           if (any(par("mfg") != mfg)) 
             stop("the 'panel' function made a new plot")
         }
         else par(new = FALSE)
       }
  if (!is.null(main)) {
    font.main <- if ("font.main" %in% nmdots) 
      dots$font.main
    else par("font.main")
    cex.main <- if ("cex.main" %in% nmdots) 
      dots$cex.main
    else par("cex.main")
    mtext(main, 3, line.main, outer = TRUE, at = 0.5, cex = cex.main, 
          font = font.main)
  }
  invisible(NULL)
}




############################################################################################################################################
#
#  Plots of the evolution of each score (likelihood) 
#
############################################################################################################################################

cat (sprintf("Printing posterior sampling distributions for %s\n", opt$input))
result = tryCatch({

    # scatterplot of points
    panel.points <- function(x, y,...) { 
        try(points(x, y, pch=20,...), silent = TRUE)
    }

    # plot an empty panel (useful for debugging)
    panel.empty <- function(x, y,...) { 
    }

    # plot correlations
    panel.correl <- function(x, y, digits=2, prefix="", use="pairwise.complete.obs", cex.cor,...)
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

    .poly.pred <- function(fit, line = FALSE, xMin, xMax, lwd,...) {

        # predictions of fitted model

        # create function formula
        f <- vector("character", 0)

        for (i in seq_along(coef(fit))) {

            if (i == 1) {
                temp <- paste(coef(fit)[[i]])
                f <- paste(f, temp, sep = "")
            }

            if (i > 1) {
                temp <- paste("(", coef(fit)[[i]], ")*", "x^", i - 1, sep = "")
                f <- paste(f, temp, sep = "+")
            }
        }

        x <- seq(xMin, xMax, length.out = 100)
        predY <- eval(parse(text = f))

        if (line == FALSE) {
            return(predY)
        }

        if (line) {
            lines(x, predY, lwd = lwd,...)
        }
    }


    panel.plotScatter <- function(x, y,...) { 

        cexPoints = 1.3
        cexXAxis = 1.3
        cexYAxis = 1.3 
        lwd = 2

        par(new = TRUE)

        # plot the points
        points(x, y, pch=20,...)

        # fit a regression line to the scatterplot 
        d <- data.frame(xx = x, yy = y)
        d <- na.omit(d)

        fit <- lm(yy ~ poly(xx, 1, raw = TRUE), d)

        xlow <- min((min(x) - 0.1 * min(x)), min(pretty(x)))
        xhigh <- max((max(x) + 0.1 * max(x)), max(pretty(x)))
        xticks <- pretty(c(xlow, xhigh))

        .poly.pred(fit, line = TRUE, xMin = xticks[1], xMax = xticks[length(xticks)], lwd = lwd,...)

        par(las = 1)

        # Now print the r-value and its confidence interval (we need to change the coordinate system first!)
        usr <- par("usr"); on.exit(par(usr))
        par(usr = c(0, 1, 0, 1))

        confidenceInterval = 0.95
        r <- abs(cor(x, y, use = "pairwise.complete.obs", method="spearman"))
        r <- formatC(round(r, 3), format = "f", digits = 3)
        txt <-  paste(bquote(r)," = ", r, sep="")
        text(0.5, 0.6, txt, cex = 1.2)

        # 95% CI for r
        tests <- c()
        ctest <- cor.test(x, y, method = tests, conf.level = confidenceInterval)
        CIlow <- formatC(round(ctest$conf.int[1], 3), format = "f", digits = 3)
        CIhigh <- formatC(round(ctest$conf.int[2], 3), format = "f", digits = 3)
        txt <-  paste(100 * confidenceInterval, "% CI: [", CIlow, ", ", CIhigh, "]", sep = "")
        text(0.5, 0.45, txt, cex = 1.2)
    }


    # contour plot for the upper panel
    panel.contour <- function(x, y, digits=2, prefix="", cex.cor,...) {

        data.interp <- matrix()
        tryd <- try(data.interp <- interp(x, y, chains$matrix[], linear=T, extrap=F, duplicate="strip"), silent=TRUE)
        if(class(tryd) != "try-error") {

            #filled.contour(data.interp)
            try(image(data.interp, col = heat.colors(64),add=TRUE,...)  , silent = TRUE)
        } else {

        }
    }

    
    # parameter values and confidence intervals
    panel.parameterValue <- function(x, y,...) { 
    }


    # diagonal panel showing the density distribution
    panel.diag.dens <- function(x, varname, ...) { 

        # get some graphical parameter settings, and reset them on exit
        usr <- par("usr"); on.exit(par(usr))
        par(usr = c(usr[1:2], 0, 1.5) )

        tryd <- try( d <- density(x, na.rm=TRUE, bw="nrd", adjust=1.2), silent=TRUE)
        if(class(tryd) != "try-error") {
            d$y <- d$y/max(d$y)
            lines(d)  
            try(boxplot(x, at=0.2, boxwex=0.3, horizontal=TRUE, add=TRUE, xaxt="n",...), silent = TRUE)
        }
    }

    # diagonal panel showing the histogram
    panel.diag.hist <- function (x,...) {
        par(new = TRUE)
        hist(x,
             col = "light gray",
             probability = TRUE,
             axes = FALSE,
             main = "",
             breaks = "Scott",...)
        lines(density(x, na.rm=TRUE,...),
              col = "red",
              lwd = 1,...)
        #lines(f, col="blue", lwd=1, lty=1) how to add gaussian normal overlay?
        rug(x,...)
    }


    # Labels for the diagonal
    panel.diag.text <- function(x, y, opt_lbls, cex, font,...) {
        txt <- parse(text = opt_labels[parent.frame(1)$i])
        text(0.2, 0.85, txt, cex=1.5)
    }


    
    # xpecifying limits (now as arrays...)
    # dims 1-2: panel
    # dim 3: lower und upper limit
    numPairs <- length(opt_columns)
    my.xlim <- array(0, dim=c(numPairs,numPairs,2))

    for (i in seq(1,numPairs)) {
        my.xlim[i,,1] <- as.numeric(opt_limits[[1]][2*i-1])
        my.xlim[i,,2] <- as.numeric(opt_limits[[1]][2*i])
    }


    # Now plot the data.
    pdffile <- paste(outputFileNameBase, "_posterior_values.pdf", sep="", collapse = NULL)
    pdf(file=pdffile, onefile=FALSE, width=7.5, height=7.5)
    my.pairs(chains$matrix, lower.panel=panel.points, upper.panel=panel.correl, diag.panel=panel.diag.hist, text.panel=panel.diag.text, gap=0, xlim=my.xlim)


    pdffile <- paste(outputFileNameBase, "_posterior_correlations.pdf", sep="", collapse = NULL)
    pdf(file=pdffile, onefile=FALSE, width=7.5, height=7.5)
    my.pairs(chains$matrix, lower.panel=panel.plotScatter, upper.panel=NULL, diag.panel=panel.diag.dens, text.panel=panel.diag.text, gap=0, xlim=my.xlim)
    


}, error = function(err) {
    cat(paste("****Could not plot parameter distribution:  ",err))
}, warning = function(war) {
    cat(paste("****Could not plot parameter distribution:  ",war))
})




cat (sprintf("Printing posterior distribution densities for %s\n", opt$input))
numCols = 2
numRows = ceiling(ncol(chains$matrix[])/numCols)


result = tryCatch({
    pdffile <- paste(outputFileNameBase, "_posterior_distribution.pdf", sep="", collapse = NULL)
    pdf(file=pdffile, onefile=FALSE, width=6.0, height=6.0)

    par(mfrow = c(numRows, numCols))
    par(mar=c(4,2,1,1), cex.lab=2)

    for (i in seq(1,ncol(chains$matrix[]))) {

        if (bw.nrd(chains$matrix[,i]) > 0) {
            dens <- try(density(chains$matrix[,i], kernel="gaussian", bw="nrd0", n=256), silent=TRUE)

            box <-  boxplot.stats(chains$matrix[,i])
            #q <- quantile(chains$matrix[,i],probs=c(0.05,0.95), type=5)
            #cat(sprintf("     %s : 5 num summary [%f,%f,%f,%f,%f] [%f,%f]\n", opt_labels[i], box$stats[1], box$stats[2], box$stats[3], box$stats[4], box$stats[5], q[1], q[2]) )

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

            cat(sprintf("      %s = %f [95%% CI = %f,%f]\n", parse(text = opt_labels[parent.frame(1)$i]), box$stats[3], limits[1], limits[2]) )
        }
    }

}, error = function(err) {
    cat(paste("****Could not plot parameter distribution:  ",err))
}, warning = function(war) {
    cat(paste("****Could not plot parameter distribution:  ",war))
})



result = tryCatch({
    pdffile <- paste(outputFileNameBase, "_posterior_histogram.pdf", sep="", collapse = NULL)
    pdf(file=pdffile, onefile=FALSE, width=6.0, height=6.0)

    par(mfrow = c(numRows, numCols))
    par(mar=c(4,2,1,1), cex.lab=2)

    for (i in seq(1,ncol(chains$matrix[]))) {
        dens <- try(density(chains$matrix[,i], kernel="gaussian", bw="nrd0", n=256), silent=TRUE)

        # create the label for each plot adding the median values for each.
        lb <- opt_labels[i]
        txt <- parse(text=lb)

        # if opt_limits is not null then get xmin xmax from here....
        if (length(opt_limits[[1]]) > 1) {
            min_x <- as.numeric(opt_limits[[1]][2*i-1])
            max_x <- as.numeric(opt_limits[[1]][2*i])
            hist(chains$matrix[,i], freq = FALSE, xlab=txt, ylab="", main="", yaxt="n", xlim=c(min_x,max_x), cex.lab=1.2)
        } else {
            hist(chains$matrix[,i], freq = FALSE, xlab=txt, ylab="", main = "")
        }
        lines(dens)
    }

}, error = function(err) {
    cat(paste("****Could not plot parameter distribution:  ",err))
}, warning = function(war) {
    cat(paste("****Could not plot parameter distribution:  ",war))
})


