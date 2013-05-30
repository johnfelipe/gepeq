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

import es.uned.lsi.gepec.model.entities.Permission;

/**
 * Backbean for a permission.
 */
@SuppressWarnings("serial")
public class PermissionBean implements Serializable
{
	public final static int DEFAULT_VALUE=0;
	public final static int USER_TYPE_PERMISSION_VALUE=1;
	public final static int USER_PERMISSION_VALUE=2;
	public final static int LIMITED_PERMISSION_VALUE=3;
	
	private Permission permission;
	private int valueType;
	private String value;
	private String confirmedValue;
	
	public PermissionBean(Permission permission,int valueType,String value)
	{
		this.permission=permission;
		this.valueType=valueType;
		this.value=value;
		this.confirmedValue=value;
	}
	
	public PermissionBean(PermissionBean otherPermission)
	{
		this.permission=otherPermission.permission;
		this.valueType=otherPermission.valueType;
		this.value=otherPermission.value;
		this.confirmedValue=otherPermission.confirmedValue;
	}
	
	public Permission getPermission()
	{
		return permission;
	}
	
	public void setPermission(Permission permission)
	{
		this.permission=permission;
	}
	
	public int getValueType()
	{
		return valueType;
	}
	
	public void setValueType(int valueType)
	{
		this.valueType=valueType;
	}
	
	public String getValue()
	{
		return value;
	}
	
	public void setValue(String value)
	{
		this.value=value;
	}
	
	public String getConfirmedValue()
	{
		return confirmedValue;
	}
	
	public void setConfirmedValue(String confirmedValue)
	{
		this.confirmedValue=confirmedValue;
	}
	
	public String getValueTypeStr()
	{
		String valueTypeStr="";
		if (getValueType()==DEFAULT_VALUE)
		{
			valueTypeStr="PERMISSION_VALUE_TYPE_DEFAULT";
		}
		else if (getValueType()==USER_TYPE_PERMISSION_VALUE)
		{
			valueTypeStr="PERMISSION_VALUE_TYPE_USERTYPE";
		}
		else if (getValueType()==USER_PERMISSION_VALUE)
		{
			valueTypeStr="PERMISSION_VALUE_TYPE_USER";
		}
		else if (getValueType()==LIMITED_PERMISSION_VALUE)
		{
			valueTypeStr="PERMISSION_VALUE_TYPE_LIMITED";
		}
		return valueTypeStr;
	}
}
