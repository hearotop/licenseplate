package org.license.UI;

import org.json.JSONArray;
import org.json.JSONObject;
import org.license.utils.RegisterInfo;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;

public class RegisterPanel {
    private JPanel registerPanel;
    private RegisterInfo registerInfo;
    private JButton addAllRecordButton;
    private JPanel buttonPanel;
    private JPanel jTablePanel;
    private JTable table;
    private Map<String, String> columnMap;
    private JButton refrshButton;
    private JTextField searchField;
    private JButton deleteButton;
    private String carCode;
    private JPanel searchPanel;
    private JButton searchButton;
    private JButton updateRecordButton;

    public RegisterPanel() {
        columnMap = new HashMap<>(); // 初始化 columnMap
        initMap();
        registerInfo = new RegisterInfo(); // 初始化 registerInfo
        registerPanel = new JPanel(new BorderLayout());
        registerPanel.setBorder(BorderFactory.createTitledBorder("外来车牌进出登记管理(默认30分钟内)"));
        // 初始化 jTablePanel
        jTablePanel = new JPanel(new BorderLayout());
        searchButton = new JButton("查询记录");
        searchButton.addActionListener(e -> {
            carCode = searchField.getText();
            registerInfo.setCarCode(carCode);
            reFresh();
            carCode = "";
            registerInfo.setCarCode(carCode);
        });
        searchPanel = new JPanel(new BorderLayout());
        searchField = new JTextField(10);
        searchPanel.add(searchField);
        searchPanel.add(searchButton, BorderLayout.EAST);
        registerPanel.add(searchPanel, BorderLayout.NORTH);
        jTablePanel.add(createTable(), BorderLayout.CENTER);
        // 初始化 buttonPanel
        buttonPanel = new JPanel(new GridLayout(1, 4));
        updateRecordButton = new JButton("登记记录");
        updateRecordButton.addActionListener(e -> updateTable());
        addAllRecordButton = new JButton("添加记录");
        addAllRecordButton.addActionListener(e -> showAddRecordDialog());
        deleteButton = new JButton("删除记录");
        refrshButton = new JButton("刷新记录");
        buttonPanel.add(updateRecordButton);
        buttonPanel.add(addAllRecordButton);
        buttonPanel.add(deleteButton);
        deleteButton.addActionListener(e -> deleteTable());
        buttonPanel.add(refrshButton);
        // 添加组件到 registerPanel
        refrshButton.addActionListener(e -> reFresh());
        registerPanel.add(jTablePanel, BorderLayout.CENTER);
        registerPanel.add(buttonPanel, BorderLayout.SOUTH);
    }

    private void deleteTable() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            int recordid = (int) table.getValueAt(selectedRow, 0);

