package org.xersys.purchasing.pojo;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.Objects;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import org.json.simple.JSONObject;
import org.xersys.commander.iface.XEntity;

@Entity
@Table(name="PO_Receiving_Detail")

public class PO_Receiving_Detail implements Serializable, XEntity {
    @Id
    @Basic(optional = false)
    @Column(name = "sTransNox")
    private String sTransNox;
        
    @Column(name = "nEntryNox")
    private int nEntryNox;
    
    @Column(name = "sOrderNox")
    private String sOrderNox;
    
    @Column(name = "sStockIDx")
    private String sStockIDx;
    
    @Column(name = "sReplacID")
    private String sReplacID;
    
    @Column(name = "cUnitType")
    private String cUnitType;
    
    @Column(name = "nQuantity")
    private int nQuantity;
    
    @Column(name = "nUnitPrce")
    private Number nUnitPrce;
    
    @Column(name = "nFreightx")
    private Number nFreightx;

    
    LinkedList laColumns = null;
    
    public PO_Receiving_Detail(){
        laColumns = new LinkedList();
        laColumns.add("sTransNox");
        laColumns.add("nEntryNox");
        laColumns.add("sStockIDx");
        laColumns.add("nQuantity");
        laColumns.add("nUnitPrce");
        laColumns.add("nReceived");
        laColumns.add("nCancelld");
        
        sTransNox = "";
        nEntryNox = -1;
        sOrderNox = "";
        sStockIDx = "";
        sReplacID = "";
        cUnitType = "";
        nQuantity = 0;
        nUnitPrce = 0.00;
        nFreightx = 0.00;
    }
    
    @Override
    public boolean equals(Object object) {
        if (!(object instanceof PO_Receiving_Detail)) return false;
        
        PO_Receiving_Detail other = (PO_Receiving_Detail) object;
        
        return !((sTransNox == null && other.sTransNox != null) || 
                (sTransNox != null && !sTransNox.equals(other.sTransNox))) &&
                nEntryNox != other.nEntryNox;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + Objects.hashCode(sTransNox);
        hash = 47 * hash + nEntryNox;
        return hash;
    }
    
    @Override
    public String toString() {
        return this.getClass().getName() + "[sTransNox=" + sTransNox + ", nEntryNox=" + nEntryNox + "]";
    }
    
    @Override
    public Object getValue(int fnColumn) {
        switch(fnColumn){
            case 1: return sTransNox;
            case 2: return nEntryNox;
            case 3: return sOrderNox;
            case 4: return sStockIDx;
            case 5: return sReplacID;
            case 6: return cUnitType;
            case 7: return nQuantity;
            case 8: return nUnitPrce;
            case 9: return nFreightx;
            default: return null;
        }
    }

    @Override
    public Object getValue(String fsColumn) {
        int lnCol = getColumn(fsColumn);
        
        if (lnCol > 0){
            return getValue(lnCol);
        } else
            return null;
    }

    @Override
    public String getColumn(int fnCol) {
        if (laColumns.size() < fnCol){
            return "";
        } else 
            return (String) laColumns.get(fnCol - 1);
    }

    @Override
    public int getColumn(String fsCol) {
        return laColumns.indexOf(fsCol) + 1;
    }

    @Override
    public void setValue(int fnColumn, Object foValue) {
        switch(fnColumn){
            case 1: sTransNox = (String) foValue; break;
            case 2: nEntryNox = Integer.parseInt(String.valueOf(foValue)); break;
            case 3: sOrderNox = (String) foValue; break;
            case 4: sStockIDx = (String) foValue; break;
            case 5: sReplacID = (String) foValue; break;
            case 6: cUnitType = (String) foValue; break;
            case 7: nQuantity = Integer.parseInt(String.valueOf(foValue)); break;
            case 8: nUnitPrce = (Number) foValue; break;
            case 9: nFreightx = (Number) foValue; break;
        }     
    }

    @Override
    public void setValue(String fsColumn, Object foValue) {
        int lnCol = getColumn(fsColumn);
        if (lnCol > 0){
            setValue(lnCol, foValue);
        }
    }

    @Override
    public int getColumnCount() {
        return laColumns.size();
    }

    @Override
    public String getTable() {
        return this.getClass().getSimpleName();
    }

    @Override
    public String toJSONString() {
        JSONObject loJSON = new JSONObject();
        
        for(int i = 0; i < laColumns.size(); i++){
            loJSON.put(laColumns.get(i), getValue(getColumn(i + 1)));
        }
        
        return loJSON.toJSONString();
    }

    @Override
    public void list() {
        for(int i = 0; i < laColumns.size(); i++){
            System.out.println(laColumns.get(i));
        }
    }
}
