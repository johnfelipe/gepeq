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
package es.uned.lsi.gepec.model.dao;

import java.util.ArrayList;
import java.util.List;

import javax.el.ELContext;
import javax.faces.context.FacesContext;

import es.uned.lsi.gepec.model.entities.Answer;
import es.uned.lsi.gepec.model.entities.Category;
import es.uned.lsi.gepec.model.entities.DragDropAnswer;
import es.uned.lsi.gepec.model.entities.Feedback;
import es.uned.lsi.gepec.model.entities.Question;
import es.uned.lsi.gepec.model.entities.QuestionResource;
import es.uned.lsi.gepec.model.entities.Resource;
import es.uned.lsi.gepec.util.HibernateUtil;
import es.uned.lsi.gepec.util.HibernateUtil.Operation;
import es.uned.lsi.gepec.web.services.LocalizationService;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.type.StandardBasicTypes;

/**
 * Manages access to questions data.
 */
public class QuestionsDao
{
	private Operation operation=null;
	private boolean singleOp;
	
	//Guarda una pregunta en la bd
	/**
	 * Adds a new question to DB. 
	 * @param question Question to add
	 * @return Question identifier
	 * @throws DaoException
	 */
	public long saveQuestion(Question question) throws DaoException
	{
		long id=0L;  
		try
		{
			startOperation();
			id=((Long)operation.session.save(question)).longValue();
			if (singleOp)
			{
				operation.commit();
			}
		}
		catch (HibernateException he)
		{
			handleException(he);
			throw new DaoException(he);
		}
		finally
		{
			endOperation();
		}
		return id;
	}
	
	//Actualiza una pregunta en la bd
	/**
	 * Updates a question on DB.
	 * @param question Question to update
	 * @throws DaoException
	 */
	public void updateQuestion(Question question) throws DaoException
	{
		try
		{
			startOperation();
			operation.session.update(question);
			if (singleOp)
			{
				operation.commit();
			}
		}
		catch (HibernateException he)
		{
			handleException(he);
			throw new DaoException(he);
		}
		finally
		{
			endOperation();
		}
	}
	
	//Elimina una pregunta de la bd
	/**
	 * Deletes a question from DB.
	 * @param question Question to delete
	 * @throws DaoException
	 */
	public void deleteQuestion(Question question) throws DaoException
	{
		try
		{
			startOperation();
			operation.session.delete(question);
			if (singleOp)
			{
				operation.commit();
			}
		}
		catch (HibernateException he)
		{
			handleException(he);
			throw new DaoException(he);
		}
		finally
		{
			endOperation();
		}
	}
	
