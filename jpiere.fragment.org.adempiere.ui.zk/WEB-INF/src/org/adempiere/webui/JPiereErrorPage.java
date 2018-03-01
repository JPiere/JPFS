/******************************************************************************
 * Product: JPiere                                                            *
 * Copyright (C) Hideaki Hagiwara (h.hagiwara@oss-erp.co.jp)                  *
 *                                                                            *
 * This program is free software, you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY.                          *
 * See the GNU General Public License for more details.                       *
 *                                                                            *
 * JPiere is maintained by OSS ERP Solutions Co., Ltd.                        *
 * (http://www.oss-erp.co.jp)                                                 *
 *****************************************************************************/

package org.adempiere.webui;

import org.adempiere.webui.part.AbstractUIPart;
import org.adempiere.webui.theme.ThemeManager;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.metainfo.PageDefinition;
import org.zkoss.zul.Borderlayout;

/**
 *
 * @author  <a href="mailto:agramdass@gmail.com">Ashley G Ramdass</a>
 * @author  Low Heng Sin
 * @date    Mar 3, 2007
 * @version $Revision: 0.10 $
 * @author Hideaki Hagiwara(h.hagiwara@oss-erp.co.jp)
 */
public class JPiereErrorPage extends AbstractUIPart
{
	private Borderlayout layout;

    public JPiereErrorPage()
    {
    	;
    }

    protected Component doCreatePart(Component parent)
    {
    	PageDefinition pageDefintion = Executions.getCurrent().getPageDefinition(ThemeManager.getThemeResource("zul/error/error.zul"));
    	Component loginPage = Executions.createComponents(pageDefintion, parent, null);

        layout = (Borderlayout) loginPage.getFellow("layout");

        JPiereErrorPageWindow loginWindow = (JPiereErrorPageWindow) loginPage.getFellow("loginWindow");
        loginWindow.init();

        return layout;
    }


	public Component getComponent() {
		return layout;
	}

}
