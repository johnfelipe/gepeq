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
package es.uned.lsi.gepec.web.backbeans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.el.ELContext;
import javax.faces.context.FacesContext;

import es.uned.lsi.gepec.model.entities.DragDropQuestion;
import es.uned.lsi.gepec.model.entities.Feedback;
import es.uned.lsi.gepec.model.entities.Question;
import es.uned.lsi.gepec.model.entities.Resource;
import es.uned.lsi.gepec.om.OmHelper;
import es.uned.lsi.gepec.util.HibernateUtil.Operation;
import es.uned.lsi.gepec.web.QuestionBean;
import es.uned.lsi.gepec.web.helper.NumberComparator;
import es.uned.lsi.gepec.web.services.LocalizationService;

/**
 * Backbean for a feedback.
 */
@SuppressWarnings("serial")
public class FeedbackBean implements Serializable
{
	public final static String TYPE_NORMAL="FEEDBACK_TYPE_NORMAL";
	public final static String TYPE_FIXED="FEEDBACK_TYPE_FIXED";
	
	public final static List<String> TYPES;
	static
	{
		TYPES=new ArrayList<String>();
		TYPES.add(TYPE_NORMAL);
		TYPES.add(TYPE_FIXED);
	}
	
	private long id;
	private QuestionBean question;
	private Resource resource;
	private String text;
	private int position;
	private String type;
	private int resourceWidth;
	private int resourceHeight;
	
	private List<ConditionBean> conditions=new ArrayList<ConditionBean>();
	
	public FeedbackBean(QuestionBean question)
	{
		this(question,question.getFeedbacks(question.getCurrentUserOperation(null)).size()+1);
	}
	
	public FeedbackBean(QuestionBean question,int position)
	{
		this.id=0;
		this.question=question;
		this.resource=null;
		this.text="";
		this.position=position;
		this.type=TYPE_NORMAL;
		this.resourceWidth=-1;
		this.resourceHeight=-1;
	}
	
	public FeedbackBean(QuestionBean question,Feedback feedback)
	{
		this.question=question;
		setFromFeedback(feedback);
	}
	
	public FeedbackBean(FeedbackBean otherFeedback)
	{
		setFromOtherFeedback(otherFeedback);
	}
	
