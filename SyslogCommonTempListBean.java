package com.h3c.imc.syslog.templet.view;

import com.h3c.imc.common.ListResult;
import com.h3c.imc.common.QueryFilter;
import com.h3c.imc.common.Restriction;
import com.h3c.imc.common.StringManager;
import com.h3c.imc.common.faces.ColumnHeader;
import com.h3c.imc.common.faces.DynamicDataModel;
import com.h3c.imc.faces.entity.OperateResult;
import com.h3c.imc.plat.operator.view.OperatorLoginInfo;
import com.h3c.imc.res.memRes.MemQueryCondition;
import com.h3c.imc.syslog.SyslogException;
import com.h3c.imc.syslog.common.view.Operlog;
import com.h3c.imc.syslog.common.view.SyslogSortableList;
import com.h3c.imc.syslog.common.view.SyslogViewUtils;
import com.h3c.imc.syslog.entity.SyslogTempletEntity;
import com.h3c.imc.syslog.templet.func.SyslogCommonTempletMgr;
import java.util.ArrayList;
import java.util.List;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import org.apache.commons.lang.StringUtils;

public class SyslogCommonTempListBean
  extends SyslogSortableList
{
  protected static final StringManager SM = StringManager.getManager("com.h3c.imc.syslog");
  private int winsMode = 0;
  private OperateResult optResult = new OperateResult();
  private SyslogCommonTempletMgr commonTempletMgr;
  private String watermarkValue = SM.getString("syslog.template.search.watermark");
  private String searchWord;
  private boolean testMode = false;
  private String syslogContent = "";
  private DataModel<SyslogTempletEntity> testData = new ListDataModel();
  
  public SyslogCommonTempListBean()
  {
    paging = true;
    showSelectAll = true;
    showModify = true;
    showCopy = true;
    showDelete = true;
    ascending = true;
    sortColumn = "disType";
  }
  
  protected void initColumnHeaders()
  {
    columnHeaderList = new ArrayList();
    columnHeaderList.add(ColumnHeader.createLinkColumn(SM.getString("syslog.templet.list.templet.name"), "tempName", "25%"));
    
    columnHeaderList.add(ColumnHeader.createLinkColumn(SM.getString("syslog.templet.list.templet.content"), "tempContent", "50%"));
    
    columnHeaderList.add(ColumnHeader.createOutputTextColumn(SM.getString("syslog.templet.list.templet.type"), "disType", "20%"));
  }
  
  public String queryConditionChanged()
  {
    fireDataChanged();
    return null;
  }
  
  public String onResetLeft()
  {
    searchWord = null;
    sortColumn = "disType";
    fireDataChanged();
    return "go_syslog_templetListPage";
  }
  
  public String onRefresh()
  {
    super.refresh();
    return null;
  }
  
  public String onMatchSyslogTemplet()
  {
    testMode = true;
    List<SyslogTempletEntity> tempList = new ArrayList();
    tempList = commonTempletMgr.queryMatchTemplates(syslogContent);
    
    testData.setWrappedData(tempList);
    return null;
  }
  
  public void onTest()
  {
    testData.setWrappedData(new ArrayList());
  }
  
  public void onCancelSyslogTemplet()
  {
    syslogContent = "";
  }
  
  protected void queryData()
  {
    if (testMode)
    {
      testMode = false;
      return;
    }
    MemQueryCondition mQuery = new MemQueryCondition(operatorLoginInfo.getResources());
    QueryFilter qf = new QueryFilter();
    if ((null != searchWord) && (StringUtils.isNotBlank(searchWord))) {
      qf.addRestriction(Restriction.or(new Restriction[] { Restriction.like("tempName", searchWord), Restriction.like("tempContent", searchWord) }));
    }
    qf.addOrder(sortColumn, ascending, false);
    mQuery.setQueryFilter(qf);
    
    List tempList = commonTempletMgr.queryAllTemplets(mQuery);
    syncData(new ListResult(tempList, tempList.size()));
  }
  
  public String preCopy()
  {
    String strNav = "";
    
    SyslogCommonTempConfigBean configBean = SyslogViewUtils.getSyslogCommonTempConfigBean();
    
    SyslogTempletEntity entity = (SyslogTempletEntity)data.getRowData();
    
    SyslogTempletEntity info = commonTempletMgr.queryTempletById(entity.getTempId());
    if (null == info)
    {
      optResult.showFailureMsgInOtherPage(SM.getString("syslog.templet.error.tempNotExist", new Object[] { entity.getTempName() }));
      
      strNav = "go_syslog_templetListPage";
    }
    else
    {
      configBean.setCommonTempEntity(info);
      
      winsMode = 2;
      strNav = "go_syslog_templetAddPage";
    }
    return strNav;
  }
  
  public String preModify()
  {
    String strNav = "";
    
    SyslogCommonTempConfigBean configBean = SyslogViewUtils.getSyslogCommonTempConfigBean();
    
    SyslogTempletEntity entity = (SyslogTempletEntity)data.getRowData();
    
    SyslogTempletEntity info = commonTempletMgr.queryTempletById(entity.getTempId());
    if (null == info)
    {
      optResult.showFailureMsgInOtherPage(SM.getString("syslog.templet.error.tempNotExist", new Object[] { entity.getTempName() }));
      
      strNav = "go_syslog_templetListPage";
    }
    else
    {
      configBean.setCommonTempEntity(info);
      winsMode = 1;
      strNav = "go_syslog_templetAddPage";
    }
    return strNav;
  }
  
  public String preDelete()
  {
    SyslogTempletEntity entity = (SyslogTempletEntity)data.getRowData();
    try
    {
      commonTempletMgr.deleteTemplet(entity);
      
      optResult.showSuccessMsg(SM.getString("syslog.templet.opt.deleteSuccessMsg", new Object[] { entity.getTempName() }));
      
      fireDataChanged();
      
      Operlog.insertSuccessLog(SM.getString("syslog.templet.oplog.deleteOpt", new Object[] { entity.getTempName() }));
    }
    catch (SyslogException e)
    {
      switch (e.getErrorCode())
      {
      case 8: 
        optResult.showFailureMsg(SM.getString("syslog.templet.error.tempNotExist", new Object[] { entity.getTempName() }));
        
        Operlog.insertFailLog(SM.getString("syslog.templet.oplog.deleteOpt", new Object[] { entity.getTempName() }), SM.getString("syslog.templet.error.tempNotExist", new Object[] { entity.getTempName() }));
      }
    }
    Operlog.insertFailLog(SM.getString("syslog.templet.oplog.deleteOpt", new Object[] { entity.getTempName() }), e.getErrorMsg());
    
    optResult.showFailureMsg(e.getErrorMsg());
    
    return null;
  }
  
  public String columnAction()
  {
    SyslogTempletEntity entity = (SyslogTempletEntity)data.getRowData();
    
    String colName = actionColumn;
    
    SyslogTempletEntity info = commonTempletMgr.queryTempletById(entity.getTempId());
    if (null == info) {
      optResult.showFailureMsg(SM.getString("syslog.templet.error.tempNotExist", new Object[] { entity.getTempName() }));
    } else if (SM.getString("syslog.templet.list.templet.name").equalsIgnoreCase(colName)) {
      return viewTempDetail(entity);
    }
    fireDataChanged();
    return super.columnAction();
  }
  
  public String showTempDetail()
  {
    SyslogTempletEntity entity = (SyslogTempletEntity)data.getRowData();
    
    SyslogTempletEntity info = commonTempletMgr.queryTempletById(entity.getTempId());
    if (null == info) {
      optResult.showFailureMsg(SM.getString("syslog.templet.error.tempNotExist", new Object[] { entity.getTempName() }));
    } else {
      return viewTempDetail(info);
    }
    return null;
  }
  
  public String preAdd()
  {
    SyslogCommonTempConfigBean tempBean = SyslogViewUtils.getSyslogCommonTempConfigBean();
    
    tempBean.setShowDiaplay(false);
    tempBean.setCommonTempEntity(new SyslogTempletEntity());
    tempBean.setShowParame(false);
    tempBean.setHaveParame(false);
    winsMode = 0;
    return "go_syslog_templetAddPage";
  }
  
  public String onDelete()
  {
    String name = "";
    try
    {
      List<SyslogTempletEntity> delList = new ArrayList();
      for (Object entity : selectedList) {
        delList.add((SyslogTempletEntity)entity);
      }
      for (SyslogTempletEntity tmp : delList)
      {
        name = tmp.getTempName();
        commonTempletMgr.deleteTemplet(tmp);
        
        Operlog.insertSuccessLog(SM.getString("syslog.templet.oplog.deleteOpt", new Object[] { name }));
      }
      if (delList.size() == 1) {
        optResult.showSuccessMsg(SM.getString("syslog.templet.opt.deleteSuccessMsg", new Object[] { name }));
      } else {
        optResult.showSuccessMsg(SM.getString("syslog.templet.opt.deleteAllSuccessMsg"));
      }
      fireDataChanged();
    }
    catch (SyslogException e)
    {
      switch (e.getErrorCode())
      {
      case 8: 
        optResult.showFailureMsg(SM.getString("syslog.templet.error.tempNotExist", new Object[] { name }));
        
        Operlog.insertFailLog(SM.getString("syslog.templet.oplog.deleteOpt", new Object[] { name }), SM.getString("syslog.templet.error.tempNotExist", new Object[] { name }));
      }
    }
    Operlog.insertFailLog(SM.getString("syslog.templet.oplog.deleteOpt", new Object[] { name }), e.getErrorMsg());
    
    optResult.showFailureMsg(e.getErrorMsg());
    
    return null;
  }
  
  public String onBack()
  {
    return "go_syslog_templetListPage";
  }
  
  private String viewTempDetail(SyslogTempletEntity entity)
  {
    SyslogCommonTempConfigBean configBean = SyslogViewUtils.getSyslogCommonTempConfigBean();
    
    configBean.setCommonTempEntity(entity);
    return "go_syslog_templetDetailPage";
  }
  
  public void setCommonTempletMgr(SyslogCommonTempletMgr commonTempletMgr)
  {
    this.commonTempletMgr = commonTempletMgr;
  }
  
  public OperateResult getOptResult()
  {
    return optResult;
  }
  
  public void setOptResult(OperateResult optResult)
  {
    this.optResult = optResult;
  }
  
  public Integer getWinsMode()
  {
    return Integer.valueOf(winsMode);
  }
  
  public void setWinsMode(Integer winsMode)
  {
    this.winsMode = winsMode.intValue();
  }
  
  public String getSearchWord()
  {
    return searchWord;
  }
  
  public void setSearchWord(String searchWord)
  {
    this.searchWord = searchWord;
  }
  
  public String getWatermarkValue()
  {
    return watermarkValue;
  }
  
  public boolean isTestMode()
  {
    return testMode;
  }
  
  public void setTestMode(boolean testMode)
  {
    this.testMode = testMode;
  }
  
  public String getSyslogContent()
  {
    return syslogContent;
  }
  
  public void setSyslogContent(String syslogContent)
  {
    this.syslogContent = syslogContent;
  }
  
  public DataModel<SyslogTempletEntity> getTestData()
  {
    return testData;
  }
  
  public void setTestData(DataModel<SyslogTempletEntity> testData)
  {
    this.testData = testData;
  }
}

/* Location:
 * Qualified Name:     com.h3c.imc.syslog.templet.view.SyslogCommonTempListBean
 * Java Class Version: 7 (51.0)
 * JD-Core Version:    0.7.1
 */