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
public class SingleAnswerConditionBeanWrapper implements Serializable
{
	private int index;
	private SingleAnswerConditionBean singleAnswerCondition;
	
	public SingleAnswerConditionBeanWrapper(int index,SingleAnswerConditionBean singleAnswerCondition)
	{
		this.index=index;
		this.singleAnswerCondition=singleAnswerCondition;
	}
	
	public int getIndex()
	{
		return index;
	}
	
	public void setIndex(int index)
	{
		this.index=index;
	}
	
	public boolean isEven()
	{
		return index % 2 == 0;
	}
	
	public boolean isOdd()
	{
		return index % 2 != 0;
	}
	
	public SingleAnswerConditionBean getSingleAnswerCondition()
	{
		return singleAnswerCondition;
	}
	
	public void setSingleAnswerCondition(SingleAnswerConditionBean singleAnswerCondition)
	{
		this.singleAnswerCondition=singleAnswerCondition;
	}
	
	public boolean isFlagNot()
	{
		return singleAnswerCondition==null?false:singleAnswerCondition.isFlagNot();
	}
	
	public void setFlagNot(boolean flagNot)
	{
		if (singleAnswerCondition!=null)
		{
			singleAnswerCondition.setFlagNot(flagNot);
		}
	}
	
	public int getAnswerPosition()
	{
		return singleAnswerCondition==null?0:singleAnswerCondition.getAnswerPosition();
	}

	public void setAnswerPosition(int answerPosition)
	{
		if (singleAnswerCondition!=null)
		{
			singleAnswerCondition.setAnswerPosition(answerPosition);
		}
	}
}
