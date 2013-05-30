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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;


@SuppressWarnings("serial")
public class TestConditionBean extends ConditionBean implements Serializable
{
	public final static String TYPE="CONDITION_TYPE_TEST";
	
	public final static String TEST_RIGHT="TEST_RIGHT";
	public final static String TEST_NOTRIGHT="TEST_NOTRIGHT";
	public final static String TEST_WRONG="TEST_WRONG";
	public final static String TEST_NOTWRONG="TEST_NOTWRONG";
	public final static String TEST_PASSED="TEST_PASSED";
	public final static String TEST_NOTPASSED="TEST_NOTPASSED";
	
	public final static Map<String,String> TESTS;
	static
	{
		TESTS=new LinkedHashMap<String,String>();
		TESTS.put(TEST_RIGHT,"right");
		TESTS.put(TEST_NOTRIGHT,"notright");
		TESTS.put(TEST_WRONG,"wrong");
		TESTS.put(TEST_NOTWRONG,"notwrong");
		TESTS.put(TEST_PASSED,"passed");
		TESTS.put(TEST_NOTPASSED,"notpassed");
	}
	
	public String test;
	
	public TestConditionBean()
	{
		super(TYPE);
		this.test=null;
	}
	
	public TestConditionBean(String testValue)
	{
		super(TYPE);
		setTestValue(testValue);
	}
	
	public TestConditionBean(TestConditionBean otherTestCondition)
	{
		super(TYPE);
		this.test=otherTestCondition.test;
	}
	
	public String getTest()
	{
		return test;
	}

	public void setTest(String test)
	{
		if (TESTS.containsKey(test))
		{
			this.test=test;
		}
		else
		{
			this.test=null;
		}
	}
	
	public String getTestValue()
	{
		return TESTS.get(getTest());
	}
	
	public void setTestValue(String testValue)
	{
		String test=null;
		for (Entry<String,String> t:TESTS.entrySet())
		{
			if (t.getValue().equals(testValue))
			{
				test=t.getKey();
				break;
			}
		}
		this.test=test;
	}
}
