package com.csc.fsg.nba.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;





/**
 * @version 	1.0
 * @author
 */
public class TestServlet extends HttpServlet implements Servlet {

		public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try{
			performWork(req,resp);
		}catch(Exception e){
			e.printStackTrace();
		}

	}

	/**
	* @see javax.servlet.http.HttpServlet#void (javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	*/
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try{
			performWork(req,resp);
		}catch(Exception e){
			e.printStackTrace();
		}

	}

public void performWork(HttpServletRequest req, HttpServletResponse resp) throws Exception {
	PrintWriter out  = resp.getWriter() ;
	try {
		String operation = req.getParameter("OPT");
		if ("CVAL".equalsIgnoreCase(operation)) {
			new TestNbaContractValidation("test").testContractValidation(req.getParameter("FILE"));
		} else if ("UWRSK".equalsIgnoreCase(operation)) {
			PrintWriter writer = resp.getWriter();
			writer.println("<HTML>");
			writer.println("<HEAD>");
			writer.println("<TITLE>");
			writer.println("Prior Ins Results");
			writer.println("</TITLE>");
			writer.println("</HEAD>");
			writer.println("<BODY>");
			//new TestUnderwritingRisk().doProcess(req.getParameter("FILE"), writer);
			writer.println("</BODY>");
			writer.println("</HEAD>");
			writer.println("</HTML>");
			
			writer.close();
		} 
		
		//else if ("ISSUE".equalsIgnoreCase(operation)) {
//			String a[] = new String[1];
//			new TestIssueProcess().processIssue(req.getParameter("FILE"));
//		} else if ("ISSUECASE".equalsIgnoreCase(operation)) {
//			String a[] = new String[1];
//			a[0] = req.getParameter("FILE");
//			IssueCase.main(a);
//		} else if ("TESTVPMS".equalsIgnoreCase(operation)) {
//			String a[] = new String[1];
//			a[0] = req.getParameter("FILE");
//			TESTVPMS.main(a);
//		} else if ("LABONE".equalsIgnoreCase(operation)) {
//			String a[] = new String[1];
//			NbaLabOneRaw.main(a);
//
//		}
			

		System.out.println("calling test class from servlet");

	} catch (Exception e) {
		e.printStackTrace();
		e.printStackTrace( out ) ;
	}
	System.out.println("exiting servlet");
}

}
