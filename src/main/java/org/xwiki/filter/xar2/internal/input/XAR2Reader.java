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
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.rest.model.jaxb.Class;
import org.xwiki.rest.model.jaxb.Page;
import org.xwiki.rest.model.jaxb.Property;

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
     * Keep track of the current opened wiki to avoid to send beginWiki all the time.
     */
    private String previousWiki;

    /**
     * Keep track of the current opened space to avoid to send beginWikiSpace all the time.
     */
    private String previousSpace;

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
        String wikiName = null;
        String spaceName = null;
        String documentName = null;

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

    private void preProcessDocumentStack(DocumentStack documentStack, XAR2InputFilter proxyFilter, boolean last)
        throws FilterException
    {
        DocumentReference reference = documentStack.getReference();
        EntityReference wikiReference = reference.extractReference(EntityType.WIKI);
        String wikiName = wikiReference.getName();
        EntityReference spaceReference = reference.extractReference(EntityType.SPACE);
        String spaceName = spaceReference.getName();

        // If we're going to change space, close the previous space first
        if (!spaceName.equals(previousSpace) && previousSpace != null) {
            proxyFilter.endWikiSpace(previousSpace, FilterEventParameters.EMPTY);
        }
        // If we're going to change wiki, close the previous wiki first
        if (!wikiName.equals(previousWiki) && previousWiki != null) {
            proxyFilter.endWiki(previousWiki, FilterEventParameters.EMPTY);
        }
        // Open a new wiki if previous one is different
        if (!wikiName.equals(previousWiki)) {
            proxyFilter.beginWiki(wikiName, FilterEventParameters.EMPTY);
        }
        // Open a new space if previous one is different
        if (!spaceName.equals(previousSpace)) {
            proxyFilter.beginWikiSpace(spaceName, FilterEventParameters.EMPTY);
        }
    }

    private void postProcessDocumentStack(DocumentStack documentStack, XAR2InputFilter proxyFilter, boolean last)
        throws FilterException
    {
        DocumentReference reference = documentStack.getReference();
        EntityReference wikiReference = reference.extractReference(EntityType.WIKI);
        String wikiName = wikiReference.getName();
        EntityReference spaceReference = reference.extractReference(EntityType.SPACE);
        String spaceName = spaceReference.getName();

        previousWiki = wikiName;
        previousSpace = spaceName;
        if (last) {
            proxyFilter.endWikiSpace(previousSpace, FilterEventParameters.EMPTY);
            proxyFilter.endWiki(previousWiki, FilterEventParameters.EMPTY);
        }
    }

    private void processDocumentStack(DocumentStack documentStack, XAR2InputFilter proxyFilter, boolean last)
    {
        DocumentReference reference = documentStack.getReference();
        EntityReference documentReference = reference.extractReference(EntityType.DOCUMENT);
        String documentName = documentReference.getName();

        try {
            preProcessDocumentStack(documentStack, proxyFilter, last);

            proxyFilter.beginWikiDocument(documentName, FilterEventParameters.EMPTY);
            proxyFilter.beginWikiDocumentLocale(Locale.ROOT, FilterEventParameters.EMPTY);
            proxyFilter.beginWikiDocumentRevision(DEFAULT_DOCUMENT_REVISION, FilterEventParameters.EMPTY);
            Class xClass = documentStack.getxClass();
            if (xClass != null) {
                proxyFilter.beginWikiClass(FilterEventParameters.EMPTY);
                for (Property property : xClass.getProperties()) {
                    proxyFilter.beginWikiClassProperty(property.getName(), property.getType(),
                        FilterEventParameters.EMPTY);
                    proxyFilter.endWikiClassProperty(property.getName(), property.getType(),
                        FilterEventParameters.EMPTY);
                }
                proxyFilter.endWikiClass(FilterEventParameters.EMPTY);
            }
            proxyFilter.endWikiDocumentRevision(DEFAULT_DOCUMENT_REVISION, FilterEventParameters.EMPTY);
            proxyFilter.endWikiDocumentLocale(Locale.ROOT, FilterEventParameters.EMPTY);
            proxyFilter.endWikiDocument(documentName, FilterEventParameters.EMPTY);

            postProcessDocumentStack(documentStack, proxyFilter, last);
        } catch (FilterException e) {
            String message = String.format("Unable to process document stack '%s'", reference.getName());
            logger.error(message, e);
            e.printStackTrace();
        }
    }

    private void parseXAR2File(File file, Object filter, XAR2InputFilter proxyFilter) throws IOException
    {
        ZipFile zf = new ZipFile(file, "UTF-8");
        DocumentReference previousDocumentReference = null;
        DocumentStack currentDocumentStack = null;
        Enumeration<ZipArchiveEntry> entries = zf.getEntries();

        while (entries.hasMoreElements()) {
            ZipArchiveEntry entry = entries.nextElement();
            boolean hasMore = entries.hasMoreElements();
            DocumentReference currentDocumentReference = getDocumentReferenceFromPath(entry.getName());
            if (!currentDocumentReference.equals(previousDocumentReference)) {
                if (currentDocumentStack != null) {
                    processDocumentStack(currentDocumentStack, proxyFilter, !hasMore);
                }
                if (hasMore) {
                    currentDocumentStack = new DocumentStack();
                    currentDocumentStack.setReference(currentDocumentReference);
                }
            }
            addEntryToDocumentStack(currentDocumentStack, zf, entry);
            if (!hasMore) {
                processDocumentStack(currentDocumentStack, proxyFilter, !hasMore);
            }
            previousDocumentReference = currentDocumentReference;
        }
    }
}
