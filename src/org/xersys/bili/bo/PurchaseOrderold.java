package org.xersys.bili.bo;

import org.xersys.imbentaryo.bo.Inventory;
import com.mysql.jdbc.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.xersys.bili.dto.PO_Detail;
import org.xersys.bili.dto.PO_Master;
import org.xersys.bili.dto.PO_Others;
import org.xersys.kumander.contants.SearchEnum;
import org.xersys.kumander.iface.LMasDetTrans;
import org.xersys.kumander.iface.XEntity;
import org.xersys.kumander.iface.XMasDetTrans;
import org.xersys.kumander.iface.XNautilus;
import org.xersys.kumander.util.MiscUtil;
import org.xersys.kumander.util.SQLUtil;
import org.xersys.kumander.contants.EditMode;
import org.xersys.kumander.contants.TransactionStatus;

public class PurchaseOrderold implements XMasDetTrans{
    XNautilus p_oNautilus;
    LMasDetTrans p_oListener;
    
    Inventory p_oInventory;
    
    boolean p_bSaveToDisk;
    boolean p_bWithParent;
    
    String p_sBranchCd;
    String p_sMessagex;
    
    int p_nEditMode;
    int p_nTranStat;
    
    PO_Master p_oMaster;
    ArrayList<PO_Detail> p_oDetail;
    ArrayList<PO_Others> p_oOthers;
    
    public PurchaseOrderold(XNautilus foNautilus, String fsBranchCd, boolean fbWithParent){
        p_oNautilus = foNautilus;
        p_sBranchCd = fsBranchCd;
        p_bWithParent = fbWithParent;
        p_nEditMode = EditMode.UNKNOWN;
        
        p_oInventory = new Inventory(p_oNautilus);
    }
    
    public PurchaseOrderold(XNautilus foNautilus, String fsBranchCd, boolean fbWithParent, int fnTranStat){
        p_oNautilus = foNautilus;
        p_sBranchCd = fsBranchCd;
        p_bWithParent = fbWithParent;
        p_nTranStat = fnTranStat;
        p_nEditMode = EditMode.UNKNOWN;
        
        p_oInventory = new Inventory(p_oNautilus);
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
        
        p_oMaster.setValue(fsFieldNm, foValue);
    }

    @Override
    public Object getMaster(String fsFieldNm) {
        return p_oMaster.getValue(fsFieldNm);
    }

    @Override
    public void setDetail(int fnRow, String fsFieldNm, Object foValue) {
        if (p_nEditMode != EditMode.ADDNEW &&
            p_nEditMode != EditMode.UPDATE){
            System.err.println("Transaction is not on update mode.");
            return;
        }
        
        p_oDetail.get(fnRow).setValue(fsFieldNm, foValue);
        
        if (fsFieldNm.equals("nQuantity") ||
            fsFieldNm.equals("nUnitPrce")){
            computeTotal();
            p_oListener.MasterRetreive("nTranTotl", p_oMaster.getValue("nTranTotl"));
        }
    }

    @Override
    public Object getDetail(int fnRow, String fsFieldNm) {
        return p_oDetail.get(fnRow).getValue(fsFieldNm);
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
        return p_oDetail.size();
    }

    @Override
    public boolean addDetail() {
        if (p_oDetail.isEmpty()){
            p_oDetail.add(new PO_Detail());
            //p_oOthers.add(new PO_Others());
        }else{
            if (!"".equals((String) p_oDetail.get(getItemCount() - 1).getValue("sStockIDx")) &&
                (int) p_oDetail.get(getItemCount() - 1).getValue("nQuantity") != 0){
                p_oDetail.add(new PO_Detail());
                //p_oOthers.add(new PO_Others());
            }
        }
        return true;
    }

    @Override
    public boolean delDetail(int fnRow) {
        p_oDetail.remove(fnRow);
        //p_oOthers.remove(fnRow);
        
        if (p_oDetail.isEmpty()) return addDetail();
        
        return true;
    }

