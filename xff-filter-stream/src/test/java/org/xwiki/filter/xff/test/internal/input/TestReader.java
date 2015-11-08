package org.xwiki.filter.xff.test.internal.input;

import java.io.InputStream;
import java.nio.file.Path;

import javax.inject.Named;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.filter.FilterException;
import org.xwiki.filter.xff.input.AbstractReader;
import org.xwiki.filter.xff.input.Reader;
import org.xwiki.filter.xff.internal.input.XFFInputFilter;
import org.xwiki.model.reference.EntityReference;

@Component
@Named("test")
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
public class TestReader extends AbstractReader
{
    @Override
    public void route(Path path, InputStream inputStream) throws FilterException
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void close() throws FilterException
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void open(String id, EntityReference parentReference, Object filter, XFFInputFilter proxyFilter)
        throws FilterException
    {
        // TODO Auto-generated method stub

    }

    public Object publicUnmarshal(InputStream inputStream, Class<?> type)
    {
        return this.unmarshal(inputStream, type);
    }

    public Reader publicGetReader(String hint) throws FilterException
    {
        return this.getReader(hint);
    }
}
