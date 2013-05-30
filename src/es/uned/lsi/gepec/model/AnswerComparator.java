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
package es.uned.lsi.gepec.model;

import java.util.Comparator;

import es.uned.lsi.gepec.model.entities.Answer;

public class AnswerComparator implements Comparator<Answer>
{
	@Override
	public int compare(Answer answer1,Answer answer2)
	{
		int position1=answer1.getPosition();
		int position2=answer2.getPosition();
		return position1<=position2?(position1==position2?0:-1):1;
	}
}
