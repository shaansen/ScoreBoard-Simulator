import java.io.*;
import java.util.*;

import javax.swing.event.TreeExpansionEvent;

import static java.lang.Math.toIntExact;

public class Main{
	
	public static Map<String,Long> registers = new TreeMap<String,Long>();
	public static int[][] Scoreboard;
	public static String[][] HazardBoard;
	public static FunctionalUnit[] funcUnit;
	public static Instruction[] instruction; 
	public static Set<String> beingWritten = new HashSet<String>();
	public static Map<Integer,FunctionalUnit> iToFMap = new HashMap<Integer, FunctionalUnit>();
	public static Map<String,Integer> funcCycleMap = new HashMap<String,Integer>();
	public static Map<Integer,Long> WordDataMap = new TreeMap<Integer,Long>();
	public static Map<Integer,Long> DoubleDataMap = new TreeMap<Integer,Long>();
	public static Map<Integer,Boolean> hitOrMiss = new TreeMap<Integer, Boolean>();
	public static Map<Integer,Integer> dataHitOrMiss = new TreeMap<Integer, Integer>();
	public static boolean[] fetchComplete;
	public static boolean[] issueComplete;
	public static boolean[] readComplete;
	public static boolean[] executeComplete;
	public static boolean[] writeComplete;
	public static boolean[] complete;
	public static int[] lastCompletedCycle;
	public static int endCondition = -1;
	public static int instructionCycle;
	public static int[][] instructionCache;
	public static int[] instructionCacheHits;
	public static int instructionCount=1;
	public static FileWriter fw;
	private static BufferedReader bufferedReader;
	public static int lastProcessedCycle = 0;
	public static int startCounter = 0;
	public static Map<Integer,Boolean> busBusy = new TreeMap<Integer, Boolean>();
	public static Queue<Integer> set0 = new LinkedList<Integer>();
	public static Queue<Integer> set1 = new LinkedList<Integer>();
	public static int instructionCacheHitCounter = 0;
	public static int instructionAccessCounter = 0;
	public static int dataCacheHitCounter = 0;
	public static int dataAccessCounter = 0;
	public static String instructionFile;
	public static String dataFile;
	public static String configFile;
	public static String resultFile;
	
	/*-----------------------------------------------------------------------------------------------
	FETCHRESOLVED CHECKS IF ALL FETCH INSTRUCTIONS ARE RESOLVED
		1. CHECKS FOR INSTRUCTION CACHE & DATA CACHE MISSES
		   IF NO MISSES, RETURNS TRUE, ELSE FALSE
	------------------------------------------------------------------------------------------------*/
	/*public static int[][] initializedataCache() throws IOException
	{
		int[][] result = new int[4][4];
		for (int i = 0; i < result.length; i++) {
			for (int j = 0; j < result[i].length; j++) {
				result[i][j]=-1;
			}
		}
		
		for (int index = 0; index < instructionCount; index++) {
			dataHitOrMiss.put(index,0);
			}
		
		return result;
	}*/
	
	public static void filldataCache(int index,int instructionCycle)
	{
			int cycle_counter = 0;
			int instr_counter = instructionCycle;
			int bus_busy_counter = index;
			Instruction instructionData = instruction[index];
			String op = instructionData.operation;
			
			//if load instruction
			if(op.equals("L.D") || op.equals("LW") || op.equals("S.D") || op.equals("SW"))
			{
				dataCacheHitCounter++;
			// if busBusy then add cycles till the bus gets free
				while(busBusy.get(instr_counter))
				{
					cycle_counter++;
					instr_counter++;
				}
				// if busbusy is free
				if(!busBusy.get(instr_counter))
				{
					// check load instruction - LW for one operand
					if(op.equals("L.D") || op.equals("S.D"))
					{
						long src1 = resolveMemoryAddressing(instructionData.source1);
						long src2 = src1+4;
						int totalCyclesBusy = 0;
						if(checkInInstructionCache(src1))
						{
							if(checkSecondInInstructionCache(findSetNumber(src1),src2))
							{
								totalCyclesBusy = cycle_counter;
								dataHitOrMiss.put(index,cycle_counter);	
							}
							else
							{
								checkInInstructionCache(src2);
								totalCyclesBusy = cycle_counter+12;
								dataHitOrMiss.put(index,cycle_counter+12);
							}
						}
						else
						{
							if(checkSecondInInstructionCache(findSetNumber(src1),src2))
							{
								checkInInstructionCache(src2);
								totalCyclesBusy = cycle_counter+12;
								dataHitOrMiss.put(index,cycle_counter+12);
							}
							else
							{
								checkInInstructionCache(src2);
								totalCyclesBusy = cycle_counter+24;
								dataHitOrMiss.put(index,cycle_counter+24);
							}
						}
						
						
					}
					else if(op.equals("LW") || op.equals("SW"))
						
						
						{
						long src1 = resolveMemoryAddressing(instructionData.source1);
						int totalCyclesBusy = 0;
						if(checkInInstructionCache(src1))
						{
							totalCyclesBusy = cycle_counter;
							dataHitOrMiss.put(index,cycle_counter);	
						}
						else
						{
								totalCyclesBusy = cycle_counter+12;
								dataHitOrMiss.put(index,cycle_counter+12);
						}
					}
				}
			}
			else
			{
				dataHitOrMiss.put(index,0);
			}
	}
	
	public static boolean checkSecondInInstructionCache(int set,long src2)
	{
		dataCacheHitCounter++;
		if(set==0)
		{
			Iterator<Integer> i = set0.iterator();
			while(i.hasNext())
			{
				int start = i.next();
				int end = start+16;
				if(src2>=start && src2<end)
				{
					dataAccessCounter++;
					return true;
				}
			}
		}
		if(set==1)
		{
			Iterator<Integer> i = set1.iterator();
			while(i.hasNext())
			{
				
				int start = i.next();
				int end = start+16;
				if(src2>=start && src2<end)
				{
					dataAccessCounter++;
					return true;
				}
			}
		}
		return false;
	}
	
	public static void updateBusBusy()
	{
		
	}
	
