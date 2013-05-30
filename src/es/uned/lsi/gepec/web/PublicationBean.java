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
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.AjaxBehaviorEvent;

import org.primefaces.component.tabview.TabView;
import org.primefaces.context.RequestContext;
import org.primefaces.event.TabChangeEvent;

import es.uned.lsi.gepec.model.QuestionLevel;
import es.uned.lsi.gepec.model.entities.Category;
import es.uned.lsi.gepec.model.entities.Question;
import es.uned.lsi.gepec.model.entities.QuestionOrder;
import es.uned.lsi.gepec.model.entities.QuestionRelease;
import es.uned.lsi.gepec.model.entities.QuestionType;
import es.uned.lsi.gepec.model.entities.Section;
import es.uned.lsi.gepec.model.entities.Test;
import es.uned.lsi.gepec.model.entities.TestRelease;
import es.uned.lsi.gepec.model.entities.User;
import es.uned.lsi.gepec.model.entities.Visibility;
import es.uned.lsi.gepec.om.OmHelper;
import es.uned.lsi.gepec.om.QuestionGenerator;
import es.uned.lsi.gepec.om.TestGenerator;
import es.uned.lsi.gepec.util.HibernateUtil.Operation;
import es.uned.lsi.gepec.web.services.CategoriesService;
import es.uned.lsi.gepec.web.services.CategoryTypesService;
import es.uned.lsi.gepec.web.services.ConfigurationService;
import es.uned.lsi.gepec.web.services.EvaluatorsService;
import es.uned.lsi.gepec.web.services.LocalizationService;
import es.uned.lsi.gepec.web.services.PermissionsService;
import es.uned.lsi.gepec.web.services.QuestionReleasesService;
import es.uned.lsi.gepec.web.services.QuestionTypesService;
import es.uned.lsi.gepec.web.services.QuestionsService;
import es.uned.lsi.gepec.web.services.ServiceException;
import es.uned.lsi.gepec.web.services.SupportContactsService;
import es.uned.lsi.gepec.web.services.TestReleasesService;
import es.uned.lsi.gepec.web.services.TestUsersService;
import es.uned.lsi.gepec.web.services.TestsService;
import es.uned.lsi.gepec.web.services.UserSessionService;
import es.uned.lsi.gepec.web.services.VisibilitiesService;

/**
 * Managed bean for questions/tests publication management.
 */
@SuppressWarnings("serial")
@ManagedBean(name="publicationBean")
@ViewScoped
public class PublicationBean implements Serializable
{
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
	
	private final static SpecialCategoryFilter ALL_EVEN_PRIVATE_QUESTION_CATEGORIES_OF_OTHER_USERS;
	static
	{
		List<String> allEvenPrivateQuestionCategoriesOfOtherUsersPermissions=new ArrayList<String>();
		allEvenPrivateQuestionCategoriesOfOtherUsersPermissions.add(
			"PERMISSION_QUESTIONS_GLOBAL_FILTER_ENABLED");
		allEvenPrivateQuestionCategoriesOfOtherUsersPermissions.add(
			"PERMISSION_QUESTIONS_OTHER_USERS_FILTER_ENABLED");
		allEvenPrivateQuestionCategoriesOfOtherUsersPermissions.add(
			"PERMISSION_QUESTIONS_VIEW_QUESTIONS_OF_OTHER_USERS_PRIVATE_CATEGORIES_ENABLED");
		ALL_EVEN_PRIVATE_QUESTION_CATEGORIES_OF_OTHER_USERS=new SpecialCategoryFilter(-1L,
			"ALL_EVEN_PRIVATE_CATEGORIES_OF_OTHER_USERS",allEvenPrivateQuestionCategoriesOfOtherUsersPermissions);
	}
	private final static SpecialCategoryFilter ALL_MY_QUESTION_CATEGORIES;
	static
	{
		List<String> allMyQuestionCategoriesPermissions=new ArrayList<String>();
		allMyQuestionCategoriesPermissions.add("PERMISSION_QUESTIONS_GLOBAL_FILTER_ENABLED");
		ALL_MY_QUESTION_CATEGORIES=
			new SpecialCategoryFilter(-2L,"ALL_MY_CATEGORIES",allMyQuestionCategoriesPermissions);
	}
	private final static SpecialCategoryFilter ALL_MY_QUESTION_CATEGORIES_EXCEPT_GLOBALS=
		new SpecialCategoryFilter(-3L,"ALL_MY_CATEGORIES_EXCEPT_GLOBALS",new ArrayList<String>());
	private final static SpecialCategoryFilter ALL_GLOBAL_QUESTION_CATEGORIES;
	static
	{
		List<String> allGlobalQuestionCategoriesPermissions=new ArrayList<String>();
		allGlobalQuestionCategoriesPermissions.add("PERMISSION_QUESTIONS_GLOBAL_FILTER_ENABLED");
		ALL_GLOBAL_QUESTION_CATEGORIES=
			new SpecialCategoryFilter(-4L,"ALL_GLOBAL_CATEGORIES",allGlobalQuestionCategoriesPermissions);
	}
	private final static SpecialCategoryFilter ALL_PUBLIC_QUESTION_CATEGORIES_OF_OTHER_USERS;
	static
	{
		List<String> allPublicQuestionCategoriesOfOtherUsersPermissions=new ArrayList<String>();
		allPublicQuestionCategoriesOfOtherUsersPermissions.add(
			"PERMISSION_QUESTIONS_OTHER_USERS_FILTER_ENABLED");
		ALL_PUBLIC_QUESTION_CATEGORIES_OF_OTHER_USERS=new SpecialCategoryFilter(
			-5L,"ALL_PUBLIC_CATEGORIES_OF_OTHER_USERS",allPublicQuestionCategoriesOfOtherUsersPermissions);
	}
	private final static SpecialCategoryFilter ALL_PRIVATE_QUESTION_CATEGORIES_OF_OTHER_USERS;
	static
	{
		List<String> allPrivateQuestionCategoriesOfOtherUsersPermissions=new ArrayList<String>();
		allPrivateQuestionCategoriesOfOtherUsersPermissions.add(
			"PERMISSION_QUESTIONS_OTHER_USERS_FILTER_ENABLED");
		allPrivateQuestionCategoriesOfOtherUsersPermissions.add(
			"PERMISSION_QUESTIONS_VIEW_QUESTIONS_OF_OTHER_USERS_PRIVATE_CATEGORIES_ENABLED");
		ALL_PRIVATE_QUESTION_CATEGORIES_OF_OTHER_USERS=new SpecialCategoryFilter(
			-6L,"ALL_PRIVATE_CATEGORIES_OF_OTHER_USERS",allPrivateQuestionCategoriesOfOtherUsersPermissions);
	}
	private final static SpecialCategoryFilter ALL_QUESTION_CATEGORIES_OF_OTHER_USERS;
	static
	{
		List<String> allQuestionCategoriesOfOtherUsersPermissions=new ArrayList<String>();
		allQuestionCategoriesOfOtherUsersPermissions.add("PERMISSION_QUESTIONS_OTHER_USERS_FILTER_ENABLED");
		allQuestionCategoriesOfOtherUsersPermissions.add(
			"PERMISSION_QUESTIONS_VIEW_QUESTIONS_OF_OTHER_USERS_PRIVATE_CATEGORIES_ENABLED");
		ALL_QUESTION_CATEGORIES_OF_OTHER_USERS=new SpecialCategoryFilter(
			-7L,"ALL_CATEGORIES_OF_OTHER_USERS",allQuestionCategoriesOfOtherUsersPermissions);
	}
	private final static SpecialCategoryFilter ALL_EVEN_PRIVATE_TEST_CATEGORIES_OF_OTHER_USERS;
	static
	{
		List<String> allEvenPrivateTestCategoriesOfOtherUsersPermissions=new ArrayList<String>();
		allEvenPrivateTestCategoriesOfOtherUsersPermissions.add("PERMISSION_TESTS_GLOBAL_FILTER_ENABLED");
		allEvenPrivateTestCategoriesOfOtherUsersPermissions.add("PERMISSION_TESTS_OTHER_USERS_FILTER_ENABLED");
		allEvenPrivateTestCategoriesOfOtherUsersPermissions.add(
			"PERMISSION_TESTS_VIEW_TESTS_OF_OTHER_USERS_PRIVATE_CATEGORIES_ENABLED");
		ALL_EVEN_PRIVATE_TEST_CATEGORIES_OF_OTHER_USERS=new SpecialCategoryFilter(
			-1L,"ALL_EVEN_PRIVATE_CATEGORIES_OF_OTHER_USERS",allEvenPrivateTestCategoriesOfOtherUsersPermissions);
	}
	private final static SpecialCategoryFilter ALL_MY_TEST_CATEGORIES;
	static
	{
		List<String> allMyTestCategoriesPermissions=new ArrayList<String>();
		allMyTestCategoriesPermissions.add("PERMISSION_TESTS_GLOBAL_FILTER_ENABLED");
		ALL_MY_TEST_CATEGORIES=
			new SpecialCategoryFilter(-2L,"ALL_MY_CATEGORIES",allMyTestCategoriesPermissions);
	}
	private final static SpecialCategoryFilter ALL_MY_TEST_CATEGORIES_EXCEPT_GLOBALS=
		new SpecialCategoryFilter(-3L,"ALL_MY_CATEGORIES_EXCEPT_GLOBALS",new ArrayList<String>());
	private final static SpecialCategoryFilter ALL_GLOBAL_TEST_CATEGORIES;
	static
	{
		List<String> allGlobalTestCategoriesPermissions=new ArrayList<String>();
		allGlobalTestCategoriesPermissions.add("PERMISSION_TESTS_GLOBAL_FILTER_ENABLED");
		ALL_GLOBAL_TEST_CATEGORIES=
			new SpecialCategoryFilter(-4L,"ALL_GLOBAL_CATEGORIES",allGlobalTestCategoriesPermissions);
	}
	private final static SpecialCategoryFilter ALL_PUBLIC_TEST_CATEGORIES_OF_OTHER_USERS;
	static
	{
		List<String> allPublicTestCategoriesOfOtherUsersPermissions=new ArrayList<String>();
		allPublicTestCategoriesOfOtherUsersPermissions.add("PERMISSION_TESTS_OTHER_USERS_FILTER_ENABLED");
		ALL_PUBLIC_TEST_CATEGORIES_OF_OTHER_USERS=new SpecialCategoryFilter(
			-5L,"ALL_PUBLIC_CATEGORIES_OF_OTHER_USERS",allPublicTestCategoriesOfOtherUsersPermissions);
	}
	private final static SpecialCategoryFilter ALL_PRIVATE_TEST_CATEGORIES_OF_OTHER_USERS;
	static
	{
		List<String> allPrivateTestCategoriesOfOtherUsersPermissions=new ArrayList<String>();
		allPrivateTestCategoriesOfOtherUsersPermissions.add("PERMISSION_TESTS_OTHER_USERS_FILTER_ENABLED");
		allPrivateTestCategoriesOfOtherUsersPermissions.add(
			"PERMISSION_TESTS_VIEW_TESTS_OF_OTHER_USERS_PRIVATE_CATEGORIES_ENABLED");
		ALL_PRIVATE_TEST_CATEGORIES_OF_OTHER_USERS=new SpecialCategoryFilter(
			-6L,"ALL_PRIVATE_CATEGORIES_OF_OTHER_USERS",allPrivateTestCategoriesOfOtherUsersPermissions);
	}
	private final static SpecialCategoryFilter ALL_TEST_CATEGORIES_OF_OTHER_USERS;
	static
	{
		List<String> allTestCategoriesOfOtherUsersPermissions=new ArrayList<String>();
		allTestCategoriesOfOtherUsersPermissions.add("PERMISSION_TESTS_OTHER_USERS_FILTER_ENABLED");
		allTestCategoriesOfOtherUsersPermissions.add(
			"PERMISSION_TESTS_VIEW_TESTS_OF_OTHER_USERS_PRIVATE_CATEGORIES_ENABLED");
		ALL_TEST_CATEGORIES_OF_OTHER_USERS=new SpecialCategoryFilter(
			-7L,"ALL_CATEGORIES_OF_OTHER_USERS",allTestCategoriesOfOtherUsersPermissions);
	}
	private final static List<String> ALL_PUBLICATION_STATUS;
	static
	{
		ALL_PUBLICATION_STATUS=new ArrayList<String>();
		ALL_PUBLICATION_STATUS.add("PUBLICATION_STATUS_RELEASED");
		ALL_PUBLICATION_STATUS.add("PUBLICATION_STATUS_NOT_RELEASED");
		ALL_PUBLICATION_STATUS.add("PUBLICATION_STATUS_ALL");
	}
	
	private final static int QUESTIONS_TABVIEW_TAB=0;
	private final static int TESTS_TABVIEW_TAB=1;
	
	private final static String BUILD_QUESTION="DEPLOYING_QUESTION";
	private final static String BUILD_TEST="DEPLOYING_TEST";
	
	private final static String CONFIRM_UNPUBLISH_QUESTION_RELEASE="CONFIRM_UNPUBLISH_QUESTION_RELEASE";
	private final static String CONFIRM_UNPUBLISH_TEST_RELEASE="CONFIRM_UNPUBLISH_TEST_RELEASE";
	
	private final static String CONFIRM_OM_VIEW_QUESTION_RELEASE="CONFIRM_OM_VIEW_QUESTION_RELEASE";
	private final static String CONFIRM_OM_VIEW_TEST_RELEASE="CONFIRM_OM_VIEW_TEST_RELEASE";
	
	@ManagedProperty(value="#{localizationService}")
	private LocalizationService localizationService;
	@ManagedProperty(value="#{configurationService}")
	private ConfigurationService configurationService;
	@ManagedProperty(value="#{questionsService}")
	private QuestionsService questionsService;
	@ManagedProperty(value="#{questionTypesService}")
	private QuestionTypesService questionTypesService;
	@ManagedProperty(value="#{questionReleasesService}")
	private QuestionReleasesService questionReleasesService;
	@ManagedProperty(value="#{testsService}")
	private TestsService testsService;
	@ManagedProperty(value="#{testUsersService}")
	private TestUsersService testUsersService;
	@ManagedProperty(value="#{testReleasesService}")
	private TestReleasesService testReleasesService;
	@ManagedProperty(value="#{categoriesService}")
	private CategoriesService categoriesService;
	@ManagedProperty(value="#{categoryTypesService}")
	private CategoryTypesService categoryTypesService;
	@ManagedProperty(value="#{visibilitiesService}")
	private VisibilitiesService visibilitiesService;
	@ManagedProperty(value="#{supportContactsService}")
	private SupportContactsService supportContactsService;
	@ManagedProperty(value="#{evaluatorsService}")
	private EvaluatorsService evaluatorsService;
	@ManagedProperty(value="#{userSessionService}")
	private UserSessionService userSessionService;
	@ManagedProperty(value="#{permissionsService}")
	private PermissionsService permissionsService;
	
	private QuestionRelease currentQuestionRelease;
	
	private List<QuestionRelease> questionsReleases;			// List of questions releases
	private List<Question> questions;							// List of questions
	private List<TestRelease> testsReleases;					// List of tests releases
	private List<Test> tests;									// List of questions
	
	private Map<Long,SpecialCategoryFilter> specialQuestionCategoryFiltersMap;
	private SpecialCategoryFilter allQuestionCategories;
	private Map<Long,SpecialCategoryFilter> specialTestCategoryFiltersMap;
	private SpecialCategoryFilter allTestCategories;
	
	private List<Category> specialQuestionCategoriesFilters;	// Special question categories filters list
	private List<Category> questionsCategories;					// Questions categories list
	private List<Category> specialTestCategoriesFilters;		// Special test categories filters list
	private List<Category> testsCategories;
	
	private String buildType;
	private String confirmType;
	
	private QuestionRelease questionRelease;
	private TestRelease testRelease;
	
	private Boolean questionsPublicationEnabled;
	private Boolean testsPublicationEnabled;
	
	private boolean criticalErrorMessage;
	
