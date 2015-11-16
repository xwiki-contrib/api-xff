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

import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.xwiki.xff.core.XFFZipper;

/**
 * Main Mojo for XFF.
 * 
 * @version $Id$
 * @since 7.1
 */
@Mojo(name = "xff")
public class XFFMojo extends AbstractXFFMojo
{
    @Override
    public void execute() throws MojoExecutionException
    {
        try {
            packageXFF();
        } catch (Exception e) {
            throw new MojoExecutionException("Error while creating XFF package.", e);
        }
    }

    private void packageXFF() throws Exception
    {
        // The source dir points to the target/classes directory where the Maven resources plugin
        // has copied the XFF files during the process-resources phase.
        // For package.xml, however, we look in the resources directory (i.e. src/main/resources).
        Path sourceDir = Paths.get(this.project.getBuild().getOutputDirectory());

        Path xffFile = Paths.get(this.project.getBuild().getDirectory(), this.project.getArtifactId() + ".xff");
        XFFZipper xffZipper = new XFFZipper(sourceDir);
        xffZipper.xff(xffFile);

        this.project.getArtifact().setFile(xffFile.toFile());
    }
}
