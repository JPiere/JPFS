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
package org.compiere.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;

import org.compiere.model.MBPartner;
import org.compiere.model.MBankAccount;
import org.compiere.model.MPaySelection;
import org.compiere.model.MPaySelectionCheck;
import org.compiere.model.MSysConfig;

/**
 * 	
 *  JPIERE-0101: FB Data Export
 *  JPIERE-0580: Select BP Bank Account
 *  JPIERE-0615: Payment Export Class
 *  
 *  Generic Payment Export
 *  Sample implementation of Payment Export Interface - brought here from MPaySelectionCheck
 *
 * 	@author 	Jorg Janke
 *  @author	Hagiwara Hideaki
 *
 *  Contributors:
 *    Carlos Ruiz - GlobalQSS - FR 3132033 - Make payment export class configurable per bank
 */
public class JapanPaymentExport implements PaymentExport
{
	/** Logger								*/
	static private CLogger	s_log = CLogger.getCLogger (JapanPaymentExport.class);


//	/** BPartner Info Index for Value       */
//	private static final int     BP_VALUE = 0;
//	/** BPartner Info Index for Name        */
//	private static final int     BP_NAME = 1;
//	/** BPartner Info Index for Contact Name    */
//	private static final int     BP_CONTACT = 2;
//	/** BPartner Info Index for Address 1   */
//	private static final int     BP_ADDR1 = 3;
//	/** BPartner Info Index for Address 2   */
//	private static final int     BP_ADDR2 = 4;
//	/** BPartner Info Index for City        */
//	private static final int     BP_CITY = 5;
//	/** BPartner Info Index for Region      */
//	private static final int     BP_REGION = 6;
//	/** BPartner Info Index for Postal Code */
//	private static final int     BP_POSTAL = 7;
//	/** BPartner Info Index for Country     */
//	private static final int     BP_COUNTRY = 8;
//	/** BPartner Info Index for Reference No    */
//	private static final int     BP_REFNO = 9;


	/** BankAccount Info Index for Value       */
	private static final int     BA_RequesterCode = 0;
	/** BankAccount Info Index for Name        */
	private static final int     BA_RequesterName = 1;
	/** BankAccount Info Index for Contact Name    */
	private static final int     BA_DATE = 2;
	/** BankAccount Info Index for Address 1   */
	private static final int     BA_RoutingNo = 3;
	/** BankAccount Info Index for Address 2   */
	private static final int     BA_BankName_Kana = 4;
	/** BankAccount Info Index for City        */
	private static final int     BA_BranchCode = 5;
	/** BankAccount Info Index for Region      */
	private static final int     BA_BranchName_Kana = 6;
	/** BankAccount Info Index for Postal Code */
	private static final int     BA_BankAccountType = 7;
	/** BankAccount Info Index for Country     */
	private static final int     BA_AccountNo = 8;

	/** BPartner Info Index for Value       */
	private static final int     BP_RoutingNo = 0;
	/** BPartner Info Index for Name        */
	private static final int     BP_BankName_Kana = 1;
	/** BPartner Info Index for Contact Name    */
	private static final int     BP_BranchCode = 2;
	/** BPartner Info Index for Address 1   */
	private static final int     BP_BranchName_Kana = 3;
	/** BPartner Info Index for City        */
	private static final int     BP_BankAccountType = 4;
	/** BPartner Info Index for Region      */
	private static final int     BP_AccountNo = 5;
	/** BPartner Info Index for Postal Code */
	private static final int     BP_A_Name_Kana = 6;

	/** Left or Right */
	private static final boolean LEFT = true;
	private static final boolean RIGHT = false;
	/** Space or Zero */
	private static final String STR1 = " ";
	private static final String STR2 = "0";