	//Obtiene una categoría a partir de su id
	/**
	 * @param idQuestion Question identifier
	 * @param includeUsers Flag to indicate if we need to initialize users
	 * @param includeCategory Flag to indicate if we need to initialize categories
	 * @param includeAnswers Flag to indicate if we need to initialize answers
	 * @param includeFeedbacks Flag to indicate if we need to initialize feedbacks
	 * @param includeResources Flag to indicate if we need to initialize resources
	 * @return Question from DB
	 * @throws DaoException
	 */
	public Question getQuestion(long idQuestion,boolean includeUsers,boolean includeCategory,
		boolean includeAnswers,boolean includeFeedbacks,boolean includeResources) throws DaoException
	{
		Question question=null;
		try
		{
			startOperation();
			question=(Question)operation.session.get(Question.class,idQuestion);
			if (question!=null)
			{
				if (includeUsers)
				{
					Hibernate.initialize(question.getCreatedBy());
					Hibernate.initialize(question.getModifiedBy());
				}
				if (includeCategory)
				{
					Category category=question.getCategory();
					Hibernate.initialize(category);
					if (category!=null)
					{
						if (includeUsers)
						{
							Hibernate.initialize(category.getUser());
						}
						Hibernate.initialize(category.getCategoryType());
						Hibernate.initialize(category.getVisibility());
					}
				}
				if (includeAnswers)
				{
					Hibernate.initialize(question.getAnswers());
					if (includeResources)
					{
						for (Answer answer:question.getAnswers())
						{
							Hibernate.initialize(answer.getResource());
							if (answer.getResource()!=null)
							{
								if (includeUsers)
								{
									Hibernate.initialize(answer.getResource().getUser());
								}
								if (includeCategory)
								{
									Hibernate.initialize(answer.getResource().getCategory());
									if (answer.getResource().getCategory()!=null)
									{
										if (includeUsers)
										{
											Hibernate.initialize(answer.getResource().getCategory().getUser());
										}
										Hibernate.initialize(answer.getResource().getCategory().getCategoryType());
										Hibernate.initialize(answer.getResource().getCategory().getVisibility());
									}
								}
							}
							if (answer instanceof DragDropAnswer)
							{
								Hibernate.initialize(((DragDropAnswer)answer).getRightAnswer());  
							}
						}
					}
				}
				if (includeFeedbacks)
				{
					Hibernate.initialize(question.getFeedbacks());
					for (Feedback feedback:question.getFeedbacks())
					{
						Hibernate.initialize(feedback.getFeedbackType());
						if (includeResources)
						{
							Hibernate.initialize(feedback.getResource());
							if (feedback.getResource()!=null)
							{
								if (includeUsers)
								{
									Hibernate.initialize(feedback.getResource().getUser());
								}
								if (includeCategory)
								{
									Hibernate.initialize(feedback.getResource().getCategory());
									if (feedback.getResource().getCategory()!=null)
									{
										if (includeUsers)
										{
											Hibernate.initialize(feedback.getResource().getCategory().getUser());
										}
										Hibernate.initialize(
											feedback.getResource().getCategory().getCategoryType());
										Hibernate.initialize(feedback.getResource().getCategory().getVisibility());
									}
								}
							}
						}
					}
				}
				if (includeResources)
				{
					Hibernate.initialize(question.getResource());
					if (question.getResource()!=null)
					{
						if (includeUsers)
						{
							Hibernate.initialize(question.getResource().getUser());
						}
						if (includeCategory)
						{
							Hibernate.initialize(question.getResource().getCategory());
							if (question.getResource().getCategory()!=null)
							{
								if (includeUsers)
								{
									Hibernate.initialize(question.getResource().getCategory().getUser());
								}
								Hibernate.initialize(question.getResource().getCategory().getCategoryType());
								Hibernate.initialize(question.getResource().getCategory().getVisibility());
							}
						}
					}
					Hibernate.initialize(question.getCorrectFeedbackResource());
					if (question.getCorrectFeedbackResource()!=null)
					{
						if (includeUsers)
						{
							Hibernate.initialize(question.getCorrectFeedbackResource().getUser());
						}
						if (includeCategory)
						{
							Hibernate.initialize(question.getCorrectFeedbackResource().getCategory());
							if (question.getCorrectFeedbackResource().getCategory()!=null)
							{
								if (includeUsers)
								{
									Hibernate.initialize(
										question.getCorrectFeedbackResource().getCategory().getUser());
								}
								Hibernate.initialize(
									question.getCorrectFeedbackResource().getCategory().getCategoryType());
								Hibernate.initialize(
									question.getCorrectFeedbackResource().getCategory().getVisibility());
							}
						}
					}
					Hibernate.initialize(question.getIncorrectFeedbackResource());
					if (question.getIncorrectFeedbackResource()!=null)
					{
						if (includeUsers)
						{
							Hibernate.initialize(question.getIncorrectFeedbackResource().getUser());
						}
						if (includeCategory)
						{
							Hibernate.initialize(question.getIncorrectFeedbackResource().getCategory());
							if (question.getIncorrectFeedbackResource().getCategory()!=null)
							{
								if (includeUsers)
								{
									Hibernate.initialize(
										question.getIncorrectFeedbackResource().getCategory().getUser());
								}
								Hibernate.initialize(
									question.getIncorrectFeedbackResource().getCategory().getCategoryType());
								Hibernate.initialize(
									question.getIncorrectFeedbackResource().getCategory().getVisibility());
							}
						}
					}
					Hibernate.initialize(question.getPassFeedbackResource());
					if (question.getPassFeedbackResource()!=null)
					{
						if (includeUsers)
						{
							Hibernate.initialize(question.getPassFeedbackResource().getUser());
						}
						if (includeCategory)
						{
							Hibernate.initialize(question.getPassFeedbackResource().getCategory());
							if (question.getPassFeedbackResource().getCategory()!=null)
							{
								if (includeUsers)
								{
									Hibernate.initialize(
										question.getPassFeedbackResource().getCategory().getUser());
								}
								Hibernate.initialize(
									question.getPassFeedbackResource().getCategory().getCategoryType());
								Hibernate.initialize(
									question.getPassFeedbackResource().getCategory().getVisibility());
							}
						}
					}
					Hibernate.initialize(question.getFinalFeedbackResource());
					if (question.getFinalFeedbackResource()!=null)
					{
						if (includeUsers)
						{
							Hibernate.initialize(question.getFinalFeedbackResource().getUser());
						}
						if (includeCategory)
						{
							Hibernate.initialize(question.getFinalFeedbackResource().getCategory());
							if (question.getFinalFeedbackResource().getCategory()!=null)
							{
								if (includeUsers)
								{
									Hibernate.initialize(question.getFinalFeedbackResource().getCategory().getUser());
								}
								Hibernate.initialize(
									question.getFinalFeedbackResource().getCategory().getCategoryType());
								Hibernate.initialize(
									question.getFinalFeedbackResource().getCategory().getVisibility());
							}
						}
					}
					Hibernate.initialize(question.getQuestionResources());
					for (QuestionResource questionResource:question.getQuestionResources())
					{
						Resource resource=questionResource.getResource();
						if (resource!=null)
						{
							Hibernate.initialize(resource);
							if (includeUsers)
							{
								Hibernate.initialize(resource.getUser());
							}
							if (includeCategory)
							{
								Hibernate.initialize(resource.getCategory());
								if (resource.getCategory()!=null)
								{
									if (includeUsers)
									{
										Hibernate.initialize(resource.getCategory().getUser());
									}
									Hibernate.initialize(resource.getCategory().getCategoryType());
									Hibernate.initialize(resource.getCategory().getVisibility());
								}
							}
						}
					}
				}
			}
		}
		catch (HibernateException he)
		{
			handleException(he,!singleOp);
			throw new DaoException(he);
		}
		finally
		{
			endOperation();
		}
		return question; 
	}
    
