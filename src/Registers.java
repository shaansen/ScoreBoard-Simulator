
public class Registers {
	
	String name = "";
	Long value = (long) 0;
	
	public Registers()
	{
		
	}
	
	public Registers(String name,Long value) {
		this.name = name;
		this.value = value;
		//System.out.println(this.name+" : "+this.value);
	}
	
	public Registers(String name) {
		this.name = name;
		this.value = (long)0;
		//System.out.println(this.name+" : "+this.value);
	}
	
	public String getName() {
		return name;
	}
	
	public Long getValue() {
		return value;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setValue(Long value) {
		this.value = value;
	}
	
	public void setValue(String value) {
		if(value.contains("("))
		{
			
		}
		else
		{
		this.value = Long.parseLong(value);
		}
	}

}