	private void setFromFeedback(Feedback feedback)
	{
		// Get current user session Hibernate operation
		Operation operation=question.getCurrentUserOperation(null);
		
		id=feedback.getId();
		resource=feedback.getResource();
		text=feedback.getText();
		position=feedback.getPosition();
		resourceWidth=feedback.getResourceWidth();
		resourceHeight=feedback.getResourceHeight();
		type=feedback.getFeedbackType().getType();
		String test=feedback.getTest();
		if (TestConditionBean.TESTS.containsValue(test))
		{
			conditions.add(new TestConditionBean(test));
		}
		String answer=feedback.getAnswer();
		if (answer!=null && !answer.equals(""))
		{
			List<String> orAnswers=OmHelper.getOrAnswers(answer);
			Question q=question.getQuestion(operation);
			if (q instanceof DragDropQuestion)
			{
				for (String orAnswer:orAnswers)
				{
					AnswerConditionBean answerCondition=new AnswerConditionBean();
					List<String> andAnswers=OmHelper.getAndAnswers(orAnswer);
					for (String andAnswer:andAnswers)
					{
						boolean flagNot=OmHelper.isAnswerFlagNot(andAnswer);
						String idAnswer=OmHelper.getOnlyId(andAnswer);
						int answerGroup=Integer.parseInt(
							idAnswer.substring("answer".length(),idAnswer.indexOf('_',"answer".length())));
						int answerPos=
							Integer.parseInt(idAnswer.substring(idAnswer.indexOf('_',"answer".length())+1));
						String idDraggableItem=OmHelper.getAnswerSelector(andAnswer);
						int rightAnswerPos=idDraggableItem.equals("+empty+")?0:Integer.parseInt(
							idDraggableItem.substring(idDraggableItem.indexOf('_',"draggableitem".length())+1));
						SingleAnswerConditionBean singleAnswer=
							new SingleDragDropAnswerConditionBean(flagNot,q,answerGroup,answerPos,rightAnswerPos);
						answerCondition.getSingleAnswerConditions().add(singleAnswer);
					}
					conditions.add(answerCondition);
				}
			}
			else
			{
				for (String orAnswer:orAnswers)
				{
					AnswerConditionBean answerCondition=new AnswerConditionBean();
					List<String> andAnswers=OmHelper.getAndAnswers(orAnswer);
					for (String andAnswer:andAnswers)
					{
						boolean flagNot=OmHelper.isAnswerFlagNot(andAnswer);
						String idAnswer=OmHelper.getOnlyId(andAnswer);
						int answerPos=Integer.parseInt(idAnswer.substring("answer".length()));
						SingleAnswerConditionBean singleAnswer=new SingleAnswerConditionBean(flagNot,q,answerPos);
						answerCondition.getSingleAnswerConditions().add(singleAnswer);
					}
					conditions.add(answerCondition);
				}
			}
		}
		LocalizationService localizationService=null;
		if (feedback.getAttemptsmin()>0 || feedback.getAttemptsmax()<AttemptsConditionBean.MAX_ATTEMPT)
		{
			AttemptsConditionBean attemptsCondition=null;
			localizationService=getLocalizationService(localizationService);
			String attemptsGen=localizationService.getLocalizedMessage("CONDITION_TYPE_ATTEMPTS_GEN");
			if ("M".equals(attemptsGen))
			{
				attemptsCondition=
					new AttemptsConditionBean(feedback.getAttemptsmin(),feedback.getAttemptsmax(),false);
			}
			else if ("F".equals(attemptsGen))
			{
				attemptsCondition=
					new AttemptsConditionBean(feedback.getAttemptsmin(),feedback.getAttemptsmax(),true);
			}
			else
			{
				attemptsCondition=
					new AttemptsConditionBean(feedback.getAttemptsmin(),feedback.getAttemptsmax());
			}
			conditions.add(attemptsCondition);
		}
		if (feedback.getSelectedanswersmin()>0 || 
			feedback.getSelectedanswersmax()<question.getNumberOfSelectableAnswers(operation))
		{
			SelectedAnswersConditionBean selectedAnswersCondition=null;
			localizationService=getLocalizationService(localizationService);
			String selectedAnswersGen=
				localizationService.getLocalizedMessage("CONDITION_TYPE_SELECTED_ANSWERS_GEN");
			if ("M".equals(selectedAnswersGen))
			{
				selectedAnswersCondition=new SelectedAnswersConditionBean(
					question,feedback.getSelectedanswersmin(),feedback.getSelectedanswersmax(),false);
			}
			else if ("F".equals(selectedAnswersGen))
			{
				selectedAnswersCondition=new SelectedAnswersConditionBean(
					question,feedback.getSelectedanswersmin(),feedback.getSelectedanswersmax(),true);
			}
			else
			{
				selectedAnswersCondition=new SelectedAnswersConditionBean(
					question,feedback.getSelectedanswersmin(),feedback.getSelectedanswersmax());
			}
			conditions.add(selectedAnswersCondition);
		}
		if (feedback.getSelectedrightanswersmin()>0 || 
			feedback.getSelectedrightanswersmax()<question.getNumberOfSelectableRightAnswers(operation))
		{
			SelectedRightAnswersConditionBean selectedRightAnswersCondition=null;
			localizationService=getLocalizationService(localizationService);
			String selectedRightAnswersGen=
				localizationService.getLocalizedMessage("CONDITION_TYPE_SELECTED_RIGHT_ANSWERS_GEN");
			if ("M".equals(selectedRightAnswersGen))
			{
				selectedRightAnswersCondition=new SelectedRightAnswersConditionBean(
					question,feedback.getSelectedrightanswersmin(),feedback.getSelectedrightanswersmax(),false);
			}
			else if ("F".equals(selectedRightAnswersGen))
			{
				selectedRightAnswersCondition=new SelectedRightAnswersConditionBean(
					question,feedback.getSelectedrightanswersmin(),feedback.getSelectedrightanswersmax(),true);
			}
			else
			{
				selectedRightAnswersCondition=new SelectedRightAnswersConditionBean(
					question,feedback.getSelectedrightanswersmin(),feedback.getSelectedrightanswersmax());
			}
			conditions.add(selectedRightAnswersCondition);
		}
		if (feedback.getSelectedwronganswersmin()>0 || 
				feedback.getSelectedwronganswersmax()<question.getNumberOfSelectableWrongAnswers(operation))
		{
			SelectedWrongAnswersConditionBean selectedWrongAnswersCondition=null;
			localizationService=getLocalizationService(localizationService);
			String selectedWrongAnswersGen=localizationService.getLocalizedMessage(
				"CONDITION_TYPE_SELECTED_WRONG_ANSWERS_GEN");
			if ("M".equals(selectedWrongAnswersGen))
			{
				selectedWrongAnswersCondition=new SelectedWrongAnswersConditionBean(
					question,feedback.getSelectedwronganswersmin(),feedback.getSelectedwronganswersmax(),false);
			}
			else if ("F".equals(selectedWrongAnswersGen))
			{
				selectedWrongAnswersCondition=new SelectedWrongAnswersConditionBean(
					question,feedback.getSelectedwronganswersmin(),feedback.getSelectedwronganswersmax(),true);
			}
			else
			{
				selectedWrongAnswersCondition=new SelectedWrongAnswersConditionBean(
					question,feedback.getSelectedwronganswersmin(),feedback.getSelectedwronganswersmax());
			}
			conditions.add(selectedWrongAnswersCondition);
		}
		if (feedback.getUnselectedanswersmin()>0 || 
				feedback.getUnselectedanswersmax()<question.getNumberOfSelectableAnswers(operation))
		{
			UnselectedAnswersConditionBean unselectedAnswersCondition=null;
			localizationService=getLocalizationService(localizationService);
			String unselectedAnswersGen=
				localizationService.getLocalizedMessage("CONDITION_TYPE_UNSELECTED_ANSWERS_GEN");
			if ("M".equals(unselectedAnswersGen))
			{
				unselectedAnswersCondition=new UnselectedAnswersConditionBean(
					question,feedback.getUnselectedanswersmin(),feedback.getUnselectedanswersmax(),false);
			}
			else if ("F".equals(unselectedAnswersGen))
			{
				unselectedAnswersCondition=new UnselectedAnswersConditionBean(
					question,feedback.getUnselectedanswersmin(),feedback.getUnselectedanswersmax(),true);
			}
			else
			{
				unselectedAnswersCondition=new UnselectedAnswersConditionBean(
					question,feedback.getUnselectedanswersmin(),feedback.getUnselectedanswersmax());
			}
			conditions.add(unselectedAnswersCondition);
		}
		if (feedback.getUnselectedrightanswersmin()>0 || 
			feedback.getUnselectedrightanswersmax()<question.getNumberOfSelectableRightAnswers(operation))
		{
			UnselectedRightAnswersConditionBean unselectedRightAnswersCondition=null;
			localizationService=getLocalizationService(localizationService);
			String unselectedRightAnswersGen=
				localizationService.getLocalizedMessage("CONDITION_TYPE_UNSELECTED_RIGHT_ANSWERS_GEN");
			if ("M".equals(unselectedRightAnswersGen))
			{
				unselectedRightAnswersCondition=new UnselectedRightAnswersConditionBean(question,
					feedback.getUnselectedrightanswersmin(),feedback.getUnselectedrightanswersmax(),false);
			}
			else if ("F".equals(unselectedRightAnswersGen))
			{
				unselectedRightAnswersCondition=new UnselectedRightAnswersConditionBean(question,
					feedback.getUnselectedrightanswersmin(),feedback.getUnselectedrightanswersmax(),true);
			}
			else
			{
				unselectedRightAnswersCondition=new UnselectedRightAnswersConditionBean(
					question,feedback.getUnselectedrightanswersmin(),feedback.getUnselectedrightanswersmax());
			}
			conditions.add(unselectedRightAnswersCondition);
		}
		if (feedback.getUnselectedwronganswersmin()>0 || 
				feedback.getUnselectedwronganswersmax()<question.getNumberOfSelectableWrongAnswers(operation))
		{
			UnselectedWrongAnswersConditionBean unselectedWrongAnswerCondition=null;
			localizationService=getLocalizationService(localizationService);
			String unselectedWrongAnswersGen=
				localizationService.getLocalizedMessage("CONDITION_TYPE_UNSELECTED_WRONG_ANSWERS_GEN");
			if ("M".equals(unselectedWrongAnswersGen))
			{
				unselectedWrongAnswerCondition=new UnselectedWrongAnswersConditionBean(question,
					feedback.getUnselectedwronganswersmin(),feedback.getUnselectedwronganswersmax(),false);
			}
			else if ("F".equals(unselectedWrongAnswersGen))
			{
				unselectedWrongAnswerCondition=new UnselectedWrongAnswersConditionBean(question,
					feedback.getUnselectedwronganswersmin(),feedback.getUnselectedwronganswersmax(),true);
			}
			else
			{
				unselectedWrongAnswerCondition=new UnselectedWrongAnswersConditionBean(
					question,feedback.getUnselectedwronganswersmin(),feedback.getUnselectedwronganswersmax());
			}
			conditions.add(unselectedWrongAnswerCondition);
		}
		if (feedback.getRightdistancemin()>0 || 
			feedback.getRightdistancemax()<new RightDistanceConditionBean(question).getMaxRightDistanceValue(operation))
		{
			RightDistanceConditionBean rightDistanceCondition=null;
			localizationService=getLocalizationService(localizationService);
			String rightDistanceGen=localizationService.getLocalizedMessage("CONDITION_TYPE_RIGHT_DISTANCE_GEN");
			if ("M".equals(rightDistanceGen))
			{
				rightDistanceCondition=new RightDistanceConditionBean(
					question,feedback.getRightdistancemin(),feedback.getRightdistancemax(),false);
			}
			else if ("F".equals(rightDistanceGen))
			{
				rightDistanceCondition=new RightDistanceConditionBean(
					question,feedback.getRightdistancemin(),feedback.getRightdistancemax(),true);
			}
			else
			{
				rightDistanceCondition=new RightDistanceConditionBean(
					question,feedback.getRightdistancemin(),feedback.getRightdistancemax());
			}
			
			conditions.add(rightDistanceCondition);
		}
	}
	
