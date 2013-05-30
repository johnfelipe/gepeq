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
package org.primefaces.component.wizard;

import javax.faces.component.UIComponent;
import javax.faces.component.UIComponentBase;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.component.UINamingContainer;
import javax.faces.validator.Validator;
import javax.el.ValueExpression;
import javax.faces.application.ResourceDependencies;
import javax.faces.application.ResourceDependency;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import org.primefaces.component.tabview.Tab;

@ResourceDependencies({
	@ResourceDependency(library="primefaces", name="primefaces.css"),
	@ResourceDependency(library="primefaces", name="jquery/jquery.js"),
	@ResourceDependency(library="primefaces", name="primefaces.js")
})
public class Wizard extends UIComponentBase implements org.primefaces.component.api.Widget {


	public static final String COMPONENT_TYPE = "org.primefaces.component.Wizard";
	public static final String COMPONENT_FAMILY = "org.primefaces.component";
	private static final String DEFAULT_RENDERER = "org.primefaces.component.WizardRenderer";
	private static final String OPTIMIZED_PACKAGE = "org.primefaces.component.";

	protected enum PropertyKeys {

		widgetVar
		,step
		,style
		,styleClass
		,flowListener
		,showNavBar
		,showStepStatus
		,onback
		,onnext
		,nextLabel
		,backLabel
		,backProcess
		,backValidate;

		String toString;

		PropertyKeys(String toString) {
			this.toString = toString;
		}

		PropertyKeys() {}

		public String toString() {
			return ((this.toString != null) ? this.toString : super.toString());
}
	}

