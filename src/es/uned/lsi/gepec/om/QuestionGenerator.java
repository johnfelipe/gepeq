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
package es.uned.lsi.gepec.om;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import javax.el.ELContext;
import javax.el.ELResolver;
import javax.faces.context.FacesContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.axis.encoding.Base64;
import org.hibernate.Hibernate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;

import es.uned.lsi.gepec.model.entities.Answer;
import es.uned.lsi.gepec.model.entities.DragDropAnswer;
import es.uned.lsi.gepec.model.entities.DragDropQuestion;
import es.uned.lsi.gepec.model.entities.Feedback;
import es.uned.lsi.gepec.model.entities.MultichoiceQuestion;
import es.uned.lsi.gepec.model.entities.OmXmlQuestion;
import es.uned.lsi.gepec.model.entities.Question;
import es.uned.lsi.gepec.model.entities.QuestionRelease;
import es.uned.lsi.gepec.model.entities.QuestionResource;
import es.uned.lsi.gepec.model.entities.QuestionType;
import es.uned.lsi.gepec.model.entities.Resource;
import es.uned.lsi.gepec.model.entities.TrueFalseQuestion;
import es.uned.lsi.gepec.model.entities.User;
import es.uned.lsi.gepec.om.axis.OmDevProxy;
import es.uned.lsi.gepec.om.axis.OmQeProxy;
import es.uned.lsi.gepec.om.axis.OmTnProxy;
import es.uned.lsi.gepec.util.HibernateUtil;
import es.uned.lsi.gepec.util.HibernateUtil.Operation;
import es.uned.lsi.gepec.util.OmTnEncryptor;
import es.uned.lsi.gepec.util.OmTnProEncryptor;
import es.uned.lsi.gepec.web.QuestionBean;
import es.uned.lsi.gepec.web.ResourceBean;
import es.uned.lsi.gepec.web.backbeans.FeedbackBean;
import es.uned.lsi.gepec.web.services.LocalizationService;
import es.uned.lsi.gepec.web.services.QuestionTypesService;
import es.uned.lsi.gepec.web.services.ResourcesService;
import es.uned.lsi.gepec.web.services.UserSessionService;

public class QuestionGenerator
{
	// Needed because reserved Java words are not allowed inside packages
	private final static String[] JAVA_RESERVED_WORDS=
	{
		"abstract",
		"boolean",
		"break",
		"byte",
		"case",
		"catch",
		"char",
		"class",
		"const",
		"continue",
		"default",
		"do",
		"double",
		"else",
		"extends",
		"false",
		"final",
		"finally",
		"float",
		"for",
		"goto",
		"if",
		"implements",
		"import",
		"instanceof",
		"int",
		"interface",
		"long",
		"native",
		"new",
		"null",
		"package",
		"private",
		"protected",
		"public",
		"return",
		"short",
		"static",
		"strictfp",
		"super",
		"switch",
		"synchronized",
		"this",
		"throw",
		"throws",
		"transient",
		"true",
		"try",
		"void",
		"volatile",
		"while"
	};
	
	private final static String OM_XML_GENERIC_QUESTION_VARIABLE="GenericQuestion";
	private final static String OM_XML_QUESTION_IMAGE_VARIABLE="QuestionImage";
	private final static String OM_XML_QUESTION_IMAGE_GAP_VARIABLE="QuestionImageGap";
	private final static String OM_XML_QUESTION_TEXT_VARIABLE="QuestionText";
	private final static String OM_XML_QUESTION_TEXT_GAP_VARIABLE="QuestionTextGap";
	private final static String OM_XML_SUBMIT_BUTTON_VARIABLE="SubmitButton";
	private final static String OM_XML_PASS_BUTTON_VARIABLE="PassButton";
	private final static String OM_XML_CORRECT_VARIABLE="Correct";
	private final static String OM_XML_CORRECT_GAP_VARIABLE="CorrectGap";
	private final static String OM_XML_CORRECT_IMAGE_VARIABLE="CorrectImage";
	private final static String OM_XML_CORRECT_IMAGE_GAP_VARIABLE="CorrectImageGap";
	private final static String OM_XML_INCORRECT_VARIABLE="Incorrect";
	private final static String OM_XML_INCORRECT_GAP_VARIABLE="IncorrectGap";
	private final static String OM_XML_INCORRECT_IMAGE_VARIABLE="IncorrectImage";
	private final static String OM_XML_INCORRECT_IMAGE_GAP_VARIABLE="IncorrectImageGap";
	private final static String OM_XML_STILL_VARIABLE="Still";
	private final static String OM_XML_STILL_GAP_VARIABLE="StillGap";
	private final static String OM_XML_PASS_VARIABLE="Pass";
	private final static String OM_XML_PASS_GAP_VARIABLE="PassGap";
	private final static String OM_XML_PASS_IMAGE_VARIABLE="PassImage";
	private final static String OM_XML_PASS_IMAGE_GAP_VARIABLE="PassImageGap";
	private final static String OM_XML_FINAL_VARIABLE="Final";
	private final static String OM_XML_FINAL_GAP_VARIABLE="FinalGap";
	private final static String OM_XML_FINAL_IMAGE_VARIABLE="FinalImage";
	private final static String OM_XML_FINAL_IMAGE_GAP_VARIABLE="FinalImageGap";
	private final static String OM_XML_RESOURCE_VARIABLE="Resource";
	private final static String OM_XML_RESOURCE_GAP_VARIABLE="ResourceGap";
	private final static String OM_XML_TRY_AGAIN_BUTTON="TryAgainButton";
	private final static String OM_XML_NEXT_QUESTION_BUTTON="NextQuestionButton";
	
	/**
	 * Localization service.
	 */
	private static LocalizationService localizationService=null;
	
	/**
	 * Question types service.
	 */
	private static QuestionTypesService questionTypesService=null;
	
	/**
	 * Resources service.
	 */
	private static ResourcesService resourcesService=null;
	
	/**
	 * User session service.
	 */
	private static UserSessionService userSessionService=null;
	
	/**
	 * Question bean.
	 */
	private static QuestionBean questionBean=null;
	
	/**
	 * Generate a question.xml and a GenericQuestion.java files for received question and put them
	 * in the indicated path.
	 * @param question Question
	 * @param path Path
	 * @throws Exception
	 */
	public static void generateQuestion(Question question,String path) throws Exception
	{
		initializeGEPEQServices(question);
		initialGenerateChecks(question,path);
		Document doc=createQuestionDocument(question);
		createFullPathDirectory(question.getPackage(),path);
		createQuestionXmlFile(doc,question.getPackage(),path);
		createGenericQuestionJavaFile(question.getPackage(),path);
	}
	
	/**
	 * Creates a question on OM developer web application.
	 * @param packageName Package's name
	 * @param path Path
	 * @param extraPackages Extra packages to include in question's jar
	 * @param omURL OM developer web application URL 
	 */
	public static void createQuestion(String packageName,String path,List<String> extraPackages,String omURL) 
			throws Exception
	{
		if (isEmpty(packageName))
		{
			throw new Exception("Package name should not be empty.");
		}
		if (isEmpty(path))
		{
			throw new Exception("Path to question should not be empty.");
		}
		checkPackage(packageName);			
		
		StringBuffer omDevWsURL=new StringBuffer(omURL);
		if (omURL.charAt(omURL.length()-1)!='/')
		{
			omDevWsURL.append('/');
		}
		omDevWsURL.append("services/OmDev");
		OmDevProxy omDevWs=new OmDevProxy();
		omDevWs.setEndpoint(omDevWsURL.toString());
		omDevWs.createQuestion(packageName,path,extraPackages.toArray(new String[0]));
	}
	
	/**
	 * Builds a question into a target jar file on OM developer web application.
	 * <br/><br/>
	 * Be careful that question must have been previously created at that 'questions' folder with 
	 * createQuestion method.
	 * @param packageName Package's name
	 * @param omURL OM developer web application URL 
	 * @return true if build succeeded, false if it failed
	 * @throws Exception
	 */
	public static boolean buildQuestion(String packageName,String omURL) throws Exception
	{
		StringBuffer omDevWsURL=new StringBuffer(omURL);
		if (omURL.charAt(omURL.length()-1)!='/')
		{
			omDevWsURL.append('/');
		}
		omDevWsURL.append("services/OmDev");
		OmDevProxy omDevWs=new OmDevProxy();
		omDevWs.setEndpoint(omDevWsURL.toString());
		return omDevWs.buildQuestion(packageName);
	}
	
	/**
	 * Publish a question on OM Test Navigator web application production environment.
	 * @param questionRelease Question release
	 * @param omURL OM developer web application URL 
	 * @param omTnProURL OM test navigator web application URL (production environment) 
	 * @param deployJar Flag to indicate that we need to copy question's far file from OM Developer web application
	 * to OM Test Navigator web application (production environment)
	 * @throws Exception
	 */
	public static void publishQuestion(QuestionRelease questionRelease,String omURL,String omTnProURL,
		boolean deployJar) throws Exception
	{
		initializeGEPEQServices(questionRelease.getQuestion());
		initialDeployChecks(questionRelease.getQuestion(),omURL,omTnProURL,deployJar,true,true);
		String packageName=questionRelease.getQuestion().getPackage();
		if (deployJar)
		{
			copyJarFile(packageName,omURL,omTnProURL,true);
		}
		Document deployDoc=createDeployDocument(questionRelease);
		createDeployXmlFile(deployDoc,packageName,omTnProURL,true);
	}
	
	/**
	 * Deploys a question on OM Test Navigator web application test environment.
	 * @param question Question
	 * @param omURL OM developer web application URL 
	 * @param omTnURL OM test navigator web application URL (test environment)
	 * @param deployJar Flag to indicate that we need to copy question's far file from OM Developer web application
	 * to OM Test Navigator web application (test environment)
	 * @param overwrite Flag to indicate that referenced OM test navigator deploy xml file and question's jar file 
	 * will be overwritten if they exist (true) or not (false)
	 * @throws Exception
	 */
	public static void deployQuestion(Question question,String omURL,String omTnURL,boolean deployJar,
		boolean overwrite) throws Exception
	{
		initializeGEPEQServices(question);
		initialDeployChecks(question,omURL,omTnURL,deployJar,overwrite,false);
		String packageName=question.getPackage();
		if (deployJar)
		{
			copyJarFile(packageName,omURL,omTnURL,false);
		}
		Document deployDoc=createDeployDocument(question);
		createDeployXmlFile(deployDoc,packageName,omTnURL,false);
	}
	
	/**
	 * Deletes a question from OM Developer and OM Test Navigator web applications. 
	 * @param packageName Package's name
	 * @param omURL OM developer web application URL 
	 * @param omTnURL OM test navigator web application URL 
	 * @throws Exception
	 */
	public static void deleteQuestion(String packageName,String omURL,String omTnURL) throws Exception
	{
		checkPackage(packageName);
		try
		{
			deleteOMQuestion(packageName,omURL);
		}
		catch (Exception e)
		{
			// Ignore delete error
		}
		try
		{
			deleteXmlFile(packageName,omTnURL,false);
		}
		catch (Exception e)
		{
			// Ignore delete error
		}
		try
		{
			deleteJarFile(packageName,omTnURL);
		}
		catch (Exception e)
		{
			// Ignore delete error
		}
	}
	
	/**
	 * Deletes a question release from OM Test Navigator production environment web application. 
	 * @param packageName Package's name
	 * @param omTnProURL OM test navigator production environment web application URL 
	 * @throws Exception
	 */
	public static void unpublishQuestionRelease(String packageName,String omTnProURL) throws Exception
	{
		checkPackage(packageName);
		try
		{
			deleteXmlFile(packageName,omTnProURL,true);
		}
		catch (Exception e)
		{
			// Ignore delete error
		}
	}
	
	/**
	 * Perform several initial checks before start generating any files to generate a question.
	 * @param question Question
	 * @param path Path
	 * @throws Exception
	 */
	private static void initialGenerateChecks(Question question,String path) throws Exception
	{
		checkQuestion(question);
		checkPackage(question.getPackage());
		checkPath(path);
	}
	
	/**
	 * Perform several initial checks before start generating any files to deploy a question on OM Test Navigator.
	 * @param question Question
	 * @param omURL OM developer web application URL 
	 * @param omTnURL OM test navigator web application URL (test or production environment)
	 * @param deployJar Flag to indicate that we need to copy question's far file from 
	 * OM Developer web application to OM Test Navigator web application
	 * @param overwrite Flag to indicate that referenced OM Test Navigator deploy xml file 
	 * and question's jar file will be overwritten if they exist (true) or not (false)
	 * @param publish true if we are publishing the xml to a Test Navigator production environment, 
	 * false if we are deploying the jar to a Test Navigator test environment
	 * @throws Exception
	 */
	private static void initialDeployChecks(Question question,String omURL,String omTnURL,boolean deployJar,
		boolean overwrite,boolean publish) throws Exception
	{
		checkQuestion(question);
		String packageName=question.getPackage();
		checkPackage(packageName);
		
		if (deployJar)
		{
			// Check that referenced question has been built on OM developer web application
			checkQuestionJar(packageName,omURL);
		}
		
		if (!overwrite)
		{
			// Check that referenced deploy xml file don't exist on OM test navigator web application
			checkTestNavigatorXml(packageName,omTnURL);
			
			if (deployJar)
			{
				// Check that referenced question's jar file don't exist on OM test navigator web application
				checkTestNavigatorQuestion(packageName,omTnURL,publish);
			}
		}
	}
	
	/**
	 * Checks if question is valid
	 * @param question Question
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	private static void checkQuestion(Question question) throws Exception
	{
		boolean ok=false;
		if (question==null)
		{
			throw new Exception("Error. There is no question to generate");
		}
		if (question.getType()==null)
		{
			throw new Exception("Error. The question has no type");
		}
		if (questionTypesService==null)
		{
			initializeGEPEQServices(question);
			if (questionTypesService==null)
			{
				throw new Exception("Error. Question types service not initialized");
			}
		}
		for (QuestionType questionType:questionTypesService.getQuestionTypes())
		{
			if (question.getType().equals(questionType.getName()))
			{
				try
				{
					if (questionType.getQuestionTypeClass().isAssignableFrom(Hibernate.getClass(question)))
					{
						ok=true;
						break;
					}
					else
					{
						StringBuffer error=new StringBuffer();
						error.append("Error. The question has the supported type ");
						error.append(questionType.getName());
						error.append(" but it is not an instance of ");
						error.append(questionType.getFullClassName());
						error.append(" class");
						throw new Exception(error.toString());
					}
				}
				catch (ClassNotFoundException e)
				{
					StringBuffer error=new StringBuffer();
					error.append("Error. Class ");
					error.append(questionType.getFullClassName());
					error.append(" for question type ");
					error.append(questionType.getName());
					error.append(" is not found");
					throw new Exception(error.toString());
				}
			}
		}
		if (!ok)
		{
			StringBuffer error=new StringBuffer();
			error.append("Error. The question type ");
			error.append(question.getType());
			error.append(" is still not supported");
			throw new Exception(error.toString());
		}
		if (question.getQuestionText()==null)
		{
			throw new Exception("Error. The question has no text");
		}
	}
	
	/**
	 * Checks if package is valid
	 * @param packageName Package's name
	 */
	private static void checkPackage(String packageName) throws Exception
	{
		if (packageName==null || packageName.equals(""))
		{
			throw new Exception("Error. There is no package");
		}
		else
		{
			StringBuffer error=new StringBuffer();
			error.append("Error. Syntax of package ");
			error.append(packageName);
			error.append(" it is not valid");
			StringBuffer word=new StringBuffer();
			boolean canFollowDot=false;
			for (int i=0;i<packageName.length();i++)
			{
				char c=packageName.charAt(i);
				if (c=='.')
				{
					if (canFollowDot)
					{
						if (isJavaReservedWord(word.toString()))
						{
							throw new Exception(error.toString());
						}
						else
						{
							word.setLength(0);
							canFollowDot=false;
						}
					}
					else
					{
						throw new Exception(error.toString());
					}
				}
				else if (Character.isLetter(c) || c=='_' || c== '$' || Character.isDigit(c))
				{
					word.append(c);
					canFollowDot=true;
				}
				else
				{
					throw new Exception(error.toString());
				}
			}
		}
	}
	
