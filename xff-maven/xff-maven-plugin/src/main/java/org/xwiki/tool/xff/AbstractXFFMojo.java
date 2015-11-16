package org.xwiki.tool.xff;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

abstract class AbstractXFFMojo extends AbstractMojo
{
    /**
     * Default encoding to use.
     */
    private static final String DEFAULT_ENCODING = "UTF-8";

    /**
     * The maven project.
     */
    @Parameter(property = "xff.project", defaultValue = "${project}", required = true, readonly = true)
    protected MavenProject project;

    /**
     * The encoding to use when generating the package summary file and when storing file names.
     */
    @Parameter(property = "xff.encoding", defaultValue = DEFAULT_ENCODING)
    protected String encoding;
}
