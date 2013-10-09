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
		int absoluteMaxRightDistance=getMaxRightDistanceValue();
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
	
	public void setNewComparator(String newComparator)
	{
		this.oldComparator=newComparator;
		this.comparator=newComparator;
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
		if (NumberComparator.compareU(getComparator(),NumberComparator.EQUAL))
		{
			rightDistanceMin=getRightDistanceCmp();
		}
		else if (NumberComparator.compareU(getComparator(),NumberComparator.GREATER))
		{
			rightDistanceMin=getRightDistanceCmp()+1;
		}
		else if (NumberComparator.compareU(getComparator(),NumberComparator.GREATER_EQUAL))
		{
			rightDistanceMin=getRightDistanceCmp();
		}
		else if (NumberComparator.compareU(getComparator(),NumberComparator.BETWEEN))
		{
			rightDistanceMin=getRightDistanceBetweenMin();
		}
		return rightDistanceMin;
	}
	
	public int getRightDistanceMax()
	{
		int rightDistanceMax=0;
		if (NumberComparator.compareU(getComparator(),NumberComparator.EQUAL))
		{
			rightDistanceMax=getRightDistanceCmp();
		}
		else if (NumberComparator.compareU(getComparator(),NumberComparator.LESS))
		{
			rightDistanceMax=getRightDistanceCmp()-1;
		}
		else if (NumberComparator.compareU(getComparator(),NumberComparator.LESS_EQUAL))
		{
			rightDistanceMax=getRightDistanceCmp();
		}
		else if (NumberComparator.compareU(getComparator(),NumberComparator.BETWEEN))
		{
			rightDistanceMax=getRightDistanceBetweenMax();
		}
		else
		{
			rightDistanceMax=getMaxRightDistanceValue();
		}
		return rightDistanceMax;
	}
	
	public boolean isBetween()
	{
		return NumberComparator.compareU(getComparator(),NumberComparator.BETWEEN);
	}
	
	public int getMaxRightDistanceValue()
	{
		int selectableRightAnswers=question.getNumberOfSelectableRightAnswers();
		int selectableWrongAnswers=question.getNumberOfSelectableWrongAnswers();
		return selectableRightAnswers>=selectableWrongAnswers?selectableRightAnswers:selectableWrongAnswers;
	}
	
	public int getMinValueRightDistanceCmp()
	{
		int minValue=0;
		if (NumberComparator.compareU(getComparator(),NumberComparator.LESS))
		{
			minValue=1;
		}
		return minValue;
	}
	
	public int getMaxValueRightDistanceCmp()
	{
		int maxValue=getMaxRightDistanceValue();
		if (NumberComparator.compareU(getComparator(),NumberComparator.GREATER))
		{
			if (maxValue>0)
			{
				maxValue--;
			}
		}
		else if (NumberComparator.compareU(getComparator(),NumberComparator.LESS) || 
			NumberComparator.compareU(getComparator(),NumberComparator.LESS_EQUAL))
		{
			maxValue++;
		}
		return maxValue;
	}
	
	public void changeComparator(AjaxBehaviorEvent event)
	{
		comparator=(String)((SelectOneMenu)event.getComponent()).getValue();
		if (NumberComparator.compareU(getComparator(),NumberComparator.BETWEEN))
		{
			if (getRightDistanceCmp()<0)
			{
				setRightDistanceBetweenMin(0);
			}
			else
			{
				int maxValue=getMaxRightDistanceValue();
				if (getRightDistanceCmp()>maxValue)
				{
					setRightDistanceBetweenMin(maxValue);
				}
				else
				{
					setRightDistanceBetweenMin(getRightDistanceCmp());
				}
			}
			if (getRightDistanceBetweenMax()<getRightDistanceBetweenMin())
			{
				setRightDistanceBetweenMax(getRightDistanceMin());
			}
		}
		else if (NumberComparator.compareU(oldComparator,NumberComparator.BETWEEN))
		{
			int minValueCmp=getMinValueRightDistanceCmp();
			if (getRightDistanceBetweenMin()<minValueCmp)
			{
				setRightDistanceCmp(minValueCmp);
			}
			else
			{
				int maxValueCmp=getMaxValueRightDistanceCmp();
				if (getRightDistanceCmp()>maxValueCmp)
				{
					setRightDistanceCmp(maxValueCmp);
				}
				else
				{
					setRightDistanceCmp(getRightDistanceMin());
				}
			}
		}
		else
		{
			int minValueCmp=getMinValueRightDistanceCmp();
			if (getRightDistanceCmp()<minValueCmp)
			{
				setRightDistanceCmp(minValueCmp);
			}
			else
			{
				int maxValueCmp=getMaxValueRightDistanceCmp();
				if (getRightDistanceCmp()>maxValueCmp)
				{
					setRightDistanceCmp(maxValueCmp);
				}
			}
		}
	}
}
