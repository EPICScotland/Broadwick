package broadwick;

import broadwick.config.ConfigValidationErrors;
import broadwick.config.ConfigValidator;
import broadwick.config.generated.Logs;
import broadwick.config.generated.Models;
import broadwick.config.generated.Project;
import broadwick.data.DataReader;
import broadwick.data.Lookup;
import broadwick.model.Model;
import com.google.common.base.Throwables;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Broadwick: Project for Scientific Computing. The Broadwick framework allows for rapid epidemic modelling.
 */
public final class Broadwick {

    /**
     * Create the Broadwick project to read and verify the configuration files and initialise the project.
     * @param args the command line arguments supplied to the project.
     */
    public Broadwick(final String[] args) {

        final LoggingFacade logFacade = new LoggingFacade();
        log = logFacade.getRootLogger();
        try {
            final CliOptions cli = new CliOptions(args);
            readConfigFile(logFacade, cli.getConfigurationFileName());
        } catch (BroadwickException ex) {
            log.error("{}\nSomething went wrong starting project. See the error messages.", ex.getLocalizedMessage());
            log.trace(Throwables.getStackTraceAsString(ex));
        }
    }

    /**
     * Read the configuration file from the configuration file.
     * @param logFacade  the LoggingFacade object used to log any messages.
     * @param configFile the name of the configuration file.
     */
    private void readConfigFile(final LoggingFacade logFacade, final String configFile) {
        if (!configFile.isEmpty()) {
            final File cfg = new File(configFile);
            if (!cfg.exists()) {
                throw new BroadwickException("Configuration file [" + configFile + "] does not exist.");
            }
            try {
                // read the configuration file
                final JAXBContext jaxbContext = JAXBContext.newInstance(Constants.GENERATED_CONFIG_CLASSES_DIR);
                final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
                project = (Project) unmarshaller.unmarshal(cfg);

                // save the config file as a string for later use
                final StringWriter writer = new StringWriter();
                jaxbContext.createMarshaller().marshal(project, writer);
                configXml = writer.toString();

                // Validate the configuration file
                final ConfigValidator validator = new ConfigValidator(project);
                final ConfigValidationErrors validationErrors = validator.validate();

                // now set up the logger as defined in the config file, we want to do this
                // BEFORE writing the results of validation
                final Logs.File file = project.getLogs().getFile();
                if (file != null) {
                    logFacade.addFileLogger(file.getName(), file.getLevel(), file.getPattern());
                }
                final Logs.Console console = project.getLogs().getConsole();
                if (console != null) {
                    logFacade.addConsoleLogger(console.getLevel(), console.getPattern());
                }

                // Log any validation errors.
                if (validationErrors.getNumErrors() > 0) {
                    log.error("Invalid configuration file.\n{}Correct any errors before continuing.", validationErrors.getValidationErrors());
                    project = validator.getValidatedProject();
                }

            } catch (JAXBException ex) {
                log.error("Could not read configuration file. {}", ex.getLocalizedMessage());
                log.trace(com.google.common.base.Throwables.getStackTraceAsString(ex));
            }
        } else {
            throw new BroadwickException("No configuration file specified");
        }
    }