	private long filterQuestionCategoryId;
	private boolean filterIncludeQuestionSubcategories;
	private String filterQuestionType;
	private String filterQuestionLevel;
	private String filterQuestionPublicationStatus;
	private Boolean filterGlobalQuestionsEnabled;
	private Boolean filterOtherUsersQuestionsEnabled;
	private Boolean viewOMQuestionsEnabled;
	private Boolean publishQuestionsEnabled;
	private Boolean publishOtherUsersQuestionsEnabled;
	private Boolean publishAdminsQuestionsEnabled;
	private Boolean publishSuperadminsQuestionsEnabled;
	private Boolean unpublishQuestionReleasesEnabled;
	private Boolean unpublishOtherUsersQuestionReleasesEnabled;
	private Boolean unpublishAdminsQuestionReleasesEnabled;
	private Boolean unpublishSuperadminsQuestionReleasesEnabled;
	private Boolean unpublishQuestionReleasesOpenedWithCloseDateEnabled;
	private Boolean unpublishQuestionReleasesBeforeDeleteDateEnabled;
	private Boolean viewQuestionsFromOtherUsersPrivateCategoriesEnabled;
	private Boolean viewQuestionsFromAdminsPrivateCategoriesEnabled;
	private Boolean viewQuestionsFromSuperadminsPrivateCategoriesEnabled;
	
	private long filterTestCategoryId;
	private boolean filterIncludeTestSubcategories;
	private String filterTestPublicationStatus;
	private boolean filterTestDisplayOldVersions;
	private Boolean filterGlobalTestsEnabled;
	private Boolean filterOtherUsersTestsEnabled;
	private Boolean viewOMTestsEnabled;
	private Boolean publishTestsEnabled;
	private Boolean publishOtherUsersTestsEnabled;
	private Boolean publishAdminsTestsEnabled;
	private Boolean publishSuperadminsTestsEnabled;
	private Boolean unpublishTestReleasesEnabled;
	private Boolean unpublishOtherUsersTestReleasesEnabled;
	private Boolean unpublishAdminsTestReleasesEnabled;
	private Boolean unpublishSuperadminsTestReleasesEnabled;
	private Boolean unpublishTestReleasesOpenedWithCloseDateEnabled;
	private Boolean unpublishTestReleasesBeforeDeleteDateEnabled;
	private Boolean viewTestsFromOtherUsersPrivateCategoriesEnabled;
	private Boolean viewTestsFromAdminsPrivateCategoriesEnabled;
	private Boolean viewTestsFromSuperadminsPrivateCategoriesEnabled;
	
	private Map<Long,Boolean> admins;
	private Map<Long,Boolean> superadmins;
	
	private Map<Long,Boolean> publishQuestionsAllowed;
	private Map<Long,Boolean> publishTestsAllowed;
	
	private Map<QuestionRelease,Boolean> unpublishQuestionReleasesAllowed;
	private Map<TestRelease,Boolean> unpublishTestReleasesAllowed;
	
	private List<QuestionLevel> questionLevels;
	
	public PublicationBean()
	{
		buildType=BUILD_QUESTION;
		criticalErrorMessage=false;
		questionsPublicationEnabled=null;
		testsPublicationEnabled=null;
		filterQuestionCategoryId=Long.MIN_VALUE;
		filterIncludeQuestionSubcategories=false;
		specialQuestionCategoryFiltersMap=null;
		filterQuestionType="";
		filterQuestionLevel="";
		filterQuestionPublicationStatus="PUBLICATION_STATUS_RELEASED";
		specialQuestionCategoriesFilters=null;
		allQuestionCategories=null;
		questionsCategories=null;
		filterGlobalQuestionsEnabled=null;
		filterOtherUsersQuestionsEnabled=null;
		viewOMQuestionsEnabled=null;
		publishQuestionsEnabled=null;
		publishOtherUsersQuestionsEnabled=null;
		publishAdminsQuestionsEnabled=null;
		publishSuperadminsQuestionsEnabled=null;
		unpublishQuestionReleasesEnabled=null;
		unpublishOtherUsersQuestionReleasesEnabled=null;;
		unpublishAdminsQuestionReleasesEnabled=null;
		unpublishSuperadminsQuestionReleasesEnabled=null;
		unpublishQuestionReleasesOpenedWithCloseDateEnabled=null;
		viewQuestionsFromOtherUsersPrivateCategoriesEnabled=null;
		viewQuestionsFromAdminsPrivateCategoriesEnabled=null;
		viewQuestionsFromSuperadminsPrivateCategoriesEnabled=null;
		filterTestCategoryId=Long.MIN_VALUE;
		filterIncludeTestSubcategories=false;
		specialTestCategoryFiltersMap=null;
		filterTestPublicationStatus="PUBLICATION_STATUS_RELEASED";
		filterTestDisplayOldVersions=false;
		specialTestCategoriesFilters=null;
		allTestCategories=null;
		testsCategories=null;
		filterGlobalTestsEnabled=null;
		filterOtherUsersTestsEnabled=null;
		viewOMTestsEnabled=null;
		publishTestsEnabled=null;
		publishOtherUsersTestsEnabled=null;
		publishAdminsTestsEnabled=null;
		publishSuperadminsTestsEnabled=null;
		unpublishTestReleasesEnabled=null;
		unpublishOtherUsersTestReleasesEnabled=null;
		unpublishAdminsTestReleasesEnabled=null;
		unpublishSuperadminsTestReleasesEnabled=null;
		unpublishTestReleasesOpenedWithCloseDateEnabled=null;
		viewTestsFromOtherUsersPrivateCategoriesEnabled=null;
		viewTestsFromAdminsPrivateCategoriesEnabled=null;
		viewTestsFromSuperadminsPrivateCategoriesEnabled=null;
		admins=new HashMap<Long,Boolean>();
		superadmins=new HashMap<Long,Boolean>();
		publishQuestionsAllowed=new HashMap<Long,Boolean>();
		publishTestsAllowed=new HashMap<Long,Boolean>();
		unpublishQuestionReleasesAllowed=new HashMap<QuestionRelease,Boolean>();
		unpublishTestReleasesAllowed=new HashMap<TestRelease,Boolean>();
		currentQuestionRelease=null;
		questionsReleases=null;
		questions=null;
		testsReleases=null;
		tests=null;
		questionLevels=null;
	}
	
	public void setLocalizationService(LocalizationService localizationService)
	{
		this.localizationService=localizationService;
	}
	
	public void setConfigurationService(ConfigurationService configurationService)
	{
		this.configurationService=configurationService;
	}
	
	public void setQuestionsService(QuestionsService questionsService)
	{
		this.questionsService=questionsService;
	}
	
	public void setQuestionTypesService(QuestionTypesService questionTypesService)
	{
		this.questionTypesService=questionTypesService;
	}
	
	public void setQuestionReleasesService(QuestionReleasesService questionReleasesService)
	{
		this.questionReleasesService=questionReleasesService;
	}
	
	public void setTestsService(TestsService testsService)
	{
		this.testsService=testsService;
	}
	
	public void setTestUsersService(TestUsersService testUsersService)
	{
		this.testUsersService=testUsersService;
	}
	
