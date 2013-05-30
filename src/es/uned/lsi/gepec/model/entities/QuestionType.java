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
package es.uned.lsi.gepec.model.entities;

import java.io.Serializable;

/**
 * Class to define a question type. 
 */
@SuppressWarnings("serial")
public class QuestionType implements Serializable
{
	/** Name used in /WEB-INF/questiontypes.xml for question's type name */
	public final static String QUESTION_TYPE_NAME="Name";
	
	/** Name used in /WEB-INF/questiontypes.xml for view name for a new question of this type */
	public final static String QUESTION_TYPE_NEW_VIEW="NewView";
	
	/** Name used in /WEB-INF/questiontypes.xml for view name for updating a question of this type */
	public final static String QUESTION_TYPE_UPDATE_VIEW="UpdateView";
	
	/** Name used in /WEB-INF/questiontypes.xml for class name used for this question's type */
	public final static String QUESTION_TYPE_CLASS_NAME="ClassName";
	
	/** Question's type name */
	private String name;
	
	/** View name for a new question of this type */
	private String newView;
	
	/** View name for updating a question of this type */
	private String updateView; 
	
	/** Class name used for this question's type */
	private String className;
	
	/**
	 * Instantiates a new question's type.
	 * @param name Question's type name
	 * @param newView View name for a new question of this type
	 * @param updateView View name for updating a question of this type
	 * @param className Class name used for this question's type
	 */
	public QuestionType(String name,String newView,String updateView,String className)
	{
		this.name=name;
		this.newView=newView;
		this.updateView=updateView;
		this.className=className;
	}
	
	/**
	 * @return Question's type name
	 */
	public String getName()
	{
		return name;
	}
	
	/**
	 * Set question's type name.
	 * @param name Question's type name
	 */
	public void setName(String name)
	{
		this.name=name;
	}
	
	/**
	 * @return View name for a new question of this type
	 */
	public String getNewView()
	{
		return newView;
	}
	
	/**
	 * Set view name for a new question of this type.
	 * @param newView View name for a new question of this type
	 */
	public void setNewView(String newView)
	{
		this.newView=newView;
	}
	
	/**
	 * @return View name for updating a question of this type
	 */
	public String getUpdateView()
	{
		return updateView;
	}
	
	/**
	 * Set view name for updating a question of this type.
	 * @param updateView View name for updating a question of this type
	 */
	public void setUpdateView(String updateView)
	{
		this.updateView=updateView;
	}
	
	/**
	 * @return Class name used for this question's type
	 */
	public String getClassName()
	{
		return className;
	}
	
	/**
	 * Set class name used for this question's type.
	 * @param className Class name used for this question's type
	 */
	public void setClassName(String className)
	{
		this.className=className;
	}
	
	/**
	 * @return Full class name used for this question's type including package's name
	 */
	public String getFullClassName()
	{
		StringBuffer fullClassName=new StringBuffer("es.uned.lsi.gepec.model.entities.");
		fullClassName.append(className);
		return fullClassName.toString();
	}
	
	/**
	 * @return Class used for this question's type
	 * @throws ClassNotFoundException
	 */
	@SuppressWarnings("rawtypes")
	public Class getQuestionTypeClass() throws ClassNotFoundException 
	{
		return Class.forName(getFullClassName());
	}
	
	@Override
	public String toString()
	{
		return name;
	}
}