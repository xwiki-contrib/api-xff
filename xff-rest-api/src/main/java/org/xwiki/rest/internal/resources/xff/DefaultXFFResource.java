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
package org.xwiki.rest.internal.resources.xff;

import java.io.IOException;
import java.io.InputStream;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.filter.FilterEventParameters;
import org.xwiki.filter.FilterException;
import org.xwiki.filter.input.InputFilterStream;
import org.xwiki.filter.input.InputFilterStreamFactory;
import org.xwiki.filter.output.DefaultOutputStreamOutputTarget;
import org.xwiki.filter.output.OutputFilterStream;
import org.xwiki.filter.output.OutputFilterStreamFactory;
import org.xwiki.filter.output.OutputStreamOutputTarget;
import org.xwiki.filter.type.FilterStreamType;
import org.xwiki.filter.type.SystemType;
import org.xwiki.rest.XWikiRestComponent;
import org.xwiki.rest.XWikiRestException;
import org.xwiki.rest.resources.xff.XFFResource;

/**
 * @version $Id$
 */
@Component
@Named("org.xwiki.rest.internal.resources.xff.DefaultXFFResource")
public class DefaultXFFResource implements XFFResource, XWikiRestComponent
{
    private static final String DEFAULT_ENCODING = "encoding";

    private static final String DEFAULT_ENCODING_VALUE = "UTF-8";

    private static final String DEFAULT_VERBOSE = "verbose";

    private static final String TRUE = "true";

    private final FilterStreamType inputFilterStreamType = new FilterStreamType(SystemType.XWIKI, "xff", "1.0");

    private final FilterStreamType outputFilterStreamType = FilterStreamType.XWIKI_INSTANCE;

    @Inject
    @Named("context")
    private Provider<ComponentManager> componentManagerProvider;

    private FilterEventParameters getInputParameters(InputStream inputStream)
    {
        FilterEventParameters inputParameters = new FilterEventParameters();
        inputParameters.put(DEFAULT_ENCODING, DEFAULT_ENCODING_VALUE);
        inputParameters.put(DEFAULT_VERBOSE, TRUE);
        inputParameters.put("source", inputStream);
        return inputParameters;
    }

    private FilterEventParameters getOutputParameters(OutputStreamOutputTarget outputStream)
    {
        FilterEventParameters outputParameters = new FilterEventParameters();
        outputParameters.put(DEFAULT_ENCODING, DEFAULT_ENCODING_VALUE);
        outputParameters.put("format", TRUE);
        outputParameters.put(DEFAULT_VERBOSE, TRUE);
        outputParameters.put("target", outputStream);
        return outputParameters;
    }

    private InputFilterStream getInputFilterStream(FilterEventParameters inputParameters) throws XWikiRestException
    {
        ComponentManager cm = this.componentManagerProvider.get();
        InputFilterStreamFactory inputFactory;
        InputFilterStream inputFilter;
        try {
            inputFactory = cm.getInstance(InputFilterStreamFactory.class, inputFilterStreamType.serialize());
        } catch (ComponentLookupException e) {
            String message =
                String.format("Unable to get an component instance of input filter '%s'",
                    inputFilterStreamType.serialize());
            throw new XWikiRestException(message, e);
        }
        try {
            inputFilter = inputFactory.createInputFilterStream(inputParameters);
        } catch (FilterException e) {
            String message = String.format("Unable to create input filter '%s'", inputFilterStreamType.serialize());
            throw new XWikiRestException(message, e);
        }
        return inputFilter;
    }

    private OutputFilterStream getOutputFilterStream(FilterEventParameters outputParameters) throws XWikiRestException
    {
        ComponentManager cm = this.componentManagerProvider.get();
        OutputFilterStreamFactory outputFactory;
        OutputFilterStream outputFilter;
        try {
            outputFactory = cm.getInstance(OutputFilterStreamFactory.class, outputFilterStreamType.serialize());
        } catch (ComponentLookupException e) {
            String message =
                String.format("Unable to get an component instance of output filter '%s'",
                    inputFilterStreamType.serialize());
            throw new XWikiRestException(message, e);
        }

        try {
            outputFilter = outputFactory.createOutputFilterStream(outputParameters);
        } catch (FilterException e) {
            String message = String.format("Unable to create output filter '%s'", outputFilterStreamType.serialize());
            throw new XWikiRestException(message, e);
        }
        return outputFilter;
    }

    private void convert(InputFilterStream inputFilter, OutputFilterStream outputFilter) throws XWikiRestException
    {
        try {
            inputFilter.read(outputFilter.getFilter());
        } catch (FilterException e) {
            String message =
                String.format("Unable to filter from '%s' to '%s'", inputFilterStreamType.serialize(),
                    outputFilterStreamType.serialize());
            throw new XWikiRestException(message, e);
        }
    }

    @Override
    public void putXFF(String wikiName, InputStream xff) throws XWikiRestException
    {
        OutputStreamOutputTarget outputStream = new DefaultOutputStreamOutputTarget(null, true);

        // Create map of parameters
        FilterEventParameters inputParameters = this.getInputParameters(xff);
        FilterEventParameters outputParameters = this.getOutputParameters(outputStream);

        // Initialize the filters
        InputFilterStream inputFilter = this.getInputFilterStream(inputParameters);
        OutputFilterStream outputFilter = this.getOutputFilterStream(outputParameters);

        // Do the conversion
        this.convert(inputFilter, outputFilter);

        // Close filters
        try {
            inputFilter.close();
            outputFilter.close();
        } catch (IOException e) {
            throw new XWikiRestException("Unable to close filters", e);
        }
    }
}
