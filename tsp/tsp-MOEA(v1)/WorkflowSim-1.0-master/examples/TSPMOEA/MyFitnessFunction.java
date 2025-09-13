/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package TSPMOEA;


import org.uma.jmetal.algorithm.multiobjective.tspmoea.MyUtils;
import org.workflowsim.CondorVM;
import org.workflowsim.FileItem;
import org.workflowsim.Task;
import org.workflowsim.utils.Parameters;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;



class TaskTime {

    public double startTime;
    public double stopTime;
    public double totalExcuteTime;
    
}


class VM {

    public double VMBusyTime;
    public double transmission;
    public double startTime;
    public double endTime;
    public List<TaskTime> taskArray ;
}

public class MyFitnessFunction  {
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------

    //task 0-N
    List<?> availableVMs;
    List<Task> avaliableTasks;
    List<HostInfo> avaliableHosts;

    List<TaskTime> taskTimeList = new ArrayList<>();
    List<VM> vmTimeTranList = new ArrayList<>();
    
    double deadline;
    double budget;
    double energy; //rand algorithm energy

    public List<?> getVmList() {
        return availableVMs;
    }

    public List<Task> getTaskList() {
        return avaliableTasks;
    }

    public MyFitnessFunction() {
        availableVMs = Tool.vmList;
        avaliableTasks = Tool.tasktList;
        avaliableHosts = Tool.HostInfoList;
        deadline = Tool.jobDeadline;
        budget = Tool.jobBudget;
        energy = Tool.jobEnergy;
    }

    public boolean checkDuplicate(ArrayList<Integer> list, int value) {

        return list.contains(value);
    }

    
    
