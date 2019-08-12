package com.csc.cv.services;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.csc.fsg.nba.servlet.TestNbaContractValidation;





/**
 * @version 	1.0
 * @author
 */

@RestController
@RequestMapping("/contractvalidation")
public class TestServlet  {


@RequestMapping("/performWork/{operation}")
public String performWork(@PathVariable("operation") String operation) throws Exception {
	//PrintWriter out  = resp.getWriter() ;
	try {
		//String operation = req.getParameter("OPT");
		System.out.println("operation :"+operation);
		if ("CVAL".equalsIgnoreCase(operation)) {
			//new TestNbaContractValidation("test").testContractValidation(req.getParameter("FILE"));
			new TestNbaContractValidation("test").testContractValidation("203.xml");
			} /*
				 * else if ("UWRSK".equalsIgnoreCase(operation)) { PrintWriter writer =
				 * resp.getWriter(); writer.println("<HTML>"); writer.println("<HEAD>");
				 * writer.println("<TITLE>"); writer.println("Prior Ins Results");
				 * writer.println("</TITLE>"); writer.println("</HEAD>");
				 * writer.println("<BODY>"); //new
				 * TestUnderwritingRisk().doProcess(req.getParameter("FILE"), writer);
				 * writer.println("</BODY>"); writer.println("</HEAD>");
				 * writer.println("</HTML>");
				 * 
				 * writer.close(); }
				 */
		
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
		return "Contract Validation called Succesfully, Please check output_203.xml";
	} catch (Exception e) {
		e.printStackTrace();
		
	}
	System.out.println("exiting servlet");
	return "Please check output_203.xml";
}

}
