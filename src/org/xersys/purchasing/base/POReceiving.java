package org.xersys.purchasing.base;

import com.mysql.jdbc.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetFactory;
import javax.sql.rowset.RowSetProvider;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.xersys.accounts.client.APClientTrans;
import org.xersys.clients.base.APClient;
import org.xersys.clients.search.ClientSearch;
import org.xersys.commander.contants.EditMode;
import org.xersys.commander.contants.RecordStatus;
import org.xersys.commander.contants.TransactionStatus;
import org.xersys.commander.iface.LMasDetTrans;
import org.xersys.commander.iface.XMasDetTrans;
import org.xersys.commander.iface.XNautilus;
import org.xersys.commander.util.CommonUtil;
import org.xersys.commander.util.MiscUtil;
import org.xersys.commander.util.SQLUtil;
import org.xersys.commander.util.StringUtil;
import org.xersys.inventory.base.InvTrans;
import org.xersys.inventory.search.InvSearchF;
import org.xersys.lib.pojo.Temp_Transactions;
import org.xersys.parameters.search.ParamSearchF;
import org.xersys.purchasing.search.PurchasingSearch;

public class POReceiving implements XMasDetTrans{
    private final String MASTER_TABLE = "PO_Receiving_Master";
    private final String DETAIL_TABLE = "PO_Receiving_Detail";
    private final String SERIAL_TABLE = "PO_Receiving_Serial";
    private final String PACKGE_TABLE = "PO_Receiving_Package";
    private final String SOURCE_CODE = "PRec";
    private final String SYSTEM_CODE = "SP";
    
    private final XNautilus p_oNautilus;
    private final boolean p_bWithParent;
    private final String p_sBranchCd;
    
    private LMasDetTrans p_oListener;
    private boolean p_bSaveToDisk;
    
    private String p_sOrderNox;
    
    private String p_sMessagex;
    
    private int p_nEditMode;
    private int p_nTranStat;
    
    private CachedRowSet p_oMaster;
    private CachedRowSet p_oDetail;
    
    private ArrayList<Temp_Transactions> p_oTemp;
    
    private InvSearchF p_oSearchItem;
    private ClientSearch p_oSearchSupplier;
    private ParamSearchF p_oSearchTerm;
    private PurchasingSearch p_oSearchTrans;
    private PurchasingSearch p_oSearchSource;

    public POReceiving(XNautilus foNautilus, String fsBranchCd, boolean fbWithParent){
        p_oNautilus = foNautilus;
        p_sBranchCd = fsBranchCd;
        p_bWithParent = fbWithParent;
        p_nEditMode = EditMode.UNKNOWN;
        
        p_oSearchItem = new InvSearchF(p_oNautilus, InvSearchF.SearchType.searchSPInventoryWPO);
        p_oSearchSupplier = new ClientSearch(p_oNautilus, ClientSearch.SearchType.searchSupplier);
        p_oSearchTerm = new ParamSearchF(p_oNautilus, ParamSearchF.SearchType.searchTerm);
        p_oSearchTrans = new PurchasingSearch(p_oNautilus, PurchasingSearch.SearchType.searchPOReceiving);
        p_oSearchSource = new PurchasingSearch(p_oNautilus, PurchasingSearch.SearchType.searchPO);
        
        loadTempTransactions();
    }
    
    public POReceiving(XNautilus foNautilus, String fsBranchCd, boolean fbWithParent, int fnTranStat){
        p_oNautilus = foNautilus;
        p_sBranchCd = fsBranchCd;
        p_bWithParent = fbWithParent;
        p_nTranStat = fnTranStat;
        p_nEditMode = EditMode.UNKNOWN;
        
        loadTempTransactions();
    }
    
    @Override
    public void setListener(LMasDetTrans foValue) {
        p_oListener = foValue;
    }

    @Override
    public void setSaveToDisk(boolean fbValue) {
        p_bSaveToDisk = fbValue;
    }

    @Override
    public void setMaster(String fsFieldNm, Object foValue) {
        if (p_nEditMode != EditMode.ADDNEW &&
            p_nEditMode != EditMode.UPDATE){
            System.err.println("Transaction is not on update mode.");
            return;
        }
        
        try {
            p_oMaster.first();
            
            switch (fsFieldNm){
                case "dTransact":
                case "dCreatedx":
                    if (StringUtil.isDate(String.valueOf(foValue), SQLUtil.FORMAT_TIMESTAMP))
                        p_oMaster.setObject(fsFieldNm, foValue);
                    else 
                        p_oMaster.setObject(fsFieldNm, p_oNautilus.getServerDate());
                    
                    p_oMaster.updateRow();
                    break;
                case "sSupplier":
                    getSupplier("a.sClientID", foValue);
                    return;
                case "sTermCode":
                    getTerm("sTermCode", foValue);
                    return;
                case "sSourceNo":
                    getSource("sTransNox", foValue);
                    return;
                case "nVATRatex":    
                case "nTWithHld":
                case "nDiscount":
                case "nAddDiscx":
                case "nAmtPaidx":
                case "nFreightx":
                    if (StringUtil.isNumeric(String.valueOf(foValue)))
                        p_oMaster.updateObject(fsFieldNm, foValue);
                    else
                        p_oMaster.updateObject(fsFieldNm, 0.00);
                    
                    p_oMaster.updateRow();
                    break;
                default:
                    p_oMaster.updateObject(fsFieldNm, foValue);
                    p_oMaster.updateRow();
            }
            
            if (p_oListener != null) p_oListener.MasterRetreive(fsFieldNm, p_oMaster.getObject(fsFieldNm));
             
            saveToDisk(RecordStatus.ACTIVE, "");
        } catch (SQLException | ParseException e) {
            e.printStackTrace();
            setMessage(e.getMessage());
        }
    }

    @Override
    public Object getMaster(String fsFieldNm) {
        try {
            p_oMaster.first();
            
            return p_oMaster.getObject(fsFieldNm);
        } catch (SQLException e) {
            e.printStackTrace();
            setMessage(e.getMessage());
        }
        
        return null;
    }

    @Override
    public void setMaster(int fnIndex, Object foValue) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object getMaster(int fnIndex) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setDetail(int fnRow, String fsFieldNm, Object foValue) {
        if (p_nEditMode != EditMode.ADDNEW &&
            p_nEditMode != EditMode.UPDATE){
            System.err.println("Transaction is not on update mode.");
            return;
        }
        
        try {
            switch (fsFieldNm){
                case "sStockIDx":                     
                    getDetail(fnRow, "a.sStockIDx", foValue);
                    computeTotal();
                    
                    p_oMaster.first();
                    if (p_oListener != null) p_oListener.MasterRetreive("nTranTotl", p_oMaster.getObject("nTranTotl"));
                    
                    break;
                default:
                    p_oDetail.absolute(fnRow + 1);
                    p_oDetail.updateObject(fsFieldNm, foValue);
                    p_oDetail.updateRow();
                    
                    computeTotal();
                    if (p_oListener != null) p_oListener.DetailRetreive(fnRow, fsFieldNm, "");
            }
            
            saveToDisk(RecordStatus.ACTIVE, "");            
        } catch (SQLException | ParseException e) {
            e.printStackTrace();
            setMessage(e.getMessage());
        }
    }

