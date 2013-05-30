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
package es.uned.lsi.gepec.web.helper;

import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

import es.uned.lsi.gepec.model.entities.Category;

@SuppressWarnings("serial")
public class CategoryTreeNode extends DefaultTreeNode
{
	// We use our own "expanded" flag because DefaultTreeNode collapse all parent nodes when we collapse a single
	// node and we don't like that behaviour
	private boolean expanded;
	
	public CategoryTreeNode(String type,Category category,TreeNode parent)
	{
		super(type,category,parent);
		expanded=false;
	}
	
	public CategoryTreeNode(Category category,TreeNode parent)
	{
		super(category,parent);
		expanded=false;
	}
	
	public Category getCategory()
	{
		Object data=getData();
		return data==null?null:data instanceof Category?(Category)data:null;
	}
	
	public void setCategory(Category category)
	{
		setData(category);
	}
	
	@Override
	public boolean isExpanded()
	{
		return expanded;
	}
	
	@Override
	public void setExpanded(boolean expanded)
	{
		this.expanded=expanded;
	}
	
	public void expandFromRoot()
	{
		this.expanded=true;
		if (getParent()!=null)
		{
			((CategoryTreeNode)getParent()).expandFromRoot();
		}
	}
}
