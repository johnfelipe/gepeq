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
import java.util.List;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.primefaces.context.RequestContext;
import org.primefaces.event.NodeCollapseEvent;
import org.primefaces.event.NodeExpandEvent;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.model.TreeNode;

import es.uned.lsi.gepec.model.entities.Category;
import es.uned.lsi.gepec.model.entities.CategoryType;
import es.uned.lsi.gepec.model.entities.User;
import es.uned.lsi.gepec.model.entities.Visibility;
import es.uned.lsi.gepec.util.HibernateUtil;
import es.uned.lsi.gepec.util.HibernateUtil.Operation;
import es.uned.lsi.gepec.util.StringUtils;
import es.uned.lsi.gepec.web.helper.CategoryTreeNode;
import es.uned.lsi.gepec.web.services.CategoriesService;
import es.uned.lsi.gepec.web.services.CategoryTypesService;
import es.uned.lsi.gepec.web.services.LocalizationService;
import es.uned.lsi.gepec.web.services.PermissionsService;
import es.uned.lsi.gepec.web.services.QuestionsService;
import es.uned.lsi.gepec.web.services.ResourcesService;
import es.uned.lsi.gepec.web.services.ServiceException;
import es.uned.lsi.gepec.web.services.TestsService;
import es.uned.lsi.gepec.web.services.UserSessionService;
import es.uned.lsi.gepec.web.services.VisibilitiesService;

/**
 * Managed bean for categories management.
 */
@SuppressWarnings("serial")
@ManagedBean(name="categoriesBean")
@ViewScoped
public class CategoriesBean implements Serializable
{
	private final static String MODE_DISPLAY="CATEGORY_DISPLAY_MODE";
	private final static String MODE_ADD="CATEGORY_ADD_MODE";
	private final static String MODE_EDIT="CATEGORY_EDIT_MODE";
	
	private final static String ADD_CHILD="CATEGORY_ADD_CHILD";
	private final static String ADD_SIBLING="CATEGORY_ADD_SIBLING";
	private final static String ADD_UNDER_ROOT="CATEGORY_ADD_UNDER_ROOT";
	
	private final static String MY_CATEGORIES_LABEL_COLUMN_CLASS="myCategoriesLabelColumn";
	private final static String MY_CATEGORIES_CHECK_COLUMN_CLASS="myCategoriesCheckColumn";
	private final static String GLOBAL_CATEGORIES_LABEL_COLUMN_CLASS="globalCategoriesLabelColumn";
	private final static String GLOBAL_CATEGORIES_CHECK_COLUMN_CLASS="globalCategoriesCheckColumn";
	private final static String OTHER_USERS_CATEGORIES_LABEL_COLUMN_CLASS="otherUsersCategoriesLabelColumn";
	private final static String OTHER_USERS_CATEGORIES_CHECK_COLUMN_CLASS="otherUsersCategoriesCheckColumn";
	private final static String OTHER_USERS_CATEGORIES_COMBO_COLUMN_CLASS="otherUsersCategoriesComboColumn";
	private final static String APPLY_FILTER_BUTTON_COLUMN_CLASS="applyFilterButtonColumn";
	
	private final static String NOT_VIEW_OTHER_USERS_CATEGORIES_CHOICE="NOT_VIEW_OTHER_USERS_CATEGORIES";
	private final static String VIEW_OTHER_USERS_PUBLIC_CATEGORIES_CHOICE="VIEW_OTHER_USERS_PUBLIC_CATEGORIES";
	private final static String VIEW_OTHER_USERS_PRIVATE_CATEGORIESS_CHOICE="VIEW_OTHER_USERS_PRIVATE_CATEGORIES";
	private final static String VIEW_OTHER_USERS_ALL_CATEGORIES_CHOICE="VIEW_OTHER_USERS_ALL_CATEGORIES";
	
	public static class DisplayOtherUsersCategoriesChoice
	{
		private String name;
		private int value;
		
		public DisplayOtherUsersCategoriesChoice(String name,int value)
		{
			this.name=name;
			this.value=value;
		}
		
		public String getName()
		{
			return this.name;
		}
		
		public int getValue()
		{
			return this.value;
		}
	}
	
	private final static List<DisplayOtherUsersCategoriesChoice> DISPLAY_OTHER_USERS_CATEGORIES_CHOICES;
	static
	{
		DISPLAY_OTHER_USERS_CATEGORIES_CHOICES=new ArrayList<DisplayOtherUsersCategoriesChoice>();
		DISPLAY_OTHER_USERS_CATEGORIES_CHOICES.add(new DisplayOtherUsersCategoriesChoice(
			NOT_VIEW_OTHER_USERS_CATEGORIES_CHOICE,CategoriesService.NOT_VIEW_OTHER_USERS_CATEGORIES));
		DISPLAY_OTHER_USERS_CATEGORIES_CHOICES.add(new DisplayOtherUsersCategoriesChoice(
			VIEW_OTHER_USERS_PUBLIC_CATEGORIES_CHOICE,CategoriesService.VIEW_OTHER_USERS_PUBLIC_CATEGORIES));
		DISPLAY_OTHER_USERS_CATEGORIES_CHOICES.add(new DisplayOtherUsersCategoriesChoice(
			VIEW_OTHER_USERS_PRIVATE_CATEGORIESS_CHOICE,CategoriesService.VIEW_OTHER_USERS_PRIVATE_CATEGORIES));
		DISPLAY_OTHER_USERS_CATEGORIES_CHOICES.add(new DisplayOtherUsersCategoriesChoice(
			VIEW_OTHER_USERS_ALL_CATEGORIES_CHOICE,CategoriesService.VIEW_OTHER_USERS_ALL_CATEGORIES));
	}
	
	@ManagedProperty(value="#{localizationService}")
	private LocalizationService localizationService;
	@ManagedProperty(value="#{categoriesService}")
	private CategoriesService categoriesService;
	@ManagedProperty(value="#{categoryTypesService}")
	private CategoryTypesService categoryTypesService;
	@ManagedProperty(value="#{visibilitiesService}")
	private VisibilitiesService visibilitiesService;
	@ManagedProperty(value="#{questionsService}")
	private QuestionsService questionsService;
	@ManagedProperty(value="#{testsService}")
	private TestsService testsService;
	@ManagedProperty(value="#{resourcesService}")
	private ResourcesService resourcesService;
	@ManagedProperty(value="#{userSessionService}")
	private UserSessionService userSessionService;
	@ManagedProperty(value="#{permissionsService}")
	private PermissionsService permissionsService;
	
	private CategoryTreeNode rootNode;					// Categories root node
	private CategoryTreeNode selectedNode;				// Selected node
	
	private Category category;
	
	private boolean displayMyCategories;
	private boolean displayGlobalCategories;
	private int displayOtherUsersCategories;
	
	private String mode;
	
	private String whereToAdd;
	
	private boolean currentDisplayMyCategories;
	private boolean currentDisplayGlobalCategories;
	private int currentDisplayOtherUsersCategories;
	
	private Boolean filterGlobalCategoriesEnabled;
	private Boolean filterOtherUsersCategoriesEnabled;
	private Boolean addModeEnabled;
	private Boolean editModeEnabled;
	private Boolean addGlobalCategoryEnabled;
	private Boolean deleteCategoryEnabled;
	private Boolean editOtherUsersCategoriesEnabled;
	private Boolean editAdminsCategoriesEnabled;
	private Boolean editSuperadminsCategoriesEnabled;
	private Boolean deleteOtherUsersCategoriesEnabled;
	private Boolean deleteAdminsCategoriesEnabled;
	private Boolean deleteSuperadminsCategoriesEnabled;
	private Boolean viewOtherUsersPrivateCategoriesEnabled;
	private Boolean viewAdminsPrivateCategoriesEnabled;
	private Boolean viewSuperadminsPrivateCategoriesEnabled;
	
	private Map<Long,Boolean> admins;
	private Map<Long,Boolean> superadmins;
	
	private Map<Long,Boolean> editCategoriesAllowed;
	private Map<Long,Boolean> deleteCategoriesAllowed;
	
	private boolean nonAuthorizedError;
	
	public CategoriesBean()
	{
		category=getEmptyCategory();
		
		currentDisplayMyCategories=displayMyCategories=true;
		currentDisplayGlobalCategories=displayGlobalCategories=false;
		currentDisplayOtherUsersCategories=displayOtherUsersCategories=CategoriesService.NOT_VIEW_OTHER_USERS_CATEGORIES;
		
		mode=MODE_DISPLAY;
		
		whereToAdd=ADD_CHILD;
		
		filterGlobalCategoriesEnabled=null;
		filterOtherUsersCategoriesEnabled=null;
		addModeEnabled=null;
		editModeEnabled=null;
		addGlobalCategoryEnabled=null;
		deleteCategoryEnabled=null;
		editOtherUsersCategoriesEnabled=null;
		editAdminsCategoriesEnabled=null;
		editSuperadminsCategoriesEnabled=null;
		deleteOtherUsersCategoriesEnabled=null;
		deleteAdminsCategoriesEnabled=null;
		deleteSuperadminsCategoriesEnabled=null;
		admins=new HashMap<Long,Boolean>();
		superadmins=new HashMap<Long,Boolean>();
		editCategoriesAllowed=new HashMap<Long,Boolean>();
		deleteCategoriesAllowed=new HashMap<Long,Boolean>();
	}
	
