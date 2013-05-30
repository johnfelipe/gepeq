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
		int selectableAnswers=getMaxUnselectedAnswersValue(question.getCurrentUserOperation(null));
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
		if (NumberComparator.compareU(comparator,NumberComparator.EQUAL))
		{
			unselectedAnswersMin=unselectedAnswersCmp;
		}
		else if (NumberComparator.compareU(comparator,NumberComparator.GREATER))
		{
			unselectedAnswersMin=unselectedAnswersCmp+1;
		}
		else if (NumberComparator.compareU(comparator,NumberComparator.GREATER_EQUAL))
		{
			unselectedAnswersMin=unselectedAnswersCmp;
		}
		else if (NumberComparator.compareU(comparator,NumberComparator.BETWEEN))
		{
			unselectedAnswersMin=unselectedAnswersBetweenMin;
		}
		return unselectedAnswersMin;
	}
	
	public int getUnselectedAnswersMax()
	{
		return getUnselectedAnswersMax(null);
	}
	
	public int getUnselectedAnswersMax(Operation operation)
	{
		int unselectedAnswersMax=getMaxUnselectedAnswersValue(question.getCurrentUserOperation(operation));
		if (NumberComparator.compareU(comparator,NumberComparator.EQUAL))
		{
			unselectedAnswersMax=unselectedAnswersCmp;
		}
		else if (NumberComparator.compareU(comparator,NumberComparator.LESS))
		{
			unselectedAnswersMax=unselectedAnswersCmp-1;
		}
		else if (NumberComparator.compareU(comparator,NumberComparator.LESS_EQUAL))
		{
			unselectedAnswersMax=unselectedAnswersCmp;
		}
		else if (NumberComparator.compareU(comparator,NumberComparator.BETWEEN))
		{
			unselectedAnswersMax=unselectedAnswersBetweenMax;
		}
		return unselectedAnswersMax;
	}
	
	public boolean isBetween()
	{
		return NumberComparator.compareU(comparator,NumberComparator.BETWEEN);
	}
	
	public int getMaxUnselectedAnswersValue()
	{
		return getMaxUnselectedAnswersValue(null);
	}
	
	public int getMaxUnselectedAnswersValue(Operation operation)
	{
		return question.getNumberOfSelectableAnswers(question.getCurrentUserOperation(operation));
	}
	
	public int getMinValueUnselectedAnswersCmp()
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
	
	public int getMaxValueUnselectedAnswersCmp()
	{
		return getMaxValueUnselectedAnswersCmp(null);
	}
	
	public int getMaxValueUnselectedAnswersCmp(Operation operation)
	{
		int maxValue=getMaxUnselectedAnswersValue(question.getCurrentUserOperation(operation));
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
			if (unselectedAnswersCmp<0)
			{
				unselectedAnswersBetweenMin=0;
			}
			else
			{
				unselectedAnswersBetweenMin=unselectedAnswersCmp;
			}
			if (unselectedAnswersBetweenMax<unselectedAnswersBetweenMin)
			{
				unselectedAnswersBetweenMax=unselectedAnswersBetweenMin;
			}
		}
		else if (NumberComparator.compareU(oldComparator,NumberComparator.BETWEEN))
		{
			if (unselectedAnswersBetweenMin<getMinValueUnselectedAnswersCmp())
			{
				unselectedAnswersCmp=getMinValueUnselectedAnswersCmp();
			}
			else
			{
				int maxValueUnselectedAnswersCmp=getMaxValueUnselectedAnswersCmp(operation);
				if (unselectedAnswersBetweenMin>maxValueUnselectedAnswersCmp)
				{
					unselectedAnswersCmp=maxValueUnselectedAnswersCmp;
				}
				else
				{
					unselectedAnswersCmp=unselectedAnswersBetweenMin;
				}
			}
		}
		else
		{
			if (unselectedAnswersCmp<getMinValueUnselectedAnswersCmp())
			{
				unselectedAnswersCmp=getMinValueUnselectedAnswersCmp();
			}
			else
			{
				int maxValueUnselectedAnswersCmp=getMaxValueUnselectedAnswersCmp(operation);
				if (unselectedAnswersCmp>maxValueUnselectedAnswersCmp)
				{
					unselectedAnswersCmp=maxValueUnselectedAnswersCmp;
				}
			}
		}
	}
}
