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

import javax.faces.event.AjaxBehaviorEvent;

import org.primefaces.component.selectonemenu.SelectOneMenu;

import es.uned.lsi.gepec.util.HibernateUtil.Operation;
import es.uned.lsi.gepec.web.QuestionBean;
import es.uned.lsi.gepec.web.helper.NumberComparator;

@SuppressWarnings("serial")
public class RightDistanceConditionBean extends ConditionBean implements Serializable
{
	public static final String TYPE="CONDITION_TYPE_RIGHT_DISTANCE";
	
	private QuestionBean question;
	private String oldComparator;
	private String comparator;
	private int rightDistanceCmp;
	private int rightDistanceBetweenMin;
	private int rightDistanceBetweenMax;
	
	public RightDistanceConditionBean(QuestionBean question)
	{
		this(question,0,0,true);
	}
	
	public RightDistanceConditionBean(QuestionBean question,boolean genF)
	{
		this(question,0,0,genF);
	}
	
	public RightDistanceConditionBean(QuestionBean question,int minRightDistance,int maxRightDistance)
	{
		this(question,minRightDistance,maxRightDistance,true);
	}
	
	public RightDistanceConditionBean(QuestionBean question,int minRightDistance,int maxRightDistance,boolean genF)
	{
		super(TYPE);
		this.question=question;
		oldComparator=null;
		int absoluteMaxRightDistance=getMaxRightDistanceValue(question.getCurrentUserOperation(null));
		rightDistanceCmp=1;
		rightDistanceBetweenMin=1;
		rightDistanceBetweenMax=absoluteMaxRightDistance;
		if (minRightDistance==maxRightDistance)
		{
			if (genF)
			{
				StringBuffer sbComparator=new StringBuffer(NumberComparator.EQUAL);
				sbComparator.append("_F");
				comparator=sbComparator.toString();
			}
			else
			{	
				comparator=NumberComparator.EQUAL;
			}
			rightDistanceCmp=minRightDistance;
		}
		else if (minRightDistance>0)
		{
			if (maxRightDistance<absoluteMaxRightDistance)
			{
				if (genF)
				{
					StringBuffer sbComparator=new StringBuffer(NumberComparator.BETWEEN);
					sbComparator.append("_F");
					comparator=sbComparator.toString();
				}
				else
				{
					comparator=NumberComparator.BETWEEN;
				}
				rightDistanceBetweenMin=minRightDistance;
				rightDistanceBetweenMax=maxRightDistance;
			}
			else
			{
				if (genF)
				{
					StringBuffer sbComparator=new StringBuffer(NumberComparator.GREATER_EQUAL);
					sbComparator.append("_F");
					comparator=sbComparator.toString();
				}
				else
				{
					comparator=NumberComparator.GREATER_EQUAL;
				}
				rightDistanceCmp=minRightDistance;
			}
		}
		else
		{
			if (genF)
			{
				StringBuffer sbComparator=new StringBuffer(NumberComparator.LESS_EQUAL);
				sbComparator.append("_F");
				comparator=sbComparator.toString();
			}
			else
			{
				comparator=NumberComparator.LESS_EQUAL;
			}
			rightDistanceCmp=maxRightDistance;
		}
	}
	
	public RightDistanceConditionBean(RightDistanceConditionBean otherRightDistanceCondition)
	{
		super(TYPE);
		question=otherRightDistanceCondition.question;
		oldComparator=otherRightDistanceCondition.oldComparator;
		comparator=otherRightDistanceCondition.comparator;
		rightDistanceCmp=otherRightDistanceCondition.rightDistanceCmp;
		rightDistanceBetweenMin=otherRightDistanceCondition.rightDistanceBetweenMin;
		rightDistanceBetweenMax=otherRightDistanceCondition.rightDistanceBetweenMax;
	}
	
	public String getComparator()
	{
		return comparator;
	}
	
	public void setComparator(String comparator)
	{
		oldComparator=this.comparator;
		this.comparator=comparator;
	}
	
	public int getRightDistanceCmp()
	{
		return rightDistanceCmp;
	}
	
	public void setRightDistanceCmp(int rightDistanceCmp)
	{
		this.rightDistanceCmp=rightDistanceCmp;
	}
	
	public int getRightDistanceBetweenMin()
	{
		return rightDistanceBetweenMin;
	}
	
	public void setRightDistanceBetweenMin(int rightDistanceBetweenMin)
	{
		this.rightDistanceBetweenMin=rightDistanceBetweenMin;
	}
	
	public int getRightDistanceBetweenMax()
	{
		return rightDistanceBetweenMax;
	}
	
	public void setRightDistanceBetweenMax(int rightDistanceBetweenMax)
	{
		this.rightDistanceBetweenMax=rightDistanceBetweenMax;
	}
	
