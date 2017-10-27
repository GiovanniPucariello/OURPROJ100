/*
 * $Id: Annotation.java 1729 2012-06-26 12:28:01Z jdavid $
 *
 * Copyright (C) INRIA, 2010
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307 USA
 */

package fr.inrialpes.exmo.ontowrap;

/**
 * class Annotation
 */
public class Annotation {

    private final String value;
    private final String language;

    /**
     *
     * @param value
     * @param language
     */
    public Annotation(String value, String language) {
	this.value=value;
	this.language=language.intern();
    }

    /**
     *
     * @param value
     */
    public Annotation(String value) {
	this.value=value;
	this.language=null;
    }

    /**
     *
     * @return
     */
    public String getValue() {
	return value;
    }

    /**
     *
     * @return
     */
    public String getLanguage() {
	return language;
    }

    /**
     *
     * @return
     */
    public String toString() {
	return value+'@'+language;
    }

    /**
     *
     * @return
     */
    public int hashCode() {
	return value==null?0:value.hashCode();
    }

    /**
     *
     * @param o
     * @return
     */
    public boolean equals(Object o) {
	if (o instanceof Annotation) {
	    Annotation a = (Annotation) o;
	    return value.equals(a.value) && language==a.language;
	}
	return false;
    }
}
