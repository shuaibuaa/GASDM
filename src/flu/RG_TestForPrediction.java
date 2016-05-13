package flu;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Time;
import java.util.*;
import java.util.Map.Entry;

public class RG_TestForPrediction {
	public static String[] prov_vector={"1","2","3","4","5","6","7","8","9","10"};
	public static Connection conn = null;
	
	public static int[] genIndicatorVector(Map<String, Integer> S_star){
		
		int[] indicatorVector= new int[prov_vector.length];
		Set<Entry<String, Integer>> entries = S_star.entrySet();
		Iterator<Entry<String, Integer>> iter = entries.iterator();
		Map<String, Integer> provIndicatorMap=new HashMap<String, Integer>();
		
		for(int i=0;i<prov_vector.length;i++){
			provIndicatorMap.put(prov_vector[i], (Integer)i);
		}
		int index=0;
		while(iter.hasNext()) {
			Entry entry = iter.next();
			if(provIndicatorMap.containsKey(entry.getKey()))
				indicatorVector[provIndicatorMap.get(entry.getKey())]=1;
		}
		System.out.println("S_star Size: "+S_star.size());
		return indicatorVector;
	}
	
	public static String arrayToString(int[] int_vector){
		String int_String="[ ";
		for(int e:int_vector)
			int_String+=e+" ";
		int_String+="]";
		return int_String;
	}
	
	private static void initDB(){
		String url = "jdbc:mysql://localhost:3306/gasdm?user=root&password=&useUnicode=true&characterEncoding=UTF8";
		try {  
			Class.forName("com.mysql.jdbc.Driver");  
			conn = DriverManager.getConnection(url);
		} catch (Exception e) {  
			e.printStackTrace();  
		}
	}
	
	public static String formatFloatNumber(Double value) {
        if(value != null){
            if(value.doubleValue() != 0.00){
                java.text.DecimalFormat df = new java.text.DecimalFormat("###.00");
                return df.format(value.doubleValue());
            }else{
                return "0.00";
            }
        }
        return "";
    }
	
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		int[] indicatorVector= new int[prov_vector.length];
		int weekCount=0;
		ArrayList<Integer> weekList = new ArrayList<Integer>();
		BufferedWriter outputWriter = null;
		outputWriter = new BufferedWriter(new FileWriter("data//flu//GASDM_result_flu.txt"));
		  
		for(int i=1;i<=70;i++)
			weekList.add(i);
		//System.out.println(">>>"+dayList.get(0));
		//String date,int G_num,int k,double alpha_v,double lambda_v
		
		Calendar c1 = Calendar.getInstance();
		Date start = c1.getTime();
		
		initDB();
		
		for(int week:weekList){
			RandomGreedy rgTest=null;
			System.out.println("["+(weekList.size()-weekCount++)+"]" + "###################"+week+"######################");
			rgTest=new RandomGreedy(week,0,0.12,5);
			int times=1;
			while(rgTest.region.size()<4 && times>0){
				rgTest=new RandomGreedy(week,0,0.12,5);
				times--;
			}
			indicatorVector=genIndicatorVector(rgTest.region);
			outputWriter.write(week+" "+arrayToString(indicatorVector)+"\n");
			System.out.println(week+" "+arrayToString(indicatorVector));
		}
		outputWriter.flush();  
		outputWriter.close();
		System.out.println("******************* Done **********************");
		
		Calendar c2 = Calendar.getInstance();
		Date end = c2.getTime();
		end.setTime(end.getTime()-start.getTime());
		System.out.println("Running Time: "+formatFloatNumber((double)end.getTime()/(3600*1000))+" hours.");
	}

}
