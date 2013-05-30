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
public class SelectedAnswersConditionBean extends ConditionBean implements Serializable
{
	public static final String TYPE="CONDITION_TYPE_SELECTED_ANSWERS";
	
	private QuestionBean question;
	private String oldComparator;
	private String comparator;
	private int selectedAnswersCmp;
	private int selectedAnswersBetweenMin;
	private int selectedAnswersBetweenMax;
	
	public SelectedAnswersConditionBean(QuestionBean question)
	{
		this(question,1,1,false);
	}
	
	public SelectedAnswersConditionBean(QuestionBean question,boolean genF)
	{
		this(question,1,1,genF);
	}
	
	public SelectedAnswersConditionBean(QuestionBean question,int minSelectedAnswers,int maxSelectedAnswers)
	{
		this(question,minSelectedAnswers,maxSelectedAnswers,false);
	}
	
	public SelectedAnswersConditionBean(QuestionBean question,int minSelectedAnswers,int maxSelectedAnswers,boolean genF)
	{
		super(TYPE);
		this.question=question;
		oldComparator=null;
		int selectableAnswers=getMaxSelectedAnswersValue(question.getCurrentUserOperation(null));
		selectedAnswersCmp=1;
		selectedAnswersBetweenMin=1;
		selectedAnswersBetweenMax=selectableAnswers;
		if (minSelectedAnswers==maxSelectedAnswers)
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
			selectedAnswersCmp=minSelectedAnswers;
		}
		else if (minSelectedAnswers>0)
		{
			if (maxSelectedAnswers<selectableAnswers)
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
				selectedAnswersBetweenMin=minSelectedAnswers;
				selectedAnswersBetweenMax=maxSelectedAnswers;
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
				selectedAnswersCmp=minSelectedAnswers;
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
			selectedAnswersCmp=maxSelectedAnswers;
		}
	}
	
	public SelectedAnswersConditionBean(SelectedAnswersConditionBean otherSelectedAnswersCondition)
	{
		super(TYPE);
		question=otherSelectedAnswersCondition.question;
		oldComparator=otherSelectedAnswersCondition.oldComparator;
		comparator=otherSelectedAnswersCondition.comparator;
		selectedAnswersCmp=otherSelectedAnswersCondition.selectedAnswersCmp;
		selectedAnswersBetweenMin=otherSelectedAnswersCondition.selectedAnswersBetweenMin;
		selectedAnswersBetweenMax=otherSelectedAnswersCondition.selectedAnswersBetweenMax;
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
	
	public int getSelectedAnswersCmp()
	{
		return selectedAnswersCmp;
	}
	
	public void setSelectedAnswersCmp(int selectedAnswersCmp)
	{
		this.selectedAnswersCmp=selectedAnswersCmp;
	}
	
	public int getSelectedAnswersBetweenMin()
	{
		return selectedAnswersBetweenMin;
	}
	
	public void setSelectedAnswersBetweenMin(int selectedAnswersBetweenMin)
	{
		this.selectedAnswersBetweenMin=selectedAnswersBetweenMin;
	}
	
	public int getSelectedAnswersBetweenMax()
	{
		return selectedAnswersBetweenMax;
	}
	
	public void setSelectedAnswersBetweenMax(int selectedAnswersBetweenMax)
	{
		this.selectedAnswersBetweenMax=selectedAnswersBetweenMax;
	}
	
	public int getSelectedAnswersMin()
	{
		int selectedAnswersMin=0;
		if (NumberComparator.compareU(comparator,NumberComparator.EQUAL))
		{
			selectedAnswersMin=selectedAnswersCmp;
		}
		else if (NumberComparator.compareU(comparator,NumberComparator.GREATER))
		{
			selectedAnswersMin=selectedAnswersCmp+1;
		}
		else if (NumberComparator.compareU(comparator,NumberComparator.GREATER_EQUAL))
		{
			selectedAnswersMin=selectedAnswersCmp;
		}
		else if (NumberComparator.compareU(comparator,NumberComparator.BETWEEN))
		{
			selectedAnswersMin=selectedAnswersBetweenMin;
		}
		return selectedAnswersMin;
	}
	
	public int getSelectedAnswersMax()
	{
		return getSelectedAnswersMax(null);
	}
	
	public int getSelectedAnswersMax(Operation operation)
	{
		int selectedAnswersMax=getMaxSelectedAnswersValue(question.getCurrentUserOperation(operation));
		if (NumberComparator.compareU(comparator,NumberComparator.EQUAL))
		{
			selectedAnswersMax=selectedAnswersCmp;
		}
		else if (NumberComparator.compareU(comparator,NumberComparator.LESS))
		{
			selectedAnswersMax=selectedAnswersCmp-1;
		}
		else if (NumberComparator.compareU(comparator,NumberComparator.LESS_EQUAL))
		{
			selectedAnswersMax=selectedAnswersCmp;
		}
		else if (NumberComparator.compareU(comparator,NumberComparator.BETWEEN))
		{
			selectedAnswersMax=selectedAnswersBetweenMax;
		}
		return selectedAnswersMax;
	}
	
	public boolean isBetween()
	{
		return NumberComparator.compareU(comparator,NumberComparator.BETWEEN);
	}
	
	public int getMaxSelectedAnswersValue()
	{
		return getMaxSelectedAnswersValue(null);
	}
	
	public int getMaxSelectedAnswersValue(Operation operation)
	{
		return question.getNumberOfSelectableAnswers(question.getCurrentUserOperation(operation));
	}
	
	public int getMinValueSelectedAnswersCmp()
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
	
	public int getMaxValueSelectedAnswersCmp()
	{
		return getMaxValueSelectedAnswersCmp(null);
	}
	
	public int getMaxValueSelectedAnswersCmp(Operation operation)
	{
		int maxValue=getMaxSelectedAnswersValue(question.getCurrentUserOperation(operation));
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
			if (selectedAnswersCmp<0)
			{
				selectedAnswersBetweenMin=0;
			}
			else
			{
				selectedAnswersBetweenMin=selectedAnswersCmp;
			}
			if (selectedAnswersBetweenMax<selectedAnswersBetweenMin)
			{
				selectedAnswersBetweenMax=selectedAnswersBetweenMin;
			}
		}
		else if (NumberComparator.compareU(oldComparator,NumberComparator.BETWEEN))
		{
			if (selectedAnswersBetweenMin<getMinValueSelectedAnswersCmp())
			{
				selectedAnswersCmp=getMinValueSelectedAnswersCmp();
			}
			else
			{
				int maxValueSelectedAnswersCmp=getMaxValueSelectedAnswersCmp(operation);
				if (selectedAnswersBetweenMin>maxValueSelectedAnswersCmp)
				{
					selectedAnswersCmp=maxValueSelectedAnswersCmp;
				}
				else
				{
					selectedAnswersCmp=selectedAnswersBetweenMin;
				}
			}
		}
		else
		{
			if (selectedAnswersCmp<getMinValueSelectedAnswersCmp())
			{
				selectedAnswersCmp=getMinValueSelectedAnswersCmp();
			}
			else
			{
				int maxValueSelectedAnswersCmp=getMaxValueSelectedAnswersCmp(operation);
				if (selectedAnswersCmp>maxValueSelectedAnswersCmp)
				{
					selectedAnswersCmp=maxValueSelectedAnswersCmp;
				}
			}
		}
	}
}
