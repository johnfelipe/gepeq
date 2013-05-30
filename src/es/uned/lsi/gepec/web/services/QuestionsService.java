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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.context.FacesContext;

import org.hibernate.exception.ConstraintViolationException;

import es.uned.lsi.gepec.model.AnswerComparator;
import es.uned.lsi.gepec.model.QuestionLevel;
import es.uned.lsi.gepec.model.dao.DaoException;
import es.uned.lsi.gepec.model.dao.QuestionsDao;
import es.uned.lsi.gepec.model.entities.Answer;
import es.uned.lsi.gepec.model.entities.Category;
import es.uned.lsi.gepec.model.entities.Feedback;
import es.uned.lsi.gepec.model.entities.Question;
import es.uned.lsi.gepec.model.entities.QuestionResource;
import es.uned.lsi.gepec.model.entities.User;
import es.uned.lsi.gepec.util.HibernateUtil;
import es.uned.lsi.gepec.util.HibernateUtil.Operation;

//Ofrece a la vista operaciones con preguntas
//author Víctor Manuel Alonso Rodríguez
//since  12/2011
/**
 * Manages questions.
 */
@SuppressWarnings("serial")
@ManagedBean(eager=true)
@ApplicationScoped
public class QuestionsService implements Serializable
{
	private final static QuestionsDao QUESTIONS_DAO=new QuestionsDao();
	
	@ManagedProperty(value="#{categoriesService}")
	private CategoriesService categoriesService;
	@ManagedProperty(value="#{categoryTypesService}")
	private CategoryTypesService categoryTypesService;
	@ManagedProperty(value="#{visibilitiesService}")
	private VisibilitiesService visibilitiesService;
	@ManagedProperty(value="#{answersService}")
	private AnswersService answersService;
	@ManagedProperty(value="#{questionResourcesService}")
	private QuestionResourcesService questionResourcesService;
	@ManagedProperty(value="#{feedbacksService}")
	private FeedbacksService feedbacksService;
	@ManagedProperty(value="#{localizationService}")
	private LocalizationService localizationService;
	@ManagedProperty(value="#{permissionsService}")
	private PermissionsService permissionsService;
	
	public QuestionsService()
	{
	}
	
	public void setCategoriesService(CategoriesService categoriesService)
	{
		this.categoriesService=categoriesService;
	}
	
	public void setcategoryTypesService(CategoryTypesService categoryTypesService)
	{
		this.categoryTypesService=categoryTypesService;
	}
	
	public void setVisibilitiesService(VisibilitiesService visibilitiesService)
	{
		this.visibilitiesService=visibilitiesService;
	}
	
	public void setAnswersService(AnswersService answersService)
	{
		this.answersService=answersService;
	}
	
	public void setQuestionResourcesService(QuestionResourcesService questionResourcesService)
	{
		this.questionResourcesService=questionResourcesService;
	}
	
	public void setFeedbacksService(FeedbacksService feedbacksService)
	{
		this.feedbacksService=feedbacksService;
	}
	
	public void setLocalizationService(LocalizationService localizationService)
	{
		this.localizationService=localizationService;
	}
	
	public void setPermissionsService(PermissionsService permissionsService)
	{
		this.permissionsService=permissionsService;
	}
	
	//Obtiene todas las preguntas de un usuario
	/**
	 * @param user User or null to get questions from all users
	 * @return List of questions filtered by user
	 * @throws ServiceException
	 */
	public List<Question> getQuestions(User user) throws ServiceException
	{
		return getQuestions((Operation)null,user);
	}
	