	/**
	 * Check if path is valid
	 * @param path Path
	 * @throws Exception
	 */
	public static void checkPath(String path) throws Exception
	{
		if (path==null)
		{
			throw new Exception("Error. There is no path");
		}
		try
		{
			File fPath=new File(path);
			if (!fPath.exists())
			{
				StringBuffer error=new StringBuffer("Error. Path ");
				error.append(path);
				error.append(" not exists"); 
				throw new Exception(error.toString());
			}
			if (!fPath.isDirectory())
			{
				StringBuffer error=new StringBuffer("Error. Path ");
				error.append(path);
				error.append(" is not a directory"); 
				throw new Exception(error.toString());
			}
			if (!fPath.canExecute() || !fPath.canRead() || !fPath.canWrite())
			{
				StringBuffer error=new StringBuffer("Error. Path ");
				error.append(path);
				error.append(" is a valid directory but you have no permissions to access it"); 
				throw new Exception(error.toString());
			}
		}
		catch (SecurityException se)
		{
			StringBuffer error=new StringBuffer("Error. You have no permissions to access path: ");
			error.append(path);
			throw new Exception(error.toString());
		}
	}
	
	/**
	 * Checks that referenced question has been built on OM developer web application.
	 * @param packageName Package's name
	 * @param omURL OM developer web application URL 
	 * @throws Exception
	 */
	public static void checkQuestionJar(String packageName,String omURL) throws Exception
	{
		StringBuffer omDevWsURL=new StringBuffer(omURL);
		if (omURL.charAt(omURL.length()-1)!='/')
		{
			omDevWsURL.append('/');
		}
		omDevWsURL.append("services/OmDev");
		OmDevProxy omDevWs=new OmDevProxy();
		omDevWs.setEndpoint(omDevWsURL.toString());
		if (!omDevWs.existQuestionJar(packageName))
		{
			StringBuffer error=new StringBuffer("Error. Question ");
			error.append(packageName);
			error.append(" has not been built on OM Developer Web Application"); 
			throw new Exception(error.toString());
		}
	}
	
	/**
	 * Checks that referenced question don't exist on OM Test Navigator web application 
	 * @param packageName Package's name
	 * @param omTnURL OM test navigator web application URL 
	 * @throws Exception
	 */
	public static void checkTestNavigatorXml(String packageName,String omTnURL) throws Exception
	{
		StringBuffer omTnWsURL=new StringBuffer(omTnURL);
		if (omTnURL.charAt(omTnURL.length()-1)!='/')
		{
			omTnWsURL.append('/');
		}
		omTnWsURL.append("services/OmTn");
		OmTnProxy omTnWs=new OmTnProxy();
		omTnWs.setEndpoint(omTnWsURL.toString());
		
		if (omTnWs.existQuestionXml(packageName))
		{
			StringBuffer error=new StringBuffer("Error. Question ");
			error.append(packageName);
			error.append(" already exists on OM Test Navigator Web Application"); 
			throw new Exception(error.toString());
		}
	}
	
	/**
	 * Checks that referenced question's jar file don't exist on OM Test Navigator web application
	 * @param packageName Package's name
	 * @param omTnURL OM test navigator web application URL (test or production environment)
	 * @param publish true if we are publishing the xml to a Test Navigator production environment, 
	 * false if we are deploying the jar to a Test Navigator test environment
	 * @throws Exception
	 */
	private static void checkTestNavigatorQuestion(String packageName,String omTnURL,boolean publish) 
		throws Exception
	{
		StringBuffer omTnWsURL=new StringBuffer(omTnURL);
		if (omTnURL.charAt(omTnURL.length()-1)!='/')
		{
			omTnWsURL.append('/');
		}
		omTnWsURL.append("services/OmTn");
		OmTnProxy omTnWs=new OmTnProxy();
		omTnWs.setEndpoint(omTnWsURL.toString());
		if (!omTnWs.existQuestionJar(packageName,publish?null:"1.0"))
		{
			StringBuffer error=new StringBuffer("Error. Jar for question ");
			error.append(packageName);
			error.append(" already exists on OM Test Navigator Web Application ");
			error.append(publish?"production":"test");
			error.append(" environment");
			throw new Exception(error.toString());
		}
	}
	
	/**
	 * Check if word is a Java reserved word or not 
	 * @param word Word
	 * @return true if word is a Java reserved word, false otherwise
	 */
	private static boolean isJavaReservedWord(String word)
	{
		boolean reserved=false;
		for (String javaWord:JAVA_RESERVED_WORDS)
		{
			if (javaWord.equals(word))
			{
				reserved=true;
				break;
			}
		}
		return reserved;
	}
	
	/**
	 * Initializes GEPEQ services.<br/><br/>
	 * Actually only localization and resources services are initialized.
	 */
	private static void initializeGEPEQServices(Question question)
	{
		FacesContext facesContext=FacesContext.getCurrentInstance();
		if (facesContext==null)
		{
			localizationService=null;
			questionTypesService=null;
			resourcesService=null;
			userSessionService=null;
			questionBean=null;
		}
		else
		{
			// Get EL Resolver
			ELResolver resolver=facesContext.getApplication().getELResolver();
			
			// Get EL context
			ELContext elContext=facesContext.getELContext();
			
	        localizationService=(LocalizationService)resolver.getValue(elContext,null,"localizationService");
	        questionTypesService=(QuestionTypesService)resolver.getValue(elContext,null,"questionTypesService");
	        resourcesService=(ResourcesService)resolver.getValue(elContext,null,"resourcesService");
	        userSessionService=(UserSessionService)resolver.getValue(elContext,null,"userSessionService");
	        questionBean=(QuestionBean)resolver.getValue(elContext,null,"questionBean");
	        questionBean.setQuestion(question);
		}
	}
	
	/**
	 * @param str String to localize
	 * @param defaultStr Default string
	 * @return Localized string or default string if localization service is not available
	 */
	private static String getLocalizedString(String str,String defaultStr)
	{
		return localizationService==null?defaultStr:localizationService.getLocalizedMessage(str);
	}
	
	/**
	 * @param resource Resource
	 * @return true if resource is an image, false if it is not an image or if resources service
	 * is not available
	 */
	private static boolean isImage(Resource resource)
	{
		boolean isImg=false;
		if (resource!=null && resourcesService!=null && userSessionService!=null)
		{
			ResourceBean res=new ResourceBean(resource);
			res.setResourcesService(resourcesService);
			res.setUserSessionService(userSessionService);
			isImg=res.isImage(userSessionService.getCurrentUserOperation());
		}
		return isImg;
	}
	
	/**
	 * @param operation Operation
	 * @param draggableItem Draggable item
	 * @return Draggable item name or null if it is not possible to get it
	 */
	private static String getDraggableItemName(Operation operation,DragDropAnswer draggableItem)
	{
		String draggableItemName=questionBean==null?null:
			questionBean.getNumberedDraggableItemName(questionBean.getCurrentUserOperation(operation),draggableItem);
		return draggableItemName==null || draggableItemName.equals("")?null:draggableItemName;
	}
	/**
	 * @param operation Operation
	 * @param answer Answer
	 * @return Answer name or null if it is not possible to get it
	 */
	private static String getDroppableAnswerName(Operation operation,DragDropAnswer answer)
	{
		String answerName=questionBean==null?
			null:questionBean.getNumberedDroppableAnswerName(questionBean.getCurrentUserOperation(operation),answer);
		return answerName==null || answerName.equals("")?null:answerName;
	}
	
	/**
	 * Create and return an instance of org.w3c.Document class with the XML document appropiated to
	 * a question.
	 * @param question Question
	 * @return An instance of org.w3c.Document class with the XML document
	 * @throws Exception
	 */
	private static Document createQuestionDocument(Question question) throws Exception
	{
		Document doc=null;
		if (question.getType().equals("OM_XML"))
		{
			doc=createOmXmlQuestionDocument(HibernateUtil.unProxyToClass(question,OmXmlQuestion.class));
		}
		else
		{
			doc=createNonOmXmlQuestionDocument(question);
		}
		return doc;
	}
	
	/**
	 * Create and return an instance of org.w3c.Document class with the XML document appropiated to
	 * a question that is not an XML (OpenMark syntax) question.
	 * @param question Question that is not an XML (OpenMark syntax) question
	 * @return An instance of org.w3c.Document class with the XML document
	 * @throws Exception
	 */
	private static Document createNonOmXmlQuestionDocument(Question question) throws Exception
	{
		// Initialize document
		DocumentBuilderFactory dbf=DocumentBuilderFactory.newInstance();
		DocumentBuilder db=dbf.newDocumentBuilder();
		Document doc=db.newDocument();
		
		// Creating XML tree
		
		// Create root
		Element root=doc.createElement("question");
		root.setAttribute("class",getFullClassName(question.getPackage()));
		doc.appendChild(root);
		
		// Add scoring
		Element scoring=doc.createElement("scoring");
		root.appendChild(scoring);
		Element marks=doc.createElement("marks");
		scoring.appendChild(marks);
		Text marksText=doc.createTextNode("3");
		marks.appendChild(marksText);
		
		// Add layout
		Element layout=doc.createElement("layout");
		root.appendChild(layout);
		Element row=doc.createElement("row");
		row.setAttribute("height","0");						// if we declare height=0, question's box height
															// apparently is considered to be the sum of 
															// their component's heights
		layout.appendChild(row);
		Element inputColumn=doc.createElement("column");
		inputColumn.setAttribute("width","296");			// larger width values doesn't work well
		layout.appendChild(inputColumn);
		Element outputColumn=doc.createElement("column");
		outputColumn.setAttribute("width","296");			// larger width values doesn't work well
		layout.appendChild(outputColumn);
		
		// Add "inputbox" box
		Element inputBox=doc.createElement("box");
		inputBox.setAttribute("gridx","0");
		inputBox.setAttribute("gridy","0");
		inputBox.setAttribute("id","inputbox");
		inputBox.setAttribute("background","input");
		root.appendChild(inputBox);
		
		// Add resource if needed
		if (question.getResource()!=null)
		{
			// Test resource type (actually only images are supported)
			if (isImage(question.getResource()))
			{
				Element image=doc.createElement("image");
				image.setAttribute("filePath",getOnlyFileName(question.getResource().getFileName()));
				image.setAttribute("alt","");
				if (question.getResourceWidth()>0 && question.getResourceHeight()>0)
				{
					image.setAttribute("width",Integer.toString(question.getResourceWidth()));
					image.setAttribute("height",Integer.toString(question.getResourceHeight()));
				}
				inputBox.appendChild(image);
				
				// Add gap
				inputBox.appendChild(doc.createElement("gap"));
			}
		}
		
		if (question.getQuestionText()!=null)
		{
			// Add question's text
			for (Node n:createTextWithBreaks(doc,question.getQuestionText(),question.isDisplayEquations()))
			{
				inputBox.appendChild(n);
			}
			
			if (!question.getQuestionText().trim().equals(""))
			{
				// Add gap
				inputBox.appendChild(doc.createElement("gap"));
			}
		}
		
		// Add selectable answers
		if (question.getType().equals("TRUE_FALSE"))
		{
			Element trueFalseSelectableAnswers=
				createTrueFalseSelectableAnswers(doc,HibernateUtil.unProxyToClass(question,TrueFalseQuestion.class));
			inputBox.appendChild(trueFalseSelectableAnswers);
		}
		else if (question.getType().equals("MULTICHOICE"))
		{
			Element multichoiceSelectableAnswers=
				createMultichoiceSelectableAnswers(doc,HibernateUtil.unProxyToClass(question,MultichoiceQuestion.class));
			if (multichoiceSelectableAnswers!=null)
			{
				inputBox.appendChild(multichoiceSelectableAnswers);
			}
		}
		else if (question.getType().equals("DRAG_DROP"))
		{
			DragDropQuestion dragDropQuestion=HibernateUtil.unProxyToClass(question,DragDropQuestion.class);
			Element dragDropDraggableItems=createDragDropDraggableItems(doc,dragDropQuestion);
			if (dragDropDraggableItems!=null)
			{
				inputBox.appendChild(dragDropDraggableItems);
			}
			
			// Add gap
			inputBox.appendChild(doc.createElement("gap"));
			
			
			Element dragDropDroppableAnswers=createDragDropDroppableAnswers(doc,dragDropQuestion);
			if (dragDropDroppableAnswers!=null)
			{
				inputBox.appendChild(dragDropDroppableAnswers);
			}
		}
		
		// Add gap
		inputBox.appendChild(doc.createElement("gap"));
		
		// Add submit button
		Element submitButton=doc.createElement("button");
		submitButton.setAttribute("action","actionSubmit");
		submitButton.setAttribute("label",getLocalizedString("OM_ENTER_ANSWER","%%lENTERANSWER%%"));
		inputBox.appendChild(submitButton);
		
		// Add give up button
		Element giveUpButton=doc.createElement("button");
		giveUpButton.setAttribute("action","actionGiveUp");
		giveUpButton.setAttribute("label",getLocalizedString("OM_GIVE_UP","%%lGIVEUP%%"));
		inputBox.appendChild(giveUpButton);
		
		// Add "answerbox" box
		Element answerBox=doc.createElement("box");
		answerBox.setAttribute("gridx","1");
		answerBox.setAttribute("gridy","0");
		answerBox.setAttribute("id","answerbox");
		answerBox.setAttribute("display","no");
		answerBox.setAttribute("background","answer");
		root.appendChild(answerBox);
		
		// Add "right" feedback
		if (question.getCorrectFeedback()!=null || question.getCorrectFeedbackResource()!=null)
		{
			Element rightTextComponent=doc.createElement("t");
			rightTextComponent.setAttribute("id","right");
			answerBox.appendChild(rightTextComponent);
			
			// Add "right" feedback text if needed
			if (question.getCorrectFeedback()!=null)
			{
				for (Node n:createTextWithBreaks(doc,question.getCorrectFeedback(),question.isDisplayEquations()))
				{
					rightTextComponent.appendChild(n);
				}
				
				if (!question.getCorrectFeedback().trim().equals(""))
				{
					// Add gap
					rightTextComponent.appendChild(doc.createElement("gap"));
				}
			}
			
			// Add "right" feedback resource if needed
			if (question.getCorrectFeedbackResource()!=null)
			{
				// Test resource type (actually only images are supported)
				if (isImage(question.getCorrectFeedbackResource()))
				{
					Element image=doc.createElement("image");
					image.setAttribute(
						"filePath",getOnlyFileName(question.getCorrectFeedbackResource().getFileName()));
					image.setAttribute("alt","");
					if (question.getCorrectFeedbackResourceWidth()>0 && 
						question.getCorrectFeedbackResourceHeight()>0)
					{
						image.setAttribute("width",Integer.toString(question.getCorrectFeedbackResourceWidth()));
						image.setAttribute("height",Integer.toString(question.getCorrectFeedbackResourceHeight()));
					}
					rightTextComponent.appendChild(image);
					
					// Add gap
					rightTextComponent.appendChild(doc.createElement("gap"));
				}
			}
		}
		
		// Add "wrong" feedback
		if (question.getIncorrectFeedback()!=null || question.getCorrectFeedbackResource()!=null)
		{
			Element wrongTextComponent=doc.createElement("t");
			wrongTextComponent.setAttribute("id","wrong");
			answerBox.appendChild(wrongTextComponent);
			
			// Add "wrong" feedback text if needed
			if (question.getIncorrectFeedback()!=null)
			{
				for (Node n:createTextWithBreaks(doc,question.getIncorrectFeedback(),question.isDisplayEquations()))
				{
					wrongTextComponent.appendChild(n);
				}
				
				if (!question.getIncorrectFeedback().trim().equals(""))
				{
					// Add gap
					wrongTextComponent.appendChild(doc.createElement("gap"));
				}
			}
			
			// Add "wrong" feedback resource if needed
			if (question.getIncorrectFeedbackResource()!=null)
			{
				// Test resource type (actually only images are supported)
				if (isImage(question.getIncorrectFeedbackResource()))
				{
					Element image=doc.createElement("image");
					image.setAttribute(
						"filePath",getOnlyFileName(question.getIncorrectFeedbackResource().getFileName()));
					image.setAttribute("alt","");
					if (question.getIncorrectFeedbackResourceWidth()>0 && 
						question.getIncorrectFeedbackResourceHeight()>0)
					{
						image.setAttribute("width",Integer.toString(question.getIncorrectFeedbackResourceWidth()));
						image.setAttribute("height",Integer.toString(question.getIncorrectFeedbackResourceHeight()));
					}
					wrongTextComponent.appendChild(image);
					
					// Add gap
					wrongTextComponent.appendChild(doc.createElement("gap"));
				}
			}
		}
		
		// Add "still" feedback (needed to add at least an empty one to avoid an error)
		Element stillTextComponent=doc.createElement("t");
		stillTextComponent.setAttribute("id","still");
		answerBox.appendChild(stillTextComponent);
		if (question.getStillFeedback()!=null)
		{
			for (Node n:createTextWithBreaks(doc,question.getStillFeedback(),question.isDisplayEquations()))
			{
				stillTextComponent.appendChild(n);
			}
			if (!question.getStillFeedback().trim().equals(""))
			{
				// Add gap
				stillTextComponent.appendChild(doc.createElement("gap"));
			}
		}
		
		// Add "pass" feedback (needed to add at least an empty one to avoid an error)
		Element passTextComponent=doc.createElement("t");
		passTextComponent.setAttribute("id","pass");
		answerBox.appendChild(passTextComponent);
		
		// Add "pass" feedback text if needed
		if (question.getPassFeedback()!=null)
		{
			for (Node n:createTextWithBreaks(doc,question.getPassFeedback(),question.isDisplayEquations()))
			{
				passTextComponent.appendChild(n);
			}
			
			if (!question.getPassFeedback().trim().equals(""))
			{
				// Add gap
				passTextComponent.appendChild(doc.createElement("gap"));
			}
		}
		
		// Add "pass" feedback resource if needed
		if (question.getPassFeedbackResource()!=null)
		{
			// Test resource type (actually only images are supported)
			if (isImage(question.getPassFeedbackResource()))
			{
				Element image=doc.createElement("image");
				image.setAttribute("filePath",getOnlyFileName(question.getPassFeedbackResource().getFileName()));
				image.setAttribute("alt","");
				if (question.getPassFeedbackResourceWidth()>0 && question.getPassFeedbackResourceHeight()>0)
				{
					image.setAttribute("width",Integer.toString(question.getPassFeedbackResourceWidth()));
					image.setAttribute("height",Integer.toString(question.getPassFeedbackResourceHeight()));
				}
				passTextComponent.appendChild(image);
				
				// Add gap
				passTextComponent.appendChild(doc.createElement("gap"));
			}
		}
		
		// Add advanced fixed feedbacks
		for (Element advancedFixedFeedback:createAdvancedFeedbacks(doc,question,FeedbackBean.TYPE_FIXED))
		{
			answerBox.appendChild(advancedFixedFeedback);
		}
		
		// Add "feedback" text component (needed to add at least an empty one to avoid an error) 
		Element feedbackTextComponent=doc.createElement("t");
		feedbackTextComponent.setAttribute("id","feedback");
		answerBox.appendChild(feedbackTextComponent);
		
		// Add advanced normal feedbacks within "feedback" text component
		for (Element advancedNormalFeedback:createAdvancedFeedbacks(doc,question,FeedbackBean.TYPE_NORMAL))
		{
			feedbackTextComponent.appendChild(advancedNormalFeedback);
		}
		
		// Add "answer" feedback (needed to add at least an empty one to avoid an error)
		Element answerTextComponent=doc.createElement("t");
		answerTextComponent.setAttribute("id","answer");
		answerBox.appendChild(answerTextComponent);
		
		// Add "answer" feedback text if needed
		if (question.getAnswerFeedback()!=null)
		{
			for (Node n:createTextWithBreaks(doc,question.getAnswerFeedback(),question.isDisplayEquations()))
			{
				answerTextComponent.appendChild(n);
			}
			
			if (!question.getAnswerFeedback().trim().equals(""))
			{
				// Add gap
				answerTextComponent.appendChild(doc.createElement("gap"));
			}
		}
		
		// Add "answer" feedback resource if needed
		if (question.getFinalFeedbackResource()!=null)
		{
			// Test resource type (actually only images are supported)
			if (isImage(question.getFinalFeedbackResource()))
			{
				Element image=doc.createElement("image");
				image.setAttribute("filePath",getOnlyFileName(question.getFinalFeedbackResource().getFileName()));
				image.setAttribute("alt","");
				if (question.getFinalFeedbackResourceWidth()>0 && question.getFinalFeedbackResourceHeight()>0)
				{
					image.setAttribute("width",Integer.toString(question.getFinalFeedbackResourceWidth()));
					image.setAttribute("height",Integer.toString(question.getFinalFeedbackResourceHeight()));
				}
				answerTextComponent.appendChild(image);
				
				// Add gap
				answerTextComponent.appendChild(doc.createElement("gap"));
			}
		}
		
		// Add ok button
		Element okButton=doc.createElement("button");
		okButton.setAttribute("id","ok");
		okButton.setAttribute("action","actionOK");
		okButton.setAttribute("label",getLocalizedString("OM_TRY_AGAIN","%%lTRYAGAIN%%"));
		answerBox.appendChild(okButton);
		
		// Add next button
		Element nextButton=doc.createElement("button");
		nextButton.setAttribute("id","next");
		nextButton.setAttribute("action","actionOK");
		nextButton.setAttribute("label",getLocalizedString("OM_NEXT_QUESTION","%%lNEXTQUESTION%%"));
		nextButton.setAttribute("display","no");
		answerBox.appendChild(nextButton);
		
		// Create custom answerline for specific question types
		Element customAnswerLine=null;
		if (question.getType().equals("DRAG_DROP"))
		{
			customAnswerLine=createDragDropAnswerLine(doc);
		}
		
		// Add custom answerline if needed, otherwise we use default answerline
		if (customAnswerLine!=null)
		{
			root.appendChild(customAnswerLine);
		}
		
		// Add summaryline
		Element summaryLine=createSummaryLine(doc);
		root.appendChild(summaryLine);
		
		return doc;
	}
	
