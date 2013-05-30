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

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.application.FacesMessage;

import org.primefaces.context.RequestContext;

import es.uned.lsi.gepec.model.entities.Category;
import es.uned.lsi.gepec.model.entities.Copyright;
import es.uned.lsi.gepec.model.entities.Resource;
import es.uned.lsi.gepec.model.entities.User;
import es.uned.lsi.gepec.model.entities.Visibility;
import es.uned.lsi.gepec.util.HtmlUtils;
import es.uned.lsi.gepec.util.StringUtils;
import es.uned.lsi.gepec.util.HibernateUtil.Operation;
import es.uned.lsi.gepec.web.services.CategoriesService;
import es.uned.lsi.gepec.web.services.CategoryTypesService;
import es.uned.lsi.gepec.web.services.CopyrightsService;
import es.uned.lsi.gepec.web.services.LocalizationService;
import es.uned.lsi.gepec.web.services.PermissionsService;
import es.uned.lsi.gepec.web.services.ResourcesService;
import es.uned.lsi.gepec.web.services.ServiceException;
import es.uned.lsi.gepec.web.services.UserSessionService;
import es.uned.lsi.gepec.web.services.VisibilitiesService;

//Backbean para la vista recursos
//author Víctor Manuel Alonso Rodríguez
//since  12/2011
/**
 * Managed bean for managing resources for questions.
 */
