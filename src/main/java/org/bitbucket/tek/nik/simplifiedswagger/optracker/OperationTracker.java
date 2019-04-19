package org.bitbucket.tek.nik.simplifiedswagger.optracker;

import java.util.ArrayList;
import java.util.List;
/**
 *  the goal of this class as of now is mainly to get at the ApiParam later
 * If needed can add more fields into OperationTrackerData
 * 
 *
 */

public class OperationTracker {
	
	
	private List<OperationTrackerData> dataList= new ArrayList<>();
	public void add(OperationTrackerData data)
	{
		dataList.add(data);
		data.getOperation().getVendorExtensions().put("opIndex", dataList.size()-1);
		
	}
	
	public OperationTrackerData get(int index)
	{
		return dataList.get(index);
	}
	

}