	public Feedback getAsFeedback()
	{
		return getAsFeedback(null);
	}
	
	public Feedback getAsFeedback(Operation operation)
	{
		// Get current user session Hibernate operation
		operation=question.getCurrentUserOperation(operation);
		
		return new Feedback(id,question.getQuestion(operation),resource,question.getFeedbackType(operation,type),text,
			getTest(),getAnswer(),getAttemptsMin(),getAttemptsMax(),getSelectedanswersmin(),
			getSelectedanswersmax(operation),getSelectedrightanswersmin(),getSelectedrightanswersmax(operation),
			getSelectedwronganswersmin(),getSelectedwronganswersmax(operation),getUnselectedanswersmin(),
			getUnselectedanswersmax(operation),getUnselectedrightanswersmin(),getUnselectedrightanswersmax(operation),
			getUnselectedwronganswersmin(),getUnselectedwronganswersmax(operation),getRightdistancemin(),
			getRightdistancemax(operation),position,resourceWidth,resourceHeight);
	}
	
	public long getId()
	{
		return id;
	}
	
	public void setId(long id)
	{
		this.id=id;
	}
	
	public Resource getResource()
	{
		return resource;
	}
	
	public void setResource(Resource resource)
	{
		this.resource=resource;
	}
	
	public String getText()
	{
		return text;
	}
	
	public void setText(String text)
	{
		this.text=text;
	}
	
	private String getComparisonString(String comparator,int valueCmp,int valueBetweenMin,int valueBetweenMax)
	{
		StringBuffer comparisonString=new StringBuffer();
		if (NumberComparator.compareU(comparator,NumberComparator.EQUAL))
		{
			comparisonString.append(valueCmp);
		}
		else if (NumberComparator.compareU(comparator,NumberComparator.GREATER))
		{
			comparisonString.append('>');
			comparisonString.append(valueCmp);
		}
		else if (NumberComparator.compareU(comparator,NumberComparator.GREATER_EQUAL))
		{
			comparisonString.append(">=");
			comparisonString.append(valueCmp);
		}
		else if (NumberComparator.compareU(comparator,NumberComparator.LESS))
		{
			comparisonString.append('<');
			comparisonString.append(valueCmp);
		}
		else if (NumberComparator.compareU(comparator,NumberComparator.LESS_EQUAL))
		{
			comparisonString.append("<=");
			comparisonString.append(valueCmp);
		}
		else if (NumberComparator.compareU(comparator,NumberComparator.BETWEEN))
		{
			comparisonString.append(valueBetweenMin);
			comparisonString.append("..");
			comparisonString.append(valueBetweenMax);
		}
		return comparisonString.toString();
	}
	
	public String getConditionString()
	{
		return getConditionString(null);
	}
	
