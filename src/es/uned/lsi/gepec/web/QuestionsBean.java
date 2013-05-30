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

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
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

import org.apache.commons.io.FileUtils;
import org.primefaces.context.RequestContext;

import es.uned.lsi.gepec.model.QuestionLevel;
import es.uned.lsi.gepec.model.entities.Category;
import es.uned.lsi.gepec.model.entities.Question;
import es.uned.lsi.gepec.model.entities.QuestionType;
import es.uned.lsi.gepec.model.entities.User;
import es.uned.lsi.gepec.model.entities.Visibility;
import es.uned.lsi.gepec.om.OmHelper;
import es.uned.lsi.gepec.om.QuestionGenerator;
import es.uned.lsi.gepec.om.TestGenerator;
import es.uned.lsi.gepec.util.HibernateUtil.Operation;
import es.uned.lsi.gepec.web.services.CategoriesService;
import es.uned.lsi.gepec.web.services.CategoryTypesService;
import es.uned.lsi.gepec.web.services.ConfigurationService;
import es.uned.lsi.gepec.web.services.LocalizationService;
import es.uned.lsi.gepec.web.services.PermissionsService;
import es.uned.lsi.gepec.web.services.QuestionDeleteConstraintServiceException;
import es.uned.lsi.gepec.web.services.QuestionReleasesService;
import es.uned.lsi.gepec.web.services.QuestionTypesService;
import es.uned.lsi.gepec.web.services.QuestionsService;
import es.uned.lsi.gepec.web.services.ServiceException;
import es.uned.lsi.gepec.web.services.UserSessionService;
import es.uned.lsi.gepec.web.services.VisibilitiesService;

//Backbean para la vista preguntas
//author Víctor Manuel Alonso Rodríguez
//since  12/2011
/**
 * Managed bean for managing questions.
 */
