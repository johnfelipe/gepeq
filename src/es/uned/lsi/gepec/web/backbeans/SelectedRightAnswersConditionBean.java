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
public class SelectedRightAnswersConditionBean extends ConditionBean implements Serializable
{
	public static final String TYPE="CONDITION_TYPE_SELECTED_RIGHT_ANSWERS";
	
	private QuestionBean question;
	private String oldComparator;
	private String comparator;
	private int selectedRightAnswersCmp;
	private int selectedRightAnswersBetweenMin;
	private int selectedRightAnswersBetweenMax;
	
	public SelectedRightAnswersConditionBean(QuestionBean question)
	{
		this(question,1,1,false);
	}
	
	public SelectedRightAnswersConditionBean(QuestionBean question,boolean genF)
	{
		this(question,1,1,genF);
	}
	
	public SelectedRightAnswersConditionBean(QuestionBean question,int minSelectedRightAnswers,
		int maxSelectedRightAnswers)
	{
		this(question,minSelectedRightAnswers,maxSelectedRightAnswers,false);
	}
	
	public SelectedRightAnswersConditionBean(QuestionBean question,int minSelectedRightAnswers,
		int maxSelectedRightAnswers,boolean genF)
	{
		super(TYPE);
		this.question=question;
		oldComparator=null;
		int selectableRightAnswers=getMaxSelectedRightAnswersValue();
		selectedRightAnswersCmp=1;
		selectedRightAnswersBetweenMin=1;
		selectedRightAnswersBetweenMax=selectableRightAnswers;
		if (minSelectedRightAnswers==maxSelectedRightAnswers)
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
			selectedRightAnswersCmp=minSelectedRightAnswers;
		}
		else if (minSelectedRightAnswers>0)
		{
			if (maxSelectedRightAnswers<selectableRightAnswers)
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
				selectedRightAnswersBetweenMin=minSelectedRightAnswers;
				selectedRightAnswersBetweenMax=maxSelectedRightAnswers;
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
				selectedRightAnswersCmp=minSelectedRightAnswers;
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
			selectedRightAnswersCmp=maxSelectedRightAnswers;
		}
	}
	
	public SelectedRightAnswersConditionBean(SelectedRightAnswersConditionBean otherSelectedRightAnswersCondition)
	{
		super(TYPE);
		question=otherSelectedRightAnswersCondition.question;
		oldComparator=otherSelectedRightAnswersCondition.oldComparator;
		comparator=otherSelectedRightAnswersCondition.comparator;
		selectedRightAnswersCmp=otherSelectedRightAnswersCondition.selectedRightAnswersCmp;
		selectedRightAnswersBetweenMin=otherSelectedRightAnswersCondition.selectedRightAnswersBetweenMin;
		selectedRightAnswersBetweenMax=otherSelectedRightAnswersCondition.selectedRightAnswersBetweenMax;
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
	
	public int getSelectedRightAnswersCmp()
	{
		return selectedRightAnswersCmp;
	}
	
	public void setSelectedRightAnswersCmp(int selectedRightAnswersCmp)
	{
		this.selectedRightAnswersCmp=selectedRightAnswersCmp;
	}
	
	public int getSelectedRightAnswersBetweenMin()
	{
		return selectedRightAnswersBetweenMin;
	}
	
	public void setSelectedRightAnswersBetweenMin(int selectedRightAnswersBetweenMin)
	{
		this.selectedRightAnswersBetweenMin=selectedRightAnswersBetweenMin;
	}
	
	public int getSelectedRightAnswersBetweenMax()
	{
		return selectedRightAnswersBetweenMax;
	}
	
	public void setSelectedRightAnswersBetweenMax(int selectedRightAnswersBetweenMax)
	{
		this.selectedRightAnswersBetweenMax=selectedRightAnswersBetweenMax;
	}
	
	public int getSelectedRightAnswersMin()
	{
		int selectedRightAnswersMin=0;
		if (NumberComparator.compareU(getComparator(),NumberComparator.EQUAL))
		{
			selectedRightAnswersMin=getSelectedRightAnswersCmp();
		}
		else if (NumberComparator.compareU(getComparator(),NumberComparator.GREATER))
		{
			selectedRightAnswersMin=getSelectedRightAnswersCmp()+1;
		}
		else if (NumberComparator.compareU(getComparator(),NumberComparator.GREATER_EQUAL))
		{
			selectedRightAnswersMin=getSelectedRightAnswersCmp();
		}
		else if (NumberComparator.compareU(getComparator(),NumberComparator.BETWEEN))
		{
			selectedRightAnswersMin=getSelectedRightAnswersBetweenMin();
		}
		return selectedRightAnswersMin;
	}
	
	public int getSelectedRightAnswersMax()
	{
		int selectedRightAnswersMax=0;
		if (NumberComparator.compareU(getComparator(),NumberComparator.EQUAL))
		{
			selectedRightAnswersMax=getSelectedRightAnswersCmp();
		}
		else if (NumberComparator.compareU(getComparator(),NumberComparator.LESS))
		{
			selectedRightAnswersMax=getSelectedRightAnswersCmp()-1;
		}
		else if (NumberComparator.compareU(getComparator(),NumberComparator.LESS_EQUAL))
		{
			selectedRightAnswersMax=getSelectedRightAnswersCmp();
		}
		else if (NumberComparator.compareU(getComparator(),NumberComparator.BETWEEN))
		{
			selectedRightAnswersMax=getSelectedRightAnswersBetweenMax();
		}
		else
		{
			selectedRightAnswersMax=getMaxSelectedRightAnswersValue();
		}
		return selectedRightAnswersMax;
	}
	
	public boolean isBetween()
	{
		return NumberComparator.compareU(getComparator(),NumberComparator.BETWEEN);
	}
	
	public int getMaxSelectedRightAnswersValue()
	{
		return question.getNumberOfSelectableRightAnswers();
	}
	
	public int getMinValueSelectedRightAnswersCmp()
	{
		int minValue=0;
		if (NumberComparator.compareU(getComparator(),NumberComparator.LESS))
		{
			minValue=1;
		}
		return minValue;
	}
	
	public int getMaxValueSelectedRightAnswersCmp()
	{
		int maxValue=getMaxSelectedRightAnswersValue();
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
			if (getSelectedRightAnswersCmp()<0)
			{
				setSelectedRightAnswersBetweenMin(0);
			}
			else
			{
				int maxValue=getMaxSelectedRightAnswersValue();
				if (getSelectedRightAnswersCmp()>maxValue)
				{
					setSelectedRightAnswersBetweenMin(maxValue);
				}
				else
				{
					setSelectedRightAnswersBetweenMin(getSelectedRightAnswersCmp());
				}
			}
			if (getSelectedRightAnswersBetweenMax()<getSelectedRightAnswersBetweenMin())
			{
				setSelectedRightAnswersBetweenMax(getSelectedRightAnswersBetweenMin());
			}
		}
		else if (NumberComparator.compareU(oldComparator,NumberComparator.BETWEEN))
		{
			int minValueCmp=getMinValueSelectedRightAnswersCmp();
			if (getSelectedRightAnswersBetweenMin()<minValueCmp)
			{
				setSelectedRightAnswersCmp(minValueCmp);
			}
			else
			{
				int maxValueCmp=getMaxValueSelectedRightAnswersCmp();
				if (getSelectedRightAnswersBetweenMin()>maxValueCmp)
				{
					setSelectedRightAnswersCmp(maxValueCmp);
				}
				else
				{
					setSelectedRightAnswersCmp(getSelectedRightAnswersBetweenMin());
				}
			}
		}
		else
		{
			int minValueCmp=getMinValueSelectedRightAnswersCmp();
			if (getSelectedRightAnswersCmp()<minValueCmp)
			{
				setSelectedRightAnswersCmp(minValueCmp);
			}
			else
			{
				int maxValueCmp=getMaxValueSelectedRightAnswersCmp();
				if (getSelectedRightAnswersCmp()>maxValueCmp)
				{
					setSelectedRightAnswersCmp(maxValueCmp);
				}
			}
		}
	}
}
