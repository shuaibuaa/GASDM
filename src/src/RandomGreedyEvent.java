package src;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class RandomGreedyEvent {
	
	static Connection conn = null;
	public static String day;
	public static int K = 10;
	public static double alpha = 0.15;
	public static double lambda = 0.015;
	
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
	
	public static ArrayList<Double> F1   = new ArrayList<Double>();
	public static ArrayList<Double> F2   = new ArrayList<Double>();
	public static ArrayList<Double> F2_Up= new ArrayList<Double>(); 
	public static ArrayList<Double> Penalty= new ArrayList<Double>();
	public static ArrayList<Double> F   = new ArrayList<Double>();
	public static ArrayList<Double> F_Low= new ArrayList<Double>();
	
	static String[] e1;
	
	public RandomGreedyEvent(String date,int k,double alpha_v,double lambda_v){
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
		
		F1   = new ArrayList<Double>();
		F2   = new ArrayList<Double>();
		F2_Up= new ArrayList<Double>(); 
		Penalty= new ArrayList<Double>();
		F   = new ArrayList<Double>();
		F_Low= new ArrayList<Double>();
		
		randomGreedy();
	}
	public static void randomGreedy() {
		init();
		greedy();
	}
	
	private static void init() {
		initV();//init V1,V2,userlocMap,pdv
	}
	
	// initial the ground set V, including all users and keywords
	private static void initV() {
		Statement stmt;
		ResultSet urs, krs, prs;
		String user, keyword;
		double pvalue;
		int index = 0;
		try {
			stmt = conn.createStatement();
			//init V1, userLocMap, u2i
			String sql = "select distinct u_id from pdvevent where day = '" + day + "';";	
			urs = stmt.executeQuery(sql);
			V1.clear();
			u2i.clear();
			while(urs.next()){
				user = urs.getString("u_id");
				V1.add(user);
				u2i.put(user, index++); // revert user to index in the array[][] "pdv"
			}
			// init V2, k2i
			sql = "select distinct keyword from pdvevent where day = '" + day + "';";
			index = 0;
			krs = stmt.executeQuery(sql);
			V2.clear();
			k2i.clear();
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
			sql = "select u_id, keyword, pvalue from pdvevent where day = '"+ day +"';"; 
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
	
	//greedy algorithm
	private static void greedy() {
		int r = 1;
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
						F1.add(f1(S1r, S2r));
						F2.add(f2(S1r, S2r));               //F2 score
						F2_Up.add(M(S1r, S2r, S1rt, S2rt)); //F2_upper bound
						Penalty.add(penalty(S1r));
						F.add((f1(S1r, S2r)- f2(S1r, S2r) + penalty(S1r)));                //F score
						F_Low.add((f1(S1r, S2r)- M(S1r, S2r, S1rt, S2rt) + penalty(S1r))); //F_lower bound
					}
				}
			}
		} while (!(compare(S1r, S1rt) && compare(S2r, S2rt)));
		System.out.println("Complete!!");
		// when converge, print the result user set to console
		System.out.println("users: ");
		for(String user:S1r){
			System.out.println(user);
		}
		System.out.println("keywords: ");
		for(String keyword:S2r){
			System.out.println(keyword);
		}
		matchLocation(S1r);
		matchEvents(S2r);
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
		//result += penalty(S1);
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
	private static double penalty(ArrayList<String> S2) {
		double x = 0;
		x = (double)(S2.size()*(V2.size()-S2.size())) * lambda;
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
	
	private static void initDB(){
		String url = "jdbc:mysql://localhost:3306/gasdm?user=root&password=&useUnicode=true&characterEncoding=UTF8";
		try {  
			Class.forName("com.mysql.jdbc.Driver");  
			conn = DriverManager.getConnection(url);
		} catch (Exception e) {  
			e.printStackTrace();  
		}
	}
	
	private static double match(String[] e, ArrayList<String> S){
		int count=0;
		for(String word : e){
			if(S.contains(word))
				count++;
		}
		return (double)count/e.length;
	}
	
	private static void matchEvents(ArrayList<String> S2){
		String[] e1 = {"恐怖","袭击","组织","政府","谴责","炸药","慰问电","遇难者","枪击","紧急状态","恐怖主义","暴力"};
		String[] e2 = {"核武器","核试验","氢弹","原子弹","试验","核爆","爆炸","核武","核设施","核基地","核计划","核爆炸","核聚变","局势"};
		String[] e3 = {"客轮","调查","渡船","家属","沉船","倾覆","翻船","乘客","遗体","客船","幸存者","天气","救援","轮船"};
		String[] e4 = {"病例","登革热","登革热病","确诊","死亡","临床","疫情","诊断","病毒","卫生","预防","患者","感染","医院","治疗","蚊虫"};
		String[] e5 = {"滑坡","山体","遇难","现场","救援","目前","塌方","死亡","人数","失踪","人员","房屋","生命","救出","村民","探测","安置",
				"抢救","群众","事故","警方","被困","灾害","调查组","垮塌","遇难者","安全"};
		String[] e6 = {"爆炸","现场","氰化物","事故","污染","环境","消防","官兵","超标","公安","火灾","化学","环保","海水","排放","应急","污水",
				"牺牲","居民","泄露"};
		String[] e7 = {"击落","战机","总统","道歉","运输","车队","飞机","边境","空袭","领土","空军","打击","领空"};
		String[] e8 = {"踩踏","事件","伤员","家属","卫生","警察","拥挤","救治","伤者","秩序","惨痛","重伤","遇难","现场","救援","遇难者","安全",
				"抢救","群众","事故","警方"};
		String[] e9 = {"日本","中国","钓鱼岛","报道","南海","海域","海警","自卫队","军舰","巡航","中方","日方","外交部","战机","演习","威胁",
				"军事","警告","海军","领海","主权"};
		double x1 = match(e1,S2);
		double x2 = match(e2,S2);
		double x3 = match(e3,S2);
		double x4 = match(e4,S2);
		double x5 = match(e5,S2);
		double x6 = match(e6,S2);
		double x7 = match(e7,S2);
		double x8 = match(e8,S2);
		double x9 = match(e9,S2);
		double x[] = {x1,x2,x3,x4,x5,x6,x7,x8,x9};
		Arrays.sort(x);
		double max = x[x.length-1];
		if(max==x1)
			System.out.println("event: 恐怖袭击");
		else if(max==x2)
			System.out.println("event: 核试验");
		else if(max==x3)
			System.out.println("event: 沉船事故");
		else if(max==x4)
			System.out.println("event: 登革热疫情");
		else if(max==x5&&S2.contains("滑坡"))
			System.out.println("event: 山体滑坡");
		else if(max==x6&&S2.contains("火灾"))
			System.out.println("event: 火灾爆炸");
		else if(max==x7)
			System.out.println("event: 击落战机");
		else if(max==x8&&S2.contains("踩踏"))
			System.out.println("event: 踩踏事故");
		else if(max==x9)
			System.out.println("event: 钓鱼岛事件");
	}
	
	private static void matchLocation(ArrayList<String> S1){
		Statement stmt;
		ResultSet rs;
		HashMap<String, Integer> locNumMap = new HashMap<String, Integer>();
		String sql;
		for(String user:S1){
			try {
				stmt = conn.createStatement();
				sql = "select location from pdvevent where u_id = "+user+";";
				rs = stmt.executeQuery(sql);
				if(rs.next()){
					String loc = rs.getString("location");
					if(locNumMap.containsKey(loc))
						locNumMap.put(loc, locNumMap.get(loc)+1);
					else
						locNumMap.put(loc, 1);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		int resnum=0;
		String num, loc, resloc="";
		Iterator iter = locNumMap.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			loc = entry.getKey().toString();
			num = entry.getValue().toString();
			if(Integer.parseInt(num)>resnum){
				resnum = Integer.parseInt(num);
				resloc = loc;
			}
		}
		System.out.println("location: "+resloc);
	}
	
	public static void main(String[] args) {
		initDB();
		new RandomGreedyEvent("2015-01-01",1,0.15,0.008);
		DrawPlot.draw(F, F_Low, F2, F2_Up, F1, Penalty);
	}

}
