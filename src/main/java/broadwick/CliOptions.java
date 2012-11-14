package broadwick;

import org.apache.commons.cli2.CommandLine;
import org.apache.commons.cli2.Group;
import org.apache.commons.cli2.Option;
import org.apache.commons.cli2.builder.ArgumentBuilder;
import org.apache.commons.cli2.builder.DefaultOptionBuilder;
import org.apache.commons.cli2.builder.GroupBuilder;
import org.apache.commons.cli2.commandline.Parser;
import org.apache.commons.cli2.util.HelpFormatter;

/**
 * Construct and read command line arguments. This class contains methods for extracting some of the main
 * options such as configuration file etc.
 */
public class CliOptions {

    private Group options;
    private static final int LINEWIDTH = 120;
    private static final String SPACE = " ";
    private CommandLine cmdLine;
    private Option configFileOpt;


    /**
     * Construct and provide GNU-compatible Options. Read the command line extracting the arguments, this
     * additionally displays the help message if the command line is empty.
     *
     * @param args the command line arguments.
     */
    public CliOptions(final String[] args) {
        buildCommandLineArguments();

        final Parser parser = new Parser();
        parser.setGroup(options);
        final HelpFormatter hf = new HelpFormatter(SPACE, SPACE, SPACE, LINEWIDTH);
        parser.setHelpFormatter(hf);
        parser.setHelpTrigger("--help");
        cmdLine = parser.parseAndHelp(args);

        if (cmdLine == null) {
            hf.printHeader();
            throw new BroadwickException("Empty command line.");
        }

        validateCommandLineArguments();
    }

    /**
     * Construct and provide GNU-compatible Options.
     */
    private void buildCommandLineArguments() {
        final DefaultOptionBuilder oBuilder = new DefaultOptionBuilder();
        final GroupBuilder gbuilder = new GroupBuilder();
        final ArgumentBuilder abuilder = new ArgumentBuilder();

        final Option helpOpt = oBuilder.withLongName("help").withShortName("h").withRequired(false)
                .withDescription("Print this message").create();
        final Option guiOpt = oBuilder.withLongName("gui").withShortName("x").withRequired(false)
                .withDescription("Use a gui to configure and run a model").create();
        configFileOpt = oBuilder.withLongName("configFile").withShortName("c").withRequired(false)
                .withArgument(abuilder.withName("input").withMinimum(1).withMaximum(1).create())
                .withDescription("Use given configuration file").create();
        options = gbuilder.withName("Options")
                .withOption(helpOpt)
                .withOption(configFileOpt)
                .withOption(guiOpt)
                .create();
    }

    /**
     * Perform validation the command line arguments.
     */
    private void validateCommandLineArguments() {
        // nothing to validate (yet)
    }

    /**
     * Get the name of the configuration file specified on the command line.
     *
     * @return the name of the configuration file specified by either the -g or -c options. If neither
     *         option is specified then we return an empty string.
     */
    public final String getConfigurationFileName() {
        if (cmdLine.hasOption(configFileOpt)) {
            return (String) cmdLine.getValue(configFileOpt);
        } else {
            return "";
        }
    }

}
