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
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.filter.FilterEventParameters;
import org.xwiki.filter.FilterException;
import org.xwiki.filter.event.model.WikiDocumentFilter;
import org.xwiki.filter.input.InputSource;
import org.xwiki.filter.input.InputStreamInputSource;
import org.xwiki.filter.xar2.input.XAR2InputProperties;
import org.xwiki.logging.marker.TranslationMarker;

/**
 * @version $Id$
 * @since 6.2M1
 */
@Component(roles = XAR2Reader.class)
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
public class XAR2Reader {
	@Inject
	private Logger logger;

	private XAR2InputProperties properties;

	public void setProperties(XAR2InputProperties properties) {
		this.properties = properties;
	}

	public void read(Object filter, XAR2InputFilter proxyFilter) {
		InputStream stream;
		InputSource source = this.properties.getSource();
		if (source instanceof InputStreamInputSource) {
			try {
				stream = ((InputStreamInputSource) source).getInputStream();
				readXAR2(stream, filter, proxyFilter);
			} catch (IOException e) {
				this.logger.error("Fail to read XAR2 file descriptor.", e);
			}
		} else {
			this.logger.error("Fail to read XAR2 file descriptor.");
		}
	}

	private void readXAR2(InputStream stream, Object filter,
			XAR2InputFilter proxyFilter) throws IOException {
		ZipArchiveInputStream zis = new ZipArchiveInputStream(stream, "UTF-8",
				false);
		for (ZipArchiveEntry entry = zis.getNextZipEntry(); entry != null; entry = zis
				.getNextZipEntry()) {
			this.logger.info("Parsing file '" + entry.getName() + "'.");
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
						proxyFilter.beginWiki(wikiName,
								FilterEventParameters.EMPTY);
						proxyFilter.beginWikiSpace(spaceName,
								FilterEventParameters.EMPTY);
						proxyFilter.beginWikiDocument(documentName,
								FilterEventParameters.EMPTY);
						proxyFilter.beginWikiDocumentLocale(Locale.ROOT,
								FilterEventParameters.EMPTY);
						proxyFilter.beginWikiDocumentRevision("1.1",
								FilterEventParameters.EMPTY);
						proxyFilter.endWikiDocumentRevision("1.1",
								FilterEventParameters.EMPTY);
						proxyFilter.endWikiDocumentLocale(Locale.ROOT,
								FilterEventParameters.EMPTY);
						proxyFilter.endWikiDocument(documentName,
								FilterEventParameters.EMPTY);
						proxyFilter.endWikiSpace(spaceName,
								FilterEventParameters.EMPTY);
						proxyFilter.endWiki(wikiName,
								FilterEventParameters.EMPTY);
					} catch (FilterException e) {
						String message = String.format(
								"Problem with the import of the file '%s'.",
								path);
						logger.error(message, e);
					}
				}
			}
		}
		zis.close();
	}
}
