package com.h3c.imc.syslog.templet.view;

import com.h3c.imc.common.ListResult;
import com.h3c.imc.common.QueryFilter;
import com.h3c.imc.common.Restriction;
import com.h3c.imc.common.StringManager;
import com.h3c.imc.common.faces.ColumnHeader;
import com.h3c.imc.common.faces.DynamicDataModel;
import com.h3c.imc.common.faces.FacesUtils;
import com.h3c.imc.faces.entity.OperateResult;
import com.h3c.imc.plat.operator.view.OperatorLoginInfo;
import com.h3c.imc.res.memRes.MemQueryCondition;
import com.h3c.imc.syslog.common.view.SyslogSortableList;
import com.h3c.imc.syslog.entity.SyslogTempletEntity;
import com.h3c.imc.syslog.rule.view.RuleConfigBean;
import com.h3c.imc.syslog.templet.func.SyslogCommonTempletMgr;
import java.util.ArrayList;
import java.util.List;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang.StringUtils;

public class SyslogTempletSelectBean
  extends SyslogSortableList
{
  private SyslogTempletEntity selectEntity;
  private SyslogTempletSelectIf syslogTempetSelect;
  private SyslogCommonTempletMgr commonTempletMgr;
  private OperateResult optResult = new OperateResult();
  protected static final StringManager SM = StringManager.getManager("com.h3c.imc.syslog");
  private static int who;
  private String searchWord = "";
  
  public SyslogTempletSelectBean()
  {
    paging = true;
    setShowSelectOne(true);
    showSelectAll = false;
    showDelete = false;
    ascending = true;
    sortColumn = "tempName";
    lazying = false;
  }
  
  public void initPage()
  {
    selectEntity = new SyslogTempletEntity();
    
    HttpServletRequest request = FacesUtils.getServletRequest();
    selectedOneRowIndex = Long.valueOf(0L);
    
    String beanName = request.getParameter("beanName");
    if (beanName != null) {
      onReset();
    }
    if (request.getParameter("who") != null)
    {
      sortColumn = "tempName";
      who = Integer.valueOf(request.getParameter("who")).intValue();
    }
    String ruleContentStr;
    String ruleContentStr;
    if (who == 1)
    {
      if (beanName != null) {
        syslogTempetSelect = ((SyslogTempletSelectIf)FacesUtils.getValueExpressionObject("#{" + beanName + "}"));
      }
      queryConditionChanged();
      if ((data == null) || (data.getRowCount() == 0))
      {
        optResult.showFailureMsg(SM.getString("sys.template.no"));
      }
      else
      {
        RuleConfigBean ruleConfigBeanObj = (RuleConfigBean)FacesUtils.getValueExpressionObject("#{syslogRuleConfigBean}");
        
        ruleContentStr = null;
        if ((ruleConfigBeanObj != null) && ((ruleConfigBeanObj instanceof RuleConfigBean)))
        {
          RuleConfigBean ruleConfigBean = ruleConfigBeanObj;
          ruleContentStr = ruleConfigBean.getTempContent();
        }
        if ((!ruleConfigBeanObj.getTempContent().isEmpty()) && 
          (!StringUtils.isEmpty(ruleContentStr)))
        {
          Object wrappedData = data.getWrappedData();
          List<SyslogTempletEntity> entities = (List)wrappedData;
          for (SyslogTempletEntity entity : entities) {
            if (ruleContentStr.equals(entity.getTempContent()))
            {
              selectEntity = entity;
              break;
            }
          }
        }
      }
    }
    else
    {
      if (beanName != null) {
        syslogTempetSelect = ((SyslogTempletSelectIf)FacesUtils.getValueExpressionObject("#{" + beanName + "}"));
      }
      queryConditionChanged();
      if ((data == null) || (data.getRowCount() == 0))
      {
        optResult.showFailureMsg(SM.getString("sys.template.no"));
      }
      else
      {
        RuleConfigBean ruleConfigBeanObj = (RuleConfigBean)FacesUtils.getValueExpressionObject("#{syslogRuleConfigBean}");
        
        ruleContentStr = null;
        if ((ruleConfigBeanObj != null) && ((ruleConfigBeanObj instanceof RuleConfigBean)))
        {
          RuleConfigBean ruleConfigBean = ruleConfigBeanObj;
          ruleContentStr = ruleConfigBean.getRecoveryContent();
        }
        if ((!ruleConfigBeanObj.getRecoveryContent().isEmpty()) && 
          (!StringUtils.isEmpty(ruleContentStr)))
        {
          List<SyslogTempletEntity> entities = (List)data.getWrappedData();
          for (SyslogTempletEntity entity : entities) {
            if (ruleContentStr.equals(entity.getTempContent()))
            {
              selectEntity = entity;
              break;
            }
          }
        }
      }
    }
  }
  
  public String onConfirm()
  {
    RuleConfigBean ruleConfig = (RuleConfigBean)FacesUtils.getValueExpressionObject("#{syslogRuleConfigBean}");
    if (selectEntity == null)
    {
      optResult.showFailureMsgInOtherPage("syslog.templet.error.tempNotExist");
      return null;
    }
    selectEntity = commonTempletMgr.queryTempletById(selectEntity.getTempId());
    syslogTempetSelect.setTempletEntity(selectEntity, who);
    
    ruleConfig.initData();
    
    return null;
  }
  
  public String getDeleteAttibute()
  {
    HttpSession session = (HttpSession)FacesContext.getCurrentInstance().getExternalContext().getSession(false);
    
    session.removeAttribute("closeTempSelectWin");
    return null;
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
    super.fireDataChanged();
    refreshData();
    return null;
  }
  
  public void onFilter()
  {
    queryConditionChanged();
  }
  
  public String onReset()
  {
    searchWord = "";
    return super.refresh();
  }
  
  protected void queryData()
  {
    MemQueryCondition mQuery = new MemQueryCondition(operatorLoginInfo.getResources());
    QueryFilter qf = new QueryFilter();
    if ((null != searchWord) && (StringUtils.isNotBlank(searchWord))) {
      qf.addRestriction(Restriction.or(new Restriction[] { Restriction.like("tempName", searchWord) }));
    }
    qf.addOrder(sortColumn, ascending, false);
    mQuery.setQueryFilter(qf);
    List tempList = commonTempletMgr.queryAllTemplets(mQuery);
    syncData(new ListResult(tempList, tempList.size()));
  }
  
  public OperateResult getOptResult()
  {
    return optResult;
  }
  
  public void setOptResult(OperateResult optResult)
  {
    this.optResult = optResult;
  }
  
  public void setCommonTempletMgr(SyslogCommonTempletMgr commonTempletMgr)
  {
    this.commonTempletMgr = commonTempletMgr;
  }
  
  public SyslogTempletSelectIf getSyslogTempetSelect()
  {
    return syslogTempetSelect;
  }
  
  public void setSyslogTempetSelect(SyslogTempletSelectIf syslogTempetSelect)
  {
    this.syslogTempetSelect = syslogTempetSelect;
  }
  
  public SyslogTempletEntity getSelectEntity()
  {
    return selectEntity;
  }
  
  public void setSelectEntity(SyslogTempletEntity selectEntity)
  {
    this.selectEntity = selectEntity;
  }
  
  public boolean isRenderTable()
  {
    if ((data == null) || (data.getRowCount() == 0)) {
      return false;
    }
    return true;
  }
  
  public int getWho()
  {
    return who;
  }
  
  public String getSearchWord()
  {
    return searchWord;
  }
  
  public void setSearchWord(String searchWord)
  {
    this.searchWord = searchWord;
  }
}

/* Location:
 * Qualified Name:     com.h3c.imc.syslog.templet.view.SyslogTempletSelectBean
 * Java Class Version: 7 (51.0)
 * JD-Core Version:    0.7.1
 */