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

import javax.el.ELContext;
import javax.faces.context.FacesContext;

import es.uned.lsi.gepec.model.entities.Section;
import es.uned.lsi.gepec.model.entities.Test;
import es.uned.lsi.gepec.model.entities.TestFeedback;
import es.uned.lsi.gepec.util.HtmlUtils;
import es.uned.lsi.gepec.web.TestBean;
import es.uned.lsi.gepec.web.helper.NumberComparator;
import es.uned.lsi.gepec.web.services.LocalizationService;

@SuppressWarnings("serial")
public class TestFeedbackBean implements Serializable
{
	private TestBean test;
	private long id;
	private String htmlContent;
	private int position;
	private TestFeedbackConditionBean condition;
	
	private String plainContent;
	
	public TestFeedbackBean(TestBean test)
	{
		this(test,test.getFeedbacks().size()+1);
	}
	
	public TestFeedbackBean(TestBean test,int position)
	{
		this.test=test;
		this.position=position;
		id=0L;
		htmlContent="";
		String scoreGen=test.getLocalizationService().getLocalizedMessage("SCORE_GEN");
		if ("M".equals(scoreGen))
		{
			condition=new TestFeedbackConditionBean(test,false);			
		}
		else if ("F".equals(scoreGen))
		{
			condition=new TestFeedbackConditionBean(test,true);			
		}
		else
		{
			condition=new TestFeedbackConditionBean(test);			
		}
		plainContent=null;
	}
	
	public TestFeedbackBean(TestBean test,TestFeedback testFeedback)
	{
		this.test=test;
		setFromTestFeedback(testFeedback);
	}
	
	public long getId()
	{
		return id;
	}
	
	public void setId(long id)
	{
		this.id=id;
	}
	
	public String getHtmlContent()
	{
		return htmlContent;
	}
	
	public void setHtmlContent(String htmlContent)
	{
		this.htmlContent=htmlContent;
		this.plainContent=null;
	}
	
	public int getPosition()
	{
		return position;
	}
	
	public void setPosition(int position)
	{
		this.position=position;
	}
	
	public TestFeedbackConditionBean getCondition()
	{
		return condition;
	}
	
	public void setCondition(TestFeedbackConditionBean condition)
	{
		this.condition=condition;
	}
	
	public String getConditionString()
	{
		StringBuffer conditionString=new StringBuffer();
        ELContext elContext=FacesContext.getCurrentInstance().getELContext();
        LocalizationService localizationService=(LocalizationService)FacesContext.getCurrentInstance().
        	getApplication().getELResolver().getValue(elContext,null,"localizationService");
		conditionString.append(localizationService.getLocalizedMessage("SCORE"));
		conditionString.append(' ');
		if (condition.getSection()!=null)
		{
			conditionString.append('(');
			conditionString.append(test.getNumberedSectionName(condition.getSection()));
			conditionString.append(") "); 
		}
		if (NumberComparator.compareU(condition.getComparator(),NumberComparator.EQUAL))
		{
			conditionString.append("= ");
			conditionString.append(condition.getConditionalCmp());
		}
		else if (NumberComparator.compareU(condition.getComparator(),NumberComparator.NOT_EQUAL))
		{
			conditionString.append("\u2260 ");
			conditionString.append(condition.getConditionalCmp());
		}
		else if (NumberComparator.compareU(condition.getComparator(),NumberComparator.GREATER))
		{
			conditionString.append("> ");
			conditionString.append(condition.getConditionalCmp());
		}
		else if (NumberComparator.compareU(condition.getComparator(),NumberComparator.GREATER_EQUAL))
		{
			conditionString.append(">= ");
			conditionString.append(condition.getConditionalCmp());
		}
		else if (NumberComparator.compareU(condition.getComparator(),NumberComparator.LESS))
		{
			conditionString.append("< ");
			conditionString.append(condition.getConditionalCmp());
		}
		else if (NumberComparator.compareU(condition.getComparator(),NumberComparator.LESS_EQUAL))
		{
			conditionString.append("<= ");
			conditionString.append(condition.getConditionalCmp());
		}
		else if (NumberComparator.compareU(condition.getComparator(),NumberComparator.BETWEEN))
		{
			conditionString.append(condition.getConditionalBetweenMin());
			conditionString.append("..");
			conditionString.append(condition.getConditionalBetweenMax());
		}
		conditionString.append(' ');
		conditionString.append(localizationService.getLocalizedMessage(condition.getUnit()));
		return conditionString.toString();
	}
	
	public String getPlainContent()
	{
		if (plainContent==null)
		{
			try
			{
				plainContent=HtmlUtils.getPlainText(HtmlUtils.readAsHTML(htmlContent));
			}
			catch (Exception e)
			{
				plainContent=null;
			}
			
		}
		return plainContent==null?"":plainContent;
	}
	
