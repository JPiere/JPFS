package org.adempiere.webui.apps.form;

import static org.compiere.model.SystemIDs.*;

import org.adempiere.webui.component.Label;
import org.adempiere.webui.component.Panel;
import org.adempiere.webui.editor.WSearchEditor;
import org.adempiere.webui.panel.ADForm;
import org.compiere.model.MLookup;
import org.compiere.model.MLookupFactory;
import org.compiere.util.DisplayType;
import org.compiere.util.Env;
import org.compiere.util.Msg;
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

		
		Label label_BP = new Label(Msg.translate(Env.getCtx(), "C_BPartner_ID"));
		centerPanel.appendChild(label_BP);
		
		int AD_Column_ID = COLUMN_C_INVOICE_C_BPARTNER_ID;        //  C_Invoice.C_BPartner_ID
		MLookup lookupBP = MLookupFactory.get (Env.getCtx(), this.getWindowNo(), 0, AD_Column_ID, DisplayType.Search);
		WSearchEditor search_BP = new WSearchEditor("C_BPartner_ID", true, false, true, lookupBP);
    	centerPanel.appendChild(search_BP.getComponent());
		
	}
}