    //Obtiene la lista de preguntas
    /**
     * @param includeUsers true to include users data within questions
     * @param includeCategories true to include categories data within questions
     * @return List of all questions
     * @throws DaoException
     */
	public List<Question> getQuestions(boolean includeUsers,boolean includeCategories) 
		throws DaoException
	{
		return getQuestions(0L,0L,"","",includeUsers,includeCategories);
	}
	
	//Obtiene la lista de preguntas de un usuario
	/**
	 * @param userId User identifier
     * @param includeUsers true to include users data within questions
	 * @param includeCategories true to include categories data within questions
	 * @return List of questions filtered by user
	 * @throws DaoException
	 */
	public List<Question> getQuestions(long userId,boolean includeUsers,boolean includeCategories) 
		throws DaoException
	{
		return getQuestions(userId,0L,"","",includeUsers,includeCategories);
	}
	
	//Obtiene la lista de preguntas de un usuario y una categoría determinados
	/**
	 * @param userId User identifier
	 * @param categoryId Filtering category identifier or 0 to get questions from all categories
     * @param includeUsers true to include users data within questions
	 * @param includeCategories true to include categories data within questions
	 * @return List of questions filtered by user and category
	 * @throws DaoException
	 */
	public List<Question> getQuestions(long userId,long categoryId,boolean includeUsers,
		boolean includeCategories) throws DaoException
	{
		return getQuestions(userId,categoryId,"","",includeUsers,includeCategories);
	}
	