	private String getConditionString(Operation operation)
	{
		StringBuffer conditionString=new StringBuffer();
        ELContext elContext=FacesContext.getCurrentInstance().getELContext();
        LocalizationService localizationService=(LocalizationService)FacesContext.getCurrentInstance().
        	getApplication().getELResolver().getValue(elContext,null,"localizationService");
		boolean appendSeparator=false;
		
		// First we add test condition
		TestConditionBean testCondition=getTestCondition();
		if (testCondition!=null)
		{
			appendSeparator=true;
			conditionString.append(localizationService.getLocalizedMessage("CONDITION_TYPE_TEST"));
			conditionString.append(": ");
			conditionString.append(localizationService.getLocalizedMessage(testCondition.getTest()));
		}
		
		// Next we add answer conditions
		if (getAnswerConditions().size()>0)
		{
			// Get current user session Hibernate operation
			operation=question.getCurrentUserOperation(operation);
			
			if (appendSeparator)
			{
				conditionString.append(",\n");
			}
			else
			{
				appendSeparator=true;
			}
			if (!areOnlyAnswerConditions())
			{
				conditionString.append(localizationService.getLocalizedMessage("ANSWERS"));
				conditionString.append(": ");
			}
			boolean severalAnswerConditions=getAnswerConditions().size()>1;
			boolean insertOr=false;
			for (AnswerConditionBean answerCondition:getAnswerConditions())
			{
				boolean severalSingleAnswerConditions=answerCondition.getSingleAnswerConditionsSize()>1;
				if (insertOr)
				{
					conditionString.append(' ');
					conditionString.append(localizationService.getLocalizedMessage("OR"));
					conditionString.append(' ');
				}
				else
				{
					insertOr=true;
				}
				if (severalAnswerConditions && severalSingleAnswerConditions)
				{
					conditionString.append('(');
				}
				boolean insertAnd=false;
				for (SingleAnswerConditionBean singleAnswerCondition:answerCondition.getSingleAnswerConditions())
				{
					if (insertAnd)
					{
						conditionString.append(' ');
						conditionString.append(localizationService.getLocalizedMessage("AND"));
						conditionString.append(' ');
					}
					else
					{
						insertAnd=true;
					}
					if (singleAnswerCondition instanceof SingleDragDropAnswerConditionBean)
					{
						SingleDragDropAnswerConditionBean singleDragDropAnswerCondition=
							(SingleDragDropAnswerConditionBean)singleAnswerCondition;
						conditionString.append(question.getNumberedDroppableAnswerName(
							operation,singleDragDropAnswerCondition.getAnswer()));
						conditionString.append(' ');
						if (singleAnswerCondition.isFlagNot())
						{
							conditionString.append(localizationService.getLocalizedMessage("NOT_CONTAINS"));
						}
						else
						{
							conditionString.append(localizationService.getLocalizedMessage("CONTAINS"));
						}
						conditionString.append(' ');
						if (singleDragDropAnswerCondition.getRightAnswer()==null)
						{
							conditionString.append('(');
							conditionString.append(localizationService.getLocalizedMessage("EMPTY"));
							conditionString.append(')');
						}
						else
						{
							conditionString.append(question.getNumberedDraggableItemName(
								operation,singleDragDropAnswerCondition.getRightAnswer()));
						}
					}
					else
					{
						if (singleAnswerCondition.isFlagNot())
						{
							conditionString.append(localizationService.getLocalizedMessage("NOT"));
							conditionString.append(' ');
						}
						conditionString.append(
							question.getNumberedAnswerName(operation,singleAnswerCondition.getAnswer()));
					}
				}
				if (severalAnswerConditions && severalSingleAnswerConditions)
				{
					conditionString.append(')');
				}
			}
		}
		
		// Next we add attempts condition
		AttemptsConditionBean attemptsCondition=getAttemptsCondition();
		if (attemptsCondition!=null)
		{
			if (appendSeparator)
			{
				conditionString.append(",\n");
			}
			else
			{
				appendSeparator=true;
			}
			conditionString.append(localizationService.getLocalizedMessage("ATTEMPT"));
			conditionString.append(": ");
			conditionString.append(getComparisonString(attemptsCondition.getComparator(),
				attemptsCondition.getAttemptsCmp(),attemptsCondition.getAttemptsBetweenMin(),
				attemptsCondition.getAttemptsBetweenMax()));
		}
		
		// Next we add selected answers condition
		SelectedAnswersConditionBean selectedAnswersCondition=getSelectedAnswersCondition();
		if (selectedAnswersCondition!=null)
		{
			if (appendSeparator)
			{
				conditionString.append(",\n");
			}
			else
			{
				appendSeparator=true;
			}
			conditionString.append(localizationService.getLocalizedMessage("SELECTED_ANSWERS"));
			conditionString.append(": ");
			conditionString.append(getComparisonString(selectedAnswersCondition.getComparator(),
				selectedAnswersCondition.getSelectedAnswersCmp(),
				selectedAnswersCondition.getSelectedAnswersBetweenMin(),
				selectedAnswersCondition.getSelectedAnswersBetweenMax()));
		}
		
		// Next we add selected right answers condition
		SelectedRightAnswersConditionBean selectedRightAnswersCondition=getSelectedRightAnswersCondition();
		if (selectedRightAnswersCondition!=null)
		{
			if (appendSeparator)
			{
				conditionString.append(",\n");
			}
			else
			{
				appendSeparator=true;
			}
			conditionString.append(localizationService.getLocalizedMessage("SELECTED_RIGHT_ANSWERS"));
			conditionString.append(": ");
			conditionString.append(getComparisonString(selectedRightAnswersCondition.getComparator(),
				selectedRightAnswersCondition.getSelectedRightAnswersCmp(),
				selectedRightAnswersCondition.getSelectedRightAnswersBetweenMin(),
				selectedRightAnswersCondition.getSelectedRightAnswersBetweenMax()));
		}
		
		// Next we add selected wrong answers condition
		SelectedWrongAnswersConditionBean selectedWrongAnswersCondition=getSelectedWrongAnswersCondition();
		if (selectedWrongAnswersCondition!=null)
		{
			if (appendSeparator)
			{
				conditionString.append(",\n");
			}
			else
			{
				appendSeparator=true;
			}
			conditionString.append(localizationService.getLocalizedMessage("SELECTED_WRONG_ANSWERS"));
			conditionString.append(": ");
			conditionString.append(getComparisonString(selectedWrongAnswersCondition.getComparator(),
				selectedWrongAnswersCondition.getSelectedWrongAnswersCmp(),
				selectedWrongAnswersCondition.getSelectedWrongAnswersBetweenMin(),
				selectedWrongAnswersCondition.getSelectedWrongAnswersBetweenMax()));
		}
		
		// Next we add unselected answers condition
		UnselectedAnswersConditionBean unselectedAnswersCondition=getUnselectedAnswersCondition();
		if (unselectedAnswersCondition!=null)
		{
			if (appendSeparator)
			{
				conditionString.append(",\n");
			}
			else
			{
				appendSeparator=true;
			}
			conditionString.append(localizationService.getLocalizedMessage("UNSELECTED_ANSWERS"));
			conditionString.append(": ");
			conditionString.append(getComparisonString(unselectedAnswersCondition.getComparator(),
				unselectedAnswersCondition.getUnselectedAnswersCmp(),
				unselectedAnswersCondition.getUnselectedAnswersBetweenMin(),
				unselectedAnswersCondition.getUnselectedAnswersBetweenMax()));
		}
		
		// Next we add unselected right answers condition
		UnselectedRightAnswersConditionBean unselectedRightAnswersCondition=getUnselectedRightAnswersCondition();
		if (unselectedRightAnswersCondition!=null)
		{
			if (appendSeparator)
			{
				conditionString.append(",\n");
			}
			else
			{
				appendSeparator=true;
			}
			conditionString.append(localizationService.getLocalizedMessage("UNSELECTED_RIGHT_ANSWERS"));
			conditionString.append(": ");
			conditionString.append(getComparisonString(unselectedRightAnswersCondition.getComparator(),
				unselectedRightAnswersCondition.getUnselectedRightAnswersCmp(),
				unselectedRightAnswersCondition.getUnselectedRightAnswersBetweenMin(),
				unselectedRightAnswersCondition.getUnselectedRightAnswersBetweenMax()));
		}
		
		// Next we add unselected wrong answers condition
		UnselectedWrongAnswersConditionBean unselectedWrongAnswersCondition=getUnselectedWrongAnswersCondition();
		if (unselectedWrongAnswersCondition!=null)
		{
			if (appendSeparator)
			{
				conditionString.append(",\n");
			}
			else
			{
				appendSeparator=true;
			}
			conditionString.append(localizationService.getLocalizedMessage("UNSELECTED_WRONG_ANSWERS"));
			conditionString.append(": ");
			conditionString.append(getComparisonString(unselectedWrongAnswersCondition.getComparator(),
				unselectedWrongAnswersCondition.getUnselectedWrongAnswersCmp(),
				unselectedWrongAnswersCondition.getUnselectedWrongAnswersBetweenMin(),
				unselectedWrongAnswersCondition.getUnselectedWrongAnswersBetweenMax()));
		}
		
		// Next we add right distance condition
		RightDistanceConditionBean rightDistanceCondition=getRightDistanceCondition();
		if (rightDistanceCondition!=null)
		{
			if (appendSeparator)
			{
				conditionString.append(",\n");
			}
			else
			{
				appendSeparator=true;
			}
			conditionString.append(localizationService.getLocalizedMessage("RIGHT_ANSWER_DISTANCE"));
			conditionString.append(": ");
			conditionString.append(getComparisonString(rightDistanceCondition.getComparator(),
			rightDistanceCondition.getRightDistanceCmp(),rightDistanceCondition.getRightDistanceBetweenMin(),
			rightDistanceCondition.getRightDistanceBetweenMax()));
		}
		
		if (conditionString.length()==0)
		{
			conditionString.append(localizationService.getLocalizedMessage("NO_CONDITIONS"));
		}
		return conditionString.toString();
	}
	
