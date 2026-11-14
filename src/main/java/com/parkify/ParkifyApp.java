package com.parkify;

import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class ParkifyApp extends JFrame {
    private final ParkingService service=new ParkingService();
    private final Color navy=new Color(18,38,68), blue=new Color(32,78,130), green=new Color(31,150,103), red=new Color(205, seventy(), 78);
    private JPanel content; private JLabel clock;
    private static int seventy(){return  seventyValue();} private static int seventyValue(){return 70;}
    public ParkifyApp(){
        super("PARKIFY | Vehicle Parking Management System");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);setSize(1180,760);setMinimumSize(new Dimension(980,650));setLocationRelativeTo(null);
        addWindowListener(new java.awt.event.WindowAdapter(){public void windowClosing(java.awt.event.WindowEvent e){exitApp();}});
        buildShell(); showDashboard();
        new javax.swing.Timer(1000,e->{if(clock!=null)clock.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy  |  hh:mm:ss a")));}).start();
    }
    private void buildShell(){
        JPanel root=new JPanel(new BorderLayout());root.setBackground(new Color(245,247,251));setContentPane(root);
        JPanel side=new JPanel();side.setBackground(navy);side.setPreferredSize(new Dimension(220,0));side.setLayout(new BoxLayout(side,BoxLayout.Y_AXIS));side.setBorder(new EmptyBorder(24,16,20,16));
        JLabel brand=new JLabel("  PARKIFY");brand.setForeground(Color.WHITE);brand.setFont(new Font("Segoe UI",Font.BOLD,25));brand.setAlignmentX(LEFT_ALIGNMENT);side.add(brand);
        JLabel sub=new JLabel("  PARKING MANAGEMENT");sub.setForeground(new Color(166,190,220));sub.setFont(new Font("Segoe UI",Font.PLAIN,10));sub.setAlignmentX(LEFT_ALIGNMENT);side.add(sub);side.add(Box.createVerticalStrut(35));
        nav(side,"Dashboard",this::showDashboard);nav(side,"Vehicle Entry",this::showEntry);nav(side,"Exit & Billing",this::showExit);nav(side,"Parking History",this::showHistory);nav(side,"Search Vehicle",this::showSearch);nav(side,"Revenue",this::showRevenue);nav(side,"Rates",this::showRates);
        side.add(Box.createVerticalGlue());JButton exit=navButton("Exit Application");exit.addActionListener(e->exitApp());side.add(exit);
        root.add(side,BorderLayout.WEST);
        JPanel main=new JPanel(new BorderLayout());main.setOpaque(false);main.setBorder(new EmptyBorder(20,24,22,24));root.add(main,BorderLayout.CENTER);
        JPanel top=new JPanel(new BorderLayout());top.setOpaque(false);JLabel title=new JLabel("Vehicle Parking Management");title.setFont(new Font("Segoe UI",Font.BOLD,22));title.setForeground(navy);top.add(title,BorderLayout.WEST);
        clock=new JLabel();clock.setFont(new Font("Segoe UI",Font.BOLD,13));clock.setForeground(blue);top.add(clock,BorderLayout.EAST);main.add(top,BorderLayout.NORTH);
        content=new JPanel(new BorderLayout(14,14));content.setOpaque(false);content.setBorder(new EmptyBorder(22,0,0,0));main.add(content,BorderLayout.CENTER);
    }
    private void nav(JPanel p,String label,Runnable action){JButton b=navButton(label);b.addActionListener(e->action.run());p.add(b);p.add(Box.createVerticalStrut(7));}
    private JButton navButton(String s){JButton b=new JButton(s);b.setHorizontalAlignment(SwingConstants.LEFT);b.setMaximumSize(new Dimension(190,42));b.setForeground(Color.WHITE);b.setBackground(new Color(30,58,94));b.setFocusPainted(false);b.setBorder(new EmptyBorder(10,14,10,10));b.setFont(new Font("Segoe UI",Font.BOLD,13));return b;}
    private void page(String title){content.removeAll();JLabel h=new JLabel(title);h.setFont(new Font("Segoe UI",Font.BOLD,20));h.setForeground(navy);content.add(h,BorderLayout.NORTH);content.revalidate();content.repaint();}
    private JPanel card(String title,String value,Color accent){JPanel p=new JPanel(new BorderLayout(4,8));p.setBackground(Color.WHITE);p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0,4,0,0,accent),new EmptyBorder(15,17,15,17)));JLabel t=new JLabel(title.toUpperCase());t.setForeground(new Color(105,119,139));t.setFont(new Font("Segoe UI",Font.BOLD,11));JLabel v=new JLabel(value);v.setForeground(navy);v.setFont(new Font("Segoe UI",Font.BOLD,25));p.add(t,BorderLayout.NORTH);p.add(v,BorderLayout.CENTER);return p;}
    private void showDashboard(){
        page("Dashboard");try{
            ParkingService.Stats s=service.stats();JPanel body=new JPanel(new BorderLayout(14,18));body.setOpaque(false);
            JPanel stats=new JPanel(new GridLayout(1,4,12,12));stats.setOpaque(false);stats.add(card("Total Slots",String.valueOf(s.total()),blue));stats.add(card("Occupied",String.valueOf(s.occupied()),red));stats.add(card("Available",String.valueOf(s.available()),green));stats.add(card("Revenue Collected","₹"+s.revenue(),new Color(214,145,35)));body.add(stats,BorderLayout.NORTH);
            JPanel grid=new JPanel(new GridLayout(4,5,12,12));grid.setOpaque(false);
            for(ParkingService.Slot sl:service.slots()){
                boolean free=sl.status().equals("AVAILABLE");JButton b=new JButton("<html><center><b> SLOT "+sl.id()+"</b><br>"+(free?"AVAILABLE":sl.vehicleNumber()+"<br>"+formatMins(sl.minutes()))+"</center></html>");
                b.setBackground(free?new Color(220,246,234):new Color(255,226,228));b.setForeground(free?new Color(20,112,75):new Color(160,48,54));b.setFocusPainted(false);b.setFont(new Font("Segoe UI",Font.PLAIN,12));b.setBorder(BorderFactory.createLineBorder(free?new Color(119,205,163):new Color(232,155,160),1));b.addActionListener(e->{if(free){showEntryWithSlot(sl.id());}else{showExit();}});grid.add(b);
            }
            JPanel slots=new JPanel(new BorderLayout(0,10));slots.setOpaque(false);JLabel label=new JLabel("LIVE PARKING GRID  •  Select an available slot to park, or occupied slot to process exit");label.setForeground(new Color(89,105,127));slots.add(label,BorderLayout.NORTH);slots.add(grid,BorderLayout.CENTER);body.add(slots,BorderLayout.CENTER);content.add(body,BorderLayout.CENTER);
        }catch(Exception e){error(e);}
        content.revalidate();content.repaint();
    }
    private String formatMins(long m){return (m/60)+"h "+(m%60)+"m";}
    private void showEntry(){showEntryWithSlot(-1);}
    private void showEntryWithSlot(int selected){
        page("Vehicle Entry");try{
            List<ParkingService.Slot> slots=service.slots().stream().filter(s->s.status().equals("AVAILABLE")).toList();
            JPanel form=formPanel();JTextField number=field(),owner=field();JComboBox<String> type=new JComboBox<>(new String[]{"Car","Bike","Scooter","Auto","Truck"});JComboBox<Integer> slot=new JComboBox<>(slots.stream().map(ParkingService.Slot::id).toArray(Integer[]::new));if(selected>0)slot.setSelectedItem(selected);
            addField(form,"Vehicle Number",number);addField(form,"Owner Name",owner);addField(form,"Vehicle Type",type);addField(form,"Available Slot",slot);
            JButton save=primary("Confirm Vehicle Entry");save.setEnabled(!slots.isEmpty());save.addActionListener(e->{try{service.park(number.getText(),owner.getText(),(String)type.getSelectedItem(),(Integer)slot.getSelectedItem());JOptionPane.showMessageDialog(this,"Vehicle parked successfully.","PARKIFY",JOptionPane.INFORMATION_MESSAGE);showDashboard();}catch(Exception ex){error(ex);}});
            form.add(save);if(slots.isEmpty())form.add(new JLabel("No parking slots are currently available."));content.add(form,BorderLayout.CENTER);
        }catch(Exception e){error(e);}content.revalidate();content.repaint();
    }
    private void showExit(){
        page("Exit & Billing");try{
            List<ParkingService.Vehicle> active=service.vehicles("",false).stream().filter(v->v.status().equals("PARKED")).toList();
            String[] cols={"ID","Vehicle No.","Owner","Type","Slot","Entry Time","Duration"};DefaultTableModel model=new DefaultTableModel(cols,0){public boolean isCellEditable(int r,int c){return false;}};
            for(var v:active){long mins=java.time.Duration.between(v.entry().toLocalDateTime(),LocalDateTime.now()).toMinutes();model.addRow(new Object[]{v.id(),v.number(),v.owner(),v.type(),v.slot(),v.entry(),formatMins(mins)});}
            JTable table=table(model);JPanel panel=new JPanel(new BorderLayout(10,12));panel.setOpaque(false);panel.add(new JScrollPane(table),BorderLayout.CENTER);
            JButton exit=primary("Process Exit & Generate Fee");exit.addActionListener(e->{int row=table.getSelectedRow();if(row<0){warn("Select a parked vehicle first.");return;}long id=((Number)model.getValueAt(table.convertRowIndexToModel(row),0)).longValue();try{var v=service.exit(id);JOptionPane.showMessageDialog(this,"VEHICLE EXITED\nVehicle: "+v.number()+"\nSlot: "+v.slot()+"\nDuration: "+formatMins(v.minutes())+"\nFinal Fee: ₹"+v.fee(),"Parking Receipt",JOptionPane.INFORMATION_MESSAGE);showExit();}catch(Exception ex){error(ex);}});
            JPanel bottom=new JPanel(new FlowLayout(FlowLayout.RIGHT));bottom.setOpaque(false);bottom.add(exit);panel.add(bottom,BorderLayout.SOUTH);content.add(panel,BorderLayout.CENTER);
        }catch(Exception e){error(e);}content.revalidate();content.repaint();
    }
    private void showHistory(){showVehicleTable("Parking History",true);}
    private void showSearch(){showVehicleTable("Search Vehicle",false);}
    private void showVehicleTable(String title,boolean history){
        page(title);JPanel p=new JPanel(new BorderLayout(10,12));p.setOpaque(false);JPanel search=new JPanel(new FlowLayout(FlowLayout.LEFT));search.setOpaque(false);JTextField q=field();q.setPreferredSize(new Dimension(260,36));search.add(new JLabel("Vehicle number / owner:"));search.add(q);JButton go=primary("Search / Refresh");search.add(go);p.add(search,BorderLayout.NORTH);
        DefaultTableModel model=new DefaultTableModel(new String[]{"ID","Vehicle No.","Owner","Type","Slot","Entry","Exit","Duration (min)","Fee","Status"},0){public boolean isCellEditable(int r,int c){return false;}};JTable t=table(model);p.add(new JScrollPane(t),BorderLayout.CENTER);
        Runnable load=()->{try{model.setRowCount(0);for(var v:service.vehicles(q.getText(),history))model.addRow(new Object[]{v.id(),v.number(),v.owner(),v.type(),v.slot(),v.entry(),v.exit()==null?"—":v.exit(),v.minutes()==null?"—":v.minutes(),v.fee()==null?"—":"₹"+v.fee(),v.status()});}catch(Exception ex){error(ex);}};
        go.addActionListener(e->load.run());q.addActionListener(e->load.run());load.run();content.add(p,BorderLayout.CENTER);content.revalidate();content.repaint();
    }
    private void showRevenue(){
        page("Revenue Overview");try{var s=service.stats();JPanel p=new JPanel(new BorderLayout(12,15));p.setOpaque(false);JPanel top=new JPanel(new GridLayout(1,3,12,12));top.setOpaque(false);top.add(card("Total Revenue","₹"+s.revenue(),green));top.add(card("Vehicles Served",String.valueOf(s.served()),blue));top.add(card("Currently Parked",String.valueOf(s.parked()),new Color(214,145,35)));p.add(top,BorderLayout.NORTH);
            DefaultTableModel m=new DefaultTableModel(new String[]{"Vehicle Type","Revenue Collected"},0);for(Map.Entry<String,BigDecimal> e:service.revenueByType().entrySet())m.addRow(new Object[]{e.getKey(),"₹"+e.getValue()});JTable t=table(m);JPanel box=new JPanel(new BorderLayout(8,8));box.setBackground(Color.WHITE);box.setBorder(new EmptyBorder(15,15,15,15));box.add(new JLabel("REVENUE BREAK-UP BY VEHICLE TYPE"),BorderLayout.NORTH);box.add(new JScrollPane(t),BorderLayout.CENTER);p.add(box,BorderLayout.CENTER);content.add(p,BorderLayout.CENTER);
        }catch(Exception e){error(e);}content.revalidate();content.repaint();
    }
    private void showRates(){
        page("Vehicle Hourly Rates");try{JPanel p=formPanel();for(String type:List.of("Car","Bike","Scooter","Auto","Truck")){JTextField rate=field();rate.setText(defaultRate(type));addField(p,type+" rate (₹ / hour)",rate);JButton update=primary("Update "+type+" Rate");update.addActionListener(e->{try{service.updateRate(type,new BigDecimal(rate.getText().trim()));JOptionPane.showMessageDialog(this,type+" rate updated.");}catch(Exception ex){error(ex);}});p.add(update);}content.add(p,BorderLayout.CENTER);}catch(Exception e){error(e);}content.revalidate();content.repaint();
    }
    private String defaultRate(String t){return switch(t){case "Car"->"40";case "Bike","Scooter"->"20";case "Auto"->"30";default->"60";};}
    private JPanel formPanel(){JPanel p=new JPanel();p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));p.setBackground(Color.WHITE);p.setBorder(new EmptyBorder(24,28,24,28));p.setMaximumSize(new Dimension(620,650));return p;}
    private void addField(JPanel p,String label,JComponent input){JLabel l=new JLabel(label);l.setFont(new Font("Segoe UI",Font.BOLD,13));l.setForeground(navy);l.setAlignmentX(LEFT_ALIGNMENT);input.setMaximumSize(new Dimension(520,38));input.setPreferredSize(new Dimension(520,38));input.setAlignmentX(LEFT_ALIGNMENT);p.add(l);p.add(Box.createVerticalStrut(5));p.add(input);p.add(Box.createVerticalStrut(17));}
    private JTextField field(){JTextField f=new JTextField();f.setFont(new Font("Segoe UI",Font.PLAIN,14));return f;}
    private JButton primary(String text){JButton b=new JButton(text);b.setBackground(blue);b.setForeground(Color.WHITE);b.setFont(new Font("Segoe UI",Font.BOLD,13));b.setFocusPainted(false);b.setBorder(new EmptyBorder(11,18,11,18));b.setAlignmentX(LEFT_ALIGNMENT);return b;}
    private JTable table(DefaultTableModel m){JTable t=new JTable(m);t.setRowHeight(30);t.setFont(new Font("Segoe UI",Font.PLAIN,12));t.getTableHeader().setFont(new Font("Segoe UI",Font.BOLD,12));t.setAutoCreateRowSorter(true);return t;}
    private void warn(String s){JOptionPane.showMessageDialog(this,s,"PARKIFY",JOptionPane.WARNING_MESSAGE);}
    private void error(Exception e){JOptionPane.showMessageDialog(this,e.getMessage()==null?e.toString():e.getMessage(),"PARKIFY - Error",JOptionPane.ERROR_MESSAGE);}
    private void exitApp(){if(JOptionPane.showConfirmDialog(this,"Are you sure you want to exit PARKIFY?","Confirm Exit",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION)System.exit(0);}
    public static void main(String[] args){SwingUtilities.invokeLater(()->{try{FlatLightLaf.setup();DB.initialize();new ParkifyApp().setVisible(true);}catch(Exception e){JOptionPane.showMessageDialog(null,"Unable to connect to MySQL.\n\n1. Start MySQL.\n2. Create database using database/parkify.sql.\n3. Configure DB username/password in README.\n\nDetails: "+e.getMessage(),"Database Connection Error",JOptionPane.ERROR_MESSAGE);}});}
}