    @Override
    public Object getDetail(int fnRow, String fsFieldNm) {
        try {
            p_oDetail.absolute(fnRow + 1);            
            return p_oDetail.getObject(fsFieldNm);
        } catch (SQLException e) {
            e.printStackTrace();
            setMessage(e.getMessage());
            return null;
        }
    }

    @Override
    public void setDetail(int fnRow, int fnIndex, Object foValue) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object getDetail(int fnRow, int fnIndex) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getMessage() {
        return p_sMessagex;
    }

    @Override
    public int getEditMode() {
        return p_nEditMode;
    }

    @Override
    public int getItemCount() {
        try {
            p_oDetail.last();
            return p_oDetail.getRow();
        } catch (SQLException e) {
            e.printStackTrace();
            setMessage(e.getMessage());
            return -1;
        }
    }

    public void setTranStat(int fnValue){
        p_nTranStat = fnValue;
    }
    
    @Override
    public boolean addDetail() {
        try {
            if (getItemCount() > 0) {
                if ("".equals((String) getDetail(getItemCount() - 1, "sStockIDx"))){
                    saveToDisk(RecordStatus.ACTIVE, "");
                    return true;
                }
            }
            
            p_oDetail.last();
            p_oDetail.moveToInsertRow();

            MiscUtil.initRowSet(p_oDetail);

            p_oDetail.insertRow();
            p_oDetail.moveToCurrentRow();
        } catch (SQLException e) {
            setMessage(e.getMessage());
            return false;
        }
        
        saveToDisk(RecordStatus.ACTIVE, "");
        return true;
    }

    @Override
    public boolean delDetail(int fnRow) {
        try {
            p_oDetail.absolute(fnRow + 1);
            p_oDetail.deleteRow();
            
            return addDetail();
        } catch (SQLException e) {
            setMessage(e.getMessage());
            return false;
        }
    }

    @Override
    public boolean NewTransaction() {
        System.out.println(this.getClass().getSimpleName() + ".NewTransaction()");
        
        p_sOrderNox = "";
        
        try {
            String lsSQL;
            ResultSet loRS;
            
            RowSetFactory factory = RowSetProvider.newFactory();
            
            //create empty master record
            lsSQL = MiscUtil.addCondition(getSQ_Master(), "0=1");
            loRS = p_oNautilus.executeQuery(lsSQL);
            p_oMaster = factory.createCachedRowSet();
            p_oMaster.populate(loRS);
            MiscUtil.close(loRS);
            addMasterRow();
            
            //create empty detail record
            lsSQL = MiscUtil.addCondition(getSQ_Detail(), "0=1");
            loRS = p_oNautilus.executeQuery(lsSQL);
            p_oDetail = factory.createCachedRowSet();
            p_oDetail.populate(loRS);
            MiscUtil.close(loRS);
            addDetail();
        } catch (SQLException ex) {
            setMessage(ex.getMessage());
            return false;
        }
        
        p_nEditMode = EditMode.ADDNEW;
        
        saveToDisk(RecordStatus.ACTIVE, "");
        loadTempTransactions();
        
        return true;
    }

    @Override
    public boolean NewTransaction(String fsOrderNox) {
        System.out.println(this.getClass().getSimpleName() + ".NewTransaction(String fsOrderNox)");
        
        if (fsOrderNox.isEmpty()) return NewTransaction();
        
        p_sOrderNox = fsOrderNox;
        
        ResultSet loTran = null;
        boolean lbLoad = false;
        
        try {
            loTran = CommonUtil.getTempOrder(p_oNautilus, SOURCE_CODE, fsOrderNox);
            
            if (loTran.next()){
                lbLoad = toDTO(loTran.getString("sPayloadx"));
            }
            
            computeTotal();
        } catch (SQLException ex) {
            setMessage(ex.getMessage());
            lbLoad = false;
        } finally {
            MiscUtil.close(loTran);
        }
        
        p_nEditMode = EditMode.ADDNEW;
        
        loadTempTransactions();
        
        return lbLoad;
    }

    @Override
    public boolean SaveTransaction(boolean fbConfirmed) {
        System.out.println(this.getClass().getSimpleName() + ".SaveTransaction()");
        
        if (p_nEditMode != EditMode.ADDNEW &&
            p_nEditMode != EditMode.UPDATE){
            System.err.println("Transaction is not on update mode.");
            return false;
        }
        
        if (!fbConfirmed){
            saveToDisk(RecordStatus.ACTIVE, "");
            return true;
        }        
        
        if (!isEntryOK()) return false;
        
        try {
            String lsSQL = "";
            
            if (!p_bWithParent) p_oNautilus.beginTrans();
        
            if ("".equals((String) getMaster("sTransNox"))){ //new record
                Connection loConn = getConnection();

                p_oMaster.updateObject("sTransNox", MiscUtil.getNextCode(MASTER_TABLE, "sTransNox", true, loConn, p_sBranchCd));
                p_oMaster.updateObject("sPrepared", (String) p_oNautilus.getUserInfo("sUserIDxx"));
                p_oMaster.updateObject("dPrepared", p_oNautilus.getServerDate());
                p_oMaster.updateObject("dModified", p_oNautilus.getServerDate());
                p_oMaster.updateRow();
                
                if (!p_bWithParent) MiscUtil.close(loConn);
                
                //save detail
                int lnCtr = 1;
                p_oDetail.beforeFirst();
                while (p_oDetail.next()){
                    if (!"".equals((String) p_oDetail.getObject("sStockIDx"))){
                        p_oDetail.updateObject("sTransNox", p_oMaster.getObject("sTransNox"));
                        p_oDetail.updateObject("nEntryNox", lnCtr);
                    
                        lsSQL = MiscUtil.rowset2SQL(p_oDetail, DETAIL_TABLE, "sBarCodex;sDescript;nQtyOnHnd;sBrandCde;sModelCde;sColorCde");

                        if(p_oNautilus.executeUpdate(lsSQL, DETAIL_TABLE, p_sBranchCd, "") <= 0){
                            if(!p_oNautilus.getMessage().isEmpty())
                                setMessage(p_oNautilus.getMessage());
                            else
                                setMessage("No record updated");

                            if (!p_bWithParent) p_oNautilus.rollbackTrans();
                            return false;
                        } 
                        lnCtr++;
                    }
                }
                
                lsSQL = MiscUtil.rowset2SQL(p_oMaster, MASTER_TABLE, "sClientNm;sTermName");
            } else { //old record
            }
            
            if (lsSQL.equals("")){
                if (!p_bWithParent) p_oNautilus.rollbackTrans();
                
                setMessage("No record to update");
                return false;
            }
            
            if(p_oNautilus.executeUpdate(lsSQL, MASTER_TABLE, p_sBranchCd, "") <= 0){
                if(!p_oNautilus.getMessage().isEmpty())
                    setMessage(p_oNautilus.getMessage());
                else
                    setMessage("No record updated");
            } 
            
            saveToDisk(RecordStatus.UNKNOWN, (String) p_oMaster.getObject("sTransNox"));

            if (!p_bWithParent) {
                if(!p_oNautilus.getMessage().isEmpty()){
                    p_oNautilus.rollbackTrans();
                    return false;
                } else
                    p_oNautilus.commitTrans();
            }    
        } catch (SQLException ex) {
            if (!p_bWithParent) p_oNautilus.rollbackTrans();
            
            ex.printStackTrace();
            setMessage(ex.getMessage());
            return false;
        }
        
        loadTempTransactions();
        
        p_oMaster = null;
        p_oDetail = null;
        p_nEditMode = EditMode.UNKNOWN;
        
        return true;
    }