	public static boolean checkInInstructionCache(long src)
	{
		int setNumber = findSetNumber(src);
		int startNumber = findStartNumber(src);
		if(setNumber==0)
		{
			if(set0.size()==0)
			{
				set0.add(startNumber);
				return false;
			}
			else
			{
				if(set0.size()==2)
				{
				Iterator<Integer> i = set0.iterator();
				while(i.hasNext())
				{
					if(i.next()==startNumber)
					{
						set0.remove(startNumber);
						set0.add(startNumber);
						dataAccessCounter++;
						return true;
					}
					
				}
				set0.remove();
				set0.add(startNumber);
				return false;
				}
				else
				{
					Iterator<Integer> i = set0.iterator();
					while(i.hasNext())
					{
						if(i.next()==startNumber)
						{
							dataAccessCounter++;
							return true;
						}
						
					}
					set0.add(startNumber);
					return false;
					}
			}
		}
		if(setNumber==1)
		{
			if(set1.size()==0)
			{
				set1.add(startNumber);
				return false;
			}
			else
			{
				if(set1.size()==2)
				{
				Iterator<Integer> i = set1.iterator();
				while(i.hasNext())
				{
					if(i.next()==startNumber)
					{
						set1.remove(startNumber);
						set1.add(startNumber);
						return true;
					}
					
				}
				set1.remove();
				set1.add(startNumber);
				return false;
				}
				else
				{
					Iterator<Integer> i = set1.iterator();
					while(i.hasNext())
					{
						if(i.next()==startNumber)
						{
							return true;
						}
						
					}
					set1.add(startNumber);
					return false;
					}
			}
		}
		
		return false;
	}
	
	public static int findSetNumber(long src)
	{
		long x = (src/16)%2;
		return toIntExact(x);
	}
	
	public static int findStartNumber(long src)
	{
		long x = src-(src%16);
		return toIntExact(x);
	}
	
	
	public static long resolveMemoryAddressing(String operand)
	{
		long output = (long)0;
		if(operand.contains("("))
		{
			int offset = Integer.parseInt(operand.substring(0,operand.indexOf("(")));
			int disp = toIntExact(registers.get(operand.substring(operand.indexOf("(")+1,operand.length()-1)));
			return offset+disp;
		}
		else
		{
			output = Long.parseLong(operand);
			return output;
		}
		
	}
	
	public static void initializeBusyBus()
	{
	for (int i = 0; i < 1000; i++) {
		busBusy.put(i,false);
	}	
	}
	
	
	public static int convertToAddress(String operand)
	{
		int offset = Integer.parseInt(operand.substring(0,operand.indexOf("(")));
		int disp = toIntExact(registers.get(operand.substring(operand.indexOf("(")+1,operand.length()-1)));
		return offset+disp;
	}
	
	public static void fillInstructionCache()
	{
		for (int index = 0; index < instructionCount; index++) {
			instructionAccessCounter++;
			int value = index%(instructionCache.length*instructionCache[0].length);
			int block_to_search = value/instructionCache[0].length;
			int word_inside_block = value%instructionCache[0].length;
			if(instructionCache[block_to_search][word_inside_block]!=index)
			{
				int start = index - word_inside_block;
				for (int i = 0; i < instructionCache[0].length; i++) {
					instructionCache[block_to_search][i]=start;
					start++;
				}
				hitOrMiss.put(index,false);
			}
			else
			{
				instructionCacheHitCounter++;
				hitOrMiss.put(index,true);
			}
			
		}
	}
	
	public static Map<Integer,Boolean> reupdateHitOrMiss(int startCounter)
	{
		Map<Integer,Boolean> horm = new HashMap<Integer,Boolean>();
		for (int index = startCounter; index < instruction.length+startCounter; index++) {
			int value = index%(instructionCache.length*instructionCache[0].length);
			int block_to_search = value/instructionCache[0].length;
			int word_inside_block = value%instructionCache[0].length;
			if(instructionCache[block_to_search][word_inside_block]!=index)
			{
				int start = index - word_inside_block;
				for (int i = 0; i < instructionCache[0].length; i++) {
					instructionCache[block_to_search][i]=start;
					start++;
				}
				horm.put(index-startCounter,false);
				
			}
			else
			{
				horm.put(index-startCounter,true);
			}
			
		}
		return horm;
	}
	
	
	public static void printInstructionCache()
	{
		for (int i = 0; i < instructionCache.length; i++) {
			System.out.println();
			for (int j = 0; j < instructionCache[i].length; j++) {
				System.out.print(instructionCache[i][j]+"\t");
			}
		}
		System.out.println();
	}
	
	public static int[][] initializeInstructionCache() throws IOException
	{
		String functionalUnit_file = configFile;
		String line = null;
		FileReader fileReader = new FileReader(functionalUnit_file);
		bufferedReader = new BufferedReader(fileReader);
		String needed = "";
		while((line = bufferedReader.readLine()) != null) {
		   needed = line;
		}  
		int no_of_blocks = Integer.parseInt(needed.substring(needed.indexOf(":")+1,needed.indexOf(",")).replaceAll(" ",""));
		int blocks_size = Integer.parseInt(needed.substring(needed.indexOf(",")+1).replaceAll(" ",""));
		int[][] result = new int[no_of_blocks][blocks_size];
		for (int i = 0; i < result.length; i++) {
			for (int j = 0; j < result[i].length; j++) {
				result[i][j]=-1;
			}
		}
		return result;
	}
	
	/*-----------------------------------------------------------------------------------------------
	ISSUERESOLVED CHECKS IF ALL ISSUE INSTRUCTIONS ARE RESOLVED
		1. CHECKS FOR WAW HAZARDS
		2. CHECKS FOR STRUCTURAL HAZARDS
		   IF BOTH CHECKS DONT HAVE ANY ISSUES THEN RETURNS TRUE, ELSE FALSE
	------------------------------------------------------------------------------------------------*/
	public static boolean issueResolved(int index)
	{
		Instruction instructionIssue = instruction[index];
		String usedFuncUnit = Instruction.determineFunctionalUnitFromInstruction(instructionIssue);
		boolean unitFree  = checkIfStructuralHazards(usedFuncUnit,index);
		boolean WawHazard = checkIfWAWHazards(index); 
		
		if(unitFree || WawHazard)
		{
			if(unitFree)
			{
				HazardBoard[index][2] = "Y";
			}
			if(WawHazard && !(instructionIssue.operation.equals("SW") || instructionIssue.operation.equals("S.D")))
			{
				HazardBoard[index][1] = "Y";
			}
			return false;
		}
		for (int i = 0; i < funcUnit.length; i++) {
			if(funcUnit[i].name.equals(usedFuncUnit) && !funcUnit[i].busy)
			{
				funcUnit[i].useFunctionalUnit(funcUnit[i], instructionIssue,beingWritten);
				iToFMap.put(index, funcUnit[i]);
				return true;
			}
		}
		return false;
	}