	/**
	 * Create and return an instance of org.w3c.Document class with the XML document appropiated to
	 * an XML (OpenMark syntax) question.
	 * @param question XML (OpenMark syntax) question
	 * @return An instance of org.w3c.Document class with the XML document
	 * @throws Exception
	 */
	private static Document createOmXmlQuestionDocument(OmXmlQuestion question) throws Exception
	{
		// Initialize document
		DocumentBuilderFactory dbf=DocumentBuilderFactory.newInstance();
		DocumentBuilder db=dbf.newDocumentBuilder();
		Document doc=db.parse(new InputSource(new StringReader(question.getXmlContent())));
		
		// We need to replace some special variables with the appropiated values
		replaceOmXmlVariables(doc,question);
		
		return doc;
	}
	
	private static void replaceOmXmlVariables(Document doc,OmXmlQuestion question)
	{
		replaceOmXmlVariables(doc,doc.getDocumentElement(),null,question);
	}
	
	private static void replaceOmXmlVariables(Document doc,Node node,Node nextSibling,OmXmlQuestion question)
	{
		if (node!=null)
		{
			if (node.getNodeType()==Node.ELEMENT_NODE)
			{
				// As this is an element node we need to replace variables within attributes
				NamedNodeMap attributes=node.getAttributes();
				for (int i=0;i<attributes.getLength();i++)
				{
					Node attribute=attributes.item(i);
					attribute.setNodeValue(replaceOmXmlVariablesAsString(
						node.getNodeName(),attribute.getNodeName(),attribute.getNodeValue(),question));
				}
				
				// Finally we need to replace special variables recursively within all its children
				NodeList childNodes=node.getChildNodes();
				
				// As we need to add and remove nodes while transversing the list we need to store childs
				// in our own list
				List<Node> childs=new ArrayList<Node>();
				for (int i=0;i<childNodes.getLength();i++)
				{
					childs.add(childNodes.item(i));
				}
				
				for (int i=0;i<childs.size();i++)
				{
					replaceOmXmlVariables(doc,childs.get(i),i==(childs.size()-1)?null:childs.get(i+1),question);
				}
			}
			else if (node.getNodeType()==Node.TEXT_NODE)
			{
				List<Node> replacedNodes=replaceOmXmlVariablesAsXMLNodes(doc,node.getNodeValue(),question);
				Node parentNode=node.getParentNode();
				Node nextNode=nextSibling;
				for (int i=replacedNodes.size()-1;i>=0;i--)
				{
					Node replacedNode=replacedNodes.get(i);
					if (nextNode==null)
					{
						parentNode.appendChild(replacedNode);
					}
					else
					{
						parentNode.insertBefore(replacedNode,nextNode);
					}
					nextNode=replacedNode;
				}
				parentNode.removeChild(node);
			}
		}
	}
	
	private static String replaceOmXmlVariablesAsString(String tagName,String attributeName,String text,
		OmXmlQuestion question)
	{
		StringBuffer newText=new StringBuffer();
		int i=0;
		while (i<text.length())
		{
			int iStartVar=text.indexOf("${",i);
			if (iStartVar!=-1)
			{
				if (iStartVar>i)
				{
					newText.append(text.substring(i,iStartVar));
					i=iStartVar;
				}
				int iEndVar=text.indexOf('}',iStartVar);
				if (iEndVar!=-1)
				{
					newText.append(getOmXmlVariableValueAsString(
						tagName,attributeName,text.substring(iStartVar+"${".length(),iEndVar),question));
					i=iEndVar+1;
				}
				else
				{
					newText.append(text.substring(iStartVar));
					i=text.length();
				}
			}
			else
			{
				newText.append(text.substring(i));
				i=text.length();
			}
		}
		return newText.toString();
	}
	
	private static String getOmXmlVariableValueAsString(String tagName,String attributeName,
		String variableName,OmXmlQuestion question)
	{
		String variableValue="";
		if (OM_XML_GENERIC_QUESTION_VARIABLE.equals(variableName))
		{
			variableValue=getOmXmlGenericQuestionValueAsString(question);
		}
		else if (OM_XML_QUESTION_IMAGE_VARIABLE.equals(variableName) || 
			OM_XML_QUESTION_IMAGE_GAP_VARIABLE.equals(variableName))
		{
			variableValue=getOmXmlQuestionImageValueAsString(tagName,attributeName,question);
		}
		else if (OM_XML_QUESTION_TEXT_VARIABLE.equals(variableName) || 
			OM_XML_QUESTION_TEXT_GAP_VARIABLE.equals(variableName))
		{
			variableValue=getOmXmlQuestionTextValueAsString(question);
		}
		else if (OM_XML_SUBMIT_BUTTON_VARIABLE.equals(variableName))
		{
			variableValue=getOmXmlSubmitButtonAsString(tagName,attributeName,question);
		}
		else if (OM_XML_PASS_BUTTON_VARIABLE.equals(variableName))
		{
			variableValue=getOmXmlPassButtonAsString(tagName,attributeName,question);
		}
		else if (OM_XML_CORRECT_VARIABLE.equals(variableName) || 
			OM_XML_CORRECT_GAP_VARIABLE.equals(variableName))
		{
			variableValue=getOmXmlCorrectValueAsString(question);
		}
		else if (OM_XML_CORRECT_IMAGE_VARIABLE.equals(variableName) ||
			OM_XML_CORRECT_IMAGE_GAP_VARIABLE.equals(variableName))
		{
			variableValue=getOmXmlCorrectImageValueAsString(tagName,attributeName,question);
		}
		else if (OM_XML_INCORRECT_VARIABLE.equals(variableName) || 
			OM_XML_INCORRECT_GAP_VARIABLE.equals(variableName))
		{
			variableValue=getOmXmlIncorrectValueAsString(question);
		}
		else if (OM_XML_INCORRECT_IMAGE_VARIABLE.equals(variableName) || 
			OM_XML_INCORRECT_IMAGE_GAP_VARIABLE.equals(variableName))
		{
			variableValue=getOmXmlIncorrectImageValueAsString(tagName,attributeName,question);
		}
		else if (OM_XML_STILL_VARIABLE.equals(variableName) || OM_XML_STILL_GAP_VARIABLE.equals(variableName))
		{
			variableValue=getOmXmlStillValueAsString(question);
		}
		else if (OM_XML_PASS_VARIABLE.equals(variableName) || OM_XML_PASS_GAP_VARIABLE.equals(variableName))
		{
			variableValue=getOmXmlPassValueAsString(question);
		}
		else if (OM_XML_PASS_IMAGE_VARIABLE.equals(variableName) ||
			OM_XML_PASS_IMAGE_GAP_VARIABLE.equals(variableName))
		{
			variableValue=getOmXmlPassImageValueAsString(tagName,attributeName,question);
		}
		else if (OM_XML_FINAL_VARIABLE.equals(variableName) || OM_XML_FINAL_GAP_VARIABLE.equals(variableName))
		{
			variableValue=getOmXmlFinalValueAsString(question);
		}
		else if (OM_XML_FINAL_IMAGE_VARIABLE.equals(variableName) ||
			OM_XML_FINAL_IMAGE_GAP_VARIABLE.equals(variableName))
		{
			variableValue=getOmXmlFinalImageValueAsString(tagName,attributeName,question);
		}
		else if (OM_XML_TRY_AGAIN_BUTTON.equals(variableName))
		{
			variableValue=getOmXmlTryAgainButtonAsString(tagName,attributeName,question);
		}
		else if (OM_XML_NEXT_QUESTION_BUTTON.equals(variableName))
		{
			variableValue=getOmXmlNextQuestionButtonAsString(tagName,attributeName,question);
		}
		else
		{
			String resourceVariableName=null;
			if (variableName.length()>OM_XML_RESOURCE_GAP_VARIABLE.length() &&
				variableName.startsWith(OM_XML_RESOURCE_GAP_VARIABLE))
			{
				resourceVariableName=OM_XML_RESOURCE_GAP_VARIABLE;
			}
			else if (variableName.length()>OM_XML_RESOURCE_VARIABLE.length() && 
				variableName.startsWith(OM_XML_RESOURCE_VARIABLE))
			{
				resourceVariableName=OM_XML_RESOURCE_VARIABLE;
			}
			if (resourceVariableName!=null)
			{
				if (variableName.charAt(resourceVariableName.length())=='_')
				{
					if (variableName.length()>resourceVariableName.length()+1)
					{
						variableValue=getOmXmlResourceValueAsString(
							tagName,attributeName,variableName.substring(resourceVariableName.length()+1),question);
					}
				}
				else
				{
					try
					{
						int position=Integer.parseInt(variableName.substring(resourceVariableName.length()));
						if (position>0)
						{
							variableValue=getOmXmlResourceValueAsString(tagName,attributeName,position,question);
						}
					}
					catch (NumberFormatException nfe)
					{
					}
				}
			}
		}
		return variableValue.replaceAll(Pattern.quote("\r"),"").replaceAll(Pattern.quote("\n"),"");
	}
	
	private static String getOmXmlGenericQuestionValueAsString(OmXmlQuestion question)
	{
		return getFullClassName(question.getPackage());
	}
	
	private static String getOmXmlQuestionImageValueAsString(String tagName,String attributeName,
		OmXmlQuestion question)
	{
		String questionImageValue=null;
		Resource resource=question.getResource();
		if (resource!=null)
		{
			if ("image".equals(tagName))
			{
				if ("filePath".equals(attributeName))
				{
					questionImageValue=getOnlyFileName(resource.getFileName());
				}
				else if ("width".equals(attributeName))
				{
					questionImageValue=Integer.toString(question.getResourceWidth());
				}
				else if ("height".equals(attributeName))
				{
					questionImageValue=Integer.toString(question.getResourceHeight());
				}
				else if ("alt".equals(attributeName))
				{
					questionImageValue=resource.getDescription();
				}
			}
			else
			{
				questionImageValue=resource.getName();
			}
		}
		return questionImageValue==null?"":questionImageValue;
	}
	