	private boolean areOnlyAnswerConditions()
	{
		return getTestCondition()==null && getAttemptsCondition()==null && getSelectedAnswersCondition()==null && 
			getSelectedRightAnswersCondition()==null && getSelectedWrongAnswersCondition()==null && 
			getUnselectedAnswersCondition()==null && getUnselectedRightAnswersCondition()==null && 
			getUnselectedWrongAnswersCondition()==null && getRightDistanceCondition()==null;
	}
	
	public int getAttemptsMin()
	{
		AttemptsConditionBean attemptsCondition=getAttemptsCondition();
		return attemptsCondition==null?0:attemptsCondition.getAttemptsMin();
	}
	
	public int getAttemptsMax()
	{
		AttemptsConditionBean attemptsCondition=getAttemptsCondition();
		return attemptsCondition==null?Integer.MAX_VALUE:attemptsCondition.getAttemptsMax();
	}
	
	public int getSelectedanswersmin()
	{
		SelectedAnswersConditionBean selectedAnswersCondition=getSelectedAnswersCondition();
		return selectedAnswersCondition==null?0:selectedAnswersCondition.getSelectedAnswersMin();
	}
	
	public int getSelectedanswersmax()
	{
		return getSelectedanswersmax(null);
	}
	
	private int getSelectedanswersmax(Operation operation)
	{
		SelectedAnswersConditionBean selectedAnswersCondition=getSelectedAnswersCondition();
		return selectedAnswersCondition==null?Integer.MAX_VALUE:
			selectedAnswersCondition.getSelectedAnswersMax(question.getCurrentUserOperation(operation));
	}
	