	public void setLocalizationService(LocalizationService localizationService)
	{
		this.localizationService=localizationService;
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
	
	public void setQuestionsService(QuestionsService questionsService)
	{
		this.questionsService=questionsService;
	}
	
	public void setTestsService(TestsService testsService)
	{
		this.testsService=testsService;
	}
	
	public void setResourcesService(ResourcesService resourcesService)
	{
		this.resourcesService=resourcesService;
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
     * @return true if current user is allowed to navigate "Categories" page, false otherwise
     */
    public boolean isNavigationAllowed()
    {
    	return isNavigationAllowed(null);
    }
    
    /**
     * @param operation Operation
     * @return true if current user is allowed to navigate "Categories" page, false otherwise
     */
    private boolean isNavigationAllowed(Operation operation)
    {
    	return userSessionService.isGranted(getCurrentUserOperation(operation),"PERMISSION_NAVIGATION_CATEGORIES");
    }
    
	public Category getCategory()
	{
		return category;
	}
	
	public void setCategory(Category category)
	{
		this.category=category;
	}
	
	public boolean isDisplayMyCategories()
	{
		return displayMyCategories;
	}
	
	public void setDisplayMyCategories(boolean displayMyCategories)
	{
		this.displayMyCategories=displayMyCategories;
	}
	
	public boolean isDisplayGlobalCategories()
	{
		return displayGlobalCategories;
	}
	
	public void setDisplayGlobalCategories(boolean displayGlobalCategories)
	{
		this.displayGlobalCategories=displayGlobalCategories;
	}
	
	public boolean isDisplayOtherUsersCategoriesCheck()
	{
		return displayOtherUsersCategories!=CategoriesService.NOT_VIEW_OTHER_USERS_CATEGORIES;
	}
	
	public void setDisplayOtherUsersCategoriesCheck(boolean displayGlobalCategoriesCheck)
	{
		displayOtherUsersCategories=displayGlobalCategoriesCheck?
			CategoriesService.VIEW_OTHER_USERS_PUBLIC_CATEGORIES:CategoriesService.NOT_VIEW_OTHER_USERS_CATEGORIES;
	}
	
	public int getDisplayOtherUsersCategoriesCombo()
	{
		return displayOtherUsersCategories;
	}
	
	public void setDisplayOtherUsersCategoriesCombo(int displayGlobalCategoriesCombo)
	{
		displayOtherUsersCategories=displayGlobalCategoriesCombo;
	}
	
	public String getMode()
	{
		return mode;
	}
	
	public void setMode(String mode)
	{
		this.mode=mode;
	}
	
	public String getWhereToAdd()
	{
		return whereToAdd;
	}
	
	public void setWhereToAdd(String whereToAdd)
	{
		this.whereToAdd=whereToAdd;
	}
	
	public Boolean getFilterGlobalCategoriesEnabled()
	{
		return getFilterGlobalCategoriesEnabled(null);
	}
	
	public void setFilterGlobalCategoriesEnabled(Boolean filterGlobalCategoriesEnabled)
	{
		this.filterGlobalCategoriesEnabled=filterGlobalCategoriesEnabled;
	}
	
	public boolean isFilterGlobalCategoriesEnabled()
	{
		return getFilterGlobalCategoriesEnabled().booleanValue();
	}
	
	private Boolean getFilterGlobalCategoriesEnabled(Operation operation)
	{
		if (filterGlobalCategoriesEnabled==null)
		{
			filterGlobalCategoriesEnabled=Boolean.valueOf(userSessionService.isGranted(
				getCurrentUserOperation(operation),"PERMISSION_CATEGORIES_GLOBAL_FILTER_ENABLED"));
		}
		return filterGlobalCategoriesEnabled;
	}
	
	public Boolean getFilterOtherUsersCategoriesEnabled()
	{
		return getFilterOtherUsersCategoriesEnabled(null);
	}
	
	public void setFilterOtherUsersCategoriesEnabled(Boolean filterOtherUsersCategoriesEnabled)
	{
		this.filterOtherUsersCategoriesEnabled=filterOtherUsersCategoriesEnabled;
	}
	
	public boolean isFilterOtherUsersCategoriesEnabled()
	{
		return getFilterOtherUsersCategoriesEnabled().booleanValue();
	}
	
	private Boolean getFilterOtherUsersCategoriesEnabled(Operation operation)
	{
		if (filterOtherUsersCategoriesEnabled==null)
		{
			filterOtherUsersCategoriesEnabled=Boolean.valueOf(userSessionService.isGranted(
				getCurrentUserOperation(operation),"PERMISSION_CATEGORIES_OTHER_USERS_FILTER_ENABLED"));
		}
		return filterOtherUsersCategoriesEnabled;
	}
	
	
	public Boolean getAddModeEnabled()
	{
		return getAddModeEnabled(null);
	}
	
	public void setAddModeEnabled(Boolean addModeEnabled)
	{
		this.addModeEnabled=addModeEnabled;
	}
	
	public boolean isAddModeEnabled()
	{
		return getAddModeEnabled().booleanValue();
	}
	
	private Boolean getAddModeEnabled(Operation operation)
	{
		if (addModeEnabled==null)
		{
			addModeEnabled=Boolean.valueOf(userSessionService.isGranted(
				getCurrentUserOperation(operation),"PERMISSION_CATEGORIES_ADD_MODE_ENABLED"));
		}
		return addModeEnabled;
	}
	
	public Boolean getEditModeEnabled()
	{
		return getEditModeEnabled(null);
	}
	
	public void setEditModeEnabled(Boolean editModeEnabled)
	{
		this.editModeEnabled=editModeEnabled;
	}
	
	public boolean isEditModeEnabled()
	{
		return getEditModeEnabled().booleanValue();
	}
	
	private Boolean getEditModeEnabled(Operation operation)
	{
		if (editModeEnabled==null)
		{
			editModeEnabled=Boolean.valueOf(userSessionService.isGranted(
				getCurrentUserOperation(operation),"PERMISSION_CATEGORIES_EDIT_MODE_ENABLED"));
		}
		return editModeEnabled;
	}
	
	public Boolean getAddGlobalCategoryEnabled()
	{
		return getAddGlobalCategoryEnabled(null);
	}
	
	public void setAddGlobalCategoryEnabled(Boolean addGlobalCategoryEnabled)
	{
		this.addGlobalCategoryEnabled=addGlobalCategoryEnabled;
	}
	
	public boolean isAddGlobalCategoryEnabled()
	{
		return getAddGlobalCategoryEnabled().booleanValue();
	}
	
	private Boolean getAddGlobalCategoryEnabled(Operation operation)
	{
		if (addGlobalCategoryEnabled==null)
		{
			addGlobalCategoryEnabled=Boolean.valueOf(userSessionService.isGranted(
				getCurrentUserOperation(operation),"PERMISSION_CATEGORIES_ADD_GLOBAL_ENABLED"));
		}
		return addGlobalCategoryEnabled;
	}
	
	public Boolean getDeleteCategoriesEnabled()
	{
		return getDeleteCategoriesEnabled(null);
	}
	
	public void setDeleteCategoriesEnabled(Boolean deleteCategoryEnabled)
	{
		this.deleteCategoryEnabled=deleteCategoryEnabled;
	}
	
	public boolean isDeleteCategoriesEnabled()
	{
		return getDeleteCategoriesEnabled().booleanValue();
	}
	
	private Boolean getDeleteCategoriesEnabled(Operation operation)
	{
		if (deleteCategoryEnabled==null)
		{
			deleteCategoryEnabled=Boolean.valueOf(
				userSessionService.isGranted(getCurrentUserOperation(operation),"PERMISSION_CATEGORIES_DELETE_ENABLED"));
		}
		return deleteCategoryEnabled;
	}
	
	public Boolean getEditOtherUsersCategoriesEnabled()
	{
		return getEditOtherUsersCategoriesEnabled(null);
	}
	
	public void setEditOtherUsersCategoriesEnabled(Boolean editOtherUsersCategoriesEnabled)
	{
		this.editOtherUsersCategoriesEnabled=editOtherUsersCategoriesEnabled;
	}
	
	public boolean isEditOtherUsersCategoriesEnabled()
	{
		return getEditOtherUsersCategoriesEnabled().booleanValue();
	}
	
	private Boolean getEditOtherUsersCategoriesEnabled(Operation operation)
	{
		if (editOtherUsersCategoriesEnabled==null)
		{
			editOtherUsersCategoriesEnabled=Boolean.valueOf(userSessionService.isGranted(
				getCurrentUserOperation(operation),"PERMISSION_CATEGORIES_EDIT_OTHER_USERS_CATEGORIES_ENABLED"));
		}
		return editOtherUsersCategoriesEnabled;
	}
	
	public Boolean getEditAdminsCategoriesEnabled()
	{
		return getEditAdminsCategoriesEnabled(null);
	}
	
	public void setEditAdminsCategoriesEnabled(Boolean editAdminsCategoriesEnabled)
	{
		this.editAdminsCategoriesEnabled=editAdminsCategoriesEnabled;
	}
	
	public boolean isEditAdminsCategoriesEnabled()
	{
		return getEditAdminsCategoriesEnabled().booleanValue();
	}
	
	private Boolean getEditAdminsCategoriesEnabled(Operation operation)
	{
		if (editAdminsCategoriesEnabled==null)
		{
			editAdminsCategoriesEnabled=Boolean.valueOf(userSessionService.isGranted(
				getCurrentUserOperation(operation),"PERMISSION_CATEGORIES_EDIT_ADMINS_CATEGORIES_ENABLED"));
		}
		return editAdminsCategoriesEnabled;
	}
	
	public Boolean getEditSuperadminsCategoriesEnabled()
	{
		return getEditSuperadminsCategoriesEnabled(null);
	}
	
	public void setEditSuperadminsCategoriesEnabled(Boolean editSuperadminsCategoriesEnabled)
	{
		this.editSuperadminsCategoriesEnabled=editSuperadminsCategoriesEnabled;
	}
	
	public boolean isEditSuperadminsCategoriesEnabled()
	{
		return getEditSuperadminsCategoriesEnabled().booleanValue();
	}
	
	private Boolean getEditSuperadminsCategoriesEnabled(Operation operation)
	{
		if (editSuperadminsCategoriesEnabled==null)
		{
			editSuperadminsCategoriesEnabled=Boolean.valueOf(userSessionService.isGranted(
				getCurrentUserOperation(operation),"PERMISSION_CATEGORIES_EDIT_SUPERADMINS_CATEGORIES_ENABLED"));
		}
		return editSuperadminsCategoriesEnabled;
	}
	
	public Boolean getDeleteOtherUsersCategoriesEnabled()
	{
		return getDeleteOtherUsersCategoriesEnabled(null);
	}
	
	public void setDeleteOtherUsersCategoriesEnabled(Boolean deleteOtherUsersCategoriesEnabled)
	{
		this.deleteOtherUsersCategoriesEnabled=deleteOtherUsersCategoriesEnabled;
	}
	
	public boolean isDeleteOtherUsersCategoriesEnabled()
	{
		return getDeleteOtherUsersCategoriesEnabled().booleanValue();
	}
	
	private Boolean getDeleteOtherUsersCategoriesEnabled(Operation operation)
	{
		if (deleteOtherUsersCategoriesEnabled==null)
		{
			deleteOtherUsersCategoriesEnabled=Boolean.valueOf(userSessionService.isGranted(
				getCurrentUserOperation(operation),"PERMISSION_CATEGORIES_DELETE_OTHER_USERS_CATEGORIES_ENABLED"));
		}
		return deleteOtherUsersCategoriesEnabled;
	}
	
	public Boolean getDeleteAdminsCategoriesEnabled()
	{
		return getDeleteAdminsCategoriesEnabled(null);
	}
	
	public void setDeleteAdminsCategoriesEnabled(Boolean deleteAdminsCategoriesEnabled)
	{
		this.deleteAdminsCategoriesEnabled=deleteAdminsCategoriesEnabled;
	}
	
	public boolean isDeleteAdminsCategoriesEnabled()
	{
		return getDeleteAdminsCategoriesEnabled().booleanValue();
	}
	
	private Boolean getDeleteAdminsCategoriesEnabled(Operation operation)
	{
		if (deleteAdminsCategoriesEnabled==null)
		{
			deleteAdminsCategoriesEnabled=Boolean.valueOf(userSessionService.isGranted(
				getCurrentUserOperation(operation),"PERMISSION_CATEGORIES_DELETE_ADMINS_CATEGORIES_ENABLED"));
		}
		return deleteAdminsCategoriesEnabled;
	}
	
	public Boolean getDeleteSuperadminsCategoriesEnabled()
	{
		return getDeleteSuperadminsCategoriesEnabled(null);
	}
	
	public void setDeleteSuperadminsCategoriesEnabled(Boolean deleteSuperadminsCategoriesEnabled)
	{
		this.deleteSuperadminsCategoriesEnabled=deleteSuperadminsCategoriesEnabled;
	}
	
	public boolean isDeleteSuperadminsCategoriesEnabled()
	{
		return getDeleteSuperadminsCategoriesEnabled().booleanValue();
	}
	
	private Boolean getDeleteSuperadminsCategoriesEnabled(Operation operation)
	{
		if (deleteSuperadminsCategoriesEnabled==null)
		{
			deleteSuperadminsCategoriesEnabled=Boolean.valueOf(userSessionService.isGranted(
				getCurrentUserOperation(operation),"PERMISSION_CATEGORIES_DELETE_SUPERADMINS_CATEGORIES_ENABLED"));
		}
		return deleteSuperadminsCategoriesEnabled;
	}
	
	public Boolean getViewOtherUsersPrivateCategoriesEnabled()
	{
		return getViewOtherUsersPrivateCategoriesEnabled(null);
	}
	
	public void setViewOtherUsersPrivateCategoriesEnabled(Boolean viewOtherUsersPrivateCategoriesEnabled)
	{
		this.viewOtherUsersPrivateCategoriesEnabled=viewOtherUsersPrivateCategoriesEnabled;
	}
	
	public boolean isViewOtherUsersPrivateCategoriesEnabled()
	{
		return getViewOtherUsersPrivateCategoriesEnabled().booleanValue();
	}
	
	private Boolean getViewOtherUsersPrivateCategoriesEnabled(Operation operation)
	{
		if (viewOtherUsersPrivateCategoriesEnabled==null)
		{
			viewOtherUsersPrivateCategoriesEnabled=Boolean.valueOf(userSessionService.isGranted(
				getCurrentUserOperation(operation),"PERMISSION_CATEGORIES_VIEW_OTHER_USERS_PRIVATE_CATEGORIES_ENABLED"));
		}
		return viewOtherUsersPrivateCategoriesEnabled;
	}
	
	public Boolean getViewAdminsPrivateCategoriesEnabled()
	{
		return getViewAdminsPrivateCategoriesEnabled(null);
	}
	
	public void setViewAdminsPrivateCategoriesEnabled(Boolean viewAdminsPrivateCategoriesEnabled)
	{
		this.viewAdminsPrivateCategoriesEnabled=viewAdminsPrivateCategoriesEnabled;
	}
	
	public boolean isViewAdminsPrivateCategoriesEnabled()
	{
		return getViewAdminsPrivateCategoriesEnabled().booleanValue();
	}
	
	private Boolean getViewAdminsPrivateCategoriesEnabled(Operation operation)
	{
		if (viewAdminsPrivateCategoriesEnabled==null)
		{
			viewAdminsPrivateCategoriesEnabled=Boolean.valueOf(userSessionService.isGranted(
				getCurrentUserOperation(operation),"PERMISSION_CATEGORIES_VIEW_ADMINS_PRIVATE_CATEGORIES_ENABLED"));
		}
		return viewAdminsPrivateCategoriesEnabled;
	}
	
	public Boolean getViewSuperadminsPrivateCategoriesEnabled()
	{
		return getViewSuperadminsPrivateCategoriesEnabled(null);
	}
	
	public void setViewSuperadminsPrivateCategoriesEnabled(Boolean viewSuperadminsPrivateCategoriesEnabled)
	{
		this.viewSuperadminsPrivateCategoriesEnabled=viewSuperadminsPrivateCategoriesEnabled;
	}
	
	public boolean isViewSuperadminsPrivateCategoriesEnabled()
	{
		return getViewSuperadminsPrivateCategoriesEnabled().booleanValue();
	}
	
	private Boolean getViewSuperadminsPrivateCategoriesEnabled(Operation operation)
	{
		if (viewSuperadminsPrivateCategoriesEnabled==null)
		{
			viewSuperadminsPrivateCategoriesEnabled=Boolean.valueOf(userSessionService.isGranted(
				getCurrentUserOperation(operation),"PERMISSION_CATEGORIES_VIEW_SUPERADMINS_PRIVATE_CATEGORIES_ENABLED"));
		}
		return viewSuperadminsPrivateCategoriesEnabled;
	}
	
	private void resetAdmins()
	{
		admins.clear();
	}
	
	private void resetAdminFromSelectedCategoryAllowed()
	{
		if (getSelectedNode()!=null && getSelectedNode()!=rootNode && getSelectedNode().getCategory()!=null && 
			getSelectedNode().getCategory().getUser()!=null)
		{
			admins.remove(Long.valueOf(getSelectedNode().getCategory().getUser().getId()));
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
	
	private void resetSuperadminFromSelectedCategoryAllowed()
	{
		if (getSelectedNode()!=null && getSelectedNode()!=rootNode && getSelectedNode().getCategory()!=null && 
			getSelectedNode().getCategory().getUser()!=null)
		{
			superadmins.remove(Long.valueOf(getSelectedNode().getCategory().getUser().getId()));
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
	
	private void resetEditCategoriesAllowed()
	{
		editCategoriesAllowed.clear();
	}
	
	private void resetEditSelectedCategoryAllowed()
	{
		if (getSelectedNode()!=null && getSelectedNode()!=rootNode && getSelectedNode().getCategory()!=null)
		{
			editCategoriesAllowed.remove(Long.valueOf(getSelectedNode().getCategory().getId()));
		}
	}
	
	private boolean isEditCategoryAllowed(long categoryId)
	{
		boolean allowed=false;
		if (categoryId>0L)
		{
			if (editCategoriesAllowed.containsKey(Long.valueOf(categoryId)))
			{
				allowed=editCategoriesAllowed.get(Long.valueOf(categoryId));
			}
			else
			{
				// Get current user session Hibernate operation
				Operation operation=getCurrentUserOperation(null);
				
				User categoryUser=getSelectedNode().getCategory().getUser();
				allowed=getEditModeEnabled(operation).booleanValue() && 
					(categoryUser.getId()==userSessionService.getCurrentUserId() || 
					(getEditOtherUsersCategoriesEnabled(operation).booleanValue() && 
					(!isAdmin(operation,categoryUser) || getEditAdminsCategoriesEnabled(operation).booleanValue()) &&
					(!isSuperadmin(operation,categoryUser) || 
					getEditSuperadminsCategoriesEnabled(operation).booleanValue())));
				
				editCategoriesAllowed.put(Long.valueOf(categoryId),Boolean.valueOf(allowed));
			}
		}
		return allowed;
	}
	
	private void resetDeleteCategoriesAllowed()
	{
		deleteCategoriesAllowed.clear();
	}
	
	private void resetDeleteSelectedCategoryAllowed()
	{
		if (getSelectedNode()!=null && getSelectedNode()!=rootNode && getSelectedNode().getCategory()!=null)
		{
			deleteCategoriesAllowed.remove(Long.valueOf(getSelectedNode().getCategory().getId()));
		}
	}
	
	private boolean isDeleteCategoryAllowed(long categoryId)
	{
		boolean allowed=false;
		if (categoryId>0L)
		{
			if (deleteCategoriesAllowed.containsKey(Long.valueOf(categoryId)))
			{
				allowed=deleteCategoriesAllowed.get(Long.valueOf(categoryId));
			}
			else
			{
				// Get current user session Hibernate operation
				Operation operation=getCurrentUserOperation(null);
				
				User categoryUser=getSelectedNode().getCategory().getUser();
				allowed=getDeleteCategoriesEnabled(operation).booleanValue() && 
					(categoryUser.getId()==userSessionService.getCurrentUserId() || 
					(getDeleteOtherUsersCategoriesEnabled(operation).booleanValue() && 
					(!isAdmin(operation,categoryUser) || 
					getDeleteAdminsCategoriesEnabled(operation).booleanValue()) && 
					(!isSuperadmin(operation,categoryUser) || 
					getDeleteSuperadminsCategoriesEnabled(operation).booleanValue())));
				
				deleteCategoriesAllowed.put(Long.valueOf(categoryId),Boolean.valueOf(allowed));
			}
		}
		return allowed;
	}
	
	public CategoryTreeNode getSelectedNode()
	{
		return selectedNode;
	}
	
	public void setSelectedNode(CategoryTreeNode selectedNode)
	{
		this.selectedNode=selectedNode;
	}
	
	public String getCategoryDescription()
	{
		return getCategoryDescription(getCategory());
	}
	
	public void setCategoryDescription(String categoryDescription)
	{
		Category category=getCategory();
		if (category!=null && !category.isDefaultCategory())
		{
			category.setDescription(categoryDescription);
		}
	}
	
	public String getCategoryDescription(Category category)
	{
		String categoryDescription="";
		if (category!=null)
		{
			if (category.isDefaultCategory())
			{
				categoryDescription=localizationService.getLocalizedMessage(category.getDescription());
			}
			else
			{
				categoryDescription=category.getDescription();
			}
		}
		return categoryDescription;
	}
	
	public String getOtherUsersCategoriesColumnClasses()
	{
		return getOtherUsersCategoriesColumnClasses(null);
	}
	
	private String getOtherUsersCategoriesColumnClasses(Operation operation)
	{
		StringBuffer otherUsersCategoriesColumnClasses=new StringBuffer();
		otherUsersCategoriesColumnClasses.append(MY_CATEGORIES_LABEL_COLUMN_CLASS);
		otherUsersCategoriesColumnClasses.append(',');
		otherUsersCategoriesColumnClasses.append(MY_CATEGORIES_CHECK_COLUMN_CLASS);
		otherUsersCategoriesColumnClasses.append(',');
		otherUsersCategoriesColumnClasses.append(GLOBAL_CATEGORIES_LABEL_COLUMN_CLASS);
		otherUsersCategoriesColumnClasses.append(',');
		otherUsersCategoriesColumnClasses.append(GLOBAL_CATEGORIES_CHECK_COLUMN_CLASS);
		otherUsersCategoriesColumnClasses.append(',');
		otherUsersCategoriesColumnClasses.append(OTHER_USERS_CATEGORIES_LABEL_COLUMN_CLASS);
		otherUsersCategoriesColumnClasses.append(',');
		if (getViewOtherUsersPrivateCategoriesEnabled(getCurrentUserOperation(operation)).booleanValue())
		{
			otherUsersCategoriesColumnClasses.append(OTHER_USERS_CATEGORIES_COMBO_COLUMN_CLASS);
		}
		else
		{
			otherUsersCategoriesColumnClasses.append(OTHER_USERS_CATEGORIES_CHECK_COLUMN_CLASS);
		}
		otherUsersCategoriesColumnClasses.append(',');
		otherUsersCategoriesColumnClasses.append(APPLY_FILTER_BUTTON_COLUMN_CLASS);
		return otherUsersCategoriesColumnClasses.toString();
	}
	
	/**
	 * @return Available display choices for categories of other users (if current user has enough permissions)
	 */
	public List<DisplayOtherUsersCategoriesChoice> getDisplayOtherUsersCategoriesChoices()
	{
		return DISPLAY_OTHER_USERS_CATEGORIES_CHOICES;
	}
	
	/**
	 * @param type Category type string
	 * @return Category type's icon
	 */
	public String getCategoryTypeIcon(String type)
	{
		return getCategoryTypeIcon(null,type);
	}
	
	/**
	 * @param operation Operation
	 * @param type Category type string
	 * @return Category type's icon
	 */
	private String getCategoryTypeIcon(Operation operation,String type)
	{
		return categoryTypesService.getCategoryType(getCurrentUserOperation(operation),type).getIcon();
	}
	
	/**
	 * Add derived category types of the category type indicated to the list of strings of category types.
	 * @param types All available category types
	 * @param type Category type
	 * @param categoryTypes List of strings of category types
	 */
	private void addDerivedCategoryTypes(List<CategoryType> types,CategoryType type,List<String> categoryTypes)
	{
		for (CategoryType t:types)
		{
			if ((t.getParent()==null && type==null) || (t.getParent()!=null && t.getParent().equals(type)))
			{
				categoryTypes.add(t.getType());
				addDerivedCategoryTypes(types,t,categoryTypes);
			}
		}
	}
	
	/**
	 * @return List of of strings of appropiated category types for edit/add a category 
	 */
	public List<String> getCategoryTypes()
	{
		return getCategoryTypes(null);
	}
	
	/**
	 * @param operation Operation
	 * @return List of of strings of appropiated category types for edit/add a category 
	 */
	private List<String> getCategoryTypes(Operation operation)
	{
		List<String> categoryTypes=new ArrayList<String>();
		List<CategoryType> types=null;
		CategoryType filterType=null;
		if (MODE_EDIT.equals(getMode()))
		{
			// Get current user session Hibernate operation
			operation=getCurrentUserOperation(operation);
			
			types=categoryTypesService.getCategoryTypes(operation);
			if (getSelectedNode()!=null && getSelectedNode()!=rootNode)
			{
				if (getSelectedNode().getParent()!=null && getSelectedNode().getParent()!=rootNode)
				{
					filterType=((CategoryTreeNode)getSelectedNode().getParent()).getCategory().getCategoryType();
				}
				else if (currentDisplayOtherUsersCategories==CategoriesService.VIEW_OTHER_USERS_PRIVATE_CATEGORIES && 
					getSelectedNode().getCategory()!=null && !getSelectedNode().getCategory().isDefaultCategory() &&
					getSelectedNode().getCategory().getVisibility()!=null && 
					!getSelectedNode().getCategory().getVisibility().isGlobal() && 
					getSelectedNode().getCategory().getParent()!=null)
				{
					filterType=categoriesService.getCategory(
						operation,getSelectedNode().getCategory().getParent().getId()).getCategoryType();
				}
				if (filterType!=null)
				{
					categoryTypes.add(filterType.getType());
				}
				addDerivedCategoryTypes(types,filterType,categoryTypes);
				for (TreeNode child:getSelectedNode().getChildren())
				{
					if (categoryTypes.size()<=1)
					{
						break;
					}
					CategoryType childType=((CategoryTreeNode)child).getCategory().getCategoryType();
					List<String> categoryTypesToRemove=new ArrayList<String>();
					addDerivedCategoryTypes(types,childType.getParent(),categoryTypesToRemove);
					List<String> childDerivedTypes=new ArrayList<String>();
					childDerivedTypes.add(childType.getType());
					addDerivedCategoryTypes(types,childType,childDerivedTypes);
					for (String childDerivedType:childDerivedTypes)
					{
						categoryTypesToRemove.remove(childDerivedType);
					}
					for (String categoryTypeToRemove:categoryTypesToRemove)
					{
						categoryTypes.remove(categoryTypeToRemove);
					}
				}
			}
		}
		else if (MODE_ADD.equals(getMode()))
		{
			boolean ok=false;
			types=categoryTypesService.getCategoryTypes(getCurrentUserOperation(null));
			if (getWhereToAdd().equals(ADD_CHILD) && getSelectedNode()!=null && getSelectedNode()!=rootNode)
			{
				filterType=getSelectedNode().getCategory().getCategoryType();
				ok=true;
			}
			else if (getWhereToAdd().equals(ADD_SIBLING) && getSelectedNode()!=null)
			{
				CategoryTreeNode parentNode=
					getSelectedNode().getParent()==null?null:(CategoryTreeNode)getSelectedNode().getParent();
				if (parentNode!=null && parentNode!=rootNode)
				{
					filterType=parentNode.getCategory().getCategoryType();
				}
				ok=true;
			}
			else if (getWhereToAdd().equals(ADD_UNDER_ROOT))
			{
				ok=true;
			}
			if (ok)
			{
				if (filterType!=null)
				{
					categoryTypes.add(filterType.getType());
				}
				addDerivedCategoryTypes(types,filterType,categoryTypes);
			}
		}
		return categoryTypes;
	}
	
	public List<String> getWhereToAddOptions()
	{
		List<String> whereToAddOptions=new ArrayList<String>();
		whereToAddOptions.add(ADD_CHILD);
		whereToAddOptions.add(ADD_SIBLING);
		if (currentDisplayGlobalCategories)
		{
			whereToAddOptions.add(ADD_UNDER_ROOT);
		}
		return whereToAddOptions;
	}
	
	public String getCategoryType()
	{
		return (getCategory()==null || getCategory().getCategoryType()==null || getSelectedNode()==null || 
			getSelectedNode().equals(rootNode))?null:getCategory().getCategoryType().getType();
	}
	
	public void setCategoryType(String categoryType)
	{
		setCategoryType(null,categoryType);
	}
	
	private void setCategoryType(Operation operation,String categoryType)
	{
		if (getCategory()!=null)
		{
			getCategory().setCategoryType(
				categoryTypesService.getCategoryType(getCurrentUserOperation(operation),categoryType));
		}
	}
	
	/**
	 * @return List of of strings of appropiated category visibilities for edit/add a category 
	 */
	public List<String> getCategoryVisibilities()
	{
		return getCategoryVisibilities(null);
	}
	
	/**
	 * @param operation Operation
	 * @return List of of strings of appropiated category visibilities for edit/add a category 
	 */
	private List<String> getCategoryVisibilities(Operation operation)
	{
		List<String> categoryVisibilities=new ArrayList<String>();
		
		List<Visibility> visibilities=null;
		Visibility filterVisibility=null;
		if (MODE_EDIT.equals(getMode()))
		{
			if (getSelectedNode()!=null && getSelectedNode()!=rootNode)
			{
				// Get current user session Hibernate operation
				operation=getCurrentUserOperation(operation);
				
				List<Visibility> visibilitiesToRemove=new ArrayList<Visibility>();
				visibilities=visibilitiesService.getVisibilities(operation);
				if (getSelectedNode().getCategory().isDefaultCategory())
				{
					for (Visibility visibility:visibilities)
					{
						if (visibility.isGlobal())
						{
							visibilitiesToRemove.add(visibility);
						}
					}
				}
				else if ((getSelectedNode().getParent()==null || getSelectedNode().getParent()==rootNode) && 
					(currentDisplayOtherUsersCategories!=CategoriesService.VIEW_OTHER_USERS_PRIVATE_CATEGORIES || 
					getSelectedNode().getCategory().getVisibility().isGlobal()))
				{
					for (Visibility visibility:visibilities)
					{
						if (!visibility.isGlobal())
						{
							visibilitiesToRemove.add(visibility);
						}
					}
				}
				else if ((getSelectedNode().getParent()==null || getSelectedNode().getParent()==rootNode) &&
					(currentDisplayOtherUsersCategories==CategoriesService.VIEW_OTHER_USERS_PRIVATE_CATEGORIES && 
					!getSelectedNode().getCategory().getVisibility().isGlobal()))
				{
					filterVisibility=categoriesService.getCategory(
						operation,getSelectedNode().getCategory().getParent().getId()).getVisibility();
					for (Visibility visibility:visibilities)
					{
						if (visibility.isGlobal()!=filterVisibility.isGlobal() || 
							visibility.getLevel()<filterVisibility.getLevel())
						{
							visibilitiesToRemove.add(visibility);
						}
					}
				}
				else
				{
					filterVisibility=((CategoryTreeNode)getSelectedNode().getParent()).getCategory().getVisibility();
					for (Visibility visibility:visibilities)
					{
						if (visibility.isGlobal()!=filterVisibility.isGlobal() || 
							visibility.getLevel()<filterVisibility.getLevel())
						{
							visibilitiesToRemove.add(visibility);
						}
					}
				}
				for (Visibility visibilityToRemove:visibilitiesToRemove)
				{
					visibilities.remove(visibilityToRemove);
				}
				for (TreeNode child:getSelectedNode().getChildren())
				{
					if (visibilities.size()<=1)
					{
						break;
					}
					Visibility childVisibility=((CategoryTreeNode)child).getCategory().getVisibility();
					visibilitiesToRemove.clear();
					for (Visibility visibility:visibilities)
					{
						if (visibility.getLevel()>childVisibility.getLevel())
						{
							visibilitiesToRemove.add(visibility);
						}
					}
					for (Visibility visibilityToRemove:visibilitiesToRemove)
					{
						visibilities.remove(visibilityToRemove);
					}
				}
				for (Visibility visibility:visibilities)
				{
					categoryVisibilities.add(visibility.getVisibility());
				}
			}
		}
		else if (MODE_ADD.equals(getMode()))
		{
			visibilities=visibilitiesService.getVisibilities(getCurrentUserOperation(operation));
			if (getWhereToAdd().equals(ADD_CHILD) && getSelectedNode()!=null && getSelectedNode()!=rootNode)
			{
				filterVisibility=getSelectedNode().getCategory().getVisibility();
			}
			else if (getWhereToAdd().equals(ADD_SIBLING) && getSelectedNode()!=null)
			{
				CategoryTreeNode parentNode=
					getSelectedNode().getParent()==null?null:(CategoryTreeNode)getSelectedNode().getParent();
				if (parentNode==null || parentNode==rootNode)
				{
					for (Visibility visibility:visibilities)
					{
						if (visibility.isGlobal() && 
							(filterVisibility==null || visibility.getLevel()<filterVisibility.getLevel()))
						{
							filterVisibility=visibility;
						}
					}
				}
				else
				{
					filterVisibility=parentNode.getCategory().getVisibility();
				}
			}
			else if (getWhereToAdd().equals(ADD_UNDER_ROOT))
			{
				for (Visibility visibility:visibilities)
				{
					if (visibility.isGlobal() && 
						(filterVisibility==null || visibility.getLevel()<filterVisibility.getLevel()))
					{
						filterVisibility=visibility;
					}
				}
			}
			if (filterVisibility!=null)
			{
				for (Visibility visibility:visibilities)
				{
					if (visibility.isGlobal()==filterVisibility.isGlobal() && 
						visibility.getLevel()>=filterVisibility.getLevel())
					{
						categoryVisibilities.add(visibility.getVisibility());
					}
				}
			}
		}
		return categoryVisibilities;
	}
	
	public String getCategoryVisibility()
	{
		return (getCategory()==null || getCategory().getVisibility()==null || getSelectedNode()==null || 
			getSelectedNode().equals(rootNode))?null:getCategory().getVisibility().getVisibility();
	}
	
	public void setCategoryVisibility(String categoryVisibility)
	{
		setCategoryVisibility(null,categoryVisibility);
	}
	
	private void setCategoryVisibility(Operation operation,String categoryVisibility)
	{
		if (getCategory()!=null)
		{
			getCategory().setVisibility(
				visibilitiesService.getVisibility(getCurrentUserOperation(operation),categoryVisibility));
		}
	}
	
	public String getCategoryDisplayName()
	{
		return getCategoryDisplayName(getCategory());
	}
	
	public void setCategoryDisplayName(String categoryDisplayName)
	{
		if (getCategory()!=null)
		{
			getCategory().setName(categoryDisplayName);
		}
	}
	
	/**
	 * @param category Category
	 * @return Category name for displaying (localized if needed)
	 */
	public String getCategoryDisplayName(Category category)
	{
		StringBuffer categoryDisplayName=new StringBuffer();
		if (category!=null && category.getUser()!=null)
		{
			if (category.isDefaultCategory())
			{
				if (category.getUser().getId()==userSessionService.getCurrentUserId())
				{
					categoryDisplayName.append(localizationService.getLocalizedMessage("MY_CATEGORIES"));
				}
				else
				{
					categoryDisplayName.append(localizationService.getLocalizedMessage("CATEGORIES_OF"));
					categoryDisplayName.append(' ');
					categoryDisplayName.append(category.getUser().getNick());
				}
			}
			else
			{
				categoryDisplayName.append(category.getName());
			}
		}
		return categoryDisplayName.toString();
	}
	
	/**
	 * @param category Category
	 * @return Category name for displaying in tree node (localized if needed)
	 */
	public String getCategoryNodeName(Category category)
	{
		return getCategoryNodeName(null,category);
	}
	
	/**
	 * @param operation Operation
	 * @param category Category
	 * @return Category name for displaying in tree node (localized if needed)
	 */
	private String getCategoryNodeName(Operation operation,Category category)
	{
		String categoryNodeName=null;
		// Names of private categories of other users under the root node of the categories tree will 
		// be displayed the long way (all path of parent categories names)
		if (currentDisplayOtherUsersCategories==CategoriesService.VIEW_OTHER_USERS_PRIVATE_CATEGORIES && 
			category!=null && !category.isDefaultCategory() && category.getVisibility()!=null && 
			!category.getVisibility().isGlobal() && findCategoryNode(rootNode,category).getParent()==rootNode)
		{
			// We need to check that category owner is not current user
			if (userSessionService.getCurrentUserId()!=category.getUser().getId())
			{
				categoryNodeName=
					categoriesService.getLocalizedCategoryLongName(getCurrentUserOperation(operation),category.getId());
			}
		}
		
		// Names of rest of categories will be displayed the same way as they are displayed 
		// in the category input field
		if (categoryNodeName==null)
		{
			categoryNodeName=getCategoryDisplayName(category);
		}
		return categoryNodeName;
	}
	
	public String getCategoryUser()
	{
		return (getCategory()==null || getCategory().getUser()==null)?"":getCategory().getUser().getNick();
	}
	
	public boolean isEnabledCategoryName()
	{
		return MODE_ADD.equals(getMode()) || (MODE_EDIT.equals(getMode()) && getSelectedNode()!=null && 
			getSelectedNode()!=rootNode && getSelectedNode().getCategory()!=null && 
			!getSelectedNode().getCategory().isDefaultCategory() && 
			isEditCategoryAllowed(getSelectedNode().getCategory().getId()));
	}
	
	public boolean isEnabledCategoryUser()
	{
		return MODE_DISPLAY.equals(getMode()) || MODE_EDIT.equals(getMode());
	}
	
	public boolean isEnabledCategoryType()
	{
		return (MODE_ADD.equals(getMode()) && (ADD_UNDER_ROOT.equals(getWhereToAdd()) || getSelectedNode()!=null && 
			getSelectedNode()!=rootNode)) || (MODE_EDIT.equals(getMode()) && getSelectedNode()!=null && 
			getSelectedNode()!=rootNode && getSelectedNode().getCategory()!=null && 
			!getSelectedNode().getCategory().isDefaultCategory() && 
			isEditCategoryAllowed(getSelectedNode().getCategory().getId()));
	}
	
	public boolean isEnabledCategoryVisibility()
	{
		return (MODE_ADD.equals(getMode()) && (ADD_UNDER_ROOT.equals(getWhereToAdd()) || getSelectedNode()!=null && 
			getSelectedNode()!=rootNode)) || (MODE_EDIT.equals(getMode()) && getSelectedNode()!=null && 
			getSelectedNode()!=rootNode && getSelectedNode().getCategory()!=null && 
			isEditCategoryAllowed(getSelectedNode().getCategory().getId()));
	}
	
	public boolean isEnabledCategoryDescription()
	{
		return MODE_ADD.equals(getMode()) || (MODE_EDIT.equals(getMode()) && getSelectedNode()!=null && 
			getSelectedNode()!=rootNode && getSelectedNode().getCategory()!=null  && 
			!getSelectedNode().getCategory().isDefaultCategory() && 
			isEditCategoryAllowed(getSelectedNode().getCategory().getId()));
	}
	
	public boolean isEnabledAddMode()
	{
		return isEnabledAddMode(null);
	}
	
	private boolean isEnabledAddMode(Operation operation)
	{
		return (currentDisplayMyCategories || currentDisplayGlobalCategories) && 
			getAddModeEnabled(getCurrentUserOperation(operation)).booleanValue();
	}
	
	public boolean isEnabledEditMode()
	{
		return isEnabledEditMode(null);
	}
	
	private boolean isEnabledEditMode(Operation operation)
	{
		// Get current user session Hibernate operation
		operation=getCurrentUserOperation(operation);
		
		return (currentDisplayMyCategories || currentDisplayGlobalCategories || 
			(currentDisplayOtherUsersCategories!=CategoriesService.NOT_VIEW_OTHER_USERS_CATEGORIES && 
			getViewOtherUsersPrivateCategoriesEnabled(operation).booleanValue())) && 
			getEditModeEnabled(operation).booleanValue();
	}
	
	public boolean isEnabledAdd()
	{
		return isEnabledAdd(null);
	}
	
	private boolean isEnabledAdd(Operation operation)
	{
		boolean enabledAdd=false;
		
		// Get current user session Hibernate operation
		operation=getCurrentUserOperation(operation);
		
		nonAuthorizedError=false;
		if (getAddModeEnabled(operation).booleanValue())
		{
			//User user=userSessionService.getCurrentUser(getCurrentUserOperation(null));
			long userId=userSessionService.getCurrentUserId();
			
			if ("CATEGORY_VISIBILITY_GLOBAL".equals(getCategoryVisibility()) && 
				!getAddGlobalCategoryEnabled(operation).booleanValue())
			{
				enabledAdd=false;
				nonAuthorizedError=true;
			}
			else if (ADD_CHILD.equals(getWhereToAdd()))
			{
				enabledAdd=getSelectedNode()!=null && getSelectedNode()!=rootNode && 
					getSelectedNode().getCategory().getUser().getId()==userId && getCategory()!=null && 
					getCategory().getName()!=null && !getCategory().getName().equals("");
				/*
				enabledAdd=selectedNode!=null && selectedNode!=rootNode && 
					user.equals(selectedNode.getCategory().getUser()) && category!=null && 
					category.getName()!=null && !category.getName().equals("");
				*/
			}
			else if (ADD_SIBLING.equals(getWhereToAdd()))
			{
				if (getSelectedNode()!=null && getSelectedNode()!=rootNode && getCategory()!=null && 
					getCategory().getName()!=null && !getCategory().getName().equals(""))
				{
					CategoryTreeNode parentNode=(CategoryTreeNode)getSelectedNode().getParent();
					if (parentNode==null || parentNode==rootNode)
					{
						enabledAdd=
							currentDisplayGlobalCategories && getAddGlobalCategoryEnabled(operation).booleanValue();
						nonAuthorizedError=!enabledAdd;
					}
					else
					{
						enabledAdd=parentNode.getCategory().getUser().getId()==userId;
						//enabledAdd=user.equals(parentNode.getCategory().getUser());
					}
				}
			}
			else if (ADD_UNDER_ROOT.equals(getWhereToAdd()))
			{
				nonAuthorizedError=!getAddGlobalCategoryEnabled(operation).booleanValue();
				enabledAdd=!nonAuthorizedError && getCategory()!=null && getCategory().getName()!=null && 
					!getCategory().getName().equals("") && currentDisplayGlobalCategories;
			}
		}
		return enabledAdd;
	}
	
	public boolean isEnabledUpdate()
	{
		boolean ok=getSelectedNode()!=null && getSelectedNode()!=rootNode;
		if (ok)
		{
			Category selectedCategory=getSelectedNode().getCategory();
			nonAuthorizedError=!isEditCategoryAllowed(selectedCategory.getId());
			ok=!nonAuthorizedError && getCategory()!=null && getCategory().getName()!=null && 
				!getCategory().getName().equals("");
		}
		return ok;
	}
	
	public boolean isEnabledDelete()
	{
		boolean ok=getSelectedNode()!=null && getSelectedNode()!=rootNode;
		if (ok)
		{
			Category selectedCategory=getSelectedNode().getCategory();
			nonAuthorizedError=!isDeleteCategoryAllowed(selectedCategory.getId());
			ok=!nonAuthorizedError && !selectedCategory.isDefaultCategory() && getSelectedNode().isLeaf();
		}
		return ok;
	}
	
	public String getDisplayModeButtonStyleClass()
	{
		return MODE_DISPLAY.equals(getMode())?"selectedButton":"button";
	}
	
	public String getAddModeButtonStyleClass()
	{
		return MODE_ADD.equals(getMode())?"selectedButton":"button";
	}
	
	public String getEditModeButtonStyleClass()
	{
		return MODE_EDIT.equals(getMode())?"selectedButton":"button";
	}
	
	public CategoryTreeNode getRootNode()
	{
		return getRootNode(null);
	}
	
	private CategoryTreeNode getRootNode(Operation operation)
	{
		if (rootNode==null)		// If there is no root node we need to generate tree
		{				
			rootNode=generateTree(getCurrentUserOperation(operation),currentDisplayMyCategories,
				currentDisplayGlobalCategories,currentDisplayOtherUsersCategories);
			rootNode.setSelected(true);
			setSelectedNode(rootNode);
		}
		return rootNode;
	}
	
	/**
	 * @param categories List of categories
	 * @param user User
	 * @return User default category
	 */
	private Category getUserDefaultCategory(List<Category> categories,User user)
	{
		Category defaultCategory=null;
		if (user!=null)
		{
			for (Category category:categories)
			{
				if (category.isDefaultCategory() && category.getParent()==null && 
					user.equals(category.getUser()))
				{
					defaultCategory=category;
					break;
				}
			}
		}
		return defaultCategory;
	}
	
	/**
	 * @param node Tree node from we do the search
	 * @param cat Category to search
	 * @return Category if it is found, null otherwise
	 */
	private CategoryTreeNode findCategoryNode(CategoryTreeNode node,Category cat)
	{
		CategoryTreeNode categoryNode=null;
		if (cat!=null)
		{
			Category cn=node.getCategory();
			if (cat.equals(cn))
			{
				categoryNode=node;
			}
			else
			{
				for (TreeNode childNode:node.getChildren())
				{
					categoryNode=findCategoryNode((CategoryTreeNode)childNode,cat);
					if (categoryNode!=null)
					{
						break;
					}
				}
			}
		}
		return categoryNode;
	}
	
	/**
	 * @param node Node to check
	 * @param from Node from we check if node derives
	 * @return true if node derives from indicated node, false otherwise
	 */
	private boolean isDerivedFrom(CategoryTreeNode node,CategoryTreeNode from)
	{
		return node==null?
			from==null:
			node.equals(from)?true:isDerivedFrom((CategoryTreeNode)node.getParent(),from);
	}
	
	/**
	 * @param operation Operation
	 * @param category Category
	 * @return true if the category is displayable with current user permissions, false otherwise
	 */
	private boolean isCategoryDisplayableByCurrentUser(Operation operation,Category category)
	{
		boolean displayable=category!=null;
		if (displayable)
		{
			// For now we consider that the category is not displayable
			displayable=false;
			
			// Get current user id
			long currentUserId=userSessionService.getCurrentUserId();
			
			// Check if category is displayable because we are filtering by categories of current user
			if (currentDisplayMyCategories)
			{
				displayable=category.getUser().getId()==currentUserId && !category.getVisibility().isGlobal();
			}
			
			// Check if category is displayable because we are filtering by global categories
			if (!displayable && currentDisplayGlobalCategories)
			{
				displayable=category.getVisibility().isGlobal();
			}
			
			// Check if category is displayable because we are filtering by categories of other users
			if (!displayable && currentDisplayOtherUsersCategories!=CategoriesService.NOT_VIEW_OTHER_USERS_CATEGORIES)
			{
				displayable=category.getUser().getId()!=currentUserId && !category.getVisibility().isGlobal();
				if (displayable)
				{
					// Get current user session Hibernate operation
					operation=getCurrentUserOperation(operation);
					
					// Check if category is displayable because we are filtering by public categories 
					// of other users
					if (currentDisplayOtherUsersCategories==CategoriesService.VIEW_OTHER_USERS_PUBLIC_CATEGORIES)
					{
						Visibility publicVisibility=
							visibilitiesService.getVisibility(operation,"CATEGORY_VISIBILITY_PUBLIC");
						displayable=category.getVisibility().getLevel()<=publicVisibility.getLevel();
					}
					// Check if category is displayable because we are filtering by private categories of other users
					else if (currentDisplayOtherUsersCategories==CategoriesService.VIEW_OTHER_USERS_PRIVATE_CATEGORIES)
					{
						Visibility privateVisibility=
							visibilitiesService.getVisibility(operation,"CATEGORY_VISIBILITY_PRIVATE");
						User categoryUser=category.getUser();
						displayable=category.getVisibility().getLevel()>=privateVisibility.getLevel() &&
							(getViewOtherUsersPrivateCategoriesEnabled(operation).booleanValue() &&
							(!isAdmin(operation,categoryUser) || 
							getViewAdminsPrivateCategoriesEnabled(operation).booleanValue()) &&
							(!isSuperadmin(operation,categoryUser) || 
							getViewSuperadminsPrivateCategoriesEnabled(operation).booleanValue()));
					}
					// Check if category is displayable because we are filtering by all categories of other users
					else if (currentDisplayOtherUsersCategories==CategoriesService.VIEW_OTHER_USERS_ALL_CATEGORIES)
					{
						Visibility publicVisibility=
							visibilitiesService.getVisibility(operation,"CATEGORY_VISIBILITY_PUBLIC");
						displayable=category.getVisibility().getLevel()<=publicVisibility.getLevel();
						if (!displayable)
						{
							Visibility privateVisibility=
								visibilitiesService.getVisibility(operation,"CATEGORY_VISIBILITY_PRIVATE");
							User categoryUser=category.getUser();
							displayable=category.getVisibility().getLevel()>=privateVisibility.getLevel() &&
								(getViewOtherUsersPrivateCategoriesEnabled(operation).booleanValue() &&
								(!isAdmin(operation,categoryUser) || 
								getViewAdminsPrivateCategoriesEnabled(operation).booleanValue()) &&
								(!isSuperadmin(operation,categoryUser) || 
								getViewSuperadminsPrivateCategoriesEnabled(operation).booleanValue()));
						}
					}
				}
			}
		}
		return displayable;
	}
	
	/**
	 * Generates categories tree.
	 * @param operation Operation
	 * @param includeUserCategories Flag to indicate if we want to include user categories in the 
	 * categories tree 
	 * @param includeGlobalCategories Flag to indicate if we want to include global categories in the 
	 * categories tree 
	 * @param includeOtherUserCategories Value indicating if we want to include public categories of 
	 * other users in the categories tree
	 */
	private CategoryTreeNode generateTree(Operation operation,boolean includeUserCategories,
		boolean includeGlobalCategories,int includeOtherUserCategories)
	{
		// Root node
		CategoryTreeNode rootCategoryNode=null;
		try
		{
			// Get Hibernate operation
			operation=getCurrentUserOperation(operation);
			
			// Current user
			User user=userSessionService.getCurrentUser(operation);
			
			// Check permissions
			if (checkCategoriesFilterPermission(operation,includeGlobalCategories,includeOtherUserCategories))
			{
				// Root node
				rootCategoryNode=new CategoryTreeNode(null,null);
				
				// First we add non-global user categories if required
				if (includeUserCategories)
				{
					// Initialize user categories
					List<Category> userCategories=categoriesService.getUserCategoriesSortedByName(operation,user);
					
					// Add user default category to the category tree
					Category userDefaultCategory=getUserDefaultCategory(userCategories,user);
					new CategoryTreeNode(
						userDefaultCategory.getCategoryType().getType(),userDefaultCategory,rootCategoryNode);
					
					// Find categories derived from the user default category
					List<Category> userDefaultDerivedCategories=new ArrayList<Category>();
					for (Category userCategory:userCategories)
					{
						if (!userCategory.equals(userDefaultCategory) && 
							categoriesService.isDerivedFrom(operation,userCategory,userDefaultCategory))
						{
							userDefaultDerivedCategories.add(userCategory);
						}
					}
					
					// Repeat iteration over the list of categories derived from the user default category 
					// until all categories have been added to the category tree.
					// Note that the list will be empty when that occurs.
					List<Category> userDefaultDerivedCategoriesAdded=new ArrayList<Category>();
					while (!userDefaultDerivedCategories.isEmpty())
					{
						// Do an iteration over the list of categories derived from the user default category 
						// to add categories to the categories tree
						userDefaultDerivedCategoriesAdded.clear();
						for (Category userDefaultDerivedCategory:userDefaultDerivedCategories)
						{
							// We can add an user category to the category tree only if its parent 
							// has already been added
							CategoryTreeNode parentNode=
								findCategoryNode(rootCategoryNode,userDefaultDerivedCategory.getParent());
							if (parentNode!=null)
							{
								// Add user category to the category tree
								new CategoryTreeNode(userDefaultDerivedCategory.getCategoryType().getType(),
									userDefaultDerivedCategory,parentNode);
								userDefaultDerivedCategoriesAdded.add(userDefaultDerivedCategory);
							}
						}
						
						// Remove user categories added to the category tree from the user categories list 
						for (Category userDefaultDerivedCategoryAdded:userDefaultDerivedCategoriesAdded)
						{
							userDefaultDerivedCategories.remove(userDefaultDerivedCategoryAdded);
						}
					}
				}
				
				// Next we add global categories if required
				if (includeGlobalCategories)
				{
					// Initialize global categories
					List<Category> globalCategories=categoriesService.getGlobalCategoriesSortedByName(operation);
					
					// We need to find global categories without a parent category (root global categories)
					List<Category> rootGlobalCategories=new ArrayList<Category>();
					for (Category globalCategory:globalCategories)
					{
						if (globalCategory.getParent()==null)
						{
							rootGlobalCategories.add(globalCategory);
						}
					}
					
					// Add root global categories and its derived categories to the categories tree
					List<Category> rootGlobalDerivedCategories=new ArrayList<Category>();
					List<Category> rootGlobalDerivedCategoriesAdded=new ArrayList<Category>();
					for (Category rootGlobalCategory:rootGlobalCategories)
					{
						// Add this root global category to the categories tree
						new CategoryTreeNode(rootGlobalCategory.getCategoryType().getType(),
							rootGlobalCategory,rootCategoryNode);
						
						// Find categories derived from this root global category
						rootGlobalDerivedCategories.clear();
						for (Category globalCategory:globalCategories)
						{
							if (!globalCategory.equals(rootGlobalCategory) && 
								categoriesService.isDerivedFrom(operation,globalCategory,rootGlobalCategory))
							{
								rootGlobalDerivedCategories.add(globalCategory);
							}
						}
						
						// Repeat iteration over list of categories derived from this root global category 
						// until all categories have been added to the category tree.
						// Note that the list will be empty when that occurs.
						while (!rootGlobalDerivedCategories.isEmpty())
						{
							// Do an iteration over the list of categories derived from this 
							// root global category to add categories to the categories tree
							rootGlobalDerivedCategoriesAdded.clear();
							for (Category rootGlobalDerivedCategory:rootGlobalDerivedCategories)
							{
								// We can add a category derived from this root global category to the 
								// category tree only if its parent has already been added
								CategoryTreeNode parentNode=
									findCategoryNode(rootCategoryNode,rootGlobalDerivedCategory.getParent());
								if (parentNode!=null)
								{
									// Add a category derived from this root global category to the category tree
									new CategoryTreeNode(rootGlobalDerivedCategory.getCategoryType().getType(),
										rootGlobalDerivedCategory,parentNode);
									rootGlobalDerivedCategoriesAdded.add(rootGlobalDerivedCategory);
								}
							}
							
							// Remove categories added to the category tree from the list of categories derived 
							// from this root global category
							for (Category rootGlobalDerivedCategoryAdded:rootGlobalDerivedCategoriesAdded)
							{
								rootGlobalDerivedCategories.remove(rootGlobalDerivedCategoryAdded);
							}
						}
					}
				}
				
				// Finally we add other users categories if required
				if (includeOtherUserCategories!=CategoriesService.NOT_VIEW_OTHER_USERS_CATEGORIES)
				{
					// Initialize categories of other users
					List<Category> otherUserCategories=null;
					switch (includeOtherUserCategories)
					{
						case CategoriesService.VIEW_OTHER_USERS_PUBLIC_CATEGORIES:
							otherUserCategories=categoriesService.getPublicCategoriesSortedByName(operation);
							break;
						case CategoriesService.VIEW_OTHER_USERS_PRIVATE_CATEGORIES:
							otherUserCategories=categoriesService.getPrivateCategories(operation);
							// In this case we need to sort categories by localized category long name
							categoriesService.sortCategoriesByLocalizedCategoryLongName(operation,otherUserCategories);
							break;
						case CategoriesService.VIEW_OTHER_USERS_ALL_CATEGORIES:
							otherUserCategories=categoriesService.getNonGlobalCategoriesSortedByName(operation);
					}
					
					// Initialize an auxiliar list of checked users used to avoid adding several times 
					// the same public categories to the category tree
					List<User> checkedUsers=new ArrayList<User>();
					
					// Add current user to checked users because we have already managed current 
					// user categories
					checkedUsers.add(user);
					
					// We need to find default categories of other users without a parent category 
					// (root categories of other users)
					List<Category> rootOtherUserCategories=new ArrayList<Category>();
					for (Category otherUserCategory:otherUserCategories)
					{
						// We need to check that current user have permission to see this category of 
						// other user
						if (isCategoryDisplayableByCurrentUser(operation,otherUserCategory))
						{
							User userToCheck=otherUserCategory.getUser();
							if (!checkedUsers.contains(userToCheck))
							{
								Category rootOtherUserCategory=
									getUserDefaultCategory(otherUserCategories,userToCheck);
								checkedUsers.add(userToCheck);
								if (rootOtherUserCategory!=null)
								{
									rootOtherUserCategories.add(rootOtherUserCategory);
								}
							}
							// If we are viewing only private categories it is possible that we need to add 
							// this category as a root category even if it is not a default category without
							// a parent category.
							// Specifically we must add this category if its parent category has public 
							// visibility (and obviously it it is not from the current user)
							if (includeOtherUserCategories==CategoriesService.VIEW_OTHER_USERS_PRIVATE_CATEGORIES)
							{
								Visibility publicVisibility=
									visibilitiesService.getVisibility(operation,"CATEGORY_VISIBILITY_PUBLIC");
								if (!user.equals(otherUserCategory.getUser()) && 
									otherUserCategory.getParent()!=null && 
									(otherUserCategory.getParent().getVisibility().isGlobal() || 
									otherUserCategory.getParent().getVisibility().getLevel()<=
									publicVisibility.getLevel()))
								{
									rootOtherUserCategories.add(otherUserCategory);
								}
							}
						}
					}
					
					// Add root categories of other users and its derived categories to the categories tree
					List<Category> rootOtherUserDerivedCategories=new ArrayList<Category>();
					List<Category> rootOtherUserDerivedCategoriesVisited=new ArrayList<Category>();
					for (Category rootOtherUserCategory:rootOtherUserCategories)
					{
						// Add this root category of other user to the categories tree
						new CategoryTreeNode(
							rootOtherUserCategory.getCategoryType().getType(),rootOtherUserCategory,rootCategoryNode);
						
						// Find categories derived from this root category of other user
						rootOtherUserDerivedCategories.clear();
						for (Category otherUserCategory:otherUserCategories)
						{
							if (!otherUserCategory.equals(rootOtherUserCategory) && 
								categoriesService.isDerivedFrom(operation,otherUserCategory,rootOtherUserCategory))
							{
								rootOtherUserDerivedCategories.add(otherUserCategory);
							}
						}
						
						// Repeat iteration over list of categories derived from this root category of
						// other user until all categories have been added to the category tree.
						// Note that the list will be empty when that occurs.
						while (!rootOtherUserDerivedCategories.isEmpty())
						{
							// Do an iteration over the list of categories derived from this 
							// root category of other user to add categories to the categories tree
							rootOtherUserDerivedCategoriesVisited.clear();
							for (Category rootOtherUserDerivedCategory:rootOtherUserDerivedCategories)
							{
								// We can add a category derived from a root category of other user 
								// to the category tree only if its parent has already been added
								CategoryTreeNode parentNode=findCategoryNode(rootCategoryNode,
									rootOtherUserDerivedCategory.getParent());
								if (parentNode!=null)
								{
									// Moreover we need to check that current user have permission to see
									// this category derived from a root category of other user
									if (isCategoryDisplayableByCurrentUser(operation,rootOtherUserDerivedCategory))
									{
										// Add a category derived from this root category of other user to the 
										// category tree
										new CategoryTreeNode(rootOtherUserDerivedCategory.getCategoryType().getType(),
											rootOtherUserDerivedCategory,parentNode);
									}
									rootOtherUserDerivedCategoriesVisited.add(rootOtherUserDerivedCategory);
								}
							}
							
							// Remove visited categories from the list of categories derived from 
							// this root category of other user
							for (Category rootOtherUserDerivedCategoryVisited:rootOtherUserDerivedCategoriesVisited)
							{
								rootOtherUserDerivedCategories.remove(rootOtherUserDerivedCategoryVisited);
							}
						}
					}
				}
			}
		}
		catch (ServiceException se)
		{
			rootCategoryNode=null;
			throw se;
		}
		return rootCategoryNode;
	}
	
	/**
	 * Refresh categories tree trying to keep current tree view as best as possible.
	 * @param operation Operation
	 * @param includeUserCategories Flag to indicate if we want to include user categories in the 
	 * categories tree 
	 * @param includeGlobalCategories Flag to indicate if we want to include global categories in the 
	 * categories tree 
	 * @param includeOtherUsersCategories Value indicating if we want to include public categories of 
	 * other users in the categories tree
	 */
	private void refreshTree(Operation operation,boolean includeUserCategories,boolean includeGlobalCategories,
		int includeOtherUsersCategories)
	{
		// Generate a new categories tree
		CategoryTreeNode newTree=generateTree(getCurrentUserOperation(operation),includeUserCategories,
			includeGlobalCategories,includeOtherUsersCategories);
		
		// We check that tree generation is successful
		if (newTree!=null)
		{
			// Expand and select nodes in the new categories tree trying to keep current tree view 
			// as best as possible
			newTree.setExpanded(rootNode.isExpanded());
			newTree.setSelected(rootNode.isSelected());
			refreshChildNodes(newTree);
			
			// Set the selected node of the new categories tree based on the selected node of the current one
			CategoryTreeNode newSelectedNode=null;
			if (getSelectedNode()!=null && getSelectedNode()!=rootNode)
			{
				newSelectedNode=findCategoryNode(newTree,getSelectedNode().getCategory());
			}
			
			// Replace current categories tree with the new one
			rootNode=newTree;
			if (newSelectedNode==null)
			{
				setSelectedNode(newTree);
			}
			else
			{
				setSelectedNode(newSelectedNode);
			}
		}
	}
	
	/**
	 * @return Empty category (used when no node is selected on the tree)
	 */
	private Category getEmptyCategory()
	{
		return new Category(0L,"","",null,null,false,null,null);
	}
	
	/**
	 * Process input fiels of the categories filter panel.
	 * @param operation Operation
	 * @param component Component that triggered the listener that called this method
	 */
	private void processCategoriesFilterPanelInputFields(Operation operation,UIComponent component)
	{
		FacesContext context=FacesContext.getCurrentInstance();
		UIInput displayMyCategories=(UIInput)component.findComponent(":categoriesForm:displayMyCategories");
		displayMyCategories.processDecodes(context);
		if (displayMyCategories.getSubmittedValue()!=null)
		{
			setDisplayMyCategories(Boolean.valueOf((String)displayMyCategories.getSubmittedValue()));
		}
		UIInput displayGlobalCategories=(UIInput)component.findComponent(":categoriesForm:displayGlobalCategories");
		displayGlobalCategories.processDecodes(context);
		if (displayGlobalCategories.getSubmittedValue()!=null)
		{
			setDisplayGlobalCategories(Boolean.valueOf((String)displayGlobalCategories.getSubmittedValue()));
		}
		if (getViewOtherUsersPrivateCategoriesEnabled(getCurrentUserOperation(operation)).booleanValue())
		{
			UIInput displayOtherUsersCategoryCombo=
				(UIInput)component.findComponent(":categoriesForm:displayOtherUsersCategoriesCombo");
			displayOtherUsersCategoryCombo.processDecodes(context);
			if (displayOtherUsersCategoryCombo.getSubmittedValue()!=null)
			{
				setDisplayOtherUsersCategoriesCombo(
					Integer.parseInt((String)displayOtherUsersCategoryCombo.getSubmittedValue()));
			}
		}
		else
		{
			UIInput displayOtherUsersCategoriesCheck=
				(UIInput)component.findComponent(":categoriesForm:displayOtherUsersCategoriesCheckbox");
			displayOtherUsersCategoriesCheck.processDecodes(context);
			if (displayOtherUsersCategoriesCheck.getSubmittedValue()!=null)
			{
				setDisplayOtherUsersCategoriesCheck(
					Boolean.valueOf((String)displayOtherUsersCategoriesCheck.getSubmittedValue()).booleanValue());
			}
		}
	}
	
	/**
	 * Process some input fields (category, type, visibility, description) of the categories page.
	 * @param component Component that triggered the listener that called this method
	 */
	private void processCategoriesInputFields(UIComponent component)
	{
		Category selectedCategory=null;
		if (getSelectedNode()!=null && getSelectedNode()!=rootNode)
		{
			selectedCategory=getSelectedNode().getCategory();
		}
		FacesContext context=FacesContext.getCurrentInstance();
		if (isEnabledCategoryName())
		{
			UIInput categoryName=(UIInput)component.findComponent(":categoriesForm:categoryName");
			categoryName.processDecodes(context);
			if (categoryName.getSubmittedValue()!=null && (selectedCategory==null ||
				!selectedCategory.isDefaultCategory()))
			{
				setCategoryDisplayName((String)categoryName.getSubmittedValue());
			}
		}
		if (isEnabledCategoryType())
		{
			UIInput categoryType=(UIInput)component.findComponent(":categoriesForm:categoryType");
			categoryType.processDecodes(context);
			if (categoryType.getSubmittedValue()!=null)
			{
				setCategoryType((String)categoryType.getSubmittedValue());
			}
		}
		if (isEnabledCategoryVisibility())
		{
			UIInput categoryVisibility=(UIInput)component.findComponent(":categoriesForm:categoryVisibility");
			categoryVisibility.processDecodes(context);
			if (categoryVisibility.getSubmittedValue()!=null)
			{
				setCategoryVisibility((String)categoryVisibility.getSubmittedValue());
			}
		}
		if (isEnabledCategoryDescription())
		{
			UIInput categoryDescription=(UIInput)component.findComponent(":categoriesForm:categoryDescription");
			categoryDescription.processDecodes(context);
			if (categoryDescription.getSubmittedValue()!=null)
			{
				getCategory().setDescription((String)categoryDescription.getSubmittedValue());
			}
		}
	}
	
	/**
	 * Node selection listener.
	 * @param event Node select event
	 */
	public void onNodeSelect(NodeSelectEvent event)
	{
		// We need to process some input fields
		processCategoriesInputFields(event.getComponent());
		
		if (MODE_DISPLAY.equals(getMode()) || MODE_EDIT.equals(getMode()))
		{
			readCategoryFromSelectedNode();
			UIInput categoryNameInput=(UIInput)event.getComponent().findComponent(":categoriesForm:categoryName");
			categoryNameInput.setSubmittedValue(getCategoryDisplayName(getCategory()));
			UIInput categoryDescriptionInput=
				(UIInput)event.getComponent().findComponent(":categoriesForm:categoryDescription");
			categoryDescriptionInput.setSubmittedValue(getCategoryDescription(getCategory()));
		}
	}
	
	/**
	 * Node expand listener.
	 * @param event Node expand event
	 */
	public void onNodeExpand(NodeExpandEvent event)
	{
		event.getTreeNode().setExpanded(true);
	}
	
	/**
	 * Node collapse listener.
	 * @param event Node collapse event
	 */
	public void onNodeCollapse(NodeCollapseEvent event)
	{
		// We need to process some input fields
		processCategoriesInputFields(event.getComponent());
		
		CategoryTreeNode node=(CategoryTreeNode)event.getTreeNode();
		node.setExpanded(false);
		if (!node.equals(getSelectedNode()) && isDerivedFrom(getSelectedNode(),node))
		{
			getSelectedNode().setSelected(false);
			setSelectedNode(null);
			setCategory(getEmptyCategory());
			
			UIInput categoryNameInput=(UIInput)event.getComponent().findComponent(":categoriesForm:categoryName");
			categoryNameInput.setSubmittedValue("");
			UIInput categoryDescriptionInput=
				(UIInput)event.getComponent().findComponent(":categoriesForm:categoryDescription");
			categoryDescriptionInput.setSubmittedValue("");
		}
	}
	
	public void selectDisplayMode(ActionEvent event)
	{
		// We need to process some input fields
		processCategoriesInputFields(event.getComponent());
		
		setMode(MODE_DISPLAY);
		if (getSelectedNode()==null || getSelectedNode()==rootNode)
		{
			setCategory(getEmptyCategory());
		}
		else
		{
			readCategoryFromSelectedNode();
		}
		UIInput categoryNameInput=(UIInput)event.getComponent().findComponent(":categoriesForm:categoryName");
		categoryNameInput.setSubmittedValue(getCategoryDisplayName(getCategory()));
		UIInput categoryDescriptionInput=
			(UIInput)event.getComponent().findComponent(":categoriesForm:categoryDescription");
		categoryDescriptionInput.setSubmittedValue(getCategory().getDescription());
	}
	
	public void selectAddMode(ActionEvent event)
	{
		// Check that the user has permission
		setAddModeEnabled(null);
		if (getAddModeEnabled(getCurrentUserOperation(null)).booleanValue())
		{
			// We need to process some input fields
			processCategoriesInputFields(event.getComponent());
			
			setMode(MODE_ADD);
			
			// We are in ADD mode so we set category identifier to 0
			getCategory().setId(0L);
			
			// If we have selected a default category we clear name and description input fields
			if (getSelectedNode()!=null && getSelectedNode()!=rootNode && 
				getSelectedNode().getCategory().isDefaultCategory())
			{
				getCategory().setName("");
				getCategory().setDescription("");
				UIInput categoryNameInput=(UIInput)event.getComponent().findComponent(":categoriesForm:categoryName");
				categoryNameInput.setSubmittedValue("");
				UIInput categoryDescriptionInput=
					(UIInput)event.getComponent().findComponent(":categoriesForm:categoryDescription");
				categoryDescriptionInput.setSubmittedValue("");
			}
		}
		else
		{
			addErrorMessage("NON_AUTHORIZED_ACTION_ERROR");
			setFilterGlobalCategoriesEnabled(null);
			setFilterOtherUsersCategoriesEnabled(null);
			setEditModeEnabled(null);
			setAddGlobalCategoryEnabled(null);
			setDeleteCategoriesEnabled(null);
			resetAdmins();
			resetSuperadmins();
			setEditOtherUsersCategoriesEnabled(null);
			setEditAdminsCategoriesEnabled(null);
			setEditSuperadminsCategoriesEnabled(null);
			resetEditCategoriesAllowed();
			setDeleteOtherUsersCategoriesEnabled(null);
			setDeleteAdminsCategoriesEnabled(null);
			setDeleteSuperadminsCategoriesEnabled(null);
			resetDeleteCategoriesAllowed();
			setViewOtherUsersPrivateCategoriesEnabled(null);
			setViewAdminsPrivateCategoriesEnabled(null);
			setViewSuperadminsPrivateCategoriesEnabled(null);
			
			// Scroll page to top position
			scrollToTop();
		}
	}
	
	public void selectEditMode(ActionEvent event)
	{
		// Check that the user has permission
		setEditModeEnabled(null);
		if (getEditModeEnabled(getCurrentUserOperation(null)).booleanValue())
		{
			// We need to process some input fields
			processCategoriesInputFields(event.getComponent());
			
			setMode(MODE_EDIT);
			if (getSelectedNode()==null || getSelectedNode()==rootNode)
			{
				setCategory(getEmptyCategory());
			}
			else
			{
				readCategoryFromSelectedNode();
			}
			UIInput categoryNameInput=(UIInput)event.getComponent().findComponent(":categoriesForm:categoryName");
			categoryNameInput.setSubmittedValue(getCategoryDisplayName(getCategory()));
			UIInput categoryDescriptionInput=
				(UIInput)event.getComponent().findComponent(":categoriesForm:categoryDescription");
			categoryDescriptionInput.setSubmittedValue(getCategory().getDescription());
		}
		else
		{
			addErrorMessage("NON_AUTHORIZED_ACTION_ERROR");
			setFilterGlobalCategoriesEnabled(null);
			setFilterOtherUsersCategoriesEnabled(null);
			setAddModeEnabled(null);
			setAddGlobalCategoryEnabled(null);
			setDeleteCategoriesEnabled(null);
			resetAdmins();
			resetSuperadmins();
			setEditOtherUsersCategoriesEnabled(null);
			setEditAdminsCategoriesEnabled(null);
			setEditSuperadminsCategoriesEnabled(null);
			resetEditCategoriesAllowed();
			setDeleteOtherUsersCategoriesEnabled(null);
			setDeleteAdminsCategoriesEnabled(null);
			setDeleteSuperadminsCategoriesEnabled(null);
			resetDeleteCategoriesAllowed();
			setViewOtherUsersPrivateCategoriesEnabled(null);
			setViewAdminsPrivateCategoriesEnabled(null);
			setViewSuperadminsPrivateCategoriesEnabled(null);
			
			// Scroll page to top position
			scrollToTop();
		}
	}
	
	/**
	 * Read current category from selected node of tree.
	 */
	private void readCategoryFromSelectedNode()
	{
		Category selectedCategory=getSelectedNode().getCategory();
		setCategory(new Category(selectedCategory.getId(),getCategoryDisplayName(selectedCategory),
			getCategoryDescription(selectedCategory),selectedCategory.getParent(),selectedCategory.getUser(),
			false,selectedCategory.getCategoryType(),selectedCategory.getVisibility()));
	}
	
	/**
	 * @param operation Operation
	 * @param node Category tree node
	 * @param checkName Flag to indicate if we need to check category name
	 * @param checkDescription Flag to indicate if we need to check category description
	 * @param checkCategoryType Flag to indicate if we need to check category type
	 * @param checkVisibility Flag to indicate if we need to check category visibility
	 * @return true if parents categories of a node have not changed from last refresh of the categories tree, 
	 * false otherwise 
	 */
	private boolean checkParentsCategories(Operation operation,CategoryTreeNode node,boolean checkName,
		boolean checkDescription,boolean checkCategoryType,boolean checkVisibility)
	{
		boolean ok=true;
		Category parentCategory=
			node.getParent()==null || node.getParent()==rootNode?null:((CategoryTreeNode)node.getParent()).getCategory();
		if (parentCategory!=null)
		{
			// Get current user session Hibernate operation
			operation=getCurrentUserOperation(operation);
			
			Category currentParentCategory=categoriesService.getCategory(operation,parentCategory.getId());
			if (currentParentCategory==null)
			{
				ok=false;
			}
			else if (!parentCategory.getName().equals(currentParentCategory.getName()))
			{
				ok=false;
			}
			else if (!parentCategory.getCategoryType().equals(currentParentCategory.getCategoryType()))
			{
				ok=false;
			}
			else if (!parentCategory.getVisibility().equals(currentParentCategory.getVisibility()))
			{
				ok=false;
			}
			else if ((parentCategory.getParent()==null && currentParentCategory.getParent()!=null) ||
				parentCategory.getParent()!=null && 
				!parentCategory.getParent().equals(currentParentCategory.getParent()))
			{
				ok=false;
			}
			else
			{
				ok=checkParentsCategories(operation,(CategoryTreeNode)node.getParent(),checkName,checkDescription,
					checkCategoryType,checkVisibility);
			}
		}
		return ok;
	}
	
	/**
	 * @param operation Operation
	 * @param node Category tree node
	 * @param checkName Flag to indicate if we need to check category name
	 * @param checkDescription Flag to indicate if we need to check category description
	 * @param checkCategoryType Flag to indicate if we need to check category type
	 * @param checkVisibility Flag to indicate if we need to check category visibility
	 * @return true if category of a node have not changed from last refresh of the categories tree,
	 * false otherwise
	 */
	private boolean checkThisCategory(Operation operation,CategoryTreeNode node,boolean checkName,
		boolean checkDescription,boolean checkCategoryType,boolean checkVisibility)
	{
		boolean ok=true;
		Category category=node==null || node==rootNode?null:node.getCategory();
		if (category!=null)
		{
			Category currentCategory=categoriesService.getCategory(getCurrentUserOperation(operation),category.getId());
			if (currentCategory==null)
			{
				ok=false;
			}
			else if (checkName && !category.getName().equals(currentCategory.getName()))
			{
				ok=false;
			}
			else if (checkDescription && !category.getDescription().equals(currentCategory.getDescription()))
			{
				ok=false;
			}
			else if (checkCategoryType && !category.getCategoryType().equals(currentCategory.getCategoryType()))
			{
				ok=false;
			}
			else if (checkVisibility && !category.getVisibility().equals(currentCategory.getVisibility()))
			{
				ok=false;
			}
			else if ((category.getParent()==null && currentCategory.getParent()!=null) ||
				category.getParent()!=null && !category.getParent().equals(currentCategory.getParent()))
			{
				ok=false;
			}
		}
		return ok;
	}
	
	/**
	 * @return true if current category name only includes valid characters (letters, digits, whitespaces or _),
	 * false otherwise
	 */
	private boolean checkValidCharactersForCategoryName()
	{
		return !StringUtils.hasUnexpectedCharacters(getCategory().getName(),true,true,true,new char[]{'_'});
	}
	
	/**
	 * @return true if current category name includes at least one letter, false otherwise
	 */
	private boolean checkLetterIncludedForCategoryName()
	{
		return StringUtils.hasLetter(getCategory().getName());
	}
	
	/**
	 * @return true if first character of current category name is not a digit nor a whitespace, false otherwise
	 */
	private boolean checkFirstCharacterNotDigitNotWhitespaceForCategoryName()
	{
		String categoryName=getCategory().getName();
		return !StringUtils.isFirstCharacterDigit(categoryName) && !StringUtils.isFirstCharacterWhitespace(categoryName);
	}
	
	/**
	 * @return true if last character of current category name is not a whitespace, false otherwise
	 */
	private boolean checkLastCharacterNotWhitespaceForCategoryName()
	{
		return !StringUtils.isLastCharacterWhitespace(getCategory().getName());
	}
	
	/**
	 * @return true if current category name does not include consecutive whitespaces, false otherwise
	 */
	private boolean checkNonConsecutiveWhitespacesForCategoryName()
	{
		return !StringUtils.hasConsecutiveWhitespaces(getCategory().getName());
	}
	
	/**
	 * @param operation Operation
	 * @param parentNode Parent node
	 * @return true if current category name entered by user is available, false otherwise 
	 */
	private boolean checkAvailableCategoryName(Operation operation,CategoryTreeNode parentNode)
	{
		boolean ok=true;
		Category parentCategory=parentNode==null || parentNode==rootNode?null:parentNode.getCategory();
		String categoryName=getCategory().getName();
		for (Category childCategory:
			categoriesService.getChildCategories(getCurrentUserOperation(operation),parentCategory))
		{
			if (categoryName.equals(childCategory.getName()) && !childCategory.isDefaultCategory() && 
				!childCategory.equals(getCategory()))
			{
				ok=false;
				break;
			}
		}
		return ok;
	}
	
	/**
	 * @param operation Operation
	 * @param node Node
	 * @param delete Flag to indicate if we are trying to delete a category (true) or update it (false)
	 * @return true if current category doesn't contains unexpected questions, false otherwise 
	 */
	private boolean checkUnexpectedQuestions(Operation operation,CategoryTreeNode node,boolean delete)
	{
		boolean ok=true;
		Category category=node==null || node==rootNode?null:node.getCategory();
		if (category!=null)
		{
			if (delete)
			{
				ok=questionsService.getQuestions(
					getCurrentUserOperation(operation),null,category.getId(),false).isEmpty();
			}
			else
			{
				// Get current user session Hibernate operation
				operation=getCurrentUserOperation(operation);
				
				CategoryType questionsType=categoryTypesService.getCategoryType(operation,"CATEGORY_TYPE_QUESTIONS");
				if (!categoryTypesService.isDerivedFrom(operation,questionsType,getCategory().getCategoryType()) &&
					!categoryTypesService.isDerivedFrom(operation,getCategory().getCategoryType(),questionsType))
				{
					ok=questionsService.getQuestions(operation,null,category.getId(),false).isEmpty();
				}
			}
		}
		return ok;
	}
	
	/**
	 * @param operation Operation
	 * @param node Node
	 * @param delete Flag to indicate if we are trying to delete a category (true) or update it (false)
	 * @return true if current category doesn't contains unexpected tests, false otherwise 
	 */
	private boolean checkUnexpectedTests(Operation operation,CategoryTreeNode node,boolean delete)
	{
		boolean ok=true;
		Category category=node==null || node==rootNode?null:node.getCategory();
		if (category!=null)
		{
			if (delete)
			{
				ok=testsService.getTests(getCurrentUserOperation(operation),null,category.getId(),false).isEmpty();
			}
			else
			{
				// Get current user operation Hibernate operation
				operation=getCurrentUserOperation(operation);
				
				CategoryType questionsType=categoryTypesService.getCategoryType(operation,"CATEGORY_TYPE_TESTS");
				if (!categoryTypesService.isDerivedFrom(operation,questionsType,getCategory().getCategoryType()) &&
					!categoryTypesService.isDerivedFrom(operation,getCategory().getCategoryType(),questionsType))
				{
					ok=testsService.getTests(operation,null,category.getId(),false).isEmpty();
				}
			}
		}
		return ok;
	}
	
	/**
	 * @param operation Operation
	 * @param node Node
	 * @param delete Flag to indicate if we are trying to delete a category (true) or update it (false)
	 * @return true if current category doesn't contains unexpected resources, false otherwise 
	 */
	private boolean checkUnexpectedResources(Operation operation,CategoryTreeNode node,boolean delete)
	{
		boolean ok=true;
		Category category=node==null || node==rootNode?null:node.getCategory();
		if (category!=null)
		{
			if (delete)
			{
				ok=resourcesService.getResources(
					getCurrentUserOperation(operation),null,category.getId(),false).isEmpty();
			}
			else
			{
				// Get current user session Hibernate operation
				operation=getCurrentUserOperation(operation);
				
				//TODO el tipo de categoría CATEGORY_TYPE_IMAGES en realidad esta pensado solo para imagenes, cambiarlo por otro más genérico como CATEGORY_TYPE_RESOURCES cuando este implementado
				CategoryType resourcesType=categoryTypesService.getCategoryType(operation,"CATEGORY_TYPE_IMAGES");
				if (!categoryTypesService.isDerivedFrom(operation,resourcesType,getCategory().getCategoryType()) &&
					!categoryTypesService.isDerivedFrom(operation,getCategory().getCategoryType(),resourcesType))
				{
					ok=resourcesService.getResources(operation,null,category.getId(),false).isEmpty();
				}
			}
		}
		return ok;
	}
	
	/**
	 * @param operation Operation
	 * @param parentNode Parent node
	 * @return true if category type is valid for add/update, false otherwise
	 */
	private boolean checkCategoryType(Operation operation,CategoryTreeNode parentNode)
	{
		boolean ok=false;
		if (getCategory().getId()==0L)
		{
			// There are no childs so we only check parent category
			Category parentCategory=parentNode==null || parentNode==rootNode?null:parentNode.getCategory();
			if (parentCategory==null)
			{
				// Global categories have no category types rectrictions
				ok=true;
			}
			else
			{
				// Get current user session Hibernate operation
				operation=getCurrentUserOperation(operation);
				
				// Read parent category from DB
				parentCategory=categoriesService.getCategory(operation,parentCategory.getId());
				ok=categoryTypesService.isDerivedFrom(
					operation,getCategory().getCategoryType(),parentCategory.getCategoryType());
			}
		}
		else
		{
			// Get current user session Hibernate operation
			operation=getCurrentUserOperation(operation);
			
			// Read category from DB
			Category category=categoriesService.getCategory(operation,getCategory().getId());
			if (category!=null)
			{
				// Check parent category type
				if (category.getParent()==null)
				{
					if (category.isDefaultCategory())
					{
						// Default categories are not allowed to change its category type
						ok=category.getCategoryType().equals(getCategory().getCategoryType());
					}
					else
					{
						// Global categories have no category types rectrictions
						ok=true;
					}
				}
				else
				{
					// Read parent category from DB
					Category parentCategory=categoriesService.getCategory(operation,category.getParent().getId());
					ok=categoryTypesService.isDerivedFrom(
						operation,getCategory().getCategoryType(),parentCategory.getCategoryType());
					if (ok)
					{
						// Check childs categories types
						for (Category childCategory:categoriesService.getChildCategories(operation,category))
						{
							if (!categoryTypesService.isDerivedFrom(
								operation,childCategory.getCategoryType(),getCategory().getCategoryType()))
							{
								ok=false;
								break;
							}
						}
					}
				}
			}
		}
		return ok;
	}
	
	/**
	 * @param operation Operation
	 * @param parentNode Parent node
	 * @return true if category visibility is valid for add/update, false otherwise
	 */
	private boolean checkVisibility(Operation operation,CategoryTreeNode parentNode)
	{
		boolean ok=false;
		if (getCategory().getId()==0L)
		{
			// There are no childs so we only check parent category
			Category parentCategory=parentNode==null || parentNode==rootNode?null:parentNode.getCategory();
			if (parentCategory==null)
			{
				// Global categories are restricted to global visibilities
				ok=getCategory().getVisibility().isGlobal();
			}
			else
			{
				// Read parent category from DB
				parentCategory=categoriesService.getCategory(getCurrentUserOperation(operation),parentCategory.getId());
				ok=parentCategory.getVisibility().isGlobal()==getCategory().getVisibility().isGlobal() &&
					parentCategory.getVisibility().getLevel()<=getCategory().getVisibility().getLevel();
			}
		}
		else
		{
			// Get current user session Hibernate operation
			operation=getCurrentUserOperation(operation);
			
			// Read category from DB
			Category category=categoriesService.getCategory(operation,getCategory().getId());
			if (category!=null)
			{
				// Check parent category visibility
				if (category.getParent()==null)
				{
					if (category.isDefaultCategory())
					{
						// Default categories are restricted to non global visibilities
						ok=!getCategory().getVisibility().isGlobal();
					}
					else
					{
						// Global categories are restricted to global visibilities
						ok=getCategory().getVisibility().isGlobal();
					}
				}
				else
				{
					// Read parent category from DB
					Category parentCategory=categoriesService.getCategory(operation,category.getParent().getId());
					ok=parentCategory.getVisibility().isGlobal()==getCategory().getVisibility().isGlobal() &&
						parentCategory.getVisibility().getLevel()<=getCategory().getVisibility().getLevel();
					if (ok)
					{
						// Check childs categories visibilities
						for (Category childCategory:categoriesService.getChildCategories(operation,category))
						{
							if (getCategory().getVisibility().isGlobal()!=
								childCategory.getVisibility().isGlobal() ||
								getCategory().getVisibility().getLevel()>
								childCategory.getVisibility().getLevel())
							{
								ok=false;
								break;
							}
						}
					}
				}
			}
		}
		return ok;
	}
	
	/**
	 * Listener for adding a new category.
	 * @param event Action event
	 */
	public void addCategory(ActionEvent event)
	{
		boolean ok=true;
		CategoryTreeNode parentNode=null;
		setAddModeEnabled(null);
		setAddGlobalCategoryEnabled(null);
		
		// Get current user session Hibernate operation
		Operation operation=getCurrentUserOperation(null);
		
		if (isEnabledAdd(operation))
		{
			// Add validations
			boolean categoryParentsChanged=false;
			// As caution we set category identifier as 0, but I think this is not needed
			getCategory().setId(0L);
			if (ADD_CHILD.equals(getWhereToAdd()))
			{
				parentNode=getSelectedNode();
				ok=checkThisCategory(operation,getSelectedNode(),true,false,false,false) && 
					checkParentsCategories(operation,getSelectedNode(),true,false,false,false);
				if (!ok)
				{
					categoryParentsChanged=true;
				}
			}
			else if (ADD_SIBLING.equals(getWhereToAdd()))
			{
				parentNode=(CategoryTreeNode)getSelectedNode().getParent();
				if (!checkParentsCategories(operation,getSelectedNode(),true,false,false,false))
				{
					ok=false;
					categoryParentsChanged=true;
				}
			}
			else if (ADD_UNDER_ROOT.equals(getWhereToAdd()))
			{
				parentNode=rootNode;
			}
			if (!checkValidCharactersForCategoryName())
			{
				ok=false;
				addErrorMessage("CATEGORY_NAME_INVALID_CHARACTERS");
			}
			if (!checkLetterIncludedForCategoryName())
			{
				ok=false;
				addErrorMessage("CATEGORY_NAME_WITHOUT_LETTER");
			}
			if (!checkFirstCharacterNotDigitNotWhitespaceForCategoryName())
			{
				ok=false;
				addErrorMessage("CATEGORY_NAME_FIRST_CHARACTER_INVALID");
			}
			if (!checkLastCharacterNotWhitespaceForCategoryName())
			{
				ok=false;
				addErrorMessage("CATEGORY_NAME_LAST_CHARACTER_INVALID");
			}
			if (!checkNonConsecutiveWhitespacesForCategoryName())
			{
				ok=false;
				addErrorMessage("CATEGORY_NAME_WITH_CONSECUTIVE_WHITESPACES");
			}
			if (!checkAvailableCategoryName(operation,parentNode))
			{
				ok=false;
				addErrorMessage("CATEGORY_NAME_ALREADY_DECLARED");
			}
			if (categoryParentsChanged)
			{
				ok=false;
				addErrorMessage("CATEGORY_PARENTS_CHANGED");
			}
			else
			{
				if (!checkCategoryType(operation,parentNode))
				{
					ok=false;
					addErrorMessage("CATEGORY_TYPE_INVALID");
				}
				if (!checkVisibility(operation,parentNode))
				{
					ok=false;
					addErrorMessage("CATEGORY_VISIBILITY_INVALID");
				}
			}
		}
		else
		{
			ok=false;
		}
		if (ok)
		{
			Operation writeOp=null;
			try
			{
				// Start a new Hibernate operation
				writeOp=HibernateUtil.startOperation();
				
				// We add new category
				getCategory().setParent(parentNode==null || parentNode==rootNode?null:parentNode.getCategory());
				getCategory().setUser(userSessionService.getCurrentUser(writeOp));
				long newCategoryId=categoriesService.addCategory(writeOp,getCategory());
				
				// Instatiate a new category for the categories tree
				Category newCategory=new Category(newCategoryId,getCategory().getName(),
					getCategory().getDescription(),getCategory().getParent(),getCategory().getUser(),false,
					getCategory().getCategoryType(),getCategory().getVisibility());
				
				// We need also to add a new tree node to the categories tree
				CategoryTreeNode newNode=
					new CategoryTreeNode(newCategory.getCategoryType().getType(),newCategory,parentNode);
				
				// Finally we unselect last selected node and select created one
				getSelectedNode().setSelected(false);
				parentNode.expandFromRoot();
				setSelectedNode(newNode);
				getSelectedNode().setSelected(true);
				
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
				// End Hibernate opertation
				HibernateUtil.endOperation(writeOp);
				
				// End current user session Hibernate operation
				userSessionService.endCurrentUserOperation();
			}
		}
		else
		{
			if (nonAuthorizedError)
			{
				addErrorMessage("NON_AUTHORIZED_ACTION_ERROR");
			}
			else if ((getSelectedNode()==null || getSelectedNode()==rootNode) && 
				(ADD_CHILD.equals(getWhereToAdd()) || ADD_SIBLING.equals(getWhereToAdd())))
			{
				// No category selected
				addErrorMessage("CATEGORY_REQUIRED");
			}
			else if (getCategory()==null || getCategory().getName()==null || getCategory().getName().equals("")) 
			{
				// Name is required
				addErrorMessage("CATEGORY_NAME_REQUIRED");
			}
			setFilterGlobalCategoriesEnabled(null);
			setFilterOtherUsersCategoriesEnabled(null);
			setEditModeEnabled(null);
			setDeleteCategoriesEnabled(null);
			resetAdmins();
			resetSuperadmins();
			setEditOtherUsersCategoriesEnabled(null);
			setEditAdminsCategoriesEnabled(null);
			setEditSuperadminsCategoriesEnabled(null);
			resetEditCategoriesAllowed();
			setDeleteOtherUsersCategoriesEnabled(null);
			setDeleteAdminsCategoriesEnabled(null);
			setDeleteSuperadminsCategoriesEnabled(null);
			resetDeleteCategoriesAllowed();
			setViewOtherUsersPrivateCategoriesEnabled(null);
			setViewAdminsPrivateCategoriesEnabled(null);
			setViewSuperadminsPrivateCategoriesEnabled(null);
			
			// Scroll page to top position
			scrollToTop();
		}
	}
	
	/**
	 * Listener for updating a category.
	 * @param event Action event
	 */
	public void updateCategory(ActionEvent event)
	{
		boolean ok=true;
		CategoryTreeNode parentNode=(CategoryTreeNode)getSelectedNode().getParent();
		setEditModeEnabled(null);
		setEditOtherUsersCategoriesEnabled(null);
		setEditAdminsCategoriesEnabled(null);
		setEditSuperadminsCategoriesEnabled(null);
		resetEditSelectedCategoryAllowed();
		resetAdminFromSelectedCategoryAllowed();
		resetSuperadminFromSelectedCategoryAllowed();
		
		// Get current user session Hibernate operation
		Operation operation=getCurrentUserOperation(null);
		
		if (isEnabledUpdate())
		{
			// Update validations
			boolean categoryParentsChanged=false;
			if (!checkParentsCategories(operation,getSelectedNode(),true,false,false,false))
			{
				ok=false;
				categoryParentsChanged=true;
			}
			if (!checkValidCharactersForCategoryName())
			{
				ok=false;
				addErrorMessage("CATEGORY_NAME_INVALID_CHARACTERS");
			}
			if (!checkLetterIncludedForCategoryName())
			{
				ok=false;
				addErrorMessage("CATEGORY_NAME_WITHOUT_LETTER");
			}
			if (!checkFirstCharacterNotDigitNotWhitespaceForCategoryName())
			{
				ok=false;
				addErrorMessage("CATEGORY_NAME_FIRST_CHARACTER_INVALID");
			}
			if (!checkLastCharacterNotWhitespaceForCategoryName())
			{
				ok=false;
				addErrorMessage("CATEGORY_NAME_LAST_CHARACTER_INVALID");
			}
			if (!checkNonConsecutiveWhitespacesForCategoryName())
			{
				ok=false;
				addErrorMessage("CATEGORY_NAME_WITH_CONSECUTIVE_WHITESPACES");
			}
			if (!checkAvailableCategoryName(operation,parentNode))
			{
				ok=false;
				addErrorMessage("CATEGORY_NAME_ALREADY_DECLARED");
			}
			if (!checkUnexpectedQuestions(operation,getSelectedNode(),false))
			{
				ok=false;
				addErrorMessage("CATEGORY_EDIT_UNEXPECTED_QUESTIONS");
			}
			if (!checkUnexpectedTests(operation,getSelectedNode(),false))
			{
				ok=false;
				addErrorMessage("CATEGORY_EDIT_UNEXPECTED_TESTS");
			}
			if (!checkUnexpectedResources(operation,getSelectedNode(),false))
			{
				ok=false;
				addErrorMessage("CATEGORY_EDIT_UNEXPECTED_RESOURCES");
			}
			if (categoryParentsChanged)
			{
				ok=false;
				addErrorMessage("CATEGORY_PARENTS_CHANGED");
			}
			else
			{
				if (!checkCategoryType(operation,parentNode))
				{
					ok=false;
					addErrorMessage("CATEGORY_TYPE_INVALID");
				}
				if (!checkVisibility(operation,parentNode))
				{
					ok=false;
					addErrorMessage("CATEGORY_VISIBILITY_INVALID");
				}
			}
		}
		else
		{
			ok=false;
		}
		if (ok)
		{
			Operation writeOp=null;
			try
			{
				// Start a new Hibernate operation
				writeOp=HibernateUtil.startOperation();
				
				
				// We update category with new values
				Category cat=categoriesService.getCategory(writeOp,getCategory().getId());
				if (getSelectedNode()!=null && getSelectedNode()!=rootNode && 
					getSelectedNode().getCategory().isDefaultCategory())
				{
					cat.setName("DEFAULT_CATEGORY");
					UIInput categoryNameInput=(UIInput)event.getComponent().findComponent(":categoriesForm:categoryName");
					categoryNameInput.setSubmittedValue(getCategoryDisplayName(getSelectedNode().getCategory()));
				}
				else
				{
					cat.setName(getCategory().getName());
				}
				cat.setDescription(getCategory().getDescription());
				cat.setUser(getCategory().getUser());
				cat.setCategoryType(getCategory().getCategoryType());
				cat.setVisibility(getCategory().getVisibility());
				cat.setParent(getCategory().getParent());
				categoriesService.updateCategory(writeOp,cat);
				
				// We need also to update tree node
				CategoryTreeNode categoryNode=findCategoryNode(rootNode,getCategory());
				if (isCategoryDisplayableByCurrentUser(writeOp,cat))
				{
					Category nodeCat=categoryNode.getCategory();
					nodeCat.setName(cat.getName());
					nodeCat.setDescription(cat.getDescription());
					nodeCat.setCategoryType(cat.getCategoryType());
					nodeCat.setVisibility(cat.getVisibility());
					categoryNode.setType(cat.getCategoryType().getType());
				}
				else
				{
					refreshTree(operation,currentDisplayMyCategories,currentDisplayGlobalCategories,
						currentDisplayOtherUsersCategories);
					
					// If EDIT mode is not enabled we select DISPLAY mode
					if (MODE_EDIT.equals(getMode()) && !isEnabledEditMode(operation))
					{
						setMode(MODE_DISPLAY);
					}
					
					if (getSelectedNode()==null || getSelectedNode()==rootNode) 
					{
						// In DISPLAY and EDIT modes we clear input fields if there is no selected category
						setCategory(getEmptyCategory());
					}
					else if (getSelectedNode()!=null && getSelectedNode()!=rootNode && MODE_DISPLAY.equals(getMode()))
					{
						// We only update input values of the new selected category in display mode
						readCategoryFromSelectedNode();
					}
					
					// Update category name and description input fields
					UIInput categoryNameInput=(UIInput)event.getComponent().findComponent(":categoriesForm:categoryName");
					categoryNameInput.setSubmittedValue(getCategoryDisplayName(getCategory()));
					UIInput categoryDescriptionInput=
						(UIInput)event.getComponent().findComponent(":categoriesForm:categoryDescription");
					categoryDescriptionInput.setSubmittedValue(getCategory().getDescription());
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
				
				// End current user session Hibernate operation
				userSessionService.endCurrentUserOperation();
			}
		}
		else
		{
			if (nonAuthorizedError)
			{
				addErrorMessage("NON_AUTHORIZED_ACTION_ERROR");
			}
			else if (getSelectedNode()==null || getSelectedNode()==rootNode)
			{
				// No category selected
				addErrorMessage("CATEGORY_REQUIRED");
			}
			// This check has sense because it is possible that "Edit" button is enabled even 
			// if name input field is empty
			else if (getCategory()==null || getCategory().getName()==null || getCategory().getName().equals(""))
			{
				// Name is required
				addErrorMessage("CATEGORY_NAME_REQUIRED");
			}
			setFilterGlobalCategoriesEnabled(null);
			setFilterOtherUsersCategoriesEnabled(null);
			setAddModeEnabled(null);
			setAddGlobalCategoryEnabled(null);
			setDeleteCategoriesEnabled(null);
			resetAdmins();
			resetSuperadmins();
			resetEditCategoriesAllowed();
			setDeleteOtherUsersCategoriesEnabled(null);
			setDeleteAdminsCategoriesEnabled(null);
			setDeleteSuperadminsCategoriesEnabled(null);
			resetDeleteCategoriesAllowed();
			setViewOtherUsersPrivateCategoriesEnabled(null);
			setViewAdminsPrivateCategoriesEnabled(null);
			setViewSuperadminsPrivateCategoriesEnabled(null);
			
			// Scroll page to top position
			scrollToTop();
		}
	}
	
	/**
	 * Listener for deleting a category.
	 * @param event Action event
	 */
	public void deleteCategory(ActionEvent event)
	{
		boolean ok=true;
		setDeleteCategoriesEnabled(null);
		setDeleteOtherUsersCategoriesEnabled(null);
		setDeleteAdminsCategoriesEnabled(null);
		setDeleteSuperadminsCategoriesEnabled(null);
		resetDeleteSelectedCategoryAllowed();
		resetAdminFromSelectedCategoryAllowed();
		resetSuperadminFromSelectedCategoryAllowed();
		if (isEnabledDelete())
		{
			// Get current user session Hibernate operation
			Operation operation=getCurrentUserOperation(null);
			
			// Delete validations
			if (!checkThisCategory(operation,getSelectedNode(),true,true,true,true) || 
				!checkParentsCategories(operation,getSelectedNode(),true,false,false,false))
			{
				ok=false;
				addErrorMessage("CATEGORY_THIS_OR_PARENTS_CHANGED");
			}
			if (!checkUnexpectedQuestions(operation,getSelectedNode(),true))
			{
				ok=false;
				addErrorMessage("CATEGORY_DELETE_UNEXPECTED_QUESTIONS");
			}
			if (!checkUnexpectedTests(operation,getSelectedNode(),true))
			{
				ok=false;
				addErrorMessage("CATEGORY_DELETE_UNEXPECTED_TESTS");
			}
			if (!checkUnexpectedResources(operation,getSelectedNode(),true))
			{
				ok=false;
				addErrorMessage("CATEGORY_DELETE_UNEXPECTED_RESOURCES");
			}
		}
		if (ok)
		{
			try
			{
				categoriesService.deleteCategory(getSelectedNode().getCategory().getId());
				
				// We need to set parent node of the removed node to null to remove it from tree
				getSelectedNode().setParent(null);
				
				// Now we have no node selected, so we reset selectedNode and category
				setSelectedNode(rootNode);
				setCategory(getEmptyCategory());
			}
			finally
			{
				// End current user session Hibernate operation
				userSessionService.endCurrentUserOperation();
			}
		}
		else
		{
			if (nonAuthorizedError)
			{
				addErrorMessage("NON_AUTHORIZED_ACTION_ERROR");
			}
			else if (getSelectedNode()==null || getSelectedNode()==rootNode)
			{
				// No category selected
				addErrorMessage("CATEGORY_REQUIRED");
			}
			else if (!getSelectedNode().isLeaf())
			{
				// Not a leaf node
				addErrorMessage("CHILD_CATEGORIES_FOUND");
			}
			else if (getSelectedNode().getCategory().isDefaultCategory())
			{
				// Default category can't be deleted
				addErrorMessage("DEFAULT_CATEGORY_DELETE_ERROR");
			}
			setFilterGlobalCategoriesEnabled(null);
			setFilterOtherUsersCategoriesEnabled(null);
			setAddModeEnabled(null);
			setEditModeEnabled(null);
			setAddGlobalCategoryEnabled(null);
			resetAdmins();
			resetSuperadmins();
			setEditOtherUsersCategoriesEnabled(null);
			setEditAdminsCategoriesEnabled(null);
			setEditSuperadminsCategoriesEnabled(null);
			resetEditCategoriesAllowed();
			resetDeleteCategoriesAllowed();
			setViewOtherUsersPrivateCategoriesEnabled(null);
			setViewAdminsPrivateCategoriesEnabled(null);
			setViewSuperadminsPrivateCategoriesEnabled(null);
			
			// Scroll page to top position
			scrollToTop();
		}
	}
	
    /**
	 * Listener to refresh categories tree based on the current categories filter.
     * @param event Action event
     */
    public void applyCategoriesFilter(ActionEvent event)
    {
		// Get current user session Hibernate operation
		Operation operation=getCurrentUserOperation(null);
    	
		// We need to process some input fields
    	processCategoriesFilterPanelInputFields(operation,event.getComponent());
    	
    	// Use the checkboxes values as the filter used to display the new category tree
    	currentDisplayMyCategories=isDisplayMyCategories();
    	currentDisplayGlobalCategories=isDisplayGlobalCategories();
    	currentDisplayOtherUsersCategories=displayOtherUsersCategories;
    	
		// Reset permissions
    	setFilterGlobalCategoriesEnabled(null);
    	setFilterOtherUsersCategoriesEnabled(null);
    	setAddModeEnabled(null);
		setEditModeEnabled(null);
		setAddGlobalCategoryEnabled(null);
		setDeleteCategoriesEnabled(null);
		resetAdmins();
		resetSuperadmins();
		setEditOtherUsersCategoriesEnabled(null);
		setEditAdminsCategoriesEnabled(null);
		setEditSuperadminsCategoriesEnabled(null);
		resetEditCategoriesAllowed();
		setDeleteOtherUsersCategoriesEnabled(null);
		setDeleteAdminsCategoriesEnabled(null);
		setDeleteSuperadminsCategoriesEnabled(null);
		resetDeleteCategoriesAllowed();
		setViewOtherUsersPrivateCategoriesEnabled(null);
		setViewAdminsPrivateCategoriesEnabled(null);
		setViewSuperadminsPrivateCategoriesEnabled(null);
    	
		if (checkCategoriesFilterPermission(operation,currentDisplayGlobalCategories,currentDisplayOtherUsersCategories))
		{
			// Refresh categories tree based on recent applied filter
			refreshTree(operation,currentDisplayMyCategories,currentDisplayGlobalCategories,currentDisplayOtherUsersCategories);
			
			// If current mode is not enabled we select DISPLAY mode
			if ((MODE_ADD.equals(getMode()) && !isEnabledAddMode(operation)) || 
				(MODE_EDIT.equals(getMode()) && !isEnabledEditMode(operation)))
			{
				setMode(MODE_DISPLAY);
			}
			
			if ((getSelectedNode()==null || getSelectedNode()==rootNode) && 
				(MODE_DISPLAY.equals(getMode()) || MODE_EDIT.equals(getMode())))
			{
				// In DISPLAY and EDIT modes we clear input fields if there is no selected category
				setCategory(getEmptyCategory());
			}
			else if (getSelectedNode()!=null && getSelectedNode()!=rootNode && MODE_DISPLAY.equals(getMode()))
			{
				// We only update input values of the new selected category in display mode
				readCategoryFromSelectedNode();
			}
			else if (getSelectedNode()!=null && getSelectedNode()!=rootNode && 
				getSelectedNode().getCategory().isDefaultCategory() && MODE_ADD.equals(getMode()))
			{
				getCategory().setName("");
				getCategory().setDescription("");
			}
			
			// Update category name and description input fields
			UIInput categoryNameInput=(UIInput)event.getComponent().findComponent(":categoriesForm:categoryName");
			categoryNameInput.setSubmittedValue(getCategoryDisplayName(getCategory()));
			UIInput categoryDescriptionInput=
				(UIInput)event.getComponent().findComponent(":categoriesForm:categoryDescription");
			categoryDescriptionInput.setSubmittedValue(getCategory().getDescription());
		}
		else
		{
			addErrorMessage("NON_AUTHORIZED_ACTION_ERROR");
			
			// Scroll page to top position
			scrollToTop();
		}
    }
	
	/**
	 * Listener to refresh categories tree keeping last used categories filter instead of current one.<br/><br/>
	 * Moreover it restores last used filter values in the categories filter panel.
	 * @param event Action event
	 */
	public void refreshTree(ActionEvent event)
	{
		// We need to process some input fields
		processCategoriesInputFields(event.getComponent());
		
		// Set categories filter panel checkboxes values as the last filter used to display the categories tree
		setDisplayMyCategories(currentDisplayMyCategories);
		setDisplayGlobalCategories(currentDisplayGlobalCategories);
		displayOtherUsersCategories=currentDisplayOtherUsersCategories;
		
		// Reset permissions
		setFilterGlobalCategoriesEnabled(null);
		setFilterOtherUsersCategoriesEnabled(null);
		setAddModeEnabled(null);
		setEditModeEnabled(null);
		setAddGlobalCategoryEnabled(null);
		setDeleteCategoriesEnabled(null);
		resetAdmins();
		resetSuperadmins();
		setEditOtherUsersCategoriesEnabled(null);
		setEditAdminsCategoriesEnabled(null);
		setEditSuperadminsCategoriesEnabled(null);
		resetEditCategoriesAllowed();
		setDeleteOtherUsersCategoriesEnabled(null);
		setDeleteAdminsCategoriesEnabled(null);
		setDeleteSuperadminsCategoriesEnabled(null);
		resetDeleteCategoriesAllowed();
		setViewOtherUsersPrivateCategoriesEnabled(null);
		setViewAdminsPrivateCategoriesEnabled(null);
		setViewSuperadminsPrivateCategoriesEnabled(null);
		
		// Get current user session Hibernate operation
		Operation operation=getCurrentUserOperation(null);
		
		if (checkCategoriesFilterPermission(operation,currentDisplayGlobalCategories,currentDisplayOtherUsersCategories))
		{
			// Refresh categories tree based on last used categories filter
			refreshTree(
				operation,currentDisplayMyCategories,currentDisplayGlobalCategories,currentDisplayOtherUsersCategories);
			
			// If current mode is not enabled we select DISPLAY mode
			if ((MODE_ADD.equals(getMode()) && !isEnabledAddMode(operation)) || 
				(MODE_EDIT.equals(getMode()) && !isEnabledEditMode(operation)))
			{
				setMode(MODE_DISPLAY);
			}
			
			if ((getSelectedNode()==null || getSelectedNode()==rootNode) && 
				(MODE_DISPLAY.equals(getMode()) || MODE_EDIT.equals(getMode())))
			{
				// In DISPLAY and EDIT modes we clear input fields if there is no selected category
				setCategory(getEmptyCategory());
			}
			else if (getSelectedNode()!=null && getSelectedNode()!=rootNode && MODE_DISPLAY.equals(getMode()))
			{
				// We only update input values of the new selected category in display mode
				readCategoryFromSelectedNode();
			}
			else if (getSelectedNode()!=null && getSelectedNode()!=rootNode && 
				getSelectedNode().getCategory().isDefaultCategory() && MODE_ADD.equals(getMode()))
			{
				getCategory().setName("");
				getCategory().setDescription("");
			}
			
			// Update category name and description input fields
			UIInput categoryNameInput=(UIInput)event.getComponent().findComponent(":categoriesForm:categoryName");
			categoryNameInput.setSubmittedValue(getCategoryDisplayName(getCategory()));
			UIInput categoryDescriptionInput=
				(UIInput)event.getComponent().findComponent(":categoriesForm:categoryDescription");
			categoryDescriptionInput.setSubmittedValue(getCategory().getDescription());
		}
		else
		{
			addErrorMessage("NON_AUTHORIZED_ACTION_ERROR");
			
			// Scroll page to top position
			scrollToTop();
		}
	}
	
	/**
	 * Refresh recursively the state (expanded and/or selected) of the childs of a node of some category tree
	 * based on the state of the same nodes of the current category tree.
	 * @param node Category tree node
	 */
	private void refreshChildNodes(CategoryTreeNode node)
	{
		for (TreeNode child:node.getChildren())
		{
			CategoryTreeNode childNode=(CategoryTreeNode)child;
			CategoryTreeNode oldChildNode=findCategoryNode(rootNode,childNode.getCategory());
			if (oldChildNode!=null)
			{
				childNode.setExpanded(oldChildNode.isExpanded());
				childNode.setSelected(oldChildNode.isSelected());
			}
			refreshChildNodes(childNode);
		}
	}
	
	/**
	 * @param operation Operation
	 * @param includeGlobalCategories Flag to indicate if we want to include global categories in the 
	 * categories tree 
	 * @param includeOtherUserCategories Value indicating if we want to include public categories of 
	 * other users in the categories tree (none, public, private, all)
	 * @return true if user has permissions to display categories with the current selected filter,
	 * false otherwise
	 */
    private boolean checkCategoriesFilterPermission(Operation operation,boolean includeGlobalCategories,
    	int includeOtherUserCategories)
	{
    	boolean ok=true;
    	if (includeGlobalCategories || includeOtherUserCategories!=CategoriesService.NOT_VIEW_OTHER_USERS_CATEGORIES)
    	{
    		// Get current user session Hibernate operation
    		operation=getCurrentUserOperation(operation);
    		
        	ok=(!includeGlobalCategories || getFilterGlobalCategoriesEnabled(operation).booleanValue()) &&
        		(includeOtherUserCategories!=CategoriesService.NOT_VIEW_OTHER_USERS_CATEGORIES || 
        		getFilterOtherUsersCategoriesEnabled(operation).booleanValue());
    	}
    	return ok;
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
	 * Scroll page to top position.
	 */
	private void scrollToTop()
	{
		RequestContext rq=RequestContext.getCurrentInstance();
		rq.execute("window.scrollTo(0,0)");
	}
}
