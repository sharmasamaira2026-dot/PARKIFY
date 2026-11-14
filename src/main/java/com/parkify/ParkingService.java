package com.parkify;

import java.math.BigDecimal;
import java.sql.*;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class ParkingService {
    public record Slot(int id, String status, String vehicleNumber, long minutes) {}
    public record Vehicle(long id,String number,String owner,String type,int slot,Timestamp entry,Timestamp exit,Long minutes,BigDecimal fee,String status) {}
    public record Stats(int total,int occupied,int available,BigDecimal revenue,long served,long parked) {}
    public List<Slot> slots() throws SQLException {
        String q="SELECT s.slot_id,s.status,v.vehicle_number,v.entry_time FROM parking_slots s LEFT JOIN vehicles v ON v.slot_id=s.slot_id AND v.status='PARKED' ORDER BY s.slot_id";
        List<Slot> out=new ArrayList<>();
        try(Connection c=DB.get();Statement st=c.createStatement();ResultSet rs=st.executeQuery(q)){
            while(rs.next()){Timestamp t=rs.getTimestamp(4);long m=t==null?0:ChronoUnit.MINUTES.between(t.toLocalDateTime(),LocalDateTime.now());out.add(new Slot(rs.getInt(1),rs.getString(2),rs.getString(3),m));}
        } return out;
    }
    public void park(String number,String owner,String type,int slot) throws SQLException {
        if(number.isBlank()||owner.isBlank()) throw new IllegalArgumentException("Vehicle number and owner name are required.");
        try(Connection c=DB.get()){
            c.setAutoCommit(false);
            try {
                try(PreparedStatement p=c.prepareStatement("SELECT status FROM parking_slots WHERE slot_id=? FOR UPDATE")) {
                    p.setInt(1,slot);try(ResultSet r=p.executeQuery()){if(!r.next()||!r.getString(1).equals("AVAILABLE"))throw new IllegalStateException("Selected slot is not available.");}
                }
                try(PreparedStatement p=c.prepareStatement("SELECT COUNT(*) FROM vehicles WHERE UPPER(vehicle_number)=UPPER(?) AND status='PARKED'")){
                    p.setString(1,number.trim());try(ResultSet r=p.executeQuery()){r.next();if(r.getInt(1)>0)throw new IllegalStateException("This vehicle is already parked.");}
                }
                try(PreparedStatement p=c.prepareStatement("INSERT INTO vehicles(vehicle_number,owner_name,vehicle_type,slot_id) VALUES(?,?,?,?)")){
                    p.setString(1,number.trim().toUpperCase());p.setString(2,owner.trim());p.setString(3,type);p.setInt(4,slot);p.executeUpdate();
                }
                try(PreparedStatement p=c.prepareStatement("UPDATE parking_slots SET status='OCCUPIED' WHERE slot_id=?")){p.setInt(1,slot);p.executeUpdate();}
                c.commit();
            } catch(Exception e){c.rollback();if(e instanceof SQLException se)throw se;if(e instanceof RuntimeException re)throw re;throw new SQLException(e);}
        }
    }
    public Vehicle exit(long id) throws SQLException {
        try(Connection c=DB.get()){
            c.setAutoCommit(false);
            try {
                Vehicle v;
                try(PreparedStatement p=c.prepareStatement("SELECT * FROM vehicles WHERE vehicle_id=? AND status='PARKED' FOR UPDATE")){
                    p.setLong(1,id);try(ResultSet r=p.executeQuery()){if(!r.next())throw new IllegalStateException("Active parking record not found.");v=map(r);}
                }
                LocalDateTime now=LocalDateTime.now();long mins=Math.max(1,ChronoUnit.MINUTES.between(v.entry().toLocalDateTime(),now));
                long hours=Math.max(1,(long)Math.ceil(mins/60.0));
                BigDecimal rate;
                try(PreparedStatement p=c.prepareStatement("SELECT hourly_rate FROM vehicle_rates WHERE vehicle_type=?")){p.setString(1,v.type());try(ResultSet r=p.executeQuery()){if(!r.next())throw new SQLException("Rate not configured.");rate=r.getBigDecimal(1);}}
                BigDecimal fee=rate.multiply(BigDecimal.valueOf(hours));
                try(PreparedStatement p=c.prepareStatement("UPDATE vehicles SET status='EXITED',exit_time=?,duration_minutes=?,fee=? WHERE vehicle_id=?")){
                    p.setTimestamp(1,Timestamp.valueOf(now));p.setLong(2,mins);p.setBigDecimal(3,fee);p.setLong(4,id);p.executeUpdate();
                }
                try(PreparedStatement p=c.prepareStatement("UPDATE parking_slots SET status='AVAILABLE' WHERE slot_id=?")){p.setInt(1,v.slot());p.executeUpdate();}
                c.commit();return new Vehicle(v.id(),v.number(),v.owner(),v.type(),v.slot(),v.entry(),Timestamp.valueOf(now),mins,fee,"EXITED");
            }catch(Exception e){c.rollback();if(e instanceof SQLException se)throw se;if(e instanceof RuntimeException re)throw re;throw new SQLException(e);}
        }
    }
    public List<Vehicle> vehicles(String filter, boolean historyOnly) throws SQLException {
        String q="SELECT * FROM vehicles WHERE (?='' OR vehicle_number LIKE ? OR owner_name LIKE ?) "+(historyOnly?"AND status='EXITED' ":"")+"ORDER BY entry_time DESC";
        List<Vehicle> out=new ArrayList<>();String f=filter==null?"":filter.trim();
        try(Connection c=DB.get();PreparedStatement p=c.prepareStatement(q)){p.setString(1,f);p.setString(2,"%"+f+"%");p.setString(3,"%"+f+"%");try(ResultSet r=p.executeQuery()){while(r.next())out.add(map(r));}}
        return out;
    }
    public Stats stats() throws SQLException {
        try(Connection c=DB.get();Statement s=c.createStatement()){
            int total=20,occ=0;BigDecimal rev=BigDecimal.ZERO;long served=0,parked=0;
            try(ResultSet r=s.executeQuery("SELECT COUNT(*) FROM parking_slots WHERE status='OCCUPIED'")){r.next();occ=r.getInt(1);}
            try(ResultSet r=s.executeQuery("SELECT COALESCE(SUM(fee),0),COUNT(*),SUM(status='PARKED') FROM vehicles")){r.next();rev=r.getBigDecimal(1);served=r.getLong(2);parked=r.getLong(3);}
            return new Stats(total,occ,total-occ,rev,served,parked);
        }
    }
    public Map<String,BigDecimal> revenueByType() throws SQLException {
        Map<String,BigDecimal> m=new LinkedHashMap<>();for(String t:List.of("Car","Bike","Scooter","Auto","Truck"))m.put(t,BigDecimal.ZERO);
        try(Connection c=DB.get();Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT vehicle_type,COALESCE(SUM(fee),0) FROM vehicles WHERE status='EXITED' GROUP BY vehicle_type")){while(r.next())m.put(r.getString(1),r.getBigDecimal(2));}return m;
    }
    public void updateRate(String type,BigDecimal rate) throws SQLException {
        if(rate.signum()<=0)throw new IllegalArgumentException("Rate must be greater than zero.");
        try(Connection c=DB.get();PreparedStatement p=c.prepareStatement("INSERT INTO vehicle_rates(vehicle_type,hourly_rate) VALUES(?,?) ON DUPLICATE KEY UPDATE hourly_rate=VALUES(hourly_rate)")){p.setString(1,type);p.setBigDecimal(2,rate);p.executeUpdate();}
    }
    private Vehicle map(ResultSet r)throws SQLException{return new Vehicle(r.getLong("vehicle_id"),r.getString("vehicle_number"),r.getString("owner_name"),r.getString("vehicle_type"),r.getInt("slot_id"),r.getTimestamp("entry_time"),r.getTimestamp("exit_time"),(Long)r.getObject("duration_minutes"),r.getBigDecimal("fee"),r.getString("status"));}
}
