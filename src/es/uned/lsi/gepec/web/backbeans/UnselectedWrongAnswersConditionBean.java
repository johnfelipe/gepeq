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
public class UnselectedWrongAnswersConditionBean extends ConditionBean implements Serializable
{
	public static final String TYPE="CONDITION_TYPE_UNSELECTED_WRONG_ANSWERS";
	
	private QuestionBean question;
	private String oldComparator;
	private String comparator;
	private int unselectedWrongAnswersCmp;
	private int unselectedWrongAnswersBetweenMin;
	private int unselectedWrongAnswersBetweenMax;
	
	public UnselectedWrongAnswersConditionBean(QuestionBean question)
	{
		this(question,1,1,false);
	}
	
	public UnselectedWrongAnswersConditionBean(QuestionBean question,boolean genF)
	{
		this(question,1,1,genF);
	}
	
	public UnselectedWrongAnswersConditionBean(QuestionBean question,int minUnselectedWrongAnswers,
		int maxUnselectedWrongAnswers)
	{
		this(question,minUnselectedWrongAnswers,maxUnselectedWrongAnswers,false);
	}
	
	public UnselectedWrongAnswersConditionBean(QuestionBean question,int minUnselectedWrongAnswers,
		int maxUnselectedWrongAnswers,boolean genF)
	{
		super(TYPE);
		this.question=question;
		oldComparator=null;
		int selectableWrongAnswers=getMaxUnselectedWrongAnswersValue();
		unselectedWrongAnswersCmp=1;
		unselectedWrongAnswersBetweenMin=1;
		unselectedWrongAnswersBetweenMax=selectableWrongAnswers;
		if (minUnselectedWrongAnswers==maxUnselectedWrongAnswers)
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
			unselectedWrongAnswersCmp=minUnselectedWrongAnswers;
		}
		else if (minUnselectedWrongAnswers>0)
		{
			if (maxUnselectedWrongAnswers<selectableWrongAnswers)
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
				unselectedWrongAnswersBetweenMin=minUnselectedWrongAnswers;
				unselectedWrongAnswersBetweenMax=maxUnselectedWrongAnswers;
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
				unselectedWrongAnswersCmp=minUnselectedWrongAnswers;
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
			unselectedWrongAnswersCmp=maxUnselectedWrongAnswers;
		}
	}
	
	public UnselectedWrongAnswersConditionBean(
		UnselectedWrongAnswersConditionBean otherUnselectedWrongAnswersCondition)
	{
		super(TYPE);
		question=otherUnselectedWrongAnswersCondition.question;
		oldComparator=otherUnselectedWrongAnswersCondition.oldComparator;
		comparator=otherUnselectedWrongAnswersCondition.comparator;
		unselectedWrongAnswersCmp=otherUnselectedWrongAnswersCondition.unselectedWrongAnswersCmp;
		unselectedWrongAnswersBetweenMin=otherUnselectedWrongAnswersCondition.unselectedWrongAnswersBetweenMin;
		unselectedWrongAnswersBetweenMax=otherUnselectedWrongAnswersCondition.unselectedWrongAnswersBetweenMax;
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
	
	public int getUnselectedWrongAnswersCmp()
	{
		return unselectedWrongAnswersCmp;
	}
	
	public void setUnselectedWrongAnswersCmp(int unselectedWrongAnswersCmp)
	{
		this.unselectedWrongAnswersCmp=unselectedWrongAnswersCmp;
	}
	
	public int getUnselectedWrongAnswersBetweenMin()
	{
		return unselectedWrongAnswersBetweenMin;
	}
	
	public void setUnselectedWrongAnswersBetweenMin(int unselectedWrongAnswersBetweenMin)
	{
		this.unselectedWrongAnswersBetweenMin=unselectedWrongAnswersBetweenMin;
	}
	
	public int getUnselectedWrongAnswersBetweenMax()
	{
		return unselectedWrongAnswersBetweenMax;
	}
	
	public void setUnselectedWrongAnswersBetweenMax(int unselectedWrongAnswersBetweenMax)
	{
		this.unselectedWrongAnswersBetweenMax=unselectedWrongAnswersBetweenMax;
	}
	
	public int getUnselectedWrongAnswersMin()
	{
		int unselectedWrongAnswersMin=0;
		if (NumberComparator.compareU(getComparator(),NumberComparator.EQUAL))
		{
			unselectedWrongAnswersMin=getUnselectedWrongAnswersCmp();
		}
		else if (NumberComparator.compareU(getComparator(),NumberComparator.GREATER))
		{
			unselectedWrongAnswersMin=getUnselectedWrongAnswersCmp()+1;
		}
		else if (NumberComparator.compareU(getComparator(),NumberComparator.GREATER_EQUAL))
		{
			unselectedWrongAnswersMin=getUnselectedWrongAnswersCmp();
		}
		else if (NumberComparator.compareU(getComparator(),NumberComparator.BETWEEN))
		{
			unselectedWrongAnswersMin=getUnselectedWrongAnswersBetweenMin();
		}
		return unselectedWrongAnswersMin;
	}
	
	public int getUnselectedWrongAnswersMax()
	{
		int unselectedWrongAnswersMax=0;
		if (NumberComparator.compareU(getComparator(),NumberComparator.EQUAL))
		{
			unselectedWrongAnswersMax=getUnselectedWrongAnswersCmp();
		}
		else if (NumberComparator.compareU(getComparator(),NumberComparator.LESS))
		{
			unselectedWrongAnswersMax=getUnselectedWrongAnswersCmp()-1;
		}
		else if (NumberComparator.compareU(getComparator(),NumberComparator.LESS_EQUAL))
		{
			unselectedWrongAnswersMax=getUnselectedWrongAnswersCmp();
		}
		else if (NumberComparator.compareU(getComparator(),NumberComparator.BETWEEN))
		{
			unselectedWrongAnswersMax=getUnselectedWrongAnswersBetweenMax();
		}
		else
		{
			unselectedWrongAnswersMax=getMaxUnselectedWrongAnswersValue();
		}
		return unselectedWrongAnswersMax;
	}
	
	public boolean isBetween()
	{
		return NumberComparator.compareU(getComparator(),NumberComparator.BETWEEN);
	}
	
	public int getMaxUnselectedWrongAnswersValue()
	{
		return question.getNumberOfSelectableWrongAnswers();
	}
	
	public int getMinValueUnselectedWrongAnswersCmp()
	{
		int minValue=0;
		if (NumberComparator.compareU(getComparator(),NumberComparator.LESS))
		{
			minValue=1;
		}
		return minValue;
	}
	
	public int getMaxValueUnselectedWrongAnswersCmp()
	{
		int maxValue=getMaxUnselectedWrongAnswersValue();
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
			if (getUnselectedWrongAnswersCmp()<0)
			{
				setUnselectedWrongAnswersBetweenMin(0);
			}
			else
			{
				int maxValue=getMaxUnselectedWrongAnswersValue();
				if (getUnselectedWrongAnswersCmp()>maxValue)
				{
					setUnselectedWrongAnswersBetweenMin(maxValue);
				}
				else
				{
					setUnselectedWrongAnswersBetweenMin(getUnselectedWrongAnswersCmp());
				}
			}
			if (getUnselectedWrongAnswersBetweenMax()<getUnselectedWrongAnswersBetweenMin())
			{
				setUnselectedWrongAnswersBetweenMax(getUnselectedWrongAnswersBetweenMin());
			}
		}
		else if (NumberComparator.compareU(oldComparator,NumberComparator.BETWEEN))
		{
			int minValueCmp=getMinValueUnselectedWrongAnswersCmp();
			if (getUnselectedWrongAnswersBetweenMin()<minValueCmp)
			{
				setUnselectedWrongAnswersCmp(minValueCmp);
			}
			else
			{
				int maxValueCmp=getMaxValueUnselectedWrongAnswersCmp();
				if (getUnselectedWrongAnswersBetweenMin()>maxValueCmp)
				{
					setUnselectedWrongAnswersCmp(maxValueCmp);
				}
				else
				{
					setUnselectedWrongAnswersCmp(getUnselectedWrongAnswersBetweenMin());
				}
			}
		}
		else
		{
			int minValueCmp=getMinValueUnselectedWrongAnswersCmp();
			if (getUnselectedWrongAnswersCmp()<minValueCmp)
			{
				setUnselectedWrongAnswersCmp(minValueCmp);
			}
			else
			{
				int maxValueCmp=getMaxValueUnselectedWrongAnswersCmp();
				if (getUnselectedWrongAnswersCmp()>maxValueCmp)
				{
					setUnselectedWrongAnswersCmp(maxValueCmp);
				}
			}
		}
	}
}
