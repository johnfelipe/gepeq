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

import es.uned.lsi.gepec.model.entities.Answer;
import es.uned.lsi.gepec.model.entities.DragDropAnswer;
import es.uned.lsi.gepec.model.entities.DragDropQuestion;
import es.uned.lsi.gepec.model.entities.Question;

@SuppressWarnings("serial")
public class SingleDragDropAnswerConditionBean extends SingleAnswerConditionBean
{
	private int group;
	private int rightAnswerPosition;
	
	public SingleDragDropAnswerConditionBean(Question question,int group,int answerPosition)
	{
		this(false,question,group,answerPosition,0);
	}
	
	public SingleDragDropAnswerConditionBean(Question question,int group,int answerPosition,int rightAnswerPosition)
	{
		this(false,question,group,answerPosition,rightAnswerPosition);
	}
	
	public SingleDragDropAnswerConditionBean(boolean flagNot,Question question,int group,int answerPosition)
	{
		this(flagNot,question,group,answerPosition,0);
	}
	
	public SingleDragDropAnswerConditionBean(boolean flagNot,Question question,int group,int answerPosition,
		int rightAnswerPosition)
	{
		super(flagNot,question,answerPosition);
		this.group=group;
		this.rightAnswerPosition=rightAnswerPosition;
	}
	
	public SingleDragDropAnswerConditionBean(SingleDragDropAnswerConditionBean otherSingleDragDropAnswerCondition)
	{
		super(otherSingleDragDropAnswerCondition);
		this.group=otherSingleDragDropAnswerCondition.group;
		this.rightAnswerPosition=otherSingleDragDropAnswerCondition.rightAnswerPosition;
	}
	
	public int getGroup()
	{
		return group;
	}
	
	public void setGroup(int group)
	{
		this.group=group;
	}
	
	public int getRightAnswerPosition()
	{
		return rightAnswerPosition;
	}
	
	public void setRightAnswerPosition(int rightAnswerPosition)
	{
		this.rightAnswerPosition=rightAnswerPosition;
	}

	@Override
	public Answer getAnswer()
	{
		DragDropAnswer answer=((DragDropQuestion)getQuestion()).getDroppableAnswer(getGroup(),getAnswerPosition());
		return new DragDropAnswer(answer.getId(),null,null,-1,-1,answer.getText(),answer.getCorrect(), 
			answer.getFixed(),answer.getPosition(),answer.getName(),answer.isDraggable(),answer.getGroup(),
			getRightAnswer());
	}
	
	public Answer getRightAnswer()
	{
		DragDropAnswer rightAnswer=getRightAnswerPosition()>0?
			((DragDropQuestion)getQuestion()).getDraggableItem(getGroup(),getRightAnswerPosition()):null;
		return rightAnswer==null?
			null:new DragDropAnswer(rightAnswer.getId(),null,null,-1,-1,rightAnswer.getText(),
			rightAnswer.getCorrect(),rightAnswer.getFixed(),rightAnswer.getPosition(),rightAnswer.getName(),
			rightAnswer.isDraggable(),rightAnswer.getGroup(),null);
	}
}
