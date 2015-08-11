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

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Locale;

import org.apache.commons.io.IOUtils;
import org.xwiki.filter.FilterEventParameters;
import org.xwiki.filter.FilterException;
import org.xwiki.filter.xar2.input.AbstractReader;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.rest.model.jaxb.Page;
import org.xwiki.xar.internal.model.XarDocumentModel;

/**
 * Read file from XAR2 and parse them or reroute them to child readers.
 * 
 * @version $Id$
 * @since 7.1
 */
public class PageReader extends AbstractReader
{
    /**
     * Name of the file to describe a page.
     */
    static final String PAGE_FILENAME = "__page.xml";

    /**
     * The used locale for Document Locale.
     */
    private static final Locale DEFAULT_LOCALE = Locale.ROOT;

    /**
     * The used locale for Document Revision.
     */
    private static final String DEFAULT_REVISION = "1.1";

    /**
     * Reference to the current document.
     */
    private DocumentReference reference;

    /**
     * Contain the model of a page (see xwiki-platform-rest-model).
     */
    private Page xPage = new Page();

    /**
     * Retain information about if a page has been opened.
     */
    private boolean started;

    /**
     * Retain the last page in order to close and reinit if the current page is a new one.
     */
    private Path previousPagePath;

    /**
     * Retain the last sub-path in the page in order to close class, objects or attachments before opening another.
     */
    private Path previousPageElementPath;

    /**
     * Retain the last object in order to close and reinit if the current object is a new one.
     */
    private Path previousObjectPath;

    /**
     * Child reader for attachments.
     */
    private AttachmentReader attachmentReader;

    /**
     * Child reader for class.
     */
    private ClassReader classReader;

    /**
     * Child reader for objects.
     */
    private ObjectReader objectReader;

    /**
     * Parameters to send to the current document.
     */
    private FilterEventParameters parameters = FilterEventParameters.EMPTY;

    /**
     * Parameters to send to the current document locale.
     */
    private FilterEventParameters parametersLocale = FilterEventParameters.EMPTY;

    /**
     * Parameters to send to the current document revision.
     */
    private FilterEventParameters parametersRevision = FilterEventParameters.EMPTY;

    /**
     * Constructor that instantiate the child class reader.
     * 
     * @param filter is the input filter
     * @param proxyFilter is the output filter
     */
    public PageReader(Object filter, XAR2InputFilter proxyFilter)
    {
        super(filter, proxyFilter);
        this.attachmentReader = new AttachmentReader(filter, proxyFilter);
        this.classReader = new ClassReader(filter, proxyFilter);
        this.objectReader = new ObjectReader(filter, proxyFilter);
    }

    private void reset()
    {
        this.reference = null;
        this.previousObjectPath = null;
        this.xPage = new Page();
        this.parameters = FilterEventParameters.EMPTY;
        this.parametersLocale = FilterEventParameters.EMPTY;
        this.parametersRevision = FilterEventParameters.EMPTY;
        this.started = false;
    }

    private void setReference(String spaceName, EntityReference parentReference)
    {
        this.reference = new DocumentReference(spaceName, (SpaceReference) parentReference);
    }

    private FilterEventParameters parseParameters()
    {
        // TODO: parse parameters for the current document
        return null;
    }

    private FilterEventParameters parseParametersLocale()
    {
        // TODO: parse parameters for the current document
        return null;
    }

    private FilterEventParameters parseParametersRevision()
    {
        // TODO: Create a more generic parser for parameters
        FilterEventParameters params = new FilterEventParameters();
        params.put(XarDocumentModel.ELEMENT_CONTENT, this.xPage.getContent());
        return params;
    }

    private void init(Path path, InputStream inputStream, EntityReference parentReference) throws FilterException
    {
        this.reset();
        if (inputStream != null) {
            this.xPage = (Page) this.unmarshal(inputStream, Page.class);
        }
        String pageName = this.xPage.getId();
        if (pageName == null) {
            pageName = path.getName(2).toString();
        }
        this.setReference(pageName, parentReference);
    }

    private void start() throws FilterException
    {
        this.parameters = this.parseParameters();
        this.parametersLocale = this.parseParametersLocale();
        this.parametersRevision = parseParametersRevision();
        this.proxyFilter.beginWikiDocument(this.reference.getName(), this.parameters);
        this.proxyFilter.beginWikiDocumentLocale(DEFAULT_LOCALE, this.parametersLocale);
        this.proxyFilter.beginWikiDocumentRevision(DEFAULT_REVISION, this.parametersRevision);
        this.started = true;
    }

    private void routeMetadata(Path path, InputStream inputStream) throws FilterException
    {
        String filename = path.getFileName().toString();
        if (filename.startsWith("content.")) {
            try {
                this.xPage.setContent(IOUtils.toString(inputStream));
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private void routeObject(Path path, InputStream inputStream) throws FilterException
    {
        Path objectPath = path.subpath(0, 6);
        if (!objectPath.equals(this.previousObjectPath)) {
            if (this.previousObjectPath != null) {
                this.objectReader.finish();
            }
            if (path.endsWith(ObjectReader.OBJECT_FILENAME) && path.getNameCount() == 7) {
                this.objectReader.open(path, this.reference, inputStream);
            } else {
                this.objectReader.open(path, this.reference, null);
                this.objectReader.route(path, inputStream, null);
            }
        } else {
            this.objectReader.route(path, inputStream, null);
        }
        this.previousObjectPath = objectPath;
    }

    @Override
    public void route(Path path, InputStream inputStream, EntityReference parentReference) throws FilterException
    {
        Path pagePath = path.subpath(0, 3);
        Path pagePageElementPath = path.subpath(0, 4);

        // Close previous page and sub-paths before starting a new one
        if (!pagePageElementPath.equals(this.previousPageElementPath)) {
            this.finishPageElements();
            if (!pagePath.equals(this.previousPagePath)) {
                this.finish();
            }
        }
        // Parse files relative to page or reroute them
        if (path.endsWith(PageReader.PAGE_FILENAME)) {
            this.init(path, inputStream, parentReference);
        } else if (pagePageElementPath.endsWith("_metadata")) {
            this.routeMetadata(path, inputStream);
        } else {
            // If the page has not been initialized, initializes it with only the path
            if (this.reference == null) {
                this.init(path, null, parentReference);
            }
            // Before routing any other file, start the page
            if (!this.started) {
                this.start();
            }

            if (pagePageElementPath.endsWith("attachments")) {
                this.attachmentReader.route(path, inputStream, this.reference);
            } else if (pagePageElementPath.endsWith("class")) {
                this.classReader.route(path, inputStream, this.reference);
            } else if (pagePageElementPath.endsWith("objects")) {
                this.routeObject(path, inputStream);
            }
            this.previousPageElementPath = pagePageElementPath;
        }
        this.previousPagePath = pagePath;
    }

    private void finishPageElements() throws FilterException
    {
        this.attachmentReader.finish();
        this.classReader.finish();
        this.objectReader.finish();
    }

    @Override
    public void finish() throws FilterException
    {
        this.finishPageElements();
        if (this.reference != null) {
            if (!this.started) {
                this.start();
            }
            this.proxyFilter.endWikiDocumentRevision(DEFAULT_REVISION, this.parametersRevision);
            this.proxyFilter.endWikiDocumentLocale(DEFAULT_LOCALE, this.parametersLocale);
            this.proxyFilter.endWikiDocument(this.reference.getName(), this.parameters);
        }
        this.reset();
    }
}