	/**************************************************************************
	 *  Export to File
	 *  @param checks array of checks
	 *  @param file file to export checks
	 *  @return number of lines
	 */
	public int exportToFile (MPaySelectionCheck[] checks, File file, StringBuffer err)
	{
		String line_end = null;
		if(Util.isEmpty(err.toString()))
		{
			line_end = Env.NL;
		}else {
			line_end = err.toString();
			err.setLength(0);//Initialize
		}
		
		if (checks == null || checks.length == 0)
			return 0;
		//  Must be a file
		if (file.isDirectory())
		{
			err.append("No se puede escribir, el archivo seleccionado es un directorio - " + file.getAbsolutePath());
			s_log.log(Level.SEVERE, err.toString());
			return -1;
		}
		//  delete if exists
		try
		{
			if (file.exists())
				file.delete();
		}
		catch (Exception e)
		{
			s_log.log(Level.WARNING, "Could not delete - " + file.getAbsolutePath(), e);
		}

		//char x = '"';      //  ease
		int noLines = 0;
		StringBuffer line = null;
		PrintWriter p_writer   = null;
		try
		{
			String encoding = MSysConfig.getValue("JP_JAPAN_PAYMENT_EXPORT_ENCODING", "Shift_JIS",  Env.getAD_Client_ID(Env.getCtx()), 0);
			if(Util.isEmpty(encoding))
				encoding = "Shift_JIS";

			//precheck Bank Info
			int C_PaySelection_ID = checks[0].getParent().get_ID();
			MPaySelection ps = new MPaySelection(Env.getCtx(), C_PaySelection_ID, null);
			MBankAccount m_BA = new MBankAccount(Env.getCtx(), ps.getC_BankAccount_ID(), null);
			String jp_RequesterName = (String)m_BA.get_Value("JP_RequesterName");
			if(Util.isEmpty(jp_RequesterName))
			{
				throw new Exception(m_BA.getName()+ " - " + Msg.getMsg(Env.getCtx(), "JP_Null") + " [ " +Msg.getElement(Env.getCtx(), "JP_RequesterName") + " ] "  );
			}
			
			p_writer    = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file),encoding)));
			
			//  write header
			line = new StringBuffer();
			String ba[] = getBankInfo(checks);
			line.append("1210")   // dataDiff + code + codeDiff,      1+2+1
			.append(ba[BA_RequesterCode])    // 0 RequesterCode,  10
			.append(ba[BA_RequesterName])	 // 1 RequesterName,  40
			.append(ba[BA_DATE])   			 // 2 System DATE,    4
			.append(ba[BA_RoutingNo])   	 // 3 RoutingNo,      4
			.append(ba[BA_BankName_Kana])    // 4 BankName_Kana,  15
			.append(ba[BA_BranchCode])  	 // 5 BranchCode,     3
			.append(ba[BA_BranchName_Kana])  // 6 BranchName_Kana,15
			.append(ba[BA_BankAccountType])  // 7 BankAccountType,1
			.append(ba[BA_AccountNo])   	 // 8 AccountNo,      7
			.append(strAdd(null,LEFT,STR1,17)) 	 // 9 space,      17
			.append(line_end);
			p_writer.write(line.toString());

			//  write FB lines
			BigDecimal allPayAmt= new BigDecimal(0);
			for (int i = 0; i < checks.length; i++)
			{
				MPaySelectionCheck mpp = checks[i];
				if (mpp == null)
					continue;
				//  BPartner Info
				String bp[] = getBPartnerInfo(mpp);

				// Data Record
				line = new StringBuffer();
				line.append("2")    				 // static 2,			1
					.append(bp[BP_RoutingNo])   	 // 1 RoutingNo,		4
					.append(bp[BP_BankName_Kana])    // 2 BankName_Kana,	15
					.append(bp[BP_BranchCode])  	 // 3 BranchCode,		3
					.append(bp[BP_BranchName_Kana])  // 4 BranchName_Kana,	15
					.append("    ")					 // 5 Space1,			4
					.append(bp[BP_BankAccountType])  // 6 BankAccountType,	1
					.append(bp[BP_AccountNo])   	 // 7 AccountNo,		7
					.append(bp[BP_A_Name_Kana]) 			 // 8 A_Name,			30
					.append(strAdd(mpp.getPayAmt().setScale(0, RoundingMode.HALF_UP).toString(),RIGHT,STR2,10)) // PayAmount, 10
					.append(STR1) 			 		 // Space 1,			1
					.append(strAdd(null,RIGHT,STR1,10)) 			 // Space 10, 10
					.append(strAdd(null,RIGHT,STR1,10)) 			 // Space 10, 10
					.append(strAdd(null,LEFT,STR1,9)) 			 	 // Space 9
					//.append(comment.toString())      // Comment
					.append(line_end);
				p_writer.write(line.toString());
				noLines++;
				allPayAmt = allPayAmt.add(mpp.getPayAmt());

			}   //  write FB line

			// write trailer Record
			line = new StringBuffer();
			line.append("8")
				.append(strAdd(String.valueOf(noLines),RIGHT,STR2,6))
				.append(strAdd(allPayAmt.toString(),RIGHT,STR2,12))
				.append(strAdd(null,LEFT,STR1,101)) 	 // space 101
				.append(line_end);
			p_writer.write(line.toString());

			// write end Record
			line = new StringBuffer();
			line.append("9")
				.append(strAdd(null,LEFT,STR1,119))
				.append(line_end);
			p_writer.write(line.toString());

			p_writer.flush();
			
		}
		catch (Exception e)
		{
			s_log.log(Level.SEVERE, "", e);
			err =err.append(e.getLocalizedMessage());
			return -1;
		}finally {
			p_writer.close();
		}

		return noLines;
	}   //  exportToFile

	/**
	 *  Get Customer/Vendor Info.
	 *  Based on BP_ static variables
	 *  @param C_BPartner_ID BPartner
	 *  @return info array
	 * @throws Exception 
	 */
	private static String[] getBPartnerInfo (MPaySelectionCheck m_PaySelectionCheck) throws Exception
	{
		String[] bp = new String[10];

		String sql = "SELECT ba.RoutingNo,"
				+ "ba.JP_BankName_Kana,"
				+ "bpbc.JP_BranchCode,"
				+ "bpbc.JP_BranchName_Kana,"
				+ "case when bpbc.BankAccountType='S' then 1 else 2 end,"
				+ "bpbc.AccountNo,"
				+ "bpbc.JP_A_Name_Kana,"
				+ "bpbc.IsDefault as IsDefault "
				+ "FROM C_BP_BANKACCOUNT bpbc "
				+ "INNER JOIN C_Bank ba ON bpbc.C_Bank_ID=ba.C_Bank_ID "
				;
		
		int C_BP_BankAccount_ID = m_PaySelectionCheck.getC_BP_BankAccount_ID();
		
		if(C_BP_BankAccount_ID == 0)
		{
			MPaySelection m_PaySelection =  new MPaySelection(Env.getCtx(),  m_PaySelectionCheck.getC_PaySelection_ID(), null); 
			boolean IsReceiptJP = m_PaySelection.get_ValueAsBoolean("IsReceiptJP");
			
			if(IsReceiptJP)
			{
				sql =  sql
						+ "WHERE bpbc.C_BPartner_ID= ? "
						+ " and bpbc.IsActive='Y' "
						+ " and bpbc.IsACH='Y' "
						+ " and bpbc.BPBankAcctUse IN ('B','D') "	//Both & Direct Debit
						+ " order by bpbc.IsDefault DESC, bpbc.Created ASC ";
			}else {
				sql =  sql
						+ "WHERE bpbc.C_BPartner_ID= ? "
						+ " and bpbc.IsActive='Y' "
						+ " and bpbc.IsACH='Y' "
						+ " and bpbc.BPBankAcctUse IN ('B','T') "	//Both & Direct Deposit
						+ " order by bpbc.IsDefault DESC, bpbc.Created ASC ";				
			}
			
		}else {
			
			sql = sql
					+ "WHERE bpbc.C_BP_BankAccount_ID= ? ";
		}
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try
		{
			pstmt = DB.prepareStatement(sql, null);
			if(C_BP_BankAccount_ID == 0)
			{
				pstmt.setInt(1, m_PaySelectionCheck.getC_BPartner_ID());
			}else {
				pstmt.setInt(1, C_BP_BankAccount_ID);
			}
			rs = pstmt.executeQuery();

			if (rs.next())
			{
				bp[BP_RoutingNo] = rs.getString(1);
				if (bp[BP_RoutingNo] == null || bp[BP_RoutingNo].length()!=4)
					bp[BP_RoutingNo] = strAdd(bp[BP_RoutingNo],LEFT,STR1,4);
				bp[BP_BankName_Kana] = rs.getString(2);
				if (bp[BP_BankName_Kana] == null || bp[BP_BankName_Kana].length()!=15)
					bp[BP_BankName_Kana] = strAdd(bp[BP_BankName_Kana],LEFT,STR1,15);
				bp[BP_BranchCode] = rs.getString(3);
				if (bp[BP_BranchCode] == null || bp[BP_BranchCode].length()!=3)
					bp[BP_BranchCode] = strAdd(bp[BP_BranchCode],LEFT,STR1,3);
				bp[BP_BranchName_Kana] = rs.getString(4);
				if (bp[BP_BranchName_Kana] == null || bp[BP_BranchName_Kana].length()!=15)
					bp[BP_BranchName_Kana] = strAdd(bp[BP_BranchName_Kana],LEFT,STR1,15);
				bp[BP_BankAccountType] = rs.getString(5);
				if (bp[BP_BankAccountType] == null || bp[BP_BankAccountType].length()!=1)
					bp[BP_BankAccountType] = strAdd(bp[BP_BankAccountType],LEFT,STR1,1);
				bp[BP_AccountNo] = rs.getString(6);
				if (bp[BP_AccountNo] == null || bp[BP_AccountNo].length()!=7)
					bp[BP_AccountNo] = strAdd(bp[BP_AccountNo],LEFT,STR1,7);
				bp[BP_A_Name_Kana] = rs.getString(7);
				if(Util.isEmpty(bp[BP_A_Name_Kana]))
				{
					int C_BPartner_ID = m_PaySelectionCheck.getC_BPartner_ID();
					MBPartner m_BP = MBPartner.get(Env.getCtx(), C_BPartner_ID);
					throw new Exception(m_BP.getName()+ " - " + Msg.getMsg(Env.getCtx(), "JP_Null") + " [ " +Msg.getElement(Env.getCtx(), "JP_A_Name_Kana") + " ] "  );
				}else if (bp[BP_A_Name_Kana].length()!=30) {
					bp[BP_A_Name_Kana] = strAdd(bp[BP_A_Name_Kana],LEFT,STR1,30);
				}
			}
			else
			{
				int C_BPartner_ID = m_PaySelectionCheck.getC_BPartner_ID();
				MBPartner m_BP = MBPartner.get(Env.getCtx(), C_BPartner_ID);
				throw new Exception(m_BP.getName()+ " - " +Msg.getElement(Env.getCtx(), "C_BP_BankAccount_ID") +" "+ Msg.getMsg(Env.getCtx(), "NotFound") );
				
//				bp[BP_RoutingNo] = strAdd(bp[BP_RoutingNo],LEFT,STR1,4);
//				bp[BP_BankName_Kana] = strAdd(bp[BP_BankName_Kana],LEFT,STR1,15);
//				bp[BP_BranchCode] = strAdd(bp[BP_BranchCode],LEFT,STR1,3);
//				bp[BP_BranchName_Kana] = strAdd(bp[BP_BranchName_Kana],LEFT,STR1,15);
//				bp[BP_BankAccountType] = strAdd(bp[BP_BankAccountType],LEFT,STR1,1);
//				bp[BP_AccountNo] = strAdd(bp[BP_AccountNo],LEFT,STR1,7);
//				bp[BP_A_Name_Kana] = strAdd(bp[BP_A_Name_Kana],LEFT,STR1,30);
			}

//			while(rs.next())
//			{
//				if(rs.getString("IsDefault").compareTo("Y")==0)
//				{
//					bp[BP_RoutingNo] = rs.getString(1);
//					if (bp[BP_RoutingNo] == null || bp[BP_RoutingNo].length()!=4)
//						bp[BP_RoutingNo] = strAdd(bp[BP_RoutingNo],LEFT,STR1,4);
//					bp[BP_BankName_Kana] = rs.getString(2);
//					if (bp[BP_BankName_Kana] == null || bp[BP_BankName_Kana].length()!=15)
//						bp[BP_BankName_Kana] = strAdd(bp[BP_BankName_Kana],LEFT,STR1,15);
//					bp[BP_BranchCode] = rs.getString(3);
//					if (bp[BP_BranchCode] == null || bp[BP_BranchCode].length()!=3)
//						bp[BP_BranchCode] = strAdd(bp[BP_BranchCode],LEFT,STR1,3);
//					bp[BP_BranchName_Kana] = rs.getString(4);
//					if (bp[BP_BranchName_Kana] == null || bp[BP_BranchName_Kana].length()!=15)
//						bp[BP_BranchName_Kana] = strAdd(bp[BP_BranchName_Kana],LEFT,STR1,15);
//					bp[BP_BankAccountType] = rs.getString(5);
//					if (bp[BP_BankAccountType] == null || bp[BP_BankAccountType].length()!=1)
//						bp[BP_BankAccountType] = strAdd(bp[BP_BankAccountType],LEFT,STR1,1);
//					bp[BP_AccountNo] = rs.getString(6);
//					if (bp[BP_AccountNo] == null || bp[BP_AccountNo].length()!=7)
//						bp[BP_AccountNo] = strAdd(bp[BP_AccountNo],LEFT,STR1,7);
//					bp[BP_A_Name_Kana] = rs.getString(7);
//					if (bp[BP_A_Name_Kana] == null || bp[BP_A_Name_Kana].length()!=30)
//						bp[BP_A_Name_Kana] = strAdd(bp[BP_A_Name_Kana],LEFT,STR1,30);
//					break;
//				}
//			}

		}
		catch (SQLException e)
		{
			s_log.log(Level.SEVERE, sql, e);
		}finally {
			DB.close(rs, pstmt);
			rs = null;
			pstmt = null;
		}
		

		return bp;
	}   //  getBPartnerInfo



	private static String[] getBankInfo (MPaySelectionCheck[]  checks)
	{
		String[] ba = new String[10];
		int C_PaymentSelection_ID = checks[0].getParent().get_ID();

		String sql = "SELECT bc.JP_RequesterCode, "
			+ "bc.JP_RequesterName,"
			+ "to_char(ps.PayDate,'MMDD'),"
			+ "ba.RoutingNo,"
			+ "ba.JP_BankName_Kana,"
			+ "bc.JP_BranchCode,"
			+ "bc.JP_BranchName_Kana,"
			+ "case when bc.BankAccountType='S' then 1 else 2 end,"
			+ "bc.AccountNo "
			+ "FROM C_PaySelection ps "
			+ " INNER JOIN C_BankAccount bc ON (ps.C_BankAccount_ID=bc.C_BankAccount_ID) "
			+ " INNER JOIN C_Bank ba ON (bc.C_Bank_ID=ba.C_Bank_ID) "
			+ "WHERE ps.C_PaySelection_ID=? ";
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try
		{
			pstmt = DB.prepareStatement(sql, null);
			pstmt.setInt(1, C_PaymentSelection_ID);
			rs = pstmt.executeQuery();
			//
			if (rs.next())
			{
				ba[BA_RequesterCode] = rs.getString(1);
				if (ba[BA_RequesterCode] == null || ba[BA_RequesterCode].length()!=10)
					ba[BA_RequesterCode] = strAdd(ba[BA_RequesterCode],LEFT,STR1,10);
				ba[BA_RequesterName] = rs.getString(2);
				if (ba[BA_RequesterName] == null || ba[BA_RequesterName].length()!=40 )
					ba[BA_RequesterName] = strAdd(ba[BA_RequesterName],LEFT,STR1,40);
				ba[BA_DATE] = rs.getString(3);
				if (ba[BA_DATE] == null)
					ba[BA_DATE] = strAdd(ba[BA_DATE],LEFT,STR1,4);
				ba[BA_RoutingNo] = rs.getString(4);
				if (ba[BA_RoutingNo] == null || ba[BA_RoutingNo].length()!=4)
					ba[BA_RoutingNo] = strAdd(ba[BA_RoutingNo],LEFT,STR1,4);
				ba[BA_BankName_Kana] = rs.getString(5);
				if (ba[BA_BankName_Kana] == null || ba[BA_BankName_Kana].length()!= 15)
					ba[BA_BankName_Kana] = strAdd(ba[BA_BankName_Kana],LEFT,STR1,15);
				ba[BA_BranchCode] = rs.getString(6);
				if (ba[BA_BranchCode] == null || ba[BA_BranchCode].length()!=3)
					ba[BA_BranchCode] = strAdd(ba[BA_BranchCode],LEFT,STR1,3);
				ba[BA_BranchName_Kana] = rs.getString(7);
				if (ba[BA_BranchName_Kana] == null || ba[BA_BranchName_Kana].length()!=15)
					ba[BA_BranchName_Kana] = strAdd(ba[BA_BranchName_Kana],LEFT,STR1,15);
				ba[BA_BankAccountType] = rs.getString(8);
				if (ba[BA_BankAccountType] == null || ba[BA_BankAccountType].length()!=1)
					ba[BA_BankAccountType] = strAdd(ba[BA_BankAccountType],LEFT,STR1,1);
				ba[BA_AccountNo] = rs.getString(9);
				if (ba[BA_AccountNo] == null || ba[BA_AccountNo].length()!=7 )
					ba[BA_AccountNo] = strAdd(ba[BA_AccountNo],LEFT,STR1,7);
			}
			
		}
		catch (SQLException e)
		{
			s_log.log(Level.SEVERE, sql, e);
		}finally {
			
			DB.close(rs, pstmt);
			rs = null;
			pstmt = null;
		}
		return ba;
	}   //  getBankInfo



	public static String strAdd(String strIn, boolean left, String addStr, int strLength)
	{
		StringBuffer returnStr= new StringBuffer("");
		int length1=0;

		if(strIn!=null)
		{
			returnStr.append(strIn);
			length1=strIn.length();
		}

		if(left)
		{
			if(length1<strLength)
			{
				for(int i=length1;i<strLength;i++)
				{
					returnStr.append(addStr);
				}
			}
			else
			{
				returnStr.delete(strLength, length1);
			}
		}
		else
		{
			for(int i=length1;i<strLength;i++)
			{
				returnStr.insert(0,addStr);
			}
		}
		return returnStr.toString();
	}


}	//	PaymentExport
