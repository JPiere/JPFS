package org.adempiere.webui.apps.form;

import org.adempiere.webui.adwindow.ADWindow;
import org.adempiere.webui.panel.ADForm;
import org.adempiere.webui.panel.CustomForm;
import org.adempiere.webui.panel.IFormController;
import org.compiere.util.Env;

public class HasADForm implements IFormController{

	private CustomForm form;

    public HasADForm()
    {
    	form = new CustomForm();

//    	Component component = Executions.createComponents("zul/iframe.zul", form, null);

    	ADWindow adw = new ADWindow(Env.getCtx(),1000014,null);
    	adw.createPart(form);

    }


	@Override
	public ADForm getForm()
	{
		return form;
	}
}
