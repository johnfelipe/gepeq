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
import java.util.Date;
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
import es.uned.lsi.gepec.model.entities.DragDropAnswer;
import es.uned.lsi.gepec.model.entities.Feedback;
import es.uned.lsi.gepec.model.entities.Question;
import es.uned.lsi.gepec.model.entities.QuestionResource;
import es.uned.lsi.gepec.model.entities.Resource;
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
	private List<Question> getQuestions(Operation operation,User viewer,User user,long categoryId,
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
			
			if (categoryId>0L)
			{
				// Check viewer permissions to execute this query
				Category category=categoriesService.getCategory(operation,categoryId);
				category.setVisibility(visibilitiesService.getVisibilityFromCategoryId(operation,categoryId));
				boolean checkVisibility=permissionsService.isDenied(operation,viewer,
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
			
			// We get questions from DB
			List<Question> questionsFromDB=null;
			QUESTIONS_DAO.setOperation(operation);
			if (!includeSubcategories || categoryId<=0L)
			{
				// We need to get questions from a single category (or without filtering by category if categoryId=0)
				questionsFromDB=QUESTIONS_DAO.getQuestions(
					user==null?0L:user.getId(),categoryId,questionType,questionLevel,true,true);
			}
			else
			{
				// We need to get questions from several categories
				questionsFromDB=QUESTIONS_DAO.getQuestions(user==null?0L:user.getId(),
					categoriesService.getDerivedCategoriesIds(operation,categoryId,null,
					categoryTypesService.getCategoryType(operation,"CATEGORY_TYPE_QUESTIONS")),questionType,
					questionLevel,true,true);
			}
			
			Map<Category,Boolean> checkedCategoriesVisibility=new HashMap<Category,Boolean>();
			
			// We return new referenced questions within a new list to avoid shared collection references
			// and object references to unsaved transient instances
			questions=new ArrayList<Question>();
			for (Question questionFromDB:questionsFromDB)
			{
				Question question=null;
				if (questionFromDB.getCategory()!=null)
				{
					Category categoryFromDB=questionFromDB.getCategory();
					boolean checkedCategoryVisibility=true;						
					if (checkedCategoriesVisibility.containsKey(categoryFromDB))
					{
						checkedCategoryVisibility=checkedCategoriesVisibility.get(categoryFromDB).booleanValue();
					}
					else
					{
						boolean checkQuestionVisibility=permissionsService.isDenied(operation,viewer,
							"PERMISSION_QUESTIONS_VIEW_QUESTIONS_OF_OTHER_USERS_PRIVATE_CATEGORIES_ENABLED") ||
							(permissionsService.isGranted(operation,categoryFromDB.getUser(),
							"PERMISSION_NAVIGATION_ADMINISTRATION") && permissionsService.isDenied(operation,viewer,
							"PERMISSION_QUESTIONS_VIEW_QUESTIONS_OF_ADMINS_PRIVATE_CATEGORIES_ENABLED")) ||
							(permissionsService.isGranted(operation,categoryFromDB.getUser(),
							"PERMISSION_ADMINISTRATION_RAISE_PERMISSIONS_OVER_OWNED_ALLOWED") &&
							permissionsService.isDenied(operation,viewer,
							"PERMISSION_QUESTIONS_VIEW_QUESTIONS_OF_SUPERADMINS_PRIVATE_CATEGORIES_ENABLED"));
						if (checkQuestionVisibility && !categoryFromDB.getUser().equals(viewer)&& 
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
						question=questionFromDB.getQuestionCopy();
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
						question.setCategory(category);
					}
				}
				if (question!=null)
				{
					if (questionFromDB.getCreatedBy()!=null)
					{
						User questionAuthor=questionFromDB.getCreatedBy().getUserCopy();
						
						// Password is set to empty string before returning instance for security reasons
						questionAuthor.setPassword("");
						
						question.setCreatedBy(questionAuthor);
					}
					if (questionFromDB.getModifiedBy()!=null)
					{
						User questionLastEditor=questionFromDB.getModifiedBy().getUserCopy();
						
						// Password is set to empty string before returning instance for security reasons
						questionLastEditor.setPassword("");
						
						question.setModifiedBy(questionLastEditor);
					}
					questions.add(question);
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
		return questions;
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
	public List<Question> getQuestions(User user,long categoryId,boolean includeSubcategories) 
		throws ServiceException
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
			
			questions=getQuestions(operation,getCurrentUser(operation),user,categoryId,includeSubcategories,"","");
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
	 * @param operation Operation
	 * @param viewer User used to check visibility
	 * @param user User or null to get questions from all users
	 * @param questionType Filtering question type or empty string to get questions of all types
	 * @param questionLevel Filtering question level or empty string to get questions of all levels
	 * @return List of questions from all categories filtered by user, question type and question level
	 * @throws ServiceException
	 */
	private List<Question> getAllCategoriesQuestions(Operation operation,User viewer,User user,String questionType,
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
			
			// Check viewer permissions to execute this query
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
				
				// We get questions from DB
				QUESTIONS_DAO.setOperation(operation);
				List<Question> allCategoriesQuestionsFromDB=QUESTIONS_DAO.getQuestions(
					user==null?0L:user.getId(),allCategoriesIds,questionType,questionLevel,true,true);
				
				// We return new referenced questions from all categories within a new list to avoid 
				// shared collection references and object references to unsaved transient instances
				allCategoriesQuestions=new ArrayList<Question>(allCategoriesQuestionsFromDB.size());
				for (Question allCategoriesQuestionFromDB:allCategoriesQuestionsFromDB)
				{
					Question allCategoriesQuestion=allCategoriesQuestionFromDB.getQuestionCopy();
					if (allCategoriesQuestionFromDB.getCreatedBy()!=null)
					{
						User allCategoriesQuestionAuthor=allCategoriesQuestionFromDB.getCreatedBy().getUserCopy();
						
						// Password is set to empty string before returning instance for security reasons
						allCategoriesQuestionAuthor.setPassword("");
						
						allCategoriesQuestion.setCreatedBy(allCategoriesQuestionAuthor);
					}
					if (allCategoriesQuestionFromDB.getModifiedBy()!=null)
					{
						User allCategoriesQuestionLastEditor=
							allCategoriesQuestionFromDB.getModifiedBy().getUserCopy();
						
						// Password is set to empty string before returning instance for security reasons
						allCategoriesQuestionLastEditor.setPassword("");
						
						allCategoriesQuestion.setModifiedBy(allCategoriesQuestionLastEditor);
					}
					if (allCategoriesQuestionFromDB.getCategory()!=null)
					{
						Category categoryFromDB=allCategoriesQuestionFromDB.getCategory();
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
						allCategoriesQuestion.setCategory(category);
					}
					allCategoriesQuestions.add(allCategoriesQuestion);
				}
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
			
			allCategoriesQuestions=
				getAllCategoriesQuestions(operation,getCurrentUser(operation),user,questionType,questionLevel);
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
	 * @param operation Operation
	 * @param viewer User used to check visibility
	 * @param user User or null to get questions from all users
	 * @param questionType Filtering question type or empty string to get questions of all types
	 * @param questionLevel Filtering question level or empty string to get questions of all levels
	 * @return List of questions from all visible categories filtered by user, question type 
	 * and question level
	 * @throws ServiceException
	 */
	private List<Question> getAllVisibleCategoriesQuestions(Operation operation,User viewer,User user,
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
			
			// Check viewer permissions to execute this query
			if (permissionsService.isGranted(operation,viewer,"PERMISSION_QUESTIONS_GLOBAL_FILTER_ENABLED") &&
				permissionsService.isGranted(operation,viewer,"PERMISSION_QUESTIONS_OTHER_USERS_FILTER_ENABLED"))
			{
				List<Long> allVisibleCategoriesIds=categoriesService.getAllVisibleCategoriesIds(
					operation,viewer,categoryTypesService.getCategoryType(operation,"CATEGORY_TYPE_QUESTIONS"));
				
				// We get questions from DB
				QUESTIONS_DAO.setOperation(operation);
				List<Question> allVisibleCategoriesQuestionsFromDB=QUESTIONS_DAO.getQuestions(
					user==null?0L:user.getId(),allVisibleCategoriesIds,questionType,questionLevel,true,true);
				
				// We return new referenced questions from all visible categories within a new list to avoid 
				// shared collection references and object references to unsaved transient instances
				allVisibleCategoriesQuestions=new ArrayList<Question>();
				for (Question allVisibleCategoriesQuestionFromDB:allVisibleCategoriesQuestionsFromDB)
				{
					Question allVisibleCategoriesQuestion=allVisibleCategoriesQuestionFromDB.getQuestionCopy();
					if (allVisibleCategoriesQuestionFromDB.getCreatedBy()!=null)
					{
						User allVisibleCategoriesQuestionAuthor=
							allVisibleCategoriesQuestionFromDB.getCreatedBy().getUserCopy();
						
						// Password is set to empty string before returning instance for security reasons
						allVisibleCategoriesQuestionAuthor.setPassword("");
						
						allVisibleCategoriesQuestion.setCreatedBy(allVisibleCategoriesQuestionAuthor);
					}
					if (allVisibleCategoriesQuestionFromDB.getModifiedBy()!=null)
					{
						User allVisibleCategoriesQuestionLastEditor=
							allVisibleCategoriesQuestionFromDB.getModifiedBy().getUserCopy();
						
						// Password is set to empty string before returning instance for security reasons
						allVisibleCategoriesQuestionLastEditor.setPassword("");
						
						allVisibleCategoriesQuestion.setModifiedBy(allVisibleCategoriesQuestionLastEditor);
					}
					if (allVisibleCategoriesQuestionFromDB.getCategory()!=null)
					{
						Category categoryFromDB=allVisibleCategoriesQuestionFromDB.getCategory();
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
						allVisibleCategoriesQuestion.setCategory(category);
					}
					allVisibleCategoriesQuestions.add(allVisibleCategoriesQuestion);
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
		return allVisibleCategoriesQuestions; 
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
			
			allVisibleCategoriesQuestions=
				getAllVisibleCategoriesQuestions(operation,getCurrentUser(operation),user,questionType,questionLevel);
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
	
	//TODO
	
	/**
	 * @param operation Operation
	 * @param viewer User used to check visibility
	 * @param owner Owner of categories from which we get the questions
	 * @param user User or null to get questions from all users
	 * @param questionType Filtering question type or empty string to get questions of all types
	 * @param questionLevel Filtering question level or empty string to get questions of all levels
	 * @return List of questions from all categories of an user (including its global categories) filtered by user, 
	 * question type and question level
	 * @throws ServiceException
	 */
	private List<Question> getAllUserCategoriesQuestions(Operation operation,User viewer,User owner,User user,
		String questionType,String questionLevel) throws ServiceException
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
			
			// Check viewer permissions to execute this query
			if (permissionsService.isGranted(operation,viewer,"PERMISSION_QUESTIONS_GLOBAL_FILTER_ENABLED") &&
				(viewer.equals(owner) || 
				permissionsService.isGranted(operation,viewer,"PERMISSION_QUESTIONS_OTHER_USERS_FILTER_ENABLED")))
			{
				List<Long> allMyCategoriesIds=categoriesService.getAllUserCategoriesIds(
					operation,owner,categoryTypesService.getCategoryType(operation,"CATEGORY_TYPE_QUESTIONS"));
				
				// We get questions from DB
				QUESTIONS_DAO.setOperation(operation);
				List<Question> allMyCategoriesQuestionsFromDB=QUESTIONS_DAO.getQuestions(
					user==null?0L:user.getId(),allMyCategoriesIds,questionType,questionLevel,true,true);
				
				// We return new referenced questions from all categories of owner within a new list 
				// to avoid shared collection references and object references to unsaved transient instances
				allMyCategoriesQuestions=new ArrayList<Question>(allMyCategoriesQuestionsFromDB.size());
				for (Question allMyCategoriesQuestionFromDB:allMyCategoriesQuestionsFromDB)
				{
					Question allMyCategoriesQuestion=allMyCategoriesQuestionFromDB.getQuestionCopy();
					if (allMyCategoriesQuestionFromDB.getCreatedBy()!=null)
					{
						User allMyCategoriesQuestionAuthor=
							allMyCategoriesQuestionFromDB.getCreatedBy().getUserCopy();
						
						// Password is set to empty string before returning instance for security reasons
						allMyCategoriesQuestionAuthor.setPassword("");
						
						allMyCategoriesQuestion.setCreatedBy(allMyCategoriesQuestionAuthor);
					}
					if (allMyCategoriesQuestionFromDB.getModifiedBy()!=null)
					{
						User allMyCategoriesQuestionLastEditor=
							allMyCategoriesQuestionFromDB.getModifiedBy().getUserCopy();
						
						// Password is set to empty string before returning instance for security reasons
						allMyCategoriesQuestionLastEditor.setPassword("");
						
						allMyCategoriesQuestion.setModifiedBy(allMyCategoriesQuestionLastEditor);
					}
					if (allMyCategoriesQuestionFromDB.getCategory()!=null)
					{
						Category categoryFromDB=allMyCategoriesQuestionFromDB.getCategory();
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
						allMyCategoriesQuestion.setCategory(category);
					}
					allMyCategoriesQuestions.add(allMyCategoriesQuestion);
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
		return allMyCategoriesQuestions;
	}
	
	/**
	 * @param owner Owner of categories from which we get the questions
	 * @param user User or null to get questions from all users
	 * @param questionType Filtering question type or empty string to get questions of all types
	 * @param questionLevel Filtering question level or empty string to get questions of all levels
	 * @return List of questions from all categories of an user (including its global categories) filtered by user, 
	 * question type and question level
	 * @throws ServiceException
	 */
	public List<Question> getAllUserCategoriesQuestions(User owner,User user,String questionType,String questionLevel)
		throws ServiceException
	{
		return getAllUserCategoriesQuestions((Operation)null,owner,user,questionType,questionLevel);
	}
	
	/**
	 * @param operation Operation
	 * @param owner Owner of categories from which we get the questions
	 * @param user User or null to get questions from all users
	 * @param questionType Filtering question type or empty string to get questions of all types
	 * @param questionLevel Filtering question level or empty string to get questions of all levels
	 * @return List of questions from all categories of an user (including its global categories) filtered by user, 
	 * question type and question level
	 * @throws ServiceException
	 */
	public List<Question> getAllUserCategoriesQuestions(Operation operation,User owner,User user,String questionType,
		String questionLevel) throws ServiceException
	{
		boolean singleOp=operation==null;
		List<Question> allUserCategoriesQuestions=null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			allUserCategoriesQuestions=getAllUserCategoriesQuestions(
				operation,getCurrentUser(operation),owner,user,questionType,questionLevel);
		}
		finally
		{
			if (singleOp)
			{
				// End Hibernate operation
				HibernateUtil.endOperation(operation);
			}
		}
		return allUserCategoriesQuestions;
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
			
			User currentUser=getCurrentUser(operation);
			allMyCategoriesQuestions=
				getAllUserCategoriesQuestions(operation,currentUser,currentUser,user,questionType,questionLevel);
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
	 * @param operation Operation
	 * @param viewer User used to check visibility
	 * @param owner Owner of categories from which we get the questions
	 * @param user User or null to get questions from all users
	 * @param questionType Filtering question type or empty string to get questions of all types
	 * @param questionLevel Filtering question level or empty string to get questions of all levels
	 * @return List of questions from all categories of an user (except global categories) filtered by user, 
	 * question type and question level
	 * @throws ServiceException
	 */
	private List<Question> getAllUserCategoriesExceptGlobalsQuestions(Operation operation,User viewer,User owner,
		User user,String questionType,String questionLevel) throws ServiceException
	{
		List<Question> allUserCategoriesExceptGlobalsQuestions=null;
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
					operation,owner,categoryTypesService.getCategoryType(operation,"CATEGORY_TYPE_QUESTIONS"));
				
				// We get questions from DB
				QUESTIONS_DAO.setOperation(operation);
				List<Question> allUserCategoriesExceptGlobalsQuestionsFromDB=QUESTIONS_DAO.getQuestions(
					user==null?0L:user.getId(),allUserCategoriesIdsExceptGlobalsIds,questionType,questionLevel,true,
					true);
				
				// We return new referenced questions from all categories of owner (except global categories) 
				// within a new list to avoid shared collection references and object references 
				// to unsaved transient instances
				allUserCategoriesExceptGlobalsQuestions=
					new ArrayList<Question>(allUserCategoriesExceptGlobalsQuestionsFromDB.size());
				for (Question allUserCategoriesExceptGlobalsQuestionFromDB:
					allUserCategoriesExceptGlobalsQuestionsFromDB)
				{
					Question allUserCategoriesExceptGlobalsQuestion=
						allUserCategoriesExceptGlobalsQuestionFromDB.getQuestionCopy();
					if (allUserCategoriesExceptGlobalsQuestionFromDB.getCreatedBy()!=null)
					{
						User allUserCategoriesExceptGlobalsQuestionAuthor=
							allUserCategoriesExceptGlobalsQuestionFromDB.getCreatedBy().getUserCopy();
						
						// Password is set to empty string before returning instance for security reasons
						allUserCategoriesExceptGlobalsQuestionAuthor.setPassword("");
						
						allUserCategoriesExceptGlobalsQuestion.setCreatedBy(
							allUserCategoriesExceptGlobalsQuestionAuthor);
					}
					if (allUserCategoriesExceptGlobalsQuestionFromDB.getModifiedBy()!=null)
					{
						User allUserCategoriesExceptGlobalsQuestionLastEditor=
							allUserCategoriesExceptGlobalsQuestionFromDB.getModifiedBy().getUserCopy();
						
						// Password is set to empty string before returning instance for security reasons
						allUserCategoriesExceptGlobalsQuestionLastEditor.setPassword("");
						
						allUserCategoriesExceptGlobalsQuestion.setModifiedBy(
							allUserCategoriesExceptGlobalsQuestionLastEditor);
					}
					if (allUserCategoriesExceptGlobalsQuestionFromDB.getCategory()!=null)
					{
						Category categoryFromDB=allUserCategoriesExceptGlobalsQuestionFromDB.getCategory();
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
						allUserCategoriesExceptGlobalsQuestion.setCategory(category);
					}
					allUserCategoriesExceptGlobalsQuestions.add(allUserCategoriesExceptGlobalsQuestion);
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
		return allUserCategoriesExceptGlobalsQuestions; 
	}
	
	/**
	 * @param owner Owner of categories from which we get the questions
	 * @param user User or null to get questions from all users
	 * @param questionType Filtering question type or empty string to get questions of all types
	 * @param questionLevel Filtering question level or empty string to get questions of all levels
	 * @return List of questions from all categories of an user (except global categories) filtered by user, 
	 * question type and question level
	 * @throws ServiceException
	 */
	public List<Question> getAllUserCategoriesExceptGlobalsQuestions(User owner,User user,String questionType,
		String questionLevel) throws ServiceException 
	{
		return getAllUserCategoriesExceptGlobalsQuestions((Operation)null,owner,user,questionType,questionLevel);
	}
	
	/**
	 * @param operation Operation
	 * @param owner Owner of categories from which we get the questions
	 * @param user User or null to get questions from all users
	 * @param questionType Filtering question type or empty string to get questions of all types
	 * @param questionLevel Filtering question level or empty string to get questions of all levels
	 * @return List of questions from all categories of an user (except global categories) filtered by user, 
	 * question type and question level
	 * @throws ServiceException
	 */
	public List<Question> getAllUserCategoriesExceptGlobalsQuestions(Operation operation,User owner,User user,
		String questionType,String questionLevel) throws ServiceException
	{
		boolean singleOp=operation==null;
		List<Question> allUserCategoriesExceptGlobalsQuestions=null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			allUserCategoriesExceptGlobalsQuestions=getAllUserCategoriesExceptGlobalsQuestions(
				operation,getCurrentUser(operation),owner,user,questionType,questionLevel);
		}
		finally
		{
			if (singleOp)
			{
				// End Hibernate operation
				HibernateUtil.endOperation(operation);
			}
		}
		return allUserCategoriesExceptGlobalsQuestions;
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
			
			User currentUser=getCurrentUser(operation);
			allMyCategoriesExceptGlobalsQuestions=getAllUserCategoriesExceptGlobalsQuestions(
				operation,currentUser,currentUser,user,questionType,questionLevel);
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
	 * @param operation Operation
	 * @param viewer User used to check visibility
	 * @param user User or null to get questions from all users
	 * @param questionType Filtering question type or empty string to get questions of all types
	 * @param questionLevel Filtering question level or empty string to get questions of all levels
	 * @return List of questions from all global categories filtered by user, question type and question level
	 * @throws ServiceException
	 */
	private List<Question> getAllGlobalCategoriesQuestions(Operation operation,User viewer,User user,
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
			
			// Check viewer permissions to execute this query
			if (permissionsService.isGranted(operation,viewer,"PERMISSION_QUESTIONS_GLOBAL_FILTER_ENABLED"))
			{
				List<Long> allGlobalCategoriesIds=categoriesService.getAllGlobalCategoriesIds(
					operation,categoryTypesService.getCategoryType(operation,"CATEGORY_TYPE_QUESTIONS"));
				
				// We get questions from DB
				QUESTIONS_DAO.setOperation(operation);
				List<Question> allGlobalCategoriesQuestionsFromDB=QUESTIONS_DAO.getQuestions(
					user==null?0L:user.getId(),allGlobalCategoriesIds,questionType,questionLevel,true,true);
				
				// We return new referenced questions from all global categories within a new list 
				// to avoid shared collection references and object references to unsaved transient instances
				allGlobalCategoriesQuestions=new ArrayList<Question>(allGlobalCategoriesQuestionsFromDB.size());
				for (Question allGlobalCategoriesQuestionFromDB:allGlobalCategoriesQuestionsFromDB)
				{
					Question allGlobalCategoriesQuestion=allGlobalCategoriesQuestionFromDB.getQuestionCopy();
					if (allGlobalCategoriesQuestionFromDB.getCreatedBy()!=null)
					{
						User allGlobalCategoriesQuestionAuthor=
							allGlobalCategoriesQuestionFromDB.getCreatedBy().getUserCopy();
						
						// Password is set to empty string before returning instance for security reasons
						allGlobalCategoriesQuestionAuthor.setPassword("");
						
						allGlobalCategoriesQuestion.setCreatedBy(allGlobalCategoriesQuestionAuthor);
					}
					if (allGlobalCategoriesQuestionFromDB.getModifiedBy()!=null)
					{
						User allGlobalCategoriesQuestionLastEditor=
							allGlobalCategoriesQuestionFromDB.getModifiedBy().getUserCopy();
						
						// Password is set to empty string before returning instance for security reasons
						allGlobalCategoriesQuestionLastEditor.setPassword("");
						
						allGlobalCategoriesQuestion.setModifiedBy(allGlobalCategoriesQuestionLastEditor);
					}
					if (allGlobalCategoriesQuestionFromDB.getCategory()!=null)
					{
						Category categoryFromDB=allGlobalCategoriesQuestionFromDB.getCategory();
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
						allGlobalCategoriesQuestion.setCategory(category);
					}
					allGlobalCategoriesQuestions.add(allGlobalCategoriesQuestion);
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
		return allGlobalCategoriesQuestions;
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
	 * @param operation Operation
	 * @param viewer User used to check visibility
	 * @param user User or null to get questions from all users
	 * @param questionType Filtering question type or empty string to get questions of all types
	 * @param questionLevel Filtering question level or empty string to get questions of all levels
	 * @return List of questions of all public categories of all users except the current one filtered 
	 * by user, question type and question level
	 * @throws ServiceException
	 */
	private List<Question> getAllPublicCategoriesOfOtherUsersQuestions(Operation operation,User viewer,User user,
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
			
			// Check viewer permissions to execute this query
			if (permissionsService.isGranted(operation,viewer,"PERMISSION_QUESTIONS_OTHER_USERS_FILTER_ENABLED"))
			{
				List<Long> allPublicCategoriesOfOtherUsersIds=
					categoriesService.getAllPublicCategoriesOfOtherUsersIds(operation,viewer,
					categoryTypesService.getCategoryType(operation,"CATEGORY_TYPE_QUESTIONS"));
				
				// We get questions from DB
				QUESTIONS_DAO.setOperation(operation);
				List<Question> allPublicCategoriesOfOtherUsersQuestionsFromDB=QUESTIONS_DAO.getQuestions(
					user==null?0L:user.getId(),allPublicCategoriesOfOtherUsersIds,questionType,questionLevel,true,
					true);
				
				// We return new referenced questions of all public categories of all users except the current one 
				// within a new list to avoid shared collection references and object references 
				// to unsaved transient instances
				allPublicCategoriesOfOtherUsersQuestions=
					new ArrayList<Question>(allPublicCategoriesOfOtherUsersQuestionsFromDB.size());
				for (Question allPublicCategoriesOfOtherUsersQuestionFromDB:
					allPublicCategoriesOfOtherUsersQuestionsFromDB)
				{
					Question allPublicCategoriesOfOtherUsersQuestion=
						allPublicCategoriesOfOtherUsersQuestionFromDB.getQuestionCopy();
					if (allPublicCategoriesOfOtherUsersQuestionFromDB.getCreatedBy()!=null)
					{
						User allPublicCategoriesOfOtherUsersQuestionAuthor=
							allPublicCategoriesOfOtherUsersQuestionFromDB.getCreatedBy().getUserCopy();
						
						// Password is set to empty string before returning instance for security reasons
						allPublicCategoriesOfOtherUsersQuestionAuthor.setPassword("");
						
						allPublicCategoriesOfOtherUsersQuestion.setCreatedBy(
							allPublicCategoriesOfOtherUsersQuestionAuthor);
					}
					if (allPublicCategoriesOfOtherUsersQuestionFromDB.getModifiedBy()!=null)
					{
						User allPublicCategoriesOfOtherUsersQuestionLastEditor=
							allPublicCategoriesOfOtherUsersQuestionFromDB.getModifiedBy().getUserCopy();
						
						// Password is set to empty string before returning instance for security reasons
						allPublicCategoriesOfOtherUsersQuestionLastEditor.setPassword("");
						
						allPublicCategoriesOfOtherUsersQuestion.setModifiedBy(
							allPublicCategoriesOfOtherUsersQuestionLastEditor);
					}
					if (allPublicCategoriesOfOtherUsersQuestionFromDB.getCategory()!=null)
					{
						Category categoryFromDB=allPublicCategoriesOfOtherUsersQuestionFromDB.getCategory();
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
						allPublicCategoriesOfOtherUsersQuestion.setCategory(category);
					}
					allPublicCategoriesOfOtherUsersQuestions.add(allPublicCategoriesOfOtherUsersQuestion);
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
		return allPublicCategoriesOfOtherUsersQuestions; 
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
	 * @param operation Operation
	 * @param viewer User used to check visibility
	 * @param user User or null to get questions from all users
	 * @param questionType Filtering question type or empty string to get questions of all types
	 * @param questionLevel Filtering question level or empty string to get questions of all levels
	 * @return List questions from all private categories of all users except the current one filtered 
	 * by user, question type and question level
	 * @throws ServiceException
	 */
	private List<Question> getAllPrivateCategoriesOfOtherUsersQuestions(Operation operation,User viewer,User user,
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
			
			// Check viewer permissions to execute this query
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
				
				// We get questions from DB
				QUESTIONS_DAO.setOperation(operation);
				List<Question> allPrivateCategoriesOfOtherUsersQuestionsFromDB=QUESTIONS_DAO.getQuestions(
					user==null?0L:user.getId(),allPrivateCategoriesOfOtherUsersIds,questionType,questionLevel,true,
					true);
				
				// We return new referenced questions from all private categories of all users 
				// except the current one within a new list to avoid shared collection references 
				// and object references to unsaved transient instances
				allPrivateCategoriesOfOtherUsersQuestions=
					new ArrayList<Question>(allPrivateCategoriesOfOtherUsersQuestionsFromDB.size());
				for (Question allPrivateCategoriesOfOtherUsersQuestionFromDB:
					allPrivateCategoriesOfOtherUsersQuestionsFromDB)
				{
					Question allPrivateCategoriesOfOtherUsersQuestion=
						allPrivateCategoriesOfOtherUsersQuestionFromDB.getQuestionCopy();
					if (allPrivateCategoriesOfOtherUsersQuestionFromDB.getCreatedBy()!=null)
					{
						User allPrivateCategoriesOfOtherUsersQuestionAuthor=
							allPrivateCategoriesOfOtherUsersQuestionFromDB.getCreatedBy().getUserCopy();
						
						// Password is set to empty string before returning instance for security reasons
						allPrivateCategoriesOfOtherUsersQuestionAuthor.setPassword("");
						
						allPrivateCategoriesOfOtherUsersQuestion.setCreatedBy(
							allPrivateCategoriesOfOtherUsersQuestionAuthor);
					}
					if (allPrivateCategoriesOfOtherUsersQuestionFromDB.getModifiedBy()!=null)
					{
						User allPrivateCategoriesOfOtherUsersQuestionLastEditor=
							allPrivateCategoriesOfOtherUsersQuestionFromDB.getModifiedBy().getUserCopy();
						
						// Password is set to empty string before returning instance for security reasons
						allPrivateCategoriesOfOtherUsersQuestionLastEditor.setPassword("");
						
						allPrivateCategoriesOfOtherUsersQuestion.setModifiedBy(
							allPrivateCategoriesOfOtherUsersQuestionLastEditor);
					}
					if (allPrivateCategoriesOfOtherUsersQuestionFromDB.getCategory()!=null)
					{
						Category categoryFromDB=allPrivateCategoriesOfOtherUsersQuestionFromDB.getCategory();
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
						allPrivateCategoriesOfOtherUsersQuestion.setCategory(category);
					}
					allPrivateCategoriesOfOtherUsersQuestions.add(allPrivateCategoriesOfOtherUsersQuestion);
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
		return allPrivateCategoriesOfOtherUsersQuestions; 
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
	 * @param operation Operation
	 * @param viewer User used to check visibility
	 * @param user User or null to get questions from all users
	 * @param questionType Filtering question type or empty string to get questions of all types
	 * @param questionLevel Filtering question level or empty string to get questions of all levels
	 * @return List questions from all categories of all users except the current one filtered by user, 
	 * question type and question level
	 * @throws ServiceException
	 */
	private List<Question> getAllCategoriesOfOtherUsersQuestions(Operation operation,User viewer,User user,
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
			
			// Check viewer permissions to execute this query
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
				
				// We get questions from DB
				QUESTIONS_DAO.setOperation(operation);
				List<Question> allCategoriesOfOtherUsersQuestionsFromDB=QUESTIONS_DAO.getQuestions(
					user==null?0L:user.getId(),allCategoriesOfOtherUsersIds,questionType,questionLevel,true,true);
				
				// We return new referenced questions from all categories of all users except the current one 
				// within a new list to avoid shared collection references and object references 
				// to unsaved transient instances
				allCategoriesOfOtherUsersQuestions=
					new ArrayList<Question>(allCategoriesOfOtherUsersQuestionsFromDB.size());
				for (Question allCategoriesOfOtherUsersQuestionFromDB:allCategoriesOfOtherUsersQuestionsFromDB)
				{
					Question allCategoriesOfOtherUsersQuestion=
						allCategoriesOfOtherUsersQuestionFromDB.getQuestionCopy();
					if (allCategoriesOfOtherUsersQuestionFromDB.getCreatedBy()!=null)
					{
						User allCategoriesOfOtherUsersQuestionAuthor=
							allCategoriesOfOtherUsersQuestionFromDB.getCreatedBy().getUserCopy();
						
						// Password is set to empty string before returning instance for security reasons
						allCategoriesOfOtherUsersQuestionAuthor.setPassword("");
						
						allCategoriesOfOtherUsersQuestion.setCreatedBy(allCategoriesOfOtherUsersQuestionAuthor);
					}
					if (allCategoriesOfOtherUsersQuestionFromDB.getModifiedBy()!=null)
					{
						User allCategoriesOfOtherUsersQuestionLastEditor=
							allCategoriesOfOtherUsersQuestionFromDB.getModifiedBy().getUserCopy();
						
						// Password is set to empty string before returning instance for security reasons
						allCategoriesOfOtherUsersQuestionLastEditor.setPassword("");
						
						allCategoriesOfOtherUsersQuestion.setModifiedBy(
							allCategoriesOfOtherUsersQuestionLastEditor);
					}
					if (allCategoriesOfOtherUsersQuestionFromDB.getCategory()!=null)
					{
						Category categoryFromDB=allCategoriesOfOtherUsersQuestionFromDB.getCategory();
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
						allCategoriesOfOtherUsersQuestion.setCategory(category);
					}
					allCategoriesOfOtherUsersQuestions.add(allCategoriesOfOtherUsersQuestion);
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
		return allCategoriesOfOtherUsersQuestions; 
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
	 * @param user User or null to get total number of questions
	 * @return Number of questions filtered by user (or total)
	 * @throws ServiceException
	 */
	public int getQuestionsCount(User user) throws ServiceException
	{
		return getQuestionsCount(null,user,0L);
	}
	
	/**
	 * @param operation Operation
	 * @param user User or null to get total number of questions
	 * @return Number of questions filtered by user (or total)
	 * @throws ServiceException
	 */
	public int getQuestionsCount(Operation operation,User user) throws ServiceException
	{
		return getQuestionsCount(operation,user,0L);
	}
	
    /**
	 * @param user User or null to get number of questions without filtering by user
	 * @param categoryId Category identifier or 0L to get number of questions without filtering by category
	 * @return Number of questions optionally filtered by user and category
	 * @throws ServiceException
	 */
	public int getQuestionsCount(User user,long categoryId) throws ServiceException
	{
		return getQuestionsCount(null,user,categoryId);
	}
	
	/**
	 * @param operation Operation
	 * @param user User or null to get number of questions without filtering by user
	 * @param categoryId Category identifier or 0L to get number of questions without filtering by category
	 * @return Number of questions optionally filtered by user and category
	 * @throws ServiceException
	 */
	public int getQuestionsCount(Operation operation,User user,long categoryId) throws ServiceException
	{
		int questionsCount=0;
		try
		{
			QUESTIONS_DAO.setOperation(operation);
			questionsCount=
				QUESTIONS_DAO.getQuestions(user==null?0L:user.getId(),categoryId,"","",false,false).size();
		}
		catch (DaoException de)
		{
			throw new ServiceException(de.getMessage(),de);
		}
		return questionsCount;
	}
	
	/**
	 * Checks if exists a question with the indicated identifier.
	 * @param id Identifier
	 * @return true if exists a question with the indicated identifier, false otherwise
	 * @throws ServiceException
	 */
	public boolean checkQuestionId(long id) throws ServiceException
	{
		return checkQuestionId(null,id);
	}
	
	/**
	 * Checks if exists a question with the indicated identifier.
	 * @param operation Operation
	 * @param id Identifier
	 * @return true if exists a question with the indicated identifier, false otherwise
	 * @throws ServiceException
	 */
	public boolean checkQuestionId(Operation operation,long id) throws ServiceException
	{
		boolean questionFound=false;
		try
		{
			QUESTIONS_DAO.setOperation(operation);
			questionFound=QUESTIONS_DAO.checkQuestionId(id);
		}
		catch (DaoException de)
		{
			throw new ServiceException(de.getMessage(),de);
		}
		return questionFound;
	}
	
	/**
	 * @param id Identifier
	 * @return Last time the question has been modified
	 * @throws ServiceException
	 */
	public Date getTimeModifiedFromQuestionId(long id) throws ServiceException
	{
		return getTimeModifiedFromQuestionId(null,id);
	}
	
	/**
	 * @param operation Operation
	 * @param id Identifier
	 * @return Last time the question has been modified
	 * @throws ServiceException
	 */
	public Date getTimeModifiedFromQuestionId(Operation operation,long id) throws ServiceException
	{
		Date timeModified=null;
		try
		{
			QUESTIONS_DAO.setOperation(operation);
			timeModified=QUESTIONS_DAO.getTimeModifiedFromQuestionId(id);
		}
		catch (DaoException de)
		{
			throw new ServiceException(de.getMessage(),de);
		}
		return timeModified;
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
			try
			{
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
				QUESTIONS_DAO.getQuestions(0L,allUserGlobalCategoriesIds,questionType,questionLevel,false,false);
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
			// Get question from DB
			QUESTIONS_DAO.setOperation(operation);
			Question questionFromDB=QUESTIONS_DAO.getQuestion(id,true,true,true,true,true);
			if (questionFromDB!=null)
			{
				question=questionFromDB.getQuestionCopy();
				if (questionFromDB.getCreatedBy()!=null)
				{
					User questionAuthor=questionFromDB.getCreatedBy().getUserCopy();
					
					// Password is set to empty string before returning instance for security reasons
					questionAuthor.setPassword("");
					
					question.setCreatedBy(questionAuthor);
				}
				if (questionFromDB.getModifiedBy()!=null)
				{
					User questionLastEditor=questionFromDB.getModifiedBy().getUserCopy();
					
					// Password is set to empty string before returning instance for security reasons
					questionLastEditor.setPassword("");
					
					question.setModifiedBy(questionLastEditor);
				}
				if (questionFromDB.getCategory()!=null)
				{
					Category categoryFromDB=questionFromDB.getCategory();
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
					question.setCategory(category);
				}
				if (questionFromDB.getAnswers()!=null)
				{
					boolean foundNonEmptyDragDropAnswers=false;
					List<Answer> answers=new ArrayList<Answer>(questionFromDB.getAnswers().size());
					for (Answer answerFromDB:questionFromDB.getAnswers())
					{
						Answer answer=answerFromDB.getAnswerCopy();
						answer.setQuestion(question);
						if (answerFromDB.getResource()!=null)
						{
							Resource answerResourceFromDB=answerFromDB.getResource();
							Resource answerResource=answerResourceFromDB.getResourceCopy();
							if (answerResourceFromDB.getUser()!=null)
							{
								User answerResourceUser=answerResourceFromDB.getUser().getUserCopy();
								
								// Password is set to empty string before returning instance for security reasons
								answerResourceUser.setPassword("");
								
								answerResource.setUser(answerResourceUser);
							}
							if (answerResourceFromDB.getCategory()!=null)
							{
								Category answerResourceCategoryFromDB=answerResourceFromDB.getCategory();
								Category answerResourceCategory=answerResourceCategoryFromDB.getCategoryCopy();
								if (answerResourceCategoryFromDB.getUser()!=null)
								{
									User answerResourceCategoryUser=
										answerResourceCategoryFromDB.getUser().getUserCopy();
									
									// Password is set to empty string before returning instance 
									// for security reasons
									answerResourceCategoryUser.setPassword("");
									
									answerResourceCategory.setUser(answerResourceCategoryUser);
								}
								if (answerResourceCategoryFromDB.getCategoryType()!=null)
								{
									answerResourceCategory.setCategoryType(
										answerResourceCategoryFromDB.getCategoryType().getCategoryTypeCopy());
								}
								if (answerResourceCategoryFromDB.getVisibility()!=null)
								{
									answerResourceCategory.setVisibility(
										answerResourceCategoryFromDB.getVisibility().getVisibilityCopy());
								}
								answerResource.setCategory(answerResourceCategory);
							}
							answer.setResource(answerResource);
						}
						if (answerFromDB instanceof DragDropAnswer && 
							((DragDropAnswer)answerFromDB).getRightAnswer()!=null)
						{
							foundNonEmptyDragDropAnswers=true;
							Answer rightAnswerFilter=new DragDropAnswer();
							rightAnswerFilter.setId(((DragDropAnswer)answerFromDB).getRightAnswer().getId());
							((DragDropAnswer)answer).setRightAnswer(rightAnswerFilter);
						}
						answers.add(answer);
					}
					if (foundNonEmptyDragDropAnswers)
					{
						for (Answer answer:answers)
						{
							if (answer instanceof DragDropAnswer)
							{
								DragDropAnswer dragDropAnswer=(DragDropAnswer)answer;
								if (dragDropAnswer.getRightAnswer()!=null)
								{
									dragDropAnswer.setRightAnswer(
										answers.get(answers.indexOf(dragDropAnswer.getRightAnswer())));
								}
							}
						}
					}
					//TODO ¿para que se ordenan las respuestas? ¿es conveniente dejar esto asi?
					Collections.sort(answers,new AnswerComparator());
					question.setAnswers(answers);
				}
				if (questionFromDB.getFeedbacks()!=null)
				{
					List<Feedback> feedbacks=new ArrayList<Feedback>(questionFromDB.getFeedbacks().size());
					for (Feedback feedbackFromDB:questionFromDB.getFeedbacks())
					{
						Feedback feedback=feedbackFromDB.getFeedbackCopy();
						if (feedbackFromDB.getFeedbackType()!=null)
						{
							feedback.setFeedbackType(feedbackFromDB.getFeedbackType().getFeedbackTypeCopy());
						}
						if (feedbackFromDB.getResource()!=null)
						{
							Resource feedbackResourceFromDB=feedbackFromDB.getResource();
							Resource feedbackResource=feedbackResourceFromDB.getResourceCopy();
							if (feedbackResourceFromDB.getUser()!=null)
							{
								feedbackResource.setUser(feedbackResourceFromDB.getUser().getUserCopy());
							}
							if (feedbackResourceFromDB.getCategory()!=null)
							{
								Category feedbackResourceCategoryFromDB=feedbackResourceFromDB.getCategory();
								Category feedbackResourceCategory=feedbackResourceCategoryFromDB.getCategoryCopy();
								if (feedbackResourceCategoryFromDB.getUser()!=null)
								{
									User feedbackResourceCategoryUser=
										feedbackResourceCategoryFromDB.getUser().getUserCopy();
									
									// Password is set to empty string before returning instance for security reasons
									feedbackResourceCategoryUser.setPassword("");
									
									feedbackResourceCategory.setUser(feedbackResourceCategoryUser);
								}
								if (feedbackResourceCategoryFromDB.getCategoryType()!=null)
								{
									feedbackResourceCategory.setCategoryType(
										feedbackResourceCategoryFromDB.getCategoryType().getCategoryTypeCopy());
								}
								if (feedbackResourceCategoryFromDB.getVisibility()!=null)
								{
									feedbackResourceCategory.setVisibility(
										feedbackResourceCategoryFromDB.getVisibility().getVisibilityCopy());
								}
								feedbackResource.setCategory(feedbackResourceCategory);
							}
							feedback.setResource(feedbackResource);
						}
						feedbacks.add(feedback);
					}
					question.setFeedbacks(feedbacks);
				}
				if (questionFromDB.getResource()!=null)
				{
					Resource resourceFromDB=questionFromDB.getResource();
					Resource resource=resourceFromDB.getResourceCopy();
					if (resourceFromDB.getUser()!=null)
					{
						resource.setUser(resourceFromDB.getUser().getUserCopy());
					}
					if (resourceFromDB.getCategory()!=null)
					{
						Category resourceCategoryFromDB=resourceFromDB.getCategory();
						Category resourceCategory=resourceCategoryFromDB.getCategoryCopy();
						if (resourceCategoryFromDB.getUser()!=null)
						{
							User resourceCategoryUser=resourceCategoryFromDB.getUser().getUserCopy();
							
							// Password is set to empty string before returning instance for security reasons
							resourceCategoryUser.setPassword("");
							
							resourceCategory.setUser(resourceCategoryUser);
						}
						if (resourceCategoryFromDB.getCategoryType()!=null)
						{
							resourceCategory.setCategoryType(
								resourceCategoryFromDB.getCategoryType().getCategoryTypeCopy());
						}
						if (resourceCategoryFromDB.getVisibility()!=null)
						{
							resourceCategory.setVisibility(
								resourceCategoryFromDB.getVisibility().getVisibilityCopy());
						}
						resource.setCategory(resourceCategory);
					}
					question.setResource(resource);
				}
				if (questionFromDB.getCorrectFeedbackResource()!=null)
				{
					Resource correctFeedbackResourceFromDB=questionFromDB.getCorrectFeedbackResource();
					Resource correctFeedbackResource=correctFeedbackResourceFromDB.getResourceCopy();
					if (correctFeedbackResourceFromDB.getUser()!=null)
					{
						correctFeedbackResource.setUser(correctFeedbackResourceFromDB.getUser().getUserCopy());
					}
					if (correctFeedbackResourceFromDB.getCategory()!=null)
					{
						Category correctFeedbackResourceCategoryFromDB=correctFeedbackResourceFromDB.getCategory();
						Category correctFeedbackResourceCategory=
							correctFeedbackResourceCategoryFromDB.getCategoryCopy();
						if (correctFeedbackResourceCategoryFromDB.getUser()!=null)
						{
							User correctFeedbackResourceCategoryUser=
								correctFeedbackResourceCategoryFromDB.getUser().getUserCopy();
							
							// Password is set to empty string before returning instance for security reasons
							correctFeedbackResourceCategoryUser.setPassword("");
							
							correctFeedbackResourceCategory.setUser(correctFeedbackResourceCategoryUser);
						}
						if (correctFeedbackResourceCategoryFromDB.getCategoryType()!=null)
						{
							correctFeedbackResourceCategory.setCategoryType(
								correctFeedbackResourceCategoryFromDB.getCategoryType().getCategoryTypeCopy());
						}
						if (correctFeedbackResourceCategoryFromDB.getVisibility()!=null)
						{
							correctFeedbackResourceCategory.setVisibility(
								correctFeedbackResourceCategoryFromDB.getVisibility().getVisibilityCopy());
						}
						correctFeedbackResource.setCategory(correctFeedbackResourceCategory);
					}
					question.setCorrectFeedbackResource(correctFeedbackResource);
				}
				if (questionFromDB.getIncorrectFeedbackResource()!=null)
				{
					Resource incorrectFeedbackResourceFromDB=questionFromDB.getIncorrectFeedbackResource();
					Resource incorrectFeedbackResource=incorrectFeedbackResourceFromDB.getResourceCopy();
					if (incorrectFeedbackResourceFromDB.getUser()!=null)
					{
						incorrectFeedbackResource.setUser(incorrectFeedbackResourceFromDB.getUser().getUserCopy());
					}
					if (incorrectFeedbackResourceFromDB.getCategory()!=null)
					{
						Category incorrectFeedbackResourceCategoryFromDB=
							incorrectFeedbackResourceFromDB.getCategory();
						Category incorrectFeedbackResourceCategory=
							incorrectFeedbackResourceCategoryFromDB.getCategoryCopy();
						if (incorrectFeedbackResourceCategoryFromDB.getUser()!=null)
						{
							User incorrectFeedbackResourceCategoryUser=
								incorrectFeedbackResourceCategoryFromDB.getUser().getUserCopy();
							
							// Password is set to empty string before returning instance for security reasons
							incorrectFeedbackResourceCategoryUser.setPassword("");
							
							incorrectFeedbackResourceCategory.setUser(incorrectFeedbackResourceCategoryUser);
						}
						if (incorrectFeedbackResourceCategoryFromDB.getCategoryType()!=null)
						{
							incorrectFeedbackResourceCategory.setCategoryType(
								incorrectFeedbackResourceCategoryFromDB.getCategoryType().getCategoryTypeCopy());
						}
						if (incorrectFeedbackResourceCategoryFromDB.getVisibility()!=null)
						{
							incorrectFeedbackResourceCategory.setVisibility(
								incorrectFeedbackResourceCategoryFromDB.getVisibility().getVisibilityCopy());
						}
						incorrectFeedbackResource.setCategory(incorrectFeedbackResourceCategory);
					}
					question.setIncorrectFeedbackResource(incorrectFeedbackResource);
				}
				if (questionFromDB.getPassFeedbackResource()!=null)
				{
					Resource passFeedbackResourceFromDB=questionFromDB.getPassFeedbackResource();
					Resource passFeedbackResource=passFeedbackResourceFromDB.getResourceCopy();
					if (passFeedbackResourceFromDB.getUser()!=null)
					{
						passFeedbackResource.setUser(passFeedbackResourceFromDB.getUser().getUserCopy());
					}
					if (passFeedbackResourceFromDB.getCategory()!=null)
					{
						Category passFeedbackResourceCategoryFromDB=passFeedbackResourceFromDB.getCategory();
						Category passFeedbackResourceCategory=passFeedbackResourceCategoryFromDB.getCategoryCopy();
						if (passFeedbackResourceCategoryFromDB.getUser()!=null)
						{
							User passFeedbackResourceCategoryUser=
								passFeedbackResourceCategoryFromDB.getUser().getUserCopy();
							
							// Password is set to empty string before returning instance for security reasons
							passFeedbackResourceCategoryUser.setPassword("");
							
							passFeedbackResourceCategory.setUser(passFeedbackResourceCategoryUser);
						}
						if (passFeedbackResourceCategoryFromDB.getCategoryType()!=null)
						{
							passFeedbackResourceCategory.setCategoryType(
								passFeedbackResourceCategoryFromDB.getCategoryType().getCategoryTypeCopy());
						}
						if (passFeedbackResourceCategoryFromDB.getVisibility()!=null)
						{
							passFeedbackResourceCategory.setVisibility(
								passFeedbackResourceCategoryFromDB.getVisibility().getVisibilityCopy());
						}
						passFeedbackResource.setCategory(passFeedbackResourceCategory);
					}
					question.setPassFeedbackResource(passFeedbackResource);
				}
				if (questionFromDB.getFinalFeedbackResource()!=null)
				{
					Resource finalFeedbackResourceFromDB=questionFromDB.getFinalFeedbackResource();
					Resource finalFeedbackResource=finalFeedbackResourceFromDB.getResourceCopy();
					if (finalFeedbackResourceFromDB.getUser()!=null)
					{
						finalFeedbackResource.setUser(finalFeedbackResourceFromDB.getUser().getUserCopy());
					}
					if (finalFeedbackResourceFromDB.getCategory()!=null)
					{
						Category finalFeedbackResourceCategoryFromDB=finalFeedbackResourceFromDB.getCategory();
						Category finalFeedbackResourceCategory=
							finalFeedbackResourceCategoryFromDB.getCategoryCopy();
						if (finalFeedbackResourceCategoryFromDB.getUser()!=null)
						{
							User finalFeedbackResourceCategoryUser=
								finalFeedbackResourceCategoryFromDB.getUser().getUserCopy();
							
							// Password is set to empty string before returning instance for security reasons
							finalFeedbackResourceCategoryUser.setPassword("");
							
							finalFeedbackResourceCategory.setUser(finalFeedbackResourceCategoryUser);
						}
						if (finalFeedbackResourceCategoryFromDB.getCategoryType()!=null)
						{
							finalFeedbackResourceCategory.setCategoryType(
								finalFeedbackResourceCategoryFromDB.getCategoryType().getCategoryTypeCopy());
						}
						if (finalFeedbackResourceCategoryFromDB.getVisibility()!=null)
						{
							finalFeedbackResourceCategory.setVisibility(
								finalFeedbackResourceCategoryFromDB.getVisibility().getVisibilityCopy());
						}
						finalFeedbackResource.setCategory(finalFeedbackResourceCategory);
					}
					question.setFinalFeedbackResource(finalFeedbackResource);
				}
				if (questionFromDB.getQuestionResources()!=null)
				{
					List<QuestionResource> questionResources=
						new ArrayList<QuestionResource>(questionFromDB.getQuestionResources().size());
					for (QuestionResource questionResourceFromDB:questionFromDB.getQuestionResources())
					{
						QuestionResource questionResource=questionResourceFromDB.getQuestionResourceCopy();
						if (questionResourceFromDB.getResource()!=null)
						{
							Resource questionResourceResourceFromDB=questionResourceFromDB.getResource();
							Resource questionResourceResource=questionResourceResourceFromDB.getResourceCopy();
							if (questionResourceResourceFromDB.getUser()!=null)
							{
								questionResourceResource.setUser(
									questionResourceResourceFromDB.getUser().getUserCopy());
							}
							if (questionResourceResourceFromDB.getCategory()!=null)
							{
								Category questionResourceResourceCategoryFromDB=
									questionResourceResourceFromDB.getCategory();
								Category questionResourceResourceCategory=
									questionResourceResourceCategoryFromDB.getCategoryCopy();
								if (questionResourceResourceCategoryFromDB.getUser()!=null)
								{
									User questionResourceResourceCategoryUser=
										questionResourceResourceCategoryFromDB.getUser().getUserCopy();
									
									// Password is set to empty string before returning instance for security reasons
									questionResourceResourceCategoryUser.setPassword("");
									
									questionResourceResourceCategory.setUser(questionResourceResourceCategoryUser);
								}
								if (questionResourceResourceCategoryFromDB.getCategoryType()!=null)
								{
									questionResourceResourceCategory.setCategoryType(
										questionResourceResourceCategoryFromDB.getCategoryType().
										getCategoryTypeCopy());
								}
								if (questionResourceResourceCategoryFromDB.getVisibility()!=null)
								{
									questionResourceResourceCategory.setVisibility(
										questionResourceResourceCategoryFromDB.getVisibility().getVisibilityCopy());
								}
								questionResourceResource.setCategory(questionResourceResourceCategory);
							}
							questionResource.setResource(questionResourceResource);
						}
						questionResources.add(questionResource);
					}
					question.setQuestionResources(questionResources);
				}
			}
		}
		catch (DaoException de)
		{
			throw new ServiceException(de.getMessage(),de);
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
			// Add a new question
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
					answersService.updateAnswer(answer);
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
					questionResourcesService.updateQuestionResource(operation,questionResource);
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
					feedbacksService.updateFeedback(operation,feedback);
				}
				else
				{
					feedbacksService.addFeedback(operation,feedback);
				}
			}
			
			
			// Get question from DB
			QUESTIONS_DAO.setOperation(operation);
			Question questionFromDB=QUESTIONS_DAO.getQuestion(question.getId(),false,false,false,false,false);
			
			// Set fields with the updated values
			questionFromDB.setFromOtherQuestion(question);
			
			// Update question
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
		deleteQuestion(null,question.getId());
	}
	
	/**
	 * Deletes a question.
	 * @param operation Operation
	 * @param question Question to delete
	 * @throws ServiceException
	 */
	public void deleteQuestion(Operation operation,Question question) throws ServiceException
	{
		deleteQuestion(operation,question.getId());
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
			
			// Get question from DB
			QUESTIONS_DAO.setOperation(operation);
			Question questionFromDB=QUESTIONS_DAO.getQuestion(questionId,false,false,false,false,false);
			
			// Delete question
			QUESTIONS_DAO.setOperation(operation);
			QUESTIONS_DAO.deleteQuestion(questionFromDB);
			
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
		UserSessionService userSessionService=(UserSessionService)context.getApplication().getELResolver().getValue(
			context.getELContext(),null,"userSessionService");
		return userSessionService.getCurrentUser(operation);
		
	}
}