	/**
	 * @param operation Operation
	 * @param user User or null to get questions from all users
	 * @return List of questions filtered by user
	 * @throws ServiceException
	 */
	public List<Question> getQuestions(Operation operation,User user) throws ServiceException
	{
		boolean singleOp=operation==null;
		List<Question> questions=null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			questions=getQuestions(operation,getCurrentUser(operation),user,0L,true,"","");
		}
		finally
		{
			if (singleOp)
			{
				// End Hibernate operation
				HibernateUtil.endOperation(operation);
			}
		}
		return questions;
	}
	
	//Obtiene todas las preguntas de un usuario y de una determinada categoría
	/**
	 * @param user User or null to get questions from all users
	 * @param categoryId Filtering category identifier or 0 to get questions from all categories
	 * @param includeSubcategories Include questions from categories derived from filtering category
	 * @return List of questions filtered by user and category (and optionally subcategories)
	 * @throws ServiceException
	 */
	public List<Question> getQuestions(User user,long categoryId,boolean includeSubcategories) throws ServiceException
	{
		return getQuestions((Operation)null,user,categoryId,includeSubcategories);
	}
	
	/**
	 * @param operation Operation
	 * @param user User or null to get questions from all users
	 * @param categoryId Filtering category identifier or 0 to get questions from all categories
	 * @param includeSubcategories Include questions from categories derived from filtering category
	 * @return List of questions filtered by user and category (and optionally subcategories)
	 * @throws ServiceException
	 */
	public List<Question> getQuestions(Operation operation,User user,long categoryId,boolean includeSubcategories) 
		throws ServiceException
	{
		boolean singleOp=operation==null;
		List<Question> questions=null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			questions=
				getQuestions(operation,getCurrentUser(operation),user,categoryId,includeSubcategories,"","");
		}
		finally
		{
			if (singleOp)
			{
				// End Hibernate operation
				HibernateUtil.endOperation(operation);
			}
		}
		return questions;
	}
	
	//Obtiene todas las preguntas de un usuario,
	//de una categoría, un tipo de pregunta y un nivel de dificultad
	/**
	 * @param user User or null to get questions from all users
	 * @param categoryId Filtering category identifier or 0 to get questions from all categories
	 * @param includeSubcategories Include questions from categories derived from filtering category
	 * @param questionType Filtering question type or empty string to get questions of all types
	 * @param questionLevel Filtering question level or empty string to get questions of all levels
	 * @return List of questions filtered by user, category (and optionally subcategories), question type 
	 * and question level
	 * @throws ServiceException
	 */
	public List<Question> getQuestions(User user,long categoryId,boolean includeSubcategories,String questionType,
		String questionLevel) throws ServiceException
	{
		return getQuestions((Operation)null,user,categoryId,includeSubcategories,questionType,questionLevel);
	}
	
	/**
	 * @param operation Operation
	 * @param user User or null to get questions from all users
	 * @param categoryId Filtering category identifier or 0 to get questions from all categories
	 * @param includeSubcategories Include questions from categories derived from filtering category
	 * @param questionType Filtering question type or empty string to get questions of all types
	 * @param questionLevel Filtering question level or empty string to get questions of all levels
	 * @return List of questions filtered by user, category (and optionally subcategories), question type 
	 * and question level
	 * @throws ServiceException
	 */
	public List<Question> getQuestions(Operation operation,User user,long categoryId,
		boolean includeSubcategories,String questionType,String questionLevel) throws ServiceException
	{
		boolean singleOp=operation==null;
		List<Question> questions=null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			questions=getQuestions(operation,getCurrentUser(operation),user,categoryId,includeSubcategories,
				questionType,questionLevel);
		}
		finally
		{
			if (singleOp)
			{
				// End Hibernate operation
				HibernateUtil.endOperation(operation);
			}
		}
		return questions;
	}
	
	/**
	 * @param viewer User used to check visibility
	 * @param user User or null to get questions from all users
	 * @return List of questions filtered by user
	 * @throws ServiceException
	 */
	public List<Question> getQuestions(User viewer,User user) throws ServiceException
	{
		return getQuestions(null,viewer,user,0L,true,"","");
	}
	
	/**
	 * @param operation Operation
	 * @param viewer User used to check visibility
	 * @param user User or null to get questions from all users
	 * @return List of questions filtered by user
	 * @throws ServiceException
	 */
	public List<Question> getQuestions(Operation operation,User viewer,User user) throws ServiceException
	{
		return getQuestions(operation,viewer,user,0L,true,"","");
	}
	
	/**
	 * @param viewer User used to check visibility
	 * @param user User or null to get questions from all users
	 * @param categoryId Filtering category identifier or 0 to get questions from all categories
	 * @param includeSubcategories Include questions from categories derived from filtering category
	 * @return List of questions filtered by user and category (and optionally subcategories)
	 * @throws ServiceException
	 */
	public List<Question> getQuestions(User viewer,User user,long categoryId,boolean includeSubcategories) 
		throws ServiceException
	{
		return getQuestions(null,viewer,user,categoryId,includeSubcategories,"","");
	}
	
	/**
	 * @param operation Operation
	 * @param viewer User used to check visibility
	 * @param user User or null to get questions from all users
	 * @param categoryId Filtering category identifier or 0 to get questions from all categories
	 * @param includeSubcategories Include questions from categories derived from filtering category
	 * @return List of questions filtered by user and category (and optionally subcategories)
	 * @throws ServiceException
	 */
	public List<Question> getQuestions(Operation operation,User viewer,User user,long categoryId,
		boolean includeSubcategories) throws ServiceException
	{
		return getQuestions(operation,viewer,user,categoryId,includeSubcategories,"","");
	}
	
	/**
	 * @param viewer User used to check visibility
	 * @param user User or null to get questions from all users
	 * @param categoryId Filtering category identifier or 0 to get questions from all categories
	 * @param includeSubcategories Include questions from categories derived from filtering category
	 * @param questionType Filtering question type or empty string to get questions of all types
	 * @param questionLevel Filtering question level or empty string to get questions of all levels
	 * @return List of questions filtered by user, category (and optionally subcategories), question type 
	 * and question level
	 * @throws ServiceException
	 */
	public List<Question> getQuestions(User viewer,User user,long categoryId,boolean includeSubcategories,
		String questionType,String questionLevel) throws ServiceException
	{
		return getQuestions(null,viewer,user,categoryId,includeSubcategories,questionType,questionLevel);
	}
	
	/**
	 * @param operation Operation
	 * @param viewer User used to check visibility
	 * @param user User or null to get questions from all users
	 * @param categoryId Filtering category identifier or 0 to get questions from all categories
	 * @param includeSubcategories Include questions from categories derived from filtering category
	 * @param questionType Filtering question type or empty string to get questions of all types
	 * @param questionLevel Filtering question level or empty string to get questions of all levels
	 * @return List of questions filtered by user, category (and optionally subcategories), question type 
	 * and question level
	 * @throws ServiceException
	 */
	public List<Question> getQuestions(Operation operation,User viewer,User user,long categoryId,
		boolean includeSubcategories,String questionType,String questionLevel) throws ServiceException
	{
		boolean singleOp=operation==null;
		List<Question> questions=null;
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
				checkVisibility=permissionsService.isDenied(operation,viewer,
					"PERMISSION_QUESTIONS_VIEW_QUESTIONS_OF_OTHER_USERS_PRIVATE_CATEGORIES_ENABLED") ||
					(permissionsService.isGranted(
					operation,category.getUser(),"PERMISSION_NAVIGATION_ADMINISTRATION") &&
					permissionsService.isDenied(operation,viewer,
					"PERMISSION_QUESTIONS_VIEW_QUESTIONS_OF_ADMINS_PRIVATE_CATEGORIES_ENABLED")) ||
					(permissionsService.isGranted(operation,category.getUser(),
					"PERMISSION_ADMINISTRATION_RAISE_PERMISSIONS_OVER_OWNED_ALLOWED") &&
					permissionsService.isDenied(operation,viewer,
					"PERMISSION_QUESTIONS_VIEW_QUESTIONS_OF_SUPERADMINS_PRIVATE_CATEGORIES_ENABLED"));
				if (checkVisibility && !category.getUser().equals(viewer)&& 
					!category.getVisibility().isGlobal() && category.getVisibility().getLevel()>=
					visibilitiesService.getVisibility(operation,"CATEGORY_VISIBILITY_PRIVATE").getLevel())
				{
					throwServiceException(
						"NON_AUTHORIZED_ACTION_ERROR","You are not authorized to execute that operation.");
				}
			}
			
			if (!includeSubcategories || categoryId<=0L)
			{
				QUESTIONS_DAO.setOperation(operation);
				questions=QUESTIONS_DAO.getQuestions(
					user==null?0L:user.getId(),categoryId,questionType,questionLevel,true,true);
				if (categoryId<=0L)
				{
					Map<Category,Boolean> checkedCategoriesVisibility=new HashMap<Category,Boolean>();
					List<Question> questionsToRemove=new ArrayList<Question>();
					for (Question question:questions)
					{
						Category questionCategory=question.getCategory();
						boolean checkedCategoryVisibility=false;						
						if (checkedCategoriesVisibility.containsKey(questionCategory))
						{
							checkedCategoryVisibility=
								checkedCategoriesVisibility.get(questionCategory).booleanValue();
						}
						else
						{
							checkedCategoryVisibility=questionCategory.getUser().equals(viewer) ||
								questionCategory.getVisibility().isGlobal() || 
								questionCategory.getVisibility().getLevel()<=
								visibilitiesService.getVisibility(
								operation,"CATEGORY_VISIBILITY_PUBLIC").getLevel();
							checkedCategoriesVisibility.put(
								questionCategory,Boolean.valueOf(checkedCategoryVisibility));
						}
						if (!checkedCategoryVisibility)
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
			else
			{
				QUESTIONS_DAO.setOperation(operation);
				questions=QUESTIONS_DAO.getQuestions(user==null?0L:user.getId(),
					categoriesService.getDerivedCategoriesIds(operation,categoryId,null,
					categoryTypesService.getCategoryType(operation,"CATEGORY_TYPE_QUESTIONS"),checkVisibility),
					questionType,questionLevel,true,true);
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
		return questions;
	}
	
	/**
	 * @param user User or null to get questions from all users
	 * @param questionType Filtering question type or empty string to get questions of all types
	 * @param questionLevel Filtering question level or empty string to get questions of all levels
	 * @return List of questions from all categories filtered by user, question type and question level
	 * @throws ServiceException
	 */
	public List<Question> getAllCategoriesQuestions(User user,String questionType,String questionLevel)
	{
		return getAllCategoriesQuestions((Operation)null,user,questionType,questionLevel);
	}
	
	/**
	 * @param operation Operation
	 * @param user User or null to get questions from all users
	 * @param questionType Filtering question type or empty string to get questions of all types
	 * @param questionLevel Filtering question level or empty string to get questions of all levels
	 * @return List of questions from all categories filtered by user, question type and question level
	 * @throws ServiceException
	 */
	public List<Question> getAllCategoriesQuestions(Operation operation,User user,String questionType,
		String questionLevel)
	{
		boolean singleOp=operation==null;
		List<Question> allCategoriesQuestions=null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			allCategoriesQuestions=getAllCategoriesQuestions(
				operation,getCurrentUser(operation),user,questionType,questionLevel);
		}
		finally
		{
			if (singleOp)
			{
				// End Hibernate operation
				HibernateUtil.endOperation(operation);
			}
		}
		return allCategoriesQuestions;
	}
	
	/**
	 * @param viewer User used to check visibility
	 * @param user User or null to get questions from all users
	 * @param questionType Filtering question type or empty string to get questions of all types
	 * @param questionLevel Filtering question level or empty string to get questions of all levels
	 * @return List of questions from all categories filtered by user, question type and question level
	 * @throws ServiceException
	 */
	public List<Question> getAllCategoriesQuestions(User viewer,User user,String questionType,String questionLevel)
	{
		return getAllCategoriesQuestions(null,viewer,user,questionType,questionLevel);
	}
	
	/**
	 * @param operation Operation
	 * @param viewer User used to check visibility
	 * @param user User or null to get questions from all users
	 * @param questionType Filtering question type or empty string to get questions of all types
	 * @param questionLevel Filtering question level or empty string to get questions of all levels
	 * @return List of questions from all categories filtered by user, question type and question level
	 * @throws ServiceException
	 */
	public List<Question> getAllCategoriesQuestions(Operation operation,User viewer,User user,String questionType,
		String questionLevel)
	{
		List<Question> allCategoriesQuestions=null;
		boolean singleOp=operation==null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			if (permissionsService.isGranted(operation,viewer,"PERMISSION_QUESTIONS_GLOBAL_FILTER_ENABLED") &&
				permissionsService.isGranted(
				operation,viewer,"PERMISSION_QUESTIONS_OTHER_USERS_FILTER_ENABLED") &&
				permissionsService.isGranted(operation,viewer,
				"PERMISSION_QUESTIONS_VIEW_QUESTIONS_OF_OTHER_USERS_PRIVATE_CATEGORIES_ENABLED"))
			{
				boolean includeAdminsPrivateCategories=permissionsService.isGranted(operation,viewer,
					"PERMISSION_QUESTIONS_VIEW_QUESTIONS_OF_ADMINS_PRIVATE_CATEGORIES_ENABLED");
				boolean includeSuperadminsPrivateCategories=permissionsService.isGranted(operation,viewer,
					"PERMISSION_QUESTIONS_VIEW_QUESTIONS_OF_SUPERADMINS_PRIVATE_CATEGORIES_ENABLED");
				List<Long> allCategoriesIds=categoriesService.getAllCategoriesIds(operation,viewer,
					categoryTypesService.getCategoryType(operation,"CATEGORY_TYPE_QUESTIONS"),
					includeAdminsPrivateCategories,includeSuperadminsPrivateCategories);
				QUESTIONS_DAO.setOperation(operation);
				allCategoriesQuestions=QUESTIONS_DAO.getQuestions(
					user==null?0L:user.getId(),allCategoriesIds,questionType,questionLevel,true,true);
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
		return allCategoriesQuestions;
	}
	
	/**
	 * @param user User or null to get questions from all users
	 * @param questionType Filtering question type or empty string to get questions of all types
	 * @param questionLevel Filtering question level or empty string to get questions of all levels
	 * @return List of questions from all visible categories filtered by user, question type 
	 * and question level
	 * @throws ServiceException
	 */
	public List<Question> getAllVisibleCategoriesQuestions(User user,String questionType,String questionLevel)
		throws ServiceException
	{
		return getAllVisibleCategoriesQuestions((Operation)null,user,questionType,questionLevel);
	}
	
	/**
	 * @param operation Operation
	 * @param user User or null to get questions from all users
	 * @param questionType Filtering question type or empty string to get questions of all types
	 * @param questionLevel Filtering question level or empty string to get questions of all levels
	 * @return List of questions from all visible categories filtered by user, question type 
	 * and question level
	 * @throws ServiceException
	 */
	public List<Question> getAllVisibleCategoriesQuestions(Operation operation,User user,String questionType,
		String questionLevel) throws ServiceException
	{
		boolean singleOp=operation==null;
		List<Question> allVisibleCategoriesQuestions=null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			allVisibleCategoriesQuestions=getAllVisibleCategoriesQuestions(
				operation,getCurrentUser(operation),user,questionType,questionLevel);
		}
		finally
		{
			if (singleOp)
			{
				// End Hibernate operation
				HibernateUtil.endOperation(operation);
			}
		}
		return allVisibleCategoriesQuestions;
	}
	
	/**
	 * @param viewer User used to check visibility
	 * @param user User or null to get questions from all users
	 * @param questionType Filtering question type or empty string to get questions of all types
	 * @param questionLevel Filtering question level or empty string to get questions of all levels
	 * @return List of questions from all visible categories filtered by user, question type 
	 * and question level
	 * @throws ServiceException
	 */
	public List<Question> getAllVisibleCategoriesQuestions(User viewer,User user,String questionType,
		String questionLevel) throws ServiceException
	{
		return getAllVisibleCategoriesQuestions(null,viewer,user,questionType,questionLevel);
	}
	
	/**
	 * @param operation Operation
	 * @param viewer User used to check visibility
	 * @param user User or null to get questions from all users
	 * @param questionType Filtering question type or empty string to get questions of all types
	 * @param questionLevel Filtering question level or empty string to get questions of all levels
	 * @return List of questions from all visible categories filtered by user, question type 
	 * and question level
	 * @throws ServiceException
	 */
	public List<Question> getAllVisibleCategoriesQuestions(Operation operation,User viewer,User user,
		String questionType,String questionLevel) throws ServiceException
	{
		List<Question> allVisibleCategoriesQuestions=null;
		boolean singleOp=operation==null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			if (permissionsService.isGranted(operation,viewer,"PERMISSION_QUESTIONS_GLOBAL_FILTER_ENABLED") &&
				permissionsService.isGranted(
				operation,viewer,"PERMISSION_QUESTIONS_OTHER_USERS_FILTER_ENABLED"))
			{
				List<Long> allVisibleCategoriesIds=categoriesService.getAllVisibleCategoriesIds(
					operation,viewer,categoryTypesService.getCategoryType(operation,"CATEGORY_TYPE_QUESTIONS"));
				QUESTIONS_DAO.setOperation(operation);
				allVisibleCategoriesQuestions=QUESTIONS_DAO.getQuestions(
					user==null?0L:user.getId(),allVisibleCategoriesIds,questionType,questionLevel,true,true);
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
		return allVisibleCategoriesQuestions; 
	}
	
	/**
	 * @param user User or null to get questions from all users
	 * @param questionType Filtering question type or empty string to get questions of all types
	 * @param questionLevel Filtering question level or empty string to get questions of all levels
	 * @return List of questions from all categories of current user (including its global categories) 
	 * filtered by user, question type and question level
	 * @throws ServiceException
	 */
	public List<Question> getAllMyCategoriesQuestions(User user,String questionType,String questionLevel)
		throws ServiceException
	{
		return getAllMyCategoriesQuestions((Operation)null,user,questionType,questionLevel);
	}
	
	/**
	 * @param operation Operation
	 * @param user User or null to get questions from all users
	 * @param questionType Filtering question type or empty string to get questions of all types
	 * @param questionLevel Filtering question level or empty string to get questions of all levels
	 * @return List of questions from all categories of current user (including its global categories) 
	 * filtered by user, question type and question level
	 * @throws ServiceException
	 */
	public List<Question> getAllMyCategoriesQuestions(Operation operation,User user,String questionType,
		String questionLevel) throws ServiceException
	{
		boolean singleOp=operation==null;
		List<Question> allMyCategoriesQuestions=null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			allMyCategoriesQuestions=
				getAllMyCategoriesQuestions(operation,getCurrentUser(operation),user,questionType,questionLevel);
		}
		finally
		{
			if (singleOp)
			{
				// End Hibernate operation
				HibernateUtil.endOperation(operation);
			}
		}
		return allMyCategoriesQuestions;
	}
	
	/**
	 * @param viewer User used to check visibility
	 * @param user User or null to get questions from all users
	 * @param questionType Filtering question type or empty string to get questions of all types
	 * @param questionLevel Filtering question level or empty string to get questions of all levels
	 * @return List of questions from all categories of current user (including its global categories) 
	 * filtered by user, question type and question level
	 * @throws ServiceException
	 */
	public List<Question> getAllMyCategoriesQuestions(User viewer,User user,String questionType,String questionLevel) 
		throws ServiceException
	{
		return getAllMyCategoriesQuestions(null,viewer,user,questionType,questionLevel);
	}
	
	/**
	 * @param operation Operation
	 * @param viewer User used to check visibility
	 * @param user User or null to get questions from all users
	 * @param questionType Filtering question type or empty string to get questions of all types
	 * @param questionLevel Filtering question level or empty string to get questions of all levels
	 * @return List of questions from all categories of current user (including its global categories) 
	 * filtered by user, question type and question level
	 * @throws ServiceException
	 */
	public List<Question> getAllMyCategoriesQuestions(Operation operation,User viewer,User user,String questionType,
		String questionLevel) throws ServiceException
	{
		List<Question> allMyCategoriesQuestions=null;
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
					operation,viewer,categoryTypesService.getCategoryType(operation,"CATEGORY_TYPE_QUESTIONS"));
				QUESTIONS_DAO.setOperation(operation);
				allMyCategoriesQuestions=QUESTIONS_DAO.getQuestions(
					user==null?0L:user.getId(),allMyCategoriesIds,questionType,questionLevel,true,true);
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
		return allMyCategoriesQuestions;
	}
	
	/**
	 * @param user User or null to get questions from all users
	 * @param questionType Filtering question type or empty string to get questions of all types
	 * @param questionLevel Filtering question level or empty string to get questions of all levels
	 * @return List of questions from all categories of current user (except global categories) 
	 * filtered by user, question type and question level
	 * @throws ServiceException
	 */
	public List<Question> getAllMyCategoriesExceptGlobalsQuestions(User user,String questionType,String questionLevel) 
		throws ServiceException 
	{
		return getAllMyCategoriesExceptGlobalsQuestions((Operation)null,user,questionType,questionLevel);
	}
	
	/**
	 * @param operation Operation
	 * @param user User or null to get questions from all users
	 * @param questionType Filtering question type or empty string to get questions of all types
	 * @param questionLevel Filtering question level or empty string to get questions of all levels
	 * @return List of questions from all categories of current user (except global categories) 
	 * filtered by user, question type and question level
	 * @throws ServiceException
	 */
	public List<Question> getAllMyCategoriesExceptGlobalsQuestions(Operation operation,User user,String questionType,
		String questionLevel) throws ServiceException
	{
		boolean singleOp=operation==null;
		List<Question> allMyCategoriesExceptGlobalsQuestions=null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			allMyCategoriesExceptGlobalsQuestions=getAllMyCategoriesExceptGlobalsQuestions(
				operation,getCurrentUser(operation),user,questionType,questionLevel);
		}
		finally
		{
			if (singleOp)
			{
				// End Hibernate operation
				HibernateUtil.endOperation(operation);
			}
		}
		return allMyCategoriesExceptGlobalsQuestions;
	}
	
	/**
	 * @param viewer User used to check visibility
	 * @param user User or null to get questions from all users
	 * @param questionType Filtering question type or empty string to get questions of all types
	 * @param questionLevel Filtering question level or empty string to get questions of all levels
	 * @return List of questions from all categories of current user (except global categories) 
	 * filtered by user, question type and question level
	 * @throws ServiceException
	 */
	public List<Question> getAllMyCategoriesExceptGlobalsQuestions(User viewer,User user,String questionType,
		String questionLevel) throws ServiceException 
	{
		return getAllMyCategoriesExceptGlobalsQuestions(null,viewer,user,questionType,questionLevel);
	}
	
	/**
	 * @param operation Operation
	 * @param viewer User used to check visibility
	 * @param user User or null to get questions from all users
	 * @param questionType Filtering question type or empty string to get questions of all types
	 * @param questionLevel Filtering question level or empty string to get questions of all levels
	 * @return List of questions from all categories of current user (except global categories) 
	 * filtered by user, question type and question level
	 * @throws ServiceException
	 */
	public List<Question> getAllMyCategoriesExceptGlobalsQuestions(Operation operation,User viewer,User user,
		String questionType,String questionLevel) throws ServiceException
	{
		List<Question> allMyCategoriesExceptGlobalsQuestions=null;
		boolean singleOp=operation==null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			List<Long> allUserCategoriesIdsExceptGlobalsIds=categoriesService.getAllUserCategoriesIdsExceptGlobals(
				operation,viewer,categoryTypesService.getCategoryType(operation,"CATEGORY_TYPE_QUESTIONS"));
			QUESTIONS_DAO.setOperation(operation);
			allMyCategoriesExceptGlobalsQuestions=QUESTIONS_DAO.getQuestions(
				user==null?0L:user.getId(),allUserCategoriesIdsExceptGlobalsIds,questionType,questionLevel,true,true);
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
		return allMyCategoriesExceptGlobalsQuestions; 
	}
	
	/**
	 * @param user User or null to get questions from all users
	 * @param questionType Filtering question type or empty string to get questions of all types
	 * @param questionLevel Filtering question level or empty string to get questions of all levels
	 * @return List of questions from all global categories filtered by user, question type and question level
	 * @throws ServiceException
	 */
	public List<Question> getAllGlobalCategoriesQuestions(User user,String questionType,String questionLevel)
		throws ServiceException
	{
		return getAllGlobalCategoriesQuestions((Operation)null,user,questionType,questionLevel);
	}
	
	/**
	 * @param operation Operation
	 * @param user User or null to get questions from all users
	 * @param questionType Filtering question type or empty string to get questions of all types
	 * @param questionLevel Filtering question level or empty string to get questions of all levels
	 * @return List of questions from all global categories filtered by user, question type and question level
	 * @throws ServiceException
	 */
	public List<Question> getAllGlobalCategoriesQuestions(Operation operation,User user,String questionType,
		String questionLevel) throws ServiceException
	{
		boolean singleOp=operation==null;
		List<Question> allGlobalCategoriesQuestions=null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			allGlobalCategoriesQuestions=
				getAllGlobalCategoriesQuestions(operation,getCurrentUser(operation),user,questionType,questionLevel);
		}
		finally
		{
			if (singleOp)
			{
				// End Hibernate operation
				HibernateUtil.endOperation(operation);
			}
		}
		return allGlobalCategoriesQuestions;
	}
	
	/**
	 * @param viewer User used to check visibility
	 * @param user User or null to get questions from all users
	 * @param questionType Filtering question type or empty string to get questions of all types
	 * @param questionLevel Filtering question level or empty string to get questions of all levels
	 * @return List of questions from all global categories filtered by user, question type and question level
	 * @throws ServiceException
	 */
	public List<Question> getAllGlobalCategoriesQuestions(User user,User viewer,String questionType,
		String questionLevel)throws ServiceException
	{
		return getAllGlobalCategoriesQuestions(null,viewer,user,questionType,questionLevel);
	}
	
	/**
	 * @param operation Operation
	 * @param viewer User used to check visibility
	 * @param user User or null to get questions from all users
	 * @param questionType Filtering question type or empty string to get questions of all types
	 * @param questionLevel Filtering question level or empty string to get questions of all levels
	 * @return List of questions from all global categories filtered by user, question type and question level
	 * @throws ServiceException
	 */
	public List<Question> getAllGlobalCategoriesQuestions(Operation operation,User viewer,User user,
		String questionType,String questionLevel) throws ServiceException
	{
		List<Question> allGlobalCategoriesQuestions=null;
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
				List<Long> allGlobalCategoriesIds=categoriesService.getAllGlobalCategoriesIds(
					operation,categoryTypesService.getCategoryType(operation,"CATEGORY_TYPE_QUESTIONS"));
				QUESTIONS_DAO.setOperation(operation);
				allGlobalCategoriesQuestions=QUESTIONS_DAO.getQuestions(
					user==null?0L:user.getId(),allGlobalCategoriesIds,questionType,questionLevel,true,true);
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
		return allGlobalCategoriesQuestions;
	}
	
	/**
	 * @param user User or null to get questions from all users
	 * @param questionType Filtering question type or empty string to get questions of all types
	 * @param questionLevel Filtering question level or empty string to get questions of all levels
	 * @return List of questions of all public categories of all users except the current one filtered 
	 * by user, question type and question level
	 * @throws ServiceException
	 */
	public List<Question> getAllPublicCategoriesOfOtherUsersQuestions(User user,String questionType,
		String questionLevel) throws ServiceException
	{
		return getAllPublicCategoriesOfOtherUsersQuestions((Operation)null,user,questionType,questionLevel);
	}
	
	/**
	 * @param operation Operation
	 * @param user User or null to get questions from all users
	 * @param questionType Filtering question type or empty string to get questions of all types
	 * @param questionLevel Filtering question level or empty string to get questions of all levels
	 * @return List of questions of all public categories of all users except the current one filtered 
	 * by user, question type and question level
	 * @throws ServiceException
	 */
	public List<Question> getAllPublicCategoriesOfOtherUsersQuestions(Operation operation,User user,
		String questionType,String questionLevel) throws ServiceException
	{
		boolean singleOp=operation==null;
		List<Question> allPublicCategoriesOfOtherUsersQuestions=null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			allPublicCategoriesOfOtherUsersQuestions=getAllPublicCategoriesOfOtherUsersQuestions(
				operation,getCurrentUser(operation),user,questionType,questionLevel);
		}
		finally
		{
			if (singleOp)
			{
				// End Hibernate operation
				HibernateUtil.endOperation(operation);
			}
		}
		return allPublicCategoriesOfOtherUsersQuestions;
	}
	
	/**
	 * @param viewer User used to check visibility
	 * @param user User or null to get questions from all users
	 * @param questionType Filtering question type or empty string to get questions of all types
	 * @param questionLevel Filtering question level or empty string to get questions of all levels
	 * @return List of questions of all public categories of all users except the current one filtered 
	 * by user, question type and question level
	 * @throws ServiceException
	 */
	public List<Question> getAllPublicCategoriesOfOtherUsersQuestions(User viewer,User user,String questionType,
		String questionLevel) throws ServiceException
	{
		return getAllPublicCategoriesOfOtherUsersQuestions(null,viewer,user,questionType,questionLevel);
	}
	
	/**
	 * @param operation Operation
	 * @param viewer User used to check visibility
	 * @param user User or null to get questions from all users
	 * @param questionType Filtering question type or empty string to get questions of all types
	 * @param questionLevel Filtering question level or empty string to get questions of all levels
	 * @return List of questions of all public categories of all users except the current one filtered 
	 * by user, question type and question level
	 * @throws ServiceException
	 */
	public List<Question> getAllPublicCategoriesOfOtherUsersQuestions(Operation operation,User viewer,User user,
		String questionType,String questionLevel) throws ServiceException
	{
		List<Question> allPublicCategoriesOfOtherUsersQuestions=null;
		boolean singleOp=operation==null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			if (permissionsService.isGranted(operation,viewer,"PERMISSION_QUESTIONS_OTHER_USERS_FILTER_ENABLED"))
			{
				List<Long> allPublicCategoriesOfOtherUsersIds=categoriesService.getAllPublicCategoriesOfOtherUsersIds(
					operation,viewer,categoryTypesService.getCategoryType(operation,"CATEGORY_TYPE_QUESTIONS"));
				QUESTIONS_DAO.setOperation(operation);
				allPublicCategoriesOfOtherUsersQuestions=QUESTIONS_DAO.getQuestions(user==null?0L:user.getId(),
					allPublicCategoriesOfOtherUsersIds,questionType,questionLevel,true,true);
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
		return allPublicCategoriesOfOtherUsersQuestions; 
	}
	
	/**
	 * @param user User or null to get questions from all users
	 * @param questionType Filtering question type or empty string to get questions of all types
	 * @param questionLevel Filtering question level or empty string to get questions of all levels
	 * @return List questions from all private categories of all users except the current one filtered 
	 * by user, question type and question level
	 * @throws ServiceException
	 */
	public List<Question> getAllPrivateCategoriesOfOtherUsersQuestions(User user,String questionType,
		String questionLevel) throws ServiceException
	{
		return getAllPrivateCategoriesOfOtherUsersQuestions((Operation)null,user,questionType,questionLevel);
	}
	
	/**
	 * @param operation Operation
	 * @param user User or null to get questions from all users
	 * @param questionType Filtering question type or empty string to get questions of all types
	 * @param questionLevel Filtering question level or empty string to get questions of all levels
	 * @return List questions from private categories of all users except the current one filtered by user, 
	 * question type and question level
	 * @throws ServiceException
	 */
	public List<Question> getAllPrivateCategoriesOfOtherUsersQuestions(Operation operation,User user,
		String questionType,String questionLevel) throws ServiceException
	{
		boolean singleOp=operation==null;
		List<Question> allPrivateCategoriesOfOtherUsersQuestions=null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			allPrivateCategoriesOfOtherUsersQuestions=getAllPrivateCategoriesOfOtherUsersQuestions(
				operation,getCurrentUser(operation),user,questionType,questionLevel);
		}
		finally
		{
			if (singleOp)
			{
				// End Hibernate operation
				HibernateUtil.endOperation(operation);
			}
		}
		return allPrivateCategoriesOfOtherUsersQuestions;
	}
	
	/**
	 * @param viewer User used to check visibility
	 * @param user User or null to get questions from all users
	 * @param questionType Filtering question type or empty string to get questions of all types
	 * @param questionLevel Filtering question level or empty string to get questions of all levels
	 * @return List questions from all private categories of all users except the current one filtered 
	 * by user, question type and question level
	 * @throws ServiceException
	 */
	public List<Question> getAllPrivateCategoriesOfOtherUsersQuestions(User viewer,User user,String questionType,
		String questionLevel) throws ServiceException
	{
		return getAllPrivateCategoriesOfOtherUsersQuestions(null,viewer,user,questionType,questionLevel);
	}
	
	/**
	 * @param operation Operation
	 * @param viewer User used to check visibility
	 * @param user User or null to get questions from all users
	 * @param questionType Filtering question type or empty string to get questions of all types
	 * @param questionLevel Filtering question level or empty string to get questions of all levels
	 * @return List questions from all private categories of all users except the current one filtered 
	 * by user, question type and question level
	 * @throws ServiceException
	 */
	public List<Question> getAllPrivateCategoriesOfOtherUsersQuestions(Operation operation,User viewer,User user,
		String questionType,String questionLevel) throws ServiceException
	{
		List<Question> allPrivateCategoriesOfOtherUsersQuestions=null;
		boolean singleOp=operation==null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			if (permissionsService.isGranted(operation,viewer,"PERMISSION_QUESTIONS_OTHER_USERS_FILTER_ENABLED") && 
				permissionsService.isGranted(
				operation,viewer,"PERMISSION_QUESTIONS_VIEW_QUESTIONS_OF_OTHER_USERS_PRIVATE_CATEGORIES_ENABLED"))
			{
				boolean includeAdminsPrivateCategories=permissionsService.isGranted(
					operation,viewer,"PERMISSION_QUESTIONS_VIEW_QUESTIONS_OF_ADMINS_PRIVATE_CATEGORIES_ENABLED");
				boolean includeSuperadminsPrivateCategories=permissionsService.isGranted(
					operation,viewer,"PERMISSION_QUESTIONS_VIEW_QUESTIONS_OF_SUPERADMINS_PRIVATE_CATEGORIES_ENABLED");
				List<Long> allPrivateCategoriesOfOtherUsersIds=
					categoriesService.getAllPrivateCategoriesOfOtherUsersIds(operation,viewer,
					categoryTypesService.getCategoryType(operation,"CATEGORY_TYPE_QUESTIONS"),
					includeAdminsPrivateCategories,includeSuperadminsPrivateCategories);
				QUESTIONS_DAO.setOperation(operation);
				allPrivateCategoriesOfOtherUsersQuestions=QUESTIONS_DAO.getQuestions(user==null?0L:user.getId(),
					allPrivateCategoriesOfOtherUsersIds,questionType,questionLevel,true,true);
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
		return allPrivateCategoriesOfOtherUsersQuestions; 
	}
	
	/**
	 * @param user User or null to get questions from all users
	 * @param questionType Filtering question type or empty string to get questions of all types
	 * @param questionLevel Filtering question level or empty string to get questions of all levels
	 * @return List questions from all categories of all users except the current one filtered by user, 
	 * question type and question level
	 * @throws ServiceException
	 */
	public List<Question> getAllCategoriesOfOtherUsersQuestions(User user,String questionType,String questionLevel) 
		throws ServiceException
	{
		return getAllCategoriesOfOtherUsersQuestions((Operation)null,user,questionType,questionLevel);
	}
	
	/**
	 * @param operation Operation
	 * @param user User or null to get questions from all users
	 * @param questionType Filtering question type or empty string to get questions of all types
	 * @param questionLevel Filtering question level or empty string to get questions of all levels
	 * @return List questions from all categories of all users except the current one filtered by user, 
	 * question type and question level
	 * @throws ServiceException
	 */
	public List<Question> getAllCategoriesOfOtherUsersQuestions(Operation operation,User user,String questionType,
		String questionLevel) throws ServiceException
	{
		boolean singleOp=operation==null;
		List<Question> allCategoriesOfOtherUsersQuestions=null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			allCategoriesOfOtherUsersQuestions=getAllCategoriesOfOtherUsersQuestions(
				operation,getCurrentUser(operation),user,questionType,questionLevel);
		}
		finally
		{
			if (singleOp)
			{
				// End Hibernate operation
				HibernateUtil.endOperation(operation);
			}
		}
		return allCategoriesOfOtherUsersQuestions;
	}
	
	/**
	 * @param viewer User used to check visibility
	 * @param user User or null to get questions from all users
	 * @param questionType Filtering question type or empty string to get questions of all types
	 * @param questionLevel Filtering question level or empty string to get questions of all levels
	 * @return List questions from all categories of all users except the current one filtered by user, 
	 * question type and question level
	 * @throws ServiceException
	 */
	public List<Question> getAllCategoriesOfOtherUsersQuestions(User viewer,User user,String questionType,
		String questionLevel) throws ServiceException
	{
		return getAllCategoriesOfOtherUsersQuestions(null,viewer,user,questionType,questionLevel);
	}
	
	/**
	 * @param operation Operation
	 * @param viewer User used to check visibility
	 * @param user User or null to get questions from all users
	 * @param questionType Filtering question type or empty string to get questions of all types
	 * @param questionLevel Filtering question level or empty string to get questions of all levels
	 * @return List questions from all categories of all users except the current one filtered by user, 
	 * question type and question level
	 * @throws ServiceException
	 */
	public List<Question> getAllCategoriesOfOtherUsersQuestions(Operation operation,User viewer,User user,
		String questionType,String questionLevel) throws ServiceException
	{
		List<Question> allCategoriesOfOtherUsersQuestions=null;
		boolean singleOp=operation==null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			if (permissionsService.isGranted(operation,viewer,"PERMISSION_QUESTIONS_OTHER_USERS_FILTER_ENABLED") && 
				permissionsService.isGranted(
				operation,viewer,"PERMISSION_QUESTIONS_VIEW_QUESTIONS_OF_OTHER_USERS_PRIVATE_CATEGORIES_ENABLED"))
			{
				boolean includeAdminsPrivateCategories=permissionsService.isGranted(
					operation,viewer,"PERMISSION_QUESTIONS_VIEW_QUESTIONS_OF_ADMINS_PRIVATE_CATEGORIES_ENABLED");
				boolean includeSuperadminsPrivateCategories=permissionsService.isGranted(
					operation,viewer,"PERMISSION_QUESTIONS_VIEW_QUESTIONS_OF_SUPERADMINS_PRIVATE_CATEGORIES_ENABLED");
				List<Long> allCategoriesOfOtherUsersIds=categoriesService.getAllCategoriesOfOtherUsersIds(
					operation,viewer,categoryTypesService.getCategoryType(operation,"CATEGORY_TYPE_QUESTIONS"),
					includeAdminsPrivateCategories,includeSuperadminsPrivateCategories);
				QUESTIONS_DAO.setOperation(operation);
				allCategoriesOfOtherUsersQuestions=QUESTIONS_DAO.getQuestions(
					user==null?0L:user.getId(),allCategoriesOfOtherUsersIds,questionType,questionLevel,true,true);
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
		return allCategoriesOfOtherUsersQuestions; 
	}
	
	/**
	 * @param user User or null to get total number of questions
	 * @return Number of questions filtered by user (or total)
	 * @throws ServiceException
	 */
	public int getQuestionsCount(User user) throws ServiceException
	{
		return getQuestionsCount(null,user);
	}
	
	/**
	 * @param operation Operation
	 * @param user User or null to get total number of questions
	 * @return Number of questions filtered by user (or total)
	 * @throws ServiceException
	 */
	public int getQuestionsCount(Operation operation,User user) throws ServiceException
	{
		int questionsCount=0;
		boolean singleOp=operation==null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			QUESTIONS_DAO.setOperation(operation);
			questionsCount=QUESTIONS_DAO.getQuestions(user==null?0L:user.getId(),0L,"","",false,false).size();
		}
		finally
		{
			if (singleOp)
			{
				// End Hibernate operation
				HibernateUtil.endOperation(operation);
			}
		}
		return questionsCount;
	}
	
	/**
	 * @param questionName Question name to check
	 * @param categoryId Category identifier
	 * @param questionId Question identifier (of the question to exclude from checking)
	 * @return true if the question name checked is available for the indicated category, false otherwise
	 * @throws ServiceException
	 */
	public boolean isQuestionNameAvailable(String questionName,long categoryId,long questionId) 
		throws ServiceException
	{
		return isQuestionNameAvailable(null,questionName,categoryId,questionId);
	}
	
	/**
	 * @param operation Operation
	 * @param questionName Question name to check
	 * @param categoryId Category identifier
	 * @param questionId Question identifier (of the question to exclude from checking)
	 * @return true if the question name checked is available for the indicated category, false otherwise
	 * @throws ServiceException
	 */
	public boolean isQuestionNameAvailable(Operation operation,String questionName,long categoryId,long questionId)
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
				
				QUESTIONS_DAO.setOperation(operation);
				for (Question question:QUESTIONS_DAO.getQuestions(0L,categoryId,"","",false,false))
				{
					if (questionId!=question.getId() && questionName.equals(question.getName()))
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
	 * @param questionType Filtering question type or empty string to get questions of all types
	 * @param questionLevel Filtering question level or empty string to get questions of all levels
	 * @return Number of questions from all user global categories filtered question type and question level
	 * @throws ServiceException
	 */
	public int getAllUserGlobalCategoriesQuestionsCount(User user,String questionType,String questionLevel) 
		throws ServiceException
	{
		return getAllUserGlobalCategoriesQuestionsCount(null,user,questionType,questionLevel);
	}
	
	/**
	 * @param operation Operation
	 * @param user User
	 * @param questionType Filtering question type or empty string to get questions of all types
	 * @param questionLevel Filtering question level or empty string to get questions of all levels
	 * @return Number of questions from all user global categories filtered by question type and question level
	 * @throws ServiceException
	 */
	public int getAllUserGlobalCategoriesQuestionsCount(Operation operation,User user,String questionType,
		String questionLevel) throws ServiceException
	{
		boolean singleOp=operation==null;
		int allUserGlobalCategoriesQuestionsCount=0;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			List<Long> allUserGlobalCategoriesIds=categoriesService.getAllUserGlobalCategoriesIds(
				operation,user,categoryTypesService.getCategoryType(operation,"CATEGORY_TYPE_QUESTIONS"));
			QUESTIONS_DAO.setOperation(operation);
			List<Question> allUserGlobalCategoriesQuestions=
				QUESTIONS_DAO.getQuestions(0L,allUserGlobalCategoriesIds,questionType,questionLevel,true,true);
			allUserGlobalCategoriesQuestionsCount=allUserGlobalCategoriesQuestions.size();
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
		return allUserGlobalCategoriesQuestionsCount;
	}
	
	//Obtiene una pregunta a partir de su id
	/**
	 * @param id Question identifier
	 * @return Question
	 * @throws ServiceException
	 */
	public Question getQuestion(long id) throws ServiceException
	{
		return getQuestion(null,id);
	}
	
	/**
	 * @param operation Operation
	 * @param id Question identifier
	 * @return Question
	 * @throws ServiceException
	 */
	public Question getQuestion(Operation operation,long id) throws ServiceException
	{
		Question question=null;
		try
		{
			QUESTIONS_DAO.setOperation(operation);
			question=QUESTIONS_DAO.getQuestion(id,true,true,true,true,true);
		}
		catch (DaoException de)
		{
			throw new ServiceException(de.getMessage(),de);
		}
		//TODO ¿para que se ordenan las respuestas? ¿es conveniente dejar esto asi?
		if (question!=null)
		{
			Collections.sort(question.getAnswers(),new AnswerComparator());
		}
		return question;
	}
	
	//Añade una nueva pregunta
	/**
	 * Adds a new question.
	 * @param question Question to add
	 * @throws ServiceException
	 */
	public void addQuestion(Question question) throws ServiceException
	{
		addQuestion(null,question);
	}
	
	/**
	 * Adds a new question.
	 * @param operation Operation
	 * @param question Question to add
	 * @throws ServiceException
	 */
	public void addQuestion(Operation operation,Question question) throws ServiceException
	{
		try
		{
			QUESTIONS_DAO.setOperation(operation);
			QUESTIONS_DAO.saveQuestion(question);
		}
		catch (DaoException de)
		{
			throw new ServiceException(de.getMessage(),de);
		}
	}
	
	/**
	 * Updates a question.
	 * @param question Question to update
	 * @throws ServiceException
	 */
	public void updateQuestion(Question question) throws ServiceException
	{
		updateQuestion(null,question);
	}
	
	/**
	 * Updates a question.
	 * @param operation Operation
	 * @param question Question to update
	 * @throws ServiceException
	 */
	public void updateQuestion(Operation operation,Question question) throws ServiceException
	{
		boolean singleOp=operation==null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			List<Answer> answersFromDB=answersService.getAnswers(operation,question);
			
			// We delete removed answers from DB 
			for (Answer answer:answersFromDB)
			{
				if (!question.getAnswers().contains(answer))
				{
					answersService.deleteAnswer(operation,answer);
				}
			}
			
			// We add/update answers on DB
			for (Answer answer:question.getAnswers())
			{
				if (answersFromDB.contains(answer))
				{
					Answer answerFromDB=answersFromDB.get(answersFromDB.indexOf(answer));
					answerFromDB.setFromOtherAnswer(answer);
					answersService.updateAnswer(operation,answerFromDB);
				}
				else
				{
					answersService.addAnswer(operation,answer);
				}
			}
			
			List<QuestionResource> questionResourcesFromDB=
				questionResourcesService.getQuestionResources(operation,question);
			
			// We delete removed resources of question from DB 
			for (QuestionResource questionResource:questionResourcesFromDB)
			{
				if (!question.getQuestionResources().contains(questionResource))
				{
					questionResourcesService.deleteQuestionResource(operation,questionResource);
				}
			}
			
			// We add/update resources of question on DB
			for (QuestionResource questionResource:question.getQuestionResources())
			{
				if (questionResourcesFromDB.contains(questionResource))
				{
					QuestionResource questionResourceFromDB=
						questionResourcesFromDB.get(questionResourcesFromDB.indexOf(questionResource));
					questionResourceFromDB.setFromOtherQuestionResource(questionResource);
					questionResourcesService.updateQuestionResource(operation,questionResourceFromDB);
				}
				else
				{
					questionResourcesService.addQuestionResource(operation,questionResource);
				}
			}
			
			List<Feedback> feedbacksFromDB=feedbacksService.getFeedbacks(operation,question);
			
			// We delete removed feedbacks from DB
			for (Feedback feedback:feedbacksFromDB)
			{
				if (!question.getFeedbacks().contains(feedback))
				{
					feedbacksService.deleteFeedback(operation,feedback);
				}
			}
			
			// We add/update feedbacks on DB
			for (Feedback feedback:question.getFeedbacks())
			{
				if (feedbacksFromDB.contains(feedback))
				{
					Feedback feedbackFromDB=feedbacksFromDB.get(feedbacksFromDB.indexOf(feedback));
					feedbackFromDB.setFromOtherFeedback(feedback);
					feedbacksService.updateFeedback(operation,feedbackFromDB);
				}
				else
				{
					feedbacksService.addFeedback(operation,feedback);
				}
			}
			
			// We get question from DB
			QUESTIONS_DAO.setOperation(operation);
			Question questionFromDB=QUESTIONS_DAO.getQuestion(question.getId(),false,false,false,false,false);
			
			// We update question on DB
			questionFromDB.setFromOtherQuestion(question);
			QUESTIONS_DAO.setOperation(operation);
			QUESTIONS_DAO.updateQuestion(questionFromDB);
			
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
	 * Deletes a question.<br/><br/>
	 * Note that it throws a <i>QuestionDeleteConstraintServiceException</i> exception 
	 * if there is a test or more using this question.
	 * @param question Question to delete
	 * @throws ServiceException
	 */
	public void deleteQuestion(Question question) throws ServiceException
	{
		deleteQuestion(null,question);
	}
	
	/**
	 * Deletes a question.
	 * @param operation Operation
	 * @param question Question to delete
	 * @throws ServiceException
	 */
	public void deleteQuestion(Operation operation,Question question) throws ServiceException
	{
		try
		{
			QUESTIONS_DAO.setOperation(operation);
			QUESTIONS_DAO.deleteQuestion(question);
		}
		catch (DaoException de)
		{
			if (de.getCause() instanceof ConstraintViolationException)
			{
				throw new QuestionDeleteConstraintServiceException(de.getMessage(),de);
			}
			else
			{
				throw new ServiceException(de.getMessage(),de);
			}
		}
	}
	
	/**
	 * Deletes a question.<br/><br/>
	 * Note that it throws a <i>QuestionDeleteConstraintServiceException</i> exception 
	 * if there is a test or more using this question.
	 * @param questionId Question's identifier
	 * @throws ServiceException
	 */
	public void deleteQuestion(long questionId) throws ServiceException
	{
		deleteQuestion(null,questionId);
	}
	
	/**
	 * Deletes a question.<br/><br/>
	 * Note that it throws a <i>QuestionDeleteConstraintServiceException</i> exception 
	 * if there is a test or more using this question.
	 * @param operation Operation
	 * @param questionId Question's identifier
	 * @throws ServiceException
	 */
	public void deleteQuestion(Operation operation,long questionId) throws ServiceException
	{
		boolean singleOp=operation==null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			QUESTIONS_DAO.setOperation(operation);
			Question question=QUESTIONS_DAO.getQuestion(questionId,false,false,false,false,false);
			
			// Delete question
			deleteQuestion(operation,question);
			
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
				throw new QuestionDeleteConstraintServiceException(de.getMessage(),de);
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
				if (se.getCause() instanceof ConstraintViolationException)
				{
					throw new QuestionDeleteConstraintServiceException(de.getMessage(),de);
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
	
	//Obtiene los niveles de dificultad
	/**
	 * @return List of question levels
	 */
	public List<QuestionLevel> getQuestionLevels()
	{
		List<QuestionLevel> levels=new ArrayList<QuestionLevel>(QuestionLevel.values().length);
		for(QuestionLevel level:QuestionLevel.values())
		{
			levels.add(level);
		}
		return levels;
	}
	
	//Obtiene los nombres de los niveles de dificultad
	/**
	 * @return List of localized names of question levels
	 */
	public List<String> getQuestionLevelStrings()
	{
		List<QuestionLevel> levels=getQuestionLevels();
		List<String> levelStrings=new ArrayList<String>(levels.size());
		for (QuestionLevel level:levels)
		{
			levelStrings.add(localizationService.getLocalizedMessage(level.toString()));
		}
		return levelStrings;
	}
	
	//Crea una nueva pregunta del tipo especificado 
	/**
	 * @param className Class name of question to instantiate
	 * @return New question
	 * @throws ServiceException
	 */
	@SuppressWarnings("rawtypes")
	public Question getNewQuestion(String className) throws ServiceException
	{
		Question question=null;
		try
		{
			Class c=Class.forName(className);
			Object o=c.newInstance();
			question=(Question)o;
		}
		catch (Exception ex)
		{
			throwServiceException("QUESTION_NEW_INSTANCE_ERROR",
				"A critical error has been ocurred when trying to instantiate the question.",ex);
		}
		return question;
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
		UserSessionService userSessionService=
			(UserSessionService)context.getApplication().getELResolver().getValue(
			context.getELContext(),null,"userSessionService");
		return userSessionService.getCurrentUser(operation);
		
	}
}
