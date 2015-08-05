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
package org.xwiki.filter.xar2.internal.input;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.filter.FilterEventParameters;
import org.xwiki.filter.FilterException;
import org.xwiki.filter.input.FileInputSource;
import org.xwiki.filter.input.InputSource;
import org.xwiki.filter.xar2.input.DocumentStack;
import org.xwiki.filter.xar2.input.XAR2InputProperties;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.rest.model.jaxb.Class;
import org.xwiki.rest.model.jaxb.Page;

/**
 * @version $Id$
 * @since 7.1
 */
@Component(roles = XAR2Reader.class)
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
public class XAR2Reader
{
    private static final String DEFAULT_DOCUMENT_REVISION = "1.1";
    /**
     * Logger to report errors, warnings, etc.
     */
    @Inject
    private Logger logger;

    /**
     * Properties of the reader.
     */
    private XAR2InputProperties properties;

    /**
     * Set the properties before launching the reader.
     * 
     * @param properties contains all properties for input source
     */
    public void setProperties(XAR2InputProperties properties)
    {
        this.properties = properties;
    }

    /**
     * Entry point for reading a XAR2 file.
     * 
     * @param filter is the input filter
     * @param proxyFilter is the filter into which you translate
     */
    public void read(Object filter, XAR2InputFilter proxyFilter)
    {
        InputSource source = this.properties.getSource();
        if (source instanceof FileInputSource) {
            try {
                parseXAR2File(((FileInputSource) source).getFile(), filter, proxyFilter);
            } catch (IOException e) {
                this.logger.error("Fail to get file from input source.", e);
            }
        } else {
            this.logger.error("Fail to read XAR2 file descriptor.");
        }
    }

    private DocumentReference getDocumentReferenceFromPath(String path)
    {
        String[] pathElements = path.split("/");
        String documentName = null;
        String spaceName = null;
        String wikiName = null;

        wikiName = pathElements[0];
        spaceName = pathElements[1];
        documentName = pathElements[2];

        return new DocumentReference(wikiName, spaceName, documentName);
    }

    private void addEntryToDocumentStack(DocumentStack documentStack, ZipFile zf, ZipArchiveEntry entry)
    {
        Path filePath = Paths.get(entry.getName());
        if (filePath.endsWith("index.xml")) {
            Page xPage = null;
            try {
                JAXBContext jc = JAXBContext.newInstance(Page.class);
                Unmarshaller unmarshaller = jc.createUnmarshaller();
                xPage = (Page) unmarshaller.unmarshal(zf.getInputStream(entry));
            } catch (IOException e) {
                String message = String.format("Cannot get Page stream from '%s'", filePath.toString());
                logger.error(message, e);
            } catch (JAXBException e) {
                String message = String.format("Unable to unmarshal a Page from '%s'", filePath.toString());
                logger.error(message, e);
            }
            documentStack.setxPage(xPage);
        } else if (filePath.endsWith("class.xml")) {
            Class xClass = null;
            try {
                JAXBContext jc = JAXBContext.newInstance(Page.class);
                Unmarshaller unmarshaller = jc.createUnmarshaller();
                xClass = (Class) unmarshaller.unmarshal(zf.getInputStream(entry));
            } catch (IOException e) {
                String message = String.format("Cannot get Class stream from '%s'", filePath.toString());
                logger.error(message, e);
            } catch (JAXBException e) {
                String message = String.format("Unable to unmarshal a Class from '%s'", filePath.toString());
                logger.error(message, e);
            }
            documentStack.setxClass(xClass);

        }
    }

