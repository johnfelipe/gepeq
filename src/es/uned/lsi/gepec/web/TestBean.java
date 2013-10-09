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

import java.io.IOException;
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
import java.util.regex.Pattern;

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
import es.uned.lsi.gepec.util.HibernateUtil;
import es.uned.lsi.gepec.util.StringUtils;
import es.uned.lsi.gepec.util.HibernateUtil.Operation;
import es.uned.lsi.gepec.web.backbeans.EvaluatorBean;
import es.uned.lsi.gepec.web.backbeans.QuestionOrderBean;
import es.uned.lsi.gepec.web.backbeans.SectionBean;
import es.uned.lsi.gepec.web.backbeans.SupportContactBean;
import es.uned.lsi.gepec.web.backbeans.TestFeedbackBean;
import es.uned.lsi.gepec.web.backbeans.TestFeedbackConditionBean;
import es.uned.lsi.gepec.web.backbeans.UserGroupBean;
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
	
	private final static long OLD_FEEDBACK_DIALOG_VALUES_DELAY=300;
	
	private final static SpecialCategoryFilter ALL_EVEN_PRIVATE_CATEGORIES;
	static
	{
		List<String> allEvenPrivateCategoriesPermissions=new ArrayList<String>();
		allEvenPrivateCategoriesPermissions.add("PERMISSION_QUESTIONS_GLOBAL_FILTER_ENABLED");
		allEvenPrivateCategoriesPermissions.add("PERMISSION_QUESTIONS_OTHER_USERS_FILTER_ENABLED");
		allEvenPrivateCategoriesPermissions.add(
			"PERMISSION_QUESTIONS_VIEW_QUESTIONS_OF_OTHER_USERS_PRIVATE_CATEGORIES_ENABLED");
		List<String> allEvenPrivateCategoriesAuthorPermissions=new ArrayList<String>();
		allEvenPrivateCategoriesAuthorPermissions.add("PERMISSION_TEST_USE_GLOBAL_QUESTIONS");
		allEvenPrivateCategoriesAuthorPermissions.add("PERMISSION_TEST_USE_OTHER_USERS_QUESTIONS");
		ALL_EVEN_PRIVATE_CATEGORIES=new SpecialCategoryFilter(-1L,"ALL_EVEN_PRIVATE_CATEGORIES",
			allEvenPrivateCategoriesPermissions,allEvenPrivateCategoriesAuthorPermissions);
	}
	
	private final static SpecialCategoryFilter ALL_MY_CATEGORIES;
	static
	{
		List<String> allMyCategoriesPermissions=new ArrayList<String>();
		allMyCategoriesPermissions.add("PERMISSION_QUESTIONS_GLOBAL_FILTER_ENABLED");
		List<String> allMyCategoriesAuthorPermissions=new ArrayList<String>();
		allMyCategoriesAuthorPermissions.add("PERMISSION_TEST_USE_GLOBAL_QUESTIONS");
		allMyCategoriesAuthorPermissions.add("PERMISSION_TEST_USE_OTHER_USERS_QUESTIONS");
		ALL_MY_CATEGORIES=new SpecialCategoryFilter(
			-2L,"ALL_MY_CATEGORIES",allMyCategoriesPermissions,allMyCategoriesAuthorPermissions);
	}
	
	private final static SpecialCategoryFilter ALL_MY_CATEGORIES_FOR_TEST_AUTHOR;
	static
	{
		List<String> allMyCategoriesForTestAuthorPermissions=new ArrayList<String>();
		allMyCategoriesForTestAuthorPermissions.add("PERMISSION_QUESTIONS_GLOBAL_FILTER_ENABLED");
		List<String> allMyCategoriesForTestAuthorAuthorPermissions=new ArrayList<String>();
		allMyCategoriesForTestAuthorAuthorPermissions.add("PERMISSION_TEST_USE_GLOBAL_QUESTIONS");
		ALL_MY_CATEGORIES_FOR_TEST_AUTHOR=new SpecialCategoryFilter(-2L,"ALL_MY_CATEGORIES",
			allMyCategoriesForTestAuthorPermissions,allMyCategoriesForTestAuthorAuthorPermissions);
	}
	
	private final static SpecialCategoryFilter ALL_MY_CATEGORIES_EXCEPT_GLOBALS;
	static
	{
		List<String> allMyCategoriesExceptGlobalsAuthorPermissions=new ArrayList<String>();
		allMyCategoriesExceptGlobalsAuthorPermissions.add("PERMISSION_QUESTION_USE_OTHER_USERS_RESOURCES");
		ALL_MY_CATEGORIES_EXCEPT_GLOBALS=new SpecialCategoryFilter(-3L,"ALL_MY_CATEGORIES_EXCEPT_GLOBALS",
			new ArrayList<String>(),allMyCategoriesExceptGlobalsAuthorPermissions);
	}
	
	private final static SpecialCategoryFilter ALL_MY_CATEGORIES_EXCEPT_GLOBALS_FOR_TEST_AUTHOR=
		new SpecialCategoryFilter(-3L,"ALL_MY_CATEGORIES_EXCEPT_GLOBALS",new ArrayList<String>(),
		new ArrayList<String>());
	
	private final static SpecialCategoryFilter ALL_TEST_AUTHOR_CATEGORIES;
	static
	{
		List<String> allTestAuthorCategoriesPermissions=new ArrayList<String>();
		allTestAuthorCategoriesPermissions.add("PERMISSION_QUESTIONS_GLOBAL_FILTER_ENABLED");
		allTestAuthorCategoriesPermissions.add("PERMISSION_QUESTIONS_OTHER_USERS_FILTER_ENABLED");
		List<String> allTestAuthorCategoriesAuthorPermissions=new ArrayList<String>();
		allTestAuthorCategoriesAuthorPermissions.add("PERMISSION_TEST_USE_GLOBAL_QUESTIONS");
		ALL_TEST_AUTHOR_CATEGORIES=new SpecialCategoryFilter(
			-4L,"ALL_CATEGORIES_OF",allTestAuthorCategoriesPermissions,allTestAuthorCategoriesAuthorPermissions);
	}
	
	private final static SpecialCategoryFilter ALL_TEST_AUTHOR_CATEGORIES_EXCEPT_GLOBALS;
	static
	{
		List<String> allTestAuthorCategoriesExceptGlobalsPermissions=new ArrayList<String>();
		allTestAuthorCategoriesExceptGlobalsPermissions.add("PERMISSION_QUESTIONS_OTHER_USERS_FILTER_ENABLED");
		ALL_TEST_AUTHOR_CATEGORIES_EXCEPT_GLOBALS=new SpecialCategoryFilter(-5L,
			"ALL_CATEGORIES_OF_EXCEPT_GLOBALS",allTestAuthorCategoriesExceptGlobalsPermissions,
			new ArrayList<String>());
	}
	
	private final static SpecialCategoryFilter ALL_GLOBAL_CATEGORIES;
	static
	{
		List<String> allGlobalCategoriesPermissions=new ArrayList<String>();
		allGlobalCategoriesPermissions.add("PERMISSION_QUESTIONS_GLOBAL_FILTER_ENABLED");
		List<String> allGlobalCategoriesAuthorPermissions=new ArrayList<String>();
		allGlobalCategoriesAuthorPermissions.add("PERMISSION_TEST_USE_GLOBAL_QUESTIONS");
		ALL_GLOBAL_CATEGORIES=new SpecialCategoryFilter(
			-6L,"ALL_GLOBAL_CATEGORIES",allGlobalCategoriesPermissions,allGlobalCategoriesAuthorPermissions);
	}
	
	private final static SpecialCategoryFilter ALL_PUBLIC_CATEGORIES_OF_OTHER_USERS; 
	static
	{
		List<String> allPublicCategoriesOfOtherUsersPermissions=new ArrayList<String>();
		allPublicCategoriesOfOtherUsersPermissions.add("PERMISSION_QUESTIONS_OTHER_USERS_FILTER_ENABLED");
		List<String> allPublicCategoriesOfOtherUsersAuthorPermissions=new ArrayList<String>();
		allPublicCategoriesOfOtherUsersAuthorPermissions.add("PERMISSION_TEST_USE_OTHER_USERS_QUESTIONS");
		ALL_PUBLIC_CATEGORIES_OF_OTHER_USERS=new SpecialCategoryFilter(-7L,"ALL_PUBLIC_CATEGORIES_OF_OTHER_USERS",
			allPublicCategoriesOfOtherUsersPermissions,allPublicCategoriesOfOtherUsersAuthorPermissions);
	}
	
	private final static SpecialCategoryFilter ALL_PRIVATE_CATEGORIES_OF_OTHER_USERS;
	static
	{
		List<String> allPrivateCategoriesOfOtherUsersPermissions=new ArrayList<String>();
		allPrivateCategoriesOfOtherUsersPermissions.add("PERMISSION_QUESTIONS_OTHER_USERS_FILTER_ENABLED");
		allPrivateCategoriesOfOtherUsersPermissions.add(
			"PERMISSION_QUESTIONS_VIEW_QUESTIONS_OF_OTHER_USERS_PRIVATE_CATEGORIES_ENABLED");
		List<String> allPrivateCategoriesOfOtherUsersAuthorPermissions=new ArrayList<String>();
		allPrivateCategoriesOfOtherUsersAuthorPermissions.add("PERMISSION_TEST_USE_OTHER_USERS_QUESTIONS");
		ALL_PRIVATE_CATEGORIES_OF_OTHER_USERS=new SpecialCategoryFilter(-8L,
			"ALL_PRIVATE_CATEGORIES_OF_OTHER_USERS",allPrivateCategoriesOfOtherUsersPermissions,
			allPrivateCategoriesOfOtherUsersAuthorPermissions);
	}
	
	private final static SpecialCategoryFilter ALL_CATEGORIES_OF_OTHER_USERS;
	static
	{
		List<String> allCategoriesOfOtherUsersPermissions=new ArrayList<String>();
		allCategoriesOfOtherUsersPermissions.add("PERMISSION_QUESTIONS_OTHER_USERS_FILTER_ENABLED");
		allCategoriesOfOtherUsersPermissions.add(
			"PERMISSION_QUESTIONS_VIEW_QUESTIONS_OF_OTHER_USERS_PRIVATE_CATEGORIES_ENABLED");
		List<String> allCategoriesOfOtherUsersAuthorPermissions=new ArrayList<String>();
		allCategoriesOfOtherUsersAuthorPermissions.add("PERMISSION_TEST_USE_OTHER_USERS_QUESTIONS");
		ALL_CATEGORIES_OF_OTHER_USERS=new SpecialCategoryFilter(-9L,"ALL_CATEGORIES_OF_OTHER_USERS",
			allCategoriesOfOtherUsersPermissions,allCategoriesOfOtherUsersAuthorPermissions);
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
	private long initialCategoryId;
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
	private Map<Long,SpecialCategoryFilter> specialCategoryFiltersMap;
	private long filterCategoryId;
	private boolean filterIncludeSubcategories;
	private String filterQuestionType;
	private String filterQuestionLevel;
	private List<Question> questions;
	private DualListModel<Question> questionsDualList;
	private List<User> filteredUsersForAddingUsers;
	private List<User> filteredUsersForAddingAdmins;
	private Map<String,List<User>> groupUsersMap;
	private List<User> filteredUsersForAddingSupportContactFilterUsers;
	private List<User> filteredUsersForAddingEvaluatorFilterUsers;
	private List<UserType> userTypes;
	private long filterUsersUserTypeId;
	private boolean filterUsersIncludeOmUsers;
	private List<UserGroupBean> testUsersGroups;
	private DualListModel<User> usersDualList;
	private String userGroup;
	private boolean userGroupsDialogDisplayed;
	private String availableUserGroupsHidden;
	private String userGroupsToAddHidden;
	private DualListModel<String> userGroupsDualList;
	private long filterAdminsUserTypeId;
	private boolean filterAdminsIncludeOmUsers;
	private List<UserGroupBean> testAdminsGroups;
	private DualListModel<User> adminsDualList;
	private String adminGroup;
	private boolean adminGroupsDialogDisplayed;
	private String availableAdminGroupsHidden;
	private String adminGroupsToAddHidden;
	private DualListModel<String> adminGroupsDualList;
	private String supportContactFilterType;
	private String supportContactFilterSubtype;
	private List<String> supportContactFilterSubtypes;
	private long filterSupportContactFilterUsersUserTypeId;
	private boolean filterSupportContactFilterUsersIncludeOmUsers;
	private List<User> testSupportContactFilterUsers;
	private String supportContactFilterUsersIdsHidden;
	private DualListModel<User> supportContactFilterUsersDualList;
	private String supportContactGroup;
	private List<String> availableSupportContactFilterGroups;
	private List<String> testSupportContactFilterGroups;
	private String availableSupportContactFilterGroupsHidden;
	private String supportContactFilterGroupsHidden;
	private DualListModel<String> supportContactFilterGroupsDualList;
	
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
	private String evaluatorGroup;
	private List<String> availableEvaluatorFilterGroups;
	private List<String> testEvaluatorFilterGroups;
	private String availableEvaluatorFilterGroupsHidden;
	private String evaluatorFilterGroupsHidden;
	private DualListModel<String> evaluatorFilterGroupsDualList;
	
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
	private int activeTestTabIndex;
	
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
	
	// Feedbacks
	private int activeFeedbackTabIndex;
	int oldConditionalCmp;
	int oldConditionalBetweenMin;
	int oldConditionalBetweenMax;
	long oldAddFeedbackDialogTimestamp;
	
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
	
	private Boolean filterGlobalTestsEnabled;
	private Boolean filterOtherUsersTestsEnabled;
	private Boolean globalOtherUserCategoryAllowed;
	private Boolean addEnabled;
	private Boolean editEnabled;
	private Boolean editOtherUsersTestsEnabled;
	private Boolean editAdminsTestsEnabled;
	private Boolean editSuperadminsTestsEnabled;
	private Boolean viewTestsFromOtherUsersPrivateCategoriesEnabled;
	private Boolean viewTestsFromAdminsPrivateCategoriesEnabled;
	private Boolean viewTestsFromSuperadminsPrivateCategoriesEnabled;
	
	private Boolean filterGlobalQuestionsEnabled;
	private Boolean filterOtherUsersQuestionsEnabled;
	private Boolean useGlobalQuestions;
	private Boolean useOtherUsersQuestions;
	private Boolean viewQuestionsFromOtherUsersPrivateCategoriesEnabled;
	private Boolean viewQuestionsFromAdminsPrivateCategoriesEnabled;
	private Boolean viewQuestionsFromSuperadminsPrivateCategoriesEnabled;
	
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
		initialCategoryId=0L;
		filterCategoryId=Long.MIN_VALUE;
		filterIncludeSubcategories=false;
		filterQuestionType="";
		filterQuestionLevel="";
		questionsDualList=null;
		filteredUsersForAddingUsers=null;
		filteredUsersForAddingAdmins=null;
		groupUsersMap=null;
		filteredUsersForAddingSupportContactFilterUsers=null;
		filteredUsersForAddingEvaluatorFilterUsers=null;
		userTypes=null;
		filterUsersUserTypeId=0L;
		filterUsersIncludeOmUsers=true;
		testUsersGroups=null;
		usersDualList=null;
		userGroup=null;
		userGroupsDialogDisplayed=false;
		availableUserGroupsHidden="";
		userGroupsToAddHidden="";
		userGroupsDualList=null;
		filterAdminsUserTypeId=0L;
		filterAdminsIncludeOmUsers=true;
		testAdminsGroups=null;
		adminsDualList=null;
		adminGroup=null;
		adminGroupsDialogDisplayed=false;
		availableAdminGroupsHidden="";
		adminGroupsToAddHidden="";
		adminGroupsDualList=null;
		
		activeTestTabName=GENERAL_WIZARD_TAB;
		activeTestTabIndex=GENERAL_TABVIEW_TAB;
		nextTestTabNameOnChangePropertyConfirm=null;
		nextTestIndexOnChangePropertyConfirm=-1;
		oldScoreType=null;
		activeGeneralTabIndex=0;
		activeSectionIndex=0;
		activeSectionName="";
		activeFeedbackTabIndex=SUMMARY_TAB_FEEDBACK_ACCORDION;
		oldConditionalCmp=-1;
		oldConditionalBetweenMin=-1;
		oldConditionalBetweenMax=-1;
		oldAddFeedbackDialogTimestamp=-1L;
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
		filterGlobalTestsEnabled=null;
		filterOtherUsersTestsEnabled=null;
		globalOtherUserCategoryAllowed=null;
		globalOtherUserCategoryAllowed=null;
		addEnabled=null;
		editEnabled=null;
		editOtherUsersTestsEnabled=null;
		editAdminsTestsEnabled=null;
		editSuperadminsTestsEnabled=null;
		viewTestsFromOtherUsersPrivateCategoriesEnabled=null;
		viewTestsFromAdminsPrivateCategoriesEnabled=null;
		viewTestsFromSuperadminsPrivateCategoriesEnabled=null;
		filterGlobalQuestionsEnabled=null;
		filterOtherUsersQuestionsEnabled=null;
		useGlobalQuestions=null;
		useOtherUsersQuestions=null;
		viewQuestionsFromOtherUsersPrivateCategoriesEnabled=null;
		viewQuestionsFromAdminsPrivateCategoriesEnabled=null;
		viewQuestionsFromSuperadminsPrivateCategoriesEnabled=null;
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
		supportContactGroup=null;
		availableSupportContactFilterGroups=null;
		testSupportContactFilterGroups=new ArrayList<String>();
		availableSupportContactFilterGroupsHidden="";
		supportContactFilterGroupsHidden="";
		supportContactFilterGroupsDualList=null;
		evaluatorFilterType="NO_FILTER";
		evaluatorFilterSubtype=null;
		evaluatorFilterSubtypes=null;
		filterEvaluatorFilterUsersUserTypeId=0L;
		filterEvaluatorFilterUsersIncludeOmUsers=true;
		testEvaluatorFilterUsers=new ArrayList<User>();
		evaluatorFilterUsersIdsHidden="";
		evaluatorFilterUsersDualList=null;
		evaluatorGroup=null;
		availableEvaluatorFilterGroups=null;
		testEvaluatorFilterGroups=new ArrayList<String>();
		availableEvaluatorFilterGroupsHidden="";
		evaluatorFilterGroupsHidden="";
		evaluatorFilterGroupsDualList=null;
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
    	if (!testInitialized)
    	{
    		initializeTest();
    		operation=null;
    	}
    	return operation==null?userSessionService.getCurrentUserOperation():operation;
    }
    
    public long getTestId()
    {
    	if (!testInitialized)
    	{
    		initializeTest();
    	}
    	return testId;
    }
    
    public void setTestId(long testId)
    {
    	this.testId=testId;
    }
    
	public String getName()
	{
    	if (!testInitialized)
    	{
    		initializeTest();
    	}
		return name;
	}
	
	public void setName(String name)
	{
		this.name=name;
	}
	
	public User getAuthor()
	{
		if (!testInitialized)
		{
			initializeTest();
		}
		return author;
	}
	
	public void setAuthor(User author)
	{
		this.author=author;
	}
	
	public Date getTimeCreated()
	{
		if (!testInitialized)
		{
			initializeTest();
		}
		return timeCreated;
	}
	
	public void setTimeCreated(Date timeCreated)
	{
		this.timeCreated=timeCreated;
	}
	
	public Date getTimeTestDeploy()
	{
		if (!testInitialized)
		{
			initializeTest();
		}
		return timeTestDeploy;
	}
	
	public void setTimeTestDeploy(Date timeTestDeploy)
	{
		this.timeTestDeploy=timeTestDeploy;
	}
	
	public Date getTimeDeployDeploy()
	{
		if (!testInitialized)
		{
			initializeTest();
		}
		return timeDeployDeploy;
	}
	
	public void setTimeDeployDeploy(Date timeDeployDeploy)
	{
		this.timeDeployDeploy=timeDeployDeploy;
	}
	
	public Category getCategory()
	{
		if (!testInitialized)
		{
			initializeTest();
		}
		return category;
	}
	
	public void setCategory(Category category)
	{
		this.category=category;
	}
	
	public long getCategoryId()
	{
		Category category=getCategory();
		return category==null?0L:category.getId();
	}
	
	public void setCategoryId(long categoryId)
	{
		setCategoryId(null,categoryId);
	}
	
	private void setCategoryId(Operation operation,long categoryId)
	{
		setCategory(
			categoryId>0L?categoriesService.getCategory(getCurrentUserOperation(operation),categoryId):null);
	}
	
	public String getDescription()
	{
    	if (!testInitialized)
    	{
    		initializeTest();
    	}
		return description;
	}
	
	public void setDescription(String description)
	{
		this.description=description;
	}
	
	public String getTitle()
	{
    	if (!testInitialized)
    	{
    		initializeTest();
    	}
		return title;
	}
	
	public void setTitle(String title)
	{
		this.title=title;
	}
	
	public Assessement getAssessement()
	{
		if (!testInitialized)
		{
			initializeTest();
		}
		return assessement;
	}
	
	public void setAssessement(Assessement assessement)
	{
		this.assessement=assessement;
	}
	
	public long getAssessementId()
	{
		Assessement assessement=getAssessement();
		return assessement==null?0L:assessement.getId();
	}
	
	public void setAssessementId(long assessementId)
	{
		setAssessementId(null,assessementId);
	}
	
	private void setAssessementId(Operation operation,long assessementId)
	{
		Assessement assessement=getAssessement();
		if (assessement==null || assessement.getId()!=assessementId)
		{
			setAssessement(assessementsService.getAssessement(getCurrentUserOperation(operation),assessementId));
		}
	}
	
	public String getAssessementTip()
	{
		String assessementTip=null;
		Assessement assessement=getAssessement();
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
		if (!testInitialized)
		{
			initializeTest();
		}
		return scoreType;
	}
	
	public void setScoreType(ScoreType scoreType)
	{
		ScoreType thisScoreType=getScoreType();
		// Needed this check because sometimes setter is called several times with the same value
		if ((scoreType==null && thisScoreType==null) || (scoreType!=null && !scoreType.equals(thisScoreType)))
		{
			oldScoreType=thisScoreType;
		}
		this.scoreType=scoreType;
	}
	
	public void rollbackScoreType()
	{
		scoreType=oldScoreType;
	}
	
	public long getScoreTypeId()
	{
		ScoreType scoreType=getScoreType();
		return scoreType==null?0L:scoreType.getId();
	}
	
	public void setScoreTypeId(long scoreTypeId)
	{
		setScoreTypeId(null,scoreTypeId);
	}
	
	private void setScoreTypeId(Operation operation,long scoreTypeId)
	{
		// Get current user Hibernate operation
		operation=getCurrentUserOperation(operation);
		
		ScoreType scoreType=getScoreType();
		if (scoreType==null || scoreType.getId()!=scoreTypeId)
		{
			setScoreType(scoreTypesService.getScoreType(operation,scoreTypeId));
		}
	}
	
	public String getScoreTypeTip()
	{
		String scoreTypeTip=null;
		ScoreType scoreType=getScoreType();
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
		if (!testInitialized)
		{
			initializeTest();
		}
		return allUsersAllowed;
	}
	
	public void setAllUsersAllowed(boolean allUsersAllowed)
	{
		if (isEnabledChecboxesSetters())
		{
			this.allUsersAllowed=allUsersAllowed;
		}
	}
	
	public boolean isAllowAdminReports()
	{
		if (!testInitialized)
		{
			initializeTest();
		}
		return allowAdminReports;
	}
	
	public void setAllowAdminReports(boolean allowAdminReports)
	{
		this.allowAdminReports=allowAdminReports;
	}
	
	public Date getStartDate()
	{
		if (!testInitialized)
		{
			initializeTest();
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
	
	public void setStartDate(Date startDate)
	{
		this.startDate=startDate;
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
		if (!testInitialized)
		{
			initializeTest();
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
	
	public void setCloseDate(Date closeDate)
	{
		this.closeDate=closeDate;
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
		if (!testInitialized)
		{
			initializeTest();
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
	
	public void setWarningDate(Date warningDate)
	{
		this.warningDate=warningDate;
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
		if (!testInitialized)
		{
			initializeTest();
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
	
	public void setFeedbackDate(Date feedbackDate)
	{
		this.feedbackDate=feedbackDate;
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
		if (!testInitialized)
		{
			initializeTest();
		}
		return restrictDates;
	}
	
	public void setRestrictDates(boolean restrictDates)
	{
		if (isEnabledChecboxesSetters())
		{
			this.restrictDates=restrictDates;
		}
	}
	
	public boolean isRestrictFeedbackDate()
	{
		if (!testInitialized)
		{
			initializeTest();
		}
		return restrictFeedbackDate;
	}
	
	public void setRestrictFeedbackDate(boolean restrictFeedbackDate)
	{
		if (isEnabledChecboxesSetters())
		{
			this.restrictFeedbackDate=restrictFeedbackDate;
		}
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
		if (!testInitialized)
		{
			initializeTest();
		}
		return freeSummary;
	}
	
	public void setFreeSummary(boolean freeSummary)
	{
		this.freeSummary=freeSummary;
	}
	
	public boolean isFreeStop()
	{
		if (!testInitialized)
		{
			initializeTest();
		}
		return freeStop;
	}
	
	public void setFreeStop(boolean freeStop)
	{
		this.freeStop=freeStop;
		
	}
	
	public boolean isSummaryQuestions()
	{
		if (!testInitialized)
		{
			initializeTest();
		}
		return summaryQuestions;
	}
	
	public void setSummaryQuestions(boolean summaryQuestions)
	{
		this.summaryQuestions=summaryQuestions;
		
	}
	
	public boolean isSummaryScores()
	{
		if (!testInitialized)
		{
			initializeTest();
		}
		return summaryScores;
	}
	
	public void setSummaryScores(boolean summaryScores)
	{
		this.summaryScores=summaryScores;
		
	}
	
	public boolean isSummaryAttempts()
	{
		if (!testInitialized)
		{
			initializeTest();
		}
		return summaryAttempts;
	}
	
	public void setSummaryAttempts(boolean summaryAttempts)
	{
		this.summaryAttempts=summaryAttempts;
		
	}
	
	public boolean isNavigation()
	{
		if (!testInitialized)
		{
			initializeTest();
		}
		return navigation;
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
	
	public NavLocation getNavLocation()
	{
		if (!testInitialized)
		{
			initializeTest();
		}
		return navLocation;
	}
	
	public void setNavLocation(NavLocation navLocation)
	{
		this.navLocation=navLocation;
	}
	
	public long getNavLocationId()
	{
		NavLocation navLocation=getNavLocation();
		return navLocation==null?0L:navLocation.getId();
	}
	
	public void setNavLocationId(long navLocationId)
	{
		setNavLocationId(null,navLocationId);
	}
	
	private void setNavLocationId(Operation operation,long navLocationId)
	{
		setNavLocation(navLocationsService.getNavLocation(getCurrentUserOperation(operation),navLocationId));
	}
	
	public RedoQuestionValue getRedoQuestion()
	{
		if (!testInitialized)
		{
			initializeTest();
		}
		return redoQuestion;
	}
	
	public void setRedoQuestion(RedoQuestionValue redoQuestion)
	{
		this.redoQuestion=redoQuestion;
	}
	
	public long getRedoQuestionId()
	{
		RedoQuestionValue redoQuestion=getRedoQuestion();
		return redoQuestion==null?0L:redoQuestion.getId();
	}
	
	public void setRedoQuestionId(long redoQuestionId)
	{
		setRedoQuestionId(null,redoQuestionId);
	}
	
	private void setRedoQuestionId(Operation operation,long redoQuestionId)
	{
		setRedoQuestion(
			redoQuestionValuesService.getRedoQuestion(getCurrentUserOperation(operation),redoQuestionId));
	}
	
	public boolean isRedoTest()
	{
		if (!testInitialized)
		{
			initializeTest();
		}
		return redoTest;
	}
	
	public void setRedoTest(boolean redoTest)
	{
		this.redoTest=redoTest;
		
	}
	
	public String getPresentationTitle()
	{
		if (!testInitialized)
		{
			initializeTest();
		}
		return presentationTitle;
	}
	
	public void setPresentationTitle(String presentationTitle)
	{
		this.presentationTitle=presentationTitle;
	}
	
	public String getPresentation()
	{
		if (!testInitialized)
		{
			initializeTest();
		}
		return presentation;
	}
	
	public void setPresentation(String presentation)
	{
		this.presentation=presentation;
	}
	
	public String getPreliminarySummaryTitle()
	{
		if (!testInitialized)
		{
			initializeTest();
		}
		return preliminarySummaryTitle;
	}
	
	public void setPreliminarySummaryTitle(String preliminarySummaryTitle)
	{
		this.preliminarySummaryTitle=preliminarySummaryTitle;
	}
	
	public String getPreliminarySummaryButton()
	{
		if (!testInitialized)
		{
			initializeTest();
		}
		return preliminarySummaryButton;
	}
	
	public void setPreliminarySummaryButton(String preliminarySummaryButton)
	{
		this.preliminarySummaryButton=preliminarySummaryButton;
	}
	
	public String getPreliminarySummary()
	{
		if (!testInitialized)
		{
			initializeTest();
		}
		return preliminarySummary;
	}
	
	public void setPreliminarySummary(String preliminarySummary)
	{
		this.preliminarySummary=preliminarySummary;
	}
	
	public boolean isFeedbackDisplaySummary()
	{
		if (!testInitialized)
		{
			initializeTest();
		}
		return feedbackDisplaySummary;
	}
	
	public void setFeedbackDisplaySummary(boolean feedbackDisplaySummary)
	{
		if (isEnabledChecboxesSetters())
		{
			this.feedbackDisplaySummary=feedbackDisplaySummary;
		}
	}
	
	public boolean isFeedbackDisplaySummaryMarks()
	{
		if (!testInitialized)
		{
			initializeTest();
		}
		return feedbackDisplaySummaryMarks;
	}
	
	public void setFeedbackDisplaySummaryMarks(boolean feedbackDisplaySummaryMarks)
	{
		this.feedbackDisplaySummaryMarks=feedbackDisplaySummaryMarks;
	}
	
	public boolean isFeedbackDisplaySummaryAttempts()
	{
		if (!testInitialized)
		{
			initializeTest();
		}
		return feedbackDisplaySummaryAttempts;
	}
	
	public void setFeedbackDisplaySummaryAttempts(boolean feedbackDisplaySummaryAttempts)
	{
		this.feedbackDisplaySummaryAttempts=feedbackDisplaySummaryAttempts;
	}
	
	public String getFeedbackSummaryPrevious()
	{
		if (!testInitialized)
		{
			initializeTest();
		}
		return feedbackSummaryPrevious;
	}
	
	public void setFeedbackSummaryPrevious(String feedbackSummaryPrevious)
	{
		this.feedbackSummaryPrevious=feedbackSummaryPrevious;
	}
	
	public boolean isFeedbackDisplayScores()
	{
		if (!testInitialized)
		{
			initializeTest();
		}
		return feedbackDisplayScores;
	}
	
	public void setFeedbackDisplayScores(boolean feedbackDisplayScores)
	{
		if (isEnabledChecboxesSetters())
		{
			this.feedbackDisplayScores=feedbackDisplayScores;
		}
	}
	
	public boolean isFeedbackDisplayScoresMarks()
	{
		if (!testInitialized)
		{
			initializeTest();
		}
		return feedbackDisplayScoresMarks;
	}
	
	public void setFeedbackDisplayScoresMarks(boolean feedbackDisplayScoresMarks)
	{
		this.feedbackDisplayScoresMarks=feedbackDisplayScoresMarks;
	}
	
	public boolean isFeedbackDisplayScoresPercentages()
	{
		if (!testInitialized)
		{
			initializeTest();
		}
		return feedbackDisplayScoresPercentages;
	}
	
	public void setFeedbackDisplayScoresPercentages(boolean feedbackDisplayScoresPercentages)
	{
		this.feedbackDisplayScoresPercentages=feedbackDisplayScoresPercentages;
	}
	
	public String getFeedbackScoresPrevious()
	{
		if (!testInitialized)
		{
			initializeTest();
		}
		return feedbackScoresPrevious;
	}
	
	public void setFeedbackScoresPrevious(String feedbackScoresPrevious)
	{
		this.feedbackScoresPrevious=feedbackScoresPrevious;
	}
	
	public String getFeedbackAdvancedPrevious()
	{
		if (!testInitialized)
		{
			initializeTest();
		}
		return feedbackAdvancedPrevious;
	}
	
	public void setFeedbackAdvancedPrevious(String feedbackAdvancedPrevious)
	{
		this.feedbackAdvancedPrevious=feedbackAdvancedPrevious;
	}
	
	public String getFeedbackAdvancedNext()
	{
		if (!testInitialized)
		{
			initializeTest();
		}
		return feedbackAdvancedNext;
	}
	
	public void setFeedbackAdvancedNext(String feedbackAdvancedNext)
	{
		this.feedbackAdvancedNext=feedbackAdvancedNext;
	}
	
	public List<SectionBean> getSections()
	{
    	if (!testInitialized)
    	{
    		initializeTest();
    	}
    	return sections;
    }
    
    public void setSections(List<SectionBean> sections)
    {
    	this.sections=sections;
    }
    
    public int getSectionsSize()
    {
    	return getSections().size();
    }
    
	public List<TestFeedbackBean> getFeedbacks()
	{
    	if (!testInitialized)
    	{
    		initializeTest();
    	}
		return feedbacks;
	}
	
	public void setFeedbacks(List<TestFeedbackBean> feedbacks)
	{
		this.feedbacks=feedbacks;
	}
	
	public List<SupportContactBean> getSupportContacts()
	{
		if (!testInitialized)
		{
			initializeTest();
		}
		return supportContacts;
	}
	
	public void setSupportContacts(List<SupportContactBean> supportContacts)
	{
		this.supportContacts=supportContacts;
	}
	
	public List<EvaluatorBean> getEvaluators()
	{
		if (!testInitialized)
		{
			initializeTest();
		}
		return evaluators;
	}
	
	public void setEvaluators(List<EvaluatorBean> evaluators)
	{
		this.evaluators=evaluators;
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
	private AddressType getAddressType(Operation operation,String type,String subtype)
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
		allTestAuthorCategoriesFilter.setId(ALL_TEST_AUTHOR_CATEGORIES.id);
		if (specialCategoriesFilters.contains(allTestAuthorCategoriesFilter))
		{
			setFilterCategoryId(ALL_TEST_AUTHOR_CATEGORIES.id);
			found=true;
		}
		if (!found)
		{
			Category allTestAuthorCategoriesExceptGlobalsFilter=new Category();
			allTestAuthorCategoriesExceptGlobalsFilter.setId(ALL_TEST_AUTHOR_CATEGORIES_EXCEPT_GLOBALS.id);
			if (specialCategoriesFilters.contains(allTestAuthorCategoriesExceptGlobalsFilter))
			{
				setFilterCategoryId(ALL_TEST_AUTHOR_CATEGORIES_EXCEPT_GLOBALS.id);
				found=true;
			}
		}
		if (!found)
		{
			Category allMyCategoriesFilter=new Category();
			allMyCategoriesFilter.setId(ALL_MY_CATEGORIES.id);
			if (specialCategoriesFilters.contains(allMyCategoriesFilter))
			{
				setFilterCategoryId(ALL_MY_CATEGORIES.id);
				found=true;
			}
		}
		if (!found)
		{
			Category allMyCategoriesExceptGlobalsFilter=new Category();
			allMyCategoriesExceptGlobalsFilter.setId(ALL_MY_CATEGORIES_EXCEPT_GLOBALS.id);
			if (specialCategoriesFilters.contains(allMyCategoriesExceptGlobalsFilter))
			{
				setFilterCategoryId(ALL_MY_CATEGORIES_EXCEPT_GLOBALS.id);
				found=true;
			}
		}
		if (!found)
		{
			Category allGlobalCategoriesFilter=new Category();
			allGlobalCategoriesFilter.setId(ALL_GLOBAL_CATEGORIES.id);
			if (specialCategoriesFilters.contains(allGlobalCategoriesFilter))
			{
				setFilterCategoryId(ALL_GLOBAL_CATEGORIES.id);
				found=true;
			}
		}
		if (!found)
		{
			Category allCategoriesFilter=new Category();
			allCategoriesFilter.setId(allCategories.id);
			if (specialCategoriesFilters.contains(allCategoriesFilter))
			{
				setFilterCategoryId(allCategories.id);
				found=true;
			}
		}
		if (!found && !specialCategoriesFilters.isEmpty())
		{
			setFilterCategoryId(specialCategoriesFilters.get(0).getId());
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
		// Get section to process if any
    	AccordionPanel sectionsAccordion=getSectionsAccordion(event.getComponent());
    	SectionBean section=getSectionFromSectionsAccordion(sectionsAccordion);
		
    	String property=getPropertyChecked();
    	if (property==null)
    	{
    		// We need to process some input fields
    		processSectionsInputFields(event.getComponent(),section);
    		
    		// Set back accordion row index -1
    		sectionsAccordion.setRowIndex(-1);
    		
    		// Check that current section name entered by user is valid
    		if (checkSectionName(section.getName()))
    		{
        		//Add a new section
        		int numberSections=getSectionsSize()+1;
            	section=new SectionBean(this,numberSections);
            	getSections().add(section);
        		
            	// Change active tab of sections accordion to display the new section
        		activeSectionIndex=numberSections-1;
        		activeSectionName=section.getName();
        		refreshActiveSection(sectionsAccordion);
        		
        		// We need to update sections text fields
        		updateSectionsTextFields(event.getComponent(),numberSections);
    		}
    		else
    		{
    			// Scroll page to top position
    			scrollToTop();
    			
    			// We need to update questions of current section
    			updateSectionQuestions(getActiveSection(event.getComponent()));
    			
        		// We need to update sections text fields
        		updateSectionsTextFields(event.getComponent(),getSectionsSize());
        		
    			// Restore old section name
    			section.setName(activeSectionName);
    		}
    	}
    	else if ("sectionWeight".equals(property))
    	{
			// We need to process weight
    		processSectionWeight(event.getComponent(),section);
    		
    		// We need to stop this response because there is another one in process
    		FacesContext.getCurrentInstance().responseComplete();
    	}
    	else if ("randomQuantity".equals(property))
    	{
			// We need to process random quantity
    		processSectionRandomQuantity(event.getComponent(),section);
    		
    		// We need to stop this response because there is another one in process
    		FacesContext.getCurrentInstance().responseComplete();
    	}
    	else if ("questionOrderWeight".equals(property))
    	{
    		// We need to process question orders weights
    		processQuestionOrderWeights(event.getComponent(),section);
    		
    		// We need to stop this response because there is another one in process
    		FacesContext.getCurrentInstance().responseComplete();
    	}
	}
    
	/**
	 * Checks if deleting a section will affect to the feedbacks already defined.
	 * @param sectionOrder Position of section to delete
	 * @return true if deleting a section won't affect to the feedbacks already defined, false otherwise
	 */
	private boolean checkFeedbacksForDeleteSection(int sectionOrder)
	{
		boolean ok=true;
		int newTotalMarks=0;
		
		ScoreType scoreType=getScoreType();
		if ("SCORE_TYPE_QUESTIONS".equals(scoreType.getType()))
		{
			for (SectionBean section:getSections())
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
			for (SectionBean section:getSections())
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
		for (TestFeedbackBean feedback:getFeedbacks())
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
	 * @param sectionOrder Position of deleted section
	 */
	private void updateFeedbacksForDeleteSection(int sectionOrder)
	{
		for (TestFeedbackBean feedback:getFeedbacks())
		{
			TestFeedbackConditionBean condition=feedback.getCondition();
			if (condition.getSection()!=null && condition.getSection().getOrder()==sectionOrder)
			{
				condition.setSection(null);
			}
			if (condition.getSection()==null && 
				condition.getUnit().equals(TestFeedbackConditionBean.MARKS_UNIT))
			{
				int maxValue=condition.getMaxConditionalValue();
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
						if (maxValue>0)
						{
							maxValue--;
						}
						else
						{
							// We set the new comparator twice so oldComparator will be changed too
							condition.setComparator(NumberComparator.GREATER_EQUAL);
							condition.setComparator(NumberComparator.GREATER_EQUAL);
						}
					}
					if (condition.getConditionalCmp()>maxValue)
					{
						condition.setConditionalCmp(maxValue);
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
    		boolean forceRemoveSection=true;
    		if (event.getComponent().getAttributes().get("order")!=null)
    		{
    			sectionToRemoveOrder=((Integer)event.getComponent().getAttributes().get("order")).intValue();
    			forceRemoveSection=false;
    		}
    		boolean checkFeedbacks=checkFeedbacksForDeleteSection(sectionToRemoveOrder);
        	if (forceRemoveSection || checkFeedbacks)
        	{
        		List<SectionBean> sections=getSections();
        		
               	// Remove section from test
            	sections.remove(sectionToRemoveOrder-1);
        		for (int index=sectionToRemoveOrder-1;index<sections.size();index++)
        		{
        			sections.get(index).setOrder(index+1);		 
        		}
        		
        		// Get sections accordion
        		AccordionPanel sectionsAccordion=getSectionsAccordion(event.getComponent());
        		
        		// If it is needeed change active tab of sections accordion
        		int numSections=sections.size();
        		if (sectionToRemoveOrder>numSections)
        		{
        			activeSectionIndex=numSections-1;
        			refreshActiveSection(sectionsAccordion);
        		}
        		activeSectionName=getSection(activeSectionIndex+1).getName();
        		
        		// Set the new current section to be able to update text fields
        		setCurrentSection(getSectionFromSectionsAccordion(sectionsAccordion));
        		
    			// If it is needed update feedbacks
    			if (!checkFeedbacks)
    			{
    				updateFeedbacksForDeleteSection(sectionToRemoveOrder);
    			}
        		
        		if (getCurrentSection()!=null)
        		{
            		// We need to update sections text fields
            		updateSectionsTextFields(event.getComponent(),numSections);
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
    		processSectionRandomQuantity(event.getComponent(),sectionChecked);
    		
    		// We need to stop this response because there is another one in process
    		FacesContext.getCurrentInstance().responseComplete();
    	}
    	else if ("randomQuantity".equals(property))
    	{
			// We need to process random quantity
    		processSectionRandomQuantity(event.getComponent(),sectionChecked);
    		
    		// We need to stop this response because there is another one in process
    		FacesContext.getCurrentInstance().responseComplete();
    	}
	}
    
	/**
	 * @return true if button to delete a section is enabled, false if it is disabled
	 */
    public boolean isEnabledRemoveSection()
    {
    	return getSections().size()>1;
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
			
			User testAuthor=getAuthor();
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
							granted=permissionsService.isGranted(operation,testAuthor,requiredAuthorPermission);
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
			specialCategoryFilterName=specialCategoryFilterName.replace("?",getAuthor().getNick());
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
    		
    		User testAuthor=getAuthor();
    		
        	testsCategories=categoriesService.getCategoriesSortedByHierarchy(operation,testAuthor,
        		categoryTypesService.getCategoryType(operation,"CATEGORY_TYPE_TESTS"),true,true,
        		CategoriesService.NOT_VIEW_OTHER_USERS_CATEGORIES);
    		
			// Remove from list categories not allowed
        	List<Category> testsCategoriesToRemove=new ArrayList<Category>();
        	for (Category testCategory:testsCategories)
        	{
        		if (!checkCategory(operation,testCategory))
        		{
        			testsCategoriesToRemove.add(testCategory);
        		}
        	}
        	for (Category testCategoryToRemove:testsCategoriesToRemove)
        	{
        		testsCategories.remove(testCategoryToRemove);
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
    		
       		// Get filter value for viewing questions from global categories
    		boolean includeGlobalCategories=getFilterGlobalQuestionsEnabled(operation).booleanValue();
    		
       		// Get filter value for viewing questions from categories of other users based on permissions
       		// of current user
    		int includeOtherUsersCategories=CategoriesService.NOT_VIEW_OTHER_USERS_CATEGORIES;
    		if (getFilterOtherUsersQuestionsEnabled(operation).booleanValue())
    		{
    			if (getViewQuestionsFromOtherUsersPrivateCategoriesEnabled(operation).booleanValue())
    			{
    				includeOtherUsersCategories=CategoriesService.VIEW_OTHER_USERS_ALL_CATEGORIES;
    			}
    			else
    			{
    				includeOtherUsersCategories=CategoriesService.VIEW_OTHER_USERS_PUBLIC_CATEGORIES;
    			}
    		}
    		
       		// In case that current user is allowed to view questions from private categories of other users 
       		// we also need to check if he/she has permission to view questions from private categories 
       		// of administrators and/or users with permission to improve permissions over their owned ones 
       		// (superadmins)
       		boolean includeAdminsPrivateCategories=false;
       		boolean includeSuperadminsPrivateCategories=false;
       		if (includeOtherUsersCategories==CategoriesService.VIEW_OTHER_USERS_ALL_CATEGORIES)
       		{
       			includeAdminsPrivateCategories=
       				getViewQuestionsFromAdminsPrivateCategoriesEnabled(operation).booleanValue();
       			includeSuperadminsPrivateCategories=
       				getViewQuestionsFromSuperadminsPrivateCategoriesEnabled(operation).booleanValue();
       		}
       		
       		// Get visible categories for images taking account all user permissions
       		questionsCategories=categoriesService.getCategoriesSortedByHierarchy(operation,
       			userSessionService.getCurrentUser(operation),
       			categoryTypesService.getCategoryType(operation,"CATEGORY_TYPE_IMAGES"),true,
       			includeGlobalCategories,includeOtherUsersCategories,includeAdminsPrivateCategories,
       			includeSuperadminsPrivateCategories);
       		
			// Remove from list categories not allowed
        	List<Category> questionsCategoriesToRemove=new ArrayList<Category>();
        	for (Category questionCategory:questionsCategories)
        	{
        		if (!checkQuestionsFilterPermission(operation,questionCategory))
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
    
    private void setQuestionsCategories(List<Category> questionsCategories)
    {
    	this.questionsCategories=questionsCategories;
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
    	ScoreType scoreType=getScoreType();
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
    	String testName=getName();
    	ok=checkTestName(testName);
    	
        // Check category
    	if (!categoriesService.checkCategoryId(operation,getCategoryId()))
    	{
    		addErrorMessage("TEST_CATEGORY_STEP_NOT_FOUND");
    		ok=false;
    		
    		// Refresh tests categories from DB
    		resetTestsCategories(operation);
    	}
    	else if (checkCategory(operation))
    	{
    		if (!checkAvailableTestName(operation))
    		{
    			addErrorMessage("TEST_NAME_ALREADY_DECLARED");
    			ok=false;
    		}
    	}
    	else
        {
        	addErrorMessage("TEST_CATEGORY_NOT_GRANTED_ERROR");
        	ok=false;
        	
    		// Refresh tests categories from DB
    		resetTestsCategories(operation);
        }
    	
    	return ok;
    }
    
	/**
	 * @param operation Operation
	 * @return true if category selected is usable by current user, false otherwise
	 */
    private boolean checkCategory(Operation operation)
    {
		return checkCategory(getCurrentUserOperation(operation),getCategory());
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
    		categoryTypesService.getCategoryType(operation,"CATEGORY_TYPE_TESTS"),
    		categoryTypesService.getCategoryTypeFromCategoryId(operation,category.getId()));
		
    	// Check visibility
    	if (ok)
    	{
    		ok=false;
    		User testAuthor=getAuthor();
    		Visibility categoryVisibility=
    			visibilitiesService.getVisibilityFromCategoryId(operation,category.getId());
    		if (categoryVisibility.isGlobal())
    		{
    			ok=getFilterGlobalTestsEnabled(operation).booleanValue() && 
    				((initialCategoryId>0L && category.getId()==initialCategoryId) || 
    				testAuthor.equals(category.getUser()) || 
    				getGlobalOtherUserCategoryAllowed(operation).booleanValue());
    		}
    		else if (testAuthor.equals(category.getUser()))
    		{
    			if (testAuthor.getId()==userSessionService.getCurrentUserId())
    			{
    				ok=true;
    			}
    			else if (getFilterOtherUsersTestsEnabled(operation).booleanValue())
    			{
    				if (categoryVisibility.getLevel()>=visibilitiesService.getVisibility(
    					operation,"CATEGORY_VISIBILITY_PRIVATE").getLevel())
    				{
    					ok=getViewTestsFromOtherUsersPrivateCategoriesEnabled(operation).booleanValue() && 
    						(!getTestAuthorAdmin(operation).booleanValue() || 
    						getViewTestsFromAdminsPrivateCategoriesEnabled(operation).booleanValue()) && 
    						(!getTestAuthorSuperadmin(operation).booleanValue() || 
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
	 * @return true if current category of the test we are editing is usable by current user, false otherwise
	 */
    private boolean checkCurrentCategory(Operation operation)
    {
    	// Get current user session Hibernate operation
    	operation=getCurrentUserOperation(operation);
    	
    	return checkCategory(operation,categoriesService.getCategoryFromTestId(operation,getTestId()));
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
		
		String testName=getName();
		long categoryId=getCategoryId();
		if (testName!=null)
		{
			ok=testsService.isTestNameAvailable(operation,testName,categoryId,getTestId());
		}
		return ok;
	}
    
    private boolean checkCalendarInputFields()
    {
    	boolean ok=true;
    	
   		if (isRestrictDates())
   		{
   			String startDateHidden=getStartDateHidden();
   			String closeDateHidden=getCloseDateHidden();
   			String warningDateHidden=getWarningDateHidden();
   			Date startDate=getStartDate();
   			Date closeDate=getCloseDate();
   			Date warningDate=getWarningDate();
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
   			if (isRestrictFeedbackDate())
   			{
   				String feedbackDateHidden=getFeedbackDateHidden();
   				Date feedbackDate=getFeedbackDate();
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
	 * Refresh available categories of the combo box.
	 * @param event Action event
	 */
	public void refreshTestCategories(ActionEvent event)
	{
		// Get current user session Hibernate operation
		Operation operation=getCurrentUserOperation(null);
		
		setFilterGlobalTestsEnabled(null);
		setFilterOtherUsersTestsEnabled(null);
		setGlobalOtherUserCategoryAllowed(null);
		resetTestAuthorAdmin();
		resetTestAuthorSuperadmin();
		setViewTestsFromOtherUsersPrivateCategoriesEnabled(null);
		setViewTestsFromAdminsPrivateCategoriesEnabled(null);
		setViewTestsFromSuperadminsPrivateCategoriesEnabled(null);
		if (!categoriesService.checkCategoryId(operation,getCategoryId()) || !checkCategory(operation))
		{
			// Refresh tests categories from DB
			resetTestsCategories(operation);
		}
		else
		{
			// Reload tests categories from DB
			setTestsCategories(null);
		}
	}
	
	private void resetTestsCategories(Operation operation)
	{
		// Get current user session operation
		operation=getCurrentUserOperation(operation);
		
		// Reload tests categories from DB
		setTestsCategories(null);
		
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
			List<Category> testsCategories=getTestsCategories(operation);
			if (!testsCategories.isEmpty())
			{
				resetCategoryId=testsCategories.get(0).getId();
			}
		}
		setCategoryId(operation,resetCategoryId);
	}
    
	/**
	 * Refresh available categories of the combo box within the 'Add questions' dialog.
	 * @param event Action event
	 */
	public void refreshQuestionsCategories(ActionEvent event)
	{
		// Get current user session operation
		Operation operation=getCurrentUserOperation(null);
		
		setFilterGlobalQuestionsEnabled(null);
		setFilterOtherUsersQuestionsEnabled(null);
		setUseGlobalQuestions(null);
		setUseOtherUsersQuestions(null);
	    setViewQuestionsFromOtherUsersPrivateCategoriesEnabled(null);
	    setViewQuestionsFromAdminsPrivateCategoriesEnabled(null);
	    setViewQuestionsFromSuperadminsPrivateCategoriesEnabled(null);
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
	    if (filterCategory==null || !checkQuestionsFilterPermission(operation,filterCategory))
	    {
	    	setFilterCategoryId(Long.MIN_VALUE);
	    }
	    
		// Reload questions categories from DB
		setQuestionsCategories(null);
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
    		setFilterGlobalTestsEnabled(null);
    		setFilterOtherUsersTestsEnabled(null);
    		setGlobalOtherUserCategoryAllowed(null);
    		resetTestAuthorAdmin();
    		resetTestAuthorSuperadmin();
    		setViewTestsFromOtherUsersPrivateCategoriesEnabled(null);
    		setViewTestsFromAdminsPrivateCategoriesEnabled(null);
    		setViewTestsFromSuperadminsPrivateCategoriesEnabled(null);
    		
    		if (!checkCommonDataInputFields(operation))
    		{
    			ok=false;
    		}
    		if (activeGeneralTabIndex==CALENDAR_TAB_GENERAL_ACCORDION && !checkCalendarInputFields())
    		{
    			ok=false;
    		}
    	}
    	else if (PRESENTATION_WIZARD_TAB.equals(oldStep))
    	{
    		if (SECTIONS_WIZARD_TAB.equals(nextStep))
    		{
    			// Get current section
    			SectionBean currentSection=getSection(activeSectionIndex+1);
    			
    			if (currentSection!=null)
    			{
    				// Restore old section name
    				currentSection.setName(activeSectionName);
    			}
    		}
    	}
    	else if (SECTIONS_WIZARD_TAB.equals(oldStep))
    	{
    		boolean errors=false;
			boolean displayErrors=
				(isNavigation()?PRELIMINARY_SUMMARY_WIZARD_TAB:FEEDBACK_WIZARD_TAB).equals(nextStep);
        	boolean needConfirm=false;
        	sectionChecked=getSection(activeSectionIndex+1);
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
        				updateSectionsTextFields(event.getComponent(),getSectionsSize());
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
   					checkFeedbacks=checkFeedbacksForChangeProperty();
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
       					updateSectionWeight(event.getComponent(),sectionChecked);
   					}
   					else if (sectionChecked.getWeight()>getMaxWeight())
   					{
   						sectionChecked.setWeight(getMaxWeight());
   						
    					// We need to update section weight
       					updateSectionWeight(event.getComponent(),sectionChecked);
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
    					updateSectionRandomQuantity(event.getComponent(),sectionChecked);
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
    					updateSectionRandomQuantity(event.getComponent(),sectionChecked);
    				}
    				else if (sectionChecked.getRandomQuantity()>sectionChecked.getQuestionOrdersSize())
    				{
    					sectionChecked.setRandomQuantity(sectionChecked.getQuestionOrdersSize());
    					
    					// We need to update random quantity
   						updateSectionRandomQuantity(event.getComponent(),sectionChecked);
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
    						updateQuestionOrderWeights(event.getComponent(),sectionChecked);
   						}
   						else if (questionOrderChecked.getWeight()>getMaxWeight())
   						{
   							questionOrderChecked.setWeight(getMaxWeight());
   							
    						// We need to update questions weights
   							updateQuestionOrderWeights(event.getComponent(),sectionChecked);
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
    		setFilterGlobalTestsEnabled(null);
    		setFilterOtherUsersTestsEnabled(null);
    		setGlobalOtherUserCategoryAllowed(null);
    		resetTestAuthorAdmin();
    		resetTestAuthorSuperadmin();
    		setViewTestsFromOtherUsersPrivateCategoriesEnabled(null);
    		setViewTestsFromAdminsPrivateCategoriesEnabled(null);
    		setViewTestsFromSuperadminsPrivateCategoriesEnabled(null);
    		setAddEnabled(null);
    		setEditEnabled(null);
    		setEditOtherUsersTestsEnabled(null);
    		setEditAdminsTestsEnabled(null);
    		setEditSuperadminsTestsEnabled(null);
    		setFilterGlobalQuestionsEnabled(null);
    		setFilterOtherUsersQuestionsEnabled(null);
    		setUseGlobalQuestions(null);
    		setUseOtherUsersQuestions(null);
    		setViewQuestionsFromOtherUsersPrivateCategoriesEnabled(null);
    		setViewQuestionsFromAdminsPrivateCategoriesEnabled(null);
    		setViewQuestionsFromSuperadminsPrivateCategoriesEnabled(null);
    		resetAdmins();
    		resetSuperadmins();
    		
			// Scroll page to top position
			scrollToTop();
    	}
    	if (ok)
    	{
        	if (GENERAL_WIZARD_TAB.equals(nextStep))
        	{
    			setFilterGlobalTestsEnabled(null);
    			setFilterOtherUsersTestsEnabled(null);
    			setGlobalOtherUserCategoryAllowed(null);
    			resetTestAuthorAdmin();
    			resetTestAuthorSuperadmin();
    			setViewTestsFromOtherUsersPrivateCategoriesEnabled(null);
    			setViewTestsFromAdminsPrivateCategoriesEnabled(null);
    			setViewTestsFromSuperadminsPrivateCategoriesEnabled(null);
    			if (!categoriesService.checkCategoryId(operation,getCategoryId()) || !checkCategory(operation))
    			{
    				// Refresh tests categories from DB
    				resetTestsCategories(operation);
    			}
    			else
    			{
    				// Reload tests categories from DB
    				setTestsCategories(null);
    			}
        	}
    	}
    	setEnabledCheckboxesSetters(!CONFIRMATION_WIZARD_TAB.equals(nextStep));    	
    	activeTestTabName=nextStep;
    	
    	updateQuestions();
    	if (SECTIONS_WIZARD_TAB.equals(nextStep))
    	{
    		updateSectionsTextFields(event.getComponent(),getSectionsSize());
    	}
    	
    	return nextStep;
    }
    
    public String getActiveTestTabName()
    {
    	String activeTestTabName=null;
    	if (getTestId()>0L)
    	{
			switch (activeTestTabIndex)
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
					if (activeTestTabIndex==preliminarySummaryTabviewTab)
					{
						activeTestTabName=PRELIMINARY_SUMMARY_WIZARD_TAB;
					}
					else if (activeTestTabIndex==feedbackTabviewTab)
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
    
    public int getTestTabIndex(String testTabName)
    {
    	int testTabIndex=-1;
    	if (GENERAL_WIZARD_TAB.equals(testTabName))
    	{
    		testTabIndex=GENERAL_TABVIEW_TAB;
    	}
    	else if (PRESENTATION_WIZARD_TAB.equals(testTabName))
    	{
    		testTabIndex=PRESENTATION_TABVIEW_TAB;
    	}
    	else if (SECTIONS_WIZARD_TAB.equals(testTabName))
    	{
    		testTabIndex=SECTIONS_TABVIEW_TAB;
    	}
    	else if (PRELIMINARY_SUMMARY_WIZARD_TAB.equals(testTabName))
    	{
    		testTabIndex=preliminarySummaryTabviewTab;
    	}
    	else if (FEEDBACK_WIZARD_TAB.equals(testTabName))
    	{
    		testTabIndex=feedbackTabviewTab;
    	}
    	return testTabIndex;
    }
    
	/**
	 * Tab change listener for displaying other tab of a test.
	 * @param event Tab change event
	 */
    public void changeActiveTestTab(TabChangeEvent event)
    {
		boolean ok=true;
		
		// Get current user session Hibernate operation
		Operation operation=getCurrentUserOperation(null);
		
    	TabView testFormTabs=(TabView)event.getComponent();
        if (activeTestTabIndex==GENERAL_TABVIEW_TAB)
        {
        	// We need to process some input fields
        	processCommonDataInputFields(operation,testFormTabs);
        	if (activeGeneralTabIndex==CALENDAR_TAB_GENERAL_ACCORDION)
        	{
        		processCalendarTabCommonDataInputFields(testFormTabs);
        	}
        	
        	setFilterGlobalTestsEnabled(null);
        	setFilterOtherUsersTestsEnabled(null);
        	setGlobalOtherUserCategoryAllowed(null);
        	resetTestAuthorAdmin();
        	resetTestAuthorSuperadmin();
       		setViewTestsFromOtherUsersPrivateCategoriesEnabled(null);
       		setViewTestsFromAdminsPrivateCategoriesEnabled(null);
       		setViewTestsFromSuperadminsPrivateCategoriesEnabled(null);
       		if (!checkCommonDataInputFields(operation))
       		{
       			ok=false;
       		}
       		if (activeGeneralTabIndex==CALENDAR_TAB_GENERAL_ACCORDION && !checkCalendarInputFields())
       		{
       			ok=false;
       		}
           	if (ok)
           	{
            	activeTestTabIndex=testFormTabs.getActiveIndex();
           	}
           	else
           	{
           		testFormTabs.setActiveIndex(activeTestTabIndex);
           	}
       	}
      	else if (activeTestTabIndex==PRESENTATION_TABVIEW_TAB)
       	{
       		processPresentationEditorField(testFormTabs);
       		activeTestTabIndex=testFormTabs.getActiveIndex();
       	}
       	else if (activeTestTabIndex==SECTIONS_TABVIEW_TAB)
       	{
       		boolean needConfirm=false;
       		sectionChecked=getSection(activeSectionIndex+1);
       		
       		// We need to process some input fields
       		processSectionsInputFields(testFormTabs,sectionChecked);
       		
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
       				checkFeedbacks=checkFeedbacksForChangeProperty();
       				needConfirm=!checkFeedbacks;
       			}
       			else
       			{
       				needConfirm=false;
       			}
       			if (needConfirm)
       			{
       				nextTestIndexOnChangePropertyConfirm=testFormTabs.getActiveIndex();
       				testFormTabs.setActiveIndex(activeTestTabIndex);
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
      					updateSectionWeight(event.getComponent(),sectionChecked);
       				}
       				else if (sectionChecked.getWeight()>getMaxWeight())
       				{
       					sectionChecked.setWeight(getMaxWeight());
       					
       					// We need to update section weights
       					updateSectionWeight(event.getComponent(),sectionChecked);
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
       					updateSectionRandomQuantity(event.getComponent(),sectionChecked);
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
       					updateSectionRandomQuantity(event.getComponent(),sectionChecked);
       				}
       				else if (sectionChecked.getRandomQuantity()>sectionChecked.getQuestionOrdersSize())
       				{
       					sectionChecked.setRandomQuantity(sectionChecked.getQuestionOrdersSize());
       					
       					// We need to update random quantity
       					updateSectionRandomQuantity(event.getComponent(),sectionChecked);
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
       						updateQuestionOrderWeights(event.getComponent(),sectionChecked);
       					}
       					else if (questionOrderChecked.getWeight()>getMaxWeight())
       					{
       						questionOrderChecked.setWeight(getMaxWeight());
       						
       						// We need to update questions weights
       						updateQuestionOrderWeights(event.getComponent(),sectionChecked);
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
       			activeTestTabIndex=testFormTabs.getActiveIndex();
       		}
       		else
       		{
       			testFormTabs.setActiveIndex(activeTestTabIndex);
       			
       			// Scroll page to top position
       			scrollToTop();
       		}
       	}
       	else if (isNavigation() && activeTestTabIndex==PRELIMINARY_SUMMARY_OR_FEEDBACK_TABVIEW_TAB)
       	{
       		processPreliminarySummaryEditorField(testFormTabs);
       		activeTestTabIndex=testFormTabs.getActiveIndex();
       	}
       	else if (activeTestTabIndex>=PRELIMINARY_SUMMARY_OR_FEEDBACK_TABVIEW_TAB)
       	{
       		processFeedbackEditorInputFields(testFormTabs);
       		activeTestTabIndex=testFormTabs.getActiveIndex();
       	}
       	else
       	{
       		activeTestTabIndex=testFormTabs.getActiveIndex();
       	}
    	if (ok)
    	{
    		if (activeTestTabIndex==GENERAL_TABVIEW_TAB)
    		{
    			
        		setFilterGlobalTestsEnabled(null);
        		setFilterOtherUsersTestsEnabled(null);
        		setGlobalOtherUserCategoryAllowed(null);
                resetTestAuthorAdmin();
                resetTestAuthorSuperadmin();
                setViewTestsFromOtherUsersPrivateCategoriesEnabled(null);
                setViewTestsFromAdminsPrivateCategoriesEnabled(null);
                setViewTestsFromSuperadminsPrivateCategoriesEnabled(null);
        		if (!categoriesService.checkCategoryId(operation,getCategoryId()) || !checkCategory(operation))
        		{
        			// Refresh tests categories from DB
        			resetTestsCategories(operation);
        		}
        		else
        		{
        			// Reload tests categories from DB
        			setTestsCategories(null);
        		}
    		}
    	}
    	else
        {
           	// Reset user permissions
        	setFilterGlobalTestsEnabled(null);
        	setFilterOtherUsersTestsEnabled(null);
        	setGlobalOtherUserCategoryAllowed(null);
        	resetTestAuthorAdmin();
        	resetTestAuthorSuperadmin();
       		setViewTestsFromOtherUsersPrivateCategoriesEnabled(null);
       		setViewTestsFromAdminsPrivateCategoriesEnabled(null);
       		setViewTestsFromSuperadminsPrivateCategoriesEnabled(null);
       		setAddEnabled(null);
       		setEditEnabled(null);
       		setEditOtherUsersTestsEnabled(null);
       		setEditAdminsTestsEnabled(null);
       		setEditSuperadminsTestsEnabled(null);
    		setFilterGlobalQuestionsEnabled(null);
    		setFilterOtherUsersQuestionsEnabled(null);
       		setUseGlobalQuestions(null);
       		setUseOtherUsersQuestions(null);
       		setViewQuestionsFromOtherUsersPrivateCategoriesEnabled(null);
       		setViewQuestionsFromAdminsPrivateCategoriesEnabled(null);
       		setViewQuestionsFromSuperadminsPrivateCategoriesEnabled(null);
           	resetAdmins();
           	resetSuperadmins();
    		
        	// Scroll page to top position
        	scrollToTop();
        }
    	updateQuestions();
    	if (activeTestTabIndex==SECTIONS_TABVIEW_TAB)
    	{
    		updateSectionsTextFields(event.getComponent(),getSectionsSize());
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
    		if (!checkCalendarInputFields())
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
    		refreshActiveGeneralTab(generalAccordion);
    	
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
    	AccordionPanel sectionsAccordion=(AccordionPanel)event.getComponent();
   		boolean ok=true;
   		boolean needConfirm=false;
   		int oldSectionsAccordionRowIndex=sectionsAccordion.getRowIndex();
   		sectionChecked=getSectionFromSectionsAccordion(sectionsAccordion);
   		if (sectionChecked!=null)
   		{
       		// We need to process some input fields
       		processSectionsInputFields(event.getComponent(),sectionChecked);
       		
			// We need to update all section weights
			updateSectionWeights(sectionsAccordion,sectionChecked);
       		
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
   					checkFeedbacks=checkFeedbacksForChangeProperty();
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
       					updateSectionWeight(sectionsAccordion,sectionChecked);
       				}
       				else if (sectionChecked.getWeight()>getMaxWeight())
       				{
       					sectionChecked.setWeight(getMaxWeight());
       					
       					// We need to update section weight
       					updateSectionWeight(sectionsAccordion,sectionChecked);
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
   						updateSectionRandomQuantity(sectionsAccordion,sectionChecked);
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
   						updateSectionRandomQuantity(sectionsAccordion,sectionChecked);
   					}
   					else if (sectionChecked.getRandomQuantity()>sectionChecked.getQuestionOrdersSize())
   					{
   						sectionChecked.setRandomQuantity(sectionChecked.getQuestionOrdersSize());
   						
   						// We need to update random quantity
   						updateSectionRandomQuantity(sectionsAccordion,sectionChecked);
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
   							updateQuestionOrderWeights(sectionsAccordion,sectionChecked);
   						}
   						else if (questionOrderChecked.getWeight()<=getMinWeight())
   						{
   							questionOrderChecked.setWeight(getMinWeight());
   							
   							// We need to update questions weights
   							updateQuestionOrderWeights(sectionsAccordion,sectionChecked);
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
   				updateSectionsTextFields(sectionsAccordion,getSectionsSize());
   				sectionChecked.setName(activeSectionName);
   				refreshActiveSection(sectionsAccordion);
   				
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
   				refreshActiveSection(sectionsAccordion);
   				
   	    		sectionsAccordion.setRowIndex(oldSectionsAccordionRowIndex);
   	    		
    			// We need to update questions of this section
    			updateSectionQuestions(sectionChecked);
   	    		
        		// We need to update sections text fields
        		updateSectionsTextFields(event.getComponent(),getSectionsSize());
    			
       			RequestContext rq=RequestContext.getCurrentInstance();
       			rq.execute("confirmChangePropertyDialog.show()");
   			}
   			else
   			{
   				SectionBean activeSection=null;
   				try
   				{
   					activeSectionIndex=Integer.parseInt(sectionsAccordion.getActiveIndex());
   					activeSection=getSectionFromSectionsAccordion(sectionsAccordion);
   					if (activeSection!=null)
   					{
   						activeSectionName=activeSection.getName();
   					}
   					else
   					{
   						activeSectionName="";
   					}
   				}
   				catch (NumberFormatException nfe)
   				{
   					activeSectionIndex=-1;
   					activeSectionName="";
   				}
       			sectionsAccordion.setRowIndex(oldSectionsAccordionRowIndex);
       			if (activeSection!=null)
       			{
       				// We need to update questions of the new active section
       				updateSectionQuestions(activeSection);
       				
            		// We need to update sections text fields
            		updateSectionsTextFields(event.getComponent(),getSectionsSize());
       			}
   			}
   		}
   		else
   		{
   			sectionsAccordion.setRowIndex(oldSectionsAccordionRowIndex);
   			
   			// We need to update questions of this section
   			updateSectionQuestions(getActiveSection(event.getComponent()));
   			
    		// We need to update sections text fields
    		updateSectionsTextFields(event.getComponent(),getSectionsSize());
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
	 * Ajax listener.<br/><br/>
	 * I have defined a tab change listener for accordion of the feedback tab of the test and that accordion 
	 * is inside the test's tabview.<br/><br/>
	 * Curiosly when I change the test's tab, Primefaces fires the listener defined for the accordion but 
	 * calls it with an AjaxBehaviourEvent argument.<br/><br/>
	 * As this listener is called unintentionally we have defined it only to avoid an error message and 
	 * it does nothing.
	 * @param event Ajax event
	 */
	public void changeActiveFeedbackTab(AjaxBehaviorEvent event)
	{
	}
	
	/**
	 * Tab change listener for displaying other tab within accordion of the feedback tab of the test.
	 * @param event Tab change event
	 */
	public void changeActiveFeedbackTab(TabChangeEvent event)
	{
    	AccordionPanel feedbackAccordion=(AccordionPanel)event.getComponent();
		try
		{
			activeFeedbackTabIndex=Integer.parseInt(feedbackAccordion.getActiveIndex());
		}
		catch (NumberFormatException nfe)
		{
			activeFeedbackTabIndex=-1;
		}
		if (activeFeedbackTabIndex==ADVANCED_FEEDBACK_TAB_FEEDBACK_ACCORDION)
		{
			updateQuestionsForFeedbacks();
		}
	}
	
	private void updateQuestions()
	{
		updateQuestions(null,false);
	}
	
	private void updateQuestions(boolean updateAllTabs)
	{
		updateQuestions(null,updateAllTabs);
	}
	
	private void updateQuestions(Operation operation,boolean updateAllTabs)
	{
		if (updateAllTabs)
		{
			for (SectionBean section:getSections())
			{
				operation=updateSectionQuestions(operation,section);
			}
		}
		else
		{
			String activeTestTabName=getActiveTestTabName();
			if (SECTIONS_WIZARD_TAB.equals(activeTestTabName))
			{
				FacesContext context=FacesContext.getCurrentInstance();
				updateSectionQuestions(operation,getActiveSection(context.getViewRoot()));
			}
			else if (FEEDBACK_WIZARD_TAB.equals(activeTestTabName))
			{
				if (activeFeedbackTabIndex==ADVANCED_FEEDBACK_TAB_FEEDBACK_ACCORDION)
				{
					updateQuestionsForFeedbacks(operation);
				}
			}
		}
	}
	
	private Operation updateSectionQuestions(SectionBean section)
	{
		return updateSectionQuestions(null,section);
	}
	
	private Operation updateSectionQuestions(Operation operation,SectionBean section)
	{
		List<QuestionOrderBean> questionOrders=section.getQuestionOrders();
		if (!questionOrders.isEmpty())
		{
			FacesContext context=FacesContext.getCurrentInstance();
			if (operation==null)
			{
				// End current user session Hibernate operation
				userSessionService.endCurrentUserOperation();
				
				// Get current user session Hibernate operation
				operation=getCurrentUserOperation(null);
			}
			List<QuestionOrderBean> questionOrdersToRemove=new ArrayList<QuestionOrderBean>();
			for (QuestionOrderBean questionOrder:section.getQuestionOrders())
			{
				if (!updateQuestion(operation,section,questionOrder))
				{
					questionOrdersToRemove.add(questionOrder);
				}
			}
			for (QuestionOrderBean questionOrderToRemove:questionOrdersToRemove)
			{
				int questionToRemoveSectionOrder=section.getOrder();
				int questionToRemoveOrder=questionOrderToRemove.getOrder();
        		boolean checkFeedbacks=
        			checkFeedbacksForDeleteQuestion(questionToRemoveSectionOrder,questionToRemoveOrder);
           		section.removeQuestionOrder(questionToRemoveOrder);
           		
            	// Update random quantity if needed
            	if (section.getRandomQuantity()>section.getQuestionOrdersSize())
            	{
            		section.setRandomQuantity(section.getQuestionOrdersSize());
            		section.acceptRandomQuantity();
            		
        			// We need to update random quantity
        			updateSectionRandomQuantity(context.getViewRoot(),section);
            	}
            	
    			// If it is needed update feedbacks
    			if (!checkFeedbacks)
    			{
    				updateFeedbacksForDeleteQuestion(questionToRemoveSectionOrder,questionToRemoveOrder);
    			}
			}
		}
		return operation;
		
	}
	
	private Operation updateQuestionsForFeedbacks()
	{
		return updateQuestionsForFeedbacks(null);
	}
	
	private Operation updateQuestionsForFeedbacks(Operation operation)
	{
		List<SectionBean> sectionsToUpdate=new ArrayList<SectionBean>();
		for (TestFeedbackBean feedback:getFeedbacks())
		{
			if (TestFeedbackConditionBean.MARKS_UNIT.equals(feedback.getCondition().getUnit()))
			{
				SectionBean section=feedback.getCondition().getSection();
				if (section==null)
				{
					sectionsToUpdate.clear();
					for (SectionBean s:getSections())
					{
						sectionsToUpdate.add(s);
					}
					break;
				}
				else
				{
					if (!sectionsToUpdate.contains(section))
					{
						sectionsToUpdate.add(section);
					}
				}
			}
		}
		for (SectionBean sectionToUpdate:sectionsToUpdate)
		{
			operation=updateSectionQuestions(operation,sectionToUpdate);
		}
		return operation;
	}
	
	private boolean updateQuestion(Operation operation,SectionBean section,QuestionOrderBean questionOrder)
	{
		boolean ok=true;
		
		long questionId=questionOrder.getQuestionId();
		if (questionsService.checkQuestionId(operation,questionId))
		{
			Question questionFromDB=questionsService.getQuestion(getCurrentUserOperation(operation),questionId);
			if (questionFromDB==null)
			{
				ok=false;
			}
			else
			{
				questionOrder.getQuestion().setFromOtherQuestion(questionFromDB.getQuestionCopy());
			}
		}
		else
		{
			ok=false;
		}
		return ok;
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
   			
   			setFilterGlobalQuestionsEnabled(null);
   			setFilterOtherUsersQuestionsEnabled(null);
   			setUseGlobalQuestions(null);
   			setUseOtherUsersQuestions(null);
   			setViewQuestionsFromOtherUsersPrivateCategoriesEnabled(null);
   			setViewQuestionsFromAdminsPrivateCategoriesEnabled(null);
   			setViewQuestionsFromSuperadminsPrivateCategoriesEnabled(null);
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
   			if (filterCategory==null || !checkQuestionsFilterPermission(operation,filterCategory))
   			{
   				setFilterCategoryId(Long.MIN_VALUE);
   			}
   			
   			// Reload questions and questions categories
   			questions=null;
   			specialCategoriesFilters=null;
   			setQuestionsCategories(null);
   			
   			getQuestions(operation);
   			
   			// Get current user session Hibernate operation
   			operation=getCurrentUserOperation(null);
   			
   			getQuestionsCategories(operation);
   			getFilterCategoryId(operation);
   			setFilteredQuestionsDualList(null);
    		
    		// Get section to process if any
        	AccordionPanel sectionsAccordion=getSectionsAccordion(event.getComponent());
    		setCurrentSection(getSectionFromSectionsAccordion(sectionsAccordion));
    		
    		// We need to process some input fields
    		processSectionsInputFields(event.getComponent(),getCurrentSection());
    		
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
   				// Scroll page to top position
   				scrollToTop();
    			
   				// We need to update questions of current section
   				updateSectionQuestions(getCurrentSection());
   				
        		// We need to update sections text fields
        		updateSectionsTextFields(event.getComponent(),getSectionsSize());
   				
				// Restore old section name
    			getCurrentSection().setName(activeSectionName);
    		}
    	}
    	else if ("sectionWeight".equals(property))
    	{
			// We need to process weight
    		processSectionWeight(event.getComponent(),sectionChecked);
    		
    		// We need to stop this response because there is another one in process
    		FacesContext.getCurrentInstance().responseComplete();
    	}
    	else if ("randomQuantity".equals(property))
    	{
			// We need to process random quantity
    		processSectionRandomQuantity(event.getComponent(),sectionChecked);
    		
    		// We need to stop this response because there is another one in process
    		FacesContext.getCurrentInstance().responseComplete();
    	}
    	else if ("questionOrderWeight".equals(property))
    	{
    		// We need to process question orders weights
    		processQuestionOrderWeights(event.getComponent(),sectionChecked);
    		
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
        	if (questionsDualList!=null)
        	{
        		int order=currentSection.getQuestionOrdersSize()+1;
        		for (Question question:questionsDualList.getTarget())
        		{
        			currentSection.addQuestionOrder(order,question.getId());
        			order++;
        		}
        		setFilteredQuestionsDualList(null);
        		
        		// Update random quantity if needed
        		if (currentSection.getRandomQuantity()==0 && currentSection.getQuestionOrdersSize()>0)
        		{
        			currentSection.setRandomQuantity(1);
        			currentSection.acceptRandomQuantity();
        			
    				// We need to update random quantity
    				updateSectionRandomQuantity(event.getComponent(),currentSection);
        		}
        	}
        	
        	// We need to update questions of current section
    		updateSectionQuestions(currentSection);
        	
    		// We need to update sections text fields
    		updateSectionsTextFields(event.getComponent(),getSectionsSize());
    	}
    }
    
    /**
	 * Action listener for updating questions information if we cancel the changes within the dialog for 
	 * adding questions to a section. 
     * @param event Action event
     */
    public void cancelAddQuestions(ActionEvent event)
    {
    	// We need to update questions of current section
		updateSectionQuestions(getCurrentSection());
    	
		// We need to update sections text fields
		updateSectionsTextFields(event.getComponent(),getSectionsSize());
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
				User testAuthor=getAuthor();
				for (String requiredAuthorPermission:filter.requiredAuthorPermissions)
				{
					if (permissionsService.isDenied(operation,testAuthor,requiredAuthorPermission))
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
				// questions by global categories and that the test's author has permission to assign
				// questions of global categories to his/her tests
				ok=getFilterGlobalQuestionsEnabled(operation).booleanValue() && 
					getUseGlobalQuestions(operation).booleanValue();
			}
			else
			{
				// First we have to see if the category is owned by current user, 
				// if that is not the case we will need to perform aditional checks
				long currentUserId=userSessionService.getCurrentUserId();
				User testAuthor=getAuthor();
				User categoryUser=filterCategory.getUser();
				if (categoryUser.getId()!=currentUserId)
				{
					// We need to check that current user has permission to filter questions by categories 
					// of other users and that the category is owned question's author or he/she has
					// permission to assign questions from categories of other users to his/her tests
					if (getFilterOtherUsersQuestionsEnabled(operation).booleanValue() && 
						(testAuthor.equals(categoryUser) || 
						getUseOtherUsersQuestions(operation).booleanValue()))
					{
						// We have to see if this a public or a private category
						// Public categories doesn't need more checks
						// But private categories need aditional permissions
						Visibility privateVisibility=
							visibilitiesService.getVisibility(operation,"CATEGORY_VISIBILITY_PRIVATE");
						if (filterCategoryVisibility.getLevel()>=privateVisibility.getLevel())
						{
							// Finally we need to check that current user has permission to view questions 
							// from private categories of other users, and aditionally we need to check 
							// that current user has permission to view questions from private categories 
							// of administrators if the owner of the category is an administrator and 
							// to check that current user has permission to view questions from private categories 
							// of users with permission to improve permissions over its owned ones if the owner 
							// of the category has that permission (superadmin)
							boolean isAdmin=isAdmin(operation,categoryUser);
							boolean isSuperadmin=isSuperadmin(operation,categoryUser);
							ok=getViewQuestionsFromOtherUsersPrivateCategoriesEnabled(operation).booleanValue() && 
								(!isAdmin || 
								getViewQuestionsFromAdminsPrivateCategoriesEnabled(operation).booleanValue()) && 
								(!isSuperadmin || 
								getViewQuestionsFromSuperadminsPrivateCategoriesEnabled(operation).booleanValue());
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
					// test's author is also the current user or he/she has permission to assign questions 
					// of global categories to his/her tests
					ok=testAuthor.getId()==currentUserId || getUseOtherUsersQuestions(operation).booleanValue();
				}
			}
		}
		return ok;
	}
    
	/**
	 * Checks if deleting a question will affect to the feedbacks already defined.
	 * @param sectionOrder Position of section of the question to delete
	 * @param questionOrder Position of question to delete within section
	 * @return true if deleting a section won't affect to the feedbacks already defined, false otherwise
	 */
	private boolean checkFeedbacksForDeleteQuestion(int sectionOrder,int questionOrder)
	{
		boolean ok=true;
		int newTotalMarks=0;
		int newSectionMarks=0;
		
		ScoreType scoreType=getScoreType();
		if ("SCORE_TYPE_QUESTIONS".equals(scoreType.getType()))
		{
			for (SectionBean section:getSections())
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
			for (SectionBean section:getSections())
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
		
		for (TestFeedbackBean feedback:getFeedbacks())
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
	 * @param sectionOrder Position of section of the deleted question
	 * @param questionOrder Position of deleted question within section
	 */
	private void updateFeedbacksForDeleteQuestion(int sectionOrder,int questionOrder)
	{
		for (TestFeedbackBean feedback:getFeedbacks())
		{
			TestFeedbackConditionBean condition=feedback.getCondition();
			if ((condition.getSection()==null || condition.getSection().getOrder()==sectionOrder) && 
				condition.getUnit().equals(TestFeedbackConditionBean.MARKS_UNIT))
			{
				int maxValue=condition.getMaxConditionalValue();
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
						if (maxValue>0)
						{
							maxValue--;
						}
						else
						{
							StringBuffer newComparator=new StringBuffer(NumberComparator.GREATER_EQUAL);
							if ("F".equals(localizationService.getLocalizedMessage("SCORE_GEN")))
							{
								newComparator.append("_F");
							}
							condition.setNewComparator(newComparator.toString());
						}
					}
					if (condition.getConditionalCmp()>maxValue)
					{
						condition.setConditionalCmp(maxValue);
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
    		Operation operation=null;
    		
    		// Get section to process if any
        	AccordionPanel sectionsAccordion=getSectionsAccordion(event.getComponent());
        	SectionBean section=getSectionFromSectionsAccordion(sectionsAccordion);
    		
    		// We need to process some input fields
    		processSectionsInputFields(event.getComponent(),section);
    		
    		// Set back accordion row index -1
    		sectionsAccordion.setRowIndex(-1);
    		
    		boolean forceRemoveQuestion=true;
    		if (event.getComponent().getAttributes().get("questionOrder")!=null)
    		{
    			questionToRemoveSectionOrder=activeSectionIndex+1;
    			questionToRemoveOrder=((Integer)event.getComponent().getAttributes().get("questionOrder")).intValue();
    			forceRemoveQuestion=false;
    		}
    		
    		boolean questionExists=true;
			long questionToRemoveId=0L;
			for (QuestionOrderBean qob:section.getQuestionOrders())
			{
				if (qob.getOrder()==questionToRemoveOrder)
				{
					questionToRemoveId=qob.getQuestionId();
					break;
				}
			}
			if (questionToRemoveId>0L)
			{
				// End current user session Hibernate operation
				userSessionService.endCurrentUserOperation();
				
				// Get current user session Hibernate operation
				operation=getCurrentUserOperation(null);
				
				questionExists=questionsService.checkQuestionId(operation,questionToRemoveId);
			}
			else
			{
				questionExists=false;
			}
    		
    		// Check that current section name entered by user is valid
    		if (checkSectionName(section.getName()) && questionExists)
    		{
        		boolean checkFeedbacks=
        			checkFeedbacksForDeleteQuestion(questionToRemoveSectionOrder,questionToRemoveOrder);
            	if (forceRemoveQuestion || checkFeedbacks)
            	{
                   	// Remove question from section
               		section.removeQuestionOrder(questionToRemoveOrder);
               		
                	// Update random quantity if needed
                	if (section.getRandomQuantity()>section.getQuestionOrdersSize())
                	{
                		section.setRandomQuantity(section.getQuestionOrdersSize());
                		section.acceptRandomQuantity();
                		
            			// We need to update random quantity
            			updateSectionRandomQuantity(event.getComponent(),section);
                	}
                	
        			// If it is needed update feedbacks
        			if (!checkFeedbacks)
        			{
        				updateFeedbacksForDeleteQuestion(questionToRemoveSectionOrder,questionToRemoveOrder);
        			}
        			
        			// We need to update questions of current section
        			updateSectionQuestions(operation,section);
        			
        			// We need to update sections text fields
        			updateSectionsTextFields(event.getComponent(),getSectionsSize());
            	}
        		else
        		{
        			RequestContext rq=RequestContext.getCurrentInstance();
        			rq.execute("confirmDeleteQuestionDialog.show()");
        		}
    		}
    		else
    		{
    			// Scroll page to top position
    			scrollToTop();
    			
    			// We need to update questions of current sections
    			updateSectionQuestions(operation,section);
    			
    			// We need to update sections text fields
    			updateSectionsTextFields(event.getComponent(),getSectionsSize());
    			
    			// Restore old section name
    			section.setName(activeSectionName);
    		}
    	}
    	else if ("sectionWeight".equals(property))
    	{
			// We need to process weight
    		processSectionWeight(event.getComponent(),sectionChecked);
    		
    		// We need to stop this response because there is another one in process
    		FacesContext.getCurrentInstance().responseComplete();
    	}
    	else if ("randomQuantity".equals(property))
    	{
			// We need to process random quantity
    		processSectionRandomQuantity(event.getComponent(),sectionChecked);
    		
    		// We need to stop this response because there is another one in process
    		FacesContext.getCurrentInstance().responseComplete();
    	}
    	else if ("questionOrderWeight".equals(property))
    	{
    		// We need to process question orders weights
    		processQuestionOrderWeights(event.getComponent(),sectionChecked);
    		
    		// We need to stop this response because there is another one in process
    		FacesContext.getCurrentInstance().responseComplete();
    	}
	}
    
	/**
	 * Action listener for updating questions information if we cancel the changes within the dialog to confirm 
	 * question deletion. 
	 * @param event Action event
	 */
	public void cancelRemoveQuestion(ActionEvent event)
	{
		// We need to update questions of current section
		updateSectionQuestions(getActiveSection(event.getComponent()));
		
		// We need to update sections text fields
		updateSectionsTextFields(event.getComponent(),getSectionsSize());
	}
    
	private boolean checkSaveTest(Operation operation)
	{
		boolean ok=false;
		
		// Get current user session operation
		operation=getCurrentUserOperation(operation);
		
		if (getTestId()>0L)
		{
			ok=false;
			if (getEditEnabled(operation).booleanValue())
			{
				if (getAuthor().getId()==userSessionService.getCurrentUserId())
				{
					ok=true;
				}
				else
				{
					ok=getEditOtherUsersTestsEnabled(operation).booleanValue() && 
						(!getTestAuthorAdmin(operation).booleanValue() || 
						getEditAdminsTestsEnabled(operation).booleanValue()) && 
						(!getTestAuthorSuperadmin(operation).booleanValue() || 
						getEditSuperadminsTestsEnabled(operation).booleanValue());
				}
			}
		}
		else
		{
			ok=getAddEnabled(operation).booleanValue();
		}
		return ok;
	}
	
    //Guarda la prueba y devuelve el nombre de la vista siguiente
    //return Nombre de la siguiente vista: "tests"
    /**
	 * Save test to DB.
	 * @return Next wiew (tests page if save is sucessful, otherwise we keep actual view)
     */
    public String saveTest()
    {
    	String nextView=null;
    	
		// Get current user session Hibernate operation
		Operation operation=getCurrentUserOperation(null);
		
		setFilterGlobalTestsEnabled(null);
		setFilterOtherUsersTestsEnabled(null);
    	setGlobalOtherUserCategoryAllowed(null);
    	resetTestAuthorAdmin();
    	resetTestAuthorSuperadmin();
    	setAddEnabled(null);
    	setEditEnabled(null);
    	setEditOtherUsersTestsEnabled(null);
    	setEditAdminsTestsEnabled(null);
    	setEditSuperadminsTestsEnabled(null);
    	setViewQuestionsFromOtherUsersPrivateCategoriesEnabled(null);
    	setViewQuestionsFromAdminsPrivateCategoriesEnabled(null);
    	setViewQuestionsFromSuperadminsPrivateCategoriesEnabled(null);
    	
    	long testId=getTestId();
    	boolean update=testId>0L;
    	
    	if (update && !testsService.checkTestId(operation,testId))
    	{
    		displayErrorPage("TEST_UPDATE_NOT_FOUND_ERROR","The test you are trying to update no longer exists.");
    	}
    	else if (!checkSaveTest(operation) || (update && !checkCurrentCategory(operation)))
    	{
    		displayErrorPage("NON_AUTHORIZED_ACTION_ERROR","You are not authorized to execute that operation");
    	}
    	else
    	{
        	nextView="tests?faces-redirect=true";
			boolean reloadCategories=true;
        	if (update)
        	{
            	if (activeTestTabIndex==GENERAL_TABVIEW_TAB)
            	{
            		// Check test name
            		if (!checkTestName(getName()))
            		{
            			nextView=null;
            		}
            		
           			// Check calendar dates if needed
           			if (activeGeneralTabIndex==CALENDAR_TAB_GENERAL_ACCORDION && !checkCalendarInputFields())
           			{
           				nextView=null;
           			}
           		}
           		else if (activeTestTabIndex==SECTIONS_TABVIEW_TAB)
           		{
       				UIComponent viewRoot=FacesContext.getCurrentInstance().getViewRoot();
           			
       	    		// Get section to process if any
       	        	AccordionPanel sectionsAccordion=getSectionsAccordion(viewRoot);
       	        	SectionBean section=getSectionFromSectionsAccordion(sectionsAccordion);
       	    		
       	    		// We need to process some input fields
       	    		processSectionsInputFields(viewRoot,section);
       	    		
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
       					updateSectionsTextFields(viewRoot,getSectionsSize());
       					section.setName(activeSectionName);
      					
       					nextView=null;
       				}
           		}
       		}
            
            // Check test category
        	if (!categoriesService.checkCategoryId(operation,getCategoryId()))
        	{
        		addErrorMessage(update?"TEST_CATEGORY_UPDATE_NOT_FOUND":"TEST_CATEGORY_ADD_NOT_FOUND");
        		nextView=null;
        		
				// Refresh tests categories from DB
        		resetTestsCategories(operation);
        		
		    	// We also need to change active tab to display categories combo
		    	setNewActiveTestTab(GENERAL_WIZARD_TAB);
        	}
        	else if (checkCategory(operation))
        	{
        		if (!checkAvailableTestName(operation))
        		{
        			addErrorMessage("TEST_NAME_ALREADY_DECLARED");
        			nextView=null;
        		}
        	}
        	else
        	{
	    		addErrorMessage("TEST_CATEGORY_NOT_GRANTED_ERROR");
	    		nextView=null;
	    		
	    		setCategoryId(operation,0L);
	    		
				// Refresh tests categories from DB
		    	resetTestsCategories(operation);
		    	reloadCategories=false;
		    	
		    	// We also need to change active tab to display categories combo
		    	setNewActiveTestTab(GENERAL_WIZARD_TAB);
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
        			
    				// We need to remove added questions that had been deleted before saving
    				updateQuestions(writeOp,true);
    				
    		       	// Validate that all sections have at least one question
    		       	boolean sectionEmpty=false;
    		       	for (SectionBean section:getSections())
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
    	            	// Get test
    	            	Test test=getAsTest(getCurrentUserOperation(null));
    	        		if (update) // Update test
    	        		{
    	        			testsService.updateTest(writeOp,test);
    	        		}
    	        		else // New test
    	        		{
    	        			testsService.addTest(writeOp,test);
    	        		}
    	        		
    					// Do commit
    					writeOp.commit();
    		       	}
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
        			setTestsCategories(null);
        		}
        		
				// Reset user permissions
        		setFilterGlobalQuestionsEnabled(null);
        		setFilterOtherUsersQuestionsEnabled(null);
        		setUseGlobalQuestions(null);
        		setUseOtherUsersQuestions(null);
        		setViewQuestionsFromOtherUsersPrivateCategoriesEnabled(null);
        		setViewQuestionsFromAdminsPrivateCategoriesEnabled(null);
        		setViewQuestionsFromSuperadminsPrivateCategoriesEnabled(null);
           		resetAdmins();
           		resetSuperadmins();
        	}
    	}
    	return nextView;
	}
    
	private void setNewActiveTestTab(String newActiveTestTab)
	{
		if (!newActiveTestTab.equals(getActiveTestTabName()))
		{
			UIComponent viewRoot=FacesContext.getCurrentInstance().getViewRoot();
			if (getTestId()>0L)
			{
    			TabView testFormTabs=(TabView)viewRoot.findComponent(":testForm:testFormTabs");
    			activeTestTabIndex=getTestTabIndex(newActiveTestTab);
    			testFormTabs.setActiveIndex(activeTestTabIndex);
			}
			else
			{
	    		Wizard testFormWizard=(Wizard)viewRoot.findComponent(":testForm:testFormWizard");
	    		activeTestTabName=newActiveTestTab;
	    		testFormWizard.setStep(newActiveTestTab);
			}
		}
	}
    
    /**
     * Set test bean fields from a Test object.
     * @param test Test object 
     */
    public void setFromTest(Test test)
    {
    	setTestId(test.getId());
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
    	setFeedbackDisplaySummaryMarks(
    		test.isFeedbackDisplaySummary()?test.isFeedbackDisplaySummaryMarks():false);
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
    	List<UserGroupBean> testUsersGroups=new ArrayList<UserGroupBean>();
    	setTestUsersGroups(testUsersGroups);
    	List<UserGroupBean> testAdminsGroups=new ArrayList<UserGroupBean>();
    	setTestAdminsGroups(testAdminsGroups);
    	for (TestUser tu:test.getTestUsers())
    	{
    		if (tu.isOmUser())
    		{
    			User testUser=tu.getUser().getUserCopy();
    			testUsersGroups.add(new UserGroupBean(testUser));
    		}
    		if (tu.isOmAdmin())
    		{
    			User testAdmin=tu.getUser().getUserCopy();
    			testAdminsGroups.add(new UserGroupBean(testAdmin));
    		}
    	}
    	if (test.getUserGroups()!=null && !"".equals(test.getUserGroups()))
    	{
    		List<String> userGroups=new ArrayList<String>();
    		for (String userGroup:test.getUserGroups().split(Pattern.quote(";")))
    		{
    			if (!userGroups.contains(userGroup))
    			{
    				userGroups.add(userGroup);
    			}
    		}
    		Collections.sort(userGroups);
    		for (String userGroup:userGroups)
    		{
    			testUsersGroups.add(new UserGroupBean(usersService,userSessionService,userGroup));
    		}
    	}
    	if (test.getAdminGroups()!=null && !"".equals(test.getAdminGroups()))
    	{
    		List<String> adminGroups=new ArrayList<String>();
    		for (String adminGroup:test.getAdminGroups().split(Pattern.quote(";")))
    		{
    			if (!adminGroups.contains(adminGroup))
    			{
    				adminGroups.add(adminGroup);
    			}
    		}
    		Collections.sort(adminGroups);
    		for (String adminGroup:adminGroups)
    		{
    			testAdminsGroups.add(new UserGroupBean(usersService,userSessionService,adminGroup));
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
        Date timeCreated=getTimeCreated();
        test=new Test(getTestId(),getName(),getCategory(),getAuthor(),timeCreated==null?currentDate:timeCreated);
        test.setModifiedBy(userSessionService.getCurrentUser(operation));
        test.setTimeModified(currentDate);
        test.setTimeTestDeploy(getTimeTestDeploy());
        test.setTimeDeployDeploy(getTimeDeployDeploy());
        test.setDescription(getDescription());
        test.setTitle(getTitle());
        test.setAssessement(getAssessement());
        test.setScoreType(getScoreType());
        test.setAllUsersAllowed(isAllUsersAllowed());
        test.setAllowAdminReports(isAllowAdminReports());
        String startDateHidden=getStartDateHidden();
        String closeDateHidden=getCloseDateHidden();
        String warningDateHidden=getWarningDateHidden();
        String feedbackDateHidden=getFeedbackDateHidden();
        boolean restrictDates=isRestrictDates();
        test.setStartDate(restrictDates?getStartDate():null);
        test.setCloseDate(restrictDates?getCloseDate():null);
        test.setWarningDate(restrictDates?getWarningDate():null);
        test.setFeedbackDate(isRestrictFeedbackDate()?getFeedbackDate():null);
        setStartDateHidden(startDateHidden);
        setCloseDateHidden(closeDateHidden);
        setWarningDateHidden(warningDateHidden);
        setFeedbackDateHidden(feedbackDateHidden);
        test.setFreeSummary(isFreeSummary());
        test.setFreeStop(isFreeStop());
        test.setSummaryQuestions(isSummaryQuestions());
        test.setSummaryScores(isSummaryScores());
        test.setSummaryAttempts(isSummaryAttempts());
        boolean navigation=isNavigation();
        test.setNavigation(navigation);
        test.setNavLocation(getNavLocation());
        test.setRedoQuestion(getRedoQuestion());
        test.setRedoTest(isRedoTest());
        test.setPresentationTitle(getPresentationTitle());
        test.setPresentation(getPresentation());
        test.setPreliminarySummaryTitle(navigation?getPreliminarySummaryTitle():"");
        test.setPreliminarySummaryButton(navigation?getPreliminarySummaryButton():"");
        test.setPreliminarySummary(navigation?getPreliminarySummary():"");
        boolean feedbackDisplaySummary=isFeedbackDisplaySummary();
        test.setFeedbackDisplaySummary(feedbackDisplaySummary);
        test.setFeedbackDisplaySummaryMarks(feedbackDisplaySummary?isFeedbackDisplaySummaryMarks():false);
        test.setFeedbackDisplaySummaryAttempts(feedbackDisplaySummary?isFeedbackDisplaySummaryAttempts():true);
       	test.setFeedbackSummaryPrevious(feedbackDisplaySummary?getFeedbackSummaryPrevious():"");
       	boolean feedbackDisplayScores=isFeedbackDisplayScores();
       	test.setFeedbackDisplayScores(feedbackDisplayScores);
       	test.setFeedbackDisplayScoresMarks(feedbackDisplayScores?isFeedbackDisplayScoresMarks():false);
       	test.setFeedbackDisplayScoresPercentages(feedbackDisplayScores?isFeedbackDisplayScoresPercentages():true);
       	test.setFeedbackScoresPrevious(feedbackDisplayScores?getFeedbackScoresPrevious():"");
       	test.setFeedbackAdvancedPrevious(getFeedbackAdvancedPrevious());
       	test.setFeedbackAdvancedNext(getFeedbackAdvancedNext());
   		
        // Users and groups with permission to do/administrate this test
       	StringBuffer sGroups=new StringBuffer();
        for (UserGroupBean testUserGroup:getTestUsersGroups())
        {
        	if (testUserGroup.isTestUser())
        	{
        		User testUser=testUserGroup.getUser();
            	TestUser tu=null;
            	TestUser testUserFromDB=testUsersService.getTestUser(operation,test,testUser);
            	if (testUserFromDB==null)
            	{
            		tu=new TestUser(0L,test,testUser,true,false);
            	}
            	else
            	{
            		tu=testUserFromDB.getTestUserCopy();
            		tu.setOmUser(true);
            		tu.setOmAdmin(false);
            	}
            	test.getTestUsers().add(tu);
        	}
        	else
        	{
        		String userGroup=testUserGroup.getGroup();
        		if (checkUserGroup(userGroup,false))
        		{
            		if (sGroups.length()>0)
            		{
            			sGroups.append(';');
            		}
            		sGroups.append(testUserGroup.getGroup());
        		}
        	}
        }
        test.setUserGroups(sGroups.toString());
        sGroups.setLength(0);
       	for (UserGroupBean testAdminGroup:getTestAdminsGroups())
       	{
       		if (testAdminGroup.isTestUser())
       		{
       			User testAdmin=testAdminGroup.getUser();
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
               			ta=testAdminFromDB.getTestUserCopy();
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
       		else
       		{
       			String adminGroup=testAdminGroup.getGroup();
       			if (checkUserGroup(adminGroup,false))
       			{
           			if (sGroups.length()>0)
           			{
           				sGroups.append(';');
           			}
           			sGroups.append(adminGroup);
       			}
       		}
       	}
       	test.setAdminGroups(sGroups.toString());
    	
    	// Sections (note that it is important to initialize sections before feedbacks)
    	for (SectionBean section:getSections())
    	{
    		test.getSections().add(section.getAsSection(test));
    	}
    	
    	// Support contacts
    	for (SupportContactBean supportContact:getSupportContacts())
    	{
    		test.getSupportContacts().add(supportContact.getAsSupportContact(test));
    	}
    	
    	// Evaluators
    	for (EvaluatorBean evaluator:getEvaluators())
    	{
    		test.getEvaluators().add(evaluator.getAsEvaluator(test));
    	}
    	
    	// Feedbacks (be careful that sections must be initialized before initializing feedbacks)
    	for (TestFeedbackBean testFeedback:getFeedbacks())
    	{
    		test.getTestFeedbacks().add(testFeedback.getAsTestFeedback(test));
    	}
    	return test;
    }
    
    /**
     * Initialize some test fields.
     */
    private void initializeTest()
    {
		// Note that it is important to set this flag first to avoid infinite recursion
    	testInitialized=true;
    	
		// End current user session Hibernate operation
		userSessionService.endCurrentUserOperation();
		
		// Get current user session Hibernate operation
		Operation operation=getCurrentUserOperation(null);
    	
    	FacesContext context=FacesContext.getCurrentInstance();
    	Map<String,String> params=context.getExternalContext().getRequestParameterMap();
    	if (params.containsKey("testId")) // Edit test
    	{
    		setFromTest(testsService.getTest(operation,Long.parseLong(params.get("testId"))));
    		initialCategoryId=getCategoryId();
    		
    		// We need to initialize special categories filters for the questions within sections tab
			List<String> allCategoriesPermissions=new ArrayList<String>();
			allCategoriesPermissions.add("PERMISSION_QUESTIONS_GLOBAL_FILTER_ENABLED");
			allCategoriesPermissions.add("PERMISSION_QUESTIONS_OTHER_USERS_FILTER_ENABLED");
			List<String> allCategoriesAuthorPermissions=new ArrayList<String>();
			allCategoriesAuthorPermissions.add("PERMISSION_TEST_USE_GLOBAL_QUESTIONS");
			allCategoriesAuthorPermissions.add("PERMISSION_TEST_USE_OTHER_USERS_QUESTIONS");
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
    		specialCategoryFiltersMap.put(Long.valueOf(ALL_EVEN_PRIVATE_CATEGORIES.id),ALL_EVEN_PRIVATE_CATEGORIES);
    		if (getAuthor().getId()==userSessionService.getCurrentUserId())
    		{
    			specialCategoryFiltersMap.put(
    				Long.valueOf(ALL_MY_CATEGORIES_FOR_TEST_AUTHOR.id),ALL_MY_CATEGORIES_FOR_TEST_AUTHOR);
        		specialCategoryFiltersMap.put(Long.valueOf(ALL_MY_CATEGORIES_EXCEPT_GLOBALS_FOR_TEST_AUTHOR.id),
        			ALL_MY_CATEGORIES_EXCEPT_GLOBALS_FOR_TEST_AUTHOR);
    		}
    		else
    		{
    			specialCategoryFiltersMap.put(Long.valueOf(ALL_MY_CATEGORIES.id),ALL_MY_CATEGORIES);
    			specialCategoryFiltersMap.put(
    				Long.valueOf(ALL_MY_CATEGORIES_EXCEPT_GLOBALS.id),ALL_MY_CATEGORIES_EXCEPT_GLOBALS);
    			specialCategoryFiltersMap.put(
    				Long.valueOf(ALL_TEST_AUTHOR_CATEGORIES.id),ALL_TEST_AUTHOR_CATEGORIES);
    			specialCategoryFiltersMap.put(Long.valueOf(ALL_TEST_AUTHOR_CATEGORIES_EXCEPT_GLOBALS.id),
    				ALL_TEST_AUTHOR_CATEGORIES_EXCEPT_GLOBALS);
    		}
    		specialCategoryFiltersMap.put(Long.valueOf(ALL_GLOBAL_CATEGORIES.id),ALL_GLOBAL_CATEGORIES);
    		specialCategoryFiltersMap.put(
    			Long.valueOf(ALL_PRIVATE_CATEGORIES_OF_OTHER_USERS.id),ALL_PRIVATE_CATEGORIES_OF_OTHER_USERS);
    		specialCategoryFiltersMap.put(
    			Long.valueOf(ALL_CATEGORIES_OF_OTHER_USERS.id),ALL_CATEGORIES_OF_OTHER_USERS);
    	}
    	else if (params.containsKey("testCopyId")) // Create copy of an existing test
    	{
	    	Test testFrom=testsService.getTest(operation,Long.parseLong(params.get("testCopyId")));
    		
	    	// Copy data from test to a new instance
	    	Test test=testFrom.getTestCopy();
	    	
	    	// Add sections
	    	Map<Section,Section> sectionsFromTo=new HashMap<Section,Section>();
	    	test.setSections(new ArrayList<Section>());
	    	for (Section sectionFrom:testFrom.getSections())
	    	{
	    		// Copy data from section
	    		Section section=sectionFrom.getSectionCopy();
	    		
	    		// Add question orders
	    		section.setQuestionOrders(new ArrayList<QuestionOrder>());
	    		for (QuestionOrder questionOrderFrom:sectionFrom.getQuestionOrders())
	    		{
	    			// Copy data from question order
	    			QuestionOrder questionOrder=questionOrderFrom.getQuestionOrderCopy();
	    			
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
    			// Copy data from feedback
    			TestFeedback testFeedback=testFeedbackFrom.getTestFeedbackCopy();
    			
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
    			// Copy data from support contact
    			SupportContact supportContact=supportContactFrom.getSupportContactCopy();
    			
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
    			// Copy data from evaluator
	    		Evaluator evaluator=evaluatorFrom.getEvaluatorCopy();
    			
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
    			// Copy data from test user
	    		TestUser testUser=testUserFrom.getTestUserCopy();
    			
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
    		if (checkCategory(operation))
    		{
    			initialCategoryId=getCategoryId();
    		}
    		else
    		{
        		List<Category> categories=getTestsCategories(operation);
    			setCategory(categories.isEmpty()?null:categories.get(0));
    			initialCategoryId=0L;
    		}
    		
       		// We need to initialize special categories filters for the questions within sections tab
			List<String> allCategoriesPermissions=new ArrayList<String>();
			allCategoriesPermissions.add("PERMISSION_QUESTIONS_GLOBAL_FILTER_ENABLED");
			allCategoriesPermissions.add("PERMISSION_QUESTIONS_OTHER_USERS_FILTER_ENABLED");
			List<String> allCategoriesAuthorPermissions=new ArrayList<String>();
			allCategoriesAuthorPermissions.add("PERMISSION_TEST_USE_GLOBAL_QUESTIONS");
			allCategoriesAuthorPermissions.add("PERMISSION_TEST_USE_OTHER_USERS_QUESTIONS");
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
   			specialCategoryFiltersMap.put(
   				Long.valueOf(ALL_MY_CATEGORIES_FOR_TEST_AUTHOR.id),ALL_MY_CATEGORIES_FOR_TEST_AUTHOR);
   			specialCategoryFiltersMap.put(Long.valueOf(ALL_MY_CATEGORIES_EXCEPT_GLOBALS_FOR_TEST_AUTHOR.id),
    			ALL_MY_CATEGORIES_EXCEPT_GLOBALS_FOR_TEST_AUTHOR);
    		specialCategoryFiltersMap.put(Long.valueOf(ALL_GLOBAL_CATEGORIES.id),ALL_GLOBAL_CATEGORIES);
    		specialCategoryFiltersMap.put(
    			Long.valueOf(ALL_PRIVATE_CATEGORIES_OF_OTHER_USERS.id),ALL_PRIVATE_CATEGORIES_OF_OTHER_USERS);
    		specialCategoryFiltersMap.put(
    			Long.valueOf(ALL_CATEGORIES_OF_OTHER_USERS.id),ALL_CATEGORIES_OF_OTHER_USERS);
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
        	initialCategoryId=0L;
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
        	setTestUsersGroups(new ArrayList<UserGroupBean>());
        	
           	// Users with permission to administrate this test
        	List<UserGroupBean> testAdminsGroups=new ArrayList<UserGroupBean>();
           	setTestAdminsGroups(testAdminsGroups);
           	testAdminsGroups.add(new UserGroupBean(currentUser));
        	
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
			List<String> allCategoriesPermissions=new ArrayList<String>();
			allCategoriesPermissions.add("PERMISSION_QUESTIONS_GLOBAL_FILTER_ENABLED");
			allCategoriesPermissions.add("PERMISSION_QUESTIONS_OTHER_USERS_FILTER_ENABLED");
			List<String> allCategoriesAuthorPermissions=new ArrayList<String>();
			allCategoriesAuthorPermissions.add("PERMISSION_TEST_USE_GLOBAL_QUESTIONS");
			allCategoriesAuthorPermissions.add("PERMISSION_TEST_USE_OTHER_USERS_QUESTIONS");
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
   			specialCategoryFiltersMap.put(
   				Long.valueOf(ALL_MY_CATEGORIES_FOR_TEST_AUTHOR.id),ALL_MY_CATEGORIES_FOR_TEST_AUTHOR);
   			specialCategoryFiltersMap.put(Long.valueOf(ALL_MY_CATEGORIES_EXCEPT_GLOBALS_FOR_TEST_AUTHOR.id),
    			ALL_MY_CATEGORIES_EXCEPT_GLOBALS_FOR_TEST_AUTHOR);
    		specialCategoryFiltersMap.put(Long.valueOf(ALL_GLOBAL_CATEGORIES.id),ALL_GLOBAL_CATEGORIES);
    		specialCategoryFiltersMap.put(
    			Long.valueOf(ALL_PRIVATE_CATEGORIES_OF_OTHER_USERS.id),ALL_PRIVATE_CATEGORIES_OF_OTHER_USERS);
    		specialCategoryFiltersMap.put(
    			Long.valueOf(ALL_CATEGORIES_OF_OTHER_USERS.id),ALL_CATEGORIES_OF_OTHER_USERS);
    	}
    	setCurrentSection(getSections().get(0));
    }
    
	/**
	 * Checks if changing a property of a section will affect to the feedbacks already defined.
	 * @return true if changing a property of a section won't affect to the feedbacks already defined, 
	 * false otherwise
	 */
	private boolean checkFeedbacksForChangeProperty()
	{
		boolean ok=true;
		
		for (TestFeedbackBean feedback:getFeedbacks())
		{
			if (feedback.getCondition().getUnit().equals(TestFeedbackConditionBean.MARKS_UNIT))
			{
				int maxConditionalValue=feedback.getCondition().getMaxConditionalValue();
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
	 */
	private void updateFeedbacksForChangeProperty()
	{
		for (TestFeedbackBean feedback:getFeedbacks())
		{
			TestFeedbackConditionBean condition=feedback.getCondition();
			if (condition.getUnit().equals(TestFeedbackConditionBean.MARKS_UNIT))
			{
				int maxValue=condition.getMaxConditionalValue();
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
						if (maxValue>0)
						{
							maxValue--;
						}
						else
						{
							StringBuffer newComparator=new StringBuffer(NumberComparator.GREATER_EQUAL);
							if ("F".equals(localizationService.getLocalizedMessage("SCORE_GEN")))
							{
								newComparator.append("_F");
							}
							condition.setNewComparator(newComparator.toString());
						}
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
	 * @param component Component that triggered the listener that called this method
	 * @param section Section
	 */
	private void processSectionShuffle(UIComponent component,SectionBean section)
	{
		List<String> exceptions=new ArrayList<String>();
		exceptions.add("sectionName");
		exceptions.add("sectionTitle");
		exceptions.add("sectionWeight");
		exceptions.add("random");
		exceptions.add("randomQuantity");
		exceptions.add("questionOrderWeight");
		processSectionsInputFields(component,section,exceptions);
	}
	
	/**
	 * Process a checkbox (random) of a section  within the sections tab of a test.
	 * @param component Component that triggered the listener that called this method
	 * @param section Section
	 */
	private void processSectionRandom(UIComponent component,SectionBean section)
	{
		List<String> exceptions=new ArrayList<String>();
		exceptions.add("sectionName");
		exceptions.add("sectionTitle");
		exceptions.add("sectionWeight");
		exceptions.add("shuffle");
		exceptions.add("randomQuantity");
		exceptions.add("questionOrderWeight");
		processSectionsInputFields(component,section,exceptions);
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
			boolean ok=true;
			boolean needConfirm=false;
			String property=(String)event.getComponent().getAttributes().get("property");
			if ("scoreType".equals(property))
			{
				needConfirm=!checkFeedbacksForChangeProperty();
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
					processSectionsInputFields(event.getComponent(),sectionChecked,exceptions);
					
					// Check that current section name entered by user is valid
					if (checkSectionName(sectionChecked.getName()))
					{
						boolean checkFeedbacks=sectionChecked.getWeight()>=getMinWeight() && 
							sectionChecked.getWeight()<=getMaxWeight();
						if (checkFeedbacks)
						{
							checkFeedbacks=checkFeedbacksForChangeProperty();
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
								updateSectionWeight(event.getComponent(),sectionChecked);
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
						processSectionRandomQuantity(event.getComponent(),section);
						
			    		section.setRandom(false);
		    		}
		    		else
		    		{
		    			setEnabledCheckboxesSetters(true);
		    			
		    			// Process the checkbox to shuffle questions within a section 
		    			// (listener will be invoked again)
		    			processSectionShuffle(event.getComponent(),section);
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
							processSectionRandomQuantity(event.getComponent(),section);
						}
						
						if (!(section.isRandom() && checkFeedbacksForChangeProperty()) || 
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
							updateSectionRandomQuantity(event.getComponent(),section);
						}
					}
					else
					{
						setEnabledCheckboxesSetters(true);
						
		    			// Process the checkbox to select randomly the questions to display within a section 
		    			// (listener will be invoked again)
						processSectionRandom(event.getComponent(),section);
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
					processSectionsInputFields(event.getComponent(),sectionChecked,exceptions);
					
					// Check that current section name entered by user is valid
					if (checkSectionName(sectionChecked.getName()))
					{
						boolean checkFeedbacks=sectionChecked.getRandomQuantity()>0 && 
							sectionChecked.getRandomQuantity()<=sectionChecked.getQuestionOrdersSize();
						if (checkFeedbacks)
						{
							checkFeedbacks=checkFeedbacksForChangeProperty();
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
								updateSectionRandomQuantity(event.getComponent(),sectionChecked);
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
								updateSectionRandomQuantity(event.getComponent(),sectionChecked);
							}
							else if (sectionChecked.getRandomQuantity()>sectionChecked.getQuestionOrdersSize())
							{
								sectionChecked.setRandomQuantity(sectionChecked.getQuestionOrdersSize());
								
								// We need to update random quantity
								updateSectionRandomQuantity(event.getComponent(),sectionChecked);
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
					processSectionsInputFields(event.getComponent(),sectionChecked,exceptions);
					
					// Check that current section name entered by user is valid
					if (checkSectionName(sectionChecked.getName()))
					{
						boolean checkFeedbacks=questionOrderChecked.getWeight()>=getMinWeight() &&
							questionOrderChecked.getWeight()<=getMaxWeight();
						if (checkFeedbacks)
						{
							checkFeedbacks=checkFeedbacksForChangeProperty();
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
								updateQuestionOrderWeights(event.getComponent(),sectionChecked);
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
			// We need to update feedbacks to valid values
			updateFeedbacksForChangeProperty();
			
			if ("sectionWeight".equals(property))
			{
				// We need to update section weights
				updateSectionWeights(event.getComponent(),sectionChecked);
				sectionChecked.acceptWeight();
			}
			else if ("randomQuantity".equals(property))
			{
				// We need to update section random quantity
				updateSectionRandomQuantity(event.getComponent(),sectionChecked);
				sectionChecked.acceptRandomQuantity();
			}
			else if ("questionOrderWeight".equals(property))
			{
				// We need to update questions weights
				updateQuestionOrderWeights(event.getComponent(),sectionChecked);
				for (QuestionOrderBean questionOrder:sectionChecked.getQuestionOrders())
				{
					questionOrder.acceptWeight();
				}
			}
			
			updateQuestions();
			
			// Reset property checked
			setPropertyChecked(null);
			sectionChecked=null;
			questionOrderChecked=null;
			
			if (nextSectionIndexOnChangePropertyConfirm!=-1)
			{
				activeSectionIndex=nextSectionIndexOnChangePropertyConfirm;
				activeSectionName=getSection(nextSectionIndexOnChangePropertyConfirm+1).getName();
				refreshActiveSection(event.getComponent());
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
				
				updateQuestions();
			}
			else if ("sectionWeight".equals(property))
			{
				if (sectionChecked!=null)
				{
					sectionChecked.rollbackWeight();
					
					updateQuestions();
					
					// We need to update section weights
					updateSectionWeights(event.getComponent(),sectionChecked);
					sectionChecked.acceptWeight();
				}
			}
			else if ("randomQuantity".equals(property))
			{
				if (sectionChecked!=null)
				{
					sectionChecked.rollbackRandomQuantity();
					
					updateQuestions();
					
					// We need to update random quantity
					updateSectionRandomQuantity(event.getComponent(),sectionChecked);
					sectionChecked.acceptRandomQuantity();
				}
			}
			else if ("questionOrderWeight".equals(property))
			{
				if (sectionChecked!=null && questionOrderChecked!=null)
				{
					questionOrderChecked.rollbackWeight();
					
					updateQuestions();
					
					// We need to update questions weights
					updateQuestionOrderWeights(event.getComponent(),sectionChecked);
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
    private Question getQuestion(Operation operation,long questionId)
    {
    	return questionsService.getQuestion(getCurrentUserOperation(operation),questionId);
    }
    
    /**
     * @return Questions used in this test
     */
    public List<Question> getUsedQuestions()
    {
    	List<Question> usedQuestions=new ArrayList<Question>();
    	for (SectionBean section:getSections())
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
			// End current user session Hibernate operation
			userSessionService.endCurrentUserOperation();
			
			// Get current user session Hibernate operation
			operation=getCurrentUserOperation(null);
			
    		try
    		{
    			User testAuthor=getAuthor();
    			
    			if (checkQuestionsFilterPermission(operation,null))   
    			{
    				long filterCategoryId=getFilterCategoryId(operation);
    				if (specialCategoryFiltersMap.containsKey(Long.valueOf(filterCategoryId)))
    				{
    					SpecialCategoryFilter filter=specialCategoryFiltersMap.get(Long.valueOf(filterCategoryId));
    					if (allCategories.equals(filter))
    					{
    						questions=questionsService.getAllVisibleCategoriesQuestions(
    							operation,null,getFilterQuestionType(),getFilterQuestionLevel());
    					}
    					else if (ALL_EVEN_PRIVATE_CATEGORIES.equals(filter))
    					{
    						questions=questionsService.getAllCategoriesQuestions(
    							operation,null,getFilterQuestionType(),getFilterQuestionLevel());
    					}
    					else if (ALL_MY_CATEGORIES.equals(filter))
    					{
    						questions=questionsService.getAllMyCategoriesQuestions(
       							operation,null,getFilterQuestionType(),getFilterQuestionLevel());
    					}
    					else if (ALL_MY_CATEGORIES_EXCEPT_GLOBALS.equals(filter))
    					{
    						questions=questionsService.getAllMyCategoriesExceptGlobalsQuestions(
    							operation,null,getFilterQuestionType(),getFilterQuestionLevel());
    					}
    					else if (ALL_TEST_AUTHOR_CATEGORIES.equals(filter))
    					{
    						questions=questionsService.getAllUserCategoriesQuestions(
    							operation,testAuthor,null,getFilterQuestionType(),getFilterQuestionLevel());
    					}
    					else if (ALL_TEST_AUTHOR_CATEGORIES_EXCEPT_GLOBALS.equals(filter))
    					{
    						questions=questionsService.getAllUserCategoriesExceptGlobalsQuestions(
    							operation,testAuthor,null,getFilterQuestionType(),getFilterQuestionLevel());
    					}
    					else if (ALL_GLOBAL_CATEGORIES.equals(filter))
    					{
    						questions=questionsService.getAllGlobalCategoriesQuestions(
    							operation,null,getFilterQuestionType(),getFilterQuestionLevel());
    					}
    					else if (ALL_PUBLIC_CATEGORIES_OF_OTHER_USERS.equals(filter))
    					{
    						questions=questionsService.getAllPublicCategoriesOfOtherUsersQuestions(
    							operation,null,getFilterQuestionType(),getFilterQuestionLevel());
    					}
    					else if (ALL_PRIVATE_CATEGORIES_OF_OTHER_USERS.equals(filter))
    					{
    						questions=questionsService.getAllPrivateCategoriesOfOtherUsersQuestions(
    							operation,null,getFilterQuestionType(),getFilterQuestionLevel());
    					}
    					else if (ALL_CATEGORIES_OF_OTHER_USERS.equals(filter))
    					{
    						questions=questionsService.getAllCategoriesOfOtherUsersQuestions(
    							operation,null,getFilterQuestionType(),getFilterQuestionLevel());
    					}
    				}
    				else
    				{
    					questions=questionsService.getQuestions(operation,null,filterCategoryId,
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
    						checkQuestionCategory=checkQuestionsFilterPermission(operation,questionCategory); 
    						questionsCategories.put(questionCategory,checkQuestionCategory);
    					}
    					if (!checkQuestionCategory)
    					{
    						questionsToRemove.add(question);
    					}
    				}
    				for (Question questionToRemove:questionsToRemove)
    				{
    					questions.remove(questionToRemove);
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
				filteredUsersForAddingUsers=usersService.getSortedUsers(
					operation,getFilterUsersUserTypeId(),isFilterUsersIncludeOmUsers());
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
				filteredUsersForAddingAdmins=usersService.getSortedUsers(
					operation,getFilterAdminsUserTypeId(),isFilterAdminsIncludeOmUsers());
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
					getFilterSupportContactFilterUsersUserTypeId(),
					isFilterSupportContactFilterUsersIncludeOmUsers());
			}
			if (!isAllUsersAllowed())
			{
				if (groupUsersMap==null)
				{
					groupUsersMap=new HashMap<String,List<User>>();
				}
				List<User> testUsers=new ArrayList<User>();
				for (UserGroupBean testUserGroup:getTestUsersGroups())
				{
					if (testUserGroup.isTestUser())
					{
						User user=testUserGroup.getUser();
						if (!testUsers.contains(user))
						{
							testUsers.add(user);
						}
					}
					else
					{
						List<User> groupUsers=groupUsersMap.get(testUserGroup.getGroup());
						if (groupUsers==null)
						{
							groupUsers=usersService.getUsersWithGroup(operation,testUserGroup.getGroup());
							groupUsersMap.put(testUserGroup.getGroup(),groupUsers);
						}
						for (User groupUser:groupUsers)
						{
							if (!testUsers.contains(groupUser))
							{
								testUsers.add(groupUser);
							}
						}
					}
				}
				List<User> testAdmins=new ArrayList<User>();
				for (UserGroupBean testAdminGroup:getTestAdminsGroups())
				{
					if (testAdminGroup.isTestUser())
					{
						User admin=testAdminGroup.getUser();
						if (!testAdmins.contains(admin))
						{
							testAdmins.add(admin);
						}
					}
					else
					{
						List<User> groupAdmins=groupUsersMap.get(testAdminGroup.getGroup());
						if (groupAdmins==null)
						{
							groupAdmins=usersService.getUsersWithGroup(operation,testAdminGroup.getGroup());
							groupUsersMap.put(testAdminGroup.getGroup(),groupAdmins);
						}
						for (User groupAdmin:groupAdmins)
						{
							if (!testAdmins.contains(groupAdmin))
							{
								testAdmins.add(groupAdmin);
							}
						}
					}
				}
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
				filteredUsersForAddingEvaluatorFilterUsers=usersService.getSortedUsersWithoutUserType(
					operation,isFilterEvaluatorFilterUsersIncludeOmUsers());
			}
			else
			{
				filteredUsersForAddingEvaluatorFilterUsers=usersService.getSortedUsers(operation,
					getFilterEvaluatorFilterUsersUserTypeId(),isFilterEvaluatorFilterUsersIncludeOmUsers());
			}
			
			if (!isAllUsersAllowed())
			{
				if (groupUsersMap==null)
				{
					groupUsersMap=new HashMap<String,List<User>>();
				}
				List<User> testUsers=new ArrayList<User>();
				for (UserGroupBean testUserGroup:getTestUsersGroups())
				{
					if (testUserGroup.isTestUser())
					{
						User user=testUserGroup.getUser();
						if (!testUsers.contains(user))
						{
							testUsers.add(user);
						}
					}
					else
					{
						List<User> groupUsers=groupUsersMap.get(testUserGroup.getGroup());
						if (groupUsers==null)
						{
							groupUsers=usersService.getUsersWithGroup(operation,testUserGroup.getGroup());
							groupUsersMap.put(testUserGroup.getGroup(),groupUsers);
						}
						for (User groupUser:groupUsers)
						{
							if (!testUsers.contains(groupUser))
							{
								testUsers.add(groupUser);
							}
						}
					}
				}
				List<User> testAdmins=new ArrayList<User>();
				for (UserGroupBean testAdminGroup:getTestAdminsGroups())
				{
					if (testAdminGroup.isTestUser())
					{
						User admin=testAdminGroup.getUser();
						if (!testAdmins.contains(admin))
						{
							testAdmins.add(testAdminGroup.getUser());
						}
					}
					else
					{
						List<User> groupAdmins=groupUsersMap.get(testAdminGroup.getGroup());
						if (groupAdmins==null)
						{
							groupAdmins=usersService.getUsersWithGroup(operation,testAdminGroup.getGroup());
							groupUsersMap.put(testAdminGroup.getGroup(),groupAdmins);
						}
						for (User groupAdmin:groupAdmins)
						{
							if (!testAdmins.contains(groupAdmin))
							{
								testAdmins.add(groupAdmin);
							}
						}
					}
				}
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
	
	public String getUserGroup()
	{
		return userGroup;
	}
	
	public void setUserGroup(String userGroup)
	{
		this.userGroup=userGroup;
	}
	
	public boolean isUserGroupsDialogDisplayed()
	{
		return userGroupsDialogDisplayed;
	}
	
	public void setUserGroupsDialogDisplayed(boolean userGroupsDialogDisplayed)
	{
		this.userGroupsDialogDisplayed=userGroupsDialogDisplayed;
	}
	
	public String getAdminGroup()
	{
		return adminGroup;
	}
	
	public void setAdminGroup(String adminGroup)
	{
		this.adminGroup=adminGroup;
	}
	
	public boolean isAdminGroupsDialogDisplayed()
	{
		return adminGroupsDialogDisplayed;
	}
	
	public void setAdminGroupsDialogDisplayed(boolean adminGroupsDialogDisplayed)
	{
		this.adminGroupsDialogDisplayed=adminGroupsDialogDisplayed;
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
	 * @return Users and groups with permission to do this test
	 */
	public List<UserGroupBean> getTestUsersGroups()
	{
    	if (!testInitialized)
    	{
    		initializeTest();
    	}
    	return testUsersGroups;
	}
	
	/**
	 * @param testUsersGroups Users and groups with permission to do this test
	 */
	public void setTestUsersGroups(List<UserGroupBean> testUsersGroups)
	{
		this.testUsersGroups=testUsersGroups;
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
		
		List<User> testUsers=new ArrayList<User>();
		for (UserGroupBean testUserGroup:getTestUsersGroups())
		{
			if (testUserGroup.isTestUser())
			{
				testUsers.add(testUserGroup.getUser());
			}
		}
		for (User user:getFilteredUsersForAddingUsers(operation))
		{
			if (!testUsers.contains(user))
			{
				User availableUser=user.getUserCopy();
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
	 * @return Users and groups with permission to administrate this test
	 */
	public List<UserGroupBean> getTestAdminsGroups()
	{
    	if (!testInitialized)
    	{
    		initializeTest();
    	}
    	return testAdminsGroups;
	}
	
	/**
	 * @param testAdminsGroups Users and groups with permission to administrate this test
	 */
	public void setTestAdminsGroups(List<UserGroupBean> testAdminsGroups)
	{
		this.testAdminsGroups=testAdminsGroups;
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
		
		List<User> testAdmins=new ArrayList<User>();
		for (UserGroupBean testAdminGroup:getTestAdminsGroups())
		{
			if (testAdminGroup.isTestUser())
			{
				testAdmins.add(testAdminGroup.getUser());
			}
		}
		for (User user:getFilteredUsersForAddingAdmins(operation))
		{
			if (!testAdmins.contains(user))
			{
				User availableAdmin=user.getUserCopy();
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
    	boolean filterCategoryNotFound=false;
    	boolean filterCategoryInvalid=false;
		
    	// Get current user session Hibernate operation
		Operation operation=getCurrentUserOperation(null);
    	
   		setFilterGlobalQuestionsEnabled(null);
   		setFilterOtherUsersQuestionsEnabled(null);
   		setUseGlobalQuestions(null);
   		setUseOtherUsersQuestions(null);
       	setViewQuestionsFromOtherUsersPrivateCategoriesEnabled(null);
       	setViewQuestionsFromAdminsPrivateCategoriesEnabled(null);
       	setViewQuestionsFromSuperadminsPrivateCategoriesEnabled(null);
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
       		filterCategoryInvalid=!checkQuestionsFilterPermission(operation,filterCategory);
       	}
		
       	if (filterCategoryNotFound)
       	{
       		addErrorMessage("QUESTIONS_FILTER_CATEGORY_NOT_FOUND_ERROR");
       		setFilterCategoryId(Long.MIN_VALUE);
       	}
       	else if (filterCategoryInvalid)
       	{
       		addErrorMessage("NON_AUTHORIZED_ACTION_ERROR");
       		setFilterCategoryId(Long.MIN_VALUE);
       	}
       	
   		// Reload questions from DB
       	questions=null;
       	
       	setAddEnabled(null);
       	setEditEnabled(null);
       	setEditOtherUsersTestsEnabled(null);
       	setEditAdminsTestsEnabled(null);
       	setEditSuperadminsTestsEnabled(null);
   		setFilterGlobalTestsEnabled(null);
   		setFilterOtherUsersTestsEnabled(null);
   		setGlobalOtherUserCategoryAllowed(null);
   		setViewTestsFromOtherUsersPrivateCategoriesEnabled(null);
   		setViewTestsFromAdminsPrivateCategoriesEnabled(null);
   		setViewTestsFromSuperadminsPrivateCategoriesEnabled(null);
       	resetAdmins();
       	resetSuperadmins();
       	
   		// Always reload images categories from DB
       	specialCategoriesFilters=null;
   		setQuestionsCategories(null);
   		
   		List<Question> questions=getQuestions(operation);
   			
        // Get current user session Hibernate operation
   		operation=getCurrentUserOperation(null);
   		
   		getQuestionsCategories(operation);
   		getFilterCategoryId(operation);
        
        DualListModel<Question> questionsDualList=getFilteredQuestionsDualList(operation);
    	questionsDualList.setSource(questions);
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
			questionsDualList=new DualListModel<Question>(
				getQuestions(getCurrentUserOperation(operation)),new ArrayList<Question>());
		}
		return questionsDualList;
	}
	
	/**
	 * @return Users as dual list
	 */
	public DualListModel<User> getUsersDualList()
	{
		if (usersDualList==null)
		{
			usersDualList=new DualListModel<User>(getAvailableUsers(),new ArrayList<User>());
		}
		return usersDualList;
	}
	
	public void setUsersDualList(DualListModel<User> usersDualList)
	{
		this.usersDualList=usersDualList;
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
			adminsDualList=new DualListModel<User>(
				getAvailableAdmins(getCurrentUserOperation(operation)),new ArrayList<User>());
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
				getAvailableSupportContactFilterUsers(getCurrentUserOperation(operation)),
				getTestSupportContactFilterUsers());
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
		refreshSupportContactFilterUsersDualList(getCurrentUserOperation(null),event.getComponent());
		
		// Reload filtered users for "Add/Edit tech support address" dialog
		setFilteredUsersForAddingSupportContactFilterUsers(null);
		groupUsersMap=null;
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
			if (getSupportContactFilterUsersIdsHidden()!=null && 
				!"".equals(getSupportContactFilterUsersIdsHidden()))
			{
				for (String userId:getSupportContactFilterUsersIdsHidden().split(Pattern.quote(",")))
				{
					testSupportContactFilterUsers.add(
						usersService.getUser(getCurrentUserOperation(operation),Long.parseLong(userId)));
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
	 * @return Groups for the filter of the "Add/Edit tech support address" dialog as dual list
	 */
	public DualListModel<String> getSupportContactFilterGroupsDualList()
	{
		return getSupportContactFilterGroupsDualList(null);
	}
	
	public void setSupportContactFilterGroupsDualList(DualListModel<String> supportContactFilterGroupsDualList)
	{
		this.supportContactFilterGroupsDualList=supportContactFilterGroupsDualList;
	}
	
	/**
	 * @param operation Operation
	 * @return Groups for the filter of the "Add/Edit tech support address" dialog as dual list
	 */
	private DualListModel<String> getSupportContactFilterGroupsDualList(Operation operation)
	{
		if (supportContactFilterGroupsDualList==null)
		{
			supportContactFilterGroupsDualList=new DualListModel<String>(
				getAvailableSupportContactFilterGroups(getCurrentUserOperation(operation)),
				getTestSupportContactFilterGroups());
		}
		return supportContactFilterGroupsDualList;
	}
	
	private void refreshSupportContactFilterGroupsDualList(Operation operation,UIComponent component)
	{
		// Process hidden field with groups
		if (processSupportContactFilterGroupsHidden(component))
		{
			// Fill the list of available groups included within the current group filter of the 
			// "Add/Edit tech support address" dialog with the information from hidden field
			List<String> availableSupportContactFilterGroups=
				getAvailableSupportContactFilterGroups(getCurrentUserOperation(operation));
			availableSupportContactFilterGroups.clear();
			if (getAvailableSupportContactFilterGroupsHidden()!=null && 
				!"".equals(getAvailableSupportContactFilterGroupsHidden()))
			{
				for (String group:getAvailableSupportContactFilterGroupsHidden().split(Pattern.quote(",")))
				{
					availableSupportContactFilterGroups.add(group);
				}
			}
			
			// Fill the list of groups included within the current group filter of the 
			// "Add/Edit tech support address" dialog with the information from hidden field
			List<String> testSupportContactFilterGroups=getTestSupportContactFilterGroups();
			testSupportContactFilterGroups.clear();
			if (getSupportContactFilterGroupsHidden()!=null && !"".equals(getSupportContactFilterGroupsHidden()))
			{
				for (String group:getSupportContactFilterGroupsHidden().split(Pattern.quote(",")))
				{
					testSupportContactFilterGroups.add(group);
				}
			}
			
			// Reload dual list of filtered groups for "Add/Edit tech support address" dialog
			setSupportContactFilterGroupsDualList(null);
		}
	}
	
	private boolean processSupportContactFilterGroupsHidden(UIComponent component)
	{
		boolean submittedValue=false;
		FacesContext context=FacesContext.getCurrentInstance();
		UIInput availableSupportContactFilterGroupsHiddenInput=(UIInput)component.findComponent(
			":techSupportAddressDialogForm:availableSupportContactFilterGroupsHidden");
		availableSupportContactFilterGroupsHiddenInput.processDecodes(context);
		if (availableSupportContactFilterGroupsHiddenInput.getSubmittedValue()!=null)
		{
			setAvailableSupportContactFilterGroupsHidden(
				(String)availableSupportContactFilterGroupsHiddenInput.getSubmittedValue());
			submittedValue=true;
		}
		UIInput supportContactFilterGroupsHiddenInput=
			(UIInput)component.findComponent(":techSupportAddressDialogForm:supportContactFilterGroupsHidden");
		supportContactFilterGroupsHiddenInput.processDecodes(context);
		if (supportContactFilterGroupsHiddenInput.getSubmittedValue()!=null)
		{
			setSupportContactFilterGroupsHidden((String)supportContactFilterGroupsHiddenInput.getSubmittedValue());
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
				getAvailableEvaluatorFilterUsers(getCurrentUserOperation(operation)),
				getTestEvaluatorFilterUsers());
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
		groupUsersMap=null;
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
				for (String userId:getEvaluatorFilterUsersIdsHidden().split(Pattern.quote(",")))
				{
					testEvaluatorFilterUsers.add(
						usersService.getUser(getCurrentUserOperation(operation),Long.parseLong(userId)));
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
	
	/**
	 * @return Groups for the filter of the "Add/Edit assessement address" dialog as dual list
	 */
	public DualListModel<String> getEvaluatorFilterGroupsDualList()
	{
		return getEvaluatorFilterGroupsDualList(null);
	}
	
	public void setEvaluatorFilterGroupsDualList(DualListModel<String> evaluatorFilterGroupsDualList)
	{
		this.evaluatorFilterGroupsDualList=evaluatorFilterGroupsDualList;
	}
	
	/**
	 * @param operation Operation
	 * @return Groups for the filter of the "Add/Edit assessement address" dialog as dual list
	 */
	private DualListModel<String> getEvaluatorFilterGroupsDualList(Operation operation)
	{
		if (evaluatorFilterGroupsDualList==null)
		{
			evaluatorFilterGroupsDualList=new DualListModel<String>(
				getAvailableEvaluatorFilterGroups(getCurrentUserOperation(operation)),
				getTestEvaluatorFilterGroups());
		}
		return evaluatorFilterGroupsDualList;
	}
	
	private void refreshEvaluatorFilterGroupsDualList(Operation operation,UIComponent component)
	{
		// Process hidden field with groups
		if (processEvaluatorFilterGroupsHidden(component))
		{
			// Fill the list of available groups included within the current group filter of the 
			// "Add/Edit assessement address" dialog with the information from hidden field
			List<String> availableEvaluatorFilterGroups=
				getAvailableEvaluatorFilterGroups(getCurrentUserOperation(operation));
			availableEvaluatorFilterGroups.clear();
			if (getAvailableEvaluatorFilterGroupsHidden()!=null && 
				!"".equals(getAvailableEvaluatorFilterGroupsHidden()))
			{
				for (String group:getAvailableEvaluatorFilterGroupsHidden().split(Pattern.quote(",")))
				{
					availableEvaluatorFilterGroups.add(group);
				}
			}
			
			// Fill the list of groups included within the current group filter of the 
			// "Add/Edit assessement address" dialog with the information from hidden field
			List<String> testEvaluatorFilterGroups=getTestEvaluatorFilterGroups();
			testEvaluatorFilterGroups.clear();
			if (getEvaluatorFilterGroupsHidden()!=null && !"".equals(getEvaluatorFilterGroupsHidden()))
			{
				for (String group:getEvaluatorFilterGroupsHidden().split(Pattern.quote(",")))
				{
					testEvaluatorFilterGroups.add(group);
				}
			}
			
			// Reload dual list of filtered groups for "Add/Edit assessement address" dialog
			setEvaluatorFilterGroupsDualList(null);
		}
	}
	
	private boolean processEvaluatorFilterGroupsHidden(UIComponent component)
	{
		boolean submittedValue=false;
		FacesContext context=FacesContext.getCurrentInstance();
		UIInput availableEvaluatorFilterGroupsHiddenInput=(UIInput)component.findComponent(
			":assessementAddressDialogForm:availableEvaluatorFilterGroupsHidden");
		availableEvaluatorFilterGroupsHiddenInput.processDecodes(context);
		if (availableEvaluatorFilterGroupsHiddenInput.getSubmittedValue()!=null)
		{
			setAvailableEvaluatorFilterGroupsHidden(
				(String)availableEvaluatorFilterGroupsHiddenInput.getSubmittedValue());
			submittedValue=true;
		}
		UIInput evaluatorFilterGroupsHiddenInput=
			(UIInput)component.findComponent(":assessementAddressDialogForm:evaluatorFilterGroupsHidden");
		evaluatorFilterGroupsHiddenInput.processDecodes(context);
		if (evaluatorFilterGroupsHiddenInput.getSubmittedValue()!=null)
		{
			setEvaluatorFilterGroupsHidden((String)evaluatorFilterGroupsHiddenInput.getSubmittedValue());
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
			for (AddressType addressType:addressTypesService.getAddressTypes(
				getCurrentUserOperation(operation),getSupportContactFilterType()))
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
				User availableSupportContactFilterUser=user.getUserCopy();
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
	
	public String getSupportContactGroup()
	{
		return supportContactGroup;
	}
	
	public void setSupportContactGroup(String supportContactGroup)
	{
		this.supportContactGroup=supportContactGroup;
	}
	
	/**
	 * @return Groups included within the current group filter of the "Add/Edit tech support address" dialog
	 */
	public List<String> getTestSupportContactFilterGroups()
	{
		return testSupportContactFilterGroups;
	}
	
	/**
	 * @param testSupportContactFilterGroups Groups included within the current group filter of the 
	 * "Add/Edit tech support address" dialog
	 */
	public void setTestSupportContactFilterGroups(List<String> testSupportContactFilterGroups)
	{
		this.testSupportContactFilterGroups=testSupportContactFilterGroups;
	}
	
	/**
	 * @return Available groups included within the current group filter of the "Add/Edit tech support address" 
	 * dialog as a string with the groups separated by commas
	 */
	public String getAvailableSupportContactFilterGroupsHidden()
	{
		return availableSupportContactFilterGroupsHidden;
	}
	
	/**
	 * @param availableSupportContactFilterGroupsHidden Groups included within the current group filter 
	 * of the "Add/Edit tech support address" dialog as a string with the groups separated by commas
	 */
	public void setAvailableSupportContactFilterGroupsHidden(String availableSupportContactFilterGroupsHidden)
	{
		this.availableSupportContactFilterGroupsHidden=availableSupportContactFilterGroupsHidden;
	}
	
	/**
	 * @return Groups included within the current group filter of the "Add/Edit tech support address" dialog 
	 * as a string with the groups separated by commas
	 */
	public String getSupportContactFilterGroupsHidden()
	{
		return supportContactFilterGroupsHidden;
	}
	
	/**
	 * @param supportContactFilterGroupsHidden Groups included within the current group filter 
	 * of the "Add/Edit tech support address" dialog as a string with the groups separated by commas
	 */
	public void setSupportContactFilterGroupsHidden(String supportContactFilterGroupsHidden)
	{
		this.supportContactFilterGroupsHidden=supportContactFilterGroupsHidden;
	}
	
	/**
	 * @return Groups not included within the current group filter of the "Add/Edit tech support address" dialog
	 */
	public List<String> getAvailableSupportContactFilterGroups()
	{
		return getAvailableSupportContactFilterGroups(null);
	}
	
	/**
	 * Set groups not included within the current group filter of the "Add/Edit tech support address" dialog 
	 * @param availableSupportContactFilterGroups Groups not included within the current group filter of the 
	 * "Add/Edit tech support address" dialog
	 */
	public void setAvailableSupportContactFilterGroups(List<String> availableSupportContactFilterGroups)
	{
		this.availableSupportContactFilterGroups=availableSupportContactFilterGroups;
	}
	
	/**
	 * @param operation Operation
	 * @return Groups not included within the current group filter of the "Add/Edit tech support address" dialog
	 */
	private List<String> getAvailableSupportContactFilterGroups(Operation operation)
	{
		if (availableSupportContactFilterGroups==null)
		{
			availableSupportContactFilterGroups=new ArrayList<String>();

			// Get current user session Hibernate operation
			operation=getCurrentUserOperation(operation);
			
			if (isAllUsersAllowed())
			{
				for (String group:usersService.getGroups(operation))
				{
					if (!getTestSupportContactFilterGroups().contains(group))
					{
						availableSupportContactFilterGroups.add(group);
					}
				}
			}
			else
			{
				for (UserGroupBean userGroup:getTestUsersGroups())
				{
					if (!userGroup.isTestUser() && 
						!getTestSupportContactFilterGroups().contains(userGroup.getGroup()) && 
						!availableSupportContactFilterGroups.contains(userGroup.getGroup()))
					{
						availableSupportContactFilterGroups.add(userGroup.getGroup());
					}
				}
				for (UserGroupBean adminGroup:getTestAdminsGroups())
				{
					if (!adminGroup.isTestUser() && 
						!getTestSupportContactFilterGroups().contains(adminGroup.getGroup()) && 
						!availableSupportContactFilterGroups.contains(adminGroup.getGroup()))
					{
						availableSupportContactFilterGroups.add(adminGroup.getGroup());
					}
				}
			}
		}
		return availableSupportContactFilterGroups;
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
			for (AddressType addressType:addressTypesService.getAddressTypes(
				getCurrentUserOperation(operation),getEvaluatorFilterType()))
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
				User availableEvaluatorFilterUser=user.getUserCopy();
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
	
	public String getEvaluatorGroup()
	{
		return evaluatorGroup;
	}
	
	public void setEvaluatorGroup(String evaluatorGroup)
	{
		this.evaluatorGroup=evaluatorGroup;
	}
	
	/**
	 * @return Groups included within the current group filter of the "Add/Edit assessement address" dialog
	 */
	public List<String> getTestEvaluatorFilterGroups()
	{
		return testEvaluatorFilterGroups;
	}
	
	/**
	 * @param testEvaluatorFilterGroups Groups included within the current group filter of the 
	 * "Add/Edit assessement address" dialog
	 */
	public void setTestEvaluatorFilterGroups(List<String> testEvaluatorFilterGroups)
	{
		this.testEvaluatorFilterGroups=testEvaluatorFilterGroups;
	}
	
	/**
	 * @return Available groups included within the current group filter of the "Add/Edit assessement address" dialog 
	 * as a string with the groups separated by commas
	 */
	public String getAvailableEvaluatorFilterGroupsHidden()
	{
		return availableEvaluatorFilterGroupsHidden;
	}
	
	/**
	 * @param availableEvaluatorFilterGroupsHidden Groups included within the current group filter 
	 * of the "Add/Edit assessement address" dialog as a string with the groups separated by commas
	 */
	public void setAvailableEvaluatorFilterGroupsHidden(String availableEvaluatorFilterGroupsHidden)
	{
		this.availableEvaluatorFilterGroupsHidden=availableEvaluatorFilterGroupsHidden;
	}
	
	/**
	 * @return Groups included within the current group filter of the "Add/Edit assessement address" dialog 
	 * as a string with the groups separated by commas
	 */
	public String getEvaluatorFilterGroupsHidden()
	{
		return evaluatorFilterGroupsHidden;
	}
	
	/**
	 * @param evaluatorFilterGroupsHidden Groups included within the current group filter 
	 * of the "Add/Edit assessement address" dialog as a string with the groups separated by commas
	 */
	public void setEvaluatorFilterGroupsHidden(String evaluatorFilterGroupsHidden)
	{
		this.evaluatorFilterGroupsHidden=evaluatorFilterGroupsHidden;
	}
	
	/**
	 * @return Groups not included within the current group filter of the "Add/Edit assessement address" dialog
	 */
	public List<String> getAvailableEvaluatorFilterGroups()
	{
		return getAvailableEvaluatorFilterGroups(null);
	}
	
	/**
	 * Set groups not included within the current group filter of the "Add/Edit assessement address" dialog 
	 * @param availableEvaluatorFilterGroups Groups not included within the current group filter of the 
	 * "Add/Edit assessement address" dialog
	 */
	public void setAvailableEvaluatorFilterGroups(List<String> availableEvaluatorFilterGroups)
	{
		this.availableEvaluatorFilterGroups=availableEvaluatorFilterGroups;
	}
	
	/**
	 * @param operation Operation
	 * @return Groups not included within the current group filter of the "Add/Edit assessement address" dialog
	 */
	private List<String> getAvailableEvaluatorFilterGroups(Operation operation)
	{
		if (availableEvaluatorFilterGroups==null)
		{
			availableEvaluatorFilterGroups=new ArrayList<String>();

			// Get current user session Hibernate operation
			operation=getCurrentUserOperation(operation);
			
			if (isAllUsersAllowed())
			{
				for (String group:usersService.getGroups(operation))
				{
					if (!getTestEvaluatorFilterGroups().contains(group))
					{
						availableEvaluatorFilterGroups.add(group);
					}
				}
			}
			else
			{
				for (UserGroupBean userGroup:getTestUsersGroups())
				{
					if (!userGroup.isTestUser() && 
						!getTestEvaluatorFilterGroups().contains(userGroup.getGroup()) && 
						!availableEvaluatorFilterGroups.contains(userGroup.getGroup()))
					{
						availableEvaluatorFilterGroups.add(userGroup.getGroup());
					}
				}
				for (UserGroupBean adminGroup:getTestAdminsGroups())
				{
					if (!adminGroup.isTestUser() && 
						!getTestEvaluatorFilterGroups().contains(adminGroup.getGroup()) && 
						!availableEvaluatorFilterGroups.contains(adminGroup.getGroup()))
					{
						availableEvaluatorFilterGroups.add(adminGroup.getGroup());
					}
				}
			}
		}
		return availableEvaluatorFilterGroups;
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
	
	public Boolean getFilterGlobalTestsEnabled()
	{
		return getFilterGlobalTestsEnabled(null);
	}
	
	public void setFilterGlobalTestsEnabled(Boolean filterGlobalTestsEnabled)
	{
		this.filterGlobalTestsEnabled=filterGlobalTestsEnabled;
	}
	
	public boolean isFilterGlobalTestsEnabled()
	{
		return getFilterGlobalTestsEnabled().booleanValue();
	}
	
	private Boolean getFilterGlobalTestsEnabled(Operation operation)
	{
		if (filterGlobalTestsEnabled==null)
		{
			filterGlobalTestsEnabled=Boolean.valueOf(userSessionService.isGranted(
				getCurrentUserOperation(operation),"PERMISSION_TESTS_GLOBAL_FILTER_ENABLED"));
		}
		return filterGlobalTestsEnabled;
	}
	
	public Boolean getFilterOtherUsersTestsEnabled()
	{
		return getFilterOtherUsersTestsEnabled(null);
	}
	
	public void setFilterOtherUsersTestsEnabled(Boolean filterOtherUsersTestsEnabled)
	{
		this.filterOtherUsersTestsEnabled=filterOtherUsersTestsEnabled;
	}
	
	public boolean isFilterOtherUsersTestsEnabled()
	{
		return getFilterOtherUsersTestsEnabled().booleanValue();
	}
	
	private Boolean getFilterOtherUsersTestsEnabled(Operation operation)
	{
		if (filterOtherUsersTestsEnabled==null)
		{
			filterOtherUsersTestsEnabled=Boolean.valueOf(userSessionService.isGranted(
				getCurrentUserOperation(operation),"PERMISSION_TESTS_OTHER_USERS_FILTER_ENABLED"));
		}
		return filterOtherUsersTestsEnabled;
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
			globalOtherUserCategoryAllowed=Boolean.valueOf(
				permissionsService.isGranted(getCurrentUserOperation(operation),getAuthor(),
				"PERMISSION_TEST_GLOBAL_OTHER_USER_CATEGORY_ALLOWED"));
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
			addEnabled=Boolean.valueOf(
				userSessionService.isGranted(getCurrentUserOperation(operation),"PERMISSION_TESTS_ADD_ENABLED"));
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
			editEnabled=Boolean.valueOf(
				userSessionService.isGranted(getCurrentUserOperation(operation),"PERMISSION_TESTS_EDIT_ENABLED"));
		}
		return editEnabled;
	}
	
	public Boolean getEditOtherUsersTestsEnabled()
	{
		return getEditOtherUsersTestsEnabled(null);
	}
	
	public void setEditOtherUsersTestsEnabled(Boolean editOtherUsersTestsEnabled)
	{
		this.editOtherUsersTestsEnabled=editOtherUsersTestsEnabled;
	}
	
	public boolean isEditOtherUsersTestsEnabled()
	{
		return getEditOtherUsersTestsEnabled().booleanValue();
	}
	
	private Boolean getEditOtherUsersTestsEnabled(Operation operation)
	{
		if (editOtherUsersTestsEnabled==null)
		{
			editOtherUsersTestsEnabled=Boolean.valueOf(userSessionService.isGranted(
				getCurrentUserOperation(operation),"PERMISSION_TESTS_EDIT_OTHER_USERS_TESTS_ENABLED"));
		}
		return editOtherUsersTestsEnabled;
	}
	
	public Boolean getEditAdminsTestsEnabled()
	{
		return getEditAdminsTestsEnabled(null);
	}
	
	public void setEditAdminsTestsEnabled(Boolean editAdminsTestsEnabled)
	{
		this.editAdminsTestsEnabled=editAdminsTestsEnabled;
	}
	
	public boolean isEditAdminsTestsEnabled()
	{
		return getEditAdminsTestsEnabled().booleanValue();
	}
	
	private Boolean getEditAdminsTestsEnabled(Operation operation)
	{
		if (editAdminsTestsEnabled==null)
		{
			editAdminsTestsEnabled=Boolean.valueOf(userSessionService.isGranted(
				getCurrentUserOperation(operation),"PERMISSION_TESTS_EDIT_ADMINS_TESTS_ENABLED"));
		}
		return editAdminsTestsEnabled;
	}
	
	public Boolean getEditSuperadminsTestsEnabled()
	{
		return getEditSuperadminsTestsEnabled(null);
	}
	
	public void setEditSuperadminsTestsEnabled(Boolean editSuperadminsTestsEnabled)
	{
		this.editSuperadminsTestsEnabled=editSuperadminsTestsEnabled;
	}
	
	public boolean isEditSuperadminsTestsEnabled()
	{
		return getEditSuperadminsTestsEnabled().booleanValue();
	}
	
	private Boolean getEditSuperadminsTestsEnabled(Operation operation)
	{
		if (editSuperadminsTestsEnabled==null)
		{
			editSuperadminsTestsEnabled=Boolean.valueOf(userSessionService.isGranted(
				getCurrentUserOperation(operation),"PERMISSION_TESTS_EDIT_SUPERADMINS_TESTS_ENABLED"));
		}
		return editSuperadminsTestsEnabled;
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
		return isAdmin(getCurrentUserOperation(operation),getAuthor());
	}
	
	private void resetTestAuthorAdmin()
	{
		admins.remove(Long.valueOf(getAuthor().getId()));
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
		return isSuperadmin(getCurrentUserOperation(operation),getAuthor());
	}
	
	private void resetTestAuthorSuperadmin()
	{
		superadmins.remove(Long.valueOf(getAuthor().getId()));
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
			viewTestsFromOtherUsersPrivateCategoriesEnabled=Boolean.valueOf(
				userSessionService.isGranted(getCurrentUserOperation(operation),
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
			viewTestsFromAdminsPrivateCategoriesEnabled=Boolean.valueOf(
				userSessionService.isGranted(getCurrentUserOperation(operation),
				"PERMISSION_TESTS_VIEW_TESTS_OF_ADMINS_PRIVATE_CATEGORIES_ENABLED"));
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
			viewTestsFromSuperadminsPrivateCategoriesEnabled=Boolean.valueOf(
				userSessionService.isGranted(getCurrentUserOperation(operation),
				"PERMISSION_TESTS_VIEW_TESTS_OF_SUPERADMINS_PRIVATE_CATEGORIES_ENABLED"));
		}
		return viewTestsFromSuperadminsPrivateCategoriesEnabled;
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
		return getFilterOtherUsersQuestionsEnabled(null);
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
			useGlobalQuestions=Boolean.valueOf(permissionsService.isGranted(
				getCurrentUserOperation(operation),getAuthor(),"PERMISSION_TEST_USE_GLOBAL_QUESTIONS"));
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
			useOtherUsersQuestions=Boolean.valueOf(permissionsService.isGranted(
				getCurrentUserOperation(operation),getAuthor(),"PERMISSION_TEST_USE_OTHER_USERS_QUESTIONS"));
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
			viewQuestionsFromOtherUsersPrivateCategoriesEnabled=Boolean.valueOf(
				userSessionService.isGranted(getCurrentUserOperation(operation),
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
			viewQuestionsFromAdminsPrivateCategoriesEnabled=Boolean.valueOf(
				userSessionService.isGranted(getCurrentUserOperation(operation),
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
			viewQuestionsFromSuperadminsPrivateCategoriesEnabled=Boolean.valueOf(
				userSessionService.isGranted(getCurrentUserOperation(operation),
				"PERMISSION_QUESTIONS_VIEW_QUESTIONS_OF_SUPERADMINS_PRIVATE_CATEGORIES_ENABLED"));
		}
		return viewQuestionsFromSuperadminsPrivateCategoriesEnabled;
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
		if (sectionsSorting==null)
		{
			sectionsSorting=new ArrayList<SectionBean>();
			for (SectionBean section:getSections())
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
	
	public void setSectionsSorting(List<SectionBean> sectionsSorting)
	{
		this.sectionsSorting=sectionsSorting;
	}
	
    public List<TestFeedbackBean> getFeedbacksSorting()
    {
    	if (feedbacksSorting==null)
    	{
    		feedbacksSorting=new ArrayList<TestFeedbackBean>();
    		for (TestFeedbackBean feedback:getFeedbacks())
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
    
	public void setFeedbacksSorting(List<TestFeedbackBean> feedbacksSorting)
	{
		this.feedbacksSorting=feedbacksSorting;
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
    private ScoreUnit getScoreUnit(Operation operation,String unit)
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
	 * @param component Component that triggered the listener that called this method
	 */
	private void refreshActiveGeneralTab(UIComponent component)
	{
		String generalAccordionId=null;
		if (getTestId()>0L)
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
	 * @param component Component that triggered the listener that called this method
	 */
	private void refreshActiveSection(UIComponent component)
	{
		String sectionsAccordionId=null;
		if (getTestId()>0L)
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
		if (getTestId()==0L)
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
	 * @param component Component that triggered the listener that called this method
	 */
	private void processUsersTabCommonDataInputFields(UIComponent component)
	{
		FacesContext context=FacesContext.getCurrentInstance();
		String allUsersAllowedId=null;
		String allowAdminReportsId=null;
		if (getTestId()==0L)
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
	 * @param component Component that triggered the listener that called this method
	 */
	private void processCalendarTabCommonDataInputFields(UIComponent component)
	{
		FacesContext context=FacesContext.getCurrentInstance();
		String startDateId=null;
		String closeDateId=null;
		String warningDateId=null;
		String feedbackDateId=null;
		if (getTestId()==0L)
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
		if (isRestrictDates())
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
	 * @param component Component that triggered the listener that called this method
	 * @param section Section
	 */
	private void processSectionsInputFields(UIComponent component,SectionBean section)
	{
		processSectionsInputFields(component,section,new ArrayList<String>(0));
	}
	
	/**
	 * Process some input fields of the sections tab of a test.
	 * @param component Component that triggered the listener that called this method
	 * @param section Section
	 * @param exceptions List of identifiers of input fields to be excluded from processing 
	 */
	private void processSectionsInputFields(UIComponent component,SectionBean section,
		List<String> exceptions)
	{
		// Reset checked property
		setPropertyChecked(null);
		
		FacesContext context=FacesContext.getCurrentInstance();
		if (section!=null)
		{
			AccordionPanel sectionsAccordion=getSectionsAccordion(component);
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
											section.setRandom(
												Boolean.valueOf((String)random.getSubmittedValue()));
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
	 * @param component Component that triggered the listener that called this method
	 */
	private void processFeedbackEditorInputFields(UIComponent component)
	{
		FacesContext context=FacesContext.getCurrentInstance();
		AccordionPanel feedbackAccordion=getFeedbackAccordion(component);
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
				UIInput feedbackAdvancedNextInput=(UIInput)component.findComponent(
					":testForm:testFormTabs:feedbackAccordion:feedbackAdvancedNext");
				feedbackAdvancedNextInput.processDecodes(context);
				if (feedbackAdvancedNextInput.getSubmittedValue()!=null)
				{
					setFeedbackAdvancedNext((String)feedbackAdvancedNextInput.getSubmittedValue());
				}
		}
	}
	
	/**
	 * Process some input fields of the advanced feedbacks tab of the accordion within the feedback tab of a test.
	 * @param component Component that triggered the listener that called this method
	 */
	private void processAdvancedFeedbacksFeedbackInputFields(UIComponent component)
	{
		FacesContext context=FacesContext.getCurrentInstance();
		String feedbackAdvancedPreviousId=null;
		String feedbackAdvancedNextId=null;
		if (getTestId()==0L)
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
	 * @param component Component that triggered the listener that called this method
	 * @param numTabs Number of tabs to update
	 */
	private void updateSectionsTextFields(UIComponent component,int numTabs)
	{
		FacesContext context=FacesContext.getCurrentInstance();
		UIData sectionsAccordion=(UIData)getSectionsAccordion(component);
        UIComponent tab=sectionsAccordion.getChildren().get(0);
		UIInput sectionNameInput=null;
		UIInput sectionTitleInput=null;
		boolean sectionWeightDone=!isSectionsWeightsDisplayed();
		UIComponent randomPanel=null;
		UIComponent questionsPanel=null;
		for (UIComponent sectionTabChild:tab.getChildren())
		{
			if (sectionTabChild.getId().equals("randomPanel"))
			{
				randomPanel=sectionTabChild;
				UIComponent sectionNamesGrid=null;
				for (UIComponent randomPanelChild:randomPanel.getChildren())
				{
					if (randomPanelChild.getId().equals("sectionNamesGrid"))
					{
						sectionNamesGrid=randomPanelChild;
						for (UIComponent sectionNamesGridChild:sectionNamesGrid.getChildren())
						{
							if (sectionNamesGridChild.getId().equals("sectionName"))
							{
								sectionNameInput=(UIInput)sectionNamesGridChild;
								for (int i=0;i<numTabs;i++)
								{
									sectionsAccordion.setRowIndex(i);
									sectionNameInput.pushComponentToEL(context,null);
									sectionNameInput.setSubmittedValue(getSection(i+1).getName());
									sectionNameInput.popComponentFromEL(context);
								}
								sectionsAccordion.setRowIndex(-1);
							}
							else if (sectionNamesGridChild.getId().equals("sectionTitle"))
							{
								sectionTitleInput=(UIInput)sectionNamesGridChild;
								for (int i=0;i<numTabs;i++)
								{
									sectionsAccordion.setRowIndex(i);
									sectionTitleInput.pushComponentToEL(context,null);
									sectionTitleInput.setSubmittedValue(getSection(i+1).getTitle());
									sectionTitleInput.popComponentFromEL(context);
								}
								sectionsAccordion.setRowIndex(-1);
							}
							else if (!sectionWeightDone && sectionNamesGridChild.getId().equals("sectionWeight"))
							{
								UIInput sectionWeightInput=(UIInput)sectionNamesGridChild;
								for (int i=0;i<numTabs;i++)
								{
									sectionsAccordion.setRowIndex(i);
									sectionWeightInput.pushComponentToEL(context,null);
									sectionWeightInput.setSubmittedValue(Integer.toString(getSection(i+1).getWeight()));
									sectionWeightInput.popComponentFromEL(context);
								}
								sectionsAccordion.setRowIndex(-1);
								sectionWeightDone=true;
							}
							if (sectionNameInput!=null && sectionTitleInput!=null && sectionWeightDone)
							{
								break;
							}
						}
						if (sectionNamesGrid!=null)
						{
							break;
						}
					}
				}
			}
			else if (sectionTabChild.getId().equals("questionsPanel"))
			{
				questionsPanel=sectionTabChild;
				UIData questionsSection=null;
				for (UIComponent questionsPanelChild:sectionTabChild.getChildren())
				{
					if (questionsPanelChild.getId().equals("questionsSection"))
					{
						questionsSection=(UIData)questionsPanelChild;
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
						UIInput questionOrderWeight=null;
						for (UIComponent columnWeightChild:columnWeight.getChildren())
						{
							if (columnWeightChild.getId().equals("questionOrderWeight"))
							{
								questionOrderWeight=(UIInput)columnWeightChild;
								for (int i=0;i<numTabs;i++)
								{
									sectionsAccordion.setRowIndex(i);
									
									// Save current datatable row index
									int currentDatatableRowIndex=questionsSection.getRowIndex();
									
									// We need to update all question weights (all rows)
									for (int j=0;j<questionsSection.getRowCount();j++)
									{
										questionsSection.setRowIndex(j);
										QuestionOrderBean questionOrder=
											(QuestionOrderBean)questionsSection.getRowData();
										questionOrderWeight.pushComponentToEL(context,null);
										questionOrderWeight.setSubmittedValue(
											Integer.toString(questionOrder.getWeight()));
										questionOrderWeight.popComponentFromEL(context);
									}
									
									// Set back datatable row index
									questionsSection.setRowIndex(currentDatatableRowIndex);
									
									sectionsAccordion.setRowIndex(-1);
								}
							}
							if (questionOrderWeight!=null)
							{
								break;
							}
						}
					}
				}
			}
			if (randomPanel!=null && questionsPanel!=null)
			{
				break;
			}
		}
	}
	
	private void processSectionWeight(UIComponent component,SectionBean section)
	{
		List<String> exceptions=new ArrayList<String>();
		exceptions.add("sectionName");
		exceptions.add("sectionTitle");
		exceptions.add("shuffle");
		exceptions.add("random");
		exceptions.add("randomQuantity");
		exceptions.add("questionOrderWeight");
		processSectionsInputFields(component,section,exceptions);
	}
	
	private void processSectionRandomQuantity(UIComponent component,SectionBean section)
	{
		List<String> exceptions=new ArrayList<String>();
		exceptions.add("sectionName");
		exceptions.add("sectionTitle");
		exceptions.add("sectionWeight");
		exceptions.add("shuffle");
		exceptions.add("random");
		exceptions.add("questionOrderWeight");
		processSectionsInputFields(component,section,exceptions);
	}
	
	private void processQuestionOrderWeights(UIComponent component,SectionBean section)
	{
		List<String> exceptions=new ArrayList<String>();
		exceptions.add("sectionName");
		exceptions.add("sectionTitle");
		exceptions.add("sectionWeight");
		exceptions.add("shuffle");
		exceptions.add("random");
		exceptions.add("randomQuantity");
		processSectionsInputFields(component,section,exceptions);
	}
	
	private void updateSectionRandomQuantity(UIComponent component,SectionBean section)
	{
		if (section!=null)
		{
			UIData sectionsAccordion=(UIData)getSectionsAccordion(component);
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
	
	private void updateSectionWeights(UIComponent component,SectionBean section,boolean updateSectionWeight,
		boolean updateQuestionOrderWeights)
	{
		if (section!=null)
		{
			UIData sectionsAccordion=(UIData)getSectionsAccordion(component);
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
	
	private void updateSectionWeights(UIComponent component,SectionBean section)
	{
		updateSectionWeights(component,section,true,true);
	}
	
	private void updateSectionWeight(UIComponent component,SectionBean section)
	{
		updateSectionWeights(component,section,true,false);
	}
	
	private void updateQuestionOrderWeights(UIComponent component,SectionBean section)
	{
		updateSectionWeights(component,section,false,true);
	}
	
	/**
	 * @return Available groups within "Add groups" dialog (users) as a string with the groups separated 
	 * by commas
	 */
	public String getAvailableUserGroupsHidden()
	{
		return availableUserGroupsHidden;
	}
	
	/**
	 * @param availableUserGroupsHidden Available groups within "Add groups" dialog (users) as a string 
	 * with the groups separated by commas
	 */
	public void setAvailableUserGroupsHidden(String availableUserGroupsHidden)
	{
		this.availableUserGroupsHidden=availableUserGroupsHidden;
	}
    
	/**
	 * @return Groups selected to add within "Add groups" dialog (users) as a string with the groups separated 
	 * by commas
	 */
	public String getUserGroupsToAddHidden()
	{
		return userGroupsToAddHidden;
	}
	
	/**
	 * @param userGroupsToAddHidden Groups selected to add within "Add groups" dialog (users) as a string 
	 * with the groups separated by commas
	 */
	public void setUserGroupsToAddHidden(String userGroupsToAddHidden)
	{
		this.userGroupsToAddHidden=userGroupsToAddHidden;
	}
    
	/**
	 * @return User groups as dual list
	 */
	public DualListModel<String> getUserGroupsDualList()
	{
		return getUserGroupsDualList(null);
	}
	
	/**
	 * Set dual list for user groups.
	 * @param userGroupsDualList User groups as dual list
	 */
	public void setUserGroupsDualList(DualListModel<String> userGroupsDualList)
	{
		this.userGroupsDualList=userGroupsDualList;
	}
    
	/**
	 * @param operation Operation
	 * @return User groups as dual list
	 */
	private DualListModel<String> getUserGroupsDualList(Operation operation)
	{
		if (userGroupsDualList==null)
		{
			List<String> availableUserGroups=usersService.getGroups(getCurrentUserOperation(operation));
			for (UserGroupBean userGroup:getTestUsersGroups())
			{
				if (!userGroup.isTestUser())
				{
					availableUserGroups.remove(userGroup.getGroup());
				}
			}
			userGroupsDualList=new DualListModel<String>(availableUserGroups,new ArrayList<String>());
		}
		return userGroupsDualList;
	}
	
	/**
	 * Display a dialog to add groups to the user 
     * @param event Action event
	 */
	public void showAddUserGroups(ActionEvent event)
	{
    	// Get current user session Hibernate operation
    	Operation operation=getCurrentUserOperation(null);
		
		// We need to process some input fields
		processCommonDataInputFields(operation,event.getComponent());
		processUsersTabCommonDataInputFields(event.getComponent());
		
		setUserGroupsDualList(null);
		setUserGroup("");
		setUserGroupsDialogDisplayed(true);
		
		RequestContext rq=RequestContext.getCurrentInstance();
		rq.execute("addUserGroupsDialog.show()");
	}
	
    /**
     * Adds a user group.
     * @param event Action event
     */
	public void addUserGroup(ActionEvent event)
	{
    	// Get current user session Hibernate operation
    	Operation operation=getCurrentUserOperation(null);
		
		// Refresh dual list of user groups for "Add groups" dialog
		refreshUserGroupsDualList(operation,event.getComponent());
		
		// Check group before adding it to dual list
		if (isEnabledAddUserGroup(operation,true))
		{
			getUserGroupsDualList(operation).getTarget().add(getUserGroup());
		}
	}
	
    /**
     * Add user groups selected within dialog
     * @param event Action event
     */
    public void acceptAddUserGroups(ActionEvent event)
    {
    	// Get current user session Hibernate operation
    	Operation operation=getCurrentUserOperation(null);
		
		// Refresh dual list of user groups for "Add groups" dialog
		refreshUserGroupsDualList(operation,event.getComponent());
    	
		// Add selected groups
		List<UserGroupBean> testUsersGroups=getTestUsersGroups();
   		for (String userGroup:getUserGroupsDualList(operation).getTarget())
  		{
  			testUsersGroups.add(new UserGroupBean(usersService,userSessionService,userGroup));
   		}
   		//Collections.sort(getUserGroups());
   		
   		setUserGroupsDialogDisplayed(false);
   		
		// Close dialog
		RequestContext rq=RequestContext.getCurrentInstance();
		rq.execute("addUserGroupsDialog.hide()");
    }
    
    /**
     * @param event Action event
     */
    public void cancelAddUserGroups(ActionEvent event)
    {
    	setUserGroupsDialogDisplayed(false);
    }
	
	/**
	 * Display a dialog to add users allowed to do test. 
     * @param event Action event
	 */
	public void showAddUsers(ActionEvent event)
	{
   		setUsersDualList(null);
		
		// Get current user session Hibernate operation
		Operation operation=getCurrentUserOperation(null);
		
		// We need to process some input fields
		processCommonDataInputFields(operation,event.getComponent());
		processUsersTabCommonDataInputFields(event.getComponent());
   		
		RequestContext rq=RequestContext.getCurrentInstance();
		rq.execute("addUsersDialog.show()");
	}
	
    /**
     * Add users selected within dialog to list of users allowed to do test.
     * @param event Action event
     */
    public void acceptAddUsers(ActionEvent event)
    {
    	List<UserGroupBean> testUsersGroups=getTestUsersGroups();
   		for (User user:getUsersDualList().getTarget())
  		{
   			testUsersGroups.add(new UserGroupBean(user));
   		}
    }
	
    /**
     * ActionListener that deletes an user or group from list of users allowed to do test.
     * @param event Action event
     */
    public void removeUserGroup(ActionEvent event)
    {
    	// Get current user session Hibernate operation
    	Operation operation=getCurrentUserOperation(null);
    	
		// We need to process some input fields
		processCommonDataInputFields(operation,event.getComponent());
		processUsersTabCommonDataInputFields(event.getComponent());
		
		getTestUsersGroups().remove((UserGroupBean)event.getComponent().getAttributes().get("userGroup"));
	}
    
	private void refreshUserGroupsDualList(Operation operation,UIComponent component)
	{
		// Process hidden fields with user groups
		if (processUserGroupsHiddens(component))
		{
			// Get current available user groups
			List<String> availableUserGroups=new ArrayList<String>();
			if (getAvailableUserGroupsHidden()!=null && !"".equals(getAvailableUserGroupsHidden()))
			{
				for (String availableUserGroup:getAvailableUserGroupsHidden().split(Pattern.quote(",")))
				{
					availableUserGroups.add(availableUserGroup);
				}
			}
			
			// Get current user groups to add
			List<String> userGroupsToAdd=new ArrayList<String>();
			if (getUserGroupsToAddHidden()!=null && !"".equals(getUserGroupsToAddHidden()))
			{
				for (String userGroupToAdd:getUserGroupsToAddHidden().split(Pattern.quote(",")))
				{
					userGroupsToAdd.add(userGroupToAdd);
				}
			}
			
			// Refresh user groups dual list
			DualListModel<String> userGroupsDualList=getUserGroupsDualList(getCurrentUserOperation(operation));
			userGroupsDualList.setSource(availableUserGroups);
			userGroupsDualList.setTarget(userGroupsToAdd);
		}
	}
	
	private boolean processUserGroupsHiddens(UIComponent component)
	{
		boolean submittedValue=false;
		FacesContext context=FacesContext.getCurrentInstance();
		UIInput availableUserGroupsHiddenInput=
			(UIInput)component.findComponent(":userGroupsDialogForm:availableUserGroupsHidden");
		availableUserGroupsHiddenInput.processDecodes(context);
		if (availableUserGroupsHiddenInput.getSubmittedValue()!=null)
		{
			setAvailableUserGroupsHidden((String)availableUserGroupsHiddenInput.getSubmittedValue());
			submittedValue=true;
		}
		UIInput userGroupsToAddHiddenInput=
			(UIInput)component.findComponent(":userGroupsDialogForm:userGroupsToAddHidden");
		userGroupsToAddHiddenInput.processDecodes(context);
		if (userGroupsToAddHiddenInput.getSubmittedValue()!=null)
		{
			setUserGroupsToAddHidden((String)userGroupsToAddHiddenInput.getSubmittedValue());
			submittedValue=true;
		}
		return submittedValue;
	}
	
	/**
	 * Checks that group entered by user only includes valid characters or displays an error message if desired.
     * @param group Group
	 * @param displayError true to display error message, false otherwise
	 * @return true if group only includes valid characters (letters, digits), false otherwise
	 */
    private boolean checkValidCharactersForUserGroup(String group,boolean displayError)
    {
    	boolean ok=true;
    	if (StringUtils.hasUnexpectedCharacters(group,true,true,false,null))
    	{
    		if (displayError)
    		{
    			addErrorMessage("INCORRECT_OPERATION","USER_GROUP_INVALID_CHARACTERS");
    		}
    		ok=false;
    	}
    	return ok;
    }
    
    /**
     * Check that first character of the group entered by user is a letter or displays an error message if
     * desired.
     * @param group Group
	 * @param displayError true to display error message, false otherwise
	 * @return true if first character of group is a letter, false otherwise
     */
    private boolean checkFirstCharacterLetterForUserGroup(String group,boolean displayError)
    {
    	boolean ok=true;
    	if (!StringUtils.isFirstCharacterLetter(group))
    	{
    		if (displayError)
    		{
    			addErrorMessage("INCORRECT_OPERATION","USER_GROUP_FIRST_CHARACTER_INVALID");
    		}
    		ok=false;
    	}
    	return ok;
    }
	
    /**
     * Check that group entered by user is valid displays error messages indicating the causes if desired.
     * @param group Group
	 * @param displayErrors true to display error messages, false otherwise
     * @return true if group entered by user is valid, false otherwise
     */
    private boolean checkUserGroup(String group,boolean displayErrors)
    {
    	boolean ok=true;
    	if (group!=null && !"".equals(group))
    	{
        	if (displayErrors)
        	{
        		if (!checkValidCharactersForUserGroup(group,true))
        		{
        			ok=false;
        		}
        		if (!checkFirstCharacterLetterForUserGroup(group,true))
        		{
        			ok=false;
        		}
        	}
        	else
        	{
        		ok=checkValidCharactersForUserGroup(group,false) && 
        			checkFirstCharacterLetterForUserGroup(group,false);
        	}
    	}
    	return ok;
    }
	
	/**
	 * @return true if button to add a user group is enabled, false if it is disabled
	 */
	public boolean isEnabledAddUserGroup()
	{
		return isEnabledAddUserGroup(null);
	}
    
	/**
	 * @param operation Operation
	 * @return true if user group entered by user is valid, false otherwise
	 */
	public boolean isEnabledAddUserGroup(Operation operation)
	{
		return isEnabledAddUserGroup(operation,false);
	}
	
	/**
	 * @param displayErrors true to display error messages, false otherwise
	 * @return true if user group entered by user is valid, false otherwise
	 */
	public boolean isEnabledAddUserGroup(boolean displayErrors)
	{
		return isEnabledAddUserGroup(null,displayErrors);
	}
	
	/**
	 * @param operation Operation
	 * @param displayErrors true to display error messages, false otherwise
	 * @return true if user group entered by user is valid, false otherwise
	 */
	public boolean isEnabledAddUserGroup(Operation operation,boolean displayErrors)
	{
		boolean ok=true;
		if (displayErrors)
		{
			if (getUserGroup()==null || getUserGroup().equals(""))
			{
				addErrorMessage("INCORRECT_OPERATION","USER_GROUP_REQUIRED");
				ok=false;
			}
			else if (checkUserGroup(getUserGroup(),true))
			{
				// Get current user session Hibernate operation
				operation=getCurrentUserOperation(operation);
				
				for (UserGroupBean userGroup:getTestUsersGroups())
				{
					if (!userGroup.isTestUser() && getUserGroup().equals(userGroup.getGroup()))
					{
						addErrorMessage("INCORRECT_OPERATION","USER_GROUP_ALREADY_DECLARED");
						ok=false;
						break;
					}
				}
				if (ok)
				{
					DualListModel<String> userGroupsDualList=getUserGroupsDualList(operation);
					if (userGroupsDualList.getSource().contains(getUserGroup()) || 
						userGroupsDualList.getTarget().contains(getUserGroup()))
					{
						addErrorMessage("INCORRECT_OPERATION","USER_GROUP_ALREADY_DECLARED");
						ok=false;
					}
				}
			}
			else
			{
				ok=false;
			}
		}
		else
		{
			ok=getUserGroup()!=null && !getUserGroup().equals("") && checkUserGroup(getUserGroup(),false);
			if (ok)
			{
				// Get current user session Hibernate operation
				operation=getCurrentUserOperation(operation);
				
				for (UserGroupBean userGroup:getTestUsersGroups())
				{
					if (!userGroup.isTestUser() && getUserGroup().equals(userGroup.getGroup()))
					{
						ok=false;
						break;
					}
				}
				if (ok)
				{
					DualListModel<String> userGroupsDualList=getUserGroupsDualList(operation);
					if (userGroupsDualList.getSource().contains(getUserGroup()) || 
						userGroupsDualList.getTarget().contains(getUserGroup()))
					{
						ok=false;
					}
				}
			}
		}
		return ok;
	}
    
	/**
	 * @return Available Groups within "Add groups" dialog (administrators) as a string with the groups 
	 * separated by commas
	 */
	public String getAvailableAdminGroupsHidden()
	{
		return availableAdminGroupsHidden;
	}
	
	/**
	 * @param availableAdminGroupsHidden Available groups within "Add groups" dialog (administrators) 
	 * as a string with the groups separated by commas
	 */
	public void setAvailableAdminGroupsHidden(String availableAdminGroupsHidden)
	{
		this.availableAdminGroupsHidden=availableAdminGroupsHidden;
	}
    
	/**
	 * @return Groups selected to add within "Add groups" dialog (administrators) as a string with the groups 
	 * separated by commas
	 */
	public String getAdminGroupsToAddHidden()
	{
		return adminGroupsToAddHidden;
	}
	
	/**
	 * @param adminGroupsToAddHidden Groups selected to add within "Add groups" dialog (administrators) 
	 * as a string with the groups separated by commas
	 */
	public void setAdminGroupsToAddHidden(String adminGroupsToAddHidden)
	{
		this.adminGroupsToAddHidden=adminGroupsToAddHidden;
	}
    
	/**
	 * @return Administration groups as dual list
	 */
	public DualListModel<String> getAdminGroupsDualList()
	{
		return getAdminGroupsDualList(null);
	}
	
	/**
	 * Set dual list for administration groups.
	 * @param adminGroupsDualList Administration groups as dual list
	 */
	public void setAdminGroupsDualList(DualListModel<String> adminGroupsDualList)
	{
		this.adminGroupsDualList=adminGroupsDualList;
	}
    
	/**
	 * @param operation Operation
	 * @return Administration groups as dual list
	 */
	private DualListModel<String> getAdminGroupsDualList(Operation operation)
	{
		if (adminGroupsDualList==null)
		{
			List<String> availableAdminGroups=usersService.getGroups(getCurrentUserOperation(operation));
			for (UserGroupBean adminGroup:getTestAdminsGroups())
			{
				if (!adminGroup.isTestUser())
				{
					availableAdminGroups.remove(adminGroup.getGroup());
				}
			}
			adminGroupsDualList=new DualListModel<String>(availableAdminGroups,new ArrayList<String>());
		}
		return adminGroupsDualList;
	}
	
	/**
	 * Display a dialog to add administration groups 
     * @param event Action event
	 */
	public void showAddAdminGroups(ActionEvent event)
	{
    	// Get current user session Hibernate operation
    	Operation operation=getCurrentUserOperation(null);
		
		// We need to process some input fields
		processCommonDataInputFields(operation,event.getComponent());
		processUsersTabCommonDataInputFields(event.getComponent());
		
		setAdminGroupsDualList(null);
		setAdminGroup("");
		setAdminGroupsDialogDisplayed(true);
		
		RequestContext rq=RequestContext.getCurrentInstance();
		rq.execute("addAdminGroupsDialog.show()");
	}
	
    /**
     * Adds an administration group.
     * @param event Action event
     */
	public void addAdminGroup(ActionEvent event)
	{
    	// Get current user session Hibernate operation
    	Operation operation=getCurrentUserOperation(null);
		
		// Refresh dual list of administration groups for "Add groups" dialog
		refreshAdminGroupsDualList(operation,event.getComponent());
		
		// Check group before adding it to dual list
		if (isEnabledAddAdminGroup(operation,true))
		{
			getAdminGroupsDualList(operation).getTarget().add(getAdminGroup());
		}
	}
	
    /**
     * Add administration groups selected within dialog
     * @param event Action event
     */
    public void acceptAddAdminGroups(ActionEvent event)
    {
    	// Get current user session Hibernate operation
    	Operation operation=getCurrentUserOperation(null);
		
		// Refresh dual list of administration groups for "Add groups" dialog
		refreshAdminGroupsDualList(operation,event.getComponent());
    	
		// Add selected groups
		List<UserGroupBean> testAdminsGroups=getTestAdminsGroups();
   		for (String adminGroup:getAdminGroupsDualList(operation).getTarget())
  		{
  			testAdminsGroups.add(new UserGroupBean(usersService,userSessionService,adminGroup));
   		}
   		//Collections.sort(getAdminGroups());
   		
   		setAdminGroupsDialogDisplayed(false);
   		
		// Close dialog
		RequestContext rq=RequestContext.getCurrentInstance();
		rq.execute("addAdminGroupsDialog.hide()");
    }
    
    /**
     * @param event Action event
     */
    public void cancelAddAdminGroups(ActionEvent event)
    {
    	setAdminGroupsDialogDisplayed(false);
    }
	
	/**
	 * Display a dialog to add administrators to test. 
     * @param event Action event
	 */
	public void showAddAdmins(ActionEvent event)
	{
		setAdminsDualList(null);
   		
   		// Get current user session Hibernate operation
   		Operation operation=getCurrentUserOperation(null);
   		
		// We need to process some input fields
		processCommonDataInputFields(operation,event.getComponent());
		processUsersTabCommonDataInputFields(event.getComponent());
   		
		RequestContext rq=RequestContext.getCurrentInstance();
		rq.execute("addAdminsDialog.show()");
	}
	
    /**
     * Add administrators selected within dialog to test.
     * @param event Action event
     */
    public void acceptAddAdmins(ActionEvent event)
    {
    	List<UserGroupBean> testAdminsGroups=getTestAdminsGroups();
   		for (User admin:getAdminsDualList().getTarget())
  		{
   			testAdminsGroups.add(new UserGroupBean(admin));
   		}
    }
	
    /**
     * Deletes an administrator or group from test.
     * @param event Action event
     */
    public void removeAdminGroup(ActionEvent event)
    {
    	// Get current user session Hibernate operation
    	Operation operation=getCurrentUserOperation(null);
    	
		// We need to process some input fields
		processCommonDataInputFields(operation,event.getComponent());
		processUsersTabCommonDataInputFields(event.getComponent());
    	
    	getTestAdminsGroups().remove((UserGroupBean)event.getComponent().getAttributes().get("adminGroup"));
	}
    
	private void refreshAdminGroupsDualList(Operation operation,UIComponent component)
	{
		// Process hidden fields with adminstration groups
		if (processAdminGroupsHiddens(component))
		{
			// Get current available administration groups
			List<String> availableAdminGroups=new ArrayList<String>();
			if (getAvailableAdminGroupsHidden()!=null && !"".equals(getAvailableAdminGroupsHidden()))
			{
				for (String availableAdminGroup:getAvailableAdminGroupsHidden().split(Pattern.quote(",")))
				{
					availableAdminGroups.add(availableAdminGroup);
				}
			}
			
			// Get current administration groups to add
			List<String> adminGroupsToAdd=new ArrayList<String>();
			if (getAdminGroupsToAddHidden()!=null && !"".equals(getAdminGroupsToAddHidden()))
			{
				for (String adminGroupToAdd:getAdminGroupsToAddHidden().split(Pattern.quote(",")))
				{
					adminGroupsToAdd.add(adminGroupToAdd);
				}
			}
			
			// Refresh administration groups dual list
			DualListModel<String> adminGroupsDualList=getAdminGroupsDualList(getCurrentUserOperation(operation));
			adminGroupsDualList.setSource(availableAdminGroups);
			adminGroupsDualList.setTarget(adminGroupsToAdd);
		}
	}
	
	private boolean processAdminGroupsHiddens(UIComponent component)
	{
		boolean submittedValue=false;
		FacesContext context=FacesContext.getCurrentInstance();
		UIInput availableAdminGroupsHiddenInput=
			(UIInput)component.findComponent(":adminGroupsDialogForm:availableAdminGroupsHidden");
		availableAdminGroupsHiddenInput.processDecodes(context);
		if (availableAdminGroupsHiddenInput.getSubmittedValue()!=null)
		{
			setAvailableAdminGroupsHidden((String)availableAdminGroupsHiddenInput.getSubmittedValue());
			submittedValue=true;
		}
		UIInput adminGroupsToAddHiddenInput=
			(UIInput)component.findComponent(":adminGroupsDialogForm:adminGroupsToAddHidden");
		adminGroupsToAddHiddenInput.processDecodes(context);
		if (adminGroupsToAddHiddenInput.getSubmittedValue()!=null)
		{
			setAdminGroupsToAddHidden((String)adminGroupsToAddHiddenInput.getSubmittedValue());
			submittedValue=true;
		}
		return submittedValue;
	}
    
	/**
	 * @return true if administration group entered by user is valid, false otherwise
	 */
	public boolean isEnabledAddAdminGroup()
	{
		return isEnabledAddAdminGroup(null);
	}
    
	/**
	 * @param operation Operation
	 * @return true if administration group entered by user is valid, false otherwise
	 */
	public boolean isEnabledAddAdminGroup(Operation operation)
	{
		return isEnabledAddAdminGroup(operation,false);
	}
	
	/**
	 * @param displayErrors true to display error messages, false otherwise
	 * @return true if administration group entered by user is valid, false otherwise
	 */
	public boolean isEnabledAddAdminGroup(boolean displayErrors)
	{
		return isEnabledAddAdminGroup(null,displayErrors);
	}
	
	/**
	 * @param operation Operation
	 * @param displayErrors true to display error messages, false otherwise
	 * @return true if administration group entered by user is valid, false otherwise
	 */
	public boolean isEnabledAddAdminGroup(Operation operation,boolean displayErrors)
	{
		boolean ok=true;
		if (displayErrors)
		{
			if (getAdminGroup()==null || getAdminGroup().equals(""))
			{
				addErrorMessage("INCORRECT_OPERATION","USER_GROUP_REQUIRED");
				ok=false;
			}
			else if (checkUserGroup(getAdminGroup(),true))
			{
				// Get current user session Hibernate operation
				operation=getCurrentUserOperation(operation);
				
				for (UserGroupBean adminGroup:getTestAdminsGroups())
				{
					if (!adminGroup.isTestUser() && getAdminGroup().equals(adminGroup.getGroup()))
					{
						addErrorMessage("INCORRECT_OPERATION","USER_GROUP_ALREADY_DECLARED");
						ok=false;
						break;
					}
				}
				if (ok)
				{
					DualListModel<String> adminGroupsDualList=getAdminGroupsDualList(operation);
					if (adminGroupsDualList.getSource().contains(getAdminGroup()) || 
						adminGroupsDualList.getTarget().contains(getAdminGroup()))
					{
						addErrorMessage("INCORRECT_OPERATION","USER_GROUP_ALREADY_DECLARED");
						ok=false;
					}
				}
			}
			else
			{
				ok=false;
			}
		}
		else
		{
			ok=getAdminGroup()!=null && !getAdminGroup().equals("") && checkUserGroup(getAdminGroup(),false);
			if (ok)
			{
				// Get current user session Hibernate operation
				operation=getCurrentUserOperation(operation);
				
				for (UserGroupBean adminGroup:getTestAdminsGroups())
				{
					if (!adminGroup.isTestUser() && getAdminGroup().equals(adminGroup.getGroup()))
					{
						ok=false;
						break;
					}
				}
				if (ok)
				{
					DualListModel<String> adminGroupsDualList=getAdminGroupsDualList(operation);
					if (adminGroupsDualList.getSource().contains(getAdminGroup()) || 
						adminGroupsDualList.getTarget().contains(getAdminGroup()))
					{
						ok=false;
					}
				}
			}
		}
		return ok;
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
    	if (getTestId()>0L)
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
    	if (getTestId()>0L)
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
    	if (getTestId()>0L)
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
    	if (getTestId()>0L)
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
			groupUsersMap=null;
	   		setSupportContactFilterUsersDualList(null);
	   		setFilterSupportContactRangeNameLowerLimit("A");
	   		setFilterSupportContactRangeNameUpperLimit("Z");
	   		setFilterSupportContactRangeSurnameLowerLimit("A");
	   		setFilterSupportContactRangeSurnameUpperLimit("Z");
	   		setSupportContactGroup("");
	   		setAvailableSupportContactFilterGroups(null);	   		
	   		getTestSupportContactFilterGroups().clear();
	   		setSupportContactFilterGroupsDualList(null);
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
			groupUsersMap=null;
	   		setSupportContactFilterUsersDualList(null);
	   		setFilterSupportContactRangeNameLowerLimit("A");
	   		setFilterSupportContactRangeNameUpperLimit("Z");
	   		setFilterSupportContactRangeSurnameLowerLimit("A");
	   		setFilterSupportContactRangeSurnameUpperLimit("Z");
	   		setSupportContactGroup("");
	   		setAvailableSupportContactFilterGroups(null);	   		
		   	getTestSupportContactFilterGroups().clear();
		   	setSupportContactFilterGroupsDualList(null);
	   		if (filterValue!=null && !"".equals(filterValue) && "USER_FILTER".equals(filterType))
	   		{
	   			if ("USERS_SELECTION".equals(filterSubtype))
	   			{
	   				List<String> checkedOUCUs=new ArrayList<String>();
	   				for (String sOUCU:filterValue.split(Pattern.quote(",")))
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
		   	else if (filterValue!=null && !"".equals(filterValue) && "GROUP_FILTER".equals(filterType))
		   	{
	   			for (String authId:filterValue.split(Pattern.quote(",")))
	   			{
	   				if (!getTestSupportContactFilterGroups().contains(authId))
	   				{
	   					getTestSupportContactFilterGroups().add(authId);
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
     * Adds a group as filter for a support contact.
     * @param event Action event
     */
	public void addSupportContactGroup(ActionEvent event)
	{
    	// Get current user session Hibernate operation
    	Operation operation=getCurrentUserOperation(null);
		
		// Refresh dual list of user groups for "Add/Edit tech support addresses" dialog
		refreshSupportContactFilterGroupsDualList(operation,event.getComponent());
		
		// Check group before adding it to dual list
		if (isEnabledAddSupportContactGroup(operation,true))
		{
			getSupportContactFilterGroupsDualList(operation).getTarget().add(getSupportContactGroup());
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
			else if ("GROUP_FILTER".equals(filterType))
			{
				refreshSupportContactFilterGroupsDualList(operation,event.getComponent());
				for (String supportedGroup:getTestSupportContactFilterGroups())
				{
					if (filterValue.length()>0)
					{
						filterValue.append(',');
					}
					filterValue.append(supportedGroup);
				}
			}
			SupportContactBean currentSupportContact=getCurrentSupportContact();
			if (currentSupportContact==null)
			{
				// Add a new support contact
				getSupportContacts().add(new SupportContactBean(
					this,getSupportContact(),filterType,filterSubtype,filterValue.toString()));
			}
			else
			{
				SupportContactBean supportContact=null;
				for (SupportContactBean s:getSupportContacts())
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
			getSupportContacts().remove(event.getComponent().getAttributes().get("supportContact"));
		}
		else
		{
			// Scroll page to top position
			scrollToTop();
		}
	}
    
	/**
	 * @return true if group entered by user for a support contact is valid, false otherwise
	 */
	public boolean isEnabledAddSupportContactGroup()
	{
		return isEnabledAddSupportContactGroup(null);
	}
    
	/**
	 * @param operation Operation
	 * @return true if group entered by user for a support contact is valid, false otherwise
	 */
	public boolean isEnabledAddSupportContactGroup(Operation operation)
	{
		return isEnabledAddSupportContactGroup(operation,false);
	}
	
	/**
	 * @param displayErrors true to display error messages, false otherwise
	 * @return true true if group entered by user for a support contact is valid, false otherwise
	 */
	public boolean isEnabledAddSupportContactGroup(boolean displayErrors)
	{
		return isEnabledAddSupportContactGroup(null,displayErrors);
	}
	
	/**
	 * @param operation Operation
	 * @param displayErrors true to display error messages, false otherwise
	 * @return  true if group entered by user for a support contact is valid, false otherwise
	 */
	public boolean isEnabledAddSupportContactGroup(Operation operation,boolean displayErrors)
	{
		boolean ok=true;
		if (displayErrors)
		{
			if (getSupportContactGroup()==null || getSupportContactGroup().equals(""))
			{
				addErrorMessage("INCORRECT_OPERATION","USER_GROUP_REQUIRED");
				ok=false;
			}
			else if (checkUserGroup(getSupportContactGroup(),true))
			{
				DualListModel<String> supportContactFilterGroupsDualList=
					getSupportContactFilterGroupsDualList(getCurrentUserOperation(operation));
				if (supportContactFilterGroupsDualList.getSource().contains(getSupportContactGroup()) || 
					supportContactFilterGroupsDualList.getTarget().contains(getSupportContactGroup()))
				{
					addErrorMessage("INCORRECT_OPERATION","USER_GROUP_ALREADY_DECLARED");
					ok=false;
				}
			}
			else
			{
				ok=false;
			}
		}
		else
		{
			ok=getSupportContactGroup()!=null && !getSupportContactGroup().equals("") && 
				checkUserGroup(getSupportContactGroup(),false);
			if (ok)
			{
				DualListModel<String> supportContactFilterGroupsDualList=
					getSupportContactFilterGroupsDualList(getCurrentUserOperation(operation));
				if (supportContactFilterGroupsDualList.getSource().contains(getSupportContactGroup()) || 
					supportContactFilterGroupsDualList.getTarget().contains(getSupportContactGroup()))
				{
					ok=false;
				}
			}
		}
		return ok;
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
			groupUsersMap=null;
	   		setEvaluatorFilterUsersDualList(null);
	   		setFilterEvaluatorRangeNameLowerLimit("A");
	   		setFilterEvaluatorRangeNameUpperLimit("Z");
	   		setFilterEvaluatorRangeSurnameLowerLimit("A");
	   		setFilterEvaluatorRangeSurnameUpperLimit("Z");
	   		setEvaluatorGroup("");
	   		setAvailableEvaluatorFilterGroups(null);	   		
	   		getTestEvaluatorFilterGroups().clear();
	   		setEvaluatorFilterGroupsDualList(null);
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
			groupUsersMap=null;
		   	setEvaluatorFilterUsersDualList(null);
		   	setFilterEvaluatorRangeNameLowerLimit("A");
		   	setFilterEvaluatorRangeNameUpperLimit("Z");
		   	setFilterEvaluatorRangeSurnameLowerLimit("A");
		   	setFilterEvaluatorRangeSurnameUpperLimit("Z");
	   		setEvaluatorGroup("");
	   		setAvailableEvaluatorFilterGroups(null);	   		
		   	getTestEvaluatorFilterGroups().clear();
		   	setEvaluatorFilterGroupsDualList(null);
		   	if (filterValue!=null && !"".equals(filterValue) && "USER_FILTER".equals(filterType))
		   	{
		   		if ("USERS_SELECTION".equals(filterSubtype))
		   		{
		   			List<String> checkedOUCUs=new ArrayList<String>();
		   			for (String sOUCU:filterValue.split(Pattern.quote(",")))
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
		   	else if (filterValue!=null && !"".equals(filterValue) && "GROUP_FILTER".equals(filterType))
		   	{
	   			for (String authId:filterValue.split(Pattern.quote(",")))
	   			{
	   				if (!getTestEvaluatorFilterGroups().contains(authId))
	   				{
	   					getTestEvaluatorFilterGroups().add(authId);
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
     * Adds a group as filter for an evaluator.
     * @param event Action event
     */
	public void addEvaluatorGroup(ActionEvent event)
	{
    	// Get current user session Hibernate operation
    	Operation operation=getCurrentUserOperation(null);
		
		// Refresh dual list of user groups for "Add/Edit assessement addresses" dialog
		refreshEvaluatorFilterGroupsDualList(operation,event.getComponent());
		
		// Check group before adding it to dual list
		if (isEnabledAddEvaluatorGroup(operation,true))
		{
			getEvaluatorFilterGroupsDualList(operation).getTarget().add(getEvaluatorGroup());
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
			else if ("GROUP_FILTER".equals(filterType))
			{
				refreshEvaluatorFilterGroupsDualList(operation,event.getComponent());
				for (String assessedGroup:getTestEvaluatorFilterGroups())
				{
					if (filterValue.length()>0)
					{
						filterValue.append(',');
					}
					filterValue.append(assessedGroup);
				}
			}
			EvaluatorBean currentEvaluator=getCurrentEvaluator();
			if (currentEvaluator==null)
			{
				// Add a new evaluator
				getEvaluators().add(
					new EvaluatorBean(this,getEvaluator(),filterType,filterSubtype,filterValue.toString()));
			}
			else
			{
				EvaluatorBean evaluator=null;
				for (EvaluatorBean e:getEvaluators())
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
			getEvaluators().remove(event.getComponent().getAttributes().get("evaluator"));
		}
		else
		{
			// Scroll page to top position
			scrollToTop();
		}
	}
	
	/**
	 * @return true if group entered by user for an evaluator is valid, false otherwise
	 */
	public boolean isEnabledAddEvaluatorGroup()
	{
		return isEnabledAddEvaluatorGroup(null);
	}
    
	/**
	 * @param operation Operation
	 * @return true if group entered by user for an evaluator is valid, false otherwise
	 */
	public boolean isEnabledAddEvaluatorGroup(Operation operation)
	{
		return isEnabledAddEvaluatorGroup(operation,false);
	}
	
	/**
	 * @param displayErrors true to display error messages, false otherwise
	 * @return true true if group entered by user for an evaluator is valid, false otherwise
	 */
	public boolean isEnabledAddEvaluatorGroup(boolean displayErrors)
	{
		return isEnabledAddEvaluatorGroup(null,displayErrors);
	}
	
	/**
	 * @param operation Operation
	 * @param displayErrors true to display error messages, false otherwise
	 * @return  true if group entered by user for an evaluator is valid, false otherwise
	 */
	public boolean isEnabledAddEvaluatorGroup(Operation operation,boolean displayErrors)
	{
		boolean ok=true;
		if (displayErrors)
		{
			if (getEvaluatorGroup()==null || getEvaluatorGroup().equals(""))
			{
				addErrorMessage("INCORRECT_OPERATION","USER_GROUP_REQUIRED");
				ok=false;
			}
			else if (checkUserGroup(getEvaluatorGroup(),true))
			{
				DualListModel<String> evaluatorFilterGroupsDualList=
					getEvaluatorFilterGroupsDualList(getCurrentUserOperation(operation));
				if (evaluatorFilterGroupsDualList.getSource().contains(getEvaluatorGroup()) || 
					evaluatorFilterGroupsDualList.getTarget().contains(getEvaluatorGroup()))
				{
					addErrorMessage("INCORRECT_OPERATION","USER_GROUP_ALREADY_DECLARED");
					ok=false;
				}
			}
			else
			{
				ok=false;
			}
		}
		else
		{
			ok=getEvaluatorGroup()!=null && !getEvaluatorGroup().equals("") && 
				checkUserGroup(getEvaluatorGroup(),false);
			if (ok)
			{
				DualListModel<String> evaluatorFilterGroupsDualList=
					getEvaluatorFilterGroupsDualList(getCurrentUserOperation(operation));
				if (evaluatorFilterGroupsDualList.getSource().contains(getEvaluatorGroup()) || 
					evaluatorFilterGroupsDualList.getTarget().contains(getEvaluatorGroup()))
				{
					ok=false;
				}
			}
		}
		return ok;
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
    		// Get section
        	AccordionPanel sectionsAccordion=getSectionsAccordion(event.getComponent());
        	SectionBean section=getSectionFromSectionsAccordion(sectionsAccordion);
    		
    		// We need to process some input fields
    		processSectionsInputFields(event.getComponent(),section);
    		
    		// Set back accordion row index -1
    		sectionsAccordion.setRowIndex(-1);
    		
    		if (section!=null)
    		{
    			// We need to update questions of this section
				updateSectionQuestions(section);
    			
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
    				// Scroll page to top position
    				scrollToTop();
    				
    				// We need to update sections text fields
    				updateSectionsTextFields(event.getComponent(),getSectionsSize());
    				
    				// Restore old section name
    				section.setName(activeSectionName);
    			}
    		}
    	}
    	else if ("sectionWeight".equals(property))
    	{
			// We need to process weight
    		processSectionWeight(event.getComponent(),sectionChecked);
    		
    		// We need to stop this response because there is another in process and we don't want to 
    		// interfere with it
    		FacesContext.getCurrentInstance().responseComplete();
    	}
    	else if ("randomQuantity".equals(property))
    	{
			// We need to process random quantity
    		processSectionRandomQuantity(event.getComponent(),sectionChecked);
    		
    		// We need to stop this response because there is another in process and we don't want to 
    		// interfere with it
    		FacesContext.getCurrentInstance().responseComplete();
    	}
    	else if ("questionOrderWeight".equals(property))
    	{
    		// We need to process question orders weights
    		processQuestionOrderWeights(event.getComponent(),sectionChecked);
    		
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
			
			// We need update questions of current section
			updateSectionQuestions(getCurrentSection());
		}
		
		// We need to update sections text fields
		updateSectionsTextFields(event.getComponent(),getSectionsSize());
	}
	
	/**
	 * Action listener for updating questions information if we cancel the changes within the dialog for 
	 * re-sorting questions of a section. 
	 * @param event Action event
	 */
	public void cancelReSortQuestions(ActionEvent event)
	{
		// We need update questions of current section
		updateSectionQuestions(getActiveSection(event.getComponent()));
		
		// We need to update sections text fields
		updateSectionsTextFields(event.getComponent(),getSectionsSize());
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
	 * @param order Order
	 * @return Section with order received or null if there is no section with that order
	 */
	private SectionBean getSection(int order)
	{
		SectionBean section=null;
		for (SectionBean s:getSections())
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
	 * @param component Component that triggered the listener that called this method
	 * @return Sections accordion
	 */
	private AccordionPanel getSectionsAccordion(UIComponent component)
	{
		String sectionsAccordionId=null;
		if (getTestId()==0L)
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
	 * @param sectionsAccordion Sections accordion component
	 * @return Section associated with the active tab on the sections accordion
	 */
	private SectionBean getSectionFromSectionsAccordion(AccordionPanel sectionsAccordion)
	{
		SectionBean section=null;
		if (sectionsAccordion!=null)
		{
			section=getSection(activeSectionIndex+1);
			sectionsAccordion.setRowIndex(activeSectionIndex);
		}
		return section;
	}
	
	/**
	 * @param component Component that triggered the listener that called this method
	 * @return Active section
	 */
	private SectionBean getActiveSection(UIComponent component)
	{
		return getSectionFromSectionsAccordion(getSectionsAccordion(component));
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
    		// Get section to process if any
        	AccordionPanel sectionsAccordion=getSectionsAccordion(event.getComponent());
        	SectionBean section=getSectionFromSectionsAccordion(sectionsAccordion);
    		
    		// We need to process some input fields
    		processSectionsInputFields(event.getComponent(),section);
    		
			// We need to update all questions
    		updateQuestions(true);
    		
    		// Set back accordion row index -1
    		sectionsAccordion.setRowIndex(-1);
    		
			// Check that current section name entered by user is valid
			if (checkSectionName(section.getName()))
			{
				activeSectionName=section.getName();
				
				setSectionsSorting(null);
	    		RequestContext rq=RequestContext.getCurrentInstance();
	    		rq.execute("resortSectionsDialog.show()");
			}
			else
			{
				// We need to update sections text fields
				updateSectionsTextFields(event.getComponent(),getSectionsSize());
				
				// Scroll page to top position
				scrollToTop();
				
				// Restore old section name
				getCurrentSection().setName(activeSectionName);
			}
    	}
    	else if ("sectionWeight".equals(property))
    	{
			// We need to process weight
    		processSectionWeight(event.getComponent(),sectionChecked);
    		
    		// We need to stop this response because there is another in process and we don't want to 
    		// interfere with it
    		FacesContext.getCurrentInstance().responseComplete();
    	}
    	else if ("randomQuantity".equals(property))
    	{
			// We need to process random quantity
    		processSectionRandomQuantity(event.getComponent(),sectionChecked);
    		
    		// We need to stop this response because there is another in process and we don't want to 
    		// interfere with it
    		FacesContext.getCurrentInstance().responseComplete();
    	}
    	else if ("questionOrderWeight".equals(property))
    	{
    		// We need to process question orders weights
    		processQuestionOrderWeights(event.getComponent(),sectionChecked);
    		
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
		List<SectionBean> sectionsSorting=getSectionsSorting();
		for (int sectionPos=1;sectionPos<=sectionsSorting.size();sectionPos++)
		{
			SectionBean section=sectionsSorting.get(sectionPos-1);
			section.setOrder(sectionPos);
		}
		setSections(sectionsSorting);
		
		// We need update questions of current section
		updateSectionQuestions(getActiveSection(event.getComponent()));
		
		// We need to update sections text fields
		updateSectionsTextFields(event.getComponent(),sectionsSorting.size());
	}
	
	/**
	 * Action listener for updating questions information if we cancel the changes within the dialog for 
	 * re-sorting sections. 
	 * @param event Action event
	 */
	public void cancelReSortSections(ActionEvent event)
	{
		// We need update questions of current section
		updateSectionQuestions(getActiveSection(event.getComponent()));
		
		// We need to update sections text fields
		updateSectionsTextFields(event.getComponent(),getSectionsSize());
	}
	
	/**
	 * @return true if button to re-sort sections is enabled, false if it is disabled
	 */
	public boolean isEnabledReSortSections()
	{
		return getSectionsSize()>1;
	}
	
	/**
	 * @param component Component that triggered the listener that called this method
	 * @return Feedback accordion
	 */
	private AccordionPanel getFeedbackAccordion(UIComponent component)
	{
		String feedbackAccordionId=null;
		if (getTestId()==0L)
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
		processAdvancedFeedbacksFeedbackInputFields(event.getComponent());
		
		updateQuestionsForFeedbacks();
		
		setFeedbacksSorting(null);
		RequestContext rq=RequestContext.getCurrentInstance();
		rq.execute("resortFeedbacksDialog.show()");
	}
	
    /**
     * Re-sort feedbacks in the same order as in the dialog.
     * @param event Action event
     */
	public void acceptReSortFeedbacks(ActionEvent event)
	{
		List<TestFeedbackBean> feedbacksSorting=getFeedbacksSorting();
		for (int feedbackPos=1;feedbackPos<=feedbacksSorting.size();feedbackPos++)
		{
			TestFeedbackBean feedback=feedbacksSorting.get(feedbackPos-1);
			feedback.setPosition(feedbackPos);
		}
		setFeedbacks(feedbacksSorting);
		
		updateQuestionsForFeedbacks();
	}
	
	/**
	 * Action listener for updating questions information if we cancel the changes within the dialog for re-sorting 
	 * feedbacks. 
	 * @param event Action event
	 */
	public void cancelReSortFeedbacks(ActionEvent event)
	{
		updateQuestionsForFeedbacks();
	}
	
	/**
	 * @return true if button to re-sort feedbacks is enabled, false if it is disabled
	 */
	public boolean isEnabledReSortFeedbacks()
	{
		return getFeedbacks().size()>1;
	}
	
	/**
	 * Action listener to show the dialog for adding a new feedback.
	 * @param event Action event
	 */
	public void showAddFeedback(ActionEvent event)
	{
		// We need to process some input fields
		processAdvancedFeedbacksFeedbackInputFields(event.getComponent());
		
		updateQuestions(true);
		
		resetOldFeedbackValues();
		
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
		processAdvancedFeedbacksFeedbackInputFields(event.getComponent());
		
		updateQuestions(true);
		
		resetOldFeedbackValues();
		
		// Copy feedback so we work at dialog with a copy
		TestFeedbackBean feedback=(TestFeedbackBean)event.getComponent().getAttributes().get("feedback");
		setCurrentFeedback(new TestFeedbackBean(this,feedback.getPosition()));
		
		getCurrentFeedback().setHtmlContent(feedback.getHtmlContent());
		TestFeedbackConditionBean condition=feedback.getCondition();
		TestFeedbackConditionBean newCondition=new TestFeedbackConditionBean(this);
		newCondition.setSection(condition.getSection());
		newCondition.setUnit(condition.getUnit());
		newCondition.setNewComparator(condition.getComparator());
		newCondition.setConditionalCmp(condition.getConditionalCmp());
		newCondition.setConditionalBetweenMin(condition.getConditionalBetweenMin());
		newCondition.setConditionalBetweenMax(condition.getConditionalBetweenMax());
		getCurrentFeedback().setCondition(newCondition);
		RequestContext rq=RequestContext.getCurrentInstance();
		rq.execute("addFeedbackDialog.show()");
	}
	
	/**
	 * Check old conditional value.
	 * @return true if old conditional value is valid, false otherwise
	 */
	private boolean checkOldFeedbackConditionalCmp()
	{
		boolean ok=true;
		TestFeedbackConditionBean conditional=getCurrentFeedback().getCondition();
		int conditionalCmp=oldConditionalCmp<0?conditional.getConditionalCmp():oldConditionalCmp;
		int minValue=conditional.getMinValueConditionalCmp();
		int maxValue=conditional.getMaxValueConditionalCmp();
		if (conditionalCmp<minValue)
		{
			ok=false;
		}
		else if (conditionalCmp>maxValue)
		{
			ok=false;
		}
		return ok;
	}
	
	/**
	 * Check old conditional min value.
	 * @return true if old conditional min value is valid, false otherwise
	 */
	private boolean checkOldFeedbackConditionalBetweenMin()
	{
		boolean ok=true;
		TestFeedbackConditionBean conditional=getCurrentFeedback().getCondition();
		int conditionalBetweenMin=
			oldConditionalBetweenMin<0?conditional.getConditionalBetweenMin():oldConditionalBetweenMin;
		int conditionalBetweenMax=
			oldConditionalBetweenMax<0?conditional.getConditionalBetweenMax():oldConditionalBetweenMax;
		if (conditionalBetweenMin<0)
		{
			ok=false;
		}
		else if (conditionalBetweenMin>conditionalBetweenMax)
		{
			ok=false;
		}
		return ok;
	}
	
	/**
	 * Check old conditional max value changing it to a valid value if it is invalid.
	 * @return true if old conditional max value is valid, false otherwise
	 */
	private boolean checkOldFeedbackConditionalBetweenMax()
	{
		boolean ok=true;
		TestFeedbackConditionBean conditional=getCurrentFeedback().getCondition();
		int conditionalBetweenMin=
			oldConditionalBetweenMin<0?conditional.getConditionalBetweenMin():oldConditionalBetweenMin;
		int conditionalBetweenMax=
			oldConditionalBetweenMax<0?conditional.getConditionalBetweenMax():oldConditionalBetweenMax;
		int maxConditionalValue=conditional.getMaxConditionalValue();
		if (conditionalBetweenMax<conditionalBetweenMin)
		{
			ok=false;
		}
		else if (conditionalBetweenMax>maxConditionalValue)
		{
			ok=false;
		}
		return ok;
	}
	
	/**
	 * Check old feedback conditions values.
	 * @return true if all old feedback conditions values are valid, false otherwise
	 */
	private boolean checkOldFeedbackConditionsValues()
	{
		boolean ok=true;
		
		TestFeedbackConditionBean conditional=getCurrentFeedback().getCondition();
		if (NumberComparator.compareU(conditional.getComparator(),NumberComparator.BETWEEN))
		{
			if (!checkOldFeedbackConditionalBetweenMin())
			{
				ok=false;
			}
			if (!checkOldFeedbackConditionalBetweenMax())
			{
				ok=false;
			}
		}
		else if (!checkOldFeedbackConditionalCmp())
		{
			ok=false;
		}
		return ok;
	}
	
	/**
	 * Check conditional value changing it to a valid value if it is invalid.
	 * @return true if conditional value is valid, false otherwise
	 */
	private boolean checkAndChangeFeedbackConditionalCmp()
	{
		boolean ok=true;
		
		TestFeedbackConditionBean conditional=getCurrentFeedback().getCondition();
		int conditionalCmp=conditional.getConditionalCmp();
		int minValue=conditional.getMinValueConditionalCmp();
		int maxValue=conditional.getMaxValueConditionalCmp();
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
	 * @return true if conditional max value is valid, false otherwise
	 */
	private boolean checkAndChangeFeedbackConditionalBetweenMax()
	{
		boolean ok=true;
		TestFeedbackConditionBean conditional=getCurrentFeedback().getCondition();
		int conditionalBetweenMin=conditional.getConditionalBetweenMin();
		int conditionalBetweenMax=conditional.getConditionalBetweenMax();
		int maxConditionalValue=conditional.getMaxConditionalValue();
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
	 * @return true if all feedback conditions values are valid, false otherwise
	 */
	private boolean checkAndChangeFeedbackConditionsValues()
	{
		boolean ok=true;
		
		TestFeedbackConditionBean conditional=getCurrentFeedback().getCondition();
		if (NumberComparator.compareU(conditional.getComparator(),NumberComparator.BETWEEN))
		{
			if (!checkAndChangeFeedbackConditionalBetweenMin())
			{
				ok=false;
			}
			if (!checkAndChangeFeedbackConditionalBetweenMax())
			{
				ok=false;
			}
		}
		else if (!checkAndChangeFeedbackConditionalCmp())
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
		boolean ok=true;
		long oldAddFeedbackDialogMilliseconds=
			oldAddFeedbackDialogTimestamp<0L?Long.MAX_VALUE:new Date().getTime()-oldAddFeedbackDialogTimestamp;
		if (oldAddFeedbackDialogMilliseconds>OLD_FEEDBACK_DIALOG_VALUES_DELAY)
		{
			resetOldFeedbackValues();
			ok=checkOldFeedbackConditionsValues();
		}
		else
		{
			ok=checkOldFeedbackConditionsValues();
			resetOldFeedbackValues();
		}
		updateQuestions(true);
		checkAndChangeFeedbackConditionsValues();
		if (ok)
		{
			List<TestFeedbackBean> feedbacks=getFeedbacks();
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
	 * Action listener for updating questions information if we cancel the changes within the dialog for adding 
	 * feedbacks. 
     * @param event Action event
     */
	public void cancelAddFeedback(ActionEvent event)
	{
		resetOldFeedbackValues();
		updateQuestionsForFeedbacks();
	}
	
	/**
	 * Action listener to delete a feedback.
	 * @param event Action event
	 */
	public void removeFeedback(ActionEvent event)
	{
		// We need to process some input fields
		processAdvancedFeedbacksFeedbackInputFields(event.getComponent());
		
		List<TestFeedbackBean> feedbacks=getFeedbacks();
		
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
	 * Reset olf feedback values.
	 */
	public void resetOldFeedbackValues()
	{
		oldConditionalCmp=-1;
		oldConditionalBetweenMin=-1;
		oldConditionalBetweenMax=-1;
		oldAddFeedbackDialogTimestamp=-1L;
	}
	
	/**
	 * Ajax listener to check conditional value.
	 * @param event Ajax event
	 */
	public void changeFeedbackConditionalCmp(AjaxBehaviorEvent event)
	{
		oldConditionalCmp=getCurrentFeedback().getCondition().getConditionalCmp();
		oldConditionalBetweenMin=-1;
		oldConditionalBetweenMax=-1;
		oldAddFeedbackDialogTimestamp=new Date().getTime();
		checkAndChangeFeedbackConditionalCmp();
	}
	
	/**
	 * Ajax listener to check conditional min value.
	 * @param event Ajax event
	 */
	public void changeFeedbackConditionalBetweenMin(AjaxBehaviorEvent event)
	{
		oldConditionalCmp=-1;
		oldConditionalBetweenMin=getCurrentFeedback().getCondition().getConditionalBetweenMin();
		oldConditionalBetweenMax=-1;
		oldAddFeedbackDialogTimestamp=new Date().getTime();
		checkAndChangeFeedbackConditionalBetweenMin();
	}
	
	/**
	 * Ajax listener to check conditional max value.
	 * @param event Ajax event
	 */
	public void changeFeedbackConditionalBetweenMax(AjaxBehaviorEvent event)
	{
		oldConditionalCmp=-1;
		oldConditionalBetweenMin=-1;
		oldConditionalBetweenMax=getCurrentFeedback().getCondition().getConditionalBetweenMax();
		oldAddFeedbackDialogTimestamp=new Date().getTime();
		checkAndChangeFeedbackConditionalBetweenMax();
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
			if (currentFeedback.getPosition()>getFeedbacks().size())
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
    		sectionName.append(getNumberedSectionName(section));
    	}
    	return sectionName.toString();
    }
    
    /**
     * @param section Section
     * @return Section's name with a number appended if it is needed to distinguish sections with the same name
     */
    public String getNumberedSectionName(SectionBean section)
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
        		for (SectionBean s:getSections())
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
    	return getSectionsSize()>1?
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
		// End current user session Hibernate operation
		userSessionService.endCurrentUserOperation();
		
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
	 * Action listener for updating questions information if we cancel the dialog to cancel the test creation/edition. 
	 * @param event Action event
	 */
	public void abortCancelTest(ActionEvent event)
	{
		updateQuestions();
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
		
		setViewOMEnabled(null);
		setViewQuestionsFromOtherUsersPrivateCategoriesEnabled(null);
		setViewQuestionsFromAdminsPrivateCategoriesEnabled(null);
		setViewQuestionsFromSuperadminsPrivateCategoriesEnabled(null);
		
		Question question=null;
		if (!questionsService.checkQuestionId(operation,questionId))
		{
			addErrorMessage("QUESTION_PREVIEW_NOT_FOUND_ERROR");
		}
		else
		{
			// Get question
			question=questionsService.getQuestion(operation,questionId);
			
			resetViewOmQuestionEnabled(question);
			resetAdminFromQuestionAllowed(question);
			resetSuperadminFromQuestionAllowed(question);
			if (!isViewOMQuestionEnabled(operation,question))
			{
				question=null;
				addErrorMessage("NON_AUTHORIZED_ACTION_ERROR");
			}
		}
		if (question==null)
		{
    		resetViewOMQuestionsEnabled();
    		resetAdmins();
			resetSuperadmins();
			setGlobalOtherUserCategoryAllowed(null);
			setViewTestsFromOtherUsersPrivateCategoriesEnabled(null);
			setViewTestsFromAdminsPrivateCategoriesEnabled(null);
			setViewTestsFromSuperadminsPrivateCategoriesEnabled(null);
    		setFilterGlobalQuestionsEnabled(null);
    		setFilterOtherUsersQuestionsEnabled(null);
			setUseGlobalQuestions(null);
			setUseOtherUsersQuestions(null);
			
			FacesContext facesContext=FacesContext.getCurrentInstance();
			updateSectionQuestions(operation,getActiveSection(facesContext.getViewRoot()));
			
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
		
		if (getTestId()==0L)
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