	/*-----------------------------------------------------------------------------------------------
	checkIfWAWHazards - CHECKS IF THERE IS ANY REGISTER IN THE FUNCTIONAL UNIT
	WHICH MATCHES THE DESTINATION REGISTER OF THE INSTRUCTION
	IF YES, THEN IT MARKS THE HAZARDS TABLE, AND RETURNS TRUE
	IF NO, IT RETURNS FALSE
	------------------------------------------------------------------------------------------------*/
	public static boolean checkIfWAWHazards(int index)
	{
		Instruction instructionWAW = instruction[index];
		String dest = instructionWAW.destination;
		if(!(instructionWAW.operation.equals("S.D") && !instructionWAW.operation.equals("SW")))
		{
			for (int i = 0; i < funcUnit.length; i++) 
			{
				if(funcUnit[i].Fi.equals(dest) && dest!="")
				{
					HazardBoard[index][0] = "Y";
					return true;
				}
			}
		}
		return false;
	}
	
	/*-----------------------------------------------------------------------------------------------
	checkIfStructuralHazards - CHECKS IF THERE IS ANY FUNCTIONAL UNIT FREE TO EXECUTE THE INSTRUCTION
	IF YES, THEN IT FINDS THE FUNCTIONAL UNIT AND MARKS IT AS BUSY, AND RETURNS TRUE
	IF NO, IT RETURNS FALSE
	------------------------------------------------------------------------------------------------*/
	public static boolean checkIfStructuralHazards(String requiredFunctionalUnit,int index)
	{
		for (int i = 0; i < funcUnit.length; i++) {
			if(funcUnit[i].name.equals(requiredFunctionalUnit) && !funcUnit[i].busy)
			{
				return false;
			}
		}
		return true;	
	}
	
	
	/*-----------------------------------------------------------------------------------------------
	READRESOLVED CHECKS IF ALL READ INSTRUCTIONS ARE RESOLVED
		1. CHECKS FOR RAW HAZARDS
		   IF NO ISSUES THEN RETURNS TRUE, ELSE FALSE
	------------------------------------------------------------------------------------------------*/
	public static boolean readResolved(int index,int cycle)
	{
	Instruction instructionRead = instruction[index];
	String opType = instructionRead.instructionType;
	if(opType=="OP3")
	{
		boolean b1 = searchRegisterInFunctionalUnit(index, instructionRead.source1);
		boolean b2 = searchRegisterInFunctionalUnit(index, instructionRead.source2);
		if(b1 || b2)
		{
			return false;
		}
	}
	else if(opType=="OP2")
	{
		String dest = instructionRead.operation;
		if(dest.equals("SW") || dest.equals("S.D") || dest.equals("SI") || dest.equals("SUI"))
		{
			boolean b1 = searchRegisterInFunctionalUnit(index, instructionRead.destination);
			boolean b2 = searchRegisterInFunctionalUnit(index, instructionRead.source1);
			
			if(b1 || b2)
			{
				return false;
			}
		}
		boolean b1 = searchRegisterInFunctionalUnit(index, instructionRead.source1);
		if(b1)
		{
			return false;
		}
	}
	return true;	
	}
	
	/*-----------------------------------------------------------------------------------------------
	EXECUTE HANDLER ACTUALLY EXECUTES THE INSTRUCTIONS AND TRANSFERS VALUES IN THE
	REGISTERS
	DEPENDING ON THE TYPE OF THE INSTRUCTION, IT CALLS THE 
	INSTEXECUTEOP2 FOR 2 OPERANDS
	INSTEXECUTEOP3 FOR 3 OPERANDS
	------------------------------------------------------------------------------------------------*/
	public static void executeHandler(int index)
	{
		
		Instruction instructionExec = instruction[index];
		String iType = instruction[index].instructionType;
		String op = instructionExec.operation;
		if(iType.equals("OP3"))
		{
			Long src1 = resolveRegisterValues(instructionExec.operation,instructionExec.source1);
			Long src2 = resolveRegisterValues(instructionExec.operation,instructionExec.source2);
			String dest = instructionExec.destination;
			instExecuteOP3(op,dest, src1, src2,index);
		}
		else if(iType.equals("OP2"))
		{
			Long src1 = resolveRegisterValues(instructionExec.operation,instructionExec.source1);
			String dest = instructionExec.destination;
			instExecuteOP2(op,dest, src1,index);
			
		}
		else
		{
			//System.out.println("HLT");
		}
	}
	
	/*-----------------------------------------------------------------------------------------------
	INSTEXECUTEOP3 DETERMINES THE INSTRUCTION TYPE AND UPDATES THE REGISTERS
	THERE IS SPECIAL LOGIC TO HANDLE BRANCHING INSTRUCTIONS
	------------------------------------------------------------------------------------------------*/
	public static void instExecuteOP3(String op,String destination,Long src1,Long src2,int index)
	{
		
		if(op.equals("DADD") || op.equals("DADDI") || op.equals("ADD.D"))
		{
			registers.put(destination,(src1+src2));
		}
		else if(op.equals("DSUB") || op.equals("SUB.D") || op.equals("DSUBI"))
		{
			registers.remove(destination);
			registers.put(destination,(src1-src2));
		}
		else if(op.equals("MUL.D"))
		{
			registers.put(destination,(src1*src2));
		}
		else if(op.equals("DIV.D"))
		{
			registers.put(destination,(src1/src2));
		}
		else if(op.equals("AND") || op.equals("ANDI"))
		{
			registers.put(destination,(src1&src2));
		}
		else if(op.equals("OR") || op.equals("ORI"))
		{
			registers.put(destination,(src1|src2));
		}
		else if(op.equals("BNE"))
		{
			if(!src1.equals(src2))
			{
				endCondition = searchIndex(destination);
			}
		}
		else if(op.equals("BEQ"))
		{
			if(src1.equals(src2))
			{
				endCondition = searchIndex(destination);
			}
		}
		
	}
	
	public static int searchIndex(String destination)
	{
		for (int i = 0; i < instruction.length; i++) {
			if(instruction[i].label.equals(destination))
			{
				return i;
			}
		}
		return instruction.length;
	}
	
