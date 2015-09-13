package org.hartorn.htf.db.generator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.FileUtils;
import org.xml.sax.SAXException;

import freemarker.ext.dom.NodeModel;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.TemplateSequenceModel;

/**
 * Generate the classes according to the templates, the xml database model and the database mapping.
 *
 * @author Hartorn
 *
 */
@Mojo(name = "generate", requiresProject = true)
public class HtfDbGenerator extends AbstractMojo {

    private static final String INTERN_FILE_PREFIX = "intern:";
    private static final String XML_MODEL_NAME = "model";
    private static final String FTL_TABLE_NAME = "table";
    private static final String DB_MAPPING_NAME = "dbMapping";
    private static final String BD_MAPPING_PATH_PATTERN = "{0}{1}{2}.properties";
    /**
     * The base package for the generated files.
     */
    @Parameter(property = "generate.javagenPackage", required = true)
    private String javagenPackage;
    /**
     * The name of the database we are generating for.
     */
    @Parameter(property = "generate.dbName", required = true)
    private String dbName;
    /**
     * The xml file representing the database.
     */
    @Parameter(property = "generate.xmlModel", required = true)
    private String xmlModel;
    /**
     * The path for src folder.
     */
    @Parameter(property = "generate.src", required = true, defaultValue = "${project.basedir}/src")
    private String srcFolder;
    /**
     * The path for the templates folder to use for the generation.
     */
    @Parameter(property = "generate.templateFolder", required = true, defaultValue = HtfDbGenerator.INTERN_FILE_PREFIX + "/templates")
    private String templateFolder;
    /**
     * The name of the template file to use for the generation.
     */
    @Parameter(property = "generate.templateNames")
    private List<String> templateNames;

    /**
     * The path for the database mapping folder.
     */
    @Parameter(property = "generate.mappingFolder", required = true, defaultValue = HtfDbGenerator.INTERN_FILE_PREFIX + "/dbMapping")
    private String dbMappingFolder;

    /**
     * The character encoding for the files.
     */
    @Parameter(property = "generate.fileEncoding", required = true, defaultValue = "UTF-8")
    private String charset;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        this.getLog().info("Starting Htf Database file generation");
        this.debugProperties();

        // this.getTemplateFolder();
        //
        final Configuration config = this.initFtlConfiguration();
        //
        final List<Template> templates = this.getTemplates(config);
        final Map<String, Object> dataModel = new HashMap<String, Object>();
        this.loadXmlModel(dataModel);
        this.loadBdMapping(dataModel);

        final NodeModel model = (NodeModel) dataModel.get(HtfDbGenerator.XML_MODEL_NAME);
        try {
            // if ("changeSet".equals(model.getNodeName())) {
            this.getLog().info("Model : got changeSet");
            final TemplateSequenceModel tables = model.getChildNodes();
            for (int i = 0; i < tables.size(); i++) {
                this.getLog().info("Model : index " + i);

                final NodeModel node = (NodeModel) tables.get(i);
                dataModel.put(HtfDbGenerator.FTL_TABLE_NAME, node);
                for (final Template template : templates) {
                    final String filePath = this.srcFolder + this.javagenPackage.replaceAll("\\.", "/") + UUID.randomUUID().toString();
                    // + node.getNode().getAttributes().getNamedItem("tableName").getTextContent();

                    this.getLog().info("Path :" + filePath);
                    final FileWriter writer = new FileWriter(filePath);
                    template.process(dataModel, writer);
                    writer.close();
                }
            }
            // }
        } catch (final IOException | TemplateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // final template.pro
        // TODO Auto-generated method stub

        this.getLog().info("Ending Htf Database file generation");
    }

    private void debugProperties() {
        final Log logger = this.getLog();
        logger.debug("Logging parameters");
        logger.debug("javagenPackage:" + this.javagenPackage);
        logger.debug("dbName:" + this.dbName);
        logger.debug("xmlModel:" + this.xmlModel);
        logger.debug("srcFolder:" + this.srcFolder);
        logger.debug("templateFolder:" + this.templateFolder);
        logger.debug("templateName:" + this.templateNames);
        logger.debug("dbMappingFolder:" + this.dbMappingFolder);
        logger.debug("End of logging parameters");

    }

    private File getDbPropertiesFile() throws MojoExecutionException {
        String filePath = MessageFormat.format(HtfDbGenerator.BD_MAPPING_PATH_PATTERN, this.dbMappingFolder, "/", this.dbName);
        final File propertiesFile;
        if (filePath.startsWith(HtfDbGenerator.INTERN_FILE_PREFIX)) {
            filePath = filePath.replaceFirst(HtfDbGenerator.INTERN_FILE_PREFIX, "");

            Path propertiesPath;
            try {
                propertiesPath = Files.createTempFile(this.dbName, ".properties");
            } catch (final IOException e) {
                throw new MojoExecutionException("Cannot create temp file", e);
            }

            try (final InputStream properties = this.getClass().getResourceAsStream(filePath);) {
                Files.copy(properties, propertiesPath, StandardCopyOption.REPLACE_EXISTING);
            } catch (final IOException e) {
                throw new MojoExecutionException("Cannot copy properties file", e);
            }
            propertiesFile = propertiesPath.toFile();
        } else {
            propertiesFile = new File(filePath);
        }

        return propertiesFile;
    }