	private static String getOmXmlQuestionTextValueAsString(OmXmlQuestion question)
	{
		String questionText=question.getQuestionText();
		if (question.isDisplayEquations())
		{
			questionText=replaceEquationsAsString(questionText);
		}
		return questionText==null?"":questionText;
	}
	
	private static String getOmXmlSubmitButtonAsString(String tagName,String attributeName,OmXmlQuestion question)
	{
		String submitButtonValue=null;
		if ("button".equals(tagName))
		{
			if ("action".equals(attributeName))
			{
				submitButtonValue="actionSubmit";
			}
			else if ("label".equals(attributeName))
			{
				submitButtonValue=getLocalizedString("OM_ENTER_ANSWER","%%lENTERANSWER%%");
			}
			else if ("display".equals(attributeName))
			{
				submitButtonValue="yes";
			}
		}
		return submitButtonValue==null?"":submitButtonValue;
	}
	
	private static String getOmXmlPassButtonAsString(String tagName,String attributeName,OmXmlQuestion question)
	{
		String passButtonValue=null;
		if ("button".equals(tagName))
		{
			if ("action".equals(attributeName))
			{
				passButtonValue="actionGiveUp";
			}
			else if ("label".equals(attributeName))
			{
				passButtonValue=getLocalizedString("OM_GIVE_UP","%%lGIVEUP%%");
			}
			else if ("display".equals(attributeName))
			{
				passButtonValue="yes";
			}
		}
		return passButtonValue==null?"":passButtonValue;
	}
	
	private static String getOmXmlCorrectValueAsString(OmXmlQuestion question)
	{
		String correct=question.getCorrectFeedback();
		if (question.isDisplayEquations())
		{
			correct=replaceEquationsAsString(correct);
		}
		return correct==null?"":correct;
	}
	
	private static String getOmXmlCorrectImageValueAsString(String tagName,String attributeName,
		OmXmlQuestion question)
	{
		String questionImageValue=null;
		Resource resource=question.getCorrectFeedbackResource();
		if (resource!=null)
		{
			if ("image".equals(tagName))
			{
				if ("filePath".equals(attributeName))
				{
					questionImageValue=getOnlyFileName(resource.getFileName());
				}
				else if ("width".equals(attributeName))
				{
					questionImageValue=Integer.toString(question.getCorrectFeedbackResourceWidth());
				}
				else if ("height".equals(attributeName))
				{
					questionImageValue=Integer.toString(question.getCorrectFeedbackResourceHeight());
				}
				else if ("alt".equals(attributeName))
				{
					questionImageValue=resource.getDescription();
				}
			}
			else
			{
				questionImageValue=resource.getName();
			}
		}
		return questionImageValue==null?"":questionImageValue;
	}
	
	private static String getOmXmlIncorrectValueAsString(OmXmlQuestion question)
	{
		String incorrect=question.getIncorrectFeedback();
		if (question.isDisplayEquations())
		{
			incorrect=replaceEquationsAsString(incorrect);
		}
		return incorrect==null?"":incorrect;
	}
	
	private static String getOmXmlIncorrectImageValueAsString(String tagName,String attributeName,
		OmXmlQuestion question)
	{
		String questionImageValue=null;
		Resource resource=question.getIncorrectFeedbackResource();
		if (resource!=null)
		{
			if ("image".equals(tagName))
			{
				if ("filePath".equals(attributeName))
				{
					questionImageValue=getOnlyFileName(resource.getFileName());
				}
				else if ("width".equals(attributeName))
				{
					questionImageValue=Integer.toString(question.getIncorrectFeedbackResourceWidth());
				}
				else if ("height".equals(attributeName))
				{
					questionImageValue=Integer.toString(question.getIncorrectFeedbackResourceHeight());
				}
				else if ("alt".equals(attributeName))
				{
					questionImageValue=resource.getDescription();
				}
			}
			else
			{
				questionImageValue=resource.getName();
			}
		}
		return questionImageValue==null?"":questionImageValue;
	}
	
	private static String getOmXmlStillValueAsString(OmXmlQuestion question)
	{
		String still=question.getStillFeedback();
		if (question.isDisplayEquations())
		{
			still=replaceEquationsAsString(still);
		}
		return still==null?"":still;
	}
	
	private static String getOmXmlPassValueAsString(OmXmlQuestion question)
	{
		String pass=question.getPassFeedback();
		if (question.isDisplayEquations())
		{
			pass=replaceEquationsAsString(pass);
		}
		return pass==null?"":pass;
	}
	
	private static String getOmXmlPassImageValueAsString(String tagName,String attributeName,
		OmXmlQuestion question)
	{
		String questionImageValue=null;
		Resource resource=question.getPassFeedbackResource();
		if (resource!=null)
		{
			if ("image".equals(tagName))
			{
				if ("filePath".equals(attributeName))
				{
					questionImageValue=getOnlyFileName(resource.getFileName());
				}
				else if ("width".equals(attributeName))
				{
					questionImageValue=Integer.toString(question.getPassFeedbackResourceWidth());
				}
				else if ("height".equals(attributeName))
				{
					questionImageValue=Integer.toString(question.getPassFeedbackResourceHeight());
				}
				else if ("alt".equals(attributeName))
				{
					questionImageValue=resource.getDescription();
				}
			}
			else
			{
				questionImageValue=resource.getName();
			}
		}
		return questionImageValue==null?"":questionImageValue;
	}
	
	private static String getOmXmlFinalValueAsString(OmXmlQuestion question)
	{
		String finalV=question.getAnswerFeedback();
		if (question.isDisplayEquations())
		{
			finalV=replaceEquationsAsString(finalV);
		}
		return finalV==null?"":finalV;
	}
	
	private static String getOmXmlFinalImageValueAsString(String tagName,String attributeName,
		OmXmlQuestion question)
	{
		String questionImageValue=null;
		Resource resource=question.getFinalFeedbackResource();
		if (resource!=null)
		{
			if ("image".equals(tagName))
			{
				if ("filePath".equals(attributeName))
				{
					questionImageValue=getOnlyFileName(resource.getFileName());
				}
				else if ("width".equals(attributeName))
				{
					questionImageValue=Integer.toString(question.getFinalFeedbackResourceWidth());
				}
				else if ("height".equals(attributeName))
				{
					questionImageValue=Integer.toString(question.getFinalFeedbackResourceHeight());
				}
				else if ("alt".equals(attributeName))
				{
					questionImageValue=resource.getDescription();
				}
			}
			else
			{
				questionImageValue=resource.getName();
			}
		}
		return questionImageValue==null?"":questionImageValue;
	}
	
	private static String getOmXmlResourceValueAsString(String tagName,String attributeName,int position,
		OmXmlQuestion question)
	{
		String resourceValue=null;
		QuestionResource questionResource=null;
		Resource resource=null;
		for (QuestionResource qr:question.getQuestionResources())
		{
			if (qr.getPosition()==position)
			{
				questionResource=qr;
				resource=qr.getResource();
				break;
			}
		}
		if (resource!=null)
		{
			if ("image".equals(tagName))
			{
				if ("filePath".equals(attributeName))
				{
					resourceValue=getOnlyFileName(resource.getFileName());
				}
				else if ("width".equals(attributeName))
				{
					resourceValue=Integer.toString(questionResource.getWidth());
				}
				else if ("height".equals(attributeName))
				{
					resourceValue=Integer.toString(questionResource.getHeight());
				}
				else if ("alt".equals(attributeName))
				{
					resourceValue=resource.getDescription();
				}
			}
			else
			{
				resourceValue=resource.getName();
			}
		}
		return resourceValue==null?"":resourceValue;
	}
	
	private static String getOmXmlResourceValueAsString(String tagName,String attributeName,String name,
		OmXmlQuestion question)
	{
		String resourceValue=null;
		QuestionResource questionResource=null;
		Resource resource=null;
		for (QuestionResource qr:question.getQuestionResources())
		{
			if (name.equals(qr.getName()))
			{
				questionResource=qr;
				resource=qr.getResource();
				break;
			}
		}
		if (resource!=null)
		{
			if ("image".equals(tagName))
			{
				if ("filePath".equals(attributeName))
				{
					resourceValue=getOnlyFileName(resource.getFileName());
				}
				else if ("width".equals(attributeName))
				{
					resourceValue=Integer.toString(questionResource.getWidth());
				}
				else if ("height".equals(attributeName))
				{
					resourceValue=Integer.toString(questionResource.getHeight());
				}
				else if ("alt".equals(attributeName))
				{
					resourceValue=resource.getDescription();
				}
			}
			else
			{
				resourceValue=resource.getName();
			}
		}
		return resourceValue==null?"":resourceValue;
	}
	
	private static String getOmXmlTryAgainButtonAsString(String tagName,String attributeName,OmXmlQuestion question)
	{
		String tryAgainButtonValue=null;
		if ("button".equals(tagName))
		{
			if ("id".equals(attributeName))
			{
				tryAgainButtonValue="ok";
			}
			else if ("action".equals(attributeName))
			{
				tryAgainButtonValue="actionOK";
			}
			else if ("label".equals(attributeName))
			{
				tryAgainButtonValue=getLocalizedString("OM_TRY_AGAIN","%%lTRYAGAIN%%");
			}
			else if ("display".equals(attributeName))
			{
				tryAgainButtonValue="yes";
			}
		}
		return tryAgainButtonValue==null?"":tryAgainButtonValue;
	}
	
	private static String getOmXmlNextQuestionButtonAsString(String tagName,String attributeName,
		OmXmlQuestion question)
	{
		String nextQuestionButtonValue=null;
		if ("button".equals(tagName))
		{
			if ("id".equals(attributeName))
			{
				nextQuestionButtonValue="next";
			}
			if ("action".equals(attributeName))
			{
				nextQuestionButtonValue="actionOK";
			}
			else if ("label".equals(attributeName))
			{
				nextQuestionButtonValue=getLocalizedString("OM_NEXT_QUESTION","%%lNEXTQUESTION%%");
			}
			else if ("display".equals(attributeName))
			{
				nextQuestionButtonValue="no";
			}
		}
		return nextQuestionButtonValue==null?"":nextQuestionButtonValue;
	}
	
	private static String replaceEquationsAsString(String text)
	{
		StringBuffer newText=new StringBuffer();
		if (text!=null && !text.equals(""))
		{
			// First we normalize new line characters
			text=text.replaceAll(Pattern.quote("\r\n"),"\n").replaceAll(Pattern.quote("\r"),"\n");
			
			// We get lines splitting by new line character
			String[] lines=text.split("\n");
			for (int i=0;i<lines.length;i++)
			{
				String[] textBlocks=lines[i].split(Pattern.quote("$$"),-1);
				for (int j=0;j<textBlocks.length;j++)
				{
					if (j%2==0)
					{
						// Add as text
						newText.append(textBlocks[j]);
					}
					else if (j==textBlocks.length-1)
					{
						// Add as text (starting with $$)
						newText.append("$$");
						newText.append(textBlocks[j]);
					}
					else
					{
						String[] equationAndAlt=textBlocks[j].split(Pattern.quote("$"));
						if (equationAndAlt.length>2)
						{
							// Add as text (surrounded with $$)
							newText.append("$$");
							newText.append(textBlocks[j]);
							newText.append("$$");
						}
						else if (equationAndAlt.length==2)
						{
							// Add alt as text
							newText.append(equationAndAlt[1]);
						}
						else
						{
							// Add as text
							newText.append(textBlocks[j]);
						}
					}
				}
				if (i<lines.length-1)
				{
					newText.append('\n');
				}
			}
		}
		return newText.toString();
	}
	
	private static List<Node> replaceOmXmlVariablesAsXMLNodes(Document doc,String text,OmXmlQuestion question)
	{
		List<Node> replacedNodes=new ArrayList<Node>();
		int i=0;
		while (i<text.length())
		{
			int iStartVar=text.indexOf("${",i);
			if (iStartVar!=-1)
			{
				if (iStartVar>i)
				{
					replacedNodes.add(doc.createTextNode(text.substring(i,iStartVar)));
					i=iStartVar;
				}
				int iEndVar=text.indexOf('}',iStartVar);
				if (iEndVar!=-1)
				{
					for (Node node:getOmXmlVariableValueAsXMLNodes(
						doc,text.substring(iStartVar+"${".length(),iEndVar),question))
					{
						replacedNodes.add(node);
					}
					i=iEndVar+1;
				}
				else
				{
					replacedNodes.add(doc.createTextNode(text.substring(iStartVar)));
					i=text.length();
				}
			}
			else
			{
				replacedNodes.add(doc.createTextNode(text.substring(i)));
				i=text.length();
			}
		}
		return replacedNodes;
	}
	
