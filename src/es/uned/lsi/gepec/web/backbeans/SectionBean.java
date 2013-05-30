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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import es.uned.lsi.gepec.model.entities.Question;
import es.uned.lsi.gepec.model.entities.QuestionOrder;
import es.uned.lsi.gepec.model.entities.Section;
import es.uned.lsi.gepec.model.entities.Test;
import es.uned.lsi.gepec.util.HibernateUtil.Operation;
import es.uned.lsi.gepec.web.TestBean;

//Backbean de soporte para la vista prueba
//author Víctor Manuel Alonso Rodríguez
//since  12/2011
/**
 * Backbean for a section.
 */
@SuppressWarnings("serial")
public class SectionBean implements Serializable
{
	private long id;
	private TestBean test;
	private int order;
	private int randomQuantity;
	private boolean shuffle;
	private boolean random;
	private String name;
	private String title;
	private int weight;
	private List<QuestionOrderBean> questionOrders=null;
	
	// Local list needed to allow sorting of questions within sections
	private List<QuestionOrderBean> questionOrdersSorting;
	
	private int oldRandomQuantity;
	private int oldWeight;
	
	public SectionBean(TestBean test)
	{
		this(test,1);
	}
	
	public SectionBean(TestBean test,int order)
	{
		this.test=test;
		this.order=order;
		this.id=0;
		this.randomQuantity=-1;
		this.shuffle=false;
		this.random=false;
		this.name="";
		this.title="";
		this.weight=1;
		this.questionOrders=new ArrayList<QuestionOrderBean>();
		this.oldRandomQuantity=-1;
		this.oldWeight=1;
	}
	
	public SectionBean(TestBean test,Section section)
	{
		this.test=test;
		setFromSection(section);
	}
	
	public long getId()
	{
		return id;
	}
	
	public void setId(long id)
	{
		this.id=id;
	}
	
	public int getOrder()
	{
		return order;
	}
	
	public void setOrder(int order)
	{
		this.order=order;
	}
	
	public int getRandomQuantity()
	{
		return randomQuantity;
	}
	
	public void setRandomQuantity(int randomQuantity)
	{
		// Needed this check because sometimes setter is called several times with the same value
		if (randomQuantity!=getRandomQuantity())
		{
			oldRandomQuantity=getRandomQuantity();
		}
		this.randomQuantity=randomQuantity;
	}
	
	/**
	 * Restores last value of random quantity.
	 */
	public void rollbackRandomQuantity()
	{
		setRandomQuantity(oldRandomQuantity);
	}
	
	/**
	 * Consider current value of random quantity correct, so we overwrite old random quantity value with it.
	 */
	public void acceptRandomQuantity()
	{
		oldRandomQuantity=getRandomQuantity();
	}
	
	/**
	 * @param randomQuantity Random quantity to check
	 * @return true if random quantity is different that current accepted random quantity, false otherwise
	 */
	public boolean checkChangeRandomQuantity(int randomQuantity)
	{
		return randomQuantity!=getRandomQuantity() || 
			(randomQuantity!=oldRandomQuantity && oldRandomQuantity!=getRandomQuantity());
	}
	
	public boolean isShuffle()
	{
		return shuffle;
	}
	
	public void setShuffle(boolean shuffle)
	{
		if (test.isEnabledChecboxesSetters())
		{
			this.shuffle=shuffle;
		}
	}
	
	public boolean isRandom()
	{
		return random;
	}
	
	public void setRandom(boolean random)
	{
		if (test.isEnabledChecboxesSetters())
		{
			this.random=random;
		}
	}
	
	public String getName()
	{
		return name;
	}
	
	public void setName(String name)
	{
		this.name=name;
	}
	
	public String getTitle()
	{
		return title;
	}
	
