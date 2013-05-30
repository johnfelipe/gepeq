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

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIComponent;
import javax.faces.component.UIData;
import javax.faces.component.UIInput;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.AjaxBehaviorEvent;
import javax.servlet.http.HttpServletRequest;

import org.primefaces.component.accordionpanel.AccordionPanel;
import org.primefaces.component.column.Column;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.component.tabview.TabView;
import org.primefaces.component.wizard.Wizard;
import org.primefaces.context.RequestContext;
import org.primefaces.event.DateSelectEvent;
import org.primefaces.event.FlowEvent;
import org.primefaces.event.TabChangeEvent;
import org.primefaces.model.DualListModel;

import es.uned.lsi.gepec.model.QuestionLevel;
import es.uned.lsi.gepec.model.entities.AddressType;
import es.uned.lsi.gepec.model.entities.Assessement;
import es.uned.lsi.gepec.model.entities.Category;
import es.uned.lsi.gepec.model.entities.Evaluator;
import es.uned.lsi.gepec.model.entities.NavLocation;
import es.uned.lsi.gepec.model.entities.Question;
import es.uned.lsi.gepec.model.entities.QuestionOrder;
import es.uned.lsi.gepec.model.entities.QuestionType;
import es.uned.lsi.gepec.model.entities.RedoQuestionValue;
import es.uned.lsi.gepec.model.entities.ScoreType;
import es.uned.lsi.gepec.model.entities.ScoreUnit;
import es.uned.lsi.gepec.model.entities.Section;
import es.uned.lsi.gepec.model.entities.SupportContact;
import es.uned.lsi.gepec.model.entities.Test;
import es.uned.lsi.gepec.model.entities.TestFeedback;
import es.uned.lsi.gepec.model.entities.TestUser;
import es.uned.lsi.gepec.model.entities.User;
import es.uned.lsi.gepec.model.entities.UserType;
import es.uned.lsi.gepec.model.entities.Visibility;
import es.uned.lsi.gepec.om.OmHelper;
import es.uned.lsi.gepec.om.QuestionGenerator;
import es.uned.lsi.gepec.om.TestGenerator;
import es.uned.lsi.gepec.util.EmailValidator;
import es.uned.lsi.gepec.util.StringUtils;
import es.uned.lsi.gepec.util.HibernateUtil.Operation;
import es.uned.lsi.gepec.web.backbeans.EvaluatorBean;
import es.uned.lsi.gepec.web.backbeans.QuestionOrderBean;
import es.uned.lsi.gepec.web.backbeans.SectionBean;
import es.uned.lsi.gepec.web.backbeans.SupportContactBean;
import es.uned.lsi.gepec.web.backbeans.TestFeedbackBean;
import es.uned.lsi.gepec.web.backbeans.TestFeedbackConditionBean;
import es.uned.lsi.gepec.web.helper.NumberComparator;
import es.uned.lsi.gepec.web.services.AddressTypesService;
import es.uned.lsi.gepec.web.services.AssessementsService;
import es.uned.lsi.gepec.web.services.CategoriesService;
import es.uned.lsi.gepec.web.services.CategoryTypesService;
import es.uned.lsi.gepec.web.services.ConfigurationService;
import es.uned.lsi.gepec.web.services.LocalizationService;
import es.uned.lsi.gepec.web.services.NavLocationsService;
import es.uned.lsi.gepec.web.services.PermissionsService;
import es.uned.lsi.gepec.web.services.QuestionTypesService;
import es.uned.lsi.gepec.web.services.QuestionsService;
import es.uned.lsi.gepec.web.services.RedoQuestionValuesService;
import es.uned.lsi.gepec.web.services.ScoreTypesService;
import es.uned.lsi.gepec.web.services.ScoreUnitsService;
import es.uned.lsi.gepec.web.services.ServiceException;
import es.uned.lsi.gepec.web.services.TestUsersService;
import es.uned.lsi.gepec.web.services.TestsService;
import es.uned.lsi.gepec.web.services.UserSessionService;
import es.uned.lsi.gepec.web.services.UserTypesService;
import es.uned.lsi.gepec.web.services.UsersService;
import es.uned.lsi.gepec.web.services.VisibilitiesService;

// Backbean para la vista prueba
// author Víctor Manuel Alonso Rodríguez
// since  12/2011
/**
 * Managed bean for creating/editing a test.
 */
@SuppressWarnings("serial")
@ManagedBean(name="testBean")
@ViewScoped
public class TestBean implements Serializable
{
	private final static int MIN_WEIGHT=1;
	private final static int MAX_WEIGHT=10;
	
	private final static String DATE_HIDDEN_PATTERN="MM-dd-yyyy HH:mm:ss";
	
	private final static class SpecialCategoryFilter
	{
		private long id;
		private String name;
		private List<String> requiredPermissions;
		
		private SpecialCategoryFilter(long id,String name,List<String> requiredPermissions)
		{
			this.id=id;
			this.name=name;
			this.requiredPermissions=requiredPermissions;
		}
		
		@Override
		public boolean equals(Object obj)
		{
			return obj instanceof SpecialCategoryFilter && id==((SpecialCategoryFilter)obj).id;
		}
	}
	
	private final static String GENERAL_WIZARD_TAB="general";
	private final static String PRESENTATION_WIZARD_TAB="presentation";
	private final static String SECTIONS_WIZARD_TAB="sections";
	private final static String PRELIMINARY_SUMMARY_WIZARD_TAB="preliminarySummary";
	private final static String FEEDBACK_WIZARD_TAB="feedback";
	private final static String CONFIRMATION_WIZARD_TAB="confirmation";
	
	private final static int GENERAL_TABVIEW_TAB=0;
	private final static int PRESENTATION_TABVIEW_TAB=1;
	private final static int SECTIONS_TABVIEW_TAB=2;
	private final static int PRELIMINARY_SUMMARY_OR_FEEDBACK_TABVIEW_TAB=3;
	
	//private final static int USERS_TAB_GENERAL_ACCORDION=0;
	private final static int CALENDAR_TAB_GENERAL_ACCORDION=1;
	//private final static int CONFIGURATION_TAB_GENERAL_ACCORDION=2;
	//private final static int ADDRESSES_TAB_GENERAL_ACCORDION=3;
	
	private final static int SUMMARY_TAB_FEEDBACK_ACCORDION=0;
	private final static int SCORES_TAB_FEEDBACK_ACCORDION=1;
	private final static int ADVANCED_FEEDBACK_TAB_FEEDBACK_ACCORDION=2;
	
	private final static int FEEDBACKS_DIALOG_BASE_HEIGHT=330;
	private final static int FEEDBACKS_DIALOG_SECTIONS_COMBO_HEIGHT=50;
	
	private final static int BASE_MARKS_PER_QUESTION=3;
	
	private final static SpecialCategoryFilter ALL_GLOBAL_CATEGORIES;
	static
	{
		List<String> allGlobalCategoriesPermissions=new ArrayList<String>();
		allGlobalCategoriesPermissions.add("PERMISSION_TEST_USE_GLOBAL_QUESTIONS");
		ALL_GLOBAL_CATEGORIES=
			new SpecialCategoryFilter(-4L,"ALL_GLOBAL_CATEGORIES",allGlobalCategoriesPermissions);
	}
	private final static SpecialCategoryFilter ALL_PUBLIC_CATEGORIES_OF_OTHER_USERS; 
	static
	{
		List<String> allPublicCategoriesOfOtherUsersPermissions=new ArrayList<String>();
		allPublicCategoriesOfOtherUsersPermissions.add("PERMISSION_TEST_USE_OTHER_USERS_QUESTIONS");
		ALL_PUBLIC_CATEGORIES_OF_OTHER_USERS=new SpecialCategoryFilter(
			-5L,"ALL_PUBLIC_CATEGORIES_OF_OTHER_USERS",allPublicCategoriesOfOtherUsersPermissions);
	}
	private final static SpecialCategoryFilter ALL_CATEGORIES_OF_OTHER_USERS;
	static
	{
		List<String> allCategoriesOfOtherUsersPermissions=new ArrayList<String>();
		allCategoriesOfOtherUsersPermissions.add("PERMISSION_TEST_USE_OTHER_USERS_QUESTIONS");
		allCategoriesOfOtherUsersPermissions.add(
			"PERMISSION_QUESTIONS_VIEW_QUESTIONS_OF_OTHER_USERS_PRIVATE_CATEGORIES_ENABLED");
		ALL_CATEGORIES_OF_OTHER_USERS=new SpecialCategoryFilter(
			-7L,"ALL_CATEGORIES_OF_OTHER_USERS",allCategoriesOfOtherUsersPermissions);
	}
	
	@ManagedProperty(value="#{localizationService}")
	private LocalizationService localizationService;
	@ManagedProperty(value="#{configurationService}")
	private ConfigurationService configurationService;
	@ManagedProperty(value="#{testsService}")
	private TestsService testsService;
	@ManagedProperty(value="#{questionsService}")
	private QuestionsService questionsService;
	@ManagedProperty(value="#{questionTypesService}")
	private QuestionTypesService questionTypesService;
	@ManagedProperty(value="#{categoriesService}")
	private CategoriesService categoriesService;
	@ManagedProperty(value="#{categoryTypesService}")
	private CategoryTypesService categoryTypesService;
	@ManagedProperty(value="#{visibilitiesService}")
	private VisibilitiesService visibilitiesService;
	@ManagedProperty(value="#{assessementsService}")
	private AssessementsService assessementsService;
	@ManagedProperty(value="#{scoreTypesService}")
	private ScoreTypesService scoreTypesService;
	@ManagedProperty(value="#{navLocationsService}")
	private NavLocationsService navLocationsService;
	@ManagedProperty(value="#{redoQuestionValuesService}")
	private RedoQuestionValuesService redoQuestionValuesService;
	@ManagedProperty(value="#{scoreUnitsService}")
	private ScoreUnitsService scoreUnitsService;
	@ManagedProperty(value="#{testUsersService}")
	private TestUsersService testUsersService;
	@ManagedProperty(value="#{addressTypesService}")
	private AddressTypesService addressTypesService;
	@ManagedProperty(value="#{usersService}")
	private UsersService usersService;
	@ManagedProperty(value="#{userTypesService}")
	private UserTypesService userTypesService;
	@ManagedProperty(value="#{userSessionService}")
	private UserSessionService userSessionService;
	@ManagedProperty(value="#{permissionsService}")
	private PermissionsService permissionsService;
	
	private long testId;
	private String name;
	private User author;
	private Date timeCreated;
	private Date timeTestDeploy;
	private Date timeDeployDeploy;
	private Category category;
	private String description;
	private String title;
	private Assessement assessement;
	private ScoreType scoreType;
	private boolean allUsersAllowed;
	private boolean allowAdminReports;
	private Date startDate;
	private Date closeDate;
	private Date warningDate;
	private Date feedbackDate;
	private boolean freeSummary;
	private boolean freeStop;
	private boolean summaryQuestions;
	private boolean summaryScores;
	private boolean summaryAttempts;
	private boolean navigation;
	private NavLocation navLocation;
	private RedoQuestionValue redoQuestion;
	private boolean redoTest;
	private String presentationTitle;
	private String presentation;
	private String preliminarySummaryTitle;
	private String preliminarySummaryButton;
	private String preliminarySummary;
	private boolean feedbackDisplaySummary;
	private boolean feedbackDisplaySummaryMarks;
	private boolean feedbackDisplaySummaryAttempts;
	private String feedbackSummaryPrevious;
	private boolean feedbackDisplayScores;
	private boolean feedbackDisplayScoresMarks;
	private boolean feedbackDisplayScoresPercentages;
	private String feedbackScoresPrevious;
	private String feedbackAdvancedPrevious;
	private String feedbackAdvancedNext;
	private List<SectionBean> sections;
	private List<TestFeedbackBean> feedbacks;
	private List<SupportContactBean> supportContacts;
	private List<EvaluatorBean> evaluators;
	
	/** UI Helper Properties */
	private boolean restrictDates;
	private boolean restrictFeedbackDate;
	private String startDateHidden;
	private String closeDateHidden;
	private String warningDateHidden;
	private String feedbackDateHidden;
	private SectionBean currentSection;
	private TestFeedbackBean currentFeedback;
	private SupportContactBean currentSuportContact;
	private EvaluatorBean currentEvaluator;
	private SpecialCategoryFilter allCategories;
	private SpecialCategoryFilter allEvenPrivateCategories; 
	private SpecialCategoryFilter allTestAuthorCategories;
	private SpecialCategoryFilter allTestAuthorCategoriesExceptGlobals;
	private SpecialCategoryFilter allPrivateCategories;
	private Map<Long,SpecialCategoryFilter> specialCategoryFiltersMap;
	private long filterCategoryId;
	private boolean filterIncludeSubcategories;
	private String filterQuestionType;
	private String filterQuestionLevel;
	private List<Question> questions;
	private DualListModel<Question> questionsDualList;
	private List<User> filteredUsersForAddingUsers;
	private List<User> filteredUsersForAddingAdmins;
	private List<User> filteredUsersForAddingSupportContactFilterUsers;
	private List<User> filteredUsersForAddingEvaluatorFilterUsers;
	private List<UserType> userTypes;
	private long filterUsersUserTypeId;
	private boolean filterUsersIncludeOmUsers;
	private List<User> testUsers;
	private DualListModel<User> usersDualList;
	private long filterAdminsUserTypeId;
	private boolean filterAdminsIncludeOmUsers;
	private List<User> testAdmins;
	private DualListModel<User> adminsDualList;
	
	private String supportContactFilterType;
	private String supportContactFilterSubtype;
	private List<String> supportContactFilterSubtypes;
	private long filterSupportContactFilterUsersUserTypeId;
	private boolean filterSupportContactFilterUsersIncludeOmUsers;
	private List<User> testSupportContactFilterUsers;
	private String supportContactFilterUsersIdsHidden;
	private DualListModel<User> supportContactFilterUsersDualList;
	
	private String filterSupportContactRangeNameLowerLimit;
	private String filterSupportContactRangeNameUpperLimit;
	private String filterSupportContactRangeSurnameLowerLimit;
	private String filterSupportContactRangeSurnameUpperLimit;
	
	private String evaluatorFilterType;
	private String evaluatorFilterSubtype;
	private List<String> evaluatorFilterSubtypes;
	private long filterEvaluatorFilterUsersUserTypeId;
	private boolean filterEvaluatorFilterUsersIncludeOmUsers;
	private List<User> testEvaluatorFilterUsers;
	private String evaluatorFilterUsersIdsHidden;
	private DualListModel<User> evaluatorFilterUsersDualList;
	
	private String filterEvaluatorRangeNameLowerLimit;
	private String filterEvaluatorRangeNameUpperLimit;
	private String filterEvaluatorRangeSurnameLowerLimit;
	private String filterEvaluatorRangeSurnameUpperLimit;
	
	private List<String> letters;
	
	private String supportContact;
	private boolean supportContactDialogDisplayed;
	private String evaluator;
	private boolean evaluatorDialogDisplayed;
	private boolean enabledCheckboxesSetters;
	private String cancelTestTarget;
	
	private int preliminarySummaryTabviewTab; 
	private int feedbackTabviewTab; 
	
	private String activeTestTabName;
	private int activeTestIndex;
	
	private String nextTestTabNameOnChangePropertyConfirm;
	private int nextTestIndexOnChangePropertyConfirm;
	
	private ScoreType oldScoreType;
	
	// Local list needed to allow sorting of sections
	private List<SectionBean> sectionsSorting;
	
	// Local list needed to allow sorting of feedbacks
	private List<TestFeedbackBean> feedbacksSorting;
	
	private boolean testInitialized;
	
	// General accordion
	private int activeGeneralTabIndex;
	
	// Sections
	private int activeSectionIndex;
	private String activeSectionName;
	private int sectionToRemoveOrder;
	private SectionBean sectionChecked;
	private QuestionOrderBean questionOrderChecked;
	
	private int nextSectionIndexOnChangePropertyConfirm;
	
	// Questions
	private int questionToRemoveSectionOrder;
	private int questionToRemoveOrder;
	
	// Available categories
	private List<Category> testsCategories;
	private List<Category> specialCategoriesFilters;					// Special categories filters list
	private List<Category> questionsCategories;
	
	// Assessements
	private List<Assessement> assessements;
	
	// Score types
	private List<ScoreType> scoreTypes;
	
	// Navigation locations
	private List<NavLocation> navLocations;
	
	// Values for property 'redoQuestion'
	private List<RedoQuestionValue> redoQuestions;
	
	private Boolean globalOtherUserCategoryAllowed;
	private Boolean viewTestsFromOtherUsersPrivateCategoriesEnabled;
	private Boolean viewTestsFromAdminsPrivateCategoriesEnabled;
	private Boolean viewTestsFromSuperadminsPrivateCategoriesEnabled;
	
	private Boolean useGlobalQuestions;
	private Boolean useOtherUsersQuestions;
	private Boolean viewQuestionsFromOtherUsersPrivateCategoriesEnabled;
	private Boolean viewQuestionsFromAdminsPrivateCategoriesEnabled;
	private Boolean viewQuestionsFromSuperadminsPrivateCategoriesEnabled;
	private Boolean testAuthorViewQuestionsFromOtherUsersPrivateCategoriesEnabled;
	private Boolean testAuthorViewQuestionsFromAdminsPrivateCategoriesEnabled;
	private Boolean testAuthorViewQuestionsFromSuperadminsPrivateCategoriesEnabled;
	
	private Boolean viewOMEnabled;
	
	private Map<Long,Boolean> viewOMQuestionsEnabled;
	
	private Map<Long,Boolean> admins;
	private Map<Long,Boolean> superadmins;
	
	private List<QuestionLevel> questionLevels;
	
	private long instantiationTime;
	
	public TestBean()
	{
		instantiationTime=new Date().getTime();
		
		// First we initialize some helper properties
		filterCategoryId=Long.MIN_VALUE;
		filterIncludeSubcategories=false;
		filterQuestionType="";
		filterQuestionLevel="";
		questionsDualList=null;
		filteredUsersForAddingUsers=null;
		filteredUsersForAddingAdmins=null;
		userTypes=null;
		filterUsersUserTypeId=0L;
		filterUsersIncludeOmUsers=true;
		testUsers=null;
		usersDualList=null;
		filterAdminsUserTypeId=0L;
		filterAdminsIncludeOmUsers=true;
		testAdmins=null;
		adminsDualList=null;
		activeTestTabName=GENERAL_WIZARD_TAB;
		activeTestIndex=GENERAL_TABVIEW_TAB;
		nextTestTabNameOnChangePropertyConfirm=null;
		nextTestIndexOnChangePropertyConfirm=-1;
		oldScoreType=null;
		activeGeneralTabIndex=0;
		activeSectionIndex=0;
		activeSectionName="";
		supportContact=null;
		supportContactDialogDisplayed=false;
		evaluator=null;
		evaluatorDialogDisplayed=false;
		enabledCheckboxesSetters=true;
		setPropertyChecked(null);
		sectionChecked=null;
		nextSectionIndexOnChangePropertyConfirm=-1;
		testsCategories=null;
		specialCategoriesFilters=null;
		questionsCategories=null;
		assessements=null;
		scoreTypes=null;
		navLocations=null;
		redoQuestions=null;
		globalOtherUserCategoryAllowed=null;
		viewTestsFromOtherUsersPrivateCategoriesEnabled=null;
		viewTestsFromAdminsPrivateCategoriesEnabled=null;
		viewTestsFromSuperadminsPrivateCategoriesEnabled=null;
		useGlobalQuestions=null;
		useOtherUsersQuestions=null;
		viewQuestionsFromOtherUsersPrivateCategoriesEnabled=null;
		viewQuestionsFromAdminsPrivateCategoriesEnabled=null;
		viewQuestionsFromSuperadminsPrivateCategoriesEnabled=null;
		testAuthorViewQuestionsFromOtherUsersPrivateCategoriesEnabled=null;
		testAuthorViewQuestionsFromAdminsPrivateCategoriesEnabled=null;
		testAuthorViewQuestionsFromSuperadminsPrivateCategoriesEnabled=null;
		viewOMEnabled=null;
		viewOMQuestionsEnabled=new HashMap<Long,Boolean>();
		admins=new HashMap<Long,Boolean>();
		superadmins=new HashMap<Long,Boolean>();
		supportContactFilterType="NO_FILTER";
		supportContactFilterSubtype=null;
		supportContactFilterSubtypes=null;
		filterSupportContactFilterUsersUserTypeId=0L;
		filterSupportContactFilterUsersIncludeOmUsers=true;
		testSupportContactFilterUsers=new ArrayList<User>();
		supportContactFilterUsersIdsHidden="";
		supportContactFilterUsersDualList=null;
		evaluatorFilterType="NO_FILTER";
		evaluatorFilterSubtype=null;
		evaluatorFilterSubtypes=null;
		filterEvaluatorFilterUsersUserTypeId=0L;
		filterEvaluatorFilterUsersIncludeOmUsers=true;
		testEvaluatorFilterUsers=new ArrayList<User>();
		evaluatorFilterUsersIdsHidden="";
		evaluatorFilterUsersDualList=null;
		letters=null;
		questionLevels=null;
		
		// Test bean initialization will be finished later
		testInitialized=false;
	}
	
	public LocalizationService getLocalizationService()
	{
		return localizationService;
	}
	
	public void setLocalizationService(LocalizationService localizationService)
	{
		this.localizationService=localizationService;
	}
	
	public void setConfigurationService(ConfigurationService configurationService)
	{
		this.configurationService=configurationService;
	}
	
	public void setTestsService(TestsService testsService)
	{
		this.testsService=testsService;
	}
	
	public void setQuestionsService(QuestionsService questionsService)
	{
		this.questionsService=questionsService;
	}
    
	public void setQuestionTypesService(QuestionTypesService questionTypesService)
	{
		this.questionTypesService=questionTypesService;
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
	
	public void setAssessementsService(AssessementsService assessementsService)
	{
		this.assessementsService=assessementsService;
	}
	
	public void setScoreTypesService(ScoreTypesService scoreTypesService)
	{
		this.scoreTypesService=scoreTypesService;
	}
	
    public void setNavLocationsService(NavLocationsService navLocationsService)
    {
    	this.navLocationsService=navLocationsService;
    }
    
    public void setRedoQuestionValuesService(RedoQuestionValuesService redoQuestionValuesService)
    {
    	this.redoQuestionValuesService=redoQuestionValuesService;
    }
    
    public void setScoreUnitsService(ScoreUnitsService scoreUnitsService)
    {
    	this.scoreUnitsService=scoreUnitsService;
    }
    
    public void setTestUsersService(TestUsersService testUsersService)
    {
    	this.testUsersService=testUsersService;
    }
    
    public void setAddressTypesService(AddressTypesService addressTypesService)
    {
    	this.addressTypesService=addressTypesService;
    }
    
    public void setUsersService(UsersService usersService)
    {
    	this.usersService=usersService;
    }
    
    public void setUserTypesService(UserTypesService userTypesService)
    {
    	this.userTypesService=userTypesService;
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
    	return operation==null?userSessionService.getCurrentUserOperation():operation;
    }
    
    public long getTestId()
    {
    	return getTestId(null);
    }
    
    public void setTestId(long testId)
    {
    	this.testId=testId;
    }
    
    private long getTestId(Operation operation)
    {
    	if (!testInitialized)
    	{
    		initializeTest(getCurrentUserOperation(operation));
    	}
    	return testId;
    }
    
	public String getName()
	{
		return getName(null);
	}
	
	public void setName(String name)
	{
		this.name=name;
	}
	
	private String getName(Operation operation)
	{
    	if (!testInitialized)
    	{
    		initializeTest(getCurrentUserOperation(operation));
    	}
		return name;
	}
	
	public User getAuthor()
	{
		return getAuthor(null);
	}
	
	public void setAuthor(User author)
	{
		this.author=author;
	}
	
	private User getAuthor(Operation operation)
	{
		if (!testInitialized)
		{
			initializeTest(getCurrentUserOperation(operation));
		}
		return author;
	}
	
	public Date getTimeCreated()
	{
		return getTimeCreated(null);
	}
	
	public void setTimeCreated(Date timeCreated)
	{
		this.timeCreated=timeCreated;
	}
	
	private Date getTimeCreated(Operation operation)
	{
		if (!testInitialized)
		{
			initializeTest(getCurrentUserOperation(operation));
		}
		return timeCreated;
	}
	
	public Date getTimeTestDeploy()
	{
		return getTimeTestDeploy(null);
	}
	
	public void setTimeTestDeploy(Date timeTestDeploy)
	{
		this.timeTestDeploy=timeTestDeploy;
	}
	
	private Date getTimeTestDeploy(Operation operation)
	{
		if (!testInitialized)
		{
			initializeTest(getCurrentUserOperation(operation));
		}
		return timeTestDeploy;
	}
	
	public Date getTimeDeployDeploy()
	{
		return getTimeDeployDeploy(null);
	}
	
	public void setTimeDeployDeploy(Date timeDeployDeploy)
	{
		this.timeDeployDeploy=timeDeployDeploy;
	}
	
	private Date getTimeDeployDeploy(Operation operation)
	{
		if (!testInitialized)
		{
			initializeTest(getCurrentUserOperation(operation));
		}
		return timeDeployDeploy;
	}
	
	public Category getCategory()
	{
		return getCategory(null);
	}
	
	public void setCategory(Category category)
	{
		this.category=category;
	}
	
	private Category getCategory(Operation operation)
	{
		if (!testInitialized)
		{
			initializeTest(getCurrentUserOperation(operation));
		}
		return category;
	}
	
	public long getCategoryId()
	{
		return getCategoryId(null);
	}
	
	public void setCategoryId(long categoryId)
	{
		setCategoryId(null,categoryId);
	}
	
	private long getCategoryId(Operation operation)
	{
		Category category=getCategory(getCurrentUserOperation(operation));
		return category==null?0L:category.getId();
	}
	
	private void setCategoryId(Operation operation,long categoryId)
	{
		setCategory(categoryId>0L?categoriesService.getCategory(getCurrentUserOperation(operation),categoryId):null);
	}
	
	public String getDescription()
	{
		return getDescription(null);
	}
	
	public void setDescription(String description)
	{
		this.description=description;
	}
	
	private String getDescription(Operation operation)
	{
    	if (!testInitialized)
    	{
    		initializeTest(getCurrentUserOperation(operation));
    	}
		return description;
	}
	
	public String getTitle()
	{
		return getTitle(null);
	}
	
	public void setTitle(String title)
	{
		this.title=title;
	}
	
	private String getTitle(Operation operation)
	{
    	if (!testInitialized)
    	{
    		initializeTest(getCurrentUserOperation(operation));
    	}
		return title;
	}
	
	public Assessement getAssessement()
	{
		return getAssessement(null);
	}
	
	public void setAssessement(Assessement assessement)
	{
		this.assessement=assessement;
	}
	
	private Assessement getAssessement(Operation operation)
	{
		if (!testInitialized)
		{
			initializeTest(getCurrentUserOperation(operation));
		}
		return assessement;
	}
	
	public long getAssessementId()
	{
		return getAssessementId(null);
	}
	
	public void setAssessementId(long assessementId)
	{
		setAssessementId(null,assessementId);
	}
	
	private long getAssessementId(Operation operation)
	{
		Assessement assessement=getAssessement(getCurrentUserOperation(operation));
		return assessement==null?0L:assessement.getId();
	}
	
	private void setAssessementId(Operation operation,long assessementId)
	{
		// Get current user session Hibernate operation
		operation=getCurrentUserOperation(operation);
		
		Assessement assessement=getAssessement(operation);
		if (assessement==null || assessement.getId()!=assessementId)
		{
			setAssessement(assessementsService.getAssessement(operation,assessementId));
		}
	}
	
	public String getAssessementTip()
	{
		return getAssessementTip(null);
	}
	
	private String getAssessementTip(Operation operation)
	{
		String assessementTip=null;
		Assessement assessement=getAssessement(getCurrentUserOperation(operation));
		if (assessement!=null)
		{
			StringBuffer assessementTipLabel=new StringBuffer(assessement.getType());
			assessementTipLabel.append("_TIP");
			assessementTip=localizationService.getLocalizedMessage(assessementTipLabel.toString());
		}
		return assessementTip==null?"":assessementTip;
	}
	
	public ScoreType getScoreType()
	{
		return getScoreType(null);
	}
	
	public void setScoreType(ScoreType scoreType)
	{
		setScoreType(null,scoreType);
	}
	
	public void setScoreType(Operation operation,ScoreType scoreType)
	{
		ScoreType thisScoreType=getScoreType(getCurrentUserOperation(operation));
		// Needed this check because sometimes setter is called several times with the same value
		if ((scoreType==null && thisScoreType==null) || (scoreType!=null && !scoreType.equals(thisScoreType)))
		{
			oldScoreType=thisScoreType;
		}
		this.scoreType=scoreType;
	}
	
	public ScoreType getScoreType(Operation operation)
	{
		if (!testInitialized)
		{
			initializeTest(getCurrentUserOperation(operation));
		}
		return scoreType;
	}
	
	public void rollbackScoreType()
	{
		scoreType=oldScoreType;
	}
	
	public long getScoreTypeId()
	{
		return getScoreTypeId(null);
	}
	
	public void setScoreTypeId(long scoreTypeId)
	{
		setScoreTypeId(null,scoreTypeId);
	}
	
	private long getScoreTypeId(Operation operation)
	{
		ScoreType scoreType=getScoreType(getCurrentUserOperation(operation));
		return scoreType==null?0L:scoreType.getId();
	}
	
	private void setScoreTypeId(Operation operation,long scoreTypeId)
	{
		// Get current user Hibernate operation
		operation=getCurrentUserOperation(operation);
		
		ScoreType scoreType=getScoreType(operation);
		if (scoreType==null || scoreType.getId()!=scoreTypeId)
		{
			setScoreType(operation,scoreTypesService.getScoreType(operation,scoreTypeId));
		}
	}
	
	public String getScoreTypeTip()
	{
		return getScoreTypeTip(null);
	}
	
	private String getScoreTypeTip(Operation operation)
	{
		String scoreTypeTip=null;
		ScoreType scoreType=getScoreType(getCurrentUserOperation(operation));
		if (scoreType!=null)
		{
			StringBuffer scoreTypeTipLabel=new StringBuffer(scoreType.getType());
			scoreTypeTipLabel.append("_TIP");
			scoreTypeTip=localizationService.getLocalizedMessage(scoreTypeTipLabel.toString());
		}
		return scoreTypeTip==null?"":scoreTypeTip;
	}
	
	
	public boolean isAllUsersAllowed()
	{
		return isAllUsersAllowed(null);
	}
	
	public void setAllUsersAllowed(boolean allUsersAllowed)
	{
		if (isEnabledChecboxesSetters())
		{
			this.allUsersAllowed=allUsersAllowed;
		}
	}
	
	private boolean isAllUsersAllowed(Operation operation)
	{
		if (!testInitialized)
		{
			initializeTest(getCurrentUserOperation(operation));
		}
		return allUsersAllowed;
	}
	
	public boolean isAllowAdminReports()
	{
		return isAllowAdminReports(null);
	}
	
	public void setAllowAdminReports(boolean allowAdminReports)
	{
		this.allowAdminReports=allowAdminReports;
	}
	
	private boolean isAllowAdminReports(Operation operation)
	{
		if (!testInitialized)
		{
			initializeTest(getCurrentUserOperation(operation));
		}
		return allowAdminReports;
	}
	
	public Date getStartDate()
	{
		return getStartDate(null);
	}
	
	public void setStartDate(Date startDate)
	{
		this.startDate=startDate;
	}
	
	public Date getStartDate(Operation operation)
	{
		if (!testInitialized)
		{
			initializeTest(getCurrentUserOperation(operation));
		}
		if (getStartDateHidden()!=null && !getStartDateHidden().equals(""))
		{
			DateFormat df=new SimpleDateFormat(DATE_HIDDEN_PATTERN);
			try
			{
				startDate=df.parse(getStartDateHidden());
			}
			catch (ParseException pe)
			{
			}
			setStartDateHidden("");
		}
		return startDate;
	}
	
	public void changeStartDate(DateSelectEvent event)
	{
		if (event.getDate()==null)
		{
			setStartDateHidden("");
		}
		else
		{
			DateFormat df=new SimpleDateFormat(DATE_HIDDEN_PATTERN);
			setStartDateHidden(df.format(event.getDate()));
		}
		setStartDate(event.getDate());
	}
	
	public Date getCloseDate()
	{
		return getCloseDate(null);
	}
	
	public void setCloseDate(Date closeDate)
	{
		this.closeDate=closeDate;
	}
	
	private Date getCloseDate(Operation operation)
	{
		if (!testInitialized)
		{
			initializeTest(getCurrentUserOperation(operation));
		}
		if (getCloseDateHidden()!=null && !getCloseDateHidden().equals(""))
		{
			DateFormat df=new SimpleDateFormat(DATE_HIDDEN_PATTERN);
			try
			{
				closeDate=df.parse(getCloseDateHidden());
			}
			catch (ParseException pe)
			{
			}
			setCloseDateHidden("");
		}
		return closeDate;
	}
	
	public void changeCloseDate(DateSelectEvent event)
	{
		if (event.getDate()==null)
		{
			setCloseDateHidden("");
		}
		else
		{
			DateFormat df=new SimpleDateFormat(DATE_HIDDEN_PATTERN);
			setCloseDateHidden(df.format(event.getDate()));
		}
		setCloseDate(event.getDate());
	}
	
	public Date getWarningDate()
	{
		return getWarningDate(null);
	}
	
	public void setWarningDate(Date warningDate)
	{
		this.warningDate=warningDate;
	}
	
	private Date getWarningDate(Operation operation)
	{
		if (!testInitialized)
		{
			initializeTest(getCurrentUserOperation(operation));
		}
		if (getWarningDateHidden()!=null && !getWarningDateHidden().equals(""))
		{
			DateFormat df=new SimpleDateFormat(DATE_HIDDEN_PATTERN);
			try
			{
				warningDate=df.parse(getWarningDateHidden());
			}
			catch (ParseException pe)
			{
			}
			setWarningDateHidden("");
		}
		return warningDate;
	}
	
	public void changeWarningDate(DateSelectEvent event)
	{
		if (event.getDate()==null)
		{
			setWarningDateHidden("");
		}
		else
		{
			DateFormat df=new SimpleDateFormat(DATE_HIDDEN_PATTERN);
			setWarningDateHidden(df.format(event.getDate()));
		}
		setWarningDate(event.getDate());
	}
	
	public Date getFeedbackDate()
	{
		return getFeedbackDate(null);
	}
	
	public void setFeedbackDate(Date feedbackDate)
	{
		this.feedbackDate=feedbackDate;
	}
	
	private Date getFeedbackDate(Operation operation)
	{
		if (!testInitialized)
		{
			initializeTest(getCurrentUserOperation(operation));
		}
		if (getFeedbackDateHidden()!=null && !getFeedbackDateHidden().equals(""))
		{
			DateFormat df=new SimpleDateFormat(DATE_HIDDEN_PATTERN);
			try
			{
				feedbackDate=df.parse(getFeedbackDateHidden());
			}
			catch (ParseException pe)
			{
			}
			setFeedbackDateHidden("");
		}
		return feedbackDate;
	}
	
	public void changeFeedbackDate(DateSelectEvent event)
	{
		if (event.getDate()==null)
		{
			setFeedbackDateHidden("");
		}
		else
		{
			DateFormat df=new SimpleDateFormat(DATE_HIDDEN_PATTERN);
			setFeedbackDateHidden(df.format(event.getDate()));
		}
		setFeedbackDate(event.getDate());
	}
	
	public boolean isRestrictDates()
	{
		return isRestrictDates(null);
	}
	
	public void setRestrictDates(boolean restrictDates)
	{
		if (isEnabledChecboxesSetters())
		{
			this.restrictDates=restrictDates;
		}
	}
	
	private boolean isRestrictDates(Operation operation)
	{
		if (!testInitialized)
		{
			initializeTest(getCurrentUserOperation(operation));
		}
		return restrictDates;
	}
	
	public boolean isRestrictFeedbackDate()
	{
		return isRestrictFeedbackDate(null);
	}
	
	public void setRestrictFeedbackDate(boolean restrictFeedbackDate)
	{
		if (isEnabledChecboxesSetters())
		{
			this.restrictFeedbackDate=restrictFeedbackDate;
		}
	}
	
	private boolean isRestrictFeedbackDate(Operation operation)
	{
		if (!testInitialized)
		{
			initializeTest(getCurrentUserOperation(operation));
		}
		return restrictFeedbackDate;
	}
	
	public String getStartDateHidden()
	{
		return startDateHidden;
	}
	
	public void setStartDateHidden(String startDateHidden)
	{
		this.startDateHidden=startDateHidden;
	}
	
	public String getCloseDateHidden()
	{
		return closeDateHidden;
	}
	
	public void setCloseDateHidden(String closeDateHidden)
	{
		this.closeDateHidden=closeDateHidden;
	}
	
	public String getWarningDateHidden()
	{
		return warningDateHidden;
	}
	
	public void setWarningDateHidden(String warningDateHidden)
	{
		this.warningDateHidden=warningDateHidden;
	}
	
	public String getFeedbackDateHidden()
	{
		return feedbackDateHidden;
	}
	
	public void setFeedbackDateHidden(String feedbackDateHidden)
	{
		this.feedbackDateHidden=feedbackDateHidden;
	}
	
	public boolean isFreeSummary()
	{
		return isFreeSummary(null);
	}
	
	public void setFreeSummary(boolean freeSummary)
	{
		this.freeSummary=freeSummary;
	}
	
	private boolean isFreeSummary(Operation operation)
	{
		if (!testInitialized)
		{
			initializeTest(getCurrentUserOperation(operation));
		}
		return freeSummary;
	}
	
	public boolean isFreeStop()
	{
		return isFreeStop(null);
	}
	
	public void setFreeStop(boolean freeStop)
	{
		this.freeStop=freeStop;
		
	}
	
	public boolean isFreeStop(Operation operation)
	{
		if (!testInitialized)
		{
			initializeTest(getCurrentUserOperation(operation));
		}
		return freeStop;
	}
	
	public boolean isSummaryQuestions()
	{
		return isSummaryQuestions(null);
	}
	
	public void setSummaryQuestions(boolean summaryQuestions)
	{
		this.summaryQuestions=summaryQuestions;
		
	}
	
	private boolean isSummaryQuestions(Operation operation)
	{
		if (!testInitialized)
		{
			initializeTest(getCurrentUserOperation(operation));
		}
		return summaryQuestions;
	}
	
	public boolean isSummaryScores()
	{
		return isSummaryScores(null);
	}
	
	public void setSummaryScores(boolean summaryScores)
	{
		this.summaryScores=summaryScores;
		
	}
	
	private boolean isSummaryScores(Operation operation)
	{
		if (!testInitialized)
		{
			initializeTest(getCurrentUserOperation(operation));
		}
		return summaryScores;
	}
	
	public boolean isSummaryAttempts()
	{
		return isSummaryAttempts(null);
	}
	
	public void setSummaryAttempts(boolean summaryAttempts)
	{
		this.summaryAttempts=summaryAttempts;
		
	}
	
	private boolean isSummaryAttempts(Operation operation)
	{
		if (!testInitialized)
		{
			initializeTest(getCurrentUserOperation(operation));
		}
		return summaryAttempts;
	}
	
	public boolean isNavigation()
	{
		return isNavigation(null);
	}
	
	public void setNavigation(boolean navigation)
	{
		this.navigation=navigation;
		if (navigation)
		{
			preliminarySummaryTabviewTab=PRELIMINARY_SUMMARY_OR_FEEDBACK_TABVIEW_TAB;
			feedbackTabviewTab=PRELIMINARY_SUMMARY_OR_FEEDBACK_TABVIEW_TAB+1;
		}
		else
		{
			preliminarySummaryTabviewTab=Integer.MIN_VALUE;
			feedbackTabviewTab=PRELIMINARY_SUMMARY_OR_FEEDBACK_TABVIEW_TAB;
		}
	}
	
	private boolean isNavigation(Operation operation)
	{
		if (!testInitialized)
		{
			initializeTest(getCurrentUserOperation(operation));
		}
		return navigation;
	}
	
	public NavLocation getNavLocation()
	{
		return getNavLocation(null);
	}
	
	public void setNavLocation(NavLocation navLocation)
	{
		this.navLocation=navLocation;
	}
	
	private NavLocation getNavLocation(Operation operation)
	{
		if (!testInitialized)
		{
			initializeTest(getCurrentUserOperation(operation));
		}
		return navLocation;
	}
	
	public long getNavLocationId()
	{
		return getNavLocationId(null);
	}
	
	public void setNavLocationId(long navLocationId)
	{
		setNavLocationId(null,navLocationId);
	}
	
	private long getNavLocationId(Operation operation)
	{
		NavLocation navLocation=getNavLocation(getCurrentUserOperation(operation));
		return navLocation==null?0L:navLocation.getId();
	}
	
	private void setNavLocationId(Operation operation,long navLocationId)
	{
		setNavLocation(navLocationsService.getNavLocation(getCurrentUserOperation(operation),navLocationId));
	}
	
	public RedoQuestionValue getRedoQuestion()
	{
		return getRedoQuestion(null);
	}
	
	public void setRedoQuestion(RedoQuestionValue redoQuestion)
	{
		this.redoQuestion=redoQuestion;
	}
	
	private RedoQuestionValue getRedoQuestion(Operation operation)
	{
		if (!testInitialized)
		{
			initializeTest(getCurrentUserOperation(operation));
		}
		return redoQuestion;
	}
	
	public long getRedoQuestionId()
	{
		return getRedoQuestionId(null);
	}
	
	public void setRedoQuestionId(long redoQuestionId)
	{
		setRedoQuestionId(null,redoQuestionId);
	}
	
	private long getRedoQuestionId(Operation operation)
	{
		RedoQuestionValue redoQuestion=getRedoQuestion(getCurrentUserOperation(operation));
		return redoQuestion==null?0L:redoQuestion.getId();
	}
	
	private void setRedoQuestionId(Operation operation,long redoQuestionId)
	{
		setRedoQuestion(redoQuestionValuesService.getRedoQuestion(getCurrentUserOperation(operation),redoQuestionId));
	}
	
	public boolean isRedoTest()
	{
		return isRedoTest(null);
	}
	
	public void setRedoTest(boolean redoTest)
	{
		this.redoTest=redoTest;
		
	}
	
	private boolean isRedoTest(Operation operation)
	{
		if (!testInitialized)
		{
			initializeTest(getCurrentUserOperation(operation));
		}
		return redoTest;
	}
	
	public String getPresentationTitle()
	{
		return getPresentationTitle(null);
	}
	
	public void setPresentationTitle(String presentationTitle)
	{
		this.presentationTitle=presentationTitle;
	}
	
	private String getPresentationTitle(Operation operation)
	{
		if (!testInitialized)
		{
			initializeTest(getCurrentUserOperation(operation));
		}
		return presentationTitle;
	}
	
	public String getPresentation()
	{
		return getPresentation(null);
	}
	
	public void setPresentation(String presentation)
	{
		this.presentation=presentation;
	}
	
	private String getPresentation(Operation operation)
	{
		if (!testInitialized)
		{
			initializeTest(getCurrentUserOperation(operation));
		}
		return presentation;
	}
	
	public String getPreliminarySummaryTitle()
	{
		return getPreliminarySummaryTitle(null);
	}
	
	public void setPreliminarySummaryTitle(String preliminarySummaryTitle)
	{
		this.preliminarySummaryTitle=preliminarySummaryTitle;
	}
	
	private String getPreliminarySummaryTitle(Operation operation)
	{
		if (!testInitialized)
		{
			initializeTest(getCurrentUserOperation(operation));
		}
		return preliminarySummaryTitle;
	}
	
	public String getPreliminarySummaryButton()
	{
		return getPreliminarySummaryButton(null);
	}
	
	public void setPreliminarySummaryButton(String preliminarySummaryButton)
	{
		this.preliminarySummaryButton=preliminarySummaryButton;
	}
	
	private String getPreliminarySummaryButton(Operation operation)
	{
		if (!testInitialized)
		{
			initializeTest(getCurrentUserOperation(operation));
		}
		return preliminarySummaryButton;
	}
	
	public String getPreliminarySummary()
	{
		return getPreliminarySummary(null);
	}
	
	public void setPreliminarySummary(String preliminarySummary)
	{
		this.preliminarySummary=preliminarySummary;
	}
	
	private String getPreliminarySummary(Operation operation)
	{
		if (!testInitialized)
		{
			initializeTest(getCurrentUserOperation(operation));
		}
		return preliminarySummary;
	}
	
	public boolean isFeedbackDisplaySummary()
	{
		return isFeedbackDisplaySummary(null);
	}
	
	public void setFeedbackDisplaySummary(boolean feedbackDisplaySummary)
	{
		if (isEnabledChecboxesSetters())
		{
			this.feedbackDisplaySummary=feedbackDisplaySummary;
		}
	}
	
	private boolean isFeedbackDisplaySummary(Operation operation)
	{
		if (!testInitialized)
		{
			initializeTest(getCurrentUserOperation(operation));
		}
		return feedbackDisplaySummary;
	}
	
	public boolean isFeedbackDisplaySummaryMarks()
	{
		return isFeedbackDisplaySummaryMarks(null);
	}
	
	public void setFeedbackDisplaySummaryMarks(boolean feedbackDisplaySummaryMarks)
	{
		this.feedbackDisplaySummaryMarks=feedbackDisplaySummaryMarks;
	}
	
	private boolean isFeedbackDisplaySummaryMarks(Operation operation)
	{
		if (!testInitialized)
		{
			initializeTest(getCurrentUserOperation(operation));
		}
		return feedbackDisplaySummaryMarks;
	}
	
	public boolean isFeedbackDisplaySummaryAttempts()
	{
		return isFeedbackDisplaySummaryAttempts(null);
	}
	
	public void setFeedbackDisplaySummaryAttempts(boolean feedbackDisplaySummaryAttempts)
	{
		this.feedbackDisplaySummaryAttempts=feedbackDisplaySummaryAttempts;
	}
	
	private boolean isFeedbackDisplaySummaryAttempts(Operation operation)
	{
		if (!testInitialized)
		{
			initializeTest(getCurrentUserOperation(operation));
		}
		return feedbackDisplaySummaryAttempts;
	}
	
	public String getFeedbackSummaryPrevious()
	{
		return getFeedbackSummaryPrevious(null);
	}
	
	public void setFeedbackSummaryPrevious(String feedbackSummaryPrevious)
	{
		this.feedbackSummaryPrevious=feedbackSummaryPrevious;
	}
	
	private String getFeedbackSummaryPrevious(Operation operation)
	{
		if (!testInitialized)
		{
			initializeTest(getCurrentUserOperation(operation));
		}
		return feedbackSummaryPrevious;
	}
	
	public boolean isFeedbackDisplayScores()
	{
		return isFeedbackDisplayScores(null);
	}
	
	public void setFeedbackDisplayScores(boolean feedbackDisplayScores)
	{
		if (isEnabledChecboxesSetters())
		{
			this.feedbackDisplayScores=feedbackDisplayScores;
		}
	}
	
	private boolean isFeedbackDisplayScores(Operation operation)
	{
		if (!testInitialized)
		{
			initializeTest(getCurrentUserOperation(operation));
		}
		return feedbackDisplayScores;
	}
	
	public boolean isFeedbackDisplayScoresMarks()
	{
		return isFeedbackDisplayScoresMarks(null);
	}
	
	public void setFeedbackDisplayScoresMarks(boolean feedbackDisplayScoresMarks)
	{
		this.feedbackDisplayScoresMarks=feedbackDisplayScoresMarks;
	}
	
	private boolean isFeedbackDisplayScoresMarks(Operation operation)
	{
		if (!testInitialized)
		{
			initializeTest(getCurrentUserOperation(operation));
		}
		return feedbackDisplayScoresMarks;
	}
	
	public boolean isFeedbackDisplayScoresPercentages()
	{
		return isFeedbackDisplayScoresPercentages(null);
	}
	
	public void setFeedbackDisplayScoresPercentages(boolean feedbackDisplayScoresPercentages)
	{
		this.feedbackDisplayScoresPercentages=feedbackDisplayScoresPercentages;
	}
	
	private boolean isFeedbackDisplayScoresPercentages(Operation operation)
	{
		if (!testInitialized)
		{
			initializeTest(getCurrentUserOperation(operation));
		}
		return feedbackDisplayScoresPercentages;
	}
	
	public String getFeedbackScoresPrevious()
	{
		return getFeedbackScoresPrevious(null);
	}
	
	public void setFeedbackScoresPrevious(String feedbackScoresPrevious)
	{
		this.feedbackScoresPrevious=feedbackScoresPrevious;
	}
	
	private String getFeedbackScoresPrevious(Operation operation)
	{
		if (!testInitialized)
		{
			initializeTest(getCurrentUserOperation(operation));
		}
		return feedbackScoresPrevious;
	}
	
	public String getFeedbackAdvancedPrevious()
	{
		return getFeedbackAdvancedPrevious(null);
	}
	
	public void setFeedbackAdvancedPrevious(String feedbackAdvancedPrevious)
	{
		this.feedbackAdvancedPrevious=feedbackAdvancedPrevious;
	}
	
	private String getFeedbackAdvancedPrevious(Operation operation)
	{
		if (!testInitialized)
		{
			initializeTest(getCurrentUserOperation(operation));
		}
		return feedbackAdvancedPrevious;
	}
	
	public String getFeedbackAdvancedNext()
	{
		return getFeedbackAdvancedNext(null);
	}
	
	public void setFeedbackAdvancedNext(String feedbackAdvancedNext)
	{
		this.feedbackAdvancedNext=feedbackAdvancedNext;
	}
	
	private String getFeedbackAdvancedNext(Operation operation)
	{
		if (!testInitialized)
		{
			initializeTest(getCurrentUserOperation(operation));
		}
		return feedbackAdvancedNext;
	}
	
	public List<SectionBean> getSections()
	{
    	return getSections(null);
    }
    
    public void setSections(List<SectionBean> sections)
    {
    	this.sections=sections;
    }
    
	public List<SectionBean> getSections(Operation operation)
	{
    	if (!testInitialized)
    	{
    		initializeTest(getCurrentUserOperation(operation));
    	}
    	return sections;
    }
    
    public int getSectionsSize()
    {
    	return getSectionsSize(null);
    }
    
    private int getSectionsSize(Operation operation)
    {
    	return getSections(getCurrentUserOperation(operation)).size();
    }
    
	public List<TestFeedbackBean> getFeedbacks()
	{
		return getFeedbacks(null);
	}
	
	public void setFeedbacks(List<TestFeedbackBean> feedbacks)
	{
		this.feedbacks=feedbacks;
	}
	
	public List<TestFeedbackBean> getFeedbacks(Operation operation)
	{
    	if (!testInitialized)
    	{
    		initializeTest(getCurrentUserOperation(operation));
    	}
		return feedbacks;
	}
	
	public List<SupportContactBean> getSupportContacts()
	{
		return getSupportContacts(null);
	}
	
	public void setSupportContacts(List<SupportContactBean> supportContacts)
	{
		this.supportContacts=supportContacts;
	}
	
	private List<SupportContactBean> getSupportContacts(Operation operation)
	{
		if (!testInitialized)
		{
			initializeTest(getCurrentUserOperation(operation));
		}
		return supportContacts;
	}
	
	public List<EvaluatorBean> getEvaluators()
	{
		return getEvaluators(null);
	}
	
	public void setEvaluators(List<EvaluatorBean> evaluators)
	{
		this.evaluators=evaluators;
	}
	
	private List<EvaluatorBean> getEvaluators(Operation operation)
	{
		if (!testInitialized)
		{
			initializeTest(getCurrentUserOperation(operation));
		}
		return evaluators;
	}
	
	/**
	 * @param type Address type's type
	 * @param subtype Address type's subtype
	 * @return Address type
	 */
	public AddressType getAddressType(String type,String subtype)
	{
		return getAddressType(null,type,subtype);
	}
	
	/**
	 * @param operation Operation
	 * @param type Address type's type
	 * @param subtype Address type's subtype
	 * @return Address type
	 */
	public AddressType getAddressType(Operation operation,String type,String subtype)
	{
		return addressTypesService.getAddressType(getCurrentUserOperation(operation),type,subtype);
	}
	
	public SectionBean getCurrentSection()
	{
		return currentSection;
	}
	
	public void setCurrentSection(SectionBean currentSection)
	{
		this.currentSection=currentSection;
	}
	
	public TestFeedbackBean getCurrentFeedback()
	{
		return currentFeedback;
	}
	
	public void setCurrentFeedback(TestFeedbackBean currentFeedback)
	{
		this.currentFeedback=currentFeedback;
	}
	
	public SupportContactBean getCurrentSupportContact()
	{
		return currentSuportContact;
	}
	
	public void setCurrentSupportContact(SupportContactBean currentSupportContact)
	{
		this.currentSuportContact=currentSupportContact;
	}
	
	public EvaluatorBean getCurrentEvaluator()
	{
		return currentEvaluator;
	}
	
	public void setCurrentEvaluator(EvaluatorBean currentEvaluator)
	{
		this.currentEvaluator=currentEvaluator;
	}
	
	private void initializeFilterCategoryId(Operation operation)
	{
		boolean found=false;
		List<Category> specialCategoriesFilters=getSpecialCategoriesFilters(getCurrentUserOperation(operation));
		Category allTestAuthorCategoriesFilter=new Category();
		allTestAuthorCategoriesFilter.setId(allTestAuthorCategories.id);
		if (specialCategoriesFilters.contains(allTestAuthorCategoriesFilter))
		{
			filterCategoryId=allTestAuthorCategories.id;
			found=true;
		}
		if (!found)
		{
			Category allTestAuthorCategoriesExceptGlobalsFilter=new Category();
			allTestAuthorCategoriesExceptGlobalsFilter.setId(allTestAuthorCategoriesExceptGlobals.id);
			if (specialCategoriesFilters.contains(allTestAuthorCategoriesExceptGlobalsFilter))
			{
				filterCategoryId=allTestAuthorCategoriesExceptGlobals.id;
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
			Category allCategoriesFilter=new Category();
			allCategoriesFilter.setId(allCategories.id);
			if (specialCategoriesFilters.contains(allCategoriesFilter))
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
	
	public String getFilterQuestionType()
	{
		return filterQuestionType;
	}
	
	public void setFilterQuestionType(String filterQuestionType)
	{
		this.filterQuestionType=filterQuestionType==null?"":filterQuestionType;
	}
	
	public String getFilterQuestionLevel()
	{
		return filterQuestionLevel;
	}
	
	public void setFilterQuestionLevel(String filterQuestionLevel) {
		this.filterQuestionLevel=filterQuestionLevel==null?"":filterQuestionLevel;
	}

	private String getPropertyChecked()
	{
		StringBuffer propertyCheckedAttribute=new StringBuffer("propertyChecked");
		propertyCheckedAttribute.append(instantiationTime);
		HttpServletRequest request=
			(HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
		return (String)request.getSession().getAttribute(propertyCheckedAttribute.toString());
	}
	
	private void setPropertyChecked(String propertyChecked)
	{
		StringBuffer propertyCheckedAttribute=new StringBuffer("propertyChecked");
		propertyCheckedAttribute.append(instantiationTime);
		HttpServletRequest request=
			(HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
		request.getSession().setAttribute(propertyCheckedAttribute.toString(),propertyChecked);
	}
	
	// ActionListener que añade una nueva sección a la prueba
	/**
     * Add a section to test.
     * @param event Action event
     */
	public void addSection(ActionEvent event)
	{
		// Get current user session Hibernate operation
		Operation operation=getCurrentUserOperation(null);
		
		// Get section to process if any
    	AccordionPanel sectionsAccordion=getSectionsAccordion(operation,event.getComponent());
    	SectionBean section=getSectionFromSectionsAccordion(operation,sectionsAccordion);
		
    	String property=getPropertyChecked();
    	if (property==null)
    	{
    		// We need to process some input fields
    		processSectionsInputFields(operation,event.getComponent(),section);
    		
    		// Set back accordion row index -1
    		sectionsAccordion.setRowIndex(-1);
    		
    		// Check that current section name entered by user is valid
    		if (checkSectionName(section.getName()))
    		{
        		//Add a new section
        		int numberSections=getSectionsSize(operation)+1;
            	section=new SectionBean(this,numberSections);
            	sections.add(section);
        		
            	// Change active tab of sections accordion to display the new section
        		activeSectionIndex=numberSections-1;
        		activeSectionName=section.getName();
        		refreshActiveSection(operation,sectionsAccordion);
        		
        		// We need to update sections text fields
        		updateSectionsTextFields(operation,event.getComponent(),numberSections);
    		}
    		else
    		{
    			// Restore old section name
    			section.setName(activeSectionName);
    			
    			// Scroll page to top position
    			scrollToTop();
    		}
    	}
    	else if ("sectionWeight".equals(property))
    	{
			// We need to process weight
    		processSectionWeight(operation,event.getComponent(),section);
    		
    		// We need to stop this response because there is another one in process
    		FacesContext.getCurrentInstance().responseComplete();
    	}
    	else if ("randomQuantity".equals(property))
    	{
			// We need to process random quantity
    		processSectionRandomQuantity(operation,event.getComponent(),section);
    		
    		// We need to stop this response because there is another one in process
    		FacesContext.getCurrentInstance().responseComplete();
    	}
    	else if ("questionOrderWeight".equals(property))
    	{
    		// We need to process question orders weights
    		processQuestionOrderWeights(operation,event.getComponent(),section);
    		
    		// We need to stop this response because there is another one in process
    		FacesContext.getCurrentInstance().responseComplete();
    	}
	}
    
	/**
	 * Checks if deleting a section will affect to the feedbacks already defined.
	 * @param operation Operation
	 * @param sectionOrder Position of section to delete
	 * @return true if deleting a section won't affect to the feedbacks already defined, false otherwise
	 */
	private boolean checkFeedbacksForDeleteSection(Operation operation,int sectionOrder)
	{
		boolean ok=true;
		int newTotalMarks=0;
		
		// Get current user session Hibernate operation
		operation=getCurrentUserOperation(operation);
		
		ScoreType scoreType=getScoreType(operation);
		if ("SCORE_TYPE_QUESTIONS".equals(scoreType.getType()))
		{
			for (SectionBean section:getSections(operation))
			{
				if (section.getOrder()!=sectionOrder)
				{
					if (section.isShuffle() && section.isRandom())
					{
						newTotalMarks+=section.getRandomQuantity()*BASE_MARKS_PER_QUESTION;
					}
					else
					{
						for (QuestionOrderBean questionOrder:section.getQuestionOrders())
						{
							newTotalMarks+=questionOrder.getWeight()*BASE_MARKS_PER_QUESTION;
						}
					}
				}
			}
		}
		else if ("SCORE_TYPE_SECTIONS".equals(scoreType.getType()))
		{
			int maxBaseSectionScore=0;
			int totalSectionsWeight=0;
			for (SectionBean section:getSections(operation))
			{
				if (section.getOrder()!=sectionOrder)
				{
					int maxSectionScore=0;
					if (section.isShuffle() && section.isRandom())
					{
						maxSectionScore=section.getRandomQuantity()*BASE_MARKS_PER_QUESTION;
					}
					else
					{
						for (QuestionOrderBean questionOrder:section.getQuestionOrders())
						{
							maxSectionScore+=questionOrder.getWeight()*BASE_MARKS_PER_QUESTION;
						}
					}
					if (maxSectionScore>maxBaseSectionScore)
					{
						maxBaseSectionScore=maxSectionScore;
					}
				}
				if (section.getOrder()!=sectionOrder)
				{
					totalSectionsWeight+=section.getWeight();
				}
			}
			newTotalMarks=totalSectionsWeight*maxBaseSectionScore;
		}
		for (TestFeedbackBean feedback:getFeedbacks(operation))
		{
			SectionBean section=feedback.getCondition().getSection();
			if (section==null)
			{
				if (feedback.getCondition().getUnit().equals(TestFeedbackConditionBean.MARKS_UNIT))
				{
					int minValue=feedback.getCondition().getMinValue();
					if (minValue>newTotalMarks)
					{
						ok=false;
						break;
					}
					int maxValue=feedback.getCondition().getMaxValue();
					if (maxValue!=Integer.MAX_VALUE && maxValue>newTotalMarks)
					{
						ok=false;
						break;
					}
				}
			}
			else if (section.getOrder()==sectionOrder)
			{
				ok=false;
				break;
			}
		}
		return ok;
	}
	
	/**
	 * Updates feebacks related to the deleted section if needed.
	 * @param operation Operation
	 * @param sectionOrder Position of deleted section
	 */
	private void updateFeedbacksForDeleteSection(Operation operation,int sectionOrder)
	{
		// Get current user session Hibernate operation
		operation=getCurrentUserOperation(operation);
		
		for (TestFeedbackBean feedback:getFeedbacks(operation))
		{
			TestFeedbackConditionBean condition=feedback.getCondition();
			if (condition.getSection()!=null && condition.getSection().getOrder()==sectionOrder)
			{
				condition.setSection(null);
			}
			if (condition.getSection()==null && 
				condition.getUnit().equals(TestFeedbackConditionBean.MARKS_UNIT))
			{
				if (NumberComparator.compareU(condition.getComparator(),NumberComparator.BETWEEN))
				{
					int maxConditionalValue=condition.getMaxConditionalValue(operation);
					if (condition.getConditionalBetweenMax()>maxConditionalValue)
					{
						condition.setConditionalBetweenMax(maxConditionalValue);
						if (condition.getConditionalBetweenMin()>condition.getConditionalBetweenMax())
						{
							condition.setConditionalBetweenMin(maxConditionalValue);
						}
					}
				}
				else
				{
					int maxValueConditionalCmp=condition.getMaxValueConditionalCmp(operation);
					if (condition.getConditionalCmp()>maxValueConditionalCmp)
					{
						condition.setConditionalCmp(maxValueConditionalCmp);
					}
				}
			}
		}
	}
	
    // ActionListener que elimina una sección de la prueba y reordena el resto de secciones
    //public void removeSection(int sectionNumber)
	/**
	 * Delete a section from test.
     * @param event Action event
	 */
    public void removeSection(ActionEvent event)
    {
    	String property=getPropertyChecked();
    	if (property==null)
    	{
    		// Get current user session Hibernate operation
    		Operation operation=getCurrentUserOperation(null);
    		
    		boolean forceRemoveSection=true;
    		if (event.getComponent().getAttributes().get("order")!=null)
    		{
    			sectionToRemoveOrder=((Integer)event.getComponent().getAttributes().get("order")).intValue();
    			forceRemoveSection=false;
    		}
    		boolean checkFeedbacks=checkFeedbacksForDeleteSection(operation,sectionToRemoveOrder);
        	if (forceRemoveSection || checkFeedbacks)
        	{
               	// Remove section from test
            	sections.remove(sectionToRemoveOrder-1);
        		for (int index=sectionToRemoveOrder-1;index<sections.size();index++)
        		{
        			sections.get(index).setOrder(index+1);		 
        		}
        		
        		// Get sections accordion
        		AccordionPanel sectionsAccordion=getSectionsAccordion(operation,event.getComponent());
        		
        		// If it is needeed change active tab of sections accordion
        		int numSections=sections.size();
        		if (sectionToRemoveOrder>numSections)
        		{
        			activeSectionIndex=numSections-1;
        			refreshActiveSection(operation,sectionsAccordion);
        		}
        		activeSectionName=getSection(operation,activeSectionIndex+1).getName();
        		
        		// Set the new current section to be able to update text fields
        		setCurrentSection(getSectionFromSectionsAccordion(operation,sectionsAccordion));
        		
    			// If it is needed update feedbacks
    			if (!checkFeedbacks)
    			{
    				updateFeedbacksForDeleteSection(operation,sectionToRemoveOrder);
    			}
        		
        		if (getCurrentSection()!=null)
        		{
            		// We need to update sections text fields
            		updateSectionsTextFields(operation,event.getComponent(),numSections);
        		}
        	}
    		else
    		{
    			RequestContext rq=RequestContext.getCurrentInstance();
    			rq.execute("confirmDeleteSectionDialog.show()");
    		}
    	}
    	else if ("sectionWeight".equals(property))
    	{
			// We need to process random quantity
    		processSectionRandomQuantity(getCurrentUserOperation(null),event.getComponent(),sectionChecked);
    		
    		// We need to stop this response because there is another one in process
    		FacesContext.getCurrentInstance().responseComplete();
    	}
    	else if ("randomQuantity".equals(property))
    	{
			// We need to process random quantity
    		processSectionRandomQuantity(getCurrentUserOperation(null),event.getComponent(),sectionChecked);
    		
    		// We need to stop this response because there is another one in process
    		FacesContext.getCurrentInstance().responseComplete();
    	}
	}
    
	/**
	 * @return true if button to delete a section is enabled, false if it is disabled
	 */
    public boolean isEnabledRemoveSection()
    {
    	return sections.size()>1;
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
			// Get current user session Hibernate operation
			operation=getCurrentUserOperation(operation);
			
			specialCategoriesFilters=new ArrayList<Category>();
			
			User testAuthor=getAuthor(operation);
			Map<String,Boolean> cachedPermissions=new HashMap<String,Boolean>();
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
						granted=permissionsService.isGranted(operation,testAuthor,requiredPermission);
						cachedPermissions.put(requiredPermission,Boolean.valueOf(granted));
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
		return specialCategoriesFilters;
	}
	
	/**
	 * @param specialCategoryFilter Special category that represents an special category filter
	 * @return Localized special category filter's name (including question's author nick if needed)
	 */
	public String getSpecialCategoryFilterName(Category specialCategoryFilter)
	{
		return getSpecialCategoryFilterName(null,specialCategoryFilter);
	}
	
	/**
	 * @param operation Operation
	 * @param specialCategoryFilter Special category that represents an special category filter
	 * @return Localized special category filter's name (including question's author nick if needed)
	 */
	private String getSpecialCategoryFilterName(Operation operation,Category specialCategoryFilter)
	{
		String specialCategoryFilterName=localizationService.getLocalizedMessage(specialCategoryFilter.getName());
		if (specialCategoryFilterName.contains("?"))
		{
			specialCategoryFilterName=
				specialCategoryFilterName.replace("?",getAuthor(getCurrentUserOperation(operation)).getNick());
		}
		return specialCategoryFilterName;
	}
	
	/**
	 * @return List of identifiers of categories for tests from current user or globals
	 */
	public List<Long> getTestsCategoriesIds()
    {
		return getTestsCategoriesIds(null);
	}
	
	/**
	 * @param operation Operation
	 * @return List of identifiers of categories for tests from current user or globals
	 */
	private List<Long> getTestsCategoriesIds(Operation operation)
    {
    	List<Long> categoriesIds=new ArrayList<Long>();
    	for (Category category:getTestsCategories(getCurrentUserOperation(operation)))
    	{
    		categoriesIds.add(Long.valueOf(category.getId()));
    	}
		return categoriesIds;
	}
	
	//Obtiene las categorías del usuario
    /**
	 * @return List of categories for tests from current user or globals
	 */
    public List<Category> getTestsCategories()
    {
    	return getTestsCategories(null);
	}
    
    public void setTestsCategories(List<Category> testsCategories)
    {
    	this.testsCategories=testsCategories;
    }
	
    /**
     * @param operation Operation
	 * @return List of categories for tests from current user or globals
	 */
    private List<Category> getTestsCategories(Operation operation)
    {
    	if (testsCategories==null)
    	{
    		// Get current user session Hibernate operation
    		operation=getCurrentUserOperation(operation);
    		
    		User testAuthor=getAuthor(operation);
    		Category category=getCategory(operation);
    		
        	testsCategories=categoriesService.getCategoriesSortedByHierarchy(operation,testAuthor,
        		categoryTypesService.getCategoryType(operation,"CATEGORY_TYPE_TESTS"),true,true,
        		CategoriesService.NOT_VIEW_OTHER_USERS_CATEGORIES);
    		
			// We need to check if test's author is allowed to assign a global category of other user
			// to his/her owned tests
        	if (!getGlobalOtherUserCategoryAllowed(operation).booleanValue())
        	{
				// As question's author is not allowed to assign a global category of other user
				// to his/her owned questions we remove them from results 
        		// (except current test category)
        		removeGlobalOtherUserCategories(testsCategories,testAuthor,category);
        	}
        	
			// We need to check if current user is allowed to see private categories of test's author
    		if (testAuthor.getId()!=userSessionService.getCurrentUserId() && 
    			(!getViewTestsFromOtherUsersPrivateCategoriesEnabled(operation).booleanValue() ||
    			(!getViewTestsFromAdminsPrivateCategoriesEnabled(operation).booleanValue() && 
    			getTestAuthorAdmin(operation).booleanValue()) || 
    			(!getViewTestsFromSuperadminsPrivateCategoriesEnabled(operation).booleanValue() && 
    			getTestAuthorSuperadmin(operation).booleanValue())))
    		{
				// As current user is not allowed to see private categories of test's author
				// we remove them from results (except current test category)
    			removePrivateCategories(operation,testsCategories,testAuthor,category);
    		}
    	}
    	return testsCategories;
	}
    
    /**
	 * @return List of visible categories for questions
	 */
    public List<Category> getQuestionsCategories()
    {
    	return getQuestionsCategories(null);
	}
    
    /**
     * @param operation Operation
	 * @return List of visible categories for questions
	 */
    private List<Category> getQuestionsCategories(Operation operation)
    {
    	if (questionsCategories==null)
    	{
    		// Get current user session Hibernate operation
    		operation=getCurrentUserOperation(operation);
    		
    		User testAuthor=getAuthor(operation);
    		
        	// Get filter value for viewing questions from categories of other users based on permissions
        	// of current user
        	int includeOtherUsersCategories=CategoriesService.NOT_VIEW_OTHER_USERS_CATEGORIES;
        	if (getUseOtherUsersQuestions(operation).booleanValue())
        	{
        		if (getViewQuestionsFromOtherUsersPrivateCategoriesEnabled(operation).booleanValue() &&
        			getTestAuthorViewQuestionsFromOtherUsersPrivateCategoriesEnabled(operation).booleanValue())
       			{
       				includeOtherUsersCategories=CategoriesService.VIEW_OTHER_USERS_ALL_CATEGORIES;
       			}
       			else
       			{
       				includeOtherUsersCategories=CategoriesService.VIEW_OTHER_USERS_PUBLIC_CATEGORIES;
       			}
       		}
    		
        	// In case that current user is allowed to view private categories of other users 
        	// we also need to check if he/she has permission to view private categories of administrators
        	// and/or users with permission to improve permissions over their owned ones (superadmins)
        	boolean includeAdminsPrivateCategories=false;
        	boolean includeSuperadminsPrivateCategories=false;
        	if (includeOtherUsersCategories==CategoriesService.VIEW_OTHER_USERS_ALL_CATEGORIES)
        	{
        		includeAdminsPrivateCategories=
        			getTestAuthorViewQuestionsFromAdminsPrivateCategoriesEnabled(operation).booleanValue();
       			includeSuperadminsPrivateCategories=
       				getTestAuthorViewQuestionsFromSuperadminsPrivateCategoriesEnabled(operation).booleanValue();
       		}
       		
        	questionsCategories=categoriesService.getCategoriesSortedByHierarchy(operation,testAuthor,
        		categoryTypesService.getCategoryType(operation,"CATEGORY_TYPE_QUESTIONS"),true,
        		getUseGlobalQuestions(operation).booleanValue(),includeOtherUsersCategories,
    			includeAdminsPrivateCategories,includeSuperadminsPrivateCategories);
   			
			// We need to check if test's author is allowed to assign a question from a 
    		// global category of other user to his/her owned tests
        	if (!getGlobalOtherUserCategoryAllowed(operation).booleanValue())
        	{
				// As test's author is not allowed to assign a question from a global category 
        		// of other user to his/her owned questions we remove them from results 
        		removeGlobalOtherUserCategories(questionsCategories,testAuthor,null);
        	}
    		
			// We need to check if current user is allowed to see private categories of test's author
        	if (testAuthor.getId()==userSessionService.getCurrentUserId() && 
        		(!getViewQuestionsFromOtherUsersPrivateCategoriesEnabled(operation).booleanValue() || 
        		(getTestAuthorAdmin(operation).booleanValue() && 
        		!getViewQuestionsFromAdminsPrivateCategoriesEnabled(operation).booleanValue()) || 
        		(getTestAuthorSuperadmin(operation).booleanValue() && 
        		!getViewQuestionsFromSuperadminsPrivateCategoriesEnabled(operation).booleanValue())))
        	{
				// As current user is not allowed to see questions from private categories of 
				// test's author we remove them from results
				removePrivateCategories(operation,questionsCategories,testAuthor,null);
        	}
    	}
    	return questionsCategories==null?new ArrayList<Category>():questionsCategories;
    }
    
    /**
     * @return All assessements
     */
    public List<Assessement> getAssessements()
    {
    	return getAssessements(null);
    }
    
    /**
     * @param assessements Assessements
     */
    public void setAssessement(List<Assessement> assessements)
    {
    	this.assessements=assessements;
    }
    
    /**
     * @param operation Operation
     * @return All assessements
     */
    private List<Assessement> getAssessements(Operation operation)
    {
    	if (assessements==null)
    	{
    		assessements=assessementsService.getAssessements(getCurrentUserOperation(operation));
    	}
    	return assessements;
    }
    
    /**
     * @return All score types
     */
    public  List<ScoreType> getScoreTypes()
    {
    	return getScoreTypes(null);
    }
    
    /**
     * @param scoreTypes Score types
     */
    public void setScoreTypes(List<ScoreType> scoreTypes)
    {
    	this.scoreTypes=scoreTypes;
    }
    
    /**
     * @param operation Operation
     * @return All score types
     */
    private List<ScoreType> getScoreTypes(Operation operation)
    {
    	if (scoreTypes==null)
    	{
    		scoreTypes=scoreTypesService.getScoreTypes(getCurrentUserOperation(operation));
    	}
    	return scoreTypes;
    }
    
    /**
     * @return All navigation locations
     */
    public List<NavLocation> getNavLocations()
    {
    	return getNavLocations(null);
    }
    
    /**
     * @param navLocations Navigation locations
     */
    public void setNavLocations(List<NavLocation> navLocations)
    {
    	this.navLocations=navLocations;
    }
    
    /**
     * @param operation Operation
     * @return All navigation locations
     */
    private List<NavLocation> getNavLocations(Operation operation)
    {
    	if (navLocations==null)
    	{
    		navLocations=navLocationsService.getNavLocations(getCurrentUserOperation(operation));
    	}
    	return navLocations;
    }
    
    /**
     * @return All values for property 'redoQuestion'
     */
    public List<RedoQuestionValue> getRedoQuestions()
    {
    	return getRedoQuestions(null);
    }
    
    /**
     * @param redoQuestions Values for property 'redoQuestion'
     */
    public void setRedoQuestions(List<RedoQuestionValue> redoQuestions)
    {
    	this.redoQuestions=redoQuestions;
    }
    
    /**
     * @param operation Operation
     * @return All values for property 'redoQuestion'
     */
    private List<RedoQuestionValue> getRedoQuestions(Operation operation)
    {
    	if (redoQuestions==null)
    	{
    		redoQuestions=redoQuestionValuesService.getRedoQuestions(getCurrentUserOperation(operation));
    	}
    	return redoQuestions;
    }
    
	//Obtiene los niveles de pregunta
    /**
	 * @return Question levels
	 */
    public List<QuestionLevel> getQuestionLevels()
    {
    	if (questionLevels==null)
    	{
    		questionLevels=questionsService.getQuestionLevels();
    	}
		return questionLevels;
	}
    
    /**
     * @return Minimum value for weights
     */
    public int getMinWeight()
    {
    	return MIN_WEIGHT;
    }
    
    /**
     * @return Maximum value for weights
     */
    public int getMaxWeight()
    {
    	return MAX_WEIGHT;
    }
    
    /**
     * @return true if input boxes for sections weights are displayed, false otherwise
     */
    public boolean isSectionsWeightsDisplayed()
    {
    	return isSectionsWeightsDisplayed(null);
    }
    
    /**
     * @param operation Operation
     * @return true if input boxes for sections weights are displayed, false otherwise
     */
    public boolean isSectionsWeightsDisplayed(Operation operation)
    {
    	ScoreType scoreType=getScoreType(operation);
    	return scoreType!=null && "SCORE_TYPE_SECTIONS".equals(scoreType.getType());
    }
    
    /**
     * Checks that user has entered a test name or displays an error message.
     * @param testName Test name
     * @return true if user has entered a test name, false otherwise
     */
    private boolean checkNonEmptyTestName(String testName)
    {
    	boolean ok=true;
    	if (testName==null || testName.equals(""))
    	{
    		addErrorMessage("TEST_NAME_REQUIRED");
    		ok=false;
    	}
    	return ok;
    }
    
	/**
	 * Checks that test name entered by user only includes valid characters or displays an error message.
     * @param testName Test name
	 * @return true if test name only includes valid characters (letters, digits, whitespaces or some 
	 * of the following characters  _ ( ) [ ] { } + - * /<br/>
	 * ), false otherwise
	 */
    private boolean checkValidCharactersForTestName(String testName)
    {
    	boolean ok=true;
    	if (StringUtils.hasUnexpectedCharacters(
        	testName,true,true,true,new char[]{'_','(',')','[',']','{','}','+','-','*','/'}))
    	{
    		addErrorMessage("TEST_NAME_INVALID_CHARACTERS");
    		ok=false;
    	}
    	return ok;
    }
    
    /**
     * Check that test name entered by user includes at least one letter or displays an error message.
     * @param testName Test name
     * @return true if test name includes at least one letter, false otherwise
     */
    private boolean checkLetterIncludedForTestName(String testName)
    {
    	boolean ok=true;
    	if (!StringUtils.hasLetter(testName))
    	{
    		addErrorMessage("TEST_NAME_WITHOUT_LETTER");
    		ok=false;
    	}
    	return ok;
    }
    
    /**
     * Check that first character of the test name entered by user is not a digit nor a whitespace 
     * or displays an error message.
     * @param testName Test name
	 * @return true if first character of test name is not a digit nor a whitespace, false otherwise
     */
    private boolean checkFirstCharacterNotDigitNotWhitespaceForTestName(String testName)
    {
    	boolean ok=true;
    	if (StringUtils.isFirstCharacterDigit(testName) || StringUtils.isFirstCharacterWhitespace(testName))
    	{
    		addErrorMessage("TEST_NAME_FIRST_CHARACTER_INVALID");
    		ok=false;
    	}
    	return ok;
    }
    
    /**
     * Check that last character of the test name entered by user is not a whitespace or displays an error message.
     * @param testName Test name
	 * @return true if last character of test name is not a whitespace, false otherwise
     */
    private boolean checkLastCharacterNotWhitespaceForTestName(String testName)
    {
    	boolean ok=true;
    	if (StringUtils.isLastCharacterWhitespace(testName))
    	{
    		addErrorMessage("TEST_NAME_LAST_CHARACTER_INVALID");
    		ok=false;
    	}
    	return ok;
    }
    
    /**
     * Check that test name entered by user does not include consecutive whitespaces or displays an error message.
     * @param testName Test name
	 * @return true if test name does not include consecutive whitespaces, false otherwise
     */
    private boolean checkNonConsecutiveWhitespacesForTestName(String testName)
    {
    	boolean ok=true;
    	if (StringUtils.hasConsecutiveWhitespaces(testName))
    	{
    		addErrorMessage("TEST_NAME_WITH_CONSECUTIVE_WHITESPACES");
    		ok=false;
    	}
    	return ok;
    }
    
    /**
     * Check that test name entered by user is valid or displays error messages indicating the causes.
     * @param testName Test name
     * @return true if test name entered by user is valid, false otherwise
     */
    private boolean checkTestName(String testName)
    {
    	boolean ok=checkNonEmptyTestName(testName);
    	if (ok)
    	{
    		if (!checkValidCharactersForTestName(testName))
    		{
    			ok=false;
    		}
    		if (!checkLetterIncludedForTestName(testName))
    		{
    			ok=false;
    		}
    		if (!checkFirstCharacterNotDigitNotWhitespaceForTestName(testName))
    		{
    			ok=false;
    		}
    		if (!checkLastCharacterNotWhitespaceForTestName(testName))
    		{
    			ok=false;
    		}
    		if (!checkNonConsecutiveWhitespacesForTestName(testName))
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
    	
    	// Check test name
    	String testName=getName(operation);
    	ok=checkTestName(testName);
    	
        // Check categories
    	if (checkCategory(operation))
    	{
    		if (!checkAvailableTestName(operation))
    		{
    			addErrorMessage("TEST_NAME_ALREADY_DECLARED");
    			ok=false;
    		}
    	}
    	else
        {
        	addErrorMessage("TEST_CATEGORY_ASSIGN_ERROR");
        	ok=false;
        	
        	// Reload test categories from DB
        	setTestsCategories(null);
        }
    	
    	return ok;
    }
    
    private boolean checkCategory(Operation operation)
    {
    	boolean ok=true;
    	try
    	{
   			// Get current user session Hibernate operation
   			operation=getCurrentUserOperation(operation);
    		
    		User testAuthor=getAuthor(operation);
    		Category category=getCategory(operation);
    		
    		if (!getGlobalOtherUserCategoryAllowed(operation).booleanValue() && 
    			!category.getUser().equals(testAuthor) && category.getVisibility().isGlobal())
    		{
    			ok=false;
    		}
    		else if (testAuthor.getId()==userSessionService.getCurrentUserId() && 
    			category.getUser().equals(testAuthor))
    		{
				Visibility testCategoryVisibility=category.getVisibility();
    			if (!testCategoryVisibility.isGlobal() && testCategoryVisibility.getLevel()>=
    				visibilitiesService.getVisibility(operation,"CATEGORY_VISIBILITY_PRIVATE").getLevel())
    			{
    				ok=getViewTestsFromOtherUsersPrivateCategoriesEnabled(operation).booleanValue() &&
    					(!getTestAuthorAdmin(operation).booleanValue() || 
    					getViewTestsFromAdminsPrivateCategoriesEnabled(operation).booleanValue())&&
    					(!getTestAuthorSuperadmin(operation).booleanValue() || 
    					getViewTestsFromSuperadminsPrivateCategoriesEnabled(operation).booleanValue());
    			}
    		}
    	}
    	catch (ServiceException se)
    	{
    		ok=false;
		}
    	return ok;
    }
    
    /**
     * @param operation Operation
	 * @return true if test name entered by user is available, false otherwise 
	 */
	private boolean checkAvailableTestName(Operation operation)
	{
		boolean ok=true;
		
		// Get current user session Hibernate operation
		operation=getCurrentUserOperation(operation);
		
		String testName=getName(operation);
		long categoryId=getCategoryId(operation);
		if (testName!=null)
		{
			ok=testsService.isTestNameAvailable(operation,testName,categoryId,getTestId(operation));
		}
		return ok;
	}
    
    private boolean checkCalendarInputFields(Operation operation)
    {
    	boolean ok=true;
    	
    	// Get current user session Hibernate operation
    	operation=getCurrentUserOperation(operation);
    	
   		if (isRestrictDates(operation))
   		{
   			String startDateHidden=getStartDateHidden();
   			String closeDateHidden=getCloseDateHidden();
   			String warningDateHidden=getWarningDateHidden();
   			Date startDate=getStartDate(operation);
   			Date closeDate=getCloseDate(operation);
   			Date warningDate=getWarningDate(operation);
   			if (startDate!=null && closeDate!=null && !closeDate.after(startDate))
   			{
  					addErrorMessage("TEST_CLOSE_DATE_NOT_AFTER_START_DATE");
   				ok=false;
   			}
   			if (warningDate!=null)
   			{
       			if (startDate!=null && warningDate.before(startDate))
       			{
   					addErrorMessage("TEST_WARNING_DATE_BEFORE_START_DATE");
       				ok=false;
       			}
   				if (closeDate!=null && !warningDate.before(closeDate))
   				{
   					addErrorMessage("TEST_WARNING_DATE_NOT_BEFORE_CLOSE_DATE");
       				ok=false;
   				}
   			}
   			if (isRestrictFeedbackDate(operation))
   			{
   				String feedbackDateHidden=getFeedbackDateHidden();
   				Date feedbackDate=getFeedbackDate(operation);
   				if (feedbackDate!=null)
   				{
   					if (startDate!=null && feedbackDate.before(startDate))
   					{
						addErrorMessage("TEST_FEEDBACK_DATE_BEFORE_START_DATE");
   						ok=false;
   					}
   					if (closeDate!=null && !feedbackDate.before(closeDate))
  					{
						addErrorMessage("TEST_FEEDBACK_DATE_NOT_BEFORE_CLOSE_DATE");
   						ok=false;
   					}
   				}
   				setFeedbackDateHidden(feedbackDateHidden);
   			}
   			setStartDateHidden(startDateHidden);
   			setCloseDateHidden(closeDateHidden);
   			setWarningDateHidden(warningDateHidden);
   		}
		return ok;
    }
	
	/**
	 * Check that current section name entered by user only includes valid characters or displays an error message.
     * @param sectionName Current section name
     * @param displayError true to display error message, false otherwise
	 * @return true if current section name only includes valid characters (letters, digits, whitespaces 
	 * or some of the following characters  _ + - * / /<br/>
	 * ), false otherwise
	 */
    private boolean checkValidCharactersForSectionName(String sectionName,boolean displayError)
    {
    	boolean ok=true;
    	if (StringUtils.hasUnexpectedCharacters(sectionName,true,true,true,new char[]{'_','+','-','*','/'}))
    	{
    		if (displayError)
    		{
    			addErrorMessage("SECTION_NAME_INVALID_CHARACTERS");
    		}
    		ok=false;
    	}
    	return ok;
    }
    
    /**
     * Check that first character of the current section name entered by user is not a whitespace 
     * or displays an error message.
     * @param sectionName Current section name
     * @param displayError true to display error message, false otherwise
	 * @return true if first character of current section name is not a whitespace, false otherwise
     */
    private boolean checkFirstCharacterNotWhitespaceForSectionName(String sectionName,boolean displayError)
    {
    	boolean ok=true;
    	if (StringUtils.isFirstCharacterWhitespace(sectionName))
    	{
    		if (displayError)
    		{
    			addErrorMessage("SECTION_NAME_FIRST_CHARACTER_INVALID");
    		}
    		ok=false;
    	}
    	return ok;
    }
    
    /**
     * Check that last character of the current section name entered by user is not a whitespace 
     * or displays an error message.
     * @param sectionName Current section name
     * @param displayError true to display error message, false otherwise
	 * @return true if last character of current answer name is not a whitespace, false otherwise
     */
    private boolean checkLastCharacterNotWhitespaceForSectionName(String sectionName,boolean displayError)
    {
    	boolean ok=true;
    	if (StringUtils.isLastCharacterWhitespace(sectionName))
    	{
    		if (displayError)
    		{
    			addErrorMessage("SECTION_NAME_LAST_CHARACTER_INVALID");
    		}
    		ok=false;
    	}
    	return ok;
    }
    
    /**
     * Check that current section name entered by user does not include consecutive whitespaces 
     * or displays an error message.
     * @param sectionName Current section name
     * @param displayError true to display error message, false otherwise
	 * @return true if current section name does not include consecutive whitespaces, false otherwise
     */
    private boolean checkNonConsecutiveWhitespacesForSectionName(String sectionName,boolean displayError)
    {
    	boolean ok=true;
    	if (StringUtils.hasConsecutiveWhitespaces(sectionName))
    	{
    		if (displayError)
    		{
    			addErrorMessage("SECTION_NAME_WITH_CONSECUTIVE_WHITESPACES");
    		}
    		ok=false;
    	}
    	return ok;
    }
    
    /**
     * Check that current section name entered by user is valid or displays error messages indicating the causes if
     * desired.
     * @param sectionName Current section name
     * @param displayErrors true to display error messages, false otherwise
     * @return true if current section name entered by user is valid, false otherwise
     */
    private boolean checkSectionName(String sectionName,boolean displayErrors)
    {
    	boolean ok=true;
    	if (displayErrors)
    	{
       		if (!checkValidCharactersForSectionName(sectionName,true))
       		{
       			ok=false;
       		}
       		if (!checkFirstCharacterNotWhitespaceForSectionName(sectionName,true))
       		{
       			ok=false;
       		}
       		if (!checkLastCharacterNotWhitespaceForSectionName(sectionName,true))
       		{
       			ok=false;
       		}
       		if (!checkNonConsecutiveWhitespacesForSectionName(sectionName,true))
       		{
       			ok=false;
       		}
    	}
    	else
    	{
    		ok=checkValidCharactersForSectionName(sectionName,false) && 
    			checkFirstCharacterNotWhitespaceForSectionName(sectionName,false) &&
    			checkLastCharacterNotWhitespaceForSectionName(sectionName,false) &&
    			checkNonConsecutiveWhitespacesForSectionName(sectionName,false);
    	}
    	return ok;
    }
    
    /**
     * Check that current section name entered by user is valid or displays error messages indicating the causes.
     * @param sectionName Current section name
     * @return true if current section name entered by user is valid, false otherwise
     */
    private boolean checkSectionName(String sectionName)
    {
    	return checkSectionName(sectionName,true);
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
    	if (GENERAL_WIZARD_TAB.equals(oldStep))
    	{
    		// Get current user session Hibernate operation
    		Operation operation=getCurrentUserOperation(null);
    		
    		setGlobalOtherUserCategoryAllowed(null);
    		resetTestAuthorAdmin(operation);
    		resetTestAuthorSuperadmin(operation);
    		setViewTestsFromOtherUsersPrivateCategoriesEnabled(null);
    		setViewTestsFromAdminsPrivateCategoriesEnabled(null);
    		setViewTestsFromSuperadminsPrivateCategoriesEnabled(null);
    		if (!checkCommonDataInputFields(operation))
    		{
    			ok=false;
    		}
    		if (activeGeneralTabIndex==CALENDAR_TAB_GENERAL_ACCORDION && !checkCalendarInputFields(operation))
    		{
    			ok=false;
    		}
    	}
    	else if (PRESENTATION_WIZARD_TAB.equals(oldStep))
    	{
    		if (SECTIONS_WIZARD_TAB.equals(nextStep))
    		{
    			// Get current section
    			SectionBean currentSection=getSection(getCurrentUserOperation(null),activeSectionIndex+1);
    			
    			if (currentSection!=null)
    			{
    				// Restore old section name
    				currentSection.setName(activeSectionName);
    			}
    		}
    	}
    	else if (SECTIONS_WIZARD_TAB.equals(oldStep))
    	{
    		// Get current user session Hibernate operation
    		Operation operation=getCurrentUserOperation(null);
    		
    		boolean errors=false;
			boolean displayErrors=(isNavigation()?PRELIMINARY_SUMMARY_WIZARD_TAB:FEEDBACK_WIZARD_TAB).equals(nextStep);
        	boolean needConfirm=false;
        	sectionChecked=getSection(operation,activeSectionIndex+1);
        	if (sectionChecked!=null)
        	{
        		// Check that current section name entered by user is valid
        		if (checkSectionName(sectionChecked.getName(),displayErrors))
        		{
        			activeSectionName=sectionChecked.getName();
        		}
        		else
        		{
        			errors=true;
        			if (displayErrors)
        			{
        				// Restore old answer tab without changing its name
        				updateSectionsTextFields(operation,event.getComponent(),getSectionsSize(operation));
        				sectionChecked.setName(activeSectionName);
        			}
        		}
       			boolean checkFeedbacks=sectionChecked.getWeight()>=getMinWeight() &&
   					sectionChecked.getWeight()<=getMaxWeight() && sectionChecked.getRandomQuantity()>=0 && 
   					sectionChecked.getRandomQuantity()<=sectionChecked.getQuestionOrdersSize() &&
   					(questionOrderChecked==null || (questionOrderChecked.getWeight()>=getMinWeight() && 
   					questionOrderChecked.getWeight()<=getMaxWeight()));
   				if (checkFeedbacks)
   				{
   					checkFeedbacks=checkFeedbacksForChangeProperty(operation);
   					needConfirm=!checkFeedbacks;
   				}
   				else
   				{
   					needConfirm=false;
   				}
        		if (needConfirm)
        		{
        			nextTestTabNameOnChangePropertyConfirm=nextStep;
        			nextStep=oldStep;
        		}
        		else
    			{
    				if (sectionChecked.getWeight()<getMinWeight())
    				{
    					if (checkFeedbacks)
    					{
    						sectionChecked.setWeight(getMinWeight());
    					}
    					else
    					{
    						sectionChecked.rollbackWeight();
    					}
    					
       					// We need to update section weight
       					updateSectionWeight(operation,event.getComponent(),sectionChecked);
   					}
   					else if (sectionChecked.getWeight()>getMaxWeight())
   					{
   						sectionChecked.setWeight(getMaxWeight());
   						
    					// We need to update section weight
       					updateSectionWeight(operation,event.getComponent(),sectionChecked);
    				}
    				sectionChecked.acceptWeight();
    				if (sectionChecked.getRandomQuantity()<0)
    				{
    					if (checkFeedbacks)
    					{
    						sectionChecked.setRandomQuantity(sectionChecked.getQuestionOrdersSize()==0?0:1);
    					}
    					else
    					{
    						sectionChecked.rollbackRandomQuantity();
    					}
    					
    					// We need to update random quantity
    					updateSectionRandomQuantity(operation,event.getComponent(),sectionChecked);
    				}
    				else if (sectionChecked.getRandomQuantity()==0 && sectionChecked.getQuestionOrdersSize()>0)
    				{
    					if (checkFeedbacks)
    					{
    						sectionChecked.setRandomQuantity(1);
    					}
    					else
    					{
    						sectionChecked.rollbackRandomQuantity();
    					}
    					
    					// We need to update random quantity
    					updateSectionRandomQuantity(operation,event.getComponent(),sectionChecked);
    				}
    				else if (sectionChecked.getRandomQuantity()>sectionChecked.getQuestionOrdersSize())
    				{
    					sectionChecked.setRandomQuantity(sectionChecked.getQuestionOrdersSize());
    					
    					// We need to update random quantity
   						updateSectionRandomQuantity(operation,event.getComponent(),sectionChecked);
   					}
   					sectionChecked.acceptRandomQuantity();
   					if (questionOrderChecked!=null)
   					{
   						if (questionOrderChecked.getWeight()<getMinWeight())
   						{
   							if (checkFeedbacks)
   							{
   								questionOrderChecked.setWeight(getMinWeight());
   							}
   							else
   							{
   								questionOrderChecked.rollbackWeight();
   							}
   							
    						// We need to update questions weights
    						updateQuestionOrderWeights(operation,event.getComponent(),sectionChecked);
   						}
   						else if (questionOrderChecked.getWeight()>getMaxWeight())
   						{
   							questionOrderChecked.setWeight(getMaxWeight());
   							
    						// We need to update questions weights
   							updateQuestionOrderWeights(operation,event.getComponent(),sectionChecked);
   						}
       					questionOrderChecked=null;
   					}
   					sectionChecked=null;
   				}
       		}
    		
    		ok=!displayErrors || !errors;    		
    	}
    	if (!ok)
    	{
    		nextStep=oldStep;
    		
    		// Reset user permissions
    		setUseGlobalQuestions(null);
    		setUseOtherUsersQuestions(null);
    		setViewQuestionsFromOtherUsersPrivateCategoriesEnabled(null);
    		setViewQuestionsFromAdminsPrivateCategoriesEnabled(null);
    		setViewQuestionsFromSuperadminsPrivateCategoriesEnabled(null);
    		setTestAuthorViewQuestionsFromOtherUsersPrivateCategoriesEnabled(null);
    		setTestAuthorViewQuestionsFromAdminsPrivateCategoriesEnabled(null);
    		setTestAuthorViewQuestionsFromSuperadminsPrivateCategoriesEnabled(null);
    		resetAdmins();
    		resetSuperadmins();
    		
			// Scroll page to top position
			scrollToTop();
    	}
    	if (GENERAL_WIZARD_TAB.equals(nextStep))
    	{
    		setTestsCategories(null);
    	}
    	setEnabledCheckboxesSetters(!CONFIRMATION_WIZARD_TAB.equals(nextStep));    	
    	activeTestTabName=nextStep;
    	return nextStep;
    }
    
    public String getActiveTestTabName()
    {
    	return getActiveTestTabName(null);
    }
    
    private String getActiveTestTabName(Operation operation)
    {
    	String activeTestTabName=null;
    	if (getTestId(getCurrentUserOperation(operation))>0L)
    	{
			switch (activeTestIndex)
			{
				case GENERAL_TABVIEW_TAB:
					activeTestTabName=GENERAL_WIZARD_TAB;
					break;
				case PRESENTATION_TABVIEW_TAB:
					activeTestTabName=PRESENTATION_WIZARD_TAB;
					break;
				case SECTIONS_TABVIEW_TAB:
					activeTestTabName=SECTIONS_WIZARD_TAB;
					break;
				default:
					if (activeTestIndex==preliminarySummaryTabviewTab)
					{
						activeTestTabName=PRELIMINARY_SUMMARY_WIZARD_TAB;
					}
					else if (activeTestIndex==feedbackTabviewTab)
					{
						activeTestTabName=FEEDBACK_WIZARD_TAB;
					}
			}
    	}
    	else
    	{
    		activeTestTabName=this.activeTestTabName;
    	}
    	return activeTestTabName;
    }
    
	/**
	 * Tab change listener for displaying other tab of a test.
	 * @param event Tab change event
	 */
    public void changeActiveTestTab(TabChangeEvent event)
    {
		boolean ok=true;
    	TabView testFormTabs=(TabView)event.getComponent();
        if (activeTestIndex==GENERAL_TABVIEW_TAB)
        {
    		// Get current user session Hibernate operation
    		Operation operation=getCurrentUserOperation(null);
        	
        	// We need to process some input fields
        	processCommonDataInputFields(operation,testFormTabs);
        	if (activeGeneralTabIndex==CALENDAR_TAB_GENERAL_ACCORDION)
        	{
        		processCalendarTabCommonDataInputFields(operation,testFormTabs);
        	}
        	
        	setGlobalOtherUserCategoryAllowed(null);
        	resetTestAuthorAdmin(operation);
        	resetTestAuthorSuperadmin(operation);
       		setViewTestsFromOtherUsersPrivateCategoriesEnabled(null);
       		setViewTestsFromAdminsPrivateCategoriesEnabled(null);
       		setViewTestsFromSuperadminsPrivateCategoriesEnabled(null);
       		if (!checkCommonDataInputFields(operation))
       		{
       			ok=false;
       		}
       		if (activeGeneralTabIndex==CALENDAR_TAB_GENERAL_ACCORDION && !checkCalendarInputFields(operation))
       		{
       			ok=false;
       		}
           	if (ok)
           	{
            	activeTestIndex=testFormTabs.getActiveIndex();
           	}
           	else
           	{
           		testFormTabs.setActiveIndex(activeTestIndex);
           		
               	// Reset user permissions
           		setUseGlobalQuestions(null);
           		setUseOtherUsersQuestions(null);
           		setViewQuestionsFromOtherUsersPrivateCategoriesEnabled(null);
           		setViewQuestionsFromAdminsPrivateCategoriesEnabled(null);
           		setViewQuestionsFromSuperadminsPrivateCategoriesEnabled(null);
           		setTestAuthorViewQuestionsFromOtherUsersPrivateCategoriesEnabled(null);
           		setTestAuthorViewQuestionsFromAdminsPrivateCategoriesEnabled(null);
           		setTestAuthorViewQuestionsFromSuperadminsPrivateCategoriesEnabled(null);
               	resetAdmins();
               	resetSuperadmins();
           	}
       	}
      	else if (activeTestIndex==PRESENTATION_TABVIEW_TAB)
       	{
       		processPresentationEditorField(testFormTabs);
       		activeTestIndex=testFormTabs.getActiveIndex();
       	}
       	else if (activeTestIndex==SECTIONS_TABVIEW_TAB)
       	{
       		// Get current user session Hibernate operation
       		Operation operation=getCurrentUserOperation(null);
       		
       		boolean needConfirm=false;
       		sectionChecked=getSection(operation,activeSectionIndex+1);
       		
       		// We need to process some input fields
       		processSectionsInputFields(operation,testFormTabs,sectionChecked);
       		
       		if (sectionChecked!=null)
       		{
       			// Check that current section name entered by user is valid
       			if (checkSectionName(sectionChecked.getName()))
       			{
       				activeSectionName=sectionChecked.getName();
       			}
       			else
       			{
       				// Restore old section name
       				sectionChecked.setName(activeSectionName);
       				
       				ok=false;
       			}
       			boolean checkFeedbacks=sectionChecked.getWeight()>=getMinWeight() && 
       				sectionChecked.getWeight()<=getMaxWeight() && sectionChecked.getRandomQuantity()>=0 && 
       				sectionChecked.getRandomQuantity()<=sectionChecked.getQuestionOrdersSize() && 
       				(questionOrderChecked==null || (questionOrderChecked.getWeight()>=getMinWeight() && 
       				questionOrderChecked.getWeight()<=getMaxWeight()));
       			if (checkFeedbacks)
       			{
       				checkFeedbacks=checkFeedbacksForChangeProperty(operation);
       				needConfirm=!checkFeedbacks;
       			}
       			else
       			{
       				needConfirm=false;
       			}
       			if (needConfirm)
       			{
       				nextTestIndexOnChangePropertyConfirm=testFormTabs.getActiveIndex();
       				testFormTabs.setActiveIndex(activeTestIndex);
       			}
       			else
       			{
       				if (sectionChecked.getWeight()<getMinWeight())
       				{
       					if (checkFeedbacks)
       					{
       						sectionChecked.setWeight(getMinWeight());
       					}
       					else
       					{
       						sectionChecked.rollbackWeight();
       					}
       					
       					// We need to update section weight
      					updateSectionWeight(operation,event.getComponent(),sectionChecked);
       				}
       				else if (sectionChecked.getWeight()>getMaxWeight())
       				{
       					sectionChecked.setWeight(getMaxWeight());
       					
       					// We need to update section weights
       					updateSectionWeight(operation,event.getComponent(),sectionChecked);
       				}
       				sectionChecked.acceptWeight();
       				if (sectionChecked.getRandomQuantity()<0)
       				{
       					if (checkFeedbacks)
       					{
       						sectionChecked.setRandomQuantity(sectionChecked.getQuestionOrdersSize()==0?0:1);
       					}
       					else
       					{
       						sectionChecked.rollbackRandomQuantity();
       					}
       					
       					// We need to update random quantity
       					updateSectionRandomQuantity(operation,event.getComponent(),sectionChecked);
       				}
       				else if (sectionChecked.getRandomQuantity()==0 && sectionChecked.getQuestionOrdersSize()>0)
      				{
       					if (checkFeedbacks)
       					{
       						sectionChecked.setRandomQuantity(1);
       					}
       					else
       					{
       						sectionChecked.rollbackRandomQuantity();
       					}
       					
       					// We need to update random quantity
       					updateSectionRandomQuantity(operation,event.getComponent(),sectionChecked);
       				}
       				else if (sectionChecked.getRandomQuantity()>sectionChecked.getQuestionOrdersSize())
       				{
       					sectionChecked.setRandomQuantity(sectionChecked.getQuestionOrdersSize());
       					
       					// We need to update random quantity
       					updateSectionRandomQuantity(operation,event.getComponent(),sectionChecked);
       				}
       				sectionChecked.acceptRandomQuantity();
       				if (questionOrderChecked!=null)
       				{
       					if (questionOrderChecked.getWeight()<getMinWeight())
       					{
       						if (checkFeedbacks)
       						{
       							questionOrderChecked.setWeight(getMinWeight());
       						}
       						else
       						{
       							questionOrderChecked.rollbackWeight();
       						}
       						
       						// We need to update questions weights
       						updateQuestionOrderWeights(operation,event.getComponent(),sectionChecked);
       					}
       					else if (questionOrderChecked.getWeight()>getMaxWeight())
       					{
       						questionOrderChecked.setWeight(getMaxWeight());
       						
       						// We need to update questions weights
       						updateQuestionOrderWeights(operation,event.getComponent(),sectionChecked);
       					}
       					for (QuestionOrderBean questionOrder:sectionChecked.getQuestionOrders())
       					{
       						questionOrder.acceptWeight();
       					}
       				questionOrderChecked=null;
       				}
       				sectionChecked=null;
       			}
       		}
       		
       		if (ok)
       		{
       			activeTestIndex=testFormTabs.getActiveIndex();
       		}
       		else
       		{
       			testFormTabs.setActiveIndex(activeTestIndex);
       			
       			// Scroll page to top position
       			scrollToTop();
       		}
       	}
       	else if (isNavigation() && activeTestIndex==PRELIMINARY_SUMMARY_OR_FEEDBACK_TABVIEW_TAB)
       	{
       		processPreliminarySummaryEditorField(testFormTabs);
       		activeTestIndex=testFormTabs.getActiveIndex();
       	}
       	else if (activeTestIndex>=PRELIMINARY_SUMMARY_OR_FEEDBACK_TABVIEW_TAB)
       	{
       		processFeedbackEditorInputFields(getCurrentUserOperation(null),testFormTabs);
       		activeTestIndex=testFormTabs.getActiveIndex();
       	}
       	else
       	{
       		activeTestIndex=testFormTabs.getActiveIndex();
       	}
        if (!ok)
        {
        	// Scroll page to top position
        	scrollToTop();
        }
    }
    
	/**
	 * Ajax listener.<br/><br/>
	 * I have defined a tab change listener for accordion of the common data tab of the test and that accordion 
	 * is inside the test's tabview.<br/><br/>
	 * Curiosly when I change the test's tab, Primefaces fires the listener defined for the accordion but 
	 * calls it with an AjaxBehaviourEvent argument.<br/><br/>
	 * As this listener is called unintentionally we have defined it only to avoid an error message and 
	 * it does nothing.
	 * @param event Ajax event
	 */
	public void changeActiveGeneralTab(AjaxBehaviorEvent event)
	{
	}
	
	/**
	 * Tab change listener for displaying other tab within accordion of the common data tab of the test.
	 * @param event Tab change event
	 */
	public void changeActiveGeneralTab(TabChangeEvent event)
	{
		boolean ok=true;
    	
		// Get current user session Hibernate operation
		Operation operation=getCurrentUserOperation(null);
		
		// We need to process some input fields
		processCommonDataInputFields(operation,event.getComponent());
    	
		ok=checkCommonDataInputFields(operation);
    	
    	AccordionPanel generalAccordion=(AccordionPanel)event.getComponent();
    	if (activeGeneralTabIndex==CALENDAR_TAB_GENERAL_ACCORDION)
    	{
    		if (!checkCalendarInputFields(operation))
    		{
    			ok=false;
    		}
    	}
    	if (ok)
    	{
    		try
    		{
    			activeGeneralTabIndex=Integer.parseInt(generalAccordion.getActiveIndex());
    		}
    		catch (NumberFormatException nfe)
    		{
    			activeGeneralTabIndex=-1;
    		}
    	}
    	else
    	{
    		// Restore old general accordion tab
    		refreshActiveGeneralTab(operation,generalAccordion);
    	
    		// Scroll page to top position
    		scrollToTop();
    	}
	}
    
	/**
	 * Ajax listener.<br/><br/>
	 * I have defined a tab change listener for sections accordion and that accordion is inside the
	 * test's tabview.<br/><br/>
	 * Curiosly when I change the test's tab, Primefaces fires the listener defined for the accordion but 
	 * calls it with an AjaxBehaviourEvent argument.<br/><br/>
	 * As this listener is called unintentionally we have defined it only to avoid an error message and 
	 * it does nothing.
	 * @param event Ajax event
	 */
	public void changeActiveSection(AjaxBehaviorEvent event)
	{
	}
	
	/**
	 * Tab change listener for displaying other section within sections accordion.
	 * @param event Tab change event
	 */
	public void changeActiveSection(TabChangeEvent event)
	{
		// Get current user session Hibernate operation
		Operation operation=getCurrentUserOperation(null);
		
    	AccordionPanel sectionsAccordion=(AccordionPanel)event.getComponent();
   		boolean ok=true;
   		boolean needConfirm=false;
   		int oldSectionsAccordionRowIndex=sectionsAccordion.getRowIndex();
   		sectionChecked=getSectionFromSectionsAccordion(operation,sectionsAccordion);
   		if (sectionChecked!=null)
   		{
       		// We need to process some input fields
       		processSectionsInputFields(operation,event.getComponent(),sectionChecked);
       		
			// We need to update all section weights
			updateSectionWeights(operation,sectionsAccordion,sectionChecked);
       		
   			// Check that current section name entered by user is valid
   			if (checkSectionName(sectionChecked.getName()))
   			{
   				boolean checkFeedbacks=sectionChecked.getWeight()>=getMinWeight() &&
   					sectionChecked.getWeight()<=getMaxWeight() && sectionChecked.getRandomQuantity()>=0 && 
   					sectionChecked.getRandomQuantity()<=sectionChecked.getQuestionOrdersSize() &&
   					(questionOrderChecked==null || (questionOrderChecked.getWeight()>=getMinWeight() && 
   					questionOrderChecked.getWeight()<=getMaxWeight()));
   				if (checkFeedbacks)
   				{
   					checkFeedbacks=checkFeedbacksForChangeProperty(operation);
   					needConfirm=!checkFeedbacks;
   				}
   				else
   				{
   					needConfirm=false;
   				}
       			if (!needConfirm)
   				{
       				if (sectionChecked.getWeight()<getMinWeight())
       				{
       					if (checkFeedbacks)
       					{
       						sectionChecked.setWeight(getMinWeight());
       					}
       					else
       					{
       						sectionChecked.rollbackWeight();
       					}
       					
       					// We need to update section weight
       					updateSectionWeight(operation,sectionsAccordion,sectionChecked);
       				}
       				else if (sectionChecked.getWeight()>getMaxWeight())
       				{
       					sectionChecked.setWeight(getMaxWeight());
       					
       					// We need to update section weight
       					updateSectionWeight(operation,sectionsAccordion,sectionChecked);
       				}
       				sectionChecked.acceptWeight();
   					if (sectionChecked.getRandomQuantity()<0)
   					{
   						if (checkFeedbacks)
   						{
   							sectionChecked.setRandomQuantity(sectionChecked.getQuestionOrdersSize()==0?0:1);
   						}
   						else
   						{
   							sectionChecked.rollbackRandomQuantity();
   						}
   						
   						// We need to update random quantity
   						updateSectionRandomQuantity(operation,sectionsAccordion,sectionChecked);
   					}
   					else if (sectionChecked.getRandomQuantity()==0 && sectionChecked.getQuestionOrdersSize()>0)
   					{
   						if (checkFeedbacks)
   						{
   							sectionChecked.setRandomQuantity(1);
   						}
   						else
   						{
   							sectionChecked.rollbackRandomQuantity();
   						}
   						
   						// We need to update random quantity
   						updateSectionRandomQuantity(operation,sectionsAccordion,sectionChecked);
   					}
   					else if (sectionChecked.getRandomQuantity()>sectionChecked.getQuestionOrdersSize())
   					{
   						sectionChecked.setRandomQuantity(sectionChecked.getQuestionOrdersSize());
   						
   						// We need to update random quantity
   						updateSectionRandomQuantity(operation,sectionsAccordion,sectionChecked);
   					}
   					sectionChecked.acceptRandomQuantity();
   					if (questionOrderChecked!=null)
   					{
   						if (questionOrderChecked.getWeight()<=getMinWeight())
   						{
   							if (checkFeedbacks)
   							{
   								questionOrderChecked.setWeight(getMinWeight());
   							}
   							else
   							{
   								questionOrderChecked.rollbackWeight();
   							}
   							
   							// We need to update questions weights
   							updateQuestionOrderWeights(operation,sectionsAccordion,sectionChecked);
   						}
   						else if (questionOrderChecked.getWeight()<=getMinWeight())
   						{
   							questionOrderChecked.setWeight(getMinWeight());
   							
   							// We need to update questions weights
   							updateQuestionOrderWeights(operation,sectionsAccordion,sectionChecked);
   						}
   						for (QuestionOrderBean questionOrder:sectionChecked.getQuestionOrders())
   						{
   							questionOrder.acceptWeight();
   						}
   						questionOrderChecked=null;
   					}
					sectionChecked=null;
   				}
   			}
   			else
   			{
   				ok=false;
   				
   				// Restore old section tab without changing its name
   				updateSectionsTextFields(operation,sectionsAccordion,getSectionsSize(operation));
   				sectionChecked.setName(activeSectionName);
   				refreshActiveSection(operation,sectionsAccordion);
   				
   				// Scroll page to top position
   				scrollToTop();
   			}
   		}
   		if (ok)
   		{
   			if (needConfirm && getPropertyChecked()!=null)
   			{
   				try
   				{
       				nextSectionIndexOnChangePropertyConfirm=Integer.parseInt(sectionsAccordion.getActiveIndex());
   				}
   				catch (NumberFormatException nfe)
   				{
       				nextSectionIndexOnChangePropertyConfirm=-1;
   				}
   				activeSectionIndex=sectionChecked.getOrder()-1;
   				activeSectionName=sectionChecked.getName();
   				refreshActiveSection(operation,sectionsAccordion);
   	    		sectionsAccordion.setRowIndex(oldSectionsAccordionRowIndex);
       			RequestContext rq=RequestContext.getCurrentInstance();
       			rq.execute("confirmChangePropertyDialog.show()");
   			}
   			else
   			{
   				try
   				{
   					activeSectionIndex=Integer.parseInt(sectionsAccordion.getActiveIndex());
   					activeSectionName=getSectionFromSectionsAccordion(operation,sectionsAccordion).getName();
   				}
   				catch (NumberFormatException nfe)
    				{
   					activeSectionIndex=-1;
   					activeSectionName="";
   				}
       			sectionsAccordion.setRowIndex(oldSectionsAccordionRowIndex);
   			}
   		}
   		else
   		{
   			sectionsAccordion.setRowIndex(oldSectionsAccordionRowIndex);
   		}
	}
    
	/**
	 * Change section's weight to a valid value.
	 * @param section Section
	 */
	private void changeSectionWeight(SectionBean section)
	{
		if (section.getWeight()<getMinWeight())
		{
			section.setWeight(getMinWeight());
		}
		else if (section.getWeight()>getMaxWeight())
		{
			section.setWeight(getMaxWeight());
		}
	}
	
	/**
	 * Change question's weight to a valid value.
	 * @param questionOrder Question reference
	 */
	private void changeQuestionOrderWeight(QuestionOrderBean questionOrder)
	{
		if (questionOrder.getWeight()<getMinWeight())
		{
			questionOrder.setWeight(getMinWeight());
		}
		else if (questionOrder.getWeight()>getMaxWeight())
		{
			questionOrder.setWeight(getMaxWeight());
		}
	}
	
	/**
	 * Display a dialog to add questions to a section. 
     * @param event Action event
	 */
	public void showAddQuestions(ActionEvent event)
	{
    	String property=getPropertyChecked();
    	if (property==null)
    	{
    		// Get current user session Hibernate operation
    		Operation operation=getCurrentUserOperation(null);
    		
    		// Get section to process if any
        	AccordionPanel sectionsAccordion=getSectionsAccordion(operation,event.getComponent());
    		setCurrentSection(getSectionFromSectionsAccordion(operation,sectionsAccordion));
    		
    		// We need to process some input fields
    		processSectionsInputFields(operation,event.getComponent(),getCurrentSection());
    		
			// Check that current section name entered by user is valid
    		if (checkSectionName(getCurrentSection().getName()))
    		{
    			activeSectionName=getCurrentSection().getName();
    			
    			// Display dialog
        		RequestContext rq=RequestContext.getCurrentInstance();
        		rq.execute("addQuestionsDialog.show()");
    		}
    		else
    		{
				// Restore old section name
    			getCurrentSection().setName(activeSectionName);
    		}
    	}
    	else if ("sectionWeight".equals(property))
    	{
			// We need to process weight
    		processSectionWeight(getCurrentUserOperation(null),event.getComponent(),sectionChecked);
    		
    		// We need to stop this response because there is another one in process
    		FacesContext.getCurrentInstance().responseComplete();
    	}
    	else if ("randomQuantity".equals(property))
    	{
			// We need to process random quantity
    		processSectionRandomQuantity(getCurrentUserOperation(null),event.getComponent(),sectionChecked);
    		
    		// We need to stop this response because there is another one in process
    		FacesContext.getCurrentInstance().responseComplete();
    	}
    	else if ("questionOrderWeight".equals(property))
    	{
    		// We need to process question orders weights
    		processQuestionOrderWeights(getCurrentUserOperation(null),event.getComponent(),sectionChecked);
    		
    		// We need to stop this response because there is another one in process
    		FacesContext.getCurrentInstance().responseComplete();
    	}
	}
    
    /**
     * Add questions selected within dialog to a section.
     * @param event Action event
     */
    public void acceptAddQuestions(ActionEvent event)
    {
    	SectionBean currentSection=getCurrentSection();
    	if (currentSection!=null)
    	{
        	// Get current user session Hibernate operation
        	Operation operation=getCurrentUserOperation(null);
    		
        	if (questionsDualList!=null)
        	{
        		int order=currentSection.getQuestionOrdersSize()+1;
        		for (Question question:questionsDualList.getTarget())
        		{
        			currentSection.addQuestionOrder(order,question.getId());
        			order++;
        		}
        		questionsDualList=null;
        		
        		// Update random quantity if needed
        		if (currentSection.getRandomQuantity()==0 && currentSection.getQuestionOrdersSize()>0)
        		{
        			currentSection.setRandomQuantity(1);
        			currentSection.acceptRandomQuantity();
        			
    				// We need to update random quantity
    				updateSectionRandomQuantity(operation,event.getComponent(),currentSection);
        		}
        	}
        	
    		// We need to update sections text fields
    		updateSectionsTextFields(operation,event.getComponent(),getSectionsSize(operation));
    	}
    }
    
	/**
	 * @param operation Operation
	 * @param questionCategory Question category
	 * @return true if user has permissions to display questions with the indicated category (not a filter), 
	 * false otherwise
	 */
	private boolean checkQuestionCategoryFilterPermission(Operation operation,Category questionCategory)
	{
		boolean ok=true;
		
		// Get current user session Hibernate operation
		operation=getCurrentUserOperation(operation);
		
		if (questionCategory.getVisibility().isGlobal())
		{
			// This is a global category, so we check that test's author has permissions to use
			// questions from global categories in their tests
			if (getUseGlobalQuestions(operation).booleanValue())
			{
				// Moreover we need to check that the category is owned by test's author or 
				// that test's author has permission to filter by categories of other users 
				User testAuthor=getAuthor(operation);
				User categoryUser=questionCategory.getUser();
				ok=testAuthor.equals(categoryUser) || getUseOtherUsersQuestions(operation).booleanValue();
			}
			else
			{
				ok=false;
			}
		}
		else
		{
			// First we have to see if the category is owned by test's author,
			// if that is not the case we will need to perform aditional checks  
			long currentUserId=userSessionService.getCurrentUserId();
			long testAuthorId=getAuthor(operation).getId();
			User categoryUser=questionCategory.getUser();
			if (categoryUser.getId()!=testAuthorId)
			{
				// We need to check that test author has permission to assign questions 
				// from categories of other users to his/her tests
				if (getUseOtherUsersQuestions(operation).booleanValue())
				{
					// We have to see if this is a public or a private category
					// Public categories doesn't need more checks
					// But private categories need aditional permissions
					Visibility privateVisibility=
						visibilitiesService.getVisibility(operation,"CATEGORY_VISIBILITY_PRIVATE");
					if (questionCategory.getVisibility().getLevel()>=privateVisibility.getLevel())
					{
						// Next we need to check that test's author has permission to view 
						// questions from private categories of other users, and aditionally we need 
						// to check that test's author has permission to view questions from 
						// private categories of administrators if the owner of the category 
						// is an administrator and to check that test's author has permission 
						// to view questions from private categories of users with permission 
						// to improve permissions over its owned ones if the owner of the category 
						// has that permission (superadmin)
						ok=getTestAuthorViewQuestionsFromOtherUsersPrivateCategoriesEnabled(operation).booleanValue() 
							&& (!isAdmin(operation,categoryUser) || 
							getTestAuthorViewQuestionsFromAdminsPrivateCategoriesEnabled(operation).booleanValue()) 
							&& (!isSuperadmin(operation,categoryUser) || 
							getTestAuthorViewQuestionsFromSuperadminsPrivateCategoriesEnabled(operation).
							booleanValue());
					}
				}
				else
				{
					ok=false;
				}
			}
			if (ok && testAuthorId!=currentUserId && categoryUser.getId()!=currentUserId)
			{
				// We have to see if this a public or a private category
				// Public categories doesn't need more checks
				// But private categories need aditional permissions
				Visibility privateVisibility=
					visibilitiesService.getVisibility(operation,"CATEGORY_VISIBILITY_PRIVATE");
				if (questionCategory.getVisibility().getLevel()>=privateVisibility.getLevel())
				{
					// Finally we need to check that current user has permission to view questions 
					// from privates categories of other users, and aditionally we need to check 
					// that current user has permission to view questions from private categories 
					// of administrators if the owner of the category is an administrator 
					// and to check that current user has permission to view questions 
					// from private categories of users with permission to improve permissions 
					// over its owned ones if the owner of the category has that permission (superadmin)
					ok=getViewQuestionsFromOtherUsersPrivateCategoriesEnabled(operation).booleanValue() &&
						(!isAdmin(operation,categoryUser) || 
						getViewQuestionsFromAdminsPrivateCategoriesEnabled(operation).booleanValue()) && 
						(!isSuperadmin(operation,categoryUser) || 
						getViewQuestionsFromSuperadminsPrivateCategoriesEnabled(operation).booleanValue());
				}
			}
		}
		return ok;
	}
    
	/**
	 * @param operation Operation
	 * @param filterCategory Filter category can be optionally passed as argument
	 * @return true if user has permissions to display questions with the current selected filter, 
	 * false otherwise
	 */
    private boolean checkQuestionsFilterPermission(Operation operation,Category filterCategory)
	{
    	boolean ok=true;
    	
    	// Get current user session Hibernate operation
    	operation=getCurrentUserOperation(operation);
    	
    	long filterCategoryId=getFilterCategoryId(operation);
		if (specialCategoryFiltersMap.containsKey(Long.valueOf(filterCategoryId)))
		{
			// Check permissions needed for selected special category filter
			User testAuthor=getAuthor(operation);
			SpecialCategoryFilter filter=specialCategoryFiltersMap.get(Long.valueOf(filterCategoryId));
			for (String requiredPermission:filter.requiredPermissions)
			{
				if (permissionsService.isDenied(operation,testAuthor,requiredPermission))
				{
					ok=false;
					break;
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
			
			// Check permissions needed for selected question category
			ok=checkQuestionCategoryFilterPermission(operation,filterCategory);
		}
		return ok;
	}
    
	/**
	 * Checks if deleting a question will affect to the feedbacks already defined.
	 * @param operation Operation
	 * @param sectionOrder Position of section of the question to delete
	 * @param questionOrder Position of question to delete within section
	 * @return true if deleting a section won't affect to the feedbacks already defined, false otherwise
	 */
	private boolean checkFeedbacksForDeleteQuestion(Operation operation,int sectionOrder,int questionOrder)
	{
		boolean ok=true;
		int newTotalMarks=0;
		int newSectionMarks=0;
		
		// Get current user session Hibernate operation
		operation=getCurrentUserOperation(operation);
		
		ScoreType scoreType=getScoreType(operation);
		if ("SCORE_TYPE_QUESTIONS".equals(scoreType.getType()))
		{
			for (SectionBean section:getSections(operation))
			{
				if (section.getOrder()==sectionOrder)
				{
					if (section.isShuffle() && section.isRandom())
					{
						int newRandomQuantity=section.getRandomQuantity();
						int newQuestionOrdersSize=section.getQuestionOrdersSize()-1;
						if (newRandomQuantity>newQuestionOrdersSize)
						{
							newRandomQuantity=newQuestionOrdersSize;
						}
						newSectionMarks=newRandomQuantity*BASE_MARKS_PER_QUESTION;
					}
					else
					{
						for (QuestionOrderBean qOrder:section.getQuestionOrders())
						{
							if (qOrder.getOrder()!=questionOrder)
							{
								newSectionMarks+=qOrder.getWeight()*BASE_MARKS_PER_QUESTION;
							}
						}
					}
					newTotalMarks+=newSectionMarks;
				}
				else
				{
					if (section.isShuffle() && section.isRandom())
					{
						newTotalMarks+=section.getRandomQuantity()*BASE_MARKS_PER_QUESTION;
					}
					else
					{
						for (QuestionOrderBean qOrder:section.getQuestionOrders())
						{
							newTotalMarks+=qOrder.getWeight()*BASE_MARKS_PER_QUESTION;
						}
					}
				}
			}
		}
		else if ("SCORE_TYPE_SECTIONS".equals(scoreType.getType()))
		{
			int maxBaseSectionScore=0;
			int totalSectionsWeight=0;
			for (SectionBean section:getSections(operation))
			{
				int maxSectionScore=0;
				if (section.getOrder()==sectionOrder)
				{
					if (section.isShuffle() && section.isRandom())
					{
						int newRandomQuantity=section.getRandomQuantity();
						int newQuestionOrdersSize=section.getQuestionOrdersSize()-1;
						if (newRandomQuantity>newQuestionOrdersSize)
						{
							newRandomQuantity=newQuestionOrdersSize;
						}
						maxSectionScore=newRandomQuantity*BASE_MARKS_PER_QUESTION;
					}
					else
					{
						for (QuestionOrderBean qOrder:section.getQuestionOrders())
						{
							if (qOrder.getOrder()!=questionOrder)
							{
								maxSectionScore+=qOrder.getWeight()*BASE_MARKS_PER_QUESTION;
							}
						}
					}
					newSectionMarks=maxSectionScore;
				}
				else
				{
					if (section.isShuffle() && section.isRandom())
					{
						maxSectionScore=section.getRandomQuantity()*BASE_MARKS_PER_QUESTION;
					}
					else
					{
						for (QuestionOrderBean qOrder:section.getQuestionOrders())
						{
							maxSectionScore+=qOrder.getWeight()*BASE_MARKS_PER_QUESTION;
						}
					}
				}
				if (maxSectionScore>maxBaseSectionScore)
				{
					maxBaseSectionScore=maxSectionScore;
				}
				totalSectionsWeight+=section.getWeight();
			}
			newTotalMarks=totalSectionsWeight*maxBaseSectionScore;
		}
		
		for (TestFeedbackBean feedback:getFeedbacks(operation))
		{
			SectionBean section=feedback.getCondition().getSection();
			if (section==null)
			{
				if (feedback.getCondition().getUnit().equals(TestFeedbackConditionBean.MARKS_UNIT))
				{
					int minValue=feedback.getCondition().getMinValue();
					if (minValue>newTotalMarks)
					{
						ok=false;
						break;
					}
					int maxValue=feedback.getCondition().getMaxValue();
					if (maxValue!=Integer.MAX_VALUE && maxValue>newTotalMarks)
					{
						ok=false;
						break;
					}
				}
			}
			else if (section.getOrder()==sectionOrder)
			{
				if (feedback.getCondition().getUnit().equals(TestFeedbackConditionBean.MARKS_UNIT))
				{
					int minValue=feedback.getCondition().getMinValue();
					if (minValue>newSectionMarks)
					{
						ok=false;
						break;
					}
					int maxValue=feedback.getCondition().getMaxValue();
					if (maxValue!=Integer.MAX_VALUE && maxValue>newSectionMarks)
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
	 * Updates feebacks related to the deleted question if needed.
	 * @param operation Operation
	 * @param sectionOrder Position of section of the deleted question
	 * @param questionOrder Position of deleted question within section
	 */
	private void updateFeedbacksForDeleteQuestion(Operation operation,int sectionOrder,int questionOrder)
	{
		// Get current user session Hibernate operation
		operation=getCurrentUserOperation(operation);
		
		for (TestFeedbackBean feedback:getFeedbacks(operation))
		{
			TestFeedbackConditionBean condition=feedback.getCondition();
			if ((condition.getSection()==null || condition.getSection().getOrder()==sectionOrder)&& 
				condition.getUnit().equals(TestFeedbackConditionBean.MARKS_UNIT))
			{
				if (NumberComparator.compareU(condition.getComparator(),NumberComparator.BETWEEN))
				{
					int maxConditionalValue=condition.getMaxConditionalValue(operation);
					if (condition.getConditionalBetweenMax()>maxConditionalValue)
					{
						condition.setConditionalBetweenMax(maxConditionalValue);
						if (condition.getConditionalBetweenMin()>condition.getConditionalBetweenMax())
						{
							condition.setConditionalBetweenMin(maxConditionalValue);
						}
					}
				}
				else
				{
					int maxValueConditionalCmp=condition.getMaxConditionalValue(operation);
					if (condition.getConditionalCmp()>maxValueConditionalCmp)
					{
						condition.setConditionalCmp(maxValueConditionalCmp);
					}
				}
			}
		}
	}
	
    //ActionListener que elimina una pregunta de la sección correspondiente
    /**
     * Deletes a question from a section.
     * @param event Action event
     */
    public void removeQuestion(ActionEvent event)
    {
    	String property=getPropertyChecked();
    	if (property==null)
    	{
			// Get current user session Hibernate operation
			Operation operation=getCurrentUserOperation(null);
    		
    		// Get section to process if any
        	AccordionPanel sectionsAccordion=getSectionsAccordion(operation,event.getComponent());
        	SectionBean section=getSectionFromSectionsAccordion(operation,sectionsAccordion);
    		
    		// We need to process some input fields
    		processSectionsInputFields(operation,event.getComponent(),section);
    		
    		// Set back accordion row index -1
    		sectionsAccordion.setRowIndex(-1);
    		
    		boolean forceRemoveQuestion=true;
    		if (event.getComponent().getAttributes().get("questionOrder")!=null)
    		{
    			//questionToRemoveSectionOrder=((Integer)event.getComponent().getAttributes().get("order")).intValue();
    			questionToRemoveSectionOrder=activeSectionIndex+1;
    			questionToRemoveOrder=((Integer)event.getComponent().getAttributes().get("questionOrder")).intValue();
    			forceRemoveQuestion=false;
    		}
    		
    		// Check that current section name entered by user is valid
    		if (checkSectionName(section.getName()))
    		{
        		boolean checkFeedbacks=
        			checkFeedbacksForDeleteQuestion(operation,questionToRemoveSectionOrder,questionToRemoveOrder);
            	if (forceRemoveQuestion || checkFeedbacks)
            	{
                   	// Remove question from section
                	if (section!=null)
                	{
                		section.removeQuestionOrder(questionToRemoveOrder);
                		
                		// Update random quantity if needed
                		if (section.getRandomQuantity()>section.getQuestionOrdersSize())
                		{
                			section.setRandomQuantity(section.getQuestionOrdersSize());
                			section.acceptRandomQuantity();
                			
            				// We need to update random quantity
            				updateSectionRandomQuantity(operation,event.getComponent(),section);
                		}
                	}
                	
        			// If it is needed update feedbacks
        			if (!checkFeedbacks)
        			{
        				updateFeedbacksForDeleteQuestion(operation,questionToRemoveSectionOrder,questionToRemoveOrder);
        			}
        			
        			// We need to update sections text fields
        			updateSectionsTextFields(operation,event.getComponent(),getSectionsSize(operation));
            	}
        		else
        		{
        			RequestContext rq=RequestContext.getCurrentInstance();
        			rq.execute("confirmDeleteQuestionDialog.show()");
        		}
    		}
    		else
    		{
    			// Restore old section name
    			section.setName(activeSectionName);
    			
    			// Scroll page to top position
    			scrollToTop();
    		}
    	}
    	else if ("sectionWeight".equals(property))
    	{
			// We need to process weight
    		processSectionWeight(getCurrentUserOperation(null),event.getComponent(),sectionChecked);
    		
    		// We need to stop this response because there is another one in process
    		FacesContext.getCurrentInstance().responseComplete();
    	}
    	else if ("randomQuantity".equals(property))
    	{
			// We need to process random quantity
    		processSectionRandomQuantity(getCurrentUserOperation(null),event.getComponent(),sectionChecked);
    		
    		// We need to stop this response because there is another one in process
    		FacesContext.getCurrentInstance().responseComplete();
    	}
    	else if ("questionOrderWeight".equals(property))
    	{
    		// We need to process question orders weights
    		processQuestionOrderWeights(getCurrentUserOperation(null),event.getComponent(),sectionChecked);
    		
    		// We need to stop this response because there is another one in process
    		FacesContext.getCurrentInstance().responseComplete();
    	}
	}
    
    //Guarda la prueba y devuelve el nombre de la vista siguiente
    //return Nombre de la siguiente vista: "tests"
    /**
	 * Save test to DB.
	 * @return Next wiew (tests page if save is sucessful, otherwise we keep actual view)
     */
    public String saveTest()
    {
    	String nextView="tests?faces-redirect=true";
    	
    	// Get current user session Hibernate operation
    	Operation operation=getCurrentUserOperation(null); 
    	
    	if (getTestId(operation)>0L)
    	{
        	if (activeTestIndex==GENERAL_TABVIEW_TAB)
        	{
        		// Check test name
        		if (!checkTestName(getName(operation)))
        		{
        			nextView=null;
        		}
        		
       			// Check calendar dates if needed
       			if (activeGeneralTabIndex==CALENDAR_TAB_GENERAL_ACCORDION && !checkCalendarInputFields(operation))
       			{
       				nextView=null;
       			}
       		}
       		else if (activeTestIndex==SECTIONS_TABVIEW_TAB)
       		{
   				UIComponent viewRoot=FacesContext.getCurrentInstance().getViewRoot();
       			
   	    		// Get section to process if any
   	        	AccordionPanel sectionsAccordion=getSectionsAccordion(operation,viewRoot);
   	        	SectionBean section=getSectionFromSectionsAccordion(operation,sectionsAccordion);
   	    		
   	    		// We need to process some input fields
   	    		processSectionsInputFields(operation,viewRoot,section);
   	    		
   	    		// Set back accordion row index -1
   	    		sectionsAccordion.setRowIndex(-1);
   				
   	    		// Check that current section name entered by user is valid
   				if (checkSectionName(section.getName()))
   				{
   					activeSectionName=section.getName();
   				}
   				else
   				{
   					// Restore old section tab without changing its name
   					updateSectionsTextFields(operation,viewRoot,getSectionsSize(operation));
   					section.setName(activeSectionName);
  					
   					nextView=null;
   				}
       		}
   		}
        
        // Check test category
		setGlobalOtherUserCategoryAllowed(null);
		resetTestAuthorAdmin(operation);
		resetTestAuthorSuperadmin(operation);
		setViewTestsFromOtherUsersPrivateCategoriesEnabled(null);
		setViewTestsFromAdminsPrivateCategoriesEnabled(null);
		setViewTestsFromSuperadminsPrivateCategoriesEnabled(null);
    	if (checkCategory(operation))
    	{
    		if (!checkAvailableTestName(operation))
    		{
    			addErrorMessage("TEST_NAME_ALREADY_DECLARED");
    			nextView=null;
    		}
    	}
    	else
    	{
    		addErrorMessage("TEST_CATEGORY_ASSIGN_ERROR");
    		nextView=null;
    		
    		setUseGlobalQuestions(null);
    		setUseOtherUsersQuestions(null);
    		setViewQuestionsFromOtherUsersPrivateCategoriesEnabled(null);
    		setViewQuestionsFromAdminsPrivateCategoriesEnabled(null);
    		setViewQuestionsFromSuperadminsPrivateCategoriesEnabled(null);
    		setTestAuthorViewQuestionsFromOtherUsersPrivateCategoriesEnabled(null);
    		setTestAuthorViewQuestionsFromAdminsPrivateCategoriesEnabled(null);
    		setTestAuthorViewQuestionsFromSuperadminsPrivateCategoriesEnabled(null);
       		resetAdmins();
       		resetSuperadmins();
       		
       		// Reload test categories from DB
       		setTestsCategories(null);
   		}
   		
       	// Validate that all sections have at least one question
       	boolean sectionEmpty=false;
       	for (SectionBean section:getSections(operation))
       	{
       		if (section.getQuestionOrdersSize()==0)
       		{
       			sectionEmpty=true;
       			break;
       		}
       	}
       	if (sectionEmpty)
       	{
       		addErrorMessage("TEST_SECTION_EMPTY");
   			nextView=null;
       	}
    	
    	if (nextView!=null)
    	{
    		try
    		{
            	// Get test
            	Test test=getAsTest(operation);
        		if (getTestId(operation)>0L) // Update test
        		{
        			testsService.updateTest(test);
        		}
        		else // New test
        		{
        			testsService.addTest(test);
        		}
    		}
    		finally
    		{
    			// End current user session Hibernate operation
    			userSessionService.endCurrentUserOperation();
    		}
    	}
    	return nextView;
	}
    
    /**
     * Set test bean fields from a Test object.
     * @param test Test object 
     */
    public void setFromTest(Test test)
    {
    	testId=test.getId();
    	setName(test.getName());
    	setAuthor(test.getCreatedBy());
    	setTimeCreated(test.getTimeCreated());
    	setTimeTestDeploy(test.getTimeTestDeploy());
    	setTimeDeployDeploy(test.getTimeDeployDeploy());
    	setCategory(test.getCategory());
    	setDescription(test.getDescription());
    	setTitle(test.getTitle());
    	setAssessement(test.getAssessement());
    	scoreType=test.getScoreType();
    	oldScoreType=scoreType;
    	setAllUsersAllowed(test.isAllUsersAllowed());
    	setAllowAdminReports(test.isAllowAdminReports());
		DateFormat df=new SimpleDateFormat(DATE_HIDDEN_PATTERN);
    	Date startDate=test.getStartDate();
    	setStartDate(startDate);
    	setStartDateHidden(startDate==null?"":df.format(startDate));
    	Date closeDate=test.getCloseDate();
    	setCloseDate(closeDate);
    	setCloseDateHidden(closeDate==null?"":df.format(closeDate));
    	Date warningDate=test.getWarningDate();
    	setWarningDate(warningDate);
    	setWarningDateHidden(warningDate==null?"":df.format(warningDate));
    	Date feedbackDate=test.getFeedbackDate();
    	setFeedbackDate(feedbackDate);
    	setFeedbackDateHidden(feedbackDate==null?"":df.format(feedbackDate));
    	setRestrictDates(startDate!=null);
    	setRestrictFeedbackDate(feedbackDate!=null);
    	setFreeSummary(test.isFreeSummary());
    	setFreeStop(test.isFreeStop());
    	setSummaryQuestions(test.isSummaryQuestions());
    	setSummaryScores(test.isSummaryScores());
    	setSummaryAttempts(test.isSummaryAttempts());
    	setNavigation(test.isNavigation());
    	setNavLocation(test.getNavLocation());
    	setRedoQuestion(test.getRedoQuestion());
    	setRedoTest(test.isRedoTest());
    	setPresentationTitle(test.getPresentationTitle());
    	setPresentation(test.getPresentation());
    	setPreliminarySummaryTitle(test.isNavigation()?test.getPreliminarySummaryTitle():"");
    	setPreliminarySummaryButton(test.isNavigation()?test.getPreliminarySummaryButton():"");
    	setPreliminarySummary(test.isNavigation()?test.getPreliminarySummary():"");
    	setFeedbackDisplaySummary(test.isFeedbackDisplaySummary());
    	setFeedbackDisplaySummaryMarks(test.isFeedbackDisplaySummary()?test.isFeedbackDisplaySummaryMarks():false);
    	setFeedbackDisplaySummaryAttempts(
    		test.isFeedbackDisplaySummary()?test.isFeedbackDisplaySummaryAttempts():true);
    	setFeedbackSummaryPrevious(test.isFeedbackDisplaySummary()?test.getFeedbackSummaryPrevious():"");
    	setFeedbackDisplayScores(test.isFeedbackDisplayScores());
    	setFeedbackDisplayScoresMarks(test.isFeedbackDisplayScores()?test.isFeedbackDisplayScoresMarks():false);
    	setFeedbackDisplayScoresPercentages(
    		test.isFeedbackDisplayScores()?test.isFeedbackDisplayScoresPercentages():true);
    	setFeedbackScoresPrevious(test.isFeedbackDisplayScores()?test.getFeedbackScoresPrevious():"");
    	setFeedbackAdvancedPrevious(test.getFeedbackAdvancedPrevious());
    	setFeedbackAdvancedNext(test.getFeedbackAdvancedNext());
    	
    	// Users with permission to do/administrate this test
    	List<User> testUsers=new ArrayList<User>();
    	setTestUsers(testUsers);
    	List<User> testAdmins=new ArrayList<User>();
    	setTestAdmins(testAdmins);
    	for (TestUser tu:test.getTestUsers())
    	{
    		if (tu.isOmUser())
    		{
    			User testUser=new User();
    			testUser.setFromOtherUser(tu.getUser());
    			testUsers.add(testUser);
    		}
    		if (tu.isOmAdmin())
    		{
    			User testAdmin=new User();
    			testAdmin.setFromOtherUser(tu.getUser());
    			testAdmins.add(testAdmin);
    		}
    	}
    	
    	// Sections (note that it is important to initialize sections before feedbacks)
    	List<SectionBean> sections=new ArrayList<SectionBean>();
    	setSections(sections);
    	for (Section section:test.getSections())
    	{
    		sections.add(new SectionBean(this,section));
    	}
    	
    	// Support contacts
    	List<SupportContactBean> supportContacts=new ArrayList<SupportContactBean>();
    	setSupportContacts(supportContacts);
    	for (SupportContact supportContact:test.getSupportContacts())
    	{
    		supportContacts.add(new SupportContactBean(this,supportContact));
    	}
    	
    	// Evaluators
    	List<EvaluatorBean> evaluators=new ArrayList<EvaluatorBean>();
    	setEvaluators(evaluators);
    	for (Evaluator evaluator:test.getEvaluators())
    	{
    		evaluators.add(new EvaluatorBean(this,evaluator));
    	}
    	
    	// Feedbacks (be careful that sections must be initialized before initializing feedbacks)
    	List<TestFeedbackBean> feedbacks=new ArrayList<TestFeedbackBean>();
    	setFeedbacks(feedbacks);
    	for (TestFeedback testFeedback:test.getTestFeedbacks())
    	{
    		feedbacks.add(new TestFeedbackBean(this,testFeedback));
    	}
    }
    
    /**
     * @return Test object with data from this test bean
     */
    public Test getAsTest()
    {
    	return getAsTest(null);
    }
    
    /**
     * @param operation Operation
     * @return Test object with data from this test bean
     */
    private Test getAsTest(Operation operation)
    {
    	Test test=null;
    	
    	// Get current user session Hibernate operation
    	operation=getCurrentUserOperation(operation);
    	
        Date currentDate=new Date();
        Date timeCreated=getTimeCreated(operation);
        test=new Test(getTestId(operation),getName(operation),getCategory(operation),getAuthor(operation),
        	timeCreated==null?currentDate:timeCreated);
        test.setModifiedBy(userSessionService.getCurrentUser(operation));
        test.setTimeModified(currentDate);
        test.setTimeTestDeploy(getTimeTestDeploy(operation));
        test.setTimeDeployDeploy(getTimeDeployDeploy(operation));
        test.setDescription(getDescription(operation));
        test.setTitle(getTitle(operation));
        test.setAssessement(getAssessement(operation));
        test.setScoreType(getScoreType(operation));
        test.setAllUsersAllowed(isAllUsersAllowed(operation));
        test.setAllowAdminReports(isAllowAdminReports(operation));
        String startDateHidden=getStartDateHidden();
        String closeDateHidden=getCloseDateHidden();
        String warningDateHidden=getWarningDateHidden();
        String feedbackDateHidden=getFeedbackDateHidden();
        boolean restrictDates=isRestrictDates(operation);
        test.setStartDate(restrictDates?getStartDate(operation):null);
        test.setCloseDate(restrictDates?getCloseDate(operation):null);
        test.setWarningDate(restrictDates?getWarningDate(operation):null);
        test.setFeedbackDate(isRestrictFeedbackDate(operation)?getFeedbackDate(operation):null);
        setStartDateHidden(startDateHidden);
        setCloseDateHidden(closeDateHidden);
        setWarningDateHidden(warningDateHidden);
        setFeedbackDateHidden(feedbackDateHidden);
        test.setFreeSummary(isFreeSummary(operation));
        test.setFreeStop(isFreeStop(operation));
        test.setSummaryQuestions(isSummaryQuestions(operation));
        test.setSummaryScores(isSummaryScores(operation));
        test.setSummaryAttempts(isSummaryAttempts(operation));
        boolean navigation=isNavigation(operation);
        test.setNavigation(navigation);
        test.setNavLocation(getNavLocation(operation));
        test.setRedoQuestion(getRedoQuestion(operation));
        test.setRedoTest(isRedoTest(operation));
        test.setPresentationTitle(getPresentationTitle(operation));
        test.setPresentation(getPresentation(operation));
        test.setPreliminarySummaryTitle(navigation?getPreliminarySummaryTitle(operation):"");
        test.setPreliminarySummaryButton(navigation?getPreliminarySummaryButton(operation):"");
        test.setPreliminarySummary(navigation?getPreliminarySummary(operation):"");
        boolean feedbackDisplaySummary=isFeedbackDisplaySummary(operation);
        test.setFeedbackDisplaySummary(feedbackDisplaySummary);
        test.setFeedbackDisplaySummaryMarks(feedbackDisplaySummary?isFeedbackDisplaySummaryMarks(operation):false);
        test.setFeedbackDisplaySummaryAttempts(
        	feedbackDisplaySummary?isFeedbackDisplaySummaryAttempts(operation):true);
       	test.setFeedbackSummaryPrevious(feedbackDisplaySummary?getFeedbackSummaryPrevious(operation):"");
       	boolean feedbackDisplayScores=isFeedbackDisplayScores(operation);
       	test.setFeedbackDisplayScores(feedbackDisplayScores);
       	test.setFeedbackDisplayScoresMarks(feedbackDisplayScores?isFeedbackDisplayScoresMarks(operation):false);
       	test.setFeedbackDisplayScoresPercentages(
       		feedbackDisplayScores?isFeedbackDisplayScoresPercentages(operation):true);
       	test.setFeedbackScoresPrevious(feedbackDisplayScores?getFeedbackScoresPrevious(operation):"");
       	test.setFeedbackAdvancedPrevious(getFeedbackAdvancedPrevious(operation));
       	test.setFeedbackAdvancedNext(getFeedbackAdvancedNext(operation));
   		
        // Users with permission to do/administrate this test
        for (User testUser:getTestUsers(operation))
        {
        	TestUser tu=null;
        	TestUser testUserFromDB=testUsersService.getTestUser(operation,test,testUser);
        	if (testUserFromDB==null)
        	{
        		tu=new TestUser(0L,test,testUser,true,false);
        	}
        	else
        	{
        		tu=new TestUser();
        		tu.setFromOtherTestUser(testUserFromDB);
        		tu.setOmUser(true);
        		tu.setOmAdmin(false);
        	}
        	test.getTestUsers().add(tu);
        }
       	for (User testAdmin:getTestAdmins(operation))
       	{
       		TestUser ta=null;
       		for (TestUser tu:test.getTestUsers())
       		{
       			if (testAdmin.equals(tu.getUser()))
       			{
       				ta=tu;
       				break;
       			}
       		}
       		if (ta==null)
       		{
          		TestUser testAdminFromDB=testUsersService.getTestUser(operation,test,testAdmin);
           		if (testAdminFromDB==null)
           		{
           			ta=new TestUser(0L,test,testAdmin,false,true);
           		}
           		else
           		{
           			ta=new TestUser();
           			ta.setFromOtherTestUser(testAdminFromDB);
           			ta.setOmUser(false);
           			ta.setOmAdmin(true);
           		}
           		test.getTestUsers().add(ta);
       		}
      		else
       		{
       			ta.setOmAdmin(true);
       		}
       	}
    	
    	// Sections (note that it is important to initialize sections before feedbacks)
    	for (SectionBean section:getSections(operation))
    	{
    		test.getSections().add(section.getAsSection(operation,test));
    	}
    	
    	// Support contacts
    	for (SupportContactBean supportContact:getSupportContacts(operation))
    	{
    		test.getSupportContacts().add(supportContact.getAsSupportContact(operation,test));
    	}
    	
    	// Evaluators
    	for (EvaluatorBean evaluator:getEvaluators(operation))
    	{
    		test.getEvaluators().add(evaluator.getAsEvaluator(operation,test));
    	}
    	
    	// Feedbacks (be careful that sections must be initialized before initializing feedbacks)
    	for (TestFeedbackBean testFeedback:getFeedbacks(operation))
    	{
    		test.getTestFeedbacks().add(testFeedback.getAsTestFeedback(operation,test));
    	}
    	return test;
    }
    
    /**
     * Initialize some test fields.
     * @param operation Operation
     */
    private void initializeTest(Operation operation)
    {
		// Note that it is important to set this flag first to avoid infinite recursion
    	testInitialized=true;
    	
		// Get current user session Hibernate operation
		operation=getCurrentUserOperation(operation);
    	
    	FacesContext context=FacesContext.getCurrentInstance();
    	Map<String,String> params=context.getExternalContext().getRequestParameterMap();
    	if (params.containsKey("testId")) // Edit test
    	{
    		setFromTest(testsService.getTest(operation,Long.parseLong(params.get("testId"))));
    		
    		// We need to initialize special categories filters for the questions within sections tab
			List<String> allPermissions=new ArrayList<String>();
			allPermissions.add("PERMISSION_TEST_USE_GLOBAL_QUESTIONS");
			allPermissions.add("PERMISSION_TEST_USE_OTHER_USERS_QUESTIONS");
			String categoryGen=localizationService.getLocalizedMessage("CATEGORY_GEN");
			if ("M".equals(categoryGen))
			{
				allCategories=new SpecialCategoryFilter(0L,"ALL_OPTIONS",allPermissions);
			}
			else
			{
				allCategories=new SpecialCategoryFilter(0L,"ALL_OPTIONS_F",allPermissions);
			}
			
			List<String> allEvenPrivateCategoriesPermissions=new ArrayList<String>();
			allEvenPrivateCategoriesPermissions.add("PERMISSION_TEST_USE_GLOBAL_QUESTIONS");
			allEvenPrivateCategoriesPermissions.add("PERMISSION_TEST_USE_OTHER_USERS_QUESTIONS");
			allEvenPrivateCategoriesPermissions.add(
				"PERMISSION_QUESTIONS_VIEW_QUESTIONS_OF_OTHER_USERS_PRIVATE_CATEGORIES_ENABLED");
			allEvenPrivateCategories=new SpecialCategoryFilter(
				-1L,"ALL_EVEN_PRIVATE_CATEGORIES",allEvenPrivateCategoriesPermissions);
			
			List<String> allPrivateCategoriesOfOtherUsersPermissions=new ArrayList<String>();
			allPrivateCategoriesOfOtherUsersPermissions.add("PERMISSION_TEST_USE_OTHER_USERS_QUESTIONS");
			allPrivateCategoriesOfOtherUsersPermissions.add(
				"PERMISSION_QUESTIONS_VIEW_QUESTIONS_OF_OTHER_USERS_PRIVATE_CATEGORIES_ENABLED");
			allPrivateCategories=
				new SpecialCategoryFilter(-6L,"ALL_PRIVATE_CATEGORIES",allPrivateCategoriesOfOtherUsersPermissions);
			
			if (getAuthor(operation).getId()==userSessionService.getCurrentUserId())
    		{
        		List<String> allMyCategoriesPermissions=new ArrayList<String>();
    			allMyCategoriesPermissions.add("PERMISSION_TEST_USE_GLOBAL_QUESTIONS");
    			allTestAuthorCategories=new SpecialCategoryFilter(-2L,"ALL_MY_CATEGORIES",allMyCategoriesPermissions);
    			
    			allTestAuthorCategoriesExceptGlobals=
    				new SpecialCategoryFilter(-3L,"ALL_MY_CATEGORIES_EXCEPT_GLOBALS",new ArrayList<String>());
    			
    			
        		// We add all initialized categories filters to categories filters map
        		specialCategoryFiltersMap=new LinkedHashMap<Long,SpecialCategoryFilter>();
        		specialCategoryFiltersMap.put(Long.valueOf(allCategories.id),allCategories);
        		specialCategoryFiltersMap.put(Long.valueOf(allEvenPrivateCategories.id),allEvenPrivateCategories);
        		specialCategoryFiltersMap.put(Long.valueOf(allTestAuthorCategories.id),allTestAuthorCategories);
        		specialCategoryFiltersMap.put(
        			Long.valueOf(allTestAuthorCategoriesExceptGlobals.id),allTestAuthorCategoriesExceptGlobals);
        		specialCategoryFiltersMap.put(Long.valueOf(ALL_GLOBAL_CATEGORIES.id),ALL_GLOBAL_CATEGORIES);
        		specialCategoryFiltersMap.put(
        			Long.valueOf(ALL_PUBLIC_CATEGORIES_OF_OTHER_USERS.id),ALL_PUBLIC_CATEGORIES_OF_OTHER_USERS);
            	specialCategoryFiltersMap.put(Long.valueOf(allPrivateCategories.id),allPrivateCategories);
            	specialCategoryFiltersMap.put(
            		Long.valueOf(ALL_CATEGORIES_OF_OTHER_USERS.id),ALL_CATEGORIES_OF_OTHER_USERS);
    		}
    		else
    		{
        		List<String> allCategoriesOfQuestionAuthorPermissions=new ArrayList<String>();
        		allCategoriesOfQuestionAuthorPermissions.add("PERMISSION_TEST_USE_GLOBAL_QUESTIONS");
    			allTestAuthorCategories=
    				new SpecialCategoryFilter(-2L,"ALL_CATEGORIES_OF",allCategoriesOfQuestionAuthorPermissions);
    			
    			allTestAuthorCategoriesExceptGlobals=
    				new SpecialCategoryFilter(-3L,"ALL_CATEGORIES_OF_EXCEPT_GLOBALS",new ArrayList<String>());
    			
        		// We add all initialized categories filters to categories filters map
        		specialCategoryFiltersMap=new LinkedHashMap<Long,SpecialCategoryFilter>();
        		specialCategoryFiltersMap.put(Long.valueOf(allCategories.id),allCategories);
        		specialCategoryFiltersMap.put(Long.valueOf(allEvenPrivateCategories.id),allEvenPrivateCategories);
        		specialCategoryFiltersMap.put(Long.valueOf(allTestAuthorCategories.id),allTestAuthorCategories);
        		specialCategoryFiltersMap.put(
        			Long.valueOf(allTestAuthorCategoriesExceptGlobals.id),allTestAuthorCategoriesExceptGlobals);
        		specialCategoryFiltersMap.put(Long.valueOf(ALL_GLOBAL_CATEGORIES.id),ALL_GLOBAL_CATEGORIES);
        		specialCategoryFiltersMap.put(Long.valueOf(allPrivateCategories.id),allPrivateCategories);
    		}
    	}
    	else if (params.containsKey("testCopyId")) // Create copy of an existing test
    	{
	    	Test testFrom=testsService.getTest(operation,Long.parseLong(params.get("testCopyId")));
    		
	    	// We instantiate a new test
	    	Test test=new Test();
    		
	    	// Copy data from test
	    	test.setFromOtherTest(testFrom);
	    	
	    	// Add sections
	    	Map<Section,Section> sectionsFromTo=new HashMap<Section,Section>();
	    	test.setSections(new ArrayList<Section>());
	    	for (Section sectionFrom:testFrom.getSections())
	    	{
	    		Section section=new Section();
	    		
	    		// Copy data from section
	    		section.setFromOtherSection(sectionFrom);
	    		
	    		// Add question orders
	    		section.setQuestionOrders(new ArrayList<QuestionOrder>());
	    		for (QuestionOrder questionOrderFrom:sectionFrom.getQuestionOrders())
	    		{
	    			QuestionOrder questionOrder=new QuestionOrder();
	    			
	    			// Copy data from question order
	    			questionOrder.setFromOtherQuestionOrder(questionOrderFrom);
	    			
	    			// This is a new question order still so we need to reset some fields
	    			questionOrder.setId(0L);
	    			questionOrder.setSection(section);
	    			
	    			// Add new question order
	    			section.getQuestionOrders().add(questionOrder);
    			}
    			
    			// This is a new section still so we need to reset some fields
    			section.setId(0L);
    			section.setTest(test);
    			
    			// Add new section
    			test.getSections().add(section);
    			sectionsFromTo.put(sectionFrom,section);
    		}
    		
    		// Add feedbacks
    		test.setTestFeedbacks(new ArrayList<TestFeedback>());
    		for (TestFeedback testFeedbackFrom:testFrom.getTestFeedbacks())
    		{
    			TestFeedback testFeedback=new TestFeedback();
    			
    			// Copy data from feedback
    			testFeedback.setFromOtherTestFeedback(testFeedbackFrom);
    			
    			// This is a new feedback still so we need to reset some fields
    			testFeedback.setId(0L);
    			testFeedback.setTest(test);
    			if (testFeedbackFrom.getSection()!=null)
    			{
    				testFeedback.setSection(sectionsFromTo.get(testFeedbackFrom.getSection()));
    			}
    			
    			// Add new feedback
    			test.getTestFeedbacks().add(testFeedback);
    		}
    		
    		// Add support contacts
    		test.setSupportContacts(new ArrayList<SupportContact>());
    		for (SupportContact supportContactFrom:testFrom.getSupportContacts())
    		{
    			SupportContact supportContact=new SupportContact();
    			
    			// Copy data from support contact
    			supportContact.setFromOtherSupportContact(supportContactFrom);
    			
    			// This is a new support contact still so we need to reset some fields
    			supportContact.setId(0L);
    			supportContact.setTest(test);
    			
    			// Add new support contact
    			test.getSupportContacts().add(supportContact);
    		}
    		
    		// Add evaluators
    		test.setEvaluators(new ArrayList<Evaluator>());
	    	for (Evaluator evaluatorFrom:testFrom.getEvaluators())
	    	{
	    		Evaluator evaluator=new Evaluator();
	    		
    			// Copy data from evaluator
    			evaluator.setFromOtherEvaluator(evaluatorFrom);
    			
    			// This is a new evaluator still so we need to reset some fields
    			evaluator.setId(0L);
    			evaluator.setTest(test);
    			
    			// Add new evaluator
    			test.getEvaluators().add(evaluator);
    		}
    		
    		// Add test users
    		test.setTestUsers(new ArrayList<TestUser>());
	    	for (TestUser testUserFrom:testFrom.getTestUsers())
    		{
    			TestUser testUser=new TestUser();
    			
    			// Copy data from test user
    			testUser.setFromOtherTestUser(testUserFrom);
    			
    			// This is a new test user still so we need to reset some fields
    			testUser.setId(0L);
    			testUser.setTest(test);
    			
    			// Add new test user
    			test.getTestUsers().add(testUser);
    		}
    		
    		// This is a new test still so we need to reset some fields
    		test.setId(0L);
    		test.setName("");
    		test.setCreatedBy(userSessionService.getCurrentUser(operation));
    		test.setModifiedBy(null);
    		test.setTimeCreated(null);
    		test.setTimeModified(null);
    		test.setTimeDeployDeploy(null);
    		test.setTimeTestDeploy(null);
    		
    		// Calendar input fields will be reset if current date is after the indicated dates
    		Date currentDate=new Date();
    		if (testFrom.getStartDate()!=null && currentDate.after(testFrom.getStartDate()))
    		{
    			test.setStartDate(null);
    			test.setCloseDate(null);
    			test.setWarningDate(null);
    			test.setFeedbackDate(null);
    		}
    		else
    		{
    			boolean resetCloseDate=false;
    			if (testFrom.getWarningDate()!=null && currentDate.after(testFrom.getWarningDate()))
    			{
    				test.setWarningDate(null);
    				resetCloseDate=true;
    			}
    			if (testFrom.getFeedbackDate()!=null && currentDate.after(testFrom.getFeedbackDate()))
    			{
    				test.setFeedbackDate(null);
    				resetCloseDate=true;
    			}
    			if (resetCloseDate || (testFrom.getCloseDate()!=null && currentDate.after(testFrom.getCloseDate())))
    			{
    				test.setCloseDate(null);
    			}
    		}
    		
    		// Fill test bean field from new test
    		setFromTest(test);
    		
        	// Set category of copied test if current user has access granted, otherwise reset it
    		if (!checkCategory(operation))
    		{
        		List<Category> categories=getTestsCategories(operation);
    			setCategory(categories.isEmpty()?null:categories.get(0));	    			
    		}
    		
       		// We need to initialize special categories filters for the questions within sections tab
   			List<String> allPermissions=new ArrayList<String>();
   			allPermissions.add("PERMISSION_TEST_USE_GLOBAL_QUESTIONS");
   			allPermissions.add("PERMISSION_TEST_USE_OTHER_USERS_QUESTIONS");
   			String categoryGen=localizationService.getLocalizedMessage("CATEGORY_GEN");
   			if ("M".equals(categoryGen))
   			{
   				allCategories=new SpecialCategoryFilter(0L,"ALL_OPTIONS",allPermissions);
   			}
   			else
   			{
   				allCategories=new SpecialCategoryFilter(0L,"ALL_OPTIONS_F",allPermissions);
   			}
       		
   			List<String> allEvenPrivateCategoriesOfOtherUsersPermissions=new ArrayList<String>();
   			allEvenPrivateCategoriesOfOtherUsersPermissions.add("PERMISSION_TEST_USE_GLOBAL_QUESTIONS");
   			allEvenPrivateCategoriesOfOtherUsersPermissions.add("PERMISSION_TEST_USE_OTHER_USERS_QUESTIONS");
   			allEvenPrivateCategoriesOfOtherUsersPermissions.add(
   				"PERMISSION_QUESTIONS_VIEW_QUESTIONS_OF_OTHER_USERS_PRIVATE_CATEGORIES_ENABLED");
   			allEvenPrivateCategories=new SpecialCategoryFilter(
   				-1L,"ALL_EVEN_PRIVATE_CATEGORIES_OF_OTHER_USERS",allEvenPrivateCategoriesOfOtherUsersPermissions);
       		
        	List<String> allMyCategoriesPermissions=new ArrayList<String>();
    		allMyCategoriesPermissions.add("PERMISSION_TEST_USE_GLOBAL_QUESTIONS");
    		allTestAuthorCategories=new SpecialCategoryFilter(-2L,"ALL_MY_CATEGORIES",allMyCategoriesPermissions);
   			
    		allTestAuthorCategoriesExceptGlobals=
    			new SpecialCategoryFilter(-3L,"ALL_MY_CATEGORIES_EXCEPT_GLOBALS",new ArrayList<String>());
   			
    		List<String> allPrivateCategoriesOfOtherUsersPermissions=new ArrayList<String>();
    		allPrivateCategoriesOfOtherUsersPermissions.add("PERMISSION_TEST_USE_OTHER_USERS_QUESTIONS");
   			allPrivateCategoriesOfOtherUsersPermissions.add(
   				"PERMISSION_QUESTIONS_VIEW_QUESTIONS_OF_OTHER_USERS_PRIVATE_CATEGORIES_ENABLED");
   			allPrivateCategories=new SpecialCategoryFilter(
   				-6L,"ALL_PRIVATE_CATEGORIES_OF_OTHER_USERS",allPrivateCategoriesOfOtherUsersPermissions);
   			
        	// We add all initialized categories filters to categories filters map
        	specialCategoryFiltersMap=new LinkedHashMap<Long,SpecialCategoryFilter>();
        	specialCategoryFiltersMap.put(Long.valueOf(allCategories.id),allCategories);
        	specialCategoryFiltersMap.put(Long.valueOf(allEvenPrivateCategories.id),allEvenPrivateCategories);
       		specialCategoryFiltersMap.put(Long.valueOf(allTestAuthorCategories.id),allTestAuthorCategories);
       		specialCategoryFiltersMap.put(
       			Long.valueOf(allTestAuthorCategoriesExceptGlobals.id),allTestAuthorCategoriesExceptGlobals);
       		specialCategoryFiltersMap.put(Long.valueOf(ALL_GLOBAL_CATEGORIES.id),ALL_GLOBAL_CATEGORIES);
        	specialCategoryFiltersMap.put(
        		Long.valueOf(ALL_PUBLIC_CATEGORIES_OF_OTHER_USERS.id),ALL_PUBLIC_CATEGORIES_OF_OTHER_USERS);
       		specialCategoryFiltersMap.put(Long.valueOf(allPrivateCategories.id),allPrivateCategories);
       		specialCategoryFiltersMap.put(Long.valueOf(ALL_CATEGORIES_OF_OTHER_USERS.id),ALL_CATEGORIES_OF_OTHER_USERS);
		}
    	else // New test
    	{
    		User currentUser=userSessionService.getCurrentUser(operation);
        	setTestId(0L);
        	setName("");
        	setAuthor(currentUser);
        	setTimeCreated(null);
        	setTimeTestDeploy(null);
        	setTimeDeployDeploy(null);
        	List<Category> categories=getTestsCategories(operation);
        	setCategory(categories.isEmpty()?null:categories.get(0));
        	setDescription("");
        	setTitle("");
        	setAssessement(assessementsService.getAssessement(operation,"ASSESSEMENT_NOT_ASSESSED"));
        	scoreType=scoreTypesService.getScoreType(operation,"SCORE_TYPE_QUESTIONS");
        	oldScoreType=scoreType;
        	setAllUsersAllowed(true);
        	setAllowAdminReports(true);
        	setStartDate(null);
        	setStartDateHidden("");
        	setCloseDate(null);
        	setCloseDateHidden("");
        	setWarningDate(null);
        	setWarningDateHidden("");
        	setFeedbackDate(null);
        	setFeedbackDateHidden("");
        	setRestrictDates(false);
        	setRestrictFeedbackDate(false);
        	setFreeSummary(false);
        	setFreeStop(true);
        	setSummaryQuestions(false);
        	setSummaryScores(false);
        	setSummaryAttempts(true);
        	setNavigation(true);
        	setNavLocation(navLocationsService.getNavLocation(operation,"NAVLOCATION_LEFT"));
        	setRedoQuestion(redoQuestionValuesService.getRedoQuestion(operation,"NO"));
        	setRedoTest(true);
        	setPresentationTitle("");
        	setPresentation("");
        	setPreliminarySummaryTitle("");
        	setPreliminarySummaryButton("");
        	setPreliminarySummary("");
        	setFeedbackDisplaySummary(true);
        	setFeedbackDisplaySummaryMarks(false);
        	setFeedbackDisplaySummaryAttempts(true);
        	setFeedbackSummaryPrevious("");
        	setFeedbackDisplayScores(true);
        	setFeedbackDisplayScoresMarks(false);
        	setFeedbackDisplayScoresPercentages(true);
        	setFeedbackScoresPrevious("");
        	setFeedbackAdvancedPrevious("");
        	setFeedbackAdvancedNext("");
        	
           	// Users with permission to do this test
        	setTestUsers(new ArrayList<User>());
        	
           	// Users with permission to administrate this test
        	List<User> testAdmins=new ArrayList<User>();
           	setTestAdmins(testAdmins);
           	testAdmins.add(currentUser);
        	
        	// Sections (note that it is important to initialize sections before feedbacks)
           	List<SectionBean> sections=new ArrayList<SectionBean>();
           	setSections(sections);
        	sections.add(new SectionBean(this)); // Initially a new test have one section
        	
        	// Support contacts
        	setSupportContacts(new ArrayList<SupportContactBean>());
        	
        	// Evaluators
        	setEvaluators(new ArrayList<EvaluatorBean>());
        	
       		// Feedbacks
       		setFeedbacks(new ArrayList<TestFeedbackBean>());
       		
       		// We need to initialize special categories filters for the questions within sections tab
   			List<String> allPermissions=new ArrayList<String>();
   			allPermissions.add("PERMISSION_TEST_USE_GLOBAL_QUESTIONS");
   			allPermissions.add("PERMISSION_TEST_USE_OTHER_USERS_QUESTIONS");
   			String categoryGen=localizationService.getLocalizedMessage("CATEGORY_GEN");
   			if ("M".equals(categoryGen))
   			{
   				allCategories=new SpecialCategoryFilter(0L,"ALL_OPTIONS",allPermissions);
   			}
   			else
   			{
   				allCategories=new SpecialCategoryFilter(0L,"ALL_OPTIONS_F",allPermissions);
   			}
       		
    		List<String> allEvenPrivateCategoriesOfOtherUsersPermissions=new ArrayList<String>();
   			allEvenPrivateCategoriesOfOtherUsersPermissions.add("PERMISSION_TEST_USE_GLOBAL_QUESTIONS");
   			allEvenPrivateCategoriesOfOtherUsersPermissions.add("PERMISSION_TEST_USE_OTHER_USERS_QUESTIONS");
   			allEvenPrivateCategoriesOfOtherUsersPermissions.add(
   				"PERMISSION_QUESTIONS_VIEW_QUESTIONS_OF_OTHER_USERS_PRIVATE_CATEGORIES_ENABLED");
   			allEvenPrivateCategories=new SpecialCategoryFilter(
   				-1L,"ALL_EVEN_PRIVATE_CATEGORIES_OF_OTHER_USERS",allEvenPrivateCategoriesOfOtherUsersPermissions);
       		
       		List<String> allMyCategoriesPermissions=new ArrayList<String>();
   			allMyCategoriesPermissions.add("PERMISSION_TEST_USE_GLOBAL_QUESTIONS");
   			allTestAuthorCategories=new SpecialCategoryFilter(-2L,"ALL_MY_CATEGORIES",allMyCategoriesPermissions);
   			
    		allTestAuthorCategoriesExceptGlobals=
    			new SpecialCategoryFilter(-3L,"ALL_MY_CATEGORIES_EXCEPT_GLOBALS",new ArrayList<String>());
   			
    		List<String> allPrivateCategoriesOfOtherUsersPermissions=new ArrayList<String>();
    		allPrivateCategoriesOfOtherUsersPermissions.add("PERMISSION_TEST_USE_OTHER_USERS_QUESTIONS");
   			allPrivateCategoriesOfOtherUsersPermissions.add(
   				"PERMISSION_QUESTIONS_VIEW_QUESTIONS_OF_OTHER_USERS_PRIVATE_CATEGORIES_ENABLED");
   			allPrivateCategories=new SpecialCategoryFilter(
   				-6L,"ALL_PRIVATE_CATEGORIES_OF_OTHER_USERS",allPrivateCategoriesOfOtherUsersPermissions);
   			
   			// We add all initialized categories filters to categories filters map
   			specialCategoryFiltersMap=new LinkedHashMap<Long,SpecialCategoryFilter>();
        	specialCategoryFiltersMap.put(Long.valueOf(allCategories.id),allCategories);
        	specialCategoryFiltersMap.put(Long.valueOf(allEvenPrivateCategories.id),allEvenPrivateCategories);
       		specialCategoryFiltersMap.put(Long.valueOf(allTestAuthorCategories.id),allTestAuthorCategories);
       		specialCategoryFiltersMap.put(
       			Long.valueOf(allTestAuthorCategoriesExceptGlobals.id),allTestAuthorCategoriesExceptGlobals);
       		specialCategoryFiltersMap.put(Long.valueOf(ALL_GLOBAL_CATEGORIES.id),ALL_GLOBAL_CATEGORIES);
       		specialCategoryFiltersMap.put(
       			Long.valueOf(ALL_PUBLIC_CATEGORIES_OF_OTHER_USERS.id),ALL_PUBLIC_CATEGORIES_OF_OTHER_USERS);
       		specialCategoryFiltersMap.put(Long.valueOf(allPrivateCategories.id),allPrivateCategories);
        	specialCategoryFiltersMap.put(Long.valueOf(ALL_CATEGORIES_OF_OTHER_USERS.id),ALL_CATEGORIES_OF_OTHER_USERS);
    	}
    	setCurrentSection(getSections(operation).get(0));
    }
    
	/**
	 * Checks if changing a property of a section will affect to the feedbacks already defined.
	 * @param operation Operation
	 * @return true if changing a property of a section won't affect to the feedbacks already defined, 
	 * false otherwise
	 */
	private boolean checkFeedbacksForChangeProperty(Operation operation)
	{
		boolean ok=true;
		
		// Get current user session Hibernate operation
		operation=getCurrentUserOperation(operation);
		
		for (TestFeedbackBean feedback:getFeedbacks(operation))
		{
			if (feedback.getCondition().getUnit().equals(TestFeedbackConditionBean.MARKS_UNIT))
			{
				int maxConditionalValue=feedback.getCondition().getMaxConditionalValue(operation);
				ok=(feedback.getCondition().getMinValue()<=0 || 
					feedback.getCondition().getMinValue()<=maxConditionalValue) && 
					(feedback.getCondition().getMaxValue()==Integer.MAX_VALUE || 
					feedback.getCondition().getMaxValue()<=maxConditionalValue);
				if (!ok)
				{
					break;
				}
			}
		}
		return ok;
	}
	
	/**
	 * Updates feebacks related to the modification of a property of a test if needed.
	 * @param operation Operation
	 */
	private void updateFeedbacksForChangeProperty(Operation operation)
	{
		// Get current user session Hibernate operation
		operation=getCurrentUserOperation(operation);
		
		for (TestFeedbackBean feedback:getFeedbacks(operation))
		{
			TestFeedbackConditionBean condition=feedback.getCondition();
			if (condition.getUnit().equals(TestFeedbackConditionBean.MARKS_UNIT))
			{
				int maxValue=condition.getMaxConditionalValue(operation);
				if (NumberComparator.compareU(condition.getComparator(),NumberComparator.BETWEEN))					
				{
					if (condition.getConditionalBetweenMax()>maxValue)
					{
						condition.setConditionalBetweenMax(maxValue);
						if (condition.getConditionalBetweenMin()>maxValue)
						{
							condition.setConditionalBetweenMin(maxValue);
						}
					}
				}
				else
				{
					if (NumberComparator.compareU(condition.getComparator(),NumberComparator.GREATER))
					{
						maxValue--;
					}
					if (condition.getConditionalCmp()>maxValue)
					{
						condition.setConditionalCmp(maxValue);
					}
				}
			}
		}
	}
	
	/**
	 * Process a checkbox (all users allowed) of the users tab of the accordion within the common data tab 
	 * of a test.
	 * @param component Component that triggered the listener that called this method
	 */
	private void processAllUsersAllowed(UIComponent component)
	{
		FacesContext context=FacesContext.getCurrentInstance();
		UIInput allUsersAllowed=
			(UIInput)component.findComponent(":testForm:generalAccordion:allUsersAllowed");
		allUsersAllowed.processDecodes(context);
		if (allUsersAllowed.getSubmittedValue()!=null)
		{
			setAllUsersAllowed(Boolean.valueOf((String)allUsersAllowed.getSubmittedValue()));
		}
	}
	
	/**
	 * Process a checkbox (restrict dates) of the calendar tab of the accordion within the common data tab 
	 * of a test.
	 * @param component Component that triggered the listener that called this method
	 */
	private void processRestrictDates(UIComponent component)
	{
		FacesContext context=FacesContext.getCurrentInstance();
		UIInput restrictDates=(UIInput)component.findComponent(":testForm:generalAccordion:restrictDates");
		restrictDates.processDecodes(context);
		if (restrictDates.getSubmittedValue()!=null)
		{
			setRestrictDates(Boolean.valueOf((String)restrictDates.getSubmittedValue()));
		}
	}
	
	/**
	 * Process a checkbox (restrict feedback date) of the calendar tab of the accordion within 
	 * the common data tab of a test.
	 * @param component Component that triggered the listener that called this method
	 */
	private void processRestrictFeedbackDate(UIComponent component)
	{
		FacesContext context=FacesContext.getCurrentInstance();
		UIInput restrictFeedbackDate=
			(UIInput)component.findComponent(":testForm:generalAccordion:restrictFeedbackDate");
		restrictFeedbackDate.processDecodes(context);
		if (restrictFeedbackDate.getSubmittedValue()!=null)
		{
			setRestrictFeedbackDate(Boolean.valueOf((String)restrictFeedbackDate.getSubmittedValue()));
		}
	}
	
	/**
	 * Process a checkbox (shuffle) of a section  within the sections tab of a test.
	 * @param operation Operation
	 * @param component Component that triggered the listener that called this method
	 * @param section Section
	 */
	private void processSectionShuffle(Operation operation,UIComponent component,SectionBean section)
	{
		List<String> exceptions=new ArrayList<String>();
		exceptions.add("sectionName");
		exceptions.add("sectionTitle");
		exceptions.add("sectionWeight");
		exceptions.add("random");
		exceptions.add("randomQuantity");
		exceptions.add("questionOrderWeight");
		processSectionsInputFields(getCurrentUserOperation(operation),component,section,exceptions);
	}
	
	/**
	 * Process a checkbox (random) of a section  within the sections tab of a test.
	 * @param operation Operation
	 * @param component Component that triggered the listener that called this method
	 * @param section Section
	 */
	private void processSectionRandom(Operation operation,UIComponent component,SectionBean section)
	{
		List<String> exceptions=new ArrayList<String>();
		exceptions.add("sectionName");
		exceptions.add("sectionTitle");
		exceptions.add("sectionWeight");
		exceptions.add("shuffle");
		exceptions.add("randomQuantity");
		exceptions.add("questionOrderWeight");
		processSectionsInputFields(getCurrentUserOperation(operation),component,section,exceptions);
	}
	
	/**
	 * Process a checkbox (feedback display summary) of the summary tab of the accordion within 
	 * the feedback tab of a test.
	 * @param component Component that triggered the listener that called this method
	 */
	private void processFeedbackDisplaySummary(UIComponent component)
	{
		FacesContext context=FacesContext.getCurrentInstance();
		UIInput feedbackDisplaySummary=
			(UIInput)component.findComponent(":testForm:feedbackAccordion:feedbackDisplaySummary");
		feedbackDisplaySummary.processDecodes(context);
		if (feedbackDisplaySummary.getSubmittedValue()!=null)
		{
			setFeedbackDisplaySummary(Boolean.valueOf((String)feedbackDisplaySummary.getSubmittedValue()));
		}
	}
	
	/**
	 * Process a checkbox (feedback display scores) of the scores tab of the accordion within the feedback tab 
	 * of a test.
	 * @param component Component that triggered the listener that called this method
	 */
	private void processFeedbackDisplayScores(UIComponent component)
	{
		FacesContext context=FacesContext.getCurrentInstance();
		UIInput feedbackDisplayScores=
			(UIInput)component.findComponent(":testForm:feedbackAccordion:feedbackDisplayScores");
		feedbackDisplayScores.processDecodes(context);
		if (feedbackDisplayScores.getSubmittedValue()!=null)
		{
			setFeedbackDisplayScores(Boolean.valueOf((String)feedbackDisplayScores.getSubmittedValue()));
		}
	}
	
	/**
	 * Ajax listener to perform checks when modifying a property.
	 * @param event Ajax event
	 */
	public void changeProperty(AjaxBehaviorEvent event)
	{
		// If there is a property checked it is because checks have been done by other listener,
		// so we dont't duplicate checks
		if (getPropertyChecked()==null)
		{
			// Get current user session Hibernate operation
			Operation operation=getCurrentUserOperation(null);
			
			boolean ok=true;
			boolean needConfirm=false;
			String property=(String)event.getComponent().getAttributes().get("property");
			if ("scoreType".equals(property))
			{
				needConfirm=!checkFeedbacksForChangeProperty(operation);
			}
			else if ("allUsersAllowed".equals(property))
			{
				if (!isEnabledChecboxesSetters())
				{
					setEnabledCheckboxesSetters(true);
					processAllUsersAllowed(event.getComponent());
				}
			}
			else if ("restrictDates".equals(property))
			{
				if (!isEnabledChecboxesSetters())
				{
					setEnabledCheckboxesSetters(true);
					
	    			// Process the checkbox to restrict (or not) dates for doing the test
	    			// (listener will be invoked again)
					processRestrictDates(event.getComponent());
				}
			}
			else if ("restrictFeedbackDate".equals(property))
			{
				if (!isEnabledChecboxesSetters())
				{
					setEnabledCheckboxesSetters(true);
					
	    			// Process the checkbox to restrict (or not) the date for feedback
	    			// (listener will be invoked again)
					processRestrictFeedbackDate(event.getComponent());
				}
			}
			else if ("sectionWeight".equals(property))
			{
				sectionChecked=(SectionBean)event.getComponent().getAttributes().get("section");
				if (sectionChecked!=null)
				{
					// We need to process some input fields
					List<String> exceptions=new ArrayList<String>(1);
					exceptions.add("sectionWeight");
					processSectionsInputFields(operation,event.getComponent(),sectionChecked,exceptions);
					
					// Check that current section name entered by user is valid
					if (checkSectionName(sectionChecked.getName()))
					{
						boolean checkFeedbacks=sectionChecked.getWeight()>=getMinWeight() && 
							sectionChecked.getWeight()<=getMaxWeight();
						if (checkFeedbacks)
						{
							checkFeedbacks=checkFeedbacksForChangeProperty(operation);
							needConfirm=!checkFeedbacks;
						}
						else
						{
							needConfirm=false;
						}
						if (!needConfirm)
						{
							if (sectionChecked.getWeight()<getMinWeight())
							{
								if (checkFeedbacks)
								{
									sectionChecked.setWeight(getMinWeight());
								}
								else
								{
									sectionChecked.rollbackWeight();
								}
								
								// We need to update section weights
								updateSectionWeight(operation,event.getComponent(),sectionChecked);
							}
							else if (sectionChecked.getWeight()>getMaxWeight())
							{
								sectionChecked.setWeight(getMaxWeight());
							}
							sectionChecked.acceptWeight();
							sectionChecked=null;
							questionOrderChecked=null;
						}
					}
					else
					{
						ok=false;
						
						// Restore old section name
						sectionChecked.setName(activeSectionName);
						
						// Restore old section weight
						sectionChecked.rollbackWeight();
						
						// Scroll page to top position
						scrollToTop();
					}
				}
			}
			else if ("shuffle".equals(property))
			{
				SectionBean section=(SectionBean)event.getComponent().getAttributes().get("section");
		    	if (section!=null && !section.isShuffle())
		    	{
		    		if (isEnabledChecboxesSetters())
		    		{
						// We need to process random quantity
						processSectionRandomQuantity(operation,event.getComponent(),section);
						
			    		section.setRandom(false);
		    		}
		    		else
		    		{
		    			setEnabledCheckboxesSetters(true);
		    			
		    			// Process the checkbox to shuffle questions within a section 
		    			// (listener will be invoked again)
		    			processSectionShuffle(operation,event.getComponent(),section);
		    		}
		    	}
			}
			else if ("random".equals(property))
			{
				SectionBean section=(SectionBean)event.getComponent().getAttributes().get("section");
				if (section!=null)
				{
					if (isEnabledChecboxesSetters())
					{
						if (!section.isRandom())
						{
							// We need to process random quantity
							processSectionRandomQuantity(operation,event.getComponent(),section);
						}
						
						if (!(section.isRandom() && checkFeedbacksForChangeProperty(operation)) || 
							(section.isRandom() && section.getRandomQuantity()<=0))
						{
							section.setRandomQuantity(section.getQuestionOrdersSize());
							section.acceptRandomQuantity();
						}
						if (section.isRandom())
						{
							// We need to set weight of all questions referenced by this section to 1
							for (QuestionOrderBean questionOrder:section.getQuestionOrders())
							{
								questionOrder.setWeight(1);
							}
							
							// We need to update random quantity
							updateSectionRandomQuantity(operation,event.getComponent(),section);
						}
					}
					else
					{
						setEnabledCheckboxesSetters(true);
						
		    			// Process the checkbox to select randomly the questions to display within a section 
		    			// (listener will be invoked again)
						processSectionRandom(operation,event.getComponent(),section);
					}
				}
			}
			else if ("randomQuantity".equals(property))
			{
				sectionChecked=(SectionBean)event.getComponent().getAttributes().get("section");
				if (sectionChecked!=null)
				{
					// We need to process some input fields
					List<String> exceptions=new ArrayList<String>(1);
					exceptions.add("randomQuantity");
					processSectionsInputFields(operation,event.getComponent(),sectionChecked,exceptions);
					
					// Check that current section name entered by user is valid
					if (checkSectionName(sectionChecked.getName()))
					{
						boolean checkFeedbacks=sectionChecked.getRandomQuantity()>0 && 
							sectionChecked.getRandomQuantity()<=sectionChecked.getQuestionOrdersSize();
						if (checkFeedbacks)
						{
							checkFeedbacks=checkFeedbacksForChangeProperty(operation);
							needConfirm=!checkFeedbacks;
						}
						else
						{
							needConfirm=false;
						}
						if (!needConfirm)
						{
							if (sectionChecked.getRandomQuantity()<0)
							{
								if (checkFeedbacks)
								{
									sectionChecked.setRandomQuantity(sectionChecked.getQuestionOrdersSize()==0?0:1);
								}
								else
								{
									sectionChecked.rollbackRandomQuantity();
								}
								
								// We need to update random quantity
								updateSectionRandomQuantity(operation,event.getComponent(),sectionChecked);
							}
							else if (sectionChecked.getRandomQuantity()==0 && sectionChecked.getQuestionOrdersSize()>0)
							{
								if (checkFeedbacks)
								{
									sectionChecked.setRandomQuantity(1);
								}
								else
								{
									sectionChecked.rollbackRandomQuantity();
								}
								
								// We need to update random quantity
								updateSectionRandomQuantity(operation,event.getComponent(),sectionChecked);
							}
							else if (sectionChecked.getRandomQuantity()>sectionChecked.getQuestionOrdersSize())
							{
								sectionChecked.setRandomQuantity(sectionChecked.getQuestionOrdersSize());
								
								// We need to update random quantity
								updateSectionRandomQuantity(operation,event.getComponent(),sectionChecked);
							}
							sectionChecked.acceptRandomQuantity();
							sectionChecked=null;
							questionOrderChecked=null;
						}
					}
					else
					{
						ok=false;
						
						// Restore old section name
						sectionChecked.setName(activeSectionName);
						
						// Restore old section random quantity
						sectionChecked.rollbackRandomQuantity();
						
						// Scroll page to top position
						scrollToTop();
					}
				}
			}
			else if ("questionOrderWeight".equals(property))
			{
				sectionChecked=(SectionBean)event.getComponent().getAttributes().get("section");
				questionOrderChecked=(QuestionOrderBean)event.getComponent().getAttributes().get("questionOrder");
				if (sectionChecked!=null && questionOrderChecked!=null)
				{
					// We need to process some input fields
					List<String> exceptions=new ArrayList<String>(1);
					exceptions.add("questionOrderWeight");
					processSectionsInputFields(operation,event.getComponent(),sectionChecked,exceptions);
					
					// Check that current section name entered by user is valid
					if (checkSectionName(sectionChecked.getName()))
					{
						boolean checkFeedbacks=questionOrderChecked.getWeight()>=getMinWeight() &&
							questionOrderChecked.getWeight()<=getMaxWeight();
						if (checkFeedbacks)
						{
							checkFeedbacks=checkFeedbacksForChangeProperty(operation);
							needConfirm=!checkFeedbacks;
						}
						else
						{
							needConfirm=false;
						}
						if (!needConfirm)
						{
							if (questionOrderChecked.getWeight()<getMinWeight())
							{
								if (checkFeedbacks)
								{
									questionOrderChecked.setWeight(getMinWeight());
								}
								else
								{
									questionOrderChecked.rollbackWeight();
								}
								
								// We need to update questions weights
								updateQuestionOrderWeights(operation,event.getComponent(),sectionChecked);
							}
							else if (questionOrderChecked.getWeight()>getMaxWeight())
							{
								questionOrderChecked.setWeight(getMaxWeight());
							}
	   						for (QuestionOrderBean questionOrder:sectionChecked.getQuestionOrders())
	   						{
	   							questionOrder.acceptWeight();
	   						}
							sectionChecked=null;
							questionOrderChecked=null;
						}
					}
					else
					{
						ok=false;
						
						// Restore old section name
						sectionChecked.setName(activeSectionName);
						
						// Restore old question weight
						questionOrderChecked.rollbackWeight();
						
						// Scroll page to top position
						scrollToTop();
					}
				}
			}
			else if ("feedbackDisplaySummary".equals(property))
			{
				if (!isEnabledChecboxesSetters())
				{
					setEnabledCheckboxesSetters(true);
					
	    			// Process the checkbox to display summary (or not) within the feedback
	    			// (listener will be invoked again)
					processFeedbackDisplaySummary(event.getComponent());
				}
			}
			else if ("feedbackDisplayScores".equals(property))
			{
				if (!isEnabledChecboxesSetters())
				{
					setEnabledCheckboxesSetters(true);
					
	    			// Process the checkbox to display scores (or not) within the feedback
	    			// (listener will be invoked again)
					processFeedbackDisplayScores(event.getComponent());
				}
			}
			if (ok && needConfirm)
			{
				setPropertyChecked(property);
				RequestContext rq=RequestContext.getCurrentInstance();
				rq.execute("confirmChangePropertyDialog.show()");
			}
		}
	}
    
	/**
	 * Action listener to make needed changes if we confirm changing a property.
	 * @param event Action event
	 */
	public void confirmChangeProperty(ActionEvent event)
	{
		// Check the property changed
		String property=getPropertyChecked();
		if (property!=null)
		{
			// Get current user session Hibernate operation
			Operation operation=getCurrentUserOperation(null);
			
			// We need to update feedbacks to valid values
			updateFeedbacksForChangeProperty(operation);
			
			if ("sectionWeight".equals(property))
			{
				// We need to update section weights
				updateSectionWeights(operation,event.getComponent(),sectionChecked);
				sectionChecked.acceptWeight();
			}
			else if ("randomQuantity".equals(property))
			{
				// We need to update section random quantity
				updateSectionRandomQuantity(operation,event.getComponent(),sectionChecked);
				sectionChecked.acceptRandomQuantity();
			}
			else if ("questionOrderWeight".equals(property))
			{
				// We need to update questions weights
				updateQuestionOrderWeights(operation,event.getComponent(),sectionChecked);
				for (QuestionOrderBean questionOrder:sectionChecked.getQuestionOrders())
				{
					questionOrder.acceptWeight();
				}
			}
			
			// Reset property checked
			setPropertyChecked(null);
			sectionChecked=null;
			questionOrderChecked=null;
			
			if (nextSectionIndexOnChangePropertyConfirm!=-1)
			{
				activeSectionIndex=nextSectionIndexOnChangePropertyConfirm;
				activeSectionName=getSection(operation,nextSectionIndexOnChangePropertyConfirm+1).getName();
				refreshActiveSection(operation,event.getComponent());
			}
			else if (nextTestTabNameOnChangePropertyConfirm!=null)
			{
				Wizard testFormWizard=(Wizard)event.getComponent().findComponent(":testForm:testFormWizard");
				activeTestTabName=nextTestTabNameOnChangePropertyConfirm;
				testFormWizard.setStep(nextTestTabNameOnChangePropertyConfirm);
			}
			else if (nextTestIndexOnChangePropertyConfirm!=-1)
			{
				// I don't know why but this code doesn't work even if 'update' includes the tabview identifier,
				// so I need to use a callback to client to change tab with help of javascript
				/*
				TabView testFormTabs=(TabView)event.getComponent().findComponent(":testForm:testFormTabs");
				activeTestIndex=nextTestIndexOnChangePropertyConfirm;
				testFormTabs.setActiveIndex(nextTestIndexOnChangePropertyConfirm);
				*/
				RequestContext rq=RequestContext.getCurrentInstance();
				StringBuffer changeTabCommand=new StringBuffer("testFormTabs.select(");
				changeTabCommand.append(nextTestIndexOnChangePropertyConfirm);
				changeTabCommand.append(')');
				rq.execute(changeTabCommand.toString());
			}
		}
		nextSectionIndexOnChangePropertyConfirm=-1;
		nextTestTabNameOnChangePropertyConfirm=null;
		nextTestIndexOnChangePropertyConfirm=-1;
	}
	
	/**
	 * Action listener to rollback the old value of the last property modified.
	 * @param event Action event
	 */
	public void rollbackProperty(ActionEvent event)
	{
		String property=getPropertyChecked();
		if (property!=null)
		{
			if ("scoreType".equals(property))
			{
				rollbackScoreType();
			}
			else if ("sectionWeight".equals(property))
			{
				if (sectionChecked!=null)
				{
					sectionChecked.rollbackWeight();
					
					// We need to update section weights
					updateSectionWeights(getCurrentUserOperation(null),event.getComponent(),sectionChecked);
					sectionChecked.acceptWeight();
				}
			}
			else if ("randomQuantity".equals(property))
			{
				if (sectionChecked!=null)
				{
					sectionChecked.rollbackRandomQuantity();
					
					// We need to update random quantity
					updateSectionRandomQuantity(getCurrentUserOperation(null),event.getComponent(),sectionChecked);
					sectionChecked.acceptRandomQuantity();
				}
			}
			else if ("questionOrderWeight".equals(property))
			{
				if (sectionChecked!=null && questionOrderChecked!=null)
				{
					questionOrderChecked.rollbackWeight();
					
					// We need to update questions weights
					updateQuestionOrderWeights(getCurrentUserOperation(null),event.getComponent(),sectionChecked);
					for (QuestionOrderBean questionOrder:sectionChecked.getQuestionOrders())
					{
						questionOrder.acceptWeight();
					}
				}
			}
			
			// Reset property checked
			setPropertyChecked(null);
			sectionChecked=null;
			questionOrderChecked=null;
		}
		nextSectionIndexOnChangePropertyConfirm=-1;
		nextTestTabNameOnChangePropertyConfirm=null;
		nextTestIndexOnChangePropertyConfirm=-1;
	}
    
    /**
     * @param questionId Question's identifier
     * @return Question
     */
    public Question getQuestion(long questionId)
    {
    	return getQuestion(null,questionId);
    }
    
    /**
     * @param operation Operation
     * @param questionId Question's identifier
     * @return Question
     */
    public Question getQuestion(Operation operation,long questionId)
    {
    	return questionsService.getQuestion(getCurrentUserOperation(operation),questionId);
    }
    
    /**
     * @return Questions used in this test
     */
    public List<Question> getUsedQuestions()
    {
    	return getUsedQuestions(null);
    }
    
    /**
     * @param operation Operation
     * @return Questions used in this test
     */
    private List<Question> getUsedQuestions(Operation operation)
    {
    	List<Question> usedQuestions=new ArrayList<Question>();
    	for (SectionBean section:getSections(getCurrentUserOperation(operation)))
    	{
    		for (Question question:section.getQuestions())
    		{
    			usedQuestions.add(question);
    		}
    	}
    	return usedQuestions;
    }
    
    /**
     * @return Questions for sections tab
     */
	public List<Question> getQuestions()
	{
		return getQuestions(null);
	}
    
    /**
     * @param operation operation
     * @return Questions for sections tab
     */
	private List<Question> getQuestions(Operation operation)
	{
		if (questions==null)
		{
    		try
    		{
    			// Get current user session Hibernate operation
    			operation=getCurrentUserOperation(operation);
    			
    			User testAuthor=getAuthor(operation);
    			
    			if (checkQuestionsFilterPermission(operation,null))   
    			{
    				long filterCategoryId=getFilterCategoryId(operation);
    				if (specialCategoryFiltersMap.containsKey(Long.valueOf(filterCategoryId)))
    				{
    					SpecialCategoryFilter filter=specialCategoryFiltersMap.get(Long.valueOf(filterCategoryId));
    					if (allCategories.equals(filter))
    					{
    						questions=questionsService.getAllVisibleCategoriesQuestions(
    							operation,testAuthor,null,getFilterQuestionType(),getFilterQuestionLevel());
    					}
    					else if (allEvenPrivateCategories.equals(filter))
    					{
    						questions=questionsService.getAllCategoriesQuestions(
    							operation,testAuthor,null,getFilterQuestionType(),getFilterQuestionLevel());
    					}
    					else if (allTestAuthorCategories.equals(filter))
    					{
    						questions=questionsService.getAllMyCategoriesQuestions(
    							operation,testAuthor,null,getFilterQuestionType(),getFilterQuestionLevel());
    					}
    					else if (allTestAuthorCategoriesExceptGlobals.equals(filter))
    					{
    						questions=questionsService.getAllMyCategoriesExceptGlobalsQuestions(
    							operation,testAuthor,null,getFilterQuestionType(),getFilterQuestionLevel());
    					}
    					else if (ALL_GLOBAL_CATEGORIES.equals(filter))
    					{
    						questions=questionsService.getAllGlobalCategoriesQuestions(
    							operation,testAuthor,null,getFilterQuestionType(),getFilterQuestionLevel());
    					}
    					else if (ALL_PUBLIC_CATEGORIES_OF_OTHER_USERS.equals(filter))
    					{
    						questions=questionsService.getAllPublicCategoriesOfOtherUsersQuestions(
    							operation,testAuthor,null,getFilterQuestionType(),getFilterQuestionLevel());
    					}
    					else if (allPrivateCategories.equals(filter))
    					{
    						questions=questionsService.getAllPrivateCategoriesOfOtherUsersQuestions(
    							operation,testAuthor,null,getFilterQuestionType(),getFilterQuestionLevel());
    					}
    					else if (ALL_CATEGORIES_OF_OTHER_USERS.equals(filter))
    					{
    						questions=questionsService.getAllCategoriesOfOtherUsersQuestions(
    							operation,testAuthor,null,getFilterQuestionType(),getFilterQuestionLevel());
    					}
    				}
    				else
    				{
    					questions=questionsService.getQuestions(operation,testAuthor,null,filterCategoryId,
    						isFilterIncludeSubcategories(),getFilterQuestionType(),getFilterQuestionLevel());
    				}
    				
    				// We need to remove questions not visible for current user
    				List<Question> questionsToRemove=new ArrayList<Question>();
    				Map<Category,Boolean> questionsCategories=new HashMap<Category,Boolean>();
    				for (Question question:questions)
    				{
    					boolean checkQuestionCategory=false;
    					Category questionCategory=question.getCategory();
    					if (questionsCategories.containsKey(questionCategory))
    					{
    						checkQuestionCategory=questionsCategories.get(questionCategory).booleanValue();
    					}
    					else
    					{
    						checkQuestionCategory=checkQuestionCategoryFilterPermission(operation,questionCategory); 
    						questionsCategories.put(questionCategory,checkQuestionCategory);
    					}
    					if (!checkQuestionCategory)
    					{
    						questionsToRemove.add(question);
    					}
    				}
    			}
    		}
    		finally
    		{
				// It is not a good idea to return null even if an error is produced because JSF getters 
				// are usually called several times
    			if (questions==null)
    			{
					questions=new ArrayList<Question>();
    			}
    		}
		}
		return questions;
	}
	
	/**
	 * @return Filtered users for the 'Add users' dialog
	 */
	public List<User> getFilteredUsersForAddingUsers()
	{
		return getFilteredUsersForAddingUsers(null);
	}
	
	/**
	 * @param filteredUsersForAddingUsers Filtered users for the 'Add users' dialog
	 */
	public void setFilteredUsersForAddingUsers(List<User> filteredUsersForAddingUsers)
	{
		this.filteredUsersForAddingUsers=filteredUsersForAddingUsers;
	}
	
	/**
	 * @param operation Operation
	 * @return Filtered users for the 'Add users' dialog
	 */
	private List<User> getFilteredUsersForAddingUsers(Operation operation)
	{
		if (filteredUsersForAddingUsers==null)
		{
			// Get current user session Hibernate operation
			operation=getCurrentUserOperation(operation);
			
			if (getFilterUsersUserTypeId()==-1L)
			{
				filteredUsersForAddingUsers=
					usersService.getSortedUsersWithoutUserType(operation,isFilterUsersIncludeOmUsers());
			}
			else
			{
				filteredUsersForAddingUsers=
					usersService.getSortedUsers(operation,getFilterUsersUserTypeId(),isFilterUsersIncludeOmUsers());
			}
		}
		return filteredUsersForAddingUsers;
	}
	
	/**
	 * @return Filtered users for the 'Add admins' dialog
	 */
	public List<User> getFilteredUsersForAddingAdmins()
	{
		return getFilteredUsersForAddingAdmins(null);
	}
	
	/**
	 * @param filteredUsersForAddingAdmins Filtered users for the 'Add admins' dialog
	 */
	public void setFilteredUsersForAddingAdmins(List<User> filteredUsersForAddingAdmins)
	{
		this.filteredUsersForAddingAdmins=filteredUsersForAddingAdmins;
	}
	
	/**
	 * @param operation Operation
	 * @return Filtered users for the 'Add admins' dialog
	 */
	private List<User> getFilteredUsersForAddingAdmins(Operation operation)
	{
		if (filteredUsersForAddingAdmins==null)
		{
			// Get current user session Hibernate operation
			operation=getCurrentUserOperation(operation);
			
			if (getFilterAdminsUserTypeId()==-1L)
			{
				filteredUsersForAddingAdmins=
					usersService.getSortedUsersWithoutUserType(operation,isFilterAdminsIncludeOmUsers());
			}
			else
			{
				filteredUsersForAddingAdmins=
					usersService.getSortedUsers(operation,getFilterAdminsUserTypeId(),isFilterAdminsIncludeOmUsers());
			}
		}
		return filteredUsersForAddingAdmins;
	}
	
	/**
	 * @return Filtered users for the "Add/Edit tech support address" dialog
	 */
	public List<User> getFilteredUsersForAddingSupportContactFilterUsers()
	{
		return getFilteredUsersForAddingSupportContactFilterUsers(null);
	}
	
	/**
	 * @param filteredUsersForAddingSupportContactFilterUsers Filtered users for the 
	 * 'Add/Edit tech support address' dialog
	 */
	public void setFilteredUsersForAddingSupportContactFilterUsers(
		List<User> filteredUsersForAddingSupportContactFilterUsers)
	{
		this.filteredUsersForAddingSupportContactFilterUsers=filteredUsersForAddingSupportContactFilterUsers;
	}
	
	/**
	 * @param operation Operation
	 * @return Filtered users for the "Add/Edit tech support address" dialog
	 */
	private List<User> getFilteredUsersForAddingSupportContactFilterUsers(Operation operation)
	{
		if (filteredUsersForAddingSupportContactFilterUsers==null)
		{
			// Get current user session Hibernate operation
			operation=getCurrentUserOperation(operation);
			
			if (getFilterSupportContactFilterUsersUserTypeId()==-1L)
			{
				filteredUsersForAddingSupportContactFilterUsers=usersService.getSortedUsersWithoutUserType(
					operation,isFilterSupportContactFilterUsersIncludeOmUsers());
			}
			else
			{
				filteredUsersForAddingSupportContactFilterUsers=usersService.getSortedUsers(operation,
					getFilterSupportContactFilterUsersUserTypeId(),isFilterSupportContactFilterUsersIncludeOmUsers());
			}
			if (!isAllUsersAllowed(operation))
			{
				List<User> testUsers=getTestUsers(operation);
				List<User> testAdmins=getTestAdmins(operation);
				List<User> usersNotAllowedToDoTest=new ArrayList<User>();
				for (User user:filteredUsersForAddingSupportContactFilterUsers)
				{
					if (!testUsers.contains(user) && !testAdmins.contains(user))
					{
						usersNotAllowedToDoTest.add(user);
					}
				}
				for (User userNotAllowedToDoTest:usersNotAllowedToDoTest)
				{
					filteredUsersForAddingSupportContactFilterUsers.remove(userNotAllowedToDoTest);
				}
			}
		}
		return filteredUsersForAddingSupportContactFilterUsers;
	}
	
	/**
	 * @return Filtered users for the "Add/Edit assessement address" dialog
	 */
	public List<User> getFilteredUsersForAddingEvaluatorFilterUsers()
	{
		return getFilteredUsersForAddingEvaluatorFilterUsers(null);
	}
	
	/**
	 * @param filteredUsersForAddingEvaluatorFilterUsers Filtered users for the 
	 * "Add/Edit assessement address" dialog
	 */
	public void setFilteredUsersForAddingEvaluatorFilterUsers(List<User> filteredUsersForAddingEvaluatorFilterUsers)
	{
		this.filteredUsersForAddingEvaluatorFilterUsers=filteredUsersForAddingEvaluatorFilterUsers;
	}
	
	/**
	 * @param operation Operation
	 * @return Filtered users for the "'Add/Edit assessement address" dialog
	 */
	private List<User> getFilteredUsersForAddingEvaluatorFilterUsers(Operation operation)
	{
		if (filteredUsersForAddingEvaluatorFilterUsers==null)
		{
			// Get current user session Hibernate operation
			operation=getCurrentUserOperation(operation);
			
			if (getFilterEvaluatorFilterUsersUserTypeId()==-1L)
			{
				filteredUsersForAddingEvaluatorFilterUsers=
					usersService.getSortedUsersWithoutUserType(operation,isFilterEvaluatorFilterUsersIncludeOmUsers());
			}
			else
			{
				filteredUsersForAddingEvaluatorFilterUsers=usersService.getSortedUsers(
					operation,getFilterEvaluatorFilterUsersUserTypeId(),isFilterEvaluatorFilterUsersIncludeOmUsers());
			}
			
			if (!isAllUsersAllowed(operation))
			{
				List<User> testUsers=getTestUsers(operation);
				List<User> testAdmins=getTestAdmins(operation);
				List<User> usersNotAllowedToDoTest=new ArrayList<User>();
				for (User user:filteredUsersForAddingEvaluatorFilterUsers)
				{
					if (!testUsers.contains(user) && !testAdmins.contains(user))
					{
						usersNotAllowedToDoTest.add(user);
					}
				}
				for (User userNotAllowedToDoTest:usersNotAllowedToDoTest)
				{
					filteredUsersForAddingEvaluatorFilterUsers.remove(userNotAllowedToDoTest);
				}
			}
		}
		return filteredUsersForAddingEvaluatorFilterUsers;
	}
	
	public List<UserType> getUserTypes()
	{
		return getUserTypes(null);
	}
	
	private List<UserType> getUserTypes(Operation operation)
	{
		if (userTypes==null)
		{
			userTypes=userTypesService.getUserTypes(getCurrentUserOperation(operation));
		}
		return userTypes; 
	}
	
	public void setUserTypes(List<UserType> userTypes)
	{
		this.userTypes=userTypes;
	}
	
	public long getFilterUsersUserTypeId()
	{
		return filterUsersUserTypeId;
	}
	
	public void setFilterUsersUserTypeId(long filterUsersUserTypeId)
	{
		this.filterUsersUserTypeId=filterUsersUserTypeId;
	}
	
	public boolean isFilterUsersIncludeOmUsers()
	{
		return filterUsersIncludeOmUsers;
	}
	
	public void setFilterUsersIncludeOmUsers(boolean filterUsersIncludeOmUsers)
	{
		this.filterUsersIncludeOmUsers=filterUsersIncludeOmUsers;
	}
	
	/**
	 * @return Users with permission to do this test
	 */
	public List<User> getTestUsers()
	{
		return getTestUsers(null);
	}
	
	/**
	 * @param testUsers Users with permission to do this test
	 */
	public void setTestUsers(List<User> testUsers)
	{
		this.testUsers=testUsers;
	}
	
	/**
	 * @param operation Operation
	 * @return Users with permission to do this test
	 */
	private List<User> getTestUsers(Operation operation)
	{
    	if (!testInitialized)
    	{
    		initializeTest(operation);
    	}
    	return testUsers;
	}
	
	/**
	 * @return Users without permission to do this test (except if it is allowed that all users do the test)
	 */
	public List<User> getAvailableUsers()
	{
		return getAvailableUsers(null);
	}
	
	/**
	 * @param operation Operation
	 * @return Users without permission to do this test (except if it is allowed that all users do the test)
	 */
	private List<User> getAvailableUsers(Operation operation)
	{
		List<User> availableUsers=new ArrayList<User>();
		
		// Get current user session Hibernate operation
		operation=getCurrentUserOperation(operation);
		
		List<User> testUsers=getTestUsers(operation);
		for (User user:getFilteredUsersForAddingUsers(operation))
		{
			if (!testUsers.contains(user))
			{
				User availableUser=new User();
				availableUser.setFromOtherUser(user);
				availableUsers.add(availableUser);
			}
		}
		return availableUsers;
	}
	
	public long getFilterAdminsUserTypeId()
	{
		return filterAdminsUserTypeId;
	}
	
	public void setFilterAdminsUserTypeId(long filterAdminsUserTypeId)
	{
		this.filterAdminsUserTypeId=filterAdminsUserTypeId;
	}
	
	public boolean isFilterAdminsIncludeOmUsers()
	{
		return filterAdminsIncludeOmUsers;
	}
	
	public void setFilterAdminsIncludeOmUsers(boolean filterAdminsIncludeOmUsers)
	{
		this.filterAdminsIncludeOmUsers=filterAdminsIncludeOmUsers;
	}
	
	/**
	 * @return Users with permission to administrate this test
	 */
	public List<User> getTestAdmins()
	{
		return getTestAdmins(null);
	}
	
	/**
	 * @param testAdmins Users with permission to administrate this test
	 */
	public void setTestAdmins(List<User> testAdmins)
	{
		this.testAdmins=testAdmins;
	}
	
	/**
	 * @param operation Operation
	 * @return Users with permission to administrate this test
	 */
	private List<User> getTestAdmins(Operation operation)
	{
    	if (!testInitialized)
    	{
    		initializeTest(operation);
    	}
    	return testAdmins;
	}
	
	/**
	 * @return Users without permission to administrate this test
	 */
	public List<User> getAvailableAdmins()
	{
		return getAvailableAdmins(null);
	}
	
	/**
	 * @param operation Operation
	 * @return Users without permission to administrate this test
	 */
	private List<User> getAvailableAdmins(Operation operation)
	{
		List<User> availableAdmins=new ArrayList<User>();
		
		// Get current user session Hibernate operation
		operation=getCurrentUserOperation(operation);
		
		List<User> testAdmins=getTestAdmins(operation);
		for (User user:getFilteredUsersForAddingAdmins(operation))
		{
			if (!testAdmins.contains(user))
			{
				User availableAdmin=new User();
				availableAdmin.setFromOtherUser(user);
				availableAdmins.add(availableAdmin);
			}
		}
		return availableAdmins;
	}
	
	/**
	 * Change questions to display on picklist based on filter. 
	 * @param event Action event
	 */
	public void applyQuestionsFilter(ActionEvent event)
	{
    	// Get current user session Hibernate operation
		Operation operation=getCurrentUserOperation(null);
    	
    	setUseGlobalQuestions(null);
    	setUseOtherUsersQuestions(null);
        Category filterCategory=null;
        long filterCategoryId=getFilterCategoryId(operation);
        if (filterCategoryId>0L)
        {
        	filterCategory=categoriesService.getCategory(operation,filterCategoryId);
        	resetAdminFromCategoryAllowed(filterCategory);
        	resetSuperadminFromCategoryAllowed(filterCategory);
        }
    	setViewQuestionsFromOtherUsersPrivateCategoriesEnabled(null);
        setViewQuestionsFromAdminsPrivateCategoriesEnabled(null);
        setViewQuestionsFromSuperadminsPrivateCategoriesEnabled(null);
        setTestAuthorViewQuestionsFromOtherUsersPrivateCategoriesEnabled(null);
        setTestAuthorViewQuestionsFromAdminsPrivateCategoriesEnabled(null);
        setTestAuthorViewQuestionsFromSuperadminsPrivateCategoriesEnabled(null);
        
        if (checkQuestionsFilterPermission(operation,filterCategory))
        {
        	// Reload questions from DB
        	questions=null;
        }
        else
        {
        	addErrorMessage("NON_AUTHORIZED_ACTION_ERROR");
        	setGlobalOtherUserCategoryAllowed(null);
        	setViewTestsFromOtherUsersPrivateCategoriesEnabled(null);
        	setViewTestsFromAdminsPrivateCategoriesEnabled(null);
        	setViewTestsFromSuperadminsPrivateCategoriesEnabled(null);
        	
        	// Reload tests categories from DB
        	testsCategories=null;
        	
        	// Reload questions categories from DB
        	specialCategoriesFilters=null;
        	questionsCategories=null;
        	
        	if (!getQuestionsCategories(operation).contains(filterCategory))
        	{
        		// Reload questions from DB
        		questions=null;
        	}
        }
    	questionsDualList.setSource(getQuestions(operation));
    	questionsDualList.getTarget().clear();
	}
	
	/**
	 * @return Filtered questions as dual list
	 */
	public DualListModel<Question> getFilteredQuestionsDualList()
	{
		return getFilteredQuestionsDualList(null);
	}
	
	public void setFilteredQuestionsDualList(DualListModel<Question> questionsDualList)
	{
		this.questionsDualList=questionsDualList;
	}
	
	/**
	 * @param operation Operation
	 * @return Filtered questions as dual list
	 */
	private DualListModel<Question> getFilteredQuestionsDualList(Operation operation)
	{
		if (questionsDualList==null)
		{
			questionsDualList=
				new DualListModel<Question>(getQuestions(getCurrentUserOperation(operation)),new ArrayList<Question>());
		}
		return questionsDualList;
	}
	
	/**
	 * @return Users as dual list
	 */
	public DualListModel<User> getUsersDualList()
	{
		return getUsersDualList(null);
	}
	
	public void setUsersDualList(DualListModel<User> usersDualList)
	{
		this.usersDualList=usersDualList;
	}
	
	/**
	 * @param operation Operation
	 * @return Users as dual list
	 */
	public DualListModel<User> getUsersDualList(Operation operation)
	{
		if (usersDualList==null)
		{
			usersDualList=
				new DualListModel<User>(getAvailableUsers(getCurrentUserOperation(operation)),new ArrayList<User>());
		}
		return usersDualList;
	}
	
	/**
	 * Change users to display on picklist of the "Add user" dialog based on filter. 
	 * @param event Action event
	 */
	public void applyUsersFilter(ActionEvent event)
	{
		// Reload filtered users for "Add users" dialog and its dual list
		setFilteredUsersForAddingUsers(null);
		setUsersDualList(null);
	}
	
	/**
	 * @return Admins as dual list
	 */
	public DualListModel<User> getAdminsDualList()
	{
		return getAdminsDualList(null);
	}
	
	public void setAdminsDualList(DualListModel<User> adminsDualList)
	{
		this.adminsDualList=adminsDualList;
	}
	
	/**
	 * @param operation Operation
	 * @return Admins as dual list
	 */
	private DualListModel<User> getAdminsDualList(Operation operation)
	{
		if (adminsDualList==null)
		{
			adminsDualList=
				new DualListModel<User>(getAvailableAdmins(getCurrentUserOperation(operation)),new ArrayList<User>());
		}
		return adminsDualList;
	}
	
	/**
	 * Change users to display on picklist of the "Add admin" dialog based on filter. 
	 * @param event Action event
	 */
	public void applyAdminsFilter(ActionEvent event)
	{
		// Reload filtered users for "Add admins" dialog and its dual list
		setFilteredUsersForAddingAdmins(null);
		setAdminsDualList(null);
	}
	
	/**
	 * @return Users for the filter of the "Add/Edit tech support address" dialog as dual list
	 */
	public DualListModel<User> getSupportContactFilterUsersDualList()
	{
		return getSupportContactFilterUsersDualList(null);
	}
	
	public void setSupportContactFilterUsersDualList(DualListModel<User> supportContactFilterUsersDualList)
	{
		this.supportContactFilterUsersDualList=supportContactFilterUsersDualList;
	}
	
	/**
	 * @param operation Operation
	 * @return Users for the filter of the "Add/Edit tech support address" dialog as dual list
	 */
	private DualListModel<User> getSupportContactFilterUsersDualList(Operation operation)
	{
		if (supportContactFilterUsersDualList==null)
		{
			supportContactFilterUsersDualList=new DualListModel<User>(
				getAvailableSupportContactFilterUsers(getCurrentUserOperation(operation)),new ArrayList<User>());
		}
		return supportContactFilterUsersDualList;
	}
	
	/**
	 * Change users to display on picklist of the "Add/Edit tech support address" dialog based on filter. 
	 * @param event Action event
	 */
	public void applySupportContactFilterUsersFilter(ActionEvent event)
	{
		// Refresh dual list of filtered users for "Add/Edit tech support address" dialog
		refreshEvaluatorFilterUsersDualList(getCurrentUserOperation(null),event.getComponent());
		
		// Reload filtered users for "Add/Edit tech support address" dialog
		setFilteredUsersForAddingSupportContactFilterUsers(null);
	}
	
	private void refreshSupportContactFilterUsersDualList(Operation operation,UIComponent component)
	{
		// Process hidden field with users ids
		if (processSupportContactFilterUsersIdsHidden(component))
		{
			// Fill the list of users included within the current user filter of the 
			// "Add/Edit tech support address" dialog with the information from hidden field
			List<User> testSupportContactFilterUsers=getTestSupportContactFilterUsers();
			testSupportContactFilterUsers.clear();
			if (getSupportContactFilterUsersIdsHidden()!=null && !"".equals(getSupportContactFilterUsersIdsHidden()))
			{
				// Get current user session Hibernate operation
				operation=getCurrentUserOperation(operation);
				
				for (String userId:getSupportContactFilterUsersIdsHidden().split(","))
				{
					testSupportContactFilterUsers.add(usersService.getUser(operation,Long.parseLong(userId)));
				}
			}
			
			// Reload dual list of filtered users for "Add/Edit assessement address" dialog
			setSupportContactFilterUsersDualList(null);
		}
	}
	
	private boolean processSupportContactFilterUsersIdsHidden(UIComponent component)
	{
		boolean submittedValue=false;
		FacesContext context=FacesContext.getCurrentInstance();
		UIInput supportContactFilterUsersIdsHiddenInput=(UIInput)component.findComponent(
			":techSupportAddressDialogForm:supportContactFilterUsersIdsHidden");
		supportContactFilterUsersIdsHiddenInput.processDecodes(context);
		if (supportContactFilterUsersIdsHiddenInput.getSubmittedValue()!=null)
		{
			setSupportContactFilterUsersIdsHidden(
				(String)supportContactFilterUsersIdsHiddenInput.getSubmittedValue());
			submittedValue=true;
		}
		return submittedValue;
	}
	
	/**
	 * @return Users for the filter of the "Add/Edit assessement address" dialog as dual list
	 */
	public DualListModel<User> getEvaluatorFilterUsersDualList()
	{
		return getEvaluatorFilterUsersDualList(null);
	}
	
	public void setEvaluatorFilterUsersDualList(DualListModel<User> evaluatorFilterUsersDualList)
	{
		this.evaluatorFilterUsersDualList=evaluatorFilterUsersDualList;
	}
	
	/**
	 * @param operation Operation
	 * @return Users for the filter of the "Add/Edit assessement address" dialog as dual list
	 */
	private DualListModel<User> getEvaluatorFilterUsersDualList(Operation operation)
	{
		if (evaluatorFilterUsersDualList==null)
		{
			evaluatorFilterUsersDualList=new DualListModel<User>(
				getAvailableEvaluatorFilterUsers(getCurrentUserOperation(operation)),getTestEvaluatorFilterUsers());
		}
		return evaluatorFilterUsersDualList;
	}
	
	/**
	 * Change users to display on picklist of the "Add/Edit assessement address" dialog based on filter. 
	 * @param event Action event
	 */
	public void applyEvaluatorFilterUsersFilter(ActionEvent event)
	{
		// Refresh dual list of filtered users for "Add/Edit assessement address" dialog
		refreshEvaluatorFilterUsersDualList(getCurrentUserOperation(null),event.getComponent());
		
		// Reload filtered users for "Add/Edit assessement address" dialog
		setFilteredUsersForAddingEvaluatorFilterUsers(null);
	}
	
	private void refreshEvaluatorFilterUsersDualList(Operation operation,UIComponent component)
	{
		// Process hidden field with users ids
		if (processEvaluatorFilterUsersIdsHidden(component))
		{
			// Fill the list of users included within the current user filter of the "Add/Edit assessement address" 
			// dialog with the information from hidden field
			List<User> testEvaluatorFilterUsers=getTestEvaluatorFilterUsers();
			testEvaluatorFilterUsers.clear();
			if (getEvaluatorFilterUsersIdsHidden()!=null && !"".equals(getEvaluatorFilterUsersIdsHidden()))
			{
				// Get current user session Hibernate operation 
				operation=getCurrentUserOperation(operation);
				
				for (String userId:getEvaluatorFilterUsersIdsHidden().split(","))
				{
					testEvaluatorFilterUsers.add(usersService.getUser(operation,Long.parseLong(userId)));
				}
			}
			
			// Reload dual list of filtered users for "Add/Edit assessement address" dialog
			setEvaluatorFilterUsersDualList(null);
		}
	}
	
	private boolean processEvaluatorFilterUsersIdsHidden(UIComponent component)
	{
		boolean submittedValue=false;
		FacesContext context=FacesContext.getCurrentInstance();
		UIInput evaluatorFilterUsersIdsHiddenInput=
			(UIInput)component.findComponent(":assessementAddressDialogForm:evaluatorFilterUsersIdsHidden");
		evaluatorFilterUsersIdsHiddenInput.processDecodes(context);
		if (evaluatorFilterUsersIdsHiddenInput.getSubmittedValue()!=null)
		{
			setEvaluatorFilterUsersIdsHidden((String)evaluatorFilterUsersIdsHiddenInput.getSubmittedValue());
			submittedValue=true;
		}
		return submittedValue;
	}
	
	public String getSupportContactFilterType()
	{
		return supportContactFilterType;
	}
	
	public void setSupportContactFilterType(String supportContactFilterType)
	{
		this.supportContactFilterType=supportContactFilterType;
	}
	
	public String getSupportContactFilterSubtype()
	{
		return getSupportContactFilterSubtype(null);
	}
	
	public void setSupportContactFilterSubtype(String supportContactFilterSubtype)
	{
		this.supportContactFilterSubtype=supportContactFilterSubtype;
	}
	
	private String getSupportContactFilterSubtype(Operation operation)
	{
		if (supportContactFilterSubtype==null)
		{
			List<String> supportContactFilterSubtypes=
				getSupportContactFilterSubtypes(getCurrentUserOperation(operation));
			if (!supportContactFilterSubtypes.isEmpty())
			{
				supportContactFilterSubtype=supportContactFilterSubtypes.get(0);
			}
		}
		return supportContactFilterSubtype;
	}
	
	public List<String> getSupportContactFilterSubtypes()
	{
		return getSupportContactFilterSubtypes(null);
	}
	
	public void setSupportContactFilterSubtypes(List<String> supportContactFilterSubtypes)
	{
		this.supportContactFilterSubtypes=supportContactFilterSubtypes;
	}
	
	private List<String> getSupportContactFilterSubtypes(Operation operation)
	{
		if (supportContactFilterSubtypes==null)
		{
			supportContactFilterSubtypes=new ArrayList<String>();
			for (AddressType addressType:
				addressTypesService.getAddressTypes(getCurrentUserOperation(operation),getSupportContactFilterType()))
			{
				String subtype=addressType.getSubtype();
				if (subtype!=null && !subtype.equals(""))
				{
					supportContactFilterSubtypes.add(addressType.getSubtype());
				}
			}
		}
		return supportContactFilterSubtypes;
	}
	
	public boolean isSupportContactFilterSubtypeEnabled()
	{
		return isSupportContactFilterSubtypeEnabled(null);
	}
	
	private boolean isSupportContactFilterSubtypeEnabled(Operation operation)
	{
		String filterSubtype=getSupportContactFilterSubtype(getCurrentUserOperation(operation));
		return filterSubtype!=null && !"".equals(filterSubtype);
	}
	
	/**
	 * Change filter of the "Add tech support address" dialog. 
	 * @param event Ajax event
	 */
	public void changeSupportContactFilterType(AjaxBehaviorEvent event)
	{
		// Refresh dual list of filtered users for "Add/Edit tech support address" dialog
		refreshSupportContactFilterUsersDualList(getCurrentUserOperation(null),event.getComponent());
		
		// Reload filter subtypes for the "Add tech support address" dialog
		setSupportContactFilterSubtypes(null);
		setSupportContactFilterSubtype(null);
	}
	
	/**
	 * Change filter subtype of the "Add tech support address" dialog. 
	 * @param event Ajax event
	 */
	public void changeSupportContactFilterSubtype(AjaxBehaviorEvent event)
	{
		// Refresh dual list of filtered users for "Add/Edit tech support address" dialog
		refreshSupportContactFilterUsersDualList(getCurrentUserOperation(null),event.getComponent());
	}
	
	public long getFilterSupportContactFilterUsersUserTypeId()
	{
		return filterSupportContactFilterUsersUserTypeId;
	}
	
	public void setFilterSupportContactFilterUsersUserTypeId(long filterSupportContactFilterUsersUserTypeId)
	{
		this.filterSupportContactFilterUsersUserTypeId=filterSupportContactFilterUsersUserTypeId;
	}
	
	public boolean isFilterSupportContactFilterUsersIncludeOmUsers()
	{
		return filterSupportContactFilterUsersIncludeOmUsers;
	}
	
	public void setFilterSupportContactFilterUsersIncludeOmUsers(
		boolean filterSupportContactFilterUsersIncludeOmUsers)
	{
		this.filterSupportContactFilterUsersIncludeOmUsers=filterSupportContactFilterUsersIncludeOmUsers;
	}
	
	/**
	 * @return Users included within the current user filter of the "Add/Edit tech support address" dialog
	 */
	public List<User> getTestSupportContactFilterUsers()
	{
		return testSupportContactFilterUsers;
	}
	
	/**
	 * @param testSupportContactFilterUsers Users included within the current user filter of the 
	 * "Add/Edit tech support address" dialog
	 */
	public void setTestSupportContactFilterUsers(List<User> testSupportContactFilterUsers)
	{
		this.testSupportContactFilterUsers=testSupportContactFilterUsers;
	}
	
	/**
	 * @return User's identifiers included within the current user filter of the "Add/Edit tech support address" 
	 * dialog as a string with the identifiers separated by commas
	 */
	public String getSupportContactFilterUsersIdsHidden()
	{
		return supportContactFilterUsersIdsHidden;
	}
	
	/**
	 * @param supportContactFilterUsersIdsHidden User's identifiers included within the current user filter 
	 * of the "Add/Edit tech support address" dialog as a string with the identifiers separated by commas
	 */
	public void setSupportContactFilterUsersIdsHidden(String supportContactFilterUsersIdsHidden)
	{
		this.supportContactFilterUsersIdsHidden=supportContactFilterUsersIdsHidden;
	}
	
	/**
	 * @return Users not included within the current user filter of the "Add/Edit tech support address" dialog
	 */
	public List<User> getAvailableSupportContactFilterUsers()
	{
		return getAvailableSupportContactFilterUsers(null);
	}
	
	/**
	 * @param operation Operation
	 * @return Users included within the current user filter of the "Add/Edit tech support address" dialog
	 */
	private List<User> getAvailableSupportContactFilterUsers(Operation operation)
	{
		List<User> availableSupportContactFilterUsers=new ArrayList<User>();
		List<User> testSupportContactFilterUsers=getTestSupportContactFilterUsers();
		for (User user:getFilteredUsersForAddingSupportContactFilterUsers(getCurrentUserOperation(operation)))
		{
			if (!testSupportContactFilterUsers.contains(user))
			{
				User availableSupportContactFilterUser=new User();
				availableSupportContactFilterUser.setFromOtherUser(user);
				availableSupportContactFilterUsers.add(availableSupportContactFilterUser);
			}
		}
		return availableSupportContactFilterUsers;
	}
	
	public String getFilterSupportContactRangeNameLowerLimit()
	{
		return filterSupportContactRangeNameLowerLimit;
	}
	
	public void setFilterSupportContactRangeNameLowerLimit(String filterSupportContactRangeNameLowerLimit)
	{
		this.filterSupportContactRangeNameLowerLimit=filterSupportContactRangeNameLowerLimit;
	}
	
	public String getFilterSupportContactRangeNameUpperLimit()
	{
		return filterSupportContactRangeNameUpperLimit;
	}
	
	public void setFilterSupportContactRangeNameUpperLimit(String filterSupportContactRangeNameUpperLimit)
	{
		this.filterSupportContactRangeNameUpperLimit=filterSupportContactRangeNameUpperLimit;
	}
	
	public String getFilterSupportContactRangeSurnameLowerLimit()
	{
		return filterSupportContactRangeSurnameLowerLimit;
	}
	
	public void setFilterSupportContactRangeSurnameLowerLimit(String filterSupportContactRangeSurnameLowerLimit)
	{
		this.filterSupportContactRangeSurnameLowerLimit=filterSupportContactRangeSurnameLowerLimit;
	}
	
	public String getFilterSupportContactRangeSurnameUpperLimit()
	{
		return filterSupportContactRangeSurnameUpperLimit;
	}
	
	public void setFilterSupportContactRangeSurnameUpperLimit(String filterSupportContactRangeSurnameUpperLimit)
	{
		this.filterSupportContactRangeSurnameUpperLimit=filterSupportContactRangeSurnameUpperLimit;
	}
	
	public String getEvaluatorFilterType()
	{
		return evaluatorFilterType;
	}
	
	public void setEvaluatorFilterType(String evaluatorFilterType)
	{
		this.evaluatorFilterType=evaluatorFilterType;
	}
	
	public String getEvaluatorFilterSubtype()
	{
		return getEvaluatorFilterSubtype(null);
	}
	
	public void setEvaluatorFilterSubtype(String evaluatorFilterSubtype)
	{
		this.evaluatorFilterSubtype=evaluatorFilterSubtype;
	}
	
	private String getEvaluatorFilterSubtype(Operation operation)
	{
		if (evaluatorFilterSubtype==null)
		{
			List<String> evaluatorFilterSubtypes=getEvaluatorFilterSubtypes(getCurrentUserOperation(operation));
			if (!evaluatorFilterSubtypes.isEmpty())
			{
				evaluatorFilterSubtype=evaluatorFilterSubtypes.get(0);
			}
		}
		return evaluatorFilterSubtype;
	}
	
	public List<String> getEvaluatorFilterSubtypes()
	{
		return getEvaluatorFilterSubtypes(null);
	}
	
	public void setEvaluatorFilterSubtypes(List<String> evaluatorFilterSubtypes)
	{
		this.evaluatorFilterSubtypes=evaluatorFilterSubtypes;
	}
	
	private List<String> getEvaluatorFilterSubtypes(Operation operation)
	{
		if (evaluatorFilterSubtypes==null)
		{
			evaluatorFilterSubtypes=new ArrayList<String>();
			for (AddressType addressType:
				addressTypesService.getAddressTypes(getCurrentUserOperation(operation),getEvaluatorFilterType()))
			{
				String subtype=addressType.getSubtype();
				if (subtype!=null && !subtype.equals(""))
				{
					evaluatorFilterSubtypes.add(addressType.getSubtype());
				}
			}
		}
		return evaluatorFilterSubtypes;
	}
	
	public boolean isEvaluatorFilterSubtypeEnabled()
	{
		return isEvaluatorFilterSubtypeEnabled(null);
	}
	
	private boolean isEvaluatorFilterSubtypeEnabled(Operation operation)
	{
		String filterSubtype=getEvaluatorFilterSubtype(getCurrentUserOperation(operation));
		return filterSubtype!=null && !"".equals(filterSubtype);
	}
	
	/**
	 * Change filter type of the "Add assessement address" dialog. 
	 * @param event Ajax event
	 */
	public void changeEvaluatorFilterType(AjaxBehaviorEvent event)
	{
		// Refresh dual list of filtered users for "Add/Edit assessement address" dialog
		refreshEvaluatorFilterUsersDualList(getCurrentUserOperation(null),event.getComponent());
		
		// Reload filter subtypes for the "Add assessement address" dialog
		setEvaluatorFilterSubtypes(null);
		setEvaluatorFilterSubtype(null);
	}
	
	/**
	 * Change filter subtype of the "Add assessement address" dialog. 
	 * @param event Ajax event
	 */
	public void changeEvaluatorFilterSubtype(AjaxBehaviorEvent event)
	{
		// Refresh dual list of filtered users for "Add/Edit assessement address" dialog
		refreshEvaluatorFilterUsersDualList(getCurrentUserOperation(null),event.getComponent());
	}
	
	public long getFilterEvaluatorFilterUsersUserTypeId()
	{
		return filterEvaluatorFilterUsersUserTypeId;
	}
	
	public void setFilterEvaluatorFilterUsersUserTypeId(long filterEvaluatorFilterUsersUserTypeId)
	{
		this.filterEvaluatorFilterUsersUserTypeId=filterEvaluatorFilterUsersUserTypeId;
	}
	
	public boolean isFilterEvaluatorFilterUsersIncludeOmUsers()
	{
		return filterEvaluatorFilterUsersIncludeOmUsers;
	}
	
	public void setFilterEvaluatorFilterUsersIncludeOmUsers(boolean filterEvaluatorFilterUsersIncludeOmUsers)
	{
		this.filterEvaluatorFilterUsersIncludeOmUsers=filterEvaluatorFilterUsersIncludeOmUsers;
	}
	
	/**
	 * @return Users included within the current user filter of the "Add/Edit assessement address" dialog
	 */
	public List<User> getTestEvaluatorFilterUsers()
	{
		return testEvaluatorFilterUsers;
	}
	
	/**
	 * @param testEvaluatorFilterUsers Users included within the current user filter of the 
	 * "Add/Edit assessement address" dialog
	 */
	public void setTestEvaluatorFilterUsers(List<User> testEvaluatorFilterUsers)
	{
		this.testEvaluatorFilterUsers=testEvaluatorFilterUsers;
	}
	
	/**
	 * @return User's identifiers included within the current user filter of the "Add/Edit assessement address" 
	 * dialog as a string with the identifiers separated by commas
	 */
	public String getEvaluatorFilterUsersIdsHidden()
	{
		return evaluatorFilterUsersIdsHidden;
	}
	
	/**
	 * @param evaluatorFilterUsersIdsHidden User's identifiers included within the current user filter 
	 * of the "Add/Edit assessement address" dialog as a string with the identifiers separated by commas
	 */
	public void setEvaluatorFilterUsersIdsHidden(String evaluatorFilterUsersIdsHidden)
	{
		this.evaluatorFilterUsersIdsHidden=evaluatorFilterUsersIdsHidden;
	}
	
	/**
	 * @return Users not included within the current user filter of the "Add/Edit assessement address" dialog
	 */
	public List<User> getAvailableEvaluatorFilterUsers()
	{
		return getAvailableEvaluatorFilterUsers(null);
	}
	
	/**
	 * @param operation Operation
	 * @return Users included within the current user filter of the "Add/Edit assessement address" dialog
	 */
	private List<User> getAvailableEvaluatorFilterUsers(Operation operation)
	{
		List<User> availableEvaluatorFilterUsers=new ArrayList<User>();
		List<User> testEvaluatorFilterUsers=getTestEvaluatorFilterUsers();
		for (User user:getFilteredUsersForAddingEvaluatorFilterUsers(getCurrentUserOperation(operation)))
		{
			if (!testEvaluatorFilterUsers.contains(user))
			{
				User availableEvaluatorFilterUser=new User();
				availableEvaluatorFilterUser.setFromOtherUser(user);
				availableEvaluatorFilterUsers.add(availableEvaluatorFilterUser);
			}
		}
		return availableEvaluatorFilterUsers;
	}
	
	public String getFilterEvaluatorRangeNameLowerLimit()
	{
		return filterEvaluatorRangeNameLowerLimit;
	}
	
	public void setFilterEvaluatorRangeNameLowerLimit(String filterEvaluatorRangeNameLowerLimit)
	{
		this.filterEvaluatorRangeNameLowerLimit=filterEvaluatorRangeNameLowerLimit;
	}
	
	public String getFilterEvaluatorRangeNameUpperLimit()
	{
		return filterEvaluatorRangeNameUpperLimit;
	}
	
	public void setFilterEvaluatorRangeNameUpperLimit(String filterEvaluatorRangeNameUpperLimit)
	{
		this.filterEvaluatorRangeNameUpperLimit=filterEvaluatorRangeNameUpperLimit;
	}
	
	public String getFilterEvaluatorRangeSurnameLowerLimit()
	{
		return filterEvaluatorRangeSurnameLowerLimit;
	}
	
	public void setFilterEvaluatorRangeSurnameLowerLimit(String filterEvaluatorRangeSurnameLowerLimit)
	{
		this.filterEvaluatorRangeSurnameLowerLimit=filterEvaluatorRangeSurnameLowerLimit;
	}
	
	public String getFilterEvaluatorRangeSurnameUpperLimit()
	{
		return filterEvaluatorRangeSurnameUpperLimit;
	}
	
	public void setFilterEvaluatorRangeSurnameUpperLimit(String filterEvaluatorRangeSurnameUpperLimit)
	{
		this.filterEvaluatorRangeSurnameUpperLimit=filterEvaluatorRangeSurnameUpperLimit;
	}
	
	public String getSupportContact()
	{
		return supportContact;
	}
	
	public void setSupportContact(String supportContact)
	{
		this.supportContact=supportContact;
	}
	
	public boolean isSupportContactDialogDisplayed()
	{
		return supportContactDialogDisplayed;
	}
	
	public void setSupportContactDialogDisplayed(boolean supportContactDialogDisplayed)
	{
		this.supportContactDialogDisplayed=supportContactDialogDisplayed;
	}
	
	public String getEvaluator()
	{
		return evaluator;
	}
	
	public void setEvaluator(String evaluator)
	{
		this.evaluator=evaluator;
	}
	
	public boolean isEvaluatorDialogDisplayed()
	{
		return evaluatorDialogDisplayed;
	}
	
	public void setEvaluatorDialogDisplayed(boolean evaluatorDialogDisplayed)
	{
		this.evaluatorDialogDisplayed=evaluatorDialogDisplayed;
	}
	
	public String getCancelTestTarget()
	{
		return cancelTestTarget;
	}
	
	public void setCancelTestTarget(String cancelTestTarget)
	{
		this.cancelTestTarget=cancelTestTarget;
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
			
			globalOtherUserCategoryAllowed=Boolean.valueOf(permissionsService.isGranted(
				operation,getAuthor(operation),"PERMISSION_TEST_GLOBAL_OTHER_USER_CATEGORY_ALLOWED"));
		}
		return globalOtherUserCategoryAllowed;
	}
	
	public Boolean getTestAuthorAdmin()
	{
		return getTestAuthorAdmin(null);
	}
	
	public boolean isTestAuthorAdmin()
	{
		return getTestAuthorAdmin().booleanValue();
	}
	
	private Boolean getTestAuthorAdmin(Operation operation)
	{
		// Get current user session Hibernate operation
		operation=getCurrentUserOperation(operation);
		
		return isAdmin(operation,getAuthor(operation));
	}
	
	private void resetTestAuthorAdmin(Operation operation)
	{
		admins.remove(Long.valueOf(getAuthor(getCurrentUserOperation(operation)).getId()));
	}
	
	public Boolean getTestAuthorSuperadmin()
	{
		return getTestAuthorSuperadmin(null);
	}
	
	public boolean isTestAuthorSuperadmin()
	{
		return getTestAuthorSuperadmin().booleanValue();
	}
	
	private Boolean getTestAuthorSuperadmin(Operation operation)
	{
		// Get current user session Hibernate operation
		operation=getCurrentUserOperation(operation);
		
		return isSuperadmin(operation,getAuthor(operation));
	}
	
	private void resetTestAuthorSuperadmin(Operation operation)
	{
		superadmins.remove(Long.valueOf(getAuthor(getCurrentUserOperation(operation)).getId()));
	}
	
	public Boolean getViewTestsFromOtherUsersPrivateCategoriesEnabled()
	{
		return getViewTestsFromOtherUsersPrivateCategoriesEnabled(null);
	}
	
	public void setViewTestsFromOtherUsersPrivateCategoriesEnabled(
		Boolean viewTestsFromOtherUsersPrivateCategoriesEnabled)
	{
		this.viewTestsFromOtherUsersPrivateCategoriesEnabled=viewTestsFromOtherUsersPrivateCategoriesEnabled;
	}
	
	public boolean isViewTestsFromOtherUsersPrivateCategoriesEnabled()
	{
		return getViewTestsFromOtherUsersPrivateCategoriesEnabled().booleanValue();
	}
	
	private Boolean getViewTestsFromOtherUsersPrivateCategoriesEnabled(Operation operation)
	{
		if (viewTestsFromOtherUsersPrivateCategoriesEnabled==null)
		{
			viewTestsFromOtherUsersPrivateCategoriesEnabled=
				Boolean.valueOf(userSessionService.isGranted(getCurrentUserOperation(operation),
				"PERMISSION_TESTS_VIEW_TESTS_OF_OTHER_USERS_PRIVATE_CATEGORIES_ENABLED"));
		}
		return viewTestsFromOtherUsersPrivateCategoriesEnabled;
	}
	
	public Boolean getViewTestsFromAdminsPrivateCategoriesEnabled()
	{
		return getViewTestsFromAdminsPrivateCategoriesEnabled(null);
	}
	
	public void setViewTestsFromAdminsPrivateCategoriesEnabled(
		Boolean viewTestsFromAdminsPrivateCategoriesEnabled)
	{
		this.viewTestsFromAdminsPrivateCategoriesEnabled=viewTestsFromAdminsPrivateCategoriesEnabled;
	}
	
	public boolean isViewTestsFromAdminsPrivateCategoriesEnabled()
	{
		return getViewTestsFromAdminsPrivateCategoriesEnabled().booleanValue();
	}
	
	private Boolean getViewTestsFromAdminsPrivateCategoriesEnabled(Operation operation)
	{
		if (viewTestsFromAdminsPrivateCategoriesEnabled==null)
		{
			viewTestsFromAdminsPrivateCategoriesEnabled=Boolean.valueOf(userSessionService.isGranted(
				getCurrentUserOperation(operation),"PERMISSION_TESTS_VIEW_TESTS_OF_ADMINS_PRIVATE_CATEGORIES_ENABLED"));
		}
		return viewTestsFromAdminsPrivateCategoriesEnabled;
	}
	
	public Boolean getViewTestsFromSuperadminsPrivateCategoriesEnabled()
	{
		return getViewTestsFromSuperadminsPrivateCategoriesEnabled(null);
	}
	
	public void setViewTestsFromSuperadminsPrivateCategoriesEnabled(
		Boolean viewTestsFromSuperadminsPrivateCategoriesEnabled)
	{
		this.viewTestsFromSuperadminsPrivateCategoriesEnabled=
			viewTestsFromSuperadminsPrivateCategoriesEnabled;
	}
	
	public boolean isViewTestsFromSuperadminsPrivateCategoriesEnabled()
	{
		return getViewTestsFromSuperadminsPrivateCategoriesEnabled().booleanValue();
	}
	
	private Boolean getViewTestsFromSuperadminsPrivateCategoriesEnabled(Operation operation)
	{
		if (viewTestsFromSuperadminsPrivateCategoriesEnabled==null)
		{
			viewTestsFromSuperadminsPrivateCategoriesEnabled=
				Boolean.valueOf(userSessionService.isGranted(getCurrentUserOperation(operation),
				"PERMISSION_TESTS_VIEW_TESTS_OF_SUPERADMINS_PRIVATE_CATEGORIES_ENABLED"));
		}
		return viewTestsFromSuperadminsPrivateCategoriesEnabled;
	}
	
	public Boolean getUseGlobalQuestions()
	{
		return getUseGlobalQuestions(null);
	}
	
	public void setUseGlobalQuestions(Boolean useGlobalQuestions)
	{
		this.useGlobalQuestions=useGlobalQuestions;
	}
	
	public boolean isUseGlobalQuestions()
	{
		return getUseGlobalQuestions().booleanValue();
	}
	
	private Boolean getUseGlobalQuestions(Operation operation)
	{
		if (useGlobalQuestions==null)
		{
			// Get current user session Hibernate operation
			operation=getCurrentUserOperation(operation);
			
			useGlobalQuestions=Boolean.valueOf(
				permissionsService.isGranted(operation,getAuthor(operation),"PERMISSION_TEST_USE_GLOBAL_QUESTIONS"));
		}
		return useGlobalQuestions;
	}
	
	public Boolean getUseOtherUsersQuestions()
	{
		return getUseOtherUsersQuestions(null);
	}
	
	public void setUseOtherUsersQuestions(Boolean useOtherUsersQuestions)
	{
		this.useOtherUsersQuestions=useOtherUsersQuestions;
	}
	
	public boolean isUseOtherUsersQuestions()
	{
		return getUseOtherUsersQuestions().booleanValue();
	}
	
	private Boolean getUseOtherUsersQuestions(Operation operation)
	{
		if (useOtherUsersQuestions==null)
		{
			// Get current user session Hibernate operation
			operation=getCurrentUserOperation(operation);
			
			useOtherUsersQuestions=Boolean.valueOf(permissionsService.isGranted(
				operation,getAuthor(operation),"PERMISSION_TEST_USE_OTHER_USERS_QUESTIONS"));
		}
		return useOtherUsersQuestions;
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
	
	public Boolean getTestAuthorViewQuestionsFromOtherUsersPrivateCategoriesEnabled()
	{
		return getTestAuthorViewQuestionsFromOtherUsersPrivateCategoriesEnabled(null);
	}
	
	public void setTestAuthorViewQuestionsFromOtherUsersPrivateCategoriesEnabled(
		Boolean testAuthorViewQuestionsFromOtherUsersPrivateCategoriesEnabled)
	{
		this.testAuthorViewQuestionsFromOtherUsersPrivateCategoriesEnabled=
			testAuthorViewQuestionsFromOtherUsersPrivateCategoriesEnabled;
	}
	
	public boolean isTestAuthorViewQuestionsFromOtherUsersPrivateCategoriesEnabled()
	{
		return getTestAuthorViewQuestionsFromOtherUsersPrivateCategoriesEnabled().booleanValue();
	}
	
	private Boolean getTestAuthorViewQuestionsFromOtherUsersPrivateCategoriesEnabled(Operation operation)
	{
		if (testAuthorViewQuestionsFromOtherUsersPrivateCategoriesEnabled==null)
		{
			// Get current user session Hibernate operation
			operation=getCurrentUserOperation(operation);
			
			User testAuthor=getAuthor(operation);
			if (testAuthor.getId()==userSessionService.getCurrentUserId())
			{
				testAuthorViewQuestionsFromOtherUsersPrivateCategoriesEnabled=
					getViewQuestionsFromOtherUsersPrivateCategoriesEnabled(operation);
			}
			else
			{
				testAuthorViewQuestionsFromOtherUsersPrivateCategoriesEnabled=
					Boolean.valueOf(permissionsService.isGranted(operation,testAuthor,
					"PERMISSION_QUESTIONS_VIEW_QUESTIONS_OF_OTHER_USERS_PRIVATE_CATEGORIES_ENABLED"));
			}
		}
		return testAuthorViewQuestionsFromOtherUsersPrivateCategoriesEnabled;
	}
	
	public Boolean getTestAuthorViewQuestionsFromAdminsPrivateCategoriesEnabled()
	{
		return getTestAuthorViewQuestionsFromAdminsPrivateCategoriesEnabled(null);
	}
	
	public void setTestAuthorViewQuestionsFromAdminsPrivateCategoriesEnabled(
			Boolean testAuthorViewQuestionsFromAdminsPrivateCategoriesEnabled)
	{
		this.testAuthorViewQuestionsFromAdminsPrivateCategoriesEnabled=
			testAuthorViewQuestionsFromAdminsPrivateCategoriesEnabled;
	}
	
	public boolean isTestAuthorViewQuestionsFromAdminsPrivateCategoriesEnabled()
	{
		return getTestAuthorViewQuestionsFromAdminsPrivateCategoriesEnabled().booleanValue();
	}
	
	private Boolean getTestAuthorViewQuestionsFromAdminsPrivateCategoriesEnabled(Operation operation)
	{
		if (testAuthorViewQuestionsFromAdminsPrivateCategoriesEnabled==null)
		{
			// Get current user session Hibernate operation
			operation=getCurrentUserOperation(operation);
				
			User testAuthor=getAuthor(operation);
			if (testAuthor.getId()==userSessionService.getCurrentUserId())
			{
				testAuthorViewQuestionsFromAdminsPrivateCategoriesEnabled=
					getViewQuestionsFromAdminsPrivateCategoriesEnabled(operation);
			}
			else
			{
				testAuthorViewQuestionsFromAdminsPrivateCategoriesEnabled=
					Boolean.valueOf(permissionsService.isGranted(operation,testAuthor,
					"PERMISSION_QUESTIONS_VIEW_QUESTIONS_OF_ADMINS_PRIVATE_CATEGORIES_ENABLED"));
			}
		}
		return testAuthorViewQuestionsFromAdminsPrivateCategoriesEnabled;
	}
	
	public Boolean getTestAuthorViewQuestionsFromSuperadminsPrivateCategoriesEnabled()
	{
		return getTestAuthorViewQuestionsFromSuperadminsPrivateCategoriesEnabled(null);
	}
	
	public void setTestAuthorViewQuestionsFromSuperadminsPrivateCategoriesEnabled(
		Boolean testAuthorViewQuestionsFromSuperadminsPrivateCategoriesEnabled)
	{
		this.testAuthorViewQuestionsFromSuperadminsPrivateCategoriesEnabled=
			testAuthorViewQuestionsFromSuperadminsPrivateCategoriesEnabled;
	}
	
	public boolean isTestAuthorViewQuestionsFromSuperadminsPrivateCategoriesEnabled()
	{
		return getTestAuthorViewQuestionsFromSuperadminsPrivateCategoriesEnabled().booleanValue();
	}
	
	private Boolean getTestAuthorViewQuestionsFromSuperadminsPrivateCategoriesEnabled(Operation operation)
	{
		if (testAuthorViewQuestionsFromSuperadminsPrivateCategoriesEnabled==null)
		{
			// Get current user session Hibernate operation
			operation=getCurrentUserOperation(operation);
				
			User testAuthor=getAuthor(operation);
			if (testAuthor.getId()==userSessionService.getCurrentUserId())
			{
				testAuthorViewQuestionsFromSuperadminsPrivateCategoriesEnabled=
					getViewQuestionsFromSuperadminsPrivateCategoriesEnabled(operation);
			}
			else
			{
				testAuthorViewQuestionsFromSuperadminsPrivateCategoriesEnabled=
					Boolean.valueOf(permissionsService.isGranted(operation,testAuthor,
					"PERMISSION_QUESTIONS_VIEW_QUESTIONS_OF_SUPERADMINS_PRIVATE_CATEGORIES_ENABLED"));
			}
		}
		return testAuthorViewQuestionsFromSuperadminsPrivateCategoriesEnabled;
	}
	
	public Boolean getViewOMEnabled()
	{
		return getViewOMEnabled(null);
	}
	
	public void setViewOMEnabled(Boolean viewOMEnabled)
	{
		this.viewOMEnabled=viewOMEnabled;
	}
	
	public boolean isViewOMEnabled()
	{
		return getViewOMEnabled().booleanValue();
	}
	
	private Boolean getViewOMEnabled(Operation operation)
	{
		if (viewOMEnabled==null)
		{
			viewOMEnabled=Boolean.valueOf(userSessionService.isGranted(
				getCurrentUserOperation(operation),"PERMISSION_QUESTIONS_VIEW_OM_ENABLED"));
		}
		return viewOMEnabled;
	}
	
	public void resetViewOMQuestionsEnabled()
	{
		viewOMQuestionsEnabled.clear();
	}
	
	public void resetViewOmQuestionEnabled(Question question)
	{
		if (question!=null)
		{
			viewOMQuestionsEnabled.remove(Long.valueOf(question.getId()));
		}
	}
	
	public boolean isViewOMQuestionEnabled(Question question)
	{
		boolean viewOMQuestionEnabled=false;
		if (question!=null)
		{
			viewOMQuestionEnabled=isViewOMQuestionEnabled(null,question.getId());
		}
		return viewOMQuestionEnabled; 
	}
	
	public boolean isViewOMQuestionEnabled(long questionId)
	{
		boolean viewOMQuestionEnabled=false;
		if (questionId>0L)
		{
			viewOMQuestionEnabled=isViewOMQuestionEnabled(null,questionId);
		}
		return viewOMQuestionEnabled; 
	}
	
	private boolean isViewOMQuestionEnabled(Operation operation,long questionId)
	{
		boolean viewOMQuestionEnabled=false;
		if (questionId>0L)
		{
			if (viewOMQuestionsEnabled.containsKey(Long.valueOf(questionId)))
			{
				viewOMQuestionEnabled=viewOMQuestionsEnabled.get(Long.valueOf(questionId));
			}
			else
			{
				// Get current user session Hibernate operation
				operation=getCurrentUserOperation(operation);
				
				Question question=questionsService.getQuestion(operation,questionId);
				viewOMQuestionEnabled=isViewOMQuestionEnabled(operation,question);
			}
		}
		return viewOMQuestionEnabled; 
	}
	
	private boolean isViewOMQuestionEnabled(Operation operation,Question question)
	{
		boolean viewOMQuestionEnabled=false;
		if (question!=null)
		{
			if (viewOMQuestionsEnabled.containsKey(Long.valueOf(question.getId())))
			{
				viewOMQuestionEnabled=viewOMQuestionsEnabled.get(Long.valueOf(question.getId()));
			}
			else
			{
				// Get current user session Hibernate operation
				operation=getCurrentUserOperation(operation);
					
				User questionAuthor=question.getCreatedBy();
				viewOMQuestionEnabled=getViewOMEnabled(operation) && 
					(questionAuthor.getId()==userSessionService.getCurrentUserId() || 
					(question.getCategory().getVisibility().isGlobal() ||
					question.getCategory().getVisibility().getLevel()<=visibilitiesService.getVisibility(
					operation,"CATEGORY_VISIBILITY_PUBLIC").getLevel()) ||
					(getViewQuestionsFromOtherUsersPrivateCategoriesEnabled(operation).booleanValue() &&
					(!isAdmin(operation,questionAuthor) || 
					getViewQuestionsFromAdminsPrivateCategoriesEnabled(operation).booleanValue()) &&
					(!isSuperadmin(operation,questionAuthor) || 
					getViewQuestionsFromSuperadminsPrivateCategoriesEnabled(operation).booleanValue())));
				
				viewOMQuestionsEnabled.put(Long.valueOf(question.getId()),Boolean.valueOf(viewOMQuestionEnabled));
			}
		}
		return viewOMQuestionEnabled;
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
	
	private void resetAdminFromQuestionAllowed(Question question)
	{
		if (question!=null && question.getCreatedBy()!=null)
		{
			admins.remove(Long.valueOf(question.getCreatedBy().getId()));
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
	
	private void resetSuperadminFromQuestionAllowed(Question question)
	{
		if (question!=null && question.getCreatedBy()!=null)
		{
			superadmins.remove(Long.valueOf(question.getCreatedBy().getId()));
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
				superadmin=permissionsService.isGranted(getCurrentUserOperation(operation),user,
					"PERMISSION_ADMINISTRATION_RAISE_PERMISSIONS_OVER_OWNED_ALLOWED");
				superadmins.put(userId,Boolean.valueOf(superadmin));
			}
		}
		return superadmin;
	}
	
	/**
	 * Check if setters for checkboxes are enabled or not.<br/><br/>
	 * Note that this is needed because of a bug of &lt;p:wizard&gt; component that always set properties for 
	 * their checkboxes components as <i>false</i> when submitting the form.
	 * @return true if setters for checkboxes are enabled, false otherwise
	 */
	public boolean isEnabledChecboxesSetters()
	{
		return enabledCheckboxesSetters;
	}
	
	/**
	 * Enable or disable setters for checkboxes.<br/><br/>
	 * Note that this is needed because of a bug of &lt;p:wizard&gt; component that always set properties for 
	 * their checkboxes components as <i>false</i> when submitting the form.
	 * @param enabledCheckboxesSetters true to enable setters for checkboxes, false to disable setters 
	 * for checkboxes
	 */
	public void setEnabledCheckboxesSetters(boolean enabledCheckboxesSetters)
	{
		this.enabledCheckboxesSetters=enabledCheckboxesSetters;
	}
	
	public List<SectionBean> getSectionsSorting()
	{
		return getSectionsSorting(null);
	}
	
	public void setSectionsSorting(List<SectionBean> sectionsSorting)
	{
		this.sectionsSorting=sectionsSorting;
	}
	
	private List<SectionBean> getSectionsSorting(Operation operation)
	{
		if (sectionsSorting==null)
		{
			sectionsSorting=new ArrayList<SectionBean>();
			for (SectionBean section:getSections(getCurrentUserOperation(operation)))
			{
				sectionsSorting.add(section);
			}
			// I think that sorting this collection it is not needed because sections are always displayed sorted
			/*
			Collections.sort(sectionsSorting,new Comparator<SectionBean>()
			{
				@Override
				public int compare(SectionBean sb1,SectionBean sb2)
				{
					return sb1.getOrder()==sb2.getOrder()?0:sb1.getOrder()>sb2.getOrder()?1:-1;
				}
			});
			*/
		}
		return sectionsSorting;
	}
	
    public List<TestFeedbackBean> getFeedbacksSorting()
    {
		return getFeedbacksSorting(null);
	}
    
	public void setFeedbacksSorting(List<TestFeedbackBean> feedbacksSorting)
	{
		this.feedbacksSorting=feedbacksSorting;
	}
	
    private List<TestFeedbackBean> getFeedbacksSorting(Operation operation)
    {
    	if (feedbacksSorting==null)
    	{
    		feedbacksSorting=new ArrayList<TestFeedbackBean>();
    		for (TestFeedbackBean feedback:getFeedbacks(getCurrentUserOperation(operation)))
    		{
    			feedbacksSorting.add(feedback);
    		}
    		Collections.sort(feedbacksSorting, new Comparator<TestFeedbackBean>()
    		{
				@Override
				public int compare(TestFeedbackBean tfb1,TestFeedbackBean tfb2)
				{
					return tfb1.getPosition()==tfb2.getPosition()?0:tfb1.getPosition()>tfb2.getPosition()?1:-1;
				}
    		});
    	}
		return feedbacksSorting;
	}
	
    /**
     * @return Number comparators
     */
    public List<String> getNumberComparators()
    {
    	List<String> numberComparators=null;
    	String scoreGen=localizationService.getLocalizedMessage("SCORE_GEN");
    	if ("M".equals(scoreGen))
    	{
    		numberComparators=NumberComparator.COMPARATORS;
    	}
    	else
    	{
    		numberComparators=NumberComparator.COMPARATORS_F;
    	}
    	return numberComparators;
    }
    
    /**
     * @return Units that can be used within conditions of feedbacks
     */
    public List<String> getConditionalUnits()
    {
    	return TestFeedbackConditionBean.UNITS;
    }
    
    /**
     * @param unit Score unit string
     * @return Score unit
     */
    public ScoreUnit getScoreUnit(String unit)
    {
    	return getScoreUnit(null,unit);
    }
    
    /**
     * @param operation Operation
     * @param unit Score unit string
     * @return Score unit
     */
    public ScoreUnit getScoreUnit(Operation operation,String unit)
    {
    	return scoreUnitsService.getScoreUnit(getCurrentUserOperation(operation),unit);
    }
    
    public SectionBean getCurrentFeedbackConditionSection()
    {
    	SectionBean section=null;
    	TestFeedbackBean currentFeedback=getCurrentFeedback();
    	if (currentFeedback!=null && currentFeedback.getCondition()!=null)
    	{
    		section=currentFeedback.getCondition().getSection();
    	}
    	return section;
    }
    
    public void setCurrentFeedbackConditionSection(SectionBean section)
    {
    	TestFeedbackBean currentFeedback=getCurrentFeedback();
    	if (currentFeedback!=null && currentFeedback.getCondition()!=null)
    	{
    		currentFeedback.getCondition().setSection(section);
    	}
    }
    
    public String getCurrentFeedbackConditionComparator()
    {
    	String comparator=null;
    	TestFeedbackBean currentFeedback=getCurrentFeedback();
    	if (currentFeedback!=null && currentFeedback.getCondition()!=null)
    	{
    		comparator=currentFeedback.getCondition().getComparator();
    	}
    	return comparator;
    }
    
    public void setCurrentFeedbackConditionComparator(String comparator)
    {
    	TestFeedbackBean currentFeedback=getCurrentFeedback();
    	if (currentFeedback!=null && currentFeedback.getCondition()!=null)
    	{
    		currentFeedback.getCondition().setComparator(comparator);
    	}
    }
    
    public String getCurrentFeedbackConditionUnit()
    {
    	String unit=null;
    	TestFeedbackBean currentFeedback=getCurrentFeedback();
    	if (currentFeedback!=null && currentFeedback.getCondition()!=null)
    	{
    		unit=currentFeedback.getCondition().getUnit();
    	}
    	return unit;
    }
    
    public void setCurrentFeedbackConditionUnit(String unit)
    {
    	TestFeedbackBean currentFeedback=getCurrentFeedback();
    	if (currentFeedback!=null && currentFeedback.getCondition()!=null)
    	{
    		currentFeedback.getCondition().setUnit(unit);
    	}
    }
    
    /**
     * @param qt1 Type of first question
     * @param qt2 Type of second question
     * @return 1 if type of first question greater than type of second question,
     * -1 if type of first question less than type of second question,
     * 0 if both questions have same type.
     */
    public int sortQuestionTypes(Object qt1,Object qt2)
    {
    	List<QuestionType> questionTypes=questionTypesService.getQuestionTypes();
    	QuestionType questionType1=questionTypesService.getQuestionType((String)qt1);
    	QuestionType questionType2=questionTypesService.getQuestionType((String)qt2);
    	int iQT1=questionTypes.indexOf(questionType1);
    	int iQT2=questionTypes.indexOf(questionType2);
    	return iQT1>iQT2?1:iQT1<iQT2?-1:0;
    }
	
	/**
	 * Refresh accordion of the common data tab of the test to display current active tab.<br/><br/>
	 * Useful to avoid undesired tab changes after updating an accordion.
	 * @param operation Operation
	 * @param component Component that triggered the listener that called this method
	 */
	private void refreshActiveGeneralTab(Operation operation,UIComponent component)
	{
		String generalAccordionId=null;
		if (getTestId(getCurrentUserOperation(operation))>0L)
		{
			generalAccordionId=":testForm:testFormTabs:generalAccordion";
		}
		else
		{
			generalAccordionId=":testForm:generalAccordion";
		}
		AccordionPanel generalAccordion=(AccordionPanel)component.findComponent(generalAccordionId);
		if (generalAccordion!=null)
		{
			if (activeSectionIndex>=0)
			{
				generalAccordion.setActiveIndex(Integer.toString(activeGeneralTabIndex));
			}
			else
			{
				generalAccordion.setActiveIndex(null);
			}
		}
	}
    
	/**
	 * Refresh sections accordion to display current active tab.<br/><br/>
	 * Useful to avoid undesired tab changes after updating an accordion.
	 * @param operation Operation
	 * @param component Component that triggered the listener that called this method
	 */
	private void refreshActiveSection(Operation operation,UIComponent component)
	{
		String sectionsAccordionId=null;
		if (getTestId(getCurrentUserOperation(operation))>0L)
		{
			sectionsAccordionId=":testForm:testFormTabs:sectionsAccordion";
		}
		else
		{
			sectionsAccordionId=":testForm:sectionsAccordion";
		}
		AccordionPanel sectionsAccordion=(AccordionPanel)component.findComponent(sectionsAccordionId);
		if (sectionsAccordion!=null)
		{
			if (activeSectionIndex>=0)
			{
				sectionsAccordion.setActiveIndex(Integer.toString(activeSectionIndex));
			}
			else
			{
				sectionsAccordion.setActiveIndex(null);
			}
		}
	}
    
	/**
	 * Process some input fields (name, category, title, description) of the common data tab of a test.<br/><br/>
	 * @param operation Operation
	 * @param component Component that triggered the listener that called this method
	 */
	private void processCommonDataInputFields(Operation operation,UIComponent component)
	{
		// Get current user session Hibernate operation
		operation=getCurrentUserOperation(operation);
		
		FacesContext context=FacesContext.getCurrentInstance();
		String nameInputId=null;
		String categoryInputId=null;
		String titleInputId=null;
		String descriptionInputId=null;
		String assessementInputId=null;
		String scoreTypeInputId=null;
		if (getTestId(operation)==0L)
		{
			nameInputId=":testForm:nameInput";
			categoryInputId=":testForm:categoryInput";
			titleInputId=":testForm:titleInput";
			descriptionInputId=":testForm:descriptionInput";
			assessementInputId=":testForm:assessementInput";
			scoreTypeInputId=":testForm:scoreTypeInput";
		}
		else
		{
			nameInputId=":testForm:testFormTabs:nameInput";
			categoryInputId=":testForm:testFormTabs:categoryInput";
			titleInputId=":testForm:testFormTabs:titleInput";
			descriptionInputId=":testForm:testFormTabs:descriptionInput";
			assessementInputId=":testForm:testFormTabs:assessementInput";
			scoreTypeInputId=":testForm:testFormTabs:scoreTypeInput";
		}
		UIInput nameInput=(UIInput)component.findComponent(nameInputId);
		nameInput.processDecodes(context);
		if (nameInput.getSubmittedValue()!=null)
		{
			setName((String)nameInput.getSubmittedValue());
		}
		UIInput categoryInput=(UIInput)component.findComponent(categoryInputId);
		categoryInput.processDecodes(context);
		if (categoryInput.getSubmittedValue()!=null)
		{
			setCategoryId(operation,Long.parseLong((String)categoryInput.getSubmittedValue()));
		}
		UIInput titleInput=(UIInput)component.findComponent(titleInputId);
		titleInput.processDecodes(context);
		if (titleInput.getSubmittedValue()!=null)
		{
			setTitle((String)titleInput.getSubmittedValue());
		}
		UIInput descriptionInput=(UIInput)component.findComponent(descriptionInputId);
		descriptionInput.processDecodes(context);
		if (descriptionInput.getSubmittedValue()!=null)
		{
			setDescription((String)descriptionInput.getSubmittedValue());
		}
		UIInput assessementInput=(UIInput)component.findComponent(assessementInputId);
		assessementInput.processDecodes(context);
		if (assessementInput.getSubmittedValue()!=null)
		{
			setAssessementId(operation,Long.parseLong((String)assessementInput.getSubmittedValue()));
		}
		UIInput scoreTypeInput=(UIInput)component.findComponent(scoreTypeInputId);
		scoreTypeInput.processDecodes(context);
		if (scoreTypeInput.getSubmittedValue()!=null)
		{
			setScoreTypeId(operation,Long.parseLong((String)scoreTypeInput.getSubmittedValue()));
		}
	}
    
	/**
	 * Process some input fields (all users allowed, allow admin reports) of the users tab of the accordion within 
	 * the common data tab of a test.
	 * @param operation Operation
	 * @param component Component that triggered the listener that called this method
	 */
	private void processUsersTabCommonDataInputFields(Operation operation,UIComponent component)
	{
		FacesContext context=FacesContext.getCurrentInstance();
		String allUsersAllowedId=null;
		String allowAdminReportsId=null;
		if (getTestId(getCurrentUserOperation(operation))==0L)
		{
			allUsersAllowedId=":testForm:generalAccordion:allUsersAllowed";
			allowAdminReportsId=":testForm:generalAccordion:allowAdminReports";
		}
		else
		{
			allUsersAllowedId=":testForm:testFormTabs:generalAccordion:allUsersAllowed";
			allowAdminReportsId=":testForm:testFormTabs:generalAccordion:allowAdminReports";
		}
		UIInput allUsersAllowed=(UIInput)component.findComponent(allUsersAllowedId);
		allUsersAllowed.processDecodes(context);
		if (allUsersAllowed.getSubmittedValue()!=null)
		{
			setAllUsersAllowed(Boolean.valueOf((String)allUsersAllowed.getSubmittedValue()));
		}
		UIInput allowAdminReports=(UIInput)component.findComponent(allowAdminReportsId);
		allowAdminReports.processDecodes(context);
		if (allowAdminReports.getSubmittedValue()!=null)
		{
			setAllowAdminReports(Boolean.valueOf((String)allowAdminReports.getSubmittedValue()));
		}
	}
	
	/**
	 * Process some input fields (start date, close date, warning date, feedback date) of the calendar tab 
	 * of the accordion within the common data tab of a test.
	 * @param operation Operation
	 * @param component Component that triggered the listener that called this method
	 */
	private void processCalendarTabCommonDataInputFields(Operation operation,UIComponent component)
	{
		// Get current user session Hibernate operation
		operation=getCurrentUserOperation(operation);
		
		FacesContext context=FacesContext.getCurrentInstance();
		String startDateId=null;
		String closeDateId=null;
		String warningDateId=null;
		String feedbackDateId=null;
		if (getTestId(operation)==0L)
		{
			startDateId=":testForm:generalAccordion:startDate";
			closeDateId=":testForm:generalAccordion:closeDate";
			warningDateId=":testForm:generalAccordion:warningDate";
			feedbackDateId=":testForm:generalAccordion:feedbackDate";
		}
		else
		{
			startDateId=":testForm:testFormTabs:generalAccordion:startDate";
			closeDateId=":testForm:testFormTabs:generalAccordion:closeDate";
			warningDateId=":testForm:testFormTabs:generalAccordion:warningDate";
			feedbackDateId=":testForm:testFormTabs:generalAccordion:feedbackDate";
		}
		DateFormat df=null;
		if (isRestrictDates(operation))
		{
			UIInput startDate=(UIInput)component.findComponent(startDateId);
			startDate.processDecodes(context);
			if (startDate.getSubmittedValue()!=null)
			{
				if (df==null)
				{
					df=new SimpleDateFormat(localizationService.getLocalizedMessage("DATE_PATTERN"));
				}
				try
				{
					setStartDate(df.parse((String)startDate.getSubmittedValue()));
				}
				catch (ParseException pe)
				{
					setStartDate(null);
				}
			}
			UIInput closeDate=(UIInput)component.findComponent(closeDateId);
			closeDate.processDecodes(context);
			if (closeDate.getSubmittedValue()!=null)
			{
				if (df==null)
				{
					df=new SimpleDateFormat(localizationService.getLocalizedMessage("DATE_PATTERN"));
				}
				try
				{
					setCloseDate(df.parse((String)closeDate.getSubmittedValue()));
				}
				catch (ParseException pe)
				{
					setCloseDate(null);
				}
			}
			UIInput warningDate=(UIInput)component.findComponent(warningDateId);
			warningDate.processDecodes(context);
			if (warningDate.getSubmittedValue()!=null)
			{
				if (df==null)
				{
					df=new SimpleDateFormat(localizationService.getLocalizedMessage("DATE_PATTERN"));
				}
				try
				{
					setWarningDate(df.parse((String)warningDate.getSubmittedValue()));
				}
				catch (ParseException pe)
				{
					setWarningDate(null);
				}
			}
		}
		if (isRestrictFeedbackDate())
		{
			UIInput feedbackDate=(UIInput)component.findComponent(feedbackDateId);
			feedbackDate.processDecodes(context);
			if (feedbackDate.getSubmittedValue()!=null)
			{
				if (df==null)
				{
					df=new SimpleDateFormat(localizationService.getLocalizedMessage("DATE_PATTERN"));
				}
				try
				{
					setFeedbackDate(df.parse((String)feedbackDate.getSubmittedValue()));
				}
				catch (ParseException pe)
				{
					setFeedbackDate(null);
				}
			}
		}
	}
	
	/**
	 * Process presentation editor input field within the presentation tab of a test.
	 * @param component Component that triggered the listener that called this method
	 */
	private void processPresentationEditorField(UIComponent component)
	{
		FacesContext context=FacesContext.getCurrentInstance();
		UIInput presentationTextInput=
			(UIInput)component.findComponent(":testForm:testFormTabs:presentationText");
		presentationTextInput.processDecodes(context);
		if (presentationTextInput.getSubmittedValue()!=null)
		{
			setPresentation((String)presentationTextInput.getSubmittedValue());
		}
	}
	
	/**
	 * Process some input fields of the sections tab of a test.
	 * @param operation Operation
	 * @param component Component that triggered the listener that called this method
	 * @param section Section
	 */
	private void processSectionsInputFields(Operation operation,UIComponent component,SectionBean section)
	{
		processSectionsInputFields(getCurrentUserOperation(operation),component,section,new ArrayList<String>(0));
	}
	
	/**
	 * Process some input fields of the sections tab of a test.
	 * @param operation Operation
	 * @param component Component that triggered the listener that called this method
	 * @param section Section
	 * @param exceptions List of identifiers of input fields to be excluded from processing 
	 */
	private void processSectionsInputFields(Operation operation,UIComponent component,SectionBean section,
		List<String> exceptions)
	{
		// Reset checked property
		setPropertyChecked(null);
		
		FacesContext context=FacesContext.getCurrentInstance();
		if (section!=null)
		{
			AccordionPanel sectionsAccordion=getSectionsAccordion(getCurrentUserOperation(operation),component);
			if (sectionsAccordion!=null)
			{
				// Save current accordion row index and set it to point active tab
				int currentAccordionRowIndex=sectionsAccordion.getRowIndex();
				sectionsAccordion.setRowIndex(activeSectionIndex);
				
				UIComponent tab=sectionsAccordion.getChildren().get(0);
				UIComponent randomPanel=null;
				UIComponent questionsPanel=null;
				for (UIComponent tabChild:tab.getChildren())
				{
					if (tabChild.getId().equals("randomPanel"))
					{
						randomPanel=tabChild;
					}
					else if (tabChild.getId().equals("questionsPanel"))
					{
						questionsPanel=tabChild;
					}
					if (randomPanel!=null && questionsPanel!=null)
					{
						break;
					}
				}
				if (randomPanel!=null)
				{
					int inputsProcessed=0;
					for (UIComponent panelGr:randomPanel.getChildren())
					{
						UIComponent panelGrid=null;
						if (panelGr.getId().equals("randomQuantityPanelGroup"))
						{
							panelGrid=panelGr.getChildren().get(0);
						}
						else
						{
							panelGrid=panelGr;
						}
						if (panelGrid!=null)
						{
							int subInputsProcessed=0;
							for (UIComponent panelGridChild:panelGrid.getChildren())
							{
								if (panelGridChild.getId().equals("sectionName"))
								{
									if (!exceptions.contains("sectionName"))
									{
										UIInput sectionName=(UIInput)panelGridChild;
										sectionName.processDecodes(context);
										if (sectionName.getSubmittedValue()!=null)
										{
											section.setName((String)sectionName.getSubmittedValue());
										}
									}
									inputsProcessed++;
									subInputsProcessed++;
									if (subInputsProcessed==3)
									{
										break;
									}
								}
								else if (panelGridChild.getId().equals("sectionTitle"))
								{
									if (!exceptions.contains("sectionTitle"))
									{
										UIInput sectionTitle=(UIInput)panelGridChild;
										sectionTitle.processDecodes(context);
										if (sectionTitle.getSubmittedValue()!=null)
										{
											section.setTitle((String)sectionTitle.getSubmittedValue());
										}
									}
									inputsProcessed++;
									subInputsProcessed++;
									if (subInputsProcessed==3)
									{
										break;
									}
								}
								else if (panelGridChild.getId().equals("sectionWeight"))
								{
									if (!exceptions.contains("sectionWeight"))
									{
										UIInput sectionWeight=(UIInput)panelGridChild;
										sectionWeight.processDecodes(context);
										if (sectionWeight.getSubmittedValue()!=null)
										{
											int newSectionWeight=Integer.MAX_VALUE;
											try
											{
												newSectionWeight=
													Integer.parseInt((String)sectionWeight.getSubmittedValue());
											}
											catch (NumberFormatException nfe)
											{
												newSectionWeight=Integer.MAX_VALUE;
											}
											if (section.checkChangeWeight(newSectionWeight))
											{
												setPropertyChecked("sectionWeight");
											}
											section.setWeight(newSectionWeight);
											changeSectionWeight(section);
										}
									}
									inputsProcessed++;
									subInputsProcessed++;
									if (subInputsProcessed==3)
									{
										break;
									}
								}
								else if (panelGridChild.getId().equals("shuffle"))
								{
									if (!exceptions.contains("shuffle"))
									{
										UIInput shuffle=(UIInput)panelGridChild;
										shuffle.processDecodes(context);
										if (shuffle.getSubmittedValue()!=null)
										{
											section.setShuffle(
												Boolean.valueOf((String)shuffle.getSubmittedValue()));
										}
									}
									inputsProcessed++;
									subInputsProcessed++;
									if (subInputsProcessed==2)
									{
										break;
									}
								}
								else if (panelGridChild.getId().equals("random"))
								{
									if (!exceptions.contains("random"))
									{
										UIInput random=(UIInput)panelGridChild;
										random.processDecodes(context);
										if (random.getSubmittedValue()!=null)
										{
											section.setRandom(Boolean.valueOf((String)random.getSubmittedValue()));
										}
									}
									inputsProcessed++;
									subInputsProcessed++;
									if (subInputsProcessed==2)
									{
										break;
									}
								}
								else if (panelGridChild.getId().equals("randomQuantity"))
								{
									if (!exceptions.contains("randomQuantity"))
									{
										UIInput randomQuantity=(UIInput)panelGridChild;
										randomQuantity.processDecodes(context);
										if (randomQuantity.getSubmittedValue()!=null)
										{
											int newRandomQuantity=-1;
											try
											{
												newRandomQuantity=
													Integer.parseInt((String)randomQuantity.getSubmittedValue());
											}
											catch (NumberFormatException nfe)
											{
												newRandomQuantity=section.getQuestionOrdersSize();
											}
											if (section.checkChangeRandomQuantity(newRandomQuantity))
											{
												setPropertyChecked("randomQuantity");
											}
											section.setRandomQuantity(newRandomQuantity);
										}
									}
									inputsProcessed++;
									break;
								}
							}
						}
						if (inputsProcessed==6)
						{
							break;
						}
					}
				}
				if (questionsPanel!=null)
				{
					DataTable questionsSection=null;
					for (UIComponent questionsPanelChild:questionsPanel.getChildren())
					{
						if (questionsPanelChild instanceof DataTable && 
							questionsPanelChild.getId().equals("questionsSection"))
						{
							questionsSection=(DataTable)questionsPanelChild;
							break;
						}
					}
					if (questionsSection!=null)
					{
						Column columnWeight=null;
						for (UIComponent questionsSectionChild:questionsSection.getChildren())
						{
							if (questionsSectionChild instanceof Column && 
								((Column)questionsSectionChild).getStyleClass().equals("columnWeight"))
							{
								columnWeight=(Column)questionsSectionChild;
								break;
							}
						}
						if (columnWeight!=null)
						{
							for (UIComponent columnWeightChild:columnWeight.getChildren())
							{
								if (columnWeightChild.getId().equals("questionOrderWeight") && 
									!exceptions.contains("questionOrderWeight"))
								{
									UIInput questionOrderWeight=(UIInput)columnWeightChild;
									
									// Save current datatable row index
									int currentDatatableRowIndex=questionsSection.getRowIndex();
									
									// We need to process all question weights (all rows)
									for (int i=0;i<questionsSection.getRowCount();i++)
									{
										questionsSection.setRowIndex(i);
										questionOrderWeight.pushComponentToEL(context,null);
										questionOrderWeight.processDecodes(context);
										if (questionOrderWeight.getSubmittedValue()!=null)
										{
											QuestionOrderBean questionOrder=
												(QuestionOrderBean)questionsSection.getRowData();
											int newQuestionOrderWeight=Integer.MAX_VALUE;
											try
											{
												newQuestionOrderWeight=Integer.parseInt(
													(String)questionOrderWeight.getSubmittedValue());
											}
											catch (NumberFormatException nfe)
											{
												newQuestionOrderWeight=Integer.MAX_VALUE;
											}
											if (questionOrder.checkChangeWeight(newQuestionOrderWeight))
											{
												questionOrderChecked=questionOrder;
												setPropertyChecked("questionOrderWeight");
											}
											questionOrder.setWeight(newQuestionOrderWeight);
											changeQuestionOrderWeight(questionOrder);
										}
										questionOrderWeight.popComponentFromEL(context);
									}
									
									// Set back datatable row index
									questionsSection.setRowIndex(currentDatatableRowIndex);
								}
							}
						}
					}
				}
				
	    		// Set back accordion row index
	    		sectionsAccordion.setRowIndex(currentAccordionRowIndex);
			}
		}
	}
	
	/**
	 * Process preliminary summary editor input field within the preliminary summary tab of a test.
	 * @param component Component that triggered the listener that called this method
	 */
	private void processPreliminarySummaryEditorField(UIComponent component)
	{
		FacesContext context=FacesContext.getCurrentInstance();
		UIInput preliminarySummaryTextInput=
			(UIInput)component.findComponent(":testForm:testFormTabs:preliminarySummaryText");
		preliminarySummaryTextInput.processDecodes(context);
		if (preliminarySummaryTextInput.getSubmittedValue()!=null)
		{
			setPreliminarySummary((String)preliminarySummaryTextInput.getSubmittedValue());
		}
	}
	
	/**
	 * Process some editor input fields within the feedback tab of a test.
	 * @param operation Operation
	 * @param component Component that triggered the listener that called this method
	 */
	private void processFeedbackEditorInputFields(Operation operation,UIComponent component)
	{
		FacesContext context=FacesContext.getCurrentInstance();
		AccordionPanel feedbackAccordion=getFeedbackAccordion(getCurrentUserOperation(operation),component);
		switch (Integer.parseInt(feedbackAccordion.getActiveIndex()))
		{
			case SUMMARY_TAB_FEEDBACK_ACCORDION:
				if (isFeedbackDisplaySummary())
				{
					UIInput feedbackSummaryPreviousInput=(UIInput)component.findComponent(
						":testForm:testFormTabs:feedbackAccordion:feedbackSummaryPrevious");
					feedbackSummaryPreviousInput.processDecodes(context);
					if (feedbackSummaryPreviousInput.getSubmittedValue()!=null)
					{
						setFeedbackSummaryPrevious((String)feedbackSummaryPreviousInput.getSubmittedValue());
					}
				}
				break;
			case SCORES_TAB_FEEDBACK_ACCORDION:
				if (isFeedbackDisplayScores())
				{
					UIInput feedbackScoresPreviousInput=(UIInput)component.findComponent(
						":testForm:testFormTabs:feedbackAccordion:feedbackScoresPrevious");
					feedbackScoresPreviousInput.processDecodes(context);
					if (feedbackScoresPreviousInput.getSubmittedValue()!=null)
					{
						setFeedbackScoresPrevious((String)feedbackScoresPreviousInput.getSubmittedValue());
					}
				}
				break;
			case ADVANCED_FEEDBACK_TAB_FEEDBACK_ACCORDION:
				UIInput feedbackAdvancedPreviousInput=(UIInput)component.findComponent(
					":testForm:testFormTabs:feedbackAccordion:feedbackAdvancedPrevious");
				feedbackAdvancedPreviousInput.processDecodes(context);
				if (feedbackAdvancedPreviousInput.getSubmittedValue()!=null)
				{
					setFeedbackAdvancedPrevious((String)feedbackAdvancedPreviousInput.getSubmittedValue());
				}
				UIInput feedbackAdvancedNextInput=
					(UIInput)component.findComponent(":testForm:testFormTabs:feedbackAccordion:feedbackAdvancedNext");
				feedbackAdvancedNextInput.processDecodes(context);
				if (feedbackAdvancedNextInput.getSubmittedValue()!=null)
				{
					setFeedbackAdvancedNext((String)feedbackAdvancedNextInput.getSubmittedValue());
				}
		}
	}
	
	/**
	 * Process some input fields of the advanced feedbacks tab of the accordion within the feedback tab of a test.
	 * @param operation Operation
	 * @param component Component that triggered the listener that called this method
	 */
	private void processAdvancedFeedbacksFeedbackInputFields(Operation operation,UIComponent component)
	{
		FacesContext context=FacesContext.getCurrentInstance();
		String feedbackAdvancedPreviousId=null;
		String feedbackAdvancedNextId=null;
		if (getTestId(getCurrentUserOperation(operation))==0L)
		{
			feedbackAdvancedPreviousId=":testForm:feedbackAccordion:feedbackAdvancedPrevious";
			feedbackAdvancedNextId=":testForm:feedbackAccordion:feedbackAdvancedNext";
		}
		else
		{
			feedbackAdvancedPreviousId=":testForm:testFormTabs:feedbackAccordion:feedbackAdvancedPrevious";
			feedbackAdvancedNextId=":testForm:testFormTabs:feedbackAccordion:feedbackAdvancedNext";
		}
		UIInput feedbackAdvancedPrevious=(UIInput)component.findComponent(feedbackAdvancedPreviousId);
		feedbackAdvancedPrevious.processDecodes(context);
		if (feedbackAdvancedPrevious.getSubmittedValue()!=null)
		{
			setFeedbackAdvancedPrevious((String)feedbackAdvancedPrevious.getSubmittedValue());
		}
		UIInput feedbackAdvancedNext=(UIInput)component.findComponent(feedbackAdvancedNextId);
		feedbackAdvancedNext.processDecodes(context);
		if (feedbackAdvancedNext.getSubmittedValue()!=null)
		{
			setFeedbackAdvancedNext((String)feedbackAdvancedNext.getSubmittedValue());
		}
	}
	
	/**
	 * Update text fields of the sections tab of a test.<br/><br/>
	 * This is needed after some operations because text fields are not always being updated correctly on 
	 * page view.
	 * @param operation Operation
	 * @param component Component that triggered the listener that called this method
	 * @param numTabs Number of tabs to update
	 */
	private void updateSectionsTextFields(Operation operation,UIComponent component,int numTabs)
	{
		// Get current user session Hibernate operation
		operation=getCurrentUserOperation(operation);
		
		UIData sectionsAccordion=(UIData)getSectionsAccordion(operation,component);
        UIComponent tab=sectionsAccordion.getChildren().get(0);
		UIInput sectionNameInput=null;
		UIInput sectionTitleInput=null;
		for (UIComponent sectionTabChild:tab.getChildren())
		{
			if (sectionTabChild.getId().equals("randomPanel"))
			{
				for (UIComponent randomPanelChild:sectionTabChild.getChildren())
				{
					if (randomPanelChild.getId().equals("sectionNamesGrid"))
					{
						for (UIComponent sectionNamesGridChild:randomPanelChild.getChildren())
						{
							if (sectionNamesGridChild.getId().equals("sectionName"))
							{
								sectionNameInput=(UIInput)sectionNamesGridChild;
								for (int i=0;i<numTabs;i++)
								{
									sectionsAccordion.setRowIndex(i);
									sectionNameInput.pushComponentToEL(FacesContext.getCurrentInstance(),null);
									sectionNameInput.setSubmittedValue(getSection(operation,i+1).getName());
									sectionNameInput.popComponentFromEL(FacesContext.getCurrentInstance());
								}
								sectionsAccordion.setRowIndex(-1);
								if (sectionTitleInput!=null)
								{
									break;
								}
							}
							else if (sectionNamesGridChild.getId().equals("sectionTitle"))
							{
								sectionTitleInput=(UIInput)sectionNamesGridChild;
								for (int i=0;i<numTabs;i++)
								{
									sectionsAccordion.setRowIndex(i);
									sectionTitleInput.pushComponentToEL(FacesContext.getCurrentInstance(),null);
									sectionTitleInput.setSubmittedValue(getSection(operation,i+1).getTitle());
									sectionTitleInput.popComponentFromEL(FacesContext.getCurrentInstance());
								}
								sectionsAccordion.setRowIndex(-1);
								if (sectionTitleInput!=null)
								{
									break;
								}
							}
						}
						break;
					}
				}
				break;
			}
			if (sectionNameInput!=null && sectionTitleInput!=null)
			{
				break;
			}
		}
	}
	
	private void processSectionWeight(Operation operation,UIComponent component,SectionBean section)
	{
		List<String> exceptions=new ArrayList<String>();
		exceptions.add("sectionName");
		exceptions.add("sectionTitle");
		exceptions.add("shuffle");
		exceptions.add("random");
		exceptions.add("randomQuantity");
		exceptions.add("questionOrderWeight");
		processSectionsInputFields(getCurrentUserOperation(operation),component,section,exceptions);
	}
	
	private void processSectionRandomQuantity(Operation operation,UIComponent component,SectionBean section)
	{
		List<String> exceptions=new ArrayList<String>();
		exceptions.add("sectionName");
		exceptions.add("sectionTitle");
		exceptions.add("sectionWeight");
		exceptions.add("shuffle");
		exceptions.add("random");
		exceptions.add("questionOrderWeight");
		processSectionsInputFields(getCurrentUserOperation(operation),component,section,exceptions);
	}
	
	private void processQuestionOrderWeights(Operation operation,UIComponent component,SectionBean section)
	{
		List<String> exceptions=new ArrayList<String>();
		exceptions.add("sectionName");
		exceptions.add("sectionTitle");
		exceptions.add("sectionWeight");
		exceptions.add("shuffle");
		exceptions.add("random");
		exceptions.add("randomQuantity");
		processSectionsInputFields(getCurrentUserOperation(operation),component,section,exceptions);
	}
	
	private void updateSectionRandomQuantity(Operation operation,UIComponent component,SectionBean section)
	{
		if (section!=null)
		{
			UIData sectionsAccordion=(UIData)getSectionsAccordion(getCurrentUserOperation(operation),component);
			int currentAccordionRowIndex=sectionsAccordion.getRowIndex();
			sectionsAccordion.setRowIndex(section.getOrder()-1);
	        UIComponent tab=sectionsAccordion.getChildren().get(0);
			UIInput randomQuantity=null;
			for (UIComponent sectionTabChild:tab.getChildren())
			{
				if (sectionTabChild.getId().equals("randomPanel"))
				{
					for (UIComponent randomPanelChild:sectionTabChild.getChildren())
					{
						if (randomPanelChild.getId().equals("randomQuantityPanelGroup"))
						{
							UIComponent randomQuantityPanelGrid=randomPanelChild.getChildren().get(0);
							for (UIComponent randomQuantityPanelGridChild:randomQuantityPanelGrid.getChildren())
							{
								if (randomQuantityPanelGridChild.getId().equals("randomQuantity"))
								{
									randomQuantity=(UIInput)randomQuantityPanelGridChild;
									randomQuantity.setSubmittedValue(
										Integer.toString(section.getRandomQuantity()));
									break;
								}
							}
							break;
						}
					}
				}
				if (randomQuantity!=null)
				{
					break;
				}
			}
			sectionsAccordion.setRowIndex(currentAccordionRowIndex);
		}
	}
	
	private void updateSectionWeights(Operation operation,UIComponent component,SectionBean section,
		boolean updateSectionWeight,boolean updateQuestionOrderWeights)
	{
		if (section!=null)
		{
			UIData sectionsAccordion=(UIData)getSectionsAccordion(getCurrentUserOperation(operation),component);
			int currentAccordionRowIndex=sectionsAccordion.getRowIndex();
			sectionsAccordion.setRowIndex(section.getOrder()-1);
	        UIComponent tab=sectionsAccordion.getChildren().get(0);
	        UIInput sectionWeight=null;
			UIInput questionOrderWeight=null;
			for (UIComponent sectionTabChild:tab.getChildren())
			{
				if (updateSectionWeight && sectionTabChild.getId().equals("randomPanel"))
				{
					for (UIComponent randomPanelChild:sectionTabChild.getChildren())
					{
						if (randomPanelChild.getId().equals("sectionNamesGrid"))
						{
							for (UIComponent sectionNamesGridChild:randomPanelChild.getChildren())
							{
								if (sectionNamesGridChild.getId().equals("sectionWeight"))
								{
									sectionWeight=(UIInput)sectionNamesGridChild;
									sectionWeight.setSubmittedValue(Integer.toString(section.getWeight()));
									break;
								}
							}
							break;
						}
					}
				}
				else if (updateQuestionOrderWeights && sectionTabChild.getId().equals("questionsPanel"))
				{
					for (UIComponent questionsPanelChild:sectionTabChild.getChildren())
					{
						if (questionsPanelChild.getId().equals("questionsSection"))
						{
							UIData questionsSection=(UIData)questionsPanelChild;
							for (UIComponent questionsSectionChild:questionsSection.getChildren())
							{
								if (questionsSectionChild instanceof Column && 
									((Column)questionsSectionChild).getStyleClass().equals("columnWeight"))
								{
									for (UIComponent columnWeightChild:questionsSectionChild.getChildren())
									{
										if (columnWeightChild.getId().equals("questionOrderWeight"))
										{
											questionOrderWeight=(UIInput)columnWeightChild;
											int currentDatatableRowIndex=questionsSection.getRowIndex();
											for (int i=0;i<questionsSection.getRowCount();i++)
											{
												questionsSection.setRowIndex(i);
												QuestionOrderBean questionOrder=
													(QuestionOrderBean)questionsSection.getRowData();
												questionOrderWeight.setSubmittedValue(
													Integer.toString(questionOrder.getWeight()));
											}
											questionsSection.setRowIndex(currentDatatableRowIndex);
											break;
										}
									}
									break;
								}
							}
							break;
						}
					}
				}
				if ((!updateSectionWeight || sectionWeight!=null) && 
					(!updateQuestionOrderWeights || questionOrderWeight!=null))
				{
					break;
				}
			}
			sectionsAccordion.setRowIndex(currentAccordionRowIndex);
		}
	}
	
	private void updateSectionWeights(Operation operation,UIComponent component,SectionBean section)
	{
		updateSectionWeights(getCurrentUserOperation(operation),component,section,true,true);
	}
	
	private void updateSectionWeight(Operation operation,UIComponent component,SectionBean section)
	{
		updateSectionWeights(getCurrentUserOperation(operation),component,section,true,false);
	}
	
	private void updateQuestionOrderWeights(Operation operation,UIComponent component,SectionBean section)
	{
		updateSectionWeights(getCurrentUserOperation(operation),component,section,false,true);
	}
	
	/**
	 * Display a dialog to add users allowed to do test. 
     * @param event Action event
	 */
	public void showAddUsers(ActionEvent event)
	{
		usersDualList=null;
   		
		// Get current user session Hibernate operation
		Operation operation=getCurrentUserOperation(null);
		
		// We need to process some input fields
		processCommonDataInputFields(operation,event.getComponent());
		processUsersTabCommonDataInputFields(operation,event.getComponent());
   		
		RequestContext rq=RequestContext.getCurrentInstance();
		rq.execute("addUsersDialog.show()");
	}
	
    /**
     * Add users selected within dialog to list of users allowed to do test.
     * @param event Action event
     */
    public void acceptAddUsers(ActionEvent event)
    {
   		for (User user:usersDualList.getTarget())
  		{
   			testUsers.add(user);
   		}
    }
	
    /**
     * ActionListener that deletes an user from list of users allowed to do test.
     * @param event Action event
     */
    public void removeUser(ActionEvent event)
    {
    	// Get current user session Hibernate operation
    	Operation operation=getCurrentUserOperation(null);
    	
		// We need to process some input fields
		processCommonDataInputFields(operation,event.getComponent());
		processUsersTabCommonDataInputFields(operation,event.getComponent());
		
		testUsers.remove((User)event.getComponent().getAttributes().get("user"));
	}
    
	/**
	 * Display a dialog to add administrators to test. 
     * @param event Action event
	 */
	public void showAddAdmins(ActionEvent event)
	{
   		adminsDualList=null;
   		
   		// Get current user session Hibernate operation
   		Operation operation=getCurrentUserOperation(null);
   		
		// We need to process some input fields
		processCommonDataInputFields(operation,event.getComponent());
		processUsersTabCommonDataInputFields(operation,event.getComponent());
   		
		RequestContext rq=RequestContext.getCurrentInstance();
		rq.execute("addAdminsDialog.show()");
	}
	
    /**
     * Add administrators selected within dialog to test.
     * @param event Action event
     */
    public void acceptAddAdmins(ActionEvent event)
    {
   		for (User admin:adminsDualList.getTarget())
  		{
   			testAdmins.add(admin);
   		}
    }
	
    /**
     * Deletes an administrator from test.
     * @param event Action event
     */
    public void removeAdmin(ActionEvent event)
    {
    	// Get current user session Hibernate operation
    	Operation operation=getCurrentUserOperation(null);
    	
		// We need to process some input fields
		processCommonDataInputFields(operation,event.getComponent());
		processUsersTabCommonDataInputFields(operation,event.getComponent());
    	
    	testAdmins.remove((User)event.getComponent().getAttributes().get("admin"));
	}
    
    /**
     * Reset start date.
     * @param event Action event
     */
    public void resetStartDate(ActionEvent event)
    {
    	setStartDate(null);
    	setStartDateHidden("");
    	
    	// We need to update manually 'startDateHidden' hidden input field
    	FacesContext context=FacesContext.getCurrentInstance();
    	String startDateHiddenId=null;
    	if (getTestId(getCurrentUserOperation(null))>0L)
    	{
    		startDateHiddenId=":testForm:testFormTabs:generalAccordion:startDateHidden";
    	}
    	else
    	{
    		startDateHiddenId=":testForm:generalAccordion:startDateHidden";
    	}
    	UIInput startDateHidden=(UIInput)event.getComponent().findComponent(startDateHiddenId);
    	startDateHidden.pushComponentToEL(context,null);
    	startDateHidden.setSubmittedValue("");
    	startDateHidden.popComponentFromEL(context);
    }
    
    /**
     * Reset close date.
     * @param event Action event
     */
    public void resetCloseDate(ActionEvent event)
    {
    	setCloseDate(null);
    	setCloseDateHidden("");
    	
    	// We need to update manually 'closeDateHidden' hidden input field
    	FacesContext context=FacesContext.getCurrentInstance();
    	String closeDateHiddenId=null;
    	if (getTestId(getCurrentUserOperation(null))>0L)
    	{
    		closeDateHiddenId=":testForm:testFormTabs:generalAccordion:closeDateHidden";
    	}
    	else
    	{
    		closeDateHiddenId=":testForm:generalAccordion:closeDateHidden";
    	}
    	UIInput closeDateHidden=(UIInput)event.getComponent().findComponent(closeDateHiddenId);
    	closeDateHidden.pushComponentToEL(context,null);
    	closeDateHidden.setSubmittedValue("");
    	closeDateHidden.popComponentFromEL(context);
    }
    
    /**
     * Reset warning date.
     * @param event Action event
     */
    public void resetWarningDate(ActionEvent event)
    {
    	setWarningDate(null);
    	setWarningDateHidden("");
    	
    	// We need to update manually 'warningDateHidden' hidden input field
    	FacesContext context=FacesContext.getCurrentInstance();
    	String warningDateHiddenId=null;
    	if (getTestId(getCurrentUserOperation(null))>0L)
    	{
    		warningDateHiddenId=":testForm:testFormTabs:generalAccordion:warningDateHidden";
    	}
    	else
    	{
    		warningDateHiddenId=":testForm:generalAccordion:warningDateHidden";
    	}
    	UIInput warningDateHidden=(UIInput)event.getComponent().findComponent(warningDateHiddenId);
    	warningDateHidden.pushComponentToEL(context,null);
    	warningDateHidden.setSubmittedValue("");
    	warningDateHidden.popComponentFromEL(context);
    }
    
    /**
     * Reset feedback date.
     * @param event Action event
     */
    public void resetFeedbackDate(ActionEvent event)
    {
    	setFeedbackDate(null);
    	setFeedbackDateHidden("");
    	
    	// We need to update manually 'feedbackDateHidden' hidden input field
    	FacesContext context=FacesContext.getCurrentInstance();
    	String feedbackDateHiddenId=null;
    	if (getTestId(getCurrentUserOperation(null))>0L)
    	{
    		feedbackDateHiddenId=":testForm:testFormTabs:generalAccordion:feedbackDateHidden";
    	}
    	else
    	{
    		feedbackDateHiddenId=":testForm:generalAccordion:feedbackDateHidden";
    	}
    	UIInput feedbackDateHidden=(UIInput)event.getComponent().findComponent(feedbackDateHiddenId);
    	feedbackDateHidden.pushComponentToEL(context,null);
    	feedbackDateHidden.setSubmittedValue("");
    	feedbackDateHidden.popComponentFromEL(context);
    }
    
    /**
     * @return Letters from A to Z
     */
    public List<String> getLetters()
    {
    	if (letters==null)
    	{
        	letters=new ArrayList<String>();
        	for (char c='A';c<='Z';c++)
        	{
        		letters.add(Character.toString(c));
        	}
    	}
    	return letters;
    }
    
	/**
	 * Display a dialog to add a support contact. 
     * @param event Action event
	 */
	public void showAddSupportContact(ActionEvent event)
	{
		// Get current user session Hibernate operation
		Operation operation=getCurrentUserOperation(null);
		
		// We need to process some input fields
		processCommonDataInputFields(operation,event.getComponent());
		
		// Check common data input fields
		if (checkCommonDataInputFields(operation))
		{
	   		setCurrentSupportContact(null);
			setSupportContactFilterType("NO_FILTER");
			setSupportContactFilterSubtype("");
	   		getTestSupportContactFilterUsers().clear();
	   		setFilteredUsersForAddingSupportContactFilterUsers(null);
	   		setSupportContactFilterUsersDualList(null);
	   		setFilterSupportContactRangeNameLowerLimit("A");
	   		setFilterSupportContactRangeNameUpperLimit("Z");
	   		setFilterSupportContactRangeSurnameLowerLimit("A");
	   		setFilterSupportContactRangeSurnameUpperLimit("Z");
	   		setSupportContact("");
	   		setSupportContactDialogDisplayed(true);
			
			RequestContext rq=RequestContext.getCurrentInstance();
			rq.execute("addTechSupportAddressDialog.show()");
		}
		else
		{
			// Scroll page to top position
			scrollToTop();
		}
	}
	
	/**
	 * Display a dialog to edit a support contact. 
     * @param event Action event
	 */
	public void showEditSupportContact(ActionEvent event)
	{
		boolean ok=false;
		
		// Get current user session Hibernate operation
		Operation operation=getCurrentUserOperation(null);
		
		// We need to process some input fields
		processCommonDataInputFields(operation,event.getComponent());
		
		// Check common data input fields
		if (checkCommonDataInputFields(operation))
		{
			// Copy support contact so we work at dialog with a copy
			SupportContactBean supportContact=
				(SupportContactBean)event.getComponent().getAttributes().get("supportContact");
			setCurrentSupportContact(new SupportContactBean(this,supportContact.getSupportContact(),
				supportContact.getFilterType(),supportContact.getFilterSubtype(),supportContact.getFilterValue()));
			getCurrentSupportContact().setId(supportContact.getId());
			String filterType=getCurrentSupportContact().getFilterType();
			String filterSubtype=getCurrentSupportContact().getFilterSubtype();
			String filterValue=getCurrentSupportContact().getFilterValue();
			setSupportContactFilterType(filterType);
			setSupportContactFilterSubtype(filterSubtype);
			setSupportContactFilterSubtypes(null);
	   		getTestSupportContactFilterUsers().clear();
	   		setFilteredUsersForAddingSupportContactFilterUsers(null);
	   		setSupportContactFilterUsersDualList(null);
	   		setFilterSupportContactRangeNameLowerLimit("A");
	   		setFilterSupportContactRangeNameUpperLimit("Z");
	   		setFilterSupportContactRangeSurnameLowerLimit("A");
	   		setFilterSupportContactRangeSurnameUpperLimit("Z");
	   		if (filterValue!=null && !"".equals(filterValue) && "USER_FILTER".equals(filterType))
	   		{
	   			if ("USERS_SELECTION".equals(filterSubtype))
	   			{
	   				List<String> checkedOUCUs=new ArrayList<String>();
	   				for (String sOUCU:filterValue.split(","))
	   				{
	   					if (!checkedOUCUs.contains(sOUCU))
	   					{
		   					User user=usersService.getUserFromOucu(operation,sOUCU);
		   					if (user!=null)
		   					{
		   						getTestSupportContactFilterUsers().add(user);
		   					}
		   					checkedOUCUs.add(sOUCU);
	   					}
	   				}
	   			}
	   			else if ("RANGE_NAME".equals(filterSubtype))
	   			{
	   				if (filterValue.length()==3 && filterValue.charAt(1)=='-')
	   				{
	   					char lowerLimitChar=filterValue.charAt(0);
	   					char upperLimitChar=filterValue.charAt(2);
	   					if (lowerLimitChar>='A' && lowerLimitChar<='Z')
	   					{
	   						setFilterSupportContactRangeNameLowerLimit(Character.toString(lowerLimitChar));
	   					}
	   					if (upperLimitChar>='A' && upperLimitChar<='Z')
	   					{
	   		   		   		setFilterSupportContactRangeNameUpperLimit(Character.toString(upperLimitChar));
	   					}
	   				}
	   			}
	   			else if ("RANGE_SURNAME".equals(filterSubtype))
	   			{
	   				if (filterValue.length()==3 && filterValue.charAt(1)=='-')
	   				{
	   					char lowerLimitChar=filterValue.charAt(0);
	   					char upperLimitChar=filterValue.charAt(2);
	   					if (lowerLimitChar>='A' && lowerLimitChar<='Z')
	   					{
	   						setFilterSupportContactRangeSurnameLowerLimit(Character.toString(lowerLimitChar));
	   					}
	   					if (upperLimitChar>='A' && upperLimitChar<='Z')
	   					{
	   		   		   		setFilterSupportContactRangeSurnameUpperLimit(Character.toString(upperLimitChar));
	   					}
	   				}
	   			}
	   		}
	   		setSupportContact(getCurrentSupportContact().getSupportContact());
	   		setSupportContactDialogDisplayed(true);
	   		
	   		ok=true;
	   		
			RequestContext rq=RequestContext.getCurrentInstance();
			rq.execute("addTechSupportAddressDialog.show()");
		}
		
		if (!ok)
		{
			// Scroll page to top position
			scrollToTop();
		}
	}
    
    /**
     * Adds a support contact.
     * @param event Action event
     */
	public void acceptAddSupportContact(ActionEvent event)
	{
		// Check support contact
		if (checkSupportContact())
		{
			// Get current user session Hibernate operation
			Operation operation=getCurrentUserOperation(null);
			
			String filterType=getSupportContactFilterType();
			String filterSubtype=getSupportContactFilterSubtype(operation);
			if (filterSubtype==null)
			{
				filterSubtype="";
			}
			StringBuffer filterValue=new StringBuffer();
			if ("USER_FILTER".equals(filterType))
			{
				if ("USERS_SELECTION".equals(filterSubtype))
				{
					refreshSupportContactFilterUsersDualList(operation,event.getComponent());
					for (User supportedUser:getTestSupportContactFilterUsers())
					{
						if (filterValue.length()>0)
						{
							filterValue.append(',');
						}
						filterValue.append(supportedUser.getOucu());
					}
				}
				else if ("RANGE_NAME".equals(filterSubtype))
				{
					filterValue.append(getFilterSupportContactRangeNameLowerLimit());
					filterValue.append('-');
					filterValue.append(getFilterSupportContactRangeNameUpperLimit());
				}
				else if ("RANGE_SURNAME".equals(filterSubtype))
				{
					filterValue.append(getFilterSupportContactRangeSurnameLowerLimit());
					filterValue.append('-');
					filterValue.append(getFilterSupportContactRangeSurnameUpperLimit());
				}
			}
			SupportContactBean currentSupportContact=getCurrentSupportContact();
			if (currentSupportContact==null)
			{
				// Add a new support contact
				getSupportContacts(operation).add(
					new SupportContactBean(this,getSupportContact(),filterType,filterSubtype,filterValue.toString()));
			}
			else
			{
				SupportContactBean supportContact=null;
				for (SupportContactBean s:getSupportContacts(operation))
				{
					if (currentSupportContact.equals(s))
					{
						supportContact=s;
					}
				}
				if (supportContact!=null)
				{
					supportContact.setSupportContact(getSupportContact());
					supportContact.setFilterType(filterType);
					supportContact.setFilterSubtype(filterSubtype);
					supportContact.setFilterValue(filterValue.toString());
				}
			}
	   		setSupportContactDialogDisplayed(false);
			
			// Close dialog
			RequestContext rq=RequestContext.getCurrentInstance();
			rq.execute("addTechSupportAddressDialog.hide()");
		}
	}
	
    /**
     * @param event Action event
     */
	public void cancelAddSupportContact(ActionEvent event)
	{
   		setSupportContactDialogDisplayed(false);
	}
	
	/**
	 * Deletes a support contact.
     * @param event Action event
	 */
	public void removeSupportContact(ActionEvent event)
	{
		// Get current user session Hibernate operation
		Operation operation=getCurrentUserOperation(null);
		
		// We need to process some input fields
		processCommonDataInputFields(operation,event.getComponent());
		
		// Check common data input fields
		if (checkCommonDataInputFields(operation))
		{
			getSupportContacts(operation).remove(event.getComponent().getAttributes().get("supportContact"));
		}
		else
		{
			// Scroll page to top position
			scrollToTop();
		}
	}
    
	/**
	 * Check that the support contact is a valid email address, displaying an error message otherwise.
	 * @return true if the support contact is a valid email address, false otherwise
	 */
	public boolean checkSupportContact()
	{
		boolean ok=true;
		if (getSupportContact()==null || "".equals(getSupportContact()))
		{
			ok=false;
			addErrorMessage("INCORRECT_OPERATION","TEST_ADDRESS_REQUIRED");
		}
		else if (!EmailValidator.validate(getSupportContact()))
		{
			ok=false;
			addErrorMessage("INCORRECT_OPERATION","TEST_ADDRESS_INVALID");
		}
		return ok;
	}
	
    /**
     * @param event Ajax event
     */
    public void changeFilterSupportContactRangeNameLowerLimit(AjaxBehaviorEvent event)
    {
    	if (getFilterSupportContactRangeNameLowerLimit().compareTo(getFilterSupportContactRangeNameUpperLimit())>0)
    	{
    		setFilterSupportContactRangeNameUpperLimit(getFilterSupportContactRangeNameLowerLimit());
    	}
    }
	
    /**
     * @param event Ajax event
     */
    public void changeFilterSupportContactRangeNameUpperLimit(AjaxBehaviorEvent event)
    {
    	if (getFilterSupportContactRangeNameLowerLimit().compareTo(getFilterSupportContactRangeNameUpperLimit())>0)
    	{
    		setFilterSupportContactRangeNameLowerLimit(getFilterSupportContactRangeNameUpperLimit());
    	}
    }
	
    /**
     * @param event Ajax event
     */
    public void changeFilterSupportContactRangeSurnameLowerLimit(AjaxBehaviorEvent event)
    {
    	if (getFilterSupportContactRangeSurnameLowerLimit().compareTo(
    		getFilterSupportContactRangeSurnameUpperLimit())>0)
    	{
    		setFilterSupportContactRangeSurnameUpperLimit(getFilterSupportContactRangeSurnameLowerLimit());
    	}
    }
	
    /**
     * @param event Ajax event
     */
    public void changeFilterSupportContactRangeSurnameUpperLimit(AjaxBehaviorEvent event)
    {
    	if (getFilterSupportContactRangeSurnameLowerLimit().compareTo(
    		getFilterSupportContactRangeSurnameUpperLimit())>0)
    	{
    		setFilterSupportContactRangeSurnameLowerLimit(getFilterSupportContactRangeSurnameUpperLimit());
    	}
    }
	
	/**
	 * Display a dialog to add an evaluator. 
     * @param event Action event
	 */
	public void showAddEvaluator(ActionEvent event)
	{
		// Get current user session Hibernate operation
		Operation operation=getCurrentUserOperation(null);
		
		// We need to process some input fields
		processCommonDataInputFields(operation,event.getComponent());
		
		// Check common data input fields
		if (checkCommonDataInputFields(operation))
		{
	   		setCurrentEvaluator(null);
			setEvaluatorFilterType("NO_FILTER");
			setEvaluatorFilterSubtype("");
	   		getTestEvaluatorFilterUsers().clear();
	   		setFilteredUsersForAddingEvaluatorFilterUsers(null);
	   		setEvaluatorFilterUsersDualList(null);
	   		setFilterEvaluatorRangeNameLowerLimit("A");
	   		setFilterEvaluatorRangeNameUpperLimit("Z");
	   		setFilterEvaluatorRangeSurnameLowerLimit("A");
	   		setFilterEvaluatorRangeSurnameUpperLimit("Z");
	   		setEvaluator("");
	   		setEvaluatorDialogDisplayed(true);
			
			RequestContext rq=RequestContext.getCurrentInstance();
			rq.execute("addAssessementAddressDialog.show()");
		}
		else
		{
			// Scroll page to top position
			scrollToTop();
		}
	}
	
	/**
	 * Display a dialog to edit an evaluator. 
     * @param event Action event
	 */
	public void showEditEvaluator(ActionEvent event)
	{
		boolean ok=false;
		
		// Get current user session Hibernate operation
		Operation operation=getCurrentUserOperation(null);
		
		// We need to process some input fields
		processCommonDataInputFields(operation,event.getComponent());
		
		// Check common data input fields
		if (checkCommonDataInputFields(operation))
		{
			// Copy evaluator so we work at dialog with a copy
			EvaluatorBean evaluator=(EvaluatorBean)event.getComponent().getAttributes().get("evaluator");
			setCurrentEvaluator(new EvaluatorBean(this,evaluator.getEvaluator(),evaluator.getFilterType(),
				evaluator.getFilterSubtype(),evaluator.getFilterValue()));
			getCurrentEvaluator().setId(evaluator.getId());
			
			String filterType=getCurrentEvaluator().getFilterType();
			String filterSubtype=getCurrentEvaluator().getFilterSubtype();
			String filterValue=getCurrentEvaluator().getFilterValue();
			setEvaluatorFilterType(filterType);
			setEvaluatorFilterSubtype(filterSubtype);
			setEvaluatorFilterSubtypes(null);
		   	getTestEvaluatorFilterUsers().clear();
		   	setFilteredUsersForAddingEvaluatorFilterUsers(null);
		   	setEvaluatorFilterUsersDualList(null);
		   	setFilterEvaluatorRangeNameLowerLimit("A");
		   	setFilterEvaluatorRangeNameUpperLimit("Z");
		   	setFilterEvaluatorRangeSurnameLowerLimit("A");
		   	setFilterEvaluatorRangeSurnameUpperLimit("Z");
		   	if (filterValue!=null && !"".equals(filterValue) && "USER_FILTER".equals(filterType))
		   	{
		   		if ("USERS_SELECTION".equals(filterSubtype))
		   		{
		   			List<String> checkedOUCUs=new ArrayList<String>();
		   			for (String sOUCU:filterValue.split(","))
		   			{
		   				if (!checkedOUCUs.contains(sOUCU))
		   				{
							User user=usersService.getUserFromOucu(operation,sOUCU);
							if (user!=null)
							{
								getTestEvaluatorFilterUsers().add(user);
							}
							checkedOUCUs.add(sOUCU);
		   				}
		   			}
		   		}
		   		else if ("RANGE_NAME".equals(filterSubtype))
		   		{
		   			if (filterValue.length()==3 && filterValue.charAt(1)=='-')
		   			{
		   				char lowerLimitChar=filterValue.charAt(0);
		   				char upperLimitChar=filterValue.charAt(2);
		   				if (lowerLimitChar>='A' && lowerLimitChar<='Z')
		   				{
		   					setFilterEvaluatorRangeNameLowerLimit(Character.toString(lowerLimitChar));
		   				}
		   				if (upperLimitChar>='A' && upperLimitChar<='Z')
		   				{
		   	   		   		setFilterEvaluatorRangeNameUpperLimit(Character.toString(upperLimitChar));
		   				}
		   			}
		   		}
		   		else if ("RANGE_SURNAME".equals(filterSubtype))
		   		{
		   			if (filterValue.length()==3 && filterValue.charAt(1)=='-')
		   			{
		   				char lowerLimitChar=filterValue.charAt(0);
		   				char upperLimitChar=filterValue.charAt(2);
		   				if (lowerLimitChar>='A' && lowerLimitChar<='Z')
		   				{
		   					setFilterEvaluatorRangeSurnameLowerLimit(Character.toString(lowerLimitChar));
		   				}
		   				if (upperLimitChar>='A' && upperLimitChar<='Z')
		   				{
		   	   		   		setFilterEvaluatorRangeSurnameUpperLimit(Character.toString(upperLimitChar));
		   				}
		   			}
		   		}
		   	}
	 		setEvaluator(getCurrentEvaluator().getEvaluator());
	   		setEvaluatorDialogDisplayed(true);
	  		
		   	ok=true;
		   	
			RequestContext rq=RequestContext.getCurrentInstance();
			rq.execute("addAssessementAddressDialog.show()");
		}
		
		if (!ok)
		{
			// Scroll page to top position
			scrollToTop();
		}
	}
	
    /**
     * Adds an evaluator.
     * @param event Action event
     */
	public void acceptAddEvaluator(ActionEvent event)
	{
		// Check evaluator
		if (checkEvaluator())
		{
			// Get current user session Hibernate operation
			Operation operation=getCurrentUserOperation(null);
			
			String filterType=getEvaluatorFilterType();
			String filterSubtype=getEvaluatorFilterSubtype(operation);
			if (filterSubtype==null)
			{
				filterSubtype="";
			}
			StringBuffer filterValue=new StringBuffer();
			if ("USER_FILTER".equals(filterType))
			{
				if ("USERS_SELECTION".equals(filterSubtype))
				{
					refreshEvaluatorFilterUsersDualList(operation,event.getComponent());
					for (User assessedUser:getTestEvaluatorFilterUsers())
					{
						if (filterValue.length()>0)
						{
							filterValue.append(',');
						}
						filterValue.append(assessedUser.getOucu());
					}
				}
				else if ("RANGE_NAME".equals(filterSubtype))
				{
					filterValue.append(getFilterEvaluatorRangeNameLowerLimit());
					filterValue.append('-');
					filterValue.append(getFilterEvaluatorRangeNameUpperLimit());
				}
				else if ("RANGE_SURNAME".equals(filterSubtype))
				{
					filterValue.append(getFilterEvaluatorRangeSurnameLowerLimit());
					filterValue.append('-');
					filterValue.append(getFilterEvaluatorRangeSurnameUpperLimit());
				}
			}
			EvaluatorBean currentEvaluator=getCurrentEvaluator();
			if (currentEvaluator==null)
			{
				// Add a new evaluator
				getEvaluators(operation).add(
					new EvaluatorBean(this,getEvaluator(),filterType,filterSubtype,filterValue.toString()));
			}
			else
			{
				EvaluatorBean evaluator=null;
				for (EvaluatorBean e:getEvaluators(operation))
				{
					if (currentEvaluator.equals(e))
					{
						evaluator=e;
					}
				}
				if (evaluator!=null)
				{
					evaluator.setEvaluator(getEvaluator());
					evaluator.setFilterType(filterType);
					evaluator.setFilterSubtype(filterSubtype);
					evaluator.setFilterValue(filterValue.toString());
				}
			}
			
	   		setEvaluatorDialogDisplayed(false);
			
			// Close dialog
			RequestContext rq=RequestContext.getCurrentInstance();
			rq.execute("addAssessementAddressDialog.hide()");
		}
	}
	
    /**
     * @param event Action event
     */
	public void cancelAddEvaluator(ActionEvent event)
	{
   		setEvaluatorDialogDisplayed(false);
	}
	
	/**
	 * Deletes an evaluator.
     * @param event Action event
	 */
	public void removeEvaluator(ActionEvent event)
	{
		// Get current user session Hibernate operation
		Operation operation=getCurrentUserOperation(null);
		
		// We need to process some input fields
		processCommonDataInputFields(operation,event.getComponent());
		
		// Check common data input fields
		if (checkCommonDataInputFields(operation))
		{
			getEvaluators(operation).remove(event.getComponent().getAttributes().get("evaluator"));
		}
		else
		{
			// Scroll page to top position
			scrollToTop();
		}
	}
	
	/**
	 * Check that the evaluator to add is a valid email address, displaying an error message otherwise.
	 * @return true if the evaluator to add is a not used valid email address, false otherwise
	 */
	public boolean checkEvaluator()
	{
		boolean ok=true;
		if (getEvaluator()==null || "".equals(getEvaluator()))
		{
			ok=false;
			addErrorMessage("INCORRECT_OPERATION","TEST_ADDRESS_REQUIRED");
		}
		else if (!EmailValidator.validate(getEvaluator()))
		{
			ok=false;
			addErrorMessage("INCORRECT_OPERATION","TEST_ADDRESS_INVALID");
		}
		return ok;
	}
	
    /**
     * @param event Ajax event
     */
    public void changeFilterEvaluatorRangeNameLowerLimit(AjaxBehaviorEvent event)
    {
    	if (getFilterEvaluatorRangeNameLowerLimit().compareTo(getFilterEvaluatorRangeNameUpperLimit())>0)
    	{
    		setFilterEvaluatorRangeNameUpperLimit(getFilterEvaluatorRangeNameLowerLimit());
    	}
    }
	
    /**
     * @param event Ajax event
     */
    public void changeFilterEvaluatorRangeNameUpperLimit(AjaxBehaviorEvent event)
    {
    	if (getFilterEvaluatorRangeNameLowerLimit().compareTo(getFilterEvaluatorRangeNameUpperLimit())>0)
    	{
    		setFilterEvaluatorRangeNameLowerLimit(getFilterEvaluatorRangeNameUpperLimit());
    	}
    }
	
    /**
     * @param event Ajax event
     */
    public void changeFilterEvaluatorRangeSurnameLowerLimit(AjaxBehaviorEvent event)
    {
    	if (getFilterEvaluatorRangeSurnameLowerLimit().compareTo(getFilterEvaluatorRangeSurnameUpperLimit())>0)
    	{
    		setFilterEvaluatorRangeSurnameUpperLimit(getFilterEvaluatorRangeSurnameLowerLimit());
    	}
    }
	
    /**
     * @param event Ajax event
     */
    public void changeFilterEvaluatorRangeSurnameUpperLimit(AjaxBehaviorEvent event)
    {
    	if (getFilterEvaluatorRangeSurnameLowerLimit().compareTo(getFilterEvaluatorRangeSurnameUpperLimit())>0)
    	{
    		setFilterEvaluatorRangeSurnameLowerLimit(getFilterEvaluatorRangeSurnameUpperLimit());
    	}
    }
	
	/**
	 * Display a dialog to re-sort questions of a section. 
     * @param event Action event
	 */
	public void showReSortQuestions(ActionEvent event)
	{
    	String property=getPropertyChecked();
    	if (property==null)
    	{
    		// Get current user session Hibernate operation
    		Operation operation=getCurrentUserOperation(null);
    		
    		// Get section
        	AccordionPanel sectionsAccordion=getSectionsAccordion(operation,event.getComponent());
        	SectionBean section=getSectionFromSectionsAccordion(operation,sectionsAccordion);
    		
    		// We need to process some input fields
    		processSectionsInputFields(operation,event.getComponent(),section);
    		
    		// Set back accordion row index -1
    		sectionsAccordion.setRowIndex(-1);
    		
    		if (section!=null)
    		{
    			// Check that current section name entered by user is valid
    			if (checkSectionName(section.getName()))
    			{
    				activeSectionName=section.getName();
    				
        			setCurrentSection(section);
        			getCurrentSection().setQuestionOrdersSorting(null);
        			
        			RequestContext rq=RequestContext.getCurrentInstance();
        			rq.execute("resortQuestionsDialog.show()");
    			}
    			else
    			{
    				// Restore old section name
    				section.setName(activeSectionName);
    				
    				// Scroll page to top position
    				scrollToTop();
    			}
    		}
    	}
    	else if ("sectionWeight".equals(property))
    	{
			// We need to process weight
    		processSectionWeight(getCurrentUserOperation(null),event.getComponent(),sectionChecked);
    		
    		// We need to stop this response because there is another in process and we don't want to 
    		// interfere with it
    		FacesContext.getCurrentInstance().responseComplete();
    	}
    	else if ("randomQuantity".equals(property))
    	{
			// We need to process random quantity
    		processSectionRandomQuantity(getCurrentUserOperation(null),event.getComponent(),sectionChecked);
    		
    		// We need to stop this response because there is another in process and we don't want to 
    		// interfere with it
    		FacesContext.getCurrentInstance().responseComplete();
    	}
    	else if ("questionOrderWeight".equals(property))
    	{
    		// We need to process question orders weights
    		processQuestionOrderWeights(getCurrentUserOperation(null),event.getComponent(),sectionChecked);
    		
    		// We need to stop this response because there is another one in process and we don't want to 
    		// interfere with it
    		FacesContext.getCurrentInstance().responseComplete();
    	}
	}
	
    /**
     * Re-sort questions of a section in the same order as in the dialog.
     * @param event Action event
     */
	public void acceptReSortQuestions(ActionEvent event)
	{
		if (getCurrentSection()!=null)
		{
			List<QuestionOrderBean> questionOrdersSorting=getCurrentSection().getQuestionOrdersSorting();
			for (int questionOrderPos=1;questionOrderPos<=questionOrdersSorting.size();questionOrderPos++)
			{
				QuestionOrderBean questionOrder=questionOrdersSorting.get(questionOrderPos-1);
				questionOrder.setOrder(questionOrderPos);
			}
			getCurrentSection().setQuestionOrders(questionOrdersSorting);
			
			//We get current user session Hibernate operation
			Operation operation=getCurrentUserOperation(null);
			
			// We need to update sections text fields
			updateSectionsTextFields(operation,event.getComponent(),getSectionsSize(operation));
		}
	}
	
	/**
	 * @return true if button to re-sort questions of a section is enabled, false if it is disabled
	 * @param section Section
	 */
	public boolean isEnabledReSortQuestions(SectionBean section)
	{
		return section.getQuestionOrdersSize()>1;
	}
	
	/**
	 * @param operation Operation
	 * @param order Order
	 * @return Section with order received or null if there is no section with that order
	 */
	private SectionBean getSection(Operation operation,int order)
	{
		SectionBean section=null;
		for (SectionBean s:getSections(getCurrentUserOperation(operation)))
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
	 * @param operation Operation
	 * @param component Component that triggered the listener that called this method
	 * @return Sections accordion
	 */
	private AccordionPanel getSectionsAccordion(Operation operation,UIComponent component)
	{
		String sectionsAccordionId=null;
		if (getTestId(getCurrentUserOperation(operation))==0L)
		{
			sectionsAccordionId=":testForm:sectionsAccordion";
		}
		else
		{
			sectionsAccordionId=":testForm:testFormTabs:sectionsAccordion";
		}
		return (AccordionPanel)component.findComponent(sectionsAccordionId);
	}
	
	/**
	 * @param operation Operation
	 * @param sectionsAccordion Sections accordion component
	 * @return Section associated with the active tab on the sections accordion
	 */
	private SectionBean getSectionFromSectionsAccordion(Operation operation,AccordionPanel sectionsAccordion)
	{
		SectionBean section=null;
		if (sectionsAccordion!=null)
		{
			section=getSection(getCurrentUserOperation(operation),activeSectionIndex+1);
			sectionsAccordion.setRowIndex(activeSectionIndex);
		}
		return section;
	}
	
	/**
	 * Display a dialog to re-sort sections. 
     * @param event Action event
	 */
	public void showReSortSections(ActionEvent event)
	{
    	String property=getPropertyChecked();
    	if (property==null)
    	{
    		// Get current user session Hibernate operation
    		Operation operation=getCurrentUserOperation(null);
    		
    		// Get section to process if any
        	AccordionPanel sectionsAccordion=getSectionsAccordion(operation,event.getComponent());
        	SectionBean section=getSectionFromSectionsAccordion(operation,sectionsAccordion);
    		
    		// We need to process some input fields
    		processSectionsInputFields(operation,event.getComponent(),section);
    		
    		// Set back accordion row index -1
    		sectionsAccordion.setRowIndex(-1);
    		
			// Check that current section name entered by user is valid
			if (checkSectionName(section.getName()))
			{
				activeSectionName=section.getName();
				
	    		sectionsSorting=null;
	    		RequestContext rq=RequestContext.getCurrentInstance();
	    		rq.execute("resortSectionsDialog.show()");
			}
			else
			{
				// Restore old section name
				getCurrentSection().setName(activeSectionName);
				
				// Scroll page to top position
				scrollToTop();
			}
    	}
    	else if ("sectionWeight".equals(property))
    	{
			// We need to process weight
    		processSectionWeight(getCurrentUserOperation(null),event.getComponent(),sectionChecked);
    		
    		// We need to stop this response because there is another in process and we don't want to 
    		// interfere with it
    		FacesContext.getCurrentInstance().responseComplete();
    	}
    	else if ("randomQuantity".equals(property))
    	{
			// We need to process random quantity
    		processSectionRandomQuantity(getCurrentUserOperation(null),event.getComponent(),sectionChecked);
    		
    		// We need to stop this response because there is another in process and we don't want to 
    		// interfere with it
    		FacesContext.getCurrentInstance().responseComplete();
    	}
    	else if ("questionOrderWeight".equals(property))
    	{
    		// We need to process question orders weights
    		processQuestionOrderWeights(getCurrentUserOperation(null),event.getComponent(),sectionChecked);
    		
    		// We need to stop this response because there is another one in process and we don't want to 
    		// interfere with it
    		FacesContext.getCurrentInstance().responseComplete();
    	}
	}
	
    /**
     * Re-sort sections in the same order as in the dialog.
     * @param event Action event
     */
	public void acceptReSortSections(ActionEvent event)
	{
		// Get current user session Hibernate operation
		Operation operation=getCurrentUserOperation(null);
		
		List<SectionBean> sectionsSorting=getSectionsSorting(operation);
		for (int sectionPos=1;sectionPos<=sectionsSorting.size();sectionPos++)
		{
			SectionBean section=sectionsSorting.get(sectionPos-1);
			section.setOrder(sectionPos);
		}
		setSections(sectionsSorting);
		
		// We need to update sections text fields
		updateSectionsTextFields(operation,event.getComponent(),sectionsSorting.size());
	}
	
	/**
	 * @return true if button to re-sort sections is enabled, false if it is disabled
	 */
	public boolean isEnabledReSortSections()
	{
		return isEnabledReSortSections(null);
	}
	
	/**
	 * @param operation Operation
	 * @return true if button to re-sort sections is enabled, false if it is disabled
	 */
	private boolean isEnabledReSortSections(Operation operation)
	{
		return getSectionsSize(getCurrentUserOperation(operation))>1;
	}
	
	/**
	 * @param operation Operation
	 * @param component Component that triggered the listener that called this method
	 * @return Feedback accordion
	 */
	private AccordionPanel getFeedbackAccordion(Operation operation,UIComponent component)
	{
		String feedbackAccordionId=null;
		if (getTestId(getCurrentUserOperation(operation))==0L)
		{
			feedbackAccordionId=":testForm:feedbackAccordion";
		}
		else
		{
			feedbackAccordionId=":testForm:testFormTabs:feedbackAccordion";
		}
		return (AccordionPanel)component.findComponent(feedbackAccordionId);
	}
	
	/**
	 * Display a dialog to re-sort feedbacks. 
     * @param event Action event
	 */
	public void showReSortFeedbacks(ActionEvent event)
	{
		// We need to process some input fields
		processAdvancedFeedbacksFeedbackInputFields(getCurrentUserOperation(null),event.getComponent());
		
		feedbacksSorting=null;
		RequestContext rq=RequestContext.getCurrentInstance();
		rq.execute("resortFeedbacksDialog.show()");
	}
	
    /**
     * Re-sort feedbacks in the same order as in the dialog.
     * @param event Action event
     */
	public void acceptReSortFeedbacks(ActionEvent event)
	{
		for (int feedbackPos=1;feedbackPos<=feedbacksSorting.size();feedbackPos++)
		{
			TestFeedbackBean feedback=feedbacksSorting.get(feedbackPos-1);
			feedback.setPosition(feedbackPos);
		}
		feedbacks=feedbacksSorting;
	}
	
	/**
	 * @return true if button to re-sort feedbacks is enabled, false if it is disabled
	 */
	public boolean isEnabledReSortFeedbacks()
	{
		return isEnabledReSortFeedbacks(null);
	}
	
	/**
	 * @param operation Operation
	 * @return true if button to re-sort feedbacks is enabled, false if it is disabled
	 */
	private boolean isEnabledReSortFeedbacks(Operation operation)
	{
		return getFeedbacks(getCurrentUserOperation(operation)).size()>1;
	}
	
	/**
	 * Action listener to show the dialog for adding a new feedback.
	 * @param event Action event
	 */
	public void showAddFeedback(ActionEvent event)
	{
		// We need to process some input fields
		processAdvancedFeedbacksFeedbackInputFields(getCurrentUserOperation(null),event.getComponent());
		
		// New feedback with default values
		setCurrentFeedback(new TestFeedbackBean(this));
		RequestContext rq=RequestContext.getCurrentInstance();
		rq.execute("addFeedbackDialog.show()");
	}
	
	/**
	 * Action listener to show the dialog for updating a feedback.
	 * @param event Action event
	 */
	public void showEditFeedback(ActionEvent event)
	{
		// We need to process some input fields
		processAdvancedFeedbacksFeedbackInputFields(getCurrentUserOperation(null),event.getComponent());
		
		// Copy feedback so we work at dialog with a copy
		TestFeedbackBean feedback=(TestFeedbackBean)event.getComponent().getAttributes().get("feedback");
		setCurrentFeedback(new TestFeedbackBean(this,feedback.getPosition()));
		
		getCurrentFeedback().setHtmlContent(feedback.getHtmlContent());
		TestFeedbackConditionBean condition=feedback.getCondition();
		TestFeedbackConditionBean newCondition=new TestFeedbackConditionBean(this);
		newCondition.setSection(condition.getSection());
		newCondition.setUnit(condition.getUnit());
		newCondition.setComparator(condition.getComparator());
		newCondition.setConditionalCmp(condition.getConditionalCmp());
		newCondition.setConditionalBetweenMin(condition.getConditionalBetweenMin());
		newCondition.setConditionalBetweenMax(condition.getConditionalBetweenMax());
		getCurrentFeedback().setCondition(newCondition);
		RequestContext rq=RequestContext.getCurrentInstance();
		rq.execute("addFeedbackDialog.show()");
	}
	
	/**
	 * Check conditional value changing it to a valid value if it is invalid.
	 * @param operation Operation
	 * @return true if conditional value is valid, false otherwise
	 */
	private boolean checkAndChangeFeedbackConditionalCmp(Operation operation)
	{
		boolean ok=true;
		TestFeedbackConditionBean conditional=getCurrentFeedback().getCondition();
		int conditionalCmp=conditional.getConditionalCmp();
		int minValue=conditional.getMinValueConditionalCmp();
		int maxValue=conditional.getMaxValueConditionalCmp(getCurrentUserOperation(operation));
		if (conditionalCmp<minValue)
		{
			conditional.setConditionalCmp(minValue);
			ok=false;
		}
		else if (conditionalCmp>maxValue)
		{
			conditional.setConditionalCmp(maxValue);
			ok=false;
		}
		return ok;
	}
	
	/**
	 * Check conditional min value changing it to a valid value if it is invalid.
	 * @return true if conditional min value is valid, false otherwise
	 */
	private boolean checkAndChangeFeedbackConditionalBetweenMin()
	{
		boolean ok=true;
		TestFeedbackConditionBean conditional=getCurrentFeedback().getCondition();
		int conditionalBetweenMin=conditional.getConditionalBetweenMin();
		int conditionalBetweenMax=conditional.getConditionalBetweenMax();
		if (conditionalBetweenMin<0)
		{
			conditional.setConditionalBetweenMin(0);
			ok=false;
		}
		else if (conditionalBetweenMin>conditionalBetweenMax)
		{
			conditional.setConditionalBetweenMin(conditionalBetweenMax);
			ok=false;
		}
		return ok;
	}
	
	/**
	 * Check conditional max value changing it to a valid value if it is invalid.
	 * @param operation Operation
	 * @return true if conditional max value is valid, false otherwise
	 */
	private boolean checkAndChangeFeedbackConditionalBetweenMax(Operation operation)
	{
		boolean ok=true;
		TestFeedbackConditionBean conditional=getCurrentFeedback().getCondition();
		int conditionalBetweenMin=conditional.getConditionalBetweenMin();
		int conditionalBetweenMax=conditional.getConditionalBetweenMax();
		int maxConditionalValue=conditional.getMaxConditionalValue(getCurrentUserOperation(operation));
		if (conditionalBetweenMax<conditionalBetweenMin)
		{
			conditional.setConditionalBetweenMax(conditionalBetweenMin);
			ok=false;
		}
		else if (conditionalBetweenMax>maxConditionalValue)
		{
			conditional.setConditionalBetweenMax(maxConditionalValue);
			ok=false;
		}
		return ok;
	}
	
	/**
	 * Check feedback conditions values changing the invalid ones to valid.
	 * @param operation Operation
	 * @return true if all feedback conditions values are valid, false otherwise
	 */
	private boolean checkAndChangeFeedbackConditionsValues(Operation operation)
	{
		boolean ok=true;
		
		// Get current user session Hibernate operation
		operation=getCurrentUserOperation(operation);
		
		TestFeedbackConditionBean conditional=getCurrentFeedback().getCondition();
		if (NumberComparator.compareU(conditional.getComparator(),NumberComparator.BETWEEN))
		{
			if (!checkAndChangeFeedbackConditionalBetweenMin())
			{
				ok=false;
			}
			if (!checkAndChangeFeedbackConditionalBetweenMax(operation))
			{
				ok=false;
			}
		}
		else if (!checkAndChangeFeedbackConditionalCmp(operation))
		{
			ok=false;
		}
		return ok;
	}
	
    /**
     * Create/update a feedback with the data from the dialog.<br/><br/>
	 * Note that conditional values are checked and if any of them is invalid it is changed to a valid one and
	 * dialog is not closed.
     * @param event Action event
     */
	public void acceptAddFeedback(ActionEvent event)
	{
		if (checkAndChangeFeedbackConditionsValues(getCurrentUserOperation(null)))
		{
	    	TestFeedbackBean currentFeedback=getCurrentFeedback();
			if (currentFeedback.getPosition()>feedbacks.size())
			{
				feedbacks.add(currentFeedback);
			}
			else
			{
				TestFeedbackBean feedback=null;
				for (TestFeedbackBean f:feedbacks)
				{
					if (f.getPosition()==currentFeedback.getPosition())
					{
						feedback=f;
						break;
					}
				}
				if (feedback!=null)
				{
					feedback.setHtmlContent(currentFeedback.getHtmlContent());
					feedback.setCondition(currentFeedback.getCondition());
				}
			}
			RequestContext rq=RequestContext.getCurrentInstance();
			rq.execute("feedbackHtmlContent.saveHTML();addFeedbackDialog.hide();");
		}
	}
	
	/**
	 * Action listener to delete a feedback.
	 * @param event Action event
	 */
	public void removeFeedback(ActionEvent event)
	{
		// We need to process some input fields
		processAdvancedFeedbacksFeedbackInputFields(getCurrentUserOperation(null),event.getComponent());
		
		// Delete feedback
		TestFeedbackBean feedback=(TestFeedbackBean)event.getComponent().getAttributes().get("feedback");
		int position=feedback.getPosition();
		for (TestFeedbackBean f:feedbacks)
		{
			if (f.getPosition()>position)
			{
				f.setPosition(f.getPosition()-1);
			}
		}
		feedbacks.remove(feedback);
	}
	
	/**
	 * Ajax listener to check conditional value.
	 * @param event Ajax event
	 */
	public void changeFeedbackConditionalCmp(AjaxBehaviorEvent event)
	{
		checkAndChangeFeedbackConditionalCmp(getCurrentUserOperation(null));
	}
	
	/**
	 * Ajax listener to check conditional min value.
	 * @param event Ajax event
	 */
	public void changeFeedbackConditionalBetweenMin(AjaxBehaviorEvent event)
	{
		checkAndChangeFeedbackConditionalBetweenMin();
	}
	
	/**
	 * Ajax listener to check conditional max value.
	 * @param event Ajax event
	 */
	public void changeFeedbackConditionalBetweenMax(AjaxBehaviorEvent event)
	{
		checkAndChangeFeedbackConditionalBetweenMax(getCurrentUserOperation(null));
	}
	
	/**
	 * @return Title of dialog for adding/updating a feedback
	 */
	public String getAddEditFeedbackTitle()
	{
		String title="";
    	TestFeedbackBean currentFeedback=getCurrentFeedback();
		if (currentFeedback!=null)
		{
			if (currentFeedback.getPosition()>feedbacks.size())
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
	 * @return Title of dialog for adding/updating a support contact
	 */
	public String getAddEditSupportContactTitle()
	{
		String title="";
		SupportContactBean currentSupportContact=getCurrentSupportContact();
		if (currentSupportContact==null)
		{
			title=localizationService.getLocalizedMessage("ADD_TECH_SUPPORT_ADDRESS");
		}
		else
		{
			title=localizationService.getLocalizedMessage("EDIT_TECH_SUPPORT_ADDRESS");
		}
		return title;
	}
	
	/**
	 * @return Title of dialog for adding/updating an evaluator
	 */
	public String getAddEditEvaluatorTitle()
	{
		String title="";
		EvaluatorBean currentEvaluator=getCurrentEvaluator();
		if (currentEvaluator==null)
		{
			title=localizationService.getLocalizedMessage("ADD_ASSESSEMENT_ADDRESS");
		}
		else
		{
			title=localizationService.getLocalizedMessage("EDIT_ASSESSEMENT_ADDRESS");
		}
		return title;
	}
	
    /**
     * @param section Section
     * @return Section's name
     */
    public String getSectionName(SectionBean section)
    {
    	return getSectionName(null,section);
    }
    
    /**
     * @param operation Operation
     * @param section Section
     * @return Section's name
     */
    public String getSectionName(Operation operation,SectionBean section)
    {
    	StringBuffer sectionName=new StringBuffer();
    	sectionName.append(localizationService.getLocalizedMessage("SECTION"));
    	sectionName.append(' ');
    	sectionName.append(section.getOrder());
    	if ((section.getName()!=null && !section.getName().equals("") && 
    		checkSectionName(section.getName(),false)) || 
    		(section.getOrder()==activeSectionIndex+1 && activeSectionName!=null && 
    		!activeSectionName.equals("") && checkSectionName(activeSectionName,false)))
    	{
    		sectionName.append(": ");
    		sectionName.append(getNumberedSectionName(getCurrentUserOperation(operation),section));
    	}
    	return sectionName.toString();
    }
    
    /**
     * @param section Section
     * @return Section's name with a number appended if it is needed to distinguish sections with the same name
     */
    public String getNumberedSectionName(SectionBean section)
    {
    	return getNumberedSectionName(null,section);
    }
	
    /**
     * @param operation Operation
     * @param section Section
     * @return Section's name with a number appended if it is needed to distinguish sections with the same name
     */
    public String getNumberedSectionName(Operation operation,SectionBean section)
    {
    	StringBuffer sectionName=new StringBuffer();
    	if (section!=null)
    	{
    		boolean okSectionName=section.getName()!=null && !section.getName().equals("") && 
				checkSectionName(section.getName(),false);
    		boolean okActiveSectionName=!okSectionName && section.getOrder()==activeSectionIndex+1 && 
    			activeSectionName!=null && !activeSectionName.equals("") && 
    			checkSectionName(activeSectionName,false);
        	if (okSectionName || okActiveSectionName)
        	{
        		
        		sectionName.append(okSectionName?section.getName():activeSectionName);
        		int itNumber=1;
        		for (SectionBean s:getSections(getCurrentUserOperation(operation)))
        		{
        			if (s.getOrder()<section.getOrder() && section.getName().equals(s.getName()))
        			{
        				itNumber++;
        			}
        		}
        		if (itNumber>1)
        		{
        			sectionName.append('(');
        			sectionName.append(itNumber);
        			sectionName.append(')');
        		}
        	}
        	else
        	{
        		sectionName.append(localizationService.getLocalizedMessage("SECTION"));
        		sectionName.append(' ');
        		sectionName.append(section.getOrder());
        	}
    	}
    	return sectionName.toString();
    }
    
    public int getFeedbackDialogHeight()
    {
    	return getFeedbackDialogHeight(null);
    }
    
    private int getFeedbackDialogHeight(Operation operation)
    {
    	return getSectionsSize(getCurrentUserOperation(operation))>1?
    		FEEDBACKS_DIALOG_BASE_HEIGHT+FEEDBACKS_DIALOG_SECTIONS_COMBO_HEIGHT:FEEDBACKS_DIALOG_BASE_HEIGHT;
    }
    
    /**
     * @param category Category
     * @return Localized category name
     */
    public String getLocalizedCategoryName(Category category)
    {
    	return getLocalizedCategoryName(null,category.getId());
    }
    
    /**
     * @param categoryId Category identifier
     * @return Localized category name
     */
    public String getLocalizedCategoryName(long categoryId)
    {
    	return getLocalizedCategoryName(null,categoryId);
    }
    
    /**
     * @param operation Operation
     * @param categoryId Category identifier
     * @return Localized category name
     */
    private String getLocalizedCategoryName(Operation operation,long categoryId)
    {
    	return categoriesService.getLocalizedCategoryName(getCurrentUserOperation(operation),categoryId);
    }
    
    /**
     * @param category Category
     * @return Localized category long name
     */
    public String getLocalizedCategoryLongName(Category category)
    {
    	return getLocalizedCategoryLongName(null,category.getId());
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
     * @param category Category
	 * @param maxLength Maximum length
	 * @return Localized category long name, if length is greater than maximum length it will be abbreviated
     */
    public String getLocalizedCategoryLongName(Category category,int maxLength)
    {
    	return getLocalizedCategoryLongName(null,category.getId(),maxLength);
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
    	return categoriesService.getLocalizedCategoryLongName(getCurrentUserOperation(operation),categoryId,maxLength);
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
    		localizedCategoryFilterName=
    			localizationService.getLocalizedMessage(specialCategoryFiltersMap.get(categoryId).name);
    	}
    	else if (categoryId>0L)
    	{
    		localizedCategoryFilterName=getLocalizedCategoryLongName(operation,categoryId,maxLength);
    	}
    	return localizedCategoryFilterName;
    }
    
	/**
	 * Action listener to show the dialog to confirm cancel of test creation/edition.
	 * @param event Action event
	 */
	public void showConfirmCancelTestDialog(ActionEvent event)
	{
		setCancelTestTarget((String)event.getComponent().getAttributes().get("target"));
		RequestContext rq=RequestContext.getCurrentInstance();
		rq.execute("confirmCancelTestDialog.show()");
	}
	
	/**
	 * Cancel test creation/edition and navigate to next view.
	 * @return Next wiew
	 */
	public String cancelTest()
	{
		StringBuffer nextView=null;
		if ("logout".equals(getCancelTestTarget()))
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
		else if (getCancelTestTarget()!=null)
		{
			nextView=new StringBuffer(getCancelTestTarget());
			nextView.append("?faces-redirect=true");
		}
		return nextView==null?null:nextView.toString();
	}
    
	/**
	 * Display a question in OM Test Navigator web application.
	 * @param questionId Question's identifier
	 * @return Next view
	 * @throws Exception
	 */
	public String viewOM(long questionId) throws Exception
	{
		// Get current user session Hibernate operation
		Operation operation=getCurrentUserOperation(null);
		
		// Get question
		Question question=questionsService.getQuestion(operation,questionId);
		
		setViewOMEnabled(null);
		setViewQuestionsFromOtherUsersPrivateCategoriesEnabled(null);
		setViewQuestionsFromAdminsPrivateCategoriesEnabled(null);
		setViewQuestionsFromSuperadminsPrivateCategoriesEnabled(null);
		resetViewOmQuestionEnabled(question);
		resetAdminFromQuestionAllowed(question);
		resetSuperadminFromQuestionAllowed(question);
		if (!isViewOMQuestionEnabled(operation,question))
		{
			question=null;
		}
		if (question==null)
		{
			addErrorMessage("NON_AUTHORIZED_ACTION_ERROR");
    		
    		resetViewOMQuestionsEnabled();
    		resetAdmins();
			resetSuperadmins();
			setGlobalOtherUserCategoryAllowed(null);
			setViewTestsFromOtherUsersPrivateCategoriesEnabled(null);
			setViewTestsFromAdminsPrivateCategoriesEnabled(null);
			setViewTestsFromSuperadminsPrivateCategoriesEnabled(null);
			setUseGlobalQuestions(null);
			setUseOtherUsersQuestions(null);
			setTestAuthorViewQuestionsFromOtherUsersPrivateCategoriesEnabled(null);
			setTestAuthorViewQuestionsFromAdminsPrivateCategoriesEnabled(null);
			setTestAuthorViewQuestionsFromSuperadminsPrivateCategoriesEnabled(null);
			
    		RequestContext requestContext=RequestContext.getCurrentInstance();
			requestContext.addCallbackParam("url","error");
		}
		else
		{
			// Get package's name and destination path
			String packageName=question.getPackage();
			String path=configurationService.getOmQuestionsPath();
			
			// Get OM Develover, OM Test Navigator and OM Question Engine URLs
			String omURL=configurationService.getOmUrl();
			String omTnURL=configurationService.getOmTnUrl();
			String omQeURL=configurationService.getOmQeUrl();
			
			// Check if we need to build and/or deploy question jar
			boolean buildQuestion=true;
			boolean deployQuestion=true;
			long lastTimeModified=question.getTimemodified()==null?-1:question.getTimemodified().getTime();
			long lastTimeDeploy=question.getTimedeploy()==null?-1:question.getTimedeploy().getTime();
			long lastTimeBuild=question.getTimebuild()==null?-1:question.getTimebuild().getTime();
			if (lastTimeDeploy!=-1 && lastTimeDeploy>lastTimeBuild && lastTimeDeploy>lastTimeModified)
			{
				try
				{
					TestGenerator.checkQuestionJar(packageName,"1.0",omTnURL);
					deployQuestion=lastTimeDeploy!=TestGenerator.getQuestionJarLastModifiedDate(
						packageName,"1.0",omTnURL).getTime();
				}
				catch (Exception e)
				{
				}
			}
			if (deployQuestion)
			{
				if (lastTimeBuild!=-1 && lastTimeBuild>lastTimeModified)
				{
					try
					{
						QuestionGenerator.checkQuestionJar(packageName,omURL);
						buildQuestion=lastTimeBuild!=
							QuestionGenerator.getQuestionJarLastModifiedDate(packageName,omURL).getTime();
					}
					catch (Exception e)
					{
					}
				}
			}
			else
			{
				buildQuestion=false;
			}
			
			// Build question if needed
			if (buildQuestion)
			{
				// First we need to copy resources needed by question
				OmHelper.copyResources(question,configurationService.getResourcesPath(),path);
				
				// Generate question files 
				QuestionGenerator.generateQuestion(question,path);
				
				// Create question on OM Developer Web Application
				QuestionGenerator.createQuestion(packageName,path,new ArrayList<String>(),omURL);
				
				// Build question on OM Developer Web Application
				QuestionGenerator.buildQuestion(packageName,omURL);
				
				// Update build time on question
				question.setTimebuild(QuestionGenerator.getQuestionJarLastModifiedDate(packageName,omURL));
			}
			
			// Deploy question on OM Test Navigator Web Application
			QuestionGenerator.deployQuestion(question,omURL,omTnURL,deployQuestion,true);
			if (deployQuestion)
			{
				// Update deploy time on question
				question.setTimedeploy(TestGenerator.getQuestionJarLastModifiedDate(packageName,"1.0",omTnURL));
				
				// Stop all Test Navigator sessions using that question
				QuestionGenerator.stopAllSessionsForQuestion(packageName,omTnURL);
				
				// Delete cached question from OM Question Engine Web Application
				QuestionGenerator.deleteJarFileFromQuestionEngineCache(packageName,omQeURL);
			}
			
			// Save question if we need to update build and/or deploy time
			if (buildQuestion || deployQuestion)
			{
				try
				{
					questionsService.updateQuestion(question);
				}
				finally
				{
					// End current user session Hibernate operation
					userSessionService.endCurrentUserOperation();
				}
			}
			
			// Add callback parameters to display question
			StringBuffer url=new StringBuffer(omTnURL);
			if (omTnURL.charAt(omTnURL.length()-1)!='/')
			{
				url.append('/');
			}
			RequestContext requestContext=RequestContext.getCurrentInstance();
			requestContext.addCallbackParam("url",url.toString());
			requestContext.addCallbackParam("packageName",packageName);
		}
	    return null;
	}
	
	/**
	 * Displays an error message.
	 * @param message Error message (before localization)
	 */
	private void addErrorMessage(String message)
	{
		addErrorMessage(message,null);
	}
	
	/**
	 * Displays an error message.
	 * @param title Error title (before localization)
	 * @param message Error message (before localization)
	 */
	private void addErrorMessage(String title,String message)
	{
		FacesContext context=FacesContext.getCurrentInstance();
		context.addMessage(null,new FacesMessage(FacesMessage.SEVERITY_ERROR,
			localizationService.getLocalizedMessage(title),
			message==null?null:localizationService.getLocalizedMessage(message)));
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
	 * Remove global categories from other users (different to the indicated user) and optionally excluding 
	 * from removing the indicated category.
	 * @param categories List of categories
	 * @param user User (his/her global categories will not be removed)
	 * @param categoryExcludedFromRemoving Category the will not be removed
	 */
	private void removeGlobalOtherUserCategories(List<Category> categories,User user,
		Category categoryExcludedFromRemoving)
	{
		List<Category> categoriesToRemove=new ArrayList<Category>();
		for (Category category:categories)
		{
			if (!category.equals(categoryExcludedFromRemoving) && category.getVisibility().isGlobal() && 
				!category.getUser().equals(user))
			{
				categoriesToRemove.add(category);
			}
		}
		for (Category categoryToRemove:categoriesToRemove)
		{
			categories.remove(categoryToRemove);
		}
	}
	
	/**
	 * Remove private categories from an user and optionally excluding from removing the indicated category.
	 * @param operation Operation
	 * @param categories List of categories
	 * @param user User
	 * @param categoryExcludedFromRemoving Category the will not be removed
	 */
	private void removePrivateCategories(Operation operation,List<Category> categories,User user,
		Category categoryExcludedFromRemoving)
	{
		Visibility privateVisibility=
			visibilitiesService.getVisibility(getCurrentUserOperation(operation),"CATEGORY_VISIBILITY_PRIVATE");
		List<Category> categoriesToRemove=new ArrayList<Category>();
		for (Category category:categories)
		{
			if (!category.equals(categoryExcludedFromRemoving) && !category.getVisibility().isGlobal() &&
				category.getVisibility().getLevel()>=privateVisibility.getLevel())
			{
				categoriesToRemove.add(category);
			}
		}
		for (Category categoryToRemove:categoriesToRemove)
		{
			categories.remove(categoryToRemove);
		}
	}
	
	/**
	 * Handles a change of the current locale.<br/><br/>
	 * This implementation disable setters because of a bug of &lt;p:wizard&gt; component that always set 
	 * properties for their checkboxes components as <i>false</i> when submitting the form.
	 * Moreover this implementation also localize the item 'All' of the 'Category' combo in the filter's panel 
	 * of the 'Add questions' dialog because submitting form is not enough to localize it.
	 * @param event Action event
	 */
	public void changeLocale(ActionEvent event)
	{
		// Get current user session Hibernate operation
		Operation operation=getCurrentUserOperation(null);
		
		if (getTestId(operation)==0L)
		{
			setEnabledCheckboxesSetters(false);
		}
		FacesContext context=FacesContext.getCurrentInstance();
		LocaleBean localeBean=
			(LocaleBean)context.getApplication().getELResolver().getValue(context.getELContext(),null,"localeBean");
		
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
			List<Category> specialCategoriesFilters=getSpecialCategoriesFilters(operation);
			Category allOptionsCategory=
				specialCategoriesFilters.get(specialCategoriesFilters.indexOf(allOptionsCategoryAux));
			allOptionsCategory.setName(allOptionsForCategory);
		}
	}
}
