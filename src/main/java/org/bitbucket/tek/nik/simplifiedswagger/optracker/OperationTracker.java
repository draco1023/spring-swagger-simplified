package org.bitbucket.tek.nik.simplifiedswagger.optracker;

import java.util.ArrayList;
import java.util.List;

import org.bitbucket.tek.nik.simplifiedswagger.exception.SimplifiedSwaggerException;

import io.swagger.models.Operation;
/**
 *  the goal of this class as of now is mainly to get at the ApiParam later
 * If needed can add more fields into OperationTrackerData
 * 
 *
 */

public class OperationTracker {
	
	public void cleanup()
	{
		dataList.clear();
		//dataList=null;
	}
	private List<OperationTrackerData> dataList= new ArrayList<>();
	public void add(OperationTrackerData data)
	{
		dataList.add(data);
		data.getOperation().getVendorExtensions().put("opIndex", dataList.size()-1);
		
	}
	
	private OperationTrackerData get(int index)
	{
		return dataList.get(index);
	}
	
	public OperationTrackerData get(Operation operation)
	{
		
		Integer opIndex= (Integer) operation.getVendorExtensions().get("opIndex");
		if(opIndex !=null)
		{
			return get(opIndex);
		}
		else
		{
			throw new SimplifiedSwaggerException("operation is missing vendor extension opIndex");
		}
	}
	

}
