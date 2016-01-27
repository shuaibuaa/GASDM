package src;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;

public class RandomGreedy {
	
	public static String day;
	public static int K = 10;
	public static double alpha = 0.15;
	public static double lambda = 0.015;
	
	public static Connection conn = null;
	public static double score=0.0D;
	public static double lastscore=0.0D;
	public static double[][] pdv=null;
	public static double FY=0.0D;
	public static double GYY=0.0D;
	public static double FV=0.0D;
	public static ArrayList<String> V1   = null;
	public static ArrayList<String> V2   = null;
	public static ArrayList<String> S1r  = null;
	public static ArrayList<String> S2r  = null;
	public static ArrayList<String> S1rt = null;
	public static ArrayList<String> S2rt = null;
	public static ArrayList<String> random = null;
	public static HashMap<String, Integer> u2i =null;
	public static HashMap<String, Integer> k2i = null;
	public static HashMap<String, String> userLocMap = null;
	public static HashMap<String, String> locMap = null;
	public static HashMap<String, Integer> locNumMap = null;
	public static HashMap<String, Integer> locConnNumMap = null;
	
	public static ArrayList<String> true_label = new ArrayList<String>();
	public static Map<String, Integer> province = new HashMap<String, Integer>();
	public static ArrayList<String> S_star = new ArrayList<String>();
	public static ArrayList<Double> F1   = new ArrayList<Double>();
	public static ArrayList<Double> F2   = new ArrayList<Double>();
	public static ArrayList<Double> F2_Up= new ArrayList<Double>(); 
	public static ArrayList<Double> Penalty= new ArrayList<Double>();
	public static ArrayList<Double> F   = new ArrayList<Double>();
	public static ArrayList<Double> F_Low= new ArrayList<Double>();
	
	public RandomGreedy(String date,int k,double alpha_v,double lambda_v){
		day = date;
		K=k;
		alpha = alpha_v;
		lambda = lambda_v;	
		V1   = new ArrayList<String>();
		V2   = new ArrayList<String>();
		S1r  = new ArrayList<String>();
		S2r  = new ArrayList<String>();
		S1rt = new ArrayList<String>();
		S2rt = new ArrayList<String>();
		random = new ArrayList<String>();
		u2i = new HashMap<String, Integer>();
		k2i = new HashMap<String, Integer>();
		userLocMap = new HashMap<String, String>();
		locMap = new HashMap<String, String>();
		locNumMap = new HashMap<String, Integer>();
		locConnNumMap = new HashMap<String, Integer>();
		
		true_label = new ArrayList<String>();
		province = new HashMap<String, Integer>();
		S_star = new ArrayList<String>();
		F1   = new ArrayList<Double>();
		F2   = new ArrayList<Double>();
		F2_Up= new ArrayList<Double>(); 
		Penalty= new ArrayList<Double>();
		F   = new ArrayList<Double>();
		F_Low= new ArrayList<Double>();
		
		initDB();
		initLocMap();
		randomGreedy();
	}
//	public static void randomGreedy(Date date) {
	public static void randomGreedy() {
//		init(date);
		init();
		greedy();
	}
	
//	private static void init(Date date) {
	private static void init() {
//		initDate(date);//init day
		initV();//init V1,V2,userlocMap,pdv
		initLocNumMap();//init locNumMap
	}
	
	private static void initDate(Date date) {
		int y, m, d;
		String yy, mm, dd;
		y = date.getYear()+1900;
		m = date.getMonth()+1;
		d = date.getDate();
		yy = y + "";
		if(m<10)
			mm = "0" + m;
		else
			mm = "" + m;
		if(d<10)
			dd = "0" + d;
		else
			dd = "" + d;
		day = yy+"-"+mm+"-"+dd; // to obtain this "day", be useful when visiting databases
	}
	
	/*
	 * need to initial your Database's "user" and "password"
	 */
	private static void initDB(){
		String url = "jdbc:mysql://localhost:3306/gasdm?user=root&password=123456&useUnicode=true&characterEncoding=UTF8";
		try {  
			Class.forName("com.mysql.jdbc.Driver");  
			conn = DriverManager.getConnection(url);
		} catch (Exception e) {  
			e.printStackTrace();  
		}
	}
	