    /**
     * Run the Broadwick framework.
     */
    public void run() {
        if (project != null) {
            // initialise the data, by reading the data files and/or the database.

            log.info("Running broadwick {}", BroadwickVersion.getVersionAndTimeStamp());

            try (DataReader dr = new DataReader(project.getData())) {
                final Map<String, Model> registeredModels = registerModels(project, dr.getLookup());
                log.info("Running broadwick for the following models {}", registeredModels.keySet());

                // Run the models, each on a separate thread.
                // TODO in a single-threaded grid environment we cannot do this - need to think again here....
                final int poolSize = registeredModels.size();
                if (poolSize > 0) {
                    final ExecutorService es = Executors.newFixedThreadPool(poolSize);

                    //final StopWatch sw = new StopWatch();
                    for (final Entry<String, Model> entry : registeredModels.entrySet()) {
                        es.submit(new Runnable() {
                            @Override
                            public void run() {
                                final String modelName = entry.getKey();
                                try {
                                    log.info("Running {} [{}]", modelName, entry.getValue().getClass().getCanonicalName());
                                    entry.getValue().init();
                                    entry.getValue().run();
                                    entry.getValue().finalise();
                                } catch (Exception ex) {
                                    log.error("Failed to create instance of model {}", modelName);
                                    log.error("{}", Throwables.getStackTraceAsString(ex));
                                }
                            }

                        });
                    }
                    es.shutdown();
                    while (!es.isTerminated()) {
                        es.awaitTermination(1, TimeUnit.SECONDS);
                    }
                    //sw.stop();
                    //log.trace("Finished {} simulations in {}.", maxSimulations, sw);

                    log.info("Simulation complete.");
                }
            } catch (InterruptedException | BroadwickException ex) {
                log.trace("{}", Throwables.getStackTraceAsString(ex));
                log.error("Something went wrong. See previous messages for details.");
            }
        }
    }

    /**
     * Create and register the models internally. If there was a problem registering the models an empty cache is
     * returned.
     * @param project the unmarshalled configuration file.
     * @param lookup  the Lookuup object that allows the model to access the data specified in the data files.
     * @return the registered models.
     */
    private Map<String, Model> registerModels(final Project project, final Lookup lookup) {
        final Map<String, Model> registeredModels = new HashMap<>();
        try {

            for (Models.Model model : project.getModels().getModel()) {
                // Create and register the new model object that we will be running later.
                final Model newInstance = Model.class.cast(Class.forName(model.getClassname()).newInstance());
                newInstance.setModelConfiguration(getModelsConfiguration(model.getId(), getAllModelConfigurations()));
                newInstance.setModelDataLookup(lookup);
                registeredModels.put(model.getId(), newInstance);
            }
        } catch (ParserConfigurationException | SAXException | IOException | ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException ex) {
            log.error("Could not create model ; {}", ex.getLocalizedMessage());
            registeredModels.clear();
        }

        return registeredModels;
    }

    /**
     * Get a collection of XML elements one for each <model> section.
     * @return a collection of XML elements of each <model>.
     * @throws ParserConfigurationException if the nodes for the configured models cannot be found.
     * @throws SAXException                 if the nodes for the configured models cannot be found.
     * @throws IOException                  if the nodes for the configured models cannot be found.
     */
    private NodeList getAllModelConfigurations() throws ParserConfigurationException, SAXException, IOException {
        final DocumentBuilderFactory xmlFactory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder docBuilder = xmlFactory.newDocumentBuilder();
        final Document xmlDoc = docBuilder.parse(new InputSource(new StringReader(configXml)));
        return xmlDoc.getElementsByTagName("model");
    }

    /**
     * Get the XML string of the model with the given id from a list of configured models.
     * @param id     the id of the model to be found.
     * @param models a list of XML <model> nodes.
     * @return the XML string for the model.
     */
    private String getModelsConfiguration(final String id, final NodeList models) {
        try {
            for (int i = 0; i < models.getLength(); i++) {
                final NamedNodeMap attributes = models.item(i).getAttributes();
                final String nodeId = attributes.getNamedItem("id").getNodeValue();

                if (id.equals(nodeId)) {
                    final TransformerFactory transFactory = TransformerFactory.newInstance();
                    final Transformer transformer = transFactory.newTransformer();
                    final StringWriter buffer = new StringWriter();
                    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
                    transformer.transform(new DOMSource(models.item(i)),
                                          new StreamResult(buffer));
                    return buffer.toString();
                }
            }
        } catch (TransformerException ex) {
            log.error("Could not get the configuration for the model [{}]. {}", id, ex.getLocalizedMessage());
        }
        return "";
    }

    /**
     * Invocation point.
     * @param args the command line arguments passed to Broadwick.
     */
    public static void main(final String[] args) {

        final Broadwick broadwick = new Broadwick(args);
        broadwick.run();
    }

    private Project project;
    private Logger log;
    private String configXml;
}
