package src;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;
import java.util.Map.Entry;

public class RG_TestForPrediction {
	public static String[] prov_vector={"beijing","tianjin","hebei","henan","shan1xi","shandong","neimenggu","liaoning","jilin","heilongjiang","shan3xi","hubei","hunan","anhui","jiangsu","gansu","ningxia","sichuan","chongqing","jiangxi","zhejiang","shanghai","xinjiang","xizang","yunnan","guangxi","guangdong","qinghai","fujian","guizhou","hainan","xianggang","aomen","taiwan"};
	
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
	
	
	public static ArrayList<String> readLines(String fileName,int threshold) throws Exception {
		File file=new File(fileName);
	      if (!file.exists()) {
	          return new ArrayList<String>();
	      }
	      BufferedReader reader = new BufferedReader(new FileReader(file));
	      ArrayList<String> results = new ArrayList<String>();
	      String line = reader.readLine();
	      int countDay=0;
	      int countDayAll=0;
	      while (line != null) {
	    	  countDayAll++;
	    	  if(Integer.parseInt(line.split(" ")[1])>threshold){
	    		  results.add(line.split(" ")[0]);
	    		  countDay++;
	    	  }
	          //System.out.println(line);
	          line = reader.readLine();
	      }
	      //System.out.println(countDay+"-----"+countDayAll);
	      return results;
	  }
	public static String arrayToString(int[] int_vector){
		String int_String="[";
		for(int e:int_vector)
			int_String+=e+" ";
		int_String+="]";
		return int_String;
	}
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		int[] indicatorVector= new int[prov_vector.length];
		int dayCount=0;
		ArrayList<String> dayList=readLines("data/days_count.txt",0);
		BufferedWriter outputWriter = null;
		outputWriter = new BufferedWriter(new FileWriter("data/result.txt"));
		  
		//System.out.println(">>>"+dayList.get(0));
		//String date,int G_num,int k,double alpha_v,double lambda_v
		for(String date:dayList){
			RandomGreedy rgTest=null;
			System.out.println("["+(dayList.size()-dayCount++)+"]" + "###################"+date+"######################");
			rgTest=new RandomGreedy(date,0,0.15,0.015);
			indicatorVector=genIndicatorVector(rgTest.province);
			outputWriter.write(date+" "+arrayToString(indicatorVector)+"\n");
			System.out.println(date+" "+arrayToString(indicatorVector));
		}
		outputWriter.flush();  
		outputWriter.close();
		System.out.println("******************* Done **********************");
	}

}
