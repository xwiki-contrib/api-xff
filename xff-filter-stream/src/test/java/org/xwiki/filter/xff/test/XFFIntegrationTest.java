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
package org.xwiki.filter.xff.test;

import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.xwiki.filter.test.integration.FilterTestSuite;
import org.xwiki.filter.test.integration.FilterTestSuite.Scope;
import org.xwiki.test.annotation.AllComponents;
import org.xwiki.xff.core.XFFZipper;

/**
 * Run all tests found in the classpath. These {@code *.test} files must follow the conventions described in
 * {@link org.xwiki.filter.test.integration.TestDataParser}.
 * 
 * @version $Id$
 */
@RunWith(FilterTestSuite.class)
@AllComponents
@Scope("xff")
public class XFFIntegrationTest
{
    @BeforeClass
    public static void beforeClass() throws Exception
    {
        Path outPath = Paths.get("target/test-" + new Date().getTime()).toAbsolutePath();
        Files.createDirectory(outPath);

        Path xffInPath = Paths.get("target/test-classes/packages/xff").toAbsolutePath();
        Path xffPath = Paths.get(outPath.toString(), "test-1.0.xff");
        XFFZipper xffPackageZipper = new XFFZipper(xffInPath);
        xffPackageZipper.xff(xffPath);

        Path xffDirSrc = Paths.get("target/test-classes/packages/xff");
        Path xffDirDst = Paths.get(outPath.toString() + "/xffdir");
//        Files.copy(xffDirSrc, xffDirDst, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
        FileVisitor<Path> visitor = new CopyDirVisitor(xffDirSrc, xffDirDst);
        Files.walkFileTree(xffDirSrc, visitor);

        System.setProperty("extension.repository", outPath.toAbsolutePath().toString());
    }
}
