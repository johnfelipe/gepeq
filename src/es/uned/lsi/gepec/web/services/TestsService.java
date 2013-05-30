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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.context.FacesContext;

import es.uned.lsi.gepec.model.dao.DaoException;
import es.uned.lsi.gepec.model.dao.TestsDao;
import es.uned.lsi.gepec.model.entities.Category;
import es.uned.lsi.gepec.model.entities.Evaluator;
import es.uned.lsi.gepec.model.entities.QuestionOrder;
import es.uned.lsi.gepec.model.entities.Section;
import es.uned.lsi.gepec.model.entities.SupportContact;
import es.uned.lsi.gepec.model.entities.Test;
import es.uned.lsi.gepec.model.entities.TestFeedback;
import es.uned.lsi.gepec.model.entities.TestUser;
import es.uned.lsi.gepec.model.entities.User;
import es.uned.lsi.gepec.util.HibernateUtil;
import es.uned.lsi.gepec.util.HibernateUtil.Operation;

//Ofrece a la vista operaciones con pruebas
//author Víctor Manuel Alonso Rodríguez
//since  12/2011
/**
 * Manages tests.
 */
@SuppressWarnings("serial")
@ManagedBean(eager=true)
@ApplicationScoped
public class TestsService implements Serializable
{
	public final static int OPERATION_OK=0;
	public final static int OPERATION_KO=-1;
	
	private final static TestsDao TESTS_DAO=new TestsDao();
	
	@ManagedProperty(value="#{categoriesService}")
	private CategoriesService categoriesService;
	@ManagedProperty(value="#{categoryTypesService}")
	private CategoryTypesService categoryTypesService;
	@ManagedProperty(value="#{visibilitiesService}")
	private VisibilitiesService visibilitiesService;
	@ManagedProperty(value="#{sectionsService}")
	private SectionsService sectionsService;
	@ManagedProperty(value="#{supportContactsService}")
	private SupportContactsService supportContactsService;
	@ManagedProperty(value="#{evaluatorsService}")
	private EvaluatorsService evaluatorsService;
	@ManagedProperty(value="#{testUsersService}")
	private TestUsersService testUsersService;
	@ManagedProperty(value="#{testFeedbacksService}")
	private TestFeedbacksService testFeedbacksService;
	@ManagedProperty(value="#{localizationService}")
	private LocalizationService localizationService;
	@ManagedProperty(value="#{permissionsService}")
	private PermissionsService permissionsService;
	
	public TestsService()
	{
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
	
	public void setSectionsService(SectionsService sectionsService)
	{
		this.sectionsService=sectionsService;
	}
	
	public void setSupportContactsService(SupportContactsService supportContactsService)
	{
		this.supportContactsService=supportContactsService;
	}
	
	public void setEvaluatorsService(EvaluatorsService evaluatorsService)
	{
		this.evaluatorsService=evaluatorsService;
	}
	
	public void setTestUsersService(TestUsersService testUsersService)
	{
		this.testUsersService=testUsersService;
	}
	
	public void setTestFeedbacksService(TestFeedbacksService testFeedbacksService)
	{
		this.testFeedbacksService=testFeedbacksService;
	}
	
	public void setLocalizationService(LocalizationService localizationService)
	{
		this.localizationService=localizationService;
	}
	
	public void setPermissionsService(PermissionsService permissionsService)
	{
		this.permissionsService=permissionsService;
	}
	
	//Obtiene todas las pruebas de un usuario
	/**
	 * @param user User or null to get tests from all users
	 * @return List of tests filtered by user
	 * @throws ServiceException
	 */
	public List<Test> getTests(User user) throws ServiceException
	{
		return getTests((Operation)null,user);
	}
	
	/**
	 * @param operation Operation
	 * @param user User or null to get tests from all users
	 * @return List of tests filtered by user
	 * @throws ServiceException
	 */
	public List<Test> getTests(Operation operation,User user) throws ServiceException
	{
		boolean singleOp=operation==null;
		List<Test> tests=null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			tests=getTests(operation,getCurrentUser(operation),user,0L,true);
		}
		finally
		{
			if (singleOp)
			{
				// End Hibernate operation
				HibernateUtil.endOperation(operation);
			}
		}
		return tests; 
	}
	
	/**
	 * @param user User or null to get tests from all users
	 * @param categoryId Filtering category identifier or 0 to get tests from all categories
	 * @param includeSubcategories Include tests from categories derived from filtering category
	 * @return List of tests filtered by user and category (and optionally subcategories)
	 * @throws ServiceException
	 */
	public List<Test> getTests(User user,long categoryId,boolean includeSubcategories) throws ServiceException
	{
		return getTests((Operation)null,user,categoryId,includeSubcategories);
	}
	
