
datafile <- "../results/broadwick.stochasticinf.ObsGenerator.dat"
output <- "../broadwick.stochasticinf.ObsGenerator.png"


timeSeries <- read.table(datafile, sep=",", stringsAsFactors=F)
t <- as.numeric(timeSeries$V1)
s <- as.numeric(timeSeries$V2)
e1 <- as.numeric(timeSeries$V3)
e2 <- as.numeric(timeSeries$V4)
i <- as.numeric(timeSeries$V5)

png(filename=output, width=960, height=960, bg="white")

par(cex=1.7)

title = ""
xlabel = "Time"
ylabel = "Population"
xrange <- range(0, t)
yrange <- range(0, max(s,e1,e2,i)) 
linetype <- c(1,1,1,1)
colours <- c("orange", "green", "blue", "red")

plot (t,s, lwd=2.5, col=colours[1], lty=linetype[1], type="l", xlim=xrange, ylim=yrange, main=title, xlab=xlabel, ylab=ylabel)
lines (t,e1, lwd=2.5, col=colours[2], lty=linetype[2], type="l")
lines (t,e2, lwd=2.5, col=colours[3], lty=linetype[3], type="l")
lines (t,i, lwd=2.5, col=colours[4], lty=linetype[4], type="l")

lgnd <- c("S", expression("E"[1]), expression("E"[2]), "I") 
legend (0.9*xrange[2],  0.95*yrange[2], lgnd, cex=0.8, col=colours, lty=linetype) 





