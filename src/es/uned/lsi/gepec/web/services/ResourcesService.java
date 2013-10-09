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
package es.uned.lsi.gepec.web.services;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.context.FacesContext;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;

import org.hibernate.exception.ConstraintViolationException;

import es.uned.lsi.gepec.model.dao.DaoException;
import es.uned.lsi.gepec.model.dao.ResourcesDao;
import es.uned.lsi.gepec.model.entities.Category;
import es.uned.lsi.gepec.model.entities.CategoryType;
import es.uned.lsi.gepec.model.entities.Resource;
import es.uned.lsi.gepec.model.entities.User;
import es.uned.lsi.gepec.util.HibernateUtil.Operation;
import es.uned.lsi.gepec.util.HibernateUtil;
import es.uned.lsi.gepec.util.ProxyConnector;
import es.uned.lsi.gepec.web.ResourceBean;

//Ofrece a la vista operaciones con recursos
//author Víctor Manuel Alonso Rodríguez
//since  12/2011
/**
 * Manages resources.
 */
@SuppressWarnings("serial")
@ManagedBean(eager=true)
@ApplicationScoped
public class ResourcesService implements Serializable
{
	private final static int BACKUP_BUFFER_SIZE=8192;
	private final static int UPLOAD_BUFFER_SIZE=8192;
	
	private final static Map<String,String> SUPPORTED_MIMES;
	static
	{
		SUPPORTED_MIMES=new HashMap<String,String>();
		SUPPORTED_MIMES.put("png","image/png");
		SUPPORTED_MIMES.put("jpg","image/jpeg");
		SUPPORTED_MIMES.put("jpe","image/jpeg");
		SUPPORTED_MIMES.put("jpeg","image/jpeg");
		SUPPORTED_MIMES.put("bmp","image/bmp");
		SUPPORTED_MIMES.put("wbmp","image/vnd.wap.wbmp");
		SUPPORTED_MIMES.put("gif","image/gif");
	}
	
	private static ResourcesDao RESOURCES_DAO=new ResourcesDao();
	@ManagedProperty(value="#{configurationService}")
	private ConfigurationService configurationService;
	@ManagedProperty(value="#{localizationService}")
	private LocalizationService localizationService;
	@ManagedProperty(value="#{categoriesService}")
	private CategoriesService categoriesService;
	@ManagedProperty(value="#{categoryTypesService}")
	private CategoryTypesService categoryTypesService;
	@ManagedProperty(value="#{visibilitiesService}")
	private VisibilitiesService visibilitiesService;
	@ManagedProperty(value="#{permissionsService}")
	private PermissionsService permissionsService;
	
	public ResourcesService()
	{
	}
	
	public void setConfigurationService(ConfigurationService configurationService)
	{
		this.configurationService=configurationService;
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
	
	public void setPermissionsService(PermissionsService permissionsService)
	{
		this.permissionsService=permissionsService;
	}
	
	/**
	 * @return List of all MIME types supported by GEPEQ (and OM)
	 */
	public List<String> getSupportedMIMETypes()
	{
		List<String> supportedMIMETypes=new ArrayList<String>();
		for (String mimeType:SUPPORTED_MIMES.values())
		{
			if (!supportedMIMETypes.contains(mimeType))
			{
				supportedMIMETypes.add(mimeType);
			}
		}
		Collections.sort(supportedMIMETypes);
		return supportedMIMETypes;
	}
	
	/**
	 * @param operation Operation
	 * @param mimeType Mime type
	 * @return Category type associated to that MIME type
	 * @throws ServiceException
	 */
	private CategoryType getCategoryTypeByMIMEType(Operation operation,String mimeType) throws ServiceException
	{
		CategoryType categoryType=null;
		if (mimeType!=null && mimeType.startsWith("image/"))
		{
			categoryType=categoryTypesService.getCategoryType(operation,"CATEGORY_TYPE_IMAGES");
		}
		else
		{
			//TODO cuando haya más tipos de recursos esto sera para un tipo que los englobe como por ejemplo CATEGORY_TYPE_RESOURCES
			categoryType=categoryTypesService.getCategoryType(operation,"CATEGORY_TYPE_IMAGES");
		}
		return categoryType;
	}
	
	/**
	 * @param operation Operation
	 * @param viewer User used to check visibility
	 * @param user User or null to get resources from all users
	 * @param categoryId Filtering category identifier or 0 to get resources from all categories
	 * @param includeSubcategories Include resources from categories derived from filtering category
	 * @param mimeType Filtering MIME type or empty string to get resources with all MIME types
	 * @param copyrightId Filtering copyright identifier or 0 to get resources with all copyrights
	 * @param sortedByName Flag to indicate if we want the results sorted by name
	 * @return List of resources filtered by user, category (and optionally subcategories) and MIME type 
	 * and optionally sorted by name
	 * @throws ServiceException
	 */
	private List<Resource> getResources(Operation operation,User viewer,User user,long categoryId,
		boolean includeSubcategories,String mimeType,long copyrightId,boolean sortedByName) throws ServiceException
	{
		boolean singleOp=operation==null;
		List<Resource> resources=null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			if (categoryId>0L)
			{
				// Check permissions to determine if we need to check categories visibility
				Category category=categoriesService.getCategory(operation,categoryId);
				category.setVisibility(visibilitiesService.getVisibilityFromCategoryId(operation,categoryId));
				boolean checkVisibility=permissionsService.isDenied(operation,viewer,
					"PERMISSION_RESOURCES_VIEW_RESOURCES_OF_OTHER_USERS_PRIVATE_CATEGORIES_ENABLED") ||
					(permissionsService.isGranted(operation,category.getUser(),"PERMISSION_NAVIGATION_ADMINISTRATION") 
					&& permissionsService.isDenied(operation,viewer,
					"PERMISSION_RESOURCES_VIEW_RESOURCES_OF_ADMINS_PRIVATE_CATEGORIES_ENABLED")) ||
					(permissionsService.isGranted(operation,category.getUser(),
					"PERMISSION_ADMINISTRATION_RAISE_PERMISSIONS_OVER_OWNED_ALLOWED") &&
					permissionsService.isDenied(operation,viewer,
					"PERMISSION_RESOURCES_VIEW_RESOURCES_OF_SUPERADMINS_PRIVATE_CATEGORIES_ENABLED"));
				if (checkVisibility && !category.getUser().equals(viewer)&& !category.getVisibility().isGlobal() && 
					category.getVisibility().getLevel()>=visibilitiesService.getVisibility(
					operation,"CATEGORY_VISIBILITY_PRIVATE").getLevel())
				{
					throwServiceException(
						"NON_AUTHORIZED_ACTION_ERROR","You are not authorized to execute that operation.");
				}
			}
			
			List<Resource> resourcesFromDB=null;
			if (!includeSubcategories || categoryId<=0L)
			{
				// We get resources from DB
				RESOURCES_DAO.setOperation(operation);
				resourcesFromDB=RESOURCES_DAO.getResources(
					user==null?0L:user.getId(),categoryId,mimeType,copyrightId,sortedByName,true,true,true);
				
				// We return resources within a new list to avoid shared collection references
				resources=new ArrayList<Resource>(resourcesFromDB.size());
				for (Resource resourceFromDB:resourcesFromDB)
				{
					resources.add(resourceFromDB);
				}
			}
			else
			{
				// We get resources from DB
				RESOURCES_DAO.setOperation(operation);
				resourcesFromDB=RESOURCES_DAO.getResources(user==null?0L:user.getId(),
					categoriesService.getDerivedCategoriesIds(operation,categoryId,viewer,
					getCategoryTypeByMIMEType(operation,mimeType)),mimeType,copyrightId,sortedByName,true,true,true);
			}
			
			Map<Category,Boolean> checkedCategoriesVisibility=new HashMap<Category,Boolean>();
			
			// We return new referenced resources within a new list to avoid shared collection references
			// and object references to unsaved transient instances
			resources=new ArrayList<Resource>();
			for (Resource resourceFromDB:resourcesFromDB)
			{
				Resource resource=null;
				if (resourceFromDB.getCategory()!=null)
				{
					Category categoryFromDB=resourceFromDB.getCategory();
					boolean checkedCategoryVisibility=true;						
					if (checkedCategoriesVisibility.containsKey(categoryFromDB))
					{
						checkedCategoryVisibility=checkedCategoriesVisibility.get(categoryFromDB).booleanValue();
					}
					else
					{
						boolean checkResourceVisibility=permissionsService.isDenied(operation,viewer,
							"PERMISSION_RESOURCES_VIEW_RESOURCES_OF_OTHER_USERS_PRIVATE_CATEGORIES_ENABLED") ||
							(permissionsService.isGranted(operation,categoryFromDB.getUser(),
							"PERMISSION_NAVIGATION_ADMINISTRATION") && permissionsService.isDenied(operation,viewer,
							"PERMISSION_RESOURCES_VIEW_RESOURCES_OF_ADMINS_PRIVATE_CATEGORIES_ENABLED")) ||
							(permissionsService.isGranted(operation,categoryFromDB.getUser(),
							"PERMISSION_ADMINISTRATION_RAISE_PERMISSIONS_OVER_OWNED_ALLOWED") &&
							permissionsService.isDenied(operation,viewer,
							"PERMISSION_RESOURCES_VIEW_RESOURCES_OF_SUPERADMINS_PRIVATE_CATEGORIES_ENABLED"));
						if (checkResourceVisibility && !categoryFromDB.getUser().equals(viewer)&& 
							!categoryFromDB.getVisibility().isGlobal() && 
							categoryFromDB.getVisibility().getLevel()>=visibilitiesService.getVisibility(
							operation,"CATEGORY_VISIBILITY_PRIVATE").getLevel())
						{
							checkedCategoryVisibility=false;
						}
						checkedCategoriesVisibility.put(categoryFromDB,Boolean.valueOf(checkedCategoryVisibility));
					}
					if (checkedCategoryVisibility)
					{
						resource=resourceFromDB.getResourceCopy();
						Category category=categoryFromDB.getCategoryCopy();
						if (categoryFromDB.getUser()!=null)
						{
							User categoryUser=categoryFromDB.getUser().getUserCopy();
							
							// Password is set to empty string before returning instance for security reasons
							categoryUser.setPassword("");
							
							category.setUser(categoryUser);
						}
						if (categoryFromDB.getCategoryType()!=null)
						{
							category.setCategoryType(categoryFromDB.getCategoryType().getCategoryTypeCopy());
						}
						if (categoryFromDB.getVisibility()!=null)
						{
							category.setVisibility(categoryFromDB.getVisibility().getVisibilityCopy());
						}
						resource.setCategory(category);
					}
				}
				if (resource!=null)
				{
					if (resourceFromDB.getUser()!=null)
					{
						User resourceUser=resourceFromDB.getUser().getUserCopy();
						
						// Password is set to empty string before returning instance for security reasons
						resourceUser.setPassword("");
						
						resource.setUser(resourceUser);
					}
					if (resourceFromDB.getCopyright()!=null)
					{
						resource.setCopyright(resourceFromDB.getCopyright().getCopyrightCopy());
					}
					resources.add(resource);
				}
			}
		}
		catch (DaoException de)
		{
			throw new ServiceException(de.getMessage(),de);
		}
		finally
		{
			if (singleOp)
			{
				// End Hibernate operation
				HibernateUtil.endOperation(operation);
			}
		}
		return resources;
	}
	
	//Obtiene todos los recursos de un usuario
	/**
	 * @param user User or null to get resources from all users
	 * @return List of resources filtered by user
	 * @throws ServiceException
	 */
	public List<Resource> getResources(User user) throws ServiceException
	{
		return getResources((Operation)null,user);
	}
	