    //task task1-N
    public List<Task> taskLayerProcess(List<Task> taskList1,int taskOrder[]) {
    	List<Task> taskList = new ArrayList<Task>();

    	List<ArrayList<Task>> layerTaskList = new ArrayList<>();
		Task ta;
		int maxLayer = -1;
		for(int i=0;i<taskList1.size();i++) {
			ta = taskList1.get(i);
			if(ta.getDepth()>maxLayer) {
				maxLayer = ta.getDepth();
			}
		}
		
		
		//list
		for(int i=0;i<maxLayer;i++) {
			layerTaskList.add(new ArrayList<Task>());
		}
    	//1.
    	for(int i=0;i<taskList1.size();i++) {
    		ta = taskList1.get(i);
    		ArrayList<Task> taskList2  = layerTaskList.get(ta.getDepth()-1);
    		taskList2.add(ta);
    		layerTaskList.set(ta.getDepth()-1,  taskList2);
    	}
    	
    	//2.
    	for(int i=0;i<layerTaskList.size();i++) {
    		ArrayList<Task> taskList2 = layerTaskList.get(i);
    		ArrayList<Task> taskList3 = new ArrayList<>();
    		int taskIndex[] = new int[taskList2.size()];
    		double taskOrder1[] = new double[taskList2.size()];
    		for(int j=0;j<taskList2.size();j++) {
    			taskIndex[j] = taskList2.get(j).getCloudletId();
    			taskOrder1[j] = (double)taskOrder[taskIndex[j]-1];
    		}
    		//taskorder1
    		int sortIndex[] = MyUtils.sortIndex(taskOrder1);

    		for(int k=0;k<taskList2.size();k++) {
    			taskList3.add(taskList2.get(sortIndex[k]));
    		}
    		layerTaskList.set(i, taskList3);
    	}
    	
    	
    	
    	for(int i=0;i<layerTaskList.size();i++) {
    		ArrayList<Task> taskList2  = layerTaskList.get(i);
    		for(int j=0;j<taskList2.size();j++) {
    			ta = taskList2.get(j);
    			taskList.add(ta);
    		}
    		
    	}
    	
    	
    	return taskList;
    }
    
    
    //DAG
    public void scheduleSimulation(int assign1[],int taskOrder[]) {

    	int assign[] =  assign1.clone();

    	avaliableTasks =  taskLayerProcess(avaliableTasks,taskOrder);

        //vmTimeTranList transmission
        for (int i=0;i<getVmList().size();i++) {
            VM v = new VM();
            v.VMBusyTime = 0.0;
            v.transmission = 0.0;
            v.startTime = 0.0;
            v.endTime = 0.0;
            v.taskArray = new ArrayList<TaskTime>();
            vmTimeTranList.add(v);
           
        }
        //taskTimeList
        for (int i=0;i<getTaskList().size();i++) {
        	TaskTime t = new TaskTime();
        	t.startTime = 0;
        	t.stopTime = 0;
        	t.totalExcuteTime = 0;
        	taskTimeList.add(t); 
        }

        
        //DAG taskList
        for (int i=0;i<getTaskList().size();i++) {

        	
        	Task task = getTaskList().get(i);
            TaskTime t = new TaskTime();
            int taskId = task.getCloudletId()-1;
            int vmId = assign[taskId];
            

           
            //CondorVM 
            CondorVM vm = (CondorVM) getVmList().get(vmId);
            
            if (task.getParentList().isEmpty()) // no parrent，没有父代节点
            {

            	
            	t.startTime = 0.0;
                t.totalExcuteTime = task.getCloudletLength() / vm.getMips();
                t.stopTime = t.startTime + t.totalExcuteTime;
                

                //taskTime, vmTimeTranList
                taskTimeList.set(taskId, t);
//                taskTimeList.add(t);
                VM tmVM = new VM();
                tmVM.VMBusyTime = t.stopTime;
                vmTimeTranList.set(vmId, tmVM);//vm

            } else {

                //find max parrent time
            	
                int parentVmId=-1;
                int parentTaskId = -1;
                double maxTime = -1;
                for (Task parrentTask : task.getParentList()) {
                	parentTaskId = parrentTask.getCloudletId() - 1;
                    if (taskTimeList.get(parentTaskId).stopTime > maxTime) {
                        maxTime = taskTimeList.get(parentTaskId).stopTime;
                    }

                }

 
               // max(availTime,max(FTti))
                double avalTime = vmTimeTranList.get(vmId).VMBusyTime;
                if (avalTime > maxTime) {
                    maxTime = avalTime;
                }

                //startTime
                TaskTime t2 = new TaskTime();
                t2.startTime = maxTime;
                t2.totalExcuteTime = task.getCloudletLength() / vm.getMips();
                t2.stopTime = t2.startTime + t2.totalExcuteTime;

                
                double sumTransferTime = 0;
                double tempTime=0.0;
                VM vmTimeTran  = vmTimeTranList.get(vmId);
                VM vmParTimeTran = new VM(); 
                CondorVM parentVm ;
                for (Task parrentTask : task.getParentList()) {
                	parentTaskId = parrentTask.getCloudletId() - 1;//序号减了1
                	parentVmId = assign[parentTaskId];
                	
                	vmParTimeTran = vmTimeTranList.get(parentVmId);
                    if (vmId != parentVmId) {
                    	parentVm = (CondorVM) getVmList().get(parentVmId);
                    	double tempFileSize = findOutputInput(parrentTask, task);
//                    	double tempFileSize = getOutputSize(parrentTask);
                        tempTime = tempFileSize / (Math.min(vm.getBw(), parentVm.getBw())*1000.0*1000.0*1000.0);
                        sumTransferTime += tempTime;

                        
                        vmParTimeTran.transmission = vmParTimeTran.transmission + tempFileSize;
                        vmTimeTranList.set(parentVmId, vmParTimeTran);
                        vmTimeTran.transmission = vmTimeTran.transmission + tempFileSize;
                        //vmTimeTranList.set(vmId, vmTimeTran);
                    }
                    
                }
 

                t2.stopTime += sumTransferTime;//stop = start + execu + trans
                taskTimeList.set(taskId, t2);
//                taskTimeList.add(t2);

                
                vmTimeTran.VMBusyTime = t2.stopTime;
                vmTimeTranList.set(vmId, vmTimeTran);
                
            }

            
        }
        
       
        List<ArrayList<TaskTime>> vmListTemp = new ArrayList<>();		
		for(int i=0;i<getVmList().size();i++) {
			vmListTemp.add(new ArrayList<TaskTime>());
		}

		ArrayList <TaskTime>taskTimeTempList = new ArrayList<TaskTime>();
        //vmTimeTranList
//        VM vm = new VM();
        Task ta1 ;
        int taskId1 ;
        int vmId1;
        TaskTime taskTime1 ;
        for(int i=0;i<getTaskList().size();i++) {
        	ta1 = (Task)getTaskList().get(i);
        	taskId1 = ta1.getCloudletId()-1;
        	taskTime1 = taskTimeList.get(taskId1);
        	vmId1 = assign[taskId1];
        	taskTimeTempList = vmListTemp.get(vmId1);//得到该vm下的taskarray
        	taskTimeTempList.add(taskTime1);
        	vmListTemp.set(vmId1, taskTimeTempList);
        }
        
        for(int i=0;i<getVmList().size();i++) {
        	
        	vmTimeTranList.get(i).taskArray = vmListTemp.get(i);
        	
        }

    }

