import java.util.*;

public class DataCacheSet {

	static Queue<Integer> set0 = new LinkedList<Integer>();
	static Queue<Integer> set1 = new LinkedList<Integer>();
	int CycleLastAccessed = 0;
	int startAddress = 0;
	int lastAddress = 0;
	
	DataCacheSet(int address,int cycle)
	{
		//system.out.println("Address "+address);
		//system.out.println("Cycle : "+cycle);
		this.CycleLastAccessed = cycle;
		int setAddress = calculateSetAddress(address);
		this.startAddress = calculateStartAddress(address);
		this.lastAddress = startAddress + 15;
		//system.out.println("Set Address : "+setAddress);
		//system.out.println("Start Address : "+setAddress);
		//system.out.println("Last Address : "+setAddress);
		if(setAddress==0 && setDoesNotContain(address,setAddress))
		{
			if(set0.size()>=2)
			{
				System.out.println("setsize greater than 2");
				reAdjustQueue("set0",startAddress);
			}
			else
			{
				addToQueue("set0",startAddress);
			}
		}
		else if(setAddress==1)
		{
			if(set1.size()>=2)
			{
				reAdjustQueue("set0",startAddress);
			}
			else
			{
				addToQueue("set1",startAddress);
			}
		}
		printDataCache();
	}
	
	public int calculateSetAddress(int address)
	{
		//system.out.println("SetAddress : "+(address/4)%2);
		return (address/4)%2;
	}
	
	public int calculateStartAddress(int address)
	{
		//system.out.println("StartAddress : "+(address-(address%16)));
		return address-(address%16);
	}
	
	public void reAdjustQueue(String str,int startAddress)
	{
		//system.out.println("Readjusting Queue");
		if(str=="set0")
		{
			set0.remove();
			set0.add(startAddress);
			
		}
		if(str=="set1")
		{
			set1.remove();
			set1.add(startAddress);
			
			
		}
	}
	
	public void addToQueue(String str,int startAddress)
	{
		//system.out.println("Adding to Queue");
		if(str=="set0")
		{
			//system.out.println("Adding to Set 0");
			set0.add(startAddress);
			
		}
		else if(str=="set1")
		{
			//system.out.println("Adding to Set 1");
			set1.add(startAddress);
			
		}
	}
	
	public void printDataCache()
	{
		System.out.println("Set 0 : "+set0);
		System.out.println("Set 1 : "+set1);
	}
	
	public boolean setDoesNotContain(int address,int setAddress)
	{
		if(setAddress==0)
		{
			if(set0.size()==0)
			{
				return true;
			}
			else
			{
				Iterator<Integer> i = set0.iterator();
				while(i.hasNext())
				{
					
					if(i.next()<=setAddress && setAddress<i.next()+15)
					{
						return true;
					}
				}
				return false;
			}
		}
		else if(setAddress==1)
		{
			if(set1.size()==0)
			{
				return true;
			}
			else
			{
				Iterator<Integer> i = set1.iterator();
				while(i.hasNext())
				{
					
					if(i.next()<=setAddress && setAddress<i.next()+15)
					{
						return true;
					}
				}
				return false;
			}
		}
		return false;
	}
}
