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

/**
 * @version $Id$
 * @since 7.1
 */
public final class XFFFilterUtils
{
    /**
     * @since 7.1
     */
    public static final String ROLEHINT = "xwiki+xff/1.0";

    /**
     * @version $Id$
     * @since 7.1
     */
    public static class EventParameter
    {
        /**
         * Name of the event parameter.
         */
        public String name;

        /**
         * Type of the event parameter.
         */
        public Class<?> type;

        /**
         * @param name is the name of the event parameter
         * @param type is the type of the event parameter
         */
        public EventParameter(String name, Class<?> type)
        {
            this.name = name;
            this.type = type;
        }

        /**
         * @param name is the name of the event parameter
         */
        public EventParameter(String name)
        {
            this(name, String.class);
        }
    }

    private XFFFilterUtils()
    {
    }
}
