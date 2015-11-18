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
package org.xwiki.filter.xff.internal.input;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Locale;

import javax.inject.Named;

import org.apache.commons.io.IOUtils;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.filter.FilterEventParameters;
import org.xwiki.filter.FilterException;
import org.xwiki.filter.event.model.WikiDocumentFilter;
import org.xwiki.filter.xff.input.AbstractReader;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.rest.model.jaxb.Page;

/**
 * Read file from XFF and parse them or reroute them to child readers.
 * 
 * @version $Id$
 * @since 7.1
 */
@Component
@Named(org.xwiki.xff.core.model.Page.PAGE_HINT)
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
public class PagesReader extends AbstractReader
{
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

    private void parsePage(InputStream inputStream) throws FilterException
    {
        if (inputStream != null) {
            this.xPage = (Page) this.unmarshal(inputStream, Page.class);
        }
        String pageName = this.xPage.getName();
        if (pageName != null) {
            this.reference = new DocumentReference(pageName, (SpaceReference) this.reference.getParent());
        }
    }

    private FilterEventParameters parseParameters()
    {
        // TODO: parse parameters for the current document
        return FilterEventParameters.EMPTY;
    }

    private FilterEventParameters parseParametersLocale()
    {
        // TODO: parse parameters for the current document
        return FilterEventParameters.EMPTY;
    }

    private FilterEventParameters parseParametersRevision()
    {
        // TODO: Create a more generic parser for parameters
        FilterEventParameters params = new FilterEventParameters();
        String content = this.xPage.getContent();
        String title = this.xPage.getTitle();
        String parent = this.xPage.getParent();
        if (content != null) {
            params.put(WikiDocumentFilter.PARAMETER_CONTENT, content);
        }
        if (title != null) {
            params.put(WikiDocumentFilter.PARAMETER_TITLE, title);
        }
        if (parent != null) {
            params.put(WikiDocumentFilter.PARAMETER_PARENT, parent);
        }
        return params;
    }

    private void start() throws FilterException
    {
        if (!this.started) {
            this.parameters = this.parseParameters();
            this.parametersLocale = this.parseParametersLocale();
            this.parametersRevision = parseParametersRevision();
            this.proxyFilter.beginWikiDocument(this.reference.getName(), this.parameters);
            this.proxyFilter.beginWikiDocumentLocale(DEFAULT_LOCALE, this.parametersLocale);
            this.proxyFilter.beginWikiDocumentRevision(DEFAULT_REVISION, this.parametersRevision);
            this.started = true;
        }
    }

    private void end() throws FilterException
    {
        if (this.started) {
            this.proxyFilter.endWikiDocumentRevision(DEFAULT_REVISION, this.parametersRevision);
            this.proxyFilter.endWikiDocumentLocale(DEFAULT_LOCALE, this.parametersLocale);
            this.proxyFilter.endWikiDocument(this.reference.getName(), this.parameters);
        }
    }

    private void routeProperty(Path path, InputStream inputStream) throws FilterException
    {
        String filename = path.getFileName().toString();
        // TODO: Make it more generic
        if (filename.startsWith("content.")) {
            try {
                this.xPage.setContent(IOUtils.toString(inputStream));
            } catch (IOException e) {
                String message = String.format("Unable to read a string from '%s'.", path.toString());
                throw new FilterException(message, e);
            }
        }
    }

    @Override
    public void open(String id, EntityReference parentReference, Object filter, XFFInputFilter proxyFilter)
        throws FilterException
    {
        this.reference = new DocumentReference(id, (SpaceReference) parentReference);
        this.setFilters(filter, proxyFilter);
    }

    @Override
    public void route(Path path, InputStream inputStream) throws FilterException
    {
        String fileName = path.toString();
        if (fileName.equals(org.xwiki.xff.core.model.Page.PAGE_FILENAME)) {
            this.parsePage(inputStream);
            this.start();
            return;
        } else {
            this.start();
        }
        String hint = path.subpath(0, 1).toString();
        if (hint.equals(org.xwiki.xff.core.model.Property.PROPERTY_HINT)) {
            this.routeProperty(path, inputStream);
            return;
        }
        String childId = null;
        Path childPath = null;
        switch (hint) {
            case "attachments":
                childId = path.subpath(1, 2).toString();
                childPath = path.subpath(1, path.getNameCount());
                break;
            case "classes":
                childId = this.reference.toString();
                childPath = path.subpath(1, path.getNameCount());
                break;
            case "objects":
                childId = path.subpath(1, 3).toString();
                childPath = path.subpath(3, path.getNameCount());
                break;
            default:
                String message = String.format("PagesReader don't know how to route '%s'.", path.toString());
                throw new FilterException(message);
        }

        // Get a new reader only if the child change
        if (!childId.equals(this.lastReaderId)) {
            if (this.reader != null) {
                this.reader.close();
            }
            this.reader = this.getReader(hint);
            this.reader.open(childId, this.reference, this.filter, this.proxyFilter);
        }
        this.reader.route(childPath, inputStream);
        this.lastReaderId = childId;
    }

    @Override
    public void close() throws FilterException
    {
        if (this.reader != null) {
            this.reader.close();
        }
        this.end();
    }
}
