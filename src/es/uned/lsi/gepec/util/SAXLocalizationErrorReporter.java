package es.uned.lsi.gepec.util;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import javax.el.ELContext;
import javax.el.ELResolver;
import javax.faces.context.FacesContext;

import com.sun.org.apache.xerces.internal.impl.XMLErrorReporter;
import com.sun.org.apache.xerces.internal.xni.XMLLocator;
import com.sun.org.apache.xerces.internal.xni.XNIException;

import es.uned.lsi.gepec.web.LocaleBean;

public class SAXLocalizationErrorReporter extends XMLErrorReporter
{
	private String key=null;
	private Object[] arguments=null;
	
	private LocaleBean localeBean=null;
	
	/*
		// Get EL context
		ELContext elContext=context.getELContext();
		
		// Get EL resolver
		ELResolver resolver=context.getApplication().getELResolver();
		
		// We get UserSessionService from EL resolver
		UserSessionService userSessionService=(UserSessionService)resolver.getValue(elContext,null,"userSessionService");
	*/
	
	@Override
	public void reportError(String domain,String key,Object[] arguments, short severity) throws XNIException
	{
		this.key=key;
		this.arguments=arguments;
		super.reportError(domain,key,arguments,severity);
	}

	@Override
	public void reportError(XMLLocator location,String domain,String key,Object[] arguments,short severity)
		throws XNIException
	{
		this.key=key;
		this.arguments=arguments;
		super.reportError(location,domain,key,arguments,severity);
	}
	
	public String getLocalizedErrorMessage()
	{
		String localizedErrorMessage=null;
		if (key!=null)
		{
			Locale locale=null;
			initializeLocaleBean();
			if (localeBean==null)
			{
				locale=Locale.getDefault();
			}
			else
			{
				locale=new Locale(localeBean.getLangCode());
			}
			ResourceBundle rb=ResourceBundle.getBundle("es.uned.lsi.gepec.util.XMLMessages",locale);
			if (rb!=null)
			{
				localizedErrorMessage=rb.getString(key);
				if (localizedErrorMessage!=null && arguments!=null)
				{
					for (int i=0;i<arguments.length;i++)
					{
						StringBuffer argBracketed=new StringBuffer("{");
						argBracketed.append(i);
						argBracketed.append('}');
						String argument=arguments[i].toString();
						localizedErrorMessage=
							localizedErrorMessage.replaceAll(Pattern.quote(argBracketed.toString()),argument);
					}
				}
			}
		}
		return localizedErrorMessage;
	}
	
	private void initializeLocaleBean()
	{
		if (localeBean==null)
		{
			FacesContext context=FacesContext.getCurrentInstance();
			if (context!=null)
			{
				ELContext elContext=context.getELContext();
				ELResolver resolver=context.getApplication().getELResolver();
				if (elContext!=null && resolver!=null)
				{
					localeBean=(LocaleBean)resolver.getValue(elContext,null,"localeBean");
				}
			}
		}
	}
}
