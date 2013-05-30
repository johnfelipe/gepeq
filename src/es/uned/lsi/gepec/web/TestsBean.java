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

import org.primefaces.context.RequestContext;

import es.uned.lsi.gepec.model.entities.Category;
import es.uned.lsi.gepec.model.entities.Question;
import es.uned.lsi.gepec.model.entities.QuestionOrder;
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
import es.uned.lsi.gepec.web.services.LocalizationService;
import es.uned.lsi.gepec.web.services.PermissionsService;
import es.uned.lsi.gepec.web.services.QuestionsService;
import es.uned.lsi.gepec.web.services.ServiceException;
import es.uned.lsi.gepec.web.services.TestReleasesService;
import es.uned.lsi.gepec.web.services.TestsService;
import es.uned.lsi.gepec.web.services.UserSessionService;
import es.uned.lsi.gepec.web.services.VisibilitiesService;

//Backbean para la vista pruebas
//author Víctor Manuel Alonso Rodríguez
//since  12/2011
/**
 * Managed bean for tests.
 */
@SuppressWarnings("serial")
@ManagedBean(name="testsBean")
@ViewScoped
public class TestsBean implements Serializable
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
		allEvenPrivateCategoriesOfOtherUsersPermissions.add("PERMISSION_TESTS_GLOBAL_FILTER_ENABLED");
		allEvenPrivateCategoriesOfOtherUsersPermissions.add("PERMISSION_TESTS_OTHER_USERS_FILTER_ENABLED");
		allEvenPrivateCategoriesOfOtherUsersPermissions.add(
			"PERMISSION_TESTS_VIEW_TESTS_OF_OTHER_USERS_PRIVATE_CATEGORIES_ENABLED");
		ALL_EVEN_PRIVATE_CATEGORIES_OF_OTHER_USERS=new SpecialCategoryFilter(
			-1L,"ALL_EVEN_PRIVATE_CATEGORIES_OF_OTHER_USERS",allEvenPrivateCategoriesOfOtherUsersPermissions);
	}
	private final static SpecialCategoryFilter ALL_MY_CATEGORIES;
	static
	{
		List<String> allMyCategoriesPermissions=new ArrayList<String>();
		allMyCategoriesPermissions.add("PERMISSION_TESTS_GLOBAL_FILTER_ENABLED");
		ALL_MY_CATEGORIES=new SpecialCategoryFilter(-2L,"ALL_MY_CATEGORIES",allMyCategoriesPermissions);
	}
	private final static SpecialCategoryFilter ALL_MY_CATEGORIES_EXCEPT_GLOBALS=
		new SpecialCategoryFilter(-3L,"ALL_MY_CATEGORIES_EXCEPT_GLOBALS",new ArrayList<String>());
	private final static SpecialCategoryFilter ALL_GLOBAL_CATEGORIES;
	static
	{
		List<String> allGlobalCategoriesPermissions=new ArrayList<String>();
		allGlobalCategoriesPermissions.add("PERMISSION_TESTS_GLOBAL_FILTER_ENABLED");
		ALL_GLOBAL_CATEGORIES=
			new SpecialCategoryFilter(-4L,"ALL_GLOBAL_CATEGORIES",allGlobalCategoriesPermissions);
	}
	private final static SpecialCategoryFilter ALL_PUBLIC_CATEGORIES_OF_OTHER_USERS;
	static
	{
		List<String> allPublicCategoriesOfOtherUsersPermissions=new ArrayList<String>();
		allPublicCategoriesOfOtherUsersPermissions.add("PERMISSION_TESTS_OTHER_USERS_FILTER_ENABLED");
		ALL_PUBLIC_CATEGORIES_OF_OTHER_USERS=new SpecialCategoryFilter(
			-5L,"ALL_PUBLIC_CATEGORIES_OF_OTHER_USERS",allPublicCategoriesOfOtherUsersPermissions);
	}
	private final static SpecialCategoryFilter ALL_PRIVATE_CATEGORIES_OF_OTHER_USERS;
	static
	{
		List<String> allPrivateCategoriesOfOtherUsersPermissions=new ArrayList<String>();
		allPrivateCategoriesOfOtherUsersPermissions.add("PERMISSION_TESTS_OTHER_USERS_FILTER_ENABLED");
		allPrivateCategoriesOfOtherUsersPermissions.add(
			"PERMISSION_TESTS_VIEW_TESTS_OF_OTHER_USERS_PRIVATE_CATEGORIES_ENABLED");
		ALL_PRIVATE_CATEGORIES_OF_OTHER_USERS=new SpecialCategoryFilter(
			-6L,"ALL_PRIVATE_CATEGORIES_OF_OTHER_USERS",allPrivateCategoriesOfOtherUsersPermissions);
	}
	private final static SpecialCategoryFilter ALL_CATEGORIES_OF_OTHER_USERS;
	static
	{
		List<String> allCategoriesOfOtherUsersPermissions=new ArrayList<String>();
		allCategoriesOfOtherUsersPermissions.add("PERMISSION_TESTS_OTHER_USERS_FILTER_ENABLED");
		allCategoriesOfOtherUsersPermissions.add(
			"PERMISSION_TESTS_VIEW_TESTS_OF_OTHER_USERS_PRIVATE_CATEGORIES_ENABLED");
		ALL_CATEGORIES_OF_OTHER_USERS=
			new SpecialCategoryFilter(-7L,"ALL_CATEGORIES_OF_OTHER_USERS",allCategoriesOfOtherUsersPermissions);
	}
	
	@ManagedProperty(value="#{localizationService}")
	private LocalizationService localizationService;
	@ManagedProperty(value="#{configurationService}")
	private ConfigurationService configurationService;
	@ManagedProperty(value="#{testsService}")
	private TestsService testsService;
	@ManagedProperty(value="#{testReleasesService}")
	private TestReleasesService testReleasesService;
	@ManagedProperty(value="#{questionsService}")
	private QuestionsService questionsService;
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
	
	private List<Test> tests;										// List of tests
	private Long testId;											// Selected test's identifier
	
	private Map<Long,SpecialCategoryFilter> specialCategoryFiltersMap;
	private SpecialCategoryFilter allCategories;
	
	private List<Category> specialCategoriesFilters;				// Special categories filters list
	private List<Category> testsCategories;							// Tests categories list
	
	private long filterCategoryId;
	private boolean filterIncludeSubcategories;
	private boolean criticalErrorMessage;
	private Boolean filterGlobalTestsEnabled;
	private Boolean filterOtherUsersTestsEnabled;
	private Boolean addEnabled;
	private Boolean editEnabled;
	private Boolean createCopyEnabled;
	private Boolean deleteEnabled;
	private Boolean viewOMEnabled;
	private Boolean editOtherUsersTestsEnabled;
	private Boolean editAdminsTestsEnabled;
	private Boolean editSuperadminsTestsEnabled;
	private Boolean createCopyOtherUsersNonEditableTestsEnabled;
	private Boolean createCopyAdminsNonEditableTestsEnabled;
	private Boolean createCopySuperadminsNonEditableTestsEnabled;
	private Boolean deleteOtherUsersTestsEnabled;
	private Boolean deleteAdminsTestsEnabled;
	private Boolean deleteSuperadminsTestsEnabled;
	private Boolean viewTestsFromOtherUsersPrivateCategoriesEnabled;
	private Boolean viewTestsFromAdminsPrivateCategoriesEnabled;
	private Boolean viewTestsFromSuperadminsPrivateCategoriesEnabled;
	
	private Map<Long,Boolean> admins;
	private Map<Long,Boolean> superadmins;
	
	private Map<Long,Boolean> editTestsAllowed;
	private Map<Long,Boolean> createCopyTestsAllowed;
	private Map<Long,Boolean> deleteTestsAllowed;
	
	public TestsBean()
	{
		tests=null;
		specialCategoryFiltersMap=null;
		allCategories=null;
		specialCategoriesFilters=null;
		filterCategoryId=Long.MIN_VALUE;
		filterIncludeSubcategories=false;
		filterGlobalTestsEnabled=null;
		filterOtherUsersTestsEnabled=null;
		addEnabled=null;
		editEnabled=null;
		createCopyEnabled=null;
		deleteEnabled=null;
		viewOMEnabled=null;
		editOtherUsersTestsEnabled=null;
		editAdminsTestsEnabled=null;
		editSuperadminsTestsEnabled=null;
		createCopyOtherUsersNonEditableTestsEnabled=null;
		createCopyAdminsNonEditableTestsEnabled=null;
		createCopySuperadminsNonEditableTestsEnabled=null;
		deleteOtherUsersTestsEnabled=null;
		deleteAdminsTestsEnabled=null;
		deleteSuperadminsTestsEnabled=null;
		viewTestsFromOtherUsersPrivateCategoriesEnabled=null;
		viewTestsFromAdminsPrivateCategoriesEnabled=null;
		viewTestsFromSuperadminsPrivateCategoriesEnabled=null;
		admins=new HashMap<Long,Boolean>();
		superadmins=new HashMap<Long,Boolean>();
		editTestsAllowed=new HashMap<Long,Boolean>();
		createCopyTestsAllowed=new HashMap<Long,Boolean>();
		deleteTestsAllowed=new HashMap<Long,Boolean>();
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
	
	public void setTestReleasesService(TestReleasesService testReleasesService)
	{
		this.testReleasesService=testReleasesService;
	}
	
	public void setQuestionsService(QuestionsService testsService)
	{
		this.questionsService=testsService;
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
     * @return true if current user is allowed to navigate "Tests" page, false otherwise
     */
    public boolean isNavigationAllowed()
    {
    	return isNavigationAllowed(null);
    }
    
    /**
     * @param operation Operation
     * @return true if current user is allowed to navigate "Tests" page, false otherwise
     */
    private boolean isNavigationAllowed(Operation operation)
    {
    	return userSessionService.isGranted(getCurrentUserOperation(operation),"PERMISSION_NAVIGATION_TESTS");
    }
    
	public Long getTestId()
	{
		return testId;
	}

	public void setTestId(Long testId)
	{
		this.testId=testId;
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
	
	public boolean isCriticalErrorMessage()
	{
		return criticalErrorMessage;
	}
	
	public void setCriticalErrorMessage(boolean criticalErrorMessage)
	{
		this.criticalErrorMessage=criticalErrorMessage;
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
			createCopyEnabled=Boolean.valueOf(
				userSessionService.isGranted(getCurrentUserOperation(operation),"PERMISSION_TESTS_CREATE_COPY_ENABLED"));
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
				userSessionService.isGranted(getCurrentUserOperation(operation),"PERMISSION_TESTS_DELETE_ENABLED"));
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
				userSessionService.isGranted(getCurrentUserOperation(operation),"PERMISSION_TESTS_VIEW_OM_ENABLED"));
		}
		return viewOMEnabled;
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
	
	public Boolean getCreateCopyOtherUsersNonEditableTestsEnabled()
	{
		return getCreateCopyOtherUsersNonEditableTestsEnabled(null);
	}
	
	public void setCreateCopyOtherUsersNonEditableTestsEnabled(Boolean createCopyOtherUsersNonEditableTestsEnabled)
	{
		this.createCopyOtherUsersNonEditableTestsEnabled=createCopyOtherUsersNonEditableTestsEnabled;
	}
	
	public boolean isCreateCopyOtherUsersNonEditableTestsEnabled()
	{
		return getCreateCopyOtherUsersNonEditableTestsEnabled().booleanValue();
	}
	
	private Boolean getCreateCopyOtherUsersNonEditableTestsEnabled(Operation operation)
	{
		if (createCopyOtherUsersNonEditableTestsEnabled==null)
		{
			createCopyOtherUsersNonEditableTestsEnabled=
				Boolean.valueOf(userSessionService.isGranted(getCurrentUserOperation(operation),
				"PERMISSION_TESTS_CREATE_COPY_FROM_OTHER_USERS_NON_EDITABLE_TESTS_ENABLED"));
		}
		return createCopyOtherUsersNonEditableTestsEnabled;
	}
	
	public Boolean getCreateCopyAdminsNonEditableTestsEnabled()
	{
		return getCreateCopyAdminsNonEditableTestsEnabled(null);
	}
	
	public void setCreateCopyAdminsNonEditableTestsEnabled(Boolean createCopyAdminsNonEditableTestsEnabled)
	{
		this.createCopyAdminsNonEditableTestsEnabled=createCopyAdminsNonEditableTestsEnabled;
	}
	
	public boolean isCreateCopyAdminsNonEditableTestsEnabled()
	{
		return getCreateCopyAdminsNonEditableTestsEnabled().booleanValue();
	}
	
	private Boolean getCreateCopyAdminsNonEditableTestsEnabled(Operation operation)
	{
		if (createCopyAdminsNonEditableTestsEnabled==null)
		{
			createCopyAdminsNonEditableTestsEnabled=
				Boolean.valueOf(userSessionService.isGranted(getCurrentUserOperation(operation),
				"PERMISSION_TESTS_CREATE_COPY_FROM_ADMINS_NON_EDITABLE_TESTS_ENABLED"));
		}
		return createCopyAdminsNonEditableTestsEnabled;
	}
	
	public Boolean getCreateCopySuperadminsNonEditableTestsEnabled()
	{
		return getCreateCopySuperadminsNonEditableTestsEnabled(null);
	}
	
	public void setCreateCopySuperadminsNonEditableTestsEnabled(
		Boolean createCopySuperadminsNonEditableTestsEnabled)
	{
		this.createCopySuperadminsNonEditableTestsEnabled=createCopySuperadminsNonEditableTestsEnabled;
	}
	
	public boolean isCreateCopySuperadminsNonEditableTestsEnabled()
	{
		return getCreateCopySuperadminsNonEditableTestsEnabled().booleanValue();
	}
	
	private Boolean getCreateCopySuperadminsNonEditableTestsEnabled(Operation operation)
	{
		if (createCopySuperadminsNonEditableTestsEnabled==null)
		{
			createCopySuperadminsNonEditableTestsEnabled=
				Boolean.valueOf(userSessionService.isGranted(getCurrentUserOperation(operation),
				"PERMISSION_TESTS_CREATE_COPY_FROM_SUPERADMINS_NON_EDITABLE_TESTS_ENABLED"));
		}
		return createCopySuperadminsNonEditableTestsEnabled;
	}
	
	public Boolean getDeleteOtherUsersTestsEnabled()
	{
		return getDeleteOtherUsersTestsEnabled(null);
	}
	
	public void setDeleteOtherUsersTestsEnabled(Boolean deleteOtherUsersTestsEnabled)
	{
		this.deleteOtherUsersTestsEnabled=deleteOtherUsersTestsEnabled;
	}
	
	public boolean isDeleteOtherUsersTestsEnabled()
	{
		return getDeleteOtherUsersTestsEnabled().booleanValue();
	}
	
	private Boolean getDeleteOtherUsersTestsEnabled(Operation operation)
	{
		if (deleteOtherUsersTestsEnabled==null)
		{
			deleteOtherUsersTestsEnabled=Boolean.valueOf(userSessionService.isGranted(
				getCurrentUserOperation(operation),"PERMISSION_TESTS_DELETE_OTHER_USERS_TESTS_ENABLED"));
		}
		return deleteOtherUsersTestsEnabled;
	}
	
	public Boolean getDeleteAdminsTestsEnabled()
	{
		return getDeleteAdminsTestsEnabled(null);
	}
	
	public void setDeleteAdminsTestsEnabled(Boolean deleteAdminsTestsEnabled)
	{
		this.deleteAdminsTestsEnabled=deleteAdminsTestsEnabled;
	}
	
	public boolean isDeleteAdminsTestsEnabled()
	{
		return getDeleteAdminsTestsEnabled().booleanValue();
	}
	
	private Boolean getDeleteAdminsTestsEnabled(Operation operation)
	{
		if (deleteAdminsTestsEnabled==null)
		{
			deleteAdminsTestsEnabled=Boolean.valueOf(userSessionService.isGranted(
				getCurrentUserOperation(operation),"PERMISSION_TESTS_DELETE_ADMINS_TESTS_ENABLED"));
		}
		return deleteAdminsTestsEnabled;
	}
	
	public Boolean getDeleteSuperadminsTestsEnabled()
	{
		return getDeleteSuperadminsTestsEnabled(null);
	}
	
	public void setDeleteSuperadminsTestsEnabled(Boolean deleteSuperadminsTestsEnabled)
	{
		this.deleteSuperadminsTestsEnabled=deleteSuperadminsTestsEnabled;
	}
	
	public boolean isDeleteSuperadminsTestsEnabled()
	{
		return getDeleteSuperadminsTestsEnabled().booleanValue();
	}
	
	private Boolean getDeleteSuperadminsTestsEnabled(Operation operation)
	{
		if (deleteSuperadminsTestsEnabled==null)
		{
			deleteSuperadminsTestsEnabled=Boolean.valueOf(userSessionService.isGranted(
				getCurrentUserOperation(operation),"PERMISSION_TESTS_DELETE_SUPERADMINS_TESTS_ENABLED"));
		}
		return deleteSuperadminsTestsEnabled;
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
	
	private void resetAdminFromTestAllowed(Test test)
	{
		if (test!=null && test.getCreatedBy()!=null)
		{
			admins.remove(Long.valueOf(test.getCreatedBy().getId()));
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
	
	private void resetSuperadminFromTestAllowed(Test test)
	{
		if (test!=null && test.getCreatedBy()!=null)
		{
			superadmins.remove(Long.valueOf(test.getCreatedBy().getId()));
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
	
	private void resetEditTestsAllowed()
	{
		editTestsAllowed.clear();
	}
	
	private void resetEditTestAllowed(Test test)
	{
		if (test!=null)
		{
			resetEditTestAllowed(test.getId());
		}
	}
	
	private void resetEditTestAllowed(long testId)
	{
		editTestsAllowed.remove(Long.valueOf(testId));
	}
	
	private boolean isEditTestAllowed(Operation operation,long testId)
	{
		boolean allowed=false;
		if (testId>0L)
		{
			if (editTestsAllowed.containsKey(Long.valueOf(testId)))
			{
				allowed=editTestsAllowed.get(Long.valueOf(testId)).booleanValue();
			}
			else
			{
				// Get current user session Hibernate operation
				operation=getCurrentUserOperation(operation);
				
				User testAuthor=testsService.getTest(operation,testId).getCreatedBy();
				allowed=getEditEnabled(operation).booleanValue() && 
					(testAuthor.getId()==userSessionService.getCurrentUserId() || 
					(getEditOtherUsersTestsEnabled(operation).booleanValue() && (!isAdmin(operation,testAuthor) || 
					getEditAdminsTestsEnabled(operation).booleanValue()) && (!isSuperadmin(operation,testAuthor) || 
					getEditSuperadminsTestsEnabled(operation).booleanValue())));
				
				editTestsAllowed.put(Long.valueOf(testId),Boolean.valueOf(allowed));
			}
		}
		return allowed;
	}
	
    public boolean isEditTestAllowed(Test test)
    {
    	return isEditTestAllowed(null,test==null?0L:test.getId());
    }
	
	private void resetCreateCopyTestsAllowed()
	{
		createCopyTestsAllowed.clear();
	}
	
	private void resetCreateCopyTestAllowed(Test test)
	{
		if (test!=null)
		{
			resetCreateCopyTestAllowed(test.getId());
		}
	}
	
	private void resetCreateCopyTestAllowed(long testId)
	{
		createCopyTestsAllowed.remove(Long.valueOf(testId));
	}
	
	private boolean isCreateCopyTestAllowed(Operation operation,long testId)
	{
		boolean allowed=false;
		if (testId>0L)
		{
			if (createCopyTestsAllowed.containsKey(Long.valueOf(testId)))
			{
				allowed=createCopyTestsAllowed.get(Long.valueOf(testId)).booleanValue();
			}
			else
			{
				// Get current user session Hibernate operation
				operation=getCurrentUserOperation(operation);
				
				User testAuthor=testsService.getTest(operation,testId).getCreatedBy();
				allowed=getAddEnabled(operation).booleanValue() && getCreateCopyEnabled(operation).booleanValue() && 
					(testAuthor.getId()==userSessionService.getCurrentUserId() || 
					isEditTestAllowed(operation,testId) || 
					(getCreateCopyOtherUsersNonEditableTestsEnabled(operation).booleanValue() && 
					(!isAdmin(operation,testAuthor) || 
					getCreateCopyAdminsNonEditableTestsEnabled(operation).booleanValue()) && 
					(!isSuperadmin(operation,testAuthor) || 
					getCreateCopySuperadminsNonEditableTestsEnabled(operation).booleanValue())));
				
				createCopyTestsAllowed.put(Long.valueOf(testId),Boolean.valueOf(allowed));
			}
		}
		return allowed;
	}
	
    public boolean isCreateCopyTestAllowed(Test test)
    {
    	return isCreateCopyTestAllowed(null,test==null?0L:test.getId());
    }
    
	private void resetDeleteTestsAllowed()
	{
		deleteTestsAllowed.clear();
	}
	
	private void resetDeleteTestAllowed(Test test)
	{
		if (test!=null)
		{
			resetDeleteTestAllowed(test.getId());
		}
	}
	
	private void resetDeleteTestAllowed(long testId)
	{
		editTestsAllowed.remove(Long.valueOf(testId));
	}
	
	private boolean isDeleteTestAllowed(Operation operation,long testId)
	{
		boolean allowed=false;
		if (testId>0L)
		{
			if (deleteTestsAllowed.containsKey(Long.valueOf(testId)))
			{
				allowed=deleteTestsAllowed.get(Long.valueOf(testId)).booleanValue();
			}
			else
			{
				// Get current user session Hibernate operation
				operation=getCurrentUserOperation(operation);
				
				User testAuthor=testsService.getTest(operation,testId).getCreatedBy();
				allowed=getDeleteEnabled(operation).booleanValue() && 
					(testAuthor.getId()==userSessionService.getCurrentUserId() || 
					(getDeleteOtherUsersTestsEnabled(operation).booleanValue() && 
					(!isAdmin(operation,testAuthor) || getDeleteAdminsTestsEnabled(operation).booleanValue()) && 
					(!isSuperadmin(operation,testAuthor) || 
					getDeleteSuperadminsTestsEnabled(operation).booleanValue())));
				
				deleteTestsAllowed.put(Long.valueOf(testId),Boolean.valueOf(allowed));
			}
		}
		return allowed;
	}
	
    public boolean isDeleteTestAllowed(Test test)
    {
    	return isDeleteTestAllowed(null,test==null?0L:test.getId());
    }
	
	public List<Test> getTests()
	{
		return getTests(null);
	}
    
	public void setTests(List<Test> tests)
	{
		this.tests=tests;
	}
	
	private List<Test> getTests(Operation operation)
	{
		if (tests==null)
		{
			try
			{
				// Get current user session Hibernate operation
				operation=getCurrentUserOperation(operation);
				
				if (checkTestsFilterPermission(operation,null))
				{
					long filterCategoryId=getFilterCategoryId(operation);
					if (getSpecialCategoryFiltersMap().containsKey(Long.valueOf(filterCategoryId)))
					{
						SpecialCategoryFilter filter=
							getSpecialCategoryFiltersMap().get(Long.valueOf(filterCategoryId));
						if (getAllCategoriesSpecialCategoryFilter().equals(filter))
						{
							tests=testsService.getAllVisibleCategoriesTests(operation,null);
						}
						else if (ALL_EVEN_PRIVATE_CATEGORIES_OF_OTHER_USERS.equals(filter))
						{
							tests=testsService.getAllCategoriesTests(operation,null);
						}
						else if (ALL_MY_CATEGORIES.equals(filter))
						{
							tests=testsService.getAllMyCategoriesTests(operation,null);
						}
						else if (ALL_MY_CATEGORIES_EXCEPT_GLOBALS.equals(filter))
						{
							tests=testsService.getAllMyCategoriesExceptGlobalsTests(operation,null);
						}
						else if (ALL_GLOBAL_CATEGORIES.equals(filter))
						{
							tests=testsService.getAllGlobalCategoriesTests(operation,null);
						}
						else if (ALL_PUBLIC_CATEGORIES_OF_OTHER_USERS.equals(filter))
						{
							tests=testsService.getAllPublicCategoriesOfOtherUsersTests(operation,null);
						}
						else if (ALL_PRIVATE_CATEGORIES_OF_OTHER_USERS.equals(filter))
						{
							tests=testsService.getAllPrivateCategoriesOfOtherUsersTests(operation,null);
						}
						else if (ALL_CATEGORIES_OF_OTHER_USERS.equals(filter))
						{
							tests=testsService.getAllCategoriesOfOtherUsersTests(operation,null);
						}
					}
					else
					{
						tests=testsService.getTests(operation,null,filterCategoryId,isFilterIncludeSubcategories());
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
    
	public SpecialCategoryFilter getAllCategoriesSpecialCategoryFilter()
	{
		if (allCategories==null)
		{
			List<String> allPermissions=new ArrayList<String>();
			allPermissions.add("PERMISSION_TESTS_GLOBAL_FILTER_ENABLED");
			allPermissions.add("PERMISSION_TESTS_OTHER_USERS_FILTER_ENABLED");
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
	
	public Map<Long, SpecialCategoryFilter> getSpecialCategoryFiltersMap()
	{
		if (specialCategoryFiltersMap==null)
		{
			specialCategoryFiltersMap=new LinkedHashMap<Long,SpecialCategoryFilter>();
			SpecialCategoryFilter allCategories=getAllCategoriesSpecialCategoryFilter();
			specialCategoryFiltersMap.put(Long.valueOf(allCategories.id),allCategories);
			specialCategoryFiltersMap.put(
				Long.valueOf(ALL_EVEN_PRIVATE_CATEGORIES_OF_OTHER_USERS.id),ALL_EVEN_PRIVATE_CATEGORIES_OF_OTHER_USERS);
			specialCategoryFiltersMap.put(Long.valueOf(ALL_MY_CATEGORIES.id),ALL_MY_CATEGORIES);
			specialCategoryFiltersMap.put(
				Long.valueOf(ALL_MY_CATEGORIES_EXCEPT_GLOBALS.id),ALL_MY_CATEGORIES_EXCEPT_GLOBALS);
			specialCategoryFiltersMap.put(Long.valueOf(ALL_GLOBAL_CATEGORIES.id),ALL_GLOBAL_CATEGORIES);
			specialCategoryFiltersMap.put(
				Long.valueOf(ALL_PUBLIC_CATEGORIES_OF_OTHER_USERS.id),ALL_PUBLIC_CATEGORIES_OF_OTHER_USERS);
			specialCategoryFiltersMap.put(
				Long.valueOf(ALL_PRIVATE_CATEGORIES_OF_OTHER_USERS.id),ALL_PRIVATE_CATEGORIES_OF_OTHER_USERS);
			specialCategoryFiltersMap.put(Long.valueOf(ALL_CATEGORIES_OF_OTHER_USERS.id),ALL_CATEGORIES_OF_OTHER_USERS);
		}
		return specialCategoryFiltersMap;
	}
	
	/**
	 * @return Spectial categories used to filter other categories
	 */
	public List<Category> getSpecialCategoriesFilters()
	{
		return getSpecialCategoriesFilters(null);
	}
	
	/**
	 * @param operation Operation
	 * @return Spectial categories used to filter other categories
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
    		testsCategories=new ArrayList<Category>();
    		
    		// Get current user session Hibernate operation
    		operation=getCurrentUserOperation(operation);
        	
        	// Get filter value for viewing resources from categories of other users based on permissions
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
	 * @param operation Operation
	 * @param filterCategory Filter category can be optionally passed as argument
	 * @return true if user has permissions to display tests with the current selected filter, false otherwise
	 */
    private boolean checkTestsFilterPermission(Operation operation,Category filterCategory)
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
				// tests by global categories
				if (getFilterGlobalTestsEnabled(operation).booleanValue())
				{
					/*
					User currentUser=userSessionService.getCurrentUser(operation);
					User categoryUser=filterCategory.getUser();
					ok=currentUser.equals(categoryUser) || getFilterOtherUsersTestsEnabled(operation).booleanValue();
					*/
					
					// Moreover we need to check that the category is owned by current user or 
					// that current user has permission to filter by categories of other users 
					ok=filterCategory.getUser().getId()==userSessionService.getCurrentUserId() || 
						getFilterOtherUsersTestsEnabled(operation).booleanValue();
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
	 * Change tests to display on datatable based on filter.
     * @param event Action event
     */
    public void applyTestsFilter(ActionEvent event)
    {
    	// Get current user session Hibernate operation
    	Operation operation=getCurrentUserOperation(null);
    	
   		setFilterGlobalTestsEnabled(null);
   		setFilterOtherUsersTestsEnabled(null);
   		Category filterCategory=null;
   		long filterCategoryId=getFilterCategoryId(operation);
   		if (filterCategoryId>0L)
   		{
   			filterCategory=categoriesService.getCategory(operation,filterCategoryId);
   			resetAdminFromCategoryAllowed(filterCategory);
   			resetSuperadminFromCategoryAllowed(filterCategory);
   		}
   		setViewTestsFromOtherUsersPrivateCategoriesEnabled(null);
   		setViewTestsFromAdminsPrivateCategoriesEnabled(null);
   		setViewTestsFromSuperadminsPrivateCategoriesEnabled(null);
   		if (checkTestsFilterPermission(operation,filterCategory))
   		{
   			// Reload tests from DB
   			setTests(null);
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
   			resetEditTestsAllowed();
   			setEditOtherUsersTestsEnabled(null);
   			setEditAdminsTestsEnabled(null);
   			setEditSuperadminsTestsEnabled(null);
   			resetCreateCopyTestsAllowed();
   			setCreateCopyOtherUsersNonEditableTestsEnabled(null);
   			setCreateCopyAdminsNonEditableTestsEnabled(null);
   			setCreateCopySuperadminsNonEditableTestsEnabled(null);
   			resetDeleteTestsAllowed();
   			setDeleteOtherUsersTestsEnabled(null);
   			setDeleteAdminsTestsEnabled(null);
   			setDeleteSuperadminsTestsEnabled(null);
   		}
    }
    
	//ActionListener para la confirmación de la eliminación de una prueba
	/**
	 * Action listener to confirm test deletion.
	 * @param event Action event
	 */
	public void confirm(ActionEvent event)
	{
		RequestContext rq=RequestContext.getCurrentInstance();
		rq.execute("confirmDialog.show()");
	}
	
	/**
	 * Adds a new test.
	 * @return Next view
	 */
	public String addTest()
	{
		String newView=null;
		setAddEnabled(null);
		if (getAddEnabled(getCurrentUserOperation(null)).booleanValue())
		{
			newView="test";
		}
		else
		{
    		addErrorMessage(true,"INCORRECT_OPERATION","NON_AUTHORIZED_ACTION_ERROR");
			setFilterGlobalTestsEnabled(null);
    		setFilterOtherUsersTestsEnabled(null);
    		setEditEnabled(null);
    		setCreateCopyEnabled(null);
			setDeleteEnabled(null);
			setViewOMEnabled(null);
			resetAdmins();
			resetSuperadmins();
			resetEditTestsAllowed();
			setEditOtherUsersTestsEnabled(null);
			setEditAdminsTestsEnabled(null);
			setEditSuperadminsTestsEnabled(null);
			resetCreateCopyTestsAllowed();
			setCreateCopyOtherUsersNonEditableTestsEnabled(null);
			setCreateCopyAdminsNonEditableTestsEnabled(null);
			setCreateCopySuperadminsNonEditableTestsEnabled(null);
			resetDeleteTestsAllowed();
			setDeleteOtherUsersTestsEnabled(null);
			setDeleteAdminsTestsEnabled(null);
			setDeleteSuperadminsTestsEnabled(null);
			setViewTestsFromOtherUsersPrivateCategoriesEnabled(null);
			setViewTestsFromAdminsPrivateCategoriesEnabled(null);
			setViewTestsFromSuperadminsPrivateCategoriesEnabled(null);
		}
		return newView;
	}
	
	/**
	 * Edits a test.
	 * @param test Test to edit
	 * @return Update view 
	 */
	public String editTest(Test test)
	{
		String updateView=null;
		setEditEnabled(null);
		resetAdminFromTestAllowed(test);
		resetSuperadminFromTestAllowed(test);
		resetEditTestAllowed(test);
		setEditOtherUsersTestsEnabled(null);
		setEditAdminsTestsEnabled(null);
		setEditSuperadminsTestsEnabled(null);
		if (isEditTestAllowed(test))
		{
			updateView="testupdate";
		}
		else
		{
    		addErrorMessage(true,"INCORRECT_OPERATION","NON_AUTHORIZED_ACTION_ERROR");
			setFilterGlobalTestsEnabled(null);
			setFilterOtherUsersTestsEnabled(null);
    		setAddEnabled(null);
			setCreateCopyEnabled(null);
    		setDeleteEnabled(null);
			setViewOMEnabled(null);
			resetAdmins();
			resetSuperadmins();
			resetEditTestsAllowed();
			resetCreateCopyTestsAllowed();
			setCreateCopyOtherUsersNonEditableTestsEnabled(null);
			setCreateCopyAdminsNonEditableTestsEnabled(null);
			setCreateCopySuperadminsNonEditableTestsEnabled(null);
			resetDeleteTestsAllowed();
			setDeleteOtherUsersTestsEnabled(null);
			setDeleteAdminsTestsEnabled(null);
			setDeleteSuperadminsTestsEnabled(null);
			setViewTestsFromOtherUsersPrivateCategoriesEnabled(null);
			setViewTestsFromAdminsPrivateCategoriesEnabled(null);
			setViewTestsFromSuperadminsPrivateCategoriesEnabled(null);
		}
		return updateView;
	}
	
	/**
	 * Adds a new test copied from an existing one.
	 * @return Next view
	 */
	public String addTestCopy(Test test)
	{
		String newView=null;
		setAddEnabled(null);
		setEditEnabled(null);
		setCreateCopyEnabled(null);
		resetAdminFromTestAllowed(test);
		resetSuperadminFromTestAllowed(test);
		resetEditTestAllowed(test);
		setEditOtherUsersTestsEnabled(null);
		setEditAdminsTestsEnabled(null);
		setEditSuperadminsTestsEnabled(null);
		resetCreateCopyTestAllowed(test);
		setCreateCopyOtherUsersNonEditableTestsEnabled(null);
		setCreateCopyAdminsNonEditableTestsEnabled(null);
		setCreateCopySuperadminsNonEditableTestsEnabled(null);
		if (isCreateCopyTestAllowed(test))
		{
			newView="test";
		}
		else
		{
    		addErrorMessage(true,"INCORRECT_OPERATION","NON_AUTHORIZED_ACTION_ERROR");
			setFilterGlobalTestsEnabled(null);
    		setFilterOtherUsersTestsEnabled(null);
			setDeleteEnabled(null);
			setViewOMEnabled(null);
			resetAdmins();
			resetSuperadmins();
			resetEditTestsAllowed();
			resetCreateCopyTestsAllowed();
			resetDeleteTestsAllowed();
			setDeleteOtherUsersTestsEnabled(null);
			setDeleteAdminsTestsEnabled(null);
			setDeleteSuperadminsTestsEnabled(null);
			setViewTestsFromOtherUsersPrivateCategoriesEnabled(null);
			setViewTestsFromAdminsPrivateCategoriesEnabled(null);
			setViewTestsFromSuperadminsPrivateCategoriesEnabled(null);
		}
		return newView;
	}
	
	//ActionListener para la eliminación de una prueba
	/**
	 * Action listener to delete a test.
	 * @param event Action event
	 */
	public void deleteTest(ActionEvent event)
	{
		// Get current user session Hibernate operation
		Operation operation=getCurrentUserOperation(null);
		
		Test test=null;
		String errorTitle="INCORRECT_OPERATION";
		String errorMessage="NON_AUTHORIZED_ACTION_ERROR";
		boolean criticalError=false;
		try
		{
			// Get test to delete
			test=testsService.getTest(operation,getTestId());
			
			setDeleteEnabled(null);
			resetAdminFromTestAllowed(test);
			resetSuperadminFromTestAllowed(test);
			resetDeleteTestAllowed(test);
			setDeleteOtherUsersTestsEnabled(null);
			setDeleteAdminsTestsEnabled(null);
			setDeleteSuperadminsTestsEnabled(null);
			
			if (isDeleteTestAllowed(operation,getTestId()))
			{
				if (testReleasesService.getTestRelease(operation,test.getId())!=null)
				{
					test=null;
					errorMessage="TEST_DELETE_PUBLISHED_ERROR";
				}
			}
			else
			{
				test=null;
			}
		}
		catch (ServiceException se)
		{
			test=null;
			errorTitle="UNEXPECTED_ERROR";
			errorMessage="TEST_DELETE_UNKNOWN_ERROR";
			criticalError=true;
			setDeleteEnabled(null);
			setDeleteOtherUsersTestsEnabled(null);
			setDeleteAdminsTestsEnabled(null);
			setDeleteSuperadminsTestsEnabled(null);
		}
		
		if (test==null)
		{
			addErrorMessage(criticalError,errorTitle,errorMessage);
			setFilterGlobalTestsEnabled(null);
			setFilterOtherUsersTestsEnabled(null);
			setAddEnabled(null);
			setEditEnabled(null);
			setCreateCopyEnabled(null);
			setViewOMEnabled(null);
			resetAdmins();
			resetSuperadmins();
			resetEditTestsAllowed();
			setEditOtherUsersTestsEnabled(null);
			setEditAdminsTestsEnabled(null);
			setEditSuperadminsTestsEnabled(null);
			resetCreateCopyTestsAllowed();
			setCreateCopyOtherUsersNonEditableTestsEnabled(null);
			setCreateCopyAdminsNonEditableTestsEnabled(null);
			setCreateCopySuperadminsNonEditableTestsEnabled(null);
			resetDeleteTestsAllowed();
			setViewTestsFromOtherUsersPrivateCategoriesEnabled(null);
			setViewTestsFromAdminsPrivateCategoriesEnabled(null);
			setViewTestsFromSuperadminsPrivateCategoriesEnabled(null);
		}
		else
		{
			try
			{
				// Delete test from DB
				testsService.deleteTest(getTestId());
				
				// Get OM Test Navigator URL
				String omTnURL=configurationService.getOmTnUrl();
				
				try
				{
					// Delete test from OM Test Navigator web application
					TestGenerator.deleteTest(test,omTnURL,true);
				}
				catch (Exception e)
				{
					// Ignore OM Test Navigator errors
					//TODO ¿seguir ignorando o hacer un rollback y lanzar un ServiceException?
				}
				
				// Reload tests from BD
		    	tests=null;
			}
			catch (ServiceException se)
			{
				addErrorMessage(true,"UNEXPECTED_ERROR","TEST_DELETE_UNKNOWN_ERROR");
			}
			finally
			{
				// End current user session Hibernate operation
				userSessionService.endCurrentUserOperation();
			}
		}
	}
	
	//Muestra la prueba seleccionada en el sistema OpenMark
	/**
	 * Display test in OM Test Navigator web application.
	 * @param testId Test's identifier
	 * @return Next view
	 * @throws Exception
	 */
	public String viewOM(long testId) throws Exception
	{
		// get current user session Hibernate operation
		Operation operation=getCurrentUserOperation(null);
		
		Test test=null;
		
		setViewOMEnabled(null);
		if (getViewOMEnabled(operation).booleanValue())
		{
			// Get test
			test=testsService.getTest(operation,testId);
		}
		
		if (test==null)
		{
    		addErrorMessage(true,"INCORRECT_OPERATION","NON_AUTHORIZED_ACTION_ERROR");
			setFilterGlobalTestsEnabled(null);
			setFilterOtherUsersTestsEnabled(null);
    		setAddEnabled(null);
    		setEditEnabled(null);
    		setCreateCopyEnabled(null);
    		setDeleteEnabled(null);
    		resetAdmins();
    		resetSuperadmins();
    		resetEditTestsAllowed();
    		setEditOtherUsersTestsEnabled(null);
    		setEditAdminsTestsEnabled(null);
    		setEditSuperadminsTestsEnabled(null);
    		resetCreateCopyTestsAllowed();
    		setCreateCopyOtherUsersNonEditableTestsEnabled(null);
    		setCreateCopyAdminsNonEditableTestsEnabled(null);
    		setCreateCopySuperadminsNonEditableTestsEnabled(null);
    		resetDeleteTestsAllowed();
    		setDeleteOtherUsersTestsEnabled(null);
    		setDeleteAdminsTestsEnabled(null);
    		setDeleteSuperadminsTestsEnabled(null);
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
					long lastTimeModified=
						question.getTimemodified()==null?-1:question.getTimemodified().getTime();
					long lastTimeDeploy=
						question.getTimedeploy()==null?-1:question.getTimedeploy().getTime();
					long lastTimeBuild=question.getTimebuild()==null?-1:question.getTimebuild().getTime();
					if (lastTimeDeploy!=-1 && lastTimeDeploy>lastTimeBuild && lastTimeDeploy>lastTimeModified)
					{
						try
						{
							TestGenerator.checkQuestionJar(packageName,"1.0",omTnURL);
							buildQuestion=lastTimeDeploy!=TestGenerator.getQuestionJarLastModifiedDate(
								packageName,"1.0",omTnURL).getTime();
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
							buildQuestion=
								lastTimeBuild!=
								QuestionGenerator.getQuestionJarLastModifiedDate(packageName,omURL).getTime();
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
						question.setTimebuild(
							QuestionGenerator.getQuestionJarLastModifiedDate(packageName,omURL));
						
						// Save question
						questionsService.updateQuestion(question);
					}
				}
				
				boolean deployTest=true;
				long lastTimeModified=test.getTimeModified()==null?-1:test.getTimeModified().getTime();
				long lastTimeTestDeploy=test.getTimeTestDeploy()==null?-1:test.getTimeTestDeploy().getTime();
				long lastTimeDeployDeploy=
					test.getTimeDeployDeploy()==null?-1:test.getTimeDeployDeploy().getTime();
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
			
			// Pass parameters to javascript to display question
			RequestContext requestContext=RequestContext.getCurrentInstance();
			requestContext.addCallbackParam("url",urlParam.toString());
			requestContext.addCallbackParam("testName",testName);
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
