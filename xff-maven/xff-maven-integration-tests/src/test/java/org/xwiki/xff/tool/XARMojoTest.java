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
package org.xwiki.xff.tool;

import java.io.File;
import java.util.Arrays;

import org.apache.maven.it.Verifier;
import org.junit.Test;

/**
 * Integration tests for the XFF Mojo.
 * 
 * @version $Id: 7d0dc5bb09b8ca8168170d4de375e995a1f88e3c $
 * @since 4.2M1
 */
public class XARMojoTest extends AbstractMojoTest
{
    @Test
    public void validPackageXml() throws Exception
    {
        Verifier verifier = createVerifier("/validXml");

        verifier.executeGoals(Arrays.asList("clean", "package"));
        verifier.verifyErrorFreeLog();

        File tempDir = new File(verifier.getBasedir(), "target/temp");
        tempDir.mkdirs();

        // Extract the generated XFF so that we verify its content easily
        File xffFile = new File(verifier.getBasedir(), "target/xff-maven-plugin-test.xff");
//        ZipUnArchiver unarchiver = new ZipUnArchiver(xffFile);
//        unarchiver.enableLogging(new ConsoleLogger(Logger.LEVEL_ERROR, "xff"));
//        unarchiver.setDestDirectory(tempDir);
//        unarchiver.extract();

//        ZipFile zip = new ZipFile(xffFile);
//        Enumeration<ZipEntry> entries = zip.getEntries();
//        Assert.assertTrue(entries.hasMoreElements());
//        Assert.assertEquals(entries.nextElement().toString(), Index.INDEX_FILENAME);

//        File classesDir = new File(testDir, "target/classes");
//        Collection<String> documentNames = XARMojo.getDocumentNamesFromXML(new File(classesDir, "package.xml"));
//
//        int countEntries = 0;
//        while (entries.hasMoreElements()) {
//            String entryName = entries.nextElement().toString();
//            ++countEntries;
//
//            File currentFile = new File(tempDir, entryName);
//            String documentName = XWikiDocument.getReference(currentFile);
//            if (!documentNames.contains(documentName)) {
//                Assert.fail(String.format("Document [%s] cannot be found in the newly created xar archive.",
//                    documentName));
//            }
//        }
//        Assert.assertEquals("The newly created xar archive doesn't contain the required documents",
//            documentNames.size(), countEntries);
    }
}