	// initial the ground set V, including all users and keywords
	private static void initV() {
		Statement stmt;
		ResultSet urs, krs, prs;
		String user, keyword, loc;
		double pvalue;
		int index = 0;
		try {
			stmt = conn.createStatement();
			//init V1, userLocMap, u2i
			String sql = "select distinct u_id, location from pdven where day = '" + day + "';";	
			urs = stmt.executeQuery(sql);
			V1.clear();
			u2i.clear();
			userLocMap.clear();
			while(urs.next()){
				user = urs.getString("u_id");
				loc = urs.getString("location");
				V1.add(user);
				u2i.put(user, index++); // revert user to index in the array[][] "pdv"
				userLocMap.put(user, loc);
			}
			// init V2, k2i
			sql = "select distinct keyword from pdven where day = '" + day + "';";
			index = 0;
			krs = stmt.executeQuery(sql);
			V2.clear();k2i.clear();
			while(krs.next()){
				keyword = krs.getString("keyword");
				V2.add(keyword);
				k2i.put(keyword, index++); // revert keyword to index in the array[][] "pdv"
			}
			// init the array[][] "pdv", pdv[keyword][user]=the pvalue of the pair{"keyword", "user"} of this day
			pdv = new double[V2.size()][V1.size()];
			for(int i=0; i<V2.size(); i++)
				for(int j=0; j<V1.size(); j++)
					pdv[i][j]=1;
			sql = "select u_id, keyword, pvalue from pdven where day = '"+ day +"';"; 
			prs = stmt.executeQuery(sql);
			while(prs.next()){
				user = prs.getString("u_id");
				keyword = prs.getString("keyword");
				pvalue = prs.getDouble("pvalue");
				pdv[k2i.get(keyword)][u2i.get(user)] = pvalue;
			}
			
			sql = "select province from true_value where date = '"+ day +"';"; 
			prs = stmt.executeQuery(sql);
			String prov;
			while(prs.next()){
				prov = prs.getString("province");
				//System.out.println(">>> "+prov);
				true_label.add(prov);
				
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	// initial locMap<String, String>, <province, its neighbor provinces>
	private static void initLocMap() {//shan1xi and shan3xi are different province
		locMap.put("beijing","hebei|tianjin");
		locMap.put("tianjin","hebei|beijing");
		locMap.put("hebei","beijing|tianjin|shan1xi|henan|shandong|liaoning|neimenggu");
		locMap.put("henan","hebei|shandong|shan1xi|shan3xi|hubei|anhui|jiangsu");
		locMap.put("shan1xi","henan|hebei|shan3xi|neimenggu");
		locMap.put("shandong","hebei|henan|anhui|jiangsu");
		locMap.put("neimenggu","gansu|ningxia|shan3xi|shan1xi|hebei|liaoning|jilin|heilongjiang");
		locMap.put("liaoning","hebei|neimenggu|jilin");
		locMap.put("jilin","neimenggu|liaoning|heilongjiang");
		locMap.put("heilongjiang","jilin|neimenggu");
		locMap.put("shan3xi","gansu|sichuan|chongqing|hubei|henan|shan1xi|neimenggu");
		locMap.put("hubei","shan3xi|henan|anhui|chongqing|hunan|jiangxi");
		locMap.put("hunan","guizhou|guangdong|hubei|chongqing|jiangxi|guangxi");
		locMap.put("anhui","jiangsu|henan|hubei|jiangxi|zhejiang");
		locMap.put("jiangsu","shandong|anhui|zhejiang|shanghai");
		locMap.put("gansu","ningxia|shan3xi|neimenggu|xinjiang|qinghai|sichuan");
		locMap.put("ningxia","gansu|neimenggu|shan3xi");
		locMap.put("sichuan","guizhou|yunnan|chongqing|shan3xi|qinghai|xizang|gansu");
		locMap.put("chongqing","hubei|shan3xi|sichuan|guizhou|hunan");
		locMap.put("jiangxi","hubei|hunan|guangdong|fujian|zhejiang|anhui");
		locMap.put("zhejiang","jiangxi|fujian|anhui|jiangsu|shanghai");
		locMap.put("shanghai","jiangsu|zhejiang");
		locMap.put("xinjiang","xizang|qinghai|gansu");
		locMap.put("xizang","qinghai|sichuan|xinjiang|yunnan");
		locMap.put("yunnan","xizang|guangxi|sichuan|guizhou");
		locMap.put("guangxi","guangdong|hunan|guizhou|yunnan");
		locMap.put("guangdong","guangxi|hunan|jiangxi|fujian|hainan");
		locMap.put("qinghai","gansu|sichuan|xinjiang|xizang");
		locMap.put("fujian","jiangxi|guangdong|taiwan|zhejiang");
		locMap.put("guizhou","yunnan|sichuan|chongqing|hunan|guangxi");
		locMap.put("hainan","guangdong");
		locMap.put("xianggang","guangdong|aomen");
		locMap.put("aomen","guangdong|xianggang");
		locMap.put("taiwan","fujian");
	}
	
	//initial locNumMap<String, Integer>, <province, the user number of this province)>
	private static void initLocNumMap() {
		Iterator iter = userLocMap.entrySet().iterator();
		locNumMap.clear();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			Object loc = entry.getValue();
			if (!locNumMap.containsKey(loc)) {
				locNumMap.put(loc.toString(), 1);
			}
			else {
				locNumMap.put(loc.toString(), locNumMap.get(loc.toString())+1);
			}
		}
	}
	
	//greedy algorithm
	private static void greedy() {
		int r = 1;
		int index=0;
		FV = f2(V1,V2); // will be use in function G, to reduce the complexity
		random.addAll(V1);
		random.addAll(V2);
		
		do {
			System.out.print("r = "+r+" ");
			r = r + 1;
			S1rt.clear();
			S1rt.addAll(S1r);
			S2rt.clear();
			S2rt.addAll(S2r);
			randomPermutation();
			
			FY = f2(S1rt, S2rt);// will be use in function M and G
			for (int i=0; i<random.size(); i++) {
				ArrayList<String> t = new ArrayList<String>();
				String e = random.get(i);
				// there is a little different from the theory to reduce the complexity, cause (add keyword to user set)=0, (add user to keyword set)=0
				if (isUser(e)) {// there are only numbers in e, indicate that it's a user
					double x = f(S1r, S2r, S1rt, S2rt);
					t.clear();
					t.addAll(S1r);
					t.add(e);
					double y = f(t, S2r, S1rt, S2rt);
					x = y - x;
					if (x > 0){
						S1r.add(e);
						score = y;
						//System.out.println(index++ + ": " +e);
						F1.add(f1(S1r, S2r));
						F2.add(f2(S1r, S2r));                //F2 score
						F2_Up.add(M(S1r, S2r, S1rt, S2rt));  //F2_upper bound
						Penalty.add(penalty(S1r));
						F.add((f1(S1r, S2r)- f2(S1r, S2r) + penalty(S1r)));                //F score
						F_Low.add((f1(S1r, S2r)- M(S1r, S2r, S1rt, S2rt) + penalty(S1r))); //F_lower bound
					}
				}
				else {// e is a keyword
					double x = f(S1r, S2r, S1rt, S2rt);
					t.clear();t.addAll(S2r);
					t.add(e);
					double y = f(S1r, t, S1rt, S2rt);
					x = y - x;
					if (x > 0){
						S2r.add(e);
						score = y;
						//System.out.println(index++ + ": " +e);
						F1.add(f1(S1r, S2r));
						F2.add(f2(S1r, S2r));               //F2 score
						F2_Up.add(M(S1r, S2r, S1rt, S2rt)); //F2_upper bound
						Penalty.add(penalty(S1r));
						F.add((f1(S1r, S2r)- f2(S1r, S2r) + penalty(S1r)));                //F score
						F_Low.add((f1(S1r, S2r)- M(S1r, S2r, S1rt, S2rt) + penalty(S1r))); //F_lower bound
					}
				}
			}
			//System.out.println();
			
		} while (!(compare(S1r, S1rt) && compare(S2r, S2rt)));
		System.out.println("Complete!!");
		// when converge, print the result user set to console
		int i;
		for (i=0; i<S1r.size(); i++) {
			String prov=userLocMap.get(S1r.get(i));
			if(province.containsKey(prov)){
				Integer temp=province.get(prov);
				province.put(prov,temp+1);
			}else{
				province.put(prov,1);
			}
		}
		province=sortByValue(province);
	}
	
	// shuffle the unchosen element to take the next iteration
	private static void randomPermutation() {
		random.clear();
		random.addAll(V1);random.removeAll(S1rt);
		random.addAll(V2);random.removeAll(S2rt);
		Collections.shuffle(random);
		System.out.println("random: " + random.size());
	}
	
	// compare two ArrayList are equal or not
	private static <T extends Comparable<T>> boolean compare(ArrayList<T> a, ArrayList<T> b) {
	    if(a.size() != b.size())
	        return false;
	    Collections.sort(a);
	    Collections.sort(b);
	    for(int i=0;i<a.size();i++){
	        if(!a.get(i).equals(b.get(i)))
	            return false;
	    }
	    return true;
	}
	
	// F = F1 - MF2 + penalty
	private static double f(ArrayList<String> S1, ArrayList<String> S2, ArrayList<String> S1rt, ArrayList<String> S2rt) {
		double result = 0;
		result += f1(S1, S2);
		result -= M(S1, S2, S1rt, S2rt);
		result += penalty(S1);
		return result;
	}
	
	// F1 = -(|S1|+|S2|)log(|S1|+|S2|)
	private static double f1(ArrayList<String> S1, ArrayList<String> S2) {
		if (S1.size() + S2.size() != 0)
			return -(S1.size() + S2.size()) * Math.log(S1.size() + S2.size());
		else 
			return 0;
	}
	
	// MF2 = GY(S)+F2(Y)-GY(Y)
	private static double M(ArrayList<String> S1, ArrayList<String> S2, ArrayList<String> S1rt, ArrayList<String> S2rt) {
		double x = 0;
		x = FY + G(S1, S2, S1rt, S2rt);
		return x;
	}
	
	private static double G(ArrayList<String> S1, ArrayList<String> S2, ArrayList<String> S1rt, ArrayList<String> S2rt) {
		double x = 0;
		ArrayList<String> t = new ArrayList<String>();
		ArrayList<String> s = new ArrayList<String>();
		s.addAll(S1rt);s.addAll(S2rt);s.removeAll(S1);s.removeAll(S2);
		for (int i=0; i<s.size(); i++) {
			String e = s.get(i);
			if (isUser(e)) {
				t.clear();t.addAll(V1);t.remove(e);
				x += (f2(t, V2) - FV);
			}
			else {
				t.clear();t.addAll(V2);t.remove(e);
				x += (f2(V1, t) - FV);
			}
		}
		s.clear();s.addAll(S1);s.addAll(S2);s.removeAll(S1rt);s.removeAll(S2rt);
		for (int i=0; i<s.size(); i++) {
			String e = s.get(i);
			if (isUser(e)) {
				t.clear();t.addAll(S1rt);t.add(e);
				x += (f2(t, S2rt) - FY);
			}
			else {
				t.clear();t.addAll(S2rt);t.add(e);
				x += (f2(S1rt, t) - FY);
			}
		}
		return x;
	}	
	
	// F2 = g(S1,S2)logα - g(S1,S2)log(g(S1,S2)) + (|S1|+|S2|-g(S1,S2))log⁡(1-α) - (|S1|+|S2|-g(S1,S2))log(|S1|+|S2|-g(S1,S2))
	private static double f2(ArrayList<String> S1, ArrayList<String> S2) {
		double x = 0;
		int g = g(S1, S2);
		x = (double)g * Math.log(alpha);
		if (g != 0)
			x -= (double)g * Math.log((double)g);
		g = S1.size() + S2.size() - g;
		x += (double)g * Math.log(1.0-alpha);
		if (g != 0)
			x -= (double)g * Math.log((double)g);
		return x;
	}
	
	// g = sum(I(p(S2)(v)<=α)) + sum(I(p(S1)(d)<=α)), here v for user, d for keyword
	private static int g(ArrayList<String> S1, ArrayList<String> S2) {
		int x = 0;
		for (int i=0; i<S1.size(); i++) {
			String e = S1.get(i);
			if (p1(S2, e) <= alpha)
				x++;
		}
		for (int i=0; i<S2.size(); i++) {
			String e = S2.get(i);
			if (p2(S1, e) <= alpha)
				x++;
		}
		return x;
	}
	
	private static double p1(ArrayList<String> S2, String e) {
		double x = 0;
		int ui, ki;
		
		if (S2.size() > 0) {
			
			ui = u2i.get(e);
			for (int i=0; i<S2.size(); i++) {
				ki = k2i.get(S2.get(i));
				x += pdv[ki][ui];
			}
			x = x/(double)S2.size();
		}
		return x;
	}
	
	private static double p2(ArrayList<String> S1, String e) {
		double x = 0;
		int ui, ki;
		
		if (S1.size() > 0) {
			ki = k2i.get(e);
			for (int i=0; i<S1.size(); i++) {
				ui = u2i.get(S1.get(i));
				x += pdv[ki][ui];
			}
			x = x/(double)S1.size();
		}
		return x;
	}
	
	// graph cut
	private static double penalty(ArrayList<String> S1) {
		double x = 0;
		int count = 0;
		for (int i=0; i<S1.size(); i++) {
			String user = S1.get(i);
			String loc = userLocMap.get(user);
			String[] locs = locMap.get(loc).split("\\|");
			for (int j=0; j<locs.length; j++) {
				if (locNumMap.containsKey(locs[j]))
					count += locNumMap.get(locs[j]);
			}
			for (int j=0; j<S1.size(); j++) {
				String u = S1.get(j);
				for (int k=0; k<locs.length; k++) {
					if (userLocMap.get(u).equals(locs[k]) && !u.equals(user)){
						count = count - 1;
						break;
					}
				}
			}
		}
		x = (double)count * lambda; // this number is the parameter, we can vary it to find better results
		
		return -x;
	}
	
	private static boolean isUser(String str) {
		for (int i = 0; i < str.length(); i++) {
			if (!Character.isDigit(str.charAt(i))) {
				return false;
			}
		}
		return true;
	}
	
	public static ArrayList<Double> pre_recall(){
		ArrayList<Double> pre_rec_fscore=new ArrayList<Double>();
		ArrayList<String> result_prov_List=new ArrayList<String>();
		Double rel_ret=0.0;
		Set<Entry<String, Integer>> entries = province.entrySet();
		Iterator<Entry<String, Integer>> iter = entries.iterator();
		
		int count=0;
		while(iter.hasNext()) {
			Entry<String, Integer> entry = iter.next();
			if((int)entry.getValue()>=K)
				result_prov_List.add((String) entry.getKey());
		}	
		rel_ret=interesect(true_label,result_prov_List);
		
		//[0]recall,[1]precision,[2]fscore
		double rec,pre,fscore;
		rec=rel_ret/province.size()*1.0;
		pre=rel_ret/true_label.size()*1.0;
		fscore = (rec+pre != 0) ? (2*rec*pre)/(rec+pre) : 0;
		System.out.println("Recall:"+rec+"\nPrecision:"+pre+"\nF-Score:"+fscore);
		pre_rec_fscore.add(rec);
		pre_rec_fscore.add(pre);
		pre_rec_fscore.add(fscore);
		return pre_rec_fscore;
	}
	private static Double interesect(ArrayList<String> f, ArrayList<String> s) { 
	    ArrayList<String> res = new ArrayList<String>();

	    Double int_len=0.0;
	    for(int i=0;i<f.size();i++)
	    	for(int j=0;j<s.size();j++){
	    		if(f.get(i).compareTo(s.get(j))==0){
	    			int_len++;
	    		}
	    	}
	    return int_len; 
	}
	
	static Map sortByValue(Map map) {
	     List list = new LinkedList(map.entrySet());
	     Collections.sort(list, new Comparator() {
	          public int compare(Object o1, Object o2) {
	               return -((Comparable) ((Map.Entry) (o1)).getValue())
	              .compareTo(((Map.Entry) (o2)).getValue());
	          }
	     });

	    Map result = new LinkedHashMap();
	    for (Iterator it = list.iterator(); it.hasNext();) {
	        Map.Entry entry = (Map.Entry)it.next();
	        result.put(entry.getKey(), entry.getValue());
	    }
	    return result;
	}
	
	
	public static void main(String[] args) {
		System.out.println("date(2014-04-12~2015-01-11), K(int), alpha(0~1), lambda(0~1). "
				+ "Sample: 2014-10-21,2,10,0.15,0.015");
		String input = "2014-10-21,1,0.15,0.008";
		String[] inputs = input.split("\\,");

		day = inputs[0];
		K = Integer.parseInt(inputs[1]);
		alpha = Double.parseDouble(inputs[2]);
		lambda = Double.parseDouble(inputs[3]);
		//link database
		initDB();
		//init locMap
		initLocMap();
		randomGreedy();

		DrawPlot.draw(F, F_Low, F2, F2_Up, F1, Penalty);
		System.out.println("True subset: "+true_label.toString());
		pre_recall();
	}

}