    @Override
    public JSONObject SearchMaster(String fsFieldNm, Object foValue) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public JSONObject SearchDetail(int fnRow, String fsFieldNm, Object foValue) {
        if (fnRow < 0) return null;
        
        switch (fsFieldNm){
            case "sBarCodex":
                return null;
            case "sDesript":
                return null;
            default:
                return null;
        }
    }
    
    public JSONObject Search(SearchEnum.Type foType, String fsValue, String fsKey, String fsFilter, int fnMaxRow, boolean fbExact){
        if (p_oInventory == null){
            JSONObject loJSON = new JSONObject();
            loJSON.put("result", "error");
            loJSON.put("message", "Inventory object is not set.");
            return loJSON;
        }
        
        p_oInventory.Search().setKey(fsKey);
        p_oInventory.Search().setFilter(fsFilter);
        p_oInventory.Search().sethMax(fnMaxRow);
        p_oInventory.Search().setExact(fbExact);

        return p_oInventory.Search().Search(foType, fsValue);
    }

    @Override
    public boolean NewTransaction() {
        System.out.println(this.getClass().getSimpleName() + ".NewTransaction()");
        
        p_oMaster = new PO_Master();
        p_oDetail = new ArrayList<>();
        p_oOthers = new ArrayList<>();
        
        addDetail();
        
        p_nEditMode = EditMode.ADDNEW;
        
        return true;
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
            System.out.println(toJSONString());
            return true;
        }
        
        String lsSQL = "";
        
        PO_Master loOldEnt = null;
        PO_Master loNewEnt = null;

        loNewEnt = (PO_Master) p_oMaster;
        
        if (!isEntryOK()) return false;
        
        try {
            if (!p_bWithParent) p_oNautilus.beginTrans();
        
            if ("".equals((String) loNewEnt.getValue("sTransNox"))){ //new record
                Connection loConn = getConnection();

                loNewEnt.setValue("sTransNox", MiscUtil.getNextCode(loNewEnt.getTable(), "sTransNox", true, loConn, p_sBranchCd));

                if (!p_bWithParent) MiscUtil.close(loConn);

                //save the detail
                if (!saveDetail(loNewEnt, true)){
                    if (!p_bWithParent) p_oNautilus.rollbackTrans();
                    return false;
                }

                loNewEnt.setValue("nEntryNox", getItemCount());

                loNewEnt.setValue("sPrepared", (String) p_oNautilus.getUserInfo("sUserIDxx"));
                loNewEnt.setValue("dPrepared", p_oNautilus.getServerDate());

                loNewEnt.setValue("sModified", (String) p_oNautilus.getUserInfo("sUserIDxx"));
                loNewEnt.setValue("dModified", p_oNautilus.getServerDate());

                lsSQL = MiscUtil.makeSQL((XEntity) loNewEnt);
            } else { //old record
                loOldEnt = loadMaster((String) loNewEnt.getValue("sTransNox"));
                
                //save the detail
                if (!saveDetail(loNewEnt, false)){
                    if (!p_bWithParent) p_oNautilus.rollbackTrans();
                    return false;
                }
                
                loNewEnt.setValue("nEntryNox", getItemCount());
                loNewEnt.setValue("sModified", (String) p_oNautilus.getUserInfo("sUserIDxx"));
                loNewEnt.setValue("dModified", p_oNautilus.getServerDate());
                
                lsSQL = MiscUtil.makeSQL((XEntity) loNewEnt, (XEntity) loOldEnt, "sTransNox = " + SQLUtil.toSQL((String) loNewEnt.getValue("sTransNox")));
            }
            
            if (lsSQL.equals("")){
                if (!p_bWithParent) p_oNautilus.rollbackTrans();
                
                setMessage("No record to update");
                return false;
            }
            
            if(p_oNautilus.executeUpdate(lsSQL, loNewEnt.getTable(), p_sBranchCd, "") <= 0){
                if(!p_oNautilus.getMessage().isEmpty())
                    setMessage(p_oNautilus.getMessage());
                else
                    setMessage("No record updated");
            } 

            if (!p_bWithParent) {
                if(!p_oNautilus.getMessage().isEmpty())
                    p_oNautilus.rollbackTrans();
                else
                    p_oNautilus.commitTrans();
            }    
        } catch (SQLException ex) {
            if (!p_bWithParent) p_oNautilus.rollbackTrans();
            
            ex.printStackTrace();
            setMessage(ex.getMessage());
            return false;
        }
        
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
            p_oMaster = loadMaster(fsTransNox);
            p_oDetail = loadDetail(fsTransNox);
            