            int result = JOptionPane.showConfirmDialog(null, "确定删除吗？", "确认删除", JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.YES_OPTION) {
                boolean deleteFlag = registerInfo.deleteRegister(recordid);
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

    private void reFresh() {
        jTablePanel.removeAll();
        Object[][] data = getTableData();
        if (data.length == 0) {
            JOptionPane.showMessageDialog(null, "查询结果为空！", "提示", JOptionPane.INFORMATION_MESSAGE);
            jTablePanel.add(createTable());
        } else {
            jTablePanel.add(createTable());
        }
        jTablePanel.revalidate();
        jTablePanel.repaint();
    }

    public JPanel getRegisterPanel() {
        return registerPanel;
    }

    private JScrollPane createTable() {
        String[] columnNames = {"编号", "车牌号", "登记时间", "是否本单位", "进/出", "手机号", "姓名"};
        Object[][] data = getTableData();
        DefaultTableModel tableModel = new DefaultTableModel(data, columnNames);
        table = new JTable(tableModel);

        // 为“是否本单位”字段添加下拉列表
        JComboBox<String> isInsideComboBox = new JComboBox<>(new String[]{"是", "否"});
        TableColumn isInsideColumn = table.getColumnModel().getColumn(3);
        isInsideColumn.setCellEditor(new DefaultCellEditor(isInsideComboBox));

        // 为“进/出”字段添加下拉列表
        JComboBox<String> flagComboBox = new JComboBox<>(new String[]{"进", "出"});
        TableColumn flagColumn = table.getColumnModel().getColumn(4);
        flagColumn.setCellEditor(new DefaultCellEditor(flagComboBox));

        // 禁止拖动表格中的列和行
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                    int selectedRow = table.getSelectedRow();
                    int recordid = (int) table.getValueAt(selectedRow, 0);
                    String tel = (String) table.getValueAt(selectedRow, 5);
                    String name = (String) table.getValueAt(selectedRow, 6);

                    // 处理选中的行，例如显示详细信息或进行其他操作
                }
            }
        });
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setResizingAllowed(false);

        return new JScrollPane(table);
    }
    private JSONObject getData() {
        return registerInfo.getregistertable();
    }

    private Object[][] getTableData() {
        JSONObject jsonObject = getData();
        if (jsonObject == null) {
            return new Object[0][0];
        }

        JSONArray jsonArray = jsonObject.getJSONArray("data");
        Object[][] tableData = new Object[jsonArray.length()][7];

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject item = jsonArray.getJSONObject(i);
            tableData[i][0] = item.getInt("record_id");
            tableData[i][1] = item.getString("license_plate");
            tableData[i][2] = item.getString("times");
            tableData[i][3] = item.isNull("is_inside") ? "" : item.getString("is_inside");
            tableData[i][4] = item.isNull("flag") ? "" : item.getString("flag");
            tableData[i][5] = item.isNull("tel") ? "" : item.getString("tel");
            tableData[i][6] = item.isNull("name") ? "" : item.getString("name");
        }

        return tableData;
    }

    private void updateTable() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            Object recordIDObject = table.getValueAt(selectedRow, 0);
            Object licensePlatesObject = table.getValueAt(selectedRow, 1);
            Object timesObject = table.getValueAt(selectedRow, 2);
            Object isInsideObject = table.getValueAt(selectedRow, 3);
            Object flagObject = table.getValueAt(selectedRow, 4);
            Object telObject = table.getValueAt(selectedRow, 5);
            Object nameObject = table.getValueAt(selectedRow, 6);

            // Check types and convert if necessary
            if (!(recordIDObject instanceof Integer) || !(licensePlatesObject instanceof String) ||
                    !(timesObject instanceof String) || !(isInsideObject instanceof String) ||
                    !(flagObject instanceof String) || !(telObject instanceof String) ||
                    !(nameObject instanceof String)) {
                JOptionPane.showMessageDialog(null, "数据类型错误！", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int recordID = (Integer) recordIDObject;
            String licensePlates = (String) licensePlatesObject;
            String times = (String) timesObject;
            int isInside;
            if (isInsideObject.equals("是")) isInside = 1;
            else isInside = 0;
            int flag;
            if (flagObject.equals("进")) flag = 1;
            else flag = 0;
            String tel = (String) telObject;
            String name = (String) nameObject;

            boolean updateSuccess = registerInfo.updateregisterinfo(recordID, licensePlates, times, isInside, flag, tel, name);

            if (updateSuccess) {
                JOptionPane.showMessageDialog(null, "修改成功！", "修改成功", JOptionPane.INFORMATION_MESSAGE);
                reFresh();
            } else {
                JOptionPane.showMessageDialog(null, "修改失败！", "修改失败", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(null, "请选择一行数据！", "修改错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showAddRecordDialog() {
        String[] columnNames = {"车牌号", "登记时间", "是否本单位", "进/出", "手机号", "姓名"};
        Object[][] data = new Object[1][6];
        DefaultTableModel model = new DefaultTableModel(data, columnNames);
        JTable inputTable = new JTable(model);

        // 为登记时间字段添加鼠标监听器
        inputTable.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(new JTextField()) {
            @Override
            public boolean isCellEditable(EventObject e) {
                return true;
            }

            @Override
            public boolean stopCellEditing() {
                JTextField textField = (JTextField) getComponent();
                textField.setText(getCurrentTime());
                return super.stopCellEditing();
            }
        });

        // 为“是否本单位”字段添加下拉列表
        JComboBox<String> isInsideComboBox = new JComboBox<>(new String[]{"是", "否"});
        inputTable.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(isInsideComboBox));

        // 为“进/出”字段添加下拉列表
        JComboBox<String> flagComboBox = new JComboBox<>(new String[]{"进", "出"});
        inputTable.getColumnModel().getColumn(3).setCellEditor(new DefaultCellEditor(flagComboBox));

        int result = JOptionPane.showConfirmDialog(null, new JScrollPane(inputTable), "添加新记录", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String licensePlate = (String) inputTable.getValueAt(0, 0);
            String times = (String) inputTable.getValueAt(0, 1);
            String isInsideStr = (String) inputTable.getValueAt(0, 2);
            String flagStr = (String) inputTable.getValueAt(0, 3);
            String tel = (String) inputTable.getValueAt(0, 4);
            String name = (String) inputTable.getValueAt(0, 5);

            if (licensePlate == null || licensePlate.isEmpty() || times == null || times.isEmpty() ||
                    isInsideStr == null || isInsideStr.isEmpty() || flagStr == null || flagStr.isEmpty() ||
                    tel == null || tel.isEmpty() || name == null || name.isEmpty()) {
                JOptionPane.showMessageDialog(null, "所有字段都是必填项！", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int isInside = isInsideStr.equals("是") ? 1 : 0;
            int flag = flagStr.equals("进") ? 1 : 0;

            boolean addSuccess = registerInfo.registerinfo(licensePlate, times, isInside, flag, tel, name);
            if (addSuccess) {
                JOptionPane.showMessageDialog(null, "添加成功！", "添加成功", JOptionPane.INFORMATION_MESSAGE);
                reFresh();
            } else {
                JOptionPane.showMessageDialog(null, "添加失败！", "添加失败", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    private String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date());
    }
    private void initMap() {
        columnMap.put("record_id", "编号");
        columnMap.put("license_plates", "车牌号");
        columnMap.put("times", "登记时间");
        columnMap.put("is_inside", "是否本单位");
        columnMap.put("flag", "进/出");
        columnMap.put("tel", "手机号");
        columnMap.put("name", "姓名");
    }
}
