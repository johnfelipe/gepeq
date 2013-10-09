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

import es.uned.lsi.gepec.web.helper.NumberComparator;

@SuppressWarnings("serial")
public class AttemptsConditionBean extends ConditionBean implements Serializable
{
	public final static String TYPE="CONDITION_TYPE_ATTEMPTS";
	public final static int MAX_ATTEMPT=3;
	
	private String oldComparator;
	private String comparator;
	private int attemptsCmp;
	private int attemptsBetweenMin;
	private int attemptsBetweenMax;
	
	public AttemptsConditionBean()
	{
		this(1,1,false);
	}
	
	public AttemptsConditionBean(boolean genF)
	{
		this(1,1,genF);
	}
	
	public AttemptsConditionBean(int minAttempts,int maxAttempts)
	{
		this(minAttempts,maxAttempts,false);
	}
	
	public AttemptsConditionBean(int minAttempts,int maxAttempts,boolean genF)
	{
		super(TYPE);
		oldComparator=null;
		attemptsCmp=1;
		attemptsBetweenMin=1;
		attemptsBetweenMax=MAX_ATTEMPT;
		if (minAttempts==maxAttempts)
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
			attemptsCmp=minAttempts;
		}
		else if (minAttempts>0)
		{
			if (maxAttempts<=MAX_ATTEMPT)
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
				attemptsBetweenMin=minAttempts;
				attemptsBetweenMax=maxAttempts;
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
				attemptsCmp=minAttempts;
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
			attemptsCmp=maxAttempts;
		}
	}
	
	public AttemptsConditionBean(AttemptsConditionBean otherAttemptsCondition)
	{
		super(TYPE);
		oldComparator=otherAttemptsCondition.oldComparator;
		comparator=otherAttemptsCondition.comparator;
		attemptsCmp=otherAttemptsCondition.attemptsCmp;
		attemptsBetweenMin=otherAttemptsCondition.attemptsBetweenMin;
		attemptsBetweenMax=otherAttemptsCondition.attemptsBetweenMax;
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
	
	public int getAttemptsCmp()
	{
		return attemptsCmp;
	}
	
	public void setAttemptsCmp(int attemptsCmp)
	{
		this.attemptsCmp=attemptsCmp;
	}
	
	public int getAttemptsBetweenMin()
	{
		return attemptsBetweenMin;
	}
	
	public void setAttemptsBetweenMin(int attemptsBetweenMin)
	{
		this.attemptsBetweenMin=attemptsBetweenMin;
	}
	
	public int getAttemptsBetweenMax()
	{
		return attemptsBetweenMax;
	}
	
	public void setAttemptsBetweenMax(int attemptsBetweenMax)
	{
		this.attemptsBetweenMax=attemptsBetweenMax;
	}
	
	public int getAttemptsMin()
	{
		int attemptsMin=0;
		if (NumberComparator.compareU(getComparator(),NumberComparator.EQUAL))
		{
			attemptsMin=getAttemptsCmp();
		}
		else if (NumberComparator.compareU(getComparator(),NumberComparator.GREATER))
		{
			attemptsMin=getAttemptsCmp()+1;
		}
		else if (NumberComparator.compareU(getComparator(),NumberComparator.GREATER_EQUAL))
		{
			attemptsMin=getAttemptsCmp();
		}
		else if (NumberComparator.compareU(getComparator(),NumberComparator.BETWEEN))
		{
			attemptsMin=getAttemptsBetweenMin();
		}
		return attemptsMin;
	}
	
	public int getAttemptsMax()
	{
		int attemptsMax=Integer.MAX_VALUE;
		if (NumberComparator.compareU(getComparator(),NumberComparator.EQUAL))
		{
			attemptsMax=getAttemptsCmp();
		}
		else if (NumberComparator.compareU(getComparator(),NumberComparator.LESS))
		{
			attemptsMax=getAttemptsCmp()-1;
		}
		else if (NumberComparator.compareU(getComparator(),NumberComparator.LESS_EQUAL))
		{
			attemptsMax=getAttemptsCmp();
		}
		else if (NumberComparator.compareU(getComparator(),NumberComparator.BETWEEN))
		{
			attemptsMax=getAttemptsBetweenMax();
		}
		return attemptsMax;
	}
	
	public boolean isBetween()
	{
		return NumberComparator.compareU(getComparator(),NumberComparator.BETWEEN);
	}
	
	public int getMaxAttemptValue()
	{
		return MAX_ATTEMPT;
	}
	
	public int getMinValueAttemptCmp()
	{
		int minValue=1;
		if (NumberComparator.compareU(getComparator(),NumberComparator.GREATER))
		{
			minValue=0;
		}
		else if (NumberComparator.compareU(getComparator(),NumberComparator.LESS))
		{
			minValue=2;
		}
		return minValue;
	}
	
	public int getMaxValueAttemptCmp()
	{
		int maxValue=MAX_ATTEMPT;
		if (NumberComparator.compareU(getComparator(),NumberComparator.GREATER))
		{
			maxValue=MAX_ATTEMPT-1;
		}
		else if (NumberComparator.compareU(getComparator(),NumberComparator.LESS) || 
			NumberComparator.compareU(getComparator(),NumberComparator.LESS_EQUAL))
		{
			maxValue=MAX_ATTEMPT+1;
		}
		return maxValue;
	}
	
	public void changeComparator(AjaxBehaviorEvent event)
	{
		comparator=(String)((SelectOneMenu)event.getComponent()).getValue();
		if (NumberComparator.compareU(getComparator(),NumberComparator.BETWEEN))
		
		{
			if (getAttemptsCmp()<1)
			{
				setAttemptsBetweenMin(1);
			}
			else if (getAttemptsCmp()>MAX_ATTEMPT)
			{
				setAttemptsBetweenMin(MAX_ATTEMPT);
			}
			else
			{
				setAttemptsBetweenMin(getAttemptsCmp());
			}
			if (getAttemptsBetweenMax()<getAttemptsBetweenMin())
			{
				setAttemptsBetweenMax(getAttemptsBetweenMin());
			}
		}
		else if (NumberComparator.compareU(oldComparator,NumberComparator.BETWEEN))
		{
			int minValueCmp=getMinValueAttemptCmp();
			if (getAttemptsBetweenMin()<minValueCmp)
			{
				setAttemptsCmp(minValueCmp);
			}
			else
			{
				int maxValueCmp=getMaxValueAttemptCmp();
				if (getAttemptsBetweenMin()>maxValueCmp)
				{
					setAttemptsCmp(maxValueCmp);
				}
				else
				{
					setAttemptsCmp(getAttemptsBetweenMin());
				}
			}
		}
		else
		{
			int minValueCmp=getMinValueAttemptCmp();
			if (getAttemptsCmp()<minValueCmp)
			{
				setAttemptsCmp(minValueCmp);
			}
			else
			{
				int maxValueCmp=getMaxValueAttemptCmp();
				if (getAttemptsCmp()>maxValueCmp)
				{
					setAttemptsCmp(maxValueCmp);
				}
			}
		}
	}
}
