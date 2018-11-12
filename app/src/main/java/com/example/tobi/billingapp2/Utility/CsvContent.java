package com.example.tobi.billingapp2.Utility;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.tobi.billingapp2.Activities.BluetoothPrinter;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class CsvContent implements Serializable {

    ArrayList<String[]> rowList;
    String[][] header;
    String[][] body;

    public void read(String path) throws FileNotFoundException {
        InputStream inputStream = new FileInputStream(path);
        CSVFileReader reader = new CSVFileReader(inputStream);
        reader.read();
        header = reader.content.header;
        body = reader.content.body;
    }

    public Integer indexOfName(String name) {
        int index = -1;
        for (int i=0;i<header[0].length;i++) {
            if (header[0][i]
                    .equals(name)) {
                index = i;
                break;
            }
            }
            return index;
    }

    public String[][] getBody(){
        return body;
    }

    public String[][] getHeader(){
        return header;
    }

    public void fromArrayList(ArrayList<String[]> rowList) {
        Integer numColumns = rowList.get(0).length;
        String[][] header = new String[1][numColumns];
        header[0] = rowList.get(0);//first line of the csv is the header
        rowList.remove(0);//drop the header
        String[][] body = new String[rowList.size()][numColumns];
        for (int i = 0; i < rowList.size(); i++) {
            body[i] = rowList.get(i);
        }
        this.rowList = rowList;
        this.body = body;
        this.header = header;
    }

    //puts the csv table in a Table View
    public void fill_table_with(TableLayout tbl,Context context,String[][] header, String[][] body) {
        tbl.removeAllViews();
        String[] headerCells = header[0];//header contains ónly one row
        TableRow headerRow = new TableRow(context);
        for (int cell_index=0; cell_index < headerCells.length;cell_index++)  {
            TextView cell = new TextView(context);
            // set the text to cell"
            cell.setText(" " + headerCells[cell_index] + " ");
            // add the TextView and the CheckBox to the new TableRow
            headerRow.addView(cell);
        }
        // add the headerRow to the TableLayout
        tbl.addView(headerRow,new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT));

        for (int row_index =0;row_index < body.length;row_index++) {
            TableRow row = new TableRow(context);
            String[] cells = body[row_index];
            for (int cell_index=0; cell_index < cells.length;cell_index++)  {
                TextView cell = new TextView(context);
                // set the text to cell"
                cell.setText(" " + cells[cell_index] + " ");
                // add the TextView and the CheckBox to the new TableRow
                row.addView(cell);
            }
            // add the TableRow to the TableLayout
            tbl.addView(row,new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT));
        }
    }


    public void fill_table(TableLayout tbl,Context context) {
        fill_table_with(tbl,context,this.header,this.body);
    }



    public ArrayList<Row> customerContent(String on, Integer indexOfOn) {
        ArrayList<Row> customerContent = new ArrayList<>();
        for (int i=0;i<body.length;i++){
            if (body[i][indexOfOn].equals(on) ) {
                Row row = new Row();
                row.apply(i,body[i]);//remember the indes
                customerContent.add(row);
            }
        }
//        return customerContent.toArray(new Row[customerContent.size()]);
        return customerContent;
    }

    public void updateBody(ArrayList<Row> customerContent) {
        for(Row row: customerContent){
            if(row.index == -1) {
                this.rowList.add(row.row);
            }
            else {
            this.rowList.set(row.index, row.row);
            }
        }
        fromArrayList(this.rowList);
    }

    public ArrayList<Row> updateProductCount(ArrayList<Row> customerContent,Integer row,Integer add) {
        Integer column = indexOfName("Menge");
        Integer currentValue = Integer.parseInt(customerContent.get(row).row[column]);
        customerContent.get(row).row[column] = (currentValue + add) + "";
        return customerContent;
    }

    String[] customerColumnNames = {"Artikelname", "Menge", "Preis", "Netto Preis"
            //"Kundennummer","Kunde","Article name","Preis netto","Mwst. %","Brutto","Quantity"
    };

    public String[] newProductRow(String productName) {//, HashMap<String,String> prices,HashMap<String,String> netPrices) {
        String[] row = {productName,"1","10000","10000"};
        return row;
    }

    public ArrayList<Row> addProductRow(ArrayList<Row> customerContent,String productName){//}, HashMap<String,String> prices,HashMap<String,String> netPrices) {
        Row newRow = new Row();
        //newRow.apply(-1,newProductRow(productName));//,prices,netPrices));
        newRow.apply(-1,header[0]);
        customerContent.add(newRow);
        return customerContent;
    }

    public void fillTableWithCustomerContent(final ArrayList<Row> customerContent, final TableLayout tbl, final Context context) {
        tbl.removeAllViews();
        TableRow printButton = new TableRow(context);
        TextView printCell = new TextView(context);
        printCell.setText("Print bill");
        printButton.addView(printCell);
        tbl.addView(printButton,new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT,
                TableLayout.LayoutParams.WRAP_CONTENT));
        TableRow headerRow = new TableRow(context);
        TextView addCell = new TextView(context);
        addCell.setText("add product ");
        headerRow.addView(addCell);
        for (String colName : customerColumnNames) {
            TextView cell = new TextView(context);
            cell.setText(" " + colName + " ");
            headerRow.addView(cell);
        }
        TextView substractCell = new TextView(context);
        substractCell.setText(" remove product");
        headerRow.addView(substractCell);
        tbl.addView(headerRow,new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT,
                TableLayout.LayoutParams.WRAP_CONTENT));
        for (int row_index = 0;row_index < customerContent.size();row_index++) {
            final TableRow row = new TableRow(context);
            Button add_button = new Button(context);
            add_button.setText("+");
            add_button.setTextColor(Color.GREEN);
            add_button.setWidth(5);
            add_button.setHeight(10);
            final Integer finalIndex = row_index;
            add_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final ArrayList<Row> finalCustomerContent = updateProductCount(customerContent,finalIndex,1);
                    fillTableWithCustomerContent(finalCustomerContent,tbl,context);
                }
            });
            row.addView(add_button);

            Row cusomterRow = customerContent.get(row_index);


            for (String colName : customerColumnNames) {
                TextView cell = new TextView(context);
                cell.setText(" " + cusomterRow.row[indexOfName(colName)] + " ");
                row.addView(cell);
            }

            Button substract_button = new Button(context);
            substract_button.setText("-");
            substract_button.setTextColor(Color.RED);
            substract_button.setWidth(5);
            substract_button.setHeight(10);
            substract_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final ArrayList<Row> finalCustomerContent = updateProductCount(customerContent,finalIndex,-1);
                    fillTableWithCustomerContent(finalCustomerContent,tbl,context);
                }
            });
            row.addView(substract_button);
            // add the TableRow to the TableLayout
            tbl.addView(row,new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT,
                    TableLayout.LayoutParams.WRAP_CONTENT));
        }
    }

}
