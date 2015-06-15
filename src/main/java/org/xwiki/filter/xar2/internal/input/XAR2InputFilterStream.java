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

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.filter.FilterException;
import org.xwiki.filter.input.AbstractBeanInputFilterStream;
import org.xwiki.filter.input.InputSource;
import org.xwiki.filter.input.InputStreamInputSource;
import org.xwiki.filter.xar2.input.XAR2InputProperties;
import org.xwiki.filter.xar2.internal.XAR2FilterUtils;

/**
 * @version $Id$
 * @since 6.2M1
 */
@Component
@Named(XAR2FilterUtils.ROLEHINT)
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
public class XAR2InputFilterStream extends
		AbstractBeanInputFilterStream<XAR2InputProperties, XAR2InputFilter> {
	@Inject
	private Provider<XAR2Reader> xar2ReaderProvider;

	@Override
	public void close() throws IOException {
		this.properties.getSource().close();
	}

	@Override
	protected void read(Object filter, XAR2InputFilter proxyFilter)
			throws FilterException {
		InputSource inputSource = this.properties.getSource();

		if (inputSource instanceof InputStreamInputSource) {
			XAR2Reader xar2Reader = this.xar2ReaderProvider.get();
			xar2Reader.setProperties(this.properties);
			xar2Reader.read(filter, proxyFilter);
		} else {
			throw new FilterException(String.format(
					"Unsupported input source of type [%s]",
					inputSource.getClass()));
		}
	}
}
