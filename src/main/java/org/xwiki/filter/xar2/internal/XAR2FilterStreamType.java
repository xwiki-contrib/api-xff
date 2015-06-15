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
package org.xwiki.filter.xar2.internal;

import org.xwiki.filter.type.FilterStreamType;
import org.xwiki.filter.type.SystemType;
import org.xwiki.stability.Unstable;

/**
 * Combination of supported system and their data types.
 *
 * @version $Id$
 * @since 7.1
 */
@Unstable
public class XAR2FilterStreamType extends FilterStreamType {
	/**
	 * The XAR format.
	 *
	 * @since 7.1
	 */
	public static final FilterStreamType XWIKI_XAR_20 = new FilterStreamType(
			SystemType.XWIKI, "xar", "2.0");

	public XAR2FilterStreamType(SystemType type, String dataFormat,
			String version) {
		super(type, dataFormat, version);
	}

	public XAR2FilterStreamType(SystemType type, String dataFormat) {
		super(type, dataFormat);
	}

}
