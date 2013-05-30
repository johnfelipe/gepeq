/*
 * Generated, Do Not Modify
 */
/*
 * Copyright 2010 Prime Technology.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.primefaces.component.growl;

import javax.faces.component.UIComponentBase;
import javax.faces.context.FacesContext;
import javax.faces.component.UINamingContainer;
import javax.el.ValueExpression;
import javax.el.MethodExpression;
import javax.faces.render.Renderer;
import java.io.IOException;
import javax.faces.component.UIComponent;
import javax.faces.event.AbortProcessingException;
import javax.faces.application.ResourceDependencies;
import javax.faces.application.ResourceDependency;
import java.util.List;
import java.util.ArrayList;

@ResourceDependencies({
	@ResourceDependency(library="primefaces", name="primefaces.css"),
	@ResourceDependency(library="primefaces", name="jquery/jquery.js"),
	@ResourceDependency(library="primefaces", name="primefaces.js")
})
public class Growl extends UIComponentBase implements org.primefaces.component.api.Widget,org.primefaces.component.api.AutoUpdatable {


	public static final String COMPONENT_TYPE = "org.primefaces.component.Growl";
	public static final String COMPONENT_FAMILY = "org.primefaces.component";
	private static final String DEFAULT_RENDERER = "org.primefaces.component.GrowlRenderer";
	private static final String OPTIMIZED_PACKAGE = "org.primefaces.component.";

	protected enum PropertyKeys {

		sticky
		,showSummary
		,showDetail
		,globalOnly
		,life
		,warnIcon
		,infoIcon
		,errorIcon
		,fatalIcon
		,autoUpdate
	    // UNED: 06-03-2013 - dballestin - Added support for a new attribute: 'itemClass' to be able to assign 
		//                                 a specific CSS class to growl messages
		,itemClass;

		String toString;

		PropertyKeys(String toString) {
			this.toString = toString;
		}

		PropertyKeys() {}

		public String toString() {
			return ((this.toString != null) ? this.toString : super.toString());
}
	}

	public Growl() {
		setRendererType(DEFAULT_RENDERER);
	}

	public String getFamily() {
		return COMPONENT_FAMILY;
	}

	public boolean isSticky() {
		return (java.lang.Boolean) getStateHelper().eval(PropertyKeys.sticky, false);
	}
	public void setSticky(boolean _sticky) {
		getStateHelper().put(PropertyKeys.sticky, _sticky);
		handleAttribute("sticky", _sticky);
	}

	public boolean isShowSummary() {
		return (java.lang.Boolean) getStateHelper().eval(PropertyKeys.showSummary, true);
	}
	public void setShowSummary(boolean _showSummary) {
		getStateHelper().put(PropertyKeys.showSummary, _showSummary);
		handleAttribute("showSummary", _showSummary);
	}

	public boolean isShowDetail() {
		return (java.lang.Boolean) getStateHelper().eval(PropertyKeys.showDetail, false);
	}
	public void setShowDetail(boolean _showDetail) {
		getStateHelper().put(PropertyKeys.showDetail, _showDetail);
		handleAttribute("showDetail", _showDetail);
	}

	public boolean isGlobalOnly() {
		return (java.lang.Boolean) getStateHelper().eval(PropertyKeys.globalOnly, false);
	}
	public void setGlobalOnly(boolean _globalOnly) {
		getStateHelper().put(PropertyKeys.globalOnly, _globalOnly);
		handleAttribute("globalOnly", _globalOnly);
	}

	public int getLife() {
		return (java.lang.Integer) getStateHelper().eval(PropertyKeys.life, 6000);
	}
	public void setLife(int _life) {
		getStateHelper().put(PropertyKeys.life, _life);
		handleAttribute("life", _life);
	}

	public java.lang.String getWarnIcon() {
		return (java.lang.String) getStateHelper().eval(PropertyKeys.warnIcon, null);
	}
	public void setWarnIcon(java.lang.String _warnIcon) {
		getStateHelper().put(PropertyKeys.warnIcon, _warnIcon);
		handleAttribute("warnIcon", _warnIcon);
	}

	public java.lang.String getInfoIcon() {
		return (java.lang.String) getStateHelper().eval(PropertyKeys.infoIcon, null);
	}
	public void setInfoIcon(java.lang.String _infoIcon) {
		getStateHelper().put(PropertyKeys.infoIcon, _infoIcon);
		handleAttribute("infoIcon", _infoIcon);
	}

	public java.lang.String getErrorIcon() {
		return (java.lang.String) getStateHelper().eval(PropertyKeys.errorIcon, null);
	}
	public void setErrorIcon(java.lang.String _errorIcon) {
		getStateHelper().put(PropertyKeys.errorIcon, _errorIcon);
		handleAttribute("errorIcon", _errorIcon);
	}

	public java.lang.String getFatalIcon() {
		return (java.lang.String) getStateHelper().eval(PropertyKeys.fatalIcon, null);
	}
	public void setFatalIcon(java.lang.String _fatalIcon) {
		getStateHelper().put(PropertyKeys.fatalIcon, _fatalIcon);
		handleAttribute("fatalIcon", _fatalIcon);
	}

	public boolean isAutoUpdate() {
		return (java.lang.Boolean) getStateHelper().eval(PropertyKeys.autoUpdate, false);
	}
	public void setAutoUpdate(boolean _autoUpdate) {
		getStateHelper().put(PropertyKeys.autoUpdate, _autoUpdate);
		handleAttribute("autoUpdate", _autoUpdate);
	}

    // UNED: 06-03-2013 - dballestin - Added support for a new attribute: 'itemClass' to be able to assign 
	//                                 a specific CSS class to growl messages
	public java.lang.String getItemClass() {
		return (java.lang.String) getStateHelper().eval(PropertyKeys.itemClass, null);
	}
	public void setItemClass(java.lang.String _itemClass) {
		getStateHelper().put(PropertyKeys.itemClass, _itemClass);
		handleAttribute("itemClass", _itemClass);
	}
    // UNED: 06-03-2013 - dballestin - END - Added support for a new attribute: 'itemClass' to be able to assign 
	//                                 a specific CSS class to growl messages

	static final String WARN_ICON = "growl/images/warn.png";
	static final String ERROR_ICON = "growl/images/error.png";
	static final String INFO_ICON = "growl/images/info.png";
	static final String FATAL_ICON = "growl/images/fatal.png";

	protected FacesContext getFacesContext() {
		return FacesContext.getCurrentInstance();
	}
	public String resolveWidgetVar() {
		FacesContext context = FacesContext.getCurrentInstance();
		String userWidgetVar = (String) getAttributes().get("widgetVar");

		if(userWidgetVar != null)
			return userWidgetVar;
		 else
			return "widget_" + getClientId(context).replaceAll("-|" + UINamingContainer.getSeparatorChar(context), "_");
	}

	public void handleAttribute(String name, Object value) {
		List<String> setAttributes = (List<String>) this.getAttributes().get("javax.faces.component.UIComponentBase.attributesThatAreSet");
		if(setAttributes == null) {
			String cname = this.getClass().getName();
			if(cname != null && cname.startsWith(OPTIMIZED_PACKAGE)) {
				setAttributes = new ArrayList<String>(6);
				this.getAttributes().put("javax.faces.component.UIComponentBase.attributesThatAreSet", setAttributes);
			}
		}
		if(setAttributes != null) {
			if(value == null) {
				ValueExpression ve = getValueExpression(name);
				if(ve == null) {
					setAttributes.remove(name);
				} else if(!setAttributes.contains(name)) {
					setAttributes.add(name);
				}
			}
		}
	}
}