	/**
	 * @param operation Operation
	 * @param user User or null to get tests from all users
	 * @param categoryId Filtering category identifier or 0 to get tests from all categories
	 * @param includeSubcategories Include tests from categories derived from filtering category
	 * @return List of tests filtered by user and category (and optionally subcategories)
	 * @throws ServiceException
	 */
	public List<Test> getTests(Operation operation,User user,long categoryId,boolean includeSubcategories) 
		throws ServiceException
	{
		boolean singleOp=operation==null;
		List<Test> tests=null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			tests=getTests(operation,getCurrentUser(operation),user,categoryId,includeSubcategories);
		}
		finally
		{
			if (singleOp)
			{
				// End Hibernate operation
				HibernateUtil.endOperation(operation);
			}
		}
		return tests;
	}
	
	/**
	 * @param viewer User used to check visibility
	 * @param user User or null to get tests from all users
	 * @return List of tests filtered by user
	 * @throws ServiceException
	 */
	public List<Test> getTests(User viewer,User user) throws ServiceException
	{
		return getTests(null,viewer,user,0L,true);
	}
	
	/**
	 * @param operation Operation
	 * @param viewer User used to check visibility
	 * @param user User or null to get tests from all users
	 * @return List of tests filtered by user
	 * @throws ServiceException
	 */
	public List<Test> getTests(Operation operation,User viewer,User user) throws ServiceException
	{
		return getTests(operation,viewer,user,0L,true);
	}
	
	/**
	 * @param viewer User used to check visibility
	 * @param user User or null to get tests from all users
	 * @param categoryId Filtering category identifier or 0 to get tests from all categories
	 * @param includeSubcategories Include tests from categories derived from filtering category
	 * @return List of tests filtered by user and category (and optionally subcategories)
	 * @throws ServiceException
	 */
	public List<Test> getTests(User viewer,User user,long categoryId,boolean includeSubcategories) 
		throws ServiceException
	{
		return getTests(null,viewer,user,categoryId,includeSubcategories);
	}
	
	/**
	 * @param operation Operation
	 * @param viewer User used to check visibility
	 * @param user User or null to get tests from all users
	 * @param categoryId Filtering category identifier or 0 to get tests from all categories
	 * @param includeSubcategories Include tests from categories derived from filtering category
	 * @return List of tests filtered by user and category (and optionally subcategories)
	 * @throws ServiceException
	 */
	public List<Test> getTests(Operation operation,User viewer,User user,long categoryId,boolean includeSubcategories) 
		throws ServiceException
	{
		boolean singleOp=operation==null;
		List<Test> tests=null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			boolean checkVisibility=false;
			if (categoryId>0L)
			{
				// Check permissions to determine if we need to check categories visibility
				Category category=categoriesService.getCategory(operation,categoryId);
				checkVisibility=permissionsService.isDenied(
					operation,viewer,"PERMISSION_TESTS_VIEW_TESTS_OF_OTHER_USERS_PRIVATE_CATEGORIES_ENABLED") ||
					(permissionsService.isGranted(
					operation,category.getUser(),"PERMISSION_NAVIGATION_ADMINISTRATION") && 
					permissionsService.isDenied(
					operation,viewer,"PERMISSION_TESTS_VIEW_TESTS_OF_ADMINS_PRIVATE_CATEGORIES_ENABLED")) ||
					(permissionsService.isGranted(
					operation,category.getUser(),"PERMISSION_ADMINISTRATION_RAISE_PERMISSIONS_OVER_OWNED_ALLOWED") &&
					permissionsService.isDenied(
					operation,viewer,"PERMISSION_TESTS_VIEW_TESTS_OF_SUPERADMINS_PRIVATE_CATEGORIES_ENABLED"));
				if (checkVisibility && !category.getUser().equals(viewer)&& !category.getVisibility().isGlobal() && 
					category.getVisibility().getLevel()>=visibilitiesService.getVisibility(
					operation,"CATEGORY_VISIBILITY_PRIVATE").getLevel())
				{
					throwServiceException(
						"NON_AUTHORIZED_ACTION_ERROR","You are not authorized to execute that operation.");
				}
			}
			
			if (!includeSubcategories || categoryId<=0L)
			{
				TESTS_DAO.setOperation(operation);
				tests=TESTS_DAO.getTests(user==null?0L:user.getId(),categoryId,true,true,true,true,true,true);
				if (categoryId<=0L)
				{
					Map<Category,Boolean> checkedCategoriesVisibility=new HashMap<Category,Boolean>();
					List<Test> testsToRemove=new ArrayList<Test>();
					for (Test test:tests)
					{
						Category testCategory=test.getCategory();
						boolean checkedCategoryVisibility=false;						
						if (checkedCategoriesVisibility.containsKey(testCategory))
						{
							checkedCategoryVisibility=checkedCategoriesVisibility.get(testCategory).booleanValue();
						}
						else
						{
							checkedCategoryVisibility=testCategory.getUser().equals(viewer) || 
								testCategory.getVisibility().isGlobal() || 
								testCategory.getVisibility().getLevel()<=visibilitiesService.getVisibility(
								operation,"CATEGORY_VISIBILITY_PUBLIC").getLevel();
							checkedCategoriesVisibility.put(testCategory,Boolean.valueOf(checkedCategoryVisibility));
						}
						if (!checkedCategoryVisibility)
						{
							testsToRemove.add(test);
						}
					}
					for (Test testToRemove:testsToRemove)
					{
						tests.remove(testToRemove);
					}
				}
			}
			else
			{
				TESTS_DAO.setOperation(operation);
				tests=TESTS_DAO.getTests(user==null?0L:user.getId(),
					categoriesService.getDerivedCategoriesIds(operation,categoryId,viewer,
					categoryTypesService.getCategoryType(operation,"CATEGORY_TYPE_TESTS"),checkVisibility),true,true,
					true,true,true,true);
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
		return tests;
	}
	
	/**
	 * @param user User or null to get tests from all users
	 * @return List of tests from all categories filtered by user, question type and question level
	 * @throws ServiceException
	 */
	public List<Test> getAllCategoriesTests(User user) throws ServiceException
	{
		return getAllCategoriesTests((Operation)null,user);
	}
	
	/**
	 * @param operation Operation
	 * @param user User or null to get tests from all users
	 * @return List of tests from all categories filtered by user, question type and question level
	 * @throws ServiceException
	 */
	public List<Test> getAllCategoriesTests(Operation operation,User user) throws ServiceException
	{
		boolean singleOp=operation==null;
		List<Test> allCategoriesTests=null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			allCategoriesTests=getAllCategoriesTests(operation,getCurrentUser(operation),user);
		}
		finally
		{
			if (singleOp)
			{
				// End Hibernate operation
				HibernateUtil.endOperation(operation);
			}
		}
		return allCategoriesTests;
	}
	
	/**
	 * @param viewer User used to check visibility
	 * @param user User or null to get tests from all users
	 * @return List of tests from all categories filtered by user, question type and question level
	 * @throws ServiceException
	 */
	public List<Test> getAllCategoriesTests(User viewer,User user) throws ServiceException
	{
		return getAllCategoriesTests(null,viewer,user);
	}
	
	/**
	 * @param operation Operation
	 * @param viewer User used to check visibility
	 * @param user User or null to get tests from all users
	 * @return List of tests from all categories filtered by user, question type and question level
	 * @throws ServiceException
	 */
	public List<Test> getAllCategoriesTests(Operation operation,User viewer,User user) throws ServiceException
	{
		List<Test> allCategoriesTests=null;
		boolean singleOp=operation==null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			if (permissionsService.isGranted(operation,viewer,"PERMISSION_TESTS_GLOBAL_FILTER_ENABLED") &&
				permissionsService.isGranted(operation,viewer,"PERMISSION_TESTS_OTHER_USERS_FILTER_ENABLED") &&
				permissionsService.isGranted(operation,viewer,
				"PERMISSION_TESTS_VIEW_TESTS_OF_OTHER_USERS_PRIVATE_CATEGORIES_ENABLED"))
			{
				boolean includeAdminsPrivateCategories=permissionsService.isGranted(
					operation,viewer,"PERMISSION_TESTS_VIEW_TESTS_OF_ADMINS_PRIVATE_CATEGORIES_ENABLED");
				boolean includeSuperadminsPrivateCategories=permissionsService.isGranted(
					operation,viewer,"PERMISSION_TESTS_VIEW_TESTS_OF_SUPERADMINS_PRIVATE_CATEGORIES_ENABLED");
				List<Long> allCategoriesIds=categoriesService.getAllCategoriesIds(operation,viewer,
					categoryTypesService.getCategoryType(operation,"CATEGORY_TYPE_TESTS"),
					includeAdminsPrivateCategories,includeSuperadminsPrivateCategories);
				TESTS_DAO.setOperation(operation);
				allCategoriesTests=
					TESTS_DAO.getTests(user==null?0L:user.getId(),allCategoriesIds,true,true,true,true,true,true);
			}
			else
			{
				throwServiceException(
					"NON_AUTHORIZED_ACTION_ERROR","You are not authorized to execute that operation.");
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
		return allCategoriesTests;
	}
	
	/**
	 * @param user User or null to get tests from all users
	 * @return List of tests from all visible categories filtered by user
	 * @throws ServiceException
	 */
	public List<Test> getAllVisibleCategoriesTests(User user) throws ServiceException
	{
		return getAllVisibleCategoriesTests((Operation)null,user);
	}
	
	/**
	 * @param operation Operation
	 * @param user User or null to get tests from all users
	 * @return List of tests from all visible categories filtered by user
	 * @throws ServiceException
	 */
	public List<Test> getAllVisibleCategoriesTests(Operation operation,User user) throws ServiceException
	{
		boolean singleOp=operation==null;
		List<Test> allVisibleCategoriesTests=null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			allVisibleCategoriesTests=getAllVisibleCategoriesTests(operation,getCurrentUser(operation),user);
		}
		finally
		{
			if (singleOp)
			{
				// End Hibernate operation
				HibernateUtil.endOperation(operation);
			}
		}
		return allVisibleCategoriesTests;
	}
	
	/**
	 * @param viewer User used to check visibility
	 * @param user User or null to get tests from all users
	 * @return List of tests from all visible categories filtered by user
	 * @throws ServiceException
	 */
	public List<Test> getAllVisibleCategoriesTests(User viewer,User user) throws ServiceException
	{
		return getAllVisibleCategoriesTests(null,viewer,user);
	}
	
	/**
	 * @param operation Operation
	 * @param viewer User used to check visibility
	 * @param user User or null to get tests from all users
	 * @return List of tests from all visible categories filtered by user
	 * @throws ServiceException
	 */
	public List<Test> getAllVisibleCategoriesTests(Operation operation,User viewer,User user) 
		throws ServiceException
	{
		List<Test> allVisibleCategoriesTests=null;
		boolean singleOp=operation==null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			if (permissionsService.isGranted(operation,viewer,"PERMISSION_TESTS_GLOBAL_FILTER_ENABLED") &&
				permissionsService.isGranted(operation,viewer,"PERMISSION_TESTS_OTHER_USERS_FILTER_ENABLED"))
			{
				List<Long> allVisibleCategoriesIds=categoriesService.getAllVisibleCategoriesIds(
					operation,viewer,categoryTypesService.getCategoryType(operation,"CATEGORY_TYPE_TESTS"));
				TESTS_DAO.setOperation(operation);
				allVisibleCategoriesTests=TESTS_DAO.getTests(
					user==null?0L:user.getId(),allVisibleCategoriesIds,true,true,true,true,true,true);
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
		return allVisibleCategoriesTests;
	}
	
	/**
	 * @param user User or null to get tests from all users
	 * @return List of test from all categories of current user (including its global categories) 
	 * filtered by user
	 * @throws ServiceException
	 */
	public List<Test> getAllMyCategoriesTests(User user) throws ServiceException
	{
		return getAllMyCategoriesTests((Operation)null,user);
	}
	
	/**
	 * @param operation Operation
	 * @param user User or null to get tests from all users
	 * @return List of tests from all categories of current user (including its global categories) 
	 * filtered by user
	 * @throws ServiceException
	 */
	public List<Test> getAllMyCategoriesTests(Operation operation,User user) throws ServiceException
	{
		boolean singleOp=operation==null;
		List<Test> allMyCategoriesTests=null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			allMyCategoriesTests=getAllMyCategoriesTests(operation,getCurrentUser(operation),user);
		}
		finally
		{
			if (singleOp)
			{
				// End Hibernate operation
				HibernateUtil.endOperation(operation);
			}
		}
		return allMyCategoriesTests; 
	}
	
	/**
	 * @param viewer User used to check visibility
	 * @param user User or null to get tests from all users
	 * @return List of tests from all categories of current user (including its global categories) 
	 * filtered by user
	 * @throws ServiceException
	 */
	public List<Test> getAllMyCategoriesTests(User viewer,User user) throws ServiceException
	{
		return getAllMyCategoriesTests(null,viewer,user);
	}
	
	/**
	 * @param operation Operation
	 * @param viewer User used to check visibility
	 * @param user User or null to get tests from all users
	 * @return List of tests from all categories of current user (including its global categories) 
	 * filtered by user
	 * @throws ServiceException
	 */
	public List<Test> getAllMyCategoriesTests(Operation operation,User viewer,User user) throws ServiceException
	{
		List<Test> allMyCategoriesTests=null;
		boolean singleOp=operation==null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			if (permissionsService.isGranted(operation,viewer,"PERMISSION_QUESTIONS_GLOBAL_FILTER_ENABLED"))
			{
				List<Long> allMyCategoriesIds=categoriesService.getAllUserCategoriesIds(
					operation,viewer,categoryTypesService.getCategoryType(operation,"CATEGORY_TYPE_TESTS"));
				TESTS_DAO.setOperation(operation);
				allMyCategoriesTests=
					TESTS_DAO.getTests(user==null?0L:user.getId(),allMyCategoriesIds,true,true,true,true,true,true);
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
		return allMyCategoriesTests; 
	}
	
	/**
	 * @param user User or null to get tests from all users
	 * @return List of tests from all categories of current user (except global categories) filtered by user
	 * @throws ServiceException
	 */
	public List<Test> getAllMyCategoriesExceptGlobalsTests(User user) throws ServiceException
	{
		return getAllMyCategoriesExceptGlobalsTests((Operation)null,user);
	}
	
	/**
	 * @param operation Operation
	 * @param user User or null to get tests from all users
	 * @return List of tests from all categories of current user (except global categories) filtered by user
	 * @throws ServiceException
	 */
	public List<Test> getAllMyCategoriesExceptGlobalsTests(Operation operation,User user) throws ServiceException
	{
		boolean singleOp=operation==null;
		List<Test> allMyCategoriesExceptGlobalsTests=null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			allMyCategoriesExceptGlobalsTests=
				getAllMyCategoriesExceptGlobalsTests(operation,getCurrentUser(operation),user);
		}
		finally
		{
			if (singleOp)
			{
				// End Hibernate operation
				HibernateUtil.endOperation(operation);
			}
		}
		return allMyCategoriesExceptGlobalsTests; 
	}
	
	/**
	 * @param viewer User used to check visibility
	 * @param user User or null to get tests from all users
	 * @return List of tests from all categories of current user (except global categories) filtered by user
	 * @throws ServiceException
	 */
	public List<Test> getAllMyCategoriesExceptGlobalsTests(User viewer,User user) throws ServiceException
	{
		return getAllMyCategoriesExceptGlobalsTests(null,viewer,user);
	}
	
	/**
	 * @param operation Operation
	 * @param viewer User used to check visibility
	 * @param user User or null to get tests from all users
	 * @return List of tests from all categories of current user (except global categories) filtered by user
	 * @throws ServiceException
	 */
	public List<Test> getAllMyCategoriesExceptGlobalsTests(Operation operation,User viewer,User user) 
		throws ServiceException
	{
		List<Test> allMyCategoriesExceptGlobalsTests=null;
		boolean singleOp=operation==null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			List<Long> allMyCategoriesExceptGlobalsIds=categoriesService.getAllUserCategoriesIdsExceptGlobals(
				operation,viewer,categoryTypesService.getCategoryType(operation,"CATEGORY_TYPE_TESTS"));
			TESTS_DAO.setOperation(operation);
			allMyCategoriesExceptGlobalsTests=TESTS_DAO.getTests(
				user==null?0L:user.getId(),allMyCategoriesExceptGlobalsIds,true,true,true,true,true,true);
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
		return allMyCategoriesExceptGlobalsTests; 
	}
	
	/**
	 * @param user User or null to get tests from all users
	 * @return List of tests from all global categories filtered by user
	 * @throws ServiceException
	 */
	public List<Test> getAllGlobalCategoriesTests(User user) throws ServiceException
	{
		return getAllGlobalCategoriesTests((Operation)null,user);
	}
	
	/**
	 * @param operation Operation
	 * @param user User or null to get tests from all users
	 * @return List of tests from all global categories filtered by user
	 * @throws ServiceException
	 */
	public List<Test> getAllGlobalCategoriesTests(Operation operation,User user) throws ServiceException
	{
		boolean singleOp=operation==null;
		List<Test> allGlobalCategoriesTests=null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			allGlobalCategoriesTests=getAllGlobalCategoriesTests(operation,getCurrentUser(operation),user);
		}
		finally
		{
			if (singleOp)
			{
				// End Hibernate operation
				HibernateUtil.endOperation(operation);
			}
		}
		return allGlobalCategoriesTests; 
	}
	
	/**
	 * @param viewer User used to check visibility
	 * @param user User or null to get tests from all users
	 * @return List of tests from all global categories filtered by user
	 * @throws ServiceException
	 */
	public List<Test> getAllGlobalCategoriesTests(User viewer,User user) throws ServiceException
	{
		return getAllGlobalCategoriesTests(null,viewer,user);
	}
	
	/**
	 * @param operation Operation
	 * @param viewer User used to check visibility
	 * @param user User or null to get tests from all users
	 * @return List of tests from all global categories filtered by user
	 * @throws ServiceException
	 */
	public List<Test> getAllGlobalCategoriesTests(Operation operation,User viewer,User user) throws ServiceException
	{
		List<Test> allGlobalCategoriesTests=null;
		boolean singleOp=operation==null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			if (permissionsService.isGranted(operation,viewer,"PERMISSION_TESTS_GLOBAL_FILTER_ENABLED"))
			{
				List<Long> allGlobalCategoriesIds=categoriesService.getAllGlobalCategoriesIds(
					operation,categoryTypesService.getCategoryType(operation,"CATEGORY_TYPE_TESTS"));
				TESTS_DAO.setOperation(operation);
				allGlobalCategoriesTests=TESTS_DAO.getTests(
					user==null?0L:user.getId(),allGlobalCategoriesIds,true,true,true,true,true,true);
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
		return allGlobalCategoriesTests; 
	}
	
	/**
	 * @param user User or null to get tests from all users
	 * @return List of tests from all public categories of all users except the current one filtered by user
	 * @throws ServiceException
	 */
	public List<Test> getAllPublicCategoriesOfOtherUsersTests(User user) throws ServiceException
	{
		return getAllPublicCategoriesOfOtherUsersTests((Operation)null,user);
	}
	
	/**
	 * @param operation Operation
	 * @param user User or null to get tests from all users
	 * @return List of tests from all public categories of all users except the current one filtered by user
	 * @throws ServiceException
	 */
	public List<Test> getAllPublicCategoriesOfOtherUsersTests(Operation operation,User user) throws ServiceException
	{
		boolean singleOp=operation==null;
		List<Test> allPublicCategoriesOfOtherUsersTests=null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			allPublicCategoriesOfOtherUsersTests=
				getAllPublicCategoriesOfOtherUsersTests(operation,getCurrentUser(operation),user);
		}
		finally
		{
			if (singleOp)
			{
				// End Hibernate operation
				HibernateUtil.endOperation(operation);
			}
		}
		return allPublicCategoriesOfOtherUsersTests; 
	}
	
	/**
	 * @param viewer User used to check visibility
	 * @param user User or null to get tests from all users
	 * @return List of tests from all public categories of all users except the current one filtered by user
	 * @throws ServiceException
	 */
	public List<Test> getAllPublicCategoriesOfOtherUsersTests(User viewer,User user) throws ServiceException
	{
		return getAllPublicCategoriesOfOtherUsersTests(null,viewer,user);
	}
	
	/**
	 * @param operation Operation
	 * @param viewer User used to check visibility
	 * @param user User or null to get tests from all users
	 * @return List of tests from all public categories of all users except the current one filtered by user
	 * @throws ServiceException
	 */
	public List<Test> getAllPublicCategoriesOfOtherUsersTests(Operation operation,User viewer,User user) 
		throws ServiceException
	{
		List<Test> allPublicCategoriesOfOtherUsersTests=null;
		boolean singleOp=operation==null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			if (permissionsService.isGranted(operation,viewer,"PERMISSION_TESTS_OTHER_USERS_FILTER_ENABLED"))
			{
				List<Long> allPublicCategoriesOfOtherUsersIds=categoriesService.getAllPublicCategoriesOfOtherUsersIds(
					operation,viewer,categoryTypesService.getCategoryType(operation,"CATEGORY_TYPE_TESTS"));
				TESTS_DAO.setOperation(operation);
				allPublicCategoriesOfOtherUsersTests=TESTS_DAO.getTests(
					user==null?0L:user.getId(),allPublicCategoriesOfOtherUsersIds,true,true,true,true,true,true);
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
		return allPublicCategoriesOfOtherUsersTests; 
	}
	
	/**
	 * @param user User or null to get tests from all users
	 * @return List of tests from all private categories of all users except the current one filtered by user
	 * @throws ServiceException
	 */
	public List<Test> getAllPrivateCategoriesOfOtherUsersTests(User user) throws ServiceException
	{
		return getAllPrivateCategoriesOfOtherUsersTests((Operation)null,user);
	}
	
	/**
	 * @param operation Operation
	 * @param user User or null to get tests from all users
	 * @return List of tests from all private categories of all users except the current one filtered by user
	 * @throws ServiceException
	 */
	public List<Test> getAllPrivateCategoriesOfOtherUsersTests(Operation operation,User user) throws ServiceException
	{
		boolean singleOp=operation==null;
		List<Test> allPrivateCategoriesOfOtherUsersTests=null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			allPrivateCategoriesOfOtherUsersTests=
				getAllPrivateCategoriesOfOtherUsersTests(operation,getCurrentUser(operation),user);
		}
		finally
		{
			if (singleOp)
			{
				// End Hibernate operation
				HibernateUtil.endOperation(operation);
			}
		}
		return allPrivateCategoriesOfOtherUsersTests; 
	}
	
	/**
	 * @param viewer User used to check visibility
	 * @param user User or null to get tests from all users
	 * @return List of tests from all private categories of all users except the current one filtered by user
	 * @throws ServiceException
	 */
	public List<Test> getAllPrivateCategoriesOfOtherUsersTests(User viewer,User user) throws ServiceException
	{
		return getAllPrivateCategoriesOfOtherUsersTests(null,viewer,user);
	}
	
	/**
	 * @param operation Operation
	 * @param viewer User used to check visibility
	 * @param user User or null to get tests from all users
	 * @return List of tests from all private categories of all users except the current one filtered by user
	 * @throws ServiceException
	 */
	public List<Test> getAllPrivateCategoriesOfOtherUsersTests(Operation operation,User viewer,User user) 
		throws ServiceException
	{
		List<Test> allPrivateCategoriesOfOtherUsersTests=null;
		boolean singleOp=operation==null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			if (permissionsService.isGranted(operation,viewer,"PERMISSION_TESTS_OTHER_USERS_FILTER_ENABLED") && 
				permissionsService.isGranted(operation,viewer,
				"PERMISSION_TESTS_VIEW_TESTS_OF_OTHER_USERS_PRIVATE_CATEGORIES_ENABLED"))
			{
				boolean includeAdminsPrivateCategories=permissionsService.isGranted(
					operation,viewer,"PERMISSION_TESTS_VIEW_TESTS_OF_ADMINS_PRIVATE_CATEGORIES_ENABLED");
				boolean includeSuperadminsPrivateCategories=permissionsService.isGranted(
					operation,viewer,"PERMISSION_TESTS_VIEW_TESTS_OF_SUPERADMINS_PRIVATE_CATEGORIES_ENABLED");
				List<Long> allPrivateCategoriesOfOtherUsersIds=
					categoriesService.getAllPrivateCategoriesOfOtherUsersIds(operation,viewer,
					categoryTypesService.getCategoryType(operation,"CATEGORY_TYPE_TESTS"),
					includeAdminsPrivateCategories,includeSuperadminsPrivateCategories);
				TESTS_DAO.setOperation(operation);
				allPrivateCategoriesOfOtherUsersTests=TESTS_DAO.getTests(
					user==null?0L:user.getId(),allPrivateCategoriesOfOtherUsersIds,true,true,true,true,true,true);
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
		return allPrivateCategoriesOfOtherUsersTests; 
	}
	
	/**
	 * @param user User or null to get tests from all users
	 * @return List of tests from all categories of all users except the current one filtered by user
	 * @throws ServiceException
	 */
	public List<Test> getAllCategoriesOfOtherUsersTests(User user) throws ServiceException
	{
		return getAllCategoriesOfOtherUsersTests((Operation)null,user);
	}
	
	/**
	 * @param operation Operation
	 * @param user User or null to get tests from all users
	 * @return List of tests from all categories of all users except the current one filtered by user
	 * @throws ServiceException
	 */
	public List<Test> getAllCategoriesOfOtherUsersTests(Operation operation,User user) throws ServiceException
	{
		boolean singleOp=operation==null;
		List<Test> allCategoriesOfOtherUsersTests=null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			allCategoriesOfOtherUsersTests=getAllCategoriesOfOtherUsersTests(operation,getCurrentUser(operation),user);
		}
		finally
		{
			if (singleOp)
			{
				// End Hibernate operation
				HibernateUtil.endOperation(operation);
			}
		}
		return allCategoriesOfOtherUsersTests; 
	}
	
	/**
	 * @param viewer User used to check visibility
	 * @param user User or null to get tests from all users
	 * @return List of tests from all categories of all users except the current one filtered by user
	 * @throws ServiceException
	 */
	public List<Test> getAllCategoriesOfOtherUsersTests(User viewer,User user) throws ServiceException
	{
		return getAllCategoriesOfOtherUsersTests(null,viewer,user);
	}
	
	/**
	 * @param operation Operation
	 * @param viewer User used to check visibility
	 * @param user User or null to get tests from all users
	 * @return List of tests from all categories of all users except the current one filtered by user
	 * @throws ServiceException
	 */
	public List<Test> getAllCategoriesOfOtherUsersTests(Operation operation,User viewer,User user) 
		throws ServiceException
	{
		List<Test> allCategoriesOfOtherUsersTests=null;
		boolean singleOp=operation==null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			if (permissionsService.isGranted(operation,viewer,"PERMISSION_TESTS_OTHER_USERS_FILTER_ENABLED") && 
				permissionsService.isGranted(operation,viewer,
				"PERMISSION_TESTS_VIEW_TESTS_OF_OTHER_USERS_PRIVATE_CATEGORIES_ENABLED"))
			{
				boolean includeAdminsPrivateCategories=permissionsService.isGranted(
					operation,viewer,"PERMISSION_TESTS_VIEW_TESTS_OF_ADMINS_PRIVATE_CATEGORIES_ENABLED");
				boolean includeSuperadminsPrivateCategories=permissionsService.isGranted(
					operation,viewer,"PERMISSION_TESTS_VIEW_TESTS_OF_SUPERADMINS_PRIVATE_CATEGORIES_ENABLED");
				List<Long> allCategoriesOfOtherUsersIds=categoriesService.getAllCategoriesOfOtherUsersIds(
					operation,viewer,categoryTypesService.getCategoryType(operation,"CATEGORY_TYPE_TESTS"),
					includeAdminsPrivateCategories,includeSuperadminsPrivateCategories);
				TESTS_DAO.setOperation(operation);
				allCategoriesOfOtherUsersTests=TESTS_DAO.getTests(
					user==null?0L:user.getId(),allCategoriesOfOtherUsersIds,true,true,true,true,true,true);
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
		return allCategoriesOfOtherUsersTests; 
	}
	
	/**
	 * @param user User or null to get total number of tests
	 * @return Number of tests filtered by user (or total)
	 * @throws ServiceException
	 */
	public int getTestsCount(User user) throws ServiceException
	{
		return getTestsCount(null,user);
	}
	
	/**
	 * @param operation Operation
	 * @param user User or null to get total number of tests
	 * @return Number of tests filtered by user (or total)
	 * @throws ServiceException
	 */
	public int getTestsCount(Operation operation,User user) throws ServiceException
	{
		int testsCount=0;
		boolean singleOp=operation==null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			TESTS_DAO.setOperation(operation);
			testsCount=TESTS_DAO.getTests(user==null?0L:user.getId(),0L,false,false,false,false,false,false).size();
		}
		finally
		{
			if (singleOp)
			{
				// End Hibernate operation
				HibernateUtil.endOperation(operation);
			}
		}
		return testsCount;
	}
	
	/**
	 * @param testName Test name to check
	 * @param categoryId Category identifier
	 * @param testId Test identifier (of the test to exclude from checking)
	 * @return true if the test name checked is available for the indicated category, false otherwise
	 * @throws ServiceException
	 */
	public boolean isTestNameAvailable(String testName,long categoryId,long testId) throws ServiceException
	{
		return isTestNameAvailable(null,testName,categoryId,testId);
	}
	
	/**
	 * @param operation Operation
	 * @param testName Test name to check
	 * @param categoryId Category identifier
	 * @param testId Test identifier (of the test to exclude from checking)
	 * @return true if the test name checked is available for the indicated category, false otherwise
	 * @throws ServiceException
	 */
	public boolean isTestNameAvailable(Operation operation,String testName,long categoryId,long testId) 
		throws ServiceException
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
				
				TESTS_DAO.setOperation(operation);
				for (Test test:TESTS_DAO.getTests(0L,categoryId,false,false,false,false,false,false))
				{
					if (testId!=test.getId() && testName.equals(test.getName()))
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
	 * @return Number of tests from all user global categories
	 * @throws ServiceException
	 */
	public int getAllUserGlobalCategoriesTestsCount(User user) throws ServiceException
	{
		return getAllUserGlobalCategoriesTestsCount(null,user);
	}
	
	/**
	 * @param operation Operation
	 * @param user User
	 * @return Number of tests from all user global categories
	 * @throws ServiceException
	 */
	public int getAllUserGlobalCategoriesTestsCount(Operation operation,User user) throws ServiceException
	{
		int allUserGlobalCategoriesTestsCount=0;
		boolean singleOp=operation==null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			List<Long> allUserGlobalCategoriesIds=categoriesService.getAllUserGlobalCategoriesIds(
				operation,user,categoryTypesService.getCategoryType(operation,"CATEGORY_TYPE_TESTS"));
			TESTS_DAO.setOperation(operation);
			List<Test> allUserGlobalCategoriesTests=
				TESTS_DAO.getTests(0L,allUserGlobalCategoriesIds,true,true,true,true,true,true);
			allUserGlobalCategoriesTestsCount=allUserGlobalCategoriesTests.size();
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
		return allUserGlobalCategoriesTestsCount; 
	}
	
	//Obtiene una prueba a partir de su id
	/**
	 * @param id Test identifier
	 * @return Test
	 * @throws ServiceException
	 */
	public Test getTest(long id) throws ServiceException
	{
		return getTest(null,id);
	}
	
	/**
	 * @param operation Operation
	 * @param id Test identifier
	 * @return Test
	 * @throws ServiceException
	 */
	public Test getTest(Operation operation,long id) throws ServiceException
	{
		Test test=null;
		boolean singleOp=operation==null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			TESTS_DAO.setOperation(operation);
			test=TESTS_DAO.getTest(id,true,true,true,true,true,true);
			
			if (test!=null)
			{
				test.setTestUsers(testUsersService.getSortedTestUsers(operation,id));
				test.setSections(sectionsService.getSections(operation,id));
				test.setSupportContacts(supportContactsService.getSupportContacts(operation,id));
				test.setEvaluators(evaluatorsService.getEvaluators(operation,id));
				test.setTestFeedbacks(testFeedbacksService.getTestFeedbacks(operation,id));
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
		return test;
	}
	
	//Modifica una prueba
	/**
	 * Updates a test.
	 * @param test Test to update
	 * @throws ServiceException
	 */
	public void updateTest(Test test) throws ServiceException
	{
		updateTest(null,test);
	}
	
	/**
	 * Updates a test.
	 * @param operation Operation
	 * @param test Test to update
	 * @throws ServiceException
	 */
	public void updateTest(Operation operation,Test test) throws ServiceException
	{
		boolean singleOp=operation==null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			// Get test sections
			List<Section> sectionsFromDB=sectionsService.getSections(operation,test);
			
			// We delete removed sections from DB 
			for (Section section:sectionsFromDB)
			{
				if (!test.getSections().contains(section))
				{
					sectionsService.deleteSection(operation,section);
				}
			}
			
			// We add/update sections on DB
			for (Section section:test.getSections())
			{
				if (sectionsFromDB.contains(section))
				{
					Section sectionFromDB=sectionsFromDB.get(sectionsFromDB.indexOf(section));
					sectionFromDB.setFromOtherSection(section);
					
					// We delete removed question references from section to update 
					for (QuestionOrder questionOrder:sectionFromDB.getQuestionOrders())
					{
						if (!section.getQuestionOrders().contains(questionOrder))
						{
							sectionFromDB.getQuestionOrders().remove(questionOrder);
						}
					}
					
					// We add/update question references on section to update
					for (QuestionOrder questionOrder:section.getQuestionOrders())
					{
						if (sectionFromDB.getQuestionOrders().contains(questionOrder))
						{
							QuestionOrder questionOrderFromDB=sectionFromDB.getQuestionOrders().get(
								sectionFromDB.getQuestionOrders().indexOf(questionOrder));
							questionOrderFromDB.setFromOtherQuestionOrder(questionOrder);
						}
						else
						{
							sectionFromDB.getQuestionOrders().add(questionOrder);
						}
					}
					
					sectionsService.updateSection(operation,sectionFromDB);
				}
				else
				{
					sectionsService.addSection(operation,section);
				}
			}
			
			// We get support contacts
			List<SupportContact> supportContactsFromDB=
				supportContactsService.getSupportContacts(operation,test);
			
			// We delete removed support contacts from DB
			for (SupportContact supportContact:supportContactsFromDB)
			{
				if (!test.getSupportContacts().contains(supportContact))
				{
					supportContactsService.deleteSupportContact(operation,supportContact);
				}
			}
			
			// We add/update support contacts on DB
			for (SupportContact supportContact:test.getSupportContacts())
			{
				if (supportContactsFromDB.contains(supportContact))
				{
					SupportContact supportContactFromDB=
						supportContactsFromDB.get(supportContactsFromDB.indexOf(supportContact));
					supportContactFromDB.setFromOtherSupportContact(supportContact);
					supportContactsService.updateSupportContact(operation,supportContactFromDB);
				}
				else
				{
					supportContactsService.addSupportContact(operation,supportContact);
				}
			}
			
			// We get evaluators
			List<Evaluator> evaluatorsFromDB=evaluatorsService.getEvaluators(operation,test);
			
			// We delete removed evaluators from DB
			for (Evaluator evaluator:evaluatorsFromDB)
			{
				if (!test.getEvaluators().contains(evaluator))
				{
					evaluatorsService.deleteEvaluator(operation,evaluator);
				}
			}
			
			// We add/update evaluators on DB
			for (Evaluator evaluator:test.getEvaluators())
			{
				if (evaluatorsFromDB.contains(evaluator))
				{
					Evaluator evaluatorFromDB=evaluatorsFromDB.get(evaluatorsFromDB.indexOf(evaluator));
					evaluatorFromDB.setFromOtherEvaluator(evaluator);
					evaluatorsService.updateEvaluator(operation,evaluatorFromDB);
				}
				else
				{
					evaluatorsService.addEvaluator(operation,evaluator);
				}
			}
			
			// We add added evaluators on DB
			for (Evaluator evaluator:test.getEvaluators())
			{
				if (!evaluatorsFromDB.contains(evaluator))
				{
					evaluatorsService.addEvaluator(operation,evaluator);
				}
			}
			
			// We get users of a test
			List<TestUser> testUsersFromDB=testUsersService.getTestUsers(operation,test);
			
			// We delete removed users of a test from DB
			for (TestUser testUser:testUsersFromDB)
			{
				if (!test.getTestUsers().contains(testUser))
				{
					testUsersService.deleteTestUser(operation,testUser);
				}
			}
			
			// We add/update users of a test on DB
			for (TestUser testUser:test.getTestUsers())
			{
				if (testUsersFromDB.contains(testUser))
				{
					TestUser testUserFromDB=testUsersFromDB.get(testUsersFromDB.indexOf(testUser));
					testUserFromDB.setFromOtherTestUser(testUser);
					testUsersService.updateTestUser(operation,testUserFromDB);
				}
				else
				{
					testUsersService.addTestUser(operation,testUser);
				}
			}
			
			// We get feedbacks of a test
			List<TestFeedback> testFeedbacksFromDB=testFeedbacksService.getTestFeedbacks(operation,test);
			
			// We delete removed feedbacks of a test from DB
			for (TestFeedback testFeedback:testFeedbacksFromDB)
			{
				if (!test.getTestFeedbacks().contains(testFeedback))
				{
					testFeedbacksService.deleteTestFeedback(operation,testFeedback);
				}
			}
			
			// We add/update feedbacks of a test on DB
			for (TestFeedback testFeedback:test.getTestFeedbacks())
			{
				if (testFeedbacksFromDB.contains(testFeedback))
				{
					TestFeedback testFeedbackFromDB=
						testFeedbacksFromDB.get(testFeedbacksFromDB.indexOf(testFeedback));
					testFeedbackFromDB.setFromOtherTestFeedback(testFeedback);
					testFeedbacksService.updateTestFeedback(operation,testFeedbackFromDB);
				}
				else
				{
					testFeedbacksService.addTestFeedback(operation,testFeedback);
				}
			}
			
			// We get test from DB
			TESTS_DAO.setOperation(operation);
			Test testFromDB=TESTS_DAO.getTest(test.getId(),false,false,false,false,false,false);
			
			// We update test on DB
			testFromDB.setFromOtherTest(test);
			TESTS_DAO.setOperation(operation);
			TESTS_DAO.updateTest(testFromDB);
			
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
	
	//Añade una nueva prueba
	/**
	 * Adds a new test.
	 * @param test Test to add
	 * @throws ServiceException
	 */
	public void addTest(Test test) throws ServiceException
	{
		addTest(null,test);
	}
	
	/**
	 * Adds a new test.
	 * @param operation Operation
	 * @param test Test to add
	 * @throws ServiceException
	 */
	public void addTest(Operation operation,Test test) throws ServiceException
	{
		try
		{
			TESTS_DAO.setOperation(operation);
			TESTS_DAO.saveTest(test);
		}
		catch (DaoException de)
		{
			throw new ServiceException(de.getMessage(),de);
		}
	}
	
	/**
	 * Deletes a test.
	 * @param test Test to delete
	 * @throws ServiceException
	 */
	public void deleteTest(Test test) throws ServiceException
	{
		deleteTest(null,test);
	}
	
	/**
	 * Deletes a test.
	 * @param operation Operation
	 * @param test Test to delete
	 * @throws ServiceException
	 */
	public void deleteTest(Operation operation,Test test) throws ServiceException
	{
		try
		{
			TESTS_DAO.setOperation(operation);
			TESTS_DAO.deleteTest(test);
		}
		catch (DaoException de)
		{
			throw new ServiceException(de.getMessage(),de);
		}
	}
	
	/**
	 * Deletes a test.
	 * @param testId Test's identifier
	 * @throws ServiceException
	 */
	public void deleteTest(long testId) throws ServiceException
	{
		deleteTest(null,testId);
	}
	
	/**
	 * Deletes a test.
	 * @param operation Operation
	 * @param testId Test's identifier
	 * @throws ServiceException
	 */
	public void deleteTest(Operation operation,long testId) throws ServiceException
	{
		boolean singleOp=operation==null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			TESTS_DAO.setOperation(operation);
			Test test=TESTS_DAO.getTest(testId,false,false,false,false,false,false);
			
			// Delete test
			deleteTest(operation,test);
			
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
	 * @param operation Operation
	 * @return Current user
	 */
	private User getCurrentUser(Operation operation)
	{
		FacesContext context=FacesContext.getCurrentInstance();
		UserSessionService userSessionService=(UserSessionService)context.getApplication().getELResolver().getValue(
			context.getELContext(),null,"userSessionService");
		return userSessionService.getCurrentUser(operation);
	}
}
