package org.adempiere.webui.process;

import java.util.Collection;

import org.compiere.model.MBPartner;
import org.compiere.model.MProduct;
import org.compiere.model.Query;
import org.compiere.process.SvrProcess;


public class TestInfoWindowProcess extends SvrProcess {

	@Override
	protected void prepare() {
		;
	}

	@Override
	protected String doIt() throws Exception {

		String whereClause = "EXISTS (SELECT T_Selection_ID FROM T_Selection WHERE T_Selection.AD_PInstance_ID=? " +
							"AND T_Selection.T_Selection_ID = M_Product.M_Product_ID)";

		Collection<MProduct> corps = new Query(getCtx(), MProduct.Table_Name, whereClause, get_TrxName())
									.setClient_ID()
									.setParameters(new Object[]{getAD_PInstance_ID()})
									.list();

		int bpNum = 0;
		for(MProduct corp : corps)
		{

		}

		return "Corporations = " + corps.size() + " - Update Gross Business Partner Num = "+ bpNum ;
	}

}