    @Override
    public boolean SearchTransaction() {
        System.out.println(this.getClass().getSimpleName() + ".SearchTransaction()");        
        return true;
    }

    @Override
    public boolean OpenTransaction(String fsTransNox) {
        System.out.println(this.getClass().getSimpleName() + ".OpenTransaction()");
        setMessage("");       
        
        try {
            if (p_oMaster != null){
                p_oMaster.first();

                if (p_oMaster.getString("sTransNox").equals(fsTransNox)){
                    p_nEditMode  = EditMode.READY;
                    return true;
                }
            }
            
            String lsSQL;
            ResultSet loRS;
            
            RowSetFactory factory = RowSetProvider.newFactory();
            
            //open master record
            lsSQL = MiscUtil.addCondition(getSQ_Master(), "a.sTransNox = " + SQLUtil.toSQL(fsTransNox));
            loRS = p_oNautilus.executeQuery(lsSQL);
            p_oMaster = factory.createCachedRowSet();
            p_oMaster.populate(loRS);
            MiscUtil.close(loRS);
            
            //open detailo record
            lsSQL = MiscUtil.addCondition(getSQ_Detail(), "a.sTransNox = " + SQLUtil.toSQL(fsTransNox));
            loRS = p_oNautilus.executeQuery(lsSQL);
            p_oDetail = factory.createCachedRowSet();
            p_oDetail.populate(loRS);
            MiscUtil.close(loRS);
            
            if (p_oMaster.size() == 1) {                            
                p_nEditMode  = EditMode.READY;
                return true;
            }
            
            setMessage("No transction loaded.");
        } catch (SQLException ex) {
            ex.printStackTrace();
            setMessage(ex.getMessage());
        }
        
        p_nEditMode  = EditMode.UNKNOWN;
        return false;
    }

    @Override
    public boolean UpdateTransaction() {
        System.out.println(this.getClass().getSimpleName() + ".UpdateTransaction()");
        
        if (p_nEditMode != EditMode.READY){
            setMessage("No transaction to update.");
            return false;
        }
        
        p_nEditMode = EditMode.UPDATE;
        
        return true;
    }

    @Override
    public boolean CloseTransaction() {
        System.out.println(this.getClass().getSimpleName() + ".CloseTransaction()");
        
        try {
            if (p_nEditMode != EditMode.READY){
                setMessage("No transaction to update.");
                return false;
            }
            
            if ((TransactionStatus.STATE_CLOSED).equals((String) p_oMaster.getObject("cTranStat"))) {
                setMessage("Transaction was already posted.");
                return false;
            }

            if ((TransactionStatus.STATE_CANCELLED).equals((String) p_oMaster.getObject("cTranStat"))){
                setMessage("This transaction was already cancelled. Unable to close transaction.");
                return false;
            }        

            if ((TransactionStatus.STATE_POSTED).equals((String) p_oMaster.getObject("cTranStat"))){
                setMessage("This transaction was already posted. Unable to close transaction.");
                return false;
            }
            
            if ((TransactionStatus.STATE_VOID).equals((String) p_oMaster.getObject("cTranStat"))){
                setMessage("This transaction was void. Unable to close transaction.");
                return false;
            }

            if (!p_bWithParent) p_oNautilus.beginTrans();
            
            if (!saveInvTrans()) return false;
            
            if (!updatePODetail()){
                if (!p_bWithParent) p_oNautilus.rollbackTrans();
                return false;
            }
            
//            if (!updateAPClient()){
//                if (!p_bWithParent) p_oNautilus.rollbackTrans();
//                return false;
//            }
            
            String lsSQL = "UPDATE " + MASTER_TABLE + " SET" +
                                "  cTranStat = " + TransactionStatus.STATE_CLOSED +
                                ", sApproved = " + SQLUtil.toSQL((String) p_oNautilus.getUserInfo("sUserIDxx")) +
                                ", dApproved = " + SQLUtil.toSQL(p_oNautilus.getServerDate()) +
                                ", dModified = " + SQLUtil.toSQL(p_oNautilus.getServerDate()) +
                            " WHERE sTransNox = " + SQLUtil.toSQL((String) p_oMaster.getObject("sTransNox"));

            if (p_oNautilus.executeUpdate(lsSQL, MASTER_TABLE, p_sBranchCd, "") <= 0){
                if (!p_bWithParent) p_oNautilus.rollbackTrans();
                setMessage(p_oNautilus.getMessage());
                return false;
            }

            if (!p_bWithParent) p_oNautilus.commitTrans();
            
            p_oMaster = null;
            p_oDetail = null;
            p_nEditMode  = EditMode.UNKNOWN;
            
            return true; 
        } catch (SQLException ex) {
            ex.printStackTrace();
            setMessage(ex.getMessage());
        }
        
        return false;   
    }