	//Obtiene la lista de preguntas filtradas por usuario, categoría, tipo de pregunta
	//y nivel de dificultad
	/**
     * @param userId User identifier
     * @param categoryId Filtering category identifier or 0 to get questions from all categories
     * @param questionType Filtering question type or empty string to get questions of all types
     * @param questionLevel Filtering  question level or empty string to get questions of all levels
     * @param includeUsers true to include users data within questions
     * @param includeCategories true to include categories data within questions
     * @return List of questions filtered by user, category, question type and question level
     * @throws DaoException
     */
	@SuppressWarnings("unchecked")
	public List<Question> getQuestions(long userId,long categoryId,String questionType,String questionLevel,
		boolean includeUsers,boolean includeCategories) throws DaoException
	{
		List<Question> questions=null;
		boolean addedFilter=false;
		try
		{
			startOperation();
			StringBuffer queryString=new StringBuffer("from Question q");
			if (userId>0L)
			{
				addedFilter=true;
				queryString.append(" Where q.createdBy = :userId");
			}
			if (categoryId>0L)
			{
				if (addedFilter)
				{
					queryString.append(" And q.category = :categoryId");
				}
				else
				{
					addedFilter=true;
					queryString.append(" Where q.category = :categoryId");
				}
			}
			if (questionType!=null && !questionType.equals(""))
			{
				if (addedFilter)
				{
					queryString.append(" And q.type = :questionType");
				}
				else
				{
					addedFilter=true;
					queryString.append(" Where q.type = :questionType");
				}
			}
			if (questionLevel!=null && !questionLevel.equals(""))
			{
				if (addedFilter)
				{
					queryString.append(" And q.levelString = :questionLevel");
				}
				else
				{
					addedFilter=true;
					queryString.append(" Where q.levelString = :questionLevel");
				}
			}
			queryString.append(" Order by q.timemodified desc");
			Query query=operation.session.createQuery(queryString.toString());
			if (userId>0L)
			{
				query.setParameter("userId",Long.valueOf(userId),StandardBasicTypes.LONG);
			}
			if (categoryId>0L)
			{
				query.setParameter("categoryId",Long.valueOf(categoryId),StandardBasicTypes.LONG);
			}
			if (questionType!=null && !questionType.equals(""))
			{
				query.setParameter("questionType",questionType,StandardBasicTypes.STRING);
			}
			if (questionLevel!=null && !questionLevel.equals(""))
			{
				query.setParameter("questionLevel",questionLevel,StandardBasicTypes.STRING);
			}
			questions=query.list();
			if (includeUsers)
			{
				if (includeCategories)
				{
					for (Question question:questions)
					{
						Hibernate.initialize(question.getCreatedBy());
						Hibernate.initialize(question.getModifiedBy());
						Category category=question.getCategory();
						Hibernate.initialize(category);
						if (category!=null)
						{
							Hibernate.initialize(category.getUser());
							Hibernate.initialize(category.getCategoryType());
							Hibernate.initialize(category.getVisibility());
						}
					}
				}
				else
				{
					for (Question question:questions)
					{
						Hibernate.initialize(question.getCreatedBy());
						Hibernate.initialize(question.getModifiedBy());
					}
				}
			}
			else if (includeCategories)
			{
				for (Question question:questions)
				{
					Category category=question.getCategory();
					Hibernate.initialize(category);
					if (category!=null)
					{
						Hibernate.initialize(category.getCategoryType());
						Hibernate.initialize(category.getVisibility());
					}
				}
			}
		}
		catch (HibernateException he)
		{
			handleException(he,!singleOp);
			throw new DaoException(he);
		}
		finally
		{
			endOperation();
		}
		return questions;
	}
	
	/**
	 * @param userId User identifier
     * @param categoriesIds Filtering categories identifiers or null to get questions of all categories
     * @param includeUsers true to include users data within questions
	 * @param includeCategories true to include categories data within questions
	 * @return List of questions filtered by user and categories
	 * @throws DaoException
	 */
	public List<Question> getQuestions(long userId,List<Long> categoriesIds,boolean includeUsers,
		boolean includeCategories) throws DaoException
	{
		return getQuestions(userId,categoriesIds,"","",includeUsers,includeCategories);
	}
	