            if (p_oMaster != null) {
                if ((int) p_oMaster.getValue("nEntryNox") != p_oDetail.size()){
                    setMessage("Transaction discrepancy detected.");
                    return false;
                }
                
                p_nEditMode  = EditMode.READY;
                return true;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            setMessage(ex.getMessage());
        }
        
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
        
        if (p_nEditMode != EditMode.READY){
            setMessage("No transaction to update.");
            return false;
        }
        
        if ((TransactionStatus.STATE_CANCELLED).equals((String) p_oMaster.getValue("cTranStat"))){
            setMessage("Unable to approve cancelled transactons");
            return false;
        }        
        
        if ((TransactionStatus.STATE_POSTED).equals((String) p_oMaster.getValue("cTranStat"))){
            setMessage("Unable to approve posted transactons");
            return false;
        }
        
        if ((TransactionStatus.STATE_CLOSED).equals((String) p_oMaster.getValue("cTranStat"))){
            setMessage("Transaction was already approved.");
            return false;
        }
        
        String lsSQL = "UPDATE " + p_oMaster.getTable() + " SET" +
                            "  cTranStat = " + TransactionStatus.STATE_CLOSED +
                            ", sApproved = " + SQLUtil.toSQL((String) p_oNautilus.getUserInfo("sUserIDxx")) +
                            ", dApproved = " + SQLUtil.toSQL(p_oNautilus.getServerDate()) +
                            ", sModified = " + SQLUtil.toSQL((String) p_oNautilus.getUserInfo("sUserIDxx")) +
                            ", dModified= " + SQLUtil.toSQL(p_oNautilus.getServerDate()) +
                        " WHERE sTransNox = " + SQLUtil.toSQL((String) p_oMaster.getValue("sTransNox"));
        
        if (p_oNautilus.executeUpdate(lsSQL, p_oMaster.getTable(), p_sBranchCd, "") <= 0){
            setMessage(p_oNautilus.getMessage());
            return false;
        }
        
        p_nEditMode  = EditMode.UNKNOWN;
        