	public Wizard() {
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

	public java.lang.String getStep() {
		return (java.lang.String) getStateHelper().eval(PropertyKeys.step, null);
	}
	public void setStep(java.lang.String _step) {
		getStateHelper().put(PropertyKeys.step, _step);
		handleAttribute("step", _step);
	}

	public java.lang.String getStyle() {
		return (java.lang.String) getStateHelper().eval(PropertyKeys.style, null);
	}
	public void setStyle(java.lang.String _style) {
		getStateHelper().put(PropertyKeys.style, _style);
		handleAttribute("style", _style);
	}

	public java.lang.String getStyleClass() {
		return (java.lang.String) getStateHelper().eval(PropertyKeys.styleClass, null);
	}
	public void setStyleClass(java.lang.String _styleClass) {
		getStateHelper().put(PropertyKeys.styleClass, _styleClass);
		handleAttribute("styleClass", _styleClass);
	}

	public javax.el.MethodExpression getFlowListener() {
		return (javax.el.MethodExpression) getStateHelper().eval(PropertyKeys.flowListener, null);
	}
	public void setFlowListener(javax.el.MethodExpression _flowListener) {
		getStateHelper().put(PropertyKeys.flowListener, _flowListener);
		handleAttribute("flowListener", _flowListener);
	}

	public boolean isShowNavBar() {
		return (java.lang.Boolean) getStateHelper().eval(PropertyKeys.showNavBar, true);
	}
	public void setShowNavBar(boolean _showNavBar) {
		getStateHelper().put(PropertyKeys.showNavBar, _showNavBar);
		handleAttribute("showNavBar", _showNavBar);
	}

	public boolean isShowStepStatus() {
		return (java.lang.Boolean) getStateHelper().eval(PropertyKeys.showStepStatus, true);
	}
	public void setShowStepStatus(boolean _showStepStatus) {
		getStateHelper().put(PropertyKeys.showStepStatus, _showStepStatus);
		handleAttribute("showStepStatus", _showStepStatus);
	}

	public java.lang.String getOnback() {
		return (java.lang.String) getStateHelper().eval(PropertyKeys.onback, null);
	}
	public void setOnback(java.lang.String _onback) {
		getStateHelper().put(PropertyKeys.onback, _onback);
		handleAttribute("onback", _onback);
	}

	public java.lang.String getOnnext() {
		return (java.lang.String) getStateHelper().eval(PropertyKeys.onnext, null);
	}
	public void setOnnext(java.lang.String _onnext) {
		getStateHelper().put(PropertyKeys.onnext, _onnext);
		handleAttribute("onnext", _onnext);
	}

	public java.lang.String getNextLabel() {
		return (java.lang.String) getStateHelper().eval(PropertyKeys.nextLabel, "Next");
	}
	public void setNextLabel(java.lang.String _nextLabel) {
		getStateHelper().put(PropertyKeys.nextLabel, _nextLabel);
		handleAttribute("nextLabel", _nextLabel);
	}

	public java.lang.String getBackLabel() {
		return (java.lang.String) getStateHelper().eval(PropertyKeys.backLabel, "Back");
	}
	public void setBackLabel(java.lang.String _backLabel) {
		getStateHelper().put(PropertyKeys.backLabel, _backLabel);
		handleAttribute("backLabel", _backLabel);
	}
	
	public boolean isBackProcess() {
		return (java.lang.Boolean) getStateHelper().eval(PropertyKeys.backProcess, false);
	}
	public void setBackProcess(boolean _backProcess) {
		getStateHelper().put(PropertyKeys.backProcess, _backProcess);
		handleAttribute("backProcess", _backProcess);
	}
	
	public boolean isBackValidate() {
		return (java.lang.Boolean) getStateHelper().eval(PropertyKeys.backValidate, false);
	}
	public void setBackValidate(boolean _backValidate) {
		getStateHelper().put(PropertyKeys.backValidate, _backValidate);
		handleAttribute("backValidate", _backValidate);
	}

	private Map<UIInput, Boolean> inputRequiredMap=null;
	private Map<UIInput, Validator[]> inputValidatorsMap=null;
	
	private void resetValidators() {
		if (inputRequiredMap==null) {
			inputRequiredMap = new HashMap<UIInput, Boolean>();
		} else {
			inputRequiredMap.clear();
		}
		if (inputValidatorsMap==null) {
			inputValidatorsMap = new HashMap<UIInput, Validator[]>();
		} else {
			inputValidatorsMap.clear();
		}
	}
	
	private void disableValidators(UIComponent component) {
		if (component instanceof UIInput) {
			boolean required = ((UIInput)component).isRequired();
			inputRequiredMap.put((UIInput)component, Boolean.valueOf(required));
			Validator[] inputValidators = ((UIInput)component).getValidators();
			inputValidatorsMap.put((UIInput)component, inputValidators);
			((UIInput)component).setRequired(false);
			for (Validator validator:inputValidators) {
				((UIInput)component).removeValidator(validator);
			}
		} else {
			for (UIComponent child:component.getChildren()) {
				disableValidators(child);
			}
		}
	}
	
	private void restoreValidators(UIComponent component) {
		if (component instanceof UIInput) {
			if (inputRequiredMap.containsKey(component)) {
				((UIInput)component).setRequired(inputRequiredMap.get(component).booleanValue());
			}
			if (inputValidatorsMap.containsKey(component)) {
				for (Validator validator:inputValidatorsMap.get(component)) {
					((UIInput)component).addValidator(validator);
				}
			}
		} else {
			for (UIComponent child:component.getChildren()) {
				restoreValidators(child);
			}
		}
	}

	private Tab tabToProcess;

	public void processDecodes(FacesContext facesContext) {
		if(isWizardRequest(facesContext)) {
                        String currentStep = facesContext.getExternalContext().getRequestParameterMap().get(getClientId(facesContext) + "_currentStep");
                        setStep(currentStep);

			if(!isBackProcess() && isBackRequest(facesContext)) {
				//If flow goes back and back processing is disabled, skip to rendering
				facesContext.renderResponse();
			} else {
				if (!isBackValidate() && isBackRequest(facesContext)) {
					Tab step=getStepToProcess(facesContext);
					
					// Disable validators
					resetValidators();
					disableValidators(step);
					
					// Process decodes now that validators are disabled
					step.processDecodes(facesContext);
					
					// Restore validators
					restoreValidators(step);
				} else {
					getStepToProcess(facesContext).processDecodes(facesContext);
				}
			}
			
		} else {
			super.processDecodes(facesContext);
		}
    }
	
	public void processValidators(FacesContext facesContext) {
		if(isWizardRequest(facesContext)) {
			
			if (!isBackValidate() && isBackRequest(facesContext)) {
				Tab step=getStepToProcess(facesContext);
				
				// Disable validators
				resetValidators();
				disableValidators(step);
				
				// Process validators now that they are disabled
				step.processValidators(facesContext);
				
				// Restore validators
				restoreValidators(step);
			} else {
				getStepToProcess(facesContext).processValidators(facesContext);
			}
		} else {
			super.processValidators(facesContext);
		}
    }
	
	public void processUpdates(FacesContext facesContext) {
		if(isWizardRequest(facesContext)) {
			getStepToProcess(facesContext).processUpdates(facesContext);
		} else {
			super.processUpdates(facesContext);
		}
	}
	
	public Tab getStepToProcess(FacesContext facesContext) {
		if(tabToProcess == null) {
			String currentStep = getStep();
			
			for(javax.faces.component.UIComponent child : getChildren()) {
				if(child.getId().equals(currentStep)) {
					tabToProcess = (Tab) child;
					
					break;
				}
			}
		}
		
		return tabToProcess;
	}
	
	public boolean isWizardRequest(FacesContext facesContext) {
		return facesContext.getExternalContext().getRequestParameterMap().containsKey(getClientId(facesContext) + "_wizardRequest");
	}
	
	public boolean isBackRequest(FacesContext facesContext) {
		return facesContext.getExternalContext().getRequestParameterMap().containsKey(getClientId(facesContext) + "_backRequest");
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
}