	public int getSelectedrightanswersmin()
	{
		SelectedRightAnswersConditionBean selectedRightAnswersCondition=getSelectedRightAnswersCondition();
		return selectedRightAnswersCondition==null?0:selectedRightAnswersCondition.getSelectedRightAnswersMin();
	}
	
	public int getSelectedrightanswersmax()
	{
		return getSelectedrightanswersmax(null);
	}
	
	private int getSelectedrightanswersmax(Operation operation)
	{
		SelectedRightAnswersConditionBean selectedRightAnswersCondition=getSelectedRightAnswersCondition();
		return selectedRightAnswersCondition==null?Integer.MAX_VALUE:
			selectedRightAnswersCondition.getSelectedRightAnswersMax(question.getCurrentUserOperation(operation));
	}
	
	public int getSelectedwronganswersmin()
	{
		SelectedWrongAnswersConditionBean selectedWrongAnswersCondition=getSelectedWrongAnswersCondition();
		return selectedWrongAnswersCondition==null?0:selectedWrongAnswersCondition.getSelectedWrongAnswersMin();
	}
	
	public int getSelectedwronganswersmax()
	{
		return getSelectedwronganswersmax(null);
	}
	
	private int getSelectedwronganswersmax(Operation operation)
	{
		SelectedWrongAnswersConditionBean selectedWrongAnswersCondition=getSelectedWrongAnswersCondition();
		return selectedWrongAnswersCondition==null?Integer.MAX_VALUE:
			selectedWrongAnswersCondition.getSelectedWrongAnswersMax(question.getCurrentUserOperation(operation));
	}
	
	public int getUnselectedanswersmin()
	{
		UnselectedAnswersConditionBean unselectedAnswersCondition=getUnselectedAnswersCondition();
		return unselectedAnswersCondition==null?0:unselectedAnswersCondition.getUnselectedAnswersMin();
	}
	
	public int getUnselectedanswersmax()
	{
		return getUnselectedanswersmax(null);
	}
	
	private int getUnselectedanswersmax(Operation operation)
	{
		UnselectedAnswersConditionBean unselectedAnswersCondition=getUnselectedAnswersCondition();
		return unselectedAnswersCondition==null?Integer.MAX_VALUE:
			unselectedAnswersCondition.getUnselectedAnswersMax(question.getCurrentUserOperation(operation));
	}
	
	public int getUnselectedrightanswersmin()
	{
		UnselectedRightAnswersConditionBean unselectedRightAnswersCondition=getUnselectedRightAnswersCondition();
		return unselectedRightAnswersCondition==null?
			0:unselectedRightAnswersCondition.getUnselectedRightAnswersMin();
	}
	
	public int getUnselectedrightanswersmax()
	{
		return getUnselectedrightanswersmax(null);
	}
	
	private int getUnselectedrightanswersmax(Operation operation)
	{
		UnselectedRightAnswersConditionBean unselectedRightAnswersCondition=getUnselectedRightAnswersCondition();
		return unselectedRightAnswersCondition==null?Integer.MAX_VALUE:
			unselectedRightAnswersCondition.getUnselectedRightAnswersMax(question.getCurrentUserOperation(operation));
	}
	
	public int getUnselectedwronganswersmin()
	{
		UnselectedWrongAnswersConditionBean unselectedWrongAnswersCondition=getUnselectedWrongAnswersCondition();
		return unselectedWrongAnswersCondition==null?
			0:unselectedWrongAnswersCondition.getUnselectedWrongAnswersMin();
	}
	
	public int getUnselectedwronganswersmax()
	{
		return getUnselectedwronganswersmax(null);
	}
	
	private int getUnselectedwronganswersmax(Operation operation)
	{
		UnselectedWrongAnswersConditionBean unselectedWrongAnswersCondition=getUnselectedWrongAnswersCondition();
		return unselectedWrongAnswersCondition==null?Integer.MAX_VALUE:
			unselectedWrongAnswersCondition.getUnselectedWrongAnswersMax(question.getCurrentUserOperation(operation));
	}
	
	public int getRightdistancemin()
	{
		RightDistanceConditionBean rightDistanceCondition=getRightDistanceCondition();
		return rightDistanceCondition==null?0:rightDistanceCondition.getRightDistanceMin();
	}
	
	public int getRightdistancemax()
	{
		return getRightdistancemax(null);
	}
	
	private int getRightdistancemax(Operation operation)
	{
		RightDistanceConditionBean rightDistanceCondition=getRightDistanceCondition();
		return rightDistanceCondition==null?
			Integer.MAX_VALUE:rightDistanceCondition.getRightDistanceMax(question.getCurrentUserOperation(operation));
	}
	
	public int getPosition()
	{
		return position;
	}
	
	public void setPosition(int position)
	{
		this.position=position;
	}
	
	public String getType()
	{
		return type;
	}
	
	public void setType(String type)
	{
		this.type=type;
	}
	
	public String getTypeTip()
	{
		StringBuffer typeTip=new StringBuffer("TIP_");
		typeTip.append(type);
		return typeTip.toString();
	}
	
	public int getResourceWidth()
	{
		return resourceWidth;
	}
	
	public void setResourceWidth(int resourceWidth)
	{
		this.resourceWidth=resourceWidth;
	}
	
	public int getResourceHeight()
	{
		return resourceHeight;
	}
	
	public void setResourceHeight(int resourceHeight)
	{
		this.resourceHeight=resourceHeight;
	}
	
	public List<ConditionBean> getConditions()
	{
		return conditions;
	}
	
