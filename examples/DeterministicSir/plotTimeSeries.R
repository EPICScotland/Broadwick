
datafile <- "broadwick.deterministicsir.dat"
output <- "broadwick.deterministicsir.png"


timeSeries <- read.table(datafile, sep=",", stringsAsFactors=F)
t <- as.numeric(timeSeries$V1)
s <- as.numeric(timeSeries$V2)
i <- as.numeric(timeSeries$V3)
r <- as.numeric(timeSeries$V4)

png(filename=output, width=960, height=960, bg="white")

title = ""
xlabel = "Time"
ylabel = ""
xrange <- range(0, t)
yrange <- range(0, max(s,i,r)) 
linetype <- c(1,1,1) 
colours <- c("orange", "red", "blue")

plot (t,s, lwd=2.5, col=colours[1], lty=linetype[1], type="l", xlim=xrange, ylim=yrange, main=title, xlab=xlabel, ylab=ylabel)
lines (t,i, lwd=2.5, col=colours[2], lty=linetype[2], type="l")
lines (t,r, lwd=2.5, col=colours[3], lty=linetype[3], type="l")

lgnd <- c("S", "I", "R") 
legend ("topright",  yrange[2], lgnd, cex=0.8, col=colours, lty=linetype) 





