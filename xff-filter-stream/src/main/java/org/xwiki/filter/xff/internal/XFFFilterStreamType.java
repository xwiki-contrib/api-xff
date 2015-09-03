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
package org.xwiki.filter.xff.internal;

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
public class XFFFilterStreamType extends FilterStreamType
{
    /**
     * The XFF format.
     *
     * @since 7.1
     */
    public static final FilterStreamType XWIKI_XFF_10 = new FilterStreamType(SystemType.XWIKI, "xff", "1.0");

    /**
     * @param type is the kind of meta model (xwiki, mediawiki, confluence, etc.)
     * @param dataFormat define in which format the data is
     * @param version define which version of this format
     */
    public XFFFilterStreamType(SystemType type, String dataFormat, String version)
    {
        super(type, dataFormat, version);
    }

    /**
     * @param type is the kind of meta model (xwiki, mediawiki, confluence, etc.)
     * @param dataFormat define in which format the data is
     */
    public XFFFilterStreamType(SystemType type, String dataFormat)
    {
        super(type, dataFormat);
    }

}
