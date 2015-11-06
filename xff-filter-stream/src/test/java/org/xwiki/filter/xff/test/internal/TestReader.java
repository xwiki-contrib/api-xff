package org.xwiki.filter.xff.test.internal;

import java.io.InputStream;
import java.nio.file.Path;

import org.xwiki.filter.FilterException;
import org.xwiki.filter.xff.input.AbstractReader;
import org.xwiki.filter.xff.internal.input.XFFInputFilter;
import org.xwiki.model.reference.EntityReference;

public class TestReader extends AbstractReader
{
    /**
     * Constructor that only call the abstract constructor.
     * 
     * @param filter is the input filter
     * @param proxyFilter is the output filter
     */
    public TestReader(Object filter, XFFInputFilter proxyFilter)
    {
        super(filter, proxyFilter);
    }

    @Override
    public void route(Path path, InputStream inputStream, EntityReference parentReference) throws FilterException
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void finish() throws FilterException
    {
        // TODO Auto-generated method stub

    }

    public Object publicUnmarshal(InputStream inputStream, Class<?> type)
    {
        return this.unmarshal(inputStream, type);
    }
}