    @Override
    public boolean CancelTransaction() {
        System.out.println(this.getClass().getSimpleName() + ".CancelTransaction()");
        
        try {
            if (p_nEditMode != EditMode.READY){
                setMessage("No transaction to update.");
                return false;
            }

            if ((TransactionStatus.STATE_CANCELLED).equals((String) p_oMaster.getObject("cTranStat"))){
                setMessage("Transaction was already cancelled.");
                return false;
            }

            if ((TransactionStatus.STATE_CLOSED).equals((String) p_oMaster.getObject("cTranStat"))){   
                setMessage("This transaction was already approved. Unable to cancel transaction.");
                return false;
            }

            if ((TransactionStatus.STATE_POSTED).equals((String) p_oMaster.getObject("cTranStat"))){
                setMessage("This transaction was already posted. Unable to cancel transaction.");
                return false;
            }
            
            if ((TransactionStatus.STATE_VOID).equals((String) p_oMaster.getObject("cTranStat"))){
                setMessage("This transaction was void. Unable to cancel transaction.");
                return false;
            }

            String lsSQL = "UPDATE " + MASTER_TABLE + " SET" +
                                "  cTranStat = " + TransactionStatus.STATE_CANCELLED +
                                ", dModified= " + SQLUtil.toSQL(p_oNautilus.getServerDate()) +
                            " WHERE sTransNox = " + SQLUtil.toSQL((String) p_oMaster.getObject("sTransNox"));

            if (p_oNautilus.executeUpdate(lsSQL, p_oMaster.getTableName(), p_sBranchCd, "") <= 0){
                setMessage(p_oNautilus.getMessage());
                return false;
            }

            p_oMaster = null;
            p_oDetail = null;
            p_nEditMode  = EditMode.UNKNOWN;

            return true;
        } catch (SQLException ex) {
            ex.printStackTrace();
            setMessage(ex.getMessage());
        }
        
        return false;
    }

    @Override
    public boolean DeleteTransaction(String fsTransNox) {
        System.out.println(this.getClass().getSimpleName() + ".DeleteTransaction()");
        
        try {
            if (p_nEditMode != EditMode.READY){
                setMessage("No transaction to update.");
                return false;
            }

            if (!(TransactionStatus.STATE_OPEN).equals((String) p_oMaster.getObject("cTranStat"))){
                setMessage("Unable to delete already processed transactions.");
                return false;
            }

            //todo:
            //  validate user level here

            if (!p_bWithParent) p_oNautilus.beginTrans();

            String lsSQL = "DELETE FROM " + MASTER_TABLE +
                            " WHERE sTransNox = " + SQLUtil.toSQL((String) p_oMaster.getObject("sTransNox"));

            if (p_oNautilus.executeUpdate(lsSQL, MASTER_TABLE, p_sBranchCd, "") <= 0){
                if (!p_bWithParent) p_oNautilus.rollbackTrans();
                setMessage(p_oNautilus.getMessage());
                return false;
            }

            lsSQL = "DELETE FROM " + DETAIL_TABLE +
                    " WHERE sTransNox = " + SQLUtil.toSQL((String) p_oMaster.getObject("sTransNox"));

            if (p_oNautilus.executeUpdate(lsSQL, DETAIL_TABLE, p_sBranchCd, "") <= 0){
                if (!p_bWithParent) p_oNautilus.rollbackTrans();
                setMessage(p_oNautilus.getMessage());
                return false;
            }

            if (!p_bWithParent) p_oNautilus.commitTrans();

            p_oMaster = null;
            p_oDetail = null;
            p_nEditMode  = EditMode.UNKNOWN;

            return true;
        } catch (SQLException ex) {
            ex.printStackTrace();
            setMessage(ex.getMessage());
        }
        
        return false;
    }

    @Override
    public boolean PostTransaction() {
        System.out.println(this.getClass().getSimpleName() + ".PostTransaction()");
        
        try {
            if (p_nEditMode != EditMode.READY){
                setMessage("No transaction to update.");
                return false;
            }
            
            if ((TransactionStatus.STATE_POSTED).equals((String) p_oMaster.getObject("cTranStat"))){
                setMessage("Transaction was already posted.");
                return false;
            }

            if ((TransactionStatus.STATE_CANCELLED).equals((String) p_oMaster.getObject("cTranStat"))){
                setMessage("This transaction was already cancelled. Unable to post transaction.");
                return false;
            }
            
            if ((TransactionStatus.STATE_VOID).equals((String) p_oMaster.getObject("cTranStat"))){
                setMessage("This transaction was void. Unable to post transaction.");
                return false;
            }

            if (!p_bWithParent) p_oNautilus.beginTrans();
            
            String lsSQL = "UPDATE " + MASTER_TABLE + " SET" +
                                "  cTranStat = " + TransactionStatus.STATE_POSTED +
                                ", sPostedxx = " + SQLUtil.toSQL((String) p_oNautilus.getUserInfo("sUserIDxx")) +
                                ", dPostedxx = " + SQLUtil.toSQL(p_oNautilus.getServerDate()) +
                                ", dModified = " + SQLUtil.toSQL(p_oNautilus.getServerDate()) +
                            " WHERE sTransNox = " + SQLUtil.toSQL((String) p_oMaster.getObject("sTransNox"));
            
            if (p_oNautilus.executeUpdate(lsSQL, MASTER_TABLE, p_sBranchCd, "") <= 0){
                if (!p_bWithParent) p_oNautilus.rollbackTrans();
                setMessage(p_oNautilus.getMessage());
                return false;
            }
            
            if (!saveClientTrans()) {
                if (!p_bWithParent) p_oNautilus.rollbackTrans();
                return false;
            }
            
            if (!p_bWithParent) p_oNautilus.commitTrans();

            p_oMaster = null;
            p_oDetail = null;
            p_nEditMode  = EditMode.UNKNOWN;

            return true;
        } catch (SQLException ex) {
            ex.printStackTrace();
            setMessage(ex.getMessage());
            return false;
        }
    }

    @Override
    public ArrayList<Temp_Transactions> TempTransactions() {
        return p_oTemp;
    }
    
    public JSONObject searchBranchInventory(String fsKey, Object foValue, boolean fbExact){
        p_oSearchItem.setKey(fsKey);
        p_oSearchItem.setValue(foValue);
        p_oSearchItem.setExact(fbExact);
        
        return p_oSearchItem.Search();
    }
    
    public InvSearchF getSearchBranchInventory(){
        return p_oSearchItem;
    }
    
    public JSONObject searchSupplier(String fsKey, Object foValue, boolean fbExact){
        p_oSearchSupplier.setKey(fsKey);
        p_oSearchSupplier.setValue(foValue);
        p_oSearchSupplier.setExact(fbExact);
        
        return p_oSearchSupplier.Search();
    }
    
    public ClientSearch getSearchSupplier(){
        return p_oSearchSupplier;
    }
    
    public JSONObject searchTerm(String fsKey, Object foValue, boolean fbExact){
        p_oSearchTerm.setKey(fsKey);
        p_oSearchTerm.setValue(foValue);
        p_oSearchTerm.setExact(fbExact);
        
        return p_oSearchTerm.Search();
    }
    
    public ParamSearchF getSearchTerm(){
        return p_oSearchTerm;
    }
    
    public JSONObject searchTransaction(String fsKey, Object foValue, boolean fbExact){
        p_oSearchTrans.setKey(fsKey);
        p_oSearchTrans.setValue(foValue);
        p_oSearchTrans.setExact(fbExact);
        
        p_oSearchTrans.addFilter("Status", p_nTranStat);
        
        return p_oSearchTrans.Search();
    }
    
