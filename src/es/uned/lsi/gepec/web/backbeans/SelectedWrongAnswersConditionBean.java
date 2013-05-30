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
		int selectableWrongAnswers=getMaxSelectedWrongAnswersValue(question.getCurrentUserOperation(null));
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
		if (NumberComparator.compareU(comparator,NumberComparator.EQUAL))
		{
			selectedWrongAnswersMin=selectedWrongAnswersCmp;
		}
		else if (NumberComparator.compareU(comparator,NumberComparator.GREATER))
		{
			selectedWrongAnswersMin=selectedWrongAnswersCmp+1;
		}
		else if (NumberComparator.compareU(comparator,NumberComparator.GREATER_EQUAL))
		{
			selectedWrongAnswersMin=selectedWrongAnswersCmp;
		}
		else if (NumberComparator.compareU(comparator,NumberComparator.BETWEEN))
		{
			selectedWrongAnswersMin=selectedWrongAnswersBetweenMin;
		}
		return selectedWrongAnswersMin;
	}
	
	public int getSelectedWrongAnswersMax()
	{
		return getSelectedWrongAnswersMax(null);
	}
	
	public int getSelectedWrongAnswersMax(Operation operation)
	{
		int selectedWrongAnswersMax=getMaxSelectedWrongAnswersValue(question.getCurrentUserOperation(operation));
		if (NumberComparator.compareU(comparator,NumberComparator.EQUAL))
		{
			selectedWrongAnswersMax=selectedWrongAnswersCmp;
		}
		else if (NumberComparator.compareU(comparator,NumberComparator.LESS))
		{
			selectedWrongAnswersMax=selectedWrongAnswersCmp-1;
		}
		else if (NumberComparator.compareU(comparator,NumberComparator.LESS_EQUAL))
		{
			selectedWrongAnswersMax=selectedWrongAnswersCmp;
		}
		else if (NumberComparator.compareU(comparator,NumberComparator.BETWEEN))
		{
			selectedWrongAnswersMax=selectedWrongAnswersBetweenMax;
		}
		return selectedWrongAnswersMax;
	}
	
	public boolean isBetween()
	{
		return NumberComparator.compareU(comparator,NumberComparator.BETWEEN);
	}
	
	public int getMaxSelectedWrongAnswersValue()
	{
		return getMaxSelectedWrongAnswersValue(null);
	}
	
	public int getMaxSelectedWrongAnswersValue(Operation operation)
	{
		return question.getNumberOfSelectableWrongAnswers(question.getCurrentUserOperation(operation));
	}
	
	public int getMinValueSelectedWrongAnswersCmp()
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
	
	public int getMaxValueSelectedWrongAnswersCmp()
	{
		return getMaxValueSelectedWrongAnswersCmp(null);
	}
	
	public int getMaxValueSelectedWrongAnswersCmp(Operation operation)
	{
		int maxValue=getMaxSelectedWrongAnswersValue(question.getCurrentUserOperation(operation));
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
			if (selectedWrongAnswersCmp<0)
			{
				selectedWrongAnswersBetweenMin=0;
			}
			else
			{
				selectedWrongAnswersBetweenMin=selectedWrongAnswersCmp;
			}
			if (selectedWrongAnswersBetweenMax<selectedWrongAnswersBetweenMin)
			{
				selectedWrongAnswersBetweenMax=selectedWrongAnswersBetweenMin;
			}
		}
		else if (NumberComparator.compareU(oldComparator,NumberComparator.BETWEEN))
		{
			if (selectedWrongAnswersBetweenMin<getMinValueSelectedWrongAnswersCmp())
			{
				selectedWrongAnswersCmp=getMinValueSelectedWrongAnswersCmp();
			}
			else
			{
				int maxValueSelectedWrongAnswersCmp=getMaxValueSelectedWrongAnswersCmp(operation);
				if (selectedWrongAnswersBetweenMin>maxValueSelectedWrongAnswersCmp)
				{
					selectedWrongAnswersCmp=maxValueSelectedWrongAnswersCmp;
				}
				else
				{
					selectedWrongAnswersCmp=selectedWrongAnswersBetweenMin;
				}
			}
		}
		else
		{
			if (selectedWrongAnswersCmp<getMinValueSelectedWrongAnswersCmp())
			{
				selectedWrongAnswersCmp=getMinValueSelectedWrongAnswersCmp();
			}
			else
			{
				int maxValueSelectedWrongAnswersCmp=getMaxValueSelectedWrongAnswersCmp(operation);
				if (selectedWrongAnswersCmp>maxValueSelectedWrongAnswersCmp)
				{
					selectedWrongAnswersCmp=maxValueSelectedWrongAnswersCmp;
				}
			}
		}
	}
}
