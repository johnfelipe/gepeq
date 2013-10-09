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
public class SelectedWrongAnswersConditionBean extends ConditionBean implements Serializable
{
	public static final String TYPE="CONDITION_TYPE_SELECTED_WRONG_ANSWERS";
	
	private QuestionBean question;
	private String oldComparator;
	private String comparator;
	private int selectedWrongAnswersCmp;
	private int selectedWrongAnswersBetweenMin;
	private int selectedWrongAnswersBetweenMax;
	
	public SelectedWrongAnswersConditionBean(QuestionBean question)
	{
		this(question,1,1,false);
	}
	
	public SelectedWrongAnswersConditionBean(QuestionBean question,boolean genF)
	{
		this(question,1,1,genF);
	}
	
	public SelectedWrongAnswersConditionBean(QuestionBean question,int minSelectedWrongAnswers,
		int maxSelectedWrongAnswers)
	{
		this(question,minSelectedWrongAnswers,maxSelectedWrongAnswers,false);
	}
	
	public SelectedWrongAnswersConditionBean(QuestionBean question,int minSelectedWrongAnswers,
		int maxSelectedWrongAnswers,boolean genF)
	{
		super(TYPE);
		this.question=question;
		oldComparator=null;
		int selectableWrongAnswers=getMaxSelectedWrongAnswersValue();
		selectedWrongAnswersCmp=1;
		selectedWrongAnswersBetweenMin=1;
		selectedWrongAnswersBetweenMax=selectableWrongAnswers;
		if (minSelectedWrongAnswers==maxSelectedWrongAnswers)
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
			selectedWrongAnswersCmp=minSelectedWrongAnswers;
		}
		else if (minSelectedWrongAnswers>0)
		{
			if (maxSelectedWrongAnswers<selectableWrongAnswers)
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
				selectedWrongAnswersBetweenMin=minSelectedWrongAnswers;
				selectedWrongAnswersBetweenMax=maxSelectedWrongAnswers;
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
				selectedWrongAnswersCmp=minSelectedWrongAnswers;
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
			selectedWrongAnswersCmp=maxSelectedWrongAnswers;
		}
	}
	
	
	public SelectedWrongAnswersConditionBean(SelectedWrongAnswersConditionBean otherSelectedWrongAnswersCondition)
	{
		super(TYPE);
		question=otherSelectedWrongAnswersCondition.question;
		oldComparator=otherSelectedWrongAnswersCondition.oldComparator;
		comparator=otherSelectedWrongAnswersCondition.comparator;
		selectedWrongAnswersCmp=otherSelectedWrongAnswersCondition.selectedWrongAnswersCmp;
		selectedWrongAnswersBetweenMin=otherSelectedWrongAnswersCondition.selectedWrongAnswersBetweenMin;
		selectedWrongAnswersBetweenMax=otherSelectedWrongAnswersCondition.selectedWrongAnswersBetweenMax;
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
	
	public int getSelectedWrongAnswersCmp()
	{
		return selectedWrongAnswersCmp;
	}
	
	public void setSelectedWrongAnswersCmp(int selectedWrongAnswersCmp)
	{
		this.selectedWrongAnswersCmp=selectedWrongAnswersCmp;
	}
	
	public int getSelectedWrongAnswersBetweenMin()
	{
		return selectedWrongAnswersBetweenMin;
	}
	
	public void setSelectedWrongAnswersBetweenMin(int selectedWrongAnswersBetweenMin)
	{
		this.selectedWrongAnswersBetweenMin=selectedWrongAnswersBetweenMin;
	}
	
	public int getSelectedWrongAnswersBetweenMax()
	{
		return selectedWrongAnswersBetweenMax;
	}
	
	public void setSelectedWrongAnswersBetweenMax(int selectedWrongAnswersBetweenMax)
	{
		this.selectedWrongAnswersBetweenMax=selectedWrongAnswersBetweenMax;
	}
	
	public int getSelectedWrongAnswersMin()
	{
		int selectedWrongAnswersMin=0;
		if (NumberComparator.compareU(getComparator(),NumberComparator.EQUAL))
		{
			selectedWrongAnswersMin=getSelectedWrongAnswersCmp();
		}
		else if (NumberComparator.compareU(getComparator(),NumberComparator.GREATER))
		{
			selectedWrongAnswersMin=getSelectedWrongAnswersCmp()+1;
		}
		else if (NumberComparator.compareU(getComparator(),NumberComparator.GREATER_EQUAL))
		{
			selectedWrongAnswersMin=getSelectedWrongAnswersCmp();
		}
		else if (NumberComparator.compareU(getComparator(),NumberComparator.BETWEEN))
		{
			selectedWrongAnswersMin=getSelectedWrongAnswersBetweenMin();
		}
		return selectedWrongAnswersMin;
	}
	
	public int getSelectedWrongAnswersMax()
	{
		int selectedWrongAnswersMax=0;
		if (NumberComparator.compareU(getComparator(),NumberComparator.EQUAL))
		{
			selectedWrongAnswersMax=getSelectedWrongAnswersCmp();
		}
		else if (NumberComparator.compareU(getComparator(),NumberComparator.LESS))
		{
			selectedWrongAnswersMax=getSelectedWrongAnswersCmp()-1;
		}
		else if (NumberComparator.compareU(getComparator(),NumberComparator.LESS_EQUAL))
		{
			selectedWrongAnswersMax=getSelectedWrongAnswersCmp();
		}
		else if (NumberComparator.compareU(getComparator(),NumberComparator.BETWEEN))
		{
			selectedWrongAnswersMax=getSelectedWrongAnswersBetweenMax();
		}
		else
		{
			selectedWrongAnswersMax=getMaxSelectedWrongAnswersValue();
		}
		return selectedWrongAnswersMax;
	}
	
	public boolean isBetween()
	{
		return NumberComparator.compareU(getComparator(),NumberComparator.BETWEEN);
	}
	
	public int getMaxSelectedWrongAnswersValue()
	{
		return question.getNumberOfSelectableWrongAnswers();
	}
	
	public int getMinValueSelectedWrongAnswersCmp()
	{
		int minValue=0;
		if (NumberComparator.compareU(getComparator(),NumberComparator.LESS))
		{
			minValue=1;
		}
		return minValue;
	}
	
	public int getMaxValueSelectedWrongAnswersCmp()
	{
		int maxValue=getMaxSelectedWrongAnswersValue();
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
			if (getSelectedWrongAnswersCmp()<0)
			{
				setSelectedWrongAnswersBetweenMin(0);
			}
			else
			{
				int maxValue=getMaxSelectedWrongAnswersValue();
				if (getSelectedWrongAnswersCmp()>maxValue)
				{
					setSelectedWrongAnswersBetweenMin(maxValue);
				}
				else
				{
					setSelectedWrongAnswersBetweenMin(getSelectedWrongAnswersBetweenMax());
				}
			}
			if (getSelectedWrongAnswersBetweenMax()<getSelectedWrongAnswersBetweenMin())
			{
				setSelectedWrongAnswersBetweenMax(getSelectedWrongAnswersBetweenMin());
			}
		}
		else if (NumberComparator.compareU(oldComparator,NumberComparator.BETWEEN))
		{
			int minValueCmp=getMinValueSelectedWrongAnswersCmp();
			if (getSelectedWrongAnswersBetweenMin()<minValueCmp)
			{
				setSelectedWrongAnswersCmp(minValueCmp);
			}
			else
			{
				int maxValueCmp=getMaxValueSelectedWrongAnswersCmp();
				if (getSelectedWrongAnswersBetweenMin()>maxValueCmp)
				{
					setSelectedWrongAnswersCmp(maxValueCmp);
				}
				else
				{
					setSelectedWrongAnswersCmp(getSelectedWrongAnswersBetweenMin());
				}
			}
		}
		else
		{
			int minValueCmp=getMinValueSelectedWrongAnswersCmp();
			if (getSelectedWrongAnswersCmp()<minValueCmp)
			{
				setSelectedWrongAnswersCmp(minValueCmp);
			}
			else
			{
				int maxValueCmp=getMaxValueSelectedWrongAnswersCmp();
				if (getSelectedWrongAnswersCmp()>maxValueCmp)
				{
					setSelectedWrongAnswersCmp(maxValueCmp);
				}
			}
		}
	}
}
