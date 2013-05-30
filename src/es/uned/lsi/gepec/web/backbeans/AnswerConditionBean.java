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
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class AnswerConditionBean extends ConditionBean implements Serializable
{
	public final static String TYPE="CONDITION_TYPE_ANSWER";
	
	private List<SingleAnswerConditionBean> singleAnswerConditions;
	
	// We use an alternative list of wrapped conditions to be able to display a condition in several rows of a 
	// <h:datatable>
	// Note that there are samples in Internet using DataModel for the same effect but we have a problem with some 
	// action listeners that don't work correctly with DataModel so we prefer to return a list of wrapped conditions
	// with all the information we need with a more common ArrayList
	private List<SingleAnswerConditionBeanWrapper> singleAnswerConditionsWrapped;
	
	public AnswerConditionBean()
	{
		super(TYPE);
		singleAnswerConditions=new ArrayList<SingleAnswerConditionBean>();
		singleAnswerConditionsWrapped=null;
	}
	
	public AnswerConditionBean(AnswerConditionBean otherAnswerCondition)
	{
		super(TYPE);
		singleAnswerConditions=new ArrayList<SingleAnswerConditionBean>();
		for (SingleAnswerConditionBean singleAnswerCondition:otherAnswerCondition.getSingleAnswerConditions())
		{
			if (singleAnswerCondition instanceof SingleDragDropAnswerConditionBean)
			{
				singleAnswerConditions.add(new SingleDragDropAnswerConditionBean(
					(SingleDragDropAnswerConditionBean)singleAnswerCondition));
			}
			else
			{
				singleAnswerConditions.add(new SingleAnswerConditionBean(singleAnswerCondition));
			}
		}
	}
	
	public List<SingleAnswerConditionBean> getSingleAnswerConditions()
	{
		return singleAnswerConditions;
	}
	
	public void setSingleAnswerConditions(List<SingleAnswerConditionBean> singleAnswerConditions)
	{
		this.singleAnswerConditions=singleAnswerConditions;
	}
	
	public int getSingleAnswerConditionsSize()
	{
		return getSingleAnswerConditions().size();
	}
	
	public int getSingleAnswerConditionPosition(SingleAnswerConditionBean singleAnswerCondition)
	{
		return getSingleAnswerConditions().indexOf(singleAnswerCondition)+1;
	}
	
	public boolean isFirstSingleAnswerConditionsSize(SingleAnswerConditionBean singleAnswerCondition)
	{
		return (getSingleAnswerConditions().get(0).equals(singleAnswerCondition));
	}
	
	public boolean isLastSingleAnswerConditionsSize(SingleAnswerConditionBean singleAnswerCondition)
	{
		return (getSingleAnswerConditions().get(getSingleAnswerConditions().size()-1).equals(singleAnswerCondition));
	}
	
	public List<SingleAnswerConditionBeanWrapper> getSingleAnswerConditionsWrapped()
	{
		return singleAnswerConditionsWrapped;
	}
	
	public void setSingleAnswerConditionsWrapped(List<SingleAnswerConditionBeanWrapper> singleAnswerConditionsWrapped)
	{
		this.singleAnswerConditionsWrapped=singleAnswerConditionsWrapped;
	}
	
	private List<SingleAnswerConditionBeanWrapper> getSingleAnswerConditionsWrappedWithRepeatedRows(int repeatedRows)
	{
		if (getSingleAnswerConditionsWrapped()==null)
		{
			setSingleAnswerConditionsWrapped(new ArrayList<SingleAnswerConditionBeanWrapper>());
			int index=0;
			for (SingleAnswerConditionBean singleAnswerCondition:getSingleAnswerConditions())
			{
				if (singleAnswerCondition instanceof SingleDragDropAnswerConditionBean)
				{
					for (int i=0;i<repeatedRows;i++,index++)
					{
						getSingleAnswerConditionsWrapped().add(new SingleDragDropAnswerConditionBeanWrapper(
							index,(SingleDragDropAnswerConditionBean)singleAnswerCondition));
					}
				}
				else
				{
					for (int i=0;i<repeatedRows;i++,index++)
					{
						getSingleAnswerConditionsWrapped().add(
							new SingleAnswerConditionBeanWrapper(index,singleAnswerCondition));
					}
				}
			}
		}
		return getSingleAnswerConditionsWrapped();
	}
	
	public List<SingleAnswerConditionBeanWrapper> getSingleAnswerConditionsWrappedWithDuplicatedRows()
	{
		return getSingleAnswerConditionsWrappedWithRepeatedRows(2);
	}
}