package TSPMOEA;




import java.util.Iterator;
import java.util.List;

import org.workflowsim.Task;
import org.workflowsim.planning.BasePlanningAlgorithm;

import org.cloudbus.cloudsim.CloudletSchedulerSpaceShared;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;
import org.workflowsim.CondorVM;
import org.workflowsim.Job;
import org.workflowsim.WorkflowDatacenter;
import org.workflowsim.WorkflowEngine;
import org.workflowsim.WorkflowParser;
import org.workflowsim.WorkflowPlanner;
import org.workflowsim.examples.WorkflowSimBasicExample1;
import org.workflowsim.utils.ClusteringParameters;
import org.workflowsim.utils.OverheadParameters;
import org.workflowsim.utils.Parameters;
import org.workflowsim.utils.ReplicaCatalog;


public class TSPMOEAplanningAlgorithm extends BasePlanningAlgorithm{
	
	//run vm task. 
    public void run()  {
    	 
        // TODO Auto-generated method stub
    	
        
    	//taskList  vmList，creatVM vmlist
//        Tool.tasktList = getTaskList();
////        Tool.vmList = getVmList();
//
//        Tool.tasktList.get(0).getCloudletId();
//        CondorVM vm = (CondorVM)Tool.vmList.get(0);
//        System.out.println(vm.getBw());
        
    	
    	//task   
        for (int i = 0; i < Tool.TaskNum; i++) {
            Task task = (Task) getTaskList().get(i);
            int vmId = Tool.allot[i];
            task.setVmId(vmId);
        }

    }

}
