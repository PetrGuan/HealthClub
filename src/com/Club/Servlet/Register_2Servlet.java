package com.Club.Servlet;

import java.io.IOException;
import java.util.Date;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.Club.Factory.DaoFactory;
import com.Club.Factory.ServiceFactory;
import com.Club.Model.HouseMember;
import com.Club.Model.PayRecord;
import com.Club.Model.PersonalMember;

/*������ͥ�û�ע��,��ɿ۷ѵȲ���
 * �ɹ��� session�д洢�����Ϣ
 * ��ת��index.jsp
 */

@WebServlet("/HouseRegister")
public class Register_2Servlet extends HttpServlet{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final double QUALIFYFEE=100;
	
	public Register_2Servlet(){
		super();
	}
	
    protected void doGet(HttpServletRequest request,HttpServletResponse response){
		
	}
	
	protected void doPost(HttpServletRequest request,HttpServletResponse response) throws ServletException, IOException{
		execute(request,response);
	}
	
	
	public void execute(HttpServletRequest request,HttpServletResponse response){
        String jspAddress="/jsp/registerFailure.jsp";
		
		String account=request.getParameter("account");
		HouseMember member=ServiceFactory.getHouseServiceInstance().validateAccount(account);
		PersonalMember pm=ServiceFactory.getPersonalServiceInstance().validateAccount(account);
		
		//�˺�ȷʵ������
		if(member==null&&pm==null){
			
			String bankCardAccount=request.getParameter("bankCardAccount");
			double balance=ServiceFactory.getBankCardServiceInstance().findBalance(bankCardAccount);
			
			if(balance!=-1){
			boolean charge=ServiceFactory.getBankCardServiceInstance().charge(bankCardAccount, QUALIFYFEE);
			//ע��ɹ�
			if(charge){
				HouseMember newMember=new HouseMember();
				newMember.setAccount(request.getParameter("account"));
				newMember.setAddress(request.getParameter("address"));
				newMember.setChildren(Integer.parseInt(request.getParameter("children")));
				newMember.setCouples(Integer.parseInt(request.getParameter("couples")));
				newMember.setBankCardAccount(request.getParameter("bankCardAccount"));
				newMember.setGender(request.getParameter("gender"));
				newMember.setIdCard(request.getParameter("cardID"));
				newMember.setPassword(request.getParameter("password"));
				//��������û�״̬Ϊ����
				newMember.setMemberState("normal");
				
				boolean isSucceed=ServiceFactory.getHouseServiceInstance().addHouseMember(newMember);
				if(isSucceed){
					
				jspAddress="/jsp/index.jsp";
				//�Ѿ���¼��Ϊtrue,���Ѷ������session��
				HttpSession session = request.getSession(true);
				session.setAttribute("type","house");
				session.setAttribute("logged", true);
				session.setAttribute("member", newMember);
				session.setAttribute("account",newMember.getAccount());
				
				
				PayRecord payRecord=new PayRecord();
				payRecord.setAccount(newMember.getAccount());
				payRecord.setPayment(QUALIFYFEE);
				payRecord.setDate(new Date());
				payRecord.setPayRecordId(0);
				DaoFactory.getPayRecordDao().addPayRecord(payRecord);
				}
				//�����Ǯ�����˻�
				else {
					ServiceFactory.getBankCardServiceInstance().recover(bankCardAccount, balance);
				}
			}
		  }
			
		}
		
		
		RequestDispatcher dispatcher = request.getRequestDispatcher(jspAddress);
		try {
			dispatcher.forward(request, response);
		} catch (ServletException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}