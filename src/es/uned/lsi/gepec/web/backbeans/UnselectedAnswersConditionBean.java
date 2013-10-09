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
public class UnselectedAnswersConditionBean extends ConditionBean implements Serializable
{
	public static final String TYPE="CONDITION_TYPE_UNSELECTED_ANSWERS";
	
	private QuestionBean question;
	private String oldComparator;
	private String comparator;
	private int unselectedAnswersCmp;
	private int unselectedAnswersBetweenMin;
	private int unselectedAnswersBetweenMax;
	
	public UnselectedAnswersConditionBean(QuestionBean question)
	{
		this(question,1,1,false);
	}
	
	public UnselectedAnswersConditionBean(QuestionBean question,boolean genF)
	{
		this(question,1,1,genF);
	}
	
	public UnselectedAnswersConditionBean(QuestionBean question,int minUnselectedAnswers,
		int maxUnselectedAnswers)
	{
		this(question,minUnselectedAnswers,maxUnselectedAnswers,false);
	}
	
	public UnselectedAnswersConditionBean(QuestionBean question,int minUnselectedAnswers,
		int maxUnselectedAnswers,boolean genF)
	{
		super(TYPE);
		this.question=question;
		oldComparator=null;
		int selectableAnswers=getMaxUnselectedAnswersValue();
		unselectedAnswersCmp=1;
		unselectedAnswersBetweenMin=1;
		unselectedAnswersBetweenMax=selectableAnswers;
		if (minUnselectedAnswers==maxUnselectedAnswers)
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
			unselectedAnswersCmp=minUnselectedAnswers;
		}
		else if (minUnselectedAnswers>0)
		{
			if (maxUnselectedAnswers<selectableAnswers)
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
				unselectedAnswersBetweenMin=minUnselectedAnswers;
				unselectedAnswersBetweenMax=maxUnselectedAnswers;
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
				unselectedAnswersCmp=minUnselectedAnswers;
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
			unselectedAnswersCmp=maxUnselectedAnswers;
		}
	}
	
	public UnselectedAnswersConditionBean(UnselectedAnswersConditionBean otherUnselectedAnswersCondition)
	{
		super(TYPE);
		question=otherUnselectedAnswersCondition.question;
		oldComparator=otherUnselectedAnswersCondition.oldComparator;
		comparator=otherUnselectedAnswersCondition.comparator;
		unselectedAnswersCmp=otherUnselectedAnswersCondition.unselectedAnswersCmp;
		unselectedAnswersBetweenMin=otherUnselectedAnswersCondition.unselectedAnswersBetweenMin;
		unselectedAnswersBetweenMax=otherUnselectedAnswersCondition.unselectedAnswersBetweenMax;
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
	
	public int getUnselectedAnswersCmp()
	{
		return unselectedAnswersCmp;
	}
	
	public void setUnselectedAnswersCmp(int unselectedAnswersCmp)
	{
		this.unselectedAnswersCmp=unselectedAnswersCmp;
	}
	
	public int getUnselectedAnswersBetweenMin()
	{
		return unselectedAnswersBetweenMin;
	}
	
	public void setUnselectedAnswersBetweenMin(int unselectedAnswersBetweenMin)
	{
		this.unselectedAnswersBetweenMin=unselectedAnswersBetweenMin;
	}
	
	public int getUnselectedAnswersBetweenMax()
	{
		return unselectedAnswersBetweenMax;
	}
	
	public void setUnselectedAnswersBetweenMax(int unselectedAnswersBetweenMax)
	{
		this.unselectedAnswersBetweenMax=unselectedAnswersBetweenMax;
	}
	
	public int getUnselectedAnswersMin()
	{
		int unselectedAnswersMin=0;
		if (NumberComparator.compareU(getComparator(),NumberComparator.EQUAL))
		{
			unselectedAnswersMin=getUnselectedAnswersCmp();
		}
		else if (NumberComparator.compareU(getComparator(),NumberComparator.GREATER))
		{
			unselectedAnswersMin=getUnselectedAnswersCmp()+1;
		}
		else if (NumberComparator.compareU(getComparator(),NumberComparator.GREATER_EQUAL))
		{
			unselectedAnswersMin=getUnselectedAnswersCmp();
		}
		else if (NumberComparator.compareU(getComparator(),NumberComparator.BETWEEN))
		{
			unselectedAnswersMin=getUnselectedAnswersMin();
		}
		return unselectedAnswersMin;
	}
	
	public int getUnselectedAnswersMax()
	{
		int unselectedAnswersMax=0;
		if (NumberComparator.compareU(getComparator(),NumberComparator.EQUAL))
		{
			unselectedAnswersMax=getUnselectedAnswersCmp();
		}
		else if (NumberComparator.compareU(getComparator(),NumberComparator.LESS))
		{
			unselectedAnswersMax=getUnselectedAnswersCmp()-1;
		}
		else if (NumberComparator.compareU(getComparator(),NumberComparator.LESS_EQUAL))
		{
			unselectedAnswersMax=getUnselectedAnswersCmp();
		}
		else if (NumberComparator.compareU(getComparator(),NumberComparator.BETWEEN))
		{
			unselectedAnswersMax=getUnselectedAnswersBetweenMax();
		}
		else
		{
			unselectedAnswersMax=getMaxUnselectedAnswersValue();
		}
		return unselectedAnswersMax;
	}
	
	public boolean isBetween()
	{
		return NumberComparator.compareU(getComparator(),NumberComparator.BETWEEN);
	}
	
	public int getMaxUnselectedAnswersValue()
	{
		return question.getNumberOfSelectableAnswers();
	}
	
	public int getMinValueUnselectedAnswersCmp()
	{
		int minValue=0;
		if (NumberComparator.compareU(getComparator(),NumberComparator.LESS))
		{
			minValue=1;
		}
		return minValue;
	}
	
	public int getMaxValueUnselectedAnswersCmp()
	{
		int maxValue=getMaxUnselectedAnswersValue();
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
			if (getUnselectedAnswersCmp()<0)
			{
				setUnselectedAnswersBetweenMin(0);
			}
			else
			{
				int maxValue=getMaxUnselectedAnswersValue();
				if (getUnselectedAnswersCmp()>maxValue)
				{
					setUnselectedAnswersBetweenMin(maxValue);
				}
				else
				{
					setUnselectedAnswersBetweenMin(getUnselectedAnswersCmp());
				}
			}
			if (getUnselectedAnswersBetweenMax()<getUnselectedAnswersBetweenMin())
			{
				setUnselectedAnswersBetweenMax(getUnselectedAnswersBetweenMin());
			}
		}
		else if (NumberComparator.compareU(oldComparator,NumberComparator.BETWEEN))
		{
			int minValueCmp=getMinValueUnselectedAnswersCmp();
			if (getUnselectedAnswersBetweenMin()<minValueCmp)
			{
				setUnselectedAnswersCmp(minValueCmp);
			}
			else
			{
				int maxValueCmp=getMaxValueUnselectedAnswersCmp();
				if (getUnselectedAnswersBetweenMin()>maxValueCmp)
				{
					setUnselectedAnswersCmp(maxValueCmp);
				}
				else
				{
					setUnselectedAnswersCmp(getUnselectedAnswersBetweenMin());
				}
			}
		}
		else
		{
			int minValueCmp=getMinValueUnselectedAnswersCmp();
			if (getUnselectedAnswersCmp()<minValueCmp)
			{
				setUnselectedAnswersCmp(minValueCmp);
			}
			else
			{
				int maxValueCmp=getMaxValueUnselectedAnswersCmp();
				if (getUnselectedAnswersCmp()>maxValueCmp)
				{
					setUnselectedAnswersCmp(maxValueCmp);
				}
			}
		}
	}
}
