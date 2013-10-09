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
import java.util.List;

import javax.faces.event.AjaxBehaviorEvent;

import org.primefaces.component.selectonemenu.SelectOneMenu;

import es.uned.lsi.gepec.model.entities.ScoreType;
import es.uned.lsi.gepec.web.TestBean;
import es.uned.lsi.gepec.web.helper.NumberComparator;

@SuppressWarnings("serial")
public class TestFeedbackConditionBean implements Serializable
{
	public final static String MARKS_UNIT="MARK_UNITS";
	public final static String PERCENTAGE_UNIT="PERCENTAGE_UNITS";
	
	private final static int MAX_PERCENTAGE=100;
	private final static int BASE_MARKS_PER_QUESTION=3;
	
	public final static List<String> UNITS;
	static
	{
		UNITS=new ArrayList<String>();
		UNITS.add(MARKS_UNIT);
		UNITS.add(PERCENTAGE_UNIT);
	}
	
	private TestBean test;
	private SectionBean section;
	private String unit;
	private String oldComparator;
	private String comparator;
	private int conditionalCmp;
	private int conditionalBetweenMin;
	private int conditionalBetweenMax;
	
	public TestFeedbackConditionBean(TestBean test)
	{
		this(test,true);
	}
	
	public TestFeedbackConditionBean(TestBean test,boolean genF)
	{
		this.test=test;
		section=null;
		unit=PERCENTAGE_UNIT;
		oldComparator=null;
		if (genF)
		{
			StringBuffer sbComparator=new StringBuffer(NumberComparator.GREATER);
			sbComparator.append("_F");
			comparator=sbComparator.toString();
		}
		else
		{
			comparator=NumberComparator.GREATER;
		}
		conditionalCmp=0;
		conditionalBetweenMin=0;
		conditionalBetweenMax=MAX_PERCENTAGE;
	}
	
	public SectionBean getSection()
	{
		return section;
	}
	
	public void setSection(SectionBean section)
	{
		this.section=section;
	}
	
	public String getUnit()
	{
		return unit;
	}
	
	public void setUnit(String unit)
	{
		this.unit=unit;
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
	
	public int getConditionalCmp()
	{
		return conditionalCmp;
	}
	
	public void setConditionalCmp(int conditionalCmp)
	{
		this.conditionalCmp=conditionalCmp;
	}
	
	public int getConditionalBetweenMin()
	{
		return conditionalBetweenMin;
	}
	
	public void setConditionalBetweenMin(int conditionalBetweenMin)
	{
		this.conditionalBetweenMin=conditionalBetweenMin;
	}
	
	public int getConditionalBetweenMax()
	{
		return conditionalBetweenMax;
	}
	
	public void setConditionalBetweenMax(int conditionalBetweenMax)
	{
		this.conditionalBetweenMax=conditionalBetweenMax;
	}
	
	
	public int getMinValue()
	{
		int minValue=0;
		if (NumberComparator.compareU(getComparator(),NumberComparator.EQUAL))
		{
			minValue=getConditionalCmp();
		}
		else if (NumberComparator.compareU(getComparator(),NumberComparator.NOT_EQUAL))
		{
			minValue=-getConditionalCmp()-1;
		}
		else if (NumberComparator.compareU(getComparator(),NumberComparator.GREATER))
		{
			minValue=getConditionalCmp()+1;
		}
		else if (NumberComparator.compareU(getComparator(),NumberComparator.GREATER_EQUAL))
		{
			minValue=getConditionalCmp();
		}
		else if (NumberComparator.compareU(getComparator(),NumberComparator.BETWEEN))
		{
			minValue=getConditionalBetweenMin();
		}
		return minValue;
	}
	
	public int getMaxValue()
	{
		int maxValue=Integer.MAX_VALUE;
		if (NumberComparator.compareU(getComparator(),NumberComparator.EQUAL))
		{
			maxValue=getConditionalCmp();
		}
		else if (NumberComparator.compareU(getComparator(),NumberComparator.NOT_EQUAL))
		{
			maxValue=-getConditionalCmp()-1;
		}
		else if (NumberComparator.compareU(getComparator(),NumberComparator.LESS))
		{
			maxValue=getConditionalCmp()-1;
		}
		else if (NumberComparator.compareU(getComparator(),NumberComparator.LESS_EQUAL))
		{
			maxValue=getConditionalCmp();
		}
		else if (NumberComparator.compareU(getComparator(),NumberComparator.BETWEEN))
		{
			maxValue=getConditionalBetweenMax();
		}
		return maxValue;
	}
	
	public boolean isBetween()
	{
		return NumberComparator.compareU(getComparator(),NumberComparator.BETWEEN);
	}
	
	public int getMaxConditionalValue()
	{
		int maxConditionalValue=0;
		if (MARKS_UNIT.equals(getUnit()))
		{
			if (getSection()==null)
			{
				ScoreType scoreType=test.getScoreType();
				if ("SCORE_TYPE_QUESTIONS".equals(scoreType.getType()))
				{
					for (SectionBean s:test.getSections())
					{
						if (s.isShuffle() && s.isRandom())
						{
							maxConditionalValue+=s.getRandomQuantity()*BASE_MARKS_PER_QUESTION;
						}
						else
						{
							for (QuestionOrderBean qob:s.getQuestionOrders())
							{
								maxConditionalValue+=qob.getWeight()*BASE_MARKS_PER_QUESTION;
							}
						}
					}
				}
				else if ("SCORE_TYPE_SECTIONS".equals(scoreType.getType()))
				{
					int maxBaseSectionScore=0;
					List<SectionBean> sections=test.getSections();
					for (SectionBean s:sections)
					{
						int maxSectionScore=0;
						if (s.isShuffle() && s.isRandom())
						{
							maxSectionScore=s.getRandomQuantity()*BASE_MARKS_PER_QUESTION;
						}
						else
						{
							for (QuestionOrderBean qob:s.getQuestionOrders())
							{
								maxSectionScore+=qob.getWeight()*BASE_MARKS_PER_QUESTION;
							}
						}
						if (maxSectionScore>maxBaseSectionScore)
						{
							maxBaseSectionScore=maxSectionScore;
						}
					}
					for (SectionBean s:sections)
					{
						maxConditionalValue+=s.getWeight()*maxBaseSectionScore;
					}
				}
			}
			else
			{
				if (getSection().isShuffle() && getSection().isRandom())
				{
					maxConditionalValue=getSection().getRandomQuantity()*BASE_MARKS_PER_QUESTION;
				}
				else
				{
					for (QuestionOrderBean qob:getSection().getQuestionOrders())
					{
						maxConditionalValue+=qob.getWeight()*BASE_MARKS_PER_QUESTION;
					}
				}
			}
		}
		else if (PERCENTAGE_UNIT.equals(getUnit()))
		{
			maxConditionalValue=MAX_PERCENTAGE;
		}
		return maxConditionalValue;
	}
	
	public int getMinValueConditionalCmp()
	{
		int minValue=0;
		if (NumberComparator.compareU(getComparator(),NumberComparator.LESS))
		{
			minValue=1;
		}
		return minValue;
	}
	
	public int getMaxValueConditionalCmp()
	{
		int maxValue=getMaxConditionalValue();
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
			if (!PERCENTAGE_UNIT.equals(getUnit()) || maxValue<MAX_PERCENTAGE)
			{
				maxValue++;
			}
		}
		return maxValue;
	}
	
