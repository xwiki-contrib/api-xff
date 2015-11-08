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

import static org.junit.Assert.assertNull;

import java.io.InputStream;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.xwiki.filter.FilterException;
import org.xwiki.filter.xff.input.Reader;
import org.xwiki.filter.xff.test.internal.input.TestReader;
import org.xwiki.rest.model.jaxb.Wiki;
import org.xwiki.test.mockito.MockitoComponentMockingRule;

public class XFFUnitTest
{
    @Rule
    public final MockitoComponentMockingRule<TestReader> mocker = new MockitoComponentMockingRule<TestReader>(
        TestReader.class);

    private TestReader testReader = null;

    @Before
    public void setUp() throws Exception
    {
        this.testReader = mocker.getInstance(Reader.class, "test");
    }

    @Test
    public void malformedXML()
    {
        String fileName = "wiki.xml";
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(fileName);
        assertNull(this.testReader.publicUnmarshal(inputStream, Wiki.class));
    }

    @Test(expected = FilterException.class)
    public void wrongHintComponent() throws FilterException
    {
        this.testReader.publicGetReader("wrongreader");
    }
}
