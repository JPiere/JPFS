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


	/**Original User(Mandatory)*/
	private int			p_AD_User_ID = 0;


	/**Target Organization(Option)*/
	private int			p_AD_Org_ID = 0;


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
			}else if (name.equals("AD_User_ID")){
				p_AD_User_ID = para[i].getParameterAsInt();
			}else if (name.equals("Created")){
				p_Created_From = (Timestamp)para[i].getParameter();
				p_Created_To = (Timestamp)para[i].getParameter_To();
				if(p_Created_To!=null)
				{Calendar cal = Calendar.getInstance();
				cal.setTimeInMillis(p_Created_To.getTime());
				cal.add(Calendar.DAY_OF_MONTH, 1);
				p_Created_To = new Timestamp(cal.getTimeInMillis());
				}
			}else if (name.equals("AD_Org_ID")){
				p_AD_Org_ID = para[i].getParameterAsInt();
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
														+ MJournal.COLUMNNAME_Processed +" = 'N' AND "
														+ MJournal.COLUMNNAME_DocStatus +" = 'DR' "
														);

		ArrayList<Object> docListParams = new ArrayList<Object>();
		docListParams.add(p_AD_Client_ID);

		//Option parameters
		if (p_AD_Org_ID != 0)
		{
			whereClause.append("AND " + MJournal.COLUMNNAME_AD_Org_ID + " = ? ");
			docListParams.add(p_AD_Org_ID);
		}
		
		if(p_AD_User_ID != 0)
		{
			whereClause.append("AND " + MJournal.COLUMNNAME_CreatedBy + " = ? ");
			docListParams.add(p_AD_User_ID);
		}


		if(p_Created_From != null)
		{
			whereClause.append("AND " + MJournal.COLUMNNAME_Created + " >= ? ");
			docListParams.add(p_Created_From);
		}

		if(p_Created_To != null)
		{
			whereClause.append("AND " + MJournal.COLUMNNAME_Created + " <= ? ");
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
			}

			
			
		}//for


		return "Success" + " = " + success + "    failure" + " = " + failure ;
	}	//	doIt
	
}
