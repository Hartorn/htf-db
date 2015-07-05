package org.hartorn.htf.db.generator;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Generate the classes according to the templates, the xml database model and the database mapping.
 *
 * @author Hartorn
 *
 */
@Mojo(name = "generate", requiresProject = true)
public class HtfDbGenerator extends AbstractMojo {

    /**
     * The base package for the generated files.
     */
    @Parameter(property = "generate.javagen-package", required = true)
    private String javagenPackage;
    /**
     * The name of the database we are generating for.
     */
    @Parameter(property = "generate.db-name", required = true)
    private String dbName;
    /**
     * The xml file representing the database.
     */
    @Parameter(property = "generate.xmlmodel", required = true)
    private String xmlModel;
    /**
     * The path for src folder.
     */
    @Parameter(property = "generate.src", required = true, defaultValue = "${basedir}/src")
    private String srcFolder;
    /**
     * The path for the template to use for the generation.
     */
    @Parameter(property = "generate.template-path", defaultValue = "intern:/templates/classTemplate.ftl")
    private String templatePath;
    /**
     * The path for the database mapping folder.
     */
    @Parameter(property = "generate.mapping-folder", defaultValue = "intern:/dbMapping")
    private String dbMappingFolder;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        this.getLog().info("Starting Htf Database file generation");
        this.debugProperties();
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
        logger.debug("templatePath:" + this.templatePath);
        logger.debug("templatePath:" + this.templatePath);
        logger.debug("End of logging parameters");

    }
}