	public void setTitle(String title)
	{
		this.title=title;
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
	
	public List<QuestionOrderBean> getQuestionOrders()
	{
		return questionOrders;
	}
	
	public void setQuestionOrders(List<QuestionOrderBean> questionOrders) {
		this.questionOrders=questionOrders;
	}
	
	public List<QuestionOrderBean> getQuestionOrdersSorting()
	{
		if (questionOrdersSorting==null)
		{
			questionOrdersSorting=new ArrayList<QuestionOrderBean>();
			for (QuestionOrderBean questionOrder:getQuestionOrders())
			{
				questionOrdersSorting.add(questionOrder);
			}
			Collections.sort(questionOrdersSorting,new Comparator<QuestionOrderBean>()
			{
				@Override
				public int compare(QuestionOrderBean qob1,QuestionOrderBean qob2)
				{
					return qob1.getOrder()==qob2.getOrder()?0:qob1.getOrder()>qob2.getOrder()?1:-1;
				}
			});
		}
		return questionOrdersSorting;
	}
	
	public void setQuestionOrdersSorting(List<QuestionOrderBean> questionOrdersSorting)
	{
		this.questionOrdersSorting=questionOrdersSorting;
	}
	
	/**
	 * Add a new question's order bean to section
	 * @param order Order
	 * @param questionId Question's identifier
	 * @return Question's order bean
	 */
	public QuestionOrderBean addQuestionOrder(int order,long questionId)
	{
		QuestionOrderBean questionOrderBean=new QuestionOrderBean(test,order);
		questionOrderBean.setQuestionId(questionId);
		getQuestionOrders().add(questionOrderBean);
		return questionOrderBean;
		
	}
	
	// Elimina un bean de sección y renumera el resto
	/**
	 * Deletes a question from a section.
	 * @param order Question's order
	 */
	public void removeQuestionOrder(int order)
	{
		if (getQuestionOrdersSize()>0)
		{
			QuestionOrderBean questionOrderBeanToRemove=null;
			for (QuestionOrderBean qob:getQuestionOrders())
			{
				if (qob.getOrder()>order)
				{
					qob.setOrder(qob.getOrder()-1);
				}
				else if (qob.getOrder()==order)
				{
					questionOrderBeanToRemove=qob;
				}
			}
			getQuestionOrders().remove(questionOrderBeanToRemove);
		}
	}
    
    /**
     * @return Number of questions in this section
     */
    public int getQuestionOrdersSize()
    {
    	List<QuestionOrderBean> questionOrders=getQuestionOrders();
    	return questionOrders==null?0:questionOrders.size();
    }
    
    /**
     * @return List of section's questions sorted
     */
    public List<Question> getQuestions()
    {
    	return getQuestions(null);
    }
    
    /**
     * @param operation Operation
     * @return List of section's questions sorted
     */
    private List<Question> getQuestions(Operation operation)
    {
    	// Get questions orders sorted by order 
    	List<QuestionOrderBean> sortedQuestionOrders=new ArrayList<QuestionOrderBean>();
    	for (QuestionOrderBean questionOrderBean:getQuestionOrders())
    	{
    		sortedQuestionOrders.add(questionOrderBean);
    	}
    	Collections.sort(sortedQuestionOrders,getQuestionOrderBeanComparatorByOrder());
    	
    	// Get current user session Hibernate operation
    	operation=test.getCurrentUserOperation(operation);
    	
    	// Get list of section's questions sorted
    	List<Question> questions = new ArrayList<Question>(getQuestionOrdersSize());
        for (QuestionOrderBean questionOrderBean:sortedQuestionOrders)
        {
    		Question question=test.getQuestion(operation,questionOrderBean.getQuestionId());
    		questions.add(question);
        }
    	return questions;
    }
    
    public String getQuestionsString()
    {
    	StringBuffer questionsStr=new StringBuffer();
    	boolean insertSeparator=false;
    	for (Question question:getQuestions())
    	{
    		if (insertSeparator)
    		{
    			questionsStr.append(", ");
    		}
    		else
    		{
    			insertSeparator=true;
    		}
    		questionsStr.append(question.getName());
    	}
    	return questionsStr.toString();
    }
    
	/**
	 * Get a comparator that can be used to order a list of instances of QuestionOrder by its order.<br/><br/>
	 * Note that can't be used with lists with null instances.
	 * @return Comparator that can be used to order a list of instances of QuestionOrder by its order
	 */
	private Comparator<QuestionOrderBean> getQuestionOrderBeanComparatorByOrder()
	{
		return new Comparator<QuestionOrderBean>()
		{
			@Override
			public int compare(QuestionOrderBean qob1,QuestionOrderBean qob2)
			{
				int compareResult=0;
				if (qob1.getOrder()<qob2.getOrder())
				{
					compareResult=-1;
				}
				else if (qob1.getOrder()>qob2.getOrder())
				{
					compareResult=1;
				}
				return compareResult;
			}
		};
	}
    
	/**
	 * @param question Question
	 * @return Question's order within section
	 */
	public int getQuestionOrderFromQuestion(Question question)
	{
		int order=0;
		QuestionOrderBean questionOrderBean=null;
		for (QuestionOrderBean qob:getQuestionOrders())
		{
			if (qob.getQuestionId()==question.getId()) {
				questionOrderBean=qob;
				break;
			}
		}
		if (questionOrderBean!=null)
		{
			order=questionOrderBean.getOrder();
		}
		return order;
	}
	
	/**
	 * Set section bean fields from a Section object.
	 * @param section Section object
	 */
	public void setFromSection(Section section)
	{
		setId(section.getId());
		setOrder(section.getOrder());
		setShuffle(section.isShuffle());
		setRandom(section.isRandom());
		randomQuantity=section.isShuffle() && section.isRandom()?section.getRandomQuantity():0;
		oldRandomQuantity=randomQuantity;
		setName(section.getName());
		setTitle(section.getTitle());
		weight=section.getWeight();
		oldWeight=weight;
		setQuestionOrders(new ArrayList<QuestionOrderBean>());
		
		// QuestionOrders
		for (QuestionOrder questionOrder:section.getQuestionOrders())
		{
			getQuestionOrders().add(new QuestionOrderBean(test,questionOrder));
		}
	}
	
	/**
	 * @param test Test object
	 * @return Section object with data from this section bean
	 */
	public Section getAsSection(Test test)
	{
		return getAsSection(null,test);
	}
	
	/**
	 * @param operation Operation
	 * @param test Test object
	 * @return Section object with data from this section bean
	 */
	public Section getAsSection(Operation operation,Test test)
	{
		Section section=new Section();
		section.setId(getId());
		section.setTest(test);
		section.setOrder(getOrder());
		section.setShuffle(isShuffle());
		section.setRandom(isRandom());
		section.setRandomQuantity(isShuffle()&& isRandom()?getRandomQuantity():0);
		section.setName(getName());
		section.setTitle(getTitle());
		
		// Get current user session Hibernate operation
		operation=this.test.getCurrentUserOperation(operation);
		
		if (this.test.isSectionsWeightsDisplayed(operation))
		{
			section.setWeight(getWeight());
		}
		else
		{
			section.setWeight(1);
		}
		
		// QuestionOrders
		for (QuestionOrderBean questionOrder:getQuestionOrders())
		{
			section.getQuestionOrders().add(questionOrder.getAsQuestionOrder(operation,section));
		}
		return section;
	}
	
	
}
