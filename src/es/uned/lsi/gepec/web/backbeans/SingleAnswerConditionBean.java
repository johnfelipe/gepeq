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

import es.uned.lsi.gepec.model.entities.Answer;
import es.uned.lsi.gepec.model.entities.Question;

@SuppressWarnings("serial")
public class SingleAnswerConditionBean implements Serializable
{
	private boolean flagNot;
	private Question question;
	private int answerPosition;
	
	public SingleAnswerConditionBean(Question question,int answerPosition)
	{
		this(false,question,answerPosition);
	}
	
	public SingleAnswerConditionBean(boolean flagNot,Question question,int answerPosition)
	{
		this.flagNot=flagNot;
		this.question=question;
		this.answerPosition=answerPosition;
	}
	
	public SingleAnswerConditionBean(SingleAnswerConditionBean otherSingleAnswerCondition)
	{
		this.flagNot=otherSingleAnswerCondition.flagNot;
		this.question=otherSingleAnswerCondition.question;
		this.answerPosition=otherSingleAnswerCondition.answerPosition;
	}
	
	public boolean isFlagNot()
	{
		return flagNot;
	}
	
	public void setFlagNot(boolean flagNot)
	{
		this.flagNot=flagNot;
	}
	
	public int getAnswerPosition()
	{
		return answerPosition;
	}

	public void setAnswerPosition(int answerPosition)
	{
		this.answerPosition=answerPosition;
	}
	
	public Answer getAnswer()
	{
		Answer answer=question.getAnswer(answerPosition);
		return new Answer(answer.getId(),null,null,-1,-1,answer.getText(),answer.getCorrect(),answer.getFixed(),
			answer.getPosition(),answer.getName());
	}
	
	protected Question getQuestion()
	{
		return question;
	}
}
