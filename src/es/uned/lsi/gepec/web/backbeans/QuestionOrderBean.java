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

import es.uned.lsi.gepec.model.entities.Question;
import es.uned.lsi.gepec.model.entities.QuestionOrder;
import es.uned.lsi.gepec.model.entities.Section;
import es.uned.lsi.gepec.util.HibernateUtil.Operation;
import es.uned.lsi.gepec.web.TestBean;

/**
 * Backbean de soporte para la vista prueba
 * 
 * @author Víctor Manuel Alonso Rodríguez
 * @since  12/2011
 */
@SuppressWarnings("serial")
public class QuestionOrderBean implements Serializable
{
	private TestBean test;
	private long id;
	private int order;
	private int weight;
	private long questionId;
	
	private Question question;
	
	private int oldWeight;
	
	public QuestionOrderBean(TestBean test,int order)
	{
		this.id=0L;
		this.test=test;
		this.order=order;
		this.weight=1;
		this.question=null;
		this.questionId=0L;
		this.oldWeight=1;
	}
	
	public QuestionOrderBean(TestBean test,QuestionOrder questionOrder)
	{
		this.test=test;
		setFromQuestionOrder(questionOrder);
	}
	
	public long getId()
	{
		return id;
	}
	
	public void setId(long id)
	{
		this.id=id;
	}
	
	public long getQuestionId()
	{
		return questionId;
	}
	
	public void setQuestionId(long questionId)
	{
		this.questionId=questionId;
	}
	
	public int getOrder()
	{
		return order;
	}
	
	public void setOrder(int order)
	{
		this.order=order;
	}
	
	public int getWeight()
	{
		return weight;
	}
	
	public void setWeight(int weight)
	{
		// Needed this check because sometimes setter is called several times with the same value
		if (weight!=getWeight())
		{
			oldWeight=getWeight();
		}
		this.weight=weight;
	}
	
	public void rollbackWeight()
	{
		weight=oldWeight;
	}
	
	/**
	 * Consider current value of weight correct, so we overwrite old weight value with it.
	 */
	public void acceptWeight()
	{
		oldWeight=getWeight();
	}
	
	/**
	 * @param weight Weight to check
	 * @return true if weight is different that current accepted weight, false otherwise
	 */
	public boolean checkChangeWeight(int weight)
	{
		return weight!=getWeight() || (weight!=oldWeight && oldWeight!=getWeight());
	}
	
	/**
	 * @return Question
	 */
	public Question getQuestion()
	{
		return getQuestion(null);
	}
	
	/**
	 * @param operation Operation
	 * @return Question
	 */
	private Question getQuestion(Operation operation)
	{
		if (questionId==0L)
		{
			question=null;
		}
		else if (question==null || question.getId()!=questionId)
		{
			question=test.getQuestion(test.getCurrentUserOperation(operation),questionId);
		}
		return question;
	}
	
	public int getSectionOrder()
	{
		return getSectionOrder(null);
	}
	
	public int getSectionOrder(Operation operation)
	{
		int sectionOrder=-1;
		for (SectionBean section:test.getSections(test.getCurrentUserOperation(operation)))
		{
			if (section.getQuestionOrders().contains(this))
			{
				sectionOrder=section.getOrder();
				break;
			}
		}
		return sectionOrder;
	}
	
	/**
	 * Set question order bean fields from a QuestionOrder object
	 * @param questionOrder QuestionOrder object
	 */
	public void setFromQuestionOrder(QuestionOrder questionOrder)
	{
		setId(questionOrder.getId());
		setOrder(questionOrder.getOrder());
		weight=questionOrder.getWeight();
		oldWeight=weight;
		question=questionOrder.getQuestion();
		questionId=question.getId();
	}
	
	/**
	 * @param section Section object
	 * @return QuestionOrder object with data from this QuestionOrderBean
	 */
	public QuestionOrder getAsQuestionOrder(Section section)
	{
		return getAsQuestionOrder(null,section);
	}
	
	/**
	 * @param operation Operation
	 * @param section Section object
	 * @return QuestionOrder object with data from this QuestionOrderBean
	 */
	public QuestionOrder getAsQuestionOrder(Operation operation,Section section)
	{
		QuestionOrder questionOrder=new QuestionOrder();
		questionOrder.setId(getId());
		questionOrder.setQuestion(getQuestion(test.getCurrentUserOperation(operation)));
		questionOrder.setOrder(getOrder());
		questionOrder.setWeight(getWeight());
		questionOrder.setSection(section);
		return questionOrder;
	}
}
