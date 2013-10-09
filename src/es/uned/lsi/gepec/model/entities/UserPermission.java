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

//Generated 09-jul-2012 14:56:59 by Hibernate Tools 3.4.0.CR1

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * UserPermission generated by hbm2java
 */
@SuppressWarnings("serial")
@Entity
@SequenceGenerator(name="userpermissions_id_seq", sequenceName="userpermissions_id_seq") 
@Table(name = "userpermissions", schema = "public")
public class UserPermission implements java.io.Serializable {

	private long id;
	private User user;
	private Permission permission;
	private String value;

	public UserPermission() {
	}

	public UserPermission(long id, User user, Permission permission,
			String value) {
		this.id = id;
		this.user = user;
		this.permission = permission;
		this.value = value;
	}

	@Id
	@GeneratedValue(generator="userpermissions_id_seq", strategy=GenerationType.AUTO)
	@Column(name = "id", unique = true, nullable = false)
	public long getId() {
		return this.id;
	}

	public void setId(long id) {
		this.id = id;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_user")
	public User getUser() {
		return this.user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_permission")
	public Permission getPermission() {
		return this.permission;
	}

	public void setPermission(Permission permission) {
		this.permission = permission;
	}

	@Column(name = "value", nullable = false)
	public String getValue() {
		return this.value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * Set the fields of this permission of an user with the values from fields from other permision of an user.
	 * @param otherUserPermission Other permission of an user
	 */
	@Transient
	public void setFromOtherUserPermission(UserPermission otherUserPermission)
	{
		if (otherUserPermission!=null)
		{
			setId(otherUserPermission.getId());
			setUser(otherUserPermission.getUser());
			setPermission(otherUserPermission.getPermission());
			setValue(otherUserPermission.getValue());
		}
	}
	
	/**
	 * @return A copy of this permission of an user.
	 */
	@Transient
	public UserPermission getUserPermissionCopy()
	{
		return new UserPermission(getId(),getUser(),getPermission(),getValue());
	}
	
	@Override
	public boolean equals(Object obj)
	{
		boolean ok=false;
		if (obj instanceof UserPermission)
		{
			if (getId()==0L)
			{
				ok=((UserPermission)obj).getId()==0L && getUser().equals(((UserPermission)obj).getUser()) &&
					getPermission().equals(((UserPermission)obj).getPermission());
			}
			else
			{
				ok=getId()==((UserPermission)obj).getId();
			}
		}
		return ok;
	}
}