	public int getRightDistanceMin()
	{
		int rightDistanceMin=0;
		if (NumberComparator.compareU(comparator,NumberComparator.EQUAL))
		{
			rightDistanceMin=rightDistanceCmp;
		}
		else if (NumberComparator.compareU(comparator,NumberComparator.GREATER))
		{
			rightDistanceMin=rightDistanceCmp+1;
		}
		else if (NumberComparator.compareU(comparator,NumberComparator.GREATER_EQUAL))
		{
			rightDistanceMin=rightDistanceCmp;
		}
		else if (NumberComparator.compareU(comparator,NumberComparator.BETWEEN))
		{
			rightDistanceMin=rightDistanceBetweenMin;
		}
		return rightDistanceMin;
	}
	
	public int getRightDistanceMax()
	{
		return getRightDistanceMax(null);
	}
	
	public int getRightDistanceMax(Operation operation)
	{
		int rightDistanceMax=getMaxRightDistanceValue(question.getCurrentUserOperation(operation));
		if (NumberComparator.compareU(comparator,NumberComparator.EQUAL))
		{
			rightDistanceMax=rightDistanceCmp;
		}
		else if (NumberComparator.compareU(comparator,NumberComparator.LESS))
		{
			rightDistanceMax=rightDistanceCmp-1;
		}
		else if (NumberComparator.compareU(comparator,NumberComparator.LESS_EQUAL))
		{
			rightDistanceMax=rightDistanceCmp;
		}
		else if (NumberComparator.compareU(comparator,NumberComparator.BETWEEN))
		{
			rightDistanceMax=rightDistanceBetweenMax;
		}
		return rightDistanceMax;
	}
	
	public boolean isBetween()
	{
		return NumberComparator.compareU(comparator,NumberComparator.BETWEEN);
	}
	
	public int getMaxRightDistanceValue()
	{
		return getMaxRightDistanceValue(null);
	}
	
	public int getMaxRightDistanceValue(Operation operation)
	{
		// Get current user session Hibernate operation
		operation=question.getCurrentUserOperation(operation);
		
		int selectableRightAnswers=question.getNumberOfSelectableRightAnswers(operation);
		int selectableWrongAnswers=question.getNumberOfSelectableWrongAnswers(operation);
		return selectableRightAnswers>=selectableWrongAnswers?selectableRightAnswers:selectableWrongAnswers;
	}
	
	public int getMinValueRightDistanceCmp()
	{
		int minValue=0;
		if (NumberComparator.compareU(comparator,NumberComparator.GREATER))
		{
			minValue=-1;
		}
		else if (NumberComparator.compareU(comparator,NumberComparator.LESS))
		{
			minValue=1;
		}
		return minValue;
	}
	
	public int getMaxValueRightDistanceCmp()
	{
		return getMaxValueRightDistanceCmp(null);
	}
	
	public int getMaxValueRightDistanceCmp(Operation operation)
	{
		int maxValue=getMaxRightDistanceValue(question.getCurrentUserOperation(operation));
		if (NumberComparator.compareU(comparator,NumberComparator.GREATER))
		{
			maxValue--;
		}
		else if (NumberComparator.compareU(comparator,NumberComparator.LESS))
		{
			maxValue++;
		}
		return maxValue;
	}
	
	public void changeComparator(AjaxBehaviorEvent event)
	{
		// Get current user session Hibernate operation
		Operation operation=question.getCurrentUserOperation(null);
		
		comparator=(String)((SelectOneMenu)event.getComponent()).getValue();
		if (NumberComparator.compareU(comparator,NumberComparator.BETWEEN))
		{
			if (rightDistanceCmp<0)
			{
				rightDistanceBetweenMin=0;
			}
			else
			{
				rightDistanceBetweenMin=rightDistanceCmp;
			}
			if (rightDistanceBetweenMax<rightDistanceBetweenMin)
			{
				rightDistanceBetweenMax=rightDistanceBetweenMin;
			}
		}
		else if (NumberComparator.compareU(oldComparator,NumberComparator.BETWEEN))
		{
			if (rightDistanceBetweenMin<getMinValueRightDistanceCmp())
			{
				rightDistanceCmp=getMinValueRightDistanceCmp();
			}
			else
			{
				int maxValueRightDistanceCmp=getMaxValueRightDistanceCmp(operation);
				if (rightDistanceBetweenMin>maxValueRightDistanceCmp)
				{
					rightDistanceCmp=maxValueRightDistanceCmp;
				}
				else
				{
					rightDistanceCmp=rightDistanceBetweenMin;
				}
			}
		}
		else
		{
			if (rightDistanceCmp<getMinValueRightDistanceCmp())
			{
				rightDistanceCmp=getMinValueRightDistanceCmp();
			}
			else
			{
				int maxValueRightDistanceCmp=getMaxValueRightDistanceCmp(operation);
				if (rightDistanceCmp>maxValueRightDistanceCmp)
				{
					rightDistanceCmp=maxValueRightDistanceCmp;
				}
			}
		}
	}
}