	/*-----------------------------------------------------------------------------------------------
	INSTEXECUTEOP2 DETERMINES THE INSTRUCTION TYPE AND UPDATES THE REGISTERS
	THERE IS SPECIAL LOGIC TO LOAD AND STORE INSTRUCTIONS
	------------------------------------------------------------------------------------------------*/
	public static void instExecuteOP2(String op,String destination,Long src1,int index)
	{
		if(op.equals("LW") || op.equals("L.D") || op.equals("LI") || op.equals("LUI"))
		{
			registers.put(destination,(src1));
			
		}
		if(op.equals("SW"))
		{
			WordDataMap.put(toIntExact(registers.get(destination)),src1);
			//loadBackToMemory(toIntExact(registers.get(destination)),src1);
			
			
		}
		if(op.equals("S.D"))
		{
			DoubleDataMap.put(toIntExact(src1),registers.get(destination));
			//loadBackToMemory(toIntExact(registers.get(destination)),src1);
			
		}
		
	}
	
	public static void loadBackToMemory(long address,long content) throws IOException
	{
		String hexAddress = Long.toHexString(address);
		List<String> lines = new ArrayList<String>();
	    String line = null;
		File f1 = new File(dataFile);
        FileReader fr = new FileReader(f1);
        BufferedReader br = new BufferedReader(fr);
        while ((line = br.readLine()) != null) {
            if (line.contains("java"))
                line = line.replace("java", " ");
            lines.add(line);
        }
        fr.close();
        br.close();
        
		FileWriter fw = new FileWriter(dataFile);
        BufferedWriter out = new BufferedWriter(fw);
        for(int i=256;i==address;i+=4)
        {
        	out.write(Long.toBinaryString(content));
        }
        out.flush();
        out.close();
		
	}
	
	
	/*-----------------------------------------------------------------------------------------------
	RESOLVEREGISTERVALUES RETURNS THE LONG VALUE FOR THE REGISTER AS PARAMETER
	IT HANDLES THE IMMEDIATE, REGISTERS AND DISPLACEMENT VALUES FOR THE REGISTER
	------------------------------------------------------------------------------------------------*/
	public static Long resolveRegisterValues(String operation, String operand)
	{
		String op = operation;
		Long output = (long)0;
		if(operand.contains("("))
		{
			int offset = Integer.parseInt(operand.substring(0,operand.indexOf("(")));
			int disp = toIntExact(registers.get(operand.substring(operand.indexOf("(")+1,operand.length()-1)));
			if(operation.equals("L.D"))
			{
				return DoubleDataMap.get(offset+disp);
			}
			else
			{
			return WordDataMap.get(offset+disp);
			}
		}
		else if(operand.contains("R") || operand.contains("F"))
		{
			return registers.get(operand);
		}
		else
		{
			output = Long.parseLong(operand);
			return output;
		}
		
	}
	
	/*-----------------------------------------------------------------------------------------------
	SEARCH REGISTER IN FUNCTIONAL UNIT CHECKS IF THE SOURCE OPERANDS FOR
	AN INSTRUCTION ARE CURRENTLY BEING WRITTEN BY ANY OTHER INSTRUCTION.
	THE INDEX PARAMETER IS TO CHECK IF THE INSTRUCTION WRITING INTO THE 
	SOURCE REGISTER WAS ISSUED EARLIER THAN THE CURRENT INSTRUCTION.
	------------------------------------------------------------------------------------------------*/
	public static boolean searchRegisterInFunctionalUnit(int index,String src)
	{
	src = getRegisterFromOperand(src);
	for (Map.Entry<Integer, FunctionalUnit> pair : iToFMap.entrySet()) {
		if(pair.getKey()<index && (pair.getValue().Fi.equals(src)) && !pair.getValue().operation.equals("SW"))
	    {
	    	return true;
	    }
	}
	return false;
	}
	
	/*-----------------------------------------------------------------------------------------------
	GETREGISTERFROMOPERAND HANDLES THE DISPLACEMENT ADDRESSING MODES
	BY RETURNING THE REGISTER BEING USED AS THE DISPLACEMENT
	--------------------------------------------------------+			----------------------------------------*/
	public static String getRegisterFromOperand(String src)
	{
		
		String result = src;
		if(src.contains("("))
		{
			result = src.substring(src.indexOf("(")+1,src.length()-1);
			
		}
		return result;
		
	}
	
	/*-----------------------------------------------------------------------------------------------
	writeResolved CHECKS IF ALL EXECUTE INSTRUCTIONS ARE RESOLVED
		1. CHECKS IF CYCLE FOR THE FUNCTIONAL UNIT IS COMPLETE
		   IF NO ISSUES THEN RETURNS TRUE, ELSE FALSE
	------------------------------------------------------------------------------------------------*/
	public static boolean writeResolved(int index)
	{
	
	Instruction instructionExec = instruction[index];
	FunctionalUnit funcUnit = iToFMap.get(index);
	String destination = instructionExec.destination;
	String op = instructionExec.operation;
	/*if(op.equals("SW")||op.equals("S.D")||op.equals("SI")||op.equals("SUI"))
	beingWritten.add(destination);*/
	if(funcUnit.Rj && funcUnit.Rk)
		{
			if(funcUnit.cycles!=1)
			{
				funcUnit.cycles--;
				return false;
			}
			
			return true;	
		}
	return false;
	}
	
	/*-----------------------------------------------------------------------------------------------
	RESET FUNCTIONAL UNIT CLEANS THE FUNCTIONAL UNIT
	AND UPDATES THE VALUES FOR THE SOURCE REGISTERS BY MARKING THEM AS AVAILABLE
	IN CASE THEY WERE EARLIER BEING WRITTEN BY A FUNCTIONAL UNIT.
	------------------------------------------------------------------------------------------------*/
	public static void resetFunctionalUnit(int index,int cycle)
	{
	resetRjRk(index);
	FunctionalUnit funcUnit = iToFMap.get(index);
	funcUnit.setBusy(false);
	funcUnit.setOperation("");
	funcUnit.setFi("");
	funcUnit.setFj("");
	funcUnit.setFk("");
	funcUnit.setRj(true);
	funcUnit.setRk(true);
	funcUnit.setCycles(funcCycleMap.get(funcUnit.name));
	beingWritten.remove(instruction[index].destination);
	iToFMap.remove(index);

	}
	
