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

import org.adempiere.webui.component.Panel;
import org.adempiere.webui.panel.ADForm;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zul.Borderlayout;
import org.zkoss.zul.Center;

public class ExtendADForm extends ADForm{

	@Override
	protected void initForm() {
		Borderlayout contentPane = new Borderlayout();
		this.appendChild(contentPane);

		Center center = new Center();
		contentPane.appendChild(center);

		Panel centerPanel = new Panel();
		center.appendChild(centerPanel);

//		Component component = Executions.createComponents(ThemeManager.getThemeResource("zul/iframe.zul"), centerPanel, null);
		Component component = Executions.createComponents("zul/iframe.zul", centerPanel, null);

		centerPanel.appendChild(component);


//		Label label_BP = new Label(Msg.translate(Env.getCtx(), "C_BPartner_ID"));
//		centerPanel.appendChild(label_BP);
//
//		int AD_Column_ID = COLUMN_C_INVOICE_C_BPARTNER_ID;        //  C_Invoice.C_BPartner_ID
//		MLookup lookupBP = MLookupFactory.get (Env.getCtx(), this.getWindowNo(), 0, AD_Column_ID, DisplayType.Search);
//		WSearchEditor search_BP = new WSearchEditor("C_BPartner_ID", true, false, true, lookupBP);
//    	centerPanel.appendChild(search_BP.getComponent());

	}
}
