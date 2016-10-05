suppressPackageStartupMessages(library("optparse"))
suppressPackageStartupMessages(library("zoo"))
suppressPackageStartupMessages(library("reshape2"))
suppressPackageStartupMessages(library("ggplot2"))

option_list <- list(
                    make_option(c("-i", "--input"), default="herd.dat", help = "Input file containing the parameters to plot [default \"%default\"]"),
                    make_option(c("-o", "--output"), default="herd.png", help = "Output file (will be a pdf!) default \"%default\"]"),
                    make_option(c("-y", "--ylabel"), dest="ylabel", default="", help = "Labels of y axis [default \"%default\"]")
                    )



opt <- parse_args(OptionParser(option_list=option_list))

opt_ylabel <- strsplit(opt$ylabel, split = ",")[[1]]
outputFileNameBase <- sub('(\\.)[^\\.]*$', '', opt$output)
outputExtension <- ".pdf"


series <- new.env()
series$zoo <- zoo()
series$size <- 0

# look at each time series file and extract each time series from it. Each line of each file is the count at each time point.
process_time_series <- function(datafile) {

    data <- try(read.csv(datafile, sep=opt$separator,  comment.char = "#", header=FALSE, stringsAsFactors = FALSE), silent=TRUE)
    if (class(data) != "try-error") {

        if (nrow(data) > 0 ) {
            cat (sprintf("                                                      Reading %s (%d rows)\n", datafile, nrow(data)))
            for (i in 1:nrow(data)) {
                line <- as.numeric(scan(text=data$V1[i], sep=","))
                series$zoo <- merge(series$zoo, zoo(c(line), 1:length(line)))
                series$size <- series$size + 1
            }
        } else {
            cat (sprintf("                                                     ignoring %s (%d rows)\n", datafile, nrow(data)))
        }

    }
}


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

invisible(mapply(process_time_series, files))

# Put simple names on each times series
names(series$zoo) <- paste ("TS", 1:series$size, sep="")

mx <- c(length=nrow(series$zoo))
mn <- c(length=nrow(series$zoo))
md <- c(length=nrow(series$zoo))

for (i in 1:nrow(series$zoo)) {

    if (!all(is.na(series$zoo[i]))) {
        mx[i] <- max(series$zoo[i],na.rm = TRUE)
        md[i] <- mean(series$zoo[i],na.rm = TRUE)
        mn[i] <- min(series$zoo[i],na.rm = TRUE)
    }
}


df <- data.frame(x = 1:length(md),
                 F = md,
                 L = mn,
                 U = mx)

pdf(file=opt$output, onefile=FALSE, width=7.5, height=7.5)
plot(df$x, df$F, ylim = c(min(mn),max(mx)), type = "l", xlab="time", ylab=opt_ylabel)
# make polygon where coordinates start with lower limit and 
# then upper limit in reverse order
polygon(c(df$x,rev(df$x)),c(df$L,rev(df$U)),col = "grey75", border = FALSE)
lines(df$x, df$F, lwd = 2)
#add red lines on borders of polygon
lines(df$x, df$U, col="red",lty=2)
lines(df$x, df$L, col="red",lty=2)



print (opt$separator)