    public double findOutputInput(Task parent, Task child) {
        List<FileItem> parentFiles = parent.getFileList();
        List<FileItem> childFiles = child.getFileList();

        double acc = 0.0;

        for (FileItem parentFile : parentFiles) {
            if (parentFile.getType() != Parameters.FileType.OUTPUT) {
                continue;
            }

            for (FileItem childFile : childFiles) {
                if (childFile.getType() == Parameters.FileType.INPUT
                        && childFile.getName().equals(parentFile.getName())) {
                    acc += childFile.getSize();
                    break;
                }
            }
        }

//        //file Size is in Bytes, acc in MB
//        acc = acc / Consts.MILLION; //MILLION:1000000
//        // acc in MB, averageBandwidth in Mb/s
        return acc;
    }
    
    

    //task outputsize
    public double getOutputSize(Task t) {
        double outputSize = 0;
        for (Iterator it = t.getFileList().iterator(); it.hasNext();) {
            FileItem f = (FileItem) it.next();

            if (f.getType() == Parameters.FileType.OUTPUT) {
                outputSize += f.getSize();
            }
        }
        return outputSize ;
    }

    public double getInputSize(Task t) {
        double inputSize = 0;
        for (Iterator it = t.getFileList().iterator(); it.hasNext();) {
            FileItem f = (FileItem) it.next();
            if (f.getType() == Parameters.FileType.INPUT) {
                inputSize += f.getSize();
            }
        }
        return inputSize ;
    }
    
    
    
    //makespan  
//    public  double calMakespan() {
//    	double makespan=0.0;
//    	VM vm;
//    	TaskTime taskTime;
//    	for(int i=0;i<vmTimeTranList.size();i++) {
//    		vm = vmTimeTranList.get(i);
//    		for(int j=0;j<vm.taskArray.size();j++) {
//    			taskTime = vm.taskArray.get(j);
//    			if(taskTime.stopTime > makespan) {
//    				makespan = taskTime.stopTime;
//    			}
//    		}
//    		
//    	}
//    	return makespan;
//    }
    