    private List<String> getDefaultTemplatesList() {
        final List<String> templatesNames = new ArrayList<String>();
        templatesNames.add("classTemplateAbstract.ftl");
        templatesNames.add("classTemplateImpl.ftl");
        return templatesNames;
    }

    private File getFile(final String filePath) throws MojoExecutionException {
        File file;

        this.getLog().debug("Getting file:" + filePath);
        if (filePath.startsWith(HtfDbGenerator.INTERN_FILE_PREFIX)) {
            final URL fileUrl = this.getClass().getResource(filePath.replaceFirst(HtfDbGenerator.INTERN_FILE_PREFIX, ""));

            try {
                file = new File(fileUrl.toURI());
                this.getLog().debug("Getting file from jar, with path:" + fileUrl.toURI());
            } catch (final URISyntaxException e) {
                file = new File(fileUrl.getPath());
                throw new MojoExecutionException("Cannot get file from jar :" + filePath, e);
            }
        } else {
            file = new File(filePath);
        }
        if ((file == null) || !file.exists()) {
            throw new MojoExecutionException("The following file cannot exist, or is not readable :" + filePath);
        }
        return file;
    }

    private File getTemplateFolder() throws MojoExecutionException {
        final File templateFolder;
        String folderPrefix = this.templateFolder;
        if (folderPrefix.endsWith(File.separator)) {
            folderPrefix = folderPrefix.substring(0, folderPrefix.length() - 1);
        }

        // final URL fileUrl = this.getClass().getResource(filePath.replaceFirst(HtfDbGenerator.INTERN_FILE_PREFIX, ""));
        if (this.templateFolder.startsWith(HtfDbGenerator.INTERN_FILE_PREFIX)) {
            folderPrefix = this.templateFolder.replaceFirst(HtfDbGenerator.INTERN_FILE_PREFIX, "");

            Path templatePath;
            try {
                templatePath = Files.createTempDirectory("templates");
            } catch (final IOException e) {
                throw new MojoExecutionException("Error creating temp folder", e);
            }
            templateFolder = templatePath.toFile();
            templateFolder.deleteOnExit();

            for (final String fileName : this.getTemplatesNames()) {
                try {
                    final File newFile = new File(templateFolder.getAbsolutePath() + "/" + fileName);
                    FileUtils.copyURLToFile(this.getClass().getResource(folderPrefix + "/" + fileName), newFile);
                    // FileUtils.copyFileToDirectory(new File(.toURI()), templateFolder);
                } catch (final IOException e) {
                    throw new MojoExecutionException("Error copying template to temp folder", e);
                }
            }
        } else {
            templateFolder = new File(folderPrefix + File.separator);
        }
        return templateFolder;
    }

    private List<Template> getTemplates(final Configuration config) throws MojoExecutionException {
        final List<Template> templates = new ArrayList<Template>();

        for (final String templateName : this.getTemplatesNames()) {
            try {
                final Template template = config.getTemplate(templateName);
                templates.add(template);
            } catch (final IOException e) {
                throw new MojoExecutionException("Error while getting template", e);
            }

        }
        return templates;
    }

    private List<String> getTemplatesNames() {
        if ((this.templateNames != null) && !this.templateNames.isEmpty()) {
            return this.templateNames;
        }
        return this.getDefaultTemplatesList();
    }

    private Configuration initFtlConfiguration() throws MojoExecutionException {
        final Configuration config = new Configuration(Configuration.VERSION_2_3_22);
        config.setDefaultEncoding(this.charset);
        config.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        try {
            config.setDirectoryForTemplateLoading(this.getTemplateFolder());
        } catch (final IOException e) {
            throw new MojoExecutionException("Error creating FTL configuration (problem with template folder ?)", e);
        }
        return config;
    }

    private void loadBdMapping(final Map<String, Object> dataModel) throws MojoExecutionException {
        final File propFile = this.getDbPropertiesFile();
        final Properties dbMapping = new Properties();
        try (final FileInputStream input = new FileInputStream(propFile);) {
            dbMapping.load(input);
        } catch (final IOException e) {
            throw new MojoExecutionException("Error loading db mapping", e);
        }
        dataModel.put(HtfDbGenerator.DB_MAPPING_NAME, dbMapping);
    }

    private void loadXmlModel(final Map<String, Object> dataModel) throws MojoExecutionException {
        final NodeModel model;
        try {
            model = NodeModel.parse(this.getFile(this.xmlModel));
        } catch (final SAXException | IOException | ParserConfigurationException e) {
            throw new MojoExecutionException("Error while getting template", e);
        }
        dataModel.put(HtfDbGenerator.XML_MODEL_NAME, model);
    }

}