	private static List<Node> getOmXmlVariableValueAsXMLNodes(Document doc,String variableName,
		OmXmlQuestion question)
	{
		List<Node> variableValueNodes=null;
		if (OM_XML_GENERIC_QUESTION_VARIABLE.equals(variableName))
		{
			variableValueNodes=getOmXmlGenericQuestionValueAsXMLNodes(doc,question);
		}
		else if (OM_XML_QUESTION_IMAGE_VARIABLE.equals(variableName))
		{
			variableValueNodes=getOmXmlQuestionImageValueAsXMLNodes(doc,question);
		}
		else if (OM_XML_QUESTION_IMAGE_GAP_VARIABLE.equals(variableName))
		{
			variableValueNodes=getOmXmlQuestionImageValueAsXMLNodes(doc,question);
			if (!checkEmptyXMLNodes(variableValueNodes))
			{
				variableValueNodes.add(doc.createElement("gap"));
			}
		}
		else if (OM_XML_QUESTION_TEXT_VARIABLE.equals(variableName))
		{
			variableValueNodes=getOmXmlQuestionTextValueAsXMLNodes(doc,question);
		}
		else if (OM_XML_QUESTION_TEXT_GAP_VARIABLE.equals(variableName))
		{
			variableValueNodes=getOmXmlQuestionTextValueAsXMLNodes(doc,question);
			if (!checkEmptyXMLNodes(variableValueNodes))
			{
				variableValueNodes.add(doc.createElement("gap"));
			}
		}
		else if (OM_XML_SUBMIT_BUTTON_VARIABLE.equals(variableName))
		{
			variableValueNodes=getOmXmlSubmitButtonAsXMLNodes(doc,question);
		}
		else if (OM_XML_PASS_BUTTON_VARIABLE.equals(variableName))
		{
			variableValueNodes=getOmXmlPassButtonAsXMLNodes(doc,question);
		}
		else if (OM_XML_CORRECT_VARIABLE.equals(variableName))
		{
			variableValueNodes=getOmXmlCorrectValueAsXMLNodes(doc,question);
		}
		else if (OM_XML_CORRECT_GAP_VARIABLE.equals(variableName))
		{
			variableValueNodes=getOmXmlCorrectValueAsXMLNodes(doc,question);
			if (!checkEmptyXMLNodes(variableValueNodes))
			{
				variableValueNodes.add(doc.createElement("gap"));
			}
		}
		else if (OM_XML_CORRECT_IMAGE_VARIABLE.equals(variableName))
		{
			variableValueNodes=getOmXmlCorrectImageValueAsXMLNodes(doc,question);
		}
		else if (OM_XML_CORRECT_IMAGE_GAP_VARIABLE.equals(variableName))
		{
			variableValueNodes=getOmXmlCorrectImageValueAsXMLNodes(doc,question);
			if (!checkEmptyXMLNodes(variableValueNodes))
			{
				variableValueNodes.add(doc.createElement("gap"));
			}
		}
		else if (OM_XML_INCORRECT_VARIABLE.equals(variableName))
		{
			variableValueNodes=getOmXmlIncorrectValueAsXMLNodes(doc,question);
		}
		else if (OM_XML_INCORRECT_GAP_VARIABLE.equals(variableName))
		{
			variableValueNodes=getOmXmlIncorrectValueAsXMLNodes(doc,question);
			if (!checkEmptyXMLNodes(variableValueNodes))
			{
				variableValueNodes.add(doc.createElement("gap"));
			}
		}
		else if (OM_XML_INCORRECT_IMAGE_VARIABLE.equals(variableName))
		{
			variableValueNodes=getOmXmlIncorrectImageValueAsXMLNodes(doc,question);
		}
		else if (OM_XML_INCORRECT_IMAGE_GAP_VARIABLE.equals(variableName))
		{
			variableValueNodes=getOmXmlIncorrectImageValueAsXMLNodes(doc,question);
			if (!checkEmptyXMLNodes(variableValueNodes))
			{
				variableValueNodes.add(doc.createElement("gap"));
			}
		}
		else if (OM_XML_STILL_VARIABLE.equals(variableName))
		{
			variableValueNodes=getOmXmlStillValueAsXMLNodes(doc,question);
		}
		else if (OM_XML_STILL_GAP_VARIABLE.equals(variableName))
		{
			variableValueNodes=getOmXmlStillValueAsXMLNodes(doc,question);
			if (!checkEmptyXMLNodes(variableValueNodes))
			{
				variableValueNodes.add(doc.createElement("gap"));
			}
		}
		else if (OM_XML_PASS_VARIABLE.equals(variableName))
		{
			variableValueNodes=getOmXmlPassValueAsXMLNodes(doc,question);
		}
		else if (OM_XML_PASS_GAP_VARIABLE.equals(variableName))
		{
			variableValueNodes=getOmXmlPassValueAsXMLNodes(doc,question);
			if (!checkEmptyXMLNodes(variableValueNodes))
			{
				variableValueNodes.add(doc.createElement("gap"));
			}
		}
		else if (OM_XML_PASS_IMAGE_VARIABLE.equals(variableName))
		{
			variableValueNodes=getOmXmlPassImageValueAsXMLNodes(doc,question);
		}
		else if (OM_XML_PASS_IMAGE_GAP_VARIABLE.equals(variableName))
		{
			variableValueNodes=getOmXmlPassImageValueAsXMLNodes(doc,question);
			if (!checkEmptyXMLNodes(variableValueNodes))
			{
				variableValueNodes.add(doc.createElement("gap"));
			}
		}
		else if (OM_XML_FINAL_VARIABLE.equals(variableName))
		{
			variableValueNodes=getOmXmlFinalValueAsXMLNodes(doc,question);
		}
		else if (OM_XML_FINAL_GAP_VARIABLE.equals(variableName))
		{
			variableValueNodes=getOmXmlFinalValueAsXMLNodes(doc,question);
			if (!checkEmptyXMLNodes(variableValueNodes))
			{
				variableValueNodes.add(doc.createElement("gap"));
			}
		}
		else if (OM_XML_FINAL_IMAGE_VARIABLE.equals(variableName))
		{
			variableValueNodes=getOmXmlFinalImageValueAsXMLNodes(doc,question);
		}
		else if (OM_XML_FINAL_IMAGE_GAP_VARIABLE.equals(variableName))
		{
			variableValueNodes=getOmXmlFinalImageValueAsXMLNodes(doc,question);
			if (!checkEmptyXMLNodes(variableValueNodes))
			{
				variableValueNodes.add(doc.createElement("gap"));
			}
		}
		else if (OM_XML_TRY_AGAIN_BUTTON.equals(variableName))
		{
			variableValueNodes=getOmXmlTryAgainButtonAsXMLNodes(doc,question);
		}
		else if (OM_XML_NEXT_QUESTION_BUTTON.equals(variableName))
		{
			variableValueNodes=getOmXmlNextQuestionButtonAsXMLNodes(doc,question);
		}
		else
		{
			String resourceVariableName=null;
			if (variableName.length()>OM_XML_RESOURCE_GAP_VARIABLE.length() && 
				variableName.startsWith(OM_XML_RESOURCE_GAP_VARIABLE))
			{
				resourceVariableName=OM_XML_RESOURCE_GAP_VARIABLE;
			}
			else if (variableName.length()>OM_XML_RESOURCE_VARIABLE.length() && 
				variableName.startsWith(OM_XML_RESOURCE_VARIABLE))
			{
				resourceVariableName=OM_XML_RESOURCE_VARIABLE;
			}
			if (resourceVariableName!=null)
			{
				if (variableName.charAt(resourceVariableName.length())=='_')
				{
					if (variableName.length()>resourceVariableName.length()+1)
					{
						variableValueNodes=getOmXmlResourceValueAsXMLNodes(
							doc,variableName.substring(resourceVariableName.length()+1),question);
					}
				}
				else
				{
					try
					{
						int position=Integer.parseInt(variableName.substring(resourceVariableName.length()));
						if (position>0)
						{
							variableValueNodes=getOmXmlResourceValueAsXMLNodes(doc,position,question);
						}
					}
					catch (NumberFormatException nfe)
					{
					}
				}
				if (OM_XML_RESOURCE_GAP_VARIABLE.equals(resourceVariableName) && 
					!checkEmptyXMLNodes(variableValueNodes))
				{
					variableValueNodes.add(doc.createElement("gap"));
				}
			}
		}
		return variableValueNodes==null?new ArrayList<Node>():variableValueNodes;
	}
	
	private static boolean checkEmptyXMLNodes(List<Node> xmlNodes)
	{
		boolean empty=xmlNodes==null || xmlNodes.isEmpty();
		if (!empty)
		{
			empty=true;
			for (Node xmlNode:xmlNodes)
			{
				if (xmlNode.getNodeType()==Node.TEXT_NODE)
				{
					if (!"".equals(xmlNode.getNodeValue().trim()))
					{
						empty=false;
						break;
					}
				}
				else
				{
					empty=false;
					break;
				}
			}
		}
		return empty;
	}
	
	private static List<Node> getOmXmlGenericQuestionValueAsXMLNodes(Document doc,OmXmlQuestion question)
	{
		List<Node> genericQuestionValueNodes=new ArrayList<Node>();
		genericQuestionValueNodes.add(doc.createTextNode(getFullClassName(question.getPackage())));
		return genericQuestionValueNodes;
	}
	
	private static List<Node> getOmXmlQuestionImageValueAsXMLNodes(Document doc,OmXmlQuestion question)
	{
		List<Node> questionImageValueNodes=new ArrayList<Node>();
		Resource resource=question.getResource();
		if (resource!=null)
		{
			Element image=doc.createElement("image");
			image.setAttribute("alt","");
			image.setAttribute("filePath",getOnlyFileName(resource.getFileName()));
			image.setAttribute("width",Integer.toString(question.getResourceWidth()));
			image.setAttribute("height",Integer.toString(question.getResourceHeight()));
			questionImageValueNodes.add(image);
		}
		return questionImageValueNodes;
	}
	
	private static List<Node> getOmXmlQuestionTextValueAsXMLNodes(Document doc,OmXmlQuestion question)
	{
		return createTextWithBreaks(doc,question.getQuestionText(),question.isDisplayEquations());
	}
	
	private static List<Node> getOmXmlSubmitButtonAsXMLNodes(Document doc,OmXmlQuestion question)
	{
		List<Node> submitButtonValueNodes=new ArrayList<Node>();
		Element submitButton=doc.createElement("button");
		submitButton.setAttribute("action","actionSubmit");
		submitButton.setAttribute("label",getLocalizedString("OM_ENTER_ANSWER","%%lENTERANSWER%%"));
		submitButtonValueNodes.add(submitButton);
		return submitButtonValueNodes;
	}
	
	private static List<Node> getOmXmlPassButtonAsXMLNodes(Document doc,OmXmlQuestion question)
	{
		List<Node> passButtonValueNodes=new ArrayList<Node>();
		Element passButton=doc.createElement("button");
		passButton.setAttribute("action","actionGiveUp");
		passButton.setAttribute("label",getLocalizedString("OM_GIVE_UP","%%lGIVEUP%%"));
		passButtonValueNodes.add(passButton);
		return passButtonValueNodes;
	}
	
	private static List<Node> getOmXmlCorrectValueAsXMLNodes(Document doc,OmXmlQuestion question)
	{
		return createTextWithBreaks(doc,question.getCorrectFeedback(),question.isDisplayEquations());
	}
	
	private static List<Node> getOmXmlCorrectImageValueAsXMLNodes(Document doc,OmXmlQuestion question)
	{
		List<Node> correctImageValueNodes=new ArrayList<Node>();
		Resource resource=question.getCorrectFeedbackResource();
		if (resource!=null)
		{
			Element image=doc.createElement("image");
			image.setAttribute("alt","");
			image.setAttribute("filePath",getOnlyFileName(resource.getFileName()));
			image.setAttribute("width",Integer.toString(question.getCorrectFeedbackResourceWidth()));
			image.setAttribute("height",Integer.toString(question.getCorrectFeedbackResourceHeight()));
			correctImageValueNodes.add(image);
		}
		return correctImageValueNodes;
	}
	
	private static List<Node> getOmXmlIncorrectValueAsXMLNodes(Document doc,OmXmlQuestion question)
	{
		return createTextWithBreaks(doc,question.getIncorrectFeedback(),question.isDisplayEquations());
	}
	
	private static List<Node> getOmXmlIncorrectImageValueAsXMLNodes(Document doc,OmXmlQuestion question)
	{
		List<Node> incorrectImageValueNodes=new ArrayList<Node>();
		Resource resource=question.getIncorrectFeedbackResource();
		if (resource!=null)
		{
			Element image=doc.createElement("image");
			image.setAttribute("alt","");
			image.setAttribute("filePath",getOnlyFileName(resource.getFileName()));
			image.setAttribute("width",Integer.toString(question.getIncorrectFeedbackResourceWidth()));
			image.setAttribute("height",Integer.toString(question.getIncorrectFeedbackResourceHeight()));
			incorrectImageValueNodes.add(image);
		}
		return incorrectImageValueNodes;
	}
	
	private static List<Node> getOmXmlStillValueAsXMLNodes(Document doc,OmXmlQuestion question)
	{
		return createTextWithBreaks(doc,question.getStillFeedback(),question.isDisplayEquations());
	}
	
	private static List<Node> getOmXmlPassValueAsXMLNodes(Document doc,OmXmlQuestion question)
	{
		return createTextWithBreaks(doc,question.getPassFeedback(),question.isDisplayEquations());
	}
	
	private static List<Node> getOmXmlPassImageValueAsXMLNodes(Document doc,OmXmlQuestion question)
	{
		List<Node> passImageValueNodes=new ArrayList<Node>();
		Resource resource=question.getPassFeedbackResource();
		if (resource!=null)
		{
			Element image=doc.createElement("image");
			image.setAttribute("alt","");
			image.setAttribute("filePath",getOnlyFileName(resource.getFileName()));
			image.setAttribute("width",Integer.toString(question.getPassFeedbackResourceWidth()));
			image.setAttribute("height",Integer.toString(question.getPassFeedbackResourceHeight()));
			passImageValueNodes.add(image);
		}
		return passImageValueNodes;
	}
	
	private static List<Node> getOmXmlFinalValueAsXMLNodes(Document doc,OmXmlQuestion question)
	{
		return createTextWithBreaks(doc,question.getAnswerFeedback(),question.isDisplayEquations());
	}
	
	private static List<Node> getOmXmlFinalImageValueAsXMLNodes(Document doc,OmXmlQuestion question)
	{
		List<Node> finalImageValueNodes=new ArrayList<Node>();
		Resource resource=question.getFinalFeedbackResource();
		if (resource!=null)
		{
			Element image=doc.createElement("image");
			image.setAttribute("alt","");
			image.setAttribute("filePath",getOnlyFileName(resource.getFileName()));
			image.setAttribute("width",Integer.toString(question.getFinalFeedbackResourceWidth()));
			image.setAttribute("height",Integer.toString(question.getFinalFeedbackResourceHeight()));
			finalImageValueNodes.add(image);
		}
		return finalImageValueNodes;
	}
	
	private static List<Node> getOmXmlResourceValueAsXMLNodes(Document doc,int position,OmXmlQuestion question)
	{
		List<Node> finalImageValueNodes=new ArrayList<Node>();
		QuestionResource questionResource=null;
		Resource resource=null;
		for (QuestionResource qr:question.getQuestionResources())
		{
			if (qr.getPosition()==position)
			{
				questionResource=qr;
				resource=qr.getResource();
				break;
			}
		}
		if (resource!=null)
		{
			Element image=doc.createElement("image");
			image.setAttribute("alt","");
			image.setAttribute("filePath",getOnlyFileName(resource.getFileName()));
			image.setAttribute("width",Integer.toString(questionResource.getWidth()));
			image.setAttribute("height",Integer.toString(questionResource.getHeight()));
			finalImageValueNodes.add(image);
		}
		return finalImageValueNodes;
	}
	
	private static List<Node> getOmXmlResourceValueAsXMLNodes(Document doc,String name,OmXmlQuestion question)
	{
		List<Node> finalImageValueNodes=new ArrayList<Node>();
		QuestionResource questionResource=null;
		Resource resource=null;
		for (QuestionResource qr:question.getQuestionResources())
		{
			if (name.equals(qr.getName()))
			{
				questionResource=qr;
				resource=qr.getResource();
				break;
			}
		}
		if (resource!=null)
		{
			Element image=doc.createElement("image");
			image.setAttribute("alt","");
			image.setAttribute("filePath",getOnlyFileName(resource.getFileName()));
			image.setAttribute("width",Integer.toString(questionResource.getWidth()));
			image.setAttribute("height",Integer.toString(questionResource.getHeight()));
			finalImageValueNodes.add(image);
		}
		return finalImageValueNodes;
	}
	
	private static List<Node> getOmXmlTryAgainButtonAsXMLNodes(Document doc,OmXmlQuestion question)
	{
		List<Node> tryAgainButtonValueNodes=new ArrayList<Node>();
		Element tryAgainButton=doc.createElement("button");
		tryAgainButton.setAttribute("id","ok");
		tryAgainButton.setAttribute("action","actionOK");
		tryAgainButton.setAttribute("label",getLocalizedString("OM_TRY_AGAIN","%%lTRYAGAIN%%"));
		tryAgainButtonValueNodes.add(tryAgainButton);
		return tryAgainButtonValueNodes;
	}
	
	private static List<Node> getOmXmlNextQuestionButtonAsXMLNodes(Document doc,OmXmlQuestion question)
	{
		List<Node> nextQuestionButtonValueNodes=new ArrayList<Node>();
		Element nextQuestionButton=doc.createElement("button");
		nextQuestionButton.setAttribute("id","next");
		nextQuestionButton.setAttribute("action","actionOK");
		nextQuestionButton.setAttribute("label",getLocalizedString("OM_NEXT_QUESTION","%%lNEXTQUESTION%%"));
		nextQuestionButton.setAttribute("display","no");
		nextQuestionButtonValueNodes.add(nextQuestionButton);
		return nextQuestionButtonValueNodes;
	}
	
	/**
	 * Create XML node with selectable answers for a "TRUE_FALSE" question 
	 * @param doc XML document
	 * @param question "TRUE_FALSE" Question
	 * @return XML node with selectable answers for a "TRUE_FALSE" question
	 */
	private static Element createTrueFalseSelectableAnswers(Document doc,TrueFalseQuestion question)
	{
		Element layoutGrid=doc.createElement("layoutgrid");
		layoutGrid.setAttribute("cols","1");
		
		// Add "true" answer
		Element trueRadiobox=doc.createElement("radiobox");
		trueRadiobox.setAttribute("id","true");
		if (question.getCorrectAnswer())
		{
			trueRadiobox.setAttribute("right","yes");
		}
		layoutGrid.appendChild(trueRadiobox);
		Element trueTextComponent=doc.createElement("t");
		trueRadiobox.appendChild(trueTextComponent);
		String trueStr=question.getTrueText();
		if (trueStr==null)
		{
			trueStr=getLocalizedString("TRUE","True");
		}
		Text trueText=doc.createTextNode(trueStr);
		trueTextComponent.appendChild(trueText);
		
		// Add "false" answer
		Element falseRadiobox=doc.createElement("radiobox");
		falseRadiobox.setAttribute("id","false");
		if (!question.getCorrectAnswer())
		{
			falseRadiobox.setAttribute("right","yes");
		}
		layoutGrid.appendChild(falseRadiobox);
		Element falseTextComponent=doc.createElement("t");
		falseRadiobox.appendChild(falseTextComponent);
		String falseStr=question.getFalseText();
		if (falseStr==null)
		{
			falseStr=getLocalizedString("FALSE","False");
		}
		Text falseText=doc.createTextNode(falseStr);
		falseTextComponent.appendChild(falseText);
		
		return layoutGrid;
	}
	
