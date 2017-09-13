package com.github.mviku.cca.ccjp;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class BankDepositAccountOrganizer {
	

	

	public static Map<String, List<ParentAccountVO>> processBankDepositData(
			String filePath) throws BankOrganizerException, FileNotFoundException {
		BankDepositAccountOrganizer organizer = new BankDepositAccountOrganizer();
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
		FileReader fr = null;
		BufferedReader br = null;
		String s;
		Map<Integer, ParentAccountVO> records = new HashMap<Integer, ParentAccountVO>();
		try {
			fr = new FileReader(filePath);
			br = new BufferedReader(fr);
			while((s = br.readLine()) != null){
				String[] str = s.split(",");
				if(validateData(str)){
					int parentAccNo = Integer.valueOf(str[0]);
					if(!records.containsKey(parentAccNo)){
						ParentAccountVO parentAccountVO = new ParentAccountVO();
						parentAccountVO.setParentAccNo(parentAccNo);
						parentAccountVO.setName(str[1]);
						parentAccountVO.setAccType(str[2]);
						parentAccountVO.setLinkedDeposits(new ArrayList<LinkedDepositVO>());
						records.put(parentAccNo, parentAccountVO);
					}
					LinkedDepositVO depositVO = new LinkedDepositVO();
					records.get(parentAccNo).getLinkedDeposits().add(depositVO);
					depositVO.setDepositAmount(Integer.valueOf(str[4]));
					depositVO.setLinkedDepositNo(str[3]);
					try{
						depositVO.setDepositStartDate(sdf.parse(str[5]));
						depositVO.setDepositMaturityDate(sdf.parse(str[6]));
						depositVO.setMaturityAmount(
								organizer.calculateMaturityAmount(
										depositVO.getDepositStartDate(), depositVO.getDepositMaturityDate(), depositVO.getDepositAmount()));
					}catch(ParseException e){
						throw new BankOrganizerException("");
					}
				}else{
					throw new BankOrganizerException("");
				}
			}
		} catch (IOException e) {
			throw new BankOrganizerException(e);
		}finally{
			if(br != null){
				try {
					br.close();
				} catch (IOException e) {
					throw new BankOrganizerException(e);
				}
			}
			if(fr != null){
				try {
					fr.close();
				} catch (IOException e) {
					throw new BankOrganizerException(e);
				}
			}
		}
		Map<String, List<ParentAccountVO>> result = new HashMap<String, List<ParentAccountVO>>();
		for(ParentAccountVO accountVO : records.values()){
			if(!result.containsKey(accountVO.getAccType())){
				result.put(accountVO.getAccType(), new ArrayList<ParentAccountVO>());
			}
			result.get(accountVO.getAccType()).add(accountVO);
		}
		return result;
	}
	
	private float calculateMaturityAmount(Date date1, Date date2,int depositamount){
		float maturity_amount = 0.00f;
		float rate= 0.00f;
		int days;
		days = (int) ((date2.getTime() - date1.getTime()) / (1000*60*60*24));
		if(days >= 0 && days <= 200){
			rate = 6.75f;
		}else if(days >= 201 && days <= 400){
			rate = 7.5f;
		}else if(days >= 401 && days <= 600){
			rate = 8.75f;
		}else if(days > 600){
			rate = 10f;
		}
		maturity_amount = depositamount + (depositamount * rate /100);
	// Write your code here
		return maturity_amount;
		
	}
	
	public static boolean validateData(String[] str) {
		if(str.length != 7){
			return false;
		}
		for(String s : str){
			if(s.length() == 0){
				return false;
			}
		}
		Pattern accountNumberPatern = Pattern.compile("^[1-9][0-9]*$");
		if(!accountNumberPatern.matcher(str[0]).matches()){
			return false;
		}
		try{
			SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
			sdf.parse(str[5]);
			sdf.parse(str[6]);
		}catch(ParseException e){
			return false;
		}
		String accountType = str[2];
		if(!("SAV".equals(accountType) || "NRI".equals(accountType) || "WM".equals(accountType))){
			return false;
		}
		String linkAccountType = str[3];
		if(!(linkAccountType.startsWith("FD-") || linkAccountType.startsWith("RD-") || linkAccountType.startsWith("MUT-"))){
			return false;
		}
		return true;
	}
	

}

class ParentAccountVO {

	private int parentAccNo;
	private String name;
	private String AccType;
	//private LinkedDepositVO linkedDeposit;
	private List<LinkedDepositVO> linkedDeposits;

	public int getParentAccNo() {
		return parentAccNo;
	}

	public void setParentAccNo(int parentAccNo) {
		this.parentAccNo = parentAccNo;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAccType() {
		return AccType;
	}

	public void setAccType(String accType) {
		AccType = accType;
	}

	public List<LinkedDepositVO> getLinkedDeposits() {
		return linkedDeposits;
	}

	public void setLinkedDeposits(List<LinkedDepositVO> linkedDeposits) {
		this.linkedDeposits = linkedDeposits;
	}

	public boolean equals(Object object) {
		boolean isEqual = false;
		ParentAccountVO otherAccount = (ParentAccountVO) object;
		if ((this.parentAccNo == otherAccount.parentAccNo)
				&& (this.AccType.equals(otherAccount.getAccType()) && (this.linkedDeposits
						.equals(otherAccount.getLinkedDeposits())))) {
			isEqual = true;
		}
		return isEqual;
	}

	

	@Override
	public String toString() {
		return "ParentAccountVO [parentAccNo=" + parentAccNo + ", name=" + name
				+ ", AccType=" + AccType + ", linkedDeposits=" + linkedDeposits
				+ "]";
		
	//	return parentAccNo  + "  , " +  name  + " ," + AccType + " ," +  linkedDeposits;
		
	}

}

class LinkedDepositVO {

