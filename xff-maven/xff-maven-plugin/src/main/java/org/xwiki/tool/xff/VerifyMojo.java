/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.tool.xff;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Main Mojo for XFF.
 * 
 * @version $Id$
 * @since 7.1
 */
@Mojo(name = "verify")
public class VerifyMojo extends AbstractXFFMojo
{
    /**
     * Disable the verification engine.
     */
    @Parameter(property = "xff.verify.skip", defaultValue = "false")
    private boolean skip;

    @Override
    public void execute() throws MojoExecutionException
    {
        if (this.skip) {
            return;
        }

        // Only format XAR modules or when forced
        if (!this.project.getPackaging().equals("xff")) {
            getLog().info("Not a XFF module, skipping validity check...");
            return;
        }

        String projectBaseDir = this.project.getBasedir().getAbsolutePath();
        String resourcesPath = (projectBaseDir + "/src/main/resources").replace("/", File.separator);
    }
}