	/*-----------------------------------------------------------------------------------------------
	RESETRJRK BASICALLY RESETS THE SOURCE REGISTERS IN THE FUNCTIONAL UNIT
	------------------------------------------------------------------------------------------------*/
	public static void resetRjRk(int index)
	{
		Instruction instructionReset = instruction[index];
		String src1 = instructionReset.destination;
		for (int i = 0; i < funcUnit.length; i++) {
			if(funcUnit[i].Fj.equals(src1))
			{
				funcUnit[i].Rj=true;
			}
			if(funcUnit[i].Fk.equals(src1))
			{
				funcUnit[i].Rk=true;
			}
		}
	}


	/*-----------------------------------------------------------------------------------------------
	INITIALIZE TRUE RETURNS A BOOLEAN ARRAY OF ALL TRUE VALUES
	THIS ARRAY IS USED FOR CHECKING IF FUNCTIONAL UNITS ARE AVAILABLE
	------------------------------------------------------------------------------------------------*/
	public static boolean[] initializeTrue(int count)
	{
		boolean[] result = new boolean[count];
		for (int i = 0; i < result.length; i++) {
			result[i] = true;
		}
		return result;
	}
	
	
	/*-----------------------------------------------------------------------------------------------
	INITIALIZEZERO RETURNS A BOOLEAN ARRAY OF ALL ZERO VALUES
	
	------------------------------------------------------------------------------------------------*/
	public static int[] initializeZero(int count)
	{
		int[] result = new int[count];
		for (int i = 0; i < result.length; i++) {
			result[i] = 0;
		}
		return result;
	}
	
	
	/*-----------------------------------------------------------------------------------------------
	INITIALIZE FALSE RETURNS A BOOLEAN ARRAY OF ALL FALSE VALUES
	THIS ARRAY IS USED FOR CHECKING IF FUNCTIONAL UNITS ARE AVAILABLE
	------------------------------------------------------------------------------------------------*/
	public static boolean[] initializeFalse(int count)
	{
		boolean[] result = new boolean[count];
		for (int i = 0; i < result.length; i++) 
		{
			result[i] = false;
		}
		return result;
	}
	
	/*-----------------------------------------------------------------------------------------------
	PRINTPFC PRINTS OUT THE FUNCTIONAL UNITS
	------------------------------------------------------------------------------------------------*/
	public static void printFuncUnit(FunctionalUnit[] array)
	{
		System.out.println("**************************************************************");
		System.out.println("Cycles\tOp\tBusy\tFi\tFj\tFk\tRj\tRk");
		for (int i = 0; i < array.length; i++) {
			array[i].print();
		}
		System.out.println("**************************************************************");
	}
	
	
	/*-----------------------------------------------------------------------------------------------
	INITIALIZEREGISTERMAP RETURNS A REGISTER ARRAY ALL INITIALIZED TO 0 VALUES
	------------------------------------------------------------------------------------------------*/
	public static Map<String,Long> initializeRegisterMap()
	{
		Map<String,Long> result = new HashMap<String, Long>();
		for (int i = 0; i < 32; i++) {
			result.put("R"+Integer.toString(i+1),(long)0);
		}
		for (int i = 0; i < 32; i++) {
			result.put("F"+Integer.toString(i+1),(long)0);
		}
		return result;
	}
	
	/*-----------------------------------------------------------------------------------------------
	INITIALIZESCOREBOARD RETURNS AN INTEGER ARRAY REPRESENTING THE SCOREBOARD
	WITH ALL THE VALUES FOR EACH STAGE INITIALIZED TO ZERO
	------------------------------------------------------------------------------------------------*/
	public static int[][] initializeScoreboard(int count)
	{
		int[][] result = new int[count][5];
		for (int i = 0; i < count; i++) {
			for (int j = 0; j < 5; j++) {
				result[i][j] = -1;
			}
		}
		return result;
	}
	
	/*-----------------------------------------------------------------------------------------------
	INITIALIZEHAZARDBOARD RETURNS AN STRING ARRAY REPRESENTING THE HAZARDBOARD
	WITH ALL THE VALUES FOR EACH STAGE INITIALIZED TO 'N'
	------------------------------------------------------------------------------------------------*/
	public static String[][] initializeHazardboard(int count)
	{
		String[][] result = new String[count][3];
		for (int i = 0; i < count; i++) {
			for (int j = 0; j < 3; j++) {
				result[i][j] = "N";
			}
		}
		return result;
	}
	
	/*-----------------------------------------------------------------------------------------------
	PRINTSCOREBOARD PRINTS OUT THE SCOREBOARD
	------------------------------------------------------------------------------------------------*/
	public static void printScoreboard(int[][] result)
	{
		for (int i = 0; i < result.length; i++) {
			System.out.print(instruction[i].operation+"\t::\t");
			for (int j = 0; j < 5; j++) 
			{
			System.out.print(result[i][j]+"\t");
			}
			System.out.println();
		}
	}
	
	/*-----------------------------------------------------------------------------------------------
	PRINTSCOREBOARD PRINTS OUT THE SCOREBOARD
	------------------------------------------------------------------------------------------------*/
	public static void printHazardboard(String[][] result)
	{
		for (int i = 0; i < result.length; i++) {
			System.out.print(i+"\t::\t");
			for (int j = 0; j < 3; j++) {
				System.out.print(HazardBoard[i][j]+"\t");
			}
			System.out.println();
		}
		
	}
	
	/*-----------------------------------------------------------------------------------------------
	SCOREBOARDFILLED RETURNS A BOOLEAN VALUE OF WHETHER THE SCOREBOARD IS COMPLETE
	------------------------------------------------------------------------------------------------*/
	public static boolean ScoreboardFilled(int[][] sb)
	{
		for (int i = 0; i < sb.length; i++) {
			for (int j = 0; j < 5; j++) {
				if(sb[i][j]==-1)
				{
					return false;
				}
			}
		}
		return true;
	}
	
	/*-----------------------------------------------------------------------------------------------
	LOADINSTRUCTIONARRAY RETURNS AN INSTRUCTION ARRAY LOADED WITH THE INSTRUCTION.txt FILE
	------------------------------------------------------------------------------------------------*/
	public static Instruction[] loadInstructionArray() throws IOException
	{
		String instruction_file = instructionFile;
		String line = null;
		LinkedList<String> instructionList = new LinkedList<String>();
		FileReader fileReader = new FileReader(instruction_file);
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		int instructionCount=0;
		while((line = bufferedReader.readLine()) != null) {
		   instructionList.add(line);
		   instructionCount++;
		}   
		bufferedReader.close(); 
		
		Iterator<String> it = instructionList.listIterator();
		Instruction[] instruction = new Instruction[instructionCount];
		int i=0;
		while(it.hasNext())
		{
			instruction[i] = new Instruction(it.next());
			i++;
		}
		
		return instruction;
	}
	