    public PurchasingSearch getSearchTransaction(){
        return p_oSearchTrans;
    }
    
    public JSONObject searchSource(String fsKey, Object foValue, boolean fbExact){
        p_oSearchSource.setKey(fsKey);
        p_oSearchSource.setValue(foValue);
        p_oSearchSource.setExact(fbExact);
        
        p_oSearchSource.addFilter("Status", 2);
        
        return p_oSearchSource.Search();
    }
    
    public PurchasingSearch getSearchSource(){
        return p_oSearchSource;
    }
    
    private String getSQ_Master(){
        return "SELECT" +
                    "  a.sTransNox" +
                    ", a.sBranchCd" +
                    ", a.dTransact" +
                    ", a.sCompnyID" +
                    ", a.sSupplier" +
                    ", a.sReferNox" +
                    ", a.dRefernce" +
                    ", a.sTermCode" +
                    ", a.nTranTotl" +
                    ", a.nVATRatex" +
                    ", a.nTWithHld" +
                    ", a.nDiscount" +
                    ", a.nAddDiscx" +
                    ", a.nAmtPaidx" +
                    ", a.nFreightx" +
                    ", a.sRemarksx" +
                    ", a.sSourceNo" +
                    ", a.sSourceCd" +
                    ", a.nEntryNox" +
                    ", a.sInvTypCd" +
                    ", a.cTranStat" +
                    ", a.sPrepared" +
                    ", a.dPrepared" +
                    ", a.sApproved" +
                    ", a.dApproved" +
                    ", a.sAprvCode" +
                    ", a.sPostedxx" +
                    ", a.dPostedxx" +
                    ", a.dCreatedx" +
                    ", a.dModified" +
                    ", IFNULL(b.sClientNm, '') sClientNm" +
                    ", IFNULL(c.sDescript, '') sTermName" +
                " FROM " + MASTER_TABLE + " a" +
                    " LEFT JOIN Client_Master b ON a.sSupplier = b.sClientID" +
                    " LEFT JOIN Term c ON a.sTermCode = c.sTermCode";
    }
    
    private String getSQ_Detail(){
        return "SELECT" +
                    "  a.sTransNox" +
                    ", a.nEntryNox" +	
                    ", a.sOrderNox" +	
                    ", a.sStockIDx" +
                    ", a.sReplacID" +	
                    ", a.cUnitType" +
                    ", a.nQuantity" +
                    ", a.nUnitPrce" +	
                    ", a.nFreightx" +	
                    ", b.sBarCodex" +
                    ", b.sDescript" +
                    ", c.nQtyOnHnd" +
                    ", b.sBrandCde" + 
                    ", b.sModelCde" +
                    ", b.sColorCde" +
                " FROM " + DETAIL_TABLE + " a" +
                    " LEFT JOIN Inventory b" +
                        " LEFT JOIN Inv_Master c" +
                            " ON b.sStockIDx = c.sStockIDx" +
                                " AND c.sBranchCd = " + SQLUtil.toSQL(p_sBranchCd) +
                    " ON a.sStockIDx = b.sStockIDx";
    }
    
    private void setMessage(String fsValue){
        p_sMessagex = fsValue;
    }
    
    private void saveToDisk(String fsRecdStat, String fsTransNox){
        if (p_bSaveToDisk && p_nEditMode == EditMode.ADDNEW){
            String lsPayloadx = toJSONString();
            
            if (p_sOrderNox.isEmpty()){
                p_sOrderNox = CommonUtil.getNextReference(p_oNautilus.getConnection().getConnection(), "xxxTempTransactions", "sOrderNox", "sSourceCd = " + SQLUtil.toSQL(SOURCE_CODE));
                CommonUtil.saveTempOrder(p_oNautilus, SOURCE_CODE, p_sOrderNox, lsPayloadx);
            } else
                CommonUtil.saveTempOrder(p_oNautilus, SOURCE_CODE, p_sOrderNox, lsPayloadx, fsRecdStat, fsTransNox);
        }
    }
    
    private void loadTempTransactions(){
        String lsSQL = "SELECT * FROM xxxTempTransactions" +
                        " WHERE cRecdStat = '1'" +
                            " AND sSourceCd = " + SQLUtil.toSQL(SOURCE_CODE);
        
        ResultSet loRS = p_oNautilus.executeQuery(lsSQL);
        
        Temp_Transactions loTemp;
        p_oTemp = new ArrayList<>();
        
        try {
            while(loRS.next()){
                loTemp = new Temp_Transactions();
                loTemp.setSourceCode(loRS.getString("sSourceCd"));
                loTemp.setOrderNo(loRS.getString("sOrderNox"));
                loTemp.setDateCreated(SQLUtil.toDate(loRS.getString("dCreatedx"), SQLUtil.FORMAT_TIMESTAMP));
                loTemp.setPayload(loRS.getString("sPayloadx"));
                p_oTemp.add(loTemp);
            }
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
        } finally {
            MiscUtil.close(loRS);
        }
    }
    
    private String toJSONString(){
        JSONParser loParser = new JSONParser();
        JSONArray laMaster = new JSONArray();
        JSONArray laDetail = new JSONArray();
        JSONObject loMaster;
        JSONObject loJSON;

        try {
            String lsValue = MiscUtil.RS2JSONi(p_oMaster).toJSONString();
            laMaster = (JSONArray) loParser.parse(lsValue);
            loMaster = (JSONObject) laMaster.get(0);
            
            lsValue = MiscUtil.RS2JSONi(p_oDetail).toJSONString();
            laDetail = (JSONArray) loParser.parse(lsValue);
 
            loJSON = new JSONObject();
            loJSON.put("master", loMaster);
            loJSON.put("detail", laDetail);
            
            return loJSON.toJSONString();
        } catch (ParseException ex) {
            ex.printStackTrace();
        }
        
        return "";
    }
    
