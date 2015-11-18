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
package org.xwiki.xff.core;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

/**
 * Create zip file out of a XFF folder structure.
 * 
 * @version $Id$
 * @since 7.1
 */
public class XFFZipper
{
    /**
     * To explore into XFF package.
     */
    private XFFExplorer packageExplorer;

    /**
     * Initialize the Walker by parsing the folder and ordering the files.
     * 
     * @param path is the root path where to look for a XFF package.
     * @throws IOException whenever there is problems in reading files or walking through the folders.
     */
    public XFFZipper(Path path) throws IOException
    {
        this.packageExplorer = new XFFExplorer(path);
    }

    /**
     * Create an XFF package from the folder.
     * 
     * @param path to the file you want to write the package into.
     * @throws IOException if file path is incorrect or if there is problem to find files of the package.
     * @throws URISyntaxException only if path is incorrect
     */
    public void xff(Path path) throws IOException, URISyntaxException
    {
        Map<String, String> options = new HashMap<>();
        options.put("create", "true");
        options.put("encoding", "UTF-8");
        Path rootPath = this.packageExplorer.getPath();
        URI xffURI = URI.create(String.format("jar:file:%s", path.toString()));
        try (FileSystem xff = FileSystems.newFileSystem(xffURI, options)) {
            for (Path p : this.packageExplorer) {
                Path filePath = rootPath.resolve(p);
                Path xffPath = xff.getPath(p.toString());
                Path dirPath = xff.getPath(xffPath.subpath(0, xffPath.getNameCount() - 1).toString());
                if (!Files.isDirectory(dirPath)) {
                    Files.createDirectories(dirPath);
                }
                Files.copy(filePath, xffPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
            }
        }
        // zos.close();
    }
}