	/**
	 * Set test feedback bean fields from a TestFeedback object.<br/><br/>
	 * <b>IMPORTANT:</b> This method considers that sections beans from the test bean of this 
	 * test feedback bean have been initialized, otherwise its fields will be initialized incorrectly.
	 * @param testFeedback Test feedback object
	 */
	public void setFromTestFeedback(TestFeedback testFeedback)
	{
		setId(testFeedback.getId());
		setHtmlContent(testFeedback.getText());
		setPosition(testFeedback.getPosition());
		setCondition(new TestFeedbackConditionBean(test));
		Section testFeedbackSection=testFeedback.getSection();
		SectionBean section=null;
		if (testFeedbackSection!=null)
		{
			for (SectionBean s:test.getSections())
			{
				if (s.getId()>0L && testFeedbackSection.getId()>0L)
				{
					if (s.getId()==testFeedbackSection.getId())
					{
						section=s;
						break;
					}
				}
				else
				{
					if (s.getOrder()==testFeedbackSection.getOrder())
					{
						section=s;
						break;
					}
				}
			}
		}
		getCondition().setSection(section);
		getCondition().setUnit(testFeedback.getScoreUnit().getUnit());
		int maxConditionalValue=getCondition().getMaxConditionalValue();
		int minValue=testFeedback.getMinvalue().intValue();
		int maxValue=testFeedback.getMaxvalue().intValue();
		getCondition().setConditionalCmp(0);
		getCondition().setConditionalBetweenMin(0);
		getCondition().setConditionalBetweenMax(maxConditionalValue);
		String scoreGen=test.getLocalizationService().getLocalizedMessage("SCORE_GEN");
		if (minValue==maxValue)
		{
			if (minValue>=0)
			{
				StringBuffer sbComparator=new StringBuffer(NumberComparator.EQUAL);
				if (!"M".equals(scoreGen))
				{
					sbComparator.append("_F");
				}
				getCondition().setComparator(sbComparator.toString());
				getCondition().setConditionalCmp(minValue);
			}
			else
			{
				StringBuffer sbComparator=new StringBuffer(NumberComparator.NOT_EQUAL);
				if (!"M".equals(scoreGen))
				{
					sbComparator.append("_F");
				}
				getCondition().setComparator(sbComparator.toString());
				getCondition().setConditionalCmp(-minValue-1);
			}
		}
		else if (minValue>0)
		{
			if (maxValue<maxConditionalValue)
			{
				StringBuffer sbComparator=new StringBuffer(NumberComparator.BETWEEN);
				if (!"M".equals(scoreGen))
				{
					sbComparator.append("_F");
				}
				getCondition().setComparator(sbComparator.toString());
				getCondition().setConditionalBetweenMin(minValue);
				getCondition().setConditionalBetweenMax(maxValue);
			}
			else
			{
				StringBuffer sbComparator=new StringBuffer(NumberComparator.GREATER_EQUAL);
				if (!"M".equals(scoreGen))
				{
					sbComparator.append("_F");
				}
				getCondition().setComparator(sbComparator.toString());
				getCondition().setConditionalCmp(minValue);
			}
		}
		else
		{
			StringBuffer sbComparator=new StringBuffer(NumberComparator.LESS_EQUAL);
			if (!"M".equals(scoreGen))
			{
				sbComparator.append("_F");
			}
			getCondition().setComparator(sbComparator.toString());
			getCondition().setConditionalCmp(maxValue);
		}
		plainContent=null;
	}
	
	/**
	 * Get a TestFeedback object with data from this test feedback bean.<br/><br/>
	 * <b>IMPORTANT:</b> This method considers that Section objects from the received Test object 
	 * have been initialized, otherwise fields of the TestFeedback object will be initialized incorrectly.
	 * @param test Test object
	 * @return TestFeedback object with data from this test feedback bean
	 */
	public TestFeedback getAsTestFeedback(Test test)
	{
		TestFeedback testFeedback=new TestFeedback();
		testFeedback.setId(getId());
		testFeedback.setTest(test);
		testFeedback.setText(getHtmlContent());
		testFeedback.setPosition(getPosition());
		SectionBean conditionSection=getCondition().getSection();
		Section section=null;
		if (conditionSection!=null)
		{
			for (Section s:test.getSections())
			{
				if (s.getId()>0L && conditionSection.getId()>0L)
				{
					if (s.getId()==conditionSection.getId())
					{
						section=s;
						break;
					}
				}
				else
				{
					if (s.getOrder()==conditionSection.getOrder())
					{
						section=s;
						break;
					}
				}
			}
		}
		testFeedback.setSection(section);
		testFeedback.setScoreUnit(this.test.getScoreUnit(getCondition().getUnit()));
		testFeedback.setMinvalue(new Integer(getCondition().getMinValue()));
		testFeedback.setMaxvalue(new Integer(getCondition().getMaxValue()));
		return testFeedback;
	}
}
