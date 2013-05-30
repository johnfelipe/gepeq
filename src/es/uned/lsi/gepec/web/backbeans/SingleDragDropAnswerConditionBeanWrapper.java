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
package es.uned.lsi.gepec.web.backbeans;

import java.io.Serializable;

@SuppressWarnings("serial")
public class SingleDragDropAnswerConditionBeanWrapper extends SingleAnswerConditionBeanWrapper 
	implements Serializable
{
	public SingleDragDropAnswerConditionBeanWrapper(int index,
			SingleDragDropAnswerConditionBean singleDragDropAnswerCondition)
	{
		super(index,singleDragDropAnswerCondition);
	}
	
	public SingleDragDropAnswerConditionBean getSingleDragDropAnswerCondition()
	{
		return (SingleDragDropAnswerConditionBean)getSingleAnswerCondition();
	}
	
	public void setSingleDragDropAnswerCondition(SingleDragDropAnswerConditionBean singleDragDropAnswerCondition)
	{
		setSingleAnswerCondition(singleDragDropAnswerCondition);
	}
	
	public int getRightAnswerPosition()
	{
		return getSingleDragDropAnswerCondition()==null?0:getSingleDragDropAnswerCondition().getRightAnswerPosition();
	}
	
	public void setRightAnswerPosition(int rightAnswerPosition)
	{
		if (getSingleDragDropAnswerCondition()!=null)
		{
			getSingleDragDropAnswerCondition().setRightAnswerPosition(rightAnswerPosition);
		}
	}
}