	public void changeSection(AjaxBehaviorEvent event)
	{
		setSection((SectionBean)((SelectOneMenu)event.getComponent()).getValue());
		if (NumberComparator.compareU(getComparator(),NumberComparator.BETWEEN))
		{
			int maxValue=getMaxConditionalValue();
			if (getConditionalBetweenMax()>maxValue)
			{
				setConditionalBetweenMax(maxValue);
			}
			if (getConditionalBetweenMin()>getConditionalBetweenMax())
			{
				setConditionalBetweenMin(getConditionalBetweenMax());
			}
		}
		else
		{
			int maxValueCmp=getMaxValueConditionalCmp();
			if (getConditionalCmp()>maxValueCmp)
			{
				setConditionalCmp(maxValueCmp);
			}
		}
		test.resetOldFeedbackValues();
	}
	
	public void changeComparator(AjaxBehaviorEvent event)
	{
		comparator=(String)((SelectOneMenu)event.getComponent()).getValue();
		if (NumberComparator.compareU(getComparator(),NumberComparator.BETWEEN))
		{
			int maxValue=getMaxConditionalValue();
			if (getConditionalCmp()<0)
			{
				setConditionalBetweenMin(0);
				if (getConditionalBetweenMax()>maxValue)
				{
					setConditionalBetweenMax(maxValue);
				}
			}
			else
			{
				if (getConditionalCmp()>maxValue)
				{
					setConditionalBetweenMin(maxValue);
				}
				else
				{
					setConditionalBetweenMin(getConditionalCmp());
				}
			}
			if (getConditionalBetweenMax()<getConditionalBetweenMin())
			{
				setConditionalBetweenMax(getConditionalBetweenMin());
			}
			else if (getConditionalBetweenMax()>maxValue)
			{
				setConditionalBetweenMax(maxValue);
			}
		}
		else if (NumberComparator.compareU(oldComparator,NumberComparator.BETWEEN))
		{
			int minValueCmp=getMinValueConditionalCmp();
			if (getConditionalBetweenMin()<minValueCmp)
			{
				setConditionalCmp(minValueCmp);
			}
			else
			{
				int maxValueCmp=getMaxValueConditionalCmp();
				if (getConditionalBetweenMin()>maxValueCmp)
				{
					setConditionalCmp(maxValueCmp);
				}
				else
				{
					setConditionalCmp(getConditionalBetweenMin());
				}
			}
		}
		else
		{
			int minValueCmp=getMinValueConditionalCmp();
			if (getConditionalCmp()<minValueCmp)
			{
				setConditionalCmp(minValueCmp);
			}
			else
			{
				int maxValueCmp=getMaxValueConditionalCmp();
				if (getConditionalCmp()>maxValueCmp)
				{
					setConditionalCmp(maxValueCmp);
				}
			}
		}
		test.resetOldFeedbackValues();
	}
	
	public void changeUnit(AjaxBehaviorEvent event)
	{
		setUnit((String)((SelectOneMenu)event.getComponent()).getValue());
		if (NumberComparator.compareU(getComparator(),NumberComparator.BETWEEN))
		{
			int maxValue=getMaxConditionalValue();
			if (getConditionalBetweenMax()>maxValue)
			{
				setConditionalBetweenMax(maxValue);
			}
			if (getConditionalBetweenMin()>getConditionalBetweenMax())
			{
				setConditionalBetweenMin(getConditionalBetweenMax());
			}
		}
		else
		{
			int maxValueCmp=getMaxValueConditionalCmp();
			if (getConditionalCmp()>maxValueCmp)
			{
				setConditionalCmp(maxValueCmp);
			}
		}
		test.resetOldFeedbackValues();
	}
}
