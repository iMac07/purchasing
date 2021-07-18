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
@Table(name="PO_Receiving_Master")

public class PO_Receiving_Master implements Serializable, XEntity {
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
    
    @Column(name = "sSupplier")
    private String sSupplier;
    
    @Column(name = "sReferNox")
    private String sReferNox;
    
    @Basic(optional = false)
    @Column(name = "dRefernce")
    @Temporal(TemporalType.DATE)
    private Date dRefernce;
    
    @Column(name = "sTermCode")
    private String sTermCode;
    
    @Column(name = "nTranTotl")
    private Number nTranTotl;
    
    @Column(name = "nVATRatex")
    private Number nVATRatex;
    
    @Column(name = "nTWithHld")
    private Number nTWithHld;
    
    @Column(name = "nDiscount")
    private Number nDiscount;
    
    @Column(name = "nAddDiscx")
    private Number nAddDiscx;
    
    @Column(name = "nAmtPaidx")
    private Number nAmtPaidx;
    
    @Column(name = "nFreightx")
    private Number nFreightx;
    
    @Column(name = "sRemarksx")
    private String sRemarksx;
    
    @Column(name = "sSourceNo")
    private String sSourceNo;
    
    @Column(name = "sSourceCd")
    private String sSourceCd;
    
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
    
    public PO_Receiving_Master(){
        laColumns = new LinkedList();
        laColumns.add("sTransNox");
        laColumns.add("sBranchCd");
        laColumns.add("dTransact");
        laColumns.add("sCompnyID");
        laColumns.add("sSupplier");
        laColumns.add("sReferNox");
        laColumns.add("dRefernce");
        laColumns.add("sTermCode");
        laColumns.add("nTranTotl");
        laColumns.add("nVATRatex");
        laColumns.add("nTWithHld");
        laColumns.add("nDiscount");
        laColumns.add("nAddDiscx");
        laColumns.add("nAmtPaidx");
        laColumns.add("nFreightx");
        laColumns.add("sRemarksx");
        laColumns.add("sSourceNo");
        laColumns.add("sSourceCd");
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
        sSupplier = "";
        sReferNox = "";
        sTermCode = "";
        nTranTotl = 0.00;
        nVATRatex = 0.00;
        nTWithHld = 0.00;
        nDiscount = 0.00;
        nAddDiscx = 0.00;
        nAmtPaidx = 0.00;
        nFreightx = 0.00;
        sRemarksx = "";
        sSourceNo = "";
        sSourceCd = "";
        nEntryNox = -1;
        sInvTypCd = "";
        cTranStat = TransactionStatus.STATE_OPEN;
        sPrepared = "";
        sApproved = "";
        sAprvCode = "";
        sPostedxx = "";
    }
    
    @Override
    public boolean equals(Object object) {
        if (!(object instanceof PO_Receiving_Master)) return false;
        
        PO_Receiving_Master other = (PO_Receiving_Master) object;
        
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
            case 5: return sSupplier;
            case 6: return sReferNox;
            case 7: return dRefernce;
            case 8: return sTermCode;
            case 9: return nTranTotl;
            case 10: return nVATRatex;
            case 11: return nTWithHld;
            case 12: return nDiscount;
            case 13: return nAddDiscx;
            case 14: return nAmtPaidx;
            case 15: return nFreightx;
            case 16: return sRemarksx;
            case 17: return sSourceNo;
            case 18: return sSourceCd;
            case 19: return nEntryNox;
            case 20: return sInvTypCd;
            case 21: return cTranStat;
            case 22: return sPrepared;
            case 23: return dPrepared;
            case 24: return sApproved;
            case 25: return dApproved;
            case 26: return sAprvCode;
            case 27: return sPostedxx;
            case 28: return dPostedxx;
            case 29: return dCreatedx;
            case 30: return dModified;
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
            case 5: sSupplier = (String) foValue; break;
            case 6: sReferNox = (String) foValue; break;
            case 7: dRefernce = (Date) foValue; break;
            case 8: sTermCode = (String) foValue; break;
            case 9: nTranTotl = (Number) foValue; break;
            case 10: nVATRatex = (Number) foValue; break;
            case 11: nTWithHld = (Number) foValue; break;
            case 12: nDiscount = (Number) foValue; break;
            case 13: nAddDiscx = (Number) foValue; break;
            case 14: nAmtPaidx = (Number) foValue; break;
            case 15: nFreightx = (Number) foValue; break;
            case 16: sRemarksx = (String) foValue; break;
            case 17: sSourceNo = (String) foValue; break;
            case 18: sSourceCd = (String) foValue; break;
            case 19: nEntryNox = Integer.parseInt(String.valueOf(foValue)); break;
            case 20: sInvTypCd = (String) foValue; break;
            case 21: cTranStat = (String) foValue; break;
            case 22: sPrepared = (String) foValue; break;
            case 23: dPrepared = (Date) foValue; break;
            case 24: sApproved = (String) foValue; break;
            case 25: dApproved = (Date) foValue; break;
            case 26: sAprvCode = (String) foValue; break;
            case 27: sPostedxx = (String) foValue; break;
            case 28: dPostedxx = (Date) foValue; break;
            case 29: dCreatedx = (Date) foValue; break;
            case 30: dModified = (Date) foValue; break;
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