	/**
	 * @param operation Operation
	 * @param user User or null to get resources from all users
	 * @return List of resources filtered by user
	 * @throws ServiceException
	 */
	public List<Resource> getResources(Operation operation,User user) throws ServiceException
	{
		boolean singleOp=operation==null;
		List<Resource> resources=null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			resources=getResources(operation,getCurrentUser(operation),user,0L,false,"",0L,false);
		}
		finally
		{
			if (singleOp)
			{
				// End Hibernate operation
				HibernateUtil.endOperation(operation);
			}
		}
		return resources;
	}
	
	/**
	 * @param user User or null to get resources from all users
	 * @param categoryId Filtering category identifier or 0 to get resources from all categories
	 * @param includeSubcategories Include resources from categories derived from filtering category
	 * @return List of resources filtered by user and category (and optionally subcategories)
	 * @throws ServiceException
	 */
	public List<Resource> getResources(User user,long categoryId,boolean includeSubcategories) throws ServiceException
	{
		return getResources((Operation)null,user,categoryId,includeSubcategories);
	}
	
	/**
	 * @param operation Operation
	 * @param user User or null to get resources from all users
	 * @param categoryId Filtering category identifier or 0 to get resources from all categories
	 * @param includeSubcategories Include resources from categories derived from filtering category
	 * @return List of resources filtered by user and category (and optionally subcategories)
	 * @throws ServiceException
	 */
	public List<Resource> getResources(Operation operation,User user,long categoryId,boolean includeSubcategories)
		throws ServiceException
	{
		boolean singleOp=operation==null;
		List<Resource> resources=null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			resources=
				getResources(operation,getCurrentUser(operation),user,categoryId,includeSubcategories,"",0L,false);
		}
		finally
		{
			if (singleOp)
			{
				// End Hibernate operation
				HibernateUtil.endOperation(operation);
			}
		}
		return resources;
	}
	
	/**
	 * @param user User or null to get resources from all users
	 * @param categoryId Filtering category identifier or 0 to get resources from all categories
	 * @param includeSubcategories Include resources from categories derived from filtering category
	 * @param mimeType Filtering MIME type or empty string to get resources with all MIME types
	 * @param copyrightId Filtering copyright identifier or 0 to get resources with all copyrights
	 * @return List of resources filtered by user, category (and optionally subcategories) and MIME type
	 * @throws ServiceException
	 */
	public List<Resource> getResources(User user,long categoryId,boolean includeSubcategories,String mimeType,
		long copyrightId) throws ServiceException
	{
		return getResources((Operation)null,user,categoryId,includeSubcategories,mimeType,copyrightId);
	}
	
	/**
	 * @param operation Operation
	 * @param user User or null to get resources from all users
	 * @param categoryId Filtering category identifier or 0 to get resources from all categories
	 * @param includeSubcategories Include resources from categories derived from filtering category
	 * @param mimeType Filtering MIME type or empty string to get resources with all MIME types
	 * @param copyrightId Filtering copyright identifier or 0 to get resources with all copyrights
	 * @return List of resources filtered by user, category (and optionally subcategories) and MIME type
	 * @throws ServiceException
	 */
	public List<Resource> getResources(Operation operation,User user,long categoryId,boolean includeSubcategories,
		String mimeType,long copyrightId) throws ServiceException
	{
		boolean singleOp=operation==null;
		List<Resource> resources=null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			resources=getResources(
				operation,getCurrentUser(operation),user,categoryId,includeSubcategories,mimeType,copyrightId,false);
		}
		finally
		{
			if (singleOp)
			{
				// End Hibernate operation
				HibernateUtil.endOperation(operation);
			}
		}
		return resources;
	}
	
	/**
	 * @param user User or null to get resources from all users
	 * @return List of resources filtered by user and sorted by name
	 * @throws ServiceException
	 */
	public List<Resource> getResourcesSortedByName(User user) throws ServiceException
	{
		return getResourcesSortedByName((Operation)null,user);
	}
	
	/**
	 * @param operation Operation
	 * @param user User or null to get resources from all users
	 * @return List of resources filtered by user and sorted by name
	 * @throws ServiceException
	 */
	public List<Resource> getResourcesSortedByName(Operation operation,User user) throws ServiceException
	{
		boolean singleOp=operation==null;
		List<Resource> resourcesSortedByName=null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			resourcesSortedByName=getResources(operation,getCurrentUser(operation),user,0L,false,"",0L,true);
		}
		finally
		{
			if (singleOp)
			{
				// End Hibernate operation
				HibernateUtil.endOperation(operation);
			}
		}
		return resourcesSortedByName; 
	}
	
	/**
	 * @param user User or null to get resources from all users
	 * @param categoryId Filtering category identifier or 0 to get resources from all categories
	 * @param includeSubcategories Include resources from categories derived from filtering category
	 * @return List of resources filtered by user and category (and optionally subcategories) 
	 * and sorted by name
	 * @throws ServiceException
	 */
	public List<Resource> getResourcesSortedByName(User user,long categoryId,boolean includeSubcategories)
		throws ServiceException
	{
		return getResourcesSortedByName((Operation)null,user,categoryId,includeSubcategories);
	}
	
	/**
	 * @param operation Operation
	 * @param user User or null to get resources from all users
	 * @param categoryId Filtering category identifier or 0 to get resources from all categories
	 * @param includeSubcategories Include resources from categories derived from filtering category
	 * @return List of resources filtered by user and category (and optionally subcategories) 
	 * and sorted by name
	 * @throws ServiceException
	 */
	public List<Resource> getResourcesSortedByName(Operation operation,User user,long categoryId,
		boolean includeSubcategories) throws ServiceException
	{
		boolean singleOp=operation==null;
		List<Resource> resourcesSortedByName=null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			resourcesSortedByName=
				getResources(operation,getCurrentUser(operation),user,categoryId,includeSubcategories,"",0L,true);
		}
		finally
		{
			if (singleOp)
			{
				// End Hibernate operation
				HibernateUtil.endOperation(operation);
			}
		}
		return resourcesSortedByName;
	}
	
	/**
	 * @param user User or null to get resources from all users
	 * @param categoryId Filtering category identifier or 0 to get resources from all categories
	 * @param includeSubcategories Include resources from categories derived from filtering category
	 * @param mimeType Filtering MIME type or empty string to get resources with all MIME types
	 * @param copyrightId Filtering copyright identifier or 0 to get resources with all copyrights
	 * @return List of resources filtered by user, category (and optionally subcategories) and MIME type 
	 * and sorted by name
	 * @throws ServiceException
	 */
	public List<Resource> getResourcesSortedByName(User user,long categoryId,boolean includeSubcategories,
		String mimeType,long copyrightId) throws ServiceException
	{
		return getResourcesSortedByName((Operation)null,user,categoryId,includeSubcategories,mimeType,copyrightId);
	}
	
	/**
	 * @param operation Operation
	 * @param user User or null to get resources from all users
	 * @param categoryId Filtering category identifier or 0 to get resources from all categories
	 * @param includeSubcategories Include resources from categories derived from filtering category
	 * @param mimeType Filtering MIME type or empty string to get resources with all MIME types
	 * @param copyrightId Filtering copyright identifier or 0 to get resources with all copyrights
	 * @return List of resources filtered by user, category (and optionally subcategories) and MIME type 
	 * and sorted by name
	 * @throws ServiceException
	 */
	public List<Resource> getResourcesSortedByName(Operation operation,User user,long categoryId,
		boolean includeSubcategories,String mimeType,long copyrightId) throws ServiceException
	{
		boolean singleOp=operation==null;
		List<Resource> resourcesSortedByName=null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			resourcesSortedByName=getResources(
				operation,getCurrentUser(operation),user,categoryId,includeSubcategories,mimeType,copyrightId,true);
		}
		finally
		{
			if (singleOp)
			{
				// End Hibernate operation
				HibernateUtil.endOperation(operation);
			}
		}
		return resourcesSortedByName;
	}
	
	/**
	 * @param operation Operation
	 * @param viewer User used to check visibility
	 * @param user User or null to get resources from all users
	 * @param mimeType Filtering MIME type or empty string to get resources with all MIME types
	 * @param copyrightId Filtering copyright identifier or 0 to get resources with all copyrights
	 * @param sortedByName Flag to indicate if we want the results sorted by name
	 * @return List of resources from all categories filtered by user and MIME type and optionally sorted
	 * by name
	 * @throws ServiceException
	 */
	private List<Resource> getAllCategoriesResources(Operation operation,User viewer,User user,String mimeType,
		long copyrightId,boolean sortedByName) throws ServiceException
	{
		List<Resource> allCategoriesResources=null;
		boolean singleOp=operation==null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			if (permissionsService.isGranted(operation,viewer,"PERMISSION_RESOURCES_GLOBAL_FILTER_ENABLED") &&
				permissionsService.isGranted(operation,viewer,"PERMISSION_RESOURCES_OTHER_USERS_FILTER_ENABLED") &&
				permissionsService.isGranted(operation,viewer,
				"PERMISSION_RESOURCES_VIEW_RESOURCES_OF_OTHER_USERS_PRIVATE_CATEGORIES_ENABLED"))
			{
				boolean includeAdminsPrivateCategories=permissionsService.isGranted(
					operation,viewer,"PERMISSION_RESOURCES_VIEW_RESOURCES_OF_ADMINS_PRIVATE_CATEGORIES_ENABLED");
				boolean includeSuperadminsPrivateCategories=permissionsService.isGranted(
					operation,viewer,"PERMISSION_RESOURCES_VIEW_RESOURCES_OF_SUPERADMINS_PRIVATE_CATEGORIES_ENABLED");
				List<Long> allCategoriesIds=categoriesService.getAllCategoriesIds(operation,viewer,
					getCategoryTypeByMIMEType(operation,mimeType),includeAdminsPrivateCategories,
					includeSuperadminsPrivateCategories);
				
				// We get resources from DB
				RESOURCES_DAO.setOperation(operation);
				List<Resource> allCategoriesResourcesFromDB=RESOURCES_DAO.getResources(
					user==null?0L:user.getId(),allCategoriesIds,mimeType,copyrightId,sortedByName,true,true,true);
				
				// We return new referenced resources from all categories within a new list to avoid 
				// shared collection references and object references to unsaved transient instances
				allCategoriesResources=new ArrayList<Resource>(allCategoriesResourcesFromDB.size());
				for (Resource allCategoriesResourceFromDB:allCategoriesResourcesFromDB)
				{
					Resource allCategoriesResource=allCategoriesResourceFromDB.getResourceCopy();
					if (allCategoriesResourceFromDB.getUser()!=null)
					{
						User allCategoriesResourceUser=allCategoriesResourceFromDB.getUser().getUserCopy();
						
						// Password is set to empty string before returning instance for security reasons
						allCategoriesResourceUser.setPassword("");
						
						allCategoriesResource.setUser(allCategoriesResourceUser);
					}
					if (allCategoriesResourceFromDB.getCategory()!=null)
					{
						Category categoryFromDB=allCategoriesResourceFromDB.getCategory();
						Category category=categoryFromDB.getCategoryCopy();
						if (categoryFromDB.getUser()!=null)
						{
							User categoryUser=categoryFromDB.getUser().getUserCopy();
							
							// Password is set to empty string before returning instance for security reasons
							categoryUser.setPassword("");
							
							category.setUser(categoryUser);
						}
						if (categoryFromDB.getCategoryType()!=null)
						{
							category.setCategoryType(categoryFromDB.getCategoryType().getCategoryTypeCopy());
						}
						if (categoryFromDB.getVisibility()!=null)
						{
							category.setVisibility(categoryFromDB.getVisibility().getVisibilityCopy());
						}
						allCategoriesResource.setCategory(category);
					}
					if (allCategoriesResourceFromDB.getCopyright()!=null)
					{
						allCategoriesResource.setCopyright(
							allCategoriesResourceFromDB.getCopyright().getCopyrightCopy());
					}
					allCategoriesResources.add(allCategoriesResource);
				}
			}
			else
			{
				throwServiceException(
					"NON_AUTHORIZED_ACTION_ERROR","You are not authorized to execute that operation.");
			}
		}
		catch (DaoException de)
		{
			throw new ServiceException(de.getMessage(),de);
		}
		finally
		{
			if (singleOp)
			{
				// End Hibernate operation
				HibernateUtil.endOperation(operation);
			}
		}
		return allCategoriesResources; 
	}
	
	/**
	 * @param user User or null to get resources from all users
	 * @param mimeType Filtering MIME type or empty string to get resources with all MIME types
	 * @param copyrightId Filtering copyright identifier or 0 to get resources with all copyrights
	 * @return List of resources from all categories filtered by user and MIME type
	 * @throws ServiceException
	 */
	public List<Resource> getAllCategoriesResources(User user,String mimeType,long copyrightId)
		throws ServiceException
	{
		return getAllCategoriesResources((Operation)null,user,mimeType,copyrightId);
	}
	
	/**
	 * @param operation Operation
	 * @param user User or null to get resources from all users
	 * @param mimeType Filtering MIME type or empty string to get resources with all MIME types
	 * @param copyrightId Filtering copyright identifier or 0 to get resources with all copyrights
	 * @return List of resources from all categories filtered by user and MIME type
	 * @throws ServiceException
	 */
	public List<Resource> getAllCategoriesResources(Operation operation,User user,String mimeType,long copyrightId) 
		throws ServiceException
	{
		boolean singleOp=operation==null;
		List<Resource> allCategoriesResources=null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			allCategoriesResources=
				getAllCategoriesResources(operation,getCurrentUser(operation),user,mimeType,copyrightId,false);
		}
		finally
		{
			if (singleOp)
			{
				// End Hibernate operation
				HibernateUtil.endOperation(operation);
			}
		}
		return allCategoriesResources;
	}
	
	/**
	 * @param user User or null to get resources from all users
	 * @param mimeType Filtering MIME type or empty string to get resources with all MIME types
	 * @param copyrightId Filtering copyright identifier or 0 to get resources with all copyrights
	 * @return List of resources from all categories filtered by user and MIME type and sorted by name
	 * @throws ServiceException
	 */
	public List<Resource> getAllCategoriesResourcesSortedByName(User user,String mimeType,long copyrightId) 
		throws ServiceException
	{
		return getAllCategoriesResourcesSortedByName((Operation)null,user,mimeType,copyrightId);
	}
	
	/**
	 * @param operation Operation
	 * @param user User or null to get resources from all users
	 * @param mimeType Filtering MIME type or empty string to get resources with all MIME types
	 * @param copyrightId Filtering copyright identifier or 0 to get resources with all copyrights
	 * @return List of resources from all categories filtered by user and MIME type and sorted by name
	 * @throws ServiceException
	 */
	public List<Resource> getAllCategoriesResourcesSortedByName(Operation operation,User user,String mimeType,
		long copyrightId) throws ServiceException
	{
		boolean singleOp=operation==null;
		List<Resource> allCategoriesResourcesSortedByName=null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			allCategoriesResourcesSortedByName=
				getAllCategoriesResources(operation,getCurrentUser(operation),user,mimeType,copyrightId,true);
		}
		finally
		{
			if (singleOp)
			{
				// End Hibernate operation
				HibernateUtil.endOperation(operation);
			}
		}
		return allCategoriesResourcesSortedByName;
	}
	
	/**
	 * @param operation Operation
	 * @param viewer User used to check visibility
	 * @param user User or null to get resources from all users
	 * @param mimeType Filtering MIME type or empty string to get resources with all MIME types
	 * @param copyrightId Filtering copyright identifier or 0 to get resources with all copyrights
	 * @param sortedByName Flag to indicate if we want the results sorted by name
	 * @return List of resources from all visible categories filtered by user and MIME type 
	 * and optionally sorted by name
	 * @throws ServiceException
	 */
	private List<Resource> getAllVisibleCategoriesResources(Operation operation,User viewer,User user,String mimeType,
		long copyrightId,boolean sortedByName) throws ServiceException
	{
		List<Resource> allVisibleCategoriesResources=null;
		boolean singleOp=operation==null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			if (permissionsService.isGranted(operation,viewer,"PERMISSION_RESOURCES_GLOBAL_FILTER_ENABLED") &&
				permissionsService.isGranted(operation,viewer,"PERMISSION_RESOURCES_OTHER_USERS_FILTER_ENABLED"))
			{
				List<Long> allVisibleCategoriesIds=categoriesService.getAllVisibleCategoriesIds(
					operation,viewer,getCategoryTypeByMIMEType(operation,mimeType));
				
				// We get answers from DB
				RESOURCES_DAO.setOperation(operation);
				List<Resource> allVisibleCategoriesResourcesFromDB=RESOURCES_DAO.getResources(
					user==null?0L:user.getId(),allVisibleCategoriesIds,mimeType,copyrightId,sortedByName,true,true,
					true);
				
				// We return new referenced resources from all visible categories within a new list to avoid 
				// shared collection references and object references to unsaved transient instances
				allVisibleCategoriesResources=new ArrayList<Resource>(allVisibleCategoriesResourcesFromDB.size());
				for (Resource allVisibleCategoriesResourceFromDB:allVisibleCategoriesResourcesFromDB)
				{
					Resource allVisibleCategoriesResource=allVisibleCategoriesResourceFromDB.getResourceCopy();
					if (allVisibleCategoriesResourceFromDB.getUser()!=null)
					{
						User allVisibleCategoriesResourceUser=
							allVisibleCategoriesResourceFromDB.getUser().getUserCopy();
						
						// Password is set to empty string before returning instance for security reasons
						allVisibleCategoriesResourceUser.setPassword("");
						
						allVisibleCategoriesResource.setUser(allVisibleCategoriesResourceUser);
					}
					if (allVisibleCategoriesResourceFromDB.getCategory()!=null)
					{
						Category categoryFromDB=allVisibleCategoriesResourceFromDB.getCategory();
						Category category=categoryFromDB.getCategoryCopy();
						if (categoryFromDB.getUser()!=null)
						{
							User categoryUser=categoryFromDB.getUser().getUserCopy();
							
							// Password is set to empty string before returning instance for security reasons
							categoryUser.setPassword("");
							
							category.setUser(categoryUser);
						}
						if (categoryFromDB.getCategoryType()!=null)
						{
							category.setCategoryType(categoryFromDB.getCategoryType().getCategoryTypeCopy());
						}
						if (categoryFromDB.getVisibility()!=null)
						{
							category.setVisibility(categoryFromDB.getVisibility().getVisibilityCopy());
						}
						allVisibleCategoriesResource.setCategory(category);
					}
					allVisibleCategoriesResources.add(allVisibleCategoriesResource);
				}
			}
			else
			{
				throwServiceException(
					"NON_AUTHORIZED_ACTION_ERROR","You are not authorized to execute that operation.");
			}
		}
		catch (DaoException de)
		{
			throw new ServiceException(de.getMessage(),de);
		}
		finally
		{
			if (singleOp)
			{
				// End Hibernate operation
				HibernateUtil.endOperation(operation);
			}
		}
		return allVisibleCategoriesResources; 
	}
	
	/**
	 * @param user User or null to get resources from all users
	 * @param mimeType Filtering MIME type or empty string to get resources with all MIME types
	 * @param copyrightId Filtering copyright identifier or 0 to get resources with all copyrights
	 * @return List of resources from all visible categories filtered by user and MIME type
	 * @throws ServiceException
	 */
	public List<Resource> getAllVisibleCategoriesResources(User user,String mimeType,long copyrightId)
		throws ServiceException
	{
		return getAllVisibleCategoriesResources((Operation)null,user,mimeType,copyrightId);
	}
	
	/**
	 * @param operation Operation
	 * @param user User or null to get resources from all users
	 * @param mimeType Filtering MIME type or empty string to get resources with all MIME types
	 * @param copyrightId Filtering copyright identifier or 0 to get resources with all copyrights
	 * @return List of resources from all visible categories filtered by user and MIME type
	 * @throws ServiceException
	 */
	public List<Resource> getAllVisibleCategoriesResources(Operation operation,User user,String mimeType,
		long copyrightId) throws ServiceException
	{
		boolean singleOp=operation==null;
		List<Resource> allVisibleCategoriesResources=null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			allVisibleCategoriesResources=
				getAllVisibleCategoriesResources(operation,getCurrentUser(operation),user,mimeType,copyrightId,false);
		}
		finally
		{
			if (singleOp)
			{
				// End Hibernate operation
				HibernateUtil.endOperation(operation);
			}
		}
		return allVisibleCategoriesResources;
	}
	
	/**
	 * @param user User or null to get resources from all users
	 * @param mimeType Filtering MIME type or empty string to get resources with all MIME types
	 * @param copyrightId Filtering copyright identifier or 0 to get resources with all copyrights
	 * @return List of resources from all visible categories filtered by user and MIME type and sorted by name
	 * @throws ServiceException
	 */
	public List<Resource> getAllVisibleCategoriesResourcesSortedByName(User user,String mimeType,long copyrightId) 
		throws ServiceException
	{
		return getAllVisibleCategoriesResourcesSortedByName((Operation)null,user,mimeType,copyrightId);
	}
	
	/**
	 * @param operation Operation
	 * @param user User or null to get resources from all users
	 * @param mimeType Filtering MIME type or empty string to get resources with all MIME types
	 * @param copyrightId Filtering copyright identifier or 0 to get resources with all copyrights
	 * @return List of resources from all visible categories filtered by user and MIME type and sorted by name
	 * @throws ServiceException
	 */
	public List<Resource> getAllVisibleCategoriesResourcesSortedByName(Operation operation,User user,String mimeType,
		long copyrightId) throws ServiceException
	{
		boolean singleOp=operation==null;
		List<Resource> allVisibleCategoriesResourcesSortedByName=null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			allVisibleCategoriesResourcesSortedByName=
				getAllVisibleCategoriesResources(operation,getCurrentUser(operation),user,mimeType,copyrightId,true);
		}
		finally
		{
			if (singleOp)
			{
				// End Hibernate operation
				HibernateUtil.endOperation(operation);
			}
		}
		return allVisibleCategoriesResourcesSortedByName;
	}
	
	/**
	 * @param operation Operation
	 * @param viewer User used to check visibility
	 * @param owner Owner of categories from which we get the resources
	 * @param user User or null to get resources from all users
	 * @param mimeType Filtering MIME type or empty string to get resources with all MIME types
	 * @param copyrightId Filtering copyright identifier or 0 to get resources with all copyrights
	 * @param sortedByName Flag to indicate if we want the results sorted by name
	 * @return List of resources from all categories of an user (including its global categories) filtered by user 
	 * and MIME type and optionally sorted by name
	 * @throws ServiceException
	 */
	private List<Resource> getAllUserCategoriesResources(Operation operation,User viewer,User owner,User user,
		String mimeType,long copyrightId,boolean sortedByName) throws ServiceException
	{
		List<Resource> allUserCategoriesResources=null;
		boolean singleOp=operation==null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			if (permissionsService.isGranted(operation,viewer,"PERMISSION_RESOURCES_GLOBAL_FILTER_ENABLED") &&
				((viewer.equals(owner)) || 
				permissionsService.isGranted(operation,viewer,"PERMISSION_RESOURCES_OTHER_USERS_FILTER_ENABLED")))
			{
				List<Long> allUserCategoriesIds=categoriesService.getAllUserCategoriesIds(
					operation,owner,getCategoryTypeByMIMEType(operation,mimeType));
				
				// We get resources from DB
				RESOURCES_DAO.setOperation(operation);
				List<Resource> allUserCategoriesResourcesFromDB=RESOURCES_DAO.getResources(
					user==null?0L:user.getId(),allUserCategoriesIds,mimeType,copyrightId,sortedByName,true,true,true);
				
				// We return new referenced resources from all categories of owner within a new list 
				// to avoid shared collection references and object references to unsaved transient instances
				allUserCategoriesResources=new ArrayList<Resource>(allUserCategoriesResourcesFromDB.size());
				for (Resource allUserCategoriesResourceFromDB:allUserCategoriesResourcesFromDB)
				{
					Resource allUserCategoriesResource=allUserCategoriesResourceFromDB.getResourceCopy();
					if (allUserCategoriesResourceFromDB.getUser()!=null)
					{
						User allUserCategoriesResourceUser=allUserCategoriesResourceFromDB.getUser().getUserCopy();
						
						// Password is set to empty string before returning instance for security reasons
						allUserCategoriesResourceUser.setPassword("");
						
						allUserCategoriesResource.setUser(allUserCategoriesResourceUser);
					}
					if (allUserCategoriesResourceFromDB.getCategory()!=null)
					{
						Category categoryFromDB=allUserCategoriesResourceFromDB.getCategory();
						Category category=categoryFromDB.getCategoryCopy();
						if (categoryFromDB.getUser()!=null)
						{
							User categoryUser=categoryFromDB.getUser().getUserCopy();
							
							// Password is set to empty string before returning instance for security reasons
							categoryUser.setPassword("");
							
							category.setUser(categoryUser);
						}
						if (categoryFromDB.getCategoryType()!=null)
						{
							category.setCategoryType(categoryFromDB.getCategoryType().getCategoryTypeCopy());
						}
						if (categoryFromDB.getVisibility()!=null)
						{
							category.setVisibility(categoryFromDB.getVisibility().getVisibilityCopy());
						}
						allUserCategoriesResource.setCategory(category);
					}
					if (allUserCategoriesResourceFromDB.getCopyright()!=null)
					{
						allUserCategoriesResource.setCopyright(
							allUserCategoriesResourceFromDB.getCopyright().getCopyrightCopy());
					}
					allUserCategoriesResources.add(allUserCategoriesResource);
				}
			}
			else
			{
				throwServiceException(
					"NON_AUTHORIZED_ACTION_ERROR","You are not authorized to execute that operation.");
			}
		}
		catch (DaoException de)
		{
			throw new ServiceException(de.getMessage(),de);
		}
		finally
		{
			if (singleOp)
			{
				// End Hibernate operation
				HibernateUtil.endOperation(operation);
			}
		}
		return allUserCategoriesResources;
	}
	
	/**
	 * @param owner Owner of categories from which we get the resources
	 * @param user User or null to get resources from all users
	 * @param mimeType Filtering MIME type or empty string to get resources with all MIME types
	 * @param copyrightId Filtering copyright identifier or 0 to get resources with all copyrights
	 * @return List of resources from all categories of an user (including its global categories) filtered by user 
	 * and MIME type
	 * @throws ServiceException
	 */
	public List<Resource> getAllUserCategoriesResources(User owner,User user,String mimeType,long copyrightId)
		throws ServiceException
	{
		return getAllUserCategoriesResources((Operation)null,owner,user,mimeType,copyrightId);
	}
	
	/**
	 * @param operation Operation
	 * @param owner Owner of categories from which we get the resources
	 * @param user User or null to get resources from all users
	 * @param mimeType Filtering MIME type or empty string to get resources with all MIME types
	 * @param copyrightId Filtering copyright identifier or 0 to get resources with all copyrights
	 * @return List of resources from all categories of an user (including its global categories) filtered by user 
	 * and MIME type
	 * @throws ServiceException
	 */
	public List<Resource> getAllUserCategoriesResources(Operation operation,User owner,User user,String mimeType,
		long copyrightId) throws ServiceException
	{
		boolean singleOp=operation==null;
		List<Resource> allUserCategoriesResources=null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			allUserCategoriesResources=getAllUserCategoriesResources(
				operation,getCurrentUser(operation),owner,user,mimeType,copyrightId,false);
		}
		finally
		{
			if (singleOp)
			{
				// End Hibernate operation
				HibernateUtil.endOperation(operation);
			}
		}
		return allUserCategoriesResources;
	}
	
	/**
	 * @param owner Owner of categories from which we get the resources
	 * @param user User or null to get resources from all users
	 * @param mimeType Filtering MIME type or empty string to get resources with all MIME types
	 * @param copyrightId Filtering copyright identifier or 0 to get resources with all copyrights
	 * @return List of resources from all categories of an user (including its global categories) filtered by user 
	 * and MIME type and sorted by name
	 * @throws ServiceException
	 */
	public List<Resource> getAllUserCategoriesResourcesSortedByName(User owner,User user,String mimeType,
		long copyrightId) throws ServiceException
	{
		return getAllUserCategoriesResourcesSortedByName((Operation)null,owner,user,mimeType,copyrightId);
	}
	
	/**
	 * @param operation Operation
	 * @param owner Owner of categories from which we get the resources
	 * @param user User or null to get resources from all users
	 * @param mimeType Filtering MIME type or empty string to get resources with all MIME types
	 * @param copyrightId Filtering copyright identifier or 0 to get resources with all copyrights
	 * @return List of resources from all categories of an user (including its global categories) filtered by user 
	 * and MIME type and sorted by name
	 * @throws ServiceException
	 */
	public List<Resource> getAllUserCategoriesResourcesSortedByName(Operation operation,User owner,User user,
		String mimeType,long copyrightId) throws ServiceException
	{
		boolean singleOp=operation==null;
		List<Resource> allUserCategoriesResourcesSortedByName=null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			allUserCategoriesResourcesSortedByName=getAllUserCategoriesResources(
				operation,getCurrentUser(operation),owner,user,mimeType,copyrightId,true);
		}
		finally
		{
			if (singleOp)
			{
				// End Hibernate operation
				HibernateUtil.endOperation(operation);
			}
		}
		return allUserCategoriesResourcesSortedByName;
	}
	
	/**
	 * @param user User or null to get resources from all users
	 * @param mimeType Filtering MIME type or empty string to get resources with all MIME types
	 * @param copyrightId Filtering copyright identifier or 0 to get resources with all copyrights
	 * @return List of resources from all categories of current user (including its global categories) 
	 * filtered by user and MIME type
	 * @throws ServiceException
	 */
	public List<Resource> getAllMyCategoriesResources(User user,String mimeType,long copyrightId)
		throws ServiceException
	{
		return getAllMyCategoriesResources((Operation)null,user,mimeType,copyrightId);
	}
	
	/**
	 * @param operation Operation
	 * @param user User or null to get resources from all users
	 * @param mimeType Filtering MIME type or empty string to get resources with all MIME types
	 * @param copyrightId Filtering copyright identifier or 0 to get resources with all copyrights
	 * @return List of resources from all categories of current user (including its global categories) 
	 * filtered by user and MIME type
	 * @throws ServiceException
	 */
	public List<Resource> getAllMyCategoriesResources(Operation operation,User user,String mimeType,long copyrightId) 
		throws ServiceException
	{
		boolean singleOp=operation==null;
		List<Resource> allMyCategoriesResources=null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			User currentUser=getCurrentUser(operation);
			allMyCategoriesResources=
				getAllUserCategoriesResources(operation,currentUser,currentUser,user,mimeType,copyrightId,false);
		}
		finally
		{
			if (singleOp)
			{
				// End Hibernate operation
				HibernateUtil.endOperation(operation);
			}
		}
		return allMyCategoriesResources;
	}
	
	/**
	 * @param user User or null to get resources from all users
	 * @param mimeType Filtering MIME type or empty string to get resources with all MIME types
	 * @param copyrightId Filtering copyright identifier or 0 to get resources with all copyrights
	 * @return List of resources from all categories of current user (including its global categories) 
	 * filtered by user and MIME type and sorted by name
	 * @throws ServiceException
	 */
	public List<Resource> getAllMyCategoriesResourcesSortedByName(User user,String mimeType,long copyrightId)
		throws ServiceException
	{
		return getAllMyCategoriesResourcesSortedByName((Operation)null,user,mimeType,copyrightId);
	}
	
	/**
	 * @param operation Operation
	 * @param user User or null to get resources from all users
	 * @param mimeType Filtering MIME type or empty string to get resources with all MIME types
	 * @param copyrightId Filtering copyright identifier or 0 to get resources with all copyrights
	 * @return List of resources from all categories of current user (including its global categories) 
	 * filtered by user and MIME type and sorted by name
	 * @throws ServiceException
	 */
	public List<Resource> getAllMyCategoriesResourcesSortedByName(Operation operation,User user,String mimeType,
		long copyrightId) throws ServiceException
	{
		boolean singleOp=operation==null;
		List<Resource> allMyCategoriesResourcesSortedByName=null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			User currentUser=getCurrentUser(operation);
			allMyCategoriesResourcesSortedByName=
				getAllUserCategoriesResources(operation,currentUser,currentUser,user,mimeType,copyrightId,true);
		}
		finally
		{
			if (singleOp)
			{
				// End Hibernate operation
				HibernateUtil.endOperation(operation);
			}
		}
		return allMyCategoriesResourcesSortedByName;
	}
	
	/**
	 * @param operation Operation
	 * @param viewer User used to check visibility
	 * @param owner Owner of categories from which we get the resources
	 * @param user User or null to get resources from all users
	 * @param mimeType Filtering MIME type or empty string to get resources with all MIME types
	 * @param copyrightId Filtering copyright identifier or 0 to get resources with all copyrights
	 * @param sortedByName Flag to indicate if we want the results sorted by name
	 * @return List of resources from all categories of an user (except global categories) filtered by user 
	 * and MIME type and optionally sorted by name
	 * @throws ServiceException
	 */
	private List<Resource> getAllUserCategoriesExceptGlobalsResources(Operation operation,User viewer,User owner,
		User user,String mimeType,long copyrightId,boolean sortedByName) throws ServiceException
	{
		List<Resource> allUserCategoriesExceptGlobalsResources=null;
		boolean singleOp=operation==null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			if (viewer.equals(owner) || 
				permissionsService.isGranted(operation,viewer,"PERMISSION_RESOURCES_OTHER_USERS_FILTER_ENABLED"))
			{
				List<Long> allUserCategoriesIdsExceptGlobalsIds=categoriesService.getAllUserCategoriesIdsExceptGlobals(
					operation,owner,getCategoryTypeByMIMEType(operation,mimeType));
				
				// We get resources from DB
				RESOURCES_DAO.setOperation(operation);
				List<Resource> allUserCategoriesExceptGlobalsResourcesFromDB=RESOURCES_DAO.getResources(
					user==null?0L:user.getId(),allUserCategoriesIdsExceptGlobalsIds,mimeType,copyrightId,sortedByName,
					true,true,true);
				
				// We return new referenced resources from all categories of owner (except global categories) 
				// within a new list to avoid shared collection references and object references 
				// to unsaved transient instances
				allUserCategoriesExceptGlobalsResources=
					new ArrayList<Resource>(allUserCategoriesExceptGlobalsResourcesFromDB.size());
				for (Resource allUserCategoriesExceptGlobalsResourceFromDB:allUserCategoriesExceptGlobalsResourcesFromDB)
				{
					Resource allUserCategoriesExceptGlobalsResource=
						allUserCategoriesExceptGlobalsResourceFromDB.getResourceCopy();
					if (allUserCategoriesExceptGlobalsResourceFromDB.getUser()!=null)
					{
						User allUserCategoriesExceptGlobalsResourceUser=
							allUserCategoriesExceptGlobalsResourceFromDB.getUser().getUserCopy();
						
						// Password is set to empty string before returning instance for security reasons
						allUserCategoriesExceptGlobalsResourceUser.setPassword("");
						
						allUserCategoriesExceptGlobalsResource.setUser(allUserCategoriesExceptGlobalsResourceUser);
					}
					if (allUserCategoriesExceptGlobalsResourceFromDB.getCategory()!=null)
					{
						Category categoryFromDB=allUserCategoriesExceptGlobalsResourceFromDB.getCategory();
						Category category=categoryFromDB.getCategoryCopy();
						if (categoryFromDB.getUser()!=null)
						{
							User categoryUser=categoryFromDB.getUser().getUserCopy();
							
							// Password is set to empty string before returning instance for security reasons
							categoryUser.setPassword("");
							
							category.setUser(categoryUser);
						}
						if (categoryFromDB.getCategoryType()!=null)
						{
							category.setCategoryType(categoryFromDB.getCategoryType().getCategoryTypeCopy());
						}
						if (categoryFromDB.getVisibility()!=null)
						{
							category.setVisibility(categoryFromDB.getVisibility().getVisibilityCopy());
						}
						allUserCategoriesExceptGlobalsResource.setCategory(category);
					}
					if (allUserCategoriesExceptGlobalsResourceFromDB.getCopyright()!=null)
					{
						allUserCategoriesExceptGlobalsResource.setCopyright(
							allUserCategoriesExceptGlobalsResourceFromDB.getCopyright().getCopyrightCopy());
					}
					allUserCategoriesExceptGlobalsResources.add(allUserCategoriesExceptGlobalsResource);
				}
			}
			else
			{
				throwServiceException(
					"NON_AUTHORIZED_ACTION_ERROR","You are not authorized to execute that operation.");
			}
		}
		catch (DaoException de)
		{
			throw new ServiceException(de.getMessage(),de);
		}
		finally
		{
			if (singleOp)
			{
				// End Hibernate operation
				HibernateUtil.endOperation(operation);
			}
		}
		return allUserCategoriesExceptGlobalsResources;
	}
	
	/**
	 * @param owner Owner of categories from which we get the resources
	 * @param user User or null to get resources from all users
	 * @param mimeType Filtering MIME type or empty string to get resources with all MIME types
	 * @param copyrightId Filtering copyright identifier or 0 to get resources with all copyrights
	 * @return List of resources from all categories of resources of an user (except global categories) 
	 * filtered by user and MIME type
	 * @throws ServiceException
	 */
	public List<Resource> getAllUserCategoriesExceptGlobalsResources(User owner,User user,String mimeType,
		long copyrightId) throws ServiceException
	{
		return getAllUserCategoriesExceptGlobalsResources((Operation)null,owner,user,mimeType,copyrightId);
	}
	
	/**
	 * @param operation Operation
	 * @param owner Owner of categories from which we get the resources
	 * @param user User or null to get resources from all users
	 * @param mimeType Filtering MIME type or empty string to get resources with all MIME types
	 * @param copyrightId Filtering copyright identifier or 0 to get resources with all copyrights
	 * @return List of resources from all categories of an user (except global categories) filtered by user 
	 * and MIME type
	 * @throws ServiceException
	 */
	public List<Resource> getAllUserCategoriesExceptGlobalsResources(Operation operation,User owner,User user,
		String mimeType,long copyrightId) throws ServiceException
	{
		boolean singleOp=operation==null;
		List<Resource> allUserCategoriesExceptGlobalsResources=null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			allUserCategoriesExceptGlobalsResources=getAllUserCategoriesExceptGlobalsResources(
				operation,getCurrentUser(operation),owner,user,mimeType,copyrightId,false);
		}
		finally
		{
			if (singleOp)
			{
				// End Hibernate operation
				HibernateUtil.endOperation(operation);
			}
		}
		return allUserCategoriesExceptGlobalsResources;
	}
	
	/**
	 * @param owner Owner of categories from which we get the resources
	 * @param user User or null to get resources from all users
	 * @param mimeType Filtering MIME type or empty string to get resources with all MIME types
	 * @param copyrightId Filtering copyright identifier or 0 to get resources with all copyrights
	 * @return List of resources from all categories of an user (except global categories) filtered 
	 * by user and MIME type and sorted by name
	 * @throws ServiceException
	 */
	public List<Resource> getAllUserCategoriesExceptGlobalsResourcesSortedByName(User owner,User user,String mimeType,
		long copyrightId) throws ServiceException
	{
		return getAllUserCategoriesExceptGlobalsResourcesSortedByName((Operation)null,owner,user,mimeType,copyrightId);
	}
	
	/**
	 * @param operation Operation
	 * @param owner Owner of categories from which we get the resources
	 * @param user User or null to get resources from all users
	 * @param mimeType Filtering MIME type or empty string to get resources with all MIME types
	 * @param copyrightId Filtering copyright identifier or 0 to get resources with all copyrights
	 * @return List of resources from all categories of an user (except global categories) filtered by user 
	 * and MIME type and sorted by name
	 * @throws ServiceException
	 */
	public List<Resource> getAllUserCategoriesExceptGlobalsResourcesSortedByName(Operation operation,User owner,
		User user,String mimeType,long copyrightId) throws ServiceException
	{
		boolean singleOp=operation==null;
		List<Resource> allUserCategoriesExceptGlobalsResourcesSortedByName=null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			allUserCategoriesExceptGlobalsResourcesSortedByName=getAllUserCategoriesExceptGlobalsResources(
				operation,getCurrentUser(operation),owner,user,mimeType,copyrightId,true);
		}
		finally
		{
			if (singleOp)
			{
				// End Hibernate operation
				HibernateUtil.endOperation(operation);
			}
		}
		return allUserCategoriesExceptGlobalsResourcesSortedByName;
	}
	
	/**
	 * @param user User or null to get resources from all users
	 * @param mimeType Filtering MIME type or empty string to get resources with all MIME types
	 * @param copyrightId Filtering copyright identifier or 0 to get resources with all copyrights
	 * @return List of resources from all categories of resources of current user (except global categories) 
	 * filtered by user and MIME type
	 * @throws ServiceException
	 */
	public List<Resource> getAllMyCategoriesExceptGlobalsResources(User user,String mimeType,long copyrightId)
		throws ServiceException
	{
		return getAllMyCategoriesExceptGlobalsResources((Operation)null,user,mimeType,copyrightId);
	}
	
	/**
	 * @param operation Operation
	 * @param user User or null to get resources from all users
	 * @param mimeType Filtering MIME type or empty string to get resources with all MIME types
	 * @param copyrightId Filtering copyright identifier or 0 to get resources with all copyrights
	 * @return List of resources from all categories of current user (except global categories) filtered 
	 * by user and MIME type
	 * @throws ServiceException
	 */
	public List<Resource> getAllMyCategoriesExceptGlobalsResources(Operation operation,User user,String mimeType,
		long copyrightId) throws ServiceException
	{
		boolean singleOp=operation==null;
		List<Resource> allMyCategoriesExceptGlobalsResources=null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			User currentUser=getCurrentUser(operation);
			allMyCategoriesExceptGlobalsResources=getAllUserCategoriesExceptGlobalsResources(
				operation,currentUser,currentUser,user,mimeType,copyrightId,false);
		}
		finally
		{
			if (singleOp)
			{
				// End Hibernate operation
				HibernateUtil.endOperation(operation);
			}
		}
		return allMyCategoriesExceptGlobalsResources;
	}
	
	/**
	 * @param user User or null to get resources from all users
	 * @param mimeType Filtering MIME type or empty string to get resources with all MIME types
	 * @param copyrightId Filtering copyright identifier or 0 to get resources with all copyrights
	 * @return List of resources from all categories of current user (except global categories) filtered 
	 * by user and MIME type and sorted by name
	 * @throws ServiceException
	 */
	public List<Resource> getAllMyCategoriesExceptGlobalsResourcesSortedByName(User user,String mimeType,
		long copyrightId) throws ServiceException
	{
		return getAllMyCategoriesExceptGlobalsResourcesSortedByName((Operation)null,user,mimeType,copyrightId);
	}
	
	/**
	 * @param operation Operation
	 * @param user User or null to get resources from all users
	 * @param mimeType Filtering MIME type or empty string to get resources with all MIME types
	 * @param copyrightId Filtering copyright identifier or 0 to get resources with all copyrights
	 * @return List of resources from all categories of current user (except global categories) filtered 
	 * by user and MIME type and sorted by name
	 * @throws ServiceException
	 */
	public List<Resource> getAllMyCategoriesExceptGlobalsResourcesSortedByName(Operation operation,User user,
		String mimeType,long copyrightId) throws ServiceException
	{
		boolean singleOp=operation==null;
		List<Resource> allMyCategoriesExceptGlobalsResourcesSortedByName=null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			User currentUser=getCurrentUser(operation);
			allMyCategoriesExceptGlobalsResourcesSortedByName=getAllUserCategoriesExceptGlobalsResources(
				operation,currentUser,currentUser,user,mimeType,copyrightId,true);
		}
		finally
		{
			if (singleOp)
			{
				// End Hibernate operation
				HibernateUtil.endOperation(operation);
			}
		}
		return allMyCategoriesExceptGlobalsResourcesSortedByName;
	}
	
	/**
	 * @param operation Operation
	 * @param viewer User used to check visibility
	 * @param user User or null to get resources from all users
	 * @param mimeType Filtering MIME type or empty string to get resources with all MIME types
	 * @param copyrightId Filtering copyright identifier or 0 to get resources with all copyrights
	 * @param sortedByName Flag to indicate if we want the results sorted by name
	 * @return List of resources from all global categories filtered by user and MIME type and optionally 
	 * sorted by name
	 * @throws ServiceException
	 */
	private List<Resource> getAllGlobalCategoriesResources(Operation operation,User viewer,User user,String mimeType,
		long copyrightId,boolean sortedByName) throws ServiceException
	{
		List<Resource> allGlobalCategoriesResources=null;
		boolean singleOp=operation==null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			if (permissionsService.isGranted(operation,viewer,"PERMISSION_RESOURCES_GLOBAL_FILTER_ENABLED"))
			{
				List<Long> allGlobalCategoriesIds=categoriesService.getAllGlobalCategoriesIds(
					operation,getCategoryTypeByMIMEType(operation,mimeType));
				
				// We get resources from DB
				RESOURCES_DAO.setOperation(operation);
				List<Resource> allGlobalCategoriesResourcesFromDB=RESOURCES_DAO.getResources(
					user==null?0L:user.getId(),allGlobalCategoriesIds,mimeType,copyrightId,sortedByName,true,true,
					true);
				
				// We return new referenced resources from all global categories within a new list to avoid 
				// shared collection references and object references to unsaved transient instances
				allGlobalCategoriesResources=new ArrayList<Resource>(allGlobalCategoriesResourcesFromDB.size());
				for (Resource allGlobalCategoriesResourceFromDB:allGlobalCategoriesResourcesFromDB)
				{
					Resource allGlobalCategoriesResource=allGlobalCategoriesResourceFromDB.getResourceCopy();
					if (allGlobalCategoriesResourceFromDB.getUser()!=null)
					{
						User allGlobalCategoriesResourceUser=
							allGlobalCategoriesResourceFromDB.getUser().getUserCopy();
						
						// Password is set to empty string before returning instance for security reasons
						allGlobalCategoriesResourceUser.setPassword("");
						
						allGlobalCategoriesResource.setUser(allGlobalCategoriesResourceUser);
					}
					if (allGlobalCategoriesResourceFromDB.getCategory()!=null)
					{
						Category categoryFromDB=allGlobalCategoriesResourceFromDB.getCategory();
						Category category=categoryFromDB.getCategoryCopy();
						if (categoryFromDB.getUser()!=null)
						{
							User categoryUser=categoryFromDB.getUser().getUserCopy();
							
							// Password is set to empty string before returning instance for security reasons
							categoryUser.setPassword("");
							
							category.setUser(categoryUser);
						}
						if (categoryFromDB.getCategoryType()!=null)
						{
							category.setCategoryType(categoryFromDB.getCategoryType().getCategoryTypeCopy());
						}
						if (categoryFromDB.getVisibility()!=null)
						{
							category.setVisibility(categoryFromDB.getVisibility().getVisibilityCopy());
						}
						allGlobalCategoriesResourceFromDB.setCategory(category);
					}
					if (allGlobalCategoriesResourceFromDB.getCopyright()!=null)
					{
						allGlobalCategoriesResource.setCopyright(
							allGlobalCategoriesResourceFromDB.getCopyright().getCopyrightCopy());
					}
					allGlobalCategoriesResources.add(allGlobalCategoriesResource);
				}
			}
			else
			{
				throwServiceException(
					"NON_AUTHORIZED_ACTION_ERROR","You are not authorized to execute that operation.");
			}
		}
		catch (DaoException de)
		{
			throw new ServiceException(de.getMessage(),de);
		}
		finally
		{
			if (singleOp)
			{
				// End Hibernate operation
				HibernateUtil.endOperation(operation);
			}
		}
		return allGlobalCategoriesResources;
	}
	
	/**
	 * @param user User or null to get resources from all users
	 * @param mimeType Filtering MIME type or empty string to get resources with all MIME types
	 * @param copyrightId Filtering copyright identifier or 0 to get resources with all copyrights
	 * @return List of resources from all global categories filtered by user and MIME type
	 * @throws ServiceException
	 */
	public List<Resource> getAllGlobalCategoriesResources(User user,String mimeType,long copyrightId)
		throws ServiceException
	{
		return getAllGlobalCategoriesResources((Operation)null,user,mimeType,copyrightId);
	}
	
	/**
	 * @param operation Operation
	 * @param user User or null to get resources from all users
	 * @param mimeType Filtering MIME type or empty string to get resources with all MIME types
	 * @param copyrightId Filtering copyright identifier or 0 to get resources with all copyrights
	 * @return List of resources from all global categories filtered by user and MIME type
	 * @throws ServiceException
	 */
	public List<Resource> getAllGlobalCategoriesResources(Operation operation,User user,String mimeType,
		long copyrightId) throws ServiceException
	{
		boolean singleOp=operation==null;
		List<Resource> allGlobalCategoriesResources=null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			allGlobalCategoriesResources=
				getAllGlobalCategoriesResources(operation,getCurrentUser(operation),user,mimeType,copyrightId,false);
		}
		finally
		{
			if (singleOp)
			{
				// End Hibernate operation
				HibernateUtil.endOperation(operation);
			}
		}
		return allGlobalCategoriesResources;
	}
	
	/**
	 * @param user User or null to get resources from all users
	 * @param mimeType Filtering MIME type or empty string to get resources with all MIME types
	 * @param copyrightId Filtering copyright identifier or 0 to get resources with all copyrights
	 * @return List of resources from all global categories filtered by user and MIME type and sorted by name
	 * @throws ServiceException
	 */
	public List<Resource> getAllGlobalCategoriesResourcesSortedByName(User user,String mimeType,long copyrightId) 
		throws ServiceException
	{
		return getAllGlobalCategoriesResourcesSortedByName((Operation)null,user,mimeType,copyrightId);
	}
	
	/**
	 * @param operation Operation
	 * @param user User or null to get resources from all users
	 * @param mimeType Filtering MIME type or empty string to get resources with all MIME types
	 * @param copyrightId Filtering copyright identifier or 0 to get resources with all copyrights
	 * @return List of resources from all global categories filtered by user and MIME type and sorted by name
	 * @throws ServiceException
	 */
	public List<Resource> getAllGlobalCategoriesResourcesSortedByName(Operation operation,User user,String mimeType,
		long copyrightId) throws ServiceException
	{
		boolean singleOp=operation==null;
		List<Resource> allGlobalCategoriesResourcesSortedByName=null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			allGlobalCategoriesResourcesSortedByName=
				getAllGlobalCategoriesResources(operation,getCurrentUser(operation),user,mimeType,copyrightId,true);
		}
		finally
		{
			if (singleOp)
			{
				// End Hibernate operation
				HibernateUtil.endOperation(operation);
			}
		}
		return allGlobalCategoriesResourcesSortedByName;
	}
	
	/**
	 * @param operation Operation
	 * @param viewer User used to check visibility
	 * @param user User or null to get resources from all users
	 * @param mimeType Filtering MIME type or empty string to get resources with all MIME types
	 * @param copyrightId Filtering copyright identifier or 0 to get resources with all copyrights
	 * @param sortedByName Flag to indicate if we want the results sorted by name
	 * @return List of resources from  all public categories of all users except the current one filtered 
	 * by user and MIME type and optionally sorted by name
	 * @throws ServiceException
	 */
	private List<Resource> getAllPublicCategoriesOfOtherUsersResources(Operation operation,User viewer,User user,
		String mimeType,long copyrightId,boolean sortedByName) throws ServiceException
	{
		List<Resource> allPublicCategoriesOfOtherUsersResources=null;
		boolean singleOp=operation==null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			if (permissionsService.isGranted(operation,viewer,"PERMISSION_RESOURCES_OTHER_USERS_FILTER_ENABLED"))
			{
				List<Long> allPublicCategoriesOfOtherUsersIds=
					categoriesService.getAllPublicCategoriesOfOtherUsersIds(operation,viewer,
					getCategoryTypeByMIMEType(operation,mimeType));
				
				// We get resources from DB
				RESOURCES_DAO.setOperation(operation);
				List<Resource> allPublicCategoriesOfOtherUsersResourcesFromDB=RESOURCES_DAO.getResources(
					user==null?0L:user.getId(),allPublicCategoriesOfOtherUsersIds,mimeType,copyrightId,sortedByName,
					true,true,true);
				
				// We return new referenced resources from  all public categories of all users 
				// except the current one within a new list to avoid shared collection references
				// and object references to unsaved transient instances
				allPublicCategoriesOfOtherUsersResources=
					new ArrayList<Resource>(allPublicCategoriesOfOtherUsersResourcesFromDB.size());
				for (Resource allPublicCategoriesOfOtherUsersResourceFromDB:
					allPublicCategoriesOfOtherUsersResourcesFromDB)
				{
					Resource allPublicCategoriesOfOtherUsersResource=
						allPublicCategoriesOfOtherUsersResourceFromDB.getResourceCopy();
					if (allPublicCategoriesOfOtherUsersResourceFromDB.getUser()!=null)
					{
						User allPublicCategoriesOfOtherUsersResourceUser=
							allPublicCategoriesOfOtherUsersResourceFromDB.getUser().getUserCopy();
						
						// Password is set to empty string before returning instance for security reasons
						allPublicCategoriesOfOtherUsersResourceUser.setPassword("");
						
						allPublicCategoriesOfOtherUsersResource.setUser(
							allPublicCategoriesOfOtherUsersResourceUser);
					}
					if (allPublicCategoriesOfOtherUsersResourceFromDB.getCategory()!=null)
					{
						Category categoryFromDB=allPublicCategoriesOfOtherUsersResourceFromDB.getCategory();
						Category category=categoryFromDB.getCategoryCopy();
						if (categoryFromDB.getUser()!=null)
						{
							User categoryUser=categoryFromDB.getUser().getUserCopy();
							
							// Password is set to empty string before returning instance for security reasons
							categoryUser.setPassword("");
							
							category.setUser(categoryUser);
						}
						if (categoryFromDB.getCategoryType()!=null)
						{
							category.setCategoryType(categoryFromDB.getCategoryType().getCategoryTypeCopy());
						}
						if (categoryFromDB.getVisibility()!=null)
						{
							category.setVisibility(categoryFromDB.getVisibility().getVisibilityCopy());
						}
						allPublicCategoriesOfOtherUsersResource.setCategory(category);
					}
					if (allPublicCategoriesOfOtherUsersResourceFromDB.getCopyright()!=null)
					{
						allPublicCategoriesOfOtherUsersResource.setCopyright(
							allPublicCategoriesOfOtherUsersResourceFromDB.getCopyright().getCopyrightCopy());
					}
					allPublicCategoriesOfOtherUsersResources.add(allPublicCategoriesOfOtherUsersResource);
				}
			}
			else
			{
				throwServiceException(
					"NON_AUTHORIZED_ACTION_ERROR","You are not authorized to execute that operation.");
			}
		}
		catch (DaoException de)
		{
			throw new ServiceException(de.getMessage(),de);
		}
		finally
		{
			if (singleOp)
			{
				// End Hibernate operation
				HibernateUtil.endOperation(operation);
			}
		}
		return allPublicCategoriesOfOtherUsersResources;
	}
	
	/**
	 * @param user User or null to get resources from all users
	 * @param mimeType Filtering MIME type or empty string to get resources with all MIME types
	 * @param copyrightId Filtering copyright identifier or 0 to get resources with all copyrights
	 * @return List of resources from all public categories of all users except the current one filtered 
	 * by user and MIME type
	 * @throws ServiceException
	 */
	public List<Resource> getAllPublicCategoriesOfOtherUsersResources(User user,String mimeType,long copyrightId) 
		throws ServiceException
	{
		return getAllPublicCategoriesOfOtherUsersResources((Operation)null,user,mimeType,copyrightId);
	}
	
	/**
	 * @param operation Operation
	 * @param user User or null to get resources from all users
	 * @param mimeType Filtering MIME type or empty string to get resources with all MIME types
	 * @param copyrightId Filtering copyright identifier or 0 to get resources with all copyrights
	 * @return List of resources from all public categories of all users except the current one filtered 
	 * by user and MIME type
	 * @throws ServiceException
	 */
	public List<Resource> getAllPublicCategoriesOfOtherUsersResources(Operation operation,User user,String mimeType,
		long copyrightId) throws ServiceException
	{
		boolean singleOp=operation==null;
		List<Resource> allPublicCategoriesOfOtherUsersResources=null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			allPublicCategoriesOfOtherUsersResources=getAllPublicCategoriesOfOtherUsersResources(
				operation,getCurrentUser(operation),user,mimeType,copyrightId,false);
		}
		finally
		{
			if (singleOp)
			{
				// End Hibernate operation
				HibernateUtil.endOperation(operation);
			}
		}
		return allPublicCategoriesOfOtherUsersResources;
	}
	
	/**
	 * @param user User or null to get resources from all users
	 * @param mimeType Filtering MIME type or empty string to get resources with all MIME types
	 * @param copyrightId Filtering copyright identifier or 0 to get resources with all copyrights
	 * @return List of resources from all public categories of all users except the current one filtered 
	 * by user and MIME type and sorted by name
	 * @throws ServiceException
	 */
	public List<Resource> getAllPublicCategoriesOfOtherUsersResourcesSortedByName(User user,String mimeType,
		long copyrightId) throws ServiceException
	{
		return getAllPublicCategoriesOfOtherUsersResourcesSortedByName((Operation)null,user,mimeType,copyrightId);
	}
	
	/**
	 * @param operation Operation
	 * @param user User or null to get resources from all users
	 * @param mimeType Filtering MIME type or empty string to get resources with all MIME types
	 * @param copyrightId Filtering copyright identifier or 0 to get resources with all copyrights
	 * @return List of resources from all public categories of all users except the current one filtered 
	 * by user and MIME type and sorted by name
	 * @throws ServiceException
	 */
	public List<Resource> getAllPublicCategoriesOfOtherUsersResourcesSortedByName(Operation operation,User user,
		String mimeType,long copyrightId) throws ServiceException
	{
		boolean singleOp=operation==null;
		List<Resource> allPublicCategoriesOfOtherUsersResourcesSortedByName=null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			allPublicCategoriesOfOtherUsersResourcesSortedByName=getAllPublicCategoriesOfOtherUsersResources(
				operation,getCurrentUser(operation),user,mimeType,copyrightId,true);
		}
		finally
		{
			if (singleOp)
			{
				// End Hibernate operation
				HibernateUtil.endOperation(operation);
			}
		}
		return allPublicCategoriesOfOtherUsersResourcesSortedByName;
	}
	
	/**
	 * @param operation Operation
	 * @param viewer User used to check visibility
	 * @param user User or null to get resources from all users
	 * @param mimeType Filtering MIME type or empty string to get resources with all MIME types
	 * @param copyrightId Filtering copyright identifier or 0 to get resources with all copyrights
	 * @return List of resources from all public categories of resources of all users except the current one 
	 * filtered by user and MIME type and sorted by name
	 * @throws ServiceException
	 */
	public List<Resource> getAllPublicCategoriesOfOtherUsersResourcesSortedByName(Operation operation,User viewer,
		User user,String mimeType,long copyrightId) throws ServiceException
	{
		return getAllPublicCategoriesOfOtherUsersResources(operation,viewer,user,mimeType,copyrightId,true);
	}
	
	/**
	 * @param operation Operation
	 * @param viewer User used to check visibility
	 * @param user User or null to get resources from all users
	 * @param mimeType Filtering MIME type or empty string to get resources with all MIME types
	 * @param copyrightId Filtering copyright identifier or 0 to get resources with all copyrights
	 * @param sortedByName Flag to indicate if we want the results sorted by name
	 * @return List of resources from all private categories of all users except the current one filtered 
	 * by user and MIME type and optionally sorted by name
	 * @throws ServiceException
	 */
	private List<Resource> getAllPrivateCategoriesOfOtherUsersResources(Operation operation,User viewer,User user,
		String mimeType,long copyrightId,boolean sortedByName) throws ServiceException
	{
		List<Resource> allPrivateCategoriesOfOtherUsersResources=null;
		boolean singleOp=operation==null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			if (permissionsService.isGranted(operation,viewer,"PERMISSION_RESOURCES_OTHER_USERS_FILTER_ENABLED") && 
				permissionsService.isGranted(operation,viewer,
				"PERMISSION_RESOURCES_VIEW_RESOURCES_OF_OTHER_USERS_PRIVATE_CATEGORIES_ENABLED"))
			{
				boolean includeAdminsPrivateCategories=permissionsService.isGranted(
					operation,viewer,"PERMISSION_RESOURCES_VIEW_RESOURCES_OF_ADMINS_PRIVATE_CATEGORIES_ENABLED");
				boolean includeSuperadminsPrivateCategories=permissionsService.isGranted(
					operation,viewer,"PERMISSION_RESOURCES_VIEW_RESOURCES_OF_SUPERADMINS_PRIVATE_CATEGORIES_ENABLED");
				List<Long> allPrivateCategoriesOfOtherUsersIds=
					categoriesService.getAllPrivateCategoriesOfOtherUsersIds(operation,viewer,
					getCategoryTypeByMIMEType(operation,mimeType),includeAdminsPrivateCategories,
					includeSuperadminsPrivateCategories);
				
				// We get resources from DB
				RESOURCES_DAO.setOperation(operation);
				List<Resource> allPrivateCategoriesOfOtherUsersResourcesFromDB=RESOURCES_DAO.getResources(
					user==null?0L:user.getId(),allPrivateCategoriesOfOtherUsersIds,mimeType,copyrightId,
					sortedByName,true,true,true);
				
				// We return new referenced resources from all private categories of all users 
				// except the current one within a new list to avoid shared collection references
				// and object references to unsaved transient instances
				allPrivateCategoriesOfOtherUsersResources=
					new ArrayList<Resource>(allPrivateCategoriesOfOtherUsersResourcesFromDB.size());
				for (Resource allPrivateCategoriesOfOtherUsersResourceFromDB:
					allPrivateCategoriesOfOtherUsersResourcesFromDB)
				{
					Resource allPrivateCategoriesOfOtherUsersResource=
						allPrivateCategoriesOfOtherUsersResourceFromDB.getResourceCopy();
					if (allPrivateCategoriesOfOtherUsersResourceFromDB.getUser()!=null)
					{
						User allPrivateCategoriesOfOtherUsersResourceUser=
							allPrivateCategoriesOfOtherUsersResourceFromDB.getUser().getUserCopy();
						
						// Password is set to empty string before returning instance for security reasons
						allPrivateCategoriesOfOtherUsersResourceUser.setPassword("");
						
						allPrivateCategoriesOfOtherUsersResource.setUser(
							allPrivateCategoriesOfOtherUsersResourceUser);
					}
					if (allPrivateCategoriesOfOtherUsersResourceFromDB.getCategory()!=null)
					{
						Category categoryFromDB=allPrivateCategoriesOfOtherUsersResourceFromDB.getCategory();
						Category category=categoryFromDB.getCategoryCopy();
						if (categoryFromDB.getUser()!=null)
						{
							User categoryUser=categoryFromDB.getUser().getUserCopy();
							
							// Password is set to empty string before returning instance for security reasons
							categoryUser.setPassword("");
							
							category.setUser(categoryUser);
						}
						if (categoryFromDB.getCategoryType()!=null)
						{
							category.setCategoryType(categoryFromDB.getCategoryType().getCategoryTypeCopy());
						}
						if (categoryFromDB.getVisibility()!=null)
						{
							category.setVisibility(categoryFromDB.getVisibility().getVisibilityCopy());
						}
						allPrivateCategoriesOfOtherUsersResource.setCategory(category);
					}
					if (allPrivateCategoriesOfOtherUsersResourceFromDB.getCopyright()!=null)
					{
						allPrivateCategoriesOfOtherUsersResource.setCopyright(
							allPrivateCategoriesOfOtherUsersResourceFromDB.getCopyright());
					}
					allPrivateCategoriesOfOtherUsersResources.add(allPrivateCategoriesOfOtherUsersResource);
				}
			}
			else
			{
				throwServiceException(
					"NON_AUTHORIZED_ACTION_ERROR","You are not authorized to execute that operation.");
			}
		}
		catch (DaoException de)
		{
			throw new ServiceException(de.getMessage(),de);
		}
		finally
		{
			if (singleOp)
			{
				// End Hibernate operation
				HibernateUtil.endOperation(operation);
			}
		}
		return allPrivateCategoriesOfOtherUsersResources;
	}
	
	/**
	 * @param user User or null to get resources from all users
	 * @param mimeType Filtering MIME type or empty string to get resources with all MIME types
	 * @param copyrightId Filtering copyright identifier or 0 to get resources with all copyrights
	 * @return List of resources from all private categories of all users except the current one filtered 
	 * by user and MIME type
	 * @throws ServiceException
	 */
	public List<Resource> getAllPrivateCategoriesOfOtherUsersResources(User user,String mimeType,long copyrightId) 
		throws ServiceException
	{
		return getAllPrivateCategoriesOfOtherUsersResources((Operation)null,user,mimeType,copyrightId);
	}
	
	/**
	 * @param operation Operation
	 * @param user User or null to get resources from all users
	 * @param mimeType Filtering MIME type or empty string to get resources with all MIME types
	 * @param copyrightId Filtering copyright identifier or 0 to get resources with all copyrights
	 * @return List resources from all private categories of all users except the current one filtered by user 
	 * and MIME type
	 * @throws ServiceException
	 */
	public List<Resource> getAllPrivateCategoriesOfOtherUsersResources(Operation operation,User user,String mimeType,
		long copyrightId) throws ServiceException
	{
		boolean singleOp=operation==null;
		List<Resource> allPrivateCategoriesOfOtherUsersResources=null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			allPrivateCategoriesOfOtherUsersResources=getAllPrivateCategoriesOfOtherUsersResources(
				operation,getCurrentUser(operation),user,mimeType,copyrightId,false);
		}
		finally
		{
			if (singleOp)
			{
				// End Hibernate operation
				HibernateUtil.endOperation(operation);
			}
		}
		return allPrivateCategoriesOfOtherUsersResources;
	}
	
	/**
	 * @param user User or null to get resources from all users
	 * @param mimeType Filtering MIME type or empty string to get resources with all MIME types
	 * @param copyrightId Filtering copyright identifier or 0 to get resources with all copyrights
	 * @return List of resources from all private categories of all users except the current one filtered 
	 * by user and MIME type and sorted by name
	 * @throws ServiceException
	 */
	public List<Resource> getAllPrivateCategoriesOfOtherUsersResourcesSortedByName(User user,String mimeType,
		long copyrightId) throws ServiceException
	{
		return getAllPrivateCategoriesOfOtherUsersResourcesSortedByName((Operation)null,user,mimeType,copyrightId);
	}
	
	/**
	 * @param operation Operation
	 * @param user User or null to get resources from all users
	 * @param mimeType Filtering MIME type or empty string to get resources with all MIME types
	 * @param copyrightId Filtering copyright identifier or 0 to get resources with all copyrights
	 * @return List of resources from all private categories of all users except the current one filtered 
	 * by user and MIME type and sorted by name
	 * @throws ServiceException
	 */
	public List<Resource> getAllPrivateCategoriesOfOtherUsersResourcesSortedByName(Operation operation,User user,
		String mimeType,long copyrightId) throws ServiceException
	{
		boolean singleOp=operation==null;
		List<Resource> allPrivateCategoriesOfOtherUsersResourcesSortedByName=null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			allPrivateCategoriesOfOtherUsersResourcesSortedByName=getAllPrivateCategoriesOfOtherUsersResources(
				operation,getCurrentUser(operation),user,mimeType,copyrightId,true);
		}
		finally
		{
			if (singleOp)
			{
				// End Hibernate operation
				HibernateUtil.endOperation(operation);
			}
		}
		return allPrivateCategoriesOfOtherUsersResourcesSortedByName;
	}
	
	/**
	 * @param operation Operation
	 * @param viewer User used to check visibility
	 * @param user User or null to get resources from all users
	 * @param mimeType Filtering MIME type or empty string to get resources with all MIME types
	 * @param copyrightId Filtering copyright identifier or 0 to get resources with all copyrights
	 * @param sortedByName Flag to indicate if we want the results sorted by name
	 * @return List of resources from all categories of all users except the current one filtered by user 
	 * and MIME type and optionally sorted by name
	 * @throws ServiceException
	 */
	private List<Resource> getAllCategoriesOfOtherUsersResources(Operation operation,User viewer,User user,
		String mimeType,long copyrightId,boolean sortedByName) throws ServiceException
	{
		List<Resource> allCategoriesOfOtherUsersResources=null;
		boolean singleOp=operation==null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			if (permissionsService.isGranted(operation,viewer,"PERMISSION_RESOURCES_OTHER_USERS_FILTER_ENABLED") && 
				permissionsService.isGranted(operation,viewer,
				"PERMISSION_RESOURCES_VIEW_RESOURCES_OF_OTHER_USERS_PRIVATE_CATEGORIES_ENABLED"))
			{
				boolean includeAdminsPrivateCategories=permissionsService.isGranted(
					operation,viewer,"PERMISSION_RESOURCES_VIEW_RESOURCES_OF_ADMINS_PRIVATE_CATEGORIES_ENABLED");
				boolean includeSuperadminsPrivateCategories=permissionsService.isGranted(
					operation,viewer,"PERMISSION_RESOURCES_VIEW_RESOURCES_OF_SUPERADMINS_PRIVATE_CATEGORIES_ENABLED");
				List<Long> allCategoriesOfOtherUsersIds=categoriesService.getAllCategoriesOfOtherUsersIds(
					operation,viewer,getCategoryTypeByMIMEType(operation,mimeType),
					includeAdminsPrivateCategories,includeSuperadminsPrivateCategories);
				
				// We get resources from DB
				RESOURCES_DAO.setOperation(operation);
				List<Resource> allCategoriesOfOtherUsersResourcesFromDB=RESOURCES_DAO.getResources(
					user==null?0L:user.getId(),allCategoriesOfOtherUsersIds,mimeType,copyrightId,sortedByName,true,
					true,true);
				
				// We return new referenced resources from all categories of all users except the current one 
				// within a new list to avoid shared collection references and object references 
				// to unsaved transient instances
				allCategoriesOfOtherUsersResources=
					new ArrayList<Resource>(allCategoriesOfOtherUsersResourcesFromDB.size());
				for (Resource allCategoriesOfOtherUsersResourceFromDB:allCategoriesOfOtherUsersResourcesFromDB)
				{
					Resource allCategoriesOfOtherUsersResource=
						allCategoriesOfOtherUsersResourceFromDB.getResourceCopy();
					if (allCategoriesOfOtherUsersResourceFromDB.getUser()!=null)
					{
						User allCategoriesOfOtherUsersResourceUser=
							allCategoriesOfOtherUsersResourceFromDB.getUser().getUserCopy();
						
						// Password is set to empty string before returning instance for security reasons
						allCategoriesOfOtherUsersResourceUser.setPassword("");
						
						allCategoriesOfOtherUsersResource.setUser(allCategoriesOfOtherUsersResourceUser);
					}
					if (allCategoriesOfOtherUsersResourceFromDB.getCategory()!=null)
					{
						Category categoryFromDB=allCategoriesOfOtherUsersResourceFromDB.getCategory();
						Category category=categoryFromDB.getCategoryCopy();
						if (categoryFromDB.getUser()!=null)
						{
							User categoryUser=categoryFromDB.getUser().getUserCopy();
							
							// Password is set to empty string before returning instance for security reasons
							categoryUser.setPassword("");
							
							category.setUser(categoryUser);
						}
						if (categoryFromDB.getCategoryType()!=null)
						{
							category.setCategoryType(categoryFromDB.getCategoryType().getCategoryTypeCopy());
						}
						if (categoryFromDB.getVisibility()!=null)
						{
							category.setVisibility(categoryFromDB.getVisibility().getVisibilityCopy());
						}
						allCategoriesOfOtherUsersResource.setCategory(category);
					}
					if (allCategoriesOfOtherUsersResourceFromDB.getCopyright()!=null)
					{
						allCategoriesOfOtherUsersResource.setCopyright(
							allCategoriesOfOtherUsersResourceFromDB.getCopyright().getCopyrightCopy());
					}
					allCategoriesOfOtherUsersResources.add(allCategoriesOfOtherUsersResource);
				}
			}
			else
			{
				throwServiceException(
					"NON_AUTHORIZED_ACTION_ERROR","You are not authorized to execute that operation.");
			}
		}
		catch (DaoException de)
		{
			throw new ServiceException(de.getMessage(),de);
		}
		finally
		{
			if (singleOp)
			{
				// End Hibernate operation
				HibernateUtil.endOperation(operation);
			}
		}
		return allCategoriesOfOtherUsersResources;
	}
	
	/**
	 * @param user User or null to get resources from all users
	 * @param mimeType Filtering MIME type or empty string to get resources with all MIME types
	 * @param copyrightId Filtering copyright identifier or 0 to get resources with all copyrights
	 * @return List of resources from all categories of all users except the current one filtered by user 
	 * and MIME type
	 * @throws ServiceException
	 */
	public List<Resource> getAllCategoriesOfOtherUsersResources(User user,String mimeType,long copyrightId) 
		throws ServiceException
	{
		return getAllCategoriesOfOtherUsersResources((Operation)null,user,mimeType,copyrightId);
	}
	
	/**
	 * @param operation Operation
	 * @param user User or null to get resources from all users
	 * @param mimeType Filtering MIME type or empty string to get resources with all MIME types
	 * @param copyrightId Filtering copyright identifier or 0 to get resources with all copyrights
	 * @return List of resources from all categories of all users except the current one filtered by user 
	 * and MIME type
	 * @throws ServiceException
	 */
	public List<Resource> getAllCategoriesOfOtherUsersResources(Operation operation,User user,String mimeType,
		long copyrightId) throws ServiceException
	{
		boolean singleOp=operation==null;
		List<Resource> allCategoriesOfOtherUsersResources=null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			allCategoriesOfOtherUsersResources=getAllCategoriesOfOtherUsersResources(
				operation,getCurrentUser(operation),user,mimeType,copyrightId,false);
		}
		finally
		{
			if (singleOp)
			{
				// End Hibernate operation
				HibernateUtil.endOperation(operation);
			}
		}
		return allCategoriesOfOtherUsersResources;
	}
	
	/**
	 * @param user User or null to get resources from all users
	 * @param mimeType Filtering MIME type or empty string to get resources with all MIME types
	 * @param copyrightId Filtering copyright identifier or 0 to get resources with all copyrights
	 * @return List of resources from all categories of all users except the current one filtered by user 
	 * and MIME type and sorted by name
	 * @throws ServiceException
	 */
	public List<Resource> getAllCategoriesOfOtherUsersResourcesSortedByName(User user,String mimeType,
		long copyrightId) throws ServiceException
	{
		return getAllCategoriesOfOtherUsersResourcesSortedByName((Operation)null,user,mimeType,copyrightId);
	}
	
	/**
	 * @param operation Operation
	 * @param user User or null to get resources from all users
	 * @param mimeType Filtering MIME type or empty string to get resources with all MIME types
	 * @param copyrightId Filtering copyright identifier or 0 to get resources with all copyrights
	 * @return List of resources from all private categories of all users except the current one filtered 
	 * by user and MIME type and sorted by name
	 * @throws ServiceException
	 */
	public List<Resource> getAllCategoriesOfOtherUsersResourcesSortedByName(Operation operation,User user,
		String mimeType,long copyrightId) throws ServiceException
	{
		boolean singleOp=operation==null;
		List<Resource> allCategoriesOfOtherUsersResourcesSortedByName=null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			allCategoriesOfOtherUsersResourcesSortedByName=getAllCategoriesOfOtherUsersResources(
				operation,getCurrentUser(operation),user,mimeType,copyrightId,true);
		}
		finally
		{
			if (singleOp)
			{
				// End Hibernate operation
				HibernateUtil.endOperation(operation);
			}
		}
		return allCategoriesOfOtherUsersResourcesSortedByName;
	}
	
	/**
	 * @param user User or null to get total number of resources
	 * @return Number of resources filtered by user (or total)
	 * @throws ServiceException
	 */
	public int getResourcesCount(User user) throws ServiceException
	{
		return getResourcesCount(null,user,0L);
	}
	
	/**
	 * @param operation Operation
	 * @param user User or null to get total number of resources
	 * @return Number of resources filtered by user (or total)
	 * @throws ServiceException
	 */
	public int getResourcesCount(Operation operation,User user) throws ServiceException
	{
		return getResourcesCount(operation,user,0L);
	}
	
	/**
	 * @param user User or null to get number of resources without filtering by user
	 * @param categoryId Category identifier or 0L to get number of resources without filtering by category
	 * @return Number of resources optionally filtered by user and category
	 * @throws ServiceException
	 */
	public int getResourcesCount(User user,long categoryId) throws ServiceException
	{
		return getResourcesCount(null,user,categoryId);
	}
	
	/**
	 * @param operation Operation
	 * @param user User or null to get number of resources without filtering by user
	 * @param categoryId Category identifier or 0L to get number of resources without filtering by category
	 * @return Number of resources optionally filtered by user and category
	 * @throws ServiceException
	 */
	public int getResourcesCount(Operation operation,User user,long categoryId) throws ServiceException
	{
		int resourcesCount=0;
		boolean singleOp=operation==null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			RESOURCES_DAO.setOperation(operation);
			resourcesCount=RESOURCES_DAO.getResources(
				user==null?0L:user.getId(),categoryId,"",0L,false,false,false,false).size();
		}
		catch (DaoException de)
		{
			throw new ServiceException(de.getMessage(),de);
		}
		finally
		{
			if (singleOp)
			{
				// End Hibernate operation
				HibernateUtil.endOperation(operation);
			}
		}
		return resourcesCount;
	}
	
	/**
	 * Checks if exists a resource with the indicated identifier.
	 * @param id Identifier
	 * @return true if exists a resource with the indicated identifier, false otherwise
	 * @throws ServiceException
	 */
	public boolean checkResourceId(long id) throws ServiceException
	{
		return checkResourceId(null,id);
	}
	
	/**
	 * Checks if exists a resource with the indicated identifier.
	 * @param operation Operation
	 * @param id Identifier
	 * @return true if exists a resource with the indicated identifier, false otherwise
	 * @throws ServiceException
	 */
	public boolean checkResourceId(Operation operation,long id) throws ServiceException
	{
		boolean resourceFound=false;
		try
		{
			RESOURCES_DAO.setOperation(operation);
			resourceFound=RESOURCES_DAO.checkResourceId(id);
		}
		catch (DaoException de)
		{
			throw new ServiceException(de.getMessage(),de);
		}
		return resourceFound;
	}
	
	/**
	 * @param resourceName Resource name to check
	 * @param categoryId Category identifier
	 * @param resourceId Resource identifier (of the resource to exclude from checking)
	 * @return true if a resource name is available for the indicated category, false otherwise
	 */
	public boolean isResourceNameAvailable(String resourceName,long categoryId,long resourceId)
	{
		return isResourceNameAvailable(null,resourceName,categoryId,resourceId);
	}
	
	/**
	 * @param operation Operation
	 * @param resourceName Resource name to check
	 * @param categoryId Category identifier
	 * @param resourceId Resource identifier (of the resource to exclude from checking)
	 * @return true if a resource name is available for the indicated category, false otherwise
	 */
	public boolean isResourceNameAvailable(Operation operation,String resourceName,long categoryId,long resourceId)
	{
		boolean available=true;
		if (categoryId>0L)
		{
			boolean singleOp=operation==null;
			try
			{
				if (singleOp)
				{
					// Start Hibernate operation
					operation=HibernateUtil.startOperation();
				}
				
				RESOURCES_DAO.setOperation(operation);
				for (Resource resource:RESOURCES_DAO.getResources(0L,categoryId,"",0L,false,false,false,false))
				{
					if (resourceId!=resource.getId() && resourceName.equals(resource.getName()))
					{
						available=false;
						break;
					}
				}
			}
			catch (DaoException de)
			{
				throw new ServiceException(de.getMessage(),de);
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
		return available;
	}
	
	/**
	 * @param user User
	 * @param mimeType Filtering MIME type or empty string to get resources with all MIME types
	 * @param copyrightId Filtering copyright identifier or 0 to get resources with all copyrights
	 * @return Number of resources from all user global categories filtered by user and MIME type
	 * @throws ServiceException
	 */
	public int getAllUserGlobalCategoriesResourcesCount(User user,String mimeType,long copyrightId)
		throws ServiceException
	{
		return getAllUserGlobalCategoriesResourcesCount(null,user,mimeType,copyrightId);
	}
	
	/**
	 * @param operation Operation
	 * @param user User
	 * @param mimeType Filtering MIME type or empty string to get resources with all MIME types
	 * @param copyrightId Filtering copyright identifier or 0 to get resources with all copyrights
	 * @return Number of resources from all user global categories filtered by user and MIME type
	 * @throws ServiceException
	 */
	public int getAllUserGlobalCategoriesResourcesCount(Operation operation,User user,String mimeType,
		long copyrightId) throws ServiceException
	{
		int allUserGlobalCategoriesResourcesCount=0;
		boolean singleOp=operation==null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			List<Long> allUserGlobalCategoriesIds=categoriesService.getAllUserGlobalCategoriesIds(
				operation,user,getCategoryTypeByMIMEType(operation,mimeType));
			RESOURCES_DAO.setOperation(operation);
			List<Resource> allUserGlobalCategoriesResources=
				RESOURCES_DAO.getResources(0L,allUserGlobalCategoriesIds,mimeType,copyrightId,false,true,true,true);
			allUserGlobalCategoriesResourcesCount=allUserGlobalCategoriesResources.size();
		}
		catch (DaoException de)
		{
			throw new ServiceException(de.getMessage(),de);
		}
		finally
		{
			if (singleOp)
			{
				// End Hibernate operation
				HibernateUtil.endOperation(operation);
			}
		}
		return allUserGlobalCategoriesResourcesCount;
	}
	
	//Obtiene un recurso a partir de su id
	/**
	 * @param id Resource's identifier
	 * @return Resource
	 * @throws ServiceException
	 */
	public Resource getResource(long id) throws ServiceException
	{
		return getResource(null,id);
	}
	
	/**
	 * @param operation Operation
	 * @param id Resource's identifier
	 * @return Resource
	 * @throws ServiceException
	 */
	public Resource getResource(Operation operation,long id) throws ServiceException
	{
		Resource resource=null;
		try
		{
			// Get resource from DB
			RESOURCES_DAO.setOperation(operation);
			Resource resourceFromDB=RESOURCES_DAO.getResource(id,true,true,true);
			if (resourceFromDB!=null)
			{
				resource=resourceFromDB.getResourceCopy();
				if (resourceFromDB.getUser()!=null)
				{
					User resourceUser=resourceFromDB.getUser().getUserCopy();
					
					// Password is set to empty string before returning instance for security reasons
					resourceUser.setPassword("");
					
					resource.setUser(resourceUser);
				}
				if (resourceFromDB.getCategory()!=null)
				{
					Category categoryFromDB=resourceFromDB.getCategory();
					Category category=categoryFromDB.getCategoryCopy();
					if (categoryFromDB.getUser()!=null)
					{
						User resourceCategoryUser=categoryFromDB.getUser().getUserCopy();
						
						// Password is set to empty string before returning instance for security reasons
						resourceCategoryUser.setPassword("");
						
						category.setUser(resourceCategoryUser);
					}
					if (categoryFromDB.getCategoryType()!=null)
					{
						category.setCategoryType(categoryFromDB.getCategoryType().getCategoryTypeCopy());
					}
					if (categoryFromDB.getVisibility()!=null)
					{
						category.setVisibility(categoryFromDB.getVisibility().getVisibilityCopy());
					}
					resource.setCategory(category);
				}
				if (resourceFromDB.getCopyright()!=null)
				{
					resource.setCopyright(resourceFromDB.getCopyright().getCopyrightCopy());
				}
			}
		}
		catch (DaoException de)
		{
			throw new ServiceException(de.getMessage(),de);
		}
		return resource; 
	}
	
	//Modifica un recurso que mantiene el mismo fichero
	/**
	 *  Updates a resource but keeping intact its associated file.
	 *  @throws ServiceException
	 */
	public void updateResource(ResourceBean resource) throws ServiceException
	{
		updateResource(resource,false);
	}
	
	//Modifica un recurso que contiene un fichero diferente
	/**
	 * Updates a resource and also its associated file if it is indicated.
	 * @param resource Resource to update
	 * @param updateFile true if associated file is going to be updated, false otherwise
	 * @throws ServiceException
	 */
	public void updateResource(ResourceBean resource,boolean updateFile) throws ServiceException
	{
		Resource res=resource.getResource();
		File backupFile=null;
		String oldResourceFilePath=null;
		String newResourceFilePath=null;
		boolean ok=false;
		Operation operation=null;
		try
		{
			// Start Hibernate operation
			operation=HibernateUtil.startOperation();
			
			oldResourceFilePath=getCurrentResourceFilePath(resource);
			newResourceFilePath=getNewResourceFilePath(resource);
			
			if (updateFile)
			{
				// We try to keep a backup to recover old file if operation fails
				if (newResourceFilePath.equals(oldResourceFilePath))
				{
					try
					{
						backupFile=createBackupFile(new File(oldResourceFilePath));
					}
					catch (IOException ioe)
					{
						// It's not possible to get a valid backup file, continue operation any way
						backupFile=null;
					}
				}
				else
				{
					// As new resource file is a different file we can use old resource file as backup
					backupFile=new File(oldResourceFilePath);
					if (!backupFile.exists() || !backupFile.isFile())
					{
						// It's not possible to get a valid backup file, continue operation any way
						backupFile=null;
					}
				}
				
				// Save updated file
				byte[] contents=resource.getFile()!=null?resource.getFile().getContents():resource.getUrlContent();
				saveNewResourceFile(newResourceFilePath,contents);
			}
			
			// Update new resource file name
			res.setFileName(getNewResourceFileName(resource));
			
			// Get resource from DB
			RESOURCES_DAO.setOperation(operation);
			Resource resourceFromDB=RESOURCES_DAO.getResource(res.getId(),false,false,false);
			
			// Set fields with the updated values
			resourceFromDB.setFromOtherResource(res);
			
			// Update resource
			RESOURCES_DAO.setOperation(operation);
			RESOURCES_DAO.updateResource(resourceFromDB);
			
			// Do commit
			operation.commit();
			
			// Operation OK
			ok=true;
		}
		catch (IOException ioe)
		{
			throwServiceException(
				"RESOURCE_FILE_UPDATE_ERROR","Error updating the file corresponding to the resource.",ioe);
		}
		catch (DaoException de)
		{
			// Do rollback
			operation.rollback();
			
			throw new ServiceException(de.getMessage(),de);
		}
		finally
		{
			// End Hibernate operation
			HibernateUtil.endOperation(operation);
			
			// We will try to restore old file from backup if operation has failed and we have a valid backup
			if (backupFile!=null)
			{
				if (!ok)
				{
					try
					{
						restoreBackupFile(new File(oldResourceFilePath),backupFile);
					}
					catch (IOException ioe)
					{
						// Restoring backup file has failed, perhaps we could treat this case
						// but we ignore it for now
					}
				}
				if (newResourceFilePath.equals(oldResourceFilePath))
				{
					// Delete backup temporary file
					deleteResourceFile(backupFile);
				}
			}
			// We need to delete old resource file if operation has been succesful 
			// and updating has not owerwrited it
			if (ok && updateFile && !newResourceFilePath.equals(oldResourceFilePath))
			{
				deleteResourceFile(new File(oldResourceFilePath));
			}
		}
	}
	
	//TODO No es invocada... ¿quizas sea obsoleta?
	//TODO ¿Y si varios usuarios modifican a la vez el mismo recurso? la operacion de BD al hacerse por transacción ok... solo valdría la última, ¿pero y el fichero?.... así en principio pienso que sí un usuario escribe en el fichero y luego otro intenta escribir sin que haya acabado el primero el segundo no va a poder... le ocurrira una IOException... en ese caso lo más probable es que se intentaría restaurar el backup aunque esto es posible que tampoco se pudiera por estar bloqueado el archivo, pero ¿y si justo en ese momento termina el primer usuario y la restauración del backup sí es posible? no es que sea muy probable pero en ese caso se volvería a poner el fichero anterior pero el resto de modificaciones del recurso valdrían las del primer usuario, lo cual sería inválido, ahora bien esto creo que es muy improbable de que ocurra
	//TODO otro caso de conflicto de modificación simultanea podría ser que el primer usuario subio ya el archivo y termino la subida y en ese momento el segundo usuario sube su archivo sin errores y aplica sus modificaciones en BD antes que el primer usuario.... si esto ocurre... que también es muy improbable porque el segundo usuario tendría que haber sido rapidísimo para adelantar al primero no habría errores.... pero quedaría el fichero que subio el segundo usuario junto con el resto de datos del recurso segun los modifico el primer usuario (NOTA... este caso me parece incluso más improbable que el primero)
	/**
	 * Updates a resource and also its associated file if it is indicated.
	 * @param operation Operation
	 * @param resource Resource to update
	 * @param updateFile true if associated file is going to be updated, false otherwise
	 * @throws ServiceException
	 */
	public void updateResource(Operation operation,ResourceBean resource,boolean updateFile) throws ServiceException
	{
		Resource res=resource.getResource();
		boolean singleOp=operation==null;
		File backupFile=null;
		String oldResourceFilePath=null;
		String newResourceFilePath=null;
		boolean ok=false;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			else
			{
				operation=resource.getCurrentUserOperation(operation);
			}
			
			oldResourceFilePath=getCurrentResourceFilePath(resource);
			newResourceFilePath=getNewResourceFilePath(resource);
			
			if (updateFile)
			{
				// We try to keep a backup to recover old file if operation fails
				if (newResourceFilePath.equals(oldResourceFilePath))
				{
					try
					{
						backupFile=createBackupFile(new File(oldResourceFilePath));
					}
					catch (IOException ioe)
					{
						// It's not possible to get a valid backup file, continue operation any way
						backupFile=null;
					}
				}
				else
				{
					// As new resource file is a different file we can use old resource file as backup
					backupFile=new File(oldResourceFilePath);
					if (!backupFile.exists() || !backupFile.isFile())
					{
						// It's not possible to get a valid backup file, continue operation any way
						backupFile=null;
					}
				}
				
				// Save updated file
				byte[] contents=resource.getFile()!=null?resource.getFile().getContents():resource.getUrlContent();
				saveNewResourceFile(newResourceFilePath,contents);
			}
			
			// Update new resource file name
			res.setFileName(getNewResourceFileName(resource));
			
			// Get resource from DB
			RESOURCES_DAO.setOperation(operation);
			Resource resourceFromDB=RESOURCES_DAO.getResource(res.getId(),false,false,false);
			
			// Set fields with the updated values
			resourceFromDB.setFromOtherResource(res);
			
			// Update resource
			RESOURCES_DAO.setOperation(operation);
			RESOURCES_DAO.updateResource(resourceFromDB);
			
			if (singleOp)
			{
				// Do commit
				operation.commit();
			}
			
			// Operation OK
			ok=true;
		}
		catch (IOException ioe)
		{
			throwServiceException(
				"RESOURCE_FILE_UPDATE_ERROR","Error updating the file corresponding to the resource.",ioe);
		}
		catch (DaoException de)
		{
			if (singleOp)
			{
				// Do rollback
				operation.rollback();
			}
			
			throw new ServiceException(de.getMessage(),de);
		}
		finally
		{
			if (singleOp)
			{
				// End Hibernate operation
				HibernateUtil.endOperation(operation);
			}
			
			// We will try to restore old file from backup if operation has failed and we have a valid backup
			if (backupFile!=null)
			{
				if (!ok)
				{
					try
					{
						restoreBackupFile(new File(oldResourceFilePath),backupFile);
					}
					catch (IOException ioe)
					{
						// Restoring backup file has failed, perhaps we could treat this case
						// but we ignore it for now
					}
				}
				if (newResourceFilePath.equals(oldResourceFilePath))
				{
					// Delete backup temporary file
					deleteResourceFile(backupFile);
				}
			}
			// We need to delete old resource file if operation has been succesful 
			// and updating has not owerwrited it
			if (ok && updateFile && !newResourceFilePath.equals(oldResourceFilePath))
			{
				deleteResourceFile(new File(oldResourceFilePath));
			}
		}
	}
	
	//Añade un nuevo recurso
	/**
	 * Adds a new resource.
	 * @param resource Resource to add
	 * @throws ServiceException
	 */
	public void addResource(ResourceBean resource) throws ServiceException
	{
		addResource(null,resource);
	}
	
	/**
	 * Adds a new resource.
	 * @param operation Operation
	 * @param resource Resource to add
	 * @throws ServiceException
	 */
	public void addResource(Operation operation,ResourceBean resource) throws ServiceException
	{
		Resource res=resource.getResource();
		boolean singleOp=operation==null;
		String newResourceFilePath=null;
		boolean ok=false;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			else
			{
				operation=resource.getCurrentUserOperation(operation);
			}
			
			// Create new resource
			RESOURCES_DAO.setOperation(operation);
			res.setId(RESOURCES_DAO.saveResource(res));
			
			// Save file
			byte[] contents=resource.getFile()!=null?resource.getFile().getContents():resource.getUrlContent();
			newResourceFilePath=getNewResourceFilePath(resource);
			saveNewResourceFile(newResourceFilePath,contents);
			
			// Update resource file name
			res.setFileName(getNewResourceFileName(resource));
			
			// Get resource from DB
			RESOURCES_DAO.setOperation(operation);
			Resource resourceFromDB=RESOURCES_DAO.getResource(res.getId(),false,false,false);
			
			// Set fields with the updated values
			resourceFromDB.setFromOtherResource(res);
			
			// Update resource
			RESOURCES_DAO.setOperation(operation);
			RESOURCES_DAO.updateResource(resourceFromDB);
			
			if (singleOp)
			{
				// Do commit
				operation.commit();
			}
			
			// Operation OK
			ok=true;
		}
		catch (IOException ioe)
		{
			throwServiceException(
				"RESOURCE_FILE_ADD_ERROR","Error creating the file corresponding to the resource.",ioe);
		}
		catch (DaoException de)
		{
			throw new ServiceException(de.getMessage(),de);
		}
		finally
		{
			// If operation has been failed and we have created a resource file we need to delete it
			if (!ok && newResourceFilePath!=null)
			{
				deleteResourceFile(new File(newResourceFilePath));
			}
			if (!ok)
			{
				if (singleOp)
				{
					// Do rollback
					operation.rollback();
				}
			}
			
			if (singleOp)
			{
				// End Hibernate operation
				HibernateUtil.endOperation(operation);
			}
		}
	}
	
	//Elimina un recurso
	/**
	 * Deletes a resource.
	 * @param resourceId Resource identifier
	 * @throws ServiceException
	 */
	public void deleteResource(long resourceId) throws ServiceException
	{
		deleteResource(null,resourceId);
	}
	
	/**
	 * Deletes a resource.
	 * @param operation Operation
	 * @param resourceId Resource identifier
	 * @throws ServiceException
	 */
	public void deleteResource(Operation operation,long resourceId) throws ServiceException
	{
		boolean singleOp=operation==null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			// Get resource from DB
			RESOURCES_DAO.setOperation(operation);
			Resource res=RESOURCES_DAO.getResource(resourceId,false,false,false);
			
			// Delete resource
			RESOURCES_DAO.setOperation(operation);
			RESOURCES_DAO.deleteResource(res);
			
			//Delete resource file
			ResourceBean deleteResourceBean=new ResourceBean(res);
			deleteResourceBean.setResourcesService(this);
			deleteResourceBean.setUserSessionService(getUserSessionService());
			String filePath=getCurrentResourceFilePath(deleteResourceBean);
			deleteResourceFile(new File(filePath));
			
			if (singleOp)
			{
				// Do commit
				operation.commit();
			}
		}
		catch (DaoException de)
		{
			if (singleOp)
			{
				// Do rollback
				operation.rollback();
			}
			if (de.getCause() instanceof ConstraintViolationException)
			{
				throw new ResourceDeleteConstraintServiceException(de.getMessage(),de);
			}
			else
			{
				throw new ServiceException(de.getMessage(),de);
			}
		}
		catch (ServiceException se)
		{
			if (se.getCause() instanceof DaoException)
			{
				DaoException de=(DaoException)se.getCause();
				if (de.getCause() instanceof ConstraintViolationException)
				{
					throw new ResourceDeleteConstraintServiceException(de.getMessage(),de);
				}
				else
				{
					throw se;
				}
			}
			else
			{
				throw se;
			}
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
	
	/**
	 * Delete a file.
	 * @param file File to delete
	 * @return true if file is correctly deleted, false otherwise
	 */
	public boolean deleteResourceFile(File file)
	{
		boolean ok=false;
		if (file!=null)
		{
			if (file.exists() && file.isFile())
			{
				file.delete();
			}
			ok=!file.exists();
		}
		return ok;
	}
	
	/**
	 * Creates a backup temporary file.
	 * @param file File to backup
	 * @return Backup temporary file
	 * @throws IOException
	 */
	public File createBackupFile(File file) throws IOException
	{
		File backupFile=File.createTempFile("gepeq_bak_tmp",null);
		FileInputStream input=new FileInputStream(file);
		FileOutputStream output=new FileOutputStream(backupFile);
		byte[] buffer=new byte[BACKUP_BUFFER_SIZE];
		int bytesRead=input.read(buffer);
		while (bytesRead>0)
		{
			output.write(buffer,0,bytesRead);
			bytesRead=input.read(buffer);
		}
		input.close();
		output.close();
		return backupFile;
	}
	
	/**
	 * Restores a file from a backup file
	 * @param file File to restore
	 * @param backupFile Backup file
	 * @throws IOException
	 */
	public void restoreBackupFile(File file,File backupFile) throws IOException
	{
		FileInputStream input=new FileInputStream(backupFile);
		FileOutputStream output=new FileOutputStream(file);
		byte[] buffer=new byte[BACKUP_BUFFER_SIZE];
		int bytesRead=input.read(buffer);
		while (bytesRead>0)
		{
			output.write(buffer,0,bytesRead);
			bytesRead=input.read(buffer);
		}
		input.close();
		output.close();
	}
	
	/**
	 * Saves uploaded file content from a file in a temporary file within temporal folder.
	 * @param resource Resource with an uploaded file content
	 * @return Temporary file or null if resource has no content to save
	 * @throws IOException
	 */
	public File saveTemporaryFile(ResourceBean resource) throws IOException
	{
		File tmpFile=null;
		byte[] contents=resource.getFile()!=null?resource.getFile().getContents():resource.getUrlContent();
		if (contents!=null)
		{
			tmpFile=File.createTempFile("gepeq_res_tmp_",null,new File(configurationService.getTmpPath()));
			tmpFile.deleteOnExit();
			FileOutputStream output=new FileOutputStream(tmpFile);
			output.write(contents);
			output.close();
		}
		return tmpFile;
	}
	
	/**
	 * Upload a file from an URL to our server.
	 * @param url URL from which we are going to get the file.
	 * @param sizeLimit Maximum size allowed in bytes for the uploaded file (0 or negative value for 
	 * unlimited maximum size)
	 * @return Contents of the uploaded file as an array of bytes.
	 * @throws MalformedURLException Throwed if URL is invalid
	 * @throws IOException Throwed if occurs an I/O error
	 * @throws Exception Throwed if uploaded file size is greater than maximum size allowed, to be able 
	 * to check it the <i>getMessage()</i> method of this exception will return "FILE_SIZE_LIMIT"
	 */
	public byte[] uploadFromURL(String url,int sizeLimit) throws MalformedURLException,IOException,Exception
	{
		byte[] content=null;
		ByteArrayOutputStream urlOutputStream=new ByteArrayOutputStream();
		InputStream urlInputStream=null;
		try
		{
			urlInputStream=ProxyConnector.openConnection(new URL(url)).getInputStream();
			byte[] buffer=new byte[UPLOAD_BUFFER_SIZE];
			int totalBytesRead=0;
			int bytesRead=urlInputStream.read(buffer);
			while (bytesRead>0)
			{
				totalBytesRead+=bytesRead;
				if (sizeLimit>0 && totalBytesRead>sizeLimit)
				{
					throw new Exception("FILE_SIZE_LIMIT");
				}
				else
				{
					urlOutputStream.write(buffer,0,bytesRead);
					bytesRead=urlInputStream.read(buffer);
				}
			}
			content=urlOutputStream.toByteArray();
		}
		finally
		{
			if (urlInputStream!=null)
			{
				try
				{
					urlInputStream.close();
				}
				catch (IOException e)
				{
				}
			}
			try
			{
				urlOutputStream.close();
			}
			catch (IOException e)
			{
			}
		}
		return content;
	}
	
	/**
	 * @param ext Extension
	 * @return Default MIME type that corresponds to an extension or null if it is not supported a default 
	 * MIME type for that extension
	 */
	public String getDefaultMimeType(String ext)
	{
		return ext==null?null:SUPPORTED_MIMES.get(ext.toLowerCase());
	}
	
	/**
	 * Check that MIME type of the received file (an image) is the predicted one.
	 * @param file File (an image)
	 * @param mimeType MIME type of the file (an image)
	 * @return true if MIME type of the received file (an image) is the predicted one, false otherwise
	 */
	public boolean checkImage(File file,String mimeType)
	{
		int[] imageDimensions=getImageDimensionsByMIME(file,mimeType);
		return imageDimensions[0]>=0 && imageDimensions[1]>=0;
	}
	
	/**
	 * Check that MIME type of the received file is the predicted one.<br/><br/>
	 * If it is not then checks that MIME type is the default for the extension associated with the file.
	 * <br/><br/>
	 * Result is the MIME type of the file or null if check fails. 
	 * @param file Files
	 * @param ext Extension associated with this file (not necessarily the actual extension)
	 * @param mimeType Predicted MIME type or null to only check extension
	 * @return MIME type of the file or null if check fails.
	 */
	public String checkMimeType(File file,String ext,String mimeType)
	{
		String checkedMimeType=null;
		if (mimeType!=null)
		{
			if (mimeType.startsWith("image/"))
			{
				checkedMimeType=checkImage(file,mimeType)?mimeType:null;
			}
		}
		if (checkedMimeType==null)
		{
			String defaultMimeType=getDefaultMimeType(ext);
			if (defaultMimeType!=null && !defaultMimeType.equals(mimeType))
			{
				if (defaultMimeType.startsWith("image/"))
				{
					checkedMimeType=checkImage(file,defaultMimeType)?defaultMimeType:null;
				}
			}
		}
		return checkedMimeType;
	}
	
	/**
	 * @param file File
	 * @param mimeType MIME type of the file
	 * @return Dimensions of an image file as an array of 2 integer values: first value will be width 
	 * and second one height, both values will be measured in pixels
	 */
	public int[] getImageDimensionsByMIME(File file,String mimeType)
	{
		int[] imageDimensions=new int[2];
		imageDimensions[0]=-1;
		imageDimensions[1]=-1;
		if (file!=null)
		{
			Iterator<ImageReader> iter=ImageIO.getImageReadersByMIMEType(mimeType);
			while (imageDimensions[0]==-1 && imageDimensions[1]==-1 && iter.hasNext())
			{
				ImageReader reader=iter.next();
				try
				{
					ImageInputStream stream=new FileImageInputStream(file);
					reader.setInput(stream);
					imageDimensions[0]=reader.getWidth(reader.getMinIndex());
					imageDimensions[1]=reader.getHeight(reader.getMinIndex());
				}
				catch (Exception e)
				{
					imageDimensions[0]=-1;
					imageDimensions[1]=-1;
				}
				finally
				{
					reader.dispose();
				}
			}
		}
		return imageDimensions;
	}
	
	/**
	 * @param resourceId Resource identifier
	 * @return Image dimensions
	 */
	public int[] getImageDimensions(long resourceId)
	{
		return getImageDimensions(getResource(resourceId));
	}
	
	/**
	 * @param resource Resource
	 * @return Image dimensions
	 */
	private int[] getImageDimensions(Resource resource)
	{
		int[] imageDimensions=null;
		if (resource!=null && resource.getId()>0L && resource.getMimeType() !=null && 
			resource.getMimeType().startsWith("image/"))
		{
			imageDimensions=
				getImageDimensionsByMIME(new File(getResourceFilePath(resource)),resource.getMimeType());
		}
		else
		{
			imageDimensions=new int[2];
			imageDimensions[0]=-1;
			imageDimensions[1]=-1;
		}
		return imageDimensions;
	}
	
	/**
	 * @param resourceId Resource identifier
	 * @return Image dimensions as string or empty string if resource is not an image
	 */
	public String getImageDimensionsString(long resourceId)
	{
		return getImageDimensionsString(null,resourceId);
	}
	
	/**
	 * @param operation Operation
	 * @param resourceId Resource identifier
	 * @return Image dimensions as string or empty string if resource is not an image
	 */
	public String getImageDimensionsString(Operation operation,long resourceId)
	{
		return getImageDimensionsString(getResource(operation,resourceId));
	}
	
	/**
	 * @param resource Resource
	 * @return Image dimensions as string or empty string if resource is not an image
	 */
	private String getImageDimensionsString(Resource resource)
	{
		StringBuffer imageDimensionsString=new StringBuffer();
		int[] imageDimensions=getImageDimensions(resource);
		if (imageDimensions[0]!=-1 && imageDimensions[1]!=-1)
		{
			imageDimensionsString.append(imageDimensions[0]);
			imageDimensionsString.append(" X ");
			imageDimensionsString.append(imageDimensions[1]);
		}
		return imageDimensionsString.toString();
	}
	
	/**
	 * @param resource Resource
	 * @return Full path to the current file associated to a resource
	 */
	public String getResourceFilePath(Resource resource)
	{
		StringBuffer resourceFilePath=null;
		if (resource!=null && resource.getFileName()!=null)
		{
			resourceFilePath=new StringBuffer();
			resourceFilePath.append(configurationService.getApplicationPath());
			resourceFilePath.append(resource.getFileName().replace('/',File.separatorChar));
		}
		return resourceFilePath==null?null:resourceFilePath.toString();
	}
	
	/**
	 * @param resource Resource bean
	 * @return Full path to the current file associated to a resource
	 * @throws ServiceException
	 */
	public String getCurrentResourceFilePath(ResourceBean resource) throws ServiceException
	{
		String currentResourceFilePath=null;
		if (resource!=null)
		{
			currentResourceFilePath=getResourceFilePath(resource.getResource());
		}
		return currentResourceFilePath;
	}
	
	/**
	 * @param resource Resource
	 * @return Full path to the new file that we are going to associate to a resource
	 * @throws ServiceException
	 */
	private String getNewResourceFilePath(ResourceBean resource) throws ServiceException
	{
		StringBuffer newResourceFilePath=new StringBuffer();
		Resource res=resource.getResource();
		if (resource.isUploaded())
		{
			newResourceFilePath.append(configurationService.getResourcesPath());
			newResourceFilePath.append(File.separatorChar);
			newResourceFilePath.append(res.getId());
			String fullName=resource.getFile()!=null?resource.getFile().getFileName():resource.getUploadedUrl();
			int iExt=fullName.lastIndexOf('.');
			if (iExt!=-1 && iExt<fullName.length()-1)
			{
				newResourceFilePath.append(fullName.substring(iExt));
			}
		}
		else
		{
			newResourceFilePath.append(configurationService.getApplicationPath());
			newResourceFilePath.append(res.getFileName().replace('/',File.separatorChar));
		}
		return newResourceFilePath.toString();
	}
	
	/**
	 * Get a new file name to give to a resource.<br/><br/>
	 * Note that this new name will be a relative valid path from web application root so it will be a viewable
	 * file from client side.
	 * @param resource Resource
	 * @return New file name to give to a resource
	 * @throws ServiceException
	 */
	private String getNewResourceFileName(ResourceBean resource) throws ServiceException
	{
		StringBuffer newResourceFileName=new StringBuffer();
		Resource res=resource.getResource();
		if (resource.isUploaded())
		{
			newResourceFileName.append('/');
			newResourceFileName.append(configurationService.getResourcesFolder());
			newResourceFileName.append('/');
			newResourceFileName.append(res.getId());
			String fullName=resource.getFile()!=null?resource.getFile().getFileName():resource.getUploadedUrl();
			int iExt=fullName.lastIndexOf('.');
			if (iExt!=-1 && iExt<fullName.length()-1)
			{
				newResourceFileName.append(fullName.substring(iExt));
			}
		}
		else
		{
			newResourceFileName.append(res.getFileName());
		}
		return newResourceFileName.toString();
	}
	
	/**
	 * Save a new file with the received contents in the indicated path.
	 * @param filePath File path
	 * @param contents Contents as array of bytes
	 * @throws IOException
	 */
	private void saveNewResourceFile(String filePath,byte[] contents) throws IOException
	{
		File file=new File(filePath);
		FileOutputStream output=new FileOutputStream(file);
		output.write(contents);
		output.close();
	}
	
	/**
	 * Throws a new ServiceException with the localized message get from error code or the plain message 
	 * if localization fails.
	 * @param errorCode Error code to get localized error message
	 * @param plainMessage Error message to be used if localization fails
	 * @throws ServiceException
	 */
	private void throwServiceException(String errorCode,String plainMessage) throws ServiceException
	{
		throwServiceException(errorCode,plainMessage,null);
	}
	
	/**
	 * Throws a new ServiceException with the localized message get from error code or the plain message 
	 * if localization fails.
	 * @param errorCode Error code to get localized error message
	 * @param plainMessage Error message to be used if localization fails
	 * @param cause Exception that caused this exception
	 * @throws ServiceException
	 */
	private void throwServiceException(String errorCode,String plainMessage,Throwable cause) 
		throws ServiceException
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
		throw new ServiceException(errorMessage,cause);
	}
	
	/**
	 * @return User session service from context
	 */
	private UserSessionService getUserSessionService()
	{
		FacesContext context=FacesContext.getCurrentInstance();
		return (UserSessionService)context.getApplication().getELResolver().getValue(
			context.getELContext(),null,"userSessionService");
	}
	
	/**
	 * @param operation Operation
	 * @return Current user
	 */
	private User getCurrentUser(Operation operation)
	{
		return getUserSessionService().getCurrentUser(operation);
		
	}
}