	public void setTestReleasesService(TestReleasesService testReleasesService)
	{
		this.testReleasesService=testReleasesService;
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
	
	public void setSupportContactsService(SupportContactsService supportContactsService)
	{
		this.supportContactsService=supportContactsService;
	}
	
	public void setEvaluatorsService(EvaluatorsService evaluatorsService)
	{
		this.evaluatorsService=evaluatorsService;
	}
	
	public void setUserSessionService(UserSessionService userSessionService)
	{
		this.userSessionService=userSessionService;
	}
	
	public void setPermissionsService(PermissionsService permissionsService)
	{
		this.permissionsService=permissionsService;
	}
	
    private Operation getCurrentUserOperation(Operation operation)
    {
    	return operation==null?userSessionService.getCurrentUserOperation():operation;
    }
	
    /**
     * @return true if current user is allowed to navigate "Publication" page, false otherwise
     */
    public boolean isNavigationAllowed()
    {
    	return isNavigationAllowed(null);
    }
    
    /**
     * @param operation Operation
     * @return true if current user is allowed to navigate "Publication" page, false otherwise
     */
    private boolean isNavigationAllowed(Operation operation)
    {
    	return userSessionService.isGranted(getCurrentUserOperation(operation),"PERMISSION_NAVIGATION_PUBLICATION");
    }
    
	public String getBuildType()
	{
		return buildType;
	}
	
	public void setBuildType(String buildType)
	{
		this.buildType=buildType;
	}
	
	public String getBuildMessage()
	{
		return getBuildType()==null?"":localizationService.getLocalizedMessage(getBuildType());
	}
	
	public String getConfirmType()
	{
		return confirmType;
	}
	
	public void setConfirmType(String confirmType)
	{
		this.confirmType=confirmType;
	}
	
	public String getConfirmMessage()
	{
		return getConfirmType()==null?"":localizationService.getLocalizedMessage(getConfirmType());
	}
	
	public QuestionRelease getQuestionRelease()
	{
		return questionRelease;
	}
	
	public void setQuestionRelease(QuestionRelease questionRelease)
	{
		this.questionRelease=questionRelease;
	}
	
	public TestRelease getTestRelease()
	{
		return testRelease;
	}
	
	public void setTestRelease(TestRelease testRelease)
	{
		this.testRelease=testRelease;
	}
	
	/**
	 * Action listener to confirm question/test release deletion.
	 * @param event Action event
	 */
	public void confirm(ActionEvent event)
	{
		Object questionReleaseObj=event.getComponent().getAttributes().get("questionRelease");
		Object testReleaseObj=event.getComponent().getAttributes().get("testRelease");
		setConfirmType(null);
		if (questionReleaseObj!=null)
		{
			setConfirmType(CONFIRM_UNPUBLISH_QUESTION_RELEASE);
			setQuestionRelease((QuestionRelease)questionReleaseObj);
		}
		else if (testReleaseObj!=null)
		{
			setConfirmType(CONFIRM_UNPUBLISH_TEST_RELEASE);
			setTestRelease((TestRelease)testReleaseObj);
		}
		if (getConfirmType()!=null)
		{
			RequestContext rq=RequestContext.getCurrentInstance();
			rq.execute("confirmDialog.show()");
		}
	}
	
	/**
	 * Action listener to confirm viewing question/test release on 
	 * OpenMark Test Navigator production environment.
	 * @param event Action event
	 */
	public void confirmOMView(ActionEvent event)
	{
		Object questionReleaseObj=event.getComponent().getAttributes().get("questionRelease");
		Object testReleaseObj=event.getComponent().getAttributes().get("testRelease");
		setConfirmType(null);
		if (questionReleaseObj!=null)
		{
			setConfirmType(CONFIRM_OM_VIEW_QUESTION_RELEASE);
			setQuestionRelease((QuestionRelease)questionReleaseObj);
		}
		else if (testReleaseObj!=null)
		{
			setConfirmType(CONFIRM_OM_VIEW_TEST_RELEASE);
			setTestRelease((TestRelease)testReleaseObj);
		}
		if (getConfirmType()!=null)
		{
			RequestContext rq=RequestContext.getCurrentInstance();
			rq.execute("confirmDialog.show()");
		}
	}
	
	private void initializeFilterQuestionCategoryId(Operation operation)
	{
		boolean found=false;
		List<Category> specialQuestionCategoriesFilters=
			getSpecialQuestionCategoriesFilters(getCurrentUserOperation(operation));
		Category allMyQuestionCategoriesFilter=new Category();
		allMyQuestionCategoriesFilter.setId(ALL_MY_QUESTION_CATEGORIES.id);
		if (specialQuestionCategoriesFilters.contains(allMyQuestionCategoriesFilter))
		{
			filterQuestionCategoryId=ALL_MY_QUESTION_CATEGORIES.id;
			found=true;
		}
		if (!found)
		{
			Category allMyQuestionCategoriesExceptGlobalsFilter=new Category();
			allMyQuestionCategoriesExceptGlobalsFilter.setId(ALL_MY_QUESTION_CATEGORIES_EXCEPT_GLOBALS.id);
			if (specialQuestionCategoriesFilters.contains(allMyQuestionCategoriesExceptGlobalsFilter))
			{
				filterQuestionCategoryId=ALL_MY_QUESTION_CATEGORIES_EXCEPT_GLOBALS.id;
				found=true;
			}
		}
		if (!found)
		{
			Category allGlobalQuestionCategoriesFilter=new Category();
			allGlobalQuestionCategoriesFilter.setId(ALL_GLOBAL_QUESTION_CATEGORIES.id);
			if (specialQuestionCategoriesFilters.contains(allGlobalQuestionCategoriesFilter))
			{
				filterQuestionCategoryId=ALL_GLOBAL_QUESTION_CATEGORIES.id;
				found=true;
			}
		}
		if (!found)
		{
			Category allQuestionFilter=new Category();
			SpecialCategoryFilter allQuestionCategories=getAllQuestionCategoriesSpecialCategoryFilter();
			allQuestionFilter.setId(allQuestionCategories.id);
			if (specialQuestionCategoriesFilters.contains(allQuestionFilter))
			{
				filterQuestionCategoryId=allQuestionCategories.id;
				found=true;
			}
		}
		if (!found && !specialQuestionCategoriesFilters.isEmpty())
		{
			filterQuestionCategoryId=specialQuestionCategoriesFilters.get(0).getId();
		}
	}
	
	public long getFilterQuestionCategoryId()
	{
		return getFilterQuestionCategoryId(null);
	}
	
	public void setFilterQuestionCategoryId(long filterQuestionCategoryId)
	{
		this.filterQuestionCategoryId=filterQuestionCategoryId;
	}
	
	private long getFilterQuestionCategoryId(Operation operation)
	{
		if (filterQuestionCategoryId==Long.MIN_VALUE)
		{
			initializeFilterQuestionCategoryId(getCurrentUserOperation(operation));
		}
		return filterQuestionCategoryId;
	}
	
	public boolean isFilterIncludeQuestionSubcategories()
	{
		return filterIncludeQuestionSubcategories;
	}
	
	public void setFilterIncludeQuestionSubcategories(boolean filterIncludeQuestionSubcategories)
	{
		this.filterIncludeQuestionSubcategories=filterIncludeQuestionSubcategories;
	}
	
	public String getFilterQuestionType()
	{
		return filterQuestionType;
	}
	
	public void setFilterQuestionType(String filterQuestionType)
	{
		this.filterQuestionType=filterQuestionType;
	}
	
	public String getFilterQuestionLevel()
	{
		return filterQuestionLevel;
	}
	
	public void setFilterQuestionLevel(String filterQuestionLevel)
	{
		this.filterQuestionLevel=filterQuestionLevel;
	}
	
	public String getFilterQuestionPublicationStatus()
	{
		return filterQuestionPublicationStatus;
	}
	
	public void setFilterQuestionPublicationStatus(String filterQuestionPublicationStatus)
	{
		this.filterQuestionPublicationStatus=filterQuestionPublicationStatus;
	}
	
	private void initializeFilterTestCategoryId(Operation operation)
	{
		boolean found=false;
		List<Category> specialTestCategoriesFilters=getSpecialTestCategoriesFilters(getCurrentUserOperation(operation));
		Category allMyTestCategoriesFilter=new Category();
		allMyTestCategoriesFilter.setId(ALL_MY_TEST_CATEGORIES.id);
		if (specialTestCategoriesFilters.contains(allMyTestCategoriesFilter))
		{
			filterTestCategoryId=ALL_MY_TEST_CATEGORIES.id;
			found=true;
		}
		if (!found)
		{
			Category allMyTestCategoriesExceptGlobalsFilter=new Category();
			allMyTestCategoriesExceptGlobalsFilter.setId(ALL_MY_TEST_CATEGORIES_EXCEPT_GLOBALS.id);
			if (specialTestCategoriesFilters.contains(allMyTestCategoriesExceptGlobalsFilter))
			{
				filterTestCategoryId=ALL_MY_TEST_CATEGORIES_EXCEPT_GLOBALS.id;
				found=true;
			}
		}
		if (!found)
		{
			Category allGlobalTestCategoriesFilter=new Category();
			allGlobalTestCategoriesFilter.setId(ALL_GLOBAL_TEST_CATEGORIES.id);
			if (specialTestCategoriesFilters.contains(allGlobalTestCategoriesFilter))
			{
				filterTestCategoryId=ALL_GLOBAL_TEST_CATEGORIES.id;
				found=true;
			}
		}
		if (!found)
		{
			Category allTestFilter=new Category();
			SpecialCategoryFilter allTestCategories=getAllTestCategoriesSpecialCategoryFilter();
			allTestFilter.setId(allTestCategories.id);
			if (specialTestCategoriesFilters.contains(allTestFilter))
			{
				filterTestCategoryId=allTestCategories.id;
				found=true;
			}
		}
		if (!found && !specialTestCategoriesFilters.isEmpty())
		{
			filterTestCategoryId=specialTestCategoriesFilters.get(0).getId();
		}
	}
	
	public long getFilterTestCategoryId()
	{
		return getFilterTestCategoryId(null);
	}
	
	public void setFilterTestCategoryId(long filterTestCategoryId)
	{
		this.filterTestCategoryId=filterTestCategoryId;
	}
	
	private long getFilterTestCategoryId(Operation operation)
	{
		if (filterTestCategoryId==Long.MIN_VALUE)
		{
			initializeFilterTestCategoryId(getCurrentUserOperation(operation));
		}
		return filterTestCategoryId;
	}
	
	public boolean isFilterIncludeTestSubcategories()
	{
		return filterIncludeTestSubcategories;
	}
	
	public void setFilterIncludeTestSubcategories(boolean filterIncludeTestSubcategories)
	{
		this.filterIncludeTestSubcategories=filterIncludeTestSubcategories;
	}
	
	public String getFilterTestPublicationStatus()
	{
		return filterTestPublicationStatus;
	}
	
	public void setFilterTestPublicationStatus(String filterTestPublicationStatus)
	{
		this.filterTestPublicationStatus=filterTestPublicationStatus;
	}
	
	public boolean isFilterTestDisplayOldVersions()
	{
		return filterTestDisplayOldVersions;
	}
	
	public void setFilterTestDisplayOldVersions(boolean filterTestDisplayOldVersions)
	{
		this.filterTestDisplayOldVersions=filterTestDisplayOldVersions;
	}
	
	public boolean isCriticalErrorMessage()
	{
		return criticalErrorMessage;
	}
	
	public void setCriticalErrorMessage(boolean criticalErrorMessage)
	{
		this.criticalErrorMessage=criticalErrorMessage;
	}
	
	public boolean isFilterTestDisplayOldVersionsEnabled()
	{
		return !"PUBLICATION_STATUS_NOT_RELEASED".equals(getFilterTestPublicationStatus());
	}
	
	public boolean isPublicationEnabled()
	{
		return isPublicationEnabled(null);
	}
	
	private boolean isPublicationEnabled(Operation operation)
	{
		// Get current user session Hibernate operation
		operation=getCurrentUserOperation(operation);
		
		return getQuestionsPublicationEnabled(operation).booleanValue() || 
			getTestsPublicationEnabled(operation).booleanValue();
	}
	
	public Boolean getQuestionsPublicationEnabled()
	{
		return getQuestionsPublicationEnabled(null);
	}
	
	public void setQuestionsPublicationEnabled(Boolean questionsPublicationEnabled)
	{
		this.questionsPublicationEnabled=questionsPublicationEnabled;
	}
	
	public boolean isQuestionsPublicationEnabled()
	{
		return getQuestionsPublicationEnabled().booleanValue();
	}
	
	private Boolean getQuestionsPublicationEnabled(Operation operation)
	{
		if (questionsPublicationEnabled==null)
		{
			questionsPublicationEnabled=Boolean.valueOf(userSessionService.isGranted(
				getCurrentUserOperation(operation),"PERMISSION_PUBLICATION_QUESTIONS"));
		}
		return questionsPublicationEnabled;
	}
	
	public Boolean getTestsPublicationEnabled()
	{
		return getTestsPublicationEnabled(null);
	}
	
	public void setTestsPublicationEnabled(Boolean testsPublicationEnabled)
	{
		this.testsPublicationEnabled=testsPublicationEnabled;
	}
	
	public boolean isTestsPublicationEnabled()
	{
		return getTestsPublicationEnabled().booleanValue();
	}
	
	private Boolean getTestsPublicationEnabled(Operation operation)
	{
		if (testsPublicationEnabled==null)
		{
			testsPublicationEnabled=Boolean.valueOf(
				userSessionService.isGranted(getCurrentUserOperation(operation),"PERMISSION_PUBLICATION_TESTS"));
		}
		return testsPublicationEnabled;
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
	
	public Boolean getViewOMQuestionsEnabled()
	{
		return getViewOMQuestionsEnabled(null);
	}
	
	public void setViewOMQuestionsEnabled(Boolean viewOMQuestionsEnabled)
	{
		this.viewOMQuestionsEnabled=viewOMQuestionsEnabled;
	}
	
	public boolean isViewOMQuestionsEnabled()
	{
		return getViewOMQuestionsEnabled().booleanValue();
	}
	
	private Boolean getViewOMQuestionsEnabled(Operation operation)
	{
		if (viewOMQuestionsEnabled==null)
		{
			viewOMQuestionsEnabled=Boolean.valueOf(
				userSessionService.isGranted(getCurrentUserOperation(operation),"PERMISSION_QUESTIONS_VIEW_OM_ENABLED"));
		}
		return viewOMQuestionsEnabled;
	}
	
	public Boolean getPublishQuestionsEnabled()
	{
		return getPublishQuestionsEnabled(null);
	}
	
	public void setPublishQuestionsEnabled(Boolean publishQuestionsEnabled)
	{
		this.publishQuestionsEnabled=publishQuestionsEnabled;
	}
	
	public boolean isPublishQuestionsEnabled()
	{
		return getPublishQuestionsEnabled().booleanValue();
	}
	
	private Boolean getPublishQuestionsEnabled(Operation operation)
	{
		if (publishQuestionsEnabled==null)
		{
			publishQuestionsEnabled=Boolean.valueOf(userSessionService.isGranted(
				getCurrentUserOperation(operation),"PERMISSION_PUBLICATION_PUBLISH_QUESTIONS_ENABLED"));
		}
		return publishQuestionsEnabled;
	}
	
	public Boolean getPublishOtherUsersQuestionsEnabled()
	{
		return getPublishOtherUsersQuestionsEnabled(null);
	}
	
	public void setPublishOtherUsersQuestionsEnabled(Boolean publishOtherUsersQuestionsEnabled)
	{
		this.publishOtherUsersQuestionsEnabled=publishOtherUsersQuestionsEnabled;
	}
	
	public boolean isPublishOtherUsersQuestionsEnabled()
	{
		return getPublishOtherUsersQuestionsEnabled().booleanValue();
	}
	
	private Boolean getPublishOtherUsersQuestionsEnabled(Operation operation)
	{
		if (publishOtherUsersQuestionsEnabled==null)
		{
			publishOtherUsersQuestionsEnabled=Boolean.valueOf(userSessionService.isGranted(
				getCurrentUserOperation(operation),"PERMISSION_PUBLICATION_PUBLISH_OTHER_USERS_QUESTIONS_ENABLED"));
		}
		return publishOtherUsersQuestionsEnabled;
	}
	
	public Boolean getPublishAdminsQuestionsEnabled()
	{
		return getPublishAdminsQuestionsEnabled(null);
	}
	
	public void setPublishAdminsQuestionsEnabled(Boolean publishAdminsQuestionsEnabled)
	{
		this.publishAdminsQuestionsEnabled=publishAdminsQuestionsEnabled;
	}
	
	public boolean isPublishAdminsQuestionsEnabled()
	{
		return getPublishAdminsQuestionsEnabled().booleanValue();
	}
	
	private Boolean getPublishAdminsQuestionsEnabled(Operation operation)
	{
		if (publishAdminsQuestionsEnabled==null)
		{
			publishAdminsQuestionsEnabled=Boolean.valueOf(userSessionService.isGranted(
				getCurrentUserOperation(operation),"PERMISSION_PUBLICATION_PUBLISH_ADMINS_QUESTIONS_ENABLED"));
		}
		return publishAdminsQuestionsEnabled;
	}
	
	public Boolean getPublishSuperadminsQuestionsEnabled()
	{
		return getPublishSuperadminsQuestionsEnabled(null);
	}
	
	public void setPublishSuperadminsQuestionsEnabled(Boolean publishSuperadminsQuestionsEnabled)
	{
		this.publishSuperadminsQuestionsEnabled=publishSuperadminsQuestionsEnabled;
	}
	
	public boolean isPublishSuperadminsQuestionsEnabled()
	{
		return getPublishSuperadminsQuestionsEnabled().booleanValue();
	}
	
	private Boolean getPublishSuperadminsQuestionsEnabled(Operation operation)
	{
		if (publishSuperadminsQuestionsEnabled==null)
		{
			publishSuperadminsQuestionsEnabled=Boolean.valueOf(userSessionService.isGranted(
				getCurrentUserOperation(operation),"PERMISSION_PUBLICATION_PUBLISH_SUPERADMINS_QUESTIONS_ENABLED"));
		}
		return publishSuperadminsQuestionsEnabled;
	}
	
	public Boolean getUnpublishQuestionReleasesEnabled()
	{
		return getUnpublishQuestionReleasesEnabled(null);
	}
	
	public void setUnpublishQuestionReleasesEnabled(Boolean unpublishQuestionReleasesEnabled)
	{
		this.unpublishQuestionReleasesEnabled=unpublishQuestionReleasesEnabled;
	}
	
	public boolean isUnpublishQuestionReleasesEnabled()
	{
		return getUnpublishQuestionReleasesEnabled().booleanValue();
	}
	
	private Boolean getUnpublishQuestionReleasesEnabled(Operation operation)
	{
		if (unpublishQuestionReleasesEnabled==null)
		{
			unpublishQuestionReleasesEnabled=Boolean.valueOf(userSessionService.isGranted(
				getCurrentUserOperation(operation),"PERMISSION_PUBLICATION_UNPUBLISH_QUESTION_RELEASES_ENABLED"));
		}
		return unpublishQuestionReleasesEnabled;
	}
	
	public Boolean getUnpublishOtherUsersQuestionReleasesEnabled()
	{
		return getUnpublishOtherUsersQuestionReleasesEnabled(null);
	}
	
	public void setUnpublishOtherUsersQuestionReleasesEnabled(
		Boolean unpublishOtherUsersQuestionReleasesEnabled)
	{
		this.unpublishOtherUsersQuestionReleasesEnabled=unpublishOtherUsersQuestionReleasesEnabled;
	}
	
	public boolean isUnpublishOtherUsersQuestionReleasesEnabled()
	{
		return getUnpublishOtherUsersQuestionReleasesEnabled().booleanValue();
	}
	
	private Boolean getUnpublishOtherUsersQuestionReleasesEnabled(Operation operation)
	{
		if (unpublishOtherUsersQuestionReleasesEnabled==null)
		{
			unpublishOtherUsersQuestionReleasesEnabled=
				Boolean.valueOf(userSessionService.isGranted(getCurrentUserOperation(operation),
				"PERMISSION_PUBLICATION_UNPUBLISH_OTHER_USERS_QUESTION_RELEASES_ENABLED"));
		}
		return unpublishOtherUsersQuestionReleasesEnabled;
	}
	
	public Boolean getUnpublishAdminsQuestionReleasesEnabled()
	{
		return getUnpublishAdminsQuestionReleasesEnabled(null);
	}
	
	public void setUnpublishAdminsQuestionReleasesEnabled(Boolean unpublishAdminsQuestionReleasesEnabled)
	{
		this.unpublishAdminsQuestionReleasesEnabled=unpublishAdminsQuestionReleasesEnabled;
	}
	
	public boolean isUnpublishAdminsQuestionReleasesEnabled()
	{
		return getUnpublishAdminsQuestionReleasesEnabled().booleanValue();
	}
	
	private Boolean getUnpublishAdminsQuestionReleasesEnabled(Operation operation)
	{
		if (unpublishAdminsQuestionReleasesEnabled==null)
		{
			unpublishAdminsQuestionReleasesEnabled=Boolean.valueOf(userSessionService.isGranted(
				getCurrentUserOperation(operation),"PERMISSION_PUBLICATION_UNPUBLISH_ADMINS_QUESTION_RELEASES_ENABLED"));
		}
		return unpublishAdminsQuestionReleasesEnabled;
	}
	
	public Boolean getUnpublishSuperadminsQuestionReleasesEnabled()
	{
		return getUnpublishSuperadminsQuestionReleasesEnabled(null);
	}
	
	public void setUnpublishSuperadminsQuestionReleasesEnabled(
		Boolean unpublishSuperadminsQuestionReleasesEnabled)
	{
		this.unpublishSuperadminsQuestionReleasesEnabled=unpublishSuperadminsQuestionReleasesEnabled;
	}
	
	public boolean isUnpublishSuperadminsQuestionReleasesEnabled()
	{
		return getUnpublishSuperadminsQuestionReleasesEnabled().booleanValue();
	}
	
	private Boolean getUnpublishSuperadminsQuestionReleasesEnabled(Operation operation)
	{
		if (unpublishSuperadminsQuestionReleasesEnabled==null)
		{
			unpublishSuperadminsQuestionReleasesEnabled=
				Boolean.valueOf(userSessionService.isGranted(getCurrentUserOperation(operation),
				"PERMISSION_PUBLICATION_UNPUBLISH_SUPERADMINS_QUESTION_RELEASES_ENABLED"));
		}
		return unpublishSuperadminsQuestionReleasesEnabled;
	}
	
	public Boolean getUnpublishQuestionReleasesOpenedWithCloseDateEnabled()
	{
		return getUnpublishQuestionReleasesOpenedWithCloseDateEnabled(null);
	}
	
	public void setUnpublishQuestionReleasesOpenedWithCloseDateEnabled(
		Boolean unpublishQuestionReleasesOpenedWithCloseDateEnabled)
	{
		this.unpublishQuestionReleasesOpenedWithCloseDateEnabled=
			unpublishQuestionReleasesOpenedWithCloseDateEnabled;
	}
	
	public boolean isUnpublishQuestionReleasesOpenedWithCloseDateEnabled()
	{
		return getUnpublishQuestionReleasesOpenedWithCloseDateEnabled().booleanValue();
	}
	
	private Boolean getUnpublishQuestionReleasesOpenedWithCloseDateEnabled(Operation operation)
	{
		if (unpublishQuestionReleasesOpenedWithCloseDateEnabled==null)
		{
			unpublishQuestionReleasesOpenedWithCloseDateEnabled=
				Boolean.valueOf(userSessionService.isGranted(getCurrentUserOperation(operation),
				"PERMISSION_PUBLICATION_UNPUBLISH_QUESTION_RELEASES_OPENED_WITH_CLOSE_DATE_ENABLED"));
		}
		return unpublishQuestionReleasesOpenedWithCloseDateEnabled;
	}
	
	public Boolean getUnpublishQuestionReleasesBeforeDeleteDateEnabled()
	{
		return getUnpublishQuestionReleasesBeforeDeleteDateEnabled(null);
	}
	
	public void setUnpublishQuestionReleasesBeforeDeleteDateEnabled(
		Boolean unpublishQuestionReleasesBeforeDeleteDateEnabled)
	{
		this.unpublishQuestionReleasesBeforeDeleteDateEnabled=
			unpublishQuestionReleasesBeforeDeleteDateEnabled;
	}
	
	public boolean isUnpublishQuestionReleasesBeforeDeleteDateEnabled()
	{
		return getUnpublishQuestionReleasesBeforeDeleteDateEnabled().booleanValue();
	}
	
	private Boolean getUnpublishQuestionReleasesBeforeDeleteDateEnabled(Operation operation)
	{
		if (unpublishQuestionReleasesBeforeDeleteDateEnabled==null)
		{
			unpublishQuestionReleasesBeforeDeleteDateEnabled=
				Boolean.valueOf(userSessionService.isGranted(getCurrentUserOperation(operation),
				"PERMISSION_PUBLICATION_UNPUBLISH_QUESTION_RELEASES_BEFORE_DELETE_DATE_ENABLED"));
		}
		return unpublishQuestionReleasesBeforeDeleteDateEnabled;
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
		return getFilterGlobalTestsEnabled();
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
		return getFilterOtherUsersTestsEnabled();
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
	
	public Boolean getViewOMTestsEnabled()
	{
		return getViewOMTestsEnabled(null);
	}
	
	public void setViewOMTestsEnabled(Boolean viewOMTestsEnabled)
	{
		this.viewOMTestsEnabled=viewOMTestsEnabled;
	}
	
	public boolean isViewOMTestsEnabled()
	{
		return getViewOMTestsEnabled().booleanValue();
	}
	
	private Boolean getViewOMTestsEnabled(Operation operation)
	{
		if (viewOMTestsEnabled==null)
		{
			viewOMTestsEnabled=Boolean.valueOf(
				userSessionService.isGranted(getCurrentUserOperation(operation),"PERMISSION_TESTS_VIEW_OM_ENABLED"));
		}
		return viewOMTestsEnabled;
	}
	
	public Boolean getPublishTestsEnabled()
	{
		return getPublishTestsEnabled(null);
	}
	
	public void setPublishTestsEnabled(Boolean publishTestsEnabled)
	{
		this.publishTestsEnabled=publishTestsEnabled;
	}
	
	public boolean isPublishTestsEnabled()
	{
		return getPublishTestsEnabled().booleanValue();
	}
	
	private Boolean getPublishTestsEnabled(Operation operation)
	{
		if (publishTestsEnabled==null)
		{
			publishTestsEnabled=Boolean.valueOf(userSessionService.isGranted(
				getCurrentUserOperation(operation),"PERMISSION_PUBLICATION_PUBLISH_TESTS_ENABLED"));
		}
		return publishTestsEnabled;
	}
	
	public Boolean getPublishOtherUsersTestsEnabled()
	{
		return getPublishOtherUsersTestsEnabled(null);
	}
	
	public void setPublishOtherUsersTestsEnabled(Boolean publishOtherUsersTestsEnabled)
	{
		this.publishOtherUsersTestsEnabled=publishOtherUsersTestsEnabled;
	}
	
	public boolean isPublishOtherUsersTestsEnabled()
	{
		return getPublishOtherUsersTestsEnabled().booleanValue();
	}
	
	private Boolean getPublishOtherUsersTestsEnabled(Operation operation)
	{
		if (publishOtherUsersTestsEnabled==null)
		{
			publishOtherUsersTestsEnabled=Boolean.valueOf(userSessionService.isGranted(
				getCurrentUserOperation(operation),"PERMISSION_PUBLICATION_PUBLISH_OTHER_USERS_TESTS_ENABLED"));
		}
		return publishOtherUsersTestsEnabled;
	}
	
	public Boolean getPublishAdminsTestsEnabled()
	{
		return getPublishAdminsTestsEnabled(null);
	}
	
	public void setPublishAdminsTestsEnabled(Boolean publishAdminsTestsEnabled)
	{
		this.publishAdminsTestsEnabled=publishAdminsTestsEnabled;
	}
	
	public boolean isPublishAdminsTestsEnabled()
	{
		return getPublishAdminsTestsEnabled().booleanValue();
	}
	
	private Boolean getPublishAdminsTestsEnabled(Operation operation)
	{
		if (publishAdminsTestsEnabled==null)
		{
			publishAdminsTestsEnabled=Boolean.valueOf(userSessionService.isGranted(
				getCurrentUserOperation(operation),"PERMISSION_PUBLICATION_PUBLISH_ADMINS_TESTS_ENABLED"));
		}
		return publishAdminsTestsEnabled;
	}
	
	public Boolean getPublishSuperadminsTestsEnabled()
	{
		return getPublishSuperadminsTestsEnabled(null);
	}
	
	public void setPublishSuperadminsTestsEnabled(Boolean publishSuperadminsTestsEnabled)
	{
		this.publishSuperadminsTestsEnabled=publishSuperadminsTestsEnabled;
	}
	
	public boolean isPublishSuperadminsTestsEnabled()
	{
		return getPublishSuperadminsTestsEnabled().booleanValue();
	}
	
	private Boolean getPublishSuperadminsTestsEnabled(Operation operation)
	{
		if (publishSuperadminsTestsEnabled==null)
		{
			publishSuperadminsTestsEnabled=Boolean.valueOf(userSessionService.isGranted(
				getCurrentUserOperation(operation),"PERMISSION_PUBLICATION_PUBLISH_SUPERADMINS_TESTS_ENABLED"));
		}
		return publishSuperadminsTestsEnabled;
	}
	
	public Boolean getUnpublishTestReleasesEnabled()
	{
		return getUnpublishTestReleasesEnabled(null);
	}
	
	public void setUnpublishTestReleasesEnabled(Boolean unpublishTestReleasesEnabled)
	{
		this.unpublishTestReleasesEnabled=unpublishTestReleasesEnabled;
	}
	
	public boolean isUnpublishTestReleasesEnabled()
	{
		return getUnpublishTestReleasesEnabled().booleanValue();
	}
	
	private Boolean getUnpublishTestReleasesEnabled(Operation operation)
	{
		if (unpublishTestReleasesEnabled==null)
		{
			unpublishTestReleasesEnabled=Boolean.valueOf(userSessionService.isGranted(
				getCurrentUserOperation(operation),"PERMISSION_PUBLICATION_UNPUBLISH_TEST_RELEASES_ENABLED"));
		}
		return unpublishTestReleasesEnabled;
	}
	
	public Boolean getUnpublishOtherUsersTestReleasesEnabled()
	{
		return getUnpublishOtherUsersTestReleasesEnabled(null);
	}
	
	public void setUnpublishOtherUsersTestReleasesEnabled(Boolean unpublishOtherUsersTestReleasesEnabled)
	{
		this.unpublishOtherUsersTestReleasesEnabled=unpublishOtherUsersTestReleasesEnabled;
	}
	
	public boolean isUnpublishOtherUsersTestReleasesEnabled()
	{
		return getUnpublishOtherUsersTestReleasesEnabled().booleanValue();
	}
	
	private Boolean getUnpublishOtherUsersTestReleasesEnabled(Operation operation)
	{
		if (unpublishOtherUsersTestReleasesEnabled==null)
		{
			unpublishOtherUsersTestReleasesEnabled=
				Boolean.valueOf(userSessionService.isGranted(getCurrentUserOperation(operation),
				"PERMISSION_PUBLICATION_UNPUBLISH_OTHER_USERS_TEST_RELEASES_ENABLED"));
		}
		return unpublishOtherUsersTestReleasesEnabled;
	}
	
	public Boolean getUnpublishAdminsTestReleasesEnabled()
	{
		return getUnpublishAdminsTestReleasesEnabled(null);
	}
	
	public void setUnpublishAdminsTestReleasesEnabled(Boolean unpublishAdminsTestReleasesEnabled)
	{
		this.unpublishAdminsTestReleasesEnabled=unpublishAdminsTestReleasesEnabled;
	}
	
	public boolean isUnpublishAdminsTestReleasesEnabled()
	{
		return getUnpublishAdminsTestReleasesEnabled().booleanValue();
	}
	
	private Boolean getUnpublishAdminsTestReleasesEnabled(Operation operation)
	{
		if (unpublishAdminsTestReleasesEnabled==null)
		{
			unpublishAdminsTestReleasesEnabled=Boolean.valueOf(userSessionService.isGranted(
				getCurrentUserOperation(operation),"PERMISSION_PUBLICATION_UNPUBLISH_ADMINS_TEST_RELEASES_ENABLED"));
		}
		return unpublishAdminsTestReleasesEnabled;
	}
	
	public Boolean getUnpublishSuperadminsTestReleasesEnabled()
	{
		return getUnpublishSuperadminsTestReleasesEnabled(null);
	}
	
	public void setUnpublishSuperadminsTestReleasesEnabled(Boolean unpublishSuperadminsTestReleasesEnabled)
	{
		this.unpublishSuperadminsTestReleasesEnabled=unpublishSuperadminsTestReleasesEnabled;
	}
	
	public boolean isUnpublishSuperadminsTestReleasesEnabled()
	{
		return getUnpublishSuperadminsTestReleasesEnabled().booleanValue();
	}
	
	private Boolean getUnpublishSuperadminsTestReleasesEnabled(Operation operation)
	{
		if (unpublishSuperadminsTestReleasesEnabled==null)
		{
			unpublishSuperadminsTestReleasesEnabled=
				Boolean.valueOf(userSessionService.isGranted(getCurrentUserOperation(operation),
				"PERMISSION_PUBLICATION_UNPUBLISH_SUPERADMINS_TEST_RELEASES_ENABLED"));
		}
		return unpublishSuperadminsTestReleasesEnabled;
	}
	
	public Boolean getUnpublishTestReleasesOpenedWithCloseDateEnabled()
	{
		return getUnpublishTestReleasesOpenedWithCloseDateEnabled(null);
	}
	
	public void setUnpublishTestReleasesOpenedWithCloseDateEnabled(
		Boolean unpublishTestReleasesOpenedWithCloseDateEnabled)
	{
		this.unpublishTestReleasesOpenedWithCloseDateEnabled=unpublishTestReleasesOpenedWithCloseDateEnabled;
	}
	
	public boolean isUnpublishTestReleasesOpenedWithCloseDateEnabled()
	{
		return getUnpublishTestReleasesOpenedWithCloseDateEnabled().booleanValue();
	}
	
	private Boolean getUnpublishTestReleasesOpenedWithCloseDateEnabled(Operation operation)
	{
		if (unpublishTestReleasesOpenedWithCloseDateEnabled==null)
		{
			unpublishTestReleasesOpenedWithCloseDateEnabled=
				Boolean.valueOf(userSessionService.isGranted(getCurrentUserOperation(operation),
				"PERMISSION_PUBLICATION_UNPUBLISH_TEST_RELEASES_OPENED_WITH_CLOSE_DATE_ENABLED"));
		}
		return unpublishTestReleasesOpenedWithCloseDateEnabled;
	}
	
	public Boolean getUnpublishTestReleasesBeforeDeleteDateEnabled()
	{
		return getUnpublishTestReleasesBeforeDeleteDateEnabled(null);
	}
	
	public void setUnpublishTestReleasesBeforeDeleteDateEnabled(
		Boolean unpublishTestReleasesBeforeDeleteDateEnabled)
	{
		this.unpublishTestReleasesBeforeDeleteDateEnabled=unpublishTestReleasesBeforeDeleteDateEnabled;
	}
	
	public boolean isUnpublishTestReleasesBeforeDeleteDateEnabled()
	{
		return getUnpublishTestReleasesBeforeDeleteDateEnabled().booleanValue();
	}
	
	private Boolean getUnpublishTestReleasesBeforeDeleteDateEnabled(Operation operation)
	{
		if (unpublishTestReleasesBeforeDeleteDateEnabled==null)
		{
			unpublishTestReleasesBeforeDeleteDateEnabled=
				Boolean.valueOf(userSessionService.isGranted(getCurrentUserOperation(operation),
				"PERMISSION_PUBLICATION_UNPUBLISH_TEST_RELEASES_BEFORE_DELETE_DATE_ENABLED"));
		}
		return unpublishTestReleasesBeforeDeleteDateEnabled;
	}
	
	public Boolean getViewTestsFromOtherUsersPrivateCategoriesEnabled()
	{
		return getViewTestsFromOtherUsersPrivateCategoriesEnabled(null);
	}
	
	public void setViewTestsFromOtherUsersPrivateCategoriesEnabled(
		Boolean viewTestsFromOtherUsersPrivateCategoriesEnabled)
	{
		this.viewTestsFromOtherUsersPrivateCategoriesEnabled=
			viewTestsFromOtherUsersPrivateCategoriesEnabled;
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
	
	private void resetAdminFromQuestionReleaseAllowed(QuestionRelease questionRelease)
	{
		if (questionRelease!=null && questionRelease.getPublisher()!=null)
		{
			admins.remove(Long.valueOf(questionRelease.getPublisher().getId()));
		}
	}
	
	private void resetAdminFromTestReleaseAllowed(TestRelease testRelease)
	{
		if (testRelease!=null && testRelease.getPublisher()!=null)
		{
			admins.remove(Long.valueOf(testRelease.getPublisher().getId()));
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
	
	private void resetSuperadminFromQuestionReleaseAllowed(QuestionRelease questionRelease)
	{
		if (questionRelease!=null && questionRelease.getPublisher()!=null)
		{
			superadmins.remove(Long.valueOf(questionRelease.getPublisher().getId()));
		}
	}
	
	private void resetSuperadminFromTestReleaseAllowed(TestRelease testRelease)
	{
		if (testRelease!=null && testRelease.getPublisher()!=null)
		{
			superadmins.remove(Long.valueOf(testRelease.getPublisher().getId()));
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
	
	private void resetPublishQuestionsAllowed()
	{
		publishQuestionsAllowed.clear();
	}
	
	private void resetPublishQuestionAllowed(Question question)
	{
		if (question!=null)
		{
			resetPublishQuestionAllowed(question.getId());
		}
	}
	
	private void resetPublishQuestionAllowed(long questionId)
	{
		publishQuestionsAllowed.remove(Long.valueOf(questionId));
	}
	
	private boolean isPublishQuestionAllowed(Operation operation,long questionId)
	{
		boolean allowed=false;
		if (questionId>0L)
		{
			if (publishQuestionsAllowed.containsKey(Long.valueOf(questionId)))
			{
				allowed=publishQuestionsAllowed.get(Long.valueOf(questionId));
			}
			else
			{
				// Get current user session Hibernate operation
				operation=getCurrentUserOperation(operation);
				
				/*
				User currentUser=userSessionService.getCurrentUser(operation);
				User questionAuthor=questionsService.getQuestion(operation,questionId).getCreatedBy();
				allowed=getPublishQuestionsEnabled(operation).booleanValue() && (currentUser.equals(questionAuthor) || 
					(getPublishOtherUsersQuestionsEnabled(operation).booleanValue() && 
					(!isAdmin(operation,questionAuthor) || 
					getPublishAdminsQuestionsEnabled(operation).booleanValue()) && 
					(!isSuperadmin(operation,questionAuthor) || 
					getPublishSuperadminsQuestionsEnabled(operation).booleanValue())));
				*/
				
				User questionAuthor=questionsService.getQuestion(operation,questionId).getCreatedBy();
				allowed=getPublishQuestionsEnabled(operation).booleanValue() && 
					(questionAuthor.getId()==userSessionService.getCurrentUserId() || 
					(getPublishOtherUsersQuestionsEnabled(operation).booleanValue() && 
					(!isAdmin(operation,questionAuthor) || 
					getPublishAdminsQuestionsEnabled(operation).booleanValue()) && 
					(!isSuperadmin(operation,questionAuthor) || 
					getPublishSuperadminsQuestionsEnabled(operation).booleanValue())));
				
				publishQuestionsAllowed.put(Long.valueOf(questionId),Boolean.valueOf(allowed));
			}
		}
		return allowed;
	}
	
	public boolean isPublishQuestionAllowed(Question question)
	{
		return isPublishQuestionAllowed(null,question==null?0L:question.getId());
	}
	
	private void resetPublishTestsAllowed()
	{
		publishTestsAllowed.clear();
	}
	
	private void resetPublishTestAllowed(Test test)
	{
		if (test!=null)
		{
			resetPublishTestAllowed(test.getId());
		}
	}
	
	private void resetPublishTestAllowed(long testId)
	{
		publishTestsAllowed.remove(Long.valueOf(testId));
	}
	
	private boolean isPublishTestAllowed(Operation operation,long testId)
	{
		boolean allowed=false;
		if (testId>0L)
		{
			if (publishTestsAllowed.containsKey(Long.valueOf(testId)))
			{
				allowed=publishTestsAllowed.get(Long.valueOf(testId));
			}
			else
			{
				// Get current user session Hibernate operation
				operation=getCurrentUserOperation(operation);
				
				/*
				User currentUser=userSessionService.getCurrentUser(operation);
				User testAuthor=testsService.getTest(operation,testId).getCreatedBy();
				allowed=getPublishTestsEnabled(operation).booleanValue() && (currentUser.equals(testAuthor) || 
					(getPublishOtherUsersTestsEnabled(operation).booleanValue() && (!isAdmin(operation,testAuthor) || 
					getPublishAdminsTestsEnabled(operation).booleanValue()) && (!isSuperadmin(operation,testAuthor) || 
					getPublishSuperadminsTestsEnabled(operation).booleanValue())));
				*/
				
				User testAuthor=testsService.getTest(operation,testId).getCreatedBy();
				allowed=getPublishTestsEnabled(operation).booleanValue() && 
					(testAuthor.getId()==userSessionService.getCurrentUserId() || 
					(getPublishOtherUsersTestsEnabled(operation).booleanValue() && (!isAdmin(operation,testAuthor) || 
					getPublishAdminsTestsEnabled(operation).booleanValue()) && (!isSuperadmin(operation,testAuthor) || 
					getPublishSuperadminsTestsEnabled(operation).booleanValue())));
				
				publishTestsAllowed.put(Long.valueOf(testId),Boolean.valueOf(allowed));
			}
		}
		return allowed;
	}
	
	public boolean isPublishTestAllowed(Test test)
	{
		return isPublishTestAllowed(null,test==null?0L:test.getId());
	}
	
	private void resetUnpublishQuestionReleasesAllowed()
	{
		unpublishQuestionReleasesAllowed.clear();
	}
	
	private void resetUnpublishQuestionReleaseAllowed(QuestionRelease questionRelease)
	{
		if (questionRelease!=null && questionRelease.getQuestion()!=null)
		{
			unpublishQuestionReleasesAllowed.remove(questionRelease);			
		}
	}
	
	private boolean isUnpublishQuestionReleaseAllowed(Operation operation,QuestionRelease questionRelease,
		boolean displayErrors)
	{
		boolean allowed=false;
		boolean displayedError=false;
		if (questionRelease!=null && questionRelease.getQuestion()!=null)
		{
			if (unpublishQuestionReleasesAllowed.containsKey(questionRelease))
			{
				allowed=unpublishQuestionReleasesAllowed.get(questionRelease);
			}
			else
			{
				if (questionRelease.getReleaseDate()!=null)
				{
					// Get current user session Hibernate operation
					operation=getCurrentUserOperation(operation);
					
					/*
					User currentUser=userSessionService.getCurrentUser(operation);
					User questionPublisher=questionRelease.getPublisher();
					allowed=getUnpublishQuestionReleasesEnabled(operation).booleanValue() && 
						(currentUser.equals(questionPublisher) || 
						(getUnpublishOtherUsersQuestionReleasesEnabled(operation).booleanValue() && 
						(!isAdmin(operation,questionPublisher) || 
						getUnpublishAdminsQuestionReleasesEnabled(operation).booleanValue()) && 
						(!isSuperadmin(operation,questionPublisher) || 
						getUnpublishSuperadminsQuestionReleasesEnabled(operation).booleanValue())));
					*/
					
					User questionPublisher=questionRelease.getPublisher();
					allowed=getUnpublishQuestionReleasesEnabled(operation).booleanValue() && 
						(questionPublisher.getId()==userSessionService.getCurrentUserId() || 
						(getUnpublishOtherUsersQuestionReleasesEnabled(operation).booleanValue() && 
						(!isAdmin(operation,questionPublisher) || 
						getUnpublishAdminsQuestionReleasesEnabled(operation).booleanValue()) && 
						(!isSuperadmin(operation,questionPublisher) || 
						getUnpublishSuperadminsQuestionReleasesEnabled(operation).booleanValue())));
					
					if (allowed)
					{
						Date currentDate=new Date();
						boolean deleteDateOk=true;
						boolean closeDateOk=true;
						if (questionRelease.getDeleteDate()!=null)
						{
							deleteDateOk=currentDate.after(questionRelease.getDeleteDate()) || 
								getUnpublishQuestionReleasesBeforeDeleteDateEnabled(operation).booleanValue();
						}
						if (questionRelease.getStartDate()!=null && questionRelease.getCloseDate()!=null)
						{
							closeDateOk=currentDate.before(questionRelease.getStartDate()) || 
								currentDate.after(questionRelease.getCloseDate()) || 
									getUnpublishQuestionReleasesOpenedWithCloseDateEnabled(operation).booleanValue();
						}
						allowed=deleteDateOk && closeDateOk;
						if (displayErrors && !allowed)
						{
							String errorKey=null;
							Date errorDate=null;
							if (!deleteDateOk && !closeDateOk)
							{
								if (questionRelease.getCloseDate().after(questionRelease.getDeleteDate()))
								{
									errorKey="UNPUBLISH_QUESTION_RELEASE_CLOSE_DATE_ERROR";
									errorDate=questionRelease.getCloseDate();
								}
								else
								{
									errorKey="UNPUBLISH_QUESTION_RELEASE_DELETE_DATE_ERROR";
									errorDate=questionRelease.getDeleteDate();
								}
							}
							else if (!deleteDateOk)
							{
								errorKey="UNPUBLISH_QUESTION_RELEASE_DELETE_DATE_ERROR";
								errorDate=questionRelease.getDeleteDate();
							}
							else
							{
								errorKey="UNPUBLISH_QUESTION_RELEASE_CLOSE_DATE_ERROR";
								errorDate=questionRelease.getCloseDate();
							}
							DateFormat dateFormat=
								new SimpleDateFormat(localizationService.getLocalizedMessage("DATE_PATTERN"));
							addPlainErrorMessage(false,"INCORRECT_OPERATION",localizationService.getLocalizedMessage(
								errorKey).replace("?",dateFormat.format(errorDate)));
							displayedError=true;
						}
					}
				}
				unpublishQuestionReleasesAllowed.put(questionRelease,Boolean.valueOf(allowed));
			}
		}
		if (displayErrors && !allowed && !displayedError)
		{
			addErrorMessage(false,"INCORRECT_OPERATION","NON_AUTHORIZED_ACTION_ERROR");
		}
		return allowed;
	}
	
	public boolean isUnpublishQuestionReleaseAllowed(QuestionRelease questionRelease)
	{
		return isUnpublishQuestionReleaseAllowed(null,questionRelease,false);
	}
	
	private void resetUnpublishTestReleasesAllowed()
	{
		unpublishTestReleasesAllowed.clear();
	}
	
	private void resetUnpublishTestReleaseAllowed(TestRelease testRelease)
	{
		if (testRelease!=null && testRelease.getTest()!=null)
		{
			unpublishTestReleasesAllowed.remove(testRelease);			
		}
	}
	
	private boolean isUnpublishTestReleaseAllowed(Operation operation,TestRelease testRelease,
		boolean displayErrors)
	{
		boolean allowed=false;
		boolean displayedError=false;
		if (testRelease!=null && testRelease.getTest()!=null)
		{
			if (unpublishTestReleasesAllowed.containsKey(testRelease))
			{
				allowed=unpublishTestReleasesAllowed.get(testRelease);
			}
			else
			{
				if (testRelease.getReleaseDate()!=null)
				{
					// Get current user session Hibernate operation
					operation=getCurrentUserOperation(operation);
					
					/*
					User currentUser=userSessionService.getCurrentUser(operation);
					User testPublisher=testRelease.getPublisher();
					allowed=getUnpublishTestReleasesEnabled(operation).booleanValue() && 
						(currentUser.equals(testPublisher) || 
						(getUnpublishOtherUsersTestReleasesEnabled(operation).booleanValue() && 
						(!isAdmin(operation,testPublisher) || 
						getUnpublishAdminsTestReleasesEnabled(operation).booleanValue()) && 
						(!isSuperadmin(operation,testPublisher) || 
						getUnpublishSuperadminsTestReleasesEnabled(operation).booleanValue())));
					*/
					
					User testPublisher=testRelease.getPublisher();
					allowed=getUnpublishTestReleasesEnabled(operation).booleanValue() && 
						(testPublisher.getId()==userSessionService.getCurrentUserId() || 
						(getUnpublishOtherUsersTestReleasesEnabled(operation).booleanValue() && 
						(!isAdmin(operation,testPublisher) || 
						getUnpublishAdminsTestReleasesEnabled(operation).booleanValue()) && 
						(!isSuperadmin(operation,testPublisher) || 
						getUnpublishSuperadminsTestReleasesEnabled(operation).booleanValue())));
					
					if (allowed)
					{
						Date currentDate=new Date();
						boolean deleteDateOk=true;
						boolean closeDateOk=true;
						if (testRelease.getDeleteDate()!=null)
						{
							deleteDateOk=currentDate.after(testRelease.getDeleteDate()) || 
								getUnpublishTestReleasesBeforeDeleteDateEnabled(operation).booleanValue();
						}
						if (testRelease.getStartDate()!=null && testRelease.getCloseDate()!=null)
						{
							closeDateOk=currentDate.before(testRelease.getStartDate()) || 
								currentDate.after(testRelease.getCloseDate()) ||
								getUnpublishTestReleasesOpenedWithCloseDateEnabled(operation).booleanValue();
						}
						allowed=deleteDateOk && closeDateOk;
						if (displayErrors && !allowed)
						{
							String errorKey=null;
							Date errorDate=null;
							if (!deleteDateOk && !closeDateOk)
							{
								if (testRelease.getCloseDate().after(testRelease.getDeleteDate()))
								{
									errorKey="UNPUBLISH_TEST_RELEASE_CLOSE_DATE_ERROR";
									errorDate=testRelease.getCloseDate();
								}
								else
								{
									errorKey="UNPUBLISH_TEST_RELEASE_DELETE_DATE_ERROR";
									errorDate=testRelease.getDeleteDate();
								}
							}
							else if (!deleteDateOk)
							{
								errorKey="UNPUBLISH_TEST_RELEASE_DELETE_DATE_ERROR";
								errorDate=testRelease.getDeleteDate();
							}
							else
							{
								errorKey="UNPUBLISH_TEST_RELEASE_CLOSE_DATE_ERROR";
								errorDate=testRelease.getCloseDate();
							}
							DateFormat dateFormat=
								new SimpleDateFormat(localizationService.getLocalizedMessage("DATE_PATTERN"));
							addPlainErrorMessage(false,"INCORRECT_OPERATION",localizationService.getLocalizedMessage(
								errorKey).replace("?",dateFormat.format(errorDate)));
							displayedError=true;
						}
					}
				}
				unpublishTestReleasesAllowed.put(testRelease,Boolean.valueOf(allowed));
			}
		}
		if (displayErrors && !allowed && !displayedError)
		{
			addErrorMessage(false,"INCORRECT_OPERATION","NON_AUTHORIZED_ACTION_ERROR");
		}
		return allowed;
	}
	
	public boolean isUnpublishTestReleaseAllowed(TestRelease testRelease)
	{
		return isUnpublishTestReleaseAllowed(null,testRelease,false);
	}
	
	public QuestionRelease getCurrentQuestionRelease()
	{
		return currentQuestionRelease;
	}
	
	public void setCurrentQuestionRelease(QuestionRelease currentQuestionRelease)
	{
		this.currentQuestionRelease=currentQuestionRelease;
	}
	
	public List<QuestionRelease> getQuestionsReleases()
	{
		return getQuestionsReleases(null);
	}
	
	private List<QuestionRelease> getQuestionsReleases(Operation operation)
	{
		if (questionsReleases==null)
		{
			// Get current user session Hibernate operation
			operation=getCurrentUserOperation(operation);
			
			questionsReleases=new ArrayList<QuestionRelease>();
			List<QuestionRelease> publishedQuestionsReleases=questionReleasesService.getQuestionsReleases(operation);
			for (Question question:getQuestions(operation))
			{
				QuestionRelease questionRelease=null;
				for (QuestionRelease qr:publishedQuestionsReleases)
				{
					if (question.equals(qr.getQuestion()))
					{
						questionRelease=qr;
						break;
					}
				}
				if (questionRelease==null)
				{
					if ("PUBLICATION_STATUS_NOT_RELEASED".equals(getFilterQuestionPublicationStatus()) ||
						"PUBLICATION_STATUS_ALL".equals(getFilterQuestionPublicationStatus()))
					{
						questionRelease=new QuestionRelease(question,null);
					}
				}
				else if (!"PUBLICATION_STATUS_RELEASED".equals(getFilterQuestionPublicationStatus()) &&
					!"PUBLICATION_STATUS_ALL".equals(getFilterQuestionPublicationStatus()))
				{
					questionRelease=null;
				}
				if (questionRelease!=null)
				{
					questionsReleases.add(questionRelease);
				}
			}
			Collections.sort(questionsReleases,getQuestionsReleasesComparator());
		}
		return questionsReleases;
	}
	
	public void setQuestionsReleases(List<QuestionRelease> questionsReleases)
	{
		this.questionsReleases=questionsReleases;
	}
	
	private Comparator<QuestionRelease> getQuestionsReleasesComparator()
	{
		return new Comparator<QuestionRelease>()
		{
			@Override
			public int compare(QuestionRelease qr1,QuestionRelease qr2)
			{
				return getQuestionReleaseDisplayDate(qr2).compareTo(getQuestionReleaseDisplayDate(qr1));
			}
		};
	}
	
	private List<Question> getQuestions(Operation operation)
	{
		if (questions==null)
		{
			try
			{
				// Get current user session Hibernate operation
				operation=getCurrentUserOperation(operation);
				
				if (checkQuestionsFilterPermission(operation,null))
				{
					long filterQuestionCategoryId=getFilterQuestionCategoryId(operation);
					if (getSpecialQuestionCategoryFiltersMap().containsKey(Long.valueOf(filterQuestionCategoryId)))
					{
						SpecialCategoryFilter filter=
							getSpecialQuestionCategoryFiltersMap().get(Long.valueOf(filterQuestionCategoryId));
						if (getAllQuestionCategoriesSpecialCategoryFilter().equals(filter))
						{
							questions=questionsService.getAllVisibleCategoriesQuestions(
								operation,null,getFilterQuestionType(),getFilterQuestionLevel());
						}
						else if (ALL_EVEN_PRIVATE_QUESTION_CATEGORIES_OF_OTHER_USERS.equals(filter))
						{
							questions=questionsService.getAllCategoriesQuestions(
								operation,null,getFilterQuestionType(),getFilterQuestionType());
						}
						else if (ALL_MY_QUESTION_CATEGORIES.equals(filter))
						{
							questions=questionsService.getAllMyCategoriesQuestions(
								operation,null,getFilterQuestionType(),getFilterQuestionLevel());
						}
						else if (ALL_MY_QUESTION_CATEGORIES_EXCEPT_GLOBALS.equals(filter))
						{
							questions=questionsService.getAllMyCategoriesExceptGlobalsQuestions(
								operation,null,getFilterQuestionType(),getFilterQuestionLevel());
						}
						else if (ALL_GLOBAL_QUESTION_CATEGORIES.equals(filter))
						{
							questions=questionsService.getAllGlobalCategoriesQuestions(
								operation,null,getFilterQuestionType(),getFilterQuestionLevel());
						}
						else if (ALL_PUBLIC_QUESTION_CATEGORIES_OF_OTHER_USERS.equals(filter))
						{
							questions=questionsService.getAllPublicCategoriesOfOtherUsersQuestions(
								operation,null,getFilterQuestionType(),getFilterQuestionLevel());
						}
						else if (ALL_PRIVATE_QUESTION_CATEGORIES_OF_OTHER_USERS.equals(filter))
						{
							questions=questionsService.getAllPrivateCategoriesOfOtherUsersQuestions(
								operation,null,getFilterQuestionType(),getFilterQuestionLevel());
						}
						else if (ALL_QUESTION_CATEGORIES_OF_OTHER_USERS.equals(filter))
						{
							questions=questionsService.getAllCategoriesOfOtherUsersQuestions(
								operation,null,getFilterQuestionType(),getFilterQuestionLevel());
						}
					}
					else
					{
						questions=questionsService.getQuestions(operation,null,filterQuestionCategoryId,
							isFilterIncludeQuestionSubcategories(),getFilterQuestionType(),
							getFilterQuestionLevel());
					}
				}
			}
			catch (ServiceException se)
			{
				questions=null;
				addPlainErrorMessage(
					true,localizationService.getLocalizedMessage("INCORRECT_OPERATION"),se.getMessage());
			}
			finally
			{
				// It is not a good idea to return null even if an error is produced because JSF getters are 
				// usually called several times
				if (questions==null)
				{
					questions=new ArrayList<Question>();
				}
			}
		}
		return questions;
	}
	
	public SpecialCategoryFilter getAllQuestionCategoriesSpecialCategoryFilter()
	{
		if (allQuestionCategories==null)
		{
			List<String> allPermissions=new ArrayList<String>();
			allPermissions.add("PERMISSION_QUESTIONS_GLOBAL_FILTER_ENABLED");
			allPermissions.add("PERMISSION_QUESTIONS_OTHER_USERS_FILTER_ENABLED");
			String categoryGen=localizationService.getLocalizedMessage("CATEGORY_GEN");
			if ("M".equals(categoryGen))
			{
				allQuestionCategories=new SpecialCategoryFilter(0L,"ALL_OPTIONS",allPermissions);
			}
			else
			{
				allQuestionCategories=new SpecialCategoryFilter(0L,"ALL_OPTIONS_F",allPermissions);
			}
		}
		return allQuestionCategories;
	}
	
	public Map<Long,SpecialCategoryFilter> getSpecialQuestionCategoryFiltersMap()
	{
		if (specialQuestionCategoryFiltersMap==null)
		{
			specialQuestionCategoryFiltersMap=new LinkedHashMap<Long,SpecialCategoryFilter>();
			SpecialCategoryFilter allQuestionCategories=getAllQuestionCategoriesSpecialCategoryFilter();
			specialQuestionCategoryFiltersMap.put(Long.valueOf(allQuestionCategories.id),allQuestionCategories);
			specialQuestionCategoryFiltersMap.put(Long.valueOf(ALL_EVEN_PRIVATE_QUESTION_CATEGORIES_OF_OTHER_USERS.id),
				ALL_EVEN_PRIVATE_QUESTION_CATEGORIES_OF_OTHER_USERS);
			specialQuestionCategoryFiltersMap.put(
				Long.valueOf(ALL_MY_QUESTION_CATEGORIES.id),ALL_MY_QUESTION_CATEGORIES);
			specialQuestionCategoryFiltersMap.put(
				Long.valueOf(ALL_MY_QUESTION_CATEGORIES_EXCEPT_GLOBALS.id),ALL_MY_QUESTION_CATEGORIES_EXCEPT_GLOBALS);
			specialQuestionCategoryFiltersMap.put(
				Long.valueOf(ALL_GLOBAL_QUESTION_CATEGORIES.id),ALL_GLOBAL_QUESTION_CATEGORIES);
			specialQuestionCategoryFiltersMap.put(Long.valueOf(ALL_PUBLIC_QUESTION_CATEGORIES_OF_OTHER_USERS.id),
				ALL_PUBLIC_QUESTION_CATEGORIES_OF_OTHER_USERS);
			specialQuestionCategoryFiltersMap.put(Long.valueOf(ALL_PRIVATE_QUESTION_CATEGORIES_OF_OTHER_USERS.id),
				ALL_PRIVATE_QUESTION_CATEGORIES_OF_OTHER_USERS);
			specialQuestionCategoryFiltersMap.put(
				Long.valueOf(ALL_QUESTION_CATEGORIES_OF_OTHER_USERS.id),ALL_QUESTION_CATEGORIES_OF_OTHER_USERS);
		}
		return specialQuestionCategoryFiltersMap;
	}
	
	/**
	 * @return Special question categories used to filter other question categories
	 */
	public List<Category> getSpecialQuestionCategoriesFilters()
	{
		return getSpecialQuestionCategoriesFilters(null);
	}
	
	/**
	 * @param operation Operation
	 * @return Special question categories used to filter other question categories
	 */
	private List<Category> getSpecialQuestionCategoriesFilters(Operation operation)
	{
		if (specialQuestionCategoriesFilters==null)
		{
			specialQuestionCategoriesFilters=new ArrayList<Category>();
			
			// Get current user session Hibernate operation
			operation=getCurrentUserOperation(operation);
			
			Map<String,Boolean> cachedPermissions=new HashMap<String,Boolean>();
			for (SpecialCategoryFilter specialQuestionCategoryFilter:getSpecialQuestionCategoryFiltersMap().values())
			{
				boolean granted=true;
				for (String requiredPermission:specialQuestionCategoryFilter.requiredPermissions)
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
					Category specialQuestionCategory=new Category();
					specialQuestionCategory.setId(specialQuestionCategoryFilter.id);
					specialQuestionCategory.setName(specialQuestionCategoryFilter.name);
					specialQuestionCategoriesFilters.add(specialQuestionCategory);
				}
			}
		}
		return specialQuestionCategoriesFilters;
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
    		
    		questionsCategories=new ArrayList<Category>();
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
        	
        	// In case that current user is allowed to view private categories of other users 
        	// we also need to check if he/she has permission to view private categories of 
        	// administrators and/or users with permission to improve permissions over their owned ones 
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
        	
        	// Get visible categories for questions taking account user permissions
        	questionsCategories=categoriesService.getCategoriesSortedByHierarchy(operation,
        		userSessionService.getCurrentUser(operation),
        		categoryTypesService.getCategoryType(operation,"CATEGORY_TYPE_QUESTIONS"),true,
        		getFilterGlobalQuestionsEnabled(operation).booleanValue(),includeOtherUsersCategories,
        		includeAdminsPrivateCategories,includeSuperadminsPrivateCategories);        		
    	}
    	return questionsCategories;
	}
    
	public List<TestRelease> getTestsReleases()
	{
		return getTestsReleases(null);
	}
	
	private List<TestRelease> getTestsReleases(Operation operation)
	{
		if (testsReleases==null)
		{
			// Get current user session Hibernate operation
			operation=getCurrentUserOperation(operation);
				
			testsReleases=new ArrayList<TestRelease>();
			List<TestRelease> publishedTestsReleases=
				testReleasesService.getTestsReleases(operation,isFilterTestDisplayOldVersions());
			for (Test test:getTests(operation))
			{
				List<TestRelease> testsReleasesForTest=new ArrayList<TestRelease>();
				for (TestRelease tr:publishedTestsReleases)
				{
					if (test.equals(tr.getTest()))
					{
						testsReleasesForTest.add(tr);
					}
				}
				if (testsReleasesForTest.isEmpty())
				{
					if ("PUBLICATION_STATUS_NOT_RELEASED".equals(getFilterTestPublicationStatus()) ||
						"PUBLICATION_STATUS_ALL".equals(getFilterTestPublicationStatus()))
					{
						testsReleasesForTest.add(new TestRelease(test,0,null));
					}
				}
				else
				{
					if (!"PUBLICATION_STATUS_RELEASED".equals(getFilterTestPublicationStatus()) &&
						!"PUBLICATION_STATUS_ALL".equals(getFilterTestPublicationStatus()))
					{
						testsReleasesForTest.clear();
					}
					else if (isFilterTestDisplayOldVersions())
					{
						testsReleasesForTest.add(0,new TestRelease(test,0,null));
					}
				}
				if (!testsReleasesForTest.isEmpty())
				{
					for (TestRelease testRelease:testsReleasesForTest)
					{
						testsReleases.add(testRelease);
					}
				}
			}
			Collections.sort(testsReleases,getTestsReleasesComparator());
		}
		return testsReleases;
	}
	
	public void setTestsReleases(List<TestRelease> testsReleases)
	{
		this.testsReleases=testsReleases;
	}
    
	private Comparator<TestRelease> getTestsReleasesComparator()
	{
		return new Comparator<TestRelease>()
		{
			@Override
			public int compare(TestRelease tr1,TestRelease tr2)
			{
				return getTestReleaseDisplayDate(tr2).compareTo(getTestReleaseDisplayDate(tr1));
			}
		};
	}
	
	private List<Test> getTests(Operation operation)
	{
		if (tests==null)
		{
			try
			{
				// Get current user Hibernate operation
				operation=getCurrentUserOperation(operation);
				
				if (checkTestsFilterPermission(operation,null))
				{
					long filterTestCategoryId=getFilterTestCategoryId(operation);
					if (getSpecialTestCategoryFiltersMap().containsKey(Long.valueOf(filterTestCategoryId)))
					{
						SpecialCategoryFilter filter=
							getSpecialTestCategoryFiltersMap().get(Long.valueOf(filterTestCategoryId));
						if (getAllTestCategoriesSpecialCategoryFilter().equals(filter))
						{
							tests=testsService.getAllVisibleCategoriesTests(operation,null);
						}
						else if (ALL_EVEN_PRIVATE_TEST_CATEGORIES_OF_OTHER_USERS.equals(filter))
						{
							tests=testsService.getAllCategoriesTests(operation,null);
						}
						else if (ALL_MY_TEST_CATEGORIES.equals(filter))
						{
							tests=testsService.getAllMyCategoriesTests(operation,null);
						}
						else if (ALL_MY_TEST_CATEGORIES_EXCEPT_GLOBALS.equals(filter))
						{
							tests=testsService.getAllMyCategoriesExceptGlobalsTests(operation,null);
						}
						else if (ALL_GLOBAL_TEST_CATEGORIES.equals(filter))
						{
							tests=testsService.getAllGlobalCategoriesTests(operation,null);
						}
						else if (ALL_PUBLIC_TEST_CATEGORIES_OF_OTHER_USERS.equals(filter))
						{
							tests=testsService.getAllPublicCategoriesOfOtherUsersTests(operation,null);
						}
						else if (ALL_PRIVATE_TEST_CATEGORIES_OF_OTHER_USERS.equals(filter))
						{
							tests=testsService.getAllPrivateCategoriesOfOtherUsersTests(operation,null);
						}
						else if (ALL_TEST_CATEGORIES_OF_OTHER_USERS.equals(filter))
						{
							tests=testsService.getAllCategoriesOfOtherUsersTests(operation,null);
						}
					}
					else
					{
						tests=testsService.getTests(
							operation,null,filterTestCategoryId,isFilterIncludeTestSubcategories());
					}
					if (tests!=null && !tests.isEmpty())
					{
						for (Test test:tests)
						{
							test.setTestUsers(testUsersService.getSortedTestUsers(operation,test.getId()));
							test.setSupportContacts(
								supportContactsService.getSupportContacts(operation,test.getId()));
							test.setEvaluators(evaluatorsService.getEvaluators(operation,test.getId()));
						}
					}
				}
			}
			catch (ServiceException se)
			{
				tests=null;
				addPlainErrorMessage(
					true,localizationService.getLocalizedMessage("INCORRECT_OPERATION"),se.getMessage());
			}
			finally
			{
				// It is not a good idea to return null even if an error is produced because JSF getters are 
				// usually called several times
				if (tests==null)
				{
					tests=new ArrayList<Test>();
				}
			}
		}
		return tests;
	}
    
	public SpecialCategoryFilter getAllTestCategoriesSpecialCategoryFilter()
	{
		if (allTestCategories==null)
		{
			List<String> allPermissions=new ArrayList<String>();
			allPermissions.add("PERMISSION_TESTS_GLOBAL_FILTER_ENABLED");
			allPermissions.add("PERMISSION_TESTS_OTHER_USERS_FILTER_ENABLED");
			String categoryGen=localizationService.getLocalizedMessage("CATEGORY_GEN");
			if ("M".equals(categoryGen))
			{
				allTestCategories=new SpecialCategoryFilter(0L,"ALL_OPTIONS",allPermissions);
			}
			else
			{
				allTestCategories=new SpecialCategoryFilter(0L,"ALL_OPTIONS_F",allPermissions);
			}
		}
		return allTestCategories;
	}
	
	public Map<Long,SpecialCategoryFilter> getSpecialTestCategoryFiltersMap()
	{
		if (specialTestCategoryFiltersMap==null)
		{
			specialTestCategoryFiltersMap=new LinkedHashMap<Long,SpecialCategoryFilter>();
			SpecialCategoryFilter allTestCategories=getAllTestCategoriesSpecialCategoryFilter();
			specialTestCategoryFiltersMap.put(Long.valueOf(allTestCategories.id),allTestCategories);
			specialTestCategoryFiltersMap.put(Long.valueOf(ALL_EVEN_PRIVATE_TEST_CATEGORIES_OF_OTHER_USERS.id),
				ALL_EVEN_PRIVATE_TEST_CATEGORIES_OF_OTHER_USERS);
			specialTestCategoryFiltersMap.put(Long.valueOf(ALL_MY_TEST_CATEGORIES.id),ALL_MY_TEST_CATEGORIES);
			specialTestCategoryFiltersMap.put(
				Long.valueOf(ALL_MY_TEST_CATEGORIES_EXCEPT_GLOBALS.id),ALL_MY_TEST_CATEGORIES_EXCEPT_GLOBALS);
			specialTestCategoryFiltersMap.put(Long.valueOf(ALL_GLOBAL_TEST_CATEGORIES.id),ALL_GLOBAL_TEST_CATEGORIES);
			specialTestCategoryFiltersMap.put(
				Long.valueOf(ALL_PUBLIC_TEST_CATEGORIES_OF_OTHER_USERS.id),ALL_PUBLIC_TEST_CATEGORIES_OF_OTHER_USERS);
			specialTestCategoryFiltersMap.put(
				Long.valueOf(ALL_PRIVATE_TEST_CATEGORIES_OF_OTHER_USERS.id),ALL_PRIVATE_TEST_CATEGORIES_OF_OTHER_USERS);
			specialTestCategoryFiltersMap.put(
				Long.valueOf(ALL_TEST_CATEGORIES_OF_OTHER_USERS.id),ALL_TEST_CATEGORIES_OF_OTHER_USERS);
		}
		return specialTestCategoryFiltersMap;
	}
	
	/**
	 * @return Special test categories used to filter other test categories
	 */
	public List<Category> getSpecialTestCategoriesFilters()
	{
		return getSpecialTestCategoriesFilters(null);
	}
	
	/**
	 * @param operation Operation
	 * @return Special test categories used to filter other test categories
	 */
	private List<Category> getSpecialTestCategoriesFilters(Operation operation)
	{
		if (specialTestCategoriesFilters==null)
		{
			specialTestCategoriesFilters=new ArrayList<Category>();
			
			// Get current user session Hibernate operation
			operation=getCurrentUserOperation(operation);
			
			Map<String,Boolean> cachedPermissions=new HashMap<String,Boolean>();
			for (SpecialCategoryFilter specialTestCategoryFilter:getSpecialTestCategoryFiltersMap().values())
			{
				boolean granted=true;
				for (String requiredPermission:specialTestCategoryFilter.requiredPermissions)
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
					Category specialTestCategory=new Category();
					specialTestCategory.setId(specialTestCategoryFilter.id);
					specialTestCategory.setName(specialTestCategoryFilter.name);
					specialTestCategoriesFilters.add(specialTestCategory);
				}
			}
		}
		return specialTestCategoriesFilters;
	}
	
    /**
	 * @return List of visible categories for tests
	 */
    public List<Category> getTestsCategories()
    {
    	return getTestsCategories(null);
	}
    
    /**
	 * @param operation Operation
	 * @return List of visible categories for tests
	 */
    private List<Category> getTestsCategories(Operation operation)
    {
    	if (testsCategories==null)
    	{
    		// Get current user session Hibernate operation
    		operation=getCurrentUserOperation(operation);
    		
    		testsCategories=new ArrayList<Category>();
    		
        	// Get filter value for viewing tests from categories of other users based on permissions
        	// of current user
        	int includeOtherUsersCategories=CategoriesService.NOT_VIEW_OTHER_USERS_CATEGORIES;
        	if (getFilterOtherUsersTestsEnabled(operation).booleanValue())
        	{
        		if (getViewTestsFromOtherUsersPrivateCategoriesEnabled(operation).booleanValue())
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
        			getViewTestsFromAdminsPrivateCategoriesEnabled(operation).booleanValue();
        		includeSuperadminsPrivateCategories=
        			getViewTestsFromSuperadminsPrivateCategoriesEnabled(operation).booleanValue();
       		}
        	
        	// Get visible categories for tests taking account user permissions
        	testsCategories=categoriesService.getCategoriesSortedByHierarchy(operation,
        		userSessionService.getCurrentUser(operation),
        		categoryTypesService.getCategoryType(operation,"CATEGORY_TYPE_TESTS"),true,
        		getFilterGlobalTestsEnabled(operation).booleanValue(),includeOtherUsersCategories,
        		includeAdminsPrivateCategories,includeSuperadminsPrivateCategories);        		
    	}
    	return testsCategories;
	}
    
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
     * @return Publication statuses
     */
    public List<String> getAllPublicationStatus()
    {
    	return ALL_PUBLICATION_STATUS;
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
     * @param questionRelease Question release
     * @return Display date of a question release (release date or last time modified if the question 
     * has not been released yet)
     */
    public Date getQuestionReleaseDisplayDate(QuestionRelease questionRelease)
    {
    	Date displayDate=questionRelease.getReleaseDate();
    	if (displayDate==null)
    	{
    		displayDate=questionRelease.getQuestion().getTimemodified();
    	}
    	return displayDate; 
    }
    
    /**
     * @param testRelease Test release
     * @return Display date of a test release (release date or last time modified if the test 
     * has not been released yet)
     */
    public Date getTestReleaseDisplayDate(TestRelease testRelease)
    {
    	Date displayDate=testRelease.getReleaseDate();
    	if (displayDate==null)
    	{
    		displayDate=testRelease.getTest().getTimeModified();
    	}
    	return displayDate; 
    }
    
    /**
     * @param testRelease Test release
     * @return Display version of a test release (version as a two digits number or empty string if the test
     * has not been released yet)
     */
    public String getTestReleaseDisplayVersion(TestRelease testRelease)
    {
    	StringBuffer displayVersion=new StringBuffer();
    	if (testRelease.getVersion()>0)
    	{
    		if (testRelease.getVersion()<10)
    		{
    			displayVersion.append('0');
    		}
    		displayVersion.append(testRelease.getVersion());
    	}
    	return displayVersion.toString();
    }
    
    /**
     * @param category Category
     * @return Localized category name
     */
    public String getLocalizedCategoryName(Category category)
    {
    	return getLocalizedCategoryName(null,category);
    }
    
    /**
     * @param operation Operation
     * @param category Category
     * @return Localized category name
     */
    private String getLocalizedCategoryName(Operation operation,Category category)
    {
    	return categoriesService.getLocalizedCategoryName(getCurrentUserOperation(operation),category.getId());
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
     * @param questionCategoryId Question category identifier
	 * @param maxLength Maximum length
     * @return Localized question category filter name (abbreviated long name if it is a category)
     */
    public String getLocalizedQuestionCategoryFilterName(Long questionCategoryId,int maxLength)
    {
    	return getLocalizedQuestionCategoryFilterName(null,questionCategoryId,maxLength);
    }
    
    /**
     * @param operation Operation
     * @param questionCategoryId Question category identifies
	 * @param maxLength Maximum length
     * @return Localized question category filter name (abbreviated long name if it is a category)
     */
    public String getLocalizedQuestionCategoryFilterName(Operation operation,Long questionCategoryId,int maxLength)
    {
    	String localizedQuestionCategoryFilterName="";
    	if (getSpecialQuestionCategoryFiltersMap().containsKey(questionCategoryId))
    	{
    		localizedQuestionCategoryFilterName=localizationService.getLocalizedMessage(
    			getSpecialQuestionCategoryFiltersMap().get(questionCategoryId).name);
    	}
    	else if (questionCategoryId>0L)
    	{
    		localizedQuestionCategoryFilterName=getLocalizedCategoryLongName(operation,questionCategoryId,maxLength);
    	}
    	return localizedQuestionCategoryFilterName;
    }
    
    /**
     * @param testCategoryId Test category identifier
	 * @param maxLength Maximum length
     * @return Localized test category filter name (abbreviated long name if it is a category)
     */
    public String getLocalizedTestCategoryFilterName(Long testCategoryId,int maxLength)
    {
    	return getLocalizedTestCategoryFilterName(null,testCategoryId,maxLength);
    }
    
    /**
     * @param operation Operation
     * @param testCategoryId Test category identifier
	 * @param maxLength Maximum length
     * @return Localized test category filter name (abbreviated long name if it is a category)
     */
    public String getLocalizedTestCategoryFilterName(Operation operation,Long testCategoryId,int maxLength)
    {
    	String localizedTestCategoryFilterName="";
    	if (getSpecialTestCategoryFiltersMap().containsKey(testCategoryId))
    	{
    		localizedTestCategoryFilterName=
    			localizationService.getLocalizedMessage(getSpecialTestCategoryFiltersMap().get(testCategoryId).name);
    	}
    	else if (testCategoryId>0L)
    	{
    		localizedTestCategoryFilterName=getLocalizedCategoryLongName(operation,testCategoryId,maxLength);
    	}
    	return localizedTestCategoryFilterName;
    }
    
	/**
	 * Tab change listener for displaying other tab of the 'Publication' page.
	 * @param event Tab change event
	 */
    public void changeActivePublicationTab(TabChangeEvent event)
    {
    	TabView publicationFormTabs=(TabView)event.getComponent();
    	switch (publicationFormTabs.getActiveIndex())
    	{
    		case QUESTIONS_TABVIEW_TAB:
    			setBuildType(BUILD_QUESTION);
    			break;
    		case TESTS_TABVIEW_TAB:
    			setBuildType(BUILD_TEST);
    	}
    }
    
	/**
	 * @param operation Operation
	 * @param filterCategory Filter category can be optionally passed as argument
	 * @return true if user has permissions to display questions with the current selected filter, false otherwise
	 */
    private boolean checkQuestionsFilterPermission(Operation operation,Category filterCategory)
	{
    	boolean ok=true;
    	
		// Get current user session Hibernate operation
		operation=getCurrentUserOperation(operation);
    	
		long filterQuestionCategoryId=getFilterQuestionCategoryId(operation);
    	if (getSpecialQuestionCategoryFiltersMap().containsKey(Long.valueOf(filterQuestionCategoryId)))
		{
			SpecialCategoryFilter filter=
				getSpecialQuestionCategoryFiltersMap().get(Long.valueOf(filterQuestionCategoryId));
			for (String requiredPermission:filter.requiredPermissions)
			{
				if (userSessionService.isDenied(operation,requiredPermission))
				{
					ok=false;
					break;
				}
			}
		}
		else
		{
			// Check permissions needed for selected category
			if (filterCategory==null)
			{
				// If we have not received filter category as argument we need to get it from DB
				filterCategory=categoriesService.getCategory(operation,filterQuestionCategoryId);	
			}
			if (filterCategory.getVisibility().isGlobal())
			{
				// This is a global category, so we check that current user has permissions to filter
				// questions by global categories
				if (getFilterGlobalQuestionsEnabled(operation).booleanValue())
				{
					// Moreover we need to check that the category is owned by current user or 
					// that current user has permission to filter by categories of other users 
					ok=filterCategory.getUser().getId()==userSessionService.getCurrentUserId() ||
						getFilterOtherUsersQuestionsEnabled(operation).booleanValue();
					/*
					User currentUser=userSessionService.getCurrentUser(operation);
					User categoryUser=filterCategory.getUser();
					ok=currentUser.equals(categoryUser) || 
						getFilterOtherUsersQuestionsEnabled(operation).booleanValue();
					*/
				}
				else
				{
					ok=false;
				}
			}
			else
			{
				/*
				User currentUser=userSessionService.getCurrentUser(operation);
				if (!currentUser.equals(categoryUser))
				*/
				
				// First we have to see if the category is owned by current user, 
				// if that is not the case we will need to perform aditional checks  
				User categoryUser=filterCategory.getUser();
				if (categoryUser.getId()!=userSessionService.getCurrentUserId())
				{
					// We need to check that current user has permission to filter by categories 
					// of other users
					if (getFilterOtherUsersQuestionsEnabled(operation).booleanValue())
					{
						// We have to see if this a public or a private category
						// Public categories doesn't need more checks
						// But private categories need aditional permissions
						Visibility privateVisibility=
							visibilitiesService.getVisibility(operation,"CATEGORY_VISIBILITY_PRIVATE");
						if (filterCategory.getVisibility().getLevel()>=privateVisibility.getLevel())
						{
							// Finally we need to check that current user has permission to view questions 
							// from private categories of other users, and aditionally we need to check 
							// that current user has permission to view questions from private categories 
							// of administrators if the owner of the category is an administrator and 
							// to check that current user has permission to view questions from 
							// private categories of users with permission to improve permissions 
							// over its owned ones if the owner of the category has that permission 
							// (superadmin)
							ok=getViewQuestionsFromOtherUsersPrivateCategoriesEnabled(operation).booleanValue() &&
								(!isAdmin(operation,categoryUser) || 
								getViewQuestionsFromAdminsPrivateCategoriesEnabled(operation).booleanValue()) &&
								(!isSuperadmin(operation,categoryUser) || 
								getViewQuestionsFromSuperadminsPrivateCategoriesEnabled(operation).booleanValue());
						}
					}
					else
					{
						ok=false;
					}
				}
			}
		}
		return ok;
	}
    
    /**
	 * Change questions to display on datatable based on filter.
     * @param event Action event
     */
    public void applyQuestionsReleasesFilter(ActionEvent event)
    {
    	// Get current user session Hibernate operation
    	Operation operation=getCurrentUserOperation(null);
   		
    	setFilterGlobalQuestionsEnabled(null);
   		setFilterOtherUsersQuestionsEnabled(null);
       	Category filterQuestionCategory=null;
       	long filterQuestionCategoryId=getFilterQuestionCategoryId(operation);
       	if (filterQuestionCategoryId>0L)
       	{
       		filterQuestionCategory=categoriesService.getCategory(operation,filterQuestionCategoryId);
       		resetAdminFromCategoryAllowed(filterQuestionCategory);
       		resetSuperadminFromCategoryAllowed(filterQuestionCategory);
       	}
   		setViewQuestionsFromOtherUsersPrivateCategoriesEnabled(null);
   		setViewQuestionsFromAdminsPrivateCategoriesEnabled(null);
   		setViewQuestionsFromSuperadminsPrivateCategoriesEnabled(null);
       	if (checkQuestionsFilterPermission(operation,filterQuestionCategory))
       	{
       		// Reload questions from DB and question releases from Test Navigator production environment
       		setQuestionsReleases(null);
       		questions=null;
       	}
       	else
       	{
       		addErrorMessage(true,"INCORRECT_OPERATION","NON_AUTHORIZED_ACTION_ERROR");
       		resetAdmins();
       		resetSuperadmins();
       		resetPublishQuestionsAllowed();
       		setPublishQuestionsEnabled(null);
       		setPublishOtherUsersQuestionsEnabled(null);
       		setPublishAdminsQuestionsEnabled(null);
       		setPublishSuperadminsQuestionsEnabled(null);
       	}
    }
    
	/**
	 * @param operation Operation
	 * @param filterCategory Filter category can be optionally passed as argument
	 * @return true if user has permissions to display tests with the current selected filter, 
	 * false otherwise
	 */
    private boolean checkTestsFilterPermission(Operation operation,Category filterCategory)
	{
    	boolean ok=true;
    	
    	// Get current user session Hibernate operation
    	operation=getCurrentUserOperation(operation);
    	
    	long filterTestCategoryId=getFilterTestCategoryId(operation);
		if (getSpecialTestCategoryFiltersMap().containsKey(Long.valueOf(filterTestCategoryId)))
		{
			SpecialCategoryFilter filter=
				getSpecialTestCategoryFiltersMap().get(Long.valueOf(filterTestCategoryId));
			for (String requiredPermission:filter.requiredPermissions)
			{
				if (userSessionService.isDenied(operation,requiredPermission))
				{
					ok=false;
					break;
				}
			}
		}
		else
		{
			// Check permissions needed for selected category
			if (filterCategory==null)
			{
				// If we have not received filter category as argument we need to get it from DB
				filterCategory=categoriesService.getCategory(operation,filterTestCategoryId);
			}
			if (filterCategory.getVisibility().isGlobal())
			{
				// This is a global category, so we check that current user has permissions to filter
				// tests by global categories
				if (getFilterGlobalTestsEnabled(operation).booleanValue())
				{
					// Moreover we need to check that the category is owned by current user or 
					// that current user has permission to filter by categories of other users
					ok=filterCategory.getUser().getId()==userSessionService.getCurrentUserId() ||
						getFilterOtherUsersTestsEnabled(operation).booleanValue();
					/*
					User currentUser=userSessionService.getCurrentUser(operation);
					User categoryUser=filterCategory.getUser();
					ok=currentUser.equals(categoryUser) || getFilterOtherUsersTestsEnabled(operation).booleanValue();
					*/
				}
				else
				{
					ok=false;
				}
			}
			else
			{
				/*
				User currentUser=userSessionService.getCurrentUser(operation);
				if (!currentUser.equals(categoryUser))
				*/
				
				// First we have to see if the category is owned by current user, 
				// if that is not the case we will need to perform aditional checks  
				User categoryUser=filterCategory.getUser();
				if (categoryUser.getId()!=userSessionService.getCurrentUserId())
				{
					// We need to check that current user has permission to filter by categories 
					// of other users
					if (getFilterOtherUsersTestsEnabled(operation).booleanValue())
					{
						// We have to see if this a public or a private category
						// Public categories doesn't need more checks
						// But private categories need aditional permissions
						Visibility privateVisibility=
							visibilitiesService.getVisibility(operation,"CATEGORY_VISIBILITY_PRIVATE");
						if (filterCategory.getVisibility().getLevel()>=privateVisibility.getLevel())
						{
							// Finally we need to check that current user has permission to view tests 
							// from private categories of other users, and aditionally we need to check 
							// that current user has permission to view tests from private categories 
							// of administrators if the owner of the category is an administrator 
							// and to check that current user has permission to view tests from 
							// private categories of users with permission to improve permissions 
							// over its owned ones if the owner of the category has that permission 
							// (superadmin)
							ok=getViewTestsFromOtherUsersPrivateCategoriesEnabled(operation).booleanValue() &&
								(!isAdmin(operation,categoryUser) || 
								getViewTestsFromAdminsPrivateCategoriesEnabled(operation).booleanValue()) &&
								(!isSuperadmin(operation,categoryUser) || 
								getViewTestsFromSuperadminsPrivateCategoriesEnabled(operation).booleanValue());
						}
					}
					else
					{
						ok=false;
					}
				}
			}
		}
		return ok;
	}
    
	/**
	 * Ajax listener to uncheck the 'Display old versions' checkbox when the user selects the option
	 * 'Not released' within the publication status field of the filter panel for test resleases.
	 * @param event Ajax event
	 */
	public void changeFilterTestPublicationStatus(AjaxBehaviorEvent event)
	{
		if (!isFilterTestDisplayOldVersionsEnabled())
		{
			setFilterTestDisplayOldVersions(false);
		}
	}
    
    /**
	 * Change tests releases to display on datatable based on filter.
     * @param event Action event
     */
    public void applyTestsReleasesFilter(ActionEvent event)
    {
    	// Get current user session Hibernate operation
    	Operation operation=getCurrentUserOperation(null);
   		
    	setFilterGlobalTestsEnabled(null);
   		setFilterOtherUsersTestsEnabled(null);
   		Category filterCategory=null;
   		long filterTestCategoryId=getFilterTestCategoryId(operation);
   		if (filterTestCategoryId>0L)
   		{
   			filterCategory=categoriesService.getCategory(operation,filterTestCategoryId);
   			resetAdminFromCategoryAllowed(filterCategory);
   			resetSuperadminFromCategoryAllowed(filterCategory);
   		}
   		setViewTestsFromOtherUsersPrivateCategoriesEnabled(null);
   		setViewTestsFromAdminsPrivateCategoriesEnabled(null);
   		setViewTestsFromSuperadminsPrivateCategoriesEnabled(null);
   		if (checkTestsFilterPermission(operation,filterCategory))
   		{
   			// Reload tests from DB and test releases from Test Navigator production environment
       		setTestsReleases(null);
       		tests=null;
   		}
   		else
   		{
       		addErrorMessage(true,"INCORRECT_OPERATION","NON_AUTHORIZED_ACTION_ERROR");
       		resetAdmins();
       		resetSuperadmins();
       		resetPublishTestsAllowed();
       		setPublishTestsEnabled(null);
       		setPublishOtherUsersTestsEnabled(null);
       		setPublishAdminsTestsEnabled(null);
       		setPublishSuperadminsTestsEnabled(null);
   		}
    }
    
	/**
	 * Display a question in OM Test Navigator test environment web application.
	 * @param questionId Question's identifier
	 * @return Next view
	 * @throws Exception
	 */
	public String viewOMQuestion(long questionId) throws Exception
	{
		// Get current user session Hibernate operation 
		Operation operation=getCurrentUserOperation(null);
		
		Question question=null;
		
		setViewOMQuestionsEnabled(null);
		if (getViewOMQuestionsEnabled(operation).booleanValue())
		{
			// Get question
			question=questionsService.getQuestion(operation,questionId);
		}
		
		if (question==null)
		{
    		addErrorMessage(true,"INCORRECT_OPERATION","NON_AUTHORIZED_ACTION_ERROR");
			setFilterGlobalQuestionsEnabled(null);
			setFilterOtherUsersQuestionsEnabled(null);
			resetAdmins();
			resetSuperadmins();
			resetPublishQuestionAllowed(null);
			setPublishQuestionsEnabled(null);
			setPublishOtherUsersQuestionsEnabled(null);
			setPublishAdminsQuestionsEnabled(null);
			setPublishSuperadminsQuestionsEnabled(null);
			setViewQuestionsFromOtherUsersPrivateCategoriesEnabled(null);
			setViewQuestionsFromAdminsPrivateCategoriesEnabled(null);
			setViewQuestionsFromSuperadminsPrivateCategoriesEnabled(null);
    		
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
						buildQuestion=
							lastTimeBuild!=QuestionGenerator.getQuestionJarLastModifiedDate(packageName,omURL).getTime();
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
			StringBuffer urlParam=new StringBuffer(omTnURL);
			if (omTnURL.charAt(omTnURL.length()-1)!='/')
			{
				urlParam.append('/');
			}
			RequestContext requestContext=RequestContext.getCurrentInstance();
			requestContext.addCallbackParam("url",urlParam.toString());
			requestContext.addCallbackParam("packageName",packageName);
		}
	    return null;
	}
    
	/**
	 * Display test in OM Test Navigator test environment web application.
	 * @param testId Test's identifier
	 * @return Next view
	 * @throws Exception
	 */
	public String viewOMTest(long testId) throws Exception
	{
		// Get current user session Hibernate operation
		Operation operation=getCurrentUserOperation(null);
		
		Test test=null;
			
		setViewOMTestsEnabled(null);
		if (getViewOMTestsEnabled(operation).booleanValue())
		{
			// Get test
			test=testsService.getTest(operation,testId);
		}
		
		if (test==null)
		{
    		addErrorMessage(true,"INCORRECT_OPERATION","NON_AUTHORIZED_ACTION_ERROR");
			setFilterGlobalTestsEnabled(null);
			setFilterOtherUsersTestsEnabled(null);
    		resetAdmins();
    		resetSuperadmins();
			resetPublishTestAllowed(null);
			setPublishTestsEnabled(null);
			setPublishOtherUsersTestsEnabled(null);
			setPublishAdminsTestsEnabled(null);
			setPublishSuperadminsTestsEnabled(null);
    		setViewTestsFromOtherUsersPrivateCategoriesEnabled(null);
    		setViewTestsFromAdminsPrivateCategoriesEnabled(null);
    		setViewTestsFromSuperadminsPrivateCategoriesEnabled(null);
			
    		RequestContext requestContext=RequestContext.getCurrentInstance();
			requestContext.addCallbackParam("url","error");
		}
		else
		{
			// Get destination path
			String path=configurationService.getOmQuestionsPath();
			
			// Get OM Develover, OM Test Navigator and OM Question Engine URLs
			String omURL=configurationService.getOmUrl();
			String omTnURL=configurationService.getOmTnUrl();
			String omQeURL=configurationService.getOmQeUrl();
			
			// Get test's signature
			String testName=test.getSignature();			
			
			// List of questions included in test
			List<Question> questions=new ArrayList<Question>();
			
			// Get section's questions
			for (Section section:test.getSections())
			{
				
				for (QuestionOrder questionOrder:section.getQuestionOrders())
				{
					Question question=questionOrder.getQuestion();
					if (!questions.contains(question))
					{
						questions.add(questionsService.getQuestion(operation,question.getId()));
					}
				}
			}
			
			boolean neededClosingUserSessionOperation=false;
			try
			{
				boolean copyJarFiles=false;
				for (Question question:questions)
				{
					// Get package's name
					String packageName=question.getPackage();
					
					// Check if we need to build question jar
					boolean buildQuestion=true;
					long lastTimeModified=question.getTimemodified()==null?-1:question.getTimemodified().getTime();
					long lastTimeDeploy=question.getTimedeploy()==null?-1:question.getTimedeploy().getTime();
					long lastTimeBuild=question.getTimebuild()==null?-1:question.getTimebuild().getTime();
					if (lastTimeDeploy!=-1 && lastTimeDeploy>lastTimeBuild && lastTimeDeploy>lastTimeModified)
					{
						try
						{
							TestGenerator.checkQuestionJar(packageName,"1.0",omTnURL);
							buildQuestion=lastTimeDeploy!=
								TestGenerator.getQuestionJarLastModifiedDate(packageName,"1.0",omTnURL).getTime();
						}
						catch (Exception e)
						{
							// Question's jar don't exists on OM Test Navigator Web Application 
							// so we ignore this exception
						}
					}
					if (buildQuestion && lastTimeBuild!=-1 && lastTimeBuild>lastTimeModified)
					{
						try
						{
							QuestionGenerator.checkQuestionJar(packageName,omURL);
							buildQuestion=lastTimeBuild!=QuestionGenerator.getQuestionJarLastModifiedDate(
								packageName,omURL).getTime();
						}
						catch (Exception e)
						{
							// Question's jar don't exists on OM Developer Web Application
							// so we ignore this exception
						}
					}
					
					// Build question if needed
					if (buildQuestion)
					{
						neededClosingUserSessionOperation=true;
						
						// If we need to build any question we will need to copy jar files even 
						// if we won't need to deploy the test
						copyJarFiles=true;
						
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
						
						// Save question
						questionsService.updateQuestion(question);
					}
				}
				
				boolean deployTest=true;
				long lastTimeModified=test.getTimeModified()==null?-1:test.getTimeModified().getTime();
				long lastTimeTestDeploy=test.getTimeTestDeploy()==null?-1:test.getTimeTestDeploy().getTime();
				long lastTimeDeployDeploy=test.getTimeDeployDeploy()==null?-1:test.getTimeDeployDeploy().getTime();
				if (lastTimeTestDeploy!=-1 && lastTimeDeployDeploy!=-1 && lastTimeTestDeploy>lastTimeModified && 
					lastTimeDeployDeploy>lastTimeModified)
				{
					try
					{
						TestGenerator.checkTestNavigatorXmls(new TestRelease(test,0,null),omTnURL);
						// Test or deploy xmls doesn't exist on OM Test Navigator Web Application 
						// so we ignore this exception
					}
					catch (Exception e)
					{
						deployTest=
							lastTimeTestDeploy!=TestGenerator.getTestXmlLastModifiedDate(testName,omTnURL).getTime() ||
							lastTimeDeployDeploy!=TestGenerator.getDeployXmlLastModifiedDate(testName,omTnURL).getTime();
					}
				}
				
				if (deployTest)
				{
					neededClosingUserSessionOperation=true;
					
					// Generate test
					TestGenerator.generateTest(test,omURL,omQeURL,omTnURL,true);
					
					// Update deploy time on test xml file
					test.setTimeTestDeploy(TestGenerator.getTestXmlLastModifiedDate(testName,omTnURL));
					
					// Update deploy time on deploy xml file
					test.setTimeDeployDeploy(TestGenerator.getDeployXmlLastModifiedDate(testName,omTnURL));
					
					// Save test to update deploy dates
					testsService.updateTest(test);
				}
				else if (copyJarFiles)
				{
					// Copy jar files
					TestGenerator.copyJarFiles(test,omURL,omQeURL,omTnURL,false);
				}
			}
			finally
			{
				if (neededClosingUserSessionOperation)
				{
					// End current user session Hibernate operation
					userSessionService.endCurrentUserOperation();
				}
			}
			
			// Get URL of OM Test Navigator (we need to be sure that ends with a slash)
			StringBuffer urlParam=new StringBuffer(omTnURL);
			if (omTnURL.charAt(omTnURL.length()-1)!='/')
			{
				urlParam.append('/');
			}
			
			// Pass parameters to javascript to display test
			RequestContext requestContext=RequestContext.getCurrentInstance();
			requestContext.addCallbackParam("url",urlParam.toString());
			requestContext.addCallbackParam("testName",testName);
		}
		return null;
	}
	
	/**
	 * Deletes a question release if current user has the needed permissions.
	 * @param event Action event
	 */
	public void unpublishQuestionRelease(ActionEvent event)
	{
		QuestionRelease questionRelease=getQuestionRelease();
		resetAdminFromQuestionReleaseAllowed(questionRelease);
		resetSuperadminFromQuestionReleaseAllowed(questionRelease);
		setUnpublishQuestionReleasesEnabled(null);
		setUnpublishOtherUsersQuestionReleasesEnabled(null);
		setUnpublishAdminsQuestionReleasesEnabled(null);
		setUnpublishSuperadminsQuestionReleasesEnabled(null);
		setUnpublishQuestionReleasesOpenedWithCloseDateEnabled(null);
		setUnpublishQuestionReleasesBeforeDeleteDateEnabled(null);
		resetUnpublishQuestionReleaseAllowed(questionRelease);
		if (isUnpublishQuestionReleaseAllowed(null,getQuestionRelease(),true))
		{
			// Get OM Test Navigator URL production environment
			String omTnProURL=configurationService.getOmTnProUrl();
			try
			{
				// Delete question XML file from OM Test Navigator production environment web application
				QuestionGenerator.unpublishQuestionRelease(
					questionRelease.getQuestion().getPackage(),omTnProURL);
			}
			catch (Exception e)
			{
				// Ignore OM Test Navigator errors
				//TODO ¿seguir ignorando o hacer un rollback y lanzar un ServiceException?
			}
			
			// Reload questions releases from OM Test Navigator production environment
			resetAdmins();
			resetSuperadmins();
			resetUnpublishQuestionReleasesAllowed();
			setQuestionsReleases(null);
		}
	}
	
	/**
	 * Deletes a test release if current user has the needed permissions.
	 * @param event Action event
	 */
	public void unpublishTestRelease(ActionEvent event)
	{
		TestRelease testRelease=getTestRelease();
		resetAdminFromTestReleaseAllowed(testRelease);
		resetSuperadminFromTestReleaseAllowed(testRelease);
		setUnpublishTestReleasesEnabled(null);
		setUnpublishOtherUsersTestReleasesEnabled(null);
		setUnpublishAdminsTestReleasesEnabled(null);
		setUnpublishSuperadminsTestReleasesEnabled(null);
		setUnpublishTestReleasesOpenedWithCloseDateEnabled(null);
		setUnpublishTestReleasesBeforeDeleteDateEnabled(null);
		resetUnpublishTestReleaseAllowed(testRelease);
		if (isUnpublishTestReleaseAllowed(null,getTestRelease(),true))
		{
			// Get OM Test Navigator URL production environment
			String omTnProURL=configurationService.getOmTnProUrl();
			try
			{
				// Delete test XML file from OM Test Navigator production environment web application
				TestGenerator.unpublishTestRelease(testRelease,omTnProURL);
			}
			catch (Exception e)
			{
				// Ignore OM Test Navigator errors
				//TODO ¿seguir ignorando o hacer un rollback y lanzar un ServiceException?
			}
			
			// Reload tests releases from OM Test Navigator production environment
			resetAdmins();
			resetSuperadmins();
			resetUnpublishTestReleasesAllowed();
			setTestsReleases(null);
		}
	}
	
	/**
	 * @param packageName Package's name
	 * @param omTnProUrl Openmark Test Navigator production environment URL
	 * @return true if question release has not been deleted recently
	 */
	private boolean checkQuestionRelease(String packageName,String omTnProUrl)
	{
		boolean ok=false;
		try
		{
			QuestionGenerator.checkTestNavigatorXml(packageName, omTnProUrl);
		}
		catch (Exception e)
		{
			ok=true;
		}
		return ok;
	}
	
	/**
	 * Display a question in OM Test Navigator production environment web application.
	 * @param event Action event
	 * @throws Exception
	 */
	public void viewOMQuestionRelease(ActionEvent event) throws Exception
	{
		String omTnProURL=configurationService.getOmTnProUrl();
		String packageName=getQuestionRelease().getQuestion().getPackage();
		if (checkQuestionRelease(packageName,omTnProURL))
		{
			// Add callback parameters to display question
			StringBuffer urlParam=new StringBuffer(omTnProURL);
			if (omTnProURL.charAt(omTnProURL.length()-1)!='/')
			{
				urlParam.append('/');
			}
			RequestContext requestContext=RequestContext.getCurrentInstance();
			requestContext.addCallbackParam("url",urlParam.toString());
			requestContext.addCallbackParam("packageName",packageName);
		}
		else
		{
			addErrorMessage(true,"INCORRECT_OPERATION","OM_VIEW_QUESTION_RELEASE_UNPUBLISH_ERROR");
			
			// Reload questions releases from OM Test Navigator production environment
			resetAdmins();
			resetSuperadmins();
			setUnpublishQuestionReleasesEnabled(null);
			setUnpublishOtherUsersQuestionReleasesEnabled(null);
			setUnpublishAdminsQuestionReleasesEnabled(null);
			setUnpublishSuperadminsQuestionReleasesEnabled(null);
			setUnpublishQuestionReleasesOpenedWithCloseDateEnabled(null);
			setUnpublishQuestionReleasesBeforeDeleteDateEnabled(null);
			resetUnpublishQuestionReleasesAllowed();
			setQuestionsReleases(null);
			
    		RequestContext requestContext=RequestContext.getCurrentInstance();
			requestContext.addCallbackParam("url","error");
		}
	}
	
	/**
	 * @param testRelease Test release
	 * @param omTnProUrl Openmark Test Navigator production environment URL
	 * @return true if test release has not been deleted recently
	 */
	private boolean checkTestRelease(TestRelease testRelease,String omTnProUrl)
	{
		boolean ok=false;
		try
		{
			TestGenerator.checkTestNavigatorXmls(testRelease,omTnProUrl);
		}
		catch (Exception e)
		{
			ok=true;
		}
		return ok;
	}
	
	/**
	 * Display a test in OM Test Navigator production environment web application.
	 * @param event Action event
	 * @throws Exception
	 */
	public void viewOMTestRelease(ActionEvent event) throws Exception
	{
		String omTnProURL=configurationService.getOmTnProUrl();
		if (checkTestRelease(getTestRelease(),omTnProURL))
		{
			String testName=getTestRelease().getTest().getSignature();
			int version=getTestRelease().getVersion();
			
			// Add callback parameters to display test
			StringBuffer urlParam=new StringBuffer(omTnProURL);
			if (omTnProURL.charAt(omTnProURL.length()-1)!='/')
			{
				urlParam.append('/');
			}
			StringBuffer sVersion=new StringBuffer();
			if (version>=10)
			{
				sVersion.append('-');
				sVersion.append(Integer.toString(version));
			}
			else if (version >0)
			{
				sVersion.append("-0");
				sVersion.append(Integer.toString(version));
			}
			
			RequestContext requestContext=RequestContext.getCurrentInstance();
			requestContext.addCallbackParam("url",urlParam.toString());
			requestContext.addCallbackParam("testName",testName);
			requestContext.addCallbackParam("version",sVersion.toString());
		}
		else
		{
			addErrorMessage(true,"INCORRECT_OPERATION","OM_VIEW_TEST_RELEASE_UNPUBLISH_ERROR");
			
			// Reload tests releases from OM Test Navigator production environment
			resetAdmins();
			resetSuperadmins();
			
			setUnpublishTestReleasesEnabled(null);
			setUnpublishOtherUsersTestReleasesEnabled(null);
			setUnpublishAdminsTestReleasesEnabled(null);
			setUnpublishSuperadminsTestReleasesEnabled(null);
			setUnpublishTestReleasesOpenedWithCloseDateEnabled(null);
			setUnpublishTestReleasesBeforeDeleteDateEnabled(null);
			resetUnpublishTestReleasesAllowed();
			setTestsReleases(null);
			
    		RequestContext requestContext=RequestContext.getCurrentInstance();
			requestContext.addCallbackParam("url","error");
		}
	}
	
	/**
	 * Displays an error message.
	 * @param criticalError Flag to indicate if error is critical (true) or not (false)
	 * @param title Error title (before localization)
	 * @param message Error message (before localization)
	 */
	private void addErrorMessage(boolean criticalError,String title,String message)
	{
		FacesContext context=FacesContext.getCurrentInstance();
		setCriticalErrorMessage(criticalError);
		context.addMessage(null,new FacesMessage(FacesMessage.SEVERITY_ERROR,
			localizationService.getLocalizedMessage(title),
			localizationService.getLocalizedMessage(message)));
	}
	
	/**
	 * Displays an error message.
	 * @param criticalError Flag to indicate if error is critical (true) or not (false)
	 * @param title Error title (localized)
	 * @param message Error message (localized)
	 */
	private void addPlainErrorMessage(boolean criticalError,String title,String message)
	{
		FacesContext context=FacesContext.getCurrentInstance();
		setCriticalErrorMessage(criticalError);
		context.addMessage(null,new FacesMessage(FacesMessage.SEVERITY_ERROR,title,message));
	}
	
	/**
	 * Handles a change of the current locale.<br/><br/>
	 * This implementation localize the item 'All' of the 'Category' combos in the filter's panels because 
	 * submitting form is not enough to localize them.
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
		
		// Get current user session Hibernate operation
		Operation operation=getCurrentUserOperation(null);
		
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
		SpecialCategoryFilter allQuestionCategories=getAllQuestionCategoriesSpecialCategoryFilter();
		if (!allOptionsForCategory.equals(allQuestionCategories.name))
		{
			allQuestionCategories.name=allOptionsForCategory;
			Category allOptionsQuestionCategoryAux=new Category();
			allOptionsQuestionCategoryAux.setId(allQuestionCategories.id);
			List<Category> specialQuestionCategoriesFilters=getSpecialQuestionCategoriesFilters(operation);
			Category allOptionsQuestionCategory=specialQuestionCategoriesFilters.get(
				specialQuestionCategoriesFilters.indexOf(allOptionsQuestionCategoryAux));
			allOptionsQuestionCategory.setName(allOptionsForCategory);
		}
		SpecialCategoryFilter allTestCategories=getAllTestCategoriesSpecialCategoryFilter();
		if (!allOptionsForCategory.equals(allTestCategories.name))
		{
			allTestCategories.name=allOptionsForCategory;
			Category allOptionsTestCategoryAux=new Category();
			allOptionsTestCategoryAux.setId(allTestCategories.id);
			List<Category> specialTestCategoriesFilters=getSpecialTestCategoriesFilters(operation);
			Category allOptionsTestCategory=specialTestCategoriesFilters.get(
				specialTestCategoriesFilters.indexOf(allOptionsTestCategoryAux));
			allOptionsTestCategory.setName(allOptionsForCategory);
		}
	}
}