@SuppressWarnings("serial")
@ManagedBean(name="questionsBean")
@ViewScoped
public class QuestionsBean implements Serializable
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
	
	private final static SpecialCategoryFilter ALL_EVEN_PRIVATE_CATEGORIES_OF_OTHER_USERS;
	static
	{
		List<String> allEvenPrivateCategoriesOfOtherUsersPermissions=new ArrayList<String>();
		allEvenPrivateCategoriesOfOtherUsersPermissions.add("PERMISSION_QUESTIONS_GLOBAL_FILTER_ENABLED");
		allEvenPrivateCategoriesOfOtherUsersPermissions.add("PERMISSION_QUESTIONS_OTHER_USERS_FILTER_ENABLED");
		allEvenPrivateCategoriesOfOtherUsersPermissions.add(
			"PERMISSION_QUESTIONS_VIEW_QUESTIONS_OF_OTHER_USERS_PRIVATE_CATEGORIES_ENABLED");
		ALL_EVEN_PRIVATE_CATEGORIES_OF_OTHER_USERS=new SpecialCategoryFilter(
			-1L,"ALL_EVEN_PRIVATE_CATEGORIES_OF_OTHER_USERS",allEvenPrivateCategoriesOfOtherUsersPermissions);
	}
	private final static SpecialCategoryFilter ALL_MY_CATEGORIES;
	static
	{
		List<String> allMyCategoriesPermissions=new ArrayList<String>();
		allMyCategoriesPermissions.add("PERMISSION_QUESTIONS_GLOBAL_FILTER_ENABLED");
		ALL_MY_CATEGORIES=new SpecialCategoryFilter(-2L,"ALL_MY_CATEGORIES",allMyCategoriesPermissions);
	}
	private final static SpecialCategoryFilter ALL_MY_CATEGORIES_EXCEPT_GLOBALS=
		new SpecialCategoryFilter(-3L,"ALL_MY_CATEGORIES_EXCEPT_GLOBALS",new ArrayList<String>());
	private final static SpecialCategoryFilter ALL_GLOBAL_CATEGORIES;
	static
	{
		List<String> allGlobalCategoriesPermissions=new ArrayList<String>();
		allGlobalCategoriesPermissions.add("PERMISSION_QUESTIONS_GLOBAL_FILTER_ENABLED");
		ALL_GLOBAL_CATEGORIES=
			new SpecialCategoryFilter(-4L,"ALL_GLOBAL_CATEGORIES",allGlobalCategoriesPermissions);
	}
	private final static SpecialCategoryFilter ALL_PUBLIC_CATEGORIES_OF_OTHER_USERS;
	static
	{
		List<String> allPublicCategoriesOfOtherUsersPermissions=new ArrayList<String>();
		allPublicCategoriesOfOtherUsersPermissions.add("PERMISSION_QUESTIONS_OTHER_USERS_FILTER_ENABLED");
		ALL_PUBLIC_CATEGORIES_OF_OTHER_USERS=new SpecialCategoryFilter(
			-5L,"ALL_PUBLIC_CATEGORIES_OF_OTHER_USERS",allPublicCategoriesOfOtherUsersPermissions);
	}
	private final static SpecialCategoryFilter ALL_PRIVATE_CATEGORIES_OF_OTHER_USERS;
	static
	{
		List<String> allPrivateCategoriesOfOtherUsersPermissions=new ArrayList<String>();
		allPrivateCategoriesOfOtherUsersPermissions.add("PERMISSION_QUESTIONS_OTHER_USERS_FILTER_ENABLED");
		allPrivateCategoriesOfOtherUsersPermissions.add(
			"PERMISSION_QUESTIONS_VIEW_QUESTIONS_OF_OTHER_USERS_PRIVATE_CATEGORIES_ENABLED");
		ALL_PRIVATE_CATEGORIES_OF_OTHER_USERS=new SpecialCategoryFilter(
			-6L,"ALL_PRIVATE_CATEGORIES_OF_OTHER_USERS",allPrivateCategoriesOfOtherUsersPermissions);
	}
	private final static SpecialCategoryFilter ALL_CATEGORIES_OF_OTHER_USERS;
	static
	{
		List<String> allCategoriesOfOtherUsersPermissions=new ArrayList<String>();
		allCategoriesOfOtherUsersPermissions.add("PERMISSION_QUESTIONS_OTHER_USERS_FILTER_ENABLED");
		allCategoriesOfOtherUsersPermissions.add(
			"PERMISSION_QUESTIONS_VIEW_QUESTIONS_OF_OTHER_USERS_PRIVATE_CATEGORIES_ENABLED");
		ALL_CATEGORIES_OF_OTHER_USERS=
			new SpecialCategoryFilter(-7L,"ALL_CATEGORIES_OF_OTHER_USERS",allCategoriesOfOtherUsersPermissions);
	}
	
	@ManagedProperty(value="#{localizationService}")
	private LocalizationService localizationService;
	@ManagedProperty(value="#{configurationService}")
	private ConfigurationService configurationService;
	@ManagedProperty(value="#{questionsService}")
	private QuestionsService questionsService;
	@ManagedProperty(value="#{questionReleasesService}")
	private QuestionReleasesService questionReleasesService;
	@ManagedProperty(value="#{questionTypesService}")
	private QuestionTypesService questionTypesService;
	@ManagedProperty(value="#{categoriesService}")
	private CategoriesService categoriesService;
	@ManagedProperty(value="#{categoryTypesService}")
	private CategoryTypesService categoryTypesService;
	@ManagedProperty(value="#{visibilitiesService}")
	private VisibilitiesService visibilitiesService;
	@ManagedProperty(value="#{userSessionService}")
	private UserSessionService userSessionService;
	@ManagedProperty(value="#{permissionsService}")
	private PermissionsService permissionsService;
	
	private List<Question> questions;					// List of questions
	
	private String selectedQuestionTypeName;			// Selected question's type name
	private QuestionType selectedQuestionType;  		// Selected question's type
	private Question selectedQuestion;					// Selected question
	private Long questionId;							// Selected question's identifier
	
	
	private Map<Long,SpecialCategoryFilter> specialCategoryFiltersMap;
	private SpecialCategoryFilter allCategories;
	
	private List<Category> specialCategoriesFilters;	// Special categories filters list
	private List<Category> questionsCategories;			// Questions categories list
	
	private long filterCategoryId;
	private boolean filterIncludeSubcategories;
	private String filterQuestionType;
	private String filterQuestionLevel;
	private boolean criticalErrorMessage;
	private Boolean filterGlobalQuestionsEnabled;
	private Boolean filterOtherUsersQuestionsEnabled;
	private Boolean addEnabled;
	private Boolean editEnabled;
	private Boolean createCopyEnabled;
	private Boolean deleteEnabled;
	private Boolean viewOMEnabled;
	private Boolean editOtherUsersQuestionsEnabled;
	private Boolean editAdminsQuestionsEnabled;
	private Boolean editSuperadminsQuestionsEnabled;
	private Boolean createCopyOtherUsersNonEditableQuestionsEnabled;
	private Boolean createCopyAdminsNonEditableQuestionsEnabled;
	private Boolean createCopySuperadminsNonEditableQuestionsEnabled;
	private Boolean deleteOtherUsersQuestionsEnabled;
	private Boolean deleteAdminsQuestionsEnabled;
	private Boolean deleteSuperadminsQuestionsEnabled;
	private Boolean viewQuestionsFromOtherUsersPrivateCategoriesEnabled;
	private Boolean viewQuestionsFromAdminsPrivateCategoriesEnabled;
	private Boolean viewQuestionsFromSuperadminsPrivateCategoriesEnabled;
	
	private Map<Long,Boolean> admins;
	private Map<Long,Boolean> superadmins;
	
	private Map<Long,Boolean> editQuestionsAllowed;
	private Map<Long,Boolean> createCopyQuestionsAllowed;
	private Map<Long,Boolean> deleteQuestionsAllowed;
	
	private List<QuestionLevel> questionLevels;
	
	public QuestionsBean()
	{
		selectedQuestionTypeName=null;
		selectedQuestionType=null;
		filterCategoryId=Long.MIN_VALUE;
		filterIncludeSubcategories=false;
		filterQuestionType="";
		filterQuestionLevel="";
		specialCategoryFiltersMap=null;
		allCategories=null;
		specialCategoriesFilters=null;
		questionsCategories=null;
		criticalErrorMessage=false;
		filterGlobalQuestionsEnabled=null;
		filterOtherUsersQuestionsEnabled=null;
		addEnabled=null;
		editEnabled=null;
		createCopyEnabled=null;
		deleteEnabled=null;
		viewOMEnabled=null;
		editOtherUsersQuestionsEnabled=null;
		editAdminsQuestionsEnabled=null;
		editSuperadminsQuestionsEnabled=null;
		createCopyOtherUsersNonEditableQuestionsEnabled=null;
		createCopyAdminsNonEditableQuestionsEnabled=null;
		createCopySuperadminsNonEditableQuestionsEnabled=null;
		deleteOtherUsersQuestionsEnabled=null;
		deleteAdminsQuestionsEnabled=null;
		deleteSuperadminsQuestionsEnabled=null;
		viewQuestionsFromOtherUsersPrivateCategoriesEnabled=null;
		viewQuestionsFromAdminsPrivateCategoriesEnabled=null;
		viewQuestionsFromSuperadminsPrivateCategoriesEnabled=null;
		admins=new HashMap<Long,Boolean>();
		superadmins=new HashMap<Long,Boolean>();
		editQuestionsAllowed=new HashMap<Long,Boolean>();
		createCopyQuestionsAllowed=new HashMap<Long,Boolean>();
		deleteQuestionsAllowed=new HashMap<Long,Boolean>();
		questions=null;
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
	
	public void setQuestionReleasesService(QuestionReleasesService questionReleasesService)
	{
		this.questionReleasesService=questionReleasesService;
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
     * @return true if current user is allowed to navigate "Questions" page, false otherwise
     */
    public boolean isNavigationAllowed()
    {
    	return isNavigationAllowed(null);
    }
    
    /**
     * @param operation Operation
     * @return true if current user is allowed to navigate "Questions" page, false otherwise
     */
    private boolean isNavigationAllowed(Operation operation)
    {
    	return userSessionService.isGranted(getCurrentUserOperation(operation),"PERMISSION_NAVIGATION_QUESTIONS");
    }
    
	public Long getQuestionId()
	{
		return questionId;
	}
	
	public void setQuestionId(Long questionId)
	{
		this.questionId=questionId;
	}
	
	private void initializeFilterCategoryId(Operation operation)
	{
		boolean found=false;
		List<Category> specialCategoriesFilters=getSpecialCategoriesFilters(getCurrentUserOperation(operation));
		Category allMyCategoriesFilter=new Category();
		allMyCategoriesFilter.setId(ALL_MY_CATEGORIES.id);
		if (specialCategoriesFilters.contains(allMyCategoriesFilter))
		{
			filterCategoryId=ALL_MY_CATEGORIES.id;
			found=true;
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
			SpecialCategoryFilter allCategories=getAllCategoriesSpecialCategoryFilter();
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
	
	public boolean isCriticalErrorMessage()
	{
		return criticalErrorMessage;
	}
	
	public void setCriticalErrorMessage(boolean criticalErrorMessage)
	{
		this.criticalErrorMessage=criticalErrorMessage;
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
				userSessionService.isGranted(getCurrentUserOperation(operation),"PERMISSION_QUESTIONS_ADD_ENABLED"));
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
				userSessionService.isGranted(getCurrentUserOperation(operation),"PERMISSION_QUESTIONS_EDIT_ENABLED"));
		}
		return editEnabled;
	}
	
	public Boolean getCreateCopyEnabled()
	{
		return getCreateCopyEnabled(null);
	}
	
	public void setCreateCopyEnabled(Boolean createCopyEnabled)
	{
		this.createCopyEnabled=createCopyEnabled;
	}
	
	public boolean isCreateCopyEnabled()
	{
		return getCreateCopyEnabled().booleanValue();
	}
	
	private Boolean getCreateCopyEnabled(Operation operation)
	{
		if (createCopyEnabled==null)
		{
			createCopyEnabled=Boolean.valueOf(userSessionService.isGranted(
				getCurrentUserOperation(operation),"PERMISSION_QUESTIONS_CREATE_COPY_ENABLED"));
		}
		return createCopyEnabled;
	}
	
	public Boolean getDeleteEnabled()
	{
		return getDeleteEnabled(null);
	}
	
	public void setDeleteEnabled(Boolean deleteEnabled)
	{
		this.deleteEnabled=deleteEnabled;
	}
	
	public boolean isDeleteEnabled()
	{
		return getDeleteEnabled().booleanValue();
	}
	
	private Boolean getDeleteEnabled(Operation operation)
	{
		if (deleteEnabled==null)
		{
			deleteEnabled=Boolean.valueOf(
				userSessionService.isGranted(getCurrentUserOperation(operation),"PERMISSION_QUESTIONS_DELETE_ENABLED"));
		}
		return deleteEnabled;
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
			viewOMEnabled=Boolean.valueOf(
				userSessionService.isGranted(getCurrentUserOperation(operation),"PERMISSION_QUESTIONS_VIEW_OM_ENABLED"));
		}
		return viewOMEnabled;
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
	
	public Boolean getCreateCopyOtherUsersNonEditableQuestionsEnabled()
	{
		return getCreateCopyOtherUsersNonEditableQuestionsEnabled(null);
	}
	
	public void setCreateCopyOtherUsersNonEditableQuestionsEnabled(
		Boolean createCopyOtherUsersNonEditableQuestionsEnabled)
	{
		this.createCopyOtherUsersNonEditableQuestionsEnabled=createCopyOtherUsersNonEditableQuestionsEnabled;
	}
	
	public boolean isCreateCopyOtherUsersNonEditableQuestionsEnabled()
	{
		return getCreateCopyOtherUsersNonEditableQuestionsEnabled().booleanValue();
	}
	
	private Boolean getCreateCopyOtherUsersNonEditableQuestionsEnabled(Operation operation)
	{
		if (createCopyOtherUsersNonEditableQuestionsEnabled==null)
		{
			createCopyOtherUsersNonEditableQuestionsEnabled=
				Boolean.valueOf(userSessionService.isGranted(getCurrentUserOperation(operation),
				"PERMISSION_QUESTIONS_CREATE_COPY_FROM_OTHER_USERS_NON_EDITABLE_QUESTIONS_ENABLED"));
		}
		return createCopyOtherUsersNonEditableQuestionsEnabled;
	}
	
	public Boolean getCreateCopyAdminsNonEditableQuestionsEnabled()
	{
		return getCreateCopyAdminsNonEditableQuestionsEnabled(null);
	}
	
	public void setCreateCopyAdminsNonEditableQuestionsEnabled(
		Boolean createCopyAdminsNonEditableQuestionsEnabled)
	{
		this.createCopyAdminsNonEditableQuestionsEnabled=createCopyAdminsNonEditableQuestionsEnabled;
	}
	
	public boolean isCreateCopyAdminsNonEditableQuestionsEnabled()
	{
		return getCreateCopyAdminsNonEditableQuestionsEnabled().booleanValue();
	}
	
	private Boolean getCreateCopyAdminsNonEditableQuestionsEnabled(Operation operation)
	{
		if (createCopyAdminsNonEditableQuestionsEnabled==null)
		{
			createCopyAdminsNonEditableQuestionsEnabled=
				Boolean.valueOf(userSessionService.isGranted(getCurrentUserOperation(operation),
				"PERMISSION_QUESTIONS_CREATE_COPY_FROM_ADMINS_NON_EDITABLE_QUESTIONS_ENABLED"));
		}
		return createCopyAdminsNonEditableQuestionsEnabled;
	}
	
	public Boolean getCreateCopySuperadminsNonEditableQuestionsEnabled()
	{
		return getCreateCopySuperadminsNonEditableQuestionsEnabled(null);
	}
	
	public void setCreateCopySuperadminsNonEditableQuestionsEnabled(
		Boolean createCopySuperadminsNonEditableQuestionsEnabled)
	{
		this.createCopySuperadminsNonEditableQuestionsEnabled=createCopySuperadminsNonEditableQuestionsEnabled;
	}
	
	public boolean isCreateCopySuperadminsNonEditableQuestionsEnabled()
	{
		return getCreateCopySuperadminsNonEditableQuestionsEnabled().booleanValue();
	}
	
	private Boolean getCreateCopySuperadminsNonEditableQuestionsEnabled(Operation operation)
	{
		if (createCopySuperadminsNonEditableQuestionsEnabled==null)
		{
			createCopySuperadminsNonEditableQuestionsEnabled=
				Boolean.valueOf(userSessionService.isGranted(getCurrentUserOperation(operation),
				"PERMISSION_QUESTIONS_CREATE_COPY_FROM_SUPERADMINS_NON_EDITABLE_QUESTIONS_ENABLED"));
		}
		return createCopySuperadminsNonEditableQuestionsEnabled;
	}
	
	public Boolean getDeleteOtherUsersQuestionsEnabled()
	{
		return getDeleteOtherUsersQuestionsEnabled(null);
	}
	
	public void setDeleteOtherUsersQuestionsEnabled(Boolean deleteOtherUsersQuestionsEnabled)
	{
		this.deleteOtherUsersQuestionsEnabled=deleteOtherUsersQuestionsEnabled;
	}
	
	public boolean isDeleteOtherUsersQuestionsEnabled()
	{
		return getDeleteOtherUsersQuestionsEnabled().booleanValue();
	}
	
	private Boolean getDeleteOtherUsersQuestionsEnabled(Operation operation)
	{
		if (deleteOtherUsersQuestionsEnabled==null)
		{
			deleteOtherUsersQuestionsEnabled=Boolean.valueOf(userSessionService.isGranted(
				getCurrentUserOperation(operation),"PERMISSION_QUESTIONS_DELETE_OTHER_USERS_QUESTIONS_ENABLED"));
		}
		return deleteOtherUsersQuestionsEnabled;
	}
	
	public Boolean getDeleteAdminsQuestionsEnabled()
	{
		return getDeleteAdminsQuestionsEnabled(null);
	}
	
	public void setDeleteAdminsQuestionsEnabled(Boolean deleteAdminsQuestionsEnabled)
	{
		this.deleteAdminsQuestionsEnabled=deleteAdminsQuestionsEnabled;
	}
	
	public boolean isDeleteAdminsQuestionsEnabled()
	{
		return getDeleteAdminsQuestionsEnabled().booleanValue();
	}
	
	private Boolean getDeleteAdminsQuestionsEnabled(Operation operation)
	{
		if (deleteAdminsQuestionsEnabled==null)
		{
			deleteAdminsQuestionsEnabled=Boolean.valueOf(userSessionService.isGranted(
				getCurrentUserOperation(operation),"PERMISSION_QUESTIONS_DELETE_ADMINS_QUESTIONS_ENABLED"));
		}
		return deleteAdminsQuestionsEnabled;
	}
	
	public Boolean getDeleteSuperadminsQuestionsEnabled()
	{
		return getDeleteSuperadminsQuestionsEnabled(null);
	}
	
	public void setDeleteSuperadminsQuestionsEnabled(Boolean deleteSuperadminsQuestionsEnabled)
	{
		this.deleteSuperadminsQuestionsEnabled=deleteSuperadminsQuestionsEnabled;
	}
	
	public boolean isDeleteSuperadminsQuestionsEnabled()
	{
		return getDeleteSuperadminsQuestionsEnabled().booleanValue();
	}
	
	private Boolean getDeleteSuperadminsQuestionsEnabled(Operation operation)
	{
		if (deleteSuperadminsQuestionsEnabled==null)
		{
			deleteSuperadminsQuestionsEnabled=Boolean.valueOf(userSessionService.isGranted(
				getCurrentUserOperation(operation),"PERMISSION_QUESTIONS_DELETE_SUPERADMINS_QUESTIONS_ENABLED"));
		}
		return deleteSuperadminsQuestionsEnabled;
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
	
	private void resetEditQuestionsAllowed()
	{
		editQuestionsAllowed.clear();
	}
	
	private void resetEditQuestionAllowed(Question question)
	{
		if (question!=null)
		{
			resetEditQuestionAllowed(question.getId());
		}
	}
	
	private void resetEditQuestionAllowed(long questionId)
	{
		editQuestionsAllowed.remove(Long.valueOf(questionId));
	}
	
	private boolean isEditQuestionAllowed(Operation operation,long questionId)
	{
		boolean allowed=false;
		if (questionId>0L)
		{
			if (editQuestionsAllowed.containsKey(Long.valueOf(questionId)))
			{
				allowed=editQuestionsAllowed.get(Long.valueOf(questionId)).booleanValue();
			}
			else
			{
				// Get current user session Hibernate operation
				operation=getCurrentUserOperation(operation);
				
				/*
				User currentUser=userSessionService.getCurrentUser(operation);
				allowed=getEditEnabled(operation).booleanValue() && (currentUser.equals(questionAuthor) || 
					(getEditOtherUsersQuestionsEnabled(operation).booleanValue() && 
					(!isAdmin(operation,questionAuthor) || getEditAdminsQuestionsEnabled(operation).booleanValue()) && 
					(!isSuperadmin(operation,questionAuthor) || 
					getEditSuperadminsQuestionsEnabled(operation).booleanValue())));
				 */
				
				User questionAuthor=questionsService.getQuestion(operation,questionId).getCreatedBy();
				allowed=getEditEnabled(operation).booleanValue() && 
					(questionAuthor.getId()==userSessionService.getCurrentUserId() || 
					(getEditOtherUsersQuestionsEnabled(operation).booleanValue() && 
					(!isAdmin(operation,questionAuthor) || getEditAdminsQuestionsEnabled(operation).booleanValue()) && 
					(!isSuperadmin(operation,questionAuthor) || 
					getEditSuperadminsQuestionsEnabled(operation).booleanValue())));
				
				editQuestionsAllowed.put(Long.valueOf(questionId),Boolean.valueOf(allowed));
			}
		}
		return allowed;
	}
	
    public boolean isEditQuestionAllowed(Question question)
    {
    	return isEditQuestionAllowed(null,question==null?0L:question.getId());
    }
    
	private void resetCreateCopyQuestionsAllowed()
	{
		createCopyQuestionsAllowed.clear();
	}
	
	private void resetCreateCopyQuestionAllowed(Question question)
	{
		if (question!=null)
		{
			resetCreateCopyQuestionAllowed(question.getId());
		}
	}
	
	private void resetCreateCopyQuestionAllowed(long questionId)
	{
		createCopyQuestionsAllowed.remove(Long.valueOf(questionId));
	}
	
	private boolean isCreateCopyQuestionAllowed(Operation operation,long questionId)
	{
		boolean allowed=false;
		if (questionId>0L)
		{
			if (createCopyQuestionsAllowed.containsKey(Long.valueOf(questionId)))
			{
				allowed=createCopyQuestionsAllowed.get(Long.valueOf(questionId)).booleanValue();
			}
			else
			{
				// Get current user session Hibernate operation
				operation=getCurrentUserOperation(operation);
				
				/*
				User currentUser=userSessionService.getCurrentUser(operation);
				allowed=getAddEnabled(operation).booleanValue() && getCreateCopyEnabled(operation).booleanValue() && 
					(currentUser.equals(questionAuthor) || isEditQuestionAllowed(operation,questionId) || 
					(getCreateCopyOtherUsersNonEditableQuestionsEnabled(operation).booleanValue() && 
					(!isAdmin(operation,questionAuthor) || 
					getCreateCopyAdminsNonEditableQuestionsEnabled(operation).booleanValue()) && 
					(!isSuperadmin(operation,questionAuthor) || 
					getCreateCopySuperadminsNonEditableQuestionsEnabled(operation).booleanValue())));
				*/
				
				User questionAuthor=questionsService.getQuestion(operation,questionId).getCreatedBy();
				allowed=getAddEnabled(operation).booleanValue() && getCreateCopyEnabled(operation).booleanValue() && 
					(questionAuthor.getId()==userSessionService.getCurrentUserId() || 
					isEditQuestionAllowed(operation,questionId) || 
					(getCreateCopyOtherUsersNonEditableQuestionsEnabled(operation).booleanValue() && 
					(!isAdmin(operation,questionAuthor) || 
					getCreateCopyAdminsNonEditableQuestionsEnabled(operation).booleanValue()) && 
					(!isSuperadmin(operation,questionAuthor) || 
					getCreateCopySuperadminsNonEditableQuestionsEnabled(operation).booleanValue())));
				
				createCopyQuestionsAllowed.put(Long.valueOf(questionId),Boolean.valueOf(allowed));
			}
		}
		return allowed;
	}
	
    public boolean isCreateCopyQuestionAllowed(Question question)
    {
    	return isCreateCopyQuestionAllowed(null,question==null?0L:question.getId());
    }
    
	private void resetDeleteQuestionsAllowed()
	{
		deleteQuestionsAllowed.clear();
	}
	
	private void resetDeleteQuestionAllowed(Question question)
	{
		if (question!=null)
		{
			resetDeleteQuestionAllowed(question.getId());
		}
	}
	
	private void resetDeleteQuestionAllowed(long questionId)
	{
		deleteQuestionsAllowed.remove(Long.valueOf(questionId));
	}
	
	private boolean isDeleteQuestionAllowed(Operation operation,long questionId)
	{
		boolean allowed=false;
		if (questionId>0L)
		{
			if (deleteQuestionsAllowed.containsKey(Long.valueOf(questionId)))
			{
				allowed=deleteQuestionsAllowed.get(Long.valueOf(questionId)).booleanValue();
			}
			else
			{
				// Get current user session Hibernate operation
				operation=getCurrentUserOperation(operation);
				
				/*
				User currentUser=userSessionService.getCurrentUser(operation);
				allowed=getDeleteEnabled(operation).booleanValue() && (currentUser.equals(questionAuthor) || 
					(getDeleteOtherUsersQuestionsEnabled(operation).booleanValue() && 
					(!isAdmin(operation,questionAuthor) || 
					getDeleteAdminsQuestionsEnabled(operation).booleanValue()) && 
					(!isSuperadmin(operation,questionAuthor) || 
					getDeleteSuperadminsQuestionsEnabled(operation).booleanValue())));
				*/
				
				User questionAuthor=questionsService.getQuestion(operation,questionId).getCreatedBy();
				allowed=getDeleteEnabled(operation).booleanValue() && 
					(questionAuthor.getId()==userSessionService.getCurrentUserId() || 
					(getDeleteOtherUsersQuestionsEnabled(operation).booleanValue() && 
					(!isAdmin(operation,questionAuthor) || 
					getDeleteAdminsQuestionsEnabled(operation).booleanValue()) && 
					(!isSuperadmin(operation,questionAuthor) || 
					getDeleteSuperadminsQuestionsEnabled(operation).booleanValue())));
				
				deleteQuestionsAllowed.put(Long.valueOf(questionId),Boolean.valueOf(allowed));
			}
		}
		return allowed;
	}
	
    public boolean isDeleteQuestionAllowed(Question question)
    {
    	return isDeleteQuestionAllowed(null,question==null?0L:question.getId());
    }
	
    public String getSelectedQuestionTypeName()
    {
    	if (selectedQuestionTypeName==null)
    	{
    		selectedQuestionType=questionTypesService.getQuestionTypes().get(0);
    		selectedQuestionTypeName=selectedQuestionType.getName();
    	}
    	return selectedQuestionTypeName;
    }
    
    public void setSelectedQuestionTypeName(String selectedQuestionTypeName)
    {
    	if (selectedQuestionType!=null && (selectedQuestionType.getName()==null || 
    		!selectedQuestionType.getName().equals(selectedQuestionTypeName)))
    	{
    		selectedQuestionType=null;
    	}
    	this.selectedQuestionTypeName=selectedQuestionTypeName;
    }
    
	public QuestionType getSelectedQuestionType()
	{
		if (selectedQuestionType==null)
		{
			selectedQuestionType=questionTypesService.getQuestionType(selectedQuestionTypeName);
		}
		return selectedQuestionType;
	}
	
	public Question getSelectedQuestion()
	{
		return selectedQuestion;
	}
	
	public void setSelectedQuestion(Question question)
	{
		this.selectedQuestion=question;
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
    	if (getSpecialCategoryFiltersMap().containsKey(categoryId))
    	{
    		localizedCategoryFilterName=
    			localizationService.getLocalizedMessage(getSpecialCategoryFiltersMap().get(categoryId).name);
    	}
    	else if (categoryId>0L)
    	{
    		localizedCategoryFilterName=getLocalizedCategoryLongName(operation,categoryId,maxLength);
    	}
    	return localizedCategoryFilterName;
    }
    
	public List<Question> getQuestions()
	{
		return getQuestions(null);
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
					long filterCategoryId=getFilterCategoryId(operation);
					if (getSpecialCategoryFiltersMap().containsKey(Long.valueOf(filterCategoryId)))
					{
						SpecialCategoryFilter filter=
							getSpecialCategoryFiltersMap().get(Long.valueOf(filterCategoryId));
						if (getAllCategoriesSpecialCategoryFilter().equals(filter))
						{
							questions=questionsService.getAllVisibleCategoriesQuestions(
								operation,null,getFilterQuestionType(),getFilterQuestionLevel());
						}
						else if (ALL_EVEN_PRIVATE_CATEGORIES_OF_OTHER_USERS.equals(filter))
						{
							questions=questionsService.getAllCategoriesQuestions(
								operation,null,getFilterQuestionType(),getFilterQuestionType());
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
	
	public void setQuestions(List<Question> questions)
	{
		this.questions=questions;
	}
	
	public SpecialCategoryFilter getAllCategoriesSpecialCategoryFilter()
	{
		if (allCategories==null)
		{
			List<String> allPermissions=new ArrayList<String>();
			allPermissions.add("PERMISSION_QUESTIONS_GLOBAL_FILTER_ENABLED");
			allPermissions.add("PERMISSION_QUESTIONS_OTHER_USERS_FILTER_ENABLED");
			String categoryGen=localizationService.getLocalizedMessage("CATEGORY_GEN");
			if ("M".equals(categoryGen))
			{
				allCategories=new SpecialCategoryFilter(0L,"ALL_OPTIONS",allPermissions);
			}
			else
			{
				allCategories=new SpecialCategoryFilter(0L,"ALL_OPTIONS_F",allPermissions);
			}
		}
		return allCategories;
	}
	
	public Map<Long,SpecialCategoryFilter> getSpecialCategoryFiltersMap()
	{
		if (specialCategoryFiltersMap==null)
		{
			specialCategoryFiltersMap=new LinkedHashMap<Long,SpecialCategoryFilter>();
			SpecialCategoryFilter allCategories=getAllCategoriesSpecialCategoryFilter();
			specialCategoryFiltersMap.put(Long.valueOf(allCategories.id),allCategories);
			specialCategoryFiltersMap.put(Long.valueOf(ALL_EVEN_PRIVATE_CATEGORIES_OF_OTHER_USERS.id),
				ALL_EVEN_PRIVATE_CATEGORIES_OF_OTHER_USERS);
			specialCategoryFiltersMap.put(Long.valueOf(ALL_MY_CATEGORIES.id),ALL_MY_CATEGORIES);
			specialCategoryFiltersMap.put(
				Long.valueOf(ALL_MY_CATEGORIES_EXCEPT_GLOBALS.id),ALL_MY_CATEGORIES_EXCEPT_GLOBALS);
			specialCategoryFiltersMap.put(Long.valueOf(ALL_GLOBAL_CATEGORIES.id),ALL_GLOBAL_CATEGORIES);
			specialCategoryFiltersMap.put(
				Long.valueOf(ALL_PUBLIC_CATEGORIES_OF_OTHER_USERS.id),ALL_PUBLIC_CATEGORIES_OF_OTHER_USERS);
			specialCategoryFiltersMap.put(
				Long.valueOf(ALL_PRIVATE_CATEGORIES_OF_OTHER_USERS.id),ALL_PRIVATE_CATEGORIES_OF_OTHER_USERS);
			specialCategoryFiltersMap.put(
				Long.valueOf(ALL_CATEGORIES_OF_OTHER_USERS.id),ALL_CATEGORIES_OF_OTHER_USERS);
		}
		return specialCategoryFiltersMap;
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
			
			Map<String,Boolean> cachedPermissions=new HashMap<String,Boolean>();
			for (SpecialCategoryFilter specialCategoryFilter:getSpecialCategoryFiltersMap().values())
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
					Category specialCategory=new Category();
					specialCategory.setId(specialCategoryFilter.id);
					specialCategory.setName(specialCategoryFilter.name);
					specialCategoriesFilters.add(specialCategory);
				}
			}
		}
		return specialCategoriesFilters;
	}
	
	//Obtiene las categorías del usuario
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
    		questionsCategories=new ArrayList<Category>();
    		
    		// Get current user session Hibernate operation
    		operation=getCurrentUserOperation(null);
    		
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
        	// we also need to check if he/she has permission to view private categories of administrators
        	// and/or users with permission to improve permissions over their owned ones (superadmins)
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
	 * @param operation Operation
	 * @param filterCategory Filter category can be optionally passed as argument
	 * @return true if user has permissions to display questions with the current selected filter, false otherwise
	 */
    private boolean checkQuestionsFilterPermission(Operation operation,Category filterCategory)
	{
    	boolean ok=true;
    	
    	// Get current user session Hibernate operation
    	operation=getCurrentUserOperation(operation);
		
    	long filterCategoryId=getFilterCategoryId(operation);
    	if (getSpecialCategoryFiltersMap().containsKey(Long.valueOf(filterCategoryId)))
		{
			SpecialCategoryFilter filter=getSpecialCategoryFiltersMap().get(Long.valueOf(filterCategoryId));
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
				filterCategory=categoriesService.getCategory(operation,filterCategoryId);	
			}
			if (filterCategory.getVisibility().isGlobal())
			{
				// This is a global category, so we check that current user has permissions to filter
				// questions by global categories
				if (getFilterGlobalQuestionsEnabled(operation).booleanValue())
				{
					/*
					User currentUser=userSessionService.getCurrentUser(operation);
					User categoryUser=filterCategory.getUser();
					ok=currentUser.equals(categoryUser) || 
						getFilterOtherUsersQuestionsEnabled(operation).booleanValue();
					*/
					
					// Moreover we need to check that the category is owned by current user or 
					// that current user has permission to filter by categories of other users 
					ok=filterCategory.getUser().getId()==userSessionService.getCurrentUserId() || 
						getFilterOtherUsersQuestionsEnabled(operation).booleanValue();
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
					// We need to check that current user has permission to filter by categories of other users
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
							ok=getViewQuestionsFromOtherUsersPrivateCategoriesEnabled(operation).
								booleanValue() && (!isAdmin(operation,categoryUser) || 
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
    public void applyQuestionsFilter(ActionEvent event)
    {
    	// Get current user session Hibernate operation
    	Operation operation=getCurrentUserOperation(null);
    	
    	setFilterGlobalQuestionsEnabled(null);
    	setFilterOtherUsersQuestionsEnabled(null);
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
        if (checkQuestionsFilterPermission(operation,filterCategory))
        {
        	// Reload questions from DB
        	setQuestions(null);
        }
        else
        {
        	addErrorMessage(true,"INCORRECT_OPERATION","NON_AUTHORIZED_ACTION_ERROR");
        	setAddEnabled(null);
        	setEditEnabled(null);
        	setCreateCopyEnabled(null);
        	setDeleteEnabled(null);
        	resetAdmins();
        	resetSuperadmins();
        	resetEditQuestionsAllowed();
        	setEditOtherUsersQuestionsEnabled(null);
        	setEditAdminsQuestionsEnabled(null);
        	setEditSuperadminsQuestionsEnabled(null);
        	resetCreateCopyQuestionsAllowed();
        	setCreateCopyOtherUsersNonEditableQuestionsEnabled(null);
        	setCreateCopyAdminsNonEditableQuestionsEnabled(null);
        	setCreateCopySuperadminsNonEditableQuestionsEnabled(null);
        	resetDeleteQuestionsAllowed();
        	setDeleteOtherUsersQuestionsEnabled(null);
        	setDeleteAdminsQuestionsEnabled(null);
        	setDeleteSuperadminsQuestionsEnabled(null);
        }
    }
    
    //ActionListener para la confirmación de la eliminación de un recurso
	/**
	 * Action listener to confirm question deletion.
	 * @param event Action event
	 */
	public void confirm(ActionEvent event)
	{
		RequestContext rq=RequestContext.getCurrentInstance();
		rq.execute("confirmDialog.show()");
	}
	
	// Obtiene la siguiente vista en función del tipo de pregunta seleccionado
	/**
	 * Adds a new question (based on the selected question type).
	 * @return Next view
	 */
	public String addQuestion()
	{
		String newView=null;
		setAddEnabled(null);
		if (getAddEnabled(getCurrentUserOperation(null)).booleanValue())
		{
			newView=getSelectedQuestionType().getNewView();
		}
		else
		{
    		addErrorMessage(true,"INCORRECT_OPERATION","NON_AUTHORIZED_ACTION_ERROR");
			setFilterGlobalQuestionsEnabled(null);
			setFilterOtherUsersQuestionsEnabled(null);
			setEditEnabled(null);
			setCreateCopyEnabled(null);
			setDeleteEnabled(null);
			setViewOMEnabled(null);
			resetAdmins();
			resetSuperadmins();
			resetEditQuestionsAllowed();
			setEditOtherUsersQuestionsEnabled(null);
			setEditAdminsQuestionsEnabled(null);
			setEditSuperadminsQuestionsEnabled(null);
			resetCreateCopyQuestionsAllowed();
			setCreateCopyOtherUsersNonEditableQuestionsEnabled(null);
			setCreateCopyAdminsNonEditableQuestionsEnabled(null);
			setCreateCopySuperadminsNonEditableQuestionsEnabled(null);
			resetDeleteQuestionsAllowed();
			setDeleteOtherUsersQuestionsEnabled(null);
			setDeleteAdminsQuestionsEnabled(null);
			setDeleteSuperadminsQuestionsEnabled(null);
			setViewQuestionsFromOtherUsersPrivateCategoriesEnabled(null);
			setViewQuestionsFromAdminsPrivateCategoriesEnabled(null);
			setViewQuestionsFromSuperadminsPrivateCategoriesEnabled(null);
		}
		return newView;
	}
	
	// Obtiene la siguiente vista en función del tipo de pregunta seleccionado
	/**
	 * Edits a question.
	 * @param question Question to edit
	 * @return Update view (based on the question type of the selected question) 
	 */
	public String editQuestion(Question question)
	{
		String updateView=null;
		setEditEnabled(null);
		resetAdminFromQuestionAllowed(question);
		resetSuperadminFromQuestionAllowed(question);
		resetEditQuestionAllowed(question);
		setEditOtherUsersQuestionsEnabled(null);
		setEditAdminsQuestionsEnabled(null);
		setEditSuperadminsQuestionsEnabled(null);
		if (isEditQuestionAllowed(question))
		{
			updateView=questionTypesService.getQuestionType(question.getType()).getUpdateView();
		}
		else
		{
    		addErrorMessage(true,"INCORRECT_OPERATION","NON_AUTHORIZED_ACTION_ERROR");
			setFilterGlobalQuestionsEnabled(null);
			setFilterOtherUsersQuestionsEnabled(null);
			setAddEnabled(null);
			setCreateCopyEnabled(null);
			setDeleteEnabled(null);
			setViewOMEnabled(null);
			resetAdmins();
			resetSuperadmins();
			resetEditQuestionsAllowed();
			resetCreateCopyQuestionsAllowed();
			setCreateCopyOtherUsersNonEditableQuestionsEnabled(null);
			setCreateCopyAdminsNonEditableQuestionsEnabled(null);
			setCreateCopySuperadminsNonEditableQuestionsEnabled(null);
			resetDeleteQuestionsAllowed();
			setDeleteOtherUsersQuestionsEnabled(null);
			setDeleteAdminsQuestionsEnabled(null);
			setDeleteSuperadminsQuestionsEnabled(null);
			setViewQuestionsFromOtherUsersPrivateCategoriesEnabled(null);
			setViewQuestionsFromAdminsPrivateCategoriesEnabled(null);
			setViewQuestionsFromSuperadminsPrivateCategoriesEnabled(null);
		}
		return updateView;
	}
	
	/**
	 * Adds a new question copied from an existing one.
	 * @return Next view
	 */
	public String addQuestionCopy(Question question)
	{
		String newView=null;
		setAddEnabled(null);
		setEditEnabled(null);
		setCreateCopyEnabled(null);
		resetAdminFromQuestionAllowed(question);
		resetSuperadminFromQuestionAllowed(question);
		resetEditQuestionAllowed(question);
		setEditOtherUsersQuestionsEnabled(null);
		setEditAdminsQuestionsEnabled(null);
		setEditSuperadminsQuestionsEnabled(null);
		resetCreateCopyQuestionAllowed(question);
		setCreateCopyOtherUsersNonEditableQuestionsEnabled(null);
		setCreateCopyAdminsNonEditableQuestionsEnabled(null);
		setCreateCopySuperadminsNonEditableQuestionsEnabled(null);
		if (isCreateCopyQuestionAllowed(question))
		{
			newView=questionTypesService.getQuestionType(question.getType()).getNewView();
		}
		else
		{
    		addErrorMessage(true,"INCORRECT_OPERATION","NON_AUTHORIZED_ACTION_ERROR");
			setFilterGlobalQuestionsEnabled(null);
			setFilterOtherUsersQuestionsEnabled(null);
			setDeleteEnabled(null);
			setViewOMEnabled(null);
			resetAdmins();
			resetSuperadmins();
			resetEditQuestionsAllowed();
			resetCreateCopyQuestionsAllowed();
			resetDeleteQuestionsAllowed();
			setDeleteOtherUsersQuestionsEnabled(null);
			setDeleteAdminsQuestionsEnabled(null);
			setDeleteSuperadminsQuestionsEnabled(null);
			setViewQuestionsFromOtherUsersPrivateCategoriesEnabled(null);
			setViewQuestionsFromAdminsPrivateCategoriesEnabled(null);
			setViewQuestionsFromSuperadminsPrivateCategoriesEnabled(null);
		}
		return newView;
	}
	
	//ActionListener para la eliminación de una pregunta
	/**
	 * Action listener to delete a question.
	 * @param event Action event
	 */
	public void deleteQuestion(ActionEvent event)
	{
		Question question=null;
		String errorTitle="INCORRECT_OPERATION";
		String errorMessage="NON_AUTHORIZED_ACTION_ERROR";
		boolean criticalError=false;
		try
		{
			// Get current user session Hibernate operation
			Operation operation=getCurrentUserOperation(null);
			
			// Get question to delete
			question=questionsService.getQuestion(operation,getQuestionId());
			
			setDeleteEnabled(null);
			resetAdminFromQuestionAllowed(question);
			resetSuperadminFromQuestionAllowed(question);
			resetDeleteQuestionAllowed(question);
			setDeleteOtherUsersQuestionsEnabled(null);
			setDeleteAdminsQuestionsEnabled(null);
			setDeleteSuperadminsQuestionsEnabled(null);
			if (isDeleteQuestionAllowed(operation,getQuestionId()))
			{
				if (questionReleasesService.getQuestionRelease(operation,question.getId())!=null)
				{
					question=null;
					errorMessage="QUESTION_DELETE_PUBLISHED_ERROR";
				}
			}
			else
			{
				question=null;
			}
		}
		catch (ServiceException se)
		{
			question=null;
			errorTitle="UNEXPECTED_ERROR";
			errorMessage="QUESTION_DELETE_UNKNOWN_ERROR";
			criticalError=true;
			setDeleteEnabled(null);
			setDeleteOtherUsersQuestionsEnabled(null);
			setDeleteAdminsQuestionsEnabled(null);
			setDeleteSuperadminsQuestionsEnabled(null);
		}
		
		if (question==null)
		{
			addErrorMessage(criticalError,errorTitle,errorMessage);
			setFilterGlobalQuestionsEnabled(null);
			setFilterOtherUsersQuestionsEnabled(null);
			setAddEnabled(null);
			setEditEnabled(null);
			setCreateCopyEnabled(null);
			setViewOMEnabled(null);
			resetAdmins();
			resetSuperadmins();
			resetEditQuestionsAllowed();
			setEditOtherUsersQuestionsEnabled(null);
			setEditAdminsQuestionsEnabled(null);
			setEditSuperadminsQuestionsEnabled(null);
			resetCreateCopyQuestionsAllowed();
			setCreateCopyOtherUsersNonEditableQuestionsEnabled(null);
			setCreateCopyAdminsNonEditableQuestionsEnabled(null);
			setCreateCopySuperadminsNonEditableQuestionsEnabled(null);
			resetDeleteQuestionsAllowed();
			setViewQuestionsFromOtherUsersPrivateCategoriesEnabled(null);
			setViewQuestionsFromAdminsPrivateCategoriesEnabled(null);
			setViewQuestionsFromSuperadminsPrivateCategoriesEnabled(null);
		}
		else
		{
			try
			{
				// Delete question from DB
				questionsService.deleteQuestion(getQuestionId());
				
				// Get package's name
				String packageName=question.getPackage();
				
				// Get OM Developer and OM Test Navigator URLs
				String omURL=configurationService.getOmUrl();
				String omTnURL=configurationService.getOmTnUrl();
				
				// Delete <OmQuestionsFolder>/u<id_user>/q<id_question> folder
				deleteQuestionFolder(packageName);
				
				// Delete question from OM Developer and OM Test Navigator web applications
				try
				{
					QuestionGenerator.deleteQuestion(packageName,omURL,omTnURL);
				}
				catch (Exception e)
				{
					// Ignore delete error
					//TODO ¿seguir ignorando o hacer un rollback y lanzar un ServiceException?
				}
				
				// Reload questions from DB
				setQuestions(null);
			}
			catch (QuestionDeleteConstraintServiceException qdcse)
			{
				addErrorMessage(false,"INCORRECT_OPERATION","QUESTION_DELETE_CONSTRAINT_ERROR");
			}
			catch (ServiceException se)
			{
				addErrorMessage(false,"UNEXPECTED_ERROR","QUESTION_DELETE_UNKNOWN_ERROR");
			}
			finally
			{
				// End current user session Hibernate operation
				userSessionService.endCurrentUserOperation();
			}
		}
	}
	
	/**
	 * Delete &lt;OmQuestionsFolder&gt;/u&lt;id_user&gt;/q&lt;id_question&gt; folder with question.xml, 
	 * GenericQuestion.java and all resources needed to build question
	 */
	private void deleteQuestionFolder(String packageName)
	{
		StringBuffer questionFolderPath=new StringBuffer(configurationService.getOmQuestionsPath());
		questionFolderPath.append(File.separatorChar);
		questionFolderPath.append(packageName.replace('.',File.separatorChar));
		File questionFolder=new File(questionFolderPath.toString());
		if (questionFolder.exists() && questionFolder.isDirectory())
		{
			try
			{
				FileUtils.deleteDirectory(questionFolder);
			}
			catch (IOException e)
			{
				// We ignore deleting errors
			}
		}
	}
	
	//Muestra la pregunta seleccionada en el sistema OpenMark
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
		
		Question question=null;
		
		setViewOMEnabled(null);
		if (getViewOMEnabled(operation).booleanValue())
		{
			// Get question
			question=questionsService.getQuestion(operation,questionId);
		}
		if (question==null)
		{
    		addErrorMessage(true,"INCORRECT_OPERATION","NON_AUTHORIZED_ACTION_ERROR");
			setFilterGlobalQuestionsEnabled(null);
			setFilterOtherUsersQuestionsEnabled(null);
			setAddEnabled(null);
			setEditEnabled(null);
			setCreateCopyEnabled(null);
			setDeleteEnabled(null);
			resetAdmins();
			resetSuperadmins();
			resetEditQuestionsAllowed();
			setEditOtherUsersQuestionsEnabled(null);
			setEditAdminsQuestionsEnabled(null);
			setEditSuperadminsQuestionsEnabled(null);
			resetCreateCopyQuestionsAllowed();
			setCreateCopyOtherUsersNonEditableQuestionsEnabled(null);
			setCreateCopyAdminsNonEditableQuestionsEnabled(null);
			setCreateCopySuperadminsNonEditableQuestionsEnabled(null);
			resetDeleteQuestionsAllowed();
			setDeleteOtherUsersQuestionsEnabled(null);
			setDeleteAdminsQuestionsEnabled(null);
			setDeleteSuperadminsQuestionsEnabled(null);
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
	 * @param criticalError Flag to indicate if error is critical (true) or not (false)
	 * @param title Error title (before localization)
	 * @param message Error message (before localization)
	 */
	private void addErrorMessage(boolean criticalError,String title,String message)
	{
		FacesContext context=FacesContext.getCurrentInstance();
		setCriticalErrorMessage(criticalError);
		context.addMessage(null,new FacesMessage(FacesMessage.SEVERITY_ERROR,
			localizationService.getLocalizedMessage(title),localizationService.getLocalizedMessage(message)));
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
	 * This implementation localize the item 'All' of the 'Category' combo in the filter's panel because 
	 * submitting form is not enough to localize it.
	 * @param event Action event
	 */
	public void changeLocale(ActionEvent event)
	{
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
		SpecialCategoryFilter allCategories=getAllCategoriesSpecialCategoryFilter();
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