    private boolean toDTO(String fsPayloadx){
        boolean lbLoad = false;
        
        if (fsPayloadx.isEmpty()) return lbLoad;
        
        JSONParser loParser = new JSONParser();
        
        JSONObject loJSON;
        JSONObject loMaster;
        JSONArray laDetail;
        
        try {
            String lsSQL;
            ResultSet loRS;
            
            RowSetFactory factory = RowSetProvider.newFactory();
            
            //create empty master record
            lsSQL = MiscUtil.addCondition(getSQ_Master(), "0=1");
            loRS = p_oNautilus.executeQuery(lsSQL);
            p_oMaster = factory.createCachedRowSet();
            p_oMaster.populate(loRS);
            MiscUtil.close(loRS);
            
            //create empty detail record
            lsSQL = MiscUtil.addCondition(getSQ_Detail(), "0=1");
            loRS = p_oNautilus.executeQuery(lsSQL);
            p_oDetail = factory.createCachedRowSet();
            p_oDetail.populate(loRS);
            MiscUtil.close(loRS);
            
            loJSON = (JSONObject) loParser.parse(fsPayloadx);
            loMaster = (JSONObject) loJSON.get("master");
            laDetail = (JSONArray) loJSON.get("detail");
            
            int lnCtr;
            int lnRow;
            
            int lnKey;
            String lsKey;
            String lsIndex;
            Iterator iterator;

            lnRow = 1;
            addMasterRow();
            for(iterator = loMaster.keySet().iterator(); iterator.hasNext();) {
                lsIndex = (String) iterator.next(); //string value of int
                lnKey = Integer.valueOf(lsIndex); //string to in
                lsKey = p_oMaster.getMetaData().getColumnLabel(lnKey); //int to metadata
                p_oMaster.absolute(lnRow);
                if (loMaster.get(lsIndex) != null){
                    switch(lsKey){
                        case "dTransact":
                        case "dRefernce":
                            p_oMaster.updateObject(lnKey, SQLUtil.toDate((String) loMaster.get(lsIndex), SQLUtil.FORMAT_SHORT_DATE));
                            break;
                        default:
                            p_oMaster.updateObject(lnKey, loMaster.get(lsIndex));
                    }

                    p_oMaster.updateRow();
                }
            }
            
            lnRow = 1;
            for(lnCtr = 0; lnCtr <= laDetail.size()-1; lnCtr++){
                JSONObject loDetail = (JSONObject) laDetail.get(lnCtr);

                addDetail();
                for(iterator = loDetail.keySet().iterator(); iterator.hasNext();) {
                    lsIndex = (String) iterator.next(); //string value of int
                    lnKey = Integer.valueOf(lsIndex); //string to int
                    p_oDetail.absolute(lnRow);
                    p_oDetail.updateObject(lnKey, loDetail.get(lsIndex));
                    p_oDetail.updateRow();
                }
                lnRow++;
            }
        } catch (SQLException | ParseException ex) {
            setMessage(ex.getMessage());
            ex.printStackTrace();
            return false;
        }
        
        return true;
    }
    
    private Connection getConnection(){         
        Connection foConn;
        
        if (p_bWithParent){
            foConn = (Connection) p_oNautilus.getConnection().getConnection();
            
            if (foConn == null) foConn = (Connection) p_oNautilus.doConnect();
        } else 
            foConn = (Connection) p_oNautilus.doConnect();
        
        return foConn;
    }
    
    private boolean isEntryOK(){
        try {
            //delete the last detail record if stock id
            int lnCtr = getItemCount();

            p_oDetail.absolute(lnCtr);
            if ("".equals((String) p_oDetail.getObject("sStockIDx"))){
                p_oDetail.deleteRow();
            }

            //validate if there is a detail record
            if (getItemCount() <= 0) {
                setMessage("There is no item in this transaction");
                addDetail(); //add detail to prevent error on the next attempt of saving
                return false;
            }
            
            if (((String)getMaster("sSupplier")).isEmpty()){
                setMessage("Supplier must not be empty.");
                return false;
            }
            
            if (((String)getMaster("sReferNox")).isEmpty()){
                setMessage("Reference no. must not be empty.");
                return false;
            }

            //assign values to master record
            p_oMaster.first();
            p_oMaster.updateObject("dTransact", p_oNautilus.getServerDate());
            
            if (((String) p_oMaster.getObject("sBranchCd")).isEmpty())
                p_oMaster.updateObject("sBranchCd", (String) p_oNautilus.getBranchConfig("sBranchCd"));
            
            if (((String) p_oMaster.getObject("sInvTypCd")).isEmpty())
                p_oMaster.updateObject("sInvTypCd", SYSTEM_CODE);

            String lsSQL = "SELECT dCreatedx FROM xxxTempTransactions" +
                            " WHERE sSourceCd = " + SQLUtil.toSQL(SOURCE_CODE) +
                                " AND sOrderNox = " + SQLUtil.toSQL(p_sOrderNox);
            
            ResultSet loRS = p_oNautilus.executeQuery(lsSQL);
            while (loRS.next()){
                p_oMaster.updateObject("dCreatedx", loRS.getString("dCreatedx"));
            }
            
            MiscUtil.close(loRS);
            p_oMaster.updateRow();

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            setMessage(e.getMessage());
            return false;
        }
    }
    
    private void addMasterRow() throws SQLException{
        p_oMaster.last();
        p_oMaster.moveToInsertRow();
        
        MiscUtil.initRowSet(p_oMaster);
        p_oMaster.updateObject("dRefernce", p_oNautilus.getServerDate());
        p_oMaster.updateObject("cTranStat", TransactionStatus.STATE_OPEN);
        
        p_oMaster.insertRow();
        p_oMaster.moveToCurrentRow();
    }
    
    private void computeTotal() throws SQLException{        
        int lnQuantity;
        double lnUnitPrce;
        double lnFreightx;
        double lnDetlTotl;
        
        double lnTranTotal = 0.00;
        int lnRow = getItemCount();
        
        for (int lnCtr = 0; lnCtr < lnRow; lnCtr++){
            lnQuantity = Integer.parseInt(String.valueOf(getDetail(lnCtr, "nQuantity")));
            lnUnitPrce = ((Number)getDetail(lnCtr, "nUnitPrce")).doubleValue();
            lnFreightx = ((Number)getDetail(lnCtr, "nFreightx")).doubleValue();
            lnDetlTotl = lnQuantity * lnUnitPrce;
            
            lnTranTotal += lnDetlTotl + lnFreightx;
        }
        
        p_oMaster.first();
        p_oMaster.updateObject("nTranTotl", lnTranTotal);
        p_oMaster.updateRow();
        
        saveToDisk(RecordStatus.ACTIVE, "");
    }
    
    public boolean DeleteTempTransaction(Temp_Transactions foValue) {
        boolean lbSuccess =  CommonUtil.saveTempOrder(p_oNautilus, foValue.getSourceCode(), foValue.getOrderNo(), foValue.getPayload(), "0");
        loadTempTransactions();
        
        p_nEditMode = EditMode.UNKNOWN;
        return lbSuccess;
    }
    
