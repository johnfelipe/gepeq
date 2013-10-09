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
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import es.uned.lsi.gepec.model.dao.QuestionOrdersDao;
import es.uned.lsi.gepec.model.dao.QuestionsDao;
import es.uned.lsi.gepec.model.dao.SectionsDao;
import es.uned.lsi.gepec.model.dao.TestsDao;
import es.uned.lsi.gepec.model.entities.Question;
import es.uned.lsi.gepec.model.entities.QuestionOrder;
import es.uned.lsi.gepec.model.entities.Section;
import es.uned.lsi.gepec.model.entities.Test;
import es.uned.lsi.gepec.model.entities.TestFeedback;
import es.uned.lsi.gepec.model.entities.TestRelease;
import es.uned.lsi.gepec.model.entities.User;
import es.uned.lsi.gepec.om.axis.OmDevProxy;
import es.uned.lsi.gepec.om.axis.OmTnProxy;
import es.uned.lsi.gepec.util.HtmlUtils;
import es.uned.lsi.gepec.util.OmTnEncryptor;
import es.uned.lsi.gepec.util.OmTnProEncryptor;
import es.uned.lsi.gepec.web.backbeans.EvaluatorBean;
import es.uned.lsi.gepec.web.backbeans.SupportContactBean;
import es.uned.lsi.gepec.web.services.LocalizationService;

public class TestGenerator
{
	/** Maximum score used for a single question with a base weight of 1 */
	private final static int MAX_BASE_SINGLE_QUESTION_SCORE=3;
	
	/**
	 * Localization service.
	 */
	private static LocalizationService localizationService=null;
	
	/**
	 * Generate <i>u<b>&lt;user id&gt;</b>.t<b>&lt;test id&gt;</b>.test.xml</i> and 
	 * <i>u<b>&lt;user id&gt;</b>.t<b>&lt;test id&gt;</b>.deploy.xml</i> files and put them in the 
	 * <i>testbank</i> folder of the OM test navigator web application.<br/><br/>
	 * Moreover copy all referenced question's jar files from the <i>questions</i> folder of the OM developer
	 * web applicacion to the <i>questionbank</i> folder of the OM test navigator 
	 * web application renaming them appropiately.<br/><br/>
	 * <b>IMPORTANT:</b> All referenced questions must have been generated and compiled previously to be able 
	 * to generate a test.<br/><br/>
	 * Also note that you can use 'overwrite' argument to allow (true) or not allow (false) 
	 * to overwrite existing test or deploy xml files on <i>testbank</i> folder of the OM test navigator 
	 * web application.<br/><br/>
	 * Be careful that if you don't allow to overwrite them and they exist, test will not be generated 
	 * and an exception will be thrown.
	 * @param test Test
	 * @param omURL OM developer web application URL 
	 * @param omQeURL OM question engine web application URL 
	 * @param omTnURL OM test navigator web application URL 
	 * @param overwrite Flag to indicate that referenced OM Test Navigator test and deploy xml files will be 
	 * overwritten if they exist (true) or not (false)
	 * @throws Exception
	 */
	public static void generateTest(Test test,String omURL,String omQeURL,String omTnURL,boolean overwrite) 
		throws Exception
	{
		deployTest(new TestRelease(test,0,null),omURL,omQeURL,omTnURL,overwrite,false);
	}
	
	/**
	 * Generate <i>u<b>&lt;user id&gt;</b>.t<b>&lt;test id&gt;</b>.test.xml</i> and 
	 * <i>u<b>&lt;user id&gt;</b>.t<b>&lt;test id&gt;</b>.deploy.xml</i> files and put them in the 
	 * <i>testbank</i> folder of the OM test navigator web application.<br/><br/>
	 * Moreover copy all referenced question's jar files from the <i>questions</i> folder of the OM developer
	 * web applicacion to the <i>questionbank</i> folder of the OM test navigator 
	 * web application renaming them appropiately.<br/><br/>
	 * <b>IMPORTANT:</b> All referenced questions must have been generated and compiled previously to be able 
	 * to generate a test.<br/><br/>
	 * Also note that you can use 'overwrite' argument to allow (true) or not allow (false) 
	 * to overwrite existing test or deploy xml files on <i>testbank</i> folder of the OM test navigator 
	 * web application.<br/><br/>
	 * Be careful that if you don't allow to overwrite them and they exist, test will not be generated 
	 * and an exception will be thrown.
	 * @param testRelease Test release
	 * @param omURL OM developer web application URL 
	 * @param omTnProURL OM test navigator production environment web application URL 
	 * @throws Exception
	 */
	public static void publishTest(TestRelease testRelease,String omURL,String omTnProURL) throws Exception
	{
		deployTest(testRelease,omURL,null,omTnProURL,false,true);
	}
	
	/**
	 * Generate <i>u<b>&lt;user id&gt;</b>.t<b>&lt;test id&gt;</b>.test.xml</i> and 
	 * <i>u<b>&lt;user id&gt;</b>.t<b>&lt;test id&gt;</b>.deploy.xml</i> files and put them in the 
	 * <i>testbank</i> folder of the OM test navigator web application.<br/><br/>
	 * Moreover copy all referenced question's jar files from the <i>questions</i> folder of the OM developer
	 * web applicacion to the <i>questionbank</i> folder of the OM test navigator 
	 * web application renaming them appropiately.<br/><br/>
	 * <b>IMPORTANT:</b> All referenced questions must have been generated and compiled previously to be able 
	 * to generate a test.<br/><br/>
	 * Also note that you can use 'overwrite' argument to allow (true) or not allow (false) 
	 * to overwrite existing test or deploy xml files on <i>testbank</i> folder of the OM test navigator 
	 * web application.<br/><br/>
	 * Be careful that if you don't allow to overwrite them and they exist, test will not be generated 
	 * and an exception will be thrown.
	 * @param testRelease Test release
	 * @param omURL OM developer web application URL 
	 * @param omQeURL OM question engine web application URL 
	 * @param omTnURL OM test navigator web application URL 
	 * @param overwrite Flag to indicate that referenced OM Test Navigator test and deploy xml files will be 
	 * overwritten if they exist (true) or not (false)
	 * @param publish true if we are publishing the xml to a Test Navigator production environment, 
	 * false if we are deploying the jar to a Test Navigator test environment
	 * @throws Exception
	 */
	private static void deployTest(TestRelease testRelease,String omURL,String omQeURL,String omTnURL,
		boolean overwrite,boolean publish) throws Exception
	{
		initialChecks(testRelease,omURL,omTnURL,overwrite,publish);
		initializeGEPEQServices();
		
		Document testDoc=createTestDocument(testRelease);
		Document deployDoc=createDeployDocument(testRelease);
		
		createTestXmlFile(testDoc,testRelease,omTnURL,publish);
		createDeployXmlFile(deployDoc,testRelease,omTnURL,publish);
		
		copyJarFiles(testRelease.getTest(),omURL,omQeURL,omTnURL,publish);
	}
	
	/**
	 * Delete <i>u<b>&lt;user id&gt;</b>.t<b>&lt;test id&gt;</b>.test.xml</i> and 
	 * <i>u<b>&lt;user id&gt;</b>.t<b>&lt;test id&gt;</b>.deploy.xml</i> files from the 
	 * <i>testbank</i> folder of the OM test navigator web application test environment.<br/><br/>
	 * Moreover you can use 'deleteJars' argument to delete all referenced question's jar files not used 
	 * in other GePEQ tests from the <i>questionbank</i> folder of the OM test navigator web application.
	 * <br/><br/>
	 * <b>IMPORTANT:</b> Be careful with 'deleteJars' option because only tests created with GePEQ will be 
	 * checked, so jar files still used by other tests can be deleted.
	 * @param test Test
	 * @param omTnURL OM test navigator web application test environment URL
	 * @param deleteJars Flag to delete all referenced question's jar files not used 
	 * in other GePEQ tests from the <i>questionbank</i> folder of the OM test navigator web application (true)
	 * or not delete them (false)
	 */
	public static void deleteTest(Test test,String omTnURL,boolean deleteJars) throws Exception
	{
		deleteXmlFiles(test,omTnURL);
		if (deleteJars)
		{
			deleteJarFiles(test,omTnURL);
		}
	}
	
	/**
	 * Delete <i>u<b>&lt;user id&gt;</b>.t<b>&lt;test id&gt;</b>.test.xml</i> and 
	 * <i>u<b>&lt;user id&gt;</b>.t<b>&lt;test id&gt;</b>.deploy.xml</i> files from the 
	 * <i>testbank</i> folder of the OM test navigator web application production environment.
	 * @param testRelease Test release
	 * @param omTnProURL OM test navigator web application production environment URL
	 */
	public static void unpublishTestRelease(TestRelease testRelease,String omTnProURL) throws Exception
	{
		deleteXmlFiles(testRelease,omTnProURL);
	}
	
