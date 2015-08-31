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
package org.xwiki.filter.xfs.internal.input;

import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.filter.input.AbstractBeanInputFilterStreamFactory;
import org.xwiki.filter.xfs.input.XFSInputProperties;
import org.xwiki.filter.xfs.internal.XFSFilterStreamType;
import org.xwiki.filter.xfs.internal.XFSFilterUtils;

/**
 * Generate events from XFS FilterStream package.
 * 
 * @version $Id$
 * @since 7.1 
 */
@Component
@Named(XFSFilterUtils.ROLEHINT)
@Singleton
public class XFSInputFilterStreamFactory extends
    AbstractBeanInputFilterStreamFactory<XFSInputProperties, XFSInputFilter>
{
    /**
     * Default constructor.
     */
    public XFSInputFilterStreamFactory()
    {
        super(XFSFilterStreamType.XWIKI_XFS_10);

        setName("XFS input stream");
        setDescription("Generates wiki events from XFS package.");
    }
}