    private void getDetail(int fnRow, String fsFieldNm, Object foValue) throws SQLException, ParseException{       
        JSONObject loJSON = searchBranchInventory(fsFieldNm, foValue, true);
        JSONParser loParser = new JSONParser();
        
        switch(fsFieldNm){
            case "a.sStockIDx":
                if ("success".equals((String) loJSON.get("result"))){
                    loJSON = (JSONObject) ((JSONArray) loParser.parse((String) loJSON.get("payload"))).get(0);
                    
                    //check if the stock id was already exists
                    boolean lbExist = false;
                    
                    for (int lnCtr = 0; lnCtr <= getItemCount() - 1; lnCtr ++){
                        p_oDetail.absolute(lnCtr + 1);
                        if (((String) p_oDetail.getObject("sStockIDx")).equals((String) loJSON.get("sStockIDx"))){
                            fnRow = lnCtr;
                            lbExist = true;
                            break;
                        }
                    }
                    
                    p_oDetail.absolute(fnRow + 1);
                    p_oDetail.updateObject("sStockIDx", (String) loJSON.get("sStockIDx"));
                    p_oDetail.updateObject("nUnitPrce", (Number) loJSON.get("nUnitPrce"));
                    p_oDetail.updateObject("nQuantity", Integer.parseInt(String.valueOf(p_oDetail.getObject("nQuantity"))) + 1);
                    
                    p_oDetail.updateObject("sBarCodex", (String) loJSON.get("sBarCodex"));
                    p_oDetail.updateObject("sDescript", (String) loJSON.get("sDescript"));
                    p_oDetail.updateObject("nQtyOnHnd", Integer.parseInt(String.valueOf(loJSON.get("nQtyOnHnd"))));

                    p_oDetail.updateObject("sBrandCde", (String) loJSON.get("sBrandCde"));
                    p_oDetail.updateObject("sModelCde", (String) loJSON.get("sModelCde"));
                    p_oDetail.updateObject("sColorCde", (String) loJSON.get("sColorCde"));
                    p_oDetail.updateRow();                    
                    if (!lbExist) addDetail();
                }
        }
    }
    
    private void getSupplier(String fsFieldNm, Object foValue) throws SQLException, ParseException{       
        JSONObject loJSON = searchSupplier(fsFieldNm, foValue, true);
        JSONParser loParser = new JSONParser();

        if ("success".equals((String) loJSON.get("result"))){
            loJSON = (JSONObject) ((JSONArray) loParser.parse((String) loJSON.get("payload"))).get(0);

            p_oMaster.first();
            p_oMaster.updateObject("sSupplier", (String) loJSON.get("sClientID"));
            p_oMaster.updateObject("sClientNm", (String) loJSON.get("sClientNm"));
            p_oMaster.updateRow();            
            
            if (p_oListener != null) p_oListener.MasterRetreive("sSupplier", (String) p_oMaster.getObject("sClientNm"));
            saveToDisk(RecordStatus.ACTIVE, "");
        }
    }
    
    private void getTerm(String fsFieldNm, Object foValue) throws SQLException, ParseException{       
        JSONObject loJSON = searchTerm(fsFieldNm, foValue, true);
        JSONParser loParser = new JSONParser();

        if ("success".equals((String) loJSON.get("result"))){
            loJSON = (JSONObject) ((JSONArray) loParser.parse((String) loJSON.get("payload"))).get(0);

            p_oMaster.first();
            p_oMaster.updateObject("sTermCode", (String) loJSON.get("sTermCode"));
            p_oMaster.updateObject("sTermName", (String) loJSON.get("sDescript"));
            p_oMaster.updateRow();            
            
            if (p_oListener != null) p_oListener.MasterRetreive("sTermCode", (String) p_oMaster.getObject("sTermName"));
            saveToDisk(RecordStatus.ACTIVE, "");
        }
    }
    
    private void getSource(String fsFieldNm, Object foValue) throws SQLException, ParseException{       
        JSONObject loJSON = searchSource(fsFieldNm, foValue, true);
        JSONParser loParser = new JSONParser();

        if ("success".equals((String) loJSON.get("result"))){
            loJSON = (JSONObject) ((JSONArray) loParser.parse((String) loJSON.get("payload"))).get(0);
            
            
            PurchaseOrder loOrder = new PurchaseOrder(p_oNautilus, p_sBranchCd, true);
            loOrder.setSaveToDisk(false);
            loOrder.setTranStat(2);
            //open PO
            if (loOrder.OpenTransaction((String) loJSON.get("sTransNox"))){
                //assign master
                p_oMaster.first();
                p_oMaster.updateObject("sBranchCd", (String) loOrder.getMaster("sBranchCd"));
                p_oMaster.updateObject("sCompnyID", (String) loOrder.getMaster("sCompnyID"));
                p_oMaster.updateObject("sInvTypCd", (String) loOrder.getMaster("sInvTypCd"));
                p_oMaster.updateObject("sSourceNo", (String) loOrder.getMaster("sTransNox"));
                p_oMaster.updateObject("sSourceCd", "PO");
                p_oMaster.updateRow();
                
                if (((String) loOrder.getMaster("sSupplier")).isEmpty()){
                    p_oMaster.first();
                    p_oMaster.updateObject("sSupplier", "");
                    p_oMaster.updateObject("sClientNm", "");
                    p_oMaster.updateRow();
                    
                    if (p_oListener != null) p_oListener.MasterRetreive("sSupplier", "");
                } else setMaster("sSupplier", (String) loOrder.getMaster("sSupplier"));
                
                if (((String) loOrder.getMaster("sTermCode")).isEmpty()){
                    p_oMaster.first();
                    p_oMaster.updateObject("sTermCode", "");
                    p_oMaster.updateObject("sTermName", "");
                    p_oMaster.updateRow();
                    
                    if (p_oListener != null) p_oListener.MasterRetreive("sTermCode", "");
                } else setMaster("sTermCode", (String) loOrder.getMaster("sTermCode"));
                
                
                //create empty detail record
                RowSetFactory factory = RowSetProvider.newFactory();
                String lsSQL = MiscUtil.addCondition(getSQ_Detail(), "0=1");
                ResultSet loRS = p_oNautilus.executeQuery(lsSQL);
                p_oDetail = factory.createCachedRowSet();
                p_oDetail.populate(loRS);
                MiscUtil.close(loRS);
                addDetail();
            
                //assign detail
                int lnRow;
                int lnCtr;
                for (lnCtr = 0; lnCtr <= loOrder.getItemCount()-1; lnCtr++){
                    lnRow = getItemCount() - 1;
                    setDetail(lnRow, "sStockIDx", (String) loOrder.getDetail(lnCtr, "sStockIDx"));
                    setDetail(lnRow, "nQuantity", (int) loOrder.getDetail(lnCtr, "nQuantity"));
                }
            }
            
            if (p_oListener != null) p_oListener.MasterRetreive("sSourceNo", getMaster("sSourceNo"));
            saveToDisk(RecordStatus.ACTIVE, "");
        }
    }
    