	/*-----------------------------------------------------------------------------------------------
	LOADDATAMAP LOADS THE DATA INTO A DATAMAP HOLDING THE VALUE FOR ALL THE REGISTERS
	------------------------------------------------------------------------------------------------*/
	public static void loadWordDataMap() throws IOException
	{
		String data_file = dataFile;
		String line = null;
		FileReader fileReader = new FileReader(data_file);
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		int dataKey=256;
		for (int i = 300; i < 400; i++) {
			WordDataMap.put(i,(long)0);
		}
		while((line = bufferedReader.readLine()) != null) {
			String line1 = line.substring(0,8);
			String line2 = line.substring(8,16);
			String line3 = line.substring(16,24);
			String line4 = line.substring(24,32);
			Long value1 = Long.parseLong(line1,2);
			Long value2 = Long.parseLong(line2,2);
			Long value3 = Long.parseLong(line3,2);
			Long value4 = Long.parseLong(line4,2);
			WordDataMap.put(dataKey,value1);
			WordDataMap.put(dataKey+1,value2);
			WordDataMap.put(dataKey+2,value3);
			WordDataMap.put(dataKey+3,value4);
		   dataKey+=4;
		   
		}   
		//System.out.println(DataMap);
		bufferedReader.close();
	}
	
	
	/*-----------------------------------------------------------------------------------------------
	LOADDATAMAP LOADS THE DATA INTO A DATAMAP HOLDING THE VALUE FOR ALL THE REGISTERS
	------------------------------------------------------------------------------------------------*/
	public static void loadDoubleDataMap() throws IOException
	{
		String data_file = dataFile;
		String line = null;
		FileReader fileReader = new FileReader(data_file);
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		int dataKey=256;
		for (int i = 300; i < 400; i++) {
			DoubleDataMap.put(i,(long)0);
		}
		while((line = bufferedReader.readLine()) != null) {
		   Long value = Long.parseLong(line,2);
		   DoubleDataMap.put(dataKey,value);
		   dataKey+=4;
		   
		}   
		//System.out.println(DataMap);
		bufferedReader.close();
	}
	
	
	
	
	
	
	/*-----------------------------------------------------------------------------------------------
	LOADFUNCTIONALUNIT RETURNS A FUNCTIONAL ARRAY LOADED WITH THE FUNCTIONALUNIT.txt FILE
	------------------------------------------------------------------------------------------------*/
	public static FunctionalUnit[] loadFunctionalUnit(int unitCount) throws IOException
	{
		String functionalUnit_file = configFile;
		String line = null;
		LinkedList<String> functionalUnitList = new LinkedList<String>();
		FileReader fileReader = new FileReader(functionalUnit_file);
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		while((line = bufferedReader.readLine()) != null) {
		   functionalUnitList.add(line);
		}   
		functionalUnitList.removeLast();
		functionalUnitList.add("Integer:1,1");
		functionalUnitList.add("LoadStore:1,1");
		functionalUnitList.add("Branching:1,1");
		bufferedReader.close(); 
		
		Iterator<String> it = functionalUnitList.listIterator();
		FunctionalUnit[] functionalUnit = new FunctionalUnit[unitCount];
		int i=0;
		while(it.hasNext())
		{

			line = it.next().replaceAll(" ","");
			String[] tokens = line.split(":|\\,");
			int loop = Integer.parseInt(tokens[1]);
			int cycle = Integer.parseInt(tokens[2]);
			String unitName = tokens[0];
			for (int j = 0; j < loop; j++) {
				functionalUnit[i++] = new FunctionalUnit(unitName, cycle);
				funcCycleMap.put(unitName,cycle);
			}
		}
		return functionalUnit;
	}
	
	/*-----------------------------------------------------------------------------------------------
	GETINSTRUCTIONCOUNT RETURNS AN INTEGER INDICATING THE NUMBER OF INSTRUCTIONS IN THE INSTRUCTIONS.txt
	------------------------------------------------------------------------------------------------*/
	public static int getInstructionCount() throws IOException
	{
		String instruction_file = instructionFile;
		String line = null;
		LinkedList<String> instructionList = new LinkedList<String>();
		FileReader fileReader = new FileReader(instruction_file);
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		int instructionCount=0;
		while((line = bufferedReader.readLine()) != null) {
		   instructionList.add(line);
		   instructionCount++;
		}   
		bufferedReader.close(); 
		
		
		return instructionCount;
	}
	
	public static void instructionPrint()
	{
		System.out.println("Instruction Length : "+instruction.length);
		for (int i = 0; i < instruction.length; i++) {
			instruction[i].printI();
		}
	}
	
	public static int findBranchingCondition()
	{
		for (int i = 0; i < instruction.length; i++) {
			if(instruction[i].operation.equals("BNE") || instruction[i].operation.equals("BEQ") || instruction[i].operation.equals("J"))
			{
				return i;
			}
		
		}
		return instruction.length;
	}
	
	/*-----------------------------------------------------------------------------------------------
	GETFUNCTIONALUNITCOUNT RETURNS AN INTEGER INDICATING THE NUMBER OF functionalUnitS IN THE functionalUnitS.txt
	------------------------------------------------------------------------------------------------*/
	public static int getFunctionalUnitCount() throws IOException
	{
		String functionalUnit_file = configFile;
		String line = null;
		LinkedList<String> functionalUnitList = new LinkedList<String>();
		FileReader fileReader = new FileReader(functionalUnit_file);
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		while((line = bufferedReader.readLine()) != null) {
		   functionalUnitList.add(line);
		}   
		functionalUnitList.removeLast();
		functionalUnitList.add("Integer:1,1");
		functionalUnitList.add("LoadStore:1,2");
		functionalUnitList.add("Branching:1,1");
		bufferedReader.close(); 
		
		Iterator<String> it = functionalUnitList.listIterator();
		
		int count=0;
		while(it.hasNext())
		{

			line = it.next().replaceAll(" ","");
			String[] tokens = line.split(":|\\,");
			int loop = Integer.parseInt(tokens[1]);
			count += loop;
		}
		return count;
	}
	
