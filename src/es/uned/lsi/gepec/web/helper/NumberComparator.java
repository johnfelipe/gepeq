/* OpenMark Authoring Tool (GEPEQ)
 * Copyright (C) 2013 UNED
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package es.uned.lsi.gepec.web.helper;

import java.util.ArrayList;
import java.util.List;

/** Helper class that define number comparators */
public class NumberComparator
{
	public final static String EQUAL="EQUAL";
	public final static String NOT_EQUAL="NOT_EQUAL";
	public final static String GREATER="GREATER";
	public final static String GREATER_EQUAL="GREATER_EQUAL";
	public final static String LESS="LESS";
	public final static String LESS_EQUAL="LESS_EQUAL";
	public final static String BETWEEN="BETWEEN";
	
	public final static List<String> COMPARATORS;
	static
	{
		COMPARATORS=new ArrayList<String>();
		COMPARATORS.add(EQUAL);
		COMPARATORS.add(NOT_EQUAL);
		COMPARATORS.add(GREATER);
		COMPARATORS.add(GREATER_EQUAL);
		COMPARATORS.add(LESS);
		COMPARATORS.add(LESS_EQUAL);
		COMPARATORS.add(BETWEEN);
	}
	
	public final static List<String> COMPARATORS_F;
	static
	{
		COMPARATORS_F=new ArrayList<String>();
		StringBuffer equalF=new StringBuffer(EQUAL);
		equalF.append("_F");
		COMPARATORS_F.add(equalF.toString());
		StringBuffer notEqualF=new StringBuffer(NOT_EQUAL);
		notEqualF.append("_F");
		COMPARATORS_F.add(notEqualF.toString());
		StringBuffer greaterF=new StringBuffer(GREATER);
		greaterF.append("_F");
		COMPARATORS_F.add(greaterF.toString());
		StringBuffer greaterEqualF=new StringBuffer(GREATER_EQUAL);
		greaterEqualF.append("_F");
		COMPARATORS_F.add(greaterEqualF.toString());
		StringBuffer lessF=new StringBuffer(LESS);
		lessF.append("_F");
		COMPARATORS_F.add(lessF.toString());
		StringBuffer lessEqualF=new StringBuffer(LESS_EQUAL);
		lessEqualF.append("_F");
		COMPARATORS_F.add(lessEqualF.toString());
		StringBuffer betweenF=new StringBuffer(BETWEEN);
		betweenF.append("_F");
		COMPARATORS_F.add(betweenF.toString());
	}
	
	public static boolean compareU(String str,String comparator)
	{
		boolean ok=false;
		if (str==null)
		{
			ok=comparator==null;
		}
		else
		{
			ok=str.equals(comparator);
			if (!ok)
			{
				StringBuffer comparatorF=new StringBuffer(comparator);
				comparatorF.append("_F");
				ok=str.equals(comparatorF.toString());
			}
		}
		return ok;
	}
}