	public void setConditions(List<ConditionBean> conditions)
	{
		this.conditions=conditions;
	}
	
	public int getConditionsSize()
	{
		return conditions.size();
	}
	
	public String getTest()
	{
		String test="true";
		TestConditionBean testCondition=getTestCondition();
		if (testCondition!=null)
		{
			test=testCondition.getTestValue();
		}
		return test;
	}
	
	public String getAnswer()
	{
		StringBuffer answer=new StringBuffer();
		boolean insertComma=false;
		for (AnswerConditionBean answerCondition:getAnswerConditions())
		{
			if (insertComma)
			{
				answer.append(',');
			}
			else
			{
				insertComma=true;
			}
			boolean insertPlus=false;
			for (SingleAnswerConditionBean singleAnswerCondition:answerCondition.getSingleAnswerConditions())
			{
				if (insertPlus)
				{
					answer.append('+');
				}
				else
				{
					insertPlus=true;
				}
				if (singleAnswerCondition.isFlagNot())
				{
					answer.append('!');
				}
				if (singleAnswerCondition instanceof SingleDragDropAnswerConditionBean)
				{
					SingleDragDropAnswerConditionBean singleDragDropAnswerCondition=
						(SingleDragDropAnswerConditionBean)singleAnswerCondition;
					answer.append("answer");
					answer.append(singleDragDropAnswerCondition.getGroup());
					answer.append('_');
					answer.append(singleDragDropAnswerCondition.getAnswer().getPosition());
					answer.append('[');
					if (singleDragDropAnswerCondition.getRightAnswer()==null)
					{
						answer.append("+empty+");
					}
					else
					{
						answer.append("draggableitem");
						answer.append(singleDragDropAnswerCondition.getGroup());
						answer.append('_');
						answer.append(singleDragDropAnswerCondition.getRightAnswerPosition());
					}
					answer.append(']');
				}
				else
				{
					answer.append("answer");
					answer.append(singleAnswerCondition.getAnswer().getPosition());
				}
			}
		}
		return answer.toString();
	}
	
	public TestConditionBean getTestCondition()
	{
		TestConditionBean testCondition=null;
		for (ConditionBean condition:conditions)
		{
			if (condition instanceof TestConditionBean)
			{
				testCondition=(TestConditionBean)condition;
				break;
			}
		}
		return testCondition;
	}
	
	public List<AnswerConditionBean> getAnswerConditions()
	{
		List<AnswerConditionBean> answerConditions=new ArrayList<AnswerConditionBean>();
		for (ConditionBean condition:conditions)
		{
			if (condition instanceof AnswerConditionBean)
			{
				answerConditions.add((AnswerConditionBean)condition);
			}
		}
		return answerConditions;
	}
	
	public AttemptsConditionBean getAttemptsCondition()
	{
		AttemptsConditionBean attemptsCondition=null;
		for (ConditionBean condition:conditions)
		{
			if (condition instanceof AttemptsConditionBean)
			{
				attemptsCondition=(AttemptsConditionBean)condition;
				break;
			}
		}
		return attemptsCondition;
	}
	
	public SelectedAnswersConditionBean getSelectedAnswersCondition()
	{
		SelectedAnswersConditionBean selectedAnswersCondition=null;
		for (ConditionBean condition:conditions)
		{
			if (condition instanceof SelectedAnswersConditionBean)
			{
				selectedAnswersCondition=(SelectedAnswersConditionBean)condition;
				break;
			}
		}
		return selectedAnswersCondition;
	}
	
	public SelectedRightAnswersConditionBean getSelectedRightAnswersCondition()
	{
		SelectedRightAnswersConditionBean selectedRightAnswersCondition=null;
		for (ConditionBean condition:conditions)
		{
			if (condition instanceof SelectedRightAnswersConditionBean)
			{
				selectedRightAnswersCondition=(SelectedRightAnswersConditionBean)condition;
				break;
			}
		}
		return selectedRightAnswersCondition;
	}
	
	public SelectedWrongAnswersConditionBean getSelectedWrongAnswersCondition()
	{
		SelectedWrongAnswersConditionBean selectedWrongAnswersCondition=null;
		for (ConditionBean condition:conditions)
		{
			if (condition instanceof SelectedWrongAnswersConditionBean)
			{
				selectedWrongAnswersCondition=(SelectedWrongAnswersConditionBean)condition;
				break;
			}
		}
		return selectedWrongAnswersCondition;
	}
	
	public UnselectedAnswersConditionBean getUnselectedAnswersCondition()
	{
		UnselectedAnswersConditionBean unselectedAnswersCondition=null;
		for (ConditionBean condition:conditions)
		{
			if (condition instanceof UnselectedAnswersConditionBean)
			{
				unselectedAnswersCondition=(UnselectedAnswersConditionBean)condition;
				break;
			}
		}
		return unselectedAnswersCondition;
	}
	
	public UnselectedRightAnswersConditionBean getUnselectedRightAnswersCondition()
	{
		UnselectedRightAnswersConditionBean unselectedRightAnswersCondition=null;
		for (ConditionBean condition:conditions)
		{
			if (condition instanceof UnselectedRightAnswersConditionBean)
			{
				unselectedRightAnswersCondition=(UnselectedRightAnswersConditionBean)condition;
				break;
			}
		}
		return unselectedRightAnswersCondition;
	}
	
	public UnselectedWrongAnswersConditionBean getUnselectedWrongAnswersCondition()
	{
		UnselectedWrongAnswersConditionBean unselectedWrongAnswersCondition=null;
		for (ConditionBean condition:conditions)
		{
			if (condition instanceof UnselectedWrongAnswersConditionBean)
			{
				unselectedWrongAnswersCondition=(UnselectedWrongAnswersConditionBean)condition;
				break;
			}
		}
		return unselectedWrongAnswersCondition;
	}
	