	public static int fillScoreBoard(int instructionCount) throws IOException
	{
		int return_result = -1;
		for (int i1 = 0; i1 < 1000; i1++) 
			{
				
				boolean fetchBusy = false;	
				for (int j = 0; j < instructionCount; j++) {
					
					if(!fetchBusy && !fetchComplete[j])
						{
						if(j>0 && issueComplete[j-1])
						{
							{
								if(hitOrMiss.get(j))
								{
									fetchBusy = true;
									fetchComplete[j] = true;
									Scoreboard[j][0] = Scoreboard[j-1][1];
									lastCompletedCycle[j] = Scoreboard[j-1][1];	
									
								}
								else
								{
										
										int prefixCycle = Scoreboard[j-1][0]+1;
										while(busBusy.get(prefixCycle))
										{
											prefixCycle++;
										}
										int desired_cycle= Math.max(prefixCycle+12,Scoreboard[j-1][1]);
										if(desired_cycle<=instructionCycle)
										{
										fetchBusy = true;
										fetchComplete[j] = true;
										Scoreboard[j][0] = desired_cycle;
										lastCompletedCycle[j] = desired_cycle;
										}
										else
										{
											for (int i = instructionCycle; i <desired_cycle; i++) {
												busBusy.put(i,true);
											}
											busBusy.put(desired_cycle,false);
										}
									
								}
							}
						}
						else if(j==0){
							
							if(!hitOrMiss.get(j))
							{
								
								if(lastProcessedCycle+13==instructionCycle)
								{
								fetchBusy = true;
								fetchComplete[j] = true;
								Scoreboard[j][0] = instructionCycle;
								lastCompletedCycle[j] = instructionCycle;
								}
								else
								{
									busBusy.put(lastProcessedCycle,true);
								}
							}
							else
							{
								fetchBusy = true;
								fetchComplete[j] = true;
								Scoreboard[j][0] = instructionCycle;
								lastCompletedCycle[j] = instructionCycle;
							}
						}
					}
				}
				for (int j = 0; j < instructionCount; j++) {
					if(fetchComplete[j] && !issueComplete[j] && lastCompletedCycle[j]!=instructionCycle)
						{
						if(issueResolved(j)){
							issueComplete[j] = true;
							Scoreboard[j][1] = instructionCycle;
							lastCompletedCycle[j] = instructionCycle;
							}
						}
					}
				for (int j = 0; j < instructionCount; j++) {
					if(issueComplete[j] && !readComplete[j]  && lastCompletedCycle[j]!=instructionCycle)
						{
						if(readResolved(j,instructionCycle)){
							readComplete[j] = true;
							Scoreboard[j][2] = instructionCycle;
							lastCompletedCycle[j] = instructionCycle;
							}
						}
					}
				for (int j =0; j < instructionCount; j++) {
					if(readComplete[j] && !executeComplete[j]  && lastCompletedCycle[j]!=instructionCycle)
					{
						
						int tempCycle = instructionCycle;
						boolean b1 = (instruction[j].operation.equals("L.D") || instruction[j].operation.equals("S.D") || instruction[j].operation.equals("LW") || instruction[j].operation.equals("SW"));
						if(writeResolved(j))
						{
							int addCycle = 0;
							if(b1)
							{
							
							filldataCache(j,instructionCycle);
							addCycle = dataHitOrMiss.get(j);
							for (int i = tempCycle; i < (tempCycle+addCycle)-1; i++) {
								
								busBusy.put(i,true);
							}
							if(instruction[j].operation.equals("L.D") && addCycle==23)
							{

								addCycle++;
								busBusy.put(tempCycle+addCycle-2,true);
							}
							if(instruction[j].operation.equals("S.D") && addCycle==22)
							{
								addCycle++;
								busBusy.put(tempCycle+addCycle-2,true);
							}
							if(instruction[j].operation.equals("LW") && instruction[j].destination.equals("R3") && addCycle==12)
							{
							int initCycle = addCycle;
							addCycle += 12;
							for (int i = addCycle; i < (tempCycle+addCycle); i++) {
								
								busBusy.put(i,true);
							}
							}
							}
							
							executeComplete[j] = true;
							Scoreboard[j][3] = tempCycle + addCycle;
							lastCompletedCycle[j] = tempCycle + addCycle;
							executeHandler(j);
							
						}
					}
				}
				for (int j = 0; j < instructionCount; j++) {

					
					if(executeComplete[j] && !writeComplete[j]  && lastCompletedCycle[j]<instructionCycle)
						{
							writeComplete[j] = true;
							Scoreboard[j][4] = instructionCycle;
							lastCompletedCycle[j] = instructionCycle;
							complete[j] = true;
							resetFunctionalUnit(j,instructionCycle);
						}
					}
				
				instructionCycle++;
				
				
			}
		
		if(endCondition!=-1)
		{
			return endCondition;		
		}
		return return_result;
	}
	
	public static void updateInstructionFile(int start) throws IOException
	{
		
		String instruction_file = instructionFile;
		String line = null;
		LinkedList<String> instructionList = new LinkedList<String>();
		FileReader fileReader = new FileReader(instruction_file);
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		int instructionCount=0;
		while((line = bufferedReader.readLine()) != null) {
			if(instructionCount>=start)
			{
		   instructionList.add(line);
			}
		   instructionCount++;
		}   
		bufferedReader.close(); 
		
		FileWriter fw = new FileWriter(instructionFile);
		Iterator<String> iterator = instructionList.iterator();
		while(iterator.hasNext())
		{
			fw.append(iterator.next()+System.getProperty("line.separator"));
			
		}
	 
		fw.close();
	}
	
	public static void clearFinalHalts()
	{
		int[][] temp = Scoreboard.clone();
		
		int n = temp.length;
		
		temp[n-3][3] = 0;
		temp[n-3][4] = 0;
		temp[n-2][2] = 0;
		temp[n-2][3] = 0;
		temp[n-2][4] = 0;
		temp[n-1][1] = 0;
		temp[n-1][2] = 0;
		temp[n-1][3] = 0;
		temp[n-1][4] = 0;
		printScoreboard(temp);
	}
	
	public static void clearIntermediateHalts()
	{
		int[][] temp = Scoreboard.clone();
		
		int n = temp.length;
		
		temp[n-3][3] = 0;
		temp[n-3][4] = 0;
		temp[n-2][1] = 0;
		temp[n-2][2] = 0;
		temp[n-2][3] = 0;
		temp[n-2][4] = 0;
		temp[n-1][1] = 0;
		temp[n-1][2] = 0;
		temp[n-1][3] = 0;
		temp[n-1][4] = 0;
		temp[n-1][0] = 0;
		//printScoreboard(temp);
	}
	