	/**
	 * Create XML node with selectable answers for a "MULTICHOICE" question 
	 * @param doc XML document
	 * @param question "MULTICHOICE" Question
	 * @return XML node with selectable answers for a "MULTICHOICE" question
	 */
	private static Element createMultichoiceSelectableAnswers(Document doc,MultichoiceQuestion question)
	{
		Element layoutGrid=null;
		
		if (question.getAnswers()!=null)
		{
			// Get a list of answers sorted by position
			List<Answer> answers=new ArrayList<Answer>();
			for (Answer answer:question.getAnswers())
			{
				if (answer!=null)
				{
					answers.add(answer);
				}
			}
			Collections.sort(answers,getAnswerComparatorByPosition());
			
			// Create a layoutgrid component
			layoutGrid=doc.createElement("layoutgrid");
			layoutGrid.setAttribute("cols","1");
			if (question.getShuffle())
			{
				layoutGrid.setAttribute("shuffle","yes");
			}
			
			// Add answers
			StringBuffer fixedPositions=null;
			for (Answer answer:answers)
			{
				// Add answer
				Element answerBox=null;
				if (question.isSingle())
				{
					answerBox=doc.createElement("radiobox");
				}
				else
				{
					answerBox=doc.createElement("checkbox");
				}
				answerBox.setAttribute("id",answer.getOmId());
				if (answer.getCorrect())
				{
					answerBox.setAttribute("right","yes");
				}
				layoutGrid.appendChild(answerBox);
				
				if (answer.getText()==null || answer.getText().equals(""))
				{
					// If there is no text for answer use answerIndex+1 as answerline for summary
					answerBox.setAttribute("answerline",Integer.toString(answer.getPosition()));
				}
				else // Add text for answer (if exists)
				{
					Element answerTextComponent=doc.createElement("t");
					answerBox.appendChild(answerTextComponent);
					for (Node n:createTextWithBreaks(doc,answer.getText(),question.isDisplayEquations()))
					{
						answerTextComponent.appendChild(n);
					}
				}
				
				// Add resource for answer (if exists)
				if (answer.getResource()!=null)
				{
					// Test resource type (actually only images are supported)
					if (isImage(answer.getResource()))
					{
						Element image=doc.createElement("image");
						image.setAttribute("filePath",getOnlyFileName(answer.getResource().getFileName()));
						image.setAttribute("alt","");
						if (answer.getResourceWidth()>0 && answer.getResourceHeight()>0)
						{
							image.setAttribute("width",Integer.toString(answer.getResourceWidth()));
							image.setAttribute("height",Integer.toString(answer.getResourceHeight()));
						}
						answerBox.appendChild(image);
					}
				}
				
				// Check if this is a fixed answer (only needed if shuffle="yes")
				if (question.getShuffle() && answer.getFixed())
				{
					if (fixedPositions==null)
					{
						fixedPositions=new StringBuffer();
						fixedPositions.append(Integer.toString(answer.getPosition()));
					}
					else
					{
						fixedPositions.append(',');
						fixedPositions.append(Integer.toString(answer.getPosition()));
					}
				}
			}
			
			// Add "fixedpositions" attribute if needed
			if (fixedPositions!=null)
			{
				layoutGrid.setAttribute("fixedpositions",fixedPositions.toString());
			}
		}
		return layoutGrid;
	}
	
	/**
	 * Create XML node with draggable items for a "DRAG_DROP" question 
	 * @param doc XML document
	 * @param question "DRAG_DROP" Question
	 * @return XML node with draggable items for a "DRAG_DROP" question
	 */
	private static Element createDragDropDraggableItems(Document doc,DragDropQuestion question)
	{
		Element layoutShuffle=null;
		//TODO de momento solo tenemos en cuenta el grupo 1, habra que cambiarlo mas adelante
		if (question.getDraggableItems(1)!=null)
		{
			// Get current user session Hibernate operation
			Operation operation=questionBean.getCurrentUserOperation(null);
			
			// Get a list of draggable items sorted by position
			List<DragDropAnswer> draggableItems=new ArrayList<DragDropAnswer>();
			//TODO de momento solo tenemos en cuenta el grupo 1, habra que cambiarlo mas adelante
			for (Answer draggableItem:question.getDraggableItemsSortedByPosition(1))
			{
				draggableItems.add((DragDropAnswer)draggableItem);
			}
			
			// Create a layoutshuffle component
			layoutShuffle=doc.createElement("layoutshuffle");
			if (question.isShuffleDrags())
			{
				layoutShuffle.setAttribute("randomgroup","draggableitems");
			}
			else
			{
				layoutShuffle.setAttribute("shuffle","no");
			}
			
			// Add draggable items
			StringBuffer fixedPositions=null;
			for (DragDropAnswer draggableItem:draggableItems)
			{
				// Add draggable item
				Element dragBox=doc.createElement("dragbox");
				dragBox.setAttribute("id",draggableItem.getOmId());
				if (question.isInfinite())
				{
					dragBox.setAttribute("infinite","yes");
				}
				layoutShuffle.appendChild(dragBox);
				
				if (draggableItem.getText()==null || draggableItem.getText().equals(""))
				{
					// If there is no text for draggable item use answerIndex+1 as answerline for summary
					String draggableItemName=getDraggableItemName(operation,draggableItem);
					if (draggableItemName!=null)
					{
						dragBox.setAttribute("answerline",draggableItemName);
					}
				}
				else 
				{
					// Add text for draggable item
					Element draggableItemTextComponent=doc.createElement("t");
					dragBox.appendChild(draggableItemTextComponent);
					for (Node n:createTextWithBreaks(doc,draggableItem.getText(),question.isDisplayEquations()))
					{
						draggableItemTextComponent.appendChild(n);
					}
					dragBox.setAttribute("answerline",draggableItem.getText());
				}
				
				// Add resource for draggable item (if exists)
				if (draggableItem.getResource()!=null)
				{
					// Test resource type (actually only images are supported)
					if (isImage(draggableItem.getResource()))
					{
						Element image=doc.createElement("image");
						image.setAttribute(
							"filePath",getOnlyFileName(draggableItem.getResource().getFileName()));
						if (draggableItem.getText()==null || draggableItem.getText().equals(""))
						{
							String draggableItemName=getDraggableItemName(operation,draggableItem);
							if (draggableItemName==null)
							{
								image.setAttribute("alt",draggableItem.getOmId());
							}
							else
							{
								image.setAttribute("alt",draggableItemName);
							}
						}
						else {
							image.setAttribute("alt","");
						}
						if (draggableItem.getResourceWidth()>0 && draggableItem.getResourceHeight()>0)
						{
							image.setAttribute("width",Integer.toString(draggableItem.getResourceWidth()));
							image.setAttribute("height",Integer.toString(draggableItem.getResourceHeight()));
						}
						dragBox.appendChild(image);
					}
				}
				
				// Check if this is a fixed draggable item (only needed if shuffleDrags="yes")
				if (question.isShuffleDrags() && draggableItem.getFixed())
				{
					if (fixedPositions==null)
					{
						fixedPositions=new StringBuffer();
						fixedPositions.append(Integer.toString(draggableItem.getPosition()));
					}
					else
					{
						fixedPositions.append(',');
						fixedPositions.append(Integer.toString(draggableItem.getPosition()));
					}
				}
			}
			
			// Add "fixedpositions" attribute if needed
			if (fixedPositions!=null)
			{
				layoutShuffle.setAttribute("fixedpositions",fixedPositions.toString());
			}
		}
		return layoutShuffle;
	}
	
	/**
	 * Create XML node with droppable answers for a "DRAG_DROP" question.<br/><br/>
	 * Note that a 'droppable answer' consist of a text and/or image and a &lt;dropbox&gt;
	 * @param doc XML document
	 * @param question "DRAG_DROP" question
	 * @return XML node with droppable answers for a "DRAG_DROP" question
	 */
	private static Element createDragDropDroppableAnswers(Document doc,DragDropQuestion question)
	{
		Element table=null;
		//TODO de momento solo tenemos en cuenta el grupo 1, habra que cambiarlo mas adelante
		if (question.getDroppableAnswers(1)!=null)
		{
			// Get current user session Hibernate operation
			Operation operation=questionBean.getCurrentUserOperation(null);
			
			// Get a list of answers sorted by position
			List<DragDropAnswer> answers=new ArrayList<DragDropAnswer>();
			//TODO de momento solo tenemos en cuenta el grupo 1, habra que cambiarlo mas adelante
			for (Answer answer:question.getDroppableAnswersSortedByPosition(1))
			{
				answers.add((DragDropAnswer)answer);
			}
			
			// Create a table component
			table=doc.createElement("table");
			int rows=answers.size();
			int cols=1;
			boolean answersWithText=false;
			boolean answersWithImage=false;
			for (DragDropAnswer answer:answers)
			{
				if (!answersWithText && answer.getText()!=null && !answer.getText().equals(""))
				{
					cols++;
					answersWithText=true;
				}
				if (!answersWithImage && answer.getResource()!=null && isImage(answer.getResource()))
				{
					cols++;
					answersWithImage=true;
				}
				if (answersWithText && answersWithImage)
				{
					break;
				}
			}
			table.setAttribute("rows",Integer.toString(rows));
			table.setAttribute("cols",Integer.toString(cols));
			table.setAttribute("border","no");
			table.setAttribute("backgroundenabled","no");
			if (question.isShuffleDrops())
			{
				table.setAttribute("shufflerows","yes");
				table.setAttribute("randomgrouprows","answers");
			}
			
			// We add answers
			StringBuffer fixedPositions=null;
			for (DragDropAnswer answer:answers)
			{
				// We add a new row
				Element row=doc.createElement("row");
				table.appendChild(row);
				
				// We add answer text or an empty column if needed
				if (answersWithText)
				{
					// Add text for answer (if exists)
					if (answer.getText()!=null && !answer.getText().equals(""))
					{
						// We emphasize answer's text
						Element answerTextComponent=doc.createElement("emphasis");
						row.appendChild(answerTextComponent);
						for (Node n:createTextWithBreaks(doc,answer.getText(),question.isDisplayEquations()))
						{
							answerTextComponent.appendChild(n);
						}
					}
					else // Add empty col if this answer has no text
					{
						Element emptyCol=doc.createElement("t");
						row.appendChild(emptyCol);
					}
				}
				
				// We add resource for answer or an empty column if needed (actually only images are supported)
				if (answersWithImage)
				{
					// Add resource for answer (actually only images are supported)
					if (answer.getResource()!=null && isImage(answer.getResource()))
					{
						Element image=doc.createElement("image");
						image.setAttribute("filePath",getOnlyFileName(answer.getResource().getFileName()));
						if (answer.getText()==null || answer.getText().equals(""))
						{
							String answerName=getDroppableAnswerName(operation,answer);
							if (answerName==null)
							{
								image.setAttribute("alt",answer.getOmId());
							}
							else
							{
								image.setAttribute("alt",answerName);
							}
						}
						else {
							image.setAttribute("alt","");
						}
						if (answer.getResourceWidth()>0 && answer.getResourceHeight()>0)
						{
							image.setAttribute("width",Integer.toString(answer.getResourceWidth()));
							image.setAttribute("height",Integer.toString(answer.getResourceHeight()));
						}
						row.appendChild(image);
					}
					else // Add empty col if this answer has no resource or if its resource is not an image
					{
						Element emptyCol=doc.createElement("t");
						row.appendChild(emptyCol);
					}
				}
				
				// Add dropbox for answer
				Element dropBox=doc.createElement("dropbox");
				dropBox.setAttribute("id",answer.getOmId());
				if (question.isForceBorders())
				{
					dropBox.setAttribute("forceborder","yes");
				}
				if (answer.getRightAnswer()!=null)
				{
					dropBox.setAttribute("right",answer.getRightAnswer().getOmId());
				}
				row.appendChild(dropBox);
				
				// Check if this is a fixed answer (only needed if shuffleDrops="yes")
				if (question.isShuffleDrops() && answer.getFixed())
				{
					if (fixedPositions==null)
					{
						fixedPositions=new StringBuffer();
						fixedPositions.append(Integer.toString(answer.getPosition()));
					}
					else
					{
						fixedPositions.append(',');
						fixedPositions.append(Integer.toString(answer.getPosition()));
					}
				}
			}
			
			// Add "fixedrows" attribute to table if needed
			if (fixedPositions!=null)
			{
				table.setAttribute("fixedrows",fixedPositions.toString());
			}
		}
		return table;
	}
	