    public  double calMakespan() {
    	double makespan=0.0;
    	
    	TaskTime taskTime;
    	
    	for(int i=0;i<taskTimeList.size();i++) {
    		taskTime = taskTimeList.get(i);
    		//System.out.println(i+1 + " " + (Tool.allot[i]+1) + " " + taskTime.startTime + "  "+ "tasktime");
    		if (taskTime.stopTime > makespan) {
    			makespan = taskTime.stopTime;
    		}
    		
    	}
    	
    	return makespan;
    }
    
    
    //cost
    public double calCost() {
    	double cpuCost = 0.0;
        double transmissionCost = 0;
        double allCost = 0;
        VM vm;
        TaskTime taskTime;
        CondorVM condorVM;
        
        for(int i=0;i<vmTimeTranList.size();i++) {
        	vm = vmTimeTranList.get(i);
        	condorVM = (CondorVM) getVmList().get(i);
        	if (vm.taskArray.size()!= 0) {
        		for(int j=0;j<vm.taskArray.size();j++) {
        			taskTime = vm.taskArray.get(j);
        			
        			cpuCost = cpuCost + condorVM.getCost()*Math.ceil(taskTime.totalExcuteTime/3600);
        		}
        		transmissionCost = transmissionCost + (vm.transmission/(1000.0*1000*1000))*condorVM.getCostPerBW();
        				
        			
        	}
        	
        }
        allCost = cpuCost + transmissionCost;
        return allCost;
    }
    
    
    public double[] calHostResourceUtilization(double hostActiveTime[]) {

    	List<VMInfo> VMInfoList = Tool.VMInfoList;
    	VMInfo vmInfo;
    	double EPS = 1.0e-14;
    	double maxValue = 1000000000;
    	double minValue = -1;
    	double hostStartTime[] = new double[avaliableHosts.size()];
    	double hostEndTime[] = new double[avaliableHosts.size()];
//    	double hostActiveTime[] = new double[avaliableHosts.size()];
    	double vmToalActiveOnHost[] = new double[avaliableHosts.size()];
    	double hostHasVMs[] = new double[avaliableHosts.size()];
    			
    	double vmExecuTime[] = new double[availableVMs.size()]; 
    	double vmStartTime[] = new double[availableVMs.size()]; 
    	double vmEndTime[] = new double[availableVMs.size()]; 
    	
    	double hostResourceUtilization[] = new double[avaliableHosts.size()];
    	
    	VM vm;
    	TaskTime taskTime;
    	//hostStartTime endTime
    	for(int i=0;i<avaliableHosts.size();i++) {
    		hostStartTime[i] = maxValue;
    		hostEndTime[i] = minValue;
    		hostActiveTime[i] = 0.0;
    		hostResourceUtilization[i] = 0.0;
    		vmToalActiveOnHost[i] = 0.0;
    		hostHasVMs[i] = 0.0;
    	}
    	for(int i=0;i<getVmList().size();i++) {
    		vmExecuTime[i] = 0.0;
    		vmStartTime[i] = maxValue;
    		vmEndTime[i] = minValue;
    	}
    	

    	for(int i=0;i<getVmList().size();i++) {
    		vm = vmTimeTranList.get(i);

    		for(int j=0;j<vm.taskArray.size();j++) {
    			taskTime = vm.taskArray.get(j);
    			
    			vmStartTime[i] = Math.min(vmStartTime[i], taskTime.startTime);
    			vmEndTime[i] = Math.max(vmEndTime[i] , taskTime.stopTime);			
    			vmExecuTime[i] = vmExecuTime[i]+taskTime.totalExcuteTime;

    		}

    		if (vmStartTime[i] == maxValue) { //vm busytime
        		vmStartTime[i] = 0;
        		vmEndTime[i] = 0;
        		vmExecuTime[i] = 0;
    		}
    		
    		
    		
    	}
    	
    	
    	
    	for(int i=0;i<getVmList().size();i++) {
    		vmInfo = VMInfoList.get(i);
    		//host  start time
    		int hostId = vmInfo.hostId-1;
    		if (vmExecuTime[i]!=0) {
        		hostStartTime[hostId] = Math.min(hostStartTime[hostId],vmStartTime[i]);
        		hostEndTime[hostId] = Math.max(hostEndTime[hostId],vmEndTime[i]);
    		}

    		hostHasVMs[hostId] = hostHasVMs[hostId] + 1;
    		vmToalActiveOnHost[hostId] = vmToalActiveOnHost[hostId] + vmExecuTime[i];
    		
    	}
    	
    	for(int i=0;i<hostActiveTime.length;i++) {
    		if (vmToalActiveOnHost[i] == 0) {
    			hostEndTime[i]= 0; 
    			hostStartTime[i] = 0;
    		}
    		hostActiveTime[i] = hostEndTime[i] - hostStartTime[i];	
    	}
    	
    	
    	for(int i=0;i<avaliableHosts.size();i++) {
    		hostResourceUtilization[i] = (vmToalActiveOnHost[i])/(hostActiveTime[i]*hostHasVMs[i]+EPS);

    	}
    	
    	return hostResourceUtilization;
    	
    }
    
    
    //Paper:Power-aware and performance-guaranteed virtual machine placement in the cloud
    public double[] calW1andW2(double power, double paras[]) {
    	paras[0] = (power * (13.0 / (13.0 + 29.0))) / 100.0;
    	paras[1] = (power * (29.0 / (13.0 + 29.0))) / 10000.0;
    	return paras;
    }
    