	public RightDistanceConditionBean getRightDistanceCondition()
	{
		RightDistanceConditionBean rightDistanceCondition=null;
		for (ConditionBean condition:conditions)
		{
			if (condition instanceof RightDistanceConditionBean)
			{
				rightDistanceCondition=(RightDistanceConditionBean)condition;
				break;
			}
		}
		return rightDistanceCondition;
	}
	
	public List<ConditionBean> getConditionsSorted()
	{
		List<ConditionBean> conditionsSorted=new ArrayList<ConditionBean>();
		// First we put test condition if exists
		ConditionBean testCondition=getTestCondition();
		if (testCondition!=null)
		{
			conditionsSorted.add(testCondition);
		}
		// Next we put answer conditions
		for (ConditionBean answerCondition:getAnswerConditions())
		{
			conditionsSorted.add(answerCondition);
		}
		// Next we put attempts condition if exists
		ConditionBean attemptsCondition=getAttemptsCondition();
		if (attemptsCondition!=null)
		{
			conditionsSorted.add(attemptsCondition);
		}
		// Next we put selected answers condition if exists
		ConditionBean selectedAnswersCondition=getSelectedAnswersCondition();
		if (selectedAnswersCondition!=null)
		{
			conditionsSorted.add(selectedAnswersCondition);
		}
		// Next we put selected right answers condition if exists
		ConditionBean selectedRightAnswersCondition=getSelectedRightAnswersCondition();
		if (selectedRightAnswersCondition!=null)
		{
			conditionsSorted.add(selectedRightAnswersCondition);
		}
		// Next we put selected wrong answers condition if exists
		ConditionBean selectedWrongAnswersCondition=getSelectedWrongAnswersCondition();
		if (selectedWrongAnswersCondition!=null)
		{
			conditionsSorted.add(selectedWrongAnswersCondition);
		}
		// Next we put unselected answers condition if exists
		ConditionBean unselectedAnswersCondition=getUnselectedAnswersCondition();
		if (unselectedAnswersCondition!=null)
		{
			conditionsSorted.add(unselectedAnswersCondition);
		}
		// Next we put unselected right answers condition if exists
		ConditionBean unselectedRightAnswersCondition=getUnselectedRightAnswersCondition();
		if (unselectedRightAnswersCondition!=null)
		{
			conditionsSorted.add(unselectedRightAnswersCondition);
		}
		// Next we put unselected wrong answers condition if exists
		ConditionBean unselectedWrongAnswersCondition=getUnselectedWrongAnswersCondition();
		if (unselectedWrongAnswersCondition!=null)
		{
			conditionsSorted.add(unselectedWrongAnswersCondition);
		}
		// Finally we put right distance condition if exists
		ConditionBean rightDistanceCondition=getRightDistanceCondition();
		if (rightDistanceCondition!=null)
		{
			conditionsSorted.add(rightDistanceCondition);
		}
		return conditionsSorted;
	}
	
	public void setFromOtherFeedback(FeedbackBean otherFeedback)
	{
		id=otherFeedback.id;
		question=otherFeedback.question;
		resource=otherFeedback.resource;
		text=otherFeedback.text;
		position=otherFeedback.position;
		type=otherFeedback.type;
		resourceWidth=otherFeedback.resourceWidth;
		resourceHeight=otherFeedback.resourceHeight;
		conditions.clear();
		for (ConditionBean condition:otherFeedback.getConditions())
		{
			ConditionBean currentCondition=null;
			if (condition instanceof TestConditionBean)
			{
				currentCondition=new TestConditionBean((TestConditionBean)condition);
			}
			else if (condition instanceof AnswerConditionBean)
			{
				currentCondition=new AnswerConditionBean((AnswerConditionBean)condition);
			}
			else if (condition instanceof AttemptsConditionBean)
			{
				currentCondition=new AttemptsConditionBean((AttemptsConditionBean)condition);
			}
			else if (condition instanceof SelectedAnswersConditionBean)
			{
				currentCondition=new SelectedAnswersConditionBean((SelectedAnswersConditionBean)condition);
			}
			else if (condition instanceof SelectedRightAnswersConditionBean)
			{
				currentCondition=
					new SelectedRightAnswersConditionBean((SelectedRightAnswersConditionBean)condition);
			}
			else if (condition instanceof SelectedWrongAnswersConditionBean)
			{
				currentCondition=
					new SelectedWrongAnswersConditionBean((SelectedWrongAnswersConditionBean)condition);
			}
			else if (condition instanceof UnselectedAnswersConditionBean)
			{
				currentCondition=new UnselectedAnswersConditionBean((UnselectedAnswersConditionBean)condition);
			}
			else if (condition instanceof UnselectedRightAnswersConditionBean)
			{
				currentCondition=
					new UnselectedRightAnswersConditionBean((UnselectedRightAnswersConditionBean)condition);
			}
			else if (condition instanceof UnselectedWrongAnswersConditionBean)
			{
				currentCondition=
					new UnselectedWrongAnswersConditionBean((UnselectedWrongAnswersConditionBean)condition);
			}
			else if (condition instanceof RightDistanceConditionBean)
			{
				currentCondition=new RightDistanceConditionBean((RightDistanceConditionBean)condition);
			}
			if (currentCondition!=null)
			{
				conditions.add(currentCondition);
			}
		}
	}
	
	private LocalizationService getLocalizationService(LocalizationService localizationService)
	{
		if (localizationService==null)
		{
			// We get the localization service from context
			FacesContext context=FacesContext.getCurrentInstance();
			localizationService=(LocalizationService)context.getApplication().getELResolver().getValue(
				context.getELContext(),null,"localizationService");
		}
		return localizationService;
	}
}
