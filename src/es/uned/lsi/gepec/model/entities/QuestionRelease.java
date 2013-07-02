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
package es.uned.lsi.gepec.model.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Class to define a question published within a Test Navigator production environment.
 */
@SuppressWarnings("serial")
public class QuestionRelease implements Serializable
{
	private Question question;
	private User publisher;
	private Date releaseDate;
	private boolean allUsersAllowed;
	private Date startDate;
	private Date closeDate;
	private Date warningDate;
	private Date deleteDate;
	private List<User> users;
	private List<String> userGroups;
	
	public QuestionRelease(Question question,User publisher)
	{
		this(question,publisher,null,null,null,null,null,true,new ArrayList<User>(0),new ArrayList<String>(0));
	}
	
	public QuestionRelease(Question question,User publisher,Date releaseDate,Date startDate,Date closeDate,
		Date deleteDate)
	{
		this(question,publisher,releaseDate,startDate,closeDate,deleteDate,null,true,new ArrayList<User>(0),
			new ArrayList<String>(0));
	}
	
	public QuestionRelease(Question question,User publisher,Date releaseDate,Date startDate,Date closeDate,
		Date deleteDate,Date warningDate,boolean allUsersAllowed,List<User> users,List<String> userGroups)
	{
		this.question=question;
		this.publisher=publisher;
		this.releaseDate=releaseDate;
		this.startDate=startDate;
		this.closeDate=closeDate;
		this.deleteDate=deleteDate;
		this.warningDate=warningDate;
		this.allUsersAllowed=allUsersAllowed;
		this.users=users;
		this.userGroups=userGroups;
	}
	
	public Question getQuestion()
	{
		return question;
	}
	
	public void setQuestion(Question question)
	{
		this.question=question;
	}
	
	public User getPublisher()
	{
		return publisher;
	}
	
	public void setPublisher(User publisher)
	{
		this.publisher=publisher;
	}
	
	public Date getReleaseDate()
	{
		return releaseDate;
	}
	
	public void setReleaseDate(Date releaseDate)
	{
		this.releaseDate=releaseDate;
	}
	
	public boolean isAllUsersAllowed()
	{
		return allUsersAllowed;
	}
	
	public void setAllUsersAllowed(boolean allUsersAllowed)
	{
		this.allUsersAllowed=allUsersAllowed;
	}
	
	public Date getStartDate()
	{
		return startDate;
	}
	
	public void setStartDate(Date startDate)
	{
		this.startDate=startDate;
	}
	
	public Date getCloseDate()
	{
		return closeDate;
	}
	
	public void setCloseDate(Date closeDate)
	{
		this.closeDate=closeDate;
	}
	
	public Date getWarningDate()
	{
		return warningDate;
	}
	
	public void setWarningDate(Date warningDate)
	{
		this.warningDate=warningDate;
	}
	
	public Date getDeleteDate()
	{
		return deleteDate;
	}
	
	public void setDeleteDate(Date deleteDate)
	{
		this.deleteDate=deleteDate;
	}
	
	public List<User> getUsers()
	{
		return users;
	}
	
	public void setUsers(List<User> users)
	{
		this.users=users;
	}
	
	public List<String> getUserGroups()
	{
		return userGroups;
	}
	
	public void setUserGroups(List<String> userGroups)
	{
		this.userGroups=userGroups;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		return obj instanceof QuestionRelease && question.equals(((QuestionRelease)obj).question);
	}
}
