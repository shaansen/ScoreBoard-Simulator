import java.util.Arrays;
import java.util.Set;


public class FunctionalUnit {
	
	String name="";
	int cycles = 0;
	boolean busy=false;
	String operation="";
	String Fi = new String();
	String Fj = new String();
	String Fk = new String();
	boolean Rj = true;
	boolean Rk = true;
	
	public FunctionalUnit() {
		// TODO Auto-generated constructor stub
	}
	
	public FunctionalUnit(String name,int cycles)
	{
		this.name = name;
		this.cycles = cycles;
		//print();
	}
	
	public static String getCompatibleSource(String src)
	{
	if(src.contains("F") || src.contains("R"))
	{
		if(src.contains("("))
			{
			src = src.substring(src.indexOf("(")+1,src.length()-1);
			
			return src;	
			}
		return src;
	}
	return "";
	
	}
	
	public void print()
	{
		System.out.println(cycles+"\t"+operation+"\t"+busy+"\t"+ Fi+"\t"+Fj+"\t"+Fk+"\t"+Rj+"\t"+Rk);
		
	}
	
	public void useFunctionalUnit(FunctionalUnit funcUnit,Instruction instruction,Set<String> beingWritten)
	{
		//instruction.printI();
		funcUnit.setBusy(true);
		funcUnit.setOperation(instruction.operation);
		if(instruction.category!="Control" && instruction.instructionType=="OP3")
		{
			String src1 = getCompatibleSource(instruction.source1);
			String src2 = getCompatibleSource(instruction.source2);
			funcUnit.setFj(src1);
			funcUnit.setFk(src2);
			funcUnit.setFi(instruction.destination);
			if(beingWritten.contains(src1))
			{
			funcUnit.setRj(false);	
			}
			if(beingWritten.contains(src2))
			{
				funcUnit.setRk(false);	
			}
			
		}
		else if(instruction.category=="Control" && instruction.instructionType=="OP3")
		{
			String src1 = getCompatibleSource(instruction.source1);
			String src2 = getCompatibleSource(instruction.source2);
			funcUnit.setFj(src1);
			funcUnit.setFk(src2);
			
		}
		else if(instruction.instructionType=="OP2")
		{
			String src1 = getCompatibleSource(instruction.source1);
			funcUnit.setFj(src1);
			funcUnit.setFi(instruction.destination);
			if(instruction.operation.equals("L.D") || instruction.operation.equals("S.D"))
			{
				funcUnit.setCycles(2);
			}
			if(instruction.operation.equals("SW") || instruction.operation.equals("SUI")|| instruction.operation.equals("SI")|| instruction.operation.equals("S.D"))
			{
				funcUnit.setFi("");
				funcUnit.setFj("");
			}
		}
		
		
		//funcUnit.print();
		
		
	}
	
	public void clearFunctionalUnit(FunctionalUnit funcUnit,Instruction instruction)
	{
		funcUnit.setBusy(false);
	}
	
	public int getCycles() {
		return cycles;
	}
	
		public String getFi() {
		return Fi;
	}
	
	public String getFj() {
		return Fj;
	}
	
	public String getFk() {
		return Fk;
	}
	
	public String getName() {
		return name;
	}
	
	public String getOperation() {
		return operation;
	}
	
	public void setBusy(boolean busy) {
		this.busy = busy;
	}
	
	public void setFi(String fi) {
		Fi = fi;
	}
	public void setFj(String fj) {
		Fj = fj;
	}
	
	public void setFk(String fk) {
		Fk = fk;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setOperation(String operation) {
		this.operation = operation;
	}
	
	public void setRj(boolean rj) {
		Rj = rj;
	}
	
	public void setRk(boolean rk) {
		Rk = rk;
	}
	
	public void setCycles(int cycles) {
		this.cycles = cycles;
	}

}
