package com.github.mviku.cca.ccjp;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TrainServiceManager {
	
	
	/* Return the list of trains for the given parameter */
	public List<TrainDetailsVO> getTrainDetails(final String filePath, int source,
			int destination, String dateOfTravel)
			throws TrainServiceException {
		
	//Write the code here
		if(source<1 || source>20 || destination<1 || destination>20 || source == destination) {
			throw new TrainServiceException("Error in params - invalid station");
		}
		List<TrainDetailsVO> result = new ArrayList<TrainDetailsVO>();
		try {
			Date travelDate = new SimpleDateFormat("dd-MM-yyyy").parse(dateOfTravel);
			if(travelDate.before(Calendar.getInstance().getTime())) {
				throw new TrainServiceException("Error in params - invalid date");
			}
			Calendar travelDateCal = Calendar.getInstance();
			travelDateCal.setTime(travelDate);
			boolean isSunday = travelDateCal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY;
			List<TrainDetailsVO> allTrains = fetchDetails(filePath);
			for(TrainDetailsVO train : allTrains) {
				if(source == train.getSource() && destination == train.getDestination() && (isSunday || 'N' == train.getSpecial() )) {
					train.setDateOfTravel(travelDate);
					result.add(train);
				}
			}
			
		} catch (ParseException e) {
			throw new TrainServiceException(e);
		}
		return result;

	}

	 /* Return the special trains */
	public Map getTrainSchedule(String filePath) throws TrainServiceException {

	//Write the code here
		List<TrainDetailsVO> allTrains = fetchDetails(filePath);
		Map<Integer, TreeSet<Integer>> result = new TreeMap<Integer, TreeSet<Integer>>();
		TreeSet<Integer> trains = new TreeSet<Integer>();
		result.put(1, trains);
		for(TrainDetailsVO train : allTrains) {
			if('N' == train.getSpecial()) {
				trains.add(Integer.valueOf(train.getTrainNumber()));
			}
		}
		return result;
	}
	
	private List<TrainDetailsVO> fetchDetails(final String filePath) throws TrainServiceException{
		FileReader fr = null;
		BufferedReader br = null;
		List<TrainDetailsVO> details = new ArrayList<TrainDetailsVO>();
		try {
			fr = new FileReader(filePath);
			br = new BufferedReader(fr);
			String line = null;
			while((line = br.readLine()) != null) {
				String[] data = line.split(",|\\|");
				validate(data);
				TrainDetailsVO train = new TrainDetailsVO();
				train.setTrainNumber(data[0]);
				train.setRoute(data[1]);
				train.setSource(Integer.valueOf(data[2]));
				train.setDestination(Integer.valueOf(data[3]));
				train.setSpecial(data[4].charAt(0));
				details.add(train);
			}
		}catch(FileNotFoundException e){
			throw new TrainServiceException(e);
		} catch (IOException e) {
			throw new TrainServiceException(e);
		}finally {
			if(fr != null) {
				try {
					fr.close();
				} catch (IOException e) {
				}
			}
			if(br != null) {
				try {
					br.close();
				} catch (IOException e) {
				}
			}
		}
		return details;
	}
	
	private void validate(String[] data) throws TrainServiceException{
		if(!checkRegex("^[0-9]{5}$", data[0]) 
				|| !validateTrainStation(data[1], data[2], data[3])
				|| !("Y".equals(data[4]) || "N".equals(data[4]))
			){
			throw new TrainServiceException("Invalid file input");
		}
	}
	
	private boolean validateTrainStation(String route, String start, String end) {
		int source = Integer.parseInt(start);
		int dest = Integer.parseInt(end);
		if("TR1".equals(route) && source>=1 && dest<=10 && source<dest) {
			return true;
		}else if("TR2".equals(route) && source>=11 && dest<=20 && source<dest) {
			return true;
		}
		return false;
	}
	
	private boolean checkRegex(String pattern, String input) {
		Pattern pat = Pattern.compile(pattern);
		Matcher matcher = pat.matcher(input);
		return matcher.find();
	}
	
}

/* Train Detail Value Object - DO NOT CHANGE*/
class TrainDetailsVO {
	private String trainNumber;
	private String route;
	private int source;
	private int destination;
	private char special;
	private Date dateOfTravel;
	
	public String getTrainNumber() {
		return trainNumber;
	}

	public void setTrainNumber(final String trainNumber) {
		this.trainNumber = trainNumber;
	}

	public String getRoute() {
		return route;
	}

	public void setRoute(final String route) {
		this.route = route;
	}

	public int getSource() {
		return source;
	}

	public void setSource(final int source) {
		this.source = source;
	}

	public int getDestination() {
		return destination;
	}

	public void setDestination(final int destination) {
		this.destination = destination;
	}

	public char getSpecial() {
		return special;
	}

	public void setSpecial(final char special) {
		this.special = special;
	}

	public void setDateOfTravel(final Date dateOfTravel){
		this.dateOfTravel= dateOfTravel;
	}
	public Date getDateOfTravel(){
		return dateOfTravel;
	}
	    
	

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		TrainDetailsVO other = (TrainDetailsVO) obj;
		
		if (trainNumber == null) {
			if (other.trainNumber != null) {
				return false;
			}
		} else if (!trainNumber.equals(other.trainNumber)) {
			return false;
		}
		if (route == null) {
			if (other.route != null) {
				return false;
			}
		} else if (!route.equals(other.route)) {
			return false;
		}
		if (special == ' ') {
			if (other.special != ' ') {
				return false;
			}
		} else if (special != other.special) {
			return false;
		}
		if (destination != other.destination) {
			return false;
		}
		if (source != other.source) {
			return false;
		}
	
		return true;
	}

}

/* User defined Exception - DO NOT CHANGE */
class TrainServiceException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public TrainServiceException(String message) {
		super(message);
	}

	public TrainServiceException(Throwable throwable) {
		super(throwable);
	}
}