@SuppressWarnings("serial")
@ManagedBean(name="resourcesBean")
@ViewScoped
public class ResourcesBean implements Serializable
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
	
	private final static int MAX_IMAGE_WIDTH_FOR_RESOURCE_PREVIEW=145;
	private final static int MAX_IMAGE_HEIGHT_FOR_RESOURCE_PREVIEW=100;
	
	private final static int MAX_IMAGE_WIDTH_FOR_RESOURCE_DIALOG=445;
	private final static int MAX_IMAGE_HEIGHT_FOR_RESOURCE_DIALOG=350;
	
	private final static Map<String,String> MIME_TYPES_MASKS;
	static
	{
		MIME_TYPES_MASKS=new HashMap<String,String>();
		MIME_TYPES_MASKS.put("IMAGES_MIME","image/*");
	}
	
	private final static SpecialCategoryFilter ALL_EVEN_PRIVATE_CATEGORIES_OF_OTHER_USERS;
	static
	{
		List<String> allEvenPrivateCategoriesOfOtherUsersPermissions=new ArrayList<String>();
		allEvenPrivateCategoriesOfOtherUsersPermissions.add("PERMISSION_RESOURCES_GLOBAL_FILTER_ENABLED");
		allEvenPrivateCategoriesOfOtherUsersPermissions.add("PERMISSION_RESOURCES_OTHER_USERS_FILTER_ENABLED");
		allEvenPrivateCategoriesOfOtherUsersPermissions.add(
			"PERMISSION_RESOURCES_VIEW_RESOURCES_OF_OTHER_USERS_PRIVATE_CATEGORIES_ENABLED");
		ALL_EVEN_PRIVATE_CATEGORIES_OF_OTHER_USERS=new SpecialCategoryFilter(
			-1L,"ALL_EVEN_PRIVATE_CATEGORIES_OF_OTHER_USERS",allEvenPrivateCategoriesOfOtherUsersPermissions);
	}
	private final static SpecialCategoryFilter ALL_MY_CATEGORIES;
	static
	{
		List<String> allMyCategoriesPermissions=new ArrayList<String>();
		allMyCategoriesPermissions.add("PERMISSION_RESOURCES_GLOBAL_FILTER_ENABLED");
		ALL_MY_CATEGORIES=new SpecialCategoryFilter(-2L,"ALL_MY_CATEGORIES",allMyCategoriesPermissions);
	}
	private final static SpecialCategoryFilter ALL_MY_CATEGORIES_EXCEPT_GLOBALS=
		new SpecialCategoryFilter(-3L,"ALL_MY_CATEGORIES_EXCEPT_GLOBALS",new ArrayList<String>());
	private final static SpecialCategoryFilter ALL_GLOBAL_CATEGORIES;
	static
	{
		List<String> allGlobalCategoriesPermissions=new ArrayList<String>();
		allGlobalCategoriesPermissions.add("PERMISSION_RESOURCES_GLOBAL_FILTER_ENABLED");
		ALL_GLOBAL_CATEGORIES=
			new SpecialCategoryFilter(-4L,"ALL_GLOBAL_CATEGORIES",allGlobalCategoriesPermissions);
	}
	private final static SpecialCategoryFilter ALL_PUBLIC_CATEGORIES_OF_OTHER_USERS;
	static
	{
		List<String> allPublicCategoriesOfOtherUsersPermissions=new ArrayList<String>();
		allPublicCategoriesOfOtherUsersPermissions.add("PERMISSION_RESOURCES_OTHER_USERS_FILTER_ENABLED");
		ALL_PUBLIC_CATEGORIES_OF_OTHER_USERS=new SpecialCategoryFilter(
			-5L,"ALL_PUBLIC_CATEGORIES_OF_OTHER_USERS",allPublicCategoriesOfOtherUsersPermissions);
	}
	private final static SpecialCategoryFilter ALL_PRIVATE_CATEGORIES_OF_OTHER_USERS;
	static
	{
		List<String> allPrivateCategoriesOfOtherUsersPermissions=new ArrayList<String>();
		allPrivateCategoriesOfOtherUsersPermissions.add("PERMISSION_RESOURCES_OTHER_USERS_FILTER_ENABLED");
		allPrivateCategoriesOfOtherUsersPermissions.add(
			"PERMISSION_RESOURCES_VIEW_RESOURCES_OF_OTHER_USERS_PRIVATE_CATEGORIES_ENABLED");
		ALL_PRIVATE_CATEGORIES_OF_OTHER_USERS=new SpecialCategoryFilter(
			-6L,"ALL_PRIVATE_CATEGORIES_OF_OTHER_USERS",allPrivateCategoriesOfOtherUsersPermissions);
	}
	private final static SpecialCategoryFilter ALL_CATEGORIES_OF_OTHER_USERS;
	static
	{
		List<String> allCategoriesOfOtherUsersPermissions=new ArrayList<String>();
		allCategoriesOfOtherUsersPermissions.add("PERMISSION_RESOURCES_OTHER_USERS_FILTER_ENABLED");
		allCategoriesOfOtherUsersPermissions.add(
			"PERMISSION_RESOURCES_VIEW_RESOURCES_OF_OTHER_USERS_PRIVATE_CATEGORIES_ENABLED");
		ALL_CATEGORIES_OF_OTHER_USERS=
			new SpecialCategoryFilter(-7L,"ALL_CATEGORIES_OF_OTHER_USERS",allCategoriesOfOtherUsersPermissions);
	}
	
	@ManagedProperty(value="#{localizationService}")
	private LocalizationService localizationService;
	@ManagedProperty(value="#{resourcesService}")
	private ResourcesService resourcesService;
	@ManagedProperty(value="#{copyrightsService}")
	private CopyrightsService copyrightsService;
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
	
	private List<Resource> resources;									// Resources list
	private Resource currentResource;									// Current resource
	private Long resourceId;													// Identifier of current resource
	
	private Map<Long,SpecialCategoryFilter> specialCategoryFiltersMap;
	private SpecialCategoryFilter allCategories;
	
	private List<Category> specialCategoriesFilters;					// Special categories filters list
	private List<Category> resourcesCategories;							// Resources categories list
	
	// Copyrights
	private List<Copyright> copyrights;
	
	/** UI Helper Properties */
	private long filterCategoryId;
	private boolean filterIncludeSubcategories;
	private String filterMimeType;
	private List<String> mimeTypes;
	private List<String> mimeTypesMasks;
	private long filterCopyrightId;
	private boolean criticalErrorMessage;
	
	private Boolean filterGlobalResourcesEnabled;
	private Boolean filterOtherUsersResourcesEnabled;
	private Boolean addEnabled;
	private Boolean editEnabled;
	private Boolean deleteEnabled;
	private Boolean editOtherUsersResourcesEnabled;
	private Boolean editAdminsResourcesEnabled;
	private Boolean editSuperadminsResourcesEnabled;
	private Boolean deleteOtherUsersResourcesEnabled;
	private Boolean deleteAdminsResourcesEnabled;
	private Boolean deleteSuperadminsResourcesEnabled;
	private Boolean viewResourcesFromOtherUsersPrivateCategoriesEnabled;
	private Boolean viewResourcesFromAdminsPrivateCategoriesEnabled;
	private Boolean viewResourcesFromSuperadminsPrivateCategoriesEnabled;
	
	private Map<Long,Boolean> admins;
	private Map<Long,Boolean> superadmins;
	
	private Map<Long,Boolean> editResourcesAllowed;
	private Map<Long,Boolean> deleteResourcesAllowed;
	
	public ResourcesBean()
	{
		resourceId=0L;
		currentResource=null;
		filterCategoryId=Long.MIN_VALUE;
		filterIncludeSubcategories=false;
		filterMimeType=null;
		mimeTypes=null;
		mimeTypesMasks=null;
		filterCopyrightId=0L;
		specialCategoryFiltersMap=null;
		allCategories=null;
		specialCategoriesFilters=null;
		resourcesCategories=null;
		copyrights=null;
		criticalErrorMessage=false;
		filterGlobalResourcesEnabled=null;
		filterOtherUsersResourcesEnabled=null;
		addEnabled=null;
		editEnabled=null;
		deleteEnabled=null;
		editOtherUsersResourcesEnabled=null;
		editAdminsResourcesEnabled=null;
		editSuperadminsResourcesEnabled=null;
		deleteOtherUsersResourcesEnabled=null;
		deleteAdminsResourcesEnabled=null;
		deleteSuperadminsResourcesEnabled=null;
		admins=new HashMap<Long,Boolean>();
		superadmins=new HashMap<Long,Boolean>();
		editResourcesAllowed=new HashMap<Long,Boolean>();
		deleteResourcesAllowed=new HashMap<Long,Boolean>();
	}
	
	public void setLocalizationService(LocalizationService localizationService)
	{
		this.localizationService=localizationService;
	}
	
	public void setResourcesService(ResourcesService resourcesService)
	{
		this.resourcesService=resourcesService;
	}
	
	public void setCopyrightsService(CopyrightsService copyrightsService)
	{
		this.copyrightsService=copyrightsService;
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
     * @return true if current user is allowed to navigate "Resources" page, false otherwise
     */
    public boolean isNavigationAllowed()
    {
    	return isNavigationAllowed(null);
    }
    
    /**
     * @param operation Operation
     * @return true if current user is allowed to navigate "Resources" page, false otherwise
     */
    private boolean isNavigationAllowed(Operation operation)
    {
    	return userSessionService.isGranted(getCurrentUserOperation(operation),"PERMISSION_NAVIGATION_RESOURCES");
    }
    
	public Long getResourceId()
	{
		return resourceId;
	}
	
	public void setResourceId(Long resourceId)
	{
		this.resourceId=resourceId;
	}
	
    /**
     * @return A special resource that represents not having selected any resource 
     */
    public Resource getNoResource()
    {
		Resource noResource=new Resource();
		noResource.setId(-1);
		noResource.setName(localizationService.getLocalizedMessage("NO_RESOURCE"));
		return noResource;
    	
    }
	
	public Resource getCurrentResource()
	{
		return getCurrentResource(null);
	}
	
	public void setCurrentResource(Resource resource)
	{
		this.currentResource=resource;
	}
	
	private Resource getCurrentResource(Operation operation)
	{
		if (currentResource==null)
		{
			if (getResourceId()>0L)
			{
				currentResource=resourcesService.getResource(getCurrentUserOperation(operation),getResourceId());
			}
			else if (getResourceId()==-1L)
			{
				currentResource=getNoResource();
			}
		}
		return currentResource;
	}
	
	public String getCurrentResourceName()
	{
		return getCurrentResourceName(null);
	}
	
	private String getCurrentResourceName(Operation operation)
	{
		Resource currentResource=getCurrentResource(getCurrentUserOperation(operation));
		return currentResource==null?"":currentResource.getName();
	}
	
	public String getCurrentResourceDescription()
	{
		return getCurrentResourceDescription(null);
	}
	
	private String getCurrentResourceDescription(Operation operation)
	{
		Resource currentResource=getCurrentResource(getCurrentUserOperation(operation));
		return currentResource==null?"":currentResource.getDescription();
	}
	
	public String getCurrentResourceFileName()
	{
		return getCurrentResourceFileName(null);
	}
	
	private String getCurrentResourceFileName(Operation operation)
	{
		Resource currentResource=getCurrentResource(getCurrentUserOperation(operation));
		return currentResource==null?null:currentResource.getFileName();
	}
	
	public String getCurrentResourceMimeType()
	{
		return getCurrentResourceMimeType(null);
	}
	
	private String getCurrentResourceMimeType(Operation operation)
	{
		Resource currentResource=getCurrentResource(getCurrentUserOperation(operation));
		return currentResource==null?"":currentResource.getMimeType();
	}
	
	public String getCurrentResourceUserNick()
	{
		return getCurrentResourceUserNick(null);
	}
	
	private String getCurrentResourceUserNick(Operation operation)
	{
		String currentResourceUserNick="";
		Resource currentResource=getCurrentResource(getCurrentUserOperation(operation));
		if (currentResource!=null && currentResource.getUser()!=null)
		{
			currentResourceUserNick=currentResource.getUser().getNick();
		}
		return currentResourceUserNick;
	}
	
	public String getCurrentResourceLocalizedCategoryLongName()
	{
		return getCurrentResourceLocalizedCategoryLongName(null);
	}
	
	private String getCurrentResourceLocalizedCategoryLongName(Operation operation)
	{
		String currentResourceCategoryLongName="";
		
		// Get current user session Hibernate operation
		operation=getCurrentUserOperation(operation);
		
		Resource currentResource=getCurrentResource(operation);
		if (currentResource!=null && currentResource.getCategory()!=null)
		{
			currentResourceCategoryLongName=
				categoriesService.getLocalizedCategoryLongName(operation,currentResource.getCategory().getId());
		}
		return currentResourceCategoryLongName;
	}
	
	public String getCurrentResourceLocalizedCopyright()
	{
		return getCurrentResourceLocalizedCopyright(null);
	}
	
	private String getCurrentResourceLocalizedCopyright(Operation operation)
	{
		String currentResourceLocalizedCopyright="";
		
		// Get current user session Hibernate operation
		operation=getCurrentUserOperation(operation);
		
		Resource currentResource=getCurrentResource(operation);
		if (currentResource!=null && currentResource.getCopyright()!=null)
		{
			currentResourceLocalizedCopyright=
				copyrightsService.getLocalizedCopyright(operation,currentResource.getCopyright().getId());
		}
		return currentResourceLocalizedCopyright;
	}
	
	public String getCurrentResourceImageDimensionsString()
	{
		return getCurrentResourceImageDimensionsString(null);
	}
	
	private String getCurrentResourceImageDimensionsString(Operation operation)
	{
		String currentResourceImageDimensionsString="";
		
		// Get current user session Hibernate operation
		operation=getCurrentUserOperation(operation);
		
		Resource currentResource=getCurrentResource(operation);
		if (currentResource!=null && currentResource.getId()>0L)
		{
			currentResourceImageDimensionsString=
				resourcesService.getImageDimensionsString(operation,currentResource.getId());
		}
		return currentResourceImageDimensionsString;
	}
	
	public List<Resource> getResources()
	{
		return getResources(null);
	}
	
	private List<Resource> getResources(Operation operation)
	{
		if (resources==null)
		{
			try
			{
				// Get current user session Hibernate operation
				operation=getCurrentUserOperation(operation);
				
				if (checkResourcesFilterPermission(operation,null))
				{
					long filterCategoryId=getFilterCategoryId(operation);
			    	Map<Long,SpecialCategoryFilter> specialCategoryFiltersMap=getSpecialCategoryFiltersMap();
					if (specialCategoryFiltersMap.containsKey(Long.valueOf(filterCategoryId)))
					{
						SpecialCategoryFilter filter=specialCategoryFiltersMap.get(Long.valueOf(filterCategoryId));
						if (getAllCategoriesSpecialCategoryFilter().equals(filter))
						{
							resources=resourcesService.getAllVisibleCategoriesResourcesSortedByName(
								operation,null,getFilterMimeType(),getFilterCopyrightId());
						}
						else if (ALL_EVEN_PRIVATE_CATEGORIES_OF_OTHER_USERS.equals(filter))
						{
							resources=resourcesService.getAllCategoriesResourcesSortedByName(
								operation,null,getFilterMimeType(),getFilterCopyrightId());
						}
						else if (ALL_MY_CATEGORIES.equals(filter))
						{
							resources=resourcesService.getAllMyCategoriesResourcesSortedByName(
								operation,null,getFilterMimeType(),getFilterCopyrightId());
						}
						else if (ALL_MY_CATEGORIES_EXCEPT_GLOBALS.equals(filter))
						{
							resources=resourcesService.getAllMyCategoriesExceptGlobalsResourcesSortedByName(
								operation,null,getFilterMimeType(),getFilterCopyrightId());
						}
						else if (ALL_GLOBAL_CATEGORIES.equals(filter))
						{
							resources=resourcesService.getAllGlobalCategoriesResourcesSortedByName(
								operation,null,getFilterMimeType(),getFilterCopyrightId());
						}
						else if (ALL_PUBLIC_CATEGORIES_OF_OTHER_USERS.equals(filter))
						{
							resources=resourcesService.getAllPublicCategoriesOfOtherUsersResourcesSortedByName(
								operation,null,getFilterMimeType(),getFilterCopyrightId());
						}
						else if (ALL_PRIVATE_CATEGORIES_OF_OTHER_USERS.equals(filter))
						{
							resources=resourcesService.getAllPrivateCategoriesOfOtherUsersResourcesSortedByName(
								operation,null,getFilterMimeType(),getFilterCopyrightId());
						}
						else if (ALL_CATEGORIES_OF_OTHER_USERS.equals(filter))
						{
							resources=resourcesService.getAllCategoriesOfOtherUsersResourcesSortedByName(
								operation,null,getFilterMimeType(),getFilterCopyrightId());
						}
					}
					else
					{
						resources=resourcesService.getResources(operation,null,filterCategoryId,
							isFilterIncludeSubcategories(),getFilterMimeType(),getFilterCopyrightId());
					}
				}
			}
			catch (ServiceException se)
			{
				resources=null;
				addPlainErrorMessage(
					true,localizationService.getLocalizedMessage("INCORRECT_OPERATION"),se.getMessage());
			}
			finally
			{
				// It is not a good idea to return null even if an error is produced because JSF getters 
				// are usually called several times
				if (resources==null)
				{
					resources=new ArrayList<Resource>();
				}
			}
		}
		return resources;
	}
	
	private Resource getResource(Operation operation,long resourceId)
	{
		Resource resource=null;
		for (Resource res:getResources(getCurrentUserOperation(operation)))
		{
			if (res.getId()==resourceId)
			{
				resource=res;
				break;
			}
		}
		return resource;
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
	
    public String getFilterMimeType()
    {
    	if (filterMimeType==null)
    	{
    		List<String> mimeTypeMasks=getMimeTypesMasks();
    		if (mimeTypeMasks.isEmpty())
    		{
        		List<String> mimeTypes=getMimeTypes();
    			filterMimeType=mimeTypes.isEmpty()?null:mimeTypes.get(0);
    		}
    		else
    		{
    			filterMimeType=MIME_TYPES_MASKS.get(mimeTypeMasks.get(0));
    		}
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
	
	public boolean isCriticalErrorMessage()
	{
		return criticalErrorMessage;
	}
	
	public void setCriticalErrorMessage(boolean criticalErrorMessage)
	{
		this.criticalErrorMessage=criticalErrorMessage;
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
		return getFilterGlobalResourcesEnabled().booleanValue();
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
				userSessionService.isGranted(getCurrentUserOperation(operation),"PERMISSION_RESOURCES_ADD_ENABLED"));
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
				userSessionService.isGranted(getCurrentUserOperation(operation),"PERMISSION_RESOURCES_EDIT_ENABLED"));
		}
		return editEnabled;
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
				userSessionService.isGranted(getCurrentUserOperation(operation),"PERMISSION_RESOURCES_DELETE_ENABLED"));
		}
		return deleteEnabled;
	}
	
	public Boolean getEditOtherUsersResourcesEnabled()
	{
		return getEditOtherUsersResourcesEnabled(null);
	}
	
	public void setEditOtherUsersResourcesEnabled(Boolean editOtherUsersResourcesEnabled)
	{
		this.editOtherUsersResourcesEnabled=editOtherUsersResourcesEnabled;
	}
	
	public boolean isEditOtherUsersResourcesEnabled()
	{
		return getEditOtherUsersResourcesEnabled().booleanValue();
	}
	
	private Boolean getEditOtherUsersResourcesEnabled(Operation operation)
	{
		if (editOtherUsersResourcesEnabled==null)
		{
			editOtherUsersResourcesEnabled=Boolean.valueOf(userSessionService.isGranted(
				getCurrentUserOperation(operation),"PERMISSION_RESOURCES_EDIT_OTHER_USERS_RESOURCES_ENABLED"));
		}
		return editOtherUsersResourcesEnabled;
	}
	
	public Boolean getEditAdminsResourcesEnabled()
	{
		return getEditAdminsResourcesEnabled(null);
	}
	
	public void setEditAdminsResourcesEnabled(Boolean editAdminsResourcesEnabled)
	{
		this.editAdminsResourcesEnabled=editAdminsResourcesEnabled;
	}
	
	public boolean isEditAdminsResourcesEnabled()
	{
		return getEditAdminsResourcesEnabled().booleanValue();
	}
	
	private Boolean getEditAdminsResourcesEnabled(Operation operation)
	{
		if (editAdminsResourcesEnabled==null)
		{
			editAdminsResourcesEnabled=Boolean.valueOf(userSessionService.isGranted(
				getCurrentUserOperation(operation),"PERMISSION_RESOURCES_EDIT_ADMINS_RESOURCES_ENABLED"));
		}
		return editAdminsResourcesEnabled;
	}
	
	public Boolean getEditSuperadminsResourcesEnabled()
	{
		return getEditSuperadminsResourcesEnabled(null);
	}
	
	public void setEditSuperadminsResourcesEnabled(Boolean editSuperadminsResourcesEnabled)
	{
		this.editSuperadminsResourcesEnabled=editSuperadminsResourcesEnabled;
	}
	
	public boolean isEditSuperadminsResourcesEnabled()
	{
		return getEditSuperadminsResourcesEnabled().booleanValue();
	}
	
	private Boolean getEditSuperadminsResourcesEnabled(Operation operation)
	{
		if (editSuperadminsResourcesEnabled==null)
		{
			editSuperadminsResourcesEnabled=Boolean.valueOf(userSessionService.isGranted(
				getCurrentUserOperation(operation),"PERMISSION_RESOURCES_EDIT_SUPERADMINS_RESOURCES_ENABLED"));
		}
		return editSuperadminsResourcesEnabled;
	}
	
	public Boolean getDeleteOtherUsersResourcesEnabled()
	{
		return getDeleteOtherUsersResourcesEnabled(null);
	}
	
	public void setDeleteOtherUsersResourcesEnabled(Boolean deleteOtherUsersResourcesEnabled)
	{
		this.deleteOtherUsersResourcesEnabled=deleteOtherUsersResourcesEnabled;
	}
	
	public boolean isDeleteOtherUsersResourcesEnabled()
	{
		return getDeleteOtherUsersResourcesEnabled().booleanValue();
	}
	
	private Boolean getDeleteOtherUsersResourcesEnabled(Operation operation)
	{
		if (deleteOtherUsersResourcesEnabled==null)
		{
			deleteOtherUsersResourcesEnabled=Boolean.valueOf(userSessionService.isGranted(
				getCurrentUserOperation(operation),"PERMISSION_RESOURCES_DELETE_OTHER_USERS_RESOURCES_ENABLED"));
		}
		return deleteOtherUsersResourcesEnabled;
	}
	
	public Boolean getDeleteAdminsResourcesEnabled()
	{
		return getDeleteAdminsResourcesEnabled(null);
	}
	
	public void setDeleteAdminsResourcesEnabled(Boolean deleteAdminsResourcesEnabled)
	{
		this.deleteAdminsResourcesEnabled=deleteAdminsResourcesEnabled;
	}
	
	public boolean isDeleteAdminsResourcesEnabled()
	{
		return getDeleteAdminsResourcesEnabled().booleanValue();
	}
	
	private Boolean getDeleteAdminsResourcesEnabled(Operation operation)
	{
		if (deleteAdminsResourcesEnabled==null)
		{
			deleteAdminsResourcesEnabled=Boolean.valueOf(userSessionService.isGranted(
				getCurrentUserOperation(operation),"PERMISSION_RESOURCES_DELETE_ADMINS_RESOURCES_ENABLED"));
		}
		return deleteAdminsResourcesEnabled;
	}
	
	public Boolean getDeleteSuperadminsResourcesEnabled()
	{
		return getDeleteSuperadminsResourcesEnabled(null);
	}
	
	public void setDeleteSuperadminsResourcesEnabled(Boolean deleteSuperadminsResourcesEnabled)
	{
		this.deleteSuperadminsResourcesEnabled=deleteSuperadminsResourcesEnabled;
	}
	
	public boolean isDeleteSuperadminsResourceEnabled()
	{
		return getDeleteSuperadminsResourcesEnabled().booleanValue();
	}
	
	private Boolean getDeleteSuperadminsResourcesEnabled(Operation operation)
	{
		if (deleteSuperadminsResourcesEnabled==null)
		{
			deleteSuperadminsResourcesEnabled=Boolean.valueOf(userSessionService.isGranted(
				getCurrentUserOperation(operation),"PERMISSION_RESOURCES_DELETE_SUPERADMINS_RESOURCES_ENABLED"));
		}
		return deleteSuperadminsResourcesEnabled;
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
	
	private void resetAdminFromResourceAllowed(Resource resource)
	{
		if (resource!=null && resource.getUser()!=null)
		{
			admins.remove(Long.valueOf(resource.getUser().getId()));
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
	
	private void resetSuperadminFromResourceAllowed(Resource resource)
	{
		if (resource!=null && resource.getUser()!=null)
		{
			superadmins.remove(Long.valueOf(resource.getUser().getId()));
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
	
	private void resetEditResourcesAllowed()
	{
		editResourcesAllowed.clear();
	}
	
	private void resetEditResourceAllowed(Resource resource)
	{
		if (resource!=null)
		{
			resetEditResourceAllowed(resource.getId());
		}
	}
	
	private void resetEditResourceAllowed(long resourceId)
	{
		editResourcesAllowed.remove(Long.valueOf(resourceId));
	}
	
	private boolean isEditResourceAllowed(Operation operation,long resourceId)
	{
		boolean allowed=false;
		if (resourceId>0L)
		{
			if (editResourcesAllowed.containsKey(Long.valueOf(resourceId)))
			{
				allowed=editResourcesAllowed.get(Long.valueOf(resourceId)).booleanValue();
			}
			else
			{
				// Get current user session Hibernate operation
				operation=getCurrentUserOperation(operation);
				
				/*
				User currentUser=userSessionService.getCurrentUser(operation);
				allowed=getEditEnabled(operation).booleanValue() && (currentUser.equals(resourceUser) || 
					(getEditOtherUsersResourcesEnabled(operation).booleanValue() && 
					(!isAdmin(operation,resourceUser) || getEditAdminsResourcesEnabled(operation).booleanValue()) && 
					(!isSuperadmin(operation,resourceUser) || 
					getEditSuperadminsResourcesEnabled(operation).booleanValue())));
				*/
				
				User resourceUser=getResource(operation,resourceId).getUser();
				allowed=getEditEnabled(operation).booleanValue() && 
					(resourceUser.getId()==userSessionService.getCurrentUserId() || 
					(getEditOtherUsersResourcesEnabled(operation).booleanValue() && 
					(!isAdmin(operation,resourceUser) || getEditAdminsResourcesEnabled(operation).booleanValue()) && 
					(!isSuperadmin(operation,resourceUser) || 
					getEditSuperadminsResourcesEnabled(operation).booleanValue())));
				
				editResourcesAllowed.put(Long.valueOf(resourceId),Boolean.valueOf(allowed));
			}
		}
		return allowed;
	}
	
	public boolean isEditResourceAllowed(Resource resource)
	{
		return isEditResourceAllowed(null,resource==null?0L:resource.getId());
	}
	
	private void resetDeleteResourcesAllowed()
	{
		deleteResourcesAllowed.clear();
	}
	
	private void resetDeleteResourceAllowed(Resource resource)
	{
		if (resource!=null)
		{
			resetDeleteResourceAllowed(resource.getId());
		}
	}
	
	private void resetDeleteResourceAllowed(long resourceId)
	{
		deleteResourcesAllowed.remove(Long.valueOf(resourceId));
	}
	
	private boolean isDeleteResourceAllowed(Operation operation,long resourceId)
	{
		boolean allowed=false;
		if (resourceId>0L)
		{
			if (deleteResourcesAllowed.containsKey(Long.valueOf(resourceId)))
			{
				allowed=deleteResourcesAllowed.get(Long.valueOf(resourceId)).booleanValue();
			}
			else
			{
				// Get current user session Hibernate operation
				operation=getCurrentUserOperation(operation);
				
				/*
				User currentUser=userSessionService.getCurrentUser(operation);
				allowed=getDeleteEnabled(operation).booleanValue() && (currentUser.equals(resourceUser) || 
					(getDeleteOtherUsersResourcesEnabled(operation).booleanValue() && 
					(!isAdmin(operation,resourceUser) || getDeleteAdminsResourcesEnabled(operation).booleanValue()) && 
					(!isSuperadmin(operation,resourceUser) || 
					getDeleteSuperadminsResourcesEnabled(operation).booleanValue())));
				*/
				
				User resourceUser=getResource(operation,resourceId).getUser();
				allowed=getDeleteEnabled(operation).booleanValue() && 
					(resourceUser.getId()==userSessionService.getCurrentUserId() || 
					(getDeleteOtherUsersResourcesEnabled(operation).booleanValue() && 
					(!isAdmin(operation,resourceUser) || getDeleteAdminsResourcesEnabled(operation).booleanValue()) && 
					(!isSuperadmin(operation,resourceUser) || 
					getDeleteSuperadminsResourcesEnabled(operation).booleanValue())));
				
				deleteResourcesAllowed.put(Long.valueOf(resourceId),Boolean.valueOf(allowed));
			}
		}
		return allowed;
	}
	
	public boolean isDeleteResourceAllowed(Resource resource)
	{
		return isDeleteResourceAllowed(null,resource==null?0L:resource.getId());
	}
	
	public SpecialCategoryFilter getAllCategoriesSpecialCategoryFilter()
	{
		if (allCategories==null)
		{
			List<String> allPermissions=new ArrayList<String>();
			allPermissions.add("PERMISSION_RESOURCES_GLOBAL_FILTER_ENABLED");
			allPermissions.add("PERMISSION_RESOURCES_OTHER_USERS_FILTER_ENABLED");
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
	
    /**
	 * @return List of visible categories for resources
	 */
    public List<Category> getResourcesCategories()
    {
    	return getResourcesCategories(null);
    }
    
    /**
	 * @param operation Operation
	 * @return List of visible categories for resources
	 */
    private List<Category> getResourcesCategories(Operation operation)
    {
    	if (resourcesCategories==null)
    	{
    		// Get current user session Hibernate operation
    		operation=getCurrentUserOperation(operation);
    		
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
       		
       		// In case that current user is allowed to view private categories of other users 
       		// we also need to check if he/she has permission to view private categories of administrators
       		// and/or users with permission to improve permissions over their owned ones (superadmins)
       		boolean includeAdminsPrivateCategories=false;
       		boolean includeSuperadminsPrivateCategories=false;
       		if (includeOtherUsersCategories==CategoriesService.VIEW_OTHER_USERS_ALL_CATEGORIES)
       		{
       			includeAdminsPrivateCategories=
       				getViewResourcesFromAdminsPrivateCategoriesEnabled(operation).booleanValue();
       			includeSuperadminsPrivateCategories=
       				getViewResourcesFromSuperadminsPrivateCategoriesEnabled(operation).booleanValue();
       		}
       		
       		// Get visible categories for resources taking account all user permissions
       		resourcesCategories=categoriesService.getCategoriesSortedByHierarchy(operation,
       			userSessionService.getCurrentUser(operation),
       			categoryTypesService.getCategoryType(operation,"CATEGORY_TYPE_IMAGES"),true,
       			getFilterGlobalResourcesEnabled(operation).booleanValue(),includeOtherUsersCategories,
       			includeAdminsPrivateCategories,includeSuperadminsPrivateCategories);
    	}
		return resourcesCategories; 
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
	
    public boolean hasDescription(Resource resource)
    {
    	return resource!=null && resource.getDescription()!=null && !resource.getDescription().equals("");
    }
    
	public int getResourceWidthForResourcePreview(Resource resource)
	{
		return getResourceWidthForResourcePreview(null,resource);
	}
	
	private int getResourceWidthForResourcePreview(Operation operation,Resource resource)
	{
		// Get current user session Hibernate operation
		operation=getCurrentUserOperation(operation);
		
		ResourceBean resourceBean=new ResourceBean(resource);
		resourceBean.setResourcesService(resourcesService);
		resourceBean.setUserSessionService(userSessionService);
		int width=resourceBean.getWidth(operation);
		int height=resourceBean.getHeight(operation);
		if (width>MAX_IMAGE_WIDTH_FOR_RESOURCE_PREVIEW || height>MAX_IMAGE_HEIGHT_FOR_RESOURCE_PREVIEW)
		{
			if ((double)width/(double)height>=
				(double)MAX_IMAGE_WIDTH_FOR_RESOURCE_PREVIEW/(double)MAX_IMAGE_HEIGHT_FOR_RESOURCE_PREVIEW)
			{
				width=MAX_IMAGE_WIDTH_FOR_RESOURCE_PREVIEW;
			}
			else
			{
				width=(width*MAX_IMAGE_HEIGHT_FOR_RESOURCE_PREVIEW)/height;
			}
		}
		return width>=1?width:1;
	}
	
	public int getResourceHeightForResourcePreview(Resource resource)
	{
		return getResourceHeightForResourcePreview(null,resource);
	}
    
	private int getResourceHeightForResourcePreview(Operation operation,Resource resource)
	{
		// Get current user session Hibernate operation
		operation=getCurrentUserOperation(operation);
		
		ResourceBean resourceBean=new ResourceBean(resource);
		resourceBean.setResourcesService(resourcesService);
		resourceBean.setUserSessionService(userSessionService);
		int width=resourceBean.getWidth(operation);
		int height=resourceBean.getHeight(operation);
		if (width>MAX_IMAGE_WIDTH_FOR_RESOURCE_PREVIEW || height>MAX_IMAGE_HEIGHT_FOR_RESOURCE_PREVIEW)
		{
			if ((double)width/(double)height>=
				(double)MAX_IMAGE_WIDTH_FOR_RESOURCE_PREVIEW/(double)MAX_IMAGE_HEIGHT_FOR_RESOURCE_PREVIEW)
			{
				height=(height*MAX_IMAGE_WIDTH_FOR_RESOURCE_PREVIEW)/width;
			}
			else
			{
				height=MAX_IMAGE_HEIGHT_FOR_RESOURCE_PREVIEW;
			}
		}
		return height>=1?height:1;
	}
	
	public int getCurrentResourceWidthForResourceDialog()
	{
		return getCurrentResourceWidthForResourceDialog(null);
	}
	
	private int getCurrentResourceWidthForResourceDialog(Operation operation)
	{
		// Get current user session Hibernate operation
		operation=getCurrentUserOperation(operation);
		
		int width=1;
		if (currentResource!=null && currentResource.getId()!=0L)
		{
			ResourceBean currentResourceBean=new ResourceBean(currentResource);
			currentResourceBean.setResourcesService(resourcesService);
			currentResourceBean.setUserSessionService(userSessionService);
			width=currentResourceBean.getWidth(operation);
			int height=currentResourceBean.getHeight(operation);
			if (width>MAX_IMAGE_WIDTH_FOR_RESOURCE_DIALOG || height>MAX_IMAGE_HEIGHT_FOR_RESOURCE_DIALOG)
			{
				if ((double)width/(double)height>=
					(double)MAX_IMAGE_WIDTH_FOR_RESOURCE_DIALOG/(double)MAX_IMAGE_HEIGHT_FOR_RESOURCE_DIALOG)
				{
					width=MAX_IMAGE_WIDTH_FOR_RESOURCE_DIALOG;
				}
				else
				{
					width=(width*MAX_IMAGE_HEIGHT_FOR_RESOURCE_DIALOG)/height;
				}
			}
		}
		return width>=1?width:1;
	}
	
	public int getCurrentResourceHeightForResourceDialog()
	{
		return getCurrentResourceHeightForResourceDialog(null);
	}
    
	private int getCurrentResourceHeightForResourceDialog(Operation operation)
	{
		int height=1;
		if (currentResource!=null && currentResource.getId()!=0L)
		{
			// Get current user session Hibernate operation
			operation=getCurrentUserOperation(operation);
			
			ResourceBean currentResourceBean=new ResourceBean(currentResource);
			currentResourceBean.setResourcesService(resourcesService);
			currentResourceBean.setUserSessionService(userSessionService);
			int width=currentResourceBean.getWidth(operation);
			height=currentResourceBean.getHeight(operation);
			if (width>MAX_IMAGE_WIDTH_FOR_RESOURCE_DIALOG || height>MAX_IMAGE_HEIGHT_FOR_RESOURCE_DIALOG)
			{
				if ((double)width/(double)height>=
					(double)MAX_IMAGE_WIDTH_FOR_RESOURCE_DIALOG/(double)MAX_IMAGE_HEIGHT_FOR_RESOURCE_DIALOG)
				{
					height=(height*MAX_IMAGE_WIDTH_FOR_RESOURCE_DIALOG)/width;
				}
				else
				{
					height=MAX_IMAGE_HEIGHT_FOR_RESOURCE_DIALOG;
				}
			}
		}
		return height>=1?height:1;
	}
	
	public String getCurrentResourceImageDimensions()
	{
		return getCurrentResourceImageDimensions(null);
	}
	
	private String getCurrentResourceImageDimensions(Operation operation)
	{
		// Get current user session Hibernate operation
		operation=getCurrentUserOperation(operation);
		
		StringBuffer imageDimensions=new StringBuffer(localizationService.getLocalizedMessage("IMAGE_DIMENSIONS"));
		imageDimensions.append(": ");
		if (currentResource!=null && currentResource.getId()!=-1)
		{
			ResourceBean currentResourceBean=new ResourceBean(currentResource);
			currentResourceBean.setResourcesService(resourcesService);
			currentResourceBean.setUserSessionService(userSessionService);
			imageDimensions.append(currentResourceBean.getWidth(operation));
			imageDimensions.append(" X ");
			imageDimensions.append(currentResourceBean.getHeight(operation));
		}
		return imageDimensions.toString();
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
    
    /**
	 * @param copyright Copyright
	 * @return Localized copyright
	 */
	public String getLocalizedCopyright(Copyright copyright)
	{
		return getLocalizedCopyright(null,copyright);
	}
	
    /**
     * @param operation Operation
	 * @param copyright Copyright
	 * @return Localized copyright
	 */
	private String getLocalizedCopyright(Operation operation,Copyright copyright)
	{
		return copyrightsService.getLocalizedCopyright(getCurrentUserOperation(operation),copyright.getId());
	}
	
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
	 * @param resource Resource
	 * @return Image dimensions as string or empty string if resource is not an image
	 */
	public String getImageDimensionsString(Resource resource)
	{
		return getImageDimensionsString(null,resource);
	}
	
	/**
	 * @param operation Operation
	 * @param resource Resource
	 * @return Image dimensions as string or empty string if resource is not an image
	 */
	private String getImageDimensionsString(Operation operation,Resource resource)
	{
		return resourcesService.getImageDimensionsString(getCurrentUserOperation(operation),resource.getId());
	}
	
    public void view(ActionEvent event)
    {
    	currentResource=(Resource)event.getComponent().getAttributes().get("resource");
		RequestContext rq=RequestContext.getCurrentInstance();
    	rq.execute("resourceDialog.show()");
    }
    
	/**
	 * @param operation Operation
	 * @param filterCategory Filter category can be optionally passed as argument
	 * @return true if user has permissions to display resources with the current selected filter, 
	 * false otherwise
	 */
    private boolean checkResourcesFilterPermission(Operation operation,Category filterCategory)
	{
    	boolean ok=true;
    	
    	// Get current user session Hibernate operation
    	operation=getCurrentUserOperation(operation);
    	
    	long filterCategoryId=getFilterCategoryId(operation);
    	Map<Long,SpecialCategoryFilter> specialCategoryFiltersMap=getSpecialCategoryFiltersMap();
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
		}
		else
		{
			// Check permissions needed for selected special category filter
			if (filterCategory==null)
			{
				// If we have not received filter category as argument we need to get it from DB
				filterCategory=categoriesService.getCategory(operation,filterCategoryId);	
			}
			if (filterCategory.getVisibility().isGlobal())
			{
				// This is a global category, so we check that current user has permissions to filter
				// resources by global categories
				if (getFilterGlobalResourcesEnabled(operation).booleanValue())
				{
					/*
					User currentUser=userSessionService.getCurrentUser(operation);
					User categoryUser=filterCategory.getUser();
					ok=currentUser.equals(categoryUser) || 
						getFilterOtherUsersResourcesEnabled(operation).booleanValue();
					*/
					
					// Moreover we need to check that the category is owned by current user or 
					// that current user has permission to filter by categories of other users 
					ok=filterCategory.getUser().getId()==userSessionService.getCurrentUserId() || 
						getFilterOtherUsersResourcesEnabled(operation).booleanValue();
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
					if (getFilterOtherUsersResourcesEnabled(operation).booleanValue())
					{
						// We have to see if this a public or a private category
						// Public categories doesn't need more checks
						// But private categories need aditional permissions
						Visibility privateVisibility=
							visibilitiesService.getVisibility(operation,"CATEGORY_VISIBILITY_PRIVATE");
						if (filterCategory.getVisibility().getLevel()>=privateVisibility.getLevel())
						{
							// Finally we need to check that current user has permission to view resources 
							// from private categories of other users, and aditionally we need to check 
							// that current user has permission to see private categories of administrators 
							// if the owner of the category is an administrator and to check that current
							// user has permission to see private categories of users with permission to 
							// improve permissions over its owned ones if the owner of the category has 
							// that permission (superadmin)
							ok=getViewResourcesFromOtherUsersPrivateCategoriesEnabled(operation).booleanValue() &&
								((!isAdmin(operation,categoryUser) || 
								getViewResourcesFromAdminsPrivateCategoriesEnabled(operation).booleanValue()) &&
								(!isSuperadmin(operation,categoryUser) || 
								getViewResourcesFromSuperadminsPrivateCategoriesEnabled(operation).booleanValue()));
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
	 * Change resources to display on datagrid based on filter.
     * @param event Action event
     */
    public void applyResourcesFilter(ActionEvent event)
    {
    	// Get current user session Hibernate operation
    	Operation operation=getCurrentUserOperation(null);
    	
       	setFilterGlobalResourcesEnabled(null);
       	setFilterOtherUsersResourcesEnabled(null);
       	Category filterCategory=null;
       	long filterCategoryId=getFilterCategoryId(operation);
       	if (filterCategoryId>0L)
       	{
       		filterCategory=categoriesService.getCategory(operation,filterCategoryId);
       		resetAdminFromCategoryAllowed(filterCategory);
       		resetSuperadminFromCategoryAllowed(filterCategory);
       	}
       	setViewResourcesFromOtherUsersPrivateCategoriesEnabled(null);
       	setViewResourcesFromAdminsPrivateCategoriesEnabled(null);
       	setViewResourcesFromSuperadminsPrivateCategoriesEnabled(null);
       	
       	if (checkResourcesFilterPermission(operation,filterCategory))
       	{
       		// Reload resources from DB
           	resources=null;
       	}
       	else
       	{
       		addErrorMessage(true,"INCORRECT_OPERATION","NON_AUTHORIZED_ACTION_ERROR");
       		setAddEnabled(null);
       		setEditEnabled(null);
       		setDeleteEnabled(null);
       		resetAdmins();
       		resetSuperadmins();
       		resetEditResourcesAllowed();
       		setEditOtherUsersResourcesEnabled(null);
       		setEditAdminsResourcesEnabled(null);
       		setEditSuperadminsResourcesEnabled(null);
       		resetDeleteResourcesAllowed();
       		setDeleteOtherUsersResourcesEnabled(null);
       		setDeleteAdminsResourcesEnabled(null);
       		setDeleteSuperadminsResourcesEnabled(null);
   			
   			// Reload categories from DB
   			resourcesCategories=null;
   			
   			if (!getResourcesCategories(operation).contains(filterCategory))
   			{
   				// Reload resources from DB
   				resources=null;
   			}
       	}
    }
    
	// ActionListener para la confirmación de la eliminación de un recurso
    /**
     * Shows a dialog to confirm resource deletion.
     * @param event Action event
     */
	public void confirm(ActionEvent event)
	{
		RequestContext rq=RequestContext.getCurrentInstance();
		rq.execute("confirmDialog.show()");
	}
	
	/**
	 * Adds a new resource.
	 * @return Next view
	 */
	public String addResource()
	{
		String newView=null;
		setAddEnabled(null);
		if (getAddEnabled(getCurrentUserOperation(null)).booleanValue())
		{
			newView="resource";
		}
		else
		{
    		addErrorMessage(true,"INCORRECT_OPERATION","NON_AUTHORIZED_ACTION_ERROR");
			setFilterGlobalResourcesEnabled(null);
			setFilterOtherUsersResourcesEnabled(null);
			setEditEnabled(null);
			setDeleteEnabled(null);
			resetAdmins();
			resetSuperadmins();
			setEditOtherUsersResourcesEnabled(null);
			setEditAdminsResourcesEnabled(null);
			setEditSuperadminsResourcesEnabled(null);
			resetEditResourcesAllowed();
			setDeleteOtherUsersResourcesEnabled(null);
			setDeleteAdminsResourcesEnabled(null);
			setDeleteSuperadminsResourcesEnabled(null);
			resetDeleteResourcesAllowed();
			
			// Reload categories from DB
			resourcesCategories=null;
		}
		return newView;
	}
	
	/**
	 * Edits a resource.
	 * @param resource Resource to edit
	 * @return Next view
	 */
	public String editResource(Resource resource)
	{
		String newView=null;
		setEditEnabled(null);
		setEditOtherUsersResourcesEnabled(null);
		setEditAdminsResourcesEnabled(null);
		setEditSuperadminsResourcesEnabled(null);
		resetEditResourceAllowed(resource);
		resetAdminFromResourceAllowed(resource);
		resetSuperadminFromResourceAllowed(resource);
		if (isEditResourceAllowed(resource))
		{
			newView="resource";
		}
		else
		{
    		addErrorMessage(true,"INCORRECT_OPERATION","NON_AUTHORIZED_ACTION_ERROR");
			setFilterGlobalResourcesEnabled(null);
			setFilterOtherUsersResourcesEnabled(null);
    		setAddEnabled(null);
			setDeleteEnabled(null);
			resetAdmins();
			resetSuperadmins();
			resetEditResourcesAllowed();
			setDeleteOtherUsersResourcesEnabled(null);
			setDeleteAdminsResourcesEnabled(null);
			setDeleteSuperadminsResourcesEnabled(null);
			resetDeleteResourcesAllowed();
			
			// Reload categories from DB
			resourcesCategories=null;
		}
		return newView;
	}
	
	// ActionListener para la eliminación de un recurso
	/**
	 * Deletes a resource.
	 * @param event Action event
	 */
	public void deleteResource(ActionEvent event)
	{
		try
		{
			// Get current user session Hibernate operation
			Operation operation=getCurrentUserOperation(null);
			
			// Check permission to delete resource
			setDeleteEnabled(null);
			setDeleteOtherUsersResourcesEnabled(null);
			setDeleteAdminsResourcesEnabled(null);
			setDeleteSuperadminsResourcesEnabled(null);
			Resource resource=getResource(operation,getResourceId());
			resetDeleteResourceAllowed(resource);
			resetAdminFromResourceAllowed(resource);
			resetSuperadminFromResourceAllowed(resource);
			if (isDeleteResourceAllowed(operation,getResourceId()))
			{
				try
				{
					// Delete resource
					resourcesService.deleteResource(getResourceId());
				}
				finally
				{
					// We force to reload resources from DB
					resources=null;
					
					// End current user session Hibernate operation
					userSessionService.endCurrentUserOperation();
				}
			}
			else
			{
				addErrorMessage(true,"INCORRECT_OPERATION","NON_AUTHORIZED_ACTION_ERROR");
				setAddEnabled(null);
				setEditEnabled(null);
				resetAdmins();
				resetSuperadmins();
				setEditOtherUsersResourcesEnabled(null);
				setEditAdminsResourcesEnabled(null);
				setEditSuperadminsResourcesEnabled(null);
				resetEditResourcesAllowed();
				resetDeleteResourcesAllowed();
				
				// Reload categories from DB
				resourcesCategories=null;
			}
		}
		catch (ServiceException se)
		{
			addErrorMessage(true,"INCORRECT_OPERATION","RESOURCE_DELETE_ERROR");
			setAddEnabled(null);
			setEditEnabled(null);
			setDeleteEnabled(null);
			resetAdmins();
			resetSuperadmins();
			setEditOtherUsersResourcesEnabled(null);
			setEditAdminsResourcesEnabled(null);
			setEditSuperadminsResourcesEnabled(null);
			resetEditResourcesAllowed();
			setDeleteOtherUsersResourcesEnabled(null);
			setDeleteAdminsResourcesEnabled(null);
			setDeleteSuperadminsResourcesEnabled(null);
			resetDeleteResourcesAllowed();
			
			// Reload categories from DB
			resourcesCategories=null;
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