	private String linkedDepositNo;
	private int depositAmount;
	private Date depositStartDate;
	private Date depositMaturityDate;
	private float maturityAmount;

	public String getLinkedDepositNo() {
		return linkedDepositNo;
	}

	public void setLinkedDepositNo(String linkedDepositNo) {
		this.linkedDepositNo = linkedDepositNo;
	}

	public int getDepositAmount() {
		return depositAmount;
	}

	public void setDepositAmount(int depositAmount) {
		this.depositAmount = depositAmount;
	}

	public Date getDepositStartDate() {
		return depositStartDate;
	}

	public void setDepositStartDate(Date depositStartDate) {
		this.depositStartDate = depositStartDate;
	}

	public Date getDepositMaturityDate() {
		return depositMaturityDate;
	}

	public void setDepositMaturityDate(Date depositMaturityDate) {
		this.depositMaturityDate = depositMaturityDate;
	}

	public float getMaturityAmount() {
		return maturityAmount;
	}

	public void setMaturityAmount(float maturityAmount) {
		this.maturityAmount = maturityAmount;
	}

	public boolean equals(Object object) {
		boolean isEquals = false;
		LinkedDepositVO depositVO = (LinkedDepositVO) object;
		if (this.linkedDepositNo.equals(depositVO.getLinkedDepositNo())
				&& (this.depositAmount == depositVO.getDepositAmount())
				&& (this.depositStartDate.equals(depositVO
						.getDepositStartDate()))
				&& (this.maturityAmount == depositVO.getMaturityAmount())) {
			isEquals = true;
		}
		return isEquals;
	}

	@Override
	public String toString() {
	
	
		return "LinkedDepositVO [linkedDepositNo=" + linkedDepositNo
				+ ", depositAmount=" + depositAmount + ", depositStartDate="
				+ depositStartDate + ", depositMaturityDate="
				+ depositMaturityDate + ", maturityAmount=" + maturityAmount
				+ "]"; 
		
	//	return linkedDepositNo  + "  , " +  depositAmount  + " ," + depositStartDate + " ," +  depositMaturityDate + "," + maturityAmount;
	}

}

class BankOrganizerException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public BankOrganizerException(String message) {
		super(message);
	}

	public BankOrganizerException(Throwable throwable) {
		super(throwable);
	}

	public BankOrganizerException(String message, Throwable throwable) {
		super(message, throwable);
	}
}

/************************************************************/
/*
 * DO NOT CHANGE THE BELOW CLASS. THIS IS FOR VERIFYING THE CLASS NAME AND
 * METHOD SIGNATURE USING REFLECTION APIs
 */
/************************************************************/
class Validator {

	private static final Logger LOG = Logger.getLogger("Validator");

	public Validator(String filePath, String className, String methodWithExcptn) {
		validateStructure(filePath, className, methodWithExcptn);
	}

	protected final void validateStructure(String filePath, String className,
			String methodWithExcptn) {

		if (validateClassName(className)) {
			validateMethodSignature(methodWithExcptn, className);
		}

	}

	protected final boolean validateClassName(String className) {

		boolean iscorrect = false;
		try {
			Class.forName(className);
			iscorrect = true;
			LOG.info("Class Name is correct");

		} catch (ClassNotFoundException e) {
			LOG.log(Level.SEVERE, "You have changed either the "
					+ "class name/package. Use the default package "
					+ "and class name as provided in the skeleton");

		} catch (Exception e) {
			LOG.log(Level.SEVERE, "There is an error in validating the "
					+ "Class Name. Please manually verify that the "
					+ "Class name is same as skeleton before uploading");
		}
		return iscorrect;

	}

	protected final void validateMethodSignature(String methodWithExcptn,
			String className) {
		Class cls;
		try {

			String[] actualmethods = methodWithExcptn.split(",");
			boolean errorFlag = false;
			String[] methodSignature;
			String methodName = null;
			String returnType = null;

			for (String singleMethod : actualmethods) {
				boolean foundMethod = false;
				methodSignature = singleMethod.split(":");

				methodName = methodSignature[0];
				returnType = methodSignature[1];
				cls = Class.forName(className);
				Method[] methods = cls.getMethods();
				for (Method findMethod : methods) {
					if (methodName.equals(findMethod.getName())) {
						foundMethod = true;
						if ((findMethod.getExceptionTypes().length != 1)) {
							LOG.log(Level.SEVERE, "You have added/removed "
									+ "Exception from '" + methodName
									+ "' method. "
									+ "Please stick to the skeleton provided");
						}
						if (!(findMethod.getReturnType().getName()
								.equals(returnType))) {
							errorFlag = true;
							LOG.log(Level.SEVERE, " You have changed the "
									+ "return type in '" + methodName
									+ "' method. Please stick to the "
									+ "skeleton provided");

						}

					}
				}
				if (!foundMethod) {
					errorFlag = true;
					LOG.log(Level.SEVERE,
							" Unable to find the given public method "
									+ methodName + ". Do not change the "
									+ "given public method name. "
									+ "Verify it with the skeleton");
				}

			}
			if (!errorFlag) {
				LOG.info("Method signature is valid");
			}

		} catch (Exception e) {
			LOG.log(Level.SEVERE,
					" There is an error in validating the "
							+ "method structure. Please manually verify that the "
							+ "Method signature is same as the skeleton before uploading");
		}
	}
}
