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

import java.util.Properties;

import org.adempiere.webui.component.FWindow;
import org.compiere.util.Env;

/**
 *
 * @author  <a href="mailto:agramdass@gmail.com">Ashley G Ramdass</a>
 * @date    Feb 25, 2007
 * @version $Revision: 0.10 $
 * @author <a href="mailto:sendy.yagambrum@posterita.org">Sendy Yagambrum</a>
 * @author Hideaki Hagiwara(h.hagiwara@oss-erp.co.jp)
 * @date    July 18, 2007
 */
public class JPiereErrorPageWindow extends FWindow
{
	/**
	 *
	 */
	private static final long serialVersionUID = -5169830531440825871L;

    protected Properties ctx;
    protected JPiereErrorPagePanel pnlLogin;
    public JPiereErrorPageWindow() {}

    public void init()
    {
    	this.ctx = Env.getCtx();
        initComponents();
        this.appendChild(pnlLogin);
        this.setStyle("background-color: transparent");
        setWidgetListener("onOK", "zAu.cmd0.showBusy(null)");
    }



    private void initComponents()
    {
		pnlLogin = new JPiereErrorPagePanel(ctx, this);
	}

}
