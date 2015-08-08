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

import java.io.InputStream;
import java.nio.file.Path;
import java.util.Locale;

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
     * Name of the file to describe a wiki.
     */
    static final String PAGE_FILENAME = "page.xml";

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
     * Retain the last page class in order to close and reinit if the current class is a new one.
     */
    private Path previousClassPath;

    /**
     * Contain the model of a page (see xwiki-platform-rest-model).
     */
    private Page xPage = new Page();

    /**
     * Child reader for class.
     */
    private ClassReader classReader;

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
        this.classReader = new ClassReader(filter, proxyFilter);
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

    @Override
    public void open(Path path, EntityReference parentReference, InputStream inputStream) throws FilterException
    {
        if (inputStream != null) {
            this.xPage = (Page) this.unmarshal(inputStream, Page.class);
        }
        String pageName = this.xPage.getId();
        if (pageName == null) {
            pageName = path.getName(2).toString();
        }
        this.setReference(pageName, parentReference);
        this.parameters = this.parseParameters();
        this.parametersLocale = this.parseParametersLocale();
        this.parametersRevision = parseParametersRevision();
        proxyFilter.beginWikiDocument(this.reference.getName(), this.parameters);
        proxyFilter.beginWikiDocumentLocale(DEFAULT_LOCALE, this.parametersLocale);
        proxyFilter.beginWikiDocumentRevision(DEFAULT_REVISION, this.parametersRevision);
    }

    private void routeClass(Path path, InputStream inputStream) throws FilterException
    {
        Path classPath = path.subpath(0, 5);
        if (!classPath.equals(previousClassPath)) {
            if (previousClassPath != null) {
                classReader.close();
            }
            if (path.endsWith(ClassReader.CLASS_FILENAME) && path.getNameCount() == 5) {
                classReader.open(path, this.reference, inputStream);
            } else {
                classReader.open(path, this.reference, null);
                classReader.route(path, inputStream);
            }
        } else {
            classReader.route(path, inputStream);
        }
        previousClassPath = classPath;
    }

    @Override
    public void route(Path path, InputStream inputStream) throws FilterException
    {
        Path subPath = path.subpath(0, 4);
        if (subPath.endsWith("class")) {
            this.routeClass(path, inputStream);
        }
    }

    @Override
    public void close() throws FilterException
    {
        this.classReader.close();
        if (this.reference != null) {
            proxyFilter.endWikiDocumentRevision(DEFAULT_REVISION, this.parametersRevision);
            proxyFilter.endWikiDocumentLocale(DEFAULT_LOCALE, this.parametersLocale);
            proxyFilter.endWikiDocument(this.reference.getName(), this.parameters);
        }

        previousClassPath = null;
    }
}