	/**
     * @param userId User identifier
     * @param categoriesIds Filtering categories identifiers or null to get questions of all categories
     * @param questionType Filtering question type or empty string to get questions of all types
     * @param questionLevel Filtering  question level or empty string to get questions of all levels
     * @param includeUsers true to include users data within questions
     * @param includeCategories true to include categories data within questions
     * @return List of questions filtered by user, categories, question type and question level
     * @throws DaoException
     */
	@SuppressWarnings("unchecked")
	public List<Question> getQuestions(long userId,List<Long> categoriesIds,String questionType,
		String questionLevel,boolean includeUsers,boolean includeCategories) throws DaoException
	{
		List<Question> questions=null;
		if (categoriesIds!=null && categoriesIds.isEmpty())
		{
			questions=new ArrayList<Question>();
		}
		else if (categoriesIds!=null && categoriesIds.size()==1)
		{
			questions=
				getQuestions(userId,categoriesIds.get(0),questionType,questionLevel,includeUsers,includeCategories);
		}
		else
		{
			boolean addedFilter=false;
			try
			{
				startOperation();
				StringBuffer queryString=new StringBuffer("from Question q");
				if (userId>0L)
				{
					addedFilter=true;
					queryString.append(" Where q.createdBy = :userId");
				}
				if (categoriesIds!=null)
				{
					if (addedFilter)
					{
						queryString.append(" And q.category In (:categoriesIds)");
					}
					else
					{
						addedFilter=true;
						queryString.append(" Where q.category In (:categoriesIds)");
					}
				}
				if (questionType!=null && !questionType.equals(""))
				{
					if (addedFilter)
					{
						queryString.append(" And q.type = :questionType");
					}
					else
					{
						addedFilter=true;
						queryString.append(" Where q.type = :questionType");
					}
				}
				if (questionLevel!=null && !questionLevel.equals(""))
				{
					if (addedFilter)
					{
						queryString.append(" And q.levelString = :questionLevel");
					}
					else
					{
						addedFilter=true;
						queryString.append(" Where q.levelString = :questionLevel");
					}
				}
				queryString.append(" Order by q.timemodified desc");
				Query query=operation.session.createQuery(queryString.toString());
				if (userId>0L)
				{
					query.setParameter("userId",userId,StandardBasicTypes.LONG);
				}
				if (categoriesIds!=null)
				{
					query.setParameterList("categoriesIds",categoriesIds,StandardBasicTypes.LONG);
				}
				if (questionType!=null && !questionType.equals(""))
				{
					query.setParameter("questionType",questionType,StandardBasicTypes.STRING);
				}
				if (questionLevel!=null && !questionLevel.equals(""))
				{
					query.setParameter("questionLevel",questionLevel,StandardBasicTypes.STRING);
				}
				questions=query.list();
				if (includeUsers)
				{
					if (includeCategories)
					{
						for (Question question:questions)
						{
							Hibernate.initialize(question.getCreatedBy());
							Hibernate.initialize(question.getModifiedBy());
							Category category=question.getCategory();
							Hibernate.initialize(category);
							if (category!=null)
							{
								Hibernate.initialize(category.getUser());
								Hibernate.initialize(category.getCategoryType());
								Hibernate.initialize(category.getVisibility());
							}
						}
					}
					else
					{
						for(Question question:questions)
						{
							Hibernate.initialize(question.getCreatedBy());
							Hibernate.initialize(question.getModifiedBy());
						}
					}
				}
				else if (includeCategories)
				{
					for (Question question:questions)
					{
						Category category=question.getCategory();
						Hibernate.initialize(category);
						if (category!=null)
						{
							Hibernate.initialize(category.getCategoryType());
							Hibernate.initialize(category.getVisibility());
						}
					}
				}
			}
			catch (HibernateException he)
			{
				handleException(he,!singleOp);
				throw new DaoException(he);
			}
			finally
			{
				endOperation();
			}
		}
		return questions;
	}
	
    //Inicia una sesión e inicia una transacción contra el dbms
	/**
	 * Starts a session and transaction against DBMS if needed.
	 * @throws DaoException
	 */
	private void startOperation() throws DaoException
	{
		try
		{
			if (operation==null)
			{
				operation=HibernateUtil.startOperation();
				singleOp=true;
			}
		}
		catch (HibernateException he)
		{
			operation=null;
			handleException(he,false);
			throw new DaoException(he);
		}
	}
	
	/**
	 * Sets a session and transaction against DBMS.
	 * @param operation Operation with started session and transaction
	 */
	public void setOperation(Operation operation)
	{
		this.operation=operation;
		singleOp=false;
	}
	
	/**
	 * Ends an operation, ending session and transaction against DBMS if this is a single operation.
	 * @throws DaoException
	 */
	private void endOperation() throws DaoException
	{
		try
		{
			if (singleOp)
			{
				HibernateUtil.endOperation(operation);
			}
		}
		catch (HibernateException he)
		{
			handleException(he,false);
			throw new DaoException(he);
		}
		finally
		{
			operation=null;
		}
	}
	
	//Maneja los errores producidos en la operación de acceso a datos
	/**
	 * Manage errors produced while accesing persistent data.<br/><br/>
	 * It also does a rollback.
	 * @param he Exception to handle
	 * @throws DaoException
	 */
	private void handleException(HibernateException he) throws DaoException
	{
		handleException(he,true);
	}
	
	/**
	 * Manage errors produced while accesing persistent data.
	 * @param he Exception to handle
	 * @param doRollback Flag to indicate to do a rollback
	 * @throws DaoException
	 */
	private void handleException(HibernateException he,boolean doRollback) throws DaoException 
	{
		String errorMessage=null;
		FacesContext facesContext=FacesContext.getCurrentInstance();
		if (facesContext==null)
		{
			errorMessage="Access error to the data layer";
		}
		else
		{
			ELContext elContext=facesContext.getELContext();
			LocalizationService localizationService=(LocalizationService)FacesContext.getCurrentInstance().
				getApplication().getELResolver().getValue(elContext,null,"localizationService");
			errorMessage=localizationService.getLocalizedMessage("ERROR_ACCESS_DATA_LAYER");
		}
		if (doRollback)
		{
			operation.rollback();
		}
		throw new DaoException(errorMessage,he); 
	}
}
