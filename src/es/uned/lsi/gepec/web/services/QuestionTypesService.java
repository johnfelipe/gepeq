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
package es.uned.lsi.gepec.web.services;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import es.uned.lsi.gepec.model.entities.QuestionType;

/**
 * Manages question types.
 */
@ManagedBean(eager=true)
@ApplicationScoped
@SuppressWarnings("serial")
public class QuestionTypesService implements Serializable
{
	@ManagedProperty(value="#{configurationService}")
	ConfigurationService configurationService;
	@ManagedProperty(value="#{localizationService}")
	LocalizationService localizationService;
	
	public QuestionTypesService()
	{
	}
	
	public void setConfigurationService(ConfigurationService configurationService)
	{
		this.configurationService=configurationService;
	}
	
	public void setLocalizationService(LocalizationService localizationService)
	{
		this.localizationService=localizationService;
	}
	
	/** Question types */
	private Map<String,QuestionType> questionTypes=null;
	
	/**
	 * @param name Question type's name
	 * @return Question type
	 * @throws ServiceException
	 */
	public QuestionType getQuestionType(String name) throws ServiceException
	{
		if (questionTypes==null)
		{
			loadQuestionTypes();
		}
		return questionTypes.get(name); 
	}
	
	/**
	 * @return Question types
	 * @throws ServiceException
	 */
	public List<QuestionType> getQuestionTypes() throws ServiceException
	{
		if (questionTypes==null)
		{
			loadQuestionTypes();
		}
		List<QuestionType> questionTypes=new ArrayList<QuestionType>();
		for (QuestionType questionType:this.questionTypes.values())
		{
			questionTypes.add(questionType);
		}
		return questionTypes;
	}
	
	/**
	 * Load question types from /WEB-INF/questiontypes.xml
	 * @throws ServiceException
	 */
	private void loadQuestionTypes() throws ServiceException
	{
		loadQuestionTypes(configurationService.getApplicationPath());
	}
	
	/**
	 * Load question types from /WEB-INF/questiontypes.xml
	 * @param applicationPath Application path
	 * @throws ServiceException
	 */
	private void loadQuestionTypes(String applicationPath) throws ServiceException
	{
		questionTypes=new LinkedHashMap<String,QuestionType>();
		try
		{
			DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
			DocumentBuilder parser=factory.newDocumentBuilder();
			
			StringBuffer questionTypesPath=new StringBuffer();
			questionTypesPath.append(applicationPath);
			questionTypesPath.append(File.separatorChar);
			questionTypesPath.append("WEB-INF");
			questionTypesPath.append(File.separatorChar);
			questionTypesPath.append("questiontypes.xml");
			
			Document document=parser.parse(new File(questionTypesPath.toString()));
			
			NodeList questionTypes=document.getElementsByTagName("QuestionType");
			for (int i=0;i<questionTypes.getLength();i++)
			{
				Element questionType=(Element)questionTypes.item(i);
				String name=
					questionType.getElementsByTagName(QuestionType.QUESTION_TYPE_NAME).item(0).getTextContent();
				String newView=
					questionType.getElementsByTagName(QuestionType.QUESTION_TYPE_NEW_VIEW).item(0).getTextContent();
				String updateView=
					questionType.getElementsByTagName(QuestionType.QUESTION_TYPE_UPDATE_VIEW).item(0).getTextContent();
				String className=
					questionType.getElementsByTagName(QuestionType.QUESTION_TYPE_CLASS_NAME).item(0).getTextContent();
				this.questionTypes.put(name,new QuestionType(name,newView,updateView,className));
			}
		}
		catch (Exception e)
		{
			questionTypes=null;
			String questionTypesFatalError=null;
			try
			{
				questionTypesFatalError=localizationService.getLocalizedMessage("QUESTION_TYPES_FATAL_ERROR");
			}
			catch (ServiceException se)
			{
				questionTypesFatalError="Question types configuration fatal error.";
			}
			throw new ServiceException(questionTypesFatalError,e);
		}
	}
	
	/**
	 * Forces to reload question types from questiontypes.xml next time we need to access any of them.<br/><br/>
	 * It can be used if it is needed to apply changes done to questiontypes.xml without restarting server.
	 */
	public void resetQuestionTypes()
	{
		questionTypes=null;
	}
}
