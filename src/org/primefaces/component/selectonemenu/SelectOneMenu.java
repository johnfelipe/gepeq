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
package org.primefaces.component.selectonemenu;

import javax.faces.component.html.HtmlSelectOneMenu;
import javax.faces.context.FacesContext;
import javax.faces.component.UINamingContainer;
import javax.el.ValueExpression;
import javax.faces.component.UIComponent;
import javax.faces.application.ResourceDependencies;
import javax.faces.application.ResourceDependency;

import java.util.List;
import java.util.ArrayList;

import org.primefaces.component.column.Column;

@ResourceDependencies({
	@ResourceDependency(library="primefaces", name="primefaces.css"),
	@ResourceDependency(library="primefaces", name="jquery/jquery.js"),
	@ResourceDependency(library="primefaces", name="primefaces.js")
})
public class SelectOneMenu extends HtmlSelectOneMenu implements org.primefaces.component.api.Widget {


	public static final String COMPONENT_TYPE = "org.primefaces.component.SelectOneMenu";
	public static final String COMPONENT_FAMILY = "org.primefaces.component";
	private static final String DEFAULT_RENDERER = "org.primefaces.component.SelectOneMenuRenderer";
	private static final String OPTIMIZED_PACKAGE = "org.primefaces.component.";

	protected enum PropertyKeys {

		widgetVar
		,effect
		,effectDuration
		,var
		,height
	    // UNED: 05-06-2012 - dballestin - Added support for two new attributes: 'panelStyleClass', 'panelStyle' 
		//                                 allowing to define a custom style class and/or style for the panel of a 
		//                                 <p:selectonemenu> component
		,panelStyleClass
		,panelStyle
		// UNED: 26-08-2013 - dballestin - Added support for a new attribute: 'disabledValueValidation' allowing
		//                                 to disable value validation for a <p:selectonemenu> component
		,disabledValueValidation;

		String toString;

		PropertyKeys(String toString) {
			this.toString = toString;
		}

		PropertyKeys() {}

		public String toString() {
			return ((this.toString != null) ? this.toString : super.toString());
		}
	}

	public SelectOneMenu() {
		setRendererType(DEFAULT_RENDERER);
	}

	public String getFamily() {
		return COMPONENT_FAMILY;
	}

	public java.lang.String getWidgetVar() {
		return (java.lang.String) getStateHelper().eval(PropertyKeys.widgetVar, null);
	}
	public void setWidgetVar(java.lang.String _widgetVar) {
		getStateHelper().put(PropertyKeys.widgetVar, _widgetVar);
		handleAttribute("widgetVar", _widgetVar);
	}

	public java.lang.String getEffect() {
		return (java.lang.String) getStateHelper().eval(PropertyKeys.effect, "blind");
	}
	public void setEffect(java.lang.String _effect) {
		getStateHelper().put(PropertyKeys.effect, _effect);
		handleAttribute("effect", _effect);
	}

	public int getEffectDuration() {
		return (java.lang.Integer) getStateHelper().eval(PropertyKeys.effectDuration, 400);
	}
	public void setEffectDuration(int _effectDuration) {
		getStateHelper().put(PropertyKeys.effectDuration, _effectDuration);
		handleAttribute("effectDuration", _effectDuration);
	}

	public java.lang.String getVar() {
		return (java.lang.String) getStateHelper().eval(PropertyKeys.var, null);
	}
	public void setVar(java.lang.String _var) {
		getStateHelper().put(PropertyKeys.var, _var);
		handleAttribute("var", _var);
	}

	public int getHeight() {
		return (java.lang.Integer) getStateHelper().eval(PropertyKeys.height, java.lang.Integer.MAX_VALUE);
	}
	public void setHeight(int _height) {
		getStateHelper().put(PropertyKeys.height, _height);
		handleAttribute("height", _height);
	}

    // UNED: 05-06-2012 - dballestin - Added support for two new attributes: 'panelStyleClass', 'panelStyle' 
	//                                 allowing to define a custom style class and/or style for the panel of a 
	//                                 <p:selectonemenu> component
	public java.lang.String getPanelStyleClass() {
		return (java.lang.String) getStateHelper().eval(PropertyKeys.panelStyleClass, null);
	}
	public void setPanelStyleClass(java.lang.String _panelStyleClass) {
		getStateHelper().put(PropertyKeys.panelStyleClass, _panelStyleClass);
		handleAttribute("panelStyleClass", _panelStyleClass);
	}
	
	public java.lang.String getPanelStyle() {
		return (java.lang.String) getStateHelper().eval(PropertyKeys.panelStyle, null);
	}
	public void setPanelStyle(java.lang.String _panelStyle) {
		getStateHelper().put(PropertyKeys.panelStyle, _panelStyle);
		handleAttribute("panelStyle", _panelStyle);
	}
    // UNED: 05-06-2012 - dballestin - END - Added support for two new attributes: 'panelStyleClass', 'panelStyle' 
	//                                 allowing to define a custom style class and/or style for the panel of a 
	//                                 <p:selectonemenu> component


	// UNED: 26-08-2013 - dballestin - Added support for a new attribute: 'disabledValueValidation' allowing
	//                                 to disable value validation for a <p:selectonemenu> component
	public boolean isDisabledValueValidation() {
		return (java.lang.Boolean) getStateHelper().eval(PropertyKeys.disabledValueValidation, false);
	}
	public void setDisabledValueValidation(boolean _disabledValueValidation) {
		getStateHelper().put(PropertyKeys.disabledValueValidation, _disabledValueValidation);
		handleAttribute("disabledValueValidation", _disabledValueValidation);
	}
	// UNED: 26-08-2013 - dballestin - END - Added support for a new attribute: 'disabledValueValidation' allowing
	//                                 to disable value validation for a <p:selectonemenu> component
	
    public final static String STYLE_CLASS = "ui-selectonemenu ui-widget ui-state-default ui-corner-all ui-helper-clearfix";
    public final static String LABEL_CONTAINER_CLASS = "ui-selectonemenu-label-container";
    public final static String LABEL_CLASS = "ui-selectonemenu-label ui-corner-all";
    public final static String TRIGGER_CLASS = "ui-selectonemenu-trigger ui-state-default ui-corner-right";
    public final static String PANEL_CLASS = "ui-selectonemenu-panel ui-widget-content ui-corner-all ui-helper-hidden";
    public final static String LIST_CLASS = "ui-selectonemenu-items ui-selectonemenu-list ui-widget-content ui-widget ui-corner-all ui-helper-reset";
    public final static String TABLE_CLASS = "ui-selectonemenu-items ui-selectonemenu-table ui-widget-content ui-widget ui-corner-all ui-helper-reset";
    public final static String ITEM_CLASS = "ui-selectonemenu-item ui-selectonemenu-list-item ui-corner-all";
    public final static String ROW_CLASS = "ui-selectonemenu-item ui-selectonemenu-row ui-widget-content";

    public List<Column> getColums() {
        List<Column> columns = new ArrayList<Column>();
        
        for(UIComponent kid : this.getChildren()) {
            if(kid instanceof Column)
                columns.add((Column) kid);
        }

        return columns;
    }

    

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

	@SuppressWarnings("unchecked")
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

	@Override
	protected void validateValue(FacesContext context, Object value) {
		if (!isDisabledValueValidation())
		{
			super.validateValue(context, value);
		}
	}
}