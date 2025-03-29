package org.license.UI;

import org.json.JSONArray;
import org.json.JSONObject;
import org.license.utils.GetCarInfo;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class CardPanel {
    private Map<String, String> columnMap = new HashMap<>();
    private GetCarInfo carInfo;
    private JTable table;
    private JPanel cardTablePanel = new JPanel(new BorderLayout());
    private JTextField searchField;
    private JButton searchButton;
    private String carCode;
    private JPanel cardPanel;

    public CardPanel() {
        carInfo = new GetCarInfo();
        initMap();
    }

    public JPanel cardPanel() {
        cardPanel = new JPanel(new BorderLayout());
        cardPanel.setBorder(BorderFactory.createTitledBorder("车牌录入管理"));

        JPanel buttonPanel = new JPanel(new GridLayout(1, 4));
        JButton addCardButton = new JButton("添加车牌");
        JButton updateCardButton = new JButton("修改车牌");
        JButton deleteCardButton = new JButton("删除车牌");
        JButton refreshButton = new JButton("刷新信息");

        buttonPanel.add(addCardButton);
        buttonPanel.add(updateCardButton);
        buttonPanel.add(deleteCardButton);
        buttonPanel.add(refreshButton);

        refreshButton.addActionListener(e -> reFresh());
        updateCardButton.addActionListener(e -> updateTable());
        deleteCardButton.addActionListener(e -> deleteTable());
        addCardButton.addActionListener(e -> addTable());

        searchButton = new JButton("查询车牌");
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchField = new JTextField(10);

        searchButton.addActionListener(e -> {
            carCode = searchField.getText();
            carInfo.setCarCode(carCode);

            reFresh();
            carCode="";
            carInfo.setCarCode(carCode);
        });

        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchButton, BorderLayout.EAST);

        cardPanel.add(searchPanel, BorderLayout.NORTH);
        cardTablePanel.add(createTable());
        cardPanel.add(cardTablePanel, BorderLayout.CENTER);
        cardPanel.add(buttonPanel, BorderLayout.SOUTH);

        return cardPanel;
    }

    private JScrollPane createTable() {
        String[] columnNames = {columnMap.get("car_id"), columnMap.get("license_plates"),columnMap.get("name")};
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
                    int carId = (int) table.getValueAt(selectedRow, 0);
                    String licensePlates = (String) table.getValueAt(selectedRow, 1);
                    // 处理选中的行，例如显示详细信息或进行其他操作
                }
            }
        });

        return new JScrollPane(table);
    }

    private void deleteTable() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            int carId = (int) table.getValueAt(selectedRow, 0);
            int result = JOptionPane.showConfirmDialog(null, "确定删除吗？", "确认删除", JOptionPane.YES_NO_OPTION);

            if (result == JOptionPane.YES_OPTION) {
                boolean deleteFlag = carInfo.deleteCarCode(carId);
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
            Object carIdObject = table.getValueAt(selectedRow, 0);
            Object licensePlatesObject = table.getValueAt(selectedRow, 1);

            if (carIdObject instanceof Integer && licensePlatesObject instanceof String) {
                int carId = (Integer) carIdObject;
                String licensePlates = (String) licensePlatesObject;
                boolean updateSuccess = carInfo.updatecarinfo(licensePlates, carId);

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
        String newLicensePlates = JOptionPane.showInputDialog("输入车牌号:");
       int newGuestIds = Integer.parseInt(JOptionPane.showInputDialog("输入工号:"));
        carInfo.setGuestId(newGuestIds);
        if (newLicensePlates != null) {
            if (newLicensePlates.length() == 7) {
                boolean addFlag = carInfo.addcarinfo(newLicensePlates, carInfo.getGuestId());
                if (addFlag) {
                    JOptionPane.showMessageDialog(null, "添加成功！", "添加成功", JOptionPane.INFORMATION_MESSAGE);
                    reFresh();
                } else {
                    JOptionPane.showMessageDialog(null, "添加失败！", "添加失败", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(null, "车牌应等于7位！", "请重新输入数据", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void reFresh() {
        cardTablePanel.removeAll();
        Object[][] data = getTableData();
        if (data.length == 0) {
            JOptionPane.showMessageDialog(null, "查询结果为空！", "提示", JOptionPane.INFORMATION_MESSAGE);
        } else {
            cardTablePanel.add(createTable());
        }
        cardTablePanel.revalidate();
        cardTablePanel.repaint();

    }

    private Object[][] getTableData() {
        JSONObject jsonObject = getData();
        if (jsonObject == null) {
            return new Object[0][0];
        }
        JSONArray jsonArray = jsonObject.getJSONArray("data");
        Object[][] tableData = new Object[jsonArray.length()][3];
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject item = jsonArray.getJSONObject(i);
            tableData[i][0] = item.getInt("car_id");
            tableData[i][1] = item.getString("license_plates");
            tableData[i][2] = item.getString("guest_name");
        }
        return tableData;
    }

    private JSONObject getData() {
        return carInfo.getcartable();
    }

    private void initMap() {
        columnMap.put("car_id", "编号");
        columnMap.put("license_plates", "车牌号");
        columnMap.put("name", "姓名");
    }
}