	/**
	 * Perform several initial checks before start generating any files.
	 * @param testRelease Test release
	 * @param omURL OM Developer web application URL 
	 * @param omTnURL OM Test Navigator web application URL (test or production environment) 
	 * @param overwrite Flag to indicate that referenced OM Test Navigator test and deploy xml files will be overwritten 
	 * if they exist (true) or not (false)
	 * @param publish true if we are publishing the xml to a Test Navigator production environment, 
	 * false if we are deploying the jar to a Test Navigator test environment
	 * @throws Exception
	 */
	private static void initialChecks(TestRelease testRelease,String omURL,String omTnURL,boolean overwrite,
		boolean publish) throws Exception
	{
		// Check test is valid (not null and with a name) 
		checkTest(testRelease.getTest());
		
		// Check that referenced questions has been deployed on OM Test Navigator web application
		// or built on OM Developer web application
		checkQuestions(testRelease.getTest(),omURL,omTnURL,publish);
		
		if (!overwrite)
		{
			// Check that referenced test and deploy xml files don't exist on OM test navigator web application
			checkTestNavigatorXmls(testRelease,omTnURL);
		}
	}
	
	/**
	 * Checks if test is valid
	 * @param test Test
	 * @throws Exception
	 */
	private static void checkTest(Test test) throws Exception
	{
		if (test==null)
		{
			throw new Exception("Error. There is no test to generate");
		}
		if (test.getName()==null || test.getName().equals(""))
		{
			throw new Exception("Error. The test has no name");
		}
	}
	
	/**
	 * Checks that referenced questions has been deployed on OM Test Navigator web application 
	 * or built on OM Developer web application.
	 * @param test Test
	 * @param omURL OM Developer web application URL 
	 * @param omTnURL OM Test Navigator web application URL (test or production environment)
	 * @param publish true if we are publishing the xml to a Test Navigator production environment, 
	 * false if we are deploying the jar to a Test Navigator test environment
	 * @throws Exception
	 */
	private static void checkQuestions(Test test,String omURL,String omTnURL,boolean publish) throws Exception
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
		