    public double calEnergyConsumption() {
    	double hostActiveTime[] = new double[avaliableHosts.size()];
    	double hostPowerStatic[] = new double[avaliableHosts.size()];
    	double hostEnergyConsumption[] = new double[avaliableHosts.size()];
    	double paras[] = new double[2];
    	double w1 = -1;// w1 = 1.30447; w2 = 0.02867;
        double w2 = -1;
        double powerDynamic = 0.0;
        double powerDynamicAndStatic = 0.0;
        double computationEnergy = 0.0;
        double transmissionEnergy = 0.0;
        double a = 3* 8.6320e-07; //byte/s
        VM vm;
        double hostResourceUtilization[] = calHostResourceUtilization(hostActiveTime);
        for(int i=0;i<avaliableHosts.size();i++) {
	        hostPowerStatic[i] = avaliableHosts.get(i).powerMax*0.5;
	        calW1andW2(avaliableHosts.get(i).powerMax * 0.5, paras);
	        w1 = paras[0];
	        w2 = paras[1];
	        powerDynamic = w1*hostResourceUtilization[i]*100 + w2*(hostResourceUtilization[i]*hostResourceUtilization[i])*10000;//加了100,10000，去了hostPowerStatic[i]
	        powerDynamicAndStatic = powerDynamic+hostPowerStatic[i];
	        hostEnergyConsumption[i] = powerDynamicAndStatic*hostActiveTime[i];
        }
        
        for(int i=0;i<hostEnergyConsumption.length;i++) {
        	computationEnergy = computationEnergy + hostEnergyConsumption[i];
        	
        }
        for(int i=0;i<getVmList().size();i++) {
        	vm = vmTimeTranList.get(i);
        	transmissionEnergy = transmissionEnergy + vm.transmission*a;
        }
        return computationEnergy + transmissionEnergy;
        
        
    }
    
    
    //USD
    public double userSatisfactionDegree() {

    	double makespan = calMakespan();
    	double userSati = 0.0;
        userSati = (-1/deadline)*makespan + 1;
        
//	    if (userSati < 0) {
//	    	userSati = 0;
//	    }
	    return userSati;
    }

    public double providerProfitMargin() {
    	double actualCost = calCost();
    	double payment = budget;
    	double profitability = (payment-actualCost)/actualCost;
    	return profitability;
    }
    
    public double energyConsumptionImprovement( ) {
    	double randAlgorithmEnergy = energy;
    	double currentEnergy = calEnergyConsumption();
    	double energyImprove = (randAlgorithmEnergy-currentEnergy)/randAlgorithmEnergy;
    	return energyImprove;
    }
}
