package event;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Main {

	public static void main(String[] args){
		Statement stmt;
		ResultSet rs;
		try {
			RandomGreedyEvent.initDB();
			stmt = RandomGreedyEvent.conn.createStatement();
			String sql = "select distinct day from pdvevent;";
			rs = stmt.executeQuery(sql);
			while(rs.next()){
				String day = rs.getString("day");
				new RandomGreedyEvent(day,1,0.15,0.008);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