        return true;
    }

    @Override
    public boolean CancelTransaction() {
        System.out.println(this.getClass().getSimpleName() + ".CancelTransaction()");
        
        if (p_nEditMode != EditMode.READY){
            setMessage("No transaction to update.");
            return false;
        }
        
        if ((TransactionStatus.STATE_CANCELLED).equals((String) p_oMaster.getValue("cTranStat"))){
            setMessage("Transaction was already cancelled.");
            return false;
        }
        
        //todo:
        //  validate user level/approval code here if we will allow them to cancel approved/posted transactions
        
        if ((TransactionStatus.STATE_CLOSED).equals((String) p_oMaster.getValue("cTranStat"))){   
            setMessage("Unable to cancel approved transactions.");
            return false;
        }
        
        if ((TransactionStatus.STATE_POSTED).equals((String) p_oMaster.getValue("cTranStat"))){
            setMessage("Unable to cancel posted transactions.");
            return false;
        }
        
        String lsSQL = "UPDATE " + p_oMaster.getTable() + " SET" +
                            "  cTranStat = " + TransactionStatus.STATE_CANCELLED +
                            ", sModified = " + SQLUtil.toSQL((String) p_oNautilus.getUserInfo("sUserIDxx")) +
                            ", dModified= " + SQLUtil.toSQL(p_oNautilus.getServerDate()) +
                        " WHERE sTransNox = " + SQLUtil.toSQL((String) p_oMaster.getValue("sTransNox"));
        
        if (p_oNautilus.executeUpdate(lsSQL, p_oMaster.getTable(), p_sBranchCd, "") <= 0){
            setMessage(p_oNautilus.getMessage());
            return false;
        }
        
        p_nEditMode  = EditMode.UNKNOWN;
        
        return true;
    }

    @Override
    public boolean DeleteTransaction(String fsTransNox) {
        System.out.println(this.getClass().getSimpleName() + ".DeleteTransaction()");
        
        if (p_nEditMode != EditMode.READY){
            setMessage("No transaction to update.");
            return false;
        }
        
        if (!(TransactionStatus.STATE_OPEN).equals((String) p_oMaster.getValue("cTranStat"))){
            setMessage("Unable to delete already processed transactions.");
            return false;
        }
        
        //todo:
        //  validate user level here
        
        if (!p_bWithParent) p_oNautilus.beginTrans();
        
        String lsSQL = "DELETE FROM " + p_oMaster.getTable() +
                        " WHERE sTransNox = " + SQLUtil.toSQL((String) p_oMaster.getValue("sTransNox"));
        
        if (p_oNautilus.executeUpdate(lsSQL, p_oMaster.getTable(), p_sBranchCd, "") <= 0){
            if (!p_bWithParent) p_oNautilus.rollbackTrans();
            setMessage(p_oNautilus.getMessage());
            return false;
        }
        
        lsSQL = "DELETE FROM " + new PO_Detail().getTable() +
                " WHERE sTransNox = " + SQLUtil.toSQL((String) p_oMaster.getValue("sTransNox"));
        
        if (p_oNautilus.executeUpdate(lsSQL, new PO_Detail().getTable(), p_sBranchCd, "") <= 0){
            if (!p_bWithParent) p_oNautilus.rollbackTrans();
            setMessage(p_oNautilus.getMessage());
            return false;
        }
        
        if (!p_bWithParent) p_oNautilus.commitTrans();
        
        p_nEditMode  = EditMode.UNKNOWN;
        
        return true;
    }

    @Override
    public boolean PostTransaction() {
        System.out.println(this.getClass().getSimpleName() + ".PostTransaction()");
        
        if (p_nEditMode != EditMode.READY){
            setMessage("No transaction to update.");
            return false;
        }
        
        if ((TransactionStatus.STATE_CANCELLED).equals((String) p_oMaster.getValue("cTranStat"))){
            setMessage("Unable to post cancelled transactions.");
            return false;
        }
        
        if ((TransactionStatus.STATE_POSTED).equals((String) p_oMaster.getValue("cTranStat"))){
            setMessage("Transaction was already posted.");
            return false;
        }
        
        //todo:
        //  check if user level validation is still needed
        
        String lsSQL = "UPDATE " + p_oMaster.getTable() + " SET" +
                            "  cTranStat = " + TransactionStatus.STATE_POSTED +
                            ", sPostedxx = " + SQLUtil.toSQL((String) p_oNautilus.getUserInfo("sUserIDxx")) +
                            ", dPostedxx = " + SQLUtil.toSQL(p_oNautilus.getServerDate()) +
                            ", sModified = " + SQLUtil.toSQL((String) p_oNautilus.getUserInfo("sUserIDxx")) +
                            ", dModified= " + SQLUtil.toSQL(p_oNautilus.getServerDate()) +
                        " WHERE sTransNox = " + SQLUtil.toSQL((String) p_oMaster.getValue("sTransNox"));
        
        if (p_oNautilus.executeUpdate(lsSQL, p_oMaster.getTable(), p_sBranchCd, "") <= 0){
            setMessage(p_oNautilus.getMessage());
            return false;
        }
        
        p_nEditMode  = EditMode.UNKNOWN;
        
        return true;
    }    
    
    //added methods
    private void setMessage(String fsValue){
        p_sMessagex = fsValue;
    }
    
    private void saveToDisk(){
        if (p_bSaveToDisk){
            System.out.println(toJSONString());
        }
    }
    
    private String toJSONString(){
        JSONParser loParser = new JSONParser();
        JSONArray loDetail = new JSONArray();
        JSONObject loMaster;
        JSONObject loJSON;

        try {
            loMaster = (JSONObject) loParser.parse(p_oMaster.toJSONString());

            for (int lnCtr = 0; lnCtr < p_oDetail.size(); lnCtr++){
                loJSON = (JSONObject) loParser.parse(p_oDetail.get(lnCtr).toJSONString());
                loDetail.add(loJSON);
            }

            loJSON = new JSONObject();
            loJSON.put("master", loMaster);
            loJSON.put("detail", loDetail);
            
            return loJSON.toJSONString();
        } catch (ParseException ex) {
            ex.printStackTrace();
        }
        
        return "";
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
    
    private boolean saveDetail(PO_Master foMaster, boolean fbNewRecord) throws SQLException{
        ArrayList<PO_Detail> loDetail = p_oDetail;
        PO_Detail loOldDet;
        
        int lnCtr;
        String lsSQL;
        
        for (lnCtr = 0; lnCtr <= loDetail.size() - 1; lnCtr++){            
            if (!"".equals((String) loDetail.get(lnCtr).getValue("sStockIDx"))){
                //check the unit price of the item
                if (((Number) loDetail.get(lnCtr).getValue("nUnitPrce")).doubleValue() <= 0.00){
                    setMessage("It seems that an item has zero or negative unit price.");
                    return false;
                }
                
                if (fbNewRecord){
                    loDetail.get(lnCtr).setValue("sTransNox", (String) foMaster.getValue("sTransNox"));
                    loDetail.get(lnCtr).setValue("nEntryNox", lnCtr + 1);
                
                    lsSQL = MiscUtil.makeSQL((XEntity) loDetail.get(lnCtr));
                } else {
                    loOldDet = loadDetail((String )foMaster.getValue("sTransNox"), lnCtr + 1);
                    
                    lsSQL = MiscUtil.makeSQL((XEntity) loDetail.get(lnCtr), (XEntity) loOldDet, 
                                                "sTransNox = " + SQLUtil.toSQL((String) loDetail.get(lnCtr).getValue("sTransNox")) + 
                                                    " AND nEntryNox = " + (int) loDetail.get(lnCtr).getValue("nEntryNox"));
                }
                
                if (!lsSQL.equals("")){
                    if(p_oNautilus.executeUpdate(lsSQL, loDetail.get(lnCtr).getTable(), p_sBranchCd, "") == 0){
                        if(!p_oNautilus.getMessage().isEmpty()){                             
                            setMessage(p_oNautilus.getMessage());
                            return false;
                        }
                    }else {
                        setMessage("No record to update.");
                    }
                }
            }
        }
        
        int lnRow = loadDetail((String) foMaster.getValue("sTransNox")).size();
        //is the new detail is less than the original count then delete the excess old records
        if (lnCtr < lnRow -1){
            for (lnCtr = lnCtr + 1; lnCtr <= lnRow; lnCtr++){
                lsSQL = "DELETE FROM " + new PO_Detail().getTable() +  
                        " WHERE sTransNox = " + SQLUtil.toSQL((String) foMaster.getValue("sTransNox")) + 
                            " AND nEntryNox = " + lnCtr;
                
                if(p_oNautilus.executeUpdate(lsSQL, new PO_Detail().getTable(), p_sBranchCd, "") == 0){
                    if(!p_oNautilus.getMessage().isEmpty()) 
                        setMessage(p_oNautilus.getMessage());
                }else {
                    setMessage("No record updated");
                    return false;
                }
            }
        }
        
        return true;
    }
    
    private PO_Detail loadDetail(String fsTransNox, int fnEntryNox) throws SQLException{
        PO_Detail loObj = new PO_Detail();
        
        String lsSQL = MiscUtil.addCondition(MiscUtil.makeSelect(new PO_Detail()), 
                                                    "sTransNox = " + SQLUtil.toSQL(fsTransNox)) + 
                                                    " AND nEntryNox = " + fnEntryNox;
        
        ResultSet loRS = p_oNautilus.executeQuery(lsSQL);
        
        if (!loRS.next()){
            setMessage("No Record Found");
        }else{
            for(int lnCol=1; lnCol<=loRS.getMetaData().getColumnCount(); lnCol++){
                loObj.setValue(lnCol, loRS.getObject(lnCol));
            }
        }      
        
        return loObj;
    }
    
    private ArrayList<PO_Detail> loadDetail(String fsTransNox) throws SQLException{
        PO_Detail loOcc = null;        
        
        ArrayList<PO_Detail> loDetail = new ArrayList<>();
        
        ResultSet loRS = p_oNautilus.executeQuery(MiscUtil.addCondition(MiscUtil.makeSelect(new PO_Detail()), 
                                                    "sTransNox = " + SQLUtil.toSQL(fsTransNox)));
               
        for (int lnCtr = 1; lnCtr <= MiscUtil.RecordCount(loRS); lnCtr ++){
            loRS.absolute(lnCtr);
            
            loOcc = new PO_Detail();
            
            for(int lnCol=1; lnCol<=loRS.getMetaData().getColumnCount(); lnCol++){
                loOcc.setValue(lnCol, loRS.getObject(lnCol));
            }
            loDetail.add(loOcc);
        }
        
        return loDetail;
    }
    
    private PO_Master loadMaster(String fsTransNox) throws SQLException{
        PO_Master loMaster = new PO_Master();        
        ArrayList<PO_Detail> loDetail = new ArrayList<>();
        
        String lsSQL = MiscUtil.addCondition(MiscUtil.makeSelect(loMaster), "sTransNox = " + SQLUtil.toSQL(fsTransNox));
        ResultSet loRS = p_oNautilus.executeQuery(lsSQL);
        
        if (!loRS.next()){
            setMessage("No Record Found");
        } else{
            //load each column to the entity
            for(int lnCol=1; lnCol<=loRS.getMetaData().getColumnCount(); lnCol++){
                loMaster.setValue(lnCol, loRS.getObject(lnCol));
            }
             
            return loMaster;
        }              
    
        return null;
    }
    
    private void computeTotal(){
        double lnTranTotal = 0.00;
        
        for (int lnCtr = 0; lnCtr < p_oDetail.size(); lnCtr++){
            lnTranTotal += (int) p_oDetail.get(lnCtr).getValue("nQuantity") * 
                            ((Number) p_oDetail.get(lnCtr).getValue("nUnitPrce")).doubleValue();
        }
        
        p_oMaster.setValue("nTranTotl", lnTranTotal);
    }
    
    private boolean isEntryOK(){
        if ("".equals((String) p_oMaster.getValue("sBranchCd"))){
            setMessage("Requesting branch is NOT SET.");
            return false;
        }
        
        if ("".equals((String) p_oMaster.getValue("sSupplier"))){
            setMessage("Supplier is NOT SET.");
            return false;
        }
        
        if ("".equals((String) p_oMaster.getValue("sTermCode"))){
            setMessage("Term is NOT SET.");
            return false;
        }
        
        if (getItemCount() <= 0){
            setMessage("No items are selected for this order.");
            return false;
        }
    
        return true;
    }

    @Override
    public boolean NewTransaction(String fsOrderNox) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
    public void setDetail(int fnRow, int fnIndex, Object foValue) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object getDetail(int fnRow, int fnIndex) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object TempTransactions() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
