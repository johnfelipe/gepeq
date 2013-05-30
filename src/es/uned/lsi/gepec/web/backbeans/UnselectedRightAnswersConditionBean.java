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
		int selectableRightAnswers=getMaxUnselectedRightAnswersValue(question.getCurrentUserOperation(null));
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
		if (NumberComparator.compareU(comparator,NumberComparator.EQUAL))
		{
			unselectedRightAnswersMin=unselectedRightAnswersCmp;
		}
		else if (NumberComparator.compareU(comparator,NumberComparator.GREATER))
		{
			unselectedRightAnswersMin=unselectedRightAnswersCmp+1;
		}
		else if (NumberComparator.compareU(comparator,NumberComparator.GREATER_EQUAL))
		{
			unselectedRightAnswersMin=unselectedRightAnswersCmp;
		}
		else if (NumberComparator.compareU(comparator,NumberComparator.BETWEEN))
		{
			unselectedRightAnswersMin=unselectedRightAnswersBetweenMin;
		}
		return unselectedRightAnswersMin;
	}
	
	public int getUnselectedRightAnswersMax()
	{
		return getUnselectedRightAnswersMax(null);
	}
	
	public int getUnselectedRightAnswersMax(Operation operation)
	{
		int unselectedRightAnswersMax=getMaxUnselectedRightAnswersValue(question.getCurrentUserOperation(operation));
		if (NumberComparator.compareU(comparator,NumberComparator.EQUAL))
		{
			unselectedRightAnswersMax=unselectedRightAnswersCmp;
		}
		else if (NumberComparator.compareU(comparator,NumberComparator.LESS))
		{
			unselectedRightAnswersMax=unselectedRightAnswersCmp-1;
		}
		else if (NumberComparator.compareU(comparator,NumberComparator.LESS_EQUAL))
		{
			unselectedRightAnswersMax=unselectedRightAnswersCmp;
		}
		else if (NumberComparator.compareU(comparator,NumberComparator.BETWEEN))
		{
			unselectedRightAnswersMax=unselectedRightAnswersBetweenMax;
		}
		return unselectedRightAnswersMax;
	}
	
	public boolean isBetween()
	{
		return NumberComparator.compareU(comparator,NumberComparator.BETWEEN);
	}
	
	public int getMaxUnselectedRightAnswersValue()
	{
		return getMaxUnselectedRightAnswersValue(null);
	}
	
	public int getMaxUnselectedRightAnswersValue(Operation operation)
	{
		return question.getNumberOfSelectableRightAnswers(question.getCurrentUserOperation(operation));
	}
	
	public int getMinValueUnselectedRightAnswersCmp()
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
	
	public int getMaxValueUnselectedRightAnswersCmp()
	{
		return getMaxValueUnselectedRightAnswersCmp(null);
	}
	
	public int getMaxValueUnselectedRightAnswersCmp(Operation operation)
	{
		int maxValue=getMaxUnselectedRightAnswersValue(question.getCurrentUserOperation(operation));
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
			if (unselectedRightAnswersCmp<0)
			{
				unselectedRightAnswersBetweenMin=0;
			}
			else
			{
				unselectedRightAnswersBetweenMin=unselectedRightAnswersCmp;
			}
			if (unselectedRightAnswersBetweenMax<unselectedRightAnswersBetweenMin)
			{
				unselectedRightAnswersBetweenMax=unselectedRightAnswersBetweenMin;
			}
		}
		else if (NumberComparator.compareU(oldComparator,NumberComparator.BETWEEN))
		{
			if (unselectedRightAnswersBetweenMin<getMinValueUnselectedRightAnswersCmp())
			{
				unselectedRightAnswersCmp=getMinValueUnselectedRightAnswersCmp();
			}
			else
			{
				int maxValueUnselectedRightAnswersCmp=getMaxValueUnselectedRightAnswersCmp(operation);
				if (unselectedRightAnswersBetweenMin>maxValueUnselectedRightAnswersCmp)
				{
					unselectedRightAnswersCmp=maxValueUnselectedRightAnswersCmp;
				}
				else
				{
					unselectedRightAnswersCmp=unselectedRightAnswersBetweenMin;
				}
			}
		}
		else
		{
			if (unselectedRightAnswersCmp<getMinValueUnselectedRightAnswersCmp())
			{
				unselectedRightAnswersCmp=getMinValueUnselectedRightAnswersCmp();
			}
			else
			{
				int maxValueUnselectedRightAnswersCmp=getMaxValueUnselectedRightAnswersCmp(operation);
				if (unselectedRightAnswersCmp>maxValueUnselectedRightAnswersCmp)
				{
					unselectedRightAnswersCmp=maxValueUnselectedRightAnswersCmp;
				}
			}
		}
	}
}
