/******************************************************************************
 * Product: Adempiere ERP & CRM Smart Business Solution                       *
 * Copyright (C) 1999-2006 ComPiere, Inc. All Rights Reserved.                *
 * This program is free software; you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program; if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 * For the text or an alternative of this public license, you may reach us    *
 * ComPiere, Inc., 2620 Augustine Dr. #245, Santa Clara, CA 95054, USA        *
 * or via info@compiere.org or http://www.compiere.org/license.html           *
 *****************************************************************************/
package org.compiere.wf;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;

import org.compiere.model.I_AD_WF_Activity;
import org.compiere.model.MPInstance;
import org.compiere.model.MProcess;
import org.compiere.model.Query;
import org.compiere.process.ProcessInfo;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.Msg;
import org.compiere.util.Trx;

/**
 * JPIERE-8 Update WF Responsible of WF Activity in a lump
 *
 * This process update WF Responsible of WF Activity that WFState is "OS" and Processed is "false".
 *
 *  @author Hideaki Hagiwara
 *  @version $Id: WFActivityRespBatchUpdate.java,v 1.0 2014/03/05 00:00:00 $
 */
public class WFActivityRespBatchUpdate extends SvrProcess
{

	private static final int PROCESS_Manage_Activity = 278;	//AD_WF_Activity_Manage(Manage Activity) Process

	private int 		p_AD_Client_ID = 0;

	/**Target Workflow(Mandatory)	*/
	private int			p_AD_Workflow_ID = 0;

	/**Original WF Responsible(Mandatory)*/
	private int			p_AD_WF_Responsible_ID = 0;

	/**Substitute WF Responsible(Mandatory)*/
	private int			p_AD_WF_RespSubstitute_ID = 0;

	/**Target Organization(Option)*/
	private int			p_AD_Org_ID = 0;

	/**Original User(option)*/
	private int			p_AD_User_ID = 0;

	/**Target Created Date(Option)*/
	private Timestamp	p_Created_From = null;
	private Timestamp	p_Created_To = null;


	/**
	 *  Prepare - get Parameters.
	 */
	protected void prepare()
	{
		p_AD_Client_ID =getProcessInfo().getAD_Client_ID();
		ProcessInfoParameter[] para = getParameter();
		for (int i = 0; i < para.length; i++)
		{
			String name = para[i].getParameterName();
			if (para[i].getParameter() == null){
				;
			}else if (name.equals("AD_WF_Responsible_ID")){
				p_AD_WF_Responsible_ID = para[i].getParameterAsInt();
			}else if (name.equals("AD_WF_RespSubstitute_ID")){
				p_AD_WF_RespSubstitute_ID = para[i].getParameterAsInt();
			}else if (name.equals("AD_Workflow_ID")){
				p_AD_Workflow_ID = para[i].getParameterAsInt();
			}else if (name.equals("Created")){
				p_Created_From = (Timestamp)para[i].getParameter();
				p_Created_To = (Timestamp)para[i].getParameter_To();
				Calendar cal = Calendar.getInstance();
				cal.setTimeInMillis(p_Created_To.getTime());
				cal.add(Calendar.DAY_OF_MONTH, 1);
				p_Created_To = new Timestamp(cal.getTimeInMillis());
			}else if (name.equals("AD_Org_ID")){
				p_AD_Org_ID = para[i].getParameterAsInt();
			}else if (name.equals("AD_User_ID")){
				p_AD_User_ID = para[i].getParameterAsInt();
			}else{
				log.log(Level.SEVERE, "Unknown Parameter: " + name);
			}//if
		}//for
	}//	prepare


	/**
	 *  Perform process.
	 *  @return Message (variables are parsed)
	 *  @throws Exception if not successful
	 */
	protected String doIt() throws Exception
	{
		//Mandatory parameters
		StringBuilder whereClause = new StringBuilder(MWFActivity.COLUMNNAME_AD_WF_Responsible_ID + " = ? AND "
														+ MWFActivity.COLUMNNAME_AD_Workflow_ID + "= ? AND "
														+ MWFActivity.COLUMNNAME_AD_Client_ID + " = ? AND "
														+ MWFActivity.COLUMNNAME_Processed +" = 'N' AND "
														+ MWFActivity.COLUMNNAME_WFState + " = 'OS' "
														);

		ArrayList<Object> getWFActivitiesParamList = new ArrayList<Object>();
		getWFActivitiesParamList.add(p_AD_WF_Responsible_ID);
		getWFActivitiesParamList.add(p_AD_Workflow_ID);
		getWFActivitiesParamList.add(p_AD_Client_ID);

		//Option parameters
		if (p_AD_Org_ID != 0)
		{
			whereClause.append("AND " + MWFActivity.COLUMNNAME_AD_Org_ID + " = ? ");
			getWFActivitiesParamList.add(p_AD_Org_ID);
		}


		if(p_AD_User_ID !=0)
		{
			whereClause.append("AND " + MWFActivity.COLUMNNAME_AD_User_ID + " = ? ");
			getWFActivitiesParamList.add(p_AD_User_ID);
		}

		if(p_Created_From != null)
		{
			whereClause.append("AND " + MWFActivity.COLUMNNAME_Created + " >= ? ");
			getWFActivitiesParamList.add(p_Created_From);
		}

		if(p_Created_To != null)
		{
			whereClause.append("AND " + MWFActivity.COLUMNNAME_Created + " <= ? ");
			getWFActivitiesParamList.add(p_Created_To);
		}

		//Get Target WF Activities
		List<MWFActivity> list = new Query(getCtx(), I_AD_WF_Activity.Table_Name, whereClause.toString(), get_TrxName())
										.setParameters(getWFActivitiesParamList)
										.list();
		MWFActivity[] activities = list.toArray(new MWFActivity[list.size()]);


		//Prepare AD_WF_Activity_Manage(Manage Activity) process Parameters.
		ProcessInfoParameter[] pipParams = new ProcessInfoParameter[]{new ProcessInfoParameter("AD_WF_Responsible_ID", p_AD_WF_RespSubstitute_ID, null, null, null)};

		for(int i = 0; i < activities.length; i++)
		{
			MProcess process = MProcess.get(getCtx(), PROCESS_Manage_Activity);
			MPInstance pInstance = new MPInstance(process, 0);

			ProcessInfo pi = new ProcessInfo(process.getName(), PROCESS_Manage_Activity, MWFActivity.Table_ID, activities[i].getAD_WF_Activity_ID());
			pi.setParameter(pipParams);
			pi.setAD_User_ID(getAD_User_ID());
			pi.setAD_Client_ID(getAD_Client_ID());
			pi.setAD_PInstance_ID(pInstance.getAD_PInstance_ID());

			boolean isOK = process.processItWithoutTrxClose(pi,Trx.get(get_TrxName(), false));
			if(isOK)
			{
				addLog(Msg.getElement(getCtx(), "AD_WF_Process_ID")+" "+Msg.getElement(getCtx(), "TextMsg")+" => "+ activities[i].getAD_WF_Process().getTextMsg());
			}else{
				throw new Exception(Msg.getMsg(getCtx(), "ProcessRunError"));//Process failed during execution
			}

		}//for


		return MWorkflow.get(getCtx(), p_AD_Workflow_ID).getName() + " = " + activities.length;
	}	//	doIt

}	//	WFActivityRespBatchUpdate
