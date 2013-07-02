package es.uned.lsi.gepec.web.backbeans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import es.uned.lsi.gepec.model.entities.User;
import es.uned.lsi.gepec.util.HibernateUtil.Operation;
import es.uned.lsi.gepec.web.services.UserSessionService;
import es.uned.lsi.gepec.web.services.UsersService;

/**
 * Backbean for an user or a group of a test.
 */
@SuppressWarnings("serial")
public class UserGroupBean implements Serializable
{
	private final static int GROUPS_SHORT_NUMBER=3;
	private final static int NICKS_SHORT_NUMBER=3;
	private final static int NICKS_LONG_NUMBER=50;
	
	private UsersService usersService;
	private UserSessionService userSessionService;
	
	private User user;
	private List<String> userGroups;
	private String group;
	private List<User> groupUsers;
	
	public UserGroupBean(User user)
	{
		this.usersService=null;
		this.userSessionService=null;
		this.user=user;
		this.userGroups=null;
		this.group=null;
		this.groupUsers=null;
	}
	
	public UserGroupBean(UsersService usersService,UserSessionService userSessionService,String group)
	{
		this.usersService=usersService;
		this.userSessionService=userSessionService;
		this.user=null;
		this.userGroups=null;
		this.group=group;
		this.groupUsers=null;
	}
	
	public User getUser()
	{
		return user;
	}
	
	public String getGroup()
	{
		return group;
	}
	
	public boolean isTestUser()
	{
		return user!=null;
	}
	
	public String getNickShort()
	{
		StringBuffer nickShort=null;
		if (isTestUser())
		{
			nickShort=new StringBuffer(getUser().getNick());
		}
		else
		{
			if (groupUsers==null)
			{
				initializeGroupUsers();
			}
			nickShort=new StringBuffer();
			for (int i=0;i<NICKS_SHORT_NUMBER && i<groupUsers.size();i++)
			{
				User groupUser=groupUsers.get(i);
				if (nickShort.length()>0)
				{
					nickShort.append(", ");
				}
				else
				{
					nickShort.append('(');
				}
				nickShort.append(groupUser.getNick());
			}
			if (nickShort.length()>0)
			{
				if (groupUsers.size()>NICKS_SHORT_NUMBER)
				{
					nickShort.append(", ...");
				}
				nickShort.append(')');
			}
		}
		return nickShort==null?"":nickShort.toString();
	}
	
	public String getNickLong()
	{
		StringBuffer nickLong=null;
		if (!isTestUser())
		{
			if (groupUsers==null)
			{
				initializeGroupUsers();
			}
			nickLong=new StringBuffer();
			for (int i=0;i<NICKS_LONG_NUMBER && i<groupUsers.size();i++)
			{
				User groupUser=groupUsers.get(i);
				if (nickLong.length()>0)
				{
					nickLong.append(", ");
				}
				else
				{
					nickLong.append('(');
				}
				nickLong.append(groupUser.getNick());
			}
			if (nickLong.length()>0)
			{
				if (groupUsers.size()>NICKS_LONG_NUMBER)
				{
					nickLong.append(", ...");
				}
				nickLong.append(')');
			}
			else
			{
				nickLong=null;
			}
		}
		return nickLong==null?null:nickLong.toString();
	}
	
	public String getGroupShort()
	{
		StringBuffer groupShort=null;
		if (isTestUser())
		{
			if (userGroups==null)
			{
				initializeUserGroups();
			}
			groupShort=new StringBuffer();
			for (int i=0;i<GROUPS_SHORT_NUMBER && i<userGroups.size();i++)
			{
				String userGroup=userGroups.get(i);
				if (groupShort.length()>0)
				{
					groupShort.append(", ");
				}
				else
				{
					groupShort.append('(');
				}
				groupShort.append(userGroup);
			}
			if (groupShort.length()>0)
			{
				if (userGroups.size()>GROUPS_SHORT_NUMBER)
				{
					groupShort.append(", ...");
				}
				groupShort.append(')');
			}
		}
		else
		{
			groupShort=new StringBuffer(getGroup());
		}
		return groupShort==null?"":groupShort.toString();
	}
	
	public String getGroupLong()
	{
		StringBuffer groupLong=null;
		if (isTestUser())
		{
			if (userGroups==null)
			{
				initializeUserGroups();
			}
			groupLong=new StringBuffer();
			for (String userGroup:userGroups)
			{
				if (groupLong.length()>0)
				{
					groupLong.append(", ");
				}
				else
				{
					groupLong.append('(');
				}
				groupLong.append(userGroup);
			}
			if (groupLong.length()>0)
			{
				groupLong.append(')');
			}
			else
			{
				groupLong=null;
			}
		}
		return groupLong==null?null:groupLong.toString();
	}
	
	public String getRole()
	{
		String role=null;
		if (isTestUser() && user.getUserType()!=null)
		{
			role=user.getUserType().getType();
		}
		return role==null?"":role;
	}
	
	public String getRoleDescription()
	{
		String roleDescription=null;
		if (isTestUser() && user.getUserType()!=null)
		{
			roleDescription=user.getUserType().getDescription();
		}
		return "".equals(roleDescription)?null:roleDescription;
	}
	
	public boolean isGepeqUser()
	{
		boolean gepeqUser=false;
		if (isTestUser())
		{
			gepeqUser=user.isGepeqUser();
		}
		return gepeqUser;
	}
	
	/**
	 * Initializes user groups as a list.
	 */
	private void initializeUserGroups()
	{
		userGroups=new ArrayList<String>();
		if (user.getGroups()!=null && !"".equals(user.getGroups()))
		{
			for (String userGroup:user.getGroups().split(";"))
			{
				userGroups.add(userGroup);
			}
			Collections.sort(userGroups);
		}
	}
	
	/**
	 * Initialize group users as a list.
	 */
	private void initializeGroupUsers()
	{
		groupUsers=new ArrayList<User>();
		if (usersService!=null)
		{
			Operation operation=null;
			if (userSessionService!=null)
			{
				operation=userSessionService.getCurrentUserOperation();
			}
			groupUsers=usersService.getUsersWithGroup(operation,getGroup());
			Collections.sort(groupUsers,new Comparator<User>()
			{
				@Override
				public int compare(User u1,User u2)
				{
					return u1.getNick().compareTo(u2.getNick());
				}
			});
		}
	}
	
	@Override
	public boolean equals(Object obj)
	{
		boolean ok=false;
		if (obj instanceof UserGroupBean)
		{
			if (user!=null)
			{
				ok=user.equals(((UserGroupBean)obj).user);
			}
			else if (group!=null)
			{
				ok=group.equals(((UserGroupBean)obj).group);
			}
		}
		return ok;
	}
}
