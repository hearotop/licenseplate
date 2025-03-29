package org.license.UI;

import org.json.JSONArray;
import org.json.JSONObject;
import org.license.Classes.GuestInfo;
import org.license.utils.GetGuestInfo;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.security.Guard;
import java.util.HashMap;
import java.util.Map;

public class UserPanel {
    private Map<String, String> columnMap = new HashMap<>();
    private GuestInfo guestInfo;
    private GetGuestInfo getGuestInfo;
    private JTable table;
    private JPanel guestTablePanel = new JPanel(new BorderLayout());
    private JTextField searchField;
    private JButton searchButton;
    private String Query;
    private JPanel guestPanel;

    public void Useranel() {
       guestInfo = new GuestInfo();
        initMap();
    }

    public JPanel userPanel() {
        guestPanel = new JPanel(new BorderLayout());
        guestPanel.setBorder(BorderFactory.createTitledBorder("人员录入管理"));
        JPanel buttonPanel = new JPanel(new GridLayout(1, 4));
        JButton addCardButton = new JButton("添加人员");
        JButton updateCardButton = new JButton("修改人员");
        JButton deleteCardButton = new JButton("删除人员");
        JButton refreshButton = new JButton("刷新信息");
        buttonPanel.add(addCardButton);
        buttonPanel.add(updateCardButton);
        buttonPanel.add(deleteCardButton);
        buttonPanel.add(refreshButton);

        refreshButton.addActionListener(e -> reFresh());
        updateCardButton.addActionListener(e -> updateTable());
        deleteCardButton.addActionListener(e -> deleteTable());
        addCardButton.addActionListener(e -> addTable());

        searchButton = new JButton("查询人员");
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchField = new JTextField(10);

        searchButton.addActionListener(e -> {
           Query = searchField.getText();
           getGuestInfo = new GetGuestInfo();
           getGuestInfo.setGusetName(Query);
           System.out.println(Query);
            reFresh();
            Query="";
            getGuestInfo.setGusetName(Query);
        });

        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchButton, BorderLayout.EAST);

        guestPanel.add(searchPanel, BorderLayout.NORTH);
        guestTablePanel.add(createTable());
        guestPanel.add(guestTablePanel, BorderLayout.CENTER);
        guestPanel.add(buttonPanel, BorderLayout.SOUTH);

        return guestPanel;
    }

    private JScrollPane createTable() {
        String[] columnNames = {"工号", "姓名"};
        Object[][] data = getTableData();
        DefaultTableModel tableModel = new DefaultTableModel(data, columnNames);
        table = new JTable(tableModel);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setResizingAllowed(false);
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                    int selectedRow = table.getSelectedRow();
                    int guestId = (int) table.getValueAt(selectedRow, 0);
                    String guestName = (String) table.getValueAt(selectedRow, 1);
                    // 处理选中的行，例如显示详细信息或进行其他操作
                }
            }
        });

        return new JScrollPane(table);
    }

    private void deleteTable() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            int guestId = (int) table.getValueAt(selectedRow, 0);
            int result = JOptionPane.showConfirmDialog(null, "确定删除吗？", "确认删除", JOptionPane.YES_NO_OPTION);

            if (result == JOptionPane.YES_OPTION) {
                boolean deleteFlag =false; //carInfo.deleteCarCode(carId);
                if (deleteFlag) {
                    JOptionPane.showMessageDialog(null, "删除成功！", "删除成功", JOptionPane.INFORMATION_MESSAGE);
                    reFresh();
                } else {
                    JOptionPane.showMessageDialog(null, "删除失败", "删除失败", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(null, "请选择一行数据", "选择数据", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateTable() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            Object guestIdObject = table.getValueAt(selectedRow, 0);
            Object guetNameObject = table.getValueAt(selectedRow, 1);

            if (guestIdObject instanceof Integer &&  guetNameObject instanceof String) {
                int guestId = (Integer) guestIdObject;
                String licensePlates = (String)  guetNameObject;
                boolean updateSuccess = false;//carInfo.updatecarinfo(licensePlates, carId);
                if (updateSuccess) {
                    JOptionPane.showMessageDialog(null, "修改成功！", "修改成功", JOptionPane.INFORMATION_MESSAGE);
                    reFresh();
                } else {
                    JOptionPane.showMessageDialog(null, "修改失败！", "修改失败", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(null, "修改失败！", "修改失败", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(null, "请选择一行数据！", "修改错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addTable() {
        String newNames = JOptionPane.showInputDialog("输入姓名:");
        int newGuestIds = Integer.parseInt(JOptionPane.showInputDialog("输入工号:"));
     //   carInfo.setGuestId(newGuestIds);
        if (!newNames.isEmpty()) {

                boolean addFlag =false;// carInfo.addcarinfo(newLicensePlates, carInfo.getGuestId());
                if (addFlag) {
                    JOptionPane.showMessageDialog(null, "添加成功！", "添加成功", JOptionPane.INFORMATION_MESSAGE);
                    reFresh();
                } else {
                    JOptionPane.showMessageDialog(null, "添加失败！", "添加失败", JOptionPane.ERROR_MESSAGE);
                }
            }
        else {
            JOptionPane.showMessageDialog(null, "姓名不能为空！", "请重新输入数据", JOptionPane.ERROR_MESSAGE);
        }
        }

    private void reFresh() {
        guestTablePanel.removeAll();
        Object[][] data = getTableData();
        if (data.length == 0) {
            JOptionPane.showMessageDialog(null, "查询结果为空！", "提示", JOptionPane.INFORMATION_MESSAGE);
        } else {
            guestTablePanel.add(createTable());
        }
        guestTablePanel.revalidate();
        guestTablePanel.repaint();

    }
    private Object[][] getTableData() {
        JSONObject jsonObject = getData();
        if (jsonObject == null) {
            return new Object[0][0];
        }
        JSONArray jsonArray = jsonObject.getJSONArray("data");
        Object[][] tableData = new Object[jsonArray.length()][2];
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONArray item = jsonArray.getJSONArray(i); // 将元素视为 JSONArray 而不是 JSONObject
            tableData[i][0] = item.getInt(0); // 获取元组的第一个元素作为 guest_id
            tableData[i][1] = item.getString(1); // 获取元组的第二个元素作为 guest_name
        }
        return tableData;
    }

    private JSONObject getData() {
        getGuestInfo=new GetGuestInfo();
        return getGuestInfo.getguesttable();

    }
    private void initMap() {
        columnMap.put("guest_id", "工号");
        columnMap.put("name", "姓名");
    }
}
