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
		int selectableWrongAnswers=getMaxUnselectedWrongAnswersValue(question.getCurrentUserOperation(null));
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
		if (NumberComparator.compareU(comparator,NumberComparator.EQUAL))
		{
			unselectedWrongAnswersMin=unselectedWrongAnswersCmp;
		}
		else if (NumberComparator.compareU(comparator,NumberComparator.GREATER))
		{
			unselectedWrongAnswersMin=unselectedWrongAnswersCmp+1;
		}
		else if (NumberComparator.compareU(comparator,NumberComparator.GREATER_EQUAL))
		{
			unselectedWrongAnswersMin=unselectedWrongAnswersCmp;
		}
		else if (NumberComparator.compareU(comparator,NumberComparator.BETWEEN))
		{
			unselectedWrongAnswersMin=unselectedWrongAnswersBetweenMin;
		}
		return unselectedWrongAnswersMin;
	}
	
	public int getUnselectedWrongAnswersMax()
	{
		return getUnselectedWrongAnswersMax(null);
	}
	
	public int getUnselectedWrongAnswersMax(Operation operation)
	{
		int unselectedWrongAnswersMax=getMaxUnselectedWrongAnswersValue(question.getCurrentUserOperation(operation));
		if (NumberComparator.compareU(comparator,NumberComparator.EQUAL))
		{
			unselectedWrongAnswersMax=unselectedWrongAnswersCmp;
		}
		else if (NumberComparator.compareU(comparator,NumberComparator.LESS))
		{
			unselectedWrongAnswersMax=unselectedWrongAnswersCmp-1;
		}
		else if (NumberComparator.compareU(comparator,NumberComparator.LESS_EQUAL))
		{
			unselectedWrongAnswersMax=unselectedWrongAnswersCmp;
		}
		else if (NumberComparator.compareU(comparator,NumberComparator.BETWEEN))
		{
			unselectedWrongAnswersMax=unselectedWrongAnswersBetweenMax;
		}
		return unselectedWrongAnswersMax;
	}
	
	public boolean isBetween()
	{
		return NumberComparator.compareU(comparator,NumberComparator.BETWEEN);
	}
	
	public int getMaxUnselectedWrongAnswersValue()
	{
		return getMaxUnselectedWrongAnswersValue(null);
	}
	
	public int getMaxUnselectedWrongAnswersValue(Operation operation)
	{
		return question.getNumberOfSelectableWrongAnswers(question.getCurrentUserOperation(operation));
	}
	
	public int getMinValueUnselectedWrongAnswersCmp()
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
	
	public int getMaxValueUnselectedWrongAnswersCmp()
	{
		return getMaxValueUnselectedWrongAnswersCmp(null);
	}
	
	public int getMaxValueUnselectedWrongAnswersCmp(Operation operation)
	{
		int maxValue=getMaxUnselectedWrongAnswersValue(question.getCurrentUserOperation(operation));
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
			if (unselectedWrongAnswersCmp<0)
			{
				unselectedWrongAnswersBetweenMin=0;
			}
			else
			{
				unselectedWrongAnswersBetweenMin=unselectedWrongAnswersCmp;
			}
			if (unselectedWrongAnswersBetweenMax<unselectedWrongAnswersBetweenMin)
			{
				unselectedWrongAnswersBetweenMax=unselectedWrongAnswersBetweenMin;
			}
		}
		else if (NumberComparator.compareU(oldComparator,NumberComparator.BETWEEN))
		{
			if (unselectedWrongAnswersBetweenMin<getMinValueUnselectedWrongAnswersCmp())
			{
				unselectedWrongAnswersCmp=getMinValueUnselectedWrongAnswersCmp();
			}
			else
			{
				int maxValueUnselectedWrongAnswersCmp=getMaxValueUnselectedWrongAnswersCmp(operation);
				if (unselectedWrongAnswersBetweenMin>maxValueUnselectedWrongAnswersCmp)
				{
					unselectedWrongAnswersCmp=maxValueUnselectedWrongAnswersCmp;
				}
				else
				{
					unselectedWrongAnswersCmp=unselectedWrongAnswersBetweenMin;
				}
			}
		}
		else
		{
			if (unselectedWrongAnswersCmp<getMinValueUnselectedWrongAnswersCmp())
			{
				unselectedWrongAnswersCmp=getMinValueUnselectedWrongAnswersCmp();
			}
			else
			{
				int maxValueUnselectedWrongAnswersCmp=getMaxValueUnselectedWrongAnswersCmp(operation);
				if (unselectedWrongAnswersCmp>maxValueUnselectedWrongAnswersCmp)
				{
					unselectedWrongAnswersCmp=maxValueUnselectedWrongAnswersCmp;
				}
			}
		}
	}
}