    private void processDocumentStack(DocumentStack documentStack, XAR2InputFilter proxyFilter) throws FilterException
    {
        DocumentReference reference = documentStack.getReference();
        String wikiName = reference.getWikiReference().getName();
        List<SpaceReference> spaceReferencesList = reference.getSpaceReferences();
        String documentName = reference.getName();
        proxyFilter.beginWiki(wikiName, FilterEventParameters.EMPTY);
        for (SpaceReference spaceReference : spaceReferencesList) {
            String spaceName = spaceReference.getName();
            proxyFilter.beginWikiSpace(spaceName, FilterEventParameters.EMPTY);
        }
        proxyFilter.beginWikiDocument(documentName, FilterEventParameters.EMPTY);
        proxyFilter.beginWikiDocumentLocale(Locale.ROOT, FilterEventParameters.EMPTY);
        proxyFilter.beginWikiDocumentRevision(DEFAULT_DOCUMENT_REVISION, FilterEventParameters.EMPTY);
        proxyFilter.endWikiDocumentRevision(DEFAULT_DOCUMENT_REVISION, FilterEventParameters.EMPTY);
        proxyFilter.endWikiDocumentLocale(Locale.ROOT, FilterEventParameters.EMPTY);
        proxyFilter.endWikiDocument(documentName, FilterEventParameters.EMPTY);
        for (SpaceReference spaceReference : spaceReferencesList) {
            String spaceName = spaceReference.getName();
            proxyFilter.endWikiSpace(spaceName, FilterEventParameters.EMPTY);
        }
        proxyFilter.endWiki(wikiName, FilterEventParameters.EMPTY);
    }

    private void parseXAR2File(File file, Object filter, XAR2InputFilter proxyFilter) throws IOException
    {
        ZipFile zf = new ZipFile(file, "UTF-8");
        DocumentReference previousDocumentReference = null;
        DocumentStack currentDocumentStack = new DocumentStack();
        Enumeration<ZipArchiveEntry> entries = zf.getEntries();
        for (ZipArchiveEntry entry = entries.nextElement(); entry != null; entry = entries.nextElement()) {
            DocumentReference currentDocumentReference = getDocumentReferenceFromPath(entry.getName());
            if (currentDocumentReference.equals(previousDocumentReference)) {
                addEntryToDocumentStack(currentDocumentStack, zf, entry);
            } else {
                try {
                    processDocumentStack(currentDocumentStack, proxyFilter);
                } catch (FilterException e) {
                    String message = String.format("Unable to process document stack '%s'", currentDocumentReference);
                    logger.error(message, e);
                    e.printStackTrace();
                }
                currentDocumentStack = new DocumentStack();
                currentDocumentStack.setReference(currentDocumentReference);
                addEntryToDocumentStack(currentDocumentStack, zf, entry);
            }
        }
    }
/*
    private void readXAR2(File file, Object filter, XAR2InputFilter proxyFilter) throws IOException
    {
        ZipFile zf = new ZipFile(file, "UTF-8");
        Enumeration<ZipArchiveEntry> entries = zf.getEntries();
        for (ZipArchiveEntry entry = entries.nextElement(); entry != null; entry = entries.nextElement()) {
            if (!entry.isDirectory()) {
                String path = entry.getName();
                String[] pathElements = path.split("/");
                int length = pathElements.length;
                String filename = pathElements[length - 1];
                if (filename.endsWith("index.xml")) {
                    String documentName = null, spaceName = null, wikiName = null;
                    if (length >= 2) {
                        documentName = pathElements[length - 2];
                    }
                    if (length >= 3) {
                        spaceName = pathElements[length - 3];
                    }
                    if (length >= 4) {
                        wikiName = pathElements[length - 4];
                    }
                    try {
                        proxyFilter.beginWiki(wikiName, FilterEventParameters.EMPTY);
                        proxyFilter.beginWikiSpace(spaceName, FilterEventParameters.EMPTY);
                        proxyFilter.beginWikiDocument(documentName, FilterEventParameters.EMPTY);
                        proxyFilter.beginWikiDocumentLocale(Locale.ROOT, FilterEventParameters.EMPTY);
                        proxyFilter.beginWikiDocumentRevision("1.1", FilterEventParameters.EMPTY);
                        proxyFilter.endWikiDocumentRevision("1.1", FilterEventParameters.EMPTY);
                        proxyFilter.endWikiDocumentLocale(Locale.ROOT, FilterEventParameters.EMPTY);
                        proxyFilter.endWikiDocument(documentName, FilterEventParameters.EMPTY);
                        proxyFilter.endWikiSpace(spaceName, FilterEventParameters.EMPTY);
                        proxyFilter.endWiki(wikiName, FilterEventParameters.EMPTY);
                    } catch (FilterException e) {
                        String message = String.format("Problem with the import of the file '%s'.", path);
                        logger.error(message, e);
                    }
                } else if (filename.equals("class.xml")) {

                }
            }
        }
        zf.close();
    }
    */
}