	public static int[][] copyScoreBoard(int[][] array)
	{
		int n = array.length;
		int m = array[0].length;
		int[][] result = new int[n][m];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < m; j++) {
				result[i][j] = array[i][j];
			}
		}
		return result;
	}
	
	public static String[][] copyHazardTable(String[][] array)
	{
		int n = array.length;
		int m = array[0].length;
		String[][] result = new String[n][m];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < m; j++) {
				result[i][j] = array[i][j];
			}
		}
		return result;
	}
	
	public static void writeToFileFinal(int start) throws IOException
	{
		//printScoreboard(Scoreboard);
		int[][] temp = copyScoreBoard(Scoreboard);
		String[][] hazardTable = copyHazardTable(HazardBoard);
		int n = temp.length;
		temp[n-3][3] = 0;
		temp[n-3][4] = 0;
		temp[n-2][1] += 1;
		temp[n-2][2] = 0;
		temp[n-2][3] = 0;
		temp[n-2][4] = 0;
		temp[n-1][0] = temp[n-2][1];
		temp[n-1][1] = 0;
		temp[n-1][2] = 0;
		temp[n-1][3] = 0;
		temp[n-1][4] = 0;
		hazardTable[n-1][0] = "N";
		hazardTable[n-1][1] = "N";
		hazardTable[n-1][2] = "N";
		hazardTable[n-2][0] = "N";
		hazardTable[n-2][1] = "N";
		hazardTable[n-2][2] = "N";
		
		
		for (int i = 0; i < n; i++) 
		{
			for (int j = 0; j < 5; j++) {
				String str = Integer.toString(temp[i][j])+"\t";
				
				fw.append(str);	
			}
			for (int j = 0; j < 3; j++) {
				String str = (hazardTable[i][j])+"\t";
				
				fw.append(str);	
			}
			fw.append(System.getProperty("line.separator"));
		}
		
		String str1 = "Total number of access requests for instruction cache:\t"+Integer.toString(instructionAccessCounter);
		fw.append(str1);
		fw.append(System.getProperty("line.separator"));
		String str2 = "Number of instruction cache hits:\t"+Integer.toString(instructionCacheHitCounter);
		fw.append(str2);
		fw.append(System.getProperty("line.separator"));
		String str3 = "Total number of access requests for data cache:\t"+Integer.toString(dataCacheHitCounter);
		fw.append(str3);
		fw.append(System.getProperty("line.separator"));
		String str4 = "Number of data cache hits:\t"+Integer.toString(dataAccessCounter);
		fw.append(str4);
		fw.append(System.getProperty("line.separator"));
		
	}
	
	public static void writeToFileInter(int start) throws IOException
	{
		//printScoreboard(Scoreboard);
		int[][] temp = copyScoreBoard(Scoreboard);
		String[][] hazardTable = copyHazardTable(HazardBoard);
		int n = temp.length;
		temp[n-3][3] = 0;
		temp[n-3][4] = 0;
		temp[n-2][1] = 0;
		temp[n-2][2] = 0;
		temp[n-2][3] = 0;
		temp[n-2][4] = 0;
		temp[n-1][1] = 0;
		temp[n-1][2] = 0;
		temp[n-1][3] = 0;
		temp[n-1][4] = 0;
		temp[n-1][0] = 0;
		for (int i = 0; i < n-1; i++) 
		{
			for (int j = 0; j < 5; j++) {
				String str = Integer.toString(temp[i][j])+"\t";
				fw.append(str);	
			}
			for (int j = 0; j < 3; j++) {
				String str = (hazardTable[i][j])+"\t";
				
				fw.append(str);	
			}
			fw.append(System.getProperty("line.separator"));
		}
	}
	
	public static int getLastProcessedCycle()
	{
		int max = 0;
		for (int i = 0; i < 1000; i++) {
			if(busBusy.get(i))
			{
				max = i;
			}
		}	
		return max;
	}
	
	/*-----------------------------------------------------------------------------------------------
	MAIN FUNCTION
	------------------------------------------------------------------------------------------------*/
	public static void main(String[] args) throws IOException,NullPointerException {
		instructionFile = args[0];
		dataFile = args[1];
		configFile = args[2];
		resultFile = args[3];
		
		registers = initializeRegisterMap();
		int functionalUnitCount = getFunctionalUnitCount();
		funcUnit = loadFunctionalUnit(functionalUnitCount);
		instructionCache = initializeInstructionCache();
		initializeBusyBus();
		
		loadWordDataMap();
		loadDoubleDataMap();
		fw = new FileWriter(resultFile);
		instructionCycle = 1;
		instructionCount = getInstructionCount();
		instruction = loadInstructionArray();
		
		Scoreboard = initializeScoreboard(instructionCount);
		HazardBoard = initializeHazardboard(instructionCount);
		fetchComplete = initializeFalse(instructionCount);
		issueComplete = initializeFalse(instructionCount);
		readComplete = initializeFalse(instructionCount);
		executeComplete = initializeFalse(instructionCount);
		writeComplete = initializeFalse(instructionCount);
		complete = initializeFalse(instructionCount);
		lastCompletedCycle = initializeZero(instructionCount);
		fillInstructionCache();
		
		int start = fillScoreBoard(instructionCount);
		while(start!=-1) 
			{
			writeToFileInter(start);
			updateInstructionFile(start);
			startCounter = start;
			
			instructionCycle = Scoreboard[Scoreboard.length-2][0]+1;
			instructionCount = getInstructionCount();
			instruction = loadInstructionArray();
			Scoreboard = initializeScoreboard(instructionCount);
			HazardBoard = initializeHazardboard(instructionCount);
			fetchComplete = initializeFalse(instructionCount);
			issueComplete = initializeFalse(instructionCount);
			readComplete = initializeFalse(instructionCount);
			executeComplete = initializeFalse(instructionCount);
			writeComplete = initializeFalse(instructionCount);
			complete = initializeFalse(instructionCount);
			instructionCacheHits = initializeZero(100);
			lastCompletedCycle = initializeZero(instructionCount);
			endCondition = -1;
			hitOrMiss = reupdateHitOrMiss(startCounter);
			lastProcessedCycle = Math.max(instructionCycle,getLastProcessedCycle());
			start = fillScoreBoard(instructionCount);
			}
		writeToFileFinal(start);
		fw.close();
	}
}