	/**
	 * Create XML nodes with feedbacks of desired type for a question.
	 * @param doc XML document
	 * @param question Question
	 * @param type Feedback type
	 * @return XML nodes with feedbacks of desired type for a question
	 */
	private static List<Element> createAdvancedFeedbacks(Document doc,Question question,String type)
	{
		List<Element> advancedFeedbacks=new ArrayList<Element>();
		if (question.getFeedbacks()!=null)
		{
			// Get a list of feedbacks of desired type sorted by position
			List<Feedback> feedbacks=new ArrayList<Feedback>();
			for (Feedback feedback:question.getFeedbacks())
			{
				if (feedback!=null && type.equals(feedback.getFeedbackType().getType()))
				{
					feedbacks.add(feedback);
				}
			}
			Collections.sort(feedbacks,getFeedbackComparatorByPosition());
			
			// Add advanced feedbacks
			for (Feedback feedback:feedbacks)
			{
				Element advancedFeedback=null;
				Element imageFeedback=null;
				if (feedback.getText()!=null && !feedback.getText().equals(""))
				{
					advancedFeedback=doc.createElement("t");
					for (Node n:createTextWithBreaks(doc,feedback.getText(),question.isDisplayEquations()))
					{
						advancedFeedback.appendChild(n);
					}
					
					if (feedback.getResource()!=null)
					{
						// Test resource type (actually only images are supported)
						if (isImage(feedback.getResource()))
						{
							// Add break
							advancedFeedback.appendChild(doc.createElement("break"));
							
							imageFeedback=doc.createElement("image");
							advancedFeedback.appendChild(imageFeedback);
						}
					}
				}
				else if (feedback.getResource()!=null)
				{
					// Test resource type (actually only images are supported)
					if (isImage(feedback.getResource()))
					{
						advancedFeedback=doc.createElement("t");
						
						imageFeedback=doc.createElement("image");
						advancedFeedback.appendChild(imageFeedback);
					}
				}
				if (imageFeedback!=null)
				{
					imageFeedback.setAttribute("filePath",getOnlyFileName(feedback.getResource().getFileName()));
					imageFeedback.setAttribute("alt","");
					if (feedback.getResourceWidth()>0 && feedback.getResourceHeight()>0)
					{
						imageFeedback.setAttribute("width",Integer.toString(feedback.getResourceWidth()));
						imageFeedback.setAttribute("height",Integer.toString(feedback.getResourceHeight()));
					}
				}
				if (advancedFeedback!=null)
				{
					//Add id and conditions to trigger feedback
					advancedFeedback.setAttribute("id",feedback.getOmId());
					if (!feedback.getTest().equals("true"))
					{
						advancedFeedback.setAttribute("test",feedback.getTest());
					}
					if (feedback.getAnswer()!=null && !feedback.getAnswer().equals(""))
					{
						advancedFeedback.setAttribute("answer",feedback.getAnswer());
					}
					if (feedback.getAttemptsmin()>0)
					{
						advancedFeedback.setAttribute("attemptsmin",Integer.toString(feedback.getAttemptsmin()));
					}
					if (feedback.getAttemptsmax()<Integer.MAX_VALUE)
					{
						advancedFeedback.setAttribute("attemptsmax",Integer.toString(feedback.getAttemptsmax()));
					}
					if (feedback.getSelectedanswersmin()>0)
					{
						advancedFeedback.setAttribute(
							"selectedanswersmin",Integer.toString(feedback.getSelectedanswersmin()));
					}
					if (feedback.getSelectedanswersmax()<Integer.MAX_VALUE)
					{
						advancedFeedback.setAttribute(
							"selectedanswersmax",Integer.toString(feedback.getSelectedanswersmax()));
					}
					if (feedback.getSelectedrightanswersmin()>0)
					{
						advancedFeedback.setAttribute(
							"selectedrightanswersmin",Integer.toString(feedback.getSelectedrightanswersmin()));
					}
					if (feedback.getSelectedrightanswersmax()<Integer.MAX_VALUE)
					{
						advancedFeedback.setAttribute(
							"selectedrightanswersmax",Integer.toString(feedback.getSelectedrightanswersmax()));
					}
					if (feedback.getSelectedwronganswersmin()>0)
					{
						advancedFeedback.setAttribute(
							"selectedwronganswersmin",Integer.toString(feedback.getSelectedwronganswersmin()));
					}
					if (feedback.getSelectedwronganswersmax()<Integer.MAX_VALUE)
					{
						advancedFeedback.setAttribute(
							"selectedwronganswersmax",Integer.toString(feedback.getSelectedwronganswersmax()));
					}
					if (feedback.getUnselectedanswersmin()>0)
					{
						advancedFeedback.setAttribute(
							"unselectedanswersmin",Integer.toString(feedback.getUnselectedanswersmin()));
					}
					if (feedback.getUnselectedanswersmax()<Integer.MAX_VALUE)
					{
						advancedFeedback.setAttribute(
							"unselectedanswersmax",Integer.toString(feedback.getUnselectedanswersmax()));
					}
					if (feedback.getUnselectedrightanswersmin()>0)
					{
						advancedFeedback.setAttribute(
							"unselectedrightanswersmin",Integer.toString(feedback.getUnselectedrightanswersmin()));
					}
					if (feedback.getUnselectedrightanswersmax()<Integer.MAX_VALUE)
					{
						advancedFeedback.setAttribute(
							"unselectedrightanswersmax",Integer.toString(feedback.getUnselectedrightanswersmax()));
					}
					if (feedback.getUnselectedwronganswersmin()>0)
					{
						advancedFeedback.setAttribute(
							"unselectedwronganswersmin",Integer.toString(feedback.getUnselectedwronganswersmin()));
					}
					if (feedback.getUnselectedwronganswersmax()<Integer.MAX_VALUE)
					{
						advancedFeedback.setAttribute(
							"unselectedwronganswersmax",Integer.toString(feedback.getUnselectedwronganswersmax()));
					}
					if (feedback.getRightdistancemin()>0)
					{
						advancedFeedback.setAttribute(
							"rightdistancemin",Integer.toString(feedback.getRightdistancemin()));
					}
					if (feedback.getRightdistancemax()<Integer.MAX_VALUE)
					{
						advancedFeedback.setAttribute(
							"rightdistancemax",Integer.toString(feedback.getRightdistancemax()));
					}
					
					// Add gap
					advancedFeedback.appendChild(doc.createElement("gap"));
					
					// Add advanced feedback
					advancedFeedbacks.add(advancedFeedback);
				}
			}
		}
		return advancedFeedbacks;
	}
	
	/**
	 * Create custom answer line for a "Drag & Drop" question
	 * @param doc Document
	 * @return Custom answer line for a "Drag & Drop" question
	 */
	private static Element createDragDropAnswerLine(Document doc)
	{
		Element answerLine=doc.createElement("summaryline");
		answerLine.setAttribute("type","answerline");
		Element summaryFor=doc.createElement("summaryfor");
		answerLine.appendChild(summaryFor);
		Element answerName=doc.createElement("summaryattribute");
		if (questionBean==null)
		{
			answerName.setAttribute("attribute","answerid");
		}
		else
		{
			// Get current user session Hibernate operation
			Operation operation=questionBean.getCurrentUserOperation(null);
			
			StringBuffer answerMap=new StringBuffer("answermap[");
			StringBuffer answerMapValues=new StringBuffer();
			boolean insertComma=false;
			for (Answer answer:questionBean.getDroppableAnswersSortedByPosition(operation))
			{
				if (insertComma)
				{
					answerMap.append(',');
					answerMapValues.append(',');
				}
				else
				{
					insertComma=true;
				}
				answerMap.append(answer.getOmId());
				String answerMapValue=null;
				if (answer.getText()==null || answer.getText().equals(""))
				{
					answerMapValue=getDroppableAnswerName(operation,(DragDropAnswer)answer);
					if (answerMapValue==null)
					{
						answerMapValue=answer.getOmId();
					}
				}
				else
				{
					answerMapValue=answer.getText();
				}
				answerMapValues.append(escapeAnswerMapCharacters(answerMapValue));
			}
			answerMap.append(':');
			answerMap.append(answerMapValues);
			answerMap.append(']');
			answerName.setAttribute("attribute",answerMap.toString());
		}
		summaryFor.appendChild(answerName);
		Text separator=doc.createTextNode(":");
		summaryFor.appendChild(separator);
		Element singleAnswerLine=doc.createElement("summaryattribute");
		singleAnswerLine.setAttribute("attribute","answerline");
		summaryFor.appendChild(singleAnswerLine);
		return answerLine;
	}
	
	/**
	 * Create summary line for question
	 * @param doc Document
	 * @return Summary line for question
	 */
	private static Element createSummaryLine(Document doc)
	{
		Element summaryLine=doc.createElement("summaryline");
		Text attemptText=doc.createTextNode("Attempt ");
		summaryLine.appendChild(attemptText);
		Element attemptSummaryAttribute=doc.createElement("summaryattribute");
		attemptSummaryAttribute.setAttribute("attribute","attempt");
		summaryLine.appendChild(attemptSummaryAttribute);
		Text selectedAnswerText=doc.createTextNode(": Selected answer: ");
		summaryLine.appendChild(selectedAnswerText);
		Element answerlineSummaryAttribute=doc.createElement("summaryattribute");
		answerlineSummaryAttribute.setAttribute("attribute","answerline");
		summaryLine.appendChild(answerlineSummaryAttribute);
		return summaryLine;
	}
	
	/**
	 * Create XML nodes to show a text with gaps 
	 * @param doc XML document
	 * @param text Text
	 * @return XML nodes to show a text with gaps
	 */
	public static List<Node> createTextWithBreaks(Document doc,String text)
	{
		List<Node> textNodes=new ArrayList<Node>();
		if (text!=null && !text.equals(""))
		{
			// First we normalize new line characters
			text=text.replaceAll(Pattern.quote("\r\n"),"\n").replaceAll(Pattern.quote("\r"),"\n");
			
			// We get lines splitting by new line character
			String[] lines=text.split("\n");
			for (int i=0;i<lines.length-1;i++)
			{
				// Add line's text
				textNodes.add(doc.createTextNode(lines[i]));
				
				// Add break
				textNodes.add(doc.createElement("break"));
			}
			// Add last line
			textNodes.add(doc.createTextNode(lines[lines.length-1]));
		}
		return textNodes;
	}
	
	/**
	 * Create XML nodes to show a text with breaks and optionally equations.
	 * @param doc XML document
	 * @param text Text
	 * @param displayEquations true to display equations, false otherwise
	 * @return XML nodes to show a text with breaks and optionally equations
	 */
	public static List<Node> createTextWithBreaks(Document doc,String text,boolean displayEquations)
	{
		return displayEquations?createTextWithBreaksAndEquations(doc,text):createTextWithBreaks(doc,text);
	}
	
	/**
	 * Create XML nodes to show a text with breaks and equations 
	 * @param doc XML document
	 * @param text Text
	 * @return XML nodes to show a text with breaks and equations
	 */
	public static List<Node> createTextWithBreaksAndEquations(Document doc,String text)
	{
		List<Node> textNodes=new ArrayList<Node>();
		if (text!=null && !text.equals(""))
		{
			// First we normalize new line characters
			text=text.replaceAll(Pattern.quote("\r\n"),"\n").replaceAll(Pattern.quote("\r"),"\n");
			
			// We get lines splitting by new line character
			String[] lines=text.split("\n");
			for (int i=0;i<lines.length;i++)
			{
				String[] textBlocks=lines[i].split(Pattern.quote("$$"),-1);
				for (int j=0;j<textBlocks.length;j++)
				{
					if (j%2==0)
					{
						// Add as plain text
						textNodes.add(doc.createTextNode(textBlocks[j]));
					}
					else if (j==textBlocks.length-1)
					{
						// Add as plain text (starting with $$)
						StringBuffer textBlockStartingWithDollars=new StringBuffer("$$");
						textBlockStartingWithDollars.append(textBlocks[j]);
						textNodes.add(doc.createTextNode(textBlockStartingWithDollars.toString()));
					}
					else
					{
						String[] equationAndAlt=textBlocks[j].split(Pattern.quote("$"));
						if (equationAndAlt.length>2)
						{
							// Add as plain text (surrounded with $$)
							StringBuffer textBlockSurroundedWithDollars=new StringBuffer("$$");
							textBlockSurroundedWithDollars.append(textBlocks[j]);
							textBlockSurroundedWithDollars.append("$$");
							textNodes.add(doc.createTextNode(textBlockSurroundedWithDollars.toString()));
						}
						else
						{
							// Add as equation
							Element equation=doc.createElement("equation");
							String alt=equationAndAlt.length==2?equationAndAlt[1]:equationAndAlt[0];
							equation.setAttribute("alt",alt);
							equation.appendChild(doc.createTextNode(equationAndAlt[0]));
							textNodes.add(equation);
						}
						
					}
				}
				
				if (i<lines.length-1)
				{
					// Add break
					textNodes.add(doc.createElement("break"));
				}
			}
		}
		return textNodes;
	}
	
	/**
	 * Get a comparator that can be used to sort a list of answers by position.<br/><br/>
	 * Note that can't be used with lists with null answers.
	 * @return Comparator that can be used to order a list of answers by position
	 */
	private static Comparator<Answer> getAnswerComparatorByPosition()
	{
		return new Comparator<Answer>()
		{
			@Override
			public int compare(Answer o1,Answer o2)
			{
				int compareResult=0;
				if (o1.getPosition()<o2.getPosition())
				{
					compareResult=-1;
				}
				else if (o1.getPosition()>o2.getPosition())
				{
					compareResult=1;
				}
				return compareResult;
			}
		};
	}
	
	/**
	 * Get a comparator that can be used to sort a list of feedbacks by position.<br/><br/>
	 * Note that can't be used with lists with null feedbacks.
	 * @return Comparator that can be used to order a list of feedbacks by position
	 */
	private static Comparator<Feedback> getFeedbackComparatorByPosition()
	{
		return new Comparator<Feedback>()
		{
			@Override
			public int compare(Feedback o1,Feedback o2)
			{
				int compareResult=0;
				if (o1.getPosition()<o2.getPosition())
				{
					compareResult=-1;
				}
				else if (o1.getPosition()>o2.getPosition())
				{
					compareResult=1;
				}
				return compareResult;
			}
		};
	}
	
	/**
	 * Creates the directoy where we are going to save question.xml and GenericQuestion.java files
	 * if it is needed.
	 * @param packageName Package's name
	 * @param path Path
	 * @throws Exception
	 */
	public static void createFullPathDirectory(String packageName,String path) throws Exception
	{
		String fullPath=getFullPath(packageName,path);
		File fullPathDirectory=new File(fullPath);
		try
		{
			if (fullPathDirectory.exists())
			{
				if (!fullPathDirectory.canExecute() || !fullPathDirectory.canRead() || !fullPathDirectory.canWrite())
				{
					StringBuffer error=new StringBuffer("Error. You have no permissions to access path: ");
					error.append(fullPath);
					throw new Exception(error.toString());
				}
			}
			else
			{
				if (!fullPathDirectory.mkdirs())
				{
					StringBuffer error=new StringBuffer("Error. Can't create path: ");
					error.append(fullPath);
					throw new Exception(error.toString());
				}
			}
		}
		catch (SecurityException se)
		{
			StringBuffer error=new StringBuffer("Error. You have no permissions to access path: ");
			error.append(fullPath);
			throw new Exception(error.toString());
		}
	}
	
	/**
	 * Create and return an instance of org.w3c.Document class with the XML document appropiated to
	 * the <i>__name__.deploy.xml</i> file of the question for testing purposes.
	 * @param question Question
	 * @return An instance of org.w3c.Document class with the XML document appropiated to the 
	 * <i>__name__.deploy.xml</i> file of the question for testing purposes
	 * @throws Exception
	 */
	private static Document createDeployDocument(Question question) throws Exception
	{
		return createDeployDocument(new QuestionRelease(question,null));
	}
	
	/**
	 * Create and return an instance of org.w3c.Document class with the XML document appropiated to
	 * the <i>__name__.deploy.xml</i> file of the question.
	 * @param questionRelease Question release
	 * @return An instance of org.w3c.Document class with the XML document appropiated to the 
	 * <i>__name__.deploy.xml</i> file of the question for publishing purposes
	 * @throws Exception
	 */
	private static Document createDeployDocument(QuestionRelease questionRelease) throws Exception
	{
		// Initialize document
		DocumentBuilderFactory dbf=DocumentBuilderFactory.newInstance();
		DocumentBuilder db=dbf.newDocumentBuilder();
		Document doc=db.newDocument();
		
		// Creating XML tree
		Element root=doc.createElement("deploy");
		doc.appendChild(root);
		
		// Add question
		Element question=doc.createElement("question");
		root.appendChild(question);
		Text questionId=doc.createTextNode(questionRelease.getQuestion().getPackage());
		question.appendChild(questionId);
		
		// Add publisher (if defined)
		if (questionRelease.getPublisher()!=null)
		{
			Element publisher=doc.createElement("publisher");
			root.appendChild(publisher);
			Text publisherOucu=doc.createTextNode(questionRelease.getPublisher().getOucu());
			publisher.appendChild(publisherOucu);
		}
		
		// Initialize DateFormat object to output dates with the format yyyy-MM-dd HH:mm:ss
		DateFormat df=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		// Add release date (if defined)
		if (questionRelease.getReleaseDate()!=null)
		{
			Element releaseDate=doc.createElement("release-date");
			root.appendChild(releaseDate);
			Text releaseDateText=doc.createTextNode(df.format(questionRelease.getReleaseDate()));
			releaseDate.appendChild(releaseDateText);
		}
		
		// Add delete date (if defined)
		if (questionRelease.getDeleteDate()!=null)
		{
			Element deleteDate=doc.createElement("delete-date");
			root.appendChild(deleteDate);
			Text deleteDateText=doc.createTextNode(df.format(questionRelease.getDeleteDate()));
			deleteDate.appendChild(deleteDateText);
		}
		
		// Add access
		Element access=doc.createElement("access");
		root.appendChild(access);
		
		// Add users to access
		Element users=doc.createElement("users");
		if (questionRelease.isAllUsersAllowed())
		{
			users.setAttribute("world","yes");
		}
		else
		{
			for (User user:questionRelease.getUsers())
			{
				Element oucu=doc.createElement("oucu");
				Text userId=doc.createTextNode(user.getOucu());
				oucu.appendChild(userId);
				users.appendChild(oucu);
			}
		}
		access.appendChild(users);
		
		// Add dates
		Element dates=doc.createElement("dates");
		root.appendChild(dates);
		
		// Add open to dates
		Element open=doc.createElement("open");
		dates.appendChild(open);
		Text openText=null;
		Date startDate=questionRelease.getStartDate();
		if (startDate==null)
		{
			openText=doc.createTextNode("yes");
		}
		else
		{
			openText=doc.createTextNode(df.format(startDate));
		}
		open.appendChild(openText);
		Date warningDate=questionRelease.getWarningDate();
		Date closeDate=questionRelease.getCloseDate();
		// Note that warning date is ignored if there is no close date even if it is defined
		if (closeDate!=null)
		{
			String closeDateStr=df.format(closeDate);
			
			// Add close to dates
			Element close=doc.createElement("close");
			dates.appendChild(close);
			Text closeText=null;
			if (warningDate==null)
			{
				closeText=doc.createTextNode(closeDateStr);
			}
			else
			{
				closeText=doc.createTextNode(df.format(warningDate));
			}
			close.appendChild(closeText);
			
			// Add forbid to dates
			Element forbid=doc.createElement("forbid");
			dates.appendChild(forbid);
			Text forbidText=doc.createTextNode(closeDateStr);
			forbid.appendChild(forbidText);
		}
		
		return doc;
	}
	
