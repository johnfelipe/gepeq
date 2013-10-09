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
public class UnselectedRightAnswersConditionBean extends ConditionBean implements Serializable
{
	public static final String TYPE="CONDITION_TYPE_UNSELECTED_RIGHT_ANSWERS";
	
	private QuestionBean question;
	private String oldComparator;
	private String comparator;
	private int unselectedRightAnswersCmp;
	private int unselectedRightAnswersBetweenMin;
	private int unselectedRightAnswersBetweenMax;
	
	public UnselectedRightAnswersConditionBean(QuestionBean question)
	{
		this(question,1,1,false);
	}
	
	public UnselectedRightAnswersConditionBean(QuestionBean question,boolean genF)
	{
		this(question,1,1,genF);
	}
	
	public UnselectedRightAnswersConditionBean(QuestionBean question,int minUnselectedRightAnswers,
		int maxUnselectedRightAnswers)
	{
		this(question,minUnselectedRightAnswers,maxUnselectedRightAnswers,false);
	}
	
	public UnselectedRightAnswersConditionBean(QuestionBean question,int minUnselectedRightAnswers,
		int maxUnselectedRightAnswers,boolean genF)
	{
		super(TYPE);
		this.question=question;
		oldComparator=null;
		int selectableRightAnswers=getMaxUnselectedRightAnswersValue();
		unselectedRightAnswersCmp=1;
		unselectedRightAnswersBetweenMin=1;
		unselectedRightAnswersBetweenMax=selectableRightAnswers;
		if (minUnselectedRightAnswers==maxUnselectedRightAnswers)
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
			unselectedRightAnswersCmp=minUnselectedRightAnswers;
		}
		else if (minUnselectedRightAnswers>0)
		{
			if (maxUnselectedRightAnswers<selectableRightAnswers)
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
				unselectedRightAnswersBetweenMin=minUnselectedRightAnswers;
				unselectedRightAnswersBetweenMax=maxUnselectedRightAnswers;
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
				unselectedRightAnswersCmp=minUnselectedRightAnswers;
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
			unselectedRightAnswersCmp=maxUnselectedRightAnswers;
		}
	}
	
	public UnselectedRightAnswersConditionBean(
		UnselectedRightAnswersConditionBean otherUnselectedRightAnswersCondition)
	{
		super(TYPE);
		question=otherUnselectedRightAnswersCondition.question;
		oldComparator=otherUnselectedRightAnswersCondition.oldComparator;
		comparator=otherUnselectedRightAnswersCondition.comparator;
		unselectedRightAnswersCmp=otherUnselectedRightAnswersCondition.unselectedRightAnswersCmp;
		unselectedRightAnswersBetweenMin=otherUnselectedRightAnswersCondition.unselectedRightAnswersBetweenMin;
		unselectedRightAnswersBetweenMax=otherUnselectedRightAnswersCondition.unselectedRightAnswersBetweenMax;
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
	
	public int getUnselectedRightAnswersCmp()
	{
		return unselectedRightAnswersCmp;
	}
	
	public void setUnselectedRightAnswersCmp(int unselectedRightAnswersCmp)
	{
		this.unselectedRightAnswersCmp=unselectedRightAnswersCmp;
	}
	
	public int getUnselectedRightAnswersBetweenMin()
	{
		return unselectedRightAnswersBetweenMin;
	}
	
	public void setUnselectedRightAnswersBetweenMin(int unselectedRightAnswersBetweenMin)
	{
		this.unselectedRightAnswersBetweenMin=unselectedRightAnswersBetweenMin;
	}
	
	public int getUnselectedRightAnswersBetweenMax()
	{
		return unselectedRightAnswersBetweenMax;
	}
	
	public void setUnselectedRightAnswersBetweenMax(int unselectedRightAnswersBetweenMax)
	{
		this.unselectedRightAnswersBetweenMax=unselectedRightAnswersBetweenMax;
	}
	
	public int getUnselectedRightAnswersMin()
	{
		int unselectedRightAnswersMin=0;
		if (NumberComparator.compareU(getComparator(),NumberComparator.EQUAL))
		{
			unselectedRightAnswersMin=getUnselectedRightAnswersCmp();
		}
		else if (NumberComparator.compareU(getComparator(),NumberComparator.GREATER))
		{
			unselectedRightAnswersMin=getUnselectedRightAnswersCmp()+1;
		}
		else if (NumberComparator.compareU(getComparator(),NumberComparator.GREATER_EQUAL))
		{
			unselectedRightAnswersMin=getUnselectedRightAnswersCmp();
		}
		else if (NumberComparator.compareU(getComparator(),NumberComparator.BETWEEN))
		{
			unselectedRightAnswersMin=getUnselectedRightAnswersBetweenMin();
		}
		return unselectedRightAnswersMin;
	}
	
	public int getUnselectedRightAnswersMax()
	{
		int unselectedRightAnswersMax=0;
		if (NumberComparator.compareU(getComparator(),NumberComparator.EQUAL))
		{
			unselectedRightAnswersMax=getUnselectedRightAnswersCmp();
		}
		else if (NumberComparator.compareU(getComparator(),NumberComparator.LESS))
		{
			unselectedRightAnswersMax=getUnselectedRightAnswersCmp()-1;
		}
		else if (NumberComparator.compareU(getComparator(),NumberComparator.LESS_EQUAL))
		{
			unselectedRightAnswersMax=getUnselectedRightAnswersCmp();
		}
		else if (NumberComparator.compareU(getComparator(),NumberComparator.BETWEEN))
		{
			unselectedRightAnswersMax=getUnselectedRightAnswersBetweenMax();
		}
		else
		{
			unselectedRightAnswersMax=getMaxUnselectedRightAnswersValue();
		}
		return unselectedRightAnswersMax;
	}
	
	public boolean isBetween()
	{
		return NumberComparator.compareU(getComparator(),NumberComparator.BETWEEN);
	}
	
	public int getMaxUnselectedRightAnswersValue()
	{
		return question.getNumberOfSelectableRightAnswers();
	}
	
	public int getMinValueUnselectedRightAnswersCmp()
	{
		int minValue=0;
		if (NumberComparator.compareU(getComparator(),NumberComparator.LESS))
		{
			minValue=1;
		}
		return minValue;
	}
	
	public int getMaxValueUnselectedRightAnswersCmp()
	{
		int maxValue=getMaxUnselectedRightAnswersValue();
		if (NumberComparator.compareU(getComparator(),NumberComparator.GREATER))
		{
			if (maxValue>0)
			{
				maxValue--;
			}
		}
		else if (NumberComparator.compareU(getComparator(),NumberComparator.LESS))
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
			if (getUnselectedRightAnswersCmp()<0)
			{
				setUnselectedRightAnswersBetweenMin(0);
			}
			else
			{
				int maxValue=getMaxUnselectedRightAnswersValue();
				if (getUnselectedRightAnswersCmp()>maxValue)
				{
					setUnselectedRightAnswersBetweenMin(maxValue);
				}
				else
				{
					setUnselectedRightAnswersBetweenMin(getUnselectedRightAnswersCmp());
				}
			}
			if (getUnselectedRightAnswersBetweenMax()<getUnselectedRightAnswersBetweenMin())
			{
				setUnselectedRightAnswersBetweenMax(getUnselectedRightAnswersBetweenMin());
			}
		}
		else if (NumberComparator.compareU(oldComparator,NumberComparator.BETWEEN))
		{
			int minValueCmp=getMinValueUnselectedRightAnswersCmp();
			if (getUnselectedRightAnswersBetweenMin()<minValueCmp)
			{
				setUnselectedRightAnswersCmp(minValueCmp);
			}
			else
			{
				int maxValueCmp=getMaxValueUnselectedRightAnswersCmp();
				if (getUnselectedRightAnswersBetweenMin()>maxValueCmp)
				{
					setUnselectedRightAnswersCmp(maxValueCmp);
				}
				else
				{
					setUnselectedRightAnswersCmp(getUnselectedRightAnswersBetweenMin());
				}
			}
		}
		else
		{
			int minValueCmp=getMinValueUnselectedRightAnswersCmp();
			if (getUnselectedRightAnswersCmp()<minValueCmp)
			{
				setUnselectedRightAnswersCmp(minValueCmp);
			}
			else
			{
				int maxValueCmp=getMaxValueUnselectedRightAnswersCmp();
				if (getUnselectedRightAnswersCmp()>maxValueCmp)
				{
					setUnselectedRightAnswersCmp(maxValueCmp);
				}
			}
		}
	}
}
