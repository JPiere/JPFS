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

import org.adempiere.webui.theme.ThemeManager;
import org.zkoss.zul.Window;

/**
 *
 * @author  <a href="mailto:agramdass@gmail.com">Ashley G Ramdass</a>
 * @date    Feb 25, 2007
 * @version $Revision: 0.10 $
 *
 * @author hengsin
 * @author Hideaki Hagiwara(h.hagiwara@oss-erp.co.jp)
 */
public class JPiereWebUIErrorPage extends Window
{
	/**
	 *
	 */
	private static final long serialVersionUID = -3320656546509525766L;

	public static final String EXECUTION_CARRYOVER_SESSION_KEY = "execution.carryover";


    public JPiereWebUIErrorPage()
    {
    	this.setVisible(false);
//    	logout();
    }

    public void onCreate()
    {
        this.getPage().setTitle(ThemeManager.getBrowserTitle());

        JPiereErrorPage loginDesktop = new JPiereErrorPage();

    	loginDesktop.createPart(this.getPage());

    }


	/* (non-Javadoc)
	 * @see org.adempiere.webui.IWebClient#logout()
	 */
    public void logout()
    {
    	;//TODO ログアウト処理？
    }


}
