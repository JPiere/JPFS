/******************************************************************************
 * Product: JPiere(ジェイピエール) - JPiere Fragments                         *
 * Copyright (C) Hideaki Hagiwara All Rights Reserved.                        *
 * このプログラムはGNU Gneral Public Licens Version2のもと公開しています。    *
 * このプログラムは自由に活用してもらう事を期待して公開していますが、         *
 * いかなる保証もしていません。                                               *
 * 著作権は萩原秀明(h.hagiwara@oss-erp.co.jp)が保持し、サポートサービスは     *
 * 株式会社オープンソース・イーアールピー・ソリューションズで                 *
 * 提供しています。サポートをご希望の際には、                                 *
 * 株式会社オープンソース・イーアールピー・ソリューションズまでご連絡下さい。 *
 * http://www.oss-erp.co.jp/                                                  *
 *****************************************************************************/
package org.adempiere.webui.apps.form;

import org.adempiere.webui.panel.ADForm;
import org.adempiere.webui.panel.CustomForm;
import org.adempiere.webui.panel.IFormController;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;

public class HasADForm implements IFormController{

	private CustomForm form;

    public HasADForm()
    {
    	form = new CustomForm();

    	Component component = Executions.createComponents("zul/iframe.zul", form, null);
    	form.appendChild(component);

//    	ADWindow adw = new ADWindow(Env.getCtx(),1000014,null);
//    	adw.createPart(form);

    }


	@Override
	public ADForm getForm()
	{
		return form;
	}
}
