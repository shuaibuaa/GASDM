package src;



import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class RandomGreedyTest {
	
	static Connection conn = null;
	static String day;
	static double alpha = 0.15;
	static double score;
	static double lastscore;
	static double[][] pdv;
	static double FY;
	static double GYY;
	static double FV;
	static ArrayList<String> V1   = new ArrayList<String>();
	static ArrayList<String> V2   = new ArrayList<String>();
	static ArrayList<String> S1r  = new ArrayList<String>();
	static ArrayList<String> S2r  = new ArrayList<String>();
	static ArrayList<String> S1rt = new ArrayList<String>();
	static ArrayList<String> S2rt = new ArrayList<String>();
	static ArrayList<String> random = new ArrayList<String>();
	static HashMap<String, Integer> u2i = new HashMap<String, Integer>();
	static HashMap<String, Integer> k2i = new HashMap<String, Integer>();
	static HashMap<String, String> userLocMap = new HashMap<String, String>();
	static HashMap<String, String> locMap = new HashMap<String, String>();
	static HashMap<String, Integer> locNumMap = new HashMap<String, Integer>();
	static HashMap<String, Integer> locConnNumMap = new HashMap<String, Integer>();


	public static void randomGreedy(Date date) {
		init(date);
		greedy();
	}
	
	private static void init(Date date) {
		//init day
		initDate(date);
		//init V1,V2,userlocMap,pdv
		initV();
		//init locNumMap
		initLocNumMap();
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
		String url = "jdbc:mysql://localhost:3306/gasdm?user=root&password=&useUnicode=true&characterEncoding=UTF8";
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
			String sql = "select distinct u_id, location from test where day = '" + day + "';";	
			urs = stmt.executeQuery(sql);
			V1.clear();u2i.clear();userLocMap.clear();
			while(urs.next()){
				user = urs.getString("u_id");
				loc = urs.getString("location");
				V1.add(user);
				u2i.put(user, index++); // revert user to index in the array[][] "pdv"
				userLocMap.put(user, loc);
			}
			// init V2, k2i
			sql = "select distinct keyword from test where day = '" + day + "';";
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
			sql = "select u_id, keyword, pvalue from test where day = '"+ day +"';"; 
			prs = stmt.executeQuery(sql);
			while(prs.next()){
				user = prs.getString("u_id");
				keyword = prs.getString("keyword");
				pvalue = prs.getDouble("pvalue");
				pdv[k2i.get(keyword)][u2i.get(user)] = pvalue;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	// initial locMap<String, String>, <province, its neighbor provinces>
	private static void initLocMap() {
		locMap.put("A","B|C|D");
		locMap.put("B","A|C|E|F");
		locMap.put("C","A|B|E");
		locMap.put("D","A|E|F");
		locMap.put("E","B|C|D|F");
		locMap.put("F","B|D|E");
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
		int r = 0;
		FV = f2(V1,V2); // will be use in function G, to reduce the complexity
		random.addAll(V1);
		random.addAll(V2);
		do {
			System.out.println("r = "+r);
			r = r + 1;
			S1rt.clear();S1rt.addAll(S1r);
			S2rt.clear();S2rt.addAll(S2r);
			randomPermutation();
			
			FY = f2(S1rt, S2rt);// will be use in function M and G
			GYY = G0(S1rt, S2rt, S1rt, S2rt);// will be use in function G, (S1t for S1(t-1) and S2t for S2(t-1), refers to the result sets of last iteration)
			
//			System.out.println("original "+r+" score: "+ (f1(S1rt, S2rt)- f2(S1rt, S2rt)+penalty(S1rt)));
//			System.out.println("begin " +r+" lower bound score: "+ (f1(S1rt, S2rt)- M(S1rt, S2rt, S1rt, S2rt)+penalty(S1rt)));
			
			for (int i=0; i<random.size(); i++) {
				ArrayList<String> t = new ArrayList<String>();
				String e = random.get(i);
				// there is a little different from the theory to reduce the complexity, cause (add keyword to user set)=0, (add user to keyword set)=0
				if (isUser(e)) {// there are only numbers in e, indicate that it's a user
					double x = f(S1r, S2r, S1rt, S2rt);
					t.clear();t.addAll(S1r);
					t.add(e);
					double y = f(t, S2r, S1rt, S2rt);
					x = y - x;
					if (x > 0){
						S1r.add(e);
						score = y;
						System.out.println(e);
						System.out.println("original score: "+ (f1(S1r, S2r)- f2(S1r, S2r) + penalty(S1r)));
						System.out.println("lower bound score: "+ score + "\t"+((f1(S1r, S2r)- f2(S1r, S2r) + penalty(S1r))>=score?true:false));
						System.out.println("original F2: "+ f2(S1r, S2r));
						System.out.println("upper bound MF2: "+ M(S1r, S2r, S1rt, S2rt) + "\t"+(M(S1r, S2r, S1rt, S2rt)>=f2(S1r, S2r)?true:false));
						System.out.println();
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
						System.out.println(e);
						System.out.println("original score: "+ (f1(S1r, S2r)- f2(S1r, S2r) + penalty(S1r)));
						System.out.println("lower bound score: "+ score + "\t"+((f1(S1r, S2r)- f2(S1r, S2r) + penalty(S1r))>=score?true:false));
						System.out.println("original F2: "+ f2(S1r, S2r));
						System.out.println("upper bound MF2: "+ M(S1r, S2r, S1rt, S2rt) + "\t"+(M(S1r, S2r, S1rt, S2rt)>=f2(S1r, S2r)?true:false));
						System.out.println();
					}
				}
			}
//			System.out.println("S1 size: " + S1r.size() + "\tS2 size: "+S2r.size());
//			System.out.println("end "+r+" lower bound score: "+ score);
			System.out.println();
		} while (!(compare(S1r, S1rt) && compare(S2r, S2rt)));
		System.out.println("Complete!!");
		// when converge, print the result user set to console
		int i;
		for (i=0; i<S1r.size(); i++) {
			System.out.print(userLocMap.get(S1r.get(i)));
			if(i%5==4)
				System.out.println();
			else{
				if (userLocMap.get(S1r.get(i)).length()<8)
					System.out.print("\t");
				System.out.print("\t");
			}
		}
		if(i%5!=0)
			System.out.println();
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
		int method = 2;
		switch(method) {
		case 0: x = FY + G0(S1, S2, S1rt, S2rt)  - GYY; break;
		case 1: x = FY + G1(S1, S2, S1rt, S2rt) - GYY ; break;
		case 2: x = FY + G2(S1, S2, S1rt, S2rt) - GYY ; break;
		case 3: x = FY + G0_1(S1, S2, S1rt, S2rt)- GYY; break;
		}
		
		return x;
	}
	
	private static double G0(ArrayList<String> S1, ArrayList<String> S2, ArrayList<String> S1rt, ArrayList<String> S2rt) {
		double x = 0;
		ArrayList<String> t = new ArrayList<String>();
		for (int i=0; i<S1.size(); i++) {
			String e = S1.get(i);
			if (S1rt.contains(e)) {
				x += FV;
				t.clear();t.addAll(V1);t.remove(e);
				x -= f2(t, V2);
			}
			else {
				t.clear();t.addAll(S1rt);t.add(e);
				x += f2(t, S2rt);
				x -= FY;
			}
		}
		for (int i=0; i<S2.size(); i++) {
			String e = S2.get(i);
			if (S2rt.contains(e)) {
				x += FV;
				t.clear();t.addAll(V2);t.remove(e);
				x -= f2(V1, t);
			}
			else {
				t.clear();t.addAll(S2rt);t.add(e);
				x += f2(S1rt, t);
				x -= FY;
			}
		}
		return x;
	}
	private static double G0_1(ArrayList<String> S1, ArrayList<String> S2, ArrayList<String> S1rt, ArrayList<String> S2rt) {
		double x = 0;
		ArrayList<String> t = new ArrayList<String>();
		ArrayList<String> s = new ArrayList<String>();
		s.addAll(S1rt);s.addAll(S2rt);s.removeAll(S1);s.removeAll(S2);
		for (int i=0; i<s.size(); i++) {
			String e = s.get(i);
			if (isUser(e)) {
				x += FY;
				t.clear();t.addAll(S1rt);t.remove(e);
				x -= f2(t, S2rt);
			}
			else {
				x += FY;
				t.clear();t.addAll(S2rt);t.remove(e);
				x -= f2(S1rt, t);
			}
		}
		s.clear();s.addAll(S1);s.addAll(S2);s.removeAll(S1rt);s.removeAll(S2rt);
		ArrayList<String> empty = new ArrayList<String>();
		empty.clear();
		for (int i=0; i<s.size(); i++) {
			String e = s.get(i);
			if (isUser(e)) {
				t.clear();t.add(e);
				x += f2(t, empty);
				x -= f2(empty, empty);
			}
			else {
				t.clear();t.add(e);
				x += f2(empty, t);
				x -= f2(empty, empty);
			}
		}
		return x;
	}
	private static double G1(ArrayList<String> S1, ArrayList<String> S2, ArrayList<String> S1rt, ArrayList<String> S2rt) {
		double x = 0;
		ArrayList<String> t = new ArrayList<String>();
		ArrayList<String> s = new ArrayList<String>();
		s.addAll(S1rt);s.addAll(S2rt);s.removeAll(S1);s.removeAll(S2);
		for (int i=0; i<s.size(); i++) {
			String e = s.get(i);
			if (isUser(e)) {
				x += FY;
				t.clear();t.addAll(S1rt);t.remove(e);
				x -= f2(t, S2rt);
			}
			else {
				x += FY;
				t.clear();t.addAll(S2rt);t.remove(e);
				x -= f2(S1rt, t);
			}
		}
		s.clear();s.addAll(S1);s.addAll(S2);s.removeAll(S1rt);s.removeAll(S2rt);
		ArrayList<String> empty = new ArrayList<String>();
		empty.clear();
		for (int i=0; i<s.size(); i++) {
			String e = s.get(i);
			if (isUser(e)) {
				t.clear();t.add(e);
				x += f2(t, empty);
				x -= f2(empty, empty);
			}
			else {
				t.clear();t.add(e);
				x += f2(empty, t);
				x -= f2(empty, empty);
			}
		}
		return x;
	}
	private static double G2(ArrayList<String> S1, ArrayList<String> S2, ArrayList<String> S1rt, ArrayList<String> S2rt) {
		double x = 0;
		ArrayList<String> t = new ArrayList<String>();
		ArrayList<String> s = new ArrayList<String>();
		s.addAll(S1rt);s.addAll(S2rt);s.removeAll(S1);s.removeAll(S2);
		for (int i=0; i<s.size(); i++) {
			String e = s.get(i);
			if (isUser(e)) {
				x += FV;
				t.clear();t.addAll(V1);t.remove(e);
				x -= f2(t, V2);
			}
			else {
				x += FV;
				t.clear();t.addAll(V2);t.remove(e);
				x -= f2(V1, t);
			}
		}
		s.clear();s.addAll(S1);s.addAll(S2);s.removeAll(S1rt);s.removeAll(S2rt);
		for (int i=0; i<s.size(); i++) {
			String e = s.get(i);
			if (isUser(e)) {
				x -= FY;
				t.clear();t.addAll(S1rt);t.add(e);
				x += f2(t, S2rt);
			}
			else {
				x -= FY;
				t.clear();t.addAll(S2rt);t.add(e);
				x += f2(S1rt, t);
			}
		}
		return x;
	}	
	// F2 = g(S1,S2)log伪 - g(S1,S2)log(g(S1,S2)) + (|S1|+|S2|-g(S1,S2))log鈦�(1-伪) - (|S1|+|S2|-g(S1,S2))log(|S1|+|S2|-g(S1,S2))
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
	
	// g = sum(I(p(S2)(v)<=伪)) + sum(I(p(S1)(d)<=伪)), here v for user, d for keyword
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
		x = (double)count/40; // this number is the parameter, we can vary it to find better results
		
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

	public static void main(String[] args) {
		Date d0 = new Date(114,9,20);//the day before the beginning day, with format "(year-1900, month-1, date)"
		Date dn = new Date(114,9,21);//the day after the ending day
		Date date = new Date(114,9,20);
		date.setTime(d0.getTime());
		//link database
		initDB();
		//init locMap
		initLocMap();
		// do random greedy every day
		do { 
			date.setTime(date.getTime()+1000*3600*24);
			System.out.println("Year: "+date.getYear()+" Month: "+date.getMonth()+" Date: "+date.getDate());
			System.out.println();
			randomGreedy(date);
		} while(date.before(dn) && date.after(d0));
	}

}
