import java.util.*;


public class Instruction {
	public String label="";
	public String operation="";
	public String destination="";
	public String source1="";
	public String source2="";
	public String instructionType="";
	public String functionalUnit="";
	public String shortPrint="";
	public String category="";
	public static Set<String> Operand_3 = new HashSet<String>();
	public static Set<String> Operand_2 = new HashSet<String>();
	public static Set<String> Operand_0 = new HashSet<String>();
	public static Set<String> Cat_A = new HashSet<String>();
	public static Set<String> Cat_D = new HashSet<String>();
	public static Set<String> Cat_C = new HashSet<String>();
	public static Set<String> Cat_S = new HashSet<String>();
	
	public Instruction(String str) {
		this.shortPrint = str;
		if(str.contains(":"))
		{
		this.label = str.substring(0,str.indexOf(":"));	
		str = str.substring(str.indexOf(":")+2);
		}
		str = str.replaceAll(",","");
		String[] elements = str.split(" ");
		this.operation = elements[0];
		this.instructionType = determineInstructionType(elements[0]);
		this.functionalUnit = determineFunctionalUnitFromInstruction(this);
		this.category = determineCategory(this);
		if(this.instructionType=="OP3" && category!="Control")
		{
			this.destination = elements[1];
			this.source1 = elements[2];
			this.source2 = elements[3];
		}
		else if(this.instructionType=="OP3" && category=="Control")
		{
			this.destination = elements[3];
			this.source1 = elements[1];
			this.source2 = elements[2];
		}
		else if(this.instructionType=="OP2")
		{
			this.destination = elements[1];
			this.source1 = elements[2];
		}
//		print();
//		System.out.println("======================================================");
	}	
	
	public void print()
	{
		
		if(label!="")
		{
			System.out.println("Label\t: "+label);
		}
		System.out.println("FunctionalUnitUsed\t: "+functionalUnit);
		System.out.println("InstructionType\t\t: "+instructionType);
		System.out.println("InstructionCategory\t: "+category);
		System.out.println("Operation\t\t: "+operation);
		if(instructionType=="OP3")
		{
			System.out.println("Destination\t\t: "+destination);
			System.out.println("Source 1\t\t: "+source1);
			System.out.println("Source 2\t\t: "+source2);
			
		}
		else if(instructionType=="OP2")
		{
			System.out.println("Destination\t\t: "+destination);
			System.out.println("Source 1\t\t: "+source1);
		}
		else
		{
			
		}
		System.out.println("==============================================");
	}
	
	public void printI()
	{
		System.out.println(shortPrint);
	}
	
	
	public static void initializeInstructionTypeSets()
	{
		// Operand 3
		Operand_3.add("DADD");
		Operand_3.add("DADDI");
		Operand_3.add("DSUB");
		Operand_3.add("DSUBI");
		Operand_3.add("AND");
		Operand_3.add("ANDI");
		Operand_3.add("OR");
		Operand_3.add("ORI");
		Operand_3.add("ADD.D");
		Operand_3.add("MUL.D");
		Operand_3.add("DIV.D");
		Operand_3.add("SUB.D");
		Operand_3.add("BEQ");
		Operand_3.add("BNE");
		Operand_3.add("J");
		
		// Operand 2
		Operand_2.add("LI");
		Operand_2.add("LUI");
		Operand_2.add("LW");
		Operand_2.add("L.D");
		Operand_2.add("SI");
		Operand_2.add("SUI");
		Operand_2.add("SW");
		Operand_2.add("S.D");
		
		// Operand 0
		Operand_0.add("HLT");
		
	}
	
	public static void initializeCategorySets()
	{
		// Arithmetic Instructions
		Cat_A.add("DADD");
		Cat_A.add("DADDI");
		Cat_A.add("DSUB");
		Cat_A.add("DSUBI");
		Cat_A.add("AND");
		Cat_A.add("ANDI");
		Cat_A.add("OR");
		Cat_A.add("ORI");
		Cat_A.add("ADD.D");
		Cat_A.add("MUL.D");
		Cat_A.add("DIV.D");
		Cat_A.add("SUB.D");
		Cat_A.add("LI");
		Cat_A.add("LUI");
		Cat_A.add("SI");
		Cat_A.add("SUI");
		
		// Data Instructions
		Cat_D.add("LW");
		Cat_D.add("L.D");
		Cat_D.add("SW");
		Cat_D.add("S.D");
		
		// Control
		Cat_C.add("BEQ");
		Cat_C.add("BNE");
		Cat_C.add("J");
		
		// Special Purpose
		Cat_S.add("HLT");
		
	}
	
	public static String determineInstructionType(String operation)
	{
		initializeInstructionTypeSets();
		if(Operand_3.contains(operation))
		{return "OP3";}
		else if(Operand_2.contains(operation))
		{return "OP2";}
		else
		{return "OP0";}
	}
	
	public static String determineFunctionalUnitFromInstruction(Instruction i)
	{
		String element = i.operation;
		String result = "";

		if(element.contains("MUL.D"))
		{
			result = "FPMultiplier";
		}
		else if(element.contains("L.D") || element.contains("LW") || element.contains("S.D") || element.contains("SW"))
		{
			result = "LoadStore";
		}
		else if(element.contains("ADD.D") || element.contains("SUB.D") || element.contains("L.D"))
		{
			result = "FPadder";
		} 
		else if(element.contains("DIV.D"))
		{
			result = "FPdivider";
		}
		else if(element.contains("BNE")||element.contains("BEQ")||element.contains("J"))
		{
			result = "Branching";
		}
		else
		{
			result = "Integer";
		}
		return result;
	}
	
	public static String determineCategory(Instruction i)
	{
		initializeCategorySets();
		String op = i.operation;
		
		if(Cat_D.contains(op))
		{return "Data";}
		else if(Cat_C.contains(op))
		{return "Control";}
		else if(Cat_A.contains(op))
		{return "Arithemetic";}
		else
		{return "Special";}
		
	}
}
