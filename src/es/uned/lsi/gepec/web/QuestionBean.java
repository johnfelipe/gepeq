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
package es.uned.lsi.gepec.web;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.faces.FacesException;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIComponent;
import javax.faces.component.UIData;
import javax.faces.component.UIInput;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.AjaxBehaviorEvent;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.primefaces.component.accordionpanel.AccordionPanel;
import org.primefaces.component.selectbooleancheckbox.SelectBooleanCheckbox;
import org.primefaces.component.spinner.Spinner;
import org.primefaces.component.tabview.TabView;
import org.primefaces.component.wizard.Wizard;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FlowEvent;
import org.primefaces.event.TabChangeEvent;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import com.sun.org.apache.xerces.internal.impl.Constants;

import es.uned.lsi.gepec.model.entities.Answer;
import es.uned.lsi.gepec.model.entities.Category;
import es.uned.lsi.gepec.model.entities.Copyright;
import es.uned.lsi.gepec.model.entities.DragDropAnswer;
import es.uned.lsi.gepec.model.entities.DragDropQuestion;
import es.uned.lsi.gepec.model.entities.Feedback;
import es.uned.lsi.gepec.model.entities.FeedbackType;
import es.uned.lsi.gepec.model.entities.MultichoiceQuestion;
import es.uned.lsi.gepec.model.entities.OmXmlQuestion;
import es.uned.lsi.gepec.model.entities.Question;
import es.uned.lsi.gepec.model.entities.QuestionResource;
import es.uned.lsi.gepec.model.entities.Resource;
import es.uned.lsi.gepec.model.entities.TrueFalseQuestion;
import es.uned.lsi.gepec.model.entities.User;
import es.uned.lsi.gepec.model.entities.Visibility;
import es.uned.lsi.gepec.util.HibernateUtil;
import es.uned.lsi.gepec.util.HtmlUtils;
import es.uned.lsi.gepec.util.HibernateUtil.Operation;
import es.uned.lsi.gepec.util.SAXLocalizationErrorReporter;
import es.uned.lsi.gepec.util.StringUtils;
import es.uned.lsi.gepec.web.backbeans.AnswerConditionBean;
import es.uned.lsi.gepec.web.backbeans.AttemptsConditionBean;
import es.uned.lsi.gepec.web.backbeans.ConditionBean;
import es.uned.lsi.gepec.web.backbeans.FeedbackBean;
import es.uned.lsi.gepec.web.backbeans.RightDistanceConditionBean;
import es.uned.lsi.gepec.web.backbeans.SelectedAnswersConditionBean;
import es.uned.lsi.gepec.web.backbeans.SelectedRightAnswersConditionBean;
import es.uned.lsi.gepec.web.backbeans.SelectedWrongAnswersConditionBean;
import es.uned.lsi.gepec.web.backbeans.SingleAnswerConditionBean;
import es.uned.lsi.gepec.web.backbeans.SingleDragDropAnswerConditionBean;
import es.uned.lsi.gepec.web.backbeans.TestConditionBean;
import es.uned.lsi.gepec.web.backbeans.UnselectedAnswersConditionBean;
import es.uned.lsi.gepec.web.backbeans.UnselectedRightAnswersConditionBean;
import es.uned.lsi.gepec.web.backbeans.UnselectedWrongAnswersConditionBean;
import es.uned.lsi.gepec.web.helper.NumberComparator;
import es.uned.lsi.gepec.web.services.AnswersService;
import es.uned.lsi.gepec.web.services.CategoriesService;
import es.uned.lsi.gepec.web.services.CategoryTypesService;
import es.uned.lsi.gepec.web.services.ConfigurationService;
import es.uned.lsi.gepec.web.services.CopyrightsService;
import es.uned.lsi.gepec.web.services.FeedbackTypesService;
import es.uned.lsi.gepec.web.services.LocalizationService;
import es.uned.lsi.gepec.web.services.PermissionsService;
import es.uned.lsi.gepec.web.services.QuestionResourcesService;
import es.uned.lsi.gepec.web.services.QuestionsService;
import es.uned.lsi.gepec.web.services.ResourcesService;
import es.uned.lsi.gepec.web.services.ServiceException;
import es.uned.lsi.gepec.web.services.UserSessionService;
import es.uned.lsi.gepec.web.services.VisibilitiesService;

//Backbean para la vista pregunta
//author Víctor Manuel Alonso Rodríguez
//since  12/2011
/**
 * Managed bean for creating/updating a question.
 */
@SuppressWarnings("serial")
@ManagedBean(name="questionBean")
@ViewScoped
public class QuestionBean implements Serializable
{
	private final static class SpecialCategoryFilter
	{
		private long id;
		private String name;
		private List<String> requiredPermissions;
		private List<String> requiredAuthorPermissions;
		
		private SpecialCategoryFilter(long id,String name,List<String> requiredPermissions,
			List<String> requiredAuthorPermissions)
		{
			this.id=id;
			this.name=name;
			this.requiredPermissions=requiredPermissions;
			this.requiredAuthorPermissions=requiredAuthorPermissions;
		}
		
		@Override
		public boolean equals(Object obj)
		{
			return obj instanceof SpecialCategoryFilter && id==((SpecialCategoryFilter)obj).id;
		}
	}
	
	private final static String GENERAL_WIZARD_TAB="general";
	private final static String ANSWERS_WIZARD_TAB="answers";
	private final static String RESOURCES_WIZARD_TAB="resources";
	private final static String FEEDBACK_WIZARD_TAB="feedback";
	//private final static String CONFIRMATION_WIZARD_TAB="confirmation";
	
	private final static int GENERAL_TABVIEW_TAB=0;
	private final static int ANSWERS_TABVIEW_TAB=1;
	private final static int RESOURCES_OR_FEEDBACK_TABVIEW_TAB=2;
	
	private final static int CORRECT_FEEDBACK_TAB=0;
	private final static int INCORRECT_FEEDBACK_TAB=1;
	private final static int PASS_FEEDBACK_TAB=2;
	private final static int FINAL_FEEDBACK_TAB=3;
	
	private final static int MAX_IMAGE_WIDTH_FOR_PROPERTIES_DIALOG=400;
	private final static int MAX_IMAGE_HEIGHT_FOR_PROPERTIES_DIALOG=300;
	
	private final static int DEFAULT_MAX_RESOURCE_WIDTH=280;
	private final static double TOLERANCE=1E-3;
	
	private final static String QUESTION_XML_TEMPLATE_PATH="WEB-INF/omtemplates/question.xml";
	
	/** Number comparators for question's feedbacks */
	private final static List<String> QUESTION_NUMBER_COMPARATORS;
	static
	{
		QUESTION_NUMBER_COMPARATORS=new ArrayList<String>();
		for (String comparator:NumberComparator.COMPARATORS)
		{
			if (!NumberComparator.compareU(comparator,NumberComparator.NOT_EQUAL))
			{
				QUESTION_NUMBER_COMPARATORS.add(comparator);
			}
		}
	}
	
	private final static List<String> QUESTION_NUMBER_COMPARATORS_F;
	static
	{
		QUESTION_NUMBER_COMPARATORS_F=new ArrayList<String>();
		for (String comparator:NumberComparator.COMPARATORS_F)
		{
			if (!NumberComparator.compareU(comparator,NumberComparator.NOT_EQUAL))
			{
				QUESTION_NUMBER_COMPARATORS_F.add(comparator);
			}
		}
	}
	
	private final static Map<String, Boolean> DEFAULT_QUESTION_NUMBER_COMPARATORS_GEN;
	static
	{
		DEFAULT_QUESTION_NUMBER_COMPARATORS_GEN=new HashMap<String,Boolean>();
		DEFAULT_QUESTION_NUMBER_COMPARATORS_GEN.put("CONDITION_TYPE_ATTEMPTS_GEN",Boolean.FALSE);
		DEFAULT_QUESTION_NUMBER_COMPARATORS_GEN.put("CONDITION_TYPE_SELECTED_ANSWERS_GEN",Boolean.FALSE);
		DEFAULT_QUESTION_NUMBER_COMPARATORS_GEN.put("CONDITION_TYPE_SELECTED_RIGHT_ANSWERS_GEN",Boolean.FALSE);
		DEFAULT_QUESTION_NUMBER_COMPARATORS_GEN.put("CONDITION_TYPE_SELECTED_WRONG_ANSWERS_GEN",Boolean.FALSE);
		DEFAULT_QUESTION_NUMBER_COMPARATORS_GEN.put("CONDITION_TYPE_UNSELECTED_ANSWERS_GEN",Boolean.FALSE);
		DEFAULT_QUESTION_NUMBER_COMPARATORS_GEN.put("CONDITION_TYPE_UNSELECTED_RIGHT_ANSWERS_GEN",Boolean.FALSE);
		DEFAULT_QUESTION_NUMBER_COMPARATORS_GEN.put("CONDITION_TYPE_UNSELECTED_WRONG_ANSWERS_GEN",Boolean.FALSE);
		DEFAULT_QUESTION_NUMBER_COMPARATORS_GEN.put("CONDITION_TYPE_RIGHT_DISTANCE_GEN",Boolean.TRUE);
	}
	
	private final static Map<String,String> MIME_TYPES_MASKS;
	static
	{
		MIME_TYPES_MASKS=new HashMap<String,String>();
		MIME_TYPES_MASKS.put("IMAGES_MIME","image/*");
	}
	
	private final static SpecialCategoryFilter ALL_EVEN_PRIVATE_CATEGORIES;
	static
	{
		List<String> allEvenPrivateCategoriesPermissions=new ArrayList<String>();
		allEvenPrivateCategoriesPermissions.add("PERMISSION_RESOURCES_GLOBAL_FILTER_ENABLED");
		allEvenPrivateCategoriesPermissions.add("PERMISSION_RESOURCES_OTHER_USERS_FILTER_ENABLED");
		allEvenPrivateCategoriesPermissions.add(
			"PERMISSION_RESOURCES_VIEW_RESOURCES_OF_OTHER_USERS_PRIVATE_CATEGORIES_ENABLED");
		List<String> allEvenPrivateCategoriesAuthorPermissions=new ArrayList<String>();
		allEvenPrivateCategoriesAuthorPermissions.add("PERMISSION_QUESTION_USE_GLOBAL_RESOURCES");
		allEvenPrivateCategoriesAuthorPermissions.add("PERMISSION_QUESTION_USE_OTHER_USERS_RESOURCES");
		ALL_EVEN_PRIVATE_CATEGORIES=new SpecialCategoryFilter(-1L,"ALL_EVEN_PRIVATE_CATEGORIES",
			allEvenPrivateCategoriesPermissions,allEvenPrivateCategoriesAuthorPermissions);
	}
	
	private final static SpecialCategoryFilter ALL_MY_CATEGORIES;
	static
	{
		List<String> allMyCategoriesPermissions=new ArrayList<String>();
		allMyCategoriesPermissions.add("PERMISSION_RESOURCES_GLOBAL_FILTER_ENABLED");
		List<String> allMyCategoriesAuthorPermissions=new ArrayList<String>();
		allMyCategoriesAuthorPermissions.add("PERMISSION_QUESTION_USE_GLOBAL_RESOURCES");
		allMyCategoriesAuthorPermissions.add("PERMISSION_QUESTION_USE_OTHER_USERS_RESOURCES");
		ALL_MY_CATEGORIES=new SpecialCategoryFilter(
			-2L,"ALL_MY_CATEGORIES",allMyCategoriesPermissions,allMyCategoriesAuthorPermissions);
	}
	
	private final static SpecialCategoryFilter ALL_MY_CATEGORIES_FOR_QUESTION_AUTHOR;
	static
	{
		List<String> allMyCategoriesForQuestionAuthorPermissions=new ArrayList<String>();
		allMyCategoriesForQuestionAuthorPermissions.add("PERMISSION_RESOURCES_GLOBAL_FILTER_ENABLED");
		List<String> allMyCategoriesForQuestionAuthorAuthorPermissions=new ArrayList<String>();
		allMyCategoriesForQuestionAuthorAuthorPermissions.add("PERMISSION_QUESTION_USE_GLOBAL_RESOURCES");
		ALL_MY_CATEGORIES_FOR_QUESTION_AUTHOR=new SpecialCategoryFilter(-2L,"ALL_MY_CATEGORIES",
			allMyCategoriesForQuestionAuthorPermissions,allMyCategoriesForQuestionAuthorAuthorPermissions);
	}
	
	private final static SpecialCategoryFilter ALL_MY_CATEGORIES_EXCEPT_GLOBALS;
	static
	{
		List<String> allMyCategoriesExceptGlobalsAuthorPermissions=new ArrayList<String>();
		allMyCategoriesExceptGlobalsAuthorPermissions.add("PERMISSION_QUESTION_USE_OTHER_USERS_RESOURCES");
		ALL_MY_CATEGORIES_EXCEPT_GLOBALS=new SpecialCategoryFilter(-3L,"ALL_MY_CATEGORIES_EXCEPT_GLOBALS",
			new ArrayList<String>(),allMyCategoriesExceptGlobalsAuthorPermissions);
	}
	
	private final static SpecialCategoryFilter ALL_MY_CATEGORIES_EXCEPT_GLOBALS_FOR_QUESTION_AUTHOR=
		new SpecialCategoryFilter(-3L,"ALL_MY_CATEGORIES_EXCEPT_GLOBALS",new ArrayList<String>(),
		new ArrayList<String>());
	
	private final static SpecialCategoryFilter ALL_QUESTION_AUTHOR_CATEGORIES;
	static
	{
		List<String> allQuestionAuthorCategoriesPermissions=new ArrayList<String>();
		allQuestionAuthorCategoriesPermissions.add("PERMISSION_RESOURCES_GLOBAL_FILTER_ENABLED");
		allQuestionAuthorCategoriesPermissions.add("PERMISSION_RESOURCES_OTHER_USERS_FILTER_ENABLED");
		List<String> allQuestionAuthorCategoriesAuthorPermissions=new ArrayList<String>();
		allQuestionAuthorCategoriesAuthorPermissions.add("PERMISSION_QUESTION_USE_GLOBAL_RESOURCES");
		ALL_QUESTION_AUTHOR_CATEGORIES=new SpecialCategoryFilter(-4L,"ALL_CATEGORIES_OF",
			allQuestionAuthorCategoriesPermissions,allQuestionAuthorCategoriesAuthorPermissions);
	}
	
	private final static SpecialCategoryFilter ALL_QUESTION_AUTHOR_CATEGORIES_EXCEPT_GLOBALS;
	static
	{
		List<String> allQuestionAuthorCategoriesExceptGlobalsPermissions=new ArrayList<String>();
		allQuestionAuthorCategoriesExceptGlobalsPermissions.add("PERMISSION_RESOURCES_OTHER_USERS_FILTER_ENABLED");
		ALL_QUESTION_AUTHOR_CATEGORIES_EXCEPT_GLOBALS=new SpecialCategoryFilter(-5L,
			"ALL_CATEGORIES_OF_EXCEPT_GLOBALS",allQuestionAuthorCategoriesExceptGlobalsPermissions,
			new ArrayList<String>());
	}
	
	private final static SpecialCategoryFilter ALL_GLOBAL_CATEGORIES;
	static
	{
		List<String> allGlobalCategoriesPermissions=new ArrayList<String>();
		allGlobalCategoriesPermissions.add("PERMISSION_RESOURCES_GLOBAL_FILTER_ENABLED");
		List<String> allGlobalCategoriesAuthorPermissions=new ArrayList<String>();
		allGlobalCategoriesAuthorPermissions.add("PERMISSION_QUESTION_USE_GLOBAL_RESOURCES");
		ALL_GLOBAL_CATEGORIES=new SpecialCategoryFilter(
			-6L,"ALL_GLOBAL_CATEGORIES",allGlobalCategoriesPermissions,allGlobalCategoriesAuthorPermissions);
	}
	
	private final static SpecialCategoryFilter ALL_PUBLIC_CATEGORIES_OF_OTHER_USERS; 
	static
	{
		List<String> allPublicCategoriesOfOtherUsersPermissions=new ArrayList<String>();
		allPublicCategoriesOfOtherUsersPermissions.add("PERMISSION_RESOURCES_OTHER_USERS_FILTER_ENABLED");
		List<String> allPublicCategoriesOfOtherUsersAuthorPermissions=new ArrayList<String>();
		allPublicCategoriesOfOtherUsersAuthorPermissions.add("PERMISSION_QUESTION_USE_OTHER_USERS_RESOURCES");
		ALL_PUBLIC_CATEGORIES_OF_OTHER_USERS=new SpecialCategoryFilter(-7L,"ALL_PUBLIC_CATEGORIES_OF_OTHER_USERS",
			allPublicCategoriesOfOtherUsersPermissions,allPublicCategoriesOfOtherUsersAuthorPermissions);
	}
	
	private final static SpecialCategoryFilter ALL_PRIVATE_CATEGORIES_OF_OTHER_USERS;
	static
	{
		List<String> allPrivateCategoriesOfOtherUsersPermissions=new ArrayList<String>();
		allPrivateCategoriesOfOtherUsersPermissions.add("PERMISSION_RESOURCES_OTHER_USERS_FILTER_ENABLED");
		allPrivateCategoriesOfOtherUsersPermissions.add(
			"PERMISSION_RESOURCES_VIEW_RESOURCES_OF_OTHER_USERS_PRIVATE_CATEGORIES_ENABLED");
		List<String> allPrivateCategoriesOfOtherUsersAuthorPermissions=new ArrayList<String>();
		allPrivateCategoriesOfOtherUsersAuthorPermissions.add("PERMISSION_QUESTION_USE_OTHER_USERS_RESOURCES");
		ALL_PRIVATE_CATEGORIES_OF_OTHER_USERS=new SpecialCategoryFilter(-8L,
			"ALL_PRIVATE_CATEGORIES_OF_OTHER_USERS",allPrivateCategoriesOfOtherUsersPermissions,
			allPrivateCategoriesOfOtherUsersAuthorPermissions);
	}
	
	private final static SpecialCategoryFilter ALL_CATEGORIES_OF_OTHER_USERS;
	static
	{
		List<String> allCategoriesOfOtherUsersPermissions=new ArrayList<String>();
		allCategoriesOfOtherUsersPermissions.add("PERMISSION_RESOURCES_OTHER_USERS_FILTER_ENABLED");
		allCategoriesOfOtherUsersPermissions.add(
			"PERMISSION_RESOURCES_VIEW_RESOURCES_OF_OTHER_USERS_PRIVATE_CATEGORIES_ENABLED");
		List<String> allCategoriesOfOtherUsersAuthorPermissions=new ArrayList<String>();
		allCategoriesOfOtherUsersAuthorPermissions.add("PERMISSION_QUESTION_USE_OTHER_USERS_RESOURCES");
		ALL_CATEGORIES_OF_OTHER_USERS=new SpecialCategoryFilter(-9L,"ALL_CATEGORIES_OF_OTHER_USERS",
			allCategoriesOfOtherUsersPermissions,allCategoriesOfOtherUsersAuthorPermissions);
	}
	
	@ManagedProperty(value="#{localizationService}")
	private LocalizationService localizationService;
	@ManagedProperty(value="#{configurationService}")
	private ConfigurationService configurationService;
	@ManagedProperty(value="#{questionsService}")
	private QuestionsService questionsService;
	@ManagedProperty(value="#{answersService}")
	private AnswersService answersService;
	@ManagedProperty(value="#{questionResourcesService}")
	private QuestionResourcesService questionResourcesService;
	@ManagedProperty(value="#{categoriesService}")
	private CategoriesService categoriesService;
	@ManagedProperty(value="#{categoryTypesService}")
	private CategoryTypesService categoryTypesService;
	@ManagedProperty(value="#{visibilitiesService}")
	private VisibilitiesService visibilitiesService;
	@ManagedProperty(value="#{resourcesService}")
	private ResourcesService resourcesService;
	@ManagedProperty(value="#{copyrightsService}")
	private CopyrightsService copyrightsService;
	@ManagedProperty(value="#{feedbackTypesService}")
	private FeedbackTypesService feedbackTypesService;
	@ManagedProperty(value="#{userSessionService}")
	private UserSessionService userSessionService;
	@ManagedProperty(value="#{permissionsService}")
	private PermissionsService permissionsService;
	
	private Question question;					// Current question
	
	private long categoryId;					// Category identifier
	private Category category;					// Caches selected category (to decrease DB queries)
	
	private long initialCategoryId;				// Initial category identifier
	
	// Local list needed to allow sorting in 'answersAccordion' accordion
	private List<Answer> answersSorting;
	
	// Local list needed to allow sorting in 'draggableItemsAccordion' accordion for a "Drag & Drop" question
	private List<Answer> draggableItemsSorting;
	
	// Local list needed to allow sorting in 'answersAccordion' accordion for a "Drag & Drop" question
	private List<Answer> droppableAnswersSorting;
	
	// Local list needed to allow sorting in 'questionResourcesAccordion' accordion
	private List<QuestionResource> questionResourcesSorting;
	
	// Local lists needed to allow sorting in 'feedbacks' datatable
	private List<FeedbackBean> feedbacks;
	private List<FeedbackBean> feedbacksSorting;
	
	/* UI Helper Properties */
	
	private String cancelQuestionTarget;
	
	private int resourcesTabviewTab;
	private int feedbackTabviewTab;
	
	private String activeQuestionTabName;
	private int activeQuestionTabIndex;
	
	private String resourceNameToAbbreviate;
	private String abbreviatedResourceName;
	
	// Select Image Dialog
	private SpecialCategoryFilter allCategories;
	private Map<Long,SpecialCategoryFilter> specialCategoryFiltersMap;
	
	private long filterCategoryId;
	private boolean filterIncludeSubcategories;
	private String filterMimeType;
	private List<String> mimeTypes;
	private List<String> mimeTypesMasks;
	private long filterCopyrightId;
	
	private String buttonId;
	private Answer currentAnswer;
	private Answer currentDraggableItem;
	private QuestionResource currentQuestionResource;
	private List<Resource> images;
	private String confirmDeleteDraggableItemMessage;
	private String confirmChangePropertyMessage;
	
	private List<String> testConditions;
	
	// Resource
	private Resource currentResource;
	private boolean currentResourceKeepAspectRatio;
	private double currentResourceAspectRatio;
	private int currentResourceWidth;
	private int currentResourceHeight;
	
	private Resource lastResource;
	
	// Answers
	private int activeAnswerIndex;
	private String activeAnswerName;
	private int answerToRemovePosition;
	private String propertyChecked;
	private Answer answerChecked;
	
	// True & False answers
	private String defaultTrueText;
	private String defaultFalseText;
	
	// Drag & Drop answers
	private int activeDraggableItemIndex;
	private String activeDraggableItemName;
	private int draggableItemToRemoveGroup;
	private int draggableItemToRemovePosition;
	private int answerToRemoveGroup;
	
	// Additional resources
	private int activeQuestionResourceIndex;
	private String activeQuestionResourceName;
	
	// Feedbacks
	private int activeFeedbackTabIndex;
	
	// Advanced feedbacks
	private FeedbackBean currentFeedback;
	private String conditionType;
	private int activeConditionIndex;
	
	// Available categories
	private List<Category> questionsCategories;
	private List<Category> specialCategoriesFilters;					// Special categories filters list
	private List<Category> imagesCategories;
	
	// Copyrights
	private List<Copyright> copyrights;
	
	private Boolean filterGlobalQuestionsEnabled;
	private Boolean filterOtherUsersQuestionsEnabled;
	private Boolean globalOtherUserCategoryAllowed;
	private Boolean addEnabled;
	private Boolean editEnabled;
	private Boolean editOtherUsersQuestionsEnabled;
	private Boolean editAdminsQuestionsEnabled;
	private Boolean editSuperadminsQuestionsEnabled;
	private Boolean viewQuestionsFromOtherUsersPrivateCategoriesEnabled;
	private Boolean viewQuestionsFromAdminsPrivateCategoriesEnabled;
	private Boolean viewQuestionsFromSuperadminsPrivateCategoriesEnabled;
	
	private Boolean filterGlobalResourcesEnabled;
	private Boolean filterOtherUsersResourcesEnabled;
	private Boolean useGlobalResources;
	private Boolean useOtherUsersResources;
	private Boolean viewResourcesFromOtherUsersPrivateCategoriesEnabled;
	private Boolean viewResourcesFromAdminsPrivateCategoriesEnabled;
	private Boolean viewResourcesFromSuperadminsPrivateCategoriesEnabled;
	
	private Map<Long,Boolean> admins;
	private Map<Long,Boolean> superadmins;
	
	public QuestionBean()
	{
		filterCategoryId=Long.MIN_VALUE;
		filterIncludeSubcategories=false;
		filterMimeType=null;
		mimeTypes=null;
		mimeTypesMasks=null;
		filterCopyrightId=0L;
		images=new ArrayList<Resource>();
		categoryId=0L;
		category=null;
		initialCategoryId=0L;
		feedbacks=null;
		currentResource=null;
		currentFeedback=null;
		conditionType=null;
		activeAnswerIndex=-1;
		activeAnswerName="";
		defaultTrueText=null;
		defaultFalseText=null;
		activeDraggableItemIndex=-1;
		activeDraggableItemName="";
		activeQuestionResourceIndex=-1;
		activeQuestionResourceName="";
		resourceNameToAbbreviate=null;
		activeConditionIndex=-1;
		answersSorting=new ArrayList<Answer>();
		draggableItemsSorting=new ArrayList<Answer>();
		droppableAnswersSorting=new ArrayList<Answer>(); //???
		questionResourcesSorting=new ArrayList<QuestionResource>();
		resourcesTabviewTab=-1;
		feedbackTabviewTab=-1;
		activeQuestionTabName=GENERAL_WIZARD_TAB;
		activeQuestionTabIndex=GENERAL_TABVIEW_TAB;
		activeFeedbackTabIndex=CORRECT_FEEDBACK_TAB;
		questionsCategories=null;
		specialCategoriesFilters=null;
		imagesCategories=null;
		copyrights=null;
		testConditions=null;
		filterGlobalQuestionsEnabled=null;
		filterOtherUsersQuestionsEnabled=null;
		globalOtherUserCategoryAllowed=null;
		addEnabled=null;
		editEnabled=null;
		editOtherUsersQuestionsEnabled=null;
		editAdminsQuestionsEnabled=null;
		editSuperadminsQuestionsEnabled=null;
		viewQuestionsFromOtherUsersPrivateCategoriesEnabled=null;
		viewQuestionsFromAdminsPrivateCategoriesEnabled=null;
		viewQuestionsFromSuperadminsPrivateCategoriesEnabled=null;
		filterGlobalResourcesEnabled=null;
		filterOtherUsersResourcesEnabled=null;
		useGlobalResources=null;
		useOtherUsersResources=null;
		viewResourcesFromOtherUsersPrivateCategoriesEnabled=null;
		viewResourcesFromAdminsPrivateCategoriesEnabled=null;
		viewResourcesFromSuperadminsPrivateCategoriesEnabled=null;
		admins=new HashMap<Long,Boolean>();
		superadmins=new HashMap<Long,Boolean>();
		allCategories=null;
		specialCategoryFiltersMap=new HashMap<Long,SpecialCategoryFilter>();
	}
	
	public void setLocalizationService(LocalizationService localizationService)
	{
		this.localizationService=localizationService;
		defaultTrueText=localizationService.getLocalizedMessage("TRUE");
		defaultFalseText=localizationService.getLocalizedMessage("FALSE");
	}
	
	public void setConfigurationService(ConfigurationService configurationService)
	{
		this.configurationService=configurationService;
	}
	
	public void setQuestionsService(QuestionsService questionsService)
	{
		this.questionsService=questionsService;
	}
    
	public void setAnswersService(AnswersService answersService)
	{
		this.answersService=answersService;
	}
	
	public void setQuestionResourcesService(QuestionResourcesService questionResourcesService)
	{
		this.questionResourcesService=questionResourcesService;
	}
	
    public void setCategoriesService(CategoriesService categoriesService)
    {
		this.categoriesService=categoriesService;
	}
    
	public void setCategoryTypesService(CategoryTypesService categoryTypesService)
	{
		this.categoryTypesService=categoryTypesService;
	}
    
	public void setVisibilitiesService(VisibilitiesService visibilitiesService)
	{
		this.visibilitiesService=visibilitiesService;
	}
	
    public void setResourcesService(ResourcesService resourcesService)
    {
		this.resourcesService=resourcesService;
	}
    
    public void setCopyrightsService(CopyrightsService copyrightsService)
    {
    	this.copyrightsService=copyrightsService;
    }
    
    public void setFeedbackTypesService(FeedbackTypesService feedbackTypesService)
    {
		this.feedbackTypesService=feedbackTypesService;
	}
    
	public void setUserSessionService(UserSessionService userSessionService)
    {
		this.userSessionService=userSessionService;
	}
    
	public void setPermissionsService(PermissionsService permissionsService)
	{
		this.permissionsService=permissionsService;
	}
	
    public Operation getCurrentUserOperation(Operation operation)
    {
    	if (operation!=null && question==null)
    	{
    		getQuestion();
    		operation=null;
    	}
    	return operation==null?userSessionService.getCurrentUserOperation():operation;
    }
	
    /**
     * Returns a new question if we are creating one or an existing question from DB if we are updating one.
     * <br/><br/>
     * Once question is instantiated (or readed from DB) next calls to this method will return that instance.
     * <br/><br/>
     * If you need to instantiate question again (or read it again from DB) you can set question to null, 
     * but be careful with possible side effects.
     * @return A new question if we are creating one or an existing question from DB if we are editing one
     */
    public Question getQuestion()
    {
    	if (question==null)
    	{
			// End current user session Hibernate operation
			userSessionService.endCurrentUserOperation();
    		
    		// Get current user session Hibernate operation
    		Operation operation=getCurrentUserOperation(null);
    		
    		// We seek parameters
    		FacesContext context=FacesContext.getCurrentInstance();
    		Map<String,String> params=context.getExternalContext().getRequestParameterMap();
    		
    		// Check if we are creating a new question or editing an existing one
    		if (params.containsKey("questionId"))
    		{
    			// As there is a question id within parameters we are editing an existing question
				question=questionsService.getQuestion(operation,Long.parseLong(params.get("questionId")));
				
    			categoryId=question.getCategory().getId();
    			initialCategoryId=categoryId;
    			
        		// We need to initialize special categories filters for the select image dialog
    			List<String> allCategoriesPermissions=new ArrayList<String>();
				allCategoriesPermissions.add("PERMISSION_RESOURCES_GLOBAL_FILTER_ENABLED");
				allCategoriesPermissions.add("PERMISSION_RESOURCES_OTHER_USERS_FILTER_ENABLED");
				List<String> allCategoriesAuthorPermissions=new ArrayList<String>();
				allCategoriesAuthorPermissions.add("PERMISSION_QUESTION_USE_GLOBAL_RESOURCES");
				allCategoriesAuthorPermissions.add("PERMISSION_QUESTION_USE_OTHER_USERS_RESOURCES");
    			String categoryGen=localizationService.getLocalizedMessage("CATEGORY_GEN");
    			if ("M".equals(categoryGen))
    			{
        			allCategories=new SpecialCategoryFilter(
        				0L,"ALL_OPTIONS",allCategoriesPermissions,allCategoriesAuthorPermissions);
    			}
    			else
    			{
        			allCategories=new SpecialCategoryFilter(
            			0L,"ALL_OPTIONS_F",allCategoriesPermissions,allCategoriesAuthorPermissions);
    			}
    			
        		// We add all initialized categories filters to categories filters map
        		specialCategoryFiltersMap=new LinkedHashMap<Long,SpecialCategoryFilter>();
        		specialCategoryFiltersMap.put(Long.valueOf(allCategories.id),allCategories);
        		specialCategoryFiltersMap.put(
        			Long.valueOf(ALL_EVEN_PRIVATE_CATEGORIES.id),ALL_EVEN_PRIVATE_CATEGORIES);
        		if (question.getCreatedBy().getId()==userSessionService.getCurrentUserId())
        		{
        			specialCategoryFiltersMap.put(Long.valueOf(ALL_MY_CATEGORIES_FOR_QUESTION_AUTHOR.id),
        				ALL_MY_CATEGORIES_FOR_QUESTION_AUTHOR);
        			specialCategoryFiltersMap.put(
        				Long.valueOf(ALL_MY_CATEGORIES_EXCEPT_GLOBALS_FOR_QUESTION_AUTHOR.id),
        				ALL_MY_CATEGORIES_EXCEPT_GLOBALS_FOR_QUESTION_AUTHOR);
        		}
        		else
        		{
        			specialCategoryFiltersMap.put(Long.valueOf(ALL_MY_CATEGORIES.id),ALL_MY_CATEGORIES);
        			specialCategoryFiltersMap.put(
        				Long.valueOf(ALL_MY_CATEGORIES_EXCEPT_GLOBALS.id),ALL_MY_CATEGORIES_EXCEPT_GLOBALS);
        			specialCategoryFiltersMap.put(
        				Long.valueOf(ALL_QUESTION_AUTHOR_CATEGORIES.id),ALL_QUESTION_AUTHOR_CATEGORIES);
        			specialCategoryFiltersMap.put(Long.valueOf(ALL_QUESTION_AUTHOR_CATEGORIES_EXCEPT_GLOBALS.id),
        				ALL_QUESTION_AUTHOR_CATEGORIES_EXCEPT_GLOBALS);
        		}
        		specialCategoryFiltersMap.put(Long.valueOf(ALL_GLOBAL_CATEGORIES.id),ALL_GLOBAL_CATEGORIES);
        		specialCategoryFiltersMap.put(
        			Long.valueOf(ALL_PRIVATE_CATEGORIES_OF_OTHER_USERS.id),ALL_PRIVATE_CATEGORIES_OF_OTHER_USERS);
        		specialCategoryFiltersMap.put(
        			Long.valueOf(ALL_CATEGORIES_OF_OTHER_USERS.id),ALL_CATEGORIES_OF_OTHER_USERS);
    		}
    		else if (params.containsKey("questionCopyId"))
    		{
    			// As there is a question copy id within parameters we are creating a new question 
    			// but filled with the data from an existing question
    			question=questionsService.getQuestion(operation,Long.parseLong(params.get("questionCopyId")));
    	    	
    	        // This is a new question so we need to reset some fields
    	        question.setId(0L);
   	        	question.setName("");
   	        	question.setCreatedBy(null);
    	        question.setModifiedBy(null);
    	        question.setTimecreated(null);
    	        question.setTimemodified(null);
    	        question.setTimebuild(null);
    	        question.setTimedeploy(null);
    	        question.setTimepublished(null);
    	        for (Answer answer:question.getAnswers())
    	        {
   	        		answer.setId(0L);
  	        	}
    	        for (QuestionResource questionResource:question.getQuestionResources())
    	        {
    	        	questionResource.setId(0L);
    	        }
    	        for (Feedback feedback:question.getFeedbacks())
    	        {
    	        	feedback.setId(0L);
    	        }
    	        
    	        // Set category of copied question if current user has access granted, otherwise reset it
        		categoryId=question.getCategory().getId();
        		if (checkCategory(operation))
        		{
        			initialCategoryId=categoryId;
        		}
        		else
        		{
        			initialCategoryId=0L;
        			setCategoryId(operation,0L);
        		}
    			
        		// We need to initialize special categories filters for the select image dialog
    			List<String> allCategoriesPermissions=new ArrayList<String>();
				allCategoriesPermissions.add("PERMISSION_RESOURCES_GLOBAL_FILTER_ENABLED");
				allCategoriesPermissions.add("PERMISSION_RESOURCES_OTHER_USERS_FILTER_ENABLED");
				List<String> allCategoriesAuthorPermissions=new ArrayList<String>();
				allCategoriesAuthorPermissions.add("PERMISSION_QUESTION_USE_GLOBAL_RESOURCES");
				allCategoriesAuthorPermissions.add("PERMISSION_QUESTION_USE_OTHER_USERS_RESOURCES");
    			String categoryGen=localizationService.getLocalizedMessage("CATEGORY_GEN");
    			if ("M".equals(categoryGen))
    			{
        			allCategories=new SpecialCategoryFilter(
        				0L,"ALL_OPTIONS",allCategoriesPermissions,allCategoriesAuthorPermissions);
    			}
    			else
    			{
        			allCategories=new SpecialCategoryFilter(
            			0L,"ALL_OPTIONS_F",allCategoriesPermissions,allCategoriesAuthorPermissions);
    			}
    			
        		// We add all initialized categories filters to categories filters map
        		specialCategoryFiltersMap=new LinkedHashMap<Long,SpecialCategoryFilter>();
        		specialCategoryFiltersMap.put(Long.valueOf(allCategories.id),allCategories);
        		specialCategoryFiltersMap.put(
       			Long.valueOf(ALL_EVEN_PRIVATE_CATEGORIES.id),ALL_EVEN_PRIVATE_CATEGORIES);
       			specialCategoryFiltersMap.put(Long.valueOf(ALL_MY_CATEGORIES_FOR_QUESTION_AUTHOR.id),
       				ALL_MY_CATEGORIES_FOR_QUESTION_AUTHOR);
       			specialCategoryFiltersMap.put(
       				Long.valueOf(ALL_MY_CATEGORIES_EXCEPT_GLOBALS_FOR_QUESTION_AUTHOR.id),
        			ALL_MY_CATEGORIES_EXCEPT_GLOBALS_FOR_QUESTION_AUTHOR);
        		specialCategoryFiltersMap.put(Long.valueOf(ALL_GLOBAL_CATEGORIES.id),ALL_GLOBAL_CATEGORIES);
        		specialCategoryFiltersMap.put(
        			Long.valueOf(ALL_PRIVATE_CATEGORIES_OF_OTHER_USERS.id),ALL_PRIVATE_CATEGORIES_OF_OTHER_USERS);
        		specialCategoryFiltersMap.put(
        			Long.valueOf(ALL_CATEGORIES_OF_OTHER_USERS.id),ALL_CATEGORIES_OF_OTHER_USERS);
    		}
    		else
    		{
    			// As there is no question id within parameters we are creating a new question
    			
    			// We need question's type to instatiate the appropiate class for question 
    			String type=params.get("questionType");
    			
        		// We instantiate question and initialize default values
        		question=questionsService.getNewQuestion(type);
        		
        		initialCategoryId=0L;
        		setCategoryId(operation,0L);
        		
        		// Some question types need specific initialization
        		if (question instanceof OmXmlQuestion)
        		{
        			File questionXmlTemplate=null;
        			FileReader questionXmlTemplateReader=null;
        			BufferedReader questionXmlTemplateBufferedReader=null;
        			StringBuffer questionXmlContent=null;
        			try
        			{
            			StringBuffer questionXmlTemplatePath=
            				new StringBuffer(configurationService.getApplicationPath());
            			questionXmlTemplatePath.append(File.separatorChar);
            			questionXmlTemplatePath.append(QUESTION_XML_TEMPLATE_PATH.replace("/",File.separator));
            			questionXmlTemplate=new File(questionXmlTemplatePath.toString());
        				questionXmlTemplateReader=new FileReader(questionXmlTemplate);
        				questionXmlTemplateBufferedReader=new BufferedReader(questionXmlTemplateReader);
        				questionXmlContent=new StringBuffer();
        				String textLine=questionXmlTemplateBufferedReader.readLine();
        				while (textLine!=null)
        				{
        					questionXmlContent.append(textLine);
        					questionXmlContent.append('\n');
        					textLine=questionXmlTemplateBufferedReader.readLine();
        				}
        			}
        			catch (IOException ioe)
        			{
        				questionXmlContent=null;
        			}
        			setXmlContent(questionXmlContent==null?"":questionXmlContent.toString());
        		}
        		
        		// We need to initialize special categories filters for the select image dialog
    			List<String> allCategoriesPermissions=new ArrayList<String>();
				allCategoriesPermissions.add("PERMISSION_RESOURCES_GLOBAL_FILTER_ENABLED");
				allCategoriesPermissions.add("PERMISSION_RESOURCES_OTHER_USERS_FILTER_ENABLED");
				List<String> allCategoriesAuthorPermissions=new ArrayList<String>();
				allCategoriesAuthorPermissions.add("PERMISSION_QUESTION_USE_GLOBAL_RESOURCES");
				allCategoriesAuthorPermissions.add("PERMISSION_QUESTION_USE_OTHER_USERS_RESOURCES");
    			String categoryGen=localizationService.getLocalizedMessage("CATEGORY_GEN");
    			if ("M".equals(categoryGen))
    			{
        			allCategories=new SpecialCategoryFilter(
        				0L,"ALL_OPTIONS",allCategoriesPermissions,allCategoriesAuthorPermissions);
    			}
    			else
    			{
        			allCategories=new SpecialCategoryFilter(
            			0L,"ALL_OPTIONS_F",allCategoriesPermissions,allCategoriesAuthorPermissions);
    			}
    			
        		// We add all initialized categories filters to categories filters map
        		specialCategoryFiltersMap=new LinkedHashMap<Long,SpecialCategoryFilter>();
        		specialCategoryFiltersMap.put(Long.valueOf(allCategories.id),allCategories);
        		specialCategoryFiltersMap.put(
       			Long.valueOf(ALL_EVEN_PRIVATE_CATEGORIES.id),ALL_EVEN_PRIVATE_CATEGORIES);
       			specialCategoryFiltersMap.put(Long.valueOf(ALL_MY_CATEGORIES_FOR_QUESTION_AUTHOR.id),
       				ALL_MY_CATEGORIES_FOR_QUESTION_AUTHOR);
       			specialCategoryFiltersMap.put(
       				Long.valueOf(ALL_MY_CATEGORIES_EXCEPT_GLOBALS_FOR_QUESTION_AUTHOR.id),
        			ALL_MY_CATEGORIES_EXCEPT_GLOBALS_FOR_QUESTION_AUTHOR);
        		specialCategoryFiltersMap.put(Long.valueOf(ALL_GLOBAL_CATEGORIES.id),ALL_GLOBAL_CATEGORIES);
        		specialCategoryFiltersMap.put(
        			Long.valueOf(ALL_PRIVATE_CATEGORIES_OF_OTHER_USERS.id),ALL_PRIVATE_CATEGORIES_OF_OTHER_USERS);
        		specialCategoryFiltersMap.put(
        			Long.valueOf(ALL_CATEGORIES_OF_OTHER_USERS.id),ALL_CATEGORIES_OF_OTHER_USERS);
    		}
    		
    		// Some more initializations
    		if (question instanceof MultichoiceQuestion)
    		{
    			activeAnswerIndex=0;
    			activeAnswerName=getActiveAnswer(context.getViewRoot()).getName();
    		}
    		else if (question instanceof DragDropQuestion)
    		{
    			activeDraggableItemIndex=0;
    			activeDraggableItemName=getActiveDraggableItem(context.getViewRoot()).getName();
    			activeAnswerIndex=0;
    			activeAnswerName=getActiveDroppableAnswer(context.getViewRoot()).getName();
    		}
    		if (question instanceof OmXmlQuestion)
    		{
    			if (!question.getQuestionResources().isEmpty())
    			{
    				activeQuestionResourceIndex=0;
    				activeQuestionResourceName=getActiveQuestionResource(context.getViewRoot()).getName();
    			}
    			resourcesTabviewTab=RESOURCES_OR_FEEDBACK_TABVIEW_TAB;
    			feedbackTabviewTab=resourcesTabviewTab+1;
    		}
    		else
    		{
    			feedbackTabviewTab=RESOURCES_OR_FEEDBACK_TABVIEW_TAB;
    		}
    	}
		return question;
	}
	
    public void setQuestion(Question question)
    {
    	this.question=question;
	}
    
	public long getCategoryId()
	{
		// We call question getter to be sure that the question has been initialized
		getQuestion();
		return categoryId;
	}
	
	public void setCategoryId(long categoryId)
	{
		setCategoryId(null,categoryId);
	}
	
	private void setCategoryId(Operation operation,long categoryId)
	{
		// Get current user session Hibernate operation
		operation=getCurrentUserOperation(operation);
		
		this.categoryId=categoryId;
		category=null;
		getQuestion().setCategory(getCategory(operation));
	}
	
	public Category getCategory()
	{
		return getCategory(null);
	}
	
	private Category getCategory(Operation operation)
	{
		if (category==null && categoryId>0L)
		{
			category=categoriesService.getCategory(getCurrentUserOperation(operation),categoryId);
		}
		return category;
	}
	
	public List<FeedbackBean> getFeedbacks()
	{
		if (feedbacks==null)
		{
			feedbacks=new ArrayList<FeedbackBean>();
			for (Feedback feedback:getQuestion().getFeedbacksSortedByPosition())
			{
				feedbacks.add(new FeedbackBean(this,feedback));
			}
		}
		return feedbacks;
	}
	
	public void setFeedbacks(List<FeedbackBean> feedbacks)
	{
		this.feedbacks=feedbacks;
	}
	
	public List<FeedbackBean> getFeedbacksSorting()
	{
		if (feedbacksSorting==null)
		{
			feedbacksSorting=new ArrayList<FeedbackBean>();
			for (FeedbackBean feedback:getFeedbacks())
			{
				feedbacksSorting.add(feedback);
			}
			Collections.sort((List<FeedbackBean>)feedbacksSorting,new Comparator<FeedbackBean>()
			{
				@Override
				public int compare(FeedbackBean fb1,FeedbackBean fb2)
				{
					return fb1.getPosition()==fb2.getPosition()?0:fb1.getPosition()>fb2.getPosition()?1:-1;
				}
			});
		}
		return feedbacksSorting;
	}
	
	public void setFeedbacksSorting(List<FeedbackBean> feedbacksSorting)
	{
		this.feedbacksSorting=feedbacksSorting;
	}
	
	public List<Answer> getAnswersSorting()
	{
		if (answersSorting==null)
		{
			Question question=getQuestion();
			if (question instanceof DragDropQuestion)
			{
				//TODO de momento solo tenemos en cuenta el grupo 1, habra que cambiarlo mas adelante
				answersSorting=((DragDropQuestion)question).getDroppableAnswersSortedByPosition(1);
			}
			else
			{
				answersSorting=question.getAnswersSortedByPosition();
			}
		}
		return answersSorting;
	}
	
	public void setAnswersSorting(List<Answer> answersSorting)
	{
		this.answersSorting=answersSorting;
	}
	
	public List<Answer> getDraggableItemsSorting()
	{
		if (draggableItemsSorting==null)
		{
			Question question=getQuestion();
			if (question instanceof DragDropQuestion)
			{
				//TODO de momento solo tenemos en cuenta el grupo 1, habra que cambiarlo mas adelante
				draggableItemsSorting=((DragDropQuestion)question).getDraggableItemsSortedByPosition(1);
			}
			else
			{
				draggableItemsSorting=new ArrayList<Answer>();
			}
		}
		return draggableItemsSorting;
	}
	
	public void setDraggableItemsSorting(List<Answer> draggableItemsSorting)
	{
		this.draggableItemsSorting=draggableItemsSorting;
	}
	
	public List<Answer> getDroppableAnswersSorting()
	{
		if (droppableAnswersSorting==null)
		{
			Question question=getQuestion();
			if (question instanceof DragDropQuestion)
			{
				//TODO de momento solo tenemos en cuenta el grupo 1, habra que cambiarlo mas adelante
				droppableAnswersSorting=((DragDropQuestion)question).getDroppableAnswersSortedByPosition(1);
			}
			else
			{
				droppableAnswersSorting=new ArrayList<Answer>();
			}
		}
		return droppableAnswersSorting;
	}
	
	public void setDroppableAnswersSorting(List<Answer> droppableAnswersSorting)
	{
		this.droppableAnswersSorting=droppableAnswersSorting;
	}
	
	public String getTrueText()
	{
		String trueText=null;
		Question question=getQuestion();
		if (question!=null && question instanceof TrueFalseQuestion)
		{
			trueText=((TrueFalseQuestion)question).getTrueText();
			if (trueText==null)
			{
				trueText=getDefaultTrueText();
			}
		}
		return trueText;
	}
	
	public void setTrueText(String trueText)
	{
		Question question=getQuestion();
		if (question instanceof TrueFalseQuestion)
		{
			if (trueText==null || trueText.equals(getDefaultTrueText()))
			{
				defaultTrueText=null;
				((TrueFalseQuestion)question).setTrueText(getDefaultTrueText());
			}
			else
			{
				defaultTrueText=null;
				((TrueFalseQuestion)question).setTrueText(trueText);
			}
		}
	}
	
	public String getFalseText()
	{
		String falseText=null;
		Question question=getQuestion();
		if (question!=null && question instanceof TrueFalseQuestion)
		{
			falseText=((TrueFalseQuestion)question).getFalseText();
			if (falseText==null)
			{
				falseText=getDefaultFalseText();
			}
		}
		return falseText;
	}
	
	public void setFalseText(String falseText)
	{
		Question question=getQuestion();
		if (question instanceof TrueFalseQuestion)
		{
			if (falseText==null || falseText.equals(getDefaultFalseText()))
			{
				defaultFalseText=null;
				((TrueFalseQuestion)question).setFalseText(getDefaultFalseText());
			}
			else
			{
				defaultFalseText=null;
				((TrueFalseQuestion)question).setFalseText(falseText);
			}
		}
	}
	
	private String getDefaultTrueText()
	{
		if (defaultTrueText==null)
		{
			defaultTrueText=localizationService.getLocalizedMessage("TRUE");
		}
		return defaultTrueText;
	}
	
	private String getDefaultFalseText()
	{
		if (defaultFalseText==null)
		{
			defaultFalseText=localizationService.getLocalizedMessage("FALSE");
		}
		return defaultFalseText;
	}
	
	public String getXmlContent()
	{
		String xmlContent=null;
		Question question=getQuestion();
		if (question!=null && question instanceof OmXmlQuestion)
		{
			xmlContent=((OmXmlQuestion)question).getXmlContent();
		}
		return xmlContent;
	}
	
	public void setXmlContent(String xmlContent)
	{
		Question question=getQuestion();
		if (question instanceof OmXmlQuestion)
		{
			((OmXmlQuestion)question).setXmlContent(xmlContent);
		}
	}
	
	public List<QuestionResource> getQuestionResourcesSorting()
	{
		if (questionResourcesSorting==null)
		{
			Question question=getQuestion();
			questionResourcesSorting=question.getQuestionResourcesSortedByPosition();
		}
		return questionResourcesSorting;
	}
	
	public void setQuestionResourcesSorting(List<QuestionResource> questionResourcesSorting)
	{
		this.questionResourcesSorting=questionResourcesSorting;
	}
	
	public Resource getCurrentResource()
	{
		return currentResource;
	}
	
	public void setCurrentResource(Resource currentResource)
	{
		lastResource=this.currentResource;
		this.currentResource=currentResource;
	}
	
	public int getCurrentResourceWidth()
	{
		return currentResourceWidth;
	}
	
	public void setCurrentResourceWidth(int currentResourceWidth)
	{
		this.currentResourceWidth=currentResourceWidth;
	}
	
	public int getCurrentResourceHeight()
	{
		return currentResourceHeight;
	}
	
	public void setCurrentResourceHeight(int currentResourceHeight)
	{
		this.currentResourceHeight=currentResourceHeight;
	}
	
	public double getCurrentResourceAspectRatio()
	{
		return currentResourceAspectRatio;
	}
	
	public void setCurrentResourceAspectRatio(double currentResourceAspectRatio)
	{
		this.currentResourceAspectRatio=currentResourceAspectRatio;
	}
	
	public boolean isCurrentResourceKeepAspectRatio()
	{
		return currentResourceKeepAspectRatio;
	}
	
	public void setCurrentResourceKeepAspectRatio(boolean currentResourceKeepAspectRatio)
	{
		this.currentResourceKeepAspectRatio=currentResourceKeepAspectRatio;
	}
	
	public FeedbackBean getCurrentFeedback()
	{
		return currentFeedback;
	}
	
	public void setCurrentFeedback(FeedbackBean currentFeedback)
	{
		this.currentFeedback=currentFeedback;
	}
	
	public String getConditionType()
	{
		if (conditionType==null)
		{
			List<String> conditionTypes=getConditionTypes();
			if (!conditionTypes.isEmpty())
			{
				conditionType=conditionTypes.get(0);
			}
		}
		return conditionType;
	}
	
	public void setConditionType(String conditionType)
	{
		this.conditionType=conditionType;
	}
	
	public String getCancelQuestionTarget()
	{
		return cancelQuestionTarget;
	}
	
	public void setCancelQuestionTarget(String cancelQuestionTarget)
	{
		this.cancelQuestionTarget=cancelQuestionTarget;
	}
	
	private void initializeFilterCategoryId(Operation operation)
	{
		boolean found=false;
		List<Category> specialCategoriesFilters=getSpecialCategoriesFilters(getCurrentUserOperation(operation));
		Category allQuestionAuthorCategoriesFilter=new Category();
		allQuestionAuthorCategoriesFilter.setId(ALL_QUESTION_AUTHOR_CATEGORIES.id);
		if (specialCategoriesFilters.contains(allQuestionAuthorCategoriesFilter))
		{
			filterCategoryId=ALL_QUESTION_AUTHOR_CATEGORIES.id;
			found=true;
		}
		if (!found)
		{
			Category allQuestionAuthorCategoriesExceptGlobalsFilter=new Category();
			allQuestionAuthorCategoriesExceptGlobalsFilter.setId(ALL_QUESTION_AUTHOR_CATEGORIES_EXCEPT_GLOBALS.id);
			if (specialCategoriesFilters.contains(allQuestionAuthorCategoriesExceptGlobalsFilter))
			{
				filterCategoryId=ALL_QUESTION_AUTHOR_CATEGORIES_EXCEPT_GLOBALS.id;
				found=true;
			}
		}
		if (!found)
		{
			Category allMyCategoriesFilter=new Category();
			allMyCategoriesFilter.setId(ALL_MY_CATEGORIES.id);
			if (specialCategoriesFilters.contains(allMyCategoriesFilter))
			{
				filterCategoryId=ALL_MY_CATEGORIES.id;
				found=true;
			}
		}
		if (!found)
		{
			Category allMyCategoriesExceptGlobalsFilter=new Category();
			allMyCategoriesExceptGlobalsFilter.setId(ALL_MY_CATEGORIES_EXCEPT_GLOBALS.id);
			if (specialCategoriesFilters.contains(allMyCategoriesExceptGlobalsFilter))
			{
				filterCategoryId=ALL_MY_CATEGORIES_EXCEPT_GLOBALS.id;
				found=true;
			}
		}
		if (!found)
		{
			Category allGlobalCategoriesFilter=new Category();
			allGlobalCategoriesFilter.setId(ALL_GLOBAL_CATEGORIES.id);
			if (specialCategoriesFilters.contains(allGlobalCategoriesFilter))
			{
				filterCategoryId=ALL_GLOBAL_CATEGORIES.id;
				found=true;
			}
		}
		if (!found)
		{
			Category allFilter=new Category();
			allFilter.setId(allCategories.id);
			if (specialCategoriesFilters.contains(allFilter))
			{
				filterCategoryId=allCategories.id;
				found=true;
			}
		}
		if (!found && !specialCategoriesFilters.isEmpty())
		{
			filterCategoryId=specialCategoriesFilters.get(0).getId();
		}
	}
	
	public long getFilterCategoryId()
	{
		return getFilterCategoryId(null);
	}
	
	public void setFilterCategoryId(long filterCategoryId)
	{
		this.filterCategoryId=filterCategoryId;
	}
	
	private long getFilterCategoryId(Operation operation)
	{
		if (filterCategoryId==Long.MIN_VALUE)
		{
			initializeFilterCategoryId(getCurrentUserOperation(operation));
		}
		return filterCategoryId;
	}
	
	public boolean isFilterIncludeSubcategories()
	{
		return filterIncludeSubcategories;
	}
	
	public void setFilterIncludeSubcategories(boolean filterIncludeSubcategories)
	{
		this.filterIncludeSubcategories=filterIncludeSubcategories;
	}
	
    public String getFilterMimeType()
    {
    	if (filterMimeType==null)
    	{
    		List<String> mimeTypes=getMimeTypes();
    		List<String> mimeTypesMasks=getMimeTypesMasks();
    		filterMimeType=mimeTypesMasks.isEmpty()?
    			mimeTypes.isEmpty()?null:mimeTypes.get(0):
    			MIME_TYPES_MASKS.get(mimeTypesMasks.get(0));
    	}
		return filterMimeType;
	}
    
	public void setFilterMimeType(String filterMimeType)
	{
		this.filterMimeType=filterMimeType;
	}
	
	public long getFilterCopyrightId()
	{
		return filterCopyrightId;
	}
	
	public void setFilterCopyrightId(long filterCopyrightId)
	{
		this.filterCopyrightId=filterCopyrightId;
	}
	
	
	public Boolean getFilterGlobalQuestionsEnabled()
	{
		return getFilterGlobalQuestionsEnabled(null);
	}
	
	public void setFilterGlobalQuestionsEnabled(Boolean filterGlobalQuestionsEnabled)
	{
		this.filterGlobalQuestionsEnabled=filterGlobalQuestionsEnabled;
	}
	
	public boolean isFilterGlobalQuestionsEnabled()
	{
		return getFilterGlobalQuestionsEnabled().booleanValue();
	}
	
	private Boolean getFilterGlobalQuestionsEnabled(Operation operation)
	{
		if (filterGlobalQuestionsEnabled==null)
		{
			filterGlobalQuestionsEnabled=Boolean.valueOf(userSessionService.isGranted(
				getCurrentUserOperation(operation),"PERMISSION_QUESTIONS_GLOBAL_FILTER_ENABLED"));
		}
		return filterGlobalQuestionsEnabled;
	}
	
	public Boolean getFilterOtherUsersQuestionsEnabled()
	{
		return getFilterGlobalQuestionsEnabled(null);
	}
	public void setFilterOtherUsersQuestionsEnabled(Boolean filterOtherUsersQuestionsEnabled)
	{
		this.filterOtherUsersQuestionsEnabled=filterOtherUsersQuestionsEnabled;
	}
	
	public boolean isFilterOtherUsersQuestionsEnabled()
	{
		return getFilterOtherUsersQuestionsEnabled().booleanValue();
	}
	
	private Boolean getFilterOtherUsersQuestionsEnabled(Operation operation)
	{
		if (filterOtherUsersQuestionsEnabled==null)
		{
			filterOtherUsersQuestionsEnabled=Boolean.valueOf(userSessionService.isGranted(
				getCurrentUserOperation(operation),"PERMISSION_QUESTIONS_OTHER_USERS_FILTER_ENABLED"));
		}
		return filterOtherUsersQuestionsEnabled;
	}
	
	public Boolean getGlobalOtherUserCategoryAllowed()
	{
		return getGlobalOtherUserCategoryAllowed(null);
	}
	
	public void setGlobalOtherUserCategoryAllowed(Boolean globalOtherUserCategoryAllowed)
	{
		this.globalOtherUserCategoryAllowed=globalOtherUserCategoryAllowed;
	}
	
	public boolean isGlobalOtherUserCategoryAllowed()
	{
		return getGlobalOtherUserCategoryAllowed().booleanValue();
	}
	
	private Boolean getGlobalOtherUserCategoryAllowed(Operation operation)
	{
		if (globalOtherUserCategoryAllowed==null)
		{
			// Get current user session Hibernate operation
			operation=getCurrentUserOperation(operation);
			
			Question question=getQuestion();
			if (question.getId()>0L)
			{
				globalOtherUserCategoryAllowed=Boolean.valueOf(permissionsService.isGranted(
					operation,question.getCreatedBy(),"PERMISSION_QUESTION_GLOBAL_OTHER_USER_CATEGORY_ALLOWED"));
			}
			else
			{
				globalOtherUserCategoryAllowed=Boolean.valueOf(userSessionService.isGranted(
					operation,"PERMISSION_QUESTION_GLOBAL_OTHER_USER_CATEGORY_ALLOWED"));
			}
		}
		return globalOtherUserCategoryAllowed;
	}
	
	public Boolean getAddEnabled()
	{
		return getAddEnabled(null);
	}
	
	public void setAddEnabled(Boolean addEnabled)
	{
		this.addEnabled=addEnabled;
	}
	
	public boolean isAddEnabled()
	{
		return getAddEnabled().booleanValue();
	}
	
	private Boolean getAddEnabled(Operation operation)
	{
		if (addEnabled==null)
		{
			addEnabled=Boolean.valueOf(userSessionService.isGranted(
				getCurrentUserOperation(operation),"PERMISSION_QUESTIONS_ADD_ENABLED"));
		}
		return addEnabled;
	}
	
	public Boolean getEditEnabled()
	{
		return getEditEnabled(null);
	}
	
	public void setEditEnabled(Boolean editEnabled)
	{
		this.editEnabled=editEnabled;
	}
	
	public boolean isEditEnabled()
	{
		return getEditEnabled().booleanValue();
	}
	
	private Boolean getEditEnabled(Operation operation)
	{
		if (editEnabled==null)
		{
			editEnabled=Boolean.valueOf(userSessionService.isGranted(
				getCurrentUserOperation(operation),"PERMISSION_QUESTIONS_EDIT_ENABLED"));
		}
		return editEnabled;
	}
	
	public Boolean getEditOtherUsersQuestionsEnabled()
	{
		return getEditOtherUsersQuestionsEnabled(null);
	}
	
	public void setEditOtherUsersQuestionsEnabled(Boolean editOtherUsersQuestionsEnabled)
	{
		this.editOtherUsersQuestionsEnabled=editOtherUsersQuestionsEnabled;
	}
	
	public boolean isEditOtherUsersQuestionsEnabled()
	{
		return getEditOtherUsersQuestionsEnabled().booleanValue();
	}
	
	private Boolean getEditOtherUsersQuestionsEnabled(Operation operation)
	{
		if (editOtherUsersQuestionsEnabled==null)
		{
			editOtherUsersQuestionsEnabled=Boolean.valueOf(userSessionService.isGranted(
				getCurrentUserOperation(operation),"PERMISSION_QUESTIONS_EDIT_OTHER_USERS_QUESTIONS_ENABLED"));
		}
		return editOtherUsersQuestionsEnabled;
	}
	
	public Boolean getEditAdminsQuestionsEnabled()
	{
		return getEditAdminsQuestionsEnabled(null);
	}
	
	public void setEditAdminsQuestionsEnabled(Boolean editAdminsQuestionsEnabled)
	{
		this.editAdminsQuestionsEnabled=editAdminsQuestionsEnabled;
	}
	
	public boolean isEditAdminsQuestionsEnabled()
	{
		return getEditAdminsQuestionsEnabled().booleanValue();
	}
	
	private Boolean getEditAdminsQuestionsEnabled(Operation operation)
	{
		if (editAdminsQuestionsEnabled==null)
		{
			editAdminsQuestionsEnabled=Boolean.valueOf(userSessionService.isGranted(
				getCurrentUserOperation(operation),"PERMISSION_QUESTIONS_EDIT_ADMINS_QUESTIONS_ENABLED"));
		}
		return editAdminsQuestionsEnabled;
	}
	
	public Boolean getEditSuperadminsQuestionsEnabled()
	{
		return getEditSuperadminsQuestionsEnabled(null);
	}
	
	public void setEditSuperadminsQuestionsEnabled(Boolean editSuperadminsQuestionsEnabled)
	{
		this.editSuperadminsQuestionsEnabled=editSuperadminsQuestionsEnabled;
	}
	
	public boolean isEditSuperadminsQuestionsEnabled()
	{
		return getEditSuperadminsQuestionsEnabled().booleanValue();
	}
	
	private Boolean getEditSuperadminsQuestionsEnabled(Operation operation)
	{
		if (editSuperadminsQuestionsEnabled==null)
		{
			editSuperadminsQuestionsEnabled=Boolean.valueOf(userSessionService.isGranted(
				getCurrentUserOperation(operation),"PERMISSION_QUESTIONS_EDIT_SUPERADMINS_QUESTIONS_ENABLED"));
		}
		return editSuperadminsQuestionsEnabled;
	}
	
	public Boolean getQuestionAuthorAdmin()
	{
		return getQuestionAuthorAdmin(null);
	}
	
	public boolean isQuestionAuthorAdmin()
	{
		return getQuestionAuthorAdmin().booleanValue();
	}
	
	private Boolean getQuestionAuthorAdmin(Operation operation)
	{
		// Get current user session Hibernate operation
		operation=getCurrentUserOperation(operation);
		
		Question question=getQuestion();
		User questionAuthor=
			question.getId()>0L?question.getCreatedBy():userSessionService.getCurrentUser(operation);
		return isAdmin(operation,questionAuthor);
	}
	
	private void resetQuestionAuthorAdmin()
	{
		Question question=getQuestion();
		long questionAuthorId=
			question.getId()>0L?question.getCreatedBy().getId():userSessionService.getCurrentUserId();
		admins.remove(Long.valueOf(questionAuthorId));
	}
	
	public Boolean getQuestionAuthorSuperadmin()
	{
		return getQuestionAuthorSuperadmin(null);
	}
	
	public boolean isQuestionAuthorSuperadmin()
	{
		return getQuestionAuthorSuperadmin().booleanValue();
	}
	
	private Boolean getQuestionAuthorSuperadmin(Operation operation)
	{
		// Get current user session Hibernate operation
		operation=getCurrentUserOperation(operation);
		
		Question question=getQuestion();
		User questionAuthor=
			question.getId()>0L?question.getCreatedBy():userSessionService.getCurrentUser(operation);
		return isSuperadmin(operation,questionAuthor);
	}
	
	private void resetQuestionAuthorSuperadmin()
	{
		Question question=getQuestion();
		long questionAuthorId=
			question.getId()>0L?question.getCreatedBy().getId():userSessionService.getCurrentUserId();
		superadmins.remove(Long.valueOf(questionAuthorId));
	}
	
	public Boolean getViewQuestionsFromOtherUsersPrivateCategoriesEnabled()
	{
		return getViewQuestionsFromOtherUsersPrivateCategoriesEnabled(null);
	}
	
	public void setViewQuestionsFromOtherUsersPrivateCategoriesEnabled(
		Boolean viewQuestionsFromOtherUsersPrivateCategoriesEnabled)
	{
		this.viewQuestionsFromOtherUsersPrivateCategoriesEnabled=
			viewQuestionsFromOtherUsersPrivateCategoriesEnabled;
	}
	
	public boolean isViewQuestionsFromOtherUsersPrivateCategoriesEnabled()
	{
		return getViewQuestionsFromOtherUsersPrivateCategoriesEnabled().booleanValue();
	}
	
	private Boolean getViewQuestionsFromOtherUsersPrivateCategoriesEnabled(Operation operation)
	{
		if (viewQuestionsFromOtherUsersPrivateCategoriesEnabled==null)
		{
			viewQuestionsFromOtherUsersPrivateCategoriesEnabled=
				Boolean.valueOf(userSessionService.isGranted(getCurrentUserOperation(operation),
				"PERMISSION_QUESTIONS_VIEW_QUESTIONS_OF_OTHER_USERS_PRIVATE_CATEGORIES_ENABLED"));
		}
		return viewQuestionsFromOtherUsersPrivateCategoriesEnabled;
	}
	
	public Boolean getViewQuestionsFromAdminsPrivateCategoriesEnabled()
	{
		return getViewQuestionsFromAdminsPrivateCategoriesEnabled(null);
	}
	
	public void setViewQuestionsFromAdminsPrivateCategoriesEnabled(
		Boolean viewQuestionsFromAdminsPrivateCategoriesEnabled)
	{
		this.viewQuestionsFromAdminsPrivateCategoriesEnabled=viewQuestionsFromAdminsPrivateCategoriesEnabled;
	}
	
	public boolean isViewQuestionsFromAdminsPrivateCategoriesEnabled()
	{
		return getViewQuestionsFromAdminsPrivateCategoriesEnabled().booleanValue();
	}
	
	private Boolean getViewQuestionsFromAdminsPrivateCategoriesEnabled(Operation operation)
	{
		if (viewQuestionsFromAdminsPrivateCategoriesEnabled==null)
		{
			viewQuestionsFromAdminsPrivateCategoriesEnabled=
				Boolean.valueOf(userSessionService.isGranted(getCurrentUserOperation(operation),
				"PERMISSION_QUESTIONS_VIEW_QUESTIONS_OF_ADMINS_PRIVATE_CATEGORIES_ENABLED"));
		}
		return viewQuestionsFromAdminsPrivateCategoriesEnabled;
	}
	
	public Boolean getViewQuestionsFromSuperadminsPrivateCategoriesEnabled()
	{
		return getViewQuestionsFromSuperadminsPrivateCategoriesEnabled(null);
	}
	
	public void setViewQuestionsFromSuperadminsPrivateCategoriesEnabled(
		Boolean viewQuestionsFromSuperadminsPrivateCategoriesEnabled)
	{
		this.viewQuestionsFromSuperadminsPrivateCategoriesEnabled=
			viewQuestionsFromSuperadminsPrivateCategoriesEnabled;
	}
	
	public boolean isViewQuestionsFromSuperadminsPrivateCategoriesEnabled()
	{
		return getViewQuestionsFromSuperadminsPrivateCategoriesEnabled().booleanValue();
	}
	
	private Boolean getViewQuestionsFromSuperadminsPrivateCategoriesEnabled(Operation operation)
	{
		if (viewQuestionsFromSuperadminsPrivateCategoriesEnabled==null)
		{
			viewQuestionsFromSuperadminsPrivateCategoriesEnabled=
				Boolean.valueOf(userSessionService.isGranted(getCurrentUserOperation(operation),
				"PERMISSION_QUESTIONS_VIEW_QUESTIONS_OF_SUPERADMINS_PRIVATE_CATEGORIES_ENABLED"));
		}
		return viewQuestionsFromSuperadminsPrivateCategoriesEnabled;
	}
	
	public Boolean getFilterGlobalResourcesEnabled()
	{
		return getFilterGlobalResourcesEnabled(null);
	}
	
	public void setFilterGlobalResourcesEnabled(Boolean filterGlobalResourcesEnabled)
	{
		this.filterGlobalResourcesEnabled=filterGlobalResourcesEnabled;
	}
	
	public boolean isFilterGlobalResourcesEnabled()
	{
		return getFilterGlobalQuestionsEnabled().booleanValue();
	}
	
	private Boolean getFilterGlobalResourcesEnabled(Operation operation)
	{
		if (filterGlobalResourcesEnabled==null)
		{
			filterGlobalResourcesEnabled=Boolean.valueOf(userSessionService.isGranted(
				getCurrentUserOperation(operation),"PERMISSION_RESOURCES_GLOBAL_FILTER_ENABLED"));
		}
		return filterGlobalResourcesEnabled;
	}
	
	public Boolean getFilterOtherUsersResourcesEnabled()
	{
		return getFilterOtherUsersResourcesEnabled(null);
	}
	
	public void setFilterOtherUsersResourcesEnabled(Boolean filterOtherUsersResourcesEnabled)
	{
		this.filterOtherUsersResourcesEnabled=filterOtherUsersResourcesEnabled;
	}
	
	public boolean isFilterOtherUsersResourcesEnabled()
	{
		return getFilterOtherUsersResourcesEnabled().booleanValue();
	}
	
	private Boolean getFilterOtherUsersResourcesEnabled(Operation operation)
	{
		if (filterOtherUsersResourcesEnabled==null)
		{
			filterOtherUsersResourcesEnabled=Boolean.valueOf(userSessionService.isGranted(
				getCurrentUserOperation(operation),"PERMISSION_RESOURCES_OTHER_USERS_FILTER_ENABLED"));
		}
		return filterOtherUsersResourcesEnabled;
	}
	
	public Boolean getUseGlobalResources()
	{
		return getUseGlobalResources(null);
	}
	
	public void setUseGlobalResources(Boolean useGlobalResources)
	{
		this.useGlobalResources=useGlobalResources;
	}
	
	public boolean isUseGlobalResources()
	{
		return getUseGlobalResources().booleanValue();
	}
	
	private Boolean getUseGlobalResources(Operation operation)
	{
		if (useGlobalResources==null)
		{
			// Get current user session Hibernate operation
			operation=getCurrentUserOperation(operation);
			
			Question question=getQuestion();
			if (question.getId()>0L)
			{
				useGlobalResources=Boolean.valueOf(permissionsService.isGranted(
					operation,question.getCreatedBy(),"PERMISSION_QUESTION_USE_GLOBAL_RESOURCES"));
			}
			else
			{
				useGlobalResources=Boolean.valueOf(
					userSessionService.isGranted(operation,"PERMISSION_QUESTION_USE_GLOBAL_RESOURCES"));
			}
		}
		return useGlobalResources;
	}
	
	public Boolean getUseOtherUsersResources()
	{
		return getUseOtherUsersResources(null);
	}
	
	public void setUseOtherUsersResources(Boolean useOtherUsersResources)
	{
		this.useOtherUsersResources=useOtherUsersResources;
	}
	
	public boolean isUseOtherUsersResources()
	{
		return getUseOtherUsersResources().booleanValue();
	}
	
	private Boolean getUseOtherUsersResources(Operation operation)
	{
		if (useOtherUsersResources==null)
		{
			// Get current user session Hibernate operation
			operation=getCurrentUserOperation(operation);
			
			Question question=getQuestion();
			if (question.getId()>0L)
			{
				useOtherUsersResources=Boolean.valueOf(permissionsService.isGranted(
					operation,question.getCreatedBy(),"PERMISSION_QUESTION_USE_OTHER_USERS_RESOURCES"));
			}
			else
			{
				useOtherUsersResources=Boolean.valueOf(
					userSessionService.isGranted(operation,"PERMISSION_QUESTION_USE_OTHER_USERS_RESOURCES"));
			}
		}
		return useOtherUsersResources;
	}
	
	public Boolean getViewResourcesFromOtherUsersPrivateCategoriesEnabled()
	{
		return getViewResourcesFromOtherUsersPrivateCategoriesEnabled(null);
	}
	
	public void setViewResourcesFromOtherUsersPrivateCategoriesEnabled(
		Boolean viewResourcesFromOtherUsersPrivateCategoriesEnabled)
	{
		this.viewResourcesFromOtherUsersPrivateCategoriesEnabled=
			viewResourcesFromOtherUsersPrivateCategoriesEnabled;
	}
	
	public boolean isViewResourcesFromOtherUsersPrivateCategoriesEnabled()
	{
		return getViewResourcesFromOtherUsersPrivateCategoriesEnabled().booleanValue();
	}
	
	private Boolean getViewResourcesFromOtherUsersPrivateCategoriesEnabled(Operation operation)
	{
		if (viewResourcesFromOtherUsersPrivateCategoriesEnabled==null)
		{
			viewResourcesFromOtherUsersPrivateCategoriesEnabled=
				Boolean.valueOf(userSessionService.isGranted(getCurrentUserOperation(operation),
				"PERMISSION_RESOURCES_VIEW_RESOURCES_OF_OTHER_USERS_PRIVATE_CATEGORIES_ENABLED"));
		}
		return viewResourcesFromOtherUsersPrivateCategoriesEnabled;
	}
	
	public Boolean getViewResourcesFromAdminsPrivateCategoriesEnabled()
	{
		return getViewResourcesFromAdminsPrivateCategoriesEnabled(null);
	}
	
	public void setViewResourcesFromAdminsPrivateCategoriesEnabled(
		Boolean viewResourcesFromAdminsPrivateCategoriesEnabled)
	{
		this.viewResourcesFromAdminsPrivateCategoriesEnabled=viewResourcesFromAdminsPrivateCategoriesEnabled;
	}
	
	public boolean isViewResourcesFromAdminsPrivateCategoriesEnabled()
	{
		return getViewResourcesFromAdminsPrivateCategoriesEnabled().booleanValue();
	}
	
	private Boolean getViewResourcesFromAdminsPrivateCategoriesEnabled(Operation operation)
	{
		if (viewResourcesFromAdminsPrivateCategoriesEnabled==null)
		{
			viewResourcesFromAdminsPrivateCategoriesEnabled=
				Boolean.valueOf(userSessionService.isGranted(getCurrentUserOperation(operation),
				"PERMISSION_RESOURCES_VIEW_RESOURCES_OF_ADMINS_PRIVATE_CATEGORIES_ENABLED"));
		}
		return viewResourcesFromAdminsPrivateCategoriesEnabled;
	}
	
	public Boolean getViewResourcesFromSuperadminsPrivateCategoriesEnabled()
	{
		return getViewResourcesFromSuperadminsPrivateCategoriesEnabled(null);
	}
	
	public void setViewResourcesFromSuperadminsPrivateCategoriesEnabled(
		Boolean viewResourcesFromSuperadminsPrivateCategoriesEnabled)
	{
		this.viewResourcesFromSuperadminsPrivateCategoriesEnabled=
			viewResourcesFromSuperadminsPrivateCategoriesEnabled;
	}
	
	public boolean isViewResourcesFromSuperadminsPrivateCategoriesEnabled()
	{
		return getViewResourcesFromSuperadminsPrivateCategoriesEnabled().booleanValue();
	}
	
	private Boolean getViewResourcesFromSuperadminsPrivateCategoriesEnabled(Operation operation)
	{
		if (viewResourcesFromSuperadminsPrivateCategoriesEnabled==null)
		{
			viewResourcesFromSuperadminsPrivateCategoriesEnabled=
				Boolean.valueOf(userSessionService.isGranted(getCurrentUserOperation(operation),
				"PERMISSION_RESOURCES_VIEW_RESOURCES_OF_SUPERADMINS_PRIVATE_CATEGORIES_ENABLED"));
		}
		return viewResourcesFromSuperadminsPrivateCategoriesEnabled;
	}
	
	private void resetAdmins()
	{
		admins.clear();
	}
	
	private void resetAdminFromCategoryAllowed(Category category)
	{
		if (category!=null && category.getUser()!=null)
		{
			admins.remove(Long.valueOf(category.getUser().getId()));
		}
	}
	
	private boolean isAdmin(Operation operation,User user)
	{
		boolean admin=false;
		if (user!=null)
		{
			Long userId=Long.valueOf(user.getId());
			if (admins.containsKey(userId))
			{
				admin=admins.get(userId).booleanValue();
			}
			else
			{
				admin=permissionsService.isGranted(
					getCurrentUserOperation(operation),user,"PERMISSION_NAVIGATION_ADMINISTRATION");
				admins.put(userId,Boolean.valueOf(admin));
			}
		}
		return admin;
	}
	
	private void resetSuperadmins()
	{
		superadmins.clear();
	}
	
	private void resetSuperadminFromCategoryAllowed(Category category)
	{
		if (category!=null && category.getUser()!=null)
		{
			superadmins.remove(Long.valueOf(category.getUser().getId()));
		}
	}
	
	private boolean isSuperadmin(Operation operation,User user)
	{
		boolean superadmin=false;
		if (user!=null)
		{
			Long userId=Long.valueOf(user.getId());
			if (superadmins.containsKey(userId))
			{
				superadmin=superadmins.get(userId).booleanValue();
			}
			else
			{
				superadmin=permissionsService.isGranted(
					getCurrentUserOperation(operation),user,
					"PERMISSION_ADMINISTRATION_RAISE_PERMISSIONS_OVER_OWNED_ALLOWED");
				superadmins.put(userId,Boolean.valueOf(superadmin));
			}
		}
		return superadmin;
	}
	
	public FeedbackType getFeedbackType(String type)
	{
		return getFeedbackType(null,type);
	}
	
	private FeedbackType getFeedbackType(Operation operation,String type)
	{
		return feedbackTypesService.getFeedbackType(getCurrentUserOperation(operation),type);
	}
	
	public String getCurrentResourceFileName()
	{
		String fileName=null;
		if (getCurrentResource()==null || getCurrentResource().getId()==-1L)
		{
			fileName="/resources/images/empty.gif";
		}
		else
		{
			fileName=getCurrentResource().getFileName();
		}
		return fileName;
	}
	
	public int getCurrentResourceWidthForPropertiesDialog()
	{
		return getCurrentResourceWidthForPropertiesDialog(null);
	}
	
	private int getCurrentResourceWidthForPropertiesDialog(Operation operation)
	{
		int width=1;
		if (getCurrentResource()!=null && getCurrentResource().getId()!=-1L)
		{
			// Get current user session Hibernate operation
			operation=getCurrentUserOperation(operation);
			
			ResourceBean currentResourceBean=new ResourceBean(getCurrentResource());
			currentResourceBean.setResourcesService(resourcesService);
			currentResourceBean.setUserSessionService(userSessionService);
			width=currentResourceBean.getWidth(operation);
			int height=currentResourceBean.getHeight(operation);
			if (width>MAX_IMAGE_WIDTH_FOR_PROPERTIES_DIALOG || height>MAX_IMAGE_HEIGHT_FOR_PROPERTIES_DIALOG)
			{
				if ((double)width/(double)height>=
					(double)MAX_IMAGE_WIDTH_FOR_PROPERTIES_DIALOG/(double)MAX_IMAGE_HEIGHT_FOR_PROPERTIES_DIALOG)
				{
					width=MAX_IMAGE_WIDTH_FOR_PROPERTIES_DIALOG;
				}
				else
				{
					width=(width*MAX_IMAGE_HEIGHT_FOR_PROPERTIES_DIALOG)/height;
				}
			}
		}
		return width>=1?width:1;
	}
	
	public int getCurrentResourceHeightForPropertiesDialog()
	{
		return getCurrentResourceHeightForPropertiesDialog(null);
	}
	
	private int getCurrentResourceHeightForPropertiesDialog(Operation operation)
	{
		int height=1;
		if (getCurrentResource()!=null && getCurrentResource().getId()!=-1L)
		{
			// Get current user session Hibernate operation
			operation=getCurrentUserOperation(operation);
			
			ResourceBean currentResourceBean=new ResourceBean(getCurrentResource());
			currentResourceBean.setResourcesService(resourcesService);
			currentResourceBean.setUserSessionService(userSessionService);
			int width=currentResourceBean.getWidth(operation);
			height=currentResourceBean.getHeight(operation);
			if (width>MAX_IMAGE_WIDTH_FOR_PROPERTIES_DIALOG || height>MAX_IMAGE_HEIGHT_FOR_PROPERTIES_DIALOG)
			{
				if ((double)width/(double)height>=
					(double)MAX_IMAGE_WIDTH_FOR_PROPERTIES_DIALOG/(double)MAX_IMAGE_HEIGHT_FOR_PROPERTIES_DIALOG)
				{
					height=(height*MAX_IMAGE_WIDTH_FOR_PROPERTIES_DIALOG)/width;
				}
				else
				{
					height=MAX_IMAGE_HEIGHT_FOR_PROPERTIES_DIALOG;
				}
			}
		}
		return height>=1?height:1;
	}
	
	public String getCurrentResourceImageName()
	{
		return getCurrentResource()==null || getCurrentResource().getId()==-1L?"":getCurrentResource().getName();
	}
	
	public String getCurrentResourceImageUser()
	{
		return getCurrentResource()==null || getCurrentResource().getId()==-1L?
			"":getCurrentResource().getUser().getNick();
	}
	
	public String getCurrentResourceImageDescription()
	{
		return getCurrentResource()==null || getCurrentResource().getId()==-1L?
			"":getCurrentResource().getDescription();
	}
	
	public String getCurrentResourceImageCategoryName()
	{
		return getCurrentResourceImageCategoryName(null);
	}
	
	private String getCurrentResourceImageCategoryName(Operation operation)
	{
		return getCurrentResource()==null || 
			getCurrentResource().getId()==-1L?"":categoriesService.getLocalizedCategoryName(
			getCurrentUserOperation(operation),getCurrentResource().getCategory().getId());
	}
	
	public String getCurrentResourceImageLongCategoryName()
	{
		return getCurrentResourceImageLongCategoryName(null);
	}
	
	private String getCurrentResourceImageLongCategoryName(Operation operation)
	{
		return getCurrentResource()==null || 
			getCurrentResource().getId()==-1L?null:categoriesService.getLocalizedCategoryLongName(
			getCurrentUserOperation(operation),getCurrentResource().getCategory().getId());
	}
	
	public String getCurrentResourceImageCopyright()
	{
		return getCurrentResourceImageCopyright(null);
	}
	
	private String getCurrentResourceImageCopyright(Operation operation)
	{
		String imageCopyright=null;
		if (getCurrentResource()!=null && getCurrentResource().getId()!=-1L && 
			getCurrentResource().getCopyright()!=null)
		{
			imageCopyright=copyrightsService.getLocalizedCopyright(
				getCurrentUserOperation(operation),getCurrentResource().getCopyright().getId());
		}
		else
		{
			imageCopyright="";
		}
		return imageCopyright;
	}
	
	public String getCurrentResourceImageMIMEType()
	{
		return getCurrentResource()==null || 
			getCurrentResource().getId()==-1L?"":getCurrentResource().getMimeType();
	}
	
	public String getCurrentResourceImageDimensions()
	{
		return getCurrentResourceImageDimensions(null); 
	}
	
	private String getCurrentResourceImageDimensions(Operation operation)
	{
		String imageDimensions=null;
		if (getCurrentResource()!=null && getCurrentResource().getId()!=-1L)
		{
			imageDimensions=resourcesService.getImageDimensionsString(
				getCurrentUserOperation(operation),getCurrentResource().getId());
		}
		return imageDimensions; 
	}
	
	private int[] getCurrentResourceImageDimensionsArray()
	{
		int[] imageDimensionsArray=null;
		if (getCurrentResource()!=null && getCurrentResource().getId()!=-1L)
		{
			imageDimensionsArray=resourcesService.getImageDimensions(getCurrentResource().getId());
		}
		else
		{
			imageDimensionsArray=new int[2];
			imageDimensionsArray[0]=-1;
			imageDimensionsArray[1]=-1;
		}
		return imageDimensionsArray;
	}
	
	/**
	 * @return List of identifiers of categories for questions from current user or globals
	 */
	public List<Long> getQuestionsCategoriesIds()
    {
		return getQuestionsCategoriesIds(null);
	}
	
	/**
	 * @return List of identifiers of categories for questions from current user or globals
	 */
	private List<Long> getQuestionsCategoriesIds(Operation operation)
    {
    	List<Long> questionsCategoriesIds=new ArrayList<Long>();
    	for (Category category:getQuestionsCategories(getCurrentUserOperation(operation)))
    	{
    		questionsCategoriesIds.add(Long.valueOf(category.getId()));
    	}
		return questionsCategoriesIds;
	}
	
    /**
	 * @return List of visible categories for questions from current user or globals 
	 */
    public List<Category> getQuestionsCategories()
    {
    	return getQuestionsCategories(null);
	}
    
    public void setQuestionsCategories(List<Category> questionsCategories)
    {
    	this.questionsCategories=questionsCategories;
    }
    
    /**
	 * @param operation Operation
	 * @return List of visible categories for questions from current user or globals 
	 */
    private List<Category> getQuestionsCategories(Operation operation)
    {
    	if (questionsCategories==null)
    	{
    		// Get current user session Hibernate operation
    		operation=getCurrentUserOperation(operation);
    		
    		Question question=getQuestion();
    		User questionAuthor=
    			question.getId()>0L?question.getCreatedBy():userSessionService.getCurrentUser(operation);
    		
        	questionsCategories=categoriesService.getCategoriesSortedByHierarchy(operation,questionAuthor,
        		categoryTypesService.getCategoryType(operation,"CATEGORY_TYPE_QUESTIONS"),true,true,
        		CategoriesService.NOT_VIEW_OTHER_USERS_CATEGORIES);
   			
			// Remove from list categories not allowed
        	List<Category> questionsCategoriesToRemove=new ArrayList<Category>();
        	for (Category questionCategory:questionsCategories)
        	{
        		if (!checkCategory(operation,questionCategory))
        		{
        			questionsCategoriesToRemove.add(questionCategory);
        		}
        	}
        	for (Category questionCategoryToRemove:questionsCategoriesToRemove)
        	{
        		questionsCategories.remove(questionCategoryToRemove);
        	}
    	}
    	return questionsCategories;
	}
    
	/**
	 * @return Identifiers of special categories used to filter other categories
	 */
	public List<Long> getSpecialCategoriesFiltersIds()
	{
		return getSpecialCategoriesFiltersIds(null);
	}
	
	/**
	 * @param operation Operation
	 * @return Identifiers of special categories used to filter other categories
	 */
	private List<Long> getSpecialCategoriesFiltersIds(Operation operation)
	{
		List<Long> specialCategoriesFiltersIds=new ArrayList<Long>();
		for (Category specialCategoryFilter:getSpecialCategoriesFilters(getCurrentUserOperation(operation)))
		{
			specialCategoriesFiltersIds.add(specialCategoryFilter.getId());
		}
		return specialCategoriesFiltersIds;
	}
	
	/**
	 * @return Special categories used to filter other categories
	 */
	public List<Category> getSpecialCategoriesFilters()
	{
		return getSpecialCategoriesFilters(null);
	}
    
	/**
	 * @param operation Operation
	 * @return Special categories used to filter other categories
	 */
	private List<Category> getSpecialCategoriesFilters(Operation operation)
	{
		if (specialCategoriesFilters==null)
		{
			specialCategoriesFilters=new ArrayList<Category>();
			
			// Get current user session Hibernate operation
			operation=getCurrentUserOperation(operation);
			
			Question question=getQuestion();
			User questionAuthor=
				question.getId()>0L?question.getCreatedBy():userSessionService.getCurrentUser(operation);
			Map<String,Boolean> cachedPermissions=new HashMap<String,Boolean>();
			Map<String,Boolean> cachedAuthorPermissions=new HashMap<String,Boolean>();
			for (SpecialCategoryFilter specialCategoryFilter:specialCategoryFiltersMap.values())
			{
				boolean granted=true;
				for (String requiredPermission:specialCategoryFilter.requiredPermissions)
				{
					if (cachedPermissions.containsKey(requiredPermission))
					{
						granted=cachedPermissions.get(requiredPermission).booleanValue();
					}
					else
					{
						granted=userSessionService.isGranted(operation,requiredPermission);
						cachedPermissions.put(requiredPermission,Boolean.valueOf(granted));
					}
					if (!granted)
					{
						break;
					}
				}
				if (granted)
				{
					for (String requiredAuthorPermission:specialCategoryFilter.requiredAuthorPermissions)
					{
						if (cachedAuthorPermissions.containsKey(requiredAuthorPermission))
						{
							granted=cachedAuthorPermissions.get(requiredAuthorPermission).booleanValue();
						}
						else
						{
							granted=
								permissionsService.isGranted(operation,questionAuthor,requiredAuthorPermission);
							cachedAuthorPermissions.put(requiredAuthorPermission,Boolean.valueOf(granted));
						}
						if (!granted)
						{
							break;
						}
					}
					if (granted)
					{
						Category specialCategory=new Category();
						specialCategory.setId(specialCategoryFilter.id);
						specialCategory.setName(specialCategoryFilter.name);
						specialCategoriesFilters.add(specialCategory);
					}
				}
			}
		}
		return specialCategoriesFilters;
	}
	
	/**
	 * @param specialCategoryFilter Special category that represents an special category filter
	 * @return Localized special category filter's name (including question's author nick if needed)
	 */
	public String getSpecialCategoryFilterName(Category specialCategoryFilter)
	{
		String specialCategoryFilterName=localizationService.getLocalizedMessage(specialCategoryFilter.getName());
		if (specialCategoryFilterName.contains("?"))
		{
			// Get question
			Question question=getQuestion();
			User questionAuthor=question.getId()>0L?question.getCreatedBy():userSessionService.getCurrentUser();
			specialCategoryFilterName=specialCategoryFilterName.replace("?",questionAuthor.getNick());
		}
		return specialCategoryFilterName;
	}
	
	/**
	 * @return List of identifiers of visible categories for images 
	 */
    public List<Long> getImagesCategoriesIds()
    {
    	return getImagesCategoriesIds(null);
    }
    
	/**
	 * @return List of identifiers of visible categories for images 
	 */
    private List<Long> getImagesCategoriesIds(Operation operation)
    {
    	List<Long> imagesCategoriesIds=new ArrayList<Long>();
    	for (Category category:getImagesCategories(getCurrentUserOperation(operation)))
    	{
    		imagesCategoriesIds.add(Long.valueOf(category.getId()));
    	}
    	return imagesCategoriesIds;
    }
    
    /**
	 * @return List of visible categories for images
	 */
    public List<Category> getImagesCategories()
    {
    	return getImagesCategories(null);
    }
    
    public void setImagesCategories(List<Category> imagesCategories)
    {
    	this.imagesCategories=imagesCategories;
    }
    
    /**
     * @param operation Operation
	 * @return List of visible categories for images
	 */
    private List<Category> getImagesCategories(Operation operation)
    {
    	if (imagesCategories==null)
    	{
    		// Get current user session Hibernate operation
    		operation=getCurrentUserOperation(operation);
    		
       		// Get filter value for viewing resources from global categories
       		boolean includeGlobalCategories=getFilterGlobalResourcesEnabled(operation).booleanValue();
    		
       		// Get filter value for viewing resources from categories of other users based on permissions
       		// of current user
       		int includeOtherUsersCategories=CategoriesService.NOT_VIEW_OTHER_USERS_CATEGORIES;
       		if (getFilterOtherUsersResourcesEnabled(operation).booleanValue())
       		{
       			if (getViewResourcesFromOtherUsersPrivateCategoriesEnabled(operation).booleanValue())
       			{
       				includeOtherUsersCategories=CategoriesService.VIEW_OTHER_USERS_ALL_CATEGORIES;
       			}
       			else
       			{
       				includeOtherUsersCategories=CategoriesService.VIEW_OTHER_USERS_PUBLIC_CATEGORIES;
       			}
       		}
       		
       		// In case that current user is allowed to view resources from private categories of other users 
       		// we also need to check if he/she has permission to view resources from private categories 
       		// of administrators and/or users with permission to improve permissions over their owned ones 
       		// (superadmins)
       		boolean includeAdminsPrivateCategories=false;
       		boolean includeSuperadminsPrivateCategories=false;
       		if (includeOtherUsersCategories==CategoriesService.VIEW_OTHER_USERS_ALL_CATEGORIES)
       		{
       			includeAdminsPrivateCategories=
       				getViewResourcesFromAdminsPrivateCategoriesEnabled(operation).booleanValue();
       			includeSuperadminsPrivateCategories=
       				getViewResourcesFromSuperadminsPrivateCategoriesEnabled(operation).booleanValue();
       		}
       		
       		// We need to get get resource category
    		Category resourceCategory=getCurrentResource()==null?null:getCurrentResource().getCategory();
       		
    		// If it is required we will need to change previous filters to be sure that query results will
       		// include resource category
    		if (resourceCategory!=null)
    		{
        		Visibility resourceCategoryVisibility=
        			visibilitiesService.getVisibilityFromCategoryId(operation,resourceCategory.getId());
        		User resourceCategoryUser=resourceCategory.getUser();
        		boolean isResourceCategoryVisibilityGlobal=resourceCategoryVisibility.isGlobal();
        		boolean isResourceCategoryFromOtherUser=!isResourceCategoryVisibilityGlobal &&
        			resourceCategoryUser.getId()!=userSessionService.getCurrentUserId();
        		boolean isResourceCategoryVisibilityPrivate=isResourceCategoryFromOtherUser && 
        			resourceCategoryVisibility.getLevel()>=visibilitiesService.getVisibility(
        			operation,"CATEGORY_VISIBILITY_PRIVATE").getLevel();
        		
        		includeGlobalCategories=includeGlobalCategories || isResourceCategoryVisibilityGlobal;
    			if (isResourceCategoryFromOtherUser)
    			{
    				if (isResourceCategoryVisibilityPrivate)
    				{
    					includeOtherUsersCategories=CategoriesService.VIEW_OTHER_USERS_ALL_CATEGORIES;
    					includeAdminsPrivateCategories=includeAdminsPrivateCategories || 
    						isAdmin(operation,resourceCategoryUser);
    					includeSuperadminsPrivateCategories=
    						includeSuperadminsPrivateCategories || isSuperadmin(operation,resourceCategoryUser);
    				}
    				else
    				{
    					if (includeOtherUsersCategories!=CategoriesService.VIEW_OTHER_USERS_ALL_CATEGORIES)
    					{
    						includeOtherUsersCategories=CategoriesService.VIEW_OTHER_USERS_PUBLIC_CATEGORIES;
    					}
    				}
    			}
    		}
       		
       		// Get visible categories for images taking account all user permissions
       		imagesCategories=categoriesService.getCategoriesSortedByHierarchy(operation,
       			userSessionService.getCurrentUser(operation),
       			categoryTypesService.getCategoryType(operation,"CATEGORY_TYPE_IMAGES"),true,
       			includeGlobalCategories,includeOtherUsersCategories,includeAdminsPrivateCategories,
       			includeSuperadminsPrivateCategories);
       		
			// Remove from list categories not allowed (except resource category)
        	List<Category> imagesCategoriesToRemove=new ArrayList<Category>();
        	for (Category imageCategory:imagesCategories)
        	{
        		if (!imageCategory.equals(resourceCategory) && 
        			!checkImagesFilterPermission(operation,imageCategory))
        		{
        			imagesCategoriesToRemove.add(imageCategory);
        		}
        	}
        	for (Category imageCategoryToRemove:imagesCategoriesToRemove)
        	{
        		imagesCategories.remove(imageCategoryToRemove);
        	}
    	}
    	return imagesCategories;
    }
    
    /**
     * @return All copyrights
     */
    public List<Copyright> getCopyrights()
    {
    	return getCopyrights(null);
    }
    
    /**
     * @param operation Operation
     * @return All copyrights
     */
    private List<Copyright> getCopyrights(Operation operation)
    {
    	if (copyrights==null)
    	{
    		copyrights=copyrightsService.getCopyrights(getCurrentUserOperation(operation));
    	}
    	return copyrights; 
    }
    
    /**
     * @param copyrights All copyrights
     */
    public void setCopyrights(List<Copyright> copyrights)
    {
    	this.copyrights=copyrights;
    }
    
    public List<String> getMimeTypes()
    {
    	if (mimeTypes==null)
    	{
    		mimeTypes=resourcesService.getSupportedMIMETypes();
    	}
    	return mimeTypes;
    }
    
    public List<String> getMimeTypesMasks()
    {
    	if (mimeTypesMasks==null)
    	{
    		mimeTypesMasks=new ArrayList<String>();
    		for (String mimeTypeMask:MIME_TYPES_MASKS.keySet())
    		{
    			mimeTypesMasks.add(mimeTypeMask);
    		}
    	}
    	return mimeTypesMasks;
    }
    
    public String getMimeTypeMaskValue(String mimeTypeMask)
    {
    	return MIME_TYPES_MASKS.get(mimeTypeMask);
    }
    
    /**
     * @return A special resource that represents not having selected any image 
     */
    public Resource getNoImage()
    {
		Resource noResource=new Resource();
		noResource.setId(-1L);
		noResource.setName(localizationService.getLocalizedMessage("NO_IMAGE"));
		return noResource;
    }
    
    /**
     * @return Images for "Select image" dialog
     */
    public List<Resource> getImages()
    {
    	if (images==null)
    	{
			// End current user session Hibernate operation
			userSessionService.endCurrentUserOperation();
    		
    		// Get current user session Hibernate operation
    		Operation operation=getCurrentUserOperation(null);
    		
    		try
    		{
    			User currentUser=userSessionService.getCurrentUser(operation);
    			Question question=getQuestion();
    			User questionAuthor=question.getId()>0L?question.getCreatedBy():currentUser;
    			
    			Resource resource=getCurrentResource();
    			Category resourceCategory=
    				resource==null?null:categoriesService.getCategoryFromResourceId(operation,resource.getId());
    			Category filterCategory=null;
				long filterCategoryId=getFilterCategoryId(operation);
    			if (filterCategoryId>0L)
    			{
    				if (categoriesService.checkCategoryId(operation, filterCategoryId))
    				{
    					filterCategory=categoriesService.getCategory(operation,filterCategoryId);
    				}
    			}
    			else
    			{
    				filterCategory=new Category();
    				filterCategory.setId(filterCategoryId);
    			}
    			if (filterCategory!=null && (filterCategory.equals(resourceCategory) || 
    				checkImagesFilterPermission(operation,filterCategory)))
    			{
    				if (specialCategoryFiltersMap.containsKey(Long.valueOf(filterCategoryId)))
    				{
    					SpecialCategoryFilter filter=specialCategoryFiltersMap.get(Long.valueOf(filterCategoryId));
    					if (allCategories.equals(filter))
    					{
    						images=resourcesService.getAllVisibleCategoriesResourcesSortedByName(
    							operation,null,getFilterMimeType(),getFilterCopyrightId());
    					}
    					else if (ALL_EVEN_PRIVATE_CATEGORIES.equals(filter))
    					{
    						images=resourcesService.getAllCategoriesResourcesSortedByName(
    							operation,null,getFilterMimeType(),getFilterCopyrightId());
    					}
    					else if (ALL_MY_CATEGORIES.equals(filter))
    					{
    						images=resourcesService.getAllMyCategoriesResourcesSortedByName(
    							operation,null,getFilterMimeType(),getFilterCopyrightId());
    					}
    					else if (ALL_MY_CATEGORIES_EXCEPT_GLOBALS.equals(filter))
    					{
    						images=resourcesService.getAllMyCategoriesExceptGlobalsResourcesSortedByName(
    							operation,null,getFilterMimeType(),getFilterCopyrightId());
    					}
    					else if (ALL_QUESTION_AUTHOR_CATEGORIES.equals(filter))
    					{
    						images=resourcesService.getAllUserCategoriesResourcesSortedByName(
    							operation,questionAuthor,null,getFilterMimeType(),getFilterCopyrightId());
    					}
    					else if (ALL_QUESTION_AUTHOR_CATEGORIES_EXCEPT_GLOBALS.equals(filter))
    					{
    						images=resourcesService.getAllUserCategoriesExceptGlobalsResourcesSortedByName(
    							operation,questionAuthor,null,getFilterMimeType(),getFilterCopyrightId());
    					}
    					else if (ALL_GLOBAL_CATEGORIES.equals(filter))
    					{
    						images=resourcesService.getAllGlobalCategoriesResourcesSortedByName(
    							operation,null,getFilterMimeType(),getFilterCopyrightId());
    					}
    					else if (ALL_PUBLIC_CATEGORIES_OF_OTHER_USERS.equals(filter))
    					{
    						images=resourcesService.getAllPublicCategoriesOfOtherUsersResourcesSortedByName(
    							operation,null,getFilterMimeType(),getFilterCopyrightId());
    					}
    					else if (ALL_PRIVATE_CATEGORIES_OF_OTHER_USERS.equals(filter))
    					{
    						images=resourcesService.getAllPrivateCategoriesOfOtherUsersResourcesSortedByName(
    							operation,null,getFilterMimeType(),getFilterCopyrightId());
    					}
    					else if (ALL_CATEGORIES_OF_OTHER_USERS.equals(filter))
    					{
    						images=resourcesService.getAllCategoriesOfOtherUsersResourcesSortedByName(
    							operation,null,getFilterMimeType(),getFilterCopyrightId());
    					}
    				}
    				else
    				{
    					images=resourcesService.getResources(operation,null,filterCategoryId,
    						isFilterIncludeSubcategories(),getFilterMimeType(),getFilterCopyrightId());
    				}
    				
    				// We need to remove images not visible for current user
    				List<Resource> imagesToRemove=new ArrayList<Resource>();
					Map<Category,Boolean> imagesCategories=new HashMap<Category,Boolean>();
    				for (Resource image:images)
    				{
    					boolean checkImageCategory=false;
    					Category imageCategory=image.getCategory();
    					if (imagesCategories.containsKey(imageCategory))
    					{
    						checkImageCategory=imagesCategories.get(imageCategory).booleanValue();
    					}
    					else
    					{
    						checkImageCategory=checkImagesFilterPermission(operation,imageCategory);
    						imagesCategories.put(imageCategory,Boolean.valueOf(checkImageCategory));
    					}
    					if (!checkImageCategory && !image.equals(resource))
    					{
    						imagesToRemove.add(image);
    					}
    				}
    				for (Resource imageToRemove:imagesToRemove)
    				{
    					images.remove(imageToRemove);
    				}
    			}
    		}
    		finally
    		{
				// It is not a good idea to return null even if an error is produced because JSF getters 
				// are usually called several times
    			if (images==null)
    			{
					images=new ArrayList<Resource>();
    			}
    		}
    	}
		return images;
    }
    
    /**
     * @return Message to display within the delete draggable item confirmation dialog
     */
    public String getConfirmDeleteDraggableItemMessage()
    {
		return confirmDeleteDraggableItemMessage;
	}
    
    /**
     * Set message to display within the delete draggable item confirmation dialog.
     * @param confirmDeleteDraggableItemMessage Message to display within the delete draggable item 
     * confirmation dialog
     */
	public void setConfirmDeleteDraggableItemMessage(String confirmDeleteDraggableItemMessage)
	{
		this.confirmDeleteDraggableItemMessage=confirmDeleteDraggableItemMessage;
	}
    
    /**
     * @return Message to display within the change property confirmation dialog
     */
    public String getConfirmChangePropertyMessage()
    {
		return confirmChangePropertyMessage;
	}
    
    /**
     * Set message to display within the change property confirmation dialog.
     * @param confirmChangePropertyMessage Message to display within the change property confirmation dialog
     */
	public void setConfirmChangePropertyMessage(String confirmChangePropertyMessage)
	{
		this.confirmChangePropertyMessage=confirmChangePropertyMessage;
	}
	
	/**
     * @return Feedback types
     */
    public List<String> getFeedbackTypes()
    {
    	return FeedbackBean.TYPES;
    }
    
    /**
     * @return Available condition's types for current feedback
     */
    public List<String> getConditionTypes()
    {
    	List<String> conditionTypes=new ArrayList<String>();
    	if (getCurrentFeedback()!=null)
    	{
    		Question question=getQuestion();
    		if (question instanceof TrueFalseQuestion)
    		{
            	if (getCurrentFeedback().getTestCondition()==null)
            	{
            		conditionTypes.add(TestConditionBean.TYPE);
            	}
            	if (getCurrentFeedback().getAttemptsCondition()==null)
            	{
            		conditionTypes.add(AttemptsConditionBean.TYPE);
            	}
    		}
    		else if (question instanceof MultichoiceQuestion)
    		{
            	if (getCurrentFeedback().getTestCondition()==null)
            	{
            		conditionTypes.add(TestConditionBean.TYPE);
            	}
        		conditionTypes.add(AnswerConditionBean.TYPE);
            	if (getCurrentFeedback().getAttemptsCondition()==null)
            	{
            		conditionTypes.add(AttemptsConditionBean.TYPE);
            	}
            	if (getCurrentFeedback().getSelectedAnswersCondition()==null)
            	{
            		conditionTypes.add(SelectedAnswersConditionBean.TYPE);
            	}
            	if (getCurrentFeedback().getSelectedRightAnswersCondition()==null)
            	{
            		conditionTypes.add(SelectedRightAnswersConditionBean.TYPE);
            	}
            	if (getCurrentFeedback().getSelectedWrongAnswersCondition()==null)
            	{
            		conditionTypes.add(SelectedWrongAnswersConditionBean.TYPE);
            	}
            	if (getCurrentFeedback().getUnselectedAnswersCondition()==null)
            	{
            		conditionTypes.add(UnselectedAnswersConditionBean.TYPE);
            	}
            	if (getCurrentFeedback().getUnselectedRightAnswersCondition()==null)
            	{
            		conditionTypes.add(UnselectedRightAnswersConditionBean.TYPE);
            	}
            	if (getCurrentFeedback().getUnselectedWrongAnswersCondition()==null)
            	{
            		conditionTypes.add(UnselectedWrongAnswersConditionBean.TYPE);
            	}
            	if (getCurrentFeedback().getRightDistanceCondition()==null)
            	{
            		conditionTypes.add(RightDistanceConditionBean.TYPE);
            	}
    		}
    		else if (question instanceof DragDropQuestion)
    		{
            	if (getCurrentFeedback().getTestCondition()==null)
            	{
            		conditionTypes.add(TestConditionBean.TYPE);
            	}
        		conditionTypes.add(AnswerConditionBean.TYPE);
            	if (getCurrentFeedback().getAttemptsCondition()==null)
            	{
            		conditionTypes.add(AttemptsConditionBean.TYPE);
            	}
            	if (getCurrentFeedback().getSelectedRightAnswersCondition()==null)
            	{
            		conditionTypes.add(SelectedRightAnswersConditionBean.TYPE);
            	}
            	if (getCurrentFeedback().getSelectedWrongAnswersCondition()==null)
            	{
            		conditionTypes.add(SelectedWrongAnswersConditionBean.TYPE);
            	}
            	if (getCurrentFeedback().getRightDistanceCondition()==null)
            	{
            		conditionTypes.add(RightDistanceConditionBean.TYPE);
            	}
    		}
    	}
    	return conditionTypes;
    }
    
    /**
     * @return Number of available condition's types for current feedback
     */
    public int getConditionTypesSize()
    {
    	return getConditionTypes().size();
    }
    
    /** 
     * @return Available conditions of type 'test'
     */
    public List<String> getTestConditions()
    {
    	if (testConditions==null)
    	{
        	testConditions=new ArrayList<String>();
        	for (String testCondition:TestConditionBean.TESTS.keySet())
        	{
        		testConditions.add(testCondition);
        	}
    	}
    	return testConditions;
    }
    
    /**
     * @param genKey Key with the genere of the comparators
     * @return Number comparators
     */
    public List<String> getNumberComparators(String genKey)
    {
    	List<String> numberComparators=null;
    	String gen=localizationService.getLocalizedMessage(genKey);
    	if ("M".equals(gen))
    	{
    		numberComparators=QUESTION_NUMBER_COMPARATORS;
    	}
    	else if ("F".equals(gen))
    	{
    		numberComparators=QUESTION_NUMBER_COMPARATORS_F;
    	}
    	else
    	{
    		boolean genF=false;
    		if (DEFAULT_QUESTION_NUMBER_COMPARATORS_GEN.containsKey(genKey))
    		{
    			genF=DEFAULT_QUESTION_NUMBER_COMPARATORS_GEN.get(genKey).booleanValue();
    		}
    		numberComparators=genF?QUESTION_NUMBER_COMPARATORS_F:QUESTION_NUMBER_COMPARATORS;
    	}
    	return numberComparators;
    }
    
    /**
     * @return Type of current feedback
     */
    public String getCurrentFeedbackType()
    {
    	String type=null;
    	if (getCurrentFeedback()!=null)
    	{
    		type=getCurrentFeedback().getType();
    	}
    	return type;
    }
    
    /**
     * Set type of current feedback.
     * @param type Type
     */
    public void setCurrentFeedbackType(String type)
    {
    	if (getCurrentFeedback()!=null)
    	{
    		getCurrentFeedback().setType(type);
    	}
    }
    
    /**
     * @return Resource file name of current feedback
     */
    public String getCurrentFeedbackResourceFileName()
    {
    	String fileName=null;
    	if (getCurrentFeedback()!=null && getCurrentFeedback().getResource()!=null)
    	{
    		fileName=getCurrentFeedback().getResource().getFileName();
    	}
    	return fileName;
    }
    
    /**
     * @return Resource name of current feedback
     */
    public String getCurrentFeedbackResourceName()
    {
    	String name=null;
    	if (getCurrentFeedback()!=null && getCurrentFeedback().getResource()!=null)
    	{
    		name=getCurrentFeedback().getResource().getName();
    	}
    	return name;
    }
    
    /**
     * @return Localized text for 'test' condition value of current feedback 
     */
	public String getCurrentFeedbakTestConditionText()
	{
		StringBuffer testConditionText=null;
		if (getCurrentFeedback()!=null)
		{
			String test=getCurrentFeedback().getTestCondition().getTest();
			if (test==null)
			{
				test=getTestConditions().get(0);
			}
			testConditionText=new StringBuffer(test);
			testConditionText.append("_TEXT");
		}
		return testConditionText==null?"":localizationService.getLocalizedMessage(testConditionText.toString());
	}
    
	/**
	 * @return Number of selectable answers
	 */
	public int getNumberOfSelectableAnswers()
	{
		int selectableAnswers=0;
		Question question=getQuestion();
		if (question!=null)
		{
			if (question instanceof TrueFalseQuestion)
			{
				selectableAnswers=1;
			}
			else if (question instanceof MultichoiceQuestion)
			{
				if (((MultichoiceQuestion)question).isSingle())
				{
					selectableAnswers=1;
				}
				else
				{
					selectableAnswers=question.getAnswers().size();
				}
			}
			else if (question instanceof DragDropQuestion)
			{
				//TODO de momento solo tenemos en cuenta el grupo 1, habra que cambiarlo mas adelante
				selectableAnswers=((DragDropQuestion)question).getDroppableAnswers(1).size();
			}
		}
		return selectableAnswers;
	}
	
	/**
	 * @return Number of selectable right answers
	 */
	public int getNumberOfSelectableRightAnswers()
	{
		int selectableRightAnswers=0;
		Question question=getQuestion();
		if (question!=null)
		{
			if (question instanceof TrueFalseQuestion)
			{
				selectableRightAnswers=1;
			}
			else if (question instanceof MultichoiceQuestion)
			{
				if (((MultichoiceQuestion)question).isSingle())
				{
					selectableRightAnswers=1;
				}
				else
				{
					for (Answer answer:question.getAnswers())
					{
						if (answer.getCorrect().booleanValue())
						{
							selectableRightAnswers++;
						}
					}
				}
			}
			else if (question instanceof DragDropQuestion)
			{
				//TODO de momento solo tenemos en cuenta el grupo 1, habra que cambiarlo mas adelante
				selectableRightAnswers=((DragDropQuestion)question).getDroppableAnswers(1).size();
			}
		}
		return selectableRightAnswers;
	}
	
	/**
	 * @return Number of selectable wrong answers
	 */
	public int getNumberOfSelectableWrongAnswers()
	{
		int selectableWrongAnswers=0;
		Question question=getQuestion();
		if (question!=null)
		{
			if (question instanceof TrueFalseQuestion)
			{
				selectableWrongAnswers=1;
			}
			else if (question instanceof MultichoiceQuestion)
			{
				if (((MultichoiceQuestion)question).isSingle())
				{
					selectableWrongAnswers=1;
				}
				else
				{
					for (Answer answer:question.getAnswers())
					{
						if (!answer.getCorrect().booleanValue())
						{
							selectableWrongAnswers++;
						}
					}
				}
			}
			else if (question instanceof DragDropQuestion)
			{
				//TODO de momento solo tenemos en cuenta el grupo 1, habra que cambiarlo mas adelante
				selectableWrongAnswers=((DragDropQuestion)question).getDroppableAnswers(1).size();
			}
		}
		return selectableWrongAnswers;
	}
	
	/**
	 * @param answer Answer
	 * @return Available draggable items for an answer in a "Drag & Drop" question 
	 */
	public List<Answer> getAvailableDraggableItemsForDroppableAnswer(Answer answer)
	{
		List<Answer> availableDraggableItems=new ArrayList<Answer>();
		Question question=getQuestion();
		if (question instanceof DragDropQuestion && answer!=null && answer instanceof DragDropAnswer && 
			!((DragDropAnswer)answer).isDraggable())
		{
			DragDropQuestion dragDropQuestion=(DragDropQuestion)question;
			int group=((DragDropAnswer)answer).getGroup();
			availableDraggableItems=dragDropQuestion.getDraggableItemsSortedByPosition(group);
			if (!dragDropQuestion.isInfinite())
			{
				for (Answer da:dragDropQuestion.getDroppableAnswers(group))
				{
					DragDropAnswer droppableAnswer=(DragDropAnswer)da; 
					if (!droppableAnswer.equals(answer) && droppableAnswer.getRightAnswer()!=null)
					{
						availableDraggableItems.remove(droppableAnswer.getRightAnswer());
					}
				}
			}
		}
		return availableDraggableItems;
	}
	
    /**
     * @return List of draggable items of a "Drag & Drop" question
     */
    public List<DragDropAnswer> getDraggableItemsSortedByPosition()
    {
    	List<DragDropAnswer> draggableItemsSortedByPosition=new ArrayList<DragDropAnswer>();
    	Question question=getQuestion();
    	if (question instanceof DragDropQuestion)
    	{
			//TODO de momento solo tenemos en cuenta el grupo 1, habra que cambiarlo mas adelante
    		for (Answer answer:((DragDropQuestion)question).getDraggableItemsSortedByPosition(1))
    		{
    			draggableItemsSortedByPosition.add((DragDropAnswer)answer);
    		}
    	}
    	return draggableItemsSortedByPosition;
    }
	
	/**
	 * @param draggableItem Draggable item
	 * @return Draggable item's title
	 */
	public String getDraggableItemTitle(Answer draggableItem)
	{
    	StringBuffer draggableItemTitle=new StringBuffer();
    	draggableItemTitle.append(localizationService.getLocalizedMessage("DRAGGABLE_ITEM"));
    	draggableItemTitle.append(' ');
    	draggableItemTitle.append(draggableItem.getPosition());
    	if ((draggableItem.getName()!=null && !draggableItem.getName().equals("") && 
    		checkDraggableItemName(draggableItem.getName(),false)) || 
    		(draggableItem.getPosition()==activeDraggableItemIndex+1 && activeDraggableItemName!=null && 
    		!activeDraggableItemName.equals("") && checkDraggableItemName(activeDraggableItemName,false)))
    	{
    		draggableItemTitle.append(": ");
    		draggableItemTitle.append(getNumberedDraggableItemName(draggableItem));
    	}
    	return draggableItemTitle.toString();
	}
	
    /**
     * @param draggableItem Draggable item
     * @return Draggable item's name with a number appended if it is needed to distinguish draggable items 
     * with the same name
     */
    public String getNumberedDraggableItemName(Answer draggableItem)
    {
    	StringBuffer draggableItemName=new StringBuffer();
    	if (draggableItem!=null)
    	{
    		boolean okDraggableItemName=draggableItem.getName()!=null && !draggableItem.getName().equals("") &&
    			checkDraggableItemName(draggableItem.getName(),false);
    		boolean okActiveDraggableItemName=!okDraggableItemName && 
    			draggableItem.getPosition()==activeDraggableItemIndex+1 && activeDraggableItemName!=null &&
    			!activeDraggableItemName.equals("") && checkDraggableItemName(activeDraggableItemName,false);
        	if (okDraggableItemName || okActiveDraggableItemName)
            {
            	DragDropQuestion dragDropQuestion=(DragDropQuestion)getQuestion();
            	draggableItemName.append(okDraggableItemName?draggableItem.getName():activeDraggableItemName);
            	int itNumber=1;
    			//TODO de momento solo tenemos en cuenta el grupo 1, habra que cambiarlo mas adelante
            	for (Answer di:dragDropQuestion.getDraggableItems(1))
            	{
            		if (di.getPosition()<draggableItem.getPosition())
            		{
            			String draggableItemNameNotNull=draggableItem.getName()==null?"":draggableItem.getName();
            			String diNameNotNull=di.getName()==null?"":di.getName();
            			if (draggableItemNameNotNull.equals(diNameNotNull))
            			{
            				itNumber++;
            			}
            		}
            	}
            	if (itNumber>1)
            	{
            		draggableItemName.append('(');
            		draggableItemName.append(itNumber);
            		draggableItemName.append(')');
            	}
            }
            else
            {
            	draggableItemName.append(localizationService.getLocalizedMessage("DRAGGABLE_ITEM"));
            	draggableItemName.append(' ');
            	draggableItemName.append(draggableItem.getPosition());
            }
    	}
    	return draggableItemName.toString();
    }
	
    /**
     * @param answer Answer
     * @return Answer's title
     */
    public String getAnswerTitle(Answer answer)
    {
    	StringBuffer answerTitle=new StringBuffer();
    	answerTitle.append(localizationService.getLocalizedMessage("ANSWER"));
    	answerTitle.append(' ');
    	answerTitle.append(answer.getPosition());
    	if ((answer.getName()!=null && !answer.getName().equals("") && checkAnswerName(answer.getName(),false)) ||
    		(answer.getPosition()==activeAnswerIndex+1 && activeAnswerName!=null && 
    		!activeAnswerName.equals("") && checkAnswerName(activeAnswerName,false)))
    	{
    		answerTitle.append(": ");
    		answerTitle.append(getNumberedAnswerName(answer));
    	}
    	return answerTitle.toString();
    }
    
    /**
     * @param answer Answer
     * @return Answer's name with a number appended if it is needed to distinguish answers with the same name
     */
    public String getNumberedAnswerName(Answer answer)
    {
    	StringBuffer answerName=new StringBuffer();
    	if (answer!=null)
    	{
    		boolean okAnswerName=answer.getName()!=null && !answer.getName().equals("") && 
    			checkAnswerName(answer.getName(),false);
    		boolean okActiveAnswerName=!okAnswerName && answer.getPosition()==activeAnswerIndex+1 && 
    			activeAnswerName!=null && !activeAnswerName.equals("") && 
    			checkAnswerName(activeAnswerName,false);
    		if (okAnswerName || okActiveAnswerName)
    		{
        		answerName.append(okAnswerName?answer.getName():activeAnswerName);
        		int itNumber=1;
        		for (Answer a:getQuestion().getAnswers())
        		{
        			if (a.getPosition()<answer.getPosition())
        			{
            			String answerNameNotNull=answer.getName()==null?"":answer.getName();
            			String aNameNotNull=a.getName()==null?"":a.getName();
            			if (answerNameNotNull.equals(aNameNotNull))
            			{
            				itNumber++;
            			}
        			}
        		}
        		if (itNumber>1)
        		{
        			answerName.append('(');
        			answerName.append(itNumber);
        			answerName.append(')');
        		}
    		}
        	else
        	{
        		answerName.append(localizationService.getLocalizedMessage("ANSWER"));
        		answerName.append(' ');
        		answerName.append(answer.getPosition());
        	}
    	}
    	return answerName.toString();
    }
    
    /**
     * @return List of answers of a "Drag & Drop" question
     */
    public List<DragDropAnswer> getDroppableAnswersSortedByPosition()
    {
    	List<DragDropAnswer> droppableAnswersSortedByPosition=new ArrayList<DragDropAnswer>();
    	Question question=getQuestion();
    	if (question instanceof DragDropQuestion)
    	{
			//TODO de momento solo tenemos en cuenta el grupo 1, habra que cambiarlo mas adelante
    		for (Answer answer:((DragDropQuestion)question).getDroppableAnswersSortedByPosition(1))
    		{
    			droppableAnswersSortedByPosition.add((DragDropAnswer)answer);
    		}
    	}
    	return droppableAnswersSortedByPosition;
    }
    
    /**
     * @param answer Answer
     * @return Answer's title
     */
    public String getDroppableAnswerTitle(Answer answer)
    {
    	StringBuffer answerTitle=new StringBuffer();
    	answerTitle.append(localizationService.getLocalizedMessage("ANSWER"));
    	answerTitle.append(' ');
    	answerTitle.append(answer.getPosition());
    	if ((answer.getName()!=null && !answer.getName().equals("") && checkAnswerName(answer.getName(),false)) || 
    		(answer.getPosition()==activeAnswerIndex && activeAnswerName!=null && 
    		!activeAnswerName.equals("") && checkAnswerName(activeAnswerName,false)))
    	{
    		answerTitle.append(": ");
    		answerTitle.append(getNumberedDroppableAnswerName(answer));
    	}
    	return answerTitle.toString();
    }
    
    /**
     * @param answer Answer
     * @return Answer's name with a number appended if it is needed to distinguish answers with the same name
     */
    public String getNumberedDroppableAnswerName(Answer answer)
    {
    	StringBuffer answerName=new StringBuffer();
    	if (answerName!=null)
    	{
    		boolean okAnswerName=answer.getName()!=null && !answer.getName().equals("") && 
    			checkAnswerName(answer.getName(),false);
    		boolean okActiveAnswerName=!okAnswerName && answer.getPosition()==activeAnswerIndex+1 && 
    			activeAnswerName!=null && !activeAnswerName.equals("") && 
    			checkAnswerName(activeAnswerName,false);
        	if (okAnswerName || okActiveAnswerName)
        	{
        		DragDropQuestion dragDropQuestion=(DragDropQuestion)getQuestion();
        		answerName.append(okAnswerName?answer.getName():activeAnswerName);
        		int itNumber=1;
    			//TODO de momento solo tenemos en cuenta el grupo 1, habra que cambiarlo mas adelante
        		for (Answer a:dragDropQuestion.getDroppableAnswers(1))
        		{
        			if (a.getPosition()<answer.getPosition())
        			{
            			String answerNameNotNull=answer.getName()==null?"":answer.getName();
            			String aNameNotNull=a.getName()==null?"":a.getName();
            			if (answerNameNotNull.equals(aNameNotNull))
            			{
            				itNumber++;
            			}
        			}
        		}
        		if (itNumber>1)
        		{
        			answerName.append('(');
        			answerName.append(itNumber);
        			answerName.append(')');
        		}
        	}
        	else
        	{
        		answerName.append(localizationService.getLocalizedMessage("ANSWER"));
        		answerName.append(' ');
        		answerName.append(answer.getPosition());
        	}
    	}
    	return answerName.toString();
    }
    
    /**
     * @param questionResource Resource
     * @return Resource's title
     */
    public String getResourceTitle(QuestionResource questionResource)
    {
    	// Get question
    	Question question=getQuestion();
    	
    	StringBuffer resourceTitle=new StringBuffer();
    	resourceTitle.append(localizationService.getLocalizedMessage("RESOURCE"));
    	resourceTitle.append(' ');
    	resourceTitle.append(questionResource.getPosition());
    	if ((questionResource.getName()!=null && !questionResource.getName().equals("") && 
    		checkResourceName(question,questionResource.getName(),questionResource.getPosition(),false)) ||
    		(questionResource.getPosition()==activeQuestionResourceIndex+1 && activeQuestionResourceName!=null && 
    		!activeQuestionResourceName.equals("") && 
    		checkResourceName(question,activeQuestionResourceName,activeQuestionResourceIndex+1,false)))
    	{
    		resourceTitle.append(": ");
    		resourceTitle.append(getValidResourceName(questionResource));
    	}
    	return resourceTitle.toString();
    }
    
    /**
     * @param questionResource Resource
     * @return Valid resource's name
     */
    public String getValidResourceName(QuestionResource questionResource)
    {
    	// Get question
    	Question question=getQuestion();
    	
    	StringBuffer resourceName=new StringBuffer();
    	if (questionResource!=null)
    	{
    		boolean okResourceName=questionResource.getName()!=null && !questionResource.getName().equals("") && 
    			checkResourceName(question,questionResource.getName(),questionResource.getPosition(),false);
    		boolean okActiveQuestionResourceName=!okResourceName && 
    			questionResource.getPosition()==activeQuestionResourceIndex+1 && 
    			activeQuestionResourceName!=null && !activeQuestionResourceName.equals("") && 
    			checkResourceName(question,activeQuestionResourceName,activeQuestionResourceIndex+1,false);
    		if (okResourceName || okActiveQuestionResourceName)
    		{
        		resourceName.append(okResourceName?questionResource.getName():activeQuestionResourceName);
    		}
        	else
        	{
        		resourceName.append(localizationService.getLocalizedMessage("RESOURCE"));
        		resourceName.append(' ');
        		resourceName.append(questionResource.getPosition());
        	}
    	}
    	return resourceName.toString();
    }
    
    public String getAbbreviatedResourceName(String resourceName)
    {
    	if (!resourceName.equals(resourceNameToAbbreviate))
    	{
    		resourceNameToAbbreviate=resourceName;
    		abbreviatedResourceName=abbreviate(abbreviateWords(resourceName, 15),20);
    	}
    	return abbreviatedResourceName;
    }
    
    public boolean isResourceNameAbbreviated(String resourceName)
    {
    	return !resourceName.equals(getAbbreviatedResourceName(resourceName));
    }
    
	/**
	 * Abbreviates a string using ellipses.<br/><br/>
	 * Leading and trailing whitespace are ignored.<br/><br/>
	 * When abbreviating a string, this method will try to leave a whitespace between the last 
	 * non trimmed word and the ellipses if there is enough space.
	 * @param str String to abbreviate (if needed)
	 * @param maxLength Maximum length of the abbreviated string (but length of ellipses 
	 * is taking account when we need to abbreviate it)
	 * @return Same string if its length is less or equal than <i>maxLength</i> or abbreviated string
	 * otherwise
	 */
    public String abbreviate(String str,int maxLength)
    {
    	return StringUtils.abbreviate(str,maxLength);
    }
    
	/**
	 * Abbreviates words within a string using short ellipses.<br/><br/>
	 * @param str String to abbreviate words (if needed)
	 * @param maxWordLength Maximum length of the abbreviated words
	 * @return Same string but with large words abbreviated
	 */
	public String abbreviateWords(String str,int maxWordLength)
	{
		return StringUtils.abbreviateWords(str,maxWordLength);
	}
    
	/**
	 * @param text Text
	 * @return Same text but replacing '\n' characters with &lt;br/&gt; tags after escaping it 
	 * using HTML entities 
	 */
	public String breakText(String text)
	{
		return HtmlUtils.breakText(text);
	}
    
	/**
	 * @param text Text
	 * @param maxBreaks Maximum number of breaks allowed or 0 unlimited
	 * @return Same text but replacing '\n' characters with &lt;br/&gt; tags after escaping it 
	 * using HTML entities
	 */
	public String breakText(String text,int maxBreaks)
	{
		return HtmlUtils.breakText(text,maxBreaks);
	}
    
    /**
     * @return true if current resource selected is an image, otherwise false
     */
    public boolean isCurrentResourceSelectedImage()
    {
    	boolean isCurrentResSelImg=false;
    	if (getCurrentResource()!=null && getCurrentResource().getId()!=-1L)
    	{
    		ResourceBean resourceBean=new ResourceBean(getCurrentResource());
    		resourceBean.setResourcesService(resourcesService);
    		resourceBean.setUserSessionService(userSessionService);
    		isCurrentResSelImg=resourceBean.isImage();
    	}
    	return isCurrentResSelImg;
    }
    
	// Elimina una respuesta de la pregunta
    /**
     * @return true if it is allowed to remove an answer from question (answers>2), otherwise false
     */
    public boolean isRemoveAnswerEnabled()
    {
    	return getQuestion().getAnswers().size()>2;
    }
    
    /**
     * @return true if it is allowed to remove a draggable item from a "Drag & Drop" question (draggable items>1),
     * otherwise false
     */
    public boolean isRemoveDraggableItemEnabled()
    {
		//TODO de momento solo tenemos en cuenta el grupo 1, habra que cambiarlo mas adelante
    	Question question=getQuestion();
    	return question instanceof DragDropQuestion?
    		((DragDropQuestion)question).getDraggableItems(1).size()>1:false;
    }
    
    /**
     * @return true if it is allowed to remove an answer from a "Drag & Drop" question (answer>1), 
     * otherwise false
     */
    public boolean isRemoveDroppableAnswerEnabled()
    {
		//TODO de momento solo tenemos en cuenta el grupo 1, habra que cambiarlo mas adelante
    	Question question=getQuestion();
    	return question instanceof DragDropQuestion?
    		((DragDropQuestion)question).getDroppableAnswers(1).size()>1:false;
    }
    
    /**
     * Checks that user has entered a question name or displays an error message.
     * @param questionName Question name
     * @return true if user has entered a question name, false otherwise
     */
    private boolean checkNonEmptyQuestionName(String questionName)
    {
    	boolean ok=true;
    	if (questionName==null || questionName.equals(""))
    	{
    		addErrorMessage("QUESTION_NAME_REQUIRED");
    		ok=false;
    	}
    	return ok;
    }
    
	/**
	 * Checks that question name entered by user only includes valid characters or displays an error message.
     * @param questionName Question name
	 * @return true if question name only includes valid characters (letters, digits, whitespaces 
	 * or some of the following characters  _ ( ) [ ] { } + - * /<br/>
	 * ), false otherwise
	 */
    private boolean checkValidCharactersForQuestionName(String questionName)
    {
    	boolean ok=true;
    	if (StringUtils.hasUnexpectedCharacters(
        	questionName,true,true,true,new char[]{'_','(',')','[',']','{','}','+','-','*','/'}))
    	{
    		addErrorMessage("QUESTION_NAME_INVALID_CHARACTERS");
    		ok=false;
    	}
    	return ok;
    }
    
    /**
     * Check that question name entered by user includes at least one letter or displays an error message.
     * @param questionName Question name
     * @return true if question name includes at least one letter, false otherwise
     */
    private boolean checkLetterIncludedForQuestionName(String questionName)
    {
    	boolean ok=true;
    	if (!StringUtils.hasLetter(questionName))
    	{
    		addErrorMessage("QUESTION_NAME_WITHOUT_LETTER");
    		ok=false;
    	}
    	return ok;
    }
    
    /**
     * Check that first character of the question name entered by user is not a digit nor a whitespace 
     * or displays an error message.
     * @param questionName Question name
	 * @return true if first character of question name is not a digit nor a whitespace, false otherwise
     */
    private boolean checkFirstCharacterNotDigitNotWhitespaceForQuestionName(String questionName)
    {
    	boolean ok=true;
    	if (StringUtils.isFirstCharacterDigit(questionName) || StringUtils.isFirstCharacterWhitespace(questionName))
    	{
    		addErrorMessage("QUESTION_NAME_FIRST_CHARACTER_INVALID");
    		ok=false;
    	}
    	return ok;
    }
    
    /**
     * Check that last character of the question name entered by user is not a whitespace 
     * or displays an error message.
     * @param questionName Question name
	 * @return true if last character of question name is not a whitespace, false otherwise
     */
    private boolean checkLastCharacterNotWhitespaceForQuestionName(String questionName)
    {
    	boolean ok=true;
    	if (StringUtils.isLastCharacterWhitespace(questionName))
    	{
    		addErrorMessage("QUESTION_NAME_LAST_CHARACTER_INVALID");
    		ok=false;
    	}
    	return ok;
    }
    
    /**
     * Check that question name entered by user does not include consecutive whitespaces 
     * or displays an error message.
     * @param questionName Question name
	 * @return true if question name does not include consecutive whitespaces, false otherwise
     */
    private boolean checkNonConsecutiveWhitespacesForQuestionName(String questionName)
    {
    	boolean ok=true;
    	if (StringUtils.hasConsecutiveWhitespaces(questionName))
    	{
    		addErrorMessage("QUESTION_NAME_WITH_CONSECUTIVE_WHITESPACES");
    		ok=false;
    	}
    	return ok;
    }
    
    /**
     * Checks that question name entered by user is valid or displays error messages indicating the causes.
     * @param questionName Question name
     * @return true if question name entered by user is valid, false otherwise
     */
    private boolean checkQuestionName(String questionName)
    {
    	boolean ok=checkNonEmptyQuestionName(questionName);
    	if (ok)
    	{
    		if (!checkValidCharactersForQuestionName(questionName))
    		{
    			ok=false;
    		}
    		if (!checkLetterIncludedForQuestionName(questionName))
    		{
    			ok=false;
    		}
    		if (!checkFirstCharacterNotDigitNotWhitespaceForQuestionName(questionName))
    		{
    			ok=false;
    		}
    		if (!checkLastCharacterNotWhitespaceForQuestionName(questionName))
    		{
    			ok=false;
    		}
    		if (!checkNonConsecutiveWhitespacesForQuestionName(questionName))
    		{
    			ok=false;
    		}
    	}
    	return ok;
    }
    
    private boolean checkCommonDataInputFields(Operation operation)
    {
    	boolean ok=true;
    	
		// Get current user session Hibernate operation
		operation=getCurrentUserOperation(operation);
    	
		// Get Question
		Question question=getQuestion();
		
    	// Check question name
    	ok=checkQuestionName(question.getName());
    	
    	// Check category
		if (!categoriesService.checkCategoryId(categoryId))
		{
    		addErrorMessage("QUESTION_CATEGORY_STEP_NOT_FOUND");
			ok=false;
    		
    		// Refresh question categories from DB
			resetQuestionsCategories(operation);
		}
		else if (checkCategory(operation))
    	{
    		if (!checkAvailableQuestionName(operation))
    		{
    			addErrorMessage("QUESTION_NAME_ALREADY_DECLARED");
    			ok=false;
    		}
    	}
    	else
    	{
    		addErrorMessage("QUESTION_CATEGORY_NOT_GRANTED_ERROR");
    		ok=false;
    		
    		// Refresh question categories from DB
			resetQuestionsCategories(operation);
    	}
    	return ok;
    }
    
	/**
	 * @param operation Operation
	 * @return true if category selected is usable by current user, false otherwise
	 */
    private boolean checkCategory(Operation operation)
    {
    	// Get current user session Hibernate operation
    	operation=getCurrentUserOperation(operation);
    	
    	return checkCategory(operation,getCategory(operation));
    }
    
	/**
	 * @param operation Operation
	 * @param category Category
	 * @return true if a category is usable by current user, false otherwise
	 */
    private boolean checkCategory(Operation operation,Category category)
    {
    	// Get current user session Hibernate operation
    	operation=getCurrentUserOperation(operation);
    	
    	// Check category type
    	boolean ok=category!=null && categoryTypesService.isDerivedFrom(operation,
    		categoryTypesService.getCategoryType(operation,"CATEGORY_TYPE_IMAGES"),
    		categoryTypesService.getCategoryTypeFromCategoryId(operation,category.getId()));
    	
    	// Check visibility
    	if (ok)
    	{
    		ok=false;
    		Question question=getQuestion();
    		User questionAuthor=
    			question.getId()>0L?question.getCreatedBy():userSessionService.getCurrentUser(operation);
    		Visibility categoryVisibility=
    			visibilitiesService.getVisibilityFromCategoryId(operation,category.getId());
    		if (categoryVisibility.isGlobal())
    		{
    			ok=getFilterGlobalQuestionsEnabled(operation).booleanValue() && 
    				((initialCategoryId>0L && category.getId()==initialCategoryId) || 
    				questionAuthor.equals(category.getUser()) || 
    				getGlobalOtherUserCategoryAllowed(operation).booleanValue());
    		}
    		else if (questionAuthor.equals(category.getUser()))
    		{
    			if (questionAuthor.getId()==userSessionService.getCurrentUserId())
    			{
    				ok=true;
    			}
    			else if (getFilterOtherUsersQuestionsEnabled(operation).booleanValue())
    			{
    				if (categoryVisibility.getLevel()>=visibilitiesService.getVisibility(
    					operation,"CATEGORY_VISIBILITY_PRIVATE").getLevel())
    				{
    					ok=getViewQuestionsFromOtherUsersPrivateCategoriesEnabled(operation).booleanValue() && 
    						(!getQuestionAuthorAdmin(operation).booleanValue() || 
    						getViewQuestionsFromAdminsPrivateCategoriesEnabled(operation).booleanValue()) && 
    						(!getQuestionAuthorSuperadmin(operation).booleanValue() || 
    						getViewQuestionsFromSuperadminsPrivateCategoriesEnabled(operation).booleanValue());
    				}
    				else
    				{
    					ok=true;
    				}
    			}
    		}
    	}
    	return ok;
    }
    
	/**
	 * @param operation Operation
	 * @return true if current category of the question we are editing is usable by current user, false otherwise
	 */
    private boolean checkCurrentCategory(Operation operation)
    {
    	// Get current user session Hibernate operation
    	operation=getCurrentUserOperation(operation);
    	
    	return checkCategory(
    		operation,categoriesService.getCategoryFromQuestionId(operation,getQuestion().getId()));
    }
    
    /**
     * @param operation Operation
	 * @return true if question name entered by user is available, false otherwise 
	 */
	private boolean checkAvailableQuestionName(Operation operation)
	{
		boolean ok=true;
		
		// Get current user session Hibernate operation
		operation=getCurrentUserOperation(operation);
		
		Question question=getQuestion();
		String questionName=question.getName();
		long categoryId=question.getCategory()==null?0L:this.categoryId;
		if (categoryId>0L && questionName!=null)
		{
			ok=questionsService.isQuestionNameAvailable(operation,questionName,categoryId,question.getId());
		}
		return ok;
	}
    
    private boolean checkAnswersFields()
    {
    	boolean ok=true;
    	if (getQuestion() instanceof MultichoiceQuestion)
    	{
    		ok=checkMultichoiceAnswersFields();
    	}
    	return ok;
    }
    
    private boolean checkMultichoiceAnswersFields()
    {
		boolean ok=true;
    	int correctAnswers=0;
		for (Answer answer:getQuestion().getAnswers()) // We count right answers
		{
			if (answer.getCorrect())
			{
				correctAnswers++;
			}
		}
		//TODO ¿esta bien mantener esto como un error? ¿o sería mejor que nos diera simplemente una advertencia?
		if (correctAnswers == 0) // There are no right answers
		{
			ok=false;
			addErrorMessage("RIGHT_ANSWER_REQUIRED");
		}
		// TODO ¿tiene sentido esta restriccion? es decir, ¿realmente queremos obligar a que solo haya una posible respuesta correcta en las respuestas de tipo radiobutton?
		else if (correctAnswers > 1 && ((MultichoiceQuestion)question).isSingle())	
			// Demasiadas respuestas correctas
		{
			ok=false;
			addErrorMessage("SEVERAL_RIGTH_ANSWERS_NOT_ALLOWED");
		}
    	return ok;
    }
    
	/**
	 * Checks that current answer name entered by user only includes valid characters or displays an error message.
     * @param answerName Current answer name
     * @param displayError true to display error message, false otherwise
	 * @return true if current answer name only includes valid characters (letters, digits, whitespaces 
	 * or some of the following characters  _ + - * / /<br/>
	 * ), false otherwise
	 */
    private boolean checkValidCharactersForAnswerName(String answerName,boolean displayError)
    {
    	boolean ok=true;
    	if (StringUtils.hasUnexpectedCharacters(answerName,true,true,true,new char[]{'_','+','-','*','/'}))
    	{
    		if (displayError)
    		{
    			addErrorMessage("ANSWER_NAME_INVALID_CHARACTERS");
    		}
    		ok=false;
    	}
    	return ok;
    }
    
    /**
     * Check that first character of the current answer name entered by user is not a whitespace 
     * or displays an error message.
     * @param answerName Current answer name
     * @param displayError true to display error message, false otherwise
	 * @return true if first character of current answer name is not a whitespace, false otherwise
     */
    private boolean checkFirstCharacterNotWhitespaceForAnswerName(String answerName,boolean displayError)
    {
    	boolean ok=true;
    	if (StringUtils.isFirstCharacterWhitespace(answerName))
    	{
    		if (displayError)
    		{
    			addErrorMessage("ANSWER_NAME_FIRST_CHARACTER_INVALID");
    		}
    		ok=false;
    	}
    	return ok;
    }
    
    /**
     * Check that last character of the current answer name entered by user is not a whitespace 
     * or displays an error message.
     * @param answerName Current answer name
     * @param displayError true to display error message, false otherwise
	 * @return true if last character of current answer name is not a whitespace, false otherwise
     */
    private boolean checkLastCharacterNotWhitespaceForAnswerName(String answerName,boolean displayError)
    {
    	boolean ok=true;
    	if (StringUtils.isLastCharacterWhitespace(answerName))
    	{
    		if (displayError)
    		{
    			addErrorMessage("ANSWER_NAME_LAST_CHARACTER_INVALID");
    		}
    		ok=false;
    	}
    	return ok;
    }
    
    /**
     * Check that current answer name entered by user does not include consecutive whitespaces 
     * or displays an error message.
     * @param answerName Current answer name
     * @param displayError true to display error message, false otherwise
	 * @return true if current answer name does not include consecutive whitespaces, false otherwise
     */
    private boolean checkNonConsecutiveWhitespacesForAnswerName(String answerName,boolean displayError)
    {
    	boolean ok=true;
    	if (StringUtils.hasConsecutiveWhitespaces(answerName))
    	{
    		if (displayError)
    		{
    			addErrorMessage("ANSWER_NAME_WITH_CONSECUTIVE_WHITESPACES");
    		}
    		ok=false;
    	}
    	return ok;
    }
    
    /**
     * Check that current answer name entered by user is valid or displays error messages indicating the causes if
     * desired.
     * @param answerName Current answer name
     * @param displayErrors true to display error messages, false otherwise
     * @return true if current answer name entered by user is valid, false otherwise
     */
    private boolean checkAnswerName(String answerName,boolean displayErrors)
    {
    	boolean ok=true;
    	if (displayErrors)
    	{
       		if (!checkValidCharactersForAnswerName(answerName,true))
       		{
       			ok=false;
       		}
       		if (!checkFirstCharacterNotWhitespaceForAnswerName(answerName,true))
       		{
       			ok=false;
       		}
       		if (!checkLastCharacterNotWhitespaceForAnswerName(answerName,true))
       		{
       			ok=false;
       		}
       		if (!checkNonConsecutiveWhitespacesForAnswerName(answerName,true))
       		{
       			ok=false;
       		}
    	}
    	else
    	{
    		ok=checkValidCharactersForAnswerName(answerName,false) && 
    			checkFirstCharacterNotWhitespaceForAnswerName(answerName,false) &&
    			checkLastCharacterNotWhitespaceForAnswerName(answerName,false) &&
    			checkNonConsecutiveWhitespacesForAnswerName(answerName,false);
    	}
    	return ok;
    }
    
    /**
     * Checks that current answer name entered by user is valid or displays error messages indicating the causes.
     * @param answerName Current answer name
     * @return true if current answer name entered by user is valid, false otherwise
     */
    private boolean checkAnswerName(String answerName)
    {
    	return checkAnswerName(answerName,true);
    }
    
	/**
	 * Checks that current draggable item name entered by user only includes valid characters 
	 * or displays an error message.
     * @param draggableItemName Current draggable item name
     * @param displayError true to display error message, false otherwise
	 * @return true if current draggable item name only includes valid characters (letters, digits, whitespaces 
	 * or some of the following characters  _ + - * / /<br/>
	 * ), false otherwise
	 */
    private boolean checkValidCharactersForDraggableItemName(String draggableItemName,boolean displayError)
    {
    	boolean ok=true;
    	if (StringUtils.hasUnexpectedCharacters(draggableItemName,true,true,true,new char[]{'_','+','-','*','/'}))
    	{
    		if (displayError)
    		{
    			addErrorMessage("DRAGGABLE_ITEM_NAME_INVALID_CHARACTERS");
    		}
    		ok=false;
    	}
    	return ok;
    }
    
    /**
     * Check that first character of the current draggable item name entered by user is not a whitespace 
     * or displays an error message.
     * @param draggableItemName Current draggable item name
     * @param displayError true to display error message, false otherwise
	 * @return true if first character of current draggable item name is not a whitespace, false otherwise
     */
    private boolean checkFirstCharacterNotWhitespaceForDraggableItemName(String draggableItemName,
    	boolean displayError)
    {
    	boolean ok=true;
    	if (StringUtils.isFirstCharacterWhitespace(draggableItemName))
    	{
    		if (displayError)
    		{
    			addErrorMessage("DRAGGABLE_ITEM_NAME_FIRST_CHARACTER_INVALID");
    		}
    		ok=false;
    	}
    	return ok;
    }
    
    /**
     * Check that last character of the current draggable item name entered by user is not a whitespace 
     * or displays an error message.
     * @param draggableItemName Current draggable item name
     * @param displayError true to display error message, false otherwise
	 * @return true if last character of current draggable item name is not a whitespace, false otherwise
     */
    private boolean checkLastCharacterNotWhitespaceForDraggableItemName(String draggableItemName,
    	boolean displayError)
    {
    	boolean ok=true;
    	if (StringUtils.isLastCharacterWhitespace(draggableItemName))
    	{
    		if (displayError)
    		{
    			addErrorMessage("DRAGGABLE_ITEM_NAME_LAST_CHARACTER_INVALID");
    		}
    		ok=false;
    	}
    	return ok;
    }
    
    /**
     * Check that current draggable item name entered by user does not include consecutive whitespaces 
     * or displays an error message.
     * @param draggableItemName Current draggable item name
     * @param displayError true to display error message, false otherwise
	 * @return true if current draggable item name does not include consecutive whitespaces, false otherwise
     */
    private boolean checkNonConsecutiveWhitespacesForDraggableItemName(String draggableItemName,
    	boolean displayError)
    {
    	boolean ok=true;
    	if (StringUtils.hasConsecutiveWhitespaces(draggableItemName))
    	{
    		addErrorMessage("DRAGGABLE_ITEM_NAME_WITH_CONSECUTIVE_WHITESPACES");
    		ok=false;
    	}
    	return ok;
    }
    
    /**
     * Check that current draggable item name entered by user is valid or displays error messages indicating 
     * the causes if desired.
     * @param draggableItemName Current draggable item name
     * @param displayErrors true to display error messages, false otherwise
     * @return true if current draggable item name entered by user is valid, false otherwise
     */
    private boolean checkDraggableItemName(String draggableItemName,boolean displayErrors)
    {
    	boolean ok=true;
    	if (displayErrors)
    	{
       		if (!checkValidCharactersForDraggableItemName(draggableItemName,true))
       		{
       			ok=false;
       		}
       		if (!checkFirstCharacterNotWhitespaceForDraggableItemName(draggableItemName,true))
       		{
       			ok=false;
       		}
       		if (!checkLastCharacterNotWhitespaceForDraggableItemName(draggableItemName,true))
       		{
       			ok=false;
       		}
       		if (!checkNonConsecutiveWhitespacesForDraggableItemName(draggableItemName,true))
       		{
       			ok=false;
       		}
    	}
    	else
    	{
    		ok=checkValidCharactersForDraggableItemName(draggableItemName,false) && 
    			checkFirstCharacterNotWhitespaceForDraggableItemName(draggableItemName,false) &&
    			checkLastCharacterNotWhitespaceForDraggableItemName(draggableItemName,false) &&
    			checkNonConsecutiveWhitespacesForDraggableItemName(draggableItemName,false);
    	}
    	return ok;
    }
    
    /**
     * Check that current draggable item name entered by user is valid 
     * or displays error messages indicating the causes.
     * @param draggableItemName Current draggable item name
     * @return true if current draggable item name entered by user is valid, false otherwise
     */
    private boolean checkDraggableItemName(String draggableItemName)
    {
    	return checkDraggableItemName(draggableItemName,true);
    }
    
    /**
     * Check that xml content entered by user is a valid XML document or displays an error message indicating 
     * the error found if desired.
     * @param xmlContent Xml content
     * @param displayErrors true to display error messages, false otherwise
     * @return true if xml content entered by user is a valid XML document, false otherwise
     */
    private boolean checkXmlContent(String xmlContent,boolean displayErrors)
    {
		SAXLocalizationErrorReporter errorReporter=null;
    	boolean ok=true;
    	try
    	{
    		// We use SAX to validate xml content
    		SAXParserFactory factory=SAXParserFactory.newInstance();
    		SAXParser parser=factory.newSAXParser();
    		
    		// We need to define an special error reporter to get localized error messages
    		StringBuffer errorReporterId=new StringBuffer(Constants.XERCES_PROPERTY_PREFIX);
    		errorReporterId.append(Constants.ERROR_REPORTER_PROPERTY);
    		errorReporter=new SAXLocalizationErrorReporter();
    		parser.setProperty(errorReporterId.toString(),errorReporter);
    		
    		// We get XML reader
    		XMLReader reader=parser.getXMLReader();
    		
    		// We parse xml content... if it is not a valid xml document will throw an exception 
    		reader.parse(new InputSource(new StringReader(xmlContent)));
    	}
    	catch (Exception e)
    	{
    		ok=false;
    		if (displayErrors)
    		{
    			StringBuffer errorMessage=
    				new StringBuffer(localizationService.getLocalizedMessage("XML_CONTENT_XML_INVALID"));
    			String localizedXMLError=errorReporter.getLocalizedErrorMessage();
    			if (localizedXMLError==null)
    			{
    				errorMessage.append('.');
    			}
    			else
    			{
    				errorMessage.append(": ");
    				errorMessage.append(localizedXMLError);
    			}
            	addPlainErrorMessage(errorMessage.toString());
    		}
    	}
    	return ok;
    }
    
    /**
     * Check that xml content entered by user is a valid XML document or displays an error message indicating 
     * the error found.
     * @param xmlContent Xml content
     * @return true if xml content entered by user is a valid XML document, false otherwise
     */
    private boolean checkXmlContent(String xmlContent)
    {
    	return checkXmlContent(xmlContent,true);
    }
    
	/**
	 * Checks that current resource name entered by user only includes valid characters or 
	 * displays an error message if desired.
     * @param questionResourceName Current resource name
     * @param displayError true to display error message, false otherwise
	 * @return true if current resource name only includes valid characters (letters, digits, whitespaces or 
	 * some of the following characters  _ + - * / /<br/>
	 * ), false otherwise
	 */
    private boolean checkValidCharactersForResourceName(String questionResourceName,boolean displayError)
    {
    	boolean ok=true;
    	if (StringUtils.hasUnexpectedCharacters(
    		questionResourceName,true,true,true,new char[]{'_','+','-','*','/'}))
    	{
    		if (displayError)
    		{
    			addErrorMessage("RESOURCE_NAME_INVALID_CHARACTERS");
    		}
    		ok=false;
    	}
    	return ok;
    }
    
    /**
     * Check that first character of the current resource name entered by user is not a whitespace 
     * or displays an error message.
     * @param questionResourceName Current resource name
     * @param displayError true to display error message, false otherwise
	 * @return true if first character of current resource name is not a whitespace, false otherwise
     */
    private boolean checkFirstCharacterNotWhitespaceForResourceName(String questionResourceName,
    	boolean displayError)
    {
    	boolean ok=true;
    	if (StringUtils.isFirstCharacterWhitespace(questionResourceName))
    	{
    		if (displayError)
    		{
    			addErrorMessage("RESOURCE_NAME_FIRST_CHARACTER_INVALID");
    		}
    		ok=false;
    	}
    	return ok;
    }
    
    /**
     * Check that last character of the current resource name entered by user is not a whitespace 
     * or displays an error message.
     * @param questionResourceName Current resource name
     * @param displayError true to display error message, false otherwise
	 * @return true if last character of current question resource name is not a whitespace, false otherwise
     */
    private boolean checkLastCharacterNotWhitespaceForResourceName(String questionResourceName,
    	boolean displayError)
    {
    	boolean ok=true;
    	if (StringUtils.isLastCharacterWhitespace(questionResourceName))
    	{
    		if (displayError)
    		{
    			addErrorMessage("RESOURCE_NAME_LAST_CHARACTER_INVALID");
    		}
    		ok=false;
    	}
    	return ok;
    }
    
    /**
     * Check that current resource name entered by user does not include consecutive whitespaces 
     * or displays an error message.
     * @param questionResourceName Current resource name
     * @param displayError true to display error message, false otherwise
	 * @return true if current resource name does not include consecutive whitespaces, false otherwise
     */
    private boolean checkNonConsecutiveWhitespacesForResourceName(String questionResourceName,
    	boolean displayError)
    {
    	boolean ok=true;
    	if (StringUtils.hasConsecutiveWhitespaces(questionResourceName))
    	{
    		if (displayError)
    		{
    			addErrorMessage("RESOURCE_NAME_WITH_CONSECUTIVE_WHITESPACES");
    		}
    		ok=false;
    	}
    	return ok;
    }
    
    /**
     * Check that current resource name entered by user is valid or displays error messages indicating 
     * the causes if desired.
     * @param questionResourceName Current resource name
     * @param displayErrors true to display error messages, false otherwise
     * @return true if current resource name entered by user is valid, false otherwise
     */
    private boolean checkValidResourceName(String questionResourceName,boolean displayErrors)
    {
    	boolean ok=true;
    	if (displayErrors)
    	{
       		if (!checkValidCharactersForResourceName(questionResourceName,true))
       		{
       			ok=false;
       		}
       		if (!checkFirstCharacterNotWhitespaceForResourceName(questionResourceName,true))
       		{
       			ok=false;
       		}
       		if (!checkLastCharacterNotWhitespaceForResourceName(questionResourceName,true))
       		{
       			ok=false;
       		}
       		if (!checkNonConsecutiveWhitespacesForResourceName(questionResourceName,true))
       		{
       			ok=false;
       		}
    	}
    	else
    	{
    		ok=checkValidCharactersForResourceName(questionResourceName,false) && 
    			checkFirstCharacterNotWhitespaceForResourceName(questionResourceName,false) &&
    			checkLastCharacterNotWhitespaceForResourceName(questionResourceName,false) &&
    			checkNonConsecutiveWhitespacesForResourceName(questionResourceName,false);
    	}
    	return ok;
    }
    
    /**
     * Checks that current resource name entered by user is available or displays a error message if desired
     * @param question Question
     * @param questionResourceName Current resource name
     * @param position Current resource position
     * @param displayError true to display error message, false otherwise
     * @return true if current resource name entered by user is available, false otherwise
     */
    private boolean checkResourceNameAvailable(Question question,String questionResourceName,int position,
    	boolean displayError)
    {
    	boolean ok=true;
    	if (questionResourceName!=null && !"".equals(questionResourceName))
    	{
        	for (QuestionResource questionResource:question.getQuestionResources())
        	{
        		if (questionResource.getPosition()!=position && 
        			questionResourceName.equals(questionResource.getName()))
        		{
        			if (displayError)
        			{
        				addErrorMessage("QUESTION_RESOURCE_NAME_ALREADY_DECLARED");
        			}
        			ok=false;
        			break;
        		}
        	}
    	}
    	return ok;
    }
    
    /**
     * Checks that current resource name entered by user is valid and available, or displays error messages 
     * indicating the causes if desired.
     * @param question Question
     * @param questionResourceName Current resource name
     * @param position Current resource position
     * @param displayErrors true to display error messages, false otherwise
     * @return true if current resource name entered by user is available, false otherwise
     */
    private boolean checkResourceName(Question question,String questionResourceName,int position,
    	boolean displayErrors)
    {
    	boolean ok=true;
    	if (displayErrors)
    	{
    		if (!checkValidResourceName(questionResourceName,true))
    		{
    			ok=false;
    		}
    		if (!checkResourceNameAvailable(question,questionResourceName,position,true))
    		{
    			ok=false;
    		}
    	}
    	else
    	{
    		ok=checkValidResourceName(questionResourceName,false) && 
    			checkResourceNameAvailable(question,questionResourceName,position,false);
    	}
    	return ok;
    }
    
    /**
     * Checks that current resource name entered by user is valid and available, or displays error messages 
     * indicating the causes.
     * @param question Question
     * @param questionResourceName Current resource name
     * @param position Current resource position
     * @return true if current resource name entered by user is available, false otherwise
     */
    private boolean checkResourceName(Question question,String questionResourceName,int position)
    {
    	return checkResourceName(question,questionResourceName,position,true);
    }
    
    /**
     * Checks that current resource selected by user has a non empty image assigned, or display a error message
     * if desired.
     * @param resourceId Resource identifier
     * @param displayError true to display error message, false otherwise
     * @return true if current resource selected by user has a non empty image assigned, false otherwise
     */
    private boolean checkResourceSelectedResource(long resourceId,boolean displayError)
    {
    	boolean ok=true;
    	if (resourceId==-1L)
    	{
    		if (displayError)
    		{
    			addErrorMessage("QUESTION_RESOURCE_NOT_SELECTED_IMAGE");
    		}
    		ok=false;
    	}
    	return ok;
    }
    
    /**
     * Checks that current resource selected by user is valid, or display error messages indicating the causes
     * if desired.
     * @param question Question
     * @param questionResource Current resource
     * @param displayErrors true to display error messages, false otherwise
     * @return
     */
    private boolean checkQuestionResource(Question question,QuestionResource questionResource,
    	boolean displayErrors)
    {
    	boolean ok=true;
    	if (displayErrors)
    	{
    		if (!checkResourceName(question,questionResource.getName(),questionResource.getPosition(),true))
    		{
    			ok=false;
    		}
    		if (!checkResourceSelectedResource(questionResource.getResource().getId(),true))
    		{
    			ok=false;
    		}
    	}
    	else
    	{
    		ok=checkResourceName(question,questionResource.getName(),questionResource.getPosition(),false) &&
    			checkResourceSelectedResource(questionResource.getResource().getId(),false);
    	}
    	return ok;
    }
    
    /**
     * Checks that current resource selected by user is valid, or display error messages indicating the causes.
     * @param question Question
     * @param questionResource Current resource
     * @return
     */
    private boolean checkQuestionResource(Question question,QuestionResource questionResource)
    {
    	return checkQuestionResource(question,questionResource,true);
    }
    
    
	/**
	 * Refresh available categories of the combo box.
	 * @param event Action event
	 */
	public void refreshQuestionCategories(ActionEvent event)
	{
		// Get current user session operation
		Operation operation=getCurrentUserOperation(null);
		
		setFilterGlobalQuestionsEnabled(null);
		setFilterOtherUsersQuestionsEnabled(null);
		setGlobalOtherUserCategoryAllowed(null);
		resetQuestionAuthorAdmin();
		resetQuestionAuthorSuperadmin();
		setViewQuestionsFromOtherUsersPrivateCategoriesEnabled(null);
		setViewQuestionsFromAdminsPrivateCategoriesEnabled(null);
		setViewQuestionsFromSuperadminsPrivateCategoriesEnabled(null);
		if (!categoriesService.checkCategoryId(operation,getCategoryId()) || !checkCategory(operation))
		{
			// Refresh questions categories from DB
			resetQuestionsCategories(operation);
		}
		else
		{
			// Reload questions categories from DB
			setQuestionsCategories(null);
		}
	}
    
	private void resetQuestionsCategories(Operation operation)
	{
		// Get current user session operation
		operation=getCurrentUserOperation(operation);
		
		// Reload questions categories from DB
		setQuestionsCategories(null);
		
		// Check that initial category already exists and it is valid
		long resetCategoryId=0L;
		if (initialCategoryId>0L)
		{
			if (!categoriesService.checkCategoryId(operation,initialCategoryId))
			{
				initialCategoryId=0L;
			}
			else if (checkCategory(operation,categoriesService.getCategory(operation,initialCategoryId)))
			{
				resetCategoryId=initialCategoryId;
			}
		}
		
		// Reset selected category
		if (resetCategoryId==0L)
		{
			List<Category> questionsCategories=getQuestionsCategories(operation);
			if (!questionsCategories.isEmpty())
			{
				resetCategoryId=questionsCategories.get(0).getId();
			}
		}
		setCategoryId(operation,resetCategoryId);
	}
	
	/**
	 * Refresh available categories of the combo box within the 'Select image' dialog.
	 * @param event Action event
	 */
	public void refreshImagesCategories(ActionEvent event)
	{
		// Get current user session operation
		Operation operation=getCurrentUserOperation(null);
		
		setFilterGlobalResourcesEnabled(null);
		setFilterOtherUsersResourcesEnabled(null);
		setUseGlobalResources(null);
		setUseOtherUsersResources(null);
	    setViewResourcesFromOtherUsersPrivateCategoriesEnabled(null);
	    setViewResourcesFromAdminsPrivateCategoriesEnabled(null);
	    setViewResourcesFromSuperadminsPrivateCategoriesEnabled(null);
	    resetAdmins();
	    resetSuperadmins();
	    
	    Category filterCategory=null;
	    long filterCategoryId=getFilterCategoryId(operation);
	    if (filterCategoryId>0L)
	    {
	    	if (categoriesService.checkCategoryId(operation,filterCategoryId))
	    	{
	    		filterCategory=categoriesService.getCategory(operation,filterCategoryId);
	    	}
	    }
	    else
	    {
	    	filterCategory=new Category();
	    	filterCategory.setId(filterCategoryId);
	    }
	    if (filterCategory==null || !checkImagesFilterPermission(operation,filterCategory))
	    {
	    	setFilterCategoryId(Long.MIN_VALUE);
	    }
	    
		// Reload images categories from DB
		setImagesCategories(null);
	}
	
    /**
     * Flow listener to handle a step change within wizard component. 
     * @param event Flow event
     * @return Next step name
     */
    public String changeStep(FlowEvent event)
    {
    	boolean ok=true;
    	String oldStep=event.getOldStep();
    	String nextStep=event.getNewStep();
    	
		// Get current user session operation
		Operation operation=getCurrentUserOperation(null);
    	
    	if (GENERAL_WIZARD_TAB.equals(oldStep))
    	{
    		setFilterGlobalQuestionsEnabled(null);
    		setFilterOtherUsersQuestionsEnabled(null);
            setGlobalOtherUserCategoryAllowed(null);
            resetQuestionAuthorAdmin();
            resetQuestionAuthorSuperadmin();
            setViewQuestionsFromOtherUsersPrivateCategoriesEnabled(null);
            setViewQuestionsFromAdminsPrivateCategoriesEnabled(null);
            setViewQuestionsFromSuperadminsPrivateCategoriesEnabled(null);
            
        	ok=checkCommonDataInputFields(operation);
        	if (ok && ANSWERS_WIZARD_TAB.equals(nextStep))
        	{
        		Question question=getQuestion();
        		if (question instanceof MultichoiceQuestion)
        		{
            		// Get current answer
            		Answer currentAnswer=getActiveAnswer(event.getComponent());
        			
            		// Restore old answer name
            		currentAnswer.setName(activeAnswerName);
        		}
        		else if (question instanceof DragDropQuestion)
        		{
        			// Get current draggable item
        			Answer currentDraggableItem=getActiveDraggableItem(event.getComponent());
        			
        			// Restore old draggable item name
        			currentDraggableItem.setName(activeDraggableItemName);
        			
            		// Get current answer
            		Answer currentAnswer=getActiveDroppableAnswer(event.getComponent());
        			
        			// Restore old answer name
           			currentAnswer.setName(activeAnswerName);
       			}
       		}
    	}
    	else if (ANSWERS_WIZARD_TAB.equals(oldStep))
    	{
    		boolean errors=false;
    		boolean displayErrors=isResourcesTabDisplayed()?
    			RESOURCES_WIZARD_TAB.equals(nextStep):FEEDBACK_WIZARD_TAB.equals(nextStep);
       		Question question=getQuestion();
    		if (question instanceof MultichoiceQuestion)
    		{
    			// Get current answer
    			Answer currentAnswer=getActiveAnswer(event.getComponent());
    			
    			// Check that current answer name entered by user is valid
    			if (checkAnswerName(currentAnswer.getName(),displayErrors))
    			{
    				activeAnswerName=currentAnswer.getName();
    			}
    			else
    			{
    				errors=true;
    				if (displayErrors)
    				{
    					// Restore old answer tab without changing its name
    					updateAnswersTextFields(event.getComponent(),question.getAnswers().size());
    					currentAnswer.setName(activeAnswerName);
    				}
    			}
    		}
    		else if (question instanceof DragDropQuestion)
    		{
    			// Get current draggable item
    			Answer currentDraggableItem=getActiveDraggableItem(event.getComponent());
    			
    			// Get current answer
    			Answer currentAnswer=getActiveDroppableAnswer(event.getComponent());
    			
    			// Check that current draggable item and answer names entered by user are valid
    			if (!checkDraggableItemName(currentDraggableItem.getName(),displayErrors))
    			{
    				errors=true;
    			}
    			if (!checkAnswerName(currentAnswer.getName(),displayErrors))
    			{
    				errors=true;
    			}
    			if (errors)
    			{
    				DragDropQuestion dragDropQuestion=(DragDropQuestion)question;
    				
    				// Restore old draggable item without changing its name
    				//TODO de momento solo tenemos en cuenta el grupo 1, habra que cambiarlo mas adelante
    				updateDraggableItemsTextFields(
    					event.getComponent(),dragDropQuestion.getDraggableItems(1).size());
    				currentDraggableItem.setName(activeDraggableItemName);
    				
					// Restore old answer tab without changing its name
    				//TODO de momento solo tenemos en cuenta el grupo 1, habra que cambiarlo mas adelante
    				updateAnswersTextFields(event.getComponent(),dragDropQuestion.getDroppableAnswers(1).size());
					currentAnswer.setName(activeAnswerName);
    			}
    			else
    			{
    				activeDraggableItemName=currentDraggableItem.getName();
    				activeAnswerName=currentAnswer.getName();
    			}
    		}
    		else if (question instanceof OmXmlQuestion)
    		{
    			// Check that xml content is a valid xml document
    			errors=!checkXmlContent(((OmXmlQuestion)question).getXmlContent(),displayErrors);
    		}
    		// Check answers fields
    		if (displayErrors && !checkAnswersFields())
    		{
    			errors=true;
    		}
    		
    		ok=!displayErrors || !errors;
    		
    		if (ok && RESOURCES_WIZARD_TAB.equals(nextStep))
    		{
       			// Get current resource
       			QuestionResource currentQuestionResource=getActiveQuestionResource(event.getComponent());
       			
       			// Restore old resource name
       			if (currentQuestionResource!=null)
       			{
       				currentQuestionResource.setName(activeQuestionResourceName);
      			}
    		}
    	}
    	else if (RESOURCES_WIZARD_TAB.equals(oldStep))
    	{
    		// Get question
    		Question question=getQuestion();
    		
    		boolean errors=false;
    		boolean displayErrors=FEEDBACK_WIZARD_TAB.equals(nextStep);
    		
			// Get current resource
    		QuestionResource currentQuestionResource=getActiveQuestionResource(event.getComponent());
			
    		if (currentQuestionResource!=null)
    		{
    			// Check that current resource is valid
    			if (checkQuestionResource(question,currentQuestionResource,displayErrors))
    			{
    				activeQuestionResourceName=currentQuestionResource.getName();
    			}
    			else
    			{
    				errors=true;
        			if (displayErrors)
        			{
       					// Restore old answer tab without changing its name
       					updateResourcesTextFields(event.getComponent(),question.getQuestionResources().size());
       					currentQuestionResource.setName(activeQuestionResourceName);
        			}
    			}
    		}
    		
    		ok=!displayErrors || !errors;
    	}
    	if (!ok)
    	{
    		nextStep=oldStep;
    		
    		// Reset user permissions
    		setAddEnabled(null);
    		setEditEnabled(null);
    		setEditOtherUsersQuestionsEnabled(null);
    		setEditAdminsQuestionsEnabled(null);
    		setEditSuperadminsQuestionsEnabled(null);
    		setFilterGlobalResourcesEnabled(null);
    		setFilterOtherUsersResourcesEnabled(null);
    		setUseGlobalResources(null);
    		setUseOtherUsersResources(null);
    		setViewResourcesFromOtherUsersPrivateCategoriesEnabled(null);
    		setViewResourcesFromAdminsPrivateCategoriesEnabled(null);
    		setViewResourcesFromSuperadminsPrivateCategoriesEnabled(null);
    		resetAdmins();
    		resetSuperadmins();
    		
			// Scroll page to top position
			scrollToTop();
    	}
    	activeQuestionTabName=nextStep;
    	if (ok)
    	{
    		if (GENERAL_WIZARD_TAB.equals(nextStep))
    		{
    			setFilterGlobalQuestionsEnabled(null);
    			setFilterOtherUsersQuestionsEnabled(null);
    			setGlobalOtherUserCategoryAllowed(null);
    			resetQuestionAuthorAdmin();
    			resetQuestionAuthorSuperadmin();
    			setViewQuestionsFromOtherUsersPrivateCategoriesEnabled(null);
    			setViewQuestionsFromAdminsPrivateCategoriesEnabled(null);
    			setViewQuestionsFromSuperadminsPrivateCategoriesEnabled(null);
    			if (!categoriesService.checkCategoryId(operation,getCategoryId()) || !checkCategory(operation))
    			{
    				// Refresh questions categories from DB
    				resetQuestionsCategories(operation);
    			}
    			else
    			{
    				// Reload questions categories from DB
    				setQuestionsCategories(null);
    			}
    		}
    		updateResourcesImages();
    	}
    	return nextStep;
    }
    
    public String getActiveQuestionTabName()
    {
    	String activeQuestionTabName=null;
    	if (getQuestion().getId()>0L)
    	{
    		switch (activeQuestionTabIndex)
    		{
    			case GENERAL_TABVIEW_TAB:
    				activeQuestionTabName=GENERAL_WIZARD_TAB;
    				break;
    			case ANSWERS_TABVIEW_TAB:
    				activeQuestionTabName=ANSWERS_WIZARD_TAB;
    				break;
    			default:
    				if (activeQuestionTabIndex==resourcesTabviewTab)
    				{
    					activeQuestionTabName=RESOURCES_WIZARD_TAB;
    				}
    				else if (activeQuestionTabIndex==feedbackTabviewTab)
    				{
    					activeQuestionTabName=FEEDBACK_WIZARD_TAB;
    				}
    		}
    	}
    	else
    	{
    		activeQuestionTabName=this.activeQuestionTabName;
    	}
    	return activeQuestionTabName;
    }
    
    private int getQuestionTabIndex(String questionTabName)
    {
    	int questionTabIndex=-1;
    	if (GENERAL_WIZARD_TAB.equals(questionTabName))
    	{
    		questionTabIndex=GENERAL_TABVIEW_TAB;
    	}
    	else if (ANSWERS_WIZARD_TAB.equals(questionTabName))
    	{
    		questionTabIndex=ANSWERS_TABVIEW_TAB;
    	}
    	else if (RESOURCES_WIZARD_TAB.equals(questionTabName))
    	{
    		questionTabIndex=resourcesTabviewTab;
    	}
    	else if (FEEDBACK_WIZARD_TAB.equals(questionTabName))
    	{
    		questionTabIndex=feedbackTabviewTab;
    	}
    	return questionTabIndex;
    }
    
	/**
	 * Tab change listener for displaying other tab of a question.
	 * @param event Tab change event
	 */
    public void changeActiveQuestionTab(TabChangeEvent event)
    {
    	boolean ok=true;
    	TabView questionFormTab=(TabView)event.getComponent();
    	
		// Get current user session Hibernate operation
		Operation operation=getCurrentUserOperation(null);
    	
    	if (activeQuestionTabIndex==GENERAL_TABVIEW_TAB)
    	{
    		// We need to process some input fields
    		processCommonDataInputFields(questionFormTab);
    		
    		setFilterGlobalQuestionsEnabled(null);
    		setFilterOtherUsersQuestionsEnabled(null);
    		setGlobalOtherUserCategoryAllowed(null);
            resetQuestionAuthorAdmin();
            resetQuestionAuthorSuperadmin();
            setViewQuestionsFromOtherUsersPrivateCategoriesEnabled(null);
            setViewQuestionsFromAdminsPrivateCategoriesEnabled(null);
            setViewQuestionsFromSuperadminsPrivateCategoriesEnabled(null);
        	if (checkCommonDataInputFields(operation))
        	{
        		activeQuestionTabIndex=questionFormTab.getActiveIndex();
        	}
        	else
        	{
        		ok=false;
        		questionFormTab.setActiveIndex(activeQuestionTabIndex);
        		
        		// Reset user permissions
        		setAddEnabled(null);
        		setEditEnabled(null);
        		setEditOtherUsersQuestionsEnabled(null);
        		setEditAdminsQuestionsEnabled(null);
        		setEditSuperadminsQuestionsEnabled(null);
        		setFilterGlobalResourcesEnabled(null);
        		setFilterOtherUsersResourcesEnabled(null);
        		setUseGlobalResources(null);
        		setUseOtherUsersResources(null);
        		setViewResourcesFromOtherUsersPrivateCategoriesEnabled(null);
        		setViewResourcesFromAdminsPrivateCategoriesEnabled(null);
        		setViewResourcesFromSuperadminsPrivateCategoriesEnabled(null);
        		resetAdmins();
        		resetSuperadmins();
        		
       			// Scroll page to top position
       			scrollToTop();
       		}
    	}
    	else if (activeQuestionTabIndex==ANSWERS_TABVIEW_TAB)
    	{
    		// Get question
    		Question question=getQuestion();
    		
    		if (question instanceof MultichoiceQuestion)
    		{
    			// Get current answer
    			Answer currentAnswer=getActiveAnswer(event.getComponent());
    			
        		// We need to process some input fields
    			processMultichoiceAnswersInputFields(questionFormTab,currentAnswer,true,new ArrayList<String>());
    			
    			// Check that current answer name entered by user is valid
    			if (checkAnswerName(currentAnswer.getName()))
    			{
    				activeAnswerName=currentAnswer.getName();
    			}
    			else
    			{
    				// Restore old answer name
    				currentAnswer.setName(activeAnswerName);
    				
    				ok=false;
    			}
    		}
    		else if (question instanceof DragDropQuestion)
    		{
    			// Get current draggable item
    			Answer currentDraggableItem=getActiveDraggableItem(event.getComponent());
    			
    			// Get current answer
    			Answer currentAnswer=getActiveDroppableAnswer(event.getComponent());
    			
        		// We need to process some input fields
    			processDragDropAnswersInputFields(
    				questionFormTab,currentDraggableItem,currentAnswer,true,new ArrayList<String>(0));
    			
    			// Check that current draggable item and answer names entered by user are valid
    			if (!checkDraggableItemName(currentDraggableItem.getName()))
    			{
    				ok=false;
    			}
    			if (!checkAnswerName(currentAnswer.getName()))
    			{
    				ok=false;
    			}
    			if (ok)
    			{
    				activeDraggableItemName=currentDraggableItem.getName();
    				activeAnswerName=currentAnswer.getName();
    			}
    			else
    			{
    				// Restore old draggable item name
    				currentDraggableItem.setName(activeDraggableItemName);
    				
    				// Restore old answer name
    				currentAnswer.setName(activeAnswerName);
    			}
    		}
    		else if (question instanceof OmXmlQuestion)
    		{
    			// We need to process some input fields
    			processOmXmlContentInputFields(questionFormTab);
    			
    			// Check that xml content is a valid xml document
    			ok=checkXmlContent(((OmXmlQuestion)question).getXmlContent());
    		}
    		else
    		{
        		// We need to process some input fields
    			processAnswersInputFields(questionFormTab,null);    			
    		}
    		if (!checkAnswersFields())
    		{
    			ok=false;
    		}
    		if (ok)
    		{
    			activeQuestionTabIndex=questionFormTab.getActiveIndex();
    		}
    		else
    		{
    			questionFormTab.setActiveIndex(activeQuestionTabIndex);
    			
    			// Scroll page to top position
    			scrollToTop();
    		}
    	}
    	else if (activeQuestionTabIndex==resourcesTabviewTab)
    	{
			// Get current resource
    		QuestionResource currentQuestionResource=getActiveQuestionResource(event.getComponent());
			
    		// We need to process some input fields
    		processResourcesInputFields(operation,event.getComponent(),currentQuestionResource);
			
    		if (currentQuestionResource!=null)
    		{
    			// Check that current resource name entered by user is valid
        		if (checkQuestionResource(getQuestion(),currentQuestionResource))
        		{
        			activeQuestionResourceName=currentQuestionResource.getName();
    			}
    			else
    			{
    				// Restore old resource name
    				currentQuestionResource.setName(activeQuestionResourceName);
    				
    				ok=false;
    			}
    		}
    		if (ok)
    		{
    			activeQuestionTabIndex=questionFormTab.getActiveIndex();
    		}
    		else
    		{
    			questionFormTab.setActiveIndex(activeQuestionTabIndex);
    			
    			// Scroll page to top position
    			scrollToTop();
    		}
    	}
    	else if (activeQuestionTabIndex==feedbackTabviewTab)
    	{
    		// We need to process some input fields
    		processFeedbackInputFields(questionFormTab);
    		
    		activeQuestionTabIndex=questionFormTab.getActiveIndex();
    	}
    	if (ok)
    	{
    		if (activeQuestionTabIndex==GENERAL_TABVIEW_TAB)
    		{
    			
        		setFilterGlobalQuestionsEnabled(null);
        		setFilterOtherUsersQuestionsEnabled(null);
        		setGlobalOtherUserCategoryAllowed(null);
                resetQuestionAuthorAdmin();
                resetQuestionAuthorSuperadmin();
                setViewQuestionsFromOtherUsersPrivateCategoriesEnabled(null);
                setViewQuestionsFromAdminsPrivateCategoriesEnabled(null);
                setViewQuestionsFromSuperadminsPrivateCategoriesEnabled(null);
        		if (!categoriesService.checkCategoryId(operation,getCategoryId()) || !checkCategory(operation))
        		{
        			// Refresh questions categories from DB
        			resetQuestionsCategories(operation);
        		}
        		else
        		{
        			// Reload questions categories from DB
        			setQuestionsCategories(null);
        		}
    		}
        	updateResourcesImages();
    	}
    }
    
    /**
     * @return Localized title for "Answers" tab
     */
    public String getAnswersTabTitle()
    {
    	String answersTabTitle="";
    	if (getQuestion() instanceof OmXmlQuestion)
    	{
    		answersTabTitle=localizationService.getLocalizedMessage("CONTENT");
    	}
    	else
    	{
    		answersTabTitle=localizationService.getLocalizedMessage("ANSWERS");
    	}
    	return answersTabTitle;
    }
    
    /**
     * @return true if this question displays the 'Resources' tab, false otherwise 
     */
    public boolean isResourcesTabDisplayed()
    {
    	return resourcesTabviewTab!=-1;
    }
    
    /**
     * @return true if this question allows to define advanced feedbacks, false otherwise
     */
    public boolean isAdvancedFeedbacksEnabled()
    {
    	return !(getQuestion() instanceof OmXmlQuestion);
    }
    
	/**
     * @return true if it is allowed to re-sort answers (answers>=2), otherwise false
	 */
	public boolean isEnabledReSortAnswers()
	{
		return getQuestion().getAnswers().size()>1;
	}
	
	/**
     * @return true if it is allowed to re-sort draggable items (draggable items>=2), otherwise false
	 */
	public boolean isEnabledReSortDraggableItems()
	{
		return ((DragDropQuestion)getQuestion()).getDraggableItems().size()>1;
	}
	
	/**
     * @return true if it is allowed to re-sort droppable answers (droppable answers>=2), otherwise false
	 */
	public boolean isEnabledReSortDroppableAnswers()
	{
		return ((DragDropQuestion)getQuestion()).getDroppableAnswers().size()>1;
	}
	
	/**
	 * Action listener to show the dialog to re-sort answers.
	 * @param event Action event
	 */
	public void showReSortAnswers(ActionEvent event)
	{
		updateMultichoiceAnswersResourcesImages((MultichoiceQuestion)getQuestion());
		
		// Get current answer
		Answer currentAnswer=getActiveAnswer(event.getComponent());
		
		// We need to process some input fields
		processAnswersInputFields(event.getComponent(),currentAnswer);
		
		// Check that current answer name entered by user is valid
		if (checkAnswerName(currentAnswer.getName()))
		{
			activeAnswerName=currentAnswer.getName();
			
			setAnswersSorting(null);
			RequestContext rq=RequestContext.getCurrentInstance();
			rq.execute("resortAnswersDialog.show()");
		}
		else
		{
			// Restore old answer name
			currentAnswer.setName(activeAnswerName);
			
			// Scroll page to top position
			scrollToTop();
		}
	}
    
	/**
	 * Action listener to apply changes from the dialog for re-sorting answers if we accept that dialog.
	 * @param event Action event
	 */
	public void acceptReSortAnswers(ActionEvent event)
	{
		// First we need to change position of answers conditions of feedbacks
		List<Answer> answersSorting=getAnswersSorting();
		for (FeedbackBean feedback:getFeedbacks())
		{
			for (AnswerConditionBean answerCondition:feedback.getAnswerConditions())
			{
				for (SingleAnswerConditionBean singleAnswerCondition:answerCondition.getSingleAnswerConditions())
				{
					int answerConditionPos=singleAnswerCondition.getAnswerPosition();
					for (int answerPos=1;answerPos<=answersSorting.size();answerPos++)
					{
						Answer answer=answersSorting.get(answerPos-1);
						if (answer.getPosition()==answerConditionPos)
						{
							singleAnswerCondition.setAnswerPosition(answerPos);
							break;
						}
					}
				}
			}
		}
		Answer activeAnswer=null;
		// We change answers positions
		for (int answerPos=1;answerPos<=answersSorting.size();answerPos++)
		{
			Answer answer=answersSorting.get(answerPos-1);
			answer.setPosition(answerPos);
			if (answerPos==activeAnswerIndex+1)
			{
				activeAnswer=answer;
				activeAnswerName=answer.getName()==null?"":answer.getName();
			}
		}
		
		getQuestion().setAnswers(answersSorting);
		
		// We need to update answers text fields
		updateAnswersTextFields(event.getComponent(),answersSorting.size());
		
		updateAnswerResourceImage(activeAnswer);
	}
	
	/**
	 * Action listener for updating resources information if we cancel the changes within the dialog for 
	 * re-sorting answers. 
	 * @param event Action event
	 */
	public void cancelReSortAnswers(ActionEvent event)
	{
		updateAnswerResourceImage(getActiveAnswer(event.getComponent()));
	}
	
	/**
	 * Action listener to show the dialog to re-sort draggable items of a "Drag & Drop" question.
	 * @param event Action event
	 */
	public void showReSortDraggableItems(ActionEvent event)
	{
		updateDragDropDraggableItemsResourcesImages((DragDropQuestion)getQuestion());
		
		// Get current draggable item
		Answer currentDraggableItem=getActiveDraggableItem(event.getComponent());
		
		// Get current answer
		Answer currentAnswer=getActiveDroppableAnswer(event.getComponent());
		
		// We need to process some input fields
		processAnswersInputFields(event.getComponent(),currentDraggableItem,currentAnswer);
		
		// Check that current draggable item and answer names entered by user are valid
		boolean ok=true;
		if (!checkDraggableItemName(currentDraggableItem.getName()))
		{
			ok=false;
		}
		if (!checkAnswerName(currentAnswer.getName()))
		{
			ok=false;
		}
		if (ok)
		{
			activeDraggableItemName=currentDraggableItem.getName();
			activeAnswerName=currentAnswer.getName();
			
			setDraggableItemsSorting(null);
			RequestContext rq=RequestContext.getCurrentInstance();
			rq.execute("resortDraggableItemsDialog.show()");
		}
		else
		{
			// Restore old draggable item name
			currentDraggableItem.setName(activeDraggableItemName);
			
			// Restore old answer name
			currentAnswer.setName(activeAnswerName);
			
			// Scroll page to top position
			scrollToTop();
		}
	}
	
	/**
	 * Action listener to apply changes from the dialog for re-sorting draggable items of a "Drag & Drop" 
	 * question if we accept that dialog.
	 * @param event Action event
	 */
	public void acceptReSortDraggableItems(ActionEvent event)
	{
		Answer activeDraggableItem=null;
		// We change draggable items positions
		List<Answer> draggableItemsSorting=getDraggableItemsSorting();
		for (int draggableItemPos=1;draggableItemPos<=draggableItemsSorting.size();draggableItemPos++)
		{
			Answer draggableItem=draggableItemsSorting.get(draggableItemPos-1);
			draggableItem.setPosition(draggableItemPos);
			if (draggableItemPos==activeDraggableItemIndex+1)
			{
				activeDraggableItem=draggableItem;
				activeDraggableItemName=draggableItem.getName()==null?"":draggableItem.getName();
			}
		}
		
		// We need to update draggable items text fields
		updateDragDropDraggableItemsTextFields(event.getComponent(),draggableItemsSorting.size());
		
		Operation operation=updateAnswerResourceImage(activeDraggableItem);
		updateAnswerResourceImage(operation,getActiveDroppableAnswer(event.getComponent()));
	}
	
	/**
	 * Action listener for updating resources information if we cancel the changes within the dialog for 
	 * re-sorting draggable items of a "Drag & Drop" question. 
	 * @param event Action event
	 */
	public void cancelReSortDraggableItems(ActionEvent event)
	{
		Operation operation=updateAnswerResourceImage(getActiveDraggableItem(event.getComponent()));
		updateAnswerResourceImage(operation,getActiveDroppableAnswer(event.getComponent()));
	}
	
	/**
	 * Action listener to show the dialog to re-sort answers of a "Drag & Drop" question.
	 * @param event Action event
	 */
	public void showReSortDroppableAnswers(ActionEvent event)
	{
		updateDragDropAnswersResourcesImages((DragDropQuestion)getQuestion());
		
		// Get current draggable item
		Answer currentDraggableItem=getActiveDraggableItem(event.getComponent());
		
		// Get current answer
		Answer currentAnswer=getActiveDroppableAnswer(event.getComponent());
		
		// We need to process some input fields
		processAnswersInputFields(event.getComponent(),currentDraggableItem,currentAnswer);
		
		// Check that current draggable item and answer names entered by user are valid
		boolean ok=true;
		if (!checkDraggableItemName(currentDraggableItem.getName()))
		{
			ok=false;
		}
		if (!checkAnswerName(currentAnswer.getName()))
		{
			ok=false;
		}
		if (ok)
		{
			activeDraggableItemName=currentDraggableItem.getName();
			activeAnswerName=currentAnswer.getName();
			
			setDroppableAnswersSorting(null);
			RequestContext rq=RequestContext.getCurrentInstance();
			rq.execute("resortDroppableAnswersDialog.show()");
		}
		else
		{
			// Restore old draggable item name
			currentDraggableItem.setName(activeDraggableItemName);
			
			// Restore old answer name
			currentAnswer.setName(activeAnswerName);
			
			// Scroll page to top position
			scrollToTop();
		}
	}
	
	/**
	 * Action listener to apply changes from the dialog for re-sorting answers of a "Drag & Drop" question 
	 * if we accept that dialog.
	 * @param event Action event
	 */
	public void acceptReSortDroppableAnswers(ActionEvent event)
	{
		Answer activeAnswer=null;
		// We change answers positions
		List<Answer> droppableAnswersSorting=getDroppableAnswersSorting();
		for (int answerPos=1;answerPos<=droppableAnswersSorting.size();answerPos++)
		{
			Answer answer=droppableAnswersSorting.get(answerPos-1);
			answer.setPosition(answerPos);
			if (answerPos==activeAnswerIndex+1)
			{
				activeAnswer=answer;
				activeAnswerName=answer.getName()==null?"":answer.getName();
			}
		}
		
		// We need to update answers text fields
		updateDragDropAnswersTextFields(event.getComponent(),droppableAnswersSorting.size());
		
		Operation operation=updateAnswerResourceImage(getActiveDraggableItem(event.getComponent()));
		updateAnswerResourceImage(operation,activeAnswer);
	}
	
	/**
	 * Action listener for updating resources information if we cancel the changes within the dialog for 
	 * re-sorting answers of a "Drag & Drop" question. 
	 * @param event Action event
	 */
	public void cancelReSortDroppableAnswers(ActionEvent event)
	{
		Operation operation=updateAnswerResourceImage(getActiveDraggableItem(event.getComponent()));
		updateAnswerResourceImage(operation,getActiveDroppableAnswer(event.getComponent()));
	}
	
    // Añade una nueva respuesta a la pregunta
    /**
     * Adds a new answer to question.
	 * @param event Action event
     */
	public void addAnswer(ActionEvent event)
	{
		// Get current answer
		Answer currentAnswer=getActiveAnswer(event.getComponent());
		
		// We need to process some input fields
		processAnswersInputFields(event.getComponent(),currentAnswer);
		
		// Check that current answer name entered by user is valid
		if (checkAnswerName(currentAnswer.getName()))
		{
			// Get question
			Question question=getQuestion();
			
			// Add a new answer
			question.addAnswer(new Answer());
			int numberAnswers=question.getAnswers().size();
			
			// Change active tab of answers accordion to display the new answer
			activeAnswerIndex=numberAnswers-1;
			activeAnswerName="";
			refreshActiveAnswer(event.getComponent());
			
			// We need to update answers text fields
			updateAnswersTextFields(event.getComponent(),numberAnswers);
		}
		else
		{
			// Restore old answer name
			currentAnswer.setName(activeAnswerName);
			
			// Scroll page to top position
			scrollToTop();
			
			updateAnswerResourceImage(currentAnswer);
		}
	}
	
    /**
     * Adds a new draggable item to a "Drag & Drop" question.
	 * @param event Action event
     */
	public void addDraggableItem(ActionEvent event)
	{
		// Get current draggable item
		Answer currentDraggableItem=getActiveDraggableItem(event.getComponent());
		
		// Get current answer
		Answer currentAnswer=getActiveDroppableAnswer(event.getComponent());
		
		// We need to process some input fields
		processAnswersInputFields(event.getComponent(),currentDraggableItem,currentAnswer,false);
		
		// Check that current draggable item and answer names entered by user are valid
		boolean ok=true;
		if (!checkDraggableItemName(currentDraggableItem.getName()))
		{
			ok=false;
		}
		if (!checkAnswerName(currentAnswer.getName()))
		{
			ok=false;
		}
		Operation operation=null;
		if (ok)
		{
			// Get question
			DragDropQuestion question=(DragDropQuestion)getQuestion();
			
			// Add a new draggable item
			//TODO de momento solo tenemos en cuenta el grupo 1, habra que cambiarlo mas adelante
			question.addDraggableItem(new DragDropAnswer(),1);
			int numberDraggableItems=question.getDraggableItems(1).size();
			
			// Change active tab of draggable items accordion to display the new draggable item
			activeDraggableItemIndex=numberDraggableItems-1;
			activeDraggableItemName="";
			refreshActiveDraggableItem(event.getComponent());
			
			// We need to update draggable items text fields
			updateDraggableItemsTextFields(event.getComponent(),numberDraggableItems);
		}
		else
		{
			// Restore old draggable item name
			currentDraggableItem.setName(activeDraggableItemName);
			
			// Restore old answer name
			currentAnswer.setName(activeAnswerName);
			
			// Scroll page to top position
			scrollToTop();
			
			operation=updateAnswerResourceImage(currentDraggableItem);
		}
		operation=updateAnswerResourceImage(operation,currentAnswer);
	}
    /**
     * Adds a new draggable item to a "Drag & Drop" question.
	 * @param event Action event
     */
	public void addDroppableAnswer(ActionEvent event)
	{
		// Get current draggable item
		Answer currentDraggableItem=getActiveDraggableItem(event.getComponent());
		
		// Get current answer
		Answer currentAnswer=getActiveDroppableAnswer(event.getComponent());
		
		// We need to process some input fields
		processAnswersInputFields(event.getComponent(),null,currentAnswer,false);
		
		// Check that current draggable item and answer names entered by user are valid
		boolean ok=true;
		if (!checkDraggableItemName(currentDraggableItem.getName()))
		{
			ok=false;
		}
		if (!checkAnswerName(currentAnswer.getName()))
		{
			ok=false;
		}
		Operation operation=null;
		if (ok)
		{
			// Get question
			DragDropQuestion question=(DragDropQuestion)getQuestion();
			
			// Add a new answer
			//TODO de momento solo tenemos en cuenta el grupo 1, habra que cambiarlo mas adelante
			question.addDroppableAnswer(new DragDropAnswer(),1);
			int numberAnswers=question.getDroppableAnswers(1).size();
			
			// Change active tab of answers accordion to display the new answer
			activeAnswerIndex=numberAnswers-1;
			activeAnswerName="";
			refreshActiveAnswer(event.getComponent());
			
			// We need to update answers text fields
			updateAnswersTextFields(event.getComponent(),numberAnswers);
		}
		else
		{
			// Restore old draggable item name
			currentDraggableItem.setName(activeDraggableItemName);
			
			// Restore old answer name
			currentAnswer.setName(activeAnswerName);
			
			// Scroll page to top position
			scrollToTop();
			
			operation=updateAnswerResourceImage(currentAnswer);
		}
		updateAnswerResourceImage(operation,currentDraggableItem);
	}
	
	/**
	 * Checks if deleting an answer will affect to the feedbacks already defined.
	 * @param deletePosition Position of answer to delete
	 * @return true if deleting an answer won't affect to the feedbacks already defined, false otherwise
	 */
	private boolean checkFeedbacksForDeleteAnswer(int deletePosition)
	{
		boolean ok=true;
		
		// Get question
		Question question=getQuestion();
		
		boolean noSingleMultichoice=false;
		boolean correct=false;
		if (question instanceof MultichoiceQuestion)
		{
			MultichoiceQuestion multichoiceQuestion=(MultichoiceQuestion)question;
			noSingleMultichoice=!multichoiceQuestion.isSingle();
			Answer answer=multichoiceQuestion.getAnswer(deletePosition);
			correct=answer.getCorrect();
		}
		for (FeedbackBean feedback:getFeedbacks())
		{
			// Check answer conditions
			ok=checkAnswerConditions(feedback,deletePosition);
			if (!ok)
			{
				break;
			}
			
			if (noSingleMultichoice)
			{
				// Check selected answers condition 
				// (answer has not been deleted so a maximum value variation needed)
				ok=checkSelectedAnswersCondition(feedback,-1);
				if (!ok)
				{
					break;
				}
				
				// Check unselected answers condition 
				// (answer has not been deleted so a maximum value variation needed)
				ok=checkUnselectedAnswersCondition(feedback,-1);
				if (!ok)
				{
					break;
				}
				
				if (correct)
				{
					// Check selected right answers condition 
					// (answer has not been deleted so a maximum value variation needed)
					ok=checkSelectedRightAnswersCondition(feedback,-1);
					if (!ok)
					{
						break;
					}
					
					// Check unselected right answers condition 
					// (answer has not been deleted so a maximum value variation needed)
					ok=checkUnselectedRightAnswersCondition(feedback,-1);
					if (!ok)
					{
						break;
					}
					
					// Check right distance answers condition 
					// (answer is correct and has not been deleted so a right variation needed)
					ok=checkRightDistanceCondition(feedback,-1,0);
					if (!ok)
					{
						break;
					}
				}
				else
				{
					// Check selected wrong answers condition 
					// (answer has not been deleted so a maximum value variation needed)
					ok=checkSelectedWrongAnswersCondition(feedback,-1);
					if (!ok)
					{
						break;
					}
					
					// Check unselected wrong answers condition 
					// (answer has not been deleted so a maximum value variation needed)
					ok=checkUnselectedWrongAnswersCondition(feedback,-1);
					if (!ok)
					{
						break;
					}
					
					// Check right distance answers condition 
					// (answer is incorrect and has not been deleted so a wrong variation needed)
					ok=checkRightDistanceCondition(feedback,0,-1);
					if (!ok)
					{
						break;
					}
				}
			}
		}
		return ok;
	}
	
	/**
	 * Updates feebacks related to the deleted answer if needed.
	 * @param deletePosition Position of deleted answer
	 */
	private void updateFeedbacksForDeleteAnswer(int deletePosition)
	{
		// Get question
		Question question=getQuestion();
		
		boolean noSingleMultichoice=question instanceof MultichoiceQuestion && 
			!((MultichoiceQuestion)question).isSingle();
		for (FeedbackBean feedback:getFeedbacks())
		{
			// Update answer conditions
			updateAnswerConditions(feedback,deletePosition);
			
			if (noSingleMultichoice)
			{
				// Update selected answers condition 
				// (answer has been deleted so no maximum value variation needed)
				updateSelectedAnswersCondition(feedback,0);
				
				// Update selected right answers condition 
				// (answer has been deleted so no maximum value variation needed)
				updateSelectedRightAnswersCondition(feedback,0);
				
				// Update selected wrong answers condition 
				// (answer has been deleted so no maximum value variation needed)
				updateSelectedWrongAnswersCondition(feedback,0);
				
				// Update unselected answers condition 
				// (answer has been deleted so no maximum value variation needed)
				updateUnselectedAnswersCondition(feedback,0);
				
				// Update unselected right answers condition 
				// (answer has been deleted so no maximum value variation needed)
				updateUnselectedRightAnswersCondition(feedback,0);
				
				// Update unselected wrong answers condition 
				// (answer has been deleted so no maximum value variation needed)
				updateUnselectedWrongAnswersCondition(feedback,0);
				
				// Update right distance condition 
				// (answer has been deleted so no right nor wrong variations needed)
				updateRightDistanceCondition(feedback,0,0);
			}
			// Update raw feedback
			question.getFeedback(feedback.getPosition()).setFromOtherFeedback(feedback.getAsFeedback());
		}
	}
	
	// Elimina una respuesta de la pregunta
	/**
	 * Removes an answer from question.
	 * @param event Action event
	 */
	public void removeAnswer(ActionEvent event)
	{
		boolean forceRemoveAnswer=true;
		if (event.getComponent().getAttributes().get("position")!=null)
		{
			answerToRemovePosition=((Integer)event.getComponent().getAttributes().get("position")).intValue();
			forceRemoveAnswer=false;
		}
		
		// Get question
		Question question=getQuestion();
		
		boolean checkFeedbacks=checkFeedbacksForDeleteAnswer(answerToRemovePosition);
		if (forceRemoveAnswer || checkFeedbacks)
		{
			// Remove answer from question
			question.removeAnswer(answerToRemovePosition);
			
			// If it is needeed change active tab of answers accordion
			int numAnswers=question.getAnswers().size();
			if (answerToRemovePosition>numAnswers)
			{
				activeAnswerIndex=numAnswers-1;
				refreshActiveAnswer(event.getComponent());
			}
			Answer activeAnswer=getActiveAnswer(event.getComponent());
			activeAnswerName=activeAnswer.getName();
			
			// If it is needed update feedbacks
			if (!checkFeedbacks)
			{
				updateFeedbacksForDeleteAnswer(answerToRemovePosition);
			}
			
			// We need to update answers text fields
			updateAnswersTextFields(event.getComponent(),numAnswers);
			
			updateAnswerResourceImage(activeAnswer);
		}
		else
		{
			RequestContext rq=RequestContext.getCurrentInstance();
			rq.execute("confirmDeleteAnswerDialog.show()");
		}
	}
	
	/**
	 * Action listener for updating resources information if we cancel the changes within the confirmation dialog 
	 * for deleting an answer from question 
	 * @param event Action event
	 */
	public void cancelConfirmRemoveAnswer(ActionEvent event)
	{
		updateAnswerResourceImage(getActiveAnswer(event.getComponent()));			
	}
	
	/**
	 * Checks if deleting a draggable item will affect to the answers already defined in a "Drag & Drop"
	 * question.
	 * @param group Group of draggable item to delete
	 * @param deletePosition Position of draggable item to delete
	 * @return true if deleting a draggable item won't affect to the feedbacks already defined in a 
	 * "Drag & Drop" question, false otherwise
	 */
	private boolean checkAnswersForDeleteDraggableItem(int group,int deletePosition)
	{
		boolean ok=true;
		DragDropQuestion dragDropQuestion=(DragDropQuestion)getQuestion();
		Answer draggableItem=dragDropQuestion.getDraggableItem(group,deletePosition);
		if (draggableItem!=null && draggableItem instanceof DragDropAnswer)
		{
			for (Answer answer:dragDropQuestion.getDroppableAnswers(group))
			{
				if (answer instanceof DragDropAnswer)
				{
					if (draggableItem.equals(((DragDropAnswer)answer).getRightAnswer())) 
					{
						ok=false;
						break;
					}
				}
			}
		}
		return ok;
	}
	
	/**
	 * Checks if deleting a draggable item will affect to the feedbacks already defined in a "Drag & Drop"
	 * question.
	 * @param group Group of draggable item to delete
	 * @param deletePosition Position of draggable item to delete
	 * @return true if deleting a draggable item won't affect to the feedbacks already defined in a 
	 * "Drag & Drop" question, false otherwise
	 */
	private boolean checkFeedbacksForDeleteDraggableItem(int group,int deletePosition)
	{
		boolean ok=true;
		for (FeedbackBean feedback:getFeedbacks())
		{
			// Check answer conditions for deleting a draggable item in a "Drag & Drop" question
			ok=checkAnswerConditionsForDeletingDraggableItem(feedback,group,deletePosition);
			if (!ok)
			{
				break;
			}
		}
		return ok;
	}
	
	/**
	 * Updates feedbacks related to the deleted draggable item in a "Drag & Drop" question if needed.
	 * @param group Group of deleted draggable item
	 * @param deletePosition Position of deleted draggable item
	 */
	private void updateFeedbacksForDeleteDraggableItem(int group,int deletePosition)
	{
		Question question=getQuestion();
		for (FeedbackBean feedback:getFeedbacks())
		{
			// Update answer conditions for deleting a draggable item in a "Drag & Drop" question
			updateAnswerConditionsForDeletingDraggableItem(feedback,group,deletePosition);
			
			// Update raw feedback
			question.getFeedback(feedback.getPosition()).setFromOtherFeedback(feedback.getAsFeedback());
		}
	}
	
	/**
	 * Removes a draggable item from a "Drag & Drop" question.
	 * @param event Action event
	 */
	public void removeDraggableItem(ActionEvent event)
	{
		// Get question
		Question question=getQuestion();
		
		if (question instanceof DragDropQuestion)
		{
			DragDropQuestion dragDropQuestion=(DragDropQuestion)question;
			
			// Get current draggable item
			Answer currentDraggableItem=getActiveDraggableItem(event.getComponent());
			
			// Get current answer
			Answer currentAnswer=getActiveDroppableAnswer(event.getComponent());
			
			// We need to process some input fields
			processAnswersInputFields(event.getComponent(),currentDraggableItem,currentAnswer);
			
			// Check that current answer name entered by user is valid (don't display error messages yet)
			if (checkAnswerName(currentAnswer.getName(),false))
			{
				boolean forceRemoveDraggableItem=true;
				if (event.getComponent().getAttributes().get("group")!=null && 
					event.getComponent().getAttributes().get("position")!=null)
				{
					draggableItemToRemoveGroup=
						((Integer)event.getComponent().getAttributes().get("group")).intValue();
					draggableItemToRemovePosition=
						((Integer)event.getComponent().getAttributes().get("position")).intValue();
					forceRemoveDraggableItem=false;
				}
				boolean checkAnswers=
					checkAnswersForDeleteDraggableItem(draggableItemToRemoveGroup,draggableItemToRemovePosition);
				boolean checkFeedbacks=checkFeedbacksForDeleteDraggableItem(
					draggableItemToRemoveGroup,draggableItemToRemovePosition);
				if (forceRemoveDraggableItem || (checkAnswers && checkFeedbacks))
				{
					// Remove draggable item from question
					dragDropQuestion.removeDraggableItem(
						draggableItemToRemoveGroup,draggableItemToRemovePosition);
					
					// If it is needeed change active tab of draggable items accordion
					int numDraggableItems=dragDropQuestion.getDraggableItems(draggableItemToRemoveGroup).size();
					if (draggableItemToRemovePosition>numDraggableItems)
					{
						activeDraggableItemIndex=numDraggableItems-1;
						refreshActiveDraggableItem(event.getComponent());
					}
					Answer activeDraggableItem=getActiveDraggableItem(event.getComponent());
					activeDraggableItemName=activeDraggableItem.getName();
					
					// If it is needed update feedbacks
					if (!checkFeedbacks)
					{
						updateFeedbacksForDeleteDraggableItem(
							draggableItemToRemoveGroup,draggableItemToRemovePosition);
					}
					
					// We need to update draggable items text fields
					updateDraggableItemsTextFields(event.getComponent(),numDraggableItems);
					
					// We need to update answers text fields
					updateAnswersTextFields(event.getComponent(),
						dragDropQuestion.getDroppableAnswers(draggableItemToRemoveGroup).size());
					
					Operation operation=updateAnswerResourceImage(activeDraggableItem);
					updateAnswerResourceImage(operation,currentAnswer);
				}
				else
				{
					if (checkAnswers)
					{
						setConfirmDeleteDraggableItemMessage("CONFIRM_DELETE_DRAGGABLE_ITEM_FOR_FEEDBACKS");
					}
					else if (checkFeedbacks)
					{
						setConfirmDeleteDraggableItemMessage("CONFIRM_DELETE_DRAGGABLE_ITEM_FOR_ANSWERS");
					}
					else
					{
						setConfirmDeleteDraggableItemMessage(
							"CONFIRM_DELETE_DRAGGABLE_ITEM_FOR_ANSWERS_AND_FEEDBACKS");
					}
					RequestContext rq=RequestContext.getCurrentInstance();
					rq.execute("confirmDeleteDraggableItemDialog.show()");
				}
			}
			else
			{
				// Perform checks and display error messages
				checkDraggableItemName(currentDraggableItem.getName());
				checkAnswerName(currentAnswer.getName());
				
				// Restore old draggable item name
				currentDraggableItem.setName(activeDraggableItemName);
				
				// Restore old answer name
				currentAnswer.setName(activeAnswerName);
				
				// Scroll page to top position
				scrollToTop();
				
				Operation operation=updateAnswerResourceImage(currentDraggableItem);			
				updateAnswerResourceImage(operation,currentAnswer);			
			}
		}
	}
	
	/**
	 * Action listener for updating resources information if we cancel the changes within the confirmation dialog 
	 * for deleting a draggable item from a "Drag & Drop" question 
	 * @param event Action event
	 */
	public void cancelConfirmRemoveDraggableItem(ActionEvent event)
	{
		Operation operation=updateAnswerResourceImage(getActiveDraggableItem(event.getComponent()));
		updateAnswerResourceImage(operation,getActiveAnswer(event.getComponent()));
	}
	
	/**
	 * Checks if deleting an answer will affect to the feedbacks already defined in a "Drag & Drop" question.
	 * @param group Group of answer to delete
	 * @param deletePosition Position of answer to delete
	 * @return true if deleting an answer won't affect to the feedbacks already defined in a "Drag & Drop" 
	 * question, false otherwise
	 */
	private boolean checkFeedbacksForDeleteDroppableAnswer(int group,int deletePosition)
	{
		boolean ok=true;
		for (FeedbackBean feedback:getFeedbacks())
		{
			// Check answer conditions for deleting an answer in a "Drag & Drop" question
			ok=checkAnswerConditionsForDeletingDroppableAnswer(feedback,group,deletePosition);
			if (!ok)
			{
				break;
			}
			
			// Check selected right answers condition 
			// (answer has not been deleted so a maximum value variation needed)
			ok=checkSelectedRightAnswersCondition(feedback,-1);
			if (!ok)
			{
				break;
			}
			
			// Check selected wrong answers condition 
			// (answer has not been deleted so a maximum value variation needed)
			ok=checkSelectedWrongAnswersCondition(feedback,-1);
			if (!ok)
			{
				break;
			}
			
			// Check right distance answers condition 
			// (answer has not been deleted so right and wrong variations needed)
			ok=checkRightDistanceCondition(feedback,-1,-1);
			if (!ok)
			{
				break;
			}
		}
		return ok;
	}
	
	/**
	 * Updates feedbacks related to the deleted answer in a "Drag & Drop" question.
	 * @param group Group of deleted answer
	 * @param deletePosition Position of deleted answer
	 */
	private void updateFeedbacksForDeleteDroppableAnswer(int group,int deletePosition)
	{
		Question question=getQuestion();
		for (FeedbackBean feedback:getFeedbacks())
		{
			// Update answer conditions for deleting an answer in a "Drag & Drop" question
			updateAnswerConditionsForDeletingDroppableAnswer(feedback,group,deletePosition);
			
			// Update selected right answers condition 
			// (answer has been deleted so no maximum value variation needed)
			updateSelectedRightAnswersCondition(feedback,0);
			
			// Update selected wrong answers condition 
			// (answer has been deleted so no maximum value variation needed)
			updateSelectedWrongAnswersCondition(feedback,0);
			
			// Update right distance condition 
			// (answer has been deleted so no right nor wrong variations needed)
			updateRightDistanceCondition(feedback,0,0);
			
			//Update raw feedback
			question.getFeedback(feedback.getPosition()).setFromOtherFeedback(feedback.getAsFeedback());
		}
	}
	
	/**
	 * Removes an answer from a "Drag & Drop" question.
	 * @param event Action event
	 */
	public void removeDroppableAnswer(ActionEvent event)
	{
		// Get question
		Question question=getQuestion();
		
		if (question instanceof DragDropQuestion)
		{
			DragDropQuestion dragDropQuestion=(DragDropQuestion)question;
			
			// Get current draggable item
			Answer currentDraggableItem=getActiveDraggableItem(event.getComponent());
			
			// Get current answer
			Answer currentAnswer=getActiveDroppableAnswer(event.getComponent());
			
			// We need to process some input fields
			processAnswersInputFields(event.getComponent(),currentDraggableItem,currentAnswer);
			
			// Check that current draggable item name entered by user is valid (also display error messages)
			if (checkDraggableItemName(currentDraggableItem.getName()))
			{
				boolean forceRemoveAnswer=true;
				if (event.getComponent().getAttributes().get("group")!=null && 
					event.getComponent().getAttributes().get("position")!=null)
				{
					answerToRemoveGroup=((Integer)event.getComponent().getAttributes().get("group")).intValue();
					answerToRemovePosition=
						((Integer)event.getComponent().getAttributes().get("position")).intValue();
					forceRemoveAnswer=false;
				}
				boolean checkFeedbacks=
					checkFeedbacksForDeleteDroppableAnswer(answerToRemoveGroup,answerToRemovePosition);
				
				if (forceRemoveAnswer || checkFeedbacks)
				{
					// Remove answer from question
					dragDropQuestion.removeDroppableItem(answerToRemoveGroup,answerToRemovePosition);
					
					// If it is needeed change active tab of answers accordion
					int numAnswers=dragDropQuestion.getDroppableAnswers(answerToRemoveGroup).size();
					if (answerToRemovePosition>numAnswers)
					{
						activeAnswerIndex=numAnswers-1;
						refreshActiveAnswer(event.getComponent());
					}
					Answer activeAnswer=getActiveDroppableAnswer(event.getComponent());
					activeAnswerName=activeAnswer.getName();
					
					// If it is needed update feedbacks
					if (!checkFeedbacks)
					{
						updateFeedbacksForDeleteDroppableAnswer(answerToRemoveGroup,answerToRemovePosition);
					}
					
					// We need to update answers text fields
					updateAnswersTextFields(event.getComponent(),numAnswers);
					
					Operation operation=updateAnswerResourceImage(currentDraggableItem);
					updateAnswerResourceImage(operation,activeAnswer);
				}
				else
				{
					RequestContext rq=RequestContext.getCurrentInstance();
					rq.execute("confirmDeleteDroppableAnswerDialog.show()");
				}
			}
			else
			{
				// Perform answer name checks and display error messages
				checkAnswerName(currentAnswer.getName());
				
				// Restore old draggable item name
				currentDraggableItem.setName(activeDraggableItemName);
				
				// Restore old answer name
				currentAnswer.setName(activeAnswerName);
				
				// Scroll page to top position
				scrollToTop();
				
				Operation operation=updateAnswerResourceImage(currentDraggableItem);			
				updateAnswerResourceImage(operation,currentAnswer);
			}
		}
	}
	
	/**
	 * Action listener for updating resources information if we cancel the changes within the confirmation dialog 
	 * for deleting an answer from a "Drag & Drop" question 
	 * @param event Action event
	 */
	public void cancelConfirmRemoveDroppableAnswer(ActionEvent event)
	{
		Operation operation=updateAnswerResourceImage(getActiveDraggableItem(event.getComponent()));
		updateAnswerResourceImage(operation,getActiveDroppableAnswer(event.getComponent()));
	}
	
    /**
     * @return A special resource that represents not having selected any resource 
     */
    public Resource getNoResource()
    {
		Resource noResource=new Resource();
		noResource.setId(-1);
		noResource.setName("");
		return noResource;
    }
	
	/**
	 * @return Number of additional resources added to this question
	 */
	public int getResourcesSize()
	{
		return getQuestion().getQuestionResources().size();
	}
	
	/**
     * @return true if it is allowed to re-sort additional resources added to this question (resources>=2), 
     * otherwise false
	 */
	public boolean isEnabledReSortResources()
	{
		return getResourcesSize()>1;
	}
	
	/**
	 * Action listener to show the dialog to re-sort additional resources added to this question.
	 * @param event Action event
	 */
	public void showReSortResources(ActionEvent event)
	{
		// Get current user session Hibernate operation
		Operation operation=getCurrentUserOperation(null);
		
		// Get current question resource
		QuestionResource currentQuestionResource=getActiveQuestionResource(event.getComponent());
		
		String currentQuestionResourceName=activeQuestionResourceName;
		
		// We need to process some input fields
		processResourcesInputFields(operation,event.getComponent(),currentQuestionResource);
		
		// Check that current question resource is valid
		if (checkQuestionResource(getQuestion(),currentQuestionResource))
		{
			updateQuestionResourcesImages();
			
			if (isEnabledReSortResources())
			{
				//activeQuestionResourceName=currentQuestionResource.getName();
				setQuestionResourcesSorting(null);
				RequestContext rq=RequestContext.getCurrentInstance();
				rq.execute("resortResourcesDialog.show()");
			}
			else
			{
				refreshActiveQuestionResource(event.getComponent());
			}
		}
		else
		{
			// Scroll page to top position
			scrollToTop();
			
			updateQuestionResourcesImages(false);
			updateResourcesTextFields(event.getComponent(),getQuestion().getQuestionResources().size());
			
			// Restore old question resource name
			currentQuestionResource=getActiveQuestionResource(event.getComponent());
			if (currentQuestionResource!=null)
			{
				currentQuestionResource.setName(activeQuestionResourceName);
				activeQuestionResourceName=currentQuestionResourceName;
			}
			
			refreshActiveQuestionResource(event.getComponent());
		}
	}
    
	/**
	 * Action listener to apply changes from the dialog for re-sorting additional resources added 
	 * to this question if we accept that dialog.
	 * @param event Action event
	 */
	public void acceptReSortResources(ActionEvent event)
	{
		// We change question resources positions
		for (int questionResourcePos=1;questionResourcePos<=questionResourcesSorting.size();questionResourcePos++)
		{
			QuestionResource questionResource=questionResourcesSorting.get(questionResourcePos-1);
			questionResource.setPosition(questionResourcePos);
			if (questionResourcePos==activeQuestionResourceIndex+1)
			{
				activeQuestionResourceName=questionResource.getName()==null?"":questionResource.getName();
			}
		}
		
		getQuestion().setQuestionResources(questionResourcesSorting);
		
		// We need to update answers text fields
		updateResourcesTextFields(event.getComponent(),questionResourcesSorting.size());
		
		updateQuestionResourcesImages();
		refreshActiveQuestionResource(event.getComponent());
	}
	
	/**
	 * Action listener for updating resources information if we cancel the changes within the dialog for 
	 * re-sorting additional resources added to this question.
	 * @param event Action event
	 */
	public void cancelReSortResources(ActionEvent event)
	{
		updateQuestionResourcesImages();
		refreshActiveQuestionResource(event.getComponent());
	}
	
    /**
     * Adds a new resource to question.
	 * @param event Action event
     */
	public void addResource(ActionEvent event)
	{
		// Get question
		Question question=getQuestion();
		
		// Get current resource of question
		QuestionResource currentQuestionResource=getActiveQuestionResource(event.getComponent());
		
		String currentQuestionResourceName=activeQuestionResourceName;
		
		// We need to process some input fields
		processResourcesInputFields(getCurrentUserOperation(null),event.getComponent(),currentQuestionResource);
		
		// Check that current resource name entered by user is valid
		if (currentQuestionResource==null || checkQuestionResource(question,currentQuestionResource))
		{
			updateQuestionResourcesImages();
			
			// Add a new resorce
			QuestionResource newQuestionResource=new QuestionResource();
			newQuestionResource.setResource(getNoResource());
			newQuestionResource.setWidth(-1);
			newQuestionResource.setHeight(-1);
			question.addQuestionResource(newQuestionResource);
			int numberQuestionResources=question.getQuestionResources().size();
			
			// Change active tab of answers accordion to display the new answer
			activeQuestionResourceIndex=numberQuestionResources-1;
			activeQuestionResourceName="";
			refreshActiveQuestionResource(event.getComponent());
			
			// We need to update resources text fields
			updateResourcesTextFields(event.getComponent(),numberQuestionResources);
		}
		else
		{
			// Scroll page to top position
			scrollToTop();
			
			updateQuestionResourcesImages(false);
			updateResourcesTextFields(event.getComponent(),getQuestion().getQuestionResources().size());
			
			// Restore old question resource name
			currentQuestionResource=getActiveQuestionResource(event.getComponent());
			if (currentQuestionResource!=null)
			{
				currentQuestionResource.setName(activeQuestionResourceName);
				activeQuestionResourceName=currentQuestionResourceName;
			}
			
			refreshActiveQuestionResource(event.getComponent());
		}
	}
	
	/**
	 * Removes an resource from question.
	 * @param event Action event
	 */
	public void removeResource(ActionEvent event)
	{
		int questionResourceToRemovePosition=
			((Integer)event.getComponent().getAttributes().get("position")).intValue();
		
		// Get question
		Question question=getQuestion();
		
		// Remove resource from question
		question.removeQuestionResource(questionResourceToRemovePosition);
		
		// If it is needeed change active tab of resources accordion
		int numQuestionResources=question.getQuestionResources().size();
		if (questionResourceToRemovePosition>numQuestionResources)
		{
			activeQuestionResourceIndex=numQuestionResources-1;
		}
		
		updateQuestionResourcesImages();
		numQuestionResources=question.getQuestionResources().size();
		
		if (numQuestionResources>0)
		{
			QuestionResource activeQuestionResource=getActiveQuestionResource(event.getComponent());
			activeQuestionResourceName=activeQuestionResource==null?"":activeQuestionResource.getName();
			if (activeQuestionResourceName==null)
			{
				activeQuestionResourceName="";
			}
			
			// We need to update resources text fields
			updateResourcesTextFields(event.getComponent(),numQuestionResources);
			
			refreshActiveQuestionResource(event.getComponent());
		}
		else
		{
			activeQuestionResourceName="";
		}
	}
	
	private boolean checkSaveQuestion(Operation operation)
	{
		boolean ok=false;
		
		// Get current user session operation
		operation=getCurrentUserOperation(operation);
		
		Question question=getQuestion();
		if (question.getId()>0L)
		{
			ok=false;
			if (getEditEnabled(operation).booleanValue())
			{
				if (question.getCreatedBy().getId()==userSessionService.getCurrentUserId())
				{
					ok=true;
				}
				else
				{
					ok=getEditOtherUsersQuestionsEnabled(operation).booleanValue() && 
						(!getQuestionAuthorAdmin(operation).booleanValue() || 
						getEditAdminsQuestionsEnabled(operation).booleanValue()) && 
						(!getQuestionAuthorSuperadmin(operation).booleanValue() || 
						getEditSuperadminsQuestionsEnabled(operation).booleanValue());
				}
			}
		}
		else
		{
			ok=getAddEnabled(operation).booleanValue();
		}
		return ok;
	}
	
	// Guardamos la pregunta
	/**
	 * Save question to DB.
	 * @return Next wiew (questions page if save is sucessful, otherwise we keep actual view)
	 */
	public String saveQuestion()
	{
		String nextView=null;
		
		// Get current user session Hibernate operation
		Operation operation=getCurrentUserOperation(null);
		
		setFilterGlobalQuestionsEnabled(null);
		setFilterOtherUsersQuestionsEnabled(null);
    	setGlobalOtherUserCategoryAllowed(null);
    	resetQuestionAuthorAdmin();
    	resetQuestionAuthorSuperadmin();
    	setAddEnabled(null);
    	setEditEnabled(null);
    	setEditOtherUsersQuestionsEnabled(null);
    	setEditAdminsQuestionsEnabled(null);
    	setEditSuperadminsQuestionsEnabled(null);
    	setViewQuestionsFromOtherUsersPrivateCategoriesEnabled(null);
    	setViewQuestionsFromAdminsPrivateCategoriesEnabled(null);
    	setViewQuestionsFromSuperadminsPrivateCategoriesEnabled(null);
    	
		Question question=getQuestion();
		long questionId=question.getId();
		boolean update=questionId>0L;
		if (update && !questionsService.checkQuestionId(operation,questionId))
		{
			displayErrorPage(
				"QUESTION_UPDATE_NOT_FOUND_ERROR","The question you are trying to update no longer exists.");
		}
		else if (!checkSaveQuestion(operation) || (update && !checkCurrentCategory(operation)))
		{
	    	displayErrorPage("NON_AUTHORIZED_ACTION_ERROR","You are not authorized to execute that operation");
		}
		else
		{
			nextView="questions?faces-redirect=true";
			User currentUser=null;
			boolean reloadCategories=true;
			if (update && activeQuestionTabIndex==GENERAL_TABVIEW_TAB)
			{
				// Check question name
		    	if (!checkQuestionName(question.getName()))
		    	{
		    		nextView=null;
		    	}
			}
			else if (update && activeQuestionTabIndex==ANSWERS_TABVIEW_TAB)
			{
				if (question instanceof MultichoiceQuestion)
				{
					UIComponent viewRoot=FacesContext.getCurrentInstance().getViewRoot();
					
					// Get current answer
					Answer currentAnswer=getActiveAnswer(viewRoot);
					
					// Check that current answer name entered by user is valid
					if (checkAnswerName(currentAnswer.getName()))
					{
						activeAnswerName=currentAnswer.getName();
					}
					else
					{
						// Restore old answer tab without changing its name
						updateAnswersTextFields(viewRoot,question.getAnswers().size());
						currentAnswer.setName(activeAnswerName);
						
						nextView=null;
					}
				}
				else if (question instanceof DragDropQuestion)
				{
					UIComponent viewRoot=FacesContext.getCurrentInstance().getViewRoot();
					
					// Get current draggable item
					Answer currentDraggableItem=
						getActiveDraggableItem(FacesContext.getCurrentInstance().getViewRoot());
					
					// Get current answer
					Answer currentAnswer=getActiveDroppableAnswer(FacesContext.getCurrentInstance().getViewRoot());
					
					// Check that current draggable item and answer names entered by user are valid
					if (!checkDraggableItemName(currentDraggableItem.getName()))
					{
						nextView=null;
					}
					if (!checkAnswerName(currentAnswer.getName()))
					{
						nextView=null;
					}
					if (nextView==null)
					{
						DragDropQuestion dragDropQuestion=(DragDropQuestion)question;
						
						// Restore old draggable item tab without changing its name
						//TODO de momento solo tenemos en cuenta el grupo 1, habra que cambiarlo mas adelante
						updateDraggableItemsTextFields(viewRoot,dragDropQuestion.getDraggableItems(1).size());
						currentDraggableItem.setName(activeDraggableItemName);
						
						// Restore old answer tab without changing its name
						//TODO de momento solo tenemos en cuenta el grupo 1, habra que cambiarlo mas adelante
						updateAnswersTextFields(viewRoot,dragDropQuestion.getDroppableAnswers(1).size());
						currentAnswer.setName(activeAnswerName);
					}
					else
					{
						activeDraggableItemName=currentDraggableItem.getName();
						activeAnswerName=currentAnswer.getName();
					}
				}
				else if (question instanceof OmXmlQuestion)
				{
	    			// Check that xml content is a valid xml document
					if (!checkXmlContent(((OmXmlQuestion)question).getXmlContent()))
					{
						nextView=null;
					}
				}
			}
			else if (update && activeQuestionTabIndex==resourcesTabviewTab)
			{
				UIComponent viewRoot=FacesContext.getCurrentInstance().getViewRoot();
				
				// Get current resource
				QuestionResource currentQuestionResource=getActiveQuestionResource(viewRoot);
				
				if (currentQuestionResource!=null)
				{
					// Check that current resource name entered by user is valid
					if (checkQuestionResource(question,currentQuestionResource))
					{
						activeQuestionResourceName=currentQuestionResource.getName();
					}
					else
					{
						nextView=null;
						
						// Restore old resource tab without changing its name
						updateResourcesTextFields(viewRoot,question.getQuestionResources().size());
						currentQuestionResource.setName(activeQuestionResourceName);
					}
				}
			}
			
	    	// Check question category
			if (!categoriesService.checkCategoryId(operation,getCategoryId()))
			{
	    		addErrorMessage(
	    			question.getId()>0L?"QUESTION_CATEGORY_UPDATE_NOT_FOUND":"QUESTION_CATEGORY_ADD_NOT_FOUND");
	    		nextView=null;
	    		
				// Refresh questions categories from DB
		    	resetQuestionsCategories(operation);
		    	
		    	// We also need to change active tab to display categories combo
		    	setNewActiveQuestionTab(GENERAL_WIZARD_TAB);
			}
			else if (checkCategory(operation))
			{
	   			if (!checkAvailableQuestionName(operation))
	   			{
	   				addErrorMessage("QUESTION_NAME_ALREADY_DECLARED");
	   				nextView=null;
	   			}
			}
			else
			{
	    		addErrorMessage("QUESTION_CATEGORY_NOT_GRANTED_ERROR");
	    		nextView=null;
	    		
	    		setCategoryId(operation,0L);
	    		
				// Refresh questions categories from DB
		    	resetQuestionsCategories(operation);
		    	reloadCategories=false;
		    	
		    	// We also need to change active tab to display categories combo
		    	setNewActiveQuestionTab(GENERAL_WIZARD_TAB);
			}
			// Right answers validation
			if (!checkAnswersFields())
			{
				nextView=null;
				question=null;
			}
			
			if (nextView!=null)
			{
				// End current user session Hibernate operation
				userSessionService.endCurrentUserOperation();
				
				Operation writeOp=null;
				try
				{
					// Start a new Hibernate operation
					writeOp=HibernateUtil.startOperation();
					
					currentUser=userSessionService.getCurrentUser(writeOp);
					
					// We need to remove added resources that had been deleted before saving
					updateResourcesImages(writeOp,true);
					
					// We set modification user & datetime
					Date dateNow=new Date();
					question.setModifiedBy(currentUser);
					question.setTimemodified(dateNow);
					
					if (question.getId()>0L) //Update question
					{
						questionsService.updateQuestion(writeOp,question);
					}
					else // New question
					{
						// As this a new question we also set creation user & datetime
						question.setCreatedBy(currentUser);
						question.setTimecreated(dateNow); 
						questionsService.addQuestion(writeOp,question);
					}
					
					// Do commit
					writeOp.commit();
				}
				catch (ServiceException se)
				{
					// Do rollback
					writeOp.rollback();
					
					throw se;
				}
				finally
				{
					// End Hibernate operation
					HibernateUtil.endOperation(writeOp);
				}
			}
			if (nextView==null)
			{
				// Reload categories if needed
				if (reloadCategories)
				{
					setQuestionsCategories(null);
				}
				
				// Reset user permissions
				setFilterGlobalResourcesEnabled(null);
				setFilterOtherUsersResourcesEnabled(null);
	    		setUseGlobalResources(null);
	    		setUseOtherUsersResources(null);
	    		setViewResourcesFromOtherUsersPrivateCategoriesEnabled(null);
	    		setViewResourcesFromAdminsPrivateCategoriesEnabled(null);
	    		setViewResourcesFromSuperadminsPrivateCategoriesEnabled(null);
	    		resetAdmins();
	    		resetSuperadmins();
			}
		}
		return nextView;
	}
	
	private void setNewActiveQuestionTab(String newActiveQuestionTab)
	{
		if (!newActiveQuestionTab.equals(getActiveQuestionTabName()))
		{
			UIComponent viewRoot=FacesContext.getCurrentInstance().getViewRoot();
			if (getQuestion().getId()>0L)
			{
    			TabView questionFormTabs=(TabView)viewRoot.findComponent(":questionForm:tabview");
    			activeQuestionTabIndex=getQuestionTabIndex(newActiveQuestionTab);
    			questionFormTabs.setActiveIndex(activeQuestionTabIndex);
			}
			else
			{
	    		Wizard questionFormWizard=(Wizard)viewRoot.findComponent(":questionForm:wizard");
	    		activeQuestionTabName=newActiveQuestionTab;
	    		questionFormWizard.setStep(newActiveQuestionTab);
			}
		}
	}
	
	/**
	 * Resets a property with a default value.<br/><br/>
	 * Currently is only supported in the case of 'True/False' questions for 'trueText' and 'falseText' 
	 * properties used within &lt;p:inputText&gt; components for input of text for true and false answers 
	 * respectively.
	 * @param event Action event
	 */
	public void reset(ActionEvent event)
	{
		String property=(String)event.getComponent().getAttributes().get("property");
		if (property.equals("trueText"))
		{
			setTrueText(null);
		}
		else if (property.equals("falseText"))
		{
			setFalseText(null);
		}
	}
	
	/**
	 * @param component Component that triggered the listener that called this method
	 * @return Active answer
	 */
	private Answer getActiveAnswer(UIComponent component)
	{
		Answer answer=null;
		String answersAccordionId=null;
		Question question=getQuestion();
		if (question.getId()==0L)
		{
			answersAccordionId=":questionForm:answersAccordion";
		}
		else
		{
			answersAccordionId=":questionForm:tabview:answersAccordion";
		}
		AccordionPanel answersAccordion=(AccordionPanel)component.findComponent(answersAccordionId);
		if (answersAccordion!=null)
		{
			answer=question.getAnswer(activeAnswerIndex+1);
		}
		return answer;
	}
	
	/**
	 * @param component Component that triggered the listener that called this method
	 * @return Active draggable item of a "Drag & Drop" question (&lt;dragbox&gt;)
	 */
	private Answer getActiveDraggableItem(UIComponent component)
	{
		Answer draggableItem=null;
		Question question=getQuestion();
		if (question instanceof DragDropQuestion)
		{
			DragDropQuestion dragDropQuestion=(DragDropQuestion)question;
			String draggableItemsAccordionId=null;
			if (dragDropQuestion.getId()==0L)
			{
				draggableItemsAccordionId=":questionForm:draggableItemsAccordion";
			}
			else
			{
				draggableItemsAccordionId=":questionForm:tabview:draggableItemsAccordion";
			}
			AccordionPanel draggableItemsAccordion=
				(AccordionPanel)component.findComponent(draggableItemsAccordionId);
			if (draggableItemsAccordion!=null)
			{
				//TODO de momento solo tenemos en cuenta el grupo 1, habra que cambiarlo mas adelante
				draggableItem=dragDropQuestion.getDraggableItem(1,activeDraggableItemIndex+1);
			}
		}
		return draggableItem;
	}
	
	/**
	 * @param component Component that triggered the listener that called this method
	 * @return Active answer of a "Drag & Drop" question (&lt;dropbox&gt;)
	 */
	private Answer getActiveDroppableAnswer(UIComponent component)
	{
		Answer answer=null;
		Question question=getQuestion();
		if (question instanceof DragDropQuestion)
		{
			DragDropQuestion dragDropQuestion=(DragDropQuestion)question;
			String answersAccordionId=null;
			if (dragDropQuestion.getId()==0L)
			{
				answersAccordionId=":questionForm:answersAccordion";
			}
			else
			{
				answersAccordionId=":questionForm:tabview:answersAccordion";
			}
			AccordionPanel answersAccordion=(AccordionPanel)component.findComponent(answersAccordionId);
			if (answersAccordion!=null)
			{
				//TODO de momento solo tenemos en cuenta el grupo 1, habra que cambiarlo mas adelante
				answer=dragDropQuestion.getDroppableAnswer(1,activeAnswerIndex+1);
			}
		}
		return answer;
	}
	
	/**
	 * @param component Component that triggered the listener that called this method
	 * @return Active resource of question
	 */
	private QuestionResource getActiveQuestionResource(UIComponent component)
	{
		QuestionResource questionResource=null;
		String questionResourcesAccordionId=null;
		Question question=getQuestion();
		if (question.getId()==0L)
		{
			questionResourcesAccordionId=":questionForm:questionResourcesAccordion";
		}
		else
		{
			questionResourcesAccordionId=":questionForm:tabview:questionResourcesAccordion";
		}
		AccordionPanel questionResourcesAccordion=
			(AccordionPanel)component.findComponent(questionResourcesAccordionId);
		if (questionResourcesAccordion!=null)
		{
			questionResource=question.getQuestionResource(activeQuestionResourceIndex+1);
		}
		return questionResource;
	}
	
	/**
	 * Refresh answers accordion to display current active tab.<br/><br/>
	 * Useful to avoid undesired tab changes after updating an accordion.
	 * @param component Component that triggered the listener that called this method
	 */
	private void refreshActiveAnswer(UIComponent component)
	{
		String answersAccordionId=null;
		if (getQuestion().getId()==0L)
		{
			answersAccordionId=":questionForm:answersAccordion";
		}
		else
		{
			answersAccordionId=":questionForm:tabview:answersAccordion";
		}
		AccordionPanel answersAccordion=(AccordionPanel)component.findComponent(answersAccordionId);
		if (answersAccordion!=null)
		{
			if (activeAnswerIndex>=0)
			{
				answersAccordion.setActiveIndex(Integer.toString(activeAnswerIndex));
			}
			else
			{
				answersAccordion.setActiveIndex(null);
			}
		}
	}
	
	/**
	 * Refresh draggable items accordion to display current active tab.<br/><br/>
	 * Useful to avoid undesired tab changes after updating an accordion.
	 * @param component Component that triggered the listener that called this method
	 */
	private void refreshActiveDraggableItem(UIComponent component)
	{
		String draggableItemsAccordionId=null;
		if (getQuestion().getId()==0L)
		{
			draggableItemsAccordionId=":questionForm:draggableItemsAccordion";
		}
		else
		{
			draggableItemsAccordionId=":questionForm:tabview:draggableItemsAccordion";
		}
		AccordionPanel draggableItemsAccordion=(AccordionPanel)component.findComponent(draggableItemsAccordionId);
		if (draggableItemsAccordion!=null)
		{
			if (activeDraggableItemIndex>=0)
			{
				draggableItemsAccordion.setActiveIndex(Integer.toString(activeDraggableItemIndex));
			}
			else
			{
				draggableItemsAccordion.setActiveIndex(null);
			}
		}
	}
	
	/**
	 * Refresh resources of question accordion to display current active tab.<br/><br/>
	 * Useful to avoid undesired tab changes after updating an accordion.
	 * @param component Component that triggered the listener that called this method
	 */
	private void refreshActiveQuestionResource(UIComponent component)
	{
		String questionResourcesAccordionId=null;
		if (getQuestion().getId()==0L)
		{
			questionResourcesAccordionId=":questionForm:questionResourcesAccordion";
		}
		else
		{
			questionResourcesAccordionId=":questionForm:tabview:questionResourcesAccordion";
		}
		AccordionPanel questionResourcesAccordion=
			(AccordionPanel)component.findComponent(questionResourcesAccordionId);
		if (questionResourcesAccordion!=null)
		{
			if (activeQuestionResourceIndex>=0)
			{
				questionResourcesAccordion.setActiveIndex(Integer.toString(activeQuestionResourceIndex));
			}
			else
			{
				questionResourcesAccordion.setActiveIndex(null);
			}
		}
	}
	
	/**
	 * Process some input fields (name, category, level, questionText) of the common data tab of a question.
	 * @param component Component that triggered the listener that called this method
	 */
	private void processCommonDataInputFields(UIComponent component)
	{
		FacesContext context=FacesContext.getCurrentInstance();
		String nameInputId=null;
		String categoryInputId=null;
		String levelInputId=null;
		String questionTextInputId=null;
		String displayEquationsInputId=null;
		Question question=getQuestion();
		if (question.getId()==0L)
		{
			nameInputId=":questionForm:nameInput";
			categoryInputId=":questionForm:categoryInput";
			levelInputId=":questionForm:levelInput";
			questionTextInputId=":questionForm:questionTextInput";
			displayEquationsInputId=":questionForm:displayEquationsInput";
		}
		else
		{
			nameInputId=":questionForm:tabview:nameInput";
			categoryInputId=":questionForm:tabview:categoryInput";
			levelInputId=":questionForm:tabview:levelInput";
			questionTextInputId=":questionForm:tabview:questionTextInput";
			displayEquationsInputId=":questionForm:tabview:displayEquationsInput";
		}
		UIInput nameInput=(UIInput)component.findComponent(nameInputId);
		nameInput.processDecodes(context);
		if (nameInput.getSubmittedValue()!=null)
		{
			question.setName((String)nameInput.getSubmittedValue());
		}
		UIInput categoryInput=(UIInput)component.findComponent(categoryInputId);
		categoryInput.processDecodes(context);
		if (categoryInput.getSubmittedValue()!=null)
		{
			long catId=Long.parseLong((String)(categoryInput.getSubmittedValue()));
			if (catId!=categoryId)
			{
				category=null;
				categoryId=catId;
			}
		}
		UIInput levelInput=(UIInput)component.findComponent(levelInputId);
		levelInput.processDecodes(context);
		if (levelInput.getSubmittedValue()!=null)
		{
			question.setLevelString((String)levelInput.getSubmittedValue());
		}
		UIInput questionTextInput=(UIInput)component.findComponent(questionTextInputId);
		questionTextInput.processDecodes(context);
		if (questionTextInput.getSubmittedValue()!=null)
		{
			question.setQuestionText((String)questionTextInput.getSubmittedValue());
		}
		UIInput displayEquationsInput=(UIInput)component.findComponent(displayEquationsInputId);
		displayEquationsInput.processDecodes(context);
		if (displayEquationsInput.getSubmittedValue()!=null)
		{
			question.setDisplayEquations(
				Boolean.valueOf((String)displayEquationsInput.getSubmittedValue()).booleanValue());
		}
	}
	
	/**
	 * Process some input fields of the answers tab of a question.
	 * @param component Component that triggered the listener that called this method
	 * @param answer Answer to process or null if we don't want to process answer
	 */
	private void processAnswersInputFields(UIComponent component,Answer answer)
	{
		processAnswersInputFields(component,answer,null,true,new ArrayList<String>(0));
	}
	
	/**
	 * Process some input fields of the answers tab of a question.
	 * @param component Component that triggered the listener that called this method
	 * @param answer Answer to process or null if we don't want to process answer
	 * @param commonInputs true to process common input fields (not referred to a single answer), 
	 * false to not process them
	 */
	@SuppressWarnings("unused")
	private void processAnswersInputFields(UIComponent component,Answer answer,boolean commonInputs)
	{
		processAnswersInputFields(component,answer,null,commonInputs,new ArrayList<String>(0));
	}
	
	/**
	 * Process some input fields of the answers tab of a question.
	 * @param component Component that triggered the listener that called this method
	 * @param answer Answer to process or null if we don't want to process answer
	 * @param commonInputs true to process common input fields (not referred to an answer), false to not process them
	 * @param exceptions List of identifiers of input fields to be excluded from processing 
	 */
	@SuppressWarnings("unused")
	private void processAnswersInputFields(UIComponent component,Answer answer,boolean commonInputs,
		List<String> exceptions)
	{
		processAnswersInputFields(component,answer,null,commonInputs,exceptions);
	}
	
	/**
	 * Process some input fields of the answers tab of a question.
	 * @param component Component that triggered the listener that called this method
	 * @param answer1 First answer to process or null if we don't want to process first answer
	 * @param answer2 Second answer to process or null if we don't want to process second answer
	 */
	private void processAnswersInputFields(UIComponent component,Answer answer1,Answer answer2)
	{
		processAnswersInputFields(component,answer1,answer2,true,new ArrayList<String>(0));
	}
	
	/**
	 * Process some input fields of the answers tab of a question.
	 * @param component Component that triggered the listener that called this method
	 * @param answer1 First answer to process or null if we don't want to process first answer
	 * @param answer2 Second answer to process or null if we don't want to process second answer
	 * @param commonInputs true to process common input fields (not referred to an answer), 
	 * false to not process them
	 */
	private void processAnswersInputFields(UIComponent component,Answer answer1,Answer answer2,boolean commonInputs)
	{
		processAnswersInputFields(component,answer1,answer2,commonInputs,new ArrayList<String>(0));
	}
	
	/**
	 * Process some input fields of the answers tab of a question.
	 * @param component Component that triggered the listener that called this method
	 * @param answer1 First answer to process or null if we don't want to process first answer
	 * @param answer2 Second answer to process or null if we don't want to process second answer
	 * @param commonInputs true to process common input fields (not referred to an answer), 
	 * false to not process them
	 * @param exceptions List of identifiers of input fields to be excluded from processing 
	 */
	private void processAnswersInputFields(UIComponent component,Answer answer1,Answer answer2,
		boolean commonInputs,List<String> exceptions)
	{
		// Get question
		Question question=getQuestion();
		/*
		if (getQuestion() instanceof TrueFalseQuestion)
		{
			if (commonInputs)
			{
				processTrueFalseAnswersInputFields(component,exceptions);
			}
		}
		else
		*/
		if (question instanceof MultichoiceQuestion)
		{
			// Multichoice questions only need to process an answer (first answer)
			processMultichoiceAnswersInputFields(component,answer1,commonInputs,exceptions);
		}
		else if (question instanceof DragDropQuestion)
		{
			// Drag & Drop questions can process a draggable item (first answer) and a droppable answer 
			// (second answer)
			processDragDropAnswersInputFields(component,answer1,answer2,commonInputs,exceptions);
		}
		else if (question instanceof OmXmlQuestion)
		{
			// Xml (Openmark syntax) questions don't need to proccess answer but instead xml content
			processOmXmlContentInputFields(component);
		}
	}
	
	// TRUE_FALSE questions use special fields for answers, so no answer processing is needed
	/**
	 * Process some input fields of the answers tab of a true/false question.
	 * @param component Component that triggered the listener that called this method
	 * @param exceptions List of identifiers of input fields to be excluded from processing 
	 */
	/*
	private void processTrueFalseAnswersInputFields(UIComponent component,List<String> exceptions)
	{
	}
	*/
	
	/**
	 * Process some input fields of the answers tab of a multichoice question.
	 * @param component Component that triggered the listener that called this method
	 * @param answer Answer to process or null if we don't want to process answer
	 * @param commonInputs true to process common input fields (not referred to an answer), 
	 * false to not process them
	 * @param exceptions List of identifiers of input fields to be excluded from processing 
	 */
	private void processMultichoiceAnswersInputFields(UIComponent component,Answer answer,boolean commonInputs,
		List<String> exceptions)
	{
		FacesContext context=FacesContext.getCurrentInstance();
		if (commonInputs)
		{
			String singleResponseId=null;
			String shuffleAnswersId=null;
			Question question=getQuestion();
			if (question.getId()==0L)
			{
				singleResponseId=":questionForm:singleResponse";
				shuffleAnswersId=":questionForm:shuffleAnswers";
			}
			else
			{
				singleResponseId=":questionForm:tabview:singleResponse";
				shuffleAnswersId=":questionForm:tabview:shuffleAnswers";
			}
			UIInput singleResponse=(UIInput)component.findComponent(singleResponseId);
			if (!exceptions.contains(singleResponse.getId()))
			{
				singleResponse.processDecodes(context);
				if (singleResponse.getSubmittedValue()!=null)
				{
					((MultichoiceQuestion)question).setSingle(
						Boolean.valueOf((String)singleResponse.getSubmittedValue()));
				}
			}
			UIInput shuffleAnswers=(UIInput)component.findComponent(shuffleAnswersId);
			if (!exceptions.contains(shuffleAnswers.getId()))
			{
				shuffleAnswers.processDecodes(context);
				if (shuffleAnswers.getSubmittedValue()!=null)
				{
					((MultichoiceQuestion)question).setShuffle(
						Boolean.valueOf((String)shuffleAnswers.getSubmittedValue()));
				}
			}
		}
		if (answer!=null)
		{
			String answersAccordionId=null;
			if (question.getId()==0L)
			{
				answersAccordionId=":questionForm:answersAccordion";
			}
			else
			{
				answersAccordionId=":questionForm:tabview:answersAccordion";
			}
			AccordionPanel answersAccordion=(AccordionPanel)component.findComponent(answersAccordionId);
			if (answersAccordion!=null && answer!=null)
			{
				// Save current row index and set row index to point active tab
				int currentRowIndex=answersAccordion.getRowIndex();
				answersAccordion.setRowIndex(activeAnswerIndex);
				
				UIComponent tab=answersAccordion.getChildren().get(0);
				int inputsProcessed=0;
				for (UIComponent panelGrid:tab.getChildren())
				{
					int subInputsProcessed=0;
					for (UIComponent panelGridChild:panelGrid.getChildren())
					{
						if (panelGridChild.getId().equals("answerName"))
						{
							if (!exceptions.contains("answerName"))
							{
								UIInput answerName=(UIInput)panelGridChild;
								answerName.processDecodes(context);
								if (answerName.getSubmittedValue()!=null)
								{
									answer.setName((String)answerName.getSubmittedValue());
								}
							}
							inputsProcessed++;
							break;
						}
						else if (panelGridChild.getId().equals("answerText"))
						{
							if (!exceptions.contains("answerText"))
							{
								UIInput answerText=(UIInput)panelGridChild;
								answerText.processDecodes(context);
								if (answerText.getSubmittedValue()!=null)
								{
									answer.setText((String)answerText.getSubmittedValue());
								}
							}
							inputsProcessed++;
							break;
						}
						else if (panelGridChild.getId().equals("answerCorrect"))
						{
							if (!exceptions.contains("answerCorrect"))
							{
								UIInput answerCorrect=(UIInput)panelGridChild;
								answerCorrect.processDecodes(context);
								if (answerCorrect.getSubmittedValue()!=null)
								{
									answer.setCorrect(
										Boolean.valueOf((String)answerCorrect.getSubmittedValue()));
								}
							}
							inputsProcessed++;
							subInputsProcessed++;
							if (subInputsProcessed==2)
							{
								break;
							}
						}
						else if (panelGridChild.getId().equals("answerFixed"))
						{
							if (!exceptions.contains("answerFixed"))
							{
								UIInput answerFixed=(UIInput)panelGridChild;
								answerFixed.processDecodes(context);
								if (answerFixed.getSubmittedValue()!=null)
								{
									answer.setFixed(Boolean.valueOf((String)answerFixed.getSubmittedValue()));
								}
							}
							inputsProcessed++;
							subInputsProcessed++;
							if (subInputsProcessed==2)
							{
								break;
							}
						}
					}
					if (inputsProcessed==4)
					{
						break;
					}
				}
				
				// Restore saved row index
				answersAccordion.setRowIndex(currentRowIndex);
			}
		}
	}
	
	/**
	 * Process some input fields of the answers tab of a "Drag & Drop" question.
	 * @param component Component that triggered the listener that called this method
	 * @param draggableItem Draggable item to process or null if we don't want to process draggable item
	 * @param answer Answer to process or null if we don't want to process answer
	 * @param commonInputs true to process common input fields (not referred to an answer), 
	 * false to not process them
	 * @param exceptions List of identifiers of input fields to be excluded from processing 
	 */
	private void processDragDropAnswersInputFields(UIComponent component,Answer draggableItem,
		Answer answer,boolean commonInputs,List<String> exceptions)
	{
		FacesContext context=FacesContext.getCurrentInstance();
		
		Question question=getQuestion();
		if (commonInputs)
		{
			String infiniteId=null;
			String forceBordersId=null;
			String shuffleDragsId=null;
			String shuffleDropsId=null;
			if (question.getId()==0L)
			{
				infiniteId=":questionForm:infinite";
				forceBordersId=":questionForm:forceBorders";
				shuffleDragsId=":questionForm:shuffleDrags";
				shuffleDropsId=":questionForm:shuffleDrops";
			}
			else
			{
				infiniteId=":questionForm:tabview:infinite";
				forceBordersId=":questionForm:tabview:forceBorders";
				shuffleDragsId=":questionForm:tabview:shuffleDrags";
				shuffleDropsId=":questionForm:tabview:shuffleDrops";
			}
			UIInput infinite=(UIInput)component.findComponent(infiniteId);
			if (!exceptions.contains(infinite.getId()))
			{
				infinite.processDecodes(context);
				if (infinite.getSubmittedValue()!=null)
				{
					((DragDropQuestion)question).setInfinite(Boolean.valueOf((String)infinite.getSubmittedValue()));
				}
			}
			UIInput forceBorders=(UIInput)component.findComponent(forceBordersId);
			if (!exceptions.contains(forceBorders.getId()))
			{
				forceBorders.processDecodes(context);
				if (forceBorders.getSubmittedValue()!=null)
				{
					((DragDropQuestion)question).setForceBorders(
						Boolean.valueOf((String)forceBorders.getSubmittedValue()));
				}
			}
			UIInput shuffleDrags=(UIInput)component.findComponent(shuffleDragsId);
			if (!exceptions.contains(shuffleDrags.getId()))
			{
				shuffleDrags.processDecodes(context);
				if (shuffleDrags.getSubmittedValue()!=null)
				{
					((DragDropQuestion)question).setShuffleDrags(
						Boolean.valueOf((String)shuffleDrags.getSubmittedValue()));
				}
			}
			UIInput shuffleDrops=(UIInput)component.findComponent(shuffleDropsId);
			if (!exceptions.contains(shuffleDrops.getId()))
			{
				shuffleDrops.processDecodes(context);
				if (shuffleDrops.getSubmittedValue()!=null)
				{
					((DragDropQuestion)question).setShuffleDrops(
						Boolean.valueOf((String)shuffleDrops.getSubmittedValue()));
				}
			}
		}
		if (draggableItem!=null)
		{
			String draggableItemsAccordionId=null;
			if (question.getId()==0L)
			{
				draggableItemsAccordionId=":questionForm:draggableItemsAccordion";
			}
			else
			{
				draggableItemsAccordionId=":questionForm:tabview:draggableItemsAccordion";
			}
			AccordionPanel draggableItemsAccordion=
				(AccordionPanel)component.findComponent(draggableItemsAccordionId);
			if (draggableItemsAccordion!=null)
			{
				// Save current row index and set row index to point active tab
				int currentRowIndex=draggableItemsAccordion.getRowIndex();
				draggableItemsAccordion.setRowIndex(activeDraggableItemIndex);
				
				UIComponent tab=draggableItemsAccordion.getChildren().get(0);
				int inputsProcessed=0;
				for (UIComponent panelGrid:tab.getChildren())
				{
					for (UIComponent panelGridChild:panelGrid.getChildren())
					{
						if (panelGridChild.getId().equals("draggableItemName"))
						{
							if (!exceptions.contains("draggableItemName"))
							{
								UIInput draggableItemName=(UIInput)panelGridChild;
								draggableItemName.processDecodes(context);
								if (draggableItemName.getSubmittedValue()!=null)
								{
									draggableItem.setName((String)draggableItemName.getSubmittedValue());
								}
							}
							inputsProcessed++;
							break;
						}
						else if (panelGridChild.getId().equals("draggableItemText"))
						{
							if (!exceptions.contains("draggableItemText"))
							{
								UIInput draggableItemText=(UIInput)panelGridChild;
								draggableItemText.processDecodes(context);
								if (draggableItemText.getSubmittedValue()!=null)
								{
									draggableItem.setText((String)draggableItemText.getSubmittedValue());
								}
							}
							inputsProcessed++;
							break;
						}
						else if (panelGridChild.getId().equals("draggableItemFixed"))
						{
							if (!exceptions.contains("draggableItemFixed"))
							{
								UIInput draggableItemFixed=(UIInput)panelGridChild;
								draggableItemFixed.processDecodes(context);
								if (draggableItemFixed.getSubmittedValue()!=null)
								{
									draggableItem.setFixed(
										Boolean.valueOf((String)draggableItemFixed.getSubmittedValue()));
								}
							}
							inputsProcessed++;
							break;
						}
					}
					if (inputsProcessed==3)
					{
						break;
					}
				}
				
				// Restore saved row index
				draggableItemsAccordion.setRowIndex(currentRowIndex);
			}
		}
		if (answer!=null)
		{
			String answersAccordionId=null;
			if (question.getId()==0L)
			{
				answersAccordionId=":questionForm:answersAccordion";
			}
			else
			{
				answersAccordionId=":questionForm:tabview:answersAccordion";
			}
			AccordionPanel answersAccordion=(AccordionPanel)component.findComponent(answersAccordionId);
			if (answersAccordion!=null && answer!=null)
			{
				// Save current row index and set row index to point active tab
				int currentRowIndex=answersAccordion.getRowIndex();
				answersAccordion.setRowIndex(activeAnswerIndex);
				
				UIComponent tab=answersAccordion.getChildren().get(0);
				int inputsProcessed=0;
				for (UIComponent panelGrid:tab.getChildren())
				{
					int subInputsProcessed=0;
					for (UIComponent panelGridChild:panelGrid.getChildren())
					{
						if (panelGridChild.getId().equals("answerName"))
						{
							if (!exceptions.contains("answerName"))
							{
								UIInput answerName=(UIInput)panelGridChild;
								answerName.processDecodes(context);
								if (answerName.getSubmittedValue()!=null)
								{
									answer.setName((String)answerName.getSubmittedValue());
								}
							}
							inputsProcessed++;
							break;
						}
						else if (panelGridChild.getId().equals("answerText"))
						{
							if (!exceptions.contains("answerText"))
							{
								UIInput answerText=(UIInput)panelGridChild;
								answerText.processDecodes(context);
								if (answerText.getSubmittedValue()!=null)
								{
									answer.setText((String)answerText.getSubmittedValue());
								}
							}
							inputsProcessed++;
							break;
						}
						else if (panelGridChild.getId().equals("rightAnswer"))
						{
							if (!exceptions.contains("rightAnswer"))
							{
								UIInput rightAnswer=(UIInput)panelGridChild;
								rightAnswer.processDecodes(context);
								if (rightAnswer.getSubmittedValue()!=null)
								{
									((DragDropAnswer)answer).setRightAnswerPosition(
										Integer.valueOf((String)rightAnswer.getSubmittedValue()).intValue());
								}
							}
							inputsProcessed++;
							subInputsProcessed++;
							if (subInputsProcessed==2)
							{
								break;
							}
						}
						else if (panelGridChild.getId().equals("answerFixed"))
						{
							if (!exceptions.contains("answerFixed"))
							{
								UIInput answerFixed=(UIInput)panelGridChild;
								answerFixed.processDecodes(context);
								if (answerFixed.getSubmittedValue()!=null)
								{
									answer.setFixed(Boolean.valueOf((String)answerFixed.getSubmittedValue()));
								}
							}
							inputsProcessed++;
							subInputsProcessed++;
							if (subInputsProcessed==2)
							{
								break;
							}
						}
					}
					if (inputsProcessed==4)
					{
						break;
					}
				}
				
				// Restore saved row index
				answersAccordion.setRowIndex(currentRowIndex);
			}
		}
	}
	
	/**
	 * Process some input fields of the content tab of a "XML (OpenMark syntax)" question.
	 * @param component Component that triggered the listener that called this method
	 */
	private void processOmXmlContentInputFields(UIComponent component)
	{
		FacesContext context=FacesContext.getCurrentInstance();
		String xmlContentId=null;
		OmXmlQuestion question=(OmXmlQuestion)getQuestion();
		if (question.getId()==0L)
		{
			xmlContentId=":questionForm:xmlContent";
		}
		else
		{
			xmlContentId=":questionForm:tabview:xmlContent";
		}
		UIInput xmlContent=(UIInput)component.findComponent(xmlContentId);
		xmlContent.processDecodes(context);
		if (xmlContent.getSubmittedValue()!=null)
		{
			question.setXmlContent((String)xmlContent.getSubmittedValue()); 
		}
	}
	
	/**
	 * Process some input fields of the resources tab of a question.
	 * @param operation Operation
	 * @param component Component that triggered the listener that called this method
	 * @param questionResource Resource of question to process
	 */
	private void processResourcesInputFields(Operation operation,UIComponent component,
		QuestionResource questionResource)
	{
		FacesContext context=FacesContext.getCurrentInstance();
		
		if (questionResource!=null)
		{
			String questionResourcesAccordionId=null;
			if (getQuestion().getId()==0L)
			{
				questionResourcesAccordionId=":questionForm:questionResourcesAccordion";
			}
			else
			{
				questionResourcesAccordionId=":questionForm:tabview:questionResourcesAccordion";
			}
			AccordionPanel questionResourcesAccordion=
				(AccordionPanel)component.findComponent(questionResourcesAccordionId);
			if (questionResourcesAccordion!=null)
			{
				// Save current row index and set row index to point active tab
				int currentRowIndex=questionResourcesAccordion.getRowIndex();
				questionResourcesAccordion.setRowIndex(activeQuestionResourceIndex);
				
				UIComponent tab=questionResourcesAccordion.getChildren().get(0);
				boolean inputsProcessed=false;
				for (UIComponent panelGrid:tab.getChildren())
				{
					for (UIComponent panelGridChild:panelGrid.getChildren())
					{
						if (panelGridChild.getId().equals("questionResourceName"))
						{
							UIInput questionResourceName=(UIInput)panelGridChild;
							questionResourceName.processDecodes(context);
							if (questionResourceName.getSubmittedValue()!=null)
							{
								questionResource.setName((String)questionResourceName.getSubmittedValue());
							}
							inputsProcessed=true;
							break;
						}
					}
					if (inputsProcessed)
					{
						break;
					}
				}
				
				// Restore saved row index
				questionResourcesAccordion.setRowIndex(currentRowIndex);
			}
		}
	}
	
	/**
	 * Process some input fields (correct feedback, incorrect feedback, still incorrect feedback, pass feedback
	 * and final feedback) of the feedback tab of a question.
	 * @param component Component that triggered the listener that called this method
	 */
	private void processFeedbackInputFields(UIComponent component)
	{
		FacesContext context=FacesContext.getCurrentInstance();
		String correctFeedbackId=null;
		String incorrectFeedbackId=null;
		String stillFeedbackId=null;
		String passFeedbackId=null;
		String answerFeedbackId=null;
		Question question=getQuestion();
		if (question.getId()==0L)
		{
			correctFeedbackId=":questionForm:feedbackAccordion:correctFeedback";
			incorrectFeedbackId=":questionForm:feedbackAccordion:incorrectFeedback";
			stillFeedbackId=":questionForm:feedbackAccordion:stillFeedback";
			passFeedbackId=":questionForm:feedbackAccordion:passFeedback";
			answerFeedbackId=":questionForm:feedbackAccordion:answerFeedback";
		}
		else
		{
			correctFeedbackId=":questionForm:tabview:feedbackAccordion:correctFeedback";
			incorrectFeedbackId=":questionForm:tabview:feedbackAccordion:incorrectFeedback";
			stillFeedbackId=":questionForm:tabview:feedbackAccordion:stillFeedback";
			passFeedbackId=":questionForm:tabview:feedbackAccordion:passFeedback";
			answerFeedbackId=":questionForm:tabview:feedbackAccordion:answerFeedback";
		}
		UIInput correctFeedback=(UIInput)component.findComponent(correctFeedbackId);
		correctFeedback.processDecodes(context);
		if (correctFeedback.getSubmittedValue()!=null)
		{
			question.setCorrectFeedback((String)correctFeedback.getSubmittedValue());
		}
		UIInput incorrectFeedback=(UIInput)component.findComponent(incorrectFeedbackId);
		incorrectFeedback.processDecodes(context);
		if (incorrectFeedback.getSubmittedValue()!=null)
		{
			question.setIncorrectFeedback((String)incorrectFeedback.getSubmittedValue());
		}
		UIInput stillFeedback=(UIInput)component.findComponent(stillFeedbackId);
		stillFeedback.processDecodes(context);
		if (stillFeedback.getSubmittedValue()!=null)
		{
			question.setStillFeedback((String)stillFeedback.getSubmittedValue());
		}
		UIInput passFeedback=(UIInput)component.findComponent(passFeedbackId);
		passFeedback.processDecodes(context);
		if (passFeedback.getSubmittedValue()!=null)
		{
			question.setPassFeedback((String)passFeedback.getSubmittedValue());
		}
		UIInput answerFeedback=(UIInput)component.findComponent(answerFeedbackId);
		answerFeedback.processDecodes(context);
		if (answerFeedback.getSubmittedValue()!=null)
		{
			question.setAnswerFeedback((String)answerFeedback.getSubmittedValue());
		}
	}
	
	/**
	 * Update text fields of the answers accordion of a question.<br/><br/>
	 * This is needed after some operations because text field are not always being updated correctly on 
	 * page view.
	 * @param component Component that triggered the listener that called this method
	 * @param numTabs Number of tabs to update
	 */
	private void updateAnswersTextFields(UIComponent component,int numTabs)
	{
		Question question=getQuestion();
		/*
		if (getQuestion() instanceof TrueFalseQuestion)
		{
			processTrueFalseAnswersInputFields(component);
		}
		else
		*/
		if (question instanceof MultichoiceQuestion)
		{
			updateMultichoiceAnswersTextFields(component,numTabs);
		}
		else if (question instanceof DragDropQuestion)
		{
			updateDragDropAnswersTextFields(component,numTabs);
		}
	}
	
	/**
	 * Update text fields of the draggable items accordion of a question.<br/><br/>
	 * This is needed after some operations because text field are not always being updated correctly on 
	 * page view.
	 * @param component Component that triggered the listener that called this method
	 * @param numTabs Number of tabs to update
	 */
	private void updateDraggableItemsTextFields(UIComponent component,int numTabs)
	{
		if (getQuestion() instanceof DragDropQuestion)
		{
			updateDragDropDraggableItemsTextFields(component,numTabs);
		}
	}
	
	/**
	 * Update text fields of the answers tab of a "Multichoice" question.<br/><br/>
	 * This is needed after some operations because text field are not always being updated correctly on 
	 * page view.
	 * @param component Component that triggered the listener that called this method
	 * @param numTabs Number of tabs to update
	 */
	private void updateMultichoiceAnswersTextFields(UIComponent component,int numTabs)
	{
		FacesContext context=FacesContext.getCurrentInstance();
		String answersAccordionId=null;
		Question question=getQuestion();
		if (question.getId()==0L)
		{
			answersAccordionId=":questionForm:answersAccordion";
		}
		else
		{
			answersAccordionId=":questionForm:tabview:answersAccordion";
		}
		UIData answersAccordion=(UIData)component.findComponent(answersAccordionId);
        UIComponent tab=answersAccordion.getChildren().get(0);
		UIInput answerNameInput=null;
		UIInput answerTextInput=null;
		for (UIComponent answerTabChild:tab.getChildren())
		{
			for (UIComponent panelGridChild:answerTabChild.getChildren())
			{
				if (panelGridChild.getId().equals("answerName"))
				{
					answerNameInput=(UIInput)panelGridChild;
			        for (int i=0;i<numTabs;i++)
			        {
			        	answersAccordion.setRowIndex(i);
			        	answerNameInput.pushComponentToEL(context,null);
						answerNameInput.setSubmittedValue(question.getAnswersSortedByPosition().get(i).getName());
						answerNameInput.popComponentFromEL(context);
			        }
			        answersAccordion.setRowIndex(-1);
					break;
				}
				else if (panelGridChild.getId().equals("answerText"))
				{
					answerTextInput=(UIInput)panelGridChild;
			        for (int i=0;i<numTabs;i++)
			        {
			        	answersAccordion.setRowIndex(i);
			        	answerTextInput.pushComponentToEL(context,null);
			        	answerTextInput.setSubmittedValue(question.getAnswersSortedByPosition().get(i).getText());
			        	answerTextInput.popComponentFromEL(context);
			        }
			        answersAccordion.setRowIndex(-1);
					break;
				}
			}
			if (answerNameInput!=null && answerTextInput!=null)
			{
				break;
			}
		}
	}
	
	/**
	 * Update text fields of the draggable items accordion of a "Drag & Drop" question.<br/><br/>
	 * This is needed after some operations because text field are not always being updated correctly on 
	 * page view.
	 * @param component Component that triggered the listener that called this method
	 * @param numTabs Number of tabs to update
	 */
	private void updateDragDropDraggableItemsTextFields(UIComponent component,int numTabs)
	{
		String draggableItemsAccordionId=null;
		Question question=getQuestion();
		if (question.getId()==0L)
		{
			draggableItemsAccordionId=":questionForm:draggableItemsAccordion";
		}
		else
		{
			draggableItemsAccordionId=":questionForm:tabview:draggableItemsAccordion";
		}
		UIData draggableItemsAccordion=(UIData)component.findComponent(draggableItemsAccordionId);
        UIComponent tab=draggableItemsAccordion.getChildren().get(0);
		UIInput draggableItemNameInput=null;
		UIInput draggableItemTextInput=null;
		for (UIComponent draggableItemTabChild:tab.getChildren())
		{
			for (UIComponent panelGridChild:draggableItemTabChild.getChildren())
			{
				if (panelGridChild.getId().equals("draggableItemName"))
				{
					draggableItemNameInput=(UIInput)panelGridChild;
			        for (int i=0;i<numTabs;i++)
			        {
			        	draggableItemsAccordion.setRowIndex(i);
			        	draggableItemNameInput.pushComponentToEL(FacesContext.getCurrentInstance(),null);
						//TODO de momento solo tenemos en cuenta el grupo 1, habra que cambiarlo mas adelante
			        	draggableItemNameInput.setSubmittedValue(question instanceof DragDropQuestion?
			        		((DragDropQuestion)question).getDraggableItemsSortedByPosition(1).get(i).getName():"");
			        	draggableItemNameInput.popComponentFromEL(FacesContext.getCurrentInstance());
			        }
			        draggableItemsAccordion.setRowIndex(-1);
					break;
				}
				else if (panelGridChild.getId().equals("draggableItemText"))
				{
					draggableItemTextInput=(UIInput)panelGridChild;
			        for (int i=0;i<numTabs;i++)
			        {
			        	draggableItemsAccordion.setRowIndex(i);
			        	draggableItemTextInput.pushComponentToEL(FacesContext.getCurrentInstance(),null);
						//TODO de momento solo tenemos en cuenta el grupo 1, habra que cambiarlo mas adelante
			        	draggableItemTextInput.setSubmittedValue(question instanceof DragDropQuestion?
				        	((DragDropQuestion)question).getDraggableItemsSortedByPosition(1).get(i).getText():"");
			        	draggableItemTextInput.popComponentFromEL(FacesContext.getCurrentInstance());
			        }
			        draggableItemsAccordion.setRowIndex(-1);
					break;
				}
				if (draggableItemNameInput!=null && draggableItemTextInput!=null)
				{
					break;
				}
			}
		}
	}
	
	/**
	 * Update text fields of the answers tab of a "Drag & Drop" question.<br/><br/>
	 * This is needed after some operations because text field are not always being updated correctly on 
	 * page view.
	 * @param component Component that triggered the listener that called this method
	 * @param numTabs Number of tabs to update
	 */
	private void updateDragDropAnswersTextFields(UIComponent component,int numTabs)
	{
		String answersAccordionId=null;
		Question question=getQuestion();
		if (question.getId()==0L)
		{
			answersAccordionId=":questionForm:answersAccordion";
		}
		else
		{
			answersAccordionId=":questionForm:tabview:answersAccordion";
		}
		UIData answersAccordion=(UIData)component.findComponent(answersAccordionId);
        UIComponent tab=answersAccordion.getChildren().get(0);
		UIInput answerNameInput=null;
		UIInput answerTextInput=null;
		for (UIComponent answerTabChild:tab.getChildren())
		{
			for (UIComponent panelGridChild:answerTabChild.getChildren())
			{
				if (panelGridChild.getId().equals("answerName"))
				{
					answerNameInput=(UIInput)panelGridChild;
			        for (int i=0;i<numTabs;i++)
			        {
			        	answersAccordion.setRowIndex(i);
			        	answerNameInput.pushComponentToEL(FacesContext.getCurrentInstance(),null);
						//TODO de momento solo tenemos en cuenta el grupo 1, habra que cambiarlo mas adelante
			        	answerNameInput.setSubmittedValue(question instanceof DragDropQuestion?
			        		((DragDropQuestion)question).getDroppableAnswersSortedByPosition(1).get(i).getName():
			        		"");
			        	answerNameInput.popComponentFromEL(FacesContext.getCurrentInstance());
			        }
			        answersAccordion.setRowIndex(-1);
					break;
				}
				else if (panelGridChild.getId().equals("answerText"))
				{
					answerTextInput=(UIInput)panelGridChild;
			        for (int i=0;i<numTabs;i++)
			        {
			        	answersAccordion.setRowIndex(i);
			        	answerTextInput.pushComponentToEL(FacesContext.getCurrentInstance(),null);
						//TODO de momento solo tenemos en cuenta el grupo 1, habra que cambiarlo mas adelante
			        	answerTextInput.setSubmittedValue(question instanceof DragDropQuestion?
				        	((DragDropQuestion)question).getDroppableAnswersSortedByPosition(1).get(i).getText():
				        	"");
			        	answerTextInput.popComponentFromEL(FacesContext.getCurrentInstance());
			        }
			        answersAccordion.setRowIndex(-1);
					break;
				}
				if (answerNameInput!=null && answerTextInput!=null)
				{
					break;
				}
			}
		}
	}
	
	/**
	 * Update text fields of the resource tab of a question.<br/><br/>
	 * This is needed after some operations because text fields are not always being updated correctly on 
	 * page view.
	 * @param component Component that triggered the listener that called this method
	 * @param numTabs Number of tabs to update
	 */
	private void updateResourcesTextFields(UIComponent component,int numTabs)
	{
		FacesContext context=FacesContext.getCurrentInstance();
		String questionResourcesAccordionId=null;
		Question question=getQuestion();
		if (question.getId()==0L)
		{
			questionResourcesAccordionId=":questionForm:questionResourcesAccordion";
		}
		else
		{
			questionResourcesAccordionId=":questionForm:tabview:questionResourcesAccordion";
		}
		UIData questionResourcesAccordion=(UIData)component.findComponent(questionResourcesAccordionId);
        UIComponent tab=questionResourcesAccordion.getChildren().get(0);
		UIInput questionResourceNameInput=null;
		for (UIComponent answerTabChild:tab.getChildren())
		{
			for (UIComponent panelGridChild:answerTabChild.getChildren())
			{
				if (panelGridChild.getId().equals("questionResourceName"))
				{
					questionResourceNameInput=(UIInput)panelGridChild;
			        for (int i=0;i<numTabs;i++)
			        {
			        	questionResourcesAccordion.setRowIndex(i);
			        	questionResourceNameInput.pushComponentToEL(context,null);
						questionResourceNameInput.setSubmittedValue(
							question.getQuestionResourcesSortedByPosition().get(i).getName());
						questionResourceNameInput.popComponentFromEL(context);
			        }
			        questionResourcesAccordion.setRowIndex(-1);
					break;
				}
			}
			if (questionResourceNameInput!=null)
			{
				break;
			}
		}
	}
	
	private void updateResourcesImages()
	{
		updateResourcesImages(null);
	}
	
	private void updateResourcesImages(Operation operation)
	{
		updateResourcesImages(operation,false);
	}
	
	private void updateResourcesImages(Operation operation,boolean updateAllTabs)
	{
		if (updateAllTabs)
		{
			if (operation==null)
			{
				// End current user session Hibernate operation
				userSessionService.endCurrentUserOperation();
				
				// Get current user session Hibernate operation
				operation=getCurrentUserOperation(null);
			}
			Question question=getQuestion();
			updateCommonDataResourcesImages(operation);
			if (question instanceof MultichoiceQuestion)
			{
				updateMultichoiceAnswersResourcesImages(operation,(MultichoiceQuestion)question);
			}
			else if (question instanceof DragDropQuestion)
			{
				DragDropQuestion dragDropQuestion=(DragDropQuestion)question;
				updateDragDropDraggableItemsResourcesImages(operation,dragDropQuestion);
				updateDragDropAnswersResourcesImages(operation,dragDropQuestion);
			}
			updateQuestionResourcesImages(operation);
			updateFeedbackResourcesImages(operation,true);
		}
		else
		{
			FacesContext context=FacesContext.getCurrentInstance();
			String activeQuestionTabName=getActiveQuestionTabName();
			if (GENERAL_WIZARD_TAB.equals(activeQuestionTabName))
			{
				updateCommonDataResourcesImages(operation);
			}
			else if (ANSWERS_WIZARD_TAB.equals(activeQuestionTabName))
			{
				Question question=getQuestion();
				if (question instanceof MultichoiceQuestion)
				{
					updateAnswerResourceImage(operation,getActiveAnswer(context.getViewRoot()));
				}
				else if (question instanceof DragDropQuestion)
				{
					operation=updateAnswerResourceImage(operation,getActiveDraggableItem(context.getViewRoot()));
					updateAnswerResourceImage(operation,getActiveDroppableAnswer(context.getViewRoot()));
				}
			}
			else if (RESOURCES_WIZARD_TAB.equals(activeQuestionTabName))
			{
				updateQuestionResourcesImages(operation,false);
				refreshActiveQuestionResource(context.getViewRoot());
			}
			else if (FEEDBACK_WIZARD_TAB.equals(activeQuestionTabName))
			{
				updateFeedbackResourcesImages(operation,false);
			}
		}
	}
	
	private Operation updateCommonDataResourcesImages(Operation operation)
	{
		Question question=getQuestion();
		if (question.getResource()!=null && question.getResource().getId()!=-1L)
		{
			if (operation==null)
			{
				// End current user session Hibernate operation
				userSessionService.endCurrentUserOperation();
				
				// Get current user session Hibernate operation
				operation=getCurrentUserOperation(null);
			}
			if (!updateResourceImage(operation,question.getResource()))
			{
				question.setResource(null);
				question.setResourceWidth(-1);
				question.setResourceHeight(-1);
			}
		}
		return operation;
	}
	
	private Operation updateDragDropDraggableItemsResourcesImages(DragDropQuestion dragDropQuestion)
	{
		return updateDragDropDraggableItemsResourcesImages(null,dragDropQuestion);
	}
	
	private Operation updateDragDropDraggableItemsResourcesImages(Operation operation,
		DragDropQuestion dragDropQuestion)
	{
		//TODO de momento solo tenemos en cuenta el grupo 1, habra que cambiarlo mas adelante
		for (Answer draggableItem:dragDropQuestion.getDraggableItems(1))
		{
			if (draggableItem.getResource()!=null && draggableItem.getResource().getId()!=-1L)
			{
				if (operation==null)
				{
					// End current user session Hibernate operation
					userSessionService.endCurrentUserOperation();
					
					// Get current user session Hibernate operation
					operation=getCurrentUserOperation(null);
				}
				if (!updateResourceImage(operation,draggableItem.getResource()))
				{
					draggableItem.setResource(null);
					draggableItem.setResourceWidth(-1);
					draggableItem.setResourceHeight(-1);
				}
			}
		}
		return operation;
	}
	
	private Operation updateMultichoiceAnswersResourcesImages(MultichoiceQuestion multichoiceQuestion)
	{
		return updateMultichoiceAnswersResourcesImages(null,multichoiceQuestion);
	}
	
	private Operation updateMultichoiceAnswersResourcesImages(Operation operation,
		MultichoiceQuestion multichoiceQuestion)
	{
		for (Answer answer:multichoiceQuestion.getAnswers())
		{
			if (answer.getResource()!=null && answer.getResource().getId()!=-1L)
			{
				if (operation==null)
				{
					// End current user session Hibernate operation
					userSessionService.endCurrentUserOperation();
					
					// Get current user session Hibernate operation
					operation=getCurrentUserOperation(null);
				}
				if (!updateResourceImage(operation,answer.getResource()))
				{
					answer.setResource(null);
					answer.setResourceWidth(-1);
					answer.setResourceHeight(-1);
				}
			}
		}
		return operation;
	}
	
	private Operation updateDragDropAnswersResourcesImages(DragDropQuestion dragDropQuestion)
	{
		return updateDragDropAnswersResourcesImages(null,dragDropQuestion);
	}
	
	private Operation updateDragDropAnswersResourcesImages(Operation operation,DragDropQuestion dragDropQuestion)
	{
		//TODO de momento solo tenemos en cuenta el grupo 1, habra que cambiarlo mas adelante
		for (Answer answer:dragDropQuestion.getDroppableAnswers(1))
		{
			if (answer.getResource()!=null && answer.getResource().getId()!=-1L)
			{
				if (operation==null)
				{
					// End current user session Hibernate operation
					userSessionService.endCurrentUserOperation();
					
					// Get current user session Hibernate operation
					operation=getCurrentUserOperation(null);
				}
				if (!updateResourceImage(operation,answer.getResource()))
				{
					answer.setResource(null);
					answer.setResourceWidth(-1);
					answer.setResourceHeight(-1);
				}
			}
		}
		return operation;
	}
	
	private Operation updateAnswerResourceImage(Answer answer)
	{
		return updateAnswerResourceImage(null,answer);
	}
	
	private Operation updateAnswerResourceImage(Operation operation,Answer answer)
	{
		if (answer!=null && answer.getResource()!=null && answer.getResource().getId()!=-1L)
		{
			if (operation==null)
			{
				// End current user session Hibernate operation
				userSessionService.endCurrentUserOperation();
				
				// Get current user session Hibernate operation
				operation=getCurrentUserOperation(null);
			}
			if (!updateResourceImage(operation,answer.getResource()))
			{
				answer.setResource(null);
				answer.setResourceWidth(-1);
				answer.setResourceHeight(-1);
			}
		}
		return operation;
	}
	
	private Operation updateQuestionResourcesImages()
	{
		return updateQuestionResourcesImages(null,true);
	}
	
	private Operation updateQuestionResourcesImages(Operation operation)
	{
		return updateQuestionResourcesImages(operation,true);
	}
	
	private Operation updateQuestionResourcesImages(boolean removeEvenCurrentQuestionResource)
	{
		return updateQuestionResourcesImages(null,removeEvenCurrentQuestionResource);
	}
	
	private Operation updateQuestionResourcesImages(Operation operation,boolean removeEvenCurrentQuestionResource)
	{
		Question question=getQuestion();
		if (question instanceof OmXmlQuestion)
		{
			FacesContext context=FacesContext.getCurrentInstance();
			QuestionResource currentQuestionResource=getActiveQuestionResource(context.getViewRoot());			
			
			List<QuestionResource> questionResources=question.getQuestionResources();
			List<QuestionResource> questionResourcesToRemove=new ArrayList<QuestionResource>();
			for (QuestionResource questionResource:questionResources)
			{
				if (questionResource.getResource()!=null && questionResource.getResource().getId()!=-1L)
				{
					if (operation==null)
					{
						// End current user session Hibernate operation
						userSessionService.endCurrentUserOperation();
						
						// Get current user session Hibernate operation
						operation=getCurrentUserOperation(null);
					}
					if (!updateResourceImage(operation,questionResource.getResource()))
					{
						if (removeEvenCurrentQuestionResource || !questionResource.equals(currentQuestionResource))
						{
							QuestionResource questionResourceToRemove=new QuestionResource();
							questionResourceToRemove.setId(questionResource.getId());
							questionResourceToRemove.setQuestion(question);
							questionResourceToRemove.setPosition(questionResource.getPosition());
							questionResourcesToRemove.add(questionResourceToRemove);
						}
						else
						{
							questionResource.setResource(getNoResource());
							questionResource.setWidth(-1);
							questionResource.setHeight(-1);
						}
					}
				}
			}
			if (!questionResourcesToRemove.isEmpty())
			{
				Collections.sort(questionResourcesToRemove,new Comparator<QuestionResource>()
				{
					@Override
					public int compare(QuestionResource qr1,QuestionResource qr2)
					{
						return qr2.getPosition()-qr1.getPosition();
					}
				});
				int newActiveQuestionResourceIndex=activeQuestionResourceIndex;
				for (QuestionResource questionResourceToRemove:questionResourcesToRemove)
				{
					if (questionResourceToRemove.getPosition()<activeQuestionResourceIndex+1)
					{
						newActiveQuestionResourceIndex--;
					}
					questionResources.remove(questionResourceToRemove);
				}
				int questionResourcesSize=questionResources.size();
				if (newActiveQuestionResourceIndex>=questionResourcesSize)
				{
					newActiveQuestionResourceIndex=questionResourcesSize-1;
				}
				for (QuestionResource questionResource:questionResources)
				{
					int position=questionResource.getPosition();
					for (int i=0;i<questionResourcesToRemove.size();i++)
					{
						if (questionResourcesToRemove.get(i).getPosition()<position)
						{
							position-=(questionResourcesToRemove.size()-i);
							break;
						}
					}
					questionResource.setPosition(position);
				}
				activeQuestionResourceIndex=newActiveQuestionResourceIndex;
				QuestionResource activeQuestionResource=getActiveQuestionResource(context.getViewRoot());
				activeQuestionResourceName=activeQuestionResource==null?"":activeQuestionResource.getName();
				if (activeQuestionResourceName==null)
				{
					activeQuestionResourceName="";
				}
			}
		}
		return operation;
	}
	
	private Operation updateFeedbackResourcesImages(boolean updateAllTabs)
	{
		return updateFeedbackResourcesImages(null,updateAllTabs);
	}
	
	private Operation updateFeedbackResourcesImages(Operation operation,boolean updateAllTabs)
	{
		if (updateAllTabs)
		{
			operation=updateCorrectFeedbackResourceImage(operation);
			operation=updateIncorrectFeedbackResourcesImages(operation);
			operation=updatePassFeedbackResourceImage(operation);
			operation=updateFinalFeedbackResourceImage(operation);
		}
		else
		{
			switch (activeFeedbackTabIndex)
			{
				case CORRECT_FEEDBACK_TAB:
					operation=updateCorrectFeedbackResourceImage(operation);
					break;
				case INCORRECT_FEEDBACK_TAB:
					operation=updateIncorrectFeedbackResourcesImages(operation);
					break;
				case PASS_FEEDBACK_TAB:
					operation=updatePassFeedbackResourceImage(operation);
					break;
				case FINAL_FEEDBACK_TAB:
					operation=updateFinalFeedbackResourceImage(operation);
			}
		}
		return operation;
	}
	
	private Operation updateCorrectFeedbackResourceImage(Operation operation)
	{
		Question question=getQuestion();
		if (question.getCorrectFeedbackResource()!=null && question.getCorrectFeedbackResource().getId()!=-1L)
		{
			if (operation==null)
			{
				// End current user session Hibernate operation
				userSessionService.endCurrentUserOperation();
				
				// Get current user session Hibernate operation
				operation=getCurrentUserOperation(null);
			}
			if (!updateResourceImage(operation,question.getCorrectFeedbackResource()))
			{
				question.setCorrectFeedbackResource(null);
				question.setCorrectFeedbackResourceWidth(-1);
				question.setCorrectFeedbackResourceHeight(-1);
			}
		}
		return operation;
	}
	
	private Operation updateIncorrectFeedbackResourcesImages()
	{
		return updateIncorrectFeedbackResourcesImages(null);
	}
	
	private Operation updateIncorrectFeedbackResourcesImages(Operation operation)
	{
		operation=updateIncorrectFeedbackResourceImage(operation);
		if (isAdvancedFeedbacksEnabled())
		{
			operation=updateAdvancedFeedbacksResourcesImages(operation);
		}
		return operation;
	}
	
	private Operation updateIncorrectFeedbackResourceImage(Operation operation)
	{
		Question question=getQuestion();
		if (question.getIncorrectFeedbackResource()!=null && question.getIncorrectFeedbackResource().getId()!=-1L)
		{
			if (operation==null)
			{
				// End current user session Hibernate operation
				userSessionService.endCurrentUserOperation();
				
				// Get current user session Hibernate operation
				operation=getCurrentUserOperation(null);
			}
			if (!updateResourceImage(operation,question.getIncorrectFeedbackResource()))
			{
				question.setIncorrectFeedbackResource(null);
				question.setIncorrectFeedbackResourceWidth(-1);
				question.setIncorrectFeedbackResourceHeight(-1);
			}
		}
		return operation;
	}
	
	private Operation updateAdvancedFeedbacksResourcesImages()
	{
		return updateAdvancedFeedbacksResourcesImages(null);
	}
	
	private Operation updateAdvancedFeedbacksResourcesImages(Operation operation)
	{
		for (Feedback feedback:getQuestion().getFeedbacks())
		{
			if (feedback.getResource()!=null && feedback.getResource().getId()!=-1L)
			{
				if (operation==null)
				{
					// End current user session Hibernate operation
					userSessionService.endCurrentUserOperation();
					
					// Get current user session Hibernate operation
					operation=getCurrentUserOperation(null);
				}
				if (!updateResourceImage(operation,feedback.getResource()))
				{
					feedback.setResource(null);
					feedback.setResourceWidth(-1);
					feedback.setResourceHeight(-1);
				}
			}
		}
		for (FeedbackBean feedback:getFeedbacks())
		{
			if (feedback.getResource()!=null && feedback.getResource().getId()!=-1L)
			{
				if (operation==null)
				{
					// End current user session Hibernate operation
					userSessionService.endCurrentUserOperation();
					
					// Get current user session Hibernate operation
					operation=getCurrentUserOperation(null);
				}
				if (!updateResourceImage(operation,feedback.getResource()))
				{
					feedback.setResource(null);
					feedback.setResourceWidth(-1);
					feedback.setResourceHeight(-1);
				}
			}
		}
		return operation;
	}
	
	private Operation updatePassFeedbackResourceImage(Operation operation)
	{
		Question question=getQuestion();
		if (question.getPassFeedbackResource()!=null && question.getPassFeedbackResource().getId()!=-1L)
		{
			if (operation==null)
			{
				// End current user session Hibernate operation
				userSessionService.endCurrentUserOperation();
				
				// Get current user session Hibernate operation
				operation=getCurrentUserOperation(null);
			}
			if (!updateResourceImage(operation,question.getPassFeedbackResource()))
			{
				question.setPassFeedbackResource(null);
				question.setPassFeedbackResourceWidth(-1);
				question.setPassFeedbackResourceHeight(-1);
			}
		}
		return operation;
	}
	
	private Operation updateFinalFeedbackResourceImage(Operation operation)
	{
		Question question=getQuestion();
		if (question.getFinalFeedbackResource()!=null && question.getFinalFeedbackResource().getId()!=-1L)
		{
			if (operation==null)
			{
				// End current user session Hibernate operation
				userSessionService.endCurrentUserOperation();
				
				// Get current user session Hibernate operation
				operation=getCurrentUserOperation(null);
			}
			if (!updateResourceImage(operation,question.getFinalFeedbackResource()))
			{
				question.setFinalFeedbackResource(null);
				question.setFinalFeedbackResourceWidth(-1);
				question.setFinalFeedbackResourceHeight(-1);
			}
		}
		return operation;
	}
	
	private void updateCurrentFeedbackResourceImage()
	{
		updateCurrentFeedbackResourceImage(null);
	}
	
	private Operation updateCurrentFeedbackResourceImage(Operation operation)
	{
		FeedbackBean currentFeedback=getCurrentFeedback();
		if (currentFeedback!=null && currentFeedback.getResource()!=null && 
			currentFeedback.getResource().getId()!=-1L)
		{
			if (operation==null)
			{
				// End current user session Hibernate operation
				userSessionService.endCurrentUserOperation();
				
				// Get current user session Hibernate operation
				operation=getCurrentUserOperation(null);
			}
			if (!updateResourceImage(operation,currentFeedback.getResource()))
			{
				currentFeedback.setResource(null);
				currentFeedback.setResourceWidth(-1);
				currentFeedback.setResourceHeight(-1);
			}
		}
		return operation;
	}
	
	private boolean checkResourceImage(Operation operation,Resource resource)
	{
		return resourcesService.checkResourceId(getCurrentUserOperation(operation),resource.getId());
	}
	
	private boolean updateResourceImage(Operation operation,Resource resource)
	{
		boolean ok=true;
		if (resource!=null && resource.getId()!=-1L)
		{
			// Get current user session Hibernate operation
			operation=getCurrentUserOperation(operation);
			
			if (checkResourceImage(operation,resource))
			{
				Resource resourceFromDB=resourcesService.getResource(operation,resource.getId());
				if (resourceFromDB==null)
				{
					ok=false;
				}
				else
				{
					resource.setFromOtherResource(resourceFromDB.getResourceCopy());
				}
			}
			else
			{
				ok=false;
			}
		}
		return ok;
	}
	
	/**
	 * Action listener to show the dialog for selecting an image.<br/><br/>
	 * Note that can be called from several places depending on if we want to select an image for displaying 
	 * below question text, for an answer or for a feedback. 
	 * @param event Action event
	 */
	public void showSelectImage(ActionEvent event)
	{
		boolean ok=true;
		
		// Reset last resource
		lastResource=null;
		
		// Get current user session Hibernate operation
		Operation operation=getCurrentUserOperation(null);
		
		buttonId=event.getComponent().getId();
		if ("selectNewImageButton".equals(buttonId) || "selectImageButton".equals(buttonId))
		{
			// Get question
			Question question=getQuestion();
			
			// We need to process some input fields
			processCommonDataInputFields(event.getComponent());
			
			// Initialize some fields of question bean needed for image dialog
			currentResource=question.getResource();
			if (getCurrentResource()!=null)
			{
				setCurrentResourceWidth(question.getResourceWidth());
				setCurrentResourceHeight(question.getResourceHeight());
			}
		}
		else if ("selectNewImageAnswerButton".equals(buttonId) || "selectImageAnswerButton".equals(buttonId))
		{
			// Get question
			Question question=getQuestion();
			
			if (question instanceof MultichoiceQuestion)
			{
				// Get current answer
				currentAnswer=getActiveAnswer(event.getComponent());
				
				// We need to process some input fields
				processAnswersInputFields(event.getComponent(),currentAnswer);
				
				// Check that current answer name entered by user is valid
				if (checkAnswerName(currentAnswer.getName()))
				{
					activeAnswerName=currentAnswer.getName();
				}
				else
				{
					// Restore old answer name
					currentAnswer.setName(activeAnswerName);
					
					ok=false;
				}
			}
			else if (question instanceof DragDropQuestion)
			{
				// Get current draggable item
				Answer currentDraggableItem=getActiveDraggableItem(event.getComponent());
				
				// Get current answer
				currentAnswer=getActiveDroppableAnswer(event.getComponent());
				
				// We need to process some input fields
				processAnswersInputFields(event.getComponent(),currentDraggableItem,currentAnswer);
				
				// Check that current draggable item and answer names entered by user are valid
				if (!checkDraggableItemName(currentDraggableItem.getName()))
				{
					ok=false;
				}
				if (!checkAnswerName(currentAnswer.getName()))
				{
					ok=false;
				}
				if (ok)
				{
					activeDraggableItemName=currentDraggableItem.getName();
					activeAnswerName=currentAnswer.getName();
				}
				else
				{
					// Restore old draggable item name
					currentDraggableItem.setName(activeDraggableItemName);
					
					// Restore old answer name
					currentAnswer.setName(activeAnswerName);
				}
			}
			if (ok && currentAnswer!=null)
			{
				currentResource=currentAnswer.getResource();
				if (getCurrentResource()!=null)
				{
					setCurrentResourceWidth(currentAnswer.getResourceWidth());
					setCurrentResourceHeight(currentAnswer.getResourceHeight());
				}
			}
		}
		else if ("selectNewImageDraggableItemButton".equals(buttonId) || 
			"selectImageDraggableItemButton".equals(buttonId))
		{
			if (getQuestion() instanceof DragDropQuestion)
			{
				// Get current draggable item
				currentDraggableItem=getActiveDraggableItem(event.getComponent());
				
				// Get current answer
				Answer currentAnswer=getActiveDroppableAnswer(event.getComponent());
				
				// We need to process some input fields
				processAnswersInputFields(event.getComponent(),currentDraggableItem,currentAnswer);
				
				// Check that current draggable item and answer names entered by user are valid
				if (!checkDraggableItemName(currentDraggableItem.getName()))
				{
					ok=false;
				}
				if (!checkAnswerName(currentAnswer.getName()))
				{
					ok=false;
				}
				if (ok)
				{
					activeDraggableItemName=currentDraggableItem.getName();
					activeAnswerName=currentAnswer.getName();
				}
				else
				{
					// Restore old draggable item name
					currentDraggableItem.setName(activeDraggableItemName);
					
					// Restore old answer name
					currentAnswer.setName(activeAnswerName);
				}
			}
			if (ok && currentDraggableItem!=null)
			{
				currentResource=currentDraggableItem.getResource();
				if (getCurrentResource()!=null)
				{
					setCurrentResourceWidth(currentDraggableItem.getResourceWidth());
					setCurrentResourceHeight(currentDraggableItem.getResourceHeight());
				}
			}
		}
		else if ("selectNewImageQuestionResourceButton".equals(buttonId) ||
			"selectImageQuestionResourceButton".equals(buttonId))
		{
			// Get current resource of question
			currentQuestionResource=getActiveQuestionResource(event.getComponent());
			
			// We need to process some input fields
			processResourcesInputFields(operation,event.getComponent(),currentQuestionResource);
			
			// Check that current resource name entered by user is valid
			if (checkResourceName(
				getQuestion(),currentQuestionResource.getName(),currentQuestionResource.getPosition()))
			{
				activeQuestionResourceName=currentQuestionResource.getName();
			}
			else
			{
				// Restore old resource name
				currentQuestionResource.setName(activeQuestionResourceName);
				
				ok=false;
			}
			
			if (ok && currentQuestionResource!=null)
			{
				currentResource=currentQuestionResource.getResource();
				if (getCurrentResource()!=null)
				{
					setCurrentResourceWidth(currentQuestionResource.getWidth());
					setCurrentResourceHeight(currentQuestionResource.getHeight());
				}
			}
		}
		else if (getCurrentFeedback()!=null && ("selectNewImageFeedbackButton".equals(buttonId) || 
			"selectImageFeedbackButton".equals(buttonId)))
		{
			currentResource=getCurrentFeedback().getResource();
			if (getCurrentResource()!=null)
			{
				setCurrentResourceWidth(getCurrentFeedback().getResourceWidth());
				setCurrentResourceHeight(getCurrentFeedback().getResourceHeight());
			}
		}
		else if ("selectNewCorrectFeedbackImageButton".equals(buttonId) || 
			"selectCorrectFeedbackImageButton".equals(buttonId))
		{
			// We need to process some input fields
			processFeedbackInputFields(event.getComponent());
			
			// Initialize some fields of question bean needed for image dialog
			Question question=getQuestion();
			currentResource=question.getCorrectFeedbackResource();
			if (getCurrentResource()!=null)
			{
				setCurrentResourceWidth(question.getCorrectFeedbackResourceWidth());
				setCurrentResourceHeight(question.getCorrectFeedbackResourceHeight());
			}
		}
		else if ("selectNewIncorrectFeedbackImageButton".equals(buttonId) || 
			"selectIncorrectFeedbackImageButton".equals(buttonId))
		{
			// We need to process some input fields
			processFeedbackInputFields(event.getComponent());
			
			// Initialize some fields of question bean needed for image dialog
			Question question=getQuestion();
			currentResource=question.getIncorrectFeedbackResource();
			if (getCurrentResource()!=null)
			{
				setCurrentResourceWidth(question.getIncorrectFeedbackResourceWidth());
				setCurrentResourceHeight(question.getIncorrectFeedbackResourceHeight());
			}
		}
		else if ("selectNewPassFeedbackImageButton".equals(buttonId) || 
			"selectPassFeedbackImageButton".equals(buttonId))
		{
			// We need to process some input fields
			processFeedbackInputFields(event.getComponent());
			
			// Initialize some fields of question bean needed for image dialog
			Question question=getQuestion();
			currentResource=question.getPassFeedbackResource();
			if (getCurrentResource()!=null)
			{
				setCurrentResourceWidth(question.getPassFeedbackResourceWidth());
				setCurrentResourceHeight(question.getPassFeedbackResourceHeight());
			}
		}
		else if ("selectNewFinalFeedbackImageButton".equals(buttonId) || 
			"selectFinalFeedbackImageButton".equals(buttonId))
		{
			// We need to process some input fields
			processFeedbackInputFields(event.getComponent());
			
			// Initialize some fields of question bean needed for image dialog
			Question question=getQuestion();
			currentResource=question.getFinalFeedbackResource();
			if (getCurrentResource()!=null)
			{
				setCurrentResourceWidth(question.getFinalFeedbackResourceWidth());
				setCurrentResourceHeight(question.getFinalFeedbackResourceHeight());
			}
		}
		
		setFilterGlobalResourcesEnabled(null);
		setFilterOtherUsersResourcesEnabled(null);
		setUseGlobalResources(null);
		setUseOtherUsersResources(null);
		setViewResourcesFromOtherUsersPrivateCategoriesEnabled(null);
		setViewResourcesFromAdminsPrivateCategoriesEnabled(null);
		setViewResourcesFromSuperadminsPrivateCategoriesEnabled(null);
		resetAdmins();
		resetSuperadmins();
		
		Category filterCategory=null;
		long filterCategoryId=getFilterCategoryId(operation);
		if (filterCategoryId>0L)
		{
			if (categoriesService.checkCategoryId(operation,filterCategoryId))
			{
				filterCategory=categoriesService.getCategory(operation,filterCategoryId);
			}
		}
		else
		{
			filterCategory=new Category();
			filterCategory.setId(filterCategoryId);
		}
		if (filterCategory==null || !checkImagesFilterPermission(operation,filterCategory))
		{
			setFilterCategoryId(Long.MIN_VALUE);
		}
		
		// Reload images and images categories
		images=null;
		specialCategoriesFilters=null;
		setImagesCategories(null);
		
		getImages();
		
		// Get current user session Hibernate operation
		operation=getCurrentUserOperation(null);
		
		getImagesCategories(operation);
		getFilterCategoryId(operation);
		
		if (ok)
		{
			if (getCurrentResource()!=null && getCurrentResource().getId()!=-1L)
			{
				// End current user session Hibernate operation
				userSessionService.endCurrentUserOperation();
				
				if (!checkResourceImage(getCurrentUserOperation(null),getCurrentResource()))
				{
					currentResource=null;
				}
			}
			if (getCurrentResource()==null)
			{
				setCurrentResourceAspectRatio(0.0);
				setCurrentResourceKeepAspectRatio(false);
				setCurrentResourceWidth(1);
				setCurrentResourceHeight(1);
			}
			else
			{
				int[] currentResourceDimensions=getCurrentResourceImageDimensionsArray();
				if (currentResourceDimensions[0]<=0 || currentResourceDimensions[1]<=0)
				{
					setCurrentResourceAspectRatio(0.0);
					setCurrentResourceKeepAspectRatio(false);
					setCurrentResourceWidth(1);
					setCurrentResourceHeight(1);
				}
				else
				{
					setCurrentResourceAspectRatio(
						((double)currentResourceDimensions[0])/((double)currentResourceDimensions[1]));
					if (getCurrentResourceWidth()<=0 || getCurrentResourceHeight()<=0)
					{
						if (currentResourceDimensions[0]<=DEFAULT_MAX_RESOURCE_WIDTH)
						{
							setCurrentResourceWidth(currentResourceDimensions[0]);
							setCurrentResourceHeight(currentResourceDimensions[1]);
						}
						else
						{
							setCurrentResourceWidth(DEFAULT_MAX_RESOURCE_WIDTH);
							setCurrentResourceHeight(currentResourceDimensions[1]*DEFAULT_MAX_RESOURCE_WIDTH/
								currentResourceDimensions[0]);
						}
						setCurrentResourceKeepAspectRatio(true);
					}
					else
					{
						double selectedAspectRatio=
							((double)getCurrentResourceWidth())/((double)getCurrentResourceHeight());
						setCurrentResourceKeepAspectRatio(
							getCurrentResourceAspectRatio()+TOLERANCE>=selectedAspectRatio && 
							getCurrentResourceAspectRatio()-TOLERANCE<=selectedAspectRatio);
					}
				}
			}
			RequestContext rq=RequestContext.getCurrentInstance();
			rq.execute("selectImageDialog.show()");
		}
		else
		{
			// Scroll page to top position
			scrollToTop();
		}
	}
	
	/**
	 * @param operation Operation
	 * @param filterCategory Filter category can be optionally passed as argument
	 * @return true if user has permissions to display resources with the current selected filter, false otherwise
	 */
    private boolean checkImagesFilterPermission(Operation operation,Category filterCategory)
	{
    	boolean ok=true;
    	
    	// Get current user session Hibernate operation
    	operation=getCurrentUserOperation(operation);
    	
		Question question=getQuestion();
    	long filterCategoryId=filterCategory==null?getFilterCategoryId(operation):filterCategory.getId();
		if (specialCategoryFiltersMap.containsKey(Long.valueOf(filterCategoryId)))
		{
			// Check permissions needed for selected special category filter
			SpecialCategoryFilter filter=specialCategoryFiltersMap.get(Long.valueOf(filterCategoryId));
			for (String requiredPermission:filter.requiredPermissions)
			{
				if (userSessionService.isDenied(operation,requiredPermission))
				{
					ok=false;
					break;
				}
			}
			if (ok)
			{
				User questionAuthor=
					question.getId()>0L?question.getCreatedBy():userSessionService.getCurrentUser(operation);
				for (String requiredAuthorPermission:filter.requiredAuthorPermissions)
				{
					if (permissionsService.isDenied(operation,questionAuthor,requiredAuthorPermission))
					{
						ok=false;
						break;
					}
				}
			}
		}
		else
		{
			if (filterCategory==null)
			{
				// If we have not received filter category as argument we need to get it from DB
				filterCategory=categoriesService.getCategory(operation,filterCategoryId);	
			}
			
			Visibility filterCategoryVisibility=
				visibilitiesService.getVisibilityFromCategoryId(operation,filterCategoryId);
			if (filterCategoryVisibility.isGlobal())
			{
				// This is a global category, so we check that current user has permission to filter
				// resources by global categories and that the question's author has permission to assign
				// resources of global categories to his/her questions
				ok=getFilterGlobalResourcesEnabled(operation).booleanValue() && 
					getUseGlobalResources(operation).booleanValue();
			}
			else
			{
				// First we have to see if the category is owned by current user, 
				// if that is not the case we will need to perform aditional checks
				long currentUserId=userSessionService.getCurrentUserId();
				User questionAuthor=
					question.getId()>0L?question.getCreatedBy():userSessionService.getCurrentUser(operation);
				User categoryUser=filterCategory.getUser();
				if (categoryUser.getId()!=currentUserId)
				{
					// We need to check that current user has permission to filter resources by categories 
					// of other users and that the category is owned question's author or he/she has
					// permission to assign resources from categories of other users to his/her questions
					if (getFilterOtherUsersResourcesEnabled(operation).booleanValue() && 
						(questionAuthor.equals(categoryUser) || 
						getUseOtherUsersResources(operation).booleanValue()))
					{
						// We have to see if this a public or a private category
						// Public categories doesn't need more checks
						// But private categories need aditional permissions
						Visibility privateVisibility=
							visibilitiesService.getVisibility(operation,"CATEGORY_VISIBILITY_PRIVATE");
						if (filterCategoryVisibility.getLevel()>=privateVisibility.getLevel())
						{
							// Finally we need to check that current user has permission to view resources 
							// from private categories of other users, and aditionally we need to check 
							// that current user has permission to view resources from private categories 
							// of administrators if the owner of the category is an administrator and 
							// to check that current user has permission to view resources from private categories 
							// of users with permission to improve permissions over its owned ones if the owner 
							// of the category has that permission (superadmin)
							boolean isAdmin=isAdmin(operation,categoryUser);
							boolean isSuperadmin=isSuperadmin(operation,categoryUser);
							ok=getViewResourcesFromOtherUsersPrivateCategoriesEnabled(operation).booleanValue() && 
								(!isAdmin || 
								getViewResourcesFromAdminsPrivateCategoriesEnabled(operation).booleanValue()) && 
								(!isSuperadmin || 
								getViewResourcesFromSuperadminsPrivateCategoriesEnabled(operation).booleanValue());
						}
					}
					else
					{
						ok=false;
					}
				}
				else
				{
					// We already know that category is owned by current user but we also need to check that 
					// question's author is also the current user or he/she has permission to assign resources 
					// of global categories to his/her tests
					ok=questionAuthor.getId()==currentUserId || 
						getUseOtherUsersResources(operation).booleanValue();
				}
			}
		}
		return ok;
	}
	
    /**
	 * Change images to display on combo based on filter.
     * @param event Action event
     */
    public void applyImagesFilter(ActionEvent event)
    {
    	boolean filterCategoryNotFound=false;
    	boolean filterCategoryInvalid=false;
    	
		// Get current user session Hibernate operation
		Operation operation=getCurrentUserOperation(null);
    	
   		setFilterGlobalResourcesEnabled(null);
   		setFilterOtherUsersResourcesEnabled(null);
   		setUseGlobalResources(null);
   		setUseOtherUsersResources(null);
       	setViewResourcesFromOtherUsersPrivateCategoriesEnabled(null);
       	setViewResourcesFromAdminsPrivateCategoriesEnabled(null);
       	setViewResourcesFromSuperadminsPrivateCategoriesEnabled(null);
       	Category filterCategory=null;
       	long filterCategoryId=getFilterCategoryId(operation);
       	if (filterCategoryId>0L)
       	{
       		if (categoriesService.checkCategoryId(operation,filterCategoryId))
       		{
           		filterCategory=categoriesService.getCategory(operation,filterCategoryId);
           		resetAdminFromCategoryAllowed(filterCategory);
           		resetSuperadminFromCategoryAllowed(filterCategory);
       		}
       		else
       		{
       			filterCategoryNotFound=true;
       		}
       	}
       	else
       	{
       		filterCategory=new Category();
       		filterCategory.setId(filterCategoryId);
       	}
       	if (!filterCategoryNotFound)
       	{
       		Resource resource=getCurrentResource();
       		Category resourceCategory=
       			resource==null?null:categoriesService.getCategoryFromResourceId(operation,resource.getId());
       		filterCategoryInvalid=
       			!filterCategory.equals(resourceCategory) && !checkImagesFilterPermission(operation,filterCategory);
       	}
       	
       	if (filterCategoryNotFound)
       	{
       		addErrorMessage("IMAGES_FILTER_CATEGORY_NOT_FOUND_ERROR");
       		setFilterCategoryId(Long.MIN_VALUE);
       	}
       	else if (filterCategoryInvalid)
       	{
       		addErrorMessage("NON_AUTHORIZED_ACTION_ERROR");
       		setFilterCategoryId(Long.MIN_VALUE);
       	}
       	
   		// Reload images from DB
       	images=null;
       	
       	setAddEnabled(null);
       	setEditEnabled(null);
       	setEditOtherUsersQuestionsEnabled(null);
       	setEditAdminsQuestionsEnabled(null);
       	setEditSuperadminsQuestionsEnabled(null);
   		setFilterGlobalQuestionsEnabled(null);
   		setFilterOtherUsersQuestionsEnabled(null);
   		setGlobalOtherUserCategoryAllowed(null);
   		setViewQuestionsFromOtherUsersPrivateCategoriesEnabled(null);
   		setViewQuestionsFromAdminsPrivateCategoriesEnabled(null);
   		setViewQuestionsFromSuperadminsPrivateCategoriesEnabled(null);
       	resetAdmins();
       	resetSuperadmins();
       	
   		// Always reload images categories from DB
       	specialCategoriesFilters=null;
   		setImagesCategories(null);
  		
   		getImages();
   			
        // Get current user session Hibernate operation
   		operation=getCurrentUserOperation(null);
   		
   		getImagesCategories(operation);
   		getFilterCategoryId(operation);
    }
	
	/**
	 * Action listener for checking if selected image has changed and in that case hide last image and display 
	 * the new one.
	 * @param event Ajax event
	 */
	public void changeImage(AjaxBehaviorEvent event)
	{
		if (currentResource==null) 
		{
			currentResource=getNoImage();
		}
		if (!currentResource.equals(lastResource))
		{
			if (currentResource.getId()==-1L)
			{
				setCurrentResourceAspectRatio(0.0);
				setCurrentResourceKeepAspectRatio(false);
				setCurrentResourceWidth(1);
				setCurrentResourceHeight(1);
			}
			else
			{
				int[] currentResourceDimensions=getCurrentResourceImageDimensionsArray();
				if (currentResourceDimensions[0]<=0 || currentResourceDimensions[1]<=0)
				{
					setCurrentResourceAspectRatio(0.0);
					setCurrentResourceKeepAspectRatio(false);
					setCurrentResourceWidth(1);
					setCurrentResourceHeight(1);
				}
				else
				{
					setCurrentResourceAspectRatio(
						((double)currentResourceDimensions[0])/((double)currentResourceDimensions[1]));
					if (currentResourceDimensions[0]<=DEFAULT_MAX_RESOURCE_WIDTH)
					{
						setCurrentResourceWidth(currentResourceDimensions[0]);
						setCurrentResourceHeight(currentResourceDimensions[1]);
					}
					else
					{
						setCurrentResourceWidth(DEFAULT_MAX_RESOURCE_WIDTH);
						setCurrentResourceHeight(
							currentResourceDimensions[1]*DEFAULT_MAX_RESOURCE_WIDTH/currentResourceDimensions[0]);
					}
					setCurrentResourceKeepAspectRatio(true);
				}
			}
			lastResource=currentResource;
		}
	}
	
	/**
	 * Action listener for checking/unchecking the check box for keeping aspect ratio of image dimensions in 
	 * the image dialog.
	 * @param event Ajax event
	 */
	public void changeKeepAspectRatio(AjaxBehaviorEvent event)
	{
		setCurrentResourceKeepAspectRatio(
			((Boolean)((SelectBooleanCheckbox)event.getComponent()).getValue()).booleanValue());
		if (isCurrentResourceKeepAspectRatio() && getCurrentResourceAspectRatio()!=0.0)
		{
			double selectedAspectRatio=((double)getCurrentResourceWidth())/((double)getCurrentResourceHeight());
			boolean aspectRatioOk=getCurrentResourceAspectRatio()+TOLERANCE>=selectedAspectRatio && 
				getCurrentResourceAspectRatio()-TOLERANCE<=selectedAspectRatio;
			if (!aspectRatioOk)
			{
				int newWidth=(int)((double)getCurrentResourceHeight()*getCurrentResourceAspectRatio());
				int newHeight=(int)((double)getCurrentResourceWidth()/getCurrentResourceAspectRatio());
				if (Math.abs(getCurrentResourceWidth()-newWidth)<Math.abs(getCurrentResourceHeight()-newHeight))
				{
					setCurrentResourceWidth(newWidth);
				}
				else
				{
					setCurrentResourceHeight(newHeight);
				}
			}
		}
	}
	
	/**
	 * Action listener for changing image width in the image dialog.
	 * @param event Ajax event
	 */
	public void changeImageWidth(AjaxBehaviorEvent event)
	{
		setCurrentResourceWidth(((Integer)((Spinner)event.getComponent()).getValue()).intValue());
		if (isCurrentResourceKeepAspectRatio() && getCurrentResourceAspectRatio()!=0.0)
		{
			double selectedAspectRatio=((double)getCurrentResourceWidth())/((double)getCurrentResourceHeight());
			boolean aspectRatioOk=getCurrentResourceAspectRatio()+TOLERANCE>=selectedAspectRatio && 
				getCurrentResourceAspectRatio()-TOLERANCE<=selectedAspectRatio;
			if (!aspectRatioOk)
			{
				setCurrentResourceHeight((int)((double)getCurrentResourceWidth()/getCurrentResourceAspectRatio()));
			}
		}
	}
	
	/**
	 * Action listener for changing image height in the image dialog.
	 * @param event Ajax event
	 */
	public void changeImageHeight(AjaxBehaviorEvent event)
	{
		setCurrentResourceHeight(((Integer)((Spinner)event.getComponent()).getValue()).intValue());
		if (isCurrentResourceKeepAspectRatio() && getCurrentResourceAspectRatio()!=0.0)
		{
			double selectedAspectRatio=((double)getCurrentResourceWidth())/((double)getCurrentResourceHeight());
			boolean aspectRatioOk=getCurrentResourceAspectRatio()+TOLERANCE>=selectedAspectRatio && 
				getCurrentResourceAspectRatio()-TOLERANCE<=selectedAspectRatio;
			if (!aspectRatioOk)
			{
				setCurrentResourceWidth((int)((double)getCurrentResourceHeight()*getCurrentResourceAspectRatio()));
			}
		}
	}
	
	/**
	 * Action listener for updating related resource if we accept the changes within the dialog for selecting 
	 * an image. 
	 * @param event Action event
	 */
	public void acceptSelectImage(ActionEvent event)
	{
		Operation operation=null;
		Resource newResource=null;
		int newWidth=-1;
		int newHeight=-1;
		if (getCurrentResource()!=null && getCurrentResource().getId()!=-1L)
		{
			// End current user session Hibernate operation
			userSessionService.endCurrentUserOperation();
			
			// Get current user session Hibernate operation
			operation=getCurrentUserOperation(null);
			
			if (checkResourceImage(operation,getCurrentResource()))
			{
				newResource=getCurrentResource();
				newWidth=getCurrentResourceWidth();
				newHeight=getCurrentResourceHeight();
			}
		}
		if ("selectNewImageButton".equals(buttonId) || "selectImageButton".equals(buttonId))
		{
			Question question=getQuestion();
			question.setResource(newResource);
			question.setResourceWidth(newWidth);
			question.setResourceHeight(newHeight);
		}
		else if (currentAnswer!=null && ("selectNewImageAnswerButton".equals(buttonId) || 
			"selectImageAnswerButton".equals(buttonId)))
		{
			currentAnswer.setResource(newResource);
			currentAnswer.setResourceWidth(newWidth);
			currentAnswer.setResourceHeight(newHeight);
		}
		else if (currentDraggableItem!=null && ("selectNewImageDraggableItemButton".equals(buttonId) || 
			"selectImageDraggableItemButton".equals(buttonId)))
		{
			currentDraggableItem.setResource(newResource);
			currentDraggableItem.setResourceWidth(newWidth);
			currentDraggableItem.setResourceHeight(newHeight);
		}
		else if (currentQuestionResource!=null && ("selectNewImageQuestionResourceButton".equals(buttonId) || 
			"selectImageQuestionResourceButton".equals(buttonId)))
		{
			if (newResource==null)
			{
				newResource=getNoResource();
			}
			currentQuestionResource.setResource(newResource);
			currentQuestionResource.setWidth(newWidth);
			currentQuestionResource.setHeight(newHeight);
		}
		else if ("selectNewImageFeedbackButton".equals(buttonId) || "selectImageFeedbackButton".equals(buttonId))
		{
			FeedbackBean currentFeedback=getCurrentFeedback();
			if (currentFeedback!=null)
			{
				currentFeedback.setResource(newResource);
				currentFeedback.setResourceWidth(newWidth);
				currentFeedback.setResourceHeight(newHeight);
				if (newResource!=null)
				{
					operation=updateCurrentFeedbackResourceImage(operation);			
				}
			}
		}
		else if ("selectNewCorrectFeedbackImageButton".equals(buttonId) || 
			"selectCorrectFeedbackImageButton".equals(buttonId))
		{
			Question question=getQuestion();
			question.setCorrectFeedbackResource(newResource);
			question.setCorrectFeedbackResourceWidth(newWidth);
			question.setCorrectFeedbackResourceHeight(newHeight);
		}
		else if ("selectNewIncorrectFeedbackImageButton".equals(buttonId) || 
			"selectIncorrectFeedbackImageButton".equals(buttonId))
		{
			Question question=getQuestion();
			question.setIncorrectFeedbackResource(newResource);
			question.setIncorrectFeedbackResourceWidth(newWidth);
			question.setIncorrectFeedbackResourceHeight(newHeight);
		}
		else if ("selectNewPassFeedbackImageButton".equals(buttonId) || 
			"selectPassFeedbackImageButton".equals(buttonId))
		{
			Question question=getQuestion();
			question.setPassFeedbackResource(newResource);
			question.setPassFeedbackResourceWidth(newWidth);
			question.setPassFeedbackResourceHeight(newHeight);
		}
		else if ("selectNewFinalFeedbackImageButton".equals(buttonId) || 
			"selectFinalFeedbackImageButton".equals(buttonId))
		{
			Question question=getQuestion();
			question.setFinalFeedbackResource(newResource);
			question.setFinalFeedbackResourceWidth(newWidth);
			question.setFinalFeedbackResourceHeight(newHeight);
		}
		updateResourcesImages(operation);
	}
	
	/**
	 * Action listener for updating resources information if we cancel the changes within the dialog for 
	 * selecting an image. 
	 * @param event Action event
	 */
	public void cancelSelectImage(ActionEvent event)
	{
		updateResourcesImages();
	}
	
	/**
	 * Ajax listener.<br/><br/>
	 * I have defined a tab change listener for answers accordion and that accordion is inside the
	 * question's tabview.<br/><br/>
	 * Curiosly when I change the question's tab, Primefaces fires the listener defined for the accordion but 
	 * calls it with an AjaxBehaviourEvent argument.<br/><br/>
	 * As this listener is called unintentionally we have defined it only to avoid an error message and 
	 * it does nothing.
	 * @param event
	 */
	public void changeActiveAnswer(AjaxBehaviorEvent event)
	{
	}
	
	/**
	 * Tab change listener for displaying other answer within answers accordion.
	 * @param event Tab change event
	 */
	public void changeActiveAnswer(TabChangeEvent event)
	{
		// Get question
		Question question=getQuestion();
		
		if (question instanceof MultichoiceQuestion)
		{
			// Get current answer
			Answer currentAnswer=getActiveAnswer(event.getComponent());
			
			// Check that current answer name entered by user is valid
			if (checkAnswerName(currentAnswer.getName()))
			{
				try
				{
					activeAnswerIndex=Integer.parseInt(((AccordionPanel)event.getComponent()).getActiveIndex());
					activeAnswerName=getActiveAnswer(event.getComponent()).getName();
					if (activeAnswerName==null)
					{
						activeAnswerName="";
					}
				}
				catch (NumberFormatException nfe)
				{
					activeAnswerIndex=-1;
					activeAnswerName="";
				}
			}
			else
			{
				// Restore old answer tab without changing its name
				updateAnswersTextFields(event.getComponent(),question.getAnswers().size());
				currentAnswer.setName(activeAnswerName);
				refreshActiveAnswer(event.getComponent());
				
				// Scroll page to top position
				scrollToTop();
			}
			
			updateAnswerResourceImage(getActiveAnswer(event.getComponent()));
		}
		else if (question instanceof DragDropQuestion)
		{
			DragDropQuestion dragDropQuestion=(DragDropQuestion)question;
			
			// Get current draggable item
			Answer currentDraggableItem=getActiveDraggableItem(event.getComponent());
			
			// Get current answer
			Answer currentAnswer=getActiveDroppableAnswer(event.getComponent());
			
			// We need to process some input fields
			processAnswersInputFields(event.getComponent(),currentDraggableItem,currentAnswer,false);
			
			// Check that current draggable item and answer names entered by user are valid
			boolean ok=true;
			if (!checkDraggableItemName(currentDraggableItem.getName()))
			{
				ok=false;
			}
			if (!checkAnswerName(currentAnswer.getName()))
			{
				ok=false;
			}
			if (ok)
			{
				try
				{
					activeAnswerIndex=Integer.parseInt(((AccordionPanel)event.getComponent()).getActiveIndex());
					activeAnswerName=getActiveDroppableAnswer(event.getComponent()).getName();
					if (activeAnswerName==null)
					{
						activeAnswerName="";
					}
				}
				catch (NumberFormatException nfe)
				{
					activeAnswerIndex=-1;
					activeAnswerName="";
				}
			}
			else
			{
				//TODO de momento solo tenemos en cuenta el grupo 1, habra que cambiarlo mas adelante
				updateDraggableItemsTextFields(event.getComponent(),dragDropQuestion.getDraggableItems(1).size());
				currentDraggableItem.setName(activeDraggableItemName);
				
				// Restore old answer tab without changing its name
				//TODO de momento solo tenemos en cuenta el grupo 1, habra que cambiarlo mas adelante
				updateAnswersTextFields(event.getComponent(),dragDropQuestion.getDroppableAnswers(1).size());
				currentAnswer.setName(activeAnswerName);
				refreshActiveAnswer(event.getComponent());
				
				// Scroll page to top position
				scrollToTop();
			}
			
			Operation operation=updateAnswerResourceImage(currentDraggableItem);
			updateAnswerResourceImage(operation,getActiveDroppableAnswer(event.getComponent()));
		}
	}
	
	/**
	 * Ajax listener.<br/><br/>
	 * I have defined a tab change listener for draggable items accordion and that accordion is inside the
	 * question's tabview.<br/><br/>
	 * Curiosly when I change the question's tab, Primefaces fires the listener defined for the accordion but 
	 * calls it with an AjaxBehaviourEvent argument.<br/><br/>
	 * As this listener is called unintentionally we have defined it only to avoid an error message and 
	 * it does nothing.
	 * @param event
	 */
	public void changeActiveDraggableItem(AjaxBehaviorEvent event)
	{
	}
	
	/**
	 * Tab change listener for displaying other draggable item within draggable items accordion.
	 * @param event Tab change event
	 */
	public void changeActiveDraggableItem(TabChangeEvent event)
	{
		// Get current draggable item
		Answer currentDraggableItem=getActiveDraggableItem(event.getComponent());
		
		// Get current answer
		Answer currentAnswer=getActiveDroppableAnswer(event.getComponent());
		
		// We need to process some input fields
		processAnswersInputFields(event.getComponent(),currentDraggableItem,currentAnswer,false);
		
		// Check that current draggable item and answer names entered by user are valid
		boolean ok=true;
		if (!checkDraggableItemName(currentDraggableItem.getName()))
		{
			ok=false;
		}
		if (!checkAnswerName(currentAnswer.getName()))
		{
			ok=false;
		}
		if (ok)
		{
			try
			{
				activeDraggableItemIndex=Integer.parseInt(((AccordionPanel)event.getComponent()).getActiveIndex());
				activeDraggableItemName=getActiveDraggableItem(event.getComponent()).getName();
				if (activeDraggableItemName==null)
				{
					activeDraggableItemName="";
				}
			}
			catch (NumberFormatException nfe)
			{
				activeDraggableItemIndex=-1;
				activeDraggableItemName="";
			}
		}
		else
		{
			// Get question
			DragDropQuestion question=(DragDropQuestion)getQuestion();
			
			// Restore old draggable item tab without changing its name
			//TODO de momento solo tenemos en cuenta el grupo 1, habra que cambiarlo mas adelante
			updateDraggableItemsTextFields(event.getComponent(),question.getDraggableItems(1).size());
			currentDraggableItem.setName(activeDraggableItemName);
			refreshActiveDraggableItem(event.getComponent());
			
			//TODO de momento solo tenemos en cuenta el grupo 1, habra que cambiarlo mas adelante
			updateAnswersTextFields(event.getComponent(),question.getDroppableAnswers(1).size());
			currentAnswer.setName(activeAnswerName);
			
			// Scroll page to top position
			scrollToTop();
		}
		
		Operation operation=updateAnswerResourceImage(getActiveDraggableItem(event.getComponent()));
		updateAnswerResourceImage(operation,currentAnswer);
	}
	
	/**
	 * Ajax listener.<br/><br/>
	 * I have defined a tab change listener for resources of question accordion and that accordion is inside the
	 * question's tabview.<br/><br/>
	 * Curiosly when I change the question's tab, Primefaces fires the listener defined for the accordion but 
	 * calls it with an AjaxBehaviourEvent argument.<br/><br/>
	 * As this listener is called unintentionally we have defined it only to avoid an error message and 
	 * it does nothing.
	 * @param event
	 */
	public void changeActiveQuestionResource(AjaxBehaviorEvent event)
	{
	}
	
	/**
	 * Tab change listener for displaying other resource within resources of question accordion.
	 * @param event Tab change event
	 */
	public void changeActiveQuestionResource(TabChangeEvent event)
	{
		Operation operation=null;
		boolean removeEvenCurrentQuestionResource=false;
		
		// Get question
		Question question=getQuestion();
		
		// Get current resource
		QuestionResource currentQuestionResource=getActiveQuestionResource(event.getComponent());
		
		// We need to process some input fields
		processResourcesInputFields(getCurrentUserOperation(null),event.getComponent(),currentQuestionResource);
		
		// Check that current resource name entered by user is valid
		if (checkQuestionResource(question,currentQuestionResource))
		{
			try
			{
				int newActiveQuestionResourceIndex=
					Integer.parseInt(((AccordionPanel)event.getComponent()).getActiveIndex());
				QuestionResource newActiveQuestionResource=
					question.getQuestionResource(newActiveQuestionResourceIndex+1);
				if (newActiveQuestionResource!=null)
				{
					removeEvenCurrentQuestionResource=activeQuestionResourceIndex!=newActiveQuestionResourceIndex;
					if (newActiveQuestionResource.getResource()!=null && 
						newActiveQuestionResource.getResource().getId()!=-1L)
					{
						// End current user session Hibernate operation
						userSessionService.endCurrentUserOperation();
						
						// Get current user session Hibernate operation
						operation=getCurrentUserOperation(null);
						
						if (checkResourceImage(operation,newActiveQuestionResource.getResource()))
						{
							activeQuestionResourceIndex=newActiveQuestionResourceIndex;
							activeQuestionResourceName=newActiveQuestionResource.getName();
							if (activeQuestionResourceName==null)
							{
								activeQuestionResourceName="";
							}
						}
						else
						{
							activeQuestionResourceName=currentQuestionResource.getName();
							if (activeQuestionResourceName==null)
							{
								activeQuestionResourceName="";
							}
						}
					}
					else
					{
						activeQuestionResourceIndex=newActiveQuestionResourceIndex;
						activeQuestionResourceName=newActiveQuestionResource.getName();
						if (activeQuestionResourceName==null)
						{
							activeQuestionResourceName="";
						}
					}
				}
			}
			catch (NumberFormatException nfe)
			{
			}
		}
		else
		{
			// Restore old resource tab without changing its name
			updateResourcesTextFields(event.getComponent(),question.getQuestionResources().size());
			if (currentQuestionResource!=null)
			{
				currentQuestionResource.setName(activeQuestionResourceName);
			}
			
			// Scroll page to top position
			scrollToTop();
		}
		updateQuestionResourcesImages(operation,removeEvenCurrentQuestionResource);
		refreshActiveQuestionResource(event.getComponent());
	}
	
	public void changeDraggableItemName(AjaxBehaviorEvent event)
	{
		// For now we only need to process this event in Drag & Drop question
		if (getQuestion() instanceof DragDropQuestion)
		{
			// Get current draggable item
			Answer currentDraggableItem=getActiveDraggableItem(event.getComponent());
			
			// Get current answer
			Answer currentAnswer=getActiveDroppableAnswer(event.getComponent());
			
			// We need to process some input fields
			processAnswersInputFields(event.getComponent(),null,currentAnswer,false);
			
			boolean ok=true;
			
			// Check that current draggable item name entered by user is valid
			if (checkDraggableItemName(currentDraggableItem.getName()))
			{
				activeDraggableItemName=currentDraggableItem.getName();
			}
			else
			{
				// Restore old draggable item name
				currentDraggableItem.setName(activeDraggableItemName);
				
				ok=false;
			}
			
			// Check that current answer name entered by user is valid
			if (checkAnswerName(currentAnswer.getName()))
			{
				activeAnswerName=currentAnswer.getName();
			}
			else
			{
				// Restore old answer name
				currentAnswer.setName(activeAnswerName);
				
				ok=false;
			}
			
			if (!ok)
			{
				// Scroll page to top position
				scrollToTop();
			}
		}
	}
	
	public void changeInfinite(AjaxBehaviorEvent event)
	{
		// Get question
		Question question=getQuestion();
		
		// For now we only need to process this event in Drag & Drop question
		if (question instanceof DragDropQuestion)
		{
			// Get current answer
			Answer currentAnswer=getActiveDroppableAnswer(event.getComponent());
			
			// We need to process some input fields
			processAnswersInputFields(event.getComponent(),null,currentAnswer,false);
			
			// Check that current answer name entered by user is valid
			if (checkAnswerName(currentAnswer.getName()))
			{
				activeAnswerName=currentAnswer.getName();
				
				// Fire changeProperty event
				changeProperty(event);
			}
			else
			{
				// Restore old answer name
				currentAnswer.setName(activeAnswerName);
				
				// As there are already other errors we restore old value if new value is not valid 
				// for current answers
				if (!checkAnswersForChangeInfinite())
				{
					DragDropQuestion dragDropQuestion=(DragDropQuestion)question;
					dragDropQuestion.setInfinite(!dragDropQuestion.isInfinite());
				}
				
				// Scroll page to top position
				scrollToTop();
			}
		}
	}
	
	/**
	 * Checks if changing the property 'singleResponse' of a multichoice question will affect to the 
	 * feedbacks already defined.
	 * @return true if changing the property 'singleResponse' of a multichoice question won't affect to the 
	 * feedbacks already defined, false otherwise
	 */
	private boolean checkFeedbacksForChangeSingleResponse()
	{
		boolean ok=true;
		
		// Get question
		Question question=getQuestion();
		
		if (question instanceof MultichoiceQuestion)
		{
			for (FeedbackBean feedback:getFeedbacks())
			{
				// Check selected answers condition
				ok=checkSelectedAnswersCondition(feedback,0);
				if (!ok)
				{
					break;
				}
				
				// Check selected right answers condition
				ok=checkSelectedRightAnswersCondition(feedback,0);
				if (!ok)
				{
					break;
				}
				
				// Check selected wrong answers condition
				ok=checkSelectedWrongAnswersCondition(feedback,0);
				if (!ok)
				{
					break;
				}
				
				// Check unselected answers condition
				ok=checkUnselectedAnswersCondition(feedback,0);
				if (!ok)
				{
					break;
				}
				
				// Check unselected right answers condition
				ok=checkUnselectedRightAnswersCondition(feedback,0);
				if (!ok)
				{
					break;
				}
				
				// Check unselected wrong answers condition
				ok=checkUnselectedWrongAnswersCondition(feedback,0);
				if (!ok)
				{
					break;
				}
				
				// Check right distance condition
				ok=checkRightDistanceCondition(feedback,0,0);
				if (!ok)
				{
					break;
				}
			}
		}
		return ok;
	}

	/**
	 * Updates feebacks related to the modification of the property 'singleResponse' of a question if needed.
	 */
	private void updateFeedbacksForChangeSingleResponse()
	{
		// Get question
		Question question=getQuestion();
		
		if (question instanceof MultichoiceQuestion)
		{
			for (FeedbackBean feedback:getFeedbacks())
			{
				// Update selected answers condition
				updateSelectedAnswersCondition(feedback,0);
				
				// Update selected right answers condition
				updateSelectedRightAnswersCondition(feedback,0);
				
				// Update selected wrong answers condition
				updateSelectedWrongAnswersCondition(feedback,0);
				
				// Update unselected answers condition
				updateUnselectedAnswersCondition(feedback,0);
				
				// Update unselected right answers condition
				updateUnselectedRightAnswersCondition(feedback,0);
				
				// Update unselected wrong answers condition
				updateUnselectedWrongAnswersCondition(feedback,0);
				
				// Update right distance condition
				updateRightDistanceCondition(feedback,0,0);
				
				//Update raw feedback
				question.getFeedback(feedback.getPosition()).setFromOtherFeedback(feedback.getAsFeedback());
			}
		}
	}
	
	/**
	 * Checks if changing the property 'correct' of an answer will affect to the feedbacks already defined.
	 * @return true if changing the property 'correct' of an answer won't affect to the feedbacks 
	 * already defined, false otherwise
	 */
	private boolean checkFeedbacksForChangeCorrect()
	{
		boolean ok=true;
		
		// Get question
		Question question=getQuestion();
		
		if (question instanceof MultichoiceQuestion && !((MultichoiceQuestion)question).isSingle())
		{
			for (FeedbackBean feedback:getFeedbacks())
			{
				// Check selected right answers condition
				ok=checkSelectedRightAnswersCondition(feedback,0);
				if (!ok)
				{
					break;
				}
				
				// Check selected wrong answers condition
				ok=checkSelectedWrongAnswersCondition(feedback,0);
				if (!ok)
				{
					break;
				}
				
				// Check unselected right answers condition
				ok=checkUnselectedRightAnswersCondition(feedback,0);
				if (!ok)
				{
					break;
				}
				
				// Check unselected wrong answers condition
				ok=checkUnselectedWrongAnswersCondition(feedback,0);
				if (!ok)
				{
					break;
				}
				
				// Check right distance condition
				ok=checkRightDistanceCondition(feedback,0,0);
				if (!ok)
				{
					break;
				}
			}
		}
		return ok;
	}
	
	/**
	 * Updates feebacks related to the modification of the property 'correct' of an answer if needed.
	 */
	private void updateFeedbacksForChangeCorrect()
	{
		// Get question
		Question question=getQuestion();
		
		if (question instanceof MultichoiceQuestion && !((MultichoiceQuestion)question).isSingle())
		{
			for (FeedbackBean feedback:getFeedbacks())
			{
				// Update selected right answers condition
				updateSelectedRightAnswersCondition(feedback,0);
				
				// Update selected wrong answers condition
				updateSelectedWrongAnswersCondition(feedback,0);
				
				// Update unselected right answers condition
				updateUnselectedRightAnswersCondition(feedback,0);
				
				// Update unselected wrong answers condition
				updateUnselectedWrongAnswersCondition(feedback,0);
				
				// Update right distance condition
				updateRightDistanceCondition(feedback,0,0);
				
				//Update raw feedback
				question.getFeedback(feedback.getPosition()).setFromOtherFeedback(feedback.getAsFeedback());
			}
		}
	}
	
	/**
	 * Checks if changing the property 'infinite' of a "Drag & Drop" question will affect to the answers 
	 * already defined.
	 * @return true if changing the property 'infinite' of a "Drag & Drop" question won't affect to the 
	 * answers already defined, false otherwise
	 */
	private boolean checkAnswersForChangeInfinite()	
	{
		return checkOrUpdateAnswersForChangeInfinite(false);
	}
	
	/**
	 * Update answers of a "Drag & Drop" question after changing "infinite" property if needed.
	 */
	private void updateAnswersForChangeInfinite()
	{
		checkOrUpdateAnswersForChangeInfinite(true);
	}
	
	/**
	 * Check or update answers of a "Drag & Drop" question after changing "infinite" property.
	 * @param update true for updating, false for checking without updating
	 * @return when checking returns true if changing the property 'infinite' of a "Drag & Drop" question 
	 * will affect to the answers already defined, false otherwise; when updating always returns true
	 */
	private boolean checkOrUpdateAnswersForChangeInfinite(boolean update)
	{
		boolean ok=true;
		// Note that if "infinite" property of a "Drag & Drop" question has been set to "true" 
		// checking and/or updating is not needed
		Question question=getQuestion();
		if (question instanceof DragDropQuestion && !((DragDropQuestion)question).isInfinite())
		{
			DragDropQuestion dragDropQuestion=(DragDropQuestion)question;
			List<Answer> usedRightAnswers=new ArrayList<Answer>();
			if (update)
			{
				for (Answer answer:dragDropQuestion.getDroppableAnswersSortedByPosition())
				{
					Answer rightAnswer=((DragDropAnswer)answer).getRightAnswer();
					if (rightAnswer!=null)
					{
						if (usedRightAnswers.contains(rightAnswer))
						{
							((DragDropAnswer)answer).setRightAnswer(null);
						}
						else
						{
							usedRightAnswers.add(rightAnswer);
						}
					}
				}
			}
			else
			{
				for (Answer answer:dragDropQuestion.getDroppableAnswersSortedByPosition())
				{
					Answer rightAnswer=((DragDropAnswer)answer).getRightAnswer();
					if (rightAnswer!=null)
					{
						if (usedRightAnswers.contains(rightAnswer))
						{
							ok=false;
							break;
						}
						else
						{
							usedRightAnswers.add(rightAnswer);
						}
					}
				}
			}
		}
		return ok;
	}
	
	/**
	 * Ajax listener to perform checks when modifying a property.
	 * @param event Ajax event
	 */
	public void changeProperty(AjaxBehaviorEvent event)
	{
		boolean needConfirm=false;
		propertyChecked=(String)event.getComponent().getAttributes().get("property");
		if ("singleResponse".equals(propertyChecked))
		{
			needConfirm=!checkFeedbacksForChangeSingleResponse();
			setConfirmChangePropertyMessage("CONFIRM_CHANGE_PROPERTY_FOR_FEEDBACKS");
		}
		else if ("correct".equals(propertyChecked))
		{
			answerChecked=(Answer)event.getComponent().getAttributes().get("answer");
			needConfirm=!checkFeedbacksForChangeCorrect();
			setConfirmChangePropertyMessage("CONFIRM_CHANGE_PROPERTY_FOR_FEEDBACKS");
		}
		else if ("infinite".equals(propertyChecked))
		{
			needConfirm=!checkAnswersForChangeInfinite();
			setConfirmChangePropertyMessage("CONFIRM_CHANGE_PROPERTY_FOR_ANSWERS");
		}
		if (needConfirm)
		{
			RequestContext rq=RequestContext.getCurrentInstance();
			rq.execute("confirmChangePropertyDialog.show()");
		}
	}
	
	/**
	 * Action listener to make needed changes if we confirm changing a property.
	 * @param event Action event
	 */
	public void confirmChangeProperty(ActionEvent event)
	{
		if (propertyChecked!=null)
		{
			if (propertyChecked.equals("singleResponse"))
			{
				updateFeedbacksForChangeSingleResponse();
				
				updateAnswerResourceImage(getActiveAnswer(event.getComponent()));
			}
			else if (propertyChecked.equals("correct"))
			{
				updateFeedbacksForChangeCorrect();
				
				updateAnswerResourceImage(getActiveAnswer(event.getComponent()));
			}
			else if (propertyChecked.equals("infinite"))
			{
				updateAnswersForChangeInfinite();
				
				Operation operation=updateAnswerResourceImage(getActiveDraggableItem(event.getComponent()));
				updateAnswerResourceImage(operation,getActiveDroppableAnswer(event.getComponent()));
			}
		}
	}
	
	/**
	 * Action listener to rollback the old value of the last property modified.
	 * @param event Action event
	 */
	public void rollbackProperty(ActionEvent event)
	{
		if (propertyChecked!=null)
		{
			if (propertyChecked.equals("singleResponse"))
			{
				MultichoiceQuestion multichoiceQuestion=(MultichoiceQuestion)getQuestion();
				multichoiceQuestion.setSingle(!multichoiceQuestion.isSingle());
				
				updateAnswerResourceImage(getActiveAnswer(event.getComponent()));
			}
			else if (propertyChecked.equals("correct"))
			{
				if (answerChecked!=null)
				{
					answerChecked.setCorrect(!answerChecked.getCorrect());
				}
				
				updateAnswerResourceImage(getActiveAnswer(event.getComponent()));
			}
			else if (propertyChecked.equals("infinite"))
			{
				DragDropQuestion dragDropQuestion=(DragDropQuestion)question;
				dragDropQuestion.setInfinite(!dragDropQuestion.isInfinite());
				
				Operation operation=updateAnswerResourceImage(getActiveDraggableItem(event.getComponent()));
				updateAnswerResourceImage(operation,getActiveDroppableAnswer(event.getComponent()));
			}
		}
	}
	
	/**
	 * Action listener to show the dialog for adding a new feedback.
	 * @param event Action event
	 */
	public void showAddFeedback(ActionEvent event)
	{
		// New feedback with default values
		setCurrentFeedback(new FeedbackBean(this));
		activeConditionIndex=-1;
		RequestContext rq=RequestContext.getCurrentInstance();
		rq.execute("addFeedbackDialog.show()");
	}
	
	/**
	 * Action listener to show the dialog for updating a feedback.
	 * @param event Action event
	 */
	public void showEditFeedback(ActionEvent event)
	{
		FeedbackBean currentFeedback=
			new FeedbackBean((FeedbackBean)event.getComponent().getAttributes().get("feedback"));
		setCurrentFeedback(currentFeedback);
		
		updateCurrentFeedbackResourceImage();
		
		if (currentFeedback.getConditionsSize()==0)
		{
			activeConditionIndex=-1;
		}
		else
		{
			activeConditionIndex=0;
			AccordionPanel conditionsAccordion=
				(AccordionPanel)event.getComponent().findComponent(":feedbackDialogForm:conditionsAccordion");
			conditionsAccordion.setActiveIndex("0");
		}
		RequestContext rq=RequestContext.getCurrentInstance();
		rq.execute("addFeedbackDialog.show()");
	}
	
	/**
	 * Check attempts min value changing it to a valid value if it is invalid.
	 * @return true if attempts min value is valid, false otherwise
	 */
	private boolean checkAndChangeFeedbackAttemptsBetweenMin()
	{
		boolean ok=true;
		AttemptsConditionBean attemptsCondition=getCurrentFeedback().getAttemptsCondition();
		int attemptsBetweenMin=attemptsCondition.getAttemptsBetweenMin();
		int attemptsBetweenMax=attemptsCondition.getAttemptsBetweenMax();
		if (attemptsBetweenMin<1)
		{
			attemptsCondition.setAttemptsBetweenMin(1);
			ok=false;
		}
		else if (attemptsBetweenMin>attemptsBetweenMax)
		{
			attemptsCondition.setAttemptsBetweenMin(attemptsBetweenMax);
			ok=false;
		}
		return ok;
	}
	
	/**
	 * Check attempts max value changing it to a valid value if it is invalid.
	 * @return true if attempts max value is valid, false otherwise
	 */
	private boolean checkAndChangeFeedbackAttemptsBetweenMax()
	{
		boolean ok=true;
		AttemptsConditionBean attemptsCondition=getCurrentFeedback().getAttemptsCondition();
		int attemptsBetweenMin=attemptsCondition.getAttemptsBetweenMin();
		int attemptsBetweenMax=attemptsCondition.getAttemptsBetweenMax();
		if (attemptsBetweenMax<attemptsBetweenMin)
		{
			attemptsCondition.setAttemptsBetweenMax(attemptsBetweenMin);
			ok=false;
		}
		else if (attemptsBetweenMax>AttemptsConditionBean.MAX_ATTEMPT)
		{
			attemptsCondition.setAttemptsBetweenMax(AttemptsConditionBean.MAX_ATTEMPT);
			ok=false;
		}
		return ok;
	}
	
	/**
	 * Check attempts value changing it to a valid value if it is invalid.
	 * @return true if attempts value is valid, false otherwise
	 */
	private boolean checkAndChangeFeedbackAttemptsCmp()
	{
		boolean ok=true;
		AttemptsConditionBean attemptsCondition=getCurrentFeedback().getAttemptsCondition();
		int attemptsCmp=attemptsCondition.getAttemptsCmp();
		int minValue=attemptsCondition.getMinValueAttemptCmp();
		int maxValue=attemptsCondition.getMaxValueAttemptCmp();
		if (attemptsCmp<minValue)
		{
			attemptsCondition.setAttemptsCmp(minValue);
			ok=false;
		}
		else if (attemptsCmp>maxValue)
		{
			attemptsCondition.setAttemptsCmp(maxValue);
			ok=false;
		}
		return ok;
	}
	
	/**
	 * Check selected answers min value changing it to a valid value if it is invalid.
	 * @return true if selected answers min value is valid, false otherwise
	 */
	private boolean checkAndChangeFeedbackSelectedAnswersBetweenMin()
	{
		boolean ok=false;
		SelectedAnswersConditionBean selectedAnswersCondition=getCurrentFeedback().getSelectedAnswersCondition();
		int selectedAnswersBetweenMin=selectedAnswersCondition.getSelectedAnswersBetweenMin();
		int selectedAnswersBetweenMax=selectedAnswersCondition.getSelectedAnswersBetweenMax();
		if (selectedAnswersBetweenMin<0)
		{
			selectedAnswersCondition.setSelectedAnswersBetweenMin(0);
			ok=false;
		}
		else if (selectedAnswersBetweenMin>selectedAnswersBetweenMax)
		{
			selectedAnswersCondition.setSelectedAnswersBetweenMin(selectedAnswersBetweenMax);
			ok=false;
		}
		return ok;
	}
	
	/**
	 * Check selected answers max value changing it to a valid value if it is invalid.
	 * @return true if selected answers max value is valid, false otherwise
	 */
	private boolean checkAndChangeFeedbackSelectedAnswersBetweenMax()
	{
		boolean ok=true;
		SelectedAnswersConditionBean selectedAnswersCondition=getCurrentFeedback().getSelectedAnswersCondition();
		int selectedAnswersBetweenMin=selectedAnswersCondition.getSelectedAnswersBetweenMin();
		int selectedAnswersBetweenMax=selectedAnswersCondition.getSelectedAnswersBetweenMax();
		int maxValue=selectedAnswersCondition.getMaxSelectedAnswersValue();
		if (selectedAnswersBetweenMax<selectedAnswersBetweenMin)
		{
			selectedAnswersCondition.setSelectedAnswersBetweenMin(selectedAnswersBetweenMin);
			ok=false;
		}
		else if (selectedAnswersBetweenMax>maxValue)
		{
			selectedAnswersCondition.setSelectedAnswersBetweenMin(maxValue);
			ok=false;
		}
		return ok;
	}
	
	/**
	 * Check selected answers value changing it to a valid value if it is invalid.
	 * @return true if selected answers value is valid, false otherwise
	 */
	private boolean checkAndChangeFeedbackSelectedAnswersCmp()
	{
		boolean ok=true;
		SelectedAnswersConditionBean selectedAnswersCondition=
			getCurrentFeedback().getSelectedAnswersCondition();
		int selectedAnswersCmp=selectedAnswersCondition.getSelectedAnswersCmp();
		int minValue=selectedAnswersCondition.getMinValueSelectedAnswersCmp();
		int maxValue=selectedAnswersCondition.getMaxValueSelectedAnswersCmp();
		if (selectedAnswersCmp<minValue)
		{
			selectedAnswersCondition.setSelectedAnswersCmp(minValue);
			ok=false;
		}
		else if (selectedAnswersCmp>maxValue)
		{
			selectedAnswersCondition.setSelectedAnswersCmp(maxValue);
			ok=false;
		}
		return ok;
	}
	
	/**
	 * Check selected right answers min value changing it to a valid value if it is invalid.
	 * @return true if selected right answers min value is valid, false otherwise
	 */
	private boolean checkAndChangeFeedbackSelectedRightAnswersBetweenMin()
	{
		boolean ok=true;
		SelectedRightAnswersConditionBean selectedRightAnswersCondition=
			getCurrentFeedback().getSelectedRightAnswersCondition();
		int selectedRightAnswersBetweenMin=selectedRightAnswersCondition.getSelectedRightAnswersBetweenMin();
		int selectedRightAnswersBetweenMax=selectedRightAnswersCondition.getSelectedRightAnswersBetweenMax();
		if (selectedRightAnswersBetweenMin<0)
		{
			selectedRightAnswersCondition.setSelectedRightAnswersBetweenMin(0);
			ok=false;
		}
		else if (selectedRightAnswersBetweenMin>selectedRightAnswersBetweenMax)
		{
			selectedRightAnswersCondition.setSelectedRightAnswersBetweenMin(selectedRightAnswersBetweenMax);
			ok=false;
		}
		return ok;
	}
	
	/**
	 * Check selected right answers max value changing it to a valid value if it is invalid.
	 * @return true if selected right answers max value is valid, false otherwise
	 */
	private boolean checkAndChangeFeedbackSelectedRightAnswersBetweenMax()
	{
		boolean ok=true;
		SelectedRightAnswersConditionBean selectedRightAnswersCondition=
			getCurrentFeedback().getSelectedRightAnswersCondition();
		int selectedRightAnswersBetweenMin=selectedRightAnswersCondition.getSelectedRightAnswersBetweenMin();
		int selectedRightAnswersBetweenMax=selectedRightAnswersCondition.getSelectedRightAnswersBetweenMax();
		int maxValue=selectedRightAnswersCondition.getMaxSelectedRightAnswersValue();
		if (selectedRightAnswersBetweenMax<selectedRightAnswersBetweenMin)
		{
			selectedRightAnswersCondition.setSelectedRightAnswersBetweenMin(selectedRightAnswersBetweenMin);
			ok=false;
		}
		else if (selectedRightAnswersBetweenMax>maxValue)
		{
			selectedRightAnswersCondition.setSelectedRightAnswersBetweenMin(maxValue);
			ok=false;
		}
		return ok;
	}
	
	/**
	 * Check selected right answers value changing it to a valid value if it is invalid.
	 * @return true if selected right answers value is valid, false otherwise
	 */
	private boolean checkAndChangeFeedbackSelectedRightAnswersCmp()
	{
		boolean ok=true;
		SelectedRightAnswersConditionBean selectedRightAnswersCondition=
			getCurrentFeedback().getSelectedRightAnswersCondition();
		int selectedRightAnswersCmp=selectedRightAnswersCondition.getSelectedRightAnswersCmp();
		int minValue=selectedRightAnswersCondition.getMinValueSelectedRightAnswersCmp();
		int maxValue=selectedRightAnswersCondition.getMaxValueSelectedRightAnswersCmp();
		if (selectedRightAnswersCmp<minValue)
		{
			selectedRightAnswersCondition.setSelectedRightAnswersCmp(minValue);
			ok=false;
		}
		else if (selectedRightAnswersCmp>maxValue)
		{
			selectedRightAnswersCondition.setSelectedRightAnswersCmp(maxValue);
			ok=false;
		}
		return ok;
	}
	
	/**
	 * Check selected wrong answers min value changing it to a valid value if it is invalid.
	 * @return true if selected wrong answers min value is valid, false otherwise
	 */
	private boolean checkAndChangeFeedbackSelectedWrongAnswersBetweenMin()
	{
		boolean ok=true;
		SelectedWrongAnswersConditionBean selectedWrongAnswersCondition=
			getCurrentFeedback().getSelectedWrongAnswersCondition();
		int selectedWrongAnswersBetweenMin=selectedWrongAnswersCondition.getSelectedWrongAnswersBetweenMin();
		int selectedWrongAnswersBetweenMax=selectedWrongAnswersCondition.getSelectedWrongAnswersBetweenMax();
		if (selectedWrongAnswersBetweenMin<0)
		{
			selectedWrongAnswersCondition.setSelectedWrongAnswersBetweenMin(0);
			ok=false;
		}
		else if (selectedWrongAnswersBetweenMin>selectedWrongAnswersBetweenMax)
		{
			selectedWrongAnswersCondition.setSelectedWrongAnswersBetweenMin(selectedWrongAnswersBetweenMax);
			ok=false;
		}
		return ok;
	}
	
	/**
	 * Check selected wrong answers max value changing it to a valid value if it is invalid.
	 * @return true if selected wrong answers max value is valid, false otherwise
	 */
	private boolean checkAndChangeFeedbackSelectedWrongAnswersBetweenMax()
	{
		boolean ok=true;
		SelectedWrongAnswersConditionBean selectedWrongAnswersCondition=
			getCurrentFeedback().getSelectedWrongAnswersCondition();
		int selectedWrongAnswersBetweenMin=selectedWrongAnswersCondition.getSelectedWrongAnswersBetweenMin();
		int selectedWrongAnswersBetweenMax=selectedWrongAnswersCondition.getSelectedWrongAnswersBetweenMax();
		int maxValue=selectedWrongAnswersCondition.getMaxSelectedWrongAnswersValue();
		if (selectedWrongAnswersBetweenMax<selectedWrongAnswersBetweenMin)
		{
			selectedWrongAnswersCondition.setSelectedWrongAnswersBetweenMin(selectedWrongAnswersBetweenMin);
			ok=false;
		}
		else if (selectedWrongAnswersBetweenMax>maxValue)
		{
			selectedWrongAnswersCondition.setSelectedWrongAnswersBetweenMin(maxValue);
			ok=false;
		}
		return ok;
	}
	
	/**
	 * Check selected wrong answers value changing it to a valid value if it is invalid.
	 * @return true if selected wrong answers value is valid, false otherwise
	 */
	private boolean checkAndChangeFeedbackSelectedWrongAnswersCmp()
	{
		boolean ok=true;
		SelectedWrongAnswersConditionBean selectedWrongAnswersCondition=
			getCurrentFeedback().getSelectedWrongAnswersCondition();
		int selectedWrongAnswersCmp=selectedWrongAnswersCondition.getSelectedWrongAnswersCmp();
		int minValue=selectedWrongAnswersCondition.getMinValueSelectedWrongAnswersCmp();
		int maxValue=
			selectedWrongAnswersCondition.getMaxValueSelectedWrongAnswersCmp();
		if (selectedWrongAnswersCmp<minValue)
		{
			selectedWrongAnswersCondition.setSelectedWrongAnswersCmp(minValue);
			ok=false;
		}
		else if (selectedWrongAnswersCmp>maxValue)
		{
			selectedWrongAnswersCondition.setSelectedWrongAnswersCmp(maxValue);
			ok=false;
		}
		return ok;
	}
	
	/**
	 * Check unselected answers min value changing it to a valid value if it is invalid.
	 * @return true if unselected answers min value is valid, false otherwise
	 */
	private boolean checkAndChangeFeedbackUnselectedAnswersBetweenMin()
	{
		boolean ok=true;
		UnselectedAnswersConditionBean unselectedAnswersCondition=
			getCurrentFeedback().getUnselectedAnswersCondition();
		int unselectedAnswersBetweenMin=unselectedAnswersCondition.getUnselectedAnswersBetweenMin();
		int unselectedAnswersBetweenMax=unselectedAnswersCondition.getUnselectedAnswersBetweenMax();
		if (unselectedAnswersBetweenMin<0)
		{
			unselectedAnswersCondition.setUnselectedAnswersBetweenMin(0);
			ok=false;
		}
		else if (unselectedAnswersBetweenMin>unselectedAnswersBetweenMax)
		{
			unselectedAnswersCondition.setUnselectedAnswersBetweenMin(unselectedAnswersBetweenMax);
			ok=false;
		}
		return ok;
	}
	
	/**
	 * Check unselected answers max value changing it to a valid value if it is invalid.
	 * @return true if unselected answers max value is valid, false otherwise
	 */
	private boolean checkAndChangeFeedbackUnselectedAnswersBetweenMax()
	{
		boolean ok=true;
		UnselectedAnswersConditionBean unselectedAnswersCondition=
			getCurrentFeedback().getUnselectedAnswersCondition();
		int unselectedAnswersBetweenMin=unselectedAnswersCondition.getUnselectedAnswersBetweenMin();
		int unselectedAnswersBetweenMax=unselectedAnswersCondition.getUnselectedAnswersBetweenMax();
		int maxValue=unselectedAnswersCondition.getMaxUnselectedAnswersValue();
		if (unselectedAnswersBetweenMax<unselectedAnswersBetweenMin)
		{
			unselectedAnswersCondition.setUnselectedAnswersBetweenMax(unselectedAnswersBetweenMin);
			ok=false;
		}
		else if (unselectedAnswersBetweenMax>maxValue)
		{
			unselectedAnswersCondition.setUnselectedAnswersBetweenMax(maxValue);
			ok=false;
		}
		return ok;
	}
	
	/**
	 * Check unselected answers value changing it to a valid value if it is invalid.
	 * @return true if unselected answers value is valid, false otherwise
	 */
	private boolean checkAndChangeFeedbackUnselectedAnswersCmp()
	{
		boolean ok=true;
		UnselectedAnswersConditionBean unselectedAnswersCondition=
			getCurrentFeedback().getUnselectedAnswersCondition();
		int unselectedAnswersCmp=unselectedAnswersCondition.getUnselectedAnswersCmp();
		int minValue=unselectedAnswersCondition.getMinValueUnselectedAnswersCmp();
		int maxValue=unselectedAnswersCondition.getMaxValueUnselectedAnswersCmp();
		if (unselectedAnswersCmp<minValue)
		{
			unselectedAnswersCondition.setUnselectedAnswersCmp(minValue);
			ok=false;
		}
		else if (unselectedAnswersCmp>maxValue)
		{
			unselectedAnswersCondition.setUnselectedAnswersCmp(maxValue);
			ok=false;
		}
		return ok;
	}
	
	/**
	 * Check unselected right answers min value changing it to a valid value if it is invalid.
	 * @return true if unselected right answers min value is valid, false otherwise
	 */
	private boolean checkAndChangeFeedbackUnselectedRightAnswersBetweenMin()
	{
		boolean ok=true;
		UnselectedRightAnswersConditionBean unselectedRightAnswersCondition=
			getCurrentFeedback().getUnselectedRightAnswersCondition();
		int unselectedRightAnswersBetweenMin=
			unselectedRightAnswersCondition.getUnselectedRightAnswersBetweenMin();
		int unselectedRightAnswersBetweenMax=
			unselectedRightAnswersCondition.getUnselectedRightAnswersBetweenMax();
		if (unselectedRightAnswersBetweenMin<0)
		{
			unselectedRightAnswersCondition.setUnselectedRightAnswersBetweenMin(0);
			ok=false;
		}
		else if (unselectedRightAnswersBetweenMin>unselectedRightAnswersBetweenMax)
		{
			unselectedRightAnswersCondition.setUnselectedRightAnswersBetweenMin(unselectedRightAnswersBetweenMax);
			ok=false;
		}
		return ok;
	}
	
	/**
	 * Check unselected right answers max value changing it to a valid value if it is invalid.
	 * @return true if unselected right answers max value is valid, false otherwise
	 */
	private boolean checkAndChangeFeedbackUnselectedRightAnswersBetweenMax()
	{
		boolean ok=true;
		UnselectedRightAnswersConditionBean unselectedRightAnswersCondition=
			getCurrentFeedback().getUnselectedRightAnswersCondition();
		int unselectedRightAnswersBetweenMin=
			unselectedRightAnswersCondition.getUnselectedRightAnswersBetweenMin();
		int unselectedRightAnswersBetweenMax=
			unselectedRightAnswersCondition.getUnselectedRightAnswersBetweenMax();
		int maxValue=unselectedRightAnswersCondition.getMaxUnselectedRightAnswersValue();
		if (unselectedRightAnswersBetweenMax<unselectedRightAnswersBetweenMin)
		{
			unselectedRightAnswersCondition.setUnselectedRightAnswersBetweenMax(
				unselectedRightAnswersBetweenMin);
			ok=false;
		}
		else if (unselectedRightAnswersBetweenMax>maxValue)
		{
			unselectedRightAnswersCondition.setUnselectedRightAnswersBetweenMax(maxValue);
			ok=false;
		}
		return ok;
	}
	
	/**
	 * Check unselected right answers value changing it to a valid value if it is invalid.
	 * @return true if unselected right answers value is valid, false otherwise
	 */
	private boolean checkAndChangeFeedbackUnselectedRightAnswersCmp()
	{
		boolean ok=true;
		UnselectedRightAnswersConditionBean unselectedRightAnswersCondition=
			getCurrentFeedback().getUnselectedRightAnswersCondition();
		int unselectedRightAnswersCmp=unselectedRightAnswersCondition.getUnselectedRightAnswersCmp();
		int minValue=unselectedRightAnswersCondition.getMinValueUnselectedRightAnswersCmp();
		int maxValue=unselectedRightAnswersCondition.getMaxValueUnselectedRightAnswersCmp();
		if (unselectedRightAnswersCmp<minValue)
		{
			unselectedRightAnswersCondition.setUnselectedRightAnswersCmp(minValue);
			ok=false;
		}
		else if (unselectedRightAnswersCmp>maxValue)
		{
			unselectedRightAnswersCondition.setUnselectedRightAnswersCmp(maxValue);
			ok=false;
		}
		return ok;
	}
	
	/**
	 * Check unselected wrong answers min value changing it to a valid value if it is invalid.
	 * @return true if unselected wrong answers min value is valid, false otherwise
	 */
	private boolean checkAndChangeFeedbackUnselectedWrongAnswersBetweenMin()
	{
		boolean ok=true;
		UnselectedWrongAnswersConditionBean unselectedWrongAnswersCondition=
			getCurrentFeedback().getUnselectedWrongAnswersCondition();
		int unselectedWrongAnswersBetweenMin=
			unselectedWrongAnswersCondition.getUnselectedWrongAnswersBetweenMin();
		int unselectedWrongAnswersBetweenMax=
			unselectedWrongAnswersCondition.getUnselectedWrongAnswersBetweenMax();
		if (unselectedWrongAnswersBetweenMin<0)
		{
			unselectedWrongAnswersCondition.setUnselectedWrongAnswersBetweenMin(0);
			ok=false;
		}
		else if (unselectedWrongAnswersBetweenMin>unselectedWrongAnswersBetweenMax)
		{
			unselectedWrongAnswersCondition.setUnselectedWrongAnswersBetweenMin(unselectedWrongAnswersBetweenMax);
			ok=false;
		}
		return ok;
	}
	
	/**
	 * Check unselected wrong answers max value changing it to a valid value if it is invalid.
	 * @return true if unselected wrong answers max value is valid, false otherwise
	 */
	private boolean checkAndChangeFeedbackUnselectedWrongAnswersBetweenMax()
	{
		boolean ok=true;
		UnselectedWrongAnswersConditionBean unselectedWrongAnswersCondition=
			getCurrentFeedback().getUnselectedWrongAnswersCondition();
		int unselectedWrongAnswersBetweenMin=
			unselectedWrongAnswersCondition.getUnselectedWrongAnswersBetweenMin();
		int unselectedWrongAnswersBetweenMax=
			unselectedWrongAnswersCondition.getUnselectedWrongAnswersBetweenMax();
		int maxValue=unselectedWrongAnswersCondition.getMaxUnselectedWrongAnswersValue();
		if (unselectedWrongAnswersBetweenMax<unselectedWrongAnswersBetweenMin)
		{
			unselectedWrongAnswersCondition.setUnselectedWrongAnswersBetweenMax(unselectedWrongAnswersBetweenMin);
			ok=false;
		}
		else if (unselectedWrongAnswersBetweenMax>maxValue)
		{
			unselectedWrongAnswersCondition.setUnselectedWrongAnswersBetweenMax(maxValue);
			ok=false;
		}
		return ok;
	}
	
	/**
	 * Check unselected wrong answers value changing it to a valid value if it is invalid.
	 * @return true if unselected wrong answers value is valid, false otherwise
	 */
	private boolean checkAndChangeFeedbackUnselectedWrongAnswersCmp()
	{
		boolean ok=true;
		UnselectedWrongAnswersConditionBean unselectedWrongAnswersCondition=
			getCurrentFeedback().getUnselectedWrongAnswersCondition();
		int unselectedWrongAnswersCmp=unselectedWrongAnswersCondition.getUnselectedWrongAnswersCmp();
		int minValue=unselectedWrongAnswersCondition.getMinValueUnselectedWrongAnswersCmp();
		int maxValue=unselectedWrongAnswersCondition.getMaxValueUnselectedWrongAnswersCmp();
		if (unselectedWrongAnswersCmp<minValue)
		{
			unselectedWrongAnswersCondition.setUnselectedWrongAnswersCmp(minValue);
			ok=false;
		}
		else if (unselectedWrongAnswersCmp>maxValue)
		{
			unselectedWrongAnswersCondition.setUnselectedWrongAnswersCmp(maxValue);
			ok=false;
		}
		return ok;
	}
	
	/**
	 * Check right distance min value changing it to a valid value if it is invalid.
	 * @return true if right distance min value is valid, false otherwise
	 */
	private boolean checkAndChangeFeedbackRightDistanceBetweenMin()
	{
		boolean ok=true;
		RightDistanceConditionBean rightDistanceCondition=getCurrentFeedback().getRightDistanceCondition();
		int rightDistanceBetweenMin=rightDistanceCondition.getRightDistanceBetweenMin();
		int rightDistanceBetweenMax=rightDistanceCondition.getRightDistanceBetweenMax();
		if (rightDistanceBetweenMin<0)
		{
			rightDistanceCondition.setRightDistanceBetweenMin(0);
			ok=false;
		}
		else if (rightDistanceBetweenMin>rightDistanceBetweenMax)
		{
			rightDistanceCondition.setRightDistanceBetweenMin(rightDistanceBetweenMax);
			ok=false;
		}
		return ok;
	}
	
	/**
	 * Check right distance max value changing it to a valid value if it is invalid.
	 * @return true if right distance max value is valid, false otherwise
	 */
	private boolean checkAndChangeFeedbackRightDistanceBetweenMax()
	{
		boolean ok=true;
		RightDistanceConditionBean rightDistanceCondition=getCurrentFeedback().getRightDistanceCondition();
		int rightDistanceBetweenMin=rightDistanceCondition.getRightDistanceBetweenMin();
		int rightDistanceBetweenMax=rightDistanceCondition.getRightDistanceBetweenMax();
		int maxValue=rightDistanceCondition.getMaxRightDistanceValue();
		if (rightDistanceBetweenMax<rightDistanceBetweenMin)
		{
			rightDistanceCondition.setRightDistanceBetweenMax(rightDistanceBetweenMin);
			ok=false;
		}
		else if (rightDistanceBetweenMax>maxValue)
		{
			rightDistanceCondition.setRightDistanceBetweenMax(maxValue);
			ok=false;
		}
		return ok;
	}
	
	/**
	 * Check right distance value changing it to a valid value if it is invalid.
	 * @return true if right distance value is valid, false otherwise
	 */
	private boolean checkAndChangeFeedbackRightDistanceCmp()
	{
		boolean ok=true;
		RightDistanceConditionBean rightDistanceCondition=getCurrentFeedback().getRightDistanceCondition();
		int rightDistanceCmp=rightDistanceCondition.getRightDistanceCmp();
		int minValue=rightDistanceCondition.getMinValueRightDistanceCmp();
		int maxValue=rightDistanceCondition.getMaxValueRightDistanceCmp();
		if (rightDistanceCmp<minValue)
		{
			rightDistanceCondition.setRightDistanceCmp(minValue);
			ok=false;
		}
		else if (rightDistanceCmp>maxValue)
		{
			rightDistanceCondition.setRightDistanceCmp(maxValue);
			ok=false;
		}
		return ok;
	}
	
	/**
	 * Check feedback conditions values changing the invalid ones to valid.
	 * @return true if all feedback conditions values are valid, false otherwise
	 */
	private boolean checkAndChangeFeedbackConditionsValues()
	{
		boolean ok=true;
		
		AttemptsConditionBean attemptsCondition=getCurrentFeedback().getAttemptsCondition();
		if (attemptsCondition!=null)
		{
			if (NumberComparator.compareU(attemptsCondition.getComparator(),NumberComparator.BETWEEN))
			{
				if (!checkAndChangeFeedbackAttemptsBetweenMin())
				{
					ok=false;
				}
				if (!checkAndChangeFeedbackAttemptsBetweenMax())
				{
					ok=false;
				}
			}
			else if (!checkAndChangeFeedbackAttemptsCmp())
			{
				ok=false;
			}
		}
		SelectedAnswersConditionBean selectedAnswersCondition=getCurrentFeedback().getSelectedAnswersCondition();
		if (selectedAnswersCondition!=null)
		{
			if (NumberComparator.compareU(selectedAnswersCondition.getComparator(),NumberComparator.BETWEEN))
			{
				if (!checkAndChangeFeedbackSelectedAnswersBetweenMin())
				{
					ok=false;
				}
				if (!checkAndChangeFeedbackSelectedAnswersBetweenMax())
				{
					ok=false;
				}
			}
			else if (!checkAndChangeFeedbackSelectedAnswersCmp())
			{
				ok=false;
				
			}
		}
		SelectedRightAnswersConditionBean selectedRightAnswersCondition=
			getCurrentFeedback().getSelectedRightAnswersCondition();
		if (selectedRightAnswersCondition!=null)
		{
			if (NumberComparator.compareU(
				selectedRightAnswersCondition.getComparator(),NumberComparator.BETWEEN))
			{
				if (!checkAndChangeFeedbackSelectedRightAnswersBetweenMin())
				{
					ok=false;
				}
				if (!checkAndChangeFeedbackSelectedRightAnswersBetweenMax())
				{
					ok=false;
				}
			}
			else if (!checkAndChangeFeedbackSelectedRightAnswersCmp())
			{
				ok=false;
			}
		}
		SelectedWrongAnswersConditionBean selectedWrongAnswersCondition=
			getCurrentFeedback().getSelectedWrongAnswersCondition();
		if (selectedWrongAnswersCondition!=null)
		{
			if (NumberComparator.compareU(
				selectedWrongAnswersCondition.getComparator(),NumberComparator.BETWEEN))
			{
				if (!checkAndChangeFeedbackSelectedWrongAnswersBetweenMin())
				{
					ok=false;
				}
				if (!checkAndChangeFeedbackSelectedWrongAnswersBetweenMax())
				{
					ok=false;
				}
			}
			else if (!checkAndChangeFeedbackSelectedWrongAnswersCmp())
			{
				ok=false;
			}
		}
		UnselectedAnswersConditionBean unselectedAnswersCondition=
			getCurrentFeedback().getUnselectedAnswersCondition();
		if (unselectedAnswersCondition!=null)
		{
			if (NumberComparator.compareU(unselectedAnswersCondition.getComparator(),NumberComparator.BETWEEN))
			{
				if (!checkAndChangeFeedbackUnselectedAnswersBetweenMin())
				{
					ok=false;
				}
				if (!checkAndChangeFeedbackUnselectedAnswersBetweenMax())
				{
					ok=false;
				}
			}
			else if (!checkAndChangeFeedbackUnselectedAnswersCmp())
			{
				ok=false;
			}
		}
		UnselectedRightAnswersConditionBean unselectedRightAnswersCondition=
			getCurrentFeedback().getUnselectedRightAnswersCondition();
		if (unselectedRightAnswersCondition!=null)
		{
			if (NumberComparator.compareU(
				unselectedRightAnswersCondition.getComparator(),NumberComparator.BETWEEN))
			{
				if (!checkAndChangeFeedbackUnselectedRightAnswersBetweenMin())
				{
					ok=false;
				}
				if (!checkAndChangeFeedbackUnselectedRightAnswersBetweenMax())
				{
					ok=false;
				}
			}
			else if (!checkAndChangeFeedbackUnselectedRightAnswersCmp())
			{
				ok=false;
			}
		}
		UnselectedWrongAnswersConditionBean unselectedWrongAnswersCondition=
			getCurrentFeedback().getUnselectedWrongAnswersCondition();
		if (unselectedWrongAnswersCondition!=null)
		{
			if (NumberComparator.compareU(
				unselectedWrongAnswersCondition.getComparator(),NumberComparator.BETWEEN))
			{
				if (!checkAndChangeFeedbackUnselectedWrongAnswersBetweenMin())
				{
					ok=false;
				}
				if (!checkAndChangeFeedbackUnselectedWrongAnswersBetweenMax())
				{
					ok=false;
				}
			}
			else if (!checkAndChangeFeedbackUnselectedWrongAnswersCmp())
			{
				ok=false;
			}
		}
		RightDistanceConditionBean rightDistanceCondition=getCurrentFeedback().getRightDistanceCondition();
		if (rightDistanceCondition!=null)
		{
			if (NumberComparator.compareU(rightDistanceCondition.getComparator(),NumberComparator.BETWEEN))
			{
				if (!checkAndChangeFeedbackRightDistanceBetweenMin())
				{
					ok=false;
				}
				if (!checkAndChangeFeedbackRightDistanceBetweenMax())
				{
					ok=false;
				}
			}
			else if (!checkAndChangeFeedbackRightDistanceCmp())
			{
				ok=false;
			}
		}
		return ok;
	}
	
	/**
	 * Action listener to create/update a feedback if we accept the changes within the dialog for 
	 * adding/updating a feedback.<br/><br/>
	 * Note that feedback condition values are checked and if any of them is invalid it is changed 
	 * to a valid one and the dialog is not closed.
	 * @param event Action event
	 */
	public void acceptAddFeedback(ActionEvent event)
	{
		if (checkAndChangeFeedbackConditionsValues())
		{
			Question question=getQuestion();
			List<FeedbackBean> feedbacks=getFeedbacks();
			FeedbackBean currentFeedback=getCurrentFeedback();
			Feedback feedback=currentFeedback.getAsFeedback();
			if (currentFeedback.getPosition()>question.getFeedbacks().size())
			{
				question.addFeedback(feedback);
				feedbacks.add(currentFeedback);
			}
			else
			{
				Feedback fb=null;
				for (Feedback f:question.getFeedbacks())
				{
					if (f.getPosition()==feedback.getPosition())
					{
						fb=f;
						break;
					}
				}
				FeedbackBean fbBean=null;
				for (FeedbackBean fBean:feedbacks)
				{
					if (fBean.getPosition()==currentFeedback.getPosition())
					{
						fbBean=fBean;
						break;
					}
				}
				if (fb!=null && fbBean!=null)
				{
					fb.setFromOtherFeedback(feedback);
					fbBean.setFromOtherFeedback(currentFeedback);
				}
			}
			
			updateIncorrectFeedbackResourcesImages();
		}
	}
	
    /**
	 * Action listener for updating resources information if we cancel the changes within the dialog for 
	 * adding advanced feedbacks to a question. 
     * @param event Action event
     */
	public void cancelAddFeedback(ActionEvent event)
	{
		updateIncorrectFeedbackResourcesImages();
	}
	
	/**
	 * Action listener to delete a feedback.
	 * @param event Action event
	 */
	public void removeFeedback(ActionEvent event)
	{
		FeedbackBean feedbackBean=(FeedbackBean)event.getComponent().getAttributes().get("feedback");
		int position=feedbackBean.getPosition();
		Feedback feedback=null;
		for (Feedback f:question.getFeedbacks())
		{
			if (f.getPosition()==position)
			{
				feedback=f;
			}
			else if (f.getPosition()>position)
			{
				f.setPosition(f.getPosition()-1);
			}
		}
		question.getFeedbacks().remove(feedback);
		for (FeedbackBean fBean:feedbacks)
		{
			if (fBean.getPosition()>position)
			{
				fBean.setPosition(fBean.getPosition()-1);
			}
		}
		feedbacks.remove(feedbackBean);
	}
	
	/**
	 * Tab change listener for displaying other condition within feedback conditions accordion.
	 * @param event Tab change event
	 */
	public void changeActiveCondition(TabChangeEvent event)
	{
		try
		{
			activeConditionIndex=Integer.parseInt(((AccordionPanel)event.getComponent()).getActiveIndex());
		}
		catch (NumberFormatException e)
		{
			activeConditionIndex=-1;
		}
		
		// We need to change feedback condition values to valid ones
		checkAndChangeFeedbackConditionsValues();
	}
	
	/**
	 * Ajax listener to check attempts value.
	 * @param event Ajax event
	 */
	public void changeFeedbackAttemptsCmp(AjaxBehaviorEvent event)
	{
		checkAndChangeFeedbackAttemptsCmp();
	}
	
	/**
	 * Ajax listener to check attempts min value.
	 * @param event Ajax event
	 */
	public void changeFeedbackAttemptsBetweenMin(AjaxBehaviorEvent event)
	{
		checkAndChangeFeedbackAttemptsBetweenMin();
	}
	
	/**
	 * Ajax listener to check attempts max value.
	 * @param event Ajax event
	 */
	public void changeFeedbackAttemptsBetweenMax(AjaxBehaviorEvent event)
	{
		checkAndChangeFeedbackAttemptsBetweenMax();
	}
	
	/**
	 * Ajax listener to check unselected answers value.
	 * @param event Ajax event
	 */
	public void changeFeedbackUnselectedAnswersCmp(AjaxBehaviorEvent event)
	{
		checkAndChangeFeedbackUnselectedAnswersCmp();
	}
	
	/**
	 * Ajax listener to check unselected answers min value.
	 * @param event Ajax event
	 */
	public void changeFeedbackUnselectedAnswersBetweenMin(AjaxBehaviorEvent event)
	{
		checkAndChangeFeedbackUnselectedAnswersBetweenMin();
	}
	
	/**
	 * Ajax listener to check unselected answers max value.
	 * @param event Ajax event
	 */
	public void changeFeedbackUnselectedAnswersBetweenMax(AjaxBehaviorEvent event)
	{
		checkAndChangeFeedbackUnselectedAnswersBetweenMax();
	}
	
	/**
	 * Ajax listener to check unselected right answers value.
	 * @param event Ajax event
	 */
	public void changeFeedbackUnselectedRightAnswersCmp(AjaxBehaviorEvent event)
	{
		checkAndChangeFeedbackUnselectedRightAnswersCmp();
	}
	
	/**
	 * Ajax listener to check unselected right answers min value.
	 * @param event Ajax event
	 */
	public void changeFeedbackUnselectedRightAnswersBetweenMin(AjaxBehaviorEvent event)
	{
		checkAndChangeFeedbackUnselectedRightAnswersBetweenMin();
	}
	
	/**
	 * Ajax listener to check unselected right answers max value.
	 * @param event Ajax event
	 */
	public void changeFeedbackUnselectedRightAnswersBetweenMax(AjaxBehaviorEvent event)
	{
		checkAndChangeFeedbackUnselectedRightAnswersBetweenMax();
	}
	
	/**
	 * Ajax listener to check unselected wrong answers value.
	 * @param event Ajax event
	 */
	public void changeFeedbackUnselectedWrongAnswersCmp(AjaxBehaviorEvent event)
	{
		checkAndChangeFeedbackUnselectedWrongAnswersCmp();
	}
	
	/**
	 * Ajax listener to check unselected wrong answers min value.
	 * @param event Ajax event
	 */
	public void changeFeedbackUnselectedWrongAnswersBetweenMin(AjaxBehaviorEvent event)
	{
		checkAndChangeFeedbackUnselectedWrongAnswersBetweenMin();
	}
	
	/**
	 * Ajax listener to check unselected wrong answers max value.
	 * @param event Ajax event
	 */
	public void changeFeedbackUnselectedWrongAnswersBetweenMax(AjaxBehaviorEvent event)
	{
		checkAndChangeFeedbackUnselectedWrongAnswersBetweenMax();
	}
	
	/**
	 * Ajax listener to check right distance value.
	 * @param event Ajax event
	 */
	public void changeFeedbackRightDistanceCmp(AjaxBehaviorEvent event)
	{
		checkAndChangeFeedbackRightDistanceCmp();
	}
	
	/**
	 * Ajax listener to check right distance min value.
	 * @param event Ajax event
	 */
	public void changeFeedbackRightDistanceBetweenMin(AjaxBehaviorEvent event)
	{
		checkAndChangeFeedbackRightDistanceBetweenMin();
	}
	
	/**
	 * Ajax listener to check right distance max value.
	 * @param event Ajax event
	 */
	public void changeFeedbackRightDistanceBetweenMax(AjaxBehaviorEvent event)
	{
		checkAndChangeFeedbackRightDistanceBetweenMax();
	}
	
	/**
	 * Ajax listener to check selected answers value.
	 * @param event Ajax event
	 */
	public void changeFeedbackSelectedAnswersCmp(AjaxBehaviorEvent event)
	{
		checkAndChangeFeedbackSelectedAnswersCmp();
	}
	
	/**
	 * Ajax listener to check selected answers min value.
	 * @param event Ajax event
	 */
	public void changeFeedbackSelectedAnswersBetweenMin(AjaxBehaviorEvent event)
	{
		checkAndChangeFeedbackSelectedAnswersBetweenMin();
	}
	
	/**
	 * Ajax listener to check selected answers max value.
	 * @param event Ajax event
	 */
	public void changeFeedbackSelectedAnswersBetweenMax(AjaxBehaviorEvent event)
	{
		checkAndChangeFeedbackSelectedAnswersBetweenMax();
	}
	
	/**
	 * Ajax listener to check selected right answers value.
	 * @param event Ajax event
	 */
	public void changeFeedbackSelectedRightAnswersCmp(AjaxBehaviorEvent event)
	{
		checkAndChangeFeedbackSelectedRightAnswersCmp();
	}
	
	/**
	 * Ajax listener to check selected right answers min value.
	 * @param event Ajax event
	 */
	public void changeFeedbackSelectedRightAnswersBetweenMin(AjaxBehaviorEvent event)
	{
		checkAndChangeFeedbackSelectedRightAnswersBetweenMin();
	}
	
	/**
	 * Ajax listener to check selected right answers max value.
	 * @param event Ajax event
	 */
	public void changeFeedbackSelectedRightAnswersBetweenMax(AjaxBehaviorEvent event)
	{
		checkAndChangeFeedbackSelectedRightAnswersBetweenMax();
	}
	
	/**
	 * Ajax listener to check selected wrong answers value.
	 * @param event Ajax event
	 */
	public void changeFeedbackSelectedWrongAnswersCmp(AjaxBehaviorEvent event)
	{
		checkAndChangeFeedbackSelectedWrongAnswersCmp();
	}
	
	/**
	 * Ajax listener to check selected wrong answers min value.
	 * @param event Ajax event
	 */
	public void changeFeedbackSelectedWrongAnswersBetweenMin(AjaxBehaviorEvent event)
	{
		checkAndChangeFeedbackSelectedWrongAnswersBetweenMin();
	}
	
	/**
	 * Ajax listener to check selected wrong answers max value.
	 * @param event Ajax event
	 */
	public void changeFeedbackSelectedWrongAnswersBetweenMax(AjaxBehaviorEvent event)
	{
		checkAndChangeFeedbackSelectedWrongAnswersBetweenMax();
	}
	
	/**
	 * @return Title of dialog for adding/updating a feedback
	 */
	public String getAddEditFeedbackTitle()
	{
		String title="";
		if (getCurrentFeedback()!=null)
		{
			if (getCurrentFeedback().getPosition()>getQuestion().getFeedbacks().size())
			{
				title=localizationService.getLocalizedMessage("ADD_FEEDBACK");
			}
			else
			{
				title=localizationService.getLocalizedMessage("EDIT_FEEDBACK");
			}
		}
		return title;
	}
	
    /**
     * @param categoryId Category identifier
     * @return Localized category long name
     */
    public String getLocalizedCategoryLongName(Long categoryId)
    {
    	return getLocalizedCategoryLongName(null,categoryId);
    }
    
    /**
     * @param operation Operation
     * @param categoryId Category identifier
     * @return Localized category long name
     */
    private String getLocalizedCategoryLongName(Operation operation,Long categoryId)
    {
    	return categoriesService.getLocalizedCategoryLongName(getCurrentUserOperation(operation),categoryId);
    }
	
    /**
     * @param categoryId Category identifier
	 * @param maxLength Maximum length
	 * @return Localized category long name, if length is greater than maximum length it will be abbreviated
     */
    public String getLocalizedCategoryLongName(Long categoryId,int maxLength)
    {
    	return getLocalizedCategoryLongName(null,categoryId,maxLength);
    }
    
    /**
     * @param operation Operation
     * @param categoryId Category identifier
	 * @param maxLength Maximum length
	 * @return Localized category long name, if length is greater than maximum length it will be abbreviated
     */
    private String getLocalizedCategoryLongName(Operation operation,Long categoryId,int maxLength)
    {
    	return categoriesService.getLocalizedCategoryLongName(
    		getCurrentUserOperation(operation),categoryId,maxLength);
    }
    
    /**
     * @param categoryId Category identifier
	 * @param maxLength Maximum length
     * @return Localized category filter name (abbreviated long name if it is a category)
     */
    public String getLocalizedCategoryFilterName(Long categoryId,int maxLength)
    {
    	return getLocalizedCategoryFilterName(null,categoryId,maxLength);
    }
    
    /**
     * @param operation Operation
     * @param categoryId Category
	 * @param maxLength Maximum length
     * @return Localized category filter name (abbreviated long name if it is a category)
     */
    public String getLocalizedCategoryFilterName(Operation operation,Long categoryId,int maxLength)
    {
    	String localizedCategoryFilterName="";
    	if (specialCategoryFiltersMap.containsKey(categoryId))
    	{
    		Category categoryFilter=new Category();
    		categoryFilter.setId(categoryId);
    		categoryFilter.setName(specialCategoryFiltersMap.get(categoryId).name);
    		localizedCategoryFilterName=getSpecialCategoryFilterName(categoryFilter);
    	}
    	else if (categoryId>0L)
    	{
    		localizedCategoryFilterName=
    			getLocalizedCategoryLongName(getCurrentUserOperation(operation),categoryId,maxLength);
    	}
    	return localizedCategoryFilterName;
    }
    
    /*
	 * @param operation Operation
	 * @param specialCategoryFilter Special category that represents an special category filter
	 * @return Localized special category filter's name (including question's author nick if needed)
    /*
	private String getSpecialCategoryFilterName(Operation operation,Category specialCategoryFilter)
    */
    
    /**
	 * @param copyright Copyright
	 * @return Short version of localized copyright
	 */
	public String getLocalizedCopyrightShort(Copyright copyright)
	{
		return getLocalizedCopyrightShort(null,copyright);
	}
	
    /**
     * @param operation Operation
	 * @param copyright Copyright
	 * @return Short version of localized copyright
	 */
	private String getLocalizedCopyrightShort(Operation operation,Copyright copyright)
	{
		return copyrightsService.getLocalizedCopyrightShort(getCurrentUserOperation(operation),copyright.getId());
	}
    
	/**
	 * Ajax listener.<br/><br/>
	 * I have defined a tab change listener for accordion of the feedback tab of the question and that accordion 
	 * is inside the question's tabview.<br/><br/>
	 * Curiosly when I change the question's tab, Primefaces fires the listener defined for the accordion but 
	 * calls it with an AjaxBehaviourEvent argument.<br/><br/>
	 * As this listener is called unintentionally we have defined it only to avoid an error message and 
	 * it does nothing.
	 * @param event Ajax event
	 */
	public void changeFeedbackTab(AjaxBehaviorEvent event)
	{
	}
	
	/**
	 * Tab change listener for displaying other tab within accordion of the feedback tab of the question.
	 * @param event Tab change event
	 */
	public void changeFeedbackTab(TabChangeEvent event)
	{
		activeFeedbackTabIndex=Integer.parseInt(((AccordionPanel)event.getComponent()).getActiveIndex());
		
		updateFeedbackResourcesImages(false);
	}
	
	/**
     * @return true if it is allowed to re-sort feedbacks (feedbacks>=2), otherwise false
	 */
	public boolean isEnabledReSortFeedbacks()
	{
		return getQuestion().getFeedbacks().size()>1;
	}
	
	/**
	 * Action listener to show the dialog to re-sort feedbacks.
	 * @param event Action event
	 */
	public void showReSortFeedbacks(ActionEvent event)
	{
		updateAdvancedFeedbacksResourcesImages();
		
		setFeedbacksSorting(null);
		RequestContext rq=RequestContext.getCurrentInstance();
		rq.execute("resortFeedbacksDialog.show()");
	}
	
	/**
	 * Action listener to apply changes from the dialog for re-sorting feedbacks if we accept that dialog.
	 * @param event Action event
	 */
	public void acceptReSortFeedbacks(ActionEvent event)
	{
		for (int feedbackPos=1;feedbackPos<=feedbacksSorting.size();feedbackPos++)
		{
			FeedbackBean feedback=feedbacksSorting.get(feedbackPos-1);
			feedback.setPosition(feedbackPos);
		}
		setFeedbacks(feedbacksSorting);
		
		updateIncorrectFeedbackResourcesImages();
	}
	
    /**
	 * Action listener for updating resources information if we cancel the changes within the dialog for 
	 * re-sorting feedbacks. 
     * @param event Action event
     */
	public void cancelReSortFeedbacks(ActionEvent event)
	{
		updateIncorrectFeedbackResourcesImages();
	}
	
	private int findNewConditionIndex(String type)
	{
		int conditionIndex=-1;
		int i=0;
		for (ConditionBean condition:getCurrentFeedback().getConditionsSorted())
		{
			if (type.equals(TestConditionBean.TYPE) && condition instanceof TestConditionBean)
			{
				conditionIndex=i;
			}
			else if (type.equals(AnswerConditionBean.TYPE) && condition instanceof AnswerConditionBean)
			{
				conditionIndex=i;
			}
			else if (type.equals(AttemptsConditionBean.TYPE) && condition instanceof AttemptsConditionBean)
			{
				conditionIndex=i;
			}
			else if (type.equals(SelectedAnswersConditionBean.TYPE) && 
				condition instanceof SelectedAnswersConditionBean)
			{
				conditionIndex=i;
			}
			else if (type.equals(SelectedRightAnswersConditionBean.TYPE) && 
				condition instanceof SelectedRightAnswersConditionBean)
			{
				conditionIndex=i;
			}
			else if (type.equals(SelectedWrongAnswersConditionBean.TYPE) && 
				condition instanceof SelectedWrongAnswersConditionBean)
			{
				conditionIndex=i;
			}
			else if (type.equals(UnselectedAnswersConditionBean.TYPE) && 
				condition instanceof UnselectedAnswersConditionBean)
			{
				conditionIndex=i;
			}
			else if (type.equals(UnselectedRightAnswersConditionBean.TYPE) && 
				condition instanceof UnselectedRightAnswersConditionBean)
			{
				conditionIndex=i;
			}
			else if (type.equals(UnselectedWrongAnswersConditionBean.TYPE) && 
				condition instanceof UnselectedWrongAnswersConditionBean)
			{
				conditionIndex=i;
			}
			else if (type.equals(RightDistanceConditionBean.TYPE) && 
				condition instanceof RightDistanceConditionBean)
			{
				conditionIndex=i;
			}
			if (conditionIndex!=-1)
			{
				if (!type.equals(AnswerConditionBean.TYPE))
				{
					break;
				}
				else if (!(condition instanceof AnswerConditionBean))
				{
					break;
				}
			}
			i++;
		}
		return conditionIndex;
	}
	
	/**
	 * Action listener for adding a new condition to a feedback.
	 * @param event Action event
	 */
	public void addCondition(ActionEvent event)
	{
		String conditionType=getConditionType();
		if (TestConditionBean.TYPE.equals(conditionType))
		{
			TestConditionBean testCondition=new TestConditionBean();
			getCurrentFeedback().getConditions().add(testCondition);
			setConditionType(null);
			activeConditionIndex=findNewConditionIndex(TestConditionBean.TYPE);
		}
		else if (AnswerConditionBean.TYPE.equals(conditionType))
		{
			AnswerConditionBean answerCondition=new AnswerConditionBean();
			SingleAnswerConditionBean singleAnswerCondition=null;
			Question question=getQuestion();
			if (question instanceof MultichoiceQuestion)
			{
				singleAnswerCondition=new SingleAnswerConditionBean(question,1);
			}
			else if (question instanceof DragDropQuestion)
			{
				//TODO de momento solo tenemos en cuenta el grupo 1, habra que cambiarlo mas adelante
				singleAnswerCondition=new SingleDragDropAnswerConditionBean(question,1,1);
			}
			if (singleAnswerCondition!=null)
			{
				answerCondition.getSingleAnswerConditions().add(singleAnswerCondition);
			}
			getCurrentFeedback().getConditions().add(answerCondition);
			activeConditionIndex=findNewConditionIndex(AnswerConditionBean.TYPE);
		}
		else if (AttemptsConditionBean.TYPE.equals(conditionType))
		{
			AttemptsConditionBean attemptsCondition=null;
			String attemptsGen=localizationService.getLocalizedMessage("CONDITION_TYPE_ATTEMPTS_GEN");
			if ("M".equals(attemptsGen))
			{
				attemptsCondition=new AttemptsConditionBean(false);
			}
			else if ("F".equals(attemptsGen))
			{
				attemptsCondition=new AttemptsConditionBean(true);
			}
			else
			{
				attemptsCondition=new AttemptsConditionBean();				
			}
			getCurrentFeedback().getConditions().add(attemptsCondition);
			setConditionType(null);
			activeConditionIndex=findNewConditionIndex(AttemptsConditionBean.TYPE);
		}
		else if (SelectedAnswersConditionBean.TYPE.equals(conditionType))
		{
			SelectedAnswersConditionBean selectedAnswersCondition=null;
			String selectedAnswersGen=
				localizationService.getLocalizedMessage("CONDITION_TYPE_SELECTED_ANSWERS_GEN");
			if ("M".equals(selectedAnswersGen))
			{
				selectedAnswersCondition=new SelectedAnswersConditionBean(this,false);
			}
			else if ("F".equals(selectedAnswersGen))
			{
				selectedAnswersCondition=new SelectedAnswersConditionBean(this,true);
			}
			else
			{
				selectedAnswersCondition=new SelectedAnswersConditionBean(this);
			}
			getCurrentFeedback().getConditions().add(selectedAnswersCondition);
			setConditionType(null);
			activeConditionIndex=findNewConditionIndex(SelectedAnswersConditionBean.TYPE);
		}
		else if (SelectedRightAnswersConditionBean.TYPE.equals(conditionType))
		{
			SelectedRightAnswersConditionBean selectedRightAnswersCondition=null;
			String selectedRightAnswersGen=
				localizationService.getLocalizedMessage("CONDITION_TYPE_SELECTED_RIGHT_ANSWERS_GEN");
			if ("M".equals(selectedRightAnswersGen))
			{
				selectedRightAnswersCondition=new SelectedRightAnswersConditionBean(this,false);
			}
			else if ("F".equals(selectedRightAnswersGen))
			{
				selectedRightAnswersCondition=new SelectedRightAnswersConditionBean(this,true);
			}
			else
			{
				selectedRightAnswersCondition=new SelectedRightAnswersConditionBean(this);
			}
			getCurrentFeedback().getConditions().add(selectedRightAnswersCondition);
			setConditionType(null);
			activeConditionIndex=findNewConditionIndex(SelectedRightAnswersConditionBean.TYPE);
			if (getNumberOfSelectableRightAnswers()==0)
			{
				selectedRightAnswersCondition.setSelectedRightAnswersCmp(0);
			}
		}
		else if (SelectedWrongAnswersConditionBean.TYPE.equals(conditionType))
		{
			SelectedWrongAnswersConditionBean selectedWrongAnswersCondition=null;
			String selectedWrongAnswersGen=
				localizationService.getLocalizedMessage("CONDITION_TYPE_SELECTED_WRONG_ANSWERS_GEN");
			if ("M".equals(selectedWrongAnswersGen))
			{
				selectedWrongAnswersCondition=new SelectedWrongAnswersConditionBean(this,false);
			}
			else if ("F".equals(selectedWrongAnswersGen))
			{
				selectedWrongAnswersCondition=new SelectedWrongAnswersConditionBean(this,true);
			}
			else
			{
				selectedWrongAnswersCondition=new SelectedWrongAnswersConditionBean(this);
			}
			getCurrentFeedback().getConditions().add(selectedWrongAnswersCondition);
			setConditionType(null);
			activeConditionIndex=findNewConditionIndex(SelectedWrongAnswersConditionBean.TYPE);
			if (getNumberOfSelectableWrongAnswers()==0)
			{
				selectedWrongAnswersCondition.setSelectedWrongAnswersCmp(0);
			}
		}
		else if (UnselectedAnswersConditionBean.TYPE.equals(conditionType))
		{
			UnselectedAnswersConditionBean unselectedAnswersCondition=null;
			String unselectedAnswersGen=
				localizationService.getLocalizedMessage("CONDITION_TYPE_UNSELECTED_ANSWERS_GEN");
			if ("M".equals(unselectedAnswersGen))
			{
				unselectedAnswersCondition=new UnselectedAnswersConditionBean(this,false);
			}
			else if ("F".equals(unselectedAnswersGen))
			{
				unselectedAnswersCondition=new UnselectedAnswersConditionBean(this,true);
			}
			else
			{
				unselectedAnswersCondition=new UnselectedAnswersConditionBean(this);
			}
			getCurrentFeedback().getConditions().add(unselectedAnswersCondition);
			setConditionType(null);
			activeConditionIndex=findNewConditionIndex(UnselectedAnswersConditionBean.TYPE);
		}
		else if (UnselectedRightAnswersConditionBean.TYPE.equals(conditionType))
		{
			UnselectedRightAnswersConditionBean unselectedRightAnswersCondition=null;
			String unselectedRightAnswersGen=
				localizationService.getLocalizedMessage("CONDITION_TYPE_UNSELECTED_RIGHT_ANSWERS_GEN");
			if ("M".equals(unselectedRightAnswersGen))
			{
				unselectedRightAnswersCondition=new UnselectedRightAnswersConditionBean(this,false);
			}
			else if ("F".equals(unselectedRightAnswersGen))
			{
				unselectedRightAnswersCondition=new UnselectedRightAnswersConditionBean(this,true);
			}
			else
			{
				unselectedRightAnswersCondition=new UnselectedRightAnswersConditionBean(this);
			}
			getCurrentFeedback().getConditions().add(unselectedRightAnswersCondition);
			setConditionType(null);
			activeConditionIndex=findNewConditionIndex(UnselectedRightAnswersConditionBean.TYPE);
			if (getNumberOfSelectableRightAnswers()==0)
			{
				unselectedRightAnswersCondition.setUnselectedRightAnswersCmp(0);
			}
		}
		else if (UnselectedWrongAnswersConditionBean.TYPE.equals(conditionType))
		{
			UnselectedWrongAnswersConditionBean unselectedWrongAnswersCondition=null;
			String unselectedWrongAnswersGen=
				localizationService.getLocalizedMessage("CONDITION_TYPE_UNSELECTED_WRONG_ANSWERS_GEN");
			if ("M".equals(unselectedWrongAnswersGen))
			{
				unselectedWrongAnswersCondition=new UnselectedWrongAnswersConditionBean(this,false);
			}
			else if ("F".equals(unselectedWrongAnswersGen))
			{
				unselectedWrongAnswersCondition=new UnselectedWrongAnswersConditionBean(this,true);
			}
			else
			{
				unselectedWrongAnswersCondition=new UnselectedWrongAnswersConditionBean(this);
			}
			getCurrentFeedback().getConditions().add(unselectedWrongAnswersCondition);
			setConditionType(null);
			activeConditionIndex=findNewConditionIndex(UnselectedWrongAnswersConditionBean.TYPE);
			if (getNumberOfSelectableRightAnswers()==0)
			{
				unselectedWrongAnswersCondition.setUnselectedWrongAnswersCmp(0);
			}
		}
		else if (RightDistanceConditionBean.TYPE.equals(conditionType))
		{
			RightDistanceConditionBean rightDistanceCondition=null;
			String rightDistanceGen=localizationService.getLocalizedMessage("CONDITION_TYPE_RIGHT_DISTANCE_GEN");
			if ("M".equals(rightDistanceGen))
			{
				rightDistanceCondition=new RightDistanceConditionBean(this,false);
			}
			else if ("F".equals(rightDistanceGen))
			{
				rightDistanceCondition=new RightDistanceConditionBean(this,true);
			}
			else
			{
				rightDistanceCondition=new RightDistanceConditionBean(this);
			}
			getCurrentFeedback().getConditions().add(rightDistanceCondition);
			setConditionType(null);
			activeConditionIndex=findNewConditionIndex(RightDistanceConditionBean.TYPE);
		}
		// Select new tab (except if it is the first tab because in that case it is opened by default)
		if (activeConditionIndex!=-1)
		{
			AccordionPanel conditionsAccordion=
				(AccordionPanel)event.getComponent().findComponent(":feedbackDialogForm:conditionsAccordion");
			if (conditionsAccordion==null)
			{
				System.out.println("conditionsAccordion null");
			}
			conditionsAccordion.setActiveIndex(Integer.toString(activeConditionIndex));
		}
	}
	
	/**
	 * Action listener for deleting a condition from a feedback.
	 * @param event
	 */
	public void deleteCondition(ActionEvent event)
	{
		ConditionBean condition=(ConditionBean)event.getComponent().getAttributes().get("condition");
		getCurrentFeedback().getConditions().remove(condition);
		
		// If it is needeed change active tab of conditions accordion
		int numConditions=getCurrentFeedback().getConditionsSize();
		if (activeConditionIndex>=numConditions)
		{
			activeConditionIndex=numConditions-1;
			if (activeConditionIndex>=0)
			{
				AccordionPanel conditionsAccordion=(AccordionPanel)event.getComponent().findComponent(
					":feedbackDialogForm:conditionsAccordion");
				conditionsAccordion.setActiveIndex(Integer.toString(activeConditionIndex));
			}
		}
	}
	
	/**
	 * Action listener for adding a new row to a condition of type 'answer'
	 * @param event Action event
	 */
	public void addSingleAnswerCondition(ActionEvent event)
	{
		AnswerConditionBean answerCondition=
			(AnswerConditionBean)event.getComponent().getAttributes().get("condition");
		SingleAnswerConditionBean singleAnswerCondition=null;
		Question question=getQuestion();
		if (question instanceof MultichoiceQuestion)
		{
			singleAnswerCondition=new SingleAnswerConditionBean(question,1);
		}
		else if (question instanceof DragDropQuestion)
		{
			//TODO de momento solo tenemos en cuenta el grupo 1, habra que cambiarlo mas adelante
			singleAnswerCondition=new SingleDragDropAnswerConditionBean(question,1,1);
		}
		if (singleAnswerCondition!=null)
		{
			answerCondition.getSingleAnswerConditions().add(singleAnswerCondition);
			
			// We need to set wrapped answers conditions to null to refresh the conditions on view
			answerCondition.setSingleAnswerConditionsWrapped(null);
		}
	}
	
	/**
	 * Action listener for deleting a row from a condition of type 'answer'
	 * @param event Action event
	 */
	public void deleteSingleAnswerCondition(ActionEvent event)
	{
		AnswerConditionBean answerCondition=
			(AnswerConditionBean)event.getComponent().getAttributes().get("condition");
		SingleAnswerConditionBean singleAnswerCondition=
			(SingleAnswerConditionBean)event.getComponent().getAttributes().get("singleanswercondition");
		answerCondition.getSingleAnswerConditions().remove(singleAnswerCondition);
		
		// We need to set wrapped answers conditions to null to refresh the conditions on view
		answerCondition.setSingleAnswerConditionsWrapped(null);
	}
	
	/**
	 * Checks if deleting an answer will affect to the answer conditions of a feedback.
	 * @param feedback Feedback
	 * @param deletePosition Position of answer to delete
	 * @return true if deleting an answer won't affect to the answer conditions of the feedback, false otherwise
	 */
	private boolean checkAnswerConditions(FeedbackBean feedback,int deletePosition)
	{
		return checkOrUpdateAnswerConditions(feedback,deletePosition,false);
	}
	
	/**
	 * Update answer conditions of a feedback related to the deleted answer if needed.
	 * @param feedback Feedback
	 * @param deletePosition Position of deleted answer
	 */
	private void updateAnswerConditions(FeedbackBean feedback,int deletePosition)
	{
		checkOrUpdateAnswerConditions(feedback,deletePosition,true);
	}
	
	/**
	 * Check or update answer conditions of a feedback related a to an answer to delete.
	 * @param feedback Feedback
	 * @param deletePosition Position of answer to delete or position of deleted answer
	 * @param update true for updating, false for checking without updating
	 * @return when checking returns true if deleting an answer won't affect to the answer conditions of the 
	 * feedback, false otherwise; when updating always returns true
	 */
	private boolean checkOrUpdateAnswerConditions(FeedbackBean feedback,int deletePosition,boolean update)
	{
		boolean ok=true;
		if (update)
		{
			List<AnswerConditionBean> answerConditionsToRemove=new ArrayList<AnswerConditionBean>();
			for (AnswerConditionBean answerCondition:feedback.getAnswerConditions())
			{
				List<SingleAnswerConditionBean> singleAnswersConditionsToRemove=
					new ArrayList<SingleAnswerConditionBean>();
				for (SingleAnswerConditionBean singleAnswerCondition:answerCondition.getSingleAnswerConditions())
				{
					int answerPos=singleAnswerCondition.getAnswerPosition();
					if (answerPos>deletePosition)
					{
						singleAnswerCondition.setAnswerPosition(answerPos-1);
					}
					else if (answerPos==deletePosition)
					{
						singleAnswersConditionsToRemove.add(singleAnswerCondition);
					}
				}
				List<SingleAnswerConditionBean> singleAnswerConditions=
					answerCondition.getSingleAnswerConditions();
				for (SingleAnswerConditionBean singleAnswerConditionToRemove:singleAnswersConditionsToRemove)
				{
					singleAnswerConditions.remove(singleAnswerConditionToRemove);
				}
				if (singleAnswerConditions.isEmpty())
				{
					answerConditionsToRemove.add(answerCondition);
				}
			}
			for (AnswerConditionBean answerConditionToRemove:answerConditionsToRemove)
			{
				feedback.getConditions().remove(answerConditionToRemove);
			}
		}
		else
		{
			for (AnswerConditionBean answerCondition:feedback.getAnswerConditions())
			{
				for (SingleAnswerConditionBean singleAnswerCondition:answerCondition.getSingleAnswerConditions())
				{
					if (singleAnswerCondition.getAnswerPosition()==deletePosition)
					{
						ok=false;
						break;
					}
				}
				if (!ok)
				{
					break;
				}
			}
		}
		return ok;
	}
	
	/**
	 * Checks if deleting a draggable item will affect to the answer conditions of a feedback in a "Drag & Drop"
	 * question.
	 * @param feedback Feedback
	 * @param group Group of draggable item to delete
	 * @param deletePosition Position of draggable item to delete
	 * @return true if deleting a draggable item won't affect to the answer conditions of the feedback 
	 * in a "Drag & Drop" question, false otherwise
	 */
	private boolean checkAnswerConditionsForDeletingDraggableItem(FeedbackBean feedback,int group,
		int deletePosition)
	{
		return checkOrUpdateAnswerConditionsForDeletingDraggableItem(feedback,group,deletePosition,false);
	}
	
	/**
	 * Update answer conditions of a feedback related to the deleted draggable item in a "Drag & Drop" question 
	 * if needed.
	 * @param feedback Feedback
	 * @param group Group of deleted draggable item
	 * @param deletePosition Position of deleted draggable item
	 */
	private void updateAnswerConditionsForDeletingDraggableItem(FeedbackBean feedback,int group,int deletePosition)
	{
		checkOrUpdateAnswerConditionsForDeletingDraggableItem(feedback,group,deletePosition,true);
	}
	
	/**
	 * Check or update answer conditions of a feedback related a to a draggable item to delete in a "Drag & Drop"
	 * question.
	 * @param feedback Feedback
	 * @param group Group of draggable item to delete or group of deleted draggable item
	 * @param deletePosition Position of draggable item to delete or position of deleted draggable item
	 * @param update true for updating, false for checking without updating
	 * @return when checking returns true if deleting a draggable item won't affect to the answer conditions 
	 * of the feedback in a "Drag & Drop" question, false otherwise; when updating always returns true
	 */
	private boolean checkOrUpdateAnswerConditionsForDeletingDraggableItem(FeedbackBean feedback,int group,
		int deletePosition,boolean update)
	{
		boolean ok=true;
		if (update)
		{
			List<AnswerConditionBean> answerConditionsToRemove=new ArrayList<AnswerConditionBean>();
			for (AnswerConditionBean answerCondition:feedback.getAnswerConditions())
			{
				List<SingleAnswerConditionBean> singleAnswersConditionsToRemove=
					new ArrayList<SingleAnswerConditionBean>();
				for (SingleAnswerConditionBean singleAnswerCondition:answerCondition.getSingleAnswerConditions())
				{
					SingleDragDropAnswerConditionBean singleDragDropAnswerCondition=
						(SingleDragDropAnswerConditionBean)singleAnswerCondition;
					if (singleDragDropAnswerCondition.getGroup()==group)
					{
						int rightAnswerPos=singleDragDropAnswerCondition.getRightAnswerPosition();
						if (rightAnswerPos>deletePosition)
						{
							singleDragDropAnswerCondition.setRightAnswerPosition(rightAnswerPos-1);
						}
						else if (rightAnswerPos==deletePosition)
						{
							singleAnswersConditionsToRemove.add(singleAnswerCondition);
						}
					}
				}
				List<SingleAnswerConditionBean> singleAnswerConditions=answerCondition.getSingleAnswerConditions();
				for (SingleAnswerConditionBean singleAnswerConditionToRemove:singleAnswersConditionsToRemove)
				{
					singleAnswerConditions.remove(singleAnswerConditionToRemove);
				}
				if (singleAnswerConditions.isEmpty())
				{
					answerConditionsToRemove.add(answerCondition);
				}
			}
			for (AnswerConditionBean answerConditionToRemove:answerConditionsToRemove)
			{
				feedback.getConditions().remove(answerConditionToRemove);
			}
		}
		else
		{
			for (AnswerConditionBean answerCondition:feedback.getAnswerConditions())
			{
				for (SingleAnswerConditionBean singleAnswerCondition:answerCondition.getSingleAnswerConditions())
				{
					SingleDragDropAnswerConditionBean singleDragDropAnswerCondition=
						(SingleDragDropAnswerConditionBean)singleAnswerCondition;
					if (singleDragDropAnswerCondition.getGroup()==group && 
						singleDragDropAnswerCondition.getRightAnswerPosition()==deletePosition)
					{
						ok=false;
						break;
					}
				}
				if (!ok)
				{
					break;
				}
			}
		}
		return ok;
	}
	
	/**
	 * Checks if deleting an answer will affect to the answer conditions of a feedback in a "Drag & Drop" question.
	 * @param feedback Feedback
	 * @param group Group of answer to delete
	 * @param deletePosition Position of answer to delete
	 * @return true if deleting an answer won't affect to the answer conditions of the feedback in a "Drag & Drop" 
	 * question, false otherwise
	 */
	private boolean checkAnswerConditionsForDeletingDroppableAnswer(FeedbackBean feedback,int group,
		int deletePosition)
	{
		return checkOrUpdateAnswerConditionsForDeletingDroppableAnswer(feedback,group,deletePosition,false);
	}
	
	/**
	 * Update answer conditions of a feedback related to the deleted answer in a "Drag & Drop" question if needed.
	 * @param feedback Feedback
	 * @param group Group of deleted answer
	 * @param deletePosition Position of deleted answer
	 */
	private void updateAnswerConditionsForDeletingDroppableAnswer(FeedbackBean feedback,int group,
		int deletePosition)
	{
		checkOrUpdateAnswerConditionsForDeletingDroppableAnswer(feedback,group,deletePosition,true);
	}
	
	/**
	 * Check or update answer conditions of a feedback related a to an answer to delete in a "Drag & Drop" 
	 * question.
	 * @param feedback Feedback
	 * @param group Group of answer to delete or group of deleted answer
	 * @param deletePosition Position of answer to delete or position of deleted answer
	 * @param update true for updating, false for checking without updating
	 * @return when checking returns true if deleting an answer won't affect to the answer conditions 
	 * of the feedback in a "Drag & Drop" question, false otherwise; when updating always returns true
	 */
	private boolean checkOrUpdateAnswerConditionsForDeletingDroppableAnswer(FeedbackBean feedback,int group,
		int deletePosition,boolean update)
	{
		boolean ok=true;
		if (update)
		{
			List<AnswerConditionBean> answerConditionsToRemove=new ArrayList<AnswerConditionBean>();
			for (AnswerConditionBean answerCondition:feedback.getAnswerConditions())
			{
				List<SingleAnswerConditionBean> singleAnswersConditionsToRemove=
					new ArrayList<SingleAnswerConditionBean>();
				for (SingleAnswerConditionBean singleAnswerCondition:answerCondition.getSingleAnswerConditions())
				{
					SingleDragDropAnswerConditionBean singleDragDropAnswerCondition=
						(SingleDragDropAnswerConditionBean)singleAnswerCondition;
					if (singleDragDropAnswerCondition.getGroup()==group)
					{
						int answerPos=singleDragDropAnswerCondition.getAnswerPosition();
						if (answerPos>deletePosition)
						{
							singleDragDropAnswerCondition.setAnswerPosition(answerPos-1);
						}
						else if (answerPos==deletePosition)
						{
							singleAnswersConditionsToRemove.add(singleAnswerCondition);
						}
					}
				}
				List<SingleAnswerConditionBean> singleAnswerConditions=answerCondition.getSingleAnswerConditions();
				for (SingleAnswerConditionBean singleAnswerConditionToRemove:singleAnswersConditionsToRemove)
				{
					singleAnswerConditions.remove(singleAnswerConditionToRemove);
				}
				if (singleAnswerConditions.isEmpty())
				{
					answerConditionsToRemove.add(answerCondition);
				}
			}
			for (AnswerConditionBean answerConditionToRemove:answerConditionsToRemove)
			{
				feedback.getConditions().remove(answerConditionToRemove);
			}
		}
		else
		{
			for (AnswerConditionBean answerCondition:feedback.getAnswerConditions())
			{
				for (SingleAnswerConditionBean singleAnswerCondition:answerCondition.getSingleAnswerConditions())
				{
					SingleDragDropAnswerConditionBean singleDragDropAnswerCondition=
						(SingleDragDropAnswerConditionBean)singleAnswerCondition;
					if (singleDragDropAnswerCondition.getGroup()==group && 
						singleDragDropAnswerCondition.getAnswerPosition()==deletePosition)
					{
						ok=false;
						break;
					}
				}
				if (!ok)
				{
					break;
				}
			}
		}
		return ok;
	}
	
	/**
	 * Checks if a predicted change on an answer will affect to the selected answers condition of a feedback.
	 * @param feedback Feedback
	 * @param maxValueVariation Expected variation of maximum value for selected answers after changes
	 * @return true if performing the change on the answer won't affect to the selected answers condition of the 
	 * feedback, false otherwise
	 */
	private boolean checkSelectedAnswersCondition(FeedbackBean feedback,int maxValueVariation)
	{
		return checkOrUpdateSelectedAnswersCondition(feedback,maxValueVariation,false);
	}
	
	/**
	 * Updates selected answers condition of a feedback if needed after performing a change. 
	 * @param feedback Feedback
	 * @param maxValueVariation Expected variation of maximum value for selected answers
	 */
	private void updateSelectedAnswersCondition(FeedbackBean feedback,int maxValueVariation)
	{
		checkOrUpdateSelectedAnswersCondition(feedback,maxValueVariation,true);
	}
	
	/**
	 * Checks or updates selected answers condition of a feedback if needed to performing a change. 
	 * @param feedback Feedback
	 * @param maxValueVariation Expected variation of maximum value for selected answers
	 * @param update true for updating, false for checking without updating
	 * @return when checking returns true if performing the change on the answer won't affect to the selected 
	 * answers condition of the feedback, false otherwise; when updating always returns true
	 */
	private boolean checkOrUpdateSelectedAnswersCondition(FeedbackBean feedback,int maxValueVariation,
		boolean update)
	{
		boolean ok=true;
		SelectedAnswersConditionBean selectedAnswersCondition=feedback.getSelectedAnswersCondition();
		if (selectedAnswersCondition!=null)
		{
			if (NumberComparator.compareU(selectedAnswersCondition.getComparator(),NumberComparator.BETWEEN))
			{
				int selectedAnswersBetweenMax=selectedAnswersCondition.getSelectedAnswersBetweenMax();
				int maxValue=selectedAnswersCondition.getMaxSelectedAnswersValue()+maxValueVariation;
				if (selectedAnswersBetweenMax>maxValue)
				{
					if (update)
					{
						int selectedAnswersBetweenMin=selectedAnswersCondition.getSelectedAnswersBetweenMin();
						selectedAnswersCondition.setSelectedAnswersBetweenMax(maxValue);
						if (selectedAnswersBetweenMin>maxValue)
						{
							selectedAnswersCondition.setSelectedAnswersBetweenMin(maxValue);
						}
					}
					else
					{
						ok=false;
					}
				}
			}
			else
			{
				int selectedAnswersCmp=selectedAnswersCondition.getSelectedAnswersCmp();
				int maxValue=selectedAnswersCondition.getMaxValueSelectedAnswersCmp()+maxValueVariation;
				if (selectedAnswersCmp>maxValue)
				{
					if (update)
					{
						selectedAnswersCondition.setSelectedAnswersCmp(maxValue);
					}
					else
					{
						ok=false;
					}
				}
				else if (update && selectedAnswersCmp==0 && maxValue==0 && 
					NumberComparator.compareU(selectedAnswersCondition.getComparator(),NumberComparator.GREATER))
				{
					StringBuffer newComparator=new StringBuffer(NumberComparator.GREATER_EQUAL);
					if ("F".equals(localizationService.getLocalizedMessage("CONDITION_TYPE_SELECTED_ANSWERS_GEN")))
					{
						newComparator.append("_F");
					}
					selectedAnswersCondition.setNewComparator(newComparator.toString());
				}
			}
		}
		return ok;
	}
	
	/**
	 * Checks if a predicted change on an answer will affect to the selected right answers condition 
	 * of a feedback.
	 * @param feedback Feedback
	 * @param maxValueVariation Expected variation of maximum value for selected right answers after changes
	 * @return true if performing the change on the answer won't affect to the selected right answers condition 
	 * of the feedback, false otherwise
	 */
	private boolean checkSelectedRightAnswersCondition(FeedbackBean feedback,int maxValueVariation)
	{
		return checkOrUpdateSelectedRightAnswersCondition(feedback,maxValueVariation,false);
	}
	
	/**
	 * Updates selected right answers condition of a feedback if needed after performing a change. 
	 * @param feedback Feedback
	 * @param maxValueVariation Expected variation of maximum value for selected right answers
	 */
	private void updateSelectedRightAnswersCondition(FeedbackBean feedback,int maxValueVariation)
	{
		checkOrUpdateSelectedRightAnswersCondition(feedback,maxValueVariation,true);
	}
	
	/**
	 * Checks or updates selected right answers condition of a feedback if needed to perform a change. 
	 * @param feedback Feedback
	 * @param maxValueVariation Expected variation of maximum value for selected right answers
	 * @param update true for updating, false for checking without updating
	 * @return when checking returns true if performing the change on the answer won't affect to the selected 
	 * right answers condition of the feedback, false otherwise; when updating always returns true
	 */
	private boolean checkOrUpdateSelectedRightAnswersCondition(FeedbackBean feedback,int maxValueVariation,
		boolean update)
	{
		boolean ok=true;
		SelectedRightAnswersConditionBean selectedRightAnswersCondition=
			feedback.getSelectedRightAnswersCondition();
		if (selectedRightAnswersCondition!=null)
		{
			if (NumberComparator.compareU(
				selectedRightAnswersCondition.getComparator(),NumberComparator.BETWEEN))
			{
				int selectedRightAnswersBetweenMax=
					selectedRightAnswersCondition.getSelectedRightAnswersBetweenMax();
				int maxValue=selectedRightAnswersCondition.getMaxSelectedRightAnswersValue()+maxValueVariation;
				if (selectedRightAnswersBetweenMax>maxValue)
				{
					if (update)
					{
						int selectedRightAnswersBetweenMin=
							selectedRightAnswersCondition.getSelectedRightAnswersBetweenMin();
						selectedRightAnswersCondition.setSelectedRightAnswersBetweenMax(maxValue);
						if (selectedRightAnswersBetweenMin>maxValue)
						{
							selectedRightAnswersCondition.setSelectedRightAnswersBetweenMin(maxValue);
						}
					}
					else
					{
						ok=false;
					}
				}
			}
			else
			{
				int selectedRightAnswersCmp=selectedRightAnswersCondition.getSelectedRightAnswersCmp();
				int maxValue=selectedRightAnswersCondition.getMaxValueSelectedRightAnswersCmp()+maxValueVariation;
				if (selectedRightAnswersCmp>maxValue)
				{
					if (update)
					{
						selectedRightAnswersCondition.setSelectedRightAnswersCmp(maxValue);
					}
					else
					{
						ok=false;
					}
				}
				else if (update && selectedRightAnswersCmp==0 && maxValue==0 && NumberComparator.compareU(
					selectedRightAnswersCondition.getComparator(),NumberComparator.GREATER))
				{
					StringBuffer newComparator=new StringBuffer(NumberComparator.GREATER_EQUAL);
					if ("F".equals(
						localizationService.getLocalizedMessage("CONDITION_TYPE_SELECTED_RIGHT_ANSWERS_GEN")))
					{
						newComparator.append("_F");
					}
					selectedRightAnswersCondition.setNewComparator(newComparator.toString());
				}
			}
		}
		return ok;
	}
	
	/**
	 * Checks if a predicted change on an answer will affect to the selected wrong answers condition of a feedback.
	 * @param feedback Feedback
	 * @param maxValueVariation Expected variation of maximum value for selected wrong answers after changes
	 * @return true if performing the change on the answer won't affect to the selected wrong answers condition 
	 * of the feedback, false otherwise
	 */
	private boolean checkSelectedWrongAnswersCondition(FeedbackBean feedback,int maxValueVariation)
	{
		return checkOrUpdateSelectedWrongAnswersCondition(feedback,maxValueVariation,false);
	}
	
	/**
	 * Updates selected wrong answers condition of a feedback if needed after performing a change. 
	 * @param feedback Feedback
	 * @param maxValueVariation Expected variation of maximum value for selected wrong answers
	 */
	private void updateSelectedWrongAnswersCondition(FeedbackBean feedback,int maxValueVariation)
	{
		checkOrUpdateSelectedWrongAnswersCondition(feedback,maxValueVariation,true);
	}
	
	/**
	 * Checks or updates selected wrong answers condition of a feedback if needed to perform a change. 
	 * @param feedback Feedback
	 * @param maxValueVariation Expected variation of maximum value for selected wrong answers
	 * @param update true for updating, false for checking without updating
	 * @return when checking returns true if performing the change on the answer won't affect to the selected 
	 * wrong answers condition of the feedback, false otherwise; when updating always returns true
	 */
	private boolean checkOrUpdateSelectedWrongAnswersCondition(FeedbackBean feedback,int maxValueVariation,
		boolean update)
	{
		boolean ok=true;
		SelectedWrongAnswersConditionBean selectedWrongAnswersCondition=
			feedback.getSelectedWrongAnswersCondition();
		if (selectedWrongAnswersCondition!=null)
		{
			if (NumberComparator.compareU(selectedWrongAnswersCondition.getComparator(),NumberComparator.BETWEEN))
			{
				int selectedWrongAnswersBetweenMax=
					selectedWrongAnswersCondition.getSelectedWrongAnswersBetweenMax();
				int maxValue=selectedWrongAnswersCondition.getMaxSelectedWrongAnswersValue()+maxValueVariation;
				if (selectedWrongAnswersBetweenMax>maxValue)
				{
					if (update)
					{
						int selectedWrongAnswersBetweenMin=
							selectedWrongAnswersCondition.getSelectedWrongAnswersBetweenMin();
						selectedWrongAnswersCondition.setSelectedWrongAnswersBetweenMax(maxValue);
						if (selectedWrongAnswersBetweenMin>maxValue)
						{
							selectedWrongAnswersCondition.setSelectedWrongAnswersBetweenMin(maxValue);
						}
					}
					else
					{
						ok=false;
					}
				}
			}
			else
			{
				int selectedWrongAnswersCmp=selectedWrongAnswersCondition.getSelectedWrongAnswersCmp();
				int maxValue=selectedWrongAnswersCondition.getMaxValueSelectedWrongAnswersCmp()+maxValueVariation;
				if (selectedWrongAnswersCmp>maxValue)
				{
					if (update)
					{
						selectedWrongAnswersCondition.setSelectedWrongAnswersCmp(maxValue);
					}
					else
					{
						ok=false;
					}
				}
				else if (update && selectedWrongAnswersCmp==0 && maxValue==0 && NumberComparator.compareU(
					selectedWrongAnswersCondition.getComparator(),NumberComparator.GREATER))
				{
					StringBuffer newComparator=new StringBuffer(NumberComparator.GREATER_EQUAL);
					if ("F".equals(
						localizationService.getLocalizedMessage("CONDITION_TYPE_SELECTED_WRONG_ANSWERS_GEN")))
					{
						newComparator.append("_F");
					}
					selectedWrongAnswersCondition.setNewComparator(newComparator.toString());
				}
			}
		}
		return ok;
	}
	
	/**
	 * Checks if a predicted change on an answer will affect to the unselected answers condition of a feedback.
	 * @param feedback Feedback
	 * @param maxValueVariation Expected variation of maximum value for unselected answers after changes
	 * @return true if performing the change on the answer won't affect to the unselected answers condition 
	 * of the feedback, false otherwise
	 */
	private boolean checkUnselectedAnswersCondition(FeedbackBean feedback,int maxValueVariation)
	{
		return checkOrUpdateUnselectedAnswersCondition(feedback,maxValueVariation,false);
	}
	
	/**
	 * Updates unselected answers condition of a feedback if needed after performing a change. 
	 * @param feedback Feedback
	 * @param maxValueVariation Expected variation of maximum value for unselected answers
	 */
	private void updateUnselectedAnswersCondition(FeedbackBean feedback,int maxValueVariation)
	{
		checkOrUpdateUnselectedAnswersCondition(feedback,maxValueVariation,true);
	}
	
	/**
	 * Checks or updates unselected answers condition of a feedback if needed to performing a change. 
	 * @param feedback Feedback
	 * @param maxValueVariation Expected variation of maximum value for unselected answers
	 * @param update true for updating, false for checking without updating
	 * @return when checking returns true if performing the change on the answer won't affect to the unselected 
	 * answers condition of the feedback, false otherwise; when updating always returns true
	 */
	private boolean checkOrUpdateUnselectedAnswersCondition(FeedbackBean feedback,int maxValueVariation,
		boolean update)
	{
		boolean ok=true;
		UnselectedAnswersConditionBean unselectedAnswersCondition=feedback.getUnselectedAnswersCondition();
		if (unselectedAnswersCondition!=null)
		{
			if (NumberComparator.compareU(unselectedAnswersCondition.getComparator(),NumberComparator.BETWEEN))
			{
				int unselectedAnswersBetweenMax=unselectedAnswersCondition.getUnselectedAnswersBetweenMax();
				int maxValue=unselectedAnswersCondition.getMaxUnselectedAnswersValue()+maxValueVariation;
				if (unselectedAnswersBetweenMax>maxValue)
				{
					if (update)
					{
						int unselectedAnswersBetweenMin=
							unselectedAnswersCondition.getUnselectedAnswersBetweenMin();
						unselectedAnswersCondition.setUnselectedAnswersBetweenMax(maxValue);
						if (unselectedAnswersBetweenMin>maxValue)
						{
							unselectedAnswersCondition.setUnselectedAnswersBetweenMin(maxValue);
						}
					}
					else
					{
						ok=false;
					}
				}
			}
			else
			{
				int unselectedAnswersCmp=unselectedAnswersCondition.getUnselectedAnswersCmp();
				int maxValue=unselectedAnswersCondition.getMaxValueUnselectedAnswersCmp()+maxValueVariation;
				if (unselectedAnswersCmp>maxValue)
				{
					if (update)
					{
						unselectedAnswersCondition.setUnselectedAnswersCmp(maxValue);
					}
					else
					{
						ok=false;
					}
				}
				else if (update && unselectedAnswersCmp==0 && maxValue==0 && NumberComparator.compareU(
					unselectedAnswersCondition.getComparator(),NumberComparator.GREATER))
				{
					StringBuffer newComparator=new StringBuffer(NumberComparator.GREATER_EQUAL);
					if ("F".equals(
						localizationService.getLocalizedMessage("CONDITION_TYPE_UNSELECTED_ANSWERS_GEN")))
					{
						newComparator.append("_F");
					}
					unselectedAnswersCondition.setNewComparator(newComparator.toString());
				}
			}
		}
		return ok;
	}
	
	/**
	 * Checks if a predicted change on an answer will affect to the unselected right answers condition 
	 * of a feedback.
	 * @param feedback Feedback
	 * @param maxValueVariation Expected variation of maximum value for unselected right answers after changes
	 * @return true if performing the change on the answer won't affect to the unselected right answers 
	 * condition of the feedback, false otherwise
	 */
	private boolean checkUnselectedRightAnswersCondition(FeedbackBean feedback,int maxValueVariation)
	{
		return checkOrUpdateUnselectedRightAnswersCondition(feedback,maxValueVariation,false);
	}
	
	/**
	 * Updates unselected right answers condition of a feedback if needed after performing a change. 
	 * @param feedback Feedback
	 * @param maxValueVariation Expected variation of maximum value for unselected right answers
	 */
	private void updateUnselectedRightAnswersCondition(FeedbackBean feedback,int maxValueVariation)
	{
		checkOrUpdateUnselectedRightAnswersCondition(feedback,maxValueVariation,true);
	}
	
	/**
	 * Checks or updates unselected right answers condition of a feedback if needed to perform a change. 
	 * @param feedback Feedback
	 * @param maxValueVariation Expected variation of maximum value for unselected right answers
	 * @param update true for updating, false for checking without updating
	 * @return when checking returns true if performing the change on the answer won't affect to the unselected 
	 * right answers condition of the feedback, false otherwise; when updating always returns true
	 */
	private boolean checkOrUpdateUnselectedRightAnswersCondition(FeedbackBean feedback,
		int maxValueVariation,boolean update)
	{
		boolean ok=true;
		UnselectedRightAnswersConditionBean unselectedRightAnswersCondition=
			feedback.getUnselectedRightAnswersCondition();
		if (unselectedRightAnswersCondition!=null)
		{
			if (NumberComparator.compareU(
				unselectedRightAnswersCondition.getComparator(),NumberComparator.BETWEEN))
			{
				int unselectedRightAnswersBetweenMax=
					unselectedRightAnswersCondition.getUnselectedRightAnswersBetweenMax();
				int maxValue=
					unselectedRightAnswersCondition.getMaxUnselectedRightAnswersValue()+maxValueVariation;
				if (unselectedRightAnswersBetweenMax>maxValue)
				{
					if (update)
					{
						int unselectedRightAnswersBetweenMin=
							unselectedRightAnswersCondition.getUnselectedRightAnswersBetweenMin();
						unselectedRightAnswersCondition.setUnselectedRightAnswersBetweenMax(maxValue);
						if (unselectedRightAnswersBetweenMin>maxValue)
						{
							unselectedRightAnswersCondition.setUnselectedRightAnswersBetweenMin(maxValue);
						}
					}
					else
					{
						ok=false;
					}
				}
			}
			else
			{
				int unselectedRightAnswersCmp=unselectedRightAnswersCondition.getUnselectedRightAnswersCmp();
				int maxValue=
					unselectedRightAnswersCondition.getMaxValueUnselectedRightAnswersCmp()+maxValueVariation;
				if (unselectedRightAnswersCmp>maxValue)
				{
					if (update)
					{
						unselectedRightAnswersCondition.setUnselectedRightAnswersCmp(maxValue);
					}
					else
					{
						ok=false;
					}
				}
				else if (update && unselectedRightAnswersCmp==0 && maxValue==0 && NumberComparator.compareU(
					unselectedRightAnswersCondition.getComparator(),NumberComparator.GREATER))
				{
					StringBuffer newComparator=new StringBuffer(NumberComparator.GREATER_EQUAL);
					if ("F".equals(
						localizationService.getLocalizedMessage("CONDITION_TYPE_UNSELECTED_RIGHT_ANSWERS_GEN")))
					{
						newComparator.append("_F");
					}
					unselectedRightAnswersCondition.setNewComparator(newComparator.toString());
				}
			}
		}
		return ok;
	}
	
	/**
	 * Checks if a predicted change on an answer will affect to the unselected wrong answers condition 
	 * of a feedback.
	 * @param feedback Feedback
	 * @param maxValueVariation Expected variation of maximum value for unselected wrong answers after changes
	 * @return true if performing the change on the answer won't affect to the unselected wrong answers 
	 * condition of the feedback, false otherwise
	 */
	private boolean checkUnselectedWrongAnswersCondition(FeedbackBean feedback,int maxValueVariation)
	{
		return checkOrUpdateUnselectedWrongAnswersCondition(feedback,maxValueVariation,false);
	}
	
	/**
	 * Updates unselected wrong answers condition of a feedback if needed after performing a change. 
	 * @param feedback Feedback
	 * @param maxValueVariation Expected variation of maximum value for unselected wrong answers
	 */
	private void updateUnselectedWrongAnswersCondition(FeedbackBean feedback,int maxValueVariation)
	{
		checkOrUpdateUnselectedWrongAnswersCondition(feedback,maxValueVariation,true);
	}
	
	/**
	 * Checks or updates unselected wrong answers condition of a feedback if needed to perform a change. 
	 * @param feedback Feedback
	 * @param maxValueVariation Expected variation of maximum value for unselected wrong answers
	 * @param update true for updating, false for checking without updating
	 * @return when checking returns true if performing the change on the answer won't affect to the unselected 
	 * wrong answers condition of the feedback, false otherwise; when updating always returns true
	 */
	private boolean checkOrUpdateUnselectedWrongAnswersCondition(FeedbackBean feedback,int maxValueVariation,
		boolean update)
	{
		boolean ok=true;
		UnselectedWrongAnswersConditionBean unselectedWrongAnswersCondition=
			feedback.getUnselectedWrongAnswersCondition();
		if (unselectedWrongAnswersCondition!=null)
		{
			if (NumberComparator.compareU(
				unselectedWrongAnswersCondition.getComparator(),NumberComparator.BETWEEN))
			{
				int unselectedWrongAnswersBetweenMax=
					unselectedWrongAnswersCondition.getUnselectedWrongAnswersBetweenMax();
				int maxValue=
					unselectedWrongAnswersCondition.getMaxUnselectedWrongAnswersValue()+maxValueVariation;
				if (unselectedWrongAnswersBetweenMax>maxValue)
				{
					if (update)
					{
						int unselectedWrongAnswersBetweenMin=
							unselectedWrongAnswersCondition.getUnselectedWrongAnswersBetweenMin();
						unselectedWrongAnswersCondition.setUnselectedWrongAnswersBetweenMax(maxValue);
						if (unselectedWrongAnswersBetweenMin>maxValue)
						{
							unselectedWrongAnswersCondition.setUnselectedWrongAnswersBetweenMin(maxValue);
						}
					}
					else
					{
						ok=false;
					}
				}
			}
			else
			{
				int unselectedWrongAnswersCmp=unselectedWrongAnswersCondition.getUnselectedWrongAnswersCmp();
				int maxValue=
					unselectedWrongAnswersCondition.getMaxValueUnselectedWrongAnswersCmp()+maxValueVariation;
				if (unselectedWrongAnswersCmp>maxValue)
				{
					if (update)
					{
						unselectedWrongAnswersCondition.setUnselectedWrongAnswersCmp(maxValue);
					}
					else
					{
						ok=false;
					}
				}
				else if (update && unselectedWrongAnswersCmp==0 && maxValue==0 && NumberComparator.compareU(
					unselectedWrongAnswersCondition.getComparator(),NumberComparator.GREATER))
				{
					StringBuffer newComparator=new StringBuffer(NumberComparator.GREATER_EQUAL);
					if ("F".equals(localizationService.getLocalizedMessage(
						"CONDITION_TYPE_UNSELECTED_WRONG_ANSWERS_GEN")))
					{
						newComparator.append("_F");
					}
					unselectedWrongAnswersCondition.setNewComparator(newComparator.toString());
				}
			}
		}
		return ok;
	}
	
	/**
	 * Checks if a predicted change on an answer will affect to the right distance condition of a feedback.
	 * @param feedback Feedback
	 * @param rightVariation Expected variation of value for selectable right answers after changes
	 * @param wrongVariation Expected variation of value for selectable wrong answers after changes
	 * @return true if performing the change on the answer won't affect to the right distance condition of the 
	 * feedback, false otherwise
	 */
	private boolean checkRightDistanceCondition(FeedbackBean feedback,int rightVariation,int wrongVariation)
	{
		return checkOrUpdateRightDistanceCondition(feedback,rightVariation,wrongVariation,false);
	}
	
	/**
	 * Updates right distance condition of a feedback if needed after performing a change. 
	 * @param feedback Feedback
	 * @param rightVariation Expected variation of value for selectable right answers
	 * @param wrongVariation Expected variation of value for selectable wrong answers
	 */
	private void updateRightDistanceCondition(FeedbackBean feedback,int rightVariation,int wrongVariation)
	{
		checkOrUpdateRightDistanceCondition(feedback,rightVariation,wrongVariation,true);
	}
	
	/**
	 * Checks or updates right distance condition of a feedback if needed to performing a change. 
	 * @param feedback Feedback
	 * @param rightVariation Expected variation of value for selectable right answers
	 * @param wrongVariation Expected variation of value for selectable wrong answers
	 * @param update true for updating, false for checking without updating
	 * @return when checking returns true if performing the change on the answer won't affect 
	 * to the right distance 
	 * condition of the feedback, false otherwise; when updating always returns true
	 */
	private boolean checkOrUpdateRightDistanceCondition(FeedbackBean feedback,int rightVariation,
		int wrongVariation,boolean update)
	{
		boolean ok=true;
		
		RightDistanceConditionBean rightDistanceCondition=feedback.getRightDistanceCondition();
		if (rightDistanceCondition!=null)
		{
			int selectableRightAnswers=getNumberOfSelectableRightAnswers()+rightVariation;
			int selectableWrongAnswers=getNumberOfSelectableWrongAnswers()+wrongVariation;
			int maxValue=
				selectableRightAnswers>=selectableWrongAnswers?selectableRightAnswers:selectableWrongAnswers;
			if (NumberComparator.compareU(rightDistanceCondition.getComparator(),NumberComparator.BETWEEN))
			{
				int rightDistanceBetweenMax=rightDistanceCondition.getRightDistanceBetweenMax();
				if (rightDistanceBetweenMax>maxValue)
				{
					if (update)
					{
						int rightDistanceBetweenMin=rightDistanceCondition.getRightDistanceBetweenMin();
						rightDistanceCondition.setRightDistanceBetweenMax(maxValue);
						if (rightDistanceBetweenMin>maxValue)
						{
							rightDistanceCondition.setRightDistanceBetweenMin(maxValue);
						}
					}
					else
					{
						ok=false;
					}
				}
			}
			else
			{
				int rightDistanceCmp=rightDistanceCondition.getRightDistanceCmp();
				if (NumberComparator.compareU(rightDistanceCondition.getComparator(),NumberComparator.GREATER))
				{
					maxValue--;
				}
				else if (NumberComparator.compareU(rightDistanceCondition.getComparator(),NumberComparator.LESS))
				{
					maxValue++;
				}
				if (rightDistanceCmp>maxValue)
				{
					if (update)
					{
						rightDistanceCondition.setRightDistanceCmp(maxValue);
					}
					else
					{
						ok=false;
					}
				}
				else if (update && rightDistanceCmp==0 && maxValue==0 && 
					NumberComparator.compareU(rightDistanceCondition.getComparator(),NumberComparator.GREATER))
				{
					StringBuffer newComparator=new StringBuffer(NumberComparator.GREATER_EQUAL);
					if ("F".equals(localizationService.getLocalizedMessage("CONDITION_TYPE_RIGHT_DISTANCE_GEN")))
					{
						newComparator.append("_F");
					}
					rightDistanceCondition.setNewComparator(newComparator.toString());
				}
			}
		}
		return ok;
	}
	
	/**
	 * Action listener to show the dialog to confirm cancel of question creation/edition.
	 * @param event Action event
	 */
	public void showConfirmCancelQuestionDialog(ActionEvent event)
	{
		setCancelQuestionTarget((String)event.getComponent().getAttributes().get("target"));
		RequestContext rq=RequestContext.getCurrentInstance();
		rq.execute("confirmCancelQuestionDialog.show()");
	}
	
	/**
	 * Cancel question creation/edition and navigate to next view.
	 * @return Next wiew
	 */
	public String cancelQuestion()
	{
		// End current user session Hibernate operation
		userSessionService.endCurrentUserOperation();
		
		StringBuffer nextView=null;
		if ("logout".equals(getCancelQuestionTarget()))
		{
			FacesContext facesContext=FacesContext.getCurrentInstance();
			LoginBean loginBean=null;
			try
			{
				loginBean=(LoginBean)facesContext.getApplication().getELResolver().getValue(
					facesContext.getELContext(),null,"loginBean");
			}
			catch (Exception e)
			{
				loginBean=null;
			}
			if (loginBean!=null)
			{
				nextView=new StringBuffer(loginBean.logout());
			}
		}
		else if (getCancelQuestionTarget()!=null)
		{
			nextView=new StringBuffer(getCancelQuestionTarget());
			nextView.append("?faces-redirect=true");
		}
		return nextView==null?null:nextView.toString();
	}
	
	/**
	 * Cancel question creation/edition and navigate to next view.
	 * @return Next wiew
	 */
	public String cancelEditQuestion()
	{
		// End current user session Hibernate operation
		userSessionService.endCurrentUserOperation();
		
		return "questions?faces-redirect=true";
	}
	
	/**
	 * Action listener for updating resources information if we cancel the dialog to cancel the question 
	 * creation/edition. 
	 * @param event Action event
	 */
	public void abortCancelQuestion(ActionEvent event)
	{
		updateResourcesImages();
	}
	
	/**
	 * Displays an error message.
	 * @param message Error message (before localization)
	 */
	private void addErrorMessage(String message)
	{
		FacesContext context=FacesContext.getCurrentInstance();
		context.addMessage(null,
			new FacesMessage(FacesMessage.SEVERITY_ERROR,localizationService.getLocalizedMessage(message),null));
	}
	
	/**
	 * Displays an error message.
	 * @param message Error message (plain message not needed to localize)
	 */
	private void addPlainErrorMessage(String message)
	{
		FacesContext context=FacesContext.getCurrentInstance();
		context.addMessage(null,new FacesMessage(FacesMessage.SEVERITY_ERROR,message,null));
		
	}
	
	/**
	 * Scroll page to top position.
	 */
	private void scrollToTop()
	{
		RequestContext rq=RequestContext.getCurrentInstance();
		rq.execute("window.scrollTo(0,0)");
	}
	
	/**
	 * Displays the error page with the indicated message.<br/><br/>
	 * Be careful that this method can only be invoked safely from non ajax actions.
	 * @param errorCode Error message (before localization)
	 * @param plainMessage Plain error message (used if it is not possible to localize error message)
	 */
	private void displayErrorPage(String errorCode,String plainMessage)
	{
		FacesContext context=FacesContext.getCurrentInstance();
		ExternalContext externalContext=context.getExternalContext();
		Map<String,Object> requestMap=externalContext.getRequestMap();
		requestMap.put("errorCode",errorCode);
		requestMap.put("plainMessage",plainMessage);
		try
		{
			externalContext.dispatch("/pages/error");
		}
		catch (IOException ioe)
		{
			String errorMessage=null;
			try
			{
				errorMessage=localizationService.getLocalizedMessage(errorCode);
			}
			catch (ServiceException se)
			{
				errorMessage=null;
			}
			if (errorMessage==null)
			{
				errorMessage=plainMessage;
			}
			throw new FacesException(errorMessage,ioe);
		}
	}
	
	/**
	 * Handles a change of the current locale.<br/><br/>
	 * This implementation localize the item 'All' of the 'Category' combo in the filter's panel of the 
	 * 'Select image' dialog because submitting form is not enough to localize it.
	 * @param event Action event
	 */
	public void changeLocale(ActionEvent event)
	{
		FacesContext context=FacesContext.getCurrentInstance();
		LocaleBean localeBean=(LocaleBean)context.getApplication().getELResolver().getValue(
			context.getELContext(),null,"localeBean");
		
		// Change locale code of current view
		UIViewRoot viewRoot=FacesContext.getCurrentInstance().getViewRoot();
		viewRoot.setLocale(new Locale(localeBean.getLangCode()));
		
		// Change 'All' item to the genere of the new locale
		String categoryGen=localizationService.getLocalizedMessage("CATEGORY_GEN");
		String allOptionsForCategory=null;
		if ("M".equals(categoryGen))
		{
			allOptionsForCategory="ALL_OPTIONS";
		}
		else
		{
			allOptionsForCategory="ALL_OPTIONS_F";
		}
		if (!allOptionsForCategory.equals(allCategories.name))
		{
			allCategories.name=allOptionsForCategory;
			Category allOptionsCategoryAux=new Category();
			allOptionsCategoryAux.setId(allCategories.id);
			List<Category> specialCategoriesFilters=getSpecialCategoriesFilters(getCurrentUserOperation(null));
			Category allOptionsCategory=
				specialCategoriesFilters.get(specialCategoriesFilters.indexOf(allOptionsCategoryAux));
			allOptionsCategory.setName(allOptionsForCategory);
		}
	}
}
