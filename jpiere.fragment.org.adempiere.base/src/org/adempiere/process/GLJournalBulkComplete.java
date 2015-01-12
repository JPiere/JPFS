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

package org.adempiere.process;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;

import org.adempiere.util.IProcessUI;
import org.compiere.model.I_GL_Journal;
import org.compiere.model.MJournal;
import org.compiere.model.Query;
import org.compiere.process.DocAction;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.Env;
import org.compiere.util.Msg;



/**
 * JPIERE-10 GLJournal Bulk Complete
 *
 *
 *  @author Hideaki Hagiwara
 *  @version $Id: GLJournalBulkComplete.java,v 1.0 2014/05/10 00:00:00 $
 */
public class GLJournalBulkComplete extends SvrProcess {

	private int 		p_AD_Client_ID = 0;

	/**Target Organization(Option)*/
	private int			p_AD_Org_ID = 0;

	/**Target DateAcct Date(Option)*/
	private Timestamp	p_DateAcct_From = null;
	private Timestamp	p_DateAcct_To = null;

	/**Target DocStatus(Mandatory)*/
	private String		p_DocStatus = "DR";

	/**Original User(Option)*/
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
			}else if (name.equals("AD_Org_ID")){
				p_AD_Org_ID = para[i].getParameterAsInt();
			}else if (name.equals("DateAcct")){
				p_DateAcct_From = (Timestamp)para[i].getParameter();
				p_DateAcct_To = (Timestamp)para[i].getParameter_To();
				if(p_DateAcct_To!=null)
				{
					Calendar cal = Calendar.getInstance();
					cal.setTimeInMillis(p_DateAcct_To.getTime());
					cal.add(Calendar.DAY_OF_MONTH, 1);
					p_DateAcct_To = new Timestamp(cal.getTimeInMillis());
				}
			}else if (name.equals("DocStatus")){
				p_DocStatus = para[i].getParameterAsString();
			}else if (name.equals("AD_User_ID")){
				p_AD_User_ID = para[i].getParameterAsInt();
			}else if (name.equals("Created")){
				p_Created_From = (Timestamp)para[i].getParameter();
				p_Created_To = (Timestamp)para[i].getParameter_To();
				if(p_Created_To!=null)
				{
					Calendar cal = Calendar.getInstance();
					cal.setTimeInMillis(p_Created_To.getTime());
					cal.add(Calendar.DAY_OF_MONTH, 1);
					p_Created_To = new Timestamp(cal.getTimeInMillis());
				}
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
		StringBuilder whereClause = new StringBuilder(MJournal.COLUMNNAME_AD_Client_ID + " = ? AND "
														+ MJournal.COLUMNNAME_Processed + " = 'N' AND "
														+ MJournal.COLUMNNAME_DocStatus + " = " + "'" + p_DocStatus + "'"
														);

		ArrayList<Object> docListParams = new ArrayList<Object>();
		docListParams.add(p_AD_Client_ID);

		//Option parameters
		if (p_AD_Org_ID != 0)
		{
			whereClause.append(" AND " + MJournal.COLUMNNAME_AD_Org_ID + " = ? ");
			docListParams.add(p_AD_Org_ID);
		}

		if(p_DateAcct_From != null)
		{
			whereClause.append(" AND " + MJournal.COLUMNNAME_DateAcct + " >= ? ");
			docListParams.add(p_DateAcct_From);
		}

		if(p_DateAcct_To != null)
		{
			whereClause.append(" AND " + MJournal.COLUMNNAME_DateAcct + " <= ? ");
			docListParams.add(p_DateAcct_To);
		}


		if(p_AD_User_ID != 0)
		{
			whereClause.append(" AND " + MJournal.COLUMNNAME_CreatedBy + " = ? ");
			docListParams.add(p_AD_User_ID);
		}


		if(p_Created_From != null)
		{
			whereClause.append(" AND " + MJournal.COLUMNNAME_Created + " >= ? ");
			docListParams.add(p_Created_From);
		}

		if(p_Created_To != null)
		{
			whereClause.append(" AND " + MJournal.COLUMNNAME_Created + " <= ? ");
			docListParams.add(p_Created_To);
		}

		//Get Target WF Activities
		List<MJournal> list = new Query(getCtx(), I_GL_Journal.Table_Name, whereClause.toString(), get_TrxName())
										.setParameters(docListParams)
										.list();
		MJournal[] gldoc = list.toArray(new MJournal[list.size()]);


		IProcessUI processMonitor = Env.getProcessUI(getCtx());
		int success = 0;
		int failure = 0;

		for(int i = 0; i < gldoc.length; i++)
		{
			MJournal mj = gldoc[i];

			boolean isOK = mj.processIt(DocAction.ACTION_Complete);
			if(isOK)
			{
				success++;
			}else{
				failure++;
			}

			String msg = Msg.getElement(getCtx(), "DocumentNo") + " : " + mj.getDocumentNo()
							+ " - " + Msg.getElement(getCtx(), "DocStatus") + " : " + mj.getDocStatus();
			addBufferLog(0, null, null, msg, MJournal.Table_ID, mj.get_ID());

			if (processMonitor != null)
			{
				processMonitor.statusUpdate(msg);
			}else{
				processMonitor = Env.getProcessUI(getCtx());
			}



		}//for


		return "Success" + " = " + success + "    Failure" + " = " + failure ;
	}	//	doIt

}