    private boolean saveInvTrans() throws SQLException{
        InvTrans loTrans = new InvTrans(p_oNautilus, p_sBranchCd);
        int lnRow = getItemCount();
        
        if (loTrans.InitTransaction()){
            p_oMaster.first();
            for (int lnCtr = 0; lnCtr <= lnRow-1; lnCtr++){
                p_oDetail.absolute(lnCtr + 1);
                loTrans.setMaster(lnCtr, "sStockIDx", p_oDetail.getString("sStockIDx"));
                loTrans.setMaster(lnCtr, "nQuantity", p_oDetail.getInt("nQuantity"));
            
                if ("X0W1".contains(p_oMaster.getString("sBranchCd")))
                    loTrans.setMaster(lnCtr, "nQtyOrder", p_oDetail.getInt("nQuantity"));
                else
                    loTrans.setMaster(lnCtr, "nQtyIssue", p_oDetail.getInt("nQuantity"));
            }
            
            if (!loTrans.POReceiving(p_oMaster.getString("sTransNox"), 
                                        p_oMaster.getDate("dTransact"), 
                                        p_oMaster.getString("sSupplier"),
                                        EditMode.ADDNEW)){
                setMessage(loTrans.getMessage());
                return false;
            }
            
            return true;
        }
        
        setMessage(loTrans.getMessage());
        return false;
    }
    
    private boolean updateAPClient(){
        APClient loClient = new APClient(p_oNautilus, p_sBranchCd, true);
        if (loClient.OpenRecord((String) getMaster("sSupplier"))){
            if (loClient.getEditMode() == EditMode.READY){
                if (loClient.UpdateRecord()){
                    loClient.setMaster("nABalance", Double.valueOf(String.valueOf(loClient.getMaster("nABalance"))) + 
                                                    Double.valueOf(String.valueOf(getMaster("nTranTotl"))));
                    
                    if (loClient.getMaster("dCltSince") == null){
                        loClient.setMaster("dCltSince", getMaster("dRefernce"));
                    }
                    
                    if (!loClient.SaveRecord()){
                        p_sMessagex = "Unable to update AP record.";
                        return false;
                    }
                } else {
                    p_sMessagex = "Unable to update AP record.";
                    return false;
                }
            } else {
                p_sMessagex = "AP record for this supplier does not exist.\n" +
                                "Please create first before confirmation of this transaction.";
                return false;
            }
            
            return true;
        } else {
            p_sMessagex = "AP record for this supplier does not exist.\n" +
                                "Please create first before confirmation of this transaction.";
            return false;
        }
    }
    
    private boolean updatePODetail() throws SQLException{
        String lsSourceCd = String.valueOf(getMaster("sSourceCd"));
        String lsSourceNo = String.valueOf(getMaster("sSourceNo"));
        
        int lnCtr;
        int lnRow;
        String lsSQL;
        
        if (lsSourceCd.equals("PO")){
            lnRow = getItemCount();
            
            if (lsSourceNo.isEmpty()){
                for (lnCtr = 0; lnCtr <= lnRow-1; lnCtr++){
                    if (!String.valueOf(getDetail(lnCtr, "sOrderNox")).isEmpty()){
                        lsSQL = "UPDATE PO_Detail SET" +
                                " nReceived = nReceived + " + (int) getDetail(lnCtr, "nQuantity") +
                                " WHERE sTransNox = " + SQLUtil.toSQL(String.valueOf(getDetail(lnCtr, "sOrderNox"))) +
                                    " AND sStockIDx = " + SQLUtil.toSQL((String) getDetail(lnCtr, "sStockIDx"));
                        
                        if(p_oNautilus.executeUpdate(lsSQL, "PO_Detail", p_sBranchCd, "") <= 0){
                            p_sMessagex = "Unable to update PO Detail";
                            return false;
                        } 
                    }
                }
            } else {
                for (lnCtr = 0; lnCtr <= lnRow-1; lnCtr++){
                    if (!String.valueOf(getDetail(lnCtr, "sOrderNox")).isEmpty()){
                        lsSQL = "UPDATE PO_Detail SET" +
                                " nReceived = nReceived + " + (int) getDetail(lnCtr, "nQuantity") +
                                " WHERE sTransNox = " + SQLUtil.toSQL(String.valueOf(getDetail(lnCtr, "sOrderNox"))) +
                                    " AND sStockIDx = " + SQLUtil.toSQL((String) getDetail(lnCtr, "sStockIDx"));
                        
                        if(p_oNautilus.executeUpdate(lsSQL, "PO_Detail", p_sBranchCd, "") <= 0){
                            p_sMessagex = "Unable to update PO Detail";
                            return false;
                        } 
                    } else {
                        lsSQL = "UPDATE PO_Detail SET" +
                                " nReceived = nReceived + " + (int) getDetail(lnCtr, "nQuantity") +
                                " WHERE sTransNox = " + SQLUtil.toSQL(lsSourceNo) +
                                    " AND sStockIDx = " + SQLUtil.toSQL((String) getDetail(lnCtr, "sStockIDx"));
                        
                        if(p_oNautilus.executeUpdate(lsSQL, "PO_Detail", p_sBranchCd, "") <= 0){
                            p_sMessagex = "Unable to update PO Detail";
                            return false;
                        } 
                    }
                }
            }
            
            
            lsSQL = "SELECT nQuantity, nReceived, nCancelld" +
                    " FROM PO_Detail" +
                    " WHERE sTransNox = " + SQLUtil.toSQL(lsSourceNo);
            
            ResultSet loRS = p_oNautilus.executeQuery(lsSQL);
            boolean lbServed = true;
            
            while (loRS.next()){
                if (loRS.getInt("nQuantity") > loRS.getInt("nReceived") + loRS.getInt("nCancelld")){
                    lbServed = false;
                    break;
                }
            }
            
            if (lbServed){
                lsSQL = "UPDATE PO_Master SET" +
                            "  cTranStat = '4'" +
                            ", dModified = " + SQLUtil.toSQL(p_oNautilus.getServerDate()) +
                        " WHERE sTransNox = " + SQLUtil.toSQL(lsSourceNo);
                
                if(p_oNautilus.executeUpdate(lsSQL, "PO_Master", p_sBranchCd, "") <= 0){
                    p_sMessagex = "Unable to update PO Master";
                    return false;
                } 
            }
        }
        
        return true;
    }
    
    private boolean saveClientTrans() throws SQLException{
        APClientTrans loClient = new APClientTrans(p_oNautilus, p_sBranchCd);
        
        double lnPayable = p_oMaster.getDouble("nTranTotl");
        double lnDiscount;
        
        if (p_oMaster.getDouble("nDiscount") > 0.00)
            lnDiscount = lnPayable * (p_oMaster.getDouble("nDiscount") / 100) + p_oMaster.getDouble("nAddDiscx");
        else
            lnDiscount = p_oMaster.getDouble("nAddDiscx");
        
        if (!loClient.Purchase((String) getMaster("sTransNox"), 
                                    (String) getMaster("sSupplier"), 
                                    p_oNautilus.getServerDate(), 
                                    lnPayable - lnDiscount, 
                                    0.00, 
                                    EditMode.ADDNEW)){
            p_sMessagex = loClient.getMessage();
            return false;
        }
        
        return true;
    }
}

