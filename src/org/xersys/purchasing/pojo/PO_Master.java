package org.xersys.purchasing.pojo;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedList;
import java.util.Objects;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import org.json.simple.JSONObject;
import org.xersys.commander.contants.TransactionStatus;
import org.xersys.commander.iface.XEntity;
import org.xersys.commander.util.SQLUtil;

@Entity
@Table(name="PO_Master")

public class PO_Master implements Serializable, XEntity {
    @Id
    @Basic(optional = false)
    @Column(name = "sTransNox")
    private String sTransNox;
    
    @Column(name = "sBranchCd")
    private String sBranchCd;
    
    @Column(name = "dTransact")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dTransact;
    
    @Column(name = "sCompnyID")
    private String sCompnyID;
    
    @Column(name = "sDestinat")
    private String sDestinat;
    
    @Column(name = "sSupplier")
    private String sSupplier;
    
    @Column(name = "sReferNox")
    private String sReferNox;
    
    @Column(name = "sTermCode")
    private String sTermCode;
    
    @Column(name = "nTranTotl")
    private Number nTranTotl;
    
    @Column(name = "sRemarksx")
    private String sRemarksx;
    
    @Column(name = "sSourceNo")
    private String sSourceNo;
    
    @Column(name = "sSourceCd")
    private String sSourceCd;
     
    @Column(name = "cEmailSnt")
    private String cEmailSnt;
    
    @Column(name = "nEmailSnt")
    private int nEmailSnt;
    
    @Column(name = "nEntryNox")
    private int nEntryNox;
    
    @Column(name = "sInvTypCd")
    private String sInvTypCd;
    
    @Column(name = "cTranStat")
    private String cTranStat;
    
    @Column(name = "sPrepared")
    private String sPrepared;
    
    @Basic(optional = false)
    @Column(name = "dPrepared")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dPrepared;
    
    @Column(name = "sApproved")
    private String sApproved;
    
    @Basic(optional = false)
    @Column(name = "dApproved")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dApproved;
    
    @Column(name = "sAprvCode")
    private String sAprvCode;    
    
    @Column(name = "sPostedxx")
    private String sPostedxx;
    
    @Basic(optional = false)
    @Column(name = "dPostedxx")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dPostedxx;
    
    @Basic(optional = false)
    @Column(name = "dCreatedx")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dCreatedx;
    
    @Basic(optional = false)
    @Column(name = "dModified")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dModified;
    
    LinkedList laColumns = null;
    
    public PO_Master(){
        laColumns = new LinkedList();
        laColumns.add("sTransNox");
        laColumns.add("sBranchCd");
        laColumns.add("dTransact");
        laColumns.add("sCompnyID");
        laColumns.add("sDestinat");
        laColumns.add("sSupplier");
        laColumns.add("sReferNox");
        laColumns.add("sTermCode");
        laColumns.add("nTranTotl");
        laColumns.add("sRemarksx");
        laColumns.add("sSourceNo");
        laColumns.add("sSourceCd");
        laColumns.add("cEmailSnt");
        laColumns.add("nEmailSnt");
        laColumns.add("nEntryNox");
        laColumns.add("sInvTypCd");
        laColumns.add("cTranStat");
        laColumns.add("sPrepared");
        laColumns.add("dPrepared");
        laColumns.add("sApproved");
        laColumns.add("dApproved");
        laColumns.add("sAprvCode");
        laColumns.add("sPostedxx");
        laColumns.add("dPostedxx");
        laColumns.add("dCreatedx");
        laColumns.add("dModified");
        
        sTransNox = "";
        sBranchCd = "";
        sCompnyID = "";
        sDestinat = "";
        sSupplier = "";
        sReferNox = "";
        sTermCode = "";
        nTranTotl = 0.00;
        sRemarksx = "";
        sSourceNo = "";
        sSourceCd = "";
        cEmailSnt = "0";
        nEmailSnt = 0;
        nEntryNox = 0;
        sInvTypCd = "";
        cTranStat = TransactionStatus.STATE_OPEN;
        sPrepared = "";
        sApproved = "";
        sAprvCode = "";
        sPostedxx = "";
    }
    
    @Override
    public boolean equals(Object object) {
        if (!(object instanceof PO_Master)) return false;
        
        PO_Master other = (PO_Master) object;
        
        return !((this.sTransNox == null && other.sTransNox != null) || 
                (this.sTransNox != null && !this.sTransNox.equals(other.sTransNox)));
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + Objects.hashCode(this.sTransNox);
        return hash;
    }

    @Override
    public String toString() {
        return this.getClass().getName() + "[sTransNox=" + sTransNox + "]";
    }
    
    @Override
    public Object getValue(int fnColumn) {
        switch(fnColumn){
            case 1: return sTransNox;
            case 2: return sBranchCd;
            case 3: return dTransact;
            case 4: return sCompnyID;
            case 5: return sDestinat;
            case 6: return sSupplier;
            case 7: return sReferNox;
            case 8: return sTermCode;
            case 9: return nTranTotl;
            case 10: return sRemarksx;
            case 11: return sSourceNo;
            case 12: return sSourceCd;
            case 13: return cEmailSnt;
            case 14: return nEmailSnt;
            case 15: return nEntryNox;
            case 16: return sInvTypCd;
            case 17: return cTranStat;
            case 18: return sPrepared;
            case 19: return dPrepared;
            case 20: return sApproved;
            case 21: return dApproved;
            case 22: return sAprvCode;
            case 23: return sPostedxx;
            case 24: return dPostedxx;
            case 25: return dCreatedx;
            case 26: return dModified;
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
            case 2: sBranchCd = (String) foValue; break;
            case 3: dTransact = (Date) foValue; break;
            case 4: sCompnyID = (String) foValue; break;
            case 5: sDestinat = (String) foValue; break;
            case 6: sSupplier = (String) foValue; break;
            case 7: sReferNox = (String) foValue; break;
            case 8: sTermCode = (String) foValue; break;
            case 9: nTranTotl = (Number) foValue; break;
            case 10: sRemarksx = (String) foValue; break;
            case 11: sSourceNo = (String) foValue; break;
            case 12: sSourceCd = (String) foValue; break;
            case 13: cEmailSnt = (String) foValue; break;
            case 14: nEmailSnt = Integer.parseInt(String.valueOf(foValue)); break;
            case 15: nEntryNox = Integer.parseInt(String.valueOf(foValue)); break;
            case 16: sInvTypCd = (String) foValue; break;
            case 17: cTranStat = (String) foValue; break;
            case 18: sPrepared = (String) foValue; break;
            case 19: dPrepared = (Date) foValue; break;
            case 20: sApproved = (String) foValue; break;
            case 21: dApproved = (Date) foValue; break;
            case 22: sAprvCode = (String) foValue; break;
            case 23: sPostedxx = (String) foValue; break;
            case 24: dPostedxx = (Date) foValue; break;
            case 25: dCreatedx = (Date) foValue; break;
            case 26: dModified = (Date) foValue; break;
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
            if (getColumn(i + 1).substring(0, 1).equals("d")){
                loJSON.put(laColumns.get(i), SQLUtil.dateFormat(getValue(getColumn(i + 1)), SQLUtil.FORMAT_TIMESTAMP));
            } else 
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
