package broadwick;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.filter.ThresholdFilter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.encoder.Encoder;
import ch.qos.logback.core.filter.Filter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;

/**
 * Facade class to the underlying logger to allow the loggers to be configured programmatically.
 */
@Slf4j
public final class LoggingFacade {

    /**
     * Create the logging facade, by default adding a console logger to output logging info to the screen (console). A
     * level of 'error' is configured by default, once the configuration file is read the logging level can be changed.
     */
    public LoggingFacade() {

        try {
            // assume SLF4J is bound to logback in the current environment
            loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
            loggerContext.start();

            rootLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
            rootLogger.detachAndStopAllAppenders();
            rootLogger.setLevel(Level.ALL);

            // Create the layout pattern for the appenders.
            addConsoleLogger("error", logFormatThreadMsg);

        } catch (ClassCastException cce) {
            rootLogger.error("Apparently SLF4J is not backed by Logback. This is a requirement, thus an internal fault.");
        }
    }

    /**
     * Add a console logger to the list of loggers of the project. This will replace all the console loggers already
     * added using this method.
     * @param level   the logging level to be applied.
     * @param pattern the pattern for the logger.
     */
    public void addConsoleLogger(final String level, final String pattern) {

        final String consoleName = "Console";

        // remove the old console appender - it will be called "Console" if it was added using this method.
        rootLogger.detachAppender(consoleName);

        final ConsoleAppender<ILoggingEvent> appender = new ConsoleAppender<>();
        appender.setName(consoleName);
        appender.setEncoder(createPatternLayoutEncoder(pattern));
        appender.addFilter(createThresholdFilter(level));

        appender.setContext(loggerContext);
        appender.start();
        rootLogger.addAppender((Appender<ILoggingEvent>) appender);
    }

    /**
     * Add a file logger to the list of loggers of the project.
     * @param file      the name of the file to which the logging messages will be added.
     * @param level     the logging level to be applied.
     * @param pattern   the pattern for the logger.
     * @param overwrite if true, the contents of any previous log are overwritten. If false, the logs are appended.
     */
    public void addFileLogger(final String file, final String level, final String pattern, final Boolean overwrite) {

        // Delete the old log file.
        try {
            if (overwrite != null && overwrite.booleanValue()) {
                Files.delete(Paths.get(file));
            }
        } catch (IOException ex) {
            log.error("Could not delete ol log file; {}", ex.getLocalizedMessage());
        }

        final FileAppender<ILoggingEvent> appender = new FileAppender<>();
        appender.setFile(file);
        appender.setEncoder(createPatternLayoutEncoder(pattern));
        appender.addFilter(createThresholdFilter(level));

        appender.setContext(loggerContext);
        appender.start();
        rootLogger.addAppender((Appender<ILoggingEvent>) appender);
    }

    /**
     * Create a pattern layout encoder for a given pattern.
     * @param pattern the pattern to apply.
     * @return the created encoder.
     */
    private Encoder<ILoggingEvent> createPatternLayoutEncoder(final String pattern) {
        // Set a default pattern if the supplied pattern is empty
        final String patt = (pattern == null) ? logFormatThreadMsg : pattern;

        final PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(loggerContext);
        encoder.setPattern(patt);
        encoder.start();
        return (Encoder<ILoggingEvent>) encoder;
    }

    /**
     * Create a thresholdFilter for a given level.
     * @param level the level to set.
     * @return the created filter.
     */
    private Filter<ILoggingEvent> createThresholdFilter(final String level) {
        // Set a default level if the supplied level is empty
        final String lev = (level == null) ? "info" : level;

        final ThresholdFilter filter = new ThresholdFilter();
        filter.setLevel(lev);
        filter.start();
        return (Filter<ILoggingEvent>) filter;
    }
    @Getter
    private Logger rootLogger;
    private LoggerContext loggerContext;
    private String logFormatThreadMsg = "[%thread] %msg\n";
}