	/**
	 * Creates a question.xml file with the generated XML document for question and saves it in the appropiated
	 * path
	 * @param doc Generated XML document for question
	 * @param packageName Package
	 * @param path Path
	 * @throws Exception
	 */
	private static void createQuestionXmlFile(Document doc,String packageName,String path) throws Exception
	{
		// Initialize XML transformer
		Transformer t=null;
		try
		{
			TransformerFactory tf=TransformerFactory.newInstance();
			t=tf.newTransformer();
			t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,"yes");
			t.setOutputProperty(OutputKeys.INDENT,"yes");
		}
		catch (TransformerConfigurationException tce)
		{
			throw new Exception(
					"Error. Unexpected error while configuring XML transformer to output question.xml file");
		}
		catch (IllegalArgumentException iae)
		{
			throw new Exception(
					"Error. Unexpected error while configuring XML transformer to output question.xml file");
		}
		Result r=new StreamResult(new File(getFullPathToFile(packageName,path,"question.xml")));
		Source s=new DOMSource(doc);
		try
		{
			t.transform(s,r);
		}
		catch (TransformerException te)
		{
			throw new Exception("Error. An error has occurred while trying to output question.xml file");
		}
	}
	
	/**
	 * Creates a GenericQuestion.java file with an empty class derived from om.helper.uned.GenericQuestion class
	 * in the appropiated path and in the package indicated 
	 * @param packageName Package's name
	 * @param path Path
	 * @throws Exception
	 */
	private static void createGenericQuestionJavaFile(String packageName,String path) throws Exception
	{
		try
		{
			PrintWriter pw=new PrintWriter(new File(getFullPathToFile(packageName,path,"GenericQuestion.java")));
			
			// Print package inside GenericQuestion.java file
			pw.print("package ");
			pw.print(packageName);
			pw.println(';');
			pw.println();
			
			// Print a GenericQuestion empty class derived from om.helper.uned.GenericQuestion class
			pw.println("public class GenericQuestion extends om.helper.uned.GenericQuestion");
			pw.println('{');
			pw.println('}');
			pw.close();
		}
		catch (FileNotFoundException fnfe)
		{
			throw new Exception("Error. An error has occurred while trying to output GenericQuestion.java file");
		}
	}
	
	/**
	 * Creates a <i>u<b>&lt;user id&gt;</b>.q<b>&lt;question id&gt;</b>.deploy.xml</i> file with the generated 
	 * deploy XML document and uploads it to OM Test Navigator Web Application
	 * @param doc Generated deploy XML document
	 * @param packageName Package's name
	 * @param omTnURL OM test navigator web application URL 
	 * @param publish true if we are publishing the xml to a Test Navigator production environment, false if we are
	 * deploying the jar to a Test Navigator test environment
	 * @throws Exception
	 */
	private static void createDeployXmlFile(Document doc,String packageName,String omTnURL,boolean publish) 
		throws Exception
	{
		// Initialize XML transformer
		Transformer t=null;
		try
		{
			TransformerFactory tf=TransformerFactory.newInstance();
			t=tf.newTransformer();
			t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,"no");
			t.setOutputProperty(OutputKeys.VERSION,"1.0");
			t.setOutputProperty(OutputKeys.ENCODING,"UTF-8");
			t.setOutputProperty(OutputKeys.INDENT,"yes");
		}
		catch (TransformerConfigurationException tce)
		{
			StringBuffer error=new StringBuffer();
			error.append("Error. Unexpected error while configuring XML transformer to create deploy XML file for ");
			error.append(packageName);
			error.append(" question");
			throw new Exception(error.toString());
			
		}
		catch (IllegalArgumentException iae)
		{
			StringBuffer error=new StringBuffer();
			error.append("Error. Unexpected error while configuring XML transformer to create deploy XML file for ");
			error.append(packageName);
			error.append(" question");
			throw new Exception(error.toString());
			
		}
		ByteArrayOutputStream baos=new ByteArrayOutputStream(8192);
		Result r=new StreamResult(baos);
		Source s=new DOMSource(doc);
		try
		{
			t.transform(s,r);
		}
		catch (TransformerException te)
		{
			StringBuffer error=new StringBuffer();
			error.append("Error. Unexpected error has occurred during transformation to create deploy XML file for ");
			error.append(packageName);
			error.append(" question");
			throw new Exception(error.toString());
			
		}
		StringBuffer omTnWsURL=new StringBuffer(omTnURL);
		if (omTnURL.charAt(omTnURL.length()-1)!='/')
		{
			omTnWsURL.append('/');
		}
		omTnWsURL.append("services/OmTn");
		OmTnProxy omTnWs=new OmTnProxy();
		omTnWs.setEndpoint(omTnWsURL.toString());
		String[] base64DeployXml=new String[1];
		base64DeployXml[0]=Base64.encode(baos.toByteArray());
		if (!omTnWs.uploadDeployXml(
			publish?OmTnProEncryptor.encrypt(packageName):OmTnEncryptor.encrypt(packageName),0,base64DeployXml))
		{
			StringBuffer error=new StringBuffer();
			error.append("Error. An error has ocurred while trying to upload deploy XML file for ");
			error.append(packageName);
			error.append(" question to OM Test Navigator Web Application");
		}
	}
	
	/**
	 * Copy referenced question's jar file from OM developer web applicacion to OM test navigator web application,
	 * renaming it appropiately.
	 * @param packageName Package's name
	 * @param omURL OM developer web application URL
	 * @param omTnURL OM test navigator web application URL (test or production environment)
	 * @param publish true if we are publishing the jar to a Test Navigator production environment, 
	 * false if we are deploying the jar to a Test Navigator test environment
	 * @throws Exception
	 */
	private static void copyJarFile(String packageName,String omURL,String omTnURL,boolean publish) throws Exception
	{
		StringBuffer omDevWsURL=new StringBuffer(omURL);
		if (omURL.charAt(omURL.length()-1)!='/')
		{
			omDevWsURL.append('/');
		}
		omDevWsURL.append("services/OmDev");
		OmDevProxy omDevWs=new OmDevProxy();
		omDevWs.setEndpoint(omDevWsURL.toString());
		
		StringBuffer omTnWsURL=new StringBuffer(omTnURL);
		if (omTnURL.charAt(omTnURL.length()-1)!='/')
		{
			omTnWsURL.append('/');
		}
		omTnWsURL.append("services/OmTn");
		OmTnProxy omTnWs=new OmTnProxy();
		omTnWs.setEndpoint(omTnWsURL.toString());
		
		// Copy jar file
		String[] base64QuestionJar=omDevWs.downloadQuestionJar(packageName);
		if (!omTnWs.uploadQuestionJar(
			publish?OmTnProEncryptor.encrypt(packageName):OmTnEncryptor.encrypt(packageName),publish?null:"1.0",
			base64QuestionJar))
		{
			StringBuffer error=new StringBuffer();
			error.append("Error. An error has occurred while trying to upload jar for ");
			error.append(packageName);
			error.append(" question to OM Test Navigator Web Application ");
			error.append(publish?"production":"test");
			error.append(" environment");
			throw new Exception(error.toString());
		}
	}
	
	/**
	 * Get full class name to put in the "class" attribute of root element.
	 * @param packageName Package's name
	 * @return Full class name to put in the "class" attribute of root element
	 */
	private static String getFullClassName(String packageName)
	{
		StringBuffer sbFullClassName=new StringBuffer(packageName);
		sbFullClassName.append(".GenericQuestion");
		return sbFullClassName.toString();
	}
	
	/**
	 * Get file name without path
	 * @param fullFileName Full file name with a path
	 * @return File name without path
	 */
	public static String getOnlyFileName(String fullFileName)
	{
		return new File(fullFileName).getName();
	}
	
	/**
	 * Get full path to the indicated filename with the full path obtained from path and package
	 * @param packageName Package
	 * @param path Path
	 * @param fileName Filename
	 * @return Full path to the indicated filename with the full path obtained from path and package
	 */
	private static String getFullPathToFile(String packageName,String path,String fileName)
	{
		StringBuffer fullPathToFile=new StringBuffer(getFullPath(packageName,path));
		fullPathToFile.append(fileName);
		return fullPathToFile.toString();
	}
	
	/**
	 * Get full path from path and package.
	 * @param packageName Package
	 * @param path Path
	 * @return Full path from path and package
	 */
	public static String getFullPath(String packageName,String path)
	{
		StringBuffer fullPath=new StringBuffer(path);
		if (!path.equals("") && !path.endsWith("/") && !path.endsWith("\\"))
		{
			fullPath.append('/');
		}
		for (int i=0;i<packageName.length();i++)
		{
			char c=packageName.charAt(i);
			if (c=='.')
			{
				fullPath.append('/');
			}
			else
			{
				fullPath.append(c);
			}
		}
		fullPath.append('/');
		return fullPath.toString();
	}
	
	/**
	 * Get same string but with some characters escaped: " < > &  
	 * @param s String
	 * @return Same string but with some characters escaped: " < > &
	 */
	public static String escapeXML(String s)
	{
		StringBuffer escaped=new StringBuffer();
		for (int i=0;i<s.length();i++)
		{
			char c=s.charAt(i);
			switch (c)
			{
				case '\"':
					escaped.append("&quot;");
					break;
				case '<':
					escaped.append("&lt;");
					break;
				case '>':
					escaped.append("&gt;");
					break;
				case '&':
					escaped.append("&amp;");
					break;
				default:
					escaped.append(c);
			}
		}
		return escaped.toString();
	}
	
	/**
	 * Get same string but with some characters escaped: , : [ ] \
	 * @param s String
	 * @return Same string but with some characters escaped: , : [ ] \
	 */
	private static String escapeAnswerMapCharacters(String s)
	{
		StringBuffer escaped=new StringBuffer();
		for (int i=0;i<s.length();i++)
		{
			char c=s.charAt(i);
			switch (c)
			{
				case ',':
				case ':':
				case '[':
				case ']':
				case '\\':
					escaped.append('\\');
					escaped.append(c);
					break;
				default:
					escaped.append(c);
			}
		}
		return escaped.toString();
	}
	
	/**
	 * @param str String
	 * @return true if the str is null or empty
	 */
	private static boolean isEmpty(String str)
	{
		return str==null || str.length()==0;
	}
	
	/**
	 * Delete question from OM Developer web application
	 * @param packageName Package's name
	 * @param omURL OM developer web application URL
	 * @throws Exception
	 */
	private static void deleteOMQuestion(String packageName,String omURL) throws Exception
	{
		StringBuffer omDevWsURL=new StringBuffer(omURL);
		if (omURL.charAt(omURL.length()-1)!='/')
		{
			omDevWsURL.append('/');
		}
		omDevWsURL.append("services/OmDev");
		OmDevProxy omDevWs=new OmDevProxy();
		omDevWs.setEndpoint(omDevWsURL.toString());
		omDevWs.deleteQuestion(packageName);
	}
	
	/**
	 * Delete <i>u<b>&lt;user id&gt;</b>.q<b>&lt;test id&gt;</b>.deploy.xml</i> file from the <i>testbank</i> folder 
	 * of Test Navigator web application.
	 * @param packageName Package's name
	 * @param omTnURL OM Test Navigator web application URL
	 * @param unpublish true if we are deleting a question release from a Test Navigator production environment,
	 * false if we are deleting a question from a Test Navigator test environment
	 */
	private static void deleteXmlFile(String packageName,String omTnURL,boolean unpublish) throws Exception
	{
		StringBuffer omTnWsURL=new StringBuffer(omTnURL);
		if (omTnURL.charAt(omTnURL.length()-1)!='/')
		{
			omTnWsURL.append('/');
		}
		omTnWsURL.append("services/OmTn");
		OmTnProxy omTnWs=new OmTnProxy();
		omTnWs.setEndpoint(omTnWsURL.toString());
		omTnWs.deleteQuestionXml(
			unpublish?OmTnProEncryptor.encrypt(packageName):OmTnEncryptor.encrypt(packageName));
	}
	
	/**
	 * Delete question's jar file from the <i>questionbank</i> folder of the OM Test Navigator web application.
	 * @param packageName Package's name
	 * @param omTnURL OM test navigator web application URL 
	 */
	private static void deleteJarFile(String packageName,String omTnURL) throws Exception
	{
		StringBuffer omTnWsURL=new StringBuffer(omTnURL);
		if (omTnURL.charAt(omTnURL.length()-1)!='/')
		{
			omTnWsURL.append('/');
		}
		omTnWsURL.append("services/OmTn");
		OmTnProxy omTnWs=new OmTnProxy();
		omTnWs.setEndpoint(omTnWsURL.toString());
		omTnWs.deleteQuestionJar(OmTnEncryptor.encrypt(packageName));
	}
	
	/**
	 * @param packageName Package's name
	 * @param omURL OM developer web application URL 
	 * @return Question's jar last modified date
	 * @throws Exception
	 */
	public static Date getQuestionJarLastModifiedDate(String packageName,String omURL) throws Exception
	{
		StringBuffer omDevWsURL=new StringBuffer(omURL);
		if (omURL.charAt(omURL.length()-1)!='/')
		{
			omDevWsURL.append('/');
		}
		omDevWsURL.append("services/OmDev");
		OmDevProxy omDevWs=new OmDevProxy();
		omDevWs.setEndpoint(omDevWsURL.toString());
		return new Timestamp(omDevWs.getQuestionJarLastModified(packageName));
	}
	
	/**
	 * Stop all active Test Navigator sessions that are using a question.<br/><br/>
	 * It is needed to be sure that the jar file for that question at <i>questioncache</i> folder of
	 * OM Question Engine web application is unlocked so it can be deleted.
	 * @param packageName Package's name
	 * @param omTnURL OM test navigator web application URL 
	 * @throws Exception
	 */
	public static void stopAllSessionsForQuestion(String packageName,String omTnURL) throws Exception
	{
		StringBuffer omTnWsURL=new StringBuffer(omTnURL);
		if (omTnURL.charAt(omTnURL.length()-1)!='/')
		{
			omTnWsURL.append('/');
		}
		omTnWsURL.append("services/OmTn");
		OmTnProxy omTnWs=new OmTnProxy();
		omTnWs.setEndpoint(omTnWsURL.toString());
		omTnWs.stopAllSessionsForQuestion(OmTnEncryptor.encrypt(packageName));
	}
	
	/**
	 * Delete question's jar file from the <i>questioncache</i> folder of the OM Question Engine web application 
	 * if it exists.
	 * @param packageName Package's name
	 * @param omQeURL OM question engine web application URL 
	 */
	public static void deleteJarFileFromQuestionEngineCache(String packageName,String omQeURL) throws Exception
	{
		StringBuffer omQeWsURL=new StringBuffer(omQeURL);
		if (omQeURL.charAt(omQeURL.length()-1)!='/')
		{
			omQeWsURL.append('/');
		}
		omQeWsURL.append("services/OmQe");
		OmQeProxy omQeWs=new OmQeProxy();
		omQeWs.setEndpoint(omQeWsURL.toString());
		omQeWs.deleteQuestionFromCache(packageName);
	}
}