		String version=publish?null:"1.0";
		for (Section section:test.getSections())
		{
			if (section!=null)
			{
				for (QuestionOrder questionOrder:section.getQuestionOrders())
				{
					if (questionOrder!=null && questionOrder.getQuestion()!=null)
					{
						String packageName=questionOrder.getQuestion().getPackage();
						if (!omTnWs.existQuestionJar(packageName,version) && 
							!omDevWs.existQuestionJar(packageName))
						{
							StringBuffer error=new StringBuffer("Error. Question ");
							error.append(packageName);
							error.append(" has not been built on OM Developer Web Application"); 
							throw new Exception(error.toString());
						}
					}
				}
			}
		}
	}
	
	/**
	 * Checks that referenced question jar has been deployed on OM Test Navigator web application.
	 * @param packageName Package's name
	 * @param version Version or null to check last version jar
	 * @param omTnURL OM test navigator web application URL 
	 * @throws Exception
	 */
	public static void checkQuestionJar(String packageName,String version,String omTnURL) throws Exception
	{
		StringBuffer omTnWsURL=new StringBuffer(omTnURL);
		if (omTnURL.charAt(omTnURL.length()-1)!='/')
		{
			omTnWsURL.append('/');
		}
		omTnWsURL.append("services/OmTn");
		OmTnProxy omTnWs=new OmTnProxy();
		omTnWs.setEndpoint(omTnWsURL.toString());
		if (!omTnWs.existQuestionJar(packageName,version))
		{
			StringBuffer error=new StringBuffer("Error. Question jar for question ");
			error.append(packageName);
			error.append(" has not been deployed on OM Test Navigator Web Application"); 
			throw new Exception(error.toString());
		}
	}
	
	/**
	 * Checks that referenced test don't exist on OM Test Navigator web application 
	 * @param testRelease Test release
	 * @param omTnURL OM test navigator web application URL 
	 * @throws Exception
	 */
	public static void checkTestNavigatorXmls(TestRelease testRelease,String omTnURL) throws Exception
	{
		StringBuffer omTnWsURL=new StringBuffer(omTnURL);
		if (omTnURL.charAt(omTnURL.length()-1)!='/')
		{
			omTnWsURL.append('/');
		}
		omTnWsURL.append("services/OmTn");
		OmTnProxy omTnWs=new OmTnProxy();
		omTnWs.setEndpoint(omTnWsURL.toString());
		String signature=testRelease.getTest().getSignature();
		if (omTnWs.existTestXmls(signature,testRelease.getVersion()))
		{
			StringBuffer error=new StringBuffer("Error. Test ");
			error.append(signature);
			error.append(" already exists on OM Test Navigator Web Application"); 
			throw new Exception(error.toString());
		}
	}
	
	/**
	 * Initializes GEPEQ services.<br/><br/>
	 * Actually only localization service is initialized.
	 */
	private static void initializeGEPEQServices()
	{
		FacesContext facesContext=FacesContext.getCurrentInstance();
		if (facesContext==null)
		{
			localizationService=null;
		}
		else
		{
			// Get EL Resolver
			ELResolver resolver=facesContext.getApplication().getELResolver();
			
			// Get EL context
			ELContext elContext=facesContext.getELContext();
			
	        localizationService=(LocalizationService)resolver.getValue(elContext,null,"localizationService");
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
	 * Create and return an instance of org.w3c.Document class with the XML document appropiated to
	 * the <i>__name__.test.xml</i> file of the test.
	 * @param testRelease Test release
	 * @return An instance of org.w3c.Document class with the XML document appropiated to the 
	 * <i>__name__.test.xml</i> file of the test
	 * @throws Exception
	 */
	private static Document createTestDocument(TestRelease testRelease) throws Exception
	{
		// Calculate maximum scores for every section of a test.
		Map<Section,Integer> maxSectionsScores=getMaxSectionsScores(testRelease.getTest());
		
		// Calculate maximum score used for a single section with a base weight of 1
		int maxBaseSingleSectionScore=getMaxBaseSingleSectionScore(testRelease.getTest(),maxSectionsScores);
		
		// Initialize document
		DocumentBuilderFactory dbf=DocumentBuilderFactory.newInstance();
		DocumentBuilder db=dbf.newDocumentBuilder();
		Document doc=db.newDocument();
		
		// Creating XML tree
		Element root=doc.createElement("test");
		doc.appendChild(root);
		
		// Add title
		Element title=doc.createElement("title");
		root.appendChild(title);
		Text titleText=null;
		if (testRelease.getTest().getTitle()!=null && !testRelease.getTest().getTitle().equals(""))
		{
			titleText=doc.createTextNode(QuestionGenerator.escapeXML(testRelease.getTest().getTitle()));
		}
		else // if there is no title defined we use test's name as title
		{
			titleText=doc.createTextNode(QuestionGenerator.escapeXML(testRelease.getTest().getName()));
		}
		title.appendChild(titleText);
		
		// Add options
		Element options=doc.createElement("options");
		root.appendChild(options);
		options.setAttribute("freesummary",testRelease.isFreeSummary()?"yes":"no");
		options.setAttribute("freestop",testRelease.isFreeStop()?"yes":"no");
		options.setAttribute("summaryquestions",testRelease.isSummaryQuestions()?"yes":"no");
		options.setAttribute("summaryscores",testRelease.isSummaryScores()?"yes":"no");
		options.setAttribute("summaryattempts",testRelease.isSummaryAttempts()?"yes":"no");
		options.setAttribute("navigation",testRelease.isNavigation()?"yes":"no");
		String navLocation=testRelease.getNavLocation().getLocation();
		if ("NAVLOCATION_LEFT".equals(navLocation))
		{
			options.setAttribute("navlocation","left");
		}
		else if ("NAVLOCATION_BOTTOM".equals(navLocation))
		{
			options.setAttribute("navlocation","bottom");
		}
		else if ("NAVLOCATION_WIDE".equals(navLocation))
		{
			options.setAttribute("navlocation","wide");
		}
		else
		{
			options.setAttribute("navlocation","left");
		}
		String redoQuestion=testRelease.getRedoQuestion().getValue();
		if ("YES".equals(redoQuestion))
		{
			options.setAttribute("redoquestion","yes");
			options.setAttribute("redoquestionauto","yes");
		}
		else if ("NO".equals(redoQuestion))
		{
			options.setAttribute("redoquestion","no");
			options.setAttribute("redoquestionauto","no");
		}
		else if ("ASK".equals(redoQuestion))
		{
			options.setAttribute("redoquestion","yes");
			options.setAttribute("redoquestionauto","no");
		}
		else
		{
			options.setAttribute("redoquestion","no");
			options.setAttribute("redoquestionauto","no");
		}
		options.setAttribute("redotest",testRelease.isRedoTest()?"yes":"no");
		
		// Add content
		Element content=doc.createElement("content");
		root.appendChild(content);
		
		// Add <rescore> elements needed to content
		List<Element> contentRescores=
			createContentRescores(doc,testRelease.getTest(),maxSectionsScores,maxBaseSingleSectionScore);
		for (Element contentRescore:contentRescores)
		{
			content.appendChild(contentRescore);
		}
		
		// Add info to content
		Element info=doc.createElement("info");
		content.appendChild(info);
		
		// Add title to info
		Element infoTitle=doc.createElement("title");
		info.appendChild(infoTitle);
		if (testRelease.getTest().getPresentationTitle()!=null)
		{
			Text infoTitleText=
				doc.createTextNode(QuestionGenerator.escapeXML(testRelease.getTest().getPresentationTitle()));
			infoTitle.appendChild(infoTitleText);
		}
		
		// Add info paragraph if defined
		if (testRelease.getTest().getPresentation()!=null && !testRelease.getTest().getPresentation().equals("") && 
			!testRelease.getTest().getPresentation().equals("<br/>"))
		{
			Element infoParagraph=createHtmlOrPlainParagraph(doc,testRelease.getTest().getPresentation());
			info.appendChild(infoParagraph);
		}
		
		// Add sections to content
		List<Element> sections=
			createContentSections(doc,testRelease.getTest(),maxSectionsScores,maxBaseSingleSectionScore);
		for (Element section:sections)
		{
			content.appendChild(section);
		}
		
		// Add confirm
		Element confirm=doc.createElement("confirm");
		
		// Add confirm button
		if (testRelease.getTest().getPreliminarySummaryButton()==null || 
			testRelease.getTest().getPreliminarySummary().equals(""))
		{
			confirm.setAttribute("button",getLocalizedString("DEFAULT_SUMMARY_CONFIRM_BUTTON","Finish"));
		}
		else
		{
			confirm.setAttribute("button",testRelease.getTest().getPreliminarySummaryButton());
		}
		root.appendChild(confirm);
		
		// Add title to confirm
		Element confirmTitle=doc.createElement("title");
		confirm.appendChild(confirmTitle);
		Text confirmTitleText=null;
		if (testRelease.getTest().getPreliminarySummaryTitle()==null || 
			testRelease.getTest().getPreliminarySummaryTitle().equals(""))
		{
			confirmTitleText=doc.createTextNode(getLocalizedString("SUMMARY","Summary"));
		}
		else
		{
			confirmTitleText=
				doc.createTextNode(QuestionGenerator.escapeXML(testRelease.getTest().getPreliminarySummaryTitle()));
		}
		confirmTitle.appendChild(confirmTitleText);
		
		// Add confirm paragraph if defined
		if (testRelease.getTest().getPreliminarySummary()!=null && 
			!testRelease.getTest().getPreliminarySummary().equals("") && 
			!testRelease.getTest().getPreliminarySummary().equals("<br/>"))
		{
			Element confirmParagraph=createHtmlOrPlainParagraph(doc,testRelease.getTest().getPreliminarySummary());
			confirm.appendChild(confirmParagraph);
		}
		
		// Add final
		Element finalElement=doc.createElement("final");
		root.appendChild(finalElement);
		
		// Add summary to final if defined
		if (testRelease.getTest().isFeedbackDisplaySummary())
		{
			// Add paragraph previously to summary if defined
			if (testRelease.getTest().getFeedbackSummaryPrevious()!=null && 
				!testRelease.getTest().getFeedbackSummaryPrevious().equals("") &&
				!testRelease.getTest().getFeedbackSummaryPrevious().equals("<br/>"))
			{
				Element finalSummaryPrevious=
					createHtmlOrPlainParagraph(doc,testRelease.getTest().getFeedbackSummaryPrevious());
				finalElement.appendChild(finalSummaryPrevious);
			}
			
			// Add summary
			Element summary=doc.createElement("summary");
			summary.setAttribute("marks",testRelease.getTest().isFeedbackDisplaySummaryMarks()?"yes":"no");
			summary.setAttribute("attempts",testRelease.getTest().isFeedbackDisplaySummaryAttempts()?"yes":"no");
			finalElement.appendChild(summary);
		}
		
		// Add scores to final if defined
		if (testRelease.getTest().isFeedbackDisplayScores())
		{
			// Add paragraph previously to scores if defined
			if (testRelease.getTest().getFeedbackScoresPrevious()!=null && 
				!testRelease.getTest().getFeedbackScoresPrevious().equals("") &&
				!testRelease.getTest().getFeedbackScoresPrevious().equals("<br/>"))
			{
				Element finalScoresPrevious=
					createHtmlOrPlainParagraph(doc,testRelease.getTest().getFeedbackScoresPrevious());
				finalElement.appendChild(finalScoresPrevious);
			}
			
			// Add scores
			Element scores=doc.createElement("scores");
			scores.setAttribute("marks",testRelease.getTest().isFeedbackDisplayScoresMarks()?"yes":"no");
			scores.setAttribute("percentage",testRelease.getTest().isFeedbackDisplayScoresPercentages()?"yes":"no");
			finalElement.appendChild(scores);
			
			// Add total axislabel to scores
			Element overallAxislabel=doc.createElement("axislabel");
			// Note that we need to use 'total' instead of 'overall' as name because 'overall' is 
			// alphabetically before 'section' and that causes test results mail reports to display overall
			// scores first... so we changed name to 'total' so it is displayed the last.
			overallAxislabel.setAttribute("axis","total");
			scores.appendChild(overallAxislabel);
			Text overallAxislabelText=doc.createTextNode(getLocalizedString("OVERALL_AXIS_LABEL","Total:"));
			overallAxislabel.appendChild(overallAxislabelText);
			
			// Add an axislabel for each section to scores (only if there are several sections)
			if (sections.size()>1)
			{
				for (int sectionIndex=1;sectionIndex<=sections.size();sectionIndex++)
				{
					Element sectionAxislabel=doc.createElement("axislabel");
					StringBuffer axisName=new StringBuffer("section");
					axisName.append(sectionIndex);
					sectionAxislabel.setAttribute("axis",axisName.toString());
					scores.appendChild(sectionAxislabel);
					StringBuffer sectionScoreText=new StringBuffer();
					Section section=getSection(testRelease.getTest(),sectionIndex);
					if (section==null || section.getTitle()==null || section.getTitle().equals(""))
					{
						sectionScoreText.append(getLocalizedString("SECTION","Section"));
						sectionScoreText.append(' ');
						sectionScoreText.append(sectionIndex);
						sectionScoreText.append(':');
					}
					else
					{
						sectionScoreText.append(section.getTitle());
						if (section.getTitle().charAt(section.getTitle().length()-1)!=':')
						{
							sectionScoreText.append(':');
						}
					}
					Text sectionAxislabelText=doc.createTextNode(sectionScoreText.toString());
					sectionAxislabel.appendChild(sectionAxislabelText);
				}
			}
		}
		
		// Add paragraph previously to advanced feedbacks if defined
		if (testRelease.getTest().getFeedbackAdvancedPrevious()!=null && 
			!testRelease.getTest().getFeedbackAdvancedPrevious().equals("") &&
			!testRelease.getTest().getFeedbackAdvancedPrevious().equals("<br/>"))
		{
			Element finalScoresPrevious=
				createHtmlOrPlainParagraph(doc,testRelease.getTest().getFeedbackAdvancedPrevious());
			finalElement.appendChild(finalScoresPrevious);
		}
		
		// Add conditionals if there advanced feedbacks defined
		List<Element> finalConditionals=createFinalConditionals(doc,testRelease.getTest());
		for (Element finalConditional:finalConditionals)
		{
			finalElement.appendChild(finalConditional);
		}
		
		// Add paragraph next to advanced feedbacks if defined
		if (testRelease.getTest().getFeedbackAdvancedNext()!=null && 
			!testRelease.getTest().getFeedbackAdvancedNext().equals("") &&
			!testRelease.getTest().getFeedbackAdvancedNext().equals("<br/>"))
		{
			Element finalScoresNext=createHtmlOrPlainParagraph(doc,testRelease.getTest().getFeedbackAdvancedNext());
			finalElement.appendChild(finalScoresNext);
		}
		
		return doc;
	}
	
	/**
	 * @param test Test
	 * @param order Section's order
	 * @return Section matching order or null if there is no section matching order
	 */
	private static Section getSection(Test test,int order)
	{
		Section section=null;
		for (Section s:test.getSections())
		{
			if (s.getOrder()==order)
			{
				section=s;
				break;
			}
		}
		return section;
	}
	
	/**
	 * Create and return an instance of org.w3c.Document class with the XML document appropiated to
	 * the <i>__name__.deploy.xml</i> file of the test.
	 * @param testRelease Test release
	 * @return An instance of org.w3c.Document class with the XML document appropiated to the 
	 * <i>__name__.deploy.xml</i> file of the test
	 * @throws Exception
	 */
	private static Document createDeployDocument(TestRelease testRelease) throws Exception
	{
		// Initialize document
		DocumentBuilderFactory dbf=DocumentBuilderFactory.newInstance();
		DocumentBuilder db=dbf.newDocumentBuilder();
		Document doc=db.newDocument();
		
		// Creating XML tree
		Element root=doc.createElement("deploy");
		doc.appendChild(root);
		
		// Add definition
		Element definition=doc.createElement("definition");
		root.appendChild(definition);
		Text testId=doc.createTextNode(testRelease.getTest().getSignature());
		definition.appendChild(testId);
		
		// Add publisher (if defined)
		if (testRelease.getPublisher()!=null)
		{
			Element publisher=doc.createElement("publisher");
			root.appendChild(publisher);
			Text publisherOucu=doc.createTextNode(testRelease.getPublisher().getOucu());
			publisher.appendChild(publisherOucu);
		}
		
		// Initialize DateFormat object to output dates with the format yyyy-MM-dd HH:mm:ss
		DateFormat df=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		// Add release date (if defined)
		if (testRelease.getReleaseDate()!=null)
		{
			Element releaseDate=doc.createElement("release-date");
			root.appendChild(releaseDate);
			Text releaseDateText=doc.createTextNode(df.format(testRelease.getReleaseDate()));
			releaseDate.appendChild(releaseDateText);
		}
		
		// Add delete date (if defined)
		if (testRelease.getDeleteDate()!=null)
		{
			Element deleteDate=doc.createElement("delete-date");
			root.appendChild(deleteDate);
			Text deleteDateText=doc.createTextNode(df.format(testRelease.getDeleteDate()));
			deleteDate.appendChild(deleteDateText);
		}
		
		// Add supportcontacts if defined
		if (!testRelease.getSupportContacts().isEmpty())
		{
			Map<SupportContactBean,StringBuffer> supportContactsMap=new HashMap<SupportContactBean,StringBuffer>();
			for (SupportContactBean supportContact:testRelease.getSupportContacts())
			{
				SupportContactBean supportContactFilter=new SupportContactBean("",supportContact.getFilterType(),
					supportContact.getFilterSubtype(),supportContact.getFilterValue());
				if (supportContactsMap.containsKey(supportContactFilter))
				{
					StringBuffer supportContactsList=supportContactsMap.get(supportContactFilter);
					if (supportContactsList.length()>0)
					{
						supportContactsList.append(',');
					}
					supportContactsList.append(supportContact.getSupportContact());
				}
				else
				{
					supportContactsMap.put(
						supportContactFilter,new StringBuffer(supportContact.getSupportContact()));
				}
			}
			if (!supportContactsMap.isEmpty())
			{
				Element supportContacts=doc.createElement("supportcontacts");
				root.appendChild(supportContacts);
				for (Entry<SupportContactBean,StringBuffer> supportContactEntry:supportContactsMap.entrySet())
				{
					String filterType=supportContactEntry.getKey().getFilterType();
					if ("NO_FILTER".equals(filterType))
					{
						Text supportContactsValue=doc.createTextNode(supportContactEntry.getValue().toString());
						supportContacts.appendChild(supportContactsValue);
					}
					else if ("USER_FILTER".equals(filterType))
					{
						String userFilterType=null;
						String filterSubtype=supportContactEntry.getKey().getFilterSubtype();
						if ("USERS_SELECTION".equals(filterSubtype))
						{
							userFilterType="single-oucu";
						}
						else if ("RANGE_NAME".equals(filterSubtype))
						{
							userFilterType="range-name";
						}
						else if ("RANGE_SURNAME".equals(filterSubtype))
						{
							userFilterType="range-surname";
						}
						if (userFilterType!=null)
						{
							Element userFilter=doc.createElement("user-filter");
							userFilter.setAttribute("type",userFilterType);
							userFilter.setAttribute("value",supportContactEntry.getKey().getFilterValue());
							supportContacts.appendChild(userFilter);
							Text supportContactsValue=doc.createTextNode(supportContactEntry.getValue().toString());
							userFilter.appendChild(supportContactsValue);
						}
					}
					else if ("GROUP_FILTER".equals(filterType))
					{
						Element userFilter=doc.createElement("user-filter");
						userFilter.setAttribute("type","single-group");
						userFilter.setAttribute("value",supportContactEntry.getKey().getFilterValue());
						supportContacts.appendChild(userFilter);
						Text supportContactsValue=doc.createTextNode(supportContactEntry.getValue().toString());
						userFilter.appendChild(supportContactsValue);
					}
				}
			}
		}
		
		// Add evaluators if defined
		if (!testRelease.getEvaluators().isEmpty())
		{
			Map<EvaluatorBean,StringBuffer> evaluatorsMap=new HashMap<EvaluatorBean,StringBuffer>();
			for (EvaluatorBean evaluator:testRelease.getEvaluators())
			{
				EvaluatorBean evaluatorFilter=new EvaluatorBean("",evaluator.getFilterType(),
					evaluator.getFilterSubtype(),evaluator.getFilterValue());
				if (evaluatorsMap.containsKey(evaluatorFilter))
				{
					StringBuffer evaluatorsList=evaluatorsMap.get(evaluatorFilter);
					if (evaluatorsList.length()>0)
					{
						evaluatorsList.append(',');
					}
					evaluatorsList.append(evaluator.getEvaluator());
				}
				else
				{
					evaluatorsMap.put(evaluatorFilter,new StringBuffer(evaluator.getEvaluator()));
				}
			}
			if (!evaluatorsMap.isEmpty())
			{
				Element evaluators=doc.createElement("evaluators");
				root.appendChild(evaluators);
				for (Entry<EvaluatorBean,StringBuffer> evaluatorEntry:evaluatorsMap.entrySet())
				{
					String filterType=evaluatorEntry.getKey().getFilterType();
					if ("NO_FILTER".equals(filterType))
					{
						Text evaluatorsValue=doc.createTextNode(evaluatorEntry.getValue().toString());
						evaluators.appendChild(evaluatorsValue);
					}
					else if ("USER_FILTER".equals(filterType))
					{
						String userFilterType=null;
						String filterSubtype=evaluatorEntry.getKey().getFilterSubtype();
						if ("USERS_SELECTION".equals(filterSubtype))
						{
							userFilterType="single-oucu";
						}
						else if ("RANGE_NAME".equals(filterSubtype))
						{
							userFilterType="range-name";
						}
						else if ("RANGE_SURNAME".equals(filterSubtype))
						{
							userFilterType="range-surname";
						}
						if (userFilterType!=null)
						{
							Element userFilter=doc.createElement("user-filter");
							userFilter.setAttribute("type",userFilterType);
							userFilter.setAttribute("value",evaluatorEntry.getKey().getFilterValue());
							evaluators.appendChild(userFilter);
							Text evaluatorsValue=doc.createTextNode(evaluatorEntry.getValue().toString());
							userFilter.appendChild(evaluatorsValue);
						}
					}
					else if ("GROUP_FILTER".equals(filterType))
					{
						Element userFilter=doc.createElement("user-filter");
						userFilter.setAttribute("type","single-group");
						userFilter.setAttribute("value",evaluatorEntry.getKey().getFilterValue());
						evaluators.appendChild(userFilter);
						Text evaluatorsValue=doc.createTextNode(evaluatorEntry.getValue().toString());
						userFilter.appendChild(evaluatorsValue);
					}
				}
			}
		}
		
		// Add access
		Element access=doc.createElement("access");
		root.appendChild(access);
		
		// Add users and groups to access
		Element users=doc.createElement("users");
		if (testRelease.isAllUsersAllowed())
		{
			users.setAttribute("world","yes");
		}
		else
		{
			for (User user:testRelease.getUsers())
			{
				Element oucu=doc.createElement("oucu");
				Text userId=doc.createTextNode(user.getOucu());
				oucu.appendChild(userId);
				users.appendChild(oucu);
			}
			for (String userGroup:testRelease.getUserGroups())
			{
				Element authid=doc.createElement("authid");
				Text groupId=doc.createTextNode(userGroup);
				authid.appendChild(groupId);
				users.appendChild(authid);
			}
		}
		access.appendChild(users);
		
		// Add admins and groups to access
		Element admins=doc.createElement("admins");
		if (testRelease.isAllowAdminReports())
		{
			for (User admin:testRelease.getAdmins())
			{
				Element oucu=doc.createElement("oucu");
				oucu.setAttribute("reports","yes");
				Text adminId=doc.createTextNode(admin.getOucu());
				oucu.appendChild(adminId);
				admins.appendChild(oucu);
			}
			for (String adminGroup:testRelease.getAdminGroups())
			{
				Element authid=doc.createElement("authid");
				authid.setAttribute("reports","yes");
				Text groupId=doc.createTextNode(adminGroup);
				authid.appendChild(groupId);
				admins.appendChild(authid);
			}
		}
		else
		{
			for (User admin:testRelease.getAdmins())
			{
				Element oucu=doc.createElement("oucu");
				Text adminId=doc.createTextNode(admin.getOucu());
				oucu.appendChild(adminId);
				admins.appendChild(oucu);
			}
			for (String adminGroup:testRelease.getAdminGroups())
			{
				Element authid=doc.createElement("authid");
				Text groupId=doc.createTextNode(adminGroup);
				authid.appendChild(groupId);
				admins.appendChild(authid);
			}
		}
		access.appendChild(admins);
		
		// Add dates
		Element dates=doc.createElement("dates");
		root.appendChild(dates);
		
		// Add open to dates
		Element open=doc.createElement("open");
		dates.appendChild(open);
		Text openText=null;
		Date startDate=testRelease.getStartDate();
		if (startDate==null)
		{
			openText=doc.createTextNode("yes");
		}
		else
		{
			openText=doc.createTextNode(df.format(startDate));
		}
		open.appendChild(openText);
		Date warningDate=testRelease.getWarningDate();
		Date closeDate=testRelease.getCloseDate();
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
		Date feedbackDate=testRelease.getFeedbackDate();
		if (feedbackDate!=null)
		{
			// Add feedback to dates
			Element feedback=doc.createElement("feedback");
			dates.appendChild(feedback);
			Text feedbackText=doc.createTextNode(df.format(feedbackDate));
			feedback.appendChild(feedbackText);
		}
		
		// Add assessement (and enable email submits) if needed by assessement
		String assessementType=testRelease.getAssessement().getType();
		if ("ASSESSEMENT_REQUIRED".equals(assessementType))
		{
			Element assessement=doc.createElement("assessed");
			root.appendChild(assessement);
			Element email=doc.createElement("email");
			email.setAttribute("submit","yes");
			root.appendChild(email);
		}
		else if ("ASSESSEMENT_OPTIONAL".equals(assessementType))
		{
			Element assessement=doc.createElement("assessed");
			assessement.setAttribute("optional","yes");
			root.appendChild(assessement);
			Element email=doc.createElement("email");
			email.setAttribute("submit","yes");
			root.appendChild(email);
		}
		return doc;
	}
	
	/**
	 * Create an XML paragraph from received content.<br/><br/>
	 * Note that we try to insert received content as HTML creating needed nodes and subnodes but if it is not
	 * possible we insert it as plain content.
	 * @param doc XML document
	 * @param text Text (can be HTML or plain text)
	 * @return XML nodes to show a text with gaps
	 */
	private static Element createHtmlOrPlainParagraph(Document doc,String text)
	{
		// Create paragraph node
		Element paragraph=doc.createElement("p");
		
		// Remove ending <br/> if exists (this is a paragraph so there is already a break line at end 
		// and we don't want an extra one)
		String htmlText=text;
		if (htmlText.endsWith("<br/>"))
		{
			htmlText=htmlText.substring(0,text.length()-"<br/>".length());
		}
		
		// Add text to paragraph 
		try
		{
			// First try to insert text as HTML format
			Document htmlTextDoc=HtmlUtils.readAsHTML(htmlText);
			HtmlUtils.importHtmlSubtree(paragraph,htmlTextDoc);
		}
		catch (Exception e)
		{
			// Inserting presentation as HTML format has failed, so we insert it as plain text
			for (Node node:QuestionGenerator.createTextWithBreaks(doc,text))
			{
				paragraph.appendChild(node);
			}
		}
		return paragraph;
	}
	
	/**
	 * @param section Section
	 * @return Maximum score for a section of a test
	 */
	private static int getMaximumSectionScore(Section section)
	{
		int maximumSectionScore=0;
		if (section.isRandom())
		{
			maximumSectionScore=section.getRandomQuantity()*MAX_BASE_SINGLE_QUESTION_SCORE;
		}
		else
		{
			for (QuestionOrder questionOrder:section.getQuestionOrders())
			{
				maximumSectionScore+=questionOrder.getWeight()*MAX_BASE_SINGLE_QUESTION_SCORE;
			}
		}
		return maximumSectionScore;
	}
	
	/**
	 * @param test Test
	 * @return Maximum scores for every section of a test.
	 */
	private static Map<Section,Integer> getMaxSectionsScores(Test test)
	{
		Map<Section,Integer> maxSectionsScores=new HashMap<Section,Integer>();
		for (Section section:test.getSections())
		{
			maxSectionsScores.put(section,Integer.valueOf(getMaximumSectionScore(section)));
		}
		return maxSectionsScores;
	}
	
	/**
	 * Calculates the maximum score used for a single section with a base weight of 1 for a given test
	 * (only used with score type 'For section').
	 * @param test Test
	 * @param maxSectionsScores Maximum scores for every section of a test
	 * @return Maximum score used for a single section with a base weight of 1
	 * @throws Exception If score type of the test is not supported
	 */
	private static int getMaxBaseSingleSectionScore(Test test,Map<Section,Integer> maxSectionsScores) throws Exception
	{
		int maxBaseSingleSectionScore=0;
		if ("SCORE_TYPE_SECTIONS".equals(test.getScoreType().getType()))
		{
			for (Integer maxSectionScore:maxSectionsScores.values())
			{
				if (maxBaseSingleSectionScore<maxSectionScore.intValue())
				{
					maxBaseSingleSectionScore=maxSectionScore.intValue();
				}
			}
		}
		else if (!"SCORE_TYPE_QUESTIONS".equals(test.getScoreType().getType()))
		{
			StringBuffer error=new StringBuffer("Error. The score's type '");
			error.append(test.getScoreType().getType());
			error.append("' is not supported.");
			throw new Exception(error.toString());			
		}
		return maxBaseSingleSectionScore;
	}
	
	/**
	 * @param test Test
	 * @param maxSectionsScores Maximum scores for every section of a test
	 * @param maxBaseSingleSectionScore Maximum score used for a single section with a base weight of 1
	 * @return Maximum total score for a test
	 */
	private static int getMaximumTotalScore(Test test,Map<Section,Integer> maxSectionsScores,
		int maxBaseSingleSectionScore)
	{
		int maximumTotalScore=0;
		if ("SCORE_TYPE_QUESTIONS".equals(test.getScoreType().getType()))
		{
			for (Integer maxSectionScore:maxSectionsScores.values())
			{
				maximumTotalScore+=maxSectionScore.intValue();
			}
		}
		else if ("SCORE_TYPE_SECTIONS".equals(test.getScoreType().getType()))
		{
			for (Entry<Section,Integer> maxSectionScoreEntry:maxSectionsScores.entrySet())
			{
				maximumTotalScore+=
					maxSectionScoreEntry.getKey().getWeight()*maxSectionScoreEntry.getValue().intValue();
			}
		}
		return maximumTotalScore;
	}
	
	/**
	 * Create XML nodes with needed &lt;rescore&gt; elements for content.
	 * @param doc XML document
	 * @param test Test
	 * @param maxSectionsScores Maximum scores for every section of a test
	 * @param maxBaseSingleSectionScore Maximum score used for a single section with a base weight of 1
	 * @return XML nodes with needed &lt;rescore&gt; elements for content
	 */
	private static List<Element> createContentRescores(Document doc,Test test,Map<Section,Integer> maxSectionsScores,
		int maxBaseSingleSectionScore)
	{
		List<Element> rescores=new ArrayList<Element>();
		
		// First we add "total" <rescore> element
		Element overallRescore=doc.createElement("rescore");
		overallRescore.setAttribute(
			"marks",Integer.toString(getMaximumTotalScore(test,maxSectionsScores,maxBaseSingleSectionScore)));
		// Note that we need to use 'total' instead of 'overall' as name because 'overall' is 
		// alphabetically before 'section' and that causes test results mail reports to display overall
		// scores first... so we changed name to 'total' so it is displayed the last.
		overallRescore.setAttribute("axis","total");
		overallRescore.setAttribute("fromaxis","all");
		rescores.add(overallRescore);
		
		// Get a list of sections sorted by its order
		List<Section> sections=new ArrayList<Section>();
		for (Section section:test.getSections())
		{
			if (section!=null)
			{
				sections.add(section);
			}
		}
		Collections.sort(sections,getSectionComparatorByOrder());
		
		// Finally we add a <rescore> element for every section (only if there are several sections)
		if (sections.size()>1)
		{
			int sectionIndex=1;
			for (Section section:sections)
			{
				Element sectionRescore=doc.createElement("rescore");
				sectionRescore.setAttribute("marks",maxSectionsScores.get(section).toString());
				StringBuffer axisName=new StringBuffer("section");
				axisName.append(sectionIndex);
				sectionRescore.setAttribute("axis",axisName.toString());
				sectionRescore.setAttribute("fromaxis",axisName.toString());
				rescores.add(sectionRescore);
				
				// Increase section index
				sectionIndex++;
			}
		}
		
		return rescores;
	}
	
	/**
	 * Create XML nodes with needed sections for content.
	 * @param doc XML document
	 * @param test Test
	 * @param maxSectionsScores Maximum scores for every section of a test
	 * @param maxBaseSingleSectionScore Maximum score used for a single section with a base weight of 1
	 * @return XML nodes with needed sections for content
	 */
	private static List<Element> createContentSections(Document doc,Test test,Map<Section,Integer> maxSectionsScores,
		int maxBaseSingleSectionScore)
	{
		List<Element> contentSections=new ArrayList<Element>();
		
		// Get a list of sections sorted by its order
		List<Section> sections=new ArrayList<Section>();
		for (Section section:test.getSections())
		{
			if (section!=null)
			{
				sections.add(section);
			}
		}
		Collections.sort(sections,getSectionComparatorByOrder());
		
		// Create sections for content
		for (Section section:sections)
		{
			Element contentSection=doc.createElement("section");
			contentSections.add(contentSection);
			
			StringBuffer sectionTitle=null;
			if (section.getTitle()==null || section.getTitle().equals(""))
			{
				// If there are several sections and this section doesn't have a name or title we add a
				// default title to distinguish sections
				if (sections.size()>1 && (section.getName()==null || section.getName().equals("")))
				{
					// We create a default title with the order of section
					sectionTitle=new StringBuffer(getLocalizedString("SECTION","Section"));
					sectionTitle.append(' ');
					sectionTitle.append(section.getOrder());
				}
				// Note that it is always possible to leave the section's title empty by giving a name
				// but not a title to that section
			}
			else
			{
				// Usually we use the section's title
				sectionTitle=new StringBuffer(section.getTitle());
			}
			
			// We only add title if we have assigned it
			if (sectionTitle!=null)
			{
				Element title=doc.createElement("title");
				contentSection.appendChild(title);
				Text titleText=doc.createTextNode(sectionTitle.toString());
				title.appendChild(titleText);
			}
			
			// Set default parent element for questions (<section> element)
			Element questionsParent=contentSection;
			if (section.isShuffle())
			{
				// As this section display its questions in a random order we must add a <random> element
				// to section
				Element random=doc.createElement("random");
				contentSection.appendChild(random);
				
				// Check if we want to show only a fixed quantity of questions
				if (section.isRandom())
				{
					// Add choose attribute to show only a fixed quantity of questions
					random.setAttribute("choose",Integer.toString(section.getRandomQuantity()));
				}
				
				// Set parent element for shuffled questions (<random> element)
				questionsParent=random;
			}
			
			// Get section questions (sorted if needed)
			List<QuestionOrder> questionOrders=new ArrayList<QuestionOrder>();
			for (QuestionOrder questionOrder:section.getQuestionOrders())
			{
				if (questionOrder!=null && questionOrder.getQuestion()!=null)
				{
					questionOrders.add(questionOrder);
				}
			}
			if (!section.isShuffle())
			{
				Collections.sort(questionOrders,getQuestionOrderComparatorByOrder());
			}
			
			// Add questions to parent element for questions
			for (QuestionOrder questionOrder:questionOrders)
			{
				Element question=doc.createElement("question");
				question.setAttribute("id",questionOrder.getQuestion().getPackage());
				if (!section.isRandom() && questionOrder.getWeight()>1)
				{
					Element questionRescore=doc.createElement("rescore");
					questionRescore.setAttribute(
						"marks",Integer.toString(questionOrder.getWeight()*MAX_BASE_SINGLE_QUESTION_SCORE));
					question.appendChild(questionRescore);
				}
				questionsParent.appendChild(question);
			}
			
			// Add "all" <rescore> element to section
			Element allRescore=doc.createElement("rescore");
			if ("SCORE_TYPE_QUESTIONS".equals(test.getScoreType().getType()))
			{
				allRescore.setAttribute("marks",maxSectionsScores.get(section).toString());
			}
			else if ("SCORE_TYPE_SECTIONS".equals(test.getScoreType().getType()))
			{
				allRescore.setAttribute("marks",Integer.toString(section.getWeight()*maxBaseSingleSectionScore));
			}
			allRescore.setAttribute("axis","all");
			allRescore.setAttribute("fromaxis","");
			contentSection.appendChild(allRescore);
			
			// Add "section<n>" <rescore> element to section (only if there are several sections)
			if (sections.size()>1)
			{
				StringBuffer axisName=new StringBuffer("section");
				axisName.append(section.getOrder());
				Element sectionRescore=doc.createElement("rescore");
				sectionRescore.setAttribute("marks",maxSectionsScores.get(section).toString());
				sectionRescore.setAttribute("axis",axisName.toString());
				sectionRescore.setAttribute("fromaxis","");
				contentSection.appendChild(sectionRescore);
			}
		}
		return contentSections;
	}
	
	/**
	 * Get number of questions displayed in a test.<br/><br/>
	 * Note that it not always match with the total number of questions of the test because can exist
	 * sections that only show a fixed number of questions.
	 * @param test Test
	 * @return Number of questions displayed in a test
	 */
	/*
	private static int getNumberOfDisplayedQuestions(Test test)
	{
		int n=0;
		for (Section section:test.getSections())
		{
			if (section!=null)
			{
				n+=getNumberOfDisplayedQuestions(section);
			}
		}
		return n;
	}
	*/
	
	/**
	 * Get number of questions displayed in a section.<br/><br/>
	 * Note that it not always match with the total number of questions of the section because it can be
	 * a section that only show a fixed number of questions.
	 * @param section Section
	 * @return Number of questions displayed in a section
	 */
	/*
	private static int getNumberOfDisplayedQuestions(Section section)
	{
		int n=0;
		if (section.isShuffle() && section.isRandom())
		{
			n=section.getRandomQuantity();
		}
		else
		{
			for (QuestionOrder questionOrder:section.getQuestionOrders())
			{
				if (questionOrder!=null && questionOrder.getQuestion()!=null)
				{
					n++;
				}
			}
		}
		return n;
	}
	*/
	
	/**
	 * Get a comparator that can be used to sort a list of sections by its order.<br/><br/>
	 * Note that can't be used with lists with null sections.
	 * @return Comparator that can be used to sort a list of sections by its order
	 */
	private static Comparator<Section> getSectionComparatorByOrder()
	{
		return new Comparator<Section>()
		{
			@Override
			public int compare(Section s1,Section s2)
			{
				int compareResult=0;
				if (s1.getOrder()<s2.getOrder())
				{
					compareResult=-1;
				}
				else if (s1.getOrder()>s2.getOrder())
				{
					compareResult=1;
				}
				return compareResult;
			}
		};
	}
	
	/**
	 * Get a comparator that can be used to sort a list of instances of QuestionOrder by its order.<br/><br/>
	 * Note that can't be used with lists with null instances.
	 * @return Comparator that can be used to sort a list of instances of QuestionOrder by its order
	 */
	private static Comparator<QuestionOrder> getQuestionOrderComparatorByOrder()
	{
		return new Comparator<QuestionOrder>()
		{
			@Override
			public int compare(QuestionOrder qo1,QuestionOrder qo2)
			{
				int compareResult=0;
				if (qo1.getOrder()<qo2.getOrder())
				{
					compareResult=-1;
				}
				else if (qo1.getOrder()>qo2.getOrder())
				{
					compareResult=1;
				}
				return compareResult;
			}
		};
	}
	
	/**
	 * Create XML nodes with conditionals from advanced feedbacks.
	 * @param doc XML document
	 * @param test Test
	 * @return XML nodes with conditionals from advanced feedbacks
	 */
	private static List<Element> createFinalConditionals(Document doc,Test test)
	{
		List<Element> finalConditionals=new ArrayList<Element>();
		
		// Get a list of advanced feedbacks sorted by its position
		List<TestFeedback> advancedFeedbacks=new ArrayList<TestFeedback>();
		for (TestFeedback advancedFeedback:test.getTestFeedbacks())
		{
			if (advancedFeedback!=null)
			{
				advancedFeedbacks.add(advancedFeedback);
			}
		}
		Collections.sort(advancedFeedbacks,getAdvancedFeedbackComparatorByPosition());
		
		// Create conditionals for final from advanced feedbacks
		for (TestFeedback advancedFeedback:advancedFeedbacks)
		{
			// First we check if has been defined a content for this advanced feedback.
			// We do this check because we don't need to add a conditional for an advanced feedback without
			// content.
			if (advancedFeedback.getText()!=null && !advancedFeedback.getText().equals("") && 
				!advancedFeedback.getText().equals("<br/>"))
			{
				Element finalConditional=doc.createElement("conditional");
				finalConditionals.add(finalConditional);
				
				// Set unit checked in this conditional
				String unit=advancedFeedback.getScoreUnit().getUnit();
				if ("MARK_UNITS".equals(unit))
				{
					finalConditional.setAttribute("on","marks");
				}
				// As percentage is the default value this check is not needed
				/*
				else if ("PERCENTAGE_UNITS".equals(unit))
				{
					
				}
				*/
				else
				{
					finalConditional.setAttribute("on","percentage");
				}
				
				// Set comparisons checked in this conditional
				int minValue=advancedFeedback.getMinvalue().intValue();
				int maxValue=advancedFeedback.getMaxvalue();
				if (minValue==maxValue)
				{
					if (minValue>=0)
					{
						finalConditional.setAttribute("e",Integer.toString(minValue));
					}
					else
					{
						finalConditional.setAttribute("ne",Integer.toString(-minValue-1));
					}
				}
				else
				{
					if (minValue>0)
					{
						finalConditional.setAttribute("gte",Integer.toString(minValue));
					}
					if (maxValue<Integer.MAX_VALUE)
					{
						finalConditional.setAttribute("lte",Integer.toString(maxValue));
					}
				}
				
				// Set axis checked in this conditional
				Section section=advancedFeedback.getSection();
				if (section==null)
				{
					// Note that we need to use 'total' instead of 'overall' as name because 'overall' is 
					// alphabetically before 'section' and that causes test results mail reports to display overall
					// scores first... so we changed name to 'total' so it is displayed the last.
					finalConditional.setAttribute("axis","total");
				}
				else
				{
					StringBuffer axisName=new StringBuffer("section");
					axisName.append(section.getOrder());
					finalConditional.setAttribute("axis",axisName.toString());
				}
				
				// Add content paragraph
				Element finalConditionalParagraph=createHtmlOrPlainParagraph(doc,advancedFeedback.getText());
				finalConditional.appendChild(finalConditionalParagraph);
			}
		}
		return finalConditionals;
	}
	
	/**
	 * Get a comparator that can be used to sort a list of advanced feedbacks by its position.<br/><br/>
	 * Note that can't be used with lists with null advanced feedbacks.
	 * @return Comparator that can be used to sort a list of advanced feedbacks by its position
	 */
	private static Comparator<TestFeedback> getAdvancedFeedbackComparatorByPosition()
	{
		return new Comparator<TestFeedback>()
		{
			@Override
			public int compare(TestFeedback tf1,TestFeedback tf2)
			{
				int compareResult=0;
				if (tf1.getPosition()<tf2.getPosition())
				{
					compareResult=-1;
				}
				else if (tf1.getPosition()>tf2.getPosition())
				{
					compareResult=1;
				}
				return compareResult;
			}
		};
	}
	
	/**
	 * Creates a <i>u<b>&lt;user id&gt;</b>.t<b>&lt;test id&gt;</b>.test.xml</i> file with the generated 
	 * test XML document and uploads it to OM Test Navigator Web Application
	 * @param doc Generated test XML document
	 * @param testRelease Test release
	 * @param omTnURL OM test navigator web application URL (test or production environment) 
	 * @param publish true if we are publishing the jar to a Test Navigator production environment, 
	 * @throws Exception
	 */
	private static void createTestXmlFile(Document doc,TestRelease testRelease,String omTnURL,boolean publish) 
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
			error.append("Error. Unexpected error while configuring XML transformer to create test XML file for ");
			error.append(testRelease.getTest().getSignature());
			error.append(" test");
			throw new Exception(error.toString());
		}
		catch (IllegalArgumentException iae)
		{
			StringBuffer error=new StringBuffer();
			error.append("Error. Unexpected error while configuring XML transformer to create test XML file for ");
			error.append(testRelease.getTest().getSignature());
			error.append(" test");
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
			error.append("Error. Unexpected error has occurred during transformation to create test XML file for ");
			error.append(testRelease.getTest().getSignature());
			error.append(" test");
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
		String[] base64TestXml=new String[1];
		base64TestXml[0]=Base64.encode(baos.toByteArray());
		String signature=testRelease.getTest().getSignature();
		if (!omTnWs.uploadTestXml(
			publish?OmTnProEncryptor.encrypt(signature):OmTnEncryptor.encrypt(signature),base64TestXml))
		{
			StringBuffer error=new StringBuffer();
			error.append("Error. An error has ocurred while trying to upload test XML file for ");
			error.append(signature);
			error.append(" test to OM Test Navigator Web Application ");
			error.append(publish?"production":"test");
			error.append(" environment");
		}
	}
	
	/**
	 * Creates a <i>u<b>&lt;user id&gt;</b>.t<b>&lt;test id&gt;</b>.deploy.xml</i> file with the generated 
	 * deploy XML document and uploads it to OM Test Navigator Web Application
	 * @param doc Generated deploy XML document
	 * @param testRelease Test release
	 * @param omTnURL OM test navigator web application URL 
	 * @param publish true if we are publishing the jar to a Test Navigator production environment, 
	 * @throws Exception
	 */
	private static void createDeployXmlFile(Document doc,TestRelease testRelease,String omTnURL,boolean publish) 
		throws Exception
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
			StringBuffer error=new StringBuffer();
			error.append("Error. Unexpected error while configuring XML transformer to create deploy XML file for ");
			error.append(testRelease.getTest().getSignature());
			error.append(" test");
			throw new Exception(error.toString());
			
		}
		catch (IllegalArgumentException iae)
		{
			StringBuffer error=new StringBuffer();
			error.append("Error. Unexpected error while configuring XML transformer to create deploy XML file for ");
			error.append(testRelease.getTest().getSignature());
			error.append(" test");
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
			error.append(testRelease.getTest().getSignature());
			error.append(" test");
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
		String signature=testRelease.getTest().getSignature();
		if (!omTnWs.uploadDeployXml(publish?OmTnProEncryptor.encrypt(signature):OmTnEncryptor.encrypt(signature),
			testRelease.getVersion(),base64DeployXml))
		{
			StringBuffer error=new StringBuffer();
			error.append("Error. An error has ocurred while trying to upload deploy XML file for ");
			error.append(signature);
			error.append(" test to OM Test Navigator Web Application ");
			error.append(publish?"production":"test");
			error.append(" environment");
		}
	}
	
	/**
	 * Copy all neeeded referenced question's jar files from OM Developer web applicacion 
	 * to OM Test Navigator web application.
	 * @param test Test
	 * @param omURL OM developer web application URL
	 * @param omQeURL OM question engine web application URL (only needed for test environments)
	 * @param omTnURL OM test navigator web application URL (test or production environment)
	 * @param publish true if we are publishing the jar to a Test Navigator production environment, 
	 * @throws Exception
	 */
	public static void copyJarFiles(Test test,String omURL,String omQeURL,String omTnURL,boolean publish) 
		throws Exception
	{
		// Initialize QuestionsDao to invoke Hibernate
		QuestionsDao questionsDao=new QuestionsDao();
		
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
		
		// Copy jar files if needed
		for (Section section:test.getSections())
		{
			if (section!=null)
			{
				for (QuestionOrder questionOrder:section.getQuestionOrders())
				{
					if (questionOrder!=null && questionOrder.getQuestion()!=null)
					{
						// Get question
						Question question=
							questionsDao.getQuestion(questionOrder.getQuestion().getId(),false,false,false,false,false);
						
						// Get package's name
						String packageName=question.getPackage();
						
						// Check if we need to deploy question jar
						boolean deployQuestion=true;
						long lastTimeModified=question.getTimemodified()==null?-1:question.getTimemodified().getTime();
						long lastTimeDeploy=publish?
							(question.getTimepublished()==null?-1:question.getTimepublished().getTime()):
							(question.getTimedeploy()==null?-1:question.getTimedeploy().getTime());
						long lastTimeBuild=question.getTimebuild()==null?-1:question.getTimebuild().getTime();
						if (lastTimeDeploy!=-1 && lastTimeDeploy>lastTimeBuild && lastTimeDeploy>lastTimeModified)
						{
							try
							{
								checkQuestionJar(packageName,publish?null:"1.0",omTnURL);
								deployQuestion=lastTimeDeploy!=getQuestionJarLastModifiedDate(
									packageName,publish?null:"1.0",omTnURL).getTime();
							}
							catch (Exception e)
							{
								// Question's jar don't exists on OM Test Navigator Web Application so we ignore this exception
							}
						}
						
						// Copy jar files if we need to deploy question
						if (deployQuestion)
						{
							// Download question's jar from OM Developer Web Application
							String[] base64QuestionJar=omDevWs.downloadQuestionJar(packageName);
							
							// Upload question's jar to OM Test Navigator Web Application
							if (!omTnWs.uploadQuestionJar(
								publish?OmTnProEncryptor.encrypt(packageName):OmTnEncryptor.encrypt(packageName),
								publish?null:"1.0",base64QuestionJar))
							{
								StringBuffer error=new StringBuffer();
								error.append("Error. An error has occurred while trying to upload jar for ");
								error.append(packageName);
								error.append(" question to OM Test Navigator Web Application ");
								error.append(publish?"production":"test");
								error.append(" environment");
								throw new Exception(error.toString());
							}
							
							if (publish)
							{
								// Update published time on question
								question.setTimepublished(getQuestionJarLastModifiedDate(packageName,null,omTnURL));
							}
							else
							{
								// Stop all Test Navigator sessions using that question
								QuestionGenerator.stopAllSessionsForQuestion(packageName,omTnURL);
								
								// Delete cached question from OM Question Engine Web Application
								QuestionGenerator.deleteJarFileFromQuestionEngineCache(packageName,omQeURL);
								
								// Update deploy time on question
								question.setTimedeploy(getQuestionJarLastModifiedDate(packageName,"1.0",omTnURL));
							}
							
							// Save question
							questionsDao.updateQuestion(question);
						}
					}
				}
			}
		}
	}
	
	/**
	 * Copy all neeeded referenced question's jar files from OM Developer web applicacion 
	 * to OM Test Navigator web application.
	 * @param test Test
	 * @param omURL OM developer web application URL
	 * @param omQeURL OM question engine web application URL (only needed for test environments)
	 * @param omTnURL OM test navigator web application URL (test or production environment)
	 * @param publish true if we are publishing the jar to a Test Navigator production environment, 
	 * @throws Exception
	 */
	/*
	public static void copyJarFiles(Test test,String omURL,String omQeURL,String omTnURL,boolean publish) 
		throws Exception
	{
		copyJarFiles(null,test,omURL,omQeURL,omTnURL,publish);
	}
	*/
	
	/**
	 * Copy all neeeded referenced question's jar files from OM Developer web applicacion 
	 * to OM Test Navigator web application.
	 * @param operation Operation
	 * @param test Test
	 * @param omURL OM developer web application URL
	 * @param omQeURL OM question engine web application URL (only needed for test environments)
	 * @param omTnURL OM test navigator web application URL (test or production environment)
	 * @param publish true if we are publishing the jar to a Test Navigator production environment, 
	 * @throws Exception
	 */
	/*
	public static void copyJarFiles(Operation operation,Test test,String omURL,String omQeURL,String omTnURL,
		boolean publish) throws Exception
	{
		boolean singleOp=operation==null;
		try
		{
			if (singleOp)
			{
				// Start a new Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			// Initialize QuestionsDao to invoke Hibernate
			QuestionsDao questionsDao=new QuestionsDao();
			
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
			
			// Copy jar files if needed
			for (Section section:test.getSections())
			{
				if (section!=null)
				{
					for (QuestionOrder questionOrder:section.getQuestionOrders())
					{
						if (questionOrder!=null && questionOrder.getQuestion()!=null)
						{
							// Get question
							questionsDao.setOperation(operation);
							Question question=questionsDao.getQuestion(
								questionOrder.getQuestion().getId(),false,false,false,false,false);
							
							// Get package's name
							String packageName=question.getPackage();
							
							// Check if we need to deploy question jar
							boolean deployQuestion=true;
							long lastTimeModified=
								question.getTimemodified()==null?-1:question.getTimemodified().getTime();
							long lastTimeDeploy=publish?
								(question.getTimepublished()==null?-1:question.getTimepublished().getTime()):
								(question.getTimedeploy()==null?-1:question.getTimedeploy().getTime());
							long lastTimeBuild=question.getTimebuild()==null?-1:question.getTimebuild().getTime();
							if (lastTimeDeploy!=-1 && lastTimeDeploy>lastTimeBuild && lastTimeDeploy>lastTimeModified)
							{
								try
								{
									checkQuestionJar(packageName,publish?null:"1.0",omTnURL);
									deployQuestion=lastTimeDeploy!=getQuestionJarLastModifiedDate(
										packageName,publish?null:"1.0",omTnURL).getTime();
								}
								catch (Exception e)
								{
									// Question's jar don't exists on OM Test Navigator Web Application 
									// so we ignore this exception
								}
							}
							
							// Copy jar files if we need to deploy question
							if (deployQuestion)
							{
								// Download question's jar from OM Developer Web Application
								String[] base64QuestionJar=omDevWs.downloadQuestionJar(packageName);
								
								// Upload question's jar to OM Test Navigator Web Application
								if (!omTnWs.uploadQuestionJar(
									publish?OmTnProEncryptor.encrypt(packageName):OmTnEncryptor.encrypt(packageName),
									publish?null:"1.0",base64QuestionJar))
								{
									StringBuffer error=new StringBuffer();
									error.append("Error. An error has occurred while trying to upload jar for ");
									error.append(packageName);
									error.append(" question to OM Test Navigator Web Application ");
									error.append(publish?"production":"test");
									error.append(" environment");
									throw new Exception(error.toString());
								}
								
								if (publish)
								{
									// Update published time on question
									question.setTimepublished(getQuestionJarLastModifiedDate(packageName,null,omTnURL));
								}
								else
								{
									// Stop all Test Navigator sessions using that question
									QuestionGenerator.stopAllSessionsForQuestion(packageName,omTnURL);
									
									// Delete cached question from OM Question Engine Web Application
									QuestionGenerator.deleteJarFileFromQuestionEngineCache(packageName,omQeURL);
									
									// Update deploy time on question
									question.setTimedeploy(getQuestionJarLastModifiedDate(packageName,"1.0",omTnURL));
								}
								
								// Save question
								questionsDao.setOperation(operation);
								questionsDao.updateQuestion(question);
							}
						}
					}
				}
			}
			
			if (singleOp)
			{
				// Do commit
				operation.commit();
			}
		}
		catch (HibernateException he)
		{
			if (singleOp)
			{
				// Do rollback
				operation.rollback();
			}
			
			throw he;
		}
		finally
		{
			if (singleOp)
			{
				// End Hibernate operation
				HibernateUtil.endOperation(operation);
			}
		}
	}
	*/
	
	/**
	 * Delete <i>u<b>&lt;user id&gt;</b>.t<b>&lt;test id&gt;</b>.test.xml</i> and 
	 * <i>u<b>&lt;user id&gt;</b>.t<b>&lt;test id&gt;</b>.deploy.xml</i> files from the 
	 * <i>testbank</i> folder of test navigator web application
	 * @param signature Test's signature
	 * @param omTnURL OM test navigator web application URL
	 * @param unpublish true if we are deleting a test release from a Test Navigator production environment,
	 * false if we are deleting a test from a Test Navigator test environment
	 */
	private static void deleteXmlFiles(String signature,int version,String omTnURL,boolean unpublish) 
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
		omTnWs.deleteTestXmls(
			unpublish?OmTnProEncryptor.encrypt(signature):OmTnEncryptor.encrypt(signature),version);
		
	}
	
	/**
	 * Delete <i>u<b>&lt;user id&gt;</b>.t<b>&lt;test id&gt;</b>.test.xml</i> and 
	 * <i>u<b>&lt;user id&gt;</b>.t<b>&lt;test id&gt;</b>.deploy.xml</i> files from the 
	 * <i>testbank</i> folder of test navigator web application Test Environment
	 * @param test Test
	 * @param omTnURL OM test navigator web application test environment URL
	 */
	private static void deleteXmlFiles(Test test,String omTnURL) throws Exception
	{
		deleteXmlFiles(test.getSignature(),0,omTnURL,false);
	}
	
	/**
	 * Delete <i>u<b>&lt;user id&gt;</b>.t<b>&lt;test id&gt;</b>.test.xml</i> and 
	 * <i>u<b>&lt;user id&gt;</b>.t<b>&lt;test id&gt;</b>.deploy.xml</i> files from the 
	 * <i>testbank</i> folder of test navigator web application Test Environment
	 * @param testRelease Test release
	 * @param omTnProURL OM test navigator web application production environment URL
	 */
	private static void deleteXmlFiles(TestRelease testRelease,String omTnProURL) throws Exception
	{
		deleteXmlFiles(testRelease.getTest().getSignature(),testRelease.getVersion(),omTnProURL,true);
	}
	
	/**
	 * Delete all referenced question's jar files not used in other GePEQ tests from the <i>questionbank</i> 
	 * folder of the OM test navigator web application.
	 * <br/><br/>
	 * <b>IMPORTANT:</b> Be careful because only tests created with GePEQ will be checked, so jar files 
	 * still used by other tests can be deleted.
	 * @param test Test
	 * @param omTnURL OM test navigator web application URL 
	 */
	private static void deleteJarFiles(Test test,String omTnURL) throws Exception
	{
		StringBuffer omTnWsURL=new StringBuffer(omTnURL);
		if (omTnURL.charAt(omTnURL.length()-1)!='/')
		{
			omTnWsURL.append('/');
		}
		omTnWsURL.append("services/OmTn");
		OmTnProxy omTnWs=new OmTnProxy();
		omTnWs.setEndpoint(omTnWsURL.toString());
		
		// Initialize TestsDao, SectionsDao, QuestionOrdersDao and QuestionsDao to invoke Hibernate
		TestsDao testsDao=new TestsDao();
		SectionsDao sectionsDao=new SectionsDao();
		QuestionOrdersDao questionOrdersDao=new QuestionOrdersDao();
		QuestionsDao questionsDao=new QuestionsDao();
		
		// Get a list of all question's identifiers used by all deployed tests
		List<Long> allQuestionsIds=new ArrayList<Long>();
		for (Test t:testsDao.getTests(false,false,false,false,false,false))
		{
			boolean deployed=false;
			try
			{
				checkTestNavigatorXmls(new TestRelease(test,0,null),omTnURL);
			}
			catch (Exception e)
			{
				deployed=true;
			}
			if (deployed)
			{
				for (Section section:sectionsDao.getSections(t.getId()))
				{
					if (section!=null)
					{
						for (QuestionOrder questionOrder:
							questionOrdersDao.getQuestionOrders(section.getId(),false,false))
						{
							if (questionOrder!=null && questionOrder.getQuestion()!=null)
							{
								Long questionId=new Long(questionOrder.getQuestion().getId());
								if (!allQuestionsIds.contains(questionId))
								{
									allQuestionsIds.add(questionId);
								}
							}
						}
					}
				}
			}
		}
		
		// Now we delete referenced question's jar files not used in other GePEQ tests
		// (we achieve that by testing that the question's id is not present in the previous constructed list 
		// and also checking that the question is not deployed)
		for (Section section:test.getSections())
		{
			if (section!=null)
			{
				for (QuestionOrder questionOrder:section.getQuestionOrders())
				{
					if (questionOrder!=null && questionOrder.getQuestion()!=null)
					{
						Question question=
							questionsDao.getQuestion(questionOrder.getQuestion().getId(),false,false,false,false,false);
						Long questionId=new Long(question.getId());
						if (!allQuestionsIds.contains(questionId))
						{
							String packageName=question.getPackage();
							boolean deployed=false;
							try
							{
								QuestionGenerator.checkTestNavigatorXml(packageName,omTnURL);
							}
							catch (Exception e)
							{
								deployed=true;
							}
							if (!deployed)
							{
								omTnWs.deleteQuestionJar(OmTnEncryptor.encrypt(packageName));
							}
							allQuestionsIds.add(questionId);
						}
					}
				}
			}
		}
	}
	
	/**
	 * @param packageName Package's name
	 * @param version Version or null to get date from last version jar
	 * @param omTnURL OM Test Navigator web application URL 
	 * @return Question's jar last modified date
	 * @throws Exception
	 */
	public static Date getQuestionJarLastModifiedDate(String packageName,String version,String omTnURL) 
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
		return new Timestamp(omTnWs.getQuestionJarLastModified(packageName,version));
	}
	
	/**
	 * @param testName Test's name
	 * @param omTnURL OM Test Navigator web application URL 
	 * @return Test xml file last modified date
	 * @throws Exception
	 */
	public static Date getTestXmlLastModifiedDate(String testName,String omTnURL) throws Exception
	{
		StringBuffer omTnWsURL=new StringBuffer(omTnURL);
		if (omTnURL.charAt(omTnURL.length()-1)!='/')
		{
			omTnWsURL.append('/');
		}
		omTnWsURL.append("services/OmTn");
		OmTnProxy omTnWs=new OmTnProxy();
		omTnWs.setEndpoint(omTnWsURL.toString());
		return new Timestamp(omTnWs.getTestXmlLastModified(testName));
	}
	
	/**
	 * @param testName Test's name
	 * @param omTnURL OM Test Navigator web application URL 
	 * @return Deploy xml file last modified date
	 * @throws Exception
	 */
	public static Date getDeployXmlLastModifiedDate(String testName,String omTnURL) throws Exception
	{
		StringBuffer omTnWsURL=new StringBuffer(omTnURL);
		if (omTnURL.charAt(omTnURL.length()-1)!='/')
		{
			omTnWsURL.append('/');
		}
		omTnWsURL.append("services/OmTn");
		OmTnProxy omTnWs=new OmTnProxy();
		omTnWs.setEndpoint(omTnWsURL.toString());
		//TODO de momento no le añadimos version al archivo de deploy, es decir le pasamos version==0
		return new Timestamp(omTnWs.getDeployXmlLastModified(testName,0));
	}
}
