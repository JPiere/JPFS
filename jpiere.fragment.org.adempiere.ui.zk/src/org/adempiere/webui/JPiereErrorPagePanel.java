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

import javax.servlet.http.HttpServletResponse;

import org.adempiere.webui.component.ConfirmPanel;
import org.adempiere.webui.component.Label;
import org.adempiere.webui.component.Window;
import org.adempiere.webui.theme.ITheme;
import org.adempiere.webui.window.FDialog;
import org.compiere.model.MSysConfig;
import org.zkoss.zhtml.Div;
import org.zkoss.zhtml.Table;
import org.zkoss.zhtml.Td;
import org.zkoss.zhtml.Tr;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;

/**
 *
 * @author  <a href="mailto:agramdass@gmail.com">Ashley G Ramdass</a>
 * @date    Feb 25, 2007
 * @version $Revision: 0.10 $
 * @author <a href="mailto:sendy.yagambrum@posterita.org">Sendy Yagambrum</a>
 * @author Hideaki Hagiwara(h.hagiwara@oss-erp.co.jp)
 * @date    July 18, 2007
 */
public class JPiereErrorPagePanel extends Window implements EventListener<Event>
{
	/**
	 *
	 */
	private static final long serialVersionUID = -3361823499124119753L;

	private static final String ON_LOAD_TOKEN = "onLoadToken";

    protected Properties ctx;
    protected JPiereErrorPageWindow wndLogin;
    protected ConfirmPanel pnlButtons;

    public JPiereErrorPagePanel(Properties ctx, JPiereErrorPageWindow loginWindow)
    {
        this.ctx = ctx;
        this.wndLogin = loginWindow;
        init();
        this.setId("loginPanel");
        this.setSclass("login-box");

        Events.echoEvent(ON_LOAD_TOKEN, this, null);
        this.addEventListener(ON_LOAD_TOKEN, this);
    }

    private void init()
    {
		Div div = new Div();
    	div.setSclass(ITheme.LOGIN_BOX_HEADER_CLASS);
    	this.appendChild(div);

    	div = new Div();
    	div.setSclass(ITheme.LOGIN_BOX_HEADER_CLASS);
    	this.appendChild(div);


    	HttpServletResponse response = (HttpServletResponse)Executions.getCurrent().getNativeResponse();
    	int statusCode = response.getStatus();

    	/** JPiere Logo */
    	Table table = new Table();
    	table.setId("grdLogin");
    	table.setDynamicProperty("cellpadding", "0");
    	table.setDynamicProperty("cellspacing", "5");
    	table.setSclass(ITheme.LOGIN_BOX_BODY_CLASS);

    	this.appendChild(table);

    	Tr tr = new Tr();
    	table.appendChild(tr);
    	tr.setStyle("height:100px");
    	Td td = new Td();
    	td.setSclass(ITheme.LOGIN_BOX_HEADER_LOGO_CLASS);
    	tr.appendChild(td);
    	td.setDynamicProperty("colspan", "2");
    	org.adempiere.webui.component.Label error = new org.adempiere.webui.component.Label("Status Code");
    	error.setStyle("font-size: 48px; color:#003894;");
        td.appendChild(error);

        tr = new Tr();
    	table.appendChild(tr);
    	tr.setStyle("height:100px");
    	td = new Td();
    	td.setSclass(ITheme.LOGIN_BOX_HEADER_LOGO_CLASS);
    	tr.appendChild(td);
    	td.setDynamicProperty("colspan", "2");
    	Label status = new Label(String.valueOf(statusCode));
    	status.setStyle("font-size: 80px; color:#003894;");
        td.appendChild(status);

       	div = new Div();
    	div.setSclass(ITheme.LOGIN_BOX_FOOTER_CLASS);
    	this.appendChild(div);

    	/** Button */
//    	div = new Div();
//    	div.setSclass(ITheme.LOGIN_BOX_FOOTER_CLASS);
//        pnlButtons = new ConfirmPanel(false, false, false, false, false, false, true);
//        pnlButtons.addActionListener(this);
//        Button okBtn = pnlButtons.getButton(ConfirmPanel.A_OK);
//        okBtn.setWidgetListener("onClick", "zAu.cmd0.showBusy(null)");
//
//        Button helpButton = pnlButtons.createButton(ConfirmPanel.A_HELP);
//		helpButton.addEventListener(Events.ON_CLICK, this);
//		helpButton.setSclass(ITheme.LOGIN_BUTTON_CLASS);
//		pnlButtons.addComponentsRight(helpButton);
//
//        LayoutUtils.addSclass(ITheme.LOGIN_BOX_FOOTER_PANEL_CLASS, pnlButtons);
//        ZKUpdateUtil.setWidth(pnlButtons, null);
//        pnlButtons.getButton(ConfirmPanel.A_OK).setSclass(ITheme.LOGIN_BUTTON_CLASS);
//        div.appendChild(pnlButtons);
//        this.appendChild(div);
	}

    public void onEvent(Event event)
    {
        if (event.getTarget().getId().equals(ConfirmPanel.A_OK))
        {
            showLogin();
        }
        else if (event.getTarget().getId().equals(ConfirmPanel.A_HELP))
        {
            openLoginHelp();
        }
        if (event.getName().equals(ON_LOAD_TOKEN))
        {
        	;
        }
        //
    }

    /**
     *  show login page
     *
    **/
    public void showLogin()
    {
    	Executions.sendRedirect("../.."+ Executions.getCurrent().getContextPath());
    }

    /**
     *  show help page
     *
    **/
	private void openLoginHelp()
	{
		String helpURL = MSysConfig.getValue(MSysConfig.LOGIN_HELP_URL, "https://www.compiere-distribution-lab.net/jpiere-lab/lets-start-jpiere/");
		try {
			Executions.getCurrent().sendRedirect(helpURL, "_blank");
		}
		catch (Exception e) {
			String message = e.getMessage();
			FDialog.warn(0, this, "URLnotValid", message);
		}
	}